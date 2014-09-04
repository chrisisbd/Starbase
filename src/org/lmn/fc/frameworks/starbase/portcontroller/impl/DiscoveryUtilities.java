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

package org.lmn.fc.frameworks.starbase.portcontroller.impl;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.StarscriptHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatusList;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * DiscoveryUtilities.
 */

public final class DiscoveryUtilities implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 FrameworkRegex
    {
    // String Resources
    public static final String DISCOVERY_CONTROLLER_IDENTIFIER = "DiscoveryController";

    public static final String INSERT_PLUGIN_MANIFEST = "[plugin_manifest]";
    public static final String INSERT_INSTRUMENT_PLUGINS = "[instrument_plugins]";
    public static final String INSERT_CONTROLLER_ADDRESS = "[controller_address]";
    public static final String INSERT_CONTROLLER_CORE = "[controller_core]";
    public static final String INSERT_CONTROLLER_PLUGINS = "[controller_plugins]";
    public static final String INSERT_CONTROLLER_END = "[controller_end]";
    private static final String MSG_INVALID_XML = "The Instrument XML is invalid";
    private static final String MSG_NO_CONTROLLER = "This Instrument does not have a Controller";
    private static final String MSG_PRIMARY_ERROR = "The PrimaryPlugin is not set correctly in the XML";
    private static final String MSG_NO_ADDRESS = "Unable to determine the Address of the Controller";
    private static final String MSG_BLANK_LINES = "\n\n";

    private static final String CORE_MODULE_NAME = "Core";

    private static final String COMMAND_CODE_BASE = "00";

    private static final String CODE_RESET = "00";
    private static final String CODE_PING = "01";
    private static final String CODE_GET_CONFIG = "02";
    private static final String CODE_GET_MODULE_CONFIG = "03";
    private static final String CODE_GET_CONFIG_BLOCK_COUNT = "04";
    private static final String CODE_GET_CONFIG_BLOCK = "05";

    private static final String COMMAND_VARIANT = "0000";
    private static final String CORE_COMMAND_CODE_BASE = "00";

    private static final int INDEX_GET_MODULE_CONFIG_MODULEID = 0;
    private static final int MODULE_COUNT = 8;
    private static final int CONTROLLER_MODULE_ID = 0;
    private static final int PRIMARY_MODULE_ID = 1;



    private static final int INDEX_GET_CONFIG_BLOCK_COUNT_MODULEID = 0;
    private static final int INDEX_GET_CONFIG_BLOCK_MODULEID = 0;
    private static final int INDEX_GET_CONFIG_BLOCK_BLOCKID = 1;



    /**********************************************************************************************/
    /* This must be kept in step with any changes to the Commands in the real Core XML!           */
    /***********************************************************************************************
     * Create the discovery Core XML description of the Staribus Instrument,
     * containing the following Commands:
     *
     * reset()
     * ping()
     * getConfiguration()
     * getModuleConfiguration()
     * getConfigurationBlockCount()
     * getConfigurationBlock()
     *
     * @param address
     *
     * @return Instrument
     */

    public static Instrument createStaribusDiscoveryInstrumentControllerCoreXml(final int address)
        {
        final Instrument instrumentXML;
        final Controller controller;
        final Controller.StaribusAddress addressController;
        final CommandType cmdReset;
        final CommandType cmdPing;
        final CommandType cmdGetConfiguration;
        final CommandType cmdGetModuleConfiguration;
        final CommandType cmdGetConfigurationBlockCount;
        final CommandType cmdGetConfigurationBlock;
        ParameterType parameter;

        // This is the only place where a new Instrument is made
        instrumentXML = Instrument.Factory.newInstance();
        instrumentXML.setIdentifier(DISCOVERY_CONTROLLER_IDENTIFIER);
        instrumentXML.setName("Discovery Controller");
        instrumentXML.setDescription("A temporary Controller used during Instrument discovery");
        // This Instrument will never be instantiated
        instrumentXML.setInstrumentClassname("x");
        instrumentXML.setControllable(true);
        instrumentXML.setSelectorPanelHeight(1);
        instrumentXML.setResourceKey(DISCOVERY_CONTROLLER_IDENTIFIER);

        // A DAO is not required during discovery

        controller = instrumentXML.addNewController();

        // BEWARE! The Address must be three digits, with leading zeroes
        // so we can't use setAddress(int)
        addressController = Controller.StaribusAddress.Factory.newInstance();
        addressController.setStringValue(Utilities.intToString(address, 10, 3));

        controller.xsetStaribusAddress(addressController);
        controller.setIdentifier(CORE_MODULE_NAME);
        controller.setName(CORE_MODULE_NAME);
        controller.setDescription("The Discovery Controller Core");
        controller.setResourceKey(CORE_MODULE_NAME);

        controller.setCommandCodeBase(COMMAND_CODE_BASE);

        // Now create each of the Commands required during Discovery

        //------------------------------------------------------------------------------------------
        // reset()

        cmdReset = controller.addNewCommand();
        cmdReset.setIdentifier(COMMAND_CORE_RESET);
        cmdReset.setCommandCode(CODE_RESET);
        cmdReset.setCommandVariant(COMMAND_VARIANT);
        cmdReset.setDescription("Resets the DiscoveryController");
        cmdReset.setSendToPort(true);
        cmdReset.addNewAck();
        cmdReset.getAck().setName(RESPONSE_ACK);

        //------------------------------------------------------------------------------------------
        // ping()

        cmdPing = controller.addNewCommand();
        cmdPing.setIdentifier(COMMAND_CORE_PING);
        cmdPing.setCommandCode(CODE_PING);
        cmdPing.setCommandVariant(COMMAND_VARIANT);
        cmdPing.setDescription("Pings the DiscoveryController");
        cmdPing.setSendToPort(true);
        cmdPing.addNewAck();
        cmdPing.getAck().setName(RESPONSE_ACK);

        //------------------------------------------------------------------------------------------
        // getConfiguration()

        cmdGetConfiguration = controller.addNewCommand();
        cmdGetConfiguration.setIdentifier(COMMAND_CORE_GET_CONFIGURATION);
        cmdGetConfiguration.setCommandCode(CODE_GET_CONFIG);
        cmdGetConfiguration.setCommandVariant(COMMAND_VARIANT);
        // TODO Review use of SteppedDataCommand
        cmdGetConfiguration.addNewSteppedDataCommand();
        cmdGetConfiguration.getSteppedDataCommandArray(0).setSteppedCommandCodeBase(COMMAND_CODE_BASE);
        cmdGetConfiguration.getSteppedDataCommandArray(0).setSteppedCommandCode(CODE_GET_MODULE_CONFIG);
        cmdGetConfiguration.setDescription("Gets the assembled XML configuration of the DiscoveryController");
        cmdGetConfiguration.setSendToPort(false);
        cmdGetConfiguration.addNewResponse();
        cmdGetConfiguration.getResponse().setName(RESPONSE_CONFIGURATION_XML);
        cmdGetConfiguration.getResponse().setDataTypeName(SchemaDataType.XML);
        cmdGetConfiguration.getResponse().setUnits(SchemaUnits.DIMENSIONLESS);

        //------------------------------------------------------------------------------------------
        // getModuleConfiguration()

        cmdGetModuleConfiguration = controller.addNewCommand();
        cmdGetModuleConfiguration.setIdentifier(COMMAND_CORE_GET_MODULE_CONFIGURATION);
        cmdGetModuleConfiguration.setCommandCode(CODE_GET_MODULE_CONFIG);
        cmdGetModuleConfiguration.setCommandVariant(COMMAND_VARIANT);
        cmdGetModuleConfiguration.addNewBlockedDataCommand();
        cmdGetModuleConfiguration.getBlockedDataCommand().setBlockCountCommandCode(CODE_GET_CONFIG_BLOCK_COUNT);
        cmdGetModuleConfiguration.getBlockedDataCommand().setBlockCommandCode(CODE_GET_CONFIG_BLOCK);
        cmdGetModuleConfiguration.setDescription("Gets the Module configuration data, as an XML fragment");

        parameter = cmdGetModuleConfiguration.addNewParameter();
        parameter.setName(PARAMETER_CONFIGURATION_MODULEID);
        parameter.addNewInputDataType();
        parameter.getInputDataType().setDataTypeName(SchemaDataType.DECIMAL_INTEGER);
        parameter.addNewTrafficDataType();
        parameter.getTrafficDataType().setDataTypeName(SchemaDataType.STRING);
        parameter.setUnits(SchemaUnits.DIMENSIONLESS);
        parameter.setRegex(REGEX_MODULEID);
        parameter.setTooltip("Allowed Values: ModuleID 0 to 7");

        cmdGetModuleConfiguration.setSendToPort(true);
        cmdGetModuleConfiguration.addNewResponse();
        cmdGetModuleConfiguration.getResponse().setName(RESPONSE_CONFIGURATION_MODULE);
        cmdGetModuleConfiguration.getResponse().setDataTypeName(SchemaDataType.XML);
        cmdGetModuleConfiguration.getResponse().setUnits(SchemaUnits.DIMENSIONLESS);

        //------------------------------------------------------------------------------------------
        // getConfigurationBlockCount()

        cmdGetConfigurationBlockCount = controller.addNewCommand();
        cmdGetConfigurationBlockCount.setIdentifier(COMMAND_CORE_GET_CONFIGURATION_BLOCK_COUNT);
        cmdGetConfigurationBlockCount.setCommandCode(CODE_GET_CONFIG_BLOCK_COUNT);
        cmdGetConfigurationBlockCount.setCommandVariant(COMMAND_VARIANT);
        cmdGetConfigurationBlockCount.addNewBlockedDataCommand();
        cmdGetConfigurationBlockCount.getBlockedDataCommand().setParentCommandCode(CODE_GET_MODULE_CONFIG);
        cmdGetConfigurationBlockCount.setDescription("Gets the number of blocks of data in the Module configuration");

        parameter = cmdGetConfigurationBlockCount.addNewParameter();
        parameter.setName(PARAMETER_CONFIGURATION_MODULEID);
        parameter.addNewInputDataType();
        parameter.getInputDataType().setDataTypeName(SchemaDataType.DECIMAL_INTEGER);
        parameter.addNewTrafficDataType();
        parameter.getTrafficDataType().setDataTypeName(SchemaDataType.STRING);
        parameter.setUnits(SchemaUnits.DIMENSIONLESS);
        parameter.setRegex(REGEX_MODULEID);
        parameter.setTooltip("Allowed Values: ModuleID 0 to 7");

        cmdGetConfigurationBlockCount.setSendToPort(true);
        cmdGetConfigurationBlockCount.addNewResponse();
        cmdGetConfigurationBlockCount.getResponse().setName(RESPONSE_CONFIGURATION_BLOCK_COUNT);
        cmdGetConfigurationBlockCount.getResponse().setDataTypeName(SchemaDataType.HEX_INTEGER);
        cmdGetConfigurationBlockCount.getResponse().setUnits(SchemaUnits.DIMENSIONLESS);
        cmdGetConfigurationBlockCount.getResponse().setRegex(REGEX_HEX_INTEGER_FOUR);

        //------------------------------------------------------------------------------------------
        // getConfigurationBlock()

        cmdGetConfigurationBlock = controller.addNewCommand();
        cmdGetConfigurationBlock.setIdentifier(COMMAND_CORE_GET_CONFIGURATION_BLOCK);
        cmdGetConfigurationBlock.setCommandCode(CODE_GET_CONFIG_BLOCK);
        cmdGetConfigurationBlock.setCommandVariant(COMMAND_VARIANT);
        cmdGetConfigurationBlock.addNewBlockedDataCommand();
        cmdGetConfigurationBlock.getBlockedDataCommand().setParentCommandCode(CODE_GET_MODULE_CONFIG);
        cmdGetConfigurationBlock.setDescription("Gets the specified block of Module configuration data, as an XML fragment");

        parameter = cmdGetConfigurationBlock.addNewParameter();
        parameter.setName(PARAMETER_CONFIGURATION_MODULEID);
        parameter.addNewInputDataType();
        parameter.getInputDataType().setDataTypeName(SchemaDataType.DECIMAL_INTEGER);
        parameter.addNewTrafficDataType();
        parameter.getTrafficDataType().setDataTypeName(SchemaDataType.STRING);
        parameter.setUnits(SchemaUnits.DIMENSIONLESS);
        parameter.setRegex(REGEX_MODULEID);
        parameter.setTooltip("Allowed Values: ModuleID 0 to 7");

        parameter = cmdGetConfigurationBlock.addNewParameter();
        parameter.setName(PARAMETER_CONFIGURATION_BLOCKID);
        parameter.addNewInputDataType();
        parameter.getInputDataType().setDataTypeName(SchemaDataType.HEX_INTEGER);
        parameter.addNewTrafficDataType();
        parameter.getTrafficDataType().setDataTypeName(SchemaDataType.STRING);
        parameter.setUnits(SchemaUnits.DIMENSIONLESS);
        parameter.setRegex(REGEX_HEX_INTEGER_FOUR);
        parameter.setTooltip("Allowed Values: BlockID 0000 to FFFF");

        cmdGetConfigurationBlock.setSendToPort(true);
        cmdGetConfigurationBlock.addNewResponse();
        cmdGetConfigurationBlock.getResponse().setName(RESPONSE_CONFIGURATION_BLOCK);
        cmdGetConfigurationBlock.getResponse().setDataTypeName(SchemaDataType.XML);
        cmdGetConfigurationBlock.getResponse().setUnits(SchemaUnits.DIMENSIONLESS);

        return (instrumentXML);
        }


    /***********************************************************************************************
     * Stitch together the Controller, Primary and Secondary XML fragments,
     * validating the composite XML document, returning it in result.
     * Return ResponseMessageStatus.INVALID_INSTRUMENT on failure.
     *
     * @param controller
     * @param primary
     * @param secondaries
     * @param result
     * @param debug
     *
     * @return String
     */

    public static ResponseMessageStatus stitchXML(final StringBuffer controller,
                                                  final StringBuffer primary,
                                                  final StringBuffer secondaries,
                                                  final StringBuffer result,
                                                  final boolean debug)
        {
        final String SOURCE = "StaribusHelper.stitchXML() ";
        ResponseMessageStatus responseMessageStatus;

        // It is permissable to have no Secondary Plugins
        // All assembly markers must be present
        if ((controller != null)
            && (controller.length() > 0)
            && (controller.indexOf(INSERT_PLUGIN_MANIFEST) >= 0)
            && (controller.indexOf(INSERT_INSTRUMENT_PLUGINS) >= 0)
            && (primary != null)
            && (primary.length() > 0)
            && (primary.indexOf(INSERT_CONTROLLER_ADDRESS) >= 0)
            && (primary.indexOf(INSERT_CONTROLLER_CORE) >= 0)
            && (primary.indexOf(INSERT_CONTROLLER_PLUGINS) >= 0)
            && (primary.indexOf(INSERT_CONTROLLER_END) >= 0)
            && (secondaries != null)
            && (result != null))
            {
            try
                {
                int intInsertionIndex;
                int intSourceStartIndex;
                int intSourceEndIndex;

                // Remove any history in the result
                result.setLength(0);

                // We start from the structure of the Primary Plugin,
                // which has markers [controller_address] and [controller_core]
                // followed by [controller_plugins] and [controller_end]
                // Use only the result buffer from here on
                result.append(primary);

                LOGGER.debug(debug,
                             "\n\nPRIMARY PLUGIN XML = [[" + result + "]]\n\n");

                // Find the first insertion point, [controller_address]
                intInsertionIndex = result.indexOf(INSERT_CONTROLLER_ADDRESS);

                // ...and the indexes of the chunk to insert
                intSourceStartIndex = 0;
                intSourceEndIndex = controller.indexOf(INSERT_PLUGIN_MANIFEST);

                // Validate all indexes
                if ((intInsertionIndex >= 0)
                    && (intSourceStartIndex >= 0)
                    && (intSourceEndIndex >= 0)
                    && (intSourceEndIndex > intSourceStartIndex))
                    {
                    // Remove the insertion tag
                    // The end index is exclusive
                    result.delete(intInsertionIndex, intInsertionIndex + INSERT_CONTROLLER_ADDRESS.length());

                    // Insert the Controller Address
                    // The end index is exclusive
                    result.insert(intInsertionIndex, controller.substring(intSourceStartIndex, intSourceEndIndex));

                    // Find the next insertion point, [controller_core]
                    intInsertionIndex = result.indexOf(INSERT_CONTROLLER_CORE);

                    // ...and the indexes of the chunk to insert
                    intSourceStartIndex = controller.indexOf(INSERT_PLUGIN_MANIFEST) + INSERT_PLUGIN_MANIFEST.length();
                    intSourceEndIndex = controller.indexOf(INSERT_INSTRUMENT_PLUGINS);

                    // Validate all indexes
                    if ((intInsertionIndex >= 0)
                        && (intSourceStartIndex >= 0)
                        && (intSourceEndIndex >= 0)
                        && (intSourceEndIndex > intSourceStartIndex))
                        {
                        // Remove the insertion tag
                        result.delete(intInsertionIndex, intInsertionIndex + INSERT_CONTROLLER_CORE.length());

                        // Insert the Controller Core
                        result.insert(intInsertionIndex, controller.substring(intSourceStartIndex, intSourceEndIndex));

                        // We are now pointing at the *end* of the list of Commands, waiting for the first Plugin
                        // Next in the buffer is the list of Plugins supplied by the Primary

                        // We want to put SecondaryPlugins, if any, just before the Controller Plugins
                        // so use the same insertion tag, [controller_plugins]
                        // which will take us to just after the Primary Plugins
                        if (secondaries.length() > 0)
                            {
                            // Find the next insertion point, [controller_plugins]
                            intInsertionIndex = result.indexOf(INSERT_CONTROLLER_PLUGINS);

                            // ...and the indexes of the chunk to insert
                            // Start at the beginning of the secondaries
                            intSourceStartIndex = 0;

                            // Copy up to the end of the secondaries
                            intSourceEndIndex = secondaries.length();

                            // Validate all indexes
                            if ((intInsertionIndex >= 0)
                                && (intSourceStartIndex >= 0)
                                && (intSourceEndIndex >= 0)
                                && (intSourceEndIndex > intSourceStartIndex))
                                {
                                // Insert the Secondary Plugins, leaving the [controller_plugins] tag
                                result.insert(intInsertionIndex, secondaries.substring(intSourceStartIndex, intSourceEndIndex));
                                }
                            }

                         // Now copy the rest of the Controller Plugins
                        intInsertionIndex = result.indexOf(INSERT_CONTROLLER_PLUGINS);
                        intSourceStartIndex = controller.indexOf(INSERT_INSTRUMENT_PLUGINS) + INSERT_INSTRUMENT_PLUGINS.length();

                        // Copy up to the end of the controller
                        intSourceEndIndex = controller.length();

                        // Validate all indexes
                        if ((intInsertionIndex >= 0)
                            && (intSourceStartIndex >= 0)
                            && (intSourceEndIndex >= 0)
                            && (intSourceEndIndex > intSourceStartIndex))
                            {
                            final InstrumentsDocument docInstruments;

                            // Remove the insertion tags
                            result.delete(intInsertionIndex, intInsertionIndex + INSERT_CONTROLLER_PLUGINS.length());
                            result.delete(intInsertionIndex, intInsertionIndex + INSERT_CONTROLLER_END.length());

                            // Insert the Controller Plugins and End tag
                            result.insert(intInsertionIndex, controller.substring(intSourceStartIndex, intSourceEndIndex));

                            LOGGER.debug(debug,
                                         "\n\nASSEMBLED XML RESULT = [[" + result + "]]\n\n");

                            // The XML document in result should now be a valid Instrument, so try to parse it
                            docInstruments = InstrumentsDocument.Factory.parse(result.toString());

                            // Now validate the XML document
                            if (XmlBeansUtilities.isValidXml(docInstruments))
                                {
                                LOGGER.log(SOURCE + "Instrument XML has been assembled successfully!");
                                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Assembled Instrument XML is invalid");
                                result.append(EMPTY_STRING);
                                responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                                }
                            }
                        else
                            {
                            LOGGER.error("Cannot find " + INSERT_CONTROLLER_PLUGINS);
                            responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                            }
                        }
                    else
                        {
                        LOGGER.error("Cannot find " + INSERT_CONTROLLER_CORE + " or " + INSERT_INSTRUMENT_PLUGINS);
                        responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                        }
                    }
                else
                    {
                    LOGGER.error("Cannot find " + INSERT_CONTROLLER_ADDRESS + " or " + INSERT_PLUGIN_MANIFEST);
                    responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                    }
                }

            catch (final NullPointerException exception)
                {
                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                result.append(EMPTY_STRING);
                responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                }

            catch (final StringIndexOutOfBoundsException exception)
                {
                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                result.append(EMPTY_STRING);
                responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                }

            catch (final XmlException exception)
                {
                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                result.append(EMPTY_STRING);
                responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Some XML is missing, or the routine has already failed");
            responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Test to see if the specified getConfiguration() Command refers to a Virtual Instrument.
     *
     * @param command
     *
     * @return boolean
     */

    public static boolean isGetConfigurationVirtualInstrument(final CommandType command)
        {
        boolean boolResult;

        boolResult = (command.getBlockedDataCommand() == null);

        boolResult = boolResult
                        && (command.getIteratedDataCommand() == null);

        boolResult = boolResult
                        && ((command.getSteppedDataCommandList() == null)
                                || (command.getSteppedDataCommandList().isEmpty()));

        return (boolResult);
        }


    /***********************************************************************************************
     * Assemble the XML fragments from the Controller (optional), and Primary and Secondary Plugins.
     * If the Controller should not be included, use the existing XML in memory.
     * Return the assembled XML if valid, or ResponseMessageStatus.RESPONSE_NODATA on failure.
     *
     * @param dao
     * @param command
     * @param instrument
     * @param controller
     * @param discovercontroller
     * @param debug
     *
     * @return String
     */

    public static String assembleXmlFragments(final ObservatoryInstrumentDAOInterface dao,
                                              final CommandType command,
                                              final Instrument instrument,
                                              final XmlObject controller,
                                              final boolean discovercontroller,
                                              final boolean debug)
        {
        final String SOURCE = "DiscoveryUtilities.assembleXmlFragments() ";
        String strResponseValue;
        final String strSteppedCommandCodeBase;
        final SteppedCodes steppedCodes;

        // Prepare an error response
        strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;

        // Retrieve the Command to execute (we know there's only one in the list)
        steppedCodes = command.getSteppedDataCommandList().get(0);

        strSteppedCommandCodeBase = steppedCodes.getSteppedCommandCodeBase();

        // The commands must be in the Core for this Command to work
        if (CORE_COMMAND_CODE_BASE.equals(strSteppedCommandCodeBase))
            {
            final String strSteppedCommandCode;
            CommandType cmdGetModuleConfig;
            final StringBuffer expression;
            final XmlObject[] selection;

            strSteppedCommandCode = steppedCodes.getSteppedCommandCode();

            LOGGER.debugTimedEvent(debug,
                                   SOURCE + "[SteppedCommandCodeBase=" + strSteppedCommandCodeBase + "]"
                                        + "[SteppedCommandCode=" + strSteppedCommandCode + "]");

            // Find the Command to which these codes relate (which we know is in the Core)
            cmdGetModuleConfig = null;
            expression = new StringBuffer();

            // The CommandCode identifies the specific Command
            // The XML holds these values as two-character Hex numbers
            // Find the getModuleConfiguration() Command
            expression.setLength(0);
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_COMMAND_FROM_CC);
            // The CommandCode gives the CommandType
            expression.append(strSteppedCommandCode);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            // Query from the root of the Controller (i.e. the Core Module)
            selection = controller.selectPath(expression.toString());

            if ((selection != null)
                && (selection instanceof CommandType[])
                && (selection.length == 1)
                && (selection[0] != null)
                && (selection[0] instanceof CommandType))
                {
                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "SteppedCommandCode CommandType=" + selection[0].xmlText());

                // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                cmdGetModuleConfig = (CommandType)selection[0].copy();
                }

            //----------------------------------------------------------------------------------
            // Check as much as we can about the sub-Command and then execute it
            // There's no need to check the CommandVariant

            if (isValidConfigurationCommand(command,
                                            cmdGetModuleConfig,
                                            strSteppedCommandCodeBase,
                                            strSteppedCommandCode))
                {
                final List<String> errors;
                final StringBuffer bufferController;
                final StringBuffer bufferPrimary;
                final StringBuffer bufferSecondaries;
                final StringBuffer bufferResult;
                final ResponseMessageStatusList listStatusModuleReadInLoop;
                boolean boolSuccess;

                // Execute the loop to assemble the data blocks
                LOGGER.debugTimedEvent(debug,
                                       SOURCE + "Command validated, assembling data...");

                errors = new ArrayList<String>(10);
                bufferController = new StringBuffer();
                bufferPrimary = new StringBuffer();
                bufferSecondaries = new StringBuffer();
                bufferResult = new StringBuffer();
                boolSuccess = true;

                listStatusModuleReadInLoop = ResponseMessageStatus.createResponseMessageStatusList();

                // At this point we are using the DAOTimer for the Local Command getConfiguration()

                // Get the configuration from each Module in turn
                // If a Module is missing, just move to the next
                for (int intModuleID = 0;
                     ((boolSuccess) && (intModuleID < MODULE_COUNT));
                     intModuleID++)
                    {
                    // See if the Controller should be discovered, or just taken from memory
                    if ((intModuleID == 0)
                        && (!discovercontroller))
                        {
                        // Take the Controller from memory, and create text in the same format as writeConfiguration(),
                        // which was the format written to the Plugin memories

                        LOGGER.debugTimedEvent(debug,
                                               SOURCE + "Controller XML taken directly from the Observatory");
                        bufferController.append(formatControllerXML(dao.getHostInstrument().getInstrument(),
                                                                    true,
                                                                    errors));

                        listStatusModuleReadInLoop.add(ResponseMessageStatus.SUCCESS);
                        }

                    // Otherwise discover everything...
                    else
                        {
                        final ResponseMessageInterface responseModuleConfig;

                        // getConfiguration() is running on the SwingWorker Thread in executeCommand()
                        // in the DAO, expecting data or an ABORT or TIMEOUT,
                        // so it is easier to execute sub-Commands in the same Thread!

                        // Read each chunk of data and assemble into the final Response
                        // Terminate if any sub-Command fails to complete,
                        // since the overall XML would then not be parseable

                        // Re-use the existing Parameter object
                        // This was proved not NULL in isValidConfigurationCommand()
                        cmdGetModuleConfig.getParameterList().get(INDEX_GET_MODULE_CONFIG_MODULEID).setValue(Integer.toString(intModuleID));

                        LOGGER.debugTimedEvent(debug,
                                               SOURCE + "Calling getModuleConfiguration("
                                               + cmdGetModuleConfig.getParameterList().get(INDEX_GET_MODULE_CONFIG_MODULEID).getValue() + ")");

                        // getModuleConfiguration() is a Local command which calls a SendToPort command
                        // executeCommandOnSameThread() does not provide timeout for Local,
                        // so restart the Timeout for the getModuleConfiguration() sub-Command
                        TimeoutHelper.restartDAOTimeoutTimer(dao, dao.getTimeoutMillis(controller, cmdGetModuleConfig));

                        // Retries of LOCAL Commands are not required
                        // WARNING Make sure a **copy** of the Command is executed, NOT the one in the Instrument XML!
                        responseModuleConfig = ExecuteCommandHelper.executeCommandOnSameThread(dao.getHostInstrument(),
                                                                                               dao,
                                                                                               instrument,
                                                                                               controller,
                                                                                               cmdGetModuleConfig,
                                                                                               // This *assumes* that the Parameters are taken from the Command verbatim
                                                                                               cmdGetModuleConfig.getParameterList(),
                                                                                               StarscriptHelper.buildSimpleStarscript(instrument,
                                                                                                                                      controller,
                                                                                                                                      null,
                                                                                                                                      cmdGetModuleConfig,
                                                                                                                                      false),
                                                                                               errors,
                                                                                               false,
                                                                                               debug);

                        // TODO WARNING!! responseModuleConfig must return with non-null DAOWrapper

                        // If we received INVALID_MODULE, there's no hardware, so just move on...
                        // WARNING! Test INVALID_MODULE first!
                        if (ResponseMessageStatus.wasResponseInvalidModule(responseModuleConfig))
                            {
                            LOGGER.debugTimedEvent(debug,
                                                   SOURCE + "Skipping missing Module [moduleid=" + intModuleID + "]");

                            // This wasn't a real failure
                            listStatusModuleReadInLoop.add(ResponseMessageStatus.SUCCESS);
                            }

                        // The Command must be completed without error or timeout
                        // Allow only SUCCESS, BUSY and CAPTURE_ACTIVE to represent a success
                        else if ((responseModuleConfig != null)
                                 && (ResponseMessageStatus.wasResponseSuccessful(responseModuleConfig))
                                 && (responseModuleConfig.getWrappedData() != null)
                                 && (responseModuleConfig.getWrappedData().getResponseValue() != null))
                            {
                            // Were there any data?
                            if (!ResponseMessageStatus.RESPONSE_NODATA.equals(responseModuleConfig.getWrappedData().getResponseValue()))
                                {
                                LOGGER.debugTimedEvent(debug,
                                                       SOURCE + "getModuleConfiguration() SUCCESS, appending data from Module [moduleid=" + intModuleID + "]");

                                if (intModuleID == CONTROLLER_MODULE_ID)
                                    {
                                    bufferController.append(responseModuleConfig.getWrappedData().getResponseValue());
                                    }
                                else if (intModuleID == PRIMARY_MODULE_ID)
                                    {
                                    bufferPrimary.append(responseModuleConfig.getWrappedData().getResponseValue());
                                    }
                                else
                                    {
                                    bufferSecondaries.append(responseModuleConfig.getWrappedData().getResponseValue());
                                    }
                                }
                            else
                                {
                                LOGGER.log(SOURCE + "getModuleConfiguration() Missing hardware, or no data found in Module [moduleid=" + intModuleID + "]");
                                }

                            // This is probably not necessary
                            responseModuleConfig.setWrappedData(null);

                            // This might be the last time through, so say it is successful
                            listStatusModuleReadInLoop.add(ResponseMessageStatus.SUCCESS);
                            }

                        // ...but terminate the loop on a real failure
                        else
                            {
                            LOGGER.debugTimedEvent(debug,
                                                   SOURCE + "getModuleConfiguration() failed [moduleid=" + intModuleID + "]");
                            bufferController.setLength(0);
                            bufferPrimary.setLength(0);
                            bufferSecondaries.setLength(0);
                            boolSuccess = false;

                            if ((responseModuleConfig != null)
                                && (responseModuleConfig.getResponseMessageStatusList() != null)
                                && (!responseModuleConfig.getResponseMessageStatusList().isEmpty()))
                                {
                                // Find out what the ResponseMessage told us
                                listStatusModuleReadInLoop.addAll(responseModuleConfig.getResponseMessageStatusList());
                                }
                            else
                                {
                                // We don't know what happened...
                                listStatusModuleReadInLoop.add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }
                            }
                        }
                    }

                //---------------------------------------------------------------------------------
                // Stitch the XML together, but only if everything worked ok

                if (listStatusModuleReadInLoop.contains(ResponseMessageStatus.SUCCESS))
                    {
                    // Remove the existing SUCCESS in case it fails again!
                    listStatusModuleReadInLoop.remove(ResponseMessageStatus.SUCCESS);

                    dao.getResponseMessageStatusList().add(stitchXML(bufferController,
                                                                     bufferPrimary,
                                                                     bufferSecondaries,
                                                                     bufferResult,
                                                                     debug));
                    }
                else
                    {
                    // Something failed already, so make a note of the error
                    dao.getResponseMessageStatusList().addAll(listStatusModuleReadInLoop);
                    }

                // Capture the final Status and ResponseValue (these may show a failure)
                strResponseValue = bufferResult.toString();
                }
            else
                {
                // Something is wrong with the XML definition of getConfiguration()
                LOGGER.error(SOURCE + "SteppedCommand incorrectly set up in XML");
                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                      SOURCE,
                                                                                      METADATA_TARGET_CONFIGURATION,
                                                                                      METADATA_ACTION_ASSEMBLE));
                }
            }
        else
            {
            // Incorrectly configured XML
            LOGGER.error(SOURCE + "SteppedCommandCodeBase must refer to the Core; check XML configuration");
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                  SOURCE,
                                                                                  METADATA_TARGET_CONFIGURATION,
                                                                                  METADATA_ACTION_ASSEMBLE));
            }

        return (strResponseValue);
        }


    /***********************************************************************************************
     * Write the Controller structure and all Controller Commands, less the PluginManifest.
     * Write those Plugins marked as having a Provider of 'Controller' .
     * Assume the Instrument XML and errors are not NULL.
     * Do NOT generate the ETX marker.
     * See similar code in writeConfigurationXML().
     *
     * @param instrument
     * @param compressed
     * @param errors
     *
     * @return StringBuffer
     */

    private static StringBuffer formatControllerXML(final Instrument instrument,
                                                    final boolean compressed,
                                                    final List<String> errors)
        {
        final String SOURCE = "DiscoveryUtilities.formatControllerXML() ";
        StringBuffer bufferOutput;

        bufferOutput = new StringBuffer();

        // There must be a Controller, otherwise no Commands or Plugins are possible
        if (instrument.getController() != null)
            {
            // WARNING! Do not close opening tag because of potential namespace declaration
            final String CONTROLLER_START = "<ins:Controller";
            final String CONTROLLER_ADDRESS_STARIBUS = "</ins:StaribusAddress>";
            final String CONTROLLER_ADDRESS_IP = "</ins:IPAddress>";
            final String CONTROLLER_ADDRESS_VIRTUAL = "</ins:VirtualAddress>";
            final String CONTROLLER_IDENTIFIER = "<ins:Identifier";
            // Allow the closing tag on this one, to avoid confusion with the Manifest
            final String PLUGIN_START = "<ins:Plugin>";
            final String PLUGIN_END = "</ins:Plugin>";
            final String CONTROLLER_END = "</ins:Controller>";
            StringBuffer bufferInput;
            final String strAddressEndTag;
            final int intStartIndex;
            final int intEndIndex;

            // Start with the entire Instrument XML
            bufferInput = new StringBuffer(instrument.xmlText());

            // Which kind of Address are we looking for?
            if ((instrument.getController().getStaribusAddress() != null)
                && (instrument.getController().getStaribusAddress().length() == 3)
                && (ObservatoryInstrumentHelper.isStaribusController(instrument)))
                {
                strAddressEndTag = CONTROLLER_ADDRESS_STARIBUS;
                }
            else if ((instrument.getController().getIPAddress() != null)
                     && (instrument.getController().getIPAddress().length()== "000.000.000.000:99999".length())
                     && (ObservatoryInstrumentHelper.isEthernetController(instrument)))
                {
                strAddressEndTag = CONTROLLER_ADDRESS_IP;
                }
            else if ((instrument.getController().getVirtualAddress() != null)
                     && (instrument.getController().getVirtualAddress().length() == 3)
                     && (ObservatoryInstrumentHelper.isVirtualController(instrument)))
                {
                // We wouldn't expect to run this Command for a Virtual Instrument
                strAddressEndTag = CONTROLLER_ADDRESS_VIRTUAL;
                }
            else
                {
                // This is an error
                LOGGER.error(SOURCE + MSG_NO_ADDRESS);
                strAddressEndTag = EMPTY_STRING;
                }

            intStartIndex = bufferInput.indexOf(CONTROLLER_START);
            intEndIndex = bufferInput.indexOf(strAddressEndTag);

            if ((intStartIndex >= 0)
                && (intEndIndex >= 0)
                && (intEndIndex > intStartIndex))
                {
                // Copy from <ins:Controller> to just after </ins:xxAddress>
                bufferOutput.append(bufferInput.substring(intStartIndex,
                                                          intEndIndex + strAddressEndTag.length()));

                // Tell the assembler where to insert the PluginManifest
                bufferOutput.append(INSERT_PLUGIN_MANIFEST);

                // Discard the beginning, to step over the *Instrument* <ins:Identifier>
                bufferInput = new StringBuffer(bufferInput.substring(intEndIndex));

                // Copy from the *Controller* <ins:Identifier>Core to just before <ins:Plugin>
                bufferOutput.append(bufferInput.substring(bufferInput.indexOf(CONTROLLER_IDENTIFIER),
                                                          bufferInput.indexOf(PLUGIN_START)));

                // Even if this Controller has no Plugins, this is the place to add more
                // Ideally the Primary Plugins should come before the Secondaries
                bufferOutput.append(INSERT_INSTRUMENT_PLUGINS);

                // Now only take those Plugins marked as having a Provider of 'Controller'
                if ((instrument.getController().getPluginList() != null)
                    && (!instrument.getController().getPluginList().isEmpty()))
                    {
                    int intPluginStartIndex;
                    final Iterator<PluginType> iterPlugins;

                    // Discard all the foregoing text, to begin at the first potential Plugin
                    intPluginStartIndex = bufferInput.indexOf(PLUGIN_START);
                    bufferInput = new StringBuffer(bufferInput.substring(intPluginStartIndex));

                    iterPlugins = instrument.getController().getPluginList().iterator();

                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;
                        final int intPluginEndIndex;

                        plugin = iterPlugins.next();

                        intPluginStartIndex = bufferInput.indexOf(PLUGIN_START);

                        // Find the end of this Plugin regardless of its type
                        intPluginEndIndex = bufferInput.indexOf(PLUGIN_END) + PLUGIN_END.length();

                        if ((intPluginStartIndex >= 0)
                            && (intPluginEndIndex >= 0)
                            && (intPluginEndIndex > intPluginStartIndex)
                            && (plugin.getProvider() != null)
                            && (PluginProvider.CONTROLLER.equals(plugin.getProvider())))
                            {
                            bufferOutput.append(bufferInput.substring(intPluginStartIndex,
                                                                      intPluginEndIndex));
                            // Make it easier to read if not compressed
                            bufferOutput.append(MSG_BLANK_LINES);
                            }

                        // Move to the start of the next Plugin, which could occur immediately after the end of the last
                        if (intPluginEndIndex >= 0)
                            {
                            bufferInput = new StringBuffer(bufferInput.substring(intPluginEndIndex));
                            }
                        }
                    }

                // Finish with </ins:Controller> regardless of Plugins
                bufferOutput.append(CONTROLLER_END);

                // Do we need to compress?
                if (compressed)
                    {
                    bufferOutput = new StringBuffer(Utilities.compressXML(bufferOutput));
                    }
                }
            else
                {
                // The XML is invalid; this should never happen!
                errors.add(MSG_INVALID_XML);
                }
            }
        else
            {
            // There is no Controller
            errors.add(MSG_NO_CONTROLLER);
            }

        return (bufferOutput);
        }


    /***********************************************************************************************
     * Check to see if the getConfiguration() and getModuleConfiguration() Commands
     * are configured correctly.
     *
     * @param getconfig
     * @param getmoduleconfig
     * @param steppedcommandcodebase
     * @param steppedcommandcode
     *
     * @return boolean
     */

    public static boolean isValidConfigurationCommand(final CommandType getconfig,
                                                      final CommandType getmoduleconfig,
                                                      final String steppedcommandcodebase,
                                                      final String steppedcommandcode)
        {
        final SteppedCodes steppedCodes;
        boolean boolValid;

        // Remember that we can't check the Child to Parent link,
        // because getModuleConfiguration() is being used as a BlockedCommand elsewhere,
        // so it can't have the ParentCommandCode

        // Retrieve the SteppedCommand to execute (we know there's only one in the list)
        steppedCodes = getconfig.getSteppedDataCommandList().get(0);

        // Check that the SteppedCodes refer to the Core
        boolValid = (CORE_COMMAND_CODE_BASE.equals(steppedCodes.getSteppedCommandCodeBase()));

        // We know that getModuleConfiguration() is in the Core, because of Xpath root node
        // Check that the CommandCodes match
        boolValid = ((boolValid)
                     && (getmoduleconfig.getCommandCode().equals(steppedcommandcode)));

        // Make sure it is getModuleConfiguration() so far as we can tell without assuming the name
        boolValid = ((boolValid)
                    && (getmoduleconfig.getParameterList() != null)
                    && (getmoduleconfig.getParameterList().size() == 1)
                    && (PARAMETER_CONFIGURATION_MODULEID.equals(getmoduleconfig.getParameterList().get(0).getName())));

        return (boolValid);
        }
    }
