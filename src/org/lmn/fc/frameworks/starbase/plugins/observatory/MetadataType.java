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
 * MetadataType.
 */

public enum MetadataType
    {
    FRAMEWORK          (0, "Framework",         false),
    OBSERVATORY        (1, "Observatory",       false),
    OBSERVER           (2, "Observer",          false),
    OBSERVATION        (3, "Observation",       false),
    INSTRUMENT         (4, "Instrument",        false),
    CONTROLLER         (5, "Controller",        false),
    PLUGIN             (6, "Plugin",            true),
    METADATA           (7, "MetadataMetadata",  false);


    private final int intIndex;
    private final String strName;
    private final boolean boolIsExpandable;


    /***********************************************************************************************
     * Get the MetadataType enum corresponding to the specified MetadataType name.
     * Return NULL if the MetadataType name is not found.
     *
     * @param name
     *
     * @return MetadataType
     */

    public static MetadataType getMetadataTypeForName(final String name)
        {
        MetadataType metadataType;

        metadataType = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final MetadataType[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final MetadataType type;

                type = types[i];

                if (name.equals(type.getName()))
                    {
                    metadataType = type;
                    boolFoundIt = true;
                    }
                }
            }

        return (metadataType);
        }


    /***********************************************************************************************
     * Construct a MetadataType.
     *
     * @param index
     * @param name
     * @param expandable
     */

    private MetadataType(final int index,
                         final String name,
                         final boolean expandable)
        {
        intIndex = index;
        strName = name;
        boolIsExpandable = expandable;
        }


    /***********************************************************************************************
     * Get the MetadataType index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the MetadataType name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Indicate if this MetadataType name is expandable with e.g. a Plugin identifier.
     *
     * @return boolean
     */

    public boolean isExpandable()
        {
        return (this.boolIsExpandable);
        }


    /***********************************************************************************************
     * Get the MetadataType as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }