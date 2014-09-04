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

import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;


/***************************************************************************************************
 * AbstractTxStream.
 */

public abstract class AbstractTxStream implements PortTxStreamInterface
    {
    private DaoPortInterface parentDaoPort;
    protected boolean boolStreamOpen;
    private OutputStream underlyingStream;
    private PortRxStreamInterface loopbackStream;
    private final String strResourceKey;
    private final Vector<Vector> vecStreamConfiguration;
    protected boolean boolEnableDebug;


    /***********************************************************************************************
     * Construct an AbstractTxStream.
     *
     * @param resourcekey
     */

    public AbstractTxStream(final String resourcekey)
        {
        this.parentDaoPort = null;
        this.boolStreamOpen = false;
        this.underlyingStream = null;
        this.loopbackStream = null;
        this.strResourceKey = resourcekey;

        this.boolEnableDebug = false;

        this.vecStreamConfiguration = new Vector<Vector>(10);
        }


    /***********************************************************************************************
     * Get the host DaoPort for this TxStream.
     *
     * @return DaoPortInterface
     */

    public DaoPortInterface getHostPort()
        {
        return (this.parentDaoPort);
        }


    /***********************************************************************************************
     * Set the host DaoPort for this TxStream.
     *
     * @param hostport
     */

    public void setHostPort(final DaoPortInterface hostport)
        {
        this.parentDaoPort = hostport;
        }


    /***********************************************************************************************
     * Get the Tx StreamType.
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
     * Closes this stream regardless of its type, and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    public void forceClose() throws IOException
        {
        close();
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public abstract void reset();


    /***********************************************************************************************
     * Flushes this stream by writing any buffered output to the underlying stream.
     *
     * @throws IOException If an I/O error occurs
     */

    public abstract void flush() throws IOException;


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     *
     * @param instrumentsdoc
     * @param commandmessage
     *
     * @throws IOException
     */

    public abstract void write(InstrumentsDocument instrumentsdoc,
                               CommandMessageInterface commandmessage) throws IOException;


    /***********************************************************************************************
     * Get the Underlying TxStream.
     *
     * @return InputStream
     */

    public synchronized final OutputStream getUnderlyingTxStream()
        {
        return (this.underlyingStream);
        }


    /***********************************************************************************************
     * Set the Underlying TxStream.
     *
     * @param underlyingstream
     */

    public synchronized final void setUnderlyingTxStream(final OutputStream underlyingstream)
        {
        this.underlyingStream = underlyingstream;
        }


    /***********************************************************************************************
     * Get the Loopback RxStream.
     *
     * @return PortRxStreamInterface
     */

    public synchronized final PortRxStreamInterface getLoopbackRxStream()
        {
        return (this.loopbackStream);
        }


    /***********************************************************************************************
     * Set the Loopback RxStream.
     *
     * @param stream
     */

    public synchronized final void setLoopbackRxStream(final PortRxStreamInterface stream)
        {
        this.loopbackStream = stream;
        }


    /***********************************************************************************************
     * Get the Vector of extra data to append to a Report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getStreamConfiguration()
        {
        return (this.vecStreamConfiguration);
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
