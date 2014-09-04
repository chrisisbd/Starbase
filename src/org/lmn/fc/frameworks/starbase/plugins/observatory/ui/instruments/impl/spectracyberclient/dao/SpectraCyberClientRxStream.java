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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.spectracyberclient.dao;

import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractRxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;


/***************************************************************************************************
 * SpectraCyberClientRxStream.
 */

public final class SpectraCyberClientRxStream extends AbstractRxStream
                                              implements PortRxStreamInterface
    {
    /***********************************************************************************************
     * Construct a SpectraCyberClientRxStream.
     *
     * @param resourcekey
     */

    public SpectraCyberClientRxStream(final String resourcekey)
        {
        super(resourcekey);
        }


    /***********************************************************************************************
     * Get the Rx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.ETHERNET);
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
        //LOGGER.debugTimedEvent("SpectraCyberClientRxStream.open()");

        readResources();

        // We rely on the SpectraCyberClientTxStream to set things up...
        // This stream should never be used...
        setUnderlyingRxStream(null);

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
        //LOGGER.debugTimedEvent("SpectraCyberClientRxStream.close()");

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

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            }
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        //LOGGER.debugTimedEvent("SpectraCyberClientRxStream.reset()");
        }


    /***********************************************************************************************
     * Read a ResponseMessage from the stream, if possible.
     * The PortController will take the message and place it in the RxQueue for the host DaoPort.
     * A null Response means that there's nothing going to appear in the RxStream.
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

        response = null;

        // There may be something in the input stream?
        if ((getUnderlyingRxStream() != null)
            && (getUnderlyingRxStream().available() > 0)
            && (isStreamOpen()))
            {
            final StringBuffer readBuffer;

            readBuffer = new StringBuffer();

            // Returns an estimate of the number of bytes that can be read (or skipped over)
            // from this input stream without blocking or 0 when it reaches the end of the input stream.
            while (getUnderlyingRxStream().available() > 0)
                {
                final int intData;

                intData = getUnderlyingRxStream().read();

                // There are some bytes available, so accumulate them
                readBuffer.append((char)intData);
                }

            // Did we get any data?
            if (readBuffer.length() > 0)
                {
                //LOGGER.debugProtocolEvent("SpectraCyberClientRxStream.read() [buffer=" + readBuffer.toString() + "]");
                //System.out.println("SpectraCyberClientRxStream.read() [bytes=" + Utilities.byteArrayToSpacedHex(readBuffer.toString().getBytes()) + "]");
                //System.out.println("SpectraCyberClientRxStream.read() [bytes=" + Utilities.byteArrayToExpandedAscii(readBuffer.toString().getBytes()) + "]");

                // Now attempt to parse out a ResponseMessage from the incoming bytes
                // and find the DAO to which the response should be sent
                // A null Response means that there's nothing in the RxStream
                // Any logging via the CommandMessage will be added in the DAO

                if (readBuffer.toString().contains(ResponseMessageStatus.SUCCESS.getMnemonic()))
                    {
                    // Make response msg for success, no response
                    //System.out.println("SpectraCyberClientRxStream.read() SUCCESS returned - No response was expected, just return Ok");
                    getCommandContext().getCommandType().getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                    response = new SpectraCyberClientResponseMessage(getHostPort().getName(),
                                                                     getCommandContext().getInstrument(),
                                                                     getCommandContext().getModule(),
                                                                     getCommandContext().getCommandType(),
                                                                     getCommandContext().getStarScript(),
                                                                     ResponseMessageStatus.SUCCESS.getBitMask());
                    }
                else if (readBuffer.toString().contains(ResponseMessageStatus.TIMEOUT.getMnemonic()))
                    {
                    // Timeouts are always indicated by NULL
                    //System.out.println("SpectraCyberClientRxStream.read()  TIMEOUT returned by server");
                    response = null;
                    }
                else if (readBuffer.toString().contains(RESPONSE_LOCAL))
                    {
                    // Make response msg for LOCAL
                    //System.out.println("SpectraCyberClientRxStream.read() LOCAL command was executed, return Ok");
                    getCommandContext().getCommandType().getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                    response = new SpectraCyberClientResponseMessage(getHostPort().getName(),
                                                                     getCommandContext().getInstrument(),
                                                                     getCommandContext().getModule(),
                                                                     getCommandContext().getCommandType(),
                                                                     getCommandContext().getStarScript(),
                                                                     ResponseMessageStatus.SUCCESS.getBitMask());
                    }
                else
                    {
                    // This is a bit of a cheat, but it is used where the Received message does not contain any
                    // context from which to derive Instrument, Module and Command.

                    //System.out.println("SpectraCyberClientRxStream.read() Good SpectraCyber RS232 response, return bytes");
                    response = SpectraCyberClientResponseMessage.parseSpectraCyberBytes(getCommandContext(),
                                                                            getHostPort().getHostDAOs(),
                                                                            readBuffer.toString().getBytes());
                    }

                if (response == null)
                    {
                    LOGGER.debugCommandEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "SpectraCyberClientRxStream.read() Message parsing failed, or TIMEOUT");
                    }
                else
                    {
                    //System.out.println("RxStream response NOT NULL");
                    }
                }
            }

        return (response);
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberClientRxStream.readResources() [ResourceKey=" + getResourceKey() + "]");
        }
    }
