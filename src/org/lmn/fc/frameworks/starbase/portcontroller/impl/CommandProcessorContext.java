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

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommanderToolbarHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ParameterListBuilder;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.panels.HTMLPanel;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * CommandProcessorContext.
 */

public final class CommandProcessorContext implements CommandProcessorContextInterface
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final InstrumentStateTransitionTable TRANSITION_TABLE = InstrumentStateTransitionTable.getInstance();

    // Context Injections
    private ObservatoryInterface observatoryHost;
    private ObservatoryInstrumentInterface observatoryInstrument;
    private Instrument instrumentXml;
    private ObservatoryUIInterface observatoryUI;
    private TaskPlugin pluginTask;
    private FontInterface pluginFont;
    private ColourInterface pluginColour;
    private String strResourceKey;

    // The Finite State Machine which controls the Observatory and its Instruments
    private final InstrumentStateMachine fsm;

    // Selection state
    private final List<XmlObject> selectedModule;             // May be Controller or PluginType

    private final List<MacroType> listAvailableMacros;        // All Macros for the selected Module
    private final List<MacroType> selectedMacro;              // The currently selected Macro

    private final List<CommandType> listAvailableCommands;    // All Commands for the selected Module
    private final List<CommandType> selectedCommand;          // The currently selected Command

    private final List<ParameterType> listExecutionParameters; // All Parameters that appear on the Parameters Panel
    private final List<ParameterType> listCommandParameters;   // The Command Parameters, i.e. as in the Instrument XML, unexpanded

    private String strStarscript;                             // The Starscript text for this Command

    // UI
    private JPanel starscriptIndicatorPanel;
    private JTextArea textStarscriptIndicator;                // The Indicator showing the constructed Command
    private JPanel modulePanel;
    private JPanel commandPanel;
    private JPanel parameterPanel;
    private JPanel execPanel;
    private ResponseViewerUIComponentInterface responseViewer;
    private HTMLPanel helpViewerCommand;
    private HTMLPanel helpViewerParameter;
    private JTabbedPane execTabs;
    private JPanel repeatConfigPanel;

    private ViewingMode viewingMode;
    private JComponent compViewerContainer;
    private CommandLifecycleUIComponentInterface lifecycleUIComponent;
    private MacroUIComponentInterface macroViewerUIComponent;
    private MacroUIComponentInterface macroEditorUIComponent;

    private ReportTablePlugin lexiconUIComponent;

    // Command execution
    private final List<ActivityIndicatorUIComponentInterface> listActivityIndicator;

    private JButton buttonExecute;
    private JButton buttonRepeat;
    private JButton buttonAbort;

    // Repeat Configuration
    private long longRepeatPeriodMillis;
    private long longRepeatCount;
    private boolean boolRepeatPeriodMode;
    private String strRepeatText;

    // Macro Management
    private JButton buttonRecordMacro;
    private JButton buttonEditMacro;
    private JButton buttonDeleteMacro;
    private JButton buttonLoadMacros;
    private JButton buttonSaveMacros;
    private JButton buttonShowMacro;

    // Macro Editor
    private JButton buttonAddSelection;
    private JButton buttonMoveUp;
    private JButton buttonMoveDown;
    private JButton buttonDeleteLine;
    private JButton buttonEditLabel;
    private JButton buttonEditComment;
    private JButton buttonValidate;


    /***********************************************************************************************
     * Get a flag to indicate if the currently prepared Command may be executed.
     * The Instrument must be in READY.
     * Update the list of errors found if the Command is not valid, or the Instrument is not READY.
     *
     * @param context
     * @param errors
     *
     * @return boolean
     */

    public static boolean isPreparedCommandReadyToExecute(final CommandProcessorContextInterface context,
                                                          final List<String> errors)
        {
        final String SOURCE = "CommandProcessorContext.isPreparedCommandReadyToExecute() ";
        final boolean boolValid;

        // There must be a running Instrument, a currently selected Module and Command, and an Errors list
        if ((errors != null)
            && (context != null)
            && (context.getObservatoryInstrument() != null))
            {
            synchronized(errors)
                {
                errors.clear();
                errors.add(MSG_CANNOT_EXECUTE);

                // READY means ready to be BUSY or REPEATING
                if (InstrumentState.READY.equals(context.getObservatoryInstrument().getInstrumentState()))
                    {
                    if ((context.getSelectedModule() != null)
                        && (!context.getSelectedModule().isEmpty())
                        && context.isSelectedMacroOrCommand())
                        {
                        // Check that ALL Parameters have been set
                        if ((context.getStarscript() != null)
                            && (!context.getStarscript().contains(ParameterListBuilder.NO_AUDIO_MIXERS)))
                            {
                            // Retrieve the Execution Parameters for the currently selected Instrument.Module.Command
                            if ((context.getExecutionParameters() != null)
                                && (!context.getExecutionParameters().isEmpty()))
                                {
                                int intFailures;
                                final Iterator<ParameterType> iterExecutionParameters;

                                intFailures = 0;
                                iterExecutionParameters = context.getExecutionParameters().iterator();

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
                                            errors.add(parameter.getName() + MSG_PARAMETER_PARSING);
                                            }
                                        }
                                    else
                                        {
                                        // This should never happen...
                                        if (parameter != null)
                                            {
                                            errors.add(parameter.getName() + MSG_PARAMETER_NOT_SET);
                                            }
                                        else
                                            {
                                            errors.add(MSG_PARAMETER_NULL);
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
                            // One or more Parameters have not been set (e.g. devices unavailable)
                            errors.add(MSG_PARAMETERS_INCOMPLETE);
                            boolValid = false;
                            }
                        }
                    else
                        {
                        errors.add(MSG_MODULE_AND_COMMAND);
                        boolValid = false;
                        }
                    }
                else
                    {
                    // BUSY or REPEATING
                    if (InstrumentState.isOccupied(context.getObservatoryInstrument()))
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
     * Get the Repeat milliseconds or execution count from the specified Context.
     *
     * @param context
     *
     * @return long
     */

    public static long getRepeatNumber(final CommandProcessorContextInterface context)
        {
        final long longNumber;

        if (context != null)
            {
            if (context.isRepeatPeriodMode())
                {
                longNumber = context.getRepeatPeriodMillis() / ChronosHelper.SECOND_MILLISECONDS;
                }
            else
                {
                longNumber = context.getRepeatCount();
                }
            }
        else
            {
            longNumber = 0;
            }

        return (longNumber);
        }


    /***********************************************************************************************
     * Get the Repeat text from the specified Context.
     *
     * @param context
     *
     * @return String
     */

    public static String getRepeatText(final CommandProcessorContextInterface context)
        {
        final String strText;

        if (context != null)
            {
            if (context.isRepeatPeriodMode())
                {
                strText = SchemaUnits.SECONDS.toString();
                }
            else
                {
                strText = CommanderToolbarHelper.MSG_REPEAT_TIMES;
                }
            }
        else
            {
            strText = FrameworkStrings.EMPTY_STRING;
            }

        return (strText);
        }


    /***********************************************************************************************
     * Construct the CommandProcessorContext.
     *
     * @param observatory
     * @param task
     * @param ui
     * @param instrument
     * @param instrumentxml
     * @param instrumentstate
     * @param font
     * @param colour
     * @param resourcekey
     */

    public CommandProcessorContext(final ObservatoryInterface observatory,
                                   final TaskPlugin task,
                                   final ObservatoryUIInterface ui,
                                   final ObservatoryInstrumentInterface instrument,
                                   final Instrument instrumentxml,
                                   final InstrumentState instrumentstate,
                                   final FontInterface font,
                                   final ColourInterface colour,
                                   final String resourcekey)
        {
        if ((observatory == null)
            || (task == null)
            || (!task.validatePlugin())
            || (ui == null)
            || (instrument == null)
            || (instrumentxml == null)
            || (!XmlBeansUtilities.isValidXml(instrumentxml))
            || (instrumentstate == null)
            || (font == null)
            || (colour == null)
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey.trim())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Context
        this.observatoryHost = observatory;
        this.observatoryInstrument = instrument;
        this.instrumentXml = instrumentxml;
        this.observatoryUI = ui;
        this.pluginTask = task;
        this.pluginFont = font;
        this.pluginColour = colour;
        this.strResourceKey = resourcekey;

        // Construct and initialise the FSM given the Transition table and this context
        this.fsm = new InstrumentStateMachine(TRANSITION_TABLE.getTransitionsTable(), this);

        // Initialise the selection state
        this.selectedModule = new ArrayList<XmlObject>(1);

        this.listAvailableMacros = new ArrayList<MacroType>(MAX_MACRO_COUNT);
        this.selectedMacro = new ArrayList<MacroType>(1);

        this.listAvailableCommands = new ArrayList<CommandType>(MAX_COMMAND_COUNT);
        this.selectedCommand = new ArrayList<CommandType>(1);

        this.listExecutionParameters = new ArrayList<ParameterType>(MAX_PARAMETER_COUNT);
        this.listCommandParameters = new ArrayList<ParameterType>(MAX_PARAMETER_COUNT);

        this.strStarscript = EMPTY_STRING;

        // Initialise the UI
        this.starscriptIndicatorPanel = null;
        this.textStarscriptIndicator = null;
        this.modulePanel = null;
        this.commandPanel = null;
        this.parameterPanel = null;
        this.execPanel = null;
        this.responseViewer = null;
        this.repeatConfigPanel = null;

        // The same HelpViewers are used for all instruments,
        // but have to be instantiated each time since a tab Component can have only one parent
        this.helpViewerCommand = new HTMLPanel(RegistryModelUtilities.getHelpURL(task.getParentAtom(), FILENAME_HELP),
                                               true,
                                               UIComponentPlugin.DEFAULT_COLOUR_HELP_BACKGROUND.getColor());

        this.helpViewerParameter = new HTMLPanel(RegistryModelUtilities.getHelpURL(task.getParentAtom(), FILENAME_HELP),
                                                 true,
                                                 UIComponentPlugin.DEFAULT_COLOUR_HELP_BACKGROUND.getColor());

        this.execTabs = null;

        this.viewingMode = ViewingMode.COMMAND_LOG;
        this.compViewerContainer = null;
        this.lifecycleUIComponent = null;
        this.macroViewerUIComponent = null;
        this.macroEditorUIComponent = null;

        this.lexiconUIComponent = null;

        // Command execution
        this.listActivityIndicator = new ArrayList<ActivityIndicatorUIComponentInterface>(10);
        this.buttonExecute = null;
        this.buttonRepeat = null;
        this.buttonAbort = null;

        // Repeat Configuration
        this.longRepeatPeriodMillis = ChronosHelper.SECOND_MILLISECONDS;
        this.longRepeatCount = 1;
        this.boolRepeatPeriodMode = false;
        this.strRepeatText = EMPTY_STRING;

        // Macro Management
        this.buttonRecordMacro = null;
        this.buttonEditMacro = null;
        this.buttonDeleteMacro = null;
        this.buttonLoadMacros = null;
        this.buttonSaveMacros = null;
        this.buttonShowMacro = null;

        // Macro Editor
        this.buttonAddSelection = null;
        this.buttonMoveUp = null;
        this.buttonMoveDown = null;
        this.buttonDeleteLine = null;
        this.buttonEditLabel = null;
        this.buttonEditComment = null;
        this.buttonValidate = null;
        }


    /**********************************************************************************************/
    /* Context                                                                                    */
    /***********************************************************************************************
     * Get the host Observatory.
     *
     * @return ObservatoryInterface
     */

    public ObservatoryInterface getObservatory()
        {
        return (this.observatoryHost);
        }


    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getObservatoryInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Get the Instrument Xml.
     *
     * @return Instrument
     */

    public final Instrument getInstrument()
        {
        return (this.instrumentXml);
        }


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    public TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    public FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    public ColourInterface getColourData()
        {
        return (this.pluginColour);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Get the Finite State Machine which controls the Observatory and its Instruments.
     *
     * @return InstrumentStateMachine
     */

    public final InstrumentStateMachine getFSM()
        {
        return (this.fsm);
        }


    /**********************************************************************************************/
    /* Selection State                                                                            */
    /***********************************************************************************************
     * Get the currently selected Module.
     * This is a bit of a cheat, to allow passing round as a parameter...
     *
     * @return XmlObject
     */

    public List<XmlObject> getSelectedModule()
        {
        return (this.selectedModule);
        }


    /***********************************************************************************************
     * Get the List of available Macros.
     *
     * @return List<CommandType>
     */

    public List<MacroType> getAvailableMacros()
        {
        return (this.listAvailableMacros);
        }


    /***********************************************************************************************
     * Get the currently selected Macro (the List should contain only one item).
     * This is a bit of a cheat, to allow passing round as a parameter...
     *
     * @return List<MacroType>
     */

    public List<MacroType> getSelectedMacro()
        {
        return (this.selectedMacro);
        }


    /***********************************************************************************************
     * Get the List of available Commands.
     *
     * @return List<CommandType>
     */

    public List<CommandType> getAvailableCommands()
        {
        return (this.listAvailableCommands);
        }


    /***********************************************************************************************
     * Get the currently selected Command (the List should contain only one item).
     * This is a bit of a cheat, to allow passing round as a parameter...
     *
     * @return List<CommandType>
     */

    public List<CommandType> getSelectedCommand()
        {
        return (this.selectedCommand);
        }


    /***********************************************************************************************
     * Get the List of Parameters actually executed,
     * i.e. including expansions for e.g. Filters.
     *
     * @return List<ParameterType>
     */

    public List<ParameterType> getExecutionParameters()
        {
        return (this.listExecutionParameters);
        }


    /***********************************************************************************************
     * Get the List of Command Parameters, i.e. as in the Instrument XML, unexpanded.
     *
     * @return List<ParameterType>
     */

    public List<ParameterType> getCommandParameters()
        {
        return (this.listCommandParameters);
        }


    /***********************************************************************************************
     * Get the prepared Starscript text.
     *
     * @return String
     */

    public String getStarscript()
        {
        return (this.strStarscript);
        }


    /***********************************************************************************************
     * Set the prepared Starscript text.
     *
     * @param text
     */

    public void setStarscript(final String text)
        {
        this.strStarscript = text;
        }


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the Starscript Indicator Panel.
     *
     * @return JPanel
     */

    public JPanel getStarscriptIndicatorPanel()
        {
        return (this.starscriptIndicatorPanel);
        }


    /***********************************************************************************************
     * Set the Starscript Indicator Panel.
     *
     * @param panel
     */

    public void setStarscriptIndicatorPanel(final JPanel panel)
        {
        this.starscriptIndicatorPanel = panel;
        }


    /**********************************************************************************************/
    /* Command Buttons                                                                            */
    /***********************************************************************************************
     * Get the Execute Button.
     *
     * @return JButton
     */

    public JButton getExecuteButton()
        {
        return (this.buttonExecute);
        }


    /***********************************************************************************************
     * Set the Execute Button.
     *
     * @param button
     */

    public void setExecuteButton(final JButton button)
        {
        this.buttonExecute = button;
        }


    /***********************************************************************************************
     * Get the Repeat Button.
     *
     * @return JButton
     */

    public JButton getRepeatButton()
        {
        return (this.buttonRepeat);
        }


    /***********************************************************************************************
     * Set the Repeat Button.
     *
     * @param button
     */

    public void setRepeatButton(final JButton button)
        {
        this.buttonRepeat = button;
        }


    /***********************************************************************************************
     * Get the Abort Button.
     *
     * @return JButton
     */

    public JButton getAbortButton()
        {
        return (this.buttonAbort);
        }


    /***********************************************************************************************
     * Set the Abort Button.
     *
     * @param button
     */

    public void setAbortButton(final JButton button)
        {
        this.buttonAbort = button;
        }


    /**********************************************************************************************/
    /* Macro Manager Buttons                                                                      */
    /***********************************************************************************************
     * Get the Record Macro Button.
     *
     * @return JButton
     */

    public JButton getRecordMacroButton()
        {
        return (this.buttonRecordMacro);
        }


    /***********************************************************************************************
     * Set the Record Macro Button.
     *
     * @param button
     */

    public void setRecordMacroButton(final JButton button)
        {
        this.buttonRecordMacro = button;
        }


    /***********************************************************************************************
     * Get the Edit Macro Button.
     *
     * @return JButton
     */

    public JButton getEditMacroButton()
        {
        return (this.buttonEditMacro);
        }


    /***********************************************************************************************
     * Set the Edit Macro Button.
     *
     * @param button
     */

    public void setEditMacroButton(final JButton button)
        {
        this.buttonEditMacro = button;
        }


    /***********************************************************************************************
     * Get the Delete Macro Button.
     *
     * @return JButton
     */

    public JButton getDeleteMacroButton()
        {
        return (this.buttonDeleteMacro);
        }


    /***********************************************************************************************
     * Set the Delete Macro Button.
     *
     * @param button
     */

    public void setDeleteMacroButton(final JButton button)
        {
        this.buttonDeleteMacro = button;
        }


    /***********************************************************************************************
     * Get the Load Macros Button.
     *
     * @return JButton
     */

    public JButton getLoadMacroButton()
        {
        return (this.buttonLoadMacros);
        }


    /***********************************************************************************************
     * Set the Load Macros Button.
     *
     * @param button
     */

    public void setLoadMacroButton(final JButton button)
        {
        this.buttonLoadMacros = button;
        }


    /***********************************************************************************************
     * Get the Save Macros Button.
     *
     * @return JButton
     */

    public JButton getSaveMacroButton()
        {
        return (this.buttonSaveMacros);
        }


    /***********************************************************************************************
     * Set the Save Macros Button.
     *
     * @param button
     */

    public void setSaveMacroButton(final JButton button)
        {
        this.buttonSaveMacros = button;
        }


    /***********************************************************************************************
     * Get the Show Macro Button.
     *
     * @return JButton
     */

    public JButton getShowMacroButton()
        {
        return (this.buttonShowMacro);
        }


    /***********************************************************************************************
     * Set the Show Macro Button.
     *
     * @param button
     */

    public void setShowMacroButton(final JButton button)
        {
        this.buttonShowMacro = button;
        }


    /**********************************************************************************************/
    /* Macro Editor Buttons                                                                       */


    public JButton getAddSelectionButton()
        {
        return buttonAddSelection;
        }


    public void setAddSelectionButton(final JButton button)
        {
        this.buttonAddSelection = button;
        }


    public JButton getMoveUpButton()
        {
        return buttonMoveUp;
        }


    public void setMoveUpButton(final JButton button)
        {
        this.buttonMoveUp = button;
        }


    public JButton getMoveDownButton()
        {
        return buttonMoveDown;
        }


    public void setMoveDownButton(final JButton button)
        {
        this.buttonMoveDown = button;
        }


    public JButton getDeleteLineButton()
        {
        return buttonDeleteLine;
        }


    public void setDeleteLineButton(final JButton button)
        {
        this.buttonDeleteLine = button;
        }


    public JButton getEditLabelButton()
        {
        return buttonEditLabel;
        }


    public void setEditLabelButton(final JButton button)
        {
        this.buttonEditLabel = button;
        }


    public JButton getEditCommentButton()
        {
        return buttonEditComment;
        }


    public void setEditCommentButton(final JButton button)
        {
        this.buttonEditComment = button;
        }


    public JButton getValidateButton()
        {
        return buttonValidate;
        }


    public void setValidateButton(final JButton button)
        {
        this.buttonValidate = button;
        }


    /***********************************************************************************************
     * Get the Indicator which shows the current Command segment.
     *
     * @return JTextArea
     */

    public JTextArea getStarscriptIndicator()
        {
        return (this.textStarscriptIndicator);
        }


    /***********************************************************************************************
     * Set the Indicator which shows the current Command segment.
     *
     * @param indicator
     */

    public void setStarscriptIndicator(final JTextArea indicator)
        {
        this.textStarscriptIndicator = indicator;
        }


    /***********************************************************************************************
     * Get the Module Panel.
     *
     * @return JPanel
     */

    public JPanel getModulePanel()
        {
        return modulePanel;
        }


    /***********************************************************************************************
     * Set the Module Panel.
     *
     * @param panel
     */

    public void setModulePanel(final JPanel panel)
        {
        this.modulePanel = panel;
        }


    /***********************************************************************************************
     * Get the Command Panel.
     *
     * @return JPanel
     */

    public JPanel getCommandPanel()
        {
        return commandPanel;
        }


    /***********************************************************************************************
     * Set the Command Panel.
     *
     * @param panel
     */

    public void setCommandPanel(final JPanel panel)
        {
        this.commandPanel = panel;
        }


    /***********************************************************************************************
     * Get the Parameter Panel.
     *
     * @return JPanel
     */

    public JPanel getParameterPanel()
        {
        return parameterPanel;
        }


    /***********************************************************************************************
     * Set the Parameter Panel.
     *
     * @param panel
     */

    public void setParameterPanel(final JPanel panel)
        {
        this.parameterPanel = panel;
        }


    /***********************************************************************************************
     * Get the Activity Indicator.
     *
     * @return ActivityIndicatorUIComponentInterface
     */

    public List<ActivityIndicatorUIComponentInterface> getActivityIndicatorList()
        {
        return (this.listActivityIndicator);
        }


    /***********************************************************************************************
     * Add an ActivityIndicator to the List.
     *
     * @param activityindicator
     */

    public void addActivityIndicator(final ActivityIndicatorUIComponentInterface activityindicator)
        {
        if ((getActivityIndicatorList() != null)
            && (!getActivityIndicatorList().contains(activityindicator)))
            {
            getActivityIndicatorList().add(activityindicator);
            }
        }


    /***********************************************************************************************
     * Get the Execution Panel.
     *
     * @return JPanel
     */

    public JPanel getExecutionPanel()
        {
        return (this.execPanel);
        }


    /***********************************************************************************************
     * Set the Execution Panel.
     *
     * @param panel
     */

    public void setExecutionPanel(final JPanel panel)
        {
        this.execPanel = panel;
        }


    /***********************************************************************************************
     * Get the ResponseViewer UIComponent.
     *
     * @return ResponseViewerUIComponentInterface
     */

    public ResponseViewerUIComponentInterface getResponseViewer()
        {
        return (this.responseViewer);
        }


    /***********************************************************************************************
     * Set the ResponseViewer UIComponent.
     *
     * @param viewer
     */

    public void setResponseViewer(final ResponseViewerUIComponentInterface viewer)
        {
        this.responseViewer = viewer;
        }


    /***********************************************************************************************
     * Get the Command HelpViewer UIComponent.
     *
     * @return HTMLPanel
     */

    public HTMLPanel getCommandHelpViewer()
        {
        return (this.helpViewerCommand);
        }


    /***********************************************************************************************
     * Get the Parameter HelpViewer UIComponent.
     *
     * @return HTMLPanel
     */

    public HTMLPanel getParameterHelpViewer()
        {
        return (this.helpViewerParameter);
        }


    /***********************************************************************************************
     * Get the  Execution Tabs.
     *
     * @return JPanel
     */

    public JTabbedPane getExecutionTabs()
        {
        return (this.execTabs);
        }


    /***********************************************************************************************
     * Set the Execution Tabs.
     *
     * @param tabs
     */

    public void setExecutionTabs(final JTabbedPane tabs)
        {
        this.execTabs = tabs;
        }


    /***********************************************************************************************
     * Get the configuration panel for Command Repeat.
     *
     * @return JPanel
     */

    public JPanel getRepeatConfig()
        {
        return (this.repeatConfigPanel);
        }


    /***********************************************************************************************
     * Set the configuration panel for Command Repeat.
     *
     * @param panel
     */

    public void setRepeatConfig(final JPanel panel)
        {
        this.repeatConfigPanel = panel;
        }


    /***********************************************************************************************
     * Get the ViewerContainer Mode.
     *
     * @return ViewingMode
     */

    public ViewingMode getViewingMode()
        {
        return (this.viewingMode);
        }


    /***********************************************************************************************
     * Set the ViewerContainer Mode.
     *
     * @param mode
     */

    public void setViewingMode(final ViewingMode mode)
        {
        this.viewingMode = mode;
        }


    /***********************************************************************************************
     * Get the container of the Viewer on the CommandBuilder panel.
     *
     * @return JComponent
     */

    public JComponent getViewerContainer()
        {
        return (this.compViewerContainer);
        }


    /***********************************************************************************************
     * Set the container of the Viewer on the CommandBuilder panel.
     *
     * @param component
     */

    public void setViewerContainer(final JComponent component)
        {
        this.compViewerContainer = component;
        }


    /***********************************************************************************************
     * Get a ReportTable of the Command Log in the Viewer on the CommandBuilder panel.
     *
     * @return CommandLifecycleUIComponentInterface
     */

    public CommandLifecycleUIComponentInterface getCommandLifecycleLog()
        {
        return (this.lifecycleUIComponent);
        }


    /***********************************************************************************************
     * Set a ReportTable of the Command Log in the Viewer on the CommandBuilder panel.
     *
     * @param report
     */

    public void setCommandLifecycleLog(final CommandLifecycleUIComponentInterface report)
        {
        this.lifecycleUIComponent = report;
        }


    /***********************************************************************************************
     * Get the MacroViewer in the Viewer on the CommandBuilder panel.
     *
     * @return MacroUIComponentInterface
     */

    public MacroUIComponentInterface getMacroViewer()
        {
        return (this.macroViewerUIComponent);
        }


    /***********************************************************************************************
     * Set a MacroViewer in the Viewer on the CommandBuilder panel.
     *
     * @param viewer
     */

    public void setMacroViewer(final MacroUIComponentInterface viewer)
        {
        this.macroViewerUIComponent = viewer;
        }


    /***********************************************************************************************
     * Get the MacroEditor in the Viewer on the CommandBuilder panel.
     *
     * @return MacroUIComponentInterface
     */

    public MacroUIComponentInterface getMacroEditor()
        {
        return (this.macroEditorUIComponent);
        }


    /***********************************************************************************************
     * Set a MacroEditor in the Viewer on the CommandBuilder panel.
     *
     * @param editor
     */

    public void setMacroEditor(final MacroUIComponentInterface editor)
        {
        this.macroEditorUIComponent = editor;
        }


    /***********************************************************************************************
     * Get a ReportTable of the Instrument's Command Lexicon.
     *
     * @return ReportTablePlugin
     */

    public ReportTablePlugin getCommandLexicon()
        {
        return (this.lexiconUIComponent);
        }


    /***********************************************************************************************
     * Set a ReportTable of the Instrument's Command Lexicon.
     *
     * @param report
     */

    public void setCommandLexicon(final ReportTablePlugin report)
        {
        this.lexiconUIComponent = report;
        }


    /**********************************************************************************************/
    /* Repeat Configuration                                                                       */
    /**********************************************************************************************
     * Get the Repeat period in milliseconds.
     *
     * @return long
     */

    public long getRepeatPeriodMillis()
        {
        return (this.longRepeatPeriodMillis);
        }


    /***********************************************************************************************
     * Set the Repeat period in milliseconds.
     *
     * @param millis
     */

    public void setRepeatPeriodMillis(final long millis)
        {
        this.longRepeatPeriodMillis = millis;
        }


    /***********************************************************************************************
     * Get the Repeat count.
     *
     * @return long
     */

    public long getRepeatCount()
        {
        return (this.longRepeatCount);
        }


    /***********************************************************************************************
     * Set the Repeat count.
     *
     * @param count
     */

    public void setRepeatCount(final long count)
        {
        this.longRepeatCount = count;
        }


    /***********************************************************************************************
     * Get the Repeat Period Mode.
     *
     * @return boolean
     */

    public boolean isRepeatPeriodMode()
        {
        return (this.boolRepeatPeriodMode);
        }


    /***********************************************************************************************
     * Set the Repeat Period Mode.
     *
     * @param mode
     */

    public void setRepeatPeriodMode(final boolean mode)
        {
        this.boolRepeatPeriodMode = mode;
        }


    /***********************************************************************************************
     * Get the text shown on the Repeat status display (usually 'sec' or 'times').
     *
     * @return String
     */

    public String getRepeatText()
        {
        return (this.strRepeatText);
        }


    /***********************************************************************************************
     * Set the text shown on the Repeat status display.
     *
     * @param text
     */

    public void setRepeatText(final String text)
        {
        this.strRepeatText = text;
        }


    /***********************************************************************************************
     * Is there a selected Macro?
     *
     * @return boolean
     */

    public boolean isSelectedMacro()
        {
        return ((getSelectedMacro() != null)
                && (getSelectedMacro().size() == 1));
        }


    /***********************************************************************************************
     * Is there a selected Command?
     *
     * @return boolean
     */

    public boolean isSelectedCommand()
        {
        return ((getSelectedCommand() != null)
                && (getSelectedCommand().size() == 1));
        }


    /***********************************************************************************************
     * Is there a selected Macro or Command?
     *
     * @return boolean
     */

    public boolean isSelectedMacroOrCommand()
        {
        return (isSelectedMacro() || isSelectedCommand());
        }


    /**********************************************************************************************/
    /* Instrument State                                                                           */
    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     * This calls the InstrumentStateMachine, which changes the Context and the controls.
     * This Event is mainly used by e.g. ControlPanels to update displays.
     *
     * @param event
     */

    public synchronized void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        final String SOURCE = "CommandProcessorContext.instrumentChanged() ";

        // Do nothing if we are given invalid parameters
        if ((event != null)
            && (getFSM() != null))
            {
            final InstrumentState nextState;

            // The Instrument is not actually *put* into the next state until the transition
            // is finished, so beware interrogating with getInstrumentState() at the wrong time
            // For instance, InstrumentState.isOccupied() looks at the *Instrument* for the state,
            // not the current or next states in the Context

            nextState = getFSM().doTransition(event.getCurrentState(),
                                              event.getNextState());

            // Transfer the calculated NextState to the Instrument
            if ((nextState != null)
                && (!InstrumentState.ERROR.equals(nextState))
                && (event.getInstrument() != null))
                {
                final boolean boolCommandReadyToExecute;

                // Put the Instrument into the NextState
                // The only other call is in AbstractObservatoryInstrument.initialise()
//                LOGGER.log(SOURCE + "InstrumentStateMachine going to state "
//                              + nextState);
                event.getInstrument().setInstrumentState(nextState);

                // Only *now* can we check if the execute buttons can be enabled
                // We are not interested in the specific errors in this case
                boolCommandReadyToExecute = isPreparedCommandReadyToExecute(this, new ArrayList<String>(10));

                if ((getExecuteButton() != null)
                    && (getExecuteButton().isEnabled())
                    && (!boolCommandReadyToExecute))
                    {
                    getExecuteButton().setEnabled(boolCommandReadyToExecute);
                    }

                if ((getRepeatButton() != null)
                    && (getRepeatButton().isEnabled())
                    && (!boolCommandReadyToExecute))
                    {
                    getRepeatButton().setEnabled(boolCommandReadyToExecute);
                    }
                }
            else
                {
                // ERROR - something is very wrong
//                LOGGER.error(SOURCE + "InstrumentStateMachine ERROR, going to state "
//                                + InstrumentState.STOPPED);
                event.getInstrument().setInstrumentState(InstrumentState.STOPPED);
                }
            }
        else
            {
            LOGGER.error(SOURCE + "InstrumentStateMachine unable to process Transition");
            }
        }
    }
