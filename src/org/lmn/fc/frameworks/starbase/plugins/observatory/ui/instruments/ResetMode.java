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


/***************************************************************************************************
 * ResetMode.
 */

public enum ResetMode
    {
    DEFAULTS     (0, "Defaults"),
    SOFT         (1, "Soft"),
    STARIBUS     (2, "Staribus");


    private final int intMode;
    private final String strName;


    /***********************************************************************************************
     * ResetMode.
     *
     * @param mode
     * @param name
     */

    private ResetMode(final int mode,
                      final String name)
        {
        intMode = mode;
        strName = name;
        }


    /***********************************************************************************************
     * Get the ResetMode.
     *
     * @return int
     */

    public int getResetMode()
        {
        return (this.intMode);
        }


    /***********************************************************************************************
     * Get the ResetMode name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the ResetMode name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
