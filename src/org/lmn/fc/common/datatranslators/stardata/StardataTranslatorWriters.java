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

package org.lmn.fc.common.datatranslators.stardata;

import org.apache.xmlbeans.XmlOptions;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.stardata.*;
import org.lmn.fc.ui.reports.ReportIcon;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


/***************************************************************************************************
 * StardataTranslatorWriters.
 */

public final class StardataTranslatorWriters implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        FrameworkRegex,
                                                        ResourceKeys
    {
    private static final String METADATA_FORMAT = "[format=stardata]";
    private static final int XML_INDENT = 4;
    private static final int INDEX_REPORT_COLUMN_DATE = 0;
    private static final int INDEX_REPORT_COLUMN_TIME = 1;


    /***********************************************************************************************
     * Write the DAO XYDataset in Stardata format.
     * All data and metadata come from the DAOWrapper.
     *
     * @param wrapper
     * @param verbose
     * @param stream
     * @param messages
     * @param compressed
     *
     * @return boolean
     */

    public static boolean writeXYDataset(final DAOWrapperInterface wrapper,
                                         final boolean verbose,
                                         final OutputStream stream,
                                         final List<String> messages,
                                         final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeXYDataset()";
        boolean boolSuccess;

        boolSuccess = false;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_XYDATASET + METADATA_ACTION_WRITE + METADATA_FORMAT);

        try
            {
            final StardataDocument docStarData;
            final StardataDocument.Stardata starData;
            final List<Metadata> listMetadata;
            final HeaderType header;
            final DataType dataType;

            docStarData = StardataDocument.Factory.newInstance();
            starData = docStarData.addNewStardata();

            //--------------------------------------------------------------------------------------
            // An optional Header, containing Metadata and Metadata describing the Metadata

            header = starData.addNewHeader();

            // Gather all available Metadata, but no MetadataMetadata
            listMetadata =  MetadataHelper.collectMetadataForExportFromWrapper(wrapper, false);

            // Write the Metadata, if any
            if ((listMetadata != null)
                && (!listMetadata.isEmpty()))
                {
                final List<Metadata> listMetadataMetadata;

                header.setMetadataArray(listMetadata.toArray(new Metadata[listMetadata.size()]));

                // Are there any MetadataMetadata?
                listMetadataMetadata = MetadataHelper.collectMetadataMetadata(wrapper.getWrappedDAO().getHostInstrument(),
                                                                              wrapper.getWrappedDAO(),
                                                                              wrapper);
                if ((listMetadataMetadata != null)
                    && (!listMetadataMetadata.isEmpty()))
                    {
                    header.setMetadataMetadataArray(listMetadataMetadata.toArray(new Metadata[listMetadataMetadata.size()]));
                    }
                else
                    {
                    final List<Metadata> listDefaultMetadataMetadata;

                    // If not, create some default MetadataMetadata
                    listDefaultMetadataMetadata = MetadataFactory.createDefaultMetadataMetadata();
                    header.setMetadataMetadataArray(listDefaultMetadataMetadata.toArray(new Metadata[listDefaultMetadataMetadata.size()]));
                    }
                }

            //--------------------------------------------------------------------------------------
            // Optional Data, containing XYDataset Data and Metadata describing the XYDataset Data

            dataType = starData.addNewData();

            // Now write the XYDataset DataMetadata
            if ((wrapper.getXYDatasetMetadata() != null)
                && (!wrapper.getXYDatasetMetadata().isEmpty()))
                {
                dataType.setDataMetadataArray(wrapper.getXYDatasetMetadata().toArray(new Metadata[wrapper.getXYDatasetMetadata().size()]));
                }

            // THIS SEEMED TO BE MORE TROUBLE THAN IT WAS WORTH?!
//            else
//                {
//                final List<Metadata> listDefaultXYDatasetDataMetadata;
//
//                // The XYDataset is assumed to contain Date and Time columns
//                listDefaultXYDatasetDataMetadata = MetadataFactory.createMissingDefaultXYDatasetDataMetadata(listMetadata,
//                                                                                                             wrapper.getRawDataChannelCount() + 2,
//                                                                                                             true);
//
//                dataType.setDataMetadataArray(listDefaultXYDatasetDataMetadata.toArray(new Metadata[listDefaultXYDatasetDataMetadata.size()]));
//                }

            //-------------------------------------------------------------------------------------
            // Finally write the XYDataset Data, in the compact form
            // This is the same as RawData, except that the data are written as Strings

            if ((wrapper.getXYDataset() != null)
                && (wrapper.getXYDataset() instanceof TimeSeriesCollection))
                {
                final TimeSeriesCollection collection;

                // There should be a collection of <channelcount> TimeSeries in the Dataset
                collection = (TimeSeriesCollection) wrapper.getXYDataset();

                if ((collection != null)
                    && (collection.getSeriesCount() > 0)
                    && (collection.getSeriesCount() == wrapper.getRawDataChannelCount())
                    && (collection.getSeries() != null)
                    && (collection.getSeries().get(0) != null))
                    {
                    final List listSeries;
                    final int intItemCount;
                    final GregorianCalendar calendar;

                    // Retrieve the TimeSeries as a List
                    listSeries = collection.getSeries();

                    // We assume that all Series contain the same number of DataItems
                    // This is the number of data rows in the Export
                    // We know that Series 0 must exist, so count that one
                    intItemCount = ((TimeSeries)listSeries.get(0)).getItemCount();

                    // A Calendar workspace
                    calendar = new GregorianCalendar();

                    // Generate each Row of the Export
                    for (int intRowCount = 0;
                         intRowCount < intItemCount;
                         intRowCount++)
                        {
                        final RT recordType;
                        final TimeSeriesDataItem itemSeriesZero;
                        final RegularTimePeriod period;

                        // One internal Data format is: <Calendar> <Channel0> <Channel1> <Channel2> ...
                        // which is RecordType in the schema, RT in its short form
                        recordType = dataType.addNewR();

                        // Use TimeSeries 0 to provide the Data and Time for each Row
                        // Get the TimeSeriesDataItem, which is dependent on the TimeZone
                        itemSeriesZero = ((TimeSeries)listSeries.get(0)).getDataItem(intRowCount);
                        period = itemSeriesZero.getPeriod();
                        calendar.setTimeInMillis(period.getStart().getTime());

                        // The Stardata Record format is: <Date> <Time> <Channel0> <Channel1> <Channel2> ...
                        // Remember that all data entries must be Strings
                        recordType.setD(ChronosHelper.toDateString(calendar));
                        recordType.setT(ChronosHelper.toTimeString(calendar));

                        // Step across each TimeSeries (channel) to make up the columns
                        for (int intColumnCount = 0;
                             intColumnCount < wrapper.getRawDataChannelCount();
                             intColumnCount++)
                            {
                            final TimeSeriesDataItem item;
                            final RT.C column;

                            column = recordType.addNewC();
                            item = ((TimeSeries)listSeries.get(intColumnCount)).getDataItem(intRowCount);

                            if (item != null)
                                {
                                // Remember that all data entries must be Strings
                                column.setStringValue(item.getValue().toString());
                                }
                            else
                                {
                                column.setStringValue(QUERY);
                                }
                            }
                        }
                    }
                else
                    {
                    DataTranslatorHelper.addMessage(messages,
                                                    METADATA_TARGET_XYDATASET
                                                       + METADATA_ACTION_WRITE
                                                       + METADATA_RESULT
                                                           + DataTranslatorInterface.ERROR_SERIES_COUNT
                                                           + TERMINATOR);
                    }
                }
            else if ((wrapper.getXYDataset() != null)
                && (wrapper.getXYDataset() instanceof XYSeriesCollection))
                {
                final XYSeriesCollection collection;

                // There should be a collection of <channelcount> XYSeries in the Dataset
                collection = (XYSeriesCollection) wrapper.getXYDataset();

                if ((collection != null)
                    && (collection.getSeriesCount() > 0)
                    && (collection.getSeriesCount() == wrapper.getRawDataChannelCount())
                    && (collection.getSeries() != null)
                    && (collection.getSeries().get(0) != null))
                    {
                    final List listSeries;
                    final int intItemCount;

                    // Retrieve the XYSeries as a List
                    listSeries = collection.getSeries();

                    // We assume that all Series contain the same number of DataItems
                    // This is the number of rows in the Export
                    // We know that Series 0 must exist, so count that one
                    intItemCount = ((XYSeries)listSeries.get(0)).getItemCount();

                    // Generate each Row of the Export
                    // X-value Channel0  Channel1  Channel2 ...
                    for (int intRowCount = 0;
                         intRowCount < intItemCount;
                         intRowCount++)
                        {
                        final XYDataItem itemSeriesZero;
                        final CD columnData;
                        final CD.C columnX;

                        // RecordData:ColumnData
                        columnData = dataType.addNewRD();

                        // Each Series is a set of (x, y) or (X, DataItem)
                        // // The X values should be the same in each Series
                        // Use XYSeries 0 to provide the X-value for each Row
                        // Get the XYDataItem
                        itemSeriesZero = ((XYSeries)listSeries.get(0)).getDataItem(intRowCount);

                        // X-value
                        columnX = columnData.addNewC();
                        columnX.setStringValue(itemSeriesZero.getX().toString());

                        // Step across each XYSeries (channel) to make up the columns
                        for (int intColumnCount = 0;
                             intColumnCount < wrapper.getRawDataChannelCount();
                             intColumnCount++)
                            {
                            final XYDataItem item;
                            final CD.C column;

                            column = columnData.addNewC();

                            // The DataItem is the Channel value
                            item = ((XYSeries)listSeries.get(intColumnCount)).getDataItem(intRowCount);

                            if (item != null)
                                {
                                column.setStringValue(item.getY().toString());
                                }
                            else
                                {
                                column.setStringValue(QUERY);
                                }
                            }
                        }
                    }
                else
                    {
                    DataTranslatorHelper.addMessage(messages,
                                                    METADATA_TARGET_XYDATASET
                                                       + METADATA_ACTION_WRITE
                                                       + METADATA_RESULT
                                                           + DataTranslatorInterface.ERROR_SERIES_COUNT
                                                           + TERMINATOR);
                    }
                }
            else
                {
                DataTranslatorHelper.addMessage(messages,
                                                METADATA_TARGET_XYDATASET
                                                    + METADATA_ACTION_WRITE
                                                    + METADATA_RESULT
                                                        + DataTranslatorInterface.ERROR_INVALID_DATASET
                                                        + TERMINATOR);
                }

            // Write the whole Stardata document (even if empty) to the output stream
            docStarData.save(stream, getXmlOptions(compressed));

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = XmlBeansUtilities.isValidXml(docStarData, LOADER_PROPERTIES.isValidationXML());
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the DAO RawData in Stardata format.
     * All data and metadata come from the DAOWrapper.
     *
     * @param wrapper
     * @param verbose
     * @param stream
     * @param messages
     * @param compressed
     *
     * @return boolean
     */

    public static boolean writeRawData(final DAOWrapperInterface wrapper,
                                       final boolean verbose,
                                       final OutputStream stream,
                                       final List<String> messages,
                                       final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeRawData()";
        boolean boolSuccess;

        boolSuccess = false;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_RAWDATA + METADATA_ACTION_WRITE + METADATA_FORMAT);
        try
            {
            final StardataDocument docStarData;
            final StardataDocument.Stardata starData;
            final HeaderType header;
            final List<Metadata> listMetadata;
            final DataType dataType;
            int intChannelCount;
            boolean boolUseCalendar;

            docStarData = StardataDocument.Factory.newInstance();
            starData = docStarData.addNewStardata();

            //--------------------------------------------------------------------------------------
            // An optional Header, containing Metadata and Metadata describing the Metadata

            header = starData.addNewHeader();

            // Gather all available Metadata, but no MetadataMetadata
            listMetadata =  MetadataHelper.collectMetadataForExportFromWrapper(wrapper, false);

            // Write the Metadata, if any
            if ((listMetadata != null)
                && (!listMetadata.isEmpty()))
                {
                final List<Metadata> listMetadataMetadata;

                header.setMetadataArray(listMetadata.toArray(new Metadata[listMetadata.size()]));

                // Are there any MetadataMetadata?
                listMetadataMetadata = MetadataHelper.collectMetadataMetadata(wrapper.getWrappedDAO().getHostInstrument(),
                                                                              wrapper.getWrappedDAO(),
                                                                              wrapper);
                if ((listMetadataMetadata != null)
                    && (!listMetadataMetadata.isEmpty()))
                    {
                    header.setMetadataMetadataArray(listMetadataMetadata.toArray(new Metadata[listMetadataMetadata.size()]));
                    }
                else
                    {
                    final List<Metadata> listDefaultMetadataMetadata;

                    // If not, create some default MetadataMetadata
                    listDefaultMetadataMetadata = MetadataFactory.createDefaultMetadataMetadata();
                    header.setMetadataMetadataArray(listDefaultMetadataMetadata.toArray(new Metadata[listDefaultMetadataMetadata.size()]));
                    }
                }

            //--------------------------------------------------------------------------------------
            // Find the channel count and the RecordType - Calendarised or just columns

            intChannelCount = 0;
            boolUseCalendar = false;

            if ((wrapper.getRawData() != null)
                && (!wrapper.getRawData().isEmpty())
                && (wrapper.getRawData().get(0) != null))
                {
                if (((Vector)wrapper.getRawData().get(0)).get(0) instanceof Calendar)
                    {
                    // The Data format is: <Calendar> <Channel0> <Channel1> <Channel2> ...
                    // So the channel count is one less than the items in the first Vector
                    intChannelCount = ((Vector)wrapper.getRawData().get(0)).size() - 1;
                    boolUseCalendar = true;
                    }
                else
                    {
                    // The Data format is: <X_value> <Channel0> <Channel1> <Channel2> ...
                    // So the channel count is one less than the size of the first Vector
                    intChannelCount = ((Vector)wrapper.getRawData().get(0)).size() - 1;
                    boolUseCalendar = false;
                    }
                }

            //--------------------------------------------------------------------------------------
            // Optional Data, containing RawData and Metadata describing the RawData

            dataType = starData.addNewData();

            // Now write the RawData MetaData
            if ((wrapper.getRawDataMetadata() != null)
                && (!wrapper.getRawDataMetadata().isEmpty()))
                {
                // Did the DAOWrapper have any RawDataMetadata?
                dataType.setDataMetadataArray(wrapper.getRawDataMetadata().toArray(new Metadata[wrapper.getRawDataMetadata().size()]));
                }

            // THIS SEEMED TO BE MORE TROUBLE THAN IT WAS WORTH?!
//            else
//                {
//                final List<Metadata> listDefaultRawDataMetadata;
//
//                // If not, create some default RawDataMetadata
//                listDefaultRawDataMetadata = MetadataFactory.createMissingDefaultRawDataMetadata(listMetadata, intChannelCount, boolUseCalendar);
//                dataType.setDataMetadataArray(listDefaultRawDataMetadata.toArray(new Metadata[listDefaultRawDataMetadata.size()]));
//                }

            //-------------------------------------------------------------------------------------
            // Finally write the RawData, in the compact form

            if ((wrapper.getRawData() != null)
                && (!wrapper.getRawData().isEmpty())
                && (wrapper.getRawData().get(0) != null))
                {
                final Iterator iterData;

                iterData = wrapper.getRawData().iterator();

                while (iterData.hasNext())
                    {
                    final Vector vecData;

                    vecData = (Vector) iterData.next();

                    if ((vecData != null)
                        && (vecData.get(0) != null))
                        {
                        // One internal Data format is: <Calendar> <Channel0> <Channel1> <Channel2> ...
                        // which is RecordType in the schema, RT in its short form
                        if (vecData.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR) instanceof Calendar)
                            {
                            final RT recordType;
                            final Calendar calendar;

                            // The Stardata Record format is: <Date> <Time> <Channel0> <Channel1> <Channel2> ...
                            recordType = dataType.addNewR();

                            // We are dealing with indexed, timestamped data
                            calendar = (Calendar)vecData.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                            // Remember that all data entries must be Strings
                            recordType.setD(ChronosHelper.toDateString(calendar));
                            recordType.setT(ChronosHelper.toTimeString(calendar));

                            // Add one column for each channel of data
                            for (int intChannelIndex = 0;
                                 ((intChannelCount > 0) && (intChannelIndex < intChannelCount));
                                 intChannelIndex++)
                                {
                                final Object objData;
                                final RT.C column;

                                column = recordType.addNewC();
                                // Step over the Calendar
                                objData = vecData.get(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intChannelIndex);

                                if (objData != null)
                                    {
                                    // Remember that all data entries must be Strings
                                    column.setStringValue(objData.toString());
                                    }
                                else
                                    {
                                    column.setStringValue(QUERY);
                                    }
                                }
                            }
                        else
                            {
                            final CD columnData;

                            // The other internal Data format is: <X_Value> <Channel0> <Channel1> <Channel2> ...
                            // which is ColumnData in the schema, or CD in its short form

                            columnData = dataType.addNewRD();

                            // Add one column for X, and one for each channel of data
                            // so read ChannelCount+1 items from the Vector

                            for (int intChannelIndex = 0;
                                 ((intChannelCount > 0) && (intChannelIndex <= intChannelCount));
                                 intChannelIndex++)
                                {
                                final Object objData;
                                final CD.C column;

                                column = columnData.addNewC();
                                objData = vecData.get(intChannelIndex);

                                if (objData != null)
                                    {
                                    // Remember that all data entries must be Strings
                                    column.setStringValue(objData.toString());
                                    }
                                else
                                    {
                                    column.setStringValue(QUERY);
                                    }
                                }
                            }
                        }
                    }
                }

            // Write the whole Stardata document (even if empty) to the output stream
            docStarData.save(stream, getXmlOptions(compressed));

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = XmlBeansUtilities.isValidXml(docStarData, LOADER_PROPERTIES.isValidationXML());
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write an InstrumentLog or EventLog in Stardata format.
     * The InstrumentLog or EventLog is assumed to have an extra 'hidden' index column,
     * used for sorting, and not included in the logcolumns counter.
     * There are no general Metadata or MetadataMetadata for the EventLogs.
     *
     * @param logdatametadata
     * @param datasettype
     * @param logdata
     * @param logcolumns
     * @param verbose
     * @param stream
     * @param messages
     * @param compressed
     *
     * @return boolean
     */

    public static boolean writeLog(final List<Metadata> logdatametadata,
                                   final DatasetType datasettype,
                                   final Vector<Vector> logdata,
                                   final int logcolumns,
                                   final boolean verbose,
                                   final OutputStream stream,
                                   final List<String> messages,
                                   final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeLog()";
        boolean boolSuccess;
        final int intCount;

        boolSuccess = false;

        if (logdata != null)
            {
            intCount = logdata.size();
            }
        else
            {
            intCount = 0;
            }

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_LOG
                                            + METADATA_ACTION_WRITE
                                            + METADATA_FORMAT + SPACE
                                            + METADATA_WIDTH + logcolumns + TERMINATOR + SPACE
                                            + METADATA_COUNT + intCount + TERMINATOR);
        try
            {
            final StardataDocument docStarData;
            final StardataDocument.Stardata starData;
            final DataType dataType;

            docStarData = StardataDocument.Factory.newInstance();
            starData = docStarData.addNewStardata();

            //--------------------------------------------------------------------------------------
            // There are no general Metadata or MetadataMetadata for the EventLog

            //--------------------------------------------------------------------------------------
            // Optional Data, containing Data and Metadata describing the Data

            dataType = starData.addNewData();

            // Write the Log DataMetadata
            if ((logdatametadata != null)
                && (!logdatametadata.isEmpty()))
                {
                dataType.setDataMetadataArray(logdatametadata.toArray(new Metadata[logdatametadata.size()]));
                }
            else
                {
                final List<Metadata> listDefaultTableDataMetadata;

                listDefaultTableDataMetadata = MetadataFactory.createDefaultReportTableMetadata(datasettype,
                                                                                                logcolumns);

                dataType.setDataMetadataArray(listDefaultTableDataMetadata.toArray(new Metadata[listDefaultTableDataMetadata.size()]));
                }

            //--------------------------------------------------------------------------------------
            // Write the EventLog, in the compact form

            if ((logdata != null)
                && (!logdata.isEmpty()))
                {
                final Iterator iterEvents;

                iterEvents = logdata.iterator();

                while (iterEvents.hasNext())
                    {
                    final CD columnData;
                    final Vector vecEvent;

                    // The EventLogs may have different columns,
                    // so we can't rely on there being Date & Time in the first two columns
                    // Use the Stardata Record format of: <Column0> <Column1> <Column2> ...
                    columnData = dataType.addNewRD();

                    // The internal Data format is a Vector of logcolumns Objects,
                    // some of which may be Strings, others may be ReportIcons, or Booleans
                    vecEvent = (Vector) iterEvents.next();

                    // Check for invalid Vectors of incorrect width
                    // Remember that the Log presented by the Instrument
                    // contains the extra last index column
                    if ((vecEvent != null)
                        && (vecEvent.size() == (logcolumns + 1)))
                        {
                        // Handle all columns; we don't know in advance what they will contain
                        // except that we don't need the index column, so use only logcolumns
                        for (int i = 0;
                             i < logcolumns;
                             i++)
                            {
                            final Object objItem;
                            final CD.C column;

                            objItem = vecEvent.get(i);
                            column = columnData.addNewC();

                            // Render all known types of content
                            // Remember that all column values must be Strings
                            if ((objItem == null)
                                || (EMPTY_STRING.equals(objItem)))
                                {
                                column.setStringValue(SPACE);
                                }
                            else if (objItem instanceof String)
                                {
                                column.setStringValue(objItem.toString());
                                }
                            else if (objItem instanceof Boolean)
                                {
                                column.setStringValue(objItem.toString());
                                }
                            else if (objItem instanceof ReportIcon)
                                {
                                // One day this might be a Mnemonic representing the state
                                column.setStringValue(QUERY);
                                }
                            else
                                {
                                // The data format is not recognised
                                column.setStringValue(QUERY + QUERY);
                                }
                            }
                        }
                    }
                }

            // Write the whole Stardata document (even if empty) to the output stream
            docStarData.save(stream, getXmlOptions(compressed));

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = XmlBeansUtilities.isValidXml(docStarData, LOADER_PROPERTIES.isValidationXML());
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the Metadata in Stardata format.
     *
     * @param metadatametadata
     * @param metadata
     * @param verbose
     * @param stream
     * @param messages
     * @param compressed
     *
     * @return boolean
     */

    public static boolean writeMetadata(final List<Metadata> metadatametadata,
                                        final List<Metadata> metadata,
                                        final boolean verbose,
                                        final OutputStream stream,
                                        final List<String> messages,
                                        final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeMetadata()";
        boolean boolSuccess;
        final int intCount;

        boolSuccess = false;

        if (metadata != null)
            {
            intCount = metadata.size();
            }
        else
            {
            intCount = 0;
            }

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_METADATA
                                            + METADATA_ACTION_WRITE
                                            + METADATA_FORMAT + SPACE
                                            + METADATA_COUNT + intCount + TERMINATOR);
        try
            {
            final StardataDocument docStarData;
            final StardataDocument.Stardata starData;
            final HeaderType header;

            docStarData = StardataDocument.Factory.newInstance();
            starData = docStarData.addNewStardata();

            //--------------------------------------------------------------------------------------
            // An optional Header, containing Metadata and Metadata describing the Metadata

            header = starData.addNewHeader();

            // Write the Metadata, if any
            if ((metadata != null)
                && (!metadata.isEmpty()))
                {
                header.setMetadataArray(metadata.toArray(new Metadata[metadata.size()]));

                // Write the MetadataMetadata, if any
                if ((metadatametadata != null)
                    && (!metadatametadata.isEmpty()))
                    {
                    header.setMetadataMetadataArray(metadatametadata.toArray(new Metadata[metadatametadata.size()]));
                    }
                else
                    {
                    final List<Metadata> listDefaultMetadataMetadata;

                    // If not, create some default MetadataMetadata
                    listDefaultMetadataMetadata = MetadataFactory.createDefaultMetadataMetadata();
                    header.setMetadataMetadataArray(listDefaultMetadataMetadata.toArray(new Metadata[listDefaultMetadataMetadata.size()]));
                    }
                }

            //--------------------------------------------------------------------------------------
            // There is no Data section for Metadata

            // Write the whole Stardata document (even if empty) to the output stream
            docStarData.save(stream, getXmlOptions(compressed));

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = XmlBeansUtilities.isValidXml(docStarData, LOADER_PROPERTIES.isValidationXML());
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the Instrument Configuration in Stardata format.
     *
     * @param metadatametadata
     * @param metadata
     * @param configdatametadata
     * @param configdata
     * @param verbose
     * @param stream
     * @param messages
     * @param compressed
     *
     * @return boolean
     */

    public static boolean writeConfiguration(final List<Metadata> metadatametadata,
                                             final List<Metadata> metadata,
                                             final List<Metadata> configdatametadata,
                                             final Vector<Vector> configdata,
                                             final boolean verbose,
                                             final OutputStream stream,
                                             final List<String> messages,
                                             final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeConfiguration()";
        boolean boolSuccess;
        final int intCount;

        boolSuccess = false;

        if (configdata != null)
            {
            intCount = configdata.size();
            }
        else
            {
            intCount = 0;
            }

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_CONFIGURATION
                                            + METADATA_ACTION_WRITE
                                            + METADATA_FORMAT + SPACE
                                            + METADATA_COUNT + intCount + TERMINATOR);
        try
            {
            final int INDEX_ICON = 0;
            final int INDEX_UPDATED = 1;
            final int INDEX_KEY = 2;
            final int INDEX_VALUE = 3;
            final int COLUMN_COUNT = 3;
            final StardataDocument docStarData;
            final StardataDocument.Stardata starData;
            final HeaderType header;
            final DataType dataType;

            docStarData = StardataDocument.Factory.newInstance();
            starData = docStarData.addNewStardata();

            //--------------------------------------------------------------------------------------
            // An optional Header, containing Metadata and Metadata describing the Metadata

            header = starData.addNewHeader();

            // Write the Configuration Metadata, if any
            if ((metadata != null)
                && (!metadata.isEmpty()))
                {
                header.setMetadataArray(metadata.toArray(new Metadata[metadata.size()]));

                // Write the Configuration MetadataMetadata, if any
                if ((metadatametadata != null)
                    && (!metadatametadata.isEmpty()))
                    {
                    header.setMetadataMetadataArray(metadatametadata.toArray(new Metadata[metadatametadata.size()]));
                    }
                else
                    {
                    final List<Metadata> listDefaultMetadataMetadata;

                    // If not, create some default MetadataMetadata
                    listDefaultMetadataMetadata = MetadataFactory.createDefaultMetadataMetadata();
                    header.setMetadataMetadataArray(listDefaultMetadataMetadata.toArray(new Metadata[listDefaultMetadataMetadata.size()]));
                    }
                }

            //--------------------------------------------------------------------------------------
            // Optional Data, containing Data and Metadata describing the Data

            dataType = starData.addNewData();

            // Write the Configuration DataMetadata
            if ((configdatametadata != null)
                && (!configdatametadata.isEmpty()))
                {
                dataType.setDataMetadataArray(configdatametadata.toArray(new Metadata[configdatametadata.size()]));
                }
            else
                {
                final List<Metadata> listDefaultConfigurationDataMetadata;

                // If not, create some default MetadataMetadata
                listDefaultConfigurationDataMetadata = MetadataFactory.createDefaultConfigurationDataMetadata(COLUMN_COUNT);

                dataType.setDataMetadataArray(listDefaultConfigurationDataMetadata.toArray(new Metadata[listDefaultConfigurationDataMetadata.size()]));
                }

            //--------------------------------------------------------------------------------------
            // Finally write the Configuration Data, in the compact form

            if ((configdata != null)
                && (!configdata.isEmpty()))
                {
                final Iterator iterData;

                // The Data format is: <Icon>  <Updated>  <Property>  <Value>
                iterData = configdata.iterator();

                while (iterData.hasNext())
                    {
                    final CD columnType;
                    final Vector vecConfigItem;

                    // The Stardata Record format is: <Property> <Value> <Updated> ...
                    columnType = dataType.addNewRD();

                    // The internal Data format is: <Icon>  <Updated>  <Property>  <Value> ...
                    vecConfigItem = (Vector) iterData.next();

                    if ((vecConfigItem != null)
                        && (vecConfigItem.size() == COLUMN_COUNT+1))
                        {
                        if (vecConfigItem.get(INDEX_ICON) instanceof ReportIcon)
                            {
                            final CD.C columnKey;
                            final CD.C columnValue;
                            final CD.C columnUpdated;
                            Object objItem;

                            // Remember that all data entries must be Strings
                            columnKey = columnType.addNewC();
                            objItem = vecConfigItem.get(INDEX_KEY);

                            if (objItem != null)
                                {
                                // Remember that all data entries must be Strings
                                columnKey.setStringValue(objItem.toString());
                                }
                            else
                                {
                                columnKey.setStringValue(QUERY);
                                }

                            columnValue = columnType.addNewC();
                            objItem = vecConfigItem.get(INDEX_VALUE);

                            if (objItem != null)
                                {
                                // Remember that all data entries must be Strings
                                columnValue.setStringValue(objItem.toString());
                                }
                            else
                                {
                                columnValue.setStringValue(QUERY);
                                }

                            columnUpdated = columnType.addNewC();
                            objItem = vecConfigItem.get(INDEX_UPDATED);

                            if ((objItem != null)
                                && (objItem instanceof Boolean))
                                {
                                // Remember that all data entries must be Strings
                                columnUpdated.setStringValue(objItem.toString());
                                }
                            else
                                {
                                columnUpdated.setStringValue(QUERY);
                                }
                            }
                        else
                            {
                            // The data format is not recognised
                            // Add one column for each Key, Value, Updated
                            for (int i = 0;
                                 i < COLUMN_COUNT;
                                 i++)
                                {
                                final CD.C column;

                                column = columnType.addNewC();
                                column.setStringValue(QUERY);
                                }
                            }
                        }
                    }
                }

            // Write the whole Stardata document (even if empty) to the output stream
            docStarData.save(stream, getXmlOptions(compressed));

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = XmlBeansUtilities.isValidXml(docStarData, LOADER_PROPERTIES.isValidationXML());
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the data from a general ReportTable in Stardata format.
     * Optional extra metadata may be added to the export.
     *
     * @param metadatametadata
     * @param metadata
     * @param infercolumnmetadata
     * @param datasettype
     * @param report
     * @param verbose
     * @param stream
     * @param messages
     * @param compressed
     *
     * @return boolean
     */

    public static boolean writeReportTable(final List<Metadata> metadatametadata,
                                           final List<Metadata> metadata,
                                           final boolean infercolumnmetadata,
                                           final DatasetType datasettype,
                                           final ReportTablePlugin report,
                                           final boolean verbose,
                                           final OutputStream stream,
                                           final List<String> messages,
                                           final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeReportTable()";
        boolean boolSuccess;

        boolSuccess = false;

//        DataTranslatorHelper.addMessage(messages,
//                                        METADATA_TARGET_REPORT
//                                            + METADATA_ACTION_WRITE
//                                            + METADATA_FORMAT);
        try
            {
            final StardataDocument docStarData;
            final StardataDocument.Stardata starData;
            final HeaderType header;
            final DataType dataType;
            final int intColumnCount;
            final Vector<Vector> vecRawReport;

            docStarData = StardataDocument.Factory.newInstance();
            starData = docStarData.addNewStardata();

            // Find how many columns to write
            if (report.getReportTableModel() != null)
                {
                intColumnCount = report.getReportTableModel().getColumnCount();
                }
            else
                {
                intColumnCount = 0;
                }

            //--------------------------------------------------------------------------------------
            // An optional Header, containing Metadata and Metadata describing the Metadata

            header = starData.addNewHeader();

            // Write the MetadataMetadata, if any
            if ((metadatametadata != null)
                && (!metadatametadata.isEmpty()))
                {
                header.setMetadataMetadataArray(metadatametadata.toArray(new Metadata[metadatametadata.size()]));
                }
//            else
//                {
//                final List<Metadata> listDefaultMetadataMetadata;
//
//                // If not, create some default MetadataMetadata
//                listDefaultMetadataMetadata = MetadataFactory.createDefaultMetadataMetadata();
//                header.setMetadataMetadataArray(listDefaultMetadataMetadata.toArray(new Metadata[listDefaultMetadataMetadata.size()]));
//                }

            // Write the Metadata, if any
            if ((metadata != null)
                && (!metadata.isEmpty()))
                {
                header.setMetadataArray(metadata.toArray(new Metadata[metadata.size()]));
                }

            //--------------------------------------------------------------------------------------
            // Optional Data, containing Data and Metadata describing the Data

            dataType = starData.addNewData();

            // Derive the Report column metadata from the ReportTableModel if possible
            if (infercolumnmetadata)
                {
                final List<Metadata> listDataMetadata;

                // Create the Report metadata
                listDataMetadata = MetadataFactory.deriveReportColumnMetadata(datasettype, report);

                // Write the ReportTable DataMetadata
                if ((listDataMetadata != null)
                    && (!listDataMetadata.isEmpty()))
                    {
                    dataType.setDataMetadataArray(listDataMetadata.toArray(new Metadata[listDataMetadata.size()]));
                    }
                else
                    {
                    final List<Metadata> listDefaultMetadata;

                    listDefaultMetadata = MetadataFactory.createDefaultReportTableMetadata(datasettype,
                                                                                           report.getReportTableModel().getColumnCount());

                    // This should never occur!
                    dataType.setDataMetadataArray(listDefaultMetadata.toArray(new Metadata[listDefaultMetadata.size()]));
                    }
                }

            //--------------------------------------------------------------------------------------
            // Write the ReportTable RawReport Data

            vecRawReport = report.generateRawReport();

            if ((vecRawReport != null)
                && (!vecRawReport.isEmpty())
                && (intColumnCount > 0))
                {
                // Determine if we can use the timestamped form of Stardata
                // Allow room for Date, Time, Column
                if ((intColumnCount > 3)
                    && (SchemaDataType.DATE.equals(report.defineColumns().get(INDEX_REPORT_COLUMN_DATE).getDataType()))
                    && (SchemaDataType.TIME.equals(report.defineColumns().get(INDEX_REPORT_COLUMN_TIME).getDataType())))
                    {
                    writeTimestampedData(vecRawReport,
                                         dataType,
                                         intColumnCount,
                                         compressed);
                    }
                else
                    {
                    writeColumnarData(vecRawReport,
                                      dataType,
                                      intColumnCount,
                                      compressed);
                    }
                }

            // Write the whole Stardata document (even if empty) to the output stream
            docStarData.save(stream, getXmlOptions(compressed));

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = XmlBeansUtilities.isValidXml(docStarData, LOADER_PROPERTIES.isValidationXML());
            }

        catch (IOException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + SPACE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                                    + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write ReportData with timestamps.
     * We know that columncount > 3.
     *
     * @param data
     * @param datatype
     * @param columncount
     * @param compressed
     */

    private static void writeTimestampedData(final Vector<Vector> data,
                                             final DataType datatype,
                                             final int columncount,
                                             final boolean compressed)
        {
        final Iterator<Vector> iterRows;

        iterRows = data.iterator();

        while (iterRows.hasNext())
            {
            final RT recordType;
            final Vector vecRow;

            // Use the Stardata Record format of: <Date> <Time> <Column0> ...
            recordType = datatype.addNewR();

            // The internal Data format is a Vector of Objects,
            // some of which may be Strings, others may be ReportIcons, or Booleans
            vecRow = iterRows.next();

            // Remember that the ReportTable presented by the Instrument
            // may contain an extra last index column
            if (vecRow != null)
                {
                // Handle all columns; we don't know in advance what they will contain
                // except that we don't need the (optional) index column, so use only columncount
                for (int i = 0;
                     i < columncount;
                     i++)
                    {
                    final Object objItem;

                    objItem = vecRow.get(i);

                    // Add the Date from column 0...
                    if (i == INDEX_REPORT_COLUMN_DATE)
                        {
                        recordType.setD(objItem.toString());
                        }
                    // ...and the Time from column 1
                    else if (i == INDEX_REPORT_COLUMN_TIME)
                        {
                        recordType.setT(objItem.toString());
                        }
                    else
                        {
                        final RT.C column;

                        column = recordType.addNewC();

                        // Render all known types of content
                        // Remember that all column values must be Strings
                        if ((objItem == null)
                            || (EMPTY_STRING.equals(objItem)))
                            {
                            column.setStringValue(SPACE);
                            }
                        else if (objItem instanceof String)
                            {
                            // Remove any HTML tags
                            column.setStringValue(Utilities.stripHTML(objItem.toString()));
                            }
                        else if (objItem instanceof Boolean)
                            {
                            column.setStringValue(objItem.toString());
                            }
                        else if (objItem instanceof ReportIcon)
                            {
                            // One day this might be a Mnemonic representing the state
                            column.setStringValue(QUERY);
                            }
                        else
                            {
                            // The data format is not recognised
                            column.setStringValue(QUERY + QUERY);
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Write ReportData as simple columns.
     *
     * @param data
     * @param datatype
     * @param columncount
     * @param compressed
     */

    private static void writeColumnarData(final Vector<Vector> data,
                                          final DataType datatype,
                                          final int columncount,
                                          final boolean compressed)
        {
        final String SOURCE = "StardataTranslatorWriters.writeColumnarData()";
        final Iterator<Vector> iterRows;

        iterRows = data.iterator();

        while (iterRows.hasNext())
            {
            final CD columnData;
            final Vector vecRow;

            // We can't rely on there being Date & Time in the first two columns
            // Use the Stardata Record format of: <Column0> <Column1> <Column2> ...
            columnData = datatype.addNewRD();

            // The internal Data format is a Vector of Objects,
            // some of which may be Strings, others may be ReportIcons, or Booleans
            vecRow = iterRows.next();

            // Remember that the ReportTable presented by the Instrument
            // may contain an extra last index column
            if (vecRow != null)
                {
                // Handle all columns; we don't know in advance what they will contain
                // except that we don't need the (optional) index column, so use only columncount
                for (int i = 0;
                     i < columncount;
                     i++)
                    {
                    try
                        {
                        final Object objItem;
                        final CD.C column;

                        objItem = vecRow.get(i);
                        column = columnData.addNewC();

                        // Render all known types of content
                        // Remember that all column values must be Strings
                        if ((objItem == null)
                            || (EMPTY_STRING.equals(objItem)))
                            {
                            column.setStringValue(SPACE);
                            }
                        else if (objItem instanceof String)
                            {
                            // Remove any HTML tags
                            column.setStringValue(Utilities.stripHTML(objItem.toString()));
                            }
                        else if (objItem instanceof Boolean)
                            {
                            column.setStringValue(objItem.toString());
                            }
                        else if (objItem instanceof ReportIcon)
                            {
                            // One day this might be a Mnemonic representing the state
                            column.setStringValue(QUERY);
                            }
                        else
                            {
                            // The data format is not recognised
                            column.setStringValue(QUERY + QUERY);
                            }
                        }

                    catch (ArrayIndexOutOfBoundsException exception)
                        {
                        LOGGER.error(SOURCE + "--> ArrayIndexOutOfBoundsException columncount=" + columncount + " [i=" + i + "]");
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the XmlOptions to use for the export.
     *
     * @param compressed
     *
     * @return XmlOptions
     */

    private static XmlOptions getXmlOptions(final boolean compressed)
        {
        final XmlOptions xmlOptions;

        xmlOptions = new XmlOptions();

        // If this option is set, the saver will try to use
        // the default namespace for the most commonly used URI.
        // If it is not set the saver will always created named prefixes.
        xmlOptions.setUseDefaultNamespace();

        // Causes the saver to reduce the number of namespace prefix declarations.
        // The saver will do this by passing over the document twice,
        // first to collect the set of needed namespace declarations,
        // and then second to actually save the document with the declarations collected at the root.
        xmlOptions.setSaveAggressiveNamespaces();

        // Reformat sensibly if not compressed
        if (!compressed)
            {
            // This option will cause the saver to reformat white space for easier reading
            xmlOptions.setSavePrettyPrint();
            xmlOptions.setSavePrettyPrintIndent(XML_INDENT);
            }

        // This option controls whether saving begins on the element or its contents
        xmlOptions.setSaveOuter();

        return (xmlOptions);
        }
    }

//System.out.println("STARDATA_FORMATTED=[" + docStarData.xmlText() + "]");

// BEWARE! This does not write single spaces in the data!
//System.out.println("STARDATA_FORMATTED=[" + docStarData.toString() + "]");

// BEWARE! This does not write single spaces in the data!
// stream.write(docStarData.toString().getBytes());

