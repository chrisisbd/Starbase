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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroStepProcessorInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.xmlbeans.instruments.*;


/***************************************************************************************************
 * MacroStepProcessor.
 */

public final class MacroStepProcessor implements MacroStepProcessorInterface
    {
    // Injections
    private final CommandProcessorContextInterface macroContext;
    private final StepType stepType;

    private Instrument instrument;
    private Controller controller;
    private PluginType plugin;
    private CommandType command;

    private boolean boolLabel;
    private boolean boolComment;
    private boolean boolStarscript;


    /***********************************************************************************************
     * Construct a MacroStepProcessor.
     *
     * @param context
     * @param step
     */

    public MacroStepProcessor(final CommandProcessorContextInterface context,
                              final StepType step)
        {
        this.macroContext = context;
        this.stepType = step;

        this.instrument = null;
        this.controller = null;
        this.plugin = null;
        this.command = null;

        this.boolLabel = false;
        this.boolComment = false;
        this.boolStarscript = false;
        }


    /***********************************************************************************************
     * Find the Instrument, Module, Command for the MacroStep in the injected Context.
     * Return the Command for convenience.
     *
     * @return CommandType
     */

    public CommandType locateStepContext()
        {
        final String SOURCE = "MacroStepProcessor.locateStepContext() ";
        CommandType commandStep;

        setInstrument(null);
        setController(null);
        setPlugin(null);
        commandStep = null;
        setCommand(commandStep);

        setLabel(false);
        setComment(false);
        setStarscript(false);

        // Check that the Injections have been set correctly
        if ((getMacroContext() != null)
            && (getMacroContext().getObservatoryUI() != null)
            && (getMacroContext().getObservatoryUI().getInstrumentsDoc() != null)
            && (getStepType() != null))
            {
            // Indicate that we have a Label
            setLabel(getStepType().getLabel() != null);

            // Is this Step just a Comment?
            if (getStepType().getComment() != null)
                {
                setComment(true);
                }
            else if (getStepType().getStarscript() != null)
                {
                final StringBuffer expression;
                XmlObject[] selection;

                setStarscript(true);
                expression = new StringBuffer();

                // Try to find the Instrument in the ObservatoryUI given its Identifier from the Starscript
                expression.append(XPATH_INSTRUMENTS_NAMESPACE);
                expression.append(XPATH_INSTRUMENT_FROM_IDENTIFIER);
                expression.append(getStepType().getStarscript().getInstrument());
                expression.append(XPATH_QUOTE_TERMINATOR);

                // Query the entire Instruments document to find the specific Instrument
                selection = getMacroContext().getObservatoryUI().getInstrumentsDoc().selectPath(expression.toString());

                if ((selection != null)
                    && (selection instanceof Instrument[])
                    && (selection.length == 1)
                    && (selection[0] != null)
                    && (selection[0] instanceof Instrument))
                    {
                    final Instrument instrumentStep;

                    // This is the Instrument
                    instrumentStep = (Instrument)selection[0];

                    if ((instrumentStep != null)
                        && (instrumentStep.getController() != null))
                        {
                        //System.out.println(SOURCE + " Instrument=" + selection[0].xmlText());

                        // Does the Command appear in the Controller or in a Plugin?
                        if ("Core".equals(getStepType().getStarscript().getModule()))
                            {
                            // Now search in the Controller to find the Command with the Identifier
                            expression.setLength(0);
                            expression.append(XPATH_INSTRUMENTS_NAMESPACE);
                            expression.append(XPATH_COMMAND_FROM_IDENTIFIER);
                            expression.append(getStepType().getStarscript().getCommand().getIdentifier());
                            expression.append(XPATH_QUOTE_TERMINATOR);

                            // Query from the root of the **Controller** to find the Command
                            selection = instrumentStep.getController().selectPath(expression.toString());

                            if ((selection != null)
                                && (selection instanceof CommandType[])
                                && (selection.length == 1)
                                && (selection[0] != null)
                                && (selection[0] instanceof CommandType))
                                {
                                commandStep = (CommandType)selection[0];

                                if (commandStep != null)
                                    {
                                    //System.out.println(SOURCE + " CommandType=" + selection[0].xmlText());

                                    // We have found all parts of the Context
                                    setInstrument(instrumentStep);
                                    setController(instrumentStep.getController());
                                    setPlugin(null);
                                    setCommand(commandStep);
                                    }
                                else
                                    {
                                    // Unable to locate the Command
                                    LOGGER.error(SOURCE + "Unable to locate the Controller Command");
                                    }
                                }
                            else
                                {
                                // XPath failed trying to find the Command
                                LOGGER.error(SOURCE + "XPath failed trying to find the Controller Command");
                                }
                            }
                        else
                            {
                            // Find the Plugin:Command to which this MacroStep relates
                            // Try to find the Module given its Identifier from the Starscript
                            expression.setLength(0);
                            expression.append(XPATH_INSTRUMENTS_NAMESPACE);
                            expression.append(XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER);
                            expression.append(getStepType().getStarscript().getModule());
                            expression.append(XPATH_QUOTE_TERMINATOR);

                            // Query from the root of the **Instrument's Controller**, since the Command could be in any Plugin
                            // e.g. /ins:Instruments/ins:Instrument/ins:Controller/ins:Plugin[ins:Identifier/text()='Macros']
                            selection = instrumentStep.getController().selectPath(expression.toString());

                            if ((selection != null)
                                && (selection instanceof PluginType[])
                                && (selection.length == 1)
                                && (selection[0] != null)
                                && (selection[0] instanceof PluginType))
                                {
                                final PluginType pluginStep;

                                pluginStep = (PluginType)selection[0];

                                if (pluginStep != null)
                                    {
                                    //System.out.println(SOURCE + " PluginType=" + selection[0].xmlText());

                                    // Now search in the Plugin to find the Command with the Identifier
                                    expression.setLength(0);
                                    expression.append(XPATH_INSTRUMENTS_NAMESPACE);
                                    expression.append(XPATH_COMMAND_FROM_IDENTIFIER);
                                    expression.append(getStepType().getStarscript().getCommand().getIdentifier());
                                    expression.append(XPATH_QUOTE_TERMINATOR);

                                    // Query from the root of the **Plugin** to find the Command
                                    selection = pluginStep.selectPath(expression.toString());

                                    if ((selection != null)
                                        && (selection instanceof CommandType[])
                                        && (selection.length == 1)
                                        && (selection[0] != null)
                                        && (selection[0] instanceof CommandType))
                                        {
                                        commandStep = (CommandType)selection[0];

                                        if (commandStep != null)
                                            {
                                            //System.out.println(SOURCE + " CommandType=" + selection[0].xmlText());

                                            // We have found all parts of the Context
                                            setInstrument(instrumentStep);
                                            setController(null);
                                            setPlugin(pluginStep);
                                            setCommand(commandStep);
                                            }
                                        else
                                            {
                                            // Unable to locate the Command
                                            LOGGER.error(SOURCE + "Unable to locate the Plugin Command");
                                            }
                                        }
                                    else
                                        {
                                        // XPath failed trying to find the Command
                                        LOGGER.error(SOURCE + "XPath failed trying to find the Plugin Command");
                                        }
                                    }
                                else
                                    {
                                    // Unable to locate the Plugin
                                    LOGGER.error(SOURCE + "Unable to locate the Plugin");
                                    }
                                }
                            else
                                {
                                // XPath failed trying to find the Plugin
                                LOGGER.error(SOURCE + "XPath failed trying to find the Plugin");
                                }
                            }
                        }
                    else
                        {
                        // Unable to locate the Instrument
                        LOGGER.error(SOURCE + "Unable to locate the Instrument, or missing Controller");
                        }
                    }
                else
                    {
                    // XPath failed trying to find the Instrument
                    LOGGER.error(SOURCE + "XPath failed trying to find the Instrument");
                    }
                }
            else
                {
                // Corrupt Macro with no content?
                LOGGER.error(SOURCE + "Corrupt Macro with no content?");
                }
            }
        else
            {
            // Invalid parameters
            LOGGER.error(SOURCE + "Invalid parameters");
            }

        // Return a NULL if we couldn't locate the Command context,
        // resetting all of the flags
        if (commandStep == null)
            {
            setInstrument(null);
            setController(null);
            setPlugin(null);
            setCommand(null);

            setLabel(false);
            setComment(false);
            setStarscript(false);
            }

        return (commandStep);
        }


    /***********************************************************************************************
     * Get the Instrument corresponding to the MacroStep.
     *
     * @return Instrument
     */

    public Instrument getInstrument()
        {
        return (this.instrument);
        }


    /***********************************************************************************************
     * Set the Instrument corresponding to the MacroStep.
     *
     * @param macroinstrument
     */

    private void setInstrument(final Instrument macroinstrument)
        {
        this.instrument = macroinstrument;
        }


    /***********************************************************************************************
     * Get the Controller corresponding to the MacroStep.
     *
     * @return Controller
     */

    public Controller getController()
        {
        return (this.controller);
        }


    /***********************************************************************************************
     * Set the Controller corresponding to the MacroStep.
     *
     * @param ctl
     */

    private void setController(final Controller ctl)
        {
        this.controller = ctl;
        }


    /***********************************************************************************************
     * Get the Plugin (Module) corresponding to the MacroStep.
     *
     * @return PluginType
     */

    public PluginType getPlugin()
        {
        return (this.plugin);
        }


    /***********************************************************************************************
     * Set the Plugin (Module) corresponding to the MacroStep.
     *
     * @param macroplugin
     */

    private void setPlugin(final PluginType macroplugin)
        {
        this.plugin = macroplugin;
        }


    /***********************************************************************************************
     * Get the Command corresponding to the MacroStep.
     *
     * @return CommandType
     */

    public CommandType getCommand()
        {
        return (this.command);
        }


    /***********************************************************************************************
     * Set the Command corresponding to the MacroStep.
     *
     * @param macrocommand
     */

    private void setCommand(final CommandType macrocommand)
        {
        this.command = macrocommand;
        }


    /***********************************************************************************************
     * Indicate if the MacroStep has a Label.
     *
     * @return boolean
     */

    public boolean hasLabel()
        {
        return (this.boolLabel);
        }


    /***********************************************************************************************
     * Indicate that the MacroStep has a Label.
     *
     * @param flag
     */

    private void setLabel(final boolean flag)
        {
        this.boolLabel = flag;
        }


    /***********************************************************************************************
     * A convenience method to get the MacroStep label, if available.
     * Return an empty String if not.
     *
     * @return String
     */

    public String getLabel()
        {
        if (hasLabel())
            {
            return (getStepType().getLabel());
            }
        else
            {
            return (EMPTY_STRING);
            }
        }


    /***********************************************************************************************
     * Indicate if the MacroStep is a Comment.
     *
     * @return boolean
     */

    public boolean isComment()
        {
        return (this.boolComment);
        }


    /***********************************************************************************************
     * Indicate that the MacroStep is a Comment.
     *
     * @param flag
     */

    private void setComment(final boolean flag)
        {
        this.boolComment = flag;
        }


    /***********************************************************************************************
     * A convenience method to get the MacroStep Comment, if available.
     * Return an empty String if not.
     *
     * @return String
     */

    public String getComment()
        {
        if (isComment())
            {
            return (getStepType().getComment());
            }
        else
            {
            return (EMPTY_STRING);
            }
        }


    /***********************************************************************************************
     * Indicate if the MacroStep is Starscript.
     *
     * @return boolean
     */

    public boolean isStarscript()
        {
        return (this.boolStarscript);
        }


    /***********************************************************************************************
     * Indicate that the MacroStep is Starscript.
     *
     * @param flag
     */

    private void setStarscript(final boolean flag)
        {
        this.boolStarscript = flag;
        }


    /***********************************************************************************************
     * A convenience method to get the MacroStep Starscript, if available.
     * Return a NULL if not.
     *
     * @return StepCommandType
     */

    public StepCommandType getStarscript()
        {
        if (isStarscript())
            {
            return (getStepType().getStarscript());
            }
        else
            {
            return (null);
            }
        }


    /***********************************************************************************************
     * Get the MacroContext being displayed.
     * Return NULL if none.
     *
     * @return MacroType
     */

    private CommandProcessorContextInterface getMacroContext()
        {
        return (this.macroContext);
        }


    /***********************************************************************************************
     * Get the Macro StepType.
     *
     * @return StepType
     */

    private StepType getStepType()
        {
        return (this.stepType);
        }
    }
