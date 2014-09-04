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

/***************************************************************************************************
 * Nutation.
 */

public final class Nutation
    {
    private static final int TERM_COUNT = 63;
    private static final int INDEX_COEFF_D = 0;
    private static final int INDEX_COEFF_M = 1;
    private static final int INDEX_COEFF_MDASH = 2;
    private static final int INDEX_COEFF_F = 3;
    private static final int INDEX_COEFF_OMEGA = 4;

    // Table 21.A Page 133
    private static final int[][] NUTATION_ARGUMENTS =
        {
          { 0,  0,  0,  0,  1},
          {-2,  0,  0,  2,  2},
          { 0,  0,  0,  2,  2},
          { 0,  0,  0,  0,  2},
          { 0,  1,  0,  0,  0},
          { 0,  0,  1,  0,  0},
          {-2,  1,  0,  2,  2},
          { 0,  0,  0,  2,  1},
          { 0,  0,  1,  2,  2},
          {-2, -1,  0,  2,  2},

          {-2,  0,  1,  0,  0},
          {-2,  0,  0,  2,  1},
          { 0,  0, -1,  2,  2},
          { 2,  0,  0,  0,  0},
          { 0,  0,  1,  0,  1},
          { 2,  0, -1,  2,  2},
          { 0,  0, -1,  0,  1},
          { 0,  0,  1,  2,  1},
          {-2,  0,  2,  0,  0},
          { 0,  0, -2,  2,  1},

          { 2,  0,  0,  2,  2},
          { 0,  0,  2,  2,  2},
          { 0,  0,  2,  0,  0},
          {-2,  0,  1,  2,  2},
          { 0,  0,  0,  2,  0},
          {-2,  0,  0,  2,  0},
          { 0,  0, -1,  2,  1},
          { 0,  2,  0,  0,  0},
          { 2,  0, -1,  0,  1},
          {-2,  2,  0,  2,  2},

          { 0,  1,  0,  0,  1},
          {-2,  0,  1,  0,  1},
          { 0, -1,  0,  0,  1},
          { 0,  0,  2, -2,  0},
          { 2,  0, -1,  2,  1},
          { 2,  0,  1,  2,  2},
          { 0,  1,  0,  2,  2},
          {-2,  1,  1,  0,  0},
          { 0, -1,  0,  2,  2},
          { 2,  0,  0,  2,  1},

          { 2,  0,  1,  0,  0},
          {-2,  0,  2,  2,  2},
          {-2,  0,  1,  2,  1},
          { 2,  0, -2,  0,  1},
          { 2,  0,  0,  0,  1},
          { 0, -1,  1,  0,  0},
          {-2, -1,  0,  2,  1},
          {-2,  0,  0,  0,  1},
          { 0,  0,  2,  2,  1},
          {-2,  0,  2,  0,  1},

          {-2,  1,  0,  2,  1},
          { 0,  0,  1, -2,  0},
          {-1,  0,  1,  0,  0},
          {-2,  1,  0,  0,  0},
          { 1,  0,  0,  0,  0},
          { 0,  0,  1,  2,  0},
          { 0,  0, -2,  2,  2},
          {-1, -1,  1,  0,  0},
          { 0,  1,  1,  0,  0},
          { 0, -1,  1,  2,  2},

          { 2, -1, -1,  2,  2},
          { 0,  0,  3,  2,  2},
          { 2, -1,  0,  2,  2},
        };

    private static final int INDEX_COEFF_SIN_1 = 0;
    private static final int INDEX_COEFF_SIN_2 = 1;
    private static final int INDEX_COEFF_COS_1 = 2;
    private static final int INDEX_COEFF_COS_2 = 3;

    // Coefficients in units of 0.0001 seconds of arc
    private static final double[][] NUTATION_TERMS =
        {
          {-171996,  -174.2,  92025,     8.9},
          { -13187,    -1.6,   5736,    -3.1},
          {  -2274,    -0.2,    977,    -0.5},
          {   2062,     0.2,   -895,     0.5},
          {   1426,    -3.4,     54,    -0.1},
          {    712,     0.1,     -7,       0},
          {   -517,     1.2,    224,    -0.6},
          {   -386,    -0.4,    200,       0},
          {   -301,       0,    129,    -0.1},
          {    217,    -0.5,    -95,     0.3},

          {   -158,       0,      0,       0},
          {    129,     0.1,    -70,       0},
          {    123,       0,    -53,       0},
          {     63,       0,      0,       0},
          {     63,     0.1,    -33,       0},
          {    -59,       0,     26,       0},
          {    -58,    -0.1,     32,       0},
          {    -51,       0,     27,       0},
          {     48,       0,      0,       0},
          {     46,       0,    -24,       0},

          {    -38,       0,     16,       0},
          {    -31,       0,     13,       0},
          {     29,       0,      0,       0},
          {     29,       0,    -12,       0},
          {     26,       0,      0,       0},
          {    -22,       0,      0,       0},
          {     21,       0,    -10,       0},
          {     17,    -0.1,      0,       0},
          {     16,       0,     -8,       0},
          {    -16,     0.1,      7,       0},

          {    -15,       0,      9,       0},
          {    -13,       0,      7,       0},
          {    -12,       0,      6,       0},
          {     11,       0,      0,       0},
          {    -10,       0,      5,       0},
          {    -8,        0,      3,       0},
          {     7,        0,     -3,       0},
          {    -7,        0,      0,       0},
          {    -7,        0,      3,       0},
          {    -7,        0,      3,       0},

          {     6,        0,      0,       0},
          {     6,        0,     -3,       0},
          {     6,        0,     -3,       0},
          {    -6,        0,      3,       0},
          {    -6,        0,      3,       0},
          {     5,        0,      0,       0},
          {    -5,        0,      3,       0},
          {    -5,        0,      3,       0},
          {    -5,        0,      3,       0},
          {     4,        0,      0,       0},

          {     4,        0,      0,       0},
          {     4,        0,      0,       0},
          {    -4,        0,      0,       0},
          {    -4,        0,      0,       0},
          {    -4,        0,      0,       0},
          {     3,        0,      0,       0},
          {    -3,        0,      0,       0},
          {    -3,        0,      0,       0},
          {    -3,        0,      0,       0},
          {    -3,        0,      0,       0},

          {    -3,        0,      0,       0},
          {    -3,        0,      0,       0},
          {    -3,        0,      0,       0},
        };


    /***********************************************************************************************
     * Calculate the Nutation in Longitude for the specified Julian Date.
     * Nutation in seconds of arc.
     *
     * @param jd
     *
     * @return double
     */

    public static double nutationInLongitude(final double jd)
        {
        double dblSigmaValue;
        final double T;
        final double Tsquared;
        final double Tcubed;
        double D;
        double M;
        double Mdash;
        double F;
        double omega;

        T = (jd - 2451545) / 36525;
        Tsquared = T*T;
        Tcubed = Tsquared*T;

        D = 297.85036 + (445267.111480*T) - (0.0019142*Tsquared) + Tcubed / 189474;
        D = CoordinateConversions.normalise360Degrees(D);

        M = 357.52772 + (35999.050340*T) - (0.0001603*Tsquared) - Tcubed / 300000;
        M = CoordinateConversions.normalise360Degrees(M);

        Mdash = 134.96298 + (477198.867398*T) + (0.0086972*Tsquared) + Tcubed / 56250;
        Mdash = CoordinateConversions.normalise360Degrees(Mdash);

        F = 93.27191 + (483202.017538*T) - (0.0036825*Tsquared) + Tcubed / 327270;
        F = CoordinateConversions.normalise360Degrees(F);

        omega = 125.04452 - (1934.136261*T) + (0.0020708*Tsquared) + Tcubed / 450000;
        omega = CoordinateConversions.normalise360Degrees(omega);

        dblSigmaValue = 0;

        for (int intTermIndex = 0;
             intTermIndex < TERM_COUNT;
             intTermIndex++)
            {
            final double dblArgumentDegrees;
            final double dblArgumentRadians;

            dblArgumentDegrees = NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_D] * D
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_M] * M
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_MDASH] * Mdash
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_F] * F
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_OMEGA] * omega;

            dblArgumentRadians = CoordinateConversions.convertDegreesToRadians(dblArgumentDegrees);
            // Coefficients are in units of 0.0001 seconds of arc, so convert to seconds
            dblSigmaValue += (NUTATION_TERMS[intTermIndex][INDEX_COEFF_SIN_1] + NUTATION_TERMS[intTermIndex][INDEX_COEFF_SIN_2] * T)
                                * Math.sin(dblArgumentRadians)
                                *  0.0001;
            }

        return (dblSigmaValue);
        }


    /***********************************************************************************************
     * Calculate the Nutation in Obliquity for the specified Julian Date.
     * Nutation in seconds of arc.
     *
     * @param jd
     *
     * @return double
     */

    public static double nutationInObliquity(final double jd)
        {
        final double T;
        final double Tsquared;
        final double Tcubed;
        double D;
        double M;
        double Mdash;
        double F;
        double omega;
        double dblSigmaValue;

        T = (jd - 2451545) / 36525;
        Tsquared = T*T;
        Tcubed = Tsquared*T;

        // Mean Elongation
        D = 297.85036 + (445267.111480*T) - (0.0019142*Tsquared) + Tcubed / 189474;
        D = CoordinateConversions.normalise360Degrees(D);

        M = 357.52772 + (35999.050340*T) - (0.0001603*Tsquared) - Tcubed / 300000;
        M = CoordinateConversions.normalise360Degrees(M);

        Mdash = 134.96298 + (477198.867398*T) + (0.0086972*Tsquared) + Tcubed / 56250;
        Mdash = CoordinateConversions.normalise360Degrees(Mdash);

        F = 93.27191 + (483202.017538*T) - (0.0036825*Tsquared) + Tcubed / 327270;
        F = CoordinateConversions.normalise360Degrees(F);

        omega = 125.04452 - (1934.136261*T) + (0.0020708*Tsquared) + Tcubed / 450000;
        omega = CoordinateConversions.normalise360Degrees(omega);

        dblSigmaValue = 0;

        for (int intTermIndex = 0;
             intTermIndex < TERM_COUNT;
             intTermIndex++)
            {
            final double dblArgumentDegrees;
            final double dblArgumentRadians;

            dblArgumentDegrees = NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_D] * D
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_M] * M
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_MDASH] * Mdash
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_F] * F
                                   + NUTATION_ARGUMENTS[intTermIndex][INDEX_COEFF_OMEGA] * omega;

            dblArgumentRadians = CoordinateConversions.convertDegreesToRadians(dblArgumentDegrees);
            // Coefficients are in units of 0.0001 seconds of arc, so convert to seconds
            dblSigmaValue += (NUTATION_TERMS[intTermIndex][INDEX_COEFF_COS_1] + NUTATION_TERMS[intTermIndex][INDEX_COEFF_COS_2] * T)
                                * Math.cos(dblArgumentRadians)
                                *  0.0001;
            }

        return (dblSigmaValue);
        }
    }

