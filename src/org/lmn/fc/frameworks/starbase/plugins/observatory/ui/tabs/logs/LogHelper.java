// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * LogHelper.
 */

public final class LogHelper implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkSingletons,
                                        FrameworkRegex
    {


    // This is the same as AbstractObservatoryInstrument.DEFAULT_INSTRUMENTLOG_WIDTH
    public static final int DEFAULT_LOG_COLUMN_COUNT = 5;


    /***********************************************************************************************
     * Get the default Log Metadata, i.e describing the columns of the Log.
     * SimpleEventLogUIComponent has five columns: Icon, Date, Time, Event, Source.
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createDefaultEventLogMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(DEFAULT_LOG_COLUMN_COUNT << 2);

        //------------------------------------------------------------------------------------------
        // Names

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      "Icon",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      "Date",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      "Time",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      "Event",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Event column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      "Source",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Source column");

        //------------------------------------------------------------------------------------------
        // DataTypes

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      // ToDO Review - what is the data type of an icon object?
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      DataTypeDictionary.DATE_YYYY_MM_DD.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      DataTypeDictionary.TIME_HH_MM_SS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Event column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Source column");

        //------------------------------------------------------------------------------------------
        // Units

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      SchemaUnits.YEAR_MONTH_DAY.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      SchemaUnits.HOUR_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Event column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Source column");

        //------------------------------------------------------------------------------------------
        // Descriptions

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      "An icon showing the event status",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      "The Date of the log entry",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      "The Time of the log entry",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      "The details of the logged Event",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Event column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      "The Source of the logged Event",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Source column");

        return (listMetadata);
        }


    /***********************************************************************************************
     * Update the EventLog fragment associated with this DAO.
     * Clear the fragment after the update.
     *
     * @param dao
     */

    public static void updateEventLogFragment(final ObservatoryInstrumentDAOInterface dao)
        {
        if (dao.getHostInstrument() != null)
            {
            // Add the EventLogFragment to the ObservatoryLog if possible
            if ((dao.getHostInstrument().getHostUI() != null)
                && (dao.getHostInstrument().getHostUI().getObservatoryLog() != null)
                && (dao.getHostInstrument().getHostUI().getObservatoryLog().getInstrumentPanel() != null))
                {
                // Always refresh the Log regardless of visibility
                // Don't update the Metadata
                dao.getHostInstrument().getHostUI().getObservatoryLog().getInstrumentPanel().setWrappedData(dao.getWrappedData(),
                                                                                                            true,
                                                                                                            false);

                // ToDo - Review clearing of fragment if the Instrument section below does not execute?
                }

            // Handle the EventLog for this Instrument
            if ((dao.getWrappedData().getEventLogFragment() != null)
                && (!dao.getWrappedData().getEventLogFragment().isEmpty())
                && (dao.getHostInstrument().getInstrumentPanel() != null)
                && (dao.getHostInstrument().getInstrumentPanel().getEventLogTab() != null))
                {
                dao.getHostInstrument().addEventLogFragment(dao.getWrappedData().getEventLogFragment());

                // Now remove the fragment, since it has been used
                dao.getWrappedData().getEventLogFragment().clear();
                dao.getWrappedData().getWrappedDAO().getEventLogFragment().clear();

                // Refresh only if visible
                if (UIComponentHelper.shouldRefresh(false,
                                                    dao.getHostInstrument(),
                                                    dao.getHostInstrument().getInstrumentPanel().getEventLogTab()))
                    {
                    ((ReportTablePlugin) dao.getHostInstrument().getInstrumentPanel().getEventLogTab()).refreshTable();
                    }
                }

            // Don't update the Metadata
            // ToDo REVIEW - old way superseded?
//            dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false);
//
//            if (dao.getEventLogFragment() != null)
//                {
//                dao.getEventLogFragment().clear();
//                }
            }
        }
    }
