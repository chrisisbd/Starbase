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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static org.lmn.fc.model.logging.EventStatus.INFO;


/***************************************************************************************************
 * Capture.
 */

public final class Capture implements FrameworkConstants,
                                      FrameworkStrings,
                                      FrameworkMetadata,
                                      FrameworkSingletons,
                                      FrameworkXpath,
                                      ObservatoryConstants
    {
    /***********************************************************************************************
     * doCaptureToHostMemory().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doCaptureToHostMemory(final StaribusCoreHostMemoryInterface dao,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "Capture.doCaptureToHostMemory() ";
        final String CAPTURE_COMMAND = "getRealtimeData";
        final ResponseMessageInterface responseMessage;
        final boolean boolPreviousModeWasCapture;
        final boolean boolModeChangedOk;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                        || LOADER_PROPERTIES.isStaribusDebug()
                        || LOADER_PROPERTIES.isStarinetDebug());

        boolPreviousModeWasCapture = dao.isCaptureMode();

        // Process the Command Parameters to get a new requested capture mode
        responseMessage = doCaptureMode(dao, commandmessage);

        boolModeChangedOk = ((responseMessage != null)
                             && (responseMessage.getResponseMessageStatusList() != null)
                             && (responseMessage.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS)));

        // Detect the start of a new Capture cycle,
        // or stop an existing cycle,
        // otherwise leave the Timer alone
        if ((boolModeChangedOk)
            && (!boolPreviousModeWasCapture)
            && (dao.isCaptureMode()))
            {
            final boolean boolFileSuccess;

            // Stop any existing Timer
            if (dao.getCaptureTimer() != null)
                {
                dao.getCaptureTimer().stop();
                dao.setCaptureTimer(null);
                }

            // We always create a new file for each capture cycle
            boolFileSuccess = createDAOHostMemoryFile(dao.getHostInstrument(),
                                                      dao,
                                                      StaribusCoreHostMemoryInterface.TIMESTAMPED_HOSTMEMORYFILE,
                                                      StaribusCoreHostMemoryInterface.MAX_FILE_SIZE_BYTES);
            if (boolFileSuccess)
                {
                final StringBuffer exprXpath;
                XmlObject[] selection;

                exprXpath = new StringBuffer();

                // Get the Command with the given Name
                exprXpath.setLength(0);
                exprXpath.append(XPATH_INSTRUMENTS_NAMESPACE);
                exprXpath.append(XPATH_PLUGIN_COMMAND_FROM_COMMAND_IDENTIFIER);
                exprXpath.append(CAPTURE_COMMAND);
                exprXpath.append(XPATH_QUOTE_TERMINATOR);

                // Query from the root of the Controller, since the Command could be in any Plugin
                selection = dao.getHostInstrument().getInstrument().getController().selectPath(exprXpath.toString());

                // Did we find at least one Command with the required name?
                // If so, take the first
                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length >= 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    final CommandType cmdGetData;

                    cmdGetData = (CommandType)selection[0].copy();

                    // Get the Plugin which contains the Command
                    exprXpath.setLength(0);
                    exprXpath.append(XPATH_INSTRUMENTS_NAMESPACE);
                    exprXpath.append(XPATH_PLUGIN_FROM_COMMAND_IDENTIFIER);
                    exprXpath.append(CAPTURE_COMMAND);
                    exprXpath.append(XPATH_QUOTE_TERMINATOR);
                    exprXpath.append(XPATH_TERMINATOR);

                    // Query from the root of the Controller, since the Command could be in any Plugin
                    selection = dao.getHostInstrument().getInstrument().getController().selectPath(exprXpath.toString());

                    // Did we find at least one parent Plugin?
                    // If so, take the first (how else to choose?)
                    if ((selection != null)
                        && (selection instanceof PluginType[])
                        && (selection.length >= 1)
                        && (selection[0] != null)
                        && (selection[0] instanceof PluginType))
                        {
                        final PluginType pluginModule;

                        pluginModule = (PluginType)selection[0].copy();

                        // Set up a new Timer to do the Capture
                        dao.setCaptureTimer(new Timer(dao.getSampleRate() * (int) ChronosHelper.SECOND_MILLISECONDS,
                                                      new ActionListener()
                                                      {
                                                      public void actionPerformed(final ActionEvent event)
                                                          {
                                                          final boolean boolDebugDynamic;

                                                          boolDebugDynamic = (LOADER_PROPERTIES.isTimingDebug()
                                                                       || LOADER_PROPERTIES.isStaribusDebug()
                                                                       || LOADER_PROPERTIES.isStarinetDebug());

                                                          LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                                                                 SOURCE + "Capture Timer TICK! "
                                                                                     + "[thread.group="
                                                                                     + REGISTRY.getThreadGroup().getName()
                                                                                     + "] [thread.name=" + Thread.currentThread().getName() + "]");

                                                          // Has the Instrument been stopped?
                                                          if ((dao.getCaptureTimer() != null)
                                                              && (InstrumentState.isDoingSomething(dao.getHostInstrument())))
                                                              {
                                                              final boolean boolCaptured;

                                                              LOGGER.debugTimedEvent(boolDebugDynamic,
                                                                                     Logger.CONSOLE_SEPARATOR_MAJOR);
                                                              LOGGER.debugTimedEvent(boolDebugDynamic,
                                                                                     SOURCE + " TICK! Execute "
                                                                                         + "[instrument=" + dao.getHostInstrument().getInstrument().getIdentifier()
                                                                                         + "] [plugin=" + pluginModule.getIdentifier()
                                                                                         + "] [command=" + cmdGetData.getIdentifier()
                                                                                         + "] [ccb=" + pluginModule.getCommandCodeBase()
                                                                                         + "] [cc=" + cmdGetData.getCommandCode()
                                                                                         + "] [ccv=" + cmdGetData.getCommandVariant()
                                                                                         + "] [rate=" + (dao.getCaptureTimer().getDelay() / ChronosHelper.SECOND_MILLISECONDS)
                                                                                         + "sec] [thread.name=" + Thread.currentThread().getName() + "]");

                                                              boolCaptured = captureDataRecord(dao.getHostInstrument(),
                                                                                               dao,
                                                                                               dao.getHostInstrument().getInstrument(),
                                                                                               pluginModule,
                                                                                               cmdGetData,
                                                                                               dao.getRandomAccessFile(),
                                                                                               StaribusCoreHostMemoryInterface.MAX_FILE_SIZE_BYTES,
                                                                                               StaribusCoreHostMemoryInterface.MAX_FILE_RECORD_COUNT,
                                                                                               StaribusCoreHostMemoryInterface.RECORD_SIZE_BYTES_CAPTURED,
                                                                                               StaribusCoreHostMemoryInterface.REALTIMEDATA_SIZE,
                                                                                               false,
                                                                                               boolDebugDynamic);
                                                              if (!boolCaptured)
                                                                  {
                                                                  LOGGER.debugTimedEvent(boolDebugDynamic,
                                                                                         SOURCE + "Data capture failed");
                                                                  }
                                                              }
                                                          else
                                                              {
                                                              LOGGER.debugTimedEvent(boolDebugDynamic,
                                                                                     SOURCE + "The Instrument has stopped, or the DAO Capture Timer is NULL");
                                                              }
                                                          }
                                                      }));

                        // Ensure that the Timer runs until it is told to stop
                        dao.getCaptureTimer().setRepeats(true);

                        // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay.
                        dao.getCaptureTimer().setInitialDelay(100);
                        dao.getCaptureTimer().restart();

                        LOGGER.debugTimedEvent(boolDebug,
                                               SOURCE + "DAO Capture Timer STARTED, HostMemoryFile OPENED [command=" + CAPTURE_COMMAND
                                               + "] [rate=" + (dao.getCaptureTimer().getDelay() / ChronosHelper.SECOND_MILLISECONDS) + "sec]");
                        }
                    else
                        {
                        // XPath failed trying to find the parent Plugin
                        LOGGER.error(SOURCE + "XPath failed trying to find the parent Plugin for the Capture Command [command=" + CAPTURE_COMMAND + "]");
                        dao.setCaptureTimer(null);
                        }
                    }
                else
                    {
                    // XPath failed trying to find the Command
                    LOGGER.error(SOURCE + "XPath failed trying to find the Plugin Command [command=" + CAPTURE_COMMAND + "]");
                    dao.setCaptureTimer(null);
                    }
                }
            else
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "Unable to open HostMemoryFile for data logging");
                dao.setCaptureTimer(null);
                }
            }
        else if ((boolModeChangedOk)
                 && (boolPreviousModeWasCapture)
                 && (!dao.isCaptureMode()))
            {
            if (dao.getCaptureTimer() != null)
                {
                dao.getCaptureTimer().stop();
                }

            // Stopping capture always closes the DAO HostMemory file
            dao.setCaptureMode(false);
            closeDAOHostMemoryFile(dao.getHostInstrument(), dao);

            // Try to tidy up, if we are allowed to run the gc
            ObservatoryInstrumentHelper.runGarbageCollector();

            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "DAO Capture Timer STOPPED, HostMemoryFile CLOSED");
            }
        else
            {
            // Nothing changed, so leave things alone, running OR stopped
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "DAO Capture Timer UNCHANGED, HostMemoryFile UNCHANGED, so do nothing");
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * doCaptureMode().
     * Process the Command Parameter.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    private static ResponseMessageInterface doCaptureMode(final StaribusCoreHostMemoryInterface dao,
                                                          final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "Capture.doCaptureMode() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_CAPTURE_MODE = 0;
        final CommandType cmdCapture;
        final List<ParameterType> listParameters;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdCapture = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, the capture mode
        listParameters = cmdCapture.getParameterList();

        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_CAPTURE_MODE) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_CAPTURE_MODE).getInputDataType().getDataTypeName()))
            && (dao.getHostInstrument() != null))
            {
            final boolean boolRequestedCaptureMode;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRequestedCaptureMode = Boolean.parseBoolean(listParameters.get(INDEX_CAPTURE_MODE).getValue());

            // We can only start capturing if not already capturing
            // We can stop capturing regardless of the current mode
            if (((boolRequestedCaptureMode) && (!dao.isCaptureMode()))
                || (!boolRequestedCaptureMode))
                {
                dao.setCaptureMode(boolRequestedCaptureMode);

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   INFO,
                                                   METADATA_TARGET
                                                       + dao.getHostInstrument().getInstrument().getIdentifier()
                                                       + TERMINATOR
                                                       + METADATA_ACTION_CAPTURE
                                                       + METADATA_CAPTURE
                                                       + boolRequestedCaptureMode
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);

                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }
            else
                {
                // We are already in Capture mode
                dao.setCaptureMode(true);
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.CAPTURE_ACTIVE);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
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
                                                                      cmdCapture,
                                                                      null,
                                                                      null,
                                                                      strResponseValue
        );
        return (responseMessage);
        }


    /***********************************************************************************************
     * Capture a single data record and write it to the specified file.
     * Obtain the data by executing the specified Command.
     * Return true if capture succeeded, false otherwise.
     *
     * @param hostinstrument
     * @param dao
     * @param instrument
     * @param module
     * @param command
     * @param memoryfile
     * @param maxfilesize
     * @param maxrecordcount
     * @param recordsize
     * @param capturesize
     * @param notifyport
     * @param notifymonitors
     *
     * @return boolean
     */

    private static boolean captureDataRecord(final ObservatoryInstrumentInterface hostinstrument,
                                             final StaribusCoreHostMemoryInterface dao,
                                             final Instrument instrument,
                                             final XmlObject module,
                                             final CommandType command,
                                             final RandomAccessFile memoryfile,
                                             final long maxfilesize,
                                             final long maxrecordcount,
                                             final int recordsize,
                                             final int capturesize,
                                             final boolean notifyport,
                                             final boolean notifymonitors)
        {
        final String SOURCE = "Capture.captureDataRecord() ";
        boolean boolSuccess;

        boolSuccess = true;

        try
            {
            if ((memoryfile != null)
                && (memoryfile.length() < maxfilesize)
                && (InstrumentState.isDoingSomething(hostinstrument))
                && (command.getSendToPort())
                && (dao.isCaptureMode())
                && (dao.getHostMemoryFile() != null)
                && (dao.getPort() != null)
                && (dao.getPort().getTxStream() != null)
                && (dao.getPort().getRxStream() != null))
                {
                final SwingWorker workerCapture;

                final List<String> errors;

                errors = new ArrayList<String>(10);

                // The command must complete and the data be written to file before we return
                workerCapture = new SwingWorker(REGISTRY.getThreadGroup(),
                                                SOURCE + "SwingWorker")
                                    {
                                    static final String CAPTURE_SOURCE = "Capture.captureDataRecord() SwingWorker ";

                                    public Object construct()
                                        {
                                        final ResponseMessageInterface responseMessage;

                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                                               CAPTURE_SOURCE + "--> construct() begins  [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");

                                        // Return data in the ResponseMessage WrappedData
                                        responseMessage = ExecuteCommandHelper.executeCommandOnSameThread(hostinstrument,
                                                                                                          dao,
                                                                                                          instrument,
                                                                                                          module,
                                                                                                          command,
                                                                                                          command.getParameterList(),
                                                                                                          StarscriptHelper.buildSimpleStarscript(instrument,
                                                                                                                                                 module,
                                                                                                                                                 null,
                                                                                                                                                 command,
                                                                                                                                                 false),
                                                                                                          errors,
                                                                                                          notifyport,
                                                                                                          notifymonitors);
                                        LOGGER.errors(SOURCE, errors);

                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                                               CAPTURE_SOURCE + "--> construct() returns [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");

                                        return (responseMessage);
                                        }

                                    public void finished()
                                        {
                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                                               CAPTURE_SOURCE + "--> finished() begins [response.notnull=" + (get() != null)
                                                                   + "] [response.ok=" + (get() instanceof ResponseMessageInterface)
                                                                   + "] [capture.mode=" + dao.isCaptureMode()
                                                                   + "] [file.ok=" + (dao.getHostMemoryFile() != null)
                                                                   + "] [errors.empty=" + (errors.isEmpty())
                                                                   + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");

                                        // Make sure we still have a file to write to
                                        if ((get() != null)
                                            && (get() instanceof ResponseMessageInterface)
                                            && (dao.isCaptureMode())
                                            && (dao.getHostMemoryFile() != null)
                                            && (errors.isEmpty()))
                                            {
                                            final ResponseMessageInterface responseMessage;

                                            responseMessage = (ResponseMessageInterface)get();

                                            if ((ResponseMessageStatus.wasResponseSuccessful(responseMessage))
                                                && (responseMessage.getWrappedData() != null)
                                                && (responseMessage.getWrappedData().getResponseValue() != null))
                                                {
                                                // Were there any data? If so, is the length correct?
                                                if ((!ResponseMessageStatus.RESPONSE_NODATA.equals(responseMessage.getWrappedData().getResponseValue()))
                                                    && (responseMessage.getWrappedData().getResponseValue().length() == capturesize))
                                                    {
                                                    try
                                                        {
                                                        final String strData;

                                                        // The separators are 0x1e, so replace with commas
                                                        strData = responseMessage.getWrappedData().getResponseValue().replaceAll(STARIBUS_RESPONSE_SEPARATOR_REGEX, COMMA);

                                                        // Make sure the size didn't change during the Replace
                                                        if ((strData.contains(COMMA))
                                                            && (strData.length() == capturesize))
                                                            {
                                                            final StringBuffer bufferRecord;

                                                            bufferRecord = new StringBuffer();

                                                            bufferRecord.append(ChronosHelper.toDateString(dao.getObservatoryClock().getCalendarDateNow()));
                                                            bufferRecord.append(COMMA);
                                                            bufferRecord.append(ChronosHelper.toTimeString(dao.getObservatoryClock().getCalendarTimeNow()));
                                                            bufferRecord.append(COMMA);
                                                            bufferRecord.append(strData);
                                                            bufferRecord.append("\n");

                                                            // Don't write anything to the file other than correctly formed records,
                                                            // otherwise things like getDataBlockCount() won't work
                                                            if (bufferRecord.length() == recordsize)
                                                                {
                                                                memoryfile.writeBytes(bufferRecord.toString());
                                                                }
                                                            else
                                                                {
                                                                LOGGER.log(CAPTURE_SOURCE + "Invalid data record assembled from Capture Command [command=" + command.getIdentifier()
                                                                               + "] [responsevalue=" + responseMessage.getWrappedData().getResponseValue()
                                                                               + "] [responsestatus=" + Utilities.intToBitString(responseMessage.getStatusBits())
                                                                               + "] [channelcount=" + dao.getRawDataChannelCount()
                                                                               + "] [record=" + bufferRecord + "]");
                                                                // No Errors to log
                                                                }
                                                            }
                                                        else
                                                            {
                                                            LOGGER.log(CAPTURE_SOURCE + "Invalid data returned from Capture Command [command=" + command.getIdentifier()
                                                                           + "] [responsevalue=" + responseMessage.getWrappedData().getResponseValue()
                                                                           + "] [responsestatus=" + Utilities.intToBitString(responseMessage.getStatusBits())
                                                                           + "] [channelcount=" + dao.getRawDataChannelCount() + "]");
                                                            // No Errors to log
                                                            }
                                                        }

                                                    catch (IOException exception)
                                                        {
                                                        LOGGER.error(CAPTURE_SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                                        }
                                                    }
                                                else
                                                    {
                                                    // We know ResponseMessage is not NULL
                                                    LOGGER.error(CAPTURE_SOURCE + "Missing or invalid data returned from Capture Command [command=" + command.getIdentifier()
                                                                     + "] [responsevalue=" + responseMessage.getWrappedData().getResponseValue()
                                                                     + "] [responsestatus=" + Utilities.intToBitString(responseMessage.getStatusBits())
                                                                     + "]");
                                                    }
                                                }
                                            else
                                                {
                                                // We know ResponseMessage is not NULL
                                                LOGGER.warn(
                                                        CAPTURE_SOURCE + "No data returned from Capture Command [command=" + command.getIdentifier()
                                                        + "] [responsestatus=" + Utilities.intToBitString(responseMessage.getStatusBits()) + "]");
                                                }
                                            }
                                        else
                                            {
                                            LOGGER.error(CAPTURE_SOURCE + "Capture Command Execution was not successful [command=" + command.getIdentifier() + "]");
                                            LOGGER.errors(CAPTURE_SOURCE, errors);
                                            }

                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                                               SOURCE + "--> finished() ends   [thread.group="
                                                                   + REGISTRY.getThreadGroup().getName()
                                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");
                                        }
                                    };

                // Start the Thread we have prepared...
                workerCapture.start();
                }
            else
                {
                LOGGER.error(SOURCE + "HostMemory File - Invalid Parameters, or the Instrument has stopped");
                boolSuccess = false;
                }
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Build the full filename of the File used to simulate HostMemory for this Instrument.
     *
     * @param obsinstrument
     * @param timestamped
     *
     * @return String
     */

    public static String buildHostMemoryFilename(final ObservatoryInstrumentInterface obsinstrument,
                                                 final boolean timestamped)
        {
        final String SOURCE = "Capture.buildHostMemoryFilename() ";
        final StringBuffer bufferFilename;

        bufferFilename = new StringBuffer();
        bufferFilename.append(InstallationFolder.DATASTORE.getName());
        bufferFilename.append(System.getProperty("file.separator"));
        bufferFilename.append(obsinstrument.getInstrument().getIdentifier());

        return(FileUtilities.buildFullFilename(bufferFilename.toString(),
                                               timestamped,
                                               DataFormat.STARIBUS));
        }


    /***********************************************************************************************
     * Open the DAO file used to simulate the logging memory.
     *
     * @param obsinstrument
     * @param dao
     * @param timestamped
     * @param maxfilesize
     *
     * @return boolean
     */

    private static boolean createDAOHostMemoryFile(final ObservatoryInstrumentInterface obsinstrument,
                                                   final StaribusCoreHostMemoryInterface dao,
                                                   final boolean timestamped,
                                                   final long maxfilesize)
        {
        final String SOURCE = "Capture.openDAOHostMemoryFile() ";
        boolean boolSuccess;

        try
            {
            final File fileHostMemory;

            fileHostMemory = new File(buildHostMemoryFilename(obsinstrument, timestamped));

            //  Create or overwrite the File used to simulate HostMemory for this Instrument
            FileUtilities.overwriteFile(fileHostMemory);
            dao.setHostMemoryFile(fileHostMemory);

            // Open for reading and writing as random access
            // If the file does not already exist then an attempt will be made to create it.
            // Also require that every update to the file's content be
            // written synchronously to the underlying storage device.
            // Note that the host OS may not write data immediately!
            dao.setRandomAccessFile(new RandomAccessFile(dao.getHostMemoryFile(), "rwd"));

            // Prepare the full file size for the first write (this does not extend the file)
            dao.getRandomAccessFile().setLength(0);
            dao.getRandomAccessFile().seek(maxfilesize - 1);
            dao.getRandomAccessFile().seek(0);

            // All ok if we get this far...
            boolSuccess = true;
            }

        catch (IllegalArgumentException exception)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET
                                                   + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                   + METADATA_ACTION_DAO_INIT
                                                   + METADATA_RESULT
                                                   + "Unable to initialise HostMemory File: Invalid File Mode"
                                                   + TERMINATOR_SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            LogHelper.updateEventLogFragment(dao);
            boolSuccess = false;
            }

        catch (FileNotFoundException exception)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET
                                                   + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                   + METADATA_ACTION_DAO_INIT
                                                   + METADATA_RESULT
                                                   + "Unable to initialise HostMemory File: FileNotFound"
                                                   + TERMINATOR_SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            LogHelper.updateEventLogFragment(dao);
            boolSuccess = false;
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET
                                                   + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                   + METADATA_ACTION_DAO_INIT
                                                   + METADATA_RESULT
                                                   + "Unable to initialise HostMemory File: IOException"
                                                   + TERMINATOR_SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            LogHelper.updateEventLogFragment(dao);
            boolSuccess = false;
            }

        catch (SecurityException exception)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET
                                                   + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                   + METADATA_ACTION_DAO_INIT
                                                   + METADATA_RESULT
                                                   + "Unable to initialise HostMemory File: SecurityManager"
                                                   + TERMINATOR_SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            LogHelper.updateEventLogFragment(dao);
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Close the DAO File used to simulate the HostMemory for this Instrument.
     *
     * @param obsinstrument
     * @param dao
     */

    public static void closeDAOHostMemoryFile(final ObservatoryInstrumentInterface obsinstrument,
                                              final StaribusCoreHostMemoryInterface dao)
        {
        final String SOURCE = "Capture.closeDAOHostMemoryFile() ";

        if (dao.getRandomAccessFile() != null)
            {
            try
                {
                dao.getRandomAccessFile().close();

                // The RandomAccess File has no method to indicate its open-close state, so use NULL
                dao.setRandomAccessFile(null);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET
                                                       + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                       + METADATA_ACTION_DAO_DISPOSE
                                                       + METADATA_RESULT
                                                       + "Unable to close HostMemory file and dispose of DAO"
                                                       + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);
                }
            }

        if (dao.getHostMemoryFile() != null)
            {
            dao.setHostMemoryFile(null);
            }
        }


    /***********************************************************************************************
     * Test to see if the specified BlockID is valid for the current length of the specified
     * HostMemory File.
     * Note that this ASSUMES the data format returned by getRealtimeData().
     *
     * @param file
     * @param blockid
     *
     * @return boolean
     */

    public static boolean isAccessibleBlockID(final File file,
                                              final int blockid)
        {
        final boolean boolValid;
        final long longPointerToLastByteOfRecord;

        // Check that the last byte of the last record of the block is in the file
        longPointerToLastByteOfRecord = ((blockid + 1)
                                            * StaribusCoreHostMemoryInterface.FILE_RECORDS_PER_STARIBUS_BLOCK
                                            * StaribusCoreHostMemoryInterface.RECORD_SIZE_BYTES_CAPTURED)
                                        - 1;

        boolValid = (file.length() >= longPointerToLastByteOfRecord);

        return (boolValid);
        }
    }
