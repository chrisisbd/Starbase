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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * MapUIComponentPlugin.
 */

public interface MapUIComponentPlugin extends UIComponentPlugin
    {
    // String Resources
    String FILENAME_MAP_IMPORT = InstallationFolder.MAPS.getName() + "/coastline.dat";
    String FILENAME_MAP_EXPORT = InstallationFolder.MAPS.getName() + "/coastline";
    String FILENAME_TOOLBAR_TOGGLE_POI = "toolbar-toggle-poi.png";
    String FILENAME_TOOLBAR_EXPORT_COASTLINE = "toolbar-export-map.png";
    String TOOLTIP_LONGITUDE   = "Longitude";
    String TOOLTIP_LATITUDE    = "Latitude";
    String TOOLTIP_GRID        = "Grid Reference";
    String TOOLTIP_POI_IMPORT  = "Import POI";
    String TOOLTIP_POI_REMOVE  = "Remove POI";
    String TOOLTIP_TOGGLE_POI  = "Toggle Points of Interest";
    String TOOLTIP_EXPORT_MAP  = "Create a new Map from " + FILENAME_MAP_IMPORT;
    String MSG_CREATE_COASTLINE_MAP = "Are you sure that you wish to create a new map?";
    String ACTION_TOGGLE_POI   = TOOLTIP_TOGGLE_POI;
    String ACTION_EXPORT_MAP   = TOOLTIP_EXPORT_MAP;
    String ERROR_PARSE_POI = "DegMinSecException - could not parse PointOfInterest";
    String ERROR_PARSE_LOI = "DegMinSecException - could not parse LineOfInterest";
    String ERROR_CREATE_MAP = "MapUIComponent.initialiseToolbar() Unable to create Map image";
    String ERROR_INDICATOR = "IndicatorException=";
    String ERROR_DMS = "DegMinSecException=";
    String FORMAT_LONGITUDE    = "00:00:00E";
    String FORMAT_LATITUDE     = "00:00:00N";
    String FORMAT_GRID         = "000000 000000";
    String DIALOG_PRINT         = "Print the Map Viewer contents";
    String MSG_MAP_VIEWER = "the Map Viewer";
    String MSG_MAP_VIEWER_PRINTED = "The Map Viewer has been printed";

    boolean HAS_GRID_REFERENCE = false;
    Dimension DIM_LONGITUDE = new Dimension(100, 20);
    Dimension DIM_LATITUDE = new Dimension(80, 20);
    Dimension DIM_GRID_REFERENCE = new Dimension(110, 20);

    Color COLOR_BACKGROUND = DEFAULT_COLOUR_CANVAS.getColor();
    Color COLOR_MAP_LAND = new Color(55, 185, 98);
    int HEIGHT_MAP_EXPORT = 750;
    int SIDEBAR_HEIGHT_SEPARATOR = 15;
    Border LABEL_SPACER = BorderFactory.createEmptyBorder(0, 0, 0, 5);


    /***********************************************************************************************
     * Clear the map  and invalidate the scale factor.
     *
     * @throws IndicatorException
     */

    //void clearMap() throws IndicatorException;


    /***********************************************************************************************
     * Redisplay the Map centred on the specified (Long, Lat).
     *
     * @param longitude
     * @param latitude
     */

    void centreLongLat(DegMinSecInterface longitude,
                       DegMinSecInterface latitude);


    /***********************************************************************************************
     * Redisplay the Map centred on the specified Pixel (x, y).
     *
     * @param x
     * @param y
     */

    void centrePixel(int x,
                     int y);


    /***********************************************************************************************
     * Add a PointOfInterest to the Map.
     *
     * @param poi
     */

    void addPointOfInterest(PointOfInterest poi);


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Map.
     */

    void clearPointsOfInterest();


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    List<PointOfInterest> getPointOfInterestList();


    /***********************************************************************************************
     * Add a LineOfInterest to the Map.
     *
     * @param loi
     */

    void addLineOfInterest(LineOfInterest loi);


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Map.
     */

    void clearLinesOfInterest();


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    List<LineOfInterest> getLineOfInterestList();


    /***********************************************************************************************
     * Reload all PointsOfInterest and all LinesOfInterest.
     */

    void collectPOIandLOI();


    /***********************************************************************************************
     * Get the ExportableComponent which may be exported.
     *
     * @return JComponent
     */

    ExportableComponentInterface getExportableComponent();
    }
