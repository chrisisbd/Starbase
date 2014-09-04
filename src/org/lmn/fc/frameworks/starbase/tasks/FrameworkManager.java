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
//  11-02-02    LMN created file
//  14-02-02    LMN added read-only PropertyViewer
//  18-02-02    LMN added JTree, previously in RegistryModel
//  30-04-02    LMN moved to Core package, as an ApplicationMethod
//  14-04-03    LMN making setDividerLocation() work...
//  15-04-03    LMN adding Icons to JTree, implemented ObjectMethodInterface
//  20-04-03    LMN added handler for TaskData
//  24-04-03    LMN used Reflection to run a TaskObject!!
//  06-05-03    LMN ran successfully as a Task from startup
//  09-06-03    LMN added support for ApplicationTasks
//  16-06-03    LMN added ApplicationProperty editor
//  29-06-03    LMN changed from TreeSelectionListener to MouseListener
//  07-10-03    LMN moved into the FrameworkTasks!
//  10-10-03    LMN made it run for the first time in months!
//  21-10-03    LMN corrected fault with initial node selection
//  06-11-03    LMN added StatusIndicatorKey properties
//  07-11-03    LMN converted to full-screen mode!
//  14-11-03    LMN converted underlying components to JComponent
//  16-12-03    LMN finally getting popup menu Task control to work...
//  18-10-04    LMN tidying up!
//  16-11-04    LMN extending status bar functionality
//  03-01-05    LMN added ToggleIcons ContextAction
//  27-05-06    LMN made it work again after 5 month XmlBeans rewrite!
//  15-06-06    LMN doing major rewrite to separate the UIComponent from the underlying UserObjectPlugin!
//  31-07-06    LMN seemed to have made it work again...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.frameworks.starbase.ui.manager.FrameworkManagerPanel;
import org.lmn.fc.model.root.ActionGroup;
import org.lmn.fc.model.tasks.FrameworkManagerPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.manager.FrameworkManagerUIComponentPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;


/***************************************************************************************************
 * The Framework Manager.
 */

