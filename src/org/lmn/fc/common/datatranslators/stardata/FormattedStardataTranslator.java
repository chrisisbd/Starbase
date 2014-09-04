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

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.*;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.stardata.*;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.*;
import java.text.ParseException;
import java.util.*;


/***************************************************************************************************
 * FormattedStardataTranslator.
 *
 * ToDo add millisec to time output
 */

public final class FormattedStardataTranslator extends DataTranslator
                                               implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + FileUtilities.xml;

    private static final String MSG_VERBOSE_NOT_SUPPORTED = "The verbose form of Stardata is not supported in this version";

    private static final boolean FORMATTED = false;


    /***********************************************************************************************
     * Construct a FormattedStardataTranslator.
     */

    public FormattedStardataTranslator()
        {
        super();

        setRawDataChannelCount(0);
        }


    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean importRawData(final String filename,
                                 final Vector<Vector> eventlog,
                                 final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.importRawData()";
        boolean boolSuccess;

        boolSuccess = true;
        setRawDataChannelCount(0);
        setTemperatureChannel(false);

        if ((filename != null)
            && (!EMPTY_STRING.equals(filename.trim()))
            && (filename.endsWith(DataFormat.STARDATA_FORMATTED.getFileExtension()))
            && (eventlog != null)
            && (clock != null))
            {
            try
                {
                final File xmlFile;
                StardataDocument docStardata;
                final TimeZone timeZone;
                final Locale locale;
                boolean boolUseCalendar;

//                boolSuccess = StardataTranslatorReaders.parseStardataToRawData(this,
//                                                                               filename,
//                                                                               eventlog,
//                                                                               clock);



                // Do this only once...
                // ToDo this should really come from the metadata?
                timeZone = TimeZone.getTimeZone("GMT+00:00");
                locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                    REGISTRY.getFramework().getCountryISOCode());
                boolUseCalendar = false;

                xmlFile = new File(filename);
                // A bit too verbose?
//                addMessage(METADATA_TARGET_RAWDATA
//                               + METADATA_ACTION_IMPORT
//                               + METADATA_FILENAME + xmlFile.getAbsolutePath() + TERMINATOR);

                docStardata = StardataDocument.Factory.parse(xmlFile);

                if (XmlBeansUtilities.isValidXml(docStardata, LOADER_PROPERTIES.isValidationXML()))
                    {
                    final StardataDocument.Stardata starData;

                    starData = docStardata.getStardata();

                    //------------------------------------------------------------------------------
                    // Do we have an optional Header?

                    if ((starData != null)
                        && (starData.getHeader() != null))
                        {
                        final HeaderType header;

                        header = starData.getHeader();

                        // If we have a Header, do we have MetadataMetadata?
                        if ((header.getMetadataMetadataList() != null)
                            && (!header.getMetadataMetadataList().isEmpty()))
                            {
                            // Take care not to keep a reference to the original document
                            addAllMetadataMetadata(((HeaderType)header.copy()).getMetadataMetadataList());
                            }
                        else
                            {
                            // If nothing in the Header, create some default MetadataMetadata
                            addAllMetadataMetadata(MetadataFactory.createDefaultMetadataMetadata());
                            }

                        // If we have a Header, do we have Metadata?
                        if ((header.getMetadataList() != null)
                            && (!header.getMetadataList().isEmpty()))
                            {
                            // Take care not to keep a reference to the original document
                            addOrUpdateAllMetadataToContainers(((HeaderType) header.copy()).getMetadataList());
                            }
                        }

                    // We can still have a valid import,
                    // even if both of the above produced no Metadata,
                    // or if there was no Header present

                    //------------------------------------------------------------------------------
                    // Do we have any Data?

                    if ((starData != null)
                        && (starData.getData() != null))
                        {
                        final DataType data;

                        data = starData.getData();

                        // Do we have the verbose or concise form of Stardata?
                        // Also, do we have timestamped columns, or just columns?
                        // There are four options in the <xs:choice> ...

//                        <xs:element name="Record"
//                                    minOccurs="0"
//                                    maxOccurs="unbounded"
//                                    type="RecordType"/>

                        if ((data.getRecordList() != null)
                            && (!data.getRecordList().isEmpty()))
                            {
                            final Iterator<RecordType> iterRecordType;

                            // Date, Time and multiple Columns (Date, Time, Column)
                            boolUseCalendar = true;

                            //System.out.println("verbose Date, Time, Column");
                            // Verbose form of RecordType
                            iterRecordType = data.getRecordList().iterator();

                            // Read each record from the file, until no more data or an exception
                            while ((boolSuccess)
                                && (iterRecordType.hasNext()))
                                {
                                try
                                    {
                                    final Vector<Object> vecColumns;
                                    final RecordType recordType;
                                    final String strDate;
                                    final String strTime;
                                    final List<String> listColumns;
                                    final Iterator<String> iterColumns;
                                    final Calendar calendar;

                                    recordType = iterRecordType.next();

                                    strDate = recordType.getDate();
                                    strTime = recordType.getTime();
                                    listColumns = recordType.getColumnList();
                                    iterColumns = listColumns.iterator();
                                    // Add an extra column for the Calendar
                                    vecColumns = new Vector<Object>(listColumns.size() + 1);

                                    // The number of columns of data
                                    setRawDataChannelCount(listColumns.size());

                                    // Add the Date and Time
                                    calendar = ChronosHelper.parseCalendar(timeZone,
                                                                           locale,
                                                                           DATE_PARSE_FORMAT,
                                                                           strDate + SPACE + strTime);

                                    // This sample isn't added if we get a ParseException above
                                    vecColumns.add(calendar);

                                    // Add the Columns
                                    while (iterColumns.hasNext())
                                        {
                                        // ToDo derive a DataType
                                        vecColumns.add(Double.parseDouble(iterColumns.next()));
                                        }

                                    addRawDataSample(vecColumns);
                                    }

                                catch (NumberFormatException exception)
                                    {
                                    addMessage(METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + MSG_PARSE_INTEGER + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                    boolSuccess = false;
                                    }

                                catch (ParseException exception)
                                    {
                                    addMessage(METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + MSG_PARSE_DATE_OR_TIME + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                    boolSuccess = false;
                                    }
                                }
                            }
                        else if ((data.getRecordDataList() != null)
                            && (!data.getRecordDataList().isEmpty()))
                            {
                            final Iterator<ColumnData> iterColumnData;

                            // Multiple Columns only (Column)
//                            <xs:element name="RecordData"
//                                        minOccurs="0"
//                                        maxOccurs="unbounded"
//                                        type="ColumnData"/>

                            //System.out.println("verbose Column");
                            boolUseCalendar = false;

                            // Verbose form of ColumnData
                            iterColumnData = data.getRecordDataList().iterator();

                            // Read each record from the file
                            while (iterColumnData.hasNext())
                                {
                                try
                                    {
                                    final Vector<Object> vecColumns;
                                    final List<String> listColumns;
                                    final Iterator<String> iterColumns;

                                    // ColumnList must exist
                                    listColumns = iterColumnData.next().getColumnList();
                                    iterColumns = listColumns.iterator();
                                    vecColumns = new Vector<Object>(listColumns.size());

                                    // The number of data channels is one less than the number of columns,
                                    // because the first column is the X-axis
                                    setRawDataChannelCount(listColumns.size() - 1);

                                    while (iterColumns.hasNext())
                                        {
                                        // ToDo derive a DataType
                                        vecColumns.add(Double.parseDouble(iterColumns.next()));
                                        }

                                    addRawDataSample(vecColumns);
                                    }

                                catch (NumberFormatException exception)
                                    {
                                    addMessage(METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + MSG_PARSE_INTEGER + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                    boolSuccess = false;
                                    }
                                }
                            }
                        else if ((data.getRList() != null)
                            && (!data.getRList().isEmpty()))
                            {
                            final Iterator<RT> iterRT;

                            // Date, Time and multiple Columns (D, T, C)
//                            <xs:element name="R"
//                                        minOccurs="0"
//                                        maxOccurs="unbounded"
//                                        type="RT"/>
                            boolUseCalendar = true;

                            //System.out.println("compact Date, Time, Column");
                            // Compact form of RecordType
                            iterRT = data.getRList().iterator();

                            // Read each record from the file, until no more data or an exception
                            while ((boolSuccess)
                                && (iterRT.hasNext()))
                                {
                                try
                                    {
                                    final Vector<Object> vecColumns;
                                    final RT recordType;
                                    final String D;
                                    final String T;
                                    final List<String> listC;
                                    final Iterator<String> iterColumns;
                                    final Calendar calendar;

                                    recordType = iterRT.next();

                                    D = recordType.getD();
                                    T = recordType.getT();
                                    listC = recordType.getCList();
                                    iterColumns = listC.iterator();
                                    // Add an extra column for the Calendar
                                    vecColumns = new Vector<Object>(listC.size() + 1);

                                    setRawDataChannelCount(listC.size());

                                    // Add the Date and Time
                                    calendar = ChronosHelper.parseCalendar(timeZone,
                                                                           locale,
                                                                           DATE_PARSE_FORMAT,
                                                                           D + SPACE + T);

                                    // This sample isn't added if we get a ParseException above
                                    vecColumns.add(calendar);

                                    // Add the Columns
                                    while (iterColumns.hasNext())
                                        {
                                        // ToDo derive a DataType
                                        vecColumns.add(Double.parseDouble(iterColumns.next()));
                                        }

                                    addRawDataSample(vecColumns);
                                    }

                                catch (NumberFormatException exception)
                                    {
                                    addMessage(METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + MSG_PARSE_INTEGER + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                    boolSuccess = false;
                                    }

                                catch (ParseException exception)
                                    {
                                    addMessage(METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + MSG_PARSE_DATE_OR_TIME + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                    boolSuccess = false;
                                    }
                                }
                            }
                        else if ((data.getRDList() != null)
                            && (!data.getRDList().isEmpty()))
                            {
                            final Iterator<CD> iterCD;

                            // Multiple Columns only (C)
//                            <xs:element name="C"
//                                        minOccurs="1"
//                                        maxOccurs="unbounded">
//                                <xs:simpleType>
//                                    <xs:restriction base="xs:normalizedString">
//                                        <!--<xs:pattern value="([0-9.\-\+e])*"/>-->
//                                        <xs:minLength value="0"/>
//                                    </xs:restriction>
//                                </xs:simpleType>
//                            </xs:element>
                            boolUseCalendar = false;

                            //System.out.println("compact Column");
                            // Compact form of ColumnData
                            iterCD = data.getRDList().iterator();

                            // Read each record from the file
                            while (iterCD.hasNext())
                                {
                                try
                                    {
                                    final Vector<Object> vecColumns;
                                    final List<String> listC;
                                    final Iterator<String> iterColumns;

                                    // CList must exist
                                    listC = iterCD.next().getCList();
                                    iterColumns = listC.iterator();
                                    vecColumns = new Vector<Object>(listC.size());

                                    // The number of data channels is one less than the number of columns,
                                    // because the first column is the X-axis
                                    setRawDataChannelCount(listC.size() - 1);

                                    while (iterColumns.hasNext())
                                        {
                                        // ToDo derive a DataType
                                        vecColumns.add(Double.parseDouble(iterColumns.next()));
                                        }

                                    addRawDataSample(vecColumns);
                                    }

                                catch (NumberFormatException exception)
                                    {
                                    addMessage(METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + MSG_PARSE_INTEGER + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                                    boolSuccess = false;
                                    }
                                }
                            }
                        else
                            {
                            // There were no Data records at all, which is permissible
                            addMessage(METADATA_TARGET_RAWDATA
                                           + METADATA_ACTION_IMPORT
                                           + METADATA_RESULT
                                                + MSG_NO_DATA
                                                + TERMINATOR);
                            }

                        // Do we have DataMetadata?
                        // We have to do this at the end,
                        // so we know what kind of Records are present to create Metadata
                        if ((data.getDataMetadataList() != null)
                            && (!data.getDataMetadataList().isEmpty()))
                            {
                            addOrUpdateAllMetadataToContainers(((DataType) data.copy()).getDataMetadataList());
                            }

                        // THIS SEEMED TO BE MORE TROUBLE THAN IT WAS WORTH?!
//                        else
//                            {
//                            // If not, create some default RawDataMetadata
//                            // Clearly we don't know if Temperature is present
//                            addOrUpdateAllMetadataToContainers(MetadataFactory.createMissingDefaultRawDataMetadata(null,
//                                                                                                                   getRawDataChannelCount(),
//                                                                                                                   boolUseCalendar));
//                            }

                        // Finally see if the ObservationMetadata contains a Temperature Channel
                        setTemperatureChannel(DataAnalyser.hasTemperatureChannel(getObservationMetadata()));

                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               "FORMATTED STARDATA TRANSLATOR CHANNEL COUNT="
                                               + getRawDataChannelCount() + " TEMPERATURE CHANNEL=" + hasTemperatureChannel());
                        }
                    else
                        {
                        // TODO REVIEW No data found (there may have been only Metadata)
                        boolSuccess = true;
                        }

                    if ((boolSuccess)
                        && (getRawData().size() >= 0))
                        {
                        setImportedCount(docStardata.toString().length());
                        addMessage(METADATA_TARGET_RAWDATA
                                       + METADATA_ACTION_IMPORT
                                       + METADATA_RESULT
                                            + getRawData().size()
                                            + MSG_SAMPLES_TRANSLATED
                                            + TERMINATOR);
                        }
                    else
                        {
                        setRawDataChannelCount(0);
                        setTemperatureChannel(false);
                        }
                    }
                else
                    {
                    addMessage(METADATA_TARGET_RAWDATA
                                   + METADATA_ACTION_IMPORT
                                   + METADATA_RESULT + ERROR_FILE_IMPORT + TERMINATOR + SPACE
                                   + METADATA_EXCEPTION + EXCEPTION_XML_VALIDATION + TERMINATOR);
                    }

                // Help the GC?
                docStardata = null;
                ObservatoryInstrumentHelper.runGarbageCollector();
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_IMPORT
                               + METADATA_RESULT + ERROR_FILE_IMPORT + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (XmlException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_IMPORT
                               + METADATA_RESULT + ERROR_FILE_IMPORT + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_IMPORT
                               + METADATA_RESULT + ERROR_FILE_IMPORT + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                exception.printStackTrace();
                }
            }
        else
            {
            addMessage(METADATA_TARGET_RAWDATA
                            + METADATA_ACTION_IMPORT
                            + METADATA_RESULT + ERROR_FILE_IMPORT + TERMINATOR + SPACE
                            + METADATA_EXCEPTION
                            + ERROR_FILE_NAME
                            + TERMINATOR + SPACE
                            + METADATA_FILENAME
                            + filename
                            + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportXYDataset().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportXYDataset(final DAOWrapperInterface wrapper,
                                   final String filename,
                                   final boolean timestamp,
                                   final Vector<Vector> eventlog,
                                   final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.exportXYDataset()";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must be something in the XYDataset!
        if ((wrapper != null)
            && (wrapper.getXYDataset() != null)
            && (wrapper.getXYDataset().getSeriesCount() > 0)
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (eventlog != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.STARDATA_FORMATTED));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the XYDataset in the specified DataFormat
                boolSuccess = StardataTranslatorWriters.writeXYDataset(wrapper,
                                                                       false,
                                                                       outputStream,
                                                                       getMessages(),
                                                                       FORMATTED);
                // Tidy up
                if (boolSuccess)
                    {
                    outputStream.flush();
                    }

                // Record how much was written
                setExportedCount(file.length());
                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT
                                    + getExportedCount()
                                    + MSG_BYTES_EXPORTED
                                    + TERMINATOR);
                outputStream.close();
                }

            catch (SecurityException exception)
                {
                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                exception.printStackTrace();
                }
            }
        else
            {
            addMessage(METADATA_TARGET_XYDATASET
                           + METADATA_ACTION_EXPORT
                           + METADATA_RESULT + ERROR_XYDATASET + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportRawData().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportRawData(final DAOWrapperInterface wrapper,
                                 final String filename,
                                 final boolean timestamp,
                                 final Vector<Vector> log,
                                 final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.exportRawData()";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must at least be some RawData!
        if ((wrapper != null)
            && (wrapper.getRawData() != null)
            && (!wrapper.getRawData().isEmpty())
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (log != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.STARDATA_FORMATTED));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the RawData in the specified DataFormat
                boolSuccess = StardataTranslatorWriters.writeRawData(wrapper,
                                                                     false,
                                                                     outputStream,
                                                                     getMessages(),
                                                                     FORMATTED);
                // Tidy up
                if (boolSuccess)
                    {
                    outputStream.flush();
                    }

                // Record how much was written
                setExportedCount(file.length());
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT
                                    + getExportedCount()
                                    + MSG_BYTES_EXPORTED
                                    + TERMINATOR);
                outputStream.close();
                }

            catch (SecurityException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                exception.printStackTrace();
                }
            }
        else
            {
            addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_RAW_DATA + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportLog().
     *
     * @param logmetadata
     * @param logdata
     * @param logwidth
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportLog(final List<Metadata> logmetadata,
                             final Vector<Vector> logdata,
                             final int logwidth,
                             final String filename,
                             final boolean timestamp,
                             final Vector<Vector> eventlog,
                             final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.exportLog()";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must be data!
        if ((logdata != null)
            && (!logdata.isEmpty())
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (eventlog != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.STARDATA_FORMATTED));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_LOG
                               + METADATA_ACTION_EXPORT
                               + METADATA_WIDTH + logwidth + TERMINATOR + SPACE
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the EventLog in the specified DataFormat
                boolSuccess = StardataTranslatorWriters.writeLog(logmetadata,
                                                                 DatasetType.TABULAR,
                                                                 logdata,
                                                                 logwidth,
                                                                 false,
                                                                 outputStream,
                                                                 getMessages(),
                                                                 FORMATTED);
                // Tidy up
                if (boolSuccess)
                    {
                    outputStream.flush();
                    }

                // Record how much was written
                setExportedCount(file.length());
                addMessage(METADATA_TARGET_LOG
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT
                                    + getExportedCount()
                                    + MSG_BYTES_EXPORTED
                                    + TERMINATOR);
                outputStream.close();
                }

            catch (SecurityException exception)
                {
                addMessage(METADATA_TARGET_LOG
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_LOG
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_LOG
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                addMessage(METADATA_TARGET_LOG
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                exception.printStackTrace();
                }
            }
        else
            {
            addMessage(METADATA_TARGET_LOG
                           + METADATA_ACTION_EXPORT
                           + METADATA_RESULT + ERROR_EVENT_LOG + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportMetadata().
     *
     * @param metadatametadata
     * @param metadata
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportMetadata(final List<Metadata> metadatametadata,
                                  final List<Metadata> metadata,
                                  final String filename,
                                  final boolean timestamp,
                                  final Vector<Vector> log,
                                  final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.exportMetadata()";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must be data!
        if ((metadata != null)
            && (!metadata.isEmpty())
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (log != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.STARDATA_FORMATTED));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the combined MetadataMetadata and Metadata in the specified DataFormat
                boolSuccess = StardataTranslatorWriters.writeMetadata(metadatametadata,
                                                                      metadata,
                                                                      false,
                                                                      outputStream,
                                                                      getMessages(),
                                                                      FORMATTED);
                // Tidy up
                if (boolSuccess)
                    {
                    outputStream.flush();
                    }

                // Record how much was written
                setExportedCount(file.length());
                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT
                                    + getExportedCount()
                                    + MSG_BYTES_EXPORTED
                                    + TERMINATOR);
                outputStream.close();
                }

            catch (SecurityException exception)
                {
                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                exception.printStackTrace();
                }
            }
        else
            {
            addMessage(METADATA_TARGET_METADATA
                           + METADATA_ACTION_EXPORT
                           + METADATA_RESULT + ERROR_METADATA + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportConfiguration().
     * The MetaData from the Instrument, Controller and Plugins,
     * and any extra Configuration (usually from the Instrument static configuration).
     *
     * @param configdatametadata
     * @param instrument
     * @param extraconfigdata
     * @param parentresourcekey
     * @param resourcekey
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportConfiguration(final List<Metadata> configdatametadata,
                                       final Instrument instrument,
                                       final Vector<Vector> extraconfigdata,
                                       final String parentresourcekey,
                                       final String resourcekey,
                                       final String filename,
                                       final boolean timestamp,
                                       final Vector<Vector> log,
                                       final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.exportConfiguration()";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must be data!
        if ((((instrument != null)
                && (XmlBeansUtilities.isValidXml(instrument, LOADER_PROPERTIES.isValidationXML())))
            || ((extraconfigdata != null)
                && (!extraconfigdata.isEmpty())))

            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (log != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;
                Vector<Vector> vecConfigData;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.STARDATA_FORMATTED));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_CONFIGURATION
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                vecConfigData = new Vector<Vector>(50);
//                InstrumentUIHelper.collectInstrumentConfiguration(vecConfigData,
//                                                                           instrument,
//                                                                           parentresourcekey);

                ConfigurationHelper.appendRegistryPropertiesForKey(vecConfigData,
                                                                   resourcekey);

                // Write the combined Configuration in the specified DataFormat
                boolSuccess = StardataTranslatorWriters.writeConfiguration(null,
                                                                           null,
                                                                           configdatametadata,
                                                                           vecConfigData,
                                                                           false,
                                                                           outputStream,
                                                                           getMessages(),
                                                                           FORMATTED);
                // Tidy up
                if (boolSuccess)
                    {
                    outputStream.flush();
                    }

                // Record how much was written
                setExportedCount(file.length());
                addMessage(METADATA_TARGET_CONFIGURATION
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT
                                    + getExportedCount()
                                    + MSG_BYTES_EXPORTED
                                    + TERMINATOR);
                outputStream.close();

                // Help the GC?
                vecConfigData = null;
                ObservatoryInstrumentHelper.runGarbageCollector();
                }

            catch (SecurityException exception)
                {
                addMessage(METADATA_TARGET_CONFIGURATION
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_CONFIGURATION
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_CONFIGURATION
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                addMessage(METADATA_TARGET_CONFIGURATION
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                exception.printStackTrace();
                }
            }
        else
            {
            addMessage(METADATA_TARGET_CONFIGURATION
                           + METADATA_ACTION_EXPORT
                           + METADATA_RESULT + ERROR_CONFIG + TERMINATOR);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportReportTable().
     * Export the the data from a general ReportTable.
     * Optional extra metadata may be added to the export.
     *
     * @param metadatametadata
     * @param metadata
     * @param infercolumnmetadata
     * @param datasettype
     * @param report
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportReportTable(final List<Metadata> metadatametadata,
                                     final List<Metadata> metadata,
                                     final boolean infercolumnmetadata,
                                     final DatasetType datasettype,
                                     final ReportTablePlugin report,
                                     final String filename,
                                     final boolean timestamp,
                                     final Vector<Vector> eventlog,
                                     final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FormattedStardataTranslator.exportReportTable()";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must be data!
        if ((report != null)
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (eventlog != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.STARDATA_FORMATTED));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_NAME + report.getReportUniqueName() + TERMINATOR_SPACE
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the ReportTable in the specified DataFormat
                boolSuccess = StardataTranslatorWriters.writeReportTable(metadatametadata,
                                                                         metadata,
                                                                         infercolumnmetadata,
                                                                         datasettype,
                                                                         report,
                                                                         false,
                                                                         outputStream,
                                                                         getMessages(),
                                                                         FORMATTED);
                // Tidy up
                if (boolSuccess)
                    {
                    outputStream.flush();
                    }

                // Record how much was written
                setExportedCount(file.length());
                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_NAME + report.getReportUniqueName() + TERMINATOR_SPACE
                               + METADATA_RESULT
                                    + getExportedCount()
                                    + MSG_BYTES_EXPORTED
                                    + TERMINATOR);
                outputStream.close();
                }

            catch (SecurityException exception)
                {
                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (IOException exception)
                {
                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }

            catch (Exception exception)
                {
                exception.printStackTrace();
                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                }
            }
        else
            {
            addMessage(METADATA_TARGET_REPORT
                           + METADATA_ACTION_EXPORT
                           + METADATA_RESULT + ERROR_REPORT + TERMINATOR);
            }

        return (boolSuccess);
        }
    }
