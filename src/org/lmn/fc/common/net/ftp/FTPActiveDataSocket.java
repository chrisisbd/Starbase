/**
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
 *        $Log: FTPActiveDataSocket.java,v $
 *        Revision 1.1  2003/11/02 21:49:52  bruceb
 *        implement FTPDataSocket interface
 *
 *
 */
package org.lmn.fc.common.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Active data socket handling class
 *
 * @author Bruce Blackshaw
 * @version $Revision: 1.1 $
 */
public final class FTPActiveDataSocket implements FTPDataSocket
    {

    /**
     * Revision control id
     */
    private static final String cvsId = "@(#)$Id: FTPActiveDataSocket.java,v 1.1 2003/11/02 21:49:52 bruceb Exp $";

    /**
     * The underlying socket for Active connection.
     */
    private final ServerSocket sock;

    /**
     * The socket accepted from server.
     */
    private Socket acceptedSock;

    /**
     * Constructor
     *
     * @param sock the server socket to use
     */
    FTPActiveDataSocket(final ServerSocket sock)
        {
        this.sock = sock;
        }


    /**
     * Set the TCP timeout on the underlying control socket.
     * <p/>
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException.
     *
     * @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(final int millis) throws SocketException
        {
        sock.setSoTimeout(millis);
        }


    /**
     * If active mode, accepts the FTP server's connection - in PASV,
     * we are already connected. Then gets the output stream of
     * the connection
     *
     * @return output stream for underlying socket.
     */
    public OutputStream getOutputStream() throws IOException
        {
        // accept socket from server
        acceptedSock = sock.accept();
        return acceptedSock.getOutputStream();
        }

    /**
     * If active mode, accepts the FTP server's connection - in PASV,
     * we are already connected. Then gets the input stream of
     * the connection
     *
     * @return input stream for underlying socket.
     */
    public InputStream getInputStream() throws IOException
        {
        // accept socket from server
        acceptedSock = sock.accept();
        return acceptedSock.getInputStream();
        }

    /**
     * Closes underlying sockets
     */
    public void close() throws IOException
        {
        if (acceptedSock != null)
            acceptedSock.close();
        sock.close();
        }

    }
