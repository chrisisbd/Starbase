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
//  28-10-04    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.impl.CountryData;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.panels.MapUIComponent;


/***************************************************************************************************
 * The Framework MapViewer.
 */

public final class MapViewer extends TaskData
    {
    // String Resources
    private static final String MSG_SHOW_MAP = "Showing the map for";

    private static final String EXCEPTION_CREATE_MAP = "Unable to create the Framework Map";


    /***********************************************************************************************
     * Construct a MapViewer for the Framework.
     */

    private MapViewer()
        {
        super(-1019726703138954233L);
        }


    /**********************************************************************************************
     * Initialise the MapViewer Task.
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
     * Run the MapViewer.
     *
     * @return boolean
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        // Remove any previous MapUIComponent
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            }

        // Create a MapUIComponent specifically for this Task
        try
            {
            if (REGISTRY.getFramework() != null)
                {
                final CountryPlugin country;

                country = REGISTRY.getCountry(CountryData.getResourceKeyFromCode(REGISTRY_MODEL.getLoggedInUser().getCountryCode()));

                setUIComponent(new MapUIComponent(REGISTRY.getFramework(), country));
                getUIComponent().setDebug(getDebugMode());
                getUIComponent().initialiseUI();

                // Add the location of the Framework, if known
               // ((MapUIComponent)getUIComponent()).addPointOfInterest((PointOfInterestInterface)REGISTRY.getFramework());
                }
            }

        catch (IndicatorException exception)
            {
            handleException(exception,
                            EXCEPTION_CREATE_MAP,
                            EventStatus.WARNING);
            }

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the MapViewer in Idle.
     *
     * @return boolean indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the MapViewer after use.
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
        setStatus(MSG_SHOW_MAP + SPACE + REGISTRY.getFramework().getName());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            // Reduce resources as far as possible
            getUIComponent().disposeUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the MapViewer.
     */

    public final void readResources()
        {
        // Use the parent Framework's Enable.Debug
        setDebugMode(REGISTRY.getBooleanProperty(REGISTRY.getFramework().getResourceKey() + KEY_ENABLE_DEBUG));
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
