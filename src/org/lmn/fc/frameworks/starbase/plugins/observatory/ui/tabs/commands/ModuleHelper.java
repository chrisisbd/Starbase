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
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.instruments.Controller;
import org.lmn.fc.model.xmlbeans.instruments.PluginCategory;
import org.lmn.fc.model.xmlbeans.instruments.PluginManifestType;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * ModuleHelper.
 */

public final class ModuleHelper implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkSingletons,
                                           ResourceKeys
    {
    // String Resources
    private static final String TITLE_MODULES = "Modules";
    private static final String ICON_PRIMARY_PLUGIN = "primary-plugin.png";
    private static final String ICON_SECONDARY_PLUGIN = "secondary-plugin.png";
    private static final String TOOLTIP_PRIMARY_PLUGIN = "Primary Plugin: ";
    private static final String TOOLTIP_SECONDARY_PLUGIN = "Secondary Plugin: ";

    private static final Dimension DIM_BUTTON_MODULE = new Dimension(CommandProcessorUtilities.WIDTH_BUTTON, CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_PANEL_MODULES = new Dimension((int) DIM_BUTTON_MODULE.getWidth() + (CommandProcessorUtilities.WIDTH_SIDE_GAP << 1), Integer.MAX_VALUE);


    /***********************************************************************************************
     * Create the ModulePanel, and place on a JScrollPane.
     * Indicate the PrimaryPlugin given in the PluginManifest with an icon.
     *
     * @param context The CommandProcessorContext
     *
     * @return JScrollPane
     */

    public static JScrollPane createModulePanel(final CommandProcessorContextInterface context)
        {
        final TitledBorder border;
        final JScrollPane scrollModules;

        border = BorderFactory.createTitledBorder(TITLE_MODULES);
        border.setTitleFont(context.getFontData().getFont());
        border.setTitleColor(context.getColourData().getColor());

        context.getModulePanel().removeAll();
        // TODO REPLACE BOXLAYOUT
        context.getModulePanel().setLayout(new BoxLayoutFixed(context.getModulePanel(), BoxLayoutFixed.Y_AXIS));
        context.getModulePanel().setBorder(border);
        context.getModulePanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        context.getModulePanel().add(Box.createVerticalStrut(CommandProcessorUtilities.HEIGHT_TOP_GAP));

        // Add a set of Module buttons to the panel if a Controller is present
        // If there is no Controller, there's nothing to control...
        if ((context.getInstrument() != null)
            && (XmlBeansUtilities.isValidXml(context.getInstrument()))
            && (CommandProcessorUtilities.hasController(context)))
            {
            final Controller controller;
            final JButton buttonController;

            controller = context.getInstrument().getController();

            // Now add buttons for each of the Controller's Plugins
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final List<PluginType> plugins;

                plugins = controller.getPluginList();

                if ((plugins != null)
                    && (!plugins.isEmpty()))
                    {
                    final Iterator<PluginType> iterPlugins;

                    iterPlugins = plugins.iterator();

                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;

                        plugin = iterPlugins.next();

                        if (plugin != null)
                            {
                            final JButton buttonPlugin;
                            final ImageIcon icon;

                            buttonPlugin = new JButton(plugin.getIdentifier());
                            buttonPlugin.setFont(context.getFontData().getFont());
                            buttonPlugin.setForeground(context.getColourData().getColor());
                            buttonPlugin.setMinimumSize(DIM_BUTTON_MODULE);
                            buttonPlugin.setMaximumSize(DIM_BUTTON_MODULE);
                            buttonPlugin.setPreferredSize(DIM_BUTTON_MODULE);
                            buttonPlugin.setAlignmentX(Component.CENTER_ALIGNMENT);

                            // Assume for now that it's just a Module
                            buttonPlugin.setToolTipText(plugin.getDescription());

                            //---------------------------------------------------------------------
                            // User Hints

                            // What is the Plugin Category?
                            // Builder takes precedence over PrimaryPlugin, because the builder user clearly knows what they are doing!
                            if ((plugin.getCategory() != null)
                                && (PluginCategory.BUILDER.equals(plugin.getCategory()))
                                && (RoleName.BUILDER.toString().equals(REGISTRY_MODEL.getLoggedInUser().getRole().getName())))
                                {
                                icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                          ICON_CATEGORY_BUILDER);
                                buttonPlugin.setIcon(icon);

                                // Remind the User why this is special
                                buttonPlugin.setToolTipText(TOOLTIP_CATEGORY_BUILDER_MODULE + plugin.getDescription());
                                }

                            // See if we can give the User a hint about this Module
                            // It is possible that the PluginManifest hasn't been used :-)
                            else if (controller.getPluginManifest() != null)
                                {
                                final PluginManifestType manifest;

                                manifest = controller.getPluginManifest();

                                // Is this the *Primary* Plugin?
                                if ((manifest.getPrimaryResourceKey() != null)
                                    && (plugin.getIdentifier().equals(manifest.getPrimaryResourceKey()))
                                    && (context.getObservatoryInstrument() != null))
                                    {
                                    icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                              ICON_PRIMARY_PLUGIN);
                                    buttonPlugin.setIcon(icon);

                                    // Remind the User why this is special
                                    buttonPlugin.setToolTipText(TOOLTIP_PRIMARY_PLUGIN + plugin.getDescription());
                                    }
                                else if ((manifest.getResourceKeyList() != null)
                                    && (!manifest.getResourceKeyList().isEmpty())
                                    && (manifest.getResourceKeyList().contains(plugin.getIdentifier())))
                                    {
                                    // Identify all Secondaries
                                    icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                              ICON_SECONDARY_PLUGIN);
                                    buttonPlugin.setIcon(icon);

                                    // Remind the User why this is special
                                    buttonPlugin.setToolTipText(TOOLTIP_SECONDARY_PLUGIN + plugin.getDescription());
                                    }
                                else
                                    {
                                    // These should be Controller or HostPlugins Providers
                                    if (plugin.getProvider() != null)
                                        {
                                        buttonPlugin.setToolTipText(plugin.getProvider().toString() + ": " + plugin.getDescription());
                                        }
                                    else
                                        {
                                        // No Provider, so do nothing for now...
                                        }
                                    }
                                }
                            else
                                {
                                // No PluginManifest, so nothing to do... for now
                                }

                            // Give the User a hint that these are Macros
                            if ((plugin.getMacroList() != null)
                                && (!plugin.getMacroList().isEmpty()))
                                {
                                buttonPlugin.setBackground(MacroManagerUtilities.COLOR_MACRO_BUTTONS);
                                }
                            else if (plugin.getColour() != null)
                                {
                                // A 'coloured Plugin'!
                                buttonPlugin.setBackground((new ColourDataType(plugin.getColour())).getColor());
                                }

                            // End of Hints
                            //---------------------------------------------------------------------

                            buttonPlugin.addActionListener(new ActionListener()
                                {
                                public void actionPerformed(final ActionEvent e)
                                    {
                                    if (InstrumentState.isReady(context.getObservatoryInstrument()))
                                        {
                                        // Block Command execution
                                        context.getExecuteButton().setEnabled(false);

                                        // Block Command repeat
                                        context.getRepeatButton().setEnabled(false);

                                        // Show this button as the current selection
                                        CommandProcessorUtilities.selectButton(context,
                                                                               context.getModulePanel(),
                                                                               buttonPlugin);

                                        // Make the selection of the Plugin Module
                                        // This clears the Macros, Commands and the Parameters
                                        selectModule(context, plugin);

                                        // Get the new Macros *or* Commands for the Module selection
                                        if ((plugin.getMacroList() != null)
                                            && (!plugin.getMacroList().isEmpty()))
                                            {
                                            context.getAvailableMacros().addAll(plugin.getMacroList());
                                            }
                                        else if ((plugin.getCommandList() != null)
                                            && (!plugin.getCommandList().isEmpty()))
                                            {
                                            context.getAvailableCommands().addAll(plugin.getCommandList());
                                            }

                                        StarscriptHelper.updateStarscript(context);
                                        CommandHelper.rebuildCommandsPanel(context);
                                        // This will expand any appropriate Parameters
                                        ParameterHelper.rebuildParametersPanel(context);
                                        InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument());
                                        }
                                    else
                                        {
                                        CommandProcessorUtilities.showUnavailableDialog(context);
                                        }
                                    }
                                });

                            // Don't add the Plugin button if the Role is incorrect
                            // At the moment this only affects the Builder Role
                            if ((!PluginCategory.BUILDER.equals(plugin.getCategory()))
                                || ((PluginCategory.BUILDER.equals(plugin.getCategory()))
                                    && (RoleName.BUILDER.toString().equals(REGISTRY_MODEL.getLoggedInUser().getRole().getName()))))
                                {
                                context.getModulePanel().add(buttonPlugin);
                                context.getModulePanel().add(Box.createVerticalStrut(CommandProcessorUtilities.HEIGHT_BUTTON_SEPARATOR));
                                }
                            }
                        }
                    }
                }
            else
                {
                // No Controller Plugins were found, which is acceptable
                }

            // Now add buttons for the Controller's root (i.e. the Core Module)
            // This will never have a Category of Builder
            buttonController = new JButton(controller.getIdentifier());
            buttonController.setFont(context.getFontData().getFont());
            buttonController.setForeground(context.getColourData().getColor());
            buttonController.setMinimumSize(DIM_BUTTON_MODULE);
            buttonController.setMaximumSize(DIM_BUTTON_MODULE);
            buttonController.setPreferredSize(DIM_BUTTON_MODULE);
            buttonController.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonController.setToolTipText(controller.getDescription());

            buttonController.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent e)
                    {
                    if (InstrumentState.isReady(context.getObservatoryInstrument()))
                        {
                        // Block Command execution
                        context.getExecuteButton().setEnabled(false);

                        // Block Command repeat
                        context.getRepeatButton().setEnabled(false);

                        // Show this button as the current selection
                        CommandProcessorUtilities.selectButton(context, context.getModulePanel(),
                                                               buttonController);

                        // Make the selection of the Controller Module
                        // This clears the Commands and the parameters
                        selectModule(context, controller);

                        // Get the new Commands for the Module selection
                        // There are no Macros for the Controller
                        if (controller.getCommandList() != null)
                            {
                            context.getAvailableCommands().addAll(controller.getCommandList());
                            }

                        StarscriptHelper.updateStarscript(context);
                        CommandHelper.rebuildCommandsPanel(context);
                        // This will expand any appropriate Parameters
                        ParameterHelper.rebuildParametersPanel(context);
                        InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument());
                        }
                    else
                        {
                        CommandProcessorUtilities.showUnavailableDialog(context);
                        }
                    }
                });

            context.getModulePanel().add(buttonController);
            }
        else
            {
            // There is no Controller for this Instrument
            CommandProcessorUtilities.showEmptySet(context.getModulePanel(),
                                                   context.getFontData(),
                                                   context.getColourData(),
                                                   ObservatoryInstrumentInterface.CONTROLLER_NOT_FOUND);
            }

        // Present the Module panel on a scroll pane
        scrollModules = new JScrollPane(context.getModulePanel(),
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollModules.setBorder(BorderFactory.createEmptyBorder());
        scrollModules.setMinimumSize(DIM_PANEL_MODULES);
        scrollModules.setMaximumSize(DIM_PANEL_MODULES);
        scrollModules.setPreferredSize(DIM_PANEL_MODULES);

        return (scrollModules);
        }


    /***********************************************************************************************
     * Select a Module.
     *
     * @param context The CommandProcessorContext
     * @param newmodule
     */

    private static void selectModule(final CommandProcessorContextInterface context,
                                     final XmlObject newmodule)
        {
        if (context != null)
            {
            if ((context.getSelectedModule() != null)
                && (newmodule != null))
                {
                context.getSelectedModule().clear();
                context.getSelectedModule().add(newmodule);
                }

            if (context.getAvailableMacros() != null)
                {
                context.getAvailableMacros().clear();
                }

            if (context.getSelectedMacro() != null)
                {
                context.getSelectedMacro().clear();
                }

            if (context.getAvailableCommands() != null)
                {
                context.getAvailableCommands().clear();
                }

            if (context.getSelectedCommand() != null)
                {
                context.getSelectedCommand().clear();

                if (context.getCommandHelpViewer() != null)
                    {
                    context.getCommandHelpViewer().setHTMLText(EMPTY_STRING);
                    }

                if (context.getParameterHelpViewer() != null)
                    {
                    context.getParameterHelpViewer().setHTMLText("Under Development - called from selectModule()");
                    }
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
    }
