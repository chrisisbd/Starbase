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

import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.trackables.Trackable;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Vector;


public interface StarMapUIComponentPlugin extends UIComponentPlugin
    {
    // String Resources
    String TOOLTIP_AZIMUTH = "Azimuth (degrees)";
    String TOOLTIP_ELEVATION = "Elevation (degrees)";
    String TOOLTIP_RA = "Right Ascension (hh:mm:ss)";
    String TOOLTIP_DEC = "Declination (degrees)";
    String TOOLTIP_GALACTIC_LONG = "Galactic Longitude (degrees)";
    String TOOLTIP_GALACTIC_LAT = "Galactic Latitude (degrees)";
    String TOOLTIP_JD = "Julian Day";
    String TOOLTIP_LONGITUDE = "Topocentric Longitude (degrees)";
    String TOOLTIP_LATITUDE = "Topocentric Latitude (degrees)";
    String TOOLTIP_HASL = "Height Above Sea Level (metres)";
    String TOOLTIP_CONTROL_SCALES = "Control the Scales";
    String ICON_TOOLBAR_SCALES = "toolbar-scales.png";
    String DIALOG_PRINT         = "Print the Star Map Viewer contents";
    String MSG_MAP_VIEWER = "the Star Map";
    String MSG_MAP_VIEWER_PRINTED = "The Star Map has been printed";

    // ToDo These defaults could be centralised?
    String DEFAULT_TIME = "00:00:00";
    String FORMAT_RA = "00:00:00";

    int AZIMUTH_NORTH = 0;
    int AZIMUTH_SOUTH = 1;
    int AZIMUTH_EXTENT = 360;
    int ELEVATION_EXTENT = 90;
    int ZOOM_EXTENTS = 0;
    int ZOOM_IN = 1;
    int ZOOM_OUT = 2;
    double ZOOM_AZIMUTH_MIN = 10.0;
    double ZOOM_ELEVATION_MIN = 2.5;
    int AZIMUTH_SCALE_HEIGHT = 18;
    int ELEVATION_SCALE_WIDTH = 18;

    Color COLOR_SCALE = new Color(5, 119, 167);
    Color COLOR_SCALE_BACKGROUND = new Color(249, 196, 105);
    Color COLOR_BACKGROUND = UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor();

    int REFRESH_SECONDS = 10;
    int SIDEBAR_HEIGHT_SEPARATOR = 15;

    Border LABEL_SPACER = BorderFactory.createEmptyBorder(0, 0, 0, 5);


    /***********************************************************************************************
     * Refresh the Map JPanel.
     *
     * @return JPanel
     */

    JPanel refreshStarMap();


    /***********************************************************************************************
     * Get the StarMapViewport.
     *
     * @return StarMapViewportInterface
     */

    StarMapViewportInterface getScreenViewport();


    /***********************************************************************************************
     * Set the StarMapViewport for the StarMap.
     *
     * @param viewport
     */

    void setScreenViewport(final StarMapViewportInterface viewport);


    /***********************************************************************************************
     * Set the Azimuth and Elevation range of the StarMap.
     *
     * @param azimutheast
     * @param azimuthwest
     * @param elevationsouth
     * @param elevationnorth
     */

    void setExtents(double azimutheast,
                    double azimuthwest,
                    double elevationsouth,
                    double elevationnorth);


    /***********************************************************************************************
     * Get the StarMap Azimuth origin.
     *
     * @return int
     */

    int getAzimuthOrigin();


    /***********************************************************************************************
     * Set the Azimuth Origin to NORTH or SOUTH.
     *
     * @param origin
     */

    void setAzimuthOrigin(int origin);


    /***********************************************************************************************
     * Control the Scales.
     *
     * @param enable
     */

    void enableScales(boolean enable);


    /***********************************************************************************************
     * Indicate if the scales are enabled.
     *
     * @return boolean
     */

    boolean areScalesEnabled();


    /***********************************************************************************************
     * Get the Trackable being tracked.
     *
     * @return Trackable
     */

    Trackable getTrackable();


    /***********************************************************************************************
     * Set the Trackable to be tracked.
     *
     * @param trackable
     */

    void setTrackable(Trackable trackable);


    /***********************************************************************************************
     * Get all Plugins attached to this StarMap, active or not.
     *
     * @return Vector<StarMapPlugin>
     */

    Vector<StarMapPlugin> getPlugins();


    /***********************************************************************************************
     * Add a StarMapPlugin to this StarMap.
     *
     * @param plugin
     */

    void addPlugin(StarMapPlugin plugin);


    /***********************************************************************************************
     * Remove a StarMapPlugin from this StarMap.
     *
     * @param plugin
     */

    void removePlugin(StarMapPlugin plugin);


    /***********************************************************************************************
     * Show all the Plugins attached to this StarMap, active or not.
     */

    void showPluginNames();


    /***********************************************************************************************
     * Get the width of the Elevation scale.
     *
     * @return int
     */

    int getElevationScaleWidth();


    /***********************************************************************************************
     * Get the height of the Azimuth scale.
     *
     * @return int
     */

    int getAzimuthScaleHeight();


    /***********************************************************************************************
     * Get the ExportableComponent which may be exported.
     *
     * @return JComponent
     */

    ExportableComponentInterface getExportableComponent();


    /***********************************************************************************************
     * Get the array of points which may be clicked.
     *
     * @return StarMapPointInterface[][]
     */

    StarMapPointInterface[][] getClickablePoints();


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getHostInstrument();
    }
