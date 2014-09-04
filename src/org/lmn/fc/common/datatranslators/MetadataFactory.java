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

package org.lmn.fc.common.datatranslators;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration.ConfigurationUIComponent;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * MetadataFactory.
 */

public final class MetadataFactory implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkRegex,
                                              FrameworkSingletons,
                                              ResourceKeys
    {
    // Metadata Object Model
    public static final String DESCRIPTION_OBSERVATORY_NAME        = "The Name of the Observatory";
    public static final String DESCRIPTION_OBSERVATORY_LONGITUDE   = "The Longitude of the Observatory";
    public static final String DESCRIPTION_OBSERVATORY_LATITUDE    = "The Latitude of the Observatory";
    public static final String DESCRIPTION_OBSERVATORY_LOCATION    = "The Location of the Observatory";

    public static final String DESCRIPTION_OBSERVER_NAME       = "The Name of the Observer";

    public static final String DESCRIPTION_OBSERVATION_TITLE   = "The Title of the Observation";
    public static final String DESCRIPTION_OBSERVATION_DATE    = "The Date of the Observation";
    public static final String DESCRIPTION_OBSERVATION_TIME    = "The Time of the Observation";
    public static final String DESCRIPTION_OBSERVATION_START_TIME = "The Time of the Start of the Observation";
    public static final String DESCRIPTION_OBSERVATION_START_DATE = "The Date of the Start of the Observation";
    public static final String DESCRIPTION_OBSERVATION_FINISH_TIME = "The Time of the End of the Observation";
    public static final String DESCRIPTION_OBSERVATION_FINISH_DATE = "The Date of the End of the Observation";
    public static final String DESCRIPTION_OBSERVATION_TIME_ZONE = "The Time Zone of the Observation";
    public static final String DESCRIPTION_OBSERVATION_TIMESYSTEM = "The Time System of the Observation";
    public static final String DESCRIPTION_OBSERVATION_NOTES    = "Observation Notes";

    public static final String DESCRIPTION_CHANNEL_COUNT       = "The Raw Data Channel Count";
    public static final String DESCRIPTION_CHANNEL_NAME        = "The Name of Channel ";
    public static final String DESCRIPTION_CHANNEL_COLOUR      = "The Colour of Channel ";
    public static final String DESCRIPTION_CHANNEL_DATATYPE    = "The DataType of Channel ";
    public static final String DESCRIPTION_CHANNEL_UNITS       = "The Units of Channel ";
    public static final String DESCRIPTION_CHANNEL_OFFSET      = "The Offset of Channel ";
    public static final String DESCRIPTION_CHANNEL_DESCRIPTION = "The Description of Channel ";

    private static final String DESCRIPTION_COLUMN_NAME        = "The Name of Column ";
    private static final String DESCRIPTION_COLUMN_DATATYPE    = "The DataType of the contents of Column ";
    private static final String DESCRIPTION_COLUMN_UNITS       = "The Units of the contents of Column ";
    private static final String DESCRIPTION_COLUMN_DESCRIPTION = "The Description of the contents of Column ";

    public static final String DESCRIPTION_CHART_TITLE         = "The Title of the Chart";
    public static final String DESCRIPTION_CHART_AXIS_Y_0      = "The label for the chart first Y axis";
    public static final String DESCRIPTION_CHART_AXIS_Y_1      = "The label for the chart second Y axis";
    public static final String DESCRIPTION_CHART_AXIS_Y_MAX    = "The maximum value of the chart Y axis";
    public static final String DESCRIPTION_CHART_AXIS_Y_MIN    = "The minimum value of the chart Y axis";
    public static final String DESCRIPTION_CHART_AXIS_X        = "The label for the chart X axis";

    // RSP
    public static final String DESCRIPTION_RSP_METADATA        = "RSP Metadata";
    public static final String DESCRIPTION_RSP_VERSION         = "RSP Software Version";
    public static final String DESCRIPTION_RSP_SOURCE          = "RSP Source";
    public static final String DESCRIPTION_RSP_NOTE_LENGTH     = "The length of the RSP Note field";
    public static final String DESCRIPTION_RSP_RAW_NOTE        = "The raw RSP Note field";
    public static final String DESCRIPTION_RSP_TIMESTAMPED     = "A flag to indicate if the RSP data are timestamped";
    public static final String DESCRIPTION_RSP_DOUBLE          = "A flag to indicate if the RSP data are double precision";

    // DataMetadata Keys
    private static final String CONFIGURATION_PROPERTY          = "Property.";
    private static final String CONFIGURATION_VALUE             = "Value";
    private static final String CONFIGURATION_UPDATED           = "Updated";

    // Names used for default items
    private static final String DEFAULT_NAME_COLUMN             = "Column";

    private static final String KEY_ERROR                       = "Error";


    /***********************************************************************************************
     * Create the default MetadataMetadata for the standard configuration.
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createDefaultMetadataMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(16);

        // Each item of Metadata must have {Name, DataType, Units, Description}
        // So we need Metadata describing each of those facets of a Metadata item

        //------------------------------------------------------------------------------------------
        // Metadata Name, or Key

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_NAME.getKey() + MetadataDictionary.SUFFIX_NAME,
                                      "Name",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Metadata key");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_NAME.getKey() + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Metadata key");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_NAME.getKey() + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Metadata key");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_NAME.getKey() + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Metadata key",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Metadata key");

        //------------------------------------------------------------------------------------------
        // Metadata DataType

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_NAME,
                                      "DataType",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Metadata DataType");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.DATA_TYPE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Metadata DataType");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Metadata DataType");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Metadata DataType",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Metadata DataType");

        //------------------------------------------------------------------------------------------
        // Metadata Units

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_UNITS.getKey() + MetadataDictionary.SUFFIX_NAME,
                                      "Units",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Metadata Units");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_UNITS.getKey() + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.UNITS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Metadata Units");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_UNITS.getKey() + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Metadata Units");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_UNITS.getKey() + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Metadata Units",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Metadata Units");

        //------------------------------------------------------------------------------------------
        // Metadata Description

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_NAME,
                                      "Description",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Metadata Description");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Metadata Description");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Metadata Description");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Metadata Description",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Metadata Description");

        return (listMetadata);
        }


    /***********************************************************************************************
     * Create the default ReportTable DataMetadata for the standard configuration.
     * The InstrumentLogs and EventLogs may have different columns,
     * so we can't rely on there being Date & Time in the first two columns.
     * Use the Stardata Record format of: Column0 Column1 Column2 ...
     * Currently used only in StardataTranslatorWriters.writeLog() and .writeReportTable().
     *
     * @param datasettype
     * @param columns
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createDefaultReportTableMetadata(final DatasetType datasettype,
                                                                  final int columns)
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(columns << 2);

        // Add the column Metadata
        for (int intColumnIndex = 0;
            intColumnIndex < columns;
            intColumnIndex++)
            {
            MetadataHelper.addNewMetadata(listMetadata,
                                          getColumnNameKeyForType(datasettype, intColumnIndex),
                                          DEFAULT_NAME_COLUMN + SPACE + intColumnIndex,
                                          REGEX_STRING,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          DESCRIPTION_COLUMN_NAME + intColumnIndex);

            MetadataHelper.addNewMetadata(listMetadata,
                                          getColumnDataTypeKeyForType(datasettype, intColumnIndex),
                                          DataTypeDictionary.STRING.toString(),
                                          REGEX_NONE,
                                          DataTypeDictionary.DATA_TYPE,
                                          SchemaUnits.DIMENSIONLESS,
                                          DESCRIPTION_COLUMN_DATATYPE + intColumnIndex);

            MetadataHelper.addNewMetadata(listMetadata,
                                          getColumnUnitsKeyForType(datasettype, intColumnIndex),
                                          SchemaUnits.DIMENSIONLESS.toString(),
                                          REGEX_NONE,
                                          DataTypeDictionary.UNITS,
                                          SchemaUnits.DIMENSIONLESS,
                                          DESCRIPTION_COLUMN_UNITS + intColumnIndex);

            MetadataHelper.addNewMetadata(listMetadata,
                                          getColumnDescriptionKeyForType(datasettype, intColumnIndex),
                                          DEFAULT_NAME_COLUMN + SPACE + intColumnIndex,
                                          REGEX_STRING,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          DESCRIPTION_COLUMN_DESCRIPTION + intColumnIndex);
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Derive the Metadata for a ReportTable from the ReportColumnMetadata.
     * Used in StardataTranslatorWriters.writeReportTable() and CsvTranslatorWriters.writeReportTable().
     *
     * @param datasettype
     * @param report
     *
     * @return List<Metadata>
     */

    public static List<Metadata> deriveReportColumnMetadata(final DatasetType datasettype,
                                                            final ReportTablePlugin report)
        {
        final List<Metadata> listMetadata;

        // There could be up to 32 Channels, each with 4 items
        listMetadata = new ArrayList<Metadata>(128);

        if ((report != null)
            && (report.getReportTableModel() != null))
            {
            final Vector<ReportColumnMetadata> vecColumnMetadata;
            final Iterator<ReportColumnMetadata> iterColumnData;
            int intColumnIndex;

            vecColumnMetadata = report.defineColumns();
            iterColumnData = vecColumnMetadata.iterator();
            intColumnIndex = 0;

            // Add the column Metadata
            while (iterColumnData.hasNext())
                {
                final ReportColumnMetadata metadataColumn;

                metadataColumn = iterColumnData.next();

                MetadataHelper.addNewMetadata(listMetadata,
                                              getColumnNameKeyForType(datasettype, intColumnIndex),
                                              metadataColumn.getName(),
                                              REGEX_STRING,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              DESCRIPTION_COLUMN_NAME + intColumnIndex);

                MetadataHelper.addNewMetadata(listMetadata,
                                              getColumnDataTypeKeyForType(datasettype, intColumnIndex),
                                              metadataColumn.getDataType().toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.DATA_TYPE,
                                              SchemaUnits.DIMENSIONLESS,
                                              DESCRIPTION_COLUMN_DATATYPE + intColumnIndex);

                MetadataHelper.addNewMetadata(listMetadata,
                                              getColumnUnitsKeyForType(datasettype, intColumnIndex),
                                              metadataColumn.getUnits().toString(),
                                              REGEX_NONE,
                                              DataTypeDictionary.UNITS,
                                              SchemaUnits.DIMENSIONLESS,
                                              DESCRIPTION_COLUMN_UNITS + intColumnIndex);

                MetadataHelper.addNewMetadata(listMetadata,
                                              getColumnDescriptionKeyForType(datasettype, intColumnIndex),
                                              metadataColumn.getDescription(),
                                              REGEX_STRING,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              DESCRIPTION_COLUMN_DESCRIPTION + intColumnIndex);
                intColumnIndex++;
                }
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Get the Key of the Name of the specified column index, given its DatasetType.
     *
     * @param datasettype
     * @param index
     *
     * @return String
     */

    private static String getColumnNameKeyForType(final DatasetType datasettype,
                                                  final int index)
        {
        final String strKey;

        switch (datasettype)
            {
            case TABULAR:
                {
                strKey = MetadataDictionary.KEY_COLUMN_NAME.getKey() + index;
                break;
                }

            case XY:
                {
                strKey = MetadataDictionary.KEY_COLUMN_NAME.getKey() + index;
                break;
                }

            case TIMESTAMPED:
                {
                switch (index)
                    {
                    case 0:
                        {
                        strKey = MetadataDictionary.KEY_OBSERVATION_DATE.getKey() + index;
                        break;
                        }

                    case 1:
                        {
                        strKey = MetadataDictionary.KEY_OBSERVATION_TIME.getKey() + index;
                        break;
                        }

                    default:
                        {
                        // Observation.Channel.Name.NNN
                        strKey = MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + (index-2);
                        }
                    }

                break;
                }

            default:
                {
                strKey = KEY_ERROR;
                }
            }

        return (strKey);
        }


    /***********************************************************************************************
     * Get the Key of the DataType of the specified column index, given its DatasetType.
     *
     * @param datasettype
     * @param index
     *
     * @return String
     */

    private static String getColumnDataTypeKeyForType(final DatasetType datasettype,
                                                      final int index)
        {
        final String strKey;

        switch (datasettype)
            {
            case TABULAR:
                {
                strKey = MetadataDictionary.KEY_COLUMN_DATATYPE.getKey() + index;
                break;
                }

            case XY:
                {
                strKey = MetadataDictionary.KEY_COLUMN_DATATYPE.getKey() + index;
                break;
                }

            case TIMESTAMPED:
                {
                strKey = MetadataDictionary.KEY_COLUMN_DATATYPE.getKey() + index;
                break;
                }

            default:
                {
                strKey = KEY_ERROR;
                }
            }

        return (strKey);
        }


    /***********************************************************************************************
     * Get the Key of the Units of the specified column index, given its DatasetType.
     *
     * @param datasettype
     * @param index
     *
     * @return String
     */

    private static String getColumnUnitsKeyForType(final DatasetType datasettype,
                                                   final int index)
        {
        final String strKey;

        switch (datasettype)
            {
            case TABULAR:
                {
                strKey = MetadataDictionary.KEY_COLUMN_UNITS.getKey() + index;
                break;
                }

            case XY:
                {
                strKey = MetadataDictionary.KEY_COLUMN_UNITS.getKey() + index;
                break;
                }

            case TIMESTAMPED:
                {
                strKey = MetadataDictionary.KEY_COLUMN_UNITS.getKey() + index;
                break;
                }

            default:
                {
                strKey = KEY_ERROR;
                }
            }

        return (strKey);
        }


    /***********************************************************************************************
     * Get the Key of the Description of the specified column index, given its DatasetType.
     *
     * @param datasettype
     * @param index
     *
     * @return String
     */

    private static String getColumnDescriptionKeyForType(final DatasetType datasettype,
                                                         final int index)
        {
        final String strKey;

        switch (datasettype)
            {
            case TABULAR:
                {
                strKey = MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + index;
                break;
                }

            case XY:
                {
                strKey = MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + index;
                break;
                }

            case TIMESTAMPED:
                {
                strKey = MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + index;
                break;
                }

            default:
                {
                strKey = KEY_ERROR;
                }
            }

        return (strKey);
        }


    /***********************************************************************************************
     * Create the default ConfigurationDataMetadata for the standard configuration.
     * Use the Stardata Record format of: Property, Value, Updated.
     * Ideally Configuration should be in the same format as Metadata.
     * Used in StardataTranslatorWriters.writeConfiguration().
     *
     * @param columncount
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createDefaultConfigurationDataMetadata(final int columncount)
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(columncount << 2);

        //------------------------------------------------------------------------------------------
        // Configuration Property

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_PROPERTY + MetadataDictionary.SUFFIX_NAME,
                                      ConfigurationUIComponent.TITLE_PROPERTY,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Property Name column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_PROPERTY + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the contents of the Property Name column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_PROPERTY + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the contents of the Property Name column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_PROPERTY + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Property name",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the contents of the Property Name column");

        //------------------------------------------------------------------------------------------
        // Configuration Value

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_VALUE + MetadataDictionary.SUFFIX_NAME,
                                      ConfigurationUIComponent.TITLE_VALUE,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Property Value column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_VALUE + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the contents of the Property Value column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_VALUE + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the contents of the Property Value column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_VALUE + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Property Value",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the contents of the Property Value column");

        //------------------------------------------------------------------------------------------
        // Configuration Updated

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_UPDATED + MetadataDictionary.SUFFIX_NAME,
                                      ConfigurationUIComponent.TITLE_UPDATED,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Name of the Property Updated flag column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_UPDATED + MetadataDictionary.SUFFIX_DATA_TYPE,
                                      DataTypeDictionary.BOOLEAN.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the contents of the Property Updated flag column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_UPDATED + MetadataDictionary.SUFFIX_UNITS,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the contents of the Property Updated flag column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_ROOT.getKey() + CONFIGURATION_UPDATED + MetadataDictionary.SUFFIX_DESCRIPTION,
                                      "The Property Updated flag",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the contents of the Property Updated flag column");

        return (listMetadata);
        }
    }



/***********************************************************************************************
 * Create the default RawDataMetadata for the standard configuration,
 * if none supplied for Import or Export.
 * Do not create an item if it appears in the specified Metadata list.
 * All channels are assumed to be of type DECIMAL_INTEGER, of DIMENSIONLESS Units,
 * since we have no way of knowing otherwise.
 * Used to read and write RawData.
 *
 * @param metadatalist
 * @param columns
 * @param usecalendar
 *
 * @return List<Metadata>
 */

// THIS SEEMED TO BE MORE TROUBLE THAN IT WAS WORTH?!
//    public static List<Metadata> createMissingDefaultRawDataMetadata(final List<Metadata> metadatalist,
//                                                                     final int columns,
//                                                                     final boolean usecalendar)
//        {
//        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
//                               "CREATE DEFAULT METADATA - NO TEMPERATURE CHANNEL!!");
//
//        return (createDefaultDataMetadata(metadatalist, columns, usecalendar, DataTypeDictionary.DECIMAL_INTEGER));
//        }


/***********************************************************************************************
 * Create the default XYDatasetDataMetadata for the standard configuration,
 * if none supplied for Import or Export.
 * Do not create an item if it appears in the specified Metadata list.
 * This is the same as RawData, except that the data are written as STRING.
 * Used to write XYDatasets.
 *
 * @param metadatalist
 * @param columns
 * @param usecalendar
 *
 * @return List<Metadata>
 */

// THIS SEEMED TO BE MORE TROUBLE THAN IT WAS WORTH?!
//    public static List<Metadata> createMissingDefaultXYDatasetDataMetadata(final List<Metadata> metadatalist,
//                                                                           final int columns,
//                                                                           final boolean usecalendar)
//        {
//        return (createDefaultDataMetadata(metadatalist, columns, usecalendar, DataTypeDictionary.STRING));
//        }


/***********************************************************************************************
 * Create default DataMetadata for the standard configuration,
 * if none supplied for Import or Export.
 * Do not create an item if it appears in the specified Metadata list.
 * Used for RawData and XYDataset.
 *
 * @param metadatalist
 * @param columns
 * @param usecalendar
 * @param datatype
 *
 * @return List<Metadata>
 */

// THIS SEEMED TO BE MORE TROUBLE THAN IT WAS WORTH?!

//    private static List<Metadata> createDefaultDataMetadata(final List<Metadata> metadatalist,
//                                                            final int columns,
//                                                            final boolean usecalendar,
//                                                            final DataTypeDictionary datatype)
//        {
//        final List<Metadata> listMetadata;
//
//        listMetadata = new ArrayList<Metadata>(8 + (columns << 2));
//
//        // Each item of XYDataset Metadata must have {Name, DataType, Units, Description}
//
//        if (usecalendar)
//            {
//            //--------------------------------------------------------------------------------------
//            // Date Metadata
//            // Do we need to use the Date and Time elements?
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_DATE.getKey() + NAME,
//                                          DEFAULT_NAME_DATE,
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The Name of the Record Date column");
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_DATE.getKey() + DATA_TYPE,
//                                          DataTypeDictionary.DATE_YYYY_MM_DD.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.DATA_TYPE,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The DataType of the Record Date");
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_DATE.getKey() + UNITS,
//                                          SchemaUnits.YEAR_MONTH_DAY.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.UNITS,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The Units of the Record Date");
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_DATE.getKey() + DESCRIPTION,
//                                          "The Record Date",
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The Description of the Record Date");
//
//            //--------------------------------------------------------------------------------------
//            // Time Metadata
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_TIME.getKey() + NAME,
//                                          DEFAULT_NAME_TIME,
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The Name of the Record Time column");
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_TIME.getKey() + DATA_TYPE,
//                                          DataTypeDictionary.TIME_HH_MM_SS.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.DATA_TYPE,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The DataType of the Record Time");
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_TIME.getKey() + UNITS,
//                                          SchemaUnits.HOUR_MIN_SEC.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.UNITS,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The Units of the Record Time");
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_TIME.getKey() + DESCRIPTION,
//                                          "The Record Time",
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          "The Description of the Record Time");
//            }
//        else
//            {
//            // We need to specify the X Column of the Indexed record
//            // but we don't know much about the contents
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_X.getKey() + NAME,
//                                          DEFAULT_NAME_X,
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_NAME + MetadataDictionary.SUFFIX_CHANNEL_X);
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_X.getKey() + DATA_TYPE,
//                                          DataTypeDictionary.STRING.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.DATA_TYPE,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_X);
//
//            // We don't know what the Units were, so they must be DIMENSIONLESS
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_X.getKey() + UNITS,
//                                          SchemaUnits.DIMENSIONLESS.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.UNITS,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_X);
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_X.getKey() + DESCRIPTION,
//                                          "The X value",
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_X);
//            }
//
//        //-----------------------------------------------------------------------------------------
//        // Now add the column (channel) Metadata
//        // This is the same as RawData, except that the data are written as Strings
//
//        // Timestamped format is: <Calendar> <Channel0> <Channel1> <Channel2>
//        // Indexed format is: <X_Value> <Channel0> <Channel1> <Channel2>
//        // so the number of data columns is the same
//
//        for (int intColumnIndex = 0;
//             intColumnIndex < columns;
//             intColumnIndex++)
//            {
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + intColumnIndex,
//                                          DEFAULT_NAME_CHANNEL + SPACE + intColumnIndex,
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_NAME + intColumnIndex);
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + intColumnIndex,
//                                          datatype.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.DATA_TYPE,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_DATATYPE + intColumnIndex);
//
//            // We don't know what the Units were, so they must be DIMENSIONLESS
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + intColumnIndex,
//                                          SchemaUnits.DIMENSIONLESS.toString(),
//                                          REGEX_NONE,
//                                          DataTypeDictionary.UNITS,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_UNITS + intColumnIndex);
//
//            MetadataHelper.addNewMetadata(listMetadata,
//                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + intColumnIndex,
//                                          DEFAULT_NAME_CHANNEL + SPACE + intColumnIndex,
//                                          REGEX_STRING,
//                                          DataTypeDictionary.STRING,
//                                          SchemaUnits.DIMENSIONLESS,
//                                          DESCRIPTION_CHANNEL_DESCRIPTION + intColumnIndex);
//            }
//
//        return (listMetadata);
//        }



