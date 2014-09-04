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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ChannelSelectionChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChannelSelectorComboBox;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * ChannelSelectorUIComponentInterface.
 */

public interface ChannelSelectorUIComponentInterface extends UIComponentPlugin,
                                                             DatasetChangedListener
    {
    // String Resources
    String TOOLTIP_SELECTION_MODE = "Select a Gain Factor for this Channel";
    String TOOLTIP_RESET_ALL = "Reset all gain factors to x1";
    String TOOLTIP_CROP = "Crop the RawData to the selected range";
    String LABEL_AUTORANGE = "Autorange";
    String TOOLTIP_AUTORANGE = "Select Autoranging for this Chart";
    String LABEL_DECIMATE = "Decimate";
    String TOOLTIP_DECIMATE = "Select Decimation for this Chart";
    String LABEL_LEGEND = "Legend";
    String TOOLTIP_LEGEND = "Add a legend to this Chart";
    String TOOLTIP_COLOUR_NOT_FOUND = "Channel colour not found in metadata";
    String TOOLTIP_DESCRIPTION_NOT_FOUND = "Channel description not found in metadata";
    String BUTTON_RESET_ALL = "Reset All";
    String BUTTON_CROP = "Crop to Range";

    int LABELS_DRIVEN_BY_METADATA = ObservatoryInterface.MAX_CHANNELS << 1;


    /***********************************************************************************************
     * Create or Update the ChannelSelector to show the details of new XYDatasets.
     * Re-initialise the Channel Selector only if necessary.
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

    void createOrUpdateSelectors(DatasetType datasettype,
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
     * Get the Channel count.
     *
     * @return int
     */

    int getChannelCount();


    /***********************************************************************************************
     * Set the Channel count.
     *
     * @param count
     */

    void setChannelCount(int count);


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature.
     * (Usually only in a Staribus dataset).
     *
     * @return boolean
     */

    boolean hasTemperatureChannel();


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature.
     * (Usually only in a Staribus dataset).
     *
     * @param temperature
     */

    void setTemperatureChannel(boolean temperature);


    /**********************************************************************************************/
    /* State                                                                                      */
    /***********************************************************************************************
     * Get the ChannelSelectionModes for each channel.
     *
     * @return List<ChannelSelectionMode>
     */

    List<ChannelSelectionMode> getChannelSelectionModes();


    /***********************************************************************************************
     * Get the List of Combo Boxes which select the Channel Multipliers.
     *
     * @return List<ChannelSelectorComboBox>
     */

    List<ChannelSelectorComboBox> getChannelMultipliers();


    /***********************************************************************************************
     * Get the List of JLabels which are set from the Metadata.
     *
     * @return List<JLabel>
     */

    List<JLabel> getLabelsForMetadata();


    /***********************************************************************************************
     * Indicate if the Chart is in Linear mode.
     *
     * @return boolean
     */

    boolean isLinearMode();


    /***********************************************************************************************
     * Indicate if the Chart is in Linear mode.
     *
     * @param linearmode
     */

    void setLinearMode(boolean linearmode);


    /***********************************************************************************************
     * Indicate if the Chart is currently Autoranging.
     *
     * @return boolean
     */

     boolean isAutoranging();


    /***********************************************************************************************
     * Indicate if the Chart is currently Autoranging.
     *
     * @param autoranging
     */

    void setAutoranging(boolean autoranging);


    /***********************************************************************************************
     * Indicate if the Chart is Decimating.
     *
     * @return boolean
     */

     boolean isDecimating();


    /***********************************************************************************************
     * Indicate if the Chart is Decimating.
     *
     * @param decimating
     */

    void setDecimating(boolean decimating);


    /***********************************************************************************************
     * Indicate if the Chart has a Legend.
     *
     * @return boolean
     */

     boolean hasLegend();


    /***********************************************************************************************
     * Indicate if the Chart has a Legend.
     *
     * @param legend
     */

    void setLegend(boolean legend);


    /***********************************************************************************************
     * Indicate if the ChannelSelector should display the Channel configuration panels.
     *
     * @return boolean
     */

    boolean showChannels();


    /***********************************************************************************************
     * Indicate if the ChannelSelector should display the Channel configuration panels.
     *
     * @param showchannels
     */

    void setShowChannels(boolean showchannels);


    /***********************************************************************************************
     * Get the JPanel holding the Selectors.
     *
     * @return JPanel
     */

    JPanel getSelectorContainer();


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


    /***********************************************************************************************
     * Show the state of the Channel Selector controls, for debugging.
     *
     * @param debug
     * @param title
     */

    void debugSelector(boolean debug,
                       String title);


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Notify all listeners of ChannelSelectionChangedEvents.
     *
     * @param eventsource
     * @param crop
     */

    void notifyChannelSelectionChangedEvent(Object eventsource,
                                            final boolean crop);


    /***********************************************************************************************
     * Get the ChannelSelectionChanged Listeners (mostly for testing).
     *
     * @return Vector<ChannelSelectionChangedListener>
     */

    Vector<ChannelSelectionChangedListener> getChannelSelectionChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addChannelSelectionChangedListener(ChannelSelectionChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeChannelSelectionChangedListener(ChannelSelectionChangedListener listener);
    }
