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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts;

import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ChannelSelectionChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ChannelSelectionChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectionMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * ChannelSelectorUIComponent.
 */

public final class ChannelSelectorUIComponent extends UIComponent
                                              implements ChannelSelectorUIComponentInterface
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final ChartUIComponentPlugin hostChartUIComponent;
    private List<Metadata> listMetadata;
    private DataUpdateType updateType;
    private final String strResourceKey;

    // State
    private final List<ChannelSelectionMode> listSelectionModes;
    private int intChannelCount;
    private boolean boolTemperatureChannel;

    // UI State
    private boolean boolLinearMode;
    private boolean boolAutoranging;
    private boolean boolDecimating;
    private boolean boolLegend;
    private boolean boolShowChannels;

    // UI
    private final JPanel panelContainer;
    private final List<ChannelSelectorComboBox> listChannelMultipliers;
    private final List<JLabel> listLabelsForMetadata;
    private JCheckBox chkDecimate;

    // Listeners
    private final Vector<ChannelSelectionChangedListener> vecSelectionChangedListeners;


    /***************************************************************************************************
     * ChannelSelectorUIComponent.
     *
     * @param hostinstrument
     * @param hostchart
     * @param metadata
     * @param updatetype
     * @param resourcekey
     */

    public ChannelSelectorUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                      final ChartUIComponentPlugin hostchart,
                                      final List<Metadata> metadata,
                                      final DataUpdateType updatetype,
                                      final String resourcekey)
        {
        super();

        // Injections
        this.hostInstrument = hostinstrument;
        this.hostChartUIComponent = hostchart;
        this.listMetadata = metadata;
        this.updateType = updatetype;
        this.strResourceKey = resourcekey;

        // State
        this.listSelectionModes = new ArrayList<ChannelSelectionMode>(ObservatoryInterface.MAX_CHANNELS);
        this.intChannelCount = 0;
        this.boolTemperatureChannel = false;
        this.boolLinearMode = true;
        this.boolAutoranging = false;
        this.boolDecimating = false;
        this.boolLegend = false;
        // Default to channel selectors, e.g. GPS Scatter does not have channels
        this.boolShowChannels = true;

        // Make the container panel only once
        this.panelContainer = new JPanel();
        this.listChannelMultipliers = new ArrayList<ChannelSelectorComboBox>(ObservatoryInterface.MAX_CHANNELS);
        this.listLabelsForMetadata = new ArrayList<JLabel>(LABELS_DRIVEN_BY_METADATA);
        this.chkDecimate = null;

        this.vecSelectionChangedListeners = new Vector<ChannelSelectionChangedListener>(10);
        }


    /***********************************************************************************************
     * Initialise the ChannelSelectorUIComponent.
     */

    public final void initialiseUI()
        {
        final JScrollPane scrollPane;

        super.initialiseUI();

        getSelectorContainer().setLayout(new BoxLayoutFixed(getSelectorContainer(), BoxLayoutFixed.Y_AXIS));
        getSelectorContainer().setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Build the ChannelSelector panel if possible
        chkDecimate = ChannelSelectorHelper.buildChannelSelectors(this,
                                                                  getSelectorContainer(),
                                                                  getHostChartUIComponent(),
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  getMetadata(),
                                                                  LOADER_PROPERTIES.isChartDebug());
        getSelectorContainer().revalidate();

        // Finally listen to the host Chart for DatasetChanged
        if (getHostChartUIComponent() != null)
            {
            getHostChartUIComponent().addDatasetChangedListener(this);
            }

        scrollPane = new JScrollPane(getSelectorContainer(),
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        // Stop listening to the host chart
        if (getHostChartUIComponent() != null)
            {
            getHostChartUIComponent().removeDatasetChangedListener(this);
            }

        if (getChannelSelectionChangedListeners() != null)
            {
            getChannelSelectionChangedListeners().clear();
            }

        if (getChannelSelectionModes() != null)
            {
            getChannelSelectionModes().clear();
            }

        if (getChannelMultipliers() != null)
            {
            getChannelMultipliers().clear();
            }

        if (getLabelsForMetadata() != null)
            {
            getLabelsForMetadata().clear();
            }

        super.disposeUI();
        }


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

    public void createOrUpdateSelectors(final DatasetType datasettype,
                                        final XYDataset primarydataset,
                                        final List<XYDataset> secondarydatasets,
                                        final boolean datasettypechanged,
                                        final boolean channelcountchanged,
                                        final boolean metadatachanged,
                                        final boolean rawdatachanged,
                                        final boolean processeddatachanged,
                                        final boolean isrefreshable,
                                        final boolean isclickrefresh,
                                        final boolean debug)
        {
        final String SOURCE = "ChannelSelectorUIComponent.createOrUpdateSelectors() ";

        if (primarydataset != null)
            {
            LOGGER.debug(debug,
                         SOURCE + "Rebuild Channel Selectors [datasettype_changed=" + datasettypechanged
                         + "] [channel.count_changed=" + channelcountchanged
                         + "] [channel.count.selector=" + getChannelCount()
                         + "] [channel.count.primarydataset=" + primarydataset.getSeriesCount()
                         + "] [selector.showchannels=" + showChannels()
                         + "]");
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "Rebuild Channel Selectors [datasettype_changed=" + datasettypechanged
                         + "] [channel.count_changed=" + channelcountchanged
                         + "] [channel.count.selector=" + getChannelCount()
                         + "] [channel.count.primarydataset=NULL"
                         + "] [selector.showchannels=" + showChannels()
                         + "]");
            }

        debugSelector(debug, SOURCE);

        // Channel Selector
        if ((datasettypechanged)
            ||(channelcountchanged))
            {
            LOGGER.debug(debug,
                         SOURCE + "DatasetType or ChannelCount changed, rebuild ChannelSelectors");

            chkDecimate = ChannelSelectorHelper.buildChannelSelectors(this,
                                                                      getSelectorContainer(),
                                                                      getHostChartUIComponent(),
                                                                      datasettype,
                                                                      primarydataset,
                                                                      secondarydatasets,
                                                                      getMetadata(),
                                                                      debug);
            }
        else if (metadatachanged)
            {
            LOGGER.debug(debug,
                         SOURCE + "Metadata changed, update ChannelSelectors");

            ChannelSelectorHelper.updateChannelSelectorMetadata(this,
                                                                getMetadata(),
                                                                debug);
            }
        else if ((rawdatachanged)
                 ||(processeddatachanged))
            {
            LOGGER.debug(debug,
                         SOURCE + "RawData or ProcessedData changed, no action taken");
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "No changes, no action taken");
            }

        // ToDo checkboxes also.....

        if (getDecimationCheckbox() != null)
            {
            getDecimationCheckbox().setEnabled(ChannelSelectorHelper.isDecimationEnabled(getHostChartUIComponent(), primarydataset));

            LOGGER.debug(debug,
                         SOURCE + "Update Decimation Checkbox [decimate=" + getDecimationCheckbox().isEnabled() + "]");
            }
        }


    /***********************************************************************************************
     * Get the Channel count.
     *
     * @return int
     */

    public int getChannelCount()
        {
        return (this.intChannelCount);
        }


    /***********************************************************************************************
     * Set the Channel count.
     *
     * @param count
     */

    public void setChannelCount(final int count)
        {
        this.intChannelCount = count;
        }


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature.
     * (Usually only in a Staribus dataset).
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (this.boolTemperatureChannel);
        }


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature.
     * (Usually only in a Staribus dataset).
     *
     * @param temperature
     */

    public void setTemperatureChannel(final boolean temperature)
        {
        this.boolTemperatureChannel = temperature;
        }


    /**********************************************************************************************/
    /* State                                                                                      */
    /***********************************************************************************************
     * Get the ChannelSelectionModes for each channel.
     *
     * @return List<ChannelSelectionMode>
     */

    public List<ChannelSelectionMode> getChannelSelectionModes()
        {
        return (this.listSelectionModes);
        }


    /***********************************************************************************************
     * Get the List of Combo Boxes which select the Channel Multipliers.
     *
     * @return List<ChannelSelectorComboBox>
     */

    public List<ChannelSelectorComboBox> getChannelMultipliers()
        {
        return (this.listChannelMultipliers);
        }


    /***********************************************************************************************
     * Get the List of JLabels which are set from the Metadata.
     *
     * @return List<JLabel>
     */

    public List<JLabel> getLabelsForMetadata()
        {
        return (this.listLabelsForMetadata);
        }


    /***********************************************************************************************
     * Indicate if the Chart is in Linear mode.
     *
     * @return boolean
     */

     public boolean isLinearMode()
        {
         return(this.boolLinearMode);
        }


    /***********************************************************************************************
     * Indicate if the Chart is in Linear mode.
     *
     * @param linearmode
     */

    public void setLinearMode(final boolean linearmode)
        {
        this.boolLinearMode = linearmode;
        }


    /***********************************************************************************************
     * Indicate if the Chart is currently Autoranging.
     *
     * @return boolean
     */

     public boolean isAutoranging()
        {
         return(this.boolAutoranging);
        }


    /***********************************************************************************************
     * Indicate if the Chart is currently Autoranging.
     *
     * @param autoranging
     */

    public void setAutoranging(final boolean autoranging)
        {
        this.boolAutoranging = autoranging;
        }


    /***********************************************************************************************
     * Indicate if the Chart is Decimating.
     *
     * @return boolean
     */

     public boolean isDecimating()
        {
         return(this.boolDecimating);
        }


    /***********************************************************************************************
     * Indicate if the Chart is Decimating.
     *
     * @param decimating
     */

    public void setDecimating(final boolean decimating)
        {
        this.boolDecimating = decimating;
        }


    /***********************************************************************************************
     * Indicate if the Chart has a Legend.
     *
     * @return boolean
     */

     public boolean hasLegend()
        {
         return(this.boolLegend);
        }


    /***********************************************************************************************
     * Indicate if the Chart has a Legend.
     *
     * @param legend
     */

    public void setLegend(final boolean legend)
        {
        this.boolLegend = legend;
        }


    /***********************************************************************************************
     * Indicate if the ChannelSelector should display the Channel configuration panels.
     *
     * @return boolean
     */

     public boolean showChannels()
        {
         return(this.boolShowChannels);
        }


    /***********************************************************************************************
     * Indicate if the ChannelSelector should display the Channel configuration panels.
     *
     * @param showchannels
     */

    public void setShowChannels(final boolean showchannels)
        {
        this.boolShowChannels = showchannels;
        }


    /***********************************************************************************************
     * Get the JPanel holding the Selectors.
     *
     * @return JPanel
     */

    public JPanel getSelectorContainer()
        {
        return (this.panelContainer);
        }


    /***********************************************************************************************
     * Get the Decimation Checkbox.
     *
     * @return JCheckBox
     */

    private JCheckBox getDecimationCheckbox()
        {
        return (this.chkDecimate);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the Host ChartUIComponent controlled by the Channel Selector.
     *
     * @return ChartUIComponentPlugin
     */

    private ChartUIComponentPlugin getHostChartUIComponent()
        {
        return (this.hostChartUIComponent);
        }


    /***********************************************************************************************
     * Get the List of Metadata upon which the Chart is based.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata upon which the Chart is based.
     *
     * @param metadata
     */

    public void setMetadata(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the Update Type.
     *
     * @return DataUpdateType
     */

    public DataUpdateType getUpdateType()
        {
        return (this.updateType);
        }


    /***********************************************************************************************
     * Set the Update Type.
     *
     * @param updatetype
     */

    public void setUpdateType(final DataUpdateType updatetype)
        {
        this.updateType = updatetype;
        }


    /***********************************************************************************************
     * Show the state of the Channel Selector controls, for debugging.
     *
     * @param debug
     * @param title
     */

    public void debugSelector(final boolean debug,
                              final String title)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        buffer.append(title);
        buffer.append(" [channel.count=");
        buffer.append(getChannelCount());
        buffer.append("] [temperature=");
        buffer.append(hasTemperatureChannel());
        buffer.append("] [linear=");
        buffer.append(isLinearMode());
        buffer.append("] [autorange=");
        buffer.append(isAutoranging());
        buffer.append("] [decimate=");
        buffer.append(isDecimating());
        buffer.append("] [legend=");
        buffer.append(hasLegend());
        buffer.append("] [channels=");
        buffer.append(showChannels());
        buffer.append("] ");

        if (getChannelSelectionModes() != null)
            {
            for (int intModeIndex = 0;
                 intModeIndex < getChannelSelectionModes().size();
                 intModeIndex++)
                {
                final ChannelSelectionMode mode;

                mode = getChannelSelectionModes().get(intModeIndex);

                buffer.append("[index=");
                buffer.append(intModeIndex);
                buffer.append("] [mode=");
                buffer.append(mode.toString());
                buffer.append("] ");
                }
            }

        LOGGER.debug(debug, buffer.toString());
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Channel Selector.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Indicate that something changed in the Dataset.
     * We need to know in order to re-evaluate the enabled state of the decimation checkbox.
     *
     * @param event
     */

    public void datasetChanged(final DatasetChangedEvent event)
        {
        final String SOURCE = "ChannelSelectorUIComponent.datasetChanged() ";

        if (getDecimationCheckbox() != null)
            {
            getDecimationCheckbox().setEnabled(ChannelSelectorHelper.isDecimationEnabled(getHostChartUIComponent(), event));

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "Decimation Checkbox [enabled=" + getDecimationCheckbox().isEnabled() + "]");
            }
        }


    /***********************************************************************************************
     * Notify all listeners of ChannelSelectionChangedEvents.
     *
     * @param eventsource
     * @param crop
     */

    public final void notifyChannelSelectionChangedEvent(final Object eventsource,
                                                         final boolean crop)
        {
        final String SOURCE = "ChannelSelectorUIComponent.notifyChannelSelectionChangedEvent() ";
        List<ChannelSelectionChangedListener> listeners;
        final ChannelSelectionChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<ChannelSelectionChangedListener>(getChannelSelectionChangedListeners());

        // Create an ChannelSelectionChangedEvent
        changeEvent = new ChannelSelectionChangedEvent(eventsource, crop);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final ChannelSelectionChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.channelSelectionChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the ChannelSelectionChanged Listeners (mostly for testing).
     *
     * @return Vector<ChannelSelectionChangedListener>
     */

    public final Vector<ChannelSelectionChangedListener> getChannelSelectionChangedListeners()
        {
        final String SOURCE = "ChannelSelectorUIComponent.getChannelSelectionChangedListeners() ";

        return (this.vecSelectionChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addChannelSelectionChangedListener(final ChannelSelectionChangedListener listener)
        {
        final String SOURCE = "ChannelSelectorUIComponent.addChannelSelectionChangedListener() ";

        if ((listener != null)
            && (getChannelSelectionChangedListeners() != null)
            && (!getChannelSelectionChangedListeners().contains(listener)))
            {
            getChannelSelectionChangedListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeChannelSelectionChangedListener(final ChannelSelectionChangedListener listener)
        {
        final String SOURCE = "ChannelSelectorUIComponent.removeChannelSelectionChangedListener() ";

        if ((listener != null)
            && (getChannelSelectionChangedListeners() != null))
            {
            getChannelSelectionChangedListeners().removeElement(listener);
            }
        }
    }
