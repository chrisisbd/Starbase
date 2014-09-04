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


import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.astronomy.Nutation;
import org.lmn.fc.common.utilities.astronomy.SphericalCoordinates;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.AbstractEphemerisDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;

import java.awt.geom.Point2D;

//--------------------------------------------------------------------------------------------------
// Test Position 1992 April 12, 0h TD jde=2448724.5
// Jean Meeus Astronomical Algorithms First Edition Page 312
//
// SigmaL=-1127526.37604035
// SigmaB=-3229274.250365931
// SigmaR=-1.6590875474461436E7
// dblLambdaDegrees=133.1626598933649
// Nutation seconds=16.59454522457142
// dblLambdaApparentDegrees=133.1672694892606   (Meeus 133.167269)
// Beta=-3.229274250365931  (Meeus -3.229127)
// Delta=368409.6845255386  (Meeus 368408.4)
// Obliquity degrees=23.4402851835068
// True Obliquity degrees=23.440624891756293  (Meeus 23.440636)
// Nutation seconds=1.2229496981802601
// RA=8.979228621140342
// RA=08:58:45
// Dec=13.768216154290739
// Dec=13.8

/***************************************************************************************************
 * LunarEphemerisDAO.
 */

public class LunarEphemerisDAO extends AbstractEphemerisDAO
                               implements EphemerisDAOInterface
    {
    private static final int TERM_COUNT = 60;

    private static final int INDEX_COEFF_D = 0;
    private static final int INDEX_COEFF_M = 1;
    private static final int INDEX_COEFF_MDASH = 2;
    private static final int INDEX_COEFF_F = 3;


    /***********************************************************************************************
     * Table 45.A
     * Arguments for the Longitude and the Distance
     * D  M  M' F
     */

    private static final int[][] ARGUMENTS_LR =
        {
            {0, 0, 1, 0},
            {2, 0,-1, 0},
            {2, 0, 0, 0},
            {0, 0, 2, 0},
            {0, 1, 0, 0},
            {0, 0, 0, 2},
            {2, 0,-2, 0},
            {2,-1,-1, 0},
            {2, 0, 1, 0},
            {2,-1, 0, 0},

            {0, 1,-1, 0},
            {1, 0, 0, 0},
            {0, 1, 1, 0},
            {2, 0, 0,-2},
            {0, 0, 1, 2},
            {0, 0, 1,-2},
            {4, 0,-1, 0},
            {0, 0, 3, 0},
            {4, 0,-2, 0},
            {2, 1,-1, 0},

            {2, 1, 0, 0},
            {1, 0,-1, 0},
            {1, 1, 0, 0},
            {2,-1, 1, 0},
            {2, 0, 2, 0},
            {4, 0, 0, 0},
            {2, 0,-3, 0},
            {0, 1,-2, 0},
            {2, 0,-1, 2},
            {2,-1,-2, 0},

            {1, 0, 1, 0},
            {2,-2, 0, 0},
            {0, 1, 2, 0},
            {0, 2, 0, 0},
            {2,-2,-1, 0},
            {2, 0, 1,-2},
            {2, 0, 0, 2},
            {4,-1,-1, 0},
            {0, 0, 2, 2},
            {3, 0,-1, 0},

            {2, 1, 1, 0},
            {4,-1,-2, 0},
            {0, 2,-1, 0},
            {2, 2,-1, 0},
            {2, 1,-2, 0},
            {2,-1, 0,-2},
            {4, 0, 1, 0},
            {0, 0, 4, 0},
            {4,-1, 0, 0},
            {1, 0,-2, 0},

            {2, 1, 0,-2},
            {0, 0, 2,-2},
            {1, 1, 1, 0},
            {3, 0,-2, 0},
            {4, 0,-3, 0},
            {2,-1, 2, 0},
            {0, 2, 1, 0},
            {1, 1,-1, 0},
            {2, 0, 3, 0},
            {2, 0,-1,-2}
        };

    private static final int INDEX_COEFF_SIN = 0;
    private static final int INDEX_COEFF_COS = 1;


    /***********************************************************************************************
     * Periodic Terms for the Longitude and the Distance
     * Units 0.000001 degree for the SinCoeff, and 0.001km for the CosCoeff
     * SinCoeff  CosCoeff
     */

    private static final long[][] TERMS_LR =
        {
            { 6288774, -20905355},
            { 1274027,  -3699111},
            {  658314,  -2955968},
            {  213618,   -569925},
            { -185116,     48888},
            { -114332,     -3149},
            {   58793,    246158},
            {   57066,   -152138},
            {   53322,   -170733},
            {   45758,   -204586},

            {  -40923,   -129620},
            {  -34720,    108743},
            {  -30383,    104755},
            {   15327,     10321},
            {  -12528,         0},
            {   10980,     79661},
            {   10675,    -34782},
            {   10034,    -23210},
            {    8548,    -21636},
            {   -7888,     24208},

            {   -6766,     30824},
            {   -5163,     -8379},
            {    4987,    -16675},
            {    4036,    -12831},
            {    3994,    -10445},
            {    3861,    -11650},
            {    3665,     14403},
            {   -2689,     -7003},
            {   -2602,         0},
            {    2390,     10056},

            {   -2348,      6322},
            {    2236,     -9884},
            {   -2120,      5751},
            {   -2069,         0},
            {    2048,     -4950},
            {   -1773,      4130},
            {   -1595,         0},
            {    1215,     -3958},
            {   -1110,         0},
            {    -892,      3258},

            {    -810,      2616},
            {     759,     -1897},
            {    -713,     -2117},
            {    -700,      2354},
            {     691,         0},
            {     596,         0},
            {     549,     -1423},
            {     537,     -1117},
            {     520,     -1571},
            {    -487,     -1739},

            {    -399,         0},
            {    -381,     -4421},
            {     351,         0},
            {    -340,         0},
            {     330,         0},
            {     327,         0},
            {    -323,      1165},
            {     299,         0},
            {     294,         0},
            {       0,      8752}
        };


    /***********************************************************************************************
     * Table 45.B
     * Arguments for the Latitude
     * D  M  M' F
     */

    private static final int[][] ARGUMENTS_B =
        {
            {0, 0, 0, 1},
            {0, 0, 1, 1},
            {0, 0, 1,-1},
            {2, 0, 0,-1},
            {2, 0,-1, 1},
            {2, 0,-1,-1},
            {2, 0, 0, 1},
            {0, 0, 2, 1},
            {2, 0, 1,-1},
            {0, 0, 2,-1},

            {2,-1, 0,-1},
            {2, 0,-2,-1},
            {2, 0, 1, 1},
            {2, 1, 0,-1},
            {2,-1,-1, 1},
            {2,-1, 0, 1},
            {2,-1,-1,-1},
            {0, 1,-1,-1},
            {4, 0,-1,-1},
            {0, 1, 0, 1},

            {0, 0, 0, 3},
            {0, 1,-1, 1},
            {1, 0, 0, 1},
            {0, 1, 1, 1},
            {0, 1, 1,-1},
            {0, 1, 0,-1},
            {1, 0, 0,-1},
            {0, 0, 3, 1},
            {4, 0, 0,-1},
            {4, 0,-1, 1},

            {0, 0, 1,-3},
            {4, 0,-2, 1},
            {2, 0, 0,-3},
            {2, 0, 2,-1},
            {2,-1, 1,-1},
            {2, 0,-2, 1},
            {0, 0, 3,-1},
            {2, 0, 2, 1},
            {2, 0,-3,-1},
            {2, 1,-1, 1},

            {2, 1, 0, 1},
            {4, 0, 0, 1},
            {2,-1, 1, 1},
            {2,-2, 0,-1},
            {0, 0, 1, 3},
            {2, 1, 1,-1},
            {1, 1, 0,-1},
            {1, 1, 0, 1},
            {0, 1,-2,-1},
            {2, 1,-1,-1},

            {1, 0, 1, 1},
            {2,-1,-2,-1},
            {0, 1, 2, 1},
            {4, 0,-2,-1},
            {4,-1,-1,-1},
            {1, 0, 1,-1},
            {4, 0, 1,-1},
            {1, 0,-1,-1},
            {4,-1, 0, 1},
            {2,-2, 0, 1}
        };


    /***********************************************************************************************
     * Periodic terms for the Latitude
     * Units are 0.000001 degree
     * SinCoeff
     */

    private static final long[][] TERMS_B =
        {
            { 5128122},
            {  280602},
            {  277693},
            {  173237},
            {   55413},
            {   46271},
            {   32573},
            {   17198},
            {    9266},
            {    8822},

            {    8216},
            {    4324},
            {    4200},
            {   -3359},
            {    2463},
            {    2211},
            {    2065},
            {   -1870},
            {    1828},
            {   -1794},

            {   -1749},
            {   -1565},
            {   -1491},
            {   -1475},
            {   -1410},
            {   -1344},
            {   -1335},
            {    1107},
            {    1021},
            {     833},

            {     777},
            {     671},
            {     607},
            {     596},
            {     491},
            {    -451},
            {     439},
            {     422},
            {     421},
            {    -366},

            {    -351},
            {     331},
            {     315},
            {     302},
            {    -283},
            {    -229},
            {     223},
            {     223},
            {    -220},
            {    -220},

            {    -185},
            {     181},
            {    -177},
            {     176},
            {     166},
            {    -164},
            {     132},
            {    -119},
            {     115},
            {     107}
        };


    /***********************************************************************************************
     * Calculate the position of the Moon given the Julian Ephemeris Day.
     * Astronomical Algorithms Jean Meeus.
     * Return a Point2D.Double(Ra, Dec).
     * The Right Ascension is returned in HOURS, Declination in DEGREES.
     *
     * See: http://aa.usno.navy.mil/data/docs/AltAz.php
     * for confirmation, which seems within 0.5deg in Elevation,
     * the difference probably caused by having no correction for refraction etc.

     * @param jde
     *
     * @return Point2D.Double
     */

    private static Point2D.Double calculateMoonPositionRaDec(final double jde)
        {
        final SphericalCoordinates coordsLBR;
        final Point2D.Double pointRaDec;

        // Find out where the Moon is in {l, b, r}
        coordsLBR = calculateSphericalCoordinatesDegrees(jde);

        // Convert SphericalCoordinates (lambda, beta, delta) to (Ra, Dec)
        pointRaDec = CoordinateConversions.convertSphericalToRaDec(coordsLBR, jde);

        // LOGGER.log("MOON --> [RA=" + pointRaDec.x + "] [Dec=" + pointRaDec.y + "]");

        return (pointRaDec);
        }


    /***********************************************************************************************
     * Calculate the position of the Moon in spherical coordinates, for the specified Julian Date.
     *
     * See: http://aa.usno.navy.mil/data/docs/AltAz.php
     * for confirmation, which seems within 0.5deg in Elevation,
     * the difference probably caused by having no correction for refraction etc.
     *
     * @param jde
     *
     * @return SphericalCoordinates
     */

     private static SphericalCoordinates calculateSphericalCoordinatesDegrees(final double jde)
         {
         // Mean Longitude
         final double[] coeffsLdash =
                 {218.3164591,
                  481267.88134236,
                  -0.0013268,
                  1.855835e-6,
                  1.53388e-8};
         // Mean Longitude Ascending Node
         final double[] coeffs0m =
                 {125.0445550,
                  -1934.1361849,
                  0.0020762,
                  2.139449e-6,
                  1.64973e-8};
         // Elongation
         final double[] coeffsD =
                 {297.8502042,
                  445267.1115168,
                  -0.0016300,
                  1.831945e-6,
                  8.84447e-6};
         // Sun Mean Anomaly
         final double[] coeffsM =
                 {357.5291092,
                  35999.0502909,
                  -0.0001536,
                  4.083299e-8};
         // Mean Anomaly
         final double[] coeffsMdash =
                 {134.9634114,
                  477198.8676313,
                  0.0089970,
                  1.434741e-5,
                  6.79717e-8};
         // Argument of Latitude
         final double[] coeffsF =
                 {93.2720993,
                  483202.0175273,
                  -0.0034029,
                  2.836075e-7,
                  1.15833e-9};

         final double T;
         double Ldash;
         final double dblOm;
         double D;
         double M;
         double Mdash;
         double F;
         final double e;
         final double eSquared;
         double A1;
         double A2;
         double A3;
         double SigmaL;
         double SigmaB;
         double SigmaR;
         final double Ladditive;
         final double Badditive;
         final double dblLambdaDegrees;
         double dblLambdaApparentDegrees;
         final double dblBeta;
         final double dblDelta;
         final SphericalCoordinates coords;

        // Convert Julian Ephemeris Day to Julian centuries since 2000
         T = (jde - 2451545) / 36525;

         // Equation 45.1
         // Moon's mean longitude
         Ldash = CoordinateConversions.normalise360Degrees(evaluatePolynomial(coeffsLdash, coeffsLdash.length - 1, T));

         // Mean Longitude Ascending Node
         dblOm = CoordinateConversions.normalise360Degrees(evaluatePolynomial(coeffs0m, coeffs0m.length-1, T));

         // Equation 45.2
         D = CoordinateConversions.normalise360Degrees(evaluatePolynomial(coeffsD, coeffsD.length - 1, T));

         // Equation 45.3
         M = CoordinateConversions.normalise360Degrees(evaluatePolynomial(coeffsM, coeffsM.length - 1, T));

         // Equation 45.4
         Mdash = CoordinateConversions.normalise360Degrees(evaluatePolynomial(coeffsMdash, coeffsMdash.length - 1, T));

         // Equation 45.5
         F = CoordinateConversions.normalise360Degrees(evaluatePolynomial(coeffsF, coeffsF.length - 1, T));

         // Equation 45.6
         e = 1 - T * (0.002516 + (T * 0.0000074));
         eSquared = e * e;

         A1 = 119.75 + (131.849 * T);
         A2 = 53.09 + (479264.290 * T);
         A3 = 313.45 + (481266.484 * T);

         // Meeus doesn't say if this is required here,
         // but if it is left out then the values don't agree with his example 45.a
         A1 = CoordinateConversions.normalise360Degrees(A1);
         A2 = CoordinateConversions.normalise360Degrees(A2);
         A3 = CoordinateConversions.normalise360Degrees(A3);

         // Convert everything to Radians
         Ldash = CoordinateConversions.convertDegreesToRadians(Ldash);
         D = CoordinateConversions.convertDegreesToRadians(D);
         M = CoordinateConversions.convertDegreesToRadians(M);
         Mdash = CoordinateConversions.convertDegreesToRadians(Mdash);
         F = CoordinateConversions.convertDegreesToRadians(F);
         A1 = CoordinateConversions.convertDegreesToRadians(A1);
         A2 = CoordinateConversions.convertDegreesToRadians(A2);
         A3 = CoordinateConversions.convertDegreesToRadians(A3);

         // Accumulators for the results
         SigmaL = 0.0;
         SigmaB = 0.0;
         SigmaR = 0.0;

         //-----------------------------------------------------------------------------------------
         // Page 308

         for (int intTermIndex = 0;
              intTermIndex < TERM_COUNT;
              intTermIndex++)
            {
            double dblSumL;
            double dblSumR;
            double dblSumB;

            // Longitude L
            dblSumL = TERMS_LR[intTermIndex][INDEX_COEFF_SIN]
                      * Math.sin(ARGUMENTS_LR[intTermIndex][INDEX_COEFF_D] * D
                                    + ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] * M
                                    + ARGUMENTS_LR[intTermIndex][INDEX_COEFF_MDASH] * Mdash
                                    + ARGUMENTS_LR[intTermIndex][INDEX_COEFF_F] * F);

            // Eccentricity correction
            if ((ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == 1)
                || (ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == -1))
                {
                dblSumL *= e;
                }
            else if ((ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == 2)
                     || (ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == -2))
                {
                dblSumL *= eSquared;
                }

            SigmaL += dblSumL;

            // Radius Vector R
            dblSumR = TERMS_LR[intTermIndex][INDEX_COEFF_COS]
                      * Math.cos(ARGUMENTS_LR[intTermIndex][INDEX_COEFF_D] * D
                                 + ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] * M
                                 + ARGUMENTS_LR[intTermIndex][INDEX_COEFF_MDASH] * Mdash
                                 + ARGUMENTS_LR[intTermIndex][INDEX_COEFF_F] * F);

            // Eccentricity correction
            if ((ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == 1)
                || (ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == -1))
                {
                dblSumR *= e;
                }
            else if ((ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == 2)
                     || (ARGUMENTS_LR[intTermIndex][INDEX_COEFF_M] == -2))
                {
                dblSumR *= eSquared;
                }

            SigmaR += dblSumR;

            // Ecliptic Latitude B
            dblSumB = TERMS_B[intTermIndex][INDEX_COEFF_SIN]
                      * Math.sin(ARGUMENTS_B[intTermIndex][INDEX_COEFF_D] * D
                                 + ARGUMENTS_B[intTermIndex][INDEX_COEFF_M] * M
                                 + ARGUMENTS_B[intTermIndex][INDEX_COEFF_MDASH] * Mdash
                                 + ARGUMENTS_B[intTermIndex][INDEX_COEFF_F] * F);

            // Eccentricity correction
            if ((ARGUMENTS_B[intTermIndex][INDEX_COEFF_M] == 1)
                || (ARGUMENTS_B[intTermIndex][INDEX_COEFF_M] == -1))
                {
                dblSumB *= e;
                }
            else if ((ARGUMENTS_B[intTermIndex][INDEX_COEFF_M] == 2)
                     || (ARGUMENTS_B[intTermIndex][INDEX_COEFF_M] == -2))
                {
                dblSumB *= eSquared;
                }

            SigmaB += dblSumB;
            }

         //-----------------------------------------------------------------------------------------
         // Page 312

         Ladditive = 3958 * Math.sin(A1)
                         + 1962 * Math.sin(Ldash - F)
                         + 318 * Math.sin(A2);
         SigmaL += Ladditive;

         Badditive = -2235 * Math.sin(Ldash)
                         + 382 * Math.sin(A3)
                         + 175 * Math.sin(A1 - F)
                         + 175 * Math.sin(A1 + F)
                         + 127 * Math.sin(Ldash - Mdash)
                         - 115 * Math.sin(Ldash + Mdash);
         SigmaB += Badditive;

         // Find the Spherical coordinates {l, b, r} in DEGREES and km
         dblLambdaDegrees = CoordinateConversions.convertRadiansToDegrees(Ldash) + (SigmaL * 0.000001);

         // Add a correction for the nutation in longitude (Chapter 21)
         // Nutation in seconds of arc
         dblLambdaApparentDegrees = dblLambdaDegrees + (Nutation.nutationInLongitude(jde) / 3600);
         dblLambdaApparentDegrees = CoordinateConversions.normaliseLongitudeDegrees(dblLambdaApparentDegrees);

         dblBeta = SigmaB * 0.000001;
         dblDelta = 385000.56 + (0.001 * SigmaR);

