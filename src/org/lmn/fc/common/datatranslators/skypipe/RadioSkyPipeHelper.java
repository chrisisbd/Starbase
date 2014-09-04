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

package org.lmn.fc.common.datatranslators.skypipe;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.maths.Mathematics;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChartUIHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/***************************************************************************************************
 * RadioSkyPipeHelper.
 */

public final class RadioSkyPipeHelper implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 FrameworkRegex,
                                                 ResourceKeys,
                                                 AstronomyConstants
    {
    // Remember that RSP day ONE must map to the first instant of 1900-01-01, or zero mSec
    // A Java calendar creates 1970-based dates from January 1, 1970 00:00:00.000 GMT (Gregorian)
    // so step forward 25,568 days, to avoid roll() or add() with their bugs :-)
    // See: http://www.timeanddate.com/date/duration.html
    private static final double RSP_DAY_CORRECTION = 25568.0;

    private static final int IBM_YEAR_ZERO = -70;


    /***********************************************************************************************
     * Add the Metadata retrieved from the RadioSkyPipeHeader.
     * Also add some metadata synthesised from what we know about the data.
     *
     * @param translator
     * @param skypipeheader
     * @param debug
     */

    public static void addMetadataFromHeader(final DataTranslatorInterface translator,
                                             final RadioSkyPipeHeader skypipeheader,
                                             final boolean debug)
        {
        final Enumeration enumKeys;

        if ((translator != null)
            && (skypipeheader != null))
            {
            translator.clearMetadata();

            //------------------------------------------------------------------------------------------
            // Metadata:Observatory

            final DegMinSecInterface dmsLongitude;
            final DegMinSecInterface dmsLatitude;

            dmsLongitude = new LongitudeDataType(skypipeheader.getLongitude());
            dmsLatitude = new LatitudeDataType(skypipeheader.getLatitude());

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATORY_NAME.getKey(),
                                              skypipeheader.getLocalName(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATORY_NAME);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey(),
                                              dmsLongitude.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.SIGNED_LONGITUDE,
                                              SchemaUnits.DEG_MIN_SEC,
                                              MetadataFactory.DESCRIPTION_OBSERVATORY_LONGITUDE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey(),
                                              dmsLatitude.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.LATITUDE,
                                              SchemaUnits.DEG_MIN_SEC,
                                              MetadataFactory.DESCRIPTION_OBSERVATORY_LATITUDE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATORY_LOCATION.getKey(),
                                              skypipeheader.getLocation(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATORY_LOCATION);

            //------------------------------------------------------------------------------------------
            // Metadata:Observer

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVER_NAME.getKey(),
                                              skypipeheader.getAuthor(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVER_NAME);

            //------------------------------------------------------------------------------------------
            // Metadata:Observation

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                              skypipeheader.getPlainNoteText(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_TITLE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey(),
                                              ChronosHelper.toDateString(doubleToCalendar(skypipeheader,
                                                                                          skypipeheader.getStart(),
                                                                                          skypipeheader.getTimeZone(),
                                                                                          debug)),
                                              REGEX_NONE,
                                              DataTypeDictionary.DATE_YYYY_MM_DD,
                                              SchemaUnits.YEAR_MONTH_DAY,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_START_DATE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey(),
                                              ChronosHelper.toTimeString(doubleToCalendar(skypipeheader,
                                                                                          skypipeheader.getStart(),
                                                                                          skypipeheader.getTimeZone(),
                                                                                          debug)),
                                              REGEX_NONE,
                                              DataTypeDictionary.TIME_HH_MM_SS,
                                              SchemaUnits.HOUR_MIN_SEC,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_START_TIME);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey(),
                                              ChronosHelper.toDateString(doubleToCalendar(skypipeheader,
                                                                                          skypipeheader.getFinish(),
                                                                                          skypipeheader.getTimeZone(),
                                                                                          debug)),
                                              REGEX_NONE,
                                              DataTypeDictionary.DATE_YYYY_MM_DD,
                                              SchemaUnits.YEAR_MONTH_DAY,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_DATE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey(),
                                              ChronosHelper.toTimeString(doubleToCalendar(skypipeheader,
                                                                                          skypipeheader.getFinish(),
                                                                                          skypipeheader.getTimeZone(),
                                                                                          debug)),
                                              REGEX_NONE,
                                              DataTypeDictionary.TIME_HH_MM_SS,
                                              SchemaUnits.HOUR_MIN_SEC,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_TIME);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIMEZONE.getKey(),
                                              normaliseRspTimeZoneID(skypipeheader.getTimeZoneAsDouble()),
                                              REGEX_NONE,
                                              DataTypeDictionary.TIME_ZONE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_TIME_ZONE);

            if (skypipeheader.isUT())
                {
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIMESYSTEM.getKey(),
                                                  TimeSystem.UT.getMnemonic(),
                                                  REGEX_NONE,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_OBSERVATION_TIMESYSTEM);
                }
            else
                {
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIMESYSTEM.getKey(),
                                                  "??",
                                                  REGEX_NONE,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_OBSERVATION_TIMESYSTEM);
                }

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_NOTES.getKey(),
                                              skypipeheader.getPlainNoteText(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_NOTES);

            //--------------------------------------------------------------------------------------
            // RSP Header Metadata

            enumKeys = skypipeheader.getMetaData().keys();

            while (enumKeys.hasMoreElements())
                {
                final String key;
                final String value;

                key = (String)enumKeys.nextElement();
                value = (String)skypipeheader.getMetaData().get(key);
                translator.addMetadataToContainer(key,
                                                  value,
                                                  REGEX_NONE,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_RSP_METADATA);
                }

            // RSP Header Parameters
            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_VERSION.getKey(),
                                              skypipeheader.getVersion(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_RSP_VERSION);
            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_SOURCE.getKey(),
                                              skypipeheader.getSource(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_RSP_SOURCE);
            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_NOTE_LENGTH.getKey(),
                                              Integer.toString(skypipeheader.getNoteLength()),
                                              REGEX_NONE,
                                              DataTypeDictionary.DECIMAL_INTEGER,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_RSP_NOTE_LENGTH);
            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_NOTE_RAW.getKey(),
                                              skypipeheader.getRawNoteText(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_RSP_RAW_NOTE);
            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_TIMESTAMPED.getKey(),
                                              Boolean.toString(skypipeheader.isDataTimestamped()),
                                              REGEX_NONE,
                                              DataTypeDictionary.BOOLEAN,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_RSP_TIMESTAMPED);
            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_DOUBLE.getKey(),
                                              Boolean.toString(skypipeheader.isDataDouble()),
                                              REGEX_NONE,
                                              DataTypeDictionary.BOOLEAN,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_RSP_DOUBLE);

            //------------------------------------------------------------------------------------------
            // RawDataMetadata:Channels

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
                                              Integer.toString(skypipeheader.getChannelCount()),
                                              REGEX_CHANNEL_COUNT,
                                              DataTypeDictionary.DECIMAL_INTEGER,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_COUNT);

            for (int i = 0;
                 i < skypipeheader.getChannelLabels().length;
                 i++)
                {
                // Channel Names
                // These end up on ControlPanel Tooltips
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + i,
                                                  skypipeheader.getChannelLabels()[i],
                                                  REGEX_NONE,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_CHANNEL_NAME + i);
                }

            // Add some default Colours and DataTypes too
            for (int i = 0;
                 i < skypipeheader.getChannelCount();
                 i++)
                {
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + i,
                                                  ChartUIHelper.getStandardColour(i).toString(),
                                                  REGEX_NONE,
                                                  DataTypeDictionary.COLOUR_DATA,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + i);
                }

            // Channel Offsets
            for (int i = 0;
                 i < skypipeheader.getChannelOffsets().length;
                 i++)
                {
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_OFFSET.getKey() + i,
                                                  Integer.toString(skypipeheader.getChannelOffsets()[i]),
                                                  REGEX_NONE,
                                                  DataTypeDictionary.DECIMAL_INTEGER,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_CHANNEL_OFFSET + i);
                }

            // Axes
            // There's not necessarily as many Axes as there are Channels...

            // Axis.X is Time
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                              skypipeheader.getAxisLabelX(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHART_AXIS_X);

            // These end up on ControlPanel Value Units
            // ToDo support multiple channels
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                              skypipeheader.getAxisLabelY(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHART_AXIS_Y_0);

            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_MAX_Y.getKey(),
                                              Double.toString(skypipeheader.getMaxY()),
                                              REGEX_NONE,
                                              DataTypeDictionary.DECIMAL_DOUBLE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHART_AXIS_Y_MAX);

            translator.addMetadataToContainer(MetadataDictionary.KEY_RSP_MIN_Y.getKey(),
                                              Double.toString(skypipeheader.getMinY()),
                                              REGEX_NONE,
                                              DataTypeDictionary.DECIMAL_DOUBLE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHART_AXIS_Y_MIN);
            }
        }


    //----------------------------------------------------------------------------------------------
    // When creating a TimeZone, the specified custom time zone ID is normalized in the following syntax:
    //
    // NormalizedCustomID:
    //         GMT Sign TwoDigitHours : Minutes
    // Sign: one of
    //         + -
    // TwoDigitHours:
    //         Digit Digit
    // Minutes:
    //         Digit Digit
    // Digit: one of
    //         0 1 2 3 4 5 6 7 8 9
    //
    // For example, TimeZone.getTimeZone("GMT-8").getID() returns "GMT-08:00".
    //
    /***********************************************************************************************
     * Take an RSP TimeZone in Hours and convert to a normalised TimeZone ID,
     * e.g. GMT+08:10.
     *
     * @param timezonehours
     *
     * @return String
     */

    public static String normaliseRspTimeZoneID(final double timezonehours)
        {
        final StringBuffer buffer;
        double dblHours;
        double dblMinutes;

        buffer = new StringBuffer();

        // Make sure that the Hours are in the range {0...24}
        dblHours = Mathematics.truncate(Math.abs(timezonehours));

        while(dblHours >= AstronomyConstants.HOURS_PER_DAY)
            {
            dblHours -= AstronomyConstants.HOURS_PER_DAY;
            }

        dblMinutes = Mathematics.fraction(timezonehours) * AstronomyConstants.MINUTES_PER_HOUR;

        // Prevent rounding embarrassment
        while (dblMinutes >= AstronomyConstants.MINUTES_PER_HOUR)
            {
            dblMinutes -= 1.0;
            }

        buffer.append("GMT");

        if (timezonehours >= 0.0)
            {
            buffer.append(PLUS);
            }
        else
            {
            buffer.append(MINUS);
            }

        // ASSUME that the RSP timezone is hours and fractional hours
        buffer.append(DecimalFormatPattern.HOURS.format(dblHours));
        buffer.append(DecimalFormatPattern.HOURS.getDelimiter());
        buffer.append(DecimalFormatPattern.MINUTES.format(dblMinutes));

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert a SkyPipe Visual Basic double representation of a Date to a Calendar.
     * I have <b>assumed</b> that 1900-01-01 is day ONE, not zero, to ensure that
     * the SkyPipe data decodes correctly. Days less than one throw an exception.
     *
     * @param header
     * @param days
     * @param timezone
     * @param debug
     *
     * @return Calendar
     */

    public static Calendar doubleToCalendar(final RadioSkyPipeHeader header,
                                            final double days,
                                            final TimeZone timezone,
                                            final boolean debug)
        {
        final GregorianCalendar calendar;

        if ((header == null)
            || (days < 0.0))
            {
            throw new FrameworkException(FrameworkStrings.EXCEPTION_PARAMETER_INVALID);
            }

        calendar = new GregorianCalendar();

        // Were the data recorded in UT?
        if (header.isUT())
            {
            calendar.setTimeZone(timezone);
            //LOGGER.debugTimedEvent("RadioSkyPipeHeader shows data were recorded in UT");
            }
        else
            {
            //LOGGER.debugTimedEvent("RadioSkyPipeHeader TimeZone defaulting to " + calendar.getTimeZone().getDisplayName());
            }

        // See: http://www.timeanddate.com/date/durationresult.html?d1=1&m1=1&y1=1900&d2=1&m2=1&y2=1970

        // From and including: Monday, 1 January 1900
        // To, but not including : Thursday, 1 January 1970
        //
        // It is 25,567 days from the start date to the end date, but not including the end date
        //
        // Or 70 years excluding the end date
        // Alternative time units
        // 25,567 days can be converted to one of these units:
        //
        //    2,208,988,800 seconds
        //    36,816,480 minutes
        //    613,608 hours
        //    3652 weeks (rounded down)

        if (days > 1.0)
            {
            // The number of days since midnight, Dec 30, 1899
            // Remember that RSP day ONE must map to the first instant of 1900-01-01, or zero mSec
            // A Java calendar creates 1970-based dates from January 1, 1970 00:00:00.000 GMT (Gregorian)
            // so step forward 25,568 days, to avoid roll() or add() with their bugs :-)
            calendar.setTimeInMillis((long)((days - RSP_DAY_CORRECTION) * ChronosHelper.DAY_MILLISECONDS));
            }
        else
            {
            LOGGER.error("WARNING! RadioSkyPipe Header Date shows Zero Days!");
            LOGGER.error("WARNING! Is this indicating an incomplete record? [days=" + days + "]");
            LOGGER.error("Check that your RadioSkyPipe is configured correctly for use with Starbase");

            // Bodgery of the highest order to cope with using days=0
            // as a flag for an incomplete observation
            // Remember that using this system, day 0 = day 1 = 1900-01-01
            calendar.setTimeInMillis(0);
            }

//        LOGGER.debug(debug, "****************************************");
//        LOGGER.debug(debug, "DAYS = " + days + " (two subtracted when setTimeInMillis() called)");
//        LOGGER.debug(debug, "BEFORE ROLL " + calendar.toString());
//        LOGGER.debug(debug, ChronosHelper.toCalendarString(calendar));
//
//        // Adjust for 1900 based dates by rolling back 70 years...
//        calendar.roll(Calendar.YEAR, IBM_YEAR_ZERO);
//
//        LOGGER.debug(debug, "AFTER ROLL " + calendar.toString());
//        LOGGER.debug(debug, ChronosHelper.toCalendarString(calendar));
//        LOGGER.debug(debug, "****************************************");

        return (calendar);
        }
    }
