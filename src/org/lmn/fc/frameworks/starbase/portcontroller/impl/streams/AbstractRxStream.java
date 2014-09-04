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

package org.lmn.fc.frameworks.starbase.portcontroller.impl.streams;

import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


/***********************************************************************************************
 * AbstractRxStream.
 */

public abstract class AbstractRxStream implements PortRxStreamInterface
    {
    private DaoPortInterface parentDaoPort;
    protected boolean boolStreamOpen;
    private InputStream underlyingStream;
    private PortTxStreamInterface loopbackStream;
    private final String strResourceKey;
    private final Vector<Vector> vecStreamConfiguration;
    private CommandMessageInterface commandMessage;
    protected boolean boolEnableDebug;


    /***********************************************************************************************
     * Construct an AbstractRxStream.
     *
     * @param resourcekey
     */
    public AbstractRxStream(final String resourcekey)
        {
        this.parentDaoPort = null;
        this.boolStreamOpen = false;
        this.underlyingStream = null;
        this.loopbackStream = null;
        this.strResourceKey = resourcekey;

        this.boolEnableDebug = false;

        this.vecStreamConfiguration = new Vector<Vector>(10);

        this.commandMessage = null;
        }


    /***********************************************************************************************
     * Get the host DaoPort for this RxStream.
     *
     * @return DaoPortInterface
     */

    public DaoPortInterface getHostPort()
        {
        return (this.parentDaoPort);
        }


    /***********************************************************************************************
     * Set the host DaoPort for this RxStream.
     *
     * @param hostport
     */

    public void setHostPort(final DaoPortInterface hostport)
        {
        this.parentDaoPort = hostport;
        }


    /***********************************************************************************************
     * Get the Rx StreamType.
     *
     * @return StreamType
     */

    public abstract StreamType getStreamType();


    /***********************************************************************************************
     * Initialise the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public abstract boolean initialise() throws IOException;


    /***********************************************************************************************
     * Open the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public abstract boolean open() throws IOException;


    /***********************************************************************************************
     * Indicate if this Stream is Open.
     *
     * @return boolean
     */

    public boolean isStreamOpen()
        {
        return (this.boolStreamOpen);
        }


    /***********************************************************************************************
     * Closes this stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    public abstract void close() throws IOException;


    /***********************************************************************************************
     * Reset the Stream.
     */

    public abstract void reset();


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

    public abstract ResponseMessageInterface read(InstrumentsDocument instrumentsdoc) throws IOException;


    /***********************************************************************************************
     * Get the Port Loopback Underlying Stream.
     *
     * @return OutputStream
     */

    public synchronized final InputStream getUnderlyingRxStream()
        {
        return (this.underlyingStream);
        }


    /***********************************************************************************************
     * Set the Port Loopback Underlying Stream.
     *
     * @param underlyingstream
     */

    public synchronized final void setUnderlyingRxStream(final InputStream underlyingstream)
        {
        this.underlyingStream = underlyingstream;
        }


    /***********************************************************************************************
     * Get the Loopback TxStream.
     *
     * @return PortTxStreamInterface
     */

    public synchronized final PortTxStreamInterface getLoopbackTxStream()
        {
        return (this.loopbackStream);
        }


    /***********************************************************************************************
     * Set the Loopback TxStream.
     *
     * @param portstream
     */

    public synchronized final void setLoopbackTxStream(final PortTxStreamInterface portstream)
        {
        this.loopbackStream = portstream;
        }


    /***********************************************************************************************
     * Get the Vector of extra data to append to a Report.
     *
     * @return Vector<Vector>
     */

    public final Vector<Vector> getStreamConfiguration()
        {
        return (this.vecStreamConfiguration);
        }


    /***********************************************************************************************
     * Get the current CommandMessage context.
     * This is a bit of a cheat, but it is used where the Received message does not contain any
     * context from which to derive Instrument, Module and Command.
     *
     * @return CommandMessageInterface
     */

    public synchronized CommandMessageInterface getCommandContext()
        {
        return (this.commandMessage);
        }


    /***********************************************************************************************
     * Set the current CommandMessage context.
     * This is a bit of a cheat, but it is used where the Received message does not contain any
     * context from which to derive Instrument, Module and Command.
     *
     * @param message
     */

    public synchronized void setCommandContext(final CommandMessageInterface message)
        {
        this.commandMessage = message;
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Stream.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Indicate if the Stream is in debug mode.
     *
     * @return boolean
     */

    public boolean isDebugMode()
        {
        return (LOADER_PROPERTIES.isStaribusDebug());
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public abstract void readResources();
    }
