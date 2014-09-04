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
// Astronomy AstronomyConstants
//------------------------------------------------------------------------------
// Revision History
//
//  20-03-02    LMN created file from Java Applications book
//
//------------------------------------------------------------------------------
// Astronomy package

package org.lmn.fc.common.utilities.maths;

import org.lmn.fc.common.constants.AstronomyConstants;

//------------------------------------------------------------------------------
// Imports


//------------------------------------------------------------------------------

public class AstroMath
    {
    //--------------------------------------------------------------------------
    // Truncation and fraction extraction
    //--------------------------------------------------------------------------
    // Truncate a number by removing the fractional part

    public static double truncate(double x)
        {
        if (x < 0.0)
            {
            // Returns the smallest (closest to negative infinity) double value
            // that is not less than the argument and is equal to a mathematical integer.

            return(Math.ceil(x));
            }
        else
            {
            // Returns the largest (closest to positive infinity) double value
            // that is not greater than the argument and is equal to a mathematical integer.

            return(Math.floor(x));
            }
        }

    //--------------------------------------------------------------------------
    // Return the fractional part of a number

    public static double fraction(double x)
        {
        return(x - truncate(x));
        }

    //--------------------------------------------------------------------------
    // Trigonometry
    //--------------------------------------------------------------------------
    // Hyperbolics

    public static double asinh(double x)
        {
        return Math.log(x + Math.sqrt(x * x + 1.0));
        }

    public static double acosh(double x)
        {
        return Math.log(x + Math.sqrt(x * x - 1.0));
        }

    public static double atanh(double x)
        {
        return Math.log((1.0 + x) / (1.0 - x)) / 2.0;
        }


    //--------------------------------------------------------------------------
    // Degrees as arguments

    public static double sind(double x)
        {
        return Math.sin(Math.toRadians(x));
        }

    public static double cosd(double x)
        {
        return Math.cos(Math.toRadians(x));
        }

    public static double tand(double x)
        {
        return Math.tan(Math.toRadians(x));
        }

    public static double asind(double x)
        {
        return Math.asin(x) * AstronomyConstants.DEGREES_PER_RADIAN;
        }

    public static double acosd(double x)
        {
        return Math.acos(x) * AstronomyConstants.DEGREES_PER_RADIAN;
        }

    public static double atand(double x)
        {
        return Math.atan(x) * AstronomyConstants.DEGREES_PER_RADIAN;
        }

    public static double atan2d(double a, double b)
        {
        return Math.atan2(a,b) * AstronomyConstants.DEGREES_PER_RADIAN;
        }
    }


//------------------------------------------------------------------------------
// End of File

