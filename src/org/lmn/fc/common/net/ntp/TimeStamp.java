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

import java.util.*;


/**
 * This class encapsulates the notion of a timestamp as in rfc2030. Logically it is the number of
 * seconds since the beginning of the century (in UTC time). It is represented as  an 8 byte array,
 * the first four bytes representing seconds and the next 4 bytes representing second fractions.
 *
 * @author Michel Van den Bergh
 * @version 1.0
 */


public final class TimeStamp
    {
    private long integerPart;
    private long fractionalPart;
    private final byte[] data;
    private final Date date;

    static private final TimeZone UTC = new SimpleTimeZone(0, "UTC");
    static private final Calendar c
        = new GregorianCalendar(1900, Calendar.JANUARY, 1, 0, 0, 0);
    static private final Date startOfCentury;


    static
    {
    c.setTimeZone(UTC);
    startOfCentury = c.getTime();
    }

    private static final byte[] emptyArray = {0, 0, 0, 0, 0, 0, 0, 0};
    /**
     * The timestamp corresponding to the beginning of the century.
     */
    public static final TimeStamp zero = new TimeStamp(emptyArray);

    private static int mp(final byte b)
        {
        final int bb = b;
        return (bb < 0) ? 256 + bb : bb;
        }

    /**
     * This constructs a timestamp initialized with the current date.
     */
    public TimeStamp()
        {
        this(new Date());
        }

    /**
     * This constructs a timestamp initialized with a given date.
     */
    public TimeStamp(final Date date)
        {
        data = new byte[8];
        this.date = date;

        final long msSinceStartOfCentury = date.getTime() - startOfCentury.getTime();

        integerPart = msSinceStartOfCentury / 1000;
        fractionalPart = ((msSinceStartOfCentury % 1000) * 0x100000000L) / 1000;

        long temp = integerPart;

        for (int i = 3; i >= 0; i--)
            {
            data[i] = (byte) (temp % 256);
            temp = temp / 256;
            }

        temp = fractionalPart;
        
        for (int i = 7; i >= 4; i--)
            {
            data[i] = (byte) (temp % 256);
            temp = temp / 256;
            }
        }

    /**
     * This constructs a timestamp starting from an eight byte array.
     */
    public TimeStamp(final byte[] data)
        {
        this.data = data;
        integerPart = 0;

        for (int i = 0; i <= 3; i++)
            {
            integerPart = 256 * integerPart + mp(data[i]);
            }
        fractionalPart = 0;
        for (int i = 4; i <= 7; i++)
            {
            fractionalPart = 256 * fractionalPart + mp(data[i]);
            }
        final long msSinceStartOfCentury
            = integerPart * 1000 + (fractionalPart * 1000) / 0x100000000L;
        date = new Date(msSinceStartOfCentury + startOfCentury.getTime());
        }


    /***********************************************************************************************
     * Checks for equality of two timestamps.
     *
     * @param ts
     *
     * @return boolean
     */

    public boolean equals(final TimeStamp ts)
        {
        boolean value = true;
        final byte[] tsData = ts.getData();
        for (int i = 0; i <= 7; i++)
            {
            if (data[i] != tsData[i])
                {
                value = false;
                }
            }
        return value;
        }

    public String toString()
        {
        return "" + date + " + " + fractionalPart + "/" + 0x100000000L;
        }


    /***********************************************************************************************
     * Get the TimeStamp as a Calendar.
     *
     * @return Calendar
     */

    public Calendar toCalendar()
        {
        final GregorianCalendar calendar;

        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTime());

        // ToDo review TimeZone ??

        return (calendar);
        }


    /***********************************************************************************************
     * Get the fractional part of the TimeStamp.
     *
     * @return double
     */

    public double getFractionalPart()
        {
        final double dblFraction;

        dblFraction = fractionalPart / (double)0x100000000L;

        return (dblFraction);
        }

    /**
     * Returns the eight byte array associated to a timestamp.
     */
    public byte[] getData()
        {
        return data;
        }

    /**
     * Returns the date associated to a timestamp.
     */
    public Date getTime()
        {
        return date;
        }
    }






