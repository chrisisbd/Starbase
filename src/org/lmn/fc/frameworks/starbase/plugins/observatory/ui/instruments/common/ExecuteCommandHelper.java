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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecutionContextInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.SimplifiedParameterHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.frameworks.starbase.portcontroller.events.CommandLifecycleEventInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


/***************************************************************************************************
 * ExecuteCommandHelper.
 */

public final class ExecuteCommandHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   FrameworkXpath,
                                                   ObservatoryConstants
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    private static final boolean NOTIFY_MONITORS = true;


    /**********************************************************************************************/
    /* Command Execution                                                                          */
    /***********************************************************************************************
     * Execute a StarScript Command (assumed to be valid).
     * Use the DAO attached to the Instrument.
     * Return true if the Command was executed.
     * The ObservationMetadata in the DAO Wrapper are updated with the Parameter Values
     * used to invoke the Command.
     *
     * @param obsinstrument
     * @param instrumentxml
     * @param module
     * @param command
     * @param executionparameters
     * @param starscript
     * @param isrepeating
     * @param repeatnumber
     * @param repeattext
     * @param errors
     *
     * @return boolean
     */

    public static synchronized boolean executeCommand(final ObservatoryInstrumentInterface obsinstrument,
                                                      final Instrument instrumentxml,
                                                      final XmlObject module,
                                                      final CommandType command,
                                                      final List<ParameterType> executionparameters,
                                                      final String starscript,
                                                      final boolean isrepeating,
                                                      final long repeatnumber,
                                                      final String repeattext,
                                                      final List<String> errors)
        {
        final String SOURCE = "ExecuteCommandHelper.executeCommand() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((obsinstrument != null)
            && (obsinstrument.getInstrument() != null)
            && (obsinstrument.getDAO() != null)
            && (errors != null))
            {
            boolSuccess = executeCommandOnDAO(obsinstrument,
                                              obsinstrument.getDAO(),
                                              instrumentxml,
                                              module,
                                              command,
                                              executionparameters,
                                              starscript,
                                              isrepeating,
                                              repeatnumber,
                                              repeattext,
                                              errors);
            }

        // Return false if we couldn't even try to execute it
        return (boolSuccess);
        }


    /***********************************************************************************************
     * Execute a StarScript Command (assumed to be valid) using a specified DAO,
     * i.e. not necessarily the DAO attached to the Instrument.
     * Return true if the Command was executed.
     * The ObservationMetadata in the DAO Wrapper are updated with the Parameter Values
     * used to invoke the Command.
     *
     * @param obsinstrument
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param executionparameters
     * @param starscript
     * @param isrepeating
     * @param repeatnumber
     * @param repeattext
     * @param errors
     *
     * @return boolean
     */

    public static synchronized boolean executeCommandOnDAO(final ObservatoryInstrumentInterface obsinstrument,
                                                           final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final List<ParameterType> executionparameters,
                                                           final String starscript,
                                                           final boolean isrepeating,
                                                           final long repeatnumber,
                                                           final String repeattext,
                                                           final List<String> errors)
        {
        final String SOURCE = "ExecuteCommandHelper.executeCommandOnDAO() ";
        boolean boolSuccess;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        boolSuccess = false;

        if ((obsinstrument != null)
           && (obsinstrument.getInstrument() != null)
           && (dao != null)
           && (errors != null))
            {
            dao.getResponseMessageStatusList().clear();
            dao.setExecutionStatus(ExecutionStatus.WAITING);

            LOGGER.debug(boolDebug,
                         Logger.CONSOLE_SEPARATOR_MAJOR);
            LOGGER.debug(boolDebug,
                         SOURCE + "START [execution.status=" + dao.getExecutionStatus().getName()
                                + "] [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                + "] [execution.status=" + dao.getExecutionStatus().getName()
                                + "] [instrument.state=" + obsinstrument.getInstrumentState().getName()
                                + "] [isrepeating=" + isrepeating
                                + "] " + DAOHelper.showPortState(dao.getPort())
                                + " [thread.group=" + REGISTRY.getThreadGroup().getName()
                                + "] [thread.name=" + Thread.currentThread().getName()
                                + "]");

            // Send the CommandMessage if we are able to
            if (DAOHelper.isCommandValid(obsinstrument,
                                         instrumentxml,
                                         module,
                                         command,
                                         starscript,
                                         dao.getPort()))
                {
                // Fire off another thread to do the Command Execution
                // This is the only place the Execute SwingWorker is set on the DAO
                dao.setExecuteWorker(new SwingWorker(REGISTRY.getThreadGroup(),
                                                     SOURCE + "SwingWorker")
                    {
                    public Object construct()
                        {
                        final CommandLifecycleEventInterface lifecycleEvent;
                        final CommandMessageInterface commandMessage;
                        ResponseMessageInterface responseMessage;
                        final boolean boolDebug;

                        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                                     || LOADER_PROPERTIES.isStaribusDebug()
                                     || LOADER_PROPERTIES.isStarinetDebug()
                                     || LOADER_PROPERTIES.isMetadataDebug()
                                     || LOADER_PROPERTIES.isThreadsDebug()
                                     || LOADER_PROPERTIES.isStateDebug());

                        LOGGER.debug(boolDebug,
                                     SOURCE + "--> construct() BEGIN  [command=" + starscript
                                           + "] [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                           + "] [execution.status=" + dao.getExecutionStatus().getName()
                                           + "] [instrument.state=" + obsinstrument.getInstrumentState().getName()
                                           + "] [isrepeating=" + isrepeating
                                           + "] " + DAOHelper.showPortState(dao.getPort())
                                           + " [thread.group=" + REGISTRY.getThreadGroup().getName()
                                           + "] [thread.name=" + Thread.currentThread().getName()
                                           + "]");

                        // The DAO only creates an EventLog *fragment* to be appended to the overall Log
                        dao.clearEventLogFragment();

                        // Prepare an un-timestamped CommandMessage
                        // ready to be sent to the Port, or to be invoked locally
                        commandMessage = dao.constructCommandMessage(dao,
                                                                     instrumentxml,
                                                                     module,
                                                                     command,
                                                                     starscript.trim());
                        commandMessage.setExecutionParameters(executionparameters);

                        // TODO REVIEW WAS****
                        if ((command.getSendToPort())
                            && (dao.getPort() != null))
                            {
                            LOGGER.debug((boolDebug),
                                          SOURCE + "Setting Port READY, clearing Tx and Rx queues");
                            dao.getPort().setPortBusy(false);

                            // TODO Review clearing of port
                            // Clear the Queues and Streams, since we know that all activity is over for now
                            // This is to trap anomalies in Timeouts
                            dao.getPort().clearQueues();
                            dao.getPort().getTxStream().reset();
                            dao.getPort().getRxStream().reset();
                            }
                        else
                            {
                            LOGGER.debug((boolDebug),
                                                   SOURCE + "Command is LOCAL, or Port was NULL, so leave Port alone");
                            }

                        // Inform the User of the change of state
                        if (isrepeating)
                            {
                            LOGGER.debug((boolDebug),
                                                   SOURCE + "Setting Instrument REPEATING");
                            obsinstrument.notifyInstrumentStateChangedEvent(this,
                                                                            obsinstrument,
                                                                            obsinstrument.getInstrumentState(),
                                                                            InstrumentState.REPEATING,
                                                                            repeatnumber,
                                                                            repeattext);
                            }
                        else
                            {
                            LOGGER.debug((boolDebug),
                                                   SOURCE + "Setting Instrument BUSY");
                            obsinstrument.notifyInstrumentStateChangedEvent(this,
                                                                            obsinstrument,
                                                                            obsinstrument.getInstrumentState(),
                                                                            InstrumentState.BUSY,
                                                                            0,
                                                                            UNEXPECTED);
                            }

                        // TODO WAS**** The Instrument and Port are now BUSY regardless of how we are executing the Command
                        if ((command.getSendToPort())
                            && (dao.getPort() != null))
                            {
                            LOGGER.debug((boolDebug),
                                                   SOURCE + "Setting Port BUSY");
                            dao.getPort().setPortBusy(true);
                            }

                        // Let the World know we are about to execute this Command,
                        // i.e. to transmit the CommandMessage
                        ObservatoryInstrumentHelper.timestampCommandMessage(commandMessage,
                                                                            dao.getObservatoryClock());

                        // Issue the notifyCommandLifecycleEvent() for the timestamped Command,
                        // but only if we are being monitored (the low-level detail may not be needed)
                        lifecycleEvent = beginCommandLifecycle(dao,
                                                               commandMessage,
                                                               NOTIFY_MONITORS);

                        //--------------------------------------------------------------------------
                        // Each Command has zero or one Response (e.g. a data packet)
                        // NOTE: A NULL ResponseMessage ALWAYS means a Timeout or Abort has occurred
                        // Ready now...

                        try
                            {
                            if (command.getSendToPort())
                                {
                                // No need for retries here
                                responseMessage = checkResetStaribusCommand(obsinstrument,
                                                                            dao,
                                                                            command,
                                                                            boolDebug);

                                // If the Command was not reset(Staribus) then carry on to execute normally
                                if (responseMessage == null)
                                    {
                                    LOGGER.debug((boolDebug),
                                                           SOURCE + "SendToPort StarScript Command [method=" + command.getIdentifier() + "]");

                                    // Keep retrying until a response is received or the User clicks ABORT
                                    for (int retryid = 0;
                                        ((retryid < TimeoutHelper.RETRY_COUNT)
                                            && (responseMessage == null)
                                            && (Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), this)));
                                        retryid++)
                                        {
                                        TimeoutHelper.logRetryEvent(dao, starscript, retryid);

                                        // Command Timeout handled in this call by Rx Queue wait loop
                                        // If we get TIMEOUT, try again until retries exhausted
                                        responseMessage = executeSendToPort(commandMessage,
                                                                            dao,
                                                                            obsinstrument,
                                                                            instrumentxml,
                                                                            module,
                                                                            command,
                                                                            errors,
                                                                            NOTIFY_MONITORS,
                                                                            boolDebug);
                                        }
                                    }

                                // We could leave here having executed reset(Staribus)
                                }
                            else
                                {
                                LOGGER.debug((boolDebug),
                                              SOURCE + "LOCAL StarScript Command [method=" + command.getIdentifier() + "]");

                                // Command Timeout handled in this call
                                // Retries are not required for LOCAL Commands
                                responseMessage = executeLocal(commandMessage,
                                                               obsinstrument,
                                                               dao,
                                                               instrumentxml,
                                                               module,
                                                               command,
                                                               NOTIFY_MONITORS,
                                                               errors,
                                                               boolDebug);
                                }

                            //--------------------------------------------------------------------------------------
                            // If the ResponseMessage was (still) null, then there was a TIMEOUT or an ABORT

                            // Obtain the ResponseStatus from the global variable in the DAO
                            if (responseMessage == null)
                                {
                                // If not continuing after an error,
                                // abandon any repeats if the Queue timed out
                                if ((!dao.continueOnError())
                                    && (dao.getRepeatTimer() != null))
                                    {
                                    dao.getRepeatTimer().stop();
                                    dao.setRepeatTimer(null);
                                    }

                                // Create an empty ResponseMessage since none supplied
                                responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                                               commandMessage,
                                                                                               instrumentxml,
                                                                                               module,
                                                                                               command,
                                                                                               AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                                                module,
                                                                                                                                                command));
                                }
                            else if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.TIMEOUT))
                                {
                                // If not continuing after an error,
                                // abandon any repeats if the Queue timed out
                                if ((!dao.continueOnError())
                                    && (dao.getRepeatTimer() != null))
                                    {
                                    dao.getRepeatTimer().stop();
                                    dao.setRepeatTimer(null);
                                    }
                                }

                            // Complete the Command Lifecycle if we are being monitored
                            completeCommandLifecycle(dao,
                                                     lifecycleEvent,
                                                     responseMessage,
                                                     SOURCE,
                                                     NOTIFY_MONITORS,
                                                     boolDebug);
                            }

                        catch (final Exception exception)
                            {
                            LOGGER.error(SOURCE + "Generic Exception during Command Execution " + exception.getMessage());
                            exception.printStackTrace();
                            }

                        LOGGER.debug(boolDebug,
                                     SOURCE + "--> construct() RETURN [command=" + starscript
                                            + "] [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                            + "] [execution.status=" + dao.getExecutionStatus().getName()
                                            + "] [instrument.state=" + obsinstrument.getInstrumentState().getName()
                                            + "] [isrepeating=" + isrepeating
                                            + "] " + DAOHelper.showPortState(dao.getPort())
                                            + " [thread.group=" + REGISTRY.getThreadGroup().getName()
                                            + "] [thread.name=" + Thread.currentThread().getName()
                                            + "]");
                        LOGGER.debug(boolDebug,
                                     Logger.CONSOLE_SEPARATOR_MINOR);

                        return (lifecycleEvent);
                        }

                    //------------------------------------------------------------------------------
                    // Display updates occur on the Event Dispatching Thread

                    public void finished()
                        {
                        final boolean boolDebug;

                        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                                     || LOADER_PROPERTIES.isStaribusDebug()
                                     || LOADER_PROPERTIES.isStarinetDebug()
                                     || LOADER_PROPERTIES.isMetadataDebug()
                                     || LOADER_PROPERTIES.isThreadsDebug()
                                     || LOADER_PROPERTIES.isStateDebug());

                        LOGGER.debug(boolDebug,
                                     Logger.CONSOLE_SEPARATOR_MAJOR);
                        LOGGER.debug(boolDebug,
                                     SOURCE + "--> finished() BEGIN [command=" + starscript
                                            + "] [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                            + "] [execution.status=" + dao.getExecutionStatus().getName()
                                            + "] [instrument.state=" + obsinstrument.getInstrumentState().getName()
                                            + "] [isrepeating=" + isrepeating
                                            + "] " + DAOHelper.showPortState(dao.getPort())
                                            + " [thread.group=" + REGISTRY.getThreadGroup().getName()
                                            + "] [thread.name=" + Thread.currentThread().getName()
                                           + "]");

                        //--------------------------------------------------------------------------
                        // First, handle any data passed back
                        // get() returns a CommandLifecyleEvent, possibly containing wrapped DAO data

                        if ((SwingUtilities.isEventDispatchThread())
                            && (get() != null)
                            && (get() instanceof CommandLifecycleEventInterface))

                            // REMOVED 2013-04-06 && (!dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))

                            {
                            final CommandMessageInterface commandMessage;
                            final ResponseMessageInterface responseMessage;

                            // The CommandMessage carries the ResponseValue
                            commandMessage = ((CommandLifecycleEventInterface)get()).getCommandMessage();

                            // The ResponseMessage carries all data from the Instrument
                            responseMessage = ((CommandLifecycleEventInterface)get()).getResponseMessage();

                            // Did the DAO come back with valid data?
                            if (responseMessage != null)
                                {
                                // Pass the DAO data from the ResponseMessage to the DAO
                                // Remember that this may be NULL (No ResponseValue)
                                // This contains the EventLog *fragment* to be appended to the overall log
                                // This is the only call to setWrappedData() with real data (one other to clear data)

                                if (responseMessage.getWrappedData() != null)
                                    {
                                    LOGGER.debug(boolDebug,
                                                 SOURCE + "$ SET WRAPPED ON DAO +++++++++++++++++++++++++++++++++++");
                                    LOGGER.debug(boolDebug,
                                                 SOURCE + "$ DAO is " + dao.getClass().getName());
                                    LOGGER.debug(boolDebug,
                                                 SOURCE + "$ Response Wrapped DAO is " + responseMessage.getWrappedData().getWrappedDAO().getClass().getName());
                                    dao.setWrappedData(responseMessage.getWrappedData());

                                    // Remove the reference to the DAO Wrapped data in the ResponseMessage!
                                    // This is VERY IMPORTANT - otherwise each ResponseMessage (and LifecycleEvent) holds on to each dataset...
                                    responseMessage.setWrappedData(null);
                                    }
                                else
                                    {
                                    final String strResponseValue;

                                    // There's no Wrapped data, so do the best we can to come up with a ResponseValue and a new Wrapper
                                    if ((commandMessage.getCommandType() != null)
                                        && (commandMessage.getCommandType().getResponse() != null))
                                        {
                                        strResponseValue = commandMessage.getCommandType().getResponse().getValue();
                                        }
                                    else
                                        {
                                        strResponseValue = EMPTY_STRING;
                                        }

                                    // ResponseMessage WrappedData was null, so make a new one for the DAO
                                    LOGGER.debug(boolDebug,
                                                 SOURCE + "? SET WRAPPED ON DAO ResponseMessage WrappedData was null +++++++++++++++++++++++++++++++++++");
                                    LOGGER.debug(boolDebug,
                                                 SOURCE + "? DAO is " + dao.getClass().getName());
                                    dao.setWrappedData(new DAOWrapper(commandMessage,
                                                                      responseMessage,
                                                                      strResponseValue,
                                                                      dao));
                                    }

                                //-----------------------------------------------------------------
                                // Finally add the latest Parameter values to the DAO Wrapped InstrumentMetadata
                                // to record how the Command was invoked
                                // We can safely assume that the CommandType is not null,
                                // and so the ParameterList can be iterated

                                MetadataHelper.addParameterValuesToMetadata(dao.getWrappedData().getInstrumentMetadata(),
                                                                            obsinstrument,
                                                                            instrumentxml,
                                                                            module,
                                                                            command);

                                //-----------------------------------------------------------------
                                // Now pass the DAO data to the host Instrument
                                // This is responsible for updating Instrument panels and EventLogs, for instance

                                // Only refresh the data if visible
                                LOGGER.debug(boolDebug,
                                             SOURCE + "* SET WRAPPED ON INSTRUMENT +++++++++++++++++++++++++++++++++++");
                                LOGGER.debug(boolDebug,
                                             SOURCE + "* DAO is " + dao.getClass().getName());
                                LOGGER.debug(boolDebug,
                                             SOURCE + "* Wrapped DAO is " + dao.getWrappedData().getWrappedDAO().getClass().getName());
                                obsinstrument.setWrappedData(dao.getWrappedData(), false, true);

                                // Check that both the Command and Response made it through the DAO...
                                if (dao.getWrappedData() != null)
                                    {
                                    DAOHelper.addErrorIfAnyNull(dao.getWrappedData().getCommandMessage(),
                                                                dao.getWrappedData().getResponseMessage(),
                                                                errors);
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Response valid, but DAO Wrapper is unexpectedly NULL");
                                    }
                                }
                            else
                                {
                                LOGGER.debug(boolDebug,
                                             SOURCE + "No Response was returned (NULL)");

                                // No data were returned in the ResponseMessage
                                //LOGGER.error(SOURCE + "clearData() --> No data were returned in the ResponseMessage");
                                // This takes account of isInstrumentDataConsumer()
                                dao.clearData();
                                dao.clearMetadata();
                                LOGGER.debug(boolDebug,
                                             SOURCE + "! SET WRAPPED ON DAO NULL RESPONSEMESSAGE +++++++++++++++++++++++++++++++++++");
                                LOGGER.debug(boolDebug,
                                             SOURCE + "! DAO is " + dao.getClass().getName());
                                dao.setWrappedData(new DAOWrapper(null, null, EMPTY_STRING, dao));

                                // Now pass the DAO Wrapper to the host Instrument
                                // This is responsible for updating Instrument panels and EventLogs, for instance
                                // Only refresh the data if visible
                                LOGGER.debug(boolDebug,
                                             SOURCE + "+ SET WRAPPED ON INSTRUMENT NULL RESPONSEMESSAGE +++++++++++++++++++++++++++++++++++");
                                LOGGER.debug(boolDebug,
                                             SOURCE + "+ DAO is " + dao.getClass().getName());
                                LOGGER.debug(boolDebug,
                                             SOURCE + "+ Wrapped DAO is " + dao.getWrappedData().getWrappedDAO().getClass().getName());
                                obsinstrument.setWrappedData(dao.getWrappedData(), false, true);
                                }
                            }
                        else
                            {
                            LOGGER.debug(boolDebug,
                                         SOURCE + "No data were returned by the SwingWorker");

                            // No data were returned by the SwingWorker
                            // This takes account of isInstrumentDataConsumer()
                            dao.clearData();
                            dao.clearMetadata();
                            LOGGER.debug(boolDebug,
                                         SOURCE + ") SET WRAPPED ON DAO - NULL +++++++++++++++++++++++++++++++++++");
                            LOGGER.debug(boolDebug,
                                         SOURCE + ") DAO is " + dao.getClass().getName());
                            dao.setWrappedData(new DAOWrapper(null, null, EMPTY_STRING, dao));

                            // Now pass the *original* DAO data to the host Instrument
                            // This is responsible for updating Instrument panels and EventLogs, for instance
                            // Only refresh the data if visible
                            LOGGER.debug(boolDebug,
                                         SOURCE + "= SET WRAPPED ON INSTRUMENT - NULL +++++++++++++++++++++++++++++++++++");
                            LOGGER.debug(boolDebug,
                                         SOURCE + "= DAO is " + dao.getClass().getName());
                            LOGGER.debug(boolDebug,
                                         SOURCE + "= Wrapped DAO is " + dao.getWrappedData().getWrappedDAO().getClass().getName());
                            obsinstrument.setWrappedData(dao.getWrappedData(), false, true);

                            DAOHelper.addErrorIfAnyNull(null, null, errors);
                            }

                        //------------------------------------------------------------------------------
                        // Now tidy up the Instrument, DAO and Port states, depending on what happened
                        //
                        // Instrument --> obsinstrument.getInstrumentState()
                        // DAO        --> getResponseMessageStatusList()
                        // Port       --> showPortState(getPort())

