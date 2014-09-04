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
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.List;


/***************************************************************************************************
 * The DataTypeParserInterface.
 */

public interface DataTypeParserInterface extends FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkSingletons,
                                                 FrameworkMetadata,
                                                 FrameworkRegex,
                                                 ResourceKeys
    {
    // String Resources
    String ERROR_NO_PARSER = "DataTypeParser: No parser implementation";
    String ERROR_NON_NUMERIC = "DataTypeParser: Parsing not supported for non-numeric DataType";
    String PREFIX_PLUS = "+";
    String MSG_REGEX = " Regex=";
    String INVALID_SYNTAX = " is not using the correct syntax";
    String INVALID_BUT_REGEX_OK = " is invalid, but did match its Regex";

    // Numerics
    String INVALID_DECIMAL_BYTE = " is not a valid Decimal Byte, or is out of range";
    String INVALID_DECIMAL_DIGIT = " is not a valid Decimal Digit";
    String INVALID_DECIMAL_INTEGER = " is not a valid Decimal Integer, or is out of range";
    String INVALID_DECIMAL_FLOAT = " is not a valid Float, or is out of range";
    String INVALID_DECIMAL_DOUBLE = " is not a valid Double, or is out of range";

    String INVALID_HEX_BYTE = " is not a valid Hex Byte, or is out of range";
    String INVALID_HEX_DIGIT = " is not a valid Hex Digit";
    String INVALID_HEX_INTEGER = " is not a valid Hex Integer, or is out of range";

    // Dates and Times
    String INVALID_YEAR_MONTH_DAY = " is not a valid YearMonthDayDataType";
    String INVALID_HOUR_MIN_SEC = " is not a valid HourMinSecDataType";
    String INVALID_TIME_ZONE = " is not a valid TimeZone";

    // Angles Superclasses
    String INVALID_ANGLE = " is not a valid Angle";
    String INVALID_DEG_MIN_SEC = " is not a valid DegMinSec";

    // Metadata
    String INVALID_METADATA_KEY = " is not a valid Metadata Key";
    String INVALID_DATATYPE = " is not a valid DataType";
    String INVALID_UNITS = " is not a valid Units";

    // Miscellaneous
    String INVALID_STRING = " is not a valid String";
    String INVALID_URL = " is not a valid URL";
    String INVALID_EXPRESSION = " is not a valid MathematicalExpression";
    String INVALID_BOOLEAN = " is not a valid Boolean (true or false)";
    String INVALID_BIT_STRING = " is not a valid BitString";
    String INVALID_COLOUR = " is not a valid Colour";
    String INVALID_FONT = " is not a valid Font";

    // Non-Metadata
    String INVALID_SCALED_HEX_INTEGER = " is not a valid Scaled Hex Integer, or is out of range";
    String INVALID_NUMERIC_INDEXED_LIST = " is not a valid Numeric Index List item, or is out of range";
    String INVALID_STARIBUS_BLOCK = " is not a valid StaribusBlock";
    String INVALID_STARIBUS_MULTICHANNEL_DATA = " is not a valid StaribusMultichannelData";
    String INVALID_XML = " is not valid XML";

    // These must be public so that they are accessible to e.g. Filters
    // Be careful not to use 'e' !
    String VARIABLE_X = "x";
    String VARIABLE_Y = "y";
    String VARIABLE_T = "t";


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

    boolean validateParameterValueAsDataType(ParameterType parameter,
                                             DataTypeDictionary datatype,
                                             List<String> errors);



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

    boolean validateValueFieldAsDataType(String valuefield,
                                         DataTypeDictionary datatype,
                                         List<String> errors);


    /***********************************************************************************************
     * Attempt to parse the ValueField into the specified DataType.
     * Return NULL if the parsing failed.
     * Called only from the static helper.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return Object
     */

    RootDataTypeInterface parseValueFieldToDataType(String valuefield,
                                                    DataTypeDictionary datatype,
                                                    String name,
                                                    String regex,
                                                    List<String> errors);


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

    Number parseValueFieldToNumber(String valuefield,
                                   DataTypeDictionary datatype,
                                   String name,
                                   String regex,
                                   List<String> errors);


    /***********************************************************************************************
     * Parse the byte array into a valid ResponseValue, returned in the ResponseType.
     * Uses the Name and Regex in the Response.
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

    boolean parseBytesToResponseValue(byte[] bytes,
                                      DataTypeDictionary datatype,
                                      ResponseType responsetype,
                                      List<String> errors);
    }
