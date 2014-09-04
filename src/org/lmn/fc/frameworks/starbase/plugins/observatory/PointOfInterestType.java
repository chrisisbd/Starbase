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

package org.lmn.fc.frameworks.starbase.plugins.observatory;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * PointOfInterestType.
 * This uses the same categories as MetadataType, which is probably over the top.
 */

public enum PointOfInterestType
    {
    FRAMEWORK          (0, "Framework"),
    OBSERVATORY        (1, "Observatory"),
    OBSERVER           (2, "Observer"),
    OBSERVATION        (3, "Observation"),
    INSTRUMENT         (4, "Instrument"),
    CONTROLLER         (5, "Controller"),
    PLUGIN             (6, "Plugin");


    private final int intIndex;
    private final String strName;


    /***********************************************************************************************
     * Get the PointOfInterestType enum corresponding to the specified PointOfInterestType name.
     * Return NULL if the PointOfInterestType name is not found.
     *
     * @param name
     *
     * @return PointOfInterestType
     */

    public static PointOfInterestType getPointOfInterestTypeForName(final String name)
        {
        final String SOURCE = "PointOfInterestType.getPointOfInterestTypeForName() ";
        PointOfInterestType poiType;

        poiType = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final PointOfInterestType[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final PointOfInterestType type;

                type = types[i];

                if (name.equals(type.getName()))
                    {
                    poiType = type;
                    boolFoundIt = true;
                    }
                }
            }

        return (poiType);
        }


    /***********************************************************************************************
     * Construct a PointOfInterestType.
     *
     * @param index
     * @param name
     */

    private PointOfInterestType(final int index,
                                final String name)
        {
        intIndex = index;
        strName = name;
        }


    /***********************************************************************************************
     * Get the PointOfInterestType index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the PointOfInterestType name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the PointOfInterestType as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }