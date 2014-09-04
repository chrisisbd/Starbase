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

//-----------------------------------------------------------------------------
// Test.java
//
// (c) 2004 Jonathan Stott
//
// Created on 02-Mar-2004
//
// 0.2 - 02 Mar 2004
//  - Initial Version
//-----------------------------------------------------------------------------

package org.lmn.fc.common.utilities.coords;

/**
 * Example conversions
 *
 * @author Jonathan Stott
 * @version 0.2
 * @since 0.2
 */
public class Test {

  public static void main(String[] args) {
    UTMReference utm = new UTMReference(456463.99,
                                        3335334.05,
                                        'R',
                                        12);
    LatitudeLongitude ll =
      GridReferenceConverter.UTMReferenceToLatitudeLongitude(ReferenceEllipsoids.WGS_84, utm);
    System.out.println(ll.toString());

    LatitudeLongitude ll2 = new LatitudeLongitude(-60.1167, -111.7833);
    UTMReference utm2 =
      GridReferenceConverter.latitudeLongitudeToUTMReference(ReferenceEllipsoids.WGS_84, ll2);
    System.out.println(utm2.toString());

    LatitudeLongitude ll3 = new LatitudeLongitude(52.65757031, 1.717921583);
    OSGBGridReference osgb =
      GridReferenceConverter.convertLatLongToGridRef(ll3);
    System.out.println(osgb.toString());

    OSGBGridReference osgb2 = new OSGBGridReference(651409.903, 313177.270);
    LatitudeLongitude ll4 =
      GridReferenceConverter.OSGBGridReferenceToLatitudeLongitude(osgb2);
    System.out.println(ll4.toString());

    LatitudeLongitude ll5 = new LatitudeLongitude(52.65757031, 1.717921583);
    UTMReference utm3 =
      GridReferenceConverter.latitudeLongitudeToUTMReference(ReferenceEllipsoids.WGS_84, ll5);
    System.out.println(utm3.toString());
  }
}
