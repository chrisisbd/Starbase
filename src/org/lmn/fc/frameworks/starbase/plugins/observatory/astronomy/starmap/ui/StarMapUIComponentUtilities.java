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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.EpochConsumerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.TrueApparentConsumerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.datatypes.types.RightAscensionDataType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.SidebarIndicator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.MemoryImageSource;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * StarMapUIComponentUtilities.
 */

public final class StarMapUIComponentUtilities implements FrameworkConstants,
                                                          FrameworkStrings,
                                                          FrameworkMetadata,
                                                          FrameworkSingletons
    {
    // String Resources
    private static final String TOOLTIP_EPOCH = "Choose an Epoch";

    private static final int CURSOR_WIDTH = 32;
    private static final int CURSOR_HEIGHT = 32;

    // Azimuth scale compass points
    private static final String [] COMPASS =
        {
        "N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"
        };


    // Search pattern of degree offsets used for click detection
    private static final Point[][] SEARCH_PATTERN =
        {
            {
            new Point(0, 0)
            },
            {
            new Point(-1, -1),  new Point(0, -1),   new Point(1, -1),
            new Point(-1, 0),                       new Point(1, 0),
            new Point(-1, 1),   new Point(0, 1),    new Point(1, 1)
            },
            {
            new Point(-2, -2),  new Point(-1, -2),  new Point(0, -2),   new Point(1, -2),   new Point(2, -2),
            new Point(-2, -1),                                                              new Point(2, -1),
            new Point(-2, 0),                                                               new Point(2, 0),
            new Point(-2, 1),                                                               new Point(2, 1),
            new Point(-2, 2),   new Point(-1, 2),   new Point(0, 2),    new Point(1, 2),    new Point(2, 2)
            },
        };


    /***********************************************************************************************
     * Paint a StarMapPlugin collection of (x, y) points as a series of points.
     * The Plugin is assumed to be Active.
     * Points may be optionally labelled.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param plugin
     */

    public static void paintPluginAsPointXY(final Graphics graphics,
                                            final StarMapViewportInterface viewport,
                                            final Insets insets,
                                            final StarMapPlugin plugin)
        {
        if ((graphics !=null)
            && (viewport != null)
            && (plugin != null))
            {
            final Vector<Vector<StarMapPointInterface>> wholeCollection;

            wholeCollection = plugin.getStarMapPoints();

            if ((wholeCollection != null)
                && (!wholeCollection.isEmpty()))
                {
                final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

                iterWholeCollection = wholeCollection.iterator();

                while ((iterWholeCollection != null)
                    && (iterWholeCollection.hasNext()))
                    {
                    final Vector<StarMapPointInterface> singleCollection;

                    singleCollection = iterWholeCollection.next();

                    if ((singleCollection != null)
                        && (!singleCollection.isEmpty()))
                        {
                        final Iterator<StarMapPointInterface> iterSingleCollection;

                        iterSingleCollection = singleCollection.iterator();

                        while ((iterSingleCollection != null)
                            && (iterSingleCollection.hasNext()))
                            {
                            final StarMapPointInterface point;

                            point = iterSingleCollection.next();

                            if ((point != null)
                                && (point.isVisible()))
                                {
                                // Only now we can try to draw the Object
                                // represented by the StarMapPoint
                                plugin.drawObject(graphics,
                                                  viewport,
                                                  insets,
                                                  point);

                                // Optionally label the StarMapPoint
                                if (plugin.isLabelled())
                                    {
                                    plugin.labelObject(graphics,
                                                       viewport,
                                                       insets,
                                                       point);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Paint a StarMapPlugin collection of (x, y) points as a series of lines.
     * The Plugin is assumed to be Active.
     * Lines may be labelled at their end.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param plugin
     */

    public static void paintPluginAsLineXY(final Graphics graphics,
                                           final StarMapViewportInterface viewport,
                                           final Insets insets,
                                           final StarMapPlugin plugin)
        {
        if ((graphics !=null)
            && (viewport != null)
            && (plugin != null))
            {
            final Vector<Vector<StarMapPointInterface>> wholeCollection;

            wholeCollection = plugin.getStarMapPoints();

            if ((wholeCollection != null)
                && (!wholeCollection.isEmpty()))
                {
                final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

                iterWholeCollection = wholeCollection.iterator();

                while ((iterWholeCollection != null)
                    && (iterWholeCollection.hasNext()))
                    {
                    final Vector<StarMapPointInterface> lineCollection;

                    lineCollection = iterWholeCollection.next();

                    // See if we can draw one Line from the collection
                    if ((lineCollection != null)
                        && (!lineCollection.isEmpty()))
                        {
                        final Iterator<StarMapPointInterface> iterLineCollection;
                        StarMapPointInterface pointOnLine;
                        Point pointCurrentXY;
                        Point pointPreviousXY;
                        boolean boolFirstTime;

                        pointOnLine = null;
                        pointCurrentXY = null;
                        pointPreviousXY = null;
                        boolFirstTime = true;

                        iterLineCollection = lineCollection.iterator();

                        // Draw the line
                        while ((iterLineCollection != null)
                            && (iterLineCollection.hasNext()))
                            {
                            pointOnLine = iterLineCollection.next();

                            if ((pointOnLine != null)
                                && (pointOnLine.isVisible()))
                                {
                                pointCurrentXY = pointOnLine.getPixelsXY();

                                if (boolFirstTime)
                                    {
                                    // The first time through just record the point,
                                    // don't try to draw anything...
                                    pointPreviousXY = pointCurrentXY;
                                    boolFirstTime = false;
                                    }
                                else
                                    {
                                    final int intOffsetX;

                                    // Remember to compensate for any scale etc.
                                    if (viewport.isScaleEnabled())
                                        {
                                        intOffsetX = viewport.getElevationScaleWidth() + insets.left;
                                        }
                                    else
                                        {
                                        intOffsetX = insets.left;
                                        }

                                    graphics.setColor(pointOnLine.getColour());
                                    graphics.drawLine(pointPreviousXY.x + intOffsetX,
                                                      pointPreviousXY.y,
                                                      pointCurrentXY.x + intOffsetX,
                                                      pointCurrentXY.y);
                                    pointPreviousXY = pointCurrentXY;
                                    }
                                }
                            }

                        // Now draw the Object at the head of the Track
                        plugin.drawObject(graphics,
                                          viewport,
                                          insets,
                                          pointOnLine);

                        // Label the Object if required
                        if (plugin.isLabelled())
                            {
                            plugin.labelObject(graphics,
                                               viewport,
                                               insets,
                                               pointOnLine);
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Paint a StarMapPlugin collection of (x, y) points as a set of open Polygons.
     * The Plugin is assumed to be Active.
     * Polygons cannot be labelled.
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param plugin
     */

    public static void paintPluginAsOpenPolygonXY(final Graphics graphics,
                                                  final StarMapViewportInterface viewport,
                                                  final Insets insets,
                                                  final StarMapPlugin plugin)
        {
        paintPluginAsPolygon(graphics, plugin, viewport, false);
        }


    /***********************************************************************************************
     * Paint a StarMapPlugin collection of (x, y) points as a set of filled Polygons
     * The Plugin is assumed to be Active
     * Polygons cannot be labelled
     *
     * @param graphics
     * @param viewport
     * @param insets
     * @param plugin
     */

    public static void paintPluginAsFilledPolygonXY(final Graphics graphics,
                                                    final StarMapViewportInterface viewport,
                                                    final Insets insets,
                                                    final StarMapPlugin plugin)
        {
        paintPluginAsPolygon(graphics, plugin, viewport, true);
        }


    /***********************************************************************************************
     * Paint a StarMapPlugin collection of (x, y) points as Polygons, filled or open.
     * The Plugin is assumed to be Active.
     *
     * @param graphics
     * @param plugin
     * @param viewport
     * @param filled
     */

    private static void paintPluginAsPolygon(final Graphics graphics,
                                             final StarMapPlugin plugin,
                                             final StarMapViewportInterface viewport,
                                             final boolean filled)
        {
        if ((graphics !=null)
            && (plugin != null))
            {
            final Vector<Vector<StarMapPointInterface>> wholeCollection;

            wholeCollection = plugin.getStarMapPoints();

            if ((wholeCollection != null)
                && (!wholeCollection.isEmpty()))
                {
                final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

                iterWholeCollection = wholeCollection.iterator();

                while ((iterWholeCollection != null)
                    && (iterWholeCollection.hasNext()))
                    {
                    final Vector<StarMapPointInterface> polyCollection;

                    polyCollection = iterWholeCollection.next();

                    // See if we can draw one Polygon from the collection
                    if ((polyCollection != null)
                        && (!polyCollection.isEmpty()))
                        {
                        final Iterator<StarMapPointInterface> iterPolyCollection;
                        final Polygon polygonXY;
                        StarMapPointInterface point;
                        final Point pointStart;
                        final Point pointEnd;

                        polygonXY = new Polygon();
                        point = null;
                        pointStart = new Point();
                        pointEnd = new Point();

                        // Ensure the Polygon will be closed
                        polygonXY.addPoint(pointStart.x, pointStart.y);

                        iterPolyCollection = polyCollection.iterator();

                        while ((iterPolyCollection != null)
                            && (iterPolyCollection.hasNext()))
                            {
                            // Retain the last Point every time
                            point = iterPolyCollection.next();

                            if ((point != null)
                                && (point.isVisible()))
                                {
                                // Accumulate (x, y) into the Polygon
                                polygonXY.addPoint(point.getPixelsXY().x,
                                                   point.getPixelsXY().y);
                                }
                            }

                        // Ensure polygon closes correctly
                        polygonXY.addPoint(pointEnd.x, pointEnd.y);

                        // Paint the Polygon in the colour of the last Point
                        if ((polygonXY.npoints > 2)
                            && (point != null))
                            {
                            graphics.setColor(point.getColour());

                            if (filled)
                                {
                                graphics.fillPolygon(polygonXY);
                                }
                            else
                                {
                                graphics.drawPolygon(polygonXY);
                                }
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Draw the Elevation Scale.
     *
     * @param graphics
     * @param viewport
     * @param background
     * @param scale
     */

    public static void drawElevationScale(final Graphics graphics,
                                          final StarMapViewportInterface viewport,
                                          final Color background,
                                          final Color scale)
        {
        final FontMetrics fontMetrics;
        final int intScalePixelRange;
        final int intScaleDegreeRange;
        int intTickPixelY;
        final int intLabelHeightDeg;
        final int intLabelOffset;

        // Fill in the Scale area, ready for the ticks
        graphics.setColor(background);
        graphics.fillRect(viewport.getElevScaleTopLeft().x,
                          viewport.getElevScaleTopLeft().y,
                          StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH,
                          viewport.getElevScaleBottomRight().y - viewport.getElevScaleTopLeft().y);

        graphics.setColor(scale);
        graphics.drawLine(viewport.getElevScaleBottomRight().x,
                          viewport.getElevScaleTopLeft().y,
                          viewport.getElevScaleBottomRight().x,
                          viewport.getElevScaleBottomRight().y);

        // Calculate the space available
        intScalePixelRange = viewport.getElevScaleBottomRight().y - viewport.getElevScaleTopLeft().y;
        intScaleDegreeRange = (int)(viewport.getElevationNorth() - viewport.getElevationSouth());

        // Set the font for the labels, and get its metrics
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fontMetrics = graphics.getFontMetrics();

        // All the degree labels occupy (<tick><intLabelOffset>00<space>) pixels,
        // multiplied by the number of degrees currently visible (roughly)
        // reduce by one pixel per label by experiment...
        // Note that using getStringBounds() gives a rectangle which is too large
        intLabelOffset = 2;
        intLabelHeightDeg = (1 + intLabelOffset + fontMetrics.getAscent())
                             * intScaleDegreeRange;

        // Draw the tick marks along the scale
        for (double i = AstroMath.truncate(viewport.getElevationSouth());
                    i <= AstroMath.truncate(viewport.getElevationNorth()); i++)
            {
            intTickPixelY = (int)(viewport.getVertPixelsPerDegree()*(i-viewport.getElevationSouth()));

            // Draw a tick every degree, if there's room
            // Allow four pixels per degree tick
            if (intScalePixelRange > (intScaleDegreeRange << 2))
                {
                graphics.drawLine(viewport.getElevScaleBottomRight().x,
                                  viewport.getElevScaleBottomRight().y - intTickPixelY,
                                  viewport.getElevScaleBottomRight().x - (StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH /5),
                                  viewport.getElevScaleBottomRight().y - intTickPixelY);
                }

            // Label the single degree points,
            // but only if we are at a high zoom setting
            // Leave the 5 degree points for later, to avoid overpaint
            if ((intScaleDegreeRange < 20)
                && (intScalePixelRange > intLabelHeightDeg)
                && (AstroMath.fraction(i/5.0) != 0.0))
                {
                graphics.drawString(DecimalFormatPattern.ELEVATION_SCALE.format((int) i),
                                    viewport.getElevScaleTopLeft().x + intLabelOffset,
                                    viewport.getElevScaleBottomRight().y - intTickPixelY - intLabelOffset);
                }

            // Draw a minor tick every 5 degrees, if there's room
            // Allow four pixels per 5 degree tick (4/5 = 0.8)
            if ((AstroMath.fraction(i/5.0) == 0.0)
                && (intScalePixelRange > (intScaleDegreeRange * 0.8)))
                {
                graphics.drawLine(viewport.getElevScaleBottomRight().x,
                                  viewport.getElevScaleBottomRight().y - intTickPixelY,
                                  viewport.getElevScaleBottomRight().x - (StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH >> 1),
                                  viewport.getElevScaleBottomRight().y - intTickPixelY);
                }

            // Enlarge the minor to a major tick every 45 degrees
            if (AstroMath.fraction(i/45.0) == 0.0)
                {
                graphics.drawLine(viewport.getElevScaleBottomRight().x,
                                  viewport.getElevScaleBottomRight().y - intTickPixelY,
                                  viewport.getElevScaleBottomRight().x - (StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH),
                                  viewport.getElevScaleBottomRight().y - intTickPixelY);
                }

            // Label all 5 degree ticks (0...85), if there's room for
            // <tick><intLabelOffset>00<space> pixels for each visible 5 degree segment
            if ((AstroMath.fraction(i/5.0) == 0.0)
                && ((int)i < 90)
                &&  (intScalePixelRange > (intLabelHeightDeg / 5)))
                {
                graphics.drawString(DecimalFormatPattern.ELEVATION_SCALE.format((int)i),
                                    viewport.getElevScaleTopLeft().x + intLabelOffset,
                                    viewport.getElevScaleBottomRight().y - intTickPixelY - intLabelOffset);
                }
            }
        }


    /***********************************************************************************************
     * Draw the Azimuth Scale.
     *
     * @param graphics
     * @param viewport
     * @param background
     * @param scale
     */

    public static void drawAzimuthScale(final Graphics graphics,
                                        final StarMapViewportInterface viewport,
                                        final Color background,
                                        final Color scale)
        {
        final FontMetrics fontMetrics;
        final int intScalePixelRange;
        final int intScaleDegreeRange;
        int intTickPixelX;
        final int intLabelWidthNS;
        final int intLabelWidthDeg;
        final int intLabelOffset;

        // Fill in the Scale area, ready for the ticks
        graphics.setColor(background);
        graphics.fillRect(viewport.getAziScaleTopLeft().x,
                          viewport.getAziScaleTopLeft().y,
                          viewport.getAziScaleBottomRight().x-viewport.getAziScaleTopLeft().x,
                          StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT);

        graphics.setColor(scale);
        graphics.drawLine(viewport.getAziScaleTopLeft().x,
                          viewport.getAziScaleTopLeft().y,
                          viewport.getAziScaleBottomRight().x,
                          viewport.getAziScaleTopLeft().y);

        // Calculate the space available
        intScalePixelRange = viewport.getAziScaleBottomRight().x - viewport.getAziScaleTopLeft().x;
        intScaleDegreeRange = (int)(viewport.getAzimuthWest() - viewport.getAzimuthEast());

        // Set the font for the labels, and get its metrics
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fontMetrics = graphics.getFontMetrics();

        // All the compass labels occupy (<tick><intLabelOffset>WW<space>) pixels,
        // multiplied by the number of 45 degree segments currently visible (roughly)
        // reduce by one pixel per label by experiment...
        intLabelOffset = 2;
        Rectangle2D rectBounds = fontMetrics.getStringBounds("WW", graphics);
        intLabelWidthNS = (1 + intLabelOffset + (int)rectBounds.getWidth())
                          * (intScaleDegreeRange / 45);
        rectBounds = fontMetrics.getStringBounds("000", graphics);

        // In the same way, single degree labels occupy three characters,
        // for as many degrees as there are visible
        intLabelWidthDeg = (1 + intLabelOffset + (int)rectBounds.getWidth())
                            * intScaleDegreeRange;

        // Draw the tick marks along the scale
        for (double i = AstroMath.truncate(viewport.getAzimuthEast());
                    i <= AstroMath.truncate(viewport.getAzimuthWest()); i++)
            {
            intTickPixelX = (int)(viewport.getHorizPixelsPerDegree()*(i-viewport.getAzimuthEast()));

            // Draw a tick every degree, if there's room
            // Allow three pixels per degree tick
            // These are usually visible only when zooming,
            // unless the screen is at a high resolution
            if (intScalePixelRange > (intScaleDegreeRange * 3))
                {
                graphics.drawLine(viewport.getAziScaleTopLeft().x + intTickPixelX,
                                  viewport.getAziScaleTopLeft().y,
                                  viewport.getAziScaleTopLeft().x + intTickPixelX,
                                  viewport.getAziScaleTopLeft().y + (StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT /5));
                }

            // Label the single degree points,
            // but only if we are at a high zoom setting
            // Leave the 45 degree points as compass indicators
            if ((intScaleDegreeRange < 45)
                && (intScalePixelRange > intLabelWidthDeg)
                && (AstroMath.fraction(i/45.0) != 0.0))
                {
                graphics.drawString(DecimalFormatPattern.AZIMUTH_SCALE.format((int)i),
                                    viewport.getAziScaleTopLeft().x + intTickPixelX + intLabelOffset,
                                    viewport.getAziScaleTopLeft().y + (StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT) - intLabelOffset);
                }

            // Draw a minor tick every 5 degrees, if there's room
            // Allow four pixels per 5 degree tick (4/5 = 0.8)
            if ((AstroMath.fraction(i/5.0) == 0.0)
                && (intScalePixelRange > (intScaleDegreeRange * 0.8)))
                {
                graphics.drawLine(viewport.getAziScaleTopLeft().x + intTickPixelX,
                                  viewport.getAziScaleTopLeft().y,
                                  viewport.getAziScaleTopLeft().x + intTickPixelX,
                                  viewport.getAziScaleTopLeft().y + (StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT /3));
                }

            // Enlarge the minor to a major tick every 45 degrees
            if (AstroMath.fraction(i/45.0) == 0.0)
                {
                graphics.drawLine(viewport.getAziScaleTopLeft().x + intTickPixelX,
                                  viewport.getAziScaleTopLeft().y,
                                  viewport.getAziScaleTopLeft().x + intTickPixelX,
                                  viewport.getAziScaleTopLeft().y + (StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT));
                }

            // Label the 45 degree compass points, if there's room for
            // <tick><intLabelOffset>WW<space> pixels for each visible 45 degree segment
            if ((AstroMath.fraction(i/45.0) == 0.0)
                && (intScalePixelRange > intLabelWidthNS))
                {
                graphics.drawString(COMPASS[(int)(i/45.0)],
                                    viewport.getAziScaleTopLeft().x + intTickPixelX + intLabelOffset,
                                    viewport.getAziScaleTopLeft().y + (StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT) - intLabelOffset);
                }
            }
        }


    /***********************************************************************************************
     * Show the properties of the specified StarMapPoint.
     * Double check that the object is clickable.
     *
     * @param obsinstrument
     * @param point
     * @param debug
     */

    public static void showObjectProperties(final ObservatoryInstrumentInterface obsinstrument,
                                            final StarMapPointInterface point,
                                            final boolean debug)
        {
        if ((point != null)
            && (point.isClickable()))
            {
            final StarMapPlugin plugin;
            final Point2D.Double dblCoordinates;
            final List<Metadata> listMetadata;
            final String [] arrayMessage;
            int index;
            final Point2D.Double pointAzEl;
            final Point2D.Double pointRaDec;
            final Point2D.Double pointGalactic;
            final AstronomicalCalendarInterface calendarUpdate;
            final double dblLatitude;

            // Find the (latest) Observatory Latitude, Longitude
            calendarUpdate = EphemeridesHelper.getCalendarNow(REGISTRY.getFramework(),
                                                              obsinstrument,
                                                              debug);
            dblLatitude = EphemeridesHelper.getLatitude(REGISTRY.getFramework(),
                                                        (ObservatoryInterface)obsinstrument.getHostAtom(),
                                                        debug);
            arrayMessage = new String[50];
            index = 0;

            // There must be a host Plugin
            plugin = point.getHostPlugin();

            arrayMessage[index++] = "Plugin.Name = " + plugin.getPluginName();
            arrayMessage[index++] = "Object.Category = " + plugin.getObjectName();
            arrayMessage[index++] = "Object.Name = " + point.getName();

            dblCoordinates = point.getCoordinates();

            switch (point.getCoordinateType())
                {
                case AZEL:
                    {
                    pointAzEl = new Point2D.Double(dblCoordinates.getX(), dblCoordinates.getY());

                    // (Ra, Dec) RA in HOURS, Dec in DEGREES
                    pointRaDec = CoordinateConversions.convertAzElToRaDec(pointAzEl,
                                                                          EphemerisDAOInterface.ORIGIN_IS_NORTH,
                                                                          calendarUpdate.getLAST(),
                                                                          dblLatitude);
                    pointGalactic = CoordinateConversions.convertRaDecToGalactic(pointRaDec);

                    index = addPointCoordinates(point, arrayMessage, index, pointAzEl, pointRaDec, pointGalactic);
                    break;
                    }

                case RADEC:
                    {
                    // (Ra, Dec) RA in HOURS, Dec in DEGREES
                    pointRaDec = new Point2D.Double(dblCoordinates.getX(), dblCoordinates.getY());
                    pointAzEl = CoordinateConversions.convertRaDecToAzEl(pointRaDec,
                                                                         EphemerisDAOInterface.ORIGIN_IS_NORTH,
                                                                         calendarUpdate.getLAST(),
                                                                         dblLatitude);
                    pointGalactic = CoordinateConversions.convertRaDecToGalactic(pointRaDec);

                    index = addPointCoordinates(point, arrayMessage, index, pointAzEl, pointRaDec, pointGalactic);
                    break;
                    }

                case GALACTIC:
                    {
                    pointGalactic = new Point2D.Double(dblCoordinates.getX(), dblCoordinates.getY());
                    pointRaDec = CoordinateConversions.convertGalacticToRaDec(pointGalactic);
                    pointAzEl = CoordinateConversions.convertRaDecToAzEl(pointRaDec,
                                                                         EphemerisDAOInterface.ORIGIN_IS_NORTH,
                                                                         calendarUpdate.getLAST(),
                                                                         dblLatitude);

                    index = addPointCoordinates(point, arrayMessage, index, pointAzEl, pointRaDec, pointGalactic);
                    break;
                    }

                default:
                    {
                    arrayMessage[index++] = "Coordinate.x = " + dblCoordinates.getX();
                    arrayMessage[index++] = "Coordinate.y = " + dblCoordinates.getY();
                    }
                }

            // Now any Metadata
            listMetadata = point.getMetadata();

            if ((listMetadata != null)
                && (!listMetadata.isEmpty()))
                {
                final Iterator<Metadata> iterMetadata;

                iterMetadata = listMetadata.iterator();

                while (iterMetadata.hasNext())
                    {
                    final Metadata metadata;
                    String strUnits;

                    metadata = iterMetadata.next();
                    strUnits = metadata.getUnits().toString();

                    // Don't show Dimensionless Units
                    if (SchemaUnits.DIMENSIONLESS.toString().equals(strUnits))
                        {
                        strUnits = EMPTY_STRING;
                        }

                    arrayMessage[index++] = (metadata.getKey() + " = " + metadata.getValue() + SPACE + strUnits).trim();
                    }
                }

            JOptionPane.showMessageDialog(null,
                                          arrayMessage,
                                          "Object Properties",
                                          JOptionPane.INFORMATION_MESSAGE);
            }
        }


    /***********************************************************************************************
     * Add the coordinates of the StarMapPoint to the specified array of properties.
     * Return the new array index.
     *
     * @param point
     * @param message
     * @param index
     * @param azel
     * @param radec
     * @param galactic
     *
     * @return int
     */

    private static int addPointCoordinates(final StarMapPointInterface point,
                                           final String[] message,
                                           final int index,
                                           final Point2D.Double azel,
                                           final Point2D.Double radec,
                                           final Point2D.Double galactic)
        {
        int intIndex;
        final HourMinSecInterface hmsRA;

        intIndex = index;

        message[intIndex++] = "Object.Type = " + point.getCoordinateType().getSymbolPair();

        message[intIndex++] = "Object.Az = " + DecimalFormatPattern.AZIMUTH.format(azel.getX());
        message[intIndex++] = "Object.El = " + DecimalFormatPattern.ELEVATION.format(azel.getY());

        hmsRA = new RightAscensionDataType(radec.getX());
        message[intIndex++] = "Object.RA = " + hmsRA.toString_HH_MM_SS();
        message[intIndex++] = "Object.Dec = " + DecimalFormatPattern.SECONDS_DECLINATION.format(radec.getY());

        message[intIndex++] = "Object.l = " + DecimalFormatPattern.LONGITUDE_GALACTIC.format(galactic.getX());
        message[intIndex++] = "Object.b = " + DecimalFormatPattern.LATITUDE_GALACTIC.format(galactic.getY());

        return (intIndex);
        }


    /***********************************************************************************************
     * See if there is a clickable StarMap object at the specified location (x, y).
     * (x, y) is relative to the map area, minus the scales (if any) and insets.
     * Search successive areas around the location, offset by one degree each time:
     *
     *  2 2 2 2 2
     *  2 1 1 1 2
     *  2 1 0 1 2
     *  2 1 1 1 2
     *  2 2 2 2 2
     *
     * @param mapui
     * @param x
     * @param y
     *
     * @return StarMapPointInterface
     */

    public static StarMapPointInterface getObjectAtXY(final StarMapUIComponentPlugin mapui,
                                                      final int x,
                                                      final int y)
        {
        final String SOURCE = "StarMapUIComponentUtilities.getObjectAtXY() ";
        StarMapPointInterface pointTarget;

        //LOGGER.log(SOURCE + "Searching around [x=" + x + "] [y=" + y + "]");

        // Expect to fail
        pointTarget = null;

        if ((mapui != null)
            && (mapui.getClickablePoints() != null)
            && (mapui.getScreenViewport() != null))
            {
            int intAzimuth;
            int intAzimuthSearch;
            int intElevation;
            int intElevationSearch;

            // The Azimuth as an integer, to index into clickablePoints[][]
            // Convert the mouse position into Azimuth degrees offset from the left edge of the viewable map area (x=0)
            intAzimuth = (int)AstroMath.truncate(((double)(x) / mapui.getScreenViewport().getHorizPixelsPerDegree()) + mapui.getScreenViewport().getAzimuthEast());

            intAzimuth = Math.max(intAzimuth, (int)AstroMath.truncate(mapui.getScreenViewport().getAzimuthEast()));
            intAzimuth = Math.min(intAzimuth, (int)AstroMath.truncate(mapui.getScreenViewport().getAzimuthWest()));

            // The Elevation as an integer, to index into clickablePoints[][]
            // Convert the mouse position into Elevation degrees offset from the bottom edge of the viewable map area
            intElevation = (int)AstroMath.truncate(mapui.getScreenViewport().getElevationNorth() - ((double)(y) / mapui.getScreenViewport().getVertPixelsPerDegree()));

            intElevation = Math.max(intElevation, (int)AstroMath.truncate(mapui.getScreenViewport().getElevationSouth()));
            intElevation = Math.min(intElevation, (int)AstroMath.truncate(mapui.getScreenViewport().getElevationNorth()));

            //LOGGER.log("SEARCH .getObjectAtXY [intAzimuthDeg=" + intAzimuth + "] [intElevationDeg=" + intElevation + "]");

            // Look around the clicked area, to see if any object can be found
            // Search successive areas around the location, offset by one degree each time
            for (int i = 0;
                 ((i < SEARCH_PATTERN.length)
                    && (pointTarget == null));
                 i++)
                {
                //LOGGER.debugTimedEvent(".getObjectAtXY [pattern=" + i + "]");
                //LOGGER.debugTimedEvent(".getObjectAtXY [SEARCH_PATTERN[" + i + "].length=" + SEARCH_PATTERN[i].length + "]");

                for (int j = 0;
                     ((j < SEARCH_PATTERN[i].length)
                        && (pointTarget == null));
                     j++)
                    {
                    final Point pointDegreesOffset = SEARCH_PATTERN[i][j];

                    // Make sure we don't stray out of the viewport!
                    intAzimuthSearch = intAzimuth + pointDegreesOffset.x;
                    intAzimuthSearch = Math.max(intAzimuthSearch, (int)AstroMath.truncate(mapui.getScreenViewport().getAzimuthEast()));
                    intAzimuthSearch = Math.min(intAzimuthSearch, (int)AstroMath.truncate(mapui.getScreenViewport().getAzimuthWest()));

                    intElevationSearch = intElevation + pointDegreesOffset.y;
                    intElevationSearch = Math.max(intElevationSearch, (int)AstroMath.truncate(mapui.getScreenViewport().getElevationSouth()));
                    intElevationSearch = Math.min(intElevationSearch, (int)AstroMath.truncate(mapui.getScreenViewport().getElevationNorth()));

                    //LOGGER.debugTimedEvent(".getObjectAtXY [j=" + j + "] [pointSearch=(" + pointSearch.x + ", " + pointSearch.y + ")]");
                    //LOGGER.debugTimedEvent(".getObjectAtXY [j=" + j + "] [intAzimuthSearch=" + intAzimuthSearch + "] [intElevationSearch=" + intElevationSearch + "]");

                    if (mapui.getClickablePoints()[intAzimuthSearch][intElevationSearch] != null)
                        {
                        if (mapui.getClickablePoints()[intAzimuthSearch][intElevationSearch].isClickable())
                            {
                            //LOGGER.log(SOURCE + "Found at [x=" + intAzimuthSearch + "] [y=" + intElevationSearch + "]");

                            pointTarget = mapui.getClickablePoints()[intAzimuthSearch][intElevationSearch];
                            }
                        }
                    }
                }
            }

        return (pointTarget);
        }


    /***********************************************************************************************
     * Clear the Sky window.
     *
     * @param graphics
     * @param viewport
     */

    public static void clearSky(final Graphics graphics,
                                final StarMapViewportInterface viewport)
        {
        // Fill in the map window in the default sky colour
        if ((graphics != null)
            && (viewport != null))
            {
            graphics.setColor(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor().darker().darker().darker());
            graphics.fillRect(viewport.getTopLeft().x,
                              viewport.getTopLeft().y,
                              (viewport.getBottomRight().x-viewport.getTopLeft().x+1),
                              (viewport.getBottomRight().y-viewport.getTopLeft().y+1));
            }
        }


    /***********************************************************************************************
     * Create a custom Cursor.
     *
     * @return Cursor
     */

    public static Cursor createCustomCursor()
        {
        final Cursor cursor;
        final Image image;
        final int xScale;
        int yScale;

        int intRGB;
        final int[] pixels;

        pixels = new int[CURSOR_WIDTH*CURSOR_HEIGHT];

        for(int y=0; y<=CURSOR_HEIGHT; y++)
            {
            for(int x=0; x<=CURSOR_WIDTH; x++)
                {
                // All points transparent
                pixels[y+x]=0;
                }
            }

        // Draw the Cursor in a 10x10 grid in the top-left corner of a 32x32 grid
        yScale = 10;
        xScale = 10;

        // 00bbbbbb00xxxxxxxxxxxxxxxxxxxxxx
        // 11111111b1xxxxxxxxxxxxxxxxxxxxxx
        // 22b222222bxxxxxxxxxxxxxxxxxxxxxx
        // 33b333333bxxxxxxxxxxxxxxxxxxxxxx
        // 44b444444bxxxxxxxxxxxxxxxxxxxxxx
        // 55b55h555bxxxxxxxxxxxxxxxxxxxxxx
        // 66b666666bxxxxxxxxxxxxxxxxxxxxxx
        // 77b777777bxxxxxxxxxxxxxxxxxxxxxx
        // 88b8888888xxxxxxxxxxxxxxxxxxxxxx
        // 99bbbbbb99xxxxxxxxxxxxxxxxxxxxxx

        // Black circle - outside
        intRGB = Color.yellow.getRGB();

        for(int x=2; x<=8; x++)
            {
            // top
            pixels[x] = intRGB;
            }

        for(int x=2; x<=8; x++)
            {
            // bottom
            pixels[(yScale*CURSOR_WIDTH)+x] = intRGB;
            }

        for(int y=2; y<=8; y++)
            {
            // left
            pixels[CURSOR_WIDTH*y] = intRGB;
            }

        for(int y=2; y<=8; y++)
            {
            // right
            pixels[(CURSOR_WIDTH*y)+yScale] = intRGB;
            }

        pixels[1+CURSOR_WIDTH] = intRGB;
        pixels[yScale+CURSOR_WIDTH-1] = intRGB;
        pixels[1+(CURSOR_WIDTH*(yScale-1))] = intRGB;
        pixels[(CURSOR_WIDTH*(yScale-1))+yScale-1] = intRGB;

        // White circle - inside
        intRGB=Color.white.getRGB();
        yScale=yScale-1;

        // Never used
        //xScale=xScale-1;

        for(int x=3; x<=7; x++)
            {
            // up
            pixels[x+CURSOR_WIDTH] = intRGB;
            }

        for(int x=3; x<=7; x++)
            {
            // bottom
            pixels[(yScale*CURSOR_WIDTH)+x] = intRGB;
            }

        for(int y=3; y<=7; y++)
            {
            // left
            pixels[CURSOR_WIDTH*y+1] = intRGB;
            }

        for(int y=3; y<=7; y++)
            {
            // right
            pixels[(CURSOR_WIDTH*y)+yScale] = intRGB;
            }

        pixels[2+CURSOR_WIDTH+CURSOR_WIDTH] = intRGB;
        pixels[yScale+CURSOR_WIDTH+CURSOR_WIDTH-1] = intRGB;
        pixels[1+(CURSOR_WIDTH*(yScale-1))+1] = intRGB;
        pixels[(CURSOR_WIDTH*(yScale-1))+yScale-1] = intRGB;

        // CURSOR_WIDTH --> scan - the distance from one row of pixels to the next in the array
        image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(CURSOR_WIDTH,
                                                                              CURSOR_HEIGHT,
                                                                              pixels,
                                                                              0,
                                                                              CURSOR_WIDTH));
        cursor = Toolkit.getDefaultToolkit().createCustomCursor(image,
                                                                new Point(5,5),
                                                                "starmap");

        return (cursor);
        }


    /***********************************************************************************************
     * Update the RA and Dec Indicators, given an (A, El) point.
     * Return the updated (Ra, Dec), or (0.0, 0.0) if conversion was not possible.
     *
     * @param hostinstrument
     * @param azeldegrees
     * @param originisnorth
     * @param indicatorra
     * @param indicatordec
     * @param debug
     * @param SOURCE
     *
     * @return Point2D.Double
     */

    public static Point2D.Double updateRaDecIndicators(final ObservatoryInstrumentInterface hostinstrument,
                                                       final Point2D.Double azeldegrees,
                                                       final boolean originisnorth,
                                                       final IndicatorInterface indicatorra,
                                                       final IndicatorInterface indicatordec,
                                                       final boolean debug,
                                                       final String SOURCE)
        {
        final Point2D.Double pointUpdatedRaDec;

        if ((hostinstrument != null)
            && (hostinstrument.getObservatoryClock() != null))
            {
            final AstronomicalCalendarInterface calendarUpdate;
            final double dblLatitude;
            final double dblLAST;
            final Point2D.Double pointRaDec;
            final HourMinSecInterface hmsRA;
            final HourMinSecInterface hmsLAST;

            // What is the Location for this update?
            // Use the (latest) Observatory Latitude, Longitude
            calendarUpdate = EphemeridesHelper.getCalendarNow(REGISTRY.getFramework(),
                                                              hostinstrument,
                                                              debug);
            dblLatitude = EphemeridesHelper.getLatitude(REGISTRY.getFramework(),
                                                        (ObservatoryInterface)hostinstrument.getHostAtom(),
                                                        debug);

            // Calculate the (Az, El) point in (Ra, Dec)
            // (Ra, Dec) RA in HOURS, Dec in DEGREES
            // LAST is in HOURS
            dblLAST = calendarUpdate.getLAST();
            pointRaDec = CoordinateConversions.convertAzElToRaDec(azeldegrees,
                                                                  originisnorth,
                                                                  dblLAST,
                                                                  dblLatitude);
            hmsLAST = new HourMinSecDataType(dblLAST);
            hmsLAST.enableFormatSign(false);

            // Convert RA to HMS for display
            hmsRA = new RightAscensionDataType(pointRaDec.getX());

//            System.out.println(SOURCE + "convertAzElToRaDec() [Az= " + azeldegrees.getX()
//                               + "] [El= " + azeldegrees.getY()
//                               + "] [RAdeg= " + pointRaDec.getX()
//                               + "] [RAhms= " + hmsRA.toString_HH_MM_SS()
//                               + "] [Dec= " + pointRaDec.getY()
//                               + "] [LAST=" + hmsLAST.toString_HH_MM_SS()
//                               + "] [long=" + Double.toString(calendarUpdate.getLongitude())
//                               + "] [lat=" + Double.toString(dblLatitude)
//                               + "]");

            indicatorra.setValue(hmsRA.toString_HH_MM_SS());
            indicatordec.setValue(DecimalFormatPattern.SECONDS_DECLINATION.format(pointRaDec.getY()));

            pointUpdatedRaDec = pointRaDec;
            }
        else
            {
            indicatorra.setValue(NO_CLOCK);
            indicatordec.setValue(NO_CLOCK);

            pointUpdatedRaDec = new math.geom2d.Point2D(0.0, 0.0);
            }

        return (pointUpdatedRaDec);
        }


    /***********************************************************************************************
     * Update the Galactic Longitude and Latitude Indicators, given an (RA, Dec) point.
     * Return the updated (l, b), or (0.0, 0.0) if conversion was not possible.
     *
     * @param pointradec
     * @param indicatorlong
     * @param indicatorlat
     * @param debug
     * @param source
     *
     * @return Point2D.Double
     */

    public static Point2D.Double updateGalacticIndicators(final Point2D.Double pointradec,
                                                          final IndicatorInterface indicatorlong,
                                                          final IndicatorInterface indicatorlat,
                                                          final boolean debug,
                                                          final String source)
        {
        final Point2D.Double pointUpdatedLongLat;

        if (pointradec != null)
            {
            final Point2D.Double pointLB;
            final DegMinSecInterface dmsGalacticLongitude;
            final DegMinSecInterface dmsGalacticLatitude;

            pointLB = CoordinateConversions.convertRaDecToGalactic(pointradec);

            dmsGalacticLongitude = new LongitudeDataType(new BigDecimal(pointLB.getX()));
            dmsGalacticLongitude.setDisplayFormat(DegMinSecFormat.NONE);
            dmsGalacticLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);

            indicatorlong.setValue(dmsGalacticLongitude.toString());

            dmsGalacticLatitude = new LatitudeDataType(new BigDecimal(pointLB.getY()));
            dmsGalacticLatitude.setDisplayFormat(DegMinSecFormat.SIGN);
            dmsGalacticLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);

            indicatorlat.setValue(dmsGalacticLatitude.toString());

            pointUpdatedLongLat = pointLB;
            }
        else
            {
            indicatorlong.setValue(NO_CLOCK);
            indicatorlat.setValue(NO_CLOCK);

            pointUpdatedLongLat = new math.geom2d.Point2D(0.0, 0.0);
            }

        return (pointUpdatedLongLat);
        }


    /***********************************************************************************************
     * Constrain the range of the specified (Az, El) point.
     *
     * @param azel
     */

    public static void constrainAzElRange(final Point2D.Double azel)
        {
        double dblAzDegrees;
        double dblElDegrees;

        dblAzDegrees = azel.getX();
        dblElDegrees = azel.getY();

        if (dblAzDegrees < CoordinateConversions.AZI_MIN)
            {
            dblAzDegrees = CoordinateConversions.AZI_MIN;
            }

        if (dblAzDegrees > CoordinateConversions.AZI_MAX)
            {
            dblAzDegrees = CoordinateConversions.AZI_MAX;
            }

        if (dblElDegrees < CoordinateConversions.ELEV_MIN)
            {
            dblElDegrees = CoordinateConversions.ELEV_MIN;
            }

        if (dblElDegrees > CoordinateConversions.ELEV_MAX)
            {
            dblElDegrees = CoordinateConversions.ELEV_MAX;
            }

        // Update the (possibly) modified coordinates ready for the RaDec transformation
        azel.setLocation(dblAzDegrees, dblElDegrees);
        }


    /***********************************************************************************************
     * Create a drop-down of Epochs.
     *
     * @param epochcombo
     * @param font
     * @param foreground
     * @param background
     * @param epochconsumer
     *
     * @return JComboBox
     */

    public static JComboBox createEpochCombo(final JComboBox epochcombo,
                                             final FontInterface font,
                                             final ColourInterface foreground,
                                             final ColourInterface background,
                                             final EpochConsumerInterface epochconsumer)
        {
        final String SOURCE = "StarMapUIComponentUtilities.createEpochCombo() ";
        final Epoch[] arrayEpoch;
        final ActionListener choiceListener;

        if ((font != null)
            && (foreground != null)
            && (background != null))
            {
            // Copy the style of the StarMapUI
            epochcombo.setFont(font.getFont());
            epochcombo.setForeground(foreground.getColor());
            epochcombo.setBackground(background.getColor());
            epochcombo.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                               font,
                                                               foreground,
                                                               UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND));
            }

        // Do NOT allow the combo box to take up all the remaining space!
        epochcombo.setPreferredSize(SidebarIndicator.DIM_SIDEBAR_INDICATOR);
        epochcombo.setMaximumSize(SidebarIndicator.DIM_SIDEBAR_INDICATOR);
        epochcombo.setAlignmentX(0);

        epochcombo.setToolTipText(TOOLTIP_EPOCH);
        epochcombo.setEnabled(true);
        epochcombo.setEditable(false);

        arrayEpoch = Epoch.values();

        for (int intEpochIndex = 0;
             intEpochIndex < arrayEpoch.length;
             intEpochIndex++)
            {
            final Epoch epoch;

            epoch = arrayEpoch[intEpochIndex];

            // Add the enum Object, not just the name
            epochcombo.addItem(epoch);
            }

        // Beware that there might not have been any valid Epochs
        if (epochcombo.getItemCount() > 0)
            {
            epochcombo.setSelectedIndex(0);
            epochcombo.revalidate();
            }

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                epochconsumer.setSelectedEpoch((Epoch)epochcombo.getSelectedItem());
                }
            };

        epochcombo.addActionListener(choiceListener);

        return (epochcombo);
        }


    /***********************************************************************************************
     * Create the radio buttons to select between True or Apparent mode.
     *
     * @param consumer
     * @param font
     * @param foreground
     * @param background
     * @param truedefault
     *
     * @return JPanel
     */

    public static JPanel createTrueApparentSelector(final TrueApparentConsumerInterface consumer,
                                                    final FontInterface font,
                                                    final ColourInterface foreground,
                                                    final ColourInterface background,
                                                    final boolean truedefault)
        {
        final JPanel panelTA;
        final ButtonGroup buttonGroup;
        final JRadioButton buttonTrue;
        final JRadioButton buttonApparent;
        final ActionListener listenerTrue;
        final ActionListener listenerApparent;

        // Set the default selection to the supplied state
        consumer.setTrueMode(truedefault);

        panelTA = new JPanel();
        panelTA.setLayout(new BoxLayoutFixed(panelTA, BoxLayoutFixed.X_AXIS));
        panelTA.setBackground(background.getColor());
        panelTA.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttonGroup = new ButtonGroup();

        // Set up the True button
        buttonTrue = new JRadioButton("True");
        buttonTrue.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonTrue.setToolTipText("Use True Elevation");
        //buttonTrue.setFont(font.getFont());
        buttonTrue.setForeground(foreground.getColor());
        buttonTrue.setBackground(background.getColor());

        listenerTrue = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                consumer.setTrueMode(buttonTrue.isSelected());
                }
            };

        buttonTrue.addActionListener(listenerTrue);
        buttonGroup.add(buttonTrue);
        buttonGroup.setSelected(buttonTrue.getModel(), truedefault);

        // Set up the Apparent button
        buttonApparent = new JRadioButton("Apparent");
        buttonApparent.setHorizontalTextPosition(SwingConstants.RIGHT);
        buttonApparent.setToolTipText("Use Apparent Elevation");
        //buttonApparent.setFont(font.getFont());
        buttonApparent.setForeground(foreground.getColor());
        buttonApparent.setBackground(background.getColor());

        listenerApparent = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                consumer.setTrueMode(!buttonApparent.isSelected());
                }
            };

        buttonApparent.addActionListener(listenerApparent);
        buttonGroup.add(buttonApparent);
        buttonGroup.setSelected(buttonApparent.getModel(), !truedefault);

        panelTA.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_LABEL_SEPARATOR.width));
        panelTA.add(buttonTrue);
        panelTA.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_LABEL_SEPARATOR.width));
        panelTA.add(buttonApparent);
        panelTA.add(Box.createHorizontalStrut(UIComponentPlugin.DIM_LABEL_SEPARATOR.width));

        return (panelTA);
        }
    }
