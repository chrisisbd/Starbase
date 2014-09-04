// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.terminal;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TerminalConsoleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.TerminalConsoleUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The TerminalEmulatorInstrumentPanel.
 */

public final class TerminalEmulatorInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER_TERMINAL = "terminal-emulator-header.png";

    private static final String TAB_CONSOLE = "Console";

    private static final String INSTRUMENT_HELP = "TerminalEmulatorHelp.html";

    private UIComponentPlugin tabConsole;


    /***********************************************************************************************
     * Construct a TerminalEmulatorInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public TerminalEmulatorInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                           final Instrument instrumentxml,
                                           final ObservatoryUIInterface hostui,
                                           final TaskPlugin task,
                                           final FontInterface font,
                                           final ColourInterface colour,
                                           final String resourcekey)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);
        }


    /***********************************************************************************************
     * Start the ObservatoryInstrumentUIComponent.
     */

    public boolean start()
        {
        boolean boolSuccess;

        // This happens **before** runUI()
        boolSuccess = super.start();

        // Run the Terminal regardless of its tab selection state
        if ((boolSuccess)
            && (getConsoleTab() != null))
            {
            boolSuccess = ((TerminalConsoleUIComponentInterface)getConsoleTab()).startSession();
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Stop the ObservatoryInstrumentUIComponent.
     */

    public boolean stop()
        {
        boolean boolSuccess;

        boolSuccess = super.stop();

        // Stop the Terminal regardless of its tab selection state
        if ((boolSuccess)
            && (getConsoleTab() != null))
            {
            boolSuccess = ((TerminalConsoleUIComponentInterface)getConsoleTab()).stopSession();
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Initialise the TerminalEmulatorInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER_TERMINAL,
                                                          getFontData(),
                                                          getColourData());

        // Create the TerminalEmulatorInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* DAO                                                                                        */
    /* There is no DAO                                                                            */
    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the TerminalEmulatorInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        tabConsole = new TerminalConsoleUIComponent(getHostTask(),
                                                    getHostInstrument(),
                                                    REGISTRY.getFrameworkResourceKey(),
                                                    getResourceKey());
        // Auto-scroll to the last entry
        ((ReportTablePlugin)getConsoleTab()).setScrollToRow(-1);
        getTabbedPane().addTab(TAB_CONSOLE,
                               (Component) getConsoleTab());

        InstrumentPanelTabFactory.addConfigurationTab(this,
                                                      TAB_CONFIGURATION,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addXmlTab(this,
                                            TAB_XML,
                                            REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addHelpTab(this,
                                             TAB_HELP,
                                             FileUtilities.html,
                                             INSTRUMENT_HELP,
                                             REGISTRY_MODEL.getLoggedInUser());

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the host UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the Console.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getConsoleTab()
        {
        return (this.tabConsole);
        }
    }
