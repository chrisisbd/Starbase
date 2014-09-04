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

import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.RaDecGrid;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.Sky;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.Sun;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.impl.StarMapUIComponent;
import org.lmn.fc.model.tasks.impl.TaskData;

import java.awt.*;


/***************************************************************************************************
 * A StarMapTester.
 */

public final class StarMapTester extends TaskData
    {
    // String Resources
    private static final String STATUS_TESTER = "A StarMap tester for";


    /***********************************************************************************************
     * Construct a StarMapTester.
     */

    private StarMapTester()
        {
        super(5116360548044166856L);
        }


    /***********************************************************************************************
     * Initialise the StarMapTester Task.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Read the Resources for the StarMapTester
        readResources();

        return (true);
        }


    /***********************************************************************************************
     * Run the StarMapTester.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Read the Resources for the StarMapTester
        readResources();

        // Remove any previous StarMapTester
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            }

        // Create a UIComponent
        StarMapUIComponentPlugin starMap = new StarMapUIComponent(null);

        starMap.setExtents(0.0, 360.0, 0.0, 90.0);
        starMap.enableScales(true);

        Sky pluginSky = new Sky(null,
                                starMap,
                                "Sky",
                                "Night",
                                new Color(0, 0, 0));
        pluginSky.setActive(true);
        starMap.addPlugin(pluginSky);

        RaDecGrid pluginRaDecGrid = new RaDecGrid(null,
                                                  starMap,
                                                  "Projection Grid",
                                                  "RaDec",
                                                  StarMapPointInterface.NOTCLICKABLE,
                                                  new Color(200, 20, 20));
        pluginRaDecGrid.setActive(true);
        starMap.addPlugin(pluginRaDecGrid);

        Sun sun = new Sun(null,
                          starMap, "Sun", "Sun", true, true, Color.yellow);
        sun.setActive(true);
        starMap.addPlugin(sun);

        starMap.showPluginNames();
        setUIComponent(starMap);
        getUIComponent().initialiseUI();
        starMap.refreshStarMap();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        return (true);
        }


    /***********************************************************************************************
     * Park the StarMapTester in Idle.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the StarMapTester after use.
     *
     * @return boolean flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        if (getUIComponent() != null)
            {
            setUIComponent(null);
            }

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this StarMapTester when its tree node is selected.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_TESTER + SPACE + REGISTRY.getFramework().getName());
        }


    /***********************************************************************************************
     * Stop the UI of this StarMapTester when its tree node is deselected.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the StarMapTester.
     */

    public final void readResources()
        {
        // Use the Framework's Enable.Debug
        setDebugMode(REGISTRY.getFramework().getDebugMode());
        }
    }
