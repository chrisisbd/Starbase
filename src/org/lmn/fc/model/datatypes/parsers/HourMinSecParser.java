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

package org.lmn.fc.model.datatypes.parsers;


import org.lmn.fc.common.exceptions.HourMinSecException;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeParserInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.RootDataTypeInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.List;
import java.util.regex.Pattern;


/***************************************************************************************************
 * HourMinSecParser.
 */

public class HourMinSecParser implements DataTypeParserInterface
    {
    // String Resources
    private static final String DEFAULT_DELIMITER = ":";

    // The Parser is a Singleton!
    private volatile static DataTypeParserInterface PARSER_INSTANCE;


    /***********************************************************************************************
     * The Parser is a Singleton!
     *
     * @return DataTypeParserInterface
     */

    public static DataTypeParserInterface getInstance()
        {
        if (PARSER_INSTANCE == null)
            {
            synchronized (HourMinSecParser.class)
                {
                if (PARSER_INSTANCE == null)
                    {
                    PARSER_INSTANCE = new HourMinSecParser();
                    }
                }
            }

        return (PARSER_INSTANCE);
        }


    /**********************************************************************************************
     * Parse a string into an HMS.
     *
     * @param hms
     * @param delimiter
     *
     * @return HourMinSecInterface
     *
     * @throws HourMinSecException
     */

    private static HourMinSecInterface parseHMS(final String hms,
                                                final String delimiter) throws HourMinSecException
        {
        final String SOURCE = "HourMinSecParser.parseHMS() ";
        String strHours;
        final int intHours;
        final String strMinutes;
        final int intMinutes;
        final String strSeconds;
        final double dblSeconds;
        final boolean boolSign;
        final int intIndex0;
        final int intIndex1;
        final int intIndex2;

       // ToDo This parser might be a lot easier with Regex!

        if (hms == null)
            {
            // Nothing to parse!
            throw new HourMinSecException(SOURCE + "Null value, unable to parse");
            }
        else
            {
            try
                {
                // Find the first occurrence of the delimiter,
                // to chop off what should be the Hours part
                intIndex0 = hms.indexOf(delimiter);

                if (intIndex0 == -1)
                    {
                    // No delimiter found, so the format is invalid
                    throw new HourMinSecException(SOURCE + "Invalid format - no first delimiter");
                    }
                else
                    {
                    // Chop off the hours part
                    strHours = hms.substring(0, intIndex0);

                    // Remove any leading '+' because of the dumb parser...
                    if (strHours.startsWith("+"))
                        {
                        strHours = strHours.substring(1);
                        }

                    // Parse the Hours, this may produce NumberFormatException
                    // Allow any number of positive or negative Hours
                    intHours = Math.abs(Integer.parseInt(strHours));

                    // Trap the case of zero hours, but non-zero minutes
                    boolSign = !strHours.startsWith("-");

                    // Now look for the second delimiter, to get Minutes
                    intIndex1 = hms.indexOf(delimiter, intIndex0 + 1);

                    if (intIndex1 == -1)
                        {
                        // There is no second delimiter, so the format is invalid
                        throw new HourMinSecException(SOURCE + "Invalid format - no second delimiter");
                        }
                    else
                        {
                        // Chop off the Minutes part
                        strMinutes = hms.substring(intIndex0 + 1, intIndex1);

                        // Parse the Minutes, this may produce NumberFormatException
                        intMinutes = Integer.parseInt(strMinutes);

                        // Range check the Minutes
                        if ((intMinutes < 0) || (intMinutes > 59))
                            {
                            throw new HourMinSecException(SOURCE + "Minutes out of range [minutes=" + intMinutes + "]");
                            }

                        // If we find another delimiter, it is invalid
                        intIndex2 = hms.indexOf(delimiter, intIndex1 + 1);

                        if (intIndex2 != -1)
                            {
                            // Too many delimiters, so the format is invalid
                            throw new HourMinSecException(SOURCE + "Invalid format - too many delimiters");
                            }
                        else
                            {
                            // What is left must be the seconds
                            strSeconds = hms.substring(intIndex1 + 1);

                            // Parse the Seconds, this may produce NumberFormatException
                            dblSeconds = Double.parseDouble(strSeconds);

                            // Range check the Seconds
                            if ((dblSeconds < 0.0) || (dblSeconds >= 60.0))
                                {
                                throw new HourMinSecException(SOURCE + "Seconds out of range [seconds=" + dblSeconds + "]");
                                }

                            return (new HourMinSecDataType(boolSign, intHours, intMinutes, dblSeconds));
                            }
                        }
                    }
                }

            catch (NumberFormatException exception)
                {
                throw new HourMinSecException(SOURCE + "Invalid Number Format [exception=" + exception.getMessage() + "]", exception);
                }
            }
        }


    /***********************************************************************************************
     * Privately construct the HourMinSecParser.
     */

    private HourMinSecParser()
        {
        super();
        }


    /**********************************************************************************************/
    /* DataTypeParserInterface implementations                                                    */
    /***********************************************************************************************
     * Attempt to parse the Starscript ParameterType Value into the specified DataType.
     * Uses the Name, Regex and Value in the Parameter.
     * Called only from the static helper.
     *
     * @param parameter
     * @param datatype
     * @param errors
     *
     * @return boolean
     */

    public boolean validateParameterValueAsDataType(final ParameterType parameter,
                                                    final DataTypeDictionary datatype,
                                                    final List<String> errors)
        {
        int intFailures;

        intFailures = 0;

        if ((parameter.getRegex() != null)
            && (!EMPTY_STRING.equals(parameter.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(parameter.getRegex(), parameter.getValue()))
                {
                try
                    {
                    // We are not interested in the return value, only any errors
                    parseHMS(parameter.getValue(), DEFAULT_DELIMITER);
                    }

                catch (HourMinSecException exception)
                    {
                    errors.add("[name=" + parameter.getName() + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_HOUR_MIN_SEC);
                errors.add("[name=" + parameter.getName() + "]" + MSG_REGEX + parameter.getRegex());
                intFailures++;
                }
            }
        else
            {
            // ToDo Consider using Regex from the DataType
            // Do the best we can with no Regex
            try
                {
                // We are not interested in the return value, only any errors
                parseHMS(parameter.getValue(), DEFAULT_DELIMITER);
                }

            catch (HourMinSecException exception)
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_HOUR_MIN_SEC);
                intFailures++;
                }
            }

        return (intFailures == 0);
        }


    /***********************************************************************************************
     * Attempt to validate the ValueField as being of the specified DataType.
     * Called only from the static helpers.
     *
     * @param valuefield
     * @param datatype
     * @param errors
     *
     * @return boolean
     */

    public boolean validateValueFieldAsDataType(final String valuefield,
                                                final DataTypeDictionary datatype,
                                                final List<String> errors)
        {
        int intFailures;

        intFailures = 0;

        try
            {
            // We are not interested in the return value, only any errors
            parseHMS(valuefield, DEFAULT_DELIMITER);
            }

        catch (HourMinSecException exception)
            {
            errors.add(exception.getMessage());
            intFailures++;
            }

        return (intFailures == 0);
        }


    /***********************************************************************************************
     * Attempt to parse the ValueField into the specified DataType.
     * Used for e.g. return of primitive types.
     * Return NULL if the parsing failed.
     * Called only from the static helper.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return RootDataTypeInterface
     */

    public RootDataTypeInterface parseValueFieldToDataType(final String valuefield,
                                                           final DataTypeDictionary datatype,
                                                           final String name,
                                                           final String regex,
                                                           final List<String> errors)
        {
        RootDataTypeInterface dataTypeInterface;

        dataTypeInterface = null;

        try
            {
            dataTypeInterface = parseHMS(valuefield, DEFAULT_DELIMITER);
            }

        catch (HourMinSecException exception)
            {
            errors.add(exception.getMessage());
            }

        return (dataTypeInterface);
        }


    /***********************************************************************************************
     * Attempt to parse the ValueField into a Number.
     * Used for e.g. return of primitive types.
     * Return NULL if the parsing failed, or if the DataType is not a numeric.
     * Called only from the static helper.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return Number
     */

    public Number parseValueFieldToNumber(final String valuefield,
                                          final DataTypeDictionary datatype,
                                          final String name,
                                          final String regex,
                                          final List<String> errors)
        {
       // Not required for this DataType

        if (errors != null)
            {
            errors.add(ERROR_NO_PARSER);

            if (!datatype.isNumeric())
                {
                errors.add(ERROR_NON_NUMERIC);
                }
            }

        return (null);
        }


    /***********************************************************************************************
     * Parse the byte array into a valid ResponseValue, returned in the ResponseType.
     * Uses the Name and Regex in the response.
     * Return a Response Value of NO_DATA if the parsing failed.
     * Called only from the static helper.
     *
     * @param bytes
     * @param datatype
     * @param responsetype
     * @param errors
     *
     * @return boolean
     */

    public boolean parseBytesToResponseValue(final byte[] bytes,
                                             final DataTypeDictionary datatype,
                                             final ResponseType responsetype,
                                             final List<String> errors)
        {
        int intFailures;
        final String valuefield;

        intFailures = 0;
        valuefield = new String(bytes);

        if ((responsetype.getRegex() != null)
            && (!EMPTY_STRING.equals(responsetype.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(responsetype.getRegex(), valuefield))
                {
                try
                    {
                    // We are not interested in the return value, only any errors
                    parseHMS(valuefield, DEFAULT_DELIMITER);
                    }

                catch (HourMinSecException exception)
                    {
                    errors.add("[name=" + responsetype.getName() + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_HOUR_MIN_SEC);
                errors.add("[name=" + responsetype.getName() + "]" + MSG_REGEX + responsetype.getRegex());
                intFailures++;
                }
            }
        else
            {
            // ToDo Consider using Regex from the DataType
            // Do the best we can with no Regex
            try
                {
                // We are not interested in the return value, only any errors
                parseHMS(valuefield, DEFAULT_DELIMITER);
                }

            catch (HourMinSecException exception)
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_HOUR_MIN_SEC);
                intFailures++;
                }
            }

        // If all went well, return the ResponseValue
        if (intFailures == 0)
            {
            responsetype.setValue(valuefield);
            }
        else
            {
            responsetype.setValue(NO_DATA);
            }

        return (intFailures == 0);
        }
    }