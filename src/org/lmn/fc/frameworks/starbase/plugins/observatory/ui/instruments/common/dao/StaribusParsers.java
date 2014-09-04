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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;

import java.text.ParseException;
import java.util.*;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * StaribusParsers.
 */

public final class StaribusParsers implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              FrameworkRegex,
                                              ResourceKeys,
                                              ObservatoryConstants
    {
    private static final String MSG_CHANNEL_SPEC = "Channel specification Metadata includes an invalid DataType";
    private static final String MSG_BLOCK_CALC = "DataRowsPerBlock calculation failed";
    private static final String MSG_INVALID_RATE = "Invalid Sample Rate replaced by 1sec";
    private static final String MSG_NON_INTEGRAL = "Non-integral number of rows in data block";
    private static final String MSG_INCORRECT_SPEC = "Incorrect definition of a data channel";
    private static final String MSG_PARSING_ERRORS = "Parsing errors encountered";
    private static final String MSG_PARSING_HEADER = "Unable to parse data field in header";

    private static final String DATE_PARSE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final int LENGTH_STARIBUS_BLOCK = 512;
    public static final int LENGTH_STARIBUS_HEADER_FIELD = 32;
    public static final int LENGTH_STARIBUS_TIMESTAMP = 19;

    private static final int LENGTH_TEMPERATURE_FIELD = 4;
    private static final int LENGTH_SAMPLE_RATE_FIELD = 4;
    private static final int OFFSET_TO_TEMPERATURE = 20;
    private static final int OFFSET_TO_SAMPLE_RATE = 25;


    /***********************************************************************************************
     * Parse the concatenated, timestamped data blocks into the Starbase internal Vector format.
     * The List of DataTypePlugin shows which parser to use for each channel, including Temperature.
     * Parse the Temperature channel if the temperatureflag List contains KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.
     * The ChannelCount must include the Temperature channel, if present.
     * Used in StaribusCoreDAO.getData().
     *
     * @param data
     * @param listdatatypes
     * @param channelcount
     * @param temperatureflag
     * @param errors
     *
     * @return Vector<Object>
     */

    public static Vector<Object> parseStaribusBlocksIntoVector(final StringBuffer data,
                                                               final List<DataTypeDictionary> listdatatypes,
                                                               final int channelcount,
                                                               final List<String> temperatureflag,
                                                               final List<String> errors)
        {
        final String SOURCE = "StaribusParsers.parseStaribusBlocksIntoVector() ";
        final Vector<Object> vecRawData;

        vecRawData = new Vector<Object>(10000);

        try
            {
            final int intTemperatureChannelCount;
            final int intDataChannelCount;

            final TimeZone timeZone;
            final Locale locale;

            long longRowFieldWidth;
            final int intDataRowsPerBlock;
            boolean boolValidDataTypes;
            Vector vecRow;

            // See if we have to deal with the optional slow-speed Temperature channel
            if ((temperatureflag != null)
                && (temperatureflag.contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey())))
                {
                intTemperatureChannelCount = 1;
                intDataChannelCount = channelcount - 1;
                //System.out.println("TEMPERATURE FOUND!");
                }
            else
                {
                intTemperatureChannelCount = 0;
                intDataChannelCount = channelcount;
                //System.out.println("NO TEMP FOUND...");
                }

            // Make the assumption that the Controller clock is synchronised to the Observatory clock
            // and that Time is in UT. Do this only once...
            timeZone = TimeZone.getTimeZone(DEFAULT_TIME_ZONE_ID);
            locale = new Locale(FrameworkSingletons.REGISTRY.getFramework().getLanguageISOCode(),
                                FrameworkSingletons.REGISTRY.getFramework().getCountryISOCode());

            // The incoming buffer has multiple blocks of 512 bytes,
            // where each block has a timestamped header of the form:
            // 2008-08-23 12:34:56 followed by Temperature +999 and SampleRate 9999, in a total of 32 characters
            // Then follow (512-32) characters assigned to fixed width fields for data values for each channel in sequence

            // Calculate the space occupied by each row of channels (excluding timestamp header)
            // and check that only DataTypes with a specified FieldWidth have been used
            // otherwise we don't know how to parse the data
            // Each row of *data* channels (not Temperature) will occupy:
            // (DataType.FieldWidth channel 0), ... (DataType.FieldWidth channel n-1)

            longRowFieldWidth = 0;
            boolValidDataTypes = true;

            // Skip over the Temperature channel if present,
            // since we don't need to know its width just yet
            for (int channel = intTemperatureChannelCount;
                channel < channelcount;
                channel++)
                {
                final DataTypeDictionary dataType;
                final long longChannelFieldWidth;

                dataType = listdatatypes.get(channel);
                longChannelFieldWidth = dataType.getFieldWidth();

                // 0 means unlimited width, so we can't parse it
                if (longChannelFieldWidth == 0)
                    {
                    boolValidDataTypes = false;
                    errors.add(SOURCE + MSG_CHANNEL_SPEC);
                    }
                else
                    {
                    // Accumulate the valid field widths
                    longRowFieldWidth += longChannelFieldWidth;
                    }
                }

            //System.out.println("ROW FIELD WIDTH=" + longRowFieldWidth);

            // Are all the channels defined correcty?
            if ((boolValidDataTypes)
                && (intDataChannelCount > 0)
                && (longRowFieldWidth > 0))
                {
                final double dblDataSpaceAvailable;

                // Calculate the number of rows of channels in the data block with:
                // (LENGTH_STARIBUS_BLOCK - LENGTH_STARIBUS_HEADER_FIELD) / longRowFieldWidth
                // which should leave no remainder
                dblDataSpaceAvailable = ((double) LENGTH_STARIBUS_BLOCK - (double) LENGTH_STARIBUS_HEADER_FIELD);

                if (Math.IEEEremainder(dblDataSpaceAvailable,
                                       longRowFieldWidth) == 0.0)
                    {
                    // Returns the largest (closest to positive infinity) double value
                    // that is less than or equal to the argument and is equal to a mathematical integer
                    intDataRowsPerBlock = (int)Math.floor(dblDataSpaceAvailable / (double) longRowFieldWidth);
                    }
                else
                    {
                    intDataRowsPerBlock = 0;
                    errors.add(SOURCE + MSG_BLOCK_CALC);
                    }

                //System.out.println("ROW WIDTH=" + longRowFieldWidth + " NUMBER OF ROWS PER BLOCK=" + intDataRowsPerBlock);

                // Did we get an integral number of data rows?
                if (intDataRowsPerBlock > 0)
                    {
                    // Process all Data blocks
                    // The number of blocks in the data should be (datalength / LENGTH_STARIBUS_BLOCK)
                    // This will have been checked by the block assembler

                    for (int blockid = 0;
                        blockid < (data.length() / LENGTH_STARIBUS_BLOCK);
                        blockid++)
                        {
                        final int intBlockStart;
                        final double dblBlockTemperature;
                        int intBlockSampleRateSecs;
                        final Calendar calendarBlock;
                        int intFieldStart;

                        // Index to the start of this block
                        intBlockStart = (blockid * LENGTH_STARIBUS_BLOCK);

                        //System.out.println("PROCESS BLOCK blockid=" + blockid + " BLOCK START=" + intBlockStart);

                        // Get the slow-speed Temperature channel for each block
                        if (intTemperatureChannelCount > 0)
                            {
                            // We know that the first DataType is that of the Temperature channel
                            dblBlockTemperature = parseStaribusFieldAsDouble(data,
                                                                             intBlockStart + OFFSET_TO_TEMPERATURE,
                                                                             LENGTH_TEMPERATURE_FIELD,
                                                                             listdatatypes.get(0),
                                                                             errors);
                            //System.out.println("parsed temp=" + dblBlockTemperature);
                            }
                        else
                            {
                            // This value will never be used
                            dblBlockTemperature = 0.0;
                            }

                        // Re-read the sample rate for the block
                        // Assume that it is encoded as a DecimalInteger
                        intBlockSampleRateSecs = parseStaribusFieldAsInteger(data,
                                                                             intBlockStart + OFFSET_TO_SAMPLE_RATE,
                                                                             LENGTH_SAMPLE_RATE_FIELD,
                                                                             DataTypeDictionary.DECIMAL_INTEGER,
                                                                             errors);
                        // Protect the unwary...
                        if (intBlockSampleRateSecs <= 0)
                            {
                            intBlockSampleRateSecs = 1;
                            errors.add(SOURCE + MSG_INVALID_RATE);
                            }

                        // Create a Calendar for the first sample of each block
                        // by parsing the timestamp in the header
                        calendarBlock = ChronosHelper.parseCalendar(timeZone,
                                                                    locale,
                                                                    DATE_PARSE_FORMAT,
                                                                    data.substring(intBlockStart, (intBlockStart + LENGTH_STARIBUS_TIMESTAMP)));
                        //System.out.println("TIMESTAMP={" + data.substring(intBlockStart, (intBlockStart + LENGTH_STARIBUS_TIMESTAMP)) + "}");

                        // Initialise the FieldStart to point to the first data row in this block
                        intFieldStart = intBlockStart + LENGTH_STARIBUS_HEADER_FIELD;

                        // Process each row in each data block
                        for (int datarow = 0;
                            datarow < intDataRowsPerBlock;
                            datarow++)
                            {
                            final Calendar calendarRow;

                            // Get the calendar for this row
                            calendarRow = (Calendar)calendarBlock.clone();

                            // Move the time index to the next row in the block
                            calendarBlock.add(Calendar.SECOND, intBlockSampleRateSecs);

                            //----------------------------------------------------------------------
                            // Timestamp each row

                            vecRow = new Vector(channelcount + 2);
                            vecRow.add(calendarRow);

                            //----------------------------------------------------------------------
                            // Add the Temperature channel if present

                            if (intTemperatureChannelCount > 0)
                                {
                                // Each row in the block has the same Temperature
                                vecRow.add(dblBlockTemperature);
                                }

                            //----------------------------------------------------------------------
                            // Now parse out each channel of data in each row,
                            // skipping the Temperature channel DataType *parser* if present

                            for (int intChannelIndex = intTemperatureChannelCount;
                                intChannelIndex < channelcount;
                                intChannelIndex++)
                                {
                                final DataTypeDictionary dataTypeOfField;
                                final int intFieldEnd;
                                final String strValueField;
                                final Number number;

                                // Skip the Temperature channel DataType parser if present...
                                dataTypeOfField = listdatatypes.get(intChannelIndex);
                                //System.out.println(SOURCE + " CHANNEL=" + k + "  DATATYPE=" + dataTypeField.getDisplayName());

                                // ...so we can rely on the DataType field width
                                intFieldEnd = intFieldStart + (int)dataTypeOfField.getFieldWidth();

                                //System.out.println("START=" + intFieldStart + " END=" + intFieldEnd);

                                // Index into the row to parse each field
                                strValueField = data.substring(intFieldStart, intFieldEnd);
                                //System.out.println("PARSING FIELD={" + strField + "}");

                                // Prepare for the next loop...
                                intFieldStart = intFieldEnd;
                                //System.out.println("NEW START=" + intFieldStart);

                                number = DataTypeHelper.parseNumberFromValueField(strValueField,
                                                                                  dataTypeOfField,
                                                                                  EMPTY_STRING,
                                                                                  EMPTY_STRING,
                                                                                  errors);
                                // Add the channel data
                                if (number != null)
                                    {
                                    vecRow.add(number);
                                    //System.out.println("START=" + intFieldStart + " END=" + intFieldEnd + " PARSING FIELD={" + strField + "} RESULT={" + objField.toString() + "}");
                                    }
                                else
                                    {
                                    // The parsing failed
                                    vecRow.add(QUERY);
                                    //System.out.println("PARSING FAILED");
                                    }
                                }

                            // Add the finished Row to the output RawData
                            vecRawData.add(vecRow);
                            }
                        }
                    }
                else
                    {
                    errors.add(SOURCE + MSG_NON_INTEGRAL);
                    vecRawData.clear();
                    }
                }
            else
                {
                errors.add(SOURCE + MSG_INCORRECT_SPEC);
                vecRawData.clear();
                }
            }

        catch (ParseException exception)
            {
            errors.add(SOURCE + MSG_PARSING_ERRORS);
            vecRawData.clear();
            }

        return (vecRawData);
        }


    /***********************************************************************************************
     * Parse the value of the data field in the block header.
     * Make the assumption that the result will end up as an Integer,
     * but may come from DecimalFloat or DecimalInteger (only types currently supported).
     * Return 0 if the parsing fails.
     *
     * @param data
     * @param fieldstart
     * @param fieldwidth
     * @param datatype
     * @param errors
     *
     * @return int
     */

    private static int parseStaribusFieldAsInteger(final StringBuffer data,
                                                   final int fieldstart,
                                                   final int fieldwidth,
                                                   final DataTypeDictionary datatype,
                                                   final List<String> errors)
        {
        final String SOURCE = "StaribusParsers.parseStaribusFieldAsInteger() ";
        final int intDataField;

        if ((data != null)
            && (fieldstart >= 0)
            && (datatype != null)
            && (errors != null))
            {
            final String strValueField;
            final Number number;

            strValueField = data.substring(fieldstart,
                                           fieldstart + fieldwidth);
            number = DataTypeHelper.parseNumberFromValueField(strValueField,
                                                              datatype,
                                                              EMPTY_STRING,
                                                              EMPTY_STRING,
                                                              errors);
            if (number != null)
                {
                intDataField = number.intValue();
                }
            else
                {
                errors.add(SOURCE + MSG_PARSING_HEADER);
                intDataField = 0;
                }
            }
        else
            {
            if (errors != null)
                {
                errors.add(SOURCE + EXCEPTION_PARAMETER_INVALID);
                }
            intDataField = 0;
            }

        return (intDataField);
        }


    /***********************************************************************************************
     * Parse the value of the data field in the block header.
     * Make the assumption that the result will end up as a Double,
     * but may come from DecimalFloat or DecimalInteger (only types currently supported).
     * Return 0 if the parsing fails.
     *
     * @param data
     * @param fieldstart
     * @param fieldwidth
     * @param datatype
     * @param errors
     *
     * @return double
     */

    private static double parseStaribusFieldAsDouble(final StringBuffer data,
                                                     final int fieldstart,
                                                     final int fieldwidth,
                                                     final DataTypeDictionary datatype,
                                                     final List<String> errors)
        {
        final String SOURCE = "StaribusParsers.parseStaribusFieldAsDouble() ";
        final double dblDataField;

        if ((data != null)
            && (fieldstart >= 0)
            && (datatype != null)
            && (errors != null))
            {
            final String strValueField;
            final Number number;

            strValueField = data.substring(fieldstart,
                                           fieldstart + fieldwidth);
            number = DataTypeHelper.parseNumberFromValueField(strValueField,
                                                              datatype,
                                                              EMPTY_STRING,
                                                              EMPTY_STRING,
                                                              errors);
            if (number != null)
                {
                dblDataField = number.doubleValue();
                }
            else
                {
                errors.add(SOURCE + MSG_PARSING_HEADER);
                dblDataField = 0.0;
                }
            }
        else
            {
            if (errors != null)
                {
                errors.add(SOURCE + EXCEPTION_PARAMETER_INVALID);
                }

            dblDataField = 0.0;
            }

        return (dblDataField);
        }


    /***********************************************************************************************
     * Parse a String into StaribusMultichannelData,
     * as a List of Objects suitable for adding to the DAO RawData Vector.
     * The List of DataTypeDictionary shows which parser to use for each channel.
     *
     * @param data
     * @param separatorregex
     * @param listdatatypes
     * @param channelcount
     * @param errors
     *
     * @return List<Object>
     */

    public static List<Object> parseStaribusMultichannelDataIntoList(final StringBuffer data,
                                                                     final String separatorregex,
                                                                     final List<DataTypeDictionary> listdatatypes,
                                                                     final int channelcount,
                                                                     final List<String> errors)
        {
        final String SOURCE = "StaribusParsers.parseStaribusMultichannelDataIntoList() ";
        final List<Object> listObjects;
        String[] arrayData;

        listObjects = new ArrayList<Object>(100);

        //System.out.println("SEPARATOR = [" + separatorregex + "]");

        if ((data != null)
            && (data.length() != 0)
            && (separatorregex != null)
            && (!EMPTY_STRING.equals(separatorregex))
            && (listdatatypes != null))
            {
            try
                {
                arrayData = data.toString().split(separatorregex);

                if ((arrayData != null)
                    && (arrayData.length == listdatatypes.size())
                    && (arrayData.length == channelcount))
                    {
                    for (int intChannelIndex = 0;
                         intChannelIndex < arrayData.length;
                         intChannelIndex++)
                        {
                        final DataTypeDictionary dataTypeOfField;
                        final String strValueField;
                        final Number number;

                        dataTypeOfField = listdatatypes.get(intChannelIndex);
                        strValueField = arrayData[intChannelIndex];

                        // StaribusMultichannelData currently only supports DataTypes which map to Double, Float and Integer
                        number = DataTypeHelper.parseNumberFromValueField(strValueField,
                                                                          dataTypeOfField,
                                                                          EMPTY_STRING,
                                                                          EMPTY_STRING,
                                                                          errors);
                        if (number != null)
                            {
                            listObjects.add(number);
                            //System.out.println("Data[" + i + "] = " + strData + " DataType=" + dataTypeOfField.getDisplayName());
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "DataTypeHelper.parseNumberFromValueField returned NULL");
                            }
                        }
                    }
                else
                    {
                    errors.add(SOURCE + "Data does not contain the correct number of channels");
                    }
                }

            catch (PatternSyntaxException exception)
                {
                errors.add(SOURCE + "The Channel separator Regex is invalid");
                }
            }
        else
            {
            errors.add(SOURCE + SPACE + EXCEPTION_PARAMETER_NULL);
            }

        // Help the GC?
        arrayData = null;

        return (listObjects);
        }


    /***********************************************************************************************
     * Create some stub data for testing the StaribusBlock parser.
     *
     * @param listdatatypes
     * @param channelcount
     *
     * @return StringBuffer
     */

    public static StringBuffer createStubStaribusBlockData(final List<DataTypeDictionary> listdatatypes,
                                                           final int channelcount)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Assume samples once per second

        // REMEMBER! Change the timestamps to agree with the test metadata channel specification

        // For one data channel
        // 15 rows of 8 samples = 120 rows per block
        // 120 rows @ 1 sec sample rate = 2 minutes per block

        // For two data channels
        // 15 rows of 4 samples per channel = 60 rows per block
        // 60 rows @ 1 sec sample rate = 1 minute per block

        // DataType is DecimalInteger, i.e. four characters per number
        buffer.append("2008-08-23 12:00:00 +023 0001   ");
        buffer.append("00000001000200030004000500060007");
        buffer.append("00080009001000110012001300140015");
        buffer.append("00160017001800190020002100220023");
        buffer.append("00240025002600270028002900300031");
        buffer.append("00000001000200030004000500060007");
        buffer.append("00080009001000110012001300140015");
        buffer.append("00160017001800190020002100220023");
        buffer.append("00240025002600270028002900300031");
        buffer.append("00000001000200030004000500060007");
        buffer.append("00080009001000110012001300140015");
        buffer.append("00160017001800190020002100220023");
        buffer.append("00240025002600270028002900300031");
        buffer.append("00000001000200030004000500060007");
        buffer.append("00080009001000110012001300140015");
        buffer.append("00160017001800190020002100220023");

        buffer.append("2008-08-23 12:01:00 +022 0001   ");
        buffer.append("00320033003400350036003700380039");
        buffer.append("00400041004200430044004500460047");
        buffer.append("00480049005000510052005300540055");
        buffer.append("00560057005800590060006100620063");
        buffer.append("00320033003400350036003700380039");
        buffer.append("00400041004200430044004500460047");
        buffer.append("00480049005000510052005300540055");
        buffer.append("00560057005800590060006100620063");
        buffer.append("00320033003400350036003700380039");
        buffer.append("00400041004200430044004500460047");
        buffer.append("00480049005000510052005300540055");
        buffer.append("00560057005800590060006100620063");
        buffer.append("00320033003400350036003700380039");
        buffer.append("00400041004200430044004500460047");
        buffer.append("00480049005000510052005300540055");

        buffer.append("2008-08-23 12:02:00 +021 0001   ");
        buffer.append("00630062006100600059005800570056");
        buffer.append("00550054005300520051005000490048");
        buffer.append("00470046004500440043004200410040");
        buffer.append("00390038003700360035003400330032");
        buffer.append("00630062006100600059005800570056");
        buffer.append("00550054005300520051005000490048");
        buffer.append("00470046004500440043004200410040");
        buffer.append("00390038003700360035003400330032");
        buffer.append("00630062006100600059005800570056");
        buffer.append("00550054005300520051005000490048");
        buffer.append("00470046004500440043004200410040");
        buffer.append("00390038003700360035003400330032");
        buffer.append("00630062006100600059005800570056");
        buffer.append("00550054005300520051005000490048");
        buffer.append("00470046004500440043004200410040");

        buffer.append("2008-08-23 12:03:00 +020 0001   ");
        buffer.append("00310030002900280027002600250024");
        buffer.append("00230022002100200019001800170016");
        buffer.append("00150014001300120011001000090008");
        buffer.append("00070006000500040003000200010000");
        buffer.append("00310030002900280027002600250024");
        buffer.append("00230022002100200019001800170016");
        buffer.append("00150014001300120011001000090008");
        buffer.append("00070006000500040003000200010000");
        buffer.append("00310030002900280027002600250024");
        buffer.append("00230022002100200019001800170016");
        buffer.append("00150014001300120011001000090008");
        buffer.append("00070006000500040003000200010000");
        buffer.append("00310030002900280027002600250024");
        buffer.append("00230022002100200019001800170016");
        buffer.append("00150014001300120011001000090008");

        return (buffer);
        }
    }
