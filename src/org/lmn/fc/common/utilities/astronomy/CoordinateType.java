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


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * CoordinateType.
 */

public enum CoordinateType
    {
    UNASSIGNED          (0, "?",        "?",    "?",    "(?, ?)"),
    AZEL                (1, "AzEl",     "Az",   "El",   "(Az, El)"),
    AZEL_METADATA       (4, "AzEl",     "Az",   "El",   "(Az, El)"),
    RADEC               (2, "RaDec",    "RA",   "Dec",  "(RA, Dec)"),
    RADEC_METADATA      (5, "RaDec",    "RA",   "Dec",  "(RA, Dec)"),
    GALACTIC            (3, "Galactic", "l",    "b",    "(l, b)"),
    GALACTIC_METADATA   (6, "Galactic", "l",    "b",    "(l, b)");

    // Injections
    private final int intIndex;
    private final String strName;
    private final String strSymbolX;
    private final String strSymbolY;
    private final String strSymbolPair;


    /***********************************************************************************************
     * Get the CoordinateType for the specified Name.
     * Return NULL if the name is not found.
     *
     * @param name
     *
     * @return CoordinateType
     */

    public static CoordinateType getCoordinateTypeForName(final String name)
        {
        CoordinateType coordinateType;

        coordinateType = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final CoordinateType[] types;
            boolean boolFoundIt;

            types = CoordinateType.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final CoordinateType type;

                type = types[i];

                if (name.equals(type.getName()))
                    {
                    coordinateType = type;
                    boolFoundIt = true;
                    }
                }
            }

        return (coordinateType);
        }


    /***********************************************************************************************
     * Construct a CoordinateType.
     *
     * @param index
     * @param name
     * @param symbolx
     * @param symboly
     * @param symbolpair
     */

    private CoordinateType(final int index,
                           final String name,
                           final String symbolx,
                           final String symboly,
                           final String symbolpair)
        {
        this.intIndex = index;
        this.strName = name;
        this.strSymbolX = symbolx;
        this.strSymbolY = symboly;
        this.strSymbolPair = symbolpair;
        }


    /***********************************************************************************************
     * Get the CoordinateType index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the CoordinateType name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the CoordinateType symbol X.
     * Az, Ra, l and so on.
     *
     * @return String
     */

    public String getSymbolX()
        {
        return(this.strSymbolX);
        }


    /***********************************************************************************************
     * Get the CoordinateType symbol Y.
     * El, Dec, b and so on.
     *
     * @return String
     */

    public String getSymbolY()
        {
        return(this.strSymbolY);
        }


    /***********************************************************************************************
     * Get the CoordinateType symbol Pair.
     * (Az, El) (Ra, Dec) (l, b) and so on.
     *
     * @return String
     */

    public String getSymbolPair()
        {
        return(this.strSymbolPair);
        }


    /***********************************************************************************************
     * Get the CoordinateType as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }