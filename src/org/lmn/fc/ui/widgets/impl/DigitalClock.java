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

//------------------------------------------------------------------------------
// Create a Digital Clock
//------------------------------------------------------------------------------
// Revision History
//
//  18-04-02    LMN created file from Indicator.java
//
//------------------------------------------------------------------------------
// Widgets package

package org.lmn.fc.ui.widgets.impl;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.AstronomicalCalendar;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.ui.widgets.DigitalClockInterface;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

//------------------------------------------------------------------------------

public final class DigitalClock extends Indicator
                                implements DigitalClockInterface
    {
    private FrameworkPlugin frameworkPlugin;
    private TimeSystem timeSystem;                          // Indicates which time to display
    private int intUpdatePeriodMillis;                      // Milliseconds between updates
    private Timer clockTimer;                               // The Clock timer
    private AstronomicalCalendarInterface clockCalendar;    // The calendar used for the clock
    private boolean boolShowTimeSystem;
    private SwingWorker workerClock;
    private ObservatoryClockInterface clockInterface;


    /***********************************************************************************************
     * Construct a DigitalClock using the ObservatoryClock as a timing reference.
     *
     * @param framework
     * @param dimIndicator
     * @param clock
     * @param border
     */

    public DigitalClock(final FrameworkPlugin framework,
                        final Dimension dimIndicator,
                        final ObservatoryClockInterface clock,
                        final Border border)
        {
        // Make the Indicator Panel and capture the parameters
        super(dimIndicator,
              EMPTY_STRING,
              EMPTY_STRING,
              EMPTY_STRING,
              EMPTY_STRING,
              border);

        this.clockInterface = clock;

        // Use the value contents to format the output
        setValueFormat(EMPTY_STRING);

        frameworkPlugin = framework;
        timeSystem = TimeSystem.LMT;
        intUpdatePeriodMillis = 1000;
        boolShowTimeSystem = false;

        initialiseClock(framework.getTimeZoneCode(), framework.getLongitude());
        }


    /***********************************************************************************************
     * Make a DigitalClock *with* a status field.
     *
     * @param framework
     * @param dimIndicator
     * @param strValue
     * @param strUnits
     * @param strStatus
     * @param strToolTip
     * @param border
     */

    public DigitalClock(final FrameworkPlugin framework,
                        final Dimension dimIndicator,
                        final String strValue,
                        final String strUnits,
                        final String strStatus,
                        final String strToolTip,
                        final Border border)
        {
        // Make the Indicator Panel and capture the parameters
        super(dimIndicator,
              strValue,
              strUnits,
              strStatus,
              strToolTip,
              border);

        this.clockInterface = null;

        // Use the value contents to format the output
        setValueFormat(strValue);

        frameworkPlugin = framework;
        timeSystem = TimeSystem.LMT;
        intUpdatePeriodMillis = 1000;
        boolShowTimeSystem = false;

        try
            {
            initialiseClock(framework.getTimeZoneCode(), framework.getLongitude());
            }

        catch(Exception exception)
            {
//            ExceptionLibrary.handleCoreException(exception,
//                                             this.getClass().getName(),
//                                             ExceptionLibrary.EXCEPTION_CONSTRUCT_CLOCK,
//                                             EventStatus.FATAL);
            }
        }


    /***********************************************************************************************
     * Construct a DigitalClock using the ObservatoryClock as a timing reference.
     * An alternative constructor for an DigitalClock *without* a status field.
     *
     *
     * @param dimIndicator
     * @param strValue
     * @param strUnits
     * @param strToolTip
     * @param timezonecode
     * @param longitude
     * @param timesystem
     * @param showtimesystem
     * @param clock
     * @param border
     */

    public DigitalClock(final Dimension dimIndicator,
                        final String strValue,
                        final String strUnits,
                        final String strToolTip,
                        final String timezonecode,
                        final DegMinSecInterface longitude,
                        final TimeSystem timesystem,
                        final boolean showtimesystem,
                        final ObservatoryClockInterface clock,
                        final Border border)
        {
        super(dimIndicator,
              strValue,
              strUnits,
              strToolTip,
              border);

        this.clockInterface = clock;

        // Use the value contents to format the output
        setValueFormat(strValue);

        timeSystem = timesystem;
        intUpdatePeriodMillis = 1000;
        this.boolShowTimeSystem = showtimesystem;

        initialiseClock(timezonecode, longitude);
        }


    //--------------------------------------------------------------------------
    // An alternative constructor for an DigitalClock *without* a status field

    public DigitalClock(final Dimension dimIndicator,
                        final String strValue,
                        final String strUnits,
                        final String strToolTip,
                        final String timezonecode,
                        final DegMinSecInterface longitude,
                        final TimeSystem timesystem,
                        final boolean showtimesystem,
                        final Border border)
        {
        super(dimIndicator,
              strValue,
              strUnits,
              strToolTip,
              border);

        this.clockInterface = null;

        // Use the value contents to format the output
        setValueFormat(strValue);

        timeSystem = timesystem;
        intUpdatePeriodMillis = 1000;
        this.boolShowTimeSystem = showtimesystem;

        try
            {
            initialiseClock(timezonecode, longitude);
            }

        catch(Exception exception)
            {
//            ExceptionLibrary.handleCoreException(exception,
//                                             this.getClass().getName(),
//                                             ExceptionLibrary.EXCEPTION_CONSTRUCT_CLOCK,
//                                             EventStatus.FATAL);
            }
        }


    //--------------------------------------------------------------------------
    // Constructor utility
    // ToDo - is there a way of moving this to the Observatory?

    private void initialiseClock(final String timezonecode,
                                 final DegMinSecInterface longitude)
        {
        final TimeZone timeZone;
        final double dblLongitude;
        final StringBuffer buffer;

        timeZone = TimeZone.getTimeZone(timezonecode);
        dblLongitude = longitude.toDouble();
        buffer = new StringBuffer();

        // Find the time NOW, since we must initialise clockCalendar
        if (getObservatoryClock() != null)
            {
            clockCalendar = new AstronomicalCalendar(getObservatoryClock().getSystemDateNow(),
                                                     TimeZone.getTimeZone(timezonecode),
                                                     longitude.toDouble());
            }
        else
            {
            clockCalendar = new AstronomicalCalendar(Chronos.getSystemDateNow(),
                                                     TimeZone.getTimeZone(timezonecode),
                                                     longitude.toDouble());
            }

        getCalendar().enableFormatSign(false);

        // Set up a Timer to tick the clock...
        clockTimer = new Timer(intUpdatePeriodMillis, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Find the time NOW
                if (getObservatoryClock() != null)
                    {
                    clockCalendar = new AstronomicalCalendar(getObservatoryClock().getSystemDateNow(),
                                                             timeZone,
                                                             dblLongitude);
                    }
                else
                    {
                    clockCalendar = new AstronomicalCalendar(Chronos.getSystemDateNow(),
                                                             timeZone,
                                                             dblLongitude);
                    }

                getCalendar().enableFormatSign(false);

                buffer.setLength(0);
                buffer.append(getCalendar().toString_HH_MM_SS(getTimeSystem()));

                if (boolShowTimeSystem)
                    {
                    buffer.append(SPACE);
                    buffer.append(getTimeSystem().getMnemonic());
                    }

                setValue(buffer.toString());
                }
            });

        clockTimer.setCoalesce(false);
        }


    // Set the full HMS format pattern string for output

