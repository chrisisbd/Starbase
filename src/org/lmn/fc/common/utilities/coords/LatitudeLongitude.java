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
// GridReferenceConverter.java
//
// (c) 2003 Jonathan Stott
//
// 0.2 - 02 Mar 2004
//  - Added exceptions to setLongitude() and setLatitude()
// 0.1 - 11 Nov 2003
//  - First version
//-----------------------------------------------------------------------------

package org.lmn.fc.common.utilities.coords;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;

/**
 * An object to represent a latitude and longitude pair
 *
 * @author Jonathan Stott
 * @version 0.2
 * @since 0.1
 */
public class LatitudeLongitude implements FrameworkConstants,
                                          FrameworkStrings
    {
  private double latitude;
  private double longitude;

  /**
   * Construct a latitude and longitude pair
   *
   * @param lat the latitude
   * @param lng the longitude
   */
  public LatitudeLongitude(double lat, double lng) {
    latitude = lat;
    longitude = lng;
  }

  /**
   * Construct a latitude and longitude pair
   *
   * @param degreesN degrees of latitude
   * @param minutesN minutes of latitude
   * @param secondsN seconds of latitude
   * @param degreesE degrees of longitude
   * @param minutesE minutes of longitude
   * @param secondsE seconds of longitude
   */
  public LatitudeLongitude(int degreesN, int minutesN, double secondsN,
                           int degreesE, int minutesE, double secondsE) {
    latitude = degreesN+((minutesN+(secondsN/60.0))/60.0);
    longitude = degreesE+((minutesE+(secondsE/60.0))/60.0);
  }


  /**
   * Get the latitude
   *
   * @return the latitude
   */
  public double getLatitude() {
    return latitude;
  }


  /**
   * Get the longitude
   *
   * @return the longitude
   */
  public double getLongitude() {
    return longitude;
  }


  /**
   * Set the latitude
   *
   * @param d the new value of the latitude
   */
  public void setLatitude(double d) {
    if (d > 90 || d < -90) {
      throw new IllegalArgumentException("Latitude must be between -90 and " +
                                         "90");
    }

    latitude = d;
  }


  /**
   * Set the longitude
   *
   * @param d the new value of the longitude
   */
  public void setLongitude(double d) {
    if (d > 180 || d < -180) {
      throw new IllegalArgumentException("Longitude must be between -180 and " +
                                         "180");
    }

    longitude = d;
  }


  /**
   * Get a string representation of the latitude and longitude in the form
   * 52�39'27.2531"N 1�43'4.5177"E
   *
   * @return
   */
  public String toString() {
    String lat = "";
    int latDeg = (int)Math.floor(Math.abs(getLatitude()));
    int latMin = (int)Math.floor((Math.abs(getLatitude()) - latDeg) * 60);
    double latSec = (((Math.abs(getLatitude()) - latDeg) * 60) - latMin) * 60;
    lat = latDeg + "�" + latMin + "'" + latSec + "\"";
    if (getLatitude() < 0) {
      lat = lat + "S";
    } else {
      lat = lat + "N";
    }

    String lng = "";
    int lngDeg = (int)Math.floor(Math.abs(getLongitude()));
    int lngMin = (int)Math.floor((Math.abs(getLongitude()) - lngDeg) * 60);
    double lngSec = (((Math.abs(getLongitude()) - lngDeg) * 60) - lngMin) * 60;
    lng = lngDeg + "�" + lngMin + "'" + lngSec + "\"";
    if (getLongitude() < 0) {
      lng = lng + "W";
    } else {
      lng = lng + "E";
    }

    return lat + SPACE + lng;
  }

}
