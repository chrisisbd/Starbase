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
// Sky StarMapPlugin
//------------------------------------------------------------------------------
// Revision History
//
//  06-10-02    LMN created file
//  07-10-02    LMN rationalised constructor parameters
//  13-09-06    LMN converting for Starbase!
//
//------------------------------------------------------------------------------
// Astronomy StarMap package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Vector;


/***************************************************************************************************
 * Sky.
 */

public final class Sky extends StarMapObject
    {
    /***********************************************************************************************
     * Construct a Sky.
     *
     * @param observatoryui
     * @param hostmap
     * @param pluginname
     * @param objectname
     * @param colour
     */

    public Sky(final ObservatoryUIInterface observatoryui,
               final StarMapUIComponentPlugin hostmap,
               final String pluginname,
               final String objectname,
               final Color colour)
        {
        super(observatoryui,
              hostmap,
              pluginname,
              objectname,
              colour,
              false,
              false,
              DrawMode.POINT);

        final ContextAction actionContext;

        // Create the toolbar button to control the plugin
        actionContext = new ContextAction("Control Sky",
                                          RegistryModelUtilities.getAtomIcon(getObservatoryUI().getHostAtom(),
                                                                             "plugin-sky.png"),
                                          "Control the Sky",
                                          KeyEvent.VK_K,
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

        setStarMapPoints(createSky());
        }


    /***********************************************************************************************
     * Create the Sky.
     */

    private Vector<Vector<StarMapPointInterface>> createSky()
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;
        final Vector<StarMapPointInterface> vecSkyPoints;
        final StarMapPointInterface pointSky;

        vecCollection = new Vector<Vector<StarMapPointInterface>>(1);
        vecSkyPoints = new Vector<StarMapPointInterface>(1);

        // Create a dummy invisible Sky (use only the colour)
        // The point will be forced as visible, so the coordinates used don't matter
        pointSky = new StarMapPoint(this,
                                    0,
                                    "Sky",
                                    CoordinateType.UNASSIGNED,
                                    new Point2D.Double(0.0, 0.0),
                                    getColour(),
                                    isClickable(),
                                    null);
        vecSkyPoints.add(pointSky);
        vecCollection.add(vecSkyPoints);

        return (vecCollection);
        }


    /***********************************************************************************************
     * Draw the Sky.
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
        // Fill in the map window in the sky colour
        // The original background colour cannot be seen if gradient paint is used
        if ((graphics != null)
            && (UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_TOP != null)
            && (UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_BOTTOM != null))
            {
            final GradientPaint gradientPaint;

            gradientPaint = new GradientPaint(((viewport.getBottomRight().x-viewport.getTopLeft().x+1)) >> 1,
                                              0,
                                              UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_TOP.getColor(),
                                              ((viewport.getBottomRight().x-viewport.getTopLeft().x+1)) >> 1,
                                              ((viewport.getBottomRight().y-viewport.getTopLeft().y+1)),
                                              UIComponentPlugin.DEFAULT_COLOUR_GRADIENT_BOTTOM.getColor());

            ((Graphics2D)graphics).setPaint(gradientPaint);
            graphics.fillRect(viewport.getTopLeft().x,
                              viewport.getTopLeft().y,
                              (viewport.getBottomRight().x-viewport.getTopLeft().x+1),
                              (viewport.getBottomRight().y-viewport.getTopLeft().y+1));
            }
        }


    /***********************************************************************************************
     * Label the Sky - not required.
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
        // Not required for the Sky
        }


    /***********************************************************************************************
     * Refresh the coordinates - not required.
     *
     * @param calendar
     */

    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        // Not required for the Sky
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
        // Check that we have some data loaded!
        if ((getStarMapPoints() != null)
            && (getStarMapPoints().size() == 1))
            {
            final Vector<StarMapPointInterface> vecPoints;

            vecPoints = getStarMapPoints().get(0);

            if ((vecPoints != null)
                && (vecPoints.size() == 1))
                {
                // The dummy Sky must be visible, so that drawObject() is called,
                // for the gradient paint
                vecPoints.get(0).setVisible(true);
                }
            }
        }
    }
