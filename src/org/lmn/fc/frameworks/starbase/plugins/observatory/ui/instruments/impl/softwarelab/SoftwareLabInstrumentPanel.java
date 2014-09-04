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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.softwarelab;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JavaConsoleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonConsoleFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JythonEditorFrameUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The SoftwareLabInstrumentPanel.
 */

public final class SoftwareLabInstrumentPanel extends InstrumentUIComponentDecorator
                                              implements SoftwareLabInstrumentPanelInterface
    {
    // String Resources
    private static final String ICON_HEADER = "softwarelab-header.png";
    private static final String INSTRUMENT_HELP = "SoftwareLabHelp.html";

    private static final long serialVersionUID = -633134124449652057L;


    /***********************************************************************************************
     * Construct a SoftwareLabInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public SoftwareLabInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                      final Instrument instrumentxml,
                                      final ObservatoryUIInterface hostui,
                                      final TaskPlugin task,
                                      final FontInterface font,
                                      final ColourInterface colour,
                                      final String resourcekey)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              ControlPanelInterface.INDICATOR_COUNT_0);
        }


    /***********************************************************************************************
     * Initialise the SoftwareLabInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER,
                                                          getFontData(),
                                                          getColourData());

        // Create the SoftwareLabInstrumentPanel and add it to the host UIComponent
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
     * Dispose of the SoftwareLabInstrumentPanel.
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
    /* DAO                                                                                        */
    /***********************************************************************************************
     * Reset the SoftwareLabInstrumentUIComponent.
     *
     * @param resetmode
     */

    public void reset(final ResetMode resetmode)
        {
        final String SOURCE = "SoftwareLabInstrumentPanel.reset() ";

        super.reset(resetmode);

        if (ResetMode.DEFAULTS.equals(resetmode))
            {
            // JavaConsoleTab
            if ((getJavaConsoleTab() != null)
                && (getJavaConsoleTab() instanceof JavaConsoleUIComponentInterface))
                {
                ((JavaConsoleUIComponentInterface)getJavaConsoleTab()).clearConsole();

                // Force an immediate update of the Report only if visible
                if ((getJavaConsoleTab() instanceof ReportTablePlugin)
                    && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getJavaConsoleTab())))
                    {
                    ((ReportTablePlugin) getJavaConsoleTab()).refreshTable();
                    }
                }
            }
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatedata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "SoftwareLabInstrumentPanel.setWrappedData() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        LOGGER.debug(boolDebug,
                     SOURCE + "Add WrappedData EventLogFragment [forcerefreshdata=" + updatedata
                            + "] [updatemetadata=" + updatemetadata + "]");

        // Just handle the EventLogFragment
        // Do not use getWrappedData() because this has not been set!
        if ((InstrumentState.isDoingSomething(getHostInstrument()))
            && (daowrapper.getEventLogFragment() != null)
            && (!daowrapper.getEventLogFragment().isEmpty())
            && (getEventLogTab() != null))
            {
            getHostInstrument().addEventLogFragment(daowrapper.getEventLogFragment());

            // Refresh only if visible
            if ((getEventLogTab() instanceof ReportTablePlugin)
                && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getEventLogTab())))
                {
                ((ReportTablePlugin) getEventLogTab()).refreshTable();
                }
            }

        // Something has changed, we may need to update indicators etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the SoftwareLabInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        final String SOURCE = "SoftwareLabInstrumentPanel.createAndInitialiseTabs() ";

        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM)
            {
            /**************************************************************************************
             * Sets the background color at <code>index</code> to
             * <code>background</code>
             * which can be <code>null</code>, in which case the tab's background color
             * will default to the background color of the <code>tabbedpane</code>.
             * An internal exception is raised if there is no tab at that index.
             *
             * @param index the tab index where the background should be set
             * @param background the color to be displayed in the tab's background
             *
             * @exception IndexOutOfBoundsException if index is out of range
             *            (index &lt; 0 || index &gt;= tab count)
             */

            public void setBackgroundAt(final int index,
                                        final Color background)
                {
                if (index == getSelectedIndex())
                    {
                    System.out.println("SELECTED TAB INDEX=" + index);
                    super.setBackgroundAt(index, Color.white);
                    }
                else
                    {
                    System.out.println("NOT SELECTED TAB INDEX=" + index);
                    super.setBackgroundAt(index, background);
                    }
                }
            });

        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addJavaConsoleTab(this,
                                                    TAB_JAVA_CONSOLE,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addJythonConsoleTab(this,
                                                      TAB_JYTHON_CONSOLE,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addJythonEditorTab(this,
                                                     TAB_JYTHON_EDITOR,
                                                     REGISTRY_MODEL.getLoggedInUser());
        // Cross link the two UI Frames
        if (getJythonConsoleTab() instanceof JythonConsoleFrameUIComponentInterface)
            {
            ((JythonConsoleFrameUIComponentInterface) getJythonConsoleTab()).setJythonEditorFrame(getJythonEditorTab());
            }

        if (getJythonEditorTab() instanceof JythonEditorFrameUIComponentInterface)
            {
            ((JythonEditorFrameUIComponentInterface)getJythonEditorTab()).setJythonConsoleFrame(getJythonConsoleTab());
            }

        InstrumentPanelTabFactory.addHexEditorTab(this,
                                                  TAB_HEX_EDITOR,
                                                  REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addImageIconTab(this,
                                                  TAB_ASCII,
                                                  RegistryModelUtilities.getAtomIcon(getObservatoryUI().getHostAtom(),
                                                                                     ObservatoryInterface.FILENAME_ICON_ASCII),
                                                  null,
                                                  REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_EVENT_LOG,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addXmlTab(this,
                                            TAB_XML,
                                            REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addHelpTab(this,
                                             TAB_HELP,
                                             FileUtilities.html,
                                             INSTRUMENT_HELP,
                                             REGISTRY_MODEL.getLoggedInUser());

       // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the host UIComponent
        this.add(getTabbedPane());
        }


   /***********************************************************************************************
     * Get the JavaConsole for use by the DAO.
     *
     * @return JavaConsoleUIComponentInterface
     */

    public JavaConsoleUIComponentInterface getJavaConsole()
        {
        return ((JavaConsoleUIComponentInterface)getJavaConsoleTab());
        }
    }
