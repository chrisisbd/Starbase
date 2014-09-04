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

import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractRxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;


/***************************************************************************************************
 * StarinetUdpRxStream.
 */

public final class StarinetUdpRxStream extends AbstractRxStream
                                       implements PortRxStreamInterface
    {
    /***********************************************************************************************
     * Construct a StarinetUdpRxStream.
     *
     * @param resourcekey
     */

    public StarinetUdpRxStream(final String resourcekey)
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
        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  "StarinetUdpRxStream.open()");

        readResources();

        // We rely on the StarinetUdpTxStream to set things up...
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
        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  "StarinetUdpRxStream.close()");

        // Do not close the port or streams if attached to the StaribusPort!
        // This should of course never happen with Starinet
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
        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  "StarinetUdpRxStream.reset()");
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
        final String SOURCE = "StarinetUdpRxStream.read() ";
        ResponseMessageInterface response;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        response = null;

        // There may be something in the input stream?
        if ((getUnderlyingRxStream() != null)
            && (getUnderlyingRxStream().available() > 0)
            && (isStreamOpen()))
            {
            final StringBuffer readBuffer;

            readBuffer = new StringBuffer();

            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "RxStream [available=" + getUnderlyingRxStream().available() + "]");

            // Returns an estimate of the number of bytes that can be read (or skipped over)
            // from this input stream without blocking or 0 when it reaches the end of the input stream.
            while (getUnderlyingRxStream().available() > 0)
                {
                final int intData;

                intData = getUnderlyingRxStream().read();

                // There are some bytes available, so accumulate them
                readBuffer.append((char)intData);
                }

            if (readBuffer.length() > 0)
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "Received [bytes=" + Utilities.byteArrayToExpandedAscii(readBuffer.toString().getBytes()) + "]");

                // Now attempt to parse out a ResponseMessage from the incoming bytes
                // and find the DAO to which the response should be sent
                // A null Response means that there's nothing in the RxStream
                // Any logging via the CommandMessage will be added in the DAO
                response = StarinetResponseMessage.parseStarinetBytes(getCommandContext(),
                                                                      getHostPort().getHostDAOs(),
                                                                      readBuffer.toString().getBytes(),
                                                                      LOADER_PROPERTIES.isStarinetDebug());
                if (response == null)
                    {
                    // TODO Review sending non-null response to avoid pseudo-timeout status
                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "Message parsing failed, pseudo-TIMEOUT behaviour");
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
        LOGGER.debugStarinetEvent(LOADER_PROPERTIES.isStarinetDebug(),
                                  "StarinetUdpRxStream.readResources() [ResourceKey=" + getResourceKey() + "]");
        }
    }
