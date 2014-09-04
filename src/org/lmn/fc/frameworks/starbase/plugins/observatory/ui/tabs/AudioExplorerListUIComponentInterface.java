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

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;


/***************************************************************************************************
 * AudioExplorerListUIComponentInterface.
 */

public interface AudioExplorerListUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String REPORT_NAME   = "Metadata";
    String REPORT_HEADER = "Metadata Report created at";
    String TITLE_COLUMN  = "Data";


    /***********************************************************************************************
     * Set the Array of Data for this UIComponent.
     *
     * @param arraydata
     */

    void setData(Object[][] arraydata);


    /***********************************************************************************************
     * Set the Column Names for this UIComponent.
     *
     * @param arraynames
     */

    void setColumnNames(String[] arraynames);


    /***********************************************************************************************
     * Refresh the table data.
     */

    void refreshTable();


    /**********************************************************************************************
     * Get the Font used on the Report.
     *
     * @return FontPlugin
     */

    FontInterface getReportFont();


    /***********************************************************************************************
     * Get the colour used for the text of the Report.
     *
     * @return ColourPlugin
     */

    ColourInterface getTextColour();
    }
