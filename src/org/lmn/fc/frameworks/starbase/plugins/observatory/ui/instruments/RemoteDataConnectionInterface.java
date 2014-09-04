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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;

import java.util.Vector;


/***************************************************************************************************
 * RemoteDataConnectionInterface.
 */

public interface RemoteDataConnectionInterface extends FrameworkConstants,
                                                       FrameworkStrings,
                                                       FrameworkMetadata,
                                                       FrameworkSingletons,
                                                       ObservatoryConstants
    {
    // String Resources
    String TRANSFER_MODE_BINARY = "binary";
    String TRANSFER_MODE_ASCII = "ascii";
    String CONNECTION_MODE_PASSIVE = "pasv";
    String CONNECTION_MODE_ACTIVE = "active";


    /**********************************************************************************************/
    /* Call these methods in this sequence:
     *
     *  initialise()
     *  login()
     *  transmit()  |  receive()  |  receiveBytesIncrementally()
     *  logout()
     *  dispose()
     *
    /***********************************************************************************************
     * Initialise the Connection to the RemoteData server.
     * Return true if successful.
     *
     * @param timeoutmillis
     * @param verbose
     *
     * @return boolean
     */

    boolean initialise(int timeoutmillis,
                       boolean verbose);


    /***********************************************************************************************
     * Login to the Remote Data server.
     * Return true if successful.
     *
     * @return boolean
     */

    boolean login();


    /***********************************************************************************************
     * Transmit data to the Remote Data server.
     * Return true if successful.
     *
     * @return boolean
     */

    boolean transmit();


    /***********************************************************************************************
     * Receive data from the Remote Data server.
     * Return true if successful.
     *
     * @return boolean
     */

    boolean receive();


    /***********************************************************************************************
     * Receive data incrementally from the Remote Data server, as an array of bytes.
     * Transfers data from the IncrementalRemoteFilename, starting at the current IncrementalIndex.
     *
     * @return boolean
     */

    byte[] receiveBytesIncrementally();


    /***********************************************************************************************
     * Logout from the Remote Data server.
     * Return true if successful.
     *
     * @return boolean
     */

    boolean logout();


    /***********************************************************************************************
     * Dispose the Connection to the RemoteData server.
     * Return true if successful.
     *
     * @return boolean
     */

    boolean dispose();


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the Hostname of the Remote Data server.
     *
     * @return String
     */

    String getHostname();


    /***********************************************************************************************
     * Set the Hostname of the Remote Data server.
     *
     * @param hostname
     */

    void setHostname(String hostname);


    /***********************************************************************************************
     * Get the Username to login to the Remote Data server.
     *
     * @return String
     */

    String getUsername();


    /***********************************************************************************************
     * Set the Username to login to the Remote Data server.
     *
     * @param username
     */

    void setUsername(String username);


    /***********************************************************************************************
     * Get the Password to login to the Remote Data server.
     *
     * @return String
     */

    String getPassword();


    /***********************************************************************************************
     * Set the Password to login to the Remote Data server.
     *
     * @param password
     */

    void setPassword(String password);


    /***********************************************************************************************
     * Get the TransferMode.
     *
     * @return String
     */

    String getTransferMode();


    /***********************************************************************************************
     * Set the TransferMode.
     *
     * @param mode
     */

    void setTransferMode(String mode);


    /***********************************************************************************************
     * Get the ConnectionMode.
     *
     * @return String
     */

    String getConnectionMode();


    /***********************************************************************************************
     * Set the ConnectionMode.
     *
     * @param mode
     */

    void setConnectionMode(String mode);


    /***********************************************************************************************
     * Get the Connection timeout in milliseconds.
     *
     * @return int
     */

    int getTimeoutMillis();


    /***********************************************************************************************
     * Set the Connection timeout in milliseconds.
     *
     * @param timeoutmillis
     *
     * @return boolean
     */

    boolean setTimeoutMillis(int timeoutmillis);


    /***********************************************************************************************
     * Get the LocalDirectory.
     *
     * @return String
     */

    String getLocalDirectory();


    /***********************************************************************************************
     * Set the LocalDirectory.
     *
     * @param directory
     */

    void setLocalDirectory(String directory);


    /***********************************************************************************************
     * Get the LocalFilename.
     *
     * @return String
     */

    String getLocalFilename();


    /***********************************************************************************************
     * Set the LocalFilename.
     *
     * @param filename
     */

    void setLocalFilename(String filename);


    /***********************************************************************************************
     * Get the RemoteDirectory.
     *
     * @return String
     */

    String getRemoteDirectory();


    /***********************************************************************************************
     * Set the RemoteDirectory.
     *
     * @param directory
     */

    void setRemoteDirectory(String directory);


    /***********************************************************************************************
     * Get the RemoteFilename.
     *
     * @return String
     */

    String getRemoteFilename();


    /***********************************************************************************************
     * Set the RemoteFilename.
     *
     * @param filename
     */

    void setRemoteFilename(String filename);


    /***********************************************************************************************
     * Get the remote filename of the downloaded file when using incremental transfers.
     *
     * @return String
     */

    String getIncrementalRemoteFilename();


    /***********************************************************************************************
     * Set the remote filename of the downloaded file when using incremental transfers.
     *
     * @param filename
     */

    void setIncrementalRemoteFilename(String filename);


    /***********************************************************************************************
     * Get the index into the downloaded file when using incremental transfers.
     *
     * @return long
     */

    long getIncrementalIndex();


    /***********************************************************************************************
     * Set the index into the downloaded file when using incremental transfers.
     *
     * @param index
     */

    void setIncrementalIndex(long index);


    /***********************************************************************************************
     * Get the EventLog.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getEventLog();


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();
    }
