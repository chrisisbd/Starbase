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
// ReferenceEllipsoid.java
//
// (c) 2004 Jonathan Stott
//
// Created on 02-Mar-2004
//
// 0.2 - 02 Mar 2004
//  - Initial version
//-----------------------------------------------------------------------------

package org.lmn.fc.common.utilities.coords;

/**
 * A class to represent a reference ellipsoid
 *
 * @author Jonathan Stott
 * @version 0.2
 * @since 0.2
 */
public class ReferenceEllipsoid {
  private String name = "";
  private double semiMajorAxis = 0.0;
  private double semiMinorAxis = 0.0;
  private double eccentricitySquared = 0.0;


  /**
   * Create a new reference ellipsoid from the semi-major (equatorial radius)
   * and semi-minor axes. The semi-minor axis of the ellipsoid can be calculated
   * from the eccentricity squared (e<sup>2</sup>) and the semi-major axis
   * (a<sup>2</sup>) by finding sqrt(-e<sup>2</sup>a<sup>2</sup> + a<sup>2</sup>)
   *
   * @param inName the name of the reference ellipsoid
   * @param inSemiMajorAxis
   * @param inSemiMinorAxis
   */
  public ReferenceEllipsoid(String inName,
                            double inSemiMajorAxis,
                            double inSemiMinorAxis) {
    name = inName;
    semiMajorAxis = inSemiMajorAxis;
    semiMinorAxis = inSemiMinorAxis;
    eccentricitySquared =
      ((semiMajorAxis * semiMajorAxis) - (semiMinorAxis * semiMinorAxis))
        / (semiMajorAxis * semiMajorAxis);
  }


  /**
   * Get the semi-minor axis of the reference ellipsoid
   *
   * @return
   */
  public double getSemiMinorAxis() {
    return semiMinorAxis;
  }


  /**
   * Get the eccentricity squared of the reference ellipsoid
   *
   * @return
   */
  public double getEccentricitySquared() {
    return eccentricitySquared;
  }


  /**
   * Get the semi-major axis (equatorial radius) of the reference ellipsoid
   *
   * @return
   */
  public double getSemiMajorAxis() {
    return semiMajorAxis;
  }


  /**
   * Get the equatorial radius (semi-major axis) of the reference ellipsoid
   *
   * @return
   */
  public double getEquatorialRadius() {
    return semiMajorAxis;
  }


  /**
   * Get the name of the reference ellipsoid
   *
   * @return
   */
  public String getName() {
    return name;
  }
}
