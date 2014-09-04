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

import org.lmn.fc.common.exceptions.YearMonthDayException;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeParserInterface;
import org.lmn.fc.model.datatypes.RootDataTypeInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;


/***************************************************************************************************
 * YearMonthDayParser.
 */

public class YearMonthDayParser implements DataTypeParserInterface
    {
    // The Parser is a Singleton!
    private volatile static DataTypeParserInterface PARSER_INSTANCE;

    // String Resources
    private static final String DEFAULT_DELIMITER = "-";


    /***********************************************************************************************
     * The Parser is a Singleton!
     *
     * @return DataTypeParserInterface
     */

    public static DataTypeParserInterface getInstance()
        {
        if (PARSER_INSTANCE == null)
            {
            synchronized (YearMonthDayParser.class)
                {
                if (PARSER_INSTANCE == null)
                    {
                    PARSER_INSTANCE = new YearMonthDayParser();
                    }
                }
            }

        return (PARSER_INSTANCE);
        }


    /**********************************************************************************************
     * Parse a string into a YMD.
     *
     * @param ymd
     * @param delimiter
     *
     * @return
     *
     * @throws YearMonthDayException
     */

    private static YearMonthDayInterface parseYMD(final String ymd,
                                                  final String delimiter) throws YearMonthDayException
        {
        final String SOURCE = "YearMonthDayParser.parseYMD() ";
        String strYear;
        final int intYear;
        final String strMonth;
        final int intMonth;
        final String strDay;
        final int intDay;
        final boolean boolSign;
        final int intIndex0;
        final int intIndex1;
        final int intIndex2;

       // ToDo This parser might be a lot easier with Regex!

        if (ymd == null)
            {
            // Nothing to parse!
            throw new YearMonthDayException(SOURCE + "Null value, unable to parse");
            }
        else
            {
            // Find the first occurrence of the delimiter,
            // to chop off what should be the Year part
            intIndex0 = ymd.indexOf(delimiter);

            if (intIndex0 == -1)
                {
                // No delimiter found, so the format is invalid
                throw new YearMonthDayException(SOURCE + "Invalid format - no first delimiter");
                }
            else
                {
                try
                    {
                    // Chop off the years part
                    strYear = ymd.substring(0, intIndex0);

                    // Remove any leading '+' because of the dumb parser...
                    if (strYear.startsWith("+"))
                        {
                        strYear = strYear.substring(1);
                        }

                    // Parse the Year, this may produce NumberFormatException
                    // Allow any number of positive or negative Years at this stage
                    intYear = Math.abs(Integer.parseInt(strYear));

                    // Range check the Year
                    if (intYear > 9999)
                        {
                        throw new YearMonthDayException(SOURCE + "Year out of range [year=" + intYear + "]");
                        }

                    // Trap the case of zero years, but non-zero months
                    boolSign = !strYear.startsWith("-");

                    // Now look for the second delimiter, to get Month
                    intIndex1 = ymd.indexOf(delimiter, intIndex0 + 1);

                    if (intIndex1 == -1)
                        {
                        // There is no second delimiter, so the format is invalid
                        throw new YearMonthDayException(SOURCE + "Invalid format - no second delimiter");
                        }
                    else
                        {
                        // Chop off the Month part
                        strMonth = ymd.substring(intIndex0 + 1, intIndex1);

                        // Parse the Month, this may produce NumberFormatException
                        intMonth = Integer.parseInt(strMonth);

                        // Range check the Month
                        if ((intMonth < 1) || (intMonth > 12))
                            {
                            throw new YearMonthDayException(SOURCE + "Month out of range [month=" + intMonth + "]");
                            }

                        // If we find another delimiter, it is invalid
                        intIndex2 = ymd.indexOf(delimiter, intIndex1 + 1);

                        if (intIndex2 != -1)
                            {
                            // Too many delimiters, so the format is invalid
                            throw new YearMonthDayException(SOURCE + "Invalid format - too many delimiters");
                            }
                        else
                            {
                            // What is left must be the days
                            strDay = ymd.substring(intIndex1 + 1);

                            // Parse the Day, this may produce NumberFormatException
                            intDay = Integer.parseInt(strDay);

                            // The Constructor will check that the Day is correct for the Month
                            return (new YearMonthDayDataType(boolSign, intYear, intMonth, intDay));
                            }
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    throw new YearMonthDayException(SOURCE + "Invalid Number Format [exception=" + exception.getMessage() + "]", exception);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Check that the Day number is valid for this Month.
     * NOTE: Months are specified {0...11}.
     *
     * @param year
     * @param month
     * @param day
     *
     * @return boolean
     */

    public static boolean isValidDayForMonth(final int year,
                                             final int month,
                                             final int day)
        {
        // Months begin Jan=0 !
        final int[] arrayDayInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        final GregorianCalendar calendarTest;

        calendarTest = new GregorianCalendar(year, 0, 1);

        // Check for a leap year, and modify the look up table if so
        if (calendarTest.isLeapYear(year))
            {
            arrayDayInMonth[1] = 29;
            }

        return ((day > 0) && (day <= arrayDayInMonth[month]));
        }


    /***********************************************************************************************
     * Get the number of days since the beginning of the Year.
     * Returns the DayNumber {1...365}.
     * The Year is required only to test for Leap Years.
     * NOTE: Months are specified {0...11}.
     *
     * @param year
     * @param month
     * @param day
     *
     * @return int
     */

    public static int getDayNumberOfYear(final int year,
                                         final int month,
                                         final int day)
        {
        final int[] arrayDayInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        final GregorianCalendar calendarTest;
        int intDayNumber;

        // Months begin Jan=0 !
        calendarTest = new GregorianCalendar(year, 0, 1);

        // Check for a leap year, and modify the look up table if so
        if (calendarTest.isLeapYear(year))
            {
            arrayDayInMonth[1] = 29;
            }

        intDayNumber = 0;

        if (month > 0)
            {
            for (int intMonthIndex = 0;
                    intMonthIndex < month;
                    intMonthIndex++)
                {
                intDayNumber += arrayDayInMonth[intMonthIndex];
                }
            }

        // Add the days in the specified Month
        intDayNumber += day;

        return (intDayNumber);
        }


    /***********************************************************************************************
     * Privately construct the YearMonthDayParser.
     */

    private YearMonthDayParser()
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
                    parseYMD(parameter.getValue(), DEFAULT_DELIMITER);
                    }

                catch (YearMonthDayException exception)
                    {
                    errors.add("[name=" + parameter.getName() + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_YEAR_MONTH_DAY);
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
                parseYMD(parameter.getValue(), DEFAULT_DELIMITER);
                }

            catch (YearMonthDayException exception)
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_YEAR_MONTH_DAY);
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
            parseYMD(valuefield, DEFAULT_DELIMITER);
            }

        catch (YearMonthDayException exception)
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
            dataTypeInterface = parseYMD(valuefield, DEFAULT_DELIMITER);
            }

        catch (YearMonthDayException exception)
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
                    parseYMD(valuefield, DEFAULT_DELIMITER);
                    }

                catch (YearMonthDayException exception)
                    {
                    errors.add("[name=" + responsetype.getName() + "]" + INVALID_BUT_REGEX_OK);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_YEAR_MONTH_DAY);
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
                parseYMD(valuefield, DEFAULT_DELIMITER);
                }

            catch (YearMonthDayException exception)
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_YEAR_MONTH_DAY);
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
