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

package org.lmn.fc.common.datatranslators.csv;

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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


/***************************************************************************************************
 * CsvTranslatorWriters.
 *
 * See: http://en.wikipedia.org/wiki/Comma-separated_values
 */

public final class CsvTranslatorWriters implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   FrameworkRegex,
                                                   ResourceKeys
    {
    private static final String METADATA_FORMAT = "[format=csv]";

    private static final int INDEX_REPORT_COLUMN_DATE = 0;
    private static final int INDEX_REPORT_COLUMN_TIME = 1;


    /***********************************************************************************************
     * Write the DAO XYDataset in CSV format.
     * All data and metadata come from the DAOWrapper.
     *
     * @param wrapper
     * @param verbose
     * @param stream
     * @param messages
     *
     * @return boolean
     */

    public static boolean writeXYDataset(final DAOWrapperInterface wrapper,
                                         final boolean verbose,
                                         final OutputStream stream,
                                         final List<String> messages)
        {
        final String SOURCE = "CsvTranslatorWriters.writeXYDataset()";
        boolean boolSuccess;

        boolSuccess = false;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_XYDATASET + METADATA_ACTION_WRITE + METADATA_FORMAT);

        try
            {
            final List<Metadata> listMetadata;
            final List<String> listKeys;

            //--------------------------------------------------------------------------------------
            // Gather all available Metadata and MetadataMetadata

            listMetadata =  MetadataHelper.collectMetadataForExportFromWrapper(wrapper, true);

            // Read and sort all of the Metadata Keys
            listKeys = MetadataHelper.sortMetadataByKeys(listMetadata);

            // Write the sorted Metadata
            DataTranslatorHelper.writeMetadata(stream,
                                               listMetadata,
                                               listKeys,
                                               DataTranslatorHelper.COMMA);

            //--------------------------------------------------------------------------------------
            // Now write the XYDataset, if any

            if ((wrapper.getXYDataset() != null)
                && (wrapper.getXYDataset().getSeriesCount() > 0))
                {
                if (wrapper.getXYDataset() instanceof XYSeriesCollection)
                    {
                    final XYSeriesCollection collection;

                    // There should be a collection of <channelcount> XYSeries in the Dataset
                    collection = (XYSeriesCollection) wrapper.getXYDataset();

                    if ((collection != null)
                        && (collection.getSeriesCount() > 0)
                        && (collection.getSeriesCount() == wrapper.getRawDataChannelCount())
                        && (collection.getSeries() != null))
                        {
                        final List listSeries;
                        final int intItemCount;

                        // Retrieve the XYSeries as a List
                        listSeries = collection.getSeries();

                        // We assume that all Series contain the same number of DataItems
                        // This is the number of rows in the Report
                        // We know that Series 0 must exist, so count that one
                        intItemCount = ((XYSeries)listSeries.get(0)).getItemCount();

                        // Generate each Row of the Export
                        // Index X-value Channel0  Channel1  Channel2 ...
                        for (int intRowCount = 0;
                             intRowCount < intItemCount;
                             intRowCount++)
                            {
                            final XYDataItem itemSeriesZero;

                            // Use XYSeries 0 to provide the X-value for each Row
                            // Get the XYDataItem
                            itemSeriesZero = ((XYSeries)listSeries.get(0)).getDataItem(intRowCount);

                            // Remember that all data entries must be Strings
                            // X-value
                            stream.write(itemSeriesZero.getX().toString().getBytes());
                            stream.write(DataTranslatorHelper.COMMA);

                            // Step across each XYSeries (channel) to make up the columns
                            for (int intColumnCount = 0;
                                 intColumnCount < wrapper.getRawDataChannelCount();
                                 intColumnCount++)
                                {
                                final XYDataItem item;

                                // Comma Separator
                                if (intColumnCount > 0)
                                    {
                                    stream.write(DataTranslatorHelper.COMMA);
                                    }

                                item = ((XYSeries)listSeries.get(intColumnCount)).getDataItem(intRowCount);

                                if (item != null)
                                    {
                                    stream.write(item.getY().toString().getBytes());
                                    }
                                else
                                    {
                                    stream.write(QUERY.getBytes());
                                    }
                                }

                            stream.write(DataTranslatorHelper.CR_LF.getBytes());
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
                else if (wrapper.getXYDataset() instanceof TimeSeriesCollection)
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
                            final TimeSeriesDataItem itemSeriesZero;
                            final RegularTimePeriod period;

                            // Use TimeSeries 0 to provide the Data and Time for each Row
                            // Get the TimeSeriesDataItem, which is dependent on the TimeZone
                            itemSeriesZero = ((TimeSeries)listSeries.get(0)).getDataItem(intRowCount);
                            period = itemSeriesZero.getPeriod();
                            calendar.setTimeInMillis(period.getStart().getTime());

                            // The Record format is: <Date> <Time> <Channel0> <Channel1> <Channel2> ...
                            // Remember that all data entries must be Strings
                            stream.write(ChronosHelper.toDateString(calendar).getBytes());
                            stream.write(DataTranslatorHelper.COMMA);
                            stream.write(ChronosHelper.toTimeString(calendar).getBytes());
                            stream.write(DataTranslatorHelper.COMMA);

                            // Step across each TimeSeries (channel) to make up the columns
                            for (int intColumnCount = 0;
                                 intColumnCount < wrapper.getRawDataChannelCount();
                                 intColumnCount++)
                                {
                                final TimeSeriesDataItem item;

                                // Comma Separator
                                if (intColumnCount > 0)
                                    {
                                    stream.write(DataTranslatorHelper.COMMA);
                                    }

                                item = ((TimeSeries)listSeries.get(intColumnCount)).getDataItem(intRowCount);

                                if (item != null)
                                    {
                                    // Remember that all data entries must be Strings
                                    stream.write(item.getValue().toString().getBytes());
                                    }
                                else
                                    {
                                    stream.write(QUERY.getBytes());
                                    }
                                }

                            stream.write(DataTranslatorHelper.CR_LF.getBytes());
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
                                                            + DataTranslatorInterface.ERROR_INVALID_DATASET_TYPE
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

            // Tidy up
            stream.flush();
            stream.close();

            // Say it worked, but only if we don't throw an IOException first
            boolSuccess = true;
            }

        catch (IOException exception)
            {
            DataTranslatorHelper.addMessage(messages,
                                            METADATA_TARGET_CSV
                                                + METADATA_ACTION_EXPORT
                                                + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the DAO RawData in CSV format.
     * All data and metadata come from the DAOWrapper.
     *
     * @param wrapper
     * @param verbose
     * @param stream
     * @param messages
     *
     * @return boolean
     */

    public static boolean writeRawData(final DAOWrapperInterface wrapper,
                                       final boolean verbose,
                                       final OutputStream stream,
                                       final List<String> messages)
        {
        final String SOURCE = "CsvTranslatorWriters.writeRawData() ";
        boolean boolSuccess;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_RAWDATA + METADATA_ACTION_WRITE + METADATA_FORMAT);
        boolSuccess = false;

        try
            {
            final List<Metadata> listMetadata;
            final List<String> listKeys;

            //--------------------------------------------------------------------------------------
            // Gather all available Metadata and MetadataMetadata

            listMetadata =  MetadataHelper.collectMetadataForExportFromWrapper(wrapper, true);

            // Read and sort all of the Metadata Keys
            listKeys = MetadataHelper.sortMetadataByKeys(listMetadata);

            // Write the sorted Metadata
            DataTranslatorHelper.writeMetadata(stream,
                                               listMetadata,
                                               listKeys,
                                               DataTranslatorHelper.COMMA);

            //-------------------------------------------------------------------------------------
            // Now write the RawData, if any

            if ((wrapper.getRawData() != null)
                && (!wrapper.getRawData().isEmpty())
                && (wrapper.getRawData().get(0) != null))
                {
                final Iterator iterRows;

                iterRows = wrapper.getRawData().iterator();

                while (iterRows.hasNext())
                    {
                    final Vector vecData;

                    // The internal Data format is a Vector of Objects
                    vecData = (Vector) iterRows.next();

                    if ((vecData != null)
                        && (vecData.get(0) != null))
                        {
                        // Timestamped
                        // One internal Data format is: <Calendar> <Channel0> <Channel1> <Channel2> ...
                        if (vecData.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR) instanceof Calendar)
                            {
                            final Calendar calendar;

                            // The Record format is: <Date> <Time> <Channel0> <Channel1> <Channel2> ...
                            // We are dealing with indexed, timestamped data
                            calendar = (Calendar)vecData.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                            stream.write(ChronosHelper.toDateString(calendar).getBytes());
                            stream.write(DataTranslatorHelper.COMMA);
                            stream.write(ChronosHelper.toTimeString(calendar).getBytes());
                            stream.write(DataTranslatorHelper.COMMA);

                            // Add one column for each channel of data
                            // Handle all columns; we don't know in advance what they will contain
                            // ChannelIndex {1...ChannelCount}

                            for (int intChannelIndex = 0;
                                 ((wrapper.getRawDataChannelCount() > 0) && (intChannelIndex < wrapper.getRawDataChannelCount()));
                                 intChannelIndex++)
                                {
                                final Object objData;

                                // Comma Separator
                                if (intChannelIndex > 0)
                                    {
                                    stream.write(DataTranslatorHelper.COMMA);
                                    }

                                objData = vecData.get(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intChannelIndex);
                                DataTranslatorHelper.writeCsvDataItem(stream, objData);
                                }

                            stream.write(DataTranslatorHelper.CR_LF.getBytes());
                            }
                        else
                            {
                            // Indexed
                            // The other internal Data format is: <X_Value> <Channel0> <Channel1> <Channel2> ...
                            // Add one column for X, and one for each channel of data
                            // Handle all columns; we don't know in advance what they will contain

                            // X-value
                            if (vecData.get(DataTranslatorInterface.INDEX_INDEXED_X_VALUE) != null)
                                {
                                stream.write(vecData.get(DataTranslatorInterface.INDEX_INDEXED_X_VALUE).toString().getBytes());
                                stream.write(DataTranslatorHelper.COMMA);
                                }
                            else
                                {
                                // The X-Value is unknown
                                stream.write(DataTranslatorInterface.DATATYPE_UNKNOWN.getBytes());
                                stream.write(DataTranslatorHelper.COMMA);
                                }

                            // The remainder of the columns in the Vector are the Data channels
                            // ChannelIndex {1...ChannelCount}

                            for (int intChannelIndex = 0;
                                 ((wrapper.getRawDataChannelCount() > 0) && (intChannelIndex < wrapper.getRawDataChannelCount()));
                                 intChannelIndex++)
                                {
                                final Object objData;

                                // Comma Separator
                                if (intChannelIndex > 0)
                                    {
                                    stream.write(DataTranslatorHelper.COMMA);
                                    }

                                objData = vecData.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex);
                                DataTranslatorHelper.writeCsvDataItem(stream, objData);
                                }

                            stream.write(DataTranslatorHelper.CR_LF.getBytes());
                            }
                        }
                    else
                        {
                        // No data!
                        DataTranslatorHelper.addMessage(messages,
                                                        METADATA_TARGET_CSV
                                                            + METADATA_ACTION_EXPORT
                                                            + METADATA_RESULT + DataTranslatorInterface.ERROR_NO_DATA + TERMINATOR);
                        }
                    }

                // Tidy up
                stream.flush();
                stream.close();

                boolSuccess = true;
                }
            }

        catch (IOException exception)
            {
            DataTranslatorHelper.addMessage(messages,
                                            METADATA_TARGET_CSV
                                                + METADATA_ACTION_EXPORT
                                                + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write an InstrumentLog or EventLog in CSV format.
     * The InstrumentLog or EventLog is assumed to have an extra 'hidden' index column,
     * used for sorting, and not included in the logcolumns counter.
     * There are no general Metadata or MetadataMetadata for the EventLogs.
     *
     * @param logdatametadata
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
                                   final Vector<Vector> logdata,
                                   final int logcolumns,
                                   final boolean verbose,
                                   final OutputStream stream,
                                   final List<String> messages,
                                   final boolean compressed)
        {
        final String SOURCE = "CsvTranslatorWriters.writeLog()";
        final boolean boolSuccess;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_LOG + METADATA_ACTION_WRITE + METADATA_FORMAT);
        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the Instrument Metadata in CSV format.
     *
     * @param metadatametadata
     * @param metadata
     * @param verbose
     * @param stream
     * @param messages
     *
     * @return boolean
     */

    public static boolean writeMetadata(final List<Metadata> metadatametadata,
                                        final List<Metadata> metadata,
                                        final boolean verbose,
                                        final OutputStream stream,
                                        final List<String> messages)
        {
        final String SOURCE = "CsvTranslatorWriters.writeMetadata()";
        boolean boolSuccess;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_METADATA + METADATA_ACTION_WRITE + METADATA_FORMAT);
        boolSuccess = false;

        try
            {
            final List<Metadata> listMetadata;

            listMetadata = new ArrayList<Metadata>(100);

            // Is there any Metadata?
            if ((metadata != null)
                && (!metadata.isEmpty()))
                {
                listMetadata.addAll(metadata);

                // Is there any MetadataMetadata?
                if ((metadatametadata != null)
                    && (!metadatametadata.isEmpty()))
                    {
                    listMetadata.addAll(metadatametadata);
                    }
                else
                    {
                    // If not, create some default MetadataMetadata
                    listMetadata.addAll(MetadataFactory.createDefaultMetadataMetadata());
                    }
                }

            // Read all of the Metadata Keys
            if (!listMetadata.isEmpty())
                {
                final List<String> listSortedKeys;

                listSortedKeys = MetadataHelper.sortMetadataByKey(listMetadata);

                // Write the sorted Metadata
                DataTranslatorHelper.writeMetadata(stream,
                                                   listMetadata,
                                                   listSortedKeys,
                                                   DataTranslatorHelper.COMMA);
                // Tidy up
                stream.flush();
                stream.close();

                boolSuccess = true;
                }
            }

        catch (IOException exception)
            {
            DataTranslatorHelper.addMessage(messages,
                                            METADATA_TARGET_CSV
                                                + METADATA_ACTION_EXPORT
                                                + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the Instrument Configuration in CSV format.
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
        final String SOURCE = "CsvTranslatorWriters.writeConfiguration()";
        final boolean boolSuccess;
        final int intCount;

        DataTranslatorHelper.addMessage(messages,
                                        METADATA_TARGET_CONFIGURATION + METADATA_ACTION_WRITE + METADATA_FORMAT);
        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Write the data from a general ReportTable in CSV format.
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
                                           final List<String> messages)
        {
        final String SOURCE = "CsvTranslatorWriters.writeReportTable()";
        boolean boolSuccess;

        boolSuccess = false;

//        DataTranslatorHelper.addMessage(messages,
//                                        METADATA_TARGET_REPORT
//                                            + METADATA_ACTION_WRITE
//                                            + METADATA_FORMAT);
        try
            {
            final List<Metadata> listMetadata;
            final List<String> listKeys;
            final int intColumnCount;
            final Vector<Vector> vecRawReport;

            // Find how many columns to write
            if (report.getReportTableModel() != null)
                {
                intColumnCount = report.getReportTableModel().getColumnCount();
                }
            else
                {
                intColumnCount = 0;
                }

            listMetadata = new ArrayList<Metadata>(100);

            // Accumulate the MetadataMetadata, if any
            if ((metadatametadata != null)
                && (!metadatametadata.isEmpty()))
                {
                listMetadata.addAll(metadatametadata);
                }

            // Accumulate the Metadata, if any
            if ((metadata != null)
                && (!metadata.isEmpty()))
                {
                listMetadata.addAll(metadata);
                }

            // Derive the Report column metadata from the ReportTableModel if possible
            if (infercolumnmetadata)
                {
                final List<Metadata> listColumnMetadata;

                listColumnMetadata = MetadataFactory.deriveReportColumnMetadata(datasettype, report);

                // Accumulate the ReportTable DataMetadata
                if ((listColumnMetadata != null)
                    && (!listColumnMetadata.isEmpty()))
                    {
                    listMetadata.addAll(listColumnMetadata);
                    }
                else
                    {
                    // This should never occur!
                    MetadataFactory.createDefaultReportTableMetadata(datasettype, intColumnCount).toArray(new Metadata[MetadataFactory.createDefaultReportTableMetadata(datasettype, intColumnCount).size()]);
                    }
                }

            // Read and sort all of the Metadata Keys
            listKeys = MetadataHelper.sortMetadataByKeys(listMetadata);

            stream.write("# CSV Export".getBytes());
            stream.write(DataTranslatorHelper.CR_LF.getBytes());
            stream.write(DataTranslatorHelper.CR_LF.getBytes());
            stream.write("# Metadata".getBytes());
            stream.write(DataTranslatorHelper.CR_LF.getBytes());

            // Write the sorted Metadata
            DataTranslatorHelper.writeMetadata(stream,
                                               listMetadata,
                                               listKeys,
                                               DataTranslatorHelper.COMMA);

            // Separate the Metadata and Data
            stream.write(DataTranslatorHelper.CR_LF.getBytes());
            stream.write("# Data Records".getBytes());
            stream.write(DataTranslatorHelper.CR_LF.getBytes());

            // Write the ReportTable RawReport Data
            vecRawReport = report.generateRawReport();

            if ((vecRawReport != null)
                && (!vecRawReport.isEmpty())
                && (intColumnCount > 0))
                {
                // Process all Rows
                for (int intRowIndex = 0;
                        intRowIndex < vecRawReport.size();
                        intRowIndex++)
                    {
                    final Vector vecRow;

                    vecRow = vecRawReport.get(intRowIndex);

                    // Write each row of the Report to the stream
                    for (int intColumnIndex = 0;
                        ((intColumnIndex < intColumnCount)
                            && (intColumnCount == vecRow.size()));
                        intColumnIndex++)
                        {
                        final Object objData;

                        objData = vecRow.get(intColumnIndex);

                        // Comma Separator
                        if (intColumnIndex > 0)
                            {
                            stream.write(DataTranslatorHelper.COMMA);
                            }

                        DataTranslatorHelper.writeCsvDataItem(stream, Utilities.stripHTML(objData.toString()));
                        }

                    // Terminate each row with CRLF
                    stream.write(DataTranslatorHelper.CR_LF.getBytes());
                    }
                }

            stream.write("# End of Export".getBytes());
            stream.write(DataTranslatorHelper.CR_LF.getBytes());

            // Tidy up
            stream.flush();
            stream.close();

            boolSuccess = true;
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
    }
