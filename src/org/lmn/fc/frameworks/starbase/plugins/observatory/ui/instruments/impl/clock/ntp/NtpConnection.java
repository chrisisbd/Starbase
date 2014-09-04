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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ntp;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Locale;
import java.util.TimeZone;


/***********************************************************************************************
 * This class encapsulates the exchange of a NtpDatagram between a client and a server.
 */

public final class NtpConnection
    {
    public static final String DEFAULT_NTP_SERVER = "uk.pool.ntp.org";

    private static final int DEFAULT_NTP_PORT = 123;

    // Injections
    private final InetAddress ntpServer;
    private final TimeZone timeZone;
    private final Locale locale;
    private final ObservatoryClockInterface clock;

    private final int ntpPort;
    private final DatagramSocket datagramSocket;
    private int timeoutMillis;


    /***********************************************************************************************
     * Creates a UDP connection for the exchange of NtpDatagrams.
     *
     * @param iaddr
     * @param obstimezone
     * @param obslocale
     * @param obsclock
     *
     * @throws SocketException
     */

    public NtpConnection(final InetAddress iaddr,
                         final TimeZone obstimezone,
                         final Locale obslocale,
                         final ObservatoryClockInterface obsclock) throws SocketException
        {
        ntpServer = iaddr;
        timeZone = obstimezone;
        locale = obslocale;
        clock = obsclock;

        ntpPort = DEFAULT_NTP_PORT;
        timeoutMillis = 10000;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(timeoutMillis);
        }


    /***********************************************************************************************
     * Get the timeout associated with the connection. The default timeout is 10s.
     *
     * @return int
     */

    public int getTimeoutMillis()
        {
        return (this.timeoutMillis);
        }


    /***********************************************************************************************
     * Set the timeout associated with the connection.
     *
     * @param timeout
     *
     * @throws SocketException
     */

    public void setTimeoutMillis(final int timeout) throws SocketException
        {
        this.timeoutMillis = timeout;
        datagramSocket.setSoTimeout(timeout);
        }


    /***********************************************************************************************
     * Send a NtpDatagram to the server.
     *
     * @param packet
     *
     * @throws IOException
     */

    private void send(final NtpDatagramPacket packet) throws IOException
        {
        datagramSocket.send(packet.getDatagramPacket());
        }


    /***********************************************************************************************
     * Wait for a reply from the server.
     *
     * @param packet
     *
     * @throws IOException
     */

    private void receive(final NtpDatagramPacket packet) throws IOException
        {
        datagramSocket.receive(packet.getDatagramPacket());
        packet.setReceptionTimeStamp(new NtpTimeStamp(timeZone, locale, clock));
        }


    /***********************************************************************************************
     * Obtain info from the server.
     *
     * @return NtpData
     *
     * @throws IOException
     */

    public NtpData getNtpData() throws IOException
        {
        final NtpDatagramPacket dpSend;
        final NtpDatagramPacket dpReceive;

        dpSend = new NtpDatagramPacket(ntpServer, ntpPort, timeZone, locale, clock);
        dpReceive = new NtpDatagramPacket(timeZone, locale, clock);
        send(dpSend);
        receive(dpReceive);

        return (dpReceive.getInfo());
        }


    /***********************************************************************************************
     * Close the connection.
     */

    public void close()
        {
        datagramSocket.close();
        }
    }
