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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.TimeZone;


/***************************************************************************************************
 * NtpDatagramPacket.
 *
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
 */

public final class NtpDatagramPacket
    {
    private static final int OFFSET_HEADER = 0;
    private static final int OFFSET_TIMESTAMP_REFERENCE = 16;
    private static final int OFFSET_TIMESTAMP_ORIGINATE = 24;
    private static final int OFFSET_TIMESTAMP_RECEIVE = 32;
    private static final int OFFSET_TIMESTAMP_TRANSMIT = 40;
    private static final int NTP_DATAGRAM_LENGTH = 48;

    // Injections
    private final TimeZone timeZone;
    private final Locale locale;
    private final ObservatoryClockInterface clock;

    private final DatagramPacket datagramPacket;
    private NtpTimeStamp receptionNtpTimeStamp;


    /***********************************************************************************************
     * Construct a NtpDatagram with only the transmit timestamp filled in (set to the current time).
     * The header is set to a NtpHeader.DEFAULT_HEADER.
     *
     * @param iaddr
     * @param iport
     * @param obstimezone
     * @param obslocale
     * @param obsclock
     */

    public NtpDatagramPacket(final InetAddress iaddr,
                             final int iport,
                             final TimeZone obstimezone,
                             final Locale obslocale,
                             final ObservatoryClockInterface obsclock)
        {
        this(NtpHeader.DEFAULT_HEADER,
             new NtpTimeStamp(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}),
             new NtpTimeStamp(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}),
             new NtpTimeStamp(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}),
             new NtpTimeStamp(obstimezone, obslocale, obsclock),
             iaddr,
             iport,
             obstimezone,
             obslocale,
             obsclock);
        }


    /***********************************************************************************************
     * Constructs an uninitialized NtpDatagram.
     *
     * @param obstimezone
     * @param obslocale
     * @param obsclock
     */

    public NtpDatagramPacket(final TimeZone obstimezone,
                             final Locale obslocale,
                             final ObservatoryClockInterface obsclock)
        {
        final byte[] buffer;

        buffer = new byte[NTP_DATAGRAM_LENGTH];
        datagramPacket = new DatagramPacket(buffer, NTP_DATAGRAM_LENGTH);

        timeZone = obstimezone;
        locale = obslocale;
        clock = obsclock;
        }


    /***********************************************************************************************
     * Construct a NtpDatagram from a header, four timestamps, an Inetaddress and a portnumber.
     *
     * @param header
     * @param referenceNtpTimeStamp
     * @param originateNtpTimeStamp
     * @param receiveNtpTimeStamp
     * @param transmitNtpTimeStamp
     * @param iaddr
     * @param iport
     * @param obstimezone
     * @param obslocale
     * @param obsclock
     */

    private NtpDatagramPacket(final NtpHeader header,
                              final NtpTimeStamp referenceNtpTimeStamp,
                              final NtpTimeStamp originateNtpTimeStamp,
                              final NtpTimeStamp receiveNtpTimeStamp,
                              final NtpTimeStamp transmitNtpTimeStamp,
                              final InetAddress iaddr,
                              final int iport,
                              final TimeZone obstimezone,
                              final Locale obslocale,
                              final ObservatoryClockInterface obsclock)
        {
        final byte[] buffer;
        byte[] temp;

        timeZone = obstimezone;
        locale = obslocale;
        clock = obsclock;

        buffer = new byte[NTP_DATAGRAM_LENGTH];

        for (int i = OFFSET_HEADER;
             i < OFFSET_TIMESTAMP_REFERENCE;
             i++)
            {
            buffer[i] = (header.getData())[i - OFFSET_HEADER];
            }

        for (int i = OFFSET_TIMESTAMP_REFERENCE;
             i < OFFSET_TIMESTAMP_ORIGINATE;
             i++)
            {
            temp = referenceNtpTimeStamp.getByteArray();
            buffer[i] = temp[i - OFFSET_TIMESTAMP_REFERENCE];
            }

        for (int i = OFFSET_TIMESTAMP_ORIGINATE;
             i < OFFSET_TIMESTAMP_RECEIVE;
             i++)
            {
            temp = originateNtpTimeStamp.getByteArray();
            buffer[i] = temp[i - OFFSET_TIMESTAMP_ORIGINATE];
            }

        for (int i = OFFSET_TIMESTAMP_RECEIVE;
             i < OFFSET_TIMESTAMP_TRANSMIT;
             i++)
            {
            temp = receiveNtpTimeStamp.getByteArray();
            buffer[i] = temp[i - OFFSET_TIMESTAMP_RECEIVE];
            }

        for (int i = OFFSET_TIMESTAMP_TRANSMIT;
             i < NTP_DATAGRAM_LENGTH;
             i++)
            {
            temp = transmitNtpTimeStamp.getByteArray();
            buffer[i] = temp[i - OFFSET_TIMESTAMP_TRANSMIT];
            }

        datagramPacket = new DatagramPacket(buffer,
                                            NTP_DATAGRAM_LENGTH,
                                            iaddr,
                                            iport);
        }


    /***********************************************************************************************
     * Returns the UDP datagram associated with an NtpDatagram.
     *
     * @return DatagramPacket
     */

    public DatagramPacket getDatagramPacket()
        {
        return (this.datagramPacket);
        }


    /***********************************************************************************************
     * Returns the header associated with a NtpDatagram.
     *
     * @return NtpHeader
     */

    private NtpHeader getHeader()
        {
        final byte[] buffer;
        final byte[] temp;

        buffer = datagramPacket.getData();
        temp = new byte[16];

        for (int i = OFFSET_HEADER;
             i < OFFSET_TIMESTAMP_REFERENCE;
             i++)
            {
            temp[i - OFFSET_HEADER] = buffer[i];
            }

        return new NtpHeader(temp);
        }


    /***********************************************************************************************
     * Returns the reference timestamp.
     *
     * @return NtpTimeStamp
     */

    private NtpTimeStamp getReferenceTimeStamp()
        {
        final byte[] buffer;
        final byte[] arrayTemp;

        buffer = datagramPacket.getData();
        arrayTemp = new byte[8];

        for (int i = OFFSET_TIMESTAMP_REFERENCE;
             i < OFFSET_TIMESTAMP_ORIGINATE;
             i++)
            {
            arrayTemp[i - OFFSET_TIMESTAMP_REFERENCE] = buffer[i];
            }

        return (new NtpTimeStamp(arrayTemp));
        }


    /***********************************************************************************************
     * Returns the originate timestamp.
     *
     * @return NtpTimeStamp
     */

    private NtpTimeStamp getOriginateTimeStamp()
        {
        final byte[] buffer;
        final byte[] arrayTemp;

        buffer = datagramPacket.getData();
        arrayTemp = new byte[8];

        for (int i = OFFSET_TIMESTAMP_ORIGINATE;
             i < OFFSET_TIMESTAMP_RECEIVE;
             i++)
            {
            arrayTemp[i - OFFSET_TIMESTAMP_ORIGINATE] = buffer[i];
            }

        return (new NtpTimeStamp(arrayTemp));
        }


    /***********************************************************************************************
     * Returns the receive timestamp.
     *
     * @return NtpTimeStamp
     */

    private NtpTimeStamp getReceiveTimeStamp()
        {
        final byte[] buffer;
        final byte[] arrayTemp;

        buffer = datagramPacket.getData();
        arrayTemp = new byte[8];

        for (int i = OFFSET_TIMESTAMP_RECEIVE;
             i < OFFSET_TIMESTAMP_TRANSMIT;
             i++)
            {
            arrayTemp[i - OFFSET_TIMESTAMP_RECEIVE] = buffer[i];
            }

        return (new NtpTimeStamp(arrayTemp));
        }


    /***********************************************************************************************
     * Returns the receive timestamp.
     *
     * @return NtpTimeStamp
     */

    private NtpTimeStamp getTransmitTimeStamp()
        {
        final byte[] buffer;
        final byte[] arrayTemp;

        buffer = datagramPacket.getData();
        arrayTemp = new byte[8];

        for (int i = OFFSET_TIMESTAMP_TRANSMIT;
             i < NTP_DATAGRAM_LENGTH;
             i++)
            {
            arrayTemp[i - OFFSET_TIMESTAMP_TRANSMIT] = buffer[i];
            }

        return (new NtpTimeStamp(arrayTemp));
        }


    /***********************************************************************************************
     * Returns the reception timestamp.
     *
     * @return NtpTimeStamp
     */

    private NtpTimeStamp getReceptionTimeStamp()
        {
        return (this.receptionNtpTimeStamp);
        }


    /***********************************************************************************************
     * Set the reception timestamp.
     *
     * @param timestamp
     */

    public void setReceptionTimeStamp(final NtpTimeStamp timestamp)
        {
        this.receptionNtpTimeStamp = timestamp;
        }


    /***********************************************************************************************
     * A convenience method which returns the useful information contained in a NtpDatagram.
     *
     * @return NtpData
     */

    public NtpData getInfo()
        {
        final NtpData dataNtp;
        final NtpHeader header;
        final long originate;
        final long receive;
        final long transmit;
        final long reception;

        dataNtp = new NtpData();
        header = getHeader();

        dataNtp.serverAddress = datagramPacket.getAddress();
        dataNtp.leapYearIndicator = header.getLeapYearIndicator();
        dataNtp.versionNumber = header.getVersionNumber();
        dataNtp.stratum = header.getStratum();
        dataNtp.mode = header.getMode();
        dataNtp.pollInterval = header.getPollInterval();
        dataNtp.precision = header.getPrecision();
        dataNtp.rootDelay = header.getRootDelay();
        dataNtp.rootDispersion = header.getRootDispersion();
        dataNtp.referenceIdentifier = header.getReferenceIdentifier();
        dataNtp.referenceNtpTimeStamp = getReferenceTimeStamp();

        originate = getOriginateTimeStamp().getTimeMillis();
        receive = getReceiveTimeStamp().getTimeMillis();
        transmit = getTransmitTimeStamp().getTimeMillis();
        reception = getReceptionTimeStamp().getTimeMillis();

        dataNtp.roundTripDelay = receive - originate + reception - transmit;
        dataNtp.offset = (receive - originate - reception + transmit) >> 1;

        return (dataNtp);
        }
    }
