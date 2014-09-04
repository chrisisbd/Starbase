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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.Series;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectionMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import java.awt.*;
import java.sql.Date;
import java.util.*;
import java.util.List;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface.INDEX_LEFT;
import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface.INDEX_RIGHT;


/***********************************************************************************************
 * ChartHelper.
 */

public final class ChartHelper implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ObservatoryConstants
    {
    // String Resources
    private static final String MSG_UNSUPPORTED_UPDATE_TYPE = "Unsupported Chart Update Type";

    public static final Color COLOR_GRIDLINES = new Color(170, 170, 170);
    public static final Color COLOR_PLOT = new Color(244, 249, 229);
    public static final RectangleInsets PLOT_RECTANGLE_INSETS = new RectangleInsets(UnitType.ABSOLUTE, 5.0, 5.0, 5.0, 5.0);


    /**********************************************************************************************
     * Create or refresh the JFreeChart, called from ChartUIComponent.refreshChart().
     * If the Chart does not exist, create it.
     * If the Chart *structure* has changed, recreate it using the new configuration.
     * Return any new JFreeChart.
     * If only the data have changed, update the existing Chart and return NULL.
     * Do not affect the UI.
     * Assume chartui is NOT NULL.
     *
     * @param obsinstrument
     * @param chartui
     * @param dao
     * @param generateflag
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param updatetype
     * @param isrefreshable
     * @param isclickrefresh
     * @param displaylimit
     * @param domainstartpoint
     * @param domainendpoint
     * @param channelselector
     * @param debug
     *
     * @return JFreeChart
     */

    public static JFreeChart createOrUpdateChart(final ObservatoryInstrumentInterface obsinstrument,
                                                 final ChartUIComponentPlugin chartui,
                                                 final ObservatoryInstrumentDAOInterface dao,
                                                 final boolean generateflag,
                                                 final DatasetType datasettype,
                                                 final XYDataset primarydataset,
                                                 final List<XYDataset> secondarydatasets,
                                                 final DataUpdateType updatetype,
                                                 final boolean isrefreshable,
                                                 final boolean isclickrefresh,
                                                 final int displaylimit,
                                                 final int domainstartpoint,
                                                 final int domainendpoint,
                                                 final ChannelSelectorUIComponentInterface channelselector,
                                                 final boolean debug)
        {
        final String SOURCE = "ChartHelper.createOrUpdateChart() ";
        final JFreeChart jFreeChart;

        //-----------------------------------------------------------------------------------------
        // DEBUGs only!

        if (dao != null)
            {
            LOGGER.debug(debug,
                         SOURCE + "DAO [classname=" + dao.getClass().getName() + "] [name=" + dao.getInstrumentName() + "]");

            // This debug ASSUMES the Instrument is not NULL
            try
                {
                LOGGER.debug(debug,
                             SOURCE + " [isselectedinstrument=" + ObservatoryUIHelper.isSelectedInstrument(obsinstrument)
                                 + "] [isinstrumenton=" + obsinstrument.getInstrumentState().isOn()
                                 + "] [visible=" + UIComponentHelper.isUIComponentShowing(chartui)
                                 + "] [generateflag=" + generateflag
                                 + "] [dao.unsaved_data=" + dao.hasUnsavedData()
                                 + "] [dao.datasettype.changed=" + dao.isDatasetTypeChanged()
                                 + "] [dao.channel.count.changed=" + dao.isChannelCountChanged()
                                 + "] [dao.channel.count.raw=" + dao.getRawDataChannelCount()
                                 + "] [dao.channel.count.xy=" + dao.getXYDataset().getSeriesCount()
                                 + "] [dao.metadata.changed=" + dao.isMetadataChanged()
                                 + "] [dao.raw_data.changed=" + dao.isRawDataChanged()
                                 + "] [dao.processed_data.changed=" + dao.isProcessedDataChanged()
                                 + "] [chartui.channel_selection.changed=" + chartui.isChannelSelectionChanged()
                                 + "] [chartui.domain.changed=" + chartui.isDatasetDomainChanged()
                                 + "] [domain.start=" + domainstartpoint
                                 + "] [domain.end=" + domainendpoint
                                 + "] [dataset.notnull=" + (primarydataset != null)
                                 + "]");
                }

            catch (final NullPointerException exception)
                {
                // Fail silently
                }
            }
        else
            {
            // This debug ASSUMES the Instrument is not NULL
            LOGGER.debug(debug,
                         SOURCE + " [isselectedinstrument=" + ObservatoryUIHelper.isSelectedInstrument(obsinstrument)
                             + "] [isinstrumenton=" + obsinstrument.getInstrumentState().isOn()
                             + "] [visible=" + UIComponentHelper.isUIComponentShowing(chartui)
                             + "] [dao=null"
                             + "] [generateflag=" + generateflag
                             + "] [chartui.channel_selection.changed=" + chartui.isChannelSelectionChanged()
                             + "] [chartui.domain_changed=" + chartui.isDatasetDomainChanged()
                             + "] [domain.start=" + domainstartpoint
                             + "] [domain.end=" + domainendpoint
                             + "] [dataset.notnull=" + (primarydataset != null)
                             + "]");
            }

        //-----------------------------------------------------------------------------------------

        // Only attempt to generate a Chart if this UIComponent is visible, and we have some data
        if ((obsinstrument.getInstrumentState().isOn())
            && (generateflag)
            && (dao != null)
            && (primarydataset != null))
            {
            LOGGER.debug(debug,
                         SOURCE + "Chart Tab is VISIBLE, checking to see if REBUILD or UPDATE required");

            // Reasons for complete rebuild
            //
            //  DatasetTypeChanged
            //  ChannelCountChanged
            //  ChannelSelectionChanged
            //  DatasetDomainChanged
            //
            // Reasons to leave Chart alone, just update the data
            //
            //  MetadataChanged
            //  RawDataChanged
            //  ProcessedDataChanged

            if ((dao.isDatasetTypeChanged())
                || (dao.isChannelCountChanged())
                || (chartui.isChannelSelectionChanged())
                || (chartui.isDatasetDomainChanged()))
                {
                // We need to make a new Chart
                LOGGER.debug(debug,
                             SOURCE + "ACTION No existing Chart --> createChartForSelection()"
                                    + "  [dataset_type.changed=" + dao.isDatasetTypeChanged()
                                    + "] [channel_count.changed=" + dao.isChannelCountChanged()
                                    + "] [channel_selection.changed=" + chartui.isChannelSelectionChanged()
                                    + "] [dataset_domain.changed=" + chartui.isDatasetDomainChanged());

                // This calls chartui.createCustomisedChart()
                jFreeChart = createChartForSelection(chartui,
                                                     dao,
                                                     datasettype,
                                                     primarydataset,
                                                     secondarydatasets,
                                                     updatetype,
                                                     isrefreshable,
                                                     isclickrefresh,
                                                     displaylimit,
                                                     domainstartpoint,
                                                     domainendpoint,
                                                     channelselector,
                                                     debug);
                // Any of these may have triggered this path
                dao.setDatasetTypeChanged(false);
                dao.setChannelCountChanged(false);
                chartui.setChannelSelectionChanged(false);
                chartui.setDatasetDomainChanged(false);
                }
            else if (dao.isMetadataChanged())
                {
                // We already have a chart (on a panel),
                // we just need to update the metadata it displays...
                if ((chartui.getChartPanel() != null)
                    && (chartui.getChartPanel().getChart() != null)
                    && (chartui.getChartPanel().getChart().getXYPlot() != null))
                    {
                    LOGGER.debug(debug,
                                 SOURCE + "ACTION Metadata changed, update Chart");

                    // Update chart metadata
                    updateChartFromDAOMetadata(chartui,
                                               dao,
                                               channelselector,
                                               debug);

                    chartui.updateChartForSelection(dao,
                                                    datasettype,
                                                    primarydataset,
                                                    secondarydatasets,
                                                    displaylimit,
                                                    domainstartpoint,
                                                    domainendpoint,
                                                    channelselector,
                                                    debug);
                    dao.setMetadataChanged(false);

                    jFreeChart = chartui.getChartPanel().getChart();
                    }
                else
                    {
                    // There's no data, so we mustn't have a Chart either
                    LOGGER.debug(debug,
                                 SOURCE + "ACTION No Chart to display, no action taken");

                    // Do NOT reset any flags, since a Chart may appear later?

                    // Ensure that no action is taken in the SwingWorker finished()
                    jFreeChart = null;
                    }
                }
            else if ((dao.isRawDataChanged())
                || (dao.isProcessedDataChanged()))
                {
                // We already have a chart (on a panel),
                // we just need to update the data it displays...
                if ((chartui.getChartPanel() != null)
                    && (chartui.getChartPanel().getChart() != null)
                    && (chartui.getChartPanel().getChart().getXYPlot() != null))
                    {
                    LOGGER.debug(debug,
                                 SOURCE + "ACTION Existing Chart given changed data --> updateChartForSelection()");

                    chartui.updateChartForSelection(dao,
                                                    datasettype,
                                                    primarydataset,
                                                    secondarydatasets,
                                                    displaylimit,
                                                    domainstartpoint,
                                                    domainendpoint,
                                                    channelselector,
                                                    debug);
                    // Any of these may have triggered this path
                    dao.setRawDataChanged(false);
                    dao.setProcessedDataChanged(false);

                    jFreeChart = chartui.getChartPanel().getChart();
                    }
                else
                    {
                    // There's no data, so we mustn't have a Chart either
                    LOGGER.debug(debug,
                                 SOURCE + "ACTION No XYPlot to display, no action taken");

                    // Do NOT reset any flags, since a Chart may appear later?

                    // Ensure that no action is taken in the SwingWorker finished()
                    jFreeChart = null;
                    }
                }
            else
                {
                LOGGER.debug(debug,
                             SOURCE + "ACTION No changes found, no action taken");

                // Do NOT reset any flags, since none were true

                // No need to update, so return NULL
                jFreeChart = null;
                }
            }
        else
            {
            // This debug ASSUMES the Instrument is not NULL
            LOGGER.debug(debug,
                         SOURCE + "ACTION No action taken"
                             + "  [isselectedinstrument=" + ObservatoryUIHelper.isSelectedInstrument(obsinstrument)
                             + "] [isinstrumenton=" + obsinstrument.getInstrumentState().isOn()
                             + "] [dao.notnull=" + (dao != null)
                             + "] [generateflag=" + generateflag
                             + "] [dataset.notnull=" + (primarydataset != null)
                             + "] [showing=" + UIComponentHelper.isUIComponentShowing(chartui)
                             + "]");

            // Do NOT reset any flags, since none were tested

            // No need to update, so return NULL
            jFreeChart = null;
            }

        return (jFreeChart);
        }


    /***********************************************************************************************
     * Update those items on the Chart which are taken from the DAO Metadata,
     * i.e.
     *      Title
     *      Axis.X
     *      Axis.Y.0
     *      Series Keys (from Channel.Name)
     *      Channel.Colour
     *
     * @param chartui
     * @param dao
     * @param channelselector
     * @param debug
     */

    private static void updateChartFromDAOMetadata(final ChartUIComponentPlugin chartui,
                                                   final ObservatoryInstrumentDAOInterface dao,
                                                   final ChannelSelectorUIComponentInterface channelselector,
                                                   final boolean debug)
        {
        final String SOURCE = "ChartHelper.updateChartFromDAOMetadata() ";

        if ((chartui != null)
            && (chartui.getChartPanel() != null)
            && (chartui.getChartPanel().getChart() != null)
            && (dao != null)
            && (dao.getXYDataset() != null)
            && (dao.getObservationMetadata() != null)
            && (channelselector != null))
            {
            // The set of Metadata available should include the Instrument
            // and any items from the current observation
            MetadataHelper.showDAOMetadata(dao, SOURCE, debug);

            if (chartui.getChartPanel().getChart().getTitle() != null)
                {
                final String strTitle;

                // Update Title
                strTitle = MetadataHelper.getMetadataValueByKey(dao.getObservationMetadata(), MetadataDictionary.KEY_OBSERVATION_TITLE.getKey());
                chartui.getChartPanel().getChart().getTitle().setText(strTitle);

                LOGGER.debug(debug, SOURCE + "Set new Chart Title [title=" + strTitle + "]");
                }
            else
                {
                LOGGER.error(SOURCE + "Chart Title is NULL");
                }

            if (chartui.getChartPanel().getChart().getXYPlot() != null)
                {
                final String strLabelX;
                final String strLabelY;
                final XYItemRenderer renderer;

                // Update Axis labels
                strLabelX = MetadataHelper.getMetadataValueByKey(dao.getObservationMetadata(), MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey());
                // Axis.Y.0 only in this version
                strLabelY = MetadataHelper.getMetadataValueByKey(dao.getObservationMetadata(), MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO);

                // Assume the axes are not NULL
                chartui.getChartPanel().getChart().getXYPlot().getDomainAxis().setLabel(strLabelX);
                chartui.getChartPanel().getChart().getXYPlot().getRangeAxis().setLabel(strLabelY);

                //---------------------------------------------------------------------------------
                // Re-synchronise the ChannelNames with the Series Keys, in case of edits

                if ((DatasetType.XY.equals(chartui.getDatasetType()))
                    && (dao.getXYDataset() instanceof XYSeriesCollection))
                    {
                    try
                        {
                        final List<XYSeries> listXYSeries;

                        // JFreeChart doesn't do Generics!
                        listXYSeries = ((XYSeriesCollection)dao.getXYDataset()).getSeries();

                        for (int intSeriesIndex = 0;
                             intSeriesIndex < listXYSeries.size();
                             intSeriesIndex++)
                            {
                            final XYSeries xySeries;
                            final String strChannelName;

                            xySeries = listXYSeries.get(intSeriesIndex);

                            // Get the channel names from the ObservationMetadata
                            strChannelName = MetadataHelper.getChannelName(dao.getObservationMetadata(),
                                                                           intSeriesIndex,
                                                                           dao.hasTemperatureChannel());

                            // Set the Series Key to be the same as the Channel.Name
                            // This will appear on the Chart Legend
                            LOGGER.debug(debug,
                                         SOURCE + "Set XY Series Key [series.index=" + intSeriesIndex + "] [key=" + strChannelName + "]");
                            xySeries.setKey(strChannelName);
                            }
                        }

                    catch (final UnknownKeyException exception)
                        {
                        LOGGER.error(SOURCE + "XYSeries has an unknown key");
                        exception.printStackTrace();
                        }
                    }
                else if ((DatasetType.TIMESTAMPED.equals(chartui.getDatasetType()))
                         && (dao.getXYDataset() instanceof TimeSeriesCollection))
                    {
                    try
                        {
                        final List<TimeSeries> listTimeSeries;

                        // JFreeChart doesn't do Generics!
                        listTimeSeries = ((TimeSeriesCollection)dao.getXYDataset()).getSeries();

                        for (int intSeriesIndex = 0;
                             intSeriesIndex < listTimeSeries.size();
                             intSeriesIndex++)
                            {
                            final TimeSeries timeSeries;
                            final String strChannelName;

                            timeSeries = listTimeSeries.get(intSeriesIndex);

                            // Get the channel names from the ObservationMetadata
                            strChannelName = MetadataHelper.getChannelName(dao.getObservationMetadata(),
                                                                           intSeriesIndex,
                                                                           dao.hasTemperatureChannel());

                            // Set the Series Key to be the same as the Channel.Name
                            // This will appear on the Chart Legend
                            LOGGER.debug(debug,
                                         SOURCE + "Set TIMESTAMPED Series Key [series.index=" + intSeriesIndex + "] [key=" + strChannelName + "]");
                            timeSeries.setKey(strChannelName);
                            }
                        }

                    catch (final UnknownKeyException exception)
                        {
                        LOGGER.error(SOURCE + "TimeSeries has an unknown key");
                        exception.printStackTrace();
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Unexpected Chart DatasetType [type=" + chartui.getDatasetType() + "]");
                    }

                //---------------------------------------------------------------------------------
                // Update ChannelColours
                renderer = chartui.getChartPanel().getChart().getXYPlot().getRenderer();

                if ((renderer != null)
                    && (renderer instanceof XYLineAndShapeRenderer))
                    {
                    final XYLineAndShapeRenderer xyItemRenderer;
                    int intSeriesCount;

                    xyItemRenderer = (XYLineAndShapeRenderer) renderer;

                    // Colour only as many Series as we know about
                    intSeriesCount = 0;

                    // Examine the state of each channel's selection
                    // Chart updates occur last, so we can use any changes in the ChannelSelector state
                    for (int intChannelIndex = 0;
                         intChannelIndex < channelselector.getChannelCount();
                         intChannelIndex++)
                        {
                        final ChannelSelectionMode selectionMode;

                        if ((channelselector.getChannelSelectionModes() != null)
                            && (channelselector.getChannelSelectionModes().size() == channelselector.getChannelCount()))
                            {
                            // If there is a ChannelSelector, get the gain setting, or OFF
                            selectionMode = channelselector.getChannelSelectionModes().get(intChannelIndex);
                            }
                        else if (!channelselector.showChannels())
                            {
                            // If there is no ChannelSelector, we assume that all Channels are ON, at X1
                            selectionMode = ChannelSelectionMode.X1;
                            }
                        else
                            {
                            // Not sure what to do, so just show all
                            selectionMode = ChannelSelectionMode.X1;
                            }

                        // Colour all visible channels
                        if (!ChannelSelectionMode.OFF.equals(selectionMode))
                            {
                            final ColourInterface colour;

                            colour = MetadataHelper.getChannelColour(dao.getObservationMetadata(),
                                                                     intChannelIndex,
                                                                     channelselector.hasTemperatureChannel());
                            if (colour != null)
                                {
                                // Map the Colour to the Series
                                xyItemRenderer.setSeriesPaint(intSeriesCount, colour.getColor());
                                }
                            else
                                {
                                // Use the default colour if the metadata doesn't have it
                                LOGGER.error(SOURCE + "The channel colour was missing or incorrectly specified in the Metadata, using default colour"
                                             + " [channel=" + intChannelIndex + "]");
                                xyItemRenderer.setSeriesPaint(intSeriesCount, ChartUIHelper.getStandardColour(intChannelIndex).getColor());
                                }

                            // Prepare for the next Series
                            intSeriesCount++;
                            }
                        }
                    }
                }

            chartui.getChartPanel().getChart().fireChartChanged();
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to update Chart");
            }
        }


    /***********************************************************************************************
     * Create a new Chart to show only those channels which are selected.
     * This must work even if the ChannelSelector is NULL.
     *
     * @param chartui
     * @param dao
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param updatetype
     * @param isrefreshable
     * @param isclickrefresh
     * @param displaylimit
     * @param domainstartpoint
     * @param domainendpoint
     * @param channelselector
     * @param debug
     *
     * @return JFreeChart
     */

    private static JFreeChart createChartForSelection(final ChartUIComponentPlugin chartui,
                                                      final ObservatoryInstrumentDAOInterface dao,
                                                      final DatasetType datasettype,
                                                      final XYDataset primarydataset,
                                                      final List<XYDataset> secondarydatasets,
                                                      final DataUpdateType updatetype,
                                                      final boolean isrefreshable,
                                                      final boolean isclickrefresh,
                                                      final int displaylimit,
                                                      final int domainstartpoint,
                                                      final int domainendpoint,
                                                      final ChannelSelectorUIComponentInterface channelselector,
                                                      final boolean debug)
        {
        final String SOURCE = "ChartHelper.createChartForSelection() ";
        final JFreeChart jFreeChart;

        LOGGER.debug(debug,
                     SOURCE);

        if ((datasettype != null)
            && (primarydataset != null)
            && (secondarydatasets != null)
            && (updatetype != null))
            {
            final XYDataset xyNewPrimaryDataset;
            final List<XYDataset> listNewSecondaryDatasets;
            final List<XYDataset> listParentSecondaryDatasetForSeries;
            final Iterator iterOriginalSecondaryDatasets;
            final Calendar calObservatory;

            // Create a list of NewSecondaryDatasets for display
            // Add an appropriate *empty* new dataset for every one existing in the secondary set
            // and record the parent new Dataset for each Series in each original Dataset
            // So much complexity just to handle the very few cases of secondary datasets...

            listNewSecondaryDatasets = new ArrayList<XYDataset>(10);
            listParentSecondaryDatasetForSeries = new ArrayList<XYDataset>(10);

            iterOriginalSecondaryDatasets = secondarydatasets.iterator();

            // Either find the Current Observatory calendar, or provide a default
            calObservatory = ObservatoryInstrumentHelper.getCurrentObservatoryCalendar(REGISTRY.getFramework(),
                                                                                       dao,
                                                                                       debug);
            while (iterOriginalSecondaryDatasets.hasNext())
                {
                final XYDataset xyDatasetOriginal;

                xyDatasetOriginal = (XYDataset) iterOriginalSecondaryDatasets.next();

                if ((xyDatasetOriginal != null)
                    && (xyDatasetOriginal.getSeriesCount() > 0))
                    {
                    final XYDataset xyDatasetNew;

                    // Create an empty Dataset to correspond with the original
                    if (datasettype.getName().equals(DatasetType.XY.getName()))
                        {
                        xyDatasetNew = new XYSeriesCollection();
                        listNewSecondaryDatasets.add(xyDatasetNew);
                        }
                    else if (datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                        {
                        xyDatasetNew = new TimeSeriesCollection(calObservatory.getTimeZone());
                        listNewSecondaryDatasets.add(xyDatasetNew);
                        }
                    else
                        {
                        xyDatasetNew = new XYSeriesCollection();
                        listNewSecondaryDatasets.add(xyDatasetNew);
                        }

                    // Record the *same* parent Dataset for each Series in each original Secondary Dataset
                    // This creates a List in channel order, but with references to Datasets not Series
                    for (int i = 0;
                         i < xyDatasetOriginal.getSeriesCount();
                         i++)
                        {
                        listParentSecondaryDatasetForSeries.add(xyDatasetNew);
                        }
                    }
                }

            LOGGER.debug(debug,
                         SOURCE + "Check the DatasetType");

            // Confirm the DatasetType
            if ((datasettype.getName().equals(DatasetType.XY.getName()))
                && (primarydataset instanceof XYSeriesCollection))
                {
                final XYSeriesCollection collectionPrimary;

                // Prepare a new XYSeriesCollection for display
                xyNewPrimaryDataset = new XYSeriesCollection();

                // There should be a collection of <channelcount> XYSeries in the Primary Dataset
                // but there may also be some in the Secondary Datasets
                collectionPrimary = (XYSeriesCollection) primarydataset;

                if ((collectionPrimary.getSeriesCount() > 0)
                    && (collectionPrimary.getSeries() != null))
                    {
                    final int intChannelCount;

                    if (channelselector != null)
                        {
                        channelselector.debugSelector(debug, SOURCE);

                        if ((channelselector.getChannelSelectionModes() != null)
                            && (channelselector.showChannels()))
                            {
                            intChannelCount = channelselector.getChannelSelectionModes().size();
                            LOGGER.debug(debug,
                                         SOURCE + "[channelcount.channelselector=" + intChannelCount + "]");
                            }
                        else if (dao != null)
                            {
                            intChannelCount = dao.getRawDataChannelCount();
                            LOGGER.debug(debug,
                                         SOURCE + "[channelcount.dao.raw=" + intChannelCount + "] (has channelselector?)");
                            }
                        else
                            {
                            intChannelCount = collectionPrimary.getSeriesCount();
                            LOGGER.debug(debug,
                                         SOURCE + "[channelcount.primary.series=" + intChannelCount + "]");
                            }
                        }
                    else if (dao != null)
                        {
                        intChannelCount = dao.getRawDataChannelCount();
                        LOGGER.debug(debug,
                                     SOURCE + "[channelcount.dao.raw" + intChannelCount + "] (no channelselector)");
                        }
                    else
                        {
                        // This should never happen!
                        intChannelCount = collectionPrimary.getSeriesCount();
                        LOGGER.debug(debug,
                                     SOURCE + "[channelcount.primary.series" + intChannelCount + "] (last resort)");
                        }

                    LOGGER.debug(debug,
                                 SOURCE + DatasetType.XY.getName()
                                     + " [domain.start.point=" + domainstartpoint
                                     + "] [domain.end.point=" + domainendpoint
                                     + "] [channelcount.inferred=" + intChannelCount
                                     + "]");

                    // Find which channels to use this time round
                    for (int intChannelIndex = 0;
                         intChannelIndex < intChannelCount;
                         intChannelIndex++)
                        {
                        final ChannelSelectionMode selectionMode;

                        // Use the ChannelSelectionMode if we can
                        if ((channelselector != null)
                            && (channelselector.getChannelSelectionModes() != null)
                            && (channelselector.showChannels()))
                            {
                            selectionMode = channelselector.getChannelSelectionModes().get(intChannelIndex);
                            }
                        else
                            {
                            // If there is no ChannelSelector then we can safely assume the Channel is ON
                            selectionMode = ChannelSelectionMode.X1;
                            }

                        if (!ChannelSelectionMode.OFF.equals(selectionMode))
                            {
                            final XYSeries seriesOriginalData;

                            seriesOriginalData = (XYSeries) getSeriesForIndex(datasettype,
                                                                              primarydataset,
                                                                              secondarydatasets,
                                                                              intChannelIndex,
                                                                              debug);
                            if (seriesOriginalData != null)
                                {
                                final XYSeries seriesChangedData;
                                final List listOriginalDataItems;
                                int intStartIndex;
                                int intEndIndex;

                                listOriginalDataItems = seriesOriginalData.getItems();

                                // Prepare a new Series for the changed data, with the same Key
                                seriesChangedData = new XYSeries(seriesOriginalData.getKey());

                                // Map the slider values to data indexes
                                intStartIndex = transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                        ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                        domainstartpoint,
                                                                                        DatasetDomainUIComponentInterface.INDEX_LEFT,
                                                                                        collectionPrimary.getDomainLowerBound(true),
                                                                                        collectionPrimary.getDomainUpperBound(true),
                                                                                        DatasetType.XY,
                                                                                        calObservatory,
                                                                                        seriesOriginalData,
                                                                                        debug);

                                intEndIndex = transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                      ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                      domainendpoint,
                                                                                      DatasetDomainUIComponentInterface.INDEX_RIGHT,
                                                                                      collectionPrimary.getDomainLowerBound(true),
                                                                                      collectionPrimary.getDomainUpperBound(true),
                                                                                      DatasetType.XY,
                                                                                      calObservatory,
                                                                                      seriesOriginalData,
                                                                                      debug);
                                if ((intStartIndex == -1)
                                    || (intEndIndex == -1))
                                    {
                                    // If either index is returned as -1, then there's nothing to do...
                                    // ...so stop the for() loop
                                    intStartIndex = 0;
                                    intEndIndex = 0;
                                    }
                                else if (intEndIndex <= intStartIndex)
                                    {
                                    intEndIndex = intStartIndex + 1;
                                    }

                                LOGGER.debug(debug,
                                             SOURCE + "before copy of selected series subset [channel=" + intChannelIndex
                                                    + "] [index.start=" + intStartIndex
                                                    + "] [index.end=" + intEndIndex
                                                    + "] [show.ticks=" + ((intEndIndex - intStartIndex) <= 25) + "]");

                                // Copy over only the selected range from the Slider
                                for (int intDataIndex = intStartIndex;
                                     ((intDataIndex < intEndIndex)
                                        && (listOriginalDataItems.size() > 0));
                                     intDataIndex++)
                                    {
                                    final XYDataItem dataOriginalItem;
                                    final XYDataItem dataChangedItem;

                                    dataOriginalItem = (XYDataItem)listOriginalDataItems.get(intDataIndex);

                                    if (!ChannelSelectionMode.X1.equals(selectionMode))
                                        {
                                        // Change each value of the series according to the multiplier
                                        dataChangedItem = new XYDataItem(dataOriginalItem.getX(),
                                                                         dataOriginalItem.getY().doubleValue() *  selectionMode.getMultiplier());
                                        }
                                    else
                                        {
                                        // Just use the whole series unaltered for gain of X1
                                        dataChangedItem = new XYDataItem(dataOriginalItem.getX(),
                                                                         dataOriginalItem.getY());
                                        }

                                    seriesChangedData.add(dataChangedItem);
                                    }

                                // Place the changed series in the correct collection
                                // to correspond with the original

                                // Did we collect any data for this Series?
                                // If not, place a dummy point at the origin of the *visible* chart
                                if (seriesChangedData.getItemCount() == 0)
                                    {
                                    // TODO
                                    seriesChangedData.add(new XYDataItem(0,
                                                                         0));
                                    }

                                if (intChannelIndex < collectionPrimary.getSeriesCount())
                                    {
                                    // Simply add the changed Primary series to the PrimaryDataset collection
                                    ((XYSeriesCollection)xyNewPrimaryDataset).addSeries(seriesChangedData);
                                    }
                                else
                                    {
                                    // It must be a secondary dataset
                                    // Add the changed Secondary series to the parent SecondaryDataset
                                    // given by the *secondary* channel index
                                    addSecondarySeries(datasettype,
                                                       listParentSecondaryDatasetForSeries,
                                                       seriesChangedData,
                                                       intChannelIndex - collectionPrimary.getSeriesCount());
                                    }
                                }
                            else
                                {
                                LOGGER.warn(SOURCE + "OriginalData XYSeries unexpectedly NULL");
                                }
                            }
                        }

                    // Dump the (partial) contents of each Series in the composite XYdataset
                    dumpXYDataset(debug,
                                  calObservatory,
                                  xyNewPrimaryDataset,
                                  4,
                                  SOURCE + "XYSeriesCollection --> createCustomisedChart() xyNewPrimaryDataset");

                    jFreeChart = chartui.createCustomisedChart(datasettype,
                                                               xyNewPrimaryDataset,
                                                               listNewSecondaryDatasets,
                                                               updatetype,
                                                               displaylimit,
                                                               channelselector,
                                                               debug);
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The XYSeriesCollection does not have any XYSeries");
                    jFreeChart = null;
                    }
                }
            else if ((datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                && (primarydataset instanceof TimeSeriesCollection))
                {
                final TimeSeriesCollection collectionPrimary;

                // Prepare a new TimeSeriesCollection for display
                xyNewPrimaryDataset = new TimeSeriesCollection(calObservatory.getTimeZone());

                // There should be a collection of <channelcount> TimeSeries in the Primary Dataset
                // but there may also be some in the Secondary Datasets
                collectionPrimary = (TimeSeriesCollection) primarydataset;

                if ((collectionPrimary.getSeriesCount() > 0)
                    && (collectionPrimary.getSeries() != null))
                    {
                    final int intChannelCount;

                    if (channelselector != null)
                        {
                        channelselector.debugSelector(debug, SOURCE);

                        if ((channelselector.getChannelSelectionModes() != null)
                            && (channelselector.showChannels()))
                            {
                            intChannelCount = channelselector.getChannelSelectionModes().size();
                            LOGGER.debug(debug,
                                         SOURCE + "[channelcount.channelselector=" + intChannelCount + "]");
                            }
                        else if (dao != null)
                            {
                            intChannelCount = dao.getRawDataChannelCount();
                            LOGGER.debug(debug,
                                         SOURCE + "[channelcount.dao.raw=" + intChannelCount + "] (has channelselector)");
                            }
                        else
                            {
                            intChannelCount = collectionPrimary.getSeriesCount();
                            LOGGER.debug(debug,
                                         SOURCE + "[channelcount.primary.series=" + intChannelCount + "]");
                            }
                        }
                    else if (dao != null)
                        {
                        intChannelCount = dao.getRawDataChannelCount();
                        LOGGER.debug(debug,
                                     SOURCE + "[channelcount.dao.raw=" + intChannelCount + "] (no channelselector)");
                        }
                    else
                        {
                        // This should never happen!
                        intChannelCount = collectionPrimary.getSeriesCount();
                        LOGGER.debug(debug,
                                     SOURCE + "[channelcount.primary.series=" + intChannelCount + "] (last resort)");
                        }

                    LOGGER.debug(debug,
                                 SOURCE + DatasetType.TIMESTAMPED.getName()
                                     + " [domain.startpoint=" + domainstartpoint
                                     + "] [domain.endpoint=" + domainendpoint
                                     + "] [domain.lowerbound=" + (long)collectionPrimary.getDomainLowerBound(true)
                                     + "] [domain.upperbound=" + (long)collectionPrimary.getDomainUpperBound(true)
                                     + "] [channelcount.inferred=" + intChannelCount
                                     + "]");

                    // Find which channels to use this time round
                    for (int intChannelIndex = 0;
                         intChannelIndex < intChannelCount;
                         intChannelIndex++)
                        {
                        final ChannelSelectionMode selectionMode;

                        // Use the ChannelSelectionMode if we can
                        if ((channelselector != null)
                            && (channelselector.getChannelSelectionModes() != null)
                            && (channelselector.showChannels()))
                            {
                            selectionMode = channelselector.getChannelSelectionModes().get(intChannelIndex);
                            }
                        else
                            {
                            // If there is no ChannelSelector then we can safely assume the Channel is ON
                            selectionMode = ChannelSelectionMode.X1;
                            }

                        if (!ChannelSelectionMode.OFF.equals(selectionMode))
                            {
                            final TimeSeries seriesOriginalData;

                            seriesOriginalData = (TimeSeries) getSeriesForIndex(datasettype,
                                                                                primarydataset,
                                                                                secondarydatasets,
                                                                                intChannelIndex,
                                                                                debug);
                            if (seriesOriginalData != null)
                                {
                                final TimeSeries seriesChangedData;
                                final List listOriginalDataItems;
                                int intStartIndex;
                                int intEndIndex;

                                listOriginalDataItems = seriesOriginalData.getItems();

                                // Prepare a new Series for the changed data, with the same Key
                                seriesChangedData = new TimeSeries(seriesOriginalData.getKey().toString(),
                                                                   seriesOriginalData.getTimePeriodClass());

                                // Map the slider values to data indexes
                                intStartIndex = transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                        ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                        domainstartpoint,
                                                                                        DatasetDomainUIComponentInterface.INDEX_LEFT,
                                                                                        collectionPrimary.getDomainLowerBound(true),
                                                                                        collectionPrimary.getDomainUpperBound(true),
                                                                                        DatasetType.TIMESTAMPED,
                                                                                        calObservatory,
                                                                                        seriesOriginalData,
                                                                                        debug);

                                intEndIndex = transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                      ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                      domainendpoint,
                                                                                      DatasetDomainUIComponentInterface.INDEX_RIGHT,
                                                                                      collectionPrimary.getDomainLowerBound(true),
                                                                                      collectionPrimary.getDomainUpperBound(true),
                                                                                      DatasetType.TIMESTAMPED,
                                                                                      calObservatory,
                                                                                      seriesOriginalData,
                                                                                      debug);
                                if ((intStartIndex == -1)
                                    || (intEndIndex == -1))
                                    {
                                    // If either index is returned as -1, then there's nothing to do...
                                    // ...so stop the for() loop
                                    intStartIndex = 0;
                                    intEndIndex = 0;
                                    }
                                else if (intEndIndex <= intStartIndex)
                                    {
                                    intEndIndex = intStartIndex + 1;
                                    }

                                LOGGER.debug(debug,
                                             SOURCE + "before copy of selected series subset [channel=" + intChannelIndex
                                                    + "] [index.start=" + intStartIndex
                                                    + "] [index.end=" + intEndIndex
                                                    + "] [item.count=" + listOriginalDataItems.size()
                                                    + "]");

                                // Copy over only the selected range from the Slider
                                for (int intDataIndex = intStartIndex;
                                     ((intDataIndex < intEndIndex)
                                        && (intDataIndex < listOriginalDataItems.size()));
                                     intDataIndex++)
                                    {
                                    final TimeSeriesDataItem dataOriginalItem;
                                    final TimeSeriesDataItem dataChangedItem;

                                    dataOriginalItem = (TimeSeriesDataItem)listOriginalDataItems.get(intDataIndex);

                                    if (!ChannelSelectionMode.X1.equals(selectionMode))
                                        {
                                        // Change each value of the series according to the multiplier
                                        dataChangedItem = new TimeSeriesDataItem(dataOriginalItem.getPeriod(),
                                                                                 dataOriginalItem.getValue().doubleValue() * selectionMode.getMultiplier());
                                        }
                                    else
                                        {
                                        // Just use the whole series unaltered for gain of X1
                                        dataChangedItem = new TimeSeriesDataItem(dataOriginalItem.getPeriod(),
                                                                                 dataOriginalItem.getValue().doubleValue());
                                        }

                                    seriesChangedData.add(dataChangedItem);
                                    }

                                // Did we collect any data for this Series?
                                // If not, place a dummy point at the origin of the *visible* chart
                                if (seriesChangedData.getItemCount() == 0)
                                    {
                                    seriesChangedData.add(createDummyTimeSeriesDataItemAtOrigin(collectionPrimary,
                                                                                                seriesOriginalData,
                                                                                                domainstartpoint,
                                                                                                debug));
                                    }

                                // Place the changed series in the correct collection
                                // to correspond with the original
                                if (intChannelIndex < collectionPrimary.getSeriesCount())
                                    {
                                    // Simply add the changed Primary series to the PrimaryDataset collection
                                    ((TimeSeriesCollection)xyNewPrimaryDataset).addSeries(seriesChangedData);
                                    }
                                else
                                    {
                                    // It must be a secondary dataset
                                    // Add the changed Secondary series to the parent SecondaryDataset
                                    // given by the *secondary* channel index
                                    addSecondarySeries(datasettype,
                                                       listParentSecondaryDatasetForSeries,
                                                       seriesChangedData,
                                                       intChannelIndex - collectionPrimary.getSeriesCount());
                                    }
                                }
                            else
                                {
                                LOGGER.warn(SOURCE + "OriginalData TimeSeries unexpectedly NULL");
                                }
                            }
                        }

                    // Dump the (partial) contents of each Series in the composite XYdataset
                    dumpXYDataset(debug,
                                  calObservatory,
                                  xyNewPrimaryDataset,
                                  4,
                                  SOURCE + "TimeSeriesCollection --> createCustomisedChart() xyNewPrimaryDataset");

                    jFreeChart = chartui.createCustomisedChart(datasettype,
                                                               xyNewPrimaryDataset,
                                                               listNewSecondaryDatasets,
                                                               updatetype,
                                                               displaylimit,
                                                               channelselector,
                                                               debug);
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The TimeSeriesCollection does not have any TimeSeries");
                    jFreeChart = null;
                    }
                }
            else
                {
                LOGGER.error(SOURCE + " The Dataset is of an invalid type");
                jFreeChart = null;
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + " Unable to change the Chart - invalid parameters");
            jFreeChart = null;
            }

        return (jFreeChart);
        }


    /***********************************************************************************************
     * Create a simple Chart from the specified XYDataset.
     * Choose a TimeSeriesChart or a XYLineChart appropriately.
     * Called from createCustomisedChart() in each ChartUIComponent subclass.
     *
     *
     * @param dataset
     * @param timezone
     * @param metadatalist
     * @param channelcount
     * @param temperaturechannel
     * @param updatetype
     * @param displaylimit
     * @param channelselector
     * @param debug
     *
     * @return JFreeChart
     */

    public static JFreeChart createChart(final XYDataset dataset,
                                         final TimeZone timezone,
                                         final List<Metadata> metadatalist,
                                         final int channelcount,
                                         final boolean temperaturechannel,
                                         final DataUpdateType updatetype,
                                         final int displaylimit,
                                         final ChannelSelectorUIComponentInterface channelselector,
                                         final boolean debug)
        {
        final String SOURCE = "ChartHelper.createChart() ";
        final JFreeChart chart;
        final String strTitle;
        final String strLabelX;
        final String strLabelY;
        final XYDataset xyDatasetToDisplay;

        // The set of Metadata available should include the Instrument
        // and any items from the current observation
        strTitle = MetadataHelper.getMetadataValueByKey(metadatalist, MetadataDictionary.KEY_OBSERVATION_TITLE.getKey());
        strLabelX = MetadataHelper.getMetadataValueByKey(metadatalist, MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey());
        // Axis.Y.0 only in this version
        strLabelY = MetadataHelper.getMetadataValueByKey(metadatalist, MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO);

        // Transform the XYdataset in accordance with the current ChannelSelection Mode before displaying

        channelselector.debugSelector(debug, SOURCE);

        xyDatasetToDisplay = copyTransformedXYDataset(dataset,
                                                      timezone,
                                                      updatetype,
                                                      displaylimit,
                                                      channelselector.isDecimating(),
                                                      channelselector.getChannelSelectionModes());
        if (dataset instanceof TimeSeriesCollection)
            {
            chart = ChartFactory.createTimeSeriesChart(strTitle,
                                                       strLabelX,
                                                       strLabelY,
                                                       xyDatasetToDisplay,
                                                       channelselector.hasLegend(),
                                                       true,
                                                       false);
            }
        else
            {
            chart = ChartFactory.createXYLineChart(strTitle,
                                                   strLabelX,
                                                   strLabelY,
                                                   xyDatasetToDisplay,
                                                   PlotOrientation.VERTICAL,
                                                   channelselector.hasLegend(),
                                                   true,
                                                   false);
            }

        if ((chart != null)
            && (chart.getXYPlot() != null)
            && (chart.getXYPlot().getRangeAxis() != null))
            {
            final Stroke strokeCrosshair;
            final XYPlot plot;
            final XYItemRenderer renderer;

            chart.setBackgroundPaint(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

            // Experimental chart configuration
            chart.getTitle().setFont(UIComponentPlugin.DEFAULT_FONT.getFont().deriveFont(20.0f));

            plot = chart.getXYPlot();
            plot.setBackgroundPaint(COLOR_PLOT);
            plot.setDomainGridlinePaint(COLOR_GRIDLINES);
            plot.setRangeGridlinePaint(COLOR_GRIDLINES);
            plot.setAxisOffset(PLOT_RECTANGLE_INSETS);

            plot.setDomainCrosshairVisible(true);
            plot.setDomainCrosshairLockedOnData(false);
            plot.setRangeCrosshairVisible(false);

            // Make the Crosshair more visible by changing the width from the default
            strokeCrosshair = new BasicStroke(2.0f,                         // The width of this BasicStroke
                                              BasicStroke.CAP_BUTT,         // The decoration of the ends of a BasicStroke
                                              BasicStroke.JOIN_BEVEL,       // The decoration applied where path segments meet
                                              0.0f,                         // The limit to trim the miter join
                                              new float[] {2.0f, 2.0f},     // The array representing the dashing pattern
                                              0.0f);                        // The offset to start the dashing pattern
            plot.setDomainCrosshairStroke(strokeCrosshair);

            renderer = plot.getRenderer();

            if ((renderer != null)
                && (renderer instanceof XYLineAndShapeRenderer))
                {
                final XYLineAndShapeRenderer xyItemRenderer;
                int intSeriesCount;

                xyItemRenderer = (XYLineAndShapeRenderer) renderer;

                xyItemRenderer.setBaseLinesVisible(true);
                xyItemRenderer.setBaseShapesVisible(false);
                xyItemRenderer.setBaseShapesFilled(true);
                xyItemRenderer.setItemLabelsVisible(true);

                // Set the shape for the Chart legend items
                ChartUIHelper.setLegendShape(xyItemRenderer);

                // Colour only as many Series as we know about
                intSeriesCount = 0;

                // Examine the state of each channel's selection
                for (int channel = 0;
                     channel < channelcount;
                     channel++)
                    {
                    final ChannelSelectionMode selectionMode;

                    if ((channelselector.getChannelSelectionModes() != null)
                        && (channelselector.getChannelSelectionModes().size() == channelcount))
                        {
                        // If there is a ChannelSelector, get the gain setting, or OFF
                        selectionMode = channelselector.getChannelSelectionModes().get(channel);
                        }
                    else if (!channelselector.showChannels())
                        {
                        // If there is no ChannelSelector, we assume that all Channels are ON, at X1
                        selectionMode = ChannelSelectionMode.X1;
                        }
                    else
                        {
                        // Not sure what to do, so just show all
                        selectionMode = ChannelSelectionMode.X1;
                        }

                    if (!ChannelSelectionMode.OFF.equals(selectionMode))
                        {
                        final ColourInterface colour;

                        colour = MetadataHelper.getChannelColour(metadatalist, channel, temperaturechannel);

                        MetadataHelper.showMetadataList(metadatalist,
                                                        SOURCE + " COLOURS FOR CHART",
                                                        LOADER_PROPERTIES.isMetadataDebug());

                        if (colour != null)
                            {
                            // Map the Colour to the Series
                            xyItemRenderer.setSeriesPaint(intSeriesCount, colour.getColor());
                            }
                        else
                            {
                            // Use the default colour if the metadata doesn't have it
                            LOGGER.error(SOURCE + "The channel colour was missing or incorrectly specified in the Metadata, using default colour"
                                            + " [channel=" + channel + "]");
                            xyItemRenderer.setSeriesPaint(intSeriesCount, ChartUIHelper.getStandardColour(channel).getColor());
                            }

                        // Prepare for the next Series
                        intSeriesCount++;
                        }
                    }
                }
            }

        return (chart);
        }


    /***********************************************************************************************
     * Apply a ChartUI and its Metadata to the specified DAO.
     * Null ChartUI and Metadata are permitted.
     *
     * @param chartui
     * @param metadatalist
     * @param dao
     */

    public static void associateChartUIWithDAO(final ChartUIComponentPlugin chartui,
                                               final List<Metadata> metadatalist,
                                               final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "ChartHelper.associateChartUIWithDAO(dao) ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug());

        // Save the new Chart for use by the DAO (if any)
        if (dao != null)
            {
            dao.setChartUI(chartui);
            dao.addAllMetadataToContainersTraced(metadatalist,
                                                 SOURCE,
                                                 boolDebug);
            LOGGER.debug(boolDebug,
                         SOURCE + "finished");
            }
        }


    /***********************************************************************************************
     * Preserve, Truncate or Decimate an XYDataset to try to achieve the requested display limit.
     * Decimation requested overrides all other modes.
     * Transform the dataset in accordance with the ChannelSelection Modes.
     * TRUNCATE is not currently used.
     *
     * ChartUpdate      isdecimated     Outcome
     *
     *  PRESERVE            N           Always PRESERVE, don't use displaylimit
     *                      Y           ditto
     *
     *  DECIMATE            N           Do same as PRESERVE
     *                      Y           Skip enough to leave displaylimit
     *
     *  TRUNCATE            N           Use only displaylimit, ignore isdecimated
     *                      Y           ditto
     *
     *
     * @param dataset
     * @param timezone
     * @param chartupdatetype
     * @param displaylimit
     * @param isdecimated
     * @param selectionmodes
     *
     * @return XYDataset
     */

    private static XYDataset copyTransformedXYDataset(final XYDataset dataset,
                                                      final TimeZone timezone,
                                                      final DataUpdateType chartupdatetype,
                                                      final int displaylimit,
                                                      final boolean isdecimated,
                                                      final List<ChannelSelectionMode> selectionmodes)
        {
        final String SOURCE = "ChartHelper.copyTransformedXYDataset() ";
        final DataUpdateType updateTypeToTransform;
        final XYDataset xyResult;

        // If decimation is NOT requested by the user for a DECIMATE chart then just do PRESERVE
        // Otherwise do exactly as the Chart configuration requests
        if ((DataUpdateType.DECIMATE.equals(chartupdatetype))
            && (!isdecimated))
            {
            updateTypeToTransform = DataUpdateType.PRESERVE;
            }
        else
            {
            updateTypeToTransform = chartupdatetype;
            }

//        System.out.println(SOURCE + "displaylimit=" + displaylimit
//                           + " isdecimated=" + isdecimated
//                           + " chartupdatetype=" + chartupdatetype.getName()
//                           + " updateTypeToTransform=" + updateTypeToTransform.getName());

        //-----------------------------------------------------------------------------------------
        // Use the whole dataset immediately if requested

        if (DataUpdateType.PRESERVE.equals(updateTypeToTransform))
            {
            //System.out.println(SOURCE + "Preserve whole dataset");

            xyResult = dataset;

            return (xyResult);
            }

        //-----------------------------------------------------------------------------------------
        // Now do the transformation: TRUNCATE or DECIMATE

        if (dataset instanceof TimeSeriesCollection)
            {
            xyResult = new TimeSeriesCollection(timezone);

            // Process each Series in turn
            for (int intSeriesIndex = 0;
                 intSeriesIndex < dataset.getSeriesCount();
                 intSeriesIndex++)
                {
                final TimeSeries timeSeriesInput;
                final TimeSeries timeSeriesOutput;

                timeSeriesInput = ((TimeSeriesCollection)dataset).getSeries(intSeriesIndex);

                // Make a TimeSeries based on Seconds...
                // whose name is the ChannelName
                timeSeriesOutput = new TimeSeries(timeSeriesInput.getKey(),
                                                  timeSeriesInput.getTimePeriodClass());
                switch (updateTypeToTransform)
                    {
                    case TRUNCATE:
                        {
                        final int intStart;

                            //System.out.println("truncate time series");
                        if (timeSeriesInput.getItemCount() > displaylimit)
                            {
                            intStart = timeSeriesInput.getItemCount() - displaylimit;
                            }
                        else
                            {
                            intStart = 0;
                            }

                        // Modify each Series in exactly the same way!
//                        timeSeriesOutput = timeSeriesInput.createCopy(intStart, timeSeriesInput.getItemCount()-1);

                        for (int item = intStart;
                             item < timeSeriesInput.getItemCount();
                             item++)
                            {
                            timeSeriesOutput.add(timeSeriesInput.getDataItem(item));
                            }

                        break;
                        }

                    case DECIMATE:
                        {
                        final int intSkipCount;

                        //System.out.println("decimate time series index=" + intSeriesIndex);
                        if (timeSeriesInput.getItemCount() > displaylimit)
                            {
                            intSkipCount = (timeSeriesInput.getItemCount() / displaylimit) - 1;
                            }
                        else
                            {
                            // Show all of the data items, i.e. insufficient data to decimate
                            intSkipCount = 0;
                            }

                        for (int item = 0;
                             item < timeSeriesInput.getItemCount();
                             item = item + intSkipCount + 1)
                            {
                            timeSeriesOutput.add(timeSeriesInput.getDataItem(item));
                            }

                        break;
                        }

                    default:
                        {
                        LOGGER.error(SOURCE + MSG_UNSUPPORTED_UPDATE_TYPE);
                        }
                    }

                // Accumulate each Series in the output
                ((TimeSeriesCollection)xyResult).addSeries(timeSeriesOutput);
                }
            }
        else if (dataset instanceof XYSeriesCollection)
            {
            xyResult = new XYSeriesCollection();

            //System.out.println(SOURCE + "XYSeriesCollection for " + displaylimit + " samples");

            // Process each Series in turn
            for (int intSeriesIndex = 0;
                 intSeriesIndex < dataset.getSeriesCount();
                 intSeriesIndex++)
                {
                final XYSeries xySeriesInput;
                final XYSeries xySeriesOutput;

                xySeriesInput = ((XYSeriesCollection)dataset).getSeries(intSeriesIndex);
                xySeriesOutput = new XYSeries(dataset.getSeriesKey(intSeriesIndex));

                switch (updateTypeToTransform)
                    {
                    case TRUNCATE:
                        {
                        final int intStart;

                            //System.out.println("truncate xy");
                        if (xySeriesInput.getItemCount() > displaylimit)
                            {
                            intStart = xySeriesInput.getItemCount() - displaylimit;
                            }
                        else
                            {
                            intStart = 0;
                            }

                        // Modify each Series in exactly the same way!
//                        xySeriesOutput = xySeriesInput.createCopy(intStart, xySeriesInput.getItemCount()-1);

                        for (int item = intStart;
                             item < xySeriesInput.getItemCount();
                             item++)
                            {
                            xySeriesOutput.add(xySeriesInput.getDataItem(item));
                            }

                        break;
                        }

                    case DECIMATE:
                        {
                        final int intSkipCount;

                        //System.out.println("decimate xy series index=" + intSeriesIndex);
                        if (xySeriesInput.getItemCount() > displaylimit)
                            {
                            intSkipCount = (xySeriesInput.getItemCount() / displaylimit) - 1;
                            }
                        else
                            {
                            // Show all of the data items, i.e. insufficient data to decimate
                            intSkipCount = 0;
                            }

                        for (int item = 0;
                             item < xySeriesInput.getItemCount();
                             item = item + intSkipCount + 1)
                            {
                            xySeriesOutput.add(xySeriesInput.getDataItem(item));
                            }
                        break;
                        }

                    default:
                        {
                        LOGGER.error(SOURCE + MSG_UNSUPPORTED_UPDATE_TYPE);
                        }
                    }

                // Accumulate each Series in the output
                ((XYSeriesCollection)xyResult).addSeries(xySeriesOutput);
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unsupported XYDataset type");

            xyResult = new XYSeriesCollection();
            }

        return (xyResult);
        }


    /***********************************************************************************************
     * Get a data Series from the collection of XYdatasets, given its index.
     * Index out of range returns NULL.
     *
     *
     * @param datasettype
     * @param xydatasetprimary
     * @param xydatasetsecondaries
     * @param seriesindex
     *
     * @param debug
     * @return Series
     */

    public static Series getSeriesForIndex(final DatasetType datasettype,
                                           final XYDataset xydatasetprimary,
                                           final List<XYDataset> xydatasetsecondaries,
                                           final int seriesindex,
                                           final boolean debug)
        {
        final String SOURCE = "ChartHelper.getSeriesForIndex() ";
        Series seriesResult;
        final List<Series> listAccumulatedSeries;
        List<Series> listDatasetSeries;
        int intSeriesCount;

        seriesResult = null;
        listAccumulatedSeries = new ArrayList<Series>(10);
        intSeriesCount = 0;

        // First count the total number of data Series in the Primary Dataset
        if ((xydatasetprimary != null)
            && (xydatasetprimary.getSeriesCount() > 0))
            {
            intSeriesCount = intSeriesCount + xydatasetprimary.getSeriesCount();

            if ((datasettype.getName().equals(DatasetType.XY.getName()))
                && (xydatasetprimary instanceof XYSeriesCollection))
                {
                //System.out.println("ChartHelper.getSeriesForIndex() Adding XY series");
                listDatasetSeries = ((XYSeriesCollection)xydatasetprimary).getSeries();
                listAccumulatedSeries.addAll(listDatasetSeries);
                }
            else if ((datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                && (xydatasetprimary instanceof TimeSeriesCollection))
                {
                //System.out.println("ChartHelper.getSeriesForIndex() Adding Time series");
                listDatasetSeries = ((TimeSeriesCollection)xydatasetprimary).getSeries();
                listAccumulatedSeries.addAll(listDatasetSeries);
                }
            else
                {
                LOGGER.warn(SOURCE + "Invalid DatasetType or Primary series collection?");
                }

            // Now the same for each Series in each secondary dataset,
            // but only if we had a Primary
            if ((listAccumulatedSeries.size() > 0)
                && (xydatasetsecondaries != null)
                && (!xydatasetsecondaries.isEmpty()))
                {
                final Iterator<XYDataset> iterSecondaryDatasets;

                iterSecondaryDatasets = xydatasetsecondaries.iterator();

                while (iterSecondaryDatasets.hasNext())
                    {
                    final XYDataset xyDatasetSecondary;

                    xyDatasetSecondary = iterSecondaryDatasets.next();

                    if ((xyDatasetSecondary != null)
                        && (xyDatasetSecondary.getSeriesCount() > 0))
                        {
                        intSeriesCount = intSeriesCount + xyDatasetSecondary.getSeriesCount();

                        if ((datasettype.getName().equals(DatasetType.XY.getName()))
                            && (xyDatasetSecondary instanceof XYSeriesCollection))
                            {
                            listDatasetSeries = ((XYSeriesCollection)xyDatasetSecondary).getSeries();
                            listAccumulatedSeries.addAll(listDatasetSeries);
                            }
                        else if ((datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                            && (xyDatasetSecondary instanceof TimeSeriesCollection))
                            {
                            listDatasetSeries = ((TimeSeriesCollection)xyDatasetSecondary).getSeries();
                            listAccumulatedSeries.addAll(listDatasetSeries);
                            }
                        else
                            {
                            LOGGER.warn(SOURCE + "Invalid DatasetType or Secondary series collection?");
                            }
                        }
                    else
                        {
                        LOGGER.warn(SOURCE + "No Secondary Datasets, or zero series count");
                        }
                    }
                }
            else
                {
                LOGGER.warn(SOURCE + "No Secondary Datsets to process");
                }
            }
        else
            {
            LOGGER.warn(SOURCE + "No Primary Dataset, or zero series count");
            }

        // Check that the series Index is in range
        // If so, return the requested Series
        if ((seriesindex >= 0)
            && (seriesindex < intSeriesCount)
            && (listAccumulatedSeries.size() == intSeriesCount))
            {
            LOGGER.log(SOURCE + "series ok!! count=" + intSeriesCount + " index=" + seriesindex);
            seriesResult = listAccumulatedSeries.get(seriesindex);
            }
        else
            {
            LOGGER.warn(SOURCE + "Unable to find requested series [index=" + seriesindex + "]");
            }

        return (seriesResult);
        }


    /***********************************************************************************************
     * Add a data Series to an existing Dataset in the collection of SecondaryDatasets,
     * given its secondary channel index.
     * Ignore primary channel indexes because we don't know how many channels were in the
     * Primary collection.
     *
     * @param datasettype
     * @param secondarydatasets
     * @param series
     * @param secondarychannelindex
     */

    public static void addSecondarySeries(final DatasetType datasettype,
                                          final List<XYDataset> secondarydatasets,
                                          final Series series,
                                          final int secondarychannelindex)
        {
        final String SOURCE = "ChartHelper.addSecondarySeries() ";

        if ((datasettype != null)
            && (secondarydatasets != null)
            && (secondarydatasets.size() > 0)
            && (series != null)
            && (secondarychannelindex >= 0)
            && (secondarychannelindex < secondarydatasets.size()))
            {
            if ((datasettype.getName().equals(DatasetType.XY.getName()))
                && (secondarydatasets.get(secondarychannelindex) instanceof XYSeriesCollection))
                {
                ((XYSeriesCollection) secondarydatasets.get(secondarychannelindex)).addSeries((XYSeries)series);
                }
            else if ((datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                && (secondarydatasets.get(secondarychannelindex) instanceof TimeSeriesCollection))
                {
                ((TimeSeriesCollection) secondarydatasets.get(secondarychannelindex)).addSeries((TimeSeries)series);
                }
            else
                {
                LOGGER.error(SOURCE + "Unsupported DatasetType [type=" + datasettype.getName() + "]");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to add a Secondary Series");
            }
        }


    /***********************************************************************************************
     * Crop the specified Vector of CalendarisedData to the range given by the start and end Calendars.
     * Return the cropped CalendarisedData, or NULL on failure.
     *
     * @param calendariseddata
     * @param startcalendar
     * @param endcalendar
     *
     * @return Vector<Object>
     */

    public static Vector<Object> cropCalendarisedDataToRange(final Vector<Object> calendariseddata,
                                                             final Calendar startcalendar,
                                                             final Calendar endcalendar)
        {
        Vector<Object> vecCropped;

        vecCropped = null;

        if ((calendariseddata != null)
            && (!calendariseddata.isEmpty())
            && (startcalendar != null)
            && (endcalendar != null))
            {
            vecCropped = new Vector<Object>(calendariseddata.size());

            for (int intDataIndex = 0;
                 intDataIndex < calendariseddata.size();
                 intDataIndex++)
                {
                final Vector<Object> vecRow;
                final Calendar calendarRow;

                vecRow = (Vector)calendariseddata.get(intDataIndex);

                // We can safely assume that the data Vector is calendarised
                calendarRow = (Calendar)vecRow.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                if ((calendarRow.equals(startcalendar) || calendarRow.after(startcalendar))
                    && (calendarRow.before(endcalendar) || calendarRow.equals(endcalendar)))
                    {
                    vecCropped.add(vecRow);
                    }
                }
            }

        // Did we gather any data?
        if ((vecCropped != null)
           && (vecCropped.isEmpty()))
            {
            // Return NULL on failure
            vecCropped = null;
            }

        return (vecCropped);
        }


    /***********************************************************************************************
     * Crop the specified TimeSeriesCollection to the range given by the start and end Calendars.
     * Return the cropped collection, or NULL on failure.
     *
     * @param collection
     * @param startcalendar
     * @param endcalendar
     *
     * @return TimeSeriesCollection
     */

    public static TimeSeriesCollection cropTimeSeriesCollectionToRange(final TimeSeriesCollection collection,
                                                                       final Calendar startcalendar,
                                                                       final Calendar endcalendar)
        {
        // ToDo Implement cropTimeSeriesCollectionToRange!

        return (collection);
        }


    /***********************************************************************************************
     * Create a dummy TimeSeriesDataItem at the origin of the Domain.
     *
     * @param timeseriescollection
     * @param timeseries
     * @param domainstart
     * @param debug
     *
     * @return TimeSeriesDataItem
     */

    public static TimeSeriesDataItem createDummyTimeSeriesDataItemAtOrigin(final TimeSeriesCollection timeseriescollection,
                                                                           final TimeSeries timeseries,
                                                                           final int domainstart,
                                                                           final boolean debug)
        {
        final String SOURCE = "ChartHelper.createDummyTimeSeriesDataItemAtOrigin() [domainstart=" + domainstart + "]";

        LOGGER.debug(debug, SOURCE);

        return new TimeSeriesDataItem(RegularTimePeriod.createInstance(timeseries.getTimePeriodClass(),
                                                                       new Date(calculateDomainSliderMillis(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                                            ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                                            domainstart,
                                                                                                            timeseriescollection.getDomainLowerBound(true),
                                                                                                            timeseriescollection.getDomainUpperBound(true),
                                                                                                            debug)),
                                                                       TimeZone.getDefault()),
                                      timeseries.getMinY());
        }


    /**********************************************************************************************/
    /* DatasetDomain Sliders                                                                      */
    /***********************************************************************************************
     * Transform a Domain Slider value to an index into the List of specified data items,
     * taking account that the List may not fully occupy the Domain.
     *
     * Series_position   Left   Right
     *        |
     *        |  xxx     start   -1
     *        |
     *        |xxx       start   start
     *        |
     *       xxx         index   index
     *        |
     *     xxx|          end     end
     *        |
     *   xxx  |          -1      end
     *        |
     *
     * @param domainslidermin
     * @param domainslidermax
     * @param domainslidervalue
     * @param domainsliderindex
     * @param domainlowerbound
     * @param domainupperbound
     * @param datasettype
     * @param calendar
     * @param series
     * @param debug
     *
     * @return int
     */

    public static int transformDomainSliderValueToSeriesIndex(final int domainslidermin,
                                                              final int domainslidermax,
                                                              final int domainslidervalue,
                                                              final int domainsliderindex,
                                                              final double domainlowerbound,
                                                              final double domainupperbound,
                                                              final DatasetType datasettype,
                                                              final Calendar calendar,
                                                              final Series series,
                                                              final boolean debug)
        {
        final String SOURCE = "ChartHelper.transformDomainSliderValueToSeriesIndex() ";
        final int intIndex;

        switch (datasettype)
            {
            case XY:
                {
                final XYSeries xySeries;
                final XYDataItem dataItemFirst;
                final XYDataItem dataItemLast;
                final double dblItemFirstX;
                final double dblItemLastX;
                final double dblItemLastFirstX;
                final double dblDomainSliderX;

                xySeries = (XYSeries)series;
                dataItemFirst = xySeries.getDataItem(0);
                dataItemLast = xySeries.getDataItem(xySeries.getItemCount() - 1);

                dblItemFirstX = dataItemFirst.getXValue();
                dblItemLastX = dataItemLast.getXValue();

                // Diagnostic only
                dblItemLastFirstX = dblItemLastX - dblItemFirstX;

                dblDomainSliderX = calculateDomainSliderX(domainslidermin,
                                                          domainslidermax,
                                                          domainslidervalue,
                                                          domainlowerbound,
                                                          domainupperbound);

                LOGGER.debug(debug,
                             SOURCE + "\n[domain.slider.min=" + domainslidermin
                                    + "]\n[domain.slider.max=" + domainslidermax
                                    + "]\n[domain.slider.value=" + domainslidervalue
                                    + "]\n[domain.slider.index=" + domainsliderindex
                                    + "]\n[domain.lowerbound=" + domainlowerbound
                                    + "]\n[domain.upperbound=" + domainupperbound
                                    + "]\n[domain.slider.x=" + dblDomainSliderX
                                    + "]\n[lastfirstitem.x=" + dblItemLastFirstX
                                    + "]\n[firstitem.x=" + dblItemFirstX
                                    + "]\n[lastitem.x=" + dblItemLastX
                                    + "]\n[dataset.type=" + datasettype.getName()
                                    + "]\n[series.itemcount=" + series.getItemCount()
                                    + "]");

                if (domainsliderindex == INDEX_LEFT)
                    {
                    if (dblDomainSliderX <= dblItemFirstX)
                        {
                        intIndex = 0;
                        //System.out.println("LEFT <=");
                        }
                    else if (dblDomainSliderX == dblItemLastX)
                        {
                        intIndex = xySeries.getItemCount() - 1;
                        //System.out.println("LEFT ==");
                        }
                    else if (dblDomainSliderX > dblItemLastX)
                        {
                        intIndex = -1;
                        //System.out.println("LEFT >");
                        }
                    else
                        {
                        intIndex = findXYDataItemIndexForX(dblDomainSliderX, xySeries);
                        //System.out.println("LEFT index");
                        }
                    }
                else if (domainsliderindex == INDEX_RIGHT)
                    {
                    if (dblDomainSliderX < dblItemFirstX)
                        {
                        intIndex = -1;
                        //System.out.println("RIGHT <");
                        }
                    else if (dblDomainSliderX == dblItemFirstX)
                        {
                        intIndex = 0;
                        //System.out.println("RIGHT ==");
                        }
                    else if (dblDomainSliderX >= dblItemLastX)
                        {
                        intIndex = xySeries.getItemCount() - 1;
                        //System.out.println("RIGHT >=");
                        }
                    else
                        {
                        intIndex = findXYDataItemIndexForX(dblDomainSliderX, xySeries);
                        //System.out.println("RIGHT index");
                        }
                    }
                else
                    {
                    // Do nothing, an Error
                    LOGGER.error(SOURCE + "Invalid Domain Slider [index=" + domainsliderindex + "]");
                    intIndex = -1;
                    }

                break;
                }

            case TIMESTAMPED:
                {
                final TimeSeries timeSeries;
                final TimeSeriesDataItem dataItemFirst;
                final TimeSeriesDataItem dataItemLast;
                final double dblItemFirstMillis;
                final double dblItemLastMillis;
                final double dblItemLastFirstMillis;
                final double dblDomainSliderMillis;

                timeSeries = (TimeSeries)series;
                dataItemFirst = timeSeries.getDataItem(0);
                dataItemLast = timeSeries.getDataItem(timeSeries.getItemCount() - 1);

                dblItemFirstMillis = dataItemFirst.getPeriod().getFirstMillisecond(calendar);
                dblItemLastMillis = dataItemLast.getPeriod().getLastMillisecond(calendar);

                // Diagnostic only
                dblItemLastFirstMillis = dblItemLastMillis - dblItemFirstMillis;

                dblDomainSliderMillis = calculateDomainSliderMillis(domainslidermin,
                                                                    domainslidermax,
                                                                    domainslidervalue,
                                                                    domainlowerbound,
                                                                    domainupperbound,
                                                                    calendar,
                                                                    debug);
                LOGGER.debug(debug,
                             SOURCE + "]\n\n[domain.slider.index=" + domainsliderindex
                                    + "]\n[domain.slider.value=" + domainslidervalue
                                    + "]\n[domain.slider.millis=" + (long)dblDomainSliderMillis

                                    + "]\n\n[firstitem.millis.calendar=" + (long)dblItemFirstMillis
                                    + "]\n[firstitem.millis.period=" + dataItemFirst.getPeriod().getFirstMillisecond()
                                    + "]\n[lastitem.millis.calendar=" + (long)dblItemLastMillis
                                    + "]\n[lastitem.millis.period=" + dataItemLast.getPeriod().getLastMillisecond()
                                    + "]\n[lastfirstitem.millis=" + (long)dblItemLastFirstMillis

                                    + "]\n\n[dataset.type=" + datasettype.getName()
                                    + "]\n[series.itemcount=" + series.getItemCount()
                                    + "]");

                if (domainsliderindex == INDEX_LEFT)
                    {
                    if (dblDomainSliderMillis <= dblItemFirstMillis)
                        {
                        intIndex = 0;
                        LOGGER.debug(debug,
                                     SOURCE + "LEFT(slider) <= FIRST(calendar)");
                        }
                    else if (dblDomainSliderMillis == dblItemLastMillis)
                        {
                        intIndex = timeSeries.getItemCount() - 1;
                        LOGGER.debug(debug,
                                     SOURCE + "LEFT(slider) == LAST(calendar)");
                        }
                    else if (dblDomainSliderMillis > dblItemLastMillis)
                        {
                        intIndex = -1;
                        LOGGER.debug(debug,
                                     SOURCE + "LEFT(slider) > LAST(calendar)");
                        }
                    else
                        {
                        intIndex = findTimeSeriesIndexForMillis(dblDomainSliderMillis, timeSeries, calendar);
                        LOGGER.debug(debug,
                                     SOURCE + "LEFT ok");
                        }
                    }
                else if (domainsliderindex == INDEX_RIGHT)
                    {
                    if (dblDomainSliderMillis < dblItemFirstMillis)
                        {
                        intIndex = -1;
                        LOGGER.debug(debug,
                                     SOURCE + "RIGHT(slider) < FIRST(calendar)");
                        }
                    else if (dblDomainSliderMillis == dblItemFirstMillis)
                        {
                        intIndex = 0;
                        LOGGER.debug(debug,
                                     SOURCE + "RIGHT(slider) == FIRST(calendar)");
                        }
                    else if (dblDomainSliderMillis >= dblItemLastMillis)
                        {
                        intIndex = timeSeries.getItemCount() - 1;
                        LOGGER.debug(debug,
                                     SOURCE + "RIGHT(slider) >= LAST(calendar)");
                        }
                    else
                        {
                        intIndex = findTimeSeriesIndexForMillis(dblDomainSliderMillis, timeSeries, calendar);
                        LOGGER.debug(debug,
                                     SOURCE + "RIGHT(slider) ok");
                        }
                    }
                else
                    {
                    // Do nothing, an Error
                    LOGGER.error(SOURCE + "Invalid Domain Slider [domainslider.index=" + domainsliderindex + "]");
                    intIndex = -1;
                    }

                break;
                }

            default:
                {
                // Do nothing, an Error
                LOGGER.error(SOURCE + "Invalid DatasetType [type=" + datasettype.getName() + "]");
                intIndex = -1;
                }
            }

        LOGGER.debug(debug,
                     SOURCE + "[series.item.index=" + intIndex
                            + "] [series.item.count=" + series.getItemCount() + "]");
        LOGGER.debug(debug, Logger.CONSOLE_SEPARATOR_MINOR);

        return (intIndex);
        }


    /**********************************************************************************************/
    /* Indexed                                                                                    */
    /***********************************************************************************************
     * Find the (nearest) XYDataItem index in the specified XYSeries, for the specified X position.
     *
     * @param xposition
     * @param xyseries
     *
     * @return int
     */

    private static int findXYDataItemIndexForX(final double xposition,
                                               final XYSeries xyseries)
        {
        final String SOURCE = "ChartHelper.findXYDataItemIndexForX() ";
        int intIndex;

        intIndex = -1;

        // Let's be careful, in case something goes wrong...
        try
            {
            final List<XYDataItem> listDataItems;
            boolean boolFoundIt;

            listDataItems = xyseries.getItems();
            boolFoundIt = false;

            // Scan the whole Series to find the first Item with the correct X value
            for (int intItemIndex = 0;
                 ((!boolFoundIt)
                  && (intItemIndex < listDataItems.size()));
                 intItemIndex++)
                {
                final XYDataItem dataItem;

                dataItem = listDataItems.get(intItemIndex);

                // The Domain Slider should be pointing at a real XYDataItem,
                // or at least very close to it...
                boolFoundIt = (dataItem.getXValue() >= xposition);

                if (boolFoundIt)
                    {
                    intIndex = intItemIndex;

                    LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                 SOURCE + "[item.index=" + intIndex
                                        + "] [series.size=" + listDataItems.size()
                                        + "] [item.x=" + dataItem.getXValue()
                                        + "] [slider.x=" + xposition
                                        + "] [found.it=" + boolFoundIt
                                        + "]");
                    }
                }
            }

        catch (final IndexOutOfBoundsException exception)
            {
            LOGGER.error(SOURCE + "IndexOutOfBoundsException");
            intIndex = -1;
            }

        catch (final NullPointerException exception)
            {
            LOGGER.error(SOURCE + "NullPointerException");
            intIndex = -1;
            }

        return (intIndex);
        }


    /***********************************************************************************************
     * Calculate the Domain Slider X position as an offset from the domain lower bound.
     *
     * @param domainslidermin
     * @param domainslidermax
     * @param domainslidervalue
     * @param domainlowerbound
     * @param domainupperbound
     *
     * @return double
     */

    private static double calculateDomainSliderX(final int domainslidermin,
                                                 final int domainslidermax,
                                                 final int domainslidervalue,
                                                 final double domainlowerbound,
                                                 final double domainupperbound)
        {
        final double dblDomainRangeX;
        final double dblDomainSliderX;

        dblDomainRangeX = domainupperbound - domainlowerbound;
        dblDomainSliderX = domainlowerbound
                                + (dblDomainRangeX * ((double)domainslidervalue / (domainslidermax - (double)domainslidermin)));

        return (dblDomainSliderX);
        }


    /**********************************************************************************************/
    /* Timestamped                                                                                */
    /***********************************************************************************************
     * Find the Series index for the specified Slider position in milliseconds.
     *
     * @param millis
     * @param timeseries
     * @param calendar
     *
     * @return int
     */

    private static int findTimeSeriesIndexForMillis(final double millis,
                                                    final TimeSeries timeseries,
                                                    final Calendar calendar)
        {
        final String SOURCE = "ChartHelper.findTimeSeriesIndexForMillis() ";
        int intIndex;

        intIndex = -1;

        // ToDo Review if this would be better using timeseries.getDataItem(RegularTimePeriod)
        // Let's be careful, in case something goes wrong...
        try
            {
            final List<TimeSeriesDataItem> listDataItems;
            boolean boolFoundIt;

            listDataItems = timeseries.getItems();
            boolFoundIt = false;

            // Scan the whole Series to find the first Item with the correct time period
            // This avoids having to create a RegularTimePeriod each time?
            for (int intItemIndex = 0;
                 ((!boolFoundIt)
                  && (intItemIndex < listDataItems.size()));
                 intItemIndex++)
                {
                final TimeSeriesDataItem dataItem;

                dataItem = listDataItems.get(intItemIndex);

                // The Domain Slider should be pointing at a real TimeSeriesDataItem,
                // or at least very close to it...
                boolFoundIt = (dataItem.getPeriod().getFirstMillisecond(calendar) >= (long)millis);

                if (boolFoundIt)
                    {
                    intIndex = intItemIndex;

                    LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                 SOURCE + "[item.index=" + intIndex
                                        + "] [series.size=" + listDataItems.size()
                                        + "] [item.period.first.millis=" + dataItem.getPeriod().getFirstMillisecond(calendar)
                                        + "] [slider.millis=" + (long)millis
                                        + "] [found.it=" + boolFoundIt
                                        + "]");
                    }
                }
            }

        catch (final IndexOutOfBoundsException exception)
            {
            LOGGER.error(SOURCE + "IndexOutOfBoundsException");
            intIndex = -1;
            }

        catch (final NullPointerException exception)
            {
            LOGGER.error(SOURCE + "NullPointerException");
            intIndex = -1;
            }

        return (intIndex);
        }


    /***********************************************************************************************
     * Calculate the Domain Slider position in milliseconds offset from the domain lower bound.
     *
     * @param domainslidermin
     * @param domainslidermax
     * @param domainslidervalue
     * @param domainlowerbound
     * @param domainupperbound
     * @param debug
     *
     * @return long
     */

    private static long calculateDomainSliderMillis(final int domainslidermin,
                                                    final int domainslidermax,
                                                    final int domainslidervalue,
                                                    final double domainlowerbound,
                                                    final double domainupperbound,
                                                    final boolean debug)
        {
        final String SOURCE = "ChartHelper.calculateDomainSliderMillis() ";
        final double dblDomainRangeMillis;
        final double dblDomainSliderMillis;

        dblDomainRangeMillis = domainupperbound - domainlowerbound;
        dblDomainSliderMillis = domainlowerbound
                                + (dblDomainRangeMillis * ((double)domainslidervalue / (domainslidermax - (double)domainslidermin)));

        LOGGER.debug(debug,
                     SOURCE + "\n[domain.slider.min=" + domainslidermin
                            + "]\n[domain.slider.max=" + domainslidermax
                            + "]\n[domain.slider.value=" + domainslidervalue
                            + "]\n[domain.lowerbound=" + (long)domainlowerbound
                            + "]\n[domain.upperbound=" + (long)domainupperbound
                            + "]\n[domain.range.millis=" + (long)dblDomainRangeMillis
                            + "]\n[domain.slider.millis=" + (long)dblDomainSliderMillis
                            + "]\n");

        return ((long)dblDomainSliderMillis);
        }


    /***********************************************************************************************
     * Calculate the Domain Slider position in milliseconds offset from the domain lower bound.
     * WARNING! This time in millis is relative to the epoch January 1, 1970, 00:00:00 GMT,
     * as are the specified lower bound and upper bound.
     *
     * @param domainslidermin
     * @param domainslidermax
     * @param domainslidervalue
     * @param domainlowerbound
     * @param domainupperbound
     * @param calendar
     * @param debug
     *
     * @return long
     */

    private static long calculateDomainSliderMillis(final int domainslidermin,
                                                    final int domainslidermax,
                                                    final int domainslidervalue,
                                                    final double domainlowerbound,
                                                    final double domainupperbound,
                                                    final Calendar calendar,
                                                    final boolean debug)
        {
        final String SOURCE = "ChartHelper.calculateDomainSliderMillis() ";
        final double dblDomainRangeMillis;
        final double dblDomainSliderMillis;

        dblDomainRangeMillis = domainupperbound - domainlowerbound;
        dblDomainSliderMillis = domainlowerbound
                                + (dblDomainRangeMillis * ((double)domainslidervalue / (domainslidermax - (double)domainslidermin)));

        LOGGER.debug(debug,
                     SOURCE + "Relative to January 1, 1970, 00:00:00 GMT"
                            + "\n[domain.slider.min=" + domainslidermin
                            + "]\n[domain.slider.max=" + domainslidermax
                            + "]\n[domain.slider.value=" + domainslidervalue
                            + "]\n[domain.lowerbound=" + (long)domainlowerbound
                            + "]\n[domain.upperbound=" + (long)domainupperbound
                            + "]\n[domain.range.millis=" + (long)dblDomainRangeMillis
                            + "]\n[domain.slider.millis=" + (long)dblDomainSliderMillis
                            + "]\n");

        calendar.setTimeInMillis((long)dblDomainSliderMillis);

        // See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4827490
        calendar.getTime();
        calendar.get(Calendar.HOUR_OF_DAY);

        System.out.println("RECALCULATED CALENDAR *********************************************");
        ObservatoryInstrumentHelper.debugCalendar(debug, calendar, SOURCE);

        return ((long)dblDomainSliderMillis);
        }


    /**********************************************************************************************/
    /* DatasetDomain Offset Knob                                                                  */


    /***********************************************************************************************
     * Handle Autorange for Linear mode.
     * When not autoranging, use the specified limits for the Y axis.
     *
     * @param jfreechart
     * @param channelselector
     * @param canautorange
     * @param fixedrangelowery
     * @param fixedrangeuppery
     * @param debug
     */

    public static void handleAutorangeForLinearMode(final JFreeChart jfreechart,
                                                    final ChannelSelectorUIComponentInterface channelselector,
                                                    final boolean canautorange,
                                                    final double fixedrangelowery,
                                                    final double fixedrangeuppery,
                                                    final boolean debug)
        {
        final String SOURCE = "ChartHelper.handleAutorangeForLinearMode() ";

        if ((jfreechart != null)
            && (channelselector != null))
            {
            final XYPlot plot;
            final ValueAxis axisLinear;

            channelselector.debugSelector(debug, SOURCE);

            plot = jfreechart.getXYPlot();

            axisLinear = plot.getRangeAxis();

            if ((canautorange)
                && (channelselector.isAutoranging()))
                {
                LOGGER.debug(debug,
                             SOURCE + "Linear Mode: Set up Autoranging");

                axisLinear.setAutoRange(true);
                axisLinear.configure();
                }
            else
                {
                LOGGER.debug(debug,
                             SOURCE + "Linear Mode: Set up Fixed Range [y.lower=" + fixedrangelowery + "] [y.upper=" + fixedrangeuppery + "]");

                axisLinear.setRange(fixedrangelowery, fixedrangeuppery);
                }
            }
        }


    /***********************************************************************************************
     * Dump the (partial) contents of each Series in an XYdatset.
     *
     * @param dump
     * @param calendar
     * @param dataset
     * @param dumprowcount
     * @param title
     */

    public static void dumpXYDataset(final boolean dump,
                                     final Calendar calendar,
                                     final XYDataset dataset,
                                     final int dumprowcount,
                                     final String title)
        {
        final String SOURCE = "ChartHelper.dumpXYDataset() ";

        if (dump)
            {
            LOGGER.log(title);

            if ((dataset != null)
                && (dataset instanceof XYSeriesCollection))
                {
                final XYSeriesCollection seriesCollection;

                seriesCollection = (XYSeriesCollection)dataset;

                LOGGER.log("XYSeriesCollection");
                LOGGER.log("    [series.count=" + seriesCollection.getSeriesCount() + "]");
                LOGGER.log("    [domain.lowerbound.interval.true=" + (long)seriesCollection.getDomainLowerBound(true) + "]");
                LOGGER.log("    [domain.lowerbound.interval.false=" + (long)seriesCollection.getDomainLowerBound(false) + "]");
                LOGGER.log("    [domain.upperbound.interval.true=" + (long)seriesCollection.getDomainUpperBound(true) + "]");
                LOGGER.log("    [domain.upperbound.interval.false=" + (long)seriesCollection.getDomainUpperBound(false) + "]");
                LOGGER.log("    [domain.order=" + seriesCollection.getDomainOrder() + "]");

                for (int intSeriesIndex = 0;
                     intSeriesIndex < seriesCollection.getSeriesCount();
                     intSeriesIndex++)
                    {
                    final XYSeries xySeries;

                    LOGGER.log("");
                    LOGGER.log("    [xyseries.index=" + intSeriesIndex + "]");

                    xySeries = seriesCollection.getSeries(intSeriesIndex);
                    LOGGER.log("    [xyseries.itemcount=" + xySeries.getItemCount() + "]");
                    LOGGER.log("    [xyseries.key=" + xySeries.getKey() + "]");
                    LOGGER.log("    [xyseries.xmin=" + xySeries.getMinX() + "]");
                    LOGGER.log("    [xyseries.xmax=" + xySeries.getMaxX() + "]");
                    LOGGER.log("    [xyseries.ymin=" + xySeries.getMinY() + "]");
                    LOGGER.log("    [xyseries.ymax=" + xySeries.getMaxY() + "]");
                    LOGGER.log("    [xyseries.description=" + xySeries.getDescription() + "]");
                    LOGGER.log("    [xyseries.autosort=" + xySeries.getAutoSort() + "]");
                    LOGGER.log("    [xyseries.allowduplicatex=" + xySeries.getAllowDuplicateXValues() + "]");

                    // Dump the first chunk
                    for (int intItemIndex = 0;
                         intItemIndex < (Math.min(dumprowcount, xySeries.getItemCount()));
                         intItemIndex++)
                        {
                        final XYDataItem item;

                        item = xySeries.getDataItem(intItemIndex);

                        LOGGER.log("        [item.index=" + intItemIndex
                                   + "] [item.x=" + item.getXValue()
                                   + "] [item.y=" + item.getYValue() + "]");
                        }

                    LOGGER.log("    ...");

                    // Dump the last chunk
                    for (int intItemIndex = 0;
                         intItemIndex < (Math.min(dumprowcount, xySeries.getItemCount()));
                         intItemIndex++)
                        {
                        final XYDataItem item;
                        final int intIndex;

                        intIndex = Math.max(0, xySeries.getItemCount() - dumprowcount) + intItemIndex;
                        item = xySeries.getDataItem(intIndex);

                        LOGGER.log("        [item.index=" + intIndex
                                   + "] [item.x=" + item.getXValue()
                                   + "] [item.y=" + item.getYValue() + "]");
                        }
                    }
                }
            else if ((dataset != null)
                     && (dataset instanceof TimeSeriesCollection))
                {
                final TimeSeriesCollection seriesCollection;

                seriesCollection = (TimeSeriesCollection)dataset;

                LOGGER.log("TimeSeriesCollection");
                LOGGER.log("    [series.count=" + seriesCollection.getSeriesCount() + "]");
                LOGGER.log("    [domain.lowerbound.interval.true=" + (long)seriesCollection.getDomainLowerBound(true) + "]");
                LOGGER.log("    [domain.lowerbound.interval.false=" + (long)seriesCollection.getDomainLowerBound(false) + "]");
                LOGGER.log("    [domain.upperbound.interval.true=" + (long)seriesCollection.getDomainUpperBound(true) + "]");
                LOGGER.log("    [domain.upperbound.interval.false=" + (long)seriesCollection.getDomainUpperBound(false) + "]");
                LOGGER.log("    [domain.order=" + seriesCollection.getDomainOrder() + "]");

                for (int intSeriesIndex = 0;
                     intSeriesIndex < seriesCollection.getSeriesCount();
                     intSeriesIndex++)
                    {
                    final TimeSeries timeSeries;

                    LOGGER.log("");
                    LOGGER.log("    [timeseries.index=" + intSeriesIndex + "]");

                    timeSeries = seriesCollection.getSeries(intSeriesIndex);
                    LOGGER.log("    [timeseries.itemcount=" + timeSeries.getItemCount() + "]");
                    LOGGER.log("    [timeseries.key=" + timeSeries.getKey() + "]");
                    LOGGER.log("    [timeseries.ymin=" + timeSeries.getMinY() + "]");
                    LOGGER.log("    [timeseries.ymax=" + timeSeries.getMaxY() + "]");
                    LOGGER.log("    [timeseries.domain=" + timeSeries.getDomainDescription() + "]");
                    LOGGER.log("    [timeseries.range=" + timeSeries.getRangeDescription() + "]");
                    LOGGER.log("    [timeseries.timeperiodclass=" + timeSeries.getTimePeriodClass().getName() + "]");

                    for (int intItemIndex = 0;
                         intItemIndex < (Math.min(dumprowcount, timeSeries.getItemCount()));
                         intItemIndex++)
                        {
                        final TimeSeriesDataItem item;

                        item = timeSeries.getDataItem(intItemIndex);

                        LOGGER.log("        [item.index=" + intItemIndex
                                   + "] [item.period.serialindex=" + item.getPeriod().getSerialIndex()
                                   + "] [item.period.firstmillis=" + item.getPeriod().getFirstMillisecond(calendar)
                                   + "] [item.value=" + item.getValue() + "]");
                        }

                    LOGGER.log("    ...");

                    for (int intItemIndex = 0;
                         intItemIndex < (Math.min(dumprowcount, timeSeries.getItemCount()));
                         intItemIndex++)
                        {
                        final TimeSeriesDataItem item;
                        final int intIndex;

                        intIndex = Math.max(0, timeSeries.getItemCount() - dumprowcount) + intItemIndex;
                        item = timeSeries.getDataItem(intIndex);

                        LOGGER.log("        [item.index=" + intIndex
                                   + "] [item.period.serialindex=" + item.getPeriod().getSerialIndex()
                                   + "] [item.period.firstmillis=" + item.getPeriod().getFirstMillisecond(calendar)
                                   + "] [item.value=" + item.getValue() + "]");
                        }
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "Unsupported XYDataset type");
                }
            }
        }


    /***********************************************************************************************
     * Creates a test dataset.
     *
     * @return the dataset.
     */

    public static XYDataset createDummyDataset()
        {
        final TimeSeries s1 = new TimeSeries("Channel 0 23.4kHz",
                                             Second.class);

        s1.add(new Second(new Date(1110400770000L)), 178);
        s1.add(new Second(new Date(1110401020000L)), 182);
        s1.add(new Second(new Date(1110401270000L)), 172);
        s1.add(new Second(new Date(1110401520000L)), 173);
        s1.add(new Second(new Date(1110401770000L)), 164);
        s1.add(new Second(new Date(1110402080000L)), 148);
        s1.add(new Second(new Date(1110402330000L)), 159);
        s1.add(new Second(new Date(1110402580000L)), 162);
        s1.add(new Second(new Date(1110402830000L)), 171);
        s1.add(new Second(new Date(1110403080000L)), 175);
        s1.add(new Second(new Date(1110403330000L)), 175);
        s1.add(new Second(new Date(1110403580000L)), 169);
        s1.add(new Second(new Date(1110403830000L)), 176);
        s1.add(new Second(new Date(1110404080000L)), 181);
        s1.add(new Second(new Date(1110404330000L)), 184);
        s1.add(new Second(new Date(1110404580000L)), 169);
        s1.add(new Second(new Date(1110404830000L)), 165);
        s1.add(new Second(new Date(1110405080000L)), 163);
        s1.add(new Second(new Date(1110405330000L)), 149);
        s1.add(new Second(new Date(1110405580000L)), 150);
        s1.add(new Second(new Date(1110405830000L)), 163);
        s1.add(new Second(new Date(1110406080000L)), 164);
        s1.add(new Second(new Date(1110406330000L)), 161);
        s1.add(new Second(new Date(1110406580000L)), 169);
        s1.add(new Second(new Date(1110406830000L)), 170);
        s1.add(new Second(new Date(1110407080000L)), 167);
        s1.add(new Second(new Date(1110407330000L)), 167);
        s1.add(new Second(new Date(1110407580000L)), 187);
        s1.add(new Second(new Date(1110407830000L)), 195);
        s1.add(new Second(new Date(1110408080000L)), 207);
        s1.add(new Second(new Date(1110408330000L)), 200);
        s1.add(new Second(new Date(1110408580000L)), 185);
        s1.add(new Second(new Date(1110408830000L)), 179);
        s1.add(new Second(new Date(1110409080000L)), 172);
        s1.add(new Second(new Date(1110409330000L)), 196);
        s1.add(new Second(new Date(1110409580000L)), 233);
        s1.add(new Second(new Date(1110409830000L)), 218);
        s1.add(new Second(new Date(1110410080000L)), 217);
        s1.add(new Second(new Date(1110410330000L)), 198);
        s1.add(new Second(new Date(1110410580000L)), 196);
        s1.add(new Second(new Date(1110410830000L)), 183);
        s1.add(new Second(new Date(1110411080000L)), 170);
        s1.add(new Second(new Date(1110411330000L)), 155);
        s1.add(new Second(new Date(1110411580000L)), 167);
        s1.add(new Second(new Date(1110411830000L)), 166);
        s1.add(new Second(new Date(1110412080000L)), 184);
        s1.add(new Second(new Date(1110412330000L)), 199);
        s1.add(new Second(new Date(1110412580000L)), 194);
        s1.add(new Second(new Date(1110412830000L)), 185);
        s1.add(new Second(new Date(1110413080000L)), 181);
        s1.add(new Second(new Date(1110413330000L)), 171);
        s1.add(new Second(new Date(1110413580000L)), 163);
        s1.add(new Second(new Date(1110413830000L)), 169);
        s1.add(new Second(new Date(1110414080000L)), 162);
        s1.add(new Second(new Date(1110414330000L)), 173);
        s1.add(new Second(new Date(1110414580000L)), 160);
        s1.add(new Second(new Date(1110414830000L)), 147);
        s1.add(new Second(new Date(1110415080000L)), 151);
        s1.add(new Second(new Date(1110415330000L)), 179);
        s1.add(new Second(new Date(1110415580000L)), 192);
        s1.add(new Second(new Date(1110415830000L)), 180);
        s1.add(new Second(new Date(1110416080000L)), 159);
        s1.add(new Second(new Date(1110416330000L)), 164);
        s1.add(new Second(new Date(1110416580000L)), 178);
        s1.add(new Second(new Date(1110416830000L)), 163);
        s1.add(new Second(new Date(1110417080000L)), 157);
        s1.add(new Second(new Date(1110417330000L)), 152);
        s1.add(new Second(new Date(1110417580000L)), 148);
        s1.add(new Second(new Date(1110417830000L)), 162);
        s1.add(new Second(new Date(1110418080000L)), 176);
        s1.add(new Second(new Date(1110418330000L)), 168);
        s1.add(new Second(new Date(1110418580000L)), 167);
        s1.add(new Second(new Date(1110418830000L)), 170);
        s1.add(new Second(new Date(1110419080000L)), 161);
        s1.add(new Second(new Date(1110419330000L)), 150);
        s1.add(new Second(new Date(1110419580000L)), 157);
        s1.add(new Second(new Date(1110419830000L)), 164);
        s1.add(new Second(new Date(1110420080000L)), 156);
        s1.add(new Second(new Date(1110420330000L)), 155);
        s1.add(new Second(new Date(1110420580000L)), 150);
        s1.add(new Second(new Date(1110420830000L)), 160);
        s1.add(new Second(new Date(1110421080000L)), 168);
        s1.add(new Second(new Date(1110421330000L)), 160);
        s1.add(new Second(new Date(1110421580000L)), 162);
        s1.add(new Second(new Date(1110421830000L)), 152);
        s1.add(new Second(new Date(1110422080000L)), 148);
        s1.add(new Second(new Date(1110422330000L)), 141);
        s1.add(new Second(new Date(1110422580000L)), 140);
        s1.add(new Second(new Date(1110422830000L)), 145);
        s1.add(new Second(new Date(1110423080000L)), 152);
        s1.add(new Second(new Date(1110423330000L)), 152);
        s1.add(new Second(new Date(1110423580000L)), 143);
        s1.add(new Second(new Date(1110423830000L)), 164);
        s1.add(new Second(new Date(1110424080000L)), 177);
        s1.add(new Second(new Date(1110424330000L)), 161);
        s1.add(new Second(new Date(1110424580000L)), 166);
        s1.add(new Second(new Date(1110424830000L)), 177);
        s1.add(new Second(new Date(1110425080000L)), 177);
        s1.add(new Second(new Date(1110425330000L)), 183);
        s1.add(new Second(new Date(1110425580000L)), 196);
        s1.add(new Second(new Date(1110425830000L)), 198);
        s1.add(new Second(new Date(1110426080000L)), 197);
        s1.add(new Second(new Date(1110426330000L)), 189);
        s1.add(new Second(new Date(1110426580000L)), 201);
        s1.add(new Second(new Date(1110426830000L)), 193);
        s1.add(new Second(new Date(1110427080000L)), 200);
        s1.add(new Second(new Date(1110427330000L)), 210);
        s1.add(new Second(new Date(1110427580000L)), 214);
        s1.add(new Second(new Date(1110427830000L)), 204);
        s1.add(new Second(new Date(1110428080000L)), 196);
        s1.add(new Second(new Date(1110428330000L)), 199);
        s1.add(new Second(new Date(1110428580000L)), 202);
        s1.add(new Second(new Date(1110428830000L)), 215);
        s1.add(new Second(new Date(1110429080000L)), 220);
        s1.add(new Second(new Date(1110429330000L)), 236);
        s1.add(new Second(new Date(1110429580000L)), 238);
        s1.add(new Second(new Date(1110429830000L)), 246);
        s1.add(new Second(new Date(1110430080000L)), 240);
        s1.add(new Second(new Date(1110430330000L)), 242);
        s1.add(new Second(new Date(1110430580000L)), 232);
        s1.add(new Second(new Date(1110430830000L)), 233);
        s1.add(new Second(new Date(1110431080000L)), 233);
        s1.add(new Second(new Date(1110431330000L)), 227);
        s1.add(new Second(new Date(1110431580000L)), 199);
        s1.add(new Second(new Date(1110431830000L)), 218);
        s1.add(new Second(new Date(1110432080000L)), 212);
        s1.add(new Second(new Date(1110432330000L)), 220);
        s1.add(new Second(new Date(1110432580000L)), 205);
        s1.add(new Second(new Date(1110432830000L)), 188);
        s1.add(new Second(new Date(1110433080000L)), 185);
        s1.add(new Second(new Date(1110433330000L)), 172);
        s1.add(new Second(new Date(1110433580000L)), 168);
        s1.add(new Second(new Date(1110433830000L)), 173);
        s1.add(new Second(new Date(1110434080000L)), 175);
        s1.add(new Second(new Date(1110434330000L)), 160);
        s1.add(new Second(new Date(1110434580000L)), 138);
        s1.add(new Second(new Date(1110434830000L)), 112);
        s1.add(new Second(new Date(1110435080000L)), 89);
        s1.add(new Second(new Date(1110435330000L)), 72);
        s1.add(new Second(new Date(1110435580000L)), 54);
        s1.add(new Second(new Date(1110435830000L)), 44);
        s1.add(new Second(new Date(1110436080000L)), 51);
        s1.add(new Second(new Date(1110436330000L)), 77);
        s1.add(new Second(new Date(1110436580000L)), 105);
        s1.add(new Second(new Date(1110436830000L)), 126);
        s1.add(new Second(new Date(1110437080000L)), 136);
        s1.add(new Second(new Date(1110437330000L)), 142);
        s1.add(new Second(new Date(1110437580000L)), 152);
        s1.add(new Second(new Date(1110437830000L)), 153);
        s1.add(new Second(new Date(1110438080000L)), 141);
        s1.add(new Second(new Date(1110438330000L)), 127);
        s1.add(new Second(new Date(1110438580000L)), 17);
        s1.add(new Second(new Date(1110438830000L)), 18);
        s1.add(new Second(new Date(1110439080000L)), 18);
        s1.add(new Second(new Date(1110439330000L)), 18);
        s1.add(new Second(new Date(1110439580000L)), 18);
        s1.add(new Second(new Date(1110439830000L)), 18);
        s1.add(new Second(new Date(1110440080000L)), 19);
        s1.add(new Second(new Date(1110440330000L)), 18);
        s1.add(new Second(new Date(1110440580000L)), 18);
        s1.add(new Second(new Date(1110440830000L)), 40);
        s1.add(new Second(new Date(1110441080000L)), 108);
        s1.add(new Second(new Date(1110441330000L)), 157);
        s1.add(new Second(new Date(1110441580000L)), 153);
        s1.add(new Second(new Date(1110441830000L)), 124);
        s1.add(new Second(new Date(1110442080000L)), 173);
        s1.add(new Second(new Date(1110442330000L)), 171);
        s1.add(new Second(new Date(1110442580000L)), 168);
        s1.add(new Second(new Date(1110442830000L)), 168);
        s1.add(new Second(new Date(1110443080000L)), 169);
        s1.add(new Second(new Date(1110443330000L)), 167);
        s1.add(new Second(new Date(1110443580000L)), 168);
        s1.add(new Second(new Date(1110443830000L)), 171);
        s1.add(new Second(new Date(1110444080000L)), 172);
        s1.add(new Second(new Date(1110444330000L)), 172);
        s1.add(new Second(new Date(1110444580000L)), 170);
        s1.add(new Second(new Date(1110444830000L)), 173);
        s1.add(new Second(new Date(1110445080000L)), 173);
        s1.add(new Second(new Date(1110445330000L)), 174);
        s1.add(new Second(new Date(1110445580000L)), 172);
        s1.add(new Second(new Date(1110445830000L)), 173);
        s1.add(new Second(new Date(1110446080000L)), 172);
        s1.add(new Second(new Date(1110446330000L)), 172);
        s1.add(new Second(new Date(1110446580000L)), 171);
        s1.add(new Second(new Date(1110446830000L)), 171);
        s1.add(new Second(new Date(1110447080000L)), 171);
        s1.add(new Second(new Date(1110447330000L)), 172);
        s1.add(new Second(new Date(1110447580000L)), 174);
        s1.add(new Second(new Date(1110447830000L)), 173);
        s1.add(new Second(new Date(1110448080000L)), 173);
        s1.add(new Second(new Date(1110448330000L)), 171);
        s1.add(new Second(new Date(1110448580000L)), 170);
        s1.add(new Second(new Date(1110448830000L)), 172);
        s1.add(new Second(new Date(1110449080000L)), 172);
        s1.add(new Second(new Date(1110449330000L)), 172);
        s1.add(new Second(new Date(1110449580000L)), 170);
        s1.add(new Second(new Date(1110449830000L)), 167);
        s1.add(new Second(new Date(1110450080000L)), 165);
        s1.add(new Second(new Date(1110450330000L)), 166);
        s1.add(new Second(new Date(1110450580000L)), 165);
        s1.add(new Second(new Date(1110450830000L)), 163);
        s1.add(new Second(new Date(1110451080000L)), 163);
        s1.add(new Second(new Date(1110451330000L)), 160);
        s1.add(new Second(new Date(1110451580000L)), 160);
        s1.add(new Second(new Date(1110451830000L)), 160);
        s1.add(new Second(new Date(1110452080000L)), 160);
        s1.add(new Second(new Date(1110452330000L)), 159);
        s1.add(new Second(new Date(1110452580000L)), 159);
        s1.add(new Second(new Date(1110452830000L)), 158);
        s1.add(new Second(new Date(1110453080000L)), 158);
        s1.add(new Second(new Date(1110453330000L)), 159);
        s1.add(new Second(new Date(1110453580000L)), 159);
        s1.add(new Second(new Date(1110453830000L)), 158);
        s1.add(new Second(new Date(1110454080000L)), 154);
        s1.add(new Second(new Date(1110454330000L)), 151);
        s1.add(new Second(new Date(1110454580000L)), 151);
        s1.add(new Second(new Date(1110454830000L)), 102);
        s1.add(new Second(new Date(1110455080000L)), 90);
        s1.add(new Second(new Date(1110455330000L)), 95);
        s1.add(new Second(new Date(1110455580000L)), 102);
        s1.add(new Second(new Date(1110455830000L)), 107);
        s1.add(new Second(new Date(1110456080000L)), 113);
        s1.add(new Second(new Date(1110456330000L)), 119);
        s1.add(new Second(new Date(1110456580000L)), 123);
        s1.add(new Second(new Date(1110456830000L)), 127);
        s1.add(new Second(new Date(1110457080000L)), 131);
        s1.add(new Second(new Date(1110457330000L)), 134);
        s1.add(new Second(new Date(1110457580000L)), 137);
        s1.add(new Second(new Date(1110457830000L)), 138);
        s1.add(new Second(new Date(1110458080000L)), 140);
        s1.add(new Second(new Date(1110458330000L)), 142);
        s1.add(new Second(new Date(1110458580000L)), 142);
        s1.add(new Second(new Date(1110458830000L)), 141);
        s1.add(new Second(new Date(1110459080000L)), 144);
        s1.add(new Second(new Date(1110459330000L)), 145);
        s1.add(new Second(new Date(1110459580000L)), 146);
        s1.add(new Second(new Date(1110459830000L)), 149);
        s1.add(new Second(new Date(1110460080000L)), 149);
        s1.add(new Second(new Date(1110460330000L)), 149);
        s1.add(new Second(new Date(1110460580000L)), 150);
        s1.add(new Second(new Date(1110460830000L)), 152);
        s1.add(new Second(new Date(1110461080000L)), 151);
        s1.add(new Second(new Date(1110461330000L)), 151);
        s1.add(new Second(new Date(1110461580000L)), 151);
        s1.add(new Second(new Date(1110461830000L)), 151);
        s1.add(new Second(new Date(1110462080000L)), 151);
        s1.add(new Second(new Date(1110462330000L)), 153);
        s1.add(new Second(new Date(1110462580000L)), 152);
        s1.add(new Second(new Date(1110462830000L)), 152);
        s1.add(new Second(new Date(1110463080000L)), 152);
        s1.add(new Second(new Date(1110463330000L)), 151);
        s1.add(new Second(new Date(1110463580000L)), 152);
        s1.add(new Second(new Date(1110463830000L)), 152);
        s1.add(new Second(new Date(1110464080000L)), 152);
        s1.add(new Second(new Date(1110464330000L)), 151);
        s1.add(new Second(new Date(1110464580000L)), 150);
        s1.add(new Second(new Date(1110464830000L)), 148);
        s1.add(new Second(new Date(1110465080000L)), 147);
        s1.add(new Second(new Date(1110465330000L)), 145);
        s1.add(new Second(new Date(1110465580000L)), 145);
        s1.add(new Second(new Date(1110465830000L)), 145);
        s1.add(new Second(new Date(1110466080000L)), 146);
        s1.add(new Second(new Date(1110466330000L)), 147);
        s1.add(new Second(new Date(1110466580000L)), 147);
        s1.add(new Second(new Date(1110466830000L)), 145);
        s1.add(new Second(new Date(1110467080000L)), 143);
        s1.add(new Second(new Date(1110467330000L)), 142);
        s1.add(new Second(new Date(1110467580000L)), 141);
        s1.add(new Second(new Date(1110467830000L)), 143);
        s1.add(new Second(new Date(1110468080000L)), 144);
        s1.add(new Second(new Date(1110468330000L)), 143);
        s1.add(new Second(new Date(1110468580000L)), 143);
        s1.add(new Second(new Date(1110468830000L)), 141);
        s1.add(new Second(new Date(1110469080000L)), 140);
        s1.add(new Second(new Date(1110469330000L)), 140);
        s1.add(new Second(new Date(1110469580000L)), 141);
        s1.add(new Second(new Date(1110469830000L)), 143);
        s1.add(new Second(new Date(1110470080000L)), 146);
        s1.add(new Second(new Date(1110470330000L)), 150);
        s1.add(new Second(new Date(1110470580000L)), 153);
        s1.add(new Second(new Date(1110470830000L)), 155);
        s1.add(new Second(new Date(1110471080000L)), 157);
        s1.add(new Second(new Date(1110471330000L)), 156);
        s1.add(new Second(new Date(1110471580000L)), 154);
        s1.add(new Second(new Date(1110471830000L)), 155);
        s1.add(new Second(new Date(1110472080000L)), 153);
        s1.add(new Second(new Date(1110472330000L)), 150);
        s1.add(new Second(new Date(1110472580000L)), 145);
        s1.add(new Second(new Date(1110472830000L)), 144);
        s1.add(new Second(new Date(1110473080000L)), 148);
        s1.add(new Second(new Date(1110473330000L)), 152);
        s1.add(new Second(new Date(1110473580000L)), 154);
        s1.add(new Second(new Date(1110473830000L)), 157);
        s1.add(new Second(new Date(1110474080000L)), 160);
        s1.add(new Second(new Date(1110474330000L)), 159);
        s1.add(new Second(new Date(1110474580000L)), 161);
        s1.add(new Second(new Date(1110474830000L)), 156);
        s1.add(new Second(new Date(1110475080000L)), 142);
        s1.add(new Second(new Date(1110475330000L)), 140);
        s1.add(new Second(new Date(1110475580000L)), 136);
        s1.add(new Second(new Date(1110475830000L)), 127);
        s1.add(new Second(new Date(1110476080000L)), 113);
        s1.add(new Second(new Date(1110476330000L)), 105);
        s1.add(new Second(new Date(1110476580000L)), 107);
        s1.add(new Second(new Date(1110476830000L)), 103);
        s1.add(new Second(new Date(1110477080000L)), 97);
        s1.add(new Second(new Date(1110477330000L)), 102);
        s1.add(new Second(new Date(1110477580000L)), 111);
        s1.add(new Second(new Date(1110477830000L)), 127);
        s1.add(new Second(new Date(1110478080000L)), 149);
        s1.add(new Second(new Date(1110478330000L)), 166);
        s1.add(new Second(new Date(1110478580000L)), 181);
        s1.add(new Second(new Date(1110478830000L)), 189);
        s1.add(new Second(new Date(1110479080000L)), 203);
        s1.add(new Second(new Date(1110479330000L)), 224);
        s1.add(new Second(new Date(1110479580000L)), 234);
        s1.add(new Second(new Date(1110479830000L)), 237);
        s1.add(new Second(new Date(1110480080000L)), 229);
        s1.add(new Second(new Date(1110480330000L)), 228);
        s1.add(new Second(new Date(1110480580000L)), 222);
        s1.add(new Second(new Date(1110480830000L)), 205);
        s1.add(new Second(new Date(1110481080000L)), 222);
        s1.add(new Second(new Date(1110481330000L)), 226);
        s1.add(new Second(new Date(1110481580000L)), 222);
        s1.add(new Second(new Date(1110481830000L)), 229);
        s1.add(new Second(new Date(1110482080000L)), 247);
        s1.add(new Second(new Date(1110482330000L)), 240);
        s1.add(new Second(new Date(1110482580000L)), 251);
        s1.add(new Second(new Date(1110482830000L)), 240);
        s1.add(new Second(new Date(1110483080000L)), 242);
        s1.add(new Second(new Date(1110483330000L)), 241);
        s1.add(new Second(new Date(1110483580000L)), 250);
        s1.add(new Second(new Date(1110483830000L)), 255);
        s1.add(new Second(new Date(1110484080000L)), 255);
        s1.add(new Second(new Date(1110484330000L)), 255);
        s1.add(new Second(new Date(1110484580000L)), 255);
        s1.add(new Second(new Date(1110484830000L)), 255);
        s1.add(new Second(new Date(1110485080000L)), 255);
        s1.add(new Second(new Date(1110485330000L)), 254);
        s1.add(new Second(new Date(1110485580000L)), 253);
        s1.add(new Second(new Date(1110485830000L)), 254);
        s1.add(new Second(new Date(1110486080000L)), 59);

        // We may as well use the default TimeZone
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

        dataset.setDomainIsPointsInTime(true);

        return (dataset);
        }
    }
