// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.List;


/***************************************************************************************************
 * ExportMetadata.
 */

public final class ExportMetadata implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportMetadata().
     * Saves the current Metadata at the specified location.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportMetadata(final ObservatoryInstrumentDAOInterface dao,
                                                            final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ExportMetadata.exportMetadata()";
        final int PARAMETER_COUNT = 3;
        final CommandType cmdExportMetadata;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdExportMetadata = (CommandType)commandmessage.getCommandType().copy();

        // We expect three parameters, the filename, timestamp flag and format
        listParameters = cmdExportMetadata.getParameterList();
        responseMessage = null;

        // Do not change any DAO data containers!
        dao.clearEventLogFragment();

        // Check the DAO data and the Command parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (listParameters.get(1) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(1).getInputDataType().getDataTypeName()))
            && (listParameters.get(2) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(2).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;
                final boolean boolTimestamp;
                final String strFormat;
                final DataFormat dataFormat;
                final DataTranslatorInterface translator;
                boolean boolSuccess;

                strFilename = listParameters.get(0).getValue();
                boolTimestamp = Boolean.parseBoolean(listParameters.get(1).getValue());
                strFormat = listParameters.get(2).getValue();

                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                dataFormat = DataFormat.getDataFormatForName(strFormat);

                // Instantiate the translator required by the DataFormat
                translator = DataTranslatorHelper.instantiateTranslator(dataFormat.getTranslatorClassname());

                boolSuccess = false;

                if (translator != null)
                    {
                    final List<Metadata> listMetadata;

                    // Set the translator for this DAO (until changed by another command)
                    dao.setTranslator(translator);
                    dao.getTranslator().initialiseTranslator();

                    // Combine the Metadata produced by the DAO with that from the Instrument and Observatory
                    listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                 dao.getHostInstrument().getContext().getObservatory(),
                                                                                 dao.getHostInstrument(),
                                                                                 dao, dao.getWrappedData(),
                                                                                 SOURCE,
                                                                                 LOADER_PROPERTIES.isMetadataDebug());

                    boolSuccess = dao.getTranslator().exportMetadata(MetadataHelper.collectMetadataMetadata(dao.getHostInstrument(),
                                                                                                            dao,
                                                                                                            dao.getWrappedData()),
                                                                     listMetadata,
                                                                     strFilename,
                                                                     boolTimestamp,
                                                                     dao.getEventLogFragment(),
                                                                     dao.getObservatoryClock());
                    // See if there's anything we need to know...
                    DataTranslatorHelper.addTranslatorMessages(dao.getTranslator(),
                                                               dao.getEventLogFragment(),
                                                               dao.getObservatoryClock(),
                                                               dao.getLocalHostname());
                    }

                if (boolSuccess)
                    {
                    // Create the ResponseMessage
                    // Just feed the existing DaoData back round again
                    cmdExportMetadata.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    dao.setRawDataChannelCount(dao.getWrappedData().getRawDataChannelCount());
                    dao.setTemperatureChannel(dao.getWrappedData().hasTemperatureChannel());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        cmdExportMetadata);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                }
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              cmdExportMetadata,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
