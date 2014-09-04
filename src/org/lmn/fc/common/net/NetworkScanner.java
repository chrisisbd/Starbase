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

package org.lmn.fc.common.net;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.misc.Semaphore;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOWrapper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.UDPClient;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerData;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerHelper;
import org.lmn.fc.model.logging.EventStatus;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.List;


/**************************************************************************************************
 * NetworkScanner.
 * ToDo: Implement a ThreadPool.
 * See: http://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking-thread-pool-executor.html
 */

public final class NetworkScanner implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons
    {
    private static final int MAX_ADDRESSES = 256;


    /***********************************************************************************************
     * Scan the network between the specified IP addresses, on the specified target port.
     * Update the Semaphore and UI buttons when the thread completes.
     * Does not check that all addresses are on the same subnet.
     *
     * @param instrument
     * @param startip
     * @param endip
     * @param sourceport
     * @param targetport
     * @param timeout
     * @param semaphore
     * @param rescanbutton
     * @param stopbutton
     * @param disposebutton
     */

    public static void scanNetwork(final ObservatoryInstrumentInterface instrument,
                                   final long startip,
                                   final long endip,
                                   final int sourceport,
                                   final int targetport,
                                   final int timeout,
                                   final Semaphore semaphore,
                                   final JButton rescanbutton,
                                   final JButton stopbutton,
                                   final JButton disposebutton)
        {
        final String SOURCE = "NetworkScanner.scanNetwork() ";
        final SwingWorker workerScan;

        workerScan = new SwingWorker(FrameworkSingletons.REGISTRY.getThreadGroup(), SOURCE)
            {
            /*******************************************************************************
             * Construct the Scanner.
             *
             * @return Object
             */

            public Object construct()
                {
                final List<NetworkScannerData> listScannerData;

                // Create the List of scan data which will appear on the scan report
                listScannerData = new ArrayList<NetworkScannerData>(MAX_ADDRESSES);

                if ((instrument != null)
                    && (instrument.getDAO() != null)
                    && (instrument.getInstrumentPanel() != null))
                    {
                    final DAOWrapperInterface daoWrapper;
                    UDPClient clientUDP;

                    // Pass the data if any back via the DAO and its Wrapper
                    daoWrapper = new DAOWrapper(null, null, EMPTY_STRING, instrument.getDAO());
                    daoWrapper.setUserObject(listScannerData);

                    // Use the Semaphore to check that we are still running,
                    // to save time if the User has stopped the Instrument

                    for (long longIPIndex = startip;
                         ((longIPIndex <= endip)
                          && (Utilities.workerCanProceed(instrument.getDAO(), this))
                          && (semaphore.getState()));
                         longIPIndex++)
                        {
                        InetAddress inetAddress;
                        DatagramPacket datagramPacket;

                        inetAddress = null;
                        datagramPacket = null;
                        clientUDP = null;

                        try
                            {
                            final String strIPAddress;

                            strIPAddress = NetworkScannerHelper.longToIPv4(longIPIndex);
                            inetAddress = InetAddress.getByName(strIPAddress);
                            clientUDP = new UDPClient(inetAddress, sourceport, targetport, timeout);

                            // Send a simple meaningless message, to see if we get an ErrorResponse
                            clientUDP.connect();
                            clientUDP.send("?".getBytes());

                            // This method blocks until a UDP Datagram is received
                            // This Command is being executed on its own SwingWorker, so this doesn't matter...
                            datagramPacket = clientUDP.receiveDatagram();

                            // The NetworkScannerData List is the DAO UserObject
                            listScannerData.add(new NetworkScannerData(longIPIndex,
                                                                       targetport,
                                                                       inetAddress,
                                                                       Chronos.getCalendarDateNow(),
                                                                       EventStatus.INFO,
                                                                       "Connected"));

                            // Always update the data regardless of visibility
                            // NOTE! We don't want to use the Metadata to update the Toolbar state,
                            // since there's no need to change anything
                            instrument.getInstrumentPanel().setWrappedData(daoWrapper, true, false);

                            clientUDP.close();
                            }

                        catch (SocketTimeoutException exception)
                            {
                            // Ignore and move on
                            listScannerData.add(new NetworkScannerData(longIPIndex,
                                                                       targetport,
                                                                       null,
                                                                       Chronos.getCalendarDateNow(),
                                                                       EventStatus.FATAL,
                                                                       "Timeout"));

                            // Always update the data regardless of visibility
                            instrument.getInstrumentPanel().setWrappedData(daoWrapper, true, false);
                            }

                        catch (PortUnreachableException exception)
                            {
                            // Ignore and move on
                            // The hostname should be cached in this case?
                            listScannerData.add(new NetworkScannerData(longIPIndex,
                                                                       targetport,
                                                                       inetAddress,
                                                                       Chronos.getCalendarDateNow(),
                                                                       EventStatus.WARNING,
                                                                       "Unreachable Port"));

                            // Always update the data regardless of visibility
                            instrument.getInstrumentPanel().setWrappedData(daoWrapper, true, false);
                            }

                        catch (UnknownHostException exception)
                            {
                            listScannerData.add(new NetworkScannerData(longIPIndex,
                                                                       targetport,
                                                                       null,
                                                                       Chronos.getCalendarDateNow(),
                                                                       EventStatus.WARNING,
                                                                       "Unknown Host"));

                            // Always update the data regardless of visibility
                            instrument.getInstrumentPanel().setWrappedData(daoWrapper, true, false);
                            }

                        catch (SecurityException exception)
                            {
                            listScannerData.add(new NetworkScannerData(longIPIndex,
                                                                       targetport,
                                                                       null,
                                                                       Chronos.getCalendarDateNow(),
                                                                       EventStatus.WARNING,
                                                                       "Security Exception"));

                            // Always update the data regardless of visibility
                            instrument.getInstrumentPanel().setWrappedData(daoWrapper, true, false);
                            }

                        catch (IllegalArgumentException exception)
                            {
                            LOGGER.error(SOURCE + "IllegalArgumentException [exception=" + exception.getMessage() + "]");
                            }

                        catch (SocketException exception)
                            {
                            LOGGER.error(SOURCE + "SocketException [exception=" + exception.getMessage() + "]");
                            }

                        catch (IOException exception)
                            {
                            LOGGER.error(SOURCE + "IOException [exception=" + exception.getMessage() + "]");
                            }

                        finally
                            {
                            if (clientUDP != null)
                                {
                                clientUDP.close();
                                }
                            }
                        }
                    }

                return (listScannerData);
                }


            /***********************************************************************************
             * When the Thread stops.
             */

            public void finished()
                {
                if ((get() != null)
                    && (get() instanceof List))
                    {
                    // Only change the toolbar UI state when the thread has stopped!
                    semaphore.setState(false);
                    rescanbutton.setEnabled(NetworkScannerHelper.isValidRangeAndPort(startip,
                                                                                     endip,
                                                                                     targetport));
                    stopbutton.setEnabled(false);
                    disposebutton.setEnabled(true);

                    // We can now lose the UserObject because the Report has been built,
                    // and cannot change
                    if ((instrument != null)
                        && (instrument.getDAO() != null))
                        {
                        // Help the gc?
                        instrument.getDAO().setUserObject(null);
                        }
                    }
                }
            };

        workerScan.start();
        }


    /***********************************************************************************************
     * Scan the Network specified by the Classless Inter-Domain Routing address.
     *
     * @param cidrIp
     */

    public static void scanNetwork(final String cidrIp)
        {
        final long[] arrayIPBounds;

        arrayIPBounds = rangeFromCidr(cidrIp);

        for (long longIPIndex = arrayIPBounds[0];
             longIPIndex <= arrayIPBounds[1];
             longIPIndex++)
            {
            connectToIP(longIPIndex, DEFAULT_STARINET_UDP_PORT, 500);
            }
        }


    /***********************************************************************************************
     * Connect to the specified IP address, as an integer.
     *
     * @param longip
     * @param port
     * @param timeout
     */

    public static void connectToIP(final long longip,
                                   final int port,
                                   final int timeout)
        {
        try
            {
            final String strIPAddress;
            final InetAddress inetAddress;
            final Socket socket;
            final SocketAddress socketAddress;

            strIPAddress = NetworkScannerHelper.longToIPv4(longip);
            inetAddress = InetAddress.getByName(strIPAddress);

            System.out.println("\n-----------------------------------------");
            System.out.println("Trying: " + strIPAddress);

            // Create a Socket to try to connect to the Starinet remote port for this address
            socket = new Socket();

            // Remote Address and Remote Port
            socketAddress = new InetSocketAddress(inetAddress, port);
            socket.connect(socketAddress, timeout);

            // Creates a stream socket and connects it to the specified
            // port number at the specified IP address.
//            Socket serverSocket = new Socket(ip, DEFAULT_STARINET_UDP_PORT);
//            serverSocket.setSoTimeout(200);

//            SocketAddress sockaddr = new InetSocketAddress(host, port);
//            Socket sock = new Socket();
//            sock.connect(sockaddr, 5000);

            System.out.println("-------------------------");
            System.out.println("Server Socket Information");
            System.out.println("-------------------------");
            System.out.println("serverSocket         : " + socket);
            System.out.println("Keep Alive           : " + socket.getKeepAlive());
            System.out.println("Receive Buffer Size  : " + socket.getReceiveBufferSize());
            System.out.println("Send Buffer Size     : " + socket.getSendBufferSize());
            System.out.println("Is Socket Bound?     : " + socket.isBound());
            System.out.println("Is Socket Connected? : " + socket.isConnected());
            System.out.println("Is Socket Closed?    : " + socket.isClosed());
            System.out.println("So Timeout           : " + socket.getSoTimeout());
            System.out.println("So Linger            : " + socket.getSoLinger());
            System.out.println("TCP No Delay         : " + socket.getTcpNoDelay());
            System.out.println("Traffic Class        : " + socket.getTrafficClass());
            System.out.println("Socket Channel       : " + socket.getChannel());
            System.out.println("Reuse Address?       : " + socket.getReuseAddress());
            System.out.println("\n");

            // --------------------------------------
            // Get (Server) InetAddress / Socket Information
            // --------------------------------------
            InetAddress inetAddrServer = socket.getInetAddress();

            System.out.println("---------------------------");
            System.out.println("Remote (Server) Information");
            System.out.println("---------------------------");
            System.out.println("InetAddress - (Structure) : " + inetAddrServer);
            System.out.println("Socket Address - (Remote) : " + socket.getRemoteSocketAddress());
            System.out.println("Canonical Name            : " + inetAddrServer.getCanonicalHostName());
            System.out.println("Host Name                 : " + inetAddrServer.getHostName());
            System.out.println("Host Address              : " + inetAddrServer.getHostAddress());
            System.out.println("Port                      : " + socket.getPort());

            System.out.print("RAW IP Address - (byte[]) : ");
            byte[] b1 = inetAddrServer.getAddress();
            for (int i=0; i< b1.length; i++) {
            if (i > 0) {System.out.print(".");}
            System.out.print(b1[i] & 0xff);
            }
            System.out.println();

            System.out.println("Is Loopback Address?      : " + inetAddrServer.isLoopbackAddress());
            System.out.println("Is Multicast Address?     : " + inetAddrServer.isMulticastAddress());
            System.out.println("\n");

            // ---------------------------------------------
            // Get (Client) InetAddress / Socket Information
            // ---------------------------------------------
            InetAddress inetAddrClient = socket.getLocalAddress();

            System.out.println("--------------------------");
            System.out.println("Local (Client) Information");
            System.out.println("--------------------------");
            System.out.println("InetAddress - (Structure) : " + inetAddrClient);
            System.out.println("Socket Address - (Local)  : " + socket.getLocalSocketAddress());
            System.out.println("Canonical Name            : " + inetAddrClient.getCanonicalHostName());
            System.out.println("Host Name                 : " + inetAddrClient.getHostName());
            System.out.println("Host Address              : " + inetAddrClient.getHostAddress());
            System.out.println("Port                      : " + socket.getLocalPort());

            System.out.print("RAW IP Address - (byte[]) : ");
            byte[] b2 = inetAddrClient.getAddress();

            for (int i=0; i< b2.length; i++) {
            if (i > 0) {System.out.print(".");}
            System.out.print(b2[i] & 0xff);
            }
            System.out.println();

            System.out.println("Is Loopback Address?      : " + inetAddrClient.isLoopbackAddress());
            System.out.println("Is Multicast Address?     : " + inetAddrClient.isMulticastAddress());
            System.out.println("\n");

            //socket.close();
            }

        catch (UnknownHostException exception)
            {
            System.out.println("UnknownHost");
            }

        catch (SocketTimeoutException exception)
            {
            System.out.println("Timeout");
            }

        catch (IllegalArgumentException exception)
            {
            exception.printStackTrace();
            }

        catch (IllegalBlockingModeException exception)
            {
            exception.printStackTrace();
            }

        catch (SecurityException exception)
            {
            exception.printStackTrace();
            }

        catch (SocketException exception)
            {
            exception.printStackTrace();
            }

        catch (IOException exception)
            {
            exception.printStackTrace();
            }
        }


    /***********************************************************************************************
     * Classless Inter-Domain Routing (CIDR) is an Internet Protocol (IP)
     * address allocation and route aggregation methodology.
     * CIDR notation is constructed from the IP address and the prefix size, the latter being
     * equivalent to the number of leading 1 bits in the routing prefix mask.
     * The IP address is expressed according to the standards of IPv4 or IPv6.
     * It is followed by a separator character, the slash (/) character,
     * and the prefix size expressed as a decimal number.
     * The maximum size of the network is given by the number of addresses that are possible
     * with the remaining, least-significant bits below the prefix.
     * This is often called the host identifier.
     * The CIDR notation 198.51.100.0/24 used to be written as 198.51.100.0/255.255.255.0.
     *
     * See: http://en.wikipedia.org/wiki/CIDR_notation
     *
     * Return the Lower and Upper address bounds of the network.
     *
     * @param cidrIp
     *
     * @return int[]
     */

    public static long[] rangeFromCidr(final String cidrIp)
        {
        final int maskStub;
        final String[] arrayAtoms;
        final int intMask;
        final long[] arrayBounds;

        maskStub = 1 << 31;
        arrayAtoms = cidrIp.split("/");
        intMask = Integer.parseInt(arrayAtoms[1]);

        System.out.println(intMask);

        arrayBounds = new long[2];


        try
            {
            arrayBounds[0] = NetworkScannerHelper.ipV4ToLong(arrayAtoms[0]) & (maskStub >> (intMask - 1)); // lower bound
            arrayBounds[1] = NetworkScannerHelper.ipV4ToLong(arrayAtoms[0]); // upper bound
            }

        catch (UnknownHostException e)
            {
            arrayBounds[0] = 0;
            arrayBounds[1] = 1;
            }

        System.out.println("Lower bound=" + NetworkScannerHelper.longToIPv4(arrayBounds[0]));
        System.out.println("Upper bound=" + NetworkScannerHelper.longToIPv4(arrayBounds[1]));

        return (arrayBounds);
        }
    }
