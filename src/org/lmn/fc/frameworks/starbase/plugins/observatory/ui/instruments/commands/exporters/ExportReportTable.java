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
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
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
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.util.List;


/***************************************************************************************************
 * ExportReportTable.
 */

public final class ExportReportTable implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons,
                                                ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportReportTable().
     * Saves the data from a general ReportTable at the specified location.
     * The DatasetType is used to refine the Metadata describing the columns.
     * Optional extra metadata may be added to the export.
     *
     * @param dao
     * @param source
     * @param commandmessage
     * @param metadatametadata
     * @param metadata
     * @param infercolumnmetadata
     * @param datasettype
     * @param report
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportReportTable(final ObservatoryInstrumentDAOInterface dao,
                                                               final String source,
                                                               final CommandMessageInterface commandmessage,
                                                               final List<Metadata> metadatametadata,
                                                               final List<Metadata> metadata,
                                                               final boolean infercolumnmetadata,
                                                               final DatasetType datasettype,
                                                               final ReportTablePlugin report)
        {
        final String SOURCE;
        final int PARAMETER_COUNT = 3;
        final int INDEX_FILENAME = 0;
        final int INDEX_TIMESTAMP = 1;
        final int INDEX_FORMAT = 2;
        final CommandType cmdExport;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        SOURCE = source;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdExport = (CommandType)commandmessage.getCommandType().copy();

        // We expect three parameters, the filename, timestamp and format
        listParameters = cmdExport.getParameterList();

        // Do not change any DAO data containers! (since these are what we wish to export)

        // Check the parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TIMESTAMP) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_TIMESTAMP).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;
                final boolean boolTimestamp;
                final String strFormat;
                final DataFormat dataFormat;
                final DataTranslatorInterface translator;
                boolean boolSuccess;

                strFilename = listParameters.get(INDEX_FILENAME).getValue();
                boolTimestamp = Boolean.parseBoolean(listParameters.get(INDEX_TIMESTAMP).getValue());
                strFormat = listParameters.get(INDEX_FORMAT).getValue();

                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                dataFormat = DataFormat.getDataFormatForName(strFormat);

                // Instantiate the translator required by the DataFormat
                translator = DataTranslatorHelper.instantiateTranslator(dataFormat.getTranslatorClassname());

                boolSuccess = false;

                if (translator != null)
                    {
                    // Set the translator for this DAO (until changed by another command)
                    dao.setTranslator(translator);
                    dao.getTranslator().initialiseTranslator();

                    // The metadata and/or data could still be null
                    boolSuccess = dao.getTranslator().exportReportTable(metadatametadata,
                                                                        metadata,
                                                                        infercolumnmetadata,
                                                                        datasettype,
                                                                        report,
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
                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else
                    {
                    strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                exception.printStackTrace();
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_EXCEPTION
                                                       + dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]"
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_REPORT,
                                                                                     METADATA_ACTION_EXPORT));
            }

        ObservatoryInstrumentHelper.runGarbageCollector();

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdExport,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
