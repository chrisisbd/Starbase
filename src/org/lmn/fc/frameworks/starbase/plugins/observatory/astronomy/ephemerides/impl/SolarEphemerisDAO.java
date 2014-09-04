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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.impl;


import org.lmn.fc.common.utilities.astronomy.AstroUtilities;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.AbstractEphemerisDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.DeclinationDataType;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * SolarEphemerisDAO.
 * All routines to calculate the position of the Sun.
 */

public class SolarEphemerisDAO extends AbstractEphemerisDAO
                               implements EphemerisDAOInterface
    {
    // This is parsed as a Declination, but is just a plain Angle in DegMinSec form for convenience
    private static final String OBLIQUITY_OF_ECLIPTIC_J2000 = "+23:26:21.448";


    /***********************************************************************************************
     * Calculate the position of the Sun given the JulianDay.
     * Astronomical Algorithms Jean Meeus Chapter 24: Solar Coordinates.
     * Meeus' test JD is dblJD = 2448908.5.
     * Return a Point2D.Double(Ra, Dec).
     * The Right Ascension is returned in HOURS, Declination in DEGREES.
     *
     * See: http://aa.usno.navy.mil/data/docs/AltAz.php
     * for confirmation, which seems within 0.5deg in Elevation,
     * the difference probably caused by having no correction for refraction etc.
     *
     * See: http://www.jgiesen.de/elevaz/index.htm
     *
     * @param dblJD
     *
     * @return Point2D.Double
     */

    public static Point2D.Double calculateSunPositionRaDec(final double dblJD)
        {
        final double dblT;
        double dblL0;
        double dblMeanAnomaly;
        final double dblEccentricity;
        final double dblEquationOfCentre;
        double dblTrueLongitude;
        double dblTrueAnomaly;
        final double dblRadiusVector;
        final double dblOmega;
        double dblApparentLongitude;
        double dblObliquityOfEcliptic;
        double dblTrueRightAscension;
        final double dblTrueDeclination;
        double dblApparentRightAscension;
        final double dblApparentDeclination;
        final DegMinSecInterface dmsObliquity;
        final List<String> errors;

        errors = new ArrayList<String>(10);

        //LOGGER.log("SUN calculateSunPositionRaDec [JD=" + dblJD + "]");

        // Convert JD0 above to Julian centuries since 2000
        dblT = AstroUtilities.J2000Centuries(dblJD);
        //LOGGER.debugTimedEvent("[dblT=" + dblT + "]");

        // Geometric mean longitude of the Sun, referred to mean equinox of date
        dblL0 = 280.46645
                + (36000.76983 * dblT)
                + (0.0003032 * dblT * dblT);
        //LOGGER.debugTimedEvent("[dblL0=" + dblL0 + "] degrees");
        dblL0 = AstroUtilities.adjustRange(dblL0, 360.0);
        //LOGGER.debugTimedEvent("[dblL0=" + dblL0 + "] degrees");

        // Mean anomaly of the Sun
        dblMeanAnomaly = 357.52910
                         + (35999.05030 * dblT)
                         - (0.0001559 * dblT * dblT)
                         -(0.00000048 * dblT * dblT * dblT);
        //LOGGER.debugTimedEvent("[dblMeanAnomaly=" + dblMeanAnomaly + "] degrees");
        dblMeanAnomaly = AstroUtilities.adjustRange(dblMeanAnomaly, 360.0);
        //LOGGER.debugTimedEvent("[dblMeanAnomaly=" + dblMeanAnomaly + "] degrees");

        // Eccentricity of the Earth's orbit
        dblEccentricity = 0.016708617
                          - (0.000042037 * dblT)
                          - (0.0000001236 * dblT * dblT);
        //LOGGER.debugTimedEvent("[dblEccentricity=" + dblEccentricity + "]");

        // The Sun's Equation of Centre
        dblEquationOfCentre = (1.914600
                                - 0.004817 * dblT
                                - 0.000014 * dblT * dblT) * AstroMath.sind(dblMeanAnomaly)
                              + (0.019993
                                 - 0.000101 * dblT) * AstroMath.sind(2.0 * dblMeanAnomaly)
                              + (0.000290 * AstroMath.sind(3.0 * dblMeanAnomaly));
        //LOGGER.debugTimedEvent("[dblEquationOfCentre=" + dblEquationOfCentre + "] degrees");

        // The Sun's true Longitude
        dblTrueLongitude = dblL0 + dblEquationOfCentre;
        //LOGGER.debugTimedEvent("[dblTrueLongitude=" + dblTrueLongitude + "] degrees");
        dblTrueLongitude = AstroUtilities.adjustRange(dblTrueLongitude, 360.0);
        //LOGGER.debugTimedEvent("[dblTrueLongitude=" + dblTrueLongitude + "] degrees");

        // The Sun's True Anomaly
        dblTrueAnomaly = dblMeanAnomaly + dblEquationOfCentre;
        //LOGGER.debugTimedEvent("[dblTrueAnomaly=" + dblTrueAnomaly + "] degrees");
        dblTrueAnomaly = AstroUtilities.adjustRange(dblTrueAnomaly, 360.0);
        //LOGGER.debugTimedEvent("[dblTrueAnomaly=" + dblTrueAnomaly + "] degrees");

        // The Sun's Radius Vector
        dblRadiusVector = 1.000001018 * (1 -(dblEccentricity * dblEccentricity))
                          / (1 + (dblEccentricity * AstroMath.cosd(dblTrueAnomaly)));
        //LOGGER.debugTimedEvent("[dblRadiusVector=" + dblRadiusVector + "]");

        // Omega
        dblOmega = 125.04 - (1934.136 * dblT);
        //LOGGER.debugTimedEvent("[dblOmega=" + dblOmega + "]");

        // The Sun's Apparent Longitude, referred to true Equinox of the date
        dblApparentLongitude = dblTrueLongitude
                                - 0.00569
                                - (0.00478 * AstroMath.sind(dblOmega));
        //LOGGER.debugTimedEvent("[dblApparentLongitude=" + dblApparentLongitude + "] degrees");
        dblApparentLongitude = AstroUtilities.adjustRange(dblApparentLongitude, 360.0);
        //LOGGER.debugTimedEvent("[dblApparentLongitude=" + dblApparentLongitude + "] degrees");

        // The Obliquity of the Ecliptic
        // This is parsed as a Declination, but is just a plain Angle in DegMinSec form for convenience
        // ToDo use improved algorithm in AstroUtilities?
        dmsObliquity = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(OBLIQUITY_OF_ECLIPTIC_J2000,
                                                                                       DataTypeDictionary.DECLINATION,
                                                                                       EMPTY_STRING,
                                                                                       EMPTY_STRING,
                                                                                       errors);
        // We don't need to set the display formats because we only need the double values

        dblObliquityOfEcliptic = dmsObliquity.toDouble()
                                 - ((46.8150 * dblT)/3600.0)
                                 - ((0.00059 * dblT * dblT)/3600.0)
                                 + ((0.001813 * dblT * dblT * dblT)/3600.0);
        //LOGGER.debugTimedEvent("[dblObliquityOfEcliptic=" + dblObliquityOfEcliptic + "] degrees");

        // The Sun's True Right Ascension
        // ATan2() works out the correct quadrant...
        dblTrueRightAscension = AstroMath.atan2d(AstroMath.cosd(dblObliquityOfEcliptic) * AstroMath.sind(dblTrueLongitude),
                                                 AstroMath.cosd(dblTrueLongitude));
        //LOGGER.debugTimedEvent("[dblTrueRightAscension=" + dblTrueRightAscension + "] degrees");
        dblTrueRightAscension = AstroUtilities.adjustRange(dblTrueRightAscension, 360.0);
        //LOGGER.debugTimedEvent("[dblTrueRightAscension=" + dblTrueRightAscension + "] degrees");

        // The Sun's True Declination
        dblTrueDeclination = AstroMath.asind(AstroMath.sind(dblObliquityOfEcliptic) * AstroMath.sind(dblTrueLongitude));
        //LOGGER.debugTimedEvent("[dblTrueDeclination=" + dblTrueDeclination + "] degrees");

        // Correct the Obliquity of the Ecliptic for the apparent position
        dblObliquityOfEcliptic = dblObliquityOfEcliptic
                                 + (0.00256 * AstroMath.cosd(dblOmega));
        //LOGGER.debugTimedEvent("[dblObliquityOfEcliptic=" + dblObliquityOfEcliptic + "] degrees");

        // The Sun's Apparent Right Ascension
        // ATan2() works out the correct quadrant...
        dblApparentRightAscension = AstroMath.atan2d(AstroMath.cosd(dblObliquityOfEcliptic) * AstroMath.sind(dblApparentLongitude),
                                                     AstroMath.cosd(dblApparentLongitude));
        //LOGGER.debugTimedEvent("[dblApparentRightAscension=" + dblApparentRightAscension + "] degrees");
        dblApparentRightAscension = AstroUtilities.adjustRange(dblApparentRightAscension, 360.0);
        //LOGGER.debugTimedEvent("[dblApparentRightAscension=" + dblApparentRightAscension + "] degrees");

        // Convert to Hours for the return()
        dblApparentRightAscension = (dblApparentRightAscension*24.0)/360.0;

        final HourMinSecDataType hmsRightAscension = new HourMinSecDataType(dblApparentRightAscension);
        //LOGGER.log("SUN [RA=" + hmsRightAscension.toString() + "] hours");

        // The Sun's Apparent Declination
        dblApparentDeclination = AstroMath.asind(AstroMath.sind(dblObliquityOfEcliptic) * AstroMath.sind(dblApparentLongitude));

        //LOGGER.debugTimedEvent("[dblApparentDeclination=" + dblApparentDeclination + "] degrees");
        final DeclinationDataType dmsDeclination = new DeclinationDataType(dblApparentDeclination);
        //LOGGER.log("SUN [Dec=" + dmsDeclination.toString() + "] degrees");

        return (new Point2D.Double(dblApparentRightAscension, dblApparentDeclination));
        }


    /***********************************************************************************************
     * SolarEphemerisDAO.
     *
     * @param ephemeris
     */

    public SolarEphemerisDAO(final Ephemeris ephemeris)
        {
        super(ephemeris);
        }


    /***********************************************************************************************
     * Recalculate the coordinates for the specified JD, as (Ra, Dec).
     *
     * @param juliandate
     * @param last
     * @param latitude
     *
     * @return CoordinateType
     */

    public CoordinateType recalculateForJulianDate(final double juliandate,
                                                   final double last,
                                                   final double latitude)
        {
        CoordinateType coordinateType;

        coordinateType = CoordinateType.UNASSIGNED;

        if (getEphemeris() != null)
            {
            final Point2D.Double pointCoords;

            // Ignore any coordinates which were supplied
            pointCoords = calculateSunPositionRaDec(juliandate);

            coordinateType = CoordinateType.RADEC;
            setCoordinates(pointCoords);
            }

        return (coordinateType);
        }
    }
