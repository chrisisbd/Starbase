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
 * MetadataItemState.
 */

public enum MetadataItemState
    {
    CREATE          (0, "Create"),
    ADD             (1, "Add"),
    EDIT            (2, "Edit"),
    DELETE          (3, "Delete"),
    REMOVE          (4, "Remove"),
    LOAD            (5, "Load"),
    UNLOAD          (6, "Unload");


    private final int intIndex;
    private final String strName;


    /***********************************************************************************************
     * Get the MetadataItemState enum corresponding to the specified MetadataItemState name.
     * Return NULL if the MetadataItemState name is not found.
     *
     * @param name
     *
     * @return MetadataType
     */

    public static MetadataItemState getMetadataItemStateForName(final String name)
        {
        MetadataItemState metadataState;

        metadataState = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final MetadataItemState[] states;
            boolean boolFoundIt;

            states = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < states.length);
                 i++)
                {
                final MetadataItemState state;

                state = states[i];

                if (name.equals(state.getName()))
                    {
                    metadataState = state;
                    boolFoundIt = true;
                    }
                }
            }

        return (metadataState);
        }


    /***********************************************************************************************
     * Construct a MetadataItemState.
     *
     * @param index
     * @param name
     */

    private MetadataItemState(final int index,
                              final String name)
        {
        intIndex = index;
        strName = name;
        }


    /***********************************************************************************************
     * Get the MetadataItemState index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the MetadataItemState name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the MetadataItemState as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }