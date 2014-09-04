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

import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractRxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/***************************************************************************************************
 * StubStaribusRxStream.
 */

public final class StubStaribusRxStream extends AbstractRxStream
                                        implements PortRxStreamInterface
    {
    private static final int QUEUE_SIZE = 1000;

    private BlockingQueue<ResponseMessageInterface> queueResponses;


    /***********************************************************************************************
     * Construct a StubStaribusRxStream.
     *
     * @param resourcekey
     */

    public StubStaribusRxStream(final String resourcekey)
        {
        super(resourcekey);

        this.queueResponses = new ArrayBlockingQueue<ResponseMessageInterface>(QUEUE_SIZE);
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
                                  "StubStaribusRxStream.initialise()");

        getResponses().clear();

        // Mark this Stream as Closed
        this.boolStreamOpen = false;

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
                                  "StubStaribusRxStream.open()");

        readResources();

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
                                  "StubStaribusRxStream.close()");

        getResponses().clear();

        // Mark this Stream as Closed
        this.boolStreamOpen = false;
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StubStaribusRxStream.reset()");

        this.queueResponses = new ArrayBlockingQueue<ResponseMessageInterface>(QUEUE_SIZE);
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
                //LOGGER.debugProtocolEvent("StubStaribusRxStream.read() Before [queuesize=" + getResponses().size() + "]");

                // take() blocks until the operation can succeed
                // Removes the head of the queue
                response = getResponses().take();

                //LOGGER.debugProtocolEvent("StubStaribusRxStream.read() After [queuesize=" + getResponses().size() + "]");

                if ((response != null)
                    && (response.getCommandType() != null))
                    {
                    if (response.getCommandType().getResponse() != null)
                        {
                        LOGGER.debugStaribusEvent(isDebugMode(),
                                                  "StubStaribusRxStream.read() [ResponseValue=" + response.getCommandType().getResponse().getValue() + "]");
                        }
                    else if (response.getCommandType().getAck() != null)
                        {
                        LOGGER.debugStaribusEvent(isDebugMode(),
                                                  "StubStaribusRxStream.read() [Ack]");
                        }
                    }
                }
            }

        catch (InterruptedException exception)
            {
            //LOGGER.debugProtocolEvent("StubStaribusRxStream.read() InterruptedException");
            exception.printStackTrace();

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
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StubStaribusRxStream.putResponseToStream() Before [queuesize=" + getResponses().size() + "]");

                if (response.getCommandType() != null)
                    {
                    if (response.getCommandType().getResponse() != null)
                        {
                        LOGGER.debugStaribusEvent(isDebugMode(),
                                                  "StubStaribusRxStream.putResponseToStream() [ResponseValue=" + response.getCommandType().getResponse().getValue() + "]");
                        }
                    else if (response.getCommandType().getAck() != null)
                        {
                        LOGGER.debugStaribusEvent(isDebugMode(),
                                                  "StubStaribusRxStream.putResponseToStream() [Ack]");
                        }
                    }

                // put() blocks until the operation can succeed
                // Inserts the item into the queue
                getResponses().put(response);

                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StubStaribusRxStream.putResponseToStream() After [queuesize=" + getResponses().size() + "]");
                }
            else
                {
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StubStaribusRxStream.putResponseToStream() Unable to put Response to stream");
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
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StubStaribusRxStream.readResources() [ResourceKey=" + getResourceKey() + "]");
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
