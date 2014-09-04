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

package org.lmn.fc.common.utilities.astronomy;


public class SphericalCoordinates
    {
    double dbLongitude;
    double dblLatitude;
    double dblRadius;

    public SphericalCoordinates(final double longitude,
                                final double latitude,
                                final double radius)
        {
        this.dbLongitude = longitude;
        this.dblLatitude = latitude;
        this.dblRadius = radius;
        }

    public double getLongitude()
        {
        return (this.dbLongitude);
        }
    public void setLongitude(final double longitude)
        {
        this.dbLongitude = longitude;
        }
    public double getLatitude()
        {
        return (this.dblLatitude);
        }
    public void setLatitude(final double latitude)
        {
        this.dblLatitude = latitude;
        }
    public double getRadius()
        {
        return (this.dblRadius);
        }
    public void setRadius(final double radius)
        {
        this.dblRadius = radius;
        }
    }
