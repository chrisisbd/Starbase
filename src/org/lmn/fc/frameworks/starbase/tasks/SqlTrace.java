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
//  22-10-04    LMN created file from EventViewer
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.impl.SqlTraceReport;



/***************************************************************************************************
 * An SqlTrace for Framework Tasks.
 */

public final class SqlTrace extends TaskData
    {
    // String Resources
    private static final String STATUS_TRACE = "The Query execution trace for";


    /***********************************************************************************************
     * Construct a Framework SqlTrace.
     */

    private SqlTrace()
        {
        super(-6736685067766065714L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the SqlTrace Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Read the Resources for the SqlTrace
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the SqlTrace.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the SqlTrace
        readResources();

        try
            {
            // Remove any previous SqlTrace
            if (getUIComponent() != null)
                {
                getUIComponent().disposeUI();
                }

            // Create a SqlTrace specifically for the Framework Events
            // The ResourceKey is always that of the host Framework,
            // since this is a general utility
            setUIComponent(new SqlTraceReport(this, REGISTRY.getFramework().getResourceKey()));
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
     * Park the SqlTrace in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the SqlTrace data
        stopUI();

        // Clear the ContextActionGroups for the SqlTrace Task
        // Non-runnable Tasks must clear Actions when they go Idle...
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the SqlTrace after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Clear the SqlTrace
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
            // Start the SqlTrace timer if applicable
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_TRACE + SPACE + REGISTRY.getFramework().getName());
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
     * Read all the Resources required by the SqlTrace.
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
