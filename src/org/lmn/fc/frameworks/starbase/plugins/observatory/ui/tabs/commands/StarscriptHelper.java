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


import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecutionContextInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.CommandProcessorContext;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.SpringUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * StarscriptHelper.
 */

public final class StarscriptHelper implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ResourceKeys
    {
    // String Resources
    private static final String TITLE_STARSCRIPT_COMMAND = STARSCRIPT + " Command";
    private static final String HTML_FONT_SETUP = "<hr><br><font  size=\"2\" face=\"Courier\">";
    private static final String HTML_COLOUR_NAME = "<font color=#6666ff>";
    private static final String HTML_COLOUR_DATATYPE = "<font color=#ff6666>";
    private static final String HTML_COLOUR_UNITS = "<font color=green>";
    private static final String HTML_FONT_SUFFIX = "</font>";
    private static final String PARAMETER_PROTOTYPE_SEPARATOR = HTML_NBSPACE + COLON + HTML_NBSPACE;
    private static final String UNDEFINED_NAME = "Undefined.Name";
    private static final String UNDEFINED_DATA_TYPE = "Undefined.DataType";
    private static final String UNDEFINED_UNITS = "Undefined.Units";
    private static final String TITLE_RESPONSE = "<br><br>Response<br>";
    private static final String NO_RESPONSE_DEFINED = "No Response defined";
    private static final String TITLE_COMMAND_METADATA = "<br><br>Command Metadata";
    private static final String FOOTNOTE_LEXICON = "<br><br><i>For further information, see the CommandLexicon</i>";

    private static final float STARSCRIPT_FONT_SIZE = 13.0f;
    private static final int HEIGHT_INDICATOR = 20;


    /**********************************************************************************************/
    /* These methods require the CommandProcessorContext                                          */
    /***********************************************************************************************
     * Create the Starscript Command Indicator.
     *
     * @param context The CommandProcessorContext
     *
     * @return JTextArea
     */

    public static JTextArea createStarscriptIndicator(final CommandProcessorContextInterface context)
        {
        final JTextArea indicator;
        final Border border;
        final TitledBorder titledBorder;

        titledBorder = BorderFactory.createTitledBorder(TITLE_STARSCRIPT_COMMAND);
        titledBorder.setTitleFont(context.getFontData().getFont());
        titledBorder.setTitleColor(context.getColourData().getColor());
        border = BorderFactory.createCompoundBorder(titledBorder,
                                                    BorderFactory.createEmptyBorder(0, 10, 5, 5));

        // The host panel contains an Indicator in a SpringLayout, to allow for expansion
        context.getStarscriptIndicatorPanel().setLayout(new SpringLayout());
        context.getStarscriptIndicatorPanel().setBorder(border);
        context.getStarscriptIndicatorPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // The Indicator
        indicator = new JTextArea(MacroManagerUtilities.PROMPT_MODULE)
           {
           // Enable Antialiasing in Java 1.5
           protected void paintComponent(final Graphics graphics)
               {
               final Graphics2D graphics2D;

               graphics2D = (Graphics2D) graphics;

               // For antialiasing text
               graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
               super.paintComponent(graphics2D);
               }
           };

        indicator.setFont(context.getFontData().getFont().deriveFont(Font.BOLD, STARSCRIPT_FONT_SIZE));
        indicator.setForeground(context.getColourData().getColor());
        indicator.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // Don't wrap the text because there's only room for one line
        indicator.setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT_INDICATOR));
        indicator.setEditable(false);
        context.getStarscriptIndicatorPanel().add(indicator);

        // Now do the layout...
        SpringUtilities.makeCompactGrid(context.getStarscriptIndicatorPanel(), 1, 1, 0, 0, 10, 1);

        return (indicator);
        }


    /***********************************************************************************************
     * Rebuild the Command Indicator panel to show the currently prepared (showing on the UI)
     * Instrument.Module.Command.Parameters.
     * The context is assumed to be not null.
     *
     * @param context The CommandProcessorContext
     */

    public static void updateStarscript(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "StarscriptHelper.updateStarscript() ";
        final Instrument instrument;
        final XmlObject module;
        final MacroType macro;
        final CommandType command;
        final boolean boolCommandReadyToExecute;

        if (context.getInstrument() != null)
            {
            instrument = context.getInstrument();
            }
        else
            {
            instrument = null;
            }

        if ((context.getSelectedModule() != null)
            && (!context.getSelectedModule().isEmpty())
            && (context.getSelectedModule().get(0) != null))
            {
            module = context.getSelectedModule().get(0);
            }
        else
            {
            module = null;
            }

        if ((context.getSelectedMacro() != null)
            && (!context.getSelectedMacro().isEmpty())
            && (context.getSelectedMacro().get(0) != null))
            {
            macro = context.getSelectedMacro().get(0);
            }
        else
            {
            macro = null;
            }

        if ((context.getSelectedCommand() != null)
            && (!context.getSelectedCommand().isEmpty())
            && (context.getSelectedCommand().get(0) != null))
            {
            command = context.getSelectedCommand().get(0);
            }
        else
            {
            command = null;
            }

        // Build the Starscript - instrument, module, macro, command may be null
        context.setStarscript(buildExpandedStarscript(context, instrument, module, macro, command, false));

        // Now display the StarScript string
        if (context.getStarscriptIndicator() != null)
            {
            context.getStarscriptIndicator().setText(context.getStarscript());
            context.getStarscriptIndicator().setToolTipText(buildExpandedStarscript(context, instrument, module, macro, command, true));
            }

        // Control the Commander buttons depending on the state of the current Command
        // We are not interested in the specific errors in this case
        boolCommandReadyToExecute = (CommandProcessorContext.isPreparedCommandReadyToExecute(context,
                                                                                             new ArrayList<String>(10)));
        if (context.getExecuteButton() != null)
            {
            context.getExecuteButton().setEnabled(boolCommandReadyToExecute);
            }

        if (context.getRepeatButton() != null)
            {
            context.getRepeatButton().setEnabled(boolCommandReadyToExecute);
            }
        }


    /***********************************************************************************************
     * Build the Starscript for the specified context, optionally formatted.
     * This *assumes* that all Parameters rendered on the Parameters Panel are passed in via the context.
     *
     * @param context
     * @param instrument
     * @param module
     * @param macro
     * @param command
     * @param formatted
     *
     * @return String
     */

    private static String buildExpandedStarscript(final CommandProcessorContextInterface context,
                                                  final Instrument instrument,
                                                  final XmlObject module,
                                                  final MacroType macro,
                                                  final CommandType command,
                                                  final boolean formatted)
        {
        final StringBuffer buffer;
        int intIndentPreamble;

        buffer = new StringBuffer();
        intIndentPreamble = 0;

        // Multiple lines are only possible if formatted as HTML
        if (formatted)
            {
            buffer.append(HTML_PREFIX);
            buffer.append("<font  size=\"3\" face=\"Courier\">");
            intIndentPreamble = buffer.length();
            }

        if (context != null)
            {
            if (instrument != null)
                {
                if (module instanceof Controller)
                    {
                    buffer.append(instrument.getIdentifier());
                    buffer.append(DOT);
                    buffer.append(((Controller)module).getIdentifier());

                    if (command != null)
                        {
                        buffer.append(DOT);
                        buffer.append(command.getIdentifier());
                        // Parameters rendered on the Parameters Panel
                        // are passed in via the context for execution
                        buffer.append(buildParameterList(context.getExecutionParameters(),
                                                         formatted,
                                                         buffer.length() - intIndentPreamble));
                        }
                    }
                else if (module instanceof PluginType)
                    {
                    buffer.append(instrument.getIdentifier());
                    buffer.append(DOT);
                    buffer.append(((PluginType)module).getIdentifier());

                    if (macro != null)
                        {
                        buffer.append(DOT);
                        buffer.append(macro.getIdentifier());
                        // Parameters rendered on the Parameters Panel
                        // are passed in via the context for execution
                        buffer.append(buildParameterList(context.getExecutionParameters(),
                                                         formatted,
                                                         buffer.length() - intIndentPreamble));
                        }
                    else if (command != null)
                        {
                        buffer.append(DOT);
                        buffer.append(command.getIdentifier());
                        // Parameters rendered on the Parameters Panel
                        // are passed in via the context for execution
                        buffer.append(buildParameterList(context.getExecutionParameters(),
                                                         formatted,
                                                         buffer.length() - intIndentPreamble));
                        }
                    }
                else
                    {
                    // There is no Module selected
                    buffer.append(MacroManagerUtilities.PROMPT_MODULE);
                    }
                }
            else
                {
                buffer.append(ObservatoryInstrumentInterface.INSTRUMENT_NOT_FOUND);
                }
            }

        if (formatted)
            {
            buffer.append(HTML_FONT_SUFFIX);
            buffer.append(HTML_SUFFIX);
            }

        return (buffer.toString());
        }


    /**********************************************************************************************/
    //* Below here does NOT require the CommandProcessorContextInterface                          */
    /***********************************************************************************************
     * Create the Starscript Command Indicator.
     *
     * @param indicatorpanel
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param enableborder
     *
     * @return JTextArea
     */

    public static JTextArea createStarscriptIndicator(final JPanel indicatorpanel,
                                                      final FontInterface fontdata,
                                                      final ColourInterface colourforeground,
                                                      final ColourInterface colourbackground,
                                                      final boolean enableborder)
        {
        final JTextArea textIndicator;

        // The host panel contains an Indicator in a SpringLayout, to allow for expansion
        indicatorpanel.setLayout(new SpringLayout());
        indicatorpanel.setBackground(colourbackground.getColor());

        if (enableborder)
            {
            final TitledBorder titledBorder;
            final Border border;

            titledBorder = BorderFactory.createTitledBorder(TITLE_STARSCRIPT_COMMAND);
            titledBorder.setTitleFont(fontdata.getFont());
            titledBorder.setTitleColor(colourforeground.getColor());
            border = BorderFactory.createCompoundBorder(titledBorder,
                                                        BorderFactory.createEmptyBorder(0, 10, 5, 5));

            indicatorpanel.setBorder(border);
            indicatorpanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
            }
        else
            {
            indicatorpanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
            indicatorpanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT_INDICATOR + 5));
            }

        // The Indicator
        textIndicator = new JTextArea(MacroManagerUtilities.PROMPT_MODULE)
            {
            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D;

                graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        textIndicator.setFont(fontdata.getFont().deriveFont(Font.BOLD, STARSCRIPT_FONT_SIZE));
        textIndicator.setForeground(colourforeground.getColor());
        textIndicator.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        textIndicator.setToolTipText("Waiting for Command selection");

        // Don't wrap the text because there's only room for one line
        textIndicator.setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT_INDICATOR));
        textIndicator.setEditable(false);
        indicatorpanel.add(textIndicator);

        // Now do the layout...
        SpringUtilities.makeCompactGrid(indicatorpanel, 1, 1, 0, 0, 10, 1);

        return (textIndicator);
        }


    /***********************************************************************************************
     * Rebuild the Command Indicator panel to show the currently prepared (showing on the UI)
     * Instrument.Module.Command.Parameters.
     *
     * @param executioncontext
     */

    public static void updateStarscript(final ExecutionContextInterface executioncontext)
        {
        final String SOURCE = "StarscriptHelper.updateStarscript(executioncontext) ";
        final boolean boolCommandReadyToExecute;

        // Now display the StarScript string
        if (executioncontext.getStarscriptIndicator() != null)
            {
            executioncontext.getStarscriptIndicator().setText(buildExpandedStarscript(executioncontext, false));
            executioncontext.getStarscriptIndicator().setToolTipText(buildExpandedStarscript(executioncontext, true));
            }

        // Control the Commander buttons depending on the state of the current Command
        // We are not interested in the specific errors in this case
        boolCommandReadyToExecute = (isPreparedCommandReadyToExecute(executioncontext,
                                                                     new ArrayList<String>(10)));
        if (executioncontext.getExecuteButton() != null)
            {
            executioncontext.getExecuteButton().setEnabled(boolCommandReadyToExecute);
            }

        if (executioncontext.getRepeatButton() != null)
            {
            executioncontext.getRepeatButton().setEnabled(boolCommandReadyToExecute);
            }

        // Keep Abort off until the Command is executing (as opposed to being ReadyToExecute)
        if (executioncontext.getAbortButton() != null)
            {
            executioncontext.getAbortButton().setEnabled(false);
            }
        }


    /***********************************************************************************************
     * Get a flag to indicate if the currently prepared Command may be executed.
     * The Instrument must be in READY.
     * Update the list of errors found if the Command is not valid, or the Instrument is not READY.
     * See very similar code in CommandProcessorContext.
     *
     * @param executioncontext
     * @param errors
     *
     * @return boolean
     */

    private static boolean isPreparedCommandReadyToExecute(final ExecutionContextInterface executioncontext,
                                                           final List<String> errors)
        {
        final String SOURCE = "StarscriptHelper.isPreparedCommandReadyToExecute() ";
        final boolean boolValid;

        // There must be a running Instrument, a currently selected Module and Command, and an Errors list
        if ((errors != null)
            && (executioncontext != null))
            {
            synchronized(errors)
                {
                errors.clear();
                errors.add(CommandProcessorContextInterface.MSG_CANNOT_EXECUTE);

                // READY means ready to be BUSY or REPEATING
                if (InstrumentState.READY.equals(executioncontext.getObservatoryInstrument().getInstrumentState()))
                    {
                    if ((executioncontext.getStarscriptModule() != null)
                        && executioncontext.isSelectedMacroOrCommand())
                        {
                        // Retrieve the Execution Parameters for the currently selected Instrument.Module.Command
                        if ((executioncontext.getStarscriptExecutionParameters() != null)
                            && (!executioncontext.getStarscriptExecutionParameters().isEmpty()))
                            {
                            int intFailures;
                            final Iterator<ParameterType> iterExecutionParameters;

                            intFailures = 0;
                            iterExecutionParameters = executioncontext.getStarscriptExecutionParameters().iterator();

                            while (iterExecutionParameters.hasNext())
                                {
                                final ParameterType parameter;

                                parameter = iterExecutionParameters.next();

                                if ((parameter != null)
                                    && (parameter.getValue() != null)
                                    && (parameter.getInputDataType() != null)
                                    && (parameter.getInputDataType().getDataTypeName() != null))
                                    {
                                    final DataTypeDictionary dataType;

                                    // Get the DataType to use for parsing the input
                                    dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString());

                                    intFailures = intFailures + DataTypeHelper.validateDataTypeOfParameterValue(parameter, dataType, errors);

                                    if (intFailures > 0)
                                        {
                                        errors.add(parameter.getName() + CommandProcessorContextInterface.MSG_PARAMETER_PARSING);
                                        }
                                    }
                                else
                                    {
                                    // This should never happen...
                                    if (parameter != null)
                                        {
                                        errors.add(parameter.getName() + CommandProcessorContextInterface.MSG_PARAMETER_NOT_SET);
                                        }
                                    else
                                        {
                                        errors.add(CommandProcessorContextInterface.MSG_PARAMETER_NULL);
                                        }

                                    intFailures++;
                                    }
                                }

                            // Are all of the Parameters valid?
                            boolValid = (intFailures == 0);
                            }
                        else
                            {
                            // If there are no Parameters, the Command must be valid...
                            // ...so no message required
                            boolValid = true;
                            }
                        }
                    else
                        {
                        errors.add(CommandProcessorContextInterface.MSG_MODULE_AND_COMMAND);
                        boolValid = false;
                        }
                    }
                else
                    {
                    // BUSY or REPEATING
                    if (InstrumentState.isOccupied(executioncontext.getObservatoryInstrument()))
                        {
                        //errors.add(ObservatoryInstrumentInterface.TOOLTIP_BUSY);
                        boolValid = false;
                        }
                    else
                        {
                        // The Instrument is switched off
                        boolValid = false;
                        }
                    }
                }

            // Ignore the first preparatory message
            if ((LOADER_PROPERTIES.isStateDebug())
                && (errors.size() > 1))
                {
                LOGGER.errors(SOURCE, errors);
                }
            }
        else
            {
            // We can't tell the User why this failed because the Error List is null...
            LOGGER.error(SOURCE + "One or more Null Parameters");

            boolValid = false;
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Build the Starscript, optionally formatted.
     *
     * @param executioncontext
     * @param formatted
     *
     * @return String
     */

    public static String buildExpandedStarscript(final ExecutionContextInterface executioncontext,
                                                 final boolean formatted)
        {
        final StringBuffer buffer;
        int intIndentPreamble;

        buffer = new StringBuffer();
        intIndentPreamble = 0;

        // Multiple lines are only possible if formatted as HTML
        if (formatted)
            {
            buffer.append(HTML_PREFIX);
            buffer.append("<font  size=\"3\" face=\"Courier\">");
            intIndentPreamble = buffer.length();
            }

        if (executioncontext.getStarscriptInstrument() != null)
            {
            if (executioncontext.getStarscriptModule() instanceof Controller)
                {
                buffer.append(executioncontext.getStarscriptInstrument().getIdentifier());
                buffer.append(DOT);
                buffer.append(((Controller)executioncontext.getStarscriptModule()).getIdentifier());

                if (executioncontext.getStarscriptCommand() != null)
                    {
                    buffer.append(DOT);
                    buffer.append(executioncontext.getStarscriptCommand().getIdentifier());
                    buffer.append(buildParameterList(executioncontext.getStarscriptExecutionParameters(),
                                                     formatted,
                                                     buffer.length() - intIndentPreamble));
                    }
                }
            else if (executioncontext.getStarscriptModule() instanceof PluginType)
                {
                buffer.append(executioncontext.getStarscriptInstrument().getIdentifier());
                buffer.append(DOT);
                buffer.append(((PluginType)executioncontext.getStarscriptModule()).getIdentifier());

                if (executioncontext.getStarscriptMacro() != null)
                    {
                    buffer.append(DOT);
                    buffer.append(executioncontext.getStarscriptMacro().getIdentifier());
                    buffer.append(buildParameterList(executioncontext.getStarscriptExecutionParameters(),
                                                     formatted,
                                                     buffer.length() - intIndentPreamble));
                    }
                else if (executioncontext.getStarscriptCommand() != null)
                    {
                    buffer.append(DOT);
                    buffer.append(executioncontext.getStarscriptCommand().getIdentifier());
                    buffer.append(buildParameterList(executioncontext.getStarscriptExecutionParameters(),
                                                     formatted,
                                                     buffer.length() - intIndentPreamble));
                    }
                }
            else
                {
                // There is no Module selected
                buffer.append(MacroManagerUtilities.PROMPT_MODULE);
                }
            }
        else
            {
            buffer.append(ObservatoryInstrumentInterface.INSTRUMENT_NOT_FOUND);
            }

        if (formatted)
            {
            buffer.append(HTML_FONT_SUFFIX);
            buffer.append(HTML_SUFFIX);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the Starscript for the specified Instrument.Module.Command, optionally formatted.
     * This *assumes* that the Parameters are taken from the Command verbatim,
     * and are not expanded, as required for e.g. the DataFilters.
     *
     * @param instrument
     * @param module
     * @param macro
     * @param command
     * @param formatted
     *
     * @return String
     */

    public static String buildSimpleStarscript(final Instrument instrument,
                                               final XmlObject module,
                                               final MacroType macro,
                                               final CommandType command,
                                               final boolean formatted)
        {
        final StringBuffer buffer;
        int intIndentPreamble;

        buffer = new StringBuffer();
        intIndentPreamble = 0;

        // Multiple lines are only possible if formatted as HTML
        if (formatted)
            {
            buffer.append(HTML_PREFIX);
            buffer.append("<font  size=\"3\" face=\"Courier\">");
            intIndentPreamble = buffer.length();
            }

        if (instrument != null)
            {
            if (module instanceof Controller)
                {
                buffer.append(instrument.getIdentifier());
                buffer.append(DOT);
                buffer.append(((Controller)module).getIdentifier());

                if (command != null)
                    {
                    buffer.append(DOT);
                    buffer.append(command.getIdentifier());
                    buffer.append(buildParameterList(command.getParameterList(),
                                                     formatted,
                                                     buffer.length() - intIndentPreamble));
                    }
                }
            else if (module instanceof PluginType)
                {
                buffer.append(instrument.getIdentifier());
                buffer.append(DOT);
                buffer.append(((PluginType)module).getIdentifier());

                if (macro != null)
                    {
                    buffer.append(DOT);
                    buffer.append(macro.getIdentifier());
                    buffer.append(buildParameterList(macro.getParameterList(),
                                                     formatted,
                                                     buffer.length() - intIndentPreamble));
                    }
                else if (command != null)
                    {
                    buffer.append(DOT);
                    buffer.append(command.getIdentifier());
                    buffer.append(buildParameterList(command.getParameterList(),
                                                     formatted,
                                                     buffer.length() - intIndentPreamble));
                    }
                }
            else
                {
                // There is no Module selected
                buffer.append(MacroManagerUtilities.PROMPT_MODULE);
                }
            }
        else
            {
            buffer.append(ObservatoryInstrumentInterface.INSTRUMENT_NOT_FOUND);
            }

        if (formatted)
            {
            buffer.append(HTML_FONT_SUFFIX);
            buffer.append(HTML_SUFFIX);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the Parameter list, indenting all lines except the first if formatted.
     *
     * @param parameters
     * @param formatted
     * @param indent
     *
     * @return String
     */

    private static String buildParameterList(final List<ParameterType> parameters,
                                             final boolean formatted,
                                             final int indent)
        {
        final StringBuffer bufferOutput;
        final int intParameterCount;

        bufferOutput = new StringBuffer();

        if ((parameters == null)
            || (parameters.isEmpty()))
            {
            intParameterCount = 0;
            }
        else
            {
            intParameterCount = parameters.size();
            }

        if (intParameterCount <= 0)
            {
            bufferOutput.append(LEFT_PARENTHESIS);
            bufferOutput.append(RIGHT_PARENTHESIS);
            }
        else
            {
            final StringBuffer bufferIndent;

            bufferOutput.append(LEFT_PARENTHESIS);

            // Record the point to indent each line if formatted
            bufferIndent = new StringBuffer();

            // There must be a better way?!
            // Take account of the left parenthesis
            for (int i = 0; i < (indent + 1); i++)
                {
                bufferIndent.append(HTML_NBSP);
                }

            // We know that intParameterCount is always > 0
            for (int intParameterIndex = 0;
                 intParameterIndex < intParameterCount;
                 intParameterIndex++)
                {
                // Process each Parameter in turn
                // Note that some may be SubParameters, but we should come to a CommandParameter first
                if ((parameters != null)
                    && (parameters.get(intParameterIndex) != null))
                    {
                    // Is it a Command Parameter...
                    if (parameters.get(intParameterIndex).getSubParameterIndex() == 0)
                        {
                        // ...but if it is not the last, is it also followed by a SubParameter?
                        if ((intParameterIndex != (intParameterCount-1))
                            && (parameters.get(intParameterIndex+1).getSubParameterIndex() > 0))
                            {
                            // Fully process the CommandParameter and SubParameter(s) before we return
                            // Return the index of the last token processed
                            intParameterIndex = renderSubParameters(parameters,
                                                                    intParameterCount,
                                                                    intParameterIndex,
                                                                    bufferOutput,
                                                                    bufferIndent,
                                                                    formatted);
                            }
                        else
                            {
                            // It's just an ordinary single Command Parameter
                            renderCommandParameter(parameters,
                                                   intParameterCount,
                                                   intParameterIndex,
                                                   bufferOutput,
                                                   bufferIndent,
                                                   formatted);
                            }
                        }
                    else
                        {
                        // This must be an error, because we didn't expect to start with a SubParameter
                        bufferOutput.append("ParameterError");
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                    }
                }

            bufferOutput.append(RIGHT_PARENTHESIS);
            }

        return (bufferOutput.toString());
        }


    /***********************************************************************************************
     * Render a single Command Parameter, terminating with a comma if it is not the last.
     *
     * @param parameters
     * @param parametercount
     * @param parameterindex
     * @param outputbuffer
     * @param indentbuffer
     * @param formatted
     */

    private static void renderCommandParameter(final List<ParameterType> parameters,
                                               final int parametercount,
                                               final int parameterindex,
                                               final StringBuffer outputbuffer,
                                               final StringBuffer indentbuffer,
                                               final boolean formatted)
        {
        if (parameters.get(parameterindex).getValue() != null)
            {
            outputbuffer.append(parameters.get(parameterindex).getValue());
            }
        else
            {
            outputbuffer.append(QUERY);
            }

        if ((parametercount > 1)
            && (parameterindex != (parametercount -1)))
            {
            outputbuffer.append(COMMA);

            // Multiple lines are only possible if formatted as HTML
            if (formatted)
                {
                outputbuffer.append(HTML_BREAK);
                outputbuffer.append(indentbuffer);
                }
            else
                {
                outputbuffer.append(SPACE);
                }
            }
        }


    /***********************************************************************************************
     * Fully process the CommandParameter and SubParameter(s).
     * Return the index of the last token processed.
     *
     * @param parameters
     * @param parametercount
     * @param commandparameterindex
     * @param outputbuffer
     * @param indentbuffer
     * @param formatted
     *
     * @return int
     */

    private static int renderSubParameters(final List<ParameterType> parameters,
                                           final int parametercount,
                                           final int commandparameterindex,
                                           final StringBuffer outputbuffer,
                                           final StringBuffer indentbuffer,
                                           final boolean formatted)
        {
        int intIndex;
        boolean boolFinishedSubParameters;

        intIndex = commandparameterindex;

        // Render the parent Command Parameter first
        if (parameters.get(intIndex).getValue() != null)
            {
            outputbuffer.append(parameters.get(intIndex).getValue());
            }
        else
            {
            // This shouldn't happen, because we expected the name of the parent CommandParameter
            outputbuffer.append("ParameterError");
            }

        // We know that there must be at least one SubParameter, so add a left parenthesis
        outputbuffer.append(LEFT_PARENTHESIS);

        // We now have parameterName( or ParameterError(

        // Now traverse the List of SubParameters, formatting in the same way as Parameters
        // Point to the first SubParameter
        intIndex++;
        boolFinishedSubParameters = false;

        for (int intRenderIndex = intIndex;
             ((!boolFinishedSubParameters)
              && (intRenderIndex < parameters.size()));
             intRenderIndex++)
            {
            final ParameterType parameterType;

            parameterType = parameters.get(intRenderIndex);
            intIndex = intRenderIndex;

            // Are we really dealing with a SubParameter?
            if (parameterType.getSubParameterIndex() > 0)
                {
                // Render the Value of the SubParameter
                if (parameterType.getValue() != null)
                    {
                    outputbuffer.append(parameterType.getValue());
                    }
                else
                    {
                    outputbuffer.append(QUERY);
                    }

                // Are there any more tokens to process beyond this one?
                if (intRenderIndex < (parametercount - 1))
                    {
                    // Inspect the *next* token in the List
                    if (parameters.get(intRenderIndex + 1) != null)
                        {
                        // Only add a trailing comma if there's another SubParameter coming
                        if (parameters.get(intRenderIndex + 1).getSubParameterIndex() > 0)
                            {
                            outputbuffer.append(COMMA);
                            outputbuffer.append(SPACE);
                            }
                        else
                            {
                            // The *next* token is a CommandParameter, so tidy up and leave
                            outputbuffer.append(RIGHT_PARENTHESIS);
                            boolFinishedSubParameters = true;
                            }
                        }
                    else
                        {
                        // Something has gone wrong?
                        outputbuffer.append("ParameterNull");
                        }
                    }
                else
                    {
                    // Nothing left, so terminate
                    outputbuffer.append(RIGHT_PARENTHESIS);
                    boolFinishedSubParameters = true;
                    }
                }
            else
                {
                // We just found a CommandParameter
                // Close the list of SubParameters
                outputbuffer.append(RIGHT_PARENTHESIS);

                // We are pointing at the *next* CommandParameter, so back up and leave
                intIndex--;
                boolFinishedSubParameters = true;
                }
            }

        // We now have parameterName(1, 2, 3)  or possibly ParameterError(1, 2, 3)
        // The index now points to the last SubParameter processed

        // If there's still more to come, add a comma
        if (intIndex < (parametercount - 1))
            {
            outputbuffer.append(COMMA);

            // Multiple lines are only possible if formatted as HTML
            if (formatted)
                {
                outputbuffer.append(HTML_BREAK);
                outputbuffer.append(indentbuffer);
                }
            else
                {
                outputbuffer.append(SPACE);
                }
            }

        return (intIndex);
        }


    /***********************************************************************************************
     * Build the formatted Starscript prototype, intended for the Help tab.
     *
     * @param commandpool
     * @param command
     *
     * @return String
     */

    public static String buildStarscriptPrototypeAsHTML(final List<String> commandpool,
                                                        final CommandType command)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (command != null)
            {
            // Multiple aligned lines are only possible if formatted as HTML
            buffer.append(HTML_FONT_SETUP);

            buffer.append(PREFIX_BOLD);
            buffer.append(command.getIdentifier());
            buffer.append(SUFFIX_BOLD);

            buffer.append(buildParameterPrototypes(command.getParameterList(),
                                                   command.getIdentifier().length()));
            buffer.append(buildResponsePrototype(command,
                                                 command.getIdentifier().length()));
            buffer.append(buildCommandAttributes(commandpool, command, 4));
            buffer.append(buildCommandMetadata(command, 4));

            buffer.append(FOOTNOTE_LEXICON);
            buffer.append(HTML_FONT_SUFFIX);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the formatted Parameter prototype list, indenting all lines by the specified amount.
     * Show the Parameter Units and DataTypes.
     *
     * @param parameters
     * @param indent
     *
     * @return String
     */

    private static String buildParameterPrototypes(final List<ParameterType> parameters,
                                                   final int indent)
        {
        final StringBuffer buffer;
        final int intParameterCount;

        buffer = new StringBuffer();

        if ((parameters == null)
            || (parameters.isEmpty()))
            {
            intParameterCount = 0;
            }
        else
            {
            intParameterCount = parameters.size();
            }

        if (intParameterCount > 0)
            {
            final StringBuffer bufferIndent;

            buffer.append(LEFT_PARENTHESIS);

            // Record the point to indent each line if formatted
            bufferIndent = new StringBuffer();

            // There must be a better way?!
            // Take account of the left parenthesis
            for (int i = 0; i < indent + 1; i++)
                {
                bufferIndent.append(HTML_NBSP);
                }

            // Process each Parameter
            for (int intParameterIndex = 0;
                 (intParameterIndex < intParameterCount);
                 intParameterIndex++)
                {
                if ((parameters != null)
                    && (parameters.get(intParameterIndex) != null))
                    {
                    buffer.append(HTML_COLOUR_NAME);
                    if (parameters.get(intParameterIndex).getName() != null)
                        {
                        buffer.append(parameters.get(intParameterIndex).getName());
                        }
                    else
                        {
                        buffer.append(PREFIX_ITALIC);
                        buffer.append(UNDEFINED_NAME);
                        buffer.append(SUFFIX_ITALIC);
                        }
                    buffer.append(HTML_FONT_SUFFIX);
                    buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);

                    buffer.append(HTML_COLOUR_DATATYPE);
                    if (parameters.get(intParameterIndex).getInputDataType() != null)
                        {
                        buffer.append(PREFIX_ITALIC);
                        buffer.append(parameters.get(intParameterIndex).getInputDataType().getDataTypeName());
                        buffer.append(SUFFIX_ITALIC);
                        }
                    else
                        {
                        buffer.append(PREFIX_ITALIC);
                        buffer.append(UNDEFINED_DATA_TYPE);
                        buffer.append(SUFFIX_ITALIC);
                        }
                    buffer.append(HTML_FONT_SUFFIX);
                    buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);

                    buffer.append(HTML_COLOUR_UNITS);
                    if (parameters.get(intParameterIndex).getUnits() != null)
                        {
                        buffer.append(PREFIX_ITALIC);
                        buffer.append(parameters.get(intParameterIndex).getUnits().toString());
                        buffer.append(SUFFIX_ITALIC);
                        }
                    else
                        {
                        buffer.append(PREFIX_ITALIC);
                        buffer.append(UNDEFINED_UNITS);
                        buffer.append(SUFFIX_ITALIC);
                        }
                    buffer.append(HTML_FONT_SUFFIX);

                    if ((intParameterCount > 1)
                        && (intParameterIndex != intParameterCount-1))
                        {
                        // Multiple lines are only possible if formatted as HTML
                        buffer.append(COMMA);
                        buffer.append(HTML_BREAK);
                        buffer.append(bufferIndent);
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                    }
                }

            // Always end with a closing parenthesis
            buffer.append(RIGHT_PARENTHESIS);
            }
        else
            {
            // If there are no Parameters, then show empty parentheses
            buffer.append(LEFT_PARENTHESIS);
            buffer.append(RIGHT_PARENTHESIS);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the formatted Response prototype.
     * Show the Response Units and DataTypes.
     *
     * @param command
     * @param indent
     *
     * @return String
     */

    private static String buildResponsePrototype(final CommandType command,
                                                 final int indent)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (command != null)
            {
            buffer.append(TITLE_RESPONSE);

            for (int i = 0; i < indent; i++)
                {
                buffer.append(HTML_NBSP);
                }

            buffer.append(LEFT_PARENTHESIS);

            // Response definition
            if (command.getResponse() != null)
                {
                buffer.append(HTML_COLOUR_NAME);
                if (command.getResponse().getName() != null)
                    {
                    buffer.append(command.getResponse().getName());
                    }
                else
                    {
                    buffer.append(PREFIX_ITALIC);
                    buffer.append(UNDEFINED_NAME);
                    buffer.append(SUFFIX_ITALIC);
                    }
                buffer.append(HTML_FONT_SUFFIX);
                buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);

                buffer.append(HTML_COLOUR_DATATYPE);
                if (command.getResponse().getDataTypeName() != null)
                    {
                    buffer.append(PREFIX_ITALIC);
                    buffer.append(command.getResponse().getDataTypeName());
                    buffer.append(SUFFIX_ITALIC);
                    }
                else
                    {
                    buffer.append(PREFIX_ITALIC);
                    buffer.append(UNDEFINED_DATA_TYPE);
                    buffer.append(SUFFIX_ITALIC);
                    }
                buffer.append(HTML_FONT_SUFFIX);
                buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);

                buffer.append(HTML_COLOUR_UNITS);
                if (command.getResponse().getUnits() != null)
                    {
                    buffer.append(PREFIX_ITALIC);
                    buffer.append(command.getResponse().getUnits().toString());
                    buffer.append(SUFFIX_ITALIC);
                    }
                else
                    {
                    buffer.append(PREFIX_ITALIC);
                    buffer.append(UNDEFINED_UNITS);
                    buffer.append(SUFFIX_ITALIC);
                    }
                buffer.append(HTML_FONT_SUFFIX);
                }

            // Ack definition
            else if (command.getAck() != null)
                {
                buffer.append(HTML_COLOUR_NAME);
                if (command.getAck().getName() != null)
                    {
                    buffer.append(command.getAck().getName());
                    }
                else
                    {
                    buffer.append(PREFIX_ITALIC);
                    buffer.append(UNDEFINED_NAME);
                    buffer.append(SUFFIX_ITALIC);
                    }
                buffer.append(HTML_FONT_SUFFIX);
                }

            // No Response defined
            else
                {
                buffer.append(PREFIX_ITALIC);
                buffer.append(NO_RESPONSE_DEFINED);
                buffer.append(SUFFIX_ITALIC);
                }

            // Always end with a closing parenthesis
            buffer.append(RIGHT_PARENTHESIS);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the formatted Command attributes, indenting all lines by the specified amount.
     *
     * @param commandpool
     * @param command
     * @param indent
     *
     * @return String
     */

    private static String buildCommandAttributes(final List<String> commandpool,
                                                 final CommandType command,
                                                 final int indent)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (command != null)
            {
            buffer.append("<br>");

            if (command.isSetBlockedDataCommand())
                {
                buffer.append("<br>This command is a composite, for the retrieval of multiple data blocks");
                }
            else if ((command.getSteppedDataCommandList() != null)
                && (!command.getSteppedDataCommandList().isEmpty()))
                {
                buffer.append("<br>This command is a composite, made up of a list of sub-commands");
                }
            else if (command.isSetIteratedDataCommand())
                {
                buffer.append("<br>This command is repeated indefinitely");
                }

            if (command.getSendToPort())
                {
                buffer.append("<br>This command is sent to the communications port");
                }
            else
                {
                if (commandpool.contains(command.getIdentifier()))
                    {
                    buffer.append("<br>This command is executed on the host computer");
                    }
                else
                    {
                    buffer.append("<br>This command is not available in the current Data Access Object (DAO)");
                    }
                }

            buffer.append("<br>Command Code ");
            buffer.append(command.getCommandCode());
            buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);
            buffer.append("Command Variant ");
            buffer.append(command.getCommandVariant());
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the formatted Command Metadata, indenting all lines by the specified amount.
     * Show the Metadata Units and DataTypes.
     *
     * @param command
     * @param indent
     *
     * @return String
     */

    private static String buildCommandMetadata(final CommandType command,
                                               final int indent)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (command != null)
            {
            final StringBuffer bufferIndent;

            bufferIndent = new StringBuffer();

            for (int i = 0; i < (indent + 1); i++)
                {
                bufferIndent.append(HTML_NBSP);
                }

            if ((command.getCommandMetadataList() != null)
                && (!command.getCommandMetadataList().isEmpty()))
                {
                final Iterator<Metadata> iterMetadata;

                buffer.append(TITLE_COMMAND_METADATA);

                iterMetadata = command.getCommandMetadataList().iterator();

                while (iterMetadata.hasNext())
                    {
                    final Metadata metadata;

                    metadata = iterMetadata.next();

                    if (metadata != null)
                        {
                        buffer.append("<br>");
                        buffer.append(bufferIndent);
                        buffer.append(HTML_COLOUR_NAME);

                        if (metadata.getKey() != null)
                            {
                            buffer.append(metadata.getKey());
                            }
                        else
                            {
                            buffer.append(PREFIX_ITALIC);
                            buffer.append(UNDEFINED_NAME);
                            buffer.append(SUFFIX_ITALIC);
                            }
                        buffer.append(HTML_FONT_SUFFIX);
                        buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);

                        buffer.append(HTML_COLOUR_DATATYPE);
                        if (metadata.getDataTypeName() != null)
                            {
                            buffer.append(PREFIX_ITALIC);
                            buffer.append(metadata.getDataTypeName());
                            buffer.append(SUFFIX_ITALIC);
                            }
                        else
                            {
                            buffer.append(PREFIX_ITALIC);
                            buffer.append(UNDEFINED_DATA_TYPE);
                            buffer.append(SUFFIX_ITALIC);
                            }
                        buffer.append(HTML_FONT_SUFFIX);
                        buffer.append(PARAMETER_PROTOTYPE_SEPARATOR);

                        buffer.append(HTML_COLOUR_UNITS);
                        if (metadata.getUnits() != null)
                            {
                            buffer.append(PREFIX_ITALIC);
                            buffer.append(metadata.getUnits().toString());
                            buffer.append(SUFFIX_ITALIC);
                            }
                        else
                            {
                            buffer.append(PREFIX_ITALIC);
                            buffer.append(UNDEFINED_UNITS);
                            buffer.append(SUFFIX_ITALIC);
                            }
                        buffer.append(HTML_FONT_SUFFIX);
                        }
                    }
                }
            }

        return (buffer.toString());
        }
    }


/***********************************************************************************************
 * Rebuild the Command Indicator panel to show the currently prepared (showing on the UI)
 * Instrument.Module.Command.Parameters.
 * The parameters are assumed to be not null.
 *
 * @param selectedinstrument
 * @param selectedmodule
 * @param selectedmacro
 * @param selectedcommand
 */

//    public static void updateStarscript(final Instrument selectedinstrument,
//                                        final XmlObject selectedmodule,
//                                        final MacroType selectedmacro,
//                                        final CommandType selectedcommand)
//        {
//        final String SOURCE = "StarscriptHelper.updateStarscript() ";
//        final boolean boolCommandReadyToExecute;
//
//        // Build the Starscript - instrument, module, macro, command may be null
//        context.setStarscript(buildExpandedStarscript(context,
//                                                      selectedinstrument,
//                                                      selectedmodule,
//                                                      selectedmacro,
//                                                      selectedcommand,
//                                                      false));
//
//        // Now display the StarScript string
//        if (context.getStarscriptIndicator() != null)
//            {
//            context.getStarscriptIndicator().setText(context.getStarscript());
//            context.getStarscriptIndicator().setToolTipText(buildExpandedStarscript(context,
//                                                                                    selectedinstrument,
//                                                                                    selectedmodule,
//                                                                                    selectedmacro,
//                                                                                    selectedcommand,
//                                                                                    true));
//            }
//
//        // Control the Commander buttons depending on the state of the current Command
//        // We are not interested in the specific errors in this case
//        boolCommandReadyToExecute = (CommandProcessorContext.isPreparedCommandReadyToExecute(context,
//                                                                                             new ArrayList<String>(10)));
//        if (context.getCopyButton() != null)
//            {
//            context.getCopyButton().setEnabled(boolCommandReadyToExecute);
//            }
//
//        if (context.getRepeatButton() != null)
//            {
//            context.getRepeatButton().setEnabled(boolCommandReadyToExecute);
//            }
//        }


