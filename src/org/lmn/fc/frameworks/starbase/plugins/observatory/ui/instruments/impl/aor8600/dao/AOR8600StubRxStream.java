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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.aor8600.dao;

import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractRxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/***************************************************************************************************
 * AOR8600StubRxStream.
 */

public final class AOR8600StubRxStream extends AbstractRxStream
                                       implements PortRxStreamInterface
    {
    private final BlockingQueue<ResponseMessageInterface> queueResponses;


    /***********************************************************************************************
     * Construct a AOR8600StubRxStream.
     *
     * @param resourcekey
     */

    public AOR8600StubRxStream(final String resourcekey)
        {
        super(resourcekey);

        this.queueResponses = new ArrayBlockingQueue<ResponseMessageInterface>(1000);
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
                               "AOR8600StubRxStream.open()");

        // Mark this Stream as Open, if successful
        this.boolStreamOpen = true;

        return (true);
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
                               "AOR8600StubRxStream.close()");

        getResponses().clear();

        // Mark this Stream as Closed
        this.boolStreamOpen = false;
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubRxStream.reset()");

        getResponses().clear();
        }


    /***********************************************************************************************
     * Read a ResponseMessage from the stream, if possible.
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

        try
            {
            if ((getResponses() != null)
                && (!getResponses().isEmpty()))
                {
                response = getResponses().take();
                }
            }

        catch (InterruptedException exception)
            {
            Thread.currentThread().interrupt();
            throw new IOException(exception);
            }

        return (response);
        }


    /***********************************************************************************************
     * Put a Response on to the stream.
     * This is not in the interface; it is just for testing!
     *
     * @param response
     *
     * @throws IOException
     */

    public final void putResponseToStream(final ResponseMessageInterface response) throws IOException
        {
        try
            {
            if ((getResponses() != null)
                && (response != null))
                {
                getResponses().put(response);
                }
            }

        catch (InterruptedException exception)
            {
            Thread.currentThread().interrupt();
            throw new IOException(exception);
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubRxStream.readResources() [ResourceKey=" + getResourceKey() + "]");
        }


    /***********************************************************************************************
     * Get the list of Responses in the stream.
     *
     * @return BlockingQueue<ResponseMessageInterface>
     */

    private BlockingQueue<ResponseMessageInterface> getResponses()
        {
        return (this.queueResponses);
        }
    }
