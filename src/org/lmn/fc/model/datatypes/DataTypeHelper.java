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

package org.lmn.fc.model.datatypes;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * DataTypeHelper.
 */

public final class DataTypeHelper implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkSingletons,
                                             FrameworkMetadata,
                                             ResourceKeys
    {
    /***********************************************************************************************
     * Attempt to parse the Starscript ParameterType Value into the specified DataType.
     * Uses the Name, Regex and Value in the Parameter.
     * If errors occur, return a count of the number of errors found.
     * Used only in CommandProcessorContext.
     *
     * @param parametertype
     * @param datatype
     * @param errors
     *
     * @return int
     */

    public static int validateDataTypeOfParameterValue(final ParameterType parametertype,
                                                       final DataTypeDictionary datatype,
                                                       final List<String> errors)
        {
        final String SOURCE = "DataTypeHelper.validateDataTypeOfParameterValue() ";
        final int intInitialErrorCount;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[value=" + parametertype.getValue() + "]");

        intInitialErrorCount = errors.size();

        if (datatype.getParser() != null)
            {
            if (!datatype.getParser().validateParameterValueAsDataType(parametertype, datatype, errors))
                {
                errors.add("DataTypeParser: Failed to parse"
                                + "  [datatype=" + datatype.getName()
                                + "] [value=" + parametertype.getValue() + "]" );
                }
            }
        else
            {
            errors.add("DataTypeParser: Failed to parse"
                            + "  [datatype=" + datatype.getName()
                            + "] [no parser implementation]" );
            }

        // We only need to know about any *new* errors
        return ((errors.size()-intInitialErrorCount));
        }


    /***********************************************************************************************
     * Check that the MetadataValue is valid for the current DataType.
     * If Regex is supplied, then this takes precedence over any Regex in the DataType definition.
     * If errors occur, return a count of the number of errors found.
     * Used in MetadataHelper, MetadataLeafUIComponent, FontChooser.
     *
     * @param value
     * @param datatype
     * @param regex
     * @param errors
     *
     * @return int
     */

    public static int validateDataTypeOfMetadataValue(final String value,
                                                      final DataTypeDictionary datatype,
                                                      final String regex,
                                                      final List<String> errors)
        {
        final String SOURCE = "DataTypeHelper.validateDataTypeOfMetadataValue() ";
        final int intInitialErrorCount;
        final boolean boolValid;

        intInitialErrorCount = errors.size();

        // Were we given a valid Metadata DataType?
        if ((datatype != null)
            && (datatype.isMetadataType()))
            {
            // First check to see if the Metadata has any Regex,
            // if so use that to Validate the Value
            // This is to ensure e.g. that Lat/Long override simple DegMinSec
            if ((regex != null)
                && (!EMPTY_STRING.equals(regex)))
                {
                try
                    {
                    boolValid = Pattern.matches(regex, value);

                    if (!boolValid)
                        {
                        errors.add(SOURCE + "Value did not match supplied Regex"
                                       + "  [type=" + datatype.getName()
                                       + "] [value=" + value
                                       + "] [regex=" + regex + "]");
                        }
                    }

                catch (PatternSyntaxException exception)
                    {
                    errors.add(SOURCE + "Invalid Regex expression"
                                   + "  [type=" + datatype.getName()
                                   + "] [value=" + value
                                   + "] [regex=" + regex + "]");
                    }
                }

            // Now try to validate the Value against the Regex in the DataTypeDictionary
            // All Metadata DataTypes have Regex except DataType and Units
            // which search the enumerations directly
            else if ((datatype.getRegex() != null)
                && (!EMPTY_STRING.equals(datatype.getRegex())))
                {
                 try
                    {
                    boolValid = Pattern.matches(datatype.getRegex(), value);

                    if (!boolValid)
                        {
                        errors.add(SOURCE + "Value did not match Regex in DataType"
                                       + "  [type=" + datatype.getName()
                                       + "] [value=" + value
                                       + "] [regex=" + regex + "]");
                        }
                    }

                catch (PatternSyntaxException exception)
                    {
                    errors.add(SOURCE + "Invalid Regex expression [regex=" + datatype.getRegex() + "]");
                    }
                }

            // Now try to use the DataType parser directly (which will probably do the same thing as above)
            else if (datatype.getParser() != null)
                {
                // Use what we know about the underlying DataType to do the parsing
                boolValid = datatype.getParser().validateValueFieldAsDataType(value, datatype, errors);

                if (!boolValid)
                    {
                    errors.add(SOURCE + "Value could not be parsed by DataType"
                                   + "  [type=" + datatype.getName()
                                   + "] [value=" + value + "]");
                    }
                }
            else
                {
                // This should never occur!
                errors.add(SOURCE + "The DataType has no associated Parser");
                }
            }
        else
            {
            // This should never occur!
            errors.add(SOURCE + "The DataType is invalid for use in Metadata");
            }

        // We only need to know about any *new* errors
        return ((errors.size()-intInitialErrorCount));
        }


    /***********************************************************************************************
     * Attempt to validate the ValueField as being of the specified DataType.
     * If errors occur, return a count of the number of errors found.
     * Used in CSV & TSV Importers and the FrameworkEditor,
     * where no Regex is available (other than in the DataType itself).
     *
     * @param valuefield
     * @param datatype
     * @param errors
     *
     * @return int
     */

    public static int validateDataTypeOfValueField(final String valuefield,
                                                   final DataTypeDictionary datatype,
                                                   final List<String> errors)
        {
        final String SOURCE = "DataTypeHelper.validateDataTypeOfValueField() ";
        final int intInitialErrorCount;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[value=" + valuefield + "]");

        intInitialErrorCount = errors.size();

        if (datatype.getParser() != null)
            {
            if (!datatype.getParser().validateValueFieldAsDataType(valuefield, datatype, errors))
                {
                errors.add("DataTypeParser: Failed to parse"
                                + "  [datatype=" + datatype.getName()
                                + "] [value=" + valuefield + "]" );
                }
            }
        else
            {
            errors.add("DataTypeParser: Failed to parse"
                            + "  [datatype=" + datatype.getName()
                            + "] [no parser implementation]" );
            }

        // We only need to know about any *new* errors
        return ((errors.size()-intInitialErrorCount));
        }


    /***********************************************************************************************
     * Parse a ValueField into the specified DataType.
     * Return NULL if parsing failed.
     * Used extensively for the multi-radix types HourMinSec and DegMinSec.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return RootDataTypeInterface
     */

    public static RootDataTypeInterface parseDataTypeFromValueField(final String valuefield,
                                                                    final DataTypeDictionary datatype,
                                                                    final String name,
                                                                    final String regex,
                                                                    final List<String> errors)
        {
        final String SOURCE = "DataTypeHelper.parseDataTypeFromValueField() ";
        final RootDataTypeInterface parsedDataType;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[value=" + valuefield + "]");

        if (datatype.getParser() != null)
            {
            parsedDataType = datatype.getParser().parseValueFieldToDataType(valuefield, datatype, name, regex, errors);

            if (parsedDataType == null)
                {
                errors.add("DataTypeParser: Failed to parse"
                               + "  [datatype=" + datatype.getName()
                               + "] [value=" + valuefield
                               + "] [name=" + name + "]");
                }
            }
        else
            {
            parsedDataType = null;
            errors.add("DataTypeParser: Failed to parse"
                            + "  [datatype=" + datatype.getName()
                            + "] [no parser implementation]" );
            }

        return (parsedDataType);
        }


    /***********************************************************************************************
     * Parse a ValueField into a Number.
     * Return NULL if parsing failed.
     * Currently used in DAOCommandHelper doIteratedDataCaptureCommand() and executeSteppedCommands()
     * and StaribusParsers, which support Double and Integer.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return Number
     */

    public static Number parseNumberFromValueField(final String valuefield,
                                                   final DataTypeDictionary datatype,
                                                   final String name,
                                                   final String regex,
                                                   final List<String> errors)
        {
        final String SOURCE = "DataTypeHelper.parseNumberFromValueField() ";
        final Number parsedNumber;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[value=" + valuefield + "]");

        if (datatype.getParser() != null)
            {
            parsedNumber = datatype.getParser().parseValueFieldToNumber(valuefield, datatype, name, regex, errors);

            if (parsedNumber == null)
                {
                errors.add("DataTypeParser: Failed to parse"
                                + "  [datatype=" + datatype.getName()
                                + "] [value=" + valuefield
                                + "] [name=" + name + "]");
                }
            }
        else
            {
            parsedNumber = null;
            errors.add("DataTypeParser: Failed to parse"
                            + "  [datatype=" + datatype.getName()
                            + "] [no parser implementation]" );
            }

        return (parsedNumber);
        }


    /***********************************************************************************************
     * Parse the byte array into the specified DataType, returned in the ResponseType Value.
     * Uses the Name and Regex in the Response.
     * If errors occur, return a count of the number of errors found.
     * Used only to parse ResponseMessages.
     *
     * @param bytes
     * @param datatype
     * @param responsetype
     * @param errors
     *
     * @return int
     */

    public synchronized static int parseResponseValueFromByteArray(final byte[] bytes,
                                                                   final DataTypeDictionary datatype,
                                                                   final ResponseType responsetype,
                                                                   final List<String> errors)
        {
        final String SOURCE = "DataTypeHelper.parseResponseValueFromByteArray() ";
        final int intInitialErrorCount;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[bytes=" + Utilities.byteArrayToSpacedHex(bytes).trim() + "]");

        intInitialErrorCount = errors.size();

        if (datatype.getParser() != null)
            {
            if (!datatype.getParser().parseBytesToResponseValue(bytes, datatype, responsetype, errors))
                {
                errors.add("DataTypeParser: Failed to parse"
                                + "  [datatype=" + datatype.getName()
                                + "] [bytes=" + Utilities.byteArrayToSpacedHex(bytes).trim() + "]" );
                }
            }
        else
            {
            errors.add("DataTypeParser: Failed to parse"
                            + "  [datatype=" + datatype.getName()
                            + "] [no parser implementation]");
            }

        // We only need to know about any *new* errors
        return ((errors.size()-intInitialErrorCount));
        }


    /***********************************************************************************************
     * Normalisation utility.
     * (this ASSUMES a symmetrical range either side of zero!).
     *
     * @param value
     * @param range_min
     * @param range_max
     *
     * @return double
     */

    public static double normalise(final double value,
                                   final double range_min,
                                   final double range_max)
        {
        double dblValue;

        dblValue = value;

        while(dblValue < -range_min)
            {
            dblValue += range_max;
            }

        while(dblValue >= range_max)
            {
            dblValue -= range_max;
            }

        return (dblValue);
        }
    }
