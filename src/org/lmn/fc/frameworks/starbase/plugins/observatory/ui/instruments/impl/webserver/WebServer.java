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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrument;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;


/***********************************************************************************************
 * An WebServer.
 */

public final class WebServer extends AbstractObservatoryInstrument
                             implements ObservatoryInstrumentInterface
    {
    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_HOST_NAME = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_INDEX_FILE = "index.html";
    public static final String DEFAULT_CONFIGURATION_FILE = "etc/jetty.xml";
    public static final String DEFAULT_RESOURCE_BASE = "webroot";

    private Server server;

    // Configurable Resources
    private int intPort;
    private String strResourceBase;


    /***********************************************************************************************
     * Construct a WebServer.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public WebServer(final Instrument instrument,
                     final AtomPlugin plugin,
                     final ObservatoryUIInterface hostui,
                     final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);

        this.intPort = DEFAULT_PORT;
        this.strResourceBase = DEFAULT_RESOURCE_BASE;
        }


    /***********************************************************************************************
     * Initialise the WebServer.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;
        final ResourceHandler resourceHandler;
        final HandlerList handlers;

        // Read the Resources for the WebServer
        readResources();

        super.initialise();

        // Create and initialise the ControlPanel
        controlPanel = new WebServerControlPanel(this,
                                               getInstrument(),
                                               getHostUI(),
                                               (TaskPlugin)getHostAtom().getRootTask(),
                                               getFontData(),
                                               getColourData(),
                                               getResourceKey());
        setControlPanel(controlPanel,
                        getInstrument().getName());
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        // Create an InstrumentPanel and initialise it
        instrumentPanel = new WebServerInstrumentPanel(this,
                                                       getInstrument(),
                                                       getHostUI(),
                                                       (TaskPlugin)getHostAtom().getRootTask(),
                                                       getFontData(),
                                                       getColourData(),
                                                       getResourceKey());
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();

        // Initialise the Jetty Server, but don't start it
        server = new Server(intPort);

        resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(strResourceBase);

        handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
        server.setHandler(handlers);
        }


    /***********************************************************************************************
     * Stop this ObservatoryInstrument.
     */

    public synchronized boolean stop()
        {
        final Server webServer;
        boolean boolSuccess;

        boolSuccess = super.stop();

        webServer = getJettyServer();

        if ((webServer != null)
            && (!webServer.isStopped()))
            {
            try
                {
                webServer.stop();
                }

            catch (Exception exception)
                {
                boolSuccess= false;
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "WebServer.stop() catching a plain Exception!");
                exception.printStackTrace();
                }

            // The Server state has changed, we may need to refresh a browser etc.
            InstrumentHelper.notifyInstrumentChanged(this);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Get the Jetty server.
     * This is not in an interface.
     *
     * @return Server
     */

    public Server getJettyServer()
        {
        return (this.server);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     *  Read all the Resources required by the WebServer.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServer [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        intPort = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT);
        strResourceBase = REGISTRY.getStringProperty(getResourceKey() + KEY_RESOURCE_BASE);
        }
    }
