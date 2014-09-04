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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroEditWindowInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandProcessorUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * A MacroEditorUIComponent, containing a MacroEditWindow and MacroEditButtons.
 */

public final class MacroEditorUIComponent extends UIComponent
                                          implements MacroUIComponentInterface
    {
    private static final Dimension DIM_PANEL_BUTTONS = new Dimension((int) MacroEditorHelper.DIM_BUTTON_EDIT.getWidth()+20, Integer.MAX_VALUE);

    // Injections
    private final TaskPlugin hostTask;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final String hostResourceKey;

    // UI
    private MacroEditWindowInterface editWindow;
    private JPanel panelEditButtons;
    private JPanel textEntryPanel;
    private JTextField textEntryField;

    // The MacroContext
    private CommandProcessorContextInterface macroContext;


    /***********************************************************************************************
     * Construct a MacroEditorUIComponent.
     * The ResourceKey is always that of the host Framework.
     *
     * @param task
     * @param hostinstrument
     * @param hostresourcekey
     */

    public MacroEditorUIComponent(final TaskPlugin task,
                                  final ObservatoryInstrumentInterface hostinstrument,
                                  final String hostresourcekey)
        {
        super();

        // Injections
        this.hostTask = task;
        this.hostInstrument = hostinstrument;
        this.hostResourceKey = hostresourcekey;

        this.editWindow = null;
        this.panelEditButtons = null;
        this.textEntryPanel = null;
        this.textEntryField = null;
        this.macroContext = null;
        }


    /***********************************************************************************************
     * initialiseUI().
     */

    public synchronized void initialiseUI()
        {
        final JPanel panelUI;
        final JPanel panelMacroEditor;
        final TableLayout layout;

        // https://tablelayout.dev.java.net/articles/TableLayoutTutorialPart1/TableLayoutTutorialPart1.html
        // https://tablelayout.dev.java.net/servlets/ProjectDocumentList?folderID=3487&expandFolder=3487&folderID=3487

        // The EditWindow fills all extra space,
        // the Buttons and TextEntry are at preferred sizes.
        final double[][] size =
            {
                { // Columns 0...1
                TableLayout.FILL,
                TableLayout.PREFERRED
                },

                { // Rows 0...1
                TableLayout.FILL,
                TableLayout.PREFERRED
                }
            };

        // TableLayout constraints
        // These specify where the components are to be placed in the layout
        // in a grid of two columns and two rows (column, row)
        // Indicates TableLayoutConstraints's position and justification as a string in the form
        // "column, row, horizontal justification, vertical justification"
        // Valid values: LEFT, RIGHT, FULL, CENTER, TOP, BOTTOM

        final String[] constraints =
            {
            // Row 0
             "0, 0, FULL, FULL",    // MacroEditor
             "1, 0, CENTER, TOP",   // Buttons

            // Row 1
             "0, 1, FULL, FULL"     // TextEntry
            };

        removeAll();
        super.initialiseUI();

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());

        // The host UIComponent uses BorderLayout
        add(panelUI, BorderLayout.CENTER);

        layout = new TableLayout(size);
        layout.setVGap(CommandProcessorUtilities.HEIGHT_BUTTON_SEPARATOR);

        panelMacroEditor = new JPanel();
        panelMacroEditor.setLayout(layout);
        panelMacroEditor.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Now add the components
        // The MacroContext should always be set before initialiseUI()
        this.editWindow = new MacroEditWindowUIComponent(getHostTask(),
                                                         getHostInstrument(),
                                                         getResourceKey(),
                                                         getMacroContext());
        this.panelEditButtons = createMacroEditButtons();

        this.textEntryPanel = MacroEditorHelper.createTextEntryPanel(getMacroContext(), this);

        // Initialise the EditWindow, the buttons and text field don't need initialising
        if (getMacroEditWindow() != null)
            {
            getMacroEditWindow().initialiseUI();
            }

        // Put it all together
        panelMacroEditor.add((Component) getMacroEditWindow(), constraints[0]);
        panelMacroEditor.add(getMacroEditButtons(), constraints[1]);
        panelMacroEditor.add(getTextEntryPanel(), constraints[2]);

        // This UIComponent is a MacroEditor on a ScrollPane
        panelUI.add(panelMacroEditor, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public synchronized void runUI()
        {
        super.runUI();

        if (getMacroEditWindow() != null)
            {
            getMacroEditWindow().runUI();
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public synchronized void stopUI()
        {
        if (getMacroEditWindow() != null)
            {
            getMacroEditWindow().stopUI();
            }

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        if (getMacroEditWindow() != null)
            {
            getMacroEditWindow().disposeUI();
            }

        if (getMacroContext() != null)
            {
            setMacroContext(null);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Create the panel of JButtons used for editing the Macro.
     *
     * @return JPanel
     */

    private JPanel createMacroEditButtons()
        {
        final JPanel panelUI;
        final JPanel panelButtons;
        final JScrollPane scrollButtons;
        final TableLayout layout;
        int intConstraintIndex;

        // https://tablelayout.dev.java.net/articles/TableLayoutTutorialPart1/TableLayoutTutorialPart1.html
        // https://tablelayout.dev.java.net/servlets/ProjectDocumentList?folderID=3487&expandFolder=3487&folderID=3487

        final double[][] size =
            {
                { // Columns 0...2
                TableLayout.FILL,
                TableLayout.PREFERRED,
                TableLayout.FILL
                },

                { // Rows 0...8
                1,
                TableLayout.PREFERRED,
                TableLayout.PREFERRED,
                TableLayout.PREFERRED,
                TableLayout.PREFERRED,
                TableLayout.PREFERRED,
                TableLayout.PREFERRED,
                TableLayout.PREFERRED,
                TableLayout.FILL
                }
            };

        // TableLayout constraints for Buttons
        // These specify where the buttons are to be placed in the layout
        // in a grid of two columns and seven rows (column, row)
        // Indicates TableLayoutConstraints's position and justification as a string in the form
        // "column, row, horizontal justification, vertical justification"
        // Valid values: LEFT, RIGHT, FULL, CENTER, TOP, BOTTOM

        final String[] constraints =
            {
             // column, row, horizontal justification, vertical justification
             "1, 1, CENTER, CENTER",
             "1, 2, CENTER, CENTER",
             "1, 3, CENTER, CENTER",
             "1, 4, CENTER, CENTER",
             "1, 5, CENTER, CENTER",
             "1, 6, CENTER, CENTER",
             "1, 7, CENTER, CENTER"
            };

        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());
        // The TableLayout will need the PreferredSize
        panelUI.setPreferredSize(DIM_PANEL_BUTTONS);

        // Create and add the Edit button panel
        panelButtons = new JPanel();

        layout = new TableLayout(size);
        layout.setVGap(CommandProcessorUtilities.HEIGHT_BUTTON_SEPARATOR);

        panelButtons.setLayout(layout);
        panelButtons.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        panelButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

//        panelButtons.setPreferredSize(DIM_PANEL_BUTTONS);

        // Add all of the Buttons and their constraints
        intConstraintIndex = 0;

        panelButtons.add(MacroEditorHelper.createAddSelectionButton(getMacroContext(), this), constraints[intConstraintIndex++]);
        panelButtons.add(MacroEditorHelper.createMoveUpButton(getMacroContext(), this), constraints[intConstraintIndex++]);
        panelButtons.add(MacroEditorHelper.createMoveDownButton(getMacroContext(), this), constraints[intConstraintIndex++]);
        panelButtons.add(MacroEditorHelper.createDeleteLineButton(getMacroContext(), this), constraints[intConstraintIndex++]);
        panelButtons.add(MacroEditorHelper.createEditLabelButton(getMacroContext(), this), constraints[intConstraintIndex++]);
        panelButtons.add(MacroEditorHelper.createEditCommentButton(getMacroContext(), this), constraints[intConstraintIndex++]);
        panelButtons.add(MacroEditorHelper.createValidateButton(getMacroContext(), this), constraints[intConstraintIndex]);

        // Present the Buttons panel on a scroll pane
        scrollButtons = new JScrollPane(panelButtons,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollButtons.setBorder(BorderFactory.createEmptyBorder());
//        scrollButtons.setPreferredSize(DIM_PANEL_BUTTONS);

        panelUI.add(scrollButtons, BorderLayout.CENTER);

        return (panelUI);
        }


    /***********************************************************************************************
     * Get the MacroContext containing the Macro to edit.
     * Return NULL if none.
     *
     * @return MacroType
     */

    public CommandProcessorContextInterface getMacroContext()
        {
        return (this.macroContext);
        }


    /***********************************************************************************************
     * Set the MacroContext containing the Macro to edit.
     *
     * @param context
     */

    public void setMacroContext(final CommandProcessorContextInterface context)
        {
        this.macroContext = context;

        // Pass this on to the EditWindow
        macroChanged();
        }


    /***********************************************************************************************
     * Indicate that there has been a change of Macro.
     */

    public synchronized final void macroChanged()
        {
        // Pass this on to the EditWindow
        if (getMacroEditWindow() != null)
            {
            getMacroEditWindow().setMacroContext(getMacroContext());
            }
        else
            {
            //LOGGER.error("MacroEditorUIComponent.macroChanged() Unable to set Macro Context, NULL Window");
            }
        }


    /***********************************************************************************************
     * Get the Macro Edit Window.
     *
     * @return MacroEditWindowInterface
     */

    private synchronized MacroEditWindowInterface getMacroEditWindow()
        {
        return (this.editWindow);
        }


    /***********************************************************************************************
     * Get the Macro Edit Buttons.
     *
     * @return JPanel
     */

    private synchronized JPanel getMacroEditButtons()
        {
        return (this.panelEditButtons);
        }


    /***********************************************************************************************
     * Get the TextEntry panel.
     *
     * @return JPanel
     */

    private synchronized JPanel getTextEntryPanel()
        {
        return (this.textEntryPanel);
        }


    /***********************************************************************************************
     * Get the TextEntry field.
     *
     * @return JTextField
     */

    private synchronized JTextField getTextEntryField()
        {
        return (this.textEntryField);
        }


    /***********************************************************************************************
     * Set the TextEntry field.
     *
     * @param field
     */

    public synchronized void setTextEntryField(final JTextField field)
        {
        this.textEntryField = field;
        }


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    private TaskPlugin getHostTask()
        {
        return (this.hostTask);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private synchronized ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ResourceKey .
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.hostResourceKey);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
