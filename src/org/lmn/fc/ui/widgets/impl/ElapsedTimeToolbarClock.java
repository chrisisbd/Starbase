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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  01-12-04    LMN created file
//  02-03-05    LMN changed constructor parameter to be a Time
//  10-03-05    LMN added StartDate, and used Chronos.showElapsedTime()
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.widgets.impl;

//--------------------------------------------------------------------------------------------------

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Time;
import java.util.Locale;
import java.util.TimeZone;


/***************************************************************************************************
 * An ElapsedTimeClock intended to be displayed on the Toolbar, as a ContextAction of a host Task.
 */

public final class ElapsedTimeToolbarClock extends ToolbarIndicator
                                           implements FrameworkConstants,
                                                      FrameworkStrings,
                                                      FrameworkSingletons,
                                                      FrameworkMetadata
    {
    // String Resources
    private static final String EXCEPTION_INDICATOR_CREATE  = "Unable to create Toolbar Clock Indicator";
    private static final String EXCEPTION_INDICATOR_UPDATE  = "Unable to update Toolbar Clock Indicator";

    private static final int TIMER_PERIOD = 1000;

    private final TaskPlugin taskPlugin;
    private final Date dateStart;
    private final Time timeStart;
    private final Timer timerClock;
    private final ElapsedTimeToolbarClock clockElapsed;
    private SwingWorker workerClock;


    /***********************************************************************************************
     * Construct an ElapsedTimeClock from a ToolbarIndicator width 70 pixels, height 20 pixels.
     *
     * @param taskplugin
     * @param startdate
     * @param starttime
     * @param tooltip
     *
     * @throws IndicatorException
     */

    public ElapsedTimeToolbarClock(final TaskPlugin taskplugin,
                                   final Date startdate,
                                   final Time starttime,
                                   final String tooltip) throws IndicatorException
        {
        super(ZERO_TIME, tooltip);

        if (taskplugin != null)
            {
            taskPlugin = taskplugin;
            }
        else
            {
            throw new IndicatorException(EXCEPTION_INDICATOR_CREATE);
            }

        if (startdate != null)
            {
            dateStart = startdate;
            }
        else
            {
            throw new IndicatorException(EXCEPTION_INDICATOR_CREATE);
            }

        if (starttime != null)
            {
            timeStart = starttime;
            }
        else
            {
            throw new IndicatorException(EXCEPTION_INDICATOR_CREATE);
            }

        timerClock = createToolbarTimer();

        if (timerClock == null)
            {
            throw new IndicatorException(EXCEPTION_INDICATOR_CREATE);
            }

        // Save a reference for use in inner classes
        clockElapsed = this;
        }


    /***********************************************************************************************
     * Create the Timer which drives the Toolbar clock.
     *
     * @return Timer
     */

    private Timer createToolbarTimer()
        {
        return(new Timer(TIMER_PERIOD, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
//                //LOGGER.debugTimerTick("ElapsedTimeToolbarClock: Timer");
//
//                // Stop any existing SwingWorker
//                if (workerClock != null)
//                    {
//                    workerClock.controlledStop(true, SWING_WORKER_STOP_DELAY);
//                    workerClock.destroy();
//                    }
//
//                // Fire off another thread to do the display updates
//                workerClock = new SwingWorker(REGISTRY.getThreadGroup(),
//                                              "ElapsedTimeToolbarClock SwingWorker")
//                    {
//                    public Object construct()
//                        {
////                        LOGGER.debugTimerTick("ElapsedTimeToolbarClock: SwingWorker");
//                        // There is no result to pass to the Event Dispatching Thread
//                        return (null);
//                        }
//
//                    // Display updates occur on the Event Dispatching Thread
//                    public void finished()
//                        {
//                        // The Event Dispatching Thread
//                        if ((SwingUtilities.isEventDispatchThread())
//                            && (!isStopping()))
//                            {
                            try
                                {
                                final TimeZone timeZone;
                                final Locale locale;

                                timeZone = REGISTRY.getFrameworkTimeZone();
                                locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                                    REGISTRY.getFramework().getCountryISOCode());

                                Chronos.showElapsedTime(clockElapsed,
                                                        timeZone,
                                                        locale,
                                                        getStartDate(),
                                                        getStartTime(),
                                                        Chronos.getSystemDateNow(),
                                                        Chronos.getSystemTimeNow(),
                                                        "",
                                                        getTooltip(),
                                                        getTooltip());
                                }

                            catch (IndicatorException exception)
                                {
                                getTaskPlugin().handleException(exception,
                                                              EXCEPTION_INDICATOR_UPDATE,
                                                              EventStatus.WARNING);
                                }
//                            }
//                        }
//                    };
//
//                workerClock.start();
                }
            }));
        }


    /***********************************************************************************************
     * Start the Toolbar Clock Timer immediately.
     */

    public final void startClock()
        {
        if (timerClock != null)
            {
            timerClock.setCoalesce(false);
            timerClock.restart();
            }
        }


    /***********************************************************************************************
     * Stop the Toolbar Clock Timer immediately.
     */

    public final void stopClock()
        {
        if (timerClock != null)
            {
            if (timerClock.isRunning())
                {
                timerClock.stop();
                }
            }
        }


    /**********************************************************************************************
     * Get the host Task for this ElapsedTimeToolbarClock.
     * This is the Task which has this clock as a ContextAction JComponent.
     *
     * @return TaskData
     */

    private TaskPlugin getTaskPlugin()
        {
        return (this.taskPlugin);
        }


    /***********************************************************************************************
     * Get the starting Time for the ElapsedTime.
     *
     * @return Time
     */

    private Date getStartDate()
        {
        return (this.dateStart);
        }


    /***********************************************************************************************
     * Get the starting Time for the ElapsedTime.
     *
     * @return Time
     */

    private Time getStartTime()
        {
        return (this.timeStart);
        }


    /***********************************************************************************************
     * Dispose of the ElapsedTimeToolbarClock.
     */

    public final void dispose()
        {
        stopClock();
        removeAll();
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
