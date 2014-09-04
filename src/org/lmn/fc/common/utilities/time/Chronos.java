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
//  25-01-00    LMN created file
//  04-04-02    LMN added other constructors, & fixed 24hr format bug
//  08-04-02    LMN made TimeZones work?!
//  13-04-03    LMN changed to correct Java 1.4 ClassCastException - yes, a year later!
//  14-02-05    LMN finally created a reliable getTimeDifference() !!
//  10-03-05    LMN added showElapsedTime()
//
//--------------------------------------------------------------------------------------------------
// ToDo
//
//    Retrieve TimeZone and DST rules from RegistryModel?
//
//--------------------------------------------------------------------------------------------------
// Utilities package

package org.lmn.fc.common.utilities.time;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//--------------------------------------------------------------------------------------------------
// Time formatting characters
// Symbol   Meaning                 Presentation        Example
// G        era designator          (Text)              AD
// y        year                    (Number)            1996
// M        month in year           (Text & Number)     July & 07
// d        day in month            (Number)            10
// h        hour in am/pm (1~12)    (Number)            12
// H        hour in day (0~23)      (Number)            0
// m        minute in hour          (Number)            30
// s        second in minute        (Number)            55
// S        millisecond             (Number)            978
// E        day in week             (Text)              Tuesday
// D        day in year             (Number)            189
// F        day of week in month    (Number)            2 (2nd Wed in July)
// w        week in year            (Number)            27
// W        week in month           (Number)            2
// a        am/pm marker            (Text)              PM
// k        hour in day (1~24)      (Number)            24
// K        hour in am/pm (0~11)    (Number)            0
// z        time zone               (Text)              Pacific Standard Time
// '        escape for text         (Delimiter)
// ''       single quote            (Literal)           '
//--------------------------------------------------------------------------------------------------


