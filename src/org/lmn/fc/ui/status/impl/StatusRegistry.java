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

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***************************************************************************************************
 * Indicate the number of beans currently in the Registry, updated once every 100 seconds.
 */

public final class StatusRegistry extends StatusIndicator
                                  implements FrameworkStrings
    {

    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    private static final RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    private static final int REGISTRY_WIDTH = 110;
    private static final int TIMER_PERIOD = 100000;

    private Timer timerRegistry;
    private SwingWorker workerRegistry;


    /***********************************************************************************************
     * Construct a StatusRegistry.
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusRegistry(final ColourInterface colour,
                          final FontInterface font,
                          final String text,
                          final String tooltip)
        {
        super(colour, font, text, tooltip);

        setMinimumSize(new Dimension(REGISTRY_WIDTH, STATUS_HEIGHT));
        setPreferredSize(new Dimension(REGISTRY_WIDTH, STATUS_HEIGHT));
        setMaximumSize(new Dimension(REGISTRY_WIDTH, STATUS_HEIGHT));
        setHorizontalAlignment(JLabel.CENTER);
        }


    public void initialiseUI()
        {
        // Initialise the StatusRegistry display text
        setText(buildText());

        // Create the Registry TImer
        createRegistryTimer();
        }


    /***********************************************************************************************
     * Dispose of all UI components and stop any Timers.
     */

    public void disposeUI()
        {
        super.dispose();

        if (timerRegistry != null)
            {
            // Stop the clock
            timerRegistry.stop();
            timerRegistry = null;
            }

        SwingWorker.disposeWorker(workerRegistry, true, SWING_WORKER_STOP_DELAY);
        workerRegistry = null;
        }


    public void runUI()
        {
        if (timerRegistry != null)
            {
            timerRegistry.setCoalesce(false);
            timerRegistry.restart();
            }
        }

    public void stopUI()
        {
        if (timerRegistry != null)
            {
            timerRegistry.stop();
            }
        }

    /***********************************************************************************************
     * Create the Timer which drives the clock.
     */

    private void createRegistryTimer()
        {
        timerRegistry = new Timer(TIMER_PERIOD, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
//                LOGGER.debugTimerTick("StatusRegistry: Timer");
//
//                // Stop any existing SwingWorker
//                if (workerRegistry != null)
//                    {
//                    workerRegistry.controlledStop(true, SWING_WORKER_STOP_DELAY);
//                    workerRegistry.destroy();
//                    }
//
//                // Prepare another thread to do the Registry update
//                workerRegistry = new SwingWorker(REGISTRY.getThreadGroup(),
//                                                 "StatusRegistry SwingWorker")
//                    {
//                    public Object construct()
//                        {
////                        LOGGER.debugTimerTick("StatusRegistry: SwingWorker");
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
                            // Update the StatusRegistry display text
                            setText(buildText());
//                            }
//                        }
//                    };
//
//                // When the Timer goes off, start the Thread we have prepared...
//                workerRegistry.start();
                }
            });
        }


    /***********************************************************************************************
     * Build the message to show in the StatusRegistry indicator.
     * <code>(1234, 789) Beans</code>
     *
     * @return String
     */

    private String buildText()
        {
        return (LEFT_PARENTHESIS
                + REGISTRY.size()
                + COMMA + SPACE
                + REGISTRY_MODEL.size()
                + RIGHT_PARENTHESIS
                + SPACE
                + MSG_BEANS);
        }
    }
