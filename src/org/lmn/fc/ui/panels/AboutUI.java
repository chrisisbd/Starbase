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

package org.lmn.fc.ui.panels;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.frameworks.starbase.tasks.JavaDoc;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.FrameworkData;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.impl.ExpanderFactory;
import org.lmn.fc.model.resources.ExceptionPlugin;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.resources.StringPlugin;
import org.lmn.fc.model.resources.impl.QueryData;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.impl.TabbedNodeReportDecorator;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;


/***************************************************************************************************
 * The AboutUI.
 */

public final class AboutUI extends UIComponent
                           implements UIComponentPlugin
    {
    // String Resources
    private static final String ACKNOWLEDGEMENTS_PDF = "Acknowledgements.pdf";
    private static final String LICENCE_PDF = "Licence.pdf";

    private TaskPlugin pluginTask;
    private FontInterface pluginFont;
    private ColourInterface pluginColour;
    private JTabbedPane tabbedPane;


    /***********************************************************************************************
     * Construct an AboutUI.
     *
     * @param task
     * @param font
     * @param colour
     */

    public AboutUI(final TaskPlugin task,
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
     * Initialise the AboutUI.
     */

    public final void initialiseUI()
        {
        // Create the AboutUI and add it to the host UIComponent
        //createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        // Create the AboutUI and add it to the host UIComponent
        // This makes use of the UserRole, which was not available in initialiseUI()
        createAndInitialiseTabs();

        // There is nothing to do to run the TabbedPane itself!
        // Set the selected tab to run each time the Task is run
        UIComponentHelper.runSelectedTabComponent(getHostTask(), this, getTabbedPane());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     * This is a non-runnable Task, so we can dispose of the UI.
     */

    public final void stopUI()
        {
        // There is nothing to do to stop the TabbedPane itself!
        // Stop all UIComponents on the tabs
        UIComponentHelper.stopAllTabComponents(getTabbedPane());
        }


    /***********************************************************************************************
     * Dispose of the AboutUI.
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
     * Create the panel of tabs for the AboutUI.
     */

    private void createAndInitialiseTabs()
        {
        final Iterator<RootPlugin> iterApplications;
        UIComponentPlugin plugin;

        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        getTabbedPane().setFont(pluginFont.getFont());
        getTabbedPane().setForeground(pluginColour.getColor());

        try
            {
            //-------------------------------------------------------------------------------------
            // Acknowledgements, libraries etc.

            plugin = new PDFPanel(RegistryModelUtilities.getHelpURL(getHostTask().getParentAtom(),
                                                                    ACKNOWLEDGEMENTS_PDF));
            getTabbedPane().addTab(FrameworkData.TAB_ACKNOWLEDGEMENTS,
                                   RegistryModelUtilities.getCommonIcon(ICON_ACKNOWLEDGEMENTS),
                                   (Component)plugin);

            //-------------------------------------------------------------------------------------
            // Find the Framework AboutBox

            if ((REGISTRY.getFramework().aboutBox() != null))
                {
                getTabbedPane().addTab(REGISTRY.getFramework().getName(),
                                       RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(),
                                                                          REGISTRY.getFramework().getIconFilename()),
                                       (Component)REGISTRY.getFramework().aboutBox());
                }

            //-------------------------------------------------------------------------------------
            // Iterate the AboutBoxes for all installed Applications

            if ((REGISTRY.getFramework().getAtoms() != null))
                {
                iterApplications = REGISTRY.getFramework().getAtoms().iterator();

                while (iterApplications.hasNext())
                    {
                    final AtomPlugin pluginAtom = (AtomPlugin)iterApplications.next();

                    if ((pluginAtom != null)
                        && (pluginAtom.isActive())
                        && (pluginAtom.aboutBox() != null))
                        {
                        getTabbedPane().addTab(pluginAtom.getName(),
                                               RegistryModelUtilities.getAtomIcon(pluginAtom,
                                                                                  pluginAtom.getIconFilename()),
                                               (Component)pluginAtom.aboutBox());
                        }
                    }
                }

            //-------------------------------------------------------------------------------------
            // Show all System Properties

            plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                   FrameworkData.TAB_SYSTEM_PROPERTIES,
                                                   REGISTRY.getFramework().getResourceKey(),
                                                   REGISTRY.getSystemReport());
            getTabbedPane().addTab(FrameworkData.TAB_SYSTEM_PROPERTIES,
                                   RegistryModelUtilities.getCommonIcon(PropertyPlugin.PROPERTIES_ICON),
                                   (Component)plugin);

            //-------------------------------------------------------------------------------------
            // Show all Properties

            plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                   ExpanderFactory.EXPANDER_PROPERTIES,
                                                   REGISTRY.getFramework().getResourceKey(),
                                                   REGISTRY.getPropertyReport());
            getTabbedPane().addTab(ExpanderFactory.EXPANDER_PROPERTIES,
                                   RegistryModelUtilities.getCommonIcon(PropertyPlugin.PROPERTIES_ICON),
                                   (Component)plugin);

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Show all Strings
                plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                       ExpanderFactory.EXPANDER_STRINGS,
                                                       REGISTRY.getFramework().getResourceKey(),
                                                       REGISTRY.getStringReport());
                getTabbedPane().addTab(ExpanderFactory.EXPANDER_STRINGS,
                                       RegistryModelUtilities.getCommonIcon(StringPlugin.STRINGS_ICON),
                                       (Component)plugin);
                }

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Show all Exceptions
                plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                       ExpanderFactory.EXPANDER_EXCEPTIONS,
                                                       REGISTRY.getFramework().getResourceKey(),
                                                       REGISTRY.getExceptionReport());
                getTabbedPane().addTab(ExpanderFactory.EXPANDER_EXCEPTIONS,
                                       RegistryModelUtilities.getCommonIcon(ExceptionPlugin.EXCEPTIONS_ICON),
                                       (Component)plugin);
                }

