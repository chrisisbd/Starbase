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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.ntpclient.dao;

import org.apache.xmlbeans.XmlObject;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.net.ntp.NtpConnection;
import org.lmn.fc.common.net.ntp.NtpData;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.ntpclient.NtpClientHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.ui.ntp.NtpLog;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;


/***************************************************************************************************
 * NtpDAO.
 */

public final class NtpDAO extends AbstractObservatoryInstrumentDAO
                          implements ObservatoryInstrumentDAOInterface
    {
    // String Resources
    private static final String LOG_TIME_SET_OK                 = "Observatory Clock set successfully";
    private static final String LOG_TIME_SET_FAILED             = "Time not set because feature not available in current Clock";
    private static final String LOG_TIME_SET_DISABLED           = "Time not set at request of User";
    private static final String LOG_TIME_NO_DATA                = "Time not set because server did not provide data";
    private static final String LOG_TIME_SET_SOCKET_ERROR       = "Time not set because of SocketException";
    private static final String LOG_TIME_SET_UNKNOWN_HOST       = "Time not set because of UnknownHostException";
    private static final String LOG_TIME_SET_IO_ERROR           = "Time not set because of IOException";
    private static final String LOG_TIME_SET_NULL               = "Time not set because of Null";

    private static final int MAX_OFFSET_MILLISEC = 100000;
    private static final int DAO_CHANNEL_COUNT = 1;
    private static final int DAO_SERVER_COUNT = 4;

    private final TimeSeriesCollection collectionTimeSeries;
    private final TimeSeries seriesSamples;

    // Configurable Resources
    //
    // KEY_DAO_NTP_SERVER_DEFAULT
    // KEY_DAO_NTP_SERVER_1
    // KEY_DAO_NTP_SERVER_2
    // KEY_DAO_NTP_SERVER_3

    private String urlServerDefault;
    private String urlServer1;
    private String urlServer2;
    private String urlServer3;


    /***********************************************************************************************
     * Add a null InstrumentLog Entry just showing the server and status,
     * but without the sorting index column.
     * There should be NtpLog.COLUMN_COUNT columns.
     *
     * @param logentry
     * @param server
     * @param status
     */

    private static void setNullInstrumentLogEntry(final Vector<Object> logentry,
                                                  final String server,
                                                  final String status)
        {
        logentry.clear();

        logentry.add(null);
        logentry.add(false);
        logentry.add(null);
        logentry.add(null);
        logentry.add(server);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(status);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        }


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("getNtpDatagram");
        pool.add("captureClockOffset");
        pool.add("exportClockOffset");
        }


    /***********************************************************************************************
     * Construct an NtpDAO.
     *
     * @param hostinstrument
     */

    public NtpDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        // Make a TimeSeries based on Seconds and place into a Collection...
        this.seriesSamples = new TimeSeries(NtpClientHelper.TITLE_OBSERVATORY_CLOCK_OFFSET);
        this.collectionTimeSeries = new TimeSeriesCollection(getTimeSeries());

        this.urlServerDefault = NtpConnection.DEFAULT_NTP_SERVER;
        this.urlServer1 = NtpConnection.DEFAULT_NTP_SERVER;
        this.urlServer2 = NtpConnection.DEFAULT_NTP_SERVER;
        this.urlServer3 = NtpConnection.DEFAULT_NTP_SERVER;

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "NtpDAO.initialiseDAO() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        getTimeSeries().clear();

        // Add some Metadata to the result
        addAllMetadataToContainersTraced(NtpClientHelper.createNtpClientChannelMetadata(NtpClientHelper.TITLE_PLATFORM_CLOCK_OFFSET,
                                                                                        LOADER_PROPERTIES.isMetadataDebug()),
                                         SOURCE,
                                         LOADER_PROPERTIES.isMetadataDebug());
        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "NtpDAO.disposeDAO()");

        getTimeSeriesCollection().removeAllSeries();

        super.disposeDAO();
        }


    /***********************************************************************************************
     * reset() resets the whole Instrument.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface reset(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NtpDAO.reset() ";
        final ResponseMessageInterface response;

        response = super.reset(commandmessage);

        // Do this here rather than in the Instrument to avoid having to expose the TimeSeries
        // Only reset this DAO to DEFAULTS if all else worked...
        if ((ResetMode.DEFAULTS.equals(DAOHelper.extractResetMode(commandmessage.getCommandType())))
            && (response != null)
            && (response.getResponseMessageStatusList() != null)
            && (response.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS)))
            {
            getTimeSeries().clear();

            // Put the Timeout back to what it should be for a single default command
            TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(this);
            }

        return (response);
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return (new NtpCommandMessage(dao,
                                      instrumentxml,
                                      module,
                                      command,
                                      starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new NtpResponseMessage(portname,
                                       instrumentxml,
                                       module,
                                       command,
                                       starscript.trim(),
                                       responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * getNtpDatagram().
     * Get a single NtpDatagram from the NTP server.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getNtpDatagram(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NtpDAO.getNtpDatagram()";
        final int PARAMETER_COUNT = 1;
        final int INDEX_SYNCHRONISE = 0;
        final CommandType cmdGetDatagram;
        final List<ParameterType> listParameters;
        final Vector<String> vecServers;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdGetDatagram = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        // We expect one parameter, a control boolean
        listParameters = cmdGetDatagram.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_SYNCHRONISE) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_SYNCHRONISE).getInputDataType().getDataTypeName())))
            {
            final TimeZone timeZone;
            final Locale locale;
            final boolean boolSynchronise;
            final Iterator iterServers;
            boolean boolTimeValid;

            // ToDo REVIEW - should these be for the Observatory?
            timeZone = REGISTRY.getFrameworkTimeZone();
            locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                REGISTRY.getFramework().getCountryISOCode());

            boolSynchronise = Boolean.parseBoolean(listParameters.get(INDEX_SYNCHRONISE).getValue());

            addAllMetadataToContainersTraced(NtpClientHelper.createNtpClientChannelMetadata(NtpClientHelper.TITLE_OBSERVATORY_CLOCK_OFFSET,
                                                                                            boolDebug),
                                             SOURCE,
                                             boolDebug);

            // Organise all available Time servers
            vecServers = new Vector<String>(4);
            vecServers.add(urlServerDefault);
            vecServers.add(urlServer1);
            vecServers.add(urlServer2);
            vecServers.add(urlServer3);

            // No Time yet!
            boolTimeValid = false;
            strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;

            // Cycle through the available servers if we get a timeout etc.
            iterServers = vecServers.iterator();

            while ((iterServers.hasNext())
                    && (Utilities.executeWorkerCanProceed(this))
                    && (!boolTimeValid))
                {
                NtpConnection ntpConnection;
                final NtpData ntpData;
                final GregorianCalendar calNTP;
                final Calendar calTimeStamp;
                final ImageIcon imageIcon;
                final Vector<Object> vecInstrumentLogEntry;
                final String strServerName;

                // Prepare a Log entry for each server attempt
                vecInstrumentLogEntry = new Vector<Object>(NtpLog.EVENT_LOG_WIDTH);
                ntpConnection = null;

                strServerName = (String) iterServers.next();

                if ((strServerName != null)
                    && (!EMPTY_STRING.equals(strServerName.trim())))
                    {
                    try
                        {
                        LOGGER.debugTimedEvent(boolDebug,
                                               "Trying to connect to NTP server " + strServerName);

                        // Attempt to get an NtpConnection and read the NtpData
                        ntpConnection = new NtpConnection(InetAddress.getByName(strServerName));

                        // Cater for the worst-case timeout where all servers don't respond
                        ntpConnection.setTimeout(getTimeoutMillis(commandmessage.getModule(), cmdGetDatagram) / vecServers.size());

                        ntpData = ntpConnection.getNtpData();

                        if ((ntpData != null)
                            && (ntpData.getServerAddress() != null)
                            && (getTimeSeries() != null))
                            {
                            final long longOriginalNTPOffset;
                            final long longAdjustedNTPOffset;

                            longOriginalNTPOffset = ntpData.getOffset();

                            // WARNING! The NTP TimeStamps are referred to the **Platform Clock** NOT the ObservatoryClock!
                            // We need to adjust the offset to be between the ObservatoryClock and the (PlatformClock + NTPOffset)
                            // In the case where the Proxy is not running, or the Clock is the SimplePlatformClock,
                            // the offset will reduce to the NTPOffset, as it should...
                            longAdjustedNTPOffset = getObservatoryClock().getSystemTimeMillis()
                                                      - (System.currentTimeMillis() + longOriginalNTPOffset);

                            // Set the Local Time if required
                            if (boolSynchronise)
                                {
                                final boolean boolSetSuccess;

                                LOGGER.debugTimedEvent(boolDebug,
                                                       "NTP attempting to set the time on the host platform...");

                                // Do not use a reference to the ObservatoryClock calendar!
                                // ToDo WARNING! If the ObservatoryClock is adjusted backwards in time,
                                // ToDo then the sequence of events on the Chart will be incorrect. Not sure how to fix this.
                                calNTP = new GregorianCalendar(timeZone, locale);
                                calNTP.setTimeInMillis(getObservatoryClock().getSystemTimeMillis() + longAdjustedNTPOffset);

                                // Sync the ObservatoryClock proxy, if possible with the current ObservatoryClockDAO
                                // The first call gets the ObservatoryClock *Instrument*, but as an ObservatoryClockInterface
                                // The second gets the instantiated DAO for that Instrument (SimplePlatformClockDAO or NTPSynchronisedProxyClockDAO)
                                // The third gets the synthesised Clock, which is to do the synchronise()
                                // This simplification was suggested by IntelliJ :-)
                                boolSetSuccess = (getObservatoryClock() != null)
                                                 && (getObservatoryClock().getClockDAO() != null)
                                                 && (getObservatoryClock().getClockDAO().getObservatoryClock() != null)
                                                 && getObservatoryClock().getClockDAO().getObservatoryClock().synchronise(longAdjustedNTPOffset,
                                                                                                                          getTimeSeries(),
                                                                                                                          timeZone,
                                                                                                                          locale,
                                                                                                                          true,
                                                                                                                          getEventLogFragment());
                                // Record the valid Time details in the Log
                                imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.INFO.getIconFilename());
                                vecInstrumentLogEntry.add(imageIcon);
                                vecInstrumentLogEntry.add(true);
                                vecInstrumentLogEntry.add(ChronosHelper.toDateString(calNTP));
                                vecInstrumentLogEntry.add(ChronosHelper.toTimeString(calNTP));
                                vecInstrumentLogEntry.add(strServerName);
                                vecInstrumentLogEntry.add(ntpData.getServerAddress().toString());
                                vecInstrumentLogEntry.add(Integer.toString(ntpData.getVersionNumber()));
                                vecInstrumentLogEntry.add(Long.toString(longAdjustedNTPOffset));
                                vecInstrumentLogEntry.add(Long.toString(ntpData.getRoundTripDelay()));
                                vecInstrumentLogEntry.add(Integer.toString(ntpData.getStratum()));
                                vecInstrumentLogEntry.add(Double.toString(ntpData.getPrecision()));

                                if (boolSetSuccess)
                                    {
                                    vecInstrumentLogEntry.add(LOG_TIME_SET_OK);
                                    }
                                else
                                    {
                                    vecInstrumentLogEntry.add(LOG_TIME_SET_FAILED);
                                    }

                                calTimeStamp = ntpData.getReferenceTimeStamp().toCalendar();
                                vecInstrumentLogEntry.add(ChronosHelper.toDateString(calTimeStamp));
                                vecInstrumentLogEntry.add(ChronosHelper.toTimeString(calTimeStamp));
                                vecInstrumentLogEntry.add(Double.toString(ntpData.getReferenceTimeStamp().getFractionalPart()));
                                getInstrumentLogFragment().add(vecInstrumentLogEntry);

                                // Record the offset vs Time
                                if (Math.abs(longAdjustedNTPOffset) < MAX_OFFSET_MILLISEC)
                                    {
                                    // ToDo addOrUpdate() Requires Locale
                                    getTimeSeries().addOrUpdate(new Second(calNTP.getTime(),
                                                                           calNTP.getTimeZone()),
                                                                longAdjustedNTPOffset);
                                    }
                                }
                            else
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       "NTP logging the time...");

                                // Save the Time wrapped in a Date
                                // Do not use a reference to the ObservatoryClock calendar!
                                calNTP = new GregorianCalendar(timeZone, locale);
                                calNTP.setTimeInMillis(getObservatoryClock().getSystemTimeMillis() + longAdjustedNTPOffset);

                                // Record the valid Time details in the Log
                                imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());
                                vecInstrumentLogEntry.add(imageIcon);
                                vecInstrumentLogEntry.add(false);
                                vecInstrumentLogEntry.add(ChronosHelper.toDateString(calNTP));
                                vecInstrumentLogEntry.add(ChronosHelper.toTimeString(calNTP));
                                vecInstrumentLogEntry.add(strServerName);
                                vecInstrumentLogEntry.add(ntpData.getServerAddress().toString());
                                vecInstrumentLogEntry.add(Integer.toString(ntpData.getVersionNumber()));
                                vecInstrumentLogEntry.add(Long.toString(longAdjustedNTPOffset));
                                vecInstrumentLogEntry.add(Long.toString(ntpData.getRoundTripDelay()));
                                vecInstrumentLogEntry.add(Integer.toString(ntpData.getStratum()));
                                vecInstrumentLogEntry.add(Double.toString(ntpData.getPrecision()));
                                vecInstrumentLogEntry.add(LOG_TIME_SET_DISABLED);

                                calTimeStamp = ntpData.getReferenceTimeStamp().toCalendar();
                                vecInstrumentLogEntry.add(ChronosHelper.toDateString(calTimeStamp));
                                vecInstrumentLogEntry.add(ChronosHelper.toTimeString(calTimeStamp));
                                vecInstrumentLogEntry.add(Double.toString(ntpData.getReferenceTimeStamp().getFractionalPart()));
                                getInstrumentLogFragment().add(vecInstrumentLogEntry);

                                // Record the offset vs Time
                                if (Math.abs(longAdjustedNTPOffset) < MAX_OFFSET_MILLISEC)
                                    {
                                    // ToDo addOrUpdate() Requires Locale
                                    getTimeSeries().addOrUpdate(new Second(calNTP.getTime(),
                                                                           calNTP.getTimeZone()),
                                                                longAdjustedNTPOffset);
                                    }
                                }

                            // Put the time offset on the ControlPanel display via the Metadata
                            NtpClientHelper.setNtpOffsetMetadataValue(getObservationMetadata(), longAdjustedNTPOffset);

                            // This is also the simple ResponseValue
                            strResponseValue = Long.toString(longAdjustedNTPOffset);
                            getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

                            // Time to leave!
                            boolTimeValid = true;
                            }
                        else
                            {
                            // We cannot read a valid NtpData for this server
                            setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                      strServerName,
                                                      LOG_TIME_NO_DATA);
                            getInstrumentLogFragment().add(vecInstrumentLogEntry);
                            }
                        }

                    catch (SocketTimeoutException exception)
                        {
                        setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                  strServerName,
                                                  LOG_TIME_SET_SOCKET_ERROR);
                        getInstrumentLogFragment().add(vecInstrumentLogEntry);
                        }

                    catch (SocketException exception)
                        {
                        setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                  strServerName,
                                                  LOG_TIME_SET_SOCKET_ERROR);
                        getInstrumentLogFragment().add(vecInstrumentLogEntry);
                        }

                    catch (UnknownHostException exception)
                        {
                        setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                  strServerName,
                                                  LOG_TIME_SET_UNKNOWN_HOST);
                        getInstrumentLogFragment().add(vecInstrumentLogEntry);
                        }

                    catch (IOException exception)
                        {
                        setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                  strServerName,
                                                  LOG_TIME_SET_IO_ERROR);
                        getInstrumentLogFragment().add(vecInstrumentLogEntry);
                        }

                    catch (NullPointerException exception)
                        {
                        setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                  strServerName,
                                                  LOG_TIME_SET_NULL);
                        getInstrumentLogFragment().add(vecInstrumentLogEntry);
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
            }
        else
            {
            // The XML configuration was inappropriate
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(this,
                                                                                SOURCE,
                                                                                METADATA_TARGET
                                                                                + SOURCE.trim()
                                                                                + TERMINATOR,
                                                                                METADATA_ACTION_COMMAND));
            }

        // Something has changed, we may need to refresh a ControlPanel etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

        responseMessage = ResponseMessageHelper.createResponseMessage(this,
                                                                      commandmessage,
                                                                      cmdGetDatagram,
                                                                      null,
                                                                      getTimeSeriesCollection(),
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * captureClockOffset().
     * This is a capture Command.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureClockOffset(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NtpDAO.captureClockOffset() ";
        final int PARAMETER_COUNT = 2;
        final int INDEX_INTERVAL = 0;
        final int INDEX_PERIOD = 1;
        final CommandType cmdCaptureOffset;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdCaptureOffset = (CommandType)commandmessage.getCommandType().copy();

        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdCaptureOffset),
                                       DAO_CHANNEL_COUNT,
                                       false,
                                       NtpClientHelper.createNtpClientChannelMetadata(NtpClientHelper.TITLE_PLATFORM_CLOCK_OFFSET,
                                                                                      LOADER_PROPERTIES.isMetadataDebug()),
                                       null);

        strResponseValue = EMPTY_STRING;

        if (getTimeSeries() != null)
            {
            getTimeSeries().clear();
            }

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        // We expect two parameters, the Interval and Period
        listParameters = cmdCaptureOffset.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_INTERVAL) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_INTERVAL).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_PERIOD) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_PERIOD).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strClockUpdateInterval;
                final String strClockUpdatePeriod;
                final int intClockUpdateIntervalSec;
                int intClockUpdatePeriodSec;

                strClockUpdateInterval = listParameters.get(INDEX_INTERVAL).getValue();
                intClockUpdateIntervalSec = Integer.parseInt(strClockUpdateInterval);

                strClockUpdatePeriod = listParameters.get(INDEX_PERIOD).getValue();
                intClockUpdatePeriodSec = Integer.parseInt(strClockUpdatePeriod);

                // Check for silly parameter settings (should have been trapped by Regex)
                // ClockUpdatePeriod = 0 means run continuously
                if ((intClockUpdateIntervalSec > 0)
                    && ((intClockUpdatePeriodSec == 0)
                        || (intClockUpdatePeriodSec >= intClockUpdateIntervalSec)))
                    {
                    final ImageIcon imageIcon;
                    final Vector<String> vecServers;
                    final String strPeriod;
                    final int intCaptureCountMax;
                    final Vector<Object> vecInstrumentLogEntry;

                    imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());

                    // Organise all available Time servers
                    vecServers = new Vector<String>(DAO_SERVER_COUNT);
                    vecServers.add(urlServerDefault);
                    vecServers.add(urlServer1);
                    vecServers.add(urlServer2);
                    vecServers.add(urlServer3);

                    if (intClockUpdatePeriodSec == 0)
                        {
                        strPeriod = CaptureCommandHelper.MSG_PERIOD_CONTINUOUS;
                        }
                    else
                        {
                        strPeriod = Integer.toString(intClockUpdatePeriodSec);
                        }

                    // Set up some basic ObservationMetadata
                    // Location Metadata will be added on a valid fix
                    addAllMetadataToContainersTraced(NtpClientHelper.createNtpClientChannelMetadata(NtpClientHelper.TITLE_PLATFORM_CLOCK_OFFSET,
                                                                                                    LOADER_PROPERTIES.isMetadataDebug()),
                                                     SOURCE,
                                                     LOADER_PROPERTIES.isMetadataDebug());

                    // Log the validated parameters we are going to use
                    SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_CLOCK_OFFSET
                                                           + METADATA_ACTION_CAPTURE
                                                           + METADATA_INTERVAL + intClockUpdateIntervalSec + TERMINATOR_SPACE
                                                           + METADATA_PERIOD + strPeriod + TERMINATOR,
                                                       getLocalHostname(),
                                                       getObservatoryClock());

                    // Now correct the loop limit for continuous operation
                    if (intClockUpdatePeriodSec == 0)
                        {
                        // Allow about 10 days of operation! (Could be Integer.MAX_VALUE?)
                        intClockUpdatePeriodSec = 1000000;

                        // We musn't timeout, since this might run forever...
                        TimeoutHelper.restartDAOTimeoutTimerInfinite(this);
                        }

                    intCaptureCountMax = intClockUpdatePeriodSec / intClockUpdateIntervalSec;

                    // Prepare a Log entry
                    vecInstrumentLogEntry = new Vector<Object>(NtpLog.EVENT_LOG_WIDTH);

                    // Capture the requested number of samples for the ClockOffset
                    for (int intCaptureIndex = 0;
                         ((intCaptureIndex < intCaptureCountMax)
                            && Utilities.executeWorkerCanProceed(this));
                        intCaptureIndex++)
                        {
                        final long longTimeStartMillis;
                        final long longTimeStopMillis;
                        final Iterator iterServers;
                        boolean boolTimeValid;

                        // Record the time of the *first attempt*
                        longTimeStartMillis = System.currentTimeMillis();

                        // Cycle through the available servers if we get a timeout etc.
                        iterServers = vecServers.iterator();

                        // No Time yet!
                        boolTimeValid = false;

                        while ((iterServers.hasNext())
                            && Utilities.executeWorkerCanProceed(this)
                            && (!boolTimeValid))
                            {
                            final Calendar calTimeStamp;
                            final String strServerName;

                            strServerName = (String) iterServers.next();

                            if ((strServerName != null)
                                && (!EMPTY_STRING.equals(strServerName.trim())))
                                {
                                NtpConnection ntpConnection;
                                final NtpData ntpData;

                                ntpConnection = null;

                                try
                                    {
                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "Trying to connect to NTP server " + strServerName);

                                    // Attempt to get an NtpConnection and read the NtpData
                                    ntpConnection = new NtpConnection(InetAddress.getByName(strServerName));

                                    // Cater for the worst-case timeout where all servers don't respond
                                    ntpConnection.setTimeout(getTimeoutMillis(commandmessage.getModule(), cmdCaptureOffset) / vecServers.size());
                                    ntpData = ntpConnection.getNtpData();

                                    if ((ntpData != null)
                                        && (ntpData.getServerAddress() != null)
                                        && (getTimeSeries() != null))
                                        {
                                        final GregorianCalendar calNTP;

                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                               "NTP logging the time...");

                                        // WARNING! The NTP TimeStamps are referred to the **Platform Clock** NOT the ObservatoryClock!
                                        // For this command we capture the offsets relative to the PlatformClock
                                        // Do not use a reference to the ObservatoryClock calendar!
                                        calNTP = new GregorianCalendar();
                                        calNTP.setTimeInMillis(getObservatoryClock().getSystemTimeMillis() + ntpData.getOffset());

                                        // Record the valid Time details in the Log
                                        vecInstrumentLogEntry.clear();
                                        vecInstrumentLogEntry.add(imageIcon);
                                        vecInstrumentLogEntry.add(false);
                                        vecInstrumentLogEntry.add(ChronosHelper.toDateString(calNTP));
                                        vecInstrumentLogEntry.add(ChronosHelper.toTimeString(calNTP));
                                        vecInstrumentLogEntry.add(strServerName);
                                        vecInstrumentLogEntry.add(ntpData.getServerAddress().toString());
                                        vecInstrumentLogEntry.add(Integer.toString(ntpData.getVersionNumber()));
                                        vecInstrumentLogEntry.add(Long.toString(ntpData.getOffset()));
                                        vecInstrumentLogEntry.add(Long.toString(ntpData.getRoundTripDelay()));
                                        vecInstrumentLogEntry.add(Integer.toString(ntpData.getStratum()));
                                        vecInstrumentLogEntry.add(Double.toString(ntpData.getPrecision()));
                                        vecInstrumentLogEntry.add(LOG_TIME_SET_DISABLED);

                                        calTimeStamp = ntpData.getReferenceTimeStamp().toCalendar();
                                        vecInstrumentLogEntry.add(ChronosHelper.toDateString(calTimeStamp));
                                        vecInstrumentLogEntry.add(ChronosHelper.toTimeString(calTimeStamp));
                                        vecInstrumentLogEntry.add(Double.toString(ntpData.getReferenceTimeStamp().getFractionalPart()));
                                        getInstrumentLogFragment().add(vecInstrumentLogEntry);

                                        // Record the offset vs Time
                                        if (Math.abs(ntpData.getOffset()) < MAX_OFFSET_MILLISEC)
                                            {
                                            // ToDo addOrUpdate() Requires Locale
                                            getTimeSeries().addOrUpdate(new Second(calNTP.getTime(),
                                                                                   calNTP.getTimeZone()),
                                                                        ntpData.getOffset());
                                            }

                                        // Put the time offset on the ControlPanel display via the Metadata
                                        NtpClientHelper.setNtpOffsetMetadataValue(getObservationMetadata(), ntpData.getOffset());

                                        // This is also the simple ResponseValue
                                        strResponseValue = Long.toString(ntpData.getOffset());

                                        // This server responded, so it is time to leave!
                                        boolTimeValid = true;
                                        }
                                    else
                                        {
                                        // We cannot read a valid NtpData for this server, so try another
                                        setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                                  strServerName,
                                                                  LOG_TIME_NO_DATA);
                                        getInstrumentLogFragment().add(vecInstrumentLogEntry);
                                        }
                                    }

                                catch (SocketTimeoutException exception)
                                    {
                                    setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                              strServerName,
                                                              LOG_TIME_SET_SOCKET_ERROR);
                                    getInstrumentLogFragment().add(vecInstrumentLogEntry);
                                    }

                                catch (SocketException exception)
                                    {
                                    setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                              strServerName,
                                                              LOG_TIME_SET_SOCKET_ERROR);
                                    getInstrumentLogFragment().add(vecInstrumentLogEntry);
                                    }

                                catch (UnknownHostException exception)
                                    {
                                    setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                              strServerName,
                                                              LOG_TIME_SET_UNKNOWN_HOST);
                                    getInstrumentLogFragment().add(vecInstrumentLogEntry);
                                    }

                                catch (IOException exception)
                                    {
                                    setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                              strServerName,
                                                              LOG_TIME_SET_IO_ERROR);
                                    getInstrumentLogFragment().add(vecInstrumentLogEntry);
                                    }

                                catch (NullPointerException exception)
                                    {
                                    setNullInstrumentLogEntry(vecInstrumentLogEntry,
                                                              strServerName,
                                                              LOG_TIME_SET_NULL);
                                    getInstrumentLogFragment().add(vecInstrumentLogEntry);
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

                            // Update the Logs on every attempt, regardless of success
                            if (getHostInstrument().getInstrumentPanel() != null)
                                {
                                getHostInstrument().getInstrumentPanel().flushLogFragments(getWrappedData());
                                }
                            }

                        // Were we successful in getting a response from one of the servers?
                        if (boolTimeValid)
                            {
                            ResponseMessageHelper.updateWrappedData(this,
                                                                    null,
                                                                    getTimeSeriesCollection(),
                                                                    true);
                            }

                        // See how long we must wait for the next interval
                        longTimeStopMillis = System.currentTimeMillis();

                        // We may have taken so long with retries that we have exceeded the original ClockUpdate.Interval,
                        // in which case we won't wait for the next interval
                        if ((intClockUpdateIntervalSec * ChronosHelper.SECOND_MILLISECONDS) > (longTimeStopMillis - longTimeStartMillis))
                            {
                            Utilities.safeSleepPollExecuteWorker((intClockUpdateIntervalSec * ChronosHelper.SECOND_MILLISECONDS) - (longTimeStopMillis - longTimeStartMillis),
                                                                 this);
                            }
                        }

                    // We always terminate correctly, taking the last ResponseValue
                    // The Wrapper has always been harmonised with the DAO, if successful
                    getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else
                    {
                    // This should never occur if the XML is Ok
                    // but the user could set Period < Interval!
                    SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_CLOCK_OFFSET
                                                       + METADATA_ACTION_CAPTURE
                                                       + METADATA_RESULT
                                                           + ResponseMessageStatus.INVALID_PARAMETER.getName()
                                                           + TERMINATOR_SPACE
                                                       + METADATA_INTERVAL + intClockUpdateIntervalSec + TERMINATOR_SPACE
                                                       + METADATA_PERIOD + intClockUpdatePeriodSec + TERMINATOR,
                                                       SOURCE,
                                                       getObservatoryClock());
                    strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                    getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }

            // These are fatal errors which must stop the capture loop

            // This should have been trapped by Regex
            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET
                                                       + SOURCE.trim()
                                                       + TERMINATOR
                                                   + METADATA_ACTION_COMMAND
                                                   + METADATA_RESULT
                                                   + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                   + exception.getMessage()
                                                   + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET
                                                       + SOURCE.trim()
                                                       + TERMINATOR
                                                   + METADATA_ACTION_COMMAND
                                                   + METADATA_RESULT
                                                   + MSG_UNSUPPORTED_FORMAT
                                                   + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            catch (Exception exception)
                {
                exception.printStackTrace();
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET
                                                       + SOURCE.trim()
                                                       + TERMINATOR
                                                   + METADATA_ACTION_COMMAND
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }
            }
        else
            {
            // The XML configuration was inappropriate
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(this,
                                                                                SOURCE,
                                                                                METADATA_TARGET
                                                                                + SOURCE.trim()
                                                                                + TERMINATOR,
                                                                                METADATA_ACTION_COMMAND));
            }

        // Something has changed, we may need to refresh a ControlPanel etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

        responseMessage = ResponseMessageHelper.createResponseMessage(this,
                                                                      commandmessage,
                                                                      cmdCaptureOffset,
                                                                      null,
                                                                      getTimeSeriesCollection(),
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * exportClockOffset().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportClockOffset(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NtpDAO.exportClockOffset()";

        // Simply change the name of the Command!
        // The Chart is already attached to the DAOWrapper

        return (exportChart(commandmessage));
        }


    /***********************************************************************************************
     * Get the TimeSeriesCollection containing the NTP offset data.
     *
     * @return TimeSeriesCollection
     */

    private TimeSeriesCollection getTimeSeriesCollection()
        {
        return (this.collectionTimeSeries);
        }


    /***********************************************************************************************
     * Get the TimeSeries containing the NTP offset data.
     *
     * @return TimeSeries
     */

    private TimeSeries getTimeSeries()
        {
        return (this.seriesSamples);
        }


    /***********************************************************************************************
     * Read all the Resources required by the DAO.
     *
     * AbstractObservatoryInstrumentDAO reads:
     *  KEY_DAO_TIMEOUT_DEFAULT
     *  KEY_DAO_UPDATE_PERIOD
     *
     * This DAO reads:
     *  KEY_DAO_NTP_SERVER_DEFAULT
     *  KEY_DAO_NTP_SERVER_1
     *  KEY_DAO_NTP_SERVER_2
     *  KEY_DAO_NTP_SERVER_3
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "NtpDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        urlServerDefault = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_DEFAULT);
        urlServer1 = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_1);
        urlServer2 = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_2);
        urlServer3 = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_3);
        }
    }
