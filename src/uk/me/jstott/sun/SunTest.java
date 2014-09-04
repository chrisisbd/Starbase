// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

//-----------------------------------------------------------------------------
// SunTest.java
//
// (c) 2004 Jonathan Stott
//
// Created on 30-Mar-2004
//
// 0.2 - 13 Apr 2004
//  - Updated handling of time zones to use the TimeZone class
//  - Changed dates to Calendar objects and used
//    uk.me.jstott.util.JulianDateConverter to convert them to Julian dates
// 0.1 - 30 Mar 2004
//  - First version
//-----------------------------------------------------------------------------

package uk.me.jstott.sun;

import uk.me.jstott.coordconv.LatitudeLongitude;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Example usage of the Sun class.
 *
 * For more information on using this class, look at
 * http://www.jstott.me.uk/jsuntimes/
 *
 * @author Jonathan Stott
 * @version 0.1
 */
public class SunTest {

	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// -------------------------------------------------------------------------
		// Calculate sunrise and sunet times for Canterbury, England for today
		// -------------------------------------------------------------------------

		LatitudeLongitude ll = new LatitudeLongitude(LatitudeLongitude.NORTH,
				51, 17, 38.0, LatitudeLongitude.EAST, 1, 5, 27.0);
		TimeZone gmt = TimeZone.getTimeZone("Europe/London");
		Calendar cal = Calendar.getInstance();
		boolean dst = false;

		System.out.println("\n\nCanterbury, England - "
				+ cal.get(Calendar.DAY_OF_MONTH) + "/"
				+ (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR)
				+ "(" + ll.toString() + ")");

		System.out.println("Astronomical twilight = "
				+ SunFixed.morningAstronomicalTwilightTime(cal, ll, gmt, dst));
		System.out.println("Nautical twilight     = "
				+ SunFixed.morningNauticalTwilightTime(cal, ll, gmt, dst));
		System.out.println("Civil twilight        = "
				+ SunFixed.morningCivilTwilightTime(cal, ll, gmt, dst));
		System.out.println("Sunrise               = "
				+ SunFixed.sunriseTime(cal, ll, gmt, dst));
		System.out.println("Sunset                = "
				+ SunFixed.sunsetTime(cal, ll, gmt, dst));
		System.out.println("Civil twilight        = "
				+ SunFixed.eveningCivilTwilightTime(cal, ll, gmt, dst));
		System.out.println("Nautical twilight     = "
				+ SunFixed.eveningNauticalTwilightTime(cal, ll, gmt, dst));
		System.out.println("Astronomical twilight = "
				+ SunFixed.eveningAstronomicalTwilightTime(cal, ll, gmt, dst));

		// -------------------------------------------------------------------------
		// Calculate sunrise and sunset time for Philadelphia, USA for today
		// -------------------------------------------------------------------------

		LatitudeLongitude ll2 = new LatitudeLongitude(39.9561, -75.1645);
		TimeZone est = TimeZone.getTimeZone("US/Eastern");

		System.out.println("\n\nPhiladelphia, USA - "
				+ cal.get(Calendar.DAY_OF_MONTH) + "/"
				+ (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

		System.out.println("Astronomical twilight = "
				+ SunFixed.morningAstronomicalTwilightTime(cal, ll2, est, dst));
		System.out.println("Nautical twilight     = "
				+ SunFixed.morningNauticalTwilightTime(cal, ll2, est, dst));
		System.out.println("Civil twilight        = "
				+ SunFixed.morningCivilTwilightTime(cal, ll2, est, dst));
		System.out.println("Sunrise               = "
				+ SunFixed.sunriseTime(cal, ll2, est, dst));
		System.out.println("Sunset                = "
				+ SunFixed.sunsetTime(cal, ll2, est, dst));
		System.out.println("Civil twilight        = "
				+ SunFixed.eveningCivilTwilightTime(cal, ll2, est, dst));
		System.out.println("Nautical twilight     = "
				+ SunFixed.eveningNauticalTwilightTime(cal, ll2, est, dst));
		System.out.println("Astronomical twilight = "
				+ SunFixed.eveningAstronomicalTwilightTime(cal, ll2, est, dst));
	}
}
