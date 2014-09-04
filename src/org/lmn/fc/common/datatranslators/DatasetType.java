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

package org.lmn.fc.common.datatranslators;

/***************************************************************************************************
 * DatasetType.
 */

public enum DatasetType
    {
        TABULAR     (0, "(c, r)", "Tabular "),
        XY          (1, "(x, y)", "Indexed "),
        TIMESTAMPED (2, "(t, y)", "Timestamped ");


    private final int    intValue;
    private final String strName;
    private final String strDescription;


    /***********************************************************************************************
     * DatasetType.
     *
     * @param value
     * @param name
     * @param description
     */

    private DatasetType(final int value,
                        final String name,
                        final String description)
        {
        intValue = value;
        strName = name;
        strDescription = description;
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
     * Get the DatasetType name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the DatasetType description.
     *
     * @return String
     */

    public String getDescription()
        {
        return (this.strDescription);
        }


    /***********************************************************************************************
     * Get the DatasetType name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
