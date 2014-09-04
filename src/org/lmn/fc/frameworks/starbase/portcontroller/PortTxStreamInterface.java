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

package org.lmn.fc.frameworks.starbase.portcontroller;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;


/***************************************************************************************************
 * PortTxStreamInterface.
 */

public interface PortTxStreamInterface extends Closeable,
                                               Flushable,
                                               FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ResourceKeys,
                                               ObservatoryConstants
    {
    int DATATYPE_XML_BLOCK_SIZE = 512;


    /***********************************************************************************************
     * Get the host DaoPort for this TxStream.
     *
     * @return DaoPortInterface
     */

    DaoPortInterface getHostPort();


    /***********************************************************************************************
     * Set the host DaoPort for this TxStream.
     *
     * @param parentport
     */

    void setHostPort(DaoPortInterface parentport);


    /***********************************************************************************************
     * Get the Tx StreamType.
     *
     * @return StreamType
     */

    StreamType getStreamType();


    /***********************************************************************************************
     * Initialise the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    boolean initialise() throws IOException;


    /***********************************************************************************************
     * Open the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    boolean open() throws IOException;


    /***********************************************************************************************
     * Indicate if this Stream is Open.
     *
     * @return boolean
     */

    boolean isStreamOpen();


    /***********************************************************************************************
     * Closes this stream regardless of its type, and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    void forceClose() throws IOException;


    /***********************************************************************************************
     * Reset the Stream.
     */

    void reset();


    /***********************************************************************************************
     * Write a CommandMessage to the Stream.
     *
     * @param instruments
     * @param command
     *
     * @throws IOException
     */

    void write(InstrumentsDocument instruments,
               CommandMessageInterface command) throws IOException;


    /***********************************************************************************************
     * Get the Underlying TxStream.
     *
     * @return InputStream
     */

    OutputStream getUnderlyingTxStream();


    /***********************************************************************************************
     * Set the Underlying TxStream.
     *
     * @param stream
     */

    void setUnderlyingTxStream(OutputStream stream);


    /***********************************************************************************************
     * Get the Loopback RxStream.
     *
     * @return PortRxStreamInterface
     */

    PortRxStreamInterface getLoopbackRxStream();


    /***********************************************************************************************
     * Set the Loopback RxStream.
     *
     * @param stream
     */

    void setLoopbackRxStream(PortRxStreamInterface stream);


    /***********************************************************************************************
     * Get the Vector of extra configuration data to append to a Report.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getStreamConfiguration();


    /***********************************************************************************************
     * Indicate if the Stream is in debug mode.
     *
     * @return boolean
     */

    boolean isDebugMode();


    /***********************************************************************************************
     * Get the ResourceKey for the Stream.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    void readResources();
    }
