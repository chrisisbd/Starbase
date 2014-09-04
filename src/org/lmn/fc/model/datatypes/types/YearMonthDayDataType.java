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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  15-04-02    LMN created file
//  17-04-02    LMN added sign enable, tidied up...
//  05-10-04    LMN extended DataType
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.datatypes.types;


import org.lmn.fc.common.exceptions.YearMonthDayException;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.parsers.YearMonthDayParser;

import java.util.Calendar;
import java.util.GregorianCalendar;


/***************************************************************************************************
 * YearMonthDayDataType.
 */

public final class YearMonthDayDataType extends RootDataType
                                        implements YearMonthDayInterface
    {
    // Internal State
    private int intYear;                    // The Year part of the YMD (positive)
    private int intMonth;                   // The Month part of the YMD
    private int intDay;                     // The Day part of the YMD
    private boolean boolPositive;           // Sign of the YMD - problems with -0000/nn/nn

    // Output Formatting
    private boolean boolEnableFormatSign;   // Enables use of '+/-' on the output from toString()


    /***********************************************************************************************
     * Add a leading sign, but only if required.
     * Assume the buffer is empty on entry.
     *
     * @param ymd
     * @param buffer
     */

    private static void addLeadingSign(final YearMonthDayInterface ymd,
                                       final StringBuffer buffer)
        {
        if ((ymd != null)
            && (buffer != null))
            {
            // Are we meant to have a sign?
            if (ymd.hasFormatSign())
                {
                // Beware parsing the output with parseDate()!
                if (ymd.isPositive())
                    {
                    buffer.append("+");
                    }
                else
                    {
                    buffer.append("-");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Don't supply anything, and get calendarNow().
     * Note that the 'normal' month numbering of {1...12} is used!
     */

    public YearMonthDayDataType()
        {
        super(DataTypeDictionary.DATE_YYYY_MM_DD);

        final GregorianCalendar calendarNow;

        calendarNow = new GregorianCalendar();

        // year must never be negative
        if (calendarNow.get(Calendar.YEAR) >= 0)
            {
            setYear(Math.abs(calendarNow.get(Calendar.YEAR)),
                    true);
            }
        else
            {
            setYear(Math.abs(calendarNow.get(Calendar.YEAR)),
                    false);
            }

        setMonth(calendarNow.get(GregorianCalendar.MONTH) + 1);
        setDay(calendarNow.get(GregorianCalendar.DAY_OF_MONTH));

        enableFormatSign(true);
        }


    /***********************************************************************************************
     * Get the Year from the specified calendar.
     *
     * @param calendar
     */

    public YearMonthDayDataType(final Calendar calendar)
        {
        super(DataTypeDictionary.DATE_YYYY_MM_DD);

        // year must never be negative
        if (calendar.get(Calendar.YEAR) >= 0)
            {
            setYear(Math.abs(calendar.get(Calendar.YEAR)),
                    true);
            }
        else
            {
            setYear(Math.abs(calendar.get(Calendar.YEAR)),
                    false);
            }

        setMonth(calendar.get(GregorianCalendar.MONTH) + 1);
        setDay(calendar.get(GregorianCalendar.DAY_OF_MONTH));

        enableFormatSign(true);
        }


    /***********************************************************************************************
     * Supply Sign, Year, Month, Day separately, and get yyyy/mm/dd.
     * Note that the 'normal' month numbering of {1...12} is used!
     * Throws an Exception if it is an invalid date.
     *
     * @param positive
     * @param year
     * @param month
     * @param day
     */

    public YearMonthDayDataType(final boolean positive,
                                final int year,
                                final int month,
                                final int day)
        {
        super(DataTypeDictionary.DATE_YYYY_MM_DD);

        // year must never be negative
        setYear(Math.abs(year), positive);
        // Note that the 'normal' month numbering of {1...12} is used!
        setMonth(month);
        setDay(day);

        enableFormatSign(false);
        }


    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Set the internal state of this YMD from the specified YMD.
     *
     * @param ymd
     */

    public void copy(final YearMonthDayInterface ymd)
        {
        if (ymd != null)
            {
            setPositive(ymd.isPositive());
            setYear(ymd.getYear(), isPositive());
            setMonth(ymd.getMonth());
            setDay(ymd.getDay());
            enableFormatSign(ymd.hasFormatSign());
            }
        }


    /***********************************************************************************************
     * Indicate if the YMD is positive.
     *
     * @param positive
     */

    public void setPositive(final boolean positive)
        {
        this.boolPositive = positive;
        }


    /***********************************************************************************************
     * Indicate if the YMD is positive.
     *
     * @return boolean
     */

    public boolean isPositive()
        {
        // We could return +1 or -1, but a boolean seems more logical...
        return (this.boolPositive);
        }


    /**********************************************************************************************
     * Set the Year field.
     * The sign must be supplied also, to avoid problems if Year=0.
     *
     * @param year
     * @param positive
     *
     * @throws YearMonthDayException
     */

    public void setYear(final int year,
                        final boolean positive) throws YearMonthDayException
        {
        // Range is {-9999...+9999}
        if (year >= 9999)
            {
            throw new YearMonthDayException(EXCEPTION_OUTOFRANGE + " [year=" + year + "]");
            }

        // year must never be negative
        this.intYear = Math.abs(year);
        setPositive(positive);
        }


    /***********************************************************************************************
     * Get the Year field.
     *
     * @return int
     */

    public int getYear()
        {
        return (this.intYear);
        }


    /***********************************************************************************************
     * Set the Month field.
     * Note that the 'normal' month numbering of {1...12} is used!
     *
     * @param month
     *
     * @throws YearMonthDayException
     */

    public void setMonth(final int month) throws YearMonthDayException
        {
        if ((month < 1) || (month > 12))
            {
            throw new YearMonthDayException(EXCEPTION_OUTOFRANGE + " [month=" + month + "]");
            }

        // month must never be negative
        this.intMonth = Math.abs(month);
        }


    /***********************************************************************************************
     * Get the Month field.
     * NOTE: Calendar months {0...11} but the instance variable holds {1...12}.
     *
     * @return int
     */

    public int getMonth()
        {
        return (this.intMonth);
        }


    /***********************************************************************************************
     * Set the Day field.
     *
     * @param day
     *
     * @throws YearMonthDayException
     */

    public void setDay(final int day) throws YearMonthDayException
        {
        // NOTE: months {0...11} but the instance variable holds {1...12}
        if (!YearMonthDayParser.isValidDayForMonth(this.intYear, this.intMonth - 1, day))
            {
            throw new YearMonthDayException(EXCEPTION_OUTOFRANGE + " [day=" + day + "]");
            }

        // day must never be negative
        this.intDay = Math.abs(day);
        }


    /***********************************************************************************************
     * Get the Day field.
     *
     * @return int
     */

    public int getDay()
        {
        return (this.intDay);
        }


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Indicate if this YMD DataType has the sign preceding the value.
     *
     * @return boolean
     */

    public boolean hasFormatSign()
        {
        return (this.boolEnableFormatSign);
        }


    /***********************************************************************************************
     * Enable the use of a sign (+/-) in the display format.
     *
     * @param sign
     */

    public void enableFormatSign(final boolean sign)
        {
        this.boolEnableFormatSign = sign;
        }


    /***********************************************************************************************
     * Return the YearMonthDayDataType as a signed double value.
     *
     * @return double
     */

    public double toDouble()
        {
        final double dblYMD;

        // NOTE: Calendar months {0...11} but the instance variable holds {1...12}
        // ToDo REVIEW: Should these be Julian Days?
        // getDayNumberOfYear() returns {1...365} so reduce this by one to give year {0 ... 0.997}
        dblYMD = (double) getYear() + ((YearMonthDayParser.getDayNumberOfYear(getYear(), (getMonth()-1), getDay()) - 1) / 365.0);

        if (isPositive())
            {
            return (dblYMD);
            }
        else
            {
            return (-dblYMD);
            }
        }


    /***********************************************************************************************
     * Return the YearMonthDayDataType as a formatted, delimited String.
     *
     * @return String
     */

    public String toString()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Do we need a leading sign character?
        addLeadingSign(this, buffer);

        // Create a formatted YYYY:MM:DD string, with integer Years, Months, Days
        buffer.append(DecimalFormatPattern.YEAR.format(getYear()));
        buffer.append(DecimalFormatPattern.YEAR.getDelimiter());
        buffer.append(DecimalFormatPattern.MONTH.format(getMonth()));
        buffer.append(DecimalFormatPattern.MONTH.getDelimiter());
        buffer.append(DecimalFormatPattern.DAY.format(getDay()));
        buffer.append(DecimalFormatPattern.DAY.getDelimiter());

        return  (buffer.toString());
        }


    /***********************************************************************************************
     * Indicate if this YMD instance is after the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    public boolean after(final YearMonthDayInterface ymdwhen)
        {
        final boolean boolAfter;

        boolAfter = (toDouble() > ymdwhen.toDouble());

        return (boolAfter);
        }


    /***********************************************************************************************
     * Indicate if this YMD instance is equal to or after the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    public boolean equalOrAfter(final YearMonthDayInterface ymdwhen)
        {
        final boolean boolEqualOrAfter;

        boolEqualOrAfter = (toDouble() >= ymdwhen.toDouble());

        return (boolEqualOrAfter);
        }


    /***********************************************************************************************
     * Indicate if this YMD instance is before the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    public boolean before(final YearMonthDayInterface ymdwhen)
        {
        final boolean boolBefore;

        boolBefore = (toDouble() < ymdwhen.toDouble());

        return (boolBefore);
        }


    /***********************************************************************************************
     * Indicate if this YMD instance is before or equal to the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    public boolean beforeOrEqual(final YearMonthDayInterface ymdwhen)
        {
        final boolean boolBeforeOrEqual;

        boolBeforeOrEqual = (toDouble() <= ymdwhen.toDouble());

        return (boolBeforeOrEqual);
        }
    }