//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Add SigmaL=" + SigmaL);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Add SigmaB=" + SigmaB);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() SigmaR=" + SigmaR);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees dblLambdaDegrees=" + dblLambdaDegrees);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees Nutation seconds=" + Nutation.nutationInLongitude(jde));
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees dblLambdaApparentDegrees=" + dblLambdaApparentDegrees);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees Beta=" + dblBeta);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() km Delta=" + dblDelta);

         // Spherical coordinates {l, b, r} in DEGREES
         coords = new SphericalCoordinates(dblLambdaApparentDegrees, dblBeta, dblDelta);

         return (coords);
         }


    /***********************************************************************************************
    * Evaluate a polynomial.
    * result = c[0] + c[1]x + c[2]x^2 + c[3]x^3 + ....
    *
    * @param coeffs    the coefficients of the polynomial : coeffs[0] is constant term, etc.
    * @param degree    the degree of the polynomial
    * @param x         the point at which to evaluate the polynomial
    *
    * @return double
    */

   private static double evaluatePolynomial(final double[] coeffs,
                                            final int degree,
                                            final double x)
       {
       double dblTmp;

       // Evaluate result = c[0] + c[1]x + c[2]x^2 + c[3]x^3 + ....
       // c[3]
       // c[3]x    +  c[2]
       // (c[3]x   +  c[2])x  +  c[1]
       // ((c[3]x  +  c[2])x  +  c[1])x  +  c[0]

       dblTmp = coeffs[degree];

       for (int index = degree-1;
            index >= 0;
            index--)
           {
           dblTmp = dblTmp * x + coeffs[index];
           }

       return (dblTmp);
       }


    /***********************************************************************************************
     * LunarEphemerisDAO.
     *
     * @param ephemeris
     */

    public LunarEphemerisDAO(final Ephemeris ephemeris)
        {
        super(ephemeris);

        //------------------------------------------------------------------------------------------
        // Meeus' test point

//        final double JDE_TEST = 2448724.5;
//        final SphericalCoordinates coords;
//        final Point2D.Double pointRaDec;
//        final HourMinSecDataType hmsRA;

        // Test position 1992 April 12, 0h TD
//        System.out.println("\nLunarEphemerisDAO Test Position 1992 April 12, 0h TD jde=" + JDE_TEST);
//        coords = calculateSphericalCoordinatesDegrees(JDE_TEST);

        // Convert SphericalCoordinates (lambda, beta, delta) to (Ra, Dec)
//        pointRaDec = CoordinateConversions.convertSphericalToRaDec(coords, JDE_TEST);

        // Right Ascension in HOURS
//        hmsRA = new HourMinSecDataType(pointRaDec.getX());
//        hmsRA.enableFormatSign(false);

//        System.out.println("LunarEphemerisDAO Test Position Degrees RA=" + (pointRaDec.getX() * 360 / 24));
//        System.out.println("LunarEphemerisDAO Test Position Hours RA=" + pointRaDec.getX());
//        System.out.println("LunarEphemerisDAO Test Position RA=" + hmsRA.toString());
//        System.out.println("LunarEphemerisDAO Test Position Dec=" + pointRaDec.getY());
//        System.out.println("LunarEphemerisDAO Test Position Dec=" + formatDec.format(pointRaDec.getY()));
        }


    /***********************************************************************************************
     * Recalculate the coordinates for the specified JD, as (Ra, Dec).
     *
     * @param juliandate
     * @param last
     * @param latitude
     */

    public CoordinateType recalculateForJulianDate(final double juliandate,
                                                   final double last,
                                                   final double latitude)
        {
        CoordinateType coordinateType;

        coordinateType = CoordinateType.UNASSIGNED;

        if (getEphemeris() != null)
            {
            final Point2D.Double pointRaDec;

            // Ignore any coordinates which were supplied
            pointRaDec = calculateMoonPositionRaDec(juliandate);

            coordinateType = CoordinateType.RADEC;
            setCoordinates(pointRaDec);
            }

        return (coordinateType);
        }
    }


//--------------------------------------------------------------------------------------------------
// THINGS WHICH MAY STILL BE USEFUL??

//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() T=" + T);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians Ldash=" + Ldash);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians D=" + D);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians M=" + M);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians Mdash=" + Mdash);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians F=" + F);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() e=" + e);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians A1=" + A1);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians A2=" + A2);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Radians A3=" + A3);

//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() T=" + T);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees Ldash=" + Ldash);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees D=" + D);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees M=" + M);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees Mdash=" + Mdash);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees F=" + F);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() e=" + e);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees A1=" + A1);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees A2=" + A2);
//         System.out.println("LunarEphemerisDAO.calculateSphericalCoordinatesDegrees() Degrees A3=" + A3);

