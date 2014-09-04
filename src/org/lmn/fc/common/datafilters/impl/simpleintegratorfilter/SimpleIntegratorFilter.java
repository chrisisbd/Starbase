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

package org.lmn.fc.common.datafilters.impl.simpleintegratorfilter;

import org.jfree.data.time.*;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilter;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ParameterHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * SimpleIntegratorFilter.
 *
 * See: http://en.wikipedia.org/wiki/Low-pass_filter
 *
 * Algorithmic implementation
 *
 * The filter recurrence relation provides a way to determine the output samples
 * in terms of the input samples and the preceding output.
 * The following pseudocode algorithm will simulate the effect of a low-pass filter on a series of digital samples:
 *
 *  Return RC low-pass filter output samples, given input samples,
 *  time interval dt, and time constant RC
 * function lowpass(real[0..n] x, real dt, real RC)
 * var real[0..n] y
 * var real ? = dt / (RC + dt)
 * y[0] := x[0]
 * for i from 1 to n
 * y[i] = ? * x[i] + (1-?) * y[i-1]
 * return y
 *
 * The loop that calculates each of the n outputs can be refactored into the equivalent:
 *
 * for i from 1 to n
 * y[i] = y[i-1] + ? * (x[i] - y[i-1])
 *
 * That is, the change from one filter output to the next is proportional to the
 * difference between the previous output and the next input.
 * This exponential smoothing property matches the exponential decay
 * seen in the continuous-time system.
 * As expected, as the time constant RC increases, the discrete-time smoothing parameter
 * alpha decreases, and the output samples (y_1,y_2,\ldots,y_n)
 * respond more slowly to a change in the input samples (x_1,x_2,\ldots,x_n) �
 * the system will have more inertia. This filter is an infinite-impulse-response (IIR) single-pole lowpass filter.
 */

