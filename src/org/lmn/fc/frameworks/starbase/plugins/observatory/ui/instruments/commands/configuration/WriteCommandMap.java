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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandLexiconHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.PluginProvider;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * WriteCommandMap.
 */

public final class WriteCommandMap implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * doWriteCommandMap().
     * Saves the Command mapping structures at the specified location, as C source files.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doWriteCommandMap(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "WriteCommandMap.doWriteCommandMap()";
        final int PARAMETER_COUNT = 2;
        final int INDEX_PATHNAME = 0;
        final int INDEX_LANGUAGE = 1;
        final int ID_LENGTH_MAX = 20;
        final CommandType cmdWriteHeader;
        final List<ParameterType> listParameters;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debug(LOADER_PROPERTIES.isMasterDebug(), SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdWriteHeader = (CommandType)commandmessage.getCommandType().copy();

        // We expect two parameters, the pathname and the language
        listParameters = cmdWriteHeader.getParameterList();

        // Do not change any DAO data containers!

        // Check the parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_PATHNAME) != null)
            && (SchemaDataType.PATH_NAME.equals(listParameters.get(INDEX_PATHNAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_LANGUAGE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_LANGUAGE).getInputDataType().getDataTypeName())))
            {
            // Check that the Instrument has a Controller
            if ((dao.getHostInstrument().getInstrument() != null)
                && (dao.getHostInstrument().getInstrument().getController() != null))
                {
                String strPathname;
                final PluginProvider.Enum enumProvider;
                final String strControllerFilename;
                final StringBuffer bufferControllerCommandsFilename;
                final Vector<Vector> vecControllerCommands;
                final List<String> errors;

                strPathname = listParameters.get(INDEX_PATHNAME).getValue();

                if (!strPathname.endsWith(System.getProperty("file.separator")))
                    {
                    strPathname = strPathname + System.getProperty("file.separator");
                    }

                errors = new ArrayList<String>(10);

                // ToDo Implement Programming Language selection

                //---------------------------------------------------------------------------------
                // Controller Commands

                enumProvider = PluginProvider.CONTROLLER;
                bufferControllerCommandsFilename = new StringBuffer();
                vecControllerCommands = new Vector<Vector>(CommandLexiconHelper.COMMAND_MAP_COLUMN_COUNT);

                // GNU General Public License
                CommandLexiconHelper.addGPL(vecControllerCommands);
                CommandLexiconHelper.addHeadersForProvider(vecControllerCommands, dao, enumProvider);
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "// " + Chronos.timeNow());
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, SPACE);

                // Check that the Instrument has a Controller
                strControllerFilename = CommandLexiconHelper.addSendToPortCommandsForC(dao.getHostInstrument().getInstrument().getController(),
                                                                                       enumProvider.toString(),
                                                                                       enumProvider,
                                                                                       ID_LENGTH_MAX,
                                                                                       vecControllerCommands);

                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "// End of " + enumProvider.toString() + " Command Codes");

                //---------------------------------------------------------------------------------
                // Useful generic constants

                // List the supported CommandVariants for each Plugin
                // This *must* agree with the document in /doc/staribus/StaribusCommandVariants.txt
                // ToDo Review if an enum would be better here! (PluginDictionary?)

                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "");
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "// Plugin Command Variant Codes");
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "");

                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "#define CONTROLLER 0x0000");

                // RAG Four channel logger
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "#define LOGGERPLUGIN 0x0001");

                // RAG single channel VLF v1
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "#define VLFPLUGIN 0x0003");

                // RAG dual-axis magnetometer
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "#define MAGNETOMETERPLUGIN 0x0004");

                // ByVac four digit BV4614. See: http://www.byvac.co.uk
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "#define DISPLAY 0x1000");

                // Colorimeter
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "#define COLORIMETER 0x1100");
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "");
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "// Check consistency of the above with /doc/staribus/StaribusCommandVariants.txt");
                CommandLexiconHelper.addTextLineForC(vecControllerCommands, "");

                // Only add the ResponseMessageStatus enum to the Controller C Header
                CommandLexiconHelper.addResponseMessageStatusForC(vecControllerCommands);

                // Write the ControllerCommands file
                bufferControllerCommandsFilename.append(strPathname);
                bufferControllerCommandsFilename.append(strControllerFilename);
                bufferControllerCommandsFilename.append("-commands");
                dao.getResponseMessageStatusList().add(writeCommandMapAsC(bufferControllerCommandsFilename.toString().toLowerCase(),
                                                                          DataFormat.H,
                                                                          vecControllerCommands,
                                                                          CommandLexiconHelper.C_COMMENT_PREFIX,
                                                                          errors));

                //---------------------------------------------------------------------------------
                // We know we have a Controller, but how many Plugins are there?

                if ((dao.getHostInstrument().getInstrument().getController().getPluginList() != null)
                    && (!dao.getHostInstrument().getInstrument().getController().getPluginList().isEmpty()))
                    {
                    final Iterator<PluginType> iterPluginType;

                    iterPluginType = dao.getHostInstrument().getInstrument().getController().getPluginList().iterator();

                    while (iterPluginType.hasNext())
                        {
                        final PluginType pluginType;

                        pluginType = iterPluginType.next();

                        if (pluginType != null)
                            {
                            // This is simpler than an enum with a switch(), for now
                            if (PluginProvider.CONTROLLER.equals(pluginType.getProvider()))
                                {
                                System.out.println("Do CONTROLLER PLUGIN [ID=" + pluginType.getIdentifier()
                                                    + "] [provider=" +  pluginType.getProvider().toString() + "]");
                                System.out.println("Skip for now, since included in earlier Controller scan");
                                }
                            else if ((PluginProvider.PRIMARY_PLUGIN.equals(pluginType.getProvider()))
                                || (PluginProvider.SECONDARY_PLUGIN.equals(pluginType.getProvider())))
                                {
                                final String strPluginFilename;
                                final StringBuffer bufferCommandsFilename;
                                final Vector<Vector> vecPluginCommands;

                                System.out.println("Do PLUGIN [ID=" + pluginType.getIdentifier()
                                                   + "] [provider=" +  pluginType.getProvider().toString() + "]");

                                bufferCommandsFilename = new StringBuffer();
                                vecPluginCommands = new Vector<Vector>(CommandLexiconHelper.COMMAND_MAP_COLUMN_COUNT);

                                CommandLexiconHelper.addGPL(vecPluginCommands);
                                CommandLexiconHelper.addHeadersForProvider(vecPluginCommands, dao, pluginType.getProvider());
                                CommandLexiconHelper.addTextLineForC(vecPluginCommands, "// " + Chronos.timeNow());
                                CommandLexiconHelper.addTextLineForC(vecPluginCommands, SPACE);

                                strPluginFilename = CommandLexiconHelper.addSendToPortCommandsForC(dao.getHostInstrument().getInstrument().getController(),
                                                                                                   pluginType.getIdentifier(),
                                                                                                   pluginType.getProvider(),
                                                                                                   ID_LENGTH_MAX,
                                                                                                   vecPluginCommands);

                                CommandLexiconHelper.addTextLineForC(vecPluginCommands, "// End of " + pluginType.getProvider().toString() + " Command Codes");

                                // Write the PluginCommands file
                                bufferCommandsFilename.append(strPathname);
                                bufferCommandsFilename.append(strPluginFilename);
                                bufferCommandsFilename.append("-commands");
                                dao.getResponseMessageStatusList().add(writeCommandMapAsC(bufferCommandsFilename.toString().toLowerCase(),
                                                                                          DataFormat.H,
                                                                                          vecPluginCommands,
                                                                                          CommandLexiconHelper.C_COMMENT_PREFIX,
                                                                                          errors));
                                }
                            else if (PluginProvider.HOST_PLUGIN.equals(pluginType.getProvider()))
                                {
                                System.out.println("Skip HOST PLUGIN [ID=" + pluginType.getIdentifier()
                                                    + "] [provider=" +  pluginType.getProvider().toString() + "]");
                                }
                            else
                                {
                                errors.add("Invalid Plugin Provider [ID=" + pluginType.getIdentifier()
                                            + "] [provider=" +  pluginType.getProvider().toString() + "]");
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Plugin unexpectedly NULL");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "No Plugins to process");
                    }

                //---------------------------------------------------------------------------------
                // Did it all work?

                if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                           + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                           + METADATA_ACTION_WRITE_MAP,
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
                // There is no Controller
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET
                                                        + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                        + METADATA_ACTION_WRITE_MAP
                                                        + METADATA_RESULT
                                                        + "There is no Controller"
                                                        + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
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
                                                                      cmdWriteHeader,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Write the specified Report Vector as a C source file.
     * Assume the report and errors are not NULL.
     *
     * @param filename
     * @param format
     * @param report
     * @param commentprefix
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus writeCommandMapAsC(final String filename,
                                                            final DataFormat format,
                                                            final Vector<Vector> report,
                                                            final String commentprefix,
                                                            final List<String> errors)
        {
        final String SOURCE = "WriteCommandMap.writeCommandMapAsC() ";
        ResponseMessageStatus responseMessageStatus;

        try
            {
            final File file;
            final OutputStream outputStream;
            final Iterator<Vector> iterRows;

            file = new File(FileUtilities.buildFullFilename(filename, false, format));
            FileUtilities.overwriteFile(file);
            outputStream = new FileOutputStream(file);
            iterRows = report.iterator();

            while (iterRows.hasNext())
                {
                final Object objRow;

                objRow = iterRows.next();

                if ((objRow instanceof Vector)
                    && (((Vector)objRow).size() == CommandLexiconHelper.COMMAND_MAP_COLUMN_COUNT))
                    {
                    final Vector vecRow;

                    vecRow = (Vector)objRow;

                    // C code
                    outputStream.write(vecRow.get(0).toString().getBytes());

                    // Comment
                    outputStream.write(vecRow.get(1).toString().getBytes());
                    outputStream.write("\n".getBytes());
                    }
                }

            outputStream.flush();
            outputStream.close();
            responseMessageStatus = ResponseMessageStatus.SUCCESS;
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
