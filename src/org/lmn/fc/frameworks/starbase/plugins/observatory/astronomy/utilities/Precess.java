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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities;

import org.lmn.fc.common.constants.AstronomyConstants;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;

import java.awt.geom.Point2D;


/***************************************************************************************************
 * Precess.
 */

public final class Precess
    {
    private static final double COS_OBLIQUITY = 0.91745051;


    /**********************************************************************************************
     * Calculate equatorial coordinates precessed between the specified Epochs.
     * RA in HOURS, Dec in DEGREES.
     * See: Jean Meeus Astronomical Algorithms First Edition pg. 126 eq. 20.2, 20.4
     *
     * @param pointradec
     * @param epoch0
     * @param epoch1
     *
     * @return Point2D.Double
     */

    public static Point2D.Double precessEquatorial(final Point2D.Double pointradec,
                                                   final Epoch epoch0,
                                                   final Epoch epoch1)
        {
        final double T;
        final double Tsquared;
        final double t;
        final double tsquared;
        final double tcubed;
        final double dblRA0;
        final double dblDec0;
        final double sigmaSeconds;
        final double sigmaRadians;
        final double zetaSeconds;
        final double zetaRadians;
        final double thetaSeconds;
        final double thetaRadians;
        final double A;
        final double B;
        final double C;
        double dblRAPrecessed;
        final double dblDecPrecessed;
        final Point2D.Double pointPrecessed;

        T = (epoch0.getJD() - Epoch.J2000.getJD()) / 36525;
        Tsquared = T * T;

        t = (epoch1.getJD() - epoch0.getJD()) / 36525;
        tsquared = t * t;
        tcubed = tsquared * t;

        // Use radians from here on
        dblRA0 = pointradec.getX() * AstronomyConstants.HOURS_TO_RADIANS;
        dblDec0 = pointradec.getY() * AstronomyConstants.DEGREES_TO_RADIANS;

        sigmaSeconds = (2306.2181 + 1.39656 * T - 0.000139 * Tsquared) * t
                           + (0.30188 - 0.0000344 * T) * tsquared
                           + 0.017998 * tcubed;
        sigmaRadians = sigmaSeconds * AstronomyConstants.ARCSEC_TO_RADIANS;

        zetaSeconds = (2306.2181 + 1.39656 * T - 0.000139 * Tsquared) * t
                          + (1.09468 + 0.000066 * T) * tsquared
                          + 0.018203 * tcubed;
        zetaRadians = zetaSeconds * AstronomyConstants.ARCSEC_TO_RADIANS;

        thetaSeconds = (2004.3109 - 0.8533 * T - 0.000217 * Tsquared) * t
                         - (0.42665 + 0.000217 * T) * tsquared
                         - 0.041833 * tcubed;
        thetaRadians = thetaSeconds * AstronomyConstants.ARCSEC_TO_RADIANS;

        A = Math.cos(dblDec0) * Math.sin(dblRA0 + sigmaRadians);

        B = Math.cos(thetaRadians) * Math.cos(dblDec0) * Math.cos(dblRA0 + sigmaRadians) - Math.sin(thetaRadians) * Math.sin(dblDec0);

        C = Math.sin(thetaRadians) * Math.cos(dblDec0) * Math.cos(dblRA0 + sigmaRadians) + Math.cos(thetaRadians) * Math.sin(dblDec0);

        dblRAPrecessed = (Math.atan2(A, B) + zetaRadians) * AstronomyConstants.RADIANS_TO_HOURS;

        if (dblRAPrecessed < 0.0)
            {
            dblRAPrecessed += AstronomyConstants.HOURS_PER_DAY;
            }

        // Are we near the Pole? - say within about one degree
        if ((dblDec0 > (0.4 * Math.PI))
            || (dblDec0 < (-0.4 * Math.PI)))
            {
            dblDecPrecessed = Math.acos(Math.sqrt(A * A + B * B)) * AstronomyConstants.RADIANS_TO_DEGREES;
            }
        else
            {
            dblDecPrecessed = Math.asin(C) * AstronomyConstants.RADIANS_TO_DEGREES;
            }

        pointPrecessed = new Point2D.Double(dblRAPrecessed, dblDecPrecessed);

        // RA in HOURS, Dec in DEGREES
        return (pointPrecessed);
        }


//----------------------------------------------------------------------------------------

    //    public RaDecCoord precessEquatorial(final double ra, final double dec,
//                                        final double jd0, final double jd) {
//    final double T = (jd0 - 2451545.0) / 36525;
//    final double Tsquared = T * T;
//    final double t = (jd - jd0) / 36525;
//    final double tsquared = t * t;
//    final double tcubed = tsquared * t;
//
//    // Now convert to radians
//    final double alpha = ra * Angle.D2R;
//    final double delta = dec * Angle.D2R;
//
//    double sigma = (2306.2181 + 1.39656 * T - 0.000139 * Tsquared) * t
//                   + (0.30188 - 0.0000344 * T) * tsquared + 0.017988 * tcubed;
//    sigma = new DMS(0, 0, sigma).getRadians();
//
//    double zeta = (2306.2181 + 1.39656 * T - 0.000138 * Tsquared) * t
//                  + (1.09468 + 0.000066 * T) * tsquared + 0.018203 * tcubed;
//    zeta = new DMS(0, 0, zeta).getRadians();
//
//    double phi = (2004.3109 - 0.8533 * T - 0.000217 * Tsquared) * t
//                 - (0.42665 + 0.000217 * T) * tsquared - 0.041833 * tcubed;
//    phi = new DMS(0, 0, phi).getRadians();
//
//    final double A = Math.cos(delta) * Math.sin(alpha + sigma);
//    final double B = Math.cos(phi) * Math.cos(delta)
//                     * Math.cos(alpha + sigma) - Math.sin(phi) * Math.sin(delta);
//    final double C = Math.sin(phi) * Math.cos(delta)
//                     * Math.cos(alpha + sigma) + Math.cos(phi) * Math.sin(delta);
//
//    double vX = (Math.atan2(A, B) + zeta) * Angle.R2H;
//    if (vX < 0) {
//    vX += 24;
//    }
//    double vY;
//    /* check for object near celestial pole */
//    if ((delta > (0.4 * MathConstantes.PI))
//        || (delta < (-0.4 * MathConstantes.PI))) {
//    /* close to pole */
//    vY = Math.acos(Math.sqrt(A * A + B * B)) * Angle.R2D;
//    } else {
//    /* not close to pole */
//    vY = (Math.asin(C)) * Angle.R2D;
//    }
//
//    final RaDecCoord value = new RaDecCoord(new HMS(vX), new DMS(vY));
//    return value;
//    }

//----------------------------------------------------------------------------------------

    // See: http://www.naughter.com/aa.html
//    CAA2DCoordinate CAAPrecession::PrecessEquatorial(double Alpha, double Delta, double JD0, double JD)
//        {
//        double T = (JD0 - 2451545.0) / 36525;
//        double Tsquared = T*T;
//        double t = (JD - JD0) / 36525;
//        double tsquared = t*t;
//        double tcubed  = tsquared * t;
//
//        //Now convert to radians
//        Alpha = CAACoordinateTransformation::HoursToRadians(Alpha);
//        Delta = CAACoordinateTransformation::DegreesToRadians(Delta);
//
//        double sigma = (2306.2181 + 1.39656*T - 0.000139*Tsquared)*t + (0.30188 - 0.0000344*T)*tsquared + 0.017988*tcubed;
//        sigma = CAACoordinateTransformation::DegreesToRadians(CAACoordinateTransformation::DMSToDegrees(0, 0, sigma));
//
//        double zeta = (2306.2181 + 1.39656*T - 0.000138*Tsquared)*t + (1.09468 + 0.000066*T)*tsquared + 0.018203*tcubed;
//        zeta = CAACoordinateTransformation::DegreesToRadians(CAACoordinateTransformation::DMSToDegrees(0, 0, zeta));
//
//        double phi = (2004.3109 - 0.8533*T - 0.000217*Tsquared)*t -  (0.42665 + 0.000217*T)*tsquared - 0.041833*tcubed;
//        phi = CAACoordinateTransformation::DegreesToRadians(CAACoordinateTransformation::DMSToDegrees(0, 0, phi));
//
//        double A = cos(Delta) * sin(Alpha + sigma);
//        double B = cos(phi)*cos(Delta)*cos(Alpha + sigma) - sin(phi)*sin(Delta);
//        double C = sin(phi)*cos(Delta)*cos(Alpha + sigma) + cos(phi)*sin(Delta);
//
//        CAA2DCoordinate value;
//        value.X = CAACoordinateTransformation::RadiansToHours(atan2(A, B) + zeta);
//        if (value.X < 0)
//            value.X += 24;
//        value.Y = CAACoordinateTransformation::RadiansToDegrees(asin(C));
//
//        return value;
//        }

    //    /*****************************************************************************/
//    /* Name:    PrecessFK5                                                       */
//    /* Type:    Procedure                                                        */
//    /* Purpose: precess equatorial coordinates from one FK5 epoch to another.    */
//    /* Arguments:                                                                */
//    /*   T0, T1 : initial and final epochs in centuries since J2000              */
//    /*   RA, Decl : coordinates to be converted                                  */
//    /*****************************************************************************/
//
//    public static void precessFK5(double T0, double T1, double RA, double Decl)
//        {
//        double t, zeta, z, theta;
//        double A, B, C;
//
//        t = T1 - T0;
//        z = 2306.2181 + T0 * (1.39656 - T0 * 0.000139);
//        zeta = t * (z + t * ((0.30188 - T0 * 0.000344) + t * 0.017998)) * SToR;
//        z = t * (z + t * ((1.09468 + T0 * 0.000066) + t * 0.018203)) * SToR;
//        theta = (2004.3109 - T0 * (0.85330 + T0 * 0.000217));
//        theta = t * (theta - t * ((0.42665 + T0 * 0.000217) + t * 0.041833)) * SToR;
//        A = cos (*Decl) * sin (*RA + zeta);
//        B = cos (theta) * cos (*Decl) * cos (*RA + zeta) - sin (theta) * sin (*Decl);
//        C = sin (theta) * cos (*Decl) * cos (*RA + zeta) + cos (theta) * sin (*Decl);
//        *RA = atan2 (A, B) + z;
//        if (*RA < 0)
//        *RA += pi2;
//        *Decl = asin (C);
//        }
//
//
//    /*****************************************************************************/
//    /* Name:    PrecessFK4                                                       */
//    /* Type:    Procedure                                                        */
//    /* Purpose: precess equatorial coordinates from one FK4 epoch to another.    */
//    /* Arguments:                                                                */
//    /*   T0, T1 : initial and final epochs in centuries since J2000              */
//    /*   RA, Decl : coordinates to be converted                                  */
//    /*****************************************************************************/
//
//    public static void precessFK4(double T0, double T1, double RA, double Decl)
//        {
//        double t, zeta, z, theta;
//        double A, B, C;
//
//        #define TB1900 (2415020.3135 - 2451545.0) / 36525
//        #define JulianToBessel (36525.0 / 36524.2199)
//
//        /* Convert T values to units of tropical centuries since B1900.0 */
//        t = (T1 - T0) * JulianToBessel;
//        T0 = (t - TB1900) * JulianToBessel;
//        zeta = t * (2304.250 + T0 * 1.396 + t * (0.302 + t * 0.018)) * SToR;
//        z = zeta + t * t * (0.791 + t * 0.001) * SToR;
//        theta = t * (2004.682 - T0 * 0.853 - t * (0.426 + t * 0.042)) * SToR;
//        A = cos (*Decl) * sin (*RA + zeta);
//        B = cos (theta) * cos (*Decl) * cos (*RA + zeta) - sin (theta) * sin (*Decl);
//        C = sin (theta) * cos (*Decl) * cos (*RA + zeta) + cos (theta) * sin (*Decl);
//        *RA = atan2 (A, B) + z;
//        if (*RA < 0)
//        *RA += pi2;
//        *Decl = asin (C);
//        }
//
//
//    /*****************************************************************************/
//    /* Name:    EquinoxCorrection                                                */
//    /* Type:    Function                                                         */
//    /* Purpose: calculate the equinox correction from FK4 to FK5 system.         */
//    /* Arguments:                                                                */
//    /*   T : number of Julian centuries since J2000.0                            */
//    /* Return value:                                                             */
//    /*   the equinox correction in radians                                       */
//    /*****************************************************************************/
//
//    public static double EquinoxCorrection(double T)
//        {
//        return (0.0775 + 0.0850 * T) * 15 * SToR;
//        }
//
//
    /***********************************************************************************************
     * Calculates the correction in RA and Dec to be added when
     * precessing coordinates.  All angles given in radians.
     *
     * precess() calculates the corrections to be added to the
     * mean coordinates for epoch_start to give the apparent
     * coordinates for epoch_end.
     *
     * precess() also calculates the
     * equation of the equinoxes (DC in minutes of time)
     * which may be added to the mean sideraeal time to
     * give the apparent sidereal time.
     *
     * deltaRA and deltaDec (the corrections) contain corrections for precession,
     * annual abberation, and some terms of nutation.
     *
     * If RA and Dec are for the mean epoch (i.e. halfway between
     * epoch_start and epoch_end the precision of deltaRA and deltaDec is
     * about 2 arcseconds.  If RA and Dec are either of the
     * endpoints, the precision is somewhat worse.
     *
     * day_number is the day of the year, epoch_start is the epoch of the
     * given ra and dec.  epoch_end is the year that the coordinates
     * should be precessed to.
     *
     * Taken from:
     * Methods of Experimental Physics
     * Volume 12, Part C: Radio Observations
     * M.L. Meeks, Editor
     *
     * @param epoch_start
     * @param epoch_end
     * @param day_number
     * @param ra_hms
     * @param declination_dms
     *
     * @return Point2D.Double  (RA, Dec)
     */

    public static Point2D.Double precessRaDec(final Epoch epoch_start,
                                              final Epoch epoch_end,
                                              final double day_number,
                                              final HourMinSecInterface ra_hms,
                                              final DegMinSecInterface declination_dms)
        {
        final Point2D.Double pointRaDec;
        final double dblRA;
        final double dblDeclination;
        final double dblCosRA;
        final double dblSinRA;
        final double dblSinDeclination;
        final double dblCosDeclination;
        final double dblTanDeclination;
        final double t0;
        final double t;
        final double zeta0;
        final double z;
        final double theta;
        final double am;
        final double an;
        final double dblMeanLongitude;
        final double dblSinMeanLongitude;
        final double dblCosMeanLongitude;
        final double dblOmega;
        final double dblOmegaRadians;
        final double dblNutationLongitude;
        final double dblNutationObliquity;
        double deltaDec;
        double deltaRA;

        dblRA = ra_hms.toDouble();
        dblDeclination = declination_dms.toDouble();

        dblCosRA = Math.cos(dblRA);
        dblSinRA = Math.sin(dblRA);

        dblSinDeclination = Math.sin(dblDeclination);
        dblCosDeclination = Math.cos(dblDeclination);
        dblTanDeclination = dblSinDeclination / dblCosDeclination;

        // day_number is an approximate day number to precess to
        // i.e. the number of days since January 0 of the year epoch_end

        // t0 is the time from 1900 to epoch_start (centuries)
        t0 = (epoch_start.getValue() - Epoch.B1900.getValue()) / 100.0;

        // t is the time from epoch_start to epoch_end (centuries)
        t = ((epoch_end.getValue() - epoch_start.getValue()) / 100.0) + (day_number / (365.2421988 * 100.0));

        // zeta0, z, and theta are precessional angles
        zeta0 = ((2304.250 + (1.396 * t0)) * t) + (0.302 * t * t) + (0.018 * t * t * t);
        z = zeta0 + (0.791 * t * t);
        theta = ((2004.682 - (0.853 * t0)) * t) - (0.426 * t * t) - (0.042 * t * t * t);

        // am and an are the precessional numbers
        am = (zeta0 + z) * 4.848136811e-6;
        an = theta * 4.848136811E-6;

        // Approximate mean longitude for the Sun
        dblMeanLongitude = ((0.985647 * day_number) + 278.5) * 0.0174532925;
        dblSinMeanLongitude = Math.sin(dblMeanLongitude);
        dblCosMeanLongitude = Math.cos(dblMeanLongitude);

        //  deltaRA and deltaDec are the annual aberration terms in radians
        //  0.91745051 is the cos(obliquity of ecliptic)

        deltaRA = ((-9.92413605e-5 * ((dblSinMeanLongitude * dblSinRA) + (COS_OBLIQUITY * dblCosMeanLongitude * dblCosRA))) / dblCosDeclination)
                    + am + (an * dblSinRA * dblTanDeclination);

        deltaDec = (-9.92413605e-5 * (((dblSinMeanLongitude * dblCosRA * dblSinDeclination) - (COS_OBLIQUITY * dblCosMeanLongitude * dblSinRA * dblSinDeclination))
                    + (0.39784993 * dblCosMeanLongitude * dblCosDeclination))) + (an * dblCosRA);

        //  omega is the angle of the first term of nutation in degrees
        dblOmega = 259.183275 - (1934.142 * (t0 + t));
        dblOmegaRadians = dblOmega * FrameworkConstants.RADIAN;

        //  Nutation in longitude in radians
        dblNutationLongitude = -8.3597e-5 * Math.sin(dblOmegaRadians);

        //  Nutation in obliquity
        dblNutationObliquity = 4.4678e-5 * Math.cos(dblOmegaRadians);

        //  Add in nutation terms
        deltaRA += (dblNutationLongitude * (COS_OBLIQUITY + (0.39784993 * dblSinRA * dblTanDeclination))) - (dblCosRA * dblTanDeclination * dblNutationObliquity);
        deltaDec += (0.39784993 * dblCosRA * dblNutationLongitude) + (dblSinRA * dblNutationObliquity);

        //  Precessed ra and dec
        pointRaDec = new Point2D.Double(dblRA + deltaRA,
                                        dblDeclination + deltaDec);
        return (pointRaDec);
        }
    }
