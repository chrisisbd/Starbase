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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ActivityIndicatorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ViewingMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.SpringUtilities;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;


/***************************************************************************************************
 * CommandBuilderUIComponent.
 */

public final class CommandBuilderUIComponent extends InstrumentUIComponentDecorator
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    // String Resources
    private static final String TOOLTIP_REVEAL_COMMANDS = "Reveal the Commands panel by clicking on the small down-arrow";
    private static final String TOOLTIP_HIDE_COMMANDS = "Hide the Commands panel by clicking on the small up-arrow";

    private static final int DIVIDER_SIZE = 15;
    private static final double DIVIDER_LOCATION = 0.8;
    private static final double RESIZE_WEIGHT = 0.5;
    private static final int VIEWER_MINIMUM_HEIGHT = 93;


    /***********************************************************************************************
     * Construct a CommandBuilderUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public CommandBuilderUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                     final Instrument instrumentxml,
                                     final ObservatoryUIInterface hostui,
                                     final TaskPlugin task,
                                     final FontInterface font,
                                     final ColourInterface colour,
                                     final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "CommandBuilderUIComponent.initialiseUI() ";
        final JSplitPane splitPane;
        final JPanel panelTop;
        final JPanel panelBuilder;
        final JPanel panelBottom;
        final JScrollPane scrollModules;
        final JScrollPane scrollCommands;
        final JPanel panelCommander;

        // DO NOT USE super.initialiseUI()

        removeAll();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(false);
        splitPane.setDividerSize(DIVIDER_SIZE);

        // Setting the location explicitly seems to be the best I can do to reveal the CommandLog at startup...
        splitPane.setDividerLocation(DIVIDER_LOCATION);
        splitPane.setResizeWeight(RESIZE_WEIGHT);

        // Sets the L&F object that renders this component,
        // which has a custom divider
        //UIUtilities.setSplitPaneDividerColor(splitPane, Color.pink);
        //uiDelegate = new ObservatorySplitPaneUI();

        // Add a gradient fill Border to the UI divider
        //UIUtilities.addSplitPaneDividerGradientBorder((BasicSplitPaneUI)splitPane.getUI());
        //UIUtilities.addSplitPaneDividerTooltips(splitPane, TOOLTIP_HIDE_COMMANDS, TOOLTIP_REVEAL_COMMANDS);

//        splitPane.setUI(uiDelegate);
//        uiDelegate.installUI(splitPane);

        // The host UIComponent uses BorderLayout
        add(splitPane, BorderLayout.CENTER);

        panelTop = new JPanel();
        panelTop.setLayout(new BorderLayout());

        splitPane.setTopComponent(panelTop);

        // The Indicator panel contains the StarScript Command text
        getContext().setStarscriptIndicatorPanel(new JPanel());
        getContext().setStarscriptIndicator(StarscriptHelper.createStarscriptIndicator(getContext()));

        panelTop.add(getContext().getStarscriptIndicatorPanel(), BorderLayout.NORTH);

        // The Module, Commands and Parameters & Response panels are laid out horizontally
        panelBuilder = new JPanel();
        panelBuilder.setLayout(new SpringLayout());
        panelBuilder.setBorder(BorderFactory.createEmptyBorder());
        panelBuilder.setMaximumSize(DIM_UNIVERSE);
        panelBuilder.setPreferredSize(DIM_UNIVERSE);
        panelBuilder.setBackground(UIComponentPlugin.DEFAULT_COLOUR_PANEL.getColor());

        panelTop.add(panelBuilder, BorderLayout.CENTER);

        // Start with some blank panels
        getContext().setModulePanel(new JPanel());
        getContext().setCommandPanel(new JPanel());
        getContext().setParameterPanel(new JPanel());
        getContext().setExecutionPanel(new JPanel());
        getContext().setResponseViewer(new ResponseViewerUIComponent(getHostInstrument(),
                                                                     getResourceKey()));
        PORT_CONTROLLER.addCommandLifecycleListener(getContext().getResponseViewer());

        // Set up a panel of the available Modules
        scrollModules = ModuleHelper.createModulePanel(getContext());
        panelBuilder.add(scrollModules);

        // An initially blank Commands panel, until a Module is selected
        scrollCommands = CommandHelper.createCommandsPanel(getContext());
        panelBuilder.add(scrollCommands);

        // Create the Commander panel and toolbar, with all components on tabs
        // Initially blank Parameters panel, until a Command is selected
        panelCommander = CommandProcessorUtilities.createExecutionPanel(getContext());
        panelBuilder.add(panelCommander);

        // Don't forget to initialise the ResponseViewer and the HelpViewers,
        // because these are UIComponents
        getContext().getResponseViewer().initialiseUI();
        getContext().getCommandHelpViewer().initialiseUI();
        getContext().getParameterHelpViewer().initialiseUI();
        //getContext().getParameterHelpViewer().setHTMLText("Under Development - called from initialiseUI()");

        // Now do the layout of the builder panel... (one row, three columns)
        SpringUtilities.makeCompactGrid(panelBuilder, 1, 3, 0, 0, 1, 1);
        panelBuilder.revalidate();

        //------------------------------------------------------------------------------------------
        // Finally add the CommandLog or MacroViewer at the bottom,
        // depending on the Observatory Mode
        // Use a separate container panel to simplify switching when the Mode changes

        panelBottom = new JPanel();
        // This minimum size leaves room for three report rows, showing the toolbar icons
        panelBottom.setMinimumSize(new Dimension(Integer.MAX_VALUE, VIEWER_MINIMUM_HEIGHT));
        panelBottom.setLayout(new BorderLayout());

        splitPane.setBottomComponent(panelBottom);

        // Make sure the Context can find whichever panel is installed
        // This is never changed anywhere else
        getContext().setViewerContainer(panelBottom);

        // Create and initialise the MacroViewer, MacroEditor and CommandLog panels

        // MacroViewer is a ReportTable, so will have a ScrollPane
        MacroManagerUtilities.createMacroViewerPanel(getContext());

        // MacroEditor is a UIComponent which requires an explicit ScrollPane
        MacroManagerUtilities.createMacroEditorPanel(getContext());

        // CommandLog is a ReportTable, so will have a ScrollPane
        CommandProcessorUtilities.createCommandLogPanel(getContext());

        // The Context now has references directly to the panels,
        // rather than to their containers

        // Put the appropriate Viewer in the container
        // At initialisation this should only ever be the CommandLifecycleLog
        if ((getContext().getObservatory() != null)
            && (getContext().getObservatory().isRecordMacroMode()))
            {
            // This should never happen during initialiseUI() ?
            getContext().setViewingMode(ViewingMode.MACRO_VIEWER);
            getContext().getViewerContainer().add((Component)getContext().getMacroViewer(),
                                                  BorderLayout.CENTER);
            }
        else
            {
            getContext().setViewingMode(ViewingMode.COMMAND_LOG);
            getContext().getViewerContainer().add((Component)getContext().getCommandLifecycleLog(),
                                                  BorderLayout.CENTER);
            }

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE +  "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "CommandBuilderUIComponent.runUI() ";

        super.runUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE +  "[ui.state=" + getUIState().getName() + "]");

        if ((getContext() != null)
            && (getContext().getViewingMode() != null))
            {
            switch (getContext().getViewingMode())
                {
                case COMMAND_LOG:
                    {
                    if (getContext().getCommandLifecycleLog() != null)
                        {
                        // runUI() will create ContextActions in the ReportTable and add to the CommandLogReport
                        // Then transfer from the CommandLogReport to this CommandBuilderUIComponent
                        UIComponentHelper.runComponentAndTransferActions((Component) getContext().getCommandLifecycleLog(),
                                                                         this);
                        }

                    break;
                    }

                case MACRO_VIEWER:
                    {
                    if (getContext().getMacroViewer() != null)
                        {
                        // runUI() will create ContextActions in the ReportTable and add to the MacroViewer
                        // Then transfer from the MacroViewer to this CommandBuilderUIComponent
                        UIComponentHelper.runComponentAndTransferActions((Component) getContext().getMacroViewer(),
                                                                         this);
                        }

                    break;
                    }

                case MACRO_EDITOR:
                    {
                    if (getContext().getMacroEditor() != null)
                        {
                        // runUI() will create ContextActions in the ReportTable and add to the MacroEditor
                        // Then transfer from the MacroEditor to this CommandBuilderUIComponent
                        UIComponentHelper.runComponentAndTransferActions((Component) getContext().getMacroEditor(),
                                                                         this);
                        }

                    break;
                    }

                default:
                    {
                    if (getContext().getCommandLifecycleLog() != null)
                        {
                        // runUI() will create ContextActions in the ReportTable and add to the CommandLogReport
                        // Then transfer from the CommandLogReport to this CommandBuilderUIComponent
                        UIComponentHelper.runComponentAndTransferActions((Component) getContext().getCommandLifecycleLog(),
                                                                         this);
                        }
                    }
                }

            if (getContext().getResponseViewer() != null)
                {
                getContext().getResponseViewer().runUI();
                }

            if (getContext().getCommandHelpViewer() != null)
                {
                getContext().getCommandHelpViewer().runUI();
                }

            if (getContext().getParameterHelpViewer() != null)
                {
                getContext().getParameterHelpViewer().runUI();
                }

            if (getContext().getActivityIndicatorList() != null)
                {
                final Iterator<ActivityIndicatorUIComponentInterface> iterIndicators;

                iterIndicators = getContext().getActivityIndicatorList().iterator();

                while (iterIndicators.hasNext())
                    {
                    final ActivityIndicatorUIComponentInterface indicator;

                    indicator = iterIndicators.next();

                    LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                           SOURCE + "Go to indicator.runUI()");
                    indicator.runUI();
                    }
                }
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "CommandBuilderUIComponent.stopUI() ";

        // Stop everything, regardless of if it being shown
        if (getContext() != null)
            {
            if (getContext().getCommandHelpViewer() != null)
                {
                getContext().getCommandHelpViewer().stopUI();
                }

            if (getContext().getParameterHelpViewer() != null)
                {
                getContext().getParameterHelpViewer().stopUI();
                }

            if (getContext().getResponseViewer() != null)
                {
                getContext().getResponseViewer().stopUI();
                }

            if (getContext().getCommandLifecycleLog() != null)
                {
                getContext().getCommandLifecycleLog().stopUI();
                }

            if (getContext().getMacroViewer() != null)
                {
                getContext().getMacroViewer().stopUI();
                }

            if (getContext().getMacroEditor() != null)
                {
                getContext().getMacroEditor().stopUI();
                }

            if (getContext().getActivityIndicatorList() != null)
                {
                final Iterator<ActivityIndicatorUIComponentInterface> iterIndicators;

                iterIndicators = getContext().getActivityIndicatorList().iterator();

                while (iterIndicators.hasNext())
                    {
                    final ActivityIndicatorUIComponentInterface indicator;

                    indicator = iterIndicators.next();

                    LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                           SOURCE + "Go to indicator.stopUI()");
                    indicator.stopUI();
                    }
                }
            }

        super.stopUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE +  "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Dispose of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "CommandBuilderUIComponent.disposeUI() ";

        // Dispose everything, regardless of if it being shown
        if (getContext() != null)
            {
            if (getContext().getCommandHelpViewer() != null)
                {
                getContext().getCommandHelpViewer().stopUI();
                getContext().getCommandHelpViewer().disposeUI();
                }

            if (getContext().getParameterHelpViewer() != null)
                {
                getContext().getParameterHelpViewer().stopUI();
                getContext().getParameterHelpViewer().disposeUI();
                }

            if (getContext().getResponseViewer() != null)
                {
                PORT_CONTROLLER.removeCommandLifecycleListener(getContext().getResponseViewer());
                getContext().getResponseViewer().stopUI();
                getContext().getResponseViewer().disposeUI();
                }

            if (getContext().getCommandLifecycleLog() != null)
                {
                PORT_CONTROLLER.removeCommandLifecycleListener(getContext().getCommandLifecycleLog());
                getContext().getCommandLifecycleLog().stopUI();
                getContext().getCommandLifecycleLog().disposeUI();
                }

            if (getContext().getMacroViewer() != null)
                {
                getContext().getMacroViewer().stopUI();
                getContext().getMacroViewer().disposeUI();
                }

            if (getContext().getMacroEditor() != null)
                {
                getContext().getMacroEditor().stopUI();
                getContext().getMacroEditor().disposeUI();
                }

            if (getContext().getActivityIndicatorList() != null)
                {
                final Iterator<ActivityIndicatorUIComponentInterface> iterIndicators;

                iterIndicators = getContext().getActivityIndicatorList().iterator();

                while (iterIndicators.hasNext())
                    {
                    final ActivityIndicatorUIComponentInterface indicator;

                    indicator = iterIndicators.next();
                    indicator.stopUI();

                    LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                                           SOURCE + "Go to indicator.disposeUI()");
                    indicator.disposeUI();
                    }
                }
            }

        super.disposeUI();

        LOGGER.debugStateEvent(LOADER_PROPERTIES.isStateDebug(),
                               SOURCE +  "[ui.state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Ensure the the CommandLifecycleLog, MacroViewer or MacroEditor is controllable and truncatable.
     *
     * @return boolean
     */

    public boolean isVisible()
        {
        boolean boolVisible;

        boolVisible = super.isVisible();

        if ((getContext() != null)
            && (getContext().getViewingMode() != null))
            {
            switch (getContext().getViewingMode())
                {
                case COMMAND_LOG:
                    {
                    if (getContext().getCommandLifecycleLog() != null)
                        {
                        boolVisible = getContext().getCommandLifecycleLog().isVisible();
                        }

                    break;
                    }

                case MACRO_VIEWER:
                    {
                    if (getContext().getMacroViewer() != null)
                        {
                        boolVisible = getContext().getMacroViewer().isVisible();
                        }

                    break;
                    }

                case MACRO_EDITOR:
                    {
                    if (getContext().getMacroEditor() != null)
                        {
                        boolVisible = getContext().getMacroEditor().isVisible();
                        }

                    break;
                    }

                default:
                    {
                    if (getContext().getCommandLifecycleLog() != null)
                        {
                        boolVisible = getContext().getCommandLifecycleLog().isVisible();
                        }
                    }
                }
            }

        return (boolVisible);
        }


    /***********************************************************************************************
     * Ensure the the CommandLifecycleLog or MacroViewer is controllable and truncatable.
     *
     * @param visible
     */

    public void setVisible(final boolean visible)
        {
        super.setVisible(visible);

        // If the CommandBuilderUIComponent is visible,
        // then make sure the inner CommandLifecycleLog, MacroViewer or MacroEditor is visible also

        // ToDo REVIEW - This does not make the component visible!

        if ((getContext() != null)
            && (getContext().getViewingMode() != null))
            {
            switch (getContext().getViewingMode())
                {
                case COMMAND_LOG:
                    {
                    if (getContext().getCommandLifecycleLog() != null)
                        {
                        getContext().getCommandLifecycleLog().setVisible(visible);
                        }

                    break;
                    }

                case MACRO_VIEWER:
                    {
                    if (getContext().getMacroViewer() != null)
                        {
                        getContext().getMacroViewer().setVisible(visible);
                        }

                    break;
                    }

                case MACRO_EDITOR:
                    {
                    if (getContext().getMacroEditor() != null)
                        {
                        getContext().getMacroEditor().setVisible(visible);
                        }

                    break;
                    }

                default:
                    {
                    if (getContext().getCommandLifecycleLog() != null)
                        {
                        getContext().getCommandLifecycleLog().setVisible(visible);
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Get the CommandProcessorContext.
     *
     * @return CommandProcessorContextInterface
     */

    private CommandProcessorContextInterface getContext()
        {
        return (getHostInstrument().getContext());
        }
    }
