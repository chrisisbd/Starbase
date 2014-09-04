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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.hex.HexFileHelper;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusParsers;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ResponseViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * GetData.
 */

public final class GetData implements FrameworkConstants,
                                      FrameworkStrings,
                                      FrameworkMetadata,
                                      FrameworkSingletons,
                                      ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetData().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetData(final StaribusCoreHostMemoryInterface dao,
                                                     final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetData.doGetData() ";
        final int PARAMETER_COUNT_MIN = 2;
        final int INDEX_FORMAT = 0;
        final int INDEX_FILTER = 1;
        Instrument xmlInstrument;
        PluginType xmlDataCaptureModule;
        final CommandType cmdGetData;
        List<Metadata> listCopiedPluginMetadata;
        final List<ParameterType> listExecutionParameters;
        final ResponseMessageInterface responseMessage;
        int intCaptureChannelCount;
        final StringBuffer bufferExpression;
        StringBuffer bufferResult;
        XmlObject[] selection;
        final List<DataTypeDictionary> listChannelDataTypes;
        final List<String> listTemperatureFlag;
        final List<String> errors;
        DataFormat dataFormat;
        DataFilterType dataFilterType;
        final boolean boolTemperature;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // We haven't found anything yet...
        xmlInstrument = null;
        xmlDataCaptureModule = null;
        listCopiedPluginMetadata = null;
        bufferExpression = new StringBuffer();
        bufferResult = new StringBuffer();
        listChannelDataTypes = new ArrayList<DataTypeDictionary>(10);
        listTemperatureFlag = new ArrayList<String>(1);
        errors = new ArrayList<String>(10);
        dataFormat = null;
        dataFilterType = null;

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdGetData = (CommandType)commandmessage.getCommandType().copy();

        // We expect two parameters, the data format (only Staribus), and the data filter
        //listExecutionParameters = cmdGetData.getParameterList();
        listExecutionParameters = commandmessage.getExecutionParameters();

        // The number of channels will be determined from the DataCapture Module Metadata
        intCaptureChannelCount = 0;

        //------------------------------------------------------------------------------------------
        // Make sure this Instrument has a DataCapture Module

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getController() != null))
            {
            xmlInstrument = dao.getHostInstrument().getInstrument();

            bufferExpression.setLength(0);
            bufferExpression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            bufferExpression.append(FrameworkXpath.XPATH_PLUGIN_DATA_CAPTURE);

            // Query from the root of the Controller
            selection = xmlInstrument.getController().selectPath(bufferExpression.toString());

            // Did we find at least one Plugin with the required name?
            // If so, take the first
            if ((selection != null)
                && (selection instanceof PluginType[])
                && (selection.length >= 1)
                && (selection[0] != null)
                && (selection[0] instanceof PluginType))
                {
                //LOGGER.debugTimedEvent("DataCapture PluginType=" + selection[0].xmlText());

                // Don't affect the Plugin in the Instrument XML
                xmlDataCaptureModule = (PluginType)selection[0].copy();
                listCopiedPluginMetadata = MetadataHelper.getCopyOfPluginMetadataList(xmlDataCaptureModule);

                // Validate the Metadata from the DataCapture Module
                // and find the number of channels to process, with their DataTypes
                // The ConfigurationList is not modified, so no need to copy()
                intCaptureChannelCount = DataAnalyser.getCaptureChannelCount(listCopiedPluginMetadata,
                                                                             EMPTY_STRING,
                                                                             listChannelDataTypes,
                                                                             listTemperatureFlag);
                }
            }

        //         LOGGER.debugTimedEvent("Instrument =" + (xmlInstrument != null));
        //         LOGGER.debugTimedEvent("DataCapture Module =" + (xmlDataCaptureModule != null));
        //         LOGGER.debugTimedEvent("DECLARED CHANNEL COUNT=" + intDiscoveredChannelCount);
        //         LOGGER.debugTimedEvent("TEMPFLAG size= " + listTemperatureFlag.size());
        //         LOGGER.debugTimedEvent("Param 0= " + listParameters.get(0).getInputDataType().getDataTypeName());
        //         LOGGER.debugTimedEvent("Param 1= " + listParameters.get(1).getInputDataType().getDataTypeName());

        //------------------------------------------------------------------------------------------
        // Do the getData() operation, which expects a Response!
        // This Command usually expects blocked data from the Port
        // Only proceed if there are correctly defined channels to capture...

        if ((intCaptureChannelCount > 0)
            && (listChannelDataTypes != null)
            && (intCaptureChannelCount == listChannelDataTypes.size())
            && (xmlInstrument != null)
            && (xmlDataCaptureModule != null)
            && (cmdGetData.getResponse() != null)
            && (cmdGetData.getBlockedDataCommand() != null)
            && (cmdGetData.getBlockedDataCommand().getBlockCountCommandCode() != null)
            && (cmdGetData.getBlockedDataCommand().getBlockCommandCode() != null))
            {
            final String strBlockCountCommandCode;
            final String strBlockCommandCode;
            CommandType cmdGetDataBlockCount;
            CommandType cmdGetDataBlock;

            // Retrieve the Commands to execute
            strBlockCountCommandCode = cmdGetData.getBlockedDataCommand().getBlockCountCommandCode();
            strBlockCommandCode = cmdGetData.getBlockedDataCommand().getBlockCommandCode();

//            LOGGER.log(SOURCE
//                       + "[BlockCountCommandCode=" + strBlockCountCommandCode + "]"
//                       + "[BlockCommandCode=" + strBlockCommandCode + "]");

            // Find the Commands to which these codes relate
            cmdGetDataBlockCount = null;
            cmdGetDataBlock = null;

            // The CommandCode identifies the specific Command
            // The XML holds these values as two-character Hex numbers
            // Find the BlockCount Command
            // ins:Plugin[ins:Identifier/text()='DataCapture']/ins:Command[ins:CommandCode/text()='02']
            bufferExpression.setLength(0);
            bufferExpression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            bufferExpression.append(FrameworkXpath.XPATH_PLUGIN_DATA_CAPTURE);
            bufferExpression.append("/");
            bufferExpression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
            // The CommandCode gives the CommandType
            bufferExpression.append(strBlockCountCommandCode);
            bufferExpression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            //System.out.println("Find the BlockCount Command EXPR=" + bufferExpression);

            // Query from the root of the Controller
            selection = xmlInstrument.getController().selectPath(bufferExpression.toString());

            // Did we find at least one Command with the required CommandCode?
            // If so, take the first
            if ((selection != null)
                && (selection instanceof CommandType[])
                && (selection.length >= 1)
                && (selection[0] != null)
                && (selection[0] instanceof CommandType))
                {
                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                cmdGetDataBlockCount = (CommandType)selection[0].copy();

                //LOGGER.log("BlockCount CommandType=" + cmdGetDataBlockCount.getIdentifier());
                }

            // Find the Block Command
            // ins:Plugin[ins:Identifier/text()='DataCapture']/ins:Command[ins:CommandCode/text()='03']
            bufferExpression.setLength(0);
            bufferExpression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            bufferExpression.append(FrameworkXpath.XPATH_PLUGIN_DATA_CAPTURE);
            bufferExpression.append("/");
            bufferExpression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
            // The CommandCode gives the CommandType
            bufferExpression.append(strBlockCommandCode);
            bufferExpression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            //System.out.println("Find the Block Command EXPR=" + bufferExpression);

            // Query from the root of the Controller
            selection = xmlInstrument.getController().selectPath(bufferExpression.toString());

            // Did we find at least one Command with the required CommandCode?
            // If so, take the first
            if ((selection != null)
                && (selection instanceof CommandType[])
                && (selection.length >= 1)
                && (selection[0] != null)
                && (selection[0] instanceof CommandType))
                {
                //LOGGER.debugTimedEvent("Block CommandType=" + selection[0].xmlText());

                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                cmdGetDataBlock = (CommandType)selection[0].copy();

                //LOGGER.log("Block CommandType=" + cmdGetDataBlock.getIdentifier());
                }

            //-------------------------------------------------------------------------------------
            // We now have all of the Commands to execute
            //-------------------------------------------------------------------------------------
            // Check that each sub-Command correctly refers to this Command as its parent,
            // and then execute the sub-Commands
            // It doesn't matter if the Command is executed on a Port, or locally
            // There's no need to check the CommandVariant

            if ((listExecutionParameters != null)
                && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
                && (listExecutionParameters.get(INDEX_FORMAT) != null)
                && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName()))
                && (listExecutionParameters.get(INDEX_FILTER) != null)
                && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName()))

                && (cmdGetDataBlockCount != null)
                && (cmdGetDataBlockCount.getBlockedDataCommand() != null)
                && (cmdGetData.getCommandCode().equals(cmdGetDataBlockCount.getBlockedDataCommand().getParentCommandCode()))

                && (cmdGetDataBlock != null)
                && (cmdGetDataBlock.getBlockedDataCommand() != null)
                && (cmdGetDataBlock.getParameterList() != null)
                && (cmdGetDataBlock.getParameterList().size() == 1)
                && (PARAMETER_DATA_BLOCKID.equals(cmdGetDataBlock.getParameterList().get(0).getName()))
                && (cmdGetData.getCommandCode().equals(cmdGetDataBlock.getBlockedDataCommand().getParentCommandCode()))

                // getDataBlock() is a command specific to the Staribus protocol, and must return a STARIBUS_BLOCK
                && (cmdGetDataBlock.getResponse() != null)
                && (SchemaDataType.STARIBUS_BLOCK.toString().equals(cmdGetDataBlock.getResponse().getDataTypeName().toString())))
                {
                try
                    {
                    final String strFormat;
                    final String strFilter;
                    String strStarscript;
                    ResponseMessageInterface responseBlockCount;
                    boolean boolSuccessfulBlockCount;

                    strFormat = listExecutionParameters.get(INDEX_FORMAT).getValue();
                    strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                    // Map the format entry to a DataFormat
                    // (may throw IllegalArgumentException if XML is incorrectly configured)
                    dataFormat = DataFormat.getDataFormatForName(strFormat);

                    // Map the filter entry to a FilterType (which must return not NULL)
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
                        }

                    // Establish the identity of this Instrument using Metadata
                    // from the Framework, Observatory and Observer
                    // Add some extra Metadata to specify the Axes and Title of the ProcessedData Chart
                    dao.establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdGetData),
                                                       intCaptureChannelCount,
                                                       DataAnalyser.hasTemperatureChannelInList(listTemperatureFlag),
                                                       listCopiedPluginMetadata,
                                                       StaribusHelper.createMultichannelChartLegendMetadata("Staribus Multichannel Data Logger",
                                                                                                            "Time (UT)",
                                                                                                            "Multichannel Outputs"));

                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_CAPTURE
                                                       + METADATA_FORMAT + dataFormat.getName() + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());

                    //                 LOGGER.debugTimedEvent("SchemaDataType.STARIBUS_BLOCK=" + SchemaDataType.STARIBUS_BLOCK.toString());
                    //                 LOGGER.debugTimedEvent("cmdGetBlock.getResponse().getDataTypeName()=" + cmdGetBlock.getResponse().getDataTypeName().toString());

                    // Execute the loop to assemble the data blocks
                    // Firstly, find out how many blocks of data we expect
                    // by calling getDataBlockCount()
                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "Calling getDataBlockCount()");

                    // No parameters are required for getDataBlockCount()
                    // This command will set off the *Queue* Timeout timer, since this is a REMOTE comand
                    responseBlockCount = null;
                    // This *assumes* that the Parameters are taken from the Command verbatim
                    strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                           xmlDataCaptureModule,
                                                                           null,
                                                                           cmdGetDataBlockCount,
                                                                           false);
                    dao.getResponseMessageStatusList().clear();
                    boolSuccessfulBlockCount = false;

                    for (int retryid = 0;
                         ((retryid < TimeoutHelper.RETRY_COUNT)
                              && (!boolSuccessfulBlockCount)
                              && (Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker())));
                         retryid++)
                        {
                        TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                        // getData() is running on the SwingWorker Thread in executeCommand()
                        // in the DAO, expecting data or an ABORT or TIMEOUT,
                        // so it is easier to execute sub-Commands in the same Thread!
                        // Use executeCommandOnSameThread() which does not start another Thread
                        // getDataBlockCount() is a SendToPort Command, so executeCommandOnSameThread()
                        // will handle the Timeout; it will execute within a default Timeout period
                        // If we get TIMEOUT, try again until retries exhausted
                        // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                        responseBlockCount = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                             dao,
                                                                                             xmlInstrument,
                                                                                             xmlDataCaptureModule,
                                                                                             cmdGetDataBlockCount,
                                                                                             // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                             cmdGetDataBlockCount.getParameterList(),
                                                                                             strStarscript,
                                                                                             errors,
                                                                                             false,
                                                                                             true);

                        // The Command must be completed without error or timeout for the next step to make sense
                        // So the Status must be SUCCESS
                        // WARNING!! responseBlockCount must return with non-null DAOWrapper
                        // since we need the ResponseValue
                        boolSuccessfulBlockCount = ((responseBlockCount != null)
                                                    && (ResponseMessageStatus.wasResponseSuccessful(responseBlockCount))
                                                    && (responseBlockCount.getWrappedData() != null));
                        // End of Retry loop
                        Thread.yield();
                        }

                    if (boolSuccessfulBlockCount)
                        {
                        final int INDEX_BLOCKID = 0;
                        final int intBlockCount;
                        final ResponseMessageStatusList listStatusInBlockReadLoop;
                        boolean boolSuccess;

                        // BlockCount is in HEX
                        intBlockCount = Integer.parseInt(responseBlockCount.getWrappedData().getResponseValue(), RADIX_HEX);
                        bufferResult = new StringBuffer();
                        boolSuccess = true;

                        LOGGER.debugTimedEvent(boolDebug,
                                               SOURCE + "getDataBlockCount() [blockcount=" + intBlockCount + "]");

                        // Keep the GC happy...
                        responseBlockCount.setWrappedData(null);

                        listStatusInBlockReadLoop = ResponseMessageStatus.createResponseMessageStatusList();

                        //-------------------------------------------------------------------------
                        // We now know the block count, and so know how many sub-commands must be
                        // executed before the local getData() command completes.
                        // The getData() LOCAL command set off the DAO Timeout timer,
                        // which won't run for long enough to complete the whole sequence,
                        // since it is set to time for a single command,
                        // so it must be restarted each time through a successful loop.
                        //-------------------------------------------------------------------------

                        //LOGGER.debugTimedEvent("StaribusCoreDAO.getData() START LOOP -------------------------------");

                        // Read each chunk of data and assemble into the final Response
                        // Terminate if any sub-Command fails to complete
                        for (int blockid = 0;
                             ((boolSuccess)
                                  && (blockid < intBlockCount)
                                  && (Utilities.executeWorkerCanProceed(dao)));
                             blockid++)
                            {
                            ResponseMessageInterface responseDataBlock;
                            boolean boolSuccessfulDataBlock;

                            // Re-use the existing Parameter object
                            cmdGetDataBlock.getParameterList().get(INDEX_BLOCKID).setValue(Utilities.intToFourHexString(blockid));
                            // This *assumes* that the Parameters are taken from the Command verbatim
                            strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                                   xmlDataCaptureModule,
                                                                                   null,
                                                                                   cmdGetDataBlock,
                                                                                   false);
                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "Calling getDataBlock(" + cmdGetDataBlock.getParameterList().get(INDEX_BLOCKID).getValue() + ")");

                            responseDataBlock =  null;

                            //                             setRawDataChannelCount(intCaptureChannelCount);
                            //                             setTemperatureChannel(DataAnalyser.hasTemperatureChannelInList(listTemperatureFlag));

                            dao.getResponseMessageStatusList().clear();
                            boolSuccessfulDataBlock = false;

                            // Attempt the getDataBlock() command again if it fails
                            // This will eat into the Timeout time, but if it fails many times, we want to know
                            for (int retryid = 0;
                                 ((retryid < TimeoutHelper.RETRY_COUNT)
                                      && (!boolSuccessfulDataBlock)
                                      && Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker()));
                                 retryid++)
                                {
                                TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                                // Restart the DAO Timeout Timer for DataCapture.getDataBlock() on every retry
                                TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(xmlDataCaptureModule, cmdGetDataBlock));

                                listStatusInBlockReadLoop.clear();

                                // getDataBlock() is a SendToPort Command
                                // This call will start the Rx Queue waiting loop timer
                                // If we get TIMEOUT, try again until retries exhausted
                                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                                responseDataBlock = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                                    dao,
                                                                                                    xmlInstrument,
                                                                                                    xmlDataCaptureModule,
                                                                                                    cmdGetDataBlock,
                                                                                                    // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                                    cmdGetDataBlock.getParameterList(),
                                                                                                    strStarscript,
                                                                                                    errors,
                                                                                                    false,
                                                                                                    true);
                                // Are the data ok this time round?
                                // Allow only SUCCESS, BUSY and CAPTURE_ACTIVE to represent a success,
                                // otherwise retry
                                boolSuccessfulDataBlock = ((responseDataBlock != null)
                                                           && (ResponseMessageStatus.wasResponseSuccessful(responseDataBlock))
                                                           && (responseDataBlock.getWrappedData() != null));

                                // We don't make any use of the failed status from executeCommandOnSameThread(),
                                // other than to log it
                                if (!boolSuccessfulDataBlock)
                                    {
                                    // Log failed blocks here...
                                    DAOCommandHelper.logResponseBlock(dao,
                                                                      commandmessage,
                                                                      responseDataBlock,
                                                                      blockid,
                                                                      retryid);
                                    }

                                Thread.yield();
                                }

                            // The Command must be completed without error or timeout
                            // Repeat the full logical test for simplicity
                            // WARNING!! responseBlock must return with non-null DAOWrapper
                            // since we need the ResponseValue
                            if (boolSuccessfulDataBlock)
                                {
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "getDataBlock() SUCCESS, appending block data to ResponseValue");

                                // Append the Block timestamped data, as yet unparsed into columns
                                // The Response DataType must be StaribusBlock, so it has been checked for length
                                bufferResult.append(responseDataBlock.getWrappedData().getResponseValue());

                                // Keep the GC happy...
                                responseDataBlock.setWrappedData(null);

                                // This might be the last time through, so say it is successful
                                listStatusInBlockReadLoop.add(ResponseMessageStatus.SUCCESS);
                                }
                            else
                                {
                                // CRC errors etc. end up here
                                // Terminate the loop on failure, since the Retry gave up
                                LOGGER.debugTimedEvent(boolDebug,
                                                       SOURCE + "getDataBlock() failed after retries [OnErrorContinue="
                                                          + dao.continueOnError() + "] [buffer_length=" + bufferResult.length() + "]");
                                //bufferResult.setLength(0);

                                // Carry on if we are told to...
                                boolSuccess = dao.continueOnError();

                                if ((responseDataBlock != null)
                                    && (responseDataBlock.getResponseMessageStatusList() != null)
                                    && (!responseDataBlock.getResponseMessageStatusList().isEmpty()))
                                    {
                                    // Find out what the ResponseMessage told us
                                    listStatusInBlockReadLoop.addAll(responseDataBlock.getResponseMessageStatusList());
                                    }
                                else
                                    {
                                    // We don't know what happened...
                                    listStatusInBlockReadLoop.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                    }
                                }

                            Thread.yield();
                            }

                        // Capture the final Status and ResponseValue (these may show a failure)
                        dao.getResponseMessageStatusList().addAll(listStatusInBlockReadLoop);
                        }
                    else
                        {
                        LOGGER.debugTimedEvent(boolDebug,
                                               SOURCE + "getDataBlockCount() failed, so cannot run getData()");

                        if ((responseBlockCount != null)
                            && (responseBlockCount.getResponseMessageStatusList() != null)
                            && (!responseBlockCount.getResponseMessageStatusList().isEmpty()))
                            {
                            // Find out what the ResponseMessage told us
                            dao.getResponseMessageStatusList().addAll(responseBlockCount.getResponseMessageStatusList());
                            }
                        else
                            {
                            // We don't know what happened...
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                            }
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       ObservatoryInstrumentDAOInterface.ERROR_PARSE_BLOCK_COUNT + exception.getMessage(),
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (IllegalArgumentException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (Exception exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]",
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
            // Something is wrong with the XML definition of getData()
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_RAWDATA,
                                                                                     METADATA_ACTION_CAPTURE));
            }

        //------------------------------------------------------------------------------------------
        // Put the Timeout back to what it should be for a single default command.

        TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);

        //------------------------------------------------------------------------------------------
        // Did we succeed in getting a valid data block?
        // ResponseStatus may be INVALID_MESSAGE, INVALID_COMMAND, TIMEOUT or ABORT at this point
        // If SUCCESS so far, continue to parse the data block into Starbase internal format
        // We can only deal with Staribus in this DAO!

        if ((ResponseMessageStatus.wasResponseListSuccessful(dao.getResponseMessageStatusList()))
            && (bufferResult.length() > 0)
            && (dataFormat != null)
            && (DataFormat.STARIBUS.getName().equals(dataFormat.getName()))
            && (dataFilterType != null))
            {
            // Parse the concatenated data Blocks into timestamped Vectors, setting RawData
            // Return NULL if the parsing failed
            //LOGGER.debugProtocolEvent("StaribusCoreDAO.getData() SUCCESS, parsing data...");
            // The data format is: <Calendar> [<Temperature>] <Channel0> <Channel1> <Channel2>

            dao.setRawData(StaribusParsers.parseStaribusBlocksIntoVector(bufferResult,
                                                                         listChannelDataTypes,
                                                                         intCaptureChannelCount,
                                                                         listTemperatureFlag,
                                                                         errors));
            //LOGGER.debugProtocolEvent("StaribusCoreDAO.getData() SUCCESS, parsing completed...");

            // TODO get errors and do something!

            if (dao.getRawData() != null)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_FILTERING
                                                       + METADATA_FORMAT + dataFormat.getName() + TERMINATOR + SPACE
                                                       + METADATA_FILTERNAME + dataFilterType.getName() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());


                //                 // TODO REVIEW  Did we have a Temperature channel?
                //                 boolTemperature = ((listTemperatureFlag != null)
                //                                        && (listTemperatureFlag.contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey())));
                //
                //                 // Filter the RawData to produce the XYDataset
                //
                //                 // This DAO is used by the Magnetometer, VLF, Loggers, etc.
                //                 setRawDataChannelCount(intCaptureChannelCount);
                //                 setTemperatureChannel(boolTemperature);

                // The data format is: <Calendar> [<Temperature>] <Channel0> <Channel1> <Channel2>
                DataFilterHelper.filterCalendarisedRawDataAndTemperature(dao,
                                                                         dao.getFilter(),
                                                                         listChannelDataTypes,
                                                                         dao.getLocalHostname());
                // Help the GC?
                if (dao.getFilter() != null)
                    {
                    dao.getFilter().disposeFilter();
                    }

                dao.setFilter(null);
                dao.setTranslator(null);

                ObservatoryInstrumentHelper.runGarbageCollector();

                // Say we succeeded, even if the Filter failed,
                // so the data are always visible in RawData
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }
            else
                {
                // This must fail, because we can't interpret the data blocks
                // This takes account of isInstrumentDataConsumer()
                dao.clearData();

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_DATA,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                }
            }
        else
            {
            // This must fail, because we can't interpret the data blocks

            LOGGER.logTimedEvent("--------------------------------------------------------------------------------------");
            LOGGER.logTimedEvent("DEBUG WARNING getData() Data assembly or parsing failed, see below for possible causes");

            LOGGER.logTimedEvent("Response Status Word=" + Utilities.intToBitString(ResponseMessageStatus.convertResponseStatusCodesToBits(dao.getResponseMessageStatusList())));
            LOGGER.logTimedEvent("Response Status Codes=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList()));

            if (dataFormat != null)
                {
                LOGGER.logTimedEvent("Format=" + dataFormat.getName());
                }
            else
                {
                LOGGER.logTimedEvent("Null or invalid Data Format");
                }

            if (dataFilterType != null)
                {
                LOGGER.logTimedEvent("Filter=" + dataFilterType.getName());
                }
            else
                {
                LOGGER.logTimedEvent("Null or invalid Data Filter");
                }

            LOGGER.logTimedEvent("Data Buffer length=" + bufferResult.length());

            // Show the buffer if we can
            if (bufferResult.length() > 0)
                {
                LOGGER.logTimedEvent("Data Buffer");
                LOGGER.logTimedEvent(HexFileHelper.dumpHex(bufferResult.toString().getBytes(),
                                                           ResponseViewerUIComponentInterface.DUMP_BYTES_PER_LINE));
                }

            LOGGER.logTimedEvent("--------------------------------------------------------------------------------------");

            // ToDo: REVIEW This must fail, because we can't interpret the data
            //dao.clearData();

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               ObservatoryInstrumentDAOInterface.ERROR_DATA_FORMAT,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage which contains the DAOWrapper

        if (ResponseMessageStatus.wasResponseListSuccessful(dao.getResponseMessageStatusList()))
            {
            // Explicitly set the ResponseValue as the first timestamp only, to save space
            cmdGetData.getResponse().setValue(bufferResult.toString().substring(0, StaribusParsers.LENGTH_STARIBUS_TIMESTAMP));

            // Create the ResponseMessage - this creates a DAOWrapper containing the parsed data
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      xmlInstrument,
                                                                      xmlDataCaptureModule,
                                                                      cmdGetData,
                                                                      AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdGetData));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           xmlInstrument,
                                                                           xmlDataCaptureModule,
                                                                           cmdGetData,
                                                                           AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdGetData));
            }

        return (responseMessage);
        }
    }
