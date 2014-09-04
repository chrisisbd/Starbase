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

package org.lmn.fc.common.datatranslators.tsv;

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


/***********************************************************************************************
 * IndexedTabSeparatedTranslator.
 */

public final class IndexedTabSeparatedTranslator extends DataTranslator
                                                 implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + FileUtilities.tsv;


    /***********************************************************************************************
     * Construct a IndexedTabSeparatedTranslator.
     */

    public IndexedTabSeparatedTranslator()
        {
        // Treat all data as doubles (for now!)
        super();
        }


    /**********************************************************************************************/
    /* Importers                                                                                  */
    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     * The output RawData format is: <Index> <Channel0> <Channel1> <Channel2> ...
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
        final String SOURCE = "IndexedTabSeparatedTranslator.importRawData()";
        boolean boolSuccess;

        initialiseTranslator();
        boolSuccess = false;

        if ((filename != null)
            && (!EMPTY_STRING.equals(filename.trim()))
            && (filename.endsWith(DataFormat.TSV.getFileExtension()))
            && (eventlog != null)
            && (clock != null))
            {
            boolSuccess = TsvTranslatorReaders.parseTabSeparatedToRawData(this,
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
        final String SOURCE = "IndexedTabSeparatedTranslator.exportXYDataset()";
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

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.TSV));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_XYDATASET
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the XYDataset in the specified DataFormat
                boolSuccess = TsvTranslatorWriters.writeXYDataset(wrapper,
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
        final String SOURCE = "IndexedTabSeparatedTranslator.exportRawData()";
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

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.TSV));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_EXPORT
                               + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR);

                // Write the RawData in the specified DataFormat
                boolSuccess = TsvTranslatorWriters.writeRawData(wrapper,
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
     *@param metadata
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
*      @return boolean
     */

    public boolean exportMetadata(final List<Metadata> metadatametadata, final List<Metadata> metadata,
                                  final String filename,
                                  final boolean timestamp,
                                  final Vector<Vector> eventlog,
                                  final ObservatoryClockInterface clock)
        {
        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "metadata",
                                                 FILENAME_EXTENSION);

        return (false);
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
        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "report",
                                                 FILENAME_EXTENSION);

        return (false);
        }
    }



/***********************************************************************************************
 *
 * @param output
 * @param rows
 * @throws IOException
 */

//    private static void writeTSV(final OutputStream output,
//                                 final Iterator rows) throws IOException
//        {
//        int intIndex = 0;
//
//        while (rows.hasNext())
//            {
//            final Vector vecRow;
//
//            vecRow = (Vector) rows.next();
//
//            // ToDo Establish required separator or output format
//            if ((vecRow != null)
//                && (!vecRow.isEmpty()))
//                {
//                final Iterator iterRow;
//                final StringBuffer bufferRow;
//
//                iterRow = vecRow.iterator();
//                bufferRow = new StringBuffer();
//
//                // Write an Index column
//                bufferRow.append(intIndex++);
//                bufferRow.append(DataExporter.SEPARATOR_TAB);
//
//                // Process all columns in the Vector
//                while (iterRow.hasNext())
//                    {
//                    final Object objCell;
//
//                    objCell = iterRow.next();
//
//                    if ((objCell instanceof String)
//                        || (objCell instanceof StringBuffer))
//                        {
//                        bufferRow.append(ReportTable.stripHTML(objCell.toString()));
//                        }
//                    else if (objCell instanceof Calendar)
//                        {
//                        final Calendar calendar;
//
//                        calendar = (Calendar)objCell;
//                        bufferRow.append(ChronosHelper.toDateString(calendar));
//                        bufferRow.append(DataExporter.SEPARATOR_TAB);
//                        bufferRow.append(ChronosHelper.toTimeString(calendar));
//                        }
//                    else if (objCell instanceof Integer)
//                        {
//                        bufferRow.append(objCell.toString());
//                        }
//                    else if (objCell instanceof Double)
//                        {
//                        bufferRow.append(objCell.toString());
//                        }
//                    else if (objCell instanceof FlagIcon)
//                        {
//                        bufferRow.append(((FlagIcon)objCell).getCountryCode());
//                        }
//                    else if (objCell instanceof Boolean)
//                        {
//                        // Checkboxes can only be off or on
//                        if ((Boolean) objCell)
//                            {
//                            bufferRow.append("Y");
//                            }
//                        else
//                            {
//                            bufferRow.append("N");
//                            }
//                        }
//                    else if (objCell == null)
//                        {
//                        bufferRow.append(FrameworkStrings.SPACE);
//                        }
//                    else
//                        {
//                        // We don't know what we are trying to render
//                        // ReportIcons come here too...
//                        bufferRow.append("??");
//                        }
//
//                    bufferRow.append(DataExporter.SEPARATOR_TAB);
//                    }
//
//                bufferRow.append(DataExporter.CR_LF);
//                output.write(bufferRow.toString().getBytes());
//                }
//            }
//        // ToDo DataExporter.writeSignature(outputStream, clock);
//
//
//        }


