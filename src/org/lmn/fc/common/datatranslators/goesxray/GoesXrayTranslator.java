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

package org.lmn.fc.common.datatranslators.goesxray;

import org.lmn.fc.common.datatranslators.DataTranslator;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/***************************************************************************************************
    SAMPLE DATA from http://sec.noaa.gov/

    e.g. http://sec.noaa.gov/ftpdir/lists/xray/20070225_G12xr_5m.txt

    20070227_G12xr_5m.txt has negative values

     :Data_list: 20070208_G12xr_5m.txt
     :Created: 2007 Feb 09 0015 UTC
     # Prepared by the U.S. Dept. of Commerce, NOAA, Space Environment Center.
     # Please send comments and suggestions to SEC.Webmaster@noaa.gov
     #
     # Label: Short = 0.05- 0.4 nanometer
     # Label: Long  = 0.1 - 0.8 nanometer
     # Units: Short = Watts per meter squared
     # Units: Long  = Watts per meter squared
     # Source: GOES-12
     # Location: W075
     # Missing data: -1.00e+05
     #
     #                         GOES-12 Solar X-ray Flux
     #
     #                 Modified Seconds
     # UTC Date  Time   Julian  of the
     # YR MO DA  HHMM    Day     Day       Short       Long        Ratio
     #-------------------------------------------------------------------
     2007 02 08  0000   54139      0     1.98e-09    2.48e-08    7.96e-02
     2007 02 08  0005   54139    300     1.97e-09    2.60e-08    7.57e-02
     2007 02 08  0010   54139    600     1.97e-09    2.76e-08    7.13e-02
     2007 02 08  0015   54139    900     1.96e-09    2.64e-08    7.44e-02
     2007 02 08  0020   54139   1200     1.98e-09    2.52e-08    7.87e-02

***************************************************************************************************/

/***************************************************************************************************
 * The GoesXray Translator.
 */

