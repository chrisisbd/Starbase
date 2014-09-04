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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.dao;

import org.apache.xmlbeans.XmlObject;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ObservatoryClockHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.clocks.NTPSynchronisedProxyClock;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.ntp.NtpConnection;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.ntpclient.NtpClientHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.lmn.fc.model.logging.EventStatus.INFO;
import static org.lmn.fc.model.logging.EventStatus.WARNING;


/***********************************************************************************************
 * NTPSynchronisedProxyClockDAO.
 * WARNING! This DAO must NOT use getObservatoryClock() to get the time,
 * since this DAO is responsible for providing the source of Time!
 */

public final class NTPSynchronisedProxyClockDAO extends AbstractClockDAO
                                                implements ObservatoryInstrumentDAOInterface,
                                                           ObservatoryClockDAOInterface
    {
    private static final String SERIES_KEY_CLOCK_OFFSET = "Offset";
    private static final String MSG_ALREADY_STOPPED = "time proxy is already stopped";
    private static final String MSG_ALREADY_RUNNING = "time proxy is already running";
    private static final String ITEM_SYNCHRONISE_PERIOD = "SynchronisePeriod";

    private static final int DAO_CHANNEL_COUNT = 1;
    public static final int DEFAULT_SYNC_MILLIS = 10000;

    // A record of the Time Offsets
    private final TimeSeriesCollection collectionTimeSeries;
    private final TimeSeries seriesOffsets;

    // The Thread for handling TimeProxy
    private SwingWorker workerProxy;

    // Proxy State
    private int intSynchronisePeriodMillis;
    private boolean boolProxyRunning;

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
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("synchroniseNow");
        pool.add("getSynchronisePeriod");
        pool.add("setSynchronisePeriod");
        pool.add("synchroniseTimeProxy");
        pool.add("exportClockOffset");
        }


    /***********************************************************************************************
     * Construct a NTPSynchronisedProxyClockDAO.
     *
     * @param hostinstrument
     */

    public NTPSynchronisedProxyClockDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super (hostinstrument);

        // Create the Clock driven by this DAO
        this.clock = new NTPSynchronisedProxyClock(this);

        // Make a TimeSeries based on Seconds and place into a Collection...
        this.seriesOffsets = new TimeSeries(SERIES_KEY_CLOCK_OFFSET);
        this.collectionTimeSeries = new TimeSeriesCollection(getTimeSeries());

        // Proxy State
        this.workerProxy = null;
        this.intSynchronisePeriodMillis = DEFAULT_SYNC_MILLIS;
        this.boolProxyRunning = false;

        // Configurable Resources
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
        final String SOURCE = "NTPSynchronisedProxyClockDAO.initialiseDAO() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        getTimeSeries().clear();
        setProxyWorker(null);
        setProxyRunning(false);

        addAllMetadataToContainersTraced(ObservatoryClockHelper.createClockOffsetChannelMetadata(),
                                         SOURCE,
                                         LOADER_PROPERTIES.isMetadataDebug());
        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        final String SOURCE = "NTPSynchronisedProxyClockDAO.disposeDAO() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(), SOURCE);

        // Stop any existing Proxy SwingWorker
        if (getProxyWorker() != null)
            {
            SwingWorker.disposeWorker(getProxyWorker(), true, SWING_WORKER_STOP_DELAY);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Proxy SwingWorker stopped");
            }

        setProxyWorker(null);
        setProxyRunning(false);

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
        final String SOURCE = "NTPSynchronisedProxyClockDAO.reset() ";
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
            setSynchronisePeriodMillis(DEFAULT_SYNC_MILLIS);

            // Stop any existing Proxy SwingWorker
            if (getProxyWorker() != null)
                {
                SwingWorker.disposeWorker(getProxyWorker(), true, SWING_WORKER_STOP_DELAY);
                }

            setProxyWorker(null);
            setProxyRunning(false);

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
        return (new ClockCommandMessage(dao,
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
        return (new ClockResponseMessage(portname,
                                         instrumentxml,
                                         module,
                                         command,
                                         starscript.trim(),
                                         responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * synchroniseNow().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface synchroniseNow(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NTPSynchronisedProxyClockDAO.synchroniseNow( ";
        final CommandType cmdSynchroniseNow;
        boolean boolSuccess;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Don't affect the CommandType of the incoming Command
        cmdSynchroniseNow = (CommandType)commandmessage.getCommandType().copy();

        // There are no Raw Data
        setRawDataChannelCount(DAO_CHANNEL_COUNT);
        setTemperatureChannel(false);

        boolSuccess = false;

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        if (getObservatoryClock() != null)
            {
            final TimeZone timeZone;
            final Locale locale;

            timeZone = REGISTRY.getFrameworkTimeZone();
            locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                REGISTRY.getFramework().getCountryISOCode());

            // Always accept the data for the Chart
            // Use the offset between NTP and whatever the ObservatoryClock is currently showing
            boolSuccess = getObservatoryClock().synchronise(ObservatoryClockHelper.getNTPOffsetMillis(urlServerDefault,
                                                                                                      urlServer1,
                                                                                                      urlServer2,
                                                                                                      urlServer3,
                                                                                                      getTimeoutMillis(commandmessage.getModule(), cmdSynchroniseNow),
                                                                                                      true,
                                                                                                      getTimeSeries(),
                                                                                                      getEventLogFragment(),
                                                                                                      timeZone,
                                                                                                      locale,
                                                                                                      getObservatoryClock()),
                                                            getTimeSeries(),
                                                            timeZone,
                                                            locale,
                                                            true,
                                                            getEventLogFragment());
            }

        if (boolSuccess)
            {
            // The synchroniseNow() ResponseValue is the (potentially new) timestamp of the event
            strResponseValue = getObservatoryClock().getDateTimeNowAsString();
            getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }
        else
            {
            strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
            getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
            }

        // Something has changed, we may need to refresh a ControlPanel etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

        responseMessage = ResponseMessageHelper.createResponseMessage(this,
                                                                      commandmessage,
                                                                      cmdSynchroniseNow,
                                                                      null,
                                                                      getTimeSeriesCollection(),
                                                                      strResponseValue);
        return (responseMessage);
        }


    /**********************************************************************************************/
    /* TimeProxy                                                                                  */
    /***********************************************************************************************
     * getSynchronisePeriod().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getSynchronisePeriod(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NTPSynchronisedProxyClockDAO.getSynchronisePeriod()";
        final CommandType cmdGetSyncPeriod;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdGetSyncPeriod = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        strResponseValue = Long.toString(getSynchronisePeriodMillis() / ChronosHelper.SECOND_MILLISECONDS);
        getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

        responseMessage = ResponseMessageHelper.createResponseMessage(this,
                                                                      commandmessage,
                                                                      cmdGetSyncPeriod,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * setSynchronisePeriod().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setSynchronisePeriod(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NTPSynchronisedProxyClockDAO.setSynchronisePeriod()";
        final int PARAMETER_COUNT = 1;
        final int INDEX_PERIOD = 0;
        final CommandType cmdSetSyncPeriod;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdSetSyncPeriod = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        // We expect one parameter, the synchronise period
        listParameters = cmdSetSyncPeriod.getParameterList();

        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_PERIOD) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_PERIOD).getInputDataType().getDataTypeName()))
            && (getHostInstrument() != null))
            {
            try
                {
                final String strSynchronisePeriod;
                final int intSynchronisePeriodSec;

                strSynchronisePeriod = listParameters.get(INDEX_PERIOD).getValue();
                intSynchronisePeriodSec = Integer.parseInt(strSynchronisePeriod);
                setSynchronisePeriodMillis((int)(intSynchronisePeriodSec * ChronosHelper.SECOND_MILLISECONDS));

                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   INFO,
                                                   METADATA_TARGET_TIME_PROXY
                                                        + METADATA_ACTION_SET
                                                        + METADATA_ITEM + ITEM_SYNCHRONISE_PERIOD + TERMINATOR_SPACE
                                                        + METADATA_VALUE + strSynchronisePeriod.trim() + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                        + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                        + TERMINATOR,
                                                   getLocalHostname(),
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
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
                                                                      cmdSetSyncPeriod,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * synchroniseTimeProxy().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface synchroniseTimeProxy(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "NTPSynchronisedProxyClockDAO.synchroniseTimeProxy()";
        final int PARAMETER_COUNT = 2;
        final int INDEX_RUN = 0;
        final int INDEX_LOGGING = 1;
        final ObservatoryInstrumentDAOInterface thisDAO;
        final CommandType cmdSyncProxy;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                        || LOADER_PROPERTIES.isMetadataDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE);

        // For use in inner classes
        thisDAO = this;

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdSyncProxy = (CommandType)commandmessage.getCommandType().copy();

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdSyncProxy),
                                       DAO_CHANNEL_COUNT,
                                       false,
                                       NtpClientHelper.createNtpClientChannelMetadata(NtpClientHelper.TITLE_OBSERVATORY_CLOCK_OFFSET,
                                                                                      boolDebug),
                                       null);

        // We expect one parameter, a control boolean
        listParameters = cmdSyncProxy.getParameterList();

        // Create a new TimeProxy, or stop the existing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_RUN) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_RUN).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_LOGGING) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_LOGGING).getInputDataType().getDataTypeName()))
            && (getHostInstrument() != null))
            {
            final boolean boolRunRequested;
            final boolean boolVerboseLogging;

            strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;

            // These should never throw NumberFormatException, because they have already been parsed
            boolRunRequested = Boolean.parseBoolean(listParameters.get(INDEX_RUN).getValue());
            boolVerboseLogging = Boolean.parseBoolean(listParameters.get(INDEX_LOGGING).getValue());

            if ((!isProxyRunning())
                && (boolRunRequested))
                {
                // Stop any existing Proxy SwingWorker
                // This should never occur
                if (getProxyWorker() != null)
                    {
                    SwingWorker.disposeWorker(getProxyWorker(), true, SWING_WORKER_STOP_DELAY);
                    setProxyRunning(false);
                    }

                // We've been asked to Run a stopped TimeProxy
                // We need a SwingWorker otherwise this would execute on the PortController Thread,
                // which would block while waiting for a Response...

                setProxyWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
                    {
                    /*******************************************************************************
                     * Run the TimeProxy synchroniser
                     *
                     * @return Object
                     */

                    public Object construct()
                        {
                        final TimeZone timeZone;
                        final Locale locale;
                        boolean boolAllowAdd;

                        timeZone = REGISTRY.getFrameworkTimeZone();
                        locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                            REGISTRY.getFramework().getCountryISOCode());

                        // There are no Raw Data
                        setRawDataChannelCount(DAO_CHANNEL_COUNT);
                        setTemperatureChannel(false);

                        // Discard the first data item, which may be hopelessly out of sync
                        boolAllowAdd = false;

                        while ((isProxyRunning())
                            && (Utilities.workerCanProceed(commandmessage.getDAO(), this)))
                            {
                            // Update all Resources each time round
                            readResources();

                            // We musn't timeout, since this might run forever...
                            TimeoutHelper.restartDAOTimeoutTimerInfinite(thisDAO);

                            // Do nothing without a Clock or a means of recording the Offset
                            if ((getObservatoryClock() != null)
                                && (getTimeSeries() != null))
                                {
                                final boolean boolSuccess;

                                // Adjust the ObservatoryClock to match the NTP time server
                                boolSuccess = getObservatoryClock().synchronise(ObservatoryClockHelper.getNTPOffsetMillis(urlServerDefault,
                                                                                                                          urlServer1,
                                                                                                                          urlServer2,
                                                                                                                          urlServer3,
                                                                                                                          getTimeoutMillis(commandmessage.getModule(), cmdSyncProxy),
                                                                                                                          boolAllowAdd,
                                                                                                                          getTimeSeries(),
                                                                                                                          getEventLogFragment(),
                                                                                                                          timeZone,
                                                                                                                          locale,
                                                                                                                          getObservatoryClock()),
                                                                                getTimeSeries(),
                                                                                timeZone,
                                                                                locale,
                                                                                boolVerboseLogging,
                                                                                getEventLogFragment());
                                // Have we got anything to show?
                                if (((boolSuccess))
                                    && (boolAllowAdd))
                                    {
                                    // Update the XYDataset and Logs
                                    // This will fire off another SwingWorker to refresh the Chart
                                    // and clear the Log fragments
                                    ResponseMessageHelper.updateWrappedData(thisDAO,
                                                                            null,
                                                                            getTimeSeriesCollection(),
                                                                            true);
                                    }

                                // Accept all data from now on
                                boolAllowAdd = true;
                                }

                            Utilities.safeSleepPollWorker(getSynchronisePeriodMillis(),
                                                          commandmessage.getDAO(),
                                                          this);
                            }

                        return (null);
                        }


                    /***********************************************************************************
                     * When the Thread stops.
                     */

                    public void finished()
                        {
                        // Put the Timeout back to what it should be for a single default command
                        TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(thisDAO);
                        }
                    });

                // Start the Thread we have prepared...
                getProxyWorker().start();
                setProxyRunning(true);

                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   INFO,
                                                   METADATA_TARGET_TIME_PROXY
                                                        + METADATA_ACTION_START,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }
            else if ((isProxyRunning())
                && (Utilities.workerCanProceed(commandmessage.getDAO(), getProxyWorker()))
                && (boolRunRequested))
                {
                // We've been asked to Run a running TimeProxy, which must fail
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   WARNING,
                                                   METADATA_TARGET_TIME_PROXY
                                                           + METADATA_ACTION_START
                                                           + METADATA_MESSAGE + MSG_ALREADY_RUNNING + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }
            else if ((isProxyRunning())
                 && (Utilities.workerCanProceed(commandmessage.getDAO(), getProxyWorker()))
                 && (!boolRunRequested))
                {
                // We've been asked to Stop a running TimeProxy
                // Put the Timeout back to what it should be for a single default command
                TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(thisDAO);

                // We must try to stop the TimeProxy
                setProxyRunning(false);
                SwingWorker.disposeWorker(getProxyWorker(), true, SWING_WORKER_STOP_DELAY);
                setProxyWorker(null);

                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   INFO,
                                                   METADATA_TARGET_TIME_PROXY
                                                        + METADATA_ACTION_STOP,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }
            else if ((!isProxyRunning())
                && (!boolRunRequested))
                {
                // We've been asked to Stop a stopped TimeProxy, which must fail
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   WARNING,
                                                   METADATA_TARGET_TIME_PROXY
                                                           + METADATA_ACTION_STOP
                                                           + METADATA_MESSAGE + MSG_ALREADY_STOPPED + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }
            else
                {
                // We are in an unknown state
                LOGGER.error(SOURCE + "Invalid Proxy state [proxy_running=" + isProxyRunning() + "] [run_requested=" + boolRunRequested + "]");
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
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
                                                                      cmdSyncProxy,
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
        final String SOURCE = "NTPSynchronisedProxyClockDAO.exportClockOffset()";

        // Simply change the name of the Command!
        // The Chart is already attached to the DAOWrapper

        return (exportChart(commandmessage));
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the ObservatoryClock being synthesised by this DAO.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        final String SOURCE = "NTPSynchronisedProxyClockDAO.getObservatoryClock() ";

        //LOGGER.log(SOURCE + "Getting synthesised NTPSynchronisedProxyClock ObservatoryClock");

        return (this.clock);
        }


    /***********************************************************************************************
     * Indicate if the TimeProxy is running.
     * This is not in an interface.
     *
     * @return boolean
     */

    private boolean isProxyRunning()
        {
        return (this.boolProxyRunning);
        }


    /***********************************************************************************************
     * Control the TimeProxy state.
     *
     * @param running
     */

    private void setProxyRunning(final boolean running)
        {
        this.boolProxyRunning = running;
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
        return (this.seriesOffsets);
        }


    /***********************************************************************************************
     * Get the period to wait before resynchronising the TimeProxy.
     *
     * @return int
     */

    private int getSynchronisePeriodMillis()
        {
        return (this.intSynchronisePeriodMillis);
        }


    /***********************************************************************************************
     * Set the period to wait before resynchronising the TimeProxy.
     *
     * @param period
     */

    private void setSynchronisePeriodMillis(final int period)
        {
        this.intSynchronisePeriodMillis = period;
        }


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Get the SwingWorker which handles the TimeProxy.
     *
     * @return SwingWorker
     */

    private SwingWorker getProxyWorker()
        {
        return (this.workerProxy);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the TimeProxy.
     *
     * @param worker
     */

    private void setProxyWorker(final SwingWorker worker)
        {
        this.workerProxy = worker;
        }


    /***********************************************************************************************
     * Read all the Resources required by the DAO.
     *
     * AbstractObservatoryInstrumentDAO reads:
     *  KEY_DAO_TIMEOUT_DEFAULT
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
                               "NTPSynchronisedProxyClockDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        urlServerDefault = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_DEFAULT);
        urlServer1 = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_1);
        urlServer2 = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_2);
        urlServer3 = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_NTP_SERVER_3);
        }
    }
