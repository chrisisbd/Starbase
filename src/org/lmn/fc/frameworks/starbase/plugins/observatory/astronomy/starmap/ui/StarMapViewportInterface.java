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

import org.lmn.fc.common.constants.*;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * StarMapViewportInterface.
 */

public interface StarMapViewportInterface extends FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ResourceKeys
    {
    /***********************************************************************************************
     * Set the Azimuth and Elevation range of the display map.
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
     * Find out the current size of the window, for coordinate conversions.
     * Remove the area occupied by the scales, if enabled.
     * Map drawing uses (pointTopLeft, pointBottomRight).
     * Azimuth Scale uses (pointAziScaleTopLeft, pointAziScaleBottomRight).
     * Elevation scale uses (pointElevScaleTopLeft, pointElevScaleBottomRight).
     *
     * @param component
     */

    void updatePixelViewportAndRemoveScales(JComponent component);


    /***********************************************************************************************
     * Find out the current size of the window, for coordinate conversions.
     * Remove the area occupied by the scales, if enabled.
     * Used for Export.
     *
     * @param width
     * @param height
     * @param insets
     */

    void updatePixelViewportAndRemoveScales(int width,
                                            int height,
                                            Insets insets);


    /***********************************************************************************************
     * Zoom in the requested direction centred at the specified Point(x, y) if possible.
     *
     * @param zoompoint
     * @param zoom
     */

    void zoomViewport(Point zoompoint, int zoom);

    double getZoomFactor();

    void setZoomFactor(double zoom);


    double getAzimuthEast();

    double getAzimuthWest();

    double getElevationSouth();

    double getElevationNorth();


    Point getAziScaleTopLeft();

    Point getAziScaleBottomRight();

    Point getElevScaleTopLeft();

    Point getElevScaleBottomRight();


    /***********************************************************************************************
      * Get the location of the TopLeft of the Viewport.
      *
      * @return Point
      */

     Point getTopLeft();


     /***********************************************************************************************
      * Get the location of the BottomRight of the Viewport.
      *
      * @return Point
      */

     Point getBottomRight();


    /***********************************************************************************************
     * Get the number of pixels per degree, Horizontally.
     *
     * @return double
     */

    double getHorizPixelsPerDegree();


    /***********************************************************************************************
     * Get the number of pixels per degree, Vertically.
     *
     * @return double
     */

    double getVertPixelsPerDegree();


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

    boolean isScaleEnabled();


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
    }
