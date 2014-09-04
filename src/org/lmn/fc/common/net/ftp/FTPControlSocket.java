/**
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003 Enterprise Distributed Technologies Ltd
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
 *        $Log: FTPControlSocket.java,v $
 *        Revision 1.7  2003/11/02 21:50:14  bruceb
 *        changed FTPDataSocket to an interface
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
 *        Revision 1.1  2001/10/05 14:42:04  bruceb
 *        moved from old project
 *
 *
 */

package org.lmn.fc.common.net.ftp;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Supports client-side FTP operations
 *
 * @author Bruce Blackshaw
 * @version $Revision: 1.7 $
 */
public final class FTPControlSocket implements FrameworkConstants,
    FrameworkStrings
    {
    /**
     * Standard FTP end of line sequence
     */
    static final String EOL = "\r\n";

    /**
     * The control port number for FTP
     */
    public static final int CONTROL_PORT = 21;

    /**
     * Used to flag messages
     */
    private static final String DEBUG_ARROW = "---> ";

    /**
     * Start of password message
     */
    private static final String PASSWORD_MESSAGE = DEBUG_ARROW + "PASS";

    /**
     * Controls if responses sent back by the
     * server are sent to assigned output stream
     */
    private boolean debugResponses;

    /**
     * Output stream debug is written to,
     * stdout by default
     */
    private PrintWriter log;

    /**
     * The underlying socket.
     */
    private Socket controlSock;

    /**
     * The write that writes to the control socket
     */
    private Writer writer;

    /**
     * The reader that reads control data from the
     * control socket
     */
    private BufferedReader reader;

    /**
     * Constructor. Performs TCP connection and
     * sets up reader/writer. Allows different control
     * port to be used
     *
     * @param remoteHost  Remote hostname
     * @param controlPort port for control stream
     * @param log         the new logging stream
     * @param timeout
     * @throws FTPException
     * @throws IOException
     */
    public FTPControlSocket(final String remoteHost,
                            final int controlPort,
                            final PrintWriter log,
                            final int timeout) throws IOException, FTPException
        {
        setLogStream(log);

        // ensure we get debug from initial connection sequence if a
        // log stream is supplied
        if (log != null)
            debugResponses(true);
        controlSock = new Socket(remoteHost, controlPort);
        setTimeout(timeout);
        initStreams();
        validateConnection();

        // switch off debug - user can switch on from this point
        debugResponses(false);
        }

    /**
     * Constructor. Performs TCP connection and
     * sets up reader/writer. Allows different control
     * port to be used
     *
     * @param remoteAddr  Remote inet address
     * @param controlPort port for control stream
     * @param log         the new logging stream
     * @param timeout
     * @throws IOException
     * @throws FTPException
     */
    public FTPControlSocket(final InetAddress remoteAddr, final int controlPort,
                            final PrintWriter log, final int timeout)
        throws IOException, FTPException
        {

        setLogStream(log);

        // ensure we get debug from initial connection sequence if a
        // log stream is supplied
        if (log != null)
            debugResponses(true);
        controlSock = new Socket(remoteAddr, controlPort);
        setTimeout(timeout);
        initStreams();
        validateConnection();

        // switch off debug - user can switch on from this point
        debugResponses(false);
        }


    /**
     * Checks that the standard 220 reply is returned
     * following the initiated connection
     *
     * @throws IOException
     * @throws FTPException
     */
    private void validateConnection()
        throws IOException, FTPException
        {

        final String reply = readReply();
        validateReply(reply, "220");
        }


    /**
     * Obtain the reader/writer streams for this
     * connection
     *
     * @throws IOException
     */
    private void initStreams() throws IOException
        {
        // input stream
        final InputStream is = controlSock.getInputStream();
        reader = new BufferedReader(new InputStreamReader(is, "US-ASCII"));

        // output stream
        final OutputStream os = controlSock.getOutputStream();
        writer = new OutputStreamWriter(os);
        }


    /**
     * Get the name of the remote host
     *
     * @return remote host name
     */
    String getRemoteHostName()
        {
        final InetAddress addr = controlSock.getInetAddress();
        return addr.getHostName();
        }


    /**
     * Set the TCP timeout on the underlying control socket.
     * <p/>
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException.
     *
     * @param millis The length of the timeout, in milliseconds
     * @throws SocketException
     */
    void setTimeout(final int millis) throws SocketException
        {

        if (controlSock == null)
            throw new IllegalStateException(
                "Failed to set timeout - no control socket");

        controlSock.setSoTimeout(millis);
        }


    /**
     * Quit this FTP session and clean up.
     *
     * @throws IOException
     */
    public void logout() throws IOException
        {
        if (log != null)
            {
            log.flush();
            log = null;
            }

        IOException ex = null;
        try
            {
            writer.close();
            }
        catch (IOException e)
            {
            ex = e;
            }

        try
            {
            reader.close();
            }
        catch (IOException e)
            {
            ex = e;
            }

        try
            {
            controlSock.close();
            }
        catch (IOException e)
            {
            ex = e;
            }

        if (ex != null)
            {
            throw ex;
            }
        }


    /**
     * Request a data socket be created on the
     * server, connect to it and return our
     * connected socket.
     *
     * @param connectMode
     * @return connected data socket
     * @throws IOException
     * @throws FTPException
     */
    FTPDataSocket createDataSocket(final FTPConnectMode connectMode)
        throws IOException, FTPException
        {

        if (FTPConnectMode.ACTIVE.equals(connectMode))
            {
            return new FTPActiveDataSocket(createDataSocketActive());
            }
        else
            { // PASV
            return new FTPPassiveDataSocket(createDataSocketPASV());
            }
        }


    /**
     * Request a data socket be created on the Client
     * client on any free port, do not connect it to yet.
     *
     * @return not connected data socket
     * @throws IOException
     * @throws FTPException
     */
    private ServerSocket createDataSocketActive()
        throws IOException, FTPException
        {

        // use any available port
        final ServerSocket socket = new ServerSocket(0);

        // get the local address to which the control socket is bound.
        final InetAddress localhost = controlSock.getLocalAddress();

        // send the PORT command to the server
        setDataPort(localhost, (short) socket.getLocalPort());

        return socket;
        }


    /**
     * Helper method to convert a byte into an unsigned short value
     *
     * @param value value to convert
     * @return the byte value as an unsigned short
     */
    private static short toUnsignedShort(final byte value)
        {
        if ((value < 0))
            {
            return (short) (value + 256);
            }
        else
            {
            return (short) value;
            }
        }

    /**
     * Convert a short into a byte array
     *
     * @param value value to convert
     * @return a byte array
     */
    private static byte[] toByteArray(final short value)
        {

        final byte[] bytes = new byte[2];
        bytes[0] = (byte) (value >> 8);     // bits 1- 8
        bytes[1] = (byte) (value & 0x00FF); // bits 9-16
        return bytes;
        }


    /**
     * Sets the data port on the server, i.e. sends a PORT
     * command
     *
     * @param host   the local host the server will connect to
     * @param portNo the port number to connect to
     * @throws FTPException
     * @throws IOException
     */
    private void setDataPort(final InetAddress host, final short portNo)
        throws IOException, FTPException
        {

        final byte[] hostBytes = host.getAddress();
        final byte[] portBytes = toByteArray(portNo);

        // assemble the PORT command
        final String cmd = new StringBuffer("PORT ")
            .append(toUnsignedShort(hostBytes[0])).append(",")
            .append(toUnsignedShort(hostBytes[1])).append(",")
            .append(toUnsignedShort(hostBytes[2])).append(",")
            .append(toUnsignedShort(hostBytes[3])).append(",")
            .append(toUnsignedShort(portBytes[0])).append(",")
            .append(toUnsignedShort(portBytes[1])).toString();

        // send command and check reply
        final String reply = sendCommand(cmd);
        validateReply(reply, "200");
        }


    /**
     * Request a data socket be created on the
     * server, connect to it and return our
     * connected socket.
     *
     * @return connected data socket
     * @throws IOException
     * @throws FTPException
     */
    private Socket createDataSocketPASV()
        throws IOException, FTPException
        {

        // PASSIVE command - tells the server to listen for
        // a connection attempt rather than initiating it
        final String reply = sendCommand("PASV");
        validateReply(reply, "227");

        // The reply to PASV is in the form:
        // 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2).
        // where h1..h4 are the IP address to connect and
        // p1,p2 the port number
        // Example:
        // 227 Entering Passive Mode (128,3,122,1,15,87).
        // NOTE: PASV command in IBM/Mainframe returns the string
        // 227 Entering Passive Mode 128,3,122,1,15,87	(missing
        // brackets)

        // extract the IP data string from between the brackets
        int startIP = reply.indexOf('(');
        int endIP = reply.indexOf(')');

        // allow for IBM missing brackets around IP address
        if (startIP < 0 && endIP < 0)
            {
            startIP = reply.toUpperCase().lastIndexOf("MODE") + 4;
            endIP = reply.length();
            }

        final String ipData = reply.substring(startIP + 1, endIP);
        final int[] parts = new int[6];

        final int len = ipData.length();
        int partCount = 0;
        final StringBuffer buf = new StringBuffer();

        // loop thru and examine each char
        for (int i = 0; i < len && partCount <= 6; i++)
            {

            final char ch = ipData.charAt(i);
            if (Character.isDigit(ch))
                buf.append(ch);
            else if (ch != ',')
                {
                throw new FTPException("Malformed PASV reply: " + reply);
                }

            // get the part
            if (ch == ',' || i + 1 == len)
                { // at end or at separator
                try
                    {
                    parts[partCount++] = Integer.parseInt(buf.toString());
                    buf.setLength(0);
                    }
                catch (NumberFormatException ex)
                    {
                    throw new FTPException("Malformed PASV reply: " + reply);
                    }
                }
            }

        // assemble the IP address
        // we try connecting, so we don't bother checking digits etc
        final String ipAddress = parts[0] + "." + parts[1] + "." +
            parts[2] + "." + parts[3];

        // assemble the port number
        final int port = (parts[4] << 8) + parts[5];

        // create the socket
        return new Socket(ipAddress, port);
        }


    /**
     * Send a command to the FTP server and
     * return the server's reply
     *
     * @param command
     * @return reply to the supplied command
     * @throws IOException
     */
    String sendCommand(final String command) throws IOException
        {
        String strReply;

        log(DEBUG_ARROW + command);
        //System.out.println(DEBUG_ARROW + command);

        strReply = "";

        // send it
        if (writer != null)
            {
            writer.write(command + EOL);
            //System.out.println("after writer write");
            writer.flush();
            //System.out.println("after writer flush");

            // and read the result
            strReply = readReply();
            //System.out.println("reply=" + strReply);
            }
        else
            {
            //System.out.println("NULL WRITER");
            }

        return (strReply);
        }

    /**
     * Read the FTP server's reply to a previously
     * issued command. RFC 959 states that a reply
     * consists of the 3 digit code followed by text.
     * The 3 digit code is followed by a hyphen if it
     * is a muliline response, and the last line starts
     * with the same 3 digit code.
     *
     * @return reply string
     * @throws IOException
     */
    String readReply() throws IOException
        {
        final String firstLine = reader.readLine();

        if (firstLine == null || firstLine.length() == 0)
            {
            throw new IOException("Unexpected null reply received");
            }

        final StringBuffer reply = new StringBuffer(firstLine);

        log(reply.toString());

        final String replyCode = reply.toString().substring(0, 3);

        // check for multiline response and build up
        // the reply
        if (reply.charAt(3) == '-')
            {
            boolean complete = false;

            while (!complete)
                {
                final String line = reader.readLine();

                if (line == null)
                    {
                    throw new IOException("Unexpected null reply received");
                    }

                log(line);

                if (line.length() > 3 &&
                    line.substring(0, 3).equals(replyCode) &&
                    line.charAt(3) == ' ')
                    {
                    reply.append(line.substring(3));
                    complete = true;
                    }
                else
                    { // not the last line
                    reply.append(SPACE);
                    reply.append(line);
                    }
                } // end while
            } // end if

        //System.out.println("END OF readReply");
        return reply.toString();
        }


    /**
     * Validate the response the host has supplied against the
     * expected reply. If we get an unexpected reply we throw an
     * exception, setting the message to that returned by the
     * FTP server
     *
     * @param reply             the entire reply string we received
     * @param expectedReplyCode the reply we expected to receive
     * @return
     * @throws FTPException
     */
    static FTPReply validateReply(final String reply, final String expectedReplyCode)
        throws FTPException
        {

        // all reply codes are 3 chars long
        final String replyCode = reply.substring(0, 3);
        final String replyText = reply.substring(4);
        final FTPReply replyObj = new FTPReply(replyCode, replyText);

        if (replyCode.equals(expectedReplyCode))
            return replyObj;

        // if unexpected reply, throw an exception
        throw new FTPException(replyText, replyCode);
        }

    /**
     * Validate the response the host has supplied against the
     * expected reply. If we get an unexpected reply we throw an
     * exception, setting the message to that returned by the
     * FTP server
     *
     * @param reply              the entire reply string we received
     * @param expectedReplyCodes array of expected replies
     *
     * @return an object encapsulating the server's reply
     *
     * @throws FTPException
     */
    static FTPReply validateReply(final String reply,
                                  final String[] expectedReplyCodes) throws FTPException
        {
        // all reply codes are 3 chars long
        final String replyCode = reply.substring(0, 3);
        final String replyText = reply.substring(4);
//        System.out.println("replyCode={" + replyCode + "}");
//        System.out.println("replyText={" + replyText + "}");

        final FTPReply replyObj = new FTPReply(replyCode, replyText);

        for (int i = 0; i < expectedReplyCodes.length; i++)
            if (replyCode.equals(expectedReplyCodes[i]))
                return replyObj;

        // got this far, not recognised
        //System.out.println("got this far, not recognised");
        throw new FTPException(replyText, replyCode);
        }


    /**
     * Switch debug of responses on or off
     *
     * @param on true if you wish to have responses to
     *           stdout, false otherwise
     */
    void debugResponses(final boolean on)
        {
        debugResponses = on;
        }

    /**
     * Set the logging stream, replacing
     * stdout. If null log supplied, logging is
     * switched off
     *
     * @param log the new logging stream
     */
    void setLogStream(final PrintWriter log)
        {
        this.log = log;
        }


    /**
     * Log a message, if logging is set up
     *
     * @param msg message to log
     */
    void log(final String msg)
        {
        if (debugResponses && log != null)
            {
            if (!msg.startsWith(PASSWORD_MESSAGE))
                log.println(msg);
            else
                log.println(PASSWORD_MESSAGE + " ********");
            }
        }

    }


