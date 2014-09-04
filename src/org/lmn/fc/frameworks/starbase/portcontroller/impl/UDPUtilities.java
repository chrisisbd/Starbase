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

package org.lmn.fc.frameworks.starbase.portcontroller.impl;

import java.io.IOException;
import java.net.*;
import java.nio.channels.IllegalBlockingModeException;


/***************************************************************************************************
 * UDPUtilities.
 */

public final class UDPUtilities
    {
    public static final int MAX_UDP_PAYLOAD = 8192;/* DatagramSocket

                                                                               */
    /***********************************************************************************************
     * Connect to the remote Port.
     *
     * @param socket
     * @param address
     * @param port
     *
     * @throws IllegalArgumentException
     * @throws SecurityException
     */

    public static void connectSocket(final DatagramSocket socket,
                                     final InetAddress address,
                                     final int port) throws IllegalArgumentException,
                                                            SecurityException
        {
        if ((socket != null)
            && (address != null))
            {
            socket.connect(address, port);
            }
        }


    /***********************************************************************************************
     * Send a byte array to the DatagramSocket.
     *
     * @param socket
     * @param address
     * @param port
     * @param data
     *
     * @throws IOException
     * @throws SecurityException
     * @throws PortUnreachableException
     * @throws IllegalBlockingModeException
     */

    public static void send(final DatagramSocket socket,
                            final InetAddress address,
                            final int port,
                            final byte[] data) throws SecurityException,
                                                      PortUnreachableException,
                                                      IllegalBlockingModeException,
                                                      IOException
        {
        if ((socket != null)
            && (address != null)
            && (data != null)
            && (data.length > 0))
            {
            final DatagramPacket packet;

            packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            }
        }


    /***********************************************************************************************
     * This method blocks until a UDP Datagram is received.
     * This can be an indefinite amount of time if
     * the host is unreachable so calls to this method should
     * be placed in a separate thread from the main program.
     *
     * @param socket
     *
     * @return byte[]
     *
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws PortUnreachableException
     * @throws IllegalBlockingModeException
     */

    public static byte[] receive(final DatagramSocket socket) throws SocketTimeoutException,
                                                                     PortUnreachableException,
                                                                     IllegalBlockingModeException,
                                                                     IOException
        {
        byte[] arrayResult;

        arrayResult = new byte[10];

        if (socket != null)
            {
            final DatagramPacket incomingPacket;
            final byte[] receiveBuffer;

            receiveBuffer = new byte[MAX_UDP_PAYLOAD];

            incomingPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            // This method blocks until a UDP Datagram is received
            socket.receive(incomingPacket);

            // If we get here, it must have worked
            arrayResult = new byte[incomingPacket.getLength()];
            System.arraycopy(incomingPacket.getData(),
                             0,
                             arrayResult,
                             0,
                             incomingPacket.getLength());
            }

        return (arrayResult);
        }


    /***********************************************************************************************
     * Close the DatagramSocket.
     *
     * @param socket
     */

    public static void closeSocket(final DatagramSocket socket)
        {
        if (socket != null)
            {
            socket.disconnect();
            socket.close();
            }
        }
    }
