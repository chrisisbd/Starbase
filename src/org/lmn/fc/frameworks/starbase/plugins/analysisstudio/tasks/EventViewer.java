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

package org.lmn.fc.frameworks.starbase.plugins.analysisstudio.tasks;

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.impl.EventReport;


/***************************************************************************************************
 * An EventViewer for AnalysisStudio Tasks.
 */

public final class EventViewer extends TaskData
    {
    /***********************************************************************************************
     * Construct a AnalysisStudio EventViewer.
     */

    private EventViewer()
        {
        super(-6513377871611316832L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the EventViewer Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        return (true);
        }


    /***********************************************************************************************
     * Run the EventViewer.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the EventViewer
        readResources();

        try
            {
            // Remove any previous EventReport
            if (getUIComponent() != null)
                {
                // This will call clearContextActionGroups() for the UIComponent
                getUIComponent().disposeUI();
                setUIComponent(null);
                }

            // Create an EventViewer specifically for the Framework Events
            // The ResourceKey is always that of the host Framework,
            // since this is a general utility
            setUIComponent(new EventReport(this,
                                           DATABASE.getDatabaseOptions().getDataStore(),
                                           REGISTRY.getFramework().getResourceKey()));

            // This will call assembleContextActionGroups() in ReportTable
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
     * Park the EventViewer in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the EventReport data
        stopUI();

        // Clear the ContextActionGroups for the EventViewer Task
        // Non-runnable Tasks must clear Actions when they go Idle...
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the EventViewer after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Clear the EventReport
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
        if (getUIComponent() != null)
            {
            // Ensure that there is something to see as soon as possible...
            // Start the EventReport timer if applicable
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_EVENTS + SPACE + getParentAtom().getName());
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
     * Read all the Resources required by the EventViewer.
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
