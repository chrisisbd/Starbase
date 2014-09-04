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

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.StaribusReceiveEventState;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractRxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.io.InputStream;


/***************************************************************************************************
 * StaribusRxStream.
 *
 * Default settings: 57600 Baud, 7 Data Bits, Even Parity, 1 Stop Bit, No flow control
 */

public class StaribusRxStream extends AbstractRxStream
                              implements PortRxStreamInterface,
                                         SerialPortEventListener
    {
    private static final int BUFFER_CAPACITY = 10000;
    private static final int RETRY_WAIT_MILLIS = 200;
    private static final int LENGTH_BEFORE_TERMINATOR = 19;

    // Previously the retry count was:
    // Allow enough time to read the longest message, i.e. a StaribusBlock
    // at the slowest expected Baud rate, 9600, i.e. roughly 1msec/char
    // (StaribusParsers.LENGTH_STARIBUS_BLOCK / RETRY_WAIT_MILLIS) << 1;

    private static final int MAX_RETRIES = 5;

    private final StringBuffer readBuffer;
    private boolean boolReadyToBeReadByPortController;
    private int intLengthBeforeTerminator;


    /***********************************************************************************************
     *  Process the state ACCUMULATE, return the next state.
     *
     * @param SOURCE
     * @param rxstream
     * @param currentstate
     * @param buffer
     * @param debugmode
     *
     * @return StaribusReceiveEventState
     */

    private static StaribusReceiveEventState stateAccumulate(final String SOURCE,
                                                             final InputStream rxstream,
                                                             final StaribusReceiveEventState currentstate,
                                                             final StringBuffer buffer,
                                                             final boolean debugmode)
        {
        final StaribusReceiveEventState nextState;
        boolean boolFoundTerminator;

        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + MSG_ENTERED_STATE + currentstate.getLogEntry());
        boolFoundTerminator = false;

        try
            {
            int intCharacterCounter;

            intCharacterCounter = 0;

            while ((!boolFoundTerminator)
                && (rxstream != null)
                && (rxstream.available() > 0))
                {
                final int intData;

                // This method blocks until input data is available,
                // the end of the stream is detected, or an exception is thrown.
                // Read all received characters, regardless...
                intData = rxstream.read();
                intCharacterCounter++;

                // Is it ever necessary to check for return value -1 ?
                if (intData >= 0)
                    {
                    // There are some bytes available, so accumulate them
                    buffer.append((char)intData);

                    LOGGER.debugStaribusEvent(debugmode,
                                              (SOURCE + "[rx_char=" +  Utilities.byteToTwoHexString((byte)intData) + "]"));

                    // Leave on the first terminator, EOT
                    if ((byte)intData == STARIBUS_TERMINATOR_0)
                        {
                        // Leave loop once the terminator is found
                        boolFoundTerminator = true;
                        }
                    }
                else
                    {
                    LOGGER.debugStaribusEvent(debugmode,
                                              (SOURCE + "Unexpected End of Stream" + currentstate.getLogEntry()));
                    }

                if (rxstream.available() <= 0)
                    {
                    LOGGER.debugStaribusEvent(debugmode,
                                              (SOURCE + "End of Stream" + currentstate.getLogEntry()));
                    }
                }
            }

        catch(IOException exception)
            {
            // Ignore errors normally, just keep trying
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "IOException reading data from serial input stream [exception=" + exception + "]" + currentstate.getLogEntry());
            }

        catch (NullPointerException exception)
            {
            // Ignore errors normally, just keep trying
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "NullPointerException reading data from non-existent serial input stream [exception=" + exception + "]" + currentstate.getLogEntry());
            }

        // We either ran out of characters, or we found the terminator sequence
        // If we found the terminator, leave immediately
        // If we reached the end of the stream, but didn't find the terminator,
        // wait a little while in case more data are coming
        if (boolFoundTerminator)
            {
            nextState = StaribusReceiveEventState.DRAIN_TRAILING;
            }
        else
            {
            // No terminator, so stay where we are
            nextState = StaribusReceiveEventState.ACCUMULATE;
            }

        return (nextState);
        }


    /***********************************************************************************************
     *  Process the state WAIT_EOT, return the next state.
     *
     * @param SOURCE
     * @param rxstream
     * @param currentstate
     * @param debugmode
     *
     * @return StaribusReceiveEventState
     */

    private static StaribusReceiveEventState stateWaitEOT(final String SOURCE,
                                                          final InputStream rxstream,
                                                          final StaribusReceiveEventState currentstate,
                                                          final boolean debugmode)
        {
        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + MSG_ENTERED_STATE + StaribusReceiveEventState.WAIT_EOT.getLogEntry());

        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + "Are there more characters to come? Go to sleep [delay=" + RETRY_WAIT_MILLIS + "msec]");
        Utilities.safeSleep(RETRY_WAIT_MILLIS);

        // Don't affect the current state
        return (currentstate);
        }


    /***********************************************************************************************
     *  Process the state DRAIN_TRAILING, return the next state.
     *
     * @param SOURCE
     * @param rxstream
     * @param currentstate
     * @param debugmode
     *
     * @return StaribusReceiveEventState
     */

    private static StaribusReceiveEventState stateDrainTrailing(final String SOURCE,
                                                                final InputStream rxstream,
                                                                final StaribusReceiveEventState currentstate,
                                                                final boolean debugmode)
        {
        final StaribusReceiveEventState nextState;

        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + MSG_ENTERED_STATE + currentstate.getLogEntry());

        // Discard extra characters after the EOT
        try
            {
            while ((rxstream != null)
                && (rxstream.available() > 0))
                {
                final int intTrailingData;

                intTrailingData = rxstream.read();
                LOGGER.debugStaribusEvent(debugmode,
                                          (SOURCE + "WARNING! Draining trailing data [rx_char=" +  Utilities.byteToTwoHexString((byte)intTrailingData) + "]"));
                }
            }

        catch(IOException exception)
            {
            // Ignore errors normally, just keep trying
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "IOException reading data from serial input stream [exception=" + exception + "]" + currentstate.getLogEntry());
            }

        catch (NullPointerException exception)
            {
            // Ignore errors normally, just keep trying
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "NullPointerException reading data from non-existent serial input stream [exception=" + exception + "]" + currentstate.getLogEntry());
            }

        // Always return to IDLE
        nextState = StaribusReceiveEventState.IDLE;

        return (nextState);
        }


    /***********************************************************************************************
     * Process the state FIND_STX, return the next state.
     *
     * @param SOURCE
     * @param rxstream
     * @param currentstate
     * @param buffer
     * @param debugmode
     *
     * @return StaribusReceiveEventState
     */

    private static StaribusReceiveEventState stateFindSTX(final String SOURCE,
                                                          final InputStream rxstream,
                                                          final StaribusReceiveEventState currentstate,
                                                          final StringBuffer buffer,
                                                          final boolean debugmode)
        {
        final StaribusReceiveEventState nextState;
        boolean boolFoundSTX;

        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + MSG_ENTERED_STATE + currentstate.getLogEntry());
        boolFoundSTX = false;

        try
            {
            while ((!boolFoundSTX)
                && (rxstream != null)
                && (rxstream.available() > 0))
                {
                final int intData;

                // This method blocks until input data is available,
                // the end of the stream is detected, or an exception is thrown.
                // Read all received characters, regardless...
                intData = rxstream.read();

                // Is it ever necessary to check for return value -1 ?
                if (intData >= 0)
                    {
                    if (STARIBUS_START == (byte)intData)
                        {
                        LOGGER.debugStaribusEvent(debugmode,
                                                  (SOURCE + "Found start of potential Staribus message"));

                        // Only accumulate the STX
                        buffer.append((char)intData);

                        // Leave loop once the STX is found
                        boolFoundSTX = true;
                        }
                    else
                        {
                        LOGGER.debugStaribusEvent(debugmode,
                                                  (SOURCE + "Discarded while waiting for start of message [discard_rx_char=" +  Utilities.byteToTwoHexString((byte)intData) + "]"));
                        }
                    }
                else
                    {
                    LOGGER.debugStaribusEvent(debugmode,
                                              (SOURCE + "Unexpected End of Stream" + currentstate.getLogEntry()));
                    }
                }
            }

        catch(IOException exception)
            {
            // Ignore errors normally, just keep trying
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "IOException reading data from serial input stream [exception=" + exception + "]" + currentstate.getLogEntry());
            }

        catch (NullPointerException exception)
            {
            // Ignore errors normally, just keep trying
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "NullPointerException reading data from non-existent serial input stream [exception=" + exception + "]" + currentstate.getLogEntry());
            }

        // If we found the STX, we can go on to try to accumulate a whole message up to EOT
        if (boolFoundSTX)
            {
            nextState = StaribusReceiveEventState.ACCUMULATE;
            }
        else
            {
            nextState = StaribusReceiveEventState.IDLE;
            }

        return (nextState);
        }


    /***********************************************************************************************
     * Process the state DRAIN_UNWANTED, return the next state.
     *
     * @param SOURCE
     * @param rxstream
     * @param currentstate
     * @param debugmode
     *
     * @return StaribusReceiveEventState
     */

    private static StaribusReceiveEventState stateDrainUnwanted(final String SOURCE,
                                                                final InputStream rxstream,
                                                                final StaribusReceiveEventState currentstate,
                                                                final boolean debugmode)
        {
        final StaribusReceiveEventState nextState;

        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + MSG_ENTERED_STATE + currentstate.getLogEntry());

        try
            {
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "WARNING! Waiting for PortController to process previous message, but received unwanted extra chars [available=" + rxstream.available() + "]" + currentstate.getLogEntry());
            // Discard extra chars?
            while (rxstream.available() > 0)
                {
                final int intUnwantedData;

                intUnwantedData = rxstream.read();
                LOGGER.debugStaribusEvent(debugmode,
                                          (SOURCE + "WARNING! Draining unwanted data [rx_char=" +  Utilities.byteToTwoHexString((byte) intUnwantedData) + "]"));
                }
            }

        catch (IOException exception)
            {
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "IOException reading data from serial input stream [exception=" + exception.getMessage() + "]" + currentstate.getLogEntry());
            }

        catch (NullPointerException exception)
            {
            LOGGER.debugStaribusEvent(debugmode,
                                      SOURCE + "NullPointerException reading data from non-existent serial input stream [exception=" + exception.getMessage() + "]" + currentstate.getLogEntry());
            }

        // Return to IDLE anyway
        nextState = StaribusReceiveEventState.IDLE;

        return (nextState);
        }


    /***********************************************************************************************
     *  Process the state COMMS_ERROR, return the next state.
     *
     * @param SOURCE
     * @param event
     * @param rxstream
     * @param currentstate
     * @param debugmode
     *
     * @return StaribusReceiveEventState
     */

    private static StaribusReceiveEventState stateCommsError(final String SOURCE,
                                                             final SerialPortEvent event,
                                                             final InputStream rxstream,
                                                             final StaribusReceiveEventState currentstate,
                                                             final boolean debugmode)
        {
        final StaribusReceiveEventState nextState;

        LOGGER.error(SOURCE + "ERROR Serial Event [eventtype=" + event.getEventType() + "]" + currentstate.getLogEntry());

        // Errors always return to IDLE
        nextState = StaribusReceiveEventState.IDLE;

        return (nextState);
        }


    /***********************************************************************************************
     * Try to find the Staribus terminator sequence EOT CR LF in the serial input buffer.
     * Discontinued in favour of looking for just the EOT.
     *
     * @param stream
     * @param buffer
     *
     * @return boolean
     */

    private static boolean findTerminatorSequence(final PortRxStreamInterface stream,
                                                  final StringBuffer buffer)
        {
        boolean boolFoundIt;

        // This is triggered three times
        // Assuming it is all working, we look for the sequence as the last three characters in the buffer
        // since a single character has just been added, and no more can come until we return
        // We expect to see EOT CR LF

        boolFoundIt = false;

        if ((buffer != null)
            && (buffer.length() > 3))
            {
            final byte[] bytes;

            LOGGER.debugStaribusEvent(stream.isDebugMode(),
                                      "StaribusRxStream.findTerminatorSequence() in [" + Utilities.byteArrayToSpacedHex(buffer.substring(buffer.length()-2).getBytes()) + "]");

            // Try just finding CR, without waiting for LF
            bytes = buffer.substring(buffer.length() - 2).getBytes();

            boolFoundIt = ((bytes[0] == STARIBUS_TERMINATOR_0)
                            && (bytes[1] == STARIBUS_TERMINATOR_1)
                            && (bytes[2] == STARIBUS_TERMINATOR_2));
            }

        if (boolFoundIt && stream.isDebugMode())
            {
            LOGGER.debugStaribusEvent(stream.isDebugMode(),
                                      "StaribusRxStream.findTerminatorSequence() FOUND TERMINATORS EOT CR (LF)");
            }

        return (boolFoundIt);
        }


    /***********************************************************************************************
     * Construct a StaribusRxStream.
     *
     * @param resourcekey
     */

    public StaribusRxStream(final String resourcekey)
        {
        super(resourcekey);

        this.readBuffer = new StringBuffer(BUFFER_CAPACITY);
        this.boolReadyToBeReadByPortController = false;
        this.intLengthBeforeTerminator = LENGTH_BEFORE_TERMINATOR;
        }


    /***********************************************************************************************
     * Get the Rx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.STARIBUS);
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
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusRxStream.initialise()");

        // Close the stream regardless of whether it is a Staribus Port
        if (getHostPort() != null)
            {
            if (getUnderlyingRxStream() != null)
                {
                getUnderlyingRxStream().close();
                }

            if (getStreamConfiguration() != null)
                {
                getStreamConfiguration().clear();
                }

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            this.readBuffer.setLength(0);
            setAssembledMessageWaitingToBeReadByPortController(false);
            }

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
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusRxStream.open()");

        readResources();

        // We rely on the StaribusTxStream to set up the serial port...

        // Clear the input buffer used by the serial port
        this.readBuffer.setLength(0);
        setAssembledMessageWaitingToBeReadByPortController(false);

        // Mark this Stream as Open, if successful
        this.boolStreamOpen = true;

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
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusRxStream.close()");

        // Do not close the port or streams if attached to the StaribusPort!
        // The Observatory will close it on shutdown
        if ((getHostPort() != null)
            && (!getHostPort().isStaribusPort()))
            {
            if (getUnderlyingRxStream() != null)
                {
                getUnderlyingRxStream().close();
                }

            if (getStreamConfiguration() != null)
                {
                getStreamConfiguration().clear();
                }

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            this.readBuffer.setLength(0);
            setAssembledMessageWaitingToBeReadByPortController(false);
            }
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusRxStream.reset()");

        try
            {
            int intRead;

            intRead = 0;

            while ((getUnderlyingRxStream() != null)
                && (getUnderlyingRxStream().available() > 0)
                && (intRead >= 0))
                {
                // Empty the stream completely
                intRead = getUnderlyingRxStream().read();
                }

            LOGGER.debugStaribusEvent(isDebugMode(),
                                      "StaribusRxStream.reset() Clearing underlying RxStream [read_count=" + intRead + "]");

            if (getUnderlyingRxStream() != null)
                {
                // This may throw IOException if mark or reset are not supported
                getUnderlyingRxStream().reset();
                }
            }

        catch (IOException exception)
            {
            LOGGER.debugStaribusEvent(isDebugMode(),
                                      "StaribusRxStream.reset() [exception=" + exception.getMessage() + "]");
            }

        this.readBuffer.setLength(0);
        setAssembledMessageWaitingToBeReadByPortController(false);
        }


    /***********************************************************************************************
     * Read a ResponseMessage from the stream, if possible.
     * The PortController will take the message and place it in the RxQueue for the host DaoPort.
     *
     * @param instrumentsdoc
     *
     * @return ResponseMessageInterface
     *
     * @throws IOException
     */

    public ResponseMessageInterface read(final InstrumentsDocument instrumentsdoc) throws IOException
        {
        ResponseMessageInterface response;

        // NULL indicates that there's nothing to read
        response = null;

        // There may be something in the input buffer?
        if ((readBuffer != null)
            && (readBuffer.length() > 0)
            && (isStreamOpen())
            && (isAssembledMessageWaitingToBeReadByPortController()))
            {
            LOGGER.debugStaribusEvent(isDebugMode(),
                                      "StaribusRxStream.read() Ready for parsing [bytes=" + Utilities.byteArrayToExpandedAscii(readBuffer.toString().getBytes()) + "]");

            // Now attempt to parse out a ResponseMessage from the incoming bytes
            // and find the DAO to which the response should be sent
            // A null Response means that the RxStream did not contain a valid message
            response = StaribusResponseMessage.parseStaribusBytes(instrumentsdoc,
                                                                  getHostPort().getHostDAOs(),
                                                                  readBuffer.toString().getBytes(),
                                                                  LOADER_PROPERTIES.isStaribusDebug());
            if (response == null)
                {
                // TODO Review sending non-null response to avoid pseudo-timeout status
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StaribusRxStream.read() Message parsing failed, pseudo-TIMEOUT behaviour (temporary solution pending design review)");
                }

            // Clear the receive data buffer, regardless of the outcome of parsing
            readBuffer.setLength(0);

            // Allow SerialEvents to gather data again
            setAssembledMessageWaitingToBeReadByPortController(false);
            }

        // This is just debug
        if ((isDebugMode())
            && (response != null)
            && (response.getCommandType() != null))
            {
            if (response.getCommandType().getResponse() != null)
                {
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StaribusRxStream.read() Non-NULL Response " + "[ResponseValue=" + response.getCommandType().getResponse().getValue() + "]");
                }
            else if (response.getCommandType().getAck() != null)
                {
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StaribusRxStream.read() Non-NULL Response [Ack]");
                }
            else
                {
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StaribusRxStream.read() This Command does not produce a Response or Ack");
                }
            }

        return (response);
        }


    /***********************************************************************************************
     * Indicate is the buffer is ready to be read by the PortController.
     *
     * @return boolean
     */

    protected boolean isAssembledMessageWaitingToBeReadByPortController()
        {
        return (this.boolReadyToBeReadByPortController);
        }


    /***********************************************************************************************
     * Set the buffer ready to read status.
     *
     * @param ready
     */

    protected void setAssembledMessageWaitingToBeReadByPortController(final boolean ready)
        {
        this.boolReadyToBeReadByPortController = ready;
        }


    /***********************************************************************************************
     * Get the Read  Buffer into which characters are placed from the serial Rx stream.
     *
     * @return StringBuffer
     */

    protected StringBuffer getReadBuffer()
        {
        return (this.readBuffer);
        }


    /***********************************************************************************************
     * Get the Length of message before the Terminator.
     * Changing this will allow use in loopback tests,
     * where the message is really the CommandMessage reflected round again.
     *
     * @return int
     */

    protected int getLengthBeforeTerminator()
        {
        return (this.intLengthBeforeTerminator);
        }


    /***********************************************************************************************
     * Set the Length of message before the Terminator.
     * Changing this will allow use in loopback tests,
     * where the message is really the CommandMessage reflected round again.
     *
     * @param length
     */

    protected void setLengthBeforeTerminator(final int length)
        {
        this.intLengthBeforeTerminator = length;
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public void readResources()
        {
//        boolEnableDebug = REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG);

        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusRxStream.readResources() [ResourceKey=" + getResourceKey() + "]");

        // Todo Range change to {0...1000}
//        this.intSerialEventDelay = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_SERIALEVENT_DELAY);
//
//        // Reload the Stream Configuration every time the Resources are read from the Registry
//        if (getStreamConfiguration() != null)
//            {
//            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
//                                                       PropertyPlugin.PROPERTY_ICON,
//                                                       getResourceKey() + KEY_PORT_SERIALEVENT_DELAY,
//                                                       Integer.toString(intSerialEventDelay));
//            }
//        else
//            {
//            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
//            }
        }


    /***********************************************************************************************
     * This is the event handler for SerialPortEventListener.
     * Received characters are placed into readBuffer.
     *
     * @param event
     */

    public final void serialEvent(final SerialPortEvent event)
        {
        final String SOURCE = "StaribusRxStream.serialEvent() ";
        StaribusReceiveEventState eventState;

        switch(event.getEventType())
            {
            case SerialPortEvent.DATA_AVAILABLE:
                {
                eventState = StaribusReceiveEventState.DATA_AVAILABLE;

                LOGGER.debugStaribusEvent(isDebugMode(),
                                          SOURCE + MSG_ENTERED_STATE + eventState.getLogEntry());

                // If the assembled message is ready for the PortController
                // and we receive serial data, then the characters are unwanted/unsolicited
                if (isAssembledMessageWaitingToBeReadByPortController())
                    {
                    eventState = stateDrainUnwanted(SOURCE,
                                                    getUnderlyingRxStream(),
                                                    StaribusReceiveEventState.DRAIN_UNWANTED,
                                                    isDebugMode());
                    }
                else
                    {
                    // The PortController could read another message if one were available,
                    // so attempt to assemble a message by first looking for STX
                    eventState = stateFindSTX(SOURCE,
                                              getUnderlyingRxStream(),
                                              StaribusReceiveEventState.FIND_STX,
                                              readBuffer,
                                              isDebugMode());

                    // If we didn't find STX, we will return to IDLE,
                    // and the PortController will not be notified
                    if (StaribusReceiveEventState.ACCUMULATE.equals(eventState))
                        {
                        int intRetryCount;

                        intRetryCount = MAX_RETRIES;

                        while ((intRetryCount > 0)
                            && (!StaribusReceiveEventState.DRAIN_TRAILING.equals(eventState)))
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "Start Accumulate Retry Loop [retry_count=" + (MAX_RETRIES-intRetryCount) + "] [MAX_RETRIES=" + MAX_RETRIES + "]");

                            eventState = stateAccumulate(SOURCE,
                                                         getUnderlyingRxStream(),
                                                         eventState,
                                                         readBuffer,
                                                         isDebugMode());

                            // Are we still in ACCUMULATE? If so, need to wait a while...
                            if (StaribusReceiveEventState.ACCUMULATE.equals(eventState))
                                {
                                eventState = stateWaitEOT(SOURCE,
                                                          getUnderlyingRxStream(),
                                                          eventState,
                                                          isDebugMode());
                                }

                            intRetryCount--;
                            }

                        // If we found EOT correctly, drain any trailing characters
                        if (StaribusReceiveEventState.DRAIN_TRAILING.equals(eventState))
                            {
                            eventState = stateDrainTrailing(SOURCE,
                                                            getUnderlyingRxStream(),
                                                            eventState,
                                                            isDebugMode());

                            // All other calls set this to false
                            setAssembledMessageWaitingToBeReadByPortController(true);
                            }
                        else
                            {
                            // Otherwise return to IDLE without notifying the PortController
                            eventState = StaribusReceiveEventState.IDLE;
                            }
                        }
                    }

                break;
                }

            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                {
                // A genuine comms error
                eventState = stateCommsError(SOURCE,
                                             event,
                                             getUnderlyingRxStream(),
                                             StaribusReceiveEventState.ERROR,
                                             isDebugMode());

                break;
                }

            default:
                {
                // Ignore unknown errors normally, just keep trying
                eventState = stateCommsError(SOURCE,
                                             event,
                                             getUnderlyingRxStream(),
                                             StaribusReceiveEventState.ERROR,
                                             isDebugMode());

                break;
                }
            }

        LOGGER.debugStaribusEvent(isDebugMode(),
                                  SOURCE + "Returning to State" + eventState.getLogEntry());
        }
    }
