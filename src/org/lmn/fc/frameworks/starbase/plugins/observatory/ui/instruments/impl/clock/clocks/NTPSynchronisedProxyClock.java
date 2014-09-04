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
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.logging.EventStatus;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;


/***********************************************************************************************
 * NTPSynchronisedProxyClock.
 */

public final class NTPSynchronisedProxyClock implements ObservatoryClockInterface,
                                                        FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        ResourceKeys
    {
    private static final long TIMER_PERIOD_MILLIS = 250;
    private static final long MIN_TIMER_PERIOD_MILLIS = 230;
    private static final long MAX_TIMER_PERIOD_MILLIS = 270;
    private static final long TRIGGER_OFFSET_MILLIS = 250;
    private static final int DRIFT_RATE_AVERAGE_COUNT = 2;

    // Injections
    private final ObservatoryClockDAOInterface daoClock;

    // Indicates if the clock is running
    private boolean boolIsRunning;

    // This is the source of Time!
    private long longProxyTimeMillis;

    // The number of millseconds to add on each update
    // at TIMER_PERIOD_MILLIS in order to keep the Clock in sync
    private long longTimestepMillis;

    // The time at which the Clock was last synchronised
    private long longLastSyncMillis;
    private boolean boolIsSynchronised;

    // The Time at which the last Timer event occurred
    private long longLastSystemTimerTickMillis;

    private final double[] arrayAvgDriftRateMillisPerMilli;
    private int intDriftRatePointer;

    private final Timer timerUpdate;
    private final GregorianCalendar calendar;
    private final TimeZone timeZone;
    private final Locale locale;

    private AstronomicalCalendarInterface calendarAstro;


    /***********************************************************************************************
     * Create the Clock.
     *
     * @param dao
     */

    public NTPSynchronisedProxyClock(final ObservatoryClockDAOInterface dao)
        {
        final ObservatoryClockInterface thisClock;

        thisClock = this;

        this.daoClock = dao;

        this.boolIsRunning = false;
        this.boolIsSynchronised = false;

        this.timeZone = REGISTRY.getFrameworkTimeZone();
        this.locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                 REGISTRY.getFramework().getCountryISOCode());
        this.calendar = new GregorianCalendar(this.timeZone, this.locale);

        // It doesn't really matter what these values are, since we will synchronise later
        // Do not set these in startClock(), since they will be reset every synchronise() !!
        this.longProxyTimeMillis = System.currentTimeMillis();
        this.longLastSyncMillis = longProxyTimeMillis;
        this.longTimestepMillis = TIMER_PERIOD_MILLIS;
        this.longLastSystemTimerTickMillis = longProxyTimeMillis;
        this.arrayAvgDriftRateMillisPerMilli = new double[] {0.0, 0.0, 0.0, 0.0};
        this.intDriftRatePointer = 0;

        timerUpdate = new Timer((int)TIMER_PERIOD_MILLIS, new ActionListener()
            {
            /***************************************************************************************
             * This operation must be very fast!
             *
             * @param event
             */

            public void actionPerformed(final ActionEvent event)
                {
                final long longSinceLastTimerTickMillis;
                //final long longSinceLastSyncMillis;
                final long longAdjustedTimestepMillis;
                //final long longPredictedOffsetMillis;

                // The Timestep is how much to add to the ObservatoryClock to give one clock tick.
                // If there were no drift, this would be the same as TIMER_PERIOD_MILLIS.
                // Calculate the Timestep on each Timer tick so as to keep the Clock accurate.

                // How much real time has elapsed according to the host platform clock?
                // These milliseconds are likely to be fairly accurate over a short time interval
                longSinceLastTimerTickMillis = System.currentTimeMillis() - longLastSystemTimerTickMillis;

                // Record the new time of the last System Timer tick (i.e. when the Timer went off)
                longLastSystemTimerTickMillis = System.currentTimeMillis();

                // How long since the last sync?
                //longSinceLastSyncMillis = longLastSystemTimerTickMillis - getLastSyncTimeMillis();

                //----------------------------------------------------------------------------------
                // Calculate the change to the timestep to account for the real elapsed time,
                // which should ideally be TIMER_PERIOD_MILLIS
                //
                // elapsed/timerperiod = adjustedtimestep/originaltimestep
                //
                // i.e. adjustedtimestep = (elapsed * originaltimestep) / timerperiod

                longAdjustedTimestepMillis = (longSinceLastTimerTickMillis * getTimestepMillis()) / TIMER_PERIOD_MILLIS;


                // Update the Proxy Time either with the real Timestep,
                // or the adjusted amount to cope with unpredicatable delays
                // TODO adjustable allowance for timestep variation ??
                if ((longAdjustedTimestepMillis >= (TIMER_PERIOD_MILLIS-20))
                    && (longAdjustedTimestepMillis <= (TIMER_PERIOD_MILLIS+20)))
                    {
                    // If there wasn't much change (drift), so just use the original Timestep
                    // to bring the ProxyTime up to the real NOW
                    setProxyTimeMillis(getProxyTimeMillis() + getTimestepMillis());
                    }
                else
                    {
                    // Otherwise change the timestep this time only
                    // This is a bit of a bodge, but it does seem to work!
                    // The Timer sometimes stops, for instance when loading RxTx libraries
                    //System.out.println("adj=" + longAdjustedTimestepMillis);
                    setProxyTimeMillis(getProxyTimeMillis() + longAdjustedTimestepMillis);
                    }

                //----------------------------------------------------------------------------------
                // Now check to see if the drift is large enough to warrant an auto-sync without NTP

//                longPredictedOffsetMillis = (long)(longSinceLastSyncMillis * getDriftRate());
//
//                // The drift rate could be negative
//                if (Math.abs(longPredictedOffsetMillis) > 200)
//                    {
//                    System.out.println("predicted offset=" + longPredictedOffsetMillis);
//                    setTimeMillis(getProxyTimeMillis() + longPredictedOffsetMillis);
//
//                    // Set the Time we last synchronised to what we now believe to be correct
//                    setLastSyncTimeMillis(getProxyTimeMillis());
//                    }

                // Tell the World that we ticked...
                // ...but only if we are synchronised
                if ((boolIsSynchronised)
                    && (getClockDAO() != null))
                    {
                    getClockDAO().notifyObservatoryClockChangedEvent(thisClock, true);
                    }
                }
            });

        timerUpdate.setInitialDelay(0);

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
    /* See also: ObservatoryClock, SimplePlatformClock                                            */
    /***********************************************************************************************
     * Start the ObservatoryClock.
     *
     * @return boolean
     */

    public synchronized boolean startClock()
        {
        final String SOURCE = "NTPSynchronisedProxyClock.startClock() ";

        boolean boolSuccess;

        boolSuccess = false;

        this.calendarAstro = ObservatoryClockHelper.createCalendar(this);

        if (timerUpdate != null)
            {
            timerUpdate.start();

            // Make sure that the Timer has recorded elapsed time correctly
            Utilities.safeSleep(TIMER_PERIOD_MILLIS << 1);
            boolSuccess = true;
            }

        if (boolSuccess)
            {
            boolIsRunning = true;
            }

        LOGGER.logTimedEvent(SOURCE + "Clock started");

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Stop the ObservatoryClock.
     */

    public synchronized void stopClock()
        {
        final String SOURCE = "NTPSynchronisedProxyClock.stopClock() ";

        this.calendarAstro = null;

        if (timerUpdate != null)
            {
            timerUpdate.stop();
            }

        LOGGER.logTimedEvent(SOURCE + "Clock stopped");

        boolIsRunning = false;
        }


    /***********************************************************************************************
     * Synchronise the Clock using the specified time offset in milliseconds.
     * Long.MAX_VALUE specified if synchronisation failed.
     *
     * @param offsetmillis
     * @param timeseries
     * @param timezone
     * @param obslocale
     * @param verboselogging
     * @param log
     *
     *  @return boolean
     */

    public synchronized boolean synchronise(final long offsetmillis,
                                            final TimeSeries timeseries,
                                            final TimeZone timezone,
                                            final Locale obslocale,
                                            final boolean verboselogging,
                                            final Vector<Vector> log)
        {
        final String SOURCE = "NTPSynchronisedProxyClock.synchronise() ";
        boolean boolSuccess;

        LOGGER.logTimedEvent(SOURCE + "[offset=" + offsetmillis + "]");

        boolSuccess = false;

        // Don't synchronise if something went wrong!
        if (offsetmillis != Long.MAX_VALUE)
            {
            // longProxyTimeMillis tells us what we think the Time should be according to the Timer
            // offsetmillis is to be added to longProxyTimeMillis in order make the time correct
            // getLastSyncTimeMillis() was the correct time when last synchronised

//            System.out.println("\nLASTSYNC=" + getLastSyncTimeMillis());
//            System.out.println("PROXYTIME=" + getProxyTimeMillis());
//            System.out.println("OFFSET=" + offsetmillis);

            // Don't modify the timestep the first time through,
            // in case the offset is untypical, so wait for synchronisation
            // The offset (which could be negative) has occurred over a period of
            // (longProxyTimeMillis + offsetmillis) - longLastSyncMillis
            // which *must* be a positive number
            if ((boolIsSynchronised)
                && ((getProxyTimeMillis() + offsetmillis - getLastSyncTimeMillis()) > 0))
                {
                final double dblDriftRateMillisPerMilli;
                final double dblAvgDriftRateMillisPerMilli;
                final String strAvgDriftRate;
                final long longNewTimestepMillis;

                // offsetmillis is how much the Clock has drifted since the last sync
                dblDriftRateMillisPerMilli = (double)offsetmillis / (double)((getProxyTimeMillis() + offsetmillis) - getLastSyncTimeMillis());
                //System.out.println("ObservatoryClock CURRENT DRIFT RATE=" + (dblDriftRateMillisPerMilli*1000) + " msec/sec");

                dblAvgDriftRateMillisPerMilli = calculateNewAverageDriftRate(dblDriftRateMillisPerMilli);
                //System.out.println("ObservatoryClock AVG DRIFT RATE=" + (dblAvgDriftRateMillisPerMilli*1000) + " msec/sec");

                // Calculate how much to change the Timestep to correct for the drift,
                // bearing in mind this occurred using the old value of getTimestepMillis()
                longNewTimestepMillis = getTimestepMillis() + (long)(dblAvgDriftRateMillisPerMilli*TIMER_PERIOD_MILLIS);

                // Format the drift rate sensibly...
                strAvgDriftRate = DecimalFormatPattern.DECIMAL_DOUBLE.format(dblAvgDriftRateMillisPerMilli * 1000);

                // Only modify the timestep if it stays inside the bounds specified
                // TODO MIN_TIMER_PERIOD_MILLIS and MAX_TIMER_PERIOD_MILLIS Configurable?
                if ((longNewTimestepMillis >= MIN_TIMER_PERIOD_MILLIS)
                    && (longNewTimestepMillis <= MAX_TIMER_PERIOD_MILLIS))
                    {
                    setTimestepMillis(longNewTimestepMillis);

                    if (verboselogging)
                        {
                        SimpleEventLogUIComponent.logEvent(log,
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_CLOCK
                                                            + METADATA_ACTION_SYNCHRONISE
                                                            + "[offset=" + offsetmillis  + " msec" + TERMINATOR_SPACE
                                                            + METADATA_DRIFT_RATE + strAvgDriftRate + " msec/sec" + TERMINATOR_SPACE
                                                            + "[timestep=" + getTimestepMillis()  + " msec" + TERMINATOR,
                                                       SOURCE,
                                                       this);
                        }
                    }
                else
                    {
                    if (verboselogging)
                        {
                        SimpleEventLogUIComponent.logEvent(log,
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_CLOCK
                                                            + METADATA_ACTION_SYNCHRONISE
                                                            + "[offset=" + offsetmillis  + " msec" + TERMINATOR_SPACE
                                                            + METADATA_DRIFT_RATE + strAvgDriftRate + " msec/sec" + TERMINATOR_SPACE
                                                            + "[timestep=" + longNewTimestepMillis + " msec" + TERMINATOR_SPACE
                                                            + METADATA_RESULT + "Drift rate is too high to compensate automatically" + TERMINATOR,
                                                       SOURCE,
                                                       this);
                        }
                    }
                }

            // Set the time to what we've been told is correct
            // Always synchronise, regardless of the drift rate calculation
            setProxyTimeMillis(getProxyTimeMillis() + offsetmillis);
            boolIsSynchronised = true;
            //System.out.println("CORRECTED TIME=" + getProxyTimeMillis() + " msec");

            // Set the Time we last synchronised to what we now believe to be correct
            setLastSyncTimeMillis(getProxyTimeMillis());
            boolSuccess = true;
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to synchronise");
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Get the System Time in milliseconds.
     *
     * @return long
     */

    public long getSystemTimeMillis()
        {
        //LOGGER.debugTimedEvent("NTPSynchronisedProxyClock obtaining SystemTime");

        return (getProxyTimeMillis());
        }


    /**********************************************************************************************
     * Get the System Date, for SQL.
     *
     * @return Date
     */

    public java.sql.Date getSystemDateNow()
        {
        //LOGGER.debugTimedEvent("NTPSynchronisedProxyClock obtaining SystemDateNow");

        return (new java.sql.Date(getProxyTimeMillis()));
        }


    /***********************************************************************************************
     * Get the Calendar date NOW.
     *
     * @return GregorianCalendar
     */

    public GregorianCalendar getCalendarDateNow()
        {
        //LOGGER.debugTimedEvent("NTPSynchronisedProxyClock obtaining CalendarDateNow");
        this.calendar.setTimeInMillis(getProxyTimeMillis());

        return (calendar);
        }


    /***********************************************************************************************
     * Get the Calendar Time NOW.
     *
     * @return GregorianCalendar
     */

    public GregorianCalendar getCalendarTimeNow()
        {
        //LOGGER.debugTimedEvent("NTPSynchronisedProxyClock obtaining CalendarTimeNow");
        this.calendar.setTimeInMillis(getProxyTimeMillis());

        return (calendar);
        }


    /***********************************************************************************************
     * Find the time Now in the format "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @return String
     */

    public String getDateTimeNowAsString()
        {
        final SimpleDateFormat formatter;

        //LOGGER.debugTimedEvent("NTPSynchronisedProxyClock obtaining DateTimeNowAsString");

        // Find out where we are in the GregorianCalendar
        this.calendar.setTimeZone(this.timeZone);
        this.calendar.setTimeInMillis(getProxyTimeMillis());

        // Set up the formatter
        // ToDo Consider ThreadLocal
        formatter = new SimpleDateFormat(ISO_DATE_FORMAT);
        formatter.setCalendar(this.calendar);
        formatter.setTimeZone(this.timeZone);

        return (formatter.format(this.calendar.getTime()));
        }


    /***********************************************************************************************
     * Get the System time as a Calendar.
     *
     * @param timezone
     * @param lcl
     *
     * @return Calendar
     */

    public Calendar getSystemCalendar(final TimeZone timezone,
                                      final Locale lcl)
        {
        final GregorianCalendar calendarNow;

        //LOGGER.debugTimedEvent("NTPSynchronisedProxyClock obtaining SystemCalendar");

        // We can't reset the Locale on the global Calendar, so we need a new Calendar
        calendarNow = new GregorianCalendar(timezone, lcl);
        calendarNow.setTimeInMillis(getProxyTimeMillis());

        return (calendarNow);
        }


    /***********************************************************************************************
     * Get the AstronomicalCalendar.
     *
     * @return AstronomicalCalendarInterface
     */

    public synchronized AstronomicalCalendarInterface getAstronomicalCalendar()
        {
        // This will recalculate all astronomical TimeSystems
        if (this.calendarAstro != null)
            {
            this.calendarAstro.setTimeInMillis(getSystemTimeMillis());
            }

        return (this.calendarAstro);
        }


    /***********************************************************************************************
     * Get the Proxy Time in milliseconds.
     * Return the host platform time if the Clock is not running.
     *
     * @return long
     */

    private synchronized long getProxyTimeMillis()
        {
        // Use the platform clock until synchronised at least once
        if ((boolIsRunning)
            && (boolIsSynchronised))
            {
            return (this.longProxyTimeMillis);
            }
        else
            {
            return (System.currentTimeMillis());
            }
        }


    /***********************************************************************************************
     * Set the Proxy Time in milliseconds.
     *
     * @param timemillis
     */

    private synchronized void setProxyTimeMillis(final long timemillis)
        {
        this.longProxyTimeMillis = timemillis;
        }


    /***********************************************************************************************
     * Get the Last Sync Time in milliseconds.
     *
     * @return long
     */

    private synchronized long getLastSyncTimeMillis()
        {
        return (this.longLastSyncMillis);
        }


    /***********************************************************************************************
     * Set the Last Sync Time in milliseconds.
     *
     * @param timemillis
     */

    private synchronized void setLastSyncTimeMillis(final long timemillis)
        {
        this.longLastSyncMillis = timemillis;
        }


    /***********************************************************************************************
     * Get the Timestep in milliseconds.
     *
     * @return long
     */

    private synchronized long getTimestepMillis()
        {
        return (this.longTimestepMillis);
        }


    /***********************************************************************************************
     * Set the Timestep in milliseconds.
     *
     * @param timemillis
     */

    private synchronized void setTimestepMillis(final long timemillis)
        {
        this.longTimestepMillis = timemillis;
        }


    /***********************************************************************************************
     * Calculate the new drift rate in msec/msec, averaged over DRIFT_RATE_AVERAGE_COUNT samples.
     *
     * @param currentrate
     *
     * @return double
     */

    private double calculateNewAverageDriftRate(final double currentrate)
        {
        // Put the current Drift Rate in the next available location
        arrayAvgDriftRateMillisPerMilli[intDriftRatePointer++] = currentrate;

        // Wrap at the end of the array
        intDriftRatePointer = intDriftRatePointer % DRIFT_RATE_AVERAGE_COUNT;

        return (getDriftRate());
        }


    /***********************************************************************************************
     * Get the current average DriftRate in msec/msec.
     *
     * @return
     */

    private double getDriftRate()
        {
        double dblSum;

        // Sum all the Drift Rates and produce the average
        dblSum = 0.0;

        for (int i = 0;
             i < DRIFT_RATE_AVERAGE_COUNT;
             i++)
            {
            dblSum += arrayAvgDriftRateMillisPerMilli[i];
            }

        return (dblSum / DRIFT_RATE_AVERAGE_COUNT);
        }
    }