public final class GoesXrayTranslator extends DataTranslator
                                      implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + "txt";

    private static final int CHANNEL_COUNT = 3;

    private static final int INDEX_YEAR = 0;
    private static final int INDEX_MONTH = 1;
    private static final int INDEX_DAY = GoesXrayTranslatorHelper.INDEX_HEADER_OBSERVATORY_NAME;
    private static final int INDEX_TIME = 3;
    private static final int INDEX_MJD = 4;
    private static final int INDEX_SEC = 5;
    private static final int INDEX_SHORT = 6;
    private static final int INDEX_LONG = 7;
    private static final int INDEX_RATIO = 8;
    private static final int TOKEN_COUNT = 9;

    private List<String> listHeader;


    /***********************************************************************************************
     * Construct a GoesXrayTranslator.
     */

    public GoesXrayTranslator()
        {
        // Xray flux is expressed as a double
        super();

        this.listHeader = new ArrayList<String>(GoesXrayTranslatorHelper.LINE_FIRST_DATA);
        }


    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     * Import data from the specified filename into the DataTranslator.
     * The output sample format is: <Calendar> <Channel0> <Channel1> <Channel2>
     *
     * Channel 0  0.05-0.4nm Wm-2
     * Channel 1  0.1-0.8nm Wm-2
     * Channel 2  ratio
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
        boolean boolSuccess;

        LOGGER.debug("Importing GOES Xray data from" + SPACE + filename);

        if ((filename != null)
            && (filename.endsWith(FILENAME_EXTENSION)))
            {
            try
                {
                final FileReader fileReader;
                final LineNumberReader lineNumberReader;
                String strLine;

                fileReader = new FileReader(filename);
                lineNumberReader = new LineNumberReader(fileReader);
                this.listHeader = new ArrayList<String>(GoesXrayTranslatorHelper.LINE_FIRST_DATA);
                setImportedCount(0L);

                do
                    {
                    final Vector<Object> vecSample;

                    vecSample = new Vector<Object>(4);
                    strLine = lineNumberReader.readLine();

                    if (strLine != null)
                        {
                        setImportedCount(getImportedCount() + strLine.length());

                        //LOGGER.debug(lineNumberReader.getLineNumber() + ": " + strLine);

                        if (lineNumberReader.getLineNumber() < GoesXrayTranslatorHelper.LINE_FIRST_DATA)
                            {
                            // Accumulate the header to extract the meta data later
                            getHeader().add(strLine);
                            }
                        else
                            {
                            final String[] tokens;
                            final int intYear;
                            final int intMonth;
                            final int intDay;
                            final int intHour;
                            final int intMin;
                            final int intSec;
                            final Calendar calendar;

                            // Process the data
                            // Greedily remove any whitespace between tokens
                            tokens = strLine.split("\\s+");

                            // Form the timestamp
                            intYear = Integer.parseInt(tokens[INDEX_YEAR]);

                            // Remember that months are zero-based!!
                            intMonth = Integer.parseInt(tokens[INDEX_MONTH]) - 1;
                            intDay = Integer.parseInt(tokens[INDEX_DAY]);

                            intHour = Integer.parseInt(tokens[INDEX_TIME].substring(0, GoesXrayTranslatorHelper.INDEX_HEADER_OBSERVATORY_NAME));
                            intMin = Integer.parseInt(tokens[INDEX_TIME].substring(GoesXrayTranslatorHelper.INDEX_HEADER_OBSERVATORY_NAME));
                            intSec = 0;

                            // Perform some simple validation on the Date and Time
                            // ToDo Improve validation
                            if ((intYear >= 1900)
                                && (intYear <= 2050)
                                && (intDay >= 1)
                                && (intDay <= 31)
                                && (intHour >= 0)
                                && (intHour <= 23)
                                && (intMin >= 0)
                                && (intMin <= 59))
                                {
                                calendar = new GregorianCalendar(intYear,
                                                                 intMonth,
                                                                 intDay,
                                                                 intHour,
                                                                 intMin,
                                                                 intSec);
                                // The Times are known to be in UT
                                calendar.setTimeZone(REGISTRY.getGMTTimeZone());
                                vecSample.add(calendar);

                                // Add the channel data
                                vecSample.add(Double.parseDouble(tokens[INDEX_SHORT]));
                                vecSample.add(Double.parseDouble(tokens[INDEX_LONG]));

                                // 2013-04-26 Allow import of both 5 minute and 1 minute data, which have different formats!
                                if (tokens.length == TOKEN_COUNT)
                                    {
                                    vecSample.add(Double.parseDouble(tokens[INDEX_RATIO]));
                                    }
                                else
                                    {
                                    vecSample.add((Double.parseDouble(tokens[INDEX_SHORT]) / Double.parseDouble(tokens[INDEX_LONG])));
                                    }

                                // If we get this far, it is a valid sample,
                                // so add to the DataTranslator superclass
                                // The sample format is: <Calendar> <Channel0> <Channel1> <Channel2>
                                addRawDataSample(vecSample);
                                }
                            else
                                {
                                throw new NumberFormatException(EXCEPTION_PARAMETER_INVALID);
                                }
                            }
                        }
                    }
                while (strLine != null);

                // Check that we have read at least one line of real data
                // and a full Header
                if ((lineNumberReader.getLineNumber() < GoesXrayTranslatorHelper.LINE_FIRST_DATA)
                    || (getHeader() == null)
                    || (getHeader().size() != (GoesXrayTranslatorHelper.LINE_FIRST_DATA - 1)))
                    {
                    addMessage(METADATA_TARGET_RAWDATA
                                   + METADATA_ACTION_TRANSLATOR
                                   + METADATA_RESULT
                                   + "insufficient data to process"
                                   + TERMINATOR);
                    boolSuccess = false;
                    }
                else
                    {
                    setRawDataChannelCount(CHANNEL_COUNT);
                    setTemperatureChannel(false);

                    GoesXrayTranslatorHelper.addMetadataFromHeader(this, getHeader(), GoesXrayTranslatorHelper.extractObservationDate(filename));
                    GoesXrayTranslatorHelper.addChannelColourMetadata(this);

                    addMessage(METADATA_TARGET_RAWDATA
                                + METADATA_ACTION_TRANSLATOR
                                + METADATA_COUNT
                                + getRawData().size()
                                + TERMINATOR);
                    boolSuccess = true;

                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "GOESXRAY TRANSLATOR IMPORT CHANNEL COUNT="
                                            + getRawDataChannelCount()
                                            + " TEMPERATURE CHANNEL=" + hasTemperatureChannel());
                    }
                }

            catch(NumberFormatException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_IMPORT
                               + METADATA_EXCEPTION
                               + exception.getMessage()
                               + TERMINATOR);
                boolSuccess = false;
                }

            catch(FileNotFoundException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_IMPORT
                               + METADATA_EXCEPTION
                               + exception.getMessage()
                               + TERMINATOR);
                boolSuccess = false;
                }

            catch(IOException exception)
                {
                addMessage(METADATA_TARGET_RAWDATA
                               + METADATA_ACTION_IMPORT
                               + METADATA_EXCEPTION
                               + exception.getMessage()
                               + TERMINATOR);
                boolSuccess = false;
                }
            }
        else
            {
            addMessage(METADATA_TARGET_RAWDATA
                            + METADATA_ACTION_IMPORT
                            + METADATA_RESULT + "Invalid filename" + TERMINATOR_SPACE
                            + METADATA_FILENAME
                            + filename
                            + TERMINATOR);
            boolSuccess = false;
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

    public boolean exportMetadata(final List<Metadata> metadatametadata,
                                  final List<Metadata> metadata,
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
     * Get all the lines of the Header.
     *
     * @return List<String>
     */

    private List<String> getHeader()
        {
        return (this.listHeader);
        }
    }
