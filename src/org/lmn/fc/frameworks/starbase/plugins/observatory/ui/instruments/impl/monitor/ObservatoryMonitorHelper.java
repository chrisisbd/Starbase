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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * ObservatoryMonitorHelper.
 */

public final class ObservatoryMonitorHelper implements FrameworkStrings,
                                                       FrameworkMetadata,
                                                       FrameworkRegex,
                                                       FrameworkSingletons,
                                                       ResourceKeys
    {
    private static final String TITLE_MEMORY_USAGE      = "Memory Usage";
    private static final String LABEL_LOCAL_TIME        = "Local Time";
    private static final String LABEL_USAGE_PERCENTAGE  = "Percentage Used";


    /***********************************************************************************************
     * Create the Metadata to describe the MemoryMonitor Channel.
     * This is applied to the MemoryMonitor tab every time the chart is updated.
     *
     * For correct rendering and export, Charts must receive the following metadata:
     *
     * Observation.Channel.Name.n
     * Observation.Channel.DataType.n
     * Observation.Channel.Units.n
     * Observation.Channel.Description.n
     * Observation.Channel.Colour.n
     * Observation.Title
     * Observation.Axis.Label.X
     * Observation.Axis.Label.Y.n
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createMemoryMonitorChannelMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(6);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      TITLE_MEMORY_USAGE,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      DataTypeDictionary.DECIMAL_INTEGER.getName(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      "The percentage of memory used as a function of time",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      "r=255 g=000 b=000",
                                      REGEX_COLOUR,
                                      DataTypeDictionary.COLOUR_DATA,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      TITLE_MEMORY_USAGE,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHART_TITLE);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      LABEL_LOCAL_TIME,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHART_AXIS_X);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      LABEL_USAGE_PERCENTAGE,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHART_AXIS_Y_0);

        return (listMetadata);
        }
    }
