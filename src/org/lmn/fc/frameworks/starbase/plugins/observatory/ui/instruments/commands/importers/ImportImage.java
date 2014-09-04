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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * ImportImage.
 */

public final class ImportImage implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ObservatoryConstants
    {
    /***********************************************************************************************
     * importImage().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportImage(final ObservatoryInstrumentDAOInterface dao,
                                                         final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportImage.importImage() ";
        final int PARAMETER_COUNT = 2;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect two parameters, the filename to import and the format
        listParameters = commandType.getParameterList();
        responseMessage = null;

        // TODO REVIEW Initialise all DAO data containers if possible...
        // dao.clearData();

        // Check the Command parameters before continuing to retrieve the data file
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (listParameters.get(1) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(1).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;
                final String strFormat;
                final DataFormat dataFormat;

                strFilename = listParameters.get(0).getValue();
                strFormat = listParameters.get(1).getValue();

                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                dataFormat = DataFormat.getDataFormatForName(strFormat);

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "[filename=" + strFilename + "] [format=" + dataFormat.getName() + "]");

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_IMAGE
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_FILENAME + strFilename + TERMINATOR + SPACE
                                                       + METADATA_FORMAT + strFormat + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                if ((strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename)))
                    {
                    final DataTranslatorInterface translator;

                    // Instantiate the translator required by the DataFormat
                    translator = DataTranslatorHelper.instantiateTranslator(dataFormat.getTranslatorClassname());

                    if (translator != null)
                        {
                        // Set the translator for this DAO (until changed by another command)
                        dao.setTranslator(translator);
                        dao.getTranslator().initialiseTranslator();

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_IMAGE
                                                               + METADATA_ACTION_TRANSLATING
                                                               + METADATA_FILENAME + strFilename + TERMINATOR + SPACE
                                                               + METADATA_FORMAT + strFormat + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());

                        // Make an image from the file and put in the DAO
                        if (dao.getTranslator().importImage(strFilename,
                                                            dao.getEventLogFragment(),
                                                            dao.getObservatoryClock()))
                            {
                            // See if there's anything we need to know...
                            DataTranslatorHelper.addTranslatorMessages(dao.getTranslator(),
                                                                       dao.getEventLogFragment(),
                                                                       dao.getObservatoryClock(),
                                                                       dao.getLocalHostname());

                            // Check that we are still running,
                            // to save time if the User has stopped the Instrument
                            if (InstrumentState.isDoingSomething(dao.getHostInstrument()))
                                {
                                // Establish the identity of this Instrument using only Metadata from the Import
                                dao.deriveDAOIdentityFromImport(dao.getTranslator(), false);
                                }
                            else
                                {
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_IMAGE
                                                                        + METADATA_ACTION_IMPORT
                                                                        + METADATA_RESULT + "DAO is not running" + TERMINATOR,
                                                                   dao.getLocalHostname(),
                                                                   dao.getObservatoryClock());
                                }
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_IMAGE
                                                                    + METADATA_ACTION_IMPORT
                                                                    + METADATA_RESULT + "DAO has no data" + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_IMAGE
                                                                + METADATA_ACTION_IMPORT
                                                                + METADATA_RESULT + "Unable to instantiate translator" + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_IMAGE
                                                            + METADATA_ACTION_IMPORT
                                                            + METADATA_RESULT + "Import filename is not valid" + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                // Were we successful in getting some data?
                if (dao.getImageData() != null)
                    {
                    // If we get here, we have the image...
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_IMAGE
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_FILENAME + strFilename + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());

                    REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                    InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                    // Create the ResponseMessage
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            catch (Exception exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }
            }

        // Help the GC?
        dao.setTranslator(null);
        dao.setFilter(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        // Construct INVALID_PARAMETER
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
