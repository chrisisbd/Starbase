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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;


/***************************************************************************************************
 * ViewingMode.
 */

public enum ViewingMode
    {
    COMMAND_LOG        (0, "CommandLog"),
    MACRO_VIEWER       (1, "MacroViewer"),
    MACRO_EDITOR       (2, "MacroEditor");


    private final int intIndex;
    private final String strName;


    /***********************************************************************************************
     * Construct a ViewingMode.
     *
     * @param index
     * @param name
     */

    private ViewingMode(final int index,
                        final String name)
        {
        intIndex = index;
        strName = name;
        }


    /***********************************************************************************************
     * Get the ViewingMode index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the ViewingMode name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the ViewingMode as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
