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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.dao;

import math.geom2d.Point2D;
import org.apache.xmlbeans.XmlObject;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.exceptions.GpsException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportChart;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportProcessedData;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsInstrumentReceiverInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsReceiverHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.receiver.SatelliteData;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.*;


/***********************************************************************************************
 * GpsDAO.
 */

public final class GpsDAO extends AbstractObservatoryInstrumentDAO
                          implements ObservatoryInstrumentDAOInterface
    {
    // String Resources
    private static final String EVENT_SOURCE = "GpsDAO";
    private static final String EXCEPTION_GPS_START    = "GPS Receiver communications did not start";
    private static final String KEY_GPS_FIXES = "Longitude";

    private static final int GPS_RETRIES = 3;
    private static final int DAO_CHANNEL_COUNT = 1;
    private static final int CAPTURE_PERIOD_MIN_SEC = 1;
    private static final int CAPTURE_PERIOD_MAX_SEC = 100;
    private static final double ERROR_KEEP_OUT = 0.1;

    // The GpsReceiver used by this DAO
    private GpsInstrumentReceiverInterface gpsReceiverInstance;

    // A Map of the history of each satellite, keyed by the satellite IDs
    private Map<Integer, Vector<SatelliteData>> mapSatellites;
    private List<Point2D> listFixes;

    // The history of all fixes, for the Scatter Plot
    private XYDataset xydatasetFixes;

    // Configurable Resources
    private String strReceiverClassName;
    private String strReceiverType;
    private boolean boolEnableGPS;
    private int intCapturePeriodSec;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("getInterpretedNMEA");
        pool.add("getGpsFix");
        pool.add("captureScatterPlot");
        pool.add("exportSatelliteMap");
        pool.add("exportScatterPlot");
        pool.add("exportGpsFixes");
        }


    /***********************************************************************************************
     * Construct a GpsDAO.
     *
     * @param hostinstrument
     */

    public GpsDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.gpsReceiverInstance = null;

        this.mapSatellites = null;
        this.listFixes = null;
        this.xydatasetFixes = null;

        // Resources
        this.strReceiverClassName = EMPTY_STRING;
        this.strReceiverType = EMPTY_STRING;
        this.boolEnableGPS = true;
        this.intCapturePeriodSec = CAPTURE_PERIOD_MIN_SEC;

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "GpsDAO.initialiseDAO() ";
        boolean boolSuccess;

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                             SOURCE + "[resourcekey=" + resourcekey + "]");

        boolSuccess = super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        // Somewhere to store the satellite history
        this.mapSatellites = Collections.synchronizedMap(new HashMap<Integer, Vector<SatelliteData>>(GpsInstrumentReceiverInterface.MAX_SATELLITES));

        // The history of all fixes, for the Scatter Plot
        this.listFixes = new ArrayList<Point2D>(1000);

        // We know that ChartHelper..updateChartFromDAOMetadata() will
        // set the Series Key to be the same as the Channel.Name
        // so to preempt this, use the Longitude name
        this.xydatasetFixes = new XYSeriesCollection(new XYSeries(KEY_GPS_FIXES));

        // Instantiate the GPS Receiver
        setGpsReceiverInstance(GpsReceiverHelper.instantiateReceiver(strReceiverClassName,
                                                                     this,
                                                                     strReceiverType,
                                                                     getResourceKey()));
        boolSuccess = boolSuccess && (getGpsReceiverInstance() != null);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        final String SOURCE = "GpsDAO.disposeDAO() ";

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                             SOURCE);

        this.mapSatellites = null;
        this.listFixes = null;
        this.xydatasetFixes = null;

        // Dispose of any existing Receiver
        if (getGpsReceiverInstance() != null)
            {
            getGpsReceiverInstance().stop();
            getGpsReceiverInstance().dispose();
            }

        super.disposeDAO();
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
        return (new GpsCommandMessage(dao,
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
        return (new GpsResponseMessage(portname,
                                       instrumentxml,
                                       module,
                                       command,
                                       starscript.trim(),
                                       responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Core Commands                                                                          */
    /***********************************************************************************************
     * reset() resets the whole Instrument.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface reset(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.reset() ";
        final ResponseMessageInterface response;

        response = super.reset(commandmessage);

        // Do this here rather than in the Instrument to avoid having to expose the SatelliteHistory
        // Only reset this DAO to DEFAULTS if all else worked...
        if ((ResetMode.DEFAULTS.equals(DAOHelper.extractResetMode(commandmessage.getCommandType())))
            && (response != null)
            && (response.getResponseMessageStatusList() != null)
            && (response.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS)))
            {
            // Clear the SatelliteHistory...
            if (getSatelliteHistory() != null)
                {
                getSatelliteHistory().clear();
                }

            // ... and the Fixes
            if (getFixPoints() != null)
                {
                getFixPoints().clear();
                }

            if ((getFixesXydataset() != null)
                && (getFixesXydataset() instanceof XYSeriesCollection)
                && (getFixesXydataset().getSeriesCount() == 1)
                && (((XYSeriesCollection)getFixesXydataset()).getSeries(0) != null))
                {
                final XYSeries seriesFixes;

                seriesFixes = ((XYSeriesCollection)getFixesXydataset()).getSeries(0);
                seriesFixes.clear();
                }
            }

        return (response);
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * getInterpretedNMEA().
     * Returns a list of interpreted NMEA sentence types.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getInterpretedNMEA(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.getInterpretedNMEA() ";
        final CommandType cmdGetNMEA;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                             SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdGetNMEA = (CommandType)commandmessage.getCommandType().copy();

        // Stop any existing Receiver
        if (getGpsReceiverInstance() != null)
            {
            getGpsReceiverInstance().stop();
            }

        // There are no parameters expected for this Command

        if (getGpsReceiverInstance() != null)
            {
            final List<String> listNMEA;
            final StringBuffer buffer;

            listNMEA = getGpsReceiverInstance().getNMEASentences();
            buffer = new StringBuffer();

            for (int i = 0;
                 i < listNMEA.size();
                 i++)
                {
                final String nmea;

                nmea = listNMEA.get(i);
                buffer.append(nmea);
                buffer.append(COMMA);
                }

            // Trim off any trailing comma!
            if (buffer.toString().endsWith(COMMA))
                {
                buffer.setLength(buffer.length() - 1);
                }

            strResponseValue = buffer.toString();
            getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }
        else
            {
            strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
            getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
            }

        // Something has changed, we may need to refresh a ControlPanel etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.createResponseMessage(this,
                                                                      commandmessage,
                                                                      cmdGetNMEA,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * getGpsFix().
     * Get a single GPS fix and associated satellite data.
     * The fix is returned in the DAO ObservationMetadata and in the ResponseValue.
     * This is a capture Command.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getGpsFix(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.getGpsFix() ";
        final int PARAMETER_COUNT = 2;
        final int INDEX_SOURCE = 0;
        final int INDEX_TARGET = 1;
        final CommandType commandGetFix;
        final List<ParameterType> listParameters;
        Vector<Object> vecRawData;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(), SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandGetFix = (CommandType)commandmessage.getCommandType().copy();

        // Prepare some RawData (in the manner of establishDAOIdentityForCapture())
        setRawDataChannelCount(DAO_CHANNEL_COUNT);
        vecRawData = null;

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        // We expect two parameters, the Update Source and the Update Target
        listParameters = commandGetFix.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_SOURCE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_SOURCE).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TARGET) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_TARGET).getInputDataType().getDataTypeName())))
            {
            strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;

            try
                {
                if ((boolEnableGPS)
                    && (getGpsReceiverInstance() != null)
                    && (InstrumentState.isDoingSomething(getHostInstrument())))
                    {
                    try
                        {
                        final String strSourceKey;
                        final String strSource;
                        final String strTargetKey;
                        final String strTarget;
                        final UpdateSource updateSource;
                        final UpdateTarget updateTarget;

                        // Stop any existing Receiver
                        if (getGpsReceiverInstance() != null)
                            {
                            getGpsReceiverInstance().stop();
                            }

                        strSourceKey = MetadataDictionary.KEY_OBSERVATION_ROOT.getKey() + listParameters.get(INDEX_SOURCE).getName();
                        strSource = listParameters.get(INDEX_SOURCE).getValue();
                        updateSource = UpdateSource.getUpdateSourceForName(strSource);

                        strTargetKey = MetadataDictionary.KEY_OBSERVATION_ROOT.getKey() + listParameters.get(INDEX_TARGET).getName();
                        strTarget = listParameters.get(INDEX_TARGET).getValue();
                        updateTarget = UpdateTarget.getUpdateTargetForName(strTarget);

                        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                                             SOURCE + "[source=" + updateSource.getName() + "] [target=" + updateTarget.getName() + "]");

                        // Start the GpsReceiver afresh, if we are configured correctly
                        if ((updateSource != null)
                            && (updateTarget != null)
                            && (getGpsReceiverInstance() != null)
                            && (getGpsReceiverInstance().start()))
                            {
                            boolean boolValidFix;
                            int intRetryCounter;

                            // Try up to GPS_RETRIES times to get a valid GpsReceiver fix
                            boolValidFix = false;
                            intRetryCounter = 0;

                            // Set up some basic ObservationMetadata
                            // Location Metadata will be added on a valid fix
                            addAllMetadataToContainersTraced(GpsReceiverHelper.createGpsReceiverChannelMetadata(LOADER_PROPERTIES.isMetadataDebug()),
                                                             SOURCE,
                                                             LOADER_PROPERTIES.isMetadataDebug());

                            // Check that there isn't a conflict between timeout period and capture period
                            // If there is, sort it out
                            if ((intCapturePeriodSec * (int) ChronosHelper.SECOND_MILLISECONDS) > ((getTimeoutMillis(commandmessage.getModule(), commandGetFix) * 3) >> 2))
                                {
                                final String strMessage;

                                intCapturePeriodSec = getTimeoutMillis(commandmessage.getModule(), commandGetFix) / (int) ChronosHelper.SECOND_MILLISECONDS;
                                intCapturePeriodSec = intCapturePeriodSec / 3;

                                REGISTRY.setIntegerProperty(getResourceKey() + KEY_DAO_GPS_PERIOD_CAPTURE,
                                                            intCapturePeriodSec);

                                strMessage = METADATA_ACTION_CHANGE
                                                + METADATA_TARGET_PROPERTY
                                                + PREFIX
                                                + KEY_DAO_GPS_PERIOD_CAPTURE
                                                + EQUALS
                                                + intCapturePeriodSec
                                                + TERMINATOR;
                                LOGGER.error(strMessage);

                                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                                   EventStatus.WARNING,
                                                                   strMessage,
                                                                   EVENT_SOURCE,
                                                                   getObservatoryClock());
                                }

                            // Check that we are still running,
                            // to save time if the User has stopped the Instrument
                            while((!boolValidFix)
                                    && (intRetryCounter < GPS_RETRIES)
                                    && (Utilities.executeWorkerCanProceed(this)))
                                {
                                LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                                                     SOURCE + "retry " + intRetryCounter);
                                boolValidFix = getGpsReceiverInstance().decodeNMEA(intCapturePeriodSec,
                                                                                   getObservatoryClock());
                                intRetryCounter++;
                                }

                            // Get the GpsReceiver data and update our location if all is well
                            strResponseValue = GpsReceiverHelper.logFixOutcome(getInstrumentLogFragment(),
                                                                               getGpsReceiverInstance(),
                                                                               getObservationMetadata(),
                                                                               strSourceKey,
                                                                               updateSource,
                                                                               strTargetKey,
                                                                               updateTarget,
                                                                               boolValidFix,
                                                                               getObservatoryClock(),
                                                                               getHostInstrument().isDebugMode());
                            // Were we successful in getting some data?
                            // The actual fix is only held in the ObservationMetaData
                            // The RawData holds the accumulated satellite positions
                            // The XYDataset of accumulated fixes will be used by the ScatterPlot
                            // The ResponseValue is the number of satellites in use and in view
                            // Even if the InstrumentLog contains only Exceptions, we need to know

                            if ((getInstrumentLogFragment() != null)
                                && (!getInstrumentLogFragment().isEmpty())
                                && (getGpsReceiverInstance() != null))
                                {
                                // Copy the satellite data from this fix into the DAO map...
                                // ...and pass it back to the Instrument as one 'channel' of RawData
                                vecRawData = GpsReceiverHelper.accumulateSatellitesInView(getSatelliteHistory(),
                                                                                          getGpsReceiverInstance().getSatellitesInView());
                                setRawDataChanged(true);

                                // The XYDataset of fixes will be used by the ScatterPlot...
                                // ... but don't add the point (0.0, 0.0) because this is an error flag,
                                // and the centroid calculation will thereafter be incorrect
                                // To avoid rounding errors, exclude the area enclosed by (-0.1, -0.1) to (0.1, 0.1)
                                if ((getFixesXydataset() != null)
                                    && (getFixesXydataset() instanceof XYSeriesCollection)
                                    && (getFixesXydataset().getSeriesCount() == 1)
                                    && (((XYSeriesCollection)getFixesXydataset()).getSeries(0) != null)
                                    && (getFixPoints() != null)
                                    && (Math.abs(getGpsReceiverInstance().getLongitude().toDouble()) > ERROR_KEEP_OUT)
                                    && (Math.abs(getGpsReceiverInstance().getLatitude().toDouble()) > ERROR_KEEP_OUT))
                                    {
                                    final XYSeries seriesFixes;

                                    seriesFixes = ((XYSeriesCollection)getFixesXydataset()).getSeries(0);
                                    seriesFixes.add(getGpsReceiverInstance().getLongitude().toDouble(),
                                                    getGpsReceiverInstance().getLatitude().toDouble());

                                    getFixPoints().add(new Point2D(getGpsReceiverInstance().getLongitude().toDouble(),
                                                                   getGpsReceiverInstance().getLatitude().toDouble()));
                                    setProcessedDataChanged(true);

                                    // Add ObservationMetadata to give the updated centroid of the current Scatter Plot
                                    GpsReceiverHelper.addGpsReceiverCentroidOfFixesMetadata(getObservationMetadata(),
                                                                                            getFixPoints(),
                                                                                            getHostInstrument().isDebugMode());
                                    }
                                else
                                    {
                                    LOGGER.warn(SOURCE + "GPS fix excluded from Centroid");
                                    }

                                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Unable to read GPS fix");
                                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET
                                                                   + "GPS Rx"
                                                                   + TERMINATOR_SPACE
                                                                   + METADATA_ACTION_START
                                                                   + SPACE
                                                                   + METADATA_RESULT + EXCEPTION_GPS_START + TERMINATOR,
                                                               EVENT_SOURCE,
                                                               getObservatoryClock());
                            // The ResponseValue
                            strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                            getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                            }
                        }

                    catch (GpsException exception)
                        {
                        SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET
                                                               + SOURCE.trim()
                                                               + TERMINATOR
                                                               + METADATA_ACTION_COMMAND
                                                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                           EVENT_SOURCE,
                                                           getObservatoryClock());
                        strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                        getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }

                    catch (Exception exception)
                        {
                        exception.printStackTrace();
                        SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET
                                                               + SOURCE.trim()
                                                               + TERMINATOR
                                                               + METADATA_ACTION_COMMAND
                                                               + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                           EVENT_SOURCE,
                                                           getObservatoryClock());
                        strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                        getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }

                    finally
                        {
                        // Make sure that the Receiver has stopped!
                        if (getGpsReceiverInstance() != null)
                            {
                            getGpsReceiverInstance().stop();
                            }
                        }
                    }
                else
                    {
                    LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                                         SOURCE + "Currently disabled");
                    strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                    getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }
                }

            catch (Exception exception)
                {
                exception.printStackTrace();
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET
                                                   + SOURCE.trim()
                                                   + TERMINATOR
                                                   + METADATA_ACTION_COMMAND
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   EVENT_SOURCE,
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
                                                                      commandGetFix,
                                                                      vecRawData,
                                                                      getFixesXydataset(),
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * captureScatterPlot().
     * Capture multiple fixes and plot them on the ScatterPlot tab.
     * The fixes are returned in the DAO ObservationMetadata and in the ResponseValue.
     * This is a capture Command.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureScatterPlot(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.captureScatterPlot() ";
        final int PARAMETER_COUNT = 4;
        final int INDEX_SOURCE = 0;
        final int INDEX_TARGET = 1;
        final int INDEX_INTERVAL = 2;
        final int INDEX_PERIOD = 3;
        final CommandType cmdCaptureScatter;
        final List<ParameterType> listParameters;
        Vector<Object> vecRawData;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(), SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdCaptureScatter = (CommandType)commandmessage.getCommandType().copy();

        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdCaptureScatter),
                                       DAO_CHANNEL_COUNT,
                                       false,
                                       GpsReceiverHelper.createGpsReceiverChannelMetadata(getHostInstrument().isDebugMode()),
                                       null);

        // Prepare some RawData
        vecRawData = null;

        // Clear the DAO's Logs for this run
        getEventLogFragment().clear();
        getInstrumentLogFragment().clear();

        // We expect four parameters, the Update Source and the Update Target, and the Interval and Period
        listParameters = cmdCaptureScatter.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_SOURCE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_SOURCE).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TARGET) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_TARGET).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_INTERVAL) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_INTERVAL).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_PERIOD) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_PERIOD).getInputDataType().getDataTypeName())))
            {
            try
                {
                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;

                if ((boolEnableGPS)
                    && (getGpsReceiverInstance() != null)
                    && (InstrumentState.isDoingSomething(getHostInstrument())))
                    {
                    final String strSourceKey;
                    final String strSource;
                    final String strTargetKey;
                    final String strTarget;
                    final UpdateSource updateSource;
                    final UpdateTarget updateTarget;
                    final String strFixUpdateInterval;
                    final String strFixUpdatePeriod;
                    int intFixUpdateIntervalSec;
                    int intFixUpdatePeriodSec;

                    // Stop any existing Receiver
                    if (getGpsReceiverInstance() != null)
                        {
                        getGpsReceiverInstance().stop();
                        }

                    strSourceKey = MetadataDictionary.KEY_OBSERVATION_ROOT.getKey() + listParameters.get(INDEX_SOURCE).getName();
                    strSource = listParameters.get(INDEX_SOURCE).getValue();
                    updateSource = UpdateSource.getUpdateSourceForName(strSource);

                    strTargetKey = MetadataDictionary.KEY_OBSERVATION_ROOT.getKey() + listParameters.get(INDEX_TARGET).getName();
                    strTarget = listParameters.get(INDEX_TARGET).getValue();
                    updateTarget = UpdateTarget.getUpdateTargetForName(strTarget);

                    strFixUpdateInterval = listParameters.get(INDEX_INTERVAL).getValue();
                    intFixUpdateIntervalSec = Integer.parseInt(strFixUpdateInterval);

                    strFixUpdatePeriod = listParameters.get(INDEX_PERIOD).getValue();
                    intFixUpdatePeriodSec = Integer.parseInt(strFixUpdatePeriod);

                    LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                                         SOURCE + "[fixupdate.source=" + updateSource.getName()
                                         + "] [fixupdate.target=" + updateTarget.getName()
                                         + "] [fixupdate.interval=" + intFixUpdateIntervalSec
                                         + "] [fixupdate.period=" + intFixUpdatePeriodSec + "]");

                    // Start the GpsReceiver afresh, if we are configured correctly
                    // Check for silly parameter settings
                    // FixUpdatePeriod = 0 means run continuously
                    if ((updateSource != null)
                        && (updateTarget != null)
                        && (intFixUpdateIntervalSec > 0)
                        && ((intFixUpdatePeriodSec == 0)
                            || (intFixUpdatePeriodSec >= intFixUpdateIntervalSec))
                        && (getGpsReceiverInstance() != null))
                        {
                        final String strPeriod;
                        final int intCaptureCountMax;
                        boolean boolValidFix;

                        if (intFixUpdatePeriodSec == 0)
                            {
                            strPeriod = CaptureCommandHelper.MSG_PERIOD_CONTINUOUS;
                            }
                        else
                            {
                            strPeriod = Integer.toString(intFixUpdatePeriodSec);
                            }

                        // Set up some basic ObservationMetadata
                        // Location Metadata will be added on a valid fix
                        addAllMetadataToContainersTraced(GpsReceiverHelper.createGpsReceiverChannelMetadata(LOADER_PROPERTIES.isMetadataDebug()),
                                                         SOURCE,
                                                         LOADER_PROPERTIES.isMetadataDebug());

                        // Check that there isn't a conflict between TimeoutPeriod and CapturePeriod
                        // If there is, sort it out
                        if ((intCapturePeriodSec * (int) ChronosHelper.SECOND_MILLISECONDS) > ((getTimeoutMillis(commandmessage.getModule(), cmdCaptureScatter) * 3) >> 2))
                            {
                            final String strMessage;

                            intCapturePeriodSec = getTimeoutMillis(commandmessage.getModule(), cmdCaptureScatter) / (int) ChronosHelper.SECOND_MILLISECONDS;
                            intCapturePeriodSec = intCapturePeriodSec / 3;

                            REGISTRY.setIntegerProperty(getResourceKey() + KEY_DAO_GPS_PERIOD_CAPTURE,
                                                        intCapturePeriodSec);

                            strMessage = METADATA_ACTION_CHANGE
                                             + METADATA_TARGET_PROPERTY
                                             + SPACE + PREFIX
                                             + KEY_DAO_GPS_PERIOD_CAPTURE
                                             + EQUALS
                                             + intCapturePeriodSec
                                             + TERMINATOR;
                            LOGGER.error(strMessage);

                            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               strMessage,
                                                               EVENT_SOURCE,
                                                               getObservatoryClock());
                            }

                        // Check that there isn't a conflict between FixUpdate.Interval and CapturePeriod (which may have changed above)
                        if (intCapturePeriodSec > intFixUpdateIntervalSec)
                            {
                            intFixUpdateIntervalSec = intCapturePeriodSec + 1;
                            }

                        SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_SCATTER_PLOT
                                                               + METADATA_ACTION_CAPTURE
                                                               + METADATA_SOURCE  + updateSource.getName() + TERMINATOR_SPACE
                                                               + METADATA_TARGET + updateTarget.getName() + TERMINATOR_SPACE
                                                               + METADATA_INTERVAL + intFixUpdateIntervalSec + TERMINATOR_SPACE
                                                               + METADATA_PERIOD + strPeriod + TERMINATOR_SPACE
                                                               + METADATA_CAPTURE + intCapturePeriodSec + TERMINATOR,
                                                           getLocalHostname(),
                                                           getObservatoryClock());

                        // Now correct the loop limit for continuous operation
                        if (intFixUpdatePeriodSec == 0)
                            {
                            // Allow about 10 days of operation! (Could be Integer.MAX_VALUE?)
                            intFixUpdatePeriodSec = 1000000;

                            // We musn't timeout, since this might run forever...
                            TimeoutHelper.restartDAOTimeoutTimerInfinite(this);
                            }

                        // Calculate how many *times* the loop should execute
                        intCaptureCountMax = intFixUpdatePeriodSec / intFixUpdateIntervalSec;
                        boolValidFix = false;

                        // Capture the requested number of fixes for the ScatterPlot
                        for (int intCaptureIndex = 0;
                             ((intCaptureIndex < intCaptureCountMax)
                                  && (getGpsReceiverInstance() != null)
                                  && Utilities.executeWorkerCanProceed(this));
                            intCaptureIndex++)
                            {
                            final long longTimeStartMillis;
                            final long longTimeStopMillis;

                            // Record the time of the *first attempt*
                            longTimeStartMillis = System.currentTimeMillis();

                            try
                                {
                                int intRetryCounter;

                                // Try up to GPS_RETRIES times to get a valid GpsReceiver fix
                                boolValidFix = false;
                                intRetryCounter = 0;

                                getGpsReceiverInstance().start();

                                // Check that we are still running,
                                // to save time if the User has stopped the Instrument
                                while((!boolValidFix)
                                          && (intRetryCounter < GPS_RETRIES)
                                          && (getGpsReceiverInstance() != null)
                                          && (getGpsReceiverInstance().isStarted())
                                          && Utilities.executeWorkerCanProceed(this))
                                    {
                                    LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                                                         SOURCE + "retry " + intRetryCounter);
                                    boolValidFix = getGpsReceiverInstance().decodeNMEA(intCapturePeriodSec,
                                                                                       getObservatoryClock());
                                    intRetryCounter++;
                                    }

                                // Make sure that the Receiver has stopped!
                                if (getGpsReceiverInstance() != null)
                                    {
                                    getGpsReceiverInstance().stop();
                                    }

                                // Get the GpsReceiver data and update our location if all is well
                                // If not, return ResponseMessageStatus.RESPONSE_NODATA
                                // Add this entry to the Instrument Log
                                // Even if the InstrumentLog contains only Exceptions, we need to know
                                strResponseValue = GpsReceiverHelper.logFixOutcome(getInstrumentLogFragment(),
                                                                                   getGpsReceiverInstance(),
                                                                                   getObservationMetadata(),
                                                                                   strSourceKey,
                                                                                   updateSource,
                                                                                   strTargetKey,
                                                                                   updateTarget,
                                                                                   boolValidFix,
                                                                                   getObservatoryClock(),
                                                                                   getHostInstrument().isDebugMode());
                                // Were we successful in getting some data?
                                // The actual fix is only held in the ObservationMetaData
                                // The RawData holds the accumulated satellite positions
                                // The XYDataset of accumulated fixes will be used by the ScatterPlot
                                // The ResponseValue is the number of satellites in use and in view,
                                // but this is not used inside the loop

                                if ((!getInstrumentLogFragment().isEmpty())
                                    && (getGpsReceiverInstance() != null)
                                    && (!ResponseMessageStatus.RESPONSE_NODATA.equals(strResponseValue)))
                                    {
                                    // Copy the satellite data from this fix into the DAO map...
                                    // ...and pass it back to the Instrument as one 'channel' of RawData
                                    vecRawData = GpsReceiverHelper.accumulateSatellitesInView(getSatelliteHistory(),
                                                                                              getGpsReceiverInstance().getSatellitesInView());

                                    // The XYDataset of fixes will be used by the ScatterPlot...
                                    // ... but don't add the point (0.0, 0.0) because this is an error flag,
                                    // and the centroid calculation will thereafter be incorrect
                                    // To avoid rounding errors, exclude the area enclosed by (-0.1, -0.1) to (0.1, 0.1)
                                    if ((getFixesXydataset() != null)
                                        && (getFixesXydataset() instanceof XYSeriesCollection)
                                        && (getFixesXydataset().getSeriesCount() == 1)
                                        && (((XYSeriesCollection)getFixesXydataset()).getSeries(0) != null)
                                        && (getFixPoints() != null)
                                        && (Math.abs(getGpsReceiverInstance().getLongitude().toDouble()) > ERROR_KEEP_OUT)
                                        && (Math.abs(getGpsReceiverInstance().getLatitude().toDouble()) > ERROR_KEEP_OUT))
                                        {
                                        final XYSeries seriesFixes;

                                        seriesFixes = ((XYSeriesCollection)getFixesXydataset()).getSeries(0);
                                        seriesFixes.add(getGpsReceiverInstance().getLongitude().toDouble(),
                                                        getGpsReceiverInstance().getLatitude().toDouble());

                                        getFixPoints().add(new Point2D(getGpsReceiverInstance().getLongitude().toDouble(),
                                                                       getGpsReceiverInstance().getLatitude().toDouble()));

                                        // Add ObservationMetadata to give the updated centroid of the current Scatter Plot
                                        GpsReceiverHelper.addGpsReceiverCentroidOfFixesMetadata(getObservationMetadata(),
                                                                                                getFixPoints(),
                                                                                                getHostInstrument().isDebugMode());
                                        }
                                    }

                                // Accept the last ResponseValue from logFixOutcome()
                                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                }

                            catch (GpsException exception)
                                {
                                // Don't terminate the loop, just try again
                                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_SCATTER_PLOT
                                                                        + METADATA_ACTION_CAPTURE
                                                                        + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                                   EVENT_SOURCE,
                                                                   getObservatoryClock());
                                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }

                            // Update the Logs on every attempt, regardless of success
                            if (getHostInstrument().getInstrumentPanel() != null)
                                {
                                getWrappedData().harmoniseWrapperWithDAO(this);
                                // flushLogFragments() requires a valid wrapper
                                getHostInstrument().getInstrumentPanel().flushLogFragments(getWrappedData());
                                }

                            // Were we successful in getting a response?
                            if (boolValidFix)
                                {
                                ResponseMessageHelper.updateWrappedData(this,
                                                                        vecRawData,
                                                                        getFixesXydataset(),
                                                                        true);
                                }

                            // See how long we must wait for the next interval
                            longTimeStopMillis = System.currentTimeMillis();

                            // We may have taken so long with retries that we have exceeded the original FixUpdate.Interval,
                            // in which case we won't wait for the next interval
                            if ((intFixUpdateIntervalSec * ChronosHelper.SECOND_MILLISECONDS) > (longTimeStopMillis - longTimeStartMillis))
                                {
                                Utilities.safeSleepPollExecuteWorker((intFixUpdateIntervalSec * ChronosHelper.SECOND_MILLISECONDS) - (longTimeStopMillis - longTimeStartMillis),
                                                                     this);
                                }
                            }

                        // We always terminate correctly, taking the last ResponseValue
                        // The Wrapper has always been harmonised with the DAO, if successful
                        }
                    else
                        {
                        // We can't do anything at all, for some reason
                        SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_SCATTER_PLOT
                                                                + METADATA_ACTION_CAPTURE
                                                                + METADATA_RESULT
                                                                + ResponseMessageStatus.INVALID_PARAMETER.getName()
                                                                + TERMINATOR_SPACE
                                                                + METADATA_INTERVAL + intFixUpdateIntervalSec + TERMINATOR_SPACE
                                                                + METADATA_PERIOD + intFixUpdatePeriodSec + TERMINATOR,
                                                           EVENT_SOURCE,
                                                           getObservatoryClock());
                        // The ResponseValue
                        strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                        getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_SCATTER_PLOT
                                                       + METADATA_ACTION_CAPTURE
                                                       + METADATA_RESULT
                                                       + "The Receiver is currently disabled"
                                                       + TERMINATOR,
                                                       SOURCE,
                                                       getObservatoryClock());
                    strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                    getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }
                }

            // These are fatal errors which must stop the capture loop

            // This should have been trapped by Regex
            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_SCATTER_PLOT
                                                       + METADATA_ACTION_CAPTURE
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
                                                   METADATA_TARGET_SCATTER_PLOT
                                                       + METADATA_ACTION_CAPTURE
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
                                                   METADATA_TARGET_SCATTER_PLOT
                                                       + METADATA_ACTION_CAPTURE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   EVENT_SOURCE,
                                                   getObservatoryClock());
                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            finally
                {
                // Make sure that the Receiver has stopped!
                if (getGpsReceiverInstance() != null)
                    {
                    getGpsReceiverInstance().stop();
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
                                                                      cmdCaptureScatter,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /**********************************************************************************************/
    /* Exporters - all as Local Commands                                                          */
    /***********************************************************************************************
     * exportSatelliteMap().
     * Saves the current SatelliteMap as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportSatelliteMap(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.exportSatelliteMap()";
        final ResponseMessageInterface responseMessage;

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(), SOURCE);

        // Use the sizes input by the user
        responseMessage = exportComponent(commandmessage,
                                          EXPORTABLE_INDEX_SATELLITE_MAP,
                                          EXPORT_USE_COMPONENT_SIZE);

        return (responseMessage);
        }


    /***********************************************************************************************
     * exportScatterPlot().
     * Saves the current Scatter Plot as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportScatterPlot(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.exportScatterPlot()";

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(), SOURCE);

        return (ExportChart.doExportChart(this, commandmessage));
        }


    /***********************************************************************************************
     * exportGpsFixes().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportGpsFixes(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GpsDAO.exportGpsFixes()";

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(), SOURCE);

        return (ExportProcessedData.doExportProcessedData(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Set the Port associated with the DAO.
     * Initialise the GPS Receiver, just after the DAO is initialised by AbstractObservatoryInstrument.
     *
     * @param daoport
     */

    public void setPort(final DaoPortInterface daoport)
        {
        final String SOURCE = "GpsDAO.setPort() ";
        final boolean boolSuccess;

        super.setPort(daoport);

        if (getGpsReceiverInstance() != null)
            {
            // Only now can we initialise the GPS Receiver,
            // because we need the Port ResourceKey
            boolSuccess = getGpsReceiverInstance().initialise();

            if (!boolSuccess)
                {
                LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                                     SOURCE + "Failed to initialise the GPS Receiver");

                // Try to continue with some commands disabled
                ObservatoryInstrumentHelper.cannotOpenPort(getHostInstrument().getInstrument());
                daoport.close();
                }
            }
        }


    /***********************************************************************************************
     * Get the list of satellites.
     *
     * @return Map{Integer, Vector{SatelliteData}}
     */

    private Map<Integer, Vector<SatelliteData>> getSatelliteHistory()
        {
        return (this.mapSatellites);
        }


    /***********************************************************************************************
     * Get the XYDataset of satellite fixes, for the Scatter Plot.
     *
     * @return XYDataset
     */

    private XYDataset getFixesXydataset()
        {
        return (this.xydatasetFixes);
        }


    /***********************************************************************************************
     * Get the List of satellite fix Points, for the Scatter Plot.
     *
     * @return List<Point2D>
     */

    private List<Point2D> getFixPoints()
        {
        return (this.listFixes);
        }


    /***********************************************************************************************
     * Get the instance of the GPS Receiver.
     *
     * @return GpsInstrumentReceiverInterface
     */

    private GpsInstrumentReceiverInterface getGpsReceiverInstance()
        {
        return (this.gpsReceiverInstance);
        }


    /***********************************************************************************************
     * Set the instance of the GPS Receiver.
     *
     * @param receiver
     */

    private void setGpsReceiverInstance(final GpsInstrumentReceiverInterface receiver)
        {
        this.gpsReceiverInstance = receiver;
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     *
     * KEY_DAO_GPS_RECEIVER_CLASS_NAME
     * KEY_DAO_GPS_RECEIVER_TYPE
     * KEY_DAO_GPS_ENABLE_RECEIVER
     * KEY_DAO_GPS_PERIOD_CAPTURE
     */

    public void readResources()
        {
        final String SOURCE = "GpsDAO.readResources() ";

        LOGGER.debugGpsEvent(getHostInstrument().isDebugMode(),
                             SOURCE + "[ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        // GPS Receiver
        strReceiverClassName = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_GPS_RECEIVER_CLASS_NAME);
        strReceiverType = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_GPS_RECEIVER_TYPE);
        boolEnableGPS = REGISTRY.getBooleanProperty(getResourceKey() + KEY_DAO_GPS_ENABLE_RECEIVER);
        intCapturePeriodSec = REGISTRY.getIntegerProperty(getResourceKey() + KEY_DAO_GPS_PERIOD_CAPTURE);

        // Trap the careless use of the Capture Period
        if ((intCapturePeriodSec < CAPTURE_PERIOD_MIN_SEC)
            || (intCapturePeriodSec > CAPTURE_PERIOD_MAX_SEC))
            {
            intCapturePeriodSec = CAPTURE_PERIOD_MIN_SEC;
            LOGGER.error(SOURCE
                             + DOT + KEY_DAO_GPS_PERIOD_CAPTURE
                             + " is set incorrectly. Using a value of 1sec.");
            }
        }
    }