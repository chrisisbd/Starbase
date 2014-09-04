// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012
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

package org.lmn.fc.common.datafilters.impl.goesxraylinearfilter;

import org.jfree.data.time.*;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilter;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;

import java.util.*;


/***************************************************************************************************
 * GOESXrayLinearFilter.
 */

public final class GOESXrayLinearFilter extends DataFilter
                                        implements DataFilterInterface
    {
    private static final int INDEX_FLUX_0 = 0;
    private static final int INDEX_FLUX_1 = 1;
    private static final int INDEX_RATIO = 2;
    private static final double MISSING_DATA = -100000.0;


    /***********************************************************************************************
     * Transform a single channel data sample for this Filter.
     * For GOES data, transform only Channels 0 and 1, since Channel 2 is the Flux Ratio.
     *
     * @param channelid
     * @param data
     *
     * @return double
     */

    private static double transformData(final int channelid,
                                        final double data)
        {
        if ((channelid == INDEX_FLUX_0)
            || (channelid == INDEX_FLUX_1))
            {
            if (data > MISSING_DATA)
                {
                return (Math.log10(data));
                }
            else
                {
                // Missing data are replaced by zero
                return (0.0);
                }
            }
        else
            {
            // Do not transform Channel 2 unless it is MISSING_DATA,
            // in which case assume a minimum of zero
            if (data > MISSING_DATA)
                {
                return (data);
                }
            else
                {
                // Missing data are replaced by zero
                return (0.0);
                }
            }
        }


    /***********************************************************************************************
     * Transform a single channel data sample for this Filter.
     * For GOES data, transform only Channels 0 and 1, since Channel 2 is the Flux Ratio.
     *
     * @param channelid
     * @param channelminimum
     * @param data
     *
     * @return double
     */

    private static double transformAndOffsetData(final int channelid,
                                                 final double channelminimum,
                                                 final double data)
        {
        if ((channelid == INDEX_FLUX_0)
            || (channelid == INDEX_FLUX_1))
            {
            if (data > MISSING_DATA)
                {
                return (Math.log10(data) - channelminimum);
                }
            else
                {
                // Missing data are replaced by (channelminimum - channelminimum) = 0
                return (0.0);
                }
            }
        else
            {
            // Do not transform Channel 2 unless it is MISSING_DATA,
            // in which case assume a minimum of zero
            if (data > MISSING_DATA)
                {
                return (data);
                }
            else
                {
                // Missing data are replaced by zero
                return (0.0);
                }
            }
        }


    /***********************************************************************************************
     * Find the minimum value of the data for the Xray Flux.
     * Assume a minimum value of zero for the Ratio Channel.
     *
     * @param timestampedvector
     * @param channelindex
     *
     * @return double
     */

    private static double findTransformedVectorChannelMinimum(final Vector<Object> timestampedvector,
                                                              final int channelindex)
        {
        final String SOURCE = "GOESXrayLinearFilter.findTransformedVectorChannelMinimum() ";
        double dblMinimum;

        // Assume a minimum value of zero for the Ratio Channel,
        // even if it is marked as MISSING_DATA
        if (channelindex == INDEX_RATIO)
            {
            dblMinimum = 0.0;
            }
        else
            {
            final Iterator iterVectorData;

            iterVectorData = timestampedvector.iterator();
            dblMinimum = Double.MAX_VALUE;

            while (iterVectorData.hasNext())
                {
                final Vector vecData;

                vecData = (Vector) iterVectorData.next();

                // There must be at least one Calendar and one data sample
                if ((vecData != null)
                    && (vecData.size() > 1))
                    {
                    final Object objItem;

                    // Retrieve the data for the selected Channel
                    objItem = vecData.get(channelindex + DataTranslatorInterface.INDEX_TIMESTAMPED_DATA);

                    if ((objItem != null)
                        && (objItem instanceof Number)
                        && (((Number)objItem).doubleValue() > MISSING_DATA))
                        {
                        // Just ignore MISSING_DATA in calculating the minimum
                        dblMinimum = Math.min(dblMinimum,
                                              transformData(channelindex,
                                                            ((Number) objItem).doubleValue()));
//                        LOGGER.debug(true,
//                                     SOURCE + "[channel=" + channelindex
//                                     +  "] [value=" + (((Number) objItem).doubleValue())
//                                     +  "] [minimum=" + dblMinimum
//                                     + "]");
                        }
                    }
                }
            }

        return (dblMinimum);
        }


    /***********************************************************************************************
     * Find the minimum value of the data for the specified Channel of the TimeSeries.
     * Assume a minimum value of zero for the Ratio Channel.
     *
     * @param timeseriesitemlist
     * @param channelindex
     *
     * @return double
     */

    private static double findTransformedTimeSeriesChannelMinimum(final List<TimeSeriesDataItem> timeseriesitemlist,
                                                                  final int channelindex)
        {
        final String SOURCE = "GOESXrayLinearFilter.findTransformedTimeSeriesChannelMinimum() ";
        double dblMinimum;

        // Assume a minimum value of zero for the Ratio Channel
        if (channelindex == INDEX_RATIO)
            {
            dblMinimum = 0.0;
            }
        else
            {
            dblMinimum = Double.MAX_VALUE;

            for (int intInputIndex = 0;
                 intInputIndex < timeseriesitemlist.size();
                 intInputIndex++)
                {
                final TimeSeriesDataItem dataItem;

                dataItem = timeseriesitemlist.get(intInputIndex);

                // Retrieve the data for the selected Channel
                if ((dataItem != null)
                    && (dataItem.getValue() != null)
                    && (dataItem.getValue().doubleValue() > MISSING_DATA))
                    {
                    // Just ignore MISSING_DATA in calculating the minimum
                    dblMinimum = Math.min(dblMinimum,
                                          transformData(channelindex,
                                                        dataItem.getValue().doubleValue()));
//                    LOGGER.debug(true,
//                                 SOURCE + "[channel=" + channelindex
//                                 +  "] [value=" + (dataItem.getValue().doubleValue())
//                                 +  "] [minimum=" + dblMinimum
//                                 + "]");
                    }
                }
            }

        return (dblMinimum);
        }


    /***********************************************************************************************
     * Construct a GOESXrayLinearFilter, which converts the flux channels of a GOES record
     * to linear values, based at zero, for display on linear axes.
     * The Dataset for this Filter need not be a power of two in length.
     */

    public GOESXrayLinearFilter()
        {
        super(DataFilterType.GOESXRAY_LINEAR, 0, false);
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        super.initialiseFilter();

        // Indicate to which DatasetTypes this Filter may be applied
        getDatasetTypes().add(DatasetType.TIMESTAMPED);
        }


    /***********************************************************************************************
     * Do the DataFilter operation on Timestamped RawData,
     * to create a TimeSeries, for the specified Channel.
     * Remember to call initialiseFilter() first!
     * The input data format is: {Calendar} {Channel0} {Channel1} {Channel2}.
     * A TimeSeries represents a sequence of zero or more data items in the form (period, value)
     * where 'period' is some instance of a subclass of RegularTimePeriod.
     * The time series will ensure that (a) all data items have the same type of period
     * (for example, Day) and (b) that each period appears at most one time in the series.
     *
     * @param timestampeddata
     * @param channelindex
     * @param channelname
     * @param eventlogfragment
     * @param clock
     *
     * @return TimeSeries
     */

    public TimeSeries filterTimestampedListToTimeSeries(final Vector<Object> timestampeddata,
                                                        final int channelindex,
                                                        final String channelname,
                                                        final Vector<Vector> eventlogfragment,
                                                        final ObservatoryClockInterface clock)
        {
        final String SOURCE = "GOESXrayLinearFilter.filterTimestampedListToTimeSeries() ";
        TimeSeries timeSeries;
        boolean boolLogged;

        timeSeries = null;
        boolLogged = false;

        if ((timestampeddata != null)
            && (timestampeddata.size() > 0))
            {
            final int intChannelIndex;
            final String strChannelName;
            final double dblChannelMinimum;
            final Iterator iterRawData;
            TimeZone timeZone;

            // Validate the remaining parameters
            if (channelindex < 0)
                {
                intChannelIndex = 0;
                }
            else
                {
                intChannelIndex = channelindex;
                }

            // TimeSeries must have a name!
            if ((channelname ==  null)
                || (FrameworkStrings.EMPTY_STRING.equals(channelname.trim())))
                {
                strChannelName = DataFilterInterface.DEFAULT_CHANNEL_NAME;
                }
            else
                {
                strChannelName = channelname;
                }

            timeSeries = new TimeSeries(strChannelName);
            timeZone = null;

            // Find the minimum value of the Xray flux in Channel 0 or Channel 1 only
            // Assume a minimum value of zero for the Ratio Channel
            dblChannelMinimum = findTransformedVectorChannelMinimum(timestampeddata, intChannelIndex);

            iterRawData = timestampeddata.iterator();

            while ((iterRawData != null)
                   && (iterRawData.hasNext()))
                {
                final Vector vecSample;

                vecSample = (Vector) iterRawData.next();

                // There must be at least one Calendar and one data sample
                if ((vecSample != null)
                    && (vecSample.size() > 1))
                    {
                    Object objItem;

                    objItem = vecSample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                    if ((objItem != null)
                        && (objItem instanceof Calendar)
                        && (intChannelIndex >= 0)
                        && (intChannelIndex < (vecSample.size() - 1)))
                        {
                        final Calendar calSample;

                        calSample = (Calendar)objItem;

                        if (timeZone == null)
                            {
                            // Record the TimeZone once and never reset it...
                            timeZone = calSample.getTimeZone();
                            }

                        // Retrieve the data for the selected Channel
                        objItem = vecSample.get(intChannelIndex + DataTranslatorInterface.INDEX_TIMESTAMPED_DATA);

                        if (objItem != null)
                            {
                            if (objItem instanceof Number)
                                {
                                // ToDo Get the Locale also
                                timeSeries.addOrUpdate(new Second(calSample.getTime(),
                                                                  calSample.getTimeZone()),
                                                       transformAndOffsetData(intChannelIndex,
                                                                              dblChannelMinimum,
                                                                              ((Number) objItem).doubleValue()));
                                }
                            else
                                {
                                if (!boolLogged)
                                    {
                                    SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_TIMESERIES
                                                                           + METADATA_ACTION_TRANSFORM + SPACE
                                                                           + METADATA_RESULT + "Unsupported DataType" + TERMINATOR,
                                                                       SOURCE,
                                                                       clock);
                                    boolLogged = true;
                                    }
                                }
                            }
                        else
                            {
                            if (!boolLogged)
                                {
                                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_TIMESERIES
                                                                       + METADATA_ACTION_TRANSFORM + SPACE
                                                                       + METADATA_RESULT + "The channel data item is null" + TERMINATOR,
                                                                   SOURCE,
                                                                   clock);
                                boolLogged = true;
                                }
                            }
                        }
                    else
                        {
                        if (!boolLogged)
                            {
                            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_TIMESERIES
                                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                                   + METADATA_RESULT + "The data sample has no Calendar, or the channel is incorrect" + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            boolLogged = true;
                            }
                        }
                    }
                else
                    {
                    if (!boolLogged)
                        {
                        SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_TIMESERIES
                                                               + METADATA_ACTION_TRANSFORM + SPACE
                                                               + METADATA_RESULT + "The data are not in the correct format" + TERMINATOR,
                                                           SOURCE,
                                                           clock);
                        boolLogged = true;
                        }
                    }
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                   + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (timeSeries);
        }


    /***********************************************************************************************
     * Do the DataFilter operation on the specified Channel of a TimeSeriesCollection,
     * to create a new TimeSeries.
     * Remember to call initialiseFilter() first!
     * A TimeSeries represents a sequence of zero or more data items in the form (period, value)
     * where 'period' is some instance of a subclass of RegularTimePeriod.
     * The time series will ensure that (a) all data items have the same type of period
     * (for example, Day) and (b) that each period appears at most one time in the series.
     *
     * @param xydataset
     * @param channelindex
     * @param channelname
     * @param eventlogfragment
     * @param clock
     *
     * @return TimeSeries
     */

    public TimeSeries filterTimeSeries(final TimeSeriesCollection xydataset,
                                       final int channelindex,
                                       final String channelname,
                                       final Vector<Vector> eventlogfragment,
                                       final ObservatoryClockInterface clock)
        {
        final String SOURCE = "GOESXrayLinearFilter.filterTimeSeries() ";
        TimeSeries seriesOutput;

        seriesOutput = null;

        if ((xydataset != null)
            && (xydataset.getSeriesCount() > 0)
            && (channelindex >= 0)
            && (channelindex < xydataset.getSeriesCount())
            && (xydataset.getSeries(channelindex) != null))
            {
            final int intChannelIndex;
            final String strChannelName;
            final TimeSeries seriesInput;
            final List<TimeSeriesDataItem> listInputData;
            final double dblChannelMinimum;

            // Validate the remaining parameters
            if (channelindex < 0)
                {
                intChannelIndex = 0;
                }
            else
                {
                intChannelIndex = channelindex;
                }

            // TimeSeries must have a name!
            if ((channelname ==  null)
                || (FrameworkStrings.EMPTY_STRING.equals(channelname.trim())))
                {
                strChannelName = DEFAULT_CHANNEL_NAME;
                }
            else
                {
                strChannelName = channelname;
                }

            // Get the TimeSeries to be transformed, which must exist
            seriesInput = xydataset.getSeries(intChannelIndex);

            // All samples must share the same TimeZone
            listInputData = seriesInput.getItems();

            // Find the minimum value of the specified Channel
            // Assume a minimum value of zero for the Ratio Channel
            dblChannelMinimum = findTransformedTimeSeriesChannelMinimum(listInputData, intChannelIndex);

            seriesOutput = new TimeSeries(strChannelName);

            // Move along the List of input data
            for (int intInputIndex = 0;
                 intInputIndex < listInputData.size();
                 intInputIndex++)
                {
                final TimeSeriesDataItem inputItem;

                inputItem = listInputData.get(intInputIndex);

                if (inputItem != null)
                    {
                    final RegularTimePeriod periodTransformed;

                    periodTransformed = inputItem.getPeriod();

                    if (periodTransformed != null)
                        {
                        // Retrieve the data for the selected Channel
                        if (inputItem.getValue() != null)
                            {
                            final double dblTransformed;

                            dblTransformed = transformAndOffsetData(intChannelIndex,
                                                                    dblChannelMinimum,
                                                                    inputItem.getValue().doubleValue());

                            // Use addOrUpdate() to avoid Exceptions from duplicates...
                            seriesOutput.addOrUpdate(periodTransformed, dblTransformed);
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_TIMESERIES
                                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                                   + METADATA_RESULT + "The channel data value is null" + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_TIMESERIES
                                                               + METADATA_ACTION_TRANSFORM + SPACE
                                                               + METADATA_RESULT + "The channel period value is null" + TERMINATOR,
                                                           SOURCE,
                                                           clock);
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_TRANSFORM + SPACE
                                                           + METADATA_RESULT + "The channel data item is null" + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    }
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                   + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (seriesOutput);
        }
    }