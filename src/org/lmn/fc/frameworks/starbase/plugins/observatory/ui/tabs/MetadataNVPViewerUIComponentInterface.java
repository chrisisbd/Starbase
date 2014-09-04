// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.util.List;


/***************************************************************************************************
 * MetadataNVPViewerUIComponentInterface.
 */

public interface MetadataNVPViewerUIComponentInterface extends ReportTablePlugin
    {
    // String Resources
    String REPORT_NAME = "Metadata";
    String REPORT_HEADER = "Metadata Report created at";
    String TITLE_NAME = "Name";
    String TITLE_VALUE = "Value";


    /***********************************************************************************************
     * Set the List of Metadata for this UIComponent.
     *
     * @param metadata
     */

    void setMetadataList(List<Metadata> metadata);
    }
