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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.publishers;


import org.apache.xmlbeans.XmlObject;
import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.*;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.RemoteDataConnectionInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusParsers;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.dataconnections.RemoteDataConnectionFTP;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatusList;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.parsers.YearMonthDayParser;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * PublishRealtime.
 *
 * Parameters:
 *
 *  Capture.Trigger         // doPublishChartRealtimeDay() ONLY
 *  Capture.Interval
 *  Capture.Period
 *  Capture.Filter
 *  Upload.Counter
 *
 *  Image.Format
 *  Image.Width
 *  Image.Height
 *  Image.LocalDirectory
 *  Image.LocalFilename
 *  Image.Timestamp
 *
 *  Server.Hostname
 *  Server.Username
 *  Server.Password
 *  Server.RemoteDirectory
 *  Server.RemoteFilename
 *
 *  VerboseLogging
 *
 *  Returns:
 *
 *  Publisher.Timestamp
 */

public final class PublishHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            FrameworkXpath,
                                            ObservatoryConstants
    {
    // String Resources
    private static final String MSG_PERIOD_CONTINUOUS = "continuous";


    /***********************************************************************************************
     * doPublishChartRealtime.
     *
     * There are multiple layers to this implementation:
     *
     *  doPublishChartRealtime()                           --> Parameter parsing from UI, set upload credentials
     *  doIteratedStaribusMultichannelDataCaptureCommand() --> Set up Capture configuration
     *  executeCaptureAndPublish()                         --> Main Capture loop, and interval timing
     *  executeSingleCapture()                             --> Execute iterated Command in retry loop
     *  exportAndUploadThread()                            --> Upload the image file on a separate Thread
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doPublishRealtime(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage,
                                                             final List<Metadata> metadatalist,
                                                             final String source,
                                                             final boolean notifyport)
        {
        final int PARAMETER_COUNT_MIN = 16;   // The minimum number of Parameters, assuming a Filter with zero Parameters

        final int INDEX_CAPTURE_INTERVAL = 0;
        final int INDEX_CAPTURE_PERIOD = 1;
        final int INDEX_CAPTURE_FILTER = 2;
        final int INDEX_UPLOAD_COUNTER = 3;

        final int INDEX_IMAGE_FORMAT = 4;
        final int INDEX_IMAGE_WIDTH = 5;
        final int INDEX_IMAGE_HEIGHT = 6;
        final int INDEX_IMAGE_LOCALDIRECTORY = 7;
        final int INDEX_IMAGE_LOCALFILENAME = 8;
        final int INDEX_IMAGE_TIMESTAMP = 9;

        final int INDEX_SERVER_HOSTNAME = 10;
        final int INDEX_SERVER_USERNAME = 11;
        final int INDEX_SERVER_PASSWORD = 12;
        final int INDEX_SERVER_REMOTEDIRECTORY = 13;
        final int INDEX_SERVER_REMOTEFILENAME = 14;

        final int INDEX_VERBOSE = 15;

        final Instrument xmlInstrument;
        final PluginType pluginIteratorModule;
        final CommandType cmdCapture;
        final List<ParameterType> listExecutionParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final List<String> errors;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Initialise
        errors = new ArrayList<String>(10);
        dao.clearEventLogFragment();

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        xmlInstrument = commandmessage.getInstrument();
        pluginIteratorModule = (PluginType) commandmessage.getModule().copy();
        cmdCapture = (CommandType)commandmessage.getCommandType().copy();

        // We expect lots of parameters!
        listExecutionParameters = commandmessage.getExecutionParameters();

        if ((dao.getHostInstrument() != null)

            && (listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)

            && (listExecutionParameters.get(INDEX_CAPTURE_INTERVAL) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_CAPTURE_INTERVAL).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_CAPTURE_PERIOD) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_CAPTURE_PERIOD).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_CAPTURE_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_CAPTURE_FILTER).getInputDataType().getDataTypeName()))
            // We can't check the remainder because of the variable number of Filter Parameters

            && (xmlInstrument != null)
            && (xmlInstrument.getController() != null)
            && (pluginIteratorModule != null)

            // We can't check the Child to Parent links
            // There's no need to check the CommandVariant
            && (cmdCapture != null)
            && (cmdCapture.getResponse() != null)
            && (cmdCapture.getIteratedDataCommand() != null)
            && (cmdCapture.getIteratedDataCommand().getIteratedCommandCodeBase() != null)
            && (cmdCapture.getIteratedDataCommand().getIteratedCommandCode() != null))
            {
            RemoteDataConnectionInterface dataConnectionSaved;

            dataConnectionSaved = null;

            try
                {
                final int intCaptureInterval;
                final int intCapturePeriod;
                final String strFilter;
                final DataFilterType dataFilterType;

                // Read all Parameters before the Filter
                intCaptureInterval = Integer.parseInt(listExecutionParameters.get(INDEX_CAPTURE_INTERVAL).getValue());
                intCapturePeriod = Integer.parseInt(listExecutionParameters.get(INDEX_CAPTURE_PERIOD).getValue());
                strFilter = listExecutionParameters.get(INDEX_CAPTURE_FILTER).getValue();

                // Map the filter entry to a FilterType
                dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                if (dataFilterType != null)
                    {
                    final DataFilterInterface filter;

                    // Instantiate the filter required by the DataFilterType
                    filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                    if (filter != null)
                        {
                        filter.initialiseFilter();
                        DataFilterHelper.applyFilterParameters(filter,
                                                               listExecutionParameters,
                                                               INDEX_CAPTURE_FILTER);

                        // All subsequent access to the Filter must be via the DAO
                        dao.setFilter(filter);
                        }
                    else
                        {
                        throw new  IllegalArgumentException("Data Filter not found");
                        }
                    }
                else
                    {
                    throw new  IllegalArgumentException("Invalid Data Filter");
                    }

                //---------------------------------------------------------------------------------
                // Parse the remaining Parameters now we know the number of Filter Parameters

                if ((dao.getFilter() != null)
                    && (listExecutionParameters.size() == (PARAMETER_COUNT_MIN + dao.getFilter().getParameterCount())))
                    {
                    final int intUploadCounter;

                    final String strImageFormat;
                    final int intImageWidth;
                    final int intImageHeight;
                    final String strImageLocalDirectory;
                    final String strImageLocalFilename;
                    final boolean boolImageTimestamp;

                    final String strServerHostname;
                    final String strServerUsername;
                    final String strServerPassword;
                    final String strServerRemoteDirectory;
                    final String strServerRemoteFilename;
                    final RemoteDataConnectionInterface dataConnection;
                    final DataFormat dataFormatExport;

                    final boolean boolVerbose;
                    int intNextParameterIndex;

                    // Point to the next Parameter after the Filter
                    intNextParameterIndex = INDEX_CAPTURE_FILTER + dao.getFilter().getParameterCount() + 1;

                    // Capture
                    intUploadCounter = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex++).getValue());

                    // Image
                    strImageFormat = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    // Width and Height are DECIMAL
                    intImageWidth = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex++).getValue());
                    intImageHeight = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex++).getValue());
                    strImageLocalDirectory = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strImageLocalFilename = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    boolImageTimestamp = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex++).getValue());

                    // Server
                    strServerHostname = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerUsername = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerPassword = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerRemoteDirectory = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerRemoteFilename = listExecutionParameters.get(intNextParameterIndex++).getValue();

                    // One day this may be a Parameter!
                    dataFormatExport = DataFormat.CSV;

                    // Miscellaneous
                    boolVerbose = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex).getValue());

                    // Save the original DataConnection, as used by the Importer etc.
                    dataConnectionSaved = dao.getRemoteDataConnection();

                    // Use the unmodified LocalFilename and RemoteFilename as placeholders for now
                    dataConnection = new RemoteDataConnectionFTP(strServerHostname,
                                                                 strServerUsername,
                                                                 strServerPassword,
                                                                 RemoteDataConnectionInterface.TRANSFER_MODE_BINARY,
                                                                 RemoteDataConnectionInterface.CONNECTION_MODE_PASSIVE,
                                                                 strImageLocalDirectory,
                                                                 strImageLocalFilename,
                                                                 strServerRemoteDirectory,
                                                                 strServerRemoteFilename,
                                                                 dao.getEventLogFragment(),
                                                                 dao.getObservatoryClock(),
                                                                 boolDebug);
                    dao.setRemoteDataConnection(dataConnection);

                    // We should now have all validated Parameters
                    if (boolVerbose)
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_CHART
                                                               + METADATA_ACTION_PUBLISH

                                                               + METADATA_INTERVAL + intCaptureInterval + TERMINATOR_SPACE
                                                               + METADATA_PERIOD + intCapturePeriod + TERMINATOR_SPACE
                                                               + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                               + METADATA_COUNT + intUploadCounter + TERMINATOR_SPACE

                                                               + METADATA_FORMAT + strImageFormat + TERMINATOR_SPACE
                                                               + METADATA_WIDTH + intImageWidth + TERMINATOR_SPACE
                                                               + METADATA_HEIGHT + intImageHeight + TERMINATOR_SPACE
                                                               + METADATA_LOCALDIR + dataConnection.getLocalDirectory() + TERMINATOR_SPACE
                                                               + METADATA_LOCALFILE + strImageLocalFilename + TERMINATOR_SPACE
                                                               + METADATA_TIMESTAMP + boolImageTimestamp + TERMINATOR_SPACE

                                                               + METADATA_HOSTNAME + dataConnection.getHostname() + TERMINATOR_SPACE
                                                               + METADATA_USERNAME + dataConnection.getUsername() + TERMINATOR_SPACE
                                                               + METADATA_PASSWORD + dataConnection.getPassword() + TERMINATOR_SPACE
                                                               + METADATA_REMOTEDIR + dataConnection.getRemoteDirectory() + TERMINATOR_SPACE
                                                               + METADATA_REMOTEFILE + strServerRemoteFilename + TERMINATOR_SPACE
                                                               + METADATA_DATAFORMAT + dataFormatExport.getName() + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        }

                    // The DAOResponseMessageStatusList contains status information
                    strResponseValue = doIteratedStaribusMultichannelDataCaptureCommand(dao,
                                                                                        xmlInstrument,
                                                                                        pluginIteratorModule,
                                                                                        cmdCapture,
                                                                                        metadatalist,
                                                                                        null,
                                                                                        intCaptureInterval,
                                                                                        intCapturePeriod,
                                                                                        intUploadCounter,
                                                                                        strImageFormat,
                                                                                        intImageWidth,
                                                                                        intImageHeight,
                                                                                        strImageLocalDirectory,
                                                                                        strImageLocalFilename,
                                                                                        boolImageTimestamp,
                                                                                        strServerRemoteDirectory,
                                                                                        strServerRemoteFilename,
                                                                                        dataFormatExport,
                                                                                        errors,
                                                                                        notifyport,
                                                                                        boolVerbose,
                                                                                        boolDebug);
                    }
                else
                    {
                    // No Filter, or the wrong number of Parameters?
                    // So incorrectly configured XML?
                    strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                             source,
                                                                                             METADATA_TARGET_CHART,
                                                                                             METADATA_ACTION_PUBLISH));
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_PUBLISH
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_PUBLISH
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_PUBLISH
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());
                exception.printStackTrace();

                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            finally
                {
                if (dao.getRemoteDataConnection() != null)
                    {
                    // We can dispose of the RemoteDataConnection,
                    // because it was created here, not via DAO.initialiseDAO()
                    dao.getRemoteDataConnection().dispose();
                    }

                // Help the GC?
                if (dao.getFilter() != null)
                    {
                    dao.getFilter().disposeFilter();
                    }

                dao.setFilter(null);
                dao.setTranslator(null);

                // Restore the original connection, as used by the Importer etc.
                dao.setRemoteDataConnection(dataConnectionSaved);
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     source,
                                                                                     METADATA_TARGET_CHART,
                                                                                     METADATA_ACTION_PUBLISH));
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
        ObservatoryInstrumentHelper.runGarbageCollector();

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdCapture,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * doPublishRealtimeDay.
     * Use a trigger Time at which to restart the data capture.
     * It is easier to have a separate method, rather than to try to combine with doPublishRealtime().
     *
     * There are multiple layers to this implementation:
     *
     *  doPublishChartRealtime()                           --> Parameter parsing from UI, set upload credentials
     *  doIteratedStaribusMultichannelDataCaptureCommand() --> Set up Capture configuration
     *  executeCaptureAndPublish()                         --> Main Capture loop, and interval timing
     *  executeSingleCapture()                             --> Execute iterated Command in retry loop
     *  exportAndUploadThread()                                     --> Upload the image file on a separate Thread
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doPublishRealtimeDay(final ObservatoryInstrumentDAOInterface dao,
                                                                final CommandMessageInterface commandmessage,
                                                                final List<Metadata> metadatalist,
                                                                final String source,
                                                                final boolean notifyport)
        {
        final int PARAMETER_COUNT_MIN = 17;   // The minimum number of Parameters, assuming a Filter with zero Parameters

        final int INDEX_CAPTURE_TRIGGER = 0;
        final int INDEX_CAPTURE_INTERVAL = 1;
        final int INDEX_CAPTURE_PERIOD = 2;
        final int INDEX_CAPTURE_FILTER = 3;
        final int INDEX_UPLOAD_COUNTER = 4;

        final int INDEX_IMAGE_FORMAT = 5;
        final int INDEX_IMAGE_WIDTH = 6;
        final int INDEX_IMAGE_HEIGHT = 7;
        final int INDEX_IMAGE_LOCALDIRECTORY = 8;
        final int INDEX_IMAGE_LOCALFILENAME = 9;
        final int INDEX_IMAGE_TIMESTAMP = 10;

        final int INDEX_SERVER_HOSTNAME = 11;
        final int INDEX_SERVER_USERNAME = 12;
        final int INDEX_SERVER_PASSWORD = 13;
        final int INDEX_SERVER_REMOTEDIRECTORY = 14;
        final int INDEX_SERVER_REMOTEFILENAME = 15;

        final int INDEX_VERBOSE = 16;

        final Instrument xmlInstrument;
        final PluginType pluginIteratorModule;
        final CommandType cmdCapture;
        final List<ParameterType> listExecutionParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final List<String> errors;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Initialise
        errors = new ArrayList<String>(10);
        dao.clearEventLogFragment();

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        xmlInstrument = commandmessage.getInstrument();
        pluginIteratorModule = (PluginType) commandmessage.getModule().copy();
        cmdCapture = (CommandType)commandmessage.getCommandType().copy();

        // We expect lots of parameters!
        listExecutionParameters = commandmessage.getExecutionParameters();

        if ((dao.getHostInstrument() != null)

            && (listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)

            && (listExecutionParameters.get(INDEX_CAPTURE_TRIGGER) != null)
            && (SchemaDataType.TIME.equals(listExecutionParameters.get(INDEX_CAPTURE_TRIGGER).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_CAPTURE_INTERVAL) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_CAPTURE_INTERVAL).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_CAPTURE_PERIOD) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_CAPTURE_PERIOD).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_CAPTURE_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_CAPTURE_FILTER).getInputDataType().getDataTypeName()))
            // We can't check the remainder because of the variable number of Filter Parameters

            && (xmlInstrument != null)
            && (xmlInstrument.getController() != null)
            && (pluginIteratorModule != null)

            // We can't check the Child to Parent links
            // There's no need to check the CommandVariant
            && (cmdCapture != null)
            && (cmdCapture.getResponse() != null)
            && (cmdCapture.getIteratedDataCommand() != null)
            && (cmdCapture.getIteratedDataCommand().getIteratedCommandCodeBase() != null)
            && (cmdCapture.getIteratedDataCommand().getIteratedCommandCode() != null))
            {
            RemoteDataConnectionInterface dataConnectionSaved;

            dataConnectionSaved = null;

            try
                {
                final String strCaptureTrigger;
                final HourMinSecInterface hmsCaptureTrigger;
                final int intCaptureInterval;
                final int intCapturePeriod;
                final String strFilter;
                final DataFilterType dataFilterType;

                // Read all Parameters before the Filter
                strCaptureTrigger = listExecutionParameters.get(INDEX_CAPTURE_TRIGGER).getValue();
                hmsCaptureTrigger = (HourMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strCaptureTrigger,
                                                                                                     DataTypeDictionary.TIME_HH_MM_SS,
                                                                                                     EMPTY_STRING,
                                                                                                     EMPTY_STRING,
                                                                                                     errors);
                hmsCaptureTrigger.enableFormatSign(false);
                intCaptureInterval = Integer.parseInt(listExecutionParameters.get(INDEX_CAPTURE_INTERVAL).getValue());
                intCapturePeriod = Integer.parseInt(listExecutionParameters.get(INDEX_CAPTURE_PERIOD).getValue());
                strFilter = listExecutionParameters.get(INDEX_CAPTURE_FILTER).getValue();

                // Map the filter entry to a FilterType
                dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                if (dataFilterType != null)
                    {
                    final DataFilterInterface filter;

                    // Instantiate the filter required by the DataFilterType
                    filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                    if (filter != null)
                        {
                        filter.initialiseFilter();
                        DataFilterHelper.applyFilterParameters(filter,
                                                               listExecutionParameters,
                                                               INDEX_CAPTURE_FILTER);

                        // All subsequent access to the Filter must be via the DAO
                        dao.setFilter(filter);
                        }
                    else
                        {
                        throw new  IllegalArgumentException("Data Filter not found");
                        }
                    }
                else
                    {
                    throw new  IllegalArgumentException("Invalid Data Filter");
                    }

                //---------------------------------------------------------------------------------
                // Parse the remaining Parameters now we know the number of Filter Parameters

                if ((dao.getFilter() != null)
                    && (listExecutionParameters.size() == (PARAMETER_COUNT_MIN + dao.getFilter().getParameterCount())))
                    {
                    final int intUploadCounter;

                    final String strImageFormat;
                    final int intImageWidth;
                    final int intImageHeight;
                    final String strImageLocalDirectory;
                    final String strImageLocalFilename;
                    final boolean boolImageTimestamp;

                    final String strServerHostname;
                    final String strServerUsername;
                    final String strServerPassword;
                    final String strServerRemoteDirectory;
                    final String strServerRemoteFilename;
                    final RemoteDataConnectionInterface dataConnection;
                    final DataFormat dataFormatExport;

                    final boolean boolVerbose;
                    int intNextParameterIndex;

                    // Point to the next Parameter after the Filter
                    intNextParameterIndex = INDEX_CAPTURE_FILTER + dao.getFilter().getParameterCount() + 1;

                    // Capture
                    intUploadCounter = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex++).getValue());

                    // Image
                    strImageFormat = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    // Width and Height are DECIMAL
                    intImageWidth = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex++).getValue());
                    intImageHeight = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex++).getValue());
                    strImageLocalDirectory = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strImageLocalFilename = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    boolImageTimestamp = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex++).getValue());

                    // Server
                    strServerHostname = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerUsername = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerPassword = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerRemoteDirectory = listExecutionParameters.get(intNextParameterIndex++).getValue();
                    strServerRemoteFilename = listExecutionParameters.get(intNextParameterIndex++).getValue();

                    // One day this may be a Parameter!
                    dataFormatExport = DataFormat.CSV;

                    // Miscellaneous
                    boolVerbose = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex).getValue());

                    // Save the original DataConnection, as used by the Importer etc.
                    dataConnectionSaved = dao.getRemoteDataConnection();

                    // Use the unmodified LocalFilename and RemoteFilename as placeholders for now
                    dataConnection = new RemoteDataConnectionFTP(strServerHostname,
                                                                 strServerUsername,
                                                                 strServerPassword,
                                                                 RemoteDataConnectionInterface.TRANSFER_MODE_BINARY,
                                                                 RemoteDataConnectionInterface.CONNECTION_MODE_PASSIVE,
                                                                 strImageLocalDirectory,
                                                                 strImageLocalFilename,
                                                                 strServerRemoteDirectory,
                                                                 strServerRemoteFilename,
                                                                 dao.getEventLogFragment(),
                                                                 dao.getObservatoryClock(),
                                                                 boolDebug);
                    dao.setRemoteDataConnection(dataConnection);

                    // We should now have all validated Parameters
                    if (boolVerbose)
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_CHART
                                                               + METADATA_ACTION_PUBLISH

                                                               + METADATA_CAPTURE_TRIGGER + hmsCaptureTrigger.toString_HH_MM_SS() + TERMINATOR_SPACE
                                                               + METADATA_INTERVAL + intCaptureInterval + TERMINATOR_SPACE
                                                               + METADATA_PERIOD + intCapturePeriod + TERMINATOR_SPACE
                                                               + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                               + METADATA_COUNT + intUploadCounter + TERMINATOR_SPACE

                                                               + METADATA_FORMAT + strImageFormat + TERMINATOR_SPACE
                                                               + METADATA_WIDTH + intImageWidth + TERMINATOR_SPACE
                                                               + METADATA_HEIGHT + intImageHeight + TERMINATOR_SPACE
                                                               + METADATA_LOCALDIR + dataConnection.getLocalDirectory() + TERMINATOR_SPACE
                                                               + METADATA_LOCALFILE + strImageLocalFilename + TERMINATOR_SPACE
                                                               + METADATA_TIMESTAMP + boolImageTimestamp + TERMINATOR_SPACE

                                                               + METADATA_HOSTNAME + dataConnection.getHostname() + TERMINATOR_SPACE
                                                               + METADATA_USERNAME + dataConnection.getUsername() + TERMINATOR_SPACE
                                                               + METADATA_PASSWORD + dataConnection.getPassword() + TERMINATOR_SPACE
                                                               + METADATA_REMOTEDIR + dataConnection.getRemoteDirectory() + TERMINATOR_SPACE
                                                               + METADATA_REMOTEFILE + strServerRemoteFilename + TERMINATOR_SPACE
                                                               + METADATA_DATAFORMAT + dataFormatExport.getName() + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        }

                    // The DAOResponseMessageStatusList contains status information
                    strResponseValue = doIteratedStaribusMultichannelDataCaptureCommand(dao,
                                                                                        xmlInstrument,
                                                                                        pluginIteratorModule,
                                                                                        cmdCapture,
                                                                                        metadatalist,
                                                                                        hmsCaptureTrigger,
                                                                                        intCaptureInterval,
                                                                                        intCapturePeriod,
                                                                                        intUploadCounter,
                                                                                        strImageFormat,
                                                                                        intImageWidth,
                                                                                        intImageHeight,
                                                                                        strImageLocalDirectory,
                                                                                        strImageLocalFilename,
                                                                                        boolImageTimestamp,
                                                                                        strServerRemoteDirectory,
                                                                                        strServerRemoteFilename,
                                                                                        dataFormatExport,
                                                                                        errors,
                                                                                        notifyport,
                                                                                        boolVerbose,
                                                                                        boolDebug);
                    }
                else
                    {
                    // No Filter, or the wrong number of Parameters?
                    // So incorrectly configured XML?
                    strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                             source,
                                                                                             METADATA_TARGET_CHART,
                                                                                             METADATA_ACTION_PUBLISH));
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_PUBLISH
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_PUBLISH
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_PUBLISH
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());
                exception.printStackTrace();

                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            finally
                {
                if (dao.getRemoteDataConnection() != null)
                    {
                    // We can dispose of the RemoteDataConnection,
                    // because it was created here, not via DAO.initialiseDAO()
                    dao.getRemoteDataConnection().dispose();
                    }

                // Help the GC?
                if (dao.getFilter() != null)
                    {
                    dao.getFilter().disposeFilter();
                    }

                dao.setFilter(null);
                dao.setTranslator(null);

                // Restore the original connection, as used by the Importer etc.
                dao.setRemoteDataConnection(dataConnectionSaved);
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     source,
                                                                                     METADATA_TARGET_CHART,
                                                                                     METADATA_ACTION_PUBLISH));
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
        ObservatoryInstrumentHelper.runGarbageCollector();

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdCapture,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * doIteratedStaribusMultichannelDataCaptureCommand().
     * Process Command:IteratedDataCommand from instruments.xsd.
     * If the CaptureTrigger Time is not used, set to NULL.
     * CapturePeriod = 0 means run continuously.
     * This uses the Metadata in the DataCapture module for channel identification,
     * so this Command should appear in that Module.
     *
     * @param dao
     * @param instrument
     * @param iteratorplugin
     * @param iteratorcommand
     * @param metadatalist
     * @param capturetrigger
     * @param captureinterval
     * @param captureperiod
     * @param uploadcounter
     * @param imageformat
     * @param imagewidth
     * @param imageheight
     * @param localdirectory
     * @param localfilename
     * @param imagetimestamp
     * @param remotedirectory
     * @param remotefilename
     * @param exportformat
     * @param errors
     * @param notifyport
     * @param verbose
     * @param debug
     *
     * @return String
     */

    private static String doIteratedStaribusMultichannelDataCaptureCommand(final ObservatoryInstrumentDAOInterface dao,
                                                                           final Instrument instrument,
                                                                           final PluginType iteratorplugin,
                                                                           final CommandType iteratorcommand,
                                                                           final List<Metadata> metadatalist,
                                                                           final HourMinSecInterface capturetrigger,
                                                                           final int captureinterval,
                                                                           final int captureperiod,
                                                                           final int uploadcounter,
                                                                           final String imageformat,
                                                                           final int imagewidth,
                                                                           final int imageheight,
                                                                           final String localdirectory,
                                                                           final String localfilename,
                                                                           final boolean imagetimestamp,
                                                                           final String remotedirectory,
                                                                           final String remotefilename,
                                                                           final DataFormat exportformat,
                                                                           final List<String> errors,
                                                                           final boolean notifyport,
                                                                           final boolean verbose,
                                                                           final boolean debug)
        {
        final String SOURCE = "PublishRealtime.doIteratedStaribusMultichannelDataCaptureCommand() ";
        final List<Metadata> listCopiedPluginMetadata;
        final StringBuffer bufferFirstTimestamp;
        final List<DataTypeDictionary> listChannelDataTypes;
        final List<String> listTemperatureChannelFlag;
        final int intCaptureChannelCount;
        final TimeSeriesCollection collectionTimeSeries;
        final String strResponseValue;

        // Initialise
        bufferFirstTimestamp = new StringBuffer(100);
        listChannelDataTypes = new ArrayList<DataTypeDictionary>(ObservatoryInterface.MAX_CHANNELS);
        listTemperatureChannelFlag = new ArrayList<String>(1);

        // Use the previous filtered ProcessedData if we can
        if ((dao.getXYDataset() != null)
            && (dao.getXYDataset() instanceof TimeSeriesCollection))
            {
            collectionTimeSeries = (TimeSeriesCollection)dao.getXYDataset();
            }
        else
            {
            dao.setXYDataset(new TimeSeriesCollection());
            collectionTimeSeries = (TimeSeriesCollection)dao.getXYDataset();
            }

        listCopiedPluginMetadata = MetadataHelper.getCopyOfPluginMetadataList(iteratorplugin);

        // Validate the Metadata from the IteratorModule
        // and find the number of channels to process, with their DataTypes
        // The ConfigurationList is not modified
        intCaptureChannelCount = DataAnalyser.getCaptureChannelCount(listCopiedPluginMetadata,
                                                                     FrameworkStrings.EMPTY_STRING,
                                                                     listChannelDataTypes,
                                                                     listTemperatureChannelFlag);
        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        dao.establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(iteratorcommand),
                                           intCaptureChannelCount,
                                           DataAnalyser.hasTemperatureChannelInList(listTemperatureChannelFlag),
                                           listCopiedPluginMetadata,
                                           metadatalist);
        dao.setRawData(new Vector<Object>(1000));

        // Do the doIteratedStaribusMultichannelDataCaptureCommand() operation, which expects a Response...
        // Allow any number of channels
        if (intCaptureChannelCount >= 1)
            {
            final PluginType pluginIterated;
            final CommandType cmdIterated;

            pluginIterated = findIteratedPlugin(instrument,
                                                iteratorplugin,
                                                iteratorcommand,
                                                debug);
            if (pluginIterated != null)
                {
                cmdIterated = findIteratedCommandInIteratedPlugin(instrument,
                                                                  pluginIterated,
                                                                  iteratorcommand.getIteratedDataCommand(),
                                                                  debug);
                }
            else
                {
                cmdIterated = null;
                }

            // See if we now have enough to proceed...
            if ((iteratorplugin != null)
                && (pluginIterated != null)

                // Remember we can't check the Child to Parent links, because these point back to captureRawDataRealtime(),
                // since at the moment the schema allows for only one reverse link
                // We may as well check that there is a non-NULL IteratedDataCommand element
                && (cmdIterated != null)
                && (cmdIterated.getIteratedDataCommand() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCodeBase() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCode() != null)

                // This is a command specific to Staribus or Starinet
                && (cmdIterated.getResponse() != null)
                && (SchemaDataType.STARIBUS_MULTICHANNEL_DATA.toString().equals(cmdIterated.getResponse().getDataTypeName().toString())))
                {
                executeCaptureAndPublish(dao,
                                         intCaptureChannelCount,
                                         listChannelDataTypes,
                                         collectionTimeSeries,
                                         listCopiedPluginMetadata,
                                         instrument,
                                         pluginIterated,
                                         cmdIterated,
                                         capturetrigger,
                                         captureinterval,
                                         captureperiod,
                                         uploadcounter,
                                         imageformat,
                                         imagewidth,
                                         imageheight,
                                         localdirectory,
                                         localfilename,
                                         imagetimestamp,
                                         remotedirectory,
                                         remotefilename,
                                         exportformat,
                                         errors,
                                         notifyport,
                                         verbose,
                                         debug);
                }
            else
                {
                // Incorrectly configured XML
                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                         SOURCE,
                                                                                         METADATA_TARGET_RAWDATA,
                                                                                         METADATA_ACTION_CAPTURE));
                }
            }
        else
            {
            // Something is wrong with the XML definition of doIteratedStaribusMultichannelDataCaptureCommand()
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_RAWDATA,
                                                                                     METADATA_ACTION_CAPTURE));
            }

        //------------------------------------------------------------------------------------------
        // Did we succeed in getting valid data?
        // If so, try to filter it for the XYDataset, and hence the Chart
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // We'd like to keep any data after ABORT!

        if (((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
             || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            && (dao.getRawData() != null))
            {
            // Do a final filter on the whole RawData and produce the complete XYDataset
            // Always log this step, irrespective of parameter settings
            // Filter all Channels
            // The supplied Metadata MUST contain the Observation.Channel.Name
            DataFilterHelper.filterCapturedCalendarisedMultichannelRawDataToTimeSeries(dao,
                                                                                       listChannelDataTypes,
                                                                                       listCopiedPluginMetadata,
                                                                                       collectionTimeSeries,
                                                                                       true,
                                                                                       verbose,
                                                                                       SOURCE);

            // Add the most recent sample Values to the ObservationMetadata, for all channels
            MetadataHelper.addLastTimestampedValuesToAllChannels(dao);

            // Say we succeeded, even if the ProcessedData Filter failed,
            // so the data are always visible in RawData
            dao.getResponseMessageStatusList().clear();
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }
        else
            {
            // ToDo: REVIEW This must fail, because we can't interpret the data
            //dao.clearData();

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_CHART
                                                   + METADATA_ACTION_PUBLISH
                                                   + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_DATA_FORMAT + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // We'd like to keep any data after ABORT!

        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Explicitly set the ResponseValue as the Timestamp of the first data item, to save space
            strResponseValue = bufferFirstTimestamp.toString();

            // Keep re-applying the updated DAO Wrapper to the host Instrument,
            // to ensure that RawData Reports get updated
            dao.setRawDataChanged(true);
            dao.setProcessedDataChanged(true);

            // Finally, ensure that the data on the Chart are always refreshed, even if not visible
            dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                                   true,
                                                   false);
            }
        else
            {
            // Create the failed ResponseValue,
            // indicating the last Status received in the DAOResponseMessageStatusList
            strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
            }

        return (strResponseValue);
        }


    /***********************************************************************************************
     * Execute the Capture and Publish operation.
     * If the CaptureTrigger Time is not used, set to NULL.
     * All status returned via DAOResponseMessageStatusList.
     *
     * @param dao
     * @param channelcount
     * @param datatypelist
     * @param timecollection
     * @param pluginmetadatalist
     * @param instrument
     * @param iteratedplugin
     * @param iteratedcommand
     * @param capturetrigger
     * @param captureinterval
     * @param captureperiod
     * @param uploadcounter
     * @param imageformat
     * @param imagewidth
     * @param imageheight
     * @param localdirectory
     * @param localfilename
     * @param imagetimestamp
     * @param remotedirectory
     * @param remotefilename
     * @param exportformat
     * @param errors
     * @param notifyport
     * @param verbose
     * @param debug
     */

    private static void executeCaptureAndPublish(final ObservatoryInstrumentDAOInterface dao,
                                                 final int channelcount,
                                                 final List<DataTypeDictionary> datatypelist,
                                                 final TimeSeriesCollection timecollection,
                                                 final List<Metadata> pluginmetadatalist,
                                                 final Instrument instrument,
                                                 final PluginType iteratedplugin,
                                                 final CommandType iteratedcommand,
                                                 final HourMinSecInterface capturetrigger,
                                                 final int captureinterval,
                                                 final int captureperiod,
                                                 final int uploadcounter,
                                                 final String imageformat,
                                                 final int imagewidth,
                                                 final int imageheight,
                                                 final String localdirectory,
                                                 final String localfilename,
                                                 final boolean imagetimestamp,
                                                 final String remotedirectory,
                                                 final String remotefilename,
                                                 final DataFormat exportformat,
                                                 final List<String> errors,
                                                 final boolean notifyport,
                                                 final boolean verbose,
                                                 final boolean debug)
        {
        final String SOURCE = "PublishRealtime.executeCaptureAndPublish() ";

        // Check for silly parameter settings
        // CaptureTrigger may be NULL
        // CapturePeriod = 0 means run continuously
        if ((dao.getObservatoryClock() != null)
            && (dao.getObservatoryClock().getAstronomicalCalendar() != null)
            && (dao.getFilter() != null)
            && (channelcount > 0)
            && (datatypelist != null)
            && (timecollection != null)
            && (pluginmetadatalist != null)
            && (instrument != null)
            && (iteratedplugin != null)
            && (iteratedcommand != null)
            && (captureinterval > 0)
            && (captureperiod >= 0)
            && (uploadcounter > 0)
            && (errors != null))
            {
            final String strCapturePeriod;
            final int intCapturePeriodSec;
            final String strStarscript;
            final int intCaptureCountMax;
            final StringBuffer bufferFirstTimestamp;
            final ResponseMessageStatusList listStatusInCaptureLoop;
            boolean boolSuccess;
            final String strLogCaptureTrigger;
            final YearMonthDayInterface ymdCaptureStart;

            // Log the CaptureTrigger later if we can
            if (capturetrigger != null)
                {
                strLogCaptureTrigger = METADATA_CAPTURE_TRIGGER + capturetrigger.toString_HH_MM_SS() + TERMINATOR_SPACE;
                }
            else
                {
                strLogCaptureTrigger = EMPTY_STRING;
                }

            // Correct the Period for continuous operation
            if (captureperiod == 0)
                {
                strCapturePeriod = MSG_PERIOD_CONTINUOUS;
                intCapturePeriodSec = Integer.MAX_VALUE;
                }
            else
                {
                strCapturePeriod = Integer.toString(captureperiod);
                intCapturePeriodSec = captureperiod;
                }

            // This *assumes* that the Parameters are taken from the Command verbatim
            strStarscript = StarscriptHelper.buildSimpleStarscript(instrument,
                                                                   iteratedplugin,
                                                                   null,
                                                                   iteratedcommand,
                                                                   false);
            // Log this, even if not verbose
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_CAPTURE
                                                   + strLogCaptureTrigger
                                                   + METADATA_INTERVAL + captureinterval + TERMINATOR_SPACE
                                                   + METADATA_PERIOD + strCapturePeriod + TERMINATOR_SPACE
                                                   + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                   + METADATA_STARSCRIPT + iteratedplugin.getIdentifier() + DOT + iteratedcommand.getIdentifier() + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());

            bufferFirstTimestamp = new StringBuffer(100);
            intCaptureCountMax = intCapturePeriodSec / captureinterval;
            listStatusInCaptureLoop = ResponseMessageStatus.createResponseMessageStatusList();
            boolSuccess = true;

            // Record the YMD when this capture sequence started
            // so that we can test for a trigger event on the *next* day
            ymdCaptureStart = dao.getObservatoryClock().getAstronomicalCalendar().toYMD();

            // Now execute the IteratedCommand every SampleInterval,
            // until CapturePeriod has elapsed or until we see an ABORT or TIMEOUT
            for (int intCaptureIndex = 0;
                 ((intCaptureIndex < intCaptureCountMax)
                  && (boolSuccess)
                  && (Utilities.executeWorkerCanProceed(dao)));
                 intCaptureIndex++)
                {
                final long longTimeStart;
                final long longTimeFinish;
                final long longTimeToWait;

                // Record the time the capture started
                longTimeStart = dao.getObservatoryClock().getSystemTimeMillis();

                boolSuccess = executeSingleCapture(dao,
                                                   channelcount,
                                                   datatypelist,
                                                   timecollection,
                                                   pluginmetadatalist,
                                                   listStatusInCaptureLoop,
                                                   strStarscript,
                                                   instrument,
                                                   iteratedplugin,
                                                   iteratedcommand,
                                                   intCaptureIndex,
                                                   ymdCaptureStart,
                                                   capturetrigger,
                                                   uploadcounter,
                                                   imageformat,
                                                   imagewidth,
                                                   imageheight,
                                                   localdirectory,
                                                   localfilename,
                                                   imagetimestamp,
                                                   remotedirectory,
                                                   remotefilename,
                                                   exportformat,
                                                   bufferFirstTimestamp,
                                                   errors,
                                                   notifyport,
                                                   verbose,
                                                   debug);

                // Take account of the time elapsed so far...
                longTimeFinish = dao.getObservatoryClock().getSystemTimeMillis();
                longTimeToWait = (captureinterval * ChronosHelper.SECOND_MILLISECONDS) - (longTimeFinish - longTimeStart);

                if ((boolSuccess)
                    && (longTimeToWait >= 0))
                    {
                    // Wait for the required time, but only if we succeeded
                    Utilities.safeSleepPollExecuteWorker(longTimeToWait, dao);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_CAPTURE
                                                           + METADATA_ERROR + "Invalid sample interval" + TERMINATOR_SPACE
                                                           + "[interval=" + longTimeToWait + " msec]",
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }
                }

            // Capture the final Status and ResponseValue (these may show a failure)
            dao.getResponseMessageStatusList().addAll(listStatusInCaptureLoop);
            }
        else
            {
            if ((dao.getObservatoryClock() == null)
                || (dao.getObservatoryClock().getAstronomicalCalendar() == null))
                {
                // There is no Observatory Clock
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_CAPTURE
                                                       + METADATA_ERROR + MSG_NO_CLOCK + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
            }
        }


    /***********************************************************************************************
     * Execute a single Capture and Publish.
     * If the CaptureTrigger Time is not used, set to NULL.
     *
     * @param dao
     * @param channelcount
     * @param datatypelist
     * @param timecollection
     * @param pluginmetadatalist
     * @param statuslist
     * @param starscript
     * @param instrument
     * @param iteratedplugin
     * @param iteratedcommand
     * @param captureindex
     * @param capturetrigger
     * @param ymdcapturestart
     * @param uploadcounter
     * @param imageformat
     * @param imagewidth
     * @param imageheight
     * @param localdirectory
     * @param localfilename
     * @param imagetimestamp
     * @param remotedirectory
     * @param remotefilename
     * @param exportformat
     * @param buffertimestamp
     * @param errors
     * @param notifyport
     * @param verbose
     * @param debug
     *
     * @return boolean
     */

    private static boolean executeSingleCapture(final ObservatoryInstrumentDAOInterface dao,
                                                final int channelcount,
                                                final List<DataTypeDictionary> datatypelist,
                                                final TimeSeriesCollection timecollection,
                                                final List<Metadata> pluginmetadatalist,
                                                final ResponseMessageStatusList statuslist,
                                                final String starscript,
                                                final Instrument instrument,
                                                final PluginType iteratedplugin,
                                                final CommandType iteratedcommand,
                                                final int captureindex,
                                                final YearMonthDayInterface ymdcapturestart,
                                                final HourMinSecInterface capturetrigger,
                                                final int uploadcounter,
                                                final String imageformat,
                                                final int imagewidth,
                                                final int imageheight,
                                                final String localdirectory,
                                                final String localfilename,
                                                final boolean imagetimestamp,
                                                final String remotedirectory,
                                                final String remotefilename,
                                                final DataFormat exportformat,
                                                final StringBuffer buffertimestamp,
                                                final List<String> errors,
                                                final boolean notifyport,
                                                final boolean verbose,
                                                final boolean debug)
        {
        final String SOURCE = "PublishRealtime.executeSingleCapture() ";
        ResponseMessageInterface responseData;
        final boolean boolRealtimeUpdate;
        final boolean boolCollectAggregateMetadata;
        boolean boolSuccessfulSubCommand;
        final boolean boolSuccess;

        // Let's assume we always do an update
        boolRealtimeUpdate = true;
        boolCollectAggregateMetadata = true;

        dao.getResponseMessageStatusList().clear();
        responseData =  null;
        boolSuccessfulSubCommand = false;

        // Attempt the IteratedCommand again if it fails
        for (int retryid = 0;
             ((retryid < TimeoutHelper.RETRY_COUNT)
              && (!boolSuccessfulSubCommand)
              && (Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker())));
             retryid++)
            {
            TimeoutHelper.logRetryEvent(dao, starscript, retryid);

            statuslist.clear();

            // Reset the DAO timeout on every retry to the Timeout for IteratedPlugin.iteratedCommand()
            TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(iteratedplugin, iteratedcommand));

            // This command will set off the *Queue* wait loop,
            // which only has to deal with a single command, usually getRealtimeData()
            // If we get TIMEOUT, try again until retries exhausted
            // All ResponseMessages come back with a new DAOWrapper
            // The latest Parameter values are added to the ResponseMessage DAOWrapper ObservationMetadata,
            // which points to the DAO ObservationMetadata
            // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
            responseData = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                           dao,
                                                                           instrument,
                                                                           iteratedplugin,
                                                                           iteratedcommand,
                                                                           // This *assumes* that the Parameters are taken from the Command verbatim
                                                                           iteratedcommand.getParameterList(),
                                                                           starscript,
                                                                           errors,
                                                                           notifyport,
                                                                           verbose);
            // Are the data ok this time round?
            boolSuccessfulSubCommand = ((responseData != null)
                                        && (ResponseMessageStatus.wasResponseSuccessful(responseData))
                                        && (responseData.getWrappedData() != null));

            // We don't make any use of the failed status from executeCommandOnSameThread()
            if (!boolSuccessfulSubCommand)
                {
                final int intStatus;

                if (responseData != null)
                    {
                    // We only need the status from a failed Command
                    intStatus = responseData.getStatusBits();

                    // Keep the GC happy...
                    // This is VERY IMPORTANT - otherwise each message holds on to each dataset...
                    responseData.setWrappedData(null);
                    }
                else
                    {
                    intStatus = 0;
                    }

                LOGGER.error(SOURCE + "End of RETRY [retry=" + retryid + "] [status=" + Utilities.intToBitString(intStatus) + "]");
                }
            }

        // The IteratedCommand must be completed without error or timeout,
        // after the maximum buber of retry attempts
        // Repeat the full logical test for simplicity
        // WARNING!! responseData must return with non-null DAOWrapper
        // since we need to parse the ResponseValue into channel data

        if ((responseData != null)
            && (ResponseMessageStatus.wasResponseSuccessful(responseData))
            && (responseData.getWrappedData() != null)
            && (responseData.getWrappedData().getResponseValue() != null))
            {
            final List<Object> listChannelData;

            // Ensure everything points to the same thing....
            dao.setRawDataChanged(true);
            dao.setProcessedDataChanged(true);
            dao.setWrappedData(responseData.getWrappedData());

            // Parse and append the data from the IteratedCommand ResponseValue
            // The Response DataType must be StaribusMultichannelData,
            // so it has already been checked for syntax by the ResponseParser
            listChannelData = StaribusParsers.parseStaribusMultichannelDataIntoList(new StringBuffer(responseData.getWrappedData().getResponseValue()),
                                                                                    ObservatoryConstants.STARIBUS_RESPONSE_SEPARATOR_REGEX,
                                                                                    datatypelist,
                                                                                    channelcount,
                                                                                    errors);
            // Did we successfully parse all channels?
            if ((listChannelData != null)
                && (listChannelData.size() == channelcount))
                {
                // Is there (still) an Observatory Clock?
                if ((dao.getObservatoryClock() != null)
                   && (dao.getObservatoryClock().getAstronomicalCalendar() != null)
                   && (dao.getObservatoryClock().getAstronomicalCalendar().getCalendar() != null))
                    {
                    final Vector vecData;

                    // Is this the first data item?
                    // If so, timestamp it for use later in the Data.Timestamp ResponseValue
                    if (buffertimestamp.length() == 0)
                        {
                        buffertimestamp.append(ChronosHelper.toDateString(dao.getObservatoryClock().getCalendarDateNow()));
                        buffertimestamp.append(FrameworkStrings.SPACE);
                        buffertimestamp.append(ChronosHelper.toTimeString(dao.getObservatoryClock().getCalendarTimeNow()));
                        }

                    // The data output must be one Calendar and DiscoveredChannelCount Numerics (DataTypes known to the filters)
                    vecData = new Vector(channelcount + 1);

                    // Remember that AstronomicalCalendarInterface is NOT a Calendar
                    // Also remember we need a separate Calendar for each observation!
                    vecData.add(dao.getObservatoryClock().getAstronomicalCalendar().getCalendar().clone());

                    for (int intChannelIndex = 0;
                         intChannelIndex < listChannelData.size();
                         intChannelIndex++)
                        {
                        // Currently only Double, Float and Integer are supported by the Filters and charts etc.
                        vecData.add(listChannelData.get(intChannelIndex));
                        }

                    // Accumulate the data we have collected into the RawData of this DAO,
                    // which is accessible via the DAOWrapper
                    dao.getRawData().add(vecData);
                    dao.setUnsavedData(true);
                    }
                else
                    {
                    // There is no Observatory Clock
                    LOGGER.error(SOURCE + MSG_NO_CLOCK + ", so StaribusMultichannelData cannot be recorded");

                    if ((dao.getObservatoryClock() == null)
                        || (dao.getObservatoryClock().getAstronomicalCalendar() == null))
                        {
                        // There is no Observatory Clock
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.FATAL,
                                                           METADATA_TARGET_RAWDATA
                                                               + METADATA_ACTION_CAPTURE
                                                               + METADATA_ERROR + MSG_NO_CLOCK + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        }

                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "Unable to parse all channels of a StaribusMultichannelData Response");
                }

            //------------------------------------------------------------------
            // Do we need to update now?

            if (boolRealtimeUpdate)
                {
                // Filter the DAO RawData, to produce the XYDataset
                // The filter will be aware of the sample DataTypes
                // The supplied Metadata MUST contain the Observation.Channel.Name
                DataFilterHelper.filterCapturedCalendarisedMultichannelRawDataToTimeSeries(dao,
                                                                                           datatypelist,
                                                                                           pluginmetadatalist,
                                                                                           timecollection,
                                                                                           boolRealtimeUpdate,
                                                                                           verbose,
                                                                                           SOURCE);

                // Add the most recent sample Values to the ObservationMetadata, for all channels
                MetadataHelper.addLastTimestampedValuesToAllChannels(dao);

                // Keep re-applying the updated DAO Wrapper to the host Instrument,
                // to ensure that RawData Reports get updated
                // Charts etc. are updated on different Threads, so should not slow this one down...
                // Should dao.getWrappedData() do the wrap??
                dao.setRawDataChanged(true);
                dao.setProcessedDataChanged(true);

                // Do the realtime update
                // Ensure that the data on the Chart are always refreshed, even if not visible
                dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                                       true,
                                                       boolCollectAggregateMetadata);

                // Don't collect the AggregateMetadata more than once
                // Values should be updated because they are referenced from the Composite collection

                // Now clear the Log fragments,
                // since these were added to the Instrument Logs by the above
                dao.getEventLogFragment().clear();
                dao.getInstrumentLogFragment().clear();

                // Remove the reference to the DAO data in the ResponseMessage!
                // This is VERY IMPORTANT - otherwise each message holds on to each dataset...
                responseData.setWrappedData(null);
                }

            // Start the Export and Upload operation, but only if it is now time
            if ((captureindex > 0)
                && ((captureindex % uploadcounter) == 0))
                {
                // We don't know when this Thread will finish,
                // so it is not easy to get a status code back
                exportAndUploadThread(dao,
                                      ymdcapturestart,
                                      capturetrigger,
                                      imageformat,
                                      imagewidth,
                                      imageheight,
                                      localdirectory,
                                      localfilename,
                                      imagetimestamp,
                                      remotedirectory,
                                      remotefilename,
                                      exportformat,
                                      verbose,
                                      debug);
                }

            // This might be the last time through, so say it is successful
            statuslist.add(ResponseMessageStatus.SUCCESS);
            boolSuccess = true;
            }
        else
            {
            // Terminate the loop immediately on failure, since the Retry gave up
            LOGGER.error(SOURCE + "Failed after retries");

            if ((responseData != null)
                && (responseData.getResponseMessageStatusList() != null)
                && (!responseData.getResponseMessageStatusList().isEmpty()))
                {
                // Handle multiple errors
                statuslist.addAll(responseData.getResponseMessageStatusList());
                }
            else
                {
                // We don't know what happened...
                statuslist.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            boolSuccess = false;
            }

        // Tidy up after every Command, just in case
        if (responseData != null)
            {
            // Keep the GC happy...
            // This is VERY IMPORTANT - otherwise each message holds on to each dataset...
            responseData.setWrappedData(null);
            }

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Do the Chart and Data local Export and remote Upload operation.
     * Do this on a separate Thread, since we don't know how long it will take.
     *
     * @param dao
     * @param ymdcapturestart
     * @param capturetrigger
     * @param imageformat
     * @param imagewidth
     * @param imageheight
     * @param localdirectory
     * @param localfilename
     * @param imagetimestamp
     * @param remotedirectory
     * @param remotefilename
     * @param exportformat
     * @param verbose
     * @param debug
     */

    private static void exportAndUploadThread(final ObservatoryInstrumentDAOInterface dao,
                                              final YearMonthDayInterface ymdcapturestart,
                                              final HourMinSecInterface capturetrigger,
                                              final String imageformat,
                                              final int imagewidth,
                                              final int imageheight,
                                              final String localdirectory,
                                              final String localfilename,
                                              final boolean imagetimestamp,
                                              final String remotedirectory,
                                              final String remotefilename,
                                              final DataFormat exportformat,
                                              final boolean verbose,
                                              final boolean debug)
        {
        final String SOURCE = "PublishRealtime.exportAndUploadThread() ";
        final SwingWorker workerUpload;

        workerUpload = new SwingWorker(REGISTRY.getThreadGroup(),
                                       SOURCE + "SwingWorker [group=" + REGISTRY.getThreadGroup().getName() + "]")
            {
            public Object construct()
                {
                final boolean boolUploadOk;

                if (dao.getRemoteDataConnection() != null)
                    {
                    final String strFullLocalChartFilename;
                    final String strFullRemoteChartFilename;
                    final String strExportChartPathname;
                    final boolean boolChartExportOk;

                    // Remember local and remote files may have different names
                    // Add the file extension, and a timestamp if required
                    strFullLocalChartFilename = FileUtilities.buildFullFilename(localfilename,
                                                                                imagetimestamp,
                                                                                imageformat);

                    strFullRemoteChartFilename = FileUtilities.buildFullFilename(remotefilename,
                                                                                 imagetimestamp,
                                                                                 imageformat);
                    // Uses these names for the transfer of the Chart
                    dao.getRemoteDataConnection().setLocalDirectory(localdirectory);
                    dao.getRemoteDataConnection().setLocalFilename(strFullLocalChartFilename);
                    dao.getRemoteDataConnection().setRemoteDirectory(remotedirectory);
                    dao.getRemoteDataConnection().setRemoteFilename(strFullRemoteChartFilename);

                    // Form the *full* pathname of the Chart for the local Export
                    // as expected by exportChartUsingFilename()
                    strExportChartPathname = dao.getRemoteDataConnection().getLocalDirectory()
                                               + System.getProperty("file.separator")
                                               + dao.getRemoteDataConnection().getLocalFilename();

                    LOGGER.debug(debug,
                                 SOURCE + "CHART EXPORT [export.pathname=" + strExportChartPathname + "]");

                    // Export the Chart image file into the LOCAL filesystem at:
                    // localdirectory/localfilename_timestamp.extension
                    boolChartExportOk = DataExporter.exportChartUsingFilename(dao,
                                                                              dao.getChartUI(),
                                                                              MetadataHelper.collectMetadataForExportFromDAO(dao, false),
                                                                              strExportChartPathname,
                                                                              imageformat,
                                                                              imagewidth,
                                                                              imageheight,
                                                                              dao.getEventLogFragment(),
                                                                              dao.getObservatoryClock(),
                                                                              verbose);
                    if (boolChartExportOk)
                        {
                        final boolean boolInitialisedOk;

                        boolInitialisedOk = dao.getRemoteDataConnection().initialise(dao.getTimeoutDefaultMillis(), verbose);

                        if (boolInitialisedOk)
                            {
                            final boolean boolConnectedOk;

                            boolConnectedOk = dao.getRemoteDataConnection().login();

                            if (boolConnectedOk)
                                {
                                final boolean boolChartTransmitOk;

                                LOGGER.debug(debug,
                                             SOURCE + "CHART TRANSMIT"
                                                     + "  [local.directory=" + dao.getRemoteDataConnection().getLocalDirectory()
                                                     + "] [local.filename=" + dao.getRemoteDataConnection().getLocalFilename()
                                                     + "] [remote.directory=" + dao.getRemoteDataConnection().getRemoteDirectory()
                                                     + "] [remote.directory=" + dao.getRemoteDataConnection().getRemoteFilename()
                                                     + "]");
                                boolChartTransmitOk = dao.getRemoteDataConnection().transmit();

                                if (boolChartTransmitOk)
                                    {
                                    LOGGER.debug(debug, SOURCE + "Chart transmit succeeded");

                                    //-------------------------------------------------------------
                                    // Is it time to begin a new capture sequence?

                                    if ((capturetrigger != null)
                                        && (dao.getObservatoryClock() != null)
                                        && (dao.getObservatoryClock().getAstronomicalCalendar() != null))
                                        {
                                        final YearMonthDayInterface ymdNow;
                                        final HourMinSecInterface hmsNow;

                                        ymdNow = dao.getObservatoryClock().getAstronomicalCalendar().toYMD();
                                        hmsNow = dao.getObservatoryClock().getAstronomicalCalendar().toHMS();
                                        hmsNow.enableFormatSign(false);

                                        LOGGER.debug(debug,
                                                     SOURCE + "EXPORT DATA ON TRIGGER?"
                                                                 + " [ymd.start=" + ymdcapturestart.toString()
                                                                 + "] [capture.trigger=" + capturetrigger.toString_HH_MM_SS()
                                                                 + "] [ymd.now=" + ymdNow.toString()
                                                                 + "] [hms.now=" + hmsNow.toString_HH_MM_SS()
                                                                 + "] [ymd.equalorafter=" + (ymdNow.equalOrAfter(ymdcapturestart))
                                                                 + "] [hms.equalorafter=" + (hmsNow.equalOrAfter(capturetrigger))
                                                                 + "] [ymd.before=" + (ymdNow.before(ymdcapturestart))
                                                                 + "] [hms.before=" + (hmsNow.before(capturetrigger))
                                                                 + "] [daynumber.now=" + YearMonthDayParser.getDayNumberOfYear(ymdNow.getYear(),
                                                                                                                               ymdNow.getMonth() - 1,
                                                                                                                               ymdNow.getDay())
                                                                 + "]");

                                        if ((ymdNow.equalOrAfter(ymdcapturestart))
                                            && (hmsNow.equalOrAfter(capturetrigger)))
                                            {
                                            final DataTranslatorInterface translator;
                                            final Calendar calendarTomorrow;
                                            boolean boolDataExportOk;

                                            LOGGER.debug(debug,
                                                         SOURCE + "DATA PUBLISH WAS RESET AT " + ymdNow.toString() + SPACE + hmsNow.toString_HH_MM_SS());

                                            // Instantiate the translator required by the DataFormat
                                            translator = DataTranslatorHelper.instantiateTranslator(exportformat.getTranslatorClassname());

                                            boolDataExportOk = false;

                                            if (translator != null)
                                                {
                                                final String strExportDataPathname;

                                                // Set the translator for this DAO (until changed by another command)
                                                dao.setTranslator(translator);
                                                dao.getTranslator().initialiseTranslator();

                                                // Export the Data file into the LOCAL filesystem at:
                                                // localpathname_timestamp.extension
                                                // NOTE: exportRawData() adds timestamp.extension
                                                strExportDataPathname = localdirectory
                                                                           + System.getProperty("file.separator")
                                                                           + localfilename;

                                                LOGGER.debug(debug,
                                                             SOURCE + "DATA EXPORT [export.pathname=" + strExportDataPathname + "]");

                                                // The file is always timestamped because it must be unique
                                                boolDataExportOk = dao.getTranslator().exportRawData(dao.getWrappedData(),
                                                                                                     strExportDataPathname,
                                                                                                     true,
                                                                                                     dao.getEventLogFragment(),
                                                                                                     dao.getObservatoryClock());
                                                // See if there's anything we need to know...
                                                DataTranslatorHelper.addTranslatorMessages(dao.getTranslator(),
                                                                                           dao.getEventLogFragment(),
                                                                                           dao.getObservatoryClock(),
                                                                                           dao.getLocalHostname());
                                                }

                                            if (boolDataExportOk)
                                                {
                                                final String strLogCaptureTrigger;
                                                final String strFullLocalDataFilename;
                                                final String strFullRemoteDataFilename;
                                                final boolean boolTransmitOk;

                                                strLogCaptureTrigger = METADATA_CAPTURE_TRIGGER + capturetrigger.toString_HH_MM_SS() + TERMINATOR_SPACE;

                                                // Export and Upload the data recorded in this sequence, before beginning another
                                                // Remember local and remote files may have different names
                                                // Add the file extension, and a timestamp if required
                                                strFullLocalDataFilename = FileUtilities.buildFullFilename(localfilename,
                                                                                                           true,
                                                                                                           exportformat);

                                                strFullRemoteDataFilename = FileUtilities.buildFullFilename(remotefilename,
                                                                                                            true,
                                                                                                            exportformat);

                                                // The LocalDirectory and RemoteDirectory are the same as for the Chart
                                                dao.getRemoteDataConnection().setLocalDirectory(localdirectory);
                                                dao.getRemoteDataConnection().setLocalFilename(strFullLocalDataFilename);
                                                dao.getRemoteDataConnection().setRemoteDirectory(remotedirectory);
                                                dao.getRemoteDataConnection().setRemoteFilename(strFullRemoteDataFilename);

                                                LOGGER.debug(debug,
                                                             SOURCE + "DATA TRANSMIT"
                                                                     + "  [local.directory=" + dao.getRemoteDataConnection().getLocalDirectory()
                                                                     + "] [local.filename=" + dao.getRemoteDataConnection().getLocalFilename()
                                                                     + "] [remote.directory=" + dao.getRemoteDataConnection().getRemoteDirectory()
                                                                     + "] [remote.directory=" + dao.getRemoteDataConnection().getRemoteFilename()
                                                                     + "]");

                                                // Upload from localdir/localfile to remotedir/remotefile
                                                boolTransmitOk = dao.getRemoteDataConnection().transmit();

                                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                                   EventStatus.WARNING,
                                                                                   METADATA_TARGET_RAWDATA
                                                                                       + METADATA_ACTION_CAPTURE_RESET
                                                                                       + strLogCaptureTrigger,
                                                                                   dao.getLocalHostname(),
                                                                                   dao.getObservatoryClock());
                                                dao.getHostInstrument().getInstrumentPanel().flushLogFragments(dao.getWrappedData());

                                                // Now clear all of the current RawData and ProcessedData and the Chart
                                                clearData(dao);

                                                dao.getRemoteDataConnection().logout();

                                                if (boolTransmitOk)
                                                    {
                                                    boolUploadOk = true;
                                                    }
                                                else
                                                    {
                                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                                       EventStatus.WARNING,
                                                                                       METADATA_TARGET_RAWDATA
                                                                                           + METADATA_ACTION_UPLOAD
                                                                                           + METADATA_RESULT + "data transmit failed" + TERMINATOR,
                                                                                       dao.getRemoteDataConnection().getHostname(),
                                                                                       dao.getObservatoryClock());
                                                    boolUploadOk = false;
                                                    }
                                                }
                                            else
                                                {
                                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                                   EventStatus.WARNING,
                                                                                   METADATA_TARGET_RAWDATA
                                                                                       + METADATA_ACTION_UPLOAD
                                                                                       + METADATA_RESULT + "unable to export data" + TERMINATOR,
                                                                                   dao.getRemoteDataConnection().getHostname(),
                                                                                   dao.getObservatoryClock());
                                                boolUploadOk = false;
                                                }

                                            // Reset the YMD trigger ready for the next sequence, regardless of the outcome of the above
                                            // This must occur on the *next* day
                                            calendarTomorrow = (Calendar)dao.getObservatoryClock().getAstronomicalCalendar().getCalendar().clone();
                                            calendarTomorrow.roll(Calendar.DAY_OF_YEAR, true);
                                            ymdcapturestart.copy(new YearMonthDayDataType(calendarTomorrow));

                                            LOGGER.debug(debug,
                                                         SOURCE + "The next trigger will occur on " + ymdcapturestart.toString());
                                            }
                                        else
                                            {
                                            // There is no action to take if the Day is the same, and Capture.Trigger has not been passed
                                            boolUploadOk = true;
                                            }
                                        }
                                    else
                                        {
                                        if ((dao.getObservatoryClock() == null)
                                            || (dao.getObservatoryClock().getAstronomicalCalendar() == null))
                                            {
                                            // There is no Observatory Clock
                                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                               EventStatus.FATAL,
                                                                               METADATA_TARGET_RAWDATA
                                                                                   + METADATA_ACTION_UPLOAD
                                                                                   + METADATA_ERROR + MSG_NO_CLOCK + TERMINATOR,
                                                                               dao.getLocalHostname(),
                                                                               dao.getObservatoryClock());

                                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                                            boolUploadOk = false;
                                            }
                                        else
                                            {
                                            // If Capture.Trigger is not set, then there is no action to take
                                            boolUploadOk = true;
                                            }
                                        }
                                    }
                                else
                                    {
                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_CHART
                                                                           + METADATA_ACTION_UPLOAD
                                                                           + METADATA_RESULT + "chart transmit failed" + TERMINATOR,
                                                                       dao.getRemoteDataConnection().getHostname(),
                                                                       dao.getObservatoryClock());
                                    boolUploadOk = false;
                                    }

                                dao.getRemoteDataConnection().logout();
                                }
                            else
                                {
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_CHART
                                                                       + METADATA_ACTION_UPLOAD
                                                                       + METADATA_RESULT + "unable to connect to data server" + TERMINATOR,
                                                                   dao.getRemoteDataConnection().getHostname(),
                                                                   dao.getObservatoryClock());
                                boolUploadOk = false;
                                }
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_CHART
                                                                   + METADATA_ACTION_UPLOAD
                                                                   + METADATA_RESULT + "unable to login to server" + TERMINATOR,
                                                               dao.getRemoteDataConnection().getHostname(),
                                                               dao.getObservatoryClock());
                            boolUploadOk = false;
                            }

                        dao.getRemoteDataConnection().dispose();
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_CHART
                                                               + METADATA_ACTION_UPLOAD
                                                               + METADATA_RESULT + "unable to export chart" + TERMINATOR,
                                                           dao.getRemoteDataConnection().getHostname(),
                                                           dao.getObservatoryClock());
                        boolUploadOk = false;
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_CHART
                                                           + METADATA_ACTION_UPLOAD
                                                           + METADATA_RESULT + "unable to create data connection" + TERMINATOR,
                                                       dao.getRemoteDataConnection().getHostname(),
                                                       dao.getObservatoryClock());
                    boolUploadOk = false;
                    }

                return (boolUploadOk);
                }


            /***********************************************************************************************
             * Eventually the Thread will complete.
             */

            public void finished()
                {
                if ((get() != null)
                    && (get() instanceof Boolean))
                    {
                    if ((Boolean)get())
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_CHART
                                                               + METADATA_ACTION_UPLOAD
                                                               + METADATA_RESULT + "upload completed" + TERMINATOR,
                                                           dao.getRemoteDataConnection().getHostname(),
                                                           dao.getObservatoryClock());
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.FATAL,
                                                           METADATA_TARGET_CHART
                                                               + METADATA_ACTION_UPLOAD
                                                               + METADATA_RESULT + "upload failed" + TERMINATOR,
                                                           dao.getRemoteDataConnection().getHostname(),
                                                           dao.getObservatoryClock());
                        }
                    }
                else
                    {
                    LOGGER.debug(debug, SOURCE + "SwingWorker did not return a Boolean");
                    }
                }
            };

        // Start the Thread we have prepared...
        workerUpload.start();
        }


    /***********************************************************************************************
     * Clear all of the current RawData and ProcessedData and the Chart.
     * Force a refresh of the Data on the Chart.
     * Do not update any Metadata.
     *
     * @param dao
     */

    private static void clearData(final ObservatoryInstrumentDAOInterface dao)
        {
        dao.getRawData().clear();

        // ... and the ProcessedData
        // We should only be dealing with a TimeSeriesCollection
        if ((dao.getXYDataset() != null)
            && (dao.getXYDataset() instanceof TimeSeriesCollection))
            {
            ((TimeSeriesCollection)dao.getXYDataset()).removeAllSeries();
            }
        else
            {
            dao.setXYDataset(null);
            }

        // Force the existing Chart to be redrawn, which will now show no sample points
        dao.setRawDataChanged(true);
        dao.setProcessedDataChanged(true);
        dao.setUnsavedData(false);

        // Ensure that the data on the Chart are always refreshed, even if not visible
        dao.getWrappedData().harmoniseWrapperWithDAO(dao);
        dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                               true,
                                               false);
        }


    /***********************************************************************************************
     * Find the Plugin module containing the Command to be iterated.
     *
     * @param instrument
     * @param iteratorplugin
     * @param iteratorcommand
     * @param debug
     *
     * @return PluginType
     */

    private static PluginType findIteratedPlugin(final Instrument instrument,
                                                 final PluginType iteratorplugin,
                                                 final CommandType iteratorcommand,
                                                 final boolean debug)
        {
        final String SOURCE = "PublishRealtime.findIteratedPlugin() ";
        PluginType pluginIterated;
        final String strIteratedCommandCodeBase;
        final String strIteratedCommandCode;
        final StringBuffer expression;
        final XmlObject[] selection;

        // Retrieve the Command Codes to execute
        strIteratedCommandCodeBase = iteratorcommand.getIteratedDataCommand().getIteratedCommandCodeBase();
        strIteratedCommandCode = iteratorcommand.getIteratedDataCommand().getIteratedCommandCode();

        // Find the Plugin:Command to which these codes relate
        pluginIterated = null;
        expression = new StringBuffer();

        // The CommandCodeBase:CommandCode identifies the specific Command
        // The XML holds these values as two-character Hex numbers
        // Find the Module containing the CommandCodeBase
        expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
        expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_CCB);
        expression.append(strIteratedCommandCodeBase);
        expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

        LOGGER.debug(debug,
                     SOURCE + "IteratorPlugin [commandcodebase=" + iteratorplugin.getCommandCodeBase()
                     + "] [id=" + iteratorplugin.getIdentifier() + "]");

        LOGGER.debug(debug,
                     SOURCE + "IteratorCommand [commandcode=" + iteratorcommand.getCommandCode()
                     + "] [id=" + iteratorcommand.getIdentifier() + "]");

        LOGGER.debug(debug,
                     SOURCE + "IteratedCommand [commandcodebase=" + strIteratedCommandCodeBase
                     + "] [commandcode=" + strIteratedCommandCode + "]");

        LOGGER.debug(debug,
                     SOURCE + "Find Iterated Plugin [xpath=" + expression.toString() + "]");

        // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
        selection = instrument.getController().selectPath(expression.toString());

        // The Plugin CommandCodeBase should be unique, but if not, take the first
        if ((selection != null)
            && (selection instanceof PluginType[])
            && (selection.length >= 1)
            && (selection[0] != null)
            && (selection[0] instanceof PluginType))
            {
            // Don't affect the Plugin in the Instrument XML
            pluginIterated = (PluginType)selection[0].copy();

            LOGGER.debug(debug,
                         SOURCE + "Iterated Plugin [commandcodebase=" + pluginIterated.getCommandCodeBase()
                         + "] [id=" + pluginIterated.getIdentifier() + "]");

            LOGGER.debug(debug,
                         SOURCE + "Iterated Plugin=[" + pluginIterated.xmlText() + "]");
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "Iterated Plugin NOT FOUND [commandcodebase=" + strIteratedCommandCodeBase
                         + "] [xpath=" + expression.toString() + "]");
            }

        return (pluginIterated);
        }


    /***********************************************************************************************
     * Find the Command to be iterated, in the iterated Plugin.
     *
     * @param instrument
     * @param iteratedplugin
     * @param iteratedcode
     * @param debug
     *
     * @return CommandType
     */

    private static CommandType findIteratedCommandInIteratedPlugin(final Instrument instrument,
                                                                   final PluginType iteratedplugin,
                                                                   final IteratedCode iteratedcode,
                                                                   final boolean debug)
        {
        final String SOURCE = "PublishRealtime.findIteratedCommandInIteratedPlugin() ";
        CommandType cmdIterated;
        final String strIteratedCommandCode;
        final StringBuffer expression;
        final XmlObject[] selection;

        cmdIterated = null;
        expression = new StringBuffer();

        strIteratedCommandCode = iteratedcode.getIteratedCommandCode();

        // Now search in the Plugin to find the Command with the required CommandCode
        // Use the PluginIdentifier, since this should be unique
        expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
        expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER);
        expression.append(iteratedplugin.getIdentifier());
        expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);
        expression.append("/");
        expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
        expression.append(strIteratedCommandCode);
        expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

        LOGGER.debug(debug,
                     SOURCE + " Find Iterated Command [xpath=" + expression.toString() + "]");

        // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
        selection = instrument.getController().selectPath(expression.toString());

        // The Plugin CommandCode should be unique, but if not, take the first
        if ((selection != null)
            && (selection instanceof CommandType[])
            && (selection.length >= 1)
            && (selection[0] != null)
            && (selection[0] instanceof CommandType))
            {
            // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
            cmdIterated = (CommandType)selection[0].copy();

            LOGGER.debug(debug,
                         SOURCE + "Iterated Command [commandcode=" + cmdIterated.getCommandCode()
                         + "] [commandvariant=" + cmdIterated.getCommandVariant()
                         + "] [id=" + cmdIterated.getIdentifier() + "]");
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "Iterated Command NOT FOUND [xpath=" + expression.toString() + "]");
            LOGGER.debug(debug,
                         SOURCE + "(selection != null) " + (selection != null));
            LOGGER.debug(debug,
                         SOURCE + "(selection instanceof CommandType[]) " + (selection instanceof CommandType[]));

            if (selection != null)
                {
                LOGGER.debug(debug,
                             SOURCE + "(selection.length == 1) " + (selection.length == 1));
                LOGGER.debug(debug,
                             SOURCE + "selection.length " + selection.length);

                if (selection instanceof CommandType[])
                    {
                    LOGGER.debug(debug,
                                 SOURCE + "(selection[0] != null) " + (selection[0] != null));
                    LOGGER.debug(debug,
                                 SOURCE + "(selection[0] instanceof CommandType) " + (selection[0] instanceof CommandType));
                    }
                }
            }

        return (cmdIterated);
        }
    }
