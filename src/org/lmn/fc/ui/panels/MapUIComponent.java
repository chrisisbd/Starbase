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
//  ??-09-04    LMN created file
//  07-10-04    LMN added Longitude & Latitude scaling
//  11-10-04    LMN converted Longitude & Latitude to DegMinSec
//  12-10-04    LMN added Grid References, improved UI
//  13-10-04    LMN added PointsOfInterest rendering and HostCountry
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.panels;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.coords.GridReferenceConverter;
import org.lmn.fc.common.utilities.coords.LatitudeLongitude;
import org.lmn.fc.common.utilities.coords.OSGBGridReference;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * A general purpose MapUIComponent.
 *
 * ToDo Fix Exceptions, do not extend EditorUIComponent
 */

public final class MapUIComponent extends EditorUIComponent
    {
    // String Resources
    private static final String TOOLTIP_LONGITUDE   = "Longitude";
    private static final String TOOLTIP_LATITUDE    = "Latitude";
    private static final String TOOLTIP_GRID        = "Grid Reference";
    private static final String ACTION_TOGGLE_POI   = "Toggle Points of Interest";
    private static final String TOOLTIP_TOGGLE_POI  = "Toggle Points of Interest";
    private static final String FORMAT_LONGITUDE    = "MM:MM:MME";
    private static final String FORMAT_LATITUDE     = "MM:MM:MMN";
    private static final String FORMAT_GRID         = "00000000 00000000";

    private static final Dimension DIM_TITLE = new Dimension(150, 25);
    private static final Dimension DIM_TOOLBAR_SIZE = new Dimension(2000, 25);
    private static final Dimension DIM_TOOLBAR_SEPARATOR = new Dimension(20, 5);
    private static final Dimension DIM_LABEL_SEPARATOR = new Dimension(5, 5);
    private static final Dimension DIM_GRID_REFERENCE = new Dimension(110, 20);
    private static final Border BORDER_BUTTON = BorderFactory.createEmptyBorder(3, 3, 3, 3);

    private final FrameworkPlugin pluginFramework;
    private JPanel panelUI;
    private JToolBar toolBar;
    private JLabel labelTitle;
    private final CountryPlugin countryHost;
    private IndicatorInterface indicatorLongitude;
    private IndicatorInterface indicatorLatitude;
    private IndicatorInterface indicatorGridRef;
    private JScrollPane scrollPaneMap;
    private JPanel panelMap;
    private JLabel labelMap;
    private Icon iconMap;
    private Point2D.Double pointTopLeft;
    private Point2D.Double pointBottomRight;
    private double dblScaleX;
    private double dblScaleY;
    private Point pointStartDrag;
    private boolean boolDoDrag;
    private boolean boolScaleValid;
    private JButton buttonPOI;
    private final List<PointOfInterest> listPOI;
    private boolean boolShowPOI;


    /***********************************************************************************************
     * Get the MapUIComponent from the specified Tab on the JTabbedPane.
     * May return <code>null</code>.
     *
     * @param tabbedpane
     * @param index
     *
     * @return MapUIComponent
     */

//    public static MapUIComponent getMapViewer(final JTabbedPane tabbedpane,
//                                        final int index)
//        {
//        if ((tabbedpane != null)
//            && (tabbedpane.getComponentAt(index) != null)
//            && (tabbedpane.getComponentAt(index) instanceof MapUIComponent))
//            {
//            return ((MapUIComponent)tabbedpane.getComponentAt(index));
//            }
//        else
//            {
//            return (null);
//            }
//        }


    /***********************************************************************************************
     * Construct a MapUIComponent, where no scale information is available.
     *
     * @param filename
     * @param title
     * @param country
     * @param resourcekey
     *
     * @throws IndicatorException
     */

//    public MapUIComponent(final String filename,
//                    final String title,
//                    final CountryPlugin country,
//                    final String resourcekey) throws IndicatorException
//        {
//        super(resourcekey);
//        setLayout(new BorderLayout());
//
//        readResources();
//
//        countryHost = country;
//        pointTopLeft = null;
//        pointBottomRight = null;
//        dblScaleX = 0.0;
//        dblScaleY = 0.0;
//        boolScaleValid = false;
//        listPOI = new Vector<MappedPointOfInterest>();
//        boolShowPOI = true;
//
//        // Set up an unscaled Map
//        replaceMap(filename, title);
//        }


    /***********************************************************************************************
     * Construct a MapUIComponent with scale information.
     *
     * @param framework
     * @param country
     *
     * @throws IndicatorException
     */

    public MapUIComponent(final FrameworkPlugin framework,
                          final CountryPlugin country) throws IndicatorException
        {
        super(framework.getResourceKey());
        setLayout(new BorderLayout());

        pluginFramework = framework;

        readResources();

        countryHost = country;
        pointTopLeft = new Point2D.Double(framework.getMapTopLeftLongitude().toDouble(),
                                          framework.getMapTopLeftLatitude().toDouble());
        pointBottomRight = new Point2D.Double(framework.getMapBottomRightLongitude().toDouble(),
                                              framework.getMapBottomRightLatitude().toDouble());
        dblScaleX = 1.0;
        dblScaleY = 1.0;
        boolScaleValid = false;
        listPOI = new ArrayList<PointOfInterest>(10);
        boolShowPOI = true;

        // Set up a scaled Map
        replaceScaledMap(framework.getMapTopLeftLongitude(),
                         framework.getMapTopLeftLatitude(),
                         framework.getMapBottomRightLongitude(),
                         framework.getMapBottomRightLatitude(),
                         framework.getMapFilename(),
                         framework.getName());
        }


    /***********************************************************************************************
     * Clear the map on the host JComponent.
     * Invalidate the scale factor.
     *
     * @throws IndicatorException
     */

    public final void clearMap() throws IndicatorException
        {
        removeAll();
        add(createMapPanel("", ""));
        repaint();

        // Clear the coordinates and invalidate the Scale information
        setTopLeft(null);
        setBottomRight(null);
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Replace the map on the host JComponent.
     * Invalidate the scale factor.
     * The map is assumed to have the same host Country as the original.
     *
     * @param filename
     * @param title
     *
     * @throws IndicatorException
     */

    private void replaceMap(final String filename,
                            final String title) throws IndicatorException
        {
        removeAll();
        add(createMapPanel(filename, title));
        repaint();

        // Clear the coordinates and invalidate the Scale information
        setTopLeft(null);
        setBottomRight(null);
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Replace the map on the host JComponent.
     * Recalculate the scale factor.
     * The map is assumed to have the same host Country as the original.
     *
     * @param topleftlong
     * @param topleftlat
     * @param bottomrightlong
     * @param bottomrightlat
     * @param filename
     * @param title
     *
     * @throws IndicatorException
     */

    private void replaceScaledMap(final DegMinSecInterface topleftlong,
                                       final DegMinSecInterface topleftlat,
                                       final DegMinSecInterface bottomrightlong,
                                       final DegMinSecInterface bottomrightlat,
                                       final String filename,
                                       final String title) throws IndicatorException
        {
        removeAll();
        add(createMapPanel(filename, title));
        repaint();

        // Set the coordinates and recalculate the Scale information
        setTopLeft(new Point2D.Double(topleftlong.toDouble(),
                                      topleftlat.toDouble()));
        setBottomRight(new Point2D.Double(bottomrightlong.toDouble(),
                                          bottomrightlat.toDouble()));
        setScaleValid(recalculateScale());
        }


    /***********************************************************************************************
     * Create the JPanel on which the Map is displayed.
     *
     * @param filename
     * @param title
     *
     * @return JComponent
     *
     * @throws IndicatorException
     */

    private JComponent createMapPanel(final String filename,
                                      final String title) throws IndicatorException
        {
        final URL imageURL;
        final ContextAction actionContext;

        // Clear any existing PointsOfInterest
        clearPointsOfInterest();

        // Create the Map JToolBar
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);

        // Map Title
        labelTitle = new JLabel(title + SPACE);
        labelTitle.setMinimumSize(DIM_TITLE);
        labelTitle.setForeground(getTextColour().getColor());
        labelTitle.setFont(getLabelFont().getFont());
        getToolBar().addSeparator(DIM_LABEL_SEPARATOR);
        getToolBar().add(labelTitle);
        getToolBar().addSeparator(DIM_TOOLBAR_SEPARATOR);

        // Longitude
        indicatorLongitude = new ToolbarIndicator("", TOOLTIP_LONGITUDE);
        indicatorLongitude.setValueFormat(FORMAT_LONGITUDE);
        indicatorLongitude.setValueBackground(Color.BLACK);
        final JLabel labelLongitude = new JLabel(TOOLTIP_LONGITUDE);
        labelLongitude.setForeground(getTextColour().getColor());
        labelLongitude.setFont(getLabelFont().getFont());
        getToolBar().add(labelLongitude);
        getToolBar().addSeparator(DIM_LABEL_SEPARATOR);
        getToolBar().add((Component)indicatorLongitude);
        getToolBar().addSeparator(DIM_TOOLBAR_SEPARATOR);

        // Latitude
        indicatorLatitude = new ToolbarIndicator("", TOOLTIP_LATITUDE);
        indicatorLatitude.setValueFormat(FORMAT_LATITUDE);
        indicatorLatitude.setValueBackground(Color.BLACK);
        final JLabel labelLatitude = new JLabel(TOOLTIP_LATITUDE);
        labelLatitude.setForeground(getTextColour().getColor());
        labelLatitude.setFont(getLabelFont().getFont());
        getToolBar().add(labelLatitude);
        getToolBar().addSeparator(DIM_LABEL_SEPARATOR);
        getToolBar().add((Component)indicatorLatitude);
        getToolBar().addSeparator(DIM_TOOLBAR_SEPARATOR);

        // Grid Reference
        indicatorGridRef = new ToolbarIndicator(DIM_GRID_REFERENCE,
                                                "",
                                                TOOLTIP_GRID);
        indicatorGridRef.setValueFormat(FORMAT_GRID);
        indicatorGridRef.setValueBackground(Color.BLACK);
        final JLabel labelGrid = new JLabel(TOOLTIP_GRID);
        labelGrid.setForeground(getTextColour().getColor());
        labelGrid.setFont(getLabelFont().getFont());
        getToolBar().add(labelGrid);
        getToolBar().addSeparator(DIM_LABEL_SEPARATOR);
        getToolBar().add((Component)indicatorGridRef);
        getToolBar().addSeparator(DIM_TOOLBAR_SEPARATOR);

        // Create the toolbar buttons to control the POI (etc.)
        imageURL = getClass().getResource(ACTION_ICON_TOGGLE_POI);

        if (imageURL != null)
            {
            actionContext = new ContextAction(ACTION_TOGGLE_POI,
                                              new ImageIcon(imageURL),
                                              TOOLTIP_TOGGLE_POI,
                                              KeyEvent.VK_P,
                                              false,
                                              true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // Toggle the PointsOfInterest
                    boolShowPOI = !boolShowPOI;
                    labelMap.repaint();
                    }
                };

            // Add the Toolbar button
            buttonPOI = new JButton();
            buttonPOI.setBorder(BORDER_BUTTON);
            buttonPOI.setAction(actionContext);
            buttonPOI.setText("");
            buttonPOI.setToolTipText((String)actionContext.getValue(Action.SHORT_DESCRIPTION));
            getToolBar().add(buttonPOI);
            }

        // Now create the main panel, and the Map JLabel
        panelMap = new JPanel();
        panelMap.setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        labelMap = new JLabel()
            {
            /***************************************************************************************
             * Render the Map, and the list of PointsOfInterest.
             *
             * @param graphics
             */

            public void paint(final Graphics graphics)
                {
                super.paint(graphics);

                if ((getPOIs() != null)
                    && (!getPOIs().isEmpty()))
                    {
                    final Iterator<PointOfInterest> iterPOI;
                    PointOfInterest mappedPOI;

                    // Enable the POI button only if there are some POI
                    buttonPOI.setEnabled(true);

                    // Now show the POI if it is the right time
                    if (boolShowPOI)
                        {
                        iterPOI = getPOIs().iterator();

                        while (iterPOI.hasNext())
                            {
                            final PointOfInterest poi = iterPOI.next();

                            if (poi != null)
                                {
                                // TODO FIX ME!!!!!!!!!!!!!!!
//                                if (mappedPOI.getPOIIcon() != null)
//                                    {
//                                    graphics.drawImage(mappedPOI.getPOIIcon().getImageData(),
//                                                       mappedPOI.getX(),
//                                                       mappedPOI.getY(),
//                                                       labelMap);
//                                    }
                                }
                            }
                        }
                    }
                else
                    {
                    // Disable the POI button if there's nothing to show
                    buttonPOI.setEnabled(false);
                    }
                }
            };

        labelMap.setOpaque(false);
        labelMap.setIcon(RegistryModelUtilities.getAtomIcon(getFramework(), filename));
        panelMap.add(labelMap);

        setDoDrag(false);

        // Save a reference to the Map Icon
        setMapIcon(labelMap.getIcon());

        // Handle Mouse clicks
        labelMap.addMouseListener(new MouseAdapter()
            {
            /***************************************************************************************
             * Handle MouseEntered events.
             *
             * @param mouseEvent
             */

            public void mouseEntered(final MouseEvent mouseEvent)
                {
                labelMap.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

                try
                    {
                    clearIndicators(Color.BLACK);
                    }

                catch (IndicatorException exception)
                    {
                    System.out.println("IndicatorException=" + exception);
                    }
                }


            /***************************************************************************************
             * Handle MouseExited events.
             *
             * @param mouseEvent
             */

            public void mouseExited(final MouseEvent mouseEvent)
                {
                labelMap.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                try
                    {
                    clearIndicators(Color.BLACK);
                    }

                catch (IndicatorException exception)
                    {
                    System.out.println("IndicatorException=" + exception);
                    }
                }


            /***************************************************************************************
             * Handle MousePressed events.
             *
             * @param mouseEvent
             */

            public void mousePressed(final MouseEvent mouseEvent)
                {
//                LOGGER.debug("mouse pressed at x=" + mouseEvent.getX() + "  y=" + mouseEvent.getY());
//                labelMap.setCursor(new Cursor(Cursor.MOVE_CURSOR));
//                setStartPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
//                setDoDrag(true);

                }


            /***************************************************************************************
             * Handle MouseReleased events.
             *
             * @param mouseEvent
             */

            public void mouseReleased(final MouseEvent mouseEvent)
                {
//                LOGGER.debug("mouse released at x=" + mouseEvent.getX() + "  y=" + mouseEvent.getY());
//                labelMap.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//                setDoDrag(false);
                }
            });

        // Handle Mouse drags
        labelMap.addMouseMotionListener(new MouseMotionListener()
            {
            /***************************************************************************************
             * Handle MouseDragged events.
             *
             * @param mouseEvent
             */

            public void mouseDragged(final MouseEvent mouseEvent)
                {
//                if (getDoDrag())
                if (false)
                    {
                    int intX;
                    int intY;
                    final int intDeltaX;
                    final int intDeltaY;
                    final int intViewportX;
                    final int intViewportY;
                    final int intViewportWidth;
                    final int intViewportHeight;

                    try
                        {
                        clearIndicators(getCanvasColour().getColor());
                        }

                    catch (IndicatorException exception)
                        {
                        System.out.println("IndicatorException=" + exception);
                        }

                    intViewportX = scrollPaneMap.getViewport().getX();
                    intViewportY = scrollPaneMap.getViewport().getY();
                    intViewportWidth = scrollPaneMap.getViewport().getWidth();
                    intViewportHeight = scrollPaneMap.getViewport().getHeight();

//                    System.out.println("viewport width=" + scrollPaneMap.getScreenViewport().getWidth());
//                    System.out.println("viewport height=" + scrollPaneMap.getScreenViewport().getHeight());
//                    System.out.println("mouse dragging at x=" + mouseEvent.getX() + "  y=" + mouseEvent.getY());
//                    System.out.println("(long, lat)  ("
//                                       + (getTopLeft().getX() + (mouseEvent.getX() * getScaleX()))
//                                       + ", "
//                                       + (getTopLeft().getY() - (mouseEvent.getY() * getScaleY()))
//                                       + ")" );

                    intDeltaX = (int)(mouseEvent.getX() - getStartPoint().getX());
//                    System.out.println("mouse delta x=" + intDeltaX);

                    intDeltaY = (int)(mouseEvent.getY() - getStartPoint().getY());
//                    System.out.println("mouse delta y=" + intDeltaY);


                    intX = intViewportX + intDeltaX;

                    // The new rectangle cannot extend beyond the size of the underlying map
                    if (intX < intViewportX)
                        {
                        // Moving left, so see if we have hit the left edge
                        if (intX < 0)
                            {
                            intX = 0;
                            }
                        }
                    else
                        {
                        // Moving right, so check the right edge
                        if ((intX + intViewportWidth) > getMapIcon().getIconWidth())
                            {
                            intX = getMapIcon().getIconWidth() - intViewportWidth;
                            }
                        }

                    intY = intViewportY + intDeltaY;

                    if (intY < intViewportY)
                        {
                        // Moving up, so see if we have hit the top edge
                        if (intY < 0)
                            {
                            intY = 0;
                            }
                        }
                    else
                        {
                        // Moving down, so check the bottom edge
                        if ((intY + intViewportHeight) > getMapIcon().getIconHeight())
                            {
                            intY = getMapIcon().getIconHeight() - intViewportHeight;
                            }
                        }

                    labelMap.scrollRectToVisible(new Rectangle(intX,
                                                               intY,
                                                               intViewportWidth,
                                                               intViewportHeight));

//                    setStartPoint(getNextPoint());
//                    setStartPoint(new Point(intViewportX + intDeltaX,
//                                            intViewportY + intDeltaY));

                    }
                }

            /***************************************************************************************
             * Handle MouseMoved events.
             *
             * @param event
             */

            public void mouseMoved(final MouseEvent event)
                {
                try
                    {
                    if ((isScaleValid())
                        && (indicatorLongitude != null)
                        && (indicatorLatitude != null))
                        {
                        final DegMinSecInterface dmsLongitude;
                        final DegMinSecInterface dmsLatitude;
                        final OSGBGridReference gridReference;
                        final LatitudeLongitude latLong;
                        final String strGridReference;

                        dmsLongitude = new LongitudeDataType(getTopLeft().getX() + (getScaleX()*event.getX()));
                        dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
                        dmsLatitude = new LatitudeDataType(getTopLeft().getY() - (getScaleY()*event.getY()));
                        dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

                        indicatorLongitude.setValue(dmsLongitude.toString());
                        indicatorLatitude.setValue(dmsLatitude.toString());

                        if (indicatorGridRef != null)
                            {
                            latLong = new LatitudeLongitude(dmsLatitude.toDouble(), dmsLongitude.toDouble());
                            gridReference = GridReferenceConverter.convertLatLongToGridRef(getHostCountry(), latLong);
                            strGridReference = (int)(gridReference.getEasting()) + SPACE + (int)(gridReference.getNorthing());

                            indicatorGridRef.setValue(strGridReference);
                            }
                        }
                    }

                catch (DegMinSecException exception)
                    {
                    System.out.println("DegMinSecException=" + exception);
                    }

                catch (IndicatorException exception)
                    {
                    System.out.println("IndicatorException=" + exception);
                    }
                }
            });

        // Create the JScrollPane for the Map
        scrollPaneMap = new JScrollPane();
        scrollPaneMap.setViewportView(panelMap);
        scrollPaneMap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneMap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneMap.setWheelScrollingEnabled(true);

        // Put all the components together
        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());
        panelUI.add(getToolBar(), BorderLayout.NORTH);
        panelUI.add(scrollPaneMap, BorderLayout.CENTER);

        return (panelUI);
        }


    /***********************************************************************************************
     * Recalculate the Map Scale factors.
     * Each Scale Factor is in degrees per pixel.
     * Note that this will not work for maps crossing the Date Line!
     *
     * @return boolean
     */

    private boolean recalculateScale()
        {
        final double dblLongitudeRange;
        final double dblLatitudeRange;
        final int intXRange;
        final int intYRange;

        if ((getTopLeft() != null)
            && (getBottomRight() != null)
            && (getMapIcon() != null))
            {
            //LOGGER.debug("top left     (long, lat) = (" + getTopLeft().getX() + "," + + getTopLeft().getY() + ")");
            //LOGGER.debug("bottom right (long, lat) = (" + getBottomRight().getX() + "," + + getBottomRight().getY() + ")");

            if ((getTopLeft().getX() < getBottomRight().getX())
                && (getTopLeft().getX() >= -180.0)
                && (getTopLeft().getX() <= 180.0)
                && (getBottomRight().getX() >= -180.0)
                && (getBottomRight().getX() <= 180.0))
                {
                dblLongitudeRange = getBottomRight().getX() - getTopLeft().getX();
                //LOGGER.debug("long range = " + dblLongitudeRange);

                if ((dblLongitudeRange > 0.0)
                    && (getTopLeft().getY() > getBottomRight().getY())
                    && (getTopLeft().getY() <= 90.0)
                    && (getTopLeft().getY() >= -90.0)
                    && (getBottomRight().getY() <= 90.0)
                    && (getBottomRight().getY() >= -90.0))
                    {
                    dblLatitudeRange = getTopLeft().getY() - getBottomRight().getY();
                    //LOGGER.debug("lat range = " + dblLatitudeRange);

                    // Get the pixel ranges
                    intXRange = getMapIcon().getIconWidth();
                    intYRange = getMapIcon().getIconHeight();
                    //LOGGER.debug("x range = " + intXRange);
                    //LOGGER.debug("y range = " + intYRange);

                    if ((dblLatitudeRange > 0.0)
                        && (intXRange > 0)
                        && (intYRange > 0))
                        {
                        // Set the scale factors
                        setScaleX(dblLongitudeRange/intXRange);
                        setScaleY(dblLatitudeRange/intYRange);

                        //LOGGER.debug("x scale = " + getScaleX());
                        //LOGGER.debug("y scale = " + getScaleY());

                        return (true);
                        }
                    }
                }
            }

        setScaleX(1.0);
        setScaleY(1.0);

        return (false);
        }


    /***********************************************************************************************
     * Redisplay the Map centred on the specified (Long, Lat).
     *
     * @param longitude
     * @param latitude
     */

    public final void centreLongLat(final DegMinSecInterface longitude,
                                    final DegMinSecInterface latitude)
        {
        final int intTargetX;
        final int intTargetY;
        final int intWidth;
        final int intHeight;
        int intTopLeftX;
        int intTopLeftY;

        if ((longitude != null)
            && (latitude != null))
            {
            // Map the (Long, Lat) into pixels (x,y)
            intTargetX = (int)((longitude.toDouble() - getTopLeft().getX()) / getScaleX());
            intTargetY = (int)((getTopLeft().getY() - latitude.toDouble()) / getScaleY());

            // Find the current viewport size
            intWidth = scrollPaneMap.getViewport().getWidth();
            intHeight = scrollPaneMap.getViewport().getHeight();

            // The TopLeftX cannot be less than 0
            intTopLeftX = Math.max((intTargetX - (intWidth >> 1)), 0);

            // The TopLeftX cannot be greater than the Map IconWidth less the width of the viewport
            intTopLeftX = Math.min((getMapIcon().getIconWidth() - intWidth), intTopLeftX);

            // The TopLeftY cannot be less than 0
            intTopLeftY = Math.max((intTargetY - (intHeight >> 1)), 0);

            // The TopLeftY cannot be greater than the Map IconHeight less the height of the viewport
            intTopLeftY = Math.min((getMapIcon().getIconHeight() - intHeight), intTopLeftY);

    //        System.out.println("centre at " + longitude.toString() + ", " + latitude.toString());
    //        System.out.println("intTargetX=" + intTargetX);
    //        System.out.println("intTargetY=" + intTargetY);
    //        System.out.println("intWidth=" + intWidth);
    //        System.out.println("intHeight=" + intHeight);
    //        System.out.println("intTopLeftX=" + intTopLeftX);
    //        System.out.println("intTopLeftY=" + intTopLeftY);

            labelMap.scrollRectToVisible(new Rectangle(intTopLeftX,
                                                       intTopLeftY,
                                                       intWidth,
                                                       intHeight));
            repaint();
            }
        }


    /***********************************************************************************************
     * Redisplay the Map centred on the specified Pixel (x, y).
     *
     * @param x
     * @param y
     */

    public final void centrePixel(final int x,
                                  final int y)
        {
        final int intWidth;
        final int intHeight;
        int intTopLeftX;
        int intTopLeftY;

        // Find the current viewport size
        intWidth = scrollPaneMap.getViewport().getWidth();
        intHeight = scrollPaneMap.getViewport().getHeight();

        // The TopLeftX cannot be less than 0
        intTopLeftX = Math.max((x - (intWidth >> 1)), 0);

        // The TopLeftX cannot be greater than the Map IconWidth less the width of the viewport
        intTopLeftX = Math.min((getMapIcon().getIconWidth() - intWidth), intTopLeftX);

        // The TopLeftY cannot be less than 0
        intTopLeftY = Math.max((y - (intHeight >> 1)), 0);

        // The TopLeftY cannot be greater than the Map IconHeight less the height of the viewport
        intTopLeftY = Math.min((getMapIcon().getIconHeight() - intHeight), intTopLeftY);

        labelMap.scrollRectToVisible(new Rectangle(intTopLeftX,
                                                   intTopLeftY,
                                                   intWidth,
                                                   intHeight));
        }


    /***********************************************************************************************
     * Clear all Indicators, and set the background colour to that specified.
     *
     * @param background
     *
     * @throws IndicatorException
     */

    private void clearIndicators(final Color background) throws IndicatorException
        {
        if ((indicatorLongitude != null)
            && (indicatorLatitude != null)
            && (indicatorGridRef != null))
            {
            indicatorLongitude.setValue("");
            indicatorLongitude.setValueBackground(background);
            indicatorLatitude.setValue("");
            indicatorLatitude.setValueBackground(background);
            indicatorGridRef.setValue("");
            indicatorGridRef.setValueBackground(background);
            }
        }


    /***********************************************************************************************
     * Add a PointOfInterest to the Map.
     *
     * @param poi
     */

    public final void addPointOfInterest(final PointOfInterest poi)
        {
        final ImageIcon iconPOI;
        int intXPOI;
        int intYPOI;

        if ((poi != null)
            && (getPOIs() != null))
            {
            iconPOI = RegistryModelUtilities.getAtomIcon(pluginFramework,
                                                         poi.getIconFilename());

            // Map the (Long, Lat) into pixels (x,y)
//            intXPOI = (int)((poi.getLongitude().toDouble() - getTopLeft().getX()) / getScaleX());
//            intYPOI = (int)((getTopLeft().getY() - poi.getLatitude().toDouble()) / getScaleY());

            // Adjust (x,y) for the size of the Icon, to point to the top left corner
//            intXPOI -= (iconPOI.getIconWidth() >> 1);
//            intYPOI -= (iconPOI.getIconHeight() >> 1);

            // Add the MappedPointOfInterest to the list to be rendered
            // TODO FIX ME!!!! getPOIs().add(new MappedPointOfInterest(intXPOI, intYPOI, iconPOI));
            }
        }


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Map.
     */

    private void clearPointsOfInterest()
        {
        if (getPOIs() != null)
            {
            getPOIs().clear();
            }
        }


    /***********************************************************************************************
     * Get the host Country.
     * This is used to select the correct National Grid.
     *
     * @return CountryData
     */

    private CountryPlugin getHostCountry()
        {
        return (this.countryHost);
        }


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    private List<PointOfInterest> getPOIs()
        {
        return (this.listPOI);
        }


    /***********************************************************************************************
     * Get the Map JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the Map JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the Map TopLeft coordinates.
     *
     * @return Point2D.Double
     */

    private Point2D.Double getTopLeft()
        {
        return (this.pointTopLeft);
        }


    /***********************************************************************************************
     * Set the Map TopLeft coordinates.
     *
     * @param topleft
     */

    private void setTopLeft(final Point2D.Double topleft)
        {
        this.pointTopLeft = topleft;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the Map BottomRight coordinates.
     *
     * @return Point2D.Double
     */

    private Point2D.Double getBottomRight()
        {
        return (this.pointBottomRight);
        }


    /***********************************************************************************************
     * Set the Map BottomRight coordinates.
     *
     * @param bottomright
     */

    private void setBottomRight(final Point2D.Double bottomright)
        {
        this.pointBottomRight = bottomright;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the Map Icon.
     *
     * @return Icon
     */

    private Icon getMapIcon()
        {
        return (this.iconMap);
        }


    /***********************************************************************************************
     * Set the Map Icon.
     *
     * @param map
     */

    private void setMapIcon(final Icon map)
        {
        this.iconMap = map;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the X-axis Scale Factor in degrees per pixel.
     *
     * @return double
     */

    private double getScaleX()
        {
        return (this.dblScaleX);
        }


    /***********************************************************************************************
     * Set the X-axis Scale Factor in degrees per pixel.
     *
     * @param scale
     */

    private void setScaleX(final double scale)
        {
        this.dblScaleX = scale;
        }


    /***********************************************************************************************
     * Get the Y-axis Scale Factor in degrees per pixel.
     *
     * @return double
     */

    private double getScaleY()
        {
        return (this.dblScaleY);
        }


    /***********************************************************************************************
     * Set the Y-axis Scale Factor in degrees per pixel.
     *
     * @param scale
     */

    private void setScaleY(final double scale)
        {
        this.dblScaleY = scale;
        }


    /***********************************************************************************************
     * Get a flag indicating if the Scale Factors are valid.
     *
     * @return boolean
     */

    private boolean isScaleValid()
        {
        return (this.boolScaleValid);
        }


    /***********************************************************************************************
     * Set a flag indicating if the Scale Factors are valid.
     *
     * @param valid
     */

    private void setScaleValid(final boolean valid)
        {
        this.boolScaleValid = valid;
        }


    /***********************************************************************************************
     * Get a flag indicating if the mouse is being dragged.
     *
     * @return boolean
     */

    private boolean getDoDrag()
        {
        return (this.boolDoDrag);
        }


    /***********************************************************************************************
     * Set a flag indicating if the mouse is being dragged.
     *
     * @param drag
     */

    private void setDoDrag(final boolean drag)
        {
        this.boolDoDrag = drag;
        }


    /***********************************************************************************************
     * Get the mouse drag start Point.
     *
     * @return Point
     */

    private Point getStartPoint()
        {
        return pointStartDrag;
        }


    /***********************************************************************************************
     * Get the mouse drag start Point.
     *
     * @param point
     */

    private void setStartPoint(final Point point)
        {
        this.pointStartDrag = point;
        }


    /***********************************************************************************************
     * Dispose of all the Map resources.
     */

    public final void disposeUI()
        {
        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Get the host Framework.
     *
     * @return FrameworkPlugin
     */

    private FrameworkPlugin getFramework()
        {
        return (this.pluginFramework);
        }
    }
