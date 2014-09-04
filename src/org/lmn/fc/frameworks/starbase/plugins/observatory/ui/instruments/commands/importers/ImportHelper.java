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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;


import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;

import java.util.List;
import java.util.regex.Pattern;


/***************************************************************************************************
 * ImportHelper.
 */

public final class ImportHelper implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkRegex,
                                           FrameworkSingletons,
                                           ResourceKeys
    {
    /***********************************************************************************************
     * Process the imported data.
     * Assume all remotely imported data should be marked as Unsaved,
     * but don't mark local data as Unsaved.
     *
     * @param dao
     * @param timeseriescollection
     * @param filter
     * @param datatypes
     * @param markunsaved
     * @param debug
     */

    public static void processImportedData(final ObservatoryInstrumentDAOInterface dao,
                                           final TimeSeriesCollection timeseriescollection,
                                           final DataFilterInterface filter,
                                           final List<DataTypeDictionary> datatypes,
                                           final boolean markunsaved,
                                           final boolean debug)
        {
        final String SOURCE = "ImportHelper.processImportedData() ";

        // See if there's anything we need to know...
        DataTranslatorHelper.addTranslatorMessages(dao.getTranslator(),
                                                   dao.getEventLogFragment(),
                                                   dao.getObservatoryClock(),
                                                   dao.getLocalHostname());
        // Check that we are still running,
        // to save time if the User has stopped the Instrument
        if (InstrumentState.isDoingSomething(dao.getHostInstrument()))
            {
            LOGGER.debug(debug,
                         DataTranslatorHelper.showTranslatedData(dao.getTranslator(), SHOW_DEBUG_LINECOUNT).toString());

            // Set the RawData and associated Metadata from the Translator
            // Assume all remotely imported data should be marked as Unsaved,
            // but don't mark local data as Unsaved
            dao.deriveDAOIdentityFromImport(dao.getTranslator(), markunsaved);

            // Filter each channel of data if required
            if (filter != null)
                {
                // What kind of data file is it?
                if (DataAnalyser.isCalendarisedRawData(dao.getRawData()))
                    {
                    // The data format is: <Calendar> <Channel0> <Channel1> <Channel2>
                    // The supplied Metadata MUST contain the Observation.Channel.Name, so look in ObservationMetadata
                    DataFilterHelper.filterCalendarisedRawDataToTimeSeries(dao,
                                                                           filter,
                                                                           filter.getFilterType(),
                                                                           datatypes,
                                                                           dao.getObservationMetadata(),
                                                                           timeseriescollection);
                    }
                else if (DataAnalyser.isColumnarRawData(dao.getRawData()))
                    {
                    // The data format is: <Channel0> <Channel1> <Channel2>
                    // The supplied Metadata MUST contain the Observation.Channel.Name, so look in ObservationMetadata
                    DataFilterHelper.filterColumnarRawDataToXYDataset(dao,
                                                                      filter,
                                                                      filter.getFilterType(),
                                                                      datatypes,
                                                                      dao.getObservationMetadata());
                    }
                else
                    {
                    // We don't understand the data format
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_RESULT + "Unsupported data format" + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }
                }
            else
                {
                // No filter is available
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_RESULT + "Unable to instantiate the DataFilter" + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_IMPORT
                                                   + METADATA_RESULT + "The instrument is not running" + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            }
        }


    /***********************************************************************************************
     * Interpret the format of the requested download filename.
     * If the name starts with the date pattern '20YYMMDD' then use the name literally.
     * Otherwise, prepend today's date using that pattern.
     * Note that this will not work after 2099!
     *
     * @param filename
     * @param clock
     *
     * @return String
     */

    public static String interpretGOESFilename(final String filename,
                                               final ObservatoryClockInterface clock)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer(filename);

        // Try to match 20YYMMDD from 20070225_G12xr_5m.txt
        if (!Pattern.matches(FrameworkRegex.REGEX_DATE_YYMMDD, filename.substring(0, 8)))
            {
            String strDate;

            // This returns 2007-05-16...
            strDate = ChronosHelper.toDateString(clock.getCalendarDateNow());
            strDate = strDate.replace("-", "");

            // ...leaving 20070516
            strDate = strDate + "_";
            buffer.insert(0, strDate);
            }

        return (buffer.toString());
        }
    }
