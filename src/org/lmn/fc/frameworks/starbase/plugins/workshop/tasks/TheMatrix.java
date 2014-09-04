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

import org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.impl.LargeFontMatrixUIComponent;
import org.lmn.fc.model.tasks.impl.TaskData;


/***************************************************************************************************
 * TheMatrix.
 */

public final class TheMatrix extends TaskData
    {
    // String Resources
    private static final String STATUS_THEMATRIX = "The Matrix";


    /***********************************************************************************************
     * Construct a TheMatrix.
     */

    private TheMatrix()
        {
        super(-6725005267599838559L);
        }


    /***********************************************************************************************
     * Initialise the TheMatrix Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Read the Resources for the TheMatrix
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the TheMatrix.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the TheMatrix
        readResources();

        // Create TheMatrix
        // It must know the host plugin in order to find images
        setUIComponent(new LargeFontMatrixUIComponent(REGISTRY_MODEL.getFrameworkManagerUI().getStatusBar()));
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the TheMatrix in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the TheMatrix after use.
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

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_THEMATRIX + SPACE + REGISTRY.getFramework().getName());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the TheMatrix.
     */

    public final void readResources()
        {
        // Use the Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }
