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
// Revision History
//
//  27-03-02    LMN created file from Java Applications book
//  29-03-02    LMN changed to AstronomicalCalendar
//  01-04-02    LMN added formatting options for output
//  09-04-02    LMN added constructor for Julian Day
//  16-04-02    LMN successfully tested all six constructors, thousands of times!
//
//------------------------------------------------------------------------------
// Time formatting characters
// Symbol   Meaning                 Presentation        Example
// G        era designator          (Text)              AD
// y        year                    (Number)            1996
// M        month in year           (Text & Number)     July & 07
// d        day in month            (Number)            10
// h        hour in am/pm (1~12)    (Number)            12
// H        hour in day (0~23)      (Number)            0
// m        minute in hour          (Number)            30
// s        second in minute        (Number)            55
// S        millisecond             (Number)            978
// E        day in week             (Text)              Tuesday
// D        day in year             (Number)            189
// F        day of week in month    (Number)            2 (2nd Wed in July)
// w        week in year            (Number)            27
// W        week in month           (Number)            2
// a        am/pm marker            (Text)              PM
// k        hour in day (1~24)      (Number)            24
// K        hour in am/pm (0~11)    (Number)            0
// z        time zone               (Text)              Pacific Standard Time
// '        escape for text         (Delimiter)
// ''       single quote            (Literal)           '
//------------------------------------------------------------------------------
// Astronomy package

package org.lmn.fc.common.utilities.time;

import org.lmn.fc.common.constants.AstronomyConstants;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.astronomy.AstroUtilities;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/***************************************************************************************************
 * AstronomicalCalendar.
 */

public final class AstronomicalCalendar extends GregorianCalendar
                                        implements AstronomicalCalendarInterface
    {
    // Internal State of each TimeSystem
    private double dblLMT;                  // Local Mean Time (as entered in constructor)
    private double dblUT;                   // UT (GMT) for the calendar instant
    private double dblJD0;                  // Julian Date at 0 UT (midnight)
    private double dblJD;                   // The Julian Date for the calendar date
    private double dblGMST0;                // Greenwich Mean Sidereal Time at 0 UT
    private double dblGAST0;                // Greenwich Apparent Sidereal Time at 0 UT
    private double dblGMST;                 // Greenwich Mean Sidereal Time
    private double dblGAST;                 // Greenwich Apparent Sidereal Time
    private double dblLMST;                 // Local Mean Sidereal Time
    private double dblLAST;                 // Local Apparent Sidereal Time

    private double dblLongitude;            // The longitude of the place

    private double dblNutationLongitude;    // The nutation in Longitude in SECONDS
    private double dblNutationObliquity;    // The nutation in Obliquity in SECONDS
    private double dblNutationRA;           // The nutation in RA in SECONDS

    private final GregorianCalendar changeoverCalendar;

    // Output Formatting
    private boolean boolEnableFormatSign;   // Enables use of '+/-' on the output

    // The below are not accessible, and are not used in tests of equality
    private final String strConstructor;    // Indicates which Constructor was used
    private boolean boolDebugMode;          // Controls debug messages


    /***********************************************************************************************
     * Set up an HMS object for formatted output.
     *
     * @param angle
     *
     * @return HourMinSecInterface
     */

    private static HourMinSecInterface createHMS(final double angle,
                                                 final boolean formatsign)
        {
        final HourMinSecInterface hmsTemp;

        hmsTemp = new HourMinSecDataType(angle);

        hmsTemp.enableFormatSign(formatsign);

        return (hmsTemp);
        }


    /***********************************************************************************************
     * Construct a Calendar from an existing Calendar and a longitude.
     * The TimeZone comes in with the calendar.
     * Used by StarMap, EphemeridesHelper and ObservatoryClockHelper.
     *
     * @param calendar
     * @param longitude
     */

    public AstronomicalCalendar(final Calendar calendar,
                                final double longitude)
        {
        super(calendar.getTimeZone());

        this.set(calendar.get(Calendar.YEAR),
                 calendar.get(Calendar.MONTH),
                 calendar.get(Calendar.DATE),
                 calendar.get(Calendar.HOUR_OF_DAY),   // Uses 24hr clock
                 calendar.get(Calendar.MINUTE),
                 calendar.get(Calendar.SECOND));
        this.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));

        this.dblLongitude = longitude;

        this.boolEnableFormatSign = true;
        this.boolDebugMode = false;

        this.changeoverCalendar = new GregorianCalendar();
        getChangeoverCalendar().setTime(getChangeoverCalendar().getGregorianChange());

        initialiseAstronomicalCalendarNotJD();

        // Identify the Constructor
        strConstructor = "GregorianCalendar";
        }


    /***********************************************************************************************
     * Construct a Calendar from a Date object, a TimeZone, and a longitude.
     * The Milliseconds come in with the Date.
     * Used by DigitalClock.
     *
     * @param date
     * @param timezone
     * @param longitude
     */

    public AstronomicalCalendar(final Date date,
                                final TimeZone timezone,
                                final double longitude)
        {
        super(timezone);

        this.setTime(date);             // Sets MILLISECOND
        this.dblLongitude = longitude;

        this.boolEnableFormatSign = true;
        this.boolDebugMode = false;

        this.changeoverCalendar = new GregorianCalendar();
        getChangeoverCalendar().setTime(getChangeoverCalendar().getGregorianChange());

        initialiseAstronomicalCalendarNotJD();

        // Identify the Constructor
        strConstructor = "Date";
        }


    /***********************************************************************************************
     * Construct a Calendar from a parseable string, a TimeZone, and a longitude.
     * Note that the Months are {1...12} for ease of use!
     * Example string '2002 4 2 19:13:01' is 2002 April 2nd, 19:13:01.
     * It is not possible to parse a string containing '+/-' on the time portion,
     * or with fractional seconds (so Milliseconds are set to zero).
     * Used by JulianDateConverter.dateToJulian().
     *
     * @param datestring
     * @param timezone
     * @param longitude
     *
     * @throws ParseException
     */

    public AstronomicalCalendar(final String datestring,
                                final TimeZone timezone,
                                final double longitude) throws ParseException
        {
        super(timezone);

        // ToDo Consider ThreadLocal
        final SimpleDateFormat dateFormat;

        // BEWARE use of 'MM', not 'mm' for months
        // and use 'HH' to ensure 24hr clock!
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Don't forget TimeZone in the DateFormat, because input is changed accordingly
        dateFormat.setCalendar(this);
        dateFormat.setTimeZone(timezone);
        this.setTimeZone(timezone);

        this.setTime(dateFormat.parse(datestring));
        this.set(Calendar.MILLISECOND, 0);

        this.dblLongitude = longitude;

        this.boolEnableFormatSign = true;
        this.boolDebugMode = false;

        this.changeoverCalendar = new GregorianCalendar();
        getChangeoverCalendar().setTime(getChangeoverCalendar().getGregorianChange());

        initialiseAstronomicalCalendarNotJD();

        // Identify the Constructor
        strConstructor = "Parser";
        }


    /***********************************************************************************************
     * Construct a Calendar from YMD & HMS objects, a TimeZone, and a longitude
     * Note that the YMD object contains normal Month numbering {1...12}
     * because it is most likely to be used outside a Calendar.
     * The HMS object can contain fractional seconds.
     * Used in CalculateTopocentricEphemeris.doCalculateTopocentricEphemeris().
     *
     * @param yearmonthday
     * @param hourminsec
     * @param timezone
     * @param longitude
     */

    public AstronomicalCalendar(final YearMonthDayInterface yearmonthday,
                                final HourMinSecInterface hourminsec,
                                final TimeZone timezone,
                                final double longitude)
        {
        super(timezone);

        this.set(yearmonthday.getYear(),
                 yearmonthday.getMonth()-1,  // Note that the YMD object contains normal Month numbering {1...12}
                 yearmonthday.getDay(),
                 hourminsec.getHours(),
                 hourminsec.getMinutes(),
                 (int)AstroMath.truncate(hourminsec.getSeconds()));

        final int intMillisecond;

        // Check for Millisecond rounding errors
        intMillisecond = (int)Math.rint(AstroMath.fraction(hourminsec.getSeconds())*1000.0);

        if (intMillisecond == 1000)
            {
            // Rollover has occurred, so correct it
            this.set(Calendar.MILLISECOND, 0);
            this.add(Calendar.SECOND, 1);
            }
        else
            {
            // Otherwise {000...999}
            this.set(Calendar.MILLISECOND, intMillisecond);
            }

        this.dblLongitude = longitude;

        this.boolEnableFormatSign = true;
        this.boolDebugMode = false;

        this.changeoverCalendar = new GregorianCalendar();
        getChangeoverCalendar().setTime(getChangeoverCalendar().getGregorianChange());

        initialiseAstronomicalCalendarNotJD();

        // Identify the Constructor
        strConstructor = "YMD & HMS";
        }


    /***********************************************************************************************
     * Construct a Calendar from a Julian Day, a TimeZone, and a longitude.
     * Used in EphemeridesHelper.generateEphemerisData().
     *
     * @param julianday
     * @param timezone
     * @param longitude
     */

    public AstronomicalCalendar(final double julianday,
                                final TimeZone timezone,
                                final double longitude)
        {
        super(timezone);

        // The algorithm doesn't work for negative JD
        if (julianday < 0.0)
            {
            throw new FrameworkException(EXCEPTION_PARAMETERJDNEG);
            }

        this.dblJD = julianday;
        this.dblLongitude = longitude;

        this.boolEnableFormatSign = true;
        this.boolDebugMode = false;

        this.changeoverCalendar = new GregorianCalendar();
        getChangeoverCalendar().setTime(getChangeoverCalendar().getGregorianChange());

        initialiseAstronomicalCalendarJD(this.dblJD, timezone);

        // Identify the Constructor
        strConstructor = "JulianDay";
        }


    /***********************************************************************************************
     * Construct a Calendar from a fully-specified date, a time zone and a longitude.
     * Note that Calendar months are {0...11} for consistency with GregorianCalendar.
     * Fractional seconds may be used.
     * No current usages.
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @param timezone
     * @param longitude
     */

    public AstronomicalCalendar(final int year,
                                final int month,
                                final int day,
                                final int hour,
                                final int minute,
                                final double second,
                                final TimeZone timezone,
                                final double longitude)
        {
        super(timezone);

        this.set(year,
                 month,
                 day,
                 hour,
                 minute,
                 (int)AstroMath.truncate(second));

        final int intMillisecond;

        // Check for Millisecond rounding errors
        intMillisecond = (int)Math.rint(AstroMath.fraction(second)*1000.0);

        if (intMillisecond == 1000)
            {
            // Rollover has occurred, so correct it
            this.set(Calendar.MILLISECOND, 0);
            this.add(Calendar.SECOND, 1);
            }
        else
            {
            // Otherwise {000...999}
            this.set(Calendar.MILLISECOND, intMillisecond);
            }

        this.dblLongitude = longitude;

        this.boolEnableFormatSign = true;
        this.boolDebugMode = false;

        this.changeoverCalendar = new GregorianCalendar();
        getChangeoverCalendar().setTime(getChangeoverCalendar().getGregorianChange());

        initialiseAstronomicalCalendarNotJD();

        // Identify the Constructor
        strConstructor = "y,m,d,h,m,s";
        }


    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Get the internal Calendar. If Calendar used an interface, this wouldn't be necessary!
     *
     * @return Calendar
     */

    public Calendar getCalendar()
        {
        return (this);
        }


    /***********************************************************************************************
     * Sets this Calendar's current time from the given long value.
     *
     * @param millis the new time in UTC milliseconds from the epoch.
     */

    public void setTimeInMillis(final long millis)
        {
        super.setTimeInMillis(millis);

        // Recalculate all astronomical fields to ensure consistency with the new Time
        initialiseAstronomicalCalendarNotJD();
        }


    /***********************************************************************************************
     * Utility to initialise AstronomicalCalendars from everything except JD.
     */

    private void initialiseAstronomicalCalendarNotJD()
        {
        final double dblYear;
        final double dblMonth;
        final double dblDay;
        final double dblLeapCorrection;
        double dblHourCorrection;

        //----------------------------------------------------------------------
        // Record the time as entered, as Local Mean Time

        dblLMT = (double)get(Calendar.HOUR_OF_DAY)
                 + (double)get(Calendar.MINUTE) / 60.0
                 + (double)get(Calendar.SECOND) / 3600.0
                 + (double)get(Calendar.MILLISECOND) / 3600000.0;

        //----------------------------------------------------------------------
        // Calculate Julian Date at 0 UT on the specified calendar date
        // Note that Calendar months are {0...11}, but we need {1...12}
        //(Meeus page 61 eq. 7.1)

        if (get(Calendar.MONTH) >= 2)      // >= March
            {
            dblYear = get(Calendar.YEAR);
            dblMonth = get(Calendar.MONTH) + 1.0;
            }
        else
            {
            dblYear = get(Calendar.YEAR) - 1.0;
            dblMonth = get(Calendar.MONTH) + 13.0;
            }

//        System.out.println("CALENDAR DEBUG initialiseAstronomicalCalendarNotJD()");
//        System.out.println("(double)get(Calendar.HOUR_OF_DAY) = " + (double)get(Calendar.HOUR_OF_DAY));
//        System.out.println("((double)get(Calendar.DST_OFFSET) / (60*60*1000)) = " + ((double)get(Calendar.DST_OFFSET) / (60*60*1000)));
//        System.out.println("((double)get(Calendar.ZONE_OFFSET) / (60*60*1000)) = " + ((double)get(Calendar.ZONE_OFFSET) / (60*60*1000)));

        // Correct the Hour for Daylight Saving Time and Zone Offset
        dblHourCorrection = (double)get(Calendar.HOUR_OF_DAY)
                           - ((double)get(Calendar.DST_OFFSET) / (60*60*1000))
                           - ((double)get(Calendar.ZONE_OFFSET) / (60*60*1000));

        dblHourCorrection = AstroUtilities.adjustRange(dblHourCorrection, 24.0);
        //System.out.println("ADJUSTED OFFSET = " + dblHourCorrection);

        // Convert dblDay to a fraction of a DAY
        dblDay = (dblHourCorrection
                 + (double)get(Calendar.MINUTE) / 60.0
                 + (double)get(Calendar.SECOND) / 3600.0
                 + (double)get(Calendar.MILLISECOND) / 3600000.0) / 24.0;

        dblLeapCorrection = 2.0
                            - AstroMath.truncate(dblYear / 100.0)
                            + AstroMath.truncate(dblYear / 400.0);

        dblJD0 = 1720994.5
                 + AstroMath.truncate(365.25 * dblYear)
                 + AstroMath.truncate(30.6001 * (dblMonth + 1.0))
                 + (double)get(Calendar.DAY_OF_MONTH);

        // Check for date before or after 15/10/1582 (or whenever)
        if (after(getChangeoverCalendar()))
            {
            // Leap year determination
            dblJD0 += dblLeapCorrection;
            }

        //----------------------------------------------------------------------
        // Calculate the Julian Date at the specified calendar date (JD)

        dblJD = 1720994.5
                + AstroMath.truncate(365.25 * dblYear)
                + AstroMath.truncate(30.6001 * (dblMonth + 1.0))
                + (double)get(Calendar.DAY_OF_MONTH)
                + dblDay;

        // Check for date before or after 15/10/1582 (or whenever)
        if (after(getChangeoverCalendar()))
            {
            // Leap year determination
            dblJD += dblLeapCorrection;
            }

        //----------------------------------------------------------------------
        // Set Universal Time (GMT)
        // dblDay is the time of day as a fraction of a day, so convert to HOURS

        dblUT = dblDay * 24.0;

        //----------------------------------------------------------------------
        // Fill in the sidereal times

        calculateSiderealTimes(dblDay, dblJD0);
        }


    /***********************************************************************************************
     * Utility to initialise AstronomicalCalendars from a Julian Day.
     *
     * @param jd
     * @param timezone
     */

    private void initialiseAstronomicalCalendarJD(final double jd,
                                                  final TimeZone timezone)
        {
        final double dblYear;
        final double dblMonth;
        final double dblDay;
        final double dblHour;
        final double dblMin;
        double dblSec;
        int intMillisecond;
        double dblTime;
        double dblTimeTemp;
        final double dblHourCorrection;

        // We know only jd and dblLongitude...
        // Find the time of day in HOURS
        dblTime = 0.0;

        // Firstly, work backwards to find dblJD0 and dblTime
        if (AstroMath.fraction(jd) == 0.5)
            {
            // 00:00:00
            dblJD0 = jd;
            dblTime = 0.0;
            }
        else if (AstroMath.fraction(jd) > 0.5)
            {
            // 00:00:01 -> 11:59:59
            dblJD0 = AstroMath.truncate(jd) + 0.5;
            dblTime = ((jd - dblJD0) * 24.0) ;  //- timezone.getRawOffset();
            }
        else if(AstroMath.fraction(jd) < 0.5)
            {
            // 12:00:00 -> 23:59:59
            dblJD0 = AstroMath.truncate(jd) - 0.5;
            dblTime = ((jd - dblJD0) * 24.0) ;  //- timezone.getRawOffset();
            }

 /*       if (dblTime < 0.0)
            {
            System.out.println("TIMEZONE??");
            dblTime += 24.0;
        // 24:00:00 -> 00:00:00
        dblTime = AstroUtilities.adjustRange(dblTime, 24.0);
            dblJD0 -= 1.0;
            }
*/

        //----------------------------------------------------------------------
        // The HMS will be in UT

        // dblTime is in HOURS
        dblHour = (int)AstroMath.truncate(dblTime);
        dblTimeTemp = AstroMath.fraction(dblTime) * 60.0;

        // dblTimeTemp is now in MINUTES
        dblMin = (int)AstroMath.truncate(dblTimeTemp);
        dblTimeTemp = AstroMath.fraction(dblTimeTemp) * 60.0;

        // dblTimeTemp is now in SECONDS
        dblSec = (int)AstroMath.truncate(dblTimeTemp);
        dblTimeTemp = AstroMath.fraction(dblTimeTemp) * 1000.0;

        // dblTimeTemp is now in MILLISECONDS {0...999.999999}
        intMillisecond = (int)Math.rint(dblTimeTemp);

        if (intMillisecond == 1000)
            {
            intMillisecond = 0;
            dblSec += 1.0;
            }

        //----------------------------------------------------------------------
        // Calculate the date (Meeus pg 63)
        // AstroMath.truncate() == INT()

        final double dblMeeusZ = AstroMath.truncate(dblJD0) + 1.0;
        final double dblMeeusA;

        if (dblMeeusZ < 2299161.0)
            {
            dblMeeusA = dblMeeusZ;
            }
        else
            {
            final double dblMeeusAlpha = AstroMath.truncate((dblMeeusZ - 1867216.25) / 36524.25);
            dblMeeusA = dblMeeusZ
                        + 1.0
                        + dblMeeusAlpha
                        - AstroMath.truncate(dblMeeusAlpha / 4.0);
            }

        final double dblMeeusB = dblMeeusA + 1524.0;
        final double dblMeeusC = AstroMath.truncate((dblMeeusB - 122.1) / 365.25);
        final double dblMeeusD = AstroMath.truncate(365.25 * dblMeeusC);
        final double dblMeeusE = AstroMath.truncate((dblMeeusB - dblMeeusD) / 30.6001);

        // Slight diversion from Meeus...
        dblDay = (int)(dblMeeusB - dblMeeusD - AstroMath.truncate(30.6001 * dblMeeusE));

        final double dblMonthTemp;

        if (dblMeeusE < 13.5)
            {
            dblMonthTemp = dblMeeusE - 1.0;
            }
        else
            {
            dblMonthTemp = dblMeeusE - 13.0;
            }

        dblMonth = (int)dblMonthTemp;

        if (dblMonthTemp > 2.5)
            {
            dblYear = (int)(dblMeeusC - 4716.0);
            }
        else
            {
            dblYear = (int)(dblMeeusC - 4715.0);
            }

        //----------------------------------------------------------------------
        // So, we now have JD0, JD, yyyy/mm/dd hh:mm:ss
        //----------------------------------------------------------------------
        // Set Universal Time (GMT) (derived directly from JD0)

        dblUT = dblHour
                + (dblMin / 60.0)
                + (dblSec / 3600.0)
                + ((double)intMillisecond / 3600000.0);

        //----------------------------------------------------------------------
        // Set Local Mean Time (LMT), working back from UT and the TimeZone
        // Check to see if the date is in the DST period

        dblHourCorrection = ((double)get(Calendar.DST_OFFSET) / (60*60*1000))
                             + ((double)get(Calendar.ZONE_OFFSET) / (60*60*1000));

        dblLMT = dblUT + dblHourCorrection;

        //----------------------------------------------------------------------
        // Now set the time of the created AstronomicalCalendar using LMT
        // This will use the TimeZone set up earlier
        // Note that Months are {0...11}

        this.set((int)dblYear,
                 (int)dblMonth-1,
                 (int)dblDay,
                 (int)(dblHour + dblHourCorrection),
                 (int)dblMin,
                 (int)dblSec);

        this.set(MILLISECOND, intMillisecond);

        //----------------------------------------------------------------------
        // Finally, fill in the sidereal times
        // dblTime is the time of day in HOURS, so convert to DAYS

        calculateSiderealTimes((dblTime / 24.0), dblJD0);
        }


    /***********************************************************************************************
     * Calculate the various sidereal times for the calendar.
     * This is common to creation by a Date or by a JD
     * 'day' is the time of day in units of DAYS (i.e. n.nnnn).
     *
     * @param day
     * @param jd0
     */

    private void calculateSiderealTimes(final double day,
                                        final double jd0)
        {
        final double dblT;                    // Julian centuries since 2000

        //----------------------------------------------------------------------
        // Calculate the Greenwich Mean Sidereal Time at 0hr UT (GMST0)
        // The Mean Sidereal Time is the intersection of the Ecliptic of date
        // with the mean equator of the date

        // Convert JD0 above to Julian centuries since 2000
        dblT = AstroUtilities.J2000Centuries(jd0);

        // Work out the nutation only once...
        calculateNutationCorrectionsRA(dblT);

        // GMST at 0h UT, in SECONDS (Meeus eq. 11.2)
        // This formula is valid only at 0hr UT
        dblGMST0 = 24110.54841              // 6h 41m 50.54841s
                 + 8640184.812866 * dblT
                 + 0.093104 * dblT * dblT
                 - 0.0000062 * dblT * dblT * dblT;

        // Convert the GMST0 to hours, in the correct range
        dblGMST0 /= 3600.0;

        dblGMST0 = AstroUtilities.adjustRange(dblGMST0, 24.0);

        //----------------------------------------------------------------------
        // Calculate the Greenwich Apparent Sidereal Time at 0hr UT (GAST0)

        dblGAST0 = dblGMST0 + (getNutationRA() / 3600.0);

        dblGAST0 = AstroUtilities.adjustRange(dblGAST0, 24.0);

        //----------------------------------------------------------------------
        // Now find the Greenwich Mean Sidereal Time for the calendar date
        // The offset from Ohr UT (time of day) in hours is dblDay*24
        // The sidereal time in SIDEREAL HOURS is:

        dblGMST = dblGMST0 + (day * 24.0 * AstronomyConstants.SIDEREAL_RATIO);

        dblGMST = AstroUtilities.adjustRange(dblGMST, 24.0);

        //----------------------------------------------------------------------
        // Now find the Greenwich Apparent Sidereal Time for the calendar date

        dblGAST = dblGMST + (getNutationRA() / 3600.0);

        dblGAST = AstroUtilities.adjustRange(dblGAST, 24.0);

        //----------------------------------------------------------------------
        // Find the Local Mean Sidereal Time for the calendar date

        // Adjust GMST for the longitude of observation to give the LMST
        dblLMST = dblGMST - (getLongitude() / 15.0);

        dblLMST = AstroUtilities.adjustRange(dblLMST, 24.0);

        //----------------------------------------------------------------------
        // Find the Local Apparent Sidereal Time for the calendar date

        dblLAST = dblLMST + (getNutationRA() / 3600.0);

        dblLAST = AstroUtilities.adjustRange(dblLAST, 24.0);
        }


    /**********************************************************************************************/
    /* TimeSystems                                                                                */
    /***********************************************************************************************
     * Read the Julian Day at 0hr UT (JD0).
     *
     * @return double
     */

    public final double getJD0()
        {
        return(this.dblJD0);
        }


    /***********************************************************************************************
     * Read the Julian Day at the calendar date (JD).
     *
     * @return double
     */

    public final double getJD()
        {
        return(this.dblJD);
        }


    /***********************************************************************************************
     * Read the Local Mean Time (LMT).
     *
     * @return double
     */

    public final double getLMT()
        {
        return(this.dblLMT);
        }


    /***********************************************************************************************
     * Read the Universal Time (UT).
     *
     * @return double
     */

    public final double getUT()
        {
        return(this.dblUT);
        }


    /***********************************************************************************************
     * Read the Greenwich Mean Sidereal Time at 0hr UT (GMST0).
     *
     * @return double
     */

    public final double getGMST0()
        {
        return(this.dblGMST0);
        }


    /***********************************************************************************************
     * Read the Greenwich Apparent Sidereal Time at 0hr UT (GAST0).
     *
     * @return double
     */

    public final double getGAST0()
        {
        return(this.dblGAST0);
        }


    /***********************************************************************************************
     * Read the Greenwich Mean Sidereal Time (GMST).
     *
     * @return double
     */

    public final double getGMST()
        {
        return(this.dblGMST);
        }


    /***********************************************************************************************
     * Read the Greenwich Apparent Sidereal Time (GAST).
     *
     * @return double
     */

    public final double getGAST()
        {
        return(this.dblGAST);
        }


    /***********************************************************************************************
     * Read the Local Mean Sidereal Time (LMST).
     *
     * @return double
     */

    public final double getLMST()
        {
        return(this.dblLMST);
        }


    /***********************************************************************************************
     * Read the Local Apparent Sidereal Time (LAST).
     *
     * @return double
     */

    public final double getLAST()
        {
        return(this.dblLAST);
        }


    /***********************************************************************************************
     * Get the Longitude of the place of observation.
     *
     * @return double
     */

    public final double getLongitude()
        {
        return(this.dblLongitude);
        }


    /***********************************************************************************************
     * Set the Longitude of the place of observation.
     * Update the Calendar accordingly.
     *
     * @param longitude
     */

    public void setLongitude(final double longitude)
        {
        this.dblLongitude = longitude;

        // Recalculate all astronomical fields to ensure consistency with the new Location
        initialiseAstronomicalCalendarNotJD();
        }


    /***********************************************************************************************
     * Find out when the Gregorian calendar changed from the Julian calendar.
     *
     * @return GregorianCalendar
     */

    private GregorianCalendar getChangeoverCalendar()
        {
        return (this.changeoverCalendar);
        }


    /***********************************************************************************************
     * Returns the value of the given calendar field. In lenient mode,
     * all calendar fields are normalized. In non-lenient mode, all
     * calendar fields are validated and this method throws an
     * exception if any calendar fields have out-of-range values. The
     * normalization and validation are handled by the
     * {@link #complete()} method, which process is calendar
     * system dependent.
     *
     * @param field the given calendar field.
     *
     * @return the value for the given calendar field.
     *
     * @throws ArrayIndexOutOfBoundsException if the specified field is out of range
     *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
     */

    public int get(final int field)
        {
        return (super.get(field));
        }


    /**********************************************************************************************
     * Adds or subtracts (up/down) a single unit of time on the given time
     * field without changing larger fields.
     * <p>
     * <em>Example</em>: Consider a <code>GregorianCalendar</code>
     * originally set to December 31, 1999. Calling {@link #roll(int,boolean) roll(Calendar.MONTH, true)}
     * sets the calendar to January 31, 1999.  The <code>YEAR</code> field is unchanged
     * because it is a larger field than <code>MONTH</code>.</p>
     *
     * @param up indicates if the value of the specified calendar field is to be
     * rolled up or rolled down. Use <code>true</code> if rolling up, <code>false</code> otherwise.
     * @exception IllegalArgumentException if <code>field</code> is
     * <code>ZONE_OFFSET</code>, <code>DST_OFFSET</code>, or unknown,
     * or if any calendar fields have out-of-range values in
     * non-lenient mode.
     */

    public void roll(final int field,
                     final boolean up)
        {
        super.roll(field, up);
        }


    /**********************************************************************************************/
    /* Nutation                                                                                   */
    /***********************************************************************************************
     * Calculate the correction in time (RA) from nutations in the longitude and obliquity.
     * See: Meeus pp 132 et seq.
     *
     * @param juliancenturies
     */

    private void calculateNutationCorrectionsRA(final double juliancenturies)
        {
        double dblAscendingLongitudeMoon;
        double dblMeanLongitudeSun;
        double dblMeanLongitudeMoon;
        double dblMeanObliquity;
        final double dblTrueObliquity;

        // Approximate equations giving 0''.5 in longitude and 0''.1 in obliquity
        // All results in DEGREES  (Meeus pp 132 et seq.)
        dblAscendingLongitudeMoon = 125.04452 - (1934.136261 * juliancenturies);
        dblMeanLongitudeSun = 280.4665 + (36000.7698 * juliancenturies);
        dblMeanLongitudeMoon = 218.3165 + (481267.8813 * juliancenturies);
        dblMeanObliquity = 23.439291 - (0.013004167 * juliancenturies);

        // Now put into the correct range of {0...360}
        dblAscendingLongitudeMoon = AstroUtilities.adjustRange(dblAscendingLongitudeMoon, 360.0);
        dblMeanLongitudeSun = AstroUtilities.adjustRange(dblMeanLongitudeSun, 360.0);
        dblMeanLongitudeMoon = AstroUtilities.adjustRange(dblMeanLongitudeMoon, 360.0);
        dblMeanObliquity = AstroUtilities.adjustRange(dblMeanObliquity, 360.0);

        // Now calculate the nutations (results in SECONDS of Arc)
        dblNutationLongitude = -17.2 * AstroMath.sind(dblAscendingLongitudeMoon)
                               - 1.32 * AstroMath.sind(2 * dblMeanLongitudeSun)
                               - 0.23 * AstroMath.sind(2 * dblMeanLongitudeMoon)
                               + 0.21 * AstroMath.sind(2 * dblAscendingLongitudeMoon);

        dblNutationObliquity = 9.2 * AstroMath.cosd(dblAscendingLongitudeMoon)
                               + 0.57 * AstroMath.cosd(2 * dblMeanLongitudeSun)
                               + 0.1 * AstroMath.cosd(2 * dblMeanLongitudeMoon)
                               + 0.09 * AstroMath.cosd(2 * dblAscendingLongitudeMoon);

        // Find the true obliquity, in DEGREES
        dblTrueObliquity = dblMeanObliquity + (getNutationObliquity() / 3600.0);

        // The correction in SECONDS of time is given by (Meeus pg 84)
        // This is the Equation of the Equinoxes, or Nutation in Right Ascension
        dblNutationRA = getNutationLongitude()
                        * AstroMath.cosd(dblTrueObliquity)
                        / 15.0;

        LOGGER.debug(isDebugMode(), "julianCenturies " + juliancenturies);
        LOGGER.debug(isDebugMode(), "dblAscendingLongitudeMoon " + dblAscendingLongitudeMoon);
        LOGGER.debug(isDebugMode(), "dblMeanLongitudeSun " + dblMeanLongitudeSun);
        LOGGER.debug(isDebugMode(), "dblMeanLongitudeMoon " + dblMeanLongitudeMoon);
        LOGGER.debug(isDebugMode(), "dblMeanObliquity (deg) " + dblMeanObliquity);
        LOGGER.debug(isDebugMode(), "dblNutationObliquity (arcsec) " + getNutationObliquity());
        LOGGER.debug(isDebugMode(), "dblTrueObliquity (deg) " + dblTrueObliquity);
        LOGGER.debug(isDebugMode(), "dblNutationLongitude (arcsec) " + getNutationLongitude());
        LOGGER.debug(isDebugMode(), "dblNutationRA (sec) " + getNutationRA());
        }


    /***********************************************************************************************
     * Read the Nutation in Longitude in SECONDS.
     *
     * @return double
     */

    public final double getNutationLongitude()
        {
        return(this.dblNutationLongitude);
        }


    /***********************************************************************************************
     * Read the Nutation in Obliquity in SECONDS.
     *
     * @return double
     */

    public final double getNutationObliquity()
        {
        return(this.dblNutationObliquity);
        }


    /***********************************************************************************************
     * Read the Nutation in RA in SECONDS.
     *
     * @return double
     */

    public final double getNutationRA()
        {
        return(this.dblNutationRA);
        }


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Get the Year Month Day as a YMD object.
     * Note that the 'normal' Month numbering is used {1...12}!
     *
     * @return YearMonthDayInterface
     */

    public final YearMonthDayInterface toYMD()
        {
        final boolean boolYearPositive;

        boolYearPositive = this.get(Calendar.YEAR) >= 0;

        return(new YearMonthDayDataType(boolYearPositive,
                                        this.get(Calendar.YEAR),
                                        (this.get(Calendar.MONTH) + 1),
                                        this.get(Calendar.DAY_OF_MONTH)));
        }


    /***********************************************************************************************
     * Get the Hour Min Sec as an HMS object.
     *
     * @return HourMinSecInterface
     */

    public final HourMinSecInterface toHMS()
        {
        return(new HourMinSecDataType(true,
                                      this.get(Calendar.HOUR_OF_DAY),
                                      this.get(Calendar.MINUTE),
                                      this.get(Calendar.SECOND)));     // ToDO Add milliseconds?
        }


    /***********************************************************************************************
     * Indicate if this AstronomicalCalendar has the sign preceding the value.
     *
     * @return boolean
     */

    public boolean hasFormatSign()
        {
        return (this.boolEnableFormatSign);
        }


    /***********************************************************************************************
     * Control the use of a sign (+/-) on the formatted output.
     *
     * @param sign
     */

    public final void enableFormatSign(final boolean sign)
        {
        this.boolEnableFormatSign = sign;
        }


    /***********************************************************************************************
     * Get the current Time of a TimeSystem as a double.
     *
     * @param timesystem
     *
     * @return double
     */

    public final double toDouble(final TimeSystem timesystem)
        {
        final double dblTS;

        switch (timesystem)
            {
            case LMT:      { dblTS = this.dblLMT;   break; }
            case UT:       { dblTS = this.dblUT;    break; }
            case JD0:      { dblTS = this.dblJD0;   break; }
            case JD:       { dblTS = this.dblJD;    break; }
            case GMST0:    { dblTS = this.dblGMST0; break; }
            case GAST0:    { dblTS = this.dblGAST0; break; }
            case GMST:     { dblTS = this.dblGMST;  break; }
            case GAST:     { dblTS = this.dblGAST;  break; }
            case LMST:     { dblTS = this.dblLMST;  break; }
            case LAST:     { dblTS = this.dblLAST;  break; }

            default:       { dblTS = 0.0;                  }
            }

        return (dblTS);
        }


    /***********************************************************************************************
     * Return the current Time of a Time system as a formatted, delimited String.
     * Primarily intended for Clocks.
     * Note that seconds are rounded to the nearest integer,
     * so internally a time may be 12:34:56.995, but it will display as 12:34:57.
     * Note that if the TimeSystem is JulianDay, then the JD format is used.
     *
     * @param timesystem
     *
     * @return String
     */

    public String toString_HH_MM_SS(final TimeSystem timesystem)
        {
        final String strFormatted;

        switch (timesystem)
            {
            case JD0:
            case JD:
                {
                // We need to treat Julian Days as a special case
                // JD cannot be negative, so don't call hasFormatSign()
                strFormatted = DecimalFormatPattern.JD.format(toDouble(timesystem));
                break;
                }

            default:
                {
                // Round to the nearest integer Second
                strFormatted = createHMS(toDouble(timesystem),
                                         hasFormatSign()).toString_HH_MM_SS();
                }
            }

        return (strFormatted);
        }


    /***********************************************************************************************
     * Read the current Time of a Time system as a formatted string.
     * Preserve the full millisecond precision,
     * so if the time is required for a Clock, use toString_HH_MM_SS() instead.
     *
     * @param timesystem
     *
     * @return String
     */

    public final String toString(final TimeSystem timesystem)
        {
        final String strFormatted;

        switch (timesystem)
            {
            // We need to treat Julian Days as a special case
            // JD cannot be negative, so don't call hasFormatSign()
            case JD0:
                {
                strFormatted = DecimalFormatPattern.JD.format(this.dblJD0);
                break;
                }

            case JD :
                {
                strFormatted = DecimalFormatPattern.JD.format(this.dblJD);
                break;
                }

            default:
                {
                // Preserve the full millisecond precision
                strFormatted = createHMS(toDouble(timesystem),
                                         hasFormatSign()).toString();
                }
            }

        return (strFormatted);
        }


    /**********************************************************************************************/
    /* Debugs                                                                                     */
    /**********************************************************************************************
     * Get the name of the Constructor of this Calendar.
     *
     * @return String
     */

    private String getConstructorName()
        {
        return(this.strConstructor);
        }


    /***********************************************************************************************
     * Get the Debug Mode flag.
     *
     * @return boolean
     */

    public final boolean isDebugMode()
        {
        return(this.boolDebugMode);
        }


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    public final void setDebugMode(final boolean flag)
        {
        this.boolDebugMode = flag;
        }


    /***********************************************************************************************
     * Reveal the inner contents of an AstronomicalCalendar.
     *
     * @param message
     */

    public final void showCalendarDebug(final String message)
        {
        LOGGER.logTimedEvent("---------------------------------------------------");
        LOGGER.logTimedEvent(message);
        LOGGER.logTimedEvent("Calendar Constructed by " + strConstructor);
        LOGGER.logTimedEvent("Calendar getTime() " + getTime().toString());

        LOGGER.logTimedEvent("JD0 " + toString(TimeSystem.JD0));
        LOGGER.logTimedEvent("JD  " + toString(TimeSystem.JD));
        LOGGER.logTimedEvent("LMT " + toString(TimeSystem.LMT));
        LOGGER.logTimedEvent("UT " + toString(TimeSystem.UT));
        LOGGER.logTimedEvent("UT " + toString_HH_MM_SS(TimeSystem.UT));

        LOGGER.logTimedEvent("GMST0  " + toString(TimeSystem.GMST0));
        LOGGER.logTimedEvent("GAST0  " + toString(TimeSystem.GAST0));
        LOGGER.logTimedEvent("GMST   " + toString(TimeSystem.GMST));
        LOGGER.logTimedEvent("GAST   " + toString(TimeSystem.GAST));
        LOGGER.logTimedEvent("LMST   " + toString(TimeSystem.LMST));
        LOGGER.logTimedEvent("LAST   " + toString(TimeSystem.LAST));

        LOGGER.logTimedEvent("ERA " + get(Calendar.ERA));
        LOGGER.logTimedEvent("YEAR " + get(Calendar.YEAR));
        LOGGER.logTimedEvent("MONTH " + get(Calendar.MONTH));
        LOGGER.logTimedEvent("WEEK_OF_YEAR " + get(Calendar.WEEK_OF_YEAR));
        LOGGER.logTimedEvent("WEEK_OF_MONTH " + get(Calendar.WEEK_OF_MONTH));
        LOGGER.logTimedEvent("DATE " + get(Calendar.DATE));
        LOGGER.logTimedEvent("DAY_OF_MONTH " + get(Calendar.DAY_OF_MONTH));
        LOGGER.logTimedEvent("DAY_OF_YEAR " + get(Calendar.DAY_OF_YEAR));
        LOGGER.logTimedEvent("DAY_OF_WEEK " + get(Calendar.DAY_OF_WEEK));
        LOGGER.logTimedEvent("DAY_OF_WEEK_IN_MONTH " + get(Calendar.DAY_OF_WEEK_IN_MONTH));

        LOGGER.logTimedEvent("AM_PM " + get(Calendar.AM_PM));
        LOGGER.logTimedEvent("HOUR " + get(Calendar.HOUR));
        LOGGER.logTimedEvent("HOUR_OF_DAY " + get(Calendar.HOUR_OF_DAY));
        LOGGER.logTimedEvent("MINUTE " + get(Calendar.MINUTE));
        LOGGER.logTimedEvent("SECOND " + get(Calendar.SECOND));
        LOGGER.logTimedEvent("MILLISECOND " + get(Calendar.MILLISECOND));

        LOGGER.logTimedEvent("TIMEZONE " + getTimeZone().getDisplayName(true, TimeZone.LONG));
        LOGGER.logTimedEvent("ZONE_OFFSET " + (get(Calendar.ZONE_OFFSET)/(60*60*1000)));
        LOGGER.logTimedEvent("DST_OFFSET " + (get(Calendar.DST_OFFSET)/(60*60*1000)));
        LOGGER.logTimedEvent("LONGITUDE " + getLongitude());
        LOGGER.logTimedEvent("---------------------------------------------------");
        }
    }