//                        LOGGER.debug(boolDebug,
//                                               SOURCE + "finished() ORIGINAL [instrumentstate="
//                                                    + obsinstrument.getInstrumentState().getName()
//                                                    + "] [responsestatus=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
//                                                    + "] " + DAOHelper.showPortState(dao.getPort())
//                                                    + " [isrepeating=" + isrepeating + "]");

                        // See if we have been interrupted by something (ABORT, TIMEOUT, PREMATURE_TERMINATION)
                        if (ResponseMessageStatus.isToStopNow(dao.getResponseMessageStatusList()))
                            {
                            //LOGGER.debugProtocolEvent("ExecuteCommandHelper.executeCommand() DAO is to stop NOW");

                            // TODO WAS**** Tell the world that the Port is now available again...
                            if ((command.getSendToPort())
                                && (dao.getPort() != null))
                                {
                                // Free the Port
                                dao.getPort().setPortBusy(false);

                                // Clear the Queues and Streams, since we know that all activity is over for now
                                // This is to trap anomalies in Timeouts
                                dao.getPort().clearQueues();
                                dao.getPort().getTxStream().reset();
                                dao.getPort().getRxStream().reset();
                                }
                            else
                                {
                                LOGGER.debug(boolDebug,
                                                       SOURCE + "The Instrument DAO has no Port");
                                }

                            // Preserve any ABORT
                            if (!dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
                                {
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                }

                            // Check that we haven't been STOPPED before returning to READY
                            if (!InstrumentState.STOPPED.equals(obsinstrument.getInstrumentState()))
                                {
                                obsinstrument.notifyInstrumentStateChangedEvent(this,
                                                                                obsinstrument,
                                                                                obsinstrument.getInstrumentState(),
                                                                                InstrumentState.READY,
                                                                                0,
                                                                                UNEXPECTED);
                                }
                            }
                        else
                            {
                            // TODO WAS**** We can continue again, if required
                            // Tell the world that the Port is now available...
                            if ((command.getSendToPort())
                                && (dao.getPort() != null))
                                {
                                // If we don't need the Port again, make it available
                                // otherwise keep it BUSY
                                dao.getPort().setPortBusy(isrepeating);
                                //LOGGER.debugProtocolEvent("ExecuteCommandHelper.executeCommand() Change the DAO Port [busy=" + isrepeating + "]");

                                // Clear the Queues and Streams, since we know that all activity is over for now
                                // This is to trap anomalies in Timeouts
                                dao.getPort().clearQueues();
                                dao.getPort().getTxStream().reset();
                                dao.getPort().getRxStream().reset();
                                }
                            else
                                {
                                LOGGER.debug(boolDebug,
                                                       SOURCE + "No action taken with Port or Queues");
                                }

                            // Preserve any ABORT
                            if (!dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
                                {
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                }

                            // If we are not repeating, return to READY, otherwise stay in REPEATING
                            if (!isrepeating)
                                {
                                LOGGER.debug(boolDebug,
                                                       SOURCE + "Not repeating, setting READY");

                                // Check that we haven't been STOPPED before returning to READY
                                if (!InstrumentState.STOPPED.equals(obsinstrument.getInstrumentState()))
                                    {
                                    obsinstrument.notifyInstrumentStateChangedEvent(this,
                                                                                    obsinstrument,
                                                                                    obsinstrument.getInstrumentState(),
                                                                                    InstrumentState.READY,
                                                                                    0,
                                                                                    UNEXPECTED);
                                    }
                                }
                            }

                        // We really have finished!
                        dao.setExecutionStatus(ExecutionStatus.FINISHED);

                        // ToDo What happens to the returned error list?
                        LOGGER.debug(boolDebug,
                                     SOURCE + "--> finished() END [command=" + starscript
                                           + "] [execution.status=" + dao.getExecutionStatus().getName()
                                           + "] [responses.tatus=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                           + "] [instrument.state=" + obsinstrument.getInstrumentState().getName()
                                           + "] [isrepeating=" + isrepeating
                                           + "] " + DAOHelper.showPortState(dao.getPort())
                                           + " [thread.group=" + REGISTRY.getThreadGroup().getName()
                                           + "] [thread.name=" + Thread.currentThread().getName()
                                           + "]");
                        LOGGER.debug(boolDebug,
                                               Logger.CONSOLE_SEPARATOR_MINOR);
                        }
                    });

                // Start the Thread we have prepared...
                dao.getExecuteWorker().start();
                boolSuccess = true;
                }
            else
                {
                LOGGER.error(SOURCE + "The Command is not valid, and cannot be executed");
                }
            }

        // Return false if we couldn't even try to execute it
        return (boolSuccess);
        }


    /***********************************************************************************************
     * Execute the Command by sending it to the Port.
     * Return NULL on TIMEOUT.
     *
     * @param commandmessage
     * @param dao
     * @param obsinstrument
     * @param instrumentxml
     * @param module
     * @param command
     * @param errors
     * @param notifymonitors
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    private static ResponseMessageInterface executeSendToPort(final CommandMessageInterface commandmessage,
                                                              final ObservatoryInstrumentDAOInterface dao,
                                                              final ObservatoryInstrumentInterface obsinstrument,
                                                              final Instrument instrumentxml,
                                                              final XmlObject module,
                                                              final CommandType command,
                                                              final List<String> errors,
                                                              final boolean notifymonitors,
                                                              final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.executeSendToPort() ";
        final ResponseMessageInterface responseMessage;

        // SendToPort Commands require a Port
        if (dao.getPort() != null)
            {
            // Send the timestamped Command to the PortController queue
            // The PortController issues a notifyPortMessageEvent() when the message is sent to the Port TxQueue
            // NOTE - the timeout (wait for space in Tx queue) is not used in this implementation!
            dao.getPort().queueCommandMessage(commandmessage,
                                              dao.getTimeoutMillis(module, command),
                                              dao.getObservatoryClock(),
                                              notifymonitors,
                                              debug);

            // Check to see if this Command is expected to return a Response or an Ack
            // If so, we must dequeue it...
            if ((command.getResponse() != null)
                || (command.getAck() != null))
                {
                // Return a timestamped ResponseMessage when complete
                // The PortController issues a notifyPortMessageEvent() when the message is dequeued from the Port RxQueue
                // This may come back NULL if it times out waiting for the RxQueue
                // NOTE - the timeout (wait for response) is used in a custom wait loop in dequeueResponseMessage()
                responseMessage = dao.getPort().dequeueResponseMessage(dao,
                                                                       dao.getTimeoutMillis(module, command),
                                                                       notifymonitors,
                                                                       debug);
                if ((responseMessage != null)
                    && (responseMessage.getCommandType() != null))
                    {
                    // Debug only the ResponseValue
                    if (responseMessage.getCommandType().getResponse() != null)
                        {
                        LOGGER.debug(debug,
                                               SOURCE + "SendToPort after dequeue [ResponseValue=" + responseMessage.getCommandType().getResponse().getValue() + "]");
                        }
                    else if (responseMessage.getCommandType().getAck() != null)
                        {
                        LOGGER.debug(debug,
                                               SOURCE + "SendToPort after dequeue [Ack]");
                        }
                    else
                        {
                        LOGGER.debug(debug,
                                               SOURCE + "SendToPort after dequeue [No Response or Ack]");
                        }

                    // ToDo REVIEW!
                    // I *think* we can just assign this DAO to the ResponseMessage from the StaribusPort,
                    // since only one CommandMessage can be processed at a time
                    if (dao.getPort().isStaribusPort())
                        {
                        LOGGER.debug(debug,
                                               SOURCE + "Assigning Port Name to ResponseMessage from the StaribusPort");
                        responseMessage.setPortName(dao.getPort().getName());
                        }
                    }
                else
                    {
                    // Preserve any ABORT
                    // This is required because it is the Queue timer (wait loop) which times out,
                    // not the main Timeout Timer in the DAO
                    if (!dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
                        {
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.TIMEOUT);
                        }
                    }
                }
            else
                {
                // The Command does not produce a Response or an Ack
                // Ensure that the 'fire and forget' Command takes a finite time to return,
                // to avoid clearing the TxQueue before the Command bytes are sent!
                Utilities.safeSleep(PortController.getInstance().getLatencyMillis() << 2);

                // No Response is expected, so make a valid timestamped empty ResponseMessage
                // Always return SUCCESS, since we don't know otherwise...
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                               commandmessage,
                                                                               instrumentxml,
                                                                               module,
                                                                               command,
                                                                               AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                                module,
                                                                                                                                command));
                }

            // Check to see if we executed a reset() which was a SendToPort
            // If so, we must also reset the DAO containers etc.
            if ((responseMessage != null)
                && (ObservatoryInstrumentDAOInterface.COMMAND_RESET.equals(command.getIdentifier())))
                {
                LOGGER.debug(debug,
                                       "Execute Command, extractResetMode()");

                obsinstrument.reset(DAOHelper.extractResetMode(command));
                }

            // ResponseMessage is NULL here on TIMEOUT
            }
        else
            {
            // There is no Port, but it is a SendToPort Command, so return INVALID_COMMAND
            LOGGER.error(SOURCE + "A SendToPort Command has no Port");

            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           instrumentxml,
                                                                           module,
                                                                           command,
                                                                           AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                            module,
                                                                                                                            command));
            }

        // ResponseMessage is NULL here on TIMEOUT

        return (responseMessage);
        }


    /***********************************************************************************************
     * Execute a Command locally in the DAO.
     * Return ResponseMessage NULL on TIMEOUT.
     *
     * @param commandmessage
     * @param obsinstrument
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param notifymonitors
     * @param errors
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    private static ResponseMessageInterface executeLocal(final CommandMessageInterface commandmessage,
                                                         final ObservatoryInstrumentInterface obsinstrument,
                                                         final ObservatoryInstrumentDAOInterface dao,
                                                         final Instrument instrumentxml,
                                                         final XmlObject module,
                                                         final CommandType command,
                                                         final boolean notifymonitors,
                                                         final List<String> errors,
                                                         final boolean debug)
        {
        final ResponseMessageInterface responseMessage;

        // No Port is required, there is no Queue timer
        // Use the timeout in the Command, or the DAO default
        TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(module, command));

        // Issue begin and end notifyPortMessageEvent() inside invokeLocalCommand()
        // Local Commands must always come back non-null unless they time out or abort
        // They are responsible for creating their own empty (but valid) SUCCESS Responses
        // Return when complete, or after a timeout
        responseMessage = invokeLocalCommand(commandmessage,
                                             dao,
                                             notifymonitors,
                                             errors,
                                             debug);

        // Return when complete, or after a timeout
        // Stop the Timer regardless
        TimeoutHelper.stopDAOTimeoutTimer(dao);

        return (responseMessage);
        }


    /***********************************************************************************************
     * Invoke the supplied DAO command method locally by reflection, if possible.
     * The CommandMessage is assumed to be valid.
     * If it didn't work, return a ResponseMessage with an appropriate status code.
     * Timeouts are handled outside this method.
     *
     * @param commandmessage
     * @param dao
     * @param notifymonitors
     * @param errors
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    private static ResponseMessageInterface invokeLocalCommand(final CommandMessageInterface commandmessage,
                                                               final ObservatoryInstrumentDAOInterface dao,
                                                               final boolean notifymonitors,
                                                               final List<String> errors,
                                                               final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.invokeLocalCommand() ";
        ResponseMessageInterface responseMessage;

        responseMessage = null;

        if ((dao != null)
            && (commandmessage != null)
            && (commandmessage.getInstrument() != null)
            && (commandmessage.getModule() != null)
            && (commandmessage.getCommandType() != null))
            {
            final String strIdentifier;
            ResponseMessageStatus responseMessageStatus;

            strIdentifier = commandmessage.getCommandType().getIdentifier();
            responseMessageStatus = null;

            try
                {
                final Method method;
                final Object objResponse;
                final Class[] parameterTypes =
                    {
                    CommandMessageInterface.class
                    };

                // NOTE getDeclaredMethod() does not find inherited methods!
                method = dao.getClass().getMethod(strIdentifier, parameterTypes);

                // The Command Identifier is not optional, so must be correct, and hence not null
                if (notifymonitors)
                    {
                    PORT_CONTROLLER.notifyPortMessageEvent(dao,
                                                           commandmessage,
                                                           null);
                    }

                // This is equivalent to putting the CommandMessage in the TxQueue for sending to the real Port
                // This method call may take a long time to execute...
                LOGGER.debug(debug,
                                       SOURCE + "Go to invoking LOCAL method [name=" + method.getName()
                                           + "] [identifier="
                                           + strIdentifier
                                           + "] [thread.group="
                                           + REGISTRY.getThreadGroup().getName()
                                           + "] [thread.name=" + Thread.currentThread().getName() + "]");

                objResponse = method.invoke(dao, commandmessage);

                LOGGER.debug(debug,
                                       SOURCE + "Return from invoking LOCAL method [name=" + method.getName()
                                           + "] [thread.group="
                                           + REGISTRY.getThreadGroup().getName()
                                           + "] [thread.name=" + Thread.currentThread().getName() + "]");

                if ((objResponse != null)
                    && (objResponse instanceof ResponseMessageInterface))
                    {
                    responseMessage = (ResponseMessageInterface)objResponse;

                    // Create another PortMessageEvent for the ResponseMessage after Command completion
                    responseMessage = ObservatoryInstrumentHelper.timestampResponseMessage(responseMessage,
                                                                                           dao.getObservatoryClock());
                    if (notifymonitors)
                        {
                        PORT_CONTROLLER.notifyPortMessageEvent(dao,
                                                               null,
                                                               responseMessage);
                        }
                    }
                else
                    {
                    // The ResponseMessage was null, probably a timeout
                    LOGGER.error(SOURCE + "The requested command failed to execute [name=" + strIdentifier + "]");
                    errors.add(ObservatoryInstrumentInterface.COMMAND_FAILED);
                    }
                }

            catch (final NoSuchMethodException exception)
                {
                LOGGER.error(SOURCE + "The requested command is not available in this DAO [method=" + strIdentifier + "] [dao=" + dao.getClass() + "] [exception=" + exception.getMessage() + "]");
                exception.printStackTrace();
                errors.add(ObservatoryInstrumentInterface.COMMAND_NOT_AVAILABLE);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            catch (final SecurityException exception)
                {
                LOGGER.error(SOURCE + "The requested command has been denied access [name=" + strIdentifier + "]");
                errors.add(ObservatoryInstrumentInterface.COMMAND_DENIED_ACCESS);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            catch (final IllegalAccessException exception)
                {
                LOGGER.error(SOURCE + "The requested command is not accessible in this DAO [name=" + strIdentifier + "]");
                errors.add(ObservatoryInstrumentInterface.COMMAND_NOT_ACCESSIBLE);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            catch (final IllegalArgumentException exception)
                {
                LOGGER.error(SOURCE + "The requested command has not been correctly specified [name=" + strIdentifier + "]");
                errors.add(ObservatoryInstrumentInterface.COMMAND_ILLEGAL_ARGUMENT);
                responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                }

            catch (final IndexOutOfBoundsException exception)
                {
                LOGGER.error(SOURCE + "The requested command has an incorrect index [name=" + strIdentifier + "]");
                errors.add(ObservatoryInstrumentInterface.COMMAND_INDEX);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            catch (final InvocationTargetException exception)
                {
                LOGGER.error(SOURCE + "The requested command has thrown an unexpected Exception (Invocation Target) [name=" + strIdentifier + "]");
                exception.printStackTrace();
                errors.add(ObservatoryInstrumentInterface.COMMAND_INVOCATION_TARGET);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            catch (final ExceptionInInitializerError exception)
                {
                LOGGER.error(SOURCE + "The requested command failed to initialise correctly [name=" + strIdentifier + "]");
                errors.add(ObservatoryInstrumentInterface.COMMAND_INITIALISER);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            catch (final Exception exception)
                {
                LOGGER.error(SOURCE + "Generic Exception [name=" + strIdentifier + "]");
                errors.add(ObservatoryInstrumentInterface.COMMAND_FAILED);
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }

            // Did anything go wrong? (other than Timeout or Abort)
            // Otherwise return the ResponseMessage from method.invoke()
            if (responseMessageStatus != null)
                {
                dao.getResponseMessageStatusList().add(responseMessageStatus);
                responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                               commandmessage,
                                                                               commandmessage.getInstrument(),
                                                                               commandmessage.getModule(),
                                                                               commandmessage.getCommandType(),
                                                                               AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                                commandmessage.getModule(),
                                                                                                                                commandmessage.getCommandType()));
                }
            }

        return (responseMessage);
        }


    /**********************************************************************************************/
    /* Execution on the Same Thread                                                               */
    /***********************************************************************************************
     * Execute a Starscript Command (assumed to be valid),
     * returning the ResponseMessage directly on the same Thread as the caller.
     * Always return with a non-NULL DAOWrapper in the ResponseMessage.
     * The ObservationMetadata in the Wrapper are updated with the Parameter Values
     * used to invoke the Command.
     * Optionally notify the DAO Port and any PortMonitors etc.
     *
     * WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
     *
     * Used for nested Commands, where the outer Command is run with executeCommand().
     * i.e. executeCommand() calling executeCommandOnSameThread(),
     * or called by a Timer Runnable.
     *
     * Valid:
     *      SendToPort                (Wait loop in Rx queue)
     *      Local                     (Real DAO Timer)
     *      Local calling Local
     *      Local calling SendToPort  (Real DAO Timer and Wait loop in Rx queue)
     *
     * Invalid:
     *      SendToPort calling Local
     *      SendToPort calling SendToPort
     *
     * Currently used in the following, which are all {Local calling SendToPort}:
     *      GetConfiguration.doGetConfiguration()
     *      GetModuleConfiguration.doGetModuleConfiguration()
     *      SetModuleConfiguration.doSetModuleConfiguration()
     *      CaptureCommandHelper.doIteratedDataCaptureCommand()
     *      CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand()
     *      DAOCommandHelper.executeSteppedCommands()
     *      StaribusLoggerDAO.getData()
     *      SignalProcessor.measureFrequencyResponse()
     *
     * @param obsinstrument
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param executionparameters
     * @param starscript
     * @param errors
     * @param notifyport
     * @param notifymonitors
     *
     * @return ResponseMessageInterface
     */

    public static synchronized ResponseMessageInterface executeCommandOnSameThread(final ObservatoryInstrumentInterface obsinstrument,
                                                                                   final ObservatoryInstrumentDAOInterface dao,
                                                                                   final Instrument instrumentxml,
                                                                                   final XmlObject module,
                                                                                   final CommandType command,
                                                                                   final List<ParameterType> executionparameters,
                                                                                   final String starscript,
                                                                                   final List<String> errors,
                                                                                   final boolean notifyport,
                                                                                   final boolean notifymonitors)
        {
        final String SOURCE = "ExecuteCommandHelper.executeCommandOnSameThread() ";
        ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        responseMessage = null;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                    || LOADER_PROPERTIES.isStaribusDebug()
                    || LOADER_PROPERTIES.isStarinetDebug()
                    || LOADER_PROPERTIES.isThreadsDebug());

        LOGGER.debug(boolDebug,
                               SOURCE + "START [command=" + starscript
                                    + "] [instrumentstate=" + obsinstrument.getInstrumentState().getName()
                                    + "] [responsestatus=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                    + "] " + DAOHelper.showPortState(dao.getPort())
                                    + " [thread.name=" + Thread.currentThread().getName() + "]");

        // Send the CommandMessage if we are able to
        if (DAOHelper.isCommandValid(obsinstrument,
                                     instrumentxml,
                                     module,
                                     command,
                                     starscript,
                                     dao.getPort()))
            {
            final CommandLifecycleEventInterface lifecycleEvent;
            final CommandMessageInterface commandmessage;

            // Pick up what we would do in the SwingWorker of executeCommand()
            // but don't change any logs, queues or streams!

            // Prepare an un-timestamped CommandMessage
            // ready to be sent to the Port, or to be invoked locally
            commandmessage = dao.constructCommandMessage(dao,
                                                         instrumentxml,
                                                         module,
                                                         command,
                                                         starscript.trim());
            commandmessage.setExecutionParameters(executionparameters);

            // We should be Busy or Repeating already, but do it again...
            if (notifyport)
                {
                LOGGER.debug(boolDebug,
                                       SOURCE + "notifyInstrumentStateChangedEvent()");
                obsinstrument.notifyInstrumentStateChangedEvent(dao,
                                                                obsinstrument,
                                                                obsinstrument.getInstrumentState(),
                                                                InstrumentState.BUSY,
                                                                0,
                                                                InstrumentState.BUSY.getStatus());  // ToDo Not sure what this text should be
                }

            // todo WAS***** The Port is BUSY regardless of how we are executing the Command
            if ((command.getSendToPort())
                && (dao.getPort() != null))
                {
                LOGGER.debug(boolDebug,
                                       SOURCE + "setPortBusy(true)");
                dao.getPort().setPortBusy(true);
                }

            // Let the World know we are about to execute this Command
            ObservatoryInstrumentHelper.timestampCommandMessage(commandmessage,
                                                                dao.getObservatoryClock());

            // Issue the notifyCommandLifecycleEvent() for the timestamped Command,
            // but only if we are being monitored (the low-level detail may not be needed)
            LOGGER.debug(boolDebug,
                                   SOURCE + "beginCommandLifecycle()");
            lifecycleEvent = beginCommandLifecycle(dao,
                                                   commandmessage,
                                                   notifymonitors);

            //--------------------------------------------------------------------------------------
            // Ready to execute as SendToPort or Local
            // Each Command has zero or one Response (e.g. a data packet)
            // NOTE: A NULL ResponseMessage ALWAYS means a Timeout or Abort has occurred

            try
                {
                if (command.getSendToPort())
                    {
                    LOGGER.debug(boolDebug,
                                           SOURCE + "executeSameThreadSendToPort()");
                    responseMessage = executeSameThreadSendToPort(dao,
                                                                  commandmessage,
                                                                  instrumentxml,
                                                                  module,
                                                                  command,
                                                                  starscript,
                                                                  notifymonitors,
                                                                  errors,
                                                                  SOURCE,
                                                                  LOADER_PROPERTIES.isThreadsDebug());
                    }
                else
                    {
                    LOGGER.debug(boolDebug,
                                           SOURCE + "executeSameThreadLocal()");
                    responseMessage = executeSameThreadLocal(commandmessage,
                                                             dao,
                                                             instrumentxml,
                                                             module,
                                                             command,
                                                             starscript,
                                                             notifymonitors,
                                                             errors,
                                                             SOURCE,
                                                             LOADER_PROPERTIES.isThreadsDebug());
                    }
                }

            catch (final Exception exception)
                {
                LOGGER.debug(boolDebug,
                                       SOURCE + "GENERIC EXCEPTION " + exception.getMessage());
                exception.printStackTrace();
                responseMessage = null;
                }

            //--------------------------------------------------------------------------------------
            // If the ResponseMessage was (still) null, then there was a timeout or an Abort or InterruptedException
            // Obtain the ResponseStatus from the DAO

            if (responseMessage == null)
                {
                responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                               commandmessage,
                                                                               instrumentxml,
                                                                               module,
                                                                               command,
                                                                               AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                                module,
                                                                                                                                command));
                }

            //--------------------------------------------------------------------------------------
            // We now know that ResponseMessage cannot be null

            // Make sure we always return with a non-null Wrapper
            if (responseMessage.getWrappedData() == null)
                {
                final String strResponseValue;

                // We need to wrap the ResponseValue too
                if ((commandmessage.getCommandType() != null)
                    && (commandmessage.getCommandType().getResponse() != null))
                    {
                    strResponseValue = commandmessage.getCommandType().getResponse().getValue();
                    }
                else
                    {
                    strResponseValue = EMPTY_STRING;
                    }

                LOGGER.debug(boolDebug,
                             SOURCE + "( SET WRAPPED ON RESPONSE +++++++++++++++++++++++++++++++++++");
                LOGGER.debug(boolDebug,
                             SOURCE + "( DAO is " + dao.getClass().getName());
                responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                              responseMessage,
                                                              strResponseValue,
                                                              dao));
                }

            //-------------------------------------------------------------------------------------
            // Something useful happened...

            // Finally add the latest Parameter values to the ResponseMessage Metadata
            // to record how the Command was invoked
            // We can safely assume that the CommandType is not null,
            // and so the ParameterList can be iterated
            MetadataHelper.addParameterValuesToMetadata(responseMessage.getWrappedData().getInstrumentMetadata(),
                                                        obsinstrument,
                                                        instrumentxml,
                                                        module,
                                                        command);

            // Complete the Command Lifecycle if we are being monitored
            LOGGER.debug(boolDebug,
                                   SOURCE + "completeCommandLifecycle()");
            completeCommandLifecycle(dao,
                                     lifecycleEvent,
                                     responseMessage,
                                     SOURCE,
                                     notifymonitors,
                                     boolDebug);

            // The above is all within construct() of the 'outer' SwingWorker in executeCommand()
            // or in a Timer Runnable,
            // so here we have to handle the resulting data differently...

            // TODO WAS**** Allow other activity again...
            if ((command.getSendToPort())
                && (dao.getPort() != null))
                {
                // The DAO may not have a Port
                LOGGER.debug(boolDebug,
                                       SOURCE + "setPortBusy(false)");
                dao.getPort().setPortBusy(false);
                }

            // Always return to READY, since this execute does not have a repeating function
            }
        else
            {
            // The supplied Command is invalid; this should never happen!
            LOGGER.error(SOURCE + "Requested Command is invalid [starscript=" + starscript + "]");
            }

        LOGGER.debug(boolDebug,
                               SOURCE + "END [command=" + starscript
                                    + "] [instrumentstate=" + obsinstrument.getInstrumentState().getName()
                                    + "] [responsestatus=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList())
                                    + "] " + DAOHelper.showPortState(dao.getPort())
                                    + " [thread.name=" + Thread.currentThread().getName() + "]");

        return (responseMessage);
        }


    /***********************************************************************************************
     * Execute the Command as a SendToPort on the Same Thread.
     * Do not affect Port or Queue status.
     * Return data in the ResponseMessage WrappedData.
     *
     * @param dao
     * @param commandmessage
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param notifymonitors
     * @param errors
     * @param SOURCE
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    private static ResponseMessageInterface executeSameThreadSendToPort(final ObservatoryInstrumentDAOInterface dao,
                                                                        final CommandMessageInterface commandmessage,
                                                                        final Instrument instrumentxml,
                                                                        final XmlObject module,
                                                                        final CommandType command,
                                                                        final String starscript,
                                                                        final boolean notifymonitors,
                                                                        final List<String> errors,
                                                                        final String SOURCE,
                                                                        final boolean debug)
        {
        ResponseMessageInterface responseMessage;

        // SendToPort Commands require a Port
        if (dao.getPort() != null)
            {
            LOGGER.debug(debug,
                         SOURCE + "Queueing nested Starscript command [" + starscript.trim()
                                + " [thread.name=" + Thread.currentThread().getName() + "]");

            // Send the timestamped Command to the PortController queue
            // The PortController issues a notifyPortMessageEvent() when the message is sent to the Port TxQueue
            // NOTE - the timeout (wait for space in Tx queue) is not used in this implementation!
            dao.getPort().queueCommandMessage(commandmessage,
                                              dao.getTimeoutMillis(module, command),
                                              dao.getObservatoryClock(),
                                              notifymonitors,
                                              debug);
            // TODO REVIEW DELAY!
            Utilities.safeSleep(10);

            LOGGER.debug(debug,
                         SOURCE + "Prepare to Dequeue Starscript command [" + starscript.trim()
                                + " [thread.name=" + Thread.currentThread().getName() + "]");

            // Check to see if this Command is expected to return a Response or an Ack
            // If so, we must dequeue it...
            if ((command.getResponse() != null)
                || (command.getAck() != null))
                {
                // Return a timestamped ResponseMessage when complete
                // The PortController issues a notifyPortMessageEvent() when the message is dequeued from the Port RxQueue
                // This may come back NULL if it times out waiting for the RxQueue
                // NOTE - the timeout (wait for response) is used in a custom wait loop in dequeueResponseMessage()
                // which does not interfere with the DAO's real Timer
                // The timeout refers to the Command actually executed, not any 'outer' Command
                responseMessage = dao.getPort().dequeueResponseMessage(dao,
                                                                       dao.getTimeoutMillis(module, command),
                                                                       notifymonitors,
                                                                       debug);
                // Did the ResponseMessage come back with valid data?
                if ((responseMessage != null)
                    && (responseMessage.getCommandType() != null))
                    {
                    if (responseMessage.getCommandType().getResponse() != null)
                        {
                        LOGGER.debug(debug,
                                     SOURCE + "SendToPort after dequeue [ResponseValue=" + responseMessage.getCommandType().getResponse().getValue() + "]");
                        LOGGER.debug(debug,
                                     SOURCE + "^ SET WRAPPED ON RESPONSE +++++++++++++++++++++++++++++++++++");
                        LOGGER.debug(debug,
                                     SOURCE + "^ DAO is " + dao.getClass().getName());
                        responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                                      responseMessage,
                                                                      responseMessage.getCommandType().getResponse().getValue(),
                                                                      dao));
                        }
                    else if (responseMessage.getCommandType().getAck() != null)
                        {
                        LOGGER.debug(debug,
                                     SOURCE + "SendToPort after dequeue [Ack]");
                        LOGGER.debug(debug,
                                     SOURCE + "% SET WRAPPED ON RESPONSE +++++++++++++++++++++++++++++++++++");
                        LOGGER.debug(debug,
                                     SOURCE + "% DAO is " + dao.getClass().getName());
                        responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                                      responseMessage,
                                                                      EMPTY_STRING,
                                                                      dao));
                        }
                    else
                        {
                        // This is a configuration error, so return INVALID_COMMAND
                        LOGGER.debug(debug,
                                     SOURCE + "SendToPort after dequeue [No Response or Ack], returning INVALID_COMMAND");
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
                        responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                                       commandmessage,
                                                                                       instrumentxml,
                                                                                       module,
                                                                                       command,
                                                                                       AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                                        module,
                                                                                                                                        command));
                        }

                    // ToDo REVIEW!
                    // I *think* we can just assign this DAO to the ResponseMessage from the StaribusPort,
                    // since only one CommandMessage can be processed at a time
                    if (dao.getPort().isStaribusPort())
                        {
                        LOGGER.debug(debug,
                                     SOURCE + "Assigning DAO to ResponseMessage from the StaribusPort");
                        responseMessage.setPortName(dao.getPort().getName());
                        }
                    }
                else
                    {
                    // There must have been a TIMEOUT
                    // It is the Queue wait loop which times out, not the DAO's real Timer
                    LOGGER.debug(debug,
                                 SOURCE + "There must have been a QUEUE TIMEOUT");
                    }
                }
            else
                {
                // No Response is expected, so make a valid timestamped empty ResponseMessage
                // Always return SUCCESS, since we don't know otherwise...
                // ToDo WARNING! This timestamp might be *earlier* than the Tx message!
                // ToDo because of the delays in the PortController
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                               commandmessage,
                                                                               instrumentxml,
                                                                               module,
                                                                               command,
                                                                               AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                                module,
                                                                                                                                command));
                LOGGER.debug(debug,
                             SOURCE + "No Response or Ack expected from Command, returning default SUCCESS");
                }

            // ResponseMessage is NULL here on TIMEOUT
            }
        else
            {
            // There is no Port, but it is a SendToPort Command, so return INVALID_COMMAND
            LOGGER.debug(debug,
                         SOURCE + "SendToPort Command has no Port, returning INVALID_COMMAND");

            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           instrumentxml,
                                                                           module,
                                                                           command,
                                                                           AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                            module,
                                                                                                                            command));
            }

        // ResponseMessage is NULL here on TIMEOUT
        return (responseMessage);
        }


    /***********************************************************************************************
     * Execute the Command as a Local on the Same Thread.
     * Do not affect Port or Queue status.
     * Return data in the ResponseMessage WrappedData.
     *
     * @param commandmessage
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param notifymonitors
     * @param errors
     * @param SOURCE
     * @param debug
     *
     * @return ResponseMessageInterface
     */

    private static ResponseMessageInterface executeSameThreadLocal(final CommandMessageInterface commandmessage,
                                                                   final ObservatoryInstrumentDAOInterface dao,
                                                                   final Instrument instrumentxml,
                                                                   final XmlObject module,
                                                                   final CommandType command,
                                                                   final String starscript,
                                                                   final boolean notifymonitors,
                                                                   final List<String> errors,
                                                                   final String SOURCE,
                                                                   final boolean debug)
        {
        ResponseMessageInterface responseMessage;

        LOGGER.debug(debug,
                     SOURCE + "Invoke LOCAL Starscript command [" + starscript.trim()
                            + " [thread.name=" + Thread.currentThread().getName() + "]");

        // No Port is required
        // Issue begin and end notifyPortMessageEvent() inside invokeLocalCommand(), if required
        // Local Commands must always come back non-null unless they time out or abort
        // They are responsible for creating their own empty (but valid) SUCCESS Responses
        // Return when complete, or after a timeout
        // LOCAL Commands nested within other Commands must not provide more Timeouts!
        // So call invokeLocalCommand() directly, not executeLocal()
        // invokeLocalCommand() will do the logging

        responseMessage = invokeLocalCommand(commandmessage,
                                             dao,
                                             notifymonitors,
                                             errors, debug);

        // Did the ResponseMessage come back with valid data?
        if ((responseMessage != null)
            && (responseMessage.getCommandType() != null))
            {
            if (responseMessage.getCommandType().getResponse() != null)
                {
                LOGGER.debug(debug,
                             SOURCE + "invokeLocalCommand() [ResponseValue=" + responseMessage.getCommandType().getResponse().getValue() + "]");
                LOGGER.debug(debug,
                             SOURCE + "@ SET WRAPPED ON RESPONSE +++++++++++++++++++++++++++++++++++");
                LOGGER.debug(debug,
                             SOURCE + "@ DAO is " + dao.getClass().getName());
                responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                              responseMessage,
                                                              responseMessage.getCommandType().getResponse().getValue(),
                                                              dao));
                }
            else if (responseMessage.getCommandType().getAck() != null)
                {
                LOGGER.debug(debug,
                                       SOURCE + "invokeLocalCommand() [Ack]");
                LOGGER.debug(debug,
                             SOURCE + ": SET WRAPPED ON RESPONSE +++++++++++++++++++++++++++++++++++");
                LOGGER.debug(debug,
                             SOURCE + ": DAO is " + dao.getClass().getName());
                responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                              responseMessage,
                                                              EMPTY_STRING,
                                                              dao));
                }
            else
                {
                // This is a configuration error, so return INVALID_COMMAND
                LOGGER.debug(debug,
                             SOURCE + "invokeLocalCommand() [No Response or Ack], returning INVALID_COMMAND");
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
                responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                               commandmessage,
                                                                               instrumentxml,
                                                                               module,
                                                                               command,
                                                                               AbstractResponseMessage.buildResponseResourceKey(instrumentxml,
                                                                                                                                module,
                                                                                                                                command));
                }

            // ToDo REVIEW!
            // I *think* we can just assign this DAO to the ResponseMessage from the StaribusPort,
            // since only one CommandMessage can be processed at a time
            if (dao.getPort().isStaribusPort())
                {
                LOGGER.debug(debug,
                             SOURCE + "invokeLocalCommand() assigning DAO to ResponseMessage from the StaribusPort");
                responseMessage.setPortName(dao.getPort().getName());
                }
            }

        return (responseMessage);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * See if the requested Command is reset(Staribus).
     * If so, try to reset the bus and return SUCCESS.
     * Return NULL for any other Command, to allow the caller to execute normally.
     *
     * @param obsinstrument
     * @param dao
     * @param command
     * @param debug
     *
     * @return boolean
     */

    private static ResponseMessageInterface checkResetStaribusCommand(final ObservatoryInstrumentInterface obsinstrument,
                                                                      final ObservatoryInstrumentDAOInterface dao,
                                                                      final CommandType command,
                                                                      final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.checkResetStaribusCommand() ";
        ResponseMessageInterface responseMessage;
        ResponseMessageStatus responseMessageStatus;

        responseMessage = null;

        // Check to see if we are asked for the special case of a STARIBUS reset()
        // reset(Staribus) will be tagged as SendToPort,
        // but we don't want to send this one outside...
        // ...but we can't use the normal Stream close()

        if ((obsinstrument != null)
            && (ObservatoryInstrumentDAOInterface.COMMAND_RESET.equals(command.getIdentifier()))
            && (ResetMode.STARIBUS.equals(DAOHelper.extractResetMode(command)))
            && (dao.getPort() != null)
            && (dao.getPort().isStaribusPort()))
            {
            try
                {
                LOGGER.debug(debug, SOURCE + "reset(Staribus)");

                // Close and re-open StaribusTxStream
                if (dao.getPort().getTxStream() != null)
                    {
                    // This will automatically remove the SerialPortEventListener
                    dao.getPort().getTxStream().initialise();

                    // We won't mark the Port as closed, since the flag is protected,
                    // and anyway we are going to open it up again...
                    dao.getPort().getTxStream().open();
                    dao.getPort().getTxStream().reset();
                    }

                // Close StaribusRxStream
                if (dao.getPort().getRxStream() != null)
                    {
                    // We won't mark the RxStream as closed, since the flag is protected,
                    // and anyway we are going to open it up again...
                    dao.getPort().getRxStream().initialise();
                    dao.getPort().getRxStream().open();
                    dao.getPort().getRxStream().reset();
                    }

                // Give the Instrument an opportunity to reset also
                obsinstrument.reset(ResetMode.STARIBUS);

                LOGGER.debug(debug, SOURCE + "reset(Staribus) completed");

                // If any of the above failed, there's not much we can do now...
                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                }

            catch (final IOException exception)
                {
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }

            // If we came here we must not try to execute any other command,
            // so tell the caller where we went...
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNullWithStatus(dao,
                                                                                            null,
                                                                                            command,
                                                                                            responseMessage,
                                                                                            responseMessageStatus);
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * Issue the notifyCommandLifecycleEvent() for the timestamped Command,
     * but only if we are being monitored (the low-level detail may not be needed).
     *
     * @param dao
     * @param commandmessage
     * @param notifymonitors
     *
     * @return CommandLifecycleEventInterface
     */

    private static CommandLifecycleEventInterface beginCommandLifecycle(final ObservatoryInstrumentDAOInterface dao,
                                                                        final CommandMessageInterface commandmessage,
                                                                        final boolean notifymonitors)
        {
        final CommandLifecycleEventInterface lifecycleEvent;

        if (notifymonitors)
            {
            lifecycleEvent = PORT_CONTROLLER.notifyCommandLifecycleEvent(dao, commandmessage, null);
            }
        else
            {
            lifecycleEvent = null;
            }

        return (lifecycleEvent);
        }


    /***********************************************************************************************
     * Complete the Command Lifecycle if we are being monitored.
     *
     * @param dao
     * @param lifecycleevent
     * @param responsemessage
     * @param SOURCE
     * @param notifymonitors
     * @param debug
     */

    private static void completeCommandLifecycle(final ObservatoryInstrumentDAOInterface dao,
                                                 final CommandLifecycleEventInterface lifecycleevent,
                                                 final ResponseMessageInterface responsemessage,
                                                 final String SOURCE,
                                                 final boolean notifymonitors,
                                                 final boolean debug)
        {
        if ((notifymonitors)
            && (lifecycleevent != null))
            {
            lifecycleevent.setResponseMessage(responsemessage);

            // Now issue the notifyCommandLifecycleEvent() for the updated Command & Response
            // This is the only call to notifyCommandLifecycleEvent()
            PORT_CONTROLLER.notifyCommandLifecycleEvent(dao, lifecycleevent);

            // This is just debug
            if (lifecycleevent.getResponseMessage().getCommandType().getResponse() != null)
                {
                LOGGER.debug(debug,
                                       SOURCE + "[ResponseValue=" + lifecycleevent.getResponseMessage().getCommandType().getResponse().getValue() + "]");
                }
            else if (lifecycleevent.getResponseMessage().getCommandType().getAck() != null)
                {
                LOGGER.debug(debug,
                                       SOURCE + "[Ack]");
                }
            else
                {
                LOGGER.debug(debug,
                                       SOURCE + "[No Response or Ack]");
                }
            }
        }


    /***********************************************************************************************
     * Render the Parameters for the specified Instrument.Module.Command.
     * Return a UIComponent, or NULL on failure.
     *
     * @param obsinstrument
     * @param executioncontext
     * @param instrumentid
     * @param moduleid
     * @param commandid
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param errors
     * @param debug
     *
     * @return UIComponentPlugin
     */

    public static UIComponentPlugin renderCommandParameters(final ObservatoryInstrumentInterface obsinstrument,
                                                            final ExecutionContextInterface executioncontext,
                                                            final String instrumentid,
                                                            final String moduleid,
                                                            final String commandid,
                                                            final FontInterface fontdata,
                                                            final ColourInterface colourforeground,
                                                            final ColourInterface colourbackground,
                                                            final List<String> errors,
                                                            final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.executeStarscriptCommand() ";
        final Instrument instrument;
        UIComponentPlugin panelParameters;

        // Prepare to fail
        executioncontext.resetCommandContext();
        panelParameters = null;

        // Now try to find each part of the command
        instrument = XpathHelper.findInstrumentFromIdentifier(obsinstrument.getHostUI().getInstrumentsDoc(),
                                                              instrumentid,
                                                              debug);
        if ((instrument != null)
            && (instrument.getController() != null))
            {
            final XmlObject module;

            module = XpathHelper.findModuleFromIdentifier(instrument,
                                                          moduleid,
                                                          debug);
            if (module != null)
                {
                final CommandType cmdImport;

                cmdImport = XpathHelper.findCommandFromIdentifier(instrument,
                                                                  module,
                                                                  commandid,
                                                                  debug);
                if (cmdImport != null)
                    {
                    // We should have everything we need now, except the Execution DAO...
                    executioncontext.setObservatoryInstrument(obsinstrument);
                    executioncontext.setStarscriptInstrument(instrument);
                    executioncontext.setStarscriptModule(module);
                    executioncontext.setStarscriptMacro(null);
                    executioncontext.setStarscriptCommand(cmdImport);
                    // Selected Command, not Macro
                    executioncontext.setSelectedMacroOrCommand(true);

                    // Build the panel for Parameter entry, and leave it to the caller to display
                    panelParameters = new UIComponent();
                    panelParameters.removeAll();

                    panelParameters.add(SimplifiedParameterHelper.createParametersPanel(executioncontext,
                                                                                        fontdata,
                                                                                        colourforeground,
                                                                                        colourbackground,
                                                                                        false),             // No border
                                        BorderLayout.CENTER);
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Unable to locate Command [command.id=" + commandid + "]");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "Unable to locate Module [module.id=" + moduleid + "]");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to locate Instrument [instrument.id=" + instrumentid + "]");
            }

        return (panelParameters);
        }


    /***********************************************************************************************
     * Handle errors arising from an ExecutionContext.
     *
     * @param context
     * @param errors
     * @param title
     */

    public static void handleErrors(final ExecutionContextInterface context,
                                    final List<String> errors,
                                    final String title)
        {
        final String SOURCE = "SuperposedDataAnalyserHelper.handleErrors() ";

        if ((context != null)
            && (context.getStarscriptInstrument() != null)
            && (errors != null)
            && (!errors.isEmpty()))
            {
            JOptionPane.showMessageDialog(null,
                                          errors.toArray(),
                                          context.getStarscriptInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else if ((context != null)
                 && (context.getObservatoryInstrument() != null)
                 && (context.getExecutionDAO() == null)
                 && (context.getStarscriptInstrument() != null))
            {
            JOptionPane.showMessageDialog(null,
                                          ObservatoryInstrumentInterface.MSG_NO_CONNECTION,
                                          context.getStarscriptInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else if ((context != null)
                 && (context.getObservatoryInstrument() != null)
                 && (context.getStarscriptInstrument() != null)
                 && ((InstrumentState.isOccupied(context.getObservatoryInstrument()))))
            {
            JOptionPane.showMessageDialog(null,
                                          ObservatoryInstrumentInterface.TOOLTIP_BUSY,
                                          context.getStarscriptInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else if ((context != null)
                 && (context.getObservatoryInstrument() != null)
                 && (context.getStarscriptInstrument() != null)
                 && ((!InstrumentState.isDoingSomething(context.getObservatoryInstrument()))))
            {
            // This should never be possible...
            JOptionPane.showMessageDialog(null,
                                          ObservatoryInstrumentInterface.TOOLTIP_STOPPED,
                                          context.getStarscriptInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else
            {
            throw new FrameworkException(SOURCE + EXCEPTION_PARAMETER_INVALID);
            }
        }
    }

