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

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * A RawDataUIComponent which can display Timestamped or plain data channels.
 */

public final class RawDataUIComponent extends AbstractVectorDataUIComponent
                                      implements ObservatoryConstants
    {
    /***********************************************************************************************
     * Construct a RawDataUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     * @param name
     * @param displaylimit
     */

    public RawDataUIComponent(final TaskPlugin task,
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
              RegistryModelUtilities.getCommonIcon(RAW_DATA_ICON_FILENAME));
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "RawDataUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        // Only generate a Report if this UIComponent is visible
        if ((UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            && (getRawData() != null)
            && (!getRawData().isEmpty())
            && (getDataViewMode() != null)
            && (getDataViewLimit() > 0))
            {
            final int intTotalItemCount;
            final int intDataViewStartIndex;
            final int intDataViewCount;
            int intSampleIndex;

            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "VISIBLE");

            intTotalItemCount = getRawData().size();

            // Prepare space for the report
            // We need a *separate* data structure to hold the row index also,
            // since this doesn't appear in the underlying 'internal' RawData
            vecReport = new Vector<Vector>();

            switch (getDataViewMode())
                {
                case SHOW_ALL:
                    {
                    // Take everything
                    intDataViewStartIndex = 0;
                    intDataViewCount = intTotalItemCount;
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;

                    vecReport.ensureCapacity(intDataViewCount);

                    break;
                    }

                case SHOW_FIRST:
                    {
                    // Only take the first samples
                    intDataViewStartIndex = 0;
                    if (intTotalItemCount > getDataViewLimit())
                        {
                        intDataViewCount = getDataViewLimit();
                        }
                    else
                        {
                        intDataViewCount = intTotalItemCount;
                        }
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;

                    vecReport.ensureCapacity(intDataViewCount);

                    break;
                    }

                case SHOW_LAST:
                    {
                    // Only take the most recent samples
                    if (intTotalItemCount > getDataViewLimit())
                        {
                        intDataViewStartIndex = intTotalItemCount - getDataViewLimit();
                        }
                    else
                        {
                        intDataViewStartIndex = 0;
                        }
                    intDataViewCount = Math.min(intTotalItemCount, getDataViewLimit());
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;

                    vecReport.ensureCapacity(intDataViewCount);

                    break;
                    }

                default:
                    {
                    LOGGER.error(SOURCE + "Unsupported DataViewMode");

                    // Just SHOW_ALL for simplicity
                    intDataViewStartIndex = 0;
                    intDataViewCount = intTotalItemCount;
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;

                    vecReport.ensureCapacity(intDataViewCount);
                    }
                }

            // Do not use an Iterator because of ConcurrentModificationException
            // The RawData may be updated by another Thread while this report is being generated
            for (int intRowIndex = intDataViewStartIndex;
                 intRowIndex < (intDataViewStartIndex + intDataViewCount);
                 intRowIndex++)
                {
                final Vector vecSample;

                vecSample = (Vector) getRawData().get(intRowIndex);

                // There must be one Calendar and ChannelCount samples in the Vector...
                // OR <x-axis> <Channel0> <Channel1> <Channel2> ...
                if ((vecSample != null)
                    && (vecSample.size() == (getChannelCount() + 1)))
                    {
                    intSampleIndex = addData(vecReport, vecSample, intSampleIndex);
                    }
                }
            }
        else
            {
            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + " NOT VISIBLE OR EMPTY");

            vecReport = new Vector<Vector>(1);
            }

        //ObservatoryInstrumentHelper.diagnoseMemory("RawDataUIComponent.generateReport() END");

        return (vecReport);
        }


    /***********************************************************************************************
     * Generate the report data table.
     * ToDo REVIEW - Left in place for convenience.
     *
     * @return Vector of report rows
     */

    private Vector<Vector> generateReportClonedVersion()
        {
        final String SOURCE = "RawDataUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

//        LOGGER.debugTimedEvent("RawDataUIComponent.generateReport() [isselectedinstrument="
//                                + ObservatoryUI.isSelectedInstrument(getHostInstrument())
//                                + "] [visible=" + UIComponent.isUIComponentShowing(this) + "]");
//        ObservatoryInstrumentHelper.diagnoseMemory("RawDataUIComponent.generateReport() START");

        // Only generate a Report if this UIComponent is visible
        if ((UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            && (getRawData() != null)
            && (!getRawData().isEmpty())
            && (getDataViewMode() != null)
            && (getDataViewLimit() > 0))
            {
            Vector<Vector> vecRawDataClone;
            final int intTotalItemCount;
            final int intDataViewStartIndex;
            final int intDataViewCount;
            int intSampleIndex;

            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "VISIBLE");

            // Make a copy of the RawData for truncation and sorting...
            vecRawDataClone = (Vector<Vector>) getRawData().clone();

            intTotalItemCount = vecRawDataClone.size();

            // Prepare space for the report
            // We need a *separate* data structure to hold the row index also,
            // since this doesn't appear in the underlying 'internal' RawData
            vecReport = new Vector<Vector>();

            switch (getDataViewMode())
                {
                case SHOW_ALL:
                    {
                    final Iterator iterClone;

                    // Take everything
                    intDataViewStartIndex = 0;
                    intDataViewCount = intTotalItemCount;
                    vecReport.ensureCapacity(intDataViewCount);
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;

                    iterClone = vecRawDataClone.iterator();

                    while ((iterClone != null)
                        && (iterClone.hasNext()))
                        {
                        final Vector vecSample;

                        vecSample = (Vector) iterClone.next();

                        // There must be one Calendar and ChannelCount samples in the Vector...
                        // OR <x-axis> <Channel0> <Channel1> <Channel2> ...
                        if ((vecSample != null)
                            && (vecSample.size() == (getChannelCount() + 1)))
                            {
                            intSampleIndex = addData(vecReport, vecSample, intSampleIndex);
                            }
                        }

                    break;
                    }

                case SHOW_FIRST:
                    {
                    // Only take the first samples
                    intDataViewStartIndex = 0;
                    if (intTotalItemCount > getDataViewLimit())
                        {
                        intDataViewCount = getDataViewLimit();
                        }
                    else
                        {
                        intDataViewCount = intTotalItemCount;
                        }
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;

                    vecReport.ensureCapacity(intDataViewCount);

                    for (int intRowIndex = intDataViewStartIndex;
                         intRowIndex < (intDataViewStartIndex + intDataViewCount);
                         intRowIndex++)
                        {
                        final Vector vecSample;

                        vecSample = vecRawDataClone.get(intRowIndex);

                        // There must be one Calendar and ChannelCount samples in the Vector...
                        // OR <x-axis> <Channel0> <Channel1> <Channel2> ...
                        if ((vecSample != null)
                            && (vecSample.size() == (getChannelCount() + 1)))
                            {
                            intSampleIndex = addData(vecReport, vecSample, intSampleIndex);
                            }
                        }

                    break;
                    }

                case SHOW_LAST:
                    {
                    // Only take the most recent samples
                    if (intTotalItemCount > getDataViewLimit())
                        {
                        intDataViewStartIndex = intTotalItemCount - getDataViewLimit();
                        }
                    else
                        {
                        intDataViewStartIndex = 0;
                        }
                    intDataViewCount = Math.min(intTotalItemCount, getDataViewLimit());
                    // Take care not to modify the loop count parameter
                    intSampleIndex = intDataViewStartIndex;
                    vecReport.ensureCapacity(intDataViewCount);

                    for (int intRowIndex = intDataViewStartIndex;
                         intRowIndex < (intDataViewStartIndex + intDataViewCount);
                         intRowIndex++)
                        {
                        final Vector vecSample;

                        vecSample = vecRawDataClone.get(intRowIndex);

                        // There must be one Calendar and ChannelCount samples in the Vector...
                        // OR <x-axis> <Channel0> <Channel1> <Channel2> ...
                        if ((vecSample != null)
                            && (vecSample.size() == (getChannelCount() + 1)))
                            {
                            intSampleIndex = addData(vecReport, vecSample, intSampleIndex);
                            }
                        }

                    break;
                    }

                default:
                    {
                    LOGGER.error(SOURCE + "Unsupported DataViewMode");
                    }
                }

            // Help the gc?
            vecRawDataClone = null;
            }
        else
            {
            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + " NOT VISIBLE OR EMPTY");

            vecReport = new Vector<Vector>(1);
            }

        //ObservatoryInstrumentHelper.diagnoseMemory("RawDataUIComponent.generateReport() END");

        return (vecReport);
        }


    /***********************************************************************************************
     * Add a row of data to the specified Report.
     *
     * @param report
     * @param sample
     * @param sampleindex
     *
     * @return int
     */

    private int addData(final Vector<Vector> report,
                        final Vector sample,
                        final int sampleindex)
        {
        int intSampleIndex;
        final Vector<Object> vecRow;

        intSampleIndex = sampleindex;
        vecRow = new Vector<Object>(getChannelCount() + 3);

        // Handle Date,Time or X-value, and preceed with a Report Index
        switch (getDatasetType())
            {
            case XY:
                {
                final Object objSample;

                // Just interpret the X-data literally as a number
                objSample = sample.get(DataTranslatorInterface.INDEX_INDEXED_X_VALUE);

                if (objSample != null)
                    {
                    // Remember that all data entries must be Strings
                    vecRow.add(Integer.toString(intSampleIndex++));
                    vecRow.add(objSample.toString());
                    }
                else
                    {
                    vecRow.add(QUERY);
                    }
                break;
                }

            case TIMESTAMPED:
                {
                final Calendar calendar;

                // We are dealing with indexed, timestamped data
                calendar = (Calendar) sample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                // Remember that all data entries must be Strings
                vecRow.add(Integer.toString(intSampleIndex++));
                vecRow.add(ChronosHelper.toDateString(calendar));
                vecRow.add(ChronosHelper.toTimeString(calendar));
                break;
                }

            default:
                {
                LOGGER.error("RawDataUIComponent.defineColumnWidths() Invalid DatasetType");
                }
            }

        // Add one column for each channel of data, for all types of report
        for (int i = 0;
             ((getChannelCount() > 0) && (i < getChannelCount()));
             i++)
            {
            final Object objSample;

            objSample = sample.get(DataTranslatorInterface.INDEX_DATA_SAMPLE + i);

            if (objSample != null)
                {
                // Remember that all data entries must be Strings
                vecRow.add(objSample.toString());
                }
            else
                {
                vecRow.add(QUERY);
                }
            }

        report.add(vecRow);

        return (intSampleIndex);
        }
    }
