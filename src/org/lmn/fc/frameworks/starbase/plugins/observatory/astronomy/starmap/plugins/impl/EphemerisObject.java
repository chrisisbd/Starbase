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

import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.types.RightAscensionDataType;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Vector;


/***************************************************************************************************
 * EphemerisObject.
 */

public final class EphemerisObject extends StarMapObject
    {
    private static final Color EPHEMERIS_COLOR = UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor();
    private static final String PLUGIN_TITLE = "Observatory Ephemeris";


    private final Ephemeris ephemeris;
    private final EphemerisDAOInterface dao;


    /***********************************************************************************************
     * EphemerisObject.
     *
     * @param observatoryui
     * @param hostmap
     * @param ephem
     * @param ephemdao
     * @param clickable
     * @param labelled
     */

    public EphemerisObject(final ObservatoryUIInterface observatoryui,
                           final StarMapUIComponentPlugin hostmap,
                           final Ephemeris ephem,
                           final EphemerisDAOInterface ephemdao,
                           final boolean clickable,
                           final boolean labelled)
        {
        super(observatoryui,
              hostmap,
              PLUGIN_TITLE,
              ephem.getName(),
              EPHEMERIS_COLOR,
              labelled,
              clickable,
              DrawMode.POINT);

        this.ephemeris = ephem;
        this.dao = ephemdao;

        setButton(null);
        setStarMapPoints(createObject());
        }


    /***********************************************************************************************
     * Create the Ephemeris object as a single point.
     *
     * @return Vector<StarMapPointInterface>
     */

    private Vector<Vector<StarMapPointInterface>> createObject()
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;
        final Vector<StarMapPointInterface> vecPoint;
        final StarMapPointInterface pointObject;

        vecCollection = new Vector<Vector<StarMapPointInterface>>(1);
        vecPoint = new Vector<StarMapPointInterface>(1);

        // Create a dummy invisible Object as a single StarMapPoint,
        // which will be replaced after refreshCoordinates()
        pointObject = new StarMapPoint(this,
                                       0,
                                       EMPTY_STRING,
                                       CoordinateType.UNASSIGNED,
                                       new Point2D.Double(0.0, 0.0),
                                       getColour(),
                                       isClickable(),
                                       null);
        vecPoint.add(pointObject);
        vecCollection.add(vecPoint);

        return (vecCollection);
        }


    /***********************************************************************************************
     * Draw the Ephemeris object.
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
            graphics.fillOval(intX - 1,
                              intY - 1,
                              3,
                              3);
            }
        }


    /***********************************************************************************************
     * Label the Ephemeris object.
     * point specifies the centre of the Ephemeris object.
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
        final String SOURCE = "EphemerisObject.labelObject() ";

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

            graphics.drawString(getEphemeris().getName(),
                                intX,
                                intTextOffset);

            switch (point.getCoordinateType())
                {
                case UNASSIGNED:
                    {
                    graphics.drawString(point.getCoordinateType().getSymbolX(),
                                        intX,
                                        intTextOffset + intAscent);
                    graphics.drawString(point.getCoordinateType().getSymbolY(),
                                        intX,
                                        intTextOffset + intAscent + intAscent);
                    break;
                    }

                case AZEL:
                case AZEL_METADATA:
                    {
                    graphics.drawString(point.getCoordinateType().getSymbolX() + SPACE + DecimalFormatPattern.AZIMUTH.format(point.getCoordinates().x),
                                        intX,
                                        intTextOffset + intAscent);
                    graphics.drawString(point.getCoordinateType().getSymbolY() + SPACE + DecimalFormatPattern.ELEVATION.format(point.getCoordinates().y),
                                        intX,
                                        intTextOffset + intAscent + intAscent);
                    break;
                    }

                case RADEC:
                case RADEC_METADATA:
                    {
                    final HourMinSecInterface hmsRA;

                    // Assume we are given (Ra, Dec)
                    hmsRA = new RightAscensionDataType(point.getCoordinates().x);

                    graphics.drawString(point.getCoordinateType().getSymbolX() + SPACE + hmsRA.toString_HH_MM_SS(),
                                        intX,
                                        intTextOffset + intAscent);
                    graphics.drawString(point.getCoordinateType().getSymbolY() + SPACE + DecimalFormatPattern.SECONDS_DECLINATION.format(point.getCoordinates().y),
                                        intX,
                                        intTextOffset + intAscent + intAscent);
                    break;
                    }

                case GALACTIC:
                case GALACTIC_METADATA:
                    {
                    graphics.drawString(point.getCoordinateType().getSymbolX() + SPACE + DecimalFormatPattern.LONGITUDE_GALACTIC.format(point.getCoordinates().x),
                                        intX,
                                        intTextOffset + intAscent);
                    graphics.drawString(point.getCoordinateType().getSymbolY() + SPACE + DecimalFormatPattern.LATITUDE_GALACTIC.format(point.getCoordinates().y),
                                        intX,
                                        intTextOffset + intAscent + intAscent);
                    break;
                    }

                default:
                    {
                    LOGGER.error(SOURCE + "Invalid CoordinateType [type=" + point.getCoordinateType().getName() + "]");
                    }
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
            }
        }


    /***********************************************************************************************
     * Make sure that the object coordinates are up to date for the specified epoch.
     *
     * @param calendar
     */

    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;
        final Vector<StarMapPointInterface> vecPoint;
        final StarMapPointInterface pointObject;
        final double dblLatitude;
        final CoordinateType coordinateType;

        // What is the Location for this update?
        // Find the (latest) Observatory Latitude
        // The route to the Observatory is a bit convoluted...
        dblLatitude = EphemeridesHelper.getLatitude(REGISTRY.getFramework(),
                                                    (ObservatoryInterface) getObservatoryUI().getHostAtom(),
                                                    LOADER_PROPERTIES.isMetadataDebug());

        // ToDo I am sure this could be made much more efficient!
        vecCollection = new Vector<Vector<StarMapPointInterface>>(1);
        vecPoint = new Vector<StarMapPointInterface>(1);

        coordinateType = getEphemerisDAO().recalculateForJulianDate(calendar.getJD(),
                                                                    calendar.getLAST(),
                                                                    dblLatitude);

        // ToDo getHostStarMap().getHostInstrument().getDAO().getEventLogFragment().addAll(getEphemerisDAO().getEventLogFragment());

        // Recalculate the position of the object as a StarMapPoint(Ra, Dec)
        pointObject = new StarMapPoint(this,
                                       0,
                                       getEphemerisDAO().getEphemeris().getName(),
                                       coordinateType,
                                       getEphemerisDAO().getCoordinates(),
                                       getColour(),
                                       isClickable(),
                                       null);
        vecPoint.add(pointObject);

        vecCollection.add(vecPoint);
        setStarMapPoints(vecCollection);
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


    /***********************************************************************************************
     * Get the injected Ephemeris.
     *
     * @return Ephemeris
     */

    private Ephemeris getEphemeris()
        {
        return (this.ephemeris);
        }


    /***********************************************************************************************
     * Get the injected Ephemeris DAO.
     *
     * @return EphemerisDAOInterface
     */

    private EphemerisDAOInterface getEphemerisDAO()
        {
        return (this.dao);
        }
    }
