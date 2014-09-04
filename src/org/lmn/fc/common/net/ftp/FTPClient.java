/**
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003  Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 *
 *  Change Log:
 *
 *        $Log: FTPClient.java,v $
 *        Revision 1.8  2003/11/03 21:19:23  bruceb
 *        added progress callback and cancel ability
 *
 *        Revision 1.7  2003/11/02 21:51:20  bruceb
 *        numerous fixes and additions
 *
 *        Revision 1.6  2003/05/31 14:53:44  bruceb
 *        1.2.2 changes
 *
 *        Revision 1.5  2003/01/29 22:46:08  bruceb
 *        minor changes
 *
 *        Revision 1.4  2002/11/19 22:01:25  bruceb
 *        changes for 1.2
 *
 *        Revision 1.3  2001/10/09 20:53:46  bruceb
 *        Active mode changes
 *
 *        Revision 1.1  2001/10/05 14:42:03  bruceb
 *        moved from old project
 *
 */

package org.lmn.fc.common.net.ftp;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

/**
 * Supports client-side FTP. Most common
 * FTP operations are present in this class.
 *
 * @author Bruce Blackshaw
 * @version $Revision: 1.8 $
 */
public final class FTPClient implements FrameworkConstants
    {
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    /**
     * Format to interpret MTDM timestamp
     */
    // ToDo Consider ThreadLocal
    private final SimpleDateFormat tsFormat =
        new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Socket responsible for controlling
     * the connection
     */
    private FTPControlSocket control;

    /**
     * Socket responsible for transferring
     * the data
     */
    private FTPDataSocket data;

    /**
     * Socket timeout for both data and control. In
     * milliseconds
     */
    private int timeout;

    /**
     * Can be used to cancel a transfer
     */
    private boolean cancelTransfer;

    /**
     * Bytes transferred in between monitor callbacks
     */
    private long monitorInterval = CHUNK_SIZE;

    /**
     * Progress monitor
     */
    private FTPProgressMonitor monitor;

    /**
     * Record of the transfer type - make the default ASCII
     */
    private FTPTransferType transferType = FTPTransferType.ASCII;

    /**
     * Record of the connect mode - make the default PASV (as this was
     * the original mode supported)
     */
    private FTPConnectMode connectMode = FTPConnectMode.PASV;

    /**
     * Holds the last valid reply from the server on the control socket
     */
    private FTPReply lastValidReply;
    private static final int CHUNK_SIZE = 4096;

    /**
     * Constructor. Sets formatter to GMT.
     */
    private FTPClient()
        {
        tsFormat.setTimeZone(REGISTRY.getGMTTimeZone());
        }

    /**
     * Constructor. Creates the control
     * socket
     *
     * @param remoteHost the remote hostname
     * @throws IOException
     * @throws FTPException
     */
    public FTPClient(final String remoteHost) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteHost,
                                       FTPControlSocket.CONTROL_PORT,
                                       null,
                                       0);
        }


    public FTPClient(final String remoteHost,
                     final int port,
                     final int timeoutmillis) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteHost,
                                       port,
                                       null,
                                       timeoutmillis);
        }

    /**
     * Constructor. Creates the control
     * socket
     *
     * @param remoteHost  the remote hostname
     * @param controlPort port for control stream
     * @throws FTPException
     * @throws IOException
     */
    public FTPClient(final String remoteHost,
                     final int controlPort) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteHost, controlPort, null, 0);
        }

    /**
     * Constructor. Creates the control
     * socket
     *
     * @param remoteAddr the address of the
     *                   remote host
     * @throws IOException
     * @throws FTPException
     */
    public FTPClient(final InetAddress remoteAddr) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteAddr,
                                       FTPControlSocket.CONTROL_PORT,
                                       null, 0);
        }

    /**
     * Constructor. Creates the control
     * socket. Allows setting of control port (normally
     * set by default to 21).
     *
     * @param remoteAddr  the address of the
     *                    remote host
     * @param controlPort port for control stream
     * @throws FTPException
     * @throws IOException
     */
    public FTPClient(final InetAddress remoteAddr,
                     final int controlPort) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteAddr,
                                       controlPort,
                                       null,
                                       0);
        }

    /**
     * Constructor. Creates the control
     * socket
     *
     * @param remoteHost the remote hostname
     * @param log
     * @param timeout
     * @throws IOException
     * @throws FTPException
     */
    public FTPClient(final String remoteHost,
                     final PrintWriter log,
                     final int timeout) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteHost,
                                       FTPControlSocket.CONTROL_PORT,
                                       log,
                                       timeout);
        }

    /**
     * Constructor. Creates the control
     * socket
     *
     * @param remoteHost  the remote hostname
     * @param controlPort port for control stream
     * @param timeout
     * @param log
     * @throws IOException
     * @throws FTPException
     */
    public FTPClient(final String remoteHost,
                     final int controlPort,
                     final PrintWriter log,
                     final int timeout) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteHost,
                                       controlPort,
                                       log,
                                       timeout);
        }

    /**
     * Constructor. Creates the control
     * socket
     *
     * @param remoteAddr the address of the
     *                   remote host
     * @param log
     * @param timeout
     * @throws FTPException
     * @throws IOException
     */
    public FTPClient(final InetAddress remoteAddr,
                     final PrintWriter log,
                     final int timeout) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteAddr,
                                       FTPControlSocket.CONTROL_PORT,
                                       log,
                                       timeout);
        }

    /**
     * Constructor. Creates the control
     * socket. Allows setting of control port (normally
     * set by default to 21).
     *
     * @param remoteAddr  the address of the
     *                    remote host
     * @param controlPort port for control stream
     * @param timeout
     * @param log
     * @throws IOException
     * @throws FTPException
     */
    public FTPClient(final InetAddress remoteAddr,
                     final int controlPort,
                     final PrintWriter log,
                     final int timeout) throws IOException, FTPException
        {
        this();
        control = new FTPControlSocket(remoteAddr,
                                       controlPort,
                                       log,
                                       timeout);
        }


    /***********************************************************************************************
     * Get the TCP timeout on the underlying socket.
     *
     * @return int
     */

    public int getTimeout()
        {
        // We shall assume that this timeout is the same as that of the FTPControlSocket
        return (this.timeout);
        }


    /***********************************************************************************************
     * Set the TCP timeout on the underlying socket.
     * <p/>
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException. We
     * set both the control and data connections
     *
     * @param millis The length of the timeout, in milliseconds
     * @throws SocketException
     */
    public void setTimeout(final int millis) throws SocketException
        {
        this.timeout = millis;
        control.setTimeout(millis);
        }

    /**
     * Set the connect mode
     *
     * @param mode ACTIVE or PASV mode
     */
    public void setConnectMode(final FTPConnectMode mode)
        {
        connectMode = mode;
        }

    /**
     * Set a progress monitor for callbacks. The bytes transferred in
     * between callbacks is only indicative. In many cases, the data is
     * read in chunks, and if the interval is set to be smaller than the
     * chunk size, the callback will occur after after chunk transfer rather
     * than the interval.
     *
     * @param monitor  the monitor object
     * @param interval bytes transferred in between callbacks
     */
    public void setProgressMonitor(final FTPProgressMonitor monitor, final long interval)
        {
        this.monitor = monitor;
        this.monitorInterval = interval;
        }

    /**
     * Set a progress monitor for callbacks. Uses default callback
     * interval
     *
     * @param monitor the monitor object
     */
    public void setProgressMonitor(final FTPProgressMonitor monitor)
        {
        this.monitor = monitor;
        }


    /**
     * Get the bytes transferred between each callback on the
     * progress monitor
     *
     * @return long     bytes to be transferred before a callback
     */
    public long getMonitorInterval()
        {
        return monitorInterval;
        }

    /**
     * Cancels the current transfer. Must be called from a separate
     * thread
     */
    public void cancelTransfer()
        {
        cancelTransfer = true;
        }

    /**
     * Login into an account on the FTP server. This
     * call completes the entire login process
     *
     * @param user     user name
     * @param password user's password
     * @throws IOException
     * @throws FTPException
     */
    public void login(final String user, final String password)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("USER " + user);

        // we allow for a site with no password - 230 response
        final String[] validCodes = {"230", "331"};
        lastValidReply = control.validateReply(reply, validCodes);
        if (lastValidReply.getReplyCode().equals("230"))
            {
            }
        else
            {
            password(password);
            }
        }


    /**
     * Supply the user name to log into an account
     * on the FTP server. Must be followed by the
     * password() method - but we allow for
     *
     * @param user user name
     * @throws FTPException
     * @throws IOException
     */
    public void user(final String user)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("USER " + user);

        // we allow for a site with no password - 230 response
        final String[] validCodes = {"230", "331"};
        lastValidReply = control.validateReply(reply, validCodes);
        }


    /**
     * Supplies the password for a previously supplied
     * username to log into the FTP server. Must be
     * preceeded by the user() method
     *
     * @param password user's password
     * @throws FTPException
     * @throws IOException
     */
    public void password(final String password)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("PASS " + password);

        // we allow for a site with no passwords (202)
        final String[] validCodes = {"230", "202"};
        lastValidReply = control.validateReply(reply, validCodes);
        }

    /**
     * Set up SOCKS v4/v5 proxy settings. This can be used if there
     * is a SOCKS proxy server in place that must be connected thru.
     * Note that setting these properties directs <b>all</b> TCP
     * sockets in this JVM to the SOCKS proxy
     *
     * @param port SOCKS proxy port
     * @param host SOCKS proxy hostname
     */
    public static void initSOCKS(final String port, final String host)
        {
        final Properties props = System.getProperties();
        props.put("socksProxyPort", port);
        props.put("socksProxyHost", host);
        System.setProperties(props);
        }

    /**
     * Set up SOCKS username and password for SOCKS username/password
     * authentication. Often, no authentication will be required
     * but the SOCKS server may be configured to request these.
     *
     * @param username the SOCKS username
     * @param password the SOCKS password
     */
    public static void initSOCKSAuthentication(final String username,
                                               final String password)
        {
        final Properties props = System.getProperties();
        props.put("java.net.socks.username", username);
        props.put("java.net.socks.password", password);
        System.setProperties(props);
        }

    /**
     * Get the name of the remote host
     *
     * @return remote host name
     */
    String getRemoteHostName()
        {
        return control.getRemoteHostName();
        }


    /**
     * Issue arbitrary ftp commands to the FTP server.
     *
     * @param command    ftp command to be sent to server
     * @param validCodes valid return codes for this command
     * @return the text returned by the FTP server
     * @throws FTPException
     * @throws IOException
     */
    public String quote(final String command, final String[] validCodes)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand(command);

        // allow for no validation to be supplied
        if (validCodes != null && validCodes.length > 0)
            {
            lastValidReply = control.validateReply(reply, validCodes);
            return lastValidReply.getReplyText();
            }
        else
            {
            throw new FTPException("Valid reply code must be supplied");
            }
        }


    /**
     * Get the size of a remote file. This is not a standard FTP command, it
     * is defined in "Extensions to FTP", a draft RFC
     * (draft-ietf-ftpext-mlst-16.txt)
     *
     * @param remoteFile name or path of remote file in current directory
     * @return size of file in bytes
     * @throws IOException
     * @throws FTPException
     */
    public long size(final String remoteFile)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("SIZE " + remoteFile);
        lastValidReply = control.validateReply(reply, "213");

        // parse the reply string .
        final String replyText = lastValidReply.getReplyText();
        try
            {
            return Long.parseLong(replyText);
            }
        catch (NumberFormatException ex)
            {
            throw new FTPException("Failed to parse reply: " + replyText);
            }
        }


    /**
     * Put a local file onto the FTP server. It
     * is placed in the current directory.
     *
     * @param localPath  path of the local file
     * @param remoteFile name of remote file in
     *                   current directory
     * @throws IOException
     * @throws FTPException
     */
    public void put(final String localPath, final String remoteFile)
        throws IOException, FTPException
        {

        put(localPath, remoteFile, false);
        }

    /**
     * Put a stream of data onto the FTP server. It
     * is placed in the current directory.
     *
     * @param srcStream  input stream of data to put
     * @param remoteFile name of remote file in
     *                   current directory
     * @throws IOException
     * @throws FTPException
     */
    public void put(final InputStream srcStream, final String remoteFile)
        throws IOException, FTPException
        {

        put(srcStream, remoteFile, false);
        }


    /**
     * Put a local file onto the FTP server. It
     * is placed in the current directory. Allows appending
     * if current file exists
     *
     * @param localPath  path of the local file
     * @param remoteFile name of remote file in
     *                   current directory
     * @param append     true if appending, false otherwise
     * @throws IOException
     * @throws FTPException
     */
    public void put(final String localPath, final String remoteFile,
                    final boolean append)
        throws IOException, FTPException
        {

        // get according to set type
        if (FTPTransferType.ASCII.equals(getType()))
            {
            putASCII(localPath, remoteFile, append);
            }
        else
            {
            putBinary(localPath, remoteFile, append);
            }
        validateTransfer();
        }

    /**
     * Put a stream of data onto the FTP server. It
     * is placed in the current directory. Allows appending
     * if current file exists
     *
     * @param srcStream  input stream of data to put
     * @param remoteFile name of remote file in
     *                   current directory
     * @param append     true if appending, false otherwise
     * @throws IOException
     * @throws FTPException
     */
    public void put(final InputStream srcStream, final String remoteFile,
                    final boolean append)
        throws IOException, FTPException
        {

        // get according to set type
        if (FTPTransferType.ASCII.equals(getType()))
            {
            putASCII(srcStream, remoteFile, append);
            }
        else
            {
            putBinary(srcStream, remoteFile, append);
            }
        validateTransfer();
        }


    /***********************************************************************************************
     * Validate that the put() or get() was successful
     *
     * @throws IOException
     * @throws FTPException
     */

    private void validateTransfer() throws IOException, FTPException
        {
        final String[] validCodes;
        final String reply;

        // Check the control response
        validCodes = new String[]{"226", "250"};
        reply = control.readReply();
        //System.out.println("reply={" + reply + "}");
        lastValidReply = control.validateReply(reply, validCodes);
        }



    /**
     * Request the server to set up the put
     *
     * @param remoteFile name of remote file in
     *                   current directory
     * @param append     true if appending, false otherwise
     * @throws IOException
     * @throws FTPException
     */
    private void initPut(final String remoteFile, final boolean append) throws IOException, FTPException
        {
        // reset the cancel flag
        cancelTransfer = false;

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the command to store
        final String cmd;
        if (append)
            {
            cmd = "APPE ";
            }
        else
            {
            cmd = "STOR ";
            }
        final String reply = control.sendCommand(cmd + remoteFile);

        // Can get a 125 or a 150
        final String[] validCodes = {"125", "150"};
        lastValidReply = FTPControlSocket.validateReply(reply, validCodes);
        }


    /**
     * Put as ASCII, i.e. read a line at a time and write
     * inserting the correct FTP separator
     *
     * @param localPath  full path of local file to read from
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     * @throws IOException
     * @throws FTPException
     */
    private void putASCII(final String localPath, final String remoteFile, final boolean append)
        throws IOException, FTPException
        {

        // create an inputstream & pass to common method
        final InputStream srcStream = new FileInputStream(localPath);
        putASCII(srcStream, remoteFile, append);
        }

    /**
     * Put as ASCII, i.e. read a line at a time and write
     * inserting the correct FTP separator
     *
     * @param srcStream  input stream of data to put
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     * @throws IOException
     * @throws FTPException
     */
    private void putASCII(final InputStream srcStream, final String remoteFile,
                          final boolean append)
        throws IOException, FTPException
        {

        // need to read line by line ...
        final LineNumberReader in
            = new LineNumberReader(new InputStreamReader(srcStream));

        initPut(remoteFile, append);

        // get an character output stream to write to ... AFTER we
        // have the ok to go ahead AND AFTER we've successfully opened a
        // stream for the local file
        final BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(data.getOutputStream()));

        // write \r\n as required by RFC959 after each line
        long size = 0;
        long monitorCount = 0;
        int ch;

        while ((ch = in.read()) != -1 && !cancelTransfer)
            {
            size++;
            monitorCount++;
            if (ch == '\n')
                out.write(FTPControlSocket.EOL);
            else
                out.write(ch);

            if (monitor != null && monitorCount > monitorInterval)
                {
                monitor.bytesTransferred(size);
                monitorCount = 0;
                }
            }
        in.close();
        out.flush();
        out.close();

        // and close the data socket
        try
            {
            data.close();
            }
        catch (IOException ignore)
            {
            }
        }


    /**
     * Put as binary, i.e. read and write raw bytes
     *
     * @param localPath  full path of local file to read from
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     * @throws FTPException
     * @throws IOException
     */
    private void putBinary(final String localPath, final String remoteFile,
                           final boolean append)
        throws IOException, FTPException
        {

        // open input stream to read source file ... do this
        // BEFORE opening output stream to server, so if file not
        // found, an exception is thrown
        final InputStream srcStream = new FileInputStream(localPath);
        putBinary(srcStream, remoteFile, append);
        }

    /**
     * Put as binary, i.e. read and write raw bytes
     *
     * @param srcStream  input stream of data to put
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     * @throws FTPException
     * @throws IOException
     */
    private void putBinary(final InputStream srcStream, final String remoteFile,
                           final boolean append)
        throws IOException, FTPException
        {

        final BufferedInputStream in =
            new BufferedInputStream(srcStream);

        initPut(remoteFile, append);

        // get an output stream
        final BufferedOutputStream out =
            new BufferedOutputStream(
                new DataOutputStream(data.getOutputStream()));

        final byte[] buf = new byte[512];

        // read a chunk at a time and write to the data socket
        long size = 0;
        long monitorCount = 0;
        int count;
        while ((count = in.read(buf)) > 0 && !cancelTransfer)
            {
            out.write(buf, 0, count);
            size += count;
            monitorCount += count;
            if (monitor != null && monitorCount > monitorInterval)
                {
                monitor.bytesTransferred(size);
                monitorCount = 0;
                }
            }

        in.close();

        // flush and clean up
        out.flush();
        out.close();

        // and close the data socket
        try
            {
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // log bytes transferred
        control.log("Transferred " + size + " bytes to remote host");
        }


    /**
     * Put data onto the FTP server. It
     * is placed in the current directory.
     *
     * @param bytes      array of bytes
     * @param remoteFile name of remote file in
     *                   current directory
     * @throws FTPException
     * @throws IOException
     */
    public void put(final byte[] bytes, final String remoteFile)
        throws IOException, FTPException
        {

        put(bytes, remoteFile, false);
        }

    /**
     * Put data onto the FTP server. It
     * is placed in the current directory. Allows
     * appending if current file exists
     *
     * @param bytes      array of bytes
     * @param remoteFile name of remote file in
     *                   current directory
     * @param append     true if appending, false otherwise
     * @param bytes
     * @throws FTPException
     * @throws IOException
     */
    public void put(final byte[] bytes, final String remoteFile, final boolean append)
        throws IOException, FTPException
        {

        initPut(remoteFile, append);

        // get an output stream
        final BufferedOutputStream out =
            new BufferedOutputStream(
                new DataOutputStream(data.getOutputStream()));

        // write array
        out.write(bytes, 0, bytes.length);

        // flush and clean up
        out.flush();
        out.close();

        // and close the data socket
        try
            {
            data.close();
            }
        catch (IOException ignore)
            {
            }

        validateTransfer();
        }


    /**********************************************************************************************/
    /* get()                                                                                      */
    /***********************************************************************************************
     * Get data from the FTP server.
     * Uses the currently set transfer mode.
     *
     * @param localPath  local file to put data in
     * @param remoteFile name of remote file in current directory
     *
     * @throws FTPException
     * @throws IOException
     */

    public void get(final String localPath,
                    final String remoteFile) throws IOException, FTPException
        {
        //System.out.println("do get in client");
        // get according to set type
        if (FTPTransferType.ASCII.equals(getType()))
            {
            getASCII(localPath, remoteFile);
            }
        else
            {
            //System.out.println("do binary xfer");
            getBinary(localPath, remoteFile);
            }

        validateTransfer();
        }


    /***********************************************************************************************
     * Get data from the FTP server.
     * Uses the currently set transfer mode.
     *
     * @param localPath  local file to put data in
     * @param remoteFile name of remote file in current directory
     * @param startpoint
     *
     * @throws FTPException
     * @throws IOException
     */

    public void getIncrementally(final String localPath,
                                 final String remoteFile,
                                 final long startpoint) throws IOException, FTPException
        {
//        System.out.println("FTPClient.getIncrementally [startpoint=" + startpoint + "]");

        // B.McKeown:
        // Call initGet() before creating the FileOutputStream.
        // This will prevent being left with an empty file if a FTPException
        // is thrown by initGet().

        // This sends the REST and RETR commands
        initGetIncrementally(remoteFile, startpoint);

        // get() according to set type
        if (FTPTransferType.ASCII.equals(getType()))
            {
            getASCIIGeneric(localPath, remoteFile);
            }
        else
            {
            getBinaryGeneric(localPath, remoteFile);
            }

        validateTransfer();
        }


    /***********************************************************************************************
     * Get data from the FTP server.
     * Uses the currently set transfer mode.
     *
     * @param remoteFile name of remote file in current directory
     * @param startpoint
     *
     * @throws FTPException
     * @throws IOException
     */

    public byte[] getBytesIncrementally(final String remoteFile,
                                        final long startpoint) throws IOException, FTPException
        {
        final byte[] bytes;

//        System.out.println("FTPClient.getBytesIncrementally [startpoint=" + startpoint + "]");

        // B.McKeown:
        // Call initGet() before creating the FileOutputStream.
        // This will prevent being left with an empty file if a FTPException
        // is thrown by initGet().

        // This sends the REST and RETR commands
        initGetIncrementally(remoteFile, startpoint);

        bytes = getBytesGeneric(remoteFile);

        return (bytes);
        }


    /***********************************************************************************************
     * Get data from the FTP server. Uses the currently
     * set transfer mode.
     *
     * @param destStream data stream to write data to
     * @param remoteFile name of remote file in
     *                   current directory
     * @throws FTPException
     * @throws IOException
     */

    public void get(final OutputStream destStream,
                    final String remoteFile) throws IOException, FTPException
        {
        // get according to set type
        if (FTPTransferType.ASCII.equals(getType()))
            {
            getASCII(destStream, remoteFile);
            }
        else
            {
            getBinary(destStream, remoteFile);
            }
        validateTransfer();
        }


    /***********************************************************************************************
     * Request to the server that the get is set up
     *
     * @param remoteFile name of remote file
     *
     * @throws FTPException
     * @throws IOException
     */

    private void initGet(final String remoteFile) throws IOException, FTPException
        {

        // reset the cancel flag
        cancelTransfer = false;

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the retrieve command
        final String reply = control.sendCommand("RETR " + remoteFile);

        // Can get a 125 or a 150
        final String[] validCodes1 = {"125", "150"};
        lastValidReply = control.validateReply(reply, validCodes1);
        }


    /***********************************************************************************************
      * Request to the server that the incremental get() is set up
      *
      * @param remoteFile name of remote file
      * @param startpoint
      *
      * @throws FTPException
      * @throws IOException
      */

     private void initGetIncrementally(final String remoteFile,
                                       final long startpoint) throws IOException, FTPException
         {
         String reply;
         String[] validCodes;

//         System.out.println("FTPClient.initGetIncrementally() [startpoint=" + startpoint + "]");

         // Reset the cancel flag
         cancelTransfer = false;

         // Set up the data channel
         data = control.createDataSocket(connectMode);
         data.setTimeout(timeout);

         // Send the REST command
         reply = control.sendCommand("REST " + startpoint);

         // Can get a 125, 150 or 350 ??
         validCodes = new String[]{"125", "150", "350"};
         lastValidReply = control.validateReply(reply, validCodes);

         // Send the retrieve command
         reply = control.sendCommand("RETR " + remoteFile);

         // Can get a 125 or a 150
         validCodes = new String[]{"125", "150"};
         lastValidReply = control.validateReply(reply, validCodes);
         }


    /***********************************************************************************************
     * Get as ASCII without initGet(), i.e. read a line at a time and write
     * using the correct newline separator for the OS
     *
     * @param localPath  full path of local file to write to
     * @param remoteFile name of remote file
     *
     * @throws FTPException
     * @throws IOException
     */

    private void getASCIIGeneric(final String localPath,
                                 final String remoteFile) throws IOException, FTPException
        {
        // B. McKeown: Need to store the local file name so the file can be
        // deleted if necessary.
        final File localFile = new File(localPath);

        // create the buffered stream for writing
        final BufferedWriter out = new BufferedWriter(new FileWriter(localPath));

        // get an character input stream to read data from ... AFTER we
        // have the ok to go ahead AND AFTER we've successfully opened a
        // stream for the local file
        final LineNumberReader in =
            new LineNumberReader(
                new InputStreamReader(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // output a new line after each received newline
        IOException storedEx = null;
        long size = 0;
        long monitorCount = 0;
        try
            {
            int ch;
            while ((ch = in.read()) != -1 && !cancelTransfer)
                {
                size++;
                monitorCount++;
                if (ch == '\n')
                    out.newLine();
                else
                    out.write(ch);

                if (monitor != null && monitorCount > monitorInterval)
                    {
                    monitor.bytesTransferred(size);
                    monitorCount = 0;
                    }
                }
            }
        catch (IOException ex)
            {
            storedEx = ex;
            localFile.delete();
            }
        finally
            {
            out.close();
            }

        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // if we failed to write the file, rethrow the exception
        if (storedEx != null)
            throw storedEx;
        }

    /***********************************************************************************************
     * Get as ASCII, i.e. read a line at a time and write
     * using the correct newline separator for the OS
     *
     * @param localPath  full path of local file to write to
     * @param remoteFile name of remote file
     * @throws FTPException
     * @throws IOException
     */
    private void getASCII(final String localPath, final String remoteFile)
        throws IOException, FTPException
        {

        // B.McKeown:
        // Call initGet() before creating the FileOutputStream.
        // This will prevent being left with an empty file if a FTPException
        // is thrown by initGet().
        initGet(remoteFile);

        // B. McKeown: Need to store the local file name so the file can be
        // deleted if necessary.
        final File localFile = new File(localPath);

        // create the buffered stream for writing
        final BufferedWriter out =
            new BufferedWriter(
                new FileWriter(localPath));

        // get an character input stream to read data from ... AFTER we
        // have the ok to go ahead AND AFTER we've successfully opened a
        // stream for the local file
        final LineNumberReader in =
            new LineNumberReader(
                new InputStreamReader(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // output a new line after each received newline
        IOException storedEx = null;
        long size = 0;
        long monitorCount = 0;
        try
            {
            int ch;
            while ((ch = in.read()) != -1 && !cancelTransfer)
                {
                size++;
                monitorCount++;
                if (ch == '\n')
                    out.newLine();
                else
                    out.write(ch);

                if (monitor != null && monitorCount > monitorInterval)
                    {
                    monitor.bytesTransferred(size);
                    monitorCount = 0;
                    }
                }
            }
        catch (IOException ex)
            {
            storedEx = ex;
            localFile.delete();
            }
        finally
            {
            out.close();
            }

        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // if we failed to write the file, rethrow the exception
        if (storedEx != null)
            throw storedEx;
        }

    /**
     * Get as ASCII, i.e. read a line at a time and write
     * using the correct newline separator for the OS
     *
     * @param destStream data stream to write data to
     * @param remoteFile name of remote file
     * @throws IOException
     * @throws FTPException
     */
    private void getASCII(final OutputStream destStream,
                          final String remoteFile) throws IOException, FTPException
        {
        initGet(remoteFile);

        // create the buffered stream for writing
        final BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(destStream));

        // get an character input stream to read data from ... AFTER we
        // have the ok to go ahead
        final LineNumberReader in =
            new LineNumberReader(
                new InputStreamReader(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // output a new line after each received newline
        IOException storedEx = null;
        long size = 0;
        long monitorCount = 0;
        try
            {
            int ch;
            while ((ch = in.read()) != -1 && !cancelTransfer)
                {
                size++;
                monitorCount++;
                if (ch == '\n')
                    out.newLine();
                else
                    out.write(ch);

                if (monitor != null && monitorCount > monitorInterval)
                    {
                    monitor.bytesTransferred(size);
                    monitorCount = 0;
                    }
                }
            }
        catch (IOException ex)
            {
            storedEx = ex;
            }
        finally
            {
            out.close();
            }

        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // if we failed to write the file, rethrow the exception
        if (storedEx != null)
            throw storedEx;
        }


    /***********************************************************************************************
     * Get as binary file without initGet(), i.e. straight transfer of data
     *
     * @param localPath  full path of local file to write to
     * @param remoteFile name of remote file
     *
     * @throws FTPException
     * @throws IOException
     */

    private void getBinaryGeneric(final String localPath,
                                  final String remoteFile) throws IOException, FTPException
        {
        // B. McKeown: Need to store the local file name so the file can be
        // deleted if necessary.
        final File localFile = new File(localPath);

        // create the buffered output stream for writing the file
        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localPath, false));

        // get an input stream to read data from ... AFTER we have
        // the ok to go ahead AND AFTER we've successfully opened a
        // stream for the local file
        final BufferedInputStream in = new BufferedInputStream(new DataInputStream(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // do the retrieving
        long size = 0;
        long monitorCount = 0;
        final int chunksize = CHUNK_SIZE;
        final byte[] chunk = new byte[chunksize];
        int count;
        IOException storedEx = null;

        // read from socket & write to file in chunks
        try
            {
            while ((count = in.read(chunk, 0, chunksize)) >= 0
                && !cancelTransfer)
                {
                //System.out.println("FTP retrieving chunk...");
                out.write(chunk, 0, count);
                size += count;
                monitorCount += count;

                if (monitor != null && monitorCount > monitorInterval)
                    {
                    monitor.bytesTransferred(size);
                    monitorCount = 0;
                    }
                }
            }
        catch (IOException ex)
            {
            storedEx = ex;
            localFile.delete();
            }
        finally
            {
            out.close();
            }

        // close streams
        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // if we failed to write the file, rethrow the exception
        if (storedEx != null)
            {
            throw storedEx;
            }

        // log bytes transferred
        control.log("Transferred " + size + " bytes from remote host");
        //System.out.println("Transferred " + size + " bytes from remote host");
        }


    /***********************************************************************************************
     * Get as binary file, i.e. straight transfer of data
     *
     * @param localPath  full path of local file to write to
     * @param remoteFile name of remote file
     *
     * @throws FTPException
     * @throws IOException
     */

    private void getBinary(final String localPath,
                           final String remoteFile) throws IOException, FTPException
        {
        // B.McKeown:
        // Call initGet() before creating the FileOutputStream.
        // This will prevent being left with an empty file if a FTPException
        // is thrown by initGet().

        // This sends the RETR command
        initGet(remoteFile);

        // B. McKeown: Need to store the local file name so the file can be
        // deleted if necessary.
        final File localFile = new File(localPath);

        // create the buffered output stream for writing the file
        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localPath, false));

        // get an input stream to read data from ... AFTER we have
        // the ok to go ahead AND AFTER we've successfully opened a
        // stream for the local file
        final BufferedInputStream in = new BufferedInputStream(new DataInputStream(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // do the retrieving
        long size = 0;
        long monitorCount = 0;
        final int chunksize = CHUNK_SIZE;
        final byte[] chunk = new byte[chunksize];
        int count;
        IOException storedEx = null;

        // read from socket & write to file in chunks
        try
            {
            while ((count = in.read(chunk, 0, chunksize)) >= 0
                && !cancelTransfer)
                {
                //System.out.println("FTP retrieving chunk...");
                out.write(chunk, 0, count);
                size += count;
                monitorCount += count;

                if (monitor != null && monitorCount > monitorInterval)
                    {
                    monitor.bytesTransferred(size);
                    monitorCount = 0;
                    }
                }
            }
        catch (IOException ex)
            {
            storedEx = ex;
            localFile.delete();
            }
        finally
            {
            out.close();
            }

        // close streams
        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // if we failed to write the file, rethrow the exception
        if (storedEx != null)
            {
            throw storedEx;
            }

        // log bytes transferred
        control.log("Transferred " + size + " bytes from remote host");
        //System.out.println("Transferred " + size + " bytes from remote host");
        }


    /***********************************************************************************************
     * Get as binary file, i.e. straight transfer of data
     *
     * @param destStream stream to write to
     * @param remoteFile name of remote file
     *
     * @throws IOException
     * @throws FTPException
     */
    private void getBinary(final OutputStream destStream,
                           final String remoteFile) throws IOException, FTPException
        {

        initGet(remoteFile);

        // create the buffered output stream for writing the file
        final BufferedOutputStream out =
            new BufferedOutputStream(destStream);

        // get an input stream to read data from ... AFTER we have
        // the ok to go ahead AND AFTER we've successfully opened a
        // stream for the local file
        final BufferedInputStream in =
            new BufferedInputStream(
                new DataInputStream(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // do the retrieving
        long size = 0;
        long monitorCount = 0;
        final int chunksize = CHUNK_SIZE;
        final byte[] chunk = new byte[chunksize];
        int count;
        IOException storedEx = null;

        // read from socket & write to file in chunks
        try
            {
            while ((count = in.read(chunk, 0, chunksize)) >= 0 && !cancelTransfer)
                {
                out.write(chunk, 0, count);
                size += count;
                monitorCount += count;

                if (monitor != null && monitorCount > monitorInterval)
                    {
                    monitor.bytesTransferred(size);
                    monitorCount = 0;
                    }
                }
            }
        catch (IOException ex)
            {
            storedEx = ex;
            }
        finally
            {
            out.close();
            }

        // close streams
        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        // if we failed to write to the stream, rethrow the exception
        if (storedEx != null)
            throw storedEx;

        // log bytes transferred
        control.log("Transferred " + size + " bytes from remote host");
        }


    /***********************************************************************************************
     * Get data from the FTP server as a byte array without initGet().
     * Transfers in whatever mode we are in.
     * Retrieve as a byte array.
     * Note that we may experience memory limitations as the entire file must be held in memory at one time.
     *
     * @param remoteFile name of remote file in current directory
     *
     * @return byte[]
     *
     * @throws IOException
     * @throws FTPException
     */

    public byte[] getBytesGeneric(final String remoteFile) throws IOException, FTPException
        {
        final BufferedInputStream in;

        // get an input stream to read data from
        in = new BufferedInputStream(new DataInputStream(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // do the retrieving
        long size = 0;
        long monitorCount = 0;
        final int chunksize = CHUNK_SIZE;
        final byte[] chunk = new byte[chunksize];  // read chunks into
        final byte[] resultBuf; // where we place result
        final ByteArrayOutputStream temp = new ByteArrayOutputStream(chunksize); // temp swap buffer
        int count;  // size of chunk read

        // read from socket & write to stream
        while ((count = in.read(chunk, 0, chunksize)) >= 0 && !cancelTransfer)
            {
            temp.write(chunk, 0, count);
            size += count;
            monitorCount += count;

            if (monitor != null && monitorCount > monitorInterval)
                {
                monitor.bytesTransferred(size);
                monitorCount = 0;
                }
            }

        temp.close();

        // get the bytes from the temp buffer
        resultBuf = temp.toByteArray();

        // close streams
        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        validateTransfer();

//        System.out.println("FTPClient.getBytesGeneric() retrieved " + resultBuf.length + " bytes");

        return resultBuf;
        }


    /***********************************************************************************************
     * Get data from the FTP server as a byte array.
     * Transfers in whatever mode we are in.
     * Retrieve as a byte array.
     * Note that we may experience memory limitations as the entire file must be held in memory at one time.
     *
     * @param remoteFile name of remote file in current directory
     *
     * @return byte[]
     *
     * @throws IOException
     * @throws FTPException
     */

    public byte[] get(final String remoteFile) throws IOException, FTPException
        {
        initGet(remoteFile);

        // get an input stream to read data from
        final BufferedInputStream in =
            new BufferedInputStream(
                new DataInputStream(data.getInputStream()));

        // B. McKeown:
        // If we are in active mode we have to set the timeout of the passive
        // socket. We can achieve this by calling setTimeout() again.
        // If we are in passive mode then we are merely setting the value twice
        // which does no harm anyway. Doing this simplifies any logic changes.
        data.setTimeout(timeout);

        // do the retrieving
        long size = 0;
        long monitorCount = 0;
        final int chunksize = CHUNK_SIZE;
        final byte[] chunk = new byte[chunksize];  // read chunks into
        final byte[] resultBuf; // where we place result
        final ByteArrayOutputStream temp =
            new ByteArrayOutputStream(chunksize); // temp swap buffer
        int count;  // size of chunk read

        // read from socket & write to file
        while ((count = in.read(chunk, 0, chunksize)) >= 0 && !cancelTransfer)
            {
            temp.write(chunk, 0, count);
            size += count;
            monitorCount += count;

            if (monitor != null && monitorCount > monitorInterval)
                {
                monitor.bytesTransferred(size);
                monitorCount = 0;
                }

            }
        temp.close();

        // get the bytes from the temp buffer
        resultBuf = temp.toByteArray();

        // close streams
        try
            {
            in.close();
            data.close();
            }
        catch (IOException ignore)
            {
            }

        validateTransfer();

        return resultBuf;
        }


    /**
     * Run a site-specific command on the
     * server. Support for commands is dependent
     * on the server
     *
     * @param command the site command to run
     * @return true if command ok, false if
     *         command not implemented
     * @throws FTPException
     * @throws IOException
     */
    public boolean site(final String command) throws IOException, FTPException
        {
        // send the retrieve command
        final String reply = control.sendCommand("SITE " + command);

        // Can get a 200 (ok) or 202 (not components). Some
        // FTP servers return 502 (not components)
        final String[] validCodes = {"200", "202", "502"};
        lastValidReply = control.validateReply(reply, validCodes);

        // return true or false? 200 is ok, 202/502 not
        // implemented
        return "200".equals(reply.substring(0, 3));
        }


    /**
     * List a directory's contents
     *
     * @param dirname the name of the directory (<b>not</b> a file mask)
     *
     * @return a string containing the line separated directory listing
     *
     * @throws IOException
     * @throws FTPException
     *
     * @deprecated As of FTP 1.1, replaced by {@link #dir(String)}
     */
    public String list(final String dirname) throws IOException, FTPException
        {
        return list(dirname, false);
        }


    /**
     * List a directory's contents as one string. A detailed
     * listing is available, otherwise just filenames are provided.
     * The detailed listing varies in details depending on OS and
     * FTP server.
     *
     * @param dirname the name of the directory(<b>not</b> a file mask)
     * @param full    true if detailed listing required
     *                false otherwise
     * @return a string containing the line separated
     *         directory listing
     * @throws IOException
     * @throws FTPException
     * @deprecated As of FTP 1.1, replaced by {@link #dir(String,boolean)}
     */

    public String list(final String dirname,
                       final boolean full) throws IOException, FTPException
        {
        final String[] list = dir(dirname, full);

        final StringBuffer result = new StringBuffer();
        final String sep = System.getProperty("line.separator");

        // loop thru results and make into one string
        for (int i = 0; i < list.length; i++)
            {
            result.append(list[i]);
            result.append(sep);
            }

        return result.toString();
        }

    /**
     * List current directory's contents as an array of strings of
     * filenames.
     *
     * @return an array of current directory listing strings
     *
     * @throws IOException
     * @throws FTPException
     */
    public String[] dir() throws IOException, FTPException
        {
        return dir(null, false);
        }

    /**
     * List a directory's contents as an array of strings of filenames.
     *
     * @param dirname name of directory(<b>not</b> a file mask)
     *
     * @return an array of directory listing strings
     *
     * @throws IOException
     * @throws FTPException
     */

    public String[] dir(final String dirname) throws IOException, FTPException
        {

        return dir(dirname, false);
        }


    /**
     * List a directory's contents as an array of strings. A detailed
     * listing is available, otherwise just filenames are provided.
     * The detailed listing varies in details depending on OS and
     * FTP server. Note that a full listing can be used on a file
     * name to obtain information about a file
     *
     * @param dirname name of directory (<b>not</b> a file mask)
     * @param full    true if detailed listing required
     *                false otherwise
     * @return an array of directory listing strings
     * @throws FTPException
     * @throws IOException
     */
    public String[] dir(final String dirname, final boolean full)
        throws IOException, FTPException
        {

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the retrieve command
        String command;
        if (full)
            {
            command = "LIST ";
            }
        else
            {
            command = "NLST ";
            }

        if (dirname != null)
            {
            command += dirname;
            }

        // some FTP servers bomb out if NLST has whitespace appended
        command = command.trim();
        String reply = control.sendCommand(command);

        // check the control response. wu-ftp returns 550 if the
        // directory is empty, so we handle 550 appropriately. Similarly
        // proFTPD returns 450
        final String[] validCodes1 = {"125", "150", "450", "550"};
        lastValidReply = control.validateReply(reply, validCodes1);

        // an empty array of files for 450/550
        String[] result = new String[0];

        // a normal reply ... extract the file list
        final String replyCode = lastValidReply.getReplyCode();
        if (!replyCode.equals("450") && !replyCode.equals("550"))
            {
            // get an character input stream to read data from .
            final LineNumberReader in =
                new LineNumberReader(
                    new InputStreamReader(data.getInputStream()));

            // read a line at a time
            final Vector lines = new Vector();
            String line;
            while ((line = in.readLine()) != null)
                {
                lines.add(line);
                }
            try
                {
                in.close();
                data.close();
                }
            catch (IOException ignore)
                {
                }

            // check the control response
            final String[] validCodes2 = {"226", "250"};
            reply = control.readReply();
            lastValidReply = control.validateReply(reply, validCodes2);

            // empty array is default
            if (!lines.isEmpty())
                result = (String[]) lines.toArray(result);
            }
        return result;
        }

    /**
     * Gets the latest valid reply from the server
     *
     * @return reply object encapsulating last valid server response
     */
    public FTPReply getLastValidReply()
        {
        return lastValidReply;
        }


    /**
     * Switch debug of responses on or off
     *
     * @param on true if you wish to have responses to
     *           the log stream, false otherwise
     */
    public void debugResponses(final boolean on)
        {
        control.debugResponses(on);
        }

    /**
     * Set the logging stream, replacing
     * stdout
     *
     * @param log the new logging stream
     */
    public void setLogStream(final PrintWriter log)
        {
        control.setLogStream(log);
        }

    /**
     * Get the current transfer type
     *
     * @return the current type of the transfer,
     *         i.e. BINARY or ASCII
     */
    public FTPTransferType getType()
        {
        return transferType;
        }

    /**
     * Set the transfer type
     *
     * @param type the transfer type to
     *             set the server to
     * @throws FTPException
     * @throws IOException
     */
    public void setType(final FTPTransferType type)
        throws IOException, FTPException
        {

        // determine the character to send
        String typeStr = FTPTransferType.ASCII_CHAR;
        if (type.equals(FTPTransferType.BINARY))
            typeStr = FTPTransferType.BINARY_CHAR;

        // send the command
        final String reply = control.sendCommand("TYPE " + typeStr);
        lastValidReply = control.validateReply(reply, "200");

        // record the type
        transferType = type;
        }


    /**
     * Delete the specified remote file
     *
     * @param remoteFile name of remote file to
     *                   delete
     * @throws IOException
     * @throws FTPException
     */
    public void delete(final String remoteFile)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("DELE " + remoteFile);
        lastValidReply = control.validateReply(reply, "250");
        }


    /**
     * Rename a file or directory
     *
     * @param from name of file or directory to rename
     * @param to   intended name
     * @throws IOException
     * @throws FTPException
     */
    public void rename(final String from, final String to)
        throws IOException, FTPException
        {

        String reply = control.sendCommand("RNFR " + from);
        lastValidReply = control.validateReply(reply, "350");

        reply = control.sendCommand("RNTO " + to);
        lastValidReply = control.validateReply(reply, "250");
        }


    /**
     * Delete the specified remote working directory
     *
     * @param dir name of remote directory to
     *            delete
     * @throws FTPException
     * @throws IOException
     */
    public void rmdir(final String dir)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("RMD " + dir);

        // some servers return 257, technically incorrect but
        // we cater for it ...
        final String[] validCodes = {"250", "257"};
        lastValidReply = control.validateReply(reply, validCodes);
        }


    /**
     * Create the specified remote working directory
     *
     * @param dir name of remote directory to
     *            create
     * @throws FTPException
     * @throws IOException
     */
    public void mkdir(final String dir)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("MKD " + dir);
        lastValidReply = control.validateReply(reply, "257");
        }


    /**
     * Change the remote working directory to
     * that supplied
     *
     * @param dir name of remote directory to
     *            change to
     * @throws IOException
     * @throws FTPException
     */
    public void chdir(final String dir) throws IOException, FTPException
        {
        final String reply;

        reply = control.sendCommand("CWD " + dir);
        lastValidReply = control.validateReply(reply, "250");
        }

    /**
     * Get modification time for a remote file
     *
     * @param remoteFile name of remote file
     * @return modification time of file as a date
     * @throws FTPException
     * @throws IOException
     */
    public Date modtime(final String remoteFile)
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("MDTM " + remoteFile);
        lastValidReply = control.validateReply(reply, "213");

        // parse the reply string ...
        final Date ts = tsFormat.parse(lastValidReply.getReplyText(),
                                       new ParsePosition(0));
        return ts;
        }

    /**
     * Get the current remote working directory
     *
     * @return the current working directory
     * @throws FTPException
     * @throws IOException
     */
    public String pwd()
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("PWD");
        lastValidReply = control.validateReply(reply, "257");

        // get the reply text and extract the dir
        // listed in quotes, if we can find it. Otherwise
        // just return the whole reply string
        final String text = lastValidReply.getReplyText();
        final int start = text.indexOf('"');
        final int end = text.lastIndexOf('"');
        if (start >= 0 && end > start)
            return text.substring(start + 1, end);
        else
            return text;
        }

    /**
     * Get the type of the OS at the server
     *
     * @return the type of server OS
     * @throws FTPException
     * @throws IOException
     */
    public String system()
        throws IOException, FTPException
        {

        final String reply = control.sendCommand("SYST");
        lastValidReply = control.validateReply(reply, "215");
        return lastValidReply.getReplyText();
        }

    /**
     * Get the help text for the specified command
     *
     * @param command name of the command to get help on
     * @return help text from the server for the supplied command
     * @throws IOException
     * @throws FTPException
     */
    public String help(final String command) throws IOException, FTPException
        {

        final String reply = control.sendCommand("HELP " + command);
        final String[] validCodes = {"211", "214"};
        lastValidReply = control.validateReply(reply, validCodes);
        return lastValidReply.getReplyText();
        }

    /**
     * Quit the FTP session
     *
     * @throws FTPException
     * @throws IOException
     */
    public void quit() throws IOException, FTPException
        {
        try
            {
            if (control != null)
                {
                final String reply = control.sendCommand("QUIT");
                final String[] validCodes = {"221", "226"};

                lastValidReply = FTPControlSocket.validateReply(reply, validCodes);
                }
            }

        finally
            {
            // ensure we clean up the connection
            if (control != null)
                {
                // This closes the Socket streams
                control.logout();
                control = null;
                }
            }
        }


    /***********************************************************************************************
     * Explicitly dispose of the FTPControlSocket.
     * Ideally call quit() first.
     */

    public void disposeSocket()
        {
        this.control = null;
        }
    }



