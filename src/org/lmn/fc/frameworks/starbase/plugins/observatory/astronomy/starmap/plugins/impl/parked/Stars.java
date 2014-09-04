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

//------------------------------------------------------------------------------
// Stars StarMapPlugin
//------------------------------------------------------------------------------
// Revision History
//
//  06-10-02    LMN created file from original code in StarMap
//  07-10-02    LMN rationalised constructor parameters
//
//------------------------------------------------------------------------------
// Astronomy StarMap package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.parked;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.StarMapObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;

import java.awt.*;

//------------------------------------------------------------------------------

public final class Stars extends StarMapObject
    {
    // Colour values used for Star display
    private static final Color [] MAGNITUDE =
        {
        new Color(255, 255, 255),
        new Color(252, 252, 252),
        new Color(232, 232, 232),
        new Color(200, 200, 200),
        new Color(136, 136, 136),
        new Color(72, 72, 72)
        };


    /***********************************************************************************************
     * Construct Stars!
     *
     * @param observatoryui
     * @param componentmodel
     * @param pluginhost
     * @param pluginname
     * @param objectname
     * @param clickable
     */

    public Stars(final ObservatoryUIInterface observatoryui,
                 final FrameworkPlugin componentmodel,
                 final StarMapUIComponentPlugin pluginhost,
                 final String pluginname,
                 final String objectname,
                 final boolean clickable)
        {
        super(observatoryui,
              pluginhost,
              pluginname,
              objectname,
              Color.white,
              false,
              clickable,
              DrawMode.POINT);
        }


    //--------------------------------------------------------------------------
    // Draw the specified StarMapPoint object on the specified Graphics context
    // The caller must take care of repaint()

    public final void drawObject(final Graphics graphics,
                                 final StarMapViewportInterface viewport,
                                 final Insets insets,
                                 final StarMapPointInterface starmapPointInterface)
        {
        //LOGGER.debugTimedEvent("Drawing Star at (" + starmapPoint.getPixelsXY().x + ", " + starmapPoint.getPixelsXY().y + ")");

        graphics.setColor(starmapPointInterface.getColour());
        graphics.fillRect(starmapPointInterface.getPixelsXY().x,
                          starmapPointInterface.getPixelsXY().y,
                          1,
                          1 );
        }


    //--------------------------------------------------------------------------
    // Label each individual StarMapPoint object
    // The caller must take care of repaint()

    public final void labelObject(final Graphics graphics,
                                  final StarMapViewportInterface viewportInterface,
                                  final Insets insets,
                                  final StarMapPointInterface pointInterface)
        {
        }


    //--------------------------------------------------------------------------
    // Make sure that the coordinates are up to date for the specified epoch
    // (e.g the position of a moving object)

    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        }


    /***********************************************************************************************
     * Transform the (Ra, Dec) to (X, Y) for display at a specified time & place.
     * The Latitude and LAST are ignored for (Az, El) transformations.
     *
     * @param viewport
     * @param calendar
     * @param latitude
     * @param originisnorth
     * @param clickcoordinates
     */

    public final void transformToViewportXY(final StarMapViewportInterface viewport,
                                            final AstronomicalCalendarInterface calendar,
                                            final double latitude,
                                            final boolean originisnorth,
                                            final StarMapPointInterface[][] clickcoordinates)
        {
        CoordinateConversions.transformToViewportXY(getStarMapPoints(),
                                                    viewport,
                                                    calendar,
                                                    latitude,
                                                    originisnorth,
                                                    clickcoordinates);
        }
    }
