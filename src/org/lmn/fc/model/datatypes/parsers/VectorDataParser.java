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


/***************************************************************************************************
 * VectorDataParser.
 */

public class VectorDataParser implements DataTypeParserInterface
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
            synchronized (VectorDataParser.class)
                {
                if (PARSER_INSTANCE == null)
                    {
                    PARSER_INSTANCE = new VectorDataParser();
                    }
                }
            }

        return (PARSER_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the VectorDataParser.
     */

    private VectorDataParser()
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
        if (errors != null)
            {
            errors.add(ERROR_NO_PARSER);
            }

        return (false);
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
        if (errors != null)
            {
            errors.add(ERROR_NO_PARSER);
            }

        return false;
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
        if (responsetype != null)
            {
            responsetype.setValue(NO_DATA);
            }

        if (errors != null)
            {
            errors.add(ERROR_NO_PARSER);
            }

        return (false);
        }
    }
