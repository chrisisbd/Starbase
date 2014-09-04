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
// StarMapPoint
//------------------------------------------------------------------------------
// Revision History
//
//  03-08-02    LMN created file
//  11-08-02    LMN added SubType
//  12-08-02    LMN added pointXY & Clickable
//  14-08-02    LMN added Colour
//  19-08-02    LMN added second constructor
//  04-10-02    LMN rewrote to use StarMapPlugin
//  14-09-06    LMN changed for Xml-based Metadata
//
//------------------------------------------------------------------------------
// Astronomy package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl;


import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;


/***************************************************************************************************
 * StarMapPoint.
 */

public final class StarMapPoint implements StarMapPointInterface
    {
    // Injections
    private final StarMapPlugin hostStarMap;        // The parent StarMapPlugin
    private final int intPointID;                   // The StarMapPoint's ID
    private final String strName;
    private CoordinateType coordinateType;
    private Point2D.Double pointCoords;             // The original coordinates of CoordinateType
    private Color colorObject;                      // The colour in which to paint the object
    private boolean boolClickable;                  // Indicates if the object is clickable
    private List<Metadata> listMetadata;

    private Point2D.Double pointViewportXY;         // The transformed Viewport position (x, y)
    private Point pointPixelsXY;                    // The transformed pixel position (x, y) (i.e. integer)
    private boolean boolVisible;                    // Indicates if the object is visible


    /***********************************************************************************************
     * Create a StarMapPoint with all parameters supplied except (x, y).
     * (x, y) is always updated before display, so there is no need to supply it.
     * The colour is a separate parameter so that it can be independent of the plugin's colour.
     *
     * @param hostplugin
     * @param pointid
     * @param name
     * @param coordinatetype
     * @param coordinates
     * @param colour
     * @param clickable
     * @param metadatalist
     */

    public StarMapPoint(final StarMapPlugin hostplugin,
                        final int pointid,
                        final String name,
                        final CoordinateType coordinatetype,
                        final Point2D.Double coordinates,
                        final Color colour,
                        final boolean clickable,
                        final List<Metadata> metadatalist)
        {
        // Injections
        this.hostStarMap = hostplugin;
        this.intPointID = pointid;
        this.strName = name;
        this.coordinateType = coordinatetype;
        this.pointCoords = coordinates;
        this.colorObject = colour;
        this.boolClickable = clickable;
        this.listMetadata = metadatalist;

        // Set later by transformCoordinates()
        this.pointViewportXY = new Point2D.Double(0.0, 0.0);
        this.pointPixelsXY = new Point(0, 0);
        this.boolVisible = false;
        }


    /***********************************************************************************************
     * Create a StarMapPoint from another StarMapPoint.
     * This is effectively clone().
     *
     * @param point
     */

    public StarMapPoint(final StarMapPointInterface point)
        {
        // Injections
        this.hostStarMap = point.getHostPlugin();
        this.intPointID = point.getPointID();
        this.strName = point.getName();
        this.coordinateType = point.getCoordinateType();
        this.pointCoords = point.getCoordinates();
        this.colorObject = point.getColour();
        this.boolClickable = point.isClickable();
        this.listMetadata = point.getMetadata();

        this.pointViewportXY = point.getViewportXY();
        this.pointPixelsXY = point.getPixelsXY();
        this.boolVisible = point.isVisible();
        }


    /***********************************************************************************************
     * Read the parent StarMapPlugin.
     *
     * @return StarMapPlugin
     */

    public final StarMapPlugin getHostPlugin()
        {
        return(this.hostStarMap);
        }


    /***********************************************************************************************
     * Get the StarMapPoint ID.
     *
     * @return int
     */

    public final int getPointID()
        {
        return(this.intPointID);
        }


    /***********************************************************************************************
     * Get the StarMapPoint Name.
     *
     * @return String
     */

    public final String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the CoordinateType of the StarMapPoint.
     *
     * @return CoordinateType
     */

    public final CoordinateType getCoordinateType()
        {
        return(this.coordinateType);
        }


    /***********************************************************************************************
     * Set the CoordinateType of the StarMapPoint.
     *
     * @param type
     */

    public final void setCoordinateType(final CoordinateType type)
        {
        this.coordinateType = type;
        }


    /***********************************************************************************************
     * Get the coordinates of the StarMapPoint.
     * (Az, El) (Ra, Dec) (l, b) and so on.
     *
     * @return Point2D.Double
     */

    public final Point2D.Double getCoordinates()
        {
        return(this.pointCoords);
        }


    /***********************************************************************************************
     * Set the coordinates of the StarMapPoint.
     * (Az, El) (Ra, Dec) (l, b) and so on.
     *
     * @param point
     */

    public final void setCoordinates(final Point2D.Double point)
        {
        this.pointCoords = point;
        }


    /***********************************************************************************************
     * Get the Colour of the StarMapPoint.
     *
     * @return Color
     */

    public final Color getColour()
        {
        return(this.colorObject);
        }


    /***********************************************************************************************
     * Set the Colour of the StarMapPoint.
     *
     * @param colour
     */

    public final void setColour(final Color colour)
        {
        this.colorObject = colour;
        }


    /***********************************************************************************************
     * Get the clickable state.
     *
     * @return boolean
     */

    public final boolean isClickable()
        {
        return(this.boolClickable);
        }


    /***********************************************************************************************
     * Set the clickable state.
     *
     * @param clickable
     */

    public void setClickable(final boolean clickable)
        {
        this.boolClickable = clickable;
        }


    /***********************************************************************************************
     * Get the Metadata for this StarMapPoint.
     *
     * @return List<Metadata>
     */

    public final List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the Metadata for this StarMapPoint.
     *
     * @param metadatalist
     */

    public final void setMetadata(final List<Metadata> metadatalist)
        {
        this.listMetadata = metadatalist;
        }


    /***********************************************************************************************
     * Get the Viewport position (x, y) of the StarMapPoint.
     *
     * @return Point2D.Double
     */

    public final Point2D.Double getViewportXY()
        {
        return(this.pointViewportXY);
        }


    /***********************************************************************************************
     * Set the Viewport position (x, y) of the StarMapPoint.
     *
     * @param pointxy
     */

    public final void setViewportXY(final Point2D.Double pointxy)
        {
        this.pointViewportXY = pointxy;
        }


    /***********************************************************************************************
     * Get the pixel position (x, y) of the StarMapPoint.
     *
     * @return Point
     */

    public final Point getPixelsXY()
        {
        return(this.pointPixelsXY);
        }


    /***********************************************************************************************
     * Set the pixel position (x, y) of the StarMapPoint.
     *
     * @param pointxy
     */

    public final void setPixelsXY(final Point pointxy)
        {
        this.pointPixelsXY = pointxy;
        }


    /***********************************************************************************************
     * See if the StarMapPoint is visible.
     *
     * @return boolean
     */

    public final boolean isVisible()
        {
        return(this.boolVisible);
        }


    /***********************************************************************************************
     * Control if the StarMapPoint is visible.
     *
     * @param visible
     */

    public final void setVisible(final boolean visible)
        {
        this.boolVisible = visible;
        }
    }
