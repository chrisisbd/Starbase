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

package org.lmn.fc.frameworks.starbase.portcontroller.impl.streams;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * StreamType.
 */

public enum StreamType
    {
    VIRTUAL     (0, "Virtual"  ),
    STARIBUS    (1, "Staribus" ),
    SERIAL      (2, "Serial"   ),
    STARINET    (3, "Starinet" ),
    ETHERNET    (4, "Ethernet" );


    private final int intIndex;
    private final String strName;


    /***********************************************************************************************
     * Get the StreamType enum corresponding to the specified StreamType name.
     * Return NULL if the StreamType name is not found.
     *
     * @param name
     *
     * @return StreamType
     */

    public static StreamType getStreamTypeForName(final String name)
        {
        StreamType streamType;

        streamType = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final StreamType[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final StreamType type;

                type = types[i];

                if (name.equals(type.getName()))
                    {
                    streamType = type;
                    boolFoundIt = true;
                    }
                }
            }

        return (streamType);
        }


    /***********************************************************************************************
     * Construct a StreamType.
     *
     * @param index
     * @param name
     */

    private StreamType(final int index,
                       final String name)
        {
        intIndex = index;
        strName = name;
        }


    /***********************************************************************************************
     * Get the StreamType index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the StreamType name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the StreamType as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }