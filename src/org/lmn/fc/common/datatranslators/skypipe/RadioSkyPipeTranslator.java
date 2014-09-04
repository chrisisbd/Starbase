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

package org.lmn.fc.common.datatranslators.skypipe;

import org.lmn.fc.common.datatranslators.DataTranslator;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import static org.lmn.fc.common.datatranslators.skypipe.RadioSkyPipeHeader.*;


/***************************************************************************************************
 * The RadioSkyPipe Translator.
 *
 * ToDo allow No Timestamps format
 *
 * See: http://www.codeguru.com/vb/gen/vb_misc/algorithms/article.php/c7495
 */

public final class RadioSkyPipeTranslator extends DataTranslator
                                          implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + "spd";

    private RadioSkyPipeHeader header;


    /***********************************************************************************************
     * Construct a RadioSkyPipeTranslator.
     */

    public RadioSkyPipeTranslator()
        {
        super();

        header = null;
        }


    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     * Import data from the specified filename into the DataTranslator.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public final boolean importRawData(final String filename,
                                       final Vector<Vector> eventlog,
                                       final ObservatoryClockInterface clock)
        {
        final boolean DEBUG = true;
        byte[] bytes;
        final boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "RadioSkyPipeTranslator.importRawData() Importing SkyPipe data from" + SPACE + filename);
        //ObservatoryInstrumentHelper.diagnoseMemory("RadioSkyPipeTranslator.importRawData() START");

        if ((filename != null)
            && (filename.endsWith(FILENAME_EXTENSION)))
            {
            bytes = readFileAsByteArray(filename);

            if ((bytes != null)
                && (bytes.length > 0))
                {
                final int intBeginData;
                final int intDataLength;
                final int intSamples;

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "RadioSkyPipeTranslator.importRawData() read " + bytes.length + " bytes");
                setImportedCount(bytes.length);

                // Read the Header and any metadata
                // The header determines the number of bytes per timestamp and data sample channels
                header = new RadioSkyPipeHeader(filename,
                                                DataTranslatorHelper.arrayToString(bytes, OFFSET_VERSION, LENGTH_VERSION),
                                                DataTranslatorHelper.arrayToDouble(bytes, OFFSET_START),
                                                DataTranslatorHelper.arrayToDouble(bytes, OFFSET_FINISH),
                                                DataTranslatorHelper.arrayToDouble(bytes, OFFSET_LAT),
                                                DataTranslatorHelper.arrayToDouble(bytes, OFFSET_LONG),
                                                DataTranslatorHelper.arrayToDouble(bytes, OFFSET_MAX_Y),
                                                DataTranslatorHelper.arrayToDouble(bytes, OFFSET_MIN_Y),
                                                DataTranslatorHelper.arrayToInt(bytes, OFFSET_TIMEZONE),
                                                DataTranslatorHelper.arrayToString(bytes, OFFSET_SOURCE, LENGTH_SOURCE),
                                                DataTranslatorHelper.arrayToString(bytes, OFFSET_AUTHOR, LENGTH_AUTHOR),
                                                DataTranslatorHelper.arrayToString(bytes, OFFSET_LOCAL_NAME, LENGTH_LOCAL_NAME),
                                                DataTranslatorHelper.arrayToString(bytes, OFFSET_LOCATION, LENGTH_LOCATION),
                                                DataTranslatorHelper.arrayToInt(bytes, OFFSET_CHANNELS),
                                                DataTranslatorHelper.arrayToInt(bytes, OFFSET_NOTE_LENGTH),
                                                DataTranslatorHelper.arrayToString(bytes, OFFSET_NOTE_TEXT,
                                                DataTranslatorHelper.arrayToInt(bytes, OFFSET_NOTE_LENGTH)));
                header.debugHeader();

                // Clear the Metadata list and add data from the Header
                RadioSkyPipeHelper.addMetadataFromHeader(this, header, DEBUG);

                // Check that we have at least one channel...
                if (header.getChannelCount() > 0)
                    {
                    // intBeginData points to the start of the data area
                    intBeginData = OFFSET_NOTE_TEXT + header.getNoteLength();

                    // The length of the data is the total length of the array less header & note
                    intDataLength = bytes.length - intBeginData;

                    // bytes per sample = (date + (channels * data))
                    // The DateSize may be zero, if Timestamps are not used
                    intSamples = intDataLength / (header.getDateSize()
                                                    + (header.getChannelCount() * header.getDataSize()));

                    // The SkyPipe sample format is: <Date> <Channel0> <Channel2> <Channel3> ...
                    //                           OR:        <Channel0> <Channel2> <Channel3> ...
                    // The Starbase sample format is: <Calendar> <Channel0> <Channel2> <Channel3> ...

                    // sampleOffset points to the start of the data group for each sample
                    int sampleOffset = 0;

                    // See if we have the 8-byte timestamp on each sample
                    if (header.isDataTimestamped())
                        {
                        // This loop can be VERY LARGE
                        // Be careful which objects are created inside...

                        for (int i = 0; i < intSamples; i++)
                            {
                            final Vector<Object> vecSample;
                            final double dblDays;
                            final Calendar calendar;

                            dblDays = DataTranslatorHelper.arrayToDouble(bytes,
                                                                         (intBeginData + sampleOffset));
                            calendar = RadioSkyPipeHelper.doubleToCalendar(header,
                                                                           dblDays,
                                                                           header.getTimeZone(),
                                                                           false);

                            vecSample = new Vector<Object>(header.getChannelCount() + 1);
                            vecSample.add(calendar);

//                            LOGGER.debug(DEBUG,
//                                         i + " " + ChronosHelper.toCalendarString(calendar) + " days=" + dblDays);

                            // Point to the start of the first channel
                            sampleOffset += header.getDateSize();

                            // Now unpack each Channel in turn
                            for (int j = 0;
                                 j < header.getChannelCount();
                                 j++)
                                {
                                // This Translator uses Doubles and Integers
                                if (header.isDataDouble())
                                    {
                                    vecSample.add(DataTranslatorHelper.arrayToDouble(bytes, intBeginData + sampleOffset));
                                    }
                                else
                                    {
                                    vecSample.add(DataTranslatorHelper.arrayToInt(bytes, intBeginData + sampleOffset));
                                    }

                                // Point to the start of the next channel, or sample, if we are done
                                sampleOffset += header.getDataSize();
                                }

                            // If we get this far, it is a valid sample, so add to the DataTranslator superclass
                            // The sample format is: <Calendar> <Channel0> <Channel2> <Channel3> ...
                            addRawDataSample(vecSample);
                            }

                        addMessage(getRawData().size() + SPACE + "samples translated successfully");
                        boolSuccess = true;
                        }
                    else
                        {
                        // Todo generate timestamps if not in original file!
                        addMessage("This version of the Translator cannot process data with no timestamps");
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    addMessage("There are no channels to process");
                    boolSuccess = false;
                    }
                }
            else
                {
                addMessage("There are no data to process");
                boolSuccess = false;
                }
            }
        else
            {
            addMessage("The SkyPipe filename is invalid");
            boolSuccess = false;
            }

        // Ensure that GC gives us a hand...
        bytes = null;

        ObservatoryInstrumentHelper.diagnoseMemory("RadioSkyPipeTranslator.importRawData() END");

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
        return (false);
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
        return (false);
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

    public boolean exportMetadata(final List<Metadata> metadatametadata, final List<Metadata> metadata,
                                  final String filename,
                                  final boolean timestamp,
                                  final Vector<Vector> eventlog,
                                  final ObservatoryClockInterface clock)
        {
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
        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "report",
                                                 FILENAME_EXTENSION);

        return (false);
        }


    /***********************************************************************************************
     * Read the Radio-SkyPipe file and return an array of bytes.
     *
     * @param filename
     *
     * @return byte[]
     */

    private byte[] readFileAsByteArray(final String filename)
        {
        byte[] bytes;

        bytes = new byte[] {};

        try
            {
            final File file;
            final InputStream inputStream;
            final long longFileLength;

            file = new File(filename);
            inputStream = new FileInputStream(file);
            longFileLength = file.length();

            if (longFileLength > Integer.MAX_VALUE)
                {
                addMessage("The RadioSkyPipe file is too large for this translator ("
                            + longFileLength
                            + " > "
                            + Integer.MAX_VALUE
                            + ")");
                }
            else
                {
                int offset;
                int intBytesRead;

                offset = 0;
                intBytesRead = 0;

                // File size limit is 2,147,483,647 bytes (!)
                bytes = new byte[(int) longFileLength];

                while (offset < bytes.length
                    && (intBytesRead >= 0))
                    {
                    intBytesRead = inputStream.read(bytes, offset, (bytes.length - offset));
                    offset += intBytesRead;
                    }

                if (offset < bytes.length)
                    {
                    addMessage("Could not read all data from RadioSkyPipe file " + file.getName());
                    }

                // We should now have the entire file in the byte array,
                // so we don't need the file again
                inputStream.close();
                }
            }

        catch (IOException exception)
            {
            addMessage("File operation failed [exception=" + exception.getMessage() + "]");
            }

        return (bytes);
        }


    /***********************************************************************************************
     * Get the RawDataChannelCount for this DataTranslator.
     *
     * @return int
     */

    public int getRawDataChannelCount()
        {
        if (header != null)
            {
            return (header.getChannelCount());
            }
        else
            {
            return (0);
            }
        }


    /***********************************************************************************************
     * Get the TemperatureChannel indicator for this DataTranslator.
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (false);
        }


    /***********************************************************************************************
     * Set the TemperatureChannel indicator for this DataTranslator.
     *
     * @param flag
     */

    public void setTemperatureChannel(final boolean flag)
        {
        // Do nothing
        }


    /***********************************************************************************************
     * Get the RadioSkyPipeHeader.
     * Ideally, this should be hidden from the end user of the data.
     *
     * @return RadioSkyPipeHeader
     */

    public RadioSkyPipeHeader getHeader()
        {
        return (this.header);
        }
    }
