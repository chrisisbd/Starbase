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

import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetDomainChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.multipleslider.MThumbSlider;

import javax.swing.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DatasetDomainUIComponentInterface.
 */

public interface DatasetDomainUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String TOOLTIP_SLIDER = "Select a start point and range; ctrl-drag to keep the current range; right-click to reset";

    int PANEL_HEIGHT = 30;
    int INDENT_LEFT = 65;
    int INDENT_RIGHT = 13;
    int INDEX_LEFT = 0;
    int INDEX_RIGHT = 1;


    /***********************************************************************************************
     * Create or Update the DatasetDomainUIComponent to show the details of a new XYDataset.
     * Re-initialise the DatasetDomain Control only if necessary.
     *
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param datasettypechanged
     * @param channelcountchanged
     * @param metadatachanged
     * @param rawdatachanged
     * @param processeddatachanged
     * @param isrefreshable
     * @param isclickrefresh
     * @param debug
     */

    void createOrUpdateDomainControl(DatasetType datasettype,
                                     XYDataset primarydataset,
                                     List<XYDataset> secondarydatasets,
                                     boolean datasettypechanged,
                                     boolean channelcountchanged,
                                     boolean metadatachanged,
                                     boolean rawdatachanged,
                                     boolean processeddatachanged,
                                     boolean isrefreshable,
                                     boolean isclickrefresh,
                                     boolean debug);


    /***********************************************************************************************
     * Get the RawData Channel count.
     *
     * @return int
     */

    int getRawDataChannelCount();


    /***********************************************************************************************
     * Set the RawData Channel count.
     *
     * @param count
     */

    void setRawDataChannelCount(int count);


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature (Usually a Staribus dataset).
     *
     * @return boolean
     */

    boolean hasTemperatureChannel();


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature
     * (Usually a Staribus dataset).
     *
     * @param temperature
     */

    void setTemperatureChannel(boolean temperature);


    /***********************************************************************************************
     * Get the JPanel holding the DatasetDomain component.
     *
     * @return JPanel
     */

    JPanel getDatasetDomainContainer();


    /***********************************************************************************************
     * Get the MThumbSlider of the DatasetDomain component.
     *
     * @return MThumbSlider
     */

    MThumbSlider getDatasetDomainSlider();


    /***********************************************************************************************
     * Reset the slider thumbs to their maximum extents.
     * Return a true flag if the reset operation was performed,
     * i.e. the sliders were not already at the extents.
     *
     * @return boolean
     */

    boolean resetToExtents();


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the List of Metadata upon which the Chart is based.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadata();


    /***********************************************************************************************
     * Get the List of Metadata upon which the Chart is based.
     *
     * @param metadata
     */

    void setMetadata(List<Metadata> metadata);


    /***********************************************************************************************
     * Get the Update Type.
     *
     * @return DataUpdateType
     */

    DataUpdateType getUpdateType();


    /***********************************************************************************************
     * Set the Update Type.
     *
     * @param updatetype
     */

    void setUpdateType(DataUpdateType updatetype);


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of DatasetDomainChangedEvents.
     *
     * @param eventsource
     */

    void notifyDatasetDomainChangedEvent(Object eventsource);


    /***********************************************************************************************
     * Get the DatasetDomainChangedListeners (mostly for testing).
     *
     * @return Vector<DatasetDomainChangedListener>
     */

    Vector<DatasetDomainChangedListener> getListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addDatasetDomainChangedListener(DatasetDomainChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeDatasetDomainChangedListener(DatasetDomainChangedListener listener);
    }
