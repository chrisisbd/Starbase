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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.news.GetMantisIssues;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.news.GetSubversionLog;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.ui.SubscriptionsUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The CommunicatorInstrumentPanel.
 */

public final class CommunicatorInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER = "communicator-header.png";

    private static final String TAB_SUBSCRIPTIONS = "Subscriptions";
    private static final String INSTRUMENT_HELP = "CommunicatorHelp.html";
    private static final String JENKINS_RSS_NAME = "UKRAA-Starbase-Jenkins";
    private static final String JENKINS_RSS_URL = "http://jenkins.ukraa.com:8080/jenkins/rssAll";
    private static final String DILBERT_RSS_NAME = "Dilbert Daily Strip";
    private static final String DILBERT_RSS_URL = "http://feed.dilbert.com/dilbert/daily_strip?format=xml";

    private UIComponentPlugin tabSubscriptions;
    private UIComponentPlugin tabDilbert;


    /***********************************************************************************************
     * Construct a CommunicatorInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public CommunicatorInstrumentPanel(final ObservatoryInstrumentInterface instrument,
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
              resourcekey,
              1);
        }


    /***********************************************************************************************
     * Initialise the CommunicatorInstrumentPanel.
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

        // Create the CommunicatorInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the CommunicatorInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        final String SOURCE = "CommunicatorInstrumentPanel.createAndInitialiseTabs() ";

        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addCommunicatorNewsreaderTab(this,
                                                               TAB_NEWSFEEDS,
                                                               REGISTRY_MODEL.getLoggedInUser());

        tabSubscriptions = new SubscriptionsUIComponent(getHostTask(),
                                                        getHostInstrument(),
                                                        REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab(TAB_SUBSCRIPTIONS,
                               (Component) getSubscriptionsTab());

        InstrumentPanelTabFactory.addMantisTab(this,
                                               TAB_MANTIS,
                                               GetMantisIssues.RSS_NAME,
                                               GetMantisIssues.RSS_URL,
                                               REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addSubversionTab(this,
                                                   TAB_SUBVERSION,
                                                   GetSubversionLog.RSS_NAME,
                                                   GetSubversionLog.RSS_URL,
                                                   REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addJenkinsTab(this,
                                                TAB_JENKINS,
                                                JENKINS_RSS_NAME,
                                                JENKINS_RSS_URL,
                                                REGISTRY_MODEL.getLoggedInUser());

        // The Dilbert RSS feed has been broken...
//        tabDilbert = InstrumentPanelTabFactory.createRssUIComponent(this,
//                                                                    DILBERT_RSS_NAME,
//                                                                    DILBERT_RSS_URL,
//                                                                    UIComponentPlugin.FILENAME_ICON_DILBERT,
//                                                                    true);
//        getTabbedPane().addTab(TAB_DILBERT,
//                               (Component) getDilbertTab());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_EVENT_LOG,
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

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the Subscriptions Tab.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getSubscriptionsTab()
        {
        return (this.tabSubscriptions);
        }


    /***********************************************************************************************
     * Get the Dilbert Tab.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getDilbertTab()
        {
        return (this.tabDilbert);
        }
    }
