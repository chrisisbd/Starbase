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
// Sun StarMapPlugin
//------------------------------------------------------------------------------
// Revision History
//
//  05-10-02    LMN created file from original code in StarMap
//  07-10-02    LMN rationalised constructor parameters
//  05-06-08    LMN tidying up, using interfaces etc.! Preparing for use in Observatory...
//
//------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.impl.SolarEphemerisDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.types.RightAscensionDataType;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Vector;


/***************************************************************************************************
 * Sun.
 */

public final class Sun extends StarMapObject
    {
    // String Resources
    public static final String NAME_PLUGIN_SUN = "Sun";
    public static final String ICON_PLUGIN_SUN = "plugin-sun.png";

    final StarMapPointInterface pointSun;


    /***********************************************************************************************
     * Create the Sun.
     * In this case there's only one object, so just add to the Collections, once only.
     *
     * @param point
     *
     * @return Vector<StarMapPointInterface>
     */

    private static Vector<Vector<StarMapPointInterface>> createSun(final StarMapPointInterface point)
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;
        final Vector<StarMapPointInterface> vecSunPoint;

        vecCollection = new Vector<Vector<StarMapPointInterface>>(1);
        vecSunPoint = new Vector<StarMapPointInterface>(1);
        vecSunPoint.add(point);
        vecCollection.add(vecSunPoint);

        return (vecCollection);
        }


    /***********************************************************************************************
     * Construct a Sun.
     *
     * @param observatoryui
     * @param hostmap
     * @param pluginname
     * @param objectname
     * @param clickable
     * @param labelled
     * @param colour
     */

    public Sun(final ObservatoryUIInterface observatoryui,
               final StarMapUIComponentPlugin hostmap,
               final String pluginname,
               final String objectname,
               final boolean clickable,
               final boolean labelled,
               final Color colour)
        {
        super(observatoryui,
              hostmap,
              pluginname,
              objectname,
              colour,
              labelled,
              clickable,
              DrawMode.POINT);

        this.pointSun = new StarMapPoint(this,
                                         0,
                                         NAME_PLUGIN_SUN,
                                         CoordinateType.RADEC,
                                         new Point2D.Double(0.0, 0.0),
                                         getColour(),
                                         isClickable(),
                                         null);

        setStarMapPoints(createSun(this.pointSun));

        final ContextAction actionContext;

        // Create the toolbar button to control the plugin
        actionContext = new ContextAction("Control Sun",
                                          RegistryModelUtilities.getAtomIcon(getObservatoryUI().getHostAtom(),
                                                                             ICON_PLUGIN_SUN),
                                          "Control the Sun",
                                          KeyEvent.VK_S,
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
        getButton().setToolTipText((String) actionContext.getValue(Action.SHORT_DESCRIPTION));
        }


    /***********************************************************************************************
     * Get the Sun as a StarMapPoint.
     *
     * @return StarMapPointInterface
     */

    public StarMapPointInterface getSun()
        {
        return (this.pointSun);
        }


    /***********************************************************************************************
     * Draw the Sun.
     * Try to make the Sun appear to be 0.5 degrees in diameter for the current zoom setting.
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
        //LOGGER.debugTimedEvent("Drawing Sun at (" + mapdataVector.pointXY.x + ", " + mapdataVector.pointXY.y + ")");

        if ((graphics !=null)
            && (viewport != null)
            && (insets != null)
            && (point != null))
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

            intX = point.getPixelsXY().x + intOffsetX;
            intY = point.getPixelsXY().y + insets.top;

            graphics.setColor(point.getColour());
            graphics.fillOval(intX,
                              intY,
                              (int)(0.5 * viewport.getHorizPixelsPerDegree()),
                              (int)(0.5 * viewport.getHorizPixelsPerDegree()));
            }
        }


    /***********************************************************************************************
     * Label the Sun.
     * point specifies the centre of the Sun.
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
        final String SOURCE = "Sun.labelObject() ";

        if ((graphics !=null)
            && (viewport != null)
            && (insets != null)
            && (point != null))
            {
            final FontMetrics labelMetrics;
            final int intTextOffset;
            final int intAscent;
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

            labelMetrics = graphics.getFontMetrics();
            intAscent = labelMetrics.getAscent();
            intTextOffset = intY - (intAscent * 3);

            graphics.drawString(getObjectName(),
                                intX,
                                intTextOffset);

            if ((CoordinateType.RADEC.equals(point.getCoordinateType()))
                || (CoordinateType.RADEC.equals(point.getCoordinateType())))
                {
                final HourMinSecInterface hmsRA;

                hmsRA = new RightAscensionDataType(point.getCoordinates().x);

                graphics.drawString(point.getCoordinateType().getSymbolX() + SPACE + hmsRA.toString_HH_MM_SS(),
                                    intX,
                                    intTextOffset + intAscent);
                graphics.drawString(point.getCoordinateType().getSymbolY() + SPACE + DecimalFormatPattern.SECONDS_DECLINATION.format(point.getCoordinates().y),
                                    intX,
                                    intTextOffset + intAscent + intAscent);
                }
            else
                {
                graphics.drawString(CoordinateType.UNASSIGNED.getSymbolX(),
                                    intX,
                                    intTextOffset + intAscent);
                graphics.drawString(CoordinateType.UNASSIGNED.getSymbolY(),
                                    intX,
                                    intTextOffset + intAscent + intAscent);

                LOGGER.error(SOURCE + "Invalid CoordinateType [type=" + point.getCoordinateType().getName() + "]");
                }

            // Now draw the Cursor at the point location
            graphics.drawLine(intX - CURSOR_HALFWIDTH,
                              intY,
                              intX + CURSOR_HALFWIDTH,
                              intY);
            graphics.drawLine(intX,
                              intY - CURSOR_HALFHEIGHT,
                              intX,
                              intY + CURSOR_HALFHEIGHT);

            graphics.drawLine(intX - (CURSOR_HALFWIDTH >> 1),
                              intY - (CURSOR_HALFHEIGHT >> 1),
                              intX + (CURSOR_HALFWIDTH >> 1),
                              intY + (CURSOR_HALFWIDTH >> 1));
            graphics.drawLine(intX + (CURSOR_HALFHEIGHT >> 1),
                              intY - (CURSOR_HALFHEIGHT >> 1),
                              intX - (CURSOR_HALFHEIGHT >> 1),
                              intY + (CURSOR_HALFHEIGHT >> 1));
            }
        }


    /***********************************************************************************************
     * Make sure that the Sun coordinates are up to date for the specified epoch.
     *
     * @param calendar
     */

    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        getSun().setCoordinates(SolarEphemerisDAO.calculateSunPositionRaDec(calendar.getJD()));
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
        //LOGGER.log("Sun before transform [RA=" + getSun().getCoordinates().x + "] [Dec=" + getSun().getCoordinates().y + "]" );
        CoordinateConversions.transformToViewportXY(getStarMapPoints(),
                                                    viewport,
                                                    calendar,
                                                    latitude,
                                                    originisnorth,
                                                    clickcoordinates);

        // For testing, transform back immediately
//        final Point2D.Double pointAzEl;
//        final Point2D.Double pointRaDec;
//
//        pointAzEl = CoordinateConversions.transformViewportXYtoAzEl(viewport, getSun().getViewportXY());
//        LOGGER.log("Sun after transform [Az=" + pointAzEl.x + "] [El=" + pointAzEl.y + "]" );
//
//        pointRaDec = CoordinateConversions.convertAzElToRaDec(pointAzEl, originisnorth, calendar.getLAST(), latitude);
//        LOGGER.log("Sun after transform [RA'=" + pointRaDec.x + "] [Dec'=" + pointRaDec.y + "]" );
        }
    }
