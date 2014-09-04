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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * A ProcessedDataUIComponent which can display Timestamped or plain data channels.
 */

public final class ProcessedDataUIComponent extends AbstractXYDatasetUIComponent
                                            implements ObservatoryConstants
    {
    /***********************************************************************************************
     * Construct a ProcessedDataUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     * @param name
     * @param displaylimit
     */

    public ProcessedDataUIComponent(final TaskPlugin task,
                                    final ObservatoryInstrumentInterface hostinstrument,
                                    final String resourcekey,
                                    final String name,
                                    final int displaylimit)
        {
        super(task,
              hostinstrument,
              resourcekey,
              name,
              displaylimit,
              ReportTableToolbar.HORIZ_NORTH_RNG_PRT_RF_RV_TV_DA,
              RegistryModelUtilities.getCommonIcon(XY_DATA_ICON_FILENAME));
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "ProcessedDataUIComponent.generateReport()";
        final Vector<Vector> vecReport;

        vecReport = new Vector<Vector>();

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + " [isselectedinstrument="
                                    + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                    + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        // Only generate a Report if this UIComponent is visible
        if ((UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            && (getXyDataset() != null)
            && (getXyDataset().getSeriesCount() > 0)
            && (getDataViewMode() != null)
            && (getDataViewLimit() > 0))
            {
            if ((getDatasetType().getName().equals(DatasetType.XY.getName()))
                && (getXyDataset() instanceof XYSeriesCollection))
                {
                final XYSeriesCollection collection;

                // There should be a collection of <channelcount> XYSeries in the Dataset
                collection = (XYSeriesCollection) getXyDataset();

                if ((collection != null)
                    && (collection.getSeriesCount() > 0)
                    && (collection.getSeriesCount() == getChannelCount())
                    && (collection.getSeries() != null))
                    {
                    final List listSeries;
                    final int intDataViewStartIndex;
                    final int intDataViewCount;
                    int intMaximumItemCount;
                    int intReferenceSeriesIndex;

                    // Retrieve the XYSeries as a List
                    listSeries = collection.getSeries();

                    // Some Series may have fewer items, if the Series has been processed in some way
                    // So search for the Series with the greatest number of items, and use that as the
                    // time reference when retrieving samples for the report
                    // Pad missing items with blanks

                    intMaximumItemCount = 0;
                    intReferenceSeriesIndex = -1;

                    for (int intSeriesIndex = 0;
                         intSeriesIndex < listSeries.size();
                         intSeriesIndex++)
                        {
                        final XYSeries series;

                        series = (XYSeries) listSeries.get(intSeriesIndex);

                        if ((series != null)
                            && (series.getItemCount() > intMaximumItemCount))
                            {
                            intMaximumItemCount = series.getItemCount();
                            intReferenceSeriesIndex= intSeriesIndex;
                            }
                        }

                    switch (getDataViewMode())
                        {
                        case SHOW_ALL:
                            {
                            intDataViewStartIndex = 0;
                            intDataViewCount = intMaximumItemCount;
                            vecReport.ensureCapacity(intDataViewCount);

                            break;
                            }

                        case SHOW_FIRST:
                            {
                            intDataViewStartIndex = 0;
                            if (intMaximumItemCount > getDataViewLimit())
                                {
                                intDataViewCount = getDataViewLimit();
                                }
                            else
                                {
                                intDataViewCount = intMaximumItemCount;
                                }

                            vecReport.ensureCapacity(intDataViewCount);

                            break;
                            }

                        case SHOW_LAST:
                            {
                            if (intMaximumItemCount > getDataViewLimit())
                                {
                                intDataViewStartIndex = intMaximumItemCount - getDataViewLimit();
                                }
                            else
                                {
                                intDataViewStartIndex = 0;
                                }
                            intDataViewCount = Math.min(intMaximumItemCount, getDataViewLimit());
                            vecReport.ensureCapacity(intDataViewCount);

                            break;
                            }

                        default:
                            {
                            LOGGER.error(SOURCE + "Unsupported DataViewMode");

                            // Just SHOW_ALL for simplicity
                            intDataViewStartIndex = 0;
                            intDataViewCount = intMaximumItemCount;
                            vecReport.ensureCapacity(intDataViewCount);
                            }
                        }

                    // Generate each Row of the Report
                    // Index X-value Channel0  Channel1  Channel2 ...
                    for (int intRowIndex = intDataViewStartIndex;
                         intRowIndex < (intDataViewStartIndex + intDataViewCount);
                         intRowIndex++)
                        {
                        final Vector<Object> vecRow;
                        final XYDataItem itemReferenceSeriesRow;

                        vecRow = new Vector<Object>(getChannelCount() + 2);

                        // Use the Reference XYSeries to provide the Index for each Row
                        itemReferenceSeriesRow = ((XYSeries)listSeries.get(intReferenceSeriesIndex)).getDataItem(intRowIndex);

                        // Remember that all data entries must be Strings
                        // Index  X-value
                        vecRow.add(Integer.toString(intRowIndex));
                        vecRow.add(itemReferenceSeriesRow.getX().toString());

                        // Step across each XYSeries (channel) to make up the columns
                        for (int intChannelIndex = 0;
                             intChannelIndex < getChannelCount();
                             intChannelIndex++)
                            {
                            final XYDataItem item;

                            item = ((XYSeries)listSeries.get(intChannelIndex)).getDataItem(intRowIndex);

                            if (item != null)
                                {
                                vecRow.add(item.getY().toString());
                                }
                            else
                                {
                                vecRow.add(SPACE);
                                }
                            }

                        vecReport.add(vecRow);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The XYSeriesCollection does not have the correct number of Series");
                    }
                }
            else if ((getDatasetType().getName().equals(DatasetType.TIMESTAMPED.getName()))
                && (getXyDataset() instanceof TimeSeriesCollection))
                {
                final TimeSeriesCollection collection;

                // There should be a collection of <channelcount> TimeSeries in the Dataset
                collection = (TimeSeriesCollection) getXyDataset();

                if ((collection != null)
                    && (collection.getSeriesCount() > 0)
                    && (collection.getSeriesCount() == getChannelCount())
                    && (collection.getSeries() != null)
                    && (getDataViewMode() != null))
                    {
                    final List listSeries;
                    final Calendar calendar;
                    final int intDataViewStartIndex;
                    final int intDataViewCount;
                    int intMaximumItemCount;
                    int intReferenceSeriesIndex;

                    // Retrieve the TimeSeries as a List
                    listSeries = collection.getSeries();

                    // Some Series may have fewer items, if the Series has been processed in some way
                    // So search for the Series with the greatest number of items, and use that as the
                    // time reference when retrieving samples for the report
                    // Pad missing items with blanks

                    intMaximumItemCount = 0;
                    intReferenceSeriesIndex = -1;

                    for (int intSeriesIndex = 0;
                         intSeriesIndex < listSeries.size();
                         intSeriesIndex++)
                        {
                        final TimeSeries series;

                        series = (TimeSeries) listSeries.get(intSeriesIndex);

                        if ((series != null)
                            && (series.getItemCount() > intMaximumItemCount))
                            {
                            intMaximumItemCount = series.getItemCount();
                            intReferenceSeriesIndex= intSeriesIndex;
                            }
                        }

                    switch (getDataViewMode())
                        {
                        case SHOW_ALL:
                            {
                            intDataViewStartIndex = 0;
                            intDataViewCount = intMaximumItemCount;
                            vecReport.ensureCapacity(intDataViewCount);

                            break;
                            }

                        case SHOW_FIRST:
                            {
                            intDataViewStartIndex = 0;
                            if (intMaximumItemCount > getDataViewLimit())
                                {
                                intDataViewCount = getDataViewLimit();
                                }
                            else
                                {
                                intDataViewCount = intMaximumItemCount;
                                }

                            vecReport.ensureCapacity(intDataViewCount);

                            break;
                            }

                        case SHOW_LAST:
                            {
                            if (intMaximumItemCount > getDataViewLimit())
                                {
                                intDataViewStartIndex = intMaximumItemCount - getDataViewLimit();
                                }
                            else
                                {
                                intDataViewStartIndex = 0;
                                }
                            intDataViewCount = Math.min(intMaximumItemCount, getDataViewLimit());
                            vecReport.ensureCapacity(intDataViewCount);

                            break;
                            }

                        default:
                            {
                            LOGGER.error(SOURCE + "Unsupported DataViewMode");

                            // Just SHOW_ALL for simplicity
                            intDataViewStartIndex = 0;
                            intDataViewCount = intMaximumItemCount;
                            vecReport.ensureCapacity(intDataViewCount);
                            }
                        }

                    // A Calendar workspace
                    calendar = new GregorianCalendar();

                    // Generate each Row of the Report
                    // Index  Date  Time Channel0  Channel1  Channel2 ...
                    for (int intRowIndex = intDataViewStartIndex;
                         intRowIndex < (intDataViewStartIndex + intDataViewCount);
                         intRowIndex++)
                        {
                        final Vector<Object> vecRow;
                        final TimeSeriesDataItem itemReferenceSeriesRow;
                        final RegularTimePeriod periodReferenceSeriesRow;

                        vecRow = new Vector<Object>(getChannelCount() + 3);

                        // TODO This gives BST error!

                        // Use the Reference TimeSeries to provide the Data and Time for each Row
                        // Get the TimeSeriesDataItem, which is dependent on the TimeZone
                        itemReferenceSeriesRow = ((TimeSeries)listSeries.get(intReferenceSeriesIndex)).getDataItem(intRowIndex);
                        periodReferenceSeriesRow = itemReferenceSeriesRow.getPeriod();
                        calendar.setTimeInMillis(periodReferenceSeriesRow.getStart().getTime());

                        // Remember that all data entries must be Strings
                        // Index  Date  Time
                        vecRow.add(Integer.toString(intRowIndex));
                        vecRow.add(ChronosHelper.toDateString(calendar));
                        vecRow.add(ChronosHelper.toTimeString(calendar));

                        // Step across each TimeSeries (channel) to make up the columns
                        // with data for the *same* RegularTimePeriod as the Reference Series

                        for (int intChannelIndex = 0;
                             intChannelIndex < getChannelCount();
                             intChannelIndex++)
                            {
                            final TimeSeriesDataItem item;

                            // This returns NULL if there is no entry for that TimePeriod
                            item = ((TimeSeries)listSeries.get(intChannelIndex)).getDataItem(periodReferenceSeriesRow);

                            if (item != null)
                                {
                                vecRow.add(item.getValue().toString());
                                }
                            else
                                {
                                vecRow.add(SPACE);
                                }
                            }

                        vecReport.add(vecRow);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The TimeSeriesCollection does not have the correct number of Series");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + " The Dataset is an invalid type");
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + " NOT VISIBLE");
            }

        return (vecReport);
        }
    }
