// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.common.datafilters.impl.passthroughfilter;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.datafilters.DataFilter;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;

import java.util.Vector;


/***************************************************************************************************
 * PassThroughFilter.
 */

public final class PassThroughFilter extends DataFilter
                                     implements DataFilterInterface
    {
    /***********************************************************************************************
     * Construct a PassThroughFilter, no Parameters are required.
     * The Dataset for this Filter need not be a power of two in length.
     */

    public PassThroughFilter()
        {
        super(DataFilterType.PASS_THROUGH, 0, false);
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        super.initialiseFilter();

        // Indicate to which DatasetTypes this Filter may be applied
        getDatasetTypes().add(DatasetType.TIMESTAMPED);
        getDatasetTypes().add(DatasetType.XY);
        }


    /***********************************************************************************************
     * Do the DataFilter operation on Timestamped RawData,
     * to create a TimeSeries, for the specified Channel.
     * Remember to call initialiseFilter() first!
     * The input data format is: {Calendar} {Channel0} {Channel1} {Channel2}.
     * Ignore the time constant and produce an unfiltered TimeSeries, i.e. PassThrough.
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
        final String SOURCE = "DataFilter.filterTimestampedListToTimeSeries() ";
        final TimeSeries timeSeries;

        timeSeries = DataFilterHelper.createUnfilteredTimeSeriesFromList(timestampeddata,
                                                                         channelindex,
                                                                         channelname,
                                                                         eventlogfragment,
                                                                         clock);
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
        final String SOURCE = "DataFilter.filterTimeSeries() ";
        final TimeSeries timeSeries;

        timeSeries = DataFilterHelper.createUnfilteredTimeSeriesFromTimeSeries(xydataset,
                                                                               channelindex,
                                                                               channelname,
                                                                               eventlogfragment,
                                                                               clock);
        return (timeSeries);
        }


    /***********************************************************************************************
     * Do the DataFilter operation on Indexed RawData, to create an XYSeries, for the specified Channel.
     * Remember to call initialiseFilter() first!
     * The input data format is: {Index} {Channel0} {Channel1} {Channel2}.
     * An XYSeries represents a sequence of zero or more data items in the form (x, y).
     * By default, items in the series will be sorted into ascending order by x-value,
     * and duplicate x-values are permitted.
     * Y-values can be null to represent missing values.
     *
     * @param indexeddata
     * @param channelindex
     * @param channelname
     * @param eventlogfragment
     * @param clock
     *
     * @return XYSeries
     */

    public XYSeries filterIndexedListToXYSeries(final Vector<Object> indexeddata,
                                                final int channelindex,
                                                final String channelname,
                                                final Vector<Vector> eventlogfragment,
                                                final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataFilter.filterIndexedListToXYSeries() ";
        final XYSeries xySeries;

        xySeries = DataFilterHelper.createUnfilteredXYSeriesFromList(indexeddata,
                                                                     channelindex,
                                                                     channelname,
                                                                     eventlogfragment,
                                                                     clock);
        return (xySeries);
        }


    /***********************************************************************************************
     * Do the DataFilter operation on the specified Channel of an XYSeriesCollection,
     * to create a new XYSeries.
     * Remember to call initialiseFilter() first!
     * An XYSeries represents a sequence of zero or more data items in the form (x, y).
     * By default, items in the series will be sorted into ascending order by x-value,
     * and duplicate x-values are permitted.
     * Y-values can be null to represent missing values.
     *
     * @param xydataset
     * @param channelindex
     * @param channelname
     * @param eventlogfragment
     * @param clock
     *
     * @return XYSeries
     */

    public XYSeries filterXYSeries(final XYSeriesCollection xydataset,
                                   final int channelindex,
                                   final String channelname,
                                   final Vector<Vector> eventlogfragment,
                                   final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataFilter.filterXYSeries() ";
        final XYSeries xySeries;

        xySeries = DataFilterHelper.createUnfilteredXYSeriesFromXYSeries(xydataset,
                                                                         channelindex,
                                                                         channelname,
                                                                         eventlogfragment,
                                                                         clock);
        return (xySeries);
        }
    }