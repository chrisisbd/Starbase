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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;

import java.util.*;


/***************************************************************************************************
 * NtpTimeStamp.
 *
 * This class encapsulates the notion of a timestamp as in rfc2030. Logically it is the number of
 * seconds since the beginning of the century (in UTC time). It is represented as an 8 byte array,
 * the first four bytes representing seconds and the next 4 bytes representing second fractions.
 *
 */

public final class NtpTimeStamp implements FrameworkConstants
    {
    /***********************************************************************************************
     * Get the milliseconds at the start of the century.
     *
     * @return long
     */

    private static long getMillisAtStartOfCentury()
        {
        final Calendar calStartOfCentury;

        calStartOfCentury = new GregorianCalendar(1900, Calendar.JANUARY, 1, 0, 0, 0);
        calStartOfCentury.setTimeZone(new SimpleTimeZone(0, DEFAULT_TIME_ZONE_ID));

        return (calStartOfCentury.getTimeInMillis());
        }


    /***********************************************************************************************
     *
     * @param bytevalue
     *
     * @return int
     */

    private static int byteToPositiveInteger(final byte bytevalue)
        {
        final int bb;

        bb = bytevalue;

        if ((bb < 0))
            {
            return 256 + bb;
            }
        else
            {
            return bb;
            }
        }


    private final byte[] arrayDataBytes;
    private long integerPartOfTimestamp;
    private long fractionalPartOfTimestamp;
    private final long timeMillis;


    /***********************************************************************************************
     * This constructs an NtpTimeStamp initialized with a given time from the ObservatoryClock.
     * Sets up:
     *      byte array
     *      integer part
     *      fractional part
     *      millseconds of Timestamp
     *
     * @param obstimezone
     * @param obslocale
     * @param obsclock
     */

    public NtpTimeStamp(final TimeZone obstimezone,
                        final Locale obslocale,
                        final ObservatoryClockInterface obsclock)
        {
        final long millisSinceStartOfCentury;
        long temp;

        arrayDataBytes = new byte[8];

        // The time in milliseconds associated with the Timestamp is the time relative to
        // time zero, i.e. January 1, 1970 00:00:00.000 GMT
        timeMillis = obsclock.getSystemTimeMillis();

        // The Timestamp is the number of seconds **since** the beginning of the century (in UTC time)
        millisSinceStartOfCentury = timeMillis - getMillisAtStartOfCentury();

        integerPartOfTimestamp = millisSinceStartOfCentury / 1000;
        fractionalPartOfTimestamp = ((millisSinceStartOfCentury % 1000) * 0x100000000L) / 1000;

        temp = integerPartOfTimestamp;

        for (int i = 3;
             i >= 0;
             i--)
            {
            arrayDataBytes[i] = (byte) (temp % 256);
            temp = temp >> 8;
            }

        temp = fractionalPartOfTimestamp;

        for (int i = 7;
             i >= 4;
             i--)
            {
            arrayDataBytes[i] = (byte) (temp % 256);
            temp = temp >> 8;
            }
        }


    /***********************************************************************************************
     * This constructs a timestamp starting from an eight byte array.
     * The first four bytes representing seconds and the next 4 bytes representing second fractions.
     * Sets up:
     *      byte array
     *      integer part
     *      fractional part
     *      millseconds of Timestamp
     *
     * @param data
     */

    public NtpTimeStamp(final byte[] data)
        {
        final long millisSinceStartOfCentury;

        this.arrayDataBytes = data;

        integerPartOfTimestamp = 0;

        for (int i = 0;
             i <= 3;
             i++)
            {
            integerPartOfTimestamp = 256 * integerPartOfTimestamp + byteToPositiveInteger(data[i]);
            }

        fractionalPartOfTimestamp = 0;

        for (int i = 4;
             i <= 7;
             i++)
            {
            fractionalPartOfTimestamp = 256 * fractionalPartOfTimestamp + byteToPositiveInteger(data[i]);
            }

        millisSinceStartOfCentury = integerPartOfTimestamp * 1000 + (fractionalPartOfTimestamp * 1000) / 0x100000000L;

        // The Timestamp is the number of seconds since the beginning of the century (in UTC time)
        // The time in milliseconds associated with the Timestamp is the time relative to
        // time zero, i.e. January 1, 1970 00:00:00.000 GMT
        timeMillis = getMillisAtStartOfCentury() + millisSinceStartOfCentury;
        }


//    /***********************************************************************************************
//     * Checks for equality of two timestamps.
//     *
//     * @param timeStamp
//     *
//     * @return boolean
//     */

//    public boolean equals(final NtpTimeStamp timeStamp)
//        {
//        boolean value;
//        final byte[] tsData;
//
//        value = true;
//        tsData = timeStamp.getByteArray();
//
//        for (int i = 0;
//             i <= 7;
//             i++)
//            {
//            if (arrayDataBytes[i] != tsData[i])
//                {
//                value = false;
//                }
//            }
//
//        return value;
//        }


    public String toString()
        {
        return "" + timeMillis + " + " + fractionalPartOfTimestamp + "/" + 0x100000000L;
        }




    /***********************************************************************************************
     * Get the fractional part of the TimeStamp.
     *
     * @return double
     */

    public double getFractionalPart()
        {
        final double dblFraction;

        dblFraction = fractionalPartOfTimestamp / (double) 0x100000000L;

        return (dblFraction);
        }


    /***********************************************************************************************
     * Returns the eight byte array associated with a timestamp.
     *
     * @return byte[]
     */

    public byte[] getByteArray()
        {
        return (this.arrayDataBytes);
        }


    /***********************************************************************************************
     * Returns the milliseconds associated with a timestamp.
     *
     * @return Date
     */

    public long getTimeMillis()
        {
        return (this.timeMillis);
        }
    }
