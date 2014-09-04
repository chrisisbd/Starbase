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
// Astronomy Utilities
//------------------------------------------------------------------------------
// Revision History
//
//  27-03-02    LMN created file from Java Applications book
//  02-11-02    LMN added calculateParallacticAngle()
//
//------------------------------------------------------------------------------
// Astronomy package

package org.lmn.fc.common.utilities.astronomy;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.AstronomyConstants;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


//------------------------------------------------------------------------------

public final class AstroUtilities
    {
    private static final double DEGREES_PER_HOUR = 15.0;

    // epsilon value in degrees


    /************************************************************************************************
     * Obliquity of the Ecliptic.
     * Meeus Page 135 Equation 21.3
     *
     * @param jd
     *
     * @return double
     */

    public static double obliquityOfEcliptic(final double jd)
        {
        final double t;

        t = J2000Centuries(jd) / 10.0;

        // Meeus Page 135 Equation 21.3
        // 23.439291111111111111111111111111
        // 84381.448

        return ((((((((((( 2.45e-10  // div 10000000000  e10
                    * t + 5.79e-9)   // div 1000000000   e9
                    * t + 2.787e-7)  // div 100000000    e8
                    * t + 7.12e-7)   // div 10000000     e7
                    * t - 3.905e-5)  // div 1000000      e6
                    * t - 2.4967e-3) // div 100000       e5
                    * t - 5.138e-3)  // div 10000        e4
                    * t + 1.999925)  // div 1000         e3
                    * t - 0.0155)    // div 100          e2
                    * t - 468.093)   // div 10           e1
                    * t + 84381.448)
                 / 3600.0);

        // Not sure where this one came from!
//        return ((((((((((( 2.45e-10
//                    * t + 5.79e-9)
//                    * t + 2.787e-7)
//                    * t + 7.12e-7)
//                    * t - 3.905e-5)
//                    * t - 2.4967e-3)
//                    * t - 5.138e-3)
//                    * t + 1.9989)
//                    * t - 0.0152)
//                    * t - 468.0927)
//                    * t + 84381.412)
//                 / 3600.0);
        }

//    double CAANutation::MeanObliquityOfEcliptic(double JD)
//    {
//      double U = (JD - 2451545) / 3652500;
//      double Usquared = U*U;
//      double Ucubed = Usquared*U;
//      double U4 = Ucubed*U;
//      double U5 = U4*U;
//      double U6= U5*U;
//      double U7 = U6*U;
//      double U8 = U7*U;
//      double U9 = U8*U;
//      double U10 = U9*U;
//
//
//      return CAACoordinateTransformation::DMSToDegrees(23, 26, 21.448) - CAACoordinateTransformation::DMSToDegrees(0, 0, 4680.93) * U
//                                                                       - CAACoordinateTransformation::DMSToDegrees(0, 0, 1.55) * Usquared
//                                                                       + CAACoordinateTransformation::DMSToDegrees(0, 0, 1999.25) * Ucubed
//                                                                       - CAACoordinateTransformation::DMSToDegrees(0, 0, 51.38) * U4
//                                                                       - CAACoordinateTransformation::DMSToDegrees(0, 0, 249.67) * U5
//                                                                       - CAACoordinateTransformation::DMSToDegrees(0, 0, 39.05) * U6
//                                                                       + CAACoordinateTransformation::DMSToDegrees(0, 0, 7.12) * U7
//                                                                       + CAACoordinateTransformation::DMSToDegrees(0, 0, 27.87) * U8
//                                                                       + CAACoordinateTransformation::DMSToDegrees(0, 0, 5.79) * U9
//                                                                       + CAACoordinateTransformation::DMSToDegrees(0, 0, 2.45) * U10;
//    }

    //--------------------------------------------------------------------------
    // Calculate the Parallactic Angle for the specified (Ra, Dec)
    // at the specified instant, for the given latitude
    // See: Meeus Chapter 11 Page 94
    // The angle is measured positively clockwise from the Zenith

    public static double calculateParallacticAngle(final AstronomicalCalendarInterface calendar,
                                                   final Point2D.Double pointradec,
                                                   final double latitude)
        {
        final double dblHourAngle;            // The calculated Hour Angle
        final double dblDenominator;

        // The ParallacticAngle is undefined at the Zenith
        if (Math.abs(pointradec.y -latitude) < 0.1)
            {
            return(0.0);
            }
        else
            {
            // TODO this seems wrong HA is in hours??!!
            // Calculate the Local Hour Angle of this point
            // LAST is in HOURS, dblHourAngle is in DEGREES
            dblHourAngle = (calendar.getLAST() + DEGREES_PER_HOUR) - pointradec.x;

            dblDenominator = (AstroMath.tand(latitude) * AstroMath.cosd(pointradec.y))
                           - (AstroMath.sind(pointradec.y) * AstroMath.cosd(dblHourAngle));

            // ATan2() works out the correct quadrant...
            return(AstroMath.atan2d(AstroMath.sind(dblHourAngle), dblDenominator));
            }
        }

    //--------------------------------------------------------------------------
    // Convert Julian Ephemeris Day to Julian centuries since 2000

    public static double J2000Centuries(final double jd)
        {
        return((jd - Epoch.J2000.getJD()) / AstronomyConstants.JULIAN_CENTURY);
        }


    //--------------------------------------------------------------------------
    // Calculate Julian Centuries since J1900

    public static double J1900Centuries(final double jd)
        {
        return((jd - Epoch.B1900.getJD()) / AstronomyConstants.JULIAN_CENTURY);
        }


    //--------------------------------------------------------------------------
    // Adjust the range of (e.g.) an angle or time into {0.0___range}

    public static double adjustRange(double value, final double range)
        {
        while (value < 0.0)
            {
            value += range;
            }

        while (value >= range)
            {
            value -= range;
            }

        return(value);
        }


    //--------------------------------------------------------------------------
    // A simple comparison of two AstronomicalCalendars (mainly for testing)

    public static List<String> compareCalendars(final AstronomicalCalendarInterface calendar0,
                                                final AstronomicalCalendarInterface calendar1)
        {
        final List<String> listMismatches;
        boolean boolEqual;

        listMismatches = new ArrayList<String>(50);
        boolEqual = true;

        // These items are in AstronomicalCalendar

        if (!calendar0.toString(TimeSystem.LMT).equals(calendar1.toString(TimeSystem.LMT)))
            {
            listMismatches.add("toStringLMT() {" + calendar0.toString(TimeSystem.LMT)
                               + "} <> {" + calendar1.toString(TimeSystem.LMT) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.UT).equals(calendar1.toString(TimeSystem.UT)))
            {
            listMismatches.add("toStringUT() {" + calendar0.toString(TimeSystem.UT)
                               + "} <> {" + calendar1.toString(TimeSystem.UT) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.JD0).equals(calendar1.toString(TimeSystem.JD0)))
            {
            listMismatches.add("toStringJD0() {" + calendar0.toString(TimeSystem.JD0)
                               + "} <> {" + calendar1.toString(TimeSystem.JD0) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.JD).equals(calendar1.toString(TimeSystem.JD)))
            {
            listMismatches.add("toStringJD() {" + calendar0.toString(TimeSystem.JD)
                               + "} <> {" + calendar1.toString(TimeSystem.JD) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.GMST0).equals(calendar1.toString(TimeSystem.GMST0)))
            {
            listMismatches.add("toStringGMST0() {" + calendar0.toString(TimeSystem.GMST0)
                               + "} <> {" + calendar1.toString(TimeSystem.GMST0) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.GAST0).equals(calendar1.toString(TimeSystem.GAST0)))
            {
            listMismatches.add("toStringGAST0() {" + calendar0.toString(TimeSystem.GAST0)
                               + "} <> {" + calendar1.toString(TimeSystem.GAST0) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.GMST).equals(calendar1.toString(TimeSystem.GMST)))
            {
            listMismatches.add("toStringGMST() {" + calendar0.toString(TimeSystem.GMST)
                               + "} <> {" + calendar1.toString(TimeSystem.GMST) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.GAST).equals(calendar1.toString(TimeSystem.GAST)))
            {
            listMismatches.add("toStringGAST() {" + calendar0.toString(TimeSystem.GAST)
                               + "} <> {" + calendar1.toString(TimeSystem.GAST) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.LMST).equals(calendar1.toString(TimeSystem.LMST)))
            {
            listMismatches.add("toStringLMST() {" + calendar0.toString(TimeSystem.LMST)
                               + "} <> {" + calendar1.toString(TimeSystem.LMST) + "}");
            boolEqual = false;
            }

        if (!calendar0.toString(TimeSystem.LAST).equals(calendar1.toString(TimeSystem.LAST)))
            {
            listMismatches.add("toStringLAST() {" + calendar0.toString(TimeSystem.LAST)
                               + "} <> {" + calendar1.toString(TimeSystem.LAST) + "}");
            boolEqual = false;
            }

        if (calendar0.getNutationLongitude() != calendar1.getNutationLongitude())
            {
            listMismatches.add("getNutationLongitude() {" + calendar0.getNutationLongitude()
                               + "} <> {" + calendar1.getNutationLongitude() + "}");
            boolEqual = false;
            }

        if (calendar0.getNutationObliquity() != calendar1.getNutationObliquity())
            {
            listMismatches.add("getNutationObliquity() {" + calendar0.getNutationObliquity()
                               + "} <> {" + calendar1.getNutationObliquity() + "}");
            boolEqual = false;
            }

        if (calendar0.getNutationRA() != calendar1.getNutationRA())
            {
            listMismatches.add("getNutationRA() {" + calendar0.getNutationRA()
                               + "} <> {" + calendar1.getNutationRA() + "}");
            boolEqual = false;
            }

        if (calendar0.getLongitude() != calendar1.getLongitude())
            {
            listMismatches.add("getLongitude() {" + calendar0.getLongitude()
                               + "} <> {" + calendar1.getLongitude() + "}");
            boolEqual = false;
            }

        // These items are in GregorianCalendar

        if (!calendar0.getTime().equals(calendar1.getTime()))
            {
            listMismatches.add("getTimeFromCalendar() {" + calendar0.getTime()
                               + "} <> {" + calendar1.getTime() + "}");
            boolEqual = false;
            }

        if (!calendar0.getTime().toString().equals(calendar1.getTime().toString()))
            {
            listMismatches.add("getTimeFromCalendar().toString() {" + calendar0.getTime().toString()
                               + "} <> {" + calendar1.getTime().toString() + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.ERA) != calendar1.get(Calendar.ERA))
            {
            listMismatches.add("get(ERA) {" + calendar0.get(Calendar.ERA)
                               + "} <> {" + calendar1.get(Calendar.ERA) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.YEAR) != calendar1.get(Calendar.YEAR))
            {
            listMismatches.add("get(YEAR) {" + calendar0.get(Calendar.YEAR)
                               + "} <> {" + calendar1.get(Calendar.YEAR) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.MONTH) != calendar1.get(Calendar.MONTH))
            {
            listMismatches.add("get(MONTH) {" + calendar0.get(Calendar.MONTH)
                               + "} <> {" + calendar1.get(Calendar.MONTH) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.DATE) != calendar1.get(Calendar.DATE))
            {
            listMismatches.add("get(DATE) {" + calendar0.get(Calendar.DATE)
                               + "} <> {" + calendar1.get(Calendar.DATE) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.DAY_OF_MONTH) != calendar1.get(Calendar.DAY_OF_MONTH))
            {
            listMismatches.add("get(DAY_OF_MONTH) {" + calendar0.get(Calendar.DAY_OF_MONTH)
                               + "} <> {" + calendar1.get(Calendar.DAY_OF_MONTH) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.DAY_OF_YEAR) != calendar1.get(Calendar.DAY_OF_YEAR))
            {
            listMismatches.add("get(DAY_OF_YEAR) {" + calendar0.get(Calendar.DAY_OF_YEAR)
                               + "} <> {" + calendar1.get(Calendar.DAY_OF_YEAR) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.DAY_OF_WEEK) != calendar1.get(Calendar.DAY_OF_WEEK))
            {
            listMismatches.add("get(DAY_OF_WEEK) {" + calendar0.get(Calendar.DAY_OF_WEEK)
                               + "} <> {" + calendar1.get(Calendar.DAY_OF_WEEK) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.DAY_OF_WEEK_IN_MONTH) != calendar1.get(Calendar.DAY_OF_WEEK_IN_MONTH))
            {
            listMismatches.add("get(DAY_OF_WEEK_IN_MONTH) {" + calendar0.get(Calendar.DAY_OF_WEEK_IN_MONTH)
                               + "} <> {" + calendar1.get(Calendar.DAY_OF_WEEK_IN_MONTH) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.WEEK_OF_YEAR) != calendar1.get(Calendar.WEEK_OF_YEAR))
            {
            listMismatches.add("get(WEEK_OF_YEAR) {" + calendar0.get(Calendar.WEEK_OF_YEAR)
                               + "} <> {" + calendar1.get(Calendar.WEEK_OF_YEAR) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.WEEK_OF_MONTH) != calendar1.get(Calendar.WEEK_OF_MONTH))
            {
            listMismatches.add("get(WEEK_OF_MONTH) {" + calendar0.get(Calendar.WEEK_OF_MONTH)
                               + "} <> {" + calendar1.get(Calendar.WEEK_OF_MONTH) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.AM_PM) != calendar1.get(Calendar.AM_PM))
            {
            listMismatches.add("get(AM_PM) {" + calendar0.get(Calendar.AM_PM)
                               + "} <> {" + calendar1.get(Calendar.AM_PM) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.HOUR) != calendar1.get(Calendar.HOUR))
            {
            listMismatches.add("get(HOUR) {" + calendar0.get(Calendar.HOUR)
                               + "} <> {" + calendar1.get(Calendar.HOUR) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.HOUR_OF_DAY) != calendar1.get(Calendar.HOUR_OF_DAY))
            {
            listMismatches.add("get(HOUR_OF_DAY) {" + calendar0.get(Calendar.HOUR_OF_DAY)
                               + "} <> {" + calendar1.get(Calendar.HOUR_OF_DAY) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.MINUTE) != calendar1.get(Calendar.MINUTE))
            {
            listMismatches.add("get(MINUTE) {" + calendar0.get(Calendar.MINUTE)
                               + "} <> {" + calendar1.get(Calendar.MINUTE) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.SECOND) != calendar1.get(Calendar.SECOND))
            {
            listMismatches.add("get(SECOND) {" + calendar0.get(Calendar.SECOND)
                               + "} <> {" + calendar1.get(Calendar.SECOND) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.MILLISECOND) != calendar1.get(Calendar.MILLISECOND))
            {
            listMismatches.add("get(MILLISECOND) {" + calendar0.get(Calendar.MILLISECOND)
                               + "} <> {" + calendar1.get(Calendar.MILLISECOND) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.ZONE_OFFSET) != calendar1.get(Calendar.ZONE_OFFSET))
            {
            listMismatches.add("get(ZONE_OFFSET) {" + calendar0.get(Calendar.ZONE_OFFSET)
                               + "} <> {" + calendar1.get(Calendar.ZONE_OFFSET) + "}");
            boolEqual = false;
            }

        if (calendar0.get(Calendar.DST_OFFSET) != calendar1.get(Calendar.DST_OFFSET))
            {
            listMismatches.add("get(DST_OFFSET) {" + calendar0.get(Calendar.DST_OFFSET)
                               + "} <> {" + calendar1.get(Calendar.DST_OFFSET) + "}");
            boolEqual = false;
            }

        if (!calendar0.getTimeZone().getDisplayName(true, TimeZone.LONG).equals(calendar1.getTimeZone().getDisplayName(true, TimeZone.LONG)))
            {
            listMismatches.add("getTimeZoneCode().getDisplayName() {"
                               + calendar0.getTimeZone().getDisplayName(true, TimeZone.LONG)
                               + "} <> {" + calendar1.getTimeZone().getDisplayName(true, TimeZone.LONG) + "}");
            boolEqual = false;
            }

        if (!calendar0.getTimeZone().hasSameRules(calendar1.getTimeZone()))
            {
            listMismatches.add("getTimeZoneCode().hasSameRules() {}");
            boolEqual = false;
            }

        return(listMismatches);
        }
    }


