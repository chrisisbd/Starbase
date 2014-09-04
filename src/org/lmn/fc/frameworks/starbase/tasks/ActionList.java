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

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.impl.ActionListReport;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;


/***************************************************************************************************
 * An ActionList.
 */

public final class ActionList extends TaskData
    {
    private ContextAction showContextAction;


    /***********************************************************************************************
     * Construct an ActionList.
     */

    private ActionList()
        {
        super(-3671031074227561653L, REGISTRY.getFramework());
        }


    /***********************************************************************************************
     * Initialise the ActionList.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        final ContextActionGroup groupStatic;
        final URL imageURL;

        // Get the latest Resources for the ActionList
        readResources();

        // This ContextAction remains visible at all times, so put it in the ActionGroup.STATIC
        if ((REGISTRY.getFramework().getUserObjectContextActionGroups() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().size() > ActionGroup.STATIC.getIndex()))
            {
            groupStatic = REGISTRY.getFramework().getUserObjectContextActionGroupByIndex(ActionGroup.STATIC);

            imageURL = getClass().getResource(ACTION_ICON_ACTIONS);

            if ((groupStatic != null)
                && (imageURL != null))
                {
                // Allow access to the ActionList from within the inner class
                // Must be final to achieve this!
                final TaskPlugin actionList = this;

                showContextAction = new ContextAction(ACTION_SHOW_ACTIONS,
                                                      new ImageIcon(imageURL),
                                                      TOOLTIP_ACTION_SHOW_ACTIONS,
                                                      KeyEvent.VK_A,
                                                      false,
                                                      true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // Show the ActionList
                        LOGGER.debugNavigation("Show the ActionList");
                        actionList.actionPerformed(event, true);
                        }
                    };

                groupStatic.addContextAction(showContextAction);
                }
            }

        return (true);
        }


    /***********************************************************************************************
     * Run the ActionList.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the ActionList
        readResources();

        // Remove any previous ActionList
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            }

        // Create an ActionList
        // The ResourceKey is always that of the host Framework,
        // since this is a general utility
        setUIComponent(new ActionListReport(this, REGISTRY.getFramework().getResourceKey()));
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the ActionList in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the PluginReport data
        stopUI();

        // Clear the ContextActionGroups for the ActionList Task
        // Non-runnable Tasks must clear Actions when they go Idle...
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the ActionList after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Remove the persistent Run ContextAction
        if ((REGISTRY.getFramework() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().size() > ActionGroup.STATIC.getIndex())
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().get(ActionGroup.STATIC.getIndex()) != null))
            {
            REGISTRY.getFramework().getUserObjectContextActionGroups().get(ActionGroup.STATIC.getIndex()).removeContextAction(showContextAction);
            }

        // Archive and then clear the ActionListReport
        // Check that there is a report to archive - it may still be a BlankUIComponent!
        if (getUIComponent() != null)
            {
            if (getUIComponent() instanceof ActionListReport)
                {
                ((ActionListReport)getUIComponent()).archiveActions();
                }

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
            // Start the ActionList timer if applicable
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_ACTIONS + SPACE + REGISTRY.getFramework().getName());
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
     * Read all the Resources required by the ActionList.
     */

    public final void readResources()
        {
        // Use the Framework's Enable.Debug
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
        }
    }
