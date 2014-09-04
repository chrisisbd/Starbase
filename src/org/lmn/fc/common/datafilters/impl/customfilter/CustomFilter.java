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

package org.lmn.fc.common.datafilters.impl.customfilter;

import net.astesana.javaluator.DoubleEvaluator;
import net.astesana.javaluator.StaticVariableSet;
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
import org.lmn.fc.model.datatypes.DataTypeParserInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.*;


/***************************************************************************************************
 * CustomFilter.
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

public final class CustomFilter extends DataFilter
                                implements DataFilterInterface
    {
    // String Resources
    private static final String PARAMETER_NAME_EXPRESSION = "Filter.Function";
    private static final String TOOLTIP_EXPRESSION = "Mathematical Expression";

    // Parameters
    private static final int PARAMETER_COUNT = 1;
    private static final int SUB_PARAMETER_EXPRESSION = 1;
    private static final String DEFAULT_EXPRESSION = "sin(pi/2)";


    private final DoubleEvaluator evaluator;
    private final StaticVariableSet<Double> setVariables;


    /***********************************************************************************************
     * Construct a CustomFilter.
     */

    public CustomFilter()
        {
        // The Dataset for this Filter need not be a power of two in length
        super(DataFilterType.CUSTOM, PARAMETER_COUNT, false);

        this.evaluator = new DoubleEvaluator();
        this.setVariables = new StaticVariableSet<Double>();
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        final ParameterType parameter;
        final ParameterDataType dataType;

        super.initialiseFilter();

        // Set up the Evaluator with the default Variables
        getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_X, 0.0);
        getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_Y, 0.0);
        getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_T, 0.0);

        // Indicate to which DatasetTypes this Filter may be applied
        getDatasetTypes().add(DatasetType.TIMESTAMPED);
        getDatasetTypes().add(DatasetType.XY);

        dataType = ParameterDataType.Factory.newInstance();
        dataType.setDataTypeName(SchemaDataType.MATHEMATICAL_EXPRESSION);

        parameter = ParameterType.Factory.newInstance();
        parameter.setName(PARAMETER_NAME_EXPRESSION);
        parameter.setSubParameterIndex(SUB_PARAMETER_EXPRESSION);
        parameter.setValue(DEFAULT_EXPRESSION);
        parameter.setUnits(SchemaUnits.DIMENSIONLESS);
        parameter.setRegex(EMPTY_STRING);
        parameter.setInputDataType(dataType);
        parameter.setTrafficDataType(dataType);
        parameter.setTooltip(TOOLTIP_EXPRESSION);

        getParameters().add(parameter);
        }


    /**********************************************************************************************/
    /* Timestamped Data                                                                           */
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
        final String SOURCE = "CustomFilter.filterTimestampedListToTimeSeries() ";
        TimeSeries timeSeries;
        final String strExpression;
        boolean boolLogged;

        strExpression = ParameterHelper.getParameterValueByName(getParameters(), PARAMETER_NAME_EXPRESSION);

        //LOGGER.log(SOURCE + PARAMETER_NAME_EXPRESSION + " = " + strExpression);

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

            // Do the Filter operation
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
                                final Second secondT;

                                // ToDo Get the Locale also
                                secondT = new Second(calSample.getTime(),
                                                     calSample.getTimeZone());
                                try
                                    {
                                    final double dblY;

                                    // This Filter will map (t, y) to t, y')
                                    // So evaluate the MathematicalExpression having set both t and y
                                    // Set x to zero for 'safety' ?

                                    getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_X,
                                                                0.0);
                                    getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_Y,
                                                                ((Number) objItem).doubleValue());
                                    getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_T,
                                                                (double)(secondT.getFirstMillisecond()/1000));

                                    dblY = getEvaluator().evaluate(strExpression, getEvaluatorVariables());

                                    // Use addOrUpdate() to avoid Exceptions from duplicates...
                                    timeSeries.addOrUpdate(secondT, dblY);
                                    }

                                // This should never occur because of validation by the entry of the Filter expression on the UI
                                catch (IllegalArgumentException exception)
                                    {
                                    if (!boolLogged)
                                        {
                                        SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                           EventStatus.WARNING,
                                                                           METADATA_TARGET_TIMESERIES
                                                                               + METADATA_ACTION_TRANSFORM + SPACE
                                                                               + METADATA_RESULT + "Unable to parse MathematicalExpression] [expression="
                                                                               + strExpression + TERMINATOR_SPACE
                                                                               + "[x=0.0] [y=" + ((Number) objItem).doubleValue()
                                                                               + "] [t=" + (double)(secondT.getFirstMillisecond()/1000) + "]",
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
        final String SOURCE = "CustomFilter.filterTimeSeries() ";
        TimeSeries seriesOutput;
        final String strExpression;
        boolean boolLogged;

        strExpression = ParameterHelper.getParameterValueByName(getParameters(), PARAMETER_NAME_EXPRESSION);

        //LOGGER.log(SOURCE + PARAMETER_NAME_EXPRESSION + " = " + strExpression);

        seriesOutput = null;
        boolLogged = false;

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
                || (EMPTY_STRING.equals(channelname.trim())))
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

            // Do the Filter operation
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
                            try
                                {
                                final double dblY;

                                // This Filter will map (t, y) to t, y')
                                // So evaluate the MathematicalExpression having set both t and y
                                // Set x to zero for 'safety' ?

                                getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_X,
                                                            0.0);
                                getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_Y,
                                                            inputItem.getValue().doubleValue());
                                getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_T,
                                                            (double)(periodTransformed.getFirstMillisecond()/1000));

                                dblY = getEvaluator().evaluate(strExpression, getEvaluatorVariables());

                                // Use addOrUpdate() to avoid Exceptions from duplicates...
                                seriesOutput.addOrUpdate(periodTransformed, dblY);
                                }

                            // This should never occur because of validation by the entry of the Filter expression on the UI
                            catch (IllegalArgumentException exception)
                                {
                                if (!boolLogged)
                                    {
                                    SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_TIMESERIES
                                                                           + METADATA_ACTION_TRANSFORM + SPACE
                                                                           + METADATA_RESULT + "Unable to parse MathematicalExpression] [expression="
                                                                           + strExpression + TERMINATOR_SPACE
                                                                           + "[x=0.0] [y=" + inputItem.getValue().doubleValue()
                                                                           + "] [t=" + (double)(periodTransformed.getFirstMillisecond()/1000) + "]",
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
                                                                       + METADATA_RESULT + "The channel data value is null" + TERMINATOR,
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
                                                                   + METADATA_RESULT + "The channel period value is null" + TERMINATOR,
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


    /**********************************************************************************************/
    /* Indexed Data                                                                               */
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
        final String SOURCE = "CustomFilter.filterIndexedListToXYSeries() ";
        XYSeries xySeries;
        final String strExpression;
        boolean boolLogged;

        strExpression = ParameterHelper.getParameterValueByName(getParameters(), PARAMETER_NAME_EXPRESSION);

        //LOGGER.log(SOURCE + PARAMETER_NAME_EXPRESSION + " = " + strExpression);

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

                // Do the Filter operation
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
                            final double dblX;

                            dblX = Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_X_VALUE).toString());

                            try
                                {
                                final double dblY;

                                // This Filter will map (x, y) to x, y')
                                // So evaluate the MathematicalExpression having set both x and y
                                // Set t to zero for 'safety' ?
                                // ToDo handle datatypes supplied in listdatatypes

                                getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_X,
                                                            dblX);
                                getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_Y,
                                                            (Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex).toString())));
                                getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_T,
                                                            0.0);

                                dblY = getEvaluator().evaluate(strExpression, getEvaluatorVariables());

                                // The first item is always the index
                                // Use addOrUpdate() to avoid Exceptions from duplicates...
                                xySeries.addOrUpdate(dblX, dblY);
                                }

                            // This should never occur because of validation by the entry of the Filter expression on the UI
                            catch (IllegalArgumentException exception)
                                {
                                if (!boolLogged)
                                    {
                                    SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_TIMESERIES
                                                                           + METADATA_ACTION_TRANSFORM + SPACE
                                                                           + METADATA_RESULT + "Unable to parse MathematicalExpression] [expression="
                                                                           + strExpression + TERMINATOR_SPACE
                                                                           + "[x= " + dblX
                                                                           + "] [y=" + Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex).toString())
                                                                           + "] [t=0.0]",
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
        final String SOURCE = "CustomFilter.filterXYSeries() ";
        XYSeries seriesOutput;
        final String strExpression;
        boolean boolLogged;

        strExpression = ParameterHelper.getParameterValueByName(getParameters(), PARAMETER_NAME_EXPRESSION);

        //LOGGER.log(SOURCE + PARAMETER_NAME_EXPRESSION + " = " + strExpression);

        seriesOutput = null;
        boolLogged = false;

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

            // Do the Filter operation
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
                        final double dblX;

                        dblX = inputItem.getX().doubleValue();

                        try
                            {
                            final double dblY;

                            // This Filter will map (x, y) to x, y')
                            // So evaluate the MathematicalExpression having set both x and y
                            // Set t to zero for 'safety' ?
                            // ToDo handle datatypes supplied in listdatatypes

                            getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_X,
                                                        dblX);
                            getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_Y,
                                                        inputItem.getY().doubleValue());
                            getEvaluatorVariables().set(DataTypeParserInterface.VARIABLE_T,
                                                        0.0);

                            dblY = getEvaluator().evaluate(strExpression, getEvaluatorVariables());

                            // The first item is always the index
                            // Use addOrUpdate() to avoid Exceptions from duplicates...
                            seriesOutput.addOrUpdate(dblX, dblY);
                            }

                        // This should never occur because of validation by the entry of the Filter expression on the UI
                        catch (IllegalArgumentException exception)
                            {
                            if (!boolLogged)
                                {
                                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_TIMESERIES
                                                                       + METADATA_ACTION_TRANSFORM + SPACE
                                                                       + METADATA_RESULT + "Unable to parse MathematicalExpression] [expression="
                                                                       + strExpression + TERMINATOR_SPACE
                                                                       + "[x= " + dblX
                                                                       + "] [y=" + inputItem.getY().doubleValue()
                                                                       + "] [t=0.0]",
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
                                                                   + METADATA_RESULT + "The channel X or Y is null" + TERMINATOR,
                                                               SOURCE,
                                                               clock);
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
                                                               + METADATA_RESULT + "The channel data item is null" + TERMINATOR,
                                                           SOURCE,
                                                           clock);
                        }
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


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the MathematicalExpression Evaluator.
     *
     * @return DoubleEvaluator
     */

    private DoubleEvaluator getEvaluator()
        {
        return (this.evaluator);
        }


    /***********************************************************************************************
     * Get the MathematicalExpression Evaluator Variables.
     *
     * @return StaticVariableSet<Double>
     */

    private StaticVariableSet<Double> getEvaluatorVariables()
        {
        return (this.setVariables);
        }
    }
