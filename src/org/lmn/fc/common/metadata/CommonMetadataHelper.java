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

package org.lmn.fc.common.metadata;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * CommonMetadataHelper.
 */

public final class CommonMetadataHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkKeys,
                                                   FrameworkSingletons,
                                                   ResourceKeys
    {
    private static final int LENGTH_DATATYPE_VALUE = 1000;


    /***********************************************************************************************
     * Get an item of Metadata, given its Key.
     * Return null if the key cannot be found in the List.
     *
     * @param metadatalist
     * @param key
     *
     * @return Metadata
     */

    public static Metadata getMetadataByKey(final List<Metadata> metadatalist,
                                            final String key)
        {
        final String SOURCE = "CommonMetadataHelper.getMetadataByKey() ";
        Metadata metaData;

        metaData = null;

        if ((metadatalist != null)
            && (!metadatalist.isEmpty())
            && (key != null)
            && (!EMPTY_STRING.equals(key)))
            {
            final Iterator<Metadata> iterMetadata;

            iterMetadata = metadatalist.iterator();

            // Just iterate over all Metadata (there are unlikely to be many...)
            while ((iterMetadata.hasNext())
                && (metaData == null))
                {
                final Metadata metadata;

                metadata = iterMetadata.next();

                if ((metadata != null)
                    && (key.equals(metadata.getKey())))
                    {
                    metaData = metadata;
                    }
                }
            }

        return (metaData);
        }


    /***********************************************************************************************
     * Create a fully-specified item of Metadata.
     * This should be the ONLY place where Metadata are created in the Framework!
     *
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     *
     * @return Metadata
     */

    public static Metadata createMetadata(final String key,
                                          final String value,
                                          final String regex,
                                          final DataTypeDictionary datatype,
                                          final SchemaUnits.Enum units,
                                          final String description)
        {
        final String SOURCE = "CommonMetadataHelper.createMetadata() ";
        final Metadata metaData;

        // This should be the ONLY place where Metadata are created in the Framework!
        metaData = Metadata.Factory.newInstance();

        // Allow the Value, Regex and Description to be an empty String, but not null
        if ((key != null)
            && (!EMPTY_STRING.equals(key))
            && (value != null)
            && (regex != null)
            && (datatype != null)
            && (units != null)
            && (description != null))
            {
            metaData.setKey(key);
            metaData.setValue(value);
            metaData.setRegex(regex);
            metaData.setDataTypeName(datatype.getSchemaDataType());
            metaData.setUnits(units);
            metaData.setDescription(description);
            }

        return (metaData);
        }


    /***********************************************************************************************
     * Add an item of metadata to a List of Metadata.
     *
     * @param listmetadata
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     *
     * @return Metadata
     */

    public static Metadata addMetadata(final List<Metadata> listmetadata,
                                       final String key,
                                       final String value,
                                       final String regex,
                                       final DataTypeDictionary datatype,
                                       final SchemaUnits.Enum units,
                                       final String description)
        {
        final String SOURCE = "CommonMetadataHelper.addNewMetadata() ";
        final Metadata metaData;

        metaData = createMetadata(key,
                                  value,
                                  regex,
                                  datatype,
                                  units,
                                  description);

        // Did we get some valid Metadata?
        if ((listmetadata != null)
            && (key != null)
            && (key.equals(metaData.getKey())))
            {
            listmetadata.add(metaData);
            }

        return (metaData);
        }


    /***********************************************************************************************
     * Check that the MetadataValue is valid for the current DataType.
     * If Regex is supplied, then this takes precedence over any Regex in the DataType definition.
     * If errors occur, return an error count.
     * Used in FrameworkData.
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
        final String SOURCE = "CommonMetadataHelper.validateDataTypeOfMetadataValue() ";
        final boolean boolValid;

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
                        errors.add(SOURCE + "Value did not match supplied Regex [type=" + datatype.getName() + "] [regex=" + regex + "]");
                        }
                    }

                catch (PatternSyntaxException exception)
                    {
                    errors.add(SOURCE + "Invalid Regex expression [type=" + datatype.getName() + "] [regex=" + regex + "]");
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
                        errors.add(SOURCE + "Value did not match Regex in DataType [type=" + datatype.getName() + "] [regex=" + regex + "]");
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
                    errors.add(SOURCE + "Value could not be parsed by DataType [type=" + datatype.getName() + "]");
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

        return (errors.size());
        }
    }
