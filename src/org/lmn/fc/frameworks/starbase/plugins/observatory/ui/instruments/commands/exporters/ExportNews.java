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
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorInstrumentPanel;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.ui.SimpleNewsreaderUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.UIComponentPlugin;

import java.util.List;


/***************************************************************************************************
 * ExportNews.
 */

public final class ExportNews implements FrameworkConstants,
                                         FrameworkStrings,
                                         FrameworkMetadata,
                                         FrameworkSingletons,
                                         ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportNews().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportNews(final CommunicatorDAOInterface dao,
                                                        final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ExportNews.doExportNews()";
        final int PARAMETER_COUNT = 2;
        final int INDEX_FILENAME = 0;
        final int INDEX_TIMESTAMP = 1;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Prepare for the worst
        commandType.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
        responseMessage = null;
        boolSuccess = false;

        // We expect two parameters, the filename and timestamp
        listParameters = commandType.getParameterList();

        // Do not change any DAO data containers!

        // Check the parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TIMESTAMP) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_TIMESTAMP).getInputDataType().getDataTypeName())))
            {
            final String strFilename;
            final boolean boolTimestamp;

            strFilename = listParameters.get(INDEX_FILENAME).getValue();
            boolTimestamp = Boolean.parseBoolean(listParameters.get(INDEX_TIMESTAMP).getValue());

            // The text we want to export should be in the DAO RawData, element zero,
            // assuming that the Newsreader has been running???
            // That didn't work, so obtained via the UI...

            if (((dao.getHostInstrument() != null))
                && (dao.getHostInstrument().getInstrumentPanel() != null)
                && (dao.getHostInstrument().getInstrumentPanel() instanceof CommunicatorInstrumentPanel))
                {
                final UIComponentPlugin uiNewsreader;
                final StringBuffer buffer;

                uiNewsreader = dao.getHostInstrument().getInstrumentPanel().getNewsreaderTab();

                if ((uiNewsreader != null)
                    && (uiNewsreader instanceof SimpleNewsreaderUIComponent))
                    {
                    buffer = new StringBuffer(((SimpleNewsreaderUIComponent)uiNewsreader).getNewsText());

                    boolSuccess = DataExporter.exportStringBuffer(strFilename,
                                                                  boolTimestamp,
                                                                  DataFormat.HTML,
                                                                  buffer,
                                                                  dao.getEventLogFragment(),
                                                                  dao.getObservatoryClock());
                    }
                }

            if (boolSuccess)
                {
                // Create the ResponseMessage
                // Just feed the existing DaoData back round again
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                dao.setRawDataChannelCount(dao.getWrappedData().getRawDataChannelCount());
                dao.setTemperatureChannel(dao.getWrappedData().hasTemperatureChannel());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                    commandmessage,
                                                                                    commandType);
                }
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
