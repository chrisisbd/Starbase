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
//  18-01-02    LMN started again...
//  21-01-02    LMN added database access
//  21-02-02    LMN added ActiveChangeEvent handling
//  19-04-02    LMN added Tester menu
//  29-04-02    LMN installing ApplicationComponent...
//  29-04-02    LMN moved to Core package, as an ApplicationMethod
//  13-05-02    LMN added Antenna.createAxisController
//  05-07-02    LMN added tester for StarMap
//  06-05-03    LMN converted to run as Core.UserInterface on startup
//  08-05-03    LMN added setCaption()
//  15-05-03    LMN converted to a TaskData, removed menus to RegistryModel
//  17-05-03    LMN converting for Task initialise(), run() and shutdown()
//  18-06-03    LMN implemented ApplicationProperties for frame load and save
//  07-10-03    LMN moved into the FrameworkTasks!
//  10-10-03    LMN converting for FrameworkTask
//  20-11-03    LMN debugging L&F switching
//  21-11-03    LMN finally made it all work!
//  02-01-05    LMN added removeHelpAction()
//  04-04-05    LMN tidying logging
//  27-05-06    LMN made it work again after 5 month XmlBeans rewrite!
//  16-06-06    LMN doing major rewrite to separate the UIComponent from the underlying UserObjectPlugin!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.comparators.RootPluginByName;
import org.lmn.fc.common.events.ActiveChangeEvent;
import org.lmn.fc.frameworks.starbase.ui.userinterface.UserInterfaceFrame;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.UserInterfacePlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.manager.UserInterfaceUIComponentPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * The User Interface Task.
 */

public final class UserInterface extends TaskData
                                 implements UserInterfacePlugin
    {
    private static final long VERSION_ID = 143085916479833623L;


    /***********************************************************************************************
     * Privately construct a UserInterface Task.
     */

    private UserInterface()
        {
        super(VERSION_ID, REGISTRY.getFramework());

        // Initialise the UserInterface UI component
        setUIComponent(null);
        }


    /**********************************************************************************************/
    /* Task Management                                                                            */
    /***********************************************************************************************
     * Initialise the User Interface Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        if (REGISTRY.getFramework().validatePlugin())
            {
            // Get the latest Resources
            readResources();

            // Add all specific UserInterface ContextActions
            clearUserObjectContextActionGroups();

            // TODO ??? addContextActionGroup(createContextActionGroup());

            // Create the UserInterface UI Component, and initialise it
            setUIComponent(new UserInterfaceFrame(getParentAtom(),
                                                  this,
                                                  getResourceKey()));
            if (getUIComponent() != null)
                {
                getUIComponent().initialiseUI();
                }

            // Install ourselves as the new UserInterface
            REGISTRY_MODEL.setUserInterface(this);

            // Initialised successfully
            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Run the UserInterface Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        if ((this.validatePlugin())
            && (REGISTRY_MODEL.getUserInterface().equals(this))
            && (REGISTRY.getFramework().validatePlugin()))
            {
            // Get the latest Resources
            readResources();

            // Run the UserInterface UI in BrowseMode
            setEditorComponent(null);
            setBrowseMode(true);

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Park the UserInterface in Idle (this should never happen).
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Stop the UserInterface UI
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the UserInterface Task after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        // Stop the UserInterface UI
        stopUI();

        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Remove all UserInterface ContextActions
        clearUserObjectContextActionGroups();

        // Uninstall ourselves...
        REGISTRY_MODEL.setUserInterface(null);

        return (true);
        }


     /**********************************************************************************************/
     /* UserInterface ContextActions                                                               */
     /***********************************************************************************************
      * Add all ContextActions which are specific to this UserInterface.
      */

     public static ContextActionGroup createContextActionGroup(final String name)
         {
         final ContextActionGroup groupUI;
         ContextAction contextAction;
         final URL imageURL;
         final ImageIcon imageIcon;
         final Enumeration<LookAndFeelPlugin> enumLookAndFeels;
         final Vector lookandfeels;
         final Iterator<LookAndFeelPlugin> iterLookAndFeels;

         groupUI = new ContextActionGroup(name, true, true);

         // Menu items to set the Look and Feel
         // Use the same Icon for all Look&Feel for now...
         imageURL = UserInterface.class.getResource(LookAndFeelPlugin.ICON_LOOKANDFEEL);

         if (imageURL != null)
             {
             imageIcon = new ImageIcon(imageURL);
             }
         else
             {
             imageIcon = null;
             }

         lookandfeels = new Vector<RootPlugin>(10);
         enumLookAndFeels = REGISTRY.getLookAndFeels().elements();

         while (enumLookAndFeels.hasMoreElements())
             {
             lookandfeels.add(enumLookAndFeels.nextElement());
             }

         // Sort the LookAndFeels by Name
         Collections.sort(lookandfeels, new RootPluginByName());

         // Iterate over the sorted list
         iterLookAndFeels = lookandfeels.iterator();
         int i = 0;

         while (iterLookAndFeels.hasNext())
             {
             final LookAndFeelPlugin lookAndFeelPlugin;

             lookAndFeelPlugin = iterLookAndFeels.next();

             // Put Look&Feel only on the menu
             contextAction = new ContextAction(lookAndFeelPlugin.getName(),
                                               imageIcon,
                                               lookAndFeelPlugin.getName(),
                                               KeyEvent.VK_0 + i++,
                                               true,
                                               false)
                 {
                 public void actionPerformed(final ActionEvent event)
                     {
                     if ((REGISTRY_MODEL.getUserInterface() != null)
                         && (REGISTRY_MODEL.getUserInterface().getUIComponent() != null))
                         {
                         ((UserInterfaceUIComponentPlugin)REGISTRY_MODEL.getUserInterface().getUIComponent()).setLookAndFeel(event, lookAndFeelPlugin.getClassName());
                         }
                     }
                 };

             groupUI.addContextAction(contextAction);
             }

         return (groupUI);
         }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }
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
     * Get the UserInterface UI plugin.
     *
     * @return JFrame
     */

    public final UserInterfaceUIComponentPlugin getUI()
        {
        return ((UserInterfaceUIComponentPlugin)getUIComponent());
        }


    /***********************************************************************************************
     * Validate the UserInterface.
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
     * Read all the Resources required by the UserInterface.
     */

    public final void readResources()
        {
        // getResourceKey() returns '<Framework>.UserInterface.'
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
        }


    /**********************************************************************************************/
    /* Event Handling                                                                             */
    /***********************************************************************************************
     * Update the UI following a change in the Active state of an item.
     * This is required by the ActiveChangeListener interface.
     *
     * @param event
     */

    public final void activeUpdate(final ActiveChangeEvent event)
        {
        if ((event.getSourceName().equals(getName()))
            && (!event.getNewValue()))
            {
            JOptionPane.showMessageDialog(null,
                                          getName() + SPACE + "has been deactivated",
                                          "Active State Changed",
                                          JOptionPane.ERROR_MESSAGE);
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
