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

import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.frameworks.starbase.events.FrameworkChangedEvent;
import org.lmn.fc.frameworks.starbase.events.FrameworkChangedListener;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.plugins.FrameworkPlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * StatusLongitude.
 */

public final class StatusLongitude extends StatusIndicator
                                   implements FrameworkChangedListener
    {
    // Allow for a few decimal places in seconds of arc
    private static final int LONGITUDE_WIDTH = 90;

    // Injections
    private final FrameworkPlugin pluginFramework;


    /***********************************************************************************************
     * Construct a StatusLongitude.
     *
     * @param framework
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusLongitude(final FrameworkPlugin framework,
                           final ColourInterface colour,
                           final FontInterface font,
                           final String text,
                           final String tooltip)
        {
        super(colour, font, text, tooltip);

        this.pluginFramework = framework;

        setMinimumSize(new Dimension(LONGITUDE_WIDTH, STATUS_HEIGHT));
        setPreferredSize(new Dimension(LONGITUDE_WIDTH, STATUS_HEIGHT));
        setMaximumSize(new Dimension(LONGITUDE_WIDTH << 1, STATUS_HEIGHT));
        setHorizontalAlignment(JLabel.CENTER);

        if (getFramework() != null)
            {
            // Register ourselves as a Framework Listener
            getFramework().addFrameworkChangedListener(this);

            // Initialise the Longitude display if possible
            if (getFramework().getLongitude() != null)
                {
                try
                    {
                    final DegMinSecInterface dmsNewLongitude;

                    // Be careful to clone the DegMinSec,
                    // to ensure we don't affect the original Sign setting!
                    dmsNewLongitude = new LongitudeDataType(getFramework().getLongitude().isPositive(),
                                                            getFramework().getLongitude().getDegrees(),
                                                            getFramework().getLongitude().getMinutes(),
                                                            getFramework().getLongitude().getSeconds());

                    // Display the Longitude with a human-friendly E/W
                    dmsNewLongitude.setDisplayFormat(DegMinSecFormat.EW);
                    setText(dmsNewLongitude.toString());
                    setTooltip(getTooltip() + getUpdateStatus(getFramework()));
                    }

                catch (DegMinSecException exception)
                    {
                    setText(MSG_ERROR);
                    LOGGER.error(EXCEPTION_PARAMETER_INVALID + SPACE + exception.getMessage());
                    }
                }
            else
                {
                setText(STATUS_NO_FIX);
                setTooltip(EMPTY_STRING);
                }
            }
        else
            {
            setText(STATUS_NO_FIX);
            setTooltip(EMPTY_STRING);
            }
        }


    /**********************************************************************************************
     * Indicate that the state of the Framework has changed.
     *
     * @param event
     */

    public void frameworkChanged(final FrameworkChangedEvent event)
        {
        if ((event != null)
            && (event.getFramework() != null)
            && (event.getFramework().getLongitude() != null))
            {
            try
                {
                final DegMinSecInterface dmsNewLongitude;

                // Be careful to clone the DegMinSec,
                // to ensure we don't affect the original Sign setting!
                dmsNewLongitude = new LongitudeDataType(event.getFramework().getLongitude().isPositive(),
                                                        event.getFramework().getLongitude().getDegrees(),
                                                        event.getFramework().getLongitude().getMinutes(),
                                                        event.getFramework().getLongitude().getSeconds());

                // Display the Longitude with a human-friendly E/W
                dmsNewLongitude.setDisplayFormat(DegMinSecFormat.EW);
                setText(dmsNewLongitude.toString());
                }

            catch (DegMinSecException exception)
                {
                setText(MSG_ERROR);
                LOGGER.error(EXCEPTION_PARAMETER_INVALID + SPACE + exception.getMessage());
                }
            }
        else
            {
            setText(STATUS_NO_FIX);
            }
        }


    /***********************************************************************************************
     * Get the Framework.
     *
     * @return FrameworkPlugin
     */

    private FrameworkPlugin getFramework()
        {
        return (this.pluginFramework);
        }
    }
