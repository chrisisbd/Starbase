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

package org.lmn.fc.common.datafilters.impl.lineartransformfilter;

import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilter;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ParameterHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.*;


/***************************************************************************************************
 * LinearTransformFilter.
 */

public final class LinearTransformFilter extends DataFilter
                                         implements DataFilterInterface
    {
    // String Resources
    private static final String NAME_SCALE_FACTOR = "Filter.ScaleFactor";
    private static final String NAME_OFFSET = "Filter.Offset";
    private static final String REGEX_SCALE_FACTOR = REGEX_SIGNED_DECIMAL_DOUBLE;
    private static final String REGEX_OFFSET = REGEX_SIGNED_DECIMAL_DOUBLE;
    private static final String TOOLTIP_SCALE_FACTOR = "The Scale Factor may be any valid positive or negative number";
    private static final String TOOLTIP_OFFSET = "The Offset may be any valid positive or negative number";

    // Parameters
    private static final int PARAMETER_COUNT = 2;
    private static final int SUB_PARAMETER_INDEX_SCALE_FACTOR = 1;
    private static final int SUB_PARAMETER_INDEX_OFFSET = 2;
    private static final float DEFAULT_SCALE_FACTOR = 1.0f;
    private static final float DEFAULT_OFFSET = 0.0f;


    /***********************************************************************************************
     * Read the specified Parameter Value, which must be of type DECIMAL_FLOAT.
     *
     * @param parameters
     * @param name
     * @param defaultvalue
     *
     * @return float
     */

    private static double readParameterValue(final List<ParameterType> parameters,
                                             final String name,
                                             final float defaultvalue)
        {
        final ParameterType parameter;
        double floatValue;

        parameter = ParameterHelper.getParameterByName(parameters, name);

        if ((parameter != null)
            && (SchemaDataType.DECIMAL_FLOAT.equals(parameter.getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strValue;

                strValue = parameter.getValue();
                floatValue = Double.parseDouble(strValue);
                }

            catch (NumberFormatException exception)
                {
                // This should of course never happen!
                floatValue = defaultvalue;
                }
            }
        else
            {
            floatValue = defaultvalue;
            }

        return (floatValue);
        }


    /***********************************************************************************************
     * Construct a LinearTransformFilter, Parameters are Linear.ScaleFactor and Linear.Offset.
     * The Dataset for this Filter need not be a power of two in length.
     */

    public LinearTransformFilter()
        {
        super(DataFilterType.LINEAR_TRANSFORM, PARAMETER_COUNT, false);
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        final ParameterType paramScaleFactor;
        final ParameterType paramOffset;
        final ParameterDataType dataType;

        super.initialiseFilter();

        // Indicate to which DatasetTypes this Filter may be applied
        getDatasetTypes().add(DatasetType.TIMESTAMPED);
        getDatasetTypes().add(DatasetType.XY);

        dataType = ParameterDataType.Factory.newInstance();
        dataType.setDataTypeName(SchemaDataType.DECIMAL_FLOAT);

        paramScaleFactor = ParameterType.Factory.newInstance();

        paramScaleFactor.setName(NAME_SCALE_FACTOR);
        paramScaleFactor.setSubParameterIndex(SUB_PARAMETER_INDEX_SCALE_FACTOR);
        paramScaleFactor.setValue(Float.toString(DEFAULT_SCALE_FACTOR));
        paramScaleFactor.setRegex(REGEX_SCALE_FACTOR);
        paramScaleFactor.setUnits(SchemaUnits.DIMENSIONLESS);
        paramScaleFactor.setInputDataType(dataType);
        paramScaleFactor.setTrafficDataType(dataType);
        paramScaleFactor.setTooltip(TOOLTIP_SCALE_FACTOR);

        getParameters().add(paramScaleFactor);

        paramOffset = ParameterType.Factory.newInstance();

        paramOffset.setName(NAME_OFFSET);
        paramOffset.setSubParameterIndex(SUB_PARAMETER_INDEX_OFFSET);
        paramOffset.setValue(Float.toString(DEFAULT_OFFSET));
        paramOffset.setRegex(REGEX_OFFSET);
        paramOffset.setUnits(SchemaUnits.DIMENSIONLESS);
        paramOffset.setInputDataType(dataType);
        paramOffset.setTrafficDataType(dataType);
        paramOffset.setTooltip(TOOLTIP_OFFSET);

        getParameters().add(paramOffset);
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
        final String SOURCE = "LinearTransformFilter.filterTimestampedListToTimeSeries() ";
        TimeSeries timeSeries;
        final double dblScaleFactor;
        final double dblOffset;
        boolean boolLogged;

        dblScaleFactor = readParameterValue(getParameters(), NAME_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
        dblOffset = readParameterValue(getParameters(), NAME_OFFSET, DEFAULT_OFFSET);

        timeSeries = null;
        boolLogged = false;

        if ((timestampeddata != null)
            && (timestampeddata.size() > 0))
            {
            final int intChannelIndex;
            final String strChannelName;
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
                                                       (((Number) objItem).doubleValue() * dblScaleFactor) + dblOffset);
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
        final String SOURCE = "LinearTransformFilter.filterTimeSeries() ";
        TimeSeries seriesOutput;
        final double dblScaleFactor;
        final double dblOffset;

        seriesOutput = null;

        dblScaleFactor = readParameterValue(getParameters(), NAME_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
        dblOffset = readParameterValue(getParameters(), NAME_OFFSET, DEFAULT_OFFSET);

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
                            double dblTransformed;

                            dblTransformed = inputItem.getValue().doubleValue();
                            dblTransformed = (dblTransformed * dblScaleFactor) + dblOffset;

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
        final String SOURCE = "LinearTransformFilter.filterIndexedListToXYSeries() ";
        XYSeries xySeries;
        final double dblScaleFactor;
        final double dblOffset;
        boolean boolLogged;

        dblScaleFactor = readParameterValue(getParameters(), NAME_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
        dblOffset = readParameterValue(getParameters(), NAME_OFFSET, DEFAULT_OFFSET);

        xySeries = null;
        boolLogged = false;

        if ((indexeddata != null)
            && (indexeddata.size() > 0))
            {
            try
                {
                final int intChannelIndex;
                final String strChannelName;
                final Iterator iterRawData;

                // Validate the remaining parameters
                if (channelindex < 0)
                    {
                    intChannelIndex = 0;
                    }
                else
                    {
                    intChannelIndex = channelindex;
                    }

                // XYSeries must have a name!
                if ((channelname ==  null)
                    || (FrameworkStrings.EMPTY_STRING.equals(channelname.trim())))
                    {
                    strChannelName = DataFilterInterface.DEFAULT_CHANNEL_NAME;
                    }
                else
                    {
                    strChannelName = channelname;
                    }

                // Make an XYSeries
                // By default, items added to the series will be sorted into ascending order by x-value, and duplicate x-values will be allowed
                xySeries = new XYSeries(strChannelName);
                iterRawData = indexeddata.iterator();

                // Copy the RawData (index, channel_i) to the XYSeries (index, channel_i)
                // The data format is: <X-axis> <Channel0> <Channel1> <Channel2>
                while ((iterRawData != null)
                       && (iterRawData.hasNext()))
                    {
                    final Object objRow;

                    objRow = iterRawData.next();

                    if ((objRow != null)
                        && (objRow instanceof Vector))
                        {
                        final Vector vecRow;

                        vecRow = (Vector)objRow;

                        if ((intChannelIndex >= 0)
                            && (intChannelIndex < (vecRow.size() - 1)))
                            {
                            // The first item is always the index
                            // ToDo handle datatypes supplied in listdatatypes
                            xySeries.addOrUpdate(Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_X_VALUE).toString()),
                                                 (Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex).toString())
                                                    * dblScaleFactor) + dblOffset);
                            }
                        else
                            {
                            // Only log once in the loop
                            if (!boolLogged)
                                {
                                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_XYDATASET
                                                                       + METADATA_ACTION_TRANSFORM + SPACE
                                                                       + METADATA_RESULT + "The data have an incorrect number of channels" + TERMINATOR,
                                                                   SOURCE,
                                                                   clock);
                                boolLogged = true;
                                }
                            }
                        }
                    else
                        {
                        // Only log once in the loop
                        if (!boolLogged)
                            {
                            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_XYDATASET
                                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                                   + METADATA_RESULT + "The data are not in the correct format" + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            boolLogged = true;
                            }
                        }
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_XYDATASET
                                                       + METADATA_ACTION_TRANSFORM + SPACE
                                                       + METADATA_RESULT + "The data contain data which cannot be parsed into a Double" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (ClassCastException exception)
                {
                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_XYDATASET
                                                       + METADATA_ACTION_TRANSFORM + SPACE
                                                       + METADATA_RESULT + "Unsupported DataType" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_XYDATASET
                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                   + METADATA_RESULT + "No data available to create an XYSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

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
        final String SOURCE = "LinearTransformFilter.filterXYSeries() ";
        XYSeries seriesOutput;
        final double dblScaleFactor;
        final double dblOffset;

        seriesOutput = null;

        dblScaleFactor = readParameterValue(getParameters(), NAME_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
        dblOffset = readParameterValue(getParameters(), NAME_OFFSET, DEFAULT_OFFSET);

        if ((xydataset != null)
            && (xydataset.getSeriesCount() > 0)
            && (channelindex >= 0)
            && (channelindex < xydataset.getSeriesCount())
            && (xydataset.getSeries(channelindex) != null))
            {
            final int intChannelIndex;
            final String strChannelName;
            final XYSeries seriesInput;
            final List<XYDataItem> listInputData;

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

            listInputData = seriesInput.getItems();

            seriesOutput = new XYSeries(strChannelName);

            // Move along the List of input data
            for (int intInputIndex = 0;
                 intInputIndex < listInputData.size();
                 intInputIndex++)
                {
                final XYDataItem inputItem;

                inputItem = listInputData.get(intInputIndex);

                if (inputItem != null)
                    {
                    // Retrieve the data for the selected Channel
                    if ((inputItem.getX() != null)
                        && (inputItem.getY() != null))
                        {
                        double dblTransformed;

                        dblTransformed = inputItem.getY().doubleValue();
                        dblTransformed = (dblTransformed * dblScaleFactor) + dblOffset;

                        // Use addOrUpdate() to avoid Exceptions from duplicates...
                        seriesOutput.addOrUpdate(inputItem.getX(), dblTransformed);
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_XYDATASET
                                                               + METADATA_ACTION_TRANSFORM + SPACE
                                                               + METADATA_RESULT + "The channel X or Y is null" + TERMINATOR,
                                                           SOURCE,
                                                           clock);
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_XYDATASET
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
                                               METADATA_TARGET_XYDATASET
                                                   + METADATA_ACTION_TRANSFORM + SPACE
                                                   + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (seriesOutput);
        }
    }