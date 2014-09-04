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

package org.lmn.fc.frameworks.starbase.portcontroller;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentStateMachine;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.panels.HTMLPanel;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.util.List;


public interface CommandProcessorContextInterface extends InstrumentStateChangedListener,
                                                          FrameworkConstants,
                                                          FrameworkStrings,
                                                          FrameworkMetadata,
                                                          FrameworkSingletons,
                                                          ResourceKeys
    {
    // String Resources  ToDo move?
    String MSG_CANNOT_EXECUTE = "The Command cannot be executed because";
    String MSG_NOT_RUNNING = "the Instrument must first be started (green button)";
    String MSG_MODULE_AND_COMMAND = "both a Module and a Command function must be selected";
    String MSG_PARAMETER_NULL = "a Parameter was NULL";
    String MSG_PARAMETER_PARSING = " could not be parsed correctly";
    String MSG_PARAMETER_NOT_SET = " has not been set";
    String MSG_PARAMETERS_INCOMPLETE = "One or more Parameters has not been set";
    String FILENAME_HELP = "CommandExecutionDefaultHelp.html";

    int MAX_MACRO_COUNT = 16;
    int MAX_COMMAND_COUNT = 16;
    int MAX_PARAMETER_COUNT = 16;


    /***********************************************************************************************
     * Get the host Observatory.
     *
     * @return ObservatoryInterface
     */

    ObservatoryInterface getObservatory();


    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getObservatoryInstrument();


    /***********************************************************************************************
     * Get the Instrument Xml.
     *
     * @return Instrument
     */

    Instrument getInstrument();


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    ObservatoryUIInterface getObservatoryUI();


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    TaskPlugin getHostTask();


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    FontInterface getFontData();


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    ColourInterface getColourData();


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Get the Finite State Machine which controls the Observatory and its Instruments.
     *
     * @return InstrumentStateMachine
     */

    InstrumentStateMachine getFSM();


    /**********************************************************************************************/
    /* Selection State                                                                            */
    /***********************************************************************************************
     * Get the currently selected Module.
     * This is a bit of a cheat, to allow passing round as a parameter...
     *
     * @return XmlObject
     */

    List<XmlObject> getSelectedModule();


    /***********************************************************************************************
     * Get the List of available Macros.
     *
     * @return List<CommandType>
     */

    List<MacroType> getAvailableMacros();


    /***********************************************************************************************
     * Get the currently selected Macro (the List should contain only one item).
     * This is a bit of a cheat, to allow passing round as a parameter...
     *
     * @return List<MacroType>
     */

    List<MacroType> getSelectedMacro();


    /***********************************************************************************************
     * Get the List of available Commands.
     *
     * @return List<CommandType>
     */

    List<CommandType> getAvailableCommands();


    /***********************************************************************************************
     * Get the currently selected Command (the List should contain only one item).
     * This is a bit of a cheat, to allow passing round as a parameter...
     *
     * @return List<CommandType>
     */

    List<CommandType> getSelectedCommand();


    /***********************************************************************************************
     * Get the List of Parameters actually executed,
     * i.e. including expansions for e.g. Filters.
     *
     * @return List<ParameterType>
     */

    List<ParameterType> getExecutionParameters();


    /***********************************************************************************************
     * Get the List of Command Parameters, i.e. as in the Instrument XML, unexpanded.
     *
     * @return List<ParameterType>
     */

    List<ParameterType> getCommandParameters();


    /***********************************************************************************************
     * Get the prepared Starscript text.
     *
     * @return String
     */

    String getStarscript();


    /***********************************************************************************************
     * Set the prepared Starscript text.
     *
     * @param text
     */

    void setStarscript(String text);


    /***********************************************************************************************
     * Get the Starscript Indicator Panel.
     *
     * @return JPanel
     */

    JPanel getStarscriptIndicatorPanel();


    /***********************************************************************************************
     * Set the Starscript Indicator Panel.
     *
     * @param panel
     */

    void setStarscriptIndicatorPanel(JPanel panel);


    /**********************************************************************************************/
    /* Command Buttons                                                                            */
    /***********************************************************************************************
     * Get the Execute Button.
     *
     * @return JButton
     */

    JButton getExecuteButton();


    /***********************************************************************************************
     * Set the Execute Button.
     *
     * @param button
     */

    void setExecuteButton(JButton button);


    /***********************************************************************************************
     * Get the Repeat Button.
     *
     * @return JButton
     */

    JButton getRepeatButton();


    /***********************************************************************************************
     * Set the Repeat Button.
     *
     * @param button
     */

    void setRepeatButton(JButton button);


    /***********************************************************************************************
     * Get the Abort Button.
     *
     * @return JButton
     */

    JButton getAbortButton();


    /***********************************************************************************************
     * Set the Abort Button.
     *
     * @param button
     */

    void setAbortButton(JButton button);


    /**********************************************************************************************/
    /* Macro Manager Buttons                                                                      */
    /***********************************************************************************************
     * Get the Record Macro Button.
     *
     * @return JButton
     */

    JButton getRecordMacroButton();


    /***********************************************************************************************
     * Set the Record Macro Button.
     *
     * @param button
     */

    void setRecordMacroButton(JButton button);


    /***********************************************************************************************
     * Get the Edit Macro Button.
     *
     * @return JButton
     */

    JButton getEditMacroButton();


    /***********************************************************************************************
     * Set the Edit Macro Button.
     *
     * @param button
     */

    void setEditMacroButton(JButton button);


    /***********************************************************************************************
     * Get the Delete Macro Button.
     *
     * @return JButton
     */

    JButton getDeleteMacroButton();


    /***********************************************************************************************
     * Set the Delete Macro Button.
     *
     * @param button
     */

    void setDeleteMacroButton(JButton button);


    /***********************************************************************************************
     * Get the Load Macro Button.
     *
     * @return JButton
     */

    JButton getLoadMacroButton();


    /***********************************************************************************************
     * Set the Load Macro Button.
     *
     * @param button
     */

    void setLoadMacroButton(JButton button);


    /***********************************************************************************************
     * Get the Save Macros Button.
     *
     * @return JButton
     */

    JButton getSaveMacroButton();


    /***********************************************************************************************
     * Set the Save Macros Button.
     *
     * @param button
     */

    void setSaveMacroButton(JButton button);


    /***********************************************************************************************
     * Get the Show Macro Button.
     *
     * @return JButton
     */

    JButton getShowMacroButton();


    /***********************************************************************************************
     * Set the Show Macro Button.
     *
     * @param button
     */

    void setShowMacroButton(JButton button);


    /**********************************************************************************************/
    /* Macro Editor Buttons                                                                       */











    /***********************************************************************************************
     * Get the Indicator which shows the current Command segment.
     *
     * @return XmlObject
     */

    JTextArea getStarscriptIndicator();


    /***********************************************************************************************
     * Set the Indicator which shows the current Command segment.
     *
     * @param indicator
     */

    void setStarscriptIndicator(JTextArea indicator);


    /***********************************************************************************************
     * Get the Module Panel.
     *
     * @return JPanel
     */
    JPanel getModulePanel();


    /***********************************************************************************************
     * Set the Module Panel.
     *
     * @param panel
     */

    void setModulePanel(JPanel panel);


    /***********************************************************************************************
     * Get the Command Panel.
     *
     * @return JPanel
     */
    JPanel getCommandPanel();


    /***********************************************************************************************
     * Set the Command Panel.
     *
     * @param panel
     */
    void setCommandPanel(JPanel panel);


    /***********************************************************************************************
     * Get the Parameter Panel.
     *
     * @return JPanel
     */
    JPanel getParameterPanel();


    /***********************************************************************************************
     * Set the Parameter Panel.
     *
     * @param panel
     */

    void setParameterPanel(JPanel panel);


    /***********************************************************************************************
     * Get the Activity Indicator List.
     *
     * @return List<ActivityIndicatorUIComponentInterface>
     */

    List<ActivityIndicatorUIComponentInterface> getActivityIndicatorList();


    /***********************************************************************************************
     * Add an ActivityIndicator to the List.
     *
     * @param activityindicator
     */

    void addActivityIndicator(ActivityIndicatorUIComponentInterface activityindicator);


    /***********************************************************************************************
     * Get the Execution Panel.
     *
     * @return JPanel
     */

    JPanel getExecutionPanel();


    /***********************************************************************************************
     * Set the Execution Panel.
     *
     * @param panel
     */

    void setExecutionPanel(JPanel panel);


    /***********************************************************************************************
     * Get the ResponseViewer UIComponent.
     *
     * @return ResponseViewerUIComponentInterface
     */

    ResponseViewerUIComponentInterface getResponseViewer();


    /***********************************************************************************************
     * Set the ResponseViewer UIComponent.
     *
     * @param viewer
     */

    void setResponseViewer(ResponseViewerUIComponentInterface viewer);


    /***********************************************************************************************
     * Get the Command HelpViewer UIComponent.
     *
     * @return HTMLPanel
     */

    HTMLPanel getCommandHelpViewer();


    /***********************************************************************************************
     * Get the Parameter HelpViewer UIComponent.
     *
     * @return HTMLPanel
     */

    HTMLPanel getParameterHelpViewer();


    /***********************************************************************************************
     * Get the  Execution Tabs.
     *
     * @return JPanel
     */

    JTabbedPane getExecutionTabs();


    /***********************************************************************************************
     * Set the Execution Tabs.
     *
     * @param tabs
     */

    void setExecutionTabs(JTabbedPane tabs);


    /***********************************************************************************************
     * Get the configuration panel for Command Repeat.
     *
     * @return JPanel
     */

    JPanel getRepeatConfig();


    /***********************************************************************************************
     * Set the configuration panel for Command Repeat.
     *
     * @param panel
     */

    void setRepeatConfig(JPanel panel);


    /***********************************************************************************************
     * Get the ViewerContainer Mode.
     *
     * @return ViewingMode
     */

    ViewingMode getViewingMode();


    /***********************************************************************************************
     * Indicate the ViewerContainer Mode.
     *
     * @param mode
     */

    void setViewingMode(ViewingMode mode);


    /***********************************************************************************************
     * Get the container of the Viewer on the CommandBuilder panel.
     *
     * @return JComponent
     */

    JComponent getViewerContainer();


    /***********************************************************************************************
     * Set the container of the Viewer on the CommandBuilder panel.
     *
     * @param component
     */

     void setViewerContainer(JComponent component);


    /***********************************************************************************************
     * Get a ReportTable of the Command Log in the Viewer on the CommandBuilder panel.
     *
     * @return ReportTablePlugin
     */

    CommandLifecycleUIComponentInterface getCommandLifecycleLog();


    /***********************************************************************************************
     * Set a ReportTable of the Command Log in the Viewer on the CommandBuilder panel.
     *
     * @param report
     */

    void setCommandLifecycleLog(CommandLifecycleUIComponentInterface report);


    /***********************************************************************************************
     * Get the MacroViewer in the Viewer on the CommandBuilder panel.
     *
     * @return MacroUIComponentInterface
     */

    MacroUIComponentInterface getMacroViewer();


    /***********************************************************************************************
     * Set a MacroViewer in the Viewer on the CommandBuilder panel.
     *
     * @param viewer
     */

    void setMacroViewer(MacroUIComponentInterface viewer);


    /***********************************************************************************************
     * Get the MacroEditor in the Viewer on the CommandBuilder panel.
     *
     * @return MacroUIComponentInterface
     */

    MacroUIComponentInterface getMacroEditor();


    /***********************************************************************************************
     * Set a MacroEditor in the Viewer on the CommandBuilder panel.
     *
     * @param editor
     */

    void setMacroEditor(MacroUIComponentInterface editor);


    /***********************************************************************************************
     * Get a ReportTable of the Instrument's Command Lexicon.
     *
     * @return ReportTablePlugin
     */

    ReportTablePlugin getCommandLexicon();


    /***********************************************************************************************
     * Set a ReportTable of the Instrument's Command Lexicon.
     *
     * @param report
     */

    void setCommandLexicon(ReportTablePlugin report);


    /**********************************************************************************************/
    /* Repeat Configuration                                                                       */
    /**********************************************************************************************
     * Get the Repeat period in milliseconds.
     *
     * @return long
     */

    long getRepeatPeriodMillis();


    /***********************************************************************************************
     * Set the Repeat period in milliseconds.
     *
     * @param millis
     */

    void setRepeatPeriodMillis(long millis);


    /***********************************************************************************************
     * Get the Repeat count.
     *
     * @return long
     */

    long getRepeatCount();


    /***********************************************************************************************
     * Set the Repeat count.
     *
     * @param count
     */

    void setRepeatCount(long count);


    /***********************************************************************************************
     * Get the Repeat Period Mode.
     *
     * @return boolean
     */

    boolean isRepeatPeriodMode();


    /***********************************************************************************************
     * Set the Repeat Period Mode.
     *
     * @param mode
     */

    void setRepeatPeriodMode(boolean mode);


    /***********************************************************************************************
     * Get the text shown on the Repeat status display (usually 'sec' or 'times').
     *
     * @return String
     */

    String getRepeatText();


    /***********************************************************************************************
     * Set the text shown on the Repeat status display.
     *
     * @param text
     */

    void setRepeatText(String text);


    /***********************************************************************************************
     * Is there a selected Macro?
     *
     * @return boolean
     */

    boolean isSelectedMacro();


    /***********************************************************************************************
     * Is there a selected Command?
     *
     * @return boolean
     */

    boolean isSelectedCommand();


    /***********************************************************************************************
     * Is there a selected Macro or Command?
     *
     * @return boolean
     */

    boolean isSelectedMacroOrCommand();


    /**********************************************************************************************/
    /* Macro Editor Buttons                                                                       */


    JButton getAddSelectionButton();


    void setAddSelectionButton(JButton button);


    JButton getMoveUpButton();


    void setMoveUpButton(JButton button);


    JButton getMoveDownButton();


    void setMoveDownButton(JButton button);


    JButton getDeleteLineButton();


    void setDeleteLineButton(JButton button);


    JButton getEditLabelButton();


    void setEditLabelButton(JButton button);


    JButton getEditCommentButton();


    void setEditCommentButton(JButton button);


    JButton getValidateButton();


    void setValidateButton(JButton button);
    }
