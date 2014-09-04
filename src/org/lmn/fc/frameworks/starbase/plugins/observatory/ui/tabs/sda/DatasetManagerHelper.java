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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;


import org.jfree.data.UnknownKeyException;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetManagerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SuperposedDataAnalyserUIComponentInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.*;


/***************************************************************************************************
 * DatasetManagerHelper.
 */

public final class DatasetManagerHelper implements FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkRegex,
                                                   FrameworkSingletons
    {
    /***********************************************************************************************
     * Rebuild the Composite Chart using the current set of Attached DAOs.
     *
     * @param datasetmanager
     * @param sdaui
     * @param datasettypechanged
     * @param channelcountchanged
     * @param channelselectionchanged
     * @param datasetdomainchanged
     * @param metadatachanged
     * @param rawdatachanged
     * @param processeddatachanged
     * @param event
     * @param debug
     *
     * @return boolean
     */

    public static synchronized boolean rebuildComposite(final DatasetManagerInterface datasetmanager,
                                                        final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                        final boolean datasettypechanged,
                                                        final boolean channelcountchanged,
                                                        final boolean channelselectionchanged,
                                                        final boolean datasetdomainchanged,
                                                        final boolean metadatachanged,
                                                        final boolean rawdatachanged,
                                                        final boolean processeddatachanged,
                                                        final EventObject event,
                                                        final boolean debug)
        {
        final String SOURCE = "DatasetManagerHelper.rebuildComposite() ";
        final ObservatoryInstrumentDAOInterface daoComposite;
        final List<ObservatoryInstrumentDAOInterface> listAttachedDAOs;
        int intCompositeChannelCount;
        int intSeriesChannelCount;
        boolean boolCompositeTemperatureChannel;
        XYDataset xyCompositeDataset;
        final List<Metadata> listAggregateCompositeDAOMetadata;
        boolean boolSuccess;

        LOGGER.debug(debug,
                     SOURCE + "[dataset_type.changed=" + datasettypechanged
                     + "] [channel.count.changed=" + channelcountchanged
                     + "] [channel_selection.changed=" + channelselectionchanged
                     + "] [dataset_domain.changed=" + datasetdomainchanged
                     + "] [metadata.changed=" + metadatachanged
                     + "] [raw_data.changed=" + rawdatachanged
                     + "] [processed_data.changed=" + processeddatachanged
                     + "] [event.source=" + event.getSource().getClass().getName()
                     + "]");

        daoComposite = sdaui.getCompositeDAO();
        listAttachedDAOs = datasetmanager.getAttachedDAOs();

        // Set some defaults, which will never be used
        intCompositeChannelCount = 0;
        intSeriesChannelCount = 0;
        boolCompositeTemperatureChannel = false;
        xyCompositeDataset = null;

        //-----------------------------------------------------------------------------------------
        // Composite Metadata

        if ((datasettypechanged)
            || (channelcountchanged)
            || (channelselectionchanged)
            || (datasetdomainchanged))
            {
            // Start again from scratch
            listAggregateCompositeDAOMetadata = new ArrayList<Metadata>(100);
            }
        else
            {
            // Collect any existing Metadata from the Composite DAO (and nothing else)
            // If this is empty, then it will all be re-collected
            listAggregateCompositeDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(null,
                                                                                              null,
                                                                                              null,
                                                                                              daoComposite,
                                                                                              null,
                                                                                              SOURCE,
                                                                                              debug);
            }

        //-----------------------------------------------------------------------------------------
        // Composite Data
        // Start again by clearing the Composite data
        // Supported datasets: XYSeriesCollection and TimeSeriesCollection

        if (DatasetType.XY.equals(datasetmanager.getDatasetType()))
            {
            if ((daoComposite.getXYDataset() != null)
                && (daoComposite.getXYDataset() instanceof XYSeriesCollection))
                {
                // Use the existing Composite Collection if we can
                ((XYSeriesCollection)daoComposite.getXYDataset()).removeAllSeries();
                xyCompositeDataset = daoComposite.getXYDataset();
                }
            else
                {
                xyCompositeDataset = new XYSeriesCollection();
                }

            boolSuccess = true;
            }
        else if (DatasetType.TIMESTAMPED.equals(datasetmanager.getDatasetType()))
            {
            if ((daoComposite.getXYDataset() != null)
                && (daoComposite.getXYDataset() instanceof TimeSeriesCollection))
                {
                // Use the existing Composite Collection if we can
                ((TimeSeriesCollection)daoComposite.getXYDataset()).removeAllSeries();
                xyCompositeDataset = daoComposite.getXYDataset();
                }
            else
                {
                xyCompositeDataset = new TimeSeriesCollection();
                }

            boolSuccess = true;
            }
        else
            {
            LOGGER.error(SOURCE + "Unexpected DatasetType [type=" + datasetmanager.getDatasetType() + "]");
            boolSuccess = false;
            }

        //-----------------------------------------------------------------------------------------
        // At this point we have the original Composite Metadata (if any) and an empty Series collection
        //-----------------------------------------------------------------------------------------
        // Primary and Secondaries
        // Process all DAOs currently attached to the Composite

        for (int intAttachedDAOIndex = 0;
             ((boolSuccess)
              && (intAttachedDAOIndex < listAttachedDAOs.size()));
             intAttachedDAOIndex++)
            {
            final ObservatoryInstrumentDAOInterface daoAttached;

            daoAttached = listAttachedDAOs.get(intAttachedDAOIndex);

            if (daoAttached != null)
                {
                final int intDatasetIndex;
                final double dblOffsetForDAO;
                int intDAOSeriesCount;

                intDatasetIndex = datasetmanager.getDatasetIndex(daoAttached);
                dblOffsetForDAO = datasetmanager.getSecondaryOffsets().get(intDatasetIndex);

                // Firstly, fill up the DAO as though it has imported the data
                // We only need the XYDataset, we'll never see the RawData
                // Filtering to ProcessedData has already occurred

                if ((DatasetType.XY.equals(datasetmanager.getDatasetType()))
                    && (daoAttached.getXYDataset() instanceof XYSeriesCollection)
                    && (xyCompositeDataset instanceof XYSeriesCollection))
                    {
                    try
                        {
                        final List<XYSeries> listXYSeriesOriginal;

                        // JFreeChart doesn't do Generics!
                        listXYSeriesOriginal = ((XYSeriesCollection)daoAttached.getXYDataset()).getSeries();

                        for (int intSeriesIndex = 0;
                             intSeriesIndex < listXYSeriesOriginal.size();
                             intSeriesIndex++)
                            {
                            final XYSeries xySeriesOriginal;
                            final XYSeries xySeriesCopy;

                            xySeriesOriginal = listXYSeriesOriginal.get(intSeriesIndex);
                            xySeriesCopy = (XYSeries)xySeriesOriginal.clone();

                            // Set the Key to be the same as the renamed Channel.Name
                            xySeriesCopy.setKey(SuperposedDataAnalyserMetadataHelper.buildChannelName(intAttachedDAOIndex,
                                                                                                      intSeriesIndex,
                                                                                                      xySeriesOriginal.getKey(),
                                                                                                      dblOffsetForDAO));
                            // The first dataset is always the Primary, and is never offset
                            if (intAttachedDAOIndex == 0)
                                {
                                ((XYSeriesCollection)xyCompositeDataset).addSeries(xySeriesCopy);
                                }
                            else
                                {
                                final XYSeries xySeriesOffset;
                                final double dblOffset;

                                // Be very cautious that we have a sensible Time Series
                                if (xySeriesCopy.getItemCount() > 0)
                                    {
                                    final double dblSeriesStartX;
                                    final double dblSeriesEndX;

                                    dblSeriesStartX = xySeriesCopy.getDataItem(0).getXValue();
                                    dblSeriesEndX = xySeriesCopy.getDataItem(xySeriesCopy.getItemCount()-1).getXValue();

                                    dblOffset = ((dblSeriesEndX - dblSeriesStartX)
                                                 * (dblOffsetForDAO
                                                    / (double) ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MAXIMUM));
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Unable to offset the XY Series");
                                    dblOffset = 0.0;
                                    }

                                xySeriesOffset = new XYSeries(xySeriesCopy.getKey());

                                // Apply the Offset to all items in the Series
                                for (int intItemIndex = 0;
                                     intItemIndex < xySeriesCopy.getItemCount();
                                     intItemIndex++)
                                    {
                                    final XYDataItem item;

                                    item = xySeriesCopy.getDataItem(intItemIndex);

                                    xySeriesOffset.addOrUpdate(item.getXValue() + dblOffset,
                                                               item.getYValue());
                                    }

                                ((XYSeriesCollection)xyCompositeDataset).addSeries(xySeriesOffset);
                                }

                            LOGGER.debug(debug,
                                         SOURCE + "xy [series.index=" + intSeriesIndex
                                         + "] [series.count=" + intSeriesChannelCount
                                         + "] [lower.bound.includeinterval=" + ((XYSeriesCollection)xyCompositeDataset).getDomainLowerBound(true)
                                         + "] [lower.bound.nointerval=" + ((XYSeriesCollection)xyCompositeDataset).getDomainLowerBound(false)
                                         + "] [upper.bound.includeinterval=" + ((XYSeriesCollection)xyCompositeDataset).getDomainUpperBound(true)
                                         + "] [upper.bound.nointerval=" + ((XYSeriesCollection)xyCompositeDataset).getDomainUpperBound(false)
                                         + "]");

                            intSeriesChannelCount++;
                            }

                        intDAOSeriesCount = listXYSeriesOriginal.size();
                        }

                    catch (final UnknownKeyException exception)
                        {
                        LOGGER.error(SOURCE + "XYSeries has an unknown key");
                        exception.printStackTrace();
                        intDAOSeriesCount = 0;
                        }

                    catch (final CloneNotSupportedException exception)
                        {
                        LOGGER.error(SOURCE + "XYSeries could not be cloned");
                        intDAOSeriesCount = 0;
                        }
                    }
                else if ((DatasetType.TIMESTAMPED.equals(datasetmanager.getDatasetType()))
                         && (daoAttached.getXYDataset() instanceof TimeSeriesCollection)
                         && (xyCompositeDataset instanceof TimeSeriesCollection))
                    {
                    try
                        {
                        final List<TimeSeries> listTimeSeriesOriginal;
                        final TimeZone timeZone;
                        final Locale locale;
                        final Date dateOffset;

                        // JFreeChart doesn't do Generics!
                        listTimeSeriesOriginal = ((TimeSeriesCollection)daoAttached.getXYDataset()).getSeries();

                        // ToDo REVIEW - should these be for the Observatory?
                        timeZone = REGISTRY.getFrameworkTimeZone();
                        locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                            REGISTRY.getFramework().getCountryISOCode());

                        dateOffset = new Date();

                        for (int intSeriesIndex = 0;
                             intSeriesIndex < listTimeSeriesOriginal.size();
                             intSeriesIndex++)
                            {
                            final TimeSeries timeSeriesOriginal;
                            final TimeSeries timeSeriesCopy;

                            timeSeriesOriginal = listTimeSeriesOriginal.get(intSeriesIndex);
                            timeSeriesCopy = (TimeSeries)timeSeriesOriginal.clone();

                            // Set the Key to be the same as the renamed Channel.Name
                            timeSeriesCopy.setKey(SuperposedDataAnalyserMetadataHelper.buildChannelName(intAttachedDAOIndex,
                                                                                                        intSeriesIndex,
                                                                                                        timeSeriesOriginal.getKey(),
                                                                                                        dblOffsetForDAO));
                            // The first dataset is always the Primary, and is never offset
                            if (intAttachedDAOIndex == 0)
                                {
                                ((TimeSeriesCollection)xyCompositeDataset).addSeries(timeSeriesCopy);
                                }
                            else
                                {
                                final TimeSeries timeSeriesOffset;
                                final long longOffsetMillis;

                                // Be very cautious that we have a sensible Time Series
                                if ((timeSeriesCopy.getItemCount() > 0)
                                    && (timeSeriesCopy.getTimePeriod(timeSeriesCopy.getItemCount()-1).getEnd().after(timeSeriesCopy.getTimePeriod(0).getStart())))
                                    {
                                    final long longSeriesStart;
                                    final long longSeriesEnd;

                                    longSeriesStart = timeSeriesCopy.getTimePeriod(0).getStart().getTime();
                                    longSeriesEnd = timeSeriesCopy.getTimePeriod(timeSeriesCopy.getItemCount()-1).getEnd().getTime();

                                    longOffsetMillis = (long)((double)(longSeriesEnd - longSeriesStart)
                                                              * (dblOffsetForDAO
                                                                 / (double)ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MAXIMUM));

                                    LOGGER.debug(debug,
                                                 SOURCE + "[start=" + longSeriesStart
                                                 + "msec] [end="
                                                 + longSeriesEnd + "msec] [offset=" + longOffsetMillis + "msec]");
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Unable to offset the Time Series");
                                    longOffsetMillis = 0L;
                                    }

                                timeSeriesOffset = new TimeSeries(timeSeriesCopy.getKey());

                                // Apply the Offset to all items in the Series
                                for (int intItemCount = 0;
                                     intItemCount < timeSeriesCopy.getItemCount();
                                     intItemCount++)
                                    {
                                    final RegularTimePeriod timePeriod;
                                    final RegularTimePeriod timePeriodOffset;

                                    timePeriod = timeSeriesCopy.getTimePeriod(intItemCount);
                                    dateOffset.setTime(timePeriod.getStart().getTime() + longOffsetMillis);

                                    LOGGER.debug(false,
                                                 SOURCE + "[time+offset=" + dateOffset + "msec]");

                                    // Create a TimePeriod of the same class as the original
                                    // Day, FixedMillisecond, Hour, Millisecond, Minute, Month, Quarter, Second, Week, Year
                                    // ToDo Review use of the Framework TimeZone
                                    timePeriodOffset = RegularTimePeriod.createInstance(timePeriod.getClass(),
                                                                                        dateOffset,
                                                                                        timeZone);
                                    timeSeriesOffset.add(timePeriodOffset, timeSeriesCopy.getValue(intItemCount));
                                    }

                                ((TimeSeriesCollection)xyCompositeDataset).addSeries(timeSeriesOffset);
                                }

                            intSeriesChannelCount++;

                            LOGGER.debug(debug,
                                         SOURCE + "timestamped [series.index=" + intSeriesIndex
                                         + "] [series.count=" + intSeriesChannelCount
                                         + "] [lower.bound.includeinterval=" + ((TimeSeriesCollection)xyCompositeDataset).getDomainLowerBound(true)
                                         + "] [lower.bound.nointerval=" + ((TimeSeriesCollection)xyCompositeDataset).getDomainLowerBound(false)
                                         + "] [upper.bound.includeinterval=" + ((TimeSeriesCollection)xyCompositeDataset).getDomainUpperBound(true)
                                         + "] [upper.bound.nointerval=" + ((TimeSeriesCollection)xyCompositeDataset).getDomainUpperBound(false)
                                         + "]");
                            }

                        intDAOSeriesCount = listTimeSeriesOriginal.size();
                        }

                    catch (final UnknownKeyException exception)
                        {
                        LOGGER.error(SOURCE + "TimeSeries has an unknown key");
                        exception.printStackTrace();
                        intDAOSeriesCount = 0;
                        }

                    catch (final CloneNotSupportedException exception)
                        {
                        LOGGER.error(SOURCE + "TimeSeries could not be cloned");
                        intDAOSeriesCount = 0;
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Unexpected DatasetType [type=" + datasetmanager.getDatasetType() + "]");
                    intDAOSeriesCount = 0;
                    }

                // Add any Channel Metadata from the specified DAO appropriate for a Chart
                // which is *missing* from the specified Composite collection,
                // renaming any Observation.Channel Keys starting at the current CompositeChannelCount,
                // to ensure all Channels of the Composite have unique ChannelIDs.
                // Do nothing if the Metadata Key is already present in the Composite collection,
                // since we wish to preserve any edits by the User.
                SuperposedDataAnalyserMetadataHelper.collectAndRenameChannelMetadataFromAttachedDAO(listAggregateCompositeDAOMetadata,
                                                                                                    daoAttached,
                                                                                                    intAttachedDAOIndex,
                                                                                                    intCompositeChannelCount,
                                                                                                    dblOffsetForDAO,
                                                                                                    event,
                                                                                                    debug);
                // We've assumed that all Channels have had corresponding Metadata
                intCompositeChannelCount += daoAttached.getRawDataChannelCount();

                // ToDo Review how multiple temperature channels will be handled!
                if (daoAttached.hasTemperatureChannel())
                    {
                    boolCompositeTemperatureChannel = true;
                    }

                boolSuccess = (intSeriesChannelCount == intCompositeChannelCount);
                }
            else
                {
                LOGGER.error(SOURCE + "Attached DAO was NULL");
                }
            }

        if (boolSuccess)
            {
            // Prepare the DAO
            // This must do the same job as setWrappedData() -- > setXyDataset() in the InstrumentUIComponentDecorator
            daoComposite.setRawDataChannelCount(intCompositeChannelCount);
            daoComposite.setTemperatureChannel(boolCompositeTemperatureChannel);
            daoComposite.setXYDataset(xyCompositeDataset);

            daoComposite.setDatasetTypeChanged(datasettypechanged);
            daoComposite.setChannelCountChanged(channelcountchanged);
            daoComposite.setRawDataChanged(rawdatachanged);
            daoComposite.setProcessedDataChanged(processeddatachanged);

            // Add any missing Metadata to describe the Composite Chart, including the revised Observation.Channel.Count
            SuperposedDataAnalyserMetadataHelper.addCompositeChartMissingMetadata(listAggregateCompositeDAOMetadata,
                                                                                  datasetmanager.getDatasetType(),
                                                                                  datasetmanager.getDatasetType().getName(),
                                                                                  daoComposite.getRawDataChannelCount(),
                                                                                  debug);
            daoComposite.setMetadataChanged(metadatachanged);
            MetadataHelper.showMetadataList(listAggregateCompositeDAOMetadata,
                                            SOURCE + "CompositeDAOMetadata After collection of Attached Dataset Metadata, and creation of Chart Metadata",
                                            debug);

            // Reasons for complete rebuild of the Composite Chart
            //  DatasetTypeChanged
            //  ChannelCountChanged
            //  ChannelSelectionChanged
            //  DatasetDomainChanged
            //
            // Reasons to leave Chart alone, just update the data
            //  MetadataChanged
            //  RawDataChanged
            //  ProcessedDataChanged

            if (sdaui.getCompositeViewer().getChartViewer() != null)
                {
                sdaui.getCompositeViewer().getChartViewer().setChannelSelectionChanged(channelselectionchanged);
                sdaui.getCompositeViewer().getChartViewer().setDatasetDomainChanged(datasetdomainchanged);
                sdaui.getCompositeViewer().getChartViewer().setChannelCount(intCompositeChannelCount);

                // The Metadata are intended for the CompositeDAO, so apply
                sdaui.getCompositeViewer().getChartViewer().setMetadata(listAggregateCompositeDAOMetadata,
                                                                        daoComposite,
                                                                        true,
                                                                        debug);

                // This calls setDatasetType() and setDatasetDomainChanged()
                // and will reset the Range and Domain crosshairs
                // This *relies* on ChannelCount having been set by the caller!
                sdaui.getCompositeViewer().getChartViewer().setPrimaryXYDataset(daoComposite, daoComposite.getXYDataset());
                }

            // Show the Composite Metadata on the viewer
            // See similar code in SuperposedDataAnalyserHelper.showSelectedXYDatasetOnDatasetViewer()
            if ((sdaui.getCompositeViewer().isValidViewerUI())
                && (sdaui.getCompositeViewer().getMetadataViewer() != null)
                && (sdaui.getCompositeViewer().getMetadataViewer() instanceof MetadataExplorerUIComponentInterface))
                {
                final MetadataExplorerUIComponentInterface uiCompositeMetadataExplorer;

                uiCompositeMetadataExplorer = (MetadataExplorerUIComponentInterface)sdaui.getCompositeViewer().getMetadataViewer();

                // This resets the selection to the root
                uiCompositeMetadataExplorer.setMetadataList(listAggregateCompositeDAOMetadata);
                }

            // Indicate how many channels we could display
            if (sdaui.getCompositeViewer().getChannelsLabel() != null)
                {
                sdaui.getCompositeViewer().getChannelsLabel().setText(intCompositeChannelCount + " Channels");
                }

            // Allow viewing of Metadata, if any
            sdaui.getCompositeViewer().getMetadataButton().setEnabled(intCompositeChannelCount > 0);

            // Allow clearing all datasets, if any are present
            sdaui.getCompositeViewer().getDetachAllButton().setEnabled(intCompositeChannelCount > 0);

            // Allow Printing, if any chart present
            sdaui.getCompositeViewer().getPageSetupButton().setEnabled(intCompositeChannelCount > 0);
            sdaui.getCompositeViewer().getPrintButton().setEnabled(intCompositeChannelCount > 0);
            }
        else
            {
            // There are no data to display
            daoComposite.setRawDataChannelCount(0);
            daoComposite.setTemperatureChannel(false);
            daoComposite.setXYDataset(null);
            daoComposite.clearMetadata();

            daoComposite.setDatasetTypeChanged(datasettypechanged);
            daoComposite.setChannelCountChanged(channelcountchanged);

            sdaui.getCompositeViewer().getChartViewer().setChannelSelectionChanged(channelselectionchanged);
            sdaui.getCompositeViewer().getChartViewer().setDatasetDomainChanged(datasetdomainchanged);

            daoComposite.setMetadataChanged(metadatachanged);
            daoComposite.setRawDataChanged(rawdatachanged);
            daoComposite.setProcessedDataChanged(processeddatachanged);

            if (sdaui.getCompositeViewer().getChannelsLabel() != null)
                {
                sdaui.getCompositeViewer().getChannelsLabel().setText("0 Channels");
                }

            // Leave the DAO alone
            sdaui.getCompositeViewer().getChartViewer().setMetadata(null,
                                                                    daoComposite,
                                                                    false,
                                                                    debug);

            sdaui.getCompositeViewer().getChartViewer().setChannelCount(0);
            sdaui.getCompositeViewer().getChartViewer().setPrimaryXYDataset(daoComposite, null);
            }

        if (sdaui.getCompositeViewer().getExportButton() != null)
            {
            // If there's anything to detach, then it can also be exported
            sdaui.getCompositeViewer().getExportButton().setEnabled(sdaui.getDatasetManager().canDetachAll());
            }

        // Refresh regardless
        sdaui.getCompositeViewer().getChartViewer().refreshChart(daoComposite,
                                                                 true,
                                                                 SOURCE);

        return (boolSuccess);
        }
    }
