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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.List;


/***************************************************************************************************
 * The GOESChartUIComponent.
 */

public final class GOESChartUIComponent extends ChartUIComponent
                                        implements ChartUIComponentPlugin
    {
    private static final int INDEX_FLUX = 0;
    private static final int INDEX_RATIO = 1;

    // The Channel Count is the sum of the Primary and Secondary Datasets
    private static final int DAO_CHANNEL_COUNT = 3;


    /***********************************************************************************************
     * Construct a GOESChartUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param name
     * @param metadatalist
     * @param resourcekey
     * @param updatetype
     * @param displaylimit
     */

    public GOESChartUIComponent(final TaskPlugin task,
                                final ObservatoryInstrumentInterface hostinstrument,
                                final String name,
                                final List<Metadata> metadatalist,
                                final String resourcekey,
                                final DataUpdateType updatetype,
                                final int displaylimit)
        {
        super(task,
              hostinstrument,
              name,
              resourcekey,
              updatetype,
              displaylimit,
              NON_REFRESHABLE,
              REFRESH_NONE);

        // Indicate the capabilities of the Chart
        setLinearMode(false);
        setLogarithmicMode(false);
        setCanAutorange(false);

        setMetadata(metadatalist,
                    null,
                    false,
                    LOADER_PROPERTIES.isMetadataDebug());
        }


    /***********************************************************************************************
     * Get the optional JToolBar.
     * Default is NULL, override if needed.
     *
     * @return JToolBar
     */

    public JToolBar getToolBar()
        {
        return (ChartUIHelper.createDefaultToolbar(getHostInstrument(),
                                                   this,
                                                   getChartName(),
                                                   getHostInstrument().getFontData(),
                                                   getHostInstrument().getColourData(),
                                                   DEFAULT_COLOUR_TAB_BACKGROUND,
                                                   isDebug()));
        }


    /***********************************************************************************************
     * Customise the XYPlot of a new chart, e.g. for fixed range axes.
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

    public JFreeChart createCustomisedChart(final DatasetType datasettype,
                                            final XYDataset primarydataset,
                                            final List<XYDataset> secondarydatasets,
                                            final DataUpdateType updatetype,
                                            final int displaylimit,
                                            final ChannelSelectorUIComponentInterface channelselector,
                                            final boolean debug)
        {
        final JFreeChart jFreeChart;

        // A plain Chart is an XYPlot
        // with a DateAxis for the x-axis (index 0) and a NumberAxis for the y-axis (index 0).
        // The default renderer is an XYLineAndShapeRenderer
        jFreeChart = ChartHelper.createChart(primarydataset,
                                             ObservatoryInstrumentHelper.getCurrentObservatoryTimeZone(REGISTRY.getFramework(),
                                                                                                       getDAO(),
                                                                                                       debug),
                                             getMetadata(),
                                             getChannelCount(),
                                             hasTemperatureChannel(),
                                             updatetype,
                                             displaylimit,
                                             channelselector,
                                             debug);

        // Customise the Chart for GOES data
        // Channels 0 & 1 are Data on a LogarithmicAxis,
        // Channel 2 is Ratio on a NumberAxis
        if (jFreeChart != null)
            {
            final String strLabelFlux;
            final String strLabelRatio;
            final XYPlot plot;
            final LogarithmicAxis axisFlux;
            final DateAxis axisDate;

            // The set of Metadata available should include the Instrument
            // and any items from the current observation
            strLabelFlux = MetadataHelper.getMetadataValueByKey(getMetadata(), MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO);
            strLabelRatio = MetadataHelper.getMetadataValueByKey(getMetadata(), MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ONE);

            plot = jFreeChart.getXYPlot();

            //----------------------------------------------------------------------------------
            // Replace the RangeAxis at index 0 NumberAxis with a LogarithmicAxis
            // The RangeAxis at index 0 is the LogarithmicAxis, to be used by Channels 0 & 1 (Data)

            axisFlux = new LogarithmicAxis(strLabelFlux);
            axisFlux.setRange(1.0E-09, 1.0E-02);
            axisFlux.setAllowNegativesFlag(false);
            axisFlux.setLog10TickLabelsFlag(true);
            plot.setRangeAxis(0, axisFlux);

            // Map the dataset to the axis
            plot.setDataset(INDEX_FLUX, primarydataset);
            plot.mapDatasetToRangeAxis(0, 0);
            plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);

            //----------------------------------------------------------------------------------
            // Customise the DomainAxis at index 0

            axisDate = (DateAxis) plot.getDomainAxis();

            // Showing the YYYY-MM-DD makes a very long label...
            // ToDo Consider ThreadLocal
            axisDate.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

            // Now customise the Flux renderer to improve legend visibility
            // Use the same colours as on http://www.swpc.noaa.gov/
            // blue=0.5 - 4.0A red=1.0 - 8.0A
            ChartUIHelper.customisePlotRenderer(plot, INDEX_FLUX);

            //----------------------------------------------------------------------------------
            // Set the RangeAxis at index 1 to a new NumberAxis, to be used by Channel 2 (Ratio)

            if ((secondarydatasets != null)
                && (secondarydatasets.size() == 1))
                {
                final NumberAxis axisRatio;
                final XYLineAndShapeRenderer rendererRatio;

                axisRatio = new NumberAxis(strLabelRatio);
                plot.setRangeAxis(1, axisRatio);

                // The RangeAxis at index 1 is the NumberAxis, to be used by Channel 2
                plot.setDataset(INDEX_RATIO, secondarydatasets.get(0));
                plot.mapDatasetToRangeAxis(1, 1);
                plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);

                rendererRatio = new XYLineAndShapeRenderer();
                rendererRatio.setLinesVisible(true);
                rendererRatio.setShapesVisible(false);
                // Channel 2 is Ratio
                rendererRatio.setSeriesPaint(0, ChartUIHelper.getStandardColour(2).getColor());
                rendererRatio.setLegendLine(SHAPE_LEGEND);

                plot.setRenderer(INDEX_RATIO, rendererRatio);
                //ChartHelper.customisePlotRenderer(plot, INDEX_RATIO);
                }
            }

        return (jFreeChart);
        }


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

    public void updateChartForSelection(final ObservatoryInstrumentDAOInterface dao,
                                        final DatasetType datasettype,
                                        final XYDataset primarydataset,
                                        final List<XYDataset> secondarydatasets,
                                        final int displaylimit,
                                        final int domainstartpoint,
                                        final int domainendpoint,
                                        final ChannelSelectorUIComponentInterface channelselector,
                                        final boolean debug)
        {
        LOGGER.warn("GOESChartUIComponent.updateChartForSelection() ToDo implement!");
        }



    //                if ((getHostInstrument() instanceof ObservatoryMonitor)
    //                    && (((ObservatoryMonitor)getHostInstrument()).isMemoryMonitorRunning()))

    //                if ((getPrimaryXYDataset() != null)
    //                    && (getSecondaryXYDatasets() != null)
    //                    && (getSecondaryXYDatasets().size() == 1))
    //                    {
    //                    // We have some data, but do we have a Chart?
    //                    if ((getChart() == null)
    //                        || (dao.isRawDataChanged()))
    //                        {
    //                        // We need to make a new Chart
    //                        }
    //                    else
    //                        {
    //                        // We already have a chart (on a panel),
    //                        // we just need to update the data it displays...
    //                        if ((getChart().getXYPlot() != null)
    //                            && (getPrimaryXYDataset() != null)
    //                            && (getPrimaryXYDataset().getSeriesCount() == 2)
    //                            && (getSecondaryXYDatasets() != null)
    //                            && (getSecondaryXYDatasets().size() == 1))


    /***********************************************************************************************
     * Set the XYDatasets for the Data and Ratio to be shown on the GOESChartUIComponent.
     * These data are produced by the DAO, and contain two channels plus ratio, i.e. Count = 3.
     *
     * @param dataset
     */

    public void setDatasetsAndMetadata(final XYDataset dataset)
        {
        if ((dataset != null)
            && (dataset instanceof TimeSeriesCollection)
            && (dataset.getSeriesCount() == DAO_CHANNEL_COUNT))
            {
            final TimeSeriesCollection seriesData;

            // Get the DAO's Metadata for use by this Chart
            if ((getHostInstrument() != null)
                && (getHostInstrument().getDAO() != null))
                {
                // The Metadata came from the Instrument DAO, so don't re-apply
                setMetadata(MetadataHelper.collectMetadataForChartFromDAO(getHostInstrument().getDAO()),
                            getHostInstrument().getDAO(),
                            false,
                            LOADER_PROPERTIES.isMetadataDebug());
                }
            else
                {
                setMetadata(null,
                            getHostInstrument().getDAO(),
                            false,
                            LOADER_PROPERTIES.isMetadataDebug());
                }

            // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
            setChannelCount(DAO_CHANNEL_COUNT);
            setTemperatureChannel(false);

            seriesData = new TimeSeriesCollection(((TimeSeriesCollection)dataset).getSeries(0));
            seriesData.addSeries(((TimeSeriesCollection)dataset).getSeries(1));

            // This sets the PrimaryDatasetType and RawDataChannelCount
            setPrimaryXYDataset(getHostInstrument().getDAO(), seriesData);

            // Do the secondary Ratio dataset only in this class
            getSecondaryXYDatasets().clear();
            getSecondaryXYDatasets().add(new TimeSeriesCollection(((TimeSeriesCollection)dataset).getSeries(2)));
            }
        else
            {
            // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
            setChannelCount(0);
            setTemperatureChannel(false);
            setPrimaryXYDataset(getHostInstrument().getDAO(), null);
            getSecondaryXYDatasets().clear();

            setMetadata(null,
                        getHostInstrument().getDAO(),
                        false,
                        LOADER_PROPERTIES.isMetadataDebug());
            }
        }
    }
