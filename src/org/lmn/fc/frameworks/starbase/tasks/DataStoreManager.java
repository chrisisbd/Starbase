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
//  20-05-05    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.datastore.DataStoreManagerPanel;


/***************************************************************************************************
 * The DataStoreManager.
 */

public final class DataStoreManager extends TaskData
    {
    private static final long VERSION_ID = 7423968493493653914L;

    // String Resources
    private static final String STATUS_MANAGER = "The Data Store Manager for";


    /***********************************************************************************************
     * Construct a DataStoreManager.
     */

    private DataStoreManager()
        {
        super(VERSION_ID, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the DataStoreManager Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Read the Resources for the DataStoreManager
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the DataStoreManager.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the DataStoreManager
        readResources();

        if ((DATABASE != null)
            && (DATABASE.getConnection() != null))
            {
            setUIComponent(new DataStoreManagerPanel(DATABASE.getConnection()));
            }
        else
            {
            setUIComponent(new BlankUIComponent(AWAITING_DEVELOPMENT));
            }
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the DataStoreManager in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the DataStoreManager after use.
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
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().initialiseUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_MANAGER + SPACE + REGISTRY.getFramework().getName());
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
     * Read all the Resources required by the DataStoreManager.
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
