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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.maps;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;

import java.awt.*;


/***************************************************************************************************
 * ExportableStarMap.
 */

public class ExportableStarMap implements ExportableComponentInterface
    {
    /***********************************************************************************************
     * Get the ExportableComponent width.
     *
     * @return int
     */

    public int getWidth()
        {
        return (0);
        }


    /***********************************************************************************************
     * Get the ExportableComponent height.
     *
     * @return int
     */

    public int getHeight()
        {
        return (0);
        }


    /***********************************************************************************************
     * Repaint the ExportableComponent ready for the export.
     *
     * @param width
     * @param height
     */

    public void paintForExport(final Graphics2D graphics,
                               final int width,
                               final int height)
        {
        // This may be overridden in the exporting component if this is more convenient
        }
    }
