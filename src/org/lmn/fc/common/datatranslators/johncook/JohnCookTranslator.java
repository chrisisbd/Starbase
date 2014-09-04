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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  23-05-05    LMN created file
//  14-08-06    LMN changed slightly in accordance with RadioSkyPipeTranslator
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.common.datatranslators.johncook;

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

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

//--------------------------------------------------------------------------------------------------
// Index to data logger channels.
//
//   Date			    Ch1	        Ch2	        Ch3	        Ch4
// Fixed format files...
// 2000 March 4th.		23.4kHz	    -	        -
// 2000 March 11th		23.4kHz	    -	        JJM
// 2000 June 2nd.		23.4kHz	    16kHz	    JJM
// 2000 October 30th	18.3kHz	    23.4kHz	    JJM
// 2001 October 14th.	21.7kHz	    23.4kHz	    JJM
//
// Variable format files...
// 2001 October 22nd.	21.7kHz     23.4kHz     JJM
// 2001 November 25th.	20.9kHz	    23.4kHz	    JJM
// 2002 June 23rd.		AMR	        23.4kHz	    JJM
// 2002 July 2nd		20.9kHz	    23.4kHz	    JJM
// 2003 April 23rd.	    20.9kHz	    23.4kHz	    JJM	        AMR
// 2003 May 2nd.		20.9kHz	    23.4kHz	    JJM
// 2004 April 10th.	    20.9kHz	    23.4kHz	    AMR
//
// JJM = JamJar staribusmagnetometer.
// AMR = Anisotropic magneto-resistive staribusmagnetometer.
//
// John Cook 2005 July 9th.

//--------------------------------------------------------------------------------------------------
// Variable Format
// Each data file contains a header, time, date and collected data in binary format:
//
// 0n ss			    header always 2 bytes
// 8a bb cc dd		    date stamp always 4 bytes,
// ee 11 (22 33 44)	    variable data with seconds added,
// ee 11 (22 33 44)...	repeated at the sample interval, until the next hour.
//
// where..
//
// n = number of channels recorded, (msb set low),
// s = sample interval in seconds.
//
// a = minutes with msb set high,
// b = hours,
// c = days,
// d = month.
//
// e = second identifier.
// 1 = channel 1 data
// 2 = channel 2 data
// 3 = channel 3 data
//
// The date stamp is applied every hour:
//
// ee 11 (22 33 44)	last sample of the hour,
// 8a bb cc dd		Date stamp on the hour,
// ee 11 (22 33 44)	First sample of the next hour...
//
// This sequence repeats until the logger is stopped.
// Note that all items are 8 bit bytes.
//
// End of File is indicated by a Datestamp of FF FF FF FF
//
// 03 05			Variable format data
// 86 13 16 09
// 05 11 22 33
// 0A 11 22 33
// 0F .........
//
//--------------------------------------------------------------------------------------------------
// Fixed format
//
// Fixed format data files all have 3 data channels,
// and so the 2-byte header is not present.
// The sample interval is fixed at 5s.
//
// 8a bb cc dd		date stamp, applied every hour
// ee 11 22 33 		sampled data
// ee 11 22 33 .....
//
// This fixed format was used prior to 2001 October 22nd.
// The easiest method to distinguish the 2 formats is to check the msb of the first byte in the file.
// If the most significant bit is high, then it is the time stamp of a fixed format file.
// If it is low, then it is the channel number of a variable format file.
//
// BA 13 0D 07		Fixed format data
// 05 11 22 33
// 0A 11 22 33
// 0F .........


/***************************************************************************************************
 * Import data from John Cook's Observatory.
 *
 * ToDo implement exportData()
 */

public final class JohnCookTranslator extends DataTranslator
                                      implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + "dat";

    // Finite State Machine states
    private static final int STATE_INITIALISE       = 0;
    private static final int STATE_GET_HEADER       = 1;
    private static final int STATE_GET_DATESTAMP_OR_EOF = 2;
    private static final int STATE_GET_CHUNK        = 3;
    private static final int STATE_OK               = 4;
    private static final int STATE_ERROR            = 5;
    private static final int STATE_EXIT             = -1;

    private static final int LENGTH_FILENAME        = 12;
    private static final int LENGTH_DATESTAMP       = 4;
    private static final int LENGTH_DATA_MIN        = 8;

    private StringBuffer bufferImport;
    private int intIndex;
    private boolean boolValid;

    private int intSampleInterval;

    private int intYear;
    private int intMonth;
    private int intDays;

    private int intHours;
    private int intMinutes;
    private int intSeconds;


    /***********************************************************************************************
     * main() method for testing.
     *
     * @param args
     */

//    public static void main(final String[] args)
//        {
//        final DataTranslatorInterface translatorInterface;
//
//        translatorInterface = new JohnCookTranslator();
//
//        if (translatorInterface != null)
//            {
//            translatorInterface.initialiseTranslator();
//            }
//
//        if (translatorInterface.importRawData("../../../../../../../testdata/20050310.dat",
//                                           getEventLogFragment(),
//                                           getObservatoryClock()))
//            {
//            LOGGER.logTimedEvent(DataTranslator.showTranslatedData(translatorInterface, SHOW_DEBUG_LINECOUNT).toString());
//            LOGGER.logMessages(translatorInterface.getMessages());
//            }
//        else
//            {
//            LOGGER.logMessages(translatorInterface.getMessages());
//            LOGGER.logTimedEvent("Translation failed, but you knew that already");
//            }
//        }


    /***********************************************************************************************
     * Construct John Cook's DataTranslator.
     * Data are held as Integers.
     */

    public JohnCookTranslator()
        {
        super();

        bufferImport = null;
        intIndex = 0;
        boolValid = false;
        }


    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     * Import data from the specified filename.
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
        int intState;
        final String strYear;

        setValid(false);

        try
            {
            if ((filename != null)
                && (filename.length() >= LENGTH_FILENAME)
                && (filename.endsWith(FILENAME_EXTENSION)))
                {
                LOGGER.logTimedEvent("importRawData() Importing JohnCook data from" + SPACE + filename);

                // Import the data!
                bufferImport = FileUtilities.readFileAsString(filename);

                LOGGER.logTimedEvent("importRawData() JohnCookTranslator read " + getImportBuffer().length() + " bytes");
                setImportedCount(getImportBuffer().length());

                intIndex = 0;
                intState = STATE_INITIALISE;

                // If we get this far, the filename must be valid, so extract the Year
                // The filename format is: 20041110.dat
                strYear = filename.substring(filename.length() - LENGTH_FILENAME ,
                                             filename.length() - LENGTH_FILENAME + 4);
                intYear = Integer.parseInt(strYear);

                // Parse the file
                while(intState != STATE_EXIT)
                    {
                    intState = fsm(intState);
                    }

                // Fill in as much Metadata as we can
                JohnCookTranslatorHelper.addTranslatorMetadata(this, filename);

                // Tidy up to help the GC
                getImportBuffer().setLength(0);
                }
            else
                {
                addMessage("importRawData() Invalid filename: The format is: <year><month><day>.dat");
                }
            }

        catch (NumberFormatException exception)
            {
            addMessage("importRawData() Unable to read the Year from the filename");
            }

        catch (IOException exception)
            {
            addMessage("importRawData() Unable to read the data file");
            }

        return (isValid());
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
*      @return boolean
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
     *
     * @param currentstate
     *
     * @return int
     */

    private int fsm(final int currentstate)
        {
        final int intNextState;

        switch (currentstate)
            {
            case STATE_INITIALISE:
                {
                intNextState = stateInitialise();
                break;
                }

            case STATE_GET_HEADER:
                {
                intNextState = stateGetHeader();
                break;
                }

            case STATE_GET_DATESTAMP_OR_EOF:
                {
                intNextState = stateGetDatestampOrEof();
                break;
                }

            case STATE_GET_CHUNK:
                {
                intNextState = stateGetChunk();
                break;
                }

            case STATE_OK:
                {
                intNextState = stateOk();
                break;
                }

            case STATE_ERROR:
                {
                intNextState = stateError();
                break;
                }

            case STATE_EXIT:
                {
                intNextState = STATE_EXIT;
                break;
                }

            default:
                {
                setValid(false);
                intNextState = STATE_ERROR;
                }
            }

        return (intNextState);
        }


    /***********************************************************************************************
     *
     * @return int
     */

    private int stateInitialise()
        {
        final StringBuffer bufferOutput;
        int intByteCounter;

        //LOGGER.logTimedEvent("stateInitialise()");

        bufferOutput = new StringBuffer();
        intByteCounter= 0;

        // Read data from the buffer
        for (int i = 0; i < getImportBuffer().length(); i++)
            {
            bufferOutput.append(Integer.toHexString(getImportBuffer().charAt(i)));
            bufferOutput.append(SPACE);
            intByteCounter++;
            }

        //LOGGER.logTimedEvent(bufferOutput.toString());
        //LOGGER.logTimedEvent("stateInitialise() " + intByteCounter + SPACE + "Bytes read");
        //LOGGER.logTimedEvent(bufferOutput.toString());

        return (STATE_GET_HEADER);
        }


    /***********************************************************************************************
     *
     * @return int
     */

    private int stateGetHeader()
        {
        //LOGGER.logTimedEvent("stateGetHeader()");

        if ((getImportBuffer() != null)
            && (getImportBuffer().length() > LENGTH_DATA_MIN))
            {
            // Find out if it is one of the old format files, with no header

            if (getImportBuffer().charAt(0) < 0x80)
                {
                boolean boolDataValid;

                boolDataValid = false;

                // Scan for the header byte '0n'
                // WARNING This isn't sufficiently unique...
                while ((getIndex() < getImportBuffer().length())
                        && (getImportBuffer().charAt(getIndex()) > 0x0F))
                    {
                    setIndex(getIndex() + 1);
                    }

                // Check that we have 0n, ss, 8a
                // We expect at least one data channel and less than 16...
                if ((getImportBuffer().charAt(getIndex()) > 0)
                    && (getImportBuffer().charAt(getIndex()) <= 0x0F)
                    && (getImportBuffer().charAt(getIndex() + 2) > 0x80))
                    {
                    // It is probably the header...
                    boolDataValid = true;
                    }

                if (boolDataValid)
                    {
                    // Read number of channels and sample interval
                    setRawDataChannelCount(getImportBuffer().charAt(getIndex()));
                    setIndex(getIndex() + 1);
                    intSampleInterval = getImportBuffer().charAt(getIndex());

                    // Leave poimting at the first data sample block
                    setIndex(getIndex() + 1);

    //                LOGGER.logTimedEvent("stateGetHeader() Channels=" + intChannelCount);
    //                LOGGER.logTimedEvent("stateGetHeader() SampleInterval=" + intSampleInterval);

                    return (STATE_GET_DATESTAMP_OR_EOF);
                    }
                else
                    {
                    LOGGER.logTimedEvent("stateGetHeader() The data are not valid, and cannot be parsed");
                    return (STATE_ERROR);
                    }
                }
            else
                {
                // Older files need to be modified by inserting a header...
//                LOGGER.logTimedEvent("inserting...");

                setRawDataChannelCount(3);
                intSampleInterval = 5;
                getImportBuffer().insert(0, (char)intSampleInterval);
                getImportBuffer().insert(0, (char)getRawDataChannelCount());

                // Leave poimting at the first data sample block
                setIndex(2);

                return (STATE_GET_DATESTAMP_OR_EOF);
                }
            }
        else
            {
            return (STATE_ERROR);
            }
        }


    /***********************************************************************************************
     *
     * @return int
     */

    private int stateGetDatestampOrEof()
        {
        //LOGGER.logTimedEvent("stateGetDatestampOrEof()");

        // We should be pointing to the first byte of the Datestamp
        // which has the MSB set
        // There must be enough characters left for a complete Datestamp
        // ToDo check this!
        if ((getImportBuffer().length() - getIndex() >= LENGTH_DATESTAMP + 1)
            && (getImportBuffer().charAt(getIndex()) & 0x80) != 0)
            {
            // Might be a Datestamp or EOF (not very efficient!)
            if ((getImportBuffer().charAt(getIndex()) == 0xff)
                && (getImportBuffer().charAt(getIndex()+1) == 0xff)
                && (getImportBuffer().charAt(getIndex()+2) == 0xff)
                && (getImportBuffer().charAt(getIndex()+3) == 0xff))
                {
                // It's EOF
                //LOGGER.logTimedEvent("stateGetDatestampOrEof() END OF FILE");

                // There are no more data to read
                return (STATE_OK);
                }
            else
                {
                // Should be a Datestamp
                intMinutes = getImportBuffer().charAt(getIndex()) & 0x7F;
                // ToDo check minutes  (should be zero on the hour)
                setIndex(getIndex() + 1);
                intHours = getImportBuffer().charAt(getIndex());
                // ToDo check hours
                setIndex(getIndex() + 1);
                intDays = getImportBuffer().charAt(getIndex());
                // ToDo check days
                setIndex(getIndex() + 1);
                intMonth = getImportBuffer().charAt(getIndex());
                // ToDo check month
                setIndex(getIndex() + 1);

//                LOGGER.logTimedEvent("stateGetDatestampOrEof() Year=" + intYear);
//                LOGGER.logTimedEvent("stateGetDatestampOrEof() Month=" + intMonth);
//                LOGGER.logTimedEvent("stateGetDatestampOrEof() Days=" + intDays);
//                LOGGER.logTimedEvent("stateGetDatestampOrEof() Hours=" + intHours);
//                LOGGER.logTimedEvent("stateGetDatestampOrEof() Minutes=" + intMinutes);

                if (getImportBuffer().length() <= getIndex())
                    {
                    // There are no more data to read
                    return (STATE_OK);
                    }
                else
                    {
                    return (STATE_GET_CHUNK);
                    }
                }
            }
        else
            {
            // It isn't the right format for a complete Datestamp
            return (STATE_ERROR);
            }
        }


    /***********************************************************************************************
     * We seem to be in sync, so read sets of <sec><sample>[<sample>] until the next datestamp.
     *
     * @return int
     */

    private int stateGetChunk()
        {
        final Vector<Object> vecSample;
        final GregorianCalendar calendar;

        //LOGGER.logTimedEvent("stateGetChunk() START [chunk(20)=" + getImportBuffer().substring(getIndex(), 20) + "]");

        // There should Channels+1 items to read for each set
        // ToDo check this!
        if (getImportBuffer().length() - getIndex() > (getRawDataChannelCount() + 1))
            {
            vecSample = new Vector<Object>();

            // The first item is the seconds identifier, i.e. {00...59)
            intSeconds = getImportBuffer().charAt(getIndex());
            setIndex(getIndex() + 1);

            //LOGGER.logTimedEvent("stateGetChunk() [length=" + getImportBuffer().length() + "] [index=" + getIndex() + "] [seconds=" + intSeconds + "]");

            if ((intSeconds >= 0)
                && (intSeconds <= 59))
                {
                // We need to add in the Seconds we have just read,
                // to the Minutes already counted
                if (intSeconds == 0)
                    {
                    intMinutes++;
                    }

                // Form the Calendar for this sample from the data collected so far...
                // Remember that Java Months are zero-based, but in the file they are one-based
                // ToDo sort out TimeZone and Locale
                calendar = new GregorianCalendar(intYear,
                                                 intMonth - 1,
                                                 intDays,
                                                 intHours,
                                                 intMinutes,
                                                 intSeconds);
                vecSample.add(calendar);

                // The subsequent items are the data, i.e. {00...255}
                // and so must be taken verbatim
                for (int i = 0;
                     i < getRawDataChannelCount();
                     i++)
                    {
                    final int intData;

                    intData = (int) getImportBuffer().charAt(getIndex());
                    //LOGGER.logTimedEvent("stateGetChunk() [data=" + intData + "]");

                    // This Translator uses Integers *for each channel*
                    vecSample.add(intData);
                    setIndex(getIndex() + 1);
                    }

                // If we get this far, it is a valid sample, so add to the superclass
                addRawDataSample(vecSample);

                // Does the next byte indicate another sample or a Datestamp?
                // Or maybe we have run out of data?
                if (getImportBuffer().length() > getIndex())
                    {
                    // Another sample?
                    // Top bit set might indicate a Datestamp or EOF
                    if ((getImportBuffer().charAt(getIndex()) & 0x80) != 0)
                        {
                        // Maybe a Datestamp or EOF
                        return (STATE_GET_DATESTAMP_OR_EOF);
                        }
                    else
                        {
                        // Probably more data, so come back here
                        return (STATE_GET_CHUNK);
                        }
                    }
                else
                    {
                    // We have run out of data
                    // Just leave gracefully, since preceding data should be Ok
                    addMessage("stateGetChunk() There are no more data in this file");

                    return (STATE_OK);
                    }
                }
            else
                {
                // The seconds identifier is invalid, so we are now out of sync
                addMessage("stateGetChunk() The seconds identifier is invalid");

                return (STATE_ERROR);
                }
            }
        else
            {
            // There are not enough data for a complete set of samples
            // Just leave gracefully, since preceding data should be Ok
            addMessage("stateGetChunk() There are not enough data to complete the sample");

            return (STATE_OK);
            }
        }


    /***********************************************************************************************
     *
     * @return int
     */

    private int stateOk()
        {
        // This is the only place where Valid is set true
        setValid(true);
        addMessage(getRawData().size() + SPACE + "samples translated successfully");

        return (STATE_EXIT);
        }


    /***********************************************************************************************
     *
     */

    private int stateError()
        {
        //LOGGER.logTimedEvent("stateError()");
        LOGGER.logTimedEvent(getMessages().toString());

        return (STATE_EXIT);
        }


    /***********************************************************************************************
     *
     * @return StringBuffer
     */

    private StringBuffer getImportBuffer()
        {
        return (this.bufferImport);
        }


    /***********************************************************************************************
     *
     * @return int
     */

    private int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     *
     * @param index
     */

    private void setIndex(final int index)
        {
        intIndex = index;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    private boolean isValid()
        {
        return boolValid;
        }


    /***********************************************************************************************
     *
     * @param valid
     */

    private void setValid(final boolean valid)
        {
        this.boolValid = valid;
        }
    }
