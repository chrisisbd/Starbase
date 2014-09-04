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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.news;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;

import static org.lmn.fc.model.logging.EventStatus.INFO;


/***************************************************************************************************
 * SetUpdatePeriod.
 */

public final class SetUpdatePeriod implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ObservatoryConstants
    {
    // String Resources
    private static final String PARAMETER_UPDATE_PERIOD = "UpdatePeriod";

    private static final int ONE_MINUTE_MILLIS = 60000;

    private static int intUpdatePeriodMillis;


    /***********************************************************************************************
     * doSetUpdatePeriod().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetUpdatePeriod(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetUpdatePeriod.doSetUpdatePeriod()";
        final int PARAMETER_COUNT = 1;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        dao.getEventLogFragment().clear();
        dao.getInstrumentLogFragment().clear();

        // We expect one parameter, the update period
        listParameters = commandType.getParameterList();
        responseMessage = null;

        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (PARAMETER_UPDATE_PERIOD.equals(listParameters.get(0).getName()))
            && (dao.getHostInstrument() != null)
            && (commandmessage.getDAO() != null))
            {
            try
                {
                final String strUpdatePeriodMins;
                final int intUpdatePeriodMins;

                strUpdatePeriodMins = listParameters.get(0).getValue();
                intUpdatePeriodMins = Integer.parseInt(strUpdatePeriodMins);
                setUpdatePeriodMillis(intUpdatePeriodMins * ONE_MINUTE_MILLIS);

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   INFO,
                                                   METADATA_TARGET_COMMUNICATOR
                                                        + METADATA_ACTION_SET
                                                        + METADATA_ITEM + PARAMETER_UPDATE_PERIOD + TERMINATOR_SPACE
                                                        + METADATA_VALUE + strUpdatePeriodMins.trim() + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);

                // Create the SUCCESS ResponseMessage
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

//                dao.setRawDataChannelCount(0);
//                dao.setTemperatureChannel(false);

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                    commandmessage,
                                                                                    commandType);
                }

            catch (NumberFormatException exception)
                {
                setUpdatePeriodMillis(ONE_MINUTE_MILLIS);
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                        + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                        + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }
            }

        // Did we still fail?
        if (responseMessage == null)
            {
            setUpdatePeriodMillis(ONE_MINUTE_MILLIS);

            // Invalid Parameters
            // Create the ResponseMessage
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           commandType,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            commandType));
            }

        // Something has changed, we may need to refresh a browser etc.
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        return (responseMessage);
        }


    /***********************************************************************************************
     * Get the period to wait before updating the Communicator News.
     *
     * @return int
     */

    public static int getUpdatePeriodMillis()
        {
        return (intUpdatePeriodMillis);
        }


    /***********************************************************************************************
     * Set the period to wait before updating the Communicator News.
     *
     * @param period
     */

    private static void setUpdatePeriodMillis(final int period)
        {
        intUpdatePeriodMillis = period;
        }
    }
