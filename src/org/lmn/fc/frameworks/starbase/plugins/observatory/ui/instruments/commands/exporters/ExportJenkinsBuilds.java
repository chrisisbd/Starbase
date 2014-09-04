// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorInstrumentPanel;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.SimpleNewsreaderPanel;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.UIComponentPlugin;

import java.util.List;


/***************************************************************************************************
 * ExportJenkinsBuilds.
 */

public final class ExportJenkinsBuilds implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportJenkinsBuilds().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportJenkinsBuilds(final ObservatoryInstrumentDAOInterface dao,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ExportJenkinsBuilds.doExportJenkinsBuilds()";
        final int PARAMETER_COUNT = 2;
        final int INDEX_FILENAME = 0;
        final int INDEX_TIMESTAMP = 1;
        final CommandType cmdJenkins;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdJenkins = (CommandType)commandmessage.getCommandType().copy();

        // We expect two parameters, the filename and timestamp
        listParameters = cmdJenkins.getParameterList();

        // Do not change any DAO data containers!

        // Check the parameters before continuing
        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TIMESTAMP) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_TIMESTAMP).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;
                final boolean boolTimestamp;
                boolean boolSuccess;

                strFilename = listParameters.get(INDEX_FILENAME).getValue();
                boolTimestamp = Boolean.parseBoolean(listParameters.get(INDEX_TIMESTAMP).getValue());

                // Prepare for the worst
                boolSuccess = false;

                // The text we want to export should be in the DAO RawData, element zero,
                // assuming that the Jenkins RSS reader has been running???
                // That didn't work, so obtained via the UI...

                if (((dao.getHostInstrument() != null))
                    && (dao.getHostInstrument().getInstrumentPanel() != null)
                    && (dao.getHostInstrument().getInstrumentPanel() instanceof CommunicatorInstrumentPanel))
                    {
                    final UIComponentPlugin uiJenkins;

                    uiJenkins = dao.getHostInstrument().getInstrumentPanel().getJenkinsTab();

                    if ((uiJenkins != null)
                        && (uiJenkins instanceof SimpleNewsreaderPanel))
                        {
                        final StringBuffer buffer;

                        buffer = new StringBuffer(((SimpleNewsreaderPanel)uiJenkins).getNewsText());

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
                                                   METADATA_EXCEPTION
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IndexOutOfBoundsException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_JENKINS,
                                                                                     METADATA_ACTION_EXPORT));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdJenkins,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
