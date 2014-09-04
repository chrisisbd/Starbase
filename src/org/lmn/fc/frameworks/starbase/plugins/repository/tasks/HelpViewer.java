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

package org.lmn.fc.frameworks.starbase.plugins.repository.tasks;

import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.panels.HTMLPanel;

/***************************************************************************************************
 * The Plugin HelpViewer.
 */

public final class HelpViewer extends TaskData
    {
    /***********************************************************************************************
     * Construct a HelpViewer for the Plugin.
     */

    private HelpViewer()
        {
        super(7071690460929156387L);
        }


    /**********************************************************************************************
     * Initialise the HelpViewer Task.
     *
     * @return boolean Flag to indicate success or failure of initialisation.
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the HelpViewer.
     *
     * @return boolean
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        // Remove any previous HelpViewer
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            }

        // Create a HelpViewer specifically for this Plugin
        LOGGER.debugNavigation("HelpViewer URL=" + RegistryModelUtilities.getHelpURL(getParentAtom()));

        setUIComponent(new HTMLPanel(RegistryModelUtilities.getHelpURL(getParentAtom())));
        getUIComponent().setDebug(getDebugMode());
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the HelpViewer in Idle.
     *
     * @return boolean indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the HelpViewer after use.
     *
     * @return boolean
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
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_HELP + SPACE + getParentAtom().getName());
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
     * Read all the Resources required by the HelpViewer.
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }
