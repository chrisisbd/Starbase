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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.DiscoveryUtilities;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * WriteConfigurationXML.
 */

public final class WriteConfigurationXML implements FrameworkConstants,
                                                    FrameworkStrings,
                                                    FrameworkMetadata,
                                                    FrameworkSingletons,
                                                    ObservatoryConstants
    {
    // String Resources
    private static final String MSG_INVALID_XML = "The Instrument XML is invalid";
    private static final String MSG_NO_CONTROLLER = "This Instrument does not have a Controller";
    private static final String MSG_PRIMARY_ERROR = "The PrimaryPlugin is not set correctly in the XML";
    private static final String MSG_NO_ADDRESS = "Unable to determine the Address of the Controller";
    private static final String MSG_BLANK_LINES = "\n\n";


    /***********************************************************************************************
     * doWriteConfigurationXML().
     * Write the Instrument XML Configuration file for the selected Module.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doWriteConfigurationXML(final ObservatoryInstrumentDAOInterface dao,
                                                                   final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "WriteConfigurationXML.doWriteConfigurationXML()";
        final int PARAMETER_COUNT = 3;
        final int INDEX_FILENAME = 0;
        final int INDEX_MODULE = 1;
        final int INDEX_COMPRESSED = 2;
        final CommandType cmdWriteConfig;
        final List<ParameterType> listParameters;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debug(dao.isDebugMode(), SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdWriteConfig = (CommandType)commandmessage.getCommandType().copy();

        // We expect three parameters, the filename, module and compressed flag
        listParameters = cmdWriteConfig.getParameterList();

        // Do not change any DAO data containers!

        // Check the parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_MODULE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_MODULE).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_COMPRESSED) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_COMPRESSED).getInputDataType().getDataTypeName())))
            {
            final String strFilename;
            final String strModule;
            final boolean boolCompressed;
            final List<String> errors;

            strFilename = listParameters.get(INDEX_FILENAME).getValue();
            strModule = listParameters.get(INDEX_MODULE).getValue();

            // This should never throw NumberFormatException, because it has already been parsed
            boolCompressed = Boolean.parseBoolean(listParameters.get(INDEX_COMPRESSED).getValue());

            errors = new ArrayList<String>(10);

            // This is simpler than an enum with a switch(), for now
            if (PluginProvider.CONTROLLER.toString().equals(strModule))
                {
                dao.getResponseMessageStatusList().add(writeControllerXML(strFilename,
                                                                          DataFormat.XML,
                                                                          dao.getHostInstrument().getInstrument(),
                                                                          boolCompressed,
                                                                          errors));
                }
            else if (PluginProvider.PRIMARY_PLUGIN.toString().equals(strModule))
                {
                dao.getResponseMessageStatusList().add(writePrimaryPluginXML(strFilename,
                                                                             DataFormat.XML,
                                                                             dao.getHostInstrument().getInstrument(),
                                                                             boolCompressed,
                                                                             errors));
                }
            else if (PluginProvider.SECONDARY_PLUGIN.toString().equals(strModule))
                {
                dao.getResponseMessageStatusList().add(writeAllSecondaryPluginsXML(strFilename,
                                                                                   DataFormat.XML,
                                                                                   dao.getHostInstrument().getInstrument(),
                                                                                   boolCompressed,
                                                                                   errors));
                }
            else if (PluginProvider.HOST_PLUGIN.toString().equals(strModule))
                {
                dao.getResponseMessageStatusList().add(writeAllHostPluginsXML(strFilename,
                                                                              DataFormat.XML,
                                                                              dao.getHostInstrument().getInstrument(),
                                                                              boolCompressed,
                                                                              errors));
                }
            else
                {
                errors.add("Invalid Module selection [module=" + strModule + "]");
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET
                                                       + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                       + METADATA_ACTION_WRITE
                                                       + METADATA_MODULE + strModule + TERMINATOR_SPACE
                                                       + METADATA_COMPRESSED + boolCompressed + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                }
            else
                {
                SimpleEventLogUIComponent.logErrors(dao.getEventLogFragment(),
                                                    EventStatus.FATAL,
                                                    errors,
                                                    SOURCE,
                                                    dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                }
            }
        else
            {
            // The XML configuration was inappropriate
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                  SOURCE,
                                                                                  METADATA_TARGET
                                                                                  + SOURCE.trim()
                                                                                  + TERMINATOR,
                                                                                  METADATA_ACTION_COMMAND));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdWriteConfig,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Write the Controller XML fragment to a file.
     * Write the Controller structure and all Controller Commands, less the PluginManifest.
     * Write those Plugins marked as having a Provider of 'Controller' .
     * Assume the Instrument XML and errors are not NULL.
     * Return ResponseMessageStatus.SUCCESS if all went well.
     *
     * @param filename
     * @param format
     * @param instrument
     * @param compressed
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus writeControllerXML(final String filename,
                                                            final DataFormat format,
                                                            final Instrument instrument,
                                                            final boolean compressed,
                                                            final List<String> errors)
        {
        final String SOURCE = "WriteConfigurationXML.writeControllerXML() ";
        ResponseMessageStatus responseMessageStatus;

        try
            {
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
                final File file;
                final OutputStream outputStream;
                StringBuffer bufferInput;
                final StringBuffer bufferOutput;
                final String strAddressEndTag;
                final int intStartIndex;
                final int intEndIndex;

                file = new File(FileUtilities.buildFullFilename(filename + "-controller",
                                                                false,
                                                                format));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                // Start with the entire Instrument XML
                bufferInput = new StringBuffer(instrument.xmlText());
                bufferOutput = new StringBuffer();

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
                    bufferOutput.append(DiscoveryUtilities.INSERT_PLUGIN_MANIFEST);

                    // Discard the beginning, to step over the *Instrument* <ins:Identifier>
                    bufferInput = new StringBuffer(bufferInput.substring(intEndIndex));

                    // Copy from the *Controller* <ins:Identifier>Core to just before <ins:Plugin>
                    bufferOutput.append(bufferInput.substring(bufferInput.indexOf(CONTROLLER_IDENTIFIER),
                                                              bufferInput.indexOf(PLUGIN_START)));

                    // Even if this Controller has no Plugins, this is the place to add more
                    // Ideally the Primary Plugins should come before the Secondaries
                    bufferOutput.append(DiscoveryUtilities.INSERT_INSTRUMENT_PLUGINS);

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

                    // Not forgetting the EEPROM or file ETX
                    bufferOutput.append(ControlCharacters.ETX.getAsChar());

                    // Do we need to compress?
                    if (compressed)
                        {
                        outputStream.write(Utilities.compressXML(bufferOutput).toString().getBytes());
                        }
                    else
                        {
                        outputStream.write(bufferOutput.toString().getBytes());
                        }

                    outputStream.flush();
                    outputStream.close();
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    // The XML is invalid; this should never happen!
                    errors.add(MSG_INVALID_XML);
                    responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                    }
                }
            else
                {
                // There is no Controller
                errors.add(MSG_NO_CONTROLLER);
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }

        catch (final SecurityException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_ACCESS_DENIED);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final FileNotFoundException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_NOT_FOUND);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final IOException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final Exception exception)
            {
            exception.printStackTrace();
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Write the PrimaryPlugin XML fragment to a file.
     * Write ALL those Plugins marked as having a Provider of 'Primary Plugin'.
     * Write the Instrument preamble, the PluginManifest, the DAO etc.
     * Assume the Instrument XML and errors are not NULL.
     * Return ResponseMessageStatus.SUCCESS if all went well.
     *
     * @param filename
     * @param format
     * @param instrument
     * @param compressed
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus writePrimaryPluginXML(final String filename,
                                                               final DataFormat format,
                                                               final Instrument instrument,
                                                               final boolean compressed,
                                                               final List<String> errors)
        {
        final String SOURCE = "WriteConfigurationXML.writePrimaryPluginXML() ";
        ResponseMessageStatus responseMessageStatus;

        try
            {
            // There must be a Controller, otherwise no Commands or Plugins are possible
            if ((instrument != null)
                && (instrument.getController() != null))
                {
                // WARNING! Do not close opening tags because of potential namespace declaration
                final String XML_HEADER = "<?xml version=\"1.0\" standalone=\"yes\"?>";
                final String INSTRUMENTS_START_0 = "<Instruments xmlns=\"instruments.xmlbeans.model.fc.lmn.org\" ";
                final String INSTRUMENTS_START_1 = "xmlns:ins=\"instruments.xmlbeans.model.fc.lmn.org\" ";
                final String INSTRUMENTS_START_2 = "xmlns:md=\"metadata.xmlbeans.model.fc.lmn.org\">";
                final String INSTRUMENT_START = "<ins:Instrument>";
                final String IDENTIFIER_START = "<ins:Identifier";
                final String CONTROLLER_START = "<ins:Controller";
                final String MANIFEST_START = "<ins:PluginManifest";
                final String MANIFEST_END = "</ins:PluginManifest>";
                // Allow the closing tag on this one, to avoid confusion with the Manifest
                final String PLUGIN_START = "<ins:Plugin>";
                final String PLUGIN_END = "</ins:Plugin>";
                final String NAME_START = "<ins:Name";
                final String DAO_END = "</ins:DAO>";
                final String INSTRUMENT_END = "</ins:Instrument>";
                final String INSTRUMENTS_END = "</Instruments>";
                PluginType pluginPrimary;

                //---------------------------------------------------------------------------------
                // First find the name of the PrimaryPlugin,
                // and check the name agrees with that in the PluginManifest
                // Don't write the file if there is a configuration error

                pluginPrimary = null;

                if ((instrument.getController().getPluginList() != null)
                    && (!instrument.getController().getPluginList().isEmpty()))
                    {
                    final Iterator<PluginType> iterPluginType;

                    iterPluginType = instrument.getController().getPluginList().iterator();

                    // Find the name of the PrimaryPlugin
                    while ((pluginPrimary == null)
                        && (iterPluginType.hasNext()))
                        {
                        final PluginType pluginType;

                        pluginType = iterPluginType.next();

                        if ((pluginType != null)
                            && (PluginProvider.PRIMARY_PLUGIN.equals(pluginType.getProvider())))
                            {
                            pluginPrimary = pluginType;
                            }
                        }
                    }

                //---------------------------------------------------------------------------------
                // If we get this far, there is only one valid PrimaryPlugin,
                // check the name agrees with that in the PluginManifest,
                // and then write the configuration file

                if ((pluginPrimary != null)
                    && (instrument.getController().getPluginManifest() != null)
                    && (instrument.getController().getPluginManifest().getPrimaryResourceKey() != null)
                    && (instrument.getController().getPluginManifest().getPrimaryResourceKey().equals(pluginPrimary.getIdentifier())))
                    {
                    final File file;
                    final OutputStream outputStream;
                    StringBuffer bufferInput;
                    final StringBuffer bufferOutput;
                    final int intStartIndex;
                    final int intEndIndex;

                    file = new File(FileUtilities.buildFullFilename(filename + "-primary-" + pluginPrimary.getIdentifier().toLowerCase(),
                                                                    false,
                                                                    format));
                    FileUtilities.overwriteFile(file);
                    outputStream = new FileOutputStream(file);

                    // Start with the entire Instrument XML
                    bufferInput = new StringBuffer(instrument.xmlText());
                    bufferOutput = new StringBuffer();

                    // WARNING! This starts with <xml-fragment xmlns:md="metadata.xmlbeans.model.fc.lmn.org">
                    // so move ahead and look for <ins:Identifier>
                    intStartIndex = bufferInput.indexOf(IDENTIFIER_START);
                    intEndIndex = bufferInput.indexOf(CONTROLLER_START);

                    if ((intEndIndex >= 0)
                        && (intEndIndex >= 0)
                        && (intEndIndex > intStartIndex))
                        {
                        // We need to replace the XML and Instruments preamble
                        bufferOutput.append(XML_HEADER);
                        bufferOutput.append(INSTRUMENTS_START_0);
                        bufferOutput.append(INSTRUMENTS_START_1);
                        bufferOutput.append(INSTRUMENTS_START_2);
                        bufferOutput.append(INSTRUMENT_START);

                        // Copy from just before <ins:Identifier> to just before <ins:Controller>
                        bufferOutput.append(bufferInput.substring(intStartIndex,
                                                                  intEndIndex));

                        // Tell the assembler where to insert the Controller and its Address
                        bufferOutput.append(DiscoveryUtilities.INSERT_CONTROLLER_ADDRESS);

                        // Copy the entire PluginManifest
                        bufferOutput.append(bufferInput.substring(bufferInput.indexOf(MANIFEST_START),
                                                                  bufferInput.indexOf(MANIFEST_END) + MANIFEST_END.length()));

                        bufferOutput.append(DiscoveryUtilities.INSERT_CONTROLLER_CORE);

                        // Now take only the Plugin marked as having a Provider of 'Primary Plugin'
                        // Scan the XML to find the right one

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

                                // Select PrimaryPlugin only
                                if ((intPluginStartIndex >= 0)
                                    && (intPluginEndIndex >= 0)
                                    && (intPluginEndIndex > intPluginStartIndex)
                                    && (plugin.getProvider() != null)
                                    && (PluginProvider.PRIMARY_PLUGIN.equals(plugin.getProvider())))
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

                        // Even if this Controller has no Plugins, this is the place to add more,
                        // i.e. those marked with a Provider of 'Controller'
                        bufferOutput.append(DiscoveryUtilities.INSERT_CONTROLLER_PLUGINS);

                        // Tell the assembler where to insert the Controller End tag
                        bufferOutput.append(DiscoveryUtilities.INSERT_CONTROLLER_END);

                        // Now take everything up to the end of the DAO, which relates to the Instrument and the DAO
                        bufferOutput.append(bufferInput.substring(bufferInput.indexOf(NAME_START),
                                                                  bufferInput.indexOf(DAO_END) + DAO_END.length()));

                        // Lose the trailing xml-fragment
                        // For some reason the Instrument end tags are missing, so add them
                        bufferOutput.append(INSTRUMENT_END);
                        bufferOutput.append(INSTRUMENTS_END);

                        // Not forgetting the EEPROM or file ETX
                        bufferOutput.append(ControlCharacters.ETX.getAsChar());

                        // Do we need to compress?
                        if (compressed)
                            {
                            outputStream.write(Utilities.compressXML(bufferOutput).toString().getBytes());
                            }
                        else
                            {
                            outputStream.write(bufferOutput.toString().getBytes());
                            }

                        outputStream.flush();
                        outputStream.close();
                        responseMessageStatus = ResponseMessageStatus.SUCCESS;
                        }
                    else
                        {
                        // The XML is invalid; this should never happen!
                        errors.add(MSG_INVALID_XML);
                        responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;
                        }
                    }
                else
                    {
                    // PrimaryPlugin is not set correctly
                    errors.add(MSG_PRIMARY_ERROR);
                    responseMessageStatus = ResponseMessageStatus.INVALID_XML;
                    }
                }
            else
                {
                // There is no Instrument or Controller
                errors.add(MSG_NO_CONTROLLER);
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }

        catch (final SecurityException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_ACCESS_DENIED);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final FileNotFoundException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_NOT_FOUND);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final IOException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final Exception exception)
            {
            exception.printStackTrace();
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Write the SecondaryPlugin XML fragments to separate files.
     * Use the specified filename as a prefix to the full name.
     * Write ALL those Plugins marked as having a Provider of 'SecondaryPlugin'.
     * Assume the Instrument XML and errors are not NULL.
     * Return ResponseMessageStatus.SUCCESS if all went well.
     *
     * @param filename
     * @param format
     * @param instrument
     * @param compressed
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus writeAllSecondaryPluginsXML(final String filename,
                                                                     final DataFormat format,
                                                                     final Instrument instrument,
                                                                     final boolean compressed,
                                                                     final List<String> errors)
        {
        final String SOURCE = "WriteConfigurationXML.writeAllSecondaryPluginsXML() ";
        ResponseMessageStatus responseMessageStatus;

        try
            {
            // There must be a Controller, otherwise no Commands or Plugins are possible
            if (instrument.getController() != null)
                {
                // WARNING! Do not close opening tags because of potential namespace declaration
                // Allow the closing tag on this one, to avoid confusion with the Manifest
                final String PLUGIN_START = "<ins:Plugin>";
                final String PLUGIN_END = "</ins:Plugin>";
                StringBuffer bufferInput;

                // Start with the entire Instrument XML
                bufferInput = new StringBuffer(instrument.xmlText());

                // Take ALL those Plugins marked as having a Provider of 'SecondaryPlugin'
                // It is a valid configuration to have zero plugins (of any type)

                if ((instrument.getController().getPluginList() != null)
                    && (!instrument.getController().getPluginList().isEmpty()))
                    {
                    int intPluginStartIndex;
                    final Iterator<PluginType> iterPlugins;

                    // Discard all the foregoing text, to begin at the first potential Plugin
                    // We know that there must be at least one, but don't know its type
                    intPluginStartIndex = bufferInput.indexOf(PLUGIN_START);
                    bufferInput = new StringBuffer(bufferInput.substring(intPluginStartIndex));

                    iterPlugins = instrument.getController().getPluginList().iterator();

                    // Scan all Plugins declared for this Instrument
                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;
                        final int intPluginEndIndex;

                        plugin = iterPlugins.next();

                        // Find the start and end of this Plugin regardless of its type
                        // indexOf() reurns -1 if the character does not occur
                        intPluginStartIndex = bufferInput.indexOf(PLUGIN_START);
                        intPluginEndIndex = bufferInput.indexOf(PLUGIN_END) + PLUGIN_END.length();

                        // Take ALL those Plugins marked as having a Provider of 'SecondaryPlugin'
                        if ((plugin != null)
                             && (plugin.getProvider() != null)
                             && (PluginProvider.SECONDARY_PLUGIN.equals(plugin.getProvider())))
                            {
                            final File file;
                            final OutputStream outputStream;
                            final StringBuffer bufferOutput;

                            // We have a valid SecondaryPlugin, so write the file
                            file = new File(FileUtilities.buildFullFilename(filename + "-secondary-" + plugin.getIdentifier().toLowerCase(),
                                                                            false,
                                                                            format));
                            FileUtilities.overwriteFile(file);
                            outputStream = new FileOutputStream(file);
                            bufferOutput = new StringBuffer();

                            // Capture the XML of this Plugin, but only if it seems to make sense
                            if ((intPluginStartIndex >= 0)
                                && (intPluginEndIndex >= 0)
                                && (intPluginEndIndex > intPluginStartIndex))
                                {
                                bufferOutput.append(bufferInput.substring(intPluginStartIndex,
                                                                          intPluginEndIndex));
                                }

                            // Not forgetting the EEPROM or file ETX
                            bufferOutput.append(ControlCharacters.ETX.getAsChar());

                            // Do we need to compress?
                            if (compressed)
                                {
                                outputStream.write(Utilities.compressXML(bufferOutput).toString().getBytes());
                                }
                            else
                                {
                                outputStream.write(bufferOutput.toString().getBytes());
                                }

                            outputStream.flush();
                            outputStream.close();
                            }
                        else
                            {
                            // Skip all other Plugin types
                            }

                        // Regardless of the type of Plugin, Move to the start of the next Plugin,
                        // which could occur immediately after the end of the last

                        if (intPluginEndIndex >= 0)
                            {
                            bufferInput = new StringBuffer(bufferInput.substring(intPluginEndIndex));
                            }
                        }
                    }

                // It is possible to arrive here, having not written any files
                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                }
            else
                {
                // There is no Controller
                errors.add(MSG_NO_CONTROLLER);
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }

        catch (final SecurityException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_ACCESS_DENIED);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final FileNotFoundException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_NOT_FOUND);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final IOException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final Exception exception)
            {
            exception.printStackTrace();
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Write the HostPlugin XML fragments to separate files.
     * Use the specified filename as a prefix to the full name.
     * Write ALL those Plugins marked as having a Provider of 'HostPlugin'.
     * Assume the Instrument XML and errors are not NULL.
     * Return ResponseMessageStatus.SUCCESS if all went well.
     *
     * @param filename
     * @param format
     * @param instrument
     * @param compressed
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus writeAllHostPluginsXML(final String filename,
                                                                final DataFormat format,
                                                                final Instrument instrument,
                                                                final boolean compressed,
                                                                final List<String> errors)
        {
        final String SOURCE = "WriteConfigurationXML.writeAllHostPluginsXML() ";
        ResponseMessageStatus responseMessageStatus;

        try
            {
            // There must be a Controller, otherwise no Commands or Plugins are possible
            if (instrument.getController() != null)
                {
                // WARNING! Do not close opening tags because of potential namespace declaration
                // Allow the closing tag on this one, to avoid confusion with the Manifest
                final String PLUGIN_START = "<ins:Plugin>";
                final String PLUGIN_END = "</ins:Plugin>";
                StringBuffer bufferInput;

                // Start with the entire Instrument XML
                bufferInput = new StringBuffer(instrument.xmlText());

                // Take ALL those Plugins marked as having a Provider of 'SecondaryPlugin'
                if ((instrument.getController().getPluginList() != null)
                    && (!instrument.getController().getPluginList().isEmpty()))
                    {
                    int intPluginStartIndex;
                    final Iterator<PluginType> iterPlugins;

                    // Discard all the foregoing text, to begin at the first potential Plugin
                    intPluginStartIndex = bufferInput.indexOf(PLUGIN_START);
                    bufferInput = new StringBuffer(bufferInput.substring(intPluginStartIndex));

                    iterPlugins = instrument.getController().getPluginList().iterator();

                    // Scan all Plugins declared for this Instrument
                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;
                        final int intPluginEndIndex;

                        plugin = iterPlugins.next();

                        // Find the start and end of this Plugin regardless of its type
                        // indexOf() reurns -1 if the character does not occur
                        intPluginStartIndex = bufferInput.indexOf(PLUGIN_START);
                        intPluginEndIndex = bufferInput.indexOf(PLUGIN_END) + PLUGIN_END.length();

                        // Take ALL those Plugins marked as having a Provider of 'HostPlugin'
                        if ((plugin != null)
                             && (plugin.getProvider() != null)
                             && (PluginProvider.HOST_PLUGIN.equals(plugin.getProvider())))
                            {
                            final File file;
                            final OutputStream outputStream;
                            final StringBuffer bufferOutput;

                            // We have a valid HostPlugin, so write the file
                            file = new File(FileUtilities.buildFullFilename(filename + "-host-" + plugin.getIdentifier().toLowerCase(),
                                                                            false,
                                                                            format));
                            FileUtilities.overwriteFile(file);
                            outputStream = new FileOutputStream(file);
                            bufferOutput = new StringBuffer();

                            // Capture the XML of this Plugin, but only if it seems to make sense
                            if ((intPluginStartIndex >= 0)
                                && (intPluginEndIndex >= 0)
                                && (intPluginEndIndex > intPluginStartIndex))
                                {
                                bufferOutput.append(bufferInput.substring(intPluginStartIndex,
                                                                          intPluginEndIndex));
                                }

                            // Not forgetting the EEPROM or file ETX
                            bufferOutput.append(ControlCharacters.ETX.getAsChar());

                            // Do we need to compress?
                            if (compressed)
                                {
                                outputStream.write(Utilities.compressXML(bufferOutput).toString().getBytes());
                                }
                            else
                                {
                                outputStream.write(bufferOutput.toString().getBytes());
                                }

                            outputStream.flush();
                            outputStream.close();
                            }
                        else
                            {
                            // Skip all other Plugin types
                            }

                        // Regardless of the type of Plugin, Move to the start of the next Plugin,
                        // which could occur immediately after the end of the last

                        if (intPluginEndIndex >= 0)
                            {
                            bufferInput = new StringBuffer(bufferInput.substring(intPluginEndIndex));
                            }
                        }
                    }

                // It is possible to arrive here, having not written any files
                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                }
            else
                {
                // There is no Controller
                errors.add(MSG_NO_CONTROLLER);
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }

        catch (final SecurityException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_ACCESS_DENIED);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final FileNotFoundException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_NOT_FOUND);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final IOException exception)
            {
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        catch (final Exception exception)
            {
            exception.printStackTrace();
            errors.add(DataTranslatorInterface.ERROR_FILE_SAVE);
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }
    }
