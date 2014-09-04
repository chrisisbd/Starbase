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

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.DatasetChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;


/**************************************************************************************************
 * ChartUIComponentPlugin.
 */

public interface ChartUIComponentPlugin extends UIComponentPlugin,
                                                ChangeListener
    {
    // String Resources
    String TOOLTIP_CLICK_HERE = "Click here to see the latest information";

    int INDEX_AXIS = 0;
    int INDEX_DATA = 0;

    Rectangle2D.Double SHAPE_LEGEND = new Rectangle2D.Double(-7.0, -1.0, 14.0, 2.0);

    // Chart configuration options (ideally these should be enums)
    boolean REFRESHABLE = true;
    boolean NON_REFRESHABLE = false;
    boolean REFRESH_CLICK = true;
    boolean REFRESH_NONE = false;

    // ToDo Tidy up these constants to be less complicated...

    // Dataset Range (Awaiting Development)
    int RANGE_MIN = 0;
    int RANGE_MAX = 1000;

    // Dataset Domain (Slider and Offset Control)
    int DOMAIN_MIN = 0;
    int DOMAIN_MAX = 1000;

    int DOMAIN_SLIDER_MINIMUM = DOMAIN_MIN;
    int DOMAIN_SLIDER_MAXIMUM = DOMAIN_MAX;

    int OFFSET_CONTROL_COARSE_MINIMUM = -100;
    int OFFSET_CONTROL_COARSE_MAXIMUM = 100;

    int OFFSET_CONTROL_FINE_MINIMUM = 0;
    int OFFSET_CONTROL_FINE_MAXIMUM = OFFSET_CONTROL_COARSE_MAXIMUM / 10;


    /***********************************************************************************************
     * Get the Chart name.
     *
     * @return String
     */

    String getChartName();


    /***********************************************************************************************
     * Set the Chart name.
     *
     * @param name
     */

    void setChartName(String name);


    /***********************************************************************************************
     * Get the ChartPanel containing the JFreeChart.
     *
     * @return ChartPanel
     */

    ChartPanel getChartPanel();


    /***********************************************************************************************
     * Set the ChartPanel containing the JFreeChart.
     *
     * @param chartpanel
     */

    void setChartPanel(ChartPanel chartpanel);


    /***********************************************************************************************
     * Conditionally create or refresh the Chart on a separate thread.
     *
     * @param dao
     * @param generateflag
     * @param message
     */

    void refreshChart(ObservatoryInstrumentDAOInterface dao,
                      boolean generateflag,
                      String message);


    /***********************************************************************************************
     * Create a new Chart and customise the XYPlot, e.g. for fixed range axes.
     *
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param updatetype
     * @param displaylimit
     * @param channelselector
     * @param debug
     *
     * @return JFreeChart
     */

    JFreeChart createCustomisedChart(DatasetType datasettype,
                                     XYDataset primarydataset,
                                     List<XYDataset> secondarydatasets,
                                     DataUpdateType updatetype,
                                     int displaylimit,
                                     ChannelSelectorUIComponentInterface channelselector,
                                     boolean debug);


    /***********************************************************************************************
     * Update the existing Chart referenced by the DAO, by applying the specified datasets,
     * using all existing channel selections and range selections.
     *
     * @param dao
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param displaylimit
     * @param domainstartpoint
     * @param domainendpoint
     * @param channelselector
     * @param debug
     */

    void updateChartForSelection(ObservatoryInstrumentDAOInterface dao,
                                 DatasetType datasettype,
                                 XYDataset primarydataset,
                                 List<XYDataset> secondarydatasets,
                                 int displaylimit,
                                 int domainstartpoint,
                                 int domainendpoint,
                                 ChannelSelectorUIComponentInterface channelselector,
                                 boolean debug);


    /***********************************************************************************************
     * Set the Channel count.
     *
     * @param count
     */

    void setChannelCount(int count);


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature
     * (Usually a Staribus dataset).
     *
     * @param temperature
     */

    void setTemperatureChannel(boolean temperature);


    /***********************************************************************************************
     * Get the optional JToolBar.
     * Default is NULL, override if needed.
     *
     * @return JToolBar
     */

    JToolBar getToolBar();


    /***********************************************************************************************
     * Get the Chart JPanel container.
     *
     * @return JPanel
     */

    JPanel getChartContainer();


    /**********************************************************************************************/
    /* Data and Metadata                                                                          */
    /***********************************************************************************************
     * Get the DAO providing data for this Chart.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getDAO();


    /***********************************************************************************************
     * Set the DAO providing data for this Chart.
     *
     * @param dao
     */

    void setDAO(ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Get the List of Metadata for this Chart.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadata();


    /***********************************************************************************************
     * Set the List of Metadata for this Chart, and associate the Chart with the specified DAO.
     * Set updatedaometadata to TRUE if the specified Metadata should be copied into the
     * appropriate containers in the specified DAO.
     * If the Metadata was already collected from the DAO, just set updatedaometadata to FALSE.
     *
     * @param metadatalist
     * @param dao
     * @param updatedaometadata
     * @param debug
     */

    void setMetadata(List<Metadata> metadatalist,
                     ObservatoryInstrumentDAOInterface dao,
                     boolean updatedaometadata,
                     boolean debug);


    /***********************************************************************************************
     * Get the Primary XYDataset to be shown on the ChartUIComponent.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     *
     * @return XYDataset
     */

    XYDataset getPrimaryXYDataset();


    /***********************************************************************************************
     * Set the primary XYDataset, adjust the type of the Dataset if required.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     *
     * @param dao
     * @param dataset
     */

    void setPrimaryXYDataset(ObservatoryInstrumentDAOInterface dao,
                             XYDataset dataset);


    /***********************************************************************************************
     * Get the DatasetType - Tabular, XY or Timestamped.
     *
     * @return DatasetType
     */

    DatasetType getDatasetType();


    /***********************************************************************************************
     * Indicate if the Chart has a ChannelSelector.
     *
     * @return boolean
     */

    boolean hasChannelSelector();


    /***********************************************************************************************
     * Indicate if the Chart has a ChannelSelector.
     *
     * @param hasselector
     */

    void setChannelSelector(boolean hasselector);


    /***********************************************************************************************
     * Get the Chart Channel Selector.
     *
     * @return ChannelSelectorUIComponentInterface
     */

    ChannelSelectorUIComponentInterface getChannelSelectorOccupant();


    /***********************************************************************************************
     * Set the Chart Channel Selector.
     *
     * @param selector
     */

    void setChannelSelectorOccupant(ChannelSelectorUIComponentInterface selector);


    /***********************************************************************************************
     * Indicate if the ChannelSelection of the chart data has changed.
     *
     * @return boolean
     */

    boolean isChannelSelectionChanged();


    /***********************************************************************************************
     * Indicate if the ChannelSelection of the chart data has changed.
     *
     * @param changed
     */

    void setChannelSelectionChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if the Chart can Autorange, or must stay at FixedRange
     *
     * @return boolean
     */

    boolean canAutorange();


    /***********************************************************************************************
     * Indicate if the Chart can Autorange, or must stay at FixedRange
     *
     * @param autorange
     */

    void setCanAutorange(boolean autorange);


    /***********************************************************************************************
     * Indicate if the Chart has a DatasetDomainControl.
     *
     * @return boolean
     */

    boolean hasDatasetDomainControl();


    /***********************************************************************************************
     * Indicate if the Chart has a DatasetDomainControl.
     *
     * @param hasrangecontrol
     */

    void setDatasetDomainControl(boolean hasrangecontrol);


    /***********************************************************************************************
     * Indicate if the DatasetDomain of the chart data has changed.
     *
     * @return boolean
     */

    boolean isDatasetDomainChanged();


    /***********************************************************************************************
     * Indicate if the DatasetDomain of the chart data has changed.
     *
     * @param changed
     */

    void setDatasetDomainChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if this Chart has a Linear display mode.
     *
     * @return boolean
     */

    boolean hasLinearMode();


    /***********************************************************************************************
     * Indicate if this Chart has a Logarithmic display mode.
     *
     * @return boolean
     */

    boolean hasLogarithmicMode();


    /***********************************************************************************************
     * Get the Range Crosshair value.
     * Returns 0 if no crosshair is in use.
     *
     * @return double
     */

    double getRangeCrosshair();


    /***********************************************************************************************
     * Set the Range Crosshair value.
     * Set 0 if no crosshair is in use.
     *
     * @param value
     */

    void setRangeCrosshair(double value);


    /***********************************************************************************************
     * Get the Domain Crosshair value.
     * Returns DOMAIN_SLIDER_MINIMUM if no crosshair is in use.
     *
     * @return double
     */

    double getDomainCrosshair();


    /***********************************************************************************************
     * Set the Domain Crosshair value.
     * Set DOMAIN_SLIDER_MINIMUM if no crosshair is in use.
     *
     * @param value
     */

    void setDomainCrosshair(double value);


    /***********************************************************************************************
     * Get the Display Limit.
     *
     * @return int
     */

    int getDisplayLimit();


    /***********************************************************************************************
     * Notify all listeners of DatasetChangedEvents.
     *
     * @param eventsource
     * @param seriescount
     * @param itemcount0
     */

    void notifyDatasetChangedEvent(Object eventsource,
                                   int seriescount,
                                   int itemcount0);


    /***********************************************************************************************
     * Get the DatasetChanged Listeners (mostly for testing).
     *
     * @return Vector<DatasetChangedListener>
     */

    Vector<DatasetChangedListener> getDatasetChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addDatasetChangedListener(DatasetChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeDatasetChangedListener(DatasetChangedListener listener);
    }
