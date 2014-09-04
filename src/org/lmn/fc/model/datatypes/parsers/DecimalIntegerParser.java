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

import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeParserInterface;
import org.lmn.fc.model.datatypes.RootDataTypeInterface;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.List;
import java.util.regex.Pattern;


/***************************************************************************************************
 * DecimalIntegerParser.
 */

public class DecimalIntegerParser implements DataTypeParserInterface
    {
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
            synchronized (DecimalIntegerParser.class)
                {
                if (PARSER_INSTANCE == null)
                    {
                    PARSER_INSTANCE = new DecimalIntegerParser();
                    }
                }
            }

        return (PARSER_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the DecimalIntegerParser.
     */

    private DecimalIntegerParser()
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
        final String valuefield;

        intFailures = 0;
        valuefield = parameter.getValue();

        if ((parameter.getRegex() != null)
            && (!EMPTY_STRING.equals(parameter.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(parameter.getRegex(), valuefield))
                {
                try
                    {
                    // ToDo set value back to remove leading zeroes...?
                    if ((valuefield.length() > 1)
                        && (valuefield.startsWith(PREFIX_PLUS)))
                        {
                        Integer.parseInt(valuefield.substring(1), datatype.getRadix());
                        }
                    else
                        {
                        Integer.parseInt(valuefield, datatype.getRadix());
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    errors.add("[name=" + parameter.getName() + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_DECIMAL_INTEGER);
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
                if ((valuefield.length() > 1)
                    && (valuefield.startsWith(PREFIX_PLUS)))
                    {
                    Integer.parseInt(valuefield.substring(1), datatype.getRadix());
                    }
                else
                    {
                    Integer.parseInt(valuefield, datatype.getRadix());
                    }
                }

            catch (NumberFormatException exception)
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_DECIMAL_INTEGER);
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

        if ((datatype.getRegex() != null)
            && (!EMPTY_STRING.equals(datatype.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(datatype.getRegex(), valuefield))
                {
                try
                    {
                    // ToDo set value back to remove leading zeroes...?
                    if ((valuefield.length() > 1)
                        && (valuefield.startsWith(PREFIX_PLUS)))
                        {
                        Integer.parseInt(valuefield.substring(1), datatype.getRadix());
                        }
                    else
                        {
                        Integer.parseInt(valuefield, datatype.getRadix());
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    errors.add("[" + valuefield + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[" + valuefield + "]" + INVALID_DECIMAL_INTEGER);
                errors.add("[" + valuefield + "]" + MSG_REGEX + datatype.getRegex());
                intFailures++;
                }
            }
        else
            {
            // Do the best we can
            try
                {
                if ((valuefield.length() > 1)
                    && (valuefield.startsWith(PREFIX_PLUS)))
                    {
                    Integer.parseInt(valuefield.substring(1), datatype.getRadix());
                    }
                else
                    {
                    Integer.parseInt(valuefield, datatype.getRadix());
                    }
                }

            catch (NumberFormatException exception)
                {
                errors.add("[" + valuefield + "]" + INVALID_DECIMAL_INTEGER);
                intFailures++;
                }
            }

        return (intFailures == 0);
        }


    /***********************************************************************************************
     * Attempt to parse the ValueField into the specified DataType.
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
     * @return RootDataTypeInterface
     */

    public RootDataTypeInterface parseValueFieldToDataType(final String valuefield,
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
        final String SOURCE = "DecimalInteger.parseValueFieldToNumber() ";
        Number number;

        number = null;

        if ((valuefield != null)
            && (valuefield.length() > 0)
            && (datatype.isNumeric()))
            {
            // Parses the string argument as a DECIMAL integer.
            // Parses the string argument as a signed integer in the radix specified
            // by the second argument. The characters in the string must all be digits of
            // the specified radix, that the first character may be an ASCII minus sign
            // '-' ('\u002D') to indicate a negative value.
            // Leading zeroes may be present when required

            if ((datatype.getRegex() != null)
                && (!EMPTY_STRING.equals(datatype.getRegex())))
                {
                // Validate using Regex
                if (Pattern.matches(datatype.getRegex(), valuefield))
                    {
                    try
                        {
                        // Remove any leading '+' because it seems to be a dumb parser
                        if ((valuefield.length() > 1)
                            && (valuefield.startsWith(PREFIX_PLUS)))
                            {
                            number = Integer.valueOf(valuefield.substring(1), datatype.getRadix());
                            }
                        else
                            {
                            number = Integer.valueOf(valuefield, datatype.getRadix());
                            }
                        }

                    catch (NumberFormatException exception)
                        {
                        errors.add("[" + valuefield + "]" + INVALID_BUT_REGEX_OK);
                        }
                    }
                else
                    {
                    errors.add("[" + valuefield + "]" + INVALID_DECIMAL_INTEGER);
                    errors.add("[" + valuefield + "]" + MSG_REGEX + datatype.getRegex());
                    }
                }
            else
                {
                // Do the best we can
                try
                    {
                    if ((valuefield.length() > 1)
                        && (valuefield.startsWith(PREFIX_PLUS)))
                        {
                        number = Integer.valueOf(valuefield.substring(1), datatype.getRadix());
                        }
                    else
                        {
                        number = Integer.valueOf(valuefield, datatype.getRadix());
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    errors.add("[" + valuefield + "]" + INVALID_DECIMAL_INTEGER);
                    }
                }
            }

        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  SOURCE + "[number=" + number.toString() + "]");
        return (number);
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
            // The Response bytes are ASCII characters of a DECIMAL representation of the signed Integer
            // Leading zeroes may be present when required
            if (Pattern.matches(responsetype.getRegex(), valuefield))
                {
                try
                    {
                    // Remove any leading '+' because of the dumb parser...
                    if ((valuefield.length() > 1)
                        && (valuefield.startsWith(PREFIX_PLUS)))
                        {
                        Integer.parseInt(valuefield.substring(1), datatype.getRadix());
                        }
                    else
                        {
                        Integer.parseInt(valuefield, datatype.getRadix());
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    errors.add("[name=" + responsetype.getName() + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_DECIMAL_INTEGER);
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
                // Parses the string argument as a DECIMAL integer.
                // Parses the string argument as a signed integer in the radix specified
                // by the second argument. The characters in the string must all be digits of
                // the specified radix, that the first character may be an ASCII minus sign
                // '-' ('\u002D') to indicate a negative value.
                // Leading zeroes may be present when required

                // Remove any leading '+' because of the dumb parser...
                if ((valuefield.length() > 1)
                    && (valuefield.startsWith(PREFIX_PLUS)))
                    {
                    Integer.parseInt(valuefield.substring(1), datatype.getRadix());
                    }
                else
                    {
                    Integer.parseInt(valuefield, datatype.getRadix());
                    }
                }

            catch (NumberFormatException exception)
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_DECIMAL_INTEGER);
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
