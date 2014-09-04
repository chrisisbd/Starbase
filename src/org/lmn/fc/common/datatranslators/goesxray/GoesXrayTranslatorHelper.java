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

package org.lmn.fc.common.datatranslators.goesxray;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.List;
import java.util.regex.Pattern;


/***************************************************************************************************
 * GoesXrayTranslatorHelper.
 */

public final class GoesXrayTranslatorHelper implements FrameworkConstants,
                                                       FrameworkStrings,
                                                       FrameworkMetadata,
                                                       FrameworkSingletons,
                                                       FrameworkRegex,
                                                       ResourceKeys
    {
    // String Resources
    public static final String OBSERVER_NAME = "Geostationary Operations Environmental Satellite";
    public static final String TOKEN_WATTS_PER_METER_SQUARED = "Watts per meter squared";
    public static final String AXIS_LABEL_Y_0 = "Xray Flux";
    public static final String AXIS_LABEL_Y_1 = "Ratio";
    public static final String DATE_DELIMITER = "-";

    public static final int LINE_FIRST_DATA = 20;
    public static final int INDEX_HEADER_SHORT = 7;
    public static final int INDEX_HEADER_LONG = 8;
    public static final int INDEX_HEADER_SOURCE = 13;
    public static final int INDEX_HEADER_OBSERVATORY_NAME = 2;


    /***********************************************************************************************
     * Add the Metadata retrieved from the Header.
     *
     * @param translator
     * @param header
     * @param observationdate
     */

    public static void addMetadataFromHeader(final DataTranslatorInterface translator,
                                             final List<String> header,
                                             final String observationdate)
        {
        if ((translator != null)
            && (header != null)
            && (header.size() == (LINE_FIRST_DATA - 1)))
            {
            final StringBuffer buffer;

            translator.clearMetadata();

            //------------------------------------------------------------------------------------------
            // Observatory

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATORY_NAME.getKey(),
                                              header.get(INDEX_HEADER_OBSERVATORY_NAME).substring(2).trim(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATORY_NAME);

            //------------------------------------------------------------------------------------------
            // Observer

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVER_NAME.getKey(),
                                              OBSERVER_NAME,
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVER_NAME);

            //------------------------------------------------------------------------------------------
            // Observation

            buffer = new StringBuffer(header.get(INDEX_HEADER_SOURCE).substring(2).trim());
            buffer.append(FrameworkStrings.SPACE);
            buffer.append(observationdate);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                              buffer.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_TITLE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_DATE.getKey(),
                                              observationdate,
                                              REGEX_NONE,
                                              DataTypeDictionary.DATE_YYYY_MM_DD,
                                              SchemaUnits.YEAR_MONTH_DAY,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_DATE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIME.getKey(),
                                              "00:00:00",
                                              REGEX_NONE,
                                              DataTypeDictionary.TIME_HH_MM_SS,
                                              SchemaUnits.HOUR_MIN_SEC,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_TIME);

            // We know that the observation times are in UTC
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIMEZONE.getKey(),
                                              FrameworkSingletons.REGISTRY.getGMTTimeZone().getDisplayName(),
                                              REGEX_NONE,
                                              DataTypeDictionary.TIME_ZONE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_OBSERVATION_TIME_ZONE);

            //--------------------------------------------------------------------------------------
            // RawDataMetadata:Channels

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
                                              Integer.toString(translator.getRawDataChannelCount()),
                                              REGEX_CHANNEL_COUNT,
                                              DataTypeDictionary.DECIMAL_INTEGER,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_COUNT);

            // Channel Labels
            // Some of these end up on ControlPanel Tooltips
            // For correct rendering and export, Charts must receive the following metadata:
            //
            // Observation.Title
            // Observation.Channel.Name.n
            // Observation.Channel.Colour.n
            // Observation.Channel.DataType.n
            // Observation.Channel.Units.n
            // Observation.Channel.Description.n
            // Observation.Axis.Label.X
            // Observation.Axis.Label.Y.n

            //--------------------------------------------------------------------------------------
            // ToDo extract string from Header

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                              "0.05 - 0.4 nanometer",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                              SchemaDataType.DECIMAL_DOUBLE.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.DATA_TYPE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                              SchemaUnits.WM_2.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.UNITS,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                              "Channel 0 Xray Flux",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

            //--------------------------------------------------------------------------------------
            // ToDo extract string from Header

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                              "0.1 - 0.8 nanometer",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ONE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                              SchemaDataType.DECIMAL_DOUBLE.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.DATA_TYPE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ONE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                              SchemaUnits.WM_2.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.UNITS,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ONE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                              "Channel 1 Xray Flux",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ONE);

            //--------------------------------------------------------------------------------------

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                              "Ratio",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_TWO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                              SchemaDataType.DECIMAL_DOUBLE.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.DATA_TYPE,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_TWO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                              SchemaUnits.DIMENSIONLESS.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.UNITS,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_TWO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                              "Ratio of Xray Flux channels",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_TWO);

            //--------------------------------------------------------------------------------------
            // Axis Labels
            // There's not necessarily as many Axes as there are Channels...

            // Axis.X is Time
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                              "Time",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHART_AXIS_X);

            // These end up on ControlPanel Value Units
            // Axis.Y.0 is the Flux axis
            if ((header.get(INDEX_HEADER_SHORT) != null)
                && (header.get(INDEX_HEADER_SHORT).contains(TOKEN_WATTS_PER_METER_SQUARED))
                && (header.get(INDEX_HEADER_LONG) != null)
                && (header.get(INDEX_HEADER_LONG).contains(TOKEN_WATTS_PER_METER_SQUARED)))
                {
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                                  AXIS_LABEL_Y_0 + FrameworkStrings.SPACE + SchemaUnits.WM_2.toString(),
                                                  REGEX_NONE,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_CHART_AXIS_Y_0);
                }
            else
                {
                translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                                  AXIS_LABEL_Y_0,
                                                  REGEX_NONE,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  MetadataFactory.DESCRIPTION_CHART_AXIS_Y_0);
                }

            // Axis.Y.1 is the Ratio
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ONE,
                                              AXIS_LABEL_Y_1,
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHART_AXIS_Y_1);
            }
        }


    /***********************************************************************************************
     * Add the Metadata to describe the colours of the Channels on the GoesXray Chart.
     *
     * @param translator
     */

    public static void addChannelColourMetadata(final DataTranslatorInterface translator)
        {
        // For correct rendering, Charts must receive the following metadata:
        //
        //  Observation.Title
        //  Observation.Channel.Name.n
        //  Observation.Channel.Colour.n
        //  Observation.Channel.Description.n
        //  Observation.Axis.Label.X
        //  Observation.Axis.Label.Y.n
        //
        // Use the same colours as on http://www.swpc.noaa.gov/
        // blue=0.5 - 4.0A red=1.0 - 8.0A

        if (translator != null)
            {
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                              "r=000 g=000 b=255",
                                              REGEX_COLOUR,
                                              DataTypeDictionary.COLOUR_DATA,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                              "r=255 g=000 b=000",
                                              REGEX_COLOUR,
                                              DataTypeDictionary.COLOUR_DATA,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ONE);

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                              "r=000 g=255 b=000",
                                              REGEX_COLOUR,
                                              DataTypeDictionary.COLOUR_DATA,
                                              SchemaUnits.DIMENSIONLESS,
                                              MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_TWO);
            }
        }


    /***********************************************************************************************
     * Extract the Observation.Date from the specified filename.
     * Return the standard format YYYY-MM-DD.
     *
     * @param filename
     *
     * @return String
     */

    public static String extractObservationDate(final String filename)
        {
        final StringBuffer buffer;
        final Pattern pattern;
        final String[] strTokens;

        // The supplied format is of the form: xx/xx/lists/xray/20070225_G12xr_5m.txt
        // For which we must return: 2007-02-25
        // Use the same Regex for the filename as used by the DAO
        pattern = Pattern.compile(REGEX_DATE_YYMMDD);
        strTokens = pattern.split(filename);

        buffer = new StringBuffer(filename.substring(strTokens[0].length(),
                                                     filename.length() - strTokens[1].length()));
        // We now have: 20070225
        buffer.insert(4, DATE_DELIMITER);

        // We now have: 2007-0225
        buffer.insert(7, DATE_DELIMITER);

        // We now have: 2007-02-25
        return (buffer.toString());
        }
    }
