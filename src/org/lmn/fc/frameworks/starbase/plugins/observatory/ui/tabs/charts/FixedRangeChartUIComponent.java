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
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.*;
import java.util.List;


/***************************************************************************************************
 * The FixedRangeChartUIComponent.
 */

public class FixedRangeChartUIComponent extends ChartUIComponent
                                        implements ChartUIComponentPlugin
    {
    // Fix the Y-range
    private final double dblMinValueY;
    private final double dblMaxValueY;


    /***********************************************************************************************
     * Construct a FixedRangeChartUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param name
     * @param metadatalist
     * @param resourcekey
     * @param updatetype
     * @param displaylimit
     * @param minvaluey
     * @param maxvaluey
     */

    public FixedRangeChartUIComponent(final TaskPlugin task,
                                      final ObservatoryInstrumentInterface hostinstrument,
                                      final String name,
                                      final List<Metadata> metadatalist,
                                      final String resourcekey,
                                      final DataUpdateType updatetype,
                                      final int displaylimit,
                                      final double minvaluey,
                                      final double maxvaluey)
        {
        super(task,
              hostinstrument,
              name,
              resourcekey,
              updatetype,
              displaylimit,
              NON_REFRESHABLE,
              REFRESH_NONE);

        this.dblMinValueY = minvaluey;
        this.dblMaxValueY = maxvaluey;

        // Indicate the capabilities of the Chart
        setLinearMode(true);
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

        // Creates TimeSeriesChart or XYLineChart to suit the dataset
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

        // Customise the Chart for fixed Y-scale
        if (jFreeChart != null)
            {
            final XYPlot plot;

            ChartHelper.handleAutorangeForLinearMode(jFreeChart,
                                                     channelselector,
                                                     canAutorange(),
                                                     dblMinValueY,
                                                     dblMaxValueY,
                                                     debug);

            // Now customise the data renderer to improve legend visibility
            plot = jFreeChart.getXYPlot();
            ChartUIHelper.customisePlotRenderer(plot, 0);
            }

        return (jFreeChart);
        }
    }
