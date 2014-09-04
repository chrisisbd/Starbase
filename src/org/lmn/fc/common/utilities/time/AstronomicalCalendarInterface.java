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

package org.lmn.fc.common.utilities.time;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/***************************************************************************************************
 * AstronomicalCalendarInterface.
 */

public interface AstronomicalCalendarInterface extends FrameworkConstants,
                                                       FrameworkStrings,
                                                       FrameworkSingletons
    {
    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Get the internal Calendar. If Calendar used an interface, this wouldn't be necessary!
     *
     * @return Calendar
     */

    Calendar getCalendar();


    /***********************************************************************************************
     * Sets this Calendar's current time from the given long value.
     *
     * @param millis the new time in UTC milliseconds from the epoch.
     */

    void setTimeInMillis(long millis);


    /***********************************************************************************************
     * Read the Julian Day at 0hr UT (JD0).
     *
     * @return double
     */

    double getJD0();


    /***********************************************************************************************
     * Read the Julian Day at the calendar date (JD).
     *
     * @return double
     */

    double getJD();


    /***********************************************************************************************
     * Read the Local Mean Time (LMT).
     *
     * @return double
     */

    double getLMT();


    /***********************************************************************************************
     * Read the Universal Time (UT).
     *
     * @return double
     */

    double getUT();


    /***********************************************************************************************
     * Read the Greenwich Mean Sidereal Time at 0hr UT (GMST0).
     *
     * @return double
     */

    double getGMST0();


    /***********************************************************************************************
     * Read the Greenwich Apparent Sidereal Time at 0hr UT (GAST0).
     *
     * @return double
     */

    double getGAST0();


    /***********************************************************************************************
     * Read the Greenwich Mean Sidereal Time (GMST).
     *
     * @return double
     */

    double getGMST();


    /***********************************************************************************************
     * Read the Greenwich Apparent Sidereal Time (GAST).
     *
     * @return double
     */

    double getGAST();


    /***********************************************************************************************
     * Read the Local Mean Sidereal Time (LMST).
     *
     * @return double
     */

    double getLMST();


    /***********************************************************************************************
     * Read the Local Apparent Sidereal Time (LAST).
     *
     * @return double
     */

    double getLAST();


    /***********************************************************************************************
     * Get the Longitude of the place of observation.
     *
     * @return double
     */

    double getLongitude();


    /***********************************************************************************************
     * Set the Longitude of the place of observation.
     * Update the Calendar accordingly.
     *
     * @param longitude
     */

    void setLongitude(double longitude);


    /***********************************************************************************************
     * Returns the value of the given calendar field. In lenient mode,
     * all calendar fields are normalized. In non-lenient mode, all
     * calendar fields are validated and this method throws an
     * exception if any calendar fields have out-of-range values. The
     * normalization and validation are handled by the
     * complete() method, which process is calendar
     * system dependent.
     *
     * @param field the given calendar field.
     *
     * @return the value for the given calendar field.
     *
     * @throws ArrayIndexOutOfBoundsException if the specified field is out of range
     *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
     */

    int get(int field);


    /**********************************************************************************************
     * Adds or subtracts (up/down) a single unit of time on the given time
     * field without changing larger fields.
     * <p>
     * <em>Example</em>: Consider a <code>GregorianCalendar</code>
     * originally set to December 31, 1999. Calling {@link #roll(int,boolean) roll(Calendar.MONTH, true)}
     * sets the calendar to January 31, 1999.  The <code>YEAR</code> field is unchanged
     * because it is a larger field than <code>MONTH</code>.</p>
     *
     * @param up indicates if the value of the specified calendar field is to be
     * rolled up or rolled down. Use <code>true</code> if rolling up, <code>false</code> otherwise.
     * @exception IllegalArgumentException if <code>field</code> is
     * <code>ZONE_OFFSET</code>, <code>DST_OFFSET</code>, or unknown,
     * or if any calendar fields have out-of-range values in
     * non-lenient mode.
     */

    void roll(int field,
              boolean up);


    /***********************************************************************************************
     * Returns a <code>Date</code> object representing this
     * <code>Calendar</code>'s time value (millisecond offset from the <a
     * href="#Epoch">Epoch</a>").
     * This abomination is here only for compatibility with Calendar. Date? Time? What a mess.
     *
     * @return a <code>Date</code> representing the time value.
     */

    Date getTime();


    /***********************************************************************************************
     * Gets the time zone.
     *
     * @return TimeZone
     */

    TimeZone getTimeZone();


    /**********************************************************************************************/
    /* Nutation                                                                                   */
    /***********************************************************************************************
     * Read the Nutation in Longitude in SECONDS.
     *
     * @return double
     */

    double getNutationLongitude();


    /***********************************************************************************************
     * Read the Nutation in Obliquity in SECONDS.
     *
     * @return double
     */

    double getNutationObliquity();


    /***********************************************************************************************
     * Read the Nutation in RA in SECONDS.
     *
     * @return double
     */

    double getNutationRA();


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Get the Year Month Day as a YMD object.
     * Note that the 'normal' Month numbering is used {1...12}!
     *
     * @return YearMonthDayInterface
     */

    YearMonthDayInterface toYMD();


    /***********************************************************************************************
     * Get the Hour Min Sec as an HMS object.
     *
     * @return HourMinSecInterface
     */

    HourMinSecInterface toHMS();


    /***********************************************************************************************
     * Indicate if this AstronomicalCalendar has the sign preceding the value.
     *
     * @return boolean
     */

    boolean hasFormatSign();


    /***********************************************************************************************
     * Control the use of a sign (+/-) on the formatted output.
     *
     * @param sign
     */

    void enableFormatSign(boolean sign);


    /***********************************************************************************************
     * Get the current Time of a TimeSystem as a double.
     *
     * @param timesystem
     *
     * @return double
     */

    double toDouble(TimeSystem timesystem);


    /***********************************************************************************************
     * Return the current Time of a Time system as a formatted, delimited String.
     * Primarily intended for Clocks.
     * Note that seconds are rounded to the nearest integer,
     * so internally a time may be 12:34:56.995, but it will display as 12:34:57.
     * Note that if the TimeSystem is JulianDay, then 00:00:00 is returned.
     *
     * @param timesystem
     *
     * @return String
     */

    String toString_HH_MM_SS(TimeSystem timesystem);


    /***********************************************************************************************
     * Read the current Time of a Time system as a formatted string.
     * Preserve the full millisecond precision,
     * so if the time is required for a Clock, use toString_HH_MM_SS() instead.
     *
     * @param timesystem
     *
     * @return String
     */

    String toString(TimeSystem timesystem);


    /**********************************************************************************************/
    /* Debugs                                                                                     */
    /***********************************************************************************************
     * Get the Debug Mode flag.
     *
     * @return boolean
     */

    boolean isDebugMode();


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    void setDebugMode(boolean flag);


    /***********************************************************************************************
     * Reveal the inner contents of an AstronomicalCalendar.
     *
     * @param message
     */

    void showCalendarDebug(String message);
    }
