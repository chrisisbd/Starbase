//////////////////////license & copyright header///////////////////////
//                                                                   //
//                Copyright (c) 1999 by Michel Van den Bergh         //
//                                                                   //
// This library is free software; you can redistribute it and/or     //
// modify it under the terms of the GNU Lesser General Public        //
// License as published by the Free Software Foundation; either      //
// version 2 of the License, or (at your option) any later version.  //
//                                                                   //
// This library is distributed in the hope that it will be useful,   //
// but WITHOUT ANY WARRANTY; without even the implied warranty of    //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU //
// Lesser General Public License for more details.                   //
//                                                                   //
// You should have received a copy of the GNU Lesser General Public  //
// License along with this library; if not, write to the             //
// Free Software Foundation, Inc., 59 Temple Place, Suite 330,       //
// Boston, MA  02111-1307  USA, or contact the author:               //
//                                                                   //
//                  Michel Van den Bergh  <vdbergh@luc.ac.be>        //
//                                                                   //
////////////////////end license & copyright header/////////////////////


package org.lmn.fc.common.net.ntp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.Vector;


/**
 * This class encapsulates the exchange of a NtpDatagram between a client and a server.
 */


public final class NtpConnection
    {
    public static final String DEFAULT_NTP_SERVER = "uk.pool.ntp.org";
    private static final int defaultNtpPort = 123;

    private final InetAddress ntpServer;
    private final int ntpPort;
    private final DatagramSocket datagramSocket;
    private final int maxHops = 15;
    private int timeout = 10000;

    /**
     * Creates a UDP connection for the exchange of NtpDatagrams.
     */
    public NtpConnection(final InetAddress iaddr, final int iport) throws SocketException
        {
        ntpServer = iaddr;
        ntpPort = iport;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(timeout);
        }

    /**
     * Creates a UDP connection for the exchange of NtpDatagrams.
     */
    public NtpConnection(final InetAddress iaddr) throws SocketException
        {
        ntpServer = iaddr;
        ntpPort = defaultNtpPort;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(timeout);
        }

    /**
     * Get the timeout associated with the connection. The default timeout is 10s.
     */
    public int getTimeout()
        {
        return timeout;
        }

    /**
     * Set the timeout associated with the connection.
     */
    public void setTimeout(final int timeout) throws SocketException
        {
        this.timeout = timeout;
        datagramSocket.setSoTimeout(timeout);
        }

    /**
     * Send a NtpDatagram to the server.
     */
    private void send(final NtpDatagramPacket ntpDatagramPacket) throws IOException
        {
        datagramSocket.send(ntpDatagramPacket.getDatagramPacket());
        }

    /**
     * Wait for a reply from the server.
     *
     * @throws java.io.IOException A IOException is thrown in case of a timeout.
     */
    private void receive(final NtpDatagramPacket ntpDatagramPacket) throws IOException
        {
        datagramSocket.receive(ntpDatagramPacket.getDatagramPacket());
        ntpDatagramPacket.setReceptionTimeStamp(new TimeStamp(new Date()));
        }

    /**
     * Obtain info from the server.
     *
     * @throws java.io.IOException A IOException is thrown in case of a timeout.
     * @see NtpData
     */
    public NtpData getNtpData() throws IOException
        {
        final NtpDatagramPacket dpSend =
            new NtpDatagramPacket(ntpServer, ntpPort);
        final NtpDatagramPacket dpReceive = new NtpDatagramPacket();
        send(dpSend);
        receive(dpReceive);
        return dpReceive.getInfo();
        }

    /**
     * Traces a server to the primary server.
     *
     * @return Vector containing the NtpData objects associated with the servers on the path to the
     *         primary server. Sometimes only a partial list will be generated due to timeouts or
     *         other problems.
     */
    public Vector getTrace()
        {
        final Vector traceList = new Vector();
        int hops = 0;
        boolean finished = false;
        NtpConnection currentNtpConnection = this;

        while ((!finished) && (hops < maxHops))
            {
            try
                {
                final NtpData dataNtp = currentNtpConnection.getNtpData();
                if (currentNtpConnection != this)
                    {
                    currentNtpConnection.close();
                    }
                traceList.addElement(dataNtp);
                if (dataNtp.referenceIdentifier instanceof InetAddress)
                    {
                    currentNtpConnection =
                        new NtpConnection((InetAddress) dataNtp.referenceIdentifier);
                    hops++;
                    }
                else
                    {
                    finished = true;
                    }
                }
            catch (Exception e)
                {
                finished = true;
                }
            }
        return traceList;
        }


    /**
     * Get the time from the server.
     *
     * @return A Date object containing the server time, adjusted for roundtrip delay. Note that it
     *         is better to use getNtpData() and then to use the offset field of the returned NtpData
     *         object.
     */

    // WARNING - which clock do you want to use?!
//    public Date getTime()
//        {
//        try
//            {
//            final long offset = getNtpData().offset;
//            return new Date(Chronos.getSystemTimeMillis() + offset);
//            }
//        catch (Exception e)
//            {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//            return null;
//            }
//        }

    /**
     * Close the connection.
     */
    public void close()
        {
        datagramSocket.close();
        }

    /**
     * Could be used to do some cleaning up. The default implementation just invokes close().
     */
    public void finalize()
        {
        close();
        }
    }
