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
// CelestialObjects StarMapPlugin
//------------------------------------------------------------------------------
// Revision History
//
//  08-10-02    LMN created file from original code in StarMap
//
//------------------------------------------------------------------------------

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

import java.awt.*;
import java.util.Vector;

//------------------------------------------------------------------------------

public final class CelestialObjects extends StarMapObject
    {
    /***********************************************************************************************
     *
     * @param observatoryui
     * @param hostmap
     * @param pluginname
     * @param objectname
     * @param celestialobjects
     * @param clickable
     * @param colour
     */

    public CelestialObjects(final ObservatoryUIInterface observatoryui,
                            final StarMapUIComponentPlugin hostmap,
                            final String pluginname,
                            final String objectname,
                            final Vector<Vector<StarMapPointInterface>> celestialobjects,
                            final boolean clickable,
                            final Color colour)
        {
        super(observatoryui,
              hostmap,
              pluginname,
              objectname,
              colour,
              false,
              clickable,
              DrawMode.POINT);

//        if (celestialobjects != null)
//            {
//            // Read the set of (Ra, Dec) to be loaded
//            final Enumeration enumRaDec = celestialobjects.elements();
//
//            if (enumRaDec != null)
//                {
//                final StarMapPointInterface pointRaDecInterface;
//
//                if (enumRaDec.hasMoreElements())
//                    {
//                    pointRaDecInterface = (StarMapPointInterface)enumRaDec.nextElement();
//                    if (pointRaDecInterface != null)
//                        {
//                        // There's something to load!
//                        setStarMapPoints(celestialobjects);
//                        LOGGER.debugTimedEvent("CelestialObjects loaded [count=" + getStarMapPoints().size() + "]");
//                        }
//                    else
//                        {
//                        // There's no Point
////                        throw new Exception(ExceptionLibrary.EXCEPTION_LOAD_OBJECTS + " [Point=null]");
//                        }
//                    }
//                else
//                    {
//                    // There's nothing in the Enumeration
////                    throw new Exception(ExceptionLibrary.EXCEPTION_LOAD_OBJECTS + " [Enumeration=empty]");
//                    }
//                }
//            else
//                {
//                // There's nothing in the Vector
////                throw new Exception(ExceptionLibrary.EXCEPTION_LOAD_OBJECTS + " [Vector=empty]");
//                }
//            }
//        else
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_LOAD_OBJECTS + " [Vector=null]");
//            }
        }


    //--------------------------------------------------------------------------
    // Draw the specified StarMapPoint object on the specified Graphics context
    // The caller must take care of repaint()
    // Note that the colour is as provided in the Vector, unless using setColour()

    public final void drawObject(final Graphics graphics,
                                 final StarMapViewportInterface viewport,
                                 final Insets insets,
                                 final StarMapPointInterface starmapPointInterface)
        {
        //LOGGER.debugTimedEvent("Drawing CelestialObject at (" + starmapPoint.getPixelsXY().x + ", " + starmapPoint.getPixelsXY().y + ")");

        graphics.setColor(starmapPointInterface.getColour());
        graphics.fillRect(starmapPointInterface.getPixelsXY().x,
                          starmapPointInterface.getPixelsXY().y,
                          5,
                          5 );
        }

    //--------------------------------------------------------------------------
    // Label each individual StarMapPoint object
    // The caller must take care of repaint()

    public final void labelObject(final Graphics graphics,
                                  final StarMapViewportInterface viewportInterface,
                                  final Insets insets,
                                  final StarMapPointInterface pointInterface)
        {
        // Not required
        }


    //--------------------------------------------------------------------------
    // Make sure that the coordinates are up to date for the specified epoch
    // (e.g the position of a moving object)

    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        // Not required
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
