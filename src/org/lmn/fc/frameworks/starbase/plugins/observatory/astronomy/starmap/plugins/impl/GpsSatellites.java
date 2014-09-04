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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/***************************************************************************************************
 * GpsSatellites.
 */

public final class GpsSatellites extends StarMapObject
    {
    /***********************************************************************************************
     * GpsSatellites.
     *
     * @param observatoryui
     * @param pluginhost
     * @param pluginname
     * @param objectname
     * @param clickable
     * @param colour
     */

    public GpsSatellites(final ObservatoryUIInterface observatoryui,
                         final StarMapUIComponentPlugin pluginhost,
                         final String pluginname,
                         final String objectname,
                         final boolean clickable,
                         final Color colour)
        {
        super(observatoryui,
              pluginhost,
              pluginname,
              objectname,
              colour, true, clickable,
              DrawMode.LINE);

        final ContextAction actionContext;

        // Create the toolbar button to control the plugin
        actionContext = new ContextAction("Control GPS",
                                          RegistryModelUtilities.getAtomIcon(getObservatoryUI().getHostAtom(),
                                                                             "plugin-gps.png"),
                                          "Control the GPS Satellites",
                                          KeyEvent.VK_G,
                                          false,
                                          true)
            {
            public void actionPerformed(final ActionEvent event)
                {
                setActive(!isActive());
                getHostStarMap().refreshStarMap();
                }
            };

        getButton().setAction(actionContext);
        getButton().setToolTipText((String)actionContext.getValue(Action.SHORT_DESCRIPTION));
        }


    /***********************************************************************************************
     * Draw the last point on the GPS satellite track.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param point
     */

    public final void drawObject(final Graphics graphics,
                                 final StarMapViewportInterface viewport,
                                 final Insets insets,
                                 final StarMapPointInterface point)
        {
        if ((graphics != null)
            && (viewport != null)
            && (insets != null)
            && (point != null))
            {
            final FontMetrics labelMetrics;
            final int intTextOffset;
            final int intX;
            final int intY;
            final int intOffsetX;

            if (viewport.isScaleEnabled())
                {
                intOffsetX = viewport.getElevationScaleWidth() + insets.left;
                }
            else
                {
                intOffsetX = insets.left;
                }

            intX = point.getPixelsXY().x + intOffsetX - 1;
            intY = point.getPixelsXY().y + insets.top - 1;

            graphics.setColor(point.getColour());
            graphics.fillOval(intX-3,
                              intY-3,
                              6,
                              6);
            }
        }


    /***********************************************************************************************
     * Label the last point on the GPS satellite track.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param point
     */

    public final void labelObject(final Graphics graphics,
                                  final StarMapViewportInterface viewport,
                                  final Insets insets,
                                  final StarMapPointInterface point)
        {
        if ((graphics != null)
            && (viewport != null)
            && (insets != null)
            && (point != null))
            {
            final FontMetrics labelMetrics;
            final int intTextOffset;
            final int intX;
            final int intY;
            final int intOffsetX;

            if (viewport.isScaleEnabled())
                {
                intOffsetX = viewport.getElevationScaleWidth() + insets.left;
                }
            else
                {
                intOffsetX = insets.left;
                }

            intX = point.getPixelsXY().x + intOffsetX;
            intY = point.getPixelsXY().y + insets.top;

            // The Satellite ID comes over in the StarMapPoint ID
            labelMetrics = graphics.getFontMetrics();
            //intTextOffset = intY + labelMetrics.getAscent();
            intTextOffset = intY - (CURSOR_HALFWIDTH >> 1);

            // The baseline of the leftmost character is at position (x, y)
            // in this graphics context's coordinate system
            graphics.drawString(DecimalFormatPattern.GPS_PRN.format((double)point.getPointID()),
                                intX+3,
                                intTextOffset);

            graphics.drawLine(intX - CURSOR_HALFWIDTH,
                              intY,
                              intX + CURSOR_HALFWIDTH,
                              intY);
            graphics.drawLine(intX,
                              intY - CURSOR_HALFHEIGHT,
                              intX,
                              intY + CURSOR_HALFHEIGHT);
            }
        }


    /***********************************************************************************************
     * Make sure that the coordinates are up to date for the specified epoch.
     * (e.g the position of a moving object).
     *
     * @param calendar
     */

    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        // Not required for GPS Satellites
        }


    /***********************************************************************************************
     * Transform the (Ra, Dec) or (Az, El) to (X, Y) for display at a specified time & place.
     * The Latitude and LAST are ignored for (Az, El) transformations.
     *
     * @param viewport
     * @param calendar
     * @param latitude
     * @param originisnorth
     * @param clickcoordinates
     */

    public void transformToViewportXY(final StarMapViewportInterface viewport,
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
