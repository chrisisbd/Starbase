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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins;

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;


/***************************************************************************************************
 * StarMapPointInterface.
 */

public interface StarMapPointInterface extends FrameworkStrings
    {
    boolean CLICKABLE = true;
    boolean NOTCLICKABLE = false;


    /***********************************************************************************************
     * Read the parent StarMapPlugin.
     *
     * @return StarMapPlugin
     */

    StarMapPlugin getHostPlugin();


    /***********************************************************************************************
     * Get the StarMapPoint ID.
     *
     * @return int
     */

    int getPointID();


    /***********************************************************************************************
     * Get the StarMapPoint Name.
     *
     * @return String
     */

    String getName();


    /***********************************************************************************************
     * Get the CoordinateType of the StarMapPoint.
     *
     * @return CoordinateType
     */

    CoordinateType getCoordinateType();


    /***********************************************************************************************
     * Set the CoordinateType of the StarMapPoint.
     *
     * @param type
     */

    void setCoordinateType(CoordinateType type);


    /***********************************************************************************************
     * Get the coordinates of the StarMapPoint.
     * (Az, El) (Ra, Dec) (l, b) and so on.
     *
     * @return Point2D.Double
     */

    Point2D.Double getCoordinates();


    /***********************************************************************************************
     * Set the coordinates of the StarMapPoint.
     * (Az, El) (Ra, Dec) (l, b) and so on.
     *
     * @param point
     */

    void setCoordinates(Point2D.Double point);


    /***********************************************************************************************
     * Get the Colour of the StarMapPoint.
     *
     * @return Color
     */

    Color getColour();


    /***********************************************************************************************
     * Set the Colour of the StarMapPoint.
     *
     * @param colour
     */

    void setColour(Color colour);


    /***********************************************************************************************
     * Get the clickable state.
     *
     * @return boolean
     */

    boolean isClickable();


    /***********************************************************************************************
     * Set the clickable state.
     *
     * @param clickable
     */

    void setClickable(boolean clickable);


    /***********************************************************************************************
     * Get the Metadata for this StarMapPoint.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadata();


    /***********************************************************************************************
     * Set the Metadata for this StarMapPoint.
     *
     * @param metadatalist
     */

    void setMetadata(List<Metadata> metadatalist);


    /***********************************************************************************************
     * Get the Viewport position (x, y) of the StarMapPoint.
     *
     * @return Point
     */

    Point2D.Double getViewportXY();


    /***********************************************************************************************
     * Set the Viewport position (x, y) of the StarMapPoint.
     *
     * @param pointxy
     */

    void setViewportXY(Point2D.Double pointxy);


    /***********************************************************************************************
     * Get the pixel position (x, y) of the StarMapPoint.
     *
     * @return Point
     */

    Point getPixelsXY();


    /***********************************************************************************************
     * Set the pixel position (x, y) of the StarMapPoint.
     *
     * @param pointxy
     */

    void setPixelsXY(Point pointxy);


    /***********************************************************************************************
     * See if the StarMapPoint is visible.
     *
     * @return boolean
     */

    boolean isVisible();


    /***********************************************************************************************
     * Control if the StarMapPoint is visible.
     *
     * @param visible
     */

    void setVisible(boolean visible);
    }
