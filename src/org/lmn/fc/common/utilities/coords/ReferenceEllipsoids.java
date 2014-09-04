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
// ReferenceEllipsoids.java
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
 * A collection of reference ellipsoids
 *
 * @author Jonathan Stott
 * @version 0.2
 * @since 0.2
 */
public class ReferenceEllipsoids {
  /**
   * The Airy 1830 ellipsoid. Used for the Ordnance Survey grid projection.
   */
  public static final ReferenceEllipsoid AIRY_1830 =
    new ReferenceEllipsoid("Airy 1830", 6377563.396, 6356256.909);

  /**
   * The Airy 1830 modified ellipsoid. Used for the Irish National Grid
   * projection.
   */
  public static final ReferenceEllipsoid AIRY_1830_MODIFIED =
    new ReferenceEllipsoid("Airy 1830 Modified", 6377340.189, 6356034.448);

  /**
   * The International 1924 ellipsoid. Also known as the Hayford 1909 ellipsoid.
   */
  public static final ReferenceEllipsoid INTERNATIONAL_1924 =
    new ReferenceEllipsoid("International 1924", 6378388, 6356911.946);

  /**
   * The WGS 84 ellipsoid.
   */
  public static final ReferenceEllipsoid WGS_84 =
    new ReferenceEllipsoid("WGS 84", 6378137, 6356752.314);
}
