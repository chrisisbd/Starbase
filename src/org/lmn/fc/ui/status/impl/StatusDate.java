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
//  16-11-04    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.status.impl;

//--------------------------------------------------------------------------------------------------

import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***************************************************************************************************
 * A clock indicating Date for the StatusIndicatorKey, updated once every 5 minutes.
 */

public final class StatusDate extends StatusIndicator
    {
    private static final int DATE_WIDTH = 70;
    private static final int TIMER_PERIOD = 300000;

    private final FrameworkPlugin pluginFramework;
    private Timer timerClock;
    private SwingWorker workerClock;


    /***********************************************************************************************
     *
     * @param framework
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusDate(final FrameworkPlugin framework,
                      final ColourInterface colour,
                      final FontInterface font,
                      final String text,
                      final String tooltip)
        {
        super(colour, font, text, tooltip);

        setMinimumSize(new Dimension(DATE_WIDTH, STATUS_HEIGHT));
        setPreferredSize(new Dimension(DATE_WIDTH, STATUS_HEIGHT));
        setMaximumSize(new Dimension(DATE_WIDTH, STATUS_HEIGHT));
        setHorizontalAlignment(JLabel.CENTER);

        this.pluginFramework = framework;

        // Initialise the StatusDate display text
        setText(SPACE + SPACE + ChronosHelper.toDateString(Chronos.getCalendarDateNow()) + SPACE + SPACE);
        setTooltip(getTooltip() + getUpdateStatus(pluginFramework));

        // Create and start the Clock TImer
        createClockTimer();

        if (timerClock != null)
            {
            timerClock.setCoalesce(false);
            timerClock.restart();
            }
        }


    /***********************************************************************************************
     * Create the Timer which drives the clock.
     */

    private void createClockTimer()
        {
        timerClock = new Timer(TIMER_PERIOD, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
//                //RegistryModel.debugTimerTick"TICK! StatusDate: Timer");
//
//                // Stop any existing SwingWorker
//                if (workerClock != null)
//                    {
//                    workerClock.controlledStop(true, SWING_WORKER_STOP_DELAY);
//                    workerClock.destroy();
//                    }
//
//                // Prepare another thread to do the Clock update
//                workerClock = new SwingWorker(REGISTRY.getThreadGroup(),
//                                              "StatusDate SwingWorker")
//                    {
//                    public Object construct()
//                        {
////                        LOGGER.debugTimerTick("StatusDate: SwingWorker");
//                        // IS this the best way to wait for 1 sec?!
//                        // There is no result to pass to the Event Dispatching Thread
//                        return (null);
//                        }
//
//                    // Display updates occur on the Event Dispatching Thread
//                    public void finished()
//                        {
//                        if ((SwingUtilities.isEventDispatchThread())
//                            && (!isStopping()))
//                            {
                            // Update the StatusDate display text
                            setText(ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
//                            }
//                        }
//                    };
//
//                // When the Timer goes off, start the Thread we have prepared...
//                workerClock.start();
                }
            });
        }


    /***********************************************************************************************
     * Dispose of all UI components and stop any Timers.
     */

    public void dispose()
        {
        super.dispose();

        if (timerClock != null)
            {
            // Stop the clock
            timerClock.stop();
            timerClock = null;
            }

        SwingWorker.disposeWorker(workerClock, true, SWING_WORKER_STOP_DELAY);
        workerClock = null;
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
