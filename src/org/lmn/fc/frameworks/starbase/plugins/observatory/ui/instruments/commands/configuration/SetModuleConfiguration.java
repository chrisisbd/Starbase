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
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusParsers;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.SteppedCodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * SetModuleConfiguration.
 */

public final class SetModuleConfiguration implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkSingletons,
                                                     FrameworkXpath,
                                                     FrameworkRegex,
                                                     ObservatoryConstants
    {
    // String resources
    private static final String CORE_COMMAND_CODE_BASE = "00";

    private static final int INDEX_SET_MODULE_CONFIG_MODULEID = 0;
    private static final int INDEX_SET_MODULE_CONFIG_FILENAME = 1;
    private static final int INDEX_SET_MODULE_CONFIG_COMPRESSED = 2;
    private static final int INDEX_SET_CONFIG_BLOCK_MODULEID = 0;
    private static final int INDEX_SET_CONFIG_BLOCK_BLOCKID = 1;
    private static final int INDEX_SET_CONFIG_BLOCK_DATA = 2;

    // The maximum file size is 64k less one reserved 512 byte block //and less one ETX
    private static final long CONFIGURATION_FILE_LENGTH_MAX = 0x10000 - StaribusParsers.LENGTH_STARIBUS_BLOCK;
    private static final long FILE_LENGTH_UNCOMPRESSED = CONFIGURATION_FILE_LENGTH_MAX * 3 >> 1;
    private static final int BLOCK_COUNT_MAX = 0x7F;


    /***********************************************************************************************
     * doSetModuleConfiguration().
     *
     * setModuleConfiguration() iterates over setConfigurationBlock()
     * to set all blocks in the specified ModuleID,
     * using the configuration data in the specified filename.
     * These sub-commands are specified through the use of the <SteppedDataCommand> element.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetModuleConfiguration(final ObservatoryInstrumentDAOInterface dao,
                                                                    final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetModuleConfiguration.setModuleConfiguration() ";
        final int CHANNEL_COUNT = 0;
        final CommandType cmdSetModuleConfig;
        final Instrument xmlInstrument;
        final XmlObject xmlController;
        final List<ParameterType> listParameters;
        int intModuleID;
        boolean boolCompressed;
        StringBuffer buffer;
        ResponseMessageInterface responseMessage;
        ResponseMessageStatus responseMessageStatus;
        int intRawSize;
        int intCompressedSize;
        boolean boolSuccess;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + "LOCAL COMMAND");

        //------------------------------------------------------------------------------------------
        // Initialisation

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command in the CommandMessage
        cmdSetModuleConfig = (CommandType)commandmessage.getCommandType().copy();

        // This Command has three parameters, the ModuleID, Filename containing the data
        // and a flag to compress the XML
        listParameters = cmdSetModuleConfig.getParameterList();

        // We know we are only dealing with the Controller's Core module (as opposed to a Plugin)
        xmlInstrument = commandmessage.getInstrument();
        xmlController=  xmlInstrument.getController();

        // We haven't found anything yet...
        intModuleID = 0;
        boolCompressed = false;
        buffer = new StringBuffer();
        responseMessage = null;
        intRawSize = 1;
        intCompressedSize = 1;

        //------------------------------------------------------------------------------------------
        // Check the setModuleConfiguration() parameters before continuing to retrieve the data file
        // We expect a SteppedDataCommand, with one Command entry

        boolSuccess = isValidCommand(cmdSetModuleConfig,
                                     listParameters,
                                     xmlController);

        //------------------------------------------------------------------------------------------
        // Read the ModuleID Parameter

        if (boolSuccess)
            {
            try
                {
                final ParameterType parameter;

                // Retrieve the requested ModuleID (encoded as Decimal)
                parameter = commandmessage.getCommandType().getParameterList().get(INDEX_SET_MODULE_CONFIG_MODULEID);
                intModuleID = Integer.parseInt(parameter.getValue(), RADIX_DECIMAL);

                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "[ModuleID=" + intModuleID + "]");
                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                }

            catch (NumberFormatException exception)
                {
                LOGGER.error(SOURCE + "Unable to parse Parameter ModuleID");
                responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_MEMORY
                                                       + METADATA_ACTION_WRITE + SPACE
                                                       + METADATA_RESULT
                                                       + responseMessageStatus.getName()
                                                       + TERMINATOR_SPACE
                                                       + METADATA_ORIGIN
                                                           + "Unable to parse Parameter ModuleID"
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                boolSuccess = false;
                }
            }
        else
            {
            responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.FATAL,
                                               METADATA_TARGET_MEMORY
                                                   + METADATA_ACTION_WRITE + SPACE
                                                   + METADATA_RESULT
                                                   + responseMessageStatus.getName()
                                                   + TERMINATOR_SPACE
                                                   + METADATA_ORIGIN
                                                       + "Invalid Command XML structure"
                                                   + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        //------------------------------------------------------------------------------------------
        // Read the Filename Parameter

        if (boolSuccess)
            {
            try
                {
                final String strFilename;

                strFilename = commandmessage.getCommandType().getParameterList().get(INDEX_SET_MODULE_CONFIG_FILENAME).getValue();

                if ((strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename)))
                    {
                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "[Filename=" + strFilename + "]");

                    // Try to open the file for reading
                    buffer = FileUtilities.readFileAsString(strFilename, FILE_LENGTH_UNCOMPRESSED);

                    // If the file is too long, then give up now
                    if (buffer == null)
                        {
                        LOGGER.error(SOURCE + "XML configuration File cannot be read");
                        responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.FATAL,
                                                           METADATA_TARGET_MEMORY
                                                               + METADATA_ACTION_WRITE + SPACE
                                                               + METADATA_RESULT
                                                               + responseMessageStatus.getName()
                                                               + TERMINATOR_SPACE
                                                               + METADATA_ORIGIN
                                                                   + "XML configuration File cannot be read"
                                                               + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET_MEMORY
                                                           + METADATA_ACTION_WRITE + SPACE
                                                           + METADATA_RESULT
                                                           + responseMessageStatus.getName()
                                                           + TERMINATOR_SPACE
                                                           + METADATA_ORIGIN
                                                               + "Invalid Filename"
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    boolSuccess = false;
                    }
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + "Unable to read XML configuration File");
                responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_MEMORY
                                                       + METADATA_ACTION_WRITE + SPACE
                                                       + METADATA_RESULT
                                                       + responseMessageStatus.getName()
                                                       + TERMINATOR_SPACE
                                                       + METADATA_ORIGIN
                                                           + "Unable to read XML configuration File"
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                boolSuccess = false;
                }
            }

        //------------------------------------------------------------------------------------------
        // Read the Compressed Parameter

        if (boolSuccess)
            {
            // This should never throw NumberFormatException, because it has already been parsed
            boolCompressed = Boolean.parseBoolean(listParameters.get(INDEX_SET_MODULE_CONFIG_COMPRESSED).getValue());

            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "[Compressed=" + boolCompressed + "]");
            }

        //------------------------------------------------------------------------------------------
        // Now try to execute the SubCommand

        if (boolSuccess)
            {
            final String strSteppedCommandCodeBase;
            final SteppedCodes steppedCodes;

            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Calling setModuleConfiguration("
                                        + cmdSetModuleConfig.getParameterList().get(INDEX_SET_MODULE_CONFIG_MODULEID).getValue()
                                        + ", "
                                        + cmdSetModuleConfig.getParameterList().get(INDEX_SET_MODULE_CONFIG_FILENAME).getValue()
                                        + ")");

            // Retrieve the SubCommand to execute (we know there's only one in the list)
            steppedCodes = cmdSetModuleConfig.getSteppedDataCommandList().get(0);

            strSteppedCommandCodeBase = steppedCodes.getSteppedCommandCodeBase();

            // The must be in the Core for this Command to work
            if (CORE_COMMAND_CODE_BASE.equals(strSteppedCommandCodeBase))
                {
                final String strSteppedCommandCode;
                CommandType cmdSetConfigBlock;
                final StringBuffer expression;
                final XmlObject[] selection;

                strSteppedCommandCode = steppedCodes.getSteppedCommandCode();

                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "[SteppedCommandCodeBase=" + strSteppedCommandCodeBase + "]"
                                            + "[SteppedCommandCode=" + strSteppedCommandCode + "]");

                // Find the Command to which these codes relate (which we know is in the Core)
                cmdSetConfigBlock = null;
                expression = new StringBuffer();

                // The CommandCode identifies the specific Command
                // The XML holds these values as two-character Hex numbers
                // Find the setModuleConfiguration() Command
                expression.setLength(0);
                expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
                // The CommandCode gives the CommandType
                expression.append(strSteppedCommandCode);
                expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

                // Query from the root of the Controller (i.e. the Core Module)
                selection = xmlController.selectPath(expression.toString());

                if ((selection != null)
                    && (selection instanceof CommandType[])
                    && (selection.length == 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof CommandType))
                    {
                    // This is a very verbose debug!
//                    LOGGER.debugTimedEvent("SteppedCommandCode CommandType=" + selection[0].xmlText(),
//                                              boolDebug);

                    // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                    cmdSetConfigBlock = (CommandType)selection[0].copy();
                    }

                //----------------------------------------------------------------------------------
                // Check as much as we can about the sub-Command and then execute it
                // There's no need to check the CommandVariant

                if (isValidSubCommand(cmdSetModuleConfig,
                                      cmdSetConfigBlock,
                                      strSteppedCommandCodeBase,
                                      strSteppedCommandCode))
                    {
                    final List<String> errors;
                    final int intLastBlockSize;
                    int intBlockCount;

                    // Step through the file buffer and write each block to the Module memory
                    LOGGER.debugTimedEvent(boolDebug,
                                           SOURCE + "SubCommand validated, writing data...");

                    errors = new ArrayList<String>(10);

                    // Start with no compression
                    intRawSize = buffer.length();
                    intCompressedSize = intRawSize;

                    // Do we have to compress the XML?
                    if (boolCompressed)
                        {
                        buffer = Utilities.compressXML(buffer);
                        }

                    //------------------------------------------------------------------------------
                    // Blocks are numbered from 00 to 7E, with a block size of 512 bytes
                    // The last block must be padded to 512 bytes with spaces,
                    // and the data terminated with ETX
                    // This has to be after the compression

                    // Add an ETX to the end of the last block if required
                    if ((buffer.length() > 0)
                        && (buffer.charAt(buffer.length()-1) != ControlCharacters.ETX.getAsChar()))
                        {
                        buffer.append(ControlCharacters.ETX.getAsChar());
                        intRawSize++;
                        }

                    // How many blocks do we have to write?
                    intBlockCount = buffer.length() / StaribusParsers.LENGTH_STARIBUS_BLOCK;
                    intLastBlockSize = buffer.length() % StaribusParsers.LENGTH_STARIBUS_BLOCK;

                    // Extend the partial last block, padding with spaces
                    if (intLastBlockSize > 0)
                        {
                        intBlockCount++;

                        for (int padcount = 0;
                             padcount < (StaribusParsers.LENGTH_STARIBUS_BLOCK - intLastBlockSize);
                             padcount++)
                            {
                            buffer.append(SPACE);
                            intRawSize++;
                            }
                        }

                    // Update the final buffer size
                    intCompressedSize = buffer.length();

                    //System.out.println("SIZE=" + intCompressedSize + "  BUFFER={{" + buffer + "}}");
                    //System.out.println("COMPRESSED SIZE=" + intCompressedSize + "  BLOCK COUNT=" + intBlockCount);

                    // Double check the total size and the Block count
                    if ((buffer.length() <= (CONFIGURATION_FILE_LENGTH_MAX))
                        && (intBlockCount > 0)
                        && (intBlockCount <= BLOCK_COUNT_MAX))
                        {
                        String strStarscript;

                        for (int blockid = 0;
                             blockid < intBlockCount;
                             blockid++)
                            {
                            boolean boolSuccessfulSubCommand;

                            // Re-use the existing Parameter object
                            // WARNING! BlockID is in HEX
                            cmdSetConfigBlock.getParameterList().get(INDEX_SET_CONFIG_BLOCK_MODULEID).setValue(Integer.toString(intModuleID));
                            cmdSetConfigBlock.getParameterList().get(INDEX_SET_CONFIG_BLOCK_BLOCKID).setValue(Utilities.intToFourHexString(blockid));
                            cmdSetConfigBlock.getParameterList().get(INDEX_SET_CONFIG_BLOCK_DATA).setValue(buffer.substring((blockid * StaribusParsers.LENGTH_STARIBUS_BLOCK),
                                                                                                           (blockid * StaribusParsers.LENGTH_STARIBUS_BLOCK) + StaribusParsers.LENGTH_STARIBUS_BLOCK));
                            LOGGER.debugTimedEvent(boolDebug,
                                                   SOURCE + "Calling setConfigurationBlock("
                                                        + cmdSetConfigBlock.getParameterList().get(INDEX_SET_CONFIG_BLOCK_MODULEID).getValue()
                                                        + ", "
                                                        + cmdSetConfigBlock.getParameterList().get(INDEX_SET_CONFIG_BLOCK_BLOCKID).getValue()
                                                        + ", "
                                                        + cmdSetConfigBlock.getParameterList().get(INDEX_SET_CONFIG_BLOCK_DATA).getValue()
                                                        + ")");

                            // This *assumes* that the Parameters are taken from the Command verbatim
                            strStarscript = StarscriptHelper.buildSimpleStarscript(xmlInstrument,
                                                                                   xmlController,
                                                                                   null,
                                                                                   cmdSetConfigBlock,
                                                                                   false);
                            dao.getResponseMessageStatusList().clear();
                            boolSuccessfulSubCommand = false;

                            for (int retryid = 0;
                                ((retryid < TimeoutHelper.RETRY_COUNT)
                                    && (!boolSuccessfulSubCommand)
                                    && (Utilities.retryCanProceed(dao, dao.getResponseMessageStatusList(), dao.getExecuteWorker())));
                                retryid++)
                                {
                                TimeoutHelper.logRetryEvent(dao, strStarscript, retryid);

                                // setModuleConfiguration() is a Local command which calls a SendToPort command
                                // executeCommandOnSameThread() does not provide timeout for Local,
                                // so restart the Timeout for the setModuleConfiguration() sub-Command
                                TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(xmlController, cmdSetConfigBlock));

                                // setModuleConfiguration() is running on the SwingWorker Thread in executeCommand()
                                // in the DAO, expecting data or an ABORT or TIMEOUT,
                                // so it is easier to execute sub-Commands in the same Thread!
                                // setConfigurationBlock() is a SendToPort command
                                // so executeCommandOnSameThread() will handle the Timeout
                                // Use the ResponseMessageStatus in the ResponseMessage
                                // If we get TIMEOUT, try again until retries exhausted
                                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                                responseMessage = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                                  dao,
                                                                                                  xmlInstrument,
                                                                                                  xmlController,
                                                                                                  cmdSetConfigBlock,
                                                                                                  // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                                  cmdSetConfigBlock.getParameterList(),
                                                                                                  strStarscript,
                                                                                                  errors,
                                                                                                  false,
                                                                                                  boolDebug);
                                // Are the data ok this time round?
                                boolSuccessfulSubCommand = ((responseMessage != null)
                                                            && (ResponseMessageStatus.wasResponseSuccessful(responseMessage))
                                                            && (responseMessage.getWrappedData() != null));

                                if (boolSuccessfulSubCommand)
                                    {
                                    // This is probably not necessary
                                    responseMessage.setWrappedData(null);

                                    boolSuccess = true;
                                    }
                                else
                                    {
                                    final int intStatus;

                                    // We don't make any use of the failed status from executeCommandOnSameThread()
                                    if (responseMessage != null)
                                        {
                                        intStatus = responseMessage.getStatusBits();
                                        }
                                    else
                                        {
                                        intStatus = 0;
                                        }

                                    LOGGER.debugTimedEvent(boolDebug,
                                                           SOURCE + " End of RETRY [retry=" + retryid + "] [status=" + Utilities.intToBitString(intStatus) + "]");
                                    // Reset success on every error
                                    boolSuccess = false;
                                    }
                                }
                            }

                        // The Success flag shows the status of the last block
                        }
                    else
                        {
                        // This should never occur!
                        if (boolCompressed)
                            {
                            LOGGER.error(SOURCE + "XML Configuration file is too long - try compression");
                            responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.FATAL,
                                                               METADATA_TARGET_MEMORY
                                                                   + METADATA_ACTION_WRITE + SPACE
                                                                   + METADATA_RESULT
                                                                   + responseMessageStatus.getName()
                                                                   + TERMINATOR_SPACE
                                                                   + METADATA_ORIGIN
                                                                       + "XML Configuration file is too long - try compression"
                                                                   + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "XML Configuration file is too long");
                            responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.FATAL,
                                                               METADATA_TARGET_MEMORY
                                                                   + METADATA_ACTION_WRITE + SPACE
                                                                   + METADATA_RESULT
                                                                   + responseMessageStatus.getName()
                                                                   + TERMINATOR_SPACE
                                                                   + METADATA_ORIGIN
                                                                       + "XML Configuration file is too long"
                                                                   + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            }

                        boolSuccess = false;
                        }
                    }
                else
                    {
                    // Something is wrong with the XML definition of setModuleConfiguration()
                    LOGGER.error(SOURCE + "SteppedCommand incorrectly set up in XML");
                    responseMessageStatus = DAOCommandHelper.logInvalidXML(dao,
                                                                           SOURCE,
                                                                           METADATA_TARGET_MEMORY,
                                                                           METADATA_ACTION_WRITE);
                    boolSuccess = false;
                    }
                }
            else
                {
                // Incorrectly configured XML
                LOGGER.error(SOURCE + "SteppedCommandCodeBase must refer to the Core; check XML configuration");
                responseMessageStatus = DAOCommandHelper.logInvalidXML(dao,
                                                                       SOURCE,
                                                                       METADATA_TARGET_MEMORY,
                                                                       METADATA_ACTION_WRITE);
                boolSuccess = false;
                }
            }

        //------------------------------------------------------------------------------------------
        // Construct an appropriate ResponseMessage

        if (boolSuccess)
            {
            final String strMode;

            if (boolCompressed)
                {
                strMode = "compressed";
                }
            else
                {
                strMode = "raw";
                }

            // These parameters are irrelevant here
//            dao.setRawDataChannelCount(CHANNEL_COUNT);
//            dao.setTemperatureChannel(false);

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                cmdSetModuleConfig);
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_MEMORY
                                                   + METADATA_ACTION_WRITE
                                                   + METADATA_RESULT_SUCCESS + SPACE
                                                   + METADATA_MODE
                                                   + strMode
                                                   + TERMINATOR_SPACE
                                                   + METADATA_LENGTH
                                                   + buffer.length()
                                                   + TERMINATOR_SPACE
                                                   + METADATA_RATIO
                                                       + ((intCompressedSize * 100) / intRawSize) + PERCENT
                                                   + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }
        else
            {
            // Create the failed ResponseMessage, adding the last Status received
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNullWithStatus(dao,
                                                                                            commandmessage,
                                                                                            cmdSetModuleConfig,
                                                                                            responseMessage,
                                                                                            responseMessageStatus);
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_MEMORY
                                                   + METADATA_ACTION_WRITE + SPACE
                                                   + METADATA_RESULT
                                                   + ResponseMessageStatus.expandResponseStatusCodes(responseMessage.getResponseMessageStatusList())
                                                   + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        // Tidy up, just in case!
        ObservatoryInstrumentHelper.runGarbageCollector();

        return (responseMessage);
        }


    /***********************************************************************************************
     * Check to see if the setModuleConfiguration() Command is configured correctly.
     *
     * @param setmoduleconfig
     * @param listparameters
     * @param xmlcontroller
     *
     * @return boolean
     */

    private static boolean isValidCommand(final CommandType setmoduleconfig,
                                          final List<ParameterType> listparameters,
                                          final XmlObject xmlcontroller)
        {
        final int PARAMETER_COUNT = 3;

        return ((setmoduleconfig != null)
            && (xmlcontroller != null)
            && (listparameters != null)
            && (listparameters.size() == PARAMETER_COUNT)
            && (listparameters.get(INDEX_SET_MODULE_CONFIG_MODULEID) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listparameters.get(INDEX_SET_MODULE_CONFIG_MODULEID).getInputDataType().getDataTypeName()))
            && (listparameters.get(INDEX_SET_MODULE_CONFIG_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listparameters.get(INDEX_SET_MODULE_CONFIG_FILENAME).getInputDataType().getDataTypeName()))
            && (listparameters.get(INDEX_SET_MODULE_CONFIG_COMPRESSED) != null)
            && (SchemaDataType.BOOLEAN.equals(listparameters.get(INDEX_SET_MODULE_CONFIG_COMPRESSED).getInputDataType().getDataTypeName()))
            && (setmoduleconfig.getSteppedDataCommandList() != null)
            && (setmoduleconfig.getSteppedDataCommandList().size() == 1));
        }


    /***********************************************************************************************
     * Check to see if the setModuleConfiguration() and setConfigurationBlock() Commands
     * are configured correctly.
     *
     * @param setmoduleconfig
     * @param setconfigblock
     * @param steppedcommandcodebase
     * @param steppedcommandcode
     *
     * @return boolean
     */

    private static boolean isValidSubCommand(final CommandType setmoduleconfig,
                                             final CommandType setconfigblock,
                                             final String steppedcommandcodebase,
                                             final String steppedcommandcode)
        {
        boolean boolValid;

        boolValid = ((setmoduleconfig != null)
                        && (setconfigblock != null));

        if (boolValid)
            {
            final SteppedCodes steppedCodes;

            // Retrieve the SteppedCommand to execute (we know there's only one in the list)
            steppedCodes = setmoduleconfig.getSteppedDataCommandList().get(0);

            // Check that the SteppedCodes refer to the Core
            boolValid = (CORE_COMMAND_CODE_BASE.equals(steppedCodes.getSteppedCommandCodeBase()));

            // We know that setConfigurationBlock() is in the Core, because of Xpath root node
            // Check that the CommandCodes match
            boolValid = ((boolValid)
                         && (setconfigblock.getCommandCode().equals(steppedcommandcode)));

            // Make sure it is setConfigurationBlock() so far as we can tell without assuming the name
            boolValid = ((boolValid)
                        && (setconfigblock.getParameterList() != null)
                        && (setconfigblock.getParameterList().size() == 3)
                        && (PARAMETER_CONFIGURATION_MODULEID.equals(setconfigblock.getParameterList().get(0).getName()))
                        && (PARAMETER_CONFIGURATION_BLOCKID.equals(setconfigblock.getParameterList().get(1).getName()))
                        && (PARAMETER_CONFIGURATION_DATA.equals(setconfigblock.getParameterList().get(2).getName())));
            }

        return (boolValid);
        }
    }
