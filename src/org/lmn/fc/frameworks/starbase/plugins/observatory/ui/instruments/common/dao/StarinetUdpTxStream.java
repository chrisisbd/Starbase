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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.UDPUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractTxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Vector;

import static org.lmn.fc.model.logging.EventStatus.*;


/***************************************************************************************************
 * StarinetUdpTxStream.
 */

public final class StarinetUdpTxStream extends AbstractTxStream
                                       implements PortTxStreamInterface
    {
    // String Resources
    private static final String MSG_TIMEOUT = "Timeout";

    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;
    private static final long RETRY_WAIT_MILLIS = 4000;

    // The Thread for writing data
    private SwingWorker workerWrite;

    // The UDP DatagramSocket
    private DatagramSocket datagramSocket;

    // StarinetUdpTxStream Stream Resources
    private long longPortTimeoutMillis;


    /***********************************************************************************************
     * Construct a StarinetUdpTxStream.
     *
     * @param resourcekey
     */

    public StarinetUdpTxStream(final String resourcekey)
        {
        super(resourcekey);

        this.workerWrite = null;
        this.datagramSocket = null;

        this.longPortTimeoutMillis = DEFAULT_TIMEOUT_MILLIS;
        }


    /***********************************************************************************************
     * Get the Tx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.STARINET);
        }


    /***********************************************************************************************
     * Initialise the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean initialise() throws IOException
        {
        return (true);
        }


    /***********************************************************************************************
     * Open the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean open() throws IOException
        {
        final String SOURCE = "StarinetUdpTxStream.open()";

        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  SOURCE);

        // Ensure that we get the port configuration...
        readResources();

        setWriteWorker(null);
        setDatagramSocket(null);

        // Everything is working, so reveal the underlying Streams
        // Do them both here, to avoid having to pass a Stream to the RxStream
        setUnderlyingTxStream(null);

        if (getLoopbackRxStream() != null)
            {
            // This stream will never be used...
            getLoopbackRxStream().setUnderlyingRxStream(null);
            }

        // Mark this Stream as Open, if successful
        this.boolStreamOpen = (getHostPort() != null);

        return (this.boolStreamOpen);
        }


    /***********************************************************************************************
     * Closes this stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    public void close() throws IOException
        {
        final String SOURCE = "StarinetUdpTxStream.close()";

        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  SOURCE);

        // Do not close the port or streams if attached to the StaribusPort!
        // This should of course never happen with Starinet
        if ((getHostPort() != null)
            && (!getHostPort().isStaribusPort()))
            {
            if (getUnderlyingTxStream() != null)
                {
                getUnderlyingTxStream().flush();
                getUnderlyingTxStream().close();
                }

            SwingWorker.disposeWorker(getWriteWorker(), true, SWING_WORKER_STOP_DELAY);
            setWriteWorker(null);

            // Make sure that the Socket is released
            UDPUtilities.closeSocket(getDatagramSocket());

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            }
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        final String SOURCE = "StarinetUdpTxStream.reset()";

        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  SOURCE);

        // This may be called by e.g. Abort in the DAO
        SwingWorker.disposeWorker(getWriteWorker(), true, SWING_WORKER_STOP_DELAY);
        setWriteWorker(null);

        // Make sure that the Socket is released
        UDPUtilities.closeSocket(getDatagramSocket());
        }


    /***********************************************************************************************
     * Flushes this stream by writing any buffered output to the underlying stream.
     *
     * @throws IOException If an I/O error occurs
     */

    public void flush() throws IOException
        {
        // There is no buffered output
        }


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     *
     * @param instrumentsdoc
     * @param commandmessage
     *
     * @throws IOException
     */

    public final void write(final InstrumentsDocument instrumentsdoc,
                            final CommandMessageInterface commandmessage) throws IOException
        {
        final String SOURCE = "StarinetUdpTxStream.write() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        // Send the message to the TxStream, but only if the Tx checksum is valid
        if ((commandmessage != null)
            && (commandmessage.getDAO() != null)
            && (StreamUtilities.isStaribusTxChecksumValid(commandmessage))
            && (isStreamOpen())
            && (getLoopbackRxStream() != null))
            {
            final Vector<Vector> vecDaoLogFragment;

            vecDaoLogFragment = commandmessage.getDAO().getEventLogFragment();

            // Make sure the RxStream can't see what we are doing
            getLoopbackRxStream().setUnderlyingRxStream(null);

            // We need a SwingWorker otherwise this would execute on the PortController Thread,
            // which would block while waiting for a Response...
            setWriteWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
                {
                /***********************************************************************************
                 * Send the UDP Datagram.
                 *
                 * @return Object
                 */

                public Object construct()
                    {
                    ByteBuffer buffer;

                    buffer = null;

                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "--> construct() begins  [thread.group=" + REGISTRY.getThreadGroup().getName()
                                               + "] [thread.name=" + Thread.currentThread().getName() + "]");
                    try
                        {
                        String strIPAddress;
                        final InetAddress remoteAddress;
                        final int intPortID;
                        boolean boolSuccess;

                        strIPAddress = EMPTY_STRING;

                        // Update all Resources
                        readResources();

                        if ((commandmessage.getDAO() != null)
                            && (commandmessage.getDAO().getHostInstrument() != null)
                            && (ObservatoryInstrumentHelper.isEthernetController(commandmessage.getDAO().getHostInstrument().getInstrument())))
                            {
                            strIPAddress = IPVersion.stripTrailingPaddingFromIPAddressAndPort(commandmessage.getDAO().getHostInstrument().getInstrument().getController().getIPAddress());
                            }
                        else
                            {
                            throw new IllegalArgumentException(EXCEPTION_NO_IPADDRESS);
                            }

                        // Make sure the RxStream can't see what we are doing
                        if (getLoopbackRxStream() != null)
                            {
                            // This stream will never be used...
                            getLoopbackRxStream().setUnderlyingRxStream(null);

                            // Prepare the RxStream for the write()
                            getLoopbackRxStream().setCommandContext(null);
                            }

                        remoteAddress = InetAddress.getByName(ObservatoryInstrumentHelper.getIPAddressWithoutPort(strIPAddress));
                        // Get the Port if specified
                        intPortID = ObservatoryInstrumentHelper.getPortFromIPAddress(strIPAddress, DEFAULT_STARINET_UDP_PORT);

                        if (boolDebug)
                            {
                            SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                               INFO,
                                                               METADATA_TARGET_STARINET
                                                                 + METADATA_ACTION_WRITE
                                                                     + METADATA_IP + remoteAddress + TERMINATOR + SPACE
                                                                     + METADATA_PORT + intPortID + TERMINATOR + SPACE
                                                                     + METADATA_PAYLOAD + Utilities.byteArrayToSpacedHex(commandmessage.getByteArray()) + TERMINATOR,
                                                               SOURCE,
                                                               getHostPort().getObservatoryClock());
                            LogHelper.updateEventLogFragment(commandmessage.getDAO());
                            }

                        // Timeout is not possible up to here...
                        // Check to see if the SwingWorker has been asked to stop before trying again

                        commandmessage.getDAO().getResponseMessageStatusList().clear();
                        boolSuccess = false;

                        for (int retryid = 0;
                             ((retryid < TimeoutHelper.RETRY_COUNT)
                                  && (!boolSuccess)
                                  && (Utilities.retryCanProceed(commandmessage.getDAO(), commandmessage.getDAO().getResponseMessageStatusList(), this)));
                             retryid++)
                            {
                            try
                                {
                                final byte[] arrayResponse;

                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "Start Tx retry [retryid=" + retryid
                                                           + "] [port.timeout=" + longPortTimeoutMillis
                                                           + "msec] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                           + "] [thread.name=" + Thread.currentThread().getName() + "]");

                                // Adjust the DAO timer for the number of retries left to go
                                TimeoutHelper.adjustDaoTimeoutTimerStarinet(commandmessage.getDAO(),
                                                                            retryid,
                                                                            TimeoutHelper.RETRY_COUNT,
                                                                            longPortTimeoutMillis,
                                                                            RETRY_WAIT_MILLIS);

                                // First find the UDP port to be used for the TxStream (and RxStream)
                                setDatagramSocket(new DatagramSocket(intPortID));
                                getDatagramSocket().setSoTimeout((int) longPortTimeoutMillis);

                                UDPUtilities.connectSocket(getDatagramSocket(), remoteAddress, intPortID);

                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "Sending bytes=[" + Utilities.byteArrayToExpandedAscii(commandmessage.getByteArray()) + "]");

                                UDPUtilities.send(getDatagramSocket(),
                                                  remoteAddress,
                                                  intPortID,
                                                  commandmessage.getByteArray());

                                if (Utilities.retryCanProceed(commandmessage.getDAO(), commandmessage.getDAO().getResponseMessageStatusList(), this))
                                    {
                                    LOGGER.debugTimedEvent(boolDebug,
                                                           SOURCE + "Waiting for UDP reply [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                           + "] [thread.name=" + Thread.currentThread().getName() + "]");

                                    // This method blocks until a UDP Datagram is received
                                    // This Command is being executed on its own SwingWorker, so this doesn't matter...
                                    arrayResponse = UDPUtilities.receive(getDatagramSocket());

                                    // Wrap up the Response to return
                                    buffer = ByteBuffer.wrap(arrayResponse);

                                    // If we get there, it all happened without a Timeout
                                    LOGGER.debugTimedEvent(boolDebug,
                                                           SOURCE + "Reply received bytes=[" + Utilities.byteArrayToExpandedAscii(buffer.array())
                                                               + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                               + "] [thread.name=" + Thread.currentThread().getName() + "]");
                                    boolSuccess = true;
                                    }
                                else
                                    {
                                    LOGGER.debugTimedEvent(boolDebug,
                                                           SOURCE + "Thread is stopping [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                           + "] [thread.name=" + Thread.currentThread().getName() + "]");
                                    }

                                // Timeouts will close the socket anyway
                                UDPUtilities.closeSocket(getDatagramSocket());

                                // Help the GC?
                                setDatagramSocket(null);
                                }

                            catch (SocketTimeoutException exception)
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "SocketTimeoutException [retryid=" + retryid + "]");
                                UDPUtilities.closeSocket(getDatagramSocket());
                                Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, commandmessage.getDAO(), this);
                                }

                            catch (PortUnreachableException exception)
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "PortUnreachableException [retryid=" + retryid + "]");
                                UDPUtilities.closeSocket(getDatagramSocket());
                                Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, commandmessage.getDAO(), this);
                                }

                            catch (IOException exception)
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "IOException [retryid=" + retryid + "]");
                                UDPUtilities.closeSocket(getDatagramSocket());
                                Utilities.safeSleepPollWorker(RETRY_WAIT_MILLIS, commandmessage.getDAO(), this);
                                }
                            }

                        if (boolSuccess)
                            {
                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "Inform LoopbackRxStream of Context [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");

                            // Let the Receive Stream know the Instrument, Module and Command context
                            // Don't do this if write() failed because of an IOException
                            getLoopbackRxStream().setCommandContext(commandmessage);

                            // WARNING! This appears to be necessary here rather than in finished() to avoid a Thread issue,
                            // which was very hard to find (if in fact I have found it).
                            getLoopbackRxStream().setUnderlyingRxStream(new ByteArrayInputStream(buffer.array()));
                            }
                        else
                            {
                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "TIMEOUT [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");
                            SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                               TIMEOUT,
                                                               METADATA_TARGET_STARINET
                                                                    + METADATA_ACTION_WRITE
                                                                    + METADATA_MESSAGE + MSG_TIMEOUT + TERMINATOR,
                                                               SOURCE,
                                                               getHostPort().getObservatoryClock());
                            LogHelper.updateEventLogFragment(commandmessage.getDAO());
                            }
                        }

                    catch (IllegalArgumentException exception)
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           WARNING,
                                                           METADATA_TARGET_STARINET
                                                                + METADATA_ACTION_WRITE
                                                                + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ILLEGAL_ARGUMENT + TERMINATOR + SPACE
                                                                + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                           SOURCE,
                                                           getHostPort().getObservatoryClock());
                        LogHelper.updateEventLogFragment(commandmessage.getDAO());
                        }

                    catch (UnknownHostException exception)
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           WARNING,
                                                           METADATA_TARGET_STARINET
                                                                + METADATA_ACTION_WRITE
                                                                + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_UNKNOWN_HOST + TERMINATOR + SPACE
                                                                + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                           SOURCE,
                                                           getHostPort().getObservatoryClock());
                        LogHelper.updateEventLogFragment(commandmessage.getDAO());
                        }

                    catch (SecurityException exception)
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           WARNING,
                                                           METADATA_TARGET_STARINET
                                                                + METADATA_ACTION_WRITE
                                                                + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_SECURITY + TERMINATOR + SPACE
                                                                + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                           SOURCE,
                                                           getHostPort().getObservatoryClock());
                        LogHelper.updateEventLogFragment(commandmessage.getDAO());
                        }

                    catch (IllegalBlockingModeException exception)
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           WARNING,
                                                           METADATA_TARGET_STARINET
                                                                + METADATA_ACTION_WRITE
                                                                + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ILLEGAL_MODE + TERMINATOR + SPACE
                                                                + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                           SOURCE,
                                                           getHostPort().getObservatoryClock());
                        LogHelper.updateEventLogFragment(commandmessage.getDAO());
                        }

                    catch (IOException exception)
                        {
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           WARNING,
                                                           METADATA_TARGET_STARINET
                                                                + METADATA_ACTION_WRITE
                                                                + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_IO + TERMINATOR + SPACE
                                                                + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                           SOURCE,
                                                           getHostPort().getObservatoryClock());
                        LogHelper.updateEventLogFragment(commandmessage.getDAO());
                        }

                    catch (Exception exception)
                        {
                        exception.printStackTrace();
                        SimpleEventLogUIComponent.logEvent(vecDaoLogFragment,
                                                           WARNING,
                                                           METADATA_TARGET_STARINET
                                                                + METADATA_ACTION_WRITE
                                                                + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_IO + TERMINATOR + SPACE
                                                                + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                                           SOURCE,
                                                           getHostPort().getObservatoryClock());
                        LogHelper.updateEventLogFragment(commandmessage.getDAO());
                        }

                    finally
                        {
                        // Make sure that the Socket is released
                        UDPUtilities.closeSocket(getDatagramSocket());

                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                               SOURCE + "Socket released");
                        }

                    if (buffer != null)
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                               SOURCE + "--> construct() returns bytes=[" + Utilities.byteArrayToExpandedAscii(buffer.array())
                                                    + "] [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                    + "] [thread.name=" + Thread.currentThread().getName() + "]");
                        }
                    else
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isThreadsDebug(),
                                               SOURCE + "--> construct() returns [thread.group=" + REGISTRY.getThreadGroup().getName()
                                               + "] [thread.name=" + Thread.currentThread().getName() + "]");
                        }

                    // buffer may be null if there are no data
                    return (buffer);
                    }


                /***********************************************************************************
                 * Get any response from the UDP write().
                 */

                public void finished()
                    {
                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "--> finished() begins [thread.group=" + REGISTRY.getThreadGroup().getName()
                                               + "] [thread.name=" + Thread.currentThread().getName() + "]");

                    // Put the Timeout back to what it should be for a single default command
                    TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(commandmessage.getDAO());

                    if ((get() != null)
                        && (get() instanceof ByteBuffer)
                        && (((ByteBuffer)get()).array() != null))
                        {
                        if (getLoopbackRxStream() != null)
                            {
                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "--> finished() SUCCESS [thread.group=" + REGISTRY.getThreadGroup().getName()
                                                   + "] [thread.name=" + Thread.currentThread().getName() + "]");
                            //TODO REVIEW  getLoopbackRxStream().setUnderlyingRxStream(new ByteArrayInputStream(((ByteBuffer)get()).array()));
                            }
                        }
                    else
                        {
                        LOGGER.debugTimedEvent(boolDebug,
                                               SOURCE + "--> finished() FAILED [thread.group=" + REGISTRY.getThreadGroup().getName()
                                               + "] [thread.name=" + Thread.currentThread().getName() + "]");
                        if (getLoopbackRxStream() != null)
                            {
                            getLoopbackRxStream().setUnderlyingRxStream(null);
                            }
                        }

                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "--> finished() ends   [thread.group=" + REGISTRY.getThreadGroup().getName()
                                               + "] [thread.name=" + Thread.currentThread().getName() + "]");
                    }
                });

            // Start the Thread we have prepared...
            getWriteWorker().start();
            }
        else
            {
            LOGGER.error(SOURCE + "ERROR Unable to transmit");
            }
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


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Get the SwingWorker which writes the data.
     *
     * @return SwingWorker
     */

    private SwingWorker getWriteWorker()
        {
        return (this.workerWrite);
        }


    /***********************************************************************************************
     * Set the SwingWorker which writes the data.
     *
     * @param worker
     */

    private void setWriteWorker(final SwingWorker worker)
        {
        this.workerWrite = worker;
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     *
     * KEY_PORT_HOSTNAME
     * KEY_PORT_PORT_ID
     * KEY_PORT_TIMEOUT
     */

    public final void readResources()
        {
        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  "StarinetUdpTxStream.readResources() [ResourceKey=" + getResourceKey() + "]");

        // Ethernet UDP Port
        longPortTimeoutMillis = ChronosHelper.SECOND_MILLISECONDS * REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_TIMEOUT);

        if (longPortTimeoutMillis <= 0)
            {
            longPortTimeoutMillis = ChronosHelper.SECOND_MILLISECONDS;
            }

        // Reload the Stream Configuration every time the Resources are read
        if (getStreamConfiguration() != null)
            {
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_TIMEOUT,
                                                       Long.toString(longPortTimeoutMillis));
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }
    }
