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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.ntpclient;


import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * NtpClientHelper.
 */

public final class NtpClientHelper implements FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkRegex,
                                              FrameworkSingletons
    {
    // String Resources
    public static final String TITLE_OBSERVATORY_CLOCK_OFFSET = "Offset between Observatory Clock and network time";
    public static final String TITLE_PLATFORM_CLOCK_OFFSET = "Offset between Platform Clock and network time";
    private static final String LABEL_ELAPSED_TIME = "Elapsed Time";
    private static final String LABEL_OFFSET_MSEC  = "Offset msec";
    private static final String DESCRIPTION_OFFSET = "The latest value of the time offset";
    private static final String CHANNEL_COLOUR = "r=255 g=000 b=000";
    private static final String KEY_CHANNEL_DELTA_T = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO;

    private static final int LOG_COLUMN_COUNT = 15;


    /***********************************************************************************************
     * Create the Metadata to describe the NTP Client data Channel.
     * Each Channel requires {Name, Value, DataType, Units, Description, Colour}.
     * The Value (last sample) is added by the DAO during data capture.
     * These are currently added to the DAO ObservationMetadata.
     *
     * For correct rendering and export, Charts must receive the following metadata:
     *
     * Observation.Channel.Name.n
     * Observation.Channel.Colour.n
     * Observation.Channel.DataType.n
     * Observation.Channel.Units.n
     * Observation.Channel.Description.n
     * Observation.Title
     * Observation.Axis.Label.X
     * Observation.Axis.Label.Y.n
     *
     * @param title
     * @param debug
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createNtpClientChannelMetadata(final String title,
                                                                final boolean debug)
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(9);

        // Channel Metadata
        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      LABEL_OFFSET_MSEC,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      TITLE_OBSERVATORY_CLOCK_OFFSET);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      SchemaDataType.DECIMAL_INTEGER.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      SchemaUnits.MSEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      TITLE_OBSERVATORY_CLOCK_OFFSET,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      CHANNEL_COLOUR,
                                      REGEX_COLOUR,
                                      DataTypeDictionary.COLOUR_DATA,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        // Chart Metadata
        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      title,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      LABEL_ELAPSED_TIME,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      LABEL_OFFSET_MSEC,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      KEY_CHANNEL_DELTA_T,
                                      "0",
                                      DataTypeDictionary.DECIMAL_INTEGER.getRegex(),
                                      DataTypeDictionary.DECIMAL_INTEGER,
                                      SchemaUnits.MSEC,
                                      DESCRIPTION_OFFSET);

        MetadataHelper.showMetadataList(listMetadata,
                                        "NtpClient Channel Metadata",
                                        debug);

        return (listMetadata);
        }


    /***********************************************************************************************
     * Set the latest value of the time offset in the Instrument Metadata.
     * These are currently added to the DAO ObservationMetadata.
     *
     * @param metadata
     * @param offset
     */

    public static void setNtpOffsetMetadataValue(final List<Metadata> metadata,
                                                 final long offset)
        {
        if (metadata != null)
            {
            final Metadata dataDeltaT;

            dataDeltaT = MetadataHelper.getMetadataByKey(metadata, KEY_CHANNEL_DELTA_T);

            if (dataDeltaT != null)
                {
                dataDeltaT.setValue(Long.toString(offset));
                }
            }
        }


    /***********************************************************************************************
     * Get the Log Metadata, i.e describing the columns of the Log.
     * NtpInstrumentLogUIComponent has 15 columns.
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createNtpClientInstrumentLogMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(LOG_COLUMN_COUNT << 2);

        //------------------------------------------------------------------------------------------
        // Names

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      "Icon",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      "Set",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Set column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      "Date",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      "Time",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      "Time Server",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Time Server column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      "Server Address",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Server Address column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      "Version",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Version column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      "Offset msec",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Offset column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      "Delay msec",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Delay column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      "Stratum",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Stratum column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      "Precision msec",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Precision column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      "Status",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Status column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + "12",
                                      "DateStamp",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the DateStamp column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_13,
                                      "Time",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + "14",
                                      "Seconds",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Seconds column");

        //------------------------------------------------------------------------------------------
        // DataTypes
        // See: NtpDAO.getNtpDatagram()

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      DataTypeDictionary.IMAGE_DATA.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      DataTypeDictionary.BOOLEAN.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Set column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      DataTypeDictionary.DATE_YYYY_MM_DD.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      DataTypeDictionary.TIME_HH_MM_SS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Time Server column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Server Address column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      DataTypeDictionary.DECIMAL_INTEGER.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Version column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Offset column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Delay column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      DataTypeDictionary.DECIMAL_INTEGER.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Stratum column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Precision column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Status column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_12,
                                      DataTypeDictionary.DATE_YYYY_MM_DD.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the DateStamp column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_13,
                                      DataTypeDictionary.TIME_HH_MM_SS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_14,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Seconds column");

        //------------------------------------------------------------------------------------------
        // Units

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Set column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      SchemaUnits.YEAR_MONTH_DAY.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      SchemaUnits.HOUR_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Time Server column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Server Address column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Version column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      SchemaUnits.MSEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Offset column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      SchemaUnits.MSEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Delay column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Stratum column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      SchemaUnits.MSEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Precision column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Status column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + "12",
                                      SchemaUnits.YEAR_MONTH_DAY.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the DateStamp column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_13,
                                      SchemaUnits.HOUR_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + "14",
                                      SchemaUnits.SECONDS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Seconds column");

        //------------------------------------------------------------------------------------------
        // Descriptions

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      "An icon showing the event status",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      "A checkbox showing if the system time has been set following receipt of this NTP Datagram",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Set column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      "The Date calculated from the offset in the NTP Datagram",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      "The Time calculated from the offset in the NTP Datagram",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      "The Time Server providing the NTP Datagram",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Time Server column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      "The Address of the Time Server",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Server Address column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      "Version number of the NTP Datagram packet",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Version column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      "Offset of the local clock versus the server clock, taking into account the roundtrip delay (in milliseconds). Calculated according to rfc2030.",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Offset column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      "Total roundtrip delay from the server to the primary server (in milliseconds)",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Delay column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      "The stratum, indicating the distance (in hops) from the server to the primary server (which is stratum 1)",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Stratum column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      "Precision of the server clock (in milliseconds)",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Precision column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      "The status of this event",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Status column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_12,
                                      "Indicates the Date when the server clock was last set",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the DateStamp column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_13,
                                      "Indicates the Time when the server clock was last set",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_14,
                                      "Indicates the Seconds when the server clock was last set",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Seconds column");

        return (listMetadata);
        }
    }
