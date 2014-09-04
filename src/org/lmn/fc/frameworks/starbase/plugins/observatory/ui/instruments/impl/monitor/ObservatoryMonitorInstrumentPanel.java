// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.CommandLifecycleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.JavaConsoleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.MemoryUsageChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.CommandLifecycleUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.PortMonitorUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.ActionsUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.PortControllerInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortMessageListener;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;


/***************************************************************************************************
 * The ObservatoryMonitorInstrumentPanel.
 */

public final class ObservatoryMonitorInstrumentPanel extends InstrumentUIComponentDecorator
                                                     implements ObservatoryMonitorInstrumentPanelInterface
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    // String Resources
    private static final String ICON_HEADER = "monitor-header.png";

    private static final String INSTRUMENT_HELP = "MonitorHelp.html";

    private static final long serialVersionUID = 947437491032658670L;

    private static final int TIMER_PERIOD_MEMORY = 20000;
    private static final int DAO_CHANNEL_COUNT = 1;

    private UIComponentPlugin tabCommandMonitor;
    private UIComponentPlugin tabPortMonitor;
    private UIComponentPlugin tabMemoryMonitor;
    private UIComponentPlugin tabActions;

    private Timer timerMemory;
    private final TimeSeriesCollection collectionTimeSeries;
    private final TimeSeries seriesMemorySnapshots;


    /***********************************************************************************************
     * Construct a ObservatoryMonitorInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public ObservatoryMonitorInstrumentPanel(final ObservatoryInstrumentInterface instrument,
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
              0);

        // Make a TimeSeries based on Seconds...
        this.seriesMemorySnapshots = new TimeSeries("Memory Usage");
        this.collectionTimeSeries = new TimeSeriesCollection(getMemoryTimeSeries());
        }


    /***********************************************************************************************
     * Initialise the ObservatoryMonitorInstrumentPanel.
     */

    public final void initialiseUI()
        {
        final Runtime runTime;

        super.initialiseUI();

        runTime = Runtime.getRuntime();
        getMemoryTimeSeries().clear();

        // Create the Memory Timer
        timerMemory = new Timer(TIMER_PERIOD_MEMORY, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final String SOURCE = "Timer.actionPerformed() ";

                if ((getMemoryTimeSeries() != null)
                    && (getObservatoryClock() != null))
                    {
                    final long longUsed;
                    final double dblUsedPercent;
                    final GregorianCalendar calMemorySnapshot;

                    calMemorySnapshot = getObservatoryClock().getCalendarDateNow();

                    // We need to set the value to MemoryTotal-MemoryFree, as a percentage of MemoryTotal
                    longUsed = runTime.totalMemory() - runTime.freeMemory();
                    dblUsedPercent = (double)((longUsed * 100) / runTime.totalMemory());

                    // ToDo Needs the Locale too
                    getMemoryTimeSeries().addOrUpdate(new Second(calMemorySnapshot.getTime(),
                                                                 calMemorySnapshot.getTimeZone()),
                                                      dblUsedPercent);

                    if (getHostInstrument().getDAO() != null)
                        {
                        // This will eventually be cleared by the call to refreshChart()
                        getHostInstrument().getDAO().setRawDataChanged(true);
                        getHostInstrument().getDAO().setProcessedDataChanged(true);
                        }

                    LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                 SOURCE + "Memory Monitor Timer TICK! [percent_used=" + dblUsedPercent + "]");

                    if ((getMemoryMonitorTab() != null)
                        && (getMemoryMonitorTab() instanceof ChartUIComponentPlugin))
                        {
                        // Remember to use the TimeZone of the original dataset
                        // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
                        ((ChartUIComponentPlugin) getMemoryMonitorTab()).setChannelCount(DAO_CHANNEL_COUNT);
                        ((ChartUIComponentPlugin) getMemoryMonitorTab()).setTemperatureChannel(false);
                        ((ChartUIComponentPlugin) getMemoryMonitorTab()).setPrimaryXYDataset(getHostInstrument().getDAO(), getTimeSeriesCollection());

                        // Apply the Metadata to the DAO
                        ((ChartUIComponentPlugin) getMemoryMonitorTab()).setMetadata(ObservatoryMonitorHelper.createMemoryMonitorChannelMetadata(),
                                                                                     getHostInstrument().getDAO(),
                                                                                     true,
                                                                                     LOADER_PROPERTIES.isMetadataDebug());

                        // Force an immediate update only if visible
                        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getMemoryMonitorTab()))
                            {
                            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                         SOURCE + "--> refreshChart()");

                            ((ChartUIComponentPlugin) getMemoryMonitorTab()).refreshChart(getHostInstrument().getDAO(),
                                                                                          UIComponentHelper.shouldRefresh(false,
                                                                                                                          getHostInstrument(),
                                                                                                                          getMemoryMonitorTab()),
                                                                                          SOURCE);
                            }
                        }
                    else
                        {
                        LOGGER.error("ObservatoryMonitorInstrumentPanel MemoryUsageTab is NULL or of incorrect type");
                        }
                    }
                else
                    {
                    LOGGER.error("ObservatoryMonitorInstrumentPanel Timer has no data");
                    }
                }
            });

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER,
                                                          getFontData(),
                                                          getColourData());

        // Create the ObservatoryMonitorInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        if (timerMemory != null)
            {
            timerMemory.setCoalesce(false);
            timerMemory.restart();
            }

        // There is nothing to do to run the TabbedPane itself!
        // Set the selected tab to run each time the Task is run
        UIComponentHelper.runSelectedTabComponent(getHostTask(), this, getTabbedPane());
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        if (timerMemory != null)
            {
            // Stop the timer
            timerMemory.stop();
            timerMemory = null;
            }

        // There is nothing to do to stop the TabbedPane!
        // Stop all UIComponents on the tabs
        UIComponentHelper.stopAllTabComponents(getTabbedPane());
        }


    /***********************************************************************************************
     * Dispose of the ObservatoryMonitorInstrumentPanel.
     */

    public final void disposeUI()
        {
        PORT_CONTROLLER.removeCommandLifecycleListener((CommandLifecycleUIComponentInterface) getCommandMonitorTab());
        PORT_CONTROLLER.removePortMessageListener((PortMessageListener) getPortMonitorTab());

        if (timerMemory != null)
            {
            // Stop the timer
            timerMemory.stop();
            timerMemory = null;
            }

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
     * Reset the ObservatoryInstrumentUIComponent.
     *
     * @param resetmode
     */

    public void reset(final ResetMode resetmode)
        {
        final String SOURCE = "ObservatoryMonitorInstrumentPanel.reset() ";

        super.reset(resetmode);

        if (ResetMode.DEFAULTS.equals(resetmode))
            {
            // CommandMonitorTab
            if ((getCommandMonitorTab() != null)
                && (getCommandMonitorTab() instanceof CommandLifecycleUIComponentInterface))
                {
                ((CommandLifecycleUIComponentInterface)getCommandMonitorTab()).getCommandLifecycleEntries().clear();

                // Force an immediate update of the Report only if visible
                if ((getCommandMonitorTab() instanceof ReportTablePlugin)
                    && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getCommandMonitorTab())))
                    {
                    ((ReportTablePlugin) getCommandMonitorTab()).refreshTable();
                    }
                }

            // PortMonitorTab
            // ToDo could use an interface
            if ((getPortMonitorTab() != null)
                && (getPortMonitorTab() instanceof PortMonitorUIComponent))
                {
                ((PortMonitorUIComponent)getPortMonitorTab()).getPortMessageEntries().clear();

                // Force an immediate update of the Report
                // Refresh only if visible
                if ((getPortMonitorTab() instanceof ReportTablePlugin)
                    && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getPortMonitorTab())))
                    {
                    ((ReportTablePlugin) getPortMonitorTab()).refreshTable();
                    }
                }

            // MemoryUsageTab
            if ((getMemoryMonitorTab() != null)
                && (getMemoryMonitorTab() instanceof ChartUIComponentPlugin))
                {
                getMemoryTimeSeries().clear();

                // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
                ((ChartUIComponentPlugin) getMemoryMonitorTab()).setChannelCount(DAO_CHANNEL_COUNT);
                ((ChartUIComponentPlugin) getMemoryMonitorTab()).setTemperatureChannel(false);
                ((ChartUIComponentPlugin) getMemoryMonitorTab()).setPrimaryXYDataset(getHostInstrument().getDAO(), null);

                // Leave the DAO alone
                ((ChartUIComponentPlugin) getMemoryMonitorTab()).setMetadata(null,
                                                                             getHostInstrument().getDAO(),
                                                                             false,
                                                                             LOADER_PROPERTIES.isMetadataDebug());

                // Force an immediate update of the Chart only if visible
                if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getMemoryMonitorTab()))
                    {
                    ((ChartUIComponentPlugin) getMemoryMonitorTab()).refreshChart(getHostInstrument().getDAO(),
                                                                                  UIComponentHelper.shouldRefresh(false,
                                                                                                                  getHostInstrument(),
                                                                                                                  getMemoryMonitorTab()),
                                                                                  SOURCE);
                    }
                }

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
        final String SOURCE = "ObservatoryMonitorInstrumentPanel.setWrappedData() ";
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
                && (UIComponentHelper.shouldRefresh(updatedata, getHostInstrument(), getEventLogTab())))
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
     * Create and initialise the JTabbedPane for the ObservatoryMonitorInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        final String SOURCE = "ObservatoryMonitorInstrumentPanel.createAndInitialiseTabs() ";

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

        // Log *all* Observatory Commands (indicated by Instrument==null),
        // but only if the ObservatoryMonitor Instrument is running
        tabCommandMonitor = new CommandLifecycleUIComponent(getHostTask(),
                                                            getHostInstrument(),
                                                            null,
                                                            REGISTRY.getFrameworkResourceKey(),
                                                            ReportTableToolbar.NONE);
        // Auto-scroll to the last entry
        ((ReportTablePlugin) getCommandMonitorTab()).setScrollToRow(-1);
        getTabbedPane().addTab(TAB_COMMAND_MONITOR,
                               (Component) getCommandMonitorTab());
        PORT_CONTROLLER.addCommandLifecycleListener((CommandLifecycleUIComponentInterface) getCommandMonitorTab());

        // Log *all* Observatory PortMessages (indicated by Instrument==null),
        // but only if the ObservatoryMonitor Instrument is running
        tabPortMonitor = new PortMonitorUIComponent(getHostTask(),
                                                    getHostInstrument(),
                                                    null,
                                                    REGISTRY.getFrameworkResourceKey());
        // Auto-scroll to the last entry
        ((ReportTablePlugin)getPortMonitorTab()).setScrollToRow(-1);
        getTabbedPane().addTab(TAB_PORT_MONITOR,
                               null,
                               (Component) getPortMonitorTab(),
                               InstrumentUIHelper.showTruncatedTooltip(getHostInstrument()));
        PORT_CONTROLLER.addPortMessageListener((PortMessageListener) getPortMonitorTab());

        tabMemoryMonitor = new MemoryUsageChartUIComponent(getHostTask(),
                                                           getHostInstrument(),
                                                           EMPTY_STRING,
                                                           ObservatoryMonitorHelper.createMemoryMonitorChannelMetadata(),
                                                           REGISTRY.getFrameworkResourceKey(),
                                                           DataUpdateType.PRESERVE,
                                                           REGISTRY.getIntegerProperty(getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX));
        getTabbedPane().addTab(TAB_MEMORY_MONITOR,
                               (Component) getMemoryMonitorTab());

        tabActions = new ActionsUIComponent(getHostTask(),
                                            REGISTRY.getFrameworkResourceKey());
        // Auto-scroll to the last entry
        ((ReportTablePlugin) getActionsTab()).setScrollToRow(-1);
        getTabbedPane().addTab(TAB_ACTIONS,
                               (Component) getActionsTab());

        InstrumentPanelTabFactory.addJavaConsoleTab(this,
                                                    TAB_JAVA_CONSOLE,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_OBSERVATORY_LOG,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addCommandLexiconTab(this,
                                                       TAB_COMMAND_LEXICON,
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
     * Get the Command ObservatoryMonitor.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getCommandMonitorTab()
        {
        return (this.tabCommandMonitor);
        }


    /***********************************************************************************************
     * Get the Command ObservatoryMonitor for use by the DAO.
     *
     * @return ReportTablePlugin
     */

    public ReportTablePlugin getCommandMonitor()
        {
        return ((ReportTablePlugin)getCommandMonitorTab());
        }


    /***********************************************************************************************
     * Get the Port ObservatoryMonitor.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getPortMonitorTab()
        {
        return (this.tabPortMonitor);
        }


    /***********************************************************************************************
     * Get the Port ObservatoryMonitor for use by the DAO.
     *
     * @return ReportTablePlugin
     */

    public ReportTablePlugin getPortMonitor()
        {
        return ((ReportTablePlugin)getPortMonitorTab());
        }


    /***********************************************************************************************
     * Get the Memory ObservatoryMonitor.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getMemoryMonitorTab()
        {
        return (this.tabMemoryMonitor);
        }


   /***********************************************************************************************
     * Get the ActionList.
     *
     * @return UIComponentPlugin
     */

    private UIComponentPlugin getActionsTab()
        {
        return (this.tabActions);
        }


    /***********************************************************************************************
      * Get the Actions for use by the DAO.
      *
      * @return ReportTablePlugin
      */

     public ReportTablePlugin getActions()
         {
         return ((ReportTablePlugin)getActionsTab());
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


    /***********************************************************************************************
     * Get the TimeSeriesCollection containing the MemoryUsage data.
     *
     * @return TimeSeriesCollection
     */

    private TimeSeriesCollection getTimeSeriesCollection()
        {
        return (this.collectionTimeSeries);
        }


    /***********************************************************************************************
     * Get the TimeSeries containing the MemoryUsage data.
     *
     * @return TimeSeries
     */

    private TimeSeries getMemoryTimeSeries()
        {
        return (this.seriesMemorySnapshots);
        }
    }
