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
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.types.RightAscensionDataType;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;


/***************************************************************************************************
 * EphemeridesObjects.
 */

public final class EphemeridesObjects extends StarMapObject
    {
    private static final String PLUGIN_TITLE = "Observatory Ephemerides";
    private static final String OBJECT_TITLE = "Ephemeris Object";
    private static final String TOOLTIP_BUTTON = "Control all Ephemeris objects";
    private static final String FILENAME_BUTTON_ICON = "plugin-ephemerides.png";
    private static final Color EPHEMERIS_COLOR = UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor().brighter();

    private final Hashtable<String, EphemerisDAOInterface> tableDAOs;


    /***********************************************************************************************
     * Create a common toolbar button to control all Ephemeris objects.
     *
     * @param observatoryui
     * @param hostmap
     * @param plugin
     *
     * @return JButton
     */

    private static JButton createControlButton(final ObservatoryUIInterface observatoryui,
                                               final StarMapUIComponentPlugin hostmap,
                                               final StarMapPlugin plugin)
        {
        final ContextAction actionContext;
        final JButton button;

        // Create the toolbar button to control the plugin
        actionContext = new ContextAction(plugin.getPluginName(),
                                          RegistryModelUtilities.getAtomIcon(observatoryui.getHostAtom(),
                                                                             FILENAME_BUTTON_ICON),
                                          TOOLTIP_BUTTON,
                                          KeyEvent.VK_E,
                                          false,
                                          true)
            {
            public void actionPerformed(final ActionEvent event)
                {
                plugin.setActive(!plugin.isActive());
                hostmap.refreshStarMap();
                }
            };

        button = new JButton();
        button.setBorder(BORDER_BUTTON);
        button.setText(EMPTY_STRING);
        button.setAction(actionContext);
        button.setToolTipText((String)actionContext.getValue(Action.SHORT_DESCRIPTION));
        // Ensure that no text appears next to the Icon...
        button.setHideActionText(true);

        return (button);
        }


    /***********************************************************************************************
     * EphemeridesObjects.
     *
     * @param observatoryui
     * @param hostmap
     * @param daotable
     * @param clickable
     * @param labelled
     */

    public EphemeridesObjects(final ObservatoryUIInterface observatoryui,
                              final StarMapUIComponentPlugin hostmap,
                              final Hashtable<String, EphemerisDAOInterface> daotable,
                              final boolean clickable,
                              final boolean labelled)
        {
        super(observatoryui,
              hostmap,
              PLUGIN_TITLE,
              OBJECT_TITLE,
              EPHEMERIS_COLOR,
              labelled,
              clickable,
              DrawMode.POINT);

        this.tableDAOs = daotable;

        setButton(createControlButton(observatoryui, hostmap, this));
        setStarMapPoints(createObject());
        }


    /***********************************************************************************************
     * Create the Ephemerides objects as a single collection.
     *
     * @return Vector<StarMapPointInterface>
     */

    private Vector<Vector<StarMapPointInterface>> createObject()
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;

        vecCollection = new Vector<Vector<StarMapPointInterface>>(50);

        if ((getDAOTable() != null)
            && (!getDAOTable().isEmpty()))
            {
            final Enumeration<String> keys;

            keys = getDAOTable().keys();

            while(keys.hasMoreElements())
                {
                final EphemerisDAOInterface dao;

                dao = getDAOTable().get(keys.nextElement());

                if (dao != null)
                    {
                    final Ephemeris ephemeris;

                    ephemeris = dao.getEphemeris();

                    // Does this DAO have an Ephemeris?
                    // Make sure we don't add the Sun again...
                    if ((ephemeris != null)
                        && (!"Sun".equals(ephemeris.getName())))
                        {
                        final Vector<StarMapPointInterface> vecPoint;
                        final StarMapPointInterface pointObject;

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
                        }
                    }
                }
            }

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
        final String SOURCE = "EphemeridesObjects.labelObject() ";

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

            // The object Name
            graphics.drawString(point.getName(),
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
        final String SOURCE = "EphemeridesObjects.refreshCoordinates() ";
        final Vector<Vector<StarMapPointInterface>> vecCollection;
        final double dblLatitude;
        final List<Metadata> listAggregateMetadata;

        // What is the Location for this update?
        // Find the (latest) Observatory Latitude
        // The route to the Observatory is a bit convoluted...
        dblLatitude = EphemeridesHelper.getLatitude(REGISTRY.getFramework(),
                                                    (ObservatoryInterface) getObservatoryUI().getHostAtom(),
                                                    LOADER_PROPERTIES.isMetadataDebug());

        // Update the AggregateMetadata just once per refresh
        if ((getObservatoryUI() != null)
            && (getObservatoryUI().getHostAtom() instanceof ObservatoryInterface)
            && (getHostStarMap() != null)
            && (getHostStarMap().getHostInstrument() != null))
            {
            // The only way to find the current Instrument is via the StarMapUIComponentPlugin
            listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                  (ObservatoryInterface) getObservatoryUI().getHostAtom(),
                                                                                  getHostStarMap().getHostInstrument(),
                                                                                  getHostStarMap().getHostInstrument().getDAO(), null,
                                                                                  SOURCE,
                                                                                  LOADER_PROPERTIES.isMetadataDebug());
            }
        else
            {
            listAggregateMetadata = new ArrayList(1);
            }

        // Gather together the individual Ephemeris objects
        vecCollection = new Vector<Vector<StarMapPointInterface>>(50);

        if ((getDAOTable() != null)
            && (!getDAOTable().isEmpty()))
            {
            final Enumeration<String> keys;

            keys = getDAOTable().keys();

            while(keys.hasMoreElements())
                {
                final EphemerisDAOInterface dao;

                dao = getDAOTable().get(keys.nextElement());

                if (dao != null)
                    {
                    final Ephemeris ephemeris;

                    ephemeris = dao.getEphemeris();

                    // Does this DAO have an Ephemeris?
                    // Make sure we don't add the Sun again...
                    if ((ephemeris != null)
                        && (!"Sun".equals(ephemeris.getName())))
                        {
                        final Vector<StarMapPointInterface> vecPoints;
                        final CoordinateType coordinateType;
                        final StarMapPointInterface pointObject;

                        vecPoints = new Vector<StarMapPointInterface>(1);

                        // Make sure the DAO knows about the AggregateMetadata, if any
                        dao.setEphemerisMetadata(listAggregateMetadata);

                        coordinateType = dao.recalculateForJulianDate(calendar.getJD(),
                                                                      calendar.getLAST(),
                                                                      dblLatitude);

                        //ToDo getHostStarMap().getHostInstrument().getDAO().getEventLogFragment().addAll(dao.getEventLogFragment());

                        // Recalculate the position of the object as a StarMapPoint
                        pointObject = new StarMapPoint(this,
                                                       0,
                                                       ephemeris.getName(),
                                                       coordinateType,
                                                       dao.getCoordinates(),
                                                       getColour(),
                                                       isClickable(),
                                                       null);
                        vecPoints.add(pointObject);
                        vecCollection.add(vecPoints);
                        }
                    }
                }
            }

        setStarMapPoints(vecCollection);
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
        // For testing, transform back immediately
//        if ((getStarMapPoints() != null)
//            && (!getStarMapPoints().isEmpty()))
//            {
//            final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;
//
//            iterWholeCollection = getStarMapPoints().iterator();
//
//            while ((iterWholeCollection != null)
//                   && (iterWholeCollection.hasNext()))
//                {
//                final Vector<StarMapPointInterface> singleCollection;
//
//                singleCollection = iterWholeCollection.next();
//
//                if ((singleCollection != null)
//                    && (!singleCollection.isEmpty()))
//                    {
//                    final Iterator<StarMapPointInterface> iterSingleCollection;
//
//                    iterSingleCollection = singleCollection.iterator();
//
//                    while ((iterSingleCollection != null)
//                           && (iterSingleCollection.hasNext()))
//                        {
//                        final StarMapPointInterface point;
//
//                        point = iterSingleCollection.next();
//
//                        if (point != null)
//                            {
//                            final Point2D.Double pointAzEl;
//                            final Point2D.Double pointRaDec;
//
//                            pointAzEl = CoordinateConversions.transformViewportXYtoAzEl(viewport, point.getViewportXY());
//                            LOGGER.log(point.getName() + " after transform [Az=" + pointAzEl.x + "] [El=" + pointAzEl.y + "]" );
//
//                            pointRaDec = CoordinateConversions.convertAzElToRaDec(pointAzEl, originisnorth, calendar.getLAST(), latitude);
//                            LOGGER.log(point.getName() + " after transform [RA=" + pointRaDec.x + "] [Dec=" + pointRaDec.y + "]");
//                            }
//                        }
//                    }
//                }
//            }
        }


    /***********************************************************************************************
     * Get the injected table of Ephemeris DAOs.
     *
     * @return EphemerisDAOInterface
     */

    private Hashtable<String, EphemerisDAOInterface> getDAOTable()
        {
        return (this.tableDAOs);
        }
    }
