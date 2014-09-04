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


/***************************************************************************************************
 * CommandProcessorArchive.
 * Somewhere to keep the various UI experiments....
 */

public final class CommandProcessorArchive
    {

    }
//private static final String TOOLTIP_REPEAT_MODE = "Select repeat operation by Period or execution Count";
//private static final String MSG_REPEAT_PERIOD = "Period sec";
//private static final String MSG_REPEAT_COUNT = "Count";
//private static final String MSG_REPEAT_ERROR = "Error";
//    /**********************************************************************************************/
//    /* No Longer Used!                                                                            */
//    /***********************************************************************************************
//     * Create the Parameters and Macro/Command Execution panels, on a split pane.
//     *
//     * @param context The CommandProcessorContext
//     *
//     * @return JSplitPane
//     */
//
//    public static JSplitPane createIOSplitPane(final CommandProcessorContextInterface context)
//        {
//        final JSplitPane splitPane;
//        final JPanel panelParameters;
//        final JPanel panelExecution;
//        final int componentCount;
//
//        panelParameters = createParametersPanel(context, true);
//
//        // Are we supporting Macros?
//        if (LOADER_PROPERTIES.isCommandMacros())
//            {
//            panelExecution = createCommanderTabbedPanel(context);
//            }
//        else
//            {
//            panelExecution = createCommandExecutionPanel(context);
//            }
//
//        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                                   panelParameters,
//                                   panelExecution);
//        splitPane.setBorder(BorderFactory.createEmptyBorder());
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setContinuousLayout(false);
//        splitPane.setMinimumSize(new Dimension(100, 100));
//        splitPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
//        //splitPane.setDividerSize(SIZE_SPLIT_PANE_DIVIDER);
//        splitPane.setDividerSize(15);
//
//        // This was irritating!
//        // splitPane.setToolTipText(TOOLTIP_REVEAL_PARAMETERS);
//
//        //UIUtilities.addSplitPaneDividerTooltips(splitPane, TOOLTIP_HIDE_PARAMETERS, TOOLTIP_REVEAL_PARAMETERS);
//
//        // See: http://forums.sun.com/thread.jspa?threadID=714732&tstart=23790
//        // How to add the ToolTip to the little widgets in the JSplitpane
//        componentCount = splitPane.getComponentCount();
//
//        for (int i = 0;
//             i < componentCount;
//             i++)
//            {
//            final Component component;
//
//            component = splitPane.getComponent(i);
//
//            if (component instanceof BasicSplitPaneDivider)
//                {
//                final BasicSplitPaneDivider divider;
//                final int subComponentCount;
//
//                divider = (BasicSplitPaneDivider)component;
//
//                subComponentCount = divider.getComponentCount();
//
//                if (subComponentCount == 2)
//                    {
//                    if ((divider.getComponent(0) instanceof JButton)
//                        && (divider.getComponent(1) instanceof JButton))
//                        {
//                        ((JButton)divider.getComponent(0)).setToolTipText(TOOLTIP_HIDE_PARAMETERS);
//                        ((JButton)divider.getComponent(1)).setToolTipText(TOOLTIP_REVEAL_PARAMETERS);
//                        }
//                    }
//                }
//            }
//
//        // The setDividerLocation(double) method has no effect if the split pane has no size
//        // (typically true if it isn't onscreen yet). You can either use setDividerLocation(int)
//        // or specify the preferred sizes of the split pane's contained components
//        // and the split pane's resize weight instead.
//
//        // setResizeWeight() Specifies how to distribute extra space when the size of the split pane changes.
//        // A value of 0, the default, indicates the right/bottom component gets all the extra space
//        // (the left/top component acts fixed), where as a value of 1 specifies the left/top component
//        // gets all the extra space (the right/bottom component acts fixed).
//        // Specifically, the left/top component gets (weight * diff) extra space and the right/bottom
//        // component gets (1 - weight) * diff extra space.
//
//        // Setting the location explicitly seems to be the best I can do to reveal the parameters at startup...
//        splitPane.setDividerLocation(100);
//        splitPane.setResizeWeight(0.2);
//
//        return (splitPane);
//        }
//
//
//    /***********************************************************************************************
//     * Create the Command Execution JPanel.
//     * Used when Macros are enabled in loader.properties.
//     * Used only for the original split pane display.
//     *
//     * @param context The CommandProcessorContext
//     *
//     * @return JPanel
//     */
//
//    private static JPanel createCommanderTabbedPanel(final CommandProcessorContextInterface context)
//        {
//        final JTabbedPane tabbedPane;
//        final TitledBorder titledBorder;
//        final Border border;
//        ImageIcon graphicIcon;
//
//        // Create an interesting border
//        titledBorder = BorderFactory.createTitledBorder(TITLE_COMMAND_EXEC);
//        titledBorder.setTitleFont(context.getFontData().getFont());
//        titledBorder.setTitleColor(context.getColourData().getColor());
//        border = BorderFactory.createCompoundBorder(titledBorder,
//                                                    BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
//        // Configure the host ExecutionPanel which will contain the JTabbedPane
//        context.getExecutionPanel().removeAll();
//        context.getExecutionPanel().setLayout(new SpringLayout());
//        context.getExecutionPanel().setBorder(border);
//        context.getExecutionPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
//        context.getExecutionPanel().setMinimumSize(new Dimension(100, 100));
//        context.getExecutionPanel().setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
//
//        // Create and add the tabs
//        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
//
//        //tabbedPane.setFont(context.getObservatoryUI().getObservatoryFont());
//        tabbedPane.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
//        tabbedPane.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
//        tabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        // If we can't find the image, just return an empty Icon, i.e. not null
//        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
//                                                         ICON_TAB_COMMANDER);
//        // Tab occupants MUST NOT have any sizes set!
//        tabbedPane.addTab(TAB_COMMANDER,
//                          null,
//                          createCommanderToolbar(context),
//                          TOOLTIP_TAB_COMMANDER);
//
//        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
//                                                         MacroManagerUtilities.ICON_TAB_MACROMANAGER);
//        // Tab occupants MUST NOT have any sizes set!
//        tabbedPane.addTab(TAB_MACROMANAGER,
//                          null,
//                          MacroManagerUtilities.createMacroManagerPanel(context),
//                          TOOLTIP_TAB_MACROMANAGER);
//
//        context.getExecutionPanel().add(tabbedPane);
//
//        // Now do the layout of the CommandExecutionPanel (one row, one column)
//        SpringUtilities.makeCompactGrid(context.getExecutionPanel(),
//                                        1, 1,                         // Rows, Columns
//                                        0, 0,                         // Initial X, Y
//                                        1, 1);                        // Padding
//
//        return (context.getExecutionPanel());
//        }
//
//
//    /***********************************************************************************************
//     * Create the Command Execution JPanel.
//     * This consists of one or more Execution control buttons, arranged vertically.
//     * Used when Macros are not supported, or switched off in loader.properties.
//     *  Used only for the original split pane display.
//     *
//     * @param context The CommandProcessorContext
//     *
//     * @return JPanel
//     */
//
//    private static JPanel createCommandExecutionPanel(final CommandProcessorContextInterface context)
//        {
//        final TitledBorder titledBorder;
//        final Border border;
//        final JPanel panelButtons;
//
//        // Create an interesting border
//        titledBorder = BorderFactory.createTitledBorder(TITLE_COMMAND_EXEC);
//        titledBorder.setTitleFont(context.getFontData().getFont());
//        titledBorder.setTitleColor(context.getColourData().getColor());
//        border = BorderFactory.createCompoundBorder(titledBorder,
//                                                    BorderFactory.createEmptyBorder(5, 10, 0, 10));
//
//        // Configure the host CommandExecutionPanel
//        context.getExecutionPanel().removeAll();
//        context.getExecutionPanel().setLayout(new SpringLayout());
//        context.getExecutionPanel().setBorder(border);
//        context.getExecutionPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//        context.getExecutionPanel().setMinimumSize(new Dimension(100, 100));
//        context.getExecutionPanel().setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
//
//        // Create and add the Command button panel (Status, Execute, Repeat, Abort, RepeatConfig)
//        panelButtons = new JPanel();
//        // TODO REPLACE BOXLAYOUT
//        panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayoutFixed.Y_AXIS));
//        panelButtons.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//        panelButtons.setMinimumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
//        panelButtons.setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT_BUTTON));
//
//        // Add the status and buttons
//        context.setCommandStatusIndicator(MacroManagerUtilities.createStatusIndicator(context));
//        panelButtons.add(context.getCommandStatusIndicator());
//        panelButtons.add(Box.createVerticalStrut(HEIGHT_BUTTON_SEPARATOR));
//
//        // Execute, Repeat and Abort are created in the disabled state
//        context.setExecuteButton(createExecuteButton(context));
//        panelButtons.add(context.getExecuteButton());
//        panelButtons.add(Box.createVerticalStrut(HEIGHT_BUTTON_SEPARATOR));
//
//        context.setRepeatButton(createRepeatButton(context));
//        panelButtons.add(context.getRepeatButton());
//        panelButtons.add(Box.createVerticalStrut(HEIGHT_BUTTON_SEPARATOR));
//
//        context.setAbortButton(createAbortButton(context));
//        panelButtons.add(context.getAbortButton());
//        panelButtons.add(Box.createVerticalStrut(HEIGHT_BUTTON_SEPARATOR << 1));
//
//        context.setRepeatConfig(createRepeatConfig(context));
//        panelButtons.add(context.getRepeatConfig());
//        panelButtons.add(Box.createVerticalStrut(HEIGHT_BUTTON_SEPARATOR));
//
//        context.getExecutionPanel().add(panelButtons);
//
//        // Now do the layout of the CommandExecutionPanel (one row, one column)
//        SpringUtilities.makeCompactGrid(context.getExecutionPanel(), 1, 1, 0, 0, 1, 15);
//
//        return (context.getExecutionPanel());
//        }
//
//
//    /***********************************************************************************************
//     * Create the Parameters, Commander and Macro Manager panels, on a JTabbedPane.
//     *
//     * @param context The CommandProcessorContext
//     *
//     * @return JPanel
//     */
//
//    public static JPanel createExecutionTabbedPane(final CommandProcessorContextInterface context)
//        {
//        final JTabbedPane tabbedPane;
//        final TitledBorder titledBorder;
//        final Border border;
//        ImageIcon graphicIcon;
//        VerticalTextIcon textIcon;
//        CompositeIcon compositeIcon;
//
//        // Create an interesting border
//        titledBorder = BorderFactory.createTitledBorder(TITLE_COMMAND_EXEC);
//        titledBorder.setTitleFont(context.getFontData().getFont());
//        titledBorder.setTitleColor(context.getColourData().getColor());
//        border = BorderFactory.createCompoundBorder(titledBorder,
//                                                    BorderFactory.createEmptyBorder(3, 5, 5, 2));
//
//        // Configure the host ExecutionPanel which will contain the JTabbedPane
//        context.getExecutionPanel().removeAll();
//        context.getExecutionPanel().setLayout(new SpringLayout());
//        context.getExecutionPanel().setBorder(border);
//        context.getExecutionPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
//        context.getExecutionPanel().setMinimumSize(new Dimension(100, 100));
//        context.getExecutionPanel().setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
//
//        // Create and add the tabs on the *right*
//        //tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
//        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
//
//        //tabbedPane.setFont(context.getObservatoryUI().getObservatoryFont());
//        tabbedPane.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
//        tabbedPane.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
//        tabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        //------------------------------------------------------------------------------------------
//        // Parameters
//
//        // If we can't find the image, just return an empty Icon, i.e. not null
//        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
//                                                         ICON_TAB_PARAMETERS);
//        textIcon = new VerticalTextIcon(tabbedPane,
//                                        TAB_PARAMETERS,
//                                        VerticalTextIcon.ROTATE_DEFAULT);
//        compositeIcon = new CompositeIcon(graphicIcon, textIcon);
//
//        // Tab occupants MUST NOT have any sizes set!
////        tabbedPane.addTab(null,
////                          compositeIcon,
////                          createParametersPanel(context, false),
////                          TOOLTIP_TAB_PARAMETERS);
//
//        tabbedPane.addTab(TAB_PARAMETERS,
//                          null,
//                          createParametersPanel(context, false),
//                          TOOLTIP_TAB_PARAMETERS);
//
//        //------------------------------------------------------------------------------------------
//        // Commander
//
//        // If we can't find the image, just return an empty Icon
//        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
//                                                         ICON_TAB_COMMANDER);
//        textIcon = new VerticalTextIcon(tabbedPane,
//                                        TAB_COMMANDER,
//                                        VerticalTextIcon.ROTATE_DEFAULT);
//        compositeIcon = new CompositeIcon(graphicIcon, textIcon);
//
//        // Tab occupants MUST NOT have any sizes set!
////        tabbedPane.addTab(null,
////                          compositeIcon,
////                          createCommanderToolbar(context),
////                          TOOLTIP_TAB_COMMANDER);
//
//        tabbedPane.addTab(TAB_COMMANDER,
//                          null,
//                          createCommanderToolbar(context),
//                          TOOLTIP_TAB_COMMANDER);
//
//        //------------------------------------------------------------------------------------------
//        // Macro Manager, but only if we are supporting Macros
//
//        if (LOADER_PROPERTIES.isCommandMacros())
//            {
//            graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
//                                                             MacroManagerUtilities.ICON_TAB_MACROMANAGER);
//            textIcon = new VerticalTextIcon(tabbedPane,
//                                            TAB_MACROMANAGER,
//                                            VerticalTextIcon.ROTATE_DEFAULT);
//            compositeIcon = new CompositeIcon(graphicIcon, textIcon);
//
////            tabbedPane.addTab(null,
////                              compositeIcon,
////                              MacroManagerUtilities.createMacroManagerPanel(context),
////                              TOOLTIP_TAB_MACROMANAGER);
//
//            // Tab occupants MUST NOT have any sizes set!
//            tabbedPane.addTab(TAB_MACROMANAGER,
//                              null,
//                              MacroManagerUtilities.createMacroManagerPanel(context),
//                              TOOLTIP_TAB_MACROMANAGER);
//            }
//
//        //------------------------------------------------------------------------------------------
//        // Response Viewer
//
////        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
////                                                         MacroManagerUtilities.ICON_TAB_RESPONSE);
////        textIcon = new VerticalTextIcon(tabbedPane,
////                                        TAB_RESPONSE,
////                                        VerticalTextIcon.ROTATE_DEFAULT);
////        compositeIcon = new CompositeIcon(graphicIcon, textIcon);
//
////            tabbedPane.addTab(null,
////                              compositeIcon,
////                              (Component)context.getResponseViewer(),
////                              TOOLTIP_TAB_RESPONSE);
//
//        tabbedPane.addTab(TAB_RESPONSE,
//                          null,
//                          (Component)context.getResponseViewer(),
//                          TOOLTIP_TAB_RESPONSE);
//
//        context.getExecutionPanel().add(tabbedPane);
//
//        // Now do the layout of the CommandExecutionPanel (one row, one column)
//        SpringUtilities.makeCompactGrid(context.getExecutionPanel(),
//                                        1, 1,                         // Rows, Columns
//                                        0, 0,                         // Initial X, Y
//                                        1, 1);                        // Padding
//
//        return (context.getExecutionPanel());
//        }
//
//
//    /***********************************************************************************************
//     * Create the Commander Panel.
//     * This consists of one or more Execution control buttons, arranged vertically.
//     *
//     * @param context The CommandProcessorContext
//     *
//     * @return Component
//     */
//
//    private static Component createCommanderToolbar(final CommandProcessorContextInterface context)
//        {
//        final JScrollPane scrollCommander;
//        final TableLayout layout;
//        final JPanel panelCommander;
//
//        final double[][] size =
//            {
//                { // Columns 0...2
//                10,
//                TableLayout.PREFERRED,
//                TableLayout.FILL
//                },
//
//                { // Rows 0...6
//                1,
//                TableLayout.PREFERRED,
//                TableLayout.PREFERRED,
//                TableLayout.PREFERRED,
//                TableLayout.PREFERRED,
//                TableLayout.PREFERRED,
//                TableLayout.FILL
//                }
//            };
//
//        // TableLayout constraints for Buttons
//        // These specify where the buttons are to be placed in the layout
//        // Indicates TableLayoutConstraints's position and justification as a string in the form
//        // "column, row, horizontal justification, vertical justification"
//        // Valid values: LEFT, RIGHT, CENTER, TOP, BOTTOM
//
//        final String[] constraints =
//            {
//            // Row 1
//             "1, 1, LEFT, CENTER", // Status 0
//            // Row 2
//             "1, 2, LEFT, CENTER", // Execute 1
//            // Row 3
//             "1, 3, LEFT, CENTER", // Repeat  2
//            // Row 4
//             "1, 4, LEFT, CENTER", // Abort   3
//            // Row 5
//             "1, 5, LEFT, TOP",    // Repeat Config 4
//            };
//
//        // https://tablelayout.dev.java.net/articles/TableLayoutTutorialPart1/TableLayoutTutorialPart1.html
//        // https://tablelayout.dev.java.net/servlets/ProjectDocumentList?folderID=3487&expandFolder=3487&folderID=3487
//
//        // Create and add the Execution button panel
//        panelCommander = new JPanel();
//
//        layout = new TableLayout(size);
//        layout.setVGap(HEIGHT_BUTTON_SEPARATOR);
//
//        panelCommander.setLayout(layout);
//        panelCommander.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//
//        // Add the Status Indicator and Command buttons
//        // Status, Execute, Repeat, Abort, RepeatConfig
//
//        context.setCommandStatusIndicator(MacroManagerUtilities.createStatusIndicator(context));
//        panelCommander.add(context.getCommandStatusIndicator(), constraints[0]);
//
//        context.setExecuteButton(createExecuteButton(context));
//        panelCommander.add(context.getExecuteButton(), constraints[1]);
//
//        context.setRepeatButton(createRepeatButton(context));
//        panelCommander.add(context.getRepeatButton(), constraints[2]);
//
//        context.setAbortButton(createAbortButton(context));
//        panelCommander.add(context.getAbortButton(), constraints[3]);
//
//        context.setRepeatConfig(createRepeatConfig(context));
//        panelCommander.add(context.getRepeatConfig(), constraints[4]);
//
//        // Tab occupants MUST NOT have any sizes set!
//        scrollCommander = new JScrollPane(panelCommander,
//                                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        scrollCommander.setBorder(BorderFactory.createEmptyBorder());
//
//        return (scrollCommander);
//        }
//
//    }


