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
 *        $Log: FTPPassiveDataSocket.java,v $
 *        Revision 1.1  2003/11/02 21:49:52  bruceb
 *        implement FTPDataSocket interface
 *
 *
 */
package org.lmn.fc.common.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Passive data socket handling class
 *
 * @author Bruce Blackshaw
 * @version $Revision: 1.1 $
 */
public final class FTPPassiveDataSocket implements FTPDataSocket
    {

    /**
     * The underlying socket
     */
    private final Socket sock;

    /**
     * Constructor
     *
     * @param sock client socket to use
     */
    FTPPassiveDataSocket(final Socket sock)
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
        return sock.getOutputStream();
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
        return sock.getInputStream();
        }

    /**
     * Closes underlying socket
     */
    public void close() throws IOException
        {
        sock.close();
        }
    }