//            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
//                {
//                // Show all installed DataTypes
//                plugin = new TabbedNodeReportDecorator(getHostTask(),
//                                                       DataTypeParser.DATATYPE_REPORT,
//                                                       REGISTRY.getFramework().getResourceKey(),
//                                                       REGISTRY.getDataTypesReport());
//                getTabbedPane().addTab(DataTypeParser.DATATYPE_REPORT,
//                                       RegistryModelUtilities.getCommonIcon(DataTypeParser.DATATYPES_ICON),
//                                       (Component)plugin);
//                }

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Show all Queries
                plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                       ExpanderFactory.EXPANDER_QUERIES,
                                                       REGISTRY.getFramework().getResourceKey(),
                                                       REGISTRY.getQueryReport());
                getTabbedPane().addTab(ExpanderFactory.EXPANDER_QUERIES,
                                       RegistryModelUtilities.getCommonIcon(QueryData.QUERIES_ICON),
                                       (Component)plugin);
                }

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Show all installed Countries
                plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                       CountryPlugin.COUNTRY_REPORT,
                                                       REGISTRY.getFramework().getResourceKey(),
                                                       REGISTRY.getCountriesReport());
                getTabbedPane().addTab(CountryPlugin.COUNTRY_REPORT,
                                       RegistryModelUtilities.getCommonIcon(CountryPlugin.COUNTRIES_ICON),
                                       (Component)plugin);
                }

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                // Show all installed Languages
                plugin = new TabbedNodeReportDecorator(getHostTask(),
                                                       LanguagePlugin.LANGUAGE_REPORT,
                                                       REGISTRY.getFramework().getResourceKey(),
                                                       REGISTRY.getLanguagesReport());
                getTabbedPane().addTab(LanguagePlugin.LANGUAGE_REPORT,
                                       RegistryModelUtilities.getCommonIcon(LanguagePlugin.LANGUAGES_ICON),
                                       (Component)plugin);
                }

            //-------------------------------------------------------------------------------------
            // Show the JavaDoc

            plugin = new HTMLPanel(RegistryModelUtilities.getJavadocURL(REGISTRY.getFramework().getClass()));
            getTabbedPane().addTab(JavaDoc.JAVADOC_REPORT,
                                   RegistryModelUtilities.getCommonIcon(ICON_JAVADOC),
                                   (Component)plugin);

            //-------------------------------------------------------------------------------------
            // The Licence

            plugin = new PDFPanel(RegistryModelUtilities.getHelpURL(getHostTask().getParentAtom(),
                                                                    LICENCE_PDF));
            getTabbedPane().addTab(FrameworkData.TAB_LICENCE,
                                   RegistryModelUtilities.getCommonIcon(ICON_LICENCE),
                                   (Component)plugin);
            }

        catch (ReportException exception)
            {
            getHostTask().handleException(exception,
                                          "AboutUI.createAndInitialiseTabs()",
                                          EventStatus.WARNING);
            }

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the UIComponent
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
    }
