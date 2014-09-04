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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.clocks;

import org.jfree.data.time.TimeSeries;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


/***********************************************************************************************
 * SimplePlatformClock.
 * A simple clock driven by the host platform clock, for use when no other clock is available.
 */

public final class SimplePlatformClock implements ObservatoryClockInterface,
                                                  FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ResourceKeys
    {
    private static final long TIMER_PERIOD_MILLIS = 100;
    private static final long UPDATE_RATE_MILLIS = 300;

    // Injections
    private final ObservatoryClockDAOInterface daoClock;

    // The Timer which checks to see if the clock has changed
    private final javax.swing.Timer timerCheckChanged;

    // The Time at which the last Timer event was notified to the listeners
    private long longLastNotifiedTickMillis;

    private AstronomicalCalendarInterface calendarAstro;


    /***********************************************************************************************
     * Create the Clock.
     *
     * @param dao
     */

    public SimplePlatformClock(final ObservatoryClockDAOInterface dao)
        {
        final ObservatoryClockInterface thisClock;

        thisClock = this;

        // Injections
        // The DAO may be null, but then no notifications of changes to listeners will occur
        this.daoClock = dao;

        this.timerCheckChanged = new Timer((int)TIMER_PERIOD_MILLIS, new ActionListener()
            {
            /***************************************************************************************
             * This operation must be very fast!
             *
             * @param event
             */

            public void actionPerformed(final ActionEvent event)
                {
                final boolean boolChanged;

                // Update fast enough to catch each second for any displays
                boolChanged = (System.currentTimeMillis() - longLastNotifiedTickMillis) > UPDATE_RATE_MILLIS;

                // Tell the World that we ticked...
                if (boolChanged)
                    {
                    // Record the new time of the last *notified* System Timer tick
                    longLastNotifiedTickMillis = System.currentTimeMillis();

                    if (getClockDAO() != null)
                        {
                        getClockDAO().notifyObservatoryClockChangedEvent(thisClock, true);
                        }
                    }
                }
            });

        getTimer().setInitialDelay(0);

        // Initialise the tick time
        this.longLastNotifiedTickMillis = System.currentTimeMillis();

        this.calendarAstro = null;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the DAO which is synthesising this Clock, but as an ObservatoryClockDAOInterface.
     *
     * @return ObservatoryClockDAOInterface
     */

    public ObservatoryClockDAOInterface getClockDAO()
        {
        return (this.daoClock);
        }


    /**********************************************************************************************/
    /* See also: ObservatoryClock, NTPSynchronisedProxyClock                                      */
    /***********************************************************************************************
     * Start the ObservatoryClock.
     *
     * @return boolean
     */

    public boolean startClock()
        {
        final String SOURCE = "SimplePlatformClock.startClock() ";

        // The SimplePlatformClock is always running

        this.calendarAstro = ObservatoryClockHelper.createCalendar(this);

        this.longLastNotifiedTickMillis = System.currentTimeMillis();

        // Start the Timer which checks to see if the Seconds have changed
        if (getTimer() != null)
            {
            getTimer().start();
            }

        LOGGER.warn(SOURCE + "Clock started");

        return (true);
        }



    /***********************************************************************************************
     * Stop the ObservatoryClock.
     */

    public void stopClock()
        {
        final String SOURCE = "SimplePlatformClock.stopClock() ";

        // The SimplePlatformClock cannot be stopped

        this.calendarAstro = null;

        if (getTimer() != null)
            {
            getTimer().stop();
            }

        LOGGER.warn(SOURCE + "Clock stopped");
        }


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

    public boolean synchronise(final long offsetmillis,
                               final TimeSeries timeseries,
                               final TimeZone timezone,
                               final Locale locale,
                               final boolean verboselogging,
                               final Vector<Vector> log)
        {
        final String SOURCE = "SimplePlatformClock.synchroniseNow() ";

        // Synchronisation is not possible with the SimplePlatformClock
        SimpleEventLogUIComponent.logEvent(log,
                                           EventStatus.INFO,
                                           METADATA_TARGET_CLOCK
                                                + METADATA_ACTION_SYNCHRONISE
                                                + METADATA_RESULT + "Invalid command for this Clock" + TERMINATOR,
                                           SOURCE,
                                           this);
        return (true);
        }


    /***********************************************************************************************
     * Get the System Time in milliseconds.
     *
     * @return long
     */

    public long getSystemTimeMillis()
        {
        //LOGGER.debugTimedEvent("SimplePlatformClock obtaining SystemTime");

        return (Chronos.getSystemTime());
        }


    /**********************************************************************************************
     * Get the System Date, for SQL.
     *
     * @return Date
     */

    public java.sql.Date getSystemDateNow()
        {
        //LOGGER.debugTimedEvent("SimplePlatformClock obtaining SystemDateNow");

        return (Chronos.getSystemDateNow());
        }


    /***********************************************************************************************
     * Get the Calendar date NOW.
     *
     * @return GregorianCalendar
     */

    public GregorianCalendar getCalendarDateNow()
        {
        //LOGGER.debugTimedEvent("SimplePlatformClock obtaining CalendarDateNow");

        return (Chronos.getCalendarDateNow());
        }


    /***********************************************************************************************
     * Get the Calendar Time NOW.
     *
     * @return GregorianCalendar
     */

    public GregorianCalendar getCalendarTimeNow()
        {
        //LOGGER.debugTimedEvent("SimplePlatformClock obtaining CalendarTimeNow");

        return (Chronos.getCalendarTimeNow());
        }


    /***********************************************************************************************
     * Find the time Now in the format "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @return String
     */

    public String getDateTimeNowAsString()
        {
        //LOGGER.debugTimedEvent("SimplePlatformClock obtaining DateTimeNowAsString");

        return (Chronos.timeNow());
        }


    /***********************************************************************************************
     * Get the System time as a Calendar.
     *
     * @param timezone
     * @param locale
     *
     * @return Calendar
     */

    public Calendar getSystemCalendar(final TimeZone timezone,
                                      final Locale locale)
        {
        //LOGGER.debugTimedEvent("SimplePlatformClock obtaining SystemCalendar");

        return (Chronos.getSystemCalendar(timezone, locale));
        }


    /***********************************************************************************************
     * Get the AstronomicalCalendar.
     *
     * @return AstronomicalCalendarInterface
     */

    public AstronomicalCalendarInterface getAstronomicalCalendar()
        {
        // This will recalculate all astronomical TimeSystems
        if (this.calendarAstro != null)
            {
            this.calendarAstro.setTimeInMillis(getSystemTimeMillis());
            }

        return (this.calendarAstro);
        }


    /***********************************************************************************************
     * Get the Timer which checks to see if Seconds have changed.
     *
     * @return Timer
     */

    private javax.swing.Timer getTimer()
        {
        return (this.timerCheckChanged);
        }
    }
