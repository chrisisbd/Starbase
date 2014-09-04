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
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ExecutionStatus;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentStateTransition;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ExecuteMacroHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * CommanderToolbarHelper.
 */

public final class CommanderToolbarHelper implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkSingletons,
                                                     ResourceKeys
    {
    // String Resources
    public static final String TITLE_COMMAND_EXEC = "Command Execution";
    private static final String TITLE_COMMAND_REPEAT = "Command Repeat";
    private static final String TITLE_COMMAND_ABORT = "Command Abort";
    private static final String BUTTON_EXECUTE = "Execute";
    private static final String BUTTON_REPEAT = "Repeat";
    private static final String BUTTON_ABORT = "Abort";
    public static final String TITLE_DIALOG_COMMAND_EXECUTION = SPACE + TITLE_COMMAND_EXEC;
    private static final String TITLE_DIALOG_COMMAND_REPEAT = SPACE + TITLE_COMMAND_REPEAT;
    public static final String TITLE_DIALOG_COMMAND_ABORT = SPACE + TITLE_COMMAND_ABORT;
    public static final String MSG_REPEAT_TIMES = "times";
    private static final String MSG_REPEAT_TIME_0 = "The Repeat time should not be less than the Timeout time";
    private static final String MSG_REPEAT_TIME_1 = "In the event of timeouts, some repeats may not occur";
    private static final String MSG_REPEAT_TIME_2 = "If you need to repeat more quickly, you should ideally change the timeout first";
    private static final String MSG_REPEAT_TIME_3 = "Check the EventLog to see if there are any problems";

    private static final int HEIGHT_COMMANDER_TOOLBAR = 40;
    private static final Dimension DIM_BUTTON_TOOLBAR_EXECUTE = new Dimension(60, CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_TOOLBAR_REPEAT = new Dimension(60, CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_TEXT_REPEAT_CONFIG = new Dimension(32, CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_TOOLBAR_ABORT = new Dimension(60, CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final int REPEAT_STRUT_WIDTH = 4;

    public static final float EXEC_PANEL_ALIGNMENT = Component.LEFT_ALIGNMENT;
    private static final int PERIOD_REPEAT_DEFAULT_SECS = 30;
    private static final long PERIOD_REPEAT_MAX_SECS = 100000L;
    private static final Color COLOR_BORDER_REPEAT = new Color(200, 200, 200);


    /***********************************************************************************************
     * Create the Commander Panel.
     * This consists of one or more Execution control buttons, arranged horizontally.
     *
     * @param context The CommandProcessorContext
     *
     * @return Component
     */

    public static Component createCommanderToolbar(final CommandProcessorContextInterface context)
        {
        final JPanel panelCommander;
        final JPanel panelRepeat;
        final int intRepeatPanelWidth;
        final int intRepeatPanelHeight;
        final ActivityIndicatorUIComponentInterface activityIndicator;

        panelCommander = new JPanel();
        panelCommander.setLayout(new BoxLayoutFixed(panelCommander, BoxLayoutFixed.X_AXIS));
        panelCommander.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCommander.setAlignmentY(Component.CENTER_ALIGNMENT);
        panelCommander.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        panelCommander.setMinimumSize(new Dimension(Integer.MAX_VALUE, HEIGHT_COMMANDER_TOOLBAR));
        panelCommander.setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT_COMMANDER_TOOLBAR));
        panelCommander.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT_COMMANDER_TOOLBAR));

        // Three struts, repeat button and text entry box, and border width
        intRepeatPanelWidth = (REPEAT_STRUT_WIDTH * 3)
                                    + (int)DIM_BUTTON_TOOLBAR_REPEAT.getWidth()
                                    + (int)DIM_TEXT_REPEAT_CONFIG.getWidth()
                                    + 2;
        intRepeatPanelHeight = (int)DIM_BUTTON_TOOLBAR_REPEAT.getHeight() + 8;

        panelRepeat = new JPanel();
        panelRepeat.setLayout(new BoxLayoutFixed(panelRepeat, BoxLayoutFixed.X_AXIS));
        panelRepeat.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_REPEAT));
        panelRepeat.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelRepeat.setAlignmentY(Component.CENTER_ALIGNMENT);
        panelRepeat.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        panelRepeat.setMinimumSize(new Dimension(intRepeatPanelWidth, intRepeatPanelHeight));
        panelRepeat.setPreferredSize(new Dimension(intRepeatPanelWidth, intRepeatPanelHeight));
        panelRepeat.setMaximumSize(new Dimension(intRepeatPanelWidth, intRepeatPanelHeight));

        // Add the Status Indicator and Command buttons
        // Activity, Status, Execute, Repeat, RepeatConfig, Abort

        // Activity Indicator must be initialised,
        // because it is a UIComponent - it may get complicated later on!
        activityIndicator = createActivityIndicator(context);
        activityIndicator.initialiseUI();
        context.addActivityIndicator(activityIndicator);

        // Use the first entry as the Instrument's main indicator
        panelCommander.add(Box.createHorizontalStrut(3));
        panelCommander.add((Component)activityIndicator);
        panelCommander.add(Box.createHorizontalStrut(9));

        context.setExecuteButton(createExecuteButton(context));
        panelCommander.add(context.getExecuteButton());
        panelCommander.add(Box.createHorizontalStrut(5));

        // Repeat Button and the repeat period box
        panelRepeat.add(Box.createHorizontalStrut(REPEAT_STRUT_WIDTH));
        context.setRepeatButton(createRepeatButton(context));
        panelRepeat.add(context.getRepeatButton());
        panelRepeat.add(Box.createHorizontalStrut(REPEAT_STRUT_WIDTH));
        context.setRepeatConfig(createRepeatConfig(context));
        panelRepeat.add(context.getRepeatConfig());
        panelRepeat.add(Box.createHorizontalStrut(REPEAT_STRUT_WIDTH));

        panelCommander.add(panelRepeat);
        panelCommander.add(Box.createHorizontalStrut(5));

        context.setAbortButton(createAbortButton(context));
        panelCommander.add(context.getAbortButton());
        panelCommander.add(Box.createHorizontalStrut(4));

        panelCommander.add(Box.createHorizontalGlue());

        return (panelCommander);
        }


    /***********************************************************************************************
     * Create the Activity Indicator.
     *
     * @param context
     *
     * @return ActivityIndicatorUIComponentInterface
     */

    private static ActivityIndicatorUIComponentInterface createActivityIndicator(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "ActivityIndicatorUIComponent.createActivityIndicator() CommanderToolbar ";
        final ActivityIndicatorUIComponentInterface activityIndicator;

        activityIndicator = new ActivityIndicatorUIComponent(context)
            {
            public void addStateTransition(final CommandProcessorContextInterface context,
                                           final InstrumentStateTransition transition)
                {
                LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                       SOURCE + "--> addStateTransition() [transition=" + transition.getCommandStatusText()
                                       + "] [host.instrument=" + context.getObservatoryInstrument().getInstrument().getIdentifier() + "]");

                super.addStateTransition(context, transition);
                }
            };

        return (activityIndicator);
        }


    /***********************************************************************************************
     * Create the Command Execute button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createExecuteButton(final CommandProcessorContextInterface context)
        {
        final JButton buttonExecute;

        // The Execute button
        buttonExecute = new JButton(BUTTON_EXECUTE);

        buttonExecute.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonExecute.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonExecute.setMinimumSize(DIM_BUTTON_TOOLBAR_EXECUTE);
        buttonExecute.setMaximumSize(DIM_BUTTON_TOOLBAR_EXECUTE);
        buttonExecute.setPreferredSize(DIM_BUTTON_TOOLBAR_EXECUTE);
        buttonExecute.setFont(context.getFontData().getFont());
        buttonExecute.setForeground(context.getColourData().getColor());

        buttonExecute.setEnabled(false);
        buttonExecute.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_EXECUTE);

        if (!InstrumentState.isDoingSomething(context.getObservatoryInstrument()))
            {
            buttonExecute.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_STOPPED);
            }

        buttonExecute.addActionListener(new ActionListener()
        {
        public synchronized void actionPerformed(final ActionEvent e)
            {
            final List<String> listErrors;

            listErrors = new ArrayList<String>(10);

            // We can only execute if currently in READY
            // It is not necessary to check for 'unavailable'
            if ((context != null)
                //&& (CommandProcessorContext.isPreparedCommandReadyToExecute(context, listErrors))
                && (InstrumentState.READY.equals(context.getObservatoryInstrument().getInstrumentState()))
                && (context.getObservatoryInstrument() != null)
                && (context.getObservatoryInstrument().getDAO() != null))
                {
                // We can execute a Local Command at any time
                // However, if there is a Port, check that it is not busy before trying to Execute a SendToPort Command
                if (isExecutable(context))
                    {
                    final boolean boolSuccess;

                    // Prevent further Command execution for a while
                    context.getExecuteButton().setEnabled(false);
                    context.getRepeatButton().setEnabled(false);

                    // Update the Command to show any changes from the parsers
                    StarscriptHelper.updateStarscript(context);

                    // Attempt to execute the Command using the Instrument's DAO, on another Thread
                    // These parameters must be valid if the prepared Command is valid
                    // This is a single execution of the Command, i.e. not repeating

                    if ((context.getSelectedMacro() != null)
                        && (!context.getSelectedMacro().isEmpty()))
                        {
                        // Execute the Macro
                        boolSuccess = ExecuteMacroHelper.executeMacro(context.getObservatoryInstrument(),
                                                                      context.getInstrument(),
                                                                      context.getSelectedModule().get(0),
                                                                      context.getSelectedMacro().get(0),
                                                                      context.getStarscript(),
                                                                      false,
                                                                      0,
                                                                      EMPTY_STRING,
                                                                      listErrors);
                        }
                    else if ((context.getSelectedCommand() != null)
                             && (!context.getSelectedCommand().isEmpty()))
                        {
                        // Execute the Command
                        boolSuccess = ExecuteCommandHelper.executeCommand(context.getObservatoryInstrument(),
                                                                          context.getInstrument(),
                                                                          context.getSelectedModule().get(0),
                                                                          context.getSelectedCommand().get(0),
                                                                          context.getExecutionParameters(),
                                                                          context.getStarscript(),
                                                                          false,
                                                                          0,
                                                                          EMPTY_STRING,
                                                                          listErrors);
                        }
                    else
                        {
                        boolSuccess = false;
                        }

                    // See if anything bad happened...
                    if (!boolSuccess)
                        {
                        // We couldn't execute it, or it failed
                        JOptionPane.showMessageDialog(null,
                                                      listErrors.toArray(),
                                                      context.getInstrument().getName() + TITLE_DIALOG_COMMAND_EXECUTION,
                                                      JOptionPane.ERROR_MESSAGE);
                        }
                    }
                else
                    {
                    // The Port is still busy doing something else
                    //listErrors.add(CommandProcessorContextInterface.MSG_CANNOT_EXECUTE);
                    listErrors.add(ObservatoryInstrumentInterface.PORT_BUSY);
                    JOptionPane.showMessageDialog(null,
                                                  listErrors.toArray(),
                                                  context.getInstrument().getName() + TITLE_DIALOG_COMMAND_EXECUTION,
                                                  JOptionPane.ERROR_MESSAGE);
                    }
                }
            else
                {
                // Leave the Execute button enabled, so that the User can try again
                // Are there any errors?
                handleErrors(context, listErrors, TITLE_DIALOG_COMMAND_EXECUTION);
                }
            }
        });

        return (buttonExecute);
        }


    /***********************************************************************************************
     * Indicate if the currently selected Macro or Command can be executed,
     * given its SendToPort configuration and the state of the Port.
     *
     * @param context
     *
     * @return boolean
     */

    private static boolean isExecutable(final CommandProcessorContextInterface context)
        {
        final boolean boolExecutableLocal;
        final boolean boolExecutableRemote;
        final boolean boolExecutablePort;
        final boolean boolExecutable;

        // We can execute a Local (i.e. not SendToPort) Command at any time
        boolExecutableLocal = ((context.getSelectedCommand() != null)
                                && (!context.getSelectedCommand().isEmpty())
                                && (!context.getSelectedCommand().get(0).getSendToPort()));

        // Allow SendToPort Commands and all Macros to be executed remotely
        boolExecutableRemote = (((context.getSelectedCommand() != null)
                                    && (!context.getSelectedCommand().isEmpty())
                                    && (context.getSelectedCommand().get(0).getSendToPort()))
                                || ((context.getSelectedMacro() != null)
                                    && (!context.getSelectedMacro().isEmpty())));

        // Allow SendToPort Commands and all Macros if the Port is not busy, or if there is no Port
        boolExecutablePort = ((context.getObservatoryInstrument().getDAO().getPort() == null)
                             || ((context.getObservatoryInstrument().getDAO().getPort() != null)
                                 && (!context.getObservatoryInstrument().getDAO().getPort().isPortBusy())));

        // One or the other might be possible
        boolExecutable = (boolExecutableLocal || (boolExecutableRemote && boolExecutablePort));

        return (boolExecutable);
        }


    /***********************************************************************************************
     * Create the Command Repeat button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createRepeatButton(final CommandProcessorContextInterface context)
        {
        final JButton buttonRepeat;

        // The Repeat button
        buttonRepeat = new JButton(BUTTON_REPEAT);

        buttonRepeat.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRepeat.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonRepeat.setMinimumSize(DIM_BUTTON_TOOLBAR_REPEAT);
        buttonRepeat.setMaximumSize(DIM_BUTTON_TOOLBAR_REPEAT);
        buttonRepeat.setPreferredSize(DIM_BUTTON_TOOLBAR_REPEAT);
        buttonRepeat.setFont(context.getFontData().getFont());
        buttonRepeat.setForeground(context.getColourData().getColor());

        buttonRepeat.setEnabled(false);
        buttonRepeat.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_REPEAT);

        if (!InstrumentState.isDoingSomething(context.getObservatoryInstrument()))
            {
            buttonRepeat.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_STOPPED);
            }

        buttonRepeat.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent e)
                {
                final List<String> listErrors;
                final XmlObject module;
                final CommandType command;
                final int intTimeout;

                listErrors = new ArrayList<String>(10);

                if ((context.getSelectedModule() != null)
                    && (context.getSelectedModule().get(0) != null))
                    {
                    module = context.getSelectedModule().get(0);
                    }
                else
                    {
                    module = null;
                    }

                if ((context.getSelectedCommand() != null)
                    && (context.getSelectedCommand().get(0) != null))
                    {
                    command = context.getSelectedCommand().get(0);
                    }
                else
                    {
                    command = null;
                    }

                // Do the best we can to get a sensible Timeout value to test against
                intTimeout = context.getObservatoryInstrument().getDAO().getTimeoutMillis(module, command);

                // Check to see if the selected Repeat time is less than the current Timeout time
                // If so, there is a risk of failure because commands might 'pile up'...
                if ((context.getObservatoryInstrument() != null)
                    && (context.getObservatoryInstrument().getDAO() != null)
                    && (context.getInstrument() != null)
                    && (context.getRepeatPeriodMillis() < intTimeout))
                    {
                    final String [] messageRepeat =
                        {
                        MSG_REPEAT_TIME_0,
                        MSG_REPEAT_TIME_1,
                        MSG_REPEAT_TIME_2,
                        MSG_REPEAT_TIME_3
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  messageRepeat,
                                                  context.getInstrument().getName() + TITLE_DIALOG_COMMAND_REPEAT,
                                                  JOptionPane.WARNING_MESSAGE);
                    }

                // We can only repeat if currently in READY
                // It is not necessary to check for 'unavailable'
                if ((context != null)
                    //&& (CommandProcessorContext.isPreparedCommandReadyToExecute(context, listErrors))
                    && (InstrumentState.READY.equals(context.getObservatoryInstrument().getInstrumentState()))
                    && (context.getObservatoryInstrument() != null)
                    && (context.getObservatoryInstrument().getDAO() != null))
                    {
                    // Ensure that we can do a repeat
                    context.getObservatoryInstrument().getDAO().getResponseMessageStatusList().clear();
                    context.getObservatoryInstrument().getDAO().setExecutionStatus(ExecutionStatus.FINISHED);

                    // If there is a Port, check that it is not busy before trying to Repeat
                    if ((context.getObservatoryInstrument().getDAO().getPort() == null)
                        || ((context.getObservatoryInstrument().getDAO().getPort() != null)
                            && (!context.getObservatoryInstrument().getDAO().getPort().isPortBusy())))
                        {
                        final boolean boolSuccess;

                        // Lock out all other activity while repeating...
                        context.getExecuteButton().setEnabled(false);
                        context.getRepeatButton().setEnabled(false);

                        // Update the Command to show any changes from the parsers
                        StarscriptHelper.updateStarscript(context);

                        // Attempt to repeatedly execute the Command using the Instrument's DAO, on another Thread
                        // These parameters must be valid if the prepared Command is valid
                        boolSuccess = context.getObservatoryInstrument().getDAO().repeatCommand(context, listErrors);

                        // See if anything bad happened...
                        if (!boolSuccess)
                            {
                            // We couldn't execute it, or it failed
                            JOptionPane.showMessageDialog(null,
                                                          listErrors.toArray(),
                                                          context.getInstrument().getName() + TITLE_DIALOG_COMMAND_REPEAT,
                                                          JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    else
                        {
                        // The Port is still busy doing something else
                        listErrors.add(ObservatoryInstrumentInterface.PORT_BUSY);
                        JOptionPane.showMessageDialog(null,
                                                      listErrors.toArray(),
                                                      context.getInstrument().getName() + TITLE_DIALOG_COMMAND_REPEAT,
                                                      JOptionPane.ERROR_MESSAGE);
                        }
                    }
                else
                    {
                    // Leave the Repeat button enabled, so that the User can try again
                    // Are there any errors?
                    handleErrors(context, listErrors, TITLE_DIALOG_COMMAND_REPEAT);
                    }
                }
            });

        return (buttonRepeat);
        }


    /***********************************************************************************************
     * Create the Command Abort button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createAbortButton(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "CommandProcessorUtilities.createAbortButton() ";
        final JButton buttonAbort;

        // The Abort button
        buttonAbort = new JButton(BUTTON_ABORT);

        buttonAbort.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonAbort.setAlignmentY(Component.CENTER_ALIGNMENT);
        buttonAbort.setMinimumSize(DIM_BUTTON_TOOLBAR_ABORT);
        buttonAbort.setMaximumSize(DIM_BUTTON_TOOLBAR_ABORT);
        buttonAbort.setPreferredSize(DIM_BUTTON_TOOLBAR_ABORT);
        buttonAbort.setFont(context.getFontData().getFont());
        buttonAbort.setForeground(context.getColourData().getColor());

        buttonAbort.setEnabled(false);
        buttonAbort.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_ABORT);

        buttonAbort.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                final List<String> listErrors;

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "ABORT NOW!");

                listErrors = new ArrayList<String>(10);

                // It is not necessary to check for 'unavailable'
                if ((context != null)
                    && (context.getObservatoryInstrument() != null)
                    && (InstrumentState.isDoingSomething(context.getObservatoryInstrument()))
                    && (context.getObservatoryInstrument().getDAO() != null)
                    && (context.getSelectedModule() != null)
                    && (!context.getSelectedModule().isEmpty())
                    && (context.isSelectedMacroOrCommand()))
                    {
                    // If there is a Port, check that it is busy before trying to Abort
                    // 2012-08-13 This should no longer be necessary?
//                    if ((context.getObservatoryInstrument().getDAO().getPort() == null)
//                        || ((context.getObservatoryInstrument().getDAO().getPort() != null)
//                            && (context.getObservatoryInstrument().getDAO().getPort().isPortBusy())))
//                        {
                        // TODO Abort Macro??

                        // If the Port isn't busy, it is not executing a Command!
                        // Attempt to stop the Command using the Instrument's DAO, on another Thread
                        context.getObservatoryInstrument().getDAO().abortCommand(context.getObservatoryInstrument(),
                                                                                 context.getObservatoryInstrument().getInstrument(),
                                                                                 context.getSelectedModule().get(0),
                                                                                 context.getSelectedCommand().get(0),
                                                                                 listErrors);
                        // See if anything bad happened
                        if (!listErrors.isEmpty())
                            {
                            JOptionPane.showMessageDialog(null,
                                                          listErrors.toArray(),
                                                          context.getInstrument().getName() + TITLE_DIALOG_COMMAND_ABORT,
                                                          JOptionPane.ERROR_MESSAGE);
                            }
//                        }
//                    else
//                        {
//                        // The Port is not busy, so there's nothing to Abort
//                        listErrors.add(ObservatoryInstrumentInterface.PORT_NOT_BUSY);
//                        JOptionPane.showMessageDialog(null,
//                                                      listErrors.toArray(),
//                                                      context.getInstrument().getName() + TITLE_DIALOG_COMMAND_ABORT,
//                                                      JOptionPane.ERROR_MESSAGE);
//                        }
                    }
                else
                    {
                    // Is the Instrument STOPPED, or in a faulty state?
                    if (context != null)
                        {
                        if ((InstrumentState.isOff(context.getObservatoryInstrument())))
                            {
                            CommandProcessorUtilities.showUnavailableDialog(context);
                            }
                        else
                            {
                            // Force a reset of the Instrument state, just in case...
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 "READY_ERROR6");
                            // Leave the Abort button enabled, so that the User can try again
                            // Are there any errors?
                            handleErrors(context, listErrors, TITLE_DIALOG_COMMAND_ABORT);
                            }
                        }
                    }
                }
            });

        return (buttonAbort);
        }


    /***********************************************************************************************
     * Create the configuration panel for Command Repeat.
     *
     * @param context
     *
     * @return JPanel
     */

    private static JPanel createRepeatConfig(final CommandProcessorContextInterface context)
        {
        final JPanel panelConfig;
        final JTextField textValue;
        final DocumentListener listenerDoc;

        // Set some default values
        // Use 30sec to try to avoid threads colliding if set too fast...
        context.setRepeatPeriodMillis(ChronosHelper.SECOND_MILLISECONDS * PERIOD_REPEAT_DEFAULT_SECS);
        context.setRepeatCount(1);
        context.setRepeatPeriodMode(true);
        context.setRepeatText(SchemaUnits.SECONDS.toString());

        // Allow free-text entry for the repeat period
        // Warning! Do not use the no-parameter constructor with SpringLayout!
        textValue = new JTextField(5);
        textValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        textValue.setAlignmentY(Component.CENTER_ALIGNMENT);
        textValue.setText(Long.toString(context.getRepeatPeriodMillis() / ChronosHelper.SECOND_MILLISECONDS));

        textValue.setMinimumSize(DIM_TEXT_REPEAT_CONFIG);
        textValue.setPreferredSize(DIM_TEXT_REPEAT_CONFIG);
        textValue.setMaximumSize(DIM_TEXT_REPEAT_CONFIG);
        textValue.setMargin(new Insets(0, 4, 0, 4));
        textValue.setFont(context.getFontData().getFont());
        textValue.setForeground(context.getColourData().getColor().darker());

        // Text Field Listener
        listenerDoc = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                if (InstrumentState.isReady(context.getObservatoryInstrument()))
                    {
                    interpretRepeatConfig(context, textValue);
                    }
                else
                    {
                    CommandProcessorUtilities.showUnavailableDialog(context);
                    }
                }

            public void removeUpdate(final DocumentEvent event)
                {
                if (InstrumentState.isReady(context.getObservatoryInstrument()))
                    {
                    interpretRepeatConfig(context, textValue);
                    }
                else
                    {
                    CommandProcessorUtilities.showUnavailableDialog(context);
                    }
                }

            public void changedUpdate(final DocumentEvent event)
                {
                if (InstrumentState.isReady(context.getObservatoryInstrument()))
                    {
                    interpretRepeatConfig(context, textValue);
                    }
                else
                    {
                    CommandProcessorUtilities.showUnavailableDialog(context);
                    }
                }
            };

        textValue.getDocument().addDocumentListener(listenerDoc);

        panelConfig = new JPanel();
        panelConfig.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelConfig.setAlignmentY(Component.CENTER_ALIGNMENT);
        panelConfig.setMaximumSize(DIM_TEXT_REPEAT_CONFIG);
        panelConfig.setMinimumSize(DIM_TEXT_REPEAT_CONFIG);
        panelConfig.setPreferredSize(DIM_TEXT_REPEAT_CONFIG);
        panelConfig.setLayout(new BorderLayout());
        panelConfig.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());

        // Finally add the text entry box to the host panel
        panelConfig.add(textValue, BorderLayout.CENTER);

        return (panelConfig);
        }


    /***********************************************************************************************
     * Process the User's input for the Repeat configuration.
     * The context is assumed to be not null.
     *
     * @param context
     * @param value
     *
     * @return String
     */

    private static String interpretRepeatConfig(final CommandProcessorContextInterface context,
                                                final JTextField value)
        {
        final String strValue;
        long longRepeatPeriodSec;
        boolean boolSuccess;

        longRepeatPeriodSec = 0;
        boolSuccess = false;

        // Interpret the text value as a period in seconds, defaulting to 10s on error
        if ((value != null)
            && (value.getText() != null)
            && (!EMPTY_STRING.equals(value.getText().trim())))
            {
            try
                {
                // Allow one second to 100,000 seconds (just over a day) --> 100,000,000 msec
                longRepeatPeriodSec = Long.parseLong(value.getText().trim());
                boolSuccess = ((longRepeatPeriodSec >= 1) && (longRepeatPeriodSec <= PERIOD_REPEAT_MAX_SECS));
                }

            catch (NumberFormatException exception)
                {
                boolSuccess = false;
                }
            }

        if (boolSuccess)
            {
            context.setRepeatPeriodMode(true);
            context.setRepeatCount(1);
            context.setRepeatPeriodMillis(longRepeatPeriodSec * ChronosHelper.SECOND_MILLISECONDS);
            context.setRepeatText(SchemaUnits.SECONDS.toString());
            strValue = Long.toString(longRepeatPeriodSec);
            }
        else
            {
            context.setRepeatPeriodMode(true);
            context.setRepeatCount(1);
            context.setRepeatPeriodMillis(10 * ChronosHelper.SECOND_MILLISECONDS);
            context.setRepeatText(SchemaUnits.SECONDS.toString());
            strValue = Long.toString(10);
            }

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "CommandToolbarHelper.interpretRepeatConfig() [period="
                                + context.getRepeatPeriodMillis() + "msec] [count="
                                + context.getRepeatCount() + "]");

        return (strValue);
        }


    /***********************************************************************************************
     * Handle errors in CommandToolbarHelper.
     *
     * @param context
     * @param errors
     * @param title
     */

    private static void handleErrors(final CommandProcessorContextInterface context,
                                     final List<String> errors,
                                     final String title)
        {
        if ((context != null)
            && (context.getInstrument() != null)
            && (errors != null)
            && (!errors.isEmpty()))
            {
            JOptionPane.showMessageDialog(null,
                                          errors.toArray(),
                                          context.getInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else if ((context != null)
                && (context.getObservatoryInstrument() != null)
                && (context.getObservatoryInstrument().getDAO() == null)
                && (context.getInstrument() != null))
            {
            JOptionPane.showMessageDialog(null,
                                          ObservatoryInstrumentInterface.MSG_NO_CONNECTION,
                                          context.getInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else if ((context != null)
                && (context.getObservatoryInstrument() != null)
                && ((InstrumentState.isOccupied(context.getObservatoryInstrument()))))
            {
            JOptionPane.showMessageDialog(null,
                                          ObservatoryInstrumentInterface.TOOLTIP_BUSY,
                                          context.getInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else if ((context != null)
                && (context.getObservatoryInstrument() != null)
                && ((!InstrumentState.isDoingSomething(context.getObservatoryInstrument()))))
            {
            // This should never be possible...
            JOptionPane.showMessageDialog(null,
                                          ObservatoryInstrumentInterface.TOOLTIP_STOPPED,
                                          context.getInstrument().getName() + title,
                                          JOptionPane.ERROR_MESSAGE);
            }
        else
            {
            throw new FrameworkException("CommandToolbarHelper.handleErrors() " + EXCEPTION_PARAMETER_INVALID);
            }
        }
    }
