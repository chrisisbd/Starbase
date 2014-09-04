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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.CommandProcessorContext;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.instruments.CommandCategory;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


/***************************************************************************************************
 * CommandHelper.
 */

public final class CommandHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ResourceKeys
    {
    // String Resources
    private static final String TITLE_COMMANDS = "Commands";
    private static final String MSG_HELP_UNDER_DEVELOPMENT = "Help under development for ";

    // NOTE: Extra 35px added to width to improved readability of long Command names
    private static final Dimension DIM_BUTTON_COMMAND = new Dimension(CommandProcessorUtilities.WIDTH_BUTTON + 35, CommandProcessorUtilities.HEIGHT_BUTTON);
    private static final Dimension DIM_PANEL_COMMANDS = new Dimension((int) DIM_BUTTON_COMMAND.getWidth() + (CommandProcessorUtilities.WIDTH_SIDE_GAP << 1), Integer.MAX_VALUE);


    /***********************************************************************************************
     * Create the CommandsPanel, and place on a JScrollPane.
     *
     * @param context The CommandProcessorContext
     *
     * @return JScrollPane
     */

    public static JScrollPane createCommandsPanel(final CommandProcessorContextInterface context)
        {
        final JScrollPane scrollCommands;

        rebuildCommandsPanel(context);
        InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument());

        // Present the Commands panel on a scroll pane
        scrollCommands = new JScrollPane(context.getCommandPanel(),
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCommands.setBorder(BorderFactory.createEmptyBorder());
        scrollCommands.setMinimumSize(DIM_PANEL_COMMANDS);
        scrollCommands.setMaximumSize(DIM_PANEL_COMMANDS);
        scrollCommands.setPreferredSize(DIM_PANEL_COMMANDS);

        return (scrollCommands);
        }


    /***********************************************************************************************
     * Rebuild the Commands panel.
     *
     * @param context The CommandProcessorContext
     */

    public static void rebuildCommandsPanel(final CommandProcessorContextInterface context)
        {
        final TitledBorder border;

        border = BorderFactory.createTitledBorder(TITLE_COMMANDS);
        border.setTitleFont(context.getFontData().getFont());
        border.setTitleColor(context.getColourData().getColor());

        context.getCommandPanel().removeAll();
        // TODO REPLACE BOXLAYOUT
        context.getCommandPanel().setLayout(new BoxLayoutFixed(context.getCommandPanel(), BoxLayoutFixed.Y_AXIS));
        context.getCommandPanel().setBorder(border);
        context.getCommandPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        context.getCommandPanel().add(Box.createVerticalStrut(CommandProcessorUtilities.HEIGHT_TOP_GAP));

        // Add a set of Command buttons to the panel
        if ((context.getSelectedModule() != null)
            && (!context.getSelectedModule().isEmpty()))
            {
            if ((context.getAvailableMacros() != null)
                && (!context.getAvailableMacros().isEmpty()))
                {
                final Iterator<MacroType> iterMacros;
                int intMacroCount;

                iterMacros = context.getAvailableMacros().iterator();
                intMacroCount = 0;

                while (iterMacros.hasNext())
                    {
                    final MacroType macroType;

                    macroType = iterMacros.next();

                    if (macroType != null)
                        {
                        final JButton buttonMacroCommand;

                        buttonMacroCommand = new JButton(macroType.getIdentifier());
                        buttonMacroCommand.setFont(context.getFontData().getFont());

                        buttonMacroCommand.setForeground(context.getColourData().getColor());

                        // See if we have any special colouring
                        if (macroType.getColour() != null)
                            {
                            // Regex should have validated the colour specification
                            buttonMacroCommand.setBackground((new ColourDataType(macroType.getColour())).getColor());
                            }
                        else
                            {
                            buttonMacroCommand.setBackground(MacroManagerUtilities.COLOR_MACRO_BUTTONS);
                            }

                        buttonMacroCommand.setMinimumSize(DIM_BUTTON_COMMAND);
                        buttonMacroCommand.setMaximumSize(DIM_BUTTON_COMMAND);
                        buttonMacroCommand.setPreferredSize(DIM_BUTTON_COMMAND);
                        buttonMacroCommand.setAlignmentX(Component.CENTER_ALIGNMENT);
                        buttonMacroCommand.setToolTipText(macroType.getDescription());

                        buttonMacroCommand.addActionListener(new ActionListener()
                            {
                            public void actionPerformed(final ActionEvent e)
                                {
                                if (InstrumentState.isReady(context.getObservatoryInstrument()))
                                    {
                                    if ((context.getSelectedModule() != null)
                                        && (context.getSelectedModule().size() == 1))
                                        {
                                        final boolean boolCommandReadyToExecute;

                                        // We are not interested in the specific errors in this case
                                        boolCommandReadyToExecute = (CommandProcessorContext.isPreparedCommandReadyToExecute(context,
                                                                                                                             new ArrayList<String>(10)));
                                        // Enable Command execution
                                        context.getExecuteButton().setEnabled(boolCommandReadyToExecute);

                                        // Enable Command repeat
                                        context.getRepeatButton().setEnabled(boolCommandReadyToExecute);

                                        // Show this button as the current selection
                                        CommandProcessorUtilities.selectButton(context,
                                                                               context.getCommandPanel(),
                                                                               buttonMacroCommand);

                                        // Make the Macro selection
                                        // this clears the RequiredParameters
                                        MacroManagerUtilities.selectMacro(context, macroType);

                                        // Get the new Parameters for the Macro selection
                                        if (macroType.getParameterList() != null)
                                            {
                                            context.getCommandParameters().addAll(macroType.getParameterList());
                                            }

                                        StarscriptHelper.updateStarscript(context);
                                        // This will expand any appropriate Parameters
                                        ParameterHelper.rebuildParametersPanel(context);
                                        InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument());
                                        }
                                    }
                                else
                                    {
                                    CommandProcessorUtilities.showUnavailableDialog(context);
                                    }
                                }
                            });

                        context.getCommandPanel().add(buttonMacroCommand);
                        context.getCommandPanel().add(Box.createVerticalStrut(CommandProcessorUtilities.HEIGHT_BUTTON_SEPARATOR));
                        intMacroCount++;
                        }
                    }

                if (intMacroCount == 0)
                    {
                    CommandProcessorUtilities.showEmptySet(context.getCommandPanel(),
                                                           context.getFontData(),
                                                           context.getColourData(),
                                                           ObservatoryInstrumentInterface.MACROS_NOT_FOUND);
                    }
                }
            else if ((context.getAvailableCommands() != null)
                && (!context.getAvailableCommands().isEmpty()))
                {
                final Iterator<CommandType> iterCommands;
                int intCommandCount;

                iterCommands = context.getAvailableCommands().iterator();
                intCommandCount = 0;

                while (iterCommands.hasNext())
                    {
                    final CommandType commandType;

                    commandType = iterCommands.next();

                    if (commandType != null)
                        {
                        final JButton buttonCommand;

                        buttonCommand = new JButton(commandType.getIdentifier());
                        buttonCommand.setFont(context.getFontData().getFont());
                        buttonCommand.setMinimumSize(DIM_BUTTON_COMMAND);
                        buttonCommand.setMaximumSize(DIM_BUTTON_COMMAND);
                        buttonCommand.setPreferredSize(DIM_BUTTON_COMMAND);
                        buttonCommand.setAlignmentX(Component.CENTER_ALIGNMENT);
                        buttonCommand.setToolTipText(commandType.getDescription());
                        buttonCommand.setEnabled(isCommandRunnable(context, commandType));

                        buttonCommand.setForeground(context.getColourData().getColor());

                        //---------------------------------------------------------------------
                        // User Hints

                        // See if we have any special colouring
                        // The Command must be in the CommandPool OR be marked as SendToPort
                        if (((context.getObservatoryInstrument() != null)
                            && (context.getObservatoryInstrument().getDAO() !=null)
                            && (context.getObservatoryInstrument().getDAO().getCommandPool() !=null)
                            && (context.getObservatoryInstrument().getDAO().getCommandPool().contains(commandType.getIdentifier())))
                            || (commandType.getSendToPort()))
                            {
                            if (commandType.getColour() != null)
                                {
                                // Regex should have validated the colour specification
                                buttonCommand.setBackground((new ColourDataType(commandType.getColour())).getColor());
                                }

                            // What is the Command Category?
                            if (commandType.getCategory() != null)
                                {
                                final ImageIcon icon;

                                if (CommandCategory.CAPTURE.equals(commandType.getCategory()))
                                    {
                                    icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                              ICON_CATEGORY_CAPTURE);
                                    buttonCommand.setIcon(icon);

                                    // Remind the User why this is special
                                    buttonCommand.setToolTipText(TOOLTIP_CATEGORY_CAPTURE_COMMAND + commandType.getDescription());
                                    }
                                else if (CommandCategory.IMPORT.equals(commandType.getCategory()))
                                    {
                                    icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                              ICON_CATEGORY_IMPORT);
                                    buttonCommand.setIcon(icon);

                                    // Remind the User why this is special
                                    buttonCommand.setToolTipText(TOOLTIP_CATEGORY_IMPORT_COMMAND + commandType.getDescription());
                                    }
                                else if ((CommandCategory.BUILDER.equals(commandType.getCategory()))
                                    && (RoleName.BUILDER.toString().equals(REGISTRY_MODEL.getLoggedInUser().getRole().getName())))
                                    {
                                    icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                              ICON_CATEGORY_BUILDER);
                                    buttonCommand.setIcon(icon);

                                    // Remind the User why this is special
                                    buttonCommand.setToolTipText(TOOLTIP_CATEGORY_BUILDER_COMMAND + commandType.getDescription());
                                    }
                                }
                            }
                        else
                            {
                            final ImageIcon icon;

                            // This Command does not appear in the pool in this DAO
                            icon = RegistryModelUtilities.getAtomIcon(context.getObservatoryInstrument().getHostAtom(),
                                                                      ICON_NOT_IN_POOL);
                            buttonCommand.setIcon(icon);

                            // Remind the User why this is special
                            buttonCommand.setToolTipText(TOOLTIP_COMMAND_NOT_IN_POOL + commandType.getDescription());
                            buttonCommand.setEnabled(false);
                            }

                        // End of Hints
                        //---------------------------------------------------------------------

                        buttonCommand.addActionListener(new ActionListener()
                            {
                            public void actionPerformed(final ActionEvent e)
                                {
                                if (InstrumentState.isReady(context.getObservatoryInstrument()))
                                    {
                                    if ((context.getSelectedModule() != null)
                                        && (context.getSelectedModule().size() == 1))
                                        {
                                        final boolean boolCommandReadyToExecute;

                                        // We are not interested in the specific errors in this case
                                        boolCommandReadyToExecute = (CommandProcessorContext.isPreparedCommandReadyToExecute(context,
                                                                                                                             new ArrayList<String>(10)));
                                        // Enable Command execution
                                        context.getExecuteButton().setEnabled(boolCommandReadyToExecute);

                                        // Enable Command repeat
                                        context.getRepeatButton().setEnabled(boolCommandReadyToExecute);

                                        // Show this button as the current selection
                                        CommandProcessorUtilities.selectButton(context,
                                                                               context.getCommandPanel(),
                                                                               buttonCommand);

                                        // Make the Command selection
                                        // this clears the RequiredParameters
                                        selectCommand(context, commandType);

                                        // Get the new Parameters for the Commands selection
                                        if (commandType.getParameterList() != null)
                                            {
                                            context.getCommandParameters().addAll(commandType.getParameterList());
                                            }

                                        StarscriptHelper.updateStarscript(context);
                                        // This will expand any appropriate Parameters
                                        ParameterHelper.rebuildParametersPanel(context);
                                        InstrumentHelper.notifyInstrumentChanged(context.getObservatoryInstrument());
                                        }
                                    }
                                else
                                    {
                                    CommandProcessorUtilities.showUnavailableDialog(context);
                                    }
                                }
                            });

                        // Don't add the Command button if the Role is incorrect
                        // At the moment this only affects the Builder Role
                        if ((!CommandCategory.BUILDER.equals(commandType.getCategory()))
                            || ((CommandCategory.BUILDER.equals(commandType.getCategory()))
                                && (RoleName.BUILDER.toString().equals(REGISTRY_MODEL.getLoggedInUser().getRole().getName()))))
                            {
                            context.getCommandPanel().add(buttonCommand);
                            context.getCommandPanel().add(Box.createVerticalStrut(CommandProcessorUtilities.HEIGHT_BUTTON_SEPARATOR));
                            intCommandCount++;
                            }
                        }
                    }

                if (intCommandCount == 0)
                    {
                    CommandProcessorUtilities.showEmptySet(context.getCommandPanel(),
                                                           context.getFontData(),
                                                           context.getColourData(),
                                                           ObservatoryInstrumentInterface.COMMANDS_NOT_FOUND);
                    }
                }
            else
                {
                CommandProcessorUtilities.showEmptySet(context.getCommandPanel(),
                                                       context.getFontData(),
                                                       context.getColourData(),
                                                       ObservatoryInstrumentInterface.COMMANDS_NOT_REQUIRED);
                }
            }
        else
            {
            if (CommandProcessorUtilities.hasController(context))
                {
                CommandProcessorUtilities.showEmptySet(context.getCommandPanel(),
                                                       context.getFontData(),
                                                       context.getColourData(),
                                                       CommandProcessorUtilities.NO_SELECTION);
                }
            }
        }


    /***********************************************************************************************
     * Select a Command.
     *
     * @param context The CommandProcessorContext
     * @param newcommand
     */

    private static void selectCommand(final CommandProcessorContextInterface context,
                                      final CommandType newcommand)
        {
        final String SOURCE = "CommandHelper.selectCommand() ";

        if (context != null)
            {
            if (context.getSelectedMacro() != null)
                {
                context.getSelectedMacro().clear();
                }

            if ((context.getSelectedCommand() != null)
                && (newcommand != null))
                {
                context.getSelectedCommand().clear();
                context.getSelectedCommand().add(newcommand);

                // Populate the Command Help Viewer tab if possible
                if ((context.getObservatoryInstrument() != null)
                    && (context.getObservatoryInstrument().getDAO() != null)
                    && (context.getCommandHelpViewer() != null))
                    {
                    final StringBuffer buffer;

                    buffer = new StringBuffer();

                    if ((context.getObservatoryInstrument().getDAO().getResourceBundles() != null)
                        && (!context.getObservatoryInstrument().getDAO().getResourceBundles().isEmpty()))
                        {
                        final ListIterator<ResourceBundle> iterBundles;
                        boolean boolResourceFound;

                        // The List of ResourceBundles must be searched in reverse order
                        // so that any subclass properties take precedence over the superclass
                        // Returns a ListIterator at the index, so previous() would give element at size-1
                        // ToDo Consider searching all Bundles, to resolve conflicts of overridden properties
                        iterBundles = context.getObservatoryInstrument().getDAO().getResourceBundles().listIterator(context.getObservatoryInstrument().getDAO().getResourceBundles().size());
                        boolResourceFound = false;

                        while ((!boolResourceFound)
                            && (iterBundles.hasPrevious()))
                            {
                            try
                                {
                                final ResourceBundle bundle;

                                bundle = iterBundles.previous();
                                buffer.setLength(0);

                                // Can we find the Command key in this bundle?
                                if (bundle != null)
                                    {
                                    final Enumeration<String> enumKeys;
                                    final String strHelp;

                                    System.out.println("\nSearching bundle for resource [command=" + newcommand.getIdentifier() + "]");

                                    enumKeys = bundle.getKeys();

                                    while (enumKeys.hasMoreElements())
                                        {
                                        final String strKey;
                                        final String strFlag;

                                        strKey = enumKeys.nextElement();

                                        if (newcommand.getIdentifier().equals(strKey))
                                            {
                                            strFlag = "*** ";
                                            }
                                        else
                                            {
                                            strFlag = "    ";
                                            }

                                        System.out.println(strFlag + strKey);
                                        }

                                    strHelp = bundle.getString(newcommand.getIdentifier());

                                    buffer.append(HTML_PREFIX_FONT_BLUE);
                                    buffer.append(strHelp);
                                    buffer.append(StarscriptHelper.buildStarscriptPrototypeAsHTML(context.getObservatoryInstrument().getDAO().getCommandPool(),
                                                                                                  newcommand));
                                    buffer.append(HTML_SUFFIX_FONT);

                                    context.getCommandHelpViewer().setHTMLText(buffer.toString());
                                    boolResourceFound = true;
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Resource Bundle unexpectedly NULL "
                                                    + "[command=" + newcommand.getIdentifier() + TERMINATOR);
                                    }
                                }

                            catch (final NoSuchElementException exception)
                                {
                                // Do nothing if no valid help exists
                                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR + " NoSuchElementException");
                                }

                            catch (final NullPointerException exception)
                                {
                                // Do nothing if no valid help exists
                                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR + " NullPointerException");
                                }

                            catch (final MissingResourceException exception)
                                {
                                // Do nothing if no valid help exists
                                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR + " MissingResourceException");
                                }

                            catch (final ClassCastException exception)
                                {
                                // Do nothing if no valid help exists
                                LOGGER.error(SOURCE + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR + " ClassCastException");
                                }
                            }

                        if (!boolResourceFound)
                            {
                            // ResourceBundles not implemented yet!
                            buffer.append(HTML_PREFIX_FONT_BLUE);
                            buffer.append(MSG_HELP_UNDER_DEVELOPMENT);
                            buffer.append(newcommand.getIdentifier());
                            buffer.append(StarscriptHelper.buildStarscriptPrototypeAsHTML(context.getObservatoryInstrument().getDAO().getCommandPool(),
                                                                                          newcommand));
                            buffer.append("<br>Help not found after searching all Instrument resources");
                            buffer.append(HTML_SUFFIX_FONT);

                            context.getCommandHelpViewer().setHTMLText(buffer.toString());
                            }
                        }
                    else
                        {
                        // ResourceBundles not implemented yet!
                        buffer.append(HTML_PREFIX_FONT_BLUE);
                        buffer.append(MSG_HELP_UNDER_DEVELOPMENT);
                        buffer.append(newcommand.getIdentifier());
                        buffer.append(StarscriptHelper.buildStarscriptPrototypeAsHTML(context.getObservatoryInstrument().getDAO().getCommandPool(),
                                                                                      newcommand));
                        buffer.append("<br>This Instrument does not have any associated Help resources");
                        buffer.append(HTML_SUFFIX_FONT);

                        context.getCommandHelpViewer().setHTMLText(buffer.toString());
                        }
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

            if (context.getParameterHelpViewer() != null)
                {
                context.getParameterHelpViewer().setHTMLText("Under Development - called from selectCommand()");
                }
            }
        }


    /***********************************************************************************************
     * Indicate if a Command is runnable, to control the button.
     *
     * @param context
     * @param command
     *
     * @return boolean
     */

    private static boolean isCommandRunnable(final CommandProcessorContextInterface context,
                                             final CommandType command)
        {
        final boolean boolRemoteCommand;
        final boolean boolNoDAO;
        final boolean boolNoPort;
        final boolean boolPortNotOpen;

        // Does this Command *require* a connection with the World?
        // If there are child commands then for now assume that's wrong too,
        // because they *probably* have SendToPort
        // The problem is that e.g. SteppedData appears for both parent and child Commands,
        // whereas we really only want to know about the parents
        // Ideally get the CommandCodes, and find the Commands by Xpath to be sure
        boolRemoteCommand = ((command.getSendToPort())
                                || ((command.getSteppedDataCommandList() != null)
                                    && (!command.getSteppedDataCommandList().isEmpty()))
                                || (command.getIteratedDataCommand() != null)
                                || (command.getBlockedDataCommand() != null));

        // If there's no DAO but we are expected to SendToPort
        // then there's something wrong...
        boolNoDAO = ((context != null)
                        && (context.getObservatoryInstrument() != null)
                        && (context.getObservatoryInstrument().getDAO() == null)
                        && (boolRemoteCommand));

        // If there's a DAO but no Port and we are expected to SendToPort
        // then there's something wrong...
        boolNoPort = ((context != null)
                        && (context.getObservatoryInstrument() != null)
                        && (context.getObservatoryInstrument().getDAO() != null)
                        && (context.getObservatoryInstrument().getDAO().getPort() == null)
                        && (boolRemoteCommand));

        // If there's a DAO and a Port which isn't open, we can't SendToPort
        boolPortNotOpen = ((context != null)
                            && (context.getObservatoryInstrument() != null)
                            && (context.getObservatoryInstrument().getDAO() != null)
                            && (context.getObservatoryInstrument().getDAO().getPort() != null)
                            && (!context.getObservatoryInstrument().getDAO().getPort().isPortOpen())
                            && (boolRemoteCommand));

        // The Command is runnable if there's no faults...
        return (!(boolNoDAO || boolNoPort || boolPortNotOpen));
        }
    }