//    public final void setHMSFormat(final String hoursformat,
//                                   final String minutesformat,
//                                   final String secondsformat)
//        {
//        if (clockCalendar != null)
//            {
//            clockCalendar.setHMSFormat(hoursformat, minutesformat, secondsformat);
//            }
//        }


    //--------------------------------------------------------------------------
    // Start the clock!

    public final void start()
        {
        clockTimer.start();
        }


    //--------------------------------------------------------------------------
    // Stop the clock!

    public final void stop()
        {
        clockTimer.stop();
        }


    //--------------------------------------------------------------------------
    // Set the clock time system
    // 0hr values don't change, of course, but they are here for consistency

    public final void setTimeSystem(final TimeSystem timesystem)
        {
        try
            {
            if (timesystem != null)
                {
                // Find out which time we have to display
                this.timeSystem = timesystem;

                // Resize Value and Status for the new displayed text for the new Time System
                setValueFormat(getCalendar().toString_HH_MM_SS(getTimeSystem()));
                setValue(getCalendar().toString_HH_MM_SS(getTimeSystem()));

                setStatusFormat(getTimeSystem().getName());
                setStatus(getTimeSystem().getName());

                setUnits(getTimeSystem().getMnemonic());
                setToolTip("Time Zone " + getFramework().getTimeZoneCode());
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (IndicatorException exception)
            {
            throw new FrameworkException(EXCEPTION_INVALIDTIMESYSTEM, exception);
            }
        }

    //--------------------------------------------------------------------------
    // Get the clock Time System

    public final TimeSystem getTimeSystem()
        {
        return(this.timeSystem);
        }


    //--------------------------------------------------------------------------
    // Set the clock update period {0...100,000 msec}

    public final void setUpdatePeriod(final int period) throws Exception
        {
        if ((period < 0) || (period > 100000))
            {
            throw new Exception(EXCEPTION_PARAMETER_RANGE + " {period=" + period + "}");
            }
        else
            {
            this.intUpdatePeriodMillis = period;
            }
        }


    //--------------------------------------------------------------------------
    // Get the clock update period

    public final int getUpdatePeriod()
        {
        return(this.intUpdatePeriodMillis);
        }


    //--------------------------------------------------------------------------
    // Get the clock calendar

    public final AstronomicalCalendarInterface getCalendar()
        {
        return(this.clockCalendar);
        }


    /***********************************************************************************************
     *
     * @return FrameworkPlugin
     */

    private FrameworkPlugin getFramework()
        {
        return frameworkPlugin;
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private synchronized ObservatoryClockInterface getObservatoryClock()
        {
        return (this.clockInterface);
        }
    }
