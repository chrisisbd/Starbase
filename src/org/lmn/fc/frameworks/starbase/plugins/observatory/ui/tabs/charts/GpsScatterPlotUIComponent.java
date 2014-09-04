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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * The GpsScatterPlotUIComponent.
 *
 * See: http://en.wikipedia.org/wiki/Latitude
 *      http://en.wikipedia.org/wiki/Longitude
 *
 * The WGS84 ellipsoid is used by all GPS devices
 *
 * One degree of latitude is approx 111132.954 - 559.822(Cos(2 phi)) + 1.175(Cos(4 phi)) in metres
 *
 * Approximation of a longitudinal degree at latitude phi is
 *
 *  (pi / 180) alpha Cos(beta)
 *
 *  where Earth's equatorial radius alpha equals 6,378,137 m
 *
 *  Tan(beta) = 0.99664719 Tan(phi)
 *
 *  beta is known as the parametric or reduced latitude
 *
 *  This is useful for checking the results of the fix:
 *  http://itouchmap.com/latlong.html
 *
 */

public final class GpsScatterPlotUIComponent extends LogLinChartUIComponent
    {
    /***********************************************************************************************
     * Construct a GpsScatterPlotUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param name
     * @param metadatalist
     * @param resourcekey
     */

    public GpsScatterPlotUIComponent(final TaskPlugin task,
                                     final ObservatoryInstrumentInterface hostinstrument,
                                     final String name,
                                     final List<Metadata> metadatalist,
                                     final String resourcekey)
        {
        super(task,
              hostinstrument,
              name,
              metadatalist,
              resourcekey,
              DataUpdateType.PRESERVE,
              10000,
              -179.99,
              179.99,
              -10.0,
              10.0);

        // Indicate the capabilities of the Chart
        setChannelSelector(false);
        setDatasetDomainControl(false);
        setLinearMode(false);
        setLogarithmicMode(false);
        setCanAutorange(true);

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
     * Remember that a GPS Scatter Plot has no ChannelSelector.
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
        final String SOURCE = "GpsScatterPlotUIComponent.createCustomisedChart() ";
        final JFreeChart jFreeChart;
        final XYPlot plot;
        final Stroke strokeCrosshair;
        final XYDotRenderer renderer;
        final ValueAxis axisRange;
        final NumberAxis axisDomain;

        // See ChartHelper for other calls to ChartFactory
        // Note that no ChannelSector means no way to control the legend, so turn it off
        jFreeChart = ChartFactory.createScatterPlot(MSG_WAITING_FOR_DATA,
                                                    MSG_WAITING_FOR_DATA,
                                                    MSG_WAITING_FOR_DATA,
                                                    primarydataset,
                                                    PlotOrientation.VERTICAL,
                                                    false, //channelselector.hasLegend(),
                                                    true,
                                                    false);

        jFreeChart.setBackgroundPaint(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Experimental chart configuration
        jFreeChart.getTitle().setFont(UIComponentPlugin.DEFAULT_FONT.getFont().deriveFont(20.0f));

        plot = (XYPlot) jFreeChart.getPlot();

        plot.setBackgroundPaint(ChartHelper.COLOR_PLOT);
        plot.setDomainGridlinePaint(ChartHelper.COLOR_GRIDLINES);
        plot.setRangeGridlinePaint(ChartHelper.COLOR_GRIDLINES);
        plot.setAxisOffset(ChartHelper.PLOT_RECTANGLE_INSETS);

        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);

        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(false);

        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairLockedOnData(true);

        // Make the Crosshair more visible by changing the width from the default
        strokeCrosshair = new BasicStroke(2.0f,                         // The width of this BasicStroke
                                          BasicStroke.CAP_BUTT,         // The decoration of the ends of a BasicStroke
                                          BasicStroke.JOIN_BEVEL,       // The decoration applied where path segments meet
                                          0.0f,                         // The limit to trim the miter join
                                          new float[] {2.0f, 2.0f},     // The array representing the dashing pattern
                                          0.0f);                        // The offset to start the dashing pattern
        plot.setDomainCrosshairStroke(strokeCrosshair);
        plot.setRangeCrosshairStroke(strokeCrosshair);

        renderer = new XYDotRenderer();
        renderer.setDotWidth(2);
        renderer.setDotHeight(2);
        plot.setRenderer(renderer);

        axisDomain = (NumberAxis) plot.getDomainAxis();
        axisRange = plot.getRangeAxis();

        // Remember that a GPS Scatter Plot has no ChannelSelector
        if (canAutorange())
            {
            // The fix could be anywhere...
            axisDomain.setAutoRangeIncludesZero(false);
            axisDomain.setAutoRange(true);
            axisRange.setAutoRange(true);
            }
        else
            {
            // Allow range to full global extents!
            axisDomain.setRange(getLinearFixedMinY(), getLinearFixedMaxY());
            axisRange.setRange(-90.0, 90.0);
            }

        return (jFreeChart);
        }


    /***********************************************************************************************
     * Update the existing Chart referenced by the DAO, by applying the specified datasets.
     * No Channel selection or DatasetDomain adjustment is allowed,
     * so just re-apply the original dataset.
     * This Chart does not use secondary datasets (yet).
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
        final String SOURCE = "GpsScatterPlotUIComponent.updateChartForSelection() ";

        LOGGER.debug(debug, SOURCE);

        if ((dao != null)
            && (dao.getChartUI() != null)
            && (dao.getChartUI().getChartPanel() != null)
            && (dao.getChartUI().getChartPanel().getChart() != null)
            && (dao.getChartUI().getChartPanel().getChart().getXYPlot() != null)
            && (datasettype != null)
            && (primarydataset != null))
            {
            // Confirm the DatasetType
            if ((datasettype.getName().equals(DatasetType.XY.getName()))
                && (primarydataset instanceof XYSeriesCollection))
                {
                final XYSeriesCollection collectionPrimary;

                // There should be a collection of <channelcount> XYSeries in the Primary Dataset
                collectionPrimary = (XYSeriesCollection) primarydataset;

                if ((collectionPrimary.getSeriesCount() > 0)
                    && (collectionPrimary.getSeries() != null))
                    {
                    LOGGER.debug(debug,
                                 SOURCE + "Update the data shown on existing Chart");

                    // No Channel selection or DatasetDomain adjustment is allowed,
                    // so just re-apply the original dataset
                    // This Chart does not use secondary datasets (yet)
                    dao.getChartUI().getChartPanel().getChart().getXYPlot().setDataset(INDEX_DATA, collectionPrimary);
                    updateCentroidCrosshairs();
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The XYSeriesCollection does not have any XYSeries");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + " The Dataset is of an invalid type");
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + " Unable to change the Chart - invalid parameters");
            }
        }


    /***********************************************************************************************
     * Update the Centroid Crosshairs, provided that ObservatoryMetadata contains valid
     * Observation.Centroid.Longitude and Observation.Centroid.Latitude.
     */

    private void updateCentroidCrosshairs()
        {
        final String SOURCE = "GpsScatterPlotUIComponent.updateCentroidCrosshairs() ";

        if ((getHostInstrument() != null)
            && (getHostInstrument().getDAO() != null)
            && (getHostInstrument().getDAO().getChartUI() != null)
            && (getHostInstrument().getDAO().getChartUI().getChartPanel() != null)
            && (getHostInstrument().getDAO().getChartUI().getChartPanel().getChart() != null)
            && (getHostInstrument().getDAO().getObservationMetadata() != null))
            {
            final Metadata metadataLongitude;
            final Metadata metadataLatitude;
            DegMinSecInterface dmsCentroidLongitude;
            DegMinSecInterface dmsCentroidLatitude;
            final List<String> errors;

            dmsCentroidLongitude = null;
            dmsCentroidLatitude = null;
            errors = new ArrayList<String>(10);

            // Get the centroid of the fixes from the Observation Metadata
            metadataLongitude = MetadataHelper.getMetadataByKey(getHostInstrument().getDAO().getObservationMetadata(),
                                                                MetadataDictionary.KEY_OBSERVATION_CENTROID_LONGITUDE.getKey());
            metadataLatitude = MetadataHelper.getMetadataByKey(getHostInstrument().getDAO().getObservationMetadata(),
                                                               MetadataDictionary.KEY_OBSERVATION_CENTROID_LATITUDE.getKey());

            if (metadataLongitude != null)
                {
                dmsCentroidLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                       DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                       EMPTY_STRING,
                                                                                                       EMPTY_STRING,
                                                                                                       errors);
                }

            if (metadataLatitude != null)
                {
                dmsCentroidLatitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                                     DataTypeDictionary.LATITUDE,
                                                                                                     EMPTY_STRING,
                                                                                                     EMPTY_STRING,
                                                                                                     errors);
                }

            if ((errors.isEmpty())
                && (dmsCentroidLongitude != null)
                && (dmsCentroidLatitude != null))
                {
                final XYPlot plot;

                plot = (XYPlot) getHostInstrument().getDAO().getChartUI().getChartPanel().getChart().getPlot();

                if (plot != null)
                    {
                    // Remember that the Domain is the X-axis, i.e. Longitude
                    plot.setDomainCrosshairValue(dmsCentroidLongitude.toDouble());
                    plot.setRangeCrosshairValue(dmsCentroidLatitude.toDouble());
                    }
                }

            LOGGER.errors(SOURCE, errors);
            }
        }
    }
