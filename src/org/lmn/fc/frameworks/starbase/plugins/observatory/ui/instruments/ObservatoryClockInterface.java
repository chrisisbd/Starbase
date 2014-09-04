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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.jfree.data.time.TimeSeries;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockDAOInterface;

import java.util.*;


/***************************************************************************************************
 * ObservatoryClockInterface.
 */

public interface ObservatoryClockInterface
    {
    String ISO_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


    /***********************************************************************************************
     * Get the DAO which is synthesising this Clock.
     *
     * @return ObservatoryClockDAOInterface
     */

    ObservatoryClockDAOInterface getClockDAO();


    /***********************************************************************************************
     * Start the ObservatoryClock.
     *
     * @return boolean
     */

    boolean startClock();


    /***********************************************************************************************
     * Stop the ObservatoryClock.
     */

    void stopClock();


    /***********************************************************************************************
     * Synchronise the Clock using the specified time offset in milliseconds.
     * Long.MAX_VALUE specified if synchronisation failed.
     *
     * @param offsetmillis
     * @param timeseries
     * @param timezone
     * @param locale
     * @param verboselogging
     * @param log
     *
     * @return boolean
     */

    boolean synchronise(long offsetmillis,
                        TimeSeries timeseries,
                        TimeZone timezone,
                        Locale locale,
                        final boolean verboselogging,
                        Vector<Vector> log);


    /***********************************************************************************************
     * Get the System Time in milliseconds.
     *
     * @return long
     */

    long getSystemTimeMillis();


    /**********************************************************************************************
     * Get the System Date, for SQL.
     *
     * @return Date
     */

    java.sql.Date getSystemDateNow();


    /***********************************************************************************************
     * Get the Calendar Date NOW.
     *
     * @return GregorianCalendar
     */

    GregorianCalendar getCalendarDateNow();


    /***********************************************************************************************
     * Get the Calendar Time NOW.
     *
     * @return GregorianCalendar
     */

    GregorianCalendar getCalendarTimeNow();


    /***********************************************************************************************
     * Find the time Now in the ISO format "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @return String
     */

    String getDateTimeNowAsString();


    /***********************************************************************************************
     * Get the System time as a Calendar.
     *
     * @param timezone
     * @param locale
     *
     * @return Calendar
     */

    Calendar getSystemCalendar(TimeZone timezone, Locale locale);


    /***********************************************************************************************
     * Get the AstronomicalCalendar.
     *
     * @return AstronomicalCalendarInterface
     */

    AstronomicalCalendarInterface getAstronomicalCalendar();
    }
