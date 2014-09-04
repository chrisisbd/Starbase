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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.starinettester;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.HTMLUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;


/***************************************************************************************************
 * The StarinetTesterInstrumentPanel.
 */

public final class StarinetTesterInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String MSG_RESPONSE = "The Response will appear here";
    private static final String MSG_RESPONSE_NONE = "No Response was returned";
    private static final String MSG_RESPONSE_INVALID = "Invalid Response";

    private static final String ICON_HEADER = "starinet-tester-header.png";

    private static final String TAB_RESPONSE = "Response";
    private static final String INSTRUMENT_HELP = "StarinetTesterHelp.html";

    private UIComponentPlugin tabResponse;


    /***********************************************************************************************
     * Construct a StarinetTesterInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public StarinetTesterInstrumentPanel(final ObservatoryInstrumentInterface instrument,
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
     * Initialise the StarinetTesterInstrumentPanel.
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

        // Create the StarinetTesterInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the StarinetTesterInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        tabResponse = new HTMLUIComponent(MSG_RESPONSE);
        getTabbedPane().addTab(TAB_RESPONSE,
                               (Component) getResponseTab());

        InstrumentPanelTabFactory.addRawDataTab(this,
                                                TAB_RAW_DATA,
                                                REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
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

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the Response.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getResponseTab()
        {
        return (this.tabResponse);
        }


    /***********************************************************************************************
     * Set the Response.
     *
     * @param plugin
     */

    private void setResponse(final UIComponentPlugin plugin)
        {
        this.tabResponse = plugin;
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        final int CHANNEL_COUNT = 1;

        super.instrumentChanged(event);

        if (event != null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "StarinetTesterInstrumentPanel.instrumentChanged() --> " + event.getNextState().getName());

            // Refresh the Response page if possible
            if ((getResponseTab() != null)
                && (getResponseTab() instanceof HTMLUIComponent))
                {
                if ((getWrappedData() != null)
                    && (getWrappedData().getRawData() != null)
                    && (getWrappedData().getRawData().size() > 0)
                    && (getWrappedData().getRawData().get(getWrappedData().getRawData().size()-1) instanceof Vector))
                    {
                    final Vector vecResponseBody;

                    // Get the most recent Response, i.e index (size-1)
                    vecResponseBody = (Vector)getWrappedData().getRawData().get(getWrappedData().getRawData().size()-1);

                    // There must be one Calendar and ChannelCount samples in the Vector...
                    if ((vecResponseBody != null)
                        && (vecResponseBody.size() == CHANNEL_COUNT+1)
                        && (vecResponseBody.get(CHANNEL_COUNT) instanceof String))
                        {
                        ((HTMLUIComponent)getResponseTab()).setHTMLText((String)vecResponseBody.get(CHANNEL_COUNT));
                        }
                    else
                        {
                        ((HTMLUIComponent)getResponseTab()).setHTMLText(MSG_RESPONSE_INVALID);
                        }
                    }
                else
                    {
                    ((HTMLUIComponent)getResponseTab()).setHTMLText(MSG_RESPONSE_NONE);
                    }
                }
            }
        }
    }
