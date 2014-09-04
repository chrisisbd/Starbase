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
import org.lmn.fc.common.datatranslators.DataExporter;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
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

import java.util.List;


/***************************************************************************************************
 * ExportChart.
 */

public final class ExportChart implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportChart().
     * Saves the current chart as an image at the specified location.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportChart(final ObservatoryInstrumentDAOInterface dao,
                                                         final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ExportChart.exportChart()";
        final int PARAMETER_COUNT = 5;
        final int INDEX_FILENAME = 0;
        final int INDEX_TIMESTAMP = 1;
        final int INDEX_FORMAT = 2;
        final int INDEX_WIDTH = 3;
        final int INDEX_HEIGHT = 4;
        final CommandType cmdExportChart;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debug(boolDebug, SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdExportChart = (CommandType)commandmessage.getCommandType().copy();

        // We expect five parameters, the filename, timestamp flag, type, width and height
        listParameters = cmdExportChart.getParameterList();

        // Do not change any DAO data containers!
        dao.clearEventLogFragment();

        // Check the parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TIMESTAMP) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_TIMESTAMP).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_WIDTH) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_WIDTH).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_HEIGHT) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_HEIGHT).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;
                final boolean boolTimestamp;
                final String strType;
                final int intWidth;
                final int intHeight;
                final boolean boolSuccess;

                strFilename = listParameters.get(INDEX_FILENAME).getValue();
                boolTimestamp = Boolean.parseBoolean(listParameters.get(INDEX_TIMESTAMP).getValue());
                strType = listParameters.get(INDEX_FORMAT).getValue();
                // Width and Height are DECIMAL
                intWidth = Integer.parseInt(listParameters.get(INDEX_WIDTH).getValue());
                intHeight = Integer.parseInt(listParameters.get(INDEX_HEIGHT).getValue());

                boolSuccess = DataExporter.exportChart(dao,
                                                       dao.getChartUI(),
                                                       MetadataHelper.collectMetadataForExportFromDAO(dao, false),
                                                       strFilename,
                                                       boolTimestamp,
                                                       strType,
                                                       intWidth,
                                                       intHeight,
                                                       dao.getEventLogFragment(),
                                                       dao.getObservatoryClock(),
                                                       false);
                if (boolSuccess)
                    {
                    // Create the ResponseMessage
                    // Just feed the existing DaoData back round again
                    dao.setRawDataChannelCount(dao.getWrappedData().getRawDataChannelCount());
                    dao.setTemperatureChannel(dao.getWrappedData().hasTemperatureChannel());

                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_CHART
                                                           + METADATA_ACTION_EXPORT
                                                           + METADATA_RESULT + "Chart could not be exported" + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_CHART,
                                                                                     METADATA_ACTION_EXPORT));
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
        ObservatoryInstrumentHelper.runGarbageCollector();

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdExportChart,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
