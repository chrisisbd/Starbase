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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * UpdateSource.
 */

public enum UpdateSource
    {
    CENTROID    (0, "CentroidOfFixes",  "Centroid of Fixes"),
    SINGLE      (1, "SingleFix",        "Single Fix");


    private final int intValue;
    private final String strName;
    private final String strSource;


    /***********************************************************************************************
     * Get the UpdateSource enum corresponding to the specified UpdateSource name.
     * Return NULL if the UpdateSource name is not found.
     *
     * @param name
     *
     * @return UpdateSource
     */

    public static UpdateSource getUpdateSourceForName(final String name)
        {
        UpdateSource source;

        source = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final UpdateSource[] sources;
            boolean boolFoundIt;

            sources = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < sources.length);
                 i++)
                {
                final UpdateSource updateSource;

                updateSource = sources[i];

                if (name.equals(updateSource.getName()))
                    {
                    source = updateSource;
                    boolFoundIt = true;
                    }
                }
            }

        return (source);
        }


    /***********************************************************************************************
     * UpdateSource.
     *
     * @param value
     * @param name
     * @param source
     */

    private UpdateSource(final int value,
                         final String name,
                         final String source)
        {
        intValue = value;
        strName = name;
        strSource = source;
        }


    /***********************************************************************************************
     * Get the TypeID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intValue);
        }


    /***********************************************************************************************
     * Get the Source name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Source text.
     *
     * @return String
     */

    public String getSource()
        {
        return (this.strSource);
        }


    /***********************************************************************************************
     * Get the Source name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
