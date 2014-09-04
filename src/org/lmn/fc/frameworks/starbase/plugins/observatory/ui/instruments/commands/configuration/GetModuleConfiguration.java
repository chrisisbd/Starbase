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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * GetModuleConfiguration.
 */

public final class GetModuleConfiguration implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkSingletons,
                                                     FrameworkXpath,
                                                     ObservatoryConstants
    {
    private static final int INDEX_GET_MODULE_CONFIG_MODULEID = 0;
    private static final int INDEX_GET_CONFIG_BLOCK_COUNT_MODULEID = 0;
    private static final int INDEX_GET_CONFIG_BLOCK_MODULEID = 0;
    private static final int INDEX_GET_CONFIG_BLOCK_BLOCKID = 1;


    /***********************************************************************************************
     * getModuleConfiguration().
     *
     * getModuleConfiguration() first uses getConfigurationBlockCount() to get the number of blocks
     * then iterates over getConfigurationBlock() to get all blocks from the specified ModuleID.
     * These sub-commands are specified through the use of the <BlockedDataCommand> element.
     * If a Module is missing, then the ResponseValue must be 'NODATA'.
     *
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetModuleConfiguration(final ObservatoryInstrumentDAOInterface dao,
                                                                    final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetModuleConfiguration.getModuleConfiguration() ";
        final CommandType cmdGetModuleConfig;
        int intModuleID;
        final ParameterType parameter;
        final Instrument xmlInstrument;
        final XmlObject xmlController;
        final ResponseMessageInterface responseMessage;
        String strResponseValue;
        boolean boolValidModuleID;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               "\n\n" + SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command in the CommandMessage
        cmdGetModuleConfig = (CommandType)commandmessage.getCommandType().copy();

        // We haven't found anything yet...
        strResponseValue = null;
        intModuleID = 0;
        boolValidModuleID = false;

        try
            {
            // Retrieve the requested ModuleID (encoded as Decimal)
            parameter = commandmessage.getCommandType().getParameterList().get(INDEX_GET_MODULE_CONFIG_MODULEID);
            intModuleID = Integer.parseInt(parameter.getValue(), RADIX_DECIMAL);

            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "[ModuleID=" + intModuleID + "]");
            boolValidModuleID = true;
            }

        catch (NumberFormatException exception)
            {
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Unable to parse Parameter ModuleID");
            }

        // Which Instrument is running this Command?
        // For a ready installed virtual Instrument, or one which has already been discovered,
        // then the incoming CommandMessage holds the real Instrument, with an address.
        // During the Discovery process, let's assume that the CommandMessage holds the
        // DiscoveryController, which is assumed to have a valid set of Core Commands.

        // The Instrument being used either to Discover, or has already been discovered :-)
        xmlInstrument = commandmessage.getInstrument();

        // We know we are only dealing with the Controller's Core module (as opposed to a Plugin)
        xmlController=  xmlInstrument.getController();

        //------------------------------------------------------------------------------------------
        // Do the getModuleConfiguration() operation, which expects a Response!

        if ((boolValidModuleID)
//            && (XmlBeansUtilities.isValidXml(xmlInstrument))
            && (cmdGetModuleConfig.getResponse() != null))
            {
            // This Command expects blocked data
            if ((cmdGetModuleConfig.getBlockedDataCommand() != null)
                && (cmdGetModuleConfig.getBlockedDataCommand().getBlockCountCommandCode() != null)
                && (cmdGetModuleConfig.getBlockedDataCommand().getBlockCommandCode() != null))
                {
                final String strBlockCountCommandCode;
                final String strBlockCommandCode;
                CommandType cmdGetConfigBlockCount;
                CommandType cmdGetConfigBlock;
                final StringBuffer expression;
                XmlObject[] selection;

                // Retrieve the Commands to execute
                strBlockCountCommandCode = cmdGetModuleConfig.getBlockedDataCommand().getBlockCountCommandCode();
                strBlockCommandCode = cmdGetModuleConfig.getBlockedDataCommand().getBlockCommandCode();

                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "[BlockCountCommandCode=" + strBlockCountCommandCode + "]"
                                           + "[BlockCommandCode=" + strBlockCommandCode + "]");

                // Find the Commands to which these codes relate
                cmdGetConfigBlockCount = null;
                cmdGetConfigBlock = null;
                expression = new StringBuffer();

                // The CommandCode identifies the specific Command
                // The XML holds these values as two-character Hex numbers
                // Find the BlockCount Command
                expression.setLength(0);
                expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
                // The CommandCode gives the CommandType
                expression.append(strBlockCountCommandCode);
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

                // Query from the root of the Controller
                selection = xmlController.selectPath(expression.toString());

                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length == 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    LOGGER.debugTimedEvent(boolDebug,
                                           "BlockCount CommandType=" + selection[0].xmlText());

                    // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                    cmdGetConfigBlockCount = (CommandType)selection[0].copy();
                    }

                // Find the Block Command
                expression.setLength(0);
                expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
                // The CommandCode gives the CommandType
                expression.append(strBlockCommandCode);
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

                // Query from the root of the Controller
                selection = xmlController.selectPath(expression.toString());

                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length == 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    LOGGER.debugTimedEvent(boolDebug,
                                           "Block CommandType=" + selection[0].xmlText());

                    // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                    cmdGetConfigBlock = (CommandType)selection[0].copy();
                    }

                //----------------------------------------------------------------------------------
                // Check that each sub-Command correctly refers to this Command as its parent,
                // and then execute the sub-Commands
                // It doesn't matter if the Command is executed on a Port, or locally
                // There's no need to check the CommandVariant

                if (isValidCommand(cmdGetModuleConfig,
                                   cmdGetConfigBlockCount,
                                   cmdGetConfigBlock))
                    {
                    String strStarscript;
                    final List<String> errors;
                    ResponseMessageInterface responseBlockCount;
                    boolean boolCountComplete;

                    // Execute the loop to assemble the data blocks
                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "Command validated, assembling data...");

                    errors = new ArrayList<String>(10);

                    // Firstly, find out how many blocks of data we expect
                    // by calling getConfigurationBlockCount()

                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "Calling getConfigurationBlockCount()");

                    // getConfigurationBlockCount() requires the ModuleID as a parameter
                    // We already know the Command XML is valid
                    cmdGetConfigBlockCount.getParameterArray(INDEX_GET_CONFIG_BLOCK_COUNT_MODULEID).setValue(Integer.toString(intModuleID));

                    // This *assumes* that the Parameters are taken from the Command verbatim
                    strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                           xmlController,
                                                                           null,
                                                                           cmdGetConfigBlockCount,
                                                                           false);
                    dao.getResponseMessageStatusList().clear();
                    responseBlockCount = null;
                    boolCountComplete = false;

                    for (int retryid = 0;
                        ((retryid < TimeoutHelper.RETRY_COUNT)
                            && (!boolCountComplete)
                            && Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker()));
                        retryid++)
                        {
                        TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                        // getModuleConfiguration() is a Local command which calls a SendToPort command
                        // executeCommandOnSameThread() does not provide timeout for Local,
                        // so restart the Timeout for the getModuleConfiguration() sub-Command
                        TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(xmlController, cmdGetConfigBlockCount));

                        // getModuleConfiguration() is running on the SwingWorker Thread in executeCommand()
                        // in the DAO, expecting data or an ABORT or TIMEOUT,
                        // so it is easier to execute sub-Commands in the same Thread!
                        // getConfigurationBlockCount() is a SendToPort Command,
                        // so executeCommandOnSameThread() will handle the Timeout
                        // If we get TIMEOUT, try again until retries exhausted
                        // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                        responseBlockCount = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                             dao,
                                                                                             xmlInstrument,
                                                                                             xmlController,
                                                                                             cmdGetConfigBlockCount,
                                                                                             // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                             cmdGetConfigBlockCount.getParameterList(),
                                                                                             strStarscript,
                                                                                             errors,
                                                                                             false,
                                                                                             boolDebug);
                        // Are the data ok this time round?
                        // Allow only SUCCESS, BUSY and CAPTURE_ACTIVE to represent a success, otherwise retry
                        // WARNING! Test INVALID_MODULE first!
                        boolCountComplete = ((ResponseMessageStatus.wasResponseInvalidModule(responseBlockCount))
                                             || (ResponseMessageStatus.wasResponseSuccessful(responseBlockCount)));
                        // End of Retry loop
                        }

                    // Allow INVALID_MODULE to return NO_DATA
                    // WARNING! Test INVALID_MODULE first!
                    if (ResponseMessageStatus.wasResponseInvalidModule(responseBlockCount))
                        {
                        LOGGER.debugTimedEvent(boolDebug,
                                               SOURCE + "getConfigurationBlockCount() Allow INVALID_MODULE to return NO_DATA");

                        // The BlockCount is zero, so there's no data
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                        strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                        }

                    // The Command must be completed without error or timeout for the next step to make sense
                    // So the Status must be SUCCESS
                    else if ((responseBlockCount != null)
                        && (ResponseMessageStatus.wasResponseSuccessful(responseBlockCount)))
                        {
                        try
                            {
                            final int intBlockCount;
                            final StringBuffer buffer;
                            final ResponseMessageStatusList listStatusBlockReadInLoop;
                            boolean boolSuccess;

                            // BlockCount is in HEX
                            intBlockCount = Integer.parseInt(responseBlockCount.getWrappedData().getResponseValue(), RADIX_HEX);
                            buffer = new StringBuffer();
                            boolSuccess = true;

                            listStatusBlockReadInLoop = ResponseMessageStatus.createResponseMessageStatusList();

                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "getConfigurationBlockCount() returned [blockcount=" + intBlockCount + "]");

                            if (intBlockCount > 0)
                                {
                                // Read each chunk of data and assemble into the final Response
                                // Terminate if any sub-Command fails to complete,
                                // since the overall XML would then not be parseable
                                for (int intBlockID = 0;
                                     (boolSuccess) && (intBlockID < intBlockCount);
                                     intBlockID++)
                                    {
                                    ResponseMessageInterface responseBlock;
                                    boolean boolSuccessfulBlock;

                                    // Re-use the existing Parameter object
                                    cmdGetConfigBlock.getParameterList().get(INDEX_GET_CONFIG_BLOCK_MODULEID).setValue(Integer.toString(intModuleID));
                                    cmdGetConfigBlock.getParameterList().get(INDEX_GET_CONFIG_BLOCK_BLOCKID).setValue(Utilities.intToFourHexString(intBlockID));

                                    LOGGER.debugTimedEvent(boolDebug,
                                                           SOURCE + "Calling getConfigurationBlock("
                                                                + cmdGetConfigBlock.getParameterList().get(INDEX_GET_CONFIG_BLOCK_MODULEID).getValue()
                                                                + COMMA
                                                                +  cmdGetConfigBlock.getParameterList().get(INDEX_GET_CONFIG_BLOCK_BLOCKID).getValue() + ")");

                                    // This *assumes* that the Parameters are taken from the Command verbatim
                                    strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                                           xmlController,
                                                                                           null,
                                                                                           cmdGetConfigBlock,
                                                                                           false);
                                    dao.getResponseMessageStatusList().clear();
                                    responseBlock = null;
                                    boolSuccessfulBlock = false;

                                    for (int retryid = 0;
                                        ((retryid < TimeoutHelper.RETRY_COUNT)
                                            && (!boolSuccessfulBlock)
                                            && Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker()));
                                        retryid++)
                                        {
                                        TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                                        listStatusBlockReadInLoop.clear();

                                        // getModuleConfiguration() is a Local command which calls a SendToPort command
                                        // executeCommandOnSameThread() does not provide timeout for Local,
                                        // so restart the Timeout for the getModuleConfiguration() sub-Command
                                        TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(xmlController, cmdGetConfigBlock));

                                        // getConfigurationBlock() is a SendToPort Command,
                                        // so executeCommandOnSameThread() will handle the Timeout
                                        // If we get TIMEOUT, try again until retries exhausted
                                        // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                                        responseBlock = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                                        dao,
                                                                                                        xmlInstrument,
                                                                                                        xmlController,
                                                                                                        cmdGetConfigBlock,
                                                                                                        // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                                        cmdGetConfigBlock.getParameterList(),
                                                                                                        strStarscript,
                                                                                                        errors,
                                                                                                        false,
                                                                                                        boolDebug);

                                        // TODO WARNING!! responseBlock must return with non-null DAOWrapper
                                        boolSuccessfulBlock = ((responseBlock != null)
                                                                && (ResponseMessageStatus.wasResponseSuccessful(responseBlock))
                                                                && (responseBlock.getWrappedData() != null));
                                        // End of Retry loop
                                        }

                                    // The Command must be completed without error or timeout
                                    if (boolSuccessfulBlock)
                                        {
                                        LOGGER.debugTimedEvent(boolDebug,
                                                               SOURCE + "getConfigurationBlock() SUCCESS, appending data");
                                        buffer.append(responseBlock.getWrappedData().getResponseValue());

                                        // This might be the last time through, so say it is successful
                                        listStatusBlockReadInLoop.add(ResponseMessageStatus.SUCCESS);
                                        }
                                    else
                                        {
                                        // Terminate the loop on failure
                                        LOGGER.debugTimedEvent(boolDebug,
                                                               SOURCE + "getConfigurationBlock() failed");
                                        buffer.setLength(0);
                                        boolSuccess = false;

                                        if ((responseBlock != null)
                                            && (responseBlock.getResponseMessageStatusList() != null)
                                            && (!responseBlock.getResponseMessageStatusList().isEmpty()))
                                            {
                                            // Find out what the ResponseMessage told us
                                            listStatusBlockReadInLoop.addAll(responseBlock.getResponseMessageStatusList());
                                            }
                                        else
                                            {
                                            // We don't know what happened...
                                            listStatusBlockReadInLoop.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                            }
                                        }

                                    // This is probably not necessary
                                    responseBlock.setWrappedData(null);
                                    }

                                // Capture the final Status and ResponseValue (these may show a failure)
                                dao.getResponseMessageStatusList().addAll(listStatusBlockReadInLoop);

                                strResponseValue = buffer.toString();

                                // Remove the ETX marker and anything beyond in the last block
                                if ((strResponseValue != null)
                                    && (!EMPTY_STRING.equals(strResponseValue))
                                    && (strResponseValue.indexOf(ControlCharacters.ETX.getAsChar()) >= 0))
                                    {
                                    strResponseValue = strResponseValue.substring(0, strResponseValue.indexOf(ControlCharacters.ETX.getAsChar()));
                                    }
                                }
                            else
                                {
                                // The BlockCount is zero, so there's no data
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                                }
                            }

                        catch (final NumberFormatException exception)
                            {
                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "Unable to parse BlockCount");
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                            }

                        // This is probably not necessary
                        responseBlockCount.setWrappedData(null);
                        }

                    // It was a real failure
                    else
                        {
                        LOGGER.debugTimedEvent(boolDebug,
                                               SOURCE + "getConfigurationBlockCount() failed");

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
                else
                    {
                    // Incorrectly configured XML
                    LOGGER.error(SOURCE + "Sub-Commands do not refer to correct parent; check XML configuration");
                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                             SOURCE,
                                                                                             METADATA_TARGET_CONFIGURATION,
                                                                                             METADATA_ACTION_ASSEMBLE));
                    }
                }
            else
                {
                // The Command is to be implemented locally?
                // i.e. Use the XML already loaded for this virtual Instrument
                LOGGER.log(SOURCE + "Implemented locally");
                strResponseValue = dao.getHostInstrument().getInstrument().toString();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }
            }
        else
            {
            // Something is wrong with the XML definition of getModuleConfiguration()
            LOGGER.error(SOURCE + "Incorrectly set up in XML");
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_CONFIGURATION,
                                                                                     METADATA_ACTION_ASSEMBLE));
            }

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // Don't keep the data on ABORT...

        if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            {
            // Explicitly set the ResponseValue as the XML plain text
            cmdGetModuleConfig.getResponse().setValue(strResponseValue);

            // These parameters are irrelevant here
//            dao.setRawDataChannelCount(CHANNEL_COUNT);
//            dao.setTemperatureChannel(false);

            // Create the ResponseMessage - this creates a DAOWrapper containing the XML
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      xmlInstrument,
                                                                      xmlController,
                                                                      cmdGetModuleConfig,
                                                                      AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdGetModuleConfig));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           xmlInstrument,
                                                                           xmlController,
                                                                           cmdGetModuleConfig,
                                                                           AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdGetModuleConfig));
             }

        // Tidy up, just in case!
        ObservatoryInstrumentHelper.runGarbageCollector();

        return (responseMessage);
        }


    /***********************************************************************************************
     * Check to see if the getModuleConfiguration() Command is configured correctly.
     *
     * @param getmoduleconfig
     * @param getblockcount
     * @param getblock
     *
     * @return boolean
     */

    private static boolean isValidCommand(final CommandType getmoduleconfig,
                                          final CommandType getblockcount,
                                          final CommandType getblock)
        {
        // getConfigurationBlockCount(ModuleID)
        return (getblockcount != null)
            && (getblockcount.getBlockedDataCommand() != null)
            && (getblockcount.getParameterList() != null)
            && (getblockcount.getParameterList().size() == 1)
            && (PARAMETER_CONFIGURATION_MODULEID.equals(getblockcount.getParameterList().get(INDEX_GET_CONFIG_BLOCK_COUNT_MODULEID).getName()))
            && (getmoduleconfig.getCommandCode().equals(getblockcount.getBlockedDataCommand().getParentCommandCode()))

            // getConfigurationBlock(ModuleID, BlockID)
            && (getblock != null)
            && (getblock.getBlockedDataCommand() != null)
            && (getblock.getParameterList() != null)
            && (getblock.getParameterList().size() == 2)
            && (PARAMETER_CONFIGURATION_MODULEID.equals(getblock.getParameterList().get(INDEX_GET_CONFIG_BLOCK_MODULEID).getName()))
            && (PARAMETER_CONFIGURATION_BLOCKID.equals(getblock.getParameterList().get(INDEX_GET_CONFIG_BLOCK_BLOCKID).getName()))
            && (getmoduleconfig.getCommandCode().equals(getblock.getBlockedDataCommand().getParentCommandCode()));
        }
    }
