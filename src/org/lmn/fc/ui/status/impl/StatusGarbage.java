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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;


/***************************************************************************************************
 * StatusIndicator for the Garbage Collector.
 */

public final class StatusGarbage extends StatusIndicator
                                 implements FrameworkConstants
    {
    private static final int GARBAGE_WIDTH = 20;


    /***********************************************************************************************
     * Construct a StatusGarbage.
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusGarbage(final ColourInterface colour,
                         final FontInterface font,
                         final String text,
                         final String tooltip)
        {
        super(colour, font, text, tooltip);

        final URL imageURL;

        setMinimumSize(new Dimension(GARBAGE_WIDTH, STATUS_HEIGHT));
        setPreferredSize(new Dimension(GARBAGE_WIDTH, STATUS_HEIGHT));
        setMaximumSize(new Dimension(GARBAGE_WIDTH << 1, STATUS_HEIGHT));
        setHorizontalAlignment(JLabel.CENTER);

        imageURL = getClass().getResource(ACTION_ICON_DELETE);
        setIcon(new ImageIcon(imageURL));

        // Handle Mouse clicks
        addMouseListener(new MouseAdapter()
            {
            /***************************************************************************************
             * Handle MousePressed events.
             *
             * @param mouseEvent
             */

            public void mousePressed(final MouseEvent mouseEvent)
                {
                final Runtime runTime;

                System.gc();
                runTime = Runtime.getRuntime();
                LOGGER.logTimedEvent("[target=gc] [action=run] "
                                        + "[max=" + runTime.maxMemory() + "] "
                                        + "[total=" + runTime.totalMemory() + "] "
                                        + "[free=" + runTime.freeMemory() + "] "
                                        + "[cpus=" + runTime.availableProcessors() + "]");
                }
            });
        }
    }
