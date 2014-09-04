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

package org.lmn.fc.model.datatypes;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.model.datatypes.types.FontDataType;

import java.awt.*;


/***************************************************************************************************
 * FontInterface.
 */

public interface FontInterface extends RootDataTypeInterface,
                                       FrameworkConstants,
                                       FrameworkStrings,
                                       FrameworkSingletons,
                                       FrameworkMetadata,
                                       ResourceKeys
    {
    FontInterface DEFAULT_FONT_LABEL = new FontDataType("font=dialog style=plain size=12");
    FontInterface DEFAULT_FONT_INTERFACE = new FontDataType("font=dialog style=plain size=12");
    FontInterface DEFAULT_FONT_BANNER = new FontDataType("font=dialog style=plain size=28");


    /***********************************************************************************************
     * Derive a larger Font.
     *
     * @param increase
     *
     * @return Font
     */

    Font deriveLargerFont(int increase);


    /***********************************************************************************************
     * Get the Font associated with this FontDataType.
     *
     * @return Font
     */

    Font getFont();


    /***********************************************************************************************
     * Get the Name of the Font.
     * The name may contain spaces.
     *
     * @return String
     */

    String getFontName();


    /***********************************************************************************************
     * Get the Style of the Font.
     * Style may be one of {Plain, Bold, Italic, BoldItalic}.
     *
     * @return String
     */

    String getFontStyle();


    /***********************************************************************************************
     * Get the Size of the Font.
     * The Size is in the range {1...n}.
     *
     * @return int
     */

    int getFontSize();
    }
