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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.audio;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.SignalGeneratorMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;
import java.util.Vector;

import static org.lmn.fc.model.logging.EventStatus.WARNING;


/***************************************************************************************************
 * RunOscillator.
 */

public final class RunOscillator implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ObservatoryConstants
    {
    /***********************************************************************************************
     * doRunOscillator().
     *
     * @param dao
     * @param oscillator
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doRunOscillator(final ObservatoryInstrumentDAOInterface dao,
                                                           final OscillatorInterface oscillator,
                                                           final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "RunOscillator.doRunOscillator() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_RUN = 0;
        final CommandType cmdRun;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final Vector<Vector> vecDaoLogFragment;

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdRun = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        dao.getEventLogFragment().clear();
        dao.getInstrumentLogFragment().clear();

        // We expect one parameter, a control boolean
        listParameters = cmdRun.getParameterList();

        vecDaoLogFragment = commandmessage.getDAO().getEventLogFragment();

        // Create a new SwingWorker, or stop the existing
        if ((dao.getHostInstrument() != null)
            && (oscillator != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_RUN) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_RUN).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strMode;
                final SignalGeneratorMode mode;

                strMode = listParameters.get(INDEX_RUN).getValue();
                mode = SignalGeneratorMode.getSignalGeneratorModeForName(strMode);

                if ((!oscillator.isRunning())
                    && (mode != null)
                    && (!SignalGeneratorMode.STOP.equals(mode)))
                    {
                    final boolean boolSuccess;

                    // We've been asked to Run a stopped Oscillator
                    boolSuccess = oscillator.initialise(dao);

                    if (boolSuccess)
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           ResponseMessageStatus.SUCCESS.getEventStatus(),
                                                           METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                                + METADATA_ACTION_START,
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        LogHelper.updateEventLogFragment(dao);

                        // Create the SUCCESS ResponseMessage
                        strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
                                                           METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                               + METADATA_ACTION_START
                                                               + METADATA_MESSAGE + OscillatorInterface.MSG_FAILED_TO_START + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        LogHelper.updateEventLogFragment(dao);

                        oscillator.setRunning(false);
                        oscillator.dispose();

                        strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }
                    }
                else if ((oscillator.isRunning())
                         && (mode != null)
                         && (!SignalGeneratorMode.STOP.equals(mode)))
                    {
                    // We've been asked to Run a running Oscillator, which must fail, with no action taken
                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                       WARNING,
                                                       METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                           + METADATA_ACTION_START
                                                           + METADATA_MESSAGE + OscillatorInterface.MSG_ALREADY_RUNNING + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    LogHelper.updateEventLogFragment(dao);

                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else if ((oscillator.isRunning())
                         && (mode != null)
                         && (SignalGeneratorMode.STOP.equals(mode)))
                    {
                    // We've been asked to Stop a running Oscillator
                    // Put the Timeout back to what it should be for a single default command
                    TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

                    // We must try to stop the Oscillator
                    oscillator.setRunning(false);
                    oscillator.dispose();

                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                       ResponseMessageStatus.SUCCESS.getEventStatus(),
                                                       METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                        + METADATA_ACTION_STOP,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    LogHelper.updateEventLogFragment(dao);

                    // Create the SUCCESS ResponseMessage
                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else if ((!oscillator.isRunning())
                         && (mode != null)
                         && (SignalGeneratorMode.STOP.equals(mode)))
                    {
                    // We've been asked to Stop a stopped Oscillator, which must fail, with no action taken
                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                       WARNING,
                                                       METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                           + METADATA_ACTION_STOP
                                                           + METADATA_MESSAGE + OscillatorInterface.MSG_ALREADY_STOPPED + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    LogHelper.updateEventLogFragment(dao);

                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                       METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                           + METADATA_ACTION_RUN
                                                           + METADATA_RESULT
                                                           + MSG_UNSUPPORTED_MODE
                                                           + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    LogHelper.updateEventLogFragment(dao);

                    // We must try to stop the Oscillator
                    oscillator.setRunning(false);
                    oscillator.dispose();

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }

            // This should have been trapped by Regex
            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                       + METADATA_ACTION_RUN
                                                       + METADATA_RESULT
                                                       + MSG_UNSUPPORTED_MODE
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // The XML configuration was inappropriate
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET
                                                                                         + SOURCE.trim()
                                                                                         + TERMINATOR,
                                                                                     METADATA_ACTION_COMMAND));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdRun,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