///***********************************************************************************************
// * Create the configuration panel for Command Repeat.
// *
// * @param context
// *
// * @return JPanel
// */
//
//private static JPanel createRepeatConfig(final CommandProcessorContextInterface context)
//    {
//    final JPanel panelConfig;
//    final JPanel panelRadioButtons;
//    final ButtonGroup group;
//    final JRadioButton buttonPeriod;
//    final JRadioButton buttonCount;
//    final JTextField textValue;
//    final DocumentListener listenerDoc;
//    final ActionListener listenerPeriod;
//    final ActionListener listenerCount;
//
//    // Set some default values
//    // Use 30sec to try to avoid threads colliding if set too fast...
//    context.setRepeatPeriodMillis(ChronosHelper.SECOND_MILLISECONDS * 30);
//    context.setRepeatCount(1);
//    context.setRepeatPeriodMode(true);
//    context.setRepeatText(Units.SECONDS.toString());
//
//    buttonPeriod = new JRadioButton(MSG_REPEAT_PERIOD);
//    buttonPeriod.setHorizontalTextPosition(SwingConstants.LEFT);
//    buttonPeriod.setToolTipText(TOOLTIP_REPEAT_MODE);
//    buttonPeriod.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//    buttonPeriod.setFont(context.getFontData().getFont());
//    buttonPeriod.setForeground(context.getColourData().getColor());
//    buttonPeriod.setSelected(true);
//
//    buttonCount = new JRadioButton(MSG_REPEAT_COUNT);
//    buttonCount.setHorizontalTextPosition(SwingConstants.LEFT);
//    buttonCount.setToolTipText(TOOLTIP_REPEAT_MODE);
//    buttonCount.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//    buttonCount.setFont(context.getFontData().getFont());
//    buttonCount.setForeground(context.getColourData().getColor());
//    buttonCount.setSelected(false);
//
//    // TODO Make Count mode work!
//    buttonCount.setEnabled(false);
//
//    group = new ButtonGroup();
//    group.add(buttonPeriod);
//    group.add(buttonCount);
//
//    // Allow free-text entry for the repeat period or count
//    // Warning! Do not use the no-parameter constructor with SpringLayout!
//    textValue = new JTextField(10);
//    textValue.setAlignmentX(EXEC_PANEL_ALIGNMENT);
//    textValue.setText(Long.toString(context.getRepeatPeriodMillis() / ChronosHelper.SECOND_MILLISECONDS));
//
//    textValue.setMinimumSize(DIM_BUTTON_REPEAT);
//    textValue.setPreferredSize(DIM_BUTTON_REPEAT);
//    textValue.setMaximumSize(DIM_BUTTON_REPEAT);
//    textValue.setMargin(new Insets(0, 5, 0, 5));
//    textValue.setFont(context.getFontData().getFont());
//    textValue.setForeground(context.getColourData().getColor().darker());
//
//    // Text Field Listener
//    listenerDoc = new DocumentListener()
//        {
//        public void insertUpdate(final DocumentEvent event)
//            {
//            if (InstrumentState.isReady(context.getObservatoryInstrument()))
//                {
//                interpretRepeatConfig(context, buttonPeriod, buttonCount, textValue);
//                }
//            else
//                {
//                CommandProcessorUtilities.showUnavailableDialog(context);
//                }
//            }
//
//        public void removeUpdate(final DocumentEvent event)
//            {
//            if (InstrumentState.isReady(context.getObservatoryInstrument()))
//                {
//                interpretRepeatConfig(context, buttonPeriod, buttonCount, textValue);
//                }
//            else
//                {
//                CommandProcessorUtilities.showUnavailableDialog(context);
//                }
//            }
//
//        public void changedUpdate(final DocumentEvent event)
//            {
//            if (InstrumentState.isReady(context.getObservatoryInstrument()))
//                {
//                interpretRepeatConfig(context, buttonPeriod, buttonCount, textValue);
//                }
//            else
//                {
//                CommandProcessorUtilities.showUnavailableDialog(context);
//                }
//            }
//        };
//
//    textValue.getDocument().addDocumentListener(listenerDoc);
//
//    // Repeat Period Listener
//    listenerPeriod = new ActionListener()
//                        {
//                        public void actionPerformed(final ActionEvent e)
//                            {
//                            if (InstrumentState.isReady(context.getObservatoryInstrument()))
//                                {
//                                textValue.setText(interpretRepeatConfig(context,
//                                                                        buttonPeriod,
//                                                                        buttonCount,
//                                                                        textValue));
//                                }
//                            else
//                                {
//                                CommandProcessorUtilities.showUnavailableDialog(context);
//                                }
//                            }
//                        };
//    buttonPeriod.addActionListener(listenerPeriod);
//
//    // Repeat Count Listener
//    listenerCount = new ActionListener()
//                        {
//                        public void actionPerformed(final ActionEvent e)
//                            {
//                            if (InstrumentState.isReady(context.getObservatoryInstrument()))
//                                {
//                                textValue.setText(interpretRepeatConfig(context,
//                                                                        buttonPeriod,
//                                                                        buttonCount,
//                                                                        textValue));
//                                }
//                            else
//                                {
//                                CommandProcessorUtilities.showUnavailableDialog(context);
//                                }
//                            }
//                        };
//    buttonCount.addActionListener(listenerCount);
//
//    // Now assemble the UI
//    panelRadioButtons = new JPanel();
//    panelRadioButtons.setAlignmentX(EXEC_PANEL_ALIGNMENT);
//    // TODO REPLACE BOXLAYOUT
//    panelRadioButtons.setLayout(new BoxLayoutFixed(panelRadioButtons, BoxLayoutFixed.X_AXIS));
//    panelRadioButtons.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//    panelRadioButtons.setMinimumSize(DIM_BUTTON_REPEAT);
//    panelRadioButtons.setPreferredSize(DIM_BUTTON_REPEAT);
//
//    panelRadioButtons.add(buttonPeriod);
//    panelRadioButtons.add(Box.createHorizontalStrut(25));
//    panelRadioButtons.add(buttonCount);
//
//    panelConfig = new JPanel();
//    panelConfig.setAlignmentX(EXEC_PANEL_ALIGNMENT);
//    panelConfig.setMaximumSize(new Dimension(MacroManagerUtilities.WIDTH_CONTROL_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON << 1));
//    panelConfig.setMinimumSize(new Dimension(MacroManagerUtilities.WIDTH_CONTROL_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON << 1));
//    panelConfig.setPreferredSize(new Dimension(MacroManagerUtilities.WIDTH_CONTROL_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON << 1));
//    // TODO REPLACE BOXLAYOUT
//    panelConfig.setLayout(new BoxLayoutFixed(panelConfig, BoxLayoutFixed.Y_AXIS));
//    panelConfig.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());
//
//    // Finally add the radio buttons and the text entry box
//    panelConfig.add(textValue);
//    panelConfig.add(panelRadioButtons);
//
//    return (panelConfig);
//    }


