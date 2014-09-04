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
// Astronomy FrameworkConstants
//------------------------------------------------------------------------------
// Revision History
//
//  20-03-02    LMN created file from Java Applications book
//
//------------------------------------------------------------------------------
// Astronomy package
package org.lmn.fc.common.constants;


/***************************************************************************************************
 * AstronomyConstants.
 */

public interface AstronomyConstants
    {
    // Geometry
    double TWO_PI = Math.PI * 2.0;
    double PI_BY_TWO = Math.PI / 2.0;
    double THREE_PI_BY_TWO = Math.PI * 1.5;

    // Radians -- Degrees
    double RADIANS_PER_DEGREE = Math.PI / 180.0;
    double DEGREES_TO_RADIANS = RADIANS_PER_DEGREE;

    double DEGREES_PER_RADIAN = 1.0 / RADIANS_PER_DEGREE;
    double RADIANS_TO_DEGREES = DEGREES_PER_RADIAN;

    // Arcsec -- Degrees
    double ARCSEC_PER_DEGREE = 3600.0;
    double DEGREES_TO_ARCSEC = ARCSEC_PER_DEGREE;

    double DEGREES_PER_ARCSEC = 1.0 / ARCSEC_PER_DEGREE;
    double ARCSEC_TO_DEGREES = DEGREES_PER_ARCSEC;

    // Arcsec -- Radians
    double ARCSEC_PER_RADIAN = DEGREES_PER_RADIAN * DEGREES_TO_ARCSEC;
    double RADIANS_TO_ARCSEC = ARCSEC_PER_RADIAN;

    double RADIANS_PER_ARCSEC = 1.0 / ARCSEC_PER_RADIAN;
    double ARCSEC_TO_RADIANS = RADIANS_PER_ARCSEC;

    // Degrees -- Hours
    double DEGREES_PER_HOUR = 360.0 / 24.0;
    double HOURS_TO_DEGREES = DEGREES_PER_HOUR;

    double HOURS_PER_DEGREE = 1.0 / DEGREES_PER_HOUR;
    double DEGREES_TO_HOURS = HOURS_PER_DEGREE;

    // Hours -- Radians
    double RADIANS_PER_HOUR = DEGREES_PER_HOUR * DEGREES_TO_RADIANS;
    double HOURS_TO_RADIANS = RADIANS_PER_HOUR;

    double HOURS_PER_RADIAN = 1.0 / RADIANS_PER_HOUR;
    double RADIANS_TO_HOURS = HOURS_PER_RADIAN;

    // Time
    double JULIAN_YEAR = 365.25;
    double JULIAN_CENTURY = 100.0 * JULIAN_YEAR;

    double SECONDS_PER_DAY = 86400.0;
    double HOURS_PER_DAY = 24.0;
    double MINUTES_PER_HOUR = 60.0;

    // Miscellaneous
    double SIDEREAL_RATIO = 1.00273790935; // Java book ends with 2558 instead of 35
    double EARTH_RAD = 6378140.0;
    double EARTH_RAD_P = 6356755.0;
    double GEOCENTRIC_K = (EARTH_RAD_P * EARTH_RAD_P) / (EARTH_RAD * EARTH_RAD);
    }
