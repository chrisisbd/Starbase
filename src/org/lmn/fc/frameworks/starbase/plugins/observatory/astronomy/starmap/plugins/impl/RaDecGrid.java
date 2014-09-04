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
// StarMap RaDec Projection Grid
//------------------------------------------------------------------------------
// Revision History
//
//  28-09-02    LMN created file
//  01-10-02    LMN made a Grid display using the Plugin!
//  07-10-02    LMN rationalised constructor parameters
//  12-09-06    LMN extracted superclass
//
//------------------------------------------------------------------------------
// To Do
//
//  Highlight the equator and Ra = 0
//
//------------------------------------------------------------------------------
// Astronomy Starmap package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Vector;


/***************************************************************************************************
 * RaDecGrid.
 */

public final class RaDecGrid extends StarMapObject
    {
    // String Resources
    private static final String ICON_PLUGIN = "plugin-grid.png";


    /***********************************************************************************************
     * Construct a RaDecGrid.
     *
     * @param observatoryui
     * @param hostmap
     * @param pluginname
     * @param objectname
     * @param clickable
     * @param colour
     */

    public RaDecGrid(final ObservatoryUIInterface observatoryui,
                     final StarMapUIComponentPlugin hostmap,
                     final String pluginname,
                     final String objectname,
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

        final ContextAction actionContext;

        // Create the toolbar button to control the plugin
        actionContext = new ContextAction("Control RaDec",
                                          RegistryModelUtilities.getAtomIcon(getObservatoryUI().getHostAtom(),
                                                                             ICON_PLUGIN),
                                          "Control the RaDec Projection Grid",
                                          KeyEvent.VK_R,
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

        setStarMapPoints(createRaDecGrid());
        }


    /***********************************************************************************************
     * Create the RaDec Projection Grid as a Vector of StarMapPoint.
     * The Grid is initially not visible - it is made visible when transformed to (x, y).
     *
     * @return Vector<StarMapPoint>
     */

    private Vector<Vector<StarMapPointInterface>> createRaDecGrid()
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;
        final Vector<StarMapPointInterface> vecGridPoints;
        double dblRA;
        int intDec;
        int intPointID;

        vecCollection = new Vector<Vector<StarMapPointInterface>>(1);
        vecGridPoints = new Vector<StarMapPointInterface>(1000);
        intPointID = 0;

        // Step through Right Ascension
        for (dblRA = 0.0;
             dblRA < 24.0;
             dblRA += 0.5)
            {
            // Step through Declination
            for (intDec = -90;
                 intDec <= 90;
                 intDec += 5)
                {
                final Color color;
                final boolean boolClickable;

                if ((dblRA == 0.0)
                    || (intDec == 0))
                    {
                    color = Color.yellow;
                    boolClickable = true;
                    }
                else
                    {
                    color = getColour();
                    boolClickable = isClickable();
                    }

                vecGridPoints.add(new StarMapPoint(this,
                                                   intPointID++,
                                                   "Grid",
                                                   CoordinateType.RADEC,
                                                   new Point2D.Double(dblRA, (double)intDec),
                                                   color,
                                                   boolClickable,
                                                   null));
                }
            }

        vecCollection.add(vecGridPoints);

        return (vecCollection);
        }


    /***********************************************************************************************
     * Draw the Grid point.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param point
     */

    public void drawObject(final Graphics graphics,
                           final StarMapViewportInterface viewport,
                           final Insets insets,
                           final StarMapPointInterface point)
        {
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

        // Use the StarMapPoint (x, y) from the latest transformation
        intX = point.getPixelsXY().x + intOffsetX;
        intY = point.getPixelsXY().y + insets.top;

        // Draw the Grid point
        graphics.setColor(point.getColour());
        graphics.drawRect(intX,
                          intY,
                          1,
                          1);
        }


    /***********************************************************************************************
     * Label the Grid point - not required.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param point
     */

    public void labelObject(final Graphics graphics,
                            final StarMapViewportInterface viewport,
                            final Insets insets,
                            final StarMapPointInterface point)
        {
        // Not required for the Grid
        }


    /***********************************************************************************************
     * Refresh the coordinates - not required.
     *
     * @param calendar
     */

    public void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        // Not required for the Grid
        }


    /***********************************************************************************************
     * Transform the StarMapPoints to (X, Y) for display at a specified time & place.
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
