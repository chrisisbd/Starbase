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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;

import javax.swing.*;
import java.util.*;


/***************************************************************************************************
 * CommandLexiconHelper.
 */

public final class CommandLexiconHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   ObservatoryConstants
    {
    private static final String TITLE_MODULE = "Module";

    private static final String TITLE_COMMAND_IDENTIFIER = "Command Identifier";
    private static final String TITLE_CODE = "Code";
    private static final String TITLE_LEGACY_CODE = "LegacyCode";
    private static final String TITLE_LOCAL_PORT = "Local|Port";
    private static final String TITLE_MACRO_STEPS = "MacroSteps";

    private static final String TITLE_COMMAND_DESCRIPTION = "Command Description";
    private static final String TITLE_RESPONSE_NAME = "Response Name";
    private static final String TITLE_RESPONSE_DATATYPE = "Response DataType";
    private static final String TITLE_RESPONSE_UNITS = "Response Units";
    private static final String TITLE_RESPONSE_REGEX = "Regex";
    private static final String TITLE_PARAMETER_NAME = "Parameter Name";
    private static final String TITLE_PARAMETER_DATATYPE_INPUT = "Param InputType";
    private static final String TITLE_PARAMETER_DATATYPE_TRAFFIC = "Param TrafficType";
    private static final String TITLE_PARAMETER_UNITS = "Parameter Units";
    private static final String TITLE_PARAMETER_REGEX = "Regex";
    private static final String MSG_FAULTY_PARAMETER = "<html><i>Faulty Parameter definition</i></html>";
    private static final String MSG_NO_PARAMETERS = "<html><i>No Parameters</i></html>";
    private static final String MSG_NO_RESPONSE = "<html><i>No Response Data</i></html>";
    private static final String COMMAND_VARIANT_GENERIC = "0000";
    public static final String C_COMMENT_PREFIX = "// ";

    private static final int PARAMETER_COUNT = 6;

    public static final int COMMAND_MAP_COLUMN_COUNT = 2;


    /**********************************************************************************************/
    /* CommandLexicon Report                                                                      */
    /***********************************************************************************************
     * Add the Controller's Commands to the Report.
     *
     * @param controller
     * @param report
     * @param columncount
     * @param columnindex
     */

    public static void addControllerCommands(final Controller controller,
                                             final Vector<Vector> report,
                                             final int columncount,
                                             final int columnindex)
        {
        if ((controller != null)
//            && (XmlBeansUtilities.isValidXml(controller))
            && (controller.getCommandList() != null)
            && (!controller.getCommandList().isEmpty())
            && (report != null)
            && (columncount > 0)
            && (columnindex >= 0)
            && (columnindex < columncount))
            {
            final List<CommandType> commands;

            commands = controller.getCommandList();

            // All parameters are assumed to be valid
            addLabelLine(report,
                         columncount,
                         columnindex,
                         controller.getIdentifier());

            addCommandDetail(controller.getCommandCodeBase(),
                             commands,
                             report,
                             columncount,
                             columnindex);
            }
        }


    /***********************************************************************************************
     * Add the Controller's Plugins to the Report.
     *
     * @param controller
     * @param report
     * @param columncount
     * @param columnindex
     */

    public static void addPluginCommands(final Controller controller,
                                         final Vector<Vector> report,
                                         final int columncount,
                                         final int columnindex)
        {
        if ((controller != null)
//            && (XmlBeansUtilities.isValidXml(controller))
            && (controller.getPluginList() != null)
            && (!controller.getPluginList().isEmpty())
            && (report != null)
            && (columncount > 0)
            && (columnindex >= 0)
            && (columnindex < columncount))
            {
            final Iterator<PluginType> iterPlugins;

            iterPlugins = controller.getPluginList().iterator();

            while (iterPlugins.hasNext())
                {
                final PluginType pluginType;

                pluginType = iterPlugins.next();

                // Only show Plugins which have Commands or Macros
                if (pluginType != null)
                    {
                    if ((pluginType.getCommandList() != null)
                        && (!pluginType.getCommandList().isEmpty()))
                        {
                        final String strIdentifier;

                        strIdentifier = pluginType.getIdentifier();

                        // Separate the Commands belonging to each Plugin
                        addLabelLine(report,
                                     columncount,
                                     columnindex,
                                     strIdentifier);

                        addCommandDetail(pluginType.getCommandCodeBase(),
                                         pluginType.getCommandList(),
                                         report,
                                         columncount,
                                         columnindex);
                        }
                    else if ((pluginType.getMacroList() != null)
                        && (!pluginType.getMacroList().isEmpty()))
                        {
                        final String strIdentifier;

                        strIdentifier = pluginType.getIdentifier();

                        // Separate the Macros belonging to each Plugin
                        addLabelLine(report,
                                     columncount,
                                     columnindex,
                                     strIdentifier);

                        addMacroDetail(pluginType.getCommandCodeBase(),
                                       pluginType.getMacroList(),
                                       report,
                                       columncount,
                                       columnindex);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add the details from the List of Commands.
     *
     * @param commandcodebase
     * @param commands
     * @param report
     * @param columncount
     * @param columnindex
     */

    private static void addCommandDetail(final String commandcodebase,
                                         final List<CommandType> commands,
                                         final Vector<Vector> report,
                                         final int columncount,
                                         final int columnindex)
        {
        if ((commands != null)
            && (!commands.isEmpty())
            && (report != null)
            && (columncount > 0)
            && (columnindex >= 0)
            && (columnindex < columncount))
            {
            final Iterator<CommandType> iterCommands;

            iterCommands = commands.iterator();

            while (iterCommands.hasNext())
                {
                final CommandType commandType;

                commandType = iterCommands.next();

                if (commandType != null)
                    {
                    final Vector<Object> vecRow;
                    final String strLegacyCode;
                    final List<ParameterType> listParameters;
                    int intIndexCounter;

                    vecRow = new Vector<Object>(columncount);

                    // Pad to the column before the starting column
                    padBlankColumns(vecRow, columnindex);

                    // Start adding items at columnindex
                    intIndexCounter = columnindex;

                    // Remember that all data entries must be Strings
                    // Skip over the Module column
                    vecRow.add(EMPTY_STRING);

                    // Added 1 item
                    intIndexCounter++;

                    vecRow.add(commandType.getIdentifier());

                    // There must be a CommandCodeBase & CommandCode & Variant
                    vecRow.add(commandcodebase
                                + COLON
                                + commandType.getCommandCode()
                                + COLON
                                + commandType.getCommandVariant());

                    // Added 2 items
                    intIndexCounter += 2;

                    // There may not be a LegacyCode
                    strLegacyCode = commandType.getLegacyCode();

                    if (strLegacyCode != null)
                        {
                        vecRow.add(commandType.getLegacyCode());
                        }
                    else
                        {
                        vecRow.add(EMPTY_STRING);
                        }

                    // Added 1 item
                    intIndexCounter++;

                    // Local|Port
                    if (commandType.getSendToPort())
                        {
                        vecRow.add("Port");
                        }
                    else
                        {
                        vecRow.add("Local");
                        }

                    // Added 1 item
                    intIndexCounter++;

                    // There are no MacroSteps
                    vecRow.add(EMPTY_STRING);

                    // Added 1 item
                    intIndexCounter++;

                    vecRow.add(commandType.getDescription());

                    // Added 1 item
                    intIndexCounter += 1;

                    if (commandType.getResponse() != null)
                        {
                        final ResponseType response;

                        response = commandType.getResponse();

                        vecRow.add(response.getName());
                        vecRow.add(response.getDataTypeName().toString());
                        vecRow.add(response.getUnits().toString());
                        vecRow.add(response.getRegex());
                        }
                    else if (commandType.getAck() != null)
                        {
                        final AckType ack;

                        ack = commandType.getAck();

                        vecRow.add(ack.getName());
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        }
                    else
                        {
                        vecRow.add(MSG_NO_RESPONSE);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        }

                    // Added 4 items
                    intIndexCounter += 4;

                    listParameters = commandType.getParameterList();

                    if ((listParameters != null)
                        && (!listParameters.isEmpty()))
                        {
                        for (int i = 0; i < listParameters.size(); i++)
                            {
                            final ParameterType parameter;

                            parameter = listParameters.get(i);

                            if (parameter != null)
                                {
                                vecRow.add(parameter.getName());
                                vecRow.add(parameter.getInputDataType().getDataTypeName().toString());
                                vecRow.add(parameter.getTrafficDataType().getDataTypeName().toString());
                                vecRow.add(parameter.getUnits().toString());
                                vecRow.add(parameter.getRegex());
                                }
                            else
                                {
                                vecRow.add(MSG_FAULTY_PARAMETER);
                                vecRow.add(EMPTY_STRING);
                                vecRow.add(EMPTY_STRING);
                                vecRow.add(EMPTY_STRING);
                                vecRow.add(EMPTY_STRING);
                                }

                            // Added 5 items
                            intIndexCounter += 5;
                            }
                        }
                    else
                        {
                        vecRow.add(MSG_NO_PARAMETERS);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);

                        // Added 5 items
                        intIndexCounter += 5;
                        }

                    // Pad to the end of the row
                    padBlankColumns(vecRow, columncount-intIndexCounter);

                    report.add(vecRow);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add the details from the List of Macros.
     *
     * @param commandcodebase
     * @param macros
     * @param report
     * @param columncount
     * @param columnindex
     */

    private static void addMacroDetail(final String commandcodebase,
                                       final List<MacroType> macros,
                                       final Vector<Vector> report,
                                       final int columncount,
                                       final int columnindex)
        {
        if ((macros != null)
            && (!macros.isEmpty())
            && (report != null)
            && (columncount > 0)
            && (columnindex >= 0)
            && (columnindex < columncount))
            {
            final Iterator<MacroType> iterMacros;

            iterMacros = macros.iterator();

            while (iterMacros.hasNext())
                {
                final MacroType macroType;

                macroType = iterMacros.next();

                if (macroType != null)
                    {
                    final Vector<Object> vecRow;
                    final List<ParameterType> listParameters;
                    int intIndexCounter;

                    vecRow = new Vector<Object>(columncount);

                    // Pad to the column before the starting column
                    padBlankColumns(vecRow, columnindex);

                    // Start adding items at columnindex
                    intIndexCounter = columnindex;

                    // Remember that all data entries must be Strings
                    // Skip over the Module column
                    vecRow.add(EMPTY_STRING);

                    // Added 1 item
                    intIndexCounter++;

                    vecRow.add(HTML_PREFIX_MACRO_IDENTIFIER + macroType.getIdentifier() + HTML_SUFFIX_MACRO_IDENTIFIER);

                    // There must be a CommandCodeBase, but no CommandCode & Variant
                    vecRow.add(commandcodebase
                                + COLON
                                + "00"
                                + COLON
                                + "0000");

                    // Added 2 items
                    intIndexCounter += 2;

                    // There is no LegacyCode
                    vecRow.add(EMPTY_STRING);

                    // Added 1 item
                    intIndexCounter++;

                    // ToDo Review: Local|Port is always Local for Macros
                    vecRow.add("Local");

                    // Added 1 item
                    intIndexCounter++;

                    // Macro Step counter
                    if ((macroType.getStepList() != null)
                        && (!macroType.getStepList().isEmpty()))
                        {
                        vecRow.add(macroType.getStepList().size());
                        }
                    else
                        {
                        vecRow.add(EMPTY_STRING);
                        }

                    // Added 1 item
                    intIndexCounter++;

                    vecRow.add(macroType.getDescription());

                    // Added 1 item
                    intIndexCounter += 1;

                    if (macroType.getResponse() != null)
                        {
                        final ResponseType response;

                        response = macroType.getResponse();

                        vecRow.add(response.getName());
                        vecRow.add(response.getDataTypeName().toString());
                        vecRow.add(response.getUnits().toString());
                        vecRow.add(response.getRegex());
                        }
                    else if (macroType.getAck() != null)
                        {
                        final AckType ack;

                        ack = macroType.getAck();

                        vecRow.add(ack.getName());
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        }
                    else
                        {
                        vecRow.add(MSG_NO_RESPONSE);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        }

                    // Added 4 items
                    intIndexCounter += 4;

                    listParameters = macroType.getParameterList();

                    if ((listParameters != null)
                        && (!listParameters.isEmpty()))
                        {
                        for (int i = 0; i < listParameters.size(); i++)
                            {
                            final ParameterType parameter;

                            parameter = listParameters.get(i);

                            if (parameter != null)
                                {
                                vecRow.add(parameter.getName());
                                vecRow.add(parameter.getInputDataType().getDataTypeName().toString());
                                vecRow.add(parameter.getTrafficDataType().getDataTypeName().toString());
                                vecRow.add(parameter.getUnits().toString());
                                vecRow.add(parameter.getRegex());
                                }
                            else
                                {
                                vecRow.add(MSG_FAULTY_PARAMETER);
                                vecRow.add(EMPTY_STRING);
                                vecRow.add(EMPTY_STRING);
                                vecRow.add(EMPTY_STRING);
                                vecRow.add(EMPTY_STRING);
                                }

                            // Added 5 items
                            intIndexCounter += 5;
                            }
                        }
                    else
                        {
                        vecRow.add(MSG_NO_PARAMETERS);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);
                        vecRow.add(EMPTY_STRING);

                        // Added 5 items
                        intIndexCounter += 5;
                        }

                    // Pad to the end of the row
                    padBlankColumns(vecRow, columncount-intIndexCounter);

                    report.add(vecRow);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add a label line to the Report, at the specified column index.
     *
     * @param report
     * @param columncount
     * @param columnindex
     * @param label
     */

    public static void addLabelLine(final Vector<Vector> report,
                                    final int columncount,
                                    final int columnindex,
                                    final String label)
        {
        if ((report != null)
            && (columncount > 0)
            && (columnindex >= 0)
            && (columnindex < columncount))
            {
            final Vector<Object> vecRow;

            vecRow = new Vector<Object>(columncount);

            // Pad to the column before the starting column
            padBlankColumns(vecRow, columnindex);

            // Remember that all data entries must be Strings
            // Add the label to the Module column
            vecRow.add(HTML_PREFIX_BOLD + label + HTML_SUFFIX_BOLD);

            // Pad to the end of the row
            padBlankColumns(vecRow, columncount - columnindex - 1);

            report.add(vecRow);
            }
        }


    /***********************************************************************************************
     * Add blank columns to a Report row.
     *
     * @param row
     * @param blankcount
     */

    private static void padBlankColumns(final Vector<Object> row,
                                        final int blankcount)
        {
        if ((row != null)
            && (blankcount > 0))
            {
            for (int i = 0; i < blankcount; i++)
                {
                row.add(EMPTY_STRING);
                }
            }
        }


    /**********************************************************************************************/
    /* CommandLexicon C Header                                                                    */
    /***********************************************************************************************
     * Add the GNU General Public License to the Header.
     *
     * @param report
     */

    public static void addGPL(final Vector<Vector> report)
        {
        addTextLineForC(report, "// Copyright");
        addTextLineForC(report, "//   2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010");
        addTextLineForC(report, "//   2010, 2011, 2012, 2013, 2013, 2012, 2013, 2014");
        addTextLineForC(report, "// Laurence Newell");
        addTextLineForC(report, "// starbase@ukraa.com");
        addTextLineForC(report, "// radio.telescope@btinternet.com");
        addTextLineForC(report, "// ");
        addTextLineForC(report, "// This file is part of Starbase.");
        addTextLineForC(report, "// ");
        addTextLineForC(report, "// Starbase is free software: you can redistribute it and/or modify");
        addTextLineForC(report, "// it under the terms of the GNU General Public License as published by");
        addTextLineForC(report, "// the Free Software Foundation, either version 3 of the License, or");
        addTextLineForC(report, "// (at your option) any later version.");
        addTextLineForC(report, "// ");
        addTextLineForC(report, "// Starbase is distributed in the hope that it will be useful,");
        addTextLineForC(report, "// but WITHOUT ANY WARRANTY; without even the implied warranty of");
        addTextLineForC(report, "// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the ");
        addTextLineForC(report, "// GNU General Public License for more details.");
        addTextLineForC(report, "// ");
        addTextLineForC(report, "// You should have received a copy of the GNU General Public License");
        addTextLineForC(report, "// along with Starbase.  If not, see http://www.gnu.org/licenses.");
        addTextLineForC(report, " ");
        }


    /***********************************************************************************************
     * Add the Framework and Observatory headers to the report, for the specified Provider.
     *
     * @param report
     * @param dao
     * @param provider
     */

    public static void addHeadersForProvider(final Vector<Vector> report,
                                             final ObservatoryInstrumentDAOInterface dao,
                                             final PluginProvider.Enum provider)
        {
        // Identify the Framework
        addTextLineForC(report, "// " + provider.toString() + " Command Codes");
        addTextLineForC(report, "// Framework Version " + REGISTRY.getFramework().getVersionNumber());
        addTextLineForC(report, "// Framework Build " + REGISTRY.getFramework().getBuildNumber());
        addTextLineForC(report, "// Framework Status " + REGISTRY.getFramework().getBuildStatus());

        // Identify the Observatory
        if (dao.getHostInstrument().getHostAtom() != null)
            {
            final AtomPlugin plugin;

            plugin = dao.getHostInstrument().getHostAtom();

            addTextLineForC(report, "// Observatory Version " + plugin.getVersionNumber());
            addTextLineForC(report, "// Observatory Build " + plugin.getBuildNumber());
            addTextLineForC(report, "// Observatory Status " + plugin.getBuildStatus());
            }
        }


    /***********************************************************************************************
     * Add the Instrument SendToPort Commands to the C Header,
     * for the specified PluginProvider.
     * Truncate Command Identifiers to maxlength.
     * Return a fragment of the output filename to identify the specific Plugin.
     *
     * @param controller
     * @param identifier
     * @param provider
     * @param maxlength
     * @param report
     *
     * @return String
     */

    public static String addSendToPortCommandsForC(final Controller controller,
                                                   final String identifier,
                                                   final PluginProvider.Enum provider,
                                                   final int maxlength,
                                                   final Vector<Vector> report)
        {
        final Hashtable<String, String> hashCommandVariants;
        final String strIdentifier;

        // Harvest the CommandVariants required for all SendToPort commands in this Instrument
        hashCommandVariants = harvestSendToPortCommandVariants(controller);

        // Build the requested PluginProvider Header report from the table of CommandVariants
        // Return a fragment of the output filename to identify the specific Plugin
        strIdentifier = buildPluginProviderHeaderForC(controller,
                                                      hashCommandVariants,
                                                      identifier,
                                                      provider,
                                                      maxlength,
                                                      report);

        return (strIdentifier);
        }


    /**********************************************************************************************/
    /* Harvester                                                                                  */
    /***********************************************************************************************
     * Harvest the table of CommandVariants required for all SendToPort commands in this Instrument.
     * The Hashtable Key is the (unique) CommandVariant code.
     * The Hashtable Value is one of 'Generic', the Controller Identifier, or a Plugin Identifier.
     * Note that both Primary and Secondary Plugins are recorded here.
     *
     * e.g.
     * Key      Value
     * 0000     Generic
     * 000x     Controller  (non-Generic Command in the Controller, rather unlikely but possible)
     * 000y     MagnetometerPlugin (Primary, via PluginManifest)
     * 000z     ExtraPlugin  (Secondary, via PluginManifest)
     *
     * @param controller
     *
     * @return Hashtable{String String}
     */

    private static Hashtable<String, String> harvestSendToPortCommandVariants(final Controller controller)
        {
        final Hashtable<String, String> hashCommandVariants;

        hashCommandVariants = new Hashtable<String, String>(20);

        // There must be a Controller with at least one SendToPort Command
        if ((controller != null)
            && (controller.getCommandList() != null)
            && (hasSendToPort(controller.getCommandList())))
            {
            final List<CommandType> listControllerCommands;
            final Iterator<CommandType> iterControllerCommands;

            listControllerCommands = controller.getCommandList();
            iterControllerCommands = listControllerCommands.iterator();

            while (iterControllerCommands.hasNext())
                {
                final CommandType cmdController;

                cmdController = iterControllerCommands.next();

                // Only add SendToPort Commands
                if ((cmdController != null)
                    && (cmdController.getSendToPort())
                    && (!hashCommandVariants.containsKey(cmdController.getCommandVariant())))
                    {
                    if (COMMAND_VARIANT_GENERIC.equals(cmdController.getCommandVariant()))
                        {
                        // All Generic (CV=0000) Controller Commands are 'Controller'
                        hashCommandVariants.put(cmdController.getCommandVariant(),
                                                PluginProvider.CONTROLLER.toString());
                        }
                    else
                        {
                        // Any other SendToPort Commands in the Controller are recorded under the Controller's Identifier
                        // This is very unlikely for a SendToPort Command
                        // For the purposes of the Builder, these are regarded as Generic Commands
                        hashCommandVariants.put(cmdController.getCommandVariant(),
                                                controller.getIdentifier());
                        }
                    }
                }

            // Now the Controller's plugins, if any
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final Iterator<PluginType> iterPlugins;

                iterPlugins = controller.getPluginList().iterator();

                while (iterPlugins.hasNext())
                    {
                    final PluginType pluginType;

                    pluginType = iterPlugins.next();

                    // Only add Plugins which have SendToPort Commands
                    // Macros are never SendToPort, so don't use MacroList
                    if ((pluginType != null)
                        && (pluginType.getCommandList() != null)
                        && (hasSendToPort(pluginType.getCommandList())))
                        {
                        final List<CommandType> listPluginCommands;
                        final Iterator<CommandType> iterPluginCommands;

                        listPluginCommands = pluginType.getCommandList();
                        iterPluginCommands = listPluginCommands.iterator();

                        while (iterPluginCommands.hasNext())
                            {
                            final CommandType cmdPlugin;

                            cmdPlugin = iterPluginCommands.next();

                            // Only add SendToPort Commands
                            if ((cmdPlugin != null)
                                && (cmdPlugin.getSendToPort())
                                && (!hashCommandVariants.containsKey(cmdPlugin.getCommandVariant())))
                                {
                                // Highly unlikely that the Controller didn't have any CV:0000 !
                                // but if not, then make an entry now
                                if (COMMAND_VARIANT_GENERIC.equals(cmdPlugin.getCommandVariant()))
                                    {
                                    // ToDO This is a bit of an anomaly, should these also be marked as 'Controller'?
                                    hashCommandVariants.put(cmdPlugin.getCommandVariant(),
                                                            PluginProvider.CONTROLLER.toString());
                                    }
                                else
                                    {
                                    // Any other SendToPort Commands in the Controller are recorded under the Plugin's Identifier
                                    // This could be Primary or Secondary
                                    // The Identifiers are verified against the Instrument PluginManifest
                                    hashCommandVariants.put(cmdPlugin.getCommandVariant(),
                                                            pluginType.getIdentifier());
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // Show the table contents for debugging purposes
        LOGGER.debugTimedEvent(true, hashCommandVariants.toString());

        return (hashCommandVariants);
        }


    /**********************************************************************************************/
    /* Builder                                                                                    */
    /***********************************************************************************************
     * Build the specified PluginProvider C Header file from the Controller structure
     * and the table of CommandVariant codes vs. Plugin identifiers.
     * Return a fragment of the output filename to identify the specific Plugin.
     *
     * PluginProvider can be:
     *  CONTROLLER
     *  PRIMARY_PLUGIN
     *  SECONDARY_PLUGIN
     *  HOST_PLUGIN
     *
     * @param controller
     * @param cvtable
     * @param identifier
     * @param provider
     * @param maxlength
     * @param report
     *
     * @return String
     */

    private static String buildPluginProviderHeaderForC(final Controller controller,
                                                        final Hashtable<String, String> cvtable,
                                                        final String identifier,
                                                        final PluginProvider.Enum provider,
                                                        final int maxlength,
                                                        final Vector<Vector> report)
        {
        final String SOURCE = "CommandLexiconHelper.buildPluginProviderHeaderForC() ";
        final String NO_COMMANDS = "NoCommands";
        final String strPluginProviderIdentifier;
        String strIdentifier;

        strIdentifier = NO_COMMANDS;

        if ((controller != null)
            && (cvtable != null)
            && (!cvtable.isEmpty())
            && (identifier != null)
            && (provider != null))
            {
            final StringBuffer buffer;
            final String strCV;
            final Vector<Object> vecRow;

            buffer = new StringBuffer();

            if (PluginProvider.CONTROLLER.equals(provider))
                {
                // Don't use the Controller's supplied Identifier directly
                // Just use "Controller", which would be the Key in the Hashtable
                strIdentifier = provider.toString();

                // Find the CommandVariant, given the PluginIdentifier
                // Check that the CommandVariant is consistent with the PluginProvider
                // Return NULL if the CV is not found, or if there is an inconsistency
                strCV = findCV(controller, cvtable, strIdentifier, provider);
                //System.out.println("CONTROLLER FOUND CV=" + strCV);

                buildHeaderForCV(controller, strIdentifier, strCV, buffer, maxlength);
                }
            else if (PluginProvider.PRIMARY_PLUGIN.equals(provider))
                {
                // Use the supplied Identifier
                strIdentifier = identifier;

                // Check that the supplied identifier is entered in the PluginManifest as the PrimaryPlugin
                if (identifier.equals(controller.getPluginManifest().getPrimaryResourceKey()))
                    {
                    strCV = findCV(controller, cvtable, strIdentifier, provider);
                    //System.out.println("PRIMARY_PLUGIN FOUND CV=" + strCV);
                    }
                else
                    {
                    // There is an XML configuration anomaly
                    strCV = null;
                    //System.out.println(SOURCE + "PRIMARY_PLUGIN There is an XML configuration anomaly PluginManifest ResourceKey");
                    }

                buildHeaderForCV(controller, strIdentifier, strCV, buffer, maxlength);
                }
            else if (PluginProvider.SECONDARY_PLUGIN.equals(provider))
                {
                // Use the supplied Identifier
                strIdentifier = identifier;

                // Check that the supplied identifier is entered in the PluginManifest as a SecondaryPlugin
                if ((controller.getPluginManifest().getResourceKeyList() != null)
                   && (controller.getPluginManifest().getResourceKeyList().contains(identifier))
                   && (controller.getPluginManifest().getPrimaryResourceKey() != null)
                   && (!identifier.equals(controller.getPluginManifest().getPrimaryResourceKey())))
                    {
                    strCV = findCV(controller, cvtable, strIdentifier, provider);
                    //System.out.println("SECONDARY_PLUGIN FOUND CV=" + strCV);
                    }
                else
                    {
                    // There is an XML configuration anomaly
                    strCV = null;
                    //System.out.println(SOURCE + "SECONDARY_PLUGIN There is an XML configuration anomaly PluginManifest ResourceKey");
                    }

                buildHeaderForCV(controller, strIdentifier, strCV, buffer, maxlength);
                }
            else if (PluginProvider.HOST_PLUGIN.equals(provider))
                {
                //System.out.println(SOURCE + "Skip HOST_PLUGIN");
                }
            else
                {
                // Something is very wrong!
                LOGGER.error(SOURCE + "Invalid PluginProvider");
                }

            // Assemble all of the above into the Report rows
            vecRow = new Vector<Object>(COMMAND_MAP_COLUMN_COUNT);
            vecRow.add(buffer.toString());
            vecRow.add(EMPTY_STRING);

            report.add(vecRow);

            // Record the discovered Identifier of the PluginProvider
            strPluginProviderIdentifier = strIdentifier;
            }
        else
            {
            addTextLineForC(report, C_COMMENT_PREFIX + "No Commands found!");

            strPluginProviderIdentifier = strIdentifier;
            }

        return (strPluginProviderIdentifier);
        }


    /***********************************************************************************************
     * Find the CommandVariant, given the PluginIdentifier.
     * Check that the CommandVariant is consistent with the PluginProvider.
     * Return NULL if the CV is not found, or if there is an inconsistency.
     *
     * @param controller
     * @param cvtable
     * @param identifier
     * @param provider
     *
     * @return String
     */


    private static String findCV(final Controller controller,
                                 final Hashtable<String, String> cvtable,
                                 final String identifier,
                                 final PluginProvider.Enum provider)
        {
        final String SOURCE = "CommandLexiconHelper.findCV() ";
        boolean boolFoundCV;
        String strCV;

        // Assume that we will fail
        boolFoundCV = false;
        strCV = null;

        if ((controller != null)
            && (cvtable != null)
            && (!cvtable.isEmpty())
            && (provider != null)
            && (identifier != null))
            {
            final Enumeration<String> enumKeys;

            enumKeys = cvtable.keys();

            while ((enumKeys.hasMoreElements())
                && (!boolFoundCV))
                {
                final String strKeyCV;
                final String strPluginIdentifier;

                // Test each item in the table to find the specified PluginIdentifier
                strKeyCV = enumKeys.nextElement();
                strPluginIdentifier = cvtable.get(strKeyCV);

                if ((strPluginIdentifier != null)
                    && (strPluginIdentifier.equals(identifier)))
                    {
                    // strKeyCV is the CommandVariant associated with this Identifier
                    // Check that it is consistent with the specified PluginProvider

                    if (PluginProvider.CONTROLLER.equals(provider))
                        {
                        // For the purposes of the Builder, all those marked as Controller are regarded as Generic Commands
                        // strPluginIdentifier should be "Controller", all CV should be 0000
                        if (((COMMAND_VARIANT_GENERIC.equals(strKeyCV))
                             && (provider.toString().equals(strPluginIdentifier)))
                            || (controller.getIdentifier().equals(strPluginIdentifier)))
                            {
                            strCV = strKeyCV;
                            boolFoundCV = true;
                            }
                        else
                            {
                            //System.out.println("WARNING! Command with PluginProvider of Controller does not have CommandVariant of 0000");
                            strCV = null;
                            }
                        }
                    else if ((PluginProvider.PRIMARY_PLUGIN.equals(provider))
                             || (PluginProvider.SECONDARY_PLUGIN.equals(provider)))
                        {
                        // All we can do is to check that the CV != 0000
                        // Ideally check that the CV is consistent with the PluginProvider
                        if (!COMMAND_VARIANT_GENERIC.equals(strKeyCV))
                            {
                            strCV = strKeyCV;
                            boolFoundCV = true;
                            }
                        else
                            {
                            //System.out.println("WARNING! Command with PluginProvider of " + provider.toString() +" has a CommandVariant of 0000");
                            strCV = null;
                            }
                        }
                    else if (PluginProvider.HOST_PLUGIN.equals(provider))
                        {
                        //System.out.println(SOURCE + "Skip HOST plugin");
                        }
                    else
                        {
                        // Something is very wrong!
                        LOGGER.error(SOURCE + "Invalid PluginProvider");
                        }
                    }
                }
            }

        return (strCV);
        }


    /***********************************************************************************************
     * Build the Header data in the specified buffer, for the Identifier and CommandVariant.
     *
     * @param controller
     * @param identifier
     * @param commandvariant
     * @param buffer
     * @param maxlength
     */

    private static void buildHeaderForCV(final Controller controller,
                                         final String identifier,
                                         final String commandvariant,
                                         final StringBuffer buffer,
                                         final int maxlength)
        {
        if ((controller != null)
            && (identifier != null)
            && (commandvariant != null)
            && (buffer != null))
            {
            final StringBuffer bufferStringTable;

            // Function Prototype
            // void vlfPlugin(uint16_t cmd);
            // CHANGED 2013-01-23  uint8_t vlfPlugin(uint16_t cmd);
//            buffer.append("uint8_t ");
//            buffer.append(identifier.substring(0, 1).toLowerCase());
//            buffer.append(identifier.substring(1));
//            buffer.append("(uint16_t cmd);");
//            buffer.append("\n");
//            buffer.append("\n");
//
//            // CommandVariant
//            // #define NAME_OF_PLUGIN 0xCVCV
//            buffer.append("#define ");
//            buffer.append(identifier.toUpperCase());
//            buffer.append(" 0x");
//            buffer.append(commandvariant);
//            buffer.append("\n");

            //-------------------------------------------------------------------------------------
            // #define NAME_OF_PLUGIN_NAME "This is the name of the plugin"

            buffer.append("#define ");
            buffer.append(identifier.toUpperCase());
            buffer.append("_");
            buffer.append("NAME \"");
            buffer.append(identifier);
            buffer.append("\"\n");

            //-------------------------------------------------------------------------------------
            // #define NAME_OF_PLUGIN_CMDS enum \

            buffer.append("#define ");
            buffer.append(identifier.toUpperCase());
            buffer.append("_");
            buffer.append("CMDS enum \\");
            buffer.append("\n");

            buffer.append("    { \\\n");
            addSendToPortCommandsForCommandVariant(controller, commandvariant, buffer);
            buffer.append("    }; \n\n");

            //-------------------------------------------------------------------------------------
            // Add the string table of truncated command names,
            // intended for display on a maxlength character LCD

            bufferStringTable = addCommandNamesForCommandVariant(controller, commandvariant, buffer, maxlength);

            // #define MAGNETOMETERPLUGIN_STRS \
            buffer.append("#define ");
            buffer.append(identifier.toUpperCase());
            buffer.append("_");
            buffer.append("STRS \\");
            buffer.append("\n");

//            buffer.append("struct cmdStr\n");
//            buffer.append("    {\n");
//            buffer.append("    uint16_t cmd;\n");
//            buffer.append("    char * ptr;\n");
//            buffer.append("    } cmdStrs[] =\n");
//            buffer.append("        {\n");

            buffer.append(bufferStringTable);
//            buffer.append("        };\n");
            }
        }


    /***********************************************************************************************
     * Add all of the SendToPort Command names for the specified CommandVariant code,
     * as part of the C enum declaration.
     *
     * @param controller
     * @param commandvariant
     * @param buffer
     */

    private static void addSendToPortCommandsForCommandVariant(final Controller controller,
                                                               final String commandvariant,
                                                               final StringBuffer buffer)
        {
        if ((controller != null)
            && (controller.getCommandList() != null)
            && (hasSendToPort(controller.getCommandList())))
            {
            final List<CommandType> listControllerCommands;
            final Iterator<CommandType> iterControllerCommands;

            listControllerCommands = controller.getCommandList();
            iterControllerCommands = listControllerCommands.iterator();

            while (iterControllerCommands.hasNext())
                {
                final CommandType cmdController;

                cmdController = iterControllerCommands.next();

                // Only add SendToPort Commands with the specified CommandVariant
                if ((cmdController != null)
                    && (cmdController.getSendToPort())
                    && (commandvariant.equals(cmdController.getCommandVariant())))
                    {
                    // reset = 0x0000,   /* Resets the Controller */ \
                    buffer.append("    ");
                    buffer.append(cmdController.getIdentifier());
                    buffer.append(" = 0x");
                    buffer.append(controller.getCommandCodeBase());
                    buffer.append(cmdController.getCommandCode());
                    buffer.append(",    /*");
                    buffer.append(cmdController.getDescription());
                    buffer.append(" */ \\");
                    buffer.append("\n");
                    }
                }

            // Now the Controller's plugins, if any
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final Iterator<PluginType> iterPlugins;

                iterPlugins = controller.getPluginList().iterator();

                while (iterPlugins.hasNext())
                    {
                    final PluginType pluginType;

                    pluginType = iterPlugins.next();

                    // Only add Plugins which have SendToPort Commands
                    // Macros are never SendToPort, so don't use MacroList
                    if ((pluginType != null)
                        && (pluginType.getCommandList() != null)
                        && (hasSendToPort(pluginType.getCommandList())))
                        {
                        final List<CommandType> listPluginCommands;
                        final Iterator<CommandType> iterPluginCommands;

                        listPluginCommands = pluginType.getCommandList();
                        iterPluginCommands = listPluginCommands.iterator();

                        while (iterPluginCommands.hasNext())
                            {
                            final CommandType cmdPlugin;

                            cmdPlugin = iterPluginCommands.next();

                            // Only add SendToPort Commands
                            if ((cmdPlugin != null)
                                && (cmdPlugin.getSendToPort())
                                && (commandvariant.equals(cmdPlugin.getCommandVariant())))
                                {
                                buffer.append("    ");
                                buffer.append(cmdPlugin.getIdentifier());
                                buffer.append(" = 0x");
                                buffer.append(pluginType.getCommandCodeBase());
                                buffer.append(cmdPlugin.getCommandCode());
                                buffer.append(",    /*");
                                buffer.append(cmdPlugin.getDescription());
                                buffer.append(" */ \\");
                                buffer.append("\n");
                                }
                            }
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add the string table of truncated command names,
     * intended for display on a maxlength character LCD.
     * Return the map of Command enums to truncated identifiers.
     *
     * @param controller
     * @param commandvariant
     * @param buffer
     * @param maxlength
     *
     * @return StringBuffer
     */

    private static StringBuffer addCommandNamesForCommandVariant(final Controller controller,
                                                                 final String commandvariant,
                                                                 final StringBuffer buffer,
                                                                 final int maxlength)
        {
        final StringBuffer bufferStringTable;

        // Accumulate the String mapping table as the Identifiers are truncated
        bufferStringTable = new StringBuffer();

        // Some tests :-)
//        truncateIdentifierIntelligently("NamingError", maxlength);
//        truncateIdentifierIntelligently("XeXeXeXe", maxlength);
//        truncateIdentifierIntelligently("NNNNNNNN", maxlength);
//
//        truncateIdentifierIntelligently("loweronly", maxlength);
//        truncateIdentifierIntelligently("loweronly_but_longer", maxlength);
//        truncateIdentifierIntelligently("loweronlybutlongerthantherequiredlength", maxlength);
//        truncateIdentifierIntelligently("getConfigurationBlockCount", maxlength);
//        truncateIdentifierIntelligently("getConfigurationBlockCountLonger", maxlength);
//        truncateIdentifierIntelligently("getConfigurationBlockCountLongerEven", maxlength);
//        truncateIdentifierIntelligently("theQuickBrownFoxJumpsOverTheLazyDog", maxlength);
//        truncateIdentifierIntelligently("eXeXeXeX", maxlength);
//
//        truncateIdentifierIntelligently("theQuickBrownFoxJumpsOverTheLazyDog", maxlength << 1);
//        truncateIdentifierIntelligently("getConfigurationBlockCountLongerEven", maxlength << 1);
//        truncateIdentifierIntelligently("getConfigurationBlockCountLongerEvenAndLonger", maxlength << 1);
//        truncateIdentifierIntelligently("exportInstrumentXMLMULTIPLEUPPER", maxlength << 1);

        if ((controller != null)
            && (controller.getCommandList() != null)
            && (hasSendToPort(controller.getCommandList())))
            {
            final List<CommandType> listControllerCommands;
            final Iterator<CommandType> iterControllerCommands;

            listControllerCommands = controller.getCommandList();
            iterControllerCommands = listControllerCommands.iterator();

            while (iterControllerCommands.hasNext())
                {
                final CommandType cmdController;

                cmdController = iterControllerCommands.next();

                // Only add SendToPort Commands with the specified CommandVariant
                if ((cmdController != null)
                    && (cmdController.getSendToPort())
                    && (commandvariant.equals(cmdController.getCommandVariant())))
                    {
                    //     { getTemperature, "getTemperature" }, \
                    bufferStringTable.append("    { ");
                    bufferStringTable.append(cmdController.getIdentifier());
                    bufferStringTable.append(", \"");
                    bufferStringTable.append(truncateIdentifierIntelligently(cmdController.getIdentifier(),
                                                                             maxlength));
                    bufferStringTable.append("\" }, \\\n");
                    }
                }

            // Now the Controller's plugins, if any
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final Iterator<PluginType> iterPlugins;

                iterPlugins = controller.getPluginList().iterator();

                while (iterPlugins.hasNext())
                    {
                    final PluginType pluginType;

                    pluginType = iterPlugins.next();

                    // Only add Plugins which have SendToPort Commands
                    // Macros are never SendToPort, so don't use MacroList
                    if ((pluginType != null)
                        && (pluginType.getCommandList() != null)
                        && (hasSendToPort(pluginType.getCommandList())))
                        {
                        final List<CommandType> listPluginCommands;
                        final Iterator<CommandType> iterPluginCommands;

                        listPluginCommands = pluginType.getCommandList();
                        iterPluginCommands = listPluginCommands.iterator();

                        while (iterPluginCommands.hasNext())
                            {
                            final CommandType cmdPlugin;

                            cmdPlugin = iterPluginCommands.next();

                            // Only add SendToPort Commands
                            if ((cmdPlugin != null)
                                && (cmdPlugin.getSendToPort())
                                && (commandvariant.equals(cmdPlugin.getCommandVariant())))
                                {
                                //     { getTemperature, "getTemperature" }, \
                                bufferStringTable.append("    { ");
                                bufferStringTable.append(cmdPlugin.getIdentifier());
                                bufferStringTable.append(", \"");
                                bufferStringTable.append(truncateIdentifierIntelligently(cmdPlugin.getIdentifier(),
                                                                                         maxlength));
                                bufferStringTable.append("\" }, \\\n");
                                }
                            }
                        }
                    }
                }
            }

        // Remove the trailing '\' from the String table, leaving the \n
        if (bufferStringTable.length() > 2)
            {
            bufferStringTable.deleteCharAt(bufferStringTable.length() - 2);
            }

        return (bufferStringTable);
        }


    /***********************************************************************************************
     * Truncate the Identifier to maxlength intelligently, trying to preserve readability.
     * Do not remove anything before and including the first capital letter.
     * This will preserve the full identifier 'Verb'.
     * Replace 'ck' with 'k'.
     * Remove all lower-case vowels backwards from the end, in the order: u, o, a, e, i.
     *
     * @param identifier
     * @param maxlength
     *
     * @return String
     */

    private static String truncateIdentifierIntelligently(final String identifier,
                                                          final int maxlength)
        {
        final List<Character> VOWELS_LC = Arrays.asList('u', 'o', 'a', 'e', 'i');
        final StringBuffer bufferTruncated;

        bufferTruncated = new StringBuffer();

        //System.out.println("\n-------------------\ntruncateIdentifierIntelligently [identifier=" + identifier + "] [maxlength=" + maxlength + "]");

        if ((identifier != null)
            && (identifier.length() > 0))
            {
            if (identifier.length() > maxlength)
                {
                int intIndex;
                final StringBuffer bufferOriginalIdentifier;
                final StringBuffer bufferVerb;

                intIndex = 0;

                // Start with the unmodified identifier
                bufferOriginalIdentifier = new StringBuffer(identifier);

                bufferVerb = new StringBuffer();

                // Retain the 'Verb' part of the name and the initial 'Noun' letter
                // So getSampleRate retains 'getS'

                // Copy all lower case characters up to the first upper case character,
                // or the end of the original is reached,
                // or until the target length is achieved,
                // whichever comes first

                while ((intIndex < bufferOriginalIdentifier.length())
                    && (Character.isLowerCase(bufferOriginalIdentifier.charAt(intIndex)))
                    && (bufferVerb.length() < maxlength))
                    {
                    bufferVerb.append(bufferOriginalIdentifier.charAt(intIndex++));
                    }

                // Now take the next character, but only if it is upper case
                // If it isn't we either have enough already or are at the end
                // The total so far becomes the verb segment

                if ((intIndex < bufferOriginalIdentifier.length())
                    && (Character.isUpperCase(bufferOriginalIdentifier.charAt(intIndex)))
                    && (bufferVerb.length() < maxlength))
                    {
                    bufferVerb.append(bufferOriginalIdentifier.charAt(intIndex++));
                    }

//                System.out.println("VERB={" + bufferVerb
//                                    + "} verb.length=" + bufferVerb.length() + "}" );

                // Make sure that there is now a non-null Verb segment
                if ((bufferVerb.length() > 0)
                    && (intIndex > 0))
                    {
                    // Make sure there is something left to process
                    if ((bufferOriginalIdentifier.length() - bufferVerb.length()) > 0)
                        {
                        final String strOriginalPostVerb;
                        StringBuffer bufferPostVerb;

                        // Separate the post-Verb segment, and prepare to process
                        strOriginalPostVerb = bufferOriginalIdentifier.substring(intIndex);
                        bufferPostVerb = new StringBuffer(strOriginalPostVerb);

//                        System.out.println("ORIGINAL POSTVERB={" + bufferPostVerb
//                                            + "} originalpostverb.length=" + bufferPostVerb.length() + "}" );

                        if ((bufferVerb.length() + bufferPostVerb.length()) > maxlength)
                            {
                            final String strPostVerbReplaceCK;

                            // Replace any occurrence of "ck" with "k" in the post-Verb segment,
                            // lower case only, i.e. don't cross a Noun naming capitalisation boundary
                            strPostVerbReplaceCK = bufferPostVerb.toString().replaceAll("ck", "k");
                            bufferPostVerb = new StringBuffer(strPostVerbReplaceCK);

//                            System.out.println("CK REMOVED POSTVERB={" + bufferPostVerb
//                                                   + " postverb.length=" + bufferPostVerb.length()
//                                                   + " max.length=" + maxlength);

                            // Do we still need to remove more characters?
                            if ((bufferVerb.length() + bufferPostVerb.length()) > maxlength)
                                {
                                final String strStartingPostVerb;
                                int intLengthAccumulated;

                                strStartingPostVerb = bufferPostVerb.toString();
                                bufferPostVerb = new StringBuffer();
                                intLengthAccumulated = 0;

//                                System.out.println("START SCANNING POSTVERB={" + strStartingPostVerb + "}");

                                // Now some character by character scanning, because Regex would be too hard :-)
                                // Scan the entire length of the PostVerb segment, backwards
                                for (int intIndexPV = strStartingPostVerb.length()-1;
                                     (intIndexPV >= 0);
                                     intIndexPV--)
                                    {
                                    final char charTest;

                                    charTest = strStartingPostVerb.charAt(intIndexPV);

                                    // Always preserve upper case characters, since these are Noun naming boundaries
                                    if (Character.isUpperCase(charTest))
                                        {
                                        bufferPostVerb.append(charTest);
                                        intLengthAccumulated++;
//                                        System.out.println("APPENDED UPPERCASE=" + charTest);
                                        }
                                    else
                                        {
                                        // Remove lower case vowels from the end of the post-Verb segment towards the start
                                        if (VOWELS_LC.contains(charTest))
                                            {
                                            // Stop removing vowels as soon as the target *overall* length is achieved,
                                            // i.e the Verb segment plus the post-Verb segment <= maxlength
                                            // The final post-Verb segment would be made of intLengthAccumulated
                                            // and the remaining (unchanged) characters to the left of the current index
                                            // So the *current* post-Verb segment is (intIndexPV + intLengthAccumulated)
                                            if ((bufferVerb.length() + (intIndexPV + 1 + intLengthAccumulated)) > maxlength)
                                                {
                                                // Remove the character, i.e. do not copy it
//                                                System.out.println("REMOVED LOWERCASE VOWEL=" + charTest);
                                                }
                                            else
                                                {
                                                bufferPostVerb.append(charTest);
                                                intLengthAccumulated++;
//                                                System.out.println("APPENDED LOWERCASE VOWEL=" + charTest);
                                                }
                                            }
                                        else
                                            {
                                            // Preserve lower-case non-vowels
                                            bufferPostVerb.append(charTest);
                                            intLengthAccumulated++;
//                                            System.out.println("APPENDED LOWERCASE NON-VOWEL=" + charTest);
                                            }
                                        }
                                    }

                                // Reverse the final order, ready for concatenation
                                bufferPostVerb = new StringBuffer(bufferPostVerb.reverse());

                                // ToDo Replace any occurrence of "ck" with "k" in the Verb segment




                                // ToDo Almost the last resort, remove lower case vowels from the end of the Verb segment towards the start,
                                // in the order: u, o, a, e, i




                                // If all else fails, simply truncate in a Procrustean manner
                                if ((bufferVerb.length() + bufferPostVerb.length()) > maxlength)
                                    {
                                    bufferPostVerb.setLength(maxlength - bufferVerb.length());
//                                    System.out.println("PROCRUSTEAN POSTVERB={" + bufferPostVerb + "}");
                                    }
                                else
                                    {
//                                    System.out.println("achieved length #2");
                                    }
                                }
                            else
                                {
//                                System.out.println("achieved length #1");
                                }
                            }
                        else
                            {
//                            System.out.println("achieved length #0");
                            }

//                        System.out.println("FINAL POSTVERB={" + bufferPostVerb + "}");

                        // Join the Verb and post-Verb segments
                        bufferTruncated.append(bufferVerb);
                        bufferTruncated.append(bufferPostVerb);
                        }
                    else
                        {
                        // The Verb segment is all we have
                        bufferTruncated.append(bufferVerb);
//                        System.out.println("ONLY VERB={" + bufferVerb + "}");
                        }
                    }
                else
                    {
                    // We didn't find the initial lower case Verb
                    bufferTruncated.append("errorNamingConvention");
                    }
                }
            else
                {
                // There is no need for truncation
                bufferTruncated.append(identifier);
                }
            }
        else
            {
            // The Identifier is invalid
            bufferTruncated.append("errorIdentifier");
            }

//        System.out.println("ORIGINAL={" + identifier + "} TRUNCATED={" + bufferTruncated + "}");

        return (bufferTruncated.toString());
        }


    /***********************************************************************************************
     * Add a plain text line to the C source file.
     *
     * @param report
     * @param text
     */

    public static void addTextLineForC(final Vector<Vector> report,
                                       final String text)
        {
        if ((report != null)
            && (text != null))
            {
            final Vector<Object> vecRow;

            vecRow = new Vector<Object>(COMMAND_MAP_COLUMN_COUNT);

            vecRow.add(text);

            // Pad to the end of the row
            vecRow.add(EMPTY_STRING);

            report.add(vecRow);
            }
        }


    /***********************************************************************************************
     * Add the ResponseMessageStatus enum to the C Header.
     *
     * @param report
     */

    public static void addResponseMessageStatusForC(final Vector<Vector> report)
        {
        final ResponseMessageStatus[] arrayRMS;

        addTextLineForC(report, EMPTY_STRING);
        addTextLineForC(report, "// ResponseMessageStatus");
        addTextLineForC(report, EMPTY_STRING);

        arrayRMS = ResponseMessageStatus.values();

        for (int intRMSIndex = 0;
             intRMSIndex < arrayRMS.length;
             intRMSIndex++)
            {
            final ResponseMessageStatus status;
            final StringBuffer buffer;
            final Vector<Object> vecRow;

            status = arrayRMS[intRMSIndex];
            buffer = new StringBuffer();
            vecRow = new Vector<Object>(COMMAND_MAP_COLUMN_COUNT);

            // #define INVALID_PARAMETER 0x0008
            buffer.append("#define ");
            buffer.append("RMS_");
            buffer.append(status.getMnemonic().toUpperCase());
            buffer.append(" 0x");
            buffer.append(Utilities.intToFourHexString(status.getBitMask()));

            vecRow.add(buffer.toString());

            // Pad to the end of the row
            vecRow.add(EMPTY_STRING);

            report.add(vecRow);
            }

        addTextLineForC(report, EMPTY_STRING);
        addTextLineForC(report, "// End of ResponseMessageStatus");
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Indicate if the Command List contains at least one Command marked as SendToPort.
     *
     * @param commands
     *
     * @return boolean
     */

    private static boolean hasSendToPort(final List<CommandType> commands)
        {
        boolean boolHasOne;
        final Iterator<CommandType> iterCommands;

        boolHasOne = false;
        iterCommands = commands.iterator();

        while ((iterCommands.hasNext())
               && (!boolHasOne))
            {
            final CommandType commandType;

            commandType = iterCommands.next();

            if (commandType != null)
                {
                boolHasOne = commandType.getSendToPort();
                }
            }

        return (boolHasOne);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object[]
     */

    public static Object[] createWidths()
        {
        final Object [] columnWidths =
            {
            // Module Name
            "MMMMMMMMMMMMMMMMMMMMMM",

            // Identifier, Codes, Local|Port & MacroSteps
            "MMMMMMMMMMMMMMMMMMMM",
            "MM:MM:MMMM",
            "MMMMM",
            "MM",
            "MMM",

            // Description
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",

             // Response
            "MMMMMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",

            // Allow up to PARAMETER_COUNT Parameters per Command to be shown
            // ToDo Review how this could be done automatically
            "MMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",

            "MMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",

            "MMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",

            "MMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",

            "MMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",

            "MMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM",
            "MMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector<ReportColumnData>
     */

    public static Vector<ReportColumnMetadata> createColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(createWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_MODULE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The name of the Module",
                                                SwingConstants.LEFT));

        vecColumns.add(new ReportColumnMetadata(TITLE_COMMAND_IDENTIFIER,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Command Identifier",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_CODE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Command (CodeBase + Code + Variant)",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_LEGACY_CODE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The optional LegacyCode",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_LOCAL_PORT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "Local or SendToPort Command",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_MACRO_STEPS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The number of Steps in the Macro",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_COMMAND_DESCRIPTION,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Conmand Description",
                                                SwingConstants.LEFT));

        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_NAME,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Name of the Response Value",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_DATATYPE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The DataType of the Response Value",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_UNITS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Units of the Response Value",
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_REGEX,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Regular Expression for the Response",
                                                SwingConstants.LEFT));

        // Limit the number of Parameters to be shown
        for (int i = 0;
             i < PARAMETER_COUNT;
             i++)
            {
            vecColumns.add(new ReportColumnMetadata(TITLE_PARAMETER_NAME,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    "The name of Parameter " + i,
                                                    SwingConstants.LEFT));
            vecColumns.add(new ReportColumnMetadata(TITLE_PARAMETER_DATATYPE_INPUT,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    "The Input DataType of Parameter " + i,
                                                    SwingConstants.LEFT));
            vecColumns.add(new ReportColumnMetadata(TITLE_PARAMETER_DATATYPE_TRAFFIC,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    "The Bus Traffic DataType of Parameter " + i,
                                                    SwingConstants.LEFT));
            vecColumns.add(new ReportColumnMetadata(TITLE_PARAMETER_UNITS,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    "The Units of Parameter " + i,
                                                    SwingConstants.LEFT));
            vecColumns.add(new ReportColumnMetadata(TITLE_PARAMETER_REGEX,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    "The Input Regular Expression for Parameter " + i,
                                                    SwingConstants.LEFT));
            }

        return (vecColumns);
        }


    /***********************************************************************************************
     * CommandLexiconHelper.
     */

    private CommandLexiconHelper()
        {
        // Do not allow instantiation
        }
    }
