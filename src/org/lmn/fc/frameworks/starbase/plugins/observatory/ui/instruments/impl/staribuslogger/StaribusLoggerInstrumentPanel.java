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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.staribuslogger;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.DigitalPanelMeterUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;


/***************************************************************************************************
 * The StaribusLoggerInstrumentPanel.
 */

public final class StaribusLoggerInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER = "staribus-logger-header.png";
    private static final String INSTRUMENT_HELP = "StaribusLoggerHelp.html";

    // Max and min values, in milliVolts
    private static final double CONTROLLER_CHART_MIN_VALUE = 0.0;
    private static final double CONTROLLER_CHART_MAX_VALUE = 2500.0;


    /***********************************************************************************************
     * Construct a StaribusLoggerInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param channelcount
     */

    public StaribusLoggerInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                         final Instrument instrumentxml,
                                         final ObservatoryUIInterface hostui,
                                         final TaskPlugin task,
                                         final FontInterface font,
                                         final ColourInterface colour,
                                         final String resourcekey,
                                         final int channelcount)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              channelcount);
        }


    /***********************************************************************************************
     * Initialise the StaribusLoggerInstrumentPanel.
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

        // Create the StaribusLoggerInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     * Notify interested tabs of the change.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        final String SOURCE = "StaribusLoggerInstrumentPanel.instrumentChanged() ";

        super.instrumentChanged(event);

        if ((getMetersTab() != null)
            && (getMetersTab() instanceof DigitalPanelMeterUIComponent))
            {
            LOGGER.debugIndicators(SOURCE + "--> updateDigitalPanelMeters()");

            InstrumentUIHelper.updateDigitalPanelMeters(getHostInstrument(),
                                                        getHostInstrument().getControlPanel(),
                                                        (DigitalPanelMeterUIComponent)getMetersTab());
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the StaribusLoggerInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addLogLinChartTab(this,
                                                    TAB_CHART,
                                                    null,
                                                    DataUpdateType.DECIMATE,
                                                    CONTROLLER_CHART_MIN_VALUE,
                                                    CONTROLLER_CHART_MAX_VALUE,
                                                    0.0,
                                                    10000.0,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetersTab(this,
                                               TAB_METERS,
                                               REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addProcessedDataTab(this,
                                                      TAB_PROCESSED_DATA,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addRawDataTab(this,
                                                TAB_RAW_DATA,
                                                REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addInstrumentLogTab(this,
                                                      TAB_INSTRUMENT_LOG,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_EVENT_LOG,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addConfigurationTab(this,
                                                      TAB_CONFIGURATION,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addInstrumentNewsreaderTab(this,
                                                             TAB_NEWS,
                                                             REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addCommandLexiconTab(this,
                                                       TAB_COMMAND_LEXICON,
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
    }
