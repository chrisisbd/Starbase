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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.impl;

import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.workshop.ui.starfield.StarData;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***********************************************************************************************
 *
 */

public class MatrixUIComponent extends UIComponent
                               implements UIComponentPlugin
    {
    private static final boolean REFRESH_START = true;
    private static final boolean REFRESH_STOP = false;

    // Refresh Timers
    private Timer timerRefresh;
    private SwingWorker workerRefresh;

    private JComponent componentHost;

    // Resources
    private int intRefreshPeriod;
    private int i;


    //------------------------------
    private StarData[] arrayStars;
    private int intStarCount;
    private int intWidth;
    private int intHeight;
    private int speed;
    private int type;
    private double rot;
    private double dx;
    private double ddx;
    private double defddx;
    private double max;
    //------------------------------


    public MatrixUIComponent(final JComponent component)
        {
        super();

        componentHost = component;
        i = 0;
        }


    /***********************************************************************************************
     *
     */

    public final void initialiseUI()
        {
        intRefreshPeriod = 20;
        //-------------------------------
        speed = intRefreshPeriod;
        intStarCount = 250;
        rot = 0;
        dx = 0;
        ddx = 0;
        type = 0;
        max = 0.3;
        defddx = 0.005;

        intWidth = 0;
        intHeight = 0;
        //---------------------------------

        timerRefresh = new Timer(getRefreshPeriod(),
                                 new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                LOGGER.debugTimerEvent("TICK! MatrixUI");
                refreshMatrix();
                }
            });

        setBackground(Color.black);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        setRefreshTimer(REFRESH_START);
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        setRefreshTimer(REFRESH_STOP);
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public final void disposeUI()
        {
        if (timerRefresh != null)
            {
            // Make sure that the Timer has stopped running
            if (timerRefresh.isRunning())
                {
                timerRefresh.stop();
                LOGGER.debugTimerEvent("STOP MATRIX REFRESH TIMER");
                }

            timerRefresh = null;
            }

        SwingWorker.disposeWorker(workerRefresh, true, SWING_WORKER_STOP_DELAY);
        workerRefresh = null;

        // Finally, remove all UIComponents...
        removeAll();
        }


    /***********************************************************************************************
     * Refresh the Matrix on a separate thread.
     */

    private final void refreshMatrix()
        {
        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerRefresh, true, SWING_WORKER_STOP_DELAY);
        workerRefresh = null;

        // Fire off another thread
        workerRefresh = new SwingWorker(REGISTRY.getThreadGroup(),
                                        "SwingWorker Matrix")
            {
            public Object construct()
                {
//                LOGGER.debugTimerTick("MatrixUI: SwingWorker");
                // Are we running under a Timer?
                if (timerRefresh != null)
                    {
                    // Update the Timer delay in case the RegistryModel has changed...
                    timerRefresh.setDelay(getRefreshPeriod());
                    }

                return (null);
                }

            // Display updates occur on the Event Dispatching Thread
            public void finished()
                {
                // Update on the Event Dispatching Thread
                LOGGER.debugTimerEvent("MatrixSwingWorker finished");

                if ((SwingUtilities.isEventDispatchThread())
                    && (!isStopping()))
                    {
                    int intNewWidth;
                    int intNewHeight;

                    rot += dx;
                    dx += ddx;

                    if (dx >= max)
                        {
                        ddx = -defddx;
                        }

                    if (dx < -max)
                        {
                        ddx = defddx;
                        }

                    intNewWidth = getWidth();
                    intNewHeight = getHeight();

                    if ((intWidth != intNewWidth)
                        || (intHeight != intNewHeight))
                        {
                        intWidth = intNewWidth;
                        intHeight = intNewHeight;

                        arrayStars = new StarData[intStarCount];

                        for (int i = 0; i < intStarCount; i++)
                            {
                            arrayStars[i] = new StarData(intWidth, intHeight, 100, type);
                            }
                        }

                    repaint();
                    }

//                if ((workerRefresh != null)
//                    && (workerRefresh.get() != null)
//                    && (SwingUtilities.isEventDispatchThread()))
//                    {
//                    LOGGER.debug("display refreshed");
//
//
//
//                    }
                }
            };

        // Start the Thread we have prepared...
        workerRefresh.start();
        }


    /***********************************************************************************************
     * Control the Refresh Timer with <code>REFRESH_START</code> or <code>REFRESH_STOP</code>.
     * This has no effect if the current refresh mode is Clickable.
     *
     * @param state
     */

    public final void setRefreshTimer(final boolean state)
        {
        // Control the Timer
        if (timerRefresh != null)
            {
            if (state == REFRESH_START)
                {
                timerRefresh.setCoalesce(false);
                timerRefresh.restart();
                LOGGER.debugTimerEvent("START MATRIX REFRESH TIMER");
                }
            else
                {
                if (timerRefresh.isRunning())
                    {
                    timerRefresh.stop();
                    LOGGER.debugTimerEvent("STOP MATRIX REFRESH TIMER");
                    }
                }
            }
//        else
//            {
//            LOGGER.debugTimerEvent("MATRIX null Timer");
//            }
        }


    /***********************************************************************************************
     *
     * @param graphics
     */

    public void paint(final Graphics graphics)
        {
        super.paint(graphics);

        graphics.setColor(Color.ORANGE);
        graphics.drawString("MATRIX!! " + i++, getWidth()/3, getHeight() >> 1);

        graphics.setClip(0, 0, intWidth, intHeight);

        // Draw the array of Stars
        for (int i = 0; i < intStarCount; i++)
            {
            if ((arrayStars != null)
                && (arrayStars[i] != null))
                {
                arrayStars[i].drawStar(graphics, rot);
                }
            }
        }


    /***********************************************************************************************
     * Get the Refresh Period in milliseconds.
     *
     * @return int
     */

    public final int getRefreshPeriod()
        {
        return (this.intRefreshPeriod);
        }
    }
