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

package org.lmn.fc.common.datatranslators.johncook;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChartUIHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.regex.Pattern;


/***************************************************************************************************
 * JohnCookTranslatorHelper.
 */

public class JohnCookTranslatorHelper implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 FrameworkRegex,
                                                 ResourceKeys
    {
    private static final String OBSERVATORY_NAME = "John Cook Observatory";
    private static final String OBSERVER_NAME = "John Cook";
    private static final String OBSERVATION_TITLE_SOLO = "Observation";
    private static final String OBSERVATION_TIME_START = "00:00:00";
    private static final String AXIS_LABEL_X = "Time UT";
    private static final String DATE_DELIMITER = "-";


    /***********************************************************************************************
     * Add the TranslatorMetadata.
     *
     * @param translator
     * @param filename
     */

    public static void addTranslatorMetadata(final DataTranslatorInterface translator,
                                             final String filename)
        {
        //------------------------------------------------------------------------------------------
        // Observatory

        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATORY_NAME.getKey(),
                                          OBSERVATORY_NAME,
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          EMPTY_STRING);

        //------------------------------------------------------------------------------------------
        // Observer

        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVER_NAME.getKey(),
                                          OBSERVER_NAME,
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          EMPTY_STRING);

        //------------------------------------------------------------------------------------------
        // Observation

        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                          OBSERVATION_TITLE_SOLO + SPACE + extractObservationDate(filename),
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          EMPTY_STRING);

        // The Observation.Date is in the standard format YYYY-MM-DD.
        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_DATE.getKey(),
                                          extractObservationDate(filename),
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          EMPTY_STRING);

        // ToDo Observations are assumed to start at midnight
        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIME.getKey(),
                                          OBSERVATION_TIME_START,
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          EMPTY_STRING);

        // We know that the observation times are in UTC
        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_TIMEZONE.getKey(),
                                          REGISTRY.getGMTTimeZone().getDisplayName(),
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          EMPTY_STRING);

        //------------------------------------------------------------------------------------------
        // RawDataMetadata:Channels

        // ToDO review type!!
        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
                                          Integer.toString(translator.getRawDataChannelCount()),
                                          REGEX_NONE,
                                          DataTypeDictionary.DECIMAL_INTEGER,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The number of data channels");

        // Channel Labels
        // Provide data for the ControlPanel display
        // These end up on ControlPanel Tooltips

        for (int i = 0;
             i < translator.getRawDataChannelCount();
             i++)
            {
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + i,
                                              "Channel " + i,
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "Channel " + i + " Name");

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + i,
                                              ChartUIHelper.getStandardColour(i).toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "Channel " + i + " Colour");

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + i,
                                              "Channel " + i,
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "Channel " + i + " Description");

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_VIRTUAL.getKey() + i,
                                              "Channel " + i,
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "Virtual Channel " + i + " Name");

            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_VIRTUAL.getKey() + i,
                                              "?",
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "Virtual Channel " + i + " Value");
            }

        // Axis Labels
        // There's not necessarily as many Axes as there are Channels...

        // Axis.X is Time
        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                          AXIS_LABEL_X,
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The label for the X axis");

        // These end up on ControlPanel Value Units
        // Axis.Y.0 is the signal axis
        translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                          "Output",
                                          REGEX_NONE,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The label for the Y axis");

        if (translator.getRawDataChannelCount() >= 2)
            {
            // Axis.Y.1 is the Magnetometer signal axis
            translator.addMetadataToContainer(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ONE,
                                              SchemaUnits.N_T.toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "The label for the Magnetometer X axis");
            }
        }


    /***********************************************************************************************
     * Extract the Observation.Date from the specified filename.
     * This is effectively more Metadata.
     * Return the standard ISO format YYYY-MM-DD.
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

        // The supplied format is of the form: xx/xx/20050531.dat
        // For which we must return: 2005-05-31
        pattern = Pattern.compile(FrameworkRegex.REGEX_DATE_YYMMDD);
        strTokens = pattern.split(filename);

        buffer = new StringBuffer(filename.substring(strTokens[0].length(),
                                                     filename.length() - strTokens[1].length()));
        // We now have: 20050531
        buffer.insert(4, DATE_DELIMITER);

        // We now have: 2005-0531
        buffer.insert(7, DATE_DELIMITER);

        // We now have: 2005-05-31
        return (buffer.toString());
        }


//    /***********************************************************************************************
//     * Add the TranslatorMetadata.
//     *
//     * @param translator
//     * @param filename
//     */
//
//    public static void addTranslatorMetadata(final DataTranslatorInterface translator,
//                                              final String filename)
//        {
//        //------------------------------------------------------------------------------------------
//        // Observatory
//
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATORY_NAME.getKey(),
//                               OBSERVATORY_NAME,
//                               Utilities.STRING,
//                               SchemaUnits.DIMENSIONLESS);
//
//        //------------------------------------------------------------------------------------------
//        // Observer
//
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVER_NAME.getKey(),
//                               OBSERVER_NAME,
//                               Utilities.STRING,
//                               SchemaUnits.DIMENSIONLESS);
//
//        //------------------------------------------------------------------------------------------
//        // Observation
//
//        if (translator.getRawDataChannelCount() == 1)
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
//                                   OBSERVATION_TITLE_SOLO + SPACE + extractObservationDate(filename),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//        else
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
//                                   OBSERVATION_TITLE + SPACE + extractObservationDate(filename),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        // The Observation.Date is in the standard format YYYY-MM-DD.
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_DATE.getKey(),
//                               extractObservationDate(filename),
//                               Utilities.STRING,
//                               SchemaUnits.DIMENSIONLESS);
//
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_TIME.getKey(),
//                               OBSERVATION_TIME_START,
//                               Utilities.STRING,
//                               SchemaUnits.DIMENSIONLESS);
//
//        // We know that the observation times are in UTC
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_TIMEZONE.getKey(),
//                               REGISTRY.getGMTTimeZone().getDisplayName(),
//                               Utilities.STRING,
//                               SchemaUnits.DIMENSIONLESS);
//
//        //------------------------------------------------------------------------------------------
//        // Channels
//
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
//                               Integer.toString(translator.getRawDataChannelCount()),
//                               Utilities.DECIMAL_INTEGER,
//                               SchemaUnits.DIMENSIONLESS);
//
//        //------------------------------------------------------------------------------------------
//        // Channel Labels
//        // These end up on ControlPanel Tooltips
//
//        if (translator.getRawDataChannelCount() == 1)
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + "0",
//                                   "Channel 0",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//        else
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + "0",
//                                   "20.9" + SPACE + Units.K_HZ.toString(),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        if (translator.getRawDataChannelCount() >= 2)
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + "1",
//                                   "23.4" + SPACE + Units.K_HZ.toString(),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        if (translator.getRawDataChannelCount() >= 3)
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + "2",
//                                   "AMR" + SPACE + SchemaUnits.N_T.toString(),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        // Provide data for the ControlPanel display
//
//        if (translator.getRawDataChannelCount() == 1)
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_VIRTUAL.getKey() + "0",
//                                   "Channel 0",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_VIRTUAL.getKey() + "0",
//                                   "Output",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//        else
//            {
//            // VLF Receivers know that Virtual Channel 0 is Frequency
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_VIRTUAL.getKey() + "0",
//                                   "Frequency",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_VIRTUAL.getKey() + "0",
//                                   "20.9",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        if (translator.getRawDataChannelCount() >= 2)
//            {
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_VIRTUAL.getKey() + "1",
//                                   "Frequency",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE_VIRTUAL.getKey() + "1",
//                                   "23.4",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        //------------------------------------------------------------------------------------------
//        // Axis Labels
//        // There's not necessarily as many Axes as there are Channels...
//
//        // Axis.X is Time
//        translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
//                               AXIS_LABEL_X,
//                               Utilities.STRING,
//                               SchemaUnits.DIMENSIONLESS);
//
//        // These end up on ControlPanel Value Units
//        if (translator.getRawDataChannelCount() == 1)
//            {
//            // Axis.Y.0 is the signal axis
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + "0",
//                                   "Output",
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//        else
//            {
//            // Axis.Y.0 is the VLF signal axis
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + "0",
//                                   Units.M_V.toString(),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//
//        if (translator.getRawDataChannelCount() >= 2)
//            {
//            // Axis.Y.1 is the StaribusMagnetometer signal axis
//            translator.addNewMetadata(MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + "1",
//                                   SchemaUnits.N_T.toString(),
//                                   Utilities.STRING,
//                                   SchemaUnits.DIMENSIONLESS);
//            }
//        }
    }