//double CAANutation::TrueObliquityOfEcliptic(double JD)
//{
//  return MeanObliquityOfEcliptic(JD) + CAACoordinateTransformation::DMSToDegrees(0, 0, NutationInObliquity(JD));
//}
//
//double CAANutation::NutationInRightAscension(double Alpha, double Delta, double Obliquity, double NutationInLongitude, double NutationInObliquity)
//{
//  //Convert to radians
//  Alpha = CAACoordinateTransformation::HoursToRadians(Alpha);
//  Delta = CAACoordinateTransformation::DegreesToRadians(Delta);
//  Obliquity = CAACoordinateTransformation::DegreesToRadians(Obliquity);
//
//  return (cos(Obliquity) + sin(Obliquity) * sin(Alpha) * tan(Delta)) * NutationInLongitude - cos(Alpha)*tan(Delta)*NutationInObliquity;
//}
//
//double CAANutation::NutationInDeclination(double Alpha, double Obliquity, double NutationInLongitude, double NutationInObliquity)
//{
//  //Convert to radians
//  Alpha = CAACoordinateTransformation::HoursToRadians(Alpha);
//  Obliquity = CAACoordinateTransformation::DegreesToRadians(Obliquity);
//
//  return sin(Obliquity) * cos(Alpha) * NutationInLongitude + sin(Alpha)*NutationInObliquity;
//}

//
//Copyright (c) 2003 - 2010 by PJ Naughter (Web: www.naughter.com, Email: pjna@naughter.com)
//
//All rights reserved.
//
//Copyright / Usage Details:
//
//You are allowed to include the source code in any product (commercial, shareware, freeware or otherwise)
//when your product is released in binary form. You are allowed to modify the source code in any way you want
//except you cannot modify the copyright details at the top of each module. If you want to distribute source
//code with your application, then you are only allowed to distribute versions released by the author. This is
//to maintain a single distribution point for the source code.
//
//*/
