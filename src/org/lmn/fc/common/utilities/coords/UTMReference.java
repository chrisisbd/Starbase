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
// UTMReference.java
//
// (c) 2004 Jonathan Stott
//
// Created on 02-Mar-2004
//
// 0.2 - 02 Mar 2004
//  - Initial version
//-----------------------------------------------------------------------------

package org.lmn.fc.common.utilities.coords;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;

/**
 * An object to represent an UTM reference
 *
 * @author Jonathan Stott
 * @version 0.2
 * @since 0.2
 */
public class UTMReference implements FrameworkConstants,
                                     FrameworkStrings
    {
  private double easting = 0.0;
  private double northing = 0.0;
  private char latitudeZone = 'Z';
  private int longitudeZone = 0;

  /**
   * Create a new UTMReference object with an easting, northing and zones
   *
   * @param inEasting
   * @param inNorthing
   * @param inLatitudeZone
   * @param inLongitudeZone
   */
  public UTMReference(double inEasting,
                      double inNorthing,
                      char inLatitudeZone,
                      int inLongitudeZone) {
    setEasting(inEasting);
    setNorthing(inNorthing);
    setLatitudeZone(inLatitudeZone);
    setLongitudeZone(inLongitudeZone);
  }


  /**
   * Return a String representation of the UTMReference object
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getLongitudeZone() + Character.toString(getLatitudeZone()) +
           SPACE + getEasting() + SPACE + getNorthing();
  }


  /**
   * Get the easting component of the reference
   *
   * @return
   */
  public double getEasting() {
    return easting;
  }


  /**
   * Get the northing component of the reference
   *
   * @return
   */
  public double getNorthing() {
    return northing;
  }


  /**
   * Get the latitude zone of the reference
   *
   * @return
   */
  public char getLatitudeZone() {
    return latitudeZone;
  }


  /**
   * Get the longitude zone of the reference
   *
   * @return
   */
  public int getLongitudeZone() {
    return longitudeZone;
  }


  /**
   * Set the easting component of the reference
   *
   * @param d
   */
  public void setEasting(double d) {
    easting = d;
  }


  /**
   * Set the northing component of the reference
   *
   * @param d
   */
  public void setNorthing(double d) {
    northing = d;
  }


  /**
   * Set the latitude zone of the reference
   *
   * @param inZone
   */
  public void setLatitudeZone(char inZone) {
    if (inZone < 'C' || inZone > 'X') {
      throw new IllegalArgumentException("Latitude zone must be from 'C' " +
                                         "through 'X'");
    }

    latitudeZone = inZone;
  }


  /**
   * Set the longitude zone of the reference
   *
   * @param inZone
   */
  public void setLongitudeZone(int inZone) {
    if (inZone < 1 || inZone > 60) {
      throw new IllegalArgumentException("Longitude zone must be from 1 " +
                                         "through 60");
    }

    longitudeZone = inZone;
  }
}
