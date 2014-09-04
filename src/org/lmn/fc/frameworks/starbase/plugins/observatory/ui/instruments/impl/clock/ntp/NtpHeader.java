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


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * This class encapsulates the header of a NtpDatagram. See rfc2030 for more details.
 */
public final class NtpHeader implements FrameworkConstants,
                                        FrameworkStrings
    {
    /**
     * The default header data for a client datagram. Version=3, Mode=client.
     */
    private static final byte[] DEFAULT_HEADER_DATA = {(byte) 0x1B, 0,
                                                       0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    /**
     * The default header for a client datagram. This is a wrapper around 'DEFAULT_HEADER_DATA'
     */
    public static final NtpHeader DEFAULT_HEADER = new NtpHeader(DEFAULT_HEADER_DATA);


    /**
     * Reference identifier is InetAddress.
     */
    private static final byte RI_IP_ADDRESS = 0;
    /**
     * Reference identifier is String.
     */
    private static final byte RI_CODE = 1;
    /**
     * Reference identifier is 4 byte array.
     */
    private static final byte RI_OTHER = 2;


    /***********************************************************************************************
     *
     * @param b
     *
     * @return int
     */

    private static int MP(final byte b)
        {
        final int bb;

        bb = b;

        if ((bb < 0))
            {
            return 256 + bb;
            }
        else
            {
            return bb;
            }
        }


    // Injections
    private final byte[] data;


    /**
     * Construct a NtpHeader from a 16 byte array.
     */
    public NtpHeader(final byte[] bytes)
        {
        this.data = bytes;
        }


    /**
     * Gets the 16 byte array constituting the header.
     */
    public byte[] getData()
        {
        return data;
        }


    public int getLeapYearIndicator()
        {
        return ((data[0] & 0xc0) >>> 6);
        }


    public int getVersionNumber()
        {
        return ((data[0] & 0x38) >>> 3);
        }


    public int getMode()
        {
        return (data[0] & 0x07);
        }


    public int getStratum()
        {
        return (int) data[1];
        }


    public int getPollInterval()
        {
        return (int) Math.round(Math.pow(2, data[2]));
        }


    /**
     * Get precision in milliseconds.
     */

    public double getPrecision()
        {
        return 1000 * Math.pow(2, data[3]);
        }


    /**
     * Get root delay in milliseconds.
     */
    public double getRootDelay()
        {
        final int temp;

        temp = 256 * (256 * (256 * data[4] + data[5]) + data[6]) + data[7];

        return 1000 * (((double) temp) / 0x10000);
        }


    /**
     * Get root dispersion in milliseconds.
     */
    public double getRootDispersion()
        {
        final long temp;

        temp = 256 * (256 * (256 * data[8] + data[9]) + data[10]) + data[11];

        return 1000 * (((double) temp) / 0x10000);
        }


    /**
     * Gets the type of the reference identifier.
     */
    private int getReferenceIdentifierType()
        {
        if (getMode() == NtpData.MODE_CLIENT)
            {
            return RI_OTHER;
            }
        else if (getStratum() < 2)
            {
            return RI_CODE;
            }
        else if (getVersionNumber() <= 3)
            {
            return RI_IP_ADDRESS;
            }
        else
            {
            return RI_OTHER;
            }
        }


    private InetAddress getReferenceAddress() throws IllegalArgumentException,
                                                     UnknownHostException
        {
        if (getReferenceIdentifierType() != RI_IP_ADDRESS)
            {
            throw new IllegalArgumentException();
            }

        final String temp;

        temp = "" + MP(data[12]) + "." + MP(data[13]) + "." + MP(data[14]) + "." + MP(data[15]);

        return InetAddress.getByName(temp);
        }


    private String getReferenceCode() throws IllegalArgumentException
        {
        int codeLength = 0;
        int index = 12;
        boolean zeroFound;

        if (getReferenceIdentifierType() != RI_CODE)
            {
            throw new IllegalArgumentException();
            }

        zeroFound = false;

        while ((!zeroFound) && (index <= 15))
            {
            if (data[index] == 0)
                {
                zeroFound = true;
                }
            else
                {
                index++;
                codeLength++;
                }
            }

        return new String(data, 12, codeLength);
        }


    private byte[] getReferenceData()
        {
        final byte[] temp;

        temp = new byte[4];

        temp[0] = data[12];
        temp[1] = data[13];
        temp[2] = data[14];
        temp[3] = data[15];

        return temp;
        }


    /**
     * Gets the  reference identifier as an object. It can be either a String, a InetAddress or a 4
     * byte array. Use 'instanceof' to find out what the true class is.
     */

    public Object getReferenceIdentifier()
        {
        if (getReferenceIdentifierType() == RI_IP_ADDRESS)
            {
            try
                {
                return getReferenceAddress();
                }
            catch (Exception e)
                {
                return getReferenceData();
                }
            }
        else if (getReferenceIdentifierType() == RI_CODE)
            {
            return getReferenceCode();
            }
        else
            {
            return getReferenceData();
            }
        }


    public String toString()
        {
        String s = "Leap year indicator : " + getLeapYearIndicator() + "\n" +
                "Version number : " + getVersionNumber() + "\n" +
                "Mode : " + getMode() + "\n" +
                "Stratum : " + getStratum() + "\n" +
                "Poll interval : " + getPollInterval() + " s\n" +
                "Precision : " + getPrecision() + " ms\n" +
                "Root delay : " + getRootDelay() + " ms\n" +
                "Root dispersion : " + getRootDispersion() + " ms\n";
        final Object o = getReferenceIdentifier();
        if (o instanceof InetAddress)
            {
            s = s + "Reference address : " + o;
            }
        else if (o instanceof String)
            {
            s = s + "Reference code : " + o;
            }
        else
            {
            final byte[] temp = (byte[]) o;
            s = s + "Reference data : " + temp[0] + SPACE + temp[1] + SPACE + temp[2] + SPACE + temp[3];
            }
        return s;
        }
    }



