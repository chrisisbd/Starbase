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

package org.lmn.fc.common.coastline.impl;

import org.lmn.fc.common.coastline.CoastlineSegmentInterface;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// The original Coastline file contains segments:
//        # -b
//        -6.450276	49.872593
//        -6.450570	49.873180
//        -6.449983	49.873767
//        -6.447636	49.873767
//        -6.447049	49.873180
//        -6.447049	49.872593
//        -6.447636	49.872007
//        -6.449983	49.872007
//        -6.450276	49.872593

/***************************************************************************************************
 * CoastlineSegment.
 */

public final class CoastlineSegment implements CoastlineSegmentInterface,
                                               FrameworkStrings,
                                               FrameworkSingletons
    {
    private final List<Point2D.Double> listPoints;

    // The maximum extents of the Coastline map defined in degrees
    private double dblExtentEast;
    private double dblExtentWest;
    private double dblExtentSouth;
    private double dblExtentNorth;


    /***********************************************************************************************
     * Construct a CoastlineSegment.
     *
     * WEST is NEGATIVE.
     */

    public CoastlineSegment()
        {
        this.listPoints = new ArrayList<Point2D.Double>(50);

        // Set extents which must change
        this.dblExtentEast = CoordinateConversions.LONGITUDE_RANGE_MIN;
        this.dblExtentWest = CoordinateConversions.LONGITUDE_RANGE_MAX;
        this.dblExtentSouth = CoordinateConversions.LATITUDE_RANGE_MAX;
        this.dblExtentNorth = CoordinateConversions.LATITUDE_RANGE_MIN;
        }


    /***********************************************************************************************
     * Add a point to the coastline segment.
     * WEST is NEGATIVE.
     *
     * @param longitude
     * @param latitude
     */

    public void addPoint(final double longitude,
                         final double latitude)
        {
        if (getPoints() != null)
            {
            getPoints().add(new Point2D.Double(longitude, latitude));

            // Update the extents
            if (longitude < getWestExtent())
                {
                setWestExtent(longitude);
                }
            else if (longitude > getEastExtent())
                {
                setEastExtent(longitude);
                }

            if (latitude < getSouthExtent())
                {
                setSouthExtent(latitude);
                }
            else if (latitude > getNorthExtent())
                {
                setNorthExtent(latitude);
                }
            }
        }


    /***********************************************************************************************
     * Show the points in the segment, for debugging.
     */

    public void showSegment()
        {
        LOGGER.log("CoastlineSegment");

        if (getPoints() != null)
            {
            final Iterator<Point2D.Double> iterPoints;

            iterPoints = getPoints().iterator();

            while (iterPoints.hasNext())
                {
                final Point2D.Double point;

                point = iterPoints.next();
                LOGGER.log(INDENT + point.getX() + SPACE + point.getY());
                }
            }
        }


    /***********************************************************************************************
     * Get the points in the CoastlineSegment.
     *
     * @return List<Point2D.Double>
     */

    public List<Point2D.Double> getPoints()
        {
        return (this.listPoints);
        }


    /***********************************************************************************************
     * Get the EastExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    public double getEastExtent()
        {
        return (this.dblExtentEast);
        }


    /***********************************************************************************************
     * Set the EastExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    private void setEastExtent(final double extent)
        {
        this.dblExtentEast = extent;
        }


    /***********************************************************************************************
     * Get the WestExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    public double getWestExtent()
        {
        return (this.dblExtentWest);
        }


    /***********************************************************************************************
     * Set the WestExtent.
     * WEST is NEGATIVE.
     *
     * @return double
     */

    private void setWestExtent(final double extent)
        {
        this.dblExtentWest = extent;
        }


    /***********************************************************************************************
     * Get the SouthExtent.
     *
     * @return double
     */

    public double getSouthExtent()
        {
        return (this.dblExtentSouth);
        }


    /***********************************************************************************************
     * Set the SouthExtent.
     *
     * @return double
     */

    private void setSouthExtent(final double extent)
        {
        this.dblExtentSouth = extent;
        }


    /***********************************************************************************************
     * Get the NorthExtent.
     *
     * @return double
     */

    public double getNorthExtent()
        {
        return (this.dblExtentNorth);
        }


    /***********************************************************************************************
     * Set the NorthExtent.
     *
     * @return double
     */

    private void setNorthExtent(final double extent)
        {
        this.dblExtentNorth = extent;
        }
    }
