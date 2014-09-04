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

package org.lmn.fc.common.datafilters;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DataFilterInterface.
 *
 * Each Filter can do the following operations on *one Channel* in each case:
 *
 *      TimestampedData to TimeSeries
 *      TimeSeriesCollection to TimeSeries
 *
 *      IndexedData to XYSeries
 *      XYSeriesCollection to XYSeries
 *
 * where each row of:
 *
 *      TimestampedData is a Vector containing Calendar and Data entries
 *      IndexedData is a Vector containing Data entries
 */

public interface DataFilterInterface extends FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             FrameworkRegex,
                                             ResourceKeys
    {
    // String Resources
    String MSG_NOT_IMPLEMENTED = "not implemented";
    String MSG_UNSUPPORTED_FILTER = "Unsupported data filter";
    String DEFAULT_CHANNEL_NAME = "Channel name not set";


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the DataFilterType.
     *
     * @return DataFilterType
     */

    DataFilterType getFilterType();


    /***********************************************************************************************
     * Get the DataFilter Parameter Count.
     * Return zero if none are required.
     *
     * @return int
     */

    int getParameterCount();


    /***********************************************************************************************
     * Indicate if the input dataset for the Filter must be a power of two in length.
     *
     * @return boolean
     */

    boolean mustbePowerOfTwo();


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    void initialiseFilter();


    /***********************************************************************************************
     * Dispose of the DataFilter.
     */

    void disposeFilter();


    /***********************************************************************************************
     * Get the List of applicable DatasetTypes.
     *
     * @return List<DatasetType>
     */

    List<DatasetType> getDatasetTypes();


    /***********************************************************************************************
     * Get the DataFilter Parameter List.
     * Return an empty List if none are required.
     *
     * @return List<Metadata>
     */

    List<ParameterType> getParameters();


    /***********************************************************************************************
     * Set the DataFilter Parameter List.
     * The size of the List must equal the DataFilter ParameterCount.
     * Each ParameterType must have a SubParameterIndex in the range {1...parametercount-1}.
     *
     * @param parameters
     */

    void setParameters(List<ParameterType> parameters);


    /***********************************************************************************************
     * Get the DataFilter Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadata();


    /***********************************************************************************************
     * Set the DataFilter Metadata List.
     *
     * @param metadata
     */

    void setMetadata(List<Metadata> metadata);


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

    TimeSeries filterTimestampedListToTimeSeries(Vector<Object> timestampeddata,
                                                 int channelindex,
                                                 String channelname,
                                                 Vector<Vector> eventlogfragment,
                                                 ObservatoryClockInterface clock);


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

    TimeSeries filterTimeSeries(TimeSeriesCollection xydataset,
                                int channelindex,
                                String channelname,
                                Vector<Vector> eventlogfragment,
                                ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Do the DataFilter operation on Indexed RawData,
     * to create an XYSeries, for the specified Channel.
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

    XYSeries filterIndexedListToXYSeries(Vector<Object> indexeddata,
                                         int channelindex,
                                         String channelname,
                                         Vector<Vector> eventlogfragment,
                                         ObservatoryClockInterface clock);


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

    XYSeries filterXYSeries(XYSeriesCollection xydataset,
                            int channelindex,
                            String channelname,
                            Vector<Vector> eventlogfragment,
                            ObservatoryClockInterface clock);
    }
