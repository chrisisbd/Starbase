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

package org.lmn.fc.common.datafilters.impl.testfilter;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;
import org.lmn.fc.common.datafilters.DataFilter;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ParameterHelper;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.Vector;


/***************************************************************************************************
 * TestFilter.
 */

public final class TestFilter extends DataFilter
                              implements DataFilterInterface
    {
    // Parameters
    private static final int PARAMETER_COUNT = 3;
    private static final String FILTER_PARAMETER_A = "Filter.Parameter.A";
    private static final String FILTER_PARAMETER_B = "Filter.Parameter.B";
    private static final String FILTER_PARAMETER_C = "Filter.Parameter.C";


    /***********************************************************************************************
     * Construct a TestFilter.
     */

    public TestFilter()
        {
        // The Dataset for this Filter need not be a power of two in length
        super(DataFilterType.TEST, PARAMETER_COUNT, false);
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        final ParameterType param0;
        final ParameterType param1;
        final ParameterType param2;
        final ParameterDataType dataType;

        super.initialiseFilter();

        // Indicate to which DatasetTypes this Filter may be applied
        getDatasetTypes().add(DatasetType.TABULAR);
        getDatasetTypes().add(DatasetType.TIMESTAMPED);
        getDatasetTypes().add(DatasetType.XY);

        dataType = ParameterDataType.Factory.newInstance();
        dataType.setDataTypeName(SchemaDataType.DECIMAL_INTEGER);

        param0 = ParameterType.Factory.newInstance();
        param0.setName(FILTER_PARAMETER_A);
        param0.setSubParameterIndex(1);
        param0.setValue(Integer.toString(0));
        param0.setUnits(SchemaUnits.SECONDS);
        param0.setInputDataType(dataType);
        param0.setTrafficDataType(dataType);
        param0.setTooltip("Parameter Zero");

        getParameters().add(param0);

        param1 = ParameterType.Factory.newInstance();
        param1.setName(FILTER_PARAMETER_B);
        param1.setSubParameterIndex(2);
        param1.setValue(Integer.toString(1));
        param1.setUnits(SchemaUnits.SECONDS);
        param1.setInputDataType(dataType);
        param1.setTrafficDataType(dataType);
        param1.setTooltip("Parameter One");

        getParameters().add(param1);

        param2 = ParameterType.Factory.newInstance();
        param2.setName(FILTER_PARAMETER_C);
        param2.setSubParameterIndex(3);
        param2.setValue(Integer.toString(2));
        param2.setUnits(SchemaUnits.SECONDS);
        param2.setInputDataType(dataType);
        param2.setTrafficDataType(dataType);
        param2.setTooltip("Parameter Two");

        getParameters().add(param2);
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
        final String SOURCE = "TestFilter.filterTimestampedListToTimeSeries() ";
        final TimeSeries timeSeries;
        final int intA;
        final int intB;
        final int intC;

        intA = ParameterHelper.getParameterByNameAsDecimalInteger(getParameters(), FILTER_PARAMETER_A);
        intB = ParameterHelper.getParameterByNameAsDecimalInteger(getParameters(), FILTER_PARAMETER_B);
        intC = ParameterHelper.getParameterByNameAsDecimalInteger(getParameters(), FILTER_PARAMETER_C);

        LOGGER.log(SOURCE + FILTER_PARAMETER_A + " = " + intA);
        LOGGER.log(SOURCE + FILTER_PARAMETER_B + " = " + intB);
        LOGGER.log(SOURCE + FILTER_PARAMETER_C + " = " + intC);

        // Ignore the time constant and produce an unfiltered TimeSeries, i.e. PassThrough
        timeSeries = DataFilterHelper.createUnfilteredTimeSeriesFromList(timestampeddata,
                                                                         channelindex,
                                                                         channelname,
                                                                         eventlogfragment,
                                                                         clock
        );
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
        final int intA;
        final int intB;
        final int intC;

        intA = ParameterHelper.getParameterByNameAsDecimalInteger(getParameters(), FILTER_PARAMETER_A);
        intB = ParameterHelper.getParameterByNameAsDecimalInteger(getParameters(), FILTER_PARAMETER_B);
        intC = ParameterHelper.getParameterByNameAsDecimalInteger(getParameters(), FILTER_PARAMETER_C);

        LOGGER.log(SOURCE + FILTER_PARAMETER_A + " = " + intA);
        LOGGER.log(SOURCE + FILTER_PARAMETER_B + " = " + intB);
        LOGGER.log(SOURCE + FILTER_PARAMETER_C + " = " + intC);

        xySeries = DataFilterHelper.createUnfilteredXYSeriesFromList(indexeddata,
                                                                     channelindex,
                                                                     channelname,
                                                                     eventlogfragment,
                                                                     clock
        );
        return (xySeries);
        }
    }
