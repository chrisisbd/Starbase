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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  11-10-03    LMN created file
//  12-05-04    LMN finished it off!
//  18-10-04    LMN converting for runUI() and stopUI()
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.impl.PluginReport;


/***************************************************************************************************
 * A PluginMonitor for FrameworkTasks.
 */

public final class PluginMonitor extends TaskData
    {
    // String Resources
    private static final String STATUS_PLUGINS = "Showing Monitored Plugins and Tasks for";


    /***********************************************************************************************
     * Construct a PluginMonitor.
     */

    private PluginMonitor()
        {
        super(-881163268211812616L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the PluginMonitor.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources for the PluginMonitor
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the PluginMonitor.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the PluginMonitor
        readResources();

        try
            {
            // Remove any previous PluginReport
            if (getUIComponent() != null)
                {
                getUIComponent().disposeUI();
                }

            // Create an PluginMonitor
            // The ResourceKey is always that of the host Framework,
            // since this is a general utility
            setUIComponent(new PluginReport(this, REGISTRY.getFramework().getResourceKey()));
            getUIComponent().initialiseUI();

            // There is no Editor, and we are always in Browse mode
            setEditorComponent(null);
            setBrowseMode(true);
            }

        catch (ReportException exception)
            {
            handleException(exception,
                            "runTask()",
                            EventStatus.WARNING);
            }

        return (true);
        }


    /***********************************************************************************************
     * Park the PluginMonitor in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the PluginReport data
        stopUI();

        // Clear the ContextActionGroups for the PluginMonitor Task
        // Non-runnable Tasks must clear Actions when they go Idle...
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the PluginMonitor after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Clear the PluginReport
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

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
            // Start the PluginReport timer if applicable
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_PLUGINS + SPACE + REGISTRY.getFramework().getName());
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
     * Read all the Resources required by the PluginMonitor.
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
