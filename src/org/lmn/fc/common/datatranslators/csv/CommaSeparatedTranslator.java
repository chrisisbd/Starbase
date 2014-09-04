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

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslator;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * CommaSeparatedTranslator.
 */

public final class CommaSeparatedTranslator extends DataTranslator
                                            implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + FileUtilities.csv;


    /***********************************************************************************************
     * Construct a CommaSeparatedTranslator.
     */

    public CommaSeparatedTranslator()
        {
        // Treat all data as doubles (for now!)
        super();
        }


    /**********************************************************************************************/
    /* Importers                                                                                  */
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
        final String SOURCE = "CommaSeparatedTranslator.importRawData() ";
        boolean boolSuccess;

        initialiseTranslator();
        boolSuccess = false;

        if ((filename != null)
            && (!EMPTY_STRING.equals(filename.trim()))
            && (filename.endsWith(DataFormat.CSV.getFileExtension()))
            && (eventlog != null)
            && (clock != null))
            {
            boolSuccess = CsvTranslatorReaders.parseCommaSeparatedToRawData(this,
                                                                            filename,
                                                                            eventlog,
                                                                            clock);
            }
        else
            {
            addMessage(METADATA_TARGET_RAWDATA
                        + METADATA_ACTION_IMPORT
                        + METADATA_RESULT + ERROR_FILE_IMPORT + TERMINATOR + SPACE
                        + METADATA_EXCEPTION + ERROR_FILE_NAME + TERMINATOR + SPACE
                        + METADATA_FILENAME + filename + TERMINATOR);
            }

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* Exporters                                                                                  */
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
        final String SOURCE = "CommaSeparatedTranslator.exportXYDataset() ";
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

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.CSV));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the XYDataset in the specified DataFormat
                boolSuccess = CsvTranslatorWriters.writeXYDataset(wrapper,
                                                                  false,
                                                                  outputStream,
                                                                  getMessages());
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
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportRawData(final DAOWrapperInterface wrapper,
                                 final String filename,
                                 final boolean timestamp,
                                 final Vector<Vector> eventlog,
                                 final ObservatoryClockInterface clock)
        {
        final String SOURCE = "CommaSeparatedTranslator.exportRawData() ";
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must at least be some RawData!
        if ((wrapper != null)
            && (wrapper.getRawData() != null)
            && (!wrapper.getRawData().isEmpty())
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (eventlog != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.CSV));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the RawData in the specified DataFormat
                boolSuccess = CsvTranslatorWriters.writeRawData(wrapper,
                                                                false,
                                                                outputStream,
                                                                getMessages());
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
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportLog(final List<Metadata> logmetadata,
                             final Vector<Vector> logdata,
                             final int logwidth,
                             final String filename,
                             final boolean timestamp,
                             final Vector<Vector> log,
                             final ObservatoryClockInterface clock)
        {
        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "log",
                                                 FILENAME_EXTENSION);

        return (false);
        }


    /***********************************************************************************************
     * exportMetadata().
     *
     * @param metadatametadata
     * @param metadata
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportMetadata(final List<Metadata> metadatametadata,
                                  final List<Metadata> metadata,
                                  final String filename,
                                  final boolean timestamp,
                                  final Vector<Vector> eventlog,
                                  final ObservatoryClockInterface clock)
        {
        boolean boolSuccess;

        boolSuccess = false;

        // Let the User know there must at least be some MetaData!
        if (((metadata != null)
            && (!metadata.isEmpty()))
            && (filename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(filename))
            && (eventlog != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.CSV));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_METADATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the MetaData in the specified DataFormat
                boolSuccess = CsvTranslatorWriters.writeMetadata(metadatametadata,
                                                                 metadata,
                                                                 false,
                                                                 outputStream,
                                                                 getMessages());
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
     *
     * @param configmetadata
     * @param instrument
     * @param extraconfig
     * @param parentresourcekey
     * @param resourcekey
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportConfiguration(final List<Metadata> configmetadata,
                                       final Instrument instrument,
                                       final Vector<Vector> extraconfig,
                                       final String parentresourcekey,
                                       final String resourcekey,
                                       final String filename,
                                       final boolean timestamp,
                                       final Vector<Vector> eventlog,
                                       final ObservatoryClockInterface clock)
        {
        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "configuration",
                                                 FILENAME_EXTENSION);

        return (false);
        }


    /***********************************************************************************************
     * exportReportTable().
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
        final String SOURCE = "CommaSeparatedTranslator.exportReportTable() ";
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

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.CSV));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_REPORT
                               + METADATA_ACTION_EXPORT
                               + METADATA_NAME + report.getReportUniqueName() + TERMINATOR_SPACE
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the ReportTable in the specified DataFormat
                boolSuccess = CsvTranslatorWriters.writeReportTable(metadatametadata,
                                                                    metadata,
                                                                    infercolumnmetadata,
                                                                    datasettype,
                                                                    report,
                                                                    false,
                                                                    outputStream,
                                                                    getMessages());
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
