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
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


/***************************************************************************************************
 * PortRxStreamInterface.
 */

public interface PortRxStreamInterface extends Closeable,
                                               FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ResourceKeys,
                                               ObservatoryConstants
    {
    String MSG_ENTERED_STATE = "Entered State";


    /***********************************************************************************************
     * Get the host DaoPort for this RxStream.
     *
     * @return DaoPortInterface
     */

    DaoPortInterface getHostPort();


    /***********************************************************************************************
     * Set the host DaoPort for this RxStream.
     *
     * @param parentport
     */

    void setHostPort(DaoPortInterface parentport);


    /***********************************************************************************************
     * Get the Rx StreamType.
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
     * Reset the Stream.
     */

    void reset();


    /***********************************************************************************************
     * Read a ResponseMessage from the Stream.
     *
     * @param instrumentsdoc
     *
     * @return ResponseMessageInterface
     *
     * @throws IOException
     */

    ResponseMessageInterface read(InstrumentsDocument instrumentsdoc) throws IOException;


    /***********************************************************************************************
     * Get the Underlying RxStream.
     *
     * @return InputStream
     */

    InputStream getUnderlyingRxStream();


    /***********************************************************************************************
     * Set the Underlying RxStream.
     *
     * @param stream
     */

    void setUnderlyingRxStream(InputStream stream);


    /***********************************************************************************************
     * Get the Loopback TxStream.
     *
     * @return PortTxStreamInterface
     */

    PortTxStreamInterface getLoopbackTxStream();


    /***********************************************************************************************
     * Set the Loopback TxStream.
     *
     * @param stream
     */

    void setLoopbackTxStream(PortTxStreamInterface stream);


    /***********************************************************************************************
     * Get the Vector of extra configuration data to append to a Report.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getStreamConfiguration();


    /***********************************************************************************************
     * Get the current CommandMessage context.
     * This is a bit of a cheat, but it is used where the Received message does not contain any
     * context from which to derive Instrument, Module and Command.
     *
     * @return CommandMessageInterface
     */

    CommandMessageInterface getCommandContext();


    /***********************************************************************************************
     * Set the current CommandMessage context.
     * This is a bit of a cheat, but it is used where the Received message does not contain any
     * context from which to derive Instrument, Module and Command.
     *
     * @param message
     */

    void setCommandContext(final CommandMessageInterface message);


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
