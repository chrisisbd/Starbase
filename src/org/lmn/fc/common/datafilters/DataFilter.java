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
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * The DataFilter base class for all DataFilters.
 */

public class DataFilter implements DataFilterInterface
    {
    // Injections
    private final DataFilterType filterType;
    private final int intParameterCount;
    private final boolean boolMustbePowerOfTwo;

    private final List<DatasetType> listDatasetTypes;
    private final List<ParameterType> listParameters;
    private List<Metadata> listMetadata;


    /***********************************************************************************************
     * Construct a DataFilter of the specified DataFilterType.
     *
     * @param filtertype
     * @param parametercount
     * @param mustbepoweroftwo
     */

    protected DataFilter(final DataFilterType filtertype,
                         final int parametercount,
                         final boolean mustbepoweroftwo)
        {
        // Injections
        this.filterType = filtertype;
        this.intParameterCount = parametercount;
        this.boolMustbePowerOfTwo = mustbepoweroftwo;

        this.listDatasetTypes = new ArrayList<DatasetType>(DatasetType.values().length);
        this.listParameters = new ArrayList<ParameterType>(parametercount);
        this.listMetadata = new ArrayList<Metadata>(10);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the DataFilterType.
     *
     * @return DataFilterType
     */

    public DataFilterType getFilterType()
        {
        return (this.filterType);
        }


    /***********************************************************************************************
     * Get the DataFilter Parameter Count.
     *
     * @return int
     */

    public int getParameterCount()
        {
        return (this.intParameterCount);
        }


    /***********************************************************************************************
     * Indicate if the input dataset for the Filter must be a power of two in length.
     *
     * @return boolean
     */

    public boolean mustbePowerOfTwo()
        {
        return (this.boolMustbePowerOfTwo);
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        // Override as required
        getDatasetTypes().clear();
        getParameters().clear();
        getMetadata().clear();
        }


    /***********************************************************************************************
     * Dispose of the DataFilter.
     */

    public void disposeFilter()
        {
        // Override as required
        getDatasetTypes().clear();
        getParameters().clear();
        getMetadata().clear();
        }


    /***********************************************************************************************
     * Get the List of applicable DatasetTypes.
     *
     * @return List<DatasetType>
     */

    public List<DatasetType> getDatasetTypes()
        {
        return (this.listDatasetTypes);
        }


    /***********************************************************************************************
     * Get the DataFilter Parameter List.
     *
     * @return List<Metadata>
     */

    public List<ParameterType> getParameters()
        {
        return (this.listParameters);
        }


    /***********************************************************************************************
     * Set the DataFilter Parameter List.
     * The size of the List must equal the DataFilter ParameterCount.
     * Each ParameterType must have a SubParameterIndex in the range {1...parametercount-1}.
     *
     * @param parameters
     */

    public void setParameters(final List<ParameterType> parameters)
        {
        getParameters().clear();

        if ((parameters != null)
            && (!parameters.isEmpty())
            && (parameters.size() == getParameterCount()))
            {
            final Iterator<ParameterType> iterParameters;
            int intSubParameterCount;

            iterParameters = parameters.iterator();
            intSubParameterCount = 0;

            while (iterParameters.hasNext())
                {
                final ParameterType parameterType;

                parameterType = iterParameters.next();

                // ToDo We could even check that all Parameters are in the order given by the SubParameterIndex?!
                if ((parameterType.getSubParameterIndex() > 0)
                    && (parameterType.getSubParameterIndex() <= getParameterCount()))
                    {
                    intSubParameterCount++;
                    }
                }

            // Only add the Parameters if they were all specified correctly
            if (intSubParameterCount == getParameterCount())
                {
                getParameters().addAll(parameters);
                }
            }
        }


    /***********************************************************************************************
     * Get the DataFilter Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the DataFilter Metadata List.
     *
     * @param metadata
     */

    public void setMetadata(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
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
        final String SOURCE = "DataFilter.filterTimestampedListToTimeSeries() ";

        LOGGER.error(SOURCE + MSG_NOT_IMPLEMENTED);

        return (null);
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

        LOGGER.error(SOURCE + MSG_NOT_IMPLEMENTED);

        return (null);
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

        LOGGER.error(SOURCE + MSG_NOT_IMPLEMENTED);

        return (null);
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

        LOGGER.error(SOURCE + MSG_NOT_IMPLEMENTED);

        return (null);
        }
    }
