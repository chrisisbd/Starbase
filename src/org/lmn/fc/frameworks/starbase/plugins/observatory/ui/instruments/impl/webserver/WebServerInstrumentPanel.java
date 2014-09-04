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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.webserver;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration.XmlUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.panels.HTMLPanel;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;


/***************************************************************************************************
 * The WebServerInstrumentPanel.
 */

public final class WebServerInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER = "webserver-header.png";

    private static final String TAB_WEBROOT = "Web Root";
    private static final String TAB_XML_SERVER = "Server XML";

    private static final String INSTRUMENT_HELP = "WebServerHelp.html";

    private UIComponentPlugin tabWebRoot;
    private UIComponentPlugin tabXMLServer;

    // Configurable Resources
    private String strProtocol;
    private String strHostName;
    private int intPort;
    private String strIndexFile;
    private String strConfigurationFile;


    /***********************************************************************************************
     * Construct a WebServerInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param oui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public WebServerInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                    final Instrument instrumentxml,
                                    final ObservatoryUIInterface oui,
                                    final TaskPlugin task,
                                    final FontInterface font,
                                    final ColourInterface colour,
                                    final String resourcekey)
        {
        super(instrument,
              instrumentxml,
              oui,
              task,
              font,
              colour,
              resourcekey, 1);

        this.strProtocol = WebServer.DEFAULT_PROTOCOL;
        this.strHostName = WebServer.DEFAULT_HOST_NAME;
        this.intPort = WebServer.DEFAULT_PORT;
        this.strIndexFile = WebServer.DEFAULT_INDEX_FILE;
        this.strConfigurationFile = WebServer.DEFAULT_CONFIGURATION_FILE;
        }


    /***********************************************************************************************
     * Initialise the WebServerInstrumentPanel.
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

        // Create the WebServerInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the WebServerInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());
        try
            {
            tabWebRoot = new HTMLPanel(new URL(strProtocol,
                                               strHostName,
                                               intPort,
                                               strIndexFile));
            getTabbedPane().addTab(TAB_WEBROOT,
                                   (Component) getWebRootTab());
            }

        catch (MalformedURLException e)
            {
            // Just ignore this tab if the URL is incorrect
            }

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
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

        tabXMLServer = new XmlUIComponent(getHostInstrument(),
                                          getServerXML(),
                                          REGISTRY.getFrameworkResourceKey())
            {
            /***********************************************************************************************
             * Reload the XML to be displayed.
             */

            public void updateXml()
                {
                // Ignore the super() and assume the **Server** XML is unchanged
                }
            };

        getTabbedPane().addTab(TAB_XML_SERVER,
                               (Component) getXMLServerTab());

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
     * Get the XML.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getXMLServerTab()
        {
        return (this.tabXMLServer);
        }


    /***********************************************************************************************
     * Get the WebRoot.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getWebRootTab()
        {
        return (this.tabWebRoot);
        }


    /***********************************************************************************************
     * Set the WebRoot.
     *
     * @param plugin
     */

    private void setWebRoot(final UIComponentPlugin plugin)
        {
        this.tabWebRoot = plugin;
        }


    /***********************************************************************************************
     * Get the Server XML configuration to display on the tab.
     *
     * @return String
     */

    private String getServerXML()
        {
        final byte[] bytesXML;

        // Read the latest from the configuration file
        bytesXML = FileUtilities.readFileAsByteArray(InstallationFolder.getTerminatedUserDir()
                                                        + strConfigurationFile);
        if (bytesXML != null)
            {
            return (new String(bytesXML));
            }
        else
            {
            return (MSG_UNABLE_TO_READ + SPACE + strConfigurationFile);
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     *  Read all the Resources required by the WebServerInstrumentPanel.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServerInstrumentPanel [ResourceKey=" + getResourceKey() + "]");

        strProtocol = REGISTRY.getStringProperty(getResourceKey() + KEY_PROTOCOL);
        strHostName = REGISTRY.getStringProperty(getResourceKey() + KEY_HOST_NAME);
        intPort = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT);
        strIndexFile = REGISTRY.getStringProperty(getResourceKey() + KEY_INDEX_FILE);
        strConfigurationFile = REGISTRY.getStringProperty(getResourceKey() + KEY_CONFIGURATION_FILE);
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        super.instrumentChanged(event);

        if (event != null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "WebServerInstrumentPanel.instrumentChanged() --> " + event.getNextState().getName());

            // Refresh the WebRoot index page
            try
                {
                readResources();

                if ((getWebRootTab() != null)
                    && (getWebRootTab() instanceof HTMLPanel))
                    {
                    ((HTMLPanel)getWebRootTab()).gotoURL(new URL(strProtocol,
                                                                 strHostName,
                                                                 intPort,
                                                                 strIndexFile));
                    }
                }

            catch (MalformedURLException e)
                {
                // Just ignore this change if the URL is incorrect
                }
            }
        }
    }
