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
//  10-10-03    LMN created file
//  13-10-03    LMN tidying up
//  16-10-03    LMN generalised paths
//  18-10-04    LMN converting for runUI() and stopUI()
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.panels.HTMLPanel;


/***************************************************************************************************
* A FrameworkTask to view the JavaDoc for the FrameworkTasks
* which are children of the selected Framework.
*/

public final class JavaDoc extends TaskData
    {
    private static final long VERSION_ID = -4453085429501834419L;

    // String Resources
    private static final String MSG_SHOW_JAVADOC = "Showing Javadoc for";
    public static final String JAVADOC_REPORT    = "JavaDoc";


    /***********************************************************************************************
     * Construct a JavaDoc viewer for the FrameworkTasks.
     */

    private JavaDoc()
        {
        super(VERSION_ID, REGISTRY.getFramework());
        }


     /***********************************************************************************************
      * Initialise the JavaDoc viewer.
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
     * Run the JavaDoc Viewer.
     *
     * @return boolean Flag to indicate success or failure of Task run.
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

        // Create a JavaDoc specifically for this Task
        setUIComponent(new HTMLPanel(RegistryModelUtilities.getJavadocURL(getClass())));
        getUIComponent().setDebug(getDebugMode());
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


     /***********************************************************************************************
      * Park the JavaDoc in Idle.
      *
      * @return boolean indicating success or failure
      */

     public final boolean idleTask()
         {
         stopUI();

         return (true);
         }


     /***********************************************************************************************
      * Shutdown the JavaDoc after use.
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
         setStatus(MSG_SHOW_JAVADOC + SPACE + REGISTRY.getFramework().getName());
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
      * Read all the Resources required by the JavaDoc.
      */

     public final void readResources()
         {
         // Use the parent Framework's Enable.Debug
         setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
