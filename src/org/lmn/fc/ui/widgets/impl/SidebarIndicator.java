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

package org.lmn.fc.ui.widgets.impl;

import java.awt.*;


/***************************************************************************************************
 * SidebarIndicator.
 */

public class SidebarIndicator extends Indicator
    {
    public static final Dimension DIM_SIDEBAR_INDICATOR = new Dimension(90, 20);


    /***********************************************************************************************
     * Construct a SidebarIndicator width 70 pixels, height 20 pixels.
     *
     * @param value
     * @param tooltip
     */

    public SidebarIndicator(final String value,
                            final String tooltip)
        {
        super(DIM_SIDEBAR_INDICATOR,
              value,
              "",
              tooltip,
              INDICATOR_BORDER);

        // Use the value contents to format the output
        setValueFormat(value);
        }


    /***********************************************************************************************
     * Construct a SidebarIndicator of the specified Dimension.
     *
     * @param dimension
     * @param value
     * @param tooltip
     */

    public SidebarIndicator(final Dimension dimension,
                            final String value,
                            final String tooltip)
        {
        super(dimension,
              value,
              "",
              tooltip,
              INDICATOR_BORDER);

        // Use the value contents to format the output
        setValueFormat(value);
        }
    }
