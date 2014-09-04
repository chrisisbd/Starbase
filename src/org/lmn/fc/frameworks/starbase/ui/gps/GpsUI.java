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

package org.lmn.fc.frameworks.starbase.ui.gps;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.impl.CountryData;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.panels.HTMLPanel;
import org.lmn.fc.ui.panels.MapUIComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;


/***************************************************************************************************
 * The GpsUI.
 */

public final class GpsUI extends UIComponent
                         implements UIComponentPlugin
    {
    private static final String TAB_GPS_LOG = "GPS Log";
    private static final String TAB_MAP = "Map";
    private static final String TAB_CONFIGURATION = "Configuration";
    private static final String TAB_HELP = "Help";
    private static final String GPS_HELP = "GpsHelp.html";

    private TaskPlugin pluginTask;
    private FontInterface pluginFont;
    private ColourInterface pluginColour;
    private JTabbedPane tabbedPane;
    private UIComponentPlugin gpsLog;
    private UIComponentPlugin gpsMap;
    private UIComponentPlugin gpsConfiguration;
    private UIComponentPlugin gpsHelp;


    /***********************************************************************************************
     * Construct a GpsUI.
     *
     * @param task
     * @param font
     * @param colour
     */

    public GpsUI(final TaskPlugin task,
                 final FontInterface font,
                 final ColourInterface colour)
        {
        super();

        if ((task == null)
            || (!task.validatePlugin())
            || (font == null)
            || (colour == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        this.pluginTask = task;
        this.pluginFont = font;
        this.pluginColour = colour;
        }


    /***********************************************************************************************
     * Initialise the GpsUI.
     */

    public final void initialiseUI()
        {
        // Create the GpsUI and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // There is nothing to do to run the TabbedPane itself!
        // Set the selected tab to run each time the Task is run
        UIComponentHelper.runSelectedTabComponent(getHostTask(), this, getTabbedPane());
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        // There is nothing to do to stop the TabbedPane!
        // Stop all UIComponents on the tabs
        UIComponentHelper.stopAllTabComponents(getTabbedPane());
        }


    /***********************************************************************************************
     * Dispose of the GpsUI.
     */

    public final void disposeUI()
        {
        if (getTabbedPane() != null)
            {
            // Reduce resources as far as possible
            UIComponentHelper.disposeAllTabComponents(getTabbedPane());
            getTabbedPane().removeAll();
            setTabbedPane(null);
            removeAll();
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the GpsUI.
     */

    private void createAndInitialiseTabs()
        {
        removeAll();

        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        getTabbedPane().setFont(pluginFont.getFont());
        getTabbedPane().setForeground(pluginColour.getColor());

        final CountryPlugin country;

        gpsLog = new GpsLog(getHostTask(),
                            REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab(TAB_GPS_LOG,
                               (Component)getGpsLog());

        country = REGISTRY.getCountry(CountryData.getResourceKeyFromCode(REGISTRY_MODEL.getLoggedInUser().getCountryCode()));
        gpsMap = new MapUIComponent(REGISTRY.getFramework(), country);
       // ((MapUIComponent)getGpsMap()).addPointOfInterest((PointOfInterestInterface)REGISTRY.getFramework());
        getTabbedPane().addTab(TAB_MAP,
                               (Component)getGpsMap());

        gpsConfiguration = new GpsConfiguration(getHostTask(),
                                                TAB_CONFIGURATION,
                                                REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab(TAB_CONFIGURATION,
                               (Component)getGpsConfiguration());

        gpsHelp = new HTMLPanel(RegistryModelUtilities.getHelpURL(getHostTask().getParentAtom(),
                                                                  GPS_HELP));
        getTabbedPane().addTab(TAB_HELP,
                               (Component)getGpsHelp());

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the NtpUI UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    private TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the JTabbedPane.
     *
     * @return JTabbedPane
     */

    private JTabbedPane getTabbedPane()
        {
        return (this.tabbedPane);
        }


    /***********************************************************************************************
     * Set the JTabbedPane.
     *
     * @param tabbedpane
     */

    private void setTabbedPane(final JTabbedPane tabbedpane)
        {
        this.tabbedPane = tabbedpane;
        }


    /***********************************************************************************************
     * Log an Event from the GPS session.
     *
     * @param logentry
     */

    public void logger(final Vector<Object> logentry)
        {
        if ((getGpsLog() != null)
            && (logentry != null)
            && (!EMPTY_STRING.equals(logentry)))
            {
            ((GpsLog)getGpsLog()).logger(logentry);
            }
        }


    /***********************************************************************************************
     * Get the Gps Log.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getGpsLog()
        {
        return (this.gpsLog);
        }


    /***********************************************************************************************
     * Get the Gps Map.
     *
      * @return UIComponentPlugin
     */

    public UIComponentPlugin getGpsMap()
        {
        return (this.gpsMap);
        }


    /***********************************************************************************************
     * Get the Gps Configuration.
     *
      * @return UIComponentPlugin
     */

    public UIComponentPlugin getGpsConfiguration()
        {
        return (this.gpsConfiguration);
        }


    /***********************************************************************************************
     * Get the Gps Help.
     *
      * @return UIComponentPlugin
     */

    public UIComponentPlugin getGpsHelp()
        {
        return (this.gpsHelp);
        }
    }
