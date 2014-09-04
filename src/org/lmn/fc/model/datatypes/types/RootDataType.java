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

package org.lmn.fc.model.datatypes.types;


import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeParserInterface;
import org.lmn.fc.model.datatypes.RootDataTypeInterface;


/***************************************************************************************************
 * RootDataType.
 */

public class RootDataType implements RootDataTypeInterface
    {
    private final DataTypeDictionary dataType;


    /***********************************************************************************************
     * Construct a RootDataType.
     *
     * @param type
     */

    public RootDataType(final DataTypeDictionary type)
        {
        this.dataType = type;
        }


    /***********************************************************************************************
     * Get the DataType enum of this DataType.
     *
     * @return DataTypeDictionary
     */

    public DataTypeDictionary getDataType()
        {
        return (this.dataType);
        }


    /***********************************************************************************************
     * Get the Parser of this DataType.
     * A convenience method, because the Parser could be obtained directly from the DataType.
     *
     * @return DataTypeParserInterface
     */

    public DataTypeParserInterface getParser()
        {
        if (getDataType() != null)
            {
            return (getDataType().getParser());
            }
        else
            {
            return (null);
            }
        }
    }
