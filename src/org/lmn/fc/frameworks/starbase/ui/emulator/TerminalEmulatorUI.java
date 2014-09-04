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

package org.lmn.fc.frameworks.starbase.ui.emulator;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.terminal.SerialPortData;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.panels.HTMLPanel;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The TerminalEmulatorInstrumentPanel.
 */

public final class TerminalEmulatorUI extends UIComponent
                                      implements UIComponentPlugin
    {
    private static final String TAB_CONSOLE = "Console";
    private static final String TAB_CONFIGURATION = "Configuration";
    private static final String TAB_HELP = "Help";
    private static final String HELP = "TerminalEmulatorHelp.html";

    private TaskPlugin pluginTask;
    private FontInterface pluginFont;
    private ColourInterface pluginColour;
    private ColourInterface colourTable;
    private ColourInterface colourCanvas;
    private SerialPortData serialportData;

    private JTabbedPane tabbedPane;
    private UIComponentPlugin terminalEmulatorConsole;
    private UIComponentPlugin terminalEmulatorConfiguration;
    private UIComponentPlugin terminalEmulatorHelp;


    /***********************************************************************************************
     * Construct a TerminalEmulatorInstrumentPanel.
     *
     * @param task
     * @param font
     * @param text
     * @param table
     * @param canvas
     * @param port
     */

    public TerminalEmulatorUI(final TaskPlugin task,
                              final FontInterface font,
                              final ColourInterface text,
                              final ColourInterface table,
                              final ColourInterface canvas,
                              final SerialPortData port)
        {
        super();

        if ((task == null)
            || (!task.validatePlugin())
            || (font == null)
            || (text == null)
            || (table == null)
            || (canvas == null)
            || (port == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.pluginTask = task;
        this.pluginFont = font;
        this.pluginColour = text;
        this.colourTable = table;
        this.colourCanvas = canvas;
        this.serialportData = port;
        }


    /***********************************************************************************************
     * Initialise the TerminalEmulatorInstrumentPanel.
     */

    public final void initialiseUI()
        {
        // Create the TerminalEmulatorInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // There is nothing to do to run the TabbedPane itself!
        // Set the selected tab to run each time the Task is run
        UIComponentHelper.runSelectedTabComponent(getHostTask(), this, getTabbedPane());
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        // There is nothing to do to stop the TabbedPane!
        // Stop all UIComponents on the tabs
        UIComponentHelper.stopAllTabComponents(getTabbedPane());
        }


    /***********************************************************************************************
     * Dispose of the TerminalEmulatorInstrumentPanel.
     */

    public final void disposeUI()
        {
        if (getTabbedPane() != null)
            {
            // Reduce resources as far as possible
            UIComponentHelper.disposeAllTabComponents(getTabbedPane());
            getTabbedPane().removeAll();
            setTabbedPane(null);
            removeAll();
            }
        }


    /***********************************************************************************************
     * Start the TerminalEmulator session.
     */

    public void startSession()
        {
        // Just delegate to the console
        if (getTerminalEmulatorConsole() != null)
            {
            ((TerminalEmulatorInterface)getTerminalEmulatorConsole()).startSession();
            }
        }


    /***********************************************************************************************
     * Stop the TerminalEmulator session.
     */

    public void stopSession()
        {
        // Just delegate to the console
        if (getTerminalEmulatorConsole() != null)
            {
            ((TerminalEmulatorInterface)getTerminalEmulatorConsole()).stopSession();
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the TerminalEmulatorInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        removeAll();

        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        getTabbedPane().setFont(pluginFont.getFont());
        getTabbedPane().setForeground(pluginColour.getColor());

        terminalEmulatorConsole = new TerminalEmulatorConsole(getHostTask(),
                                                              pluginFont,
                                                              pluginColour,
                                                              colourTable,
                                                              colourCanvas,
                                                              serialportData,
                                                              REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab(TAB_CONSOLE,
                               (Component)getTerminalEmulatorConsole());

        terminalEmulatorConfiguration = new TerminalEmulatorConfiguration(getHostTask(),
                                                                          TAB_CONFIGURATION,
                                                                          REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab(TAB_CONFIGURATION,
                               (Component)getTerminalEmulatorConfiguration());

        terminalEmulatorHelp = new HTMLPanel(RegistryModelUtilities.getHelpURL(getHostTask().getParentAtom(),
                                                                               HELP));
        getTabbedPane().addTab(TAB_HELP,
                               (Component)getTerminalEmulatorHelp());

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the TerminalEmulatorInstrumentPanel UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    private TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the JTabbedPane.
     *
     * @return JTabbedPane
     */

    private JTabbedPane getTabbedPane()
        {
        return (this.tabbedPane);
        }


    /***********************************************************************************************
     * Set the JTabbedPane.
     *
     * @param tabbedpane
     */

    private void setTabbedPane(final JTabbedPane tabbedpane)
        {
        this.tabbedPane = tabbedpane;
        }


    /***********************************************************************************************
     * Get the Gps Log.
     *
      * @return UIComponentPlugin
     */

    private UIComponentPlugin getTerminalEmulatorConsole()
        {
        return (this.terminalEmulatorConsole);
        }


    /***********************************************************************************************
     * Get the Gps Configuration.
     *
      * @return UIComponentPlugin
     */

    private UIComponentPlugin getTerminalEmulatorConfiguration()
        {
        return (this.terminalEmulatorConfiguration);
        }


    /***********************************************************************************************
     * Get the Gps Help.
     *
      * @return UIComponentPlugin
     */

    private UIComponentPlugin getTerminalEmulatorHelp()
        {
        return (this.terminalEmulatorHelp);
        }
    }
