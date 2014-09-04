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

package org.lmn.fc.common.utilities.astronomy;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.StarMapPoint;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * CoordinateConversions.
 */

public final class CoordinateConversions implements FrameworkSingletons
    {
    public static final double DEGREES_PER_HOUR = 15.0;
    public static final double RA_DEG_MIN = 0.0;
    public static final double RA_DEG_MAX = 360.0;
    public static final double RA_HOUR_MIN = 0.0;
    public static final double RA_HOUR_MAX = 24.0;
    public static final double DEC_MIN = -90.0;
    public static final double DEC_MAX = 90.0;
    public static final double AZI_MIN = 0.0;
    public static final double AZI_MAX = 360.0;
    public static final double ELEV_MIN = 0.0;
    public static final double ELEV_MAX = 90.0;
    public static final double LONGITUDE_RANGE_MIN = -180.0;
    public static final double LONGITUDE_RANGE_MAX = 180.0;
    public static final double LONGITUDE_MIN = 0.0;
    public static final double LONGITUDE_MAX = 360.0;
    public static final double LATITUDE_RANGE_MIN = -90.0;
    public static final double LATITUDE_RANGE_MAX = 90.0;
    public static final double AZIMUTH_CORRECTION = 180.0;


    /**********************************************************************************************/
    /* Coordinate Conversions                                                                     */
    /***********************************************************************************************
     * Convert Spherical (Ecliptic) {l, b, r} Coordinates to {Ra, Dec}.
     *
     * @param coords
     * @param jde
     *
     * @return Point2D.Double
     */

    public static Point2D.Double convertSphericalToRaDec(final SphericalCoordinates coords,
                                                         final double jde)
        {
        final double dblObliquityDegrees;
        final double dblRaNumerator;
        double dblRaDegrees;
        double dblDecDegrees;

        // Obliquity in degrees, Nutation in seconds of arc
        dblObliquityDegrees = AstroUtilities.obliquityOfEcliptic(jde) + (Nutation.nutationInObliquity(jde) / 3600);

        //System.out.println("Obliquity degrees=" + AstroUtilities.obliquityOfEcliptic(jde) + " True Obliquity degrees=" + dblObliquityDegrees + "  Nutation seconds=" + Nutation.nutationInObliquity(jde));
        // lambda = longitude, beta = latitude, epsilon = obliquity

        // tan(alpha) = (sin(lambda) cos(epsilon) - tan(beta) sin(epsilon)) / cos(lambda)
        dblRaNumerator = (AstroMath.sind(coords.getLongitude()) * AstroMath.cosd(dblObliquityDegrees))
                            - (AstroMath.tand(coords.getLatitude()) * AstroMath.sind(dblObliquityDegrees));
        dblRaDegrees = AstroMath.atan2d(dblRaNumerator, AstroMath.cosd(coords.getLongitude()));
        dblRaDegrees = normaliseRaDegrees(dblRaDegrees);

        // sin(delta) = sin(beta) cos(epsilon) + cos(beta) sin(epsilon) sin(lambda)
        dblDecDegrees = (AstroMath.sind(coords.getLatitude()) * AstroMath.cosd(dblObliquityDegrees))
                            + (AstroMath.cosd(coords.getLatitude())
                                   * AstroMath.sind(dblObliquityDegrees)
                                   *  AstroMath.sind(coords.getLongitude()));
        dblDecDegrees = AstroMath.asind(dblDecDegrees);
        dblDecDegrees = normaliseDecDegrees(dblDecDegrees);

        // (Ra, Dec) RA in HOURS, Dec in DEGREES
        return (new Point2D.Double(dblRaDegrees / DEGREES_PER_HOUR,
                                   dblDecDegrees));
        }


    /***********************************************************************************************
     * Convert a single point (Ra, Dec) to (Az, El) - RA in HOURS, Dec in DEGREES.
     * BEWARE the origin of Azimuth!
     * See: Meeus pp 88, 89.
     *
     * @param pointradec
     * @param originisnorth
     * @param last
     * @param latitude
     *
     * @return Point2D.Double
     */

    public static Point2D.Double convertRaDecToAzEl(final Point2D.Double pointradec,
                                                    final boolean originisnorth,
                                                    final double last,
                                                    final double latitude)
        {
        final Point2D.Double pointAzEl;
        double dblAzimuthDegrees;
        final double dblAzimuthDenominatorDegrees;
        final double dblElevationDegrees;
        final double dblHourAngleDegrees;

        // Calculate the Local Hour Angle of this point
        // LAST and RA are in HOURS, DEC in DEGREES
        dblHourAngleDegrees = (last - pointradec.getX()) * DEGREES_PER_HOUR;

        // Calculate the Azimuth
        // Meeus Page 89 Eq. 12.5
        dblAzimuthDenominatorDegrees = (AstroMath.cosd(dblHourAngleDegrees)
                                            * AstroMath.sind(latitude))
                                         - (AstroMath.tand(pointradec.getY())
                                            * AstroMath.cosd(latitude));

        // ATan2() works out the correct quadrant...
        dblAzimuthDegrees = AstroMath.atan2d(AstroMath.sind(dblHourAngleDegrees),
                                             dblAzimuthDenominatorDegrees);

        // Adjust the Azimuth for the origin at North or South
        if (originisnorth)
            {
            dblAzimuthDegrees += AZIMUTH_CORRECTION;
            }

        // Make sure Azimuth is {0.0 ... 360.0}
        dblAzimuthDegrees = normaliseAzimuthDegrees(dblAzimuthDegrees);

        // Calculate the Elevation
        // Meeus Page 89 Eq. 12.6
        dblElevationDegrees = AstroMath.asind((AstroMath.sind(latitude)
                                                * AstroMath.sind(pointradec.getY()))
                                              + (AstroMath.cosd(latitude)
                                                * AstroMath.cosd(pointradec.getY())
                                                * AstroMath.cosd(dblHourAngleDegrees)));

        // Elevation is {-90.0 ... +90.0}
        //dblElevationDegrees = normaliseElevationDegrees(dblElevationDegrees);

        pointAzEl = new Point2D.Double(dblAzimuthDegrees, dblElevationDegrees);

        return (pointAzEl);
        }


    /***********************************************************************************************
     * Convert a single point (Ra, Dec) RA in HOURS, Dec in DEGREES
     * to Galactic (l, b) Longitude, Latitude.
     * See: Meeus pp 90.
     *
     * @param pointradec
     *
     * @return Point2D.Double
     */

    public static Point2D.Double convertRaDecToGalactic(final Point2D.Double pointradec)
        {
        final double SIN27p4 = 0.46019978478385164567409136144904;
        final double COS27p4 = 0.88781538513640133208124598804415;
        final Point2D.Double pointGalactic;
        final double dblRADegrees;
        double dblLongitudeDegrees;
        double dblLatitudeDegrees;

        // RA in HOURS, DEC in DEGREES
        dblRADegrees = 192.25 - (pointradec.getX() * DEGREES_PER_HOUR);

        // Calculate the Longitude
        // Meeus Page 90 Eq. 12.7
        // ATan2() works out the correct quadrant...
        dblLongitudeDegrees = 303.0 - AstroMath.atan2d(AstroMath.sind(dblRADegrees),
                                                       (AstroMath.cosd(dblRADegrees) * SIN27p4)
                                                        - (AstroMath.tand(pointradec.getY()) * COS27p4));
        // Ensure Longitude is {0.0 ... 360.0}
        dblLongitudeDegrees = normaliseLongitudeDegrees(dblLongitudeDegrees);

        // Calculate the Latitude
        // Meeus Page 90 Eq. 12.8
        dblLatitudeDegrees = AstroMath.asind(AstroMath.sind(pointradec.getY()) * SIN27p4
                                            + AstroMath.cosd(pointradec.getY()) * AstroMath.cosd(dblRADegrees) * COS27p4);

        // Ensure Latitude is {-90.0 ... +90.0}
        dblLatitudeDegrees = normaliseLatitudeDegrees(dblLatitudeDegrees);

        pointGalactic = new Point2D.Double(dblLongitudeDegrees, dblLatitudeDegrees);

        return (pointGalactic);
        }


    /***********************************************************************************************
     * Convert a single point (Az, El) to (Ra, Dec).
     * (Az, El) in DEGREES.
     * (Ra, Dec) RA in HOURS, Dec in DEGREES.
     * BEWARE the origin of Azimuth!
     *
     * @param pointazel
     * @param originisnorth
     * @param last
     * @param latitude
     *
     * @return Point2D.Double
     */

    public static Point2D.Double convertAzElToRaDec(final Point2D.Double pointazel,
                                                    final boolean originisnorth,
                                                    final double last,
                                                    final double latitude)
        {
        double dblChauvenetAzimuth;
        final double dblElevation;
        final double dblLocalHourAngleDegrees;
        double dblRaDegrees;
        double dblDecDegrees;
        final double dblDenominator;

        // Correct the Azimuth for a different origin
        // AzEl is in DEGREES
        // See: Jean Meeus Astronomical Algorithms Footnote on pg 88
        if (originisnorth)
            {
            dblChauvenetAzimuth = pointazel.x + AZIMUTH_CORRECTION;
            }
        else
            {
            // Origin is assumed SOUTH, as used by Meeus pg. 89
            dblChauvenetAzimuth = pointazel.x;
            }

        // Make sure Azimuth is {0.0 ... 360.0}
        dblChauvenetAzimuth = normaliseAzimuthDegrees(dblChauvenetAzimuth);

        // Elevation is {0.0 ... 90.0}
        dblElevation = normaliseElevationDegrees(pointazel.y);

        // LAST is in HOURS
//        LOGGER.log("convertAzElToRaDec() [az=" + pointazel.x
//                       + "] [az_chauvenet=" + dblChauvenetAzimuth
//                       + "] [el=" + pointazel.y
//                       + "] [el_norm=" + dblElevation
//                       + "] [last=" + (new HourMinSecDataType(last)).toString_HH_MM_SS()
//                       + "] [latitude=" + latitude + "]");

        //-----------------------------------------------------------------------------------------
        // Right Ascension
        // See: Jean Meeus Astronomical Algorithms (after 12.6, not numbered)
        // See: http://en.wikipedia.org/wiki/Astronomical_coordinate_systems#Horizontal_system
        // See: http://en.wikipedia.org/wiki/Celestial_coordinate_system#Hour_angle_.E2.86.90.E2.86.92_right_ascension
        // See: http://star-www.st-and.ac.uk/~fv/webnotes/chapter7.htm
        // See: http://www.pveducation.org/pvcdrom/properties-of-sunlight/suns-position
        // See: http://code.google.com/p/solarposition/source/browse/trunk/SolarPositionAlgorithm+2/SolarPositionAlgorithm2/src/com/cepmuvakkit/times/posAlgo/AstroLib.java?r=131
        // All trig functions xxxd() use DEGREES

        // This is the one from Meeus
        dblDenominator = (AstroMath.cosd(dblChauvenetAzimuth) * AstroMath.sind(latitude))
                          + (AstroMath.tand(dblElevation) * AstroMath.cosd(latitude));

        // See: Jean Meeus Astronomical Algorithms (after 12.6, not numbered)
        // LHA is measured Westwards from the SOUTH by Chauvenet et al.
        // ATan2() works out the correct quadrant...
        // sin(A) / [cos(A) cos(phi) + tan(h) cos(phi)]
        dblLocalHourAngleDegrees = AstroMath.atan2d(AstroMath.sind(dblChauvenetAzimuth), dblDenominator);

        // Meeus pg. 88
        // theta - alpha
        dblRaDegrees = (last * DEGREES_PER_HOUR) - dblLocalHourAngleDegrees;

        // Normalise Right Ascension in degrees to the range {0.0 ... 360.0}
        dblRaDegrees = normaliseRaDegrees(dblRaDegrees);
//        LOGGER.log("convertAzElToRaDec() [ra_norm=" + (new HourMinSecDataType(dblRaDegrees / DEGREES_PER_HOUR)).toString()
//                        + "] [lha_norm=" + dblLocalHourAngleDegrees + "] [lha_norm1=" + dblLocalHourAngleDegrees + "]");

        //-----------------------------------------------------------------------------------------
        // Declination
        // See: Jean Meeus Astronomical Algorithms (after 12.6, not numbered)
        // sin(phi) sin(h) - cos(phi) cos(h) cos(A)

        dblDecDegrees = (AstroMath.sind(latitude) * AstroMath.sind(dblElevation))
                        - (AstroMath.cosd(latitude) * AstroMath.cosd(dblElevation) * AstroMath.cosd(dblChauvenetAzimuth));

        dblDecDegrees = AstroMath.asind(dblDecDegrees);

        // (Ra, Dec) RA in HOURS, Dec in DEGREES
        return (new Point2D.Double(dblRaDegrees / DEGREES_PER_HOUR,
                                   dblDecDegrees));
        }


    /***********************************************************************************************
     * Convert a single point Galactic (l, b) Longitude, Latitude
     * to (Ra, Dec) RA in HOURS, Dec in DEGREES.
     * See: Meeus pp 90.
     *
     * @param pointgalactic
     *
     * @return Point2D.Double
     */

    public static Point2D.Double convertGalacticToRaDec(final Point2D.Double pointgalactic)
        {
        final double SIN27p4 = 0.46019978478385164567409136144904;
        final double COS27p4 = 0.88781538513640133208124598804415;
        final Point2D.Double pointRaDec;
        final double dblLongitudeDegrees;
        double dblRADegrees;
        double dblDeclinationDegrees;

        dblLongitudeDegrees = pointgalactic.getX() - 123.0;

        dblRADegrees = 12.25 + AstroMath.atan2d(AstroMath.sind(dblLongitudeDegrees),
                                                (AstroMath.cosd(dblLongitudeDegrees) * SIN27p4)
                                                    - (AstroMath.tand(pointgalactic.getY()) * COS27p4));

        // Normalise Right Ascension in degrees to the range {0.0 ... 360.0}
        dblRADegrees = normaliseRaDegrees(dblRADegrees);

        dblDeclinationDegrees = AstroMath.asind(AstroMath.sind(pointgalactic.getY()) * SIN27p4
                                                + AstroMath.cosd(pointgalactic.getY()) * AstroMath.cosd(dblLongitudeDegrees) * COS27p4);

        // Normalise Declination in degrees to the range {-90.0 ... +90.0}
        dblDeclinationDegrees = normaliseDecDegrees(dblDeclinationDegrees);

        // RA in HOURS, Dec in DEGREES
        pointRaDec = new Point2D.Double(dblRADegrees / DEGREES_PER_HOUR,
                                        dblDeclinationDegrees);

        // ToDo Precess to J2000

        return (pointRaDec);
        }


    /**********************************************************************************************/
    /* StarMap Viewport Transformations                                                           */
    /***********************************************************************************************
     * Convert a single point (x, y) in the StarMapViewport to (Az, El) in DEGREES.
     *
     * @param viewport
     * @param pointxy
     *
     * @return Point2D.Double
     */

    public static Point2D.Double transformViewportXYtoAzEl(final StarMapViewportInterface viewport,
                                                           final Point pointxy)
        {
        double dblAzimuth;
        double dblElevation;

        dblAzimuth = 0.0;
        dblElevation = 0.0;

        if ((viewport != null)
            && (pointxy != null))
            {
            dblAzimuth = viewport.getAzimuthEast() + (double)(pointxy.x) / viewport.getHorizPixelsPerDegree();
            dblElevation = viewport.getElevationNorth() - ((double)(pointxy.y) / viewport.getVertPixelsPerDegree());
            }

        return (new Point2D.Double(dblAzimuth, dblElevation));
        }


    /***********************************************************************************************
     * Convert a single point (x, y) in the StarMapViewport to (Az, El) in DEGREES.
     *
     * @param viewport
     * @param pointxy
     *
     * @return Point2D.Double
     */

    public static Point2D.Double transformViewportXYtoAzEl(final StarMapViewportInterface viewport,
                                                           final Point2D.Double pointxy)
        {
        double dblAzimuth;
        double dblElevation;

        dblAzimuth = 0.0;
        dblElevation = 0.0;

        if ((viewport != null)
            && (pointxy != null))
            {
            dblAzimuth = viewport.getAzimuthEast() + (pointxy.x / viewport.getHorizPixelsPerDegree());
            dblElevation = viewport.getElevationNorth() - (pointxy.y / viewport.getVertPixelsPerDegree());
            }

        return (new Point2D.Double(dblAzimuth, dblElevation));
        }


    /**********************************************************************************************/
    /* Transform TO Viewport (x, y)                                                               */
    /***********************************************************************************************
     * Transform the specified Collection of StarMapPoints to (x, y),
     * to be displayed in an area defined by a StarMapViewport,
     * for the observer's latitude at the specified calendar instant.
     * viewport.updatePixelViewportAndRemoveScales() MUST have been called first!
     * clickCoordinates[][] must have been initialised.
     *
     * @param collection
     * @param viewport
     * @param calendar
     * @param latitude
     * @param originisnorth
     * @param clickcoordinates
     */

    public static void transformToViewportXY(final Vector<Vector<StarMapPointInterface>> collection,
                                             final StarMapViewportInterface viewport,
                                             final AstronomicalCalendarInterface calendar,
                                             final double latitude,
                                             final boolean originisnorth,
                                             final StarMapPointInterface[][] clickcoordinates)
        {
        final String SOURCE = "CoordinateConversions.transformToViewportXY() ";

        // Check that we have some data loaded into the plugin!
        if ((collection != null)
            && (!collection.isEmpty())
            && (viewport != null)
            && (calendar != null)
            && (clickcoordinates != null))
            {
            final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

            iterWholeCollection = collection.iterator();

            while ((iterWholeCollection != null)
                   && (iterWholeCollection.hasNext()))
                {
                final Vector<StarMapPointInterface> singleCollection;

                singleCollection = iterWholeCollection.next();

                // Read the set of StarMapPoints to be converted
                // See if we can transform one set from the collection
                if ((singleCollection != null)
                    && (!singleCollection.isEmpty()))
                    {
                    final Iterator<StarMapPointInterface> iterSingleCollection;

                    iterSingleCollection = singleCollection.iterator();

                    while ((iterSingleCollection != null)
                           && (iterSingleCollection.hasNext()))
                        {
                        final StarMapPointInterface pointToTransform;

                        pointToTransform = iterSingleCollection.next();

                        // Transform from the original CoordinateType to (x, y) pixels in the Viewport
                        switch (pointToTransform.getCoordinateType())
                            {
                            case UNASSIGNED:
                                {
                                //LOGGER.warn(SOURCE + "CoordinateType UNASSIGNED in use, no action taken [name=" + pointToTransform.getName() + "]");
                                break;
                                }

                            case AZEL:
                            case AZEL_METADATA:
                                {
                                // Time and place are ignored
                                transformAzElToViewport(pointToTransform,
                                                        viewport,
                                                        clickcoordinates);
                                break;
                                }

                            case RADEC:
                            case RADEC_METADATA:
                                {
                                transformRaDecToViewport(pointToTransform,
                                                         viewport,
                                                         calendar,
                                                         latitude,
                                                         originisnorth,
                                                         clickcoordinates);
                                break;
                                }

                            case GALACTIC:
                            case GALACTIC_METADATA:
                                {
                                transformGalacticToViewport(pointToTransform,
                                                            viewport,
                                                            calendar,
                                                            latitude,
                                                            originisnorth,
                                                            clickcoordinates);
                                break;
                                }

                            default:
                                {
                                LOGGER.error(SOURCE + "Invalid CoordinateType [type=" + pointToTransform.getCoordinateType().getName()
                                                +  "] [name=" + pointToTransform.getName() + "]");
                                }
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Transform the Vector of StarMapPoint(Az, El) to (x, y).
     * To be displayed in an area defined by a StarMapViewport,
     * for the observer's latitude at the specified calendar instant.
     * viewport.updatePixelViewportAndRemoveScales() MUST have been called first!
     * clickCoordinates[][] must have been initialised.
     * Currently used only in GpsSatellites.
     *
     * ToDo Implement OriginIsNorth
     *
     * @param collection
     * @param viewport
     * @param calendar
     * @param originisnorth
     * @param latitude
     * @param clickcoordinates
     */

    public static void transformAzElToViewportXY(final Vector<Vector<StarMapPointInterface>> collection,
                                                 final StarMapViewportInterface viewport,
                                                 final AstronomicalCalendarInterface calendar,
                                                 final boolean originisnorth,
                                                 final double latitude,
                                                 final StarMapPointInterface[][] clickcoordinates)
        {
        // Check that we have some data loaded into the plugin!
        if ((collection != null)
            && (!collection.isEmpty())
            && (viewport != null)
            && (calendar != null)
            && (clickcoordinates != null))
            {
            final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

            iterWholeCollection = collection.iterator();

            while ((iterWholeCollection != null)
                && (iterWholeCollection.hasNext()))
                {
                final Vector<StarMapPointInterface> singleCollection;

                singleCollection = iterWholeCollection.next();

                // Read the set of (Az, El) to be converted
                // See if we can transform one set from the collection
                if ((singleCollection != null)
                    && (!singleCollection.isEmpty()))
                    {
                    final Iterator<StarMapPointInterface> iterSingleCollection;

                    iterSingleCollection = singleCollection.iterator();

                    while ((iterSingleCollection != null)
                        && (iterSingleCollection.hasNext()))
                        {
                        final StarMapPointInterface pointAzEl;

                        pointAzEl = iterSingleCollection.next();
                        transformAzElToViewport(pointAzEl, viewport, clickcoordinates);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Transform a single StarMapPoint(Az, El) to (x, y) for the Viewport.
     *
     * @param point
     * @param viewport
     * @param clickcoordinates
     */

    private static void transformAzElToViewport(final StarMapPointInterface point,
                                                final StarMapViewportInterface viewport,
                                                final StarMapPointInterface[][] clickcoordinates)
        {
        final String SOURCE = "CoordinateConversions.transformAzElToViewport() ";

        if ((point != null)
            && (viewport != null)
            && (clickcoordinates != null))
            {
            final double dblAzimuthDegrees;

            // Read the Azimuth, for conversion to (x)
            dblAzimuthDegrees = point.getCoordinates().x;

            // To save time, check that this point is actually displayable
            // Just move to the next point if not
            if ((dblAzimuthDegrees >= viewport.getAzimuthEast())
                && (dblAzimuthDegrees <= viewport.getAzimuthWest()))
                {
                final double dblElevationDegrees;
                final double dblViewportX;  // The double X value for an object
                final double dblViewportY;  // The double Y value for an object
                final int intPixelX;        // The integer pixel X value for an object
                final int intPixelY;        // The integer pixel Y value for an object

                // Convert Azimuth to pixels *relative to the viewport*
                dblViewportX = (dblAzimuthDegrees - viewport.getAzimuthEast()) * viewport.getHorizPixelsPerDegree();
                intPixelX = (int)dblViewportX;

                // Read the Elevation, for conversion to (y)
                dblElevationDegrees = point.getCoordinates().y;

                // To save more time, check that this point is actually displayable
                // Just move to the next point if not
                if ((dblElevationDegrees >= viewport.getElevationSouth())
                    && (dblElevationDegrees <= viewport.getElevationNorth()))
                    {

                    // Convert Elevation to pixels *relative to the viewport*
                    dblViewportY = Math.abs((viewport.getElevationNorth() - dblElevationDegrees) * viewport.getVertPixelsPerDegree());
                    intPixelY = (int)dblViewportY;

                    // Store the converted (x,y) and mark as Visible
                    point.setViewportXY(new Point2D.Double(dblViewportX, dblViewportY));
                    point.setPixelsXY(new Point(intPixelX, intPixelY));
                    point.setVisible(true);

                    // We don't want to click on the Grid for example
                    if (point.isClickable())
                        {
                        final int intAzimuth;
                        final int intElevation;

                        // Refer to the object from the clickable object array
                        intAzimuth = (int) AstroMath.truncate(dblAzimuthDegrees);
                        intElevation = (int) AstroMath.truncate(dblElevationDegrees);
                        clickcoordinates[intAzimuth][intElevation] = point;
                        }
                    }
                else
                    {
                    //LOGGER.debugTimedEvent("CoordinateConversions.transformAzElToViewportXY() [Elevation not visible]");
                    point.setVisible(false);
                    }
                }
            else
                {
                //LOGGER.debugTimedEvent("CoordinateConversions.transformAzElToViewportXY() [Azimuth not visible]");
                point.setVisible(false);
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Invalid Parameters");
            }
        }


    /***********************************************************************************************
     * Transform the Vector of StarMapPoint(Ra, Dec) to (x, y).
     * To be displayed in an area defined by a StarMapViewport,
     * for the observer's latitude at the specified calendar instant.
     * viewport.updatePixelViewportAndRemoveScales() MUST have been called first!
     * clickCoordinates[][] must have been initialised.
     * LAST and RA are in HOURS, DEC in DEGREES.
     *
     * @param collection
     * @param viewport
     * @param calendar
     * @param originisnorth
     * @param latitude
     * @param clickcoordinates
     */

    public static void transformRaDecToViewportXY(final Vector<Vector<StarMapPointInterface>> collection,
                                                  final StarMapViewportInterface viewport,
                                                  final AstronomicalCalendarInterface calendar,
                                                  final boolean originisnorth,
                                                  final double latitude,
                                                  final StarMapPointInterface[][] clickcoordinates)
        {
        // Check that we have some data loaded into the plugin!
        if ((collection != null)
            && (!collection.isEmpty())
            && (viewport != null)
            && (calendar != null)
            && (clickcoordinates != null))
            {
            final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

            iterWholeCollection = collection.iterator();

            while ((iterWholeCollection != null)
                && (iterWholeCollection.hasNext()))
                {
                final Vector<StarMapPointInterface> singleCollection;

                singleCollection = iterWholeCollection.next();

                // Read the set of (Ra, Dec) to be converted
                // See if we can transform one set from the collection
                if ((singleCollection != null)
                    && (!singleCollection.isEmpty()))
                    {
                    final Iterator<StarMapPointInterface> iterSingleCollection;

                    iterSingleCollection = singleCollection.iterator();

                    while ((iterSingleCollection != null)
                        && (iterSingleCollection.hasNext()))
                        {
                        final StarMapPointInterface pointRaDec;

                        pointRaDec = iterSingleCollection.next();

                        transformRaDecToViewport(pointRaDec,
                                                 viewport,
                                                 calendar,
                                                 latitude,
                                                 originisnorth,
                                                 clickcoordinates);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Transform a single StarMapPoint(Ra, Dec) to (x, y) for the Viewport.
     * LAST and RA are in HOURS, DEC in DEGREES.
     *
     * @param point
     * @param viewport
     * @param calendar
     * @param latitude
     * @param originisnorth
     * @param clickcoordinates
     */

    private static void transformRaDecToViewport(final StarMapPointInterface point,
                                                 final StarMapViewportInterface viewport,
                                                 final AstronomicalCalendarInterface calendar,
                                                 final double latitude,
                                                 final boolean originisnorth,
                                                 final StarMapPointInterface[][] clickcoordinates)
        {
        final String SOURCE = "CoordinateConversions.transformRaDecToViewport() ";

        if ((point != null)
            && (viewport != null)
            && (calendar != null)
            && (clickcoordinates != null))
            {
            double dblAzimuthDegrees;           // The calculated Azimuth of an object
            final double dblHourAngleDegrees;   // The calculated Hour Angle of an object

            // Calculate the Local Hour Angle of this point, measured Westwards from the South
            // LAST and RA are in HOURS, DEC in DEGREES
            // Jean Meeus Astronomical Algorithms pg 88
            dblHourAngleDegrees = (calendar.getLAST() - point.getCoordinates().x) * DEGREES_PER_HOUR;

            // Calculate the Azimuth from Ra, Dec and Latitude
            // Jean Meeus Astronomical Algorithms pg 89 eq. 12.5
            dblAzimuthDegrees = (AstroMath.cosd(dblHourAngleDegrees)
                                    * AstroMath.sind(latitude))
                                 - (AstroMath.tand(point.getCoordinates().y)
                                    * AstroMath.cosd(latitude));

            // ATan2() works out the correct quadrant...
            dblAzimuthDegrees = AstroMath.atan2d(AstroMath.sind(dblHourAngleDegrees), dblAzimuthDegrees);

            // Adjust the Azimuth for the origin at North or South
            // Jean Meeus Astronomical Algorithms pg 89 See note after eq. 12.6
            if (originisnorth)
                {
                dblAzimuthDegrees += 180.0;
                }

            // To save time, check that this point is actually displayable
            // Just move to the next point if not
            if ((dblAzimuthDegrees >= viewport.getAzimuthEast())
                && (dblAzimuthDegrees <= viewport.getAzimuthWest()))
                {
                final double dblElevation;         // The calculated Elevation of an object
                final double dblViewportX;  // The double X value for an object
                final double dblViewportY;  // The double Y value for an object
                final int intPixelX;        // The integer pixel X value for an object
                final int intPixelY;        // The integer pixel Y value for an object

                // Convert Azimuth to pixels, relative to top left viewport origin
                dblViewportX = (dblAzimuthDegrees - viewport.getAzimuthEast()) * viewport.getHorizPixelsPerDegree();
                intPixelX = (int)AstroMath.truncate(dblViewportX);

                // Calculate the Elevation from LHA, Dec and Latitude
                // Jean Meeus Astronomical Algorithms pg 89 eq. 12.6
                dblElevation = AstroMath.asind((AstroMath.sind(latitude)
                                                * AstroMath.sind(point.getCoordinates().y))
                                              + (AstroMath.cosd(latitude)
                                                * AstroMath.cosd(point.getCoordinates().y)
                                                * AstroMath.cosd(dblHourAngleDegrees)));

                // To save more time, check that this point is actually displayable
                // Just move to the next point if not
                if ((dblElevation >= viewport.getElevationSouth())
                    && (dblElevation <= viewport.getElevationNorth()))
                    {
                    // Convert Elevation to pixels, relative to top left viewport origin
                    dblViewportY = Math.abs((viewport.getElevationNorth() - dblElevation) * viewport.getVertPixelsPerDegree());
                    intPixelY = (int)AstroMath.truncate(dblViewportY);

                    // Store the converted (x,y) and mark as Visible
                    point.setViewportXY(new Point2D.Double(dblViewportX, dblViewportY));
                    point.setPixelsXY(new Point(intPixelX, intPixelY));
                    point.setVisible(true);

                    // Some things are not clickable, so check
                    if (point.isClickable())
                        {
                        final int intAzimuth;
                        final int intElevation;

                        // Refer to the object from the clickable object array
                        intAzimuth = (int) AstroMath.truncate(dblAzimuthDegrees);
                        intElevation = (int) AstroMath.truncate(dblElevation);
                        clickcoordinates[intAzimuth][intElevation] = point;
                        }
                    }
                else
                    {
                    point.setVisible(false);
                    }
                }
            else
                {
                point.setVisible(false);
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Invalid Parameters");
            }
        }


    /***********************************************************************************************
     * Transform the Vector of StarMapPoint(l, b) to (x, y).
     * To be displayed in an area defined by a StarMapViewport,
     * for the observer's latitude at the specified calendar instant.
     * viewport.updatePixelViewportAndRemoveScales() MUST have been called first!
     * clickCoordinates[][] must have been initialised.
     * LAST and RA are in HOURS, DEC in DEGREES.
     *
     * @param collection
     * @param viewport
     * @param calendar
     * @param originisnorth
     * @param latitude
     * @param clickcoordinates
     */

    public static void transformGalacticToViewportXY(final Vector<Vector<StarMapPointInterface>> collection,
                                                     final StarMapViewportInterface viewport,
                                                     final AstronomicalCalendarInterface calendar,
                                                     final boolean originisnorth,
                                                     final double latitude,
                                                     final StarMapPointInterface[][] clickcoordinates)
        {
        // Check that we have some data loaded into the plugin!
        if ((collection != null)
            && (!collection.isEmpty())
            && (viewport != null)
            && (calendar != null)
            && (clickcoordinates != null))
            {
            final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

            iterWholeCollection = collection.iterator();

            while ((iterWholeCollection != null)
                   && (iterWholeCollection.hasNext()))
                {
                final Vector<StarMapPointInterface> singleCollection;

                singleCollection = iterWholeCollection.next();

                // Read the set of (l, b) to be converted
                // See if we can transform one set from the collection
                if ((singleCollection != null)
                    && (!singleCollection.isEmpty()))
                    {
                    final Iterator<StarMapPointInterface> iterSingleCollection;

                    iterSingleCollection = singleCollection.iterator();

                    while ((iterSingleCollection != null)
                           && (iterSingleCollection.hasNext()))
                        {
                        final StarMapPointInterface pointGalactic;

                        pointGalactic = iterSingleCollection.next();

                        transformGalacticToViewport(pointGalactic,
                                                    viewport,
                                                    calendar,
                                                    latitude,
                                                    originisnorth,
                                                    clickcoordinates);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Transform a single Galactic StarMapPoint (l, b) to (x, y) for the Viewport.
     *
     * @param point
     * @param viewport
     * @param calendar
     * @param latitude
     * @param originisnorth
     * @param clickcoordinates
     */

    private static void transformGalacticToViewport(final StarMapPointInterface point,
                                                    final StarMapViewportInterface viewport,
                                                    final AstronomicalCalendarInterface calendar,
                                                    final double latitude,
                                                    final boolean originisnorth,
                                                    final StarMapPointInterface[][] clickcoordinates)
        {
        final String SOURCE = "CoordinateConversions.transformGalacticToViewport() ";

        if ((point != null)
            && (viewport != null)
            && (calendar != null)
            && (clickcoordinates != null))
            {
            final Point2D.Double pointRaDec;
            final StarMapPointInterface pointClone;

            // Use existing code for the RaDec transformation
            pointRaDec = convertGalacticToRaDec(point.getCoordinates());

            pointClone = new StarMapPoint(point);
            pointClone.setCoordinateType(CoordinateType.RADEC);
            pointClone.setCoordinates(pointRaDec);

            transformRaDecToViewport(pointClone,
                                     viewport,
                                     calendar,
                                     latitude,
                                     originisnorth,
                                     clickcoordinates);

            // Recover the transformed data from the clone point
            point.setViewportXY(pointClone.getViewportXY());
            point.setPixelsXY(pointClone.getPixelsXY());
            point.setVisible(pointClone.isVisible());
            }
        }


    /**********************************************************************************************/
    /* Normalisation                                                                              */
    /***********************************************************************************************
     * Normalise an angle in degrees to the range {0.0 ... 360.0}.
     *
     * @param degrees
     *
     * @return double
     */

    public static double normalise360Degrees(final double degrees)
        {
        return (normaliseAzimuthDegrees(degrees));
        }


    /***********************************************************************************************
     * Normalise an Azimuth in degrees to the range {0.0 ... 360.0}.
     *
     * @param azimuth
     *
     * @return double
     */

    public static double normaliseAzimuthDegrees(final double azimuth)
        {
        double dblAzimuth;

        dblAzimuth = azimuth;

        while (dblAzimuth < AZI_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() Azimuth too small [value=" + azimuth + "]");
            dblAzimuth += AZI_MAX;
            }

        while (dblAzimuth >= AZI_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() Azimuth too large [value=" + azimuth + "]");
            dblAzimuth -= AZI_MAX;
            }

        return (dblAzimuth);
        }


    /***********************************************************************************************
     * Normalise an Elevation in degrees to the range {0.0 ... 90.0}.
     *
     * @param elevation
     *
     * @return double
     */

    public static double normaliseElevationDegrees(final double elevation)
        {
        double dblElevation;

        dblElevation = elevation;

        while (dblElevation < ELEV_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() Elevation too small [value=" + elevation + "]");
            dblElevation += ELEV_MAX;
            }

        while (dblElevation >= ELEV_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() Elevation too large [value=" + elevation + "]");
            dblElevation -= ELEV_MAX;
            }

        return (dblElevation);
        }


    /***********************************************************************************************
     * Normalise a Longitude in degrees to the range {0.0 ... 360.0}.
     *
     * @param longitude
     *
     * @return double
     */

    public static double normaliseLongitudeDegrees(final double longitude)
        {
        double dblLongitude;

        dblLongitude = longitude;

        while (dblLongitude < LONGITUDE_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() Longitude too small [value=" + longitude + "]");
            dblLongitude += LONGITUDE_MAX;
            }

        while (dblLongitude >= LONGITUDE_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() Longitude too large [value=" + longitude + "]");
            dblLongitude -= LONGITUDE_MAX;
            }

        return (dblLongitude);
        }


    /***********************************************************************************************
     * Normalise a Latitude in degrees to the range {-90.0 ... +90.0}.
     *
     * @param latitude
     *
     * @return double
     */

    public static double normaliseLatitudeDegrees(final double latitude)
        {
        double dblLatitude;

        dblLatitude = latitude;

        while (dblLatitude < LATITUDE_RANGE_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() Latitude too small [value=" + latitude + "]");
            dblLatitude += LATITUDE_RANGE_MAX;
            }

        while (dblLatitude >= LATITUDE_RANGE_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() Latitude too large [value=" + latitude + "]");
            dblLatitude -= LATITUDE_RANGE_MAX;
            }

        return (dblLatitude);
        }


    /***********************************************************************************************
     * Normalise a Right Ascension in degrees to the range {0.0 ... 360.0}.
     *
     * @param ra
     *
     * @return double
     */

    public static double normaliseRaDegrees(final double ra)
        {
        double dblRA;

        dblRA = ra;

        while (dblRA < RA_DEG_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() RA too small [value=" + ra + "]");
            dblRA += RA_DEG_MAX;
            }

        while (dblRA >= RA_DEG_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() RA too large [value=" + ra + "]");
            dblRA -= RA_DEG_MAX;
            }

        return(dblRA);
        }


    /***********************************************************************************************
     * Normalise a Right Ascension in Hours to the range {0.0 ... 24.0}.
     *
     * @param ra
     *
     * @return double
     */

    public static double normaliseRaHours(final double ra)
        {
        double dblRA;

        dblRA = ra;

        while (dblRA < RA_HOUR_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() RA too small [value=" + ra + "]");
            dblRA += RA_HOUR_MAX;
            }

        while (dblRA >= RA_HOUR_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() RA too large [value=" + ra + "]");
            dblRA -= RA_HOUR_MAX;
            }

        return(dblRA);
        }


    /***********************************************************************************************
     * Normalise a Declination in degrees to the range {-90.0 ... +90.0}.
     *
     * @param dec
     *
     * @return double
     */

    public static double normaliseDecDegrees(final double dec)
        {
        double dblDeclination;

        dblDeclination = dec;

        while (dblDeclination < DEC_MIN)
            {
            //LOGGER.log("normaliseRaDegrees() Declination too small [value=" + dec + "]");
            dblDeclination += DEC_MAX;
            }

        while (dblDeclination >= DEC_MAX)
            {
            //LOGGER.log("normaliseRaDegrees() Declination too large [value=" + dec + "]");
            dblDeclination -= DEC_MAX;
            }

        return(dblDeclination);
        }


    /***********************************************************************************************
     * Parse and validate the entered JD String.
     * Return -1.0 if the JD is not valid.
     * Allow 4713 BC January 1 to 2132 August 31.
     * See: http://en.wikipedia.org/wiki/Julian_day
     *
     * @param jd
     *
     * @return boolean
     */

    public static double parseJD(final String jd)
        {
        double dblJD;

        try
            {
            dblJD = Double.parseDouble(jd);

            // Allow 4713 BC January 1 to 2132 August 31 (!)
            if ((dblJD < 0.0)
                || (dblJD > 2500000.0))
                {
                dblJD = -1.0;
                }
            }

        catch (NumberFormatException exception)
            {
            dblJD = -1.0;
            }

        return (dblJD);
        }


    /***********************************************************************************************
     * Convert Degrees to Radians.
     *
     * @param degrees
     *
     * @return double
     */

    public static double convertDegreesToRadians(final double degrees)
        {
        return (degrees * 0.017453292519943295769236907684886);
        }


    /***********************************************************************************************
     * Convert Radians to Degrees.
     *
     * @param radians
     *
     * @return double
     */

    public static double convertRadiansToDegrees(final double radians)
        {
        return (radians * 57.295779513082320876798154814105);
        }

    }


//function maidenhead
//% conversion long/lat en degr� d�cimaux vers maidenhead grid
//% longitude positive vers l'est (n�gative vers l'ouest)
//% latitude positive vers le nord (n�gative vers le sud)
//% Y.OESCH / 30.5.2002
//
//%===============================        choisir un exemple au choix ou en modifier un
//%                                       supprimer les % en debut de ligne
//%long=6.46673           %Suchet: JN36FS
//%lat=46.77219
//
//%long=6.62556           %Villars: JN36HP
//%lat=46.65389
//
//%long=6.63355           %Lsne: JN36HM
//%lat=46.51979
//
//%long=7.47194           %lommiswil: JN37RF
//%lat=47.22470
//
//%long=-41.683           %GG97DO
//%lat=-22.40
//
//%long=-97.73            %EM10DG
//%lat=30.266
//
//%long=7.00170            %ASULAB, Marin (NE)
//%lat=47.01248
//%================================
//
//long_mh=long+180;                           %d�callage de la grille
//lat_mh=lat+90;
//
//digit1=floor(long_mh/20)+1;                 %1er digit (tranche de 20 degr�s longitude)
//char1=char(digit1+64);                      %conversion en alphab�tique
//
//digit2=floor(lat_mh/10)+1;                  %2�me digit (tranche de 10 degr�s latitude)
//char2=char(digit2+64);                      %conversion en alphab�tique
//
//digit3=floor(mod(long_mh,20)/2);            %3�me digit (tranche de 2 degr� longitude)
//char3=num2str(digit3);                      %conversion ASCII
//
//digit4=floor(mod(lat_mh,10)/1);             %4�me digit (tranche de 1 degr� latitude)
//char4=num2str(digit4);                      %conversion ASCII
//
//digit5=floor(mod(long_mh,2)*60/5)+1;        %5�me digit (tranche de 5 minutes d'arc longitude)
//char5=char(digit5+64);                      %conversion en alphab�tique
//
//digit6=floor(mod(lat_mh,1)/1*60/2.5)+1;     %6�me digit (tranche de 2.5 minutes d'arc latitude)
//char6=char(digit6+64);                      %conversion en alphab�tique
//
//strcat(char1,char2,char3,char4,char5,char6) %concat�nation et affichage