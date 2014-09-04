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

package org.lmn.fc.frameworks.starbase.plugins.levelonea.plugins.leveltwoa.tasks;

import org.lmn.fc.model.tasks.impl.TaskData;


/***************************************************************************************************
 * The RootTask.
 */

public final class RootTask extends TaskData
    {
    private static final long VERSION_ID = -6813793539736054652L;


    /***********************************************************************************************
     * Construct the RootTask.
     */

    private RootTask()
        {
        super(VERSION_ID);

        // Install ourselves as the parent Plugin's RootTask
        setRootTask(true);
        }


    /***********************************************************************************************
     * Initialise the RootTask.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources
        readResources();

        return (REGISTRY.getFramework().validatePlugin());
        }


    /***********************************************************************************************
     * Run the RootTask.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        return (true);
        }


    /***********************************************************************************************
     * Put the RootTask into the Idle state.
     *
     * @return boolean
     */

    public final boolean idleTask()
        {
        return (true);
        }


    /***********************************************************************************************
     * Shutdown the RootTask after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        }


    /***********************************************************************************************
     * Read the Resources for this Task.
     */

    public void readResources()
        {
        //LOGGER.log(getName() + ".readResources()");
        }
    }
