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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.spectracyberserver.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.UDPUtilities;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.io.IOException;
import java.net.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.List;
import java.util.Vector;

import static org.lmn.fc.model.logging.EventStatus.INFO;
import static org.lmn.fc.model.logging.EventStatus.WARNING;


/***************************************************************************************************
 * SpectraCyberServerDAO.
 */

public final class SpectraCyberServerDAO extends AbstractObservatoryInstrumentDAO
                                         implements ObservatoryInstrumentDAOInterface
    {
    // String Resources
    private static final String MSG_ALREADY_STOPPED = "server is already stopped";
    private static final String MSG_ALREADY_RUNNING = "server is already running";
    private static final String PAYLOAD_UNBLOCK = "UNBLOCK";

    private static final String CONTROLLER_IDENTIFIER = "SpectraCyberController";

    private static final String MODULE_NAME = "Core";
    private static final String MODULE_COMMAND = "Core Command";
    private static final String SOFTWARE_VERSION = "00.1";

    private static final String COMMAND_CODE_BASE = "00";
    private static final String CODE_EXECUTE = "00";
    private static final String IDENTIFIER_EXECUTE = "execute";

    private static final String RESPONSE_NAME = "Response";
    private static final String PREFIX_NO_RESPONSE = "!";
    private static final String PREFIX_RESPONSE = "*!";

    private static final String COMMAND_EXECUTE = "execute";

    private static final boolean NOTIFY_MONITORS = true;

    // WARNING! Other addresses might not work... e.g. 1.1.1.1 fails
    private static final String UNBLOCK_ADDRESS = "192.168.1.255";
//    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;
    private static final long RETRY_WAIT_MILLIS = 4000;
//    private static final int RETRY_COUNT = 5;

    // The Thread for handling UDP
    private SwingWorker workerServer;

    // The UDP DatagramSocket
    private DatagramSocket datagramSocket;

    private boolean boolServerRunning;

    // UDP Resources
    private int intPortID;


    /***********************************************************************************************
     * Send a DatagramPacket to unblock receive() in the Server Thread during controlledStop().
     *
     * @param socket
     * @param portid
     */

    private static void unblockReceive(final DatagramSocket socket,
                                       final int portid)
        {
        if (socket != null)
            {
            try
                {
                final String strMessage;
                final byte[] buffer;
                final DatagramPacket sendPacket;

                strMessage = PAYLOAD_UNBLOCK;
                buffer = new byte[strMessage.length()];
                strMessage.getBytes(0, strMessage.length(), buffer, 0);

                sendPacket = new DatagramPacket(buffer,
                                                strMessage.length(),
                                                Inet4Address.getByName(UNBLOCK_ADDRESS),
                                                portid);
                socket.send(sendPacket);
                }

            catch (IOException exception)
                {
                // Just absorb the exception
                LOGGER.error("SpectraCyberServerDAO.unblockReceive() [exception=" + exception.getMessage() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Update the EventLog fragment associated with this DAO.
     *
     * @param dao
     */

    private static void updateLog(final ObservatoryInstrumentDAOInterface dao)
        {
        // Always update the Log, regardless of visibility
         // Don't update the Metadata
        dao.getHostInstrument().setWrappedData(dao.getWrappedData(), true, false);
        dao.getEventLogFragment().clear();
        }


    /***********************************************************************************************
     * Create the dummy Command to execute a SpectraCyber serial command.
     *
     * @param identifier
     * @param expectresponse
     *
     * @return CommandType
     */

    private static CommandType createCommand(final String identifier,
                                             final boolean expectresponse)
        {
        final CommandType commandType;

        commandType = CommandType.Factory.newInstance();
        commandType.setIdentifier(identifier);
        commandType.setCommandCode("FF");
        commandType.setCommandVariant("0000");

        // LegacyCode is not required
        // BlockedDataCommand etc. are not required

        commandType.setDescription("A dummy Command to control SpectraCyber");

        // Parameters are not required

        // SpectraCyber Commands are always sent to the port
        commandType.setSendToPort(true);

        if (expectresponse)
            {
            final ResponseType responseType;

            responseType = ResponseType.Factory.newInstance();
            responseType.setName("SpectraCyber.Response");
            responseType.setDataTypeName(DataTypeDictionary.STRING.getSchemaDataType());
            responseType.setUnits(SchemaUnits.DIMENSIONLESS);

            // REGEX is not required
            // Value is not required

            commandType.setResponse(responseType);
            }

        return (commandType);
        }


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("runServer");
        }


    /***********************************************************************************************
     * Construct a SpectraCyberServerDAO.
     *
     * @param hostinstrument
     */

    public SpectraCyberServerDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.workerServer = null;
        this.datagramSocket = null;

        this.boolServerRunning = false;

        this.intPortID = 0;

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        // Ensure that we get the port configuration...
        readResources();

        setServerWorker(null);
        setDatagramSocket(null);
        setRunning(false);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerDAO.disposeDAO()");

        // Ensure that we get the port configuration...
        readResources();

        SwingWorker.disposeWorker(getServerWorker(), true, SWING_WORKER_STOP_DELAY);
        setServerWorker(null);

        // Send a message to unblock receive() so controlledStop() can complete
        unblockReceive(getDatagramSocket(), getPortID());

        // Make sure that the Socket is released
        UDPUtilities.closeSocket(getDatagramSocket());

        setServerWorker(null);
        setDatagramSocket(null);
        setRunning(false);

        super.disposeDAO();
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return (new SpectraCyberServerCommandMessage(dao,
                                                     instrumentxml,
                                                     module,
                                                     command,
                                                     starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new SpectraCyberServerResponseMessage(portname,
                                                      instrumentxml,
                                                      module,
                                                      command,
                                                      starscript.trim(),
                                                      responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * Run the Server.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface runServer(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SpectraCyberServerDAO.runServer()";
        final ObservatoryInstrumentDAOInterface thisDAO;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        final Vector<Vector> vecDaoLogFragment;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // For use in inner classes
        thisDAO = this;

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, a control boolean
        listParameters = commandType.getParameterList();
        responseMessage = null;

        vecDaoLogFragment = commandmessage.getDAO().getEventLogFragment();

        // Create a new server, or stop the existing
        if ((listParameters != null)
            && (listParameters.size() == 1)
            && (listParameters.get(0) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (getHostInstrument() != null)
            && (commandmessage.getDAO() != null))
            {
            final boolean boolRunRequested;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRunRequested = Boolean.parseBoolean(listParameters.get(0).getValue());

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "SpectraCyberServerDAO.runServer() run=" + boolRunRequested);

            if ((!isRunning())
                && (boolRunRequested))
                {
                // We've been asked to Run a stopped Server
                // We need a SwingWorker otherwise this would execute on the PortController Thread,
                // which would block while waiting for a Response...

                setServerWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
                    {
                    /***********************************************************************************
                     * Listen for UDP Datagrams.
                     *
                     * @return Object
                     */

                    public Object construct()
                        {
                        try
                            {
                            boolean boolFinished;

                            boolFinished = false;

                            while ((!boolFinished)
                                && (Utilities.workerCanProceed(thisDAO, getServerWorker())))
                                {
                                try
                                    {
                                    final DatagramPacket incomingPacket;
                                    final byte[] receiveBuffer;
                                    final byte[] arrayCommand;
                                    final SocketAddress saIncoming;
                                    final InetAddress iaIncoming;
                                    final int intIncomingPort;

                                    // Update all Resources each time round
                                    readResources();

                                    // We musn't timeout, since this might run forever...
                                    TimeoutHelper.restartDAOTimeoutTimerInfinite(thisDAO);

                                    setDatagramSocket(new DatagramSocket(getPortID()));
                                    getDatagramSocket().setSoTimeout(0);

                                    receiveBuffer = new byte[UDPUtilities.MAX_UDP_PAYLOAD];
                                    incomingPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                                    // This method blocks until a UDP Datagram is received
                                    // This Command is being executed on its own SwingWorker, so this doesn't matter...
                                    // However, we need to send a DatagramPacket to unblock during controlledStop()
                                    getDatagramSocket().receive(incomingPacket);

                                    saIncoming = incomingPacket.getSocketAddress();
                                    iaIncoming = incomingPacket.getAddress();
                                    intIncomingPort = incomingPacket.getPort();
                                    UDPUtilities.closeSocket(getDatagramSocket());

                                    // If we get here, it must have worked
                                    arrayCommand = new byte[incomingPacket.getLength()];
                                    System.arraycopy(incomingPacket.getData(),
                                                     0,
                                                     arrayCommand,
                                                     0,
                                                     incomingPacket.getLength());

                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "SERVER: Received from " + saIncoming + " UDP bytes=[" + Utilities.byteArrayToExpandedAscii(arrayCommand) +"]");

                                    // Don't log the unblocking dummy message
                                    if (!Utilities.byteArrayToExpandedAscii(arrayCommand).contains(PAYLOAD_UNBLOCK))
                                        {
                                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                                           EventStatus.INFO,
                                                                           METADATA_TARGET_UDP_SERVER
                                                                                 + METADATA_ACTION_RECEIVE
                                                                                 + METADATA_ADDRESS_REMOTE + saIncoming + TERMINATOR + SPACE
                                                                                 + METADATA_PAYLOAD + Utilities.byteArrayToExpandedAscii(arrayCommand) + TERMINATOR,
                                                                           SOURCE,
                                                                           getPort().getObservatoryClock());
                                        updateLog(thisDAO);

                                        if ((Utilities.byteArrayToExpandedAscii(arrayCommand).startsWith(PREFIX_NO_RESPONSE))
                                            || (Utilities.byteArrayToExpandedAscii(arrayCommand).startsWith(PREFIX_RESPONSE)))
                                            {
                                            // It must be a valid SpectraCyber send to port command
                                            //System.out.println("SERVER: VALID SPECTRACYBER COMMAND!");

                                            // All SpectraCyber Commands are SendToPort, and require a Port
                                            if (getPort() != null)
                                                {
                                                final CommandType cmdExecute;
                                                final CommandMessageInterface msgExecute;
                                                final ResponseMessageInterface responseMessage;

                                                // Create a dummy 'Execute' Command
                                                cmdExecute = createCommand(COMMAND_EXECUTE,
                                                                           Utilities.byteArrayToExpandedAscii(arrayCommand).startsWith(PREFIX_RESPONSE));

                                                // This CommandMessage *must* contain the Instrument, Module,Command
                                                // because the RxStream uses them to create the ResponseMessage
                                                // Do we expect a Response?
                                                msgExecute = new SpectraCyberServerCommandMessage(thisDAO,
                                                                                                  commandmessage.getInstrument(),
                                                                                                  commandmessage.getModule(),
                                                                                                  cmdExecute,
                                                                                                  commandmessage.getStarScript(),
                                                                                                  arrayCommand);

                                                // Let the World know we are about to execute this Command
                                                ObservatoryInstrumentHelper.timestampCommandMessage(msgExecute,
                                                                                                    thisDAO.getObservatoryClock());

                                                // Send the timestamped Command to the PortController queue
                                                // The PortController issues a notifyPortMessageEvent() when the message is sent to the Port TxQueue
                                                getPort().queueCommandMessage(msgExecute,
                                                                              getTimeoutMillis(commandmessage.getModule(), cmdExecute),
                                                                              thisDAO.getObservatoryClock(),
                                                                              NOTIFY_MONITORS,
                                                                              LOADER_PROPERTIES.isTimingDebug());

                                                // Do we expect a Response?
                                                if (Utilities.byteArrayToExpandedAscii(arrayCommand).startsWith(PREFIX_RESPONSE))
                                                    {
                                                    //System.out.println("SERVER: Response expected");

                                                    // Return a timestamped ResponseMessage when complete
                                                    // The PortController issues a notifyPortMessageEvent() when the message is dequeued from the Port RxQueue
                                                    // This may come back NULL if it times out waiting for the RxQueue
                                                    // InterruptedException is also possible
                                                    // Always notify any monitors
                                                    // Use the unadjusted DAO timeout, not the current Timer delay
                                                    // We expect to get a SpectraCyberServerResponseMessage,
                                                    // but need only the byte array
                                                    responseMessage = getPort().dequeueResponseMessage(thisDAO,
                                                                                                       getTimeoutMillis(commandmessage.getModule(), cmdExecute),
                                                                                                       NOTIFY_MONITORS,
                                                                                                       LOADER_PROPERTIES.isTimingDebug());
                                                    // Debug only the ResponseValue
                                                    if ((responseMessage != null)
                                                        && (responseMessage.getByteArray() != null))
                                                        {
                                                        final DatagramSocket socketReply;

                                                        //System.out.println("SERVER: Good Response");
                                                        // First find the UDP address and port
                                                        socketReply = new DatagramSocket(intIncomingPort);

                                                        UDPUtilities.connectSocket(socketReply,
                                                                                   iaIncoming,
                                                                                   intIncomingPort);
                                                        UDPUtilities.send(socketReply,
                                                                          iaIncoming,
                                                                          intIncomingPort,
                                                                          responseMessage.getByteArray());
                                                        socketReply.close();
                                                        }
                                                    else
                                                        {
                                                        // TODO REVIEW This is required because it is the Queue timer which times out, not the DAO's timer
                                                        //setResponseStatus(ResponseMessageStatus.TIMEOUT);

                                                        //System.out.println("SERVER: null response == RS232 timeout");

                                                        final DatagramSocket socketReply;

                                                        // First find the UDP address and port
                                                        socketReply = new DatagramSocket(intIncomingPort);

                                                        UDPUtilities.connectSocket(socketReply,
                                                                                   iaIncoming,
                                                                                   intIncomingPort);
                                                        UDPUtilities.send(socketReply,
                                                                          iaIncoming,
                                                                          intIncomingPort,
                                                                          ResponseMessageStatus.TIMEOUT.getMnemonic().getBytes());
                                                        socketReply.close();
                                                        }
                                                    }
                                                else
                                                    {
                                                    final DatagramSocket socketReply;

                                                    // No Response is expected, so just say it worked
                                                    //System.out.println("SERVER: NO Response expected, just return SUCCESS");

                                                    // First find the UDP address and port
                                                    socketReply = new DatagramSocket(intIncomingPort);

                                                    UDPUtilities.connectSocket(socketReply,
                                                                               iaIncoming,
                                                                               intIncomingPort);
                                                    UDPUtilities.send(socketReply,
                                                                      iaIncoming,
                                                                      intIncomingPort,
                                                                      ResponseMessageStatus.SUCCESS.getMnemonic().getBytes());
                                                    socketReply.close();
                                                    }
                                                }
                                            else
                                                {
                                                // No Port, this should never happen...
                                                LOGGER.error("SERVER: No port");
                                                }
                                            }
                                        else
                                            {
                                            final DatagramSocket socketReply;

                                            // TODO LOCAL
                                            //System.out.println("SERVER: execute LOCAL server command");






                                            // First find the UDP address and port
                                            socketReply = new DatagramSocket(intIncomingPort);

                                            UDPUtilities.connectSocket(socketReply,
                                                                       iaIncoming,
                                                                       intIncomingPort);
                                            UDPUtilities.send(socketReply,
                                                              iaIncoming,
                                                              intIncomingPort,
                                                              RESPONSE_LOCAL.getBytes());
                                            socketReply.close();
                                            }
                                        }
                                    else
                                        {
                                        // UNBLOCK detected, so try to leave now!
                                        boolFinished = true;
                                        }

                                    UDPUtilities.closeSocket(getDatagramSocket());
                                    }

                                catch (SocketTimeoutException exception)
                                    {
                                    LOGGER.debugException(exception);
                                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                                       WARNING,
                                                                       METADATA_TARGET_UDP_SERVER
                                                                               + METADATA_ACTION_CONNECT
                                                                            + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_SOCKET + TERMINATOR + SPACE
                                                                            + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                                       SOURCE,
                                                                       getPort().getObservatoryClock());
                                    updateLog(thisDAO);
                                    UDPUtilities.closeSocket(getDatagramSocket());
                                    Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, thisDAO, getServerWorker());
                                    }

                                catch (PortUnreachableException exception)
                                    {
                                    LOGGER.debugException(exception);
                                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                                       WARNING,
                                                                       METADATA_TARGET_UDP_SERVER
                                                                               + METADATA_ACTION_CONNECT
                                                                            + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_PORT + TERMINATOR + SPACE
                                                                            + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                                       SOURCE,
                                                                       getPort().getObservatoryClock());
                                    updateLog(thisDAO);
                                    UDPUtilities.closeSocket(getDatagramSocket());
                                    Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, thisDAO, getServerWorker());
                                    }

                                catch (IllegalBlockingModeException exception)
                                    {
                                    LOGGER.debugException(exception);
                                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                                       WARNING,
                                                                       METADATA_TARGET_UDP_SERVER
                                                                               + METADATA_ACTION_CONNECT
                                                                            + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ILLEGAL_MODE + TERMINATOR + SPACE
                                                                            + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                                       SOURCE,
                                                                       getPort().getObservatoryClock());
                                    updateLog(thisDAO);
                                    UDPUtilities.closeSocket(getDatagramSocket());
                                    Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, thisDAO, getServerWorker());
                                    }

                                catch (IOException exception)
                                    {
                                    LOGGER.debugException(exception);
                                    SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                                       WARNING,
                                                                       METADATA_TARGET_UDP_SERVER
                                                                               + METADATA_ACTION_CONNECT
                                                                            + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_IO + TERMINATOR + SPACE
                                                                            + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                                       SOURCE,
                                                                       getPort().getObservatoryClock());
                                    updateLog(thisDAO);
                                    UDPUtilities.closeSocket(getDatagramSocket());
                                    Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, thisDAO, getServerWorker());
                                    }
                                }
                            }

                        catch (IllegalArgumentException exception)
                            {
                            LOGGER.debugException(exception);
                            SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                               WARNING,
                                                               METADATA_TARGET_UDP_SERVER
                                                                       + METADATA_ACTION_CONNECT
                                                                    + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ILLEGAL_ARGUMENT + TERMINATOR + SPACE
                                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                               SOURCE,
                                                               getPort().getObservatoryClock());
                            updateLog(thisDAO);
                            }

                        catch (SecurityException exception)
                            {
                            LOGGER.debugException(exception);
                            SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                               WARNING,
                                                               METADATA_TARGET_UDP_SERVER
                                                                       + METADATA_ACTION_CONNECT
                                                                    + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_SECURITY + TERMINATOR + SPACE
                                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                               SOURCE,
                                                               getPort().getObservatoryClock());
                            updateLog(thisDAO);
                            }

                        catch (IllegalBlockingModeException exception)
                            {
                            LOGGER.debugException(exception);
                            SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                               WARNING,
                                                               METADATA_TARGET_UDP_SERVER
                                                                       + METADATA_ACTION_CONNECT
                                                                    + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ILLEGAL_MODE + TERMINATOR + SPACE
                                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                               SOURCE,
                                                               getPort().getObservatoryClock());
                            updateLog(thisDAO);
                            }

                        catch (Exception exception)
                            {
                            LOGGER.debugException(exception);
                            SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                               WARNING,
                                                               METADATA_TARGET_UDP_SERVER
                                                                       + METADATA_ACTION_CONNECT
                                                                    + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_GENERIC + TERMINATOR + SPACE
                                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                               SOURCE,
                                                               getPort().getObservatoryClock());
                            updateLog(thisDAO);
                            }

                        finally
                            {
                            // Make sure that the Socket is released
                            // Timeouts will close the socket anyway
                            UDPUtilities.closeSocket(getDatagramSocket());

                            // Help the GC?
                            setDatagramSocket(null);
                            }

                        return (null);
                        }


                    /***********************************************************************************
                     * When the Server Thread stops.
                     */

                    public void finished()
                        {
                        // Make sure that the Socket is released
                        // Timeouts will close the socket anyway
                        UDPUtilities.closeSocket(getDatagramSocket());

                        // Put the Timeout back to what it should be for a single default command
                        TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(thisDAO);

                        // Help the GC?
                        setDatagramSocket(null);

                        setRunning(false);
                        }
                    });

                // Start the Server Thread we have prepared...
                getServerWorker().start();
                setRunning(true);

                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   INFO,
                                                   METADATA_TARGET_UDP_SERVER
                                                        + METADATA_ACTION_START,
                                                   SOURCE,
                                                   getPort().getObservatoryClock());
                updateLog(thisDAO);

                // Create the SUCCESS ResponseMessage
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                    commandmessage,
                                                                                    commandType);
                }
            else if ((isRunning())
                    && (boolRunRequested))
                {
                // We've been asked to Run a running Server, which must fail
                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   WARNING,
                                                   METADATA_TARGET_UDP_SERVER
                                                           + METADATA_ACTION_START
                                                           + METADATA_MESSAGE + MSG_ALREADY_RUNNING + TERMINATOR,
                                                   SOURCE,
                                                   getPort().getObservatoryClock());
                updateLog(thisDAO);
                }
            else if ((isRunning())
                    && (!boolRunRequested))
                {
                // We've been asked to Stop a running Server
                // Put the Timeout back to what it should be for a single default command
                TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(thisDAO);

                // We must try to stop the Server
                SwingWorker.disposeWorker(getServerWorker(), true, SWING_WORKER_STOP_DELAY);
                setServerWorker(null);

                // Send a message to unblock receive() so controlledStop() can complete
                unblockReceive(getDatagramSocket(), getPortID());

                UDPUtilities.closeSocket(getDatagramSocket());

                setDatagramSocket(null);
                setRunning(false);

                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   INFO,
                                                   METADATA_TARGET_UDP_SERVER
                                                        + METADATA_ACTION_STOP,
                                                   SOURCE,
                                                   getPort().getObservatoryClock());
                updateLog(thisDAO);

                // Create the SUCCESS ResponseMessage
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                    commandmessage,
                                                                                    commandType);
                }
            else if ((!isRunning())
                    && (!boolRunRequested))
                {
                // We've been asked to Stop a stopped Server, which must fail
                SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                   WARNING,
                                                   METADATA_TARGET_UDP_SERVER
                                                           + METADATA_ACTION_STOP
                                                           + METADATA_MESSAGE + MSG_ALREADY_STOPPED + TERMINATOR,
                                                   SOURCE,
                                                   getPort().getObservatoryClock());
                updateLog(thisDAO);
                }
            }

        // Did we still fail?
        if (responseMessage == null)
            {
            // Invalid Parameters
            // Create the ResponseMessage
            thisDAO.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
            responseMessage = ResponseMessageHelper.constructEmptyResponse(this,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           commandType,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            commandType));
            }

        // Something has changed, we may need to refresh a browser etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

        return (responseMessage);
        }


    /***********************************************************************************************
     * Indicate if the Server is running.
     * This is not in an interface.
     *
     * @return boolean
     */

    public boolean isRunning()
        {
        return (this.boolServerRunning);
        }


    /***********************************************************************************************
     * Control the Server state.
     *
     * @param running
     */

    private void setRunning(final boolean running)
        {
        this.boolServerRunning = running;
        }


    /**********************************************************************************************/
    /* Socket                                                                                     */
    /***********************************************************************************************
     * Get the DatagramSocket underlying this TxStream.
     *
     * @return DatagramSocket
     */

    private DatagramSocket getDatagramSocket()
        {
        return (this.datagramSocket);
        }


    /***********************************************************************************************
     * Set the DatagramSocket underlying this TxStream.
     *
     * @param socket
     */

    private void setDatagramSocket(final DatagramSocket socket)
        {
        this.datagramSocket = socket;
        }


    /***********************************************************************************************
     * Get the PortID.
     *
     * @return int
     */

    private int getPortID()
        {
        return (this.intPortID);
        }


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Get the SwingWorker which handles the Server.
     *
     * @return SwingWorker
     */

    private SwingWorker getServerWorker()
        {
        return (this.workerServer);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the Server.
     *
     * @param worker
     */

    private void setServerWorker(final SwingWorker worker)
        {
        this.workerServer = worker;
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     *
     * KEY_SERVER_PORT_ID
     * KEY_SERVER_TIMEOUT
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        // Ethernet UDP Port
        intPortID = REGISTRY.getIntegerProperty(getResourceKey() + KEY_SERVER_PORT_ID);

        // Reload the DAO Configuration every time the Resources are read
        if (getDAOConfiguration() != null)
            {
            ConfigurationHelper.addItemToConfiguration(getDAOConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_SERVER_PORT_ID,
                                                       Integer.toString(getPortID()));
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }
    }
