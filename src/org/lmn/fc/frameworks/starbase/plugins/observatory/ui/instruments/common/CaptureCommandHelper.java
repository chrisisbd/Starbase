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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import org.apache.xmlbeans.XmlObject;
import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusParsers;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.*;


/***************************************************************************************************
 * CaptureCommandHelper.
 * All utilities for Capture Commands only.
 */

public final class CaptureCommandHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   ObservatoryConstants
    {
    // String Resources
    public static final String MSG_PERIOD_CONTINUOUS = "continuous";


    /***********************************************************************************************
     * doIteratedStaribusMultichannelDataCaptureCommand(), used by DataCapture.captureRawDataRealtime().
     * Process Command:IteratedDataCommand from instruments.xsd.
     * There are a minimum of five Command parameters: the iteration interval, the iteration period,
     * the data filter, realtime updates and verbose logging control.
     * CapturePeriod = 0 means run continuously.
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doIteratedStaribusMultichannelDataCaptureCommand(final ObservatoryInstrumentDAOInterface dao,
                                                                                            final CommandMessageInterface commandmessage,
                                                                                            final List<Metadata> metadatalist,
                                                                                            final String source,
                                                                                            final boolean notifyport)
        {
        final String SOURCE = "CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand() ";
        final int PARAMETER_COUNT_MIN = 5;
        final int INDEX_INTERVAL = 0;
        final int INDEX_PERIOD = 1;
        final int INDEX_FILTER = 2;
        final PluginType pluginIteratorModule;
        final CommandType cmdDoIterator;
        final List<Metadata> listCopiedPluginMetadata;
        final List<ParameterType> listExecutionParameters;
        final List<String> errors;
        final StringBuffer bufferFirstTimestamp;
        final DataFilterType dataFilterType;
        final List<DataTypeDictionary> listChannelDataTypes;
        final List<String> listTemperatureChannelFlag;
        final int intCaptureChannelCount;
        final TimeSeriesCollection collectionTimeSeries;
        final ResponseMessageInterface responseMessage;
        boolean boolVerboseLogging;
        final boolean boolDebug;

        boolVerboseLogging = false;
        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Initialise
        bufferFirstTimestamp = new StringBuffer(100);
        listChannelDataTypes = new ArrayList<DataTypeDictionary>(10);
        listTemperatureChannelFlag = new ArrayList<String>(1);
        errors = new ArrayList<String>(10);

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

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        pluginIteratorModule = (PluginType) commandmessage.getModule().copy();
        cmdDoIterator = (CommandType)commandmessage.getCommandType().copy();
        listCopiedPluginMetadata = MetadataHelper.getCopyOfPluginMetadataList(pluginIteratorModule);

//        MetadataHelper.showMetadataList(listCopiedPluginMetadata,
//                                        "Capture Plugin",
//                                        boolDebug);

        // We expect a minimum of five parameters: the iteration interval, the iteration period,
        // the data filter, realtime updates  and verbose logging
        listExecutionParameters = commandmessage.getExecutionParameters();

        // Validate the Metadata from the IteratorModule
        // and find the number of channels to process, with their DataTypes
        // The ConfigurationList is not modified
        intCaptureChannelCount = DataAnalyser.getCaptureChannelCount(listCopiedPluginMetadata,
                                                                     FrameworkStrings.EMPTY_STRING,
                                                                     listChannelDataTypes,
                                                                     listTemperatureChannelFlag);
        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        dao.establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdDoIterator),
                                           intCaptureChannelCount,
                                           DataAnalyser.hasTemperatureChannelInList(listTemperatureChannelFlag),
                                           listCopiedPluginMetadata,
                                           metadatalist);
        dao.setRawData(new Vector<Object>(1000));

        // Do the doIteratedStaribusMultichannelDataCaptureCommand() operation, which expects a Response...
        // Allow any number of channels
        if ((intCaptureChannelCount >= 1)
            && (commandmessage.getInstrument() != null)
            && (commandmessage.getInstrument().getController() != null)
            && (cmdDoIterator.getResponse() != null)
            && (cmdDoIterator.getIteratedDataCommand() != null)
            && (cmdDoIterator.getIteratedDataCommand().getIteratedCommandCodeBase() != null)
            && (cmdDoIterator.getIteratedDataCommand().getIteratedCommandCode() != null))
            {
            final Instrument xmlInstrument;
            final String strIteratedCommandCodeBase;
            final String strIteratedCommandCode;
            PluginType pluginIterated;
            CommandType cmdIterated;
            final StringBuffer expression;
            XmlObject[] selection;

            xmlInstrument = commandmessage.getInstrument();

            // Retrieve the Command to execute
            strIteratedCommandCodeBase = cmdDoIterator.getIteratedDataCommand().getIteratedCommandCodeBase();
            strIteratedCommandCode = cmdDoIterator.getIteratedDataCommand().getIteratedCommandCode();

            // Find the Plugin:Command to which these codes relate
            pluginIterated = null;
            cmdIterated = null;
            expression = new StringBuffer();

            // The CommandCodeBase:CommandCode identifies the specific Command
            // The XML holds these values as two-character Hex numbers
            // First find the Module containing the CommandCodeBase
            expression.setLength(0);
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_CCB);
            expression.append(strIteratedCommandCodeBase);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            //LOGGER.debugTimedEvent(debug,
            //             SOURCE + "IteratorPlugin [commandcodebase=" + pluginIteratorModule.getCommandCodeBase()
            //                + "] [id=" + pluginIteratorModule.getIdentifier() + "]");
            //
            //LOGGER.debugTimedEvent(debug,
            //             SOURCE + "IteratorCommand [commandcode=" + cmdDoIterator.getCommandCode()
            //                + "] [id=" + cmdDoIterator.getIdentifier() + "]");
            //
            //LOGGER.debugTimedEvent(debug,
            //             SOURCE + "IteratedCommand [commandcodebase=" + strIteratedCommandCodeBase
            //                + "] [commandcode=" + strIteratedCommandCode + "]");
            //
            //LOGGER.debugTimedEvent(debug,
            //             SOURCE + "Find Iterated Plugin [xpath=" + expression.toString() + "]");

            // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
            selection = commandmessage.getInstrument().getController().selectPath(expression.toString());

            // The Plugin CommandCodeBase should be unique, but if not, take the first
            if ((selection != null)
                && (selection instanceof PluginType[])
                && (selection.length >= 1)
                && (selection[0] != null)
                && (selection[0] instanceof PluginType))
                {
                // Don't affect the Plugin in the Instrument XML
                pluginIterated = (PluginType)selection[0].copy();

//                LOGGER.debugTimedEvent(debug,
//                             SOURCE + "Iterated Plugin [commandcodebase=" + pluginIterated.getCommandCodeBase()
//                                + "] [id=" + pluginIterated.getIdentifier() + "]");
//
//                LOGGER.debugTimedEvent(debug,
//                             SOURCE + "Iterated Plugin=[" + pluginIterated.xmlText() + "]");
                }
            else
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "Iterated Plugin NOT FOUND [commandcodebase=" + strIteratedCommandCodeBase
                                            + "] [xpath=" + expression.toString() + "]");
                }

            if (pluginIterated != null)
                {
                // Now search in the Plugin to find the Command with the required CommandCode
                // Use the PluginIdentifier, since this should be unique
                expression.setLength(0);
                expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER);
                expression.append(pluginIterated.getIdentifier());
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);
                expression.append("/");
                expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
                expression.append(strIteratedCommandCode);
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

//                LOGGER.debugTimedEvent(debug,
//                             SOURCE + " Find Iterated Command [xpath=" + expression.toString() + "]");

                // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
                selection = commandmessage.getInstrument().getController().selectPath(expression.toString());

                // The Plugin CommandCode should be unique, but if not, take the first
                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length >= 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                    cmdIterated = (CommandType)selection[0].copy();

//                    LOGGER.debugTimedEvent(debug,
//                                 SOURCE + "Iterated Command [commandcode=" + cmdIterated.getCommandCode()
//                                   + "] [commandvariant=" + cmdIterated.getCommandVariant()
//                                   + "] [id=" + cmdIterated.getIdentifier() + "]");
                    }
                else
                    {
                    LOGGER.debugTimedEvent(boolDebug,
                                 SOURCE + "Iterated Command NOT FOUND [xpath=" + expression.toString() + "]");
                    LOGGER.debugTimedEvent(boolDebug,
                                 SOURCE + "(selection != null) " + (selection != null));
                    LOGGER.debugTimedEvent(boolDebug,
                                 SOURCE + "(selection instanceof CommandType[]) " + (selection instanceof CommandType[]));

                    if (selection != null)
                        {
                        LOGGER.debugTimedEvent(boolDebug,
                                     SOURCE + "(selection.length == 1) " + (selection.length == 1));
                        LOGGER.debugTimedEvent(boolDebug,
                                 SOURCE + "selection.length " + selection.length);

                        if (selection instanceof CommandType[])
                            {
                            LOGGER.debugTimedEvent(boolDebug,
                                         SOURCE + "(selection[0] != null) " + (selection[0] != null));
                            LOGGER.debugTimedEvent(boolDebug,
                                         SOURCE + "(selection[0] instanceof CommandType) " + (selection[0] instanceof CommandType));
                            }
                        }
                    }
                }

            //----------------------------------------------------------------------------------
            // There are a minimum of five Command parameters: the iteration interval, the iteration period,
            // the data filter, realtime updates and verbose logging control.
            // We can only check as far as the Filter, because there may be a variable number of Parameters
            // Check that the sub-Command correctly refers to this Command as its parent,
            // and that the sub-Command has no Parameters, then execute the sub-Command
            // It doesn't matter if the Command is executed on a Port, or locally

            if ((dao.getHostInstrument() != null)

                // Check the ExecutionParameters as far as the Filter
                && (listExecutionParameters != null)
                && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
                && (listExecutionParameters.get(INDEX_INTERVAL) != null)
                && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_INTERVAL).getInputDataType().getDataTypeName()))
                && (listExecutionParameters.get(INDEX_PERIOD) != null)
                && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_PERIOD).getInputDataType().getDataTypeName()))
                && (listExecutionParameters.get(INDEX_FILTER) != null)
                && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName()))

                // We can only check as far as the Filter, because there may be a variable number of Parameters following
                //&& (listParameters.get(3) != null)
                //&& (SchemaDataType.BOOLEAN.equals(listParameters.get(3).getInputDataType().getDataTypeName()))
                //&& (listParameters.get(4) != null)
                //&& (SchemaDataType.BOOLEAN.equals(listParameters.get(4).getInputDataType().getDataTypeName()))

                // Child to Parent links
                // There's no need to check the CommandVariant
                && (cmdIterated != null)
                && (pluginIteratorModule != null)
                && (cmdIterated.getIteratedDataCommand() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCodeBase() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCode() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCodeBase().equals(pluginIteratorModule.getCommandCodeBase()))
                && (cmdIterated.getIteratedDataCommand().getParentCommandCode().equals(cmdDoIterator.getCommandCode()))

                // This is a command specific to Staribus
                && (cmdIterated.getResponse() != null)
                && (SchemaDataType.STARIBUS_MULTICHANNEL_DATA.toString().equals(cmdIterated.getResponse().getDataTypeName().toString()))

                // Parent to Child links
                // There's no need to check the CommandVariant
                && (pluginIterated != null)
                && (cmdDoIterator.getIteratedDataCommand().getIteratedCommandCodeBase().equals(pluginIterated.getCommandCodeBase()))
                && (cmdDoIterator.getIteratedDataCommand().getIteratedCommandCode().equals(cmdIterated.getCommandCode())))
                {
                try
                    {
                    final String strSampleInterval;
                    final String strCapturePeriod;
                    final int intSampleIntervalSec;
                    int intCapturePeriodSec;
                    final String strFilter;
                    final DataTypeDictionary typeIteratedResponse;

                    // RealtimeUpdate updates the RawData, ProcessedData and Chart on every sample read
                    // VerboseLogging allows us to turn off the individual logging of each sub-command

                    strSampleInterval = listExecutionParameters.get(INDEX_INTERVAL).getValue();
                    strCapturePeriod = listExecutionParameters.get(INDEX_PERIOD).getValue();
                    strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                    intSampleIntervalSec = Integer.parseInt(strSampleInterval);
                    intCapturePeriodSec = Integer.parseInt(strCapturePeriod);

                    // Find the DataType of the IteratedCommand Response
                    typeIteratedResponse = DataTypeDictionary.getDataTypeDictionaryEntryForName(cmdIterated.getResponse().getDataTypeName().toString());

                    // Map the filter entry to a FilterType (which must return not NULL)
                    dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                    // Check for silly parameter settings
                    // CapturePeriod = 0 means run continuously
                    if ((intSampleIntervalSec > 0)
                        && (intCapturePeriodSec >= 0)
                        && (dataFilterType != null)
                        && (typeIteratedResponse != null))
                        {
                        final boolean boolRealtimeUpdate;
                        final int intCaptureCountMax;
                        final ResponseMessageStatusList listStatusInCaptureLoop;
                        final String strStarscript;
                        final DataFilterInterface filter;
                        final String strPeriod;
                        boolean boolCollectAggregateMetadata;
                        boolean boolSuccess;

                        if (intCapturePeriodSec == 0)
                            {
                            strPeriod = MSG_PERIOD_CONTINUOUS;
                            }
                        else
                            {
                            strPeriod = Integer.toString(intCapturePeriodSec);
                            }

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_RAWDATA
                                                               + METADATA_ACTION_CAPTURE
                                                               + METADATA_INTERVAL + intSampleIntervalSec + TERMINATOR + FrameworkStrings.SPACE
                                                               + METADATA_PERIOD + strPeriod + TERMINATOR + FrameworkStrings.SPACE
                                                               + METADATA_STARSCRIPT + pluginIterated.getIdentifier() + FrameworkStrings.DOT + cmdIterated.getIdentifier() + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());

                        // Instantiate the filter required by the DataFilterType
                        filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                        if (filter != null)
                            {
                            filter.initialiseFilter();
                            DataFilterHelper.applyFilterParameters(filter,
                                                                   commandmessage.getExecutionParameters(),
                                                                   INDEX_FILTER);

                            // All subsequent access to the Filter must be via the DAO
                            dao.setFilter(filter);
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Unable to instantiate the DataFilter [name=" + dataFilterType.getName() + "]");
                            dao.setFilter(null);
                            }

                        // Parse the remaining Parameters now we know the number of Filter Parameters
                        if (listExecutionParameters.size() == (PARAMETER_COUNT_MIN + filter.getParameterCount()))
                            {
                            final int intNextParameterIndex;

                            intNextParameterIndex = INDEX_FILTER + filter.getParameterCount() + 1;
                            boolRealtimeUpdate = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex).getValue());
                            boolVerboseLogging = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex + 1).getValue());
                            }
                        else
                            {
                            boolRealtimeUpdate = false;
                            boolVerboseLogging = false;
                            }

                        // Now execute the IteratedCommand every SampleInterval,
                        // until CapturePeriod has elapsed or until we see an ABORT or TIMEOUT

                        // Correct for continuous operation
                        if (intCapturePeriodSec == 0)
                            {
                            // Allow about 10 days of operation! (Could be Integer.MAX_VALUE?)
                            intCapturePeriodSec = 1000000;
                            }

                        intCaptureCountMax = intCapturePeriodSec / intSampleIntervalSec;
                        boolSuccess = true;

                        // To keep the compiler happy...
                        boolCollectAggregateMetadata = true;

                        // This *assumes* that the Parameters are taken from the Command verbatim
                        strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                               pluginIterated,
                                                                               null,
                                                                               cmdIterated,
                                                                               false);

                        listStatusInCaptureLoop = ResponseMessageStatus.createResponseMessageStatusList();

                        for (int intCaptureIndex = 0;
                             ((intCaptureIndex < intCaptureCountMax)
                                && (boolSuccess)
                                && (Utilities.executeWorkerCanProceed(dao)));
                             intCaptureIndex++)
                            {
                            ResponseMessageInterface responseData;
                            boolean boolSuccessfulSubCommand;
                            final long longTimeStart;
                            final long longTimeFinish;
                            final long longTimeToWait;

                            longTimeStart = dao.getObservatoryClock().getSystemTimeMillis();

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
                                TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                                listStatusInCaptureLoop.clear();

                                // Reset the DAO timeout on every retry to the Timeout for IteratedPlugin.iteratedCommand()
                                TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(pluginIterated, cmdIterated));

                                // This command will set off the *Queue* wait loop,
                                // which only has to deal with a single command, usually getRealtimeData()
                                // If we get TIMEOUT, try again until retries exhausted
                                // All ResponseMessages come back with a new DAOWrapper
                                // The latest Parameter values are added to the ResponseMessage DAOWrapper ObservationMetadata,
                                // which points to the DAO ObservationMetadata
                                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                                responseData = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                               dao,
                                                                                               xmlInstrument,
                                                                                               pluginIterated,
                                                                                               cmdIterated,
                                                                                               // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                               cmdIterated.getParameterList(),
                                                                                               strStarscript,
                                                                                               errors,
                                                                                               notifyport,
                                                                                               boolVerboseLogging);
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
                                    DAOCommandHelper.logResponseBlock(dao,
                                                                      commandmessage,
                                                                      responseData,
                                                                      intCaptureIndex,
                                                                      retryid);
                                    }
                                }

                            // The IteratedCommand must be completed without error or timeout
                            // Repeat the full logical test for simplicity
                            // WARNING!! responseData must return with non-null DAOWrapper
                            // since we need to parse the ResponseValue into channel data
                            if ((responseData != null)
                                && (ResponseMessageStatus.wasResponseSuccessful(responseData))
                                && (responseData.getWrappedData() != null)
                                && (responseData.getWrappedData().getResponseValue() != null))
                                {
                                final Vector vecData;
                                final List<Object> listChannelData;

                                // Ensure everything points to the same thing....
                                dao.setRawDataChanged(true);
                                dao.setProcessedDataChanged(true);
                                dao.setWrappedData(responseData.getWrappedData());

//                                MetadataHelper.showWrapperMetadata(responseData.getWrappedData(),
//                                                                   SOURCE + "Response Wrapper returned from Command Execution",
//                                                                   boolDebug);

                                // Parse and append the data from the IteratedCommand ResponseValue
                                // The Response DataType must be StaribusMultichannelData,
                                // so it has already been checked for syntax by the ResponseParser
                                listChannelData = StaribusParsers.parseStaribusMultichannelDataIntoList(new StringBuffer(responseData.getWrappedData().getResponseValue()),
                                                                                                        ObservatoryConstants.STARIBUS_RESPONSE_SEPARATOR_REGEX,
                                                                                                        listChannelDataTypes,
                                                                                                        intCaptureChannelCount,
                                                                                                        errors);
                                // Is this the first data item?
                                // If so, timestamp it for use later in the Data.Timestamp ResponseValue
                                if (bufferFirstTimestamp.length() == 0)
                                    {
                                    bufferFirstTimestamp.append(ChronosHelper.toDateString(dao.getObservatoryClock().getCalendarDateNow()));
                                    bufferFirstTimestamp.append(FrameworkStrings.SPACE);
                                    bufferFirstTimestamp.append(ChronosHelper.toTimeString(dao.getObservatoryClock().getCalendarTimeNow()));
                                    }

                                // Did we successfully parse all channels?
                                // Make sure we have an ObservatoryClock
                                if ((listChannelData != null)
                                    && (listChannelData.size() == intCaptureChannelCount)
                                    && (dao.getObservatoryClock() != null)
                                    && (dao.getObservatoryClock().getAstronomicalCalendar() != null)
                                    && (dao.getObservatoryClock().getAstronomicalCalendar().getCalendar() != null))
                                    {
                                    // The data output must be one Calendar and DiscoveredChannelCount Numerics (DataTypes known to the filters)
                                    vecData = new Vector(intCaptureChannelCount + 1);

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
                                    LOGGER.error(SOURCE + "Unable to parse all channels of a StaribusMultichannelData Response");

                                    if ((dao.getObservatoryClock() == null)
                                        || (dao.getObservatoryClock().getAstronomicalCalendar() == null)
                                        || (dao.getObservatoryClock().getAstronomicalCalendar().getCalendar() == null))
                                        {
                                        // There is no Observatory Clock
                                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                           EventStatus.FATAL,
                                                                           METADATA_TARGET_RAWDATA
                                                                               + METADATA_ACTION_CAPTURE
                                                                               + METADATA_ERROR + MSG_NO_CLOCK + TERMINATOR,
                                                                           dao.getLocalHostname(),
                                                                           dao.getObservatoryClock());

                                        listStatusInCaptureLoop.add(ResponseMessageStatus.INVALID_PARAMETER);
                                        boolSuccess = false;
                                        }
                                    }

                                //------------------------------------------------------------------
                                // Do we need to update now?

                                if ((boolSuccess)
                                    && (boolRealtimeUpdate))
                                    {
//                                    LOGGER.debugTimedEvent(boolDebug,
//                                                           SOURCE + "RealTime update [boolCollectAggregateMetadata=" + boolCollectAggregateMetadata + "]");

                                    // Filter the DAO RawData, to produce the XYDataset
                                    // The filter will be aware of the sample DataTypes
                                    // The supplied Metadata MUST contain the Observation.Channel.Name
                                    DataFilterHelper.filterCapturedCalendarisedMultichannelRawDataToTimeSeries(dao,
                                                                                                               listChannelDataTypes,
                                                                                                               listCopiedPluginMetadata,
                                                                                                               collectionTimeSeries,
                                                                                                               boolRealtimeUpdate,
                                                                                                               boolVerboseLogging,
                                                                                                               source);

                                    // Add the most recent sample Values to the ObservationMetadata, for all channels
                                    MetadataHelper.addLastTimestampedValuesToAllChannels(dao);

//                                    MetadataHelper.showDAOMetadata(dao,
//                                                                   SOURCE + "DAO Metadata after adding last Values",
//                                                                   boolDebug);

//                                    MetadataHelper.showWrapperMetadata(dao.getWrappedData(),
//                                                                       SOURCE + "DAO Wrapper Metadata after adding last Values - is this the same??",
//                                                                       boolDebug);

                                    // Keep re-applying the updated DAO Wrapper to the host Instrument,
                                    // to ensure that RawData Reports get updated
                                    // Charts etc. are updated on different Threads, so should not slow this one down...
                                    // Should dao.getWrappedData() do the wrap??
                                    dao.setRawDataChanged(true);
                                    dao.setProcessedDataChanged(true);

                                    // Only refresh the data if visible (otherwise it isn't a realtime update!)
                                    dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                                                           false,
                                                                           boolCollectAggregateMetadata);

                                    // Don't collect the AggregateMetadata more than once
                                    // Values should be updated because they are referenced from the Composite collection

//                                    MetadataHelper.showMetadataList(dao.getHostInstrument().getAggregateMetadata(),
//                                                                    SOURCE + "AggregateMetadata *after* Wrapper set on Instrument [boolCollectAggregateMetadata" + boolCollectAggregateMetadata + "]",
//                                                                    boolDebug);

                                    // Now clear the Log fragments,
                                    // since these were added to the Instrument Logs by the above
                                    dao.getEventLogFragment().clear();
                                    dao.getInstrumentLogFragment().clear();

                                    // Remove the reference to the DAO data in the ResponseMessage!
                                    // This is VERY IMPORTANT - otherwise each message holds on to each dataset...
                                    responseData.setWrappedData(null);
                                    }

                                //------------------------------------------------------------------

                                // This might be the last time through, so say it is successful
                                if (boolSuccess)
                                    {
                                    listStatusInCaptureLoop.add(ResponseMessageStatus.SUCCESS);
                                    }

                                // Take account of the time elapsed so far...
                                longTimeFinish = dao.getObservatoryClock().getSystemTimeMillis();
                                longTimeToWait = (intSampleIntervalSec * ChronosHelper.SECOND_MILLISECONDS)
                                                    - (longTimeFinish - longTimeStart);
                                if (longTimeToWait >= 0)
                                    {
                                    // Wait for the required time, but only if we succeeded
                                    Utilities.safeSleepPollExecuteWorker(longTimeToWait, dao);
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Invalid sample interval [time_to_wait=" + longTimeToWait + " msec]");
                                    }
                                }
                            else
                                {
                                // Terminate the loop immediately on failure, since the Retry gave up
                                LOGGER.error(SOURCE + "Failed after retries");

                                boolSuccess = false;

                                if ((responseData != null)
                                    && (responseData.getResponseMessageStatusList() != null)
                                    && (!responseData.getResponseMessageStatusList().isEmpty()))
                                    {
                                    // Handle multiple errors
                                    listStatusInCaptureLoop.addAll(responseData.getResponseMessageStatusList());
                                    }
                                else
                                    {
                                    // We don't know what happened...
                                    listStatusInCaptureLoop.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                    }
                                }

                            // Tidy up after every Command, just in case
                            if (responseData != null)
                                {
                                // Keep the GC happy...
                                // This is VERY IMPORTANT - otherwise each message holds on to each dataset...
                                responseData.setWrappedData(null);
                                }
                            }

                        // Capture the final Status and ResponseValue (these may show a failure)
                        dao.getResponseMessageStatusList().addAll(listStatusInCaptureLoop);
                        }
                    else
                        {
                        throw new IllegalArgumentException(FrameworkStrings.EXCEPTION_PARAMETER_INVALID);
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_EXCEPTION
                                                            + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                            + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (IllegalArgumentException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_EXCEPTION
                                                            + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                            + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (Exception exception)
                    {
                    exception.printStackTrace();
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_EXCEPTION
                                                            + dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]"
                                                            + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                    }
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
        // Put the Timeout back to what it should be for a single default command.

        // TODO REVIEW TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

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
                                                                                       boolVerboseLogging,
                                                                                       source);

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
                                               METADATA_EXCEPTION
                                                    + ObservatoryInstrumentDAOInterface.ERROR_DATA_FORMAT
                                                    + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        //------------------------------------------------------------------------------------------
        // Tidy up

        if (dao.getFilter() != null)
            {
            dao.getFilter().disposeFilter();
            }

        dao.setFilter(null);
        dao.setTranslator(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // We'd like to keep any data after ABORT!

        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Explicitly set the ResponseValue as the Timestamp of the first data item, to save space
            cmdDoIterator.getResponse().setValue(bufferFirstTimestamp.toString());

            // Keep re-applying the updated DAO Wrapper to the host Instrument,
            // to ensure that RawData Reports get updated
            dao.setRawDataChanged(true);
            dao.setProcessedDataChanged(true);

            // Only refresh the data if visible
            dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                                   false,
                                                   false);

            // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdDoIterator,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdDoIterator));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdDoIterator,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdDoIterator));
             }

        return (responseMessage);
        }


    /***********************************************************************************************
     * doIteratedDataCaptureCommand().
     * Process Command:IteratedDataCommand from instruments.xsd.
     * There is a minimum of five Command parameters: the iteration interval, the iteration period,
     * the data filter, realtime updates and verbose logging control.
     * Currently used only in SpectraCyberDAO and SpectraCyberClientDAO captureContinuum().
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doIteratedDataCaptureCommand(final ObservatoryInstrumentDAOInterface dao,
                                                                        final CommandMessageInterface commandmessage,
                                                                        final List<Metadata> metadatalist,
                                                                        final String source,
                                                                        final boolean notifyport)
        {
        final String SOURCE = "CaptureCommandHelper.doIteratedDataCaptureCommand() ";
        final int PARAMETER_COUNT_MIN = 5;
        final int INDEX_INTERVAL = 0;
        final int INDEX_PERIOD = 1;
        final int INDEX_FILTER = 2;
        final int WRAPPER_CHANNEL_COUNT = 1;
        final PluginType pluginIteratorModule;
        final CommandType cmdDoIteratedData;
        final List<ParameterType> listExecutionParameters;
        final ResponseMessageInterface responseMessage;
        final List<String> errors;
        final StringBuffer bufferFirstTimestamp;
        final DataFilterType dataFilterType;
        final List<DataTypeDictionary> listChannelDataTypes;
        final List<String> listTemperatureChannelFlag;
        final int intCaptureChannelCount;
        final List<Metadata> listCopiedPluginMetadata;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Initialise
        bufferFirstTimestamp = new StringBuffer(100);
        listChannelDataTypes = new ArrayList<DataTypeDictionary>(10);
        listTemperatureChannelFlag = new ArrayList<String>(1);
        errors = new ArrayList<String>(10);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdDoIteratedData = (CommandType)commandmessage.getCommandType().copy();
        pluginIteratorModule = (PluginType)commandmessage.getModule().copy();
        listCopiedPluginMetadata = MetadataHelper.getCopyOfPluginMetadataList(pluginIteratorModule);

        // We expect a minimum of five parameters: the iteration interval, the iteration period,
        // the data filter, realtime updates  and verbose logging
        listExecutionParameters = commandmessage.getExecutionParameters();

        // Validate the Metadata from the IteratorModule
        // and find the number of channels to process, with their DataTypes
        // The ConfigurationList is not modified, so no need to copy()
        intCaptureChannelCount = DataAnalyser.getCaptureChannelCount(listCopiedPluginMetadata,
                                                                     EMPTY_STRING,
                                                                     listChannelDataTypes,
                                                                     listTemperatureChannelFlag);
        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        dao.establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdDoIteratedData),
                                           intCaptureChannelCount,
                                           DataAnalyser.hasTemperatureChannelInList(listTemperatureChannelFlag),
                                           listCopiedPluginMetadata,
                                           metadatalist);

        // Do the doIteratedDataCaptureCommand() operation, which expects a Response...
        // Allow any number of channels, but force the DAOWrapper to wrap only the first
        if ((intCaptureChannelCount >= WRAPPER_CHANNEL_COUNT)
            && (commandmessage.getInstrument() != null)
            && (commandmessage.getInstrument().getController() != null)
            && (cmdDoIteratedData.getResponse() != null)
            && (cmdDoIteratedData.getIteratedDataCommand() != null)
            && (cmdDoIteratedData.getIteratedDataCommand().getIteratedCommandCodeBase() != null)
            && (cmdDoIteratedData.getIteratedDataCommand().getIteratedCommandCode() != null))
            {
            final Instrument xmlInstrument;
            final String strIteratedCommandCodeBase;
            final String strIteratedCommandCode;
            PluginType pluginIterated;
            CommandType cmdIterated;
            final StringBuffer expression;
            XmlObject[] selection;

            xmlInstrument = commandmessage.getInstrument();

            // Retrieve the Command to execute
            strIteratedCommandCodeBase = cmdDoIteratedData.getIteratedDataCommand().getIteratedCommandCodeBase();
            strIteratedCommandCode = cmdDoIteratedData.getIteratedDataCommand().getIteratedCommandCode();

//            LOGGER.debugTimedEvent(boolDebug, SOURCE + " IteratedCommand [commandcodebase=" + strIteratedCommandCodeBase
//                                            + "] [commandcode=" + strIteratedCommandCode + "]");

            // Find the Plugin:Command to which this code relates
            pluginIterated = null;
            cmdIterated = null;
            expression = new StringBuffer();

            // The CommandCodeBase:CommandCode identifies the specific Command
            // The XML holds these values as two-character Hex numbers
            // First find the Module containing the CommandCodeBase
            expression.setLength(0);
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_CCB);
            expression.append(strIteratedCommandCodeBase);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
            selection = commandmessage.getInstrument().getController().selectPath(expression.toString());

            // The CommandCodeBase should be unique
            if ((selection != null)
                && (selection instanceof PluginType[])
                && (selection.length == 1)
                && (selection[0] != null)
                && (selection[0] instanceof PluginType))
                {
                //LOGGER.debugTimedEvent(boolDebug, SOURCE + " IteratedCommand PluginType=" + selection[0].xmlText());

                // Don't affect the Plugin in the Instrument XML
                pluginIterated = (PluginType)selection[0].copy();
                }
//            else
//                {
//                LOGGER.debugTimedEvent(boolDebug, SOURCE + " IteratedCommand PluginType NOT FOUND");
//                }

            if (pluginIterated != null)
                {
                // Now search in the Plugin to find the Command with the requiredCommandCode
                // Use the PluginIdentifier, since this should be unique
                expression.setLength(0);
                expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER);
                expression.append(pluginIterated.getIdentifier());
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);
                expression.append("/");
                expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
                expression.append(strIteratedCommandCode);
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

                // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
                selection = commandmessage.getInstrument().getController().selectPath(expression.toString());

                // The CommandCodeBase should be unique
                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length == 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    //LOGGER.debugTimedEvent(boolDebug, SOURCE + " IteratedCommand CommandType=" + selection[0].xmlText());

                    // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                    cmdIterated = (CommandType)selection[0].copy();
                    }
//                else
//                    {
//                    LOGGER.debugTimedEvent(boolDebug, SOURCE + " IteratedCommand CommandType NOT FOUND");
//                    }
                }

            //----------------------------------------------------------------------------------
            // There are five Command parameters: the iteration interval, the iteration period,
            // the data filter, realtime updates and verbose logging control.
            // We can only check as far as the Filter, because there may be a variable number of Parameters
            // Check that the sub-Command correctly refers to this Command as its parent,
            // and that the sub-Command has no Parameters, then execute the sub-Command
            // It doesn't matter if the Command is executed on a Port, or locally

            if ((dao.getHostInstrument() != null)

                // Check the ExecutionParameters as far as the Filter
                && (listExecutionParameters != null)
                && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
                && (listExecutionParameters.get(INDEX_INTERVAL) != null)
                && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_INTERVAL).getInputDataType().getDataTypeName()))
                && (listExecutionParameters.get(INDEX_PERIOD) != null)
                && (SchemaDataType.DECIMAL_INTEGER.equals(listExecutionParameters.get(INDEX_PERIOD).getInputDataType().getDataTypeName()))
                && (listExecutionParameters.get(INDEX_FILTER) != null)
                && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName()))

                // We can only check as far as the Filter, because there may be a variable number of Parameters following
                //&& (listParameters.get(3) != null)
                //&& (SchemaDataType.BOOLEAN.equals(listParameters.get(3).getInputDataType().getDataTypeName()))
                //&& (listParameters.get(4) != null)
                //&& (SchemaDataType.BOOLEAN.equals(listParameters.get(4).getInputDataType().getDataTypeName()))

                // Child to Parent links
                // There's no need to check the CommandVariant
                && (cmdIterated != null)
                && (pluginIteratorModule != null)
                && (cmdIterated.getIteratedDataCommand() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCodeBase() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCode() != null)
                && (cmdIterated.getIteratedDataCommand().getParentCommandCodeBase().equals(pluginIteratorModule.getCommandCodeBase()))
                && (cmdIterated.getIteratedDataCommand().getParentCommandCode().equals(cmdDoIteratedData.getCommandCode()))
                && (cmdIterated.getResponse() != null)

                // Parent to Child links
                // There's no need to check the CommandVariant
                && (pluginIterated != null)
                && (cmdDoIteratedData.getIteratedDataCommand().getIteratedCommandCodeBase().equals(pluginIterated.getCommandCodeBase()))
                && (cmdDoIteratedData.getIteratedDataCommand().getIteratedCommandCode().equals(cmdIterated.getCommandCode())))
                {
                try
                    {
                    final String strIterationInterval;
                    final String strIterationPeriod;
                    final int intIterationIntervalSec;
                    int intIterationPeriodSec;
                    final String strFilter;
                    final DataTypeDictionary typeIteratedResponse;

                    // RealtimeUpdate updates the RawData, ProcessedData and Chart on every sample read
                    // VerboseLogging allows us to turn off the individual logging of each sub-command

                    strIterationInterval = listExecutionParameters.get(INDEX_INTERVAL).getValue();
                    strIterationPeriod = listExecutionParameters.get(INDEX_PERIOD).getValue();
                    strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                    intIterationIntervalSec = Integer.parseInt(strIterationInterval);
                    intIterationPeriodSec = Integer.parseInt(strIterationPeriod);

                    // Check for continuous operation request
                    if (intIterationPeriodSec == 0)
                        {
                        intIterationPeriodSec = Integer.MAX_VALUE;
                        }

                    // Find the DataType of the IteratedCommand Response
                    typeIteratedResponse = DataTypeDictionary.getDataTypeDictionaryEntryForName(cmdIterated.getResponse().getDataTypeName().toString());

                    // Map the filter entry to a FilterType (which must return not NULL)
                    dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                    // Check for silly parameter settings
                    // Only look for Numeric Responses from the IteratedCommand
                    if ((intIterationIntervalSec > 0)
                        && (intIterationPeriodSec >= intIterationIntervalSec)
                        && (dataFilterType != null)
                        && (typeIteratedResponse != null)
                        && (typeIteratedResponse.isNumeric()))
                        {
                        final boolean boolRealtimeUpdate;
                        final boolean boolVerboseLogging;
                        final int intCounter;
                        final ResponseMessageStatusList listStatusInIterationLoop;
                        boolean boolSuccess;
                        final String strStarscript;
                        final TimeZone timeZone;
                        final Locale locale;
                        final DataFilterInterface filter;
                        boolean boolCollectAggregateMetadata;

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_CAPTURE
                                                           + METADATA_INTERVAL + intIterationIntervalSec + TERMINATOR + FrameworkStrings.SPACE
                                                           + METADATA_PERIOD + intIterationPeriodSec + TERMINATOR + FrameworkStrings.SPACE
                                                           + METADATA_STARSCRIPT + pluginIterated.getIdentifier() + FrameworkStrings.DOT + cmdIterated.getIdentifier() + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());

                        // Instantiate the filter required by the DataFilterType
                        filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                        if (filter != null)
                            {
                            filter.initialiseFilter();
                            DataFilterHelper.applyFilterParameters(filter,
                                                                   commandmessage.getExecutionParameters(),
                                                                   INDEX_FILTER);

                            // All subsequent access to the Filter must be via the DAO
                            dao.setFilter(filter);
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Unable to instantiate the DataFilter [name=" + dataFilterType.getName() + "]");
                            dao.setFilter(null);
                            }

                        // Parse the remaining Parameters now we know the number of Filter Parameters
                        if (listExecutionParameters.size() == (PARAMETER_COUNT_MIN + filter.getParameterCount()))
                            {
                            final int intNextParameterIndex;

                            intNextParameterIndex = INDEX_FILTER + filter.getParameterCount() + 1;
                            boolRealtimeUpdate = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex).getValue());
                            boolVerboseLogging = Boolean.parseBoolean(listExecutionParameters.get(intNextParameterIndex + 1).getValue());
                            }
                        else
                            {
                            boolRealtimeUpdate = false;
                            boolVerboseLogging = false;
                            }

                        // Make the assumption that observations are synchronised to the Observatory clock
                        // and that Time is in UT. Do this only once...
                        timeZone = TimeZone.getTimeZone(FrameworkConstants.DEFAULT_TIME_ZONE_ID);
                        locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                            REGISTRY.getFramework().getCountryISOCode());

                        // Now execute the IteratedCommand every IterationInterval,
                        // until IterationPeriod has elapsed or until we see an ABORT or TIMEOUT
                        intCounter = intIterationPeriodSec / intIterationIntervalSec;
                        boolSuccess = true;

                        // To keep the compiler happy...
                        boolCollectAggregateMetadata = true;

                        // This *assumes* that the Parameters are taken from the Command verbatim
                        strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                               pluginIterated,
                                                                               null,
                                                                               cmdIterated,
                                                                               false);

                        listStatusInIterationLoop = ResponseMessageStatus.createResponseMessageStatusList();

                        for (int commandid = 0;
                             (commandid < intCounter)
                                && (boolSuccess)
                                && (Utilities.executeWorkerCanProceed(dao));
                             commandid++)
                            {
                            ResponseMessageInterface responseData;
                            boolean boolSuccessfulSubCommand;
                            final long longTimeStart;
                            final long longTimeFinish;
                            final long longTimeToWait;

                            //LOGGER.debugTimedEvent(boolDebug, SOURCE + " Calling IteratedCommand");

                            longTimeStart = dao.getObservatoryClock().getSystemTimeMillis();

                            // Allow any number of channels, but force the DAOWrapper to wrap only the first
//                            dao.setRawDataChannelCount(WRAPPER_CHANNEL_COUNT);
//                            dao.setTemperatureChannel(DataAnalyser.hasTemperatureChannelInList(listTemperatureChannelFlag));

                            dao.getResponseMessageStatusList().clear();
                            responseData =  null;
                            boolSuccessfulSubCommand = false;

                            // Attempt the IteratedCommand again if it fails
                            // This will eat into the Timeout time, but if it fails many times, we want to know
                            for (int retryid = 0;
                                ((retryid < TimeoutHelper.RETRY_COUNT)
                                    && (!boolSuccessfulSubCommand)
                                    && (Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker())));
                                retryid++)
                                {
                                TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                                listStatusInIterationLoop.clear();

                                // Reset the DAO timeout on every retry to the Timeout for IteratedPlugin.iteratedCommand()
                                TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(pluginIterated, cmdIterated));

                                // This command will set off the *Queue* wait loop,
                                // which only has to deal with a single command
                                // Only the ResponseValue is populated
                                // If we get TIMEOUT, try again until retries exhausted
                                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                                responseData = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                               dao,
                                                                                               xmlInstrument,
                                                                                               pluginIterated,
                                                                                               cmdIterated,
                                                                                               // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                               cmdIterated.getParameterList(),
                                                                                               strStarscript,
                                                                                               errors,
                                                                                               notifyport,
                                                                                               boolVerboseLogging);
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
                                        intStatus = responseData.getStatusBits();
                                        }
                                    else
                                        {
                                        intStatus = 0;
                                        }

                                    LOGGER.debugTimedEvent(boolDebug,
                                                           SOURCE + " End of RETRY [retry=" + retryid + "] [status=" + Utilities.intToBitString(
                                                                intStatus) + "]");
                                    DAOCommandHelper.logResponseBlock(dao,
                                                                      commandmessage,
                                                                      responseData,
                                                                      commandid,
                                                                      retryid);
                                    }
                                }

                            // The Command must be completed without error or timeout
                            // Repeat the full logical test for simplicity
                            // WARNING!! responseData must return with non-null DAOWrapper
                            // since we need the ResponseValue
                            if ((responseData != null)
                                && (ResponseMessageStatus.wasResponseSuccessful(responseData))
                                && (responseData.getWrappedData() != null)
                                && (responseData.getWrappedData().getResponseValue() != null))
                                {
                                final Number number;
                                final Vector vecData;

                                // Parse and append the data from the IteratedCommand ResponseValue
                                number = DataTypeHelper.parseNumberFromValueField(responseData.getWrappedData().getResponseValue(),
                                                                                  typeIteratedResponse,
                                                                                  cmdIterated.getResponse().getName(),
                                                                                  cmdIterated.getResponse().getRegex(),
                                                                                  errors);
                                // Is this the first data item?
                                // If so, timestamp it for use later in the Data.Timestamp ResponseValue
                                if (bufferFirstTimestamp.length() == 0)
                                    {
                                    bufferFirstTimestamp.append(ChronosHelper.toDateString(dao.getObservatoryClock().getCalendarDateNow()));
                                    bufferFirstTimestamp.append(FrameworkStrings.SPACE);
                                    bufferFirstTimestamp.append(ChronosHelper.toTimeString(dao.getObservatoryClock().getCalendarTimeNow()));
                                    }

                                // The data output must be one Calendar and one Numeric (known to the filters)
                                vecData = new Vector(2);
                                vecData.add(dao.getObservatoryClock().getSystemCalendar(timeZone, locale));

                                if (number != null)
                                    {
                                    vecData.add(number);
                                    }
                                else
                                    {
                                    // Ensure we see something sensible on error
                                    vecData.add(0);
                                    }

                                // Accumulate the data we have collected into the RawData of this DAO
                                // which is accessible via the DAOWrapper
                                dao.getRawData().add(vecData);
                                dao.setUnsavedData(true);

                                //------------------------------------------------------------------
                                // Do we need to update on every sample?

                                if (boolRealtimeUpdate)
                                    {
                                    //LOGGER.debugTimedEvent(boolDebug, SOURCE + " RealTime update");

                                    // Filter the DAO RawData, to produce the XYDataset
                                    // The filter will be aware of the sample DataTypes
                                    // The supplied Metadata MUST contain the Observation.Channel.Name
                                    DataFilterHelper.filterCapturedCalendarisedRawDataToTimeSeries(dao,
                                                                                                   listChannelDataTypes,
                                                                                                   listCopiedPluginMetadata,
                                                                                                   boolRealtimeUpdate,
                                                                                                   boolVerboseLogging,
                                                                                                   source);

                                    // Modify the sub-command DAOWrapper in the Response to use the latest RawData
                                    // NOTE - executeCommandOnSameThread() sets RawData etc. to NULL
                                    // so we just overwrite with the accumulated data

                                    // MetadataMetadata and Metadata are already set in the DAOWrapper

//                                    responseData.getWrappedData().setRawDataMetadata(dao.getRawDataMetadata());
//                                    responseData.getWrappedData().setRawData(dao.getRawData());
//
//                                    responseData.getWrappedData().setXYDatasetMetadata(dao.getXYDatasetMetadata());
//                                    responseData.getWrappedData().setXYDataset(dao.getXYDataset());
//
//                                    responseData.getWrappedData().setImageData(dao.getImageData());

                                    // Update all the Channel Values, for Indicators etc.
//                                    for (int intChannelIndex = 0;
//                                         intChannelIndex < WRAPPER_CHANNEL_COUNT;
//                                         intChannelIndex++)
//                                        {
//                                        final String strChannelName;
//
//                                        // Get the channel names from the RawDataMetadata
//                                        strChannelName = MetadataHelper.getChannelName(dao.getRawDataMetadata(),
//                                                                                       intChannelIndex,
//                                                                                       dao.hasTemperatureChannel());
//                                        MetadataHelper.addLastTimestampedValueToMetadata(dao.getObservationMetadata(),
//                                                                                         dao.getRawData(),
//                                                                                         intChannelIndex,
//                                                                                         dao.hasTemperatureChannel(),
//                                                                                         MetadataHelper.getChannelDataType(dao.getRawDataMetadata(),
//                                                                                                                           intChannelIndex,
//                                                                                                                           dao.hasTemperatureChannel()),
//                                                                                         MetadataHelper.getChannelUnits(dao.getRawDataMetadata(),
//                                                                                                                        intChannelIndex,
//                                                                                                                        dao.hasTemperatureChannel()),
//                                                                                         strChannelName);
//                                        }
//
                                    // Add the most recent sample Values to the ObservationMetadata, for all channels
                                    MetadataHelper.addLastTimestampedValuesToAllChannels(dao);

                                    // Keep re-applying the updated DAO Wrapper to the host Instrument,
                                    // to ensure that RawData Reports get updated
                                    // Charts etc. are updated on different Threads,
                                    // so should not slow this one down...
                                    dao.setRawDataChanged(true);
                                    dao.setProcessedDataChanged(true);

                                    // Only refresh the data if visible (otherwise it isn't a realtime update!)
                                    dao.getHostInstrument().setWrappedData(responseData.getWrappedData(),
                                                                           false,
                                                                           boolCollectAggregateMetadata);

                                    // Don't collect the AggregateMetadata more than once
                                    boolCollectAggregateMetadata = false;

                                    // Now clear the Log fragments,
                                    // since these were added to the Instrument Logs by the above
                                    dao.getEventLogFragment().clear();
                                    dao.getInstrumentLogFragment().clear();

                                    // Keep the GC happy...
                                    responseData.setWrappedData(null);
                                    }

                                //------------------------------------------------------------------

                                // This might be the last time through, so say it is successful
                                listStatusInIterationLoop.add(ResponseMessageStatus.SUCCESS);

                                // Take account of the time elapsed so far...
                                longTimeFinish = dao.getObservatoryClock().getSystemTimeMillis();
                                longTimeToWait = (intIterationIntervalSec * ChronosHelper.SECOND_MILLISECONDS)
                                                    - (longTimeFinish - longTimeStart);
                                if (longTimeToWait > 0)
                                    {
                                    // Wait for the required time, but only if we succeeded
                                    Utilities.safeSleepPollExecuteWorker(longTimeToWait, dao);
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + " Invalid sample interval [time_to_wait=" + longTimeToWait + " msec]");
                                    }
                                }
                            else
                                {
                                // Terminate the loop immediately on failure, since the Retry gave up
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + " Failed after retries");

                                boolSuccess = false;

                                if ((responseData != null)
                                    && (responseData.getResponseMessageStatusList() != null)
                                    && (!responseData.getResponseMessageStatusList().isEmpty()))
                                    {
                                    // Handle multiple errors
                                    listStatusInIterationLoop.addAll(responseData.getResponseMessageStatusList());
                                    }
                                else
                                    {
                                    // We don't know what happened...
                                    listStatusInIterationLoop.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                    }
                                }
                            }

                        // Capture the final Status and ResponseValue (these may show a failure)
                        dao.getResponseMessageStatusList().addAll(listStatusInIterationLoop);
                        }
                    else
                        {
                        throw new IllegalArgumentException(FrameworkStrings.EXCEPTION_PARAMETER_INVALID);
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_EXCEPTION
                                                            + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                            + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (IllegalArgumentException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_EXCEPTION
                                                            + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                            + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (Exception exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_EXCEPTION
                                                            + dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]"
                                                            + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                    }
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
            // Something is wrong with the XML definition of doIteratedDataCaptureCommand()
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_RAWDATA,
                                                                                     METADATA_ACTION_CAPTURE));
            }

        //------------------------------------------------------------------------------------------
        // Put the Timeout back to what it should be for a single default command.

        // TODO REVIEW TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

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
            // This version filters only one channel, the first
            // The supplied Metadata MUST contain the Observation.Channel.Name
            // The DAO must hold the Filter!
            DataFilterHelper.filterCapturedCalendarisedRawDataToTimeSeries(dao,
                                                                           listChannelDataTypes,
                                                                           listCopiedPluginMetadata,
                                                                           true,
                                                                           true,
                                                                           source);

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
                                               METADATA_EXCEPTION
                                                    + ObservatoryInstrumentDAOInterface.ERROR_DATA_FORMAT
                                                    + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        //------------------------------------------------------------------------------------------
        // Tidy up

        if (dao.getFilter() != null)
            {
            dao.getFilter().disposeFilter();
            }

        dao.setFilter(null);
        dao.setTranslator(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // We'd like to keep any data after ABORT!

        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Explicitly set the ResponseValue as the Timestamp of the first data item, to save space
            cmdDoIteratedData.getResponse().setValue(bufferFirstTimestamp.toString());

            // Keep re-applying the updated DAO Wrapper to the host Instrument,
            // to ensure that RawData Reports get updated
            dao.setRawDataChanged(true);
            dao.setProcessedDataChanged(true);

            // Only refresh the data if visible
            dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                                   false,
                                                   false);

            // Create the ResponseMessage - this creates a DAOWrapper containing the data
            // Allow any number of channels, but force the DAOWrapper to wrap only the first
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdDoIteratedData,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdDoIteratedData));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdDoIteratedData,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdDoIteratedData));
             }

        return (responseMessage);
        }


    /***********************************************************************************************
     * doSteppedDataCaptureCommand().
     * Process Command:SteppedDataCommand from instruments.xsd.
     * There must be at least five User Parameters supplied: the starting value, the end value,
     * the step size, realtime updates and verbose logging control.
     * This produces columnar data, i.e. indexed, not timestamped.
     * The starting value is used to drive the first command in the list.
     * The list must contain at least two Commands, the 'set up' and the 'get data'.
     * The last Command must return a numeric value.
     * MetadataX is updated by the set() step, MetadataY is updated by the get() step.
     * Currently used in SpectraCyberDAO and SpectraCyberClientDAO, captureSpectrum().
     *
     * @param dao
     * @param commandmessage
     * @param metadatalist
     * @param metadatax
     * @param metadatay
     * @param parameteroffset
     * @param userparametercount
     * @param commandparametercount
     * @param source
     * @param notifyport
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSteppedDataCaptureCommand(final ObservatoryInstrumentDAOInterface dao,
                                                                       final CommandMessageInterface commandmessage,
                                                                       final List<Metadata> metadatalist,
                                                                       final Metadata metadatax,
                                                                       final Metadata metadatay,
                                                                       final int parameteroffset,
                                                                       final int userparametercount,
                                                                       final int commandparametercount,
                                                                       final String source,
                                                                       final boolean notifyport)
        {
        final String SOURCE = "CaptureCommandHelper.doSteppedDataCaptureCommand()";
        final int PARAMETER_COUNT_MIN = 5;
        final int WRAPPER_CHANNEL_COUNT = 1;
        final PluginType pluginSteppingModule;
        final CommandType cmdDoSteppedData;
        final List<ParameterType> listAllParameters;
        final ResponseMessageInterface responseMessage;
        final List<String> errors;
        final StringBuffer bufferTimestamp;
        final List<DataTypeDictionary> listChannelDataTypes;
        final List<String> listTemperatureChannelFlag;
        final int intCaptureChannelCount;
        final List<Metadata> listCopiedPluginMetadata;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Initialise
        bufferTimestamp = new StringBuffer(100);
        listChannelDataTypes = new ArrayList<DataTypeDictionary>(10);
        listTemperatureChannelFlag = new ArrayList<String>(1);
        errors = new ArrayList<String>(10);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        pluginSteppingModule = (PluginType) commandmessage.getModule().copy();
        cmdDoSteppedData = (CommandType)commandmessage.getCommandType().copy();
        listCopiedPluginMetadata = MetadataHelper.getCopyOfPluginMetadataList(pluginSteppingModule);

        // There are seven Command parameters counting from the parameteroffset
        // The starting value, the end value, the step size and the wait time are passed to commands
        // The datafilter, realtime updates and verbose logging control are used outside the loop
        listAllParameters = cmdDoSteppedData.getParameterList();

        // Validate the Metadata from the SteppingModule (DataCapture)
        // and find the number of channels to process, with their DataTypes
        // The ConfigurationList is not modified, so no need to copy()
        intCaptureChannelCount = DataAnalyser.getCaptureChannelCount(listCopiedPluginMetadata,
                                                                     FrameworkStrings.EMPTY_STRING,
                                                                     listChannelDataTypes,
                                                                     listTemperatureChannelFlag);
        // Establish the identity of this Instrument using Metadata
        // from the Framework, Observatory and Observer
        dao.establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdDoSteppedData),
                                           intCaptureChannelCount,
                                           DataAnalyser.hasTemperatureChannelInList(listTemperatureChannelFlag),
                                           listCopiedPluginMetadata,
                                           metadatalist);
        dao.setRawData(new Vector<Object>(1000));

        // Do the doSteppedDataCaptureCommand() operation, which expects a Response...
        // No Temperature channel is allowed!
        // There must be at least two SteppedCommands, the 'set up' and the 'get data'
        // Usually there will be a third, an intermediate wait()
        // There must be at least six Parameters, the start, end and step values
        // the filter, realtimeupdates and verboselogging
        // We must have only one channel, the Value
        // Allow any number of channels, but force the DAOWrapper to wrap only the first
        if ((dao.getHostInstrument() != null)
            && (intCaptureChannelCount >= WRAPPER_CHANNEL_COUNT)
            && (commandmessage.getInstrument() != null)
            && (commandmessage.getInstrument().getController() != null)
            && (cmdDoSteppedData.getResponse() != null)
            && (cmdDoSteppedData.getSteppedDataCommandList() != null)
            && (cmdDoSteppedData.getSteppedDataCommandList().size() >= 2)
            && (parameteroffset >= 0)
            && (listAllParameters != null)
            && (listAllParameters.size() >= (parameteroffset + PARAMETER_COUNT_MIN)))
            {
            final Instrument xmlInstrument;
            final List<PluginType> listSteppedPlugin;
            final List<CommandType> listSteppedCommand;
            final List<ParameterType> listMacroParameters;

            xmlInstrument = commandmessage.getInstrument();

            listSteppedPlugin = findSteppedPlugins(commandmessage,
                                                   pluginSteppingModule,
                                                   cmdDoSteppedData,
                                                   boolDebug);

            listSteppedCommand = findSteppedCommands(commandmessage,
                                                     pluginSteppingModule,
                                                     cmdDoSteppedData,
                                                     listSteppedPlugin,
                                                     boolDebug);

            listMacroParameters = collectSteppedMacroParameters(listAllParameters,
                                                                parameteroffset,
                                                                PARAMETER_COUNT_MIN,
                                                                boolDebug);

            // Check that the Command list is consistent and correct
            if (validateSteppedCommandList(listSteppedPlugin,
                                           listSteppedCommand,
                                           listMacroParameters,
                                           commandparametercount,
                                           boolDebug))
                {
                // Prepare a Timestamp for use later in the (successful) Data.Timestamp ResponseValue
                bufferTimestamp.append(ChronosHelper.toDateString(dao.getObservatoryClock().getCalendarDateNow()));
                bufferTimestamp.append(FrameworkStrings.SPACE);
                bufferTimestamp.append(ChronosHelper.toTimeString(dao.getObservatoryClock().getCalendarTimeNow()));

                // Run the macro!
                dao.getResponseMessageStatusList().addAll(executeSteps(dao,
                                                                          commandmessage,
                                                                          xmlInstrument,
                                                                          pluginSteppingModule,
                                                                          cmdDoSteppedData,
                                                                          listSteppedPlugin,
                                                                          listSteppedCommand,
                                                                          parameteroffset,
                                                                          listAllParameters,
                                                                          listMacroParameters,
                                                                          listChannelDataTypes,
                                                                          listCopiedPluginMetadata,
                                                                          metadatax,
                                                                          metadatay,
                                                                          intCaptureChannelCount,
                                                                          notifyport,
                                                                          errors,
                                                                          boolDebug));
                }
            else
                {
                // Something is wrong with the XML definition of doSteppedDataCaptureCommand()
                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                         SOURCE,
                                                                                         METADATA_TARGET_RAWDATA,
                                                                                         METADATA_ACTION_CAPTURE));
                }
            }
        else
            {
            // Something is wrong with the XML definition of doSteppedDataCaptureCommand()
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_RAWDATA,
                                                                                     METADATA_ACTION_CAPTURE));
            }

        //------------------------------------------------------------------------------------------
        // Put the Timeout back to what it should be for a single default command.

        // TODO REVIEW TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

        //------------------------------------------------------------------------------------------
        // Did we succeed in getting valid data?
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // We'd like to keep any data after ABORT!

        if (((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            && (dao.getRawData() != null))
            {
            // Finally produce the complete XYDataset, with no filtering
            // Always log this step, irrespective of parameter settings
            dao.setRawDataChannelCount(intCaptureChannelCount);
            dao.setTemperatureChannel(DataAnalyser.hasTemperatureChannelInList(listTemperatureChannelFlag));

            // Copy all Channels
            // The supplied Metadata MUST contain the Observation.Channel.Name
            DataFilterHelper.copyColumnarRawDataToXYDataset(dao,
                                                            listChannelDataTypes,
                                                            listCopiedPluginMetadata,
                                                            true,
                                                            source);

            // Add the most recent sample Values to the ObservationMetadata, for all channels
            MetadataHelper.addLastColumnarValuesToAllChannels(dao);

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
                                               METADATA_EXCEPTION
                                                    + ObservatoryInstrumentDAOInterface.ERROR_DATA_FORMAT
                                                    + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        //------------------------------------------------------------------------------------------
        // Tidy up (these should never have been set)

        dao.setFilter(null);
        dao.setTranslator(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

       //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // We'd like to keep any data after ABORT!

        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
             || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Explicitly set the ResponseValue as the Timestamp of the first data item, to save space
            cmdDoSteppedData.getResponse().setValue(bufferTimestamp.toString());

            // Keep re-applying the updated DAO Wrapper to the host Instrument,
            // to ensure that RawData Reports get updated
            dao.setRawDataChanged(true);
            dao.setProcessedDataChanged(true);

            // Only refresh the data if visible
            dao.getHostInstrument().setWrappedData(dao.getWrappedData(),
                                                   false,
                                                   false);
            dao.setUnsavedData(true);

            // Create the ResponseMessage - this creates a DAOWrapper containing the data
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdDoSteppedData,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdDoSteppedData));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdDoSteppedData,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdDoSteppedData));
             }

        return (responseMessage);
        }


    /**********************************************************************************************/
    /* Stepped Command Utilities                                                                  */
    /***********************************************************************************************
     * Find the Plugins containing the Commands to be Stepped.
     *
     * @param commandmessage
     * @param steppingmodule
     * @param steppingcommand
     * @param debug
     *
     * @return List<PluginType>
     */

    private static List<PluginType> findSteppedPlugins(final CommandMessageInterface commandmessage,
                                                       final PluginType steppingmodule,
                                                       final CommandType steppingcommand,
                                                       final boolean debug)
        {
        final String SOURCE = "CaptureCommandHelper.findSteppedPlugins() ";
        final ListIterator<SteppedCodes> iterSteppedCommands;
        final List<PluginType> listPlugins;
        final StringBuffer expression;

        iterSteppedCommands = steppingcommand.getSteppedDataCommandList().listIterator();
        listPlugins = new ArrayList<PluginType>(10);
        expression = new StringBuffer();

        while (iterSteppedCommands.hasNext())
            {
            final SteppedCodes steppedCodes;
            final String strSteppedCommandCodeBase;
            final XmlObject[] selection;
            final PluginType pluginStepped;

            steppedCodes = iterSteppedCommands.next();

            // Retrieve the CommandCodeBase in order to find the host Plugin
            strSteppedCommandCodeBase = steppedCodes.getSteppedCommandCodeBase();

            LOGGER.debugTimedEvent(debug,
                                   SOURCE + "Search for Plugin [commandcodebase=" + strSteppedCommandCodeBase + "]");

            // The XML holds these values as two-character Hex numbers
            // Find the Module containing the CommandCodeBase
            expression.setLength(0);
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_CCB);
            expression.append(strSteppedCommandCodeBase);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            // Query from the root of the Instrument's Controller,
            // since the CommandCodeBase could be in any Plugin - but not in the Controller.Core
            selection = commandmessage.getInstrument().getController().selectPath(expression.toString());

            // The CommandCodeBase should be unique, i.e. only one selection is returned
            if ((selection != null)
                && (selection instanceof PluginType[])
                && (selection.length == 1)
                && (selection[0] != null)
                && (selection[0] instanceof PluginType))
                {
                // Don't affect the Plugin in the Instrument XML
                pluginStepped = (PluginType)selection[0].copy();
                listPlugins.add(pluginStepped);

                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "[plugin=" + pluginStepped.getIdentifier() + "] [commandcodebase=" + strSteppedCommandCodeBase + "]");
                }
            else
                {
                LOGGER.error(SOURCE + " SteppedCommand PluginType NOT FOUND");
                }
            }

        return (listPlugins);
        }


    /***********************************************************************************************
     * Find the Commands to be Stepped by the SteppingPlugin.
     * Supply the list of Plugins already discovered to contain the Commands.
     *
     * @param commandmessage
     * @param steppingplugin
     * @param steppingcommand
     * @param plugins
     * @param debug
     *
     * @return List<CommandType>
     */

    private static List<CommandType> findSteppedCommands(final CommandMessageInterface commandmessage,
                                                         final PluginType steppingplugin,
                                                         final CommandType steppingcommand,
                                                         final List<PluginType> plugins,
                                                         final boolean debug)
        {
        final String SOURCE = "CaptureCommandHelper.findSteppedCommands() ";
        final List<CommandType> listCommands;

        listCommands = new ArrayList<CommandType>(10);

        if ((plugins != null)
            && (!plugins.isEmpty()))
            {
            final ListIterator<SteppedCodes> iterSteppedCommands;
            final StringBuffer expression;
            int intCounter;

            iterSteppedCommands = steppingcommand.getSteppedDataCommandList().listIterator();
            expression = new StringBuffer();
            intCounter = 0;

            while ((iterSteppedCommands.hasNext())
                && (plugins.size() > intCounter))
                {
                final SteppedCodes steppedCodes;
                final String strSteppedCommandCode;
                final PluginType pluginStepped;
                final XmlObject[] selection;
                final CommandType cmdStepped;

                pluginStepped = plugins.get(intCounter);
                steppedCodes = iterSteppedCommands.next();

                // Retrieve the CommandCode
                strSteppedCommandCode = steppedCodes.getSteppedCommandCode();

                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "[plugin=" + pluginStepped.getIdentifier()
                                            + "] [commandcodebase=" + pluginStepped.getCommandCodeBase()
                                            + "] [commandcode=" + strSteppedCommandCode + "]");

                // Now search in the Plugin to find the Command with the requiredCommandCode
                // Use the PluginIdentifier, since this should be unique
                expression.setLength(0);
                expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER);
                expression.append(pluginStepped.getIdentifier());
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);
                expression.append("/");
                expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
                expression.append(strSteppedCommandCode);
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

                // Query from the root of the Instrument's Controller, since the CommandCodeBase could be in any Plugin
                selection = commandmessage.getInstrument().getController().selectPath(expression.toString());

                // The CommandCodeBase should be unique, i.e. only one result is returned
                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length == 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                    cmdStepped = (CommandType)selection[0].copy();

                    LOGGER.debugTimedEvent(debug,
                                           SOURCE + "[plugin=" + pluginStepped.getIdentifier()
                                                + "] [commandcodebase=" + pluginStepped.getCommandCodeBase()
                                                + "] [command=" + cmdStepped.getIdentifier()
                                                + "] [commandcode=" + strSteppedCommandCode + "]");

                    // Check links between the parent SteppingCommand and each SteppedCommand
                    // ToDo WARNING! ASSUMPTION - the SteppedCommand has only one entry in the list!
                    // The reverse links have already been checked,
                    // because traversing them resulted in the stepping set of Plugins and Commands
                    // There's no need to check the CommandVariant
                    if ((cmdStepped.getSteppedDataCommandList() != null)
                        && (cmdStepped.getSteppedDataCommandList().size() == 1)
                        && (cmdStepped.getSteppedDataCommandList().get(0).getParentCommandCodeBase() != null)
                        && (cmdStepped.getSteppedDataCommandList().get(0).getParentCommandCodeBase().equals(steppingplugin.getCommandCodeBase()))
                        && (cmdStepped.getSteppedDataCommandList().get(0).getParentCommandCode() != null)
                        && (cmdStepped.getSteppedDataCommandList().get(0).getParentCommandCode().equals(steppingcommand.getCommandCode())))
                        {
                        listCommands.add(cmdStepped);
                        // If nothing was added, the list sizes will be incorrect later...
                        }
                    else
                        {
                        LOGGER.error(SOURCE + " SteppedCommand Invalid Parent-Child links");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + " SteppedCommand CommandType NOT FOUND");
                    }

                // Move on to the next Plugin
                intCounter++;
                }
            }

        return (listCommands);
        }


    /***********************************************************************************************
     * Collect the Parameters to be passed to the SteppedCommand macro.
     *
     * @param parameters
     * @param parameteroffset
     * @param parameter_count_min
     * @param debug
     *
     * @return List<ParameterType>
     */

    private static List<ParameterType> collectSteppedMacroParameters(final List<ParameterType> parameters,
                                                                     final int parameteroffset,
                                                                     final int parameter_count_min,
                                                                     final boolean debug)
        {
        final String SOURCE = "CaptureCommandHelper.collectSteppedMacroParameters() ";
        final int INDEX_INITIAL_VALUE = 0;
        final int INDEX_ADDITIONAL_PARAMS = 3;
        final List<ParameterType> listMacroParameters;

        listMacroParameters = new ArrayList<ParameterType>(10);

        // Collect the macro Parameters to be passed to the SteppedCommands
        // The first Parameter (at parameteroffset) is the initial value, and must be collected.
        // The second and third are the final value and step size,
        // which are used outside the SteppedCommands.

        // Any further Parameters are collected, except for the last two...
        // Y N N ? ? ... N N

        // Always collect the first Parameter at the parameteroffset, the initial value
        if (parameters.size() > (parameteroffset + INDEX_INITIAL_VALUE))
            {
            listMacroParameters.add(parameters.get(parameteroffset + INDEX_INITIAL_VALUE));
            LOGGER.debugTimedEvent(debug,
                                   SOURCE + "[parameter.step[" + (parameteroffset + INDEX_INITIAL_VALUE - 1)
                                    + "]=" + parameters.get(parameteroffset + INDEX_INITIAL_VALUE).getName() + "]");
            }

        // Are there any additional Parameters before the final two?
        // If so, they will begin at index (parameteroffset + 3)
        if (parameters.size() >= (parameteroffset + parameter_count_min))
            {
            for (int intAdditionalIndex = 0;
                 intAdditionalIndex < (parameters.size() - parameteroffset - parameter_count_min);    // TODO NOT SURE ABOUT THIS?
                 intAdditionalIndex++)
                {
                final int intParameterIndex;

                intParameterIndex = parameteroffset + INDEX_ADDITIONAL_PARAMS + intAdditionalIndex;

                listMacroParameters.add(parameters.get(intParameterIndex));
                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "[parameter.step[" + (intParameterIndex - 1)
                                            + "]=" + parameters.get(intParameterIndex).getName() + "]");
                }
            }

        return (listMacroParameters);
        }


    /***********************************************************************************************
     * Check that the Plugins and Commands derived from the SteppedDataCommand configuration
     * represent a consistent set.
     * The two Lists must be of the same, non-zero, length.
     * The number and DataType of Parameters supplied must match those expected by the Commands.
     * Only use the first parametercount entries in the Parameters list.
     * The last Command must return a single numeric data item in the Response.
     *
     * @param steppedplugins
     * @param steppedcommands
     * @param macroparameters
     * @param parametercount
     * @param debug
     *
     * @return boolean
     */

    private static boolean validateSteppedCommandList(final List<PluginType> steppedplugins,
                                                      final List<CommandType> steppedcommands,
                                                      final List<ParameterType> macroparameters,
                                                      final int parametercount,
                                                      final boolean debug)
        {
        final String SOURCE = "CaptureCommandHelper.validateSteppedCommandList()";
        boolean boolSuccess;

        for (int i = 0;
             i < steppedplugins.size();
             i++)
            {
            final PluginType pluginType;

            pluginType = steppedplugins.get(i);

            LOGGER.debugTimedEvent(debug,
                                   "SteppedDataCommand: [plugin=" + pluginType.getIdentifier() + "] [index=" + i + "]");
            }

        for (int i = 0;
             i < steppedcommands.size();
             i++)
            {
            final CommandType commandType;

            commandType = steppedcommands.get(i);

            LOGGER.debugTimedEvent(debug,
                                   "SteppedDataCommand: [command=" + commandType.getIdentifier() + "] [index=" + i + "]");
            }

        for (int i = 0;
             i < macroparameters.size();
             i++)
            {
            final ParameterType parameterType;

            parameterType = macroparameters.get(i);

            LOGGER.debugTimedEvent(debug,
                                   "SteppedDataCommand: [parameter=" + parameterType.getName() + "] [index=" + i + "]");
            }

        // The two Lists must be of the same, non-zero, length
        boolSuccess = ((steppedplugins != null)
                        && (steppedcommands != null)
                        && (steppedplugins.size() > 0)
                        && (steppedplugins.size() == steppedcommands.size()));

        LOGGER.debugTimedEvent(debug,
                               SOURCE + "SteppedDataCommand: Checking list lengths [equal=" + boolSuccess + "]");

        if (boolSuccess)
            {
            final Iterator<CommandType> iterSteppedCommands;
            int intMacroParameterIndex;

            // The number and DataType of Parameters supplied must match those expected by the Commands
            iterSteppedCommands = steppedcommands.iterator();
            intMacroParameterIndex = 0;

            while (iterSteppedCommands.hasNext())
                {
                final CommandType cmdStepped;
                final List<ParameterType> listSteppedParameters;

                cmdStepped = iterSteppedCommands.next();

                // Work through the Parameter list of each Command
                listSteppedParameters = cmdStepped.getParameterList();

                // Are there any Parameters for this SteppedCommand?
                if ((listSteppedParameters != null)
                    && (!listSteppedParameters.isEmpty())
                    && (intMacroParameterIndex < macroparameters.size()))
                    {
                    final Iterator<ParameterType> iterParameters;

                    iterParameters = listSteppedParameters.iterator();

                    while (iterParameters.hasNext())
                        {
                        final ParameterType parameterMacro;
                        final ParameterType parameterStepped;

                        parameterMacro = macroparameters.get(intMacroParameterIndex);
                        parameterStepped = iterParameters.next();

                        if ((parameterMacro != null)
                            && (parameterStepped != null)
                            && (parameterMacro.getInputDataType().getDataTypeName().toString().equals(parameterStepped.getInputDataType().getDataTypeName().toString())))
                            {
                            // We have found a valid Parameter assignment
                            intMacroParameterIndex++;
                            LOGGER.debugTimedEvent(debug,
                                                   SOURCE + "SteppedDataCommand: Parameter match [command=" + cmdStepped.getIdentifier()
                                                   + "] [parameter=" + parameterMacro.getName() + "]");
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "SteppedDataCommand: Parameter DataTypes do not match [command=" + cmdStepped.getIdentifier() + "]");
                            }
                        }
                    }
                else
                    {
                    LOGGER.debugTimedEvent(debug,
                                           SOURCE + "SteppedDataCommand: No parameters for this command");
                    }
                }

            // Only use the first parametercount entries in the Parameters list
            boolSuccess = (intMacroParameterIndex == parametercount);

            // Is it still worth checking?
            if (boolSuccess)
                {
                final CommandType cmdExpectNumericResponse;

                // The last Command must return a single numeric data item in the Response
                cmdExpectNumericResponse = steppedcommands.get(steppedcommands.size() - 1);
                boolSuccess = (cmdExpectNumericResponse.getResponse() != null);

                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "SteppedDataCommand: Checking return type of final command [command=" + cmdExpectNumericResponse.getIdentifier()
                                            + "] [hasresponse=" + boolSuccess + "]");

                if (boolSuccess)
                    {
                    final DataTypeDictionary typeResponse;

                    // Find the DataType of the SteppedCommand Response
                    typeResponse = DataTypeDictionary.getDataTypeDictionaryEntryForName(cmdExpectNumericResponse.getResponse().getDataTypeName().toString());

                     // The final Command must return a numeric Response
                    boolSuccess = typeResponse.isNumeric();

                    if (!boolSuccess)
                        {
                        LOGGER.error(SOURCE + "SteppedDataCommand: Final Command in list does not return a numeric");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "SteppedDataCommand: Final Command in list does not return a Response");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "SteppedDataCommand: Parameter list is of incorrect length, or the DataTypes do not match "
                                + "[macro_index=" + intMacroParameterIndex + "] [param_count=" + parametercount + "]");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "SteppedDataCommand: Plugin and Command lists are of incorrect length");
            }

        LOGGER.debugTimedEvent(debug,
                               SOURCE + "SteppedDataCommand: Return flag = " + boolSuccess);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Execute the Steps.
     * We must have three numerics for the start, end and step values at {0, 1, 2}
     * and Boolean, Boolean for RealtimeUpdates, VerboseLogging at the end {end-2, end-1, end}.
     * MetadataX is updated by the set() step, MetadataY is updated by the get() step.
     *
     * @param dao
     * @param commandmessage
     * @param instrument
     * @param steppingplugin
     * @param steppingcommand
     * @param steppedplugins
     * @param steppedcommands
     * @param parameteroffset
     * @param allparameters
     * @param macroparameters
     * @param channeldatatypes
     * @param metadatalist
     * @param metadatax
     * @param metadatay
     * @param channelcount
     * @param notifyport
     * @param errors
     * @param debug
     *
     * @return ResponseMessageStatusList
     */

    private static ResponseMessageStatusList executeSteps(final ObservatoryInstrumentDAOInterface dao,
                                                          final CommandMessageInterface commandmessage,
                                                          final Instrument instrument,
                                                          final PluginType steppingplugin,
                                                          final CommandType steppingcommand,
                                                          final List<PluginType> steppedplugins,
                                                          final List<CommandType> steppedcommands,
                                                          final int parameteroffset,
                                                          final List<ParameterType> allparameters,
                                                          final List<ParameterType> macroparameters,
                                                          final List<DataTypeDictionary> channeldatatypes,
                                                          final List<Metadata> metadatalist,
                                                          final Metadata metadatax,
                                                          final Metadata metadatay,
                                                          final int channelcount,
                                                          final boolean notifyport,
                                                          final List<String> errors,
                                                          final boolean debug)
        {
        final String SOURCE = "CaptureCommandHelper.executeSteps() ";
        final int INDEX_INITIAL_VALUE = 0;
        final int INDEX_FINAL_VALUE = 1;
        final int INDEX_STEP_SIZE = 2;
        final int intLastParameterIndex;
        final ResponseMessageStatusList listResponseMessageStatus;

        // A convenience index to the last Parameter,
        // which is always at the end, regardless of the parameteroffset
        intLastParameterIndex = allparameters.size() - 1;

        listResponseMessageStatus = ResponseMessageStatus.createResponseMessageStatusList();

        // Check the control Parameters
        // We must have three numerics for the start, end and step values at {0, 1, 2}
        // and Boolean, Boolean for RealtimeUpdates, VerboseLogging
        // at the end {end-2, end-1, end}
        // Remember that the first parameter is at parameteroffset, not at the first entry in allparameters
        if ((allparameters.get(INDEX_INITIAL_VALUE + parameteroffset) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(allparameters.get(INDEX_INITIAL_VALUE + parameteroffset).getInputDataType().getDataTypeName()))
            && (allparameters.get(INDEX_FINAL_VALUE + parameteroffset) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(allparameters.get(INDEX_FINAL_VALUE + parameteroffset).getInputDataType().getDataTypeName()))
            && (allparameters.get(INDEX_STEP_SIZE + parameteroffset) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(allparameters.get(INDEX_STEP_SIZE + parameteroffset).getInputDataType().getDataTypeName()))

            // There could be any number of extra Parameters here...

            && (allparameters.get(intLastParameterIndex-1) != null)
            && (SchemaDataType.BOOLEAN.equals(allparameters.get(intLastParameterIndex-1).getInputDataType().getDataTypeName()))
            && (allparameters.get(intLastParameterIndex) != null)
            && (SchemaDataType.BOOLEAN.equals(allparameters.get(intLastParameterIndex).getInputDataType().getDataTypeName()))

            // Units of Initial, Final and Step must be identical!
            && (allparameters.get(INDEX_INITIAL_VALUE + parameteroffset).getUnits().toString().equals(allparameters.get(INDEX_FINAL_VALUE + parameteroffset).getUnits().toString()))
            && (allparameters.get(INDEX_FINAL_VALUE + parameteroffset).getUnits().toString().equals(allparameters.get(INDEX_STEP_SIZE + parameteroffset).getUnits().toString())))
            {
            try
                {
                final String strInitialValue;
                final String strFinalValue;
                final String strStepSize;
                final int intInitialValue;
                final int intFinalValue;
                final int intStepSize;

                final boolean boolRealtimeUpdate;
                final boolean boolVerboseLogging;

                // We now know there are some valid SteppedCommands to execute
                // RealtimeUpdate updates the RawData, ProcessedData and Chart on every sample read
                // VerboseLogging allows us to turn off the individual logging of each sub-command

                strInitialValue = allparameters.get(INDEX_INITIAL_VALUE + parameteroffset).getValue();
                strFinalValue = allparameters.get(INDEX_FINAL_VALUE + parameteroffset).getValue();
                strStepSize = allparameters.get(INDEX_STEP_SIZE + parameteroffset).getValue();

                boolRealtimeUpdate = Boolean.parseBoolean(allparameters.get(intLastParameterIndex-1).getValue());
                boolVerboseLogging = Boolean.parseBoolean(allparameters.get(intLastParameterIndex).getValue());

                // Parse out the values of the Parameters
                intInitialValue = Integer.parseInt(strInitialValue);
                intFinalValue = Integer.parseInt(strFinalValue);
                intStepSize = Integer.parseInt(strStepSize);

                // Check for silly parameter settings (some also checked in Regex)
                if ((intFinalValue > intInitialValue)
                    && (intStepSize > 0))
                    {
                    final int intCommandCount;
                    final ResponseMessageStatusList listStatusInStepLoop;
                    final int intStepCount;
                    int intSteppedValue;

                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_CAPTURE
                                                           + METADATA_INITIAL + intInitialValue + TERMINATOR + FrameworkStrings.SPACE
                                                           + METADATA_FINAL + intFinalValue + TERMINATOR + FrameworkStrings.SPACE
                                                           + METADATA_STEP + intStepSize + TERMINATOR + FrameworkStrings.SPACE
                                                           + METADATA_STARSCRIPT
                                                           // This *assumes* that the Parameters are taken from the Command verbatim
                                                           + StarscriptHelper.buildSimpleStarscript(instrument,
                                                                                                    steppingplugin,
                                                                                                    null,
                                                                                                    steppingcommand,
                                                                                                    false)
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());

                    // Ensure that the Initial and Final values are inclusive
                    intStepCount = ((intFinalValue - intInitialValue) / intStepSize) + 1;

                    // The total number of Commands to be executed is the length of each 'macro' by the number of steps
                    intCommandCount = steppedplugins.size() * intStepCount;

                    intSteppedValue = intInitialValue;

                    // Adjust the Timeout for the number of commands expected
                    // TODO REVIEW TimeoutHelper.restartDAOTimeoutTimer(dao, intCommandCount, 0, 0);

                    listStatusInStepLoop = ResponseMessageStatus.createResponseMessageStatusList();

                    // Perform the required number of steps
                    // Check that the DAO and the execution status are all Ok
                    for (int stepid = 0;
                         ((stepid < intStepCount)
                             && (Utilities.executeWorkerCanProceed(dao))
                             && (ResponseMessageStatus.isResponseStatusOk(listStatusInStepLoop)));
                         stepid++)
                        {
                        LOGGER.debug(debug,
                                     "===========================================================================================");
                        LOGGER.debug(debug,
                                     "Execute macro [macro.stepid=" + stepid + "]");

                        listStatusInStepLoop.addAll(executeSteppedCommands(dao,
                                                                           commandmessage,
                                                                           instrument,
                                                                           steppingplugin,
                                                                           steppingcommand,
                                                                           steppedplugins,
                                                                           steppedcommands,
                                                                           macroparameters,
                                                                           intSteppedValue,
                                                                           channeldatatypes,
                                                                           metadatalist,
                                                                           metadatax,
                                                                           metadatay,
                                                                           intCommandCount,
                                                                           channelcount,
                                                                           notifyport,
                                                                           boolRealtimeUpdate,
                                                                           boolVerboseLogging,
                                                                           errors,
                                                                           debug));

                        // The first Command in the list is the 'setup' which requires a numeric value
                        // which is incremented by the StepSize on each step
                        intSteppedValue += intStepSize;

                        // We know that the DataType must be DecimalInteger
                        macroparameters.get(INDEX_INITIAL_VALUE).setValue(Integer.toString(intSteppedValue));

                        LOGGER.debug(debug,
                                     SOURCE + "Completed Macro [macro.stepid=" + stepid
                                            + "] [macro.step.value=" + intSteppedValue
                                            + "] [status.list.local=" + ResponseMessageStatus.expandResponseStatusCodes(listStatusInStepLoop)
                                            + "] [response_ok.local=" + (ResponseMessageStatus.isResponseStatusOk(listStatusInStepLoop))
                                            + "] [dao.can_proceed=" + Utilities.executeWorkerCanProceed(dao)
                                            + "]\n ");
                        }

                    // Add the most recent sample Values to the ObservationMetadata, for all channels
                    MetadataHelper.addLastColumnarValuesToAllChannels(dao);

                    // Capture the final Status and ResponseValue (these may show a failure)
                    listResponseMessageStatus.addAll(listStatusInStepLoop);
                    }
                else
                    {
                    // Throw an Exception so we can use the logging
                    throw new IllegalArgumentException(FrameworkStrings.EXCEPTION_PARAMETER_INVALID);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                        + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                        + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                listResponseMessageStatus.add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                        + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                        + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                listResponseMessageStatus.add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // Incorrectly configured XML
            listResponseMessageStatus.add(DAOCommandHelper.logInvalidXML(dao,
                                                                         SOURCE,
                                                                         METADATA_TARGET_RAWDATA,
                                                                         METADATA_ACTION_CAPTURE));
            }

        return (listResponseMessageStatus);
        }


    /***********************************************************************************************
     * Execute the individual SteppedCommands representing one 'macro'.
     * MetadataX is updated by the set() step, MetadataY is updated by the get() step.
     * Return the last ResponseMessageStatus.
     * If any command in the macro retries without recovery after TimeoutHelper.RETRY_COUNT,
     * then the macro fails.
     *
     * @param dao
     * @param commandmessage
     * @param instrument
     * @param steppingplugin
     * @param steppingcommand
     * @param steppedplugins
     * @param steppedcommands
     * @param macroparameters
     * @param steppedindex
     * @param channeldatatypes
     * @param metadatalist
     * @param metadatax
     * @param metadatay
     * @param commandcount
     * @param channelcount
     * @param notifyport
     * @param realtimeupdate
     * @param verboselogging
     * @param errors
     * @param debug
     *
     * @return ResponseMessageStatusList
     */

    private static ResponseMessageStatusList executeSteppedCommands(final ObservatoryInstrumentDAOInterface dao,
                                                                    final CommandMessageInterface commandmessage,
                                                                    final Instrument instrument,
                                                                    final PluginType steppingplugin,
                                                                    final CommandType steppingcommand,
                                                                    final List<PluginType> steppedplugins,
                                                                    final List<CommandType> steppedcommands,
                                                                    final List<ParameterType> macroparameters,
                                                                    final int steppedindex,
                                                                    final List<DataTypeDictionary> channeldatatypes,
                                                                    final List<Metadata> metadatalist,
                                                                    final Metadata metadatax,
                                                                    final Metadata metadatay,
                                                                    final int commandcount,
                                                                    final int channelcount,
                                                                    final boolean notifyport,
                                                                    final boolean realtimeupdate,
                                                                    final boolean verboselogging,
                                                                    final List<String> errors,
                                                                    final boolean debug)
        {
        final String SOURCE = "CaptureCommandHelper.executeSteppedCommands() ";
        final ResponseMessageStatusList listStatusInSubStepLoop;
        int intMacroParameterIndex;
        boolean boolSuccessfulSubStep;
        boolean boolUpdateMetadata;

        // ToDo Review use of Metadata
        boolUpdateMetadata = true;

        // Begin to take Parameters from the macro List
        intMacroParameterIndex = 0;

        // Initialise ResponseMessageStatus
        listStatusInSubStepLoop = ResponseMessageStatus.createResponseMessageStatusList();
        boolSuccessfulSubStep = true;

        //-----------------------------------------------------------------------------------------
        // Execute the list of Commands as steps of a 'macro'
        // Check that the User hasn't clicked ABORT, or the Instrument has stopped

        for (int intMacroSubStepIndex = 0;
             (intMacroSubStepIndex < steppedplugins.size())
                 && (boolSuccessfulSubStep)
                 && (Utilities.executeWorkerCanProceed(dao)
                 && (ResponseMessageStatus.isResponseStatusOk(listStatusInSubStepLoop)));
             intMacroSubStepIndex++)
            {
            final PluginType pluginExecute;
            final CommandType cmdExecute;
            final String strStarscript;
            ResponseMessageInterface responseSubStep;

            // Prepare each Plugin.Command for execution
            pluginExecute = steppedplugins.get(intMacroSubStepIndex);
            cmdExecute = steppedcommands.get(intMacroSubStepIndex);

            //-------------------------------------------------------------------------------------
            // Does the Command expect any Parameters?

            if ((cmdExecute.getParameterList() != null)
                && (!cmdExecute.getParameterList().isEmpty()))
                {
                final List<ParameterType> listExecuteParameters;

                // Fill in the Parameter values from the list of supplied Commands
                // until these are all used up...
                listExecuteParameters = cmdExecute.getParameterList();

                // We musn't take more Parameters than are in the list
                for (int intExecuteParameterIndex = 0;
                     (intExecuteParameterIndex < listExecuteParameters.size())
                        && (intMacroParameterIndex < macroparameters.size());
                     intExecuteParameterIndex++)
                    {
                    final ParameterType parameterType;

                    // The Parameter Value has already been parsed and checked
                    parameterType = listExecuteParameters.get(intExecuteParameterIndex);
                    parameterType.setValue(macroparameters.get(intMacroParameterIndex).getValue());

                    // Move to the next macro Parameter
                    intMacroParameterIndex++;
                    }
                }

            // The Command is now prepared for execution
            // This *assumes* that the Parameters are taken from the Command verbatim

            strStarscript = StarscriptHelper.buildSimpleStarscript(instrument,
                                                                   pluginExecute,
                                                                   null,
                                                                   cmdExecute,
                                                                   false);

            // Only the ResponseMessageInterface.ResponseValue is populated
            // We expect ONE channel in the output data (WRAPPER_CHANNEL_COUNT),
            // so tell the returned DAOWrapper
            // Attempt the SteppedCommand again if it fails
            // This will eat into the Timeout time, but if it fails many times, we want to know
            // Check that the User hasn't clicked ABORT, or the Instrument has stopped

            // Make sure we enter the retry loop at least once
            dao.getResponseMessageStatusList().clear();
            boolSuccessfulSubStep = false;
            responseSubStep =  null;

            LOGGER.debug(debug,
                         "Execute macro substep [macro.substep=" + intMacroSubStepIndex + "] [starscript=" + strStarscript + "]");

            for (int intRetrySubStepID = 0;
                ((intRetrySubStepID < TimeoutHelper.RETRY_COUNT)
                    && (!boolSuccessfulSubStep));
                intRetrySubStepID++)
                {
                // This shows the DAOResponseMessageStatusList
                TimeoutHelper.logRetryEvent(dao, strStarscript, intRetrySubStepID);

                LOGGER.debug(debug,
                             SOURCE + "Retry of substep [macro.substep=" + intMacroSubStepIndex + "] [retryid=" + intRetrySubStepID + "]");

                dao.getResponseMessageStatusList().clear();
                listStatusInSubStepLoop.clear();

                // Empty the DAO queues
                // This seems like a bodge, because we don't understand the reasons for queue faults in this context
                if (dao.getPort() != null)
                    {
                    dao.getPort().clearQueues();
                    }

                // Reset the DAO timeout on every retry to the Timeout for SteppedPlugin.steppedCommand()
                TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(pluginExecute, cmdExecute));

                // This could be executing a Local or SendToPort Command
                // If we get TIMEOUT, try again until retries exhausted, remembering to clear the status
                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                // Status appears in the DAO ResponseMessageStatusList

                ResponseMessageStatus.showResponseMessageStatus(responseSubStep,
                                                                dao,
                                                                listStatusInSubStepLoop,
                                                                SOURCE + "Before executeCommandOnSameThread() ",
                                                                debug);

                responseSubStep = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                  dao,
                                                                                  instrument,
                                                                                  pluginExecute,
                                                                                  cmdExecute,
                                                                                  // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                  cmdExecute.getParameterList(),
                                                                                  strStarscript,
                                                                                  errors,
                                                                                  notifyport,
                                                                                  verboselogging);
                // Check for consistency of the status reporting...
                if ((responseSubStep != null)
                    && (ResponseMessageStatus.wasResponseSuccessful(responseSubStep) != ResponseMessageStatus.wasResponseListSuccessful(responseSubStep.getResponseMessageStatusList())))
                    {
                    LOGGER.error(SOURCE + "ResponseMessageStatus: status bits are not consistent with the List of status codes "
                                        + "[response.status=" + ResponseMessageStatus.accumulateResponseStatusBits(responseSubStep.getStatusBits())
                                        + "] [response.codes=" + ResponseMessageStatus.expandResponseStatusCodes(responseSubStep.getResponseMessageStatusList()) + "]");
                    }

                // Are the data ok this time round?
                // This tests the status *bits*
                // Allow only SUCCESS, BUSY and CAPTURE_ACTIVE to represent a success
                boolSuccessfulSubStep = ((responseSubStep != null)
                                            && (ResponseMessageStatus.wasResponseSuccessful(responseSubStep)));

                // Log any failed response blocks
                // If successful, just execute the next step
                if (!boolSuccessfulSubStep)
                    {
                    DAOCommandHelper.logResponseBlock(dao,
                                                      commandmessage,
                                                      responseSubStep,
                                                      intMacroSubStepIndex,
                                                      intRetrySubStepID);
                    }

                ResponseMessageStatus.showResponseMessageStatus(responseSubStep,
                                                                dao,
                                                                listStatusInSubStepLoop,
                                                                SOURCE + "At end of substep retry loop [substep.success=" + boolSuccessfulSubStep + "]",
                                                                debug);
                }

            // All Commands must be completed without error or timeout
            // If we leave the retry loop with SuccessfulSubCommand = false, then retries didn't recover
            if (boolSuccessfulSubStep)
                {
                // execid = 0 is the 'setup'
                // execid = 1 ... (n-1) are the intermediate commands
                // execid = n is the getData() command which must produce a numeric Response

                // Check for the last Command, the getData()
                if (intMacroSubStepIndex == (steppedplugins.size() - 1))
                    {
                    if ((responseSubStep.getWrappedData() != null)
                        && (responseSubStep.getWrappedData().getResponseValue() != null))
                        {
                        final DataTypeDictionary typeIteratedResponse;
                        final Number number;
                        final Vector vecData;

                        // Find the DataType of the *last* SteppedDataCommand Response,
                        // which should be numeric - this will be checked by the parser
                        typeIteratedResponse = DataTypeDictionary.getDataTypeDictionaryEntryForName(cmdExecute.getResponse().getDataTypeName().toString());

                        // Parse and append the data from the SteppedDataCommand ResponseValue
                        number = DataTypeHelper.parseNumberFromValueField(responseSubStep.getWrappedData().getResponseValue(),
                                                                          typeIteratedResponse,
                                                                          cmdExecute.getResponse().getName(),
                                                                          cmdExecute.getResponse().getRegex(),
                                                                          errors);
                        // We know we have *one* data channel for a SteppedDataCommand (WRAPPER_CHANNEL_COUNT)
                        // The data output must be one Numeric for the X-axis SteppedIndex,
                        // and one Numeric for the Channel data Value, known to the Filters
                        vecData = new Vector(2);

                        // The current SteppedIndex is the X-axis
                        vecData.add(steppedindex);

                        if (realtimeupdate)
                            {
                            // Update the X Value indicator
                            metadatax.setValue(Integer.toString(steppedindex));
                            }

                        // The data output is in Channel0
                        if (number != null)
                            {
                            vecData.add(number);

                            if (realtimeupdate)
                                {
                                // Update the Y Value indicator
                                metadatay.setValue(number.toString());
                                }
                            }
                        else
                            {
                            // Ensure we see something sensible on error
                            vecData.add(0);

                            if (realtimeupdate)
                                {
                                metadatay.setValue("Error");
                                }
                            }

                        // Accumulate the data we have collected into the RawData of this DAO,
                        // but only if we can...

                        if (dao.getRawData() != null)
                            {
                            dao.getRawData().add(vecData);
                            }

                        //--------------------------------------------------------------------------
                        // Do we need to update the UI on every sample?

                        if (realtimeupdate)
                            {
                            // Produce the complete XYDataset, with no filtering
                            // The supplied Metadata MUST contain the Observation.Channel.Name
                            DataFilterHelper.copyColumnarRawDataToXYDataset(dao,
                                                                            channeldatatypes,
                                                                            metadatalist,
                                                                            verboselogging,
                                                                            SOURCE);

                            // Modify the sub-command DAOWrapper in the Response to use the latest RawData
                            // NOTE - executeCommandOnSameThread() sets RawData etc. to NULL
                            // so we just overwrite with the accumulated data

                            // Add the most recent sample Values to the ObservationMetadata, for all channels
                            MetadataHelper.addLastColumnarValuesToAllChannels(dao);

                            // Keep re-applying the updated DAO Wrapper to the host Instrument,
                            // to ensure that RawData Reports get updated
                            // Charts etc. are updated on different Threads,
                            // so should not slow this one down...
                            dao.setRawDataChanged(true);
                            dao.setProcessedDataChanged(true);

                            // Only refresh the data if visible
                            dao.getHostInstrument().setWrappedData(responseSubStep.getWrappedData(),
                                                                   false,
                                                                   boolUpdateMetadata);

                            // Don't update the Metadata more than once
                            boolUpdateMetadata = false;

                            // Now clear the Log fragments,
                            // since these were added to the Instrument Logs by the above
                            dao.getEventLogFragment().clear();
                            dao.getInstrumentLogFragment().clear();

                            // Keep the GC happy...
                            responseSubStep.setWrappedData(null);
                            }

                        //--------------------------------------------------------------------------
                         // As far as we know, everything succeeded

                        listStatusInSubStepLoop.add(ResponseMessageStatus.SUCCESS);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + " Final SubStep Command in SteppedDataCommand did not return any data");
                        listStatusInSubStepLoop.add(ResponseMessageStatus.INVALID_COMMAND);
                        }
                    }
                else
                    {
                    // All successful Commands except the last end up here...
                    listStatusInSubStepLoop.add(ResponseMessageStatus.SUCCESS);
                    }
                }
            else
                {
                // Terminate the loop immediately on failure, since the Retry gave up
                // If we leave the retry loop with boolSuccessfulSubCommand = false,
                // then retries didn't recover, so stop executing the macro now

                if ((responseSubStep != null)
                    && (responseSubStep.getResponseMessageStatusList() != null)
                    && (!responseSubStep.getResponseMessageStatusList().isEmpty()))
                    {
                    // Find out what the ResponseMessage told us
                    listStatusInSubStepLoop.addAll(responseSubStep.getResponseMessageStatusList());
                    }
                else if ((dao.getResponseMessageStatusList() != null)
                         && (!dao.getResponseMessageStatusList().isEmpty()))
                    {
                    // Find out what the DAO told us (should be the same as the ResponseMessage)
                    listStatusInSubStepLoop.addAll(dao.getResponseMessageStatusList());
                    }
                else
                    {
                    // We don't know what happened...
                    listStatusInSubStepLoop.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }

                LOGGER.error(SOURCE + "Failed after " + TimeoutHelper.RETRY_COUNT + " retries"
                                    + "  [status.list.local=" + ResponseMessageStatus.expandResponseStatusCodes(listStatusInSubStepLoop)
                                    + "] [response_ok=" + (ResponseMessageStatus.isResponseStatusOk(listStatusInSubStepLoop))
                                    + "] [substep.success=" + boolSuccessfulSubStep
                                    + "] [dao.can_proceed=" + Utilities.executeWorkerCanProceed(dao)
                                    + "]");
                }

            // boolSuccessfulSubCommand controls if we try to execute the next step, or not...
            ResponseMessageStatus.showResponseMessageStatus(responseSubStep,
                                                            dao,
                                                            listStatusInSubStepLoop,
                                                            SOURCE + "At end of macro substep loop ",
                                                            debug);
            }

        ResponseMessageStatus.showResponseMessageStatus(null,
                                                        dao,
                                                        listStatusInSubStepLoop,
                                                        SOURCE + "At end of executeSteppedCommands(), returned status in status.list.local ",
                                                        debug);
        return (listStatusInSubStepLoop);
        }
    }
