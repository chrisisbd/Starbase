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

import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedListener;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import java.awt.*;
import java.util.Vector;


/***************************************************************************************************
 * MetadataLeafUIComponentInterface.
 */

public interface MetadataLeafUIComponentInterface extends UIComponentPlugin,
                                                          ObservatoryMetadataChangedListener,
                                                          ObserverMetadataChangedListener
    {
    // String Resources
    String MSG_NODATA = "NODATA";
    String MSG_CHOOSE_VALUE = "Choose the metadata value";
    String METADATA_EDITOR_HELP = "MetadataEditorHelp.html";
    String BUTTON_CHOOSER = "Chooser";

    String RESOURCE_KEY = "Editor.Metadata.";
    String KEY_LABEL_KEY = "Label.Key";
    String KEY_LABEL_EDITABLE = "Label.Editable";
    String KEY_LABEL_VALUE = "Label.Value";
    String KEY_LABEL_REGEX = "Label.Regex";
    String KEY_LABEL_DATA_TYPE = "Label.DataType";
    String KEY_LABEL_UNITS = "Label.Units";
    String KEY_LABEL_DESCRIPTION = "Label.Description";
    String KEY_TOOLTIP_KEY = "Tooltip.Key";
    String KEY_TOOLTIP_EDITABLE = "Tooltip.Editable";
    String KEY_TOOLTIP_VALUE = "Tooltip.Value";
    String KEY_TOOLTIP_REGEX = "Tooltip.Regex";
    String KEY_TOOLTIP_DATA_TYPE = "Tooltip.DataType";
    String KEY_TOOLTIP_UNITS = "Tooltip.Units";
    String KEY_TOOLTIP_DESCRIPTION = "Tooltip.Description";
    String KEY_LABEL_COMMIT = "Label.Commit";
    String KEY_TOOLTIP_COMMIT = "Tooltip.Commit";
    String KEY_LABEL_REVERT = "Label.Revert";
    String KEY_TOOLTIP_REVERT = "Tooltip.Revert";
    String KEY_TITLE = "Title";
    String KEY_WARNING_REGEX = "Warning.Regex";
    String KEY_WARNING_DATATYPE = "Warning.DataType";
    String KEY_WARNING_UNITS = "Warning.Units";
    String KEY_WARNING_TRUNCATED_DESCRIPTION = "Warning.TruncatedDescription";
    String KEY_INVALID_VALUE = "Invalid.Value";

    // The number of standard height rows (i.e. not including Regex or the Description)
    int ROW_COUNT = 4;
    boolean ENABLED = true;
    int LENGTH_DESCRIPTION = 100;
    int HEIGHT_BUTTON = 27;
    int WIDTH_BUTTON = 170;
    int WIDTH_CHOOSER_BUTTON = 60;
    int HEIGHT_REGEX = 50;
    Dimension DIM_REGEX_SPACER = new Dimension(1, HEIGHT_REGEX - EditorUtilities.HEIGHT_ROW + (int) EditorUIComponent.DIM_ROW_SPACER.getHeight());
    int LENGTH_DATATYPE_VALUE = 1000;

    int DIVIDER_SIZE = 15;
    double DIVIDER_LOCATION = 0.90;
    double RESIZE_WEIGHT = 0.90;


    /***********************************************************************************************
     * Set the Metadata for this Editor.
     * Save a reference to the original Metadata for any reverts, edit only a copy.
     *
     * @param metadataitem
     */

    void setEditedMetadata(Metadata metadataitem);


    /***********************************************************************************************
     * Notify all listeners of MetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param metadatavalue
     * @param state
     */

    void notifyMetadataChangedEvent(Object eventsource,
                                    String metadatakey,
                                    final String metadatavalue,
                                    MetadataItemState state);


    /***********************************************************************************************
     * Get the MetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<MetadataChangedListener>
     */

    Vector<MetadataChangedListener> getMetadataChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event, uniquely.
     *
     * @param listener
     */

    void addMetadataChangedListener(MetadataChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeMetadataChangedListener(MetadataChangedListener listener);
    }
