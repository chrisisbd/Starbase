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
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.text.SimpleDateFormat;
import java.util.List;


/***************************************************************************************************
 * The LogLinChartUIComponent.
 */

public class LogLinChartUIComponent extends ChartUIComponent
                                    implements ChartUIComponentPlugin
    {
    // Injections for Fixed Range limits
    private final double dblLinFixedMinY;
    private final double dblLinFixedMaxY;
    private final double dblLogFixedMinY;
    private final double dblLogFixedMaxY;


    /***********************************************************************************************
     * Construct a LogLinChartUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param name
     * @param metadatalist
     * @param resourcekey
     * @param updatetype
     * @param displaylimit
     * @param lin_fixed_min_y
     * @param lin_fixed_max_y
     * @param log_fixed_min_y
     * @param log_fixed_max_y
     */

    public LogLinChartUIComponent(final TaskPlugin task,
                                  final ObservatoryInstrumentInterface hostinstrument,
                                  final String name,
                                  final List<Metadata> metadatalist,
                                  final String resourcekey,
                                  final DataUpdateType updatetype,
                                  final int displaylimit,
                                  final double lin_fixed_min_y,
                                  final double lin_fixed_max_y,
                                  final double log_fixed_min_y,
                                  final double log_fixed_max_y)
        {
        super(task,
              hostinstrument,
              name,
              resourcekey,
              updatetype,
              displaylimit,
              NON_REFRESHABLE,
              REFRESH_NONE);

        // Injections for Fixed Range limits
        this.dblLinFixedMinY = lin_fixed_min_y;
        this.dblLinFixedMaxY = lin_fixed_max_y;
        this.dblLogFixedMinY = log_fixed_min_y;
        this.dblLogFixedMaxY = log_fixed_max_y;

        // Indicate the capabilities of the Chart
        setLinearMode(true);
        setLogarithmicMode(true);
        setCanAutorange(true);

        setMetadata(metadatalist,
                    null,
                    false,
                    LOADER_PROPERTIES.isMetadataDebug());
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
        final String SOURCE = "LogLinChartUIComponent.createCustomisedChart ";
        final JFreeChart jFreeChart;

        LOGGER.debug(debug,
                     SOURCE + "--> ChartHelper.createChart()");

        MetadataHelper.showMetadataList(getMetadata(),
                                        SOURCE + " CHART METADATA --> ChartHelper.createChart()",
                                        LOADER_PROPERTIES.isMetadataDebug());

        channelselector.debugSelector(debug, SOURCE);

        // Creates TimeSeriesChart or XYLineChart to suit the dataset
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
        if (jFreeChart != null)
            {
            // Customise the Chart for LogLin data if possible
            if ((hasLogarithmicMode())
                && (!channelselector.isLinearMode()))
                {
                final XYPlot plot;
                final LogarithmicAxis axisLog;
                final String strAxisLabel;

                LOGGER.debug(debug,
                             SOURCE + "Customise the Chart for LogLin data");

                // The set of Metadata available should include the Instrument
                // and any items from the current observation
                strAxisLabel = MetadataHelper.getMetadataValueByKey(getMetadata(),
                                                                    MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO);

                // Replace the RangeAxis at index 0 NumberAxis with a LogarithmicAxis
                axisLog = new LogarithmicAxis(strAxisLabel);

                axisLog.setAllowNegativesFlag(true);
                axisLog.setLog10TickLabelsFlag(true);

                if ((canAutorange())
                    && (channelselector.isAutoranging()))
                    {
                    axisLog.setAutoRange(true);
                    axisLog.configure();
                    axisLog.autoAdjustRange();
                    }
                else
                    {
                    axisLog.setRange(getLogarithmicFixedMinY(),
                                     getLogarithmicFixedMaxY());
                    axisLog.configure();
                    axisLog.autoAdjustRange();
                    }

                plot = jFreeChart.getXYPlot();
                plot.setRangeAxis(INDEX_AXIS, axisLog);
                plot.setRangeAxisLocation(INDEX_AXIS, AxisLocation.BOTTOM_OR_LEFT);

                // Map the dataset to the axis
                plot.setDataset(INDEX_DATA, primarydataset);
                plot.mapDatasetToRangeAxis(INDEX_DATA, INDEX_AXIS);

                // Change the DateAxis format
                if (DatasetType.TIMESTAMPED.equals(datasettype))
                    {
                    final DateAxis axisDate;

                    // Customise the DomainAxis at index 0
                    axisDate = (DateAxis) plot.getDomainAxis();

                    // Showing the YYYY-MM-DD makes a very long label...
                    axisDate.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
                    }

                // Now customise the data renderer to improve legend visibility
                ChartUIHelper.customisePlotRenderer(plot, 0);
                }
            else
                {
                final XYPlot plot;

                LOGGER.debug(debug,
                             SOURCE + "Customise the Chart for Linear data");

                // Linear Mode

                // A default range suitable for display of dB
                ChartHelper.handleAutorangeForLinearMode(jFreeChart,
                                                         channelselector,
                                                         canAutorange(),
                                                         getLinearFixedMinY(),
                                                         getLinearFixedMaxY(),
                                                         debug);

                // Now customise the data renderer to improve legend visibility
                plot = jFreeChart.getXYPlot();
                ChartUIHelper.customisePlotRenderer(plot, 0);
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "Chart is NULL");
            }

        return (jFreeChart);
        }


    /***********************************************************************************************
     * Get the minimum value of the Y axis for Linear Fixed Range.
     *
     * @return double
     */

    public double getLinearFixedMinY()
        {
        return (this.dblLinFixedMinY);
        }


    /***********************************************************************************************
     * Get the maximum value of the Y axis for Linear Fixed Range.
     *
     * @return double
     */

    public double getLinearFixedMaxY()
        {
        return (this.dblLinFixedMaxY);
        }


    /***********************************************************************************************
     * Get the minimum value of the Y axis for Logarithmic Fixed Range.
     *
     * @return double
     */

    public double getLogarithmicFixedMinY()
        {
        return (this.dblLogFixedMinY);
        }


    /***********************************************************************************************
     * Get the maximum value of the Y axis for Logarithmic Fixed Range.
     *
     * @return double
     */

    public double getLogarithmicFixedMaxY()
        {
        return (this.dblLogFixedMaxY);
        }
    }