/*
        //----------------------------------------------------------------------
        // Compute Local Mean sidereal time at 0hr

        dblLMST0 = dblGMST0 + (dblZoneCorrection * AstronomyConstants.SIDEREAL_RATIO);

        // Adjust for the longitude of observation
        dblLMST0 = dblLMST0 - (longitude / 15.0);

        if (dblLMST0 >= 24.0)
            {
            dblLMST0 -= 24.0;
            }
      */
             //----------------------------------------------------------------------
        // Correct the Julian Date for the timezone offset,
 /*
        dblTimeOfDay = AstroUtilities.angleToDecimal(calendar.get(Calendar.HOUR_OF_DAY),
                                                     calendar.get(Calendar.MINUTE),
                                                     calendar.get(Calendar.SECOND));

        dblZoneCorrection = dblTimeOfDay + timezone;

        if (dblZoneCorrection < 0.0)
            {
            // The Date refers to the previous day
            dblZoneCorrection += 24.0;
            dblJD0 -=  1.0;
            }
        else if (dblZoneCorrection >= 24.0)
            {
            // The Date refers to the next day
            dblZoneCorrection  -= 24.0;
            dblJD0 += 1.0;
            }

        //----------------------------------------------------------------------
        // Calculate the Julian Date at the specified time (JD)
        // DON'T UNDERSTAND!!
        // Add UT sidereal time to Julian Date

        dblJD = dblJD0 + (dblZoneCorrection / 24.0);
*/