///***********************************************************************************************
// * Write the sorted Metadata to the output stream, using the specified format.
// *
// * @param metadata
// * @param stream
// * @param format
// *
// * @throws IOException
// */
//
//public static void writeMetadata(final List<Metadata> metadata,
//                                 final OutputStream stream,
//                                 final DataFormat format) throws IOException
//    {
//    if ((metadata != null)
//        && (!metadata.isEmpty())
//        && (stream != null)
//        && (format != null))
//        {
//        final Vector<Vector> vecExportRows;
//        final Iterator<Vector> iterExportRows;
//        final Iterator<Metadata> iterMetadata;
//        final StringBuffer bufferMetadata;
//
//        vecExportRows = new Vector<Vector>(50);
//        iterMetadata = metadata.iterator();
//
//        // First assemble the Metadata into an easily sortable Vector
//        while (iterMetadata.hasNext())
//            {
//            final Metadata metaData;
//
//            metaData = iterMetadata.next();
//
//            if (metaData != null)
//                {
//                final Vector<Object> vecRow;
//
//                vecRow = new Vector<Object>(METADATA_ITEM_COUNT);
//
//                // Remember that all data entries must be Strings
//                vecRow.add(metaData.getKey());
//                vecRow.add(metaData.getValue());
//                vecRow.add(metaData.getUnits().toString());
//                vecRow.add(metaData.getDataTypeName().toString());
//
//                if (metaData.getDescription() != null)
//                    {
//                    vecRow.add(metaData.getDescription());
//                    }
//                else
//                    {
//                    vecRow.add(EMPTY_STRING);
//                    }
//
//                vecExportRows.add(vecRow);
//                }
//            }
//
//        // Sort the Metadata in the Vector by their keys (column 0)
//        Collections.sort(vecExportRows, new ReportRowsByColumn(0));
//
//        // Now assemble the sorted text of the export
//        bufferMetadata = new StringBuffer();
//        iterExportRows = vecExportRows.iterator();
//
//        // ToDo Establish required separator or output format
//        while (iterExportRows.hasNext())
//            {
//            final Vector vecRow;
//
//            vecRow = iterExportRows.next();
//
//            // Retrieve each item of Metadata (in text form) from the row Vector
//            bufferMetadata.append(vecRow.get(0));
//            bufferMetadata.append(SEPARATOR_TAB);
//            bufferMetadata.append(vecRow.get(1));
//            bufferMetadata.append(SEPARATOR_TAB);
//            bufferMetadata.append(vecRow.get(2));
//            bufferMetadata.append(SEPARATOR_TAB);
//            bufferMetadata.append(vecRow.get(3));
//            bufferMetadata.append(SEPARATOR_TAB);
//            bufferMetadata.append(vecRow.get(4));
//            bufferMetadata.append(CR_LF);
//            }
//
//        // One blank line to separate MetaData from RawData
//        bufferMetadata.append(CR_LF);
//
//        // Write the assembled String to the OutputStream
//        stream.write(bufferMetadata.toString().getBytes());
//        }
//    }


