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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.common.utilities.time.AstronomicalCalendar;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ntp.NtpConnection;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ntp.NtpData;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;


/***************************************************************************************************
 * ObservatoryClockHelper.
 */

public final class ObservatoryClockHelper implements FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkRegex,
                                                     FrameworkSingletons
    {
    // String Resources
    private static final String TITLE_CLOCK_OFFSET = "Offset between Observatory Clock and network time";
    private static final String LABEL_ELAPSED_TIME = "Elapsed Time";
    private static final String LABEL_OFFSET_MSEC  = "Offset msec";
    private static final String DESCRIPTION_OFFSET = "The latest value of the time offset";
    private static final String CHANNEL_COLOUR = "r=255 g=000 b=000";
    private static final String KEY_CHANNEL_DELTA_T = MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO;

    private static final int MAX_OFFSET_MILLISEC = 100000;


    /***********************************************************************************************
     * Get the NTP time offset in milliseconds, using NTP to connect to the specified servers.
     * Add an entry in the TimeSeries to record successful synchronisation.
     * Return Long.MAX_VALUE if synchronisation failed.
     *
     * @param serverdefault
     * @param server1
     * @param server2
     * @param server3
     * @param timeoutperiodmillis
     * @param allowadd
     * @param timeseries
     * @param log
     * @param timezone
     * @param locale
     * @param obsclock
     *
     * @return long
     */

    public static long getNTPOffsetMillis(final String serverdefault,
                                          final String server1,
                                          final String server2,
                                          final String server3,
                                          final int timeoutperiodmillis,
                                          final boolean allowadd,
                                          final TimeSeries timeseries,
                                          final Vector<Vector> log,
                                          final TimeZone timezone,
                                          final Locale locale,
                                          final ObservatoryClockInterface obsclock)
        {
        final String SOURCE = "ObservatoryClockHelper.getNTPOffsetMillis()";
        final Vector<String> vecServers;
        final Iterator iterServers;
        final GregorianCalendar calNTP;
        long longNTPOffsetMillis;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(), SOURCE);

        // A calendar in which to store NTP time
        // Do not use a reference to the ObservatoryClock calendar!
        calNTP = new GregorianCalendar();

        // Organise all available Time servers
        vecServers = new Vector<String>(4);
        vecServers.add(serverdefault);
        vecServers.add(server1);
        vecServers.add(server2);
        vecServers.add(server3);

        // Return Long.MAX_VALUE if synchronisation failed
        longNTPOffsetMillis = Long.MAX_VALUE;

        // No Time yet!
        boolSuccess = false;

        // Cycle through the available servers if we get a timeout etc.
        iterServers = vecServers.iterator();

        while ((iterServers.hasNext())
            && (!boolSuccess))
            {
            NtpConnection ntpConnection;
            final NtpData ntpData;
            final String strServerName;

            ntpConnection = null;

            strServerName = (String) iterServers.next();

            if ((strServerName != null)
                && (!EMPTY_STRING.equals(strServerName.trim()))
                && (vecServers.size() > 0))
                {
                try
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           SOURCE + " Trying to connect to NTP server " + strServerName);

                    // Attempt to get an NtpConnection and read the NtpData
                    // Use the ObservatoryClock for Timestamps!
                    ntpConnection = new NtpConnection(InetAddress.getByName(strServerName),
                                                      timezone,
                                                      locale,
                                                      obsclock);

                    // Cater for the worst-case timeout where all servers don't respond
                    ntpConnection.setTimeoutMillis(timeoutperiodmillis / vecServers.size());

                    ntpData = ntpConnection.getNtpData();

                    if ((ntpData != null)
                        && (ntpData.getServerAddress() != null))
                        {
                        longNTPOffsetMillis = ntpData.getOffset();

                        // Save the Time wrapped in a Date, for the TimeSeries
                        calNTP.setTimeInMillis(obsclock.getSystemTimeMillis() + longNTPOffsetMillis);

//                        System.out.println("\nxObservatoryClock CURRENTTIME=" + obsclock.getSystemTimeMillis());
//                        System.out.println("ObservatoryClock OFFSET=" + longNTPOffsetMillis);

                        // Record the offset vs Time if we can
                        if ((longNTPOffsetMillis < MAX_OFFSET_MILLISEC)
                            && (timeseries != null)
                            && (allowadd))
                            {
                            timeseries.addOrUpdate(new Second(calNTP.getTime(),
                                                              calNTP.getTimeZone()),
                                                   longNTPOffsetMillis);
                            }

//                        SimpleEventLogUIComponent.logEvent(log,
//                                                           EventStatus.INFO,
//                                                           FrameworkMetadata.METADATA_TARGET_CLOCK
//                                                                   + FrameworkMetadata.METADATA_ACTION_SYNCHRONISE
//                                                                   + FrameworkMetadata.METADATA_OFFSET + longNTPOffsetMillis + FrameworkMetadata.TERMINATOR,
//                                                           SOURCE,
//                                                           obsclock);

                        // Time to leave!
                        boolSuccess = true;
                        }
                    }

                catch (SocketTimeoutException exception)
                    {
                    LOGGER.error(SOURCE + SPACE + exception.getMessage());
                    }

                catch (SocketException exception)
                    {
                    LOGGER.error(SOURCE + SPACE + exception.getMessage());
                    }

                catch (UnknownHostException exception)
                    {
                    LOGGER.error(SOURCE + SPACE + exception.getMessage());
                    }

                catch (IOException exception)
                    {
                    LOGGER.error(SOURCE + SPACE + exception.getMessage());
                    }

                catch (NullPointerException exception)
                    {
                    LOGGER.error(SOURCE + SPACE + exception.getMessage());
                    }

                finally
                    {
                    // Always close the connection for this server
                    if (ntpConnection != null)
                        {
                        ntpConnection.close();
                        }
                    }
                }
            }

        // Return Long.MAX_VALUE if synchronisation failed
        if (!boolSuccess)
            {
            SimpleEventLogUIComponent.logEvent(log,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_CLOCK
                                                       + METADATA_ACTION_SYNCHRONISE
                                                       + METADATA_RESULT + "Failed to get time offset" + TERMINATOR,
                                               SOURCE,
                                               obsclock);
            }

        return (longNTPOffsetMillis);
        }


    /***********************************************************************************************
     * Create the Metadata to describe the Clock Offset Channels.
     * Each Channel requires {Name, Value, DataType, Units, Description, Colour}.
     * These are currently added to the DAO ObservationMetadata.
     *
     * For correct rendering and export, Charts must receive the following metadata:
     *
     * Observation.Channel.Name.n
     * Observation.Channel.DataType.n
     * Observation.Channel.Units.n
     * Observation.Channel.Description.n
     * Observation.Channel.Colour.n
     * Observation.Title
     * Observation.Axis.Label.X
     * Observation.Axis.Label.Y.n
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createClockOffsetChannelMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(9);

        // Channel Metadata
        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      LABEL_OFFSET_MSEC,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      TITLE_CLOCK_OFFSET);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      SchemaDataType.DECIMAL_INTEGER.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      SchemaUnits.MSEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      TITLE_CLOCK_OFFSET,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      CHANNEL_COLOUR,
                                      REGEX_COLOUR,
                                      DataTypeDictionary.COLOUR_DATA,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        // Chart Metadata
        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      TITLE_CLOCK_OFFSET,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      LABEL_ELAPSED_TIME,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      LABEL_OFFSET_MSEC,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      KEY_CHANNEL_DELTA_T,
                                      "0",
                                      REGEX_NONE,
                                      MetadataHelper.getChannelDataType(listMetadata, 0, false),
                                      MetadataHelper.getChannelUnits(listMetadata, 0, false),
                                      DESCRIPTION_OFFSET);

        return (listMetadata);
        }


    /***********************************************************************************************
     * Create a new AstronomicalCalendar for clocks.
     * Return NULL if no ObservatoryClock is available.
     *
     * @param clock
     *
     * @return AstronomicalCalendarInterface
     */

    public static AstronomicalCalendarInterface createCalendar(final ObservatoryClockInterface clock)
        {
        AstronomicalCalendarInterface calendarClocks;

        calendarClocks = null;

        if (clock != null)
            {
            final TimeZone timeZone;
            final Locale locale;
            final double dblLongitude;

            timeZone = REGISTRY.getFrameworkTimeZone();
            locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                REGISTRY.getFramework().getCountryISOCode());

            // Find the (latest) Observatory Longitude
            dblLongitude = REGISTRY.getFramework().getLongitude().toDouble();

            // Recreate the Calendar now in case we moved
            //System.out.println("CREATE ASTRO CALENDAR *******************************");
            calendarClocks = new AstronomicalCalendar(clock.getSystemCalendar(timeZone, locale),
                                                      dblLongitude);
            calendarClocks.enableFormatSign(false);
            }

        return (calendarClocks);
        }
    }
