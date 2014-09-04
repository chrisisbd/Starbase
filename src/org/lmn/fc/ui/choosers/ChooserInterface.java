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

package org.lmn.fc.ui.choosers;


import org.lmn.fc.common.constants.*;

import java.awt.*;


/***************************************************************************************************
 * ChooserInterface.
 */

public interface ChooserInterface extends FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ResourceKeys
    {
    String BUTTON_SELECT = "  Select  ";
    String BUTTON_CANCEL = "  Cancel  ";
    String BUTTON_RESET  = "  Reset  ";


    /***********************************************************************************************
     * Get the Value returned by the Chooser, or a default if no choice is made.
     *
     * @return String
     */

    String getValue();


    /***********************************************************************************************
     * Set the Value to be returned by the Chooser.
     *
     * @param value
     */

    void setValue(String value);


    /***********************************************************************************************
     * Show the Chooser, centred on the specified Component.
     *
     * @param component
     */

    void showChooser(Component component);
    }
