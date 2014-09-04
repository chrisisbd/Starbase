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

import info.clearthought.layout.TableLayout;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ViewingMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.XmlUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandProcessorUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommanderToolbarHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.events.CommandLifecycleEventInterface;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.instruments.*;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * MacroManagerUtilities.
 */

public final class MacroManagerUtilities implements FrameworkConstants,
                                                    FrameworkStrings,
                                                    FrameworkMetadata,
                                                    FrameworkSingletons,
                                                    ResourceKeys
    {
    public static final String TOOLTIP_MACROS = "The Macros defined for this Instrument";

    public static final String BUTTON_MACROS = "Macros";
    private static final String BUTTON_RECORD = "Record Macro";
    private static final String BUTTON_EDIT = "Toggle Edit Macro";
    private static final String BUTTON_DELETE = "Delete Macro";
    private static final String BUTTON_SHOW = "Toggle View Macro";
    public static final String BUTTON_HIDE = "Hide Macro";
    private static final String BUTTON_LOAD = "Load Instrument Macros";
    private static final String BUTTON_SAVE = "Save Instrument Macros";

    public static final String PROMPT_MODULE = "Please select a Module";
    private static final String TITLE_DIALOG_MACRO_RECORDER = " Macro Recorder";
    private static final String TITLE_DIALOG_MACRO_EDITOR = " Macro Editor";
    public static final String TITLE_DIALOG_MACRO_DELETION = " Macro Deletion";
    private static final String MSG_DELETE_MACRO = "Are you sure that you wish to delete the selected Instrument Macro?";
    private static final String MSG_SAVE_MACROS = "Are you sure that you wish to save the Instrument Macros?";
    public static final String ICON_TAB_MACROMANAGER = "tab-macromanager.png";
    private static final String FILENAME_MACROS_SUFFIX = "-macros.xml";
    private static final String INSTRUMENT_MACROS_MODULE = "InstrumentMacros";
    private static final String INSTRUMENT_MACROS_NAME = "Instrument Macros";
    private static final String INSTRUMENT_MACROS_DESCRIPTION = "Macros specific to this Instrument";
    private static final String INSTRUMENT_MACROS_RESOURCEKEY = "InstrumentMacros";
    private static final String INSTRUMENT_MACROS_CCB = "91";

    public static final int WIDTH_CONTROL_BUTTON = CommandProcessorUtilities.WIDTH_BUTTON;
    public static final Color COLOR_MACRO_BUTTONS = new Color(254, 213, 213);

    private static final Dimension DIM_BUTTON_RECORD = new Dimension(WIDTH_CONTROL_BUTTON,
                                                                    CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_EDIT = new Dimension(WIDTH_CONTROL_BUTTON,
                                                                  CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_DELETE = new Dimension(WIDTH_CONTROL_BUTTON,
                                                                    CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_SHOW = new Dimension(WIDTH_CONTROL_BUTTON,
                                                                  CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_LOAD = new Dimension(WIDTH_CONTROL_BUTTON,
                                                                  CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_BUTTON_SAVE = new Dimension(WIDTH_CONTROL_BUTTON,
                                                                  CommandProcessorUtilities.HEIGHT_BUTTON);


    /***********************************************************************************************
     * Build the Macro Starscript for the specified context, not formatted.
     *
     * @param instrument
     * @param module
     * @param macro
     *
     * @return String
     */

    public static String buildMacroStarscript(final Instrument instrument,
                                              final XmlObject module,
                                              final MacroType macro)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (instrument != null)
            {
            // Controllers can't have Macros in this version
            if (module instanceof PluginType)
                {
                buffer.append(instrument.getIdentifier());
                buffer.append(FrameworkStrings.DOT);
                buffer.append(((PluginType)module).getIdentifier());

                if (macro != null)
                    {
                    buffer.append(FrameworkStrings.DOT);
                    buffer.append(macro.getIdentifier());
                    buffer.append(buildMacroParameterList(macro.getParameterList()));
                    }
                }
            else
                {
                // There is no Module selected
                buffer.append(PROMPT_MODULE);
                }
            }
        else
            {
            buffer.append(ObservatoryInstrumentInterface.INSTRUMENT_NOT_FOUND);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Build the formatted Macro Parameter list.
     * Show the Parameter Names, not their Values.
     *
     * @param parameters
     *
     * @return String
     */

    private static String buildMacroParameterList(final List<ParameterType> parameters)
        {
        final StringBuffer buffer;
        final int intCount;

        buffer = new StringBuffer();

        if ((parameters == null)
            || (parameters.isEmpty()))
            {
            intCount = 0;
            }
        else
            {
            intCount = parameters.size();
            }

        if (intCount <= 0)
            {
            buffer.append(LEFT_PARENTHESIS);
            buffer.append(RIGHT_PARENTHESIS);
            }
        else
            {
            buffer.append(FrameworkStrings.LEFT_PARENTHESIS);

            for (int i = 0; i < intCount; i++)
                {
                if ((parameters != null)
                    && (parameters.get(i) != null))
                    {
                    if (parameters.get(i).getName() != null)
                        {
                        buffer.append(parameters.get(i).getName());
                        }
                    else
                        {
                        buffer.append(QUERY);
                        }

                    if ((intCount > 1)
                        && (i != intCount-1))
                        {
                        buffer.append(COMMA);
                        buffer.append(SPACE);
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                    }
                }

            buffer.append(RIGHT_PARENTHESIS);
            }

        return (buffer.toString());
        }


        /***********************************************************************************************
         * Create the Macro Manager Panel.
         * This consists of the buttons to record, edit, delete and save Macros.
         *
         * @param context The CommandProcessorContext
         *
         * @return Component
         */

        public static Component createMacroManagerPanel(final CommandProcessorContextInterface context)
            {
            final JScrollPane scrollMacro;
            final TableLayout layout;
            final JPanel panelMacro;

            // https://tablelayout.dev.java.net/articles/TableLayoutTutorialPart1/TableLayoutTutorialPart1.html
            // https://tablelayout.dev.java.net/servlets/ProjectDocumentList?folderID=3487&expandFolder=3487&folderID=3487

            final double[][] size =
                {
                    { // Columns 0...4
                    10,
                    TableLayout.PREFERRED,
                    30,                    // Buuton Spacer
                    TableLayout.PREFERRED,
                    TableLayout.FILL
                    },

                    { // Rows 0...5
                    1,
                    TableLayout.PREFERRED,
                    TableLayout.PREFERRED,
                    TableLayout.PREFERRED,
                    TableLayout.PREFERRED,
                    TableLayout.FILL
                    }
                };

            // TableLayout constraints for Buttons
            // These specify where the buttons are to be placed in the layout
            // Indicates TableLayoutConstraints's position and justification as a string in the form
            // "column, row, horizontal justification, vertical justification"
            // Valid values: LEFT, RIGHT, CENTER, TOP, BOTTOM

            final String[] constraints =
                {
                 "1, 1, LEFT, CENTER", // Record
                 "1, 2, LEFT, CENTER", // Edit
                 "1, 3, LEFT, CENTER", // Show
                 "1, 4, LEFT, CENTER", // Delete

                 "3, 1, LEFT, CENTER", // Load
                 "3, 2, LEFT, CENTER", // Save
                };

            // Create and add the Execution button panel
            panelMacro = new JPanel();

            layout = new TableLayout(size);
            layout.setVGap(CommandProcessorUtilities.HEIGHT_BUTTON_SEPARATOR);

            panelMacro.setLayout(layout);
            panelMacro.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());

            // Add the Macro Management buttons
            // Record, Edit, Show, Delete, Load, and Save

            context.setRecordMacroButton(createRecordMacroButton(context));
            panelMacro.add(context.getRecordMacroButton(), constraints[0]);

            context.setEditMacroButton(createEditMacroButton(context));
            panelMacro.add(context.getEditMacroButton(), constraints[1]);

            context.setShowMacroButton(createShowMacroButton(context));
            panelMacro.add(context.getShowMacroButton(), constraints[2]);

            context.setDeleteMacroButton(createDeleteMacroButton(context));
            panelMacro.add(context.getDeleteMacroButton(), constraints[3]);

            context.setLoadMacroButton(createLoadInstrumentMacrosButton(context));
            panelMacro.add(context.getLoadMacroButton(), constraints[4]);

            context.setSaveMacroButton(createSaveMacroButton(context));
            panelMacro.add(context.getSaveMacroButton(), constraints[5]);

            // Tab occupants MUST NOT have any sizes set!
            scrollMacro = new JScrollPane(panelMacro,
                                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollMacro.setBorder(BorderFactory.createEmptyBorder());

            return (scrollMacro);
            }


    /***********************************************************************************************
     * Create the RecordMacro button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createRecordMacroButton(final CommandProcessorContextInterface context)
        {
        final JButton buttonRecordMacro;

        // The RecordMacro button
        buttonRecordMacro = new JButton(BUTTON_RECORD);

        buttonRecordMacro.setAlignmentX(CommanderToolbarHelper.EXEC_PANEL_ALIGNMENT);
        buttonRecordMacro.setMinimumSize(DIM_BUTTON_RECORD);
        buttonRecordMacro.setMaximumSize(DIM_BUTTON_RECORD);
        buttonRecordMacro.setPreferredSize(DIM_BUTTON_RECORD);
        buttonRecordMacro.setFont(context.getFontData().getFont());
        buttonRecordMacro.setBackground(COLOR_MACRO_BUTTONS);
        buttonRecordMacro.setForeground(context.getColourData().getColor());

        buttonRecordMacro.setEnabled(false);
        buttonRecordMacro.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_RECORD);

        buttonRecordMacro.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                final String [] messageRecord =
                    {
                    "Record Macro Not Implemented"
                    };

                JOptionPane.showMessageDialog(null,
                                              messageRecord,
                                              context.getInstrument().getName() + TITLE_DIALOG_MACRO_RECORDER,
                                              JOptionPane.ERROR_MESSAGE);
                }
            });

        return (buttonRecordMacro);
        }


    /***********************************************************************************************
     * Create the EditMacro button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createEditMacroButton(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "MacroManagerUtilities.createEditMacroButton().actionPerformed() ";
        final JButton buttonEditMacro;

        // The EditMacro button
        buttonEditMacro = new JButton(BUTTON_EDIT);

        buttonEditMacro.setAlignmentX(CommanderToolbarHelper.EXEC_PANEL_ALIGNMENT);
        buttonEditMacro.setMinimumSize(DIM_BUTTON_EDIT);
        buttonEditMacro.setMaximumSize(DIM_BUTTON_EDIT);
        buttonEditMacro.setPreferredSize(DIM_BUTTON_EDIT);
        buttonEditMacro.setFont(context.getFontData().getFont());
        buttonEditMacro.setBackground(COLOR_MACRO_BUTTONS);
        buttonEditMacro.setForeground(context.getColourData().getColor());

        buttonEditMacro.setEnabled(false);
        buttonEditMacro.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_EDIT);

        buttonEditMacro.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1)
                    && (context.getMacroEditor() != null)
                    && (context.getObservatoryInstrument() != null))
                    {
                    switch (context.getViewingMode())
                        {
                        case COMMAND_LOG:
                            {
                            context.getMacroEditor().setMacroContext(context);
                            showMacroEditor(context);

                            // Move into the EDIT_MACRO state
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.EDIT_MACRO,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            break;
                            }

                        case MACRO_VIEWER:
                            {
                            LOGGER.error(SOURCE + "Invalid Viewing Mode, attempting to return to READY");

                            // Attempt to return to READY
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            break;
                            }

                        case MACRO_EDITOR:
                            {
                            // Already editing Macros, so restore the CommandLog, and return to READY
                            restoreCommandLifecycleLog(context);

                            // We can only be in the EDIT_MACRO state already
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            break;
                            }

                        default:
                            {
                            LOGGER.error(SOURCE + "Invalid Viewing Mode, attempting to return to READY");

                            // Attempt to return to READY
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Current Command selection is not a Macro, or unable to edit");
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        return (buttonEditMacro);
        }


    /***********************************************************************************************
     * Create the DeleteMacro button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createDeleteMacroButton(final CommandProcessorContextInterface context)
        {
        final JButton buttonDeleteMacro;

        // The DeleteMacro button
        buttonDeleteMacro = new JButton(BUTTON_DELETE);

        buttonDeleteMacro.setAlignmentX(CommanderToolbarHelper.EXEC_PANEL_ALIGNMENT);
        buttonDeleteMacro.setMinimumSize(DIM_BUTTON_DELETE);
        buttonDeleteMacro.setMaximumSize(DIM_BUTTON_DELETE);
        buttonDeleteMacro.setPreferredSize(DIM_BUTTON_DELETE);
        buttonDeleteMacro.setFont(context.getFontData().getFont());
        buttonDeleteMacro.setBackground(COLOR_MACRO_BUTTONS);
        buttonDeleteMacro.setForeground(context.getColourData().getColor());

        buttonDeleteMacro.setEnabled(false);
        buttonDeleteMacro.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_DELETE);

        buttonDeleteMacro.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                // Move to the DELETE_MACRO state
                context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                     context.getObservatoryInstrument(),
                                                                                     context.getObservatoryInstrument().getInstrumentState(),
                                                                                     InstrumentState.DELETE_MACRO,
                                                                                     0,
                                                                                     UNEXPECTED);

                if ((context.getObservatoryInstrument() != null)
                    && (context.getObservatoryInstrument().getInstrumentMacros() != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().get(0) != null))
                    {
                    final int intChoice;
                    final String [] strMessage =
                        {
                        MSG_DELETE_MACRO
                        };

                    intChoice = JOptionPane.showOptionDialog(null,
                                                             strMessage,
                                                             "Delete Macro " + context.getSelectedMacro().get(0).getIdentifier(),
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             null,
                                                             null);
                    if (intChoice == JOptionPane.YES_OPTION)
                        {
                        System.out.println("delete! " + context.getSelectedMacro().get(0).getIdentifier());









                        }
                    }

                // Back to the READY State
                context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                     context.getObservatoryInstrument(),
                                                                                     context.getObservatoryInstrument().getInstrumentState(),
                                                                                     InstrumentState.READY,
                                                                                     0,
                                                                                     UNEXPECTED);
                // Finally, update the Instrument XML tab and redraw the Commands tab buttons
                // regardless of what happened above...
                rebuildMacroCommands(context, this);
                }
            });

        return (buttonDeleteMacro);
        }


    /***********************************************************************************************
     * Create the ShowMacro button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createShowMacroButton(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "MacroManagerUtilities.createShowMacroButton().actionPerformed() ";
        final JButton buttonShowMacro;

        // The ShowMacro button
        buttonShowMacro = new JButton(BUTTON_SHOW);

        buttonShowMacro.setAlignmentX(CommanderToolbarHelper.EXEC_PANEL_ALIGNMENT);
        buttonShowMacro.setMinimumSize(DIM_BUTTON_SHOW);
        buttonShowMacro.setMaximumSize(DIM_BUTTON_SHOW);
        buttonShowMacro.setPreferredSize(DIM_BUTTON_SHOW);
        buttonShowMacro.setFont(context.getFontData().getFont());
        buttonShowMacro.setBackground(COLOR_MACRO_BUTTONS);
        buttonShowMacro.setForeground(context.getColourData().getColor());

        buttonShowMacro.setEnabled(false);
        buttonShowMacro.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_SHOW);

        buttonShowMacro.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1)
                    && (context.getMacroViewer() != null)
                    && (context.getObservatoryInstrument() != null))
                    {
                    switch (context.getViewingMode())
                        {
                        case COMMAND_LOG:
                            {
                            context.getMacroViewer().setMacroContext(context);
                            showMacroViewer(context);

                            // Move into the SHOW_MACRO state
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.SHOW_MACRO,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            break;
                            }

                        case MACRO_VIEWER:
                            {
                            // Already viewing Macros, so restore the CommandLog, and return to READY
                            restoreCommandLifecycleLog(context);

                            // We can only be in the SHOW_MACRO state already
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            break;
                            }

                        case MACRO_EDITOR:
                            {
                            LOGGER.error(SOURCE + "Invalid Viewing Mode, attempting to return to READY");

                            // Attempt to return to READY
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            break;
                            }

                        default:
                            {
                            LOGGER.error(SOURCE + "Invalid Viewing Mode, attempting to return to READY");

                            // Attempt to return to READY
                            context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                                 context.getObservatoryInstrument(),
                                                                                                 context.getObservatoryInstrument().getInstrumentState(),
                                                                                                 InstrumentState.READY,
                                                                                                 0,
                                                                                                 UNEXPECTED);
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Current Command selection is not a Macro, or unable to display");
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        return (buttonShowMacro);
        }


    /***********************************************************************************************
     * Show the MacroViewer.
     *
     * @param context
     */

    private static void showMacroViewer(final CommandProcessorContextInterface context)
        {
        if ((context != null)
            && (context.getViewerContainer() != null)
            && (context.getMacroViewer() != null))
            {
            // Stop the CommandLifecycleLog regardless
            if (context.getCommandLifecycleLog() != null)
                {
                context.getCommandLifecycleLog().stopUI();
                }

            context.getViewerContainer().removeAll();
            context.getViewerContainer().add((Component) context.getMacroViewer(),
                                             BorderLayout.CENTER);
            context.getViewerContainer().revalidate();
            context.getMacroViewer().initialiseUI();

            context.setViewingMode(ViewingMode.MACRO_VIEWER);

            // Update the Commands Tab, if we have one...
            if ((context.getObservatoryInstrument() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab() != null))
                {
                context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().runUI();
                }

            // Tell the MacroViewer that something has changed
            if (context.getMacroViewer() != null)
                {
                context.getMacroViewer().macroChanged();
                }
            }
        }


    /***********************************************************************************************
     * Show the MacroEditor.
     *
     * @param context
     */

    private static void showMacroEditor(final CommandProcessorContextInterface context)
        {
        if ((context != null)
            && (context.getViewerContainer() != null)
            && (context.getMacroEditor() != null))
            {
            // Stop the CommandLifecycleLog regardless
            if (context.getCommandLifecycleLog() != null)
                {
                context.getCommandLifecycleLog().stopUI();
                }

            context.getViewerContainer().removeAll();
            context.getViewerContainer().add((Component) context.getMacroEditor(),
                                             BorderLayout.CENTER);
            context.getViewerContainer().revalidate();
            context.getMacroEditor().initialiseUI();

            context.setViewingMode(ViewingMode.MACRO_EDITOR);

            // Update the Commands Tab, if we have one...
            if ((context.getObservatoryInstrument() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab() != null))
                {
                context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().runUI();
                }

            // Tell the MacroEditor that something has changed
            if (context.getMacroEditor() != null)
                {
                context.getMacroEditor().macroChanged();
                }
            }
        }


    /***********************************************************************************************
     * Restore the CommandLifecycleLog regardless of what happened, ready for the next ShowMacro etc.
     *
     * @param context
     */

    public static void restoreCommandLifecycleLog(final CommandProcessorContextInterface context)
        {
        if ((context != null)
            && (context.getViewerContainer() != null)
            && (context.getCommandLifecycleLog() != null))
            {
            // Stop the MacroViewer regardless...
            if (context.getMacroViewer() != null)
                {
                context.getMacroViewer().stopUI();
                }

            // ...and the MacroEditor
            if (context.getMacroEditor() != null)
                {
                context.getMacroEditor().stopUI();
                }

            context.getViewerContainer().removeAll();
            context.getViewerContainer().add((Component) context.getCommandLifecycleLog(),
                                             BorderLayout.CENTER);
            context.getViewerContainer().revalidate();
            context.getCommandLifecycleLog().initialiseUI();

            context.setViewingMode(ViewingMode.COMMAND_LOG);

            // Update the Commands Tab, if we have one...
            if ((context.getObservatoryInstrument() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab() != null))
                {
                context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().runUI();
                }
            }
        }


    /***********************************************************************************************
     * Create the LoadInstrumentMacros button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createLoadInstrumentMacrosButton(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "CommandProcessorUtilities.createLoadInstrumentMacrosButton() ";
        final JButton buttonLoadMacros;

        // The LoadInstrumentMacros button
        buttonLoadMacros = new JButton(BUTTON_LOAD);

        buttonLoadMacros.setAlignmentX(CommanderToolbarHelper.EXEC_PANEL_ALIGNMENT);
        buttonLoadMacros.setMinimumSize(DIM_BUTTON_LOAD);
        buttonLoadMacros.setMaximumSize(DIM_BUTTON_LOAD);
        buttonLoadMacros.setPreferredSize(DIM_BUTTON_LOAD);
        buttonLoadMacros.setFont(context.getFontData().getFont());
        buttonLoadMacros.setBackground(COLOR_MACRO_BUTTONS);
        buttonLoadMacros.setForeground(context.getColourData().getColor());

        buttonLoadMacros.setEnabled(false);
        buttonLoadMacros.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_LOAD);

        buttonLoadMacros.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                MacrosDocument docLoadedInstrumentMacros;

                docLoadedInstrumentMacros = null;

                // Move to the LOAD_MACROS state
                context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                     context.getObservatoryInstrument(),
                                                                                     context.getObservatoryInstrument().getInstrumentState(),
                                                                                     InstrumentState.LOAD_MACROS,
                                                                                     0,
                                                                                     UNEXPECTED);
                try
                    {
                    final String strPathnameMacros;
                    final File fileMacros;
                    final String strIdentifier;
                    final String strFilename;

                    // Macros have the filename prefixed by the Instrument Identifier
                    // The filename is <Instrument>-macros.xml
                    strIdentifier = context.getInstrument().getIdentifier();
                    strFilename = strIdentifier + FILENAME_MACROS_SUFFIX;

                    strPathnameMacros = InstallationFolder.getTerminatedUserDir()
                                               + ObservatoryInstrumentDAOInterface.PATHNAME_PLUGINS_OBSERVATORY
                                               + DataStore.CONFIG.getLoadFolder()
                                               + System.getProperty("file.separator")
                                               + strFilename;
                    fileMacros = new File(strPathnameMacros);

                    docLoadedInstrumentMacros = MacrosDocument.Factory.parse(fileMacros);

                    if (XmlBeansUtilities.isValidXml(docLoadedInstrumentMacros))
                        {
                        if (docLoadedInstrumentMacros.getMacros() == null)
                            {
                            docLoadedInstrumentMacros = null;
                            }
                        }
                    else
                        {
                        throw new XmlException(EXCEPTION_XML_VALIDATION);
                        }
                    }

                catch (IllegalArgumentException exception)
                    {
                    LOGGER.error(SOURCE + "Failed to read Instrument Macros" + SPACE + exception.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    docLoadedInstrumentMacros = null;
                    }

                catch (IOException exception)
                    {
                    LOGGER.error(SOURCE + "Failed to read Instrument Macros" + SPACE + exception.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    docLoadedInstrumentMacros = null;
                    }

                catch (XmlException exception)
                    {
                    LOGGER.error(SOURCE + "Failed to read Instrument Macros" + SPACE + exception.getMessage());
                    Toolkit.getDefaultToolkit().beep();
                    docLoadedInstrumentMacros = null;
                    }

                //----------------------------------------------------------------------------------
                // Now add the loaded Macros to the Instrument Macro Module,
                // but only if they have different Identifiers from those already loaded

                if ((docLoadedInstrumentMacros != null)
                    && (docLoadedInstrumentMacros.getMacros() != null)
                    && (docLoadedInstrumentMacros.getMacros().getMacroList() != null)
                    && (context.getObservatoryInstrument() != null))
                    {
                    if ((context.getInstrument() != null)
                        && (context.getInstrument().getController() != null))
                        {
                        final List<PluginType> listPlugins;
                        PluginType pluginInstrumentMacros;
                        final List<MacroType> listLoadedInstrumentMacros;
                        final MacrosDocument docInstrumentMacros;
                        final MacrosDocument.Macros macros;

                        listPlugins = context.getInstrument().getController().getPluginList();
                        pluginInstrumentMacros = null;

                        if (listPlugins != null)
                            {
                            boolean boolInsertLater;

                            pluginInstrumentMacros = null;
                            boolInsertLater = false;

                            for (int i = 0;
                                 ((i < listPlugins.size()) && (pluginInstrumentMacros == null));
                                 i++)
                                {
                                final PluginType plugin;

                                plugin = listPlugins.get(i);

                                // Do we already have an Instrument Macros Plugin?
                                if (INSTRUMENT_MACROS_MODULE.equals(plugin.getIdentifier()))
                                    {
                                    pluginInstrumentMacros = plugin;
                                    boolInsertLater = false;
                                    }
                                else
                                    {
                                    // No existing Instrument Macros Plugin, so make one and add it
                                    pluginInstrumentMacros = configurePluginAsInstrumentMacros(PluginType.Factory.newInstance());

                                    // We musn't insert while iterating over the List!
                                    boolInsertLater = true;
                                    }
                                }

                            // It is now safe to modify the List
                            if ((boolInsertLater)
                                && (pluginInstrumentMacros != null))
                                {
                                listPlugins.add(0, pluginInstrumentMacros);

                                // Read back just to be sure...
                                pluginInstrumentMacros = context.getInstrument().getController().getPluginList().get(0);
                                }
                            }
                        else
                            {
                            // Add a new List of Plugins to the Controller by creating a Plugin
                            pluginInstrumentMacros = context.getInstrument().getController().addNewPlugin();
                            configurePluginAsInstrumentMacros(pluginInstrumentMacros);
                            }

                        //--------------------------------------------------------------------------
                        // Now add the Macros we've just imported to the InstrumentMacros Plugin,
                        // but only if they have different Identifiers from those already loaded

                        listLoadedInstrumentMacros = docLoadedInstrumentMacros.getMacros().getMacroList();

                        for (int i = 0;
                             i < listLoadedInstrumentMacros.size();
                             i++)
                            {
                            final MacroType macro;

                            macro = listLoadedInstrumentMacros.get(i);

                            // Can we find this Macro already?
                            if (MacroHelper.getMacroByIdentifier(pluginInstrumentMacros.getMacroList(), macro.getIdentifier()) == null)
                                {
                                pluginInstrumentMacros.getMacroList().add(macro);
                                }
                            }

//                        System.out.println("Loaded PLUGIN =" + pluginInstrumentMacros.xmlText());
//                        System.out.println("INSTRUMENT CONTROLLER =" + context.getInstrument().getController().xmlText());

                        // Now save the entire updated InstrumentMacros Plugin back to the Context
                        // This is the set that will be saved and reloaded
                        // Doing it this way removes the need for a clear() on reload
                        // which produced an XmlDisconnectedException

                        docInstrumentMacros = MacrosDocument.Factory.newInstance();
                        macros = docInstrumentMacros.addNewMacros();

                        if (macros.getMacroList() != null)
                            {
                            macros.getMacroList().addAll(pluginInstrumentMacros.getMacroList());

                            context.getObservatoryInstrument().setInstrumentMacros(docInstrumentMacros);
                            }

                        // Move back to the READY state
                        context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                             context.getObservatoryInstrument(),
                                                                                             context.getObservatoryInstrument().getInstrumentState(),
                                                                                             InstrumentState.READY,
                                                                                             0,
                                                                                             UNEXPECTED);

                        // Finally, update the Instrument XML tab and redraw the Commands tab buttons
                        // regardless of what happened above...
                        rebuildMacroCommands(context, this);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Instrument has no Controller, so unable to add Instrument Macros");
                        Toolkit.getDefaultToolkit().beep();

                        // Move back to the READY state
                        context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                             context.getObservatoryInstrument(),
                                                                                             context.getObservatoryInstrument().getInstrumentState(),
                                                                                             InstrumentState.READY,
                                                                                             0,
                                                                                             UNEXPECTED);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "There are no Instrument Macros to add");
                    Toolkit.getDefaultToolkit().beep();

                    // Move back to the READY state
                    context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                         context.getObservatoryInstrument(),
                                                                                         context.getObservatoryInstrument().getInstrumentState(),
                                                                                         InstrumentState.READY,
                                                                                         0,
                                                                                         UNEXPECTED);
                    }
                }
            });

        return (buttonLoadMacros);
        }


    /*******************************************************************************************
     * Rebuild the Macro Commands, reload the Logs etc.
     */

    private static void rebuildMacroCommands(final CommandProcessorContextInterface context,
                                             final Object eventsource)
        {
        if ((context.getObservatoryInstrument() != null)
            && (context.getObservatoryInstrument().getInstrumentPanel() != null))
            {
            // Update the Commands Tab, if we have one...
            if (context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab() != null)
                {
                final List<CommandLifecycleEventInterface> listEvents;

                // Save the existing CommandLog if possible
                listEvents = new ArrayList<CommandLifecycleEventInterface>();

                if ((context.getCommandLifecycleLog() != null)
                    && (context.getCommandLifecycleLog().getCommandLifecycleEntries() != null)
                    && (!context.getCommandLifecycleLog().getCommandLifecycleEntries().isEmpty()))
                    {
                    listEvents.addAll(context.getCommandLifecycleLog().getCommandLifecycleEntries());
                    }

                // Remove the previous log data
                context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().stopUI();

                // Remove any selection and force the user to re-select
                context.getSelectedModule().clear();
                context.getSelectedMacro().clear();
                context.getSelectedCommand().clear();
                context.getExecutionParameters().clear();
                context.getCommandParameters().clear();
                context.getObservatory().setRecordMacroMode(false);
                context.setViewingMode(ViewingMode.COMMAND_LOG);

                // Rebuild the CommandBuilder
                context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().initialiseUI();

                // Reset all buttons etc. for the READY state
                context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(eventsource,
                                                                                     context.getObservatoryInstrument(),
                                                                                     context.getObservatoryInstrument().getInstrumentState(),
                                                                                     InstrumentState.READY,
                                                                                     0,
                                                                                     UNEXPECTED);
                // Replace the CommandLog if possible
                if ((listEvents != null)
                    && (!listEvents.isEmpty()))
                    {
                    for (int i = 0;
                         i < listEvents.size();
                         i++)
                        {
                        final CommandLifecycleEventInterface cmdEvent;

                        cmdEvent = listEvents.get(i);
                        context.getCommandLifecycleLog().getCommandLifecycleEntries().add(cmdEvent);
                        }
                    }
                }

            // Show the User
            context.getObservatoryInstrument().getInstrumentPanel().getCommandsTab().runUI();

            // Update the XML Tab, if we have one...
            if ((context.getObservatoryInstrument().getInstrumentPanel().getXMLTab() != null)
                && (context.getObservatoryInstrument().getInstrumentPanel().getXMLTab() instanceof XmlUIComponentInterface))
                {
                ((XmlUIComponentInterface)context.getObservatoryInstrument().getInstrumentPanel().getXMLTab()).updateXml();
                }
            }
        }


    /***********************************************************************************************
     * Configure the specified Plugin to be an Instrument Macros Plugin.
     *
     * @param plugin
     */

    private static PluginType configurePluginAsInstrumentMacros(final PluginType plugin)
        {
        if (plugin != null)
            {
            plugin.setIdentifier(INSTRUMENT_MACROS_MODULE);
            plugin.setName(INSTRUMENT_MACROS_NAME);
            plugin.setDescription(INSTRUMENT_MACROS_DESCRIPTION);
            plugin.setResourceKey(INSTRUMENT_MACROS_RESOURCEKEY);
            plugin.setCommandCodeBase(INSTRUMENT_MACROS_CCB);

            // This seems to be the only way to get a non-NULL List
            plugin.addNewPluginMetadata();
            plugin.getPluginMetadataList().clear();

            plugin.addNewMacro();
            plugin.getMacroList().clear();
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the SaveMacro button.
     *
     * @param context
     *
     * @return JButton
     */

    private static JButton createSaveMacroButton(final CommandProcessorContextInterface context)
        {
        final String SOURCE = "CommandProcessorUtilities.createSaveMacroButton() ";
        final JButton buttonSaveMacro;

        // The SaveMacro button
        buttonSaveMacro = new JButton(BUTTON_SAVE);

        buttonSaveMacro.setAlignmentX(CommanderToolbarHelper.EXEC_PANEL_ALIGNMENT);
        buttonSaveMacro.setMinimumSize(DIM_BUTTON_SAVE);
        buttonSaveMacro.setMaximumSize(DIM_BUTTON_SAVE);
        buttonSaveMacro.setPreferredSize(DIM_BUTTON_SAVE);
        buttonSaveMacro.setFont(context.getFontData().getFont());
        buttonSaveMacro.setBackground(COLOR_MACRO_BUTTONS);
        buttonSaveMacro.setForeground(context.getColourData().getColor());

        buttonSaveMacro.setEnabled(false);
        buttonSaveMacro.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_COMMAND_SAVE);

        buttonSaveMacro.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                // Move to the SAVE_MACROS state
                context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                     context.getObservatoryInstrument(),
                                                                                     context.getObservatoryInstrument().getInstrumentState(),
                                                                                     InstrumentState.SAVE_MACROS,
                                                                                     0,
                                                                                     UNEXPECTED);
                if ((context.getObservatoryInstrument() != null)
                    && (context.getObservatoryInstrument().getInstrumentMacros() != null))
                    {
                    final int intChoice;
                    final String [] strMessage =
                        {
                        MSG_SAVE_MACROS
                        };

                    intChoice = JOptionPane.showOptionDialog(null,
                                                             strMessage,
                                                             "Save All " + context.getInstrument().getIdentifier() + " Instrument Macros",
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             null,
                                                             null);
                    if (intChoice == JOptionPane.YES_OPTION)
                        {
                        try
                            {
                            final String strPathnameMacros;
                            final File fileMacros;
                            final String strIdentifier;
                            final String strFilename;
                            final OutputStream outputStream;

                            // Macros have the filename prefixed by the Instrument Identifier
                            // The filename is <Instrument>-macros.xml
                            strIdentifier = context.getInstrument().getIdentifier();
                            strFilename = strIdentifier + FILENAME_MACROS_SUFFIX;

                            strPathnameMacros = InstallationFolder.getTerminatedUserDir()
                                                       + ObservatoryInstrumentDAOInterface.PATHNAME_PLUGINS_OBSERVATORY
                                                       + DataStore.CONFIG.getLoadFolder()
                                                       + System.getProperty("file.separator")
                                                       + strFilename;
                            fileMacros = new File(strPathnameMacros);

                            FileUtilities.overwriteFile(fileMacros);
                            outputStream = new FileOutputStream(fileMacros);

                            // Write the whole document (even if empty) to the output stream
                            context.getObservatoryInstrument().getInstrumentMacros().save(outputStream,
                                                                                          ObservatoryUIHelper.getXmlOptions(false));
                            outputStream.flush();
                            outputStream.close();
                            }

                        catch (FileNotFoundException exception)
                            {
                            LOGGER.error(SOURCE + "Unable to save Instrument Macros");
                            Toolkit.getDefaultToolkit().beep();
                            }

                        catch (IOException exception)
                            {
                            LOGGER.error(SOURCE + "Unable to save Instrument Macros");
                            Toolkit.getDefaultToolkit().beep();
                            }
                        }
                    }
                else
                    {
                    // There are no Macros to save
                    LOGGER.error(SOURCE + "There are no Macros to save");
                    Toolkit.getDefaultToolkit().beep();
                    }

                // Move back to the READY state
                context.getObservatoryInstrument().notifyInstrumentStateChangedEvent(this,
                                                                                     context.getObservatoryInstrument(),
                                                                                     context.getObservatoryInstrument().getInstrumentState(),
                                                                                     InstrumentState.READY,
                                                                                     0,
                                                                                     UNEXPECTED);
                }
            });

        return (buttonSaveMacro);
        }


    /***********************************************************************************************
     * Create the MacroViewer Panel.
     *
     * @param context
     */

    public static void createMacroViewerPanel(final CommandProcessorContextInterface context)
        {
        final MacroUIComponentInterface viewer;

        // Remove any previous MacroViewer
        if ((context != null)
            && (context.getMacroViewer() != null))
            {
            context.getMacroViewer().stopUI();
            context.getMacroViewer().disposeUI();
            }

        // Create a new MacroViewer and initialise it
        // Tab occupants MUST NOT have any sizes set!
        // This is a ReportTable
        viewer = new MacroViewerUIComponent(context.getHostTask(),
                                            context.getObservatoryInstrument(),
                                            REGISTRY.getFrameworkResourceKey());
        context.setMacroViewer(viewer);
        context.getMacroViewer().setMacroContext(context);
        context.getMacroViewer().initialiseUI();
        }


    /***********************************************************************************************
     * Create the MacroEditor Panel.
     *
     * @param context
     */

    public static void createMacroEditorPanel(final CommandProcessorContextInterface context)
        {
        final MacroUIComponentInterface editor;

        // Remove any previous MacroEditor
        if ((context != null)
            && (context.getMacroEditor() != null))
            {
            context.getMacroEditor().stopUI();
            context.getMacroEditor().disposeUI();
            }

        // Create a new MacroEditor and initialise it
        // Tab occupants MUST NOT have any sizes set!
        editor = new MacroEditorUIComponent(context.getHostTask(),
                                            context.getObservatoryInstrument(),
                                            REGISTRY.getFrameworkResourceKey());
        context.setMacroEditor(editor);
        context.getMacroEditor().setMacroContext(context);
        context.getMacroEditor().initialiseUI();
        }


    /***********************************************************************************************
     * Select a Macro.
     *
     * @param context The CommandProcessorContext
     * @param newmacro
     */

    public static void selectMacro(final CommandProcessorContextInterface context,
                                    final MacroType newmacro)
        {
        if (context != null)
            {
            if (context.getSelectedCommand() != null)
                {
                context.getSelectedCommand().clear();

                if (context.getCommandHelpViewer() != null)
                    {
                    context.getCommandHelpViewer().setHTMLText(EMPTY_STRING);
                    }

                if (context.getParameterHelpViewer() != null)
                    {
                    context.getParameterHelpViewer().setHTMLText("Under Development - called from selectMacro()");
                    }
                }

            if ((context.getSelectedMacro() != null)
                && (newmacro != null))
                {
                context.getSelectedMacro().clear();
                context.getSelectedMacro().add(newmacro);
                }

            if (context.getExecutionParameters() != null)
                {
                context.getExecutionParameters().clear();
                }

            if (context.getCommandParameters() != null)
                {
                context.getCommandParameters().clear();
                }
            }
        }


    /***********************************************************************************************
     * Debug a Macro.
     *
     * @param macro
     */

    public static void debugMacro(final MacroType macro)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((macro != null)
            && (macro.getStepList() != null))
            {
            final List<StepType> listSteps;

            listSteps = macro.getStepList();

            for (int i = 0;
                 i < listSteps.size();
                 i++)
                {
                debugMacroStep(macro, i);
                }
            }

        LOGGER.log(buffer.toString());
        }


    /***********************************************************************************************
     * Debug a Macro Step at the specified index.
     *
     * @param macro
     * @param stepindex
     */

    public static void debugMacroStep(final MacroType macro,
                                      final int stepindex)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((macro != null)
            && (macro.getStepList() != null)
            && (stepindex < macro.getStepList().size()))
            {
            final List<StepType> listSteps;
            final StepType step;

            listSteps = macro.getStepList();

            step = listSteps.get(stepindex);

            buffer.append("\n\rStep " + stepindex);
            buffer.append("\n\r");

            if (step.getLabel() != null)
                {
                buffer.append(step.getLabel());
                buffer.append(": ");
                }

            if (step.getComment() != null)
                {
                buffer.append("# ");
                buffer.append(step.getComment());
                }
            else if (step.getStarscript() != null)
                {
                final StepCommandType stepCommand;

                stepCommand = step.getStarscript();

                if (stepCommand.getInstrument() != null)
                    {
                    buffer.append(stepCommand.getInstrument());

                    if (stepCommand.getModule() != null)
                        {
                        buffer.append(".");
                        buffer.append(stepCommand.getModule());

                        if ((stepCommand.getCommand() != null)
                            && (stepCommand.getCommand().getIdentifier() != null))
                            {
                            buffer.append(".");
                            buffer.append(stepCommand.getCommand().getIdentifier());

                            if ((stepCommand.getCommand().getParameterList() != null)
                                && (!stepCommand.getCommand().getParameterList().isEmpty()))
                                {
                                final List<MacroParameterType> listParameters;

                                listParameters = stepCommand.getCommand().getParameterList();
                                buffer.append("(");

                                for (int j = 0;
                                     j < listParameters.size();
                                     j++)
                                    {
                                    final MacroParameterType parameter;

                                    parameter = listParameters.get(j);
                                    buffer.append(parameter.getToken());

                                    if (j < listParameters.size() - 1)
                                        {
                                        buffer.append(", ");
                                        }
                                    }

                                buffer.append(")");
                                }
                            else
                                {
                                buffer.append("()");
                                }
                            }
                        }
                    }
                }
            }

        LOGGER.log(buffer.toString());
        }
    }