public final class Chronos implements FrameworkConstants,
                                      FrameworkSingletons,
                                      FrameworkStrings,
                                      FrameworkMetadata
    {
    /***********************************************************************************************
     * Get the System time in milliseconds.
     *
     * @return long
     */

    public static long getSystemTime()
        {
        return (System.currentTimeMillis());
        }


    /***********************************************************************************************
     * Get the System time as a Calendar.
     *
     * @param timezone
     * @param locale
     *
     * @return Calendar
     */

    public static Calendar getSystemCalendar(final TimeZone timezone,
                                             final Locale locale)
        {
        final GregorianCalendar calendarNow;

        calendarNow = new GregorianCalendar(timezone,
                                            locale);
        calendarNow.setTimeInMillis(getSystemTime());

        return (calendarNow);
        }


    /**********************************************************************************************
     * Get the System Date, for SQL.
     *
     * @return Date
     */

    public static java.sql.Date getSystemDateNow()
        {
        return (new java.sql.Date(getSystemTime()));
        }


    /***********************************************************************************************
     * Get the System Calendar.
     *
     * @return GregorianCalendar
     */

    public static GregorianCalendar getCalendarDateNow()
        {
        return (new GregorianCalendar());
        }


    /**********************************************************************************************
     * Get the System Time, for SQL.
     *
     * @return Time
     */

    public static Time getSystemTimeNow()
        {
        return (new Time(getSystemTime()));
        }


    /***********************************************************************************************
     * Get the System Calendar.
     *
     * @return GregorianCalendar
     */

    public static GregorianCalendar getCalendarTimeNow()
        {
        return (new GregorianCalendar());
        }


    /***********************************************************************************************
     * Return a flag to indicate if the two Dates are on the same Day.
     *
     * @param first
     * @param second
     *
     * @return boolean
     */
    public static boolean onSameDay(final TimeZone timezone,
                                          final Locale locale,
                                          final Date first,
                                          final Date second)
        {
        final GregorianCalendar calendarFirst;
        final GregorianCalendar calendarSecond;

        if ((timezone != null)
            && (locale != null)
            && (first != null)
            && (second != null))
            {
            // Pass the Dates through Calendars in case there are DST problems...
            calendarFirst = new GregorianCalendar(timezone, locale);
            calendarFirst.setTimeInMillis(first.getTime());

            calendarSecond = new GregorianCalendar(timezone, locale);
            calendarSecond.setTimeInMillis(second.getTime());

            return (calendarFirst.get(Calendar.DATE) == calendarSecond.get(Calendar.DATE));
            }
            else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Return the Time of Midnight.
     *
     * @return java.sql.Time
     */

    public static Time getEarliestTime()
        {
        return (ChronosHelper.parseTime("00:00:00", getSystemDateNow()));
        }


    /***********************************************************************************************
     * Return the Time one second before Midnight.
     *
     * @return java.sql.Time
     */

    public static Time getLatestTime()
        {
        return (ChronosHelper.parseTime("23:59:59", getSystemDateNow()));
        }


    /***********************************************************************************************
     * Read a Date and Time from the specified columns of the ResultSet,
     * parsing into a Calendar using the specified TimeZone and Locale,
     * using the standard JDBC pattern of <code>yyyy-MM-dd HH:mm:ss</code>.
     *
     * @param timezone
     * @param locale
     * @param result
     * @param datecolumn
     * @param timecolumn
     *
     * @return Calendar
     *
     * @throws SQLException
     * @throws ParseException
     */

    public static Calendar getDatabaseCalendar(final TimeZone timezone,
                                                     final Locale locale,
                                                     final ResultSet result,
                                                     final String datecolumn,
                                                     final String timecolumn) throws SQLException,
                                                                               ParseException
        {
        final String DATE_PARSE_FORMAT = "yyyy-MM-dd HH:mm:ss";

        final java.sql.Date date;
        final Time time;
        Calendar calendar;
        final String strDateAndTime;

        calendar = null;

        if ((timezone != null)
            && (locale != null)
            && (result != null)
            && (datecolumn != null)
            && (timecolumn != null))
            {
            date = result.getDate(datecolumn);
            time = result.getTime(timecolumn);

            if ((date != null)
                && (time != null))
                {
                strDateAndTime = date.toString()
                                    + SPACE
                                    + time.toString();

                // Create a reliable Calendar holding the Date and Time
                calendar = ChronosHelper.parseCalendar(timezone,
                                         locale,
                                         DATE_PARSE_FORMAT,
                                         strDateAndTime);
                }
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Return the current Year as an integer.
     *
     * @return int
     */

    public static int getYearNow()
        {
        return ((new GregorianCalendar()).get(Calendar.YEAR));
        }


    /***********************************************************************************************
     * A utility to return the Date part of a calendar.
     *
     * @param calendar
     *
     * @return java.sql.Date
     */

    public static java.sql.Date getDate(final Calendar calendar)
        {
        return (new java.sql.Date(calendar.getTimeInMillis()));
        }


    /***********************************************************************************************
     * Convert a Time into a number of Hours.
     *
     * @param timezone
     * @param locale
     * @param time
     *
     * @return float
     */
    public static float toHours(final TimeZone timezone,
                                      final Locale locale,
                                      final Time time)
        {
        final GregorianCalendar calendar;
        float floatTime;

        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time.getTime());
        debugCalendar(calendar);

        floatTime = 0;
        floatTime += calendar.get(Calendar.HOUR_OF_DAY) * ChronosHelper.HOUR_MILLISECONDS;
        floatTime += calendar.get(Calendar.MINUTE) * ChronosHelper.MINUTE_MILLISECONDS;
        floatTime += calendar.get(Calendar.SECOND) * ChronosHelper.SECOND_MILLISECONDS;
        floatTime += calendar.get(Calendar.MILLISECOND);

        return (floatTime / ChronosHelper.HOUR_MILLISECONDS);
        }


    /**********************************************************************************************/

    //    /***********************************************************************************************
//     * Show the Date and Time value of a Calendar as a String.
//     * This is putting right more problems with Java Dates...
//     *
//     * @param calendar
//     *
//     * @return String
//     */
//
//    public static String toDateAndTimeString(final Calendar calendar)
//        {
//        final StringBuffer bufferDate;
//
//        bufferDate = new StringBuffer();
//
//        if (calendar != null)
//            {
//            bufferDate.append(Integer.toString(calendar.get(Calendar.YEAR)));
//            bufferDate.append(Integer.toString(calendar.get(Calendar.MONTH)));
//            bufferDate.append(Integer.toString(calendar.get(Calendar.DATE)));
//
//            if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
//                {
//                bufferDate.append("0" + calendar.get(Calendar.HOUR_OF_DAY));
//                }
//            else
//                {
//                bufferDate.append(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
//                }
//
//            if (calendar.get(Calendar.MINUTE) < 10)
//                {
//                bufferDate.append("0" + calendar.get(Calendar.MINUTE));
//                }
//            else
//                {
//                bufferDate.append(Integer.toString(calendar.get(Calendar.MINUTE)));
//                }
//
//            if (calendar.get(Calendar.SECOND) < 10)
//                {
//                bufferDate.append("0" + calendar.get(Calendar.SECOND));
//                }
//            else
//                {
//                bufferDate.append(Integer.toString(calendar.get(Calendar.SECOND)));
//                }
//            }
//
//        return (bufferDate.toString());
//        }


    //----------------------------------------------------------------------------------------------
    // Find the time Now in the format "yyyy-MM-dd HH:mm:ss.SSS"
    // The TimeZone and the DST rules should be coming from the object model...

    public static String timeNow()
        {
        // Make a time zone, by a rather odd route...
        final TimeZone timezone = TimeZone.getDefault();
        final SimpleTimeZone tzSimple = new SimpleTimeZone(timezone.getRawOffset(),
                                                           timezone.getID());

        // Set the DST rules, otherwise we don't get any...
        tzSimple.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        tzSimple.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // Find out where we are in the GregorianCalendar, using the SimpleTimeZone
        final GregorianCalendar calendarNow = new GregorianCalendar(tzSimple);
        calendarNow.setTime(new Date());

        // Set up the formatter
        // ToDo Consider ThreadLocal
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        formatter.setCalendar(calendarNow);
        formatter.setTimeZone(tzSimple);

        return (formatter.format(calendarNow.getTime()));
        }


    //----------------------------------------------------------------------------------------------
    // Find the time Now, given a TimeZone, in the format "dd/MM/yyyy HH:mm:ss"

// --Commented out by Inspection START (14/02/05 14:54):
//    public static String timeNow(final SimpleTimeZone tzSimple)
//        {
//        // Find out where we are in the GregorianCalendar
//        final GregorianCalendar calendarNow = new GregorianCalendar(tzSimple);
//        calendarNow.setTime(new Date());
//
//        // Set up the formatter
//        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//        formatter.setCalendar(calendarNow);
//        formatter.setTimeZoneCode(tzSimple);
//
//        return (formatter.format(calendarNow.getTime()));
//        }
// --Commented out by Inspection STOP (14/02/05 14:54)


    //----------------------------------------------------------------------------------------------
    // Find the time Now, returned with the specified format

// --Commented out by Inspection START (14/02/05 14:54):
//    public static String timeNow(final String strFormat)
//        {
//        // Make a time zone, by a rather odd route...
//        final TimeZone timezone = TimeZone.getDefault();
//        final SimpleTimeZone tzSimple = new SimpleTimeZone(timezone.getRawOffset(),
//                                                     timezone.getID());
//
//        // Set the DST rules, otherwise we don't get any...
//        tzSimple.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
//        tzSimple.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
//
//        // Find out where we are in the GregorianCalendar, using the SimpleTimeZone
//        final GregorianCalendar calendarNow = new GregorianCalendar(tzSimple);
//        calendarNow.setTime(new Date());
//
//        // Set up the formatter
//        final SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
//        formatter.setCalendar(calendarNow);
//        formatter.setTimeZoneCode(tzSimple);
//
//        return (formatter.format(calendarNow.getTime()));
//        }
// --Commented out by Inspection STOP (14/02/05 14:54)


    //----------------------------------------------------------------------------------------------
    // Find the time Now in the specified TimeZone, returned with the specified format

    public static String timeNow(final TimeZone timezone,
                                 final String format)
        {
        // Find out where we are in the GregorianCalendar
        final GregorianCalendar calendarNow = new GregorianCalendar(timezone);
        calendarNow.setTime(new Date());

        // Set up the formatter
        // ToDo Consider ThreadLocal
        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setCalendar(calendarNow);
        formatter.setTimeZone(timezone);

        return (formatter.format(calendarNow.getTime()));
        }


    /***********************************************************************************************
     * Calculate the <b>Time</b> difference (0...24hr) between two java.sql.Times.
     * Ignore the order in which the parameters are given, i.e. 'negative Time' not supported.
     * The resultant Time is on the day of TimeFirst.
     *
     * @param timezone
     * @param locale
     * @param timeFirst
     * @param timeLast
     *
     * @return GregorianCalendar difference
     */

    public static GregorianCalendar getCalendarDifference(final TimeZone timezone,
                                                          final Locale locale,
                                                          final Time timeFirst,
                                                          final Time timeLast)
        {
        final GregorianCalendar calendarFirst;
        final GregorianCalendar calendarLast;
        final GregorianCalendar calendarDiff;
        long longDiff;
        final int intHours;
        final int intMinutes;
        final int intSeconds;
        final int intMilliseconds;

        // ToDo null checks
        // Set up a Calendar to return the Time difference
        calendarDiff = new GregorianCalendar(timezone, locale);

        try
            {
            // Pass the Times through Calendars in case there are DST problems...
            //System.out.println("timeFirst.toString " + timeFirst);
            calendarFirst = (GregorianCalendar) ChronosHelper.parseCalendarFromTime(timezone,
                                                                     locale,
                                                                     ChronosHelper.TIME_FORMAT,
                                                                     timeFirst.toString(),
                                                                     getSystemDateNow());
//            System.out.println("\n\nFIRST CALENDAR " + debugCalendar(calendarFirst));
//            System.out.println("calendarFirst.toString " + toTimeString(calendarFirst));
//
//            System.out.println("timeLast.toString " + timeLast);
            calendarLast = (GregorianCalendar) ChronosHelper.parseCalendarFromTime(timezone,
                                                                    locale,
                                                                    ChronosHelper.TIME_FORMAT,
                                                                    timeLast.toString(),
                                                                    getSystemDateNow());
//            System.out.println("LAST CALENDAR " + debugCalendar(calendarLast));
//            System.out.println("calendarLast.toString " + toTimeString(calendarLast));

            // Ignore the order in which the parameters are given.
            longDiff = Math.abs(calendarLast.getTimeInMillis() - calendarFirst.getTimeInMillis());

            // It might be a rollover!
            // although it shouldn't be, if both Times are on the same day...
            while (longDiff > ChronosHelper.DAY_MILLISECONDS)
                {
                longDiff -= ChronosHelper.DAY_MILLISECONDS;
                }

            calendarDiff.setTimeInMillis(timeFirst.getTime());
//            System.out.println("DIFF CALENDAR set to timeFirst " + debugCalendar(calendarDiff));

            // Return to midnight on the day of the first Time
            calendarDiff.set(Calendar.MILLISECOND, 0);
            calendarDiff.set(Calendar.SECOND, 0);
            calendarDiff.set(Calendar.MINUTE, 0);
            calendarDiff.set(Calendar.HOUR_OF_DAY, 0);
//            System.out.println("DIFF CALENDAR set to midnight " + debugCalendar(calendarDiff));

            // Move to the Time as given by the Difference
            intHours = (int)(longDiff / ChronosHelper.HOUR_MILLISECONDS);
            intMinutes = (int)((longDiff - (intHours * ChronosHelper.HOUR_MILLISECONDS)) / ChronosHelper.MINUTE_MILLISECONDS);
            intSeconds = (int)((longDiff - ((intHours * ChronosHelper.HOUR_MILLISECONDS) + (intMinutes * ChronosHelper.MINUTE_MILLISECONDS))) / ChronosHelper.SECOND_MILLISECONDS);
            intMilliseconds = (int)(longDiff - ((intHours * ChronosHelper.HOUR_MILLISECONDS) + (intMinutes * ChronosHelper.MINUTE_MILLISECONDS) + (intSeconds * ChronosHelper.SECOND_MILLISECONDS)));

//            System.out.println("longDiff=" + longDiff);
//            System.out.println("longDiff++=" + ((intHours*3600000) + (intMinutes*60000) + (intSeconds*1000) + intMilliseconds));
//            System.out.println("intHours=" + intHours);
//            System.out.println("intMinutes=" + intMinutes);
//            System.out.println("intSeconds=" + intSeconds);
//            System.out.println("intMilliseconds=" + intMilliseconds);

            calendarDiff.add(Calendar.MILLISECOND, intMilliseconds);
            calendarDiff.add(Calendar.SECOND, intSeconds);
            calendarDiff.add(Calendar.MINUTE, intMinutes);
            calendarDiff.add(Calendar.HOUR_OF_DAY, intHours);
//            System.out.println("DIFF CALENDAR set to diff " + debugCalendar(calendarDiff) + "\n\n");
            }
        catch (ParseException e)
            {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        return (calendarDiff);
        }


    /***********************************************************************************************
     * Calculate the <b>Time</b> difference (0...24hr) between two java.sql.Times.
     * Ignore the order in which the parameters are given, i.e. 'negative Time' not supported.
     * The resultant Time is on the day of TimeFirst.
     *
     * @param timezone
     * @param locale
     * @param timeFirst
     * @param timeLast
     *
     * @return GregorianCalendar difference
     */

    public static Time getTimeDifference(final TimeZone timezone,
                                                  final Locale locale,
                                                  final Time timeFirst,
                                                  final Time timeLast)
        {
        // Return a Time {00:00:00...23:59:59}
        // whose Date part is that of TimeFirst (we hope!)
        return (ChronosHelper.getTimeFromCalendar(getCalendarDifference(timezone,
                                                          locale,
                                                          timeFirst,
                                                          timeLast)));
        }


    /***********************************************************************************************
     * Show the difference between two times on an Indicator,
     * warning the user if the times are not on the same day.
     *
     * @param clock
     * @param timezone
     * @param locale
     * @param datefirst
     * @param timefirst
     * @param datesecond
     * @param timesecond
     * @param status
     * @param tooltip
     * @param tooltiperror
     *
     * @throws IndicatorException
     */

    public static void showElapsedTime(final ToolbarIndicator clock,
                                       final TimeZone timezone,
                                       final Locale locale,
                                       final Date datefirst,
                                       final Time timefirst,
                                       final Date datesecond,
                                       final Time timesecond,
                                       final String status,
                                       final String tooltip,
                                       final String tooltiperror) throws IndicatorException
        {
        final String DATE_PARSE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        final GregorianCalendar calendarFirst;
        final GregorianCalendar calendarSecond;
        final GregorianCalendar calendarDiff;

        if ((clock != null)
            && (timezone != null)
            && (locale != null)
            && (datefirst != null)
            && (timefirst != null)
            && (datesecond != null)
            && (timesecond != null))
            {
            // Warn the User if the ElapsedTime is likely to be misleading...
            if (onSameDay(timezone,
                          locale,
                          datefirst,
                          datesecond))
                {
                clock.setStatus(status);
                clock.setToolTip(tooltip);
                }
            else
                {
                // The Times are not on the same Day
                clock.setStatus(status);
                clock.setToolTip(tooltiperror);
                clock.setValueForeground(Color.RED);
                }

            // BODGE! Create Calendars for each Time
            // There must be a better way...
            try
                {
                calendarFirst = (GregorianCalendar) ChronosHelper.parseCalendar(timezone,
                                                                 locale,
                                                                 DATE_PARSE_FORMAT,
                                                                 datefirst.toString()
                                                                     + SPACE
                                                                     + timefirst.toString());
//                System.out.println("calendarFirst " + debugCalendar(calendarFirst));
//                System.out.println("calendarFirst.toString " + toTimeString(calendarFirst));

                calendarSecond = (GregorianCalendar) ChronosHelper.parseCalendar(timezone,
                                                                  locale,
                                                                  DATE_PARSE_FORMAT,
                                                                  datesecond.toString()
                                                                      + SPACE
                                                                      + timesecond.toString());
//                System.out.println("calendarSecond " + debugCalendar(calendarSecond));
//                System.out.println("calendarSecond.toString " + toTimeString(calendarSecond));
//                System.out.println("calendarFirst.getTimeFromCalendar(calendarFirst) " + getTimeFromCalendar(calendarFirst));
//                System.out.println("calendarSecond.getTimeFromCalendar(calendarSecond) " + getTimeFromCalendar(calendarSecond));

                // Only use the Time parts of each Calendar
                // It doesn't matter which day, since we only want hh:mm:ss
                calendarDiff = getCalendarDifference(timezone,
                                                     locale,
                                                     ChronosHelper.getTimeFromCalendar(calendarFirst),
                                                     ChronosHelper.getTimeFromCalendar(calendarSecond));

                // Show the elapsed Time, with no TimeZone
                clock.setValue(ChronosHelper.toTimeString(calendarDiff));
                }

            catch (ParseException exception)
                {
                throw new IndicatorException(exception.getMessage());
                }
            }
        else
            {
            // Warn the User of configuration errors, if possible...
            if (clock != null)
                {
                clock.setValue("Error");
                }
            }
        }


    /***********************************************************************************************
     * Convert HH:MM:SS.ss to a milliseconds value.
     *
     * @param hours
     * @param minutes
     * @param seconds
     * @param milliseconds
     *
     * @return long
     */

    public static long convertToMillis(final int hours,
                                       final int minutes,
                                       final int seconds,
                                       final int milliseconds)
        {
        return ((hours*3600000)
                + (minutes*60000)
                + (seconds*1000)
                + milliseconds);
        }


    /***********************************************************************************************
     *
     * @param calendar
     */

    public static String debugCalendar(final Calendar calendar)
        {
        if (calendar != null)
            {
            return ("NULL");
            }
        else
            {
            return(calendar.get(Calendar.YEAR)
                                   + "-"
                                   + (calendar.get(Calendar.MONTH) + 1)
                                   + "-"
                                   + calendar.get(Calendar.DAY_OF_MONTH)
                                   + "  "
                                   + calendar.get(Calendar.HOUR_OF_DAY)
                                   + ":"
                                   + calendar.get(Calendar.MINUTE)
                                   + ":"
                                   + calendar.get(Calendar.SECOND)
                                   + "."
                                   + calendar.get(Calendar.MILLISECOND));
            }
        }


    /***********************************************************************************************
     *
     * @param calendar
     */

    public static void debugFullCalendar(final Calendar calendar)
        {
        LOGGER.debug("getTime() " + calendar.getTime().toString());
        LOGGER.debug("getTimeInMillis() " + calendar.getTimeInMillis());
        System.out.println("ERA: " + calendar.get(Calendar.ERA));
        System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
        System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
        System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
        System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
        System.out.println("DATE: " + calendar.get(Calendar.DATE));
        System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
        System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
        System.out.println("DAY_OF_WEEK_IN_MONTH: " + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
        System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
        System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
        System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
        System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
        System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
        System.out.println("ZONE_OFFSET: " + (calendar.get(Calendar.ZONE_OFFSET) / (60 * 60 * 1000)));
        System.out.println("DST_OFFSET: " + (calendar.get(Calendar.DST_OFFSET) / (60 * 60 * 1000)));
        }


    /***********************************************************************************************
     *
     */

    public Chronos()
        {
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
