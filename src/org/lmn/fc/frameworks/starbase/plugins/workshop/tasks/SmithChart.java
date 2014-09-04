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

package org.lmn.fc.frameworks.starbase.plugins.workshop.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.frameworks.starbase.plugins.workshop.ui.smithchart.SmithChartPanel;
import org.lmn.fc.model.tasks.impl.TaskData;


/***************************************************************************************************
 * A Smith Chart.
 */

public final class SmithChart extends TaskData
    {
    // String Resources
    private static final String STATUS_CHART = "A Smith Chart for";


    /***********************************************************************************************
     * Construct a Smith Chart.
     */

    private SmithChart()
        {
        super(1270432371282261748L);
        }


    /***********************************************************************************************
     * Initialise the SmithChart Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Read the Resources for the SmithChart
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the SmithChart.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the SmithChart
        readResources();

        setUIComponent(new SmithChartPanel());
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the SmithChart in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the SmithChart after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        if (getUIComponent() != null)
            {
            // Shutdown SmithChart here
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
            // Todo replace with runUI()
            ((SmithChartPanel)getUIComponent()).initialise();
            }

        setCaption(getPathname());
        setStatus(STATUS_CHART + SPACE + REGISTRY.getFramework().getName());
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
     * Read all the Resources required by the SmithChart.
     */

    public final void readResources()
        {
        // Use the Framework's debug mode
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
