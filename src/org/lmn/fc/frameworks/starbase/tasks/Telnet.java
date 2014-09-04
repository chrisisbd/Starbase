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

package org.lmn.fc.frameworks.starbase.tasks;

import org.lmn.fc.frameworks.starbase.ui.telnet.TelnetUI;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.UIComponentPlugin;


/***************************************************************************************************
 * Telnet.
 */

public final class Telnet extends TaskData
    {
    // String Resources
    private static final String STATUS_TELNET = "Telnet for ";

    // Resources
    private String strHost;
    private int intPort;
    private boolean boolEnableSSH;
    private String strID;
    private int intBufferSize;
    private int intTimeout;
    private String strCommand;
    private FontInterface fontDisplay;
    private ColourInterface colourForeground;
    private ColourInterface colourBackground;
    private ColourInterface colourCursorForeground;
    private ColourInterface colourCursorBackground;


    /***********************************************************************************************
     * Construct a Telnet.
     */

    private Telnet()
        {
        super(-2603083911449242570L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise Telnet.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources for Telnet
        // Get the latest Resources
        readResources();

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        // Create a TelnetUI and initialise it with the Resources
        final UIComponentPlugin telnet = new TelnetUI(this,
                                                      strHost,
                                                      intPort,
                                                      boolEnableSSH,
                                                      strID,
                                                      intBufferSize,
                                                      intTimeout,
                                                      strCommand,
                                                      fontDisplay,
                                                      colourForeground,
                                                      colourBackground,
                                                      colourCursorForeground,
                                                      colourCursorBackground);
        setUIComponent(telnet);
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Run Telnet.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Park Telnet in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown Telnet after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Clear the Telnet UI
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            // Ensure that there is something to see as soon as possible...
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_TELNET + SPACE + REGISTRY.getFramework().getName());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            // Reduce resources as far as possible
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by Telnet.
     */

    public final void readResources()
        {
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_TELNET_ENABLE_DEBUG));

        strHost = REGISTRY.getStringProperty(getResourceKey() + KEY_TELNET_CONNECTION_SOCKET_HOST);
        intPort = REGISTRY.getIntegerProperty(getResourceKey() + KEY_TELNET_CONNECTION_SOCKET_PORT);
        boolEnableSSH = REGISTRY.getBooleanProperty(getResourceKey() + KEY_TELNET_ENABLE_SSH);
        strID = REGISTRY.getStringProperty(getResourceKey() + KEY_TELNET_TERMINAL_ID);
        intBufferSize = REGISTRY.getIntegerProperty(getResourceKey() + KEY_TELNET_TERMINAL_BUFFER_SIZE);
        intTimeout = REGISTRY.getIntegerProperty(getResourceKey() + KEY_TELNET_CONNECTION_TIMEOUT_SECONDS);
        strCommand = REGISTRY.getStringProperty(getResourceKey() + KEY_TELNET_CONNECTION_TIMEOUT_COMMAND);
        fontDisplay = (FontInterface)REGISTRY.getProperty(getResourceKey() + KEY_TELNET_FONT_DISPLAY);
        colourForeground = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_TELNET_COLOUR_DISPLAY_FOREGROUND);
        colourBackground = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_TELNET_COLOUR_DISPLAY_BACKGROUND);
        colourCursorForeground = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_TELNET_COLOUR_CURSOR_FOREGROUND);
        colourCursorBackground = (ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_TELNET_COLOUR_CURSOR_BACKGROUND);
        }
    }
