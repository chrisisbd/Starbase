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
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkXpath;
import org.lmn.fc.model.xmlbeans.instruments.*;


/***************************************************************************************************
 * XpathHelper.
 */

public final class XpathHelper
    {
    /***********************************************************************************************
     * Find the Instrument given its Identifier.
     * If found, return a **copy**, NOT the one in the Instrument XML
     *
     * @param instrumentsdoc
     * @param instrumentid
     * @param debug
     *
     * @return Instrument
     */

    public static Instrument findInstrumentFromIdentifier(final InstrumentsDocument instrumentsdoc,
                                                          final String instrumentid,
                                                          final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.findInstrumentFromIdentifier() ";
        Instrument instrument;
        final StringBuffer expression;
        final XmlObject[] selection;

        instrument = null;
        expression = new StringBuffer();

        // Try to find the Instrument given its Identifier
        expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
        expression.append(FrameworkXpath.XPATH_INSTRUMENT_FROM_IDENTIFIER);
        expression.append(instrumentid);
        expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

        FrameworkSingletons.LOGGER.debug(debug,
                     SOURCE + "Find Instrument [xpath=" + expression.toString() + "]");

        // Query the entire Instruments document to find the specific Instrument
        selection = instrumentsdoc.selectPath(expression.toString());

        if ((selection != null)
            && (selection instanceof Instrument[])
            && (selection.length == 1)
            && (selection[0] != null)
            && (selection[0] instanceof Instrument))
            {
            // This is the required Instrument
            // WARNING Make sure a **copy** of the Instrument is taken, NOT the one in the Instrument XML!
            instrument = (Instrument)selection[0].copy();

            FrameworkSingletons.LOGGER.debug(debug,
                         SOURCE + "Found Instrument [identifier=" + instrument.getIdentifier() + "]");
            }
        else
            {
            FrameworkSingletons.LOGGER.debug(debug,
                         SOURCE + "(selection != null) " + (selection != null));
            FrameworkSingletons.LOGGER.debug(debug,
                         SOURCE + "(selection instanceof Instrument[]) " + (selection instanceof Instrument[]));

            if (selection != null)
                {
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "selection.length " + selection.length);

                if (selection instanceof Instrument[])
                    {
                    FrameworkSingletons.LOGGER.debug(debug,
                                 SOURCE + "(selection[0] != null) " + (selection[0] != null));
                    FrameworkSingletons.LOGGER.debug(debug,
                                 SOURCE + "(selection[0] instanceof Instrument) " + (selection[0] instanceof Instrument));
                    }
                }
            }

        return (instrument);
        }


    /***********************************************************************************************
     * Find the Module (Core or Plugin) given its Identifier.
     * If found, return a **copy**, NOT the one in the Instrument XML
     *
     * @param instrument
     * @param moduleid
     * @param debug
     *
     * @return XmlObject
     */

    public static XmlObject findModuleFromIdentifier(final Instrument instrument,
                                                     final String moduleid,
                                                     final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.findModuleFromIdentifier() ";
        XmlObject xmlModule;

        xmlModule = null;

        // Are we trying to find a Core Command in the Controller?
        if (FrameworkXpath.CORE.equals(moduleid))
            {
            xmlModule = instrument.getController();
            }
        else
            {
            final StringBuffer expression;
            final XmlObject[] selection;

            expression = new StringBuffer();

            // Use the PluginIdentifier, since this should be unique
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER);
            expression.append(moduleid);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            FrameworkSingletons.LOGGER.debug(debug,
                         SOURCE + "Find Plugin [xpath=" + expression.toString() + "]");

            // Query from the root of the Instrument's Controller, since the Identifier could refer to any Plugin
            selection = instrument.getController().selectPath(expression.toString());

            // The Plugin should be unique, but if not, take the first
            if ((selection != null)
                && (selection instanceof PluginType[])
                && (selection.length >= 1)
                && (selection[0] != null)
                && (selection[0] instanceof PluginType))
                {
                // WARNING Make sure a **copy** of the Plugin is taken, NOT the one in the Instrument XML!
                xmlModule = selection[0].copy();

                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "Found Plugin [identifier=" + ((PluginType)xmlModule).getIdentifier() + "]");
                }
            else
                {
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "(selection != null) " + (selection != null));
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "(selection instanceof PluginType[]) " + (selection instanceof PluginType[]));

                if (selection != null)
                    {
                    FrameworkSingletons.LOGGER.debug(debug,
                                 SOURCE + "selection.length " + selection.length);

                    if (selection instanceof PluginType[])
                        {
                        FrameworkSingletons.LOGGER.debug(debug,
                                     SOURCE + "(selection[0] != null) " + (selection[0] != null));
                        FrameworkSingletons.LOGGER.debug(debug,
                                     SOURCE + "(selection[0] instanceof PluginType) " + (selection[0] instanceof PluginType));
                        }
                    }
                }
            }

        return (xmlModule);
        }


    /***********************************************************************************************
     * Find the Command given its Identifier.
     * If found, return a **copy**, NOT the one in the Instrument XML
     *
     * @param instrument
     * @param module
     * @param commandid
     * @param debug
     *
     * @return CommandType
     */

    public static CommandType findCommandFromIdentifier(final Instrument instrument,
                                                        final XmlObject module,
                                                        final String commandid,
                                                        final boolean debug)
        {
        final String SOURCE = "ExecuteCommandHelper.findCommandFromIdentifier() ";
        final StringBuffer expression;
        final XmlObject[] selection;
        CommandType command;

        command = null;
        expression = new StringBuffer();

        // Is the Command in the Core or a Plugin?
        if (module instanceof Controller)
            {
            expression.setLength(0);
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_COMMAND_FROM_IDENTIFIER);
            expression.append(commandid);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            FrameworkSingletons.LOGGER.debug(debug,
                         SOURCE + "Find Controller Command [xpath=" + expression.toString() + "]");

            // Query from the root of the **Controller** to find the Command
            selection = instrument.getController().selectPath(expression.toString());

            if ((selection != null)
                && (selection instanceof CommandType[])
                && (selection.length == 1)
                && (selection[0] != null)
                && (selection[0] instanceof CommandType))
                {
                // WARNING Make sure a **copy** of the Command is taken, NOT the one in the Instrument XML!
                command = (CommandType)selection[0].copy();

                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "Found Controller Command [identifier=" + (command.getIdentifier() + "]"));
                }
            else
                {
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "(selection != null) " + (selection != null));
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "(selection instanceof CommandType[]) " + (selection instanceof CommandType[]));

                if (selection != null)
                    {
                    FrameworkSingletons.LOGGER.debug(debug,
                                 SOURCE + "selection.length " + selection.length);

                    if (selection instanceof CommandType[])
                        {
                        FrameworkSingletons.LOGGER.debug(debug,
                                     SOURCE + "(selection[0] != null) " + (selection[0] != null));
                        FrameworkSingletons.LOGGER.debug(debug,
                                     SOURCE + "(selection[0] instanceof CommandType) " + (selection[0] instanceof CommandType));
                        }
                    }
                }
            }
        else if (module instanceof PluginType)
            {
            expression.setLength(0);
            expression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            expression.append(FrameworkXpath.XPATH_PLUGIN_COMMAND_FROM_COMMAND_IDENTIFIER);
            expression.append(commandid);
            expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

            FrameworkSingletons.LOGGER.debug(debug,
                         SOURCE + "Find Plugin Command [xpath=" + expression.toString() + "]");

            // Query from the root of the **Controller** to find the Command
            selection = instrument.getController().selectPath(expression.toString());

            if ((selection != null)
                && (selection instanceof CommandType[])
                && (selection.length == 1)
                && (selection[0] != null)
                && (selection[0] instanceof CommandType))
                {
                // WARNING Make sure a **copy** of the Command is taken, NOT the one in the Instrument XML!
                command = (CommandType)selection[0].copy();

                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "Found Plugin Command [identifier=" + (command.getIdentifier() + "]"));
                }
            else
                {
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "(selection != null) " + (selection != null));
                FrameworkSingletons.LOGGER.debug(debug,
                             SOURCE + "(selection instanceof CommandType[]) " + (selection instanceof CommandType[]));

                if (selection != null)
                    {
                    FrameworkSingletons.LOGGER.debug(debug,
                                 SOURCE + "selection.length " + selection.length);

                    if (selection instanceof CommandType[])
                        {
                        FrameworkSingletons.LOGGER.debug(debug,
                                     SOURCE + "(selection[0] != null) " + (selection[0] != null));
                        FrameworkSingletons.LOGGER.debug(debug,
                                     SOURCE + "(selection[0] instanceof CommandType) " + (selection[0] instanceof CommandType));
                        }
                    }
                }
            }
        else
            {
            FrameworkSingletons.LOGGER.error(SOURCE + "Unable to determine the type of Controller or Module");
            }

        return (command);
        }
    }
