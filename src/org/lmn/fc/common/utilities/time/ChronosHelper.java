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

package org.lmn.fc.common.utilities.time;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


public final class ChronosHelper
    {
    public static final long DAY_MILLISECONDS = 86400000;
    public static final long HOUR_MILLISECONDS = 3600000;
    public static final long MINUTE_MILLISECONDS = 60000;
    public static final long SECOND_MILLISECONDS = 1000;
    public static final String TIME_FORMAT = "HH:mm:ss";


    /***********************************************************************************************
     * Show the Date value of a Calendar as a String.
     * This is putting right more problems with Java Dates...
     *
     * @param calendar
     *
     * @return String
     */

    public static String toDateString(final Calendar calendar)
        {
        final StringBuffer bufferDate;

        bufferDate = new StringBuffer();

        if (calendar != null)
            {
            bufferDate.append(Integer.toString(calendar.get(Calendar.YEAR)));
            bufferDate.append("-");

            // Months {0...11} sigh
            if (calendar.get(Calendar.MONTH) < 9)
                {
                bufferDate.append("0");
                bufferDate.append((calendar.get(Calendar.MONTH) + 1));
                }
            else
                {
                bufferDate.append(Integer.toString((calendar.get(Calendar.MONTH) + 1)));
                }
            bufferDate.append("-");

            if (calendar.get(Calendar.DATE) < 10)
                {
                bufferDate.append("0");
                bufferDate.append(calendar.get(Calendar.DATE));
                }
            else
                {
                bufferDate.append(Integer.toString(calendar.get(Calendar.DATE)));
                }
            }

        return (bufferDate.toString());
        }


    /***********************************************************************************************
     * Show the Time value of a Calendar as a String.
     * This is putting right more problems with Java Dates...
     *
     * @param calendar
     *
     * @return String
     */

    public static String toTimeString(final Calendar calendar)
        {
        String strHour;
        String strMinute;
        String strSecond;

        strHour = "";
        strMinute = "";
        strSecond = "";

        if (calendar != null)
            {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
                {
                strHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
                }
            else
                {
                strHour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
                }

            if (calendar.get(Calendar.MINUTE) < 10)
                {
                strMinute = "0" + calendar.get(Calendar.MINUTE);
                }
            else
                {
                strMinute = Integer.toString(calendar.get(Calendar.MINUTE));
                }

            if (calendar.get(Calendar.SECOND) < 10)
                {
                strSecond = "0" + calendar.get(Calendar.SECOND);
                }
            else
                {
                strSecond = Integer.toString(calendar.get(Calendar.SECOND));
                }
            }

        return (strHour + ":" + strMinute + ":" + strSecond);
        }


    /**********************************************************************************************/
    /* Display Functions                                                                          */
    /***********************************************************************************************
     * Show the value of a Calendar as a String.
     * This is putting right more problems with Java Dates...
     *
     * @param calendar
     *
     * @return String
     */

    public static String toCalendarString(final Calendar calendar)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append(toDateString(calendar));
        buffer.append(" ");
        buffer.append(toTimeString(calendar));

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Parse a Date string into a Calendar according to the specified Date format,
     * for the specified TimeZone and Locale.
     *
     * @param timezone
     * @param locale
     * @param dateformat
     * @param datestring
     *
     * @return Calendar
     *
     * @throws ParseException
     */

    public static Calendar parseCalendar(final TimeZone timezone,
                                         final Locale locale,
                                         final String dateformat,
                                         final String datestring) throws ParseException
        {
        final SimpleDateFormat dateFormat;
        final GregorianCalendar calendar;
        final java.sql.Date date;

        // ToDo Consider ThreadLocal
        dateFormat = new SimpleDateFormat(dateformat, locale);
        dateFormat.setTimeZone(timezone);
        date = new java.sql.Date(dateFormat.parse(datestring).getTime());

        // Take the Date via a calendar to avoid any incomprehensible changes...
        calendar = new GregorianCalendar(timezone, locale);
        calendar.setTimeInMillis(date.getTime());

        return (calendar);
        }


    /***********************************************************************************************
     * Parse a Time string. Apply today's Date before parsing, to correct (?!) bugs in Java (?!).
     *
     * @param timezone
     * @param locale
     * @param timeformat
     * @param timestring
     * @param dateprefix
     *
     * @return Calendar
     *
     * @throws ParseException
     */

    public static Calendar parseCalendarFromTime(final TimeZone timezone,
                                                 final Locale locale,
                                                 final String timeformat,
                                                 final String timestring,
                                                 final java.sql.Date dateprefix) throws ParseException
        {
        return (parseCalendar(timezone,
                              locale,
                              "yyyy-MM-dd " + timeformat,
                              dateprefix.toString() + FrameworkStrings.SPACE + timestring));
        }


    /***********************************************************************************************
     * A utility to return the Time part of a calendar.
     * The Date part is set to 1970-01-01.
     *
     * @param calendar
     *
     * @return java.sql.Time
     */

    public static Time getTimeFromCalendar(final Calendar calendar)
        {
        // Parse the Time back again to avoid more problems with Java Dates...
        // Specifically, Time.toString() doesn't give the same hours back!
        return (Time.valueOf(toTimeString(calendar)));
        }


    /***********************************************************************************************
     * Parse a Time from the specified String.
     *
     * @param timestring
     * @param dateprefix
     *
     * @return java.sql.Time
     */

    public static Time parseTime(final String timestring,
                                 final java.sql.Date dateprefix)
        {
        try
            {
            final Calendar calendarTime;
            final TimeZone timeZone;
            final Locale locale;

            if (timestring != null)
                {
                timeZone = FrameworkSingletons.REGISTRY.getFrameworkTimeZone();
                locale = new Locale(FrameworkSingletons.REGISTRY.getFramework().getLanguageISOCode(),
                                    FrameworkSingletons.REGISTRY.getFramework().getCountryISOCode());
                calendarTime = parseCalendarFromTime(timeZone,
                                                     locale,
                                                     TIME_FORMAT,
                                                     timestring,
                                                     dateprefix);

                return (getTimeFromCalendar(calendarTime));
                }
            else
                {
                return (null);
                }
            }

        catch (ParseException exception)
            {
            return (null);
            }
        }
    }