public final class SimpleIntegratorFilter extends DataFilter
                                          implements DataFilterInterface
    {
    // String Resources
    private static final String NAME_TIME_CONSTANT = "Filter.TimeConstant";
    private static final String REGEX_TIME_CONSTANT = "^([1-9][0-9]{0,2}|1000)$";
    private static final String TOOLTIP_TIME_CONSTANT = "Set the Time Constant in seconds for the Integrator";

    // Parameters
    private static final int PARAMETER_COUNT = 1;
    private static final int SUB_PARAMETER_INDEX_TIME_CONSTANT = 1;
    private static final int DEFAULT_TIME_CONSTANT = 10;


    /**********************************************************************************************/
    /* Filter RawData                                                                             */
    /***********************************************************************************************
     * Create an Integrated TimeSeries from these data, for the specified channelindex,
     * and integrating over the specified time interval in seconds.
     * If the combination of timeconstant and sample rate is not appropriate for averaging,
     * just return a plain unintegrated TimeSeries.
     *
     * @param rawdata
     * @param channelindex
     * @param channelname
     * @param timeconstant
     * @param eventlog
     * @param clock
     * @param filtertype
     *
     * @return TimeSeries
     */

    private static TimeSeries createIntegratedTimeSeriesForChannel(final Vector<Object> rawdata,
                                                                   final int channelindex,
                                                                   final String channelname,
                                                                   final int timeconstant,
                                                                   final Vector<Vector> eventlog,
                                                                   final ObservatoryClockInterface clock,
                                                                   final DataFilterType filtertype)
        {
        TimeSeries timeSeries;

        timeSeries = null;

        if ((rawdata != null)
            && (rawdata.size() > 0))
            {
            final int intChannelIndex;
            final String strChannelName;
            final int intTimeConstant;
            final int intSamplesToAverage;

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

            if (timeconstant <= 0)
                {
                intTimeConstant = 1;
                }
            else
                {
                intTimeConstant = timeconstant;
                }

            // Calculate the SamplesToAverage in order to produce the required TimeConstant
            intSamplesToAverage = getSampleCountToAverage(rawdata, intTimeConstant);

            // Can we do the integration?
            if (intSamplesToAverage > 0)
                {
                if (channelindex == 0)
                    {
                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_INTEGRATING
                                                           + METADATA_SAMPLES + intSamplesToAverage + TERMINATOR,
                                                       filtertype.getName(),
                                                       clock);
                    }

                // This series may be shorter than the original,
                // if there's not enough data to integrate the last period
                timeSeries = performIntegrationForChannel(rawdata,
                                                          intChannelIndex,
                                                          strChannelName,
                                                          intSamplesToAverage,
                                                          eventlog,
                                                          clock,
                                                          filtertype);
                }
            else
                {
                // There's just not enough samples for a sensible integration
                if (channelindex == 0)
                    {
                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_CREATE
                                                       + METADATA_RESULT + "Integration not possible, insufficient data samples for integration period?" + TERMINATOR,
                                                   filtertype.getName(),
                                                   clock);
                    }

                // This series should always be the same length as the original
                timeSeries = DataFilterHelper.createUnfilteredTimeSeriesFromList(rawdata,
                                                                                 intChannelIndex,
                                                                                 strChannelName,
                                                                                 eventlog,
                                                                                 clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlog,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_INTEGRATING
                                                   + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               filtertype.getName(),
                                               clock);
            }

        return (timeSeries);
        }


    /***********************************************************************************************
     * Integrate the specified data, to produce a TimeSeries.
     * This *assumes* that all parameters have been validated, e.g. the input RawData exists.
     *
     * @param samples
     * @param channelindex
     * @param channelname
     * @param samplestoaverage
     * @param eventlog
     * @param clock
     * @param filtertype
     *
     * @return TimeSeries
     */

    private static TimeSeries performIntegrationForChannel(final Vector<Object> samples,
                                                           final int channelindex,
                                                           final String channelname,
                                                           final int samplestoaverage,
                                                           final Vector<Vector> eventlog,
                                                           final ObservatoryClockInterface clock,
                                                           final DataFilterType filtertype)
        {
        final TimeSeries seriesSamples;
        final Iterator iterSamples;
        Calendar calIntegrated;
        double dblIntegrated;
        int intIntegrationCounter;

        // Make a TimeSeries based on Seconds...
        seriesSamples = new TimeSeries(channelname, Second.class);

        // All samples must share the same TimeZone
        iterSamples = samples.iterator();

        while ((samplestoaverage > 1)
            && (iterSamples != null)
            && (iterSamples.hasNext()))
            {
            boolean boolIntegratedCompleteSet;

            // Make sure that each integration set is complete,
            // otherwise the graph might show odd values at the end
            boolIntegratedCompleteSet = true;

            // Reset the Calendar of each group of integrated data
            calIntegrated = null;

            // Always integrate into a double, regardless of the data class
            dblIntegrated = 0.0;

            // Average over sets of the calculated number of samples,
            // to produce the required TimeConstant
            for (intIntegrationCounter = 0;
                 ((intIntegrationCounter < samplestoaverage)
                  && (boolIntegratedCompleteSet));
                 intIntegrationCounter++)
                {
                if (iterSamples.hasNext())
                    {
                    final Vector vecSample;

                    vecSample = (Vector) iterSamples.next();

                    // There must be at least one Calendar and one data sample
                    if ((vecSample != null)
                        && (vecSample.size() > 1))
                        {
                        Object objItem;

                        objItem = vecSample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                        if ((objItem != null)
                            && (objItem instanceof Calendar)
                            && (channelindex >= 0)
                            && (channelindex < (vecSample.size() - 1)))
                            {
                            // Capture only the first Calendar of the integrated group,
                            // for speed
                            if (calIntegrated == null)
                                {
                                calIntegrated = (Calendar)objItem;
                                }

                            // Retrieve the data for the selected Channel
                            objItem = vecSample.get(channelindex + DataTranslatorInterface.INDEX_TIMESTAMPED_DATA);

                            if (objItem != null)
                                {
                                // ToDo use reflection to construct the data type to avoid assumptions!
                                if (objItem instanceof Number)
                                    {
                                    dblIntegrated = dblIntegrated + ((Number) objItem).doubleValue();
                                    }
                                else
                                    {
                                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_TIMESERIES
                                                                           + METADATA_ACTION_INTEGRATING
                                                                           + METADATA_RESULT + "Unsupported DataType" + TERMINATOR,
                                                                       filtertype.getName(),
                                                                       clock);
                                    }
                                }
                            else
                                {
                                SimpleEventLogUIComponent.logEvent(eventlog,
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_TIMESERIES
                                                                       + METADATA_ACTION_INTEGRATING
                                                                       + METADATA_RESULT + "The channel data item is null" + TERMINATOR,
                                                                   filtertype.getName(),
                                                                   clock);
                                }
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(eventlog,
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_TIMESERIES
                                                                   + METADATA_ACTION_INTEGRATING
                                                                   + METADATA_RESULT + "The data sample has no Calendar, or the channel is incorrect" + TERMINATOR,
                                                               filtertype.getName(),
                                                               clock);
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(eventlog,
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_TIMESERIES
                                                               + METADATA_ACTION_INTEGRATING
                                                               + METADATA_RESULT + "The sample data are not in the correct format" + TERMINATOR,
                                                           filtertype.getName(),
                                                           clock);
                        }
                    }
                else
                    {
                    // There are not enough samples to make up this set to be integrated
                    boolIntegratedCompleteSet = false;
                    }
                }

            // Create a new sample, but only after each integration period...

            // If there is a Calendar, we must have started to capture the last sequence
            if (calIntegrated != null)
                {
                // Check that this sequence is complete
                if ((boolIntegratedCompleteSet)
                    && (intIntegrationCounter == samplestoaverage))
                    {
                    // ToDo use reflection to construct the data type to avoid assumptions!
                    // ToDo Second() Deprecated. As of 1.0.13, use the constructor that specifies the locale also
                    // Use addOrUpdate() to avoid Exceptions from duplicates...
                    seriesSamples.addOrUpdate(new Second(calIntegrated.getTime(),
                                                         calIntegrated.getTimeZone()),
                                              (dblIntegrated/samplestoaverage));
                    }
                else
                    {
                    // There is a Calendar, but the sequence is incomplete,
                    // so pad it with a zero timed with the start Calendar
                    // Use addOrUpdate() to avoid Exceptions from duplicates...
                    seriesSamples.addOrUpdate(new Second(calIntegrated.getTime(),
                                                         calIntegrated.getTimeZone()),
                                              0.0);

                    // ...and another at the very last point
                    }
                }
            }

        return (seriesSamples);
        }


    /***********************************************************************************************
     * Calculate the Samples To Average in order to produce the required TimeConstant.
     * Return zero if it is not possible to calculate a sensible value.
     *
     * @param samples
     * @param timeconstant
     *
     * @return int
     */

    private static int getSampleCountToAverage(final Vector<Object> samples,
                                               final int timeconstant)
         {
         int intSamplesToAverage;

         // Prepare for error return
         intSamplesToAverage = 0;

         // Do some simple checks to make sure that the data are ok
         if ((samples != null)
             && (samples.size() > 1)
             && (timeconstant > 0))
             {
             final Calendar calStart;
             final Calendar calNext;

             // There must be a valid Calendar at the first and next samples
             // Each sample must contain a Calendar and at least one sample,
             // i.e. the size > 1
             calStart = getCalendarFromSample((Vector) samples.get(0));
             calNext = getCalendarFromSample((Vector) samples.get(1));

             // Check that we found two valid Calendars moving in the right direction
             if ((calStart != null)
                && (calNext != null)
                && (calStart.before(calNext)))
                {
                int intSamplePeriod;

                // How long is the sample period in Seconds?
                intSamplePeriod = (int)(calNext.getTimeInMillis() - calStart.getTimeInMillis());
                intSamplePeriod = (int)(intSamplePeriod / ChronosHelper.SECOND_MILLISECONDS);

                // How many samples are there in one time constant?
                if ((intSamplePeriod > 0)
                    && (timeconstant > intSamplePeriod))
                    {
                    intSamplesToAverage = timeconstant/intSamplePeriod;
                    }
                else
                    {
                    // An inappropriate TimeConstant has been selected
                    intSamplesToAverage = 1;
                    }
                }
             }

         // A final check, to be certain in case of odd choices or rounding errors...
         if (intSamplesToAverage < 2)
             {
             intSamplesToAverage = 0;
             }

         return (intSamplesToAverage);
         }


    /***********************************************************************************************
     * Extract the Calendar from the Vector of sample data.
     *
     * @param sample
     *
     * @return Calendar
     */

    private static Calendar getCalendarFromSample(final Vector sample)
        {
        Calendar calendar;

        calendar = null;

        if ((sample != null)
            && (sample.size() > 1))
            {
            final Object objItem;

            objItem = sample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

            if ((objItem != null)
                && (objItem instanceof Calendar))
                {
                calendar = (Calendar)objItem;
                }
            }

        return (calendar);
        }


    /**********************************************************************************************/
    /* Filter ProcessedData                                                                       */
    /***********************************************************************************************
     * Create an Integrated TimeSeries from these data, for the specified channelindex,
     * and integrating over the specified time interval in seconds.
     * If the combination of timeconstant and sample rate is not appropriate for averaging,
     * just return a plain unintegrated TimeSeries.
     *
     * @param xydataset
     * @param channelindex
     * @param channelname
     * @param timeconstant
     * @param eventlog
     * @param clock
     * @param filtertype
     *
     * @return TimeSeries
     */

    private static TimeSeries createIntegratedTimeSeriesForChannel(final TimeSeriesCollection xydataset,
                                                                   final int channelindex,
                                                                   final String channelname,
                                                                   final int timeconstant,
                                                                   final Vector<Vector> eventlog,
                                                                   final ObservatoryClockInterface clock,
                                                                   final DataFilterType filtertype)
        {
        TimeSeries timeSeries;

        timeSeries = null;

        if ((xydataset != null)
            && (xydataset.getSeriesCount() > 0)
            && (channelindex >= 0)
            && (channelindex < xydataset.getSeriesCount())
            && (xydataset.getSeries(channelindex) != null))
            {
            final int intChannelIndex;
            final String strChannelName;
            final int intTimeConstant;
            final int intSamplesToAverage;

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

            if (timeconstant <= 0)
                {
                intTimeConstant = 1;
                }
            else
                {
                intTimeConstant = timeconstant;
                }

            // Calculate the SamplesToAverage in order to produce the required TimeConstant
            // Make sure we choose the selected Channel, because not all Series are necessarily of the same length
            intSamplesToAverage = getSampleCountToAverage(xydataset, channelindex, intTimeConstant);

            // Can we do the integration?
            if (intSamplesToAverage > 0)
                {
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_INTEGRATING
                                                       + METADATA_SAMPLES + intSamplesToAverage + TERMINATOR,
                                                   filtertype.getName(),
                                                   clock);

                // This series may be shorter than the original,
                // if there's not enough data to integrate the last period
                timeSeries = performIntegrationForChannel(xydataset,
                                                          intChannelIndex,
                                                          strChannelName,
                                                          intSamplesToAverage,
                                                          eventlog,
                                                          clock,
                                                          filtertype);
                }
            else
                {
                // There's just not enough samples for a sensible integration
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_CREATE
                                                       + METADATA_RESULT + "Integration not possible, insufficient data samples for integration period?" + TERMINATOR,
                                                   filtertype.getName(),
                                                   clock);

                // This series should always be the same length as the original
                timeSeries = DataFilterHelper.createUnfilteredTimeSeriesFromTimeSeries(xydataset,
                                                                                       intChannelIndex,
                                                                                       strChannelName,
                                                                                       eventlog,
                                                                                       clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlog,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_INTEGRATING
                                                   + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               filtertype.getName(),
                                               clock);
            }

        return (timeSeries);
        }


    /***********************************************************************************************
     * Integrate the specified data, to produce a TimeSeries.
     * This *assumes* that all parameters have been validated, e.g. the input Series exists.
     *
     * @param timeseriescollection
     * @param channelindex
     * @param channelname
     * @param samplestoaverage
     * @param eventlog
     * @param clock
     * @param filtertype
     *
     * @return TimeSeries
     */

    private static TimeSeries performIntegrationForChannel(final TimeSeriesCollection timeseriescollection,
                                                           final int channelindex,
                                                           final String channelname,
                                                           final int samplestoaverage,
                                                           final Vector<Vector> eventlog,
                                                           final ObservatoryClockInterface clock,
                                                           final DataFilterType filtertype)
        {
        final TimeSeries seriesInput;
        final TimeSeries seriesOutput;
        final List<TimeSeriesDataItem> listInputData;
        RegularTimePeriod periodIntegrated;
        double dblIntegrated;
        int intIntegrationCounter;

        // Get the TimeSeries to be integrated, which must exist
        seriesInput = timeseriescollection.getSeries(channelindex);

        // All samples must share the same TimeZone
        listInputData = seriesInput.getItems();

        seriesOutput = new TimeSeries(channelname);

        // Move along the List of input data, and integrate in chunks of the TimeConstant
        for (int intInputIndex = 0;
             intInputIndex < listInputData.size();
             intInputIndex = intInputIndex + samplestoaverage)
            {
            boolean boolIntegratedCompleteSet;

            // Make sure that each integration set is complete,
            // otherwise the graph might show odd values at the end
            boolIntegratedCompleteSet = false;

            // Reset the Period of each group of integrated data
            periodIntegrated = null;

            // Always integrate into a double, regardless of the data class
            dblIntegrated = 0.0;

            // Average over sets of the calculated number of samples,
            // to produce the required TimeConstant
            for (intIntegrationCounter = 0;
                 ((intIntegrationCounter < samplestoaverage)
                  && (intInputIndex + intIntegrationCounter < listInputData.size()));
                 intIntegrationCounter++)
                {
                final TimeSeriesDataItem inputItem;

                // Did we integrate one complete chunk?
                if (intIntegrationCounter == samplestoaverage - 1)
                    {
                    boolIntegratedCompleteSet = true;
                    }

                inputItem = listInputData.get(intInputIndex + intIntegrationCounter);

                if (inputItem != null)
                    {
                    // Capture only the first Period of the integrated group, for speed
                    if (periodIntegrated == null)
                        {
                        periodIntegrated = inputItem.getPeriod();
                        }

                    // Retrieve the data for the selected Channel
                    if (inputItem.getValue() != null)
                        {
                        dblIntegrated = dblIntegrated + inputItem.getValue().doubleValue();
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(eventlog,
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_TIMESERIES
                                                               + METADATA_ACTION_INTEGRATING
                                                               + METADATA_RESULT + "The channel data value is null" + TERMINATOR,
                                                           filtertype.getName(),
                                                           clock);
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_INTEGRATING
                                                           + METADATA_RESULT + "The channel data item is null" + TERMINATOR,
                                                       filtertype.getName(),
                                                       clock);
                    }
                }

            // Create a new sample, but only after each integration period...
            // If there is a Period, we must have started to capture the last sequence
            if (periodIntegrated != null)
                {
                // Check that this sequence is complete
                if ((boolIntegratedCompleteSet)
                    && (intIntegrationCounter == samplestoaverage))
                    {
                    // Use addOrUpdate() to avoid Exceptions from duplicates...
                    seriesOutput.addOrUpdate(periodIntegrated,
                                             (dblIntegrated / samplestoaverage));
                    }
                else
                    {
                    // There is a Period, but the sequence is incomplete,
                    // so pad it with a zero timed with the start Period
                    // Use addOrUpdate() to avoid Exceptions from duplicates...
                    seriesOutput.addOrUpdate(periodIntegrated,
                                             0.0);

                    // ...and another at the very last point??
                    }
                }
            }

        return (seriesOutput);
        }


    /***********************************************************************************************
     * Calculate the Samples To Average in order to produce the required TimeConstant.
     * This *assumes* regularly spaced data samples in the selected Channel Series!
     * Return zero if it is not possible to calculate a sensible value.
     *
     * @param timeseriescollection
     * @param channelindex
     * @param timeconstant
     *
     * @return int
     */

    private static int getSampleCountToAverage(final TimeSeriesCollection timeseriescollection,
                                               final int channelindex,
                                               final int timeconstant)
        {
        int intSamplesToAverage;

        // Prepare for error return
        intSamplesToAverage = 0;

        // Do some simple checks to make sure that the data are ok
        if ((timeseriescollection != null)
            && (channelindex >= 0)
            && (channelindex < timeseriescollection.getSeriesCount())
            && (timeseriescollection.getSeries(channelindex) != null)
            && (timeconstant > 0))
            {
            final RegularTimePeriod period0;
            final RegularTimePeriod period1;

            // Compare the first two period entries
            period0 = timeseriescollection.getSeries(channelindex).getTimePeriod(0);
            period1 = timeseriescollection.getSeries(channelindex).getTimePeriod(1);

            // Check that we found two valid Periods moving in the right direction
            if ((period0 != null)
                && (period1 != null)
                && (period1.getFirstMillisecond() > period0.getFirstMillisecond()))
                {
                int intSamplePeriod;

                // How long is the sample period in Seconds?
                intSamplePeriod = (int)(period1.getFirstMillisecond() - period0.getFirstMillisecond());
                intSamplePeriod = (int)(intSamplePeriod / ChronosHelper.SECOND_MILLISECONDS);

                // How many samples are there in one time constant?
                if ((intSamplePeriod > 0)
                    && (timeconstant > intSamplePeriod))
                    {
                    intSamplesToAverage = timeconstant/intSamplePeriod;
                    }
                else
                    {
                    // An inappropriate TimeConstant has been selected
                    intSamplesToAverage = 1;
                    }
                }
            else
                {
                // Something went wrong
                intSamplesToAverage = 0;
                }
            }

        // A final check, to be certain in case of odd choices or rounding errors...
        if (intSamplesToAverage < 2)
            {
            intSamplesToAverage = 0;
            }

        return (intSamplesToAverage);
        }


    /***********************************************************************************************
     * Construct a SimpleIntegratorFilter, one Parameter is required, the TimeConstant.
     * The Dataset for this Filter need not be a power of two in length.
     */

    public SimpleIntegratorFilter()
        {
        super(DataFilterType.SIMPLE_INTEGRATOR, PARAMETER_COUNT, false);
        }


    /***********************************************************************************************
     * Initialise the DataFilter.
     */

    public void initialiseFilter()
        {
        final ParameterType paramTimeConstant;
        final ParameterDataType inputType;
        final ParameterDataType trafficType;

        super.initialiseFilter();

        // Indicate to which DatasetTypes this Filter may be applied
        getDatasetTypes().add(DatasetType.TIMESTAMPED);

        // Only one Parameter is required, the TimeConstant
        paramTimeConstant = ParameterType.Factory.newInstance();

        inputType = ParameterDataType.Factory.newInstance();
        inputType.setDataTypeName(SchemaDataType.DECIMAL_INTEGER);
        trafficType = ParameterDataType.Factory.newInstance();
        trafficType.setDataTypeName(SchemaDataType.DECIMAL_INTEGER);

        paramTimeConstant.setName(NAME_TIME_CONSTANT);
        paramTimeConstant.setSubParameterIndex(SUB_PARAMETER_INDEX_TIME_CONSTANT);
        paramTimeConstant.setValue(Integer.toString(DEFAULT_TIME_CONSTANT));
        paramTimeConstant.setRegex(REGEX_TIME_CONSTANT);
        paramTimeConstant.setUnits(SchemaUnits.SECONDS);
        paramTimeConstant.setInputDataType(inputType);
        paramTimeConstant.setTrafficDataType(trafficType);
        paramTimeConstant.setTooltip(TOOLTIP_TIME_CONSTANT);

        getParameters().add(paramTimeConstant);
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
        final ParameterType parameter;
        final TimeSeries timeSeries;
        int intTimeConstant;

        parameter = ParameterHelper.getParameterByName(getParameters(), NAME_TIME_CONSTANT);

        if ((parameter != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(parameter.getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strTimeConstant;

                strTimeConstant = parameter.getValue();
                intTimeConstant = Integer.parseInt(strTimeConstant);
                }

            catch (NumberFormatException exception)
                {
                // This should of course never happen!
                intTimeConstant = DEFAULT_TIME_CONSTANT;
                }
            }
        else
            {
            intTimeConstant = DEFAULT_TIME_CONSTANT;
            }

        // Use the TimeConstant to produce an integrated TimeSeries for RawData
        timeSeries = createIntegratedTimeSeriesForChannel(timestampeddata,
                                                          channelindex,
                                                          channelname,
                                                          intTimeConstant,
                                                          eventlogfragment,
                                                          clock,
                                                          getFilterType());
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
        final ParameterType parameter;
        final TimeSeries timeSeries;
        int intTimeConstant;

        parameter = ParameterHelper.getParameterByName(getParameters(), NAME_TIME_CONSTANT);

        if ((parameter != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(parameter.getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strTimeConstant;

                strTimeConstant = parameter.getValue();
                intTimeConstant = Integer.parseInt(strTimeConstant);
                }

            catch (NumberFormatException exception)
                {
                // This should of course never happen!
                intTimeConstant = DEFAULT_TIME_CONSTANT;
                }
            }
        else
            {
            intTimeConstant = DEFAULT_TIME_CONSTANT;
            }

        // Use the TimeConstant to produce an integrated TimeSeries for ProcessedData
        timeSeries = createIntegratedTimeSeriesForChannel(xydataset,
                                                          channelindex,
                                                          channelname,
                                                          intTimeConstant,
                                                          eventlogfragment,
                                                          clock,
                                                          getFilterType());
        return (timeSeries);
        }
    }
