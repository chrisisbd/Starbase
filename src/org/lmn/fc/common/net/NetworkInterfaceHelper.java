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


import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**************************************************************************************************
 * NetworkInterfaceHelper.
 * With thanks to: http://web.archiveorange.com/archive/v/9zwtbZAkSzOXBSu2TcLT
 */

public final class NetworkInterfaceHelper
    {
    /**********************************************************************************************
     * We must use -Djava.net.preferIPv4Stack=true to get expected results
     * for broadcast address and prefix length.
     *
     * @throws SocketException
     */

    public static StringBuffer getInterfaceDetails() throws SocketException
        {
        final StringBuffer buffer;
        final Enumeration<NetworkInterface> interfaces;

        buffer = new StringBuffer();

        // Returns all the interfaces on this machine.
        // Returns null if no network interfaces could be found on this machine.
        for (interfaces = NetworkInterface.getNetworkInterfaces();
             interfaces.hasMoreElements(); )
            {
            final NetworkInterface networkInterface;

            networkInterface = interfaces.nextElement();

            if ((networkInterface.isUp())
                && (!networkInterface.isVirtual()))
                {
                showInterface(networkInterface, buffer);
                }
            }

        return (buffer);
        }


    /***********************************************************************************************
     * Show all details of a Network Interface in the specified StringBuffer.
     * May be called recursively.
     *
     * @param networkinterface
     * @param buffer
     *
     * @throws SocketException
     */

    private static void showInterface(final NetworkInterface networkinterface,
                                      final StringBuffer buffer) throws SocketException
        {
        final byte[] arrayMAC;
        final Enumeration<NetworkInterface> interfaces;

        // Get a List of all or a subset of the InterfaceAddresses of this network interface
        // Get an Enumeration with all the subinterfaces (also known as virtual interfaces) attached to this network interface
        if ((networkinterface.getInterfaceAddresses().isEmpty())
            && (!networkinterface.getSubInterfaces().hasMoreElements()))
            {
            return;    // has no addresses or child interfaces
            }

        //    [displayname=MS TCP Loopback interface] [loopback=true]
        //    [address=127.0.0.1] [loopback=true] [broadcast=127.255.255.255] [prefixlength=8] [host=localhost]
        //    [displayname=Dell Wireless 1390 WLAN Mini-Card - Packet Scheduler Miniport] [macaddress=:0:19:7d:60:15:94]
        //    [address=192.168.1.64] [broadcast=192.168.1.255] [prefixlength=24] [host=TRISHLAPTOP.home]

        buffer.append("[interface] [displayname=");
        buffer.append(networkinterface.getDisplayName());
        buffer.append("]");

        // Returns whether a network interface is a loopback interface
        if (networkinterface.isLoopback())
            {
            buffer.append(" [loopback=true]");
            }
        else
            {
            buffer.append(" [loopback=false]");
            }

        // Returns whether a network interface is a point to point interface
        if (networkinterface.isPointToPoint())
            {
            buffer.append(" [ptp=true]");
            }
        else
            {
            buffer.append(" [ptp=false]");
            }

        // Returns the hardware address (usually MAC) of the interface if it has one,
        // and if it can be accessed given the current privileges
        arrayMAC = networkinterface.getHardwareAddress();

        if ((arrayMAC != null)
            && (arrayMAC.length > 0))
            {
            buffer.append(" [macaddress=");

            for (int intMACIndex = 0;
                 intMACIndex < arrayMAC.length;
                 intMACIndex++)
                {
                final byte byteMAC;

                if (intMACIndex > 0)
                    {
                    buffer.append(':');
                    }

                byteMAC = arrayMAC[intMACIndex];
                buffer.append(Integer.toHexString(byteMAC & 0xff));
                }

            buffer.append("]");
            }

        buffer.append(System.getProperty("line.separator"));

        // Now the InterfaceAddress information
        for (final InterfaceAddress interfaceAddress : networkinterface.getInterfaceAddresses())
            {
            buffer.append(" [address=");
            // Returns an InetAddress for this address
            buffer.append(interfaceAddress.getAddress());
            buffer.append("]");

            if (interfaceAddress.getAddress().isLoopbackAddress())
                {
                buffer.append(" [loopback=true]");
                }
            else
                {
                buffer.append(" [loopback=false]");
                }

            buffer.append(" [broadcast=");
            buffer.append(interfaceAddress.getBroadcast());
            buffer.append("]");

            buffer.append(" [prefixlength=");
            buffer.append(interfaceAddress.getNetworkPrefixLength());
            buffer.append("]");

            //System.out.print(", searchedBroadcast=");
            //System.out.print(BroadcastAddress.instance().getBroadcastAddress(addr.getAddress()));

            try
                {
                final String strHostname;

                strHostname = interfaceAddress.getAddress().getCanonicalHostName();

                if (strHostname != null)
                    {
                    buffer.append(" [host=");
                    buffer.append(strHostname);
                    buffer.append("]");
                    }
                }

            catch (Exception exception)
                {
                // Just ignore any Exceptions
                }

            buffer.append(System.getProperty("line.separator"));
            }

        // Now recurse down through the subinterfaces

        for (interfaces = networkinterface.getSubInterfaces();
             interfaces.hasMoreElements(); )
            {
            final NetworkInterface childInterface;

            childInterface = interfaces.nextElement();

            if (childInterface.isUp())
                {
                showInterface(childInterface, buffer);
                }
            }
        }


    /*********************************************************************************************/
    /* The original version                                                                      */
    /**********************************************************************************************
     * We must use -Djava.net.preferIPv4Stack=true to get expected results
     * for broadcast address and prefix length.
     *
     * @throws SocketException
     */

    public static void enumerateInterfaces() throws SocketException
        {
        final Enumeration<NetworkInterface> interfaces;

        // Returns all the interfaces on this machine.
        // Returns null if no network interfaces could be found on this machine.
        for (interfaces = NetworkInterface.getNetworkInterfaces();
             interfaces.hasMoreElements(); )
            {
            final NetworkInterface networkInterface;

            networkInterface = interfaces.nextElement();

            if ((networkInterface.isUp())
                && (!networkInterface.isVirtual()))
                {
                // Begin with no indent
                reportInterface(networkInterface, "");
                }
            }
        }


    /***********************************************************************************************
     * Show all details of a Network Interface.
     * May be called recursively.
     *
     * @param networkinterface
     * @param indent
     *
     * @throws SocketException
     */

    private static void reportInterface(final NetworkInterface networkinterface,
                                        String indent) throws SocketException
        {
        final byte[] arrayMAC;
        final Enumeration<NetworkInterface> interfaces;

        // Get a List of all or a subset of the InterfaceAddresses of this network interface
        // Get an Enumeration with all the subinterfaces (also known as virtual interfaces) attached to this network interface
        if ((networkinterface.getInterfaceAddresses().isEmpty())
            && (!networkinterface.getSubInterfaces().hasMoreElements()))
            {
            return;    // has no addresses or child interfaces
            }

        System.out.print(indent);
        System.out.print("/");
        System.out.print(networkinterface.getDisplayName());
        System.out.print("/");

        // Returns whether a network interface is a loopback interface
        if (networkinterface.isLoopback())
            {
            System.out.print(" [loopback]");
            }

        // Returns whether a network interface is a point to point interface
        if (networkinterface.isPointToPoint())
            {
            System.out.print(" [ptp]");
            }

        // Returns the hardware address (usually MAC) of the interface if it has one
        // and if it can be accessed given the current privileges
        arrayMAC = networkinterface.getHardwareAddress();

        if ((arrayMAC != null)
            && (arrayMAC.length > 0))
            {
            System.out.print(", hardware=");

            for (final byte byteMAC : arrayMAC)
                {
                System.out.print(':');
                System.out.print(Integer.toHexString(byteMAC & 0xff));
                }
            }

        System.out.println();
        indent = indent + "  ";

        // Now the InterfaceAddress information
        for (final InterfaceAddress interfaceAddress : networkinterface.getInterfaceAddresses())
            {
            System.out.print(indent);
            System.out.print(interfaceAddress.getAddress());

            // Returns an InetAddress for this address
            if (interfaceAddress.getAddress().isLoopbackAddress())
                {
                System.out.print(" [loopback]");
                }

            System.out.print(", broadcast=");
            System.out.print(interfaceAddress.getBroadcast());

            System.out.print(", prefixLength=");
            System.out.print(interfaceAddress.getNetworkPrefixLength());

            //System.out.print(", searchedBroadcast=");
            //System.out.print(BroadcastAddress.instance().getBroadcastAddress(addr.getAddress()));

            try
                {
                final String strHostname;

                strHostname = interfaceAddress.getAddress().getCanonicalHostName();

                if (strHostname != null)
                    {
                    System.out.print(", host=");
                    System.out.print(strHostname);
                    }
                }

            catch (Exception exception)
                {
                // Just ignore any Exceptions
                }

            System.out.println();
            }

        // Now recurse down through the subinterfaces

        for (interfaces = networkinterface.getSubInterfaces();
             interfaces.hasMoreElements(); )
            {
            final NetworkInterface childInterface;

            childInterface = interfaces.nextElement();

            if (childInterface.isUp())
                {
                reportInterface(childInterface, indent);
                }
            }
        }
    }

