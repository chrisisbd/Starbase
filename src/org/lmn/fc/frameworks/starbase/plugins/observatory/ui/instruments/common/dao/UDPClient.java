// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * This class allows you to send and receive data via UDP
 * without concerning yourself with DatagramPackets
 * and DatagramSockets.
 *
 * @author Elliotte Rusty Harold
 * @version 2.0 of October 5, 1999
 */

public final class UDPClient
    {
    private final InetAddress inetRemoteAddress;
    private final int intLocalPort;
    private final int intRemotePort;
    private DatagramSocket datagramSocket;
    private final byte[] receiveBuffer;


    /**********************************************************************************************
     * Creates a new UDPClient.
     *
     * @param remoteHost The address of the remote host to which data will be sent
     * @param localport
     * @param remoteport
     * @param timeout    The number of milliseconds to wait to receive a packet before timing out
     *
     * @throws SocketException
     */

    public UDPClient(final InetAddress remoteHost,
                     final int localport,
                     final int remoteport,
                     final int timeout) throws SocketException
        {
        this.inetRemoteAddress = remoteHost;
        this.intLocalPort = localport;
        this.intRemotePort = remoteport;

        receiveBuffer = new byte[65507];

        // Constructs a datagram socket and binds it to the specified port
        // on the local host machine. The socket will be bound to the wildcard
        // address, an IP address chosen by the kernel.
        // In order to receive broadcast packets a DatagramSocket should be bound to the wildcard address
        datagramSocket = new DatagramSocket(intLocalPort);
        datagramSocket.setSoTimeout(timeout);
        }


    /***********************************************************************************************
     * Connect to the remote Port.
     * WARNING! Do not try to connect to the broadcast address!
     */

    public void connect()
        {
        if (datagramSocket != null)
            {
            // Remote Address and Remote Port
            datagramSocket.connect(inetRemoteAddress, intRemotePort);
            }
        }

    /**********************************************************************************************
     * This method sends data to the remote host via UDP. If the array
     * is longer than the maximum reliable length of a UDP Datagram
     * (8192) bytes then an IOException is thrown
     *
     * @param data A byte array containing the data to be sent
     *
     * @throws IOException
     */

    public void send(final byte[] data) throws IOException
        {
        final DatagramPacket datagramPacket;

        if (data.length > 8192)
            {
            throw new IOException("UDPClient.send() Too much data");
            }

        // Remote Address and Remote Port
        datagramPacket = new DatagramPacket(data,
                                            data.length,
                                            inetRemoteAddress,
                                            intRemotePort);

        // Sends a datagram packet from this socket. The
        // DatagramPacket includes information indicating the
        // data to be sent, its length, the IP address of the remote host,
        // and the port number on the remote host.
        datagramSocket.send(datagramPacket);
        }


    /**********************************************************************************************
     * This method blocks until a UDP Datagram is received.
     * This can be an indefinite amount of time if
     * the host is unreachable so calls to this method should
     * be placed in a separate thread from the main program.
     *
     * @return the data received as a byte array
     *
     * @throws IOException
     */

    public byte[] receive() throws IOException
        {
        final DatagramPacket incoming;
        final byte[] arrayResult;

        // Constructs a DatagramPacket for receiving packets
        incoming = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        // Receives a datagram packet from this socket. When this method
        // returns, the DatagramPacket's buffer is filled with
        // the data received. The datagram packet also contains the sender's
        // IP address, and the port number on the sender's machine.
        datagramSocket.receive(incoming);

        arrayResult = new byte[incoming.getLength()];
        System.arraycopy(incoming.getData(),
                         0,
                         arrayResult,
                         0,
                         incoming.getLength());

        return (arrayResult);
        }


    /**********************************************************************************************
     * This method blocks until a UDP Datagram is received.
     * This can be an indefinite amount of time if
     * the host is unreachable so calls to this method should
     * be placed in a separate thread from the main program.
     *
     * @return DatagramPacket
     *
     * @throws IOException
     */

    public DatagramPacket receiveDatagram() throws IOException
        {
        final DatagramPacket incoming;

        // Constructs a DatagramPacket for receiving packets
        incoming = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        // Receives a datagram packet from this socket. When this method
        // returns, the DatagramPacket's buffer is filled with
        // the data received. The datagram packet also contains the sender's
        // IP address, and the port number on the sender's machine.
        datagramSocket.receive(incoming);

        return (incoming);
        }


    /***********************************************************************************************
     * Close the DatagramSocket.
     */

    public void close()
        {
        if (datagramSocket != null)
            {
            datagramSocket.close();
            }
        }


    /***********************************************************************************************
     * Get the DatagramSocket.
     *
     * @return DatagramSocket
     */

    public DatagramSocket getSocket()
        {
        return (this.datagramSocket);
        }










    // just for testing via echo
//    public static void main(String[] args)
//        {
//
//        String hostname = "localhost";
//        int intRemotePort = 7;
//
//        if (args.length > 0)
//            {
//            hostname = args[0];
//            }
//        if (args.length > 1)
//            {
//            try
//                {
//                intRemotePort = Integer.parseInt(args[1]);
//                }
//            catch (Exception e)
//                {
//                }
//            }
//
//        try
//            {
//            InetAddress ia = InetAddress.getByName(hostname);
//            UDPClient client = new UDPClient(ia,
//                                             intRemotePort);
//            Random r = new Random();
//            for (int i = 1;
//                 i <= 8192;
//                 i++)
//                {
//                try
//                    {
//                    byte[] data = new byte[i];
//                    r.nextBytes(data);
//                    client.send(data);
//                    byte[] result = client.receive();
//                    if (result.length != data.length)
//                        {
//                        System.err.println("Packet " + i
//                                + " failed; input length: " + data.length + "; output length: " + result.length);
//                        continue;
//                        }
//                    for (int j = 0;
//                         j < result.length;
//                         j++)
//                        {
//                        if (data[j] != result[j])
//                            {
//                            System.err.println("Packet " + i + " failed; data mismatch");
//                            break;
//                            }
//                        }
//                    }
//                catch (IOException e)
//                    {
//                    System.err.println("Packet " + i + " failed with " + e);
//                    }
//
//                }
//            }
//        catch (UnknownHostException e)
//            {
//            System.err.println(e);
//            }
//        catch (SocketException se)
//            {
//            System.err.println(se);
//            }
//
//        }







    /**
     * This method sends an empty datagram to the remote host via UDP.
     *
     * @throws IOException
     */
//    public void send() throws
//                       IOException
//        {
//        byte[] b = new byte[1];
//        this.send(b);
//        }




//    /**
//     * @return the intRemotePort which this object sends data to
//     */
//    public int getPort()
//        {
//        return this.intRemotePort;
//        }
//
//
//    /**
//     * @return the intRemotePort which this client is bound to
//     */
//    public int getLocalPort()
//        {
//        return this.datagramSocket.getLocalPort();
//        }
//
//
//    /**
//     * @return the InetAddress which this client sends data to
//     */
//    public InetAddress getAddress()
//        {
//        return this.remote;
//        }


//    /**
//     * @return a String showing the remote host and intRemotePort
//     *         which this client sends data to
//     */
//    public String toString()
//        {
//        return "[UDPClient:address=" + remote + ";intRemotePort=" + intRemotePort + "]";
//        }

    }

/**
 *   Java Network Programming, Third Edition
 *   By Elliotte Rusty Harold
 *   Third Edition October 2004
 *   ISBN: 0-596-00721-3
 */

//    public UDPClient(InetAddress remoteHost,
//                     int remotePort) throws SocketException
//        {
//
//        this.remote = remoteHost;
//        this.port = remotePort;
//        ds = new DatagramSocket();
//        // the next line requires Java 2
//        ds.connect(remote, port);
//
//        }


//    public UDPClient(String hostname,
//                     int port)
//            throws
//            UnknownHostException,
//            SocketException
//        {
//        this(InetAddress.getByName(hostname),
//             port);
//        }

