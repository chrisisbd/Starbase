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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.GetConfiguration;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.GetModuleConfiguration;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.GetVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.SetModuleConfiguration;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core.Ping;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core.Reset;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core.Start;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core.Stop;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.datasets.ApplyFilter;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.datasets.ApplyLinearTransform;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.datasets.EvaluateTemperatureCorrelation;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.datasets.SegmentTimeSeries;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.metadata.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.utilities.CopyResponseToClipboard;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.utilities.Wait;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.xmlbeans.instruments.CommandCategory;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.Timer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;


/***************************************************************************************************
 * AbstractObservatoryInstrumentDAO.
 */

public abstract class AbstractObservatoryInstrumentDAO implements ObservatoryInstrumentDAOInterface,
                                                                  FrameworkConstants,
                                                                  FrameworkStrings,
                                                                  FrameworkMetadata,
                                                                  FrameworkSingletons,
                                                                  ObservatoryConstants
    {
    // Injections
    private final ObservatoryInstrumentInterface instrument;

    // Translates Imported data to internal format
    private DataTranslatorInterface translatorInterface;

    // Filters RawData to make ProcessedData
    private DataFilterInterface filterInterface;

    private DaoPortInterface port;
    private String strResourceKey;

    // All method names accessible to this DAO
    private final CommandPoolList listCommandPool;

    //----------------------------------------------------------------------------------------------
    // The following are available via the DAO Wrapper

    // Metadata containers
    private List<Metadata> listMetadataMetadata;
    private List<Metadata> listCurrentObservatoryMetadata;
    private List<Metadata> listCurrentObserverMetadata;
    private List<Metadata> listObservationMetadata;
    private List<Metadata> listInstrumentMetadata;
    private List<Metadata> listControllerMetadata;
    private List<Metadata> listPluginMetadata;
    private List<Metadata> listRawDataMetadata;
    private List<Metadata> listXYDatasetMetadata;

    // POIs and LOIs
    private List<PointOfInterest> listPOIs;
    private List<LineOfInterest> listLOIs;

    // Data produced by this DAO
    private Vector<Object> vecRawData;
    private XYDataset xyDataset;
    private Image imageData;
    private Object userObject;

    // The currently selected Chart for use with this DAO
    private ChartUIComponentPlugin associatedChartUI;

    private int intRawDataChannelCount;
    private boolean boolTemperatureChannel;

    // Data Status
    private boolean boolUnsavedData;
    private boolean boolDatasetTypeChanged;
    private boolean boolChannelCountChanged;
    private boolean boolMetadataChanged;
    private boolean boolRawDataChanged;
    private boolean boolProcessedDataChanged;

    // DAO Logging
    private Vector<Vector> vecEventLogFragment;
    private List<Metadata> listEventLogMetadata;
    private Vector<Vector> vecInstrumentLogFragment;
    private List<Metadata> listInstrumentLogMetadata;

    // End of DAO Wrapper
    //----------------------------------------------------------------------------------------------

    private final Vector<Vector> vecDAOConfiguration;

    private DAOWrapperInterface daoWrapperInterface;

    // DAO Control
    private SwingWorker workerExecute;
    private Timer timerTimeout;
    private Timer timerRepeat;
    private final ResponseMessageStatusList listResponseMessageStatus;
    private ExecutionStatus executionStatus;

    // Context sensitive help
    private final List<ResourceBundle> listBundles;

    // RemoteDataConnection Configuration
    private RemoteDataConnectionInterface dataConnection;

    // DAO debugging mode
    private boolean boolDebug;


    //---------------------------------------------------------------------------------------------

    // Configurable Resources
    //
    // KEY_DAO_ONERROR_CONTINUE
    // KEY_DAO_TIMEOUT_DEFAULT

    private boolean boolOnErrorContinue;
    private int intTimeoutDefaultMillis;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void buildRootCommandPool(final CommandPoolList pool)
        {
        // Core
        pool.add("reset");
        pool.add("ping");
        pool.add("getConfiguration");
        pool.add("getModuleConfiguration");
        pool.add("setModuleConfiguration");
        pool.add("start");
        pool.add("stop");
        pool.add("getVersion");

        // Utilities
        pool.add("copyResponseToClipboard");
        pool.add("wait");

        // Metadata
        pool.add("importMetadata");
        pool.add("importMetadataLocal");
        pool.add("setMetadataValue");
        pool.add("removeMetadata");
        pool.add("importPointsOfInterest");
        pool.add("removePointsOfInterest");
        pool.add("addToposWMMMetadata");
        pool.add("addVLSRMetadata");

        // DataProcessor
        pool.add("evalTemperatureCorrelation");
        pool.add("segmentTimeSeries");
        pool.add("applyFilter");
        pool.add("applyLinearTransform");
        pool.add("appendDataset");

        // Importers
        pool.add("importRawDataLocal");
        pool.add("importRawDataRemote");
        pool.add("importRawDataRemoteIncrement");
        pool.add("importRawDataRemoteServer");

        // Exporters
        pool.add("exportCommandLog");
        pool.add("exportChart");
        pool.add("exportProcessedData");
        pool.add("exportRawData");
        pool.add("exportEphemeris");
        pool.add("exportStarMap");
        pool.add("exportRegionalMap");
        pool.add("exportInstrumentLog");
        pool.add("exportEventLog");
        pool.add("exportMetadata");
        pool.add("exportConfiguration");
        pool.add("exportCommandLexicon");
        pool.add("exportInstrumentXML");
        }


    /***********************************************************************************************
     * Construct an AbstractObservatoryInstrumentDAO.
     *
     * @param hostinstrument
     */

    public AbstractObservatoryInstrumentDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        // Injections
        this.instrument = hostinstrument;

        this.translatorInterface = null;
        this.filterInterface = null;
        this.port = null;

        this.listCommandPool = new CommandPoolList(100);

        // Initialise all DAO MetaData containers
        this.listMetadataMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listCurrentObservatoryMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listCurrentObserverMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listObservationMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listInstrumentMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listControllerMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listPluginMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listRawDataMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listXYDatasetMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);

        // POIs and LOIs
        this.listPOIs = new ArrayList<PointOfInterest>(INITIAL_CAPACITY);
        this.listLOIs = new ArrayList<LineOfInterest>(INITIAL_CAPACITY);

        // Data containers
        this.vecRawData = null;
        this.xyDataset = null;
        this.imageData = null;
        this.userObject = null;
        this.associatedChartUI = null;

        this.intRawDataChannelCount = 0;
        this.boolTemperatureChannel = false;

        // Data Status
        this.boolUnsavedData = false;
        this.boolDatasetTypeChanged = false;
        this.boolChannelCountChanged = false;
        this.boolMetadataChanged = false;
        this.boolRawDataChanged = false;
        this.boolProcessedDataChanged = false;

        // Logging
        this.vecInstrumentLogFragment = new Vector<Vector>(100);
        this.listInstrumentLogMetadata = new ArrayList<Metadata>(10);
        this.vecEventLogFragment = new Vector<Vector>(100);
        this.listEventLogMetadata = new ArrayList<Metadata>(10);

        // Wrap it all up!
        this.daoWrapperInterface = new DAOWrapper(null, null, EMPTY_STRING, this);

        this.vecDAOConfiguration = new Vector<Vector>(10);

        this.workerExecute = null;
        this.timerTimeout = null;
        this.timerRepeat = null;
        this.listResponseMessageStatus = ResponseMessageStatus.createResponseMessageStatusList();
        this.executionStatus = ExecutionStatus.INITIALISED;

        this.listBundles = new ArrayList<ResourceBundle>(5);

        // Initialise the Resources
        this.boolOnErrorContinue = true;
        this.intTimeoutDefaultMillis = 10 * (int) ChronosHelper.SECOND_MILLISECONDS;
        //this.intPeriodUpdateMilliSec = 10 * (int) ChronosHelper.SECOND_MILLISECONDS;

        this.dataConnection = null;

        this.boolDebug = false;

        buildRootCommandPool(this.listCommandPool);
        }


    /***********************************************************************************************
     * Initialise the DAO.
     * Be careful to take account of the flag which indicates if the host Instrument consumes
     * the data from this DAO. If not, e.g. leave the **host's** data containers alone!
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.initialiseDAO() ";

        setDebugMode(LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        //-----------------------------------------------------------------------------------------
        // The following occur for all kinds of DAO

        // Override in subclasses
        this.strResourceKey = resourcekey;

        LOGGER.debug(boolDebug,
                     SOURCE + "[resourcekey=" + resourcekey + "]");

        // Get the latest DAO Resources
        readResources();

        // This is the first to be added to the List of ResourceBundles
        DAOHelper.loadCommonResourceBundle(this);

        // Stop any existing timeout Timer
        if (getTimeoutTimer() != null)
            {
            getTimeoutTimer().stop();
            setTimeoutTimer(null);
            }

        // Stop any existing repeat Timer
        if (getRepeatTimer() != null)
            {
            getRepeatTimer().stop();
            setRepeatTimer(null);
            }

        // Set up a Timer to do the timeouts
        // Creates a Timer and initializes both the initial delay and between-event delay
        // to delay milliseconds.
        // If delay is less than or equal to zero,  the timer fires as soon as it is started.
        // If listener is not null, it's registered as an action listener on the timer.
        setTimeoutTimer(new Timer(intTimeoutDefaultMillis,
                                  new ActionListener()
                                  {
                                  public void actionPerformed(final ActionEvent event)
                                      {
                                      final String SOURCE = "AbstractObservatoryInstrumentDAO Timer.actionPerformed() ";

                                      if (getTimeoutTimer() != null)
                                          {
                                          LOGGER.debug(boolDebug,
                                                       SOURCE + "DAO Timeout occurred [delay=" + getTimeoutTimer().getDelay() + "]");
                                          }
                                      else
                                          {
                                          LOGGER.debug(boolDebug,
                                                       SOURCE + "DAO TIMEOUT Timer is NULL");
                                          }

                                      // We have timed out!
                                      getResponseMessageStatusList().add(ResponseMessageStatus.TIMEOUT);
                                      }
                                  }));

        // Ensure that the Timer is single-shot
        getTimeoutTimer().setRepeats(false);

        //-----------------------------------------------------------------------------------------
        // The following should occur ONLY if the host Instrument consumes data from this DAO!

        if (isInstrumentDataConsumer())
            {
            // Stop any existing SwingWorker running in this DAO
            if (getExecuteWorker() != null)
                {
                // Treat the initialise() as ABORT to control the SwingWorker
                getResponseMessageStatusList().add(ResponseMessageStatus.ABORT);

                SwingWorker.disposeWorker(getExecuteWorker(), true, SWING_WORKER_STOP_DELAY);
                setExecuteWorker(null);
                }
            else
                {
                // Set a Status in case nothing else does...
                // ToDo Review this status
                getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }

            // See if we need to inform listeners of MetadataChanged events
            // Listen to the Observatory and Observer Metadata
            if ((getHostInstrument() != null)
                && (getHostInstrument().getContext() != null)
                && (getHostInstrument().getContext().getObservatory() != null))
                {
                getHostInstrument().getContext().getObservatory().addObservatoryMetadataChangedListener(this);
                getHostInstrument().getContext().getObservatory().addObserverMetadataChangedListener(this);
                }
            }

        // Initialise all DAO data containers and set an empty Wrapper
        // This takes account of isInstrumentDataConsumer()
        clearData();

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.disposeDAO() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(), SOURCE);
        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(), SOURCE);

        // Dispose of any RemoteDataConnection
        if (getRemoteDataConnection() != null)
            {
            getRemoteDataConnection().dispose();
            setRemoteDataConnection(null);
            }

        // Stop listening to MetadataChanged events
        if ((getHostInstrument() != null)
            && (getHostInstrument().getContext() != null)
            && (getHostInstrument().getContext().getObservatory() != null))
            {
            getHostInstrument().getContext().getObservatory().removeObservatoryMetadataChangedListener(this);
            getHostInstrument().getContext().getObservatory().removeObserverMetadataChangedListener(this);
            }

        // Stop any existing SwingWorker
        if (getExecuteWorker() != null)
            {
            // Treat the dispose() as ABORT
            getResponseMessageStatusList().add(ResponseMessageStatus.ABORT);

            SwingWorker.disposeWorker(getExecuteWorker(), true, SWING_WORKER_STOP_DELAY);
            setExecuteWorker(null);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "SwingWorker stopped");
            }
        else
            {
            getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }

        // Stop any timeout Timer
        if (getTimeoutTimer() != null)
            {
            getTimeoutTimer().stop();
            setTimeoutTimer(null);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Timeout stopped");
            }

        // Stop any repeat Timer
        if (getRepeatTimer() != null)
            {
            getRepeatTimer().stop();
            setRepeatTimer(null);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Repeat stopped");
            }

        getCommandPool().clear();
        }


    /**********************************************************************************************/
    /* DAO Core Commands                                                                          */
    /***********************************************************************************************
     * reset() resets the whole Instrument.
     * This local Command may be overridden by a Command which sends a reset() to the DAO Port.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface reset(final CommandMessageInterface commandmessage)
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "Entered DAO reset");

        return Reset.doReset(this, commandmessage);
        }


    /***********************************************************************************************
     * ping().
     * This may be overridden by a Command which sends a ping() to the DAO Port.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface ping(final CommandMessageInterface commandmessage)
        {
        return Ping.doPing(this, commandmessage);
        }


    /***********************************************************************************************
     * getConfiguration().
     *
     * getConfiguration() reads the memories of all connected Modules,
     * and forms the XML configuration of the composite Instrument.
     * This Command is executed entirely on the host.     *
     * This may be overridden by a Command which sends a getConfiguration() directly to the DAO Port.
     * This may return XML from the Port (usually retrieved in blocks of 512 bytes),
     * or directly from a virtual Instrument implemented completely on the host.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getConfiguration(final CommandMessageInterface commandmessage)
        {
        return GetConfiguration.doGetConfiguration(this, commandmessage);
        }


    /***********************************************************************************************
     * getModuleConfiguration().
     *
     * getModuleConfiguration() first uses getConfigurationBlockCount() to get the number of blocks
     * then iterates over getConfigurationBlock() to get all blocks from the specified ModuleID.
     * These sub-commands are specified through the use of the <BlockedDataCommand> element.
     * If a Module is missing, then the ResponseValue must be 'NODATA'.
     *
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getModuleConfiguration(final CommandMessageInterface commandmessage)
        {
        return (GetModuleConfiguration.doGetModuleConfiguration(this, commandmessage));
        }


    /***********************************************************************************************
     * setModuleConfiguration().
     *
     * setModuleConfiguration() iterates over setConfigurationBlock()
     * to set all blocks in the specified ModuleID,
     * using the configuration data in the specified filename
     * These sub-commands are specified through the use of the <BlockedDataCommand> element.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setModuleConfiguration(final CommandMessageInterface commandmessage)
        {
        return (SetModuleConfiguration.doSetModuleConfiguration(this, commandmessage));
        }


    /***********************************************************************************************
     * start().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface start(final CommandMessageInterface commandmessage)
        {
        return (Start.doStart(this, commandmessage));
        }


    /***********************************************************************************************
     * stop().
     * See the code in ObservatoryInstrumentHelper.createButtonPanel().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface stop(final CommandMessageInterface commandmessage)
        {
        return (Stop.doStop(this, commandmessage));
        }


    /***********************************************************************************************
     * getVersion().
     * This may be overridden by a Command which sends a getVersion() to the DAO Port.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getVersion(final CommandMessageInterface commandmessage)
        {
        return (GetVersion.doGetVersion(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * copyResponseToClipboard().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface copyResponseToClipboard(final CommandMessageInterface commandmessage)
        {
        return (CopyResponseToClipboard.doCopyResponseToClipboard(this, commandmessage));
        }


    /***********************************************************************************************
     * wait().
     * Waits for the specified number of Seconds.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface wait(final CommandMessageInterface commandmessage)
        {
        return (Wait.doWait(this, commandmessage));
        }


    /**********************************************************************************************/
    /* DataProcessor                                                                              */
    /***********************************************************************************************
     * applyLinearTransform().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface applyLinearTransform(final CommandMessageInterface commandmessage)
        {
        return (ApplyLinearTransform.doApplyLinearTransform(this, commandmessage));
        }


    /***********************************************************************************************
     * segmentTimeSeries().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface segmentTimeSeries(final CommandMessageInterface commandmessage)
        {
        return (SegmentTimeSeries.doSegmentTimeSeries(this, commandmessage));
        }


    /***********************************************************************************************
     * applyFilter().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface applyFilter(final CommandMessageInterface commandmessage)
        {
        return (ApplyFilter.doApplyFilter(this, commandmessage));
        }


    /***********************************************************************************************
     * appendDataset().
     * Append the Dataset imported from a local file to the data already held by the DAO.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface appendDataset(final CommandMessageInterface commandmessage)
        {
//        return (AppendDataset.doAppendDataset(this, commandmessage));
        return (AwaitingDevelopment.doAwaitingDevelopment(this, commandmessage));
        }


    /***********************************************************************************************
     * importMetadata().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importMetadata(final CommandMessageInterface commandmessage)
        {
        return (ImportMetadata.doImportMetadata(this, commandmessage));
        }


    /***********************************************************************************************
     * importMetadataLocal().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importMetadataLocal(final CommandMessageInterface commandmessage)
        {
        return (ImportMetadataLocal.doImportMetadataLocal(this, commandmessage));
        }


    /***********************************************************************************************
     * setMetadataValue().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setMetadataValue(final CommandMessageInterface commandmessage)
        {
        return (SetMetadataValue.doSetMetadataValue(this, commandmessage));
        }


    /***********************************************************************************************
     * removeMetadata().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface removeMetadata(final CommandMessageInterface commandmessage)
        {
        return (RemoveMetadata.doRemoveMetadata(this, commandmessage));
        }


    /***********************************************************************************************
     * importPointsOfInterest().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importPointsOfInterest(final CommandMessageInterface commandmessage)
        {
        return (ImportPointsOfInterest.doImportPointsOfInterest(this, commandmessage));
        }


    /***********************************************************************************************
     * removePointsOfInterest().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface removePointsOfInterest(final CommandMessageInterface commandmessage)
        {
        return (RemovePointsOfInterest.doRemovePointsOfInterest(this, commandmessage));
        }


    /***********************************************************************************************
     * addToposWMMMetadata().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface addToposWMMMetadata(final CommandMessageInterface commandmessage)
        {
        return (AddWMMMetadata.doAddToposWMMMetadata(this, commandmessage));
        }


    /***********************************************************************************************
     * addVLSRMetadata().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface addVLSRMetadata(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SpectraCyberDAO.addVLSRMetadata()";

        // Only generate a ResponseMessage when completed
        return (AddVLSRMetadata.doAddVlsrMetadata(this,
                                                  commandmessage,
                                                  SOURCE));
        }


    /***********************************************************************************************
     * evalTemperatureCorrelation().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface evalTemperatureCorrelation(final CommandMessageInterface commandmessage)
        {
        return (EvaluateTemperatureCorrelation.doEvaluateTemperatureCorrelation(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Importers                                                                                  */
    /* A subset may be available to any DAO                                                       */
    /***********************************************************************************************
     * importRawDataLocal().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importRawDataLocal(final CommandMessageInterface commandmessage)
        {
        return (ImportRawDataLocal.doImportRawDataLocal(this, commandmessage));
        }


    /***********************************************************************************************
     * importRawDataRemote().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importRawDataRemote(final CommandMessageInterface commandmessage)
        {
        return (ImportRawDataRemote.doImportRawDataRemote(this, commandmessage));
        }


    /***********************************************************************************************
     * importRawDataRemoteIncrement().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importRawDataRemoteIncrement(final CommandMessageInterface commandmessage)
        {
        return (ImportRawDataRemoteIncrement.doImportRawDataRemoteIncrement(this, commandmessage));
        }


    /***********************************************************************************************
     * importRawDataRemoteServer().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importRawDataRemoteServer(final CommandMessageInterface commandmessage)
        {
        return (ImportRawDataRemoteServer.doImportRawDataRemoteServer(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Exporters - all as Local Commands                                                          */
    /* A subset may be available to any DAO                                                       */
    /***********************************************************************************************
     * exportCommandLog().
     * Saves the Instrument Command Log at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportCommandLog(final CommandMessageInterface commandmessage)
        {
        return ExportCommandLog.doExportCommandLog(this, commandmessage);
        }


    /***********************************************************************************************
     * exportChart().
     * Saves the current chart as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportChart(final CommandMessageInterface commandmessage)
        {
        return (ExportChart.doExportChart(this, commandmessage));
        }


    /***********************************************************************************************
     * exportXYDataset().
     * Saves the current processed data (i.e. the XYDataset used for the Chart)
     * at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportProcessedData(final CommandMessageInterface commandmessage)
        {
        return (ExportProcessedData.doExportProcessedData(this, commandmessage));
        }


    /***********************************************************************************************
     * exportRawData().
     * Saves the current raw data at the specified location, in the specified DataFormat.
     * Optionally timestamp the filename.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportRawData(final CommandMessageInterface commandmessage)
        {
        return (ExportRawData.doExportRawData(this, commandmessage));
        }


    /***********************************************************************************************
     * exportEphemeris().
     * Saves the Ephemeris at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportEphemeris(final CommandMessageInterface commandmessage)
        {
        return (ExportEphemeris.doExportEphemeris(this, commandmessage));
        }


    /***********************************************************************************************
     * exportStarMap().
     * Saves the current StarMap as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportStarMap(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.exportStarMap() ";
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(), SOURCE);

        // Use the sizes input by the user
        responseMessage = exportComponent(commandmessage,
                                          EXPORTABLE_INDEX_STAR_MAP,
                                          EXPORT_USE_COMPONENT_SIZE);

        return (responseMessage);
        }


    /***********************************************************************************************
     * exportRegionalMap().
     * Saves the current RegionalMap as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportRegionalMap(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.exportRegionalMap()";
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(), SOURCE);

        // Use the sizes of the component itself
        responseMessage = exportComponent(commandmessage,
                                          EXPORTABLE_INDEX_REGIONAL_MAP,
                                          EXPORT_IGNORE_COMPONENT_SIZE);

        return (responseMessage);
        }


    /***********************************************************************************************
     * exportInstrumentLog().
     * Saves the current InstrumentLog at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportInstrumentLog(final CommandMessageInterface commandmessage)
        {
        return (ExportInstrumentLog.doExportInstrumentLog(this, commandmessage));
        }


    /***********************************************************************************************
     * exportEventLog().
     * Saves the current EventLog at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportEventLog(final CommandMessageInterface commandmessage)
        {
        return (ExportEventLog.doExportEventLog(this, commandmessage));
        }


    /***********************************************************************************************
     * exportMetadata().
     * Saves the current Metadata at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportMetadata(final CommandMessageInterface commandmessage)
        {
        return (ExportMetadata.doExportMetadata(this, commandmessage));
        }


    /***********************************************************************************************
     * exportConfiguration().
     * Saves the current Instrument configuration at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportConfiguration(final CommandMessageInterface commandmessage)
        {
        return (ExportConfiguration.doExportConfiguration(this, commandmessage));
        }


    /***********************************************************************************************
     * exportCommandLexicon().
     * Saves the Instrument Command Lexicon at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportCommandLexicon(final CommandMessageInterface commandmessage)
        {
        return (ExportCommandLexicon.doExportCommandLexicon(this, commandmessage));
        }


    /***********************************************************************************************
     * exportInstrumentXML().
     * Saves the current Instrument XML at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportInstrumentXML(final CommandMessageInterface commandmessage)
        {
        return (ExportInstrumentXML.doExportInstrumentXML(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Command Helpers                                                                            */
    /***********************************************************************************************
     * exportReportTable().
     * Saves the data from a general ReportTable at the specified location.
     * The DatasetType is used to refine the Metadata describing the columns.
     * Optional extra metadata may be added to the export.
     *
     * @param source
     * @param commandmessage
     * @param metadatametadata
     * @param metadata
     * @param infercolumnmetadata
     * @param datasettype
     * @param report
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportReportTable(final String source,
                                                      final CommandMessageInterface commandmessage,
                                                      final List<Metadata> metadatametadata,
                                                      final List<Metadata> metadata,
                                                      final boolean infercolumnmetadata,
                                                      final DatasetType datasettype,
                                                      final ReportTablePlugin report)
        {
        return (ExportReportTable.doExportReportTable(this,
                                                      source,
                                                      commandmessage,
                                                      metadatametadata,
                                                      metadata,
                                                      infercolumnmetadata,
                                                      datasettype,
                                                      report));
        }


    /***********************************************************************************************
     * exportComponent().
     * Saves a visual component at componentindex as an image at the specified location.
     * Optionally use the size entered by the user.
     *
     * @param commandmessage
     * @param componentindex
     * @param usesize
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportComponent(final CommandMessageInterface commandmessage,
                                                    final int componentindex,
                                                    final boolean usesize)
        {
        return (ExportComponent.doExportComponent(this,
                                                  commandmessage,
                                                  componentindex,
                                                  usesize));
        }


    /**********************************************************************************************/
    /* Command Execution                                                                          */
    /***********************************************************************************************
     * Repeatedly execute a StarScript Command (assumed to be valid).
     * Return true if the Command was executed at least once.
     * Repeat either every repeatcount millisec, or repeatcount times, controlled by repeatmode.
     *
     * @param context
     * @param errors
     *
     * @return boolean
     */

    public synchronized boolean repeatCommand(final CommandProcessorContextInterface context,
                                              final List<String> errors)
        {
        final boolean boolSuccessful;

        // Stop any existing repeat Timer (should never happen...)
        if (getRepeatTimer() != null)
            {
            getRepeatTimer().stop();
            setRepeatTimer(null);
            }

        // Which repeat mode do we need?
        if (context.isRepeatPeriodMode())
            {
            // Range check the repeat period
            if (context.getRepeatPeriodMillis() >= ChronosHelper.SECOND_MILLISECONDS)
                {
                // Repeat every repeatmsec millisec
                // This will be stopped by the DAO TimeoutTimer,
                // or as a result of the Queue returning NULL, indicating TIMEOUT or ABORT
                // This is the only initialisation
                // Creates a Timer and initializes both the initial delay and between-event delay
                // to delay milliseconds.
                // If delay is less than or equal to zero,  the timer fires as soon as it is started.
                // If listener is not null, it's registered as an action listener on the timer.
                setRepeatTimer(new Timer((int)context.getRepeatPeriodMillis(),
                                        new ActionListener()
                                            {
                                            public void actionPerformed(final ActionEvent event)
                                                {
                                                //LOGGER.debugProtocolEvent("AbstractObservatoryInstrumentDAO.repeatCommand() ActionListener.actionPerformed() REPEAT [period=" + context.getRepeatPeriodMillis() + "]");

                                                // Don't allow another repeat if we timed out or something...
                                                if (ExecutionStatus.FINISHED.equals(getExecutionStatus()))
                                                    {
                                                    final boolean boolSuccess;

                                                    // Remember that the execution ResponseStatus comes back on this Thread immediately
                                                    // It simply indicates that the command was queued for execution
                                                    // This is always a repeating execution of the Command,
                                                    // which keeps the Instrument state in REPEATING until we are done

                                                    if ((context.getSelectedMacro() != null)
                                                        && (!context.getSelectedMacro().isEmpty()))
                                                        {
                                                        // Execute the Macro
                                                        boolSuccess = ExecuteMacroHelper.executeMacro(context.getObservatoryInstrument(),
                                                                                                      context.getInstrument(),
                                                                                                      context.getSelectedModule().get(0),
                                                                                                      context.getSelectedMacro().get(0),
                                                                                                      context.getStarscript(),
                                                                                                      context.isRepeatPeriodMode(),
                                                                                                      context.getRepeatPeriodMillis() / ChronosHelper.SECOND_MILLISECONDS,
                                                                                                      context.getRepeatText(),
                                                                                                      errors);
                                                        }
                                                    else if ((context.getSelectedCommand() != null)
                                                        && (!context.getSelectedCommand().isEmpty()))
                                                        {
                                                        // Execute the Command
                                                        boolSuccess = ExecuteCommandHelper.executeCommand(context.getObservatoryInstrument(),
                                                                                                          context.getInstrument(),
                                                                                                          context.getSelectedModule().get(0),
                                                                                                          context.getSelectedCommand().get(0),
                                                                                                          context.getExecutionParameters(),
                                                                                                          context.getStarscript(),
                                                                                                          context.isRepeatPeriodMode(),
                                                                                                          context.getRepeatPeriodMillis() / ChronosHelper.SECOND_MILLISECONDS,
                                                                                                          context.getRepeatText(),
                                                                                                          errors);
                                                        }
                                                    else
                                                        {
                                                        boolSuccess = false;
                                                        }

                                                    if ((!boolSuccess)
                                                        && (errors != null))
                                                        {
                                                        errors.add(ObservatoryInstrumentInterface.COMMAND_FAILED + "[command=" + context.getStarscript() + "]");
                                                        }
                                                    }
                                                else
                                                    {
                                                    LOGGER.logTimedEvent("AbstractObservatoryInstrumentDAO.repeatCommand() Repeat deferred because waiting for command to complete");

                                                    // Issue a warning that the repeat time is too short
                                                    SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                                                       EventStatus.WARNING,
                                                                                       METADATA_TARGET_COMMAND + SPACE
                                                                                           + METADATA_ACTION_EXECUTE
                                                                                           + METADATA_WARNING + "repeat deferred waiting for command to complete" + TERMINATOR,
                                                                                       getLocalHostname(),
                                                                                       getObservatoryClock());
                                                    }
                                                }
                                            }));

                // Ensure that the Timer repeats!
                getRepeatTimer().setRepeats(true);

                // Override the initial delay so that it starts effectively immediately
                getRepeatTimer().setInitialDelay(100);
                getRepeatTimer().start();

                // Now that the RepeatTimer is running, the only way out is via ABORT or TIMEOUT
                // The Instrument state should remain in REPEATING
                boolSuccessful = true;
                }
            else
                {
                if (errors != null)
                    {
                    errors.add(ObservatoryInstrumentInterface.COMMAND_ILLEGAL_ARGUMENT + " [period=" + context.getRepeatPeriodMillis() + "msec]");
                    }

                boolSuccessful = false;
                }
            }
        else
            {
            // Range check the repeat count
            if ((context.getRepeatCount() >= 1)
                && (context.getRepeatCount() <= MAX_REPEAT_COUNT))
                {
//                // Repeat for repeatcount times
//                for (int i = 0; i < repeatcount; i++)
//                    {
//                    boolean boolSuccess;
//
//                    // Remember that the execution ResponseStatus comes back on this Thread immediately
//                    // It simply indicates that the command was queued for execution
//                    // This is always a repeating execution of the Command,
//                    // which keeps the Instrument state in REPEATING until we are done
//                    boolSuccess = executeCommand(obsinstrument,
//                                                 instrumentxml,
//                                                 module,
//                                                 command,
//                                                 starscript,
//                                                 true,
//                                                 errors);
//                    if ((!boolSuccess)
//                        && (errors != null))
//                        {
//                        errors.add(ObservatoryInstrumentInterface.COMMAND_FAILED + "[command=" + starscript + "]");
//                        }
//
//                    LOGGER.debugTimedEvent("AbstractObservatoryInstrumentDAO.repeatCommand() [count] success=" + boolSuccess);
//                    }
//
//                //setResponseStatus(ResponseMessageStatus.SUCCESS);
//                //obsinstrument.setInstrumentState(InstrumentState.READY);
//                //obsinstrument.notifyInstrumentStateChangedEvent(this, obsinstrument, obsinstrument.getInstrumentState());
//                boolSuccessful = true;

                if (errors != null)
                    {
                    errors.add(ObservatoryInstrumentInterface.COMMAND_NOT_AVAILABLE);
                    }

                boolSuccessful = false;
                }
            else
                {
                if (errors != null)
                    {
                    errors.add(ObservatoryInstrumentInterface.COMMAND_ILLEGAL_ARGUMENT + " [count=" + context.getRepeatCount() + "]");
                    }

                boolSuccessful = false;
                }
            }

        return (boolSuccessful);
        }


    /***********************************************************************************************
     * Abort the execution of any Command.
     *
     * @param obsinstrument
     * @param instrumentxml
     * @param module
     * @param command
     * @param errors
     */

    public synchronized void abortCommand(final ObservatoryInstrumentInterface obsinstrument,
                                          final Instrument instrumentxml,
                                          final XmlObject module,
                                          final CommandType command,
                                          final List<String> errors)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.abortCommand() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        LOGGER.debug(boolDebug, SOURCE);

        // Stop any existing SwingWorker
        if (getExecuteWorker() != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Requesting Execute SwingWorker to stop");

            // Indicate that this is an Abort, not a Timeout (not visible for long)
            getResponseMessageStatusList().add(ResponseMessageStatus.ABORT);

            SwingWorker.disposeWorker(getExecuteWorker(), true, SWING_WORKER_STOP_DELAY);
            setExecuteWorker(null);
            getResponseMessageStatusList().add(ResponseMessageStatus.ABORT);
            }
        else
            {
            // If there is no SwingWorker, there is no need to indicate ABORT
            getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }

        // Time passes while the SwingWorker Thread stops...

        // Try to stop any pending remote connection (again)
        if (getRemoteDataConnection() != null)
            {
            getRemoteDataConnection().dispose();
            }

        // Stop any timeout Timer
        if (getTimeoutTimer() != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Requesting Timeout Timer to stop");
            getTimeoutTimer().stop();
            }

        // Stop any repeat Timer
        if (getRepeatTimer() != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Requesting Repeat Timer to stop");
            getRepeatTimer().stop();
            }

        // Indicate that the Command execution was interrupted: BUSY --> READY, or REPEATING --> READY
        // This action is duplicated if there was a SwingWorker
        if (getPort() != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Setting Port Ready");
            getPort().setPortBusy(false);

            // Clear the Queues and Streams, since we know that all activity is over for now
            // This is to trap anomalies in Timeouts
            getPort().clearQueues();
            getPort().getTxStream().reset();
            getPort().getRxStream().reset();
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Port was NULL");
            }

        // Force a return to READY
        // This action is duplicated if there was a SwingWorker
        // Check that we haven't been STOPPED before returning to READY
        if (!InstrumentState.STOPPED.equals(obsinstrument.getInstrumentState()))
            {
            obsinstrument.notifyInstrumentStateChangedEvent(this,
                                                            obsinstrument,
                                                            obsinstrument.getInstrumentState(),
                                                            InstrumentState.READY,
                                                            0,
                                                            UNEXPECTED);
            }

        // Ensure that we can do a repeat
        // This action is duplicated if there was a SwingWorker
        setExecutionStatus(ExecutionStatus.FINISHED);

        LOGGER.debug(boolDebug,
                     SOURCE + "[instrument.state=" + obsinstrument.getInstrumentState().getName()
                            + "] [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(getResponseMessageStatusList())
                            + "] [execution.status=" + getExecutionStatus().getName()
                            + "] "
                            + DAOHelper.showPortState(getPort()));

        LOGGER.debug(boolDebug,
                     Logger.CONSOLE_SEPARATOR_MAJOR);
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

    public abstract CommandMessageInterface constructCommandMessage(ObservatoryInstrumentDAOInterface dao,
                                                                    Instrument instrumentxml,
                                                                    XmlObject module,
                                                                    CommandType command,
                                                                    String starscript);


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

    public abstract ResponseMessageInterface constructResponseMessage(String portname,
                                                                      Instrument instrumentxml,
                                                                      XmlObject module,
                                                                      CommandType command,
                                                                      String starscript,
                                                                      int responsestatusbits);


    /***********************************************************************************************
     * Indicate that the DAO has lost ownership of the Clipboard.
     *
     * @param clipboard
     * @param transferable
     */

    public void lostOwnership (final Clipboard clipboard,
                               final Transferable transferable)
        {
        LOGGER.log("AbstractObservatoryInstrumentDAO Lost ownership of the Clipboard [name=" + clipboard.getName() + "]");
        }


    /**********************************************************************************************/
    /* DAO Data                                                                                   */
    /***********************************************************************************************
     * Get the data produced by the DAO wrapped in a convenience object.
     * Return null if not applicable or not available.
     *
     * @return DAOWrapperInterface
     */

    public DAOWrapperInterface getWrappedData()
        {
        return (this.daoWrapperInterface);
        }


    /***********************************************************************************************
     * Set wrapped data on the DAO.
     *
     * @param wrapper
     */

    public void setWrappedData(final DAOWrapperInterface wrapper)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.setWrappedData() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        this.daoWrapperInterface = wrapper;

        LOGGER.debug(boolDebug,
                     SOURCE + "DAO-------------------------------------------------------------------------------------------");

        //LOGGER.log(SOURCE + "Ensure consistency between the DAO and the Wrapper");
        // ToDo REVIEW: I am not happy about the DAOWrapper concept!

        if (wrapper != null)
            {
            if (wrapper.getWrappedDAO() != null)
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "Set Wrapped Data on DAO [dao.class.host=" + getClass().getName()
                                 + "] [dao.class.wrapped=" + wrapper.getWrappedDAO().getClass().getName()
                                 + "] [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(wrapper.getWrappedDAO().getResponseMessageStatusList())
                                 + "] [execution.status=" + wrapper.getWrappedDAO().getExecutionStatus().getName()
                                 + "]");
                }
            else
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "Set Wrapped Data on DAO [dao.class.host=" + getClass().getName() + "]");
                }

            // Metadata containers
            this.listMetadataMetadata = wrapper.getMetadataMetadata();
            this.listCurrentObservatoryMetadata = wrapper.getCurrentObservatoryMetadata();
            this.listCurrentObserverMetadata = wrapper.getCurrentObserverMetadata();
            this.listObservationMetadata = wrapper.getObservationMetadata();
            this.listInstrumentMetadata = wrapper.getInstrumentMetadata();
            this.listControllerMetadata = wrapper.getControllerMetadata();
            this.listPluginMetadata = wrapper.getPluginMetadata();
            this.listRawDataMetadata = wrapper.getRawDataMetadata();
            this.listXYDatasetMetadata = wrapper.getXYDatasetMetadata();

            // POIs and LOIs
            this.listPOIs = wrapper.getPointOfInterestList();
            this.listLOIs = wrapper.getLineOfInterestList();

            // Data produced by this DAO
            this.vecRawData = wrapper.getRawData();
            this.xyDataset = wrapper.getXYDataset();
            this.imageData = wrapper.getImageData();
            this.userObject = wrapper.getUserObject();

            // The currently selected Chart for use with this DAO
            this.associatedChartUI = wrapper.getChartUI();

            // DAO Logging
            this.vecEventLogFragment = wrapper.getEventLogFragment();
            this.listEventLogMetadata = wrapper.getEventLogMetadata();
            this.vecInstrumentLogFragment = wrapper.getInstrumentLogFragment();
            this.listInstrumentLogMetadata = wrapper.getInstrumentLogMetadata();
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Set NULL Wrapped Data on DAO [dao.class.host=" + getClass().getName() + "]");

            // Metadata containers
            this.listMetadataMetadata = new ArrayList<Metadata>(1);
            this.listCurrentObservatoryMetadata = new ArrayList<Metadata>(1);
            this.listCurrentObserverMetadata = new ArrayList<Metadata>(1);
            this.listObservationMetadata = new ArrayList<Metadata>(1);
            this.listInstrumentMetadata = new ArrayList<Metadata>(1);
            this.listControllerMetadata = new ArrayList<Metadata>(1);
            this.listPluginMetadata = new ArrayList<Metadata>(1);
            this.listRawDataMetadata = new ArrayList<Metadata>(1);
            this.listXYDatasetMetadata = new ArrayList<Metadata>(1);

            // POIs and LOIs
            this.listPOIs = new ArrayList<PointOfInterest>(1);
            this.listLOIs = new ArrayList<LineOfInterest>(1);

            // Data produced by this DAO
            this.vecRawData = new Vector<Object>(1);
            this.xyDataset = null;
            this.imageData = null;
            this.userObject = null;

            // The currently selected Chart for use with this DAO
            this.associatedChartUI = null;

            // DAO Logging
            this.vecEventLogFragment = new Vector<Vector>(10);
            this.listEventLogMetadata = new ArrayList<Metadata>(10);
            this.vecInstrumentLogFragment = new Vector<Vector>(10);
            this.listInstrumentLogMetadata = new ArrayList<Metadata>(10);
            }
        }


    /**********************************************************************************************/
    /* Instrument Identity                                                                        */
    /***********************************************************************************************
     * Derive the identity of this DAO from the Imported data in the DataTranslator.
     * Set the RawDataChannelCount and the Temperature flag in the DAO.
     * Copy the Metadata from the DataTranslator to the appropriate containers in the DAO.
     * Optionally mark the imported data as unsaved.
     * All Imports must preserve FrameworkMetadata and clear all existing DAO Metadata.
     * If the Import contains FrameworkMetadata, it will be updated, but we'll take our chances.
     * Do not affect the Observatory and Observer Metadata master copies.
     * Return CommandCategory.IMPORT on success or CommandCategory.Enum.UNDEFINED if the Import failed.
     *
     * @param translator
     * @param unsaved
     *
     * @return CommandCategory.Enum
     */

    public CommandCategory.Enum deriveDAOIdentityFromImport(final DataTranslatorInterface translator,
                                                            final boolean unsaved)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.deriveDAOIdentityFromImport() ";
        final CommandCategory.Enum category;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        // MetadataMetadata, CurrentObservatoryMetadata, CurrentObserverMetadata,
        // ObservationMetadata, InstrumentMetadata, ControllerMetadata, PluginMetadata
        // This takes account of isInstrumentDataConsumer()
        clearData();

        // See what the Translator produced
        // We need these data for this DAO, even if the host Instrument does not consume them
        if (translator != null)
            {
            // Do the basics
            setRawDataChannelCount(translator.getRawDataChannelCount());
            setTemperatureChannel(translator.hasTemperatureChannel());

            DataTranslatorHelper.copyMetadataFromTranslator(translator,
                                                            REGISTRY.getFramework(),
                                                            this,
                                                            LOADER_PROPERTIES.isMetadataDebug());
            // Usually only one of these contains data, the other is NULL
            setRawData(translator.getRawData());
            setImageData(translator.getImage());

            // Without doing a lot of testing, we can assume the data have completely changed
            setUnsavedData(unsaved);

            if (getRawData() != null)
                {
                setDatasetTypeChanged(true);
                setChannelCountChanged(true);

                // Imports only produce RawData, but ProcessedData changes too
                setMetadataChanged(true);
                setRawDataChanged(true);
                setProcessedDataChanged(true);

                category = CommandCategory.IMPORT;

                // POIs and LOIs are manually re-imported
                }
            else
                {
                category = CommandCategory.UNDEFINED;
                }
            }
        else
            {
            category = CommandCategory.UNDEFINED;
            }

        LOGGER.debug(boolDebug,
                     SOURCE + "DAO Identity "
                     + "[category=" + category.toString()
                     + "] [channelcount=" + getRawDataChannelCount()
                     + "] [channel_count_changed=" + isChannelCountChanged()
                     + "] [temperature=" + hasTemperatureChannel()
                     + "] [datasettype_changed=" + isDatasetTypeChanged()
                     + "] [raw_data_changed=" + isRawDataChanged()
                     + "] [processed_data_changed=" + isProcessedDataChanged()
                     + "]");

        setWrappedData(new DAOWrapper(null, null, EMPTY_STRING, this));
        MetadataHelper.showDAOMetadata(this, SOURCE, LOADER_PROPERTIES.isMetadataDebug());

        return (category);
        }


    /***********************************************************************************************
     * Establish the identity of this DAO for Capture using Metadata from the DataCapture module.
     * Set the RawDataChannelCount from the count specified in the DataCapture Module Metadata.
     * Preserve FrameworkMetadata and use the master ObservatoryMetadata and ObserverMetadata,
     * reloading the default Observatory and Observer Metadata from the XML files if necessary.
     * If the DataCapture Module Metadata contains FrameworkMetadata, ObservatoryMetadata
     * or ObserverMetadata, they will be updated accordingly.
     * Clear all other Metadata, ready for the capture.
     * ToDo Review how this works if the Instrument does not consume the data.
     *
     * @param category
     * @param channelcount
     * @param temperaturechannel
     * @param capturemetadata
     * @param sundrymetadata
     */

    public void establishDAOIdentityForCapture(final CommandCategory.Enum category,
                                               final int channelcount,
                                               final boolean temperaturechannel,
                                               final List<Metadata> capturemetadata,
                                               final List<Metadata> sundrymetadata)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.establishDAOIdentityForCapture() ";
        final List<Metadata> listAggregateMetadata;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        // Remove all traces of any previous Capture or Import
        // This takes account of isInstrumentDataConsumer()
        clearData();

        // Do the basics
        setRawDataChannelCount(channelcount);
        setTemperatureChannel(temperaturechannel);

        // We have to do this here, to anticipate the capture obtaining different data
        setUnsavedData(false);
        setDatasetTypeChanged(true);
        setChannelCountChanged(true);

        // Capture only produces RawData, but ProcessedData changes too
        setMetadataChanged(true);
        setRawDataChanged(true);
        setProcessedDataChanged(true);

        // All Captures must preserve FrameworkMetadata and use references to the master ObservatoryMetadata and ObserverMetadata
        // If the Capture contains FrameworkMetadata, it will be updated, so we'll take our chances
        // If ObservatoryMetadata and ObserverMetadata do not exist, load the defaults
        if ((getHostInstrument() != null)
            && (getHostInstrument().getContext() != null)
            && (getHostInstrument().getContext().getObservatory() != null))
            {
            try
                {
                boolean boolLoaded;

                // Reload the default Observatory and Observer Metadata from the XML files
                boolLoaded = MetadataHelper.reloadObservatoryDefaultMetadata(getHostInstrument(),
                                                                             boolDebug);
                boolLoaded = MetadataHelper.reloadObserverDefaultMetadata(getHostInstrument(),
                                                                          boolDebug);

                // We don't mind if the Metadata were reloaded or not, just use the Observatory and Observer containers
                // Now add references to the loaded ObservatoryMetadata and ObserverMetadata to the DAO containers
                if (getHostInstrument().getContext().getObservatory().getObservatoryMetadata() != null)
                    {
                    addAllMetadataToContainersTraced(getHostInstrument().getContext().getObservatory().getObservatoryMetadata(),
                                                     SOURCE + "Adding reference to Observatory Metadata to DAO",
                                                     boolDebug);
                    }

                if (getHostInstrument().getContext().getObservatory().getObserverMetadata() != null)
                    {
                    addAllMetadataToContainersTraced(getHostInstrument().getContext().getObservatory().getObserverMetadata(),
                                                     SOURCE + "Adding reference to Observer Metadata to DAO",
                                                     boolDebug);
                    }
                }

            catch (XmlException exception)
                {
                LOGGER.error(SOURCE + exception.getMessage());
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + exception.getMessage());
                }
            }

        // Now merge the unique DataCapture Module Metadata...
        addAllMetadataToContainersTraced(capturemetadata,
                                         SOURCE + "Adding Capture Metadata",
                                         boolDebug);

        // ... and anything else which was supplied
        addAllMetadataToContainersTraced(sundrymetadata,
                                         SOURCE + "Adding Sundry Metadata",
                                         boolDebug);

        setWrappedData(new DAOWrapper(null, null, EMPTY_STRING, this));

        // Now would be a good time to update the Instrument's view of the Aggregate Metadata,
        // since there may have been lots of additions above
        listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                              getHostInstrument().getContext().getObservatory(),
                                                                              getHostInstrument(),
                                                                              this,
                                                                              getWrappedData(),
                                                                              SOURCE,
                                                                              boolDebug);
        // NOTE THAT The DAO data take precedence over those in the Wrapper
        // ToDo Review how this works if the Instrument does not consume the data
        getHostInstrument().setAggregateMetadata(listAggregateMetadata);

        MetadataHelper.showMetadataList(getHostInstrument().getAggregateMetadata(),
                                        SOURCE + "Aggregate Metadata",
                                        boolDebug);

        if ((category != null)
            && (!CommandCategory.CAPTURE.equals(category)))
            {
            LOGGER.warn(SOURCE + "Capture Command is not configured as such, so will not show a Capture icon on the Command button");
            }

        LOGGER.debug(boolDebug,
                     SOURCE + "Capture configuration "
                       + "[category=" + category.toString()
                       + "] [channelcount=" + getRawDataChannelCount()
                       + "] [temperature=" + hasTemperatureChannel() + "]");

        MetadataHelper.showDAOMetadata(this, SOURCE, LOADER_PROPERTIES.isMetadataDebug());
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Add all Metadata items in a List to the most appropriate Metadata container
     * in the DAO, if possible. Optionally trace the caller.
     *
     * @param metadatalist
     * @param tracemessage
     * @param traceon
     */

    public void addAllMetadataToContainersTraced(final List<Metadata> metadatalist,
                                                 final String tracemessage,
                                                 final boolean traceon)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.addAllMetadataToContainersTraced() ";

        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            final Iterator<Metadata> iterMetadata;

            iterMetadata = metadatalist.iterator();

            while (iterMetadata.hasNext())
                {
                final Metadata metadata;
                final List<Metadata> listMetadataContainer;

                metadata = iterMetadata.next();

                listMetadataContainer = MetadataHelper.findMetadataContainerByKeyTraced(this,
                                                                                        metadata.getKey(),
                                                                                        tracemessage,
                                                                                        traceon);
                if (listMetadataContainer != null)
                    {
                    MetadataHelper.addOrUpdateMetadataItemTraced(listMetadataContainer,
                                                                 metadata,
                                                                 tracemessage,
                                                                 traceon);
                    }
                else
                    {
                    LOGGER.error(tracemessage + " Metadata container was NULL [key=" + metadata.getKey() + "]");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the DAO Metadata Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadataMetadata()
        {
        return (this.listMetadataMetadata);
        }


    /***********************************************************************************************
     * Get the DAO CurrentObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getCurrentObservatoryMetadata()
        {
        return (this.listCurrentObservatoryMetadata);
        }


    /***********************************************************************************************
     * Get the DAO CurrentObserverMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getCurrentObserverMetadata()
        {
        return (this.listCurrentObserverMetadata);
        }


    /***********************************************************************************************
     * Get the DAO ObservationMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObservationMetadata()
        {
        return (this.listObservationMetadata);
        }


    /***********************************************************************************************
     * Get the DAO InstrumentMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentMetadata()
        {
        return (this.listInstrumentMetadata);
        }


    /***********************************************************************************************
     * Get the DAO ControllerMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getControllerMetadata()
        {
        return (this.listControllerMetadata);
        }


    /***********************************************************************************************
     * Get the DAO PluginMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getPluginMetadata()
        {
        return (this.listPluginMetadata);
        }


    /***********************************************************************************************
     * Get the DAO RawData Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getRawDataMetadata()
        {
        return (this.listRawDataMetadata);
        }


    /***********************************************************************************************
     * Get the DAO XYDataset Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getXYDatasetMetadata()
        {
        return (this.listXYDatasetMetadata);
        }


    /***********************************************************************************************
     * Clear all non-data DAO Metadata containers.
     */

    public void clearMetadata()
        {
        if (getMetadataMetadata() != null)
            {
            getMetadataMetadata().clear();
            setMetadataChanged(true);
            }

        if (getCurrentObservatoryMetadata() != null)
            {
            getCurrentObservatoryMetadata().clear();
            setMetadataChanged(true);
            }

        if (getCurrentObserverMetadata() != null)
            {
            getCurrentObserverMetadata().clear();
            setMetadataChanged(true);
            }

        if (getObservationMetadata() != null)
            {
            getObservationMetadata().clear();
            setMetadataChanged(true);
            }

        if (getInstrumentMetadata() != null)
            {
            getInstrumentMetadata().clear();
            setMetadataChanged(true);
            }

        if (getControllerMetadata() != null)
            {
            getControllerMetadata().clear();
            setMetadataChanged(true);
            }

        if (getPluginMetadata() != null)
            {
            getPluginMetadata().clear();
            setMetadataChanged(true);
            }
        }


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add a PointOfInterest to the DAO.
     *
     * @param poi
     */

    public final void addPointOfInterest(final PointOfInterest poi)
        {
        if ((poi != null)
            && (getPointOfInterestList() != null))
            {
            getPointOfInterestList().add(poi);
            }
        }


    /***********************************************************************************************
     * Remove all PointsOfInterest from the DAO.
     */

    public void clearPointsOfInterest()
        {
        if (getPointOfInterestList() != null)
            {
            getPointOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    public List<PointOfInterest> getPointOfInterestList()
        {
        return (this.listPOIs);
        }


    /***********************************************************************************************
     * Set the Points of Interest for the DAO.
     *
     * @param pois
     */

    public final void setPointOfInterestList(final List<PointOfInterest> pois)
        {
        this.listPOIs = pois;
        }


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add a LineOfInterest to the DAO.
     *
     * @param loi
     */

    public final void addLineOfInterest(final LineOfInterest loi)
        {
        if ((loi != null)
            && (getLineOfInterestList() != null))
            {
            getLineOfInterestList().add(loi);
            }
        }


    /***********************************************************************************************
     * Remove all LinesOfInterest from the DAO.
     */

    public void clearLinesOfInterest()
        {
        if (getLineOfInterestList() != null)
            {
            getLineOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    public List<LineOfInterest> getLineOfInterestList()
        {
        return (this.listLOIs);
        }


    /***********************************************************************************************
     * Set the Lines of Interest for the DAO.
     *
     * @param lois
     */

    public final void setLineOfInterestList(final List<LineOfInterest> lois)
        {
        this.listLOIs = lois;
        }


    /**********************************************************************************************/
    /* Data                                                                                       */
    /***********************************************************************************************
     * Get the DAO RawData.
     *
     * @return Vector<Object>
     */

    public Vector<Object> getRawData()
        {
        return (this.vecRawData);
        }


    /***********************************************************************************************
     * Set the DAO RawData.
     *
     * @param data
     */

    public void setRawData(final Vector<Object> data)
        {
        this.vecRawData = data;
        }


    /***********************************************************************************************
     * Clear the RawData and associated data.
     */

    public void clearRawData()
        {
        getRawDataMetadata().clear();
        setRawData(null);

        setRawDataChannelCount(0);
        setTemperatureChannel(false);
        setUnsavedData(false);
        }


    /***********************************************************************************************
     * Clear all DAO data containers.
     * Remove any identity from the Instrument, e.g. Metadata, RawData, XYDataset etc.
     * Remove any previous listeners for changes in the master ObservatoryMetadata and ObserverMetadata.
     * ToDo REVIEW: always removing both data and image may be a bad idea
     * Be careful to take account of the flag which indicates if the host Instrument consumes
     * the data from this DAO. If not, e.g. leave the **host's** data containers alone!
     */

    public void clearData()
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.clearData() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        //-----------------------------------------------------------------------------------------
        // The following occur for all kinds of DAO

        // Do the basics
        setRawDataChannelCount(0);
        setTemperatureChannel(false);

        setUnsavedData(false);
        setDatasetTypeChanged(false);
        setChannelCountChanged(false);

        setMetadataChanged(false);
        setRawDataChanged(false);
        setProcessedDataChanged(false);

        // MetadataMetadata, CurrentObservatoryMetadata, CurrentObserverMetadata,
        // ObservationMetadata, InstrumentMetadata, ControllerMetadata, PluginMetadata
        clearMetadata();

        getRawDataMetadata().clear();
        setRawData(null);

        getXYDatasetMetadata().clear();
        setXYDataset(null);

        setImageData(null);

        setChartUI(null);

        setUserObject(null);

        //-----------------------------------------------------------------------------------------
        // The following should occur ONLY if the host Instrument consumes data from this DAO!

        if (isInstrumentDataConsumer())
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Remove Instrument identity, and remove identity from all InstrumentPanel tabs");

            if (getHostInstrument() != null)
                {
                // Wipe out all data on all tabs
                getHostInstrument().removeInstrumentIdentity();
                }

            LOGGER.debug(boolDebug,
                         SOURCE + "Instrument identity removed [channelcount=0] [temperature=false] [unsaveddata=false]");

            MetadataHelper.showDAOMetadata(this, SOURCE, LOADER_PROPERTIES.isMetadataDebug());
            }

        // Finally, ensure that the Wrapped data agree with the DAO data
        setWrappedData(new DAOWrapper(null, null, EMPTY_STRING, this));
        }


    /***********************************************************************************************
     * Get the DAO XYDataset.
     *
     * @return XYDataset
     */

    public XYDataset getXYDataset()
        {
        return (this.xyDataset);
        }


    /***********************************************************************************************
     * Set the DAO XYDataset.
     *
     * @param dataset
     */

    public void setXYDataset(final XYDataset dataset)
        {
        this.xyDataset = dataset;
        }


    /***********************************************************************************************
     * Get the DAO Image.
     *
     * @return Image
     */

    public Image getImageData()
        {
        return (this.imageData);
        }


    /***********************************************************************************************
     * Set the DAO Image.
     *
     * @param image
     */

    public void setImageData(final Image image)
        {
        this.imageData = image;
        }


    /***********************************************************************************************
     * Get the UserObject.
     *
     * @return Object
     */

    public Object getUserObject()
        {
        return (this.userObject);
        }


    /***********************************************************************************************
     * Set the UserObject.
     *
     * @param userobject
     */

    public void setUserObject(final Object userobject)
        {
        this.userObject = userobject;
        }


    /***********************************************************************************************
     * Get the currently selected Chart for use with this DAO.
     *
     * @return ChartUIComponentPlugin
     */

    public ChartUIComponentPlugin getChartUI()
        {
        return (this.associatedChartUI);
        }


    /***********************************************************************************************
     * Set the currently selected Chart for use with this DAO.
     *
     * @param chartui
     */

    public void setChartUI(final ChartUIComponentPlugin chartui)
        {
        this.associatedChartUI = chartui;
        }


    /***********************************************************************************************
     * Get the RawData Channel count.
     *
     * @return int
     */

    public int getRawDataChannelCount()
        {
        return (this.intRawDataChannelCount);
        }


    /***********************************************************************************************
     * Set the RawData Channel count.
     *
     * @param count
     */

    public void setRawDataChannelCount(final int count)
        {
        this.intRawDataChannelCount = count;
        }


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature (Usually a Staribus dataset).
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (this.boolTemperatureChannel);
        }


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature
     * (Usually a Staribus dataset).
     *
     * @param flag
     */

    public void setTemperatureChannel(final boolean flag)
        {
        this.boolTemperatureChannel = flag;
        }


    /***********************************************************************************************
     * Indicate if this DAO has unsaved data.
     * Data may be RawData or Image.
     *
     * @return boolean
     */

    public boolean hasUnsavedData()
        {
        return (this.boolUnsavedData);
        }


    /***********************************************************************************************
     * Indicate if this DAO has unsaved data.
     * Data may be RawData or Image.
     *
     * @param unsaved
     */

    public void setUnsavedData(final boolean unsaved)
        {
        this.boolUnsavedData = unsaved;
        }


    /***********************************************************************************************
     * Indicate if the DatasetType of this DAO's data has changed.
     *
     * @return boolean
     */

    public boolean isDatasetTypeChanged()
        {
        return (this.boolDatasetTypeChanged);
        }


    /***********************************************************************************************
     * Indicate if the DatasetType of this DAO's data has changed.
     *
     * @param changed
     */

    public void setDatasetTypeChanged(final boolean changed)
        {
        this.boolDatasetTypeChanged = changed;
        }


    /***********************************************************************************************
     * Indicate if the ChannelCount of this DAO's data has changed.
     *
     * @return boolean
     */

    public boolean isChannelCountChanged()
        {
        return (this.boolChannelCountChanged);
        }


    /***********************************************************************************************
     * Indicate if the ChannelCount of this DAO's data has changed.
     *
     * @param changed
     */

    public void setChannelCountChanged(final boolean changed)
        {
        this.boolChannelCountChanged = changed;
        }


    /***********************************************************************************************
     * Indicate if this DAO's Metadata has changed.
     *
     * @return boolean
     */

    public boolean isMetadataChanged()
        {
        return (this.boolMetadataChanged);
        }


    /***********************************************************************************************
     * Indicate if this DAO's Metadata has changed.
     *
     * @param changed
     */

    public void setMetadataChanged(final boolean changed)
        {
        this.boolMetadataChanged = changed;
        }


    /***********************************************************************************************
     * Indicate if this DAO's RawData has changed.
     *
     * @return boolean
     */

    public boolean isRawDataChanged()
        {
        return (this.boolRawDataChanged);
        }


    /***********************************************************************************************
     * Indicate if this DAO's Data has changed.
     *
     * @param changed
     */

    public void setRawDataChanged(final boolean changed)
        {
        this.boolRawDataChanged = changed;
        }


    /***********************************************************************************************
     * Indicate if this DAO's ProcessedData has changed.
     *
     * @return boolean
     */

    public boolean isProcessedDataChanged()
        {
        return (this.boolProcessedDataChanged);
        }


    /***********************************************************************************************
     * Indicate if this DAO's ProcessedData has changed.
     *
     * @param changed
     */

    public void setProcessedDataChanged(final boolean changed)
        {
        this.boolProcessedDataChanged = changed;
        }


    /***********************************************************************************************
     * Indicate that the host Instrument requires the DAO data, some may not...
     *
     * @return boolean
     */

    public boolean isInstrumentDataConsumer()
        {
        return (true);
        }


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the DAO InstrumentLog Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentLogMetadata()
        {
        return (this.listInstrumentLogMetadata);
        }


    /***********************************************************************************************
     * Set the DAO InstrumentLog Metadata List.
     *
     * @param metadata
     */

    public void setInstrumentLogMetadata(final List<Metadata> metadata)
        {
        this.listInstrumentLogMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the DAO InstrumentLogFragment.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getInstrumentLogFragment()
        {
        return (this.vecInstrumentLogFragment);
        }


    /***********************************************************************************************
     * Get the DAO EventLog Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getEventLogMetadata()
        {
        return (this.listEventLogMetadata);
        }


    /***********************************************************************************************
     * Set the DAO EventLog Metadata List.
     *
     * @param metadata
     */

    public void setEventLogMetadata(final List<Metadata> metadata)
        {
        this.listEventLogMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the DAO EventLogFragment.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getEventLogFragment()
        {
        return (this.vecEventLogFragment);
        }


    /***********************************************************************************************
     * Re-initialise the EventLogFragment and its Metadata.
     */

    public synchronized void clearEventLogFragment()
        {
        if (getEventLogFragment() != null)
            {
            getEventLogFragment().clear();
            }
        else
            {
            this.vecEventLogFragment = new Vector<Vector>(100);
            }

        if (getEventLogMetadata() != null)
            {
            getEventLogMetadata().clear();
            }
        else
            {
            setEventLogMetadata(new ArrayList<Metadata>(10));
            }
        }


    /***********************************************************************************************
     * Get the Vector of extra data to append to a Report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getDAOConfiguration()
        {
        return (this.vecDAOConfiguration);
        }


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Get the SwingWorker which executes the Command which produces the data.
     *
     * @return SwingWorker
     */

    public SwingWorker getExecuteWorker()
        {
        return (this.workerExecute);
        }


    /***********************************************************************************************
     * Set the SwingWorker which executes the Command which produces the data.
     *
     * @param worker
     */

    public void setExecuteWorker(final SwingWorker worker)
        {
        this.workerExecute = worker;
        }


    /***********************************************************************************************
     * Get the timeout Timer.
     *
     * @return Timer
     */

    public Timer getTimeoutTimer()
        {
        return (this.timerTimeout);
        }


    /***********************************************************************************************
     * Set the timeout Timer.
     *
     * @param timer
     */

    public void setTimeoutTimer(final Timer timer)
        {
        this.timerTimeout = timer;
        }


    /***********************************************************************************************
     * Get the repeat Timer.
     *
     * @return Timer
     */

    public Timer getRepeatTimer()
        {
        return (this.timerRepeat);
        }


    /***********************************************************************************************
     * Set the repeat Timer.
     *
     * @param timer
     */

    public void setRepeatTimer(final Timer timer)
        {
        this.timerRepeat = timer;
        }


    /***********************************************************************************************
     * Return a flag to indicate if the DAO is Busy.
     *
     * @return boolean
     */

    public boolean isDaoBusy()
        {
        boolean boolBusy;

        boolBusy = false;

        if (getPort() != null)
            {
            boolBusy = getPort().isPortBusy();
            }

        return (boolBusy);
        }


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

   public ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.instrument);
        }


    /***********************************************************************************************
     * Get the DataTranslator.
     *
     * @return DataTranslatorInterface
     */

    public DataTranslatorInterface getTranslator()
        {
        return (this.translatorInterface);
        }


    /***********************************************************************************************
     * Set the DataTranslator.
     *
     * @param translator
     */

    public void setTranslator(final DataTranslatorInterface translator)
        {
        this.translatorInterface = translator;
        }


    /***********************************************************************************************
     * Get the DataFilter.
     *
     * @return DataFilterInterface
     */

    public DataFilterInterface getFilter()
        {
        return (this.filterInterface);
        }


    /***********************************************************************************************
     * Set the DataFilter.
     *
     * @param filter
     */

    public void setFilter(final DataFilterInterface filter)
        {
        this.filterInterface = filter;
        }


    /***********************************************************************************************
     * Get the Port associated with the DAO.
     *
     * @return DaoPortInterface
     */

    public DaoPortInterface getPort()
        {
        return (this.port);
        }


    /***********************************************************************************************
     * Set the Port associated with the DAO.
     *
     * @param daoport
     */

    public void setPort(final DaoPortInterface daoport)
        {
        this.port = daoport;
        }


    /***********************************************************************************************
     * Get the List of method names accessible to this DAO and its subclasses.
     *
     * @return List<String>
     */

    public CommandPoolList getCommandPool()
        {
        return (this.listCommandPool);
        }


    /***********************************************************************************************
     * Get the default timeout period in milliSeconds.
     *
     * @return int
     */

    public int getTimeoutDefaultMillis()
        {
        return (this.intTimeoutDefaultMillis);
        }


    /***********************************************************************************************
     * Set the default timeout period in milliSeconds.
     *
     * @param timeout
     */

    public void setTimeoutDefaultMillis(final int timeout)
        {
        this.intTimeoutDefaultMillis = timeout;
        }


    /***********************************************************************************************
     * Get the real timeout period in milliSeconds.
     * Use the timeout in the optional Command Metadata, or the DAO default.
     *
     * @param module
     * @param command
     *
     * @return int
     */

    public int getTimeoutMillis(final XmlObject module,
                                final CommandType command)
        {
        // Get the custom Command timeout from the CommandMetadata if possible,
        // or use the specified default
        return (TimeoutHelper.getCommandTimeout(module, command, getTimeoutDefaultMillis()));
        }


    /**********************************************************************************************/
    /* Provide access to useful Instruments                                                       */
    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.getObservatoryClock() ";
        final ObservatoryClockInterface clock;

        if ((getHostInstrument() != null)
            && (getHostInstrument().getHostUI() != null)
            && (getHostInstrument().getHostUI().getObservatoryClock() != null))
            {
            //LOGGER.log(SOURCE + "Getting ObservatoryClock *Instrument*");
            clock = getHostInstrument().getHostUI().getObservatoryClock();
            }
        else
            {
            // We should have a real or simulated Clock by this stage...
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        return (clock);
        }


    /***********************************************************************************************
     * Get the List of ResourceBundles associated with this DAO.
     *
     * @return List<ResourceBundle>
     */

    public List<ResourceBundle> getResourceBundles()
        {
        return (this.listBundles);
        }


    /******************************************************************************************/
    /* Events                                                                                 */
    /*******************************************************************************************
     * Indicate that the ObservatoryMetadata has changed.
     * Only respond to Events which originated elsewhere, i.e. not in this DAO.
     *
     * @param event
     */

    public void observatoryChanged(final ObservatoryMetadataChangedEvent event)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.observatoryChanged() ";

        if ((event != null)
            && (!event.getSource().equals(this))
            && (event.getMetadataKey() != null)
            && (event.getMetadataKey().startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey())))
            {
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[state=" + event.getItemState().getName()
                                + "] [key=" + event.getMetadataKey()
                                + "] [instrument=" + getHostInstrument().getInstrument().getIdentifier()
                                + "] [source=" + event.getSource().getClass().getName()
                                + "]");
            }
        }


    /*******************************************************************************************
     * Indicate that the ObserverMetadata has changed.
     * Only respond to Events which originated elsewhere, i.e. not in this DAO.
     *
     * @param event
     */

    public void observerChanged(final ObserverMetadataChangedEvent event)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.observerChanged() ";

        if ((event != null)
            && (!event.getSource().equals(this))
            && (event.getMetadataKey() != null)
            && (event.getMetadataKey().startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey())))
            {
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[state=" + event.getItemState().getName()
                         + "] [key=" + event.getMetadataKey()
                         + "] [instrument=" + getHostInstrument().getInstrument().getIdentifier()
                                + "] [source=" + event.getSource().getClass().getName()
                                + "]");
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the List of ResponseMessageStatus of the DAO Command processor.
     *
     * @return ResponseMessageStatusList
     */

    public synchronized ResponseMessageStatusList getResponseMessageStatusList()
        {
        return (this.listResponseMessageStatus);
        }


    /***********************************************************************************************
     * Get the ExecutionStatus.
     *
     * @return ExecutionStatus
     */

    public ExecutionStatus getExecutionStatus()
        {
        return (this.executionStatus);
        }


    /***********************************************************************************************
     * Set the ExecutionStatus.
     *
     * @param executionstatus
     */

    public void setExecutionStatus(final ExecutionStatus executionstatus)
        {
        this.executionStatus = executionstatus;
        }


    /***********************************************************************************************
     * Indicate if execution should continue after an error is detected, e.g. on repeats.
     *
     * @return boolean
     */

    public boolean continueOnError()
        {
        return (this.boolOnErrorContinue);
        }


    /***********************************************************************************************
     * Get a simulated address of the 'local host'.
     *
     * @return String
     */

    public String getLocalHostname()
        {
        return (getInstrumentName().toLowerCase() + DOT + "local");
        }


    /***********************************************************************************************
     * Get the host Instrument name, mainly for logging.
     *
     * @return String
     */

    public String getInstrumentName()
        {
        if ((getHostInstrument() != null)
            && (getHostInstrument().getInstrument() != null))
            {
            return (this.getHostInstrument().getInstrument().getName());
            }
        else
            {
            return (EMPTY_STRING);
            }
        }


    /***********************************************************************************************
     * Get the RemoteDataConnection.
     *
     * @return RemoteDataConnectionInterface
     */

    public RemoteDataConnectionInterface getRemoteDataConnection()
        {
        return (this.dataConnection);
        }


    /***********************************************************************************************
     * Set the RemoteDataConnection.
     *
     * @param connection
     */

    public void setRemoteDataConnection(final RemoteDataConnectionInterface connection)
        {
        this.dataConnection = connection;
        }


    /***********************************************************************************************
     * Indicate if the DAO is in debug mode. This will control Logger messages.
     *
     * @return boolean
     */

    public boolean isDebugMode()
        {
        return (this.boolDebug);
        }


    /***********************************************************************************************
     * Indicate if the DAO is in debug mode. This will control Logger messages.
     *
     * @param debug
     */

    public void setDebugMode(final boolean debug)
        {
        this.boolDebug = debug;
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the DAO.
     *
     * KEY_DAO_ONERROR_CONTINUE
     * KEY_DAO_TIMEOUT_DEFAULT
     */

    public void readResources()
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.readResources() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[ResourceKey=" + getResourceKey() + "]");

        // OnError.Continue
        boolOnErrorContinue = REGISTRY.getBooleanProperty(getResourceKey() + KEY_DAO_ONERROR_CONTINUE);

        // Default Timeout Period
        intTimeoutDefaultMillis = REGISTRY.getIntegerProperty(getResourceKey() + KEY_DAO_TIMEOUT_DEFAULT)
                                                               * (int) ChronosHelper.SECOND_MILLISECONDS;
        // Trap the careless use of the Timeout Period
        if ((intTimeoutDefaultMillis < ChronosHelper.SECOND_MILLISECONDS)
            || (intTimeoutDefaultMillis > TimeoutHelper.TIMEOUT_MAX_MILLISECONDS))
            {
            intTimeoutDefaultMillis = 10 * (int) ChronosHelper.SECOND_MILLISECONDS;
            LOGGER.error(SOURCE + KEY_DAO_TIMEOUT_DEFAULT + " is set incorrectly. Using a value of 10sec.");
            }

        // Update Period has been removed since it is no longer used
        }
    }
