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

package org.lmn.fc.ui.status.impl;

import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.ui.status.StatusIndicatorInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***************************************************************************************************
 * A Memory Usage display, updated once per second.
 */

public final class StatusMemory extends JPanel
                                implements StatusIndicatorInterface
    {
    private static final int DISPLAY_WIDTH = 100;
    private static final int DISPLAY_HEIGHT = STATUS_HEIGHT - 10;
    private static final int TIMER_PERIOD = 10000;
    private static final int THROTTLE_GC_MILLIS = 250000;

    private final String strTooltip;
    private final JProgressBar progressBar;
    private Timer timerMemory;
    private final ColourInterface colourOriginal;
    private long longTimeOfLastGC;


    /***********************************************************************************************
     * Run the GarbageCollector.
     */

    private static void runGarbageCollector()
        {
        final SwingWorker workerGC;

        workerGC = new SwingWorker(REGISTRY.getThreadGroup(),
                                   "StatusMemory.runGarbageCollector()")
            {
            /***************************************************************************************
             * Run the Garbage Collector.
             *
             * @return Object
             */

            public Object construct()
                {
                System.gc();

                return (null);
                }


            /***************************************************************************************
             * Tell the World.
             */

            public void finished()
                {
                final Runtime runTime;

                runTime = Runtime.getRuntime();

                LOGGER.logTimedEvent("[target=gc] [action=run] [cause=low memory warning] "
                                     + "[max=" + runTime.maxMemory() + "] "
                                     + "[total=" + runTime.totalMemory() + "] "
                                     + "[free=" + runTime.freeMemory() + "] "
                                     + "[cpus=" + runTime.availableProcessors() + "]");
                }
            };

        workerGC.start();
        }


    /***********************************************************************************************
     * Construct a StatusMemory.
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusMemory(final ColourInterface colour,
                        final FontInterface font,
                        final String text,
                        final String tooltip)
        {
        super();

        this.colourOriginal = colour;
        this.strTooltip = tooltip;
        this.longTimeOfLastGC = System.currentTimeMillis();

        setLayout(new BorderLayout());
        setAlignmentY(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        setMinimumSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
        setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
        setMaximumSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));

        // Create a simple 100% progress bar
        progressBar = new JProgressBar(0, 100);
        getProgressBar().setStringPainted(true);
        getProgressBar().setString(text);
        getProgressBar().setValue(0);
        getProgressBar().setForeground(colourOriginal.getColor());
        getProgressBar().setBackground(Color.green);
        getProgressBar().setFont(font.getFont());
        getProgressBar().setToolTipText(strTooltip);

        add(getProgressBar(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Initialise the UI of the StatusMemory.
     */

    public void initialiseUI()
        {
        final Runtime runTime;

        runTime = Runtime.getRuntime();
        setTimeOfLastGCMillis(System.currentTimeMillis());

        // Create the Memory Timer
        timerMemory = new Timer(TIMER_PERIOD, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final long longUsed;
                final int intUsedPercent;

                // We need to set the value to MemoryTotal-MemoryFree, as a percentage of MemoryTotal
                longUsed = runTime.totalMemory() - runTime.freeMemory();
                intUsedPercent = (int)((longUsed * 100) / runTime.totalMemory());

                // Give a gentle warning that running the GC might be a good idea...
                if (intUsedPercent > 85)
                    {
                    getProgressBar().setForeground(Color.red);
                    }
                else if (intUsedPercent > 75)
                    {
                    getProgressBar().setForeground(new Color(255, 255, 0));
                    }
                else
                    {
                    getProgressBar().setForeground(colourOriginal.getColor());
                    }

                // Try running the gc anyway if over 90%, which is indicative of real problems brewing...
                // but don't do it too frequently...
                if ((intUsedPercent > 90)
                    && ((System.currentTimeMillis() - getTimeOfLastGCMillis()) > THROTTLE_GC_MILLIS))
                    {
                    runGarbageCollector();
                    setTimeOfLastGCMillis(System.currentTimeMillis());
                    }

                getProgressBar().setString(Integer.toString(intUsedPercent) + "%");
                getProgressBar().setValue(intUsedPercent);
                getProgressBar().setToolTipText(strTooltip
                                                    + SPACE
                                                    + (longUsed/1000000)
                                                    + "Mb of "
                                                    + (runTime.totalMemory()/1000000)
                                                    + "Mb");
                }
            });
        }


    /***********************************************************************************************
     * Run the UI of the StatusMemory.
     */

    public void runUI()
        {
        if (timerMemory != null)
            {
            timerMemory.setCoalesce(false);
            timerMemory.restart();
            }
        }


    /***********************************************************************************************
     * Stop the UI of the StatusMemory.
     */

    public void stopUI()
        {
        if (timerMemory != null)
            {
            // Stop the timer
            timerMemory.stop();
            timerMemory = null;
            }
        }


    /***********************************************************************************************
     * Dispose of all UI components and stop any Timers.
     */

    public void disposeUI()
        {
        if (timerMemory != null)
            {
            // Stop the timer
            timerMemory.stop();
            timerMemory = null;
            }

        removeAll();
        }


    /***********************************************************************************************
     * Get the Time of the last GC in millisexonds.
     *
     * @return long
     */

    private long getTimeOfLastGCMillis()
        {
        return (this.longTimeOfLastGC);
        }


    /***********************************************************************************************
     * Set the Time of the last GC in millisexonds.
     *
     * @param timemillis
     */

    private void setTimeOfLastGCMillis(final long timemillis)
        {
        this.longTimeOfLastGC = timemillis;
        }


    /***********************************************************************************************
     * Set the text on the ProgressBar.
     *
     * @param text
     */

    public final void setText(final String text)
        {
        if (text != null)
            {
            getProgressBar().setString(text);
            }
        else
            {
            getProgressBar().setString(EMPTY_STRING);
            }
        }


    /***********************************************************************************************
     * Get the text on the ProgressBar.
     *
     * @return String
     */

    public final String getText()
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Set the text colour on the ProgressBar.
     *
     * @param colour
     */

    public final void setTextColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            getProgressBar().setForeground(colour.getColor());
            }
        else
            {
            getProgressBar().setForeground(Color.BLACK);
            }
        }


    /***********************************************************************************************
     * Set the text font on the ProgressBar.
     *
     * @param font
     */

    public final void setTextFont(final FontInterface font)
        {
        if (font != null)
            {
            getProgressBar().setFont(font.getFont());
            }
        }


    /***********************************************************************************************
     * Get the tooltip text on the ProgressBar.
     *
     * @return String
     */

    public final String getTooltip()
        {
        return(getProgressBar().getToolTipText());
        }


    /***********************************************************************************************
     * Set the tooltip text on the ProgressBar.
     *
     * @param tooltip
     */

    public final void setTooltip(final String tooltip)
        {
        if (tooltip != null)
            {
            getProgressBar().setToolTipText(tooltip);
            }
        }


    /***********************************************************************************************
     * Set an Icon (not used).
     *
     * @param icon
     */

    public void setIcon(final Icon icon)
        {
        }


    /***********************************************************************************************
     * Get the LocaleAutoUpdate status as a String, intended for use in tooltips.
     * Not applicable to StatusMemory.
     *
     * @param framework
     *
     * @return String
     */

    public String getUpdateStatus(final FrameworkPlugin framework)
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Get the ProgressBar.
     *
     * @return JProgressBar
     */

    private JProgressBar getProgressBar()
        {
        return (this.progressBar);
        }
    }