//------------------------------------------------------------------------------
// End of File


/* extras from algorithms book



    // solar mean longitude
    public static double solarLongitude(double jd)
    {
        double lng = 280.461 + 0.9856474 * (jd - AstronomyConstants.J2000);

        while (lng <   0.0)
            lng += 360.0;

        while (lng > 360.0)
            lng -= 360.0;

        return lng;
    }

    // conversion for Deg/Hours:Min:Sec to Decimal
    public static double angleToDecimal
        (
        int    angle,
        int    min,
        double sec
        )
    {
        return (double)angle + min / 60.0 + sec / 3600.0;
    }

    // calculate angle between points
    public static double angleBetween
        (
        double lat1,
        double lng1,
        double lat2,
        double lng2
        )
    {
        return AstroMath.acosd(AstroMath.cosd(lng1 - lng2) * AstroMath.cosd(lat1) * AstroMath.cosd(lat2)
                           + AstroMath.sind(lat1) * AstroMath.sind(lat2));
    }

    // calculate approximate distance based on angle
    public static double angleToDist
        (
        double a,
        double r
        )
    {
        return ((r + r) * Math.PI * a / 360.0);
    }

    // get day of week from Julian date
    public static int dayOfWeek
        (
        double jd
        )
    {
        return (int)(AstroMath.truncate(7.0 * AstroMath.fraction(AstroMath.truncate(jd + 0.5) / 7.0) + 0.1));
    }

    // extract UT from Julain date
    public static double julianToUT
        (
        double jd,
        double offset
        )
    {
        // separate date and time
        double jd0;

        if (AstroMath.fraction(jd) > 0.5)
            jd0 = AstroMath.truncate(jd) + 0.5;
        else
            jd0 = AstroMath.truncate(jd) - 0.5;

        // calculate time
        double t = (jd - jd0) * 24.0 - offset;

        if (t < 0.0)
            t += 24.0;

        return t;
    }

    // geocentric latitude
    public static double geocentricLatitude
        (
        double lat
        )
    {
        double e = (AstronomyConstants.EARTH_RAD_P * AstronomyConstants.EARTH_RAD_P)
                                      /
                     (AstronomyConstants.EARTH_RAD * AstronomyConstants.EARTH_RAD);

        return AstroMath.atand(e * AstroMath.tand(lat));
    }

    */
