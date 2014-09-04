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

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.spectracyber.dao.SpectraCyberResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractRxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;


/***************************************************************************************************
 * SpectraCyberServerRxStream.
 */

public final class SpectraCyberServerRxStream extends AbstractRxStream
                                              implements PortRxStreamInterface,
                                                         SerialPortEventListener
    {
    private static final int BUFFER_CAPACITY = 10000;
    private static final int RETRY_WAIT_MILLIS = 10;
    private static final int RETRY_COUNT = 5;

    private final StringBuffer readBuffer;
    private boolean boolReadyToRead;


    /***********************************************************************************************
     * Construct a SpectraCyberServerRxStream.
     *
     * @param resourcekey
     */

    public SpectraCyberServerRxStream(final String resourcekey)
        {
        super(resourcekey);

        this.readBuffer = new StringBuffer(BUFFER_CAPACITY);
        this.boolReadyToRead = false;
        }


    /***********************************************************************************************
     * Get the Rx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.SERIAL);
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
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerRxStream.open()");

        readResources();

        // We rely on the SpectraCyberServerTxStream to set things up...

        // Clear the input buffer used by the serial port
        this.readBuffer.setLength(0);
        setReadyToRead(false);

        setCommandContext(null);

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
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerRxStream.close()");

        // Do not close the port or streams if attached to the StaribusPort!
        if (!getHostPort().isStaribusPort())
            {
            if (getUnderlyingRxStream() != null)
                {
                getUnderlyingRxStream().close();
                }

            if (getStreamConfiguration() != null)
                {
                getStreamConfiguration().clear();
                }

            setCommandContext(null);

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            this.readBuffer.setLength(0);
            setReadyToRead(false);
            }
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerRxStream.reset()");
        }


    /***********************************************************************************************
     * Read a ResponseMessage from the stream, if possible.
     * The PortController will take the message and place it in the RxQueue for the host DaoPort.
     * There are only three SpectraCyber commands which produce a Response!
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

        // A null Response means that there's nothing in the RxStream
        response = null;

        // There may be something in the input buffer?
        // If we never found enough characters, the PortController will time out,
        // because ReadyToRead will never be set...
        if ((readBuffer != null)
            && (readBuffer.length() > 0)
            && (getHostPort() != null)
            && (getCommandContext() != null)
            && (isStreamOpen())
            && (isReadyToRead()))
            {
            //System.out.println("SpectraCyberServerRxStream.read() [bytes=" + Utilities.byteArrayToSpacedHex(readBuffer.toString().getBytes()) + "]");
            //LOGGER.debugTimedEvent("SpectraCyberServerRxStream.read() [bytes=" + Utilities.byteArrayToSpacedHex(readBuffer.toString().getBytes()) + "]");
            //LOGGER.debugProtocolEvent("SpectraCyberServerRxStream.read() [bytes=" + Utilities.byteArrayToExpandedAscii(readBuffer.toString().getBytes()) + "]");

            // Now use the Command context injected by the TxStream
            // and the bytes received from the Port, to construct a ResponseMessage
            response = new SpectraCyberServerResponseMessage(getHostPort().getName(),
                                                             getCommandContext().getInstrument(),
                                                             getCommandContext().getModule(),
                                                             getCommandContext().getCommandType(),
                                                             getCommandContext().getStarScript(),
                                                             readBuffer.toString().getBytes(),
                                                             ResponseMessageStatus.SUCCESS.getBitMask());

            // Clear the data buffer, regardless of the outcome
            readBuffer.setLength(0);

            // Allow SerialEvents to gather data again
            setReadyToRead(false);
            }

        return (response);
        }


    /***********************************************************************************************
     * Indicate is the buffer is ready to be read by the PortController.
     *
     * @return boolean
     */

    private boolean isReadyToRead()
        {
        return (this.boolReadyToRead);
        }


    /***********************************************************************************************
     * Set the buffer ready to read status.
     *
     * @param ready
     */

    private void setReadyToRead(final boolean ready)
        {
        //LOGGER.debugTimedEvent("SpectraCyberServerRxStream.setReadyToRead() [ready=" + ready + "]");
        this.boolReadyToRead = ready;
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberServerRxStream.readResources() [ResourceKey=" + getResourceKey() + "]");
        }


    /***********************************************************************************************
     * This is the event handler for SerialPortEventListener.
     *
     * @param event
     */

    public final void serialEvent(final SerialPortEvent event)
        {
        switch(event.getEventType())
            {
            case SerialPortEvent.DATA_AVAILABLE:
                {
                //LOGGER.debugProtocolEvent("SpectraCyberServerRxStream.serialEvent() SerialPortEvent.DATA_AVAILABLE");

                // Data are available, so read from the underlying InputStream and place in a buffer
                // We can only gather data if NOT ReadyToRead,
                // otherwise we are waiting for the PortController to read and parse the buffer
                if (!isReadyToRead())
                    {
                    try
                        {
                        int intRetries;
                        boolean boolFoundEnough;

                        //LOGGER.debugTimedEvent("SpectraCyberServerRxStream.serialEvent() ENTER SERIAL WAIT LOOP [length=" + readBuffer.length() + "]");

                        intRetries = RETRY_COUNT;
                        boolFoundEnough = false;

                        //LOGGER.debugTimedEvent("SpectraCyberServerRxStream.serialEvent() [retries=" + intRetries + "]");

                        while ((intRetries > 0)
                            && (!boolFoundEnough)
                            && (getUnderlyingRxStream() != null))
                            {
                            // Returns an estimate of the number of bytes that can be read (or skipped over)
                            // from this input stream without blocking or 0 when it reaches the end of the input stream.
                            while ((getUnderlyingRxStream().available() > 0)
                                && (!boolFoundEnough))
                                {
                                final int intData;

                                intData = getUnderlyingRxStream().read();

                                // There are some bytes available, so accumulate them
                                readBuffer.append((char)intData);

                                // All SpectraCyber Responses are 4 characters in length plus <cr>
                                boolFoundEnough = (readBuffer.length() == SpectraCyberResponseMessage.LENGTH_VALID_RESPONSE);

                                // Leave all loops once we have enough characters
                                setReadyToRead(boolFoundEnough);
                                }

                            // We either ran out of characters, or we found enough data
                            // If we reached the end of the stream,
                            // wait a little while in case more data are coming
                            if (!boolFoundEnough)
                                {
                                //LOGGER.debugTimedEvent("sleep ... buffer=[" + Utilities.byteArrayToSpacedHex(readBuffer.toString().getBytes()) + "]");
                                Utilities.safeSleep(RETRY_WAIT_MILLIS);
                                }

                            intRetries--;
                            }

                        // If we never found enough characters, the PortController will time out,
                        // because ReadyToRead will never be set...

                        //LOGGER.debugTimedEvent("SpectraCyberServerRxStream.serialEvent() LEAVE SERIAL WAIT LOOP [length=" + readBuffer.length() + "]");
//                        if (boolFoundEnough)
//                            {
//                            LOGGER.debugProtocolEvent("SpectraCyberServerRxStream.serialEvent() Data assembled, ready to be read");
//                            }
                        }

                    catch (IOException exception)
                        {
                        // Ignore errors normally, just keep trying
                        LOGGER.error("SpectraCyberServerRxStream.serialEvent() ERROR IOException reading data from input stream");
                        }
                    }
                else
                    {
                    // This should never happen...
                    //LOGGER.error("SpectraCyberServerRxStream.serialEvent() ERROR Waiting for PortController");

                    // TODO Review discarding extra chars?
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
                LOGGER.error("SpectraCyberServerRxStream.serialEvent() ERROR Serial Event type [eventtype=" + event.getEventType() + "]");
                break;
                }

            default:
                {
                // Ignore unknown errors normally, just keep trying
                LOGGER.error("SpectraCyberServerRxStream.serialEvent() ERROR Unknown Serial Event type [eventtype=" + event.getEventType() + "]");
                break;
                }
            }
        }
    }