public final class FrameworkManager extends TaskData
                                    implements FrameworkManagerPlugin
    {
    /***********************************************************************************************
     * Privately construct a FrameworkManager Task.
     */

    private FrameworkManager()
        {
        super(3963688163875061490L);
        }


    /**********************************************************************************************/
    /* Task Management                                                                            */
    /***********************************************************************************************
     * Initialise the FrameworkManager Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Initialise the FrameworkManager UI component
        setUIComponent(null);

        // The FrameworkManager has no meaning if there is no valid UserInterface installed
        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getUserInterface().validatePlugin())
            && (REGISTRY.getFramework().validatePlugin()))
            {
            // Get the latest Resources
            readResources();

            // Create the FrameworkManager UI Component, and initialise it
            setUIComponent(new FrameworkManagerPanel(this, getResourceKey()));

            if (getUIComponent() != null)
                {
                getUIComponent().initialiseUI();
                }

            // Get the Framework STATIC ContextActionGroup,
            // since the Actions must be visible at all times
            // (the FrameworkManager cannot be selected)
            if ((REGISTRY.getFramework().getUserObjectContextActionGroups() != null)
                && (REGISTRY.getFramework().getUserObjectContextActionGroups().size() > ActionGroup.STATIC.getIndex()))
                {
                final ContextActionGroup groupStatic;

                groupStatic = REGISTRY.getFramework().getUserObjectContextActionGroupByIndex(ActionGroup.STATIC);

                if (groupStatic != null)
                    {
                    // Set up all ContextActions for this FrameworkManager
                    createFrameworkManagerActions(groupStatic, getUIComponent());
                    }
                }

            // Install ourselves as the new FrameworkManager
            REGISTRY_MODEL.setFrameworkManager(this);

            // Initialised successfully
            return (true);
            }
        else
            {
            // We must have a UserInterface!
            return (false);
            }
        }


    /***********************************************************************************************
     * Run the FrameworkManager Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getUserInterface().validatePlugin())
            && (REGISTRY_MODEL.getFrameworkManager().equals(this))
            && (REGISTRY_MODEL.getFrameworkManager().validatePlugin())
            && (REGISTRY.getFramework().validatePlugin()))
            {
            // Get the latest Properties
            readResources();

            // Run the FrameworkManager UI in BrowseMode
            setEditorComponent(null);
            setBrowseMode(true);

            return (true);
            }
        else
            {
            // No UserInterface, so we can't show the FrameworkManager!
            return (false);
            }
        }


    /***********************************************************************************************
     * Park the FrameworkManager in Idle (this should never happen).
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Stop the FrameworkManager UI
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the FrameworkManager Task after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        // Stop the FrameworkManager UI
        stopUI();

        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // ToDo Review this! Remove all ContextActions
        // Slightly academic, since without a FrameworkManager,
        // there's no point in having ContextActions?
        if ((REGISTRY.getFramework().getUserObjectContextActionGroups() != null)
            && (REGISTRY.getFramework().getUserObjectContextActionGroups().size() > ActionGroup.STATIC.getIndex()))
            {
            final ContextActionGroup groupStatic;

            groupStatic = REGISTRY.getFramework().getUserObjectContextActionGroupByIndex(ActionGroup.STATIC);

            if (groupStatic != null)
                {
                // Remove the Framework persistent ContextActions
                REGISTRY.getFramework().removeUserObjectContextActionGroup(groupStatic);
                }
            }

        // Clear the ContextActionGroups for the Task (there shouldn't be any)
        clearUserObjectContextActionGroups();

        // Uninstall the FrameworkManager...
        REGISTRY_MODEL.setFrameworkManager(null);

        return (true);
        }


    /**********************************************************************************************/
    /* Framework Manager User Interface                                                           */
    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }
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
     * Get the FrameworkManager UIComponentPlugin.
     *
     * @return FrameworkManagerUIComponentPlugin
     */

    public FrameworkManagerUIComponentPlugin getUI()
        {
        return ((FrameworkManagerUIComponentPlugin)getUIComponent());
        }


    /***********************************************************************************************
     * Set up the FrameworkManager Task ContextActions.
     * These usually affect the UIComponent.
     *
     * @param actiongroup
     * @param uicomponent
     */

    private void createFrameworkManagerActions(final ContextActionGroup actiongroup,
                                               final UIComponentPlugin uicomponent)
        {
        final ContextAction actionFullScreen;
        final ContextAction actionToggleMode;
        URL imageURL;

        if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
            {
            // Set up the FrameworkManager ContextActions, for toolbar only
            // The full screen Action must appear on the Framework STATIC area of the toolbar,
            // otherwise we could only change modes if the FrameworkManager was selected...

            imageURL = getClass().getResource(ACTION_ICON_FULLSCREEN);
            if (imageURL != null)
                {
                actionFullScreen = new ContextAction(ACTION_FULL_SCREEN,
                                                     new ImageIcon(imageURL),
                                                     TOOLTIP_ACTION_FULL_SCREEN,
                                                     KeyEvent.VK_F,
                                                     false,
                                                     true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        if (uicomponent != null)
                            {
                            ((FrameworkManagerUIComponentPlugin)uicomponent).toggleScreenMode();
                            }
                        }
                    };

                // ToDo I just can't get this to work reliably!
                //actiongroup.addContextAction(actionFullScreen);
                }
            }

        if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
            {
            // Set up the persistent ContextAction for the Icon toggle
            imageURL = getClass().getResource(ACTION_ICON_ICONS);
            if (imageURL != null)
                {
                actionToggleMode = new ContextAction(ACTION_TOGGLE_ICONS,
                                                     new ImageIcon(imageURL),
                                                     TOOLTIP_ACTION_TOGGLE_ICONS,
                                                     KeyEvent.VK_I,
                                                     false,
                                                     true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        if (uicomponent != null)
                            {
                            ((FrameworkManagerUIComponentPlugin)uicomponent).toggleCustomIcons();
                            }
                        }
                    };

                actiongroup.addContextAction(actionToggleMode);
                }
            }
        }


    /***********************************************************************************************
     * Validate the FrameworkManager.
     *
     * @return boolean
     */

    public boolean validatePlugin()
        {
        boolean boolValid;

        boolValid = super.validatePlugin();

//        if (getUIComponent() == null)
//            {
//            boolValid = false;
//            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Read all the Resources required by the FrameworkManager.
     */

    public void readResources()
        {
        // getResourceKey() returns '<Framework>.FrameworkManager.'
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
