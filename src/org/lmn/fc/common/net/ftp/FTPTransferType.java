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
 *        $Log: FTPTransferType.java,v $
 *        Revision 1.4  2002/11/19 22:01:25  bruceb
 *        changes for 1.2
 *
 *        Revision 1.3  2001/10/09 20:54:08  bruceb
 *        No change
 *
 *        Revision 1.1  2001/10/05 14:42:04  bruceb
 *        moved from old project
 *
 *
 */

package org.lmn.fc.common.net.ftp;

/**
 * Enumerates the transfer types possible. We
 * support only the two common types, ASCII and
 * Image (often called binary).
 *
 * @author Bruce Blackshaw
 * @version $Revision: 1.4 $
 */
public final class FTPTransferType
    {

    /**
     * Represents ASCII transfer type
     */
    public static final FTPTransferType ASCII = new FTPTransferType();

    /**
     * Represents Image (or binary) transfer type
     */
    public static final FTPTransferType BINARY = new FTPTransferType();

    /**
     * The char sent to the server to set ASCII
     */
    static final String ASCII_CHAR = "A";

    /**
     * The char sent to the server to set BINARY
     */
    static final String BINARY_CHAR = "I";

    /**
     * Private so no-one else can instantiate this class
     */
    private FTPTransferType()
        {
        }
    }
