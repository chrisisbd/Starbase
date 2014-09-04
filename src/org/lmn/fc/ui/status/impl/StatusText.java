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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  16-11-04    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.status.impl;

//--------------------------------------------------------------------------------------------------

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;


/***************************************************************************************************
 * The StatusIndicatorKey status text.
 */

public final class StatusText extends StatusIndicator
    {
    private final FrameworkPlugin pluginFramework;


    /***********************************************************************************************
     * Construct a StatusText.
     *
     * @param framework
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusText(final FrameworkPlugin framework,
                      final ColourInterface colour,
                      final FontInterface font,
                      final String text,
                      final String tooltip)
        {
        super(colour, font, text, tooltip);

        this.pluginFramework = framework;

        setMinimumSize(DIM_STATUS_MIN);
        setPreferredSize(DIM_STATUS_PREF);
        setMaximumSize(DIM_STATUS_MAX);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
