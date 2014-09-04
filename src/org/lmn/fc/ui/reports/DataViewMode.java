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

package org.lmn.fc.ui.reports;


/***************************************************************************************************
 * DataViewMode.
 */

public enum DataViewMode
    {
    SHOW_ALL    (0, false, "Show All",   "Show all lines of data - WARNING! This may use a lot of memory"),
    SHOW_FIRST  (1, false, "Show First", "Show only the first few lines of data, to save memory"),
    SHOW_LAST   (2, true,  "Show Last",  "Show only the last few lines of data, to save memory");


    private final int intIndex;
    private final String strName;
    private final String strTooltip;
    private final boolean boolSelectedByDefault;


    /***********************************************************************************************
     * Construct a DataViewMode.
     *
     * @param index
     * @param selected
     * @param name
     * @param tooltip
     */

    private DataViewMode(final int index,
                         final boolean selected,
                         final String name,
                         final String tooltip)
        {
        this.intIndex = index;
        this.boolSelectedByDefault = selected;
        this.strName = name;
        this.strTooltip = tooltip;
        }


    /***********************************************************************************************
     * Get the index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Indicate if this view is the default selection.
     *
     * @return boolean
     */

    public boolean isDefaultSelection()
        {
        return (this.boolSelectedByDefault);
        }


    /***********************************************************************************************
     * Get the name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the tooltip.
     *
     * @return String
     */

    public String getTooltip()
        {
        return(this.strTooltip);
        }


    /***********************************************************************************************
     * Get the DataViewMode as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
