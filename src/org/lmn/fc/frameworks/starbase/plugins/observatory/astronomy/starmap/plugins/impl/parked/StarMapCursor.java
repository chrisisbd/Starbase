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
// StarMapCursor StarMapPlugin
//------------------------------------------------------------------------------
// Revision History
//
//  08-10-02    LMN created file from original code in StarMap
//  25-10-02    LMN made Cursor move via AntennaTrackingEvent!
//
//------------------------------------------------------------------------------
// To Do
//
//    Parallactic angle marker
//    Exception handling
//    Label positioning
//
//------------------------------------------------------------------------------
// Astronomy StarMap package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.parked;

//------------------------------------------------------------------------------
// Imports


/***********************************************************************************************
 * StarMapCursor.
 */

public final class StarMapCursor //extends StarMapObject
                                 //implements TrackingListener
    {
//    public static final boolean LABELLED = true;
//    public static final boolean NOLABEL = false;
//
//    private static final int CURSOR_HALFWIDTH = 20;
//    private static final int CURSOR_HALFHEIGHT = 20;
//    private static final int CURSOR_HALFPARALLACTIC = 60;
//    private static final int CURSOR_TRACKLENGTH = 50;
//
//    // Cursor-specific items
//    private Vector<Vector<StarMapPointInterface>> vecCursorTrack;
//    private final int intCursorType;              // The type of Cursor to draw
//    private boolean boolParallacticAngleMode;   // Controls drawing the parallactic angle
//    private double dblParallacticAngle;     // Parallactic angle, calculated when required
//    private Trackable trackableObject;         // The ID of the Trackable being tracked
//
//
//    /***********************************************************************************************
//     * Construct a StarMapCursor.
//     *
//     * @param pluginhost
//     * @param pluginname
//     * @param objectname
//     * @param cursortype
//     * @param labelled
//     * @param clickable
//     * @param colour
//     */
//
//    public StarMapCursor(final StarMapUIComponentPlugin pluginhost,
//                         final String pluginname,
//                         final String objectname,
//                         final int cursortype,
//                         final boolean labelled,
//                         final boolean clickable,
//                         final Color colour)
//        {
//        super(pluginhost,
//              pluginname,
//              objectname,
//              clickable,
//              false,
//              colour,
//              DrawMode.POINT);
//
//        this.intCursorType = cursortype;
//        this.boolParallacticAngleMode = false;
//
//        // Block tracking initially
//        this.trackableObject = null;
//
//        // Prepare an initial Point for each Cursor
//        final Point2D.Double pointInitial = new Point2D.Double(0.0, 0.0);
//
//        // Initialise the Cursor StarMapPoint
//        final StarMapPointInterface pointCursor = new StarMapPoint(this,
//                                                                   this.intCursorType,
//                                                                   isClickable(),
//                                                                   pointInitial,
//                                                                   getColour(),
//                                                                   new Vector<Metadata>(1));
//
//        setStarMapPoints(new Vector<Vector<StarMapPointInterface>>(1));
//
//        getStarMapPoints().add(pointCursor);
//
//        // Initialise the Cursor Track (never clickable)
//        final StarMapPointInterface pointTrack = new StarMapPoint(this,
//                                                                  this.intCursorType,
//                                                                  StarMapPointInterface.NOTCLICKABLE,
//                                                                  pointInitial,
//                                                                  getColour(),
//                                                                  new Vector<Metadata>(1));
//
//        this.vecCursorTrack = new Vector(CURSOR_TRACKLENGTH);
//        this.vecCursorTrack.add(pointTrack);
//        }
//
//
//    /***********************************************************************************************
//     * Return the collection of StarMapPoints to be displayed.
//     *
//     * @return Vector<Vector<StarMapPointInterface>>
//     */
//
//    public final Vector<Vector<StarMapPointInterface>> getStarMapPoints()
//        {
//        switch (this.getDrawMode())
//            {
//            case POINT:
//                {
//                return (getStarMapPoints());
//                }
//
//            case LINE:
//                {
//                return (this.vecCursorTrack);
//                }
//
//            default:
//                {
//                return (null);
//                }
//            }
//        }
//
//    //--------------------------------------------------------------------------
//    // Draw Methods
//    //--------------------------------------------------------------------------
//    // Set the paint geometry
//    // Cursors may be drawn either as Points, or as Lines
//
//    public final void setDrawMode(final DrawMode drawmode)
//        {
//        if ((!DrawMode.POINT.equals(drawmode))
//            || (!DrawMode.LINE.equals(drawmode)))
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " setDrawMode [drawmode=" + drawmode + "]");
//            }
//
//        // See if the Draw Mode has changed, reset tracks if so
//        if (!getDrawMode().equals(drawmode))
//            {
//            this.resetTracks();
//            }
//
//        setDrawMode(drawmode);
//        }
//
//
//    /***********************************************************************************************
//     * Draw the specified StarMapPoint object on the specified Graphics context.
//     *
//     * @param graphics
//     * @param viewport
//     * @param point
//     */
//
//    public final void drawObject(final Graphics graphics,
//                                 final StarMapViewportInterface viewport,
//                                 final StarMapPointInterface point)
//        {
//        switch(this.intCursorType)
//            {
//            // Why doesn't the compiler like this?
//            //case AxisData.COORD_REQUESTED:
//            case 0:
//                {
//                graphics.setColor(point.getColour());
//                graphics.drawRect((point.getPixelsXY().x - (CURSOR_HALFWIDTH/3)),
//                                  (point.getPixelsXY().y - (CURSOR_HALFHEIGHT/3)),
//                                  (CURSOR_HALFWIDTH << 1) /3,
//                                  (CURSOR_HALFHEIGHT << 1) /3);
//                graphics.drawLine(point.getPixelsXY().x - CURSOR_HALFWIDTH,
//                                  point.getPixelsXY().y,
//                                  point.getPixelsXY().x + CURSOR_HALFWIDTH,
//                                  point.getPixelsXY().y);
//                graphics.drawLine(point.getPixelsXY().x,
//                                  point.getPixelsXY().y - CURSOR_HALFHEIGHT,
//                                  point.getPixelsXY().x,
//                                  point.getPixelsXY().y + CURSOR_HALFHEIGHT);
//                break;
//                }
//
//            //case AxisData.COORD_ACTUAL:
//            case 1:
//                {
//                graphics.setColor(point.getColour());
//                graphics.drawOval((point.getPixelsXY().x - (CURSOR_HALFWIDTH >> 1)),
//                                  (point.getPixelsXY().y - (CURSOR_HALFHEIGHT >> 1)),
//                                  CURSOR_HALFWIDTH,
//                                  CURSOR_HALFHEIGHT);
//                graphics.drawLine(point.getPixelsXY().x - CURSOR_HALFWIDTH,
//                                  point.getPixelsXY().y,
//                                  point.getPixelsXY().x + CURSOR_HALFWIDTH,
//                                  point.getPixelsXY().y);
//                graphics.drawLine(point.getPixelsXY().x,
//                                  point.getPixelsXY().y - CURSOR_HALFHEIGHT,
//                                  point.getPixelsXY().x,
//                                  point.getPixelsXY().y + CURSOR_HALFHEIGHT);
//                break;
//                }
//
//            default:
//                {
////                throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                    + " [cursortype=" + intCursorType + "]");
//                }
//            }
//
//        // Draw the ParallacticAngle marker...
//        // The angle was calculated in transformCoordinates()
//
//        if (boolParallacticAngleMode)
//            {
//            // The angle is negative before, and positive after,
//            // passage through the southern meridian
//
//            final int intXoffset = (int)(CURSOR_HALFPARALLACTIC * AstroMath.sind(dblParallacticAngle));
//            final int intYoffset = (int)(CURSOR_HALFPARALLACTIC * AstroMath.cosd(dblParallacticAngle));
//            //LOGGER.debugTimedEvent(".drawObject [intXoffset=" + intXoffset + "] [intYoffset=" + intYoffset + "]");
//
//            graphics.setColor(Color.white);
//            graphics.drawLine(point.getPixelsXY().x + intXoffset,
//                              point.getPixelsXY().y - intYoffset,
//                              point.getPixelsXY().x - intXoffset,
//                              point.getPixelsXY().y + intYoffset);
//            }
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Reset the tracks left by the Cursor, regardless of mode
//
//    public final void resetTracks()
//        {
//        this.vecCursorTrack = new Vector(CURSOR_TRACKLENGTH);
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Control drawing the parallactic angle
//
//    public final void setParallacticAngleMode(final boolean enableparallactic)
//        {
//        this.boolParallacticAngleMode = enableparallactic;
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Read the parallactic angle mode
//
//    public final boolean getParallacticAngleMode()
//        {
//        return(this.boolParallacticAngleMode);
//        }
//
//    //--------------------------------------------------------------------------
//    // Label each individual StarMapPoint object
//    // The caller must take care of repaint()
//
//    public final void labelObject(final Graphics graphics,
//                                  final StarMapViewportInterface viewportInterface,
//                                  final StarMapPointInterface pointInterface)
//        {
//        final FontMetrics textMetrics = graphics.getFontMetrics();
//        final int intTextOffset = pointInterface.getPixelsXY().y - CURSOR_HALFHEIGHT;
//
//        graphics.drawString("Az " + horizontalFormat.format(pointInterface.getCoordinates().x),
//                            pointInterface.getPixelsXY().x + 3,
//                            intTextOffset);
//        graphics.drawString("El " + verticalFormat.format(pointInterface.getCoordinates().y),
//                            pointInterface.getPixelsXY().x + 3,
//                            intTextOffset + textMetrics.getAscent());
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Make sure that the coordinates are up to date for the specified epoch
//    // (e.g the position of a moving object)
//
//    public final void refreshCoordinates(final AstronomicalCalendarInterface calendar)
//        {
//        // Not required for a Cursor
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Coordinate Methods
//    //--------------------------------------------------------------------------
//    // Transform the (Az, El) to (X, Y) for display at a specified time & place
//    // The Latitude and LAST are ignored for (Az, El) transformations
//    // The caller must take care of repaint()
//
//    public final void transformCoordinates(final StarMapViewportInterface viewport,
//                                           final AstronomicalCalendarInterface calendar,
//                                           final StarMapPointInterface[][] clickcoordinates,
//                                           final boolean originisnorth,
//                                           final double latitude)
//        {
//        // Transform the Plugin points
//        StarMapHelper.transformAzElToViewportXY(viewport,
//                                                  getStarMapPoints(),
//                                                  calendar,
//                                                  clickcoordinates,
//                                                  originisnorth,
//                                                  latitude);
//
//        // Transform the Cursor Track points
//        if (DrawMode.LINE.equals(this.getDrawMode()))
//            {
//            StarMapHelper.transformAzElToViewportXY(viewport,
//                                                      this.vecCursorTrack,
//                                                      calendar,
//                                                      clickcoordinates,
//                                                      originisnorth,
//                                                      latitude);
//            }
//
//        // Calculate the current (ra, dec) of the Cursor
//        final StarMapPointInterface pointCursorInterface = (StarMapPointInterface) getStarMapPoints().elementAt(0);
//        final Point2D.Double pointAzEl = new Point2D.Double(pointCursorInterface.getCoordinates().x,
//                                                      pointCursorInterface.getCoordinates().y);
//        final Point2D.Double pointRaDec = AstroUtilities.transformAzEltoRaDec(calendar,
//                                                                        pointAzEl,
//                                                                        latitude,
//                                                                        originisnorth);
//
//        // Calculate the ParallacticAngle while we know the time and place
//        dblParallacticAngle = AstroUtilities.calculateParallacticAngle(calendar,
//                                                                       pointRaDec,
//                                                                       latitude);
//        LOGGER.debugTimedEvent(".transformCoordinates [az=" + pointAzEl.x + "] [el=" + pointAzEl.y + "]");
//        LOGGER.debugTimedEvent(".transformCoordinates [ra=" + pointRaDec.x + "] [dec=" + pointRaDec.y + "]");
//        LOGGER.debugTimedEvent(".transformCoordinates [dblParallacticAngle=" + dblParallacticAngle + "]");
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Move the Cursor to (Azimuth, Elevation) in the specified StarMapViewport
//    // Return true if the Cursor was moved successfully
//
//    public final boolean moveCursorAzEl(final double azimuth,
//                                        final double elevation)
//        {
//        final StarMapPointInterface pointCursorInterface;       // The Cursor being moved
//        final StarMapViewportInterface viewportStarMap;
//
//        // Retrieve the Cursor
//        pointCursorInterface = (StarMapPointInterface) getStarMapPoints().get(0);
//
//        if (pointCursorInterface == null)
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " moveCursorAzEl [cursor=null]");
//            }
//
//        // Get the viewport where the Cursor is displayed
//        viewportStarMap = getHostStarMap().getScreenViewport();
//
//        // Range check the requested position
//        // Give this feedback even if the cursor is not enabled
//        if ((azimuth < viewportStarMap.getAzimuthEast())
//            || (azimuth > viewportStarMap.getAzimuthWest())
//            || (elevation < viewportStarMap.getElevationSouth())
//            || (elevation > viewportStarMap.getElevationNorth()))
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " moveCursorAzEl "
////                                + "[azimuth=" + azimuth + "] "
////                                + "[elevation=" + elevation + "]");
//            }
//
//        // If we get this far, it is safe to move the Cursor!
//        // Move to the requested location only if the Cursor is enabled
//        if (isActive())
//            {
//            // transformCoordinates() will fill in the (x, y)
//            pointCursorInterface.setCoordinates(new Point2D.Double(azimuth, elevation));
//
//            if (DrawMode.LINE.equals(this.getDrawMode()))
//                {
//                // Keep a track of where the Cursor has been
//                this.vecCursorTrack.add(new StarMapPoint(this,
//                                                         this.intCursorType,
//                                                         StarMapPointInterface.NOTCLICKABLE,
//                                                         pointCursorInterface.getCoordinates(),
//                                                         getColour(),
//                                                         pointCursorInterface.getMetadata()));
//                }
//
//            LOGGER.debugTimedEvent(".moveCursorAzEl "
//                              + "[azimuth=" + azimuth + "] "
//                              + "[elevation=" + elevation + "]");
//            getHostStarMap().repaint();
//            return(true);
//            }
//
//        return(false);
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Move the Cursor to (Ra, Dec) in the specified StarMapViewport
//    // at the specified instant and latitude
//    // Note that this is mapped immediately to (Az, El),
//    // so the (Ra, Dec) is valid only for this instant
//    // Return true if the Cursor was moved successfully
//
//    public final boolean moveCursorRaDec(final double ra,
//                                         final double dec,
//                                         final AstronomicalCalendarInterface calendar,
//                                         final double latitude )
//        {
//        final StarMapPointInterface pointCursorInterface;       // Cursor being moved
//        final StarMapViewportInterface viewportStarMap;
//        double dblAzimuth;              // The calculated Azimuth of the Cursor
//        final double dblElevation;            // The calculated Elevation of the Cursor
//        final double dblHourAngle;            // The calculated Hour Angle of the Cursor
//
//        // Retrieve the Cursor
//        pointCursorInterface = (StarMapPointInterface) getStarMapPoints().get(0);
//
//        if (pointCursorInterface == null)
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " moveCursorRaDec [cursor=null]");
//            }
//
//        // Range check the requested position
//        // Give this feedback even if the cursor is not enabled
//        if ((ra < 0.0)
//            || (ra > 24.0)
//            || (dec < -90.0)
//            || (dec > 90.0))
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " moveCursorRaDec "
////                                + "[ra=" + ra + "] "
////                                + "[dec=" + dec + "]");
//            }
//
//        // If we get this far, it is safe to move the Cursor!
//        // Move to the requested location only if the Cursor is enabled
//        if (isActive())
//            {
//            // Get the viewport where the Cursor is displayed
//            viewportStarMap = getHostStarMap().getScreenViewport();
//
//            // Calculate the Local Hour Angle of this point
//            // LAST and RA are in HOURS
//            dblHourAngle = 15.0 * (calendar.getLAST() - ra);
//
//            // Calculate the Azimuth
//            dblAzimuth = (AstroMath.cosd(dblHourAngle)
//                          * AstroMath.sind(latitude))
//                          - (AstroMath.tand(dec)
//                          * AstroMath.cosd(latitude));
//
//            // ATan2() works out the correct quadrant...
//            dblAzimuth = AstroMath.atan2d(AstroMath.sind(dblHourAngle), dblAzimuth);
//
//            // Adjust the Azimuth for the origin at North or South
//            if (this.getHostStarMap().getAzimuthOrigin() == StarMapUIComponentPlugin.AZIMUTH_NORTH)
//                {
//                dblAzimuth += 180.0;
//                }
//
//            // To save time, check that this point is actually displayable
//            if ((dblAzimuth >= viewportStarMap.getAzimuthEast())
//                && (dblAzimuth <= viewportStarMap.getAzimuthWest()))
//                {
//                // Calculate the Elevation
//                dblElevation = AstroMath.asind((AstroMath.sind(latitude)
//                                               * AstroMath.sind(dec))
//                                               + (AstroMath.cosd(latitude)
//                                               * AstroMath.cosd(dec)
//                                               * AstroMath.cosd(dblHourAngle)));
//
//                // To save more time, check that this point is actually displayable
//                if ((dblElevation >= viewportStarMap.getElevationSouth())
//                    && (dblElevation <= viewportStarMap.getElevationNorth()))
//                    {
//                    // transformCoordinates() will fill in the (x, y)
//                    pointCursorInterface.setCoordinates(new Point2D.Double(dblAzimuth, dblElevation));
//
//                    if (DrawMode.LINE.equals(this.getDrawMode()))
//                        {
//                        // Keep a track of where the Cursor has been
//                        this.vecCursorTrack.add(new StarMapPoint(this,
//                                                                 this.intCursorType,
//                                                                 StarMapPointInterface.NOTCLICKABLE,
//                                                                 pointCursorInterface.getCoordinates(),
//                                                                 getColour(),
//                                                                 pointCursorInterface.getMetadata()));
//                        }
//
//                    LOGGER.debugTimedEvent(".moveCursorRaDec "
//                                     + "[ra=" + ra + "] "
//                                     + "[dec=" + dec + "]");
//                    getHostStarMap().repaint();
//                    return(true);
//                    }
//                }
//            }
//
//        return(false);
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Move the Cursor to an (x, y) location in the specified StarMapViewport
//    // Do nothing if the Cursor is not enabled
//
//    public final void moveCursorXY(final int x,
//                                   final int y)
//        {
//        final StarMapPointInterface pointCursorInterface;       // The Cursor being moved
//        final StarMapViewportInterface viewportStarMap;
//        final double dblAzimuthCurrent;
//        final double dblElevationCurrent;
//
//        // Get the Cursor StarMapPoint from the Plugin Vector
//        pointCursorInterface = (StarMapPointInterface) getStarMapPoints().get(0);
//
//        if (pointCursorInterface == null)
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " moveCursorXY [cursor=null]");
//            }
//
//        // Get the viewport where the Cursor is displayed
//        viewportStarMap = getHostStarMap().getScreenViewport();
//
//        // Range check the requested position within the current viewport
//        // Give this feedback even if the cursor is not enabled
//        if ((x < viewportStarMap.getTopLeft().x)
//            || (x > viewportStarMap.getBottomRight().x)
//            || (y < viewportStarMap.getTopLeft().y)
//            || (y > viewportStarMap.getBottomRight().y))
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " moveCursorXY "
////                                + "[x=" + x + "] "
////                                + "[y=" + y + "]");
//            }
//
//        // See if this Cursor is enabled
//        if (isActive())
//            {
//            // Calculate the new Cursor position
//            dblAzimuthCurrent = viewportStarMap.getAzimuthEast() + (double)(x-viewportStarMap.getTopLeft().x) / viewportStarMap.getHorizPixelsPerDegree();
//            dblElevationCurrent = viewportStarMap.getElevationNorth() - ((double)(y-viewportStarMap.getTopLeft().y) / viewportStarMap.getVertPixelsPerDegree());
//
//            // Move the Cursor!
//            pointCursorInterface.setCoordinates(new Point2D.Double(dblAzimuthCurrent, dblElevationCurrent));
//
//            // This isn't strictly necessary, but we may as well store the values
//            // transformCoordinates() will fill in the (x, y)
//            pointCursorInterface.setPixelsXY(new Point(x, y));
//
//            if (DrawMode.LINE.equals(this.getDrawMode()))
//                {
//                // Keep a track of where the Cursor has been
//                this.vecCursorTrack.add(new StarMapPoint(this,
//                                                         this.intCursorType,
//                                                         StarMapPointInterface.NOTCLICKABLE,
//                                                         pointCursorInterface.getCoordinates(),
//                                                         getColour(),
//                                                         pointCursorInterface.getMetadata()));
//                }
//
//            LOGGER.debugTimedEvent(".moveCursorXY "
//                              + "[x=" + x + "] "
//                              + "[y=" + y + "]");
//            getHostStarMap().repaint();
//            }
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Events
//    //--------------------------------------------------------------------------
//    // The Tracking Interface
//
//    public final void trackingEvent(final TrackingEvent event)
//        {
//        final Trackable trackable;
//
//        // Find out which Trackable has produced the Event
//        trackable = event.getTrackable();
//
//        // Check for null data
//        if ((trackable != null)
//            && (trackable.equals(this.trackableObject)))
//            {
//            if (event.getDebugMode())
//                {
//                LOGGER.debugTimedEvent(".trackingEvent received "
//                                 + "[trackable=" + trackable.getName() + "] "
//                                 + "[plugin=" + getPluginName() + "] "
//                                 + "[cursor=" + getObjectName() + "]");
//                }
//
//            // Move the Cursor to the Topocentric coordinates specified by the Trackable
//            // We could have used Celestial (Ra, Dec), but the effect is the same
//            moveCursorAzEl(trackable.getTopocentricCoordinates(this.intCursorType).x,
//                           trackable.getTopocentricCoordinates(this.intCursorType).y);
//            }
//        }
//
//
//    //--------------------------------------------------------------------------
//    // Indicate which Trackable to track with this instance
//
//    public final void setTrackable(final Trackable trackable)
//        {
//        if (trackable != null)
//            {
//            this.trackableObject = trackable;
//            }
//        else
//            {
////            throw new Exception(ExceptionLibrary.EXCEPTION_OUTOFRANGE
////                                + " setAntennaID [antennaID=" + antennaID + "]");
//            }
//        }
    }


//------------------------------------------------------------------------------
// End of File

