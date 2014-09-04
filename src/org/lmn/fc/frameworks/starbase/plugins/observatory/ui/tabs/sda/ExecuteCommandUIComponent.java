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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;

import info.clearthought.layout.TableLayout;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentStateTransition;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecuteCommandUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExecutionContextInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.*;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * ExecuteCommandUIComponent.
 */

public abstract class ExecuteCommandUIComponent extends UIComponent
                                                implements ExecuteCommandUIComponentInterface
    {
    // TableLayout row and column size definitions
    private static final double[][] size =
        {
            { // Columns
            TableLayout.FILL
            },
            { // Rows
            TableLayout.PREFERRED,  // Toolbar
            TableLayout.PREFERRED,  // Starscript
            TableLayout.FILL        // Parameters
            }
        };

    // TableLayout constraints
    // The horizontal justification is specified before the vertical justification
    private static final String[] constraints =
        { // Column, Row, JustificationX, JustificationY
        "0, 0, CENTER, TOP",       // Toolbar
        "0, 1, CENTER, CENTER",    // Starscript
        "0, 2, CENTER, TOP"        // Parameters
        };

    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final String strInstrumentID;
    private final String strModuleID;
    private final String strCommandID;
    private final FontInterface fontData;
    private final ColourInterface colourData;

    // The Instrument.Module.Command to execute for the Export operation
    private final ExecutionContextInterface executionContext;

    // UI Components for Command Execution
    private JToolBar toolbarExecute;
    private ActivityIndicatorUIComponentInterface activityIndicator;
    private JButton buttonExecute;
    private JButton buttonAbort;
    private JButton buttonViewer;
    private JTextArea textStarscriptIndicator;


    /***********************************************************************************************
     * Create the Activity Indicator.
     *
     * @param executeui
     *
     * @return ActivityIndicatorUIComponentInterface
     */

    public static ActivityIndicatorUIComponentInterface createActivityIndicator(final ExecuteCommandUIComponentInterface executeui)
        {
        final String SOURCE = "ActivityIndicatorUIComponent.createActivityIndicator() ExecuteCommandUIComponent ";
        final ActivityIndicatorUIComponentInterface activityIndicator;

        activityIndicator = new ActivityIndicatorUIComponent(executeui.getObservatoryInstrument().getContext())
            {
            public void addStateTransition(final CommandProcessorContextInterface context,
                                           final InstrumentStateTransition transition)
                {
                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                       SOURCE + "--> addStateTransition() [transition=" + transition.getCommandStatusText()
                                       + "] [host.instrument=" + context.getObservatoryInstrument().getInstrument().getIdentifier() + "]");

                super.addStateTransition(context, transition);
                }

            public void initialiseUI()
                {
                final String SOURCE = "ActivityIndicatorUIComponent.initialiseUI() ";

                super.initialiseUI();
                }
            };

        return (activityIndicator);
        }


    /***********************************************************************************************
     * Create the Command Abort button.
     *
     * @param importerui
     *
     * @return JButton
     */

    public static JButton createAbortButton(final ExecuteCommandUIComponentInterface importerui)
        {
        final String SOURCE = "ExecuteCommandUIComponent.createAbortButton() ";
        final JButton buttonAbort;

        // The Abort button
        buttonAbort = new JButton(BUTTON_ABORT);

        buttonAbort.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonAbort.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonAbort.setMinimumSize(DIM_BUTTON_TOOLBAR_ABORT);
        buttonAbort.setMaximumSize(DIM_BUTTON_TOOLBAR_ABORT);
        buttonAbort.setPreferredSize(DIM_BUTTON_TOOLBAR_ABORT);
        buttonAbort.setFont(importerui.getFontData().getFont());
        buttonAbort.setForeground(importerui.getColourData().getColor());

        buttonAbort.setEnabled(false);
        buttonAbort.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_ABORT);

        buttonAbort.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                final ExecutionContextInterface context;
                final List<String> listErrors;
                final boolean boolDebug;

                boolDebug = (LOADER_PROPERTIES.isChartDebug()
                             || LOADER_PROPERTIES.isStaribusDebug()
                             || LOADER_PROPERTIES.isStarinetDebug()
                             || LOADER_PROPERTIES.isMetadataDebug()
                             || LOADER_PROPERTIES.isThreadsDebug()
                             || LOADER_PROPERTIES.isStateDebug());

                LOGGER.debug(boolDebug,
                             SOURCE + "ABORT NOW!");

                listErrors = new ArrayList<String>(10);

                context = importerui.getExecutionContext();

                // It is not necessary to check for 'unavailable'
                if ((context != null)
                    && (context.getObservatoryInstrument() != null)
                    && (InstrumentState.isDoingSomething(context.getObservatoryInstrument()))
                    && (context.getExecutionDAO() != null)
                    && (context.isSelectedMacroOrCommand()))
                    {
                    // TODO Abort Macro??

                    // If the Port isn't busy, it is not executing a Command!
                    // Attempt to stop the Command using the Execution DAO, on another Thread
                    context.getExecutionDAO().abortCommand(context.getObservatoryInstrument(),
                                                           context.getStarscriptInstrument(),
                                                           context.getStarscriptModule(),
                                                           context.getStarscriptCommand(),
                                                           listErrors);

                    // Disable the Abort button, since the Abort should complete very soon
                    if ((importerui.getExecutionContext() != null)
                        && (importerui.getExecutionContext().getAbortButton() != null))
                        {
                        importerui.getExecutionContext().getAbortButton().setEnabled(false);
                        }

                    // See if anything bad happened
                    if (!listErrors.isEmpty())
                        {
                        JOptionPane.showMessageDialog(null,
                                                      listErrors.toArray(),
                                                      context.getStarscriptInstrument().getName() + CommanderToolbarHelper.TITLE_DIALOG_COMMAND_ABORT,
                                                      JOptionPane.ERROR_MESSAGE);
                        }
                    }
                else
                    {
                    // Is the Instrument STOPPED, or in a faulty state?
                    if (context != null)
                        {
                        if ((InstrumentState.isOff(context.getObservatoryInstrument())))
                            {
                            CommandProcessorUtilities.showUnavailableDialog(context.getObservatoryInstrument());
                            }
                        else
                            {
                            // Force a reset of the Instrument state, just in case...
                            LOGGER.debug(boolDebug,
                                         SOURCE + "Force a reset of the Instrument state to READY, during ABORT");

                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 "READY_ERROR_COMMAND EXECUTION");
                            // Leave the Abort button enabled, so that the User can try again
                            // Are there any errors?
                            ExecuteCommandHelper.handleErrors(context,
                                                              listErrors,
                                                              CommanderToolbarHelper.TITLE_DIALOG_COMMAND_ABORT);
                            }
                        }
                    else
                        {
                        LOGGER.debug(boolDebug,
                                     SOURCE + "Execution Context was NULL during ABORT, no action taken");
                        }
                    }
                }
            });

        return (buttonAbort);
        }


    /***********************************************************************************************
     * Construct an ExecuteCommandUIComponent.
     *
     * @param obsinstrument
     * @param instrumentid
     * @param moduleid
     * @param commandid
     * @param fontdata
     * @param colourdata
     */

    public ExecuteCommandUIComponent(final ObservatoryInstrumentInterface obsinstrument,
                                     final String instrumentid,
                                     final String moduleid,
                                     final String commandid,
                                     final FontInterface fontdata,
                                     final ColourInterface colourdata)
        {
        super();

        // Injections
        this.observatoryInstrument = obsinstrument;
        this.strInstrumentID = instrumentid;
        this.strModuleID = moduleid;
        this.strCommandID = commandid;
        this.fontData = fontdata;
        this.colourData = colourdata;

        this.executionContext = new ExecutionContext();

        this.toolbarExecute = null;
        this.activityIndicator = null;
        this.buttonExecute = null;
        this.buttonAbort = null;
        this.buttonViewer = null;
        this.textStarscriptIndicator = null;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "ExecuteCommandUIComponent.initialiseUI() ";
        final UIComponentPlugin panelParameters;
        final JPanel panelStarscript;
        final List<String> errors;
        final boolean boolDebug;

        errors = new ArrayList<String>(10);
        boolDebug = false;

        super.initialiseUI();
        setLayout(new TableLayout(size));

        // Create the Exporter JToolBar
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // Initialise the toolbar
        initialiseToolbar();
        add(getToolBar(), constraints[0]);

        panelStarscript = new JPanel();
        setStarscriptIndicator(StarscriptHelper.createStarscriptIndicator(panelStarscript,
                                                                          getFontData(),
                                                                          getColourData(),
                                                                          UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND,
                                                                          false));

        // We should now have all of the UI context for the command execution
        getExecutionContext().resetCommandContext();
        getExecutionContext().resetUIContext();
        getExecutionContext().setStarscriptIndicator(getStarscriptIndicator());
        getExecutionContext().setExecuteButton(getExecuteButton());
        getExecutionContext().setRepeatButton(null);
        getExecutionContext().setAbortButton(getAbortButton());

        // Try to initialise the Parameters panel for the given Starscript command
        panelParameters = ExecuteCommandHelper.renderCommandParameters(getObservatoryInstrument(),
                                                                       getExecutionContext(),
                                                                       getInstrumentIdentifier(),
                                                                       getModuleIdentifier(),
                                                                       getCommandIdentifier(),
                                                                       getFontData(),
                                                                       getColourData(),
                                                                       UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND,
                                                                       errors,
                                                                       boolDebug);
        if ((panelParameters != null)
            && (errors.isEmpty()))
            {
            panelParameters.initialiseUI();

            // Initialise the Starscript to the command with uninitialised parameters
            getStarscriptIndicator().setText(StarscriptHelper.buildSimpleStarscript(getExecutionContext().getStarscriptInstrument(),
                                                                                    getExecutionContext().getStarscriptModule(),
                                                                                    null,
                                                                                    getExecutionContext().getStarscriptCommand(),
                                                                                    false));
            add(panelStarscript, constraints[1]);
            add((Component)panelParameters, constraints[2]);
            }
        else
            {
            LOGGER.errors(SOURCE,  errors);

            getExecutionContext().resetCommandContext();

            add(new BlankUIComponent("Command Execution Error!",
                                     DEFAULT_COLOUR_CANVAS,
                                     COLOUR_WARN_TEXT),
                constraints[2]);
            }

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "ExecuteCommandUIComponent.runUI() ";

        super.runUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[ui.state=" + getUIState().getName() + "]");

        if (getActivityIndicator() != null)
            {
            getActivityIndicator().runUI();
            }

        // Update the Command to show any changes from the parsers
        // This will disable the Abort button
        StarscriptHelper.updateStarscript(getExecutionContext());
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "ExecuteCommandUIComponent.stopUI() ";

        if (getActivityIndicator() != null)
            {
            getActivityIndicator().stopUI();
            }

        super.stopUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "ExecuteCommandUIComponent.disposeUI() ";

        if (getActivityIndicator() != null)
            {
            if ((getObservatoryInstrument().getContext() != null)
                && (getObservatoryInstrument().getContext().getActivityIndicatorList() != null))
                {
                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                       SOURCE + "Remove ActivityIndicator from Instrument Context");

                getObservatoryInstrument().getContext().getActivityIndicatorList().remove(getActivityIndicator());
                }

            getActivityIndicator().disposeUI();
            setActivityIndicator(null);
            }

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        getExecutionContext().resetCommandContext();
        getExecutionContext().setStarscriptIndicator(null);
        getExecutionContext().setExecuteButton(null);
        getExecutionContext().setRepeatButton(null);

        super.disposeUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Initialise the Execute Command Toolbar.
     */

    protected abstract void initialiseToolbar();


    /***********************************************************************************************
     * Get the JToolBar.
     *
     * @return JToolBar
     */

    public JToolBar getToolBar()
        {
        return (this.toolbarExecute);
        }


    /***********************************************************************************************
     * Set the JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolbarExecute = toolbar;
        }


    /***********************************************************************************************
     * Get the Activity Indicator.
     *
     * @return ActivityIndicatorUIComponentInterface
     */

    public ActivityIndicatorUIComponentInterface getActivityIndicator()
        {
        return (this.activityIndicator);
        }


    /***********************************************************************************************
     * Set the Activity Indicator.
     *
     * @param activityindicator
     */

    public void setActivityIndicator(final ActivityIndicatorUIComponentInterface activityindicator)
        {
        this.activityIndicator = activityindicator;
        }


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


    /***********************************************************************************************
     * Get the Viewer Button.
     *
     * @return JButton
     */

    public JButton getViewerButton()
        {
        return (this.buttonViewer);
        }


    /***********************************************************************************************
     * Set the Viewer Button.
     *
     * @param button
     */

    public void setViewerButton(final JButton button)
        {
        this.buttonViewer = button;
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
     * Get the ExecutionContext.
     *
     * @return ExecutionContextInterface
     */

    public ExecutionContextInterface getExecutionContext()
        {
        return (this.executionContext);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
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
     * Get the Instrument Identifier for Command Execution.
     *
     * @return String
     */

    public String getInstrumentIdentifier()
        {
        return (this.strInstrumentID);
        }


    /***********************************************************************************************
     * Get the Module Identifier for Command Execution.
     *
     * @return String
     */

    public String getModuleIdentifier()
        {
        return (this.strModuleID);
        }


    /***********************************************************************************************
     * Get the Command Identifier for Command Execution.
     *
     * @return String
     */

    public String getCommandIdentifier()
        {
        return (this.strCommandID);
        }


    /***********************************************************************************************
     * Get the FontData.
     *
     * @return FontPlugin
     */

    public FontInterface getFontData()
        {
        return (this.fontData);
        }


    /***********************************************************************************************
     * Get the ColourData.
     *
     * @return ColourPlugin
     */

    public ColourInterface getColourData()
        {
        return (this.colourData);
        }
    }
