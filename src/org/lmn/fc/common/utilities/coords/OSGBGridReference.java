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
// OSGBGridReference.java
//
// (c) 2003 Jonathan Stott
//
// 0.2 - 02 Mar 2004
//  - Renamed from EastingNorthing to OSGBGridReference
// 0.1 - 11 Nov 2003
//  - First version
//-----------------------------------------------------------------------------

package org.lmn.fc.common.utilities.coords;

/**
 * An object to represent an OSGB easting and northing pair
 *
 * @author Jonathan Stott
 * @version 0.2
 * @since 0.1
 */
public class OSGBGridReference {
  private double easting;
  private double northing;


  /**
   * Construct an easting and northing pair
   *
   * @param easting the easting in metres
   * @param northing the northing in metres
   */
  public OSGBGridReference(double easting, double northing) {
    this.easting = easting;
    this.northing = northing;
  }


  /**
   * Get the easting
   *
   * @return the easting
   */
  public double getEasting() {
    return easting;
  }


  /**
   * Get the northing
   *
   * @return the northing
   */
  public double getNorthing() {
    return northing;
  }


  /**
   * Set the easting
   *
   * @param i the new value of the easting
   */
  public void setEasting(double i) {
    easting = i;
  }


  /**
   * Set the northing
   *
   * @param i the new value of the northing
   */
  public void setNorthing(double i) {
    northing = i;
  }


  /**
   * Return a string representation of the easting and northing in the form
   * (easting,northing)
   */
  public String toString() {
    return "(" + getEasting() + "," + getNorthing() + ")";
  }
}
