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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.dataconnections;

import org.lmn.fc.common.net.ftp.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.RemoteDataConnectionInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;

import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;


/**********************************************************************************************/
/* Call these methods in this sequence:
 *
 *  initialise()
 *  login()
 *  transmit()  |  receive()  |  receiveBytesIncrementally()
 *  logout()
 *  dispose()
 *
/***************************************************************************************************
* RemoteDataConnectionFTP.
*/

public final class RemoteDataConnectionFTP implements RemoteDataConnectionInterface
    {
    // Injections: FTP Configuration
    private String strHostname;
    private String strUsername;
    private String strPassword;
    private String strTransferMode;
    private String strConnectionMode;
    private String strLocalDirectory;
    private String strLocalFilename;
    private String strRemoteDirectory;
    private String strRemoteFilename;

    // Injections: Logging
    private final Vector<Vector> vecEventLog;
    private final ObservatoryClockInterface clock;
    private final boolean boolDebug;

    // Incremental transfers
    private long longIncrementalIndex;
    private String strIncrementalRemoteFilename;

    private boolean boolVerboseLogging;

    // Internal transport implementation
    private FTPClient clientFTP;


    /***********************************************************************************************
     * Construct a RemoteDataConnectionFTP.
     *
     * @param hostname
     * @param username
     * @param password
     * @param transfermode
     * @param connectionmode
     * @param localdirectory
     * @param localfilename
     * @param remotedirectory
     * @param remotefilename
     * @param eventlog
     * @param obsclock
     * @param debug
     */

    public RemoteDataConnectionFTP(final String hostname,
                                   final String username,
                                   final String password,
                                   final String transfermode,
                                   final String connectionmode,
                                   final String localdirectory,
                                   final String localfilename,
                                   final String remotedirectory,
                                   final String remotefilename,
                                   final Vector<Vector> eventlog,
                                   final ObservatoryClockInterface obsclock,
                                   final boolean debug)
        {
        // Injections: FTP Configuration
        this.strHostname = hostname;
        this.strUsername = username;
        this.strPassword = password;
        this.strTransferMode = transfermode;
        this.strConnectionMode = connectionmode;
        this.strLocalDirectory = localdirectory;
        this.strLocalFilename = localfilename;
        this.strRemoteDirectory = remotedirectory;
        this.strRemoteFilename = remotefilename;

        // Injections: Logging
        this.vecEventLog = eventlog;
        this.clock = obsclock;
        this.boolDebug = debug;

        // Incremental transfers
        this.longIncrementalIndex = 0L;
        this.strIncrementalRemoteFilename = EMPTY_STRING;

        this.boolVerboseLogging = false;

        // Internal transport implementation
        this.clientFTP = null;

        LOGGER.debug(debug, "Constructed RemoteDataConnectionFTP [hostname=" + hostname
                                + "] [username=" + username
                                + "] [password=" + password
                                + "] [transfermode=" + transfermode
                                + "] [connectionmode=" + connectionmode
                                + "] [localdirectory=" + localdirectory
                                + "] [localfilename=" + localfilename
                                + "] [remotedirectory=" + remotedirectory
                                + "] [remotefilename=" + remotefilename
                                + "]");
        }


    /***********************************************************************************************
     * Construct an uninitialised RemoteDataConnectionFTP.
     *
     * @param eventlog
     * @param obsclock
     * @param debug
     */

    public RemoteDataConnectionFTP(final Vector<Vector> eventlog,
                                   final ObservatoryClockInterface obsclock,
                                   final boolean debug)
        {
        this.strHostname = EMPTY_STRING;
        this.strUsername = EMPTY_STRING;
        this.strPassword = EMPTY_STRING;
        this.strTransferMode = EMPTY_STRING;
        this.strConnectionMode = EMPTY_STRING;
        this.strLocalDirectory = EMPTY_STRING;
        this.strLocalFilename = EMPTY_STRING;
        this.strRemoteDirectory = EMPTY_STRING;
        this.strRemoteFilename = EMPTY_STRING;

        // Injections: Logging
        this.vecEventLog = eventlog;
        this.clock = obsclock;
        this.boolDebug = debug;

        // Incremental transfers
        this.longIncrementalIndex = 0L;
        this.strIncrementalRemoteFilename = EMPTY_STRING;

        this.boolVerboseLogging = false;

        // Internal transport implementation
        this.clientFTP = null;

        LOGGER.debug(debug, "Constructed uninitialised RemoteDataConnectionFTP");
        }


    /***********************************************************************************************
     * Initialise the Connection to the RemoteData server.
     * Return true if successful.
     *
     * @param timeoutmillis
     * @param verbose
     *
     * @return boolean
     */

    public boolean initialise(final int timeoutmillis,
                              final boolean verbose)
        {
        final String SOURCE = "RemoteDataConnectionFTP.initialise() ";
        boolean boolSuccess;

        this.boolVerboseLogging = verbose;

        try
            {
            LOGGER.debug(isDebugMode(), SOURCE + "Creating FTPClient [hostname=" + getHostname()
                                        + "] [timeoutmillis=" + timeoutmillis
                                        + "] [verbose=" + verbose + "]" );

            setFTPClient(new FTPClient(getHostname(),
                                       FTPControlSocket.CONTROL_PORT,
                                       timeoutmillis));
            getFTPClient().debugResponses(isDebugMode());

            // Reset the file pointers for incremental downloading
            // This would be accessible by the User from the reset() command
            setIncrementalIndex(0L);
            setIncrementalRemoteFilename(EMPTY_STRING);

            boolSuccess = true;
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_INITIALISE
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        catch (FTPException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_INITIALISE
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Login to the Remote Data server.
     * Return true if successful.
     *
     * @return boolean
     */

    public boolean login()
        {
        final String SOURCE = "RemoteDataConnectionFTP.connect() ";
        boolean boolSuccess;

        try
            {
            // Set the transfer mode to ASCII or BINARY
            // Set Connection mode to pasv or active
            if (getFTPClient() != null)
                {
                LOGGER.debug(isDebugMode(), SOURCE + "Login to FTPClient [hostname=" + getHostname() + "] [password=" + getPassword() + "]");

                // Try to Login to the FTP server
                getFTPClient().login(getUsername(), getPassword());

                if (TRANSFER_MODE_BINARY.equalsIgnoreCase(getTransferMode()))
                    {
                    getFTPClient().setType(FTPTransferType.BINARY);
                    }
                else if (TRANSFER_MODE_ASCII.equalsIgnoreCase(getTransferMode()))
                    {
                    getFTPClient().setType(FTPTransferType.ASCII);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_DATACONNECTION
                                                           + METADATA_ACTION_LOGIN
                                                           + METADATA_TRANSFERMODE
                                                           + getConnectionMode()
                                                           + TERMINATOR,
                                                       getHostname(),
                                                       getObservatoryClock());
                    getFTPClient().setType(FTPTransferType.BINARY);
                    }

                if (CONNECTION_MODE_PASSIVE.equalsIgnoreCase(getConnectionMode()))
                    {
                    getFTPClient().setConnectMode(FTPConnectMode.PASV);
                    }
                else if (CONNECTION_MODE_ACTIVE.equalsIgnoreCase(getConnectionMode()))
                    {
                    getFTPClient().setConnectMode(FTPConnectMode.ACTIVE);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_DATACONNECTION
                                                           + METADATA_ACTION_LOGIN
                                                           + METADATA_CONNECTIONMODE
                                                           + getConnectionMode()
                                                           + TERMINATOR,
                                                       getHostname(),
                                                       getObservatoryClock());
                    getFTPClient().setConnectMode(FTPConnectMode.PASV);
                    }

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_DATACONNECTION
                                                           + METADATA_ACTION_LOGIN
                                                           + METADATA_SYSTEM
                                                           + getFTPClient().system()
                                                           + TERMINATOR,
                                                       getHostname(),
                                                       getObservatoryClock());
                    }

                // Change to the RemoteDirectory if we can
                getFTPClient().chdir(getRemoteDirectory());

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_LOGIN
                                                       + METADATA_PWD
                                                       + getFTPClient().pwd()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                boolSuccess = true;
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_LOGIN
                                                       + METADATA_RESULT
                                                       + "no FTP client"
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                boolSuccess = false;
                }
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_LOGIN
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        catch (FTPException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_LOGIN
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Transmit data to the Remote Data server.
     * Return true if successful.
     *
     * @return boolean
     */

    public boolean transmit()
        {
        final String SOURCE = "RemoteDataConnectionFTP.transmit() ";
        boolean boolSuccess;

        try
            {
            if (getFTPClient() != null)
                {
                final String strLocalPath;
                final String strReplyCode;

                strLocalPath = getLocalDirectory()
                                   + System.getProperty("file.separator")
                                   + getLocalFilename();

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_TRANSMIT
                                                       + METADATA_LOCALDIR + getLocalDirectory() + TERMINATOR_SPACE
                                                       + METADATA_LOCALFILE + getLocalFilename() + TERMINATOR_SPACE
                                                       + METADATA_REMOTEDIR + getRemoteDirectory() + TERMINATOR_SPACE
                                                       + METADATA_REMOTEFILE + getRemoteFilename() + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                // From: full path of local file to read from
                // To: the name of the remote file in current directory
                getFTPClient().put(strLocalPath, getRemoteFilename());

                strReplyCode = getFTPClient().getLastValidReply().getReplyCode();

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_TRANSMIT
                                                       + "[code="
                                                       + getFTPClient().getLastValidReply().getReplyCode()
                                                       + "] [text="
                                                       + getFTPClient().getLastValidReply().getReplyText()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                boolSuccess = (FTPReplyCode.VALID_REPLY.equals(strReplyCode));
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_TRANSMIT
                                                       + METADATA_RESULT
                                                       + "no FTP client"
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                boolSuccess = false;
                }
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_TRANSMIT
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        catch (FTPException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_TRANSMIT
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Receive data from the Remote Data server.
     * Transfer RemoteFilename from RemoteDirectory to LocalDirectory/LocalFilename.
     * Return true if successful.
     *
     * @return boolean
     */

    public boolean receive()
        {
        final String SOURCE = "RemoteDataConnectionFTP.receive() ";
        boolean boolSuccess;

        try
            {
            if (getFTPClient() != null)
                {
                final String strLocalPath;
                final String strReplyCode;

                strLocalPath = getLocalDirectory()
                                   + System.getProperty("file.separator")
                                   + getLocalFilename();

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_RECEIVE
                                                       + METADATA_LOCALDIR + getLocalDirectory() + TERMINATOR_SPACE
                                                       + METADATA_LOCALFILE + getLocalFilename() + TERMINATOR_SPACE
                                                       + METADATA_REMOTEDIR + getRemoteDirectory() + TERMINATOR_SPACE
                                                       + METADATA_REMOTEFILE + getRemoteFilename() + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                // Change dir
                getFTPClient().chdir(getRemoteDirectory());

                // Get a remote file
                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_RECEIVE
                                                       + METADATA_STATUS
                                                       + "retrieving file"
                                                       + TERMINATOR_SPACE
                                                       + METADATA_REMOTEFILE
                                                       + getRemoteFilename()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                // To: full path of local file to write to
                // From: the name of the remote file in current directory
                getFTPClient().get(strLocalPath, getRemoteFilename());

                strReplyCode = getFTPClient().getLastValidReply().getReplyCode();

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_RECEIVE
                                                       + "[code="
                                                       + getFTPClient().getLastValidReply().getReplyCode()
                                                       + "] [text="
                                                       + getFTPClient().getLastValidReply().getReplyText()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                boolSuccess = (FTPReplyCode.VALID_REPLY.equals(strReplyCode));
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_RECEIVE
                                                       + METADATA_RESULT
                                                       + "no FTP client"
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                boolSuccess = false;
                }
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                               + METADATA_ACTION_RECEIVE
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        catch (FTPException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                               + METADATA_ACTION_RECEIVE
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Receive data incrementally from the Remote Data server, as an array of bytes.
     * Transfers data from the IncrementalRemoteFilename, starting at the current IncrementalIndex.
     * This is a wrapper for FTPClient.getBytesIncrementally(remotefile, startpoint).
     * Return NULL on failure,
     *
     * @return byte[]
     */

    public byte[] receiveBytesIncrementally()
        {
        final String SOURCE = "RemoteDataConnectionFTP.receiveBytesIncrementally() ";
        byte[] arrayBytes;

        try
            {
            if (getFTPClient() != null)
                {
                final String strReplyCode;

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_RECEIVE_INC
                                                       + METADATA_LOCALDIR + getLocalDirectory() + TERMINATOR_SPACE
                                                       + METADATA_LOCALFILE + getLocalFilename() + TERMINATOR_SPACE
                                                       + METADATA_REMOTEDIR + getRemoteDirectory() + TERMINATOR_SPACE
                                                       + METADATA_REMOTEFILE_INC + getIncrementalRemoteFilename() + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                // Change dir
                getFTPClient().chdir(getRemoteDirectory());

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_RECEIVE_INC
                                                       + METADATA_PWD
                                                       + getFTPClient().pwd()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                // Get a remote file
                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_RECEIVE_INC
                                                       + METADATA_STATUS
                                                       + "retrieving file"
                                                       + TERMINATOR_SPACE
                                                       + METADATA_REMOTEFILE_INC
                                                       + getIncrementalRemoteFilename()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                arrayBytes = getFTPClient().getBytesIncrementally(getIncrementalRemoteFilename(),
                                                                  getIncrementalIndex());

                strReplyCode = getFTPClient().getLastValidReply().getReplyCode();

                if (isVerboseLogging())
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_RECEIVE_INC
                                                       + "[code="
                                                       + getFTPClient().getLastValidReply().getReplyCode()
                                                       + "] [text="
                                                       + getFTPClient().getLastValidReply().getReplyText()
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                    }

                if (!FTPReplyCode.VALID_REPLY.equals(strReplyCode))
                    {
                    arrayBytes = null;
                    }
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_RECEIVE_INC
                                                       + METADATA_RESULT
                                                       + "no FTP client"
                                                       + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                arrayBytes = null;
                }
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_RECEIVE_INC
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            arrayBytes = null;
            }

        catch (FTPException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_RECEIVE_INC
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            arrayBytes = null;
            }

        return (arrayBytes);
        }


    /***********************************************************************************************
     * Logout from the Remote Data server.
     * For the FTP implementation this cancels any transfer in progress, and executes QUIT.
     * Return true if successful.
     *
     * @return boolean
     */

    public boolean logout()
        {
        final String SOURCE = "RemoteDataConnectionFTP.disconnect() ";
        boolean boolSuccess;

        try
            {
            if (getFTPClient() != null)
                {
                LOGGER.debug(isDebugMode(), SOURCE);

                getFTPClient().cancelTransfer();
                getFTPClient().quit();
                boolSuccess = true;
                }
            else
                {
                boolSuccess = false;
                }
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_LOGOUT
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        catch (FTPException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLog(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_DATACONNECTION
                                                   + METADATA_ACTION_LOGOUT
                                                   + METADATA_EXCEPTION
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                               getHostname(),
                                               getObservatoryClock());
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Dispose the Connection to the RemoteData server.
     * For the FTP implementation this cancels any transfer in progress, and executes QUIT.
     * Return true if successful.
     *
     * @return boolean
     */

    public boolean dispose()
        {
        final String SOURCE = "RemoteDataConnectionFTP.dispose() ";

        LOGGER.debug(isDebugMode(), SOURCE);

        // Close down the FTP session...
        logout();

        // ...remove the Socket...
        if (getFTPClient() != null)
            {
            getFTPClient().disposeSocket();
            }

        // ...and remove the FTP client
        setFTPClient(null);

        setIncrementalIndex(0L);
        setIncrementalRemoteFilename(EMPTY_STRING);

        // There's not a lot we can do if this fails!
        return (true);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the FTPClient.
     *
     * @return FTPClient
     */

    private FTPClient getFTPClient()
        {
        return (this.clientFTP);
        }


    /***********************************************************************************************
     * Set the FTPClient.
     *
     * @param ftp
     */

    private void setFTPClient(final FTPClient ftp)
        {
        this.clientFTP = ftp;
        }


    /***********************************************************************************************
     * Get the Hostname of the Remote Data server.
     *
     * @return String
     */

    public String getHostname()
        {
        return (this.strHostname);
        }


    /***********************************************************************************************
     * Set the Hostname of the Remote Data server.
     *
     * @param hostname
     */

    public void setHostname(final String hostname)
        {
        this.strHostname = hostname;
        }


    /***********************************************************************************************
     * Get the Username to login to the Remote Data server.
     *
     * @return String
     */

    public String getUsername()
        {
        return (this.strUsername);
        }


    /***********************************************************************************************
     * Set the Username to login to the Remote Data server.
     *
     * @param username
     */

    public void setUsername(final String username)
        {
        this.strUsername = username;
        }


    /***********************************************************************************************
     * Get the Password to login to the Remote Data server.
     *
     * @return String
     */

    public String getPassword()
        {
        return (this.strPassword);
        }


    /***********************************************************************************************
     * Set the Password to login to the Remote Data server.
     *
     * @param password
     */

    public void setPassword(final String password)
        {
        this.strPassword = password;
        }


    /***********************************************************************************************
     * Get the TransferMode.
     *
     * @return String
     */

    public String getTransferMode()
        {
        return (this.strTransferMode);
        }


    /***********************************************************************************************
     * Set the TransferMode.
     *
     * @param mode
     */

    public void setTransferMode(final String mode)
        {
        this.strTransferMode = mode;
        }


    /***********************************************************************************************
     * Get the ConnectionMode.
     *
     * @return String
     */

    public String getConnectionMode()
        {
        return (this.strConnectionMode);
        }


    /***********************************************************************************************
     * Set the ConnectionMode.
     *
     * @param mode
     */

    public void setConnectionMode(final String mode)
        {
        this.strConnectionMode = mode;
        }


    /***********************************************************************************************
     * Get the Connection timeout in milliseconds.
     *
     * @return int
     */

    public int getTimeoutMillis()
        {
        final String SOURCE = "RemoteDataConnectionFTP.getTimeoutMillis() ";
        final int intTimeoutMillis;

        if (getFTPClient() != null)
            {
            intTimeoutMillis = getFTPClient().getTimeout();
            }
        else
            {
            intTimeoutMillis = -1;
            }

        LOGGER.debug(isDebugMode(), SOURCE + "[timeoutmillis=" + intTimeoutMillis + "]");

        return (intTimeoutMillis);
        }


    /***********************************************************************************************
     * Set the Connection timeout in milliseconds.
     *
     * @param timeoutmillis
     */

    public boolean setTimeoutMillis(final int timeoutmillis)
        {
        final String SOURCE = "RemoteDataConnectionFTP.setTimeoutMillis() ";
        boolean boolSuccess;

        if (getFTPClient() != null)
            {
            try
                {
                LOGGER.debug(isDebugMode(), SOURCE + "[timeoutmillis=" + timeoutmillis + "]");

                getFTPClient().setTimeout(timeoutmillis);
                boolSuccess = true;
                }

            catch (SocketException exception)
                {
                SimpleEventLogUIComponent.logEvent(getEventLog(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_DATACONNECTION
                                                       + METADATA_ACTION_SET
                                                           + METADATA_EXCEPTION
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                   getHostname(),
                                                   getObservatoryClock());
                boolSuccess = false;
                }
            }
        else
            {
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Get the LocalDirectory.
     *
     * @return String
     */

    public String getLocalDirectory()
        {
        return (this.strLocalDirectory);
        }


    /***********************************************************************************************
     * Set the LocalDirectory.
     *
     * @param directory
     */

    public void setLocalDirectory(final String directory)
        {
        this.strLocalDirectory = directory;
        }


     /***********************************************************************************************
     * Get the LocalFilename.
     *
     * @return String
     */

    public String getLocalFilename()
        {
        return (this.strLocalFilename);
        }


    /***********************************************************************************************
     * Set the LocalFilename.
     *
     * @param filename
     */

    public void setLocalFilename(final String filename)
        {
        this.strLocalFilename = filename;
        }


    /***********************************************************************************************
     * Get the RemoteDirectory.
     *
     * @return String
     */

    public String getRemoteDirectory()
        {
        return (this.strRemoteDirectory);
        }


    /***********************************************************************************************
     * Set the RemoteDirectory.
     *
     * @param directory
     */

    public void setRemoteDirectory(final String directory)
        {
        this.strRemoteDirectory = directory;
        }


    /***********************************************************************************************
     * Get the RemoteFilename.
     *
     * @return String
     */

    public String getRemoteFilename()
        {
        return (this.strRemoteFilename);
        }


    /***********************************************************************************************
     * Set the RemoteFilename.
     *
     * @param filename
     */

    public void setRemoteFilename(final String filename)
        {
        this.strRemoteFilename = filename;
        }


    /***********************************************************************************************
     * Get the remote filename of the downloaded file when using incremental transfers.
     *
     * @return String
     */

    public String getIncrementalRemoteFilename()
        {
        return (this.strIncrementalRemoteFilename);
        }


    /***********************************************************************************************
     * Set the remote filename of the downloaded file when using incremental transfers.
     *
     * @param filename
     */

    public void setIncrementalRemoteFilename(final String filename)
        {
        this.strIncrementalRemoteFilename = filename;
        }


    /***********************************************************************************************
     * Get the index into the downloaded file when using incremental transfers.
     *
     * @return long
     */

    public long getIncrementalIndex()
        {
        return (this.longIncrementalIndex);
        }


    /***********************************************************************************************
     * Set the index into the downloaded file when using incremental transfers.
     *
     * @param index
     */

    public void setIncrementalIndex(final long index)
        {
        this.longIncrementalIndex = index;
        }


    /***********************************************************************************************
     * Get the EventLog.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getEventLog()
        {
        return (this.vecEventLog);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        return (this.clock);
        }


    /***********************************************************************************************
     * Indicate if we are in Verbose Logging mode.
     *
     * @return boolean
     */

    private boolean isVerboseLogging()
        {
        return (this.boolVerboseLogging);
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    private boolean isDebugMode()
        {
        return (this.boolDebug);
        }
    }
