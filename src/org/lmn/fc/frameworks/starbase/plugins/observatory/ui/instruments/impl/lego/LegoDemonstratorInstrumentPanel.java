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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.lego;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ControlPanelInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;


/***************************************************************************************************
 * The LegoDemonstratorInstrumentPanel.
 */

public final class LegoDemonstratorInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER = "lego-demonstrator-header.png";

    private static final String INSTRUMENT_HELP = "LegoDemonstratorHelp.html";
    private static final String DEFAULT_PUBLICATION = "LegoDemonstratorPresentation.pdf";

    private static final long serialVersionUID = 9075831275770380460L;

    private final double dblChartMin;
    private final double dblChartMax;


    /***********************************************************************************************
     * Construct a LegoDemonstratorInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param chartmin
     * @param chartmax
     */

    public LegoDemonstratorInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                           final Instrument instrumentxml,
                                           final ObservatoryUIInterface hostui,
                                           final TaskPlugin task,
                                           final FontInterface font,
                                           final ColourInterface colour,
                                           final String resourcekey,
                                           final double chartmin,
                                           final double chartmax)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              ControlPanelInterface.INDICATOR_COUNT_1);

        dblChartMin = chartmin;
        dblChartMax = chartmax;
        }


    /***********************************************************************************************
     * Initialise the LegoDemonstratorInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER,
                                                          getFontData(),
                                                          getColourData());

        // Create the LegoDemonstratorInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the LegoDemonstratorInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        final UserPlugin user;

        user = REGISTRY_MODEL.getLoggedInUser();

        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 user);

        InstrumentPanelTabFactory.addLogLinChartTab(this,
                                                    TAB_CHART,
                                                    null,
                                                    DataUpdateType.DECIMATE,
                                                    dblChartMin,
                                                    dblChartMax,
                                                    dblChartMin,
                                                    dblChartMax,
                                                    user);

        InstrumentPanelTabFactory.addMetersTab(this,
                                               TAB_METERS,
                                               user);

        InstrumentPanelTabFactory.addProcessedDataTab(this,
                                                      TAB_PROCESSED_DATA,
                                                      user);

        InstrumentPanelTabFactory.addRawDataTab(this,
                                                TAB_RAW_DATA,
                                                user);

        InstrumentPanelTabFactory.addSuperposedDataAnalyserTab(this,
                                                               TAB_SUPERPOSED_DATA_ANALYSER,
                                                               user);

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         user);

        InstrumentPanelTabFactory.addStarMapTab(this,
                                                TAB_STAR_MAP,
                                                user);

        InstrumentPanelTabFactory.addEphemeridesTab(this,
                                                    TAB_EPHEMERIDES,
                                                    user);

        InstrumentPanelTabFactory.addRegionalMapTab(this,
                                                    TAB_REGIONAL_MAP,
                                                    user);

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_EVENT_LOG,
                                                 user);

        InstrumentPanelTabFactory.addConfigurationTab(this,
                                                      TAB_CONFIGURATION,
                                                      user);

        InstrumentPanelTabFactory.addInstrumentNewsreaderTab(this,
                                                             TAB_NEWS,
                                                             user);

        InstrumentPanelTabFactory.addCommandLexiconTab(this,
                                                       TAB_COMMAND_LEXICON,
                                                       user);

        InstrumentPanelTabFactory.addXmlTab(this,
                                            TAB_XML,
                                            user);

        InstrumentPanelTabFactory.addHelpTab(this,
                                             TAB_HELP,
                                             FileUtilities.html,
                                             INSTRUMENT_HELP,
                                             user);

        InstrumentPanelTabFactory.addPublisherTab(this,
                                                  TAB_PRESENTER,
                                                  DEFAULT_PUBLICATION,
                                                  user);

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Allow export of the RegionalMap
        // The RegionalMap is not created until the MapUIComponent is initialised,
        // which happens in initialiseAllTabComponents()
        // Normally the ExportableComponent would be one of the tabs,
        // but in this case it is held within a tab component, so must be retrieved differently
        setExportableComponent(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_REGIONAL_MAP,
                               ((MapUIComponentPlugin) getRegionalMapTab()).getExportableComponent());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }
    }
