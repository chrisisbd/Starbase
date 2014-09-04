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

import org.lmn.fc.frameworks.starbase.ui.snmp.SnmpUI;
import org.lmn.fc.model.tasks.impl.TaskData;


/***************************************************************************************************
 * An SNMP Explorer.
 */

public final class SnmpExplorer extends TaskData
    {
    // String Resources
    private static final String STATUS_EXPLORER = "An SNMP explorer for";


    /***********************************************************************************************
     * Construct an SnmpExplorer.
     */

    private SnmpExplorer()
        {
        super(-2407442292759411587L, REGISTRY.getFramework());

        setDebugMode(false);
        }


    /***********************************************************************************************
     * Initialise the SnmpExplorer Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        return (true);
        }


    /***********************************************************************************************
     * Run the SnmpExplorer.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the SnmpExplorer
        readResources();

        // Remove any previous SnmpExplorer
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Create a UIComponent and initialise it
        // Use the Framework's Report settings...
        setUIComponent(new SnmpUI(this,
                                  REGISTRY.getFrameworkResourceKey()));
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the SnmpExplorer in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        // Clear the ContextActionGroups for the SnmpExplorer Task
        // Non-runnable Tasks must clear Actions when they go Idle...
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the SnmpExplorer after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Clear the ContextActionGroups for the EventViewer Task
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        LOGGER.debugNavigation("SnmpExplorer.runUI()");

        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_EXPLORER + SPACE + REGISTRY.getFramework().getName());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the SnmpExplorer.
     */

    public final void readResources()
        {
        // Use the Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }
