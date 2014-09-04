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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.dataprocessor;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.PatchPanelUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The DataProcessorInstrumentPanel.
 */

public final class DataProcessorInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER_PROCESSOR = "processor-header.png";

    private static final String TAB_PATCHPANEL = "PatchPanel";

    private static final String INSTRUMENT_HELP = "DataProcessorHelp.html";

    private UIComponentPlugin tabPatchPanel;


    /***********************************************************************************************
     * Construct a DataProcessorInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param ui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public DataProcessorInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                        final Instrument instrumentxml,
                                        final ObservatoryUIInterface ui,
                                        final TaskPlugin task,
                                        final FontInterface font,
                                        final ColourInterface colour,
                                        final String resourcekey)
        {
        super(instrument,
              instrumentxml,
              ui,
              task,
              font,
              colour,
              resourcekey, 1);
        }


    /***********************************************************************************************
     * Initialise the DataProcessorInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER_PROCESSOR,
                                                          getFontData(),
                                                          getColourData());

        // Create the DataProcessorInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the DataProcessorInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        tabPatchPanel = new PatchPanelUIComponent(getHostInstrument(),
                                                  getInstrument(),
                                                  getObservatoryUI(),
                                                  getHostTask(),
                                                  getFontData(),
                                                  getColourData(),
                                                  getResourceKey());
        getTabbedPane().addTab(TAB_PATCHPANEL,
                               (Component) getPatchPanelTab());

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

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the Controller.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getPatchPanelTab()
        {
        return (this.tabPatchPanel);
        }
    }
