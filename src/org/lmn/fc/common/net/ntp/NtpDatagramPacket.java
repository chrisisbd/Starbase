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

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * This class encapsulates a ntp-datagram as described in rfc2030. Such a datagram consists of a
 * header and four timestamps. The four timestamps are respectively: <UL> <LI> The reference
 * timestamp. This indicates when the local clock was last set. Can be set to zero for datagrams
 * originating on the client. <LI> The originate timestamp. Indicates when the datagram originated
 * on the client. Copied by the server from the transmit timestamp. Can be set to zero for datagrams
 * originating on the client. <LI> The receive timestamp. Indicates when the reply left the server.
 * Can be set to zero for datagrams originating on the client. <LI> The transmit timestamp.
 * Indicates when the datagram departed. </UL> We have added a fifth timestamp. Namely a 'reception
 * timestamp' which is normally set by NtpConnection.receive(NtpDatagramPacket). When transmitted a
 * ntp-datagram is wrapped in a UDP datagram.
 *
 * @author Michel Van den Bergh
 * @version 1.0
 * @see TimeStamp
 * @see NtpHeader
 * @see org.lmn.fc.common.net.ntp.NtpConnection
 */

public final class NtpDatagramPacket
    {
    private static final int headerOffset = 0;
    private static final int referenceTimeStampOffset = 16;
    private static final int originateTimeStampOffset = 24;
    private static final int receiveTimeStampOffset = 32;
    private static final int transmitTimeStampOffset = 40;
    private static final int ntpDatagramLength = 48;

    private final DatagramPacket dp;
    private TimeStamp receptionTimeStamp;


    /**
     * Construct a NtpDatagram from a header, four timestamps, an Inetaddress and a portnumber.
     *
     * @see InetAddress
     */
    private NtpDatagramPacket(final NtpHeader header, final TimeStamp referenceTimeStamp,
                             final TimeStamp originateTimeStamp, final TimeStamp receiveTimeStamp,
                             final TimeStamp transmitTimeStamp, final InetAddress iaddr,
                             final int iport)
        {
        byte[] temp;
        final byte[] buffer = new byte[ntpDatagramLength];
        for (int i = headerOffset; i < referenceTimeStampOffset; i++)
            {
            buffer[i] = (header.getData())[i - headerOffset];
            }
        for (int i = referenceTimeStampOffset; i < originateTimeStampOffset; i++)
            {
            temp = referenceTimeStamp.getData();
            buffer[i] = temp[i - referenceTimeStampOffset];
            }
        for (int i = originateTimeStampOffset; i < receiveTimeStampOffset; i++)
            {
            temp = originateTimeStamp.getData();
            buffer[i] = temp[i - originateTimeStampOffset];
            }
        for (int i = receiveTimeStampOffset; i < transmitTimeStampOffset; i++)
            {
            temp = receiveTimeStamp.getData();
            buffer[i] = temp[i - receiveTimeStampOffset];
            }
        for (int i = transmitTimeStampOffset; i < ntpDatagramLength; i++)
            {
            temp = transmitTimeStamp.getData();
            buffer[i] = temp[i - transmitTimeStampOffset];
            }
        dp = new DatagramPacket(buffer, ntpDatagramLength, iaddr, iport);
        }

    /**
     * Construct a NtpDatagram with only the transmit timestamp filled in (set to the current time).
     * The header is set to a NtpHeader.DEFAULT_HEADER.
     *
     * @see NtpHeader
     */

    public NtpDatagramPacket(final InetAddress iaddr, final int iport)
        {
        this(NtpHeader.defaultHeader, TimeStamp.zero, TimeStamp.zero,
             TimeStamp.zero, new TimeStamp(), iaddr, iport);
        }

    /**
     * Constructs an uninitialized NtpDatagram.
     */
    public NtpDatagramPacket()
        {
        final byte[] buffer = new byte[ntpDatagramLength];
        dp = new DatagramPacket(buffer, ntpDatagramLength);
        }

    /**
     * Constructs an uninitialized NtpDatagram from a UDP datagram.
     */
    public NtpDatagramPacket(final DatagramPacket dp)
        {
        this.dp = dp;
        }

    /**
     * Returns the UDP datagram associated to an NtpDatagram.
     */
    DatagramPacket getDatagramPacket()
        {
        return dp;
        }

    /**
     * Returns the header associated to a NtpDatagram.
     *
     * @see NtpHeader
     */

    private NtpHeader getHeader()
        {
        final byte[] buffer = dp.getData();
        final byte[] temp = new byte[16];
        for (int i = headerOffset; i < referenceTimeStampOffset; i++)
            {
            temp[i - headerOffset] = buffer[i];
            }
        return new NtpHeader(temp);
        }

    /**
     * Returns the reference timestamp.
     */
    private TimeStamp getReferenceTimeStamp()
        {
        final byte[] buffer = dp.getData();
        final byte[] temp = new byte[8];
        for (int i = referenceTimeStampOffset; i < originateTimeStampOffset; i++)
            {
            temp[i - referenceTimeStampOffset] = buffer[i];
            }
        return new TimeStamp(temp);
        }

    /**
     * Returns the originate timestamp
     */

    private TimeStamp getOriginateTimeStamp()
        {
        final byte[] buffer = dp.getData();
        final byte[] temp = new byte[8];
        for (int i = originateTimeStampOffset; i < receiveTimeStampOffset; i++)
            {
            temp[i - originateTimeStampOffset] = buffer[i];
            }
        return new TimeStamp(temp);
        }

    /**
     * Returns the receive timestamp
     */
    private TimeStamp getReceiveTimeStamp()
        {
        final byte[] buffer = dp.getData();
        final byte[] temp = new byte[8];
        for (int i = receiveTimeStampOffset; i < transmitTimeStampOffset; i++)
            {
            temp[i - receiveTimeStampOffset] = buffer[i];
            }
        return new TimeStamp(temp);
        }

    /**
     * Returns the transmit timestamp
     */

    private TimeStamp getTransmitTimeStamp()
        {
        final byte[] buffer = dp.getData();
        final byte[] temp = new byte[8];
        for (int i = transmitTimeStampOffset; i < ntpDatagramLength; i++)
            {
            temp[i - transmitTimeStampOffset] = buffer[i];
            }
        return new TimeStamp(temp);
        }

    /**
     * Returns the reception timestamp
     */

    private TimeStamp getReceptionTimeStamp()
        {
        return receptionTimeStamp;
        }

    void setReceptionTimeStamp(final TimeStamp receptionTimeStamp)
        {
        this.receptionTimeStamp = receptionTimeStamp;
        }

    /**
     * A convenience method which returns the useful information contained in a NtpDatagram.
     *
     * @see NtpData
     */
    public NtpData getInfo()
        {
        final NtpData dataNtp = new NtpData();
        final NtpHeader h = getHeader();

        dataNtp.serverAddress = dp.getAddress();
        dataNtp.leapYearIndicator = h.getLeapYearIndicator();
        dataNtp.versionNumber = h.getVersionNumber();
        dataNtp.stratum = h.getStratum();
        dataNtp.mode = h.getMode();
        dataNtp.pollInterval = h.getPollInterval();
        dataNtp.precision = h.getPrecision();
        dataNtp.rootDelay = h.getRootDelay();
        dataNtp.rootDispersion = h.getRootDispersion();
        dataNtp.referenceIdentifier = h.getReferenceIdentifier();
        dataNtp.referenceTimeStamp = getReferenceTimeStamp();

        final long originate = getOriginateTimeStamp().getTime().getTime();
        final long receive = getReceiveTimeStamp().getTime().getTime();
        final long transmit = getTransmitTimeStamp().getTime().getTime();
        final long reception = getReceptionTimeStamp().getTime().getTime();

        dataNtp.roundTripDelay = receive - originate + reception - transmit;
        dataNtp.offset = (receive - originate - reception + transmit) / 2;

        return dataNtp;
        }

    public String toString()
        {
        String s;
        s = "Header : ";
        s = s + getHeader();
        s = s + "\n";
        s = s + "ReferenceTimeStamp : ";
        s = s + getReferenceTimeStamp();
        s = s + "\n";
        s = s + "OriginateTimeStamp : ";
        s = s + getOriginateTimeStamp();
        s = s + "\n";
        s = s + "ReceiveTimeStamp : ";
        s = s + getReceiveTimeStamp();
        s = s + "\n";
        s = s + "TransmitTimeStamp : ";
        s = s + getTransmitTimeStamp();
        return s;
        }
    }



