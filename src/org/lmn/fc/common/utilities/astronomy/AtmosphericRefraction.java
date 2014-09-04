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

package org.lmn.fc.common.utilities.astronomy;


import org.lmn.fc.common.utilities.maths.AstroMath;


/***************************************************************************************************
 * AtmosphericRefraction.
 */

public final class AtmosphericRefraction
    {
    /***********************************************************************************************
     * Calculate the effect of atmospheric refraction on the specified true Elevation.
     * This is for mean conditions (P=1010 hPa, T=10�C, yellow light),
     * and is known as Saemundsson's formula.
     * See: Jean Meeus Astronomical Algorithms First Edition pg. 102 Eq. 15.4
     * See eg.: http://www.jgiesen.de/refract/index.html
     * Return the Refraction in DEGREES.
     *
     * @param trueelevationdegrees
     *
     * @return double
     */

    public static double atmosphericRefraction(final double trueelevationdegrees)
        {
        final double dblRMinutes;

        dblRMinutes = 1.02 / AstroMath.tand(trueelevationdegrees + (10.3 / (trueelevationdegrees + 5.11)));

        // Correct the result so that R = 0 for Elevation = 90 deg.
        return (dblRMinutes + 0.0019279) / 60;
        }
    }
