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

import org.apache.xmlbeans.XmlObject;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.instruments.CommandCategory;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;


/***************************************************************************************************
 * ObservatoryInstrumentDAOInterface.
 */

public interface ObservatoryInstrumentDAOInterface extends ObservatoryMetadataChangedListener,
                                                           ObserverMetadataChangedListener,
                                                           FrameworkConstants,
                                                           FrameworkStrings,
                                                           FrameworkMetadata,
                                                           FrameworkSingletons,
                                                           FrameworkRegex,
                                                           FrameworkXpath,
                                                           ResourceKeys,
                                                           ObservatoryConstants,
                                                           ClipboardOwner
    {
    // String Resources
    String PATHNAME_PLUGINS_OBSERVATORY = "plugins/observatory/";
    String COMMON_HELP_BUNDLE = "CommonHelpBundle";
    String DAO_HELP_BUNDLE_SUFFIX = "HelpBundle";

    // Required Commands
    String COMMAND_RESET = "reset";
    String COMMAND_PING = "ping";
    String COMMAND_GET_CONFIGURATION = "getConfiguration";

    String MSG_NO_VERSIONS = "No Version information found";
    String MSG_GENERIC_EXCEPTION = "catching a plain Exception!";

    // String Resources
    String ERROR_NOT_INSTANTIATED = "Unable to instantiate the ObservatoryInstrumentDAO ";
    String ERROR_PARSE_INPUT = "Unable to parse input parameters ";
    String ERROR_READ_IMAGE = "Unable to read the Image file ";
    String ERROR_PARSE_DATA = "Unable to parse data ";
    String ERROR_DATA_FORMAT = "No data, or incorrect format ";
    String ERROR_PARSE_BLOCK_COUNT = "Unable to parse Block Count ";

    String ERROR_GENERIC = "Generic error";
    String ERROR_HTTP = "HTTP error";
    String ERROR_IO = "Input/Output error";
    String ERROR_UNKNOWN_HOST = "Unknown Host error";
    String ERROR_SOCKET = "Socket error";
    String ERROR_SECURITY = "Security error";
    String ERROR_ILLEGAL_MODE = "Illegal Mode error";
    String ERROR_ILLEGAL_ARGUMENT = "Illegal Argument error";
    String ERROR_TIMEOUT = "Timeout error";
    String ERROR_PORT = "Port error";
    String ERROR_ARRAY_INDEX = "Array Index Out of Bounds error";

    String ERROR_LOG_SAVE = "Unable to save log file";
    String ERROR_LOG_NOT_FOUND = "Log file not found";
    String ERROR_LOG_ACCESS_DENIED = "Access denied to log file";

    int INITIAL_CAPACITY = 50;
    long MAX_REPEAT_COUNT = 1000000L;

    // ExportableComponent indexes and controls
    int EXPORTABLE_INDEX_MAX = 5;

    int EXPORTABLE_INDEX_SATELLITE_MAP = 0;
    int EXPORTABLE_INDEX_STAR_MAP = 0;
    int EXPORTABLE_INDEX_REGIONAL_MAP = 1;

    boolean EXPORT_USE_COMPONENT_SIZE = true;
    boolean EXPORT_IGNORE_COMPONENT_SIZE = false;

    // Icons
    ImageIcon IMAGE_ICON_PLAIN = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());
    ImageIcon IMAGE_ICON_WARNING = RegistryModelUtilities.getCommonIcon(EventStatus.WARNING.getIconFilename());


    /**********************************************************************************************/
    /* DAO State                                                                                  */
    /***********************************************************************************************
     * Initialise the DAO.
     * Be careful to take account of the flag which indicates if the host Instrument consumes
     * the data from this DAO. If not, e.g. leave the host's data containers alone!
     *
     * @param resourcekey
     *
     * @return boolean
     */

    boolean initialiseDAO(String resourcekey);


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    void disposeDAO();


    /**********************************************************************************************/
    /* DAO Local Mandatory Commands                                                               */
    /***********************************************************************************************
     * ping().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface ping(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * reset().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface reset(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * getConfiguration().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getConfiguration(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * start().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface start(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * stop().
     * See the code in ObservatoryInstrumentHelper.createButtonPanel().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface stop(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * getVersion().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getVersion(CommandMessageInterface commandmessage);


    /**********************************************************************************************/
    /* Importer Commands                                                                          */
    /***********************************************************************************************
     * importRawDataLocal().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface importRawDataLocal(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * importRawDataRemote().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface importRawDataRemote(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * importRawDataRemoteIncrement().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface importRawDataRemoteIncrement(CommandMessageInterface commandmessage);


    /**********************************************************************************************/
    /* Exporter Commands                                                                          */
    /***********************************************************************************************
     * exportChart().
     * Saves the current chart as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportChart(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportXYDataset().
     * Saves the current processed data (i.e. the XYDataset used for the Chart)
     * at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportProcessedData(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportRawData().
     * Saves the current raw data at the specified location, in the specified DataFormat.
     * Optionally timestamp the filename.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportRawData(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportInstrumentLog().
     * Saves the current InstrumentLog at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportInstrumentLog(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportLog().
     * Saves the current EventLog at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportEventLog(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportMetadata().
     * Saves the current Metadata at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportMetadata(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportConfiguration().
     * Saves the current Instrument configuration at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportConfiguration(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * exportInstrumentXML().
     * Saves the current Instrument XML at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface exportInstrumentXML(CommandMessageInterface commandmessage);


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

    boolean repeatCommand(CommandProcessorContextInterface context,
                          List<String> errors);


    /***********************************************************************************************
     * Abort the execution of any Command.
     *
     * @param instrument
     * @param instrumentxml
     * @param module
     * @param command
     * @param errors
     */

    void abortCommand(ObservatoryInstrumentInterface instrument,
                      Instrument instrumentxml,
                      XmlObject module,
                      CommandType command,
                      List<String> errors);


    /***********************************************************************************************
     * exportReportTable().
     * Saves the data from a general ReportTable at the specified location.
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

    ResponseMessageInterface exportReportTable(String source,
                                               CommandMessageInterface commandmessage,
                                               List<Metadata> metadatametadata,
                                               List<Metadata> metadata,
                                               boolean infercolumnmetadata,
                                               DatasetType datasettype,
                                               ReportTablePlugin report);


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

    ResponseMessageInterface exportComponent(CommandMessageInterface commandmessage,
                                             int componentindex,
                                             boolean usesize);


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

    CommandMessageInterface constructCommandMessage(ObservatoryInstrumentDAOInterface dao,
                                                    Instrument instrumentxml,
                                                    XmlObject module,
                                                    CommandType command,
                                                    String starscript);


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface constructResponseMessage(String portname,
                                                      Instrument instrumentxml,
                                                      XmlObject module,
                                                      CommandType command,
                                                      String starscript,
                                                      int responsestatusbits);


    /**********************************************************************************************/
    /* DAO Data                                                                                   */
    /***********************************************************************************************
     * Get the data produced by the DAO wrapped in a convenience object.
     * Return null if not applicable or not available.
     *
     * @return DAOWrapperInterface
     */

    DAOWrapperInterface getWrappedData();


    /***********************************************************************************************
     * Set wrapped data on the DAO.
     *
     * @param data
     */

    void setWrappedData(DAOWrapperInterface data);


    /***********************************************************************************************
     * Derive the identity of this DAO from the Imported data in the DataTranslator.
     * Set the RawDataChannelCount and the Temperature flag in the DAO.
     * Copy the Metadata from the DataTranslator to the appropriate containers in the DAO.
     * Optionally mark the imported data as unsaved.
     *
     * @param translator
     * @param unsaved
     */

    CommandCategory.Enum deriveDAOIdentityFromImport(DataTranslatorInterface translator,
                                                     boolean unsaved);


    /***********************************************************************************************
     * Establish the identity of this DAO for Capture using Metadata from the DataCapture module.
     * Set the RawDataChannelCount from the count specified in the DataCapture Module Metadata.
     * Preserve FrameworkMetadata and use the default ObservatoryMetadata and ObserverMetadata.
     * If the DataCapture Module Metadata contains FrameworkMetadata, ObservatoryMetadata
     * or ObserverMetadata, they will be updated accordingly.
     * Clear all other Metadata, ready for the capture.
     *
     * @param category
     * @param channelcount
     * @param temperaturechannel
     * @param capturemetadata
     * @param sundrymetadata
     */

    void establishDAOIdentityForCapture(CommandCategory.Enum category,
                                        int channelcount,
                                        boolean temperaturechannel,
                                        List<Metadata> capturemetadata,
                                        List<Metadata> sundrymetadata);


    /***********************************************************************************************
     * Add all Metadata items in a List to the most appropriate Metadata container
     * in the DAO, if possible.
     *
     * @param metadatalist
     * @param tracemessage
     * @param traceon
     */

    void addAllMetadataToContainersTraced(List<Metadata> metadatalist,
                                          String tracemessage,
                                          boolean traceon);


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the DAO Metadata Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadataMetadata();


    /***********************************************************************************************
     * Get the DAO CurrentObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getCurrentObservatoryMetadata();


    /***********************************************************************************************
     * Get the DAO CurrentObserverMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getCurrentObserverMetadata();


    /***********************************************************************************************
     * Get the DAO ObservationMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObservationMetadata();


    /***********************************************************************************************
     * Get the DAO InstrumentMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getInstrumentMetadata();


    /***********************************************************************************************
     * Get the DAO ControllerMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getControllerMetadata();


    /***********************************************************************************************
     * Get the DAO PluginMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getPluginMetadata();


    /***********************************************************************************************
     * Get the DAO RawData Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getRawDataMetadata();


    /***********************************************************************************************
     * Get the DAO XYDataset Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getXYDatasetMetadata();


    /***********************************************************************************************
     * Clear all DAO Metadata containers.
     */

    void clearMetadata();


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add a PointOfInterest to the Observatory.
     *
     * @param poi
     */

    void addPointOfInterest(PointOfInterest poi);


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Observatory.
     */

    void clearPointsOfInterest();


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    List<PointOfInterest> getPointOfInterestList();


    /***********************************************************************************************
     * Set the Points of Interest for the Observatory.
     *
     * @param pois
     */

    void setPointOfInterestList(List<PointOfInterest> pois);


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add a LineOfInterest to the Observatory.
     *
     * @param loi
     */

    void addLineOfInterest(LineOfInterest loi);


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Observatory.
     */

    void clearLinesOfInterest();


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    List<LineOfInterest> getLineOfInterestList();


    /***********************************************************************************************
     * Set the Lines of Interest for the Observatory.
     *
     * @param lois
     */

    void setLineOfInterestList(List<LineOfInterest> lois);


    /**********************************************************************************************/
    /* Data                                                                                       */
    /***********************************************************************************************
     * Get the DAO RawData.
     *
     * @return Vector<Object>
     */

    Vector<Object> getRawData();


    /***********************************************************************************************
     * Set the DAO RawData.
     *
     * @param data
     */

    void setRawData(Vector<Object> data);


    /***********************************************************************************************
     * Clear the RawData and associated data.
     */

    void clearRawData();


    /***********************************************************************************************
     * Clear all DAO data containers.
     * Remove any identity from the Instrument, e.g. Metadata, RawData, XYDataset etc.
     * Remove any previous listeners for changes in the master ObservatoryMetadata and ObserverMetadata.
     * ToDo REVIEW: always removing both data and image may be a bad idea
     * Be careful to take account of the flag which indicates if the host Instrument consumes
     * the data from this DAO. If not, e.g. leave the **host's** data containers alone!
     */

    void clearData();


    /***********************************************************************************************
     * Get the DAO XYDataset.
     *
     * @return XYDataset
     */

    XYDataset getXYDataset();


    /***********************************************************************************************
     * Set the DAO XYDataset.
     *
     * @param dataset
     */

    void setXYDataset(XYDataset dataset);


    /***********************************************************************************************
     * Get the DAO Image.
     *
     * @return Image
     */

    Image getImageData();


    /***********************************************************************************************
     * Set the DAO Image.
     *
     * @param image
     */

    void setImageData(Image image);


    /***********************************************************************************************
     * Get the UserObject.
     *
     * @return Object
     */

    Object getUserObject();


    /***********************************************************************************************
     * Set the UserObject.
     *
     * @param userobject
     */

    void setUserObject(Object userobject);


    /***********************************************************************************************
     * Get the currently selected Chart for use with this DAO.
     *
     * @return ChartUIComponentPlugin
     */

    ChartUIComponentPlugin getChartUI();


    /***********************************************************************************************
     * Set the currently selected Chart for use with this DAO.
     *
     * @param chartui
     */

    void setChartUI(ChartUIComponentPlugin chartui);


    /***********************************************************************************************
     * Get the RawData Channel count.
     *
     * @return int
     */

    int getRawDataChannelCount();


    /***********************************************************************************************
     * Set the RawData Channel count.
     *
     * @param count
     */

    void setRawDataChannelCount(int count);


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature (Usually a Staribus dataset).
     *
     * @return boolean
     */

    boolean hasTemperatureChannel();


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature
     * (Usually a Staribus dataset).
     *
     * @param temperature
     */

    void setTemperatureChannel(boolean temperature);


    /***********************************************************************************************
     * Indicate if this DAO has unsaved data.
     * Data may be RawData or Image.
     *
     * @return boolean
     */

    boolean hasUnsavedData();


    /***********************************************************************************************
     * Indicate if this DAO has unsaved data.
     * Data may be RawData or Image.
     *
     * @param unsaved
     */

    void setUnsavedData(boolean unsaved);


    /***********************************************************************************************
     * Indicate if the DatasetType of this DAO's data has changed.
     *
     * @return boolean
     */

    boolean isDatasetTypeChanged();


    /***********************************************************************************************
     * Indicate if the DatasetType of this DAO's data has changed.
     *
     * @param changed
     */

    void setDatasetTypeChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if the Structure of this DAO's data has changed.
     * e.g. Channel Count, or a change from Columnar to Timestamped data.
     *
     * @return boolean
     */

    boolean isChannelCountChanged();


    /***********************************************************************************************
     * Indicate if the Structure of this DAO's data has changed.
     * e.g. Channel Count, or a change from Columnar to Timestamped data.
     *
     * @param changed
     */

    void setChannelCountChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if this DAO's Metadata has changed.
     *
     * @return boolean
     */

    boolean isMetadataChanged();


    /***********************************************************************************************
     * Indicate if this DAO's Metadata has changed.
     *
     * @param changed
     */

    void setMetadataChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if this DAO's RawData has changed.
     *
     * @return boolean
     */

    boolean isRawDataChanged();


    /***********************************************************************************************
     * Indicate if this DAO's RawData has changed.
     *
     * @param changed
     */

    void setRawDataChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if this DAO's ProcessedData has changed.
     *
     * @return boolean
     */

    boolean isProcessedDataChanged();


    /***********************************************************************************************
     * Indicate if this DAO's ProcessedData has changed.
     *
     * @param changed
     */

    void setProcessedDataChanged(boolean changed);


    /***********************************************************************************************
     * Indicate that the host Instrument requires the DAO data, some may not...
     *
     * @return boolean
     */

    boolean isInstrumentDataConsumer();


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the DAO InstrumentLogFragment.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getInstrumentLogFragment();


    /***********************************************************************************************
     * Get the InstrumentLog Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getInstrumentLogMetadata();


    /***********************************************************************************************
     * Set the DAO InstrumentLog Metadata List.
     *
     * @param metadata
     */

    void setInstrumentLogMetadata(List<Metadata> metadata);


    /***********************************************************************************************
     * Get the DAO EventLogFragment.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getEventLogFragment();


    /***********************************************************************************************
     * Get the EventLog Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getEventLogMetadata();


    /***********************************************************************************************
     * Set the DAO EventLog Metadata List.
     *
     * @param metadata
     */

    void setEventLogMetadata(List<Metadata> metadata);


    /***********************************************************************************************
     * Re-initialise the EventLog and its Metadata.
     */

    void clearEventLogFragment();


    /***********************************************************************************************
     * Get the Vector of extra configuration data to append to a Report.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getDAOConfiguration();


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Get the SwingWorker which executes the Command.
     *
     * @return SwingWorker
     */

    SwingWorker getExecuteWorker();


    /***********************************************************************************************
     * Set the SwingWorker which executes the Command.
     *
     * @param worker
     */

    void setExecuteWorker(SwingWorker worker);


    /***********************************************************************************************
     * Get the timeout Timer.
     *
     * @return Timer
     */

    Timer getTimeoutTimer();


    /***********************************************************************************************
     * Set the timeout Timer.
     *
     * @param timer
     */

    void setTimeoutTimer(Timer timer);


    /***********************************************************************************************
     * Get the repeat Timer.
     *
     * @return Timer
     */

    Timer getRepeatTimer();


    /***********************************************************************************************
     * Set the repeat Timer.
     *
     * @param timer
     */

    void setRepeatTimer(Timer timer);


    /***********************************************************************************************
     * Return a flag to indicate if the DAO is Busy.
     *
     * @return boolean
     */

    boolean isDaoBusy();


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getHostInstrument();


    /***********************************************************************************************
     * Get the DataTranslator.
     *
     * @return DataTranslatorInterface
     */

    DataTranslatorInterface getTranslator();


    /***********************************************************************************************
     * Set the DataTranslator.
     *
     * @param translator
     */

    void setTranslator(DataTranslatorInterface translator);


    /***********************************************************************************************
     * Get the DataFilter.
     *
     * @return DataFilterInterface
     */

    DataFilterInterface getFilter();


    /***********************************************************************************************
     * Set the DataFilter.
     *
     * @param filter
     */

    void setFilter(DataFilterInterface filter);


    /***********************************************************************************************
     * Get the Port associated with the DAO.
     *
     * @return DaoPortInterface
     */

    DaoPortInterface getPort();


    /***********************************************************************************************
     * Set the Port associated with the DAO.
     *
     * @param daoport
     */

    void setPort(DaoPortInterface daoport);


    /***********************************************************************************************
     * Get the List of method names accessible to this DAO and its subclasses.
     *
     * @return List<String>
     */

    CommandPoolList getCommandPool();


    /***********************************************************************************************
     * Get the default timeout period in milliSeconds.
     *
     * @return int
     */

    int getTimeoutDefaultMillis();


    /***********************************************************************************************
     * Set the default timeout period in milliSeconds.
     *
     * @param timeout
     */

    void setTimeoutDefaultMillis(int timeout);


    /***********************************************************************************************
     * Get the real timeout period in milliSeconds.
     * Use the timeout in the optional Command Metadata, or the DAO default.
     *
     * @param module
     * @param command
     *
     * @return int
     */

    int getTimeoutMillis(XmlObject module,
                         CommandType command);


    /**********************************************************************************************/
    /* Provide access to useful Instruments                                                       */
    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();


    /***********************************************************************************************
     * Get the List of ResourceBundles associated with this DAO.
     *
     * @return List<ResourceBundle>
     */

    List<ResourceBundle> getResourceBundles();


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Get the List of ResponseMessageStatus of the DAO Command processor.
     *
     * @return ResponseMessageStatusList
     */

    ResponseMessageStatusList getResponseMessageStatusList();


    /***********************************************************************************************
     * Get the ExecutionStatus.
     *
     * @return ExecutionStatus
     */

    ExecutionStatus getExecutionStatus();


    /***********************************************************************************************
     * Set the ExecutionStatus.
     *
     * @param executionstatus
     */

    void setExecutionStatus(ExecutionStatus executionstatus);


    /***********************************************************************************************
     * Indicate if execution should continue after an error is detected, e.g. on repeats.
     *
     * @return boolean
     */

    boolean continueOnError();


    /***********************************************************************************************
     * Get a simulated address of the 'local host'.
     *
     * @return String
     */

    String getLocalHostname();


    /***********************************************************************************************
     * Get the host Instrument name, mainly for logging.
     *
     * @return String
     */

    String getInstrumentName();


    /***********************************************************************************************
     * Get the RemoteDataConnection.
     *
     * @return RemoteDataConnectionInterface
     */

    RemoteDataConnectionInterface getRemoteDataConnection();


    /***********************************************************************************************
     * Set the RemoteDataConnection.
     *
     * @param connection
     */

    void setRemoteDataConnection(RemoteDataConnectionInterface connection);


    /***********************************************************************************************
     * Indicate if the DAO is in debug mode. This will control Logger messages.
     *
     * @return boolean
     */

    boolean isDebugMode();


    /***********************************************************************************************
     * Indicate if the DAO is in debug mode. This will control Logger messages.
     *
     * @param debug
     */

    void setDebugMode(boolean debug);


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     */

    void readResources();
    }
