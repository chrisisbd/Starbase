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

import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedListener;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import java.util.List;


/***************************************************************************************************
 * MetadataExplorerFrameUIComponentInterface.
 */

public interface MetadataExplorerFrameUIComponentInterface extends UIComponentPlugin,
                                                                   ObservatoryMetadataChangedListener,
                                                                   ObserverMetadataChangedListener
    {
    // String Resources
    String METADATA_EDITOR_NAME = "Metadata Editor";
    String TOOLTIP_METADATA_IMPORT = "Import Metadata";
    String TOOLTIP_METADATA_REMOVE = "Remove Metadata";


    /***********************************************************************************************
     * Set the List of Metadata for this UIComponent.
     *
     * @param metadata
     */

    void setMetadataList(List<Metadata> metadata);


    /***********************************************************************************************
     * Get the MetadataExplorerUI.
     *
     * @return MetadataExplorerUIComponentInterface
     */

    MetadataExplorerUIComponentInterface getMetadataExplorerUI();
    }