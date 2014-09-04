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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.CommandLifecycleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.CommandLifecycleUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.CompositeIcon;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.VerticalTextIcon;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.SpringUtilities;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;


/***************************************************************************************************
 * CommandProcessorUtilities.
 */

public final class CommandProcessorUtilities implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        ResourceKeys
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    public static final String BUTTON_CHOOSER = "Chooser";

    public static final String NO_SELECTION = "Waiting for selection";

    private static final String MSG_STOPPED = "You must start the Instrument before executing a Command";
    private static final String MSG_BUSY = "The Instrument is currently busy";
    private static final String MSG_ABORT_OR_WAIT = "Abort the operation, or wait until it is complete";

    private static final String ICON_TAB_PARAMETERS = "tab-parameters.png";

    private static final String TAB_PARAMETERS = "Parameters";
    private static final String TAB_MACROMANAGER = "Macro Manager";
    private static final String TAB_RESPONSE = "Response";
    private static final String TAB_COMMAND_HELP = "Command Help";
    private static final String TAB_PARAMETER_HELP = "Parameter Help";

    private static final String TOOLTIP_TAB_PARAMETERS = "Show the Parameters required for this Command";
    private static final String TOOLTIP_TAB_MACROMANAGER = "Management of Macros associated with this Instrument";
    private static final String TOOLTIP_TAB_RESPONSE = "The Response obtained from executing the last Command";
    private static final String TOOLTIP_TAB_COMMAND_HELP = "Guidance on command execution procedures";
    private static final String TOOLTIP_TAB_PARAMETER_HELP = "Information specific to this Parameter";

    public static final int HEIGHT_BUTTON = 27;
    public static final int HEIGHT_BUTTON_SEPARATOR = 10;
    public static final int WIDTH_BUTTON = 170;
    public static final int WIDTH_CHOOSER_BUTTON = 60;

    public static final int HEIGHT_TOP_GAP = 15;
    public static final int WIDTH_SIDE_GAP = 15;


    /***********************************************************************************************
     * Create the Commander toolbar with Execute, Repeat and Abort buttons,
     * and the Parameters, MacroManager and ResponseViewer panels, on a JTabbedPane.
     *
     * @param context The CommandProcessorContext
     *
     * @return JPanel
     */

    public static JPanel createExecutionPanel(final CommandProcessorContextInterface context)
        {
        final TitledBorder titledBorder;
        final JTabbedPane tabbedPane;
        final Border border;
        ImageIcon graphicIcon;
        VerticalTextIcon textIcon;
        CompositeIcon compositeIcon;

        // Create an interesting border
        titledBorder = BorderFactory.createTitledBorder(CommanderToolbarHelper.TITLE_COMMAND_EXEC);
        titledBorder.setTitleFont(context.getFontData().getFont());
        titledBorder.setTitleColor(context.getColourData().getColor());
        border = BorderFactory.createCompoundBorder(titledBorder,
                                                    BorderFactory.createEmptyBorder(3, 5, 5, 2));

        // Configure the host ExecutionPanel which will contain these components
        context.getExecutionPanel().removeAll();
        context.getExecutionPanel().setLayout(new SpringLayout());
        context.getExecutionPanel().setBorder(border);
        context.getExecutionPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        context.getExecutionPanel().setMinimumSize(new Dimension(100, 100));
        context.getExecutionPanel().setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Create the Commander toolbar panel
        context.getExecutionPanel().add(CommanderToolbarHelper.createCommanderToolbar(context));

        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.setFont(UIComponentPlugin.DEFAULT_FONT.getFont());
        tabbedPane.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
        tabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        //------------------------------------------------------------------------------------------
        // Parameters

        // If we can't find the image, just return an empty Icon, i.e. not null
        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                         ICON_TAB_PARAMETERS);
        textIcon = new VerticalTextIcon(tabbedPane,
                                        TAB_PARAMETERS,
                                        VerticalTextIcon.ROTATE_DEFAULT);
        compositeIcon = new CompositeIcon(graphicIcon, textIcon);

        // Tab occupants MUST NOT have any sizes set!
//        tabbedPane.addTab(null,
//                          compositeIcon,
//                          createParametersPanel(context, false),
//                          TOOLTIP_TAB_PARAMETERS);

        // Creation of the ParametersPanel issues a InstrumentStateChangedEvent
        tabbedPane.addTab(TAB_PARAMETERS,
                          null,
                          ParameterHelper.createParametersPanel(context, false),
                          TOOLTIP_TAB_PARAMETERS);

        //------------------------------------------------------------------------------------------
        // Macro Manager, but only if we are supporting Macros

        if (LOADER_PROPERTIES.isCommandMacros())
            {
            graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                             MacroManagerUtilities.ICON_TAB_MACROMANAGER);
            textIcon = new VerticalTextIcon(tabbedPane,
                                            TAB_MACROMANAGER,
                                            VerticalTextIcon.ROTATE_DEFAULT);
            compositeIcon = new CompositeIcon(graphicIcon, textIcon);

//            tabbedPane.addTab(null,
//                              compositeIcon,
//                              MacroManagerUtilities.createMacroManagerPanel(context),
//                              TOOLTIP_TAB_MACROMANAGER);

            // Tab occupants MUST NOT have any sizes set!
            tabbedPane.addTab(TAB_MACROMANAGER,
                              null,
                              MacroManagerUtilities.createMacroManagerPanel(context),
                              TOOLTIP_TAB_MACROMANAGER);
            }

        //------------------------------------------------------------------------------------------
        // Response Viewer

//        graphicIcon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
//                                                         MacroManagerUtilities.ICON_TAB_RESPONSE);
//        textIcon = new VerticalTextIcon(tabbedPane,
//                                        TAB_RESPONSE,
//                                        VerticalTextIcon.ROTATE_DEFAULT);
//        compositeIcon = new CompositeIcon(graphicIcon, textIcon);

//            tabbedPane.addTab(null,
//                              compositeIcon,
//                              (Component)context.getResponseViewer(),
//                              TOOLTIP_TAB_RESPONSE);

        tabbedPane.addTab(TAB_RESPONSE,
                          null,
                          (Component)context.getResponseViewer(),
                          TOOLTIP_TAB_RESPONSE);

        //------------------------------------------------------------------------------------------
        // Help Viewers

        tabbedPane.addTab(TAB_COMMAND_HELP,
                          null,
                          context.getCommandHelpViewer(),
                          TOOLTIP_TAB_COMMAND_HELP);

        tabbedPane.addTab(TAB_PARAMETER_HELP,
                          null,
                          context.getParameterHelpViewer(),
                          TOOLTIP_TAB_PARAMETER_HELP);

        context.setExecutionTabs(tabbedPane);
        context.getExecutionPanel().add(tabbedPane);

        // Now do the layout of the ExecutionPanel (two rows, one column)
        SpringUtilities.makeCompactGrid(context.getExecutionPanel(),
                                        2, 1,                         // Rows, Columns
                                        0, 0,                         // Initial X, Y
                                        1, 13);                       // Padding

        return (context.getExecutionPanel());
        }


    /***********************************************************************************************
     * Create the CommandLog Panel.
     *
     * @param context
     */

    public static void createCommandLogPanel(final CommandProcessorContextInterface context)
        {
        if (context != null)
            {
            final CommandLifecycleUIComponentInterface reportLog;

            // Remove any previous CommandLifecycleLog
            if (context.getCommandLifecycleLog() != null)
                {
                PORT_CONTROLLER.removeCommandLifecycleListener(context.getCommandLifecycleLog());
                context.getCommandLifecycleLog().stopUI();
                context.getCommandLifecycleLog().disposeUI();
                }

            // Create a new Log and initialise it
            // Tab occupants MUST NOT have any sizes set!
            reportLog = new CommandLifecycleUIComponent(context.getHostTask(),
                                                        context.getObservatoryInstrument(),
                                                        context.getObservatoryInstrument(),
                                                        REGISTRY.getFrameworkResourceKey(),
                                                        ReportTableToolbar.VERT_EAST_PRT_TV_DA);
            // Auto-scroll to the last entry
            reportLog.setScrollToRow(-1);

            context.setCommandLifecycleLog(reportLog);
            context.getCommandLifecycleLog().initialiseUI();

            // Listen for data
            PORT_CONTROLLER.addCommandLifecycleListener(context.getCommandLifecycleLog());
            }
        }


    /***********************************************************************************************
     * Deselect all JButtons on the specified Container, and then selected the one specified.
     *
     * @param context
     * @param container
     * @param button
     */

    public static void selectButton(final CommandProcessorContextInterface context,
                                    final Container container,
                                    final JButton button)
        {
        if ((container != null)
            && (container.getComponents() != null))
            {
            final Component[] components;

            components = container.getComponents();

            for (int i = 0; i < components.length; i++)
                {
                final Component component;

                component = components[i];

                if ((component != null)
                    && (component instanceof JButton)
                    && (context != null))
                    {
                    component.setForeground(context.getColourData().getColor());
                    }
                }

            if (button != null)
                {
                button.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT_HIGHLIGHT.getColor());
                }
            }
        }


    /***********************************************************************************************
     * Show a dialog indicating that the Instrument is unavailable, i.e STOPPED, BUSY or REPEATING.
     *
     * @param context
     */

    public static void showUnavailableDialog(final CommandProcessorContextInterface context)
        {
        if (context != null)
            {
            showUnavailableDialog(context.getObservatoryInstrument());
            }
        }


    /***********************************************************************************************
     * Show a dialog indicating that the Instrument is unavailable, i.e STOPPED, BUSY or REPEATING.
     *
     * @param obsinstrument
     */

    public static void showUnavailableDialog(final ObservatoryInstrumentInterface obsinstrument)
        {
        final String [] messageStopped =
            {
            MSG_STOPPED
            };

        final String [] messageBusy =
            {
            MSG_BUSY,
            MSG_ABORT_OR_WAIT
            };

        Toolkit.getDefaultToolkit().beep();

        if (obsinstrument != null)
            {
            if ((InstrumentState.isOff(obsinstrument)))
                {
                // STOPPED
                JOptionPane.showMessageDialog(null,
                                              messageStopped,
                                              obsinstrument.getInstrument().getName() + CommanderToolbarHelper.TITLE_DIALOG_COMMAND_EXECUTION,
                                              JOptionPane.WARNING_MESSAGE);
                }
            else
                {
                // BUSY or REPEATING
                JOptionPane.showMessageDialog(null,
                                              messageBusy,
                                              obsinstrument.getInstrument().getName() + CommanderToolbarHelper.TITLE_DIALOG_COMMAND_EXECUTION,
                                              JOptionPane.WARNING_MESSAGE);
                }
            }
        }


    /***********************************************************************************************
     * Add a label to show that nothing was found.
     *
     * @param hostpanel
     * @param fontdata
     * @param colourdata
     * @param text
     */

    public static void showEmptySet(final JPanel hostpanel,
                                    final FontInterface fontdata,
                                    final ColourInterface colourdata,
                                    final String text)
        {
        final JLabel labelText;

        // The previous layout was a SpringLayout
        hostpanel.setLayout(new BorderLayout());
        labelText = new JLabel(text);
        labelText.setBorder(BorderFactory.createEmptyBorder(HEIGHT_TOP_GAP,
                                                            WIDTH_SIDE_GAP,
                                                            HEIGHT_TOP_GAP,
                                                            WIDTH_SIDE_GAP));
        labelText.setFont(fontdata.getFont());
        labelText.setForeground(colourdata.getColor());
        hostpanel.add(labelText, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Return a flag indicating if this Instrument has a Controller.
     *
     * @param context
     *
     * @return boolean
     */

    public static boolean hasController(final CommandProcessorContextInterface context)
        {
        return ((context != null)
                && (hasController(context.getInstrument())));
        }


    /***********************************************************************************************
     * Return a flag indicating if this Instrument has a Controller.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean hasController(final Instrument instrument)
        {
        return ((instrument != null)
                && (instrument.getController() != null)
                && (!instrument.getController().isNil()));
        }
    }