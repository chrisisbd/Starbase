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
// StarMapViewport
//------------------------------------------------------------------------------
// Revision History
//
//  28-09-02    LMN created file
//
//------------------------------------------------------------------------------
// Astronomy Starmap package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.impl;

import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;

import javax.swing.*;
import java.awt.*;

//------------------------------------------------------------------------------

public final class StarMapViewport implements StarMapViewportInterface
    {
    // The current zoomed Viewport defined in degrees
    private double dblAzimuthEast;
    private double dblAzimuthWest;
    private double dblElevationSouth;
    private double dblElevationNorth;

    // The maximum extents of the Viewport defined in degrees
    private double dblExtentsAzimuthEast;
    private double dblExtentsAzimuthWest;
    private double dblExtentsElevationSouth;
    private double dblExtentsElevationNorth;

    // The factor to apply between zoom settings
    private double dblZoomFactor;

    // The Viewport transformed into pixels by updatePixelViewportAndRemoveScales()
    private Point pointTopLeft;                  // The pixel coords of the frame top left
    private Point pointBottomRight;              // The pixel coords of the frame bottom right

    private Point pointAziScaleTopLeft;          // Top left of Azimuth scale area
    private Point pointAziScaleBottomRight;      // Bottom right of Azimuth scale area
    private Point pointElevScaleTopLeft;         // Top left of Elevation scale area
    private Point pointElevScaleBottomRight;     // Bottom right of Elevation scale area

    // Some useful derived parameters
    private double dblHorizPixelsPerDegree;
    private double dblVertPixelsPerDegree;
    private double dblAspectRatio;

    // Scales
    private boolean boolEnableScales;              // Controls display of the scales


    /***********************************************************************************************
     * StarMapViewport.
     * Set up a Viewport, defining the extents and the initial view.
     * The extents can be changed later with setExtents().
     *
     * @param extentsazimutheast
     * @param extentsazimuthwest
     * @param extentselevationsouth
     * @param extentselevationnorth
     * @param azimutheast
     * @param azimuthwest
     * @param elevationsouth
     * @param elevationnorth
     */

    public StarMapViewport(final double extentsazimutheast,
                           final double extentsazimuthwest,
                           final double extentselevationsouth,
                           final double extentselevationnorth,
                           final double azimutheast,
                           final double azimuthwest,
                           final double elevationsouth,
                           final double elevationnorth)
        {
        if ((azimutheast < 0.0)
            || (azimuthwest > 360.0)
            || (elevationsouth < 0.0)
            || (elevationnorth > 90.0)
            || (azimutheast < extentsazimutheast)
            || (azimuthwest > extentsazimuthwest)
            || (elevationsouth < extentselevationsouth)
            || (elevationnorth > extentselevationnorth))
            {
            LOGGER.error("StarMapViewport EXCEPTION_OUTOFRANGE");
//            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE);
            }

        dblExtentsAzimuthEast = extentsazimutheast;
        dblExtentsAzimuthWest = extentsazimuthwest;
        dblExtentsElevationSouth = extentselevationsouth;
        dblExtentsElevationNorth = extentselevationnorth;

        dblAzimuthEast = azimutheast;
        dblAzimuthWest = azimuthwest;
        dblElevationSouth = elevationsouth;
        dblElevationNorth = elevationnorth;

        dblZoomFactor = 1.0;
        this.boolEnableScales = false;
        }


    /***********************************************************************************************
     * Set the Azimuth and Elevation range of the display map.
     *
     * @param azimutheast
     * @param azimuthwest
     * @param elevationsouth
     * @param elevationnorth
     */

    public final void setExtents(final double azimutheast,
                                 final double azimuthwest,
                                 final double elevationsouth,
                                 final double elevationnorth)
        {
        // Range check the incoming data
        // azimutheast must always be less than azimuthwest, range !> 360
        // elevationsouth must always be less than elevationnorth, range !> 90

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "[azimutheast=" + azimutheast + "]");
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "[azimuthwest=" + azimuthwest + "]");
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "[elevationsouth=" + elevationsouth + "]");
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "[elevationnorth=" + elevationnorth + "]");

        if ((azimuthwest <= azimutheast)
            || (azimutheast < 0.0)
            || (azimuthwest > 360.0)
            || (azimuthwest - azimutheast < 10.0)
            || (azimuthwest - azimutheast > 360.0)
            || (elevationnorth <= elevationsouth)
            || (elevationsouth < 0.0)
            || (elevationnorth > 90.0)
            || (elevationnorth - elevationsouth < 10.0)
            || (elevationnorth - elevationsouth > 90.0))
            {
            LOGGER.error("StarMapViewport EXCEPTION_OUTOFRANGE");
//            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
//                                + " [azimutheast=" + azimutheast + "]"
//                                + " [azimuthwest=" + azimuthwest + "]"
//                                + " [elevationsouth=" + elevationsouth + "]"
//                                + " [elevationnorth=" + elevationnorth + "]");
            }

        // This is the unzoomed maximum area which can be viewed by this instance
        dblExtentsAzimuthEast = azimutheast;
        dblExtentsAzimuthWest = azimuthwest;
        dblExtentsElevationSouth = elevationsouth;
        dblExtentsElevationNorth = elevationnorth;

        // Start viewing at no zoom, i.e. the same as full extents
        dblAzimuthEast = dblExtentsAzimuthEast;
        dblAzimuthWest = dblExtentsAzimuthWest;
        dblElevationSouth = dblExtentsElevationSouth;
        dblElevationNorth = dblExtentsElevationNorth;
        }


    /***********************************************************************************************
     * Find out the current size of the window, for coordinate conversions.
     * Remove the area occupied by the scales, if enabled.
     * Map drawing uses (pointTopLeft, pointBottomRight).
     * Azimuth Scale uses (pointAziScaleTopLeft, pointAziScaleBottomRight).
     * Elevation scale uses (pointElevScaleTopLeft, pointElevScaleBottomRight).
     *
     * @param component
     */

    public final void updatePixelViewportAndRemoveScales(final JComponent component)
        {
        if (component != null)
            {
            updatePixelViewportAndRemoveScales(component.getWidth(),
                                               component.getHeight(),
                                               component.getInsets());
            }
        }


    /***********************************************************************************************
     * Find out the current size of the window, for coordinate conversions.
     * Remove the area occupied by the scales, if enabled.
     * Used for Export.
     *
     * @param width
     * @param height
     * @param insets
     */

    public void updatePixelViewportAndRemoveScales(final int width,
                                                   final int height,
                                                   final Insets insets)
        {
        final int intPixelWidth;
        final int intPixelHeight;

        // See if the Scales are being displayed
        if (isScaleEnabled())
            {
            // Remove the Insets and the Scales
            intPixelWidth = width - insets.left - insets.right - StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH;
            intPixelHeight = height - insets.top - insets.bottom - StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT;

            pointAziScaleTopLeft = new Point((insets.left + StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH),   // WAS: -1
                                             (insets.top + intPixelHeight));

            pointAziScaleBottomRight = new Point((insets.left + intPixelWidth + StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH - 1),
                                                 insets.top + intPixelHeight + StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT - 1);

            pointElevScaleTopLeft = new Point(insets.left, insets.top);

            pointElevScaleBottomRight = new Point(pointAziScaleTopLeft.x - 1, pointAziScaleTopLeft.y - 1);   // WAS: pointAziScaleTopLeft copy

            pointTopLeft = new Point(pointElevScaleBottomRight.x + 1, pointElevScaleTopLeft.y);
            pointBottomRight = new Point(pointAziScaleBottomRight.x, pointAziScaleTopLeft.y - 1);
            }
        else
            {
            // No scales are visible, so just remove the Insets
            intPixelWidth = width - insets.left - insets.right;
            intPixelHeight = height - insets.top - insets.bottom;

            pointTopLeft = new Point(insets.left, insets.top);
            pointBottomRight = new Point((insets.left + intPixelWidth - 1),
                                         (insets.top + intPixelHeight - 1));

            // Be consistent...
            pointAziScaleTopLeft =  new Point(pointTopLeft.x, pointBottomRight.y);
            pointAziScaleBottomRight = pointBottomRight;
            pointElevScaleTopLeft = pointTopLeft;
            pointElevScaleBottomRight = pointAziScaleTopLeft;
            }

        // Scale coordinates to the supplied coordinate frame
        dblHorizPixelsPerDegree = (double)intPixelWidth / (dblAzimuthWest - dblAzimuthEast);
        dblVertPixelsPerDegree = (double)intPixelHeight / (dblElevationNorth - dblElevationSouth);

        // Calculate the aspect ratio, so circles are circular!
        dblAspectRatio = (double)intPixelHeight / (double)intPixelWidth;
        }


    /***********************************************************************************************
     * Zoom in the requested direction centred at the specified Point(x, y) if possible.
     *
     * @param zoompoint
     * @param zoom
     */

    public final void zoomViewport(final Point zoompoint,
                                   final int zoom)
        {
        double dblZoomAzimuthEast;
        double dblZoomAzimuthWest;
        final double dblZoomAzimuthCentre;
        final double dblAzimuthCentre;        // The Azimuth centre of the unzoomed map
        double dblAzimuthRange;

        double dblZoomElevationSouth;
        double dblZoomElevationNorth;
        final double dblZoomElevationCentre;
        final double dblElevationCentre;      // The Elevation centre of the unzoomed map
        double dblElevationRange;

        // Try to beep if the click is outside the pixel viewport
        if ((zoompoint.x < this.pointTopLeft.x)
            || (zoompoint.x > this.pointBottomRight.x)
            || (zoompoint.y < this.pointTopLeft.y)
            || (zoompoint.y > this.pointBottomRight.y))
            {
//            final Toolkit toolKit = this.hostStarMapComponent.getToolkit();
//            toolKit.beep();
            return;
            }

        switch(zoom)
            {
            case StarMapUIComponentPlugin.ZOOM_EXTENTS:
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       ".zoomViewport [Zoom extents]");

                // Just reset to the Extents
                dblAzimuthEast = dblExtentsAzimuthEast;
                dblAzimuthWest = dblExtentsAzimuthWest;
                dblElevationSouth = dblExtentsElevationSouth;
                dblElevationNorth = dblExtentsElevationNorth;
                break;
                }

            case StarMapUIComponentPlugin.ZOOM_IN:
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       ".zoomViewport [Zoom in]");

                // Deal with Azimuth first
                // Find the current Azimuth range of the unzoomed map
                dblAzimuthRange = dblAzimuthWest - dblAzimuthEast;

                // Find the Azimuth centre of the unzoomed map
                dblAzimuthCentre = dblAzimuthEast + (dblAzimuthRange * 0.5);

                // Change the range with the zoom factor for this viewport
                dblAzimuthRange = dblAzimuthRange * dblZoomFactor;

                // Limit the Zoom In
                dblAzimuthRange = Math.max(dblAzimuthRange, StarMapUIComponentPlugin.ZOOM_AZIMUTH_MIN);

                // Calculate the ideal Azimuth centre of the zoom region
                // This is not necessarily going to be the centre of the zoomed map!
                dblZoomAzimuthCentre = dblAzimuthEast
                                       + ((double)(zoompoint.x-this.pointTopLeft.x) / this.dblHorizPixelsPerDegree);

                // Calculate the *ideal* Azimuth bounds of the new zoomed region
                if (dblZoomAzimuthCentre <= dblAzimuthCentre)
                    {
                    // The Azimuth zoom centre is to the East of the original centre
                    // Find the *ideal* AzimuthEast of the new zoomed region
                    dblZoomAzimuthEast = dblZoomAzimuthCentre - (dblAzimuthRange * 0.5);

                    // Did we move out of the viewport?
                    if (dblZoomAzimuthEast < dblAzimuthEast)
                        {
                        // Move the region to start on the Eastern boundary
                        dblZoomAzimuthEast = dblAzimuthEast;
                        }

                    // Complete the definition of the new Azimuth viewport
                    dblZoomAzimuthWest = dblZoomAzimuthEast + dblAzimuthRange;
                    }
                else
                    {
                    // The Azimuth zoom centre is to the West of the original centre
                    // Find the *ideal* AzimuthWest of the new zoomed region
                    dblZoomAzimuthWest = dblZoomAzimuthCentre + (dblAzimuthRange * 0.5);

                    // Did we move out of the viewport?
                    if (dblZoomAzimuthWest > dblAzimuthWest)
                        {
                        // Move the region to start on the Western boundary
                        dblZoomAzimuthWest = dblAzimuthWest;
                        }

                    // Complete the definition of the new Azimuth viewport
                    dblZoomAzimuthEast = dblZoomAzimuthWest - dblAzimuthRange;
                    }

                //--------------------------------------------------------------
                // Now do Elevation
                // Find the current Elevation range of the unzoomed map
                dblElevationRange = dblElevationNorth - dblElevationSouth;

                // Find the Elevation centre of the unzoomed map
                dblElevationCentre = dblElevationSouth + (dblElevationRange * 0.5);

                // Change the range with the zoom factor for this viewport
                dblElevationRange = dblElevationRange * dblZoomFactor;

                // Limit the Zoom In
                dblElevationRange = Math.max(dblElevationRange, StarMapUIComponentPlugin.ZOOM_ELEVATION_MIN);

                // Calculate the ideal Elevation centre of the zoom region
                // This is not necessarily going to be the centre of the zoomed map!
                dblZoomElevationCentre = dblElevationNorth
                                         - ((double)(zoompoint.y-this.pointTopLeft.y) / this.dblVertPixelsPerDegree);

                // Calculate the *ideal* Elevation bounds of the new zoomed region
                if (dblZoomElevationCentre <= dblElevationCentre)
                    {
                    // The zoom centre is to the south of the original centre
                    // Find the *ideal* ElevationSouth of the new zoomed region
                    dblZoomElevationSouth = dblZoomElevationCentre - (dblElevationRange * 0.5);

                    // Did we move out of the viewport?
                    if (dblZoomElevationSouth < dblElevationSouth)
                        {
                        // Move the region to start on the Southern boundary
                        dblZoomElevationSouth = dblElevationSouth;
                        }

                    // Complete the definition of the new Elevation viewport
                    dblZoomElevationNorth = dblZoomElevationSouth + dblElevationRange;
                    }
                else
                    {
                    // The Elevation zoom centre is to the north of the original centre
                    // Find the *ideal* ElevationNorth of the new zoomed region
                    dblZoomElevationNorth = dblZoomElevationCentre + (dblElevationRange * 0.5);

                    // Did we move out of the viewport?
                    if (dblZoomElevationNorth > dblElevationNorth)
                        {
                        // Move the region to start on the Northern boundary
                        dblZoomElevationNorth = dblElevationNorth;
                        }

                    // Complete the definition of the new Elevation viewport
                    dblZoomElevationSouth = dblZoomElevationNorth - dblElevationRange;
                    }

                // Apply the new bounds
                dblAzimuthEast = dblZoomAzimuthEast;
                dblAzimuthWest = dblZoomAzimuthWest;
                dblElevationSouth = dblZoomElevationSouth;
                dblElevationNorth = dblZoomElevationNorth;

                break;
                }

            case StarMapUIComponentPlugin.ZOOM_OUT:
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       ".zoomViewport [Zoom out]");

                // Deal with Azimuth first
                // Find the current Azimuth range of the unzoomed map
                dblAzimuthRange = dblAzimuthWest - dblAzimuthEast;
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       ".zoomViewport [dblAzimuthRange=" + dblAzimuthRange + "]");

                // Find the Azimuth centre of the unzoomed map
                dblAzimuthCentre = dblAzimuthEast + (dblAzimuthRange * 0.5);

                // Change the range with the zoom factor for this viewport
                dblAzimuthRange = dblAzimuthRange / dblZoomFactor;
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       ".zoomViewport [dblAzimuthRange=" + dblAzimuthRange + "]");

                // Limit the Zoom Out to no more than the original extents
                dblAzimuthRange = Math.min(dblAzimuthRange, (dblExtentsAzimuthWest - dblExtentsAzimuthEast));
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       ".zoomViewport [dblAzimuthRange=" + dblAzimuthRange + "]");

                // Calculate the ideal Azimuth centre of the zoom region
                // This is not necessarily going to be the centre of the zoomed map!
                dblZoomAzimuthCentre = dblAzimuthEast
                                       + ((double)(zoompoint.x-this.pointTopLeft.x) / this.dblHorizPixelsPerDegree);

                // Calculate the *ideal* Azimuth bounds of the new zoomed region
                if (dblZoomAzimuthCentre <= dblAzimuthCentre)
                    {
                    // The Azimuth zoom centre is to the East of the original centre
                    // Find the *ideal* AzimuthEast of the new zoomed region
                    dblZoomAzimuthEast = dblZoomAzimuthCentre - (dblAzimuthRange * 0.5);

                    // Did we move out of the Extents viewport?
                    if (dblZoomAzimuthEast < dblExtentsAzimuthEast)
                        {
                        // Move the region to start on the Eastern boundary
                        dblZoomAzimuthEast = dblExtentsAzimuthEast;
                        }

                    // Complete the definition of the new Azimuth viewport
                    dblZoomAzimuthWest = dblZoomAzimuthEast + dblAzimuthRange;
                    }
                else
                    {
                    // The Azimuth zoom centre is to the West of the original centre
                    // Find the *ideal* AzimuthWest of the new zoomed region
                    dblZoomAzimuthWest = dblZoomAzimuthCentre + (dblAzimuthRange * 0.5);

                    // Did we move out of the Extents viewport?
                    if (dblZoomAzimuthWest > dblExtentsAzimuthWest)
                        {
                        // Move the region to start on the Western boundary
                        dblZoomAzimuthWest = dblExtentsAzimuthWest;
                        }

                    // Complete the definition of the new Azimuth viewport
                    dblZoomAzimuthEast = dblZoomAzimuthWest - dblAzimuthRange;
                    }

                //--------------------------------------------------------------
                // Now do Elevation
                // Find the current Elevation range of the unzoomed map
                dblElevationRange = dblElevationNorth - dblElevationSouth;

                // Find the Elevation centre of the unzoomed map
                dblElevationCentre = dblElevationSouth + (dblElevationRange * 0.5);

                // Change the range with the zoom factor for this viewport
                dblElevationRange = dblElevationRange / dblZoomFactor;

                // Limit the Zoom In
                dblElevationRange = Math.min(dblElevationRange, (dblExtentsElevationNorth - dblExtentsElevationSouth));

                // Calculate the ideal Elevation centre of the zoom region
                // This is not necessarily going to be the centre of the zoomed map!
                dblZoomElevationCentre = dblElevationNorth
                                         - ((double)(zoompoint.y-this.pointTopLeft.y) / this.dblVertPixelsPerDegree);

                // Calculate the *ideal* Elevation bounds of the new zoomed region
                if (dblZoomElevationCentre <= dblElevationCentre)
                    {
                    // The zoom centre is to the south of the original centre
                    // Find the *ideal* ElevationSouth of the new zoomed region
                    dblZoomElevationSouth = dblZoomElevationCentre - (dblElevationRange * 0.5);

                    // Did we move out of the Extents viewport?
                    if (dblZoomElevationSouth < dblExtentsElevationSouth)
                        {
                        // Move the region to start on the Southern boundary
                        dblZoomElevationSouth = dblExtentsElevationSouth;
                        }

                    // Complete the definition of the new Elevation viewport
                    dblZoomElevationNorth = dblZoomElevationSouth + dblElevationRange;
                    }
                else
                    {
                    // The Elevation zoom centre is to the north of the original centre
                    // Find the *ideal* ElevationNorth of the new zoomed region
                    dblZoomElevationNorth = dblZoomElevationCentre + (dblElevationRange * 0.5);

                    // Did we move out of the Extents viewport?
                    if (dblZoomElevationNorth > dblExtentsElevationNorth)
                        {
                        // Move the region to start on the Northern boundary
                        dblZoomElevationNorth = dblExtentsElevationNorth;
                        }

                    // Complete the definition of the new Elevation viewport
                    dblZoomElevationSouth = dblZoomElevationNorth - dblElevationRange;
                    }

                // Apply the new bounds
                dblAzimuthEast = dblZoomAzimuthEast;
                dblAzimuthWest = dblZoomAzimuthWest;
                dblElevationSouth = dblZoomElevationSouth;
                dblElevationNorth = dblZoomElevationNorth;

                break;
                }

            default:
                {
//                throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
//                                    + " zoomViewport [zoom=" + zoom + "]");
                }
            }

        showDebugViewport();
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public double getAzimuthEast()
        {
        return (this.dblAzimuthEast);
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public double getAzimuthWest()
        {
        return (this.dblAzimuthWest);
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public double getElevationSouth()
        {
        return (this.dblElevationSouth);
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public double getElevationNorth()
        {
        return (this.dblElevationNorth);
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public double getZoomFactor()
        {
        return (this.dblZoomFactor);
        }


    /***********************************************************************************************
     *
     * @param zoom
     */

    public void setZoomFactor(final double zoom)
        {
        this.dblZoomFactor = zoom;
        }


    /***********************************************************************************************
     *
     * @return Point
     */

    public Point getAziScaleTopLeft()
        {
        return (this.pointAziScaleTopLeft);
        }


    /***********************************************************************************************
     *
     * @return Point
     */

    public Point getAziScaleBottomRight()
        {
        return (this.pointAziScaleBottomRight);
        }


    /***********************************************************************************************
     *
     * @return Point
     */

    public Point getElevScaleTopLeft()
        {
        return (this.pointElevScaleTopLeft);
        }


    /***********************************************************************************************
     *
     * @return Point
     */

    public Point getElevScaleBottomRight()
        {
        return (this.pointElevScaleBottomRight);
        }


    /***********************************************************************************************
     * Get the location of the TopLeft of the Viewport.
     *
     * @return Point
     */

    public Point getTopLeft()
        {
        return (this.pointTopLeft);
        }


    /***********************************************************************************************
     * Get the location of the BottomRight of the Viewport.
     *
     * @return Point
     */

    public Point getBottomRight()
        {
        return (this.pointBottomRight);
        }


    /***********************************************************************************************
     * Get the number of pixels per degree, Horizontally.
     *
     * @return double
     */

    public double getHorizPixelsPerDegree()
        {
        return (this.dblHorizPixelsPerDegree);
        }

    /***********************************************************************************************
     * Get the number of pixels per degree, Vertically.
     *
     * @return double
     */

    public double getVertPixelsPerDegree()
        {
        return (this.dblVertPixelsPerDegree);
        }


    /***********************************************************************************************
     * Control the Scales.
     *
     * @param enable
     */

    public final void enableScales(final boolean enable)
        {
        this.boolEnableScales = enable;
        }


    /***********************************************************************************************
     * Indicate if the scales are enabled.
     *
     * @return boolean
     */

    public final boolean isScaleEnabled()
        {
        return (this.boolEnableScales);
        }


    /***********************************************************************************************
     * Get the width of the Elevation scale.
     *
     * @return int
     */

    public int getElevationScaleWidth()
        {
        return (StarMapUIComponentPlugin.ELEVATION_SCALE_WIDTH);
        }


    /***********************************************************************************************
     * Get the height of the Azimuth scale.
     *
     * @return int
     */

    public int getAzimuthScaleHeight()
        {
        return (StarMapUIComponentPlugin.AZIMUTH_SCALE_HEIGHT);
        }


    /***********************************************************************************************
     * Debug a viewport.
     */

    private void showDebugViewport()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarMapViewport.showDebugViewport [dblAzimuthEast=" + dblAzimuthEast + "]");
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarMapViewport.showDebugViewport [dblAzimuthWest=" + dblAzimuthWest + "]");
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarMapViewport.showDebugViewport [dblElevationSouth=" + dblElevationSouth + "]");
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarMapViewport.showDebugViewport [dblElevationNorth=" + dblElevationNorth + "]");
        }
    }
