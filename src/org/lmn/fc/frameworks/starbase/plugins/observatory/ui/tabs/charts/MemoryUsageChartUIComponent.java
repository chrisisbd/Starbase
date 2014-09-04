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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.*;
import java.util.List;


/***************************************************************************************************
 * MemoryUsageChartUIComponent.
 */

public final class MemoryUsageChartUIComponent extends LogLinChartUIComponent
    {
    /***********************************************************************************************
     * Construct a MemoryUsageChartUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param name
     * @param metadatalist
     * @param resourcekey
     * @param updatetype
     * @param displaylimit
     */

    public MemoryUsageChartUIComponent(final TaskPlugin task,
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
              metadatalist,
              resourcekey,
              updatetype,
              displaylimit,
              0.0,
              100.0,
              0.0,
              0.0);

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

        // Customise the Chart for MemoryUsage
        if (jFreeChart != null)
            {
            final XYPlot plot;

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

        return (jFreeChart);
        }
    }
