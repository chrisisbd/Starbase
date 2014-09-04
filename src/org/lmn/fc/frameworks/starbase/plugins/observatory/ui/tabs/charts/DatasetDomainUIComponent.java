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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts;


import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetDomainChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetDomainChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.multipleslider.BasicMThumbSliderUI;
import org.lmn.fc.ui.multipleslider.MThumbSlider;
import org.lmn.fc.ui.multipleslider.MThumbSliderAdditional;
import org.lmn.fc.ui.multipleslider.MThumbSliderAdditionalUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * DatasetDomainUIComponent.
 */

public final class DatasetDomainUIComponent extends UIComponent
                                            implements DatasetDomainUIComponentInterface,
                                                       DatasetDomainChangedListener
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final ChartUIComponentPlugin hostChart;
    private List<Metadata> listMetadata;
    private DataUpdateType updateType;
    private final String strResourceKey;

    // State
    private int intChannelCount;
    private boolean boolTemperatureChannel;

    private final Vector<DatasetDomainChangedListener> vecListeners;

    // UI
    private final JPanel panelContainer;
    private MThumbSlider slider;


    /***********************************************************************************************
     * Build the Slider on its container panel.
     *
     * @param datasetdomain
     * @param xydataset
     * @param slidercontainer
     * @param debug
     *
     * @return MThumbSlider
     */

    private static MThumbSlider buildSlider(final DatasetDomainUIComponentInterface datasetdomain,
                                            final XYDataset xydataset,
                                            final JPanel slidercontainer,
                                            final boolean debug)
        {
        final String SOURCE = "DatasetDomainUIComponent.buildSlider() ";
        MThumbSlider mSlider;

        slidercontainer.removeAll();
        slidercontainer.setPreferredSize(new Dimension(Integer.MAX_VALUE, 0));
        mSlider = null;

        if ((xydataset != null)
            && (xydataset.getSeriesCount() > 0))
            {
            LOGGER.debug(debug,
                         SOURCE + "Building a new DatasetDomain slider [series.count=" + xydataset.getSeriesCount() + "]");

            // Two knobs on the slider...
            // WARNING! setMinimum() and setMaximum() need attention...
            mSlider = new MThumbSlider(2,
                                       ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                       ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM);
            mSlider.setUI(new BasicMThumbSliderUI());
            mSlider.addChangeListener(new ChangeListener()
                {
                public void stateChanged(final ChangeEvent event)
                    {
                    final Object source;

                    source = event.getSource();

                    // Only notify the DatasetDomainUIComponent if the slider has stopped moving
                    if ((source instanceof MThumbSlider)
                        && (!((JSlider) source).getValueIsAdjusting()))
                        {
                        final MThumbSlider thumbSlider;

                        thumbSlider = (MThumbSlider) source;
                        LOGGER.debug(debug,
                                     SOURCE + "ChangeEvent slider [value.0="
                                         + thumbSlider.getValueAt(0)
                                         + "] [value.1=" + thumbSlider.getValueAt(1)
                                         + "] --> notifyDatasetDomainChangedEvent");

                        datasetdomain.notifyDatasetDomainChangedEvent(event.getSource());
                        }
                    else
                        {
                        LOGGER.debug(debug,
                                     SOURCE + "ChangeEvent No action taken - IsAdjusting? [instanceof=" + (source instanceof MThumbSlider) + "]");
                        }
                    }
                });

            mSlider.setBackground(DEFAULT_COLOUR_CANVAS.getColor());
            mSlider.setOrientation(JSlider.HORIZONTAL);
            mSlider.setValueAt(mSlider.getMinimum(), 0);
            mSlider.setValueAt(mSlider.getMaximum(), 1);
            mSlider.setToolTipText(TOOLTIP_SLIDER);

            slidercontainer.setPreferredSize(new Dimension(Integer.MAX_VALUE, PANEL_HEIGHT));
            slidercontainer.add(mSlider);
            }

        return(mSlider);
        }


    /***********************************************************************************************
     * DatasetDomainUIComponent.
     *
     * @param hostinstrument
     * @param hostchart
     * @param metadatalist
     * @param updatetype
     * @param resourcekey
     */

    public DatasetDomainUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                    final ChartUIComponentPlugin hostchart,
                                    final List<Metadata> metadatalist,
                                    final DataUpdateType updatetype,
                                    final String resourcekey)
        {
        super();

        this.hostInstrument = hostinstrument;
        this.hostChart = hostchart;
        this.listMetadata = metadatalist;
        this.updateType = updatetype;
        this.strResourceKey = resourcekey;

        this.vecListeners = new Vector<DatasetDomainChangedListener>(10);

        // Make the container panel only once
        this.panelContainer = new JPanel();

        this.slider = null;
        }


    /***********************************************************************************************
     * Initialise the DatasetDomainUIComponent.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        getDatasetDomainContainer().setLayout(new BoxLayoutFixed(getDatasetDomainContainer(), BoxLayoutFixed.X_AXIS));
        getDatasetDomainContainer().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        getDatasetDomainContainer().setPreferredSize(new Dimension(Integer.MAX_VALUE, 0));

        addDatasetDomainChangedListener(this);

        add(getDatasetDomainContainer(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        this.slider = null;

        if (getDatasetDomainContainer() != null)
            {
            getDatasetDomainContainer().removeAll();
            }

        removeDatasetDomainChangedListener(this);

        if (getListeners() != null)
            {
            getListeners().clear();
            }

        super.disposeUI();
        }


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

     public void createOrUpdateDomainControl(final DatasetType datasettype,
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
         final String SOURCE = "DatasetDomainUIComponent.createOrUpdateDomainControl() ";

         if ((datasettypechanged)
             || (channelcountchanged))
             {
             LOGGER.debug(debug,
                          SOURCE + "DatasetType or ChannelCount changed, rebuild DatasetDomainControl, reset thumbs");

             setDatasetDomainSlider(buildSlider(this,
                                                primarydataset,
                                                getDatasetDomainContainer(),
                                                debug));

             // Reset the slider thumbs every time we do a rebuild
             if (getDatasetDomainSlider() != null)
                 {
                 getDatasetDomainSlider().setValueAt(getDatasetDomainSlider().getMinimum(), INDEX_LEFT);
                 getDatasetDomainSlider().setValueAt(getDatasetDomainSlider().getMaximum(), INDEX_RIGHT);

                 // Any previous listeners will be evaporated...
                 getDatasetDomainSlider().addChangeListener(getHostChart());
                 }
             }
         else if (metadatachanged)
             {
             LOGGER.debug(debug,
                          SOURCE + "Metadata changed, no action taken");

             MetadataHelper.showMetadataList(getMetadata(),
                                             SOURCE,
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
         }


    /***********************************************************************************************
     * Get the RawData Channel count.
     *
     * @return int
     */

    public int getRawDataChannelCount()
        {
        return (this.intChannelCount);
        }


    /***********************************************************************************************
     * Set the RawData Channel count.
     *
     * @param count
     */

    public void setRawDataChannelCount(final int count)
        {
        this.intChannelCount = count;
        }


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature (Usually a Staribus dataset).
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (this.boolTemperatureChannel);
        }


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature
     * (Usually a Staribus dataset).
     *
     * @param temperature
     */

    public void setTemperatureChannel(final boolean temperature)
        {
        this.boolTemperatureChannel = temperature;
        }


    /***********************************************************************************************
     * Get the JPanel holding the DatasetDomain component.
     *
     * @return JPanel
     */

    public JPanel getDatasetDomainContainer()
        {
        return (this.panelContainer);
        }


    /***********************************************************************************************
     * Get the MThumbSlider of the DatasetDomain component.
     *
     * @return MThumbSlider
     */

    public MThumbSlider getDatasetDomainSlider()
        {
        return (this.slider);
        }


    /***********************************************************************************************
     * Set the MThumbSlider of the DatasetDomain component.
     *
     * @param mslider
     */

    private void setDatasetDomainSlider(final MThumbSlider mslider)
        {
        this.slider = mslider;
        }


    /***********************************************************************************************
     * Reset the slider thumbs to their maximum extents.
     * Return a true flag if the reset operation was performed,
     * i.e. the sliders were not already at the extents.
     *
     * @return boolean
     */

    public boolean resetToExtents()
        {
        final String SOURCE = "DatasetDomainUIComponent.resetToExtents() ";
        boolean boolReset;

        boolReset = false;

         if (getDatasetDomainSlider() != null)
             {
             if ((getDatasetDomainSlider().getUI() != null)
                 && (getDatasetDomainSlider().getUI() instanceof MThumbSliderAdditional))
                 {
                 final MThumbSliderAdditionalUI additionalUI;

                 additionalUI = ((MThumbSliderAdditional) getDatasetDomainSlider().getUI()).getAdditionalUI();

                 if ((additionalUI != null)
                     && (additionalUI.getTrackListener() != null))
                     {
                     boolReset = additionalUI.getTrackListener().resetToExtents();

                     if ((boolReset)
                         && (getDatasetDomainSlider() != null))
                         {
                         getDatasetDomainSlider().repaint();
                         }

                     LOGGER.debug((LOADER_PROPERTIES.isChartDebug() && boolReset),
                                  SOURCE + "Sliders reset to extents");
                     }
                 else
                     {
                     LOGGER.error("DatasetDomainUIComponent.resetToExtents() Incorrect TrackListener");
                     }
                 }
             else
                 {
                 LOGGER.error("DatasetDomainUIComponent.resetToExtents() Incorrect SliderUI");
                 }
             }

        if (getDatasetDomainContainer() != null)
            {
            getDatasetDomainContainer().repaint();
            }

        return (boolReset);
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
     * Get the Host Chart controlled by the Channel Selector.
     *
     * @return ChartUIComponentPlugin
     */

    private ChartUIComponentPlugin getHostChart()
        {
        return (this.hostChart);
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
     * Get the List of Metadata upon which the Chart is based.
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
    /*******************************************************************************************
     * Indicate that something changed on the DatasetDomain panel.
     *
     * @param event
     */

    public void datasetDomainChanged(final DatasetDomainChangedEvent event)
        {
        final String SOURCE = "DatasetDomainUIComponent datasetDomainChanged() ";

        if ((event != null)
            && (event.getSource() instanceof MThumbSlider)
            && (getDatasetDomainContainer() != null))
            {
            getDatasetDomainContainer().repaint();
            }
        else
            {
            LOGGER.error(SOURCE + "Incorrect source!");
            }
        }


    /***********************************************************************************************
     * Notify all listeners of DatasetDomainChangedEvents.
     *
     * @param eventsource
     */

    public final void notifyDatasetDomainChangedEvent(final Object eventsource)
        {
        List<DatasetDomainChangedListener> listeners;
        final DatasetDomainChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<DatasetDomainChangedListener>(getListeners());

        // Create an DatasetDomainChangedEvent
        changeEvent = new DatasetDomainChangedEvent(eventsource);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final DatasetDomainChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.datasetDomainChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the DatasetDomainChangedListeners (mostly for testing).
     *
     * @return Vector<DatasetDomainChangedListener>
     */

    public final Vector<DatasetDomainChangedListener> getListeners()
        {
        return (this.vecListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addDatasetDomainChangedListener(final DatasetDomainChangedListener listener)
        {
        if ((listener != null)
            && (getListeners() != null)
            && (!getListeners().contains(listener)))
            {
            getListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeDatasetDomainChangedListener(final DatasetDomainChangedListener listener)
        {
        if ((listener != null)
            && (getListeners() != null))
            {
            getListeners().removeElement(listener);
            }
        }
    }
