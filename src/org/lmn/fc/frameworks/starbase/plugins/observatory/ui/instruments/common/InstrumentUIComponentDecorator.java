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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.AutoScaleChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.FixedRangeChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.LogLinChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.AbstractXYDatasetUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.RawDataUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.ui.userinterface.HeaderUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.widgets.IndicatorInterface;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * InstrumentUIComponentDecorator.
 */

public class InstrumentUIComponentDecorator extends UIComponent
                                            implements InstrumentUIComponentDecoratorInterface,
                                                       ObservatoryConstants
    {
    // Injections
    private ObservatoryInstrumentInterface observatoryInstrument;
    private Instrument instrumentXml;
    private ObservatoryUIInterface observatoryUI;
    private TaskPlugin pluginTask;
    private FontInterface pluginFont;
    private ColourInterface pluginColourForeground;
    private String strResourceKey;
    private final int intIndicatorCount;

    private boolean boolStarted;
    private DAOWrapperInterface daoWrapperInterface;
    private final List<ExportableComponentInterface> listExportableComponents;

    // UI Containers
    private HeaderUIComponent headerUIComponent;
    private JTabbedPane tabbedPane;

    // Control Panel Indicators
    private IndicatorInterface indicator0;
    private IndicatorInterface indicator1;
    private IndicatorInterface indicator2;
    private IndicatorInterface indicator3;
    private IndicatorInterface indicator4;
    private IndicatorInterface indicator5;
    private IndicatorInterface indicator6;
    private IndicatorInterface indicator7;

    private final List<IndicatorInterface> listIndicators;
    private final List<String> listDefaultValueKeys;
    private final List<SchemaUnits.Enum> listDefaultUnits;
    private final List<String> listDefaultTooltipKeys;

    // Instrument Panel Tabs
    private final List<String> listExportableTabs;
    private UIComponentPlugin tabCommands;
    private UIComponentPlugin tabChart;
    private UIComponentPlugin tabMeters;
    private UIComponentPlugin tabClocks;
    private UIComponentPlugin tabSerialConfiguration;
    private UIComponentPlugin tabJavaConsole;
    private UIComponentPlugin tabJythonConsole;
    private UIComponentPlugin tabJythonEditor;
    private UIComponentPlugin tabHexEditor;
    private UIComponentPlugin tabAudioExplorer;
    private UIComponentPlugin tabImage;
    private UIComponentPlugin tabSuperposedDataAnalyser;
    private UIComponentPlugin tabProcessedData;
    private UIComponentPlugin tabRawData;
    private UIComponentPlugin tabRegionalMap;
    private UIComponentPlugin tabTimeZones;
    private UIComponentPlugin tabStarMap;
    private UIComponentPlugin tabEphemerides;
    private UIComponentPlugin tabNetworkScanner;
    private UIComponentPlugin tabInstrumentLog;
    private UIComponentPlugin tabEventLog;
    private UIComponentPlugin tabMetadataExplorer;
    private UIComponentPlugin tabConfiguration;
    private UIComponentPlugin tabMantis;
    private UIComponentPlugin tabSubversion;
    private UIComponentPlugin tabJenkins;
    private UIComponentPlugin tabNewsreader;
    private UIComponentPlugin tabCommandLexicon;
    private UIComponentPlugin tabXML;
    private UIComponentPlugin tabHelp;
    private UIComponentPlugin tabManual;
    private UIComponentPlugin tabPublisher;


    /***********************************************************************************************
     * Construct a InstrumentUIComponentDecorator.
     * Intended for Instrument Panel Tabs, i.e. no Control Panel Indicators.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public InstrumentUIComponentDecorator(final ObservatoryInstrumentInterface instrument,
                                          final Instrument instrumentxml,
                                          final ObservatoryUIInterface hostui,
                                          final TaskPlugin task,
                                          final FontInterface font,
                                          final ColourInterface colour,
                                          final String resourcekey)
        {
        super();

        if ((instrument == null)
            || (instrumentxml == null)
            || (!XmlBeansUtilities.isValidXml(instrumentxml))
            || (hostui == null)
            || (task == null)
            || (!task.validatePlugin())
            || (font == null)
            || (colour == null)
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey.trim())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        this.observatoryInstrument = instrument;
        this.instrumentXml = instrumentxml;
        this.observatoryUI = hostui;
        this.pluginTask = task;
        this.pluginFont = font;
        this.pluginColourForeground = colour;
        this.strResourceKey = resourcekey;
        this.intIndicatorCount = 0;

        this.boolStarted = false;
        this.daoWrapperInterface = new DAOWrapper(null, null, EMPTY_STRING, instrument.getDAO());
        this.headerUIComponent = null;
        this.tabbedPane = null;
        this.listExportableComponents = new ArrayList<ExportableComponentInterface>(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_MAX);
        InstrumentHelper.resetExportableComponents(this.listExportableComponents);

        // Use this list to select tabs for Parameters
        this.listExportableTabs = new ArrayList<String>(20);

        // Control Panel Indicators
        this.indicator0 = null;
        this.indicator1 = null;
        this.indicator2 = null;
        this.indicator3 = null;
        this.indicator4 = null;
        this.indicator5 = null;
        this.indicator6 = null;
        this.indicator7 = null;

        this.listIndicators = new ArrayList<IndicatorInterface>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);
        this.listDefaultValueKeys = new ArrayList<String>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);
        this.listDefaultUnits = new ArrayList<SchemaUnits.Enum>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);
        this.listDefaultTooltipKeys = new ArrayList<String>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);

        // Instrument Panel Tabs
        this.tabCommands = null;
        this.tabChart = null;
        this.tabMeters = null;
        this.tabClocks = null;
        this.tabSerialConfiguration = null;
        this.tabJavaConsole = null;
        this.tabJythonConsole = null;
        this.tabJythonEditor = null;
        this.tabHexEditor = null;
        this.tabAudioExplorer = null;
        this.tabImage = null;
        this.tabSuperposedDataAnalyser = null;
        this.tabProcessedData = null;
        this.tabRawData = null;
        this.tabRegionalMap = null;
        this.tabTimeZones = null;
        this.tabStarMap = null;
        this.tabEphemerides = null;
        this.tabNetworkScanner = null;
        this.tabInstrumentLog = null;
        this.tabEventLog = null;
        this.tabMetadataExplorer = null;
        this.tabConfiguration = null;
        this.tabMantis = null;
        this.tabSubversion = null;
        this.tabJenkins = null;
        this.tabNewsreader = null;
        this.tabCommandLexicon = null;
        this.tabXML = null;
        this.tabHelp = null;
        this.tabManual = null;
        this.tabPublisher = null;
        }


    /***********************************************************************************************
     * Construct a InstrumentUIComponentDecorator.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colourforeground
     * @param resourcekey
     * @param indicatorcount
     */

    public InstrumentUIComponentDecorator(final ObservatoryInstrumentInterface instrument,
                                          final Instrument instrumentxml,
                                          final ObservatoryUIInterface hostui,
                                          final TaskPlugin task,
                                          final FontInterface font,
                                          final ColourInterface colourforeground,
                                          final String resourcekey,
                                          final int indicatorcount)
        {
        super();

        if ((instrument == null)
            || (instrumentxml == null)
            || (!XmlBeansUtilities.isValidXml(instrumentxml))
            || (hostui == null)
            || (task == null)
            || (!task.validatePlugin())
            || (font == null)
            || (colourforeground == null)
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey.trim())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        this.observatoryInstrument = instrument;
        this.instrumentXml = instrumentxml;
        this.observatoryUI = hostui;
        this.pluginTask = task;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.strResourceKey = resourcekey;
        this.intIndicatorCount = indicatorcount;

        this.boolStarted = false;
        this.daoWrapperInterface = new DAOWrapper(null, null, EMPTY_STRING, instrument.getDAO());
        this.headerUIComponent = null;
        this.tabbedPane = null;
        this.listExportableComponents = new ArrayList<ExportableComponentInterface>(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_MAX);
        InstrumentHelper.resetExportableComponents(this.listExportableComponents);

        // Use this list to select tabs for Parameters
        this.listExportableTabs = new ArrayList<String>(20);

        // Control Panel Indicators
        this.indicator0 = null;
        this.indicator1 = null;
        this.indicator2 = null;
        this.indicator3 = null;
        this.indicator4 = null;
        this.indicator5 = null;
        this.indicator6 = null;
        this.indicator7 = null;

        this.listIndicators = new ArrayList<IndicatorInterface>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);
        this.listDefaultValueKeys = new ArrayList<String>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);
        this.listDefaultUnits = new ArrayList<SchemaUnits.Enum>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);
        this.listDefaultTooltipKeys = new ArrayList<String>(ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS);

        // Instrument Panel Tabs
        this.tabCommands = null;
        this.tabChart = null;
        this.tabMeters = null;
        this.tabClocks = null;
        this.tabSerialConfiguration = null;
        this.tabJavaConsole = null;
        this.tabJythonConsole = null;
        this.tabJythonEditor = null;
        this.tabHexEditor = null;
        this.tabAudioExplorer = null;
        this.tabImage = null;
        this.tabSuperposedDataAnalyser = null;
        this.tabProcessedData = null;
        this.tabRawData = null;
        this.tabRegionalMap = null;
        this.tabTimeZones = null;
        this.tabStarMap = null;
        this.tabEphemerides = null;
        this.tabNetworkScanner = null;
        this.tabInstrumentLog = null;
        this.tabEventLog = null;
        this.tabMetadataExplorer = null;
        this.tabConfiguration = null;
        this.tabMantis = null;
        this.tabSubversion = null;
        this.tabJenkins = null;
        this.tabNewsreader = null;
        this.tabCommandLexicon = null;
        this.tabXML = null;
        this.tabHelp = null;
        this.tabPublisher = null;
        this.tabManual = null;
        }


    /***********************************************************************************************
     * Initialise the InstrumentUIComponentDecorator.
     */

    public void initialiseUI()
        {
        final Border border;

        super.initialiseUI();

        InstrumentHelper.resetExportableComponents(getExportableComponents());
        resetControlPanelIndicators();
        removeAll();

        // Configure the host UIComponent JPanel
        setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.Y_AXIS));
        setMinimumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(MAX_UNIVERSE, MAX_UNIVERSE));
        setMaximumSize(new Dimension(MAX_UNIVERSE, MAX_UNIVERSE));
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // Create a Header
        border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                                                    BorderFactory.createEmptyBorder(3, 10, 0, 10));

        this.headerUIComponent = new HeaderUIComponent();
        getHeaderUIComponent().setBorder(border);
        getHeaderUIComponent().setMinimumSize(new Dimension(MAX_UNIVERSE, RackPanel.getInstrumentHeaderU().getPixelHeight()));
        getHeaderUIComponent().setPreferredSize(new Dimension(MAX_UNIVERSE, RackPanel.getInstrumentHeaderU().getPixelHeight()));
        getHeaderUIComponent().setMaximumSize(new Dimension(MAX_UNIVERSE, RackPanel.getInstrumentHeaderU().getPixelHeight()));
        getHeaderUIComponent().setAlignmentX(LEFT_ALIGNMENT);

        // Add the Header to the host UIComponent
        this.add(getHeaderUIComponent());
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        SwingUtilities.updateComponentTreeUI(this);

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

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of the UIComponent.
     */

    public void disposeUI()
        {
        if (getTabbedPane() != null)
            {
            // Reduce resources as far as possible
            if ((getMetadataExplorerTab() != null)
                && (getMetadataExplorerTab() instanceof MetadataExplorerFrameUIComponentInterface)
                && (((MetadataExplorerFrameUIComponentInterface) getMetadataExplorerTab()).getMetadataExplorerUI() != null)
                && (((MetadataExplorerFrameUIComponentInterface) getMetadataExplorerTab()).getMetadataExplorerUI().getTheLeafUI() != null))
                {
                ((MetadataExplorerFrameUIComponentInterface) getMetadataExplorerTab()).getMetadataExplorerUI().getTheLeafUI().removeMetadataChangedListener(this);
                }

            UIComponentHelper.disposeAllTabComponents(getTabbedPane());
            getTabbedPane().removeAll();
            setTabbedPane(null);
            removeAll();
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Get the Instrument Xml.
     *
     * @return Instrument
     */

    public final Instrument getInstrument()
        {
        return (this.instrumentXml);
        }


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    public TaskPlugin getHostTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    public FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    public ColourInterface getColourData()
        {
        return (this.pluginColourForeground);
        }


    /**********************************************************************************************
     * Get the ColourDataType.
     * ToDo Refactor to remove getColourData().
     *
     * @return ColourPlugin
     */

    public ColourInterface getForegroundColour()
        {
        return (this.pluginColourForeground);
        }


    /**********************************************************************************************
     * Get the Background Colour.
     * ToDo Refactor to implement a variable.
     *
     * @return ColourInterface
     */

    public ColourInterface getBackgroundColour()
        {
        return (DEFAULT_COLOUR_TAB_BACKGROUND);
        }


    /***********************************************************************************************
     * Get the Header UIComponent.
     *
     * @return HeaderUIComponent
     */

    public HeaderUIComponent getHeaderUIComponent()
        {
        return (this.headerUIComponent);
        }


    /***********************************************************************************************
     * Get the JTabbedPane underlying this UI, if used (null if not).
     *
     * @return JTabbedPane
     */

    public JTabbedPane getTabbedPane()
        {
        return (this.tabbedPane);
        }


    /***********************************************************************************************
     * Set the JTabbedPane.
     *
     * @param tabbedpane
     */

    public void setTabbedPane(final JTabbedPane tabbedpane)
        {
        this.tabbedPane = tabbedpane;
        }


    /***********************************************************************************************
     * Configure the TabbedPane with default colours and font.
     */

    public void configureTabbedPane()
        {
        getTabbedPane().setFont(getFontData().getFont());
        getTabbedPane().setForeground(getColourData().getColor());
        getTabbedPane().setBackground(Color.pink);
        getTabbedPane().setAlignmentX(Component.LEFT_ALIGNMENT);
        }


    /***********************************************************************************************
     * Get the List of ExportableComponents which may be exported.
     *
     * @return List<ExportableComponentInterface>
     */

    public final List<ExportableComponentInterface> getExportableComponents()
        {
        return (this.listExportableComponents);
        }


    /***********************************************************************************************
     * Get the specified ExportableComponent which may be exported.
     *
     * @param index
     *
     * @return ExportableComponentInterface
     */

    public final ExportableComponentInterface getExportableComponent(final int index)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.getExportableComponent() ";

        if ((getExportableComponents() != null)
            && (index >= 0)
            && (index < getExportableComponents().size())
            && (index < ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_MAX))
            {
            return (getExportableComponents().get(index));
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to get ExportableComponent [index=" + index + "]");
            return (null);
            }
        }


    /***********************************************************************************************
     * Set an ExportableComponent which may be exported, at the specified Index.
     *
     * @param index
     * @param component
     */

    public final void setExportableComponent(final int index,
                                             final ExportableComponentInterface component)
        {
        final String SOURCE = "InstrumentUIComponentDecorators.setExportableComponent() ";

        if ((getExportableComponents() != null)
            && (component != null)
            && (index < getExportableComponents().size())
            && (index < ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_MAX))
            {
            getExportableComponents().set(index, component);
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to add ExportableComponent [index=" + index + "]");
            }
        }


    /***********************************************************************************************
     * Get the List of ExportableTabs.
     *
     * @return List<String>
     */

    public final List<String> getExportableTabs()
        {
        return (this.listExportableTabs);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Initialise the ObservatoryInstrumentUIComponent.
     */

    public void initialise()
        {

        }


    /***********************************************************************************************
     * Start the ObservatoryInstrumentUIComponent.
     *
     * @return boolean
     */

    public boolean start()
        {
        this.boolStarted = true;

        return (isStarted());
        }


    /***********************************************************************************************
     * Stop the ObservatoryInstrumentUIComponent.
     *
     * @return boolean
     */

    public boolean stop()
        {
        this.boolStarted = false;

        return (!isStarted());
        }


    /***********************************************************************************************
     * Indicate if the Instrument is in the Started state.
     *
     * @return boolean
     */

    public final boolean isStarted()
        {
        return (this.boolStarted);
        }


    /***********************************************************************************************
     * Shutdown the ObservatoryInstrumentUIComponent after use.
     */

    public void dispose()
        {
        if (getExportableComponents() != null)
            {
            getExportableComponents().clear();
            }
        }


    /***********************************************************************************************
     * Reset the ObservatoryInstrumentUIComponent.
     *
     * @param resetmode
     */

    public void reset(final ResetMode resetmode)
        {
        LOGGER.debug((LOADER_PROPERTIES.isStaribusDebug() || LOADER_PROPERTIES.isChartDebug()),
                     "InstrumentUIComponentDecorator.reset() --> Reset Instrument Panel");
        }


    /**********************************************************************************************/
    /* DAO Helper methods                                                                         */
    /***********************************************************************************************
     * Get the data produced by the DAO.
     * Return null if not applicable or not available.
     *
     * @return DAOWrapper
     */

    public DAOWrapperInterface getWrappedData()
        {
        return (this.daoWrapperInterface);
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     * This is used only on ControlPanels, InstrumentPanels and their UIComponents.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatedata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.setWrappedData() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug());

        this.daoWrapperInterface = daowrapper;

        // This is the simplest way!
        if (getWrappedData() == null)
            {
            return;
            }

        LOGGER.debug(boolDebug,
                     SOURCE + "Setting wrapped data [instrument.dataconsumer=" + getWrappedData().getWrappedDAO().isInstrumentDataConsumer()
                         + "] [decorator.class=" + getClass().getName()
                         + "] [dao.class=" + getWrappedData().getWrappedDAO().getClass().getName()
                         + "] [dao.isinstrumentdataconsumer=" + (getWrappedData().getWrappedDAO().isInstrumentDataConsumer())
                         + "]");

        LOGGER.debug(boolDebug,
                     SOURCE + "Setting wrapped data "
                         + "  [forcerefreshdata=" + updatedata
                         + "] [updatemetadata=" + updatemetadata
                         + "] [wrapper_channel_count=" + getWrappedData().getRawDataChannelCount()
                         + "] [userobject.isephemeris=" + EphemeridesHelper.isUserObjectAnEphemeris(getWrappedData().getUserObject())
                         + "]");

        if (getWrappedData().getWrappedDAO() != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + " [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(getWrappedData().getWrappedDAO().getResponseMessageStatusList())
                                + "] [execution.status=" + getWrappedData().getWrappedDAO().getExecutionStatus().getName()
                                + "]");
            }

        //------------------------------------------------------------------------------------------
        // DATA UPDATES
        // These affect Metadata, Image, RawData, ProcessedData, Chart
        //------------------------------------------------------------------------------------------
        // Do NOT use the data provided by a Child DAO (e.g. BasicInstrumentChildDAO),
        // since this is intended for use elsewhere, usually on a dedicated panel

        if (getWrappedData().getWrappedDAO().isInstrumentDataConsumer())
            {
            final boolean boolRawDataChanged;

            // Metadata
            // Do this first, to avoid a double refresh of the Chart

            if (updatemetadata)
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "--> addAggregateWrapperMetadata()");
                addAggregateWrapperMetadata(daowrapper);
                }
            else
                {
                LOGGER.debugTimedEvent(boolDebug,
                                       SOURCE + "Aggregate Metadata will NOT be updated");
                }

            //------------------------------------------------------------------------------------------
            // ImageData

            setImage(getWrappedData().getImageData(),
                     updatedata,
                     boolDebug);

            //------------------------------------------------------------------------------------------
            // RawData

            // Do this only if the RawData do not represent an Ephemeris or NetworkScan
            boolRawDataChanged = setRawData(getWrappedData().getRawData(),
                                            getWrappedData().getRawDataChannelCount(),
                                            updatedata,
                                            boolDebug);

            //------------------------------------------------------------------------------------------
            // XYDataset

            // This will also refresh the Chart if it is of the appropriate DataType
            // and it is on the ChartTab, but only if the RawData have changed

            if ((boolRawDataChanged)
                || ((getWrappedData().getWrappedDAO() != null)
                    && getWrappedData().getWrappedDAO().isProcessedDataChanged()))
                {
                setXyDataset(getWrappedData().getWrappedDAO(),
                             getWrappedData().getXYDataset(),
                             getWrappedData().getRawDataChannelCount(),
                             getWrappedData().hasTemperatureChannel(),
                             updatedata,
                             boolDebug);
                }
            else
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "XyDataset NOT updated");
                }
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Data from DAO NOT passed to Instrument [dao.class="
                                + getWrappedData().getWrappedDAO().getClass().getName() + "]");
            }

        //------------------------------------------------------------------------------------------
        // END OF DATA UPDATES
        //------------------------------------------------------------------------------------------
        // Now handle specific tabs on the InstrumentPanel
        // This isn't ideal, but we need to make progress :-)
        //------------------------------------------------------------------------------------------
        // Update the PointOfInterest on the map, if any

        // Force an immediate update
        // Refresh only if visible
        if (UIComponentHelper.shouldRefresh(updatedata, getHostInstrument(), getRegionalMapTab()))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Refresh the RegionalMap Tab");
            getRegionalMapTab().validate();
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Data are inappropriate for a RegionalMap");
            }

        //------------------------------------------------------------------------------------------
        // Update the Ephemeris, if any
        // EphemerisFrameUIComponent is a UIComponent

        if ((getEphemeridesTab() != null)
            && (getEphemeridesTab() instanceof EphemerisFrameUIComponentInterface)
            && (((EphemerisFrameUIComponentInterface)getEphemeridesTab()).getEphemerisUI() != null)
            && (EphemeridesHelper.isUserObjectAnEphemeris(daowrapper.getUserObject())))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on EphemerisFrameUIComponent");
            ((EphemerisFrameUIComponentInterface)getEphemeridesTab()).setWrappedData(daowrapper, updatemetadata);

            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on EphemerisUIComponent");
            ((EphemerisFrameUIComponentInterface)getEphemeridesTab()).getEphemerisUI().setWrappedData(daowrapper, updatemetadata);
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Data are inappropriate for an Ephemeris");
            }

        //------------------------------------------------------------------------------------------
        // Update the Starinet Network Scanner, if any

        if ((getNetworkScannerTab() != null)
            && (getNetworkScannerTab() instanceof NetworkScannerFrameUIComponent)
            && (((NetworkScannerFrameUIComponent)getNetworkScannerTab()).getNetworkScannerPanel() != null)
            && (NetworkScannerHelper.isUserObjectNetworkScan(daowrapper.getUserObject())))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on NetworkScannerFrameUIComponent");
            ((NetworkScannerFrameUIComponent)getNetworkScannerTab()).setWrappedData(daowrapper, updatemetadata);

            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on NetworkScannerUIComponent");
            ((NetworkScannerFrameUIComponent)getNetworkScannerTab()).getNetworkScannerPanel().setWrappedData(daowrapper, updatemetadata);
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Data are inappropriate for a NetworkScanner");
            }

        //------------------------------------------------------------------------------------------
        // Update the Communicator Newsreader, if any

        if ((getNewsreaderTab() != null)
            && (getNewsreaderTab() instanceof InstrumentUIComponentDecoratorInterface))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on NewsreaderTab");
            // The Newsreader UIComponent is a *tab*, not the InstrumentPanel (even though it is a Decorator)
            // This is the only call to the Newsreader's setWrappedData()
            ((InstrumentUIComponentDecoratorInterface)getNewsreaderTab()).setWrappedData(daowrapper,
                                                                                         updatedata,
                                                                                         updatemetadata);
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Data are inappropriate for a Newsreader");
            }

        //------------------------------------------------------------------------------------------
        // Update the Superposed Data Analyser, if any

        if ((getSuperposedDataAnalyserTab() != null)
            && (getSuperposedDataAnalyserTab() instanceof InstrumentUIComponentDecoratorInterface))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on SuperposedDataAnalyserTab");
            ((InstrumentUIComponentDecoratorInterface) getSuperposedDataAnalyserTab()).setWrappedData(daowrapper,
                                                                                                      updatedata,
                                                                                                      updatemetadata);
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Data are inappropriate for Superposed Data Analyser");
            }

        //------------------------------------------------------------------------------------------
        // Update the InstrumentLog, if any

        if ((getWrappedData().getInstrumentLogFragment() != null)
            && (!getWrappedData().getInstrumentLogFragment().isEmpty())
            && (getInstrumentLogTab() != null))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on InstrumentLogFragment");

            getHostInstrument().addInstrumentLogFragment(getWrappedData().getInstrumentLogFragment());

            // Refresh only if visible
            if (UIComponentHelper.shouldRefresh(updatedata, getHostInstrument(), getInstrumentLogTab()))
                {
                ((ReportTablePlugin) getInstrumentLogTab()).refreshTable();
                }
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Unable to update InstrumentLog");
            }

        //------------------------------------------------------------------------------------------
        // Update the EventLogs

        if ((getHostInstrument() != null)
            && (getHostInstrument().getHostUI() != null)
            && (getHostInstrument().getHostUI().getObservatoryLog() != null)
            && (getHostInstrument().getHostUI().getObservatoryLog().getInstrumentPanel() != null))
            {
            // Add the EventLogFragment to the ObservatoryLog, which is a tab on the ObservatoryMonitor Instrument
            // so just inform the InstrumentPanel of the ObservatoryMonitor
            getHostInstrument().getHostUI().getObservatoryLog().getInstrumentPanel().setWrappedData(getWrappedData(),
                                                                                                    updatedata,
                                                                                                    updatemetadata);
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Unable to update ObservatoryLog");
            }

        if ((getWrappedData().getEventLogFragment() != null)
            && (!getWrappedData().getEventLogFragment().isEmpty())
            && (getEventLogTab() != null))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Go to setWrappedData() on EventLogFragment");

            // Handle the EventLog for this Instrument
            getHostInstrument().addEventLogFragment(getWrappedData().getEventLogFragment());

            // Now remove the fragment, since it has been used
            getWrappedData().getEventLogFragment().clear();
            getWrappedData().getWrappedDAO().getEventLogFragment().clear();

            // Refresh only if visible
            if (UIComponentHelper.shouldRefresh(updatedata, getHostInstrument(), getEventLogTab()))
                {
                ((ReportTablePlugin) getEventLogTab()).refreshTable();
                }
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Unable to update EventLog");
            }

        // Something has changed, we may need to update indicators etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());
        }


    /***********************************************************************************************
     * Add the Metadata retrieved along with the data.
     * This refreshes the MetadataExplorer tab.
     *
     * @param wrapper
     */

    private void addAggregateWrapperMetadata(final DAOWrapperInterface wrapper)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.addAggregateWrapperMetadata() ";
        List<Metadata> listAggregateDAOMetadata;
        final boolean boolDebug;

        boolDebug = LOADER_PROPERTIES.isMetadataDebug()
                    || LOADER_PROPERTIES.isChartDebug();

        listAggregateDAOMetadata = new ArrayList<Metadata>(100);

        if ((getHostInstrument() != null)
            && (getHostInstrument().getContext() != null))
            {
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + " --> MetadataHelper.collectAggregateMetadataTraced");

            listAggregateDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                     getHostInstrument().getContext().getObservatory(),
                                                                                     getHostInstrument(),
                                                                                     wrapper.getWrappedDAO(),
                                                                                     wrapper,
                                                                                     SOURCE,
                                                                                     boolDebug);
            // Set the aggregate Metadata on the host Instrument
            // All e.g. Control panel data are taken from here
            // NOTE THAT The DAO data take precedence over those in the Wrapper
            getHostInstrument().setAggregateMetadata(listAggregateDAOMetadata);

            MetadataHelper.showMetadataList(getHostInstrument().getAggregateMetadata(),
                                            "Aggregate Metadata, including from Wrapper",
                                            boolDebug);
            }

        // Deal with all Chart types
        if ((getChartTab() != null)
            && (getChartTab() instanceof ChartUIComponentPlugin)
            && (wrapper.getWrappedDAO() != null))
            {
            // The Metadata came from the Instrument DAO, so don't re-apply
            ((ChartUIComponentPlugin) getChartTab()).setMetadata(listAggregateDAOMetadata,
                                                                 wrapper.getWrappedDAO(),
                                                                 false,
                                                                 boolDebug);
            }

        // Publish to the MetadataExplorer
        // This will refresh the table if it is currently visible
        if ((getMetadataExplorerTab() != null)
            && (getMetadataExplorerTab() instanceof MetadataExplorerFrameUIComponentInterface))
            {
            // This resets the selection to the root
            ((MetadataExplorerFrameUIComponentInterface)getMetadataExplorerTab()).setMetadataList(listAggregateDAOMetadata);
            }

        // Even the ImageTab needs to know!
        if ((getImageTab() != null)
            && (getImageTab() instanceof ImageUIComponentInterface))
            {
            ((ImageUIComponentInterface) getImageTab()).setMetadata(listAggregateDAOMetadata);
            }

        // And the MapTab needs to know, just in case POIs and LOIs have changed
        if ((getRegionalMapTab() != null)
            && (getRegionalMapTab() instanceof MapUIComponentPlugin))
            {
            ((MapUIComponentPlugin) getRegionalMapTab()).collectPOIandLOI();
            }
        }


    /***********************************************************************************************
     * Set and refresh the Image to be displayed on the Image tab.
     *
     * @param image
     * @param refreshdata
     * @param debug
     */

    private void setImage(final Image image,
                          final boolean refreshdata,
                          final boolean debug)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.setImage() ";

        if ((getImageTab() != null)
           && (getImageTab() instanceof ImageUIComponentInterface))
            {
            ((ImageUIComponentInterface) getImageTab()).setImage(image);
            ((ImageUIComponentInterface) getImageTab()).refreshImage();

            LOGGER.debug(debug,
                         SOURCE + "Image updated [image.isnull=" + (image == null) + "]");
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "Unable to update Image");
            }
        }


     /***********************************************************************************************
      * Set and refresh the Vector of data to be displayed on the RawData tab,
      * but only if the RawData do not represent an Ephemeris or a Network Scan.
      * Return a flag to indicate if the RawData were changed.
      *
      * @param rawdata
      * @param channelcount
      * @param forcerefreshdata
      * @param debug
      *
      * @return boolean
      */

     private boolean setRawData(final Vector<Object> rawdata,
                                final int channelcount,
                                final boolean forcerefreshdata,
                                final boolean debug)
         {
         final String SOURCE = "InstrumentUIComponentDecorator.setRawData() ";
         boolean boolChanged;

         boolChanged = false;

         LOGGER.debug(debug,
                      SOURCE + "[channelcount=" + channelcount
                        + "] [forcerefreshdata=" + forcerefreshdata + "]");

         if ((getRawDataTab() != null)
             && (!EphemeridesHelper.isUserObjectAnEphemeris(rawdata))
             && (!NetworkScannerHelper.isUserObjectNetworkScan(rawdata)))
             {
             LOGGER.debug(debug,
                          SOURCE + "Prepare to update RawDataUIComponent");

             // This will set the ChannelCount used by the tab UIComponent
             ((RawDataUIComponent) getRawDataTab()).setRawData(rawdata, channelcount);
             boolChanged = true;

             // Force an immediate update using the new data
             // Refresh only if visible
             if ((getRawDataTab() instanceof ReportTablePlugin)
                 && (UIComponentHelper.shouldRefresh(forcerefreshdata, getHostInstrument(), getRawDataTab())))
                 {
                 LOGGER.debug(debug,
                              SOURCE + "--> RawDataTab refreshTable()");

                 ((ReportTablePlugin) getRawDataTab()).refreshTable();
                 }
             else
                 {
                 LOGGER.debug(debug,
                              SOURCE + "Cannot refresh RawDataTab [rawdata.instanceof="
                                  + (getRawDataTab() instanceof ReportTablePlugin)
                                  + "] [should.refresh=" + (UIComponentHelper.shouldRefresh(forcerefreshdata, getHostInstrument(), getRawDataTab())) + "]");
                 }
             }
         else
             {
             LOGGER.debug(debug,
                          SOURCE + "Unable to update RawData");
             }

         return (boolChanged);
         }


    /***********************************************************************************************
     * Set and refresh the XYDataset to be displayed on the Chart and the ProcessedData tab.
     *
     * @param dao
     * @param dataset
     * @param channelcount
     * @param temperaturechannel
     * @param forcerefreshdata
     * @param debug
     */

    private void setXyDataset(final ObservatoryInstrumentDAOInterface dao,
                              final XYDataset dataset,
                              final int channelcount,
                              final boolean temperaturechannel,
                              final boolean forcerefreshdata,
                              final boolean debug)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.setXyDataset() ";

        LOGGER.debug(debug,
                     SOURCE + "[channelcount=" + channelcount
                     + "] [temperaturechannel=" + temperaturechannel
                     + "] [forcerefreshdata=" + forcerefreshdata + "]");

        // Unsupported Chart types must be set and refreshed in subclasses
        // e.g. GOESChartUIComponent

        // ToDo This isn't very a good way of doing it :-)
        // Allow null datasets following a reset()
        if (getChartTab() != null)
            {
            if (getChartTab() instanceof AutoScaleChartUIComponent)
                {
                LOGGER.debug(debug,
                             SOURCE + "Prepare to update instanceof AutoScaleChartUIComponent");

                // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
                ((AutoScaleChartUIComponent) getChartTab()).setChannelCount(channelcount);
                ((AutoScaleChartUIComponent) getChartTab()).setTemperatureChannel(temperaturechannel);
                ((AutoScaleChartUIComponent) getChartTab()).setPrimaryXYDataset(dao, dataset);
                }
            else if (getChartTab() instanceof FixedRangeChartUIComponent)
                {
                LOGGER.debug(debug,
                             SOURCE + "Prepare to update instanceof FixedRangeChartUIComponent");

                // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
                ((FixedRangeChartUIComponent) getChartTab()).setChannelCount(channelcount);
                ((FixedRangeChartUIComponent) getChartTab()).setTemperatureChannel(temperaturechannel);
                ((FixedRangeChartUIComponent) getChartTab()).setPrimaryXYDataset(dao, dataset);
                }
            else if (getChartTab() instanceof LogLinChartUIComponent)
                {
                LOGGER.debug(debug,
                             SOURCE + "Prepare to update instanceof LogLinChartUIComponent");

                // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
                ((LogLinChartUIComponent) getChartTab()).setChannelCount(channelcount);
                ((LogLinChartUIComponent) getChartTab()).setTemperatureChannel(temperaturechannel);
                ((LogLinChartUIComponent) getChartTab()).setPrimaryXYDataset(dao, dataset);
                }

            // Force an immediate update of the Chart
            if ((getChartTab() instanceof ChartUIComponentPlugin)
                && (UIComponentHelper.shouldRefresh(forcerefreshdata, getHostInstrument(), getChartTab())))
                {
                LOGGER.debug(debug,
                             SOURCE + "--> ChartTab refreshChart()");

                ((ChartUIComponentPlugin) getChartTab()).refreshChart(dao,
                                                                      UIComponentHelper.shouldRefresh(forcerefreshdata,
                                                                                                      getHostInstrument(),
                                                                                                      getChartTab()),
                                                                      SOURCE);
                }
            else
                {
                LOGGER.debug(debug,
                             SOURCE + "Cannot refresh ChartTab [chart.instanceof="
                             + (getChartTab() instanceof ChartUIComponentPlugin)
                             + "] [should.refresh=" + (UIComponentHelper.shouldRefresh(forcerefreshdata, getHostInstrument(), getChartTab())) + "]");
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "ChartTab is NULL");
            }

        // ProcessedData can always be updated here, since it also uses the XYDataset
        if (getProcessedDataTab() != null)
            {
            // This will set the ChannelCount used by the tab UIComponent
            ((AbstractXYDatasetUIComponent) getProcessedDataTab()).setXyDataset(dataset, channelcount);

            // Force an immediate update using the new data
            if ((getProcessedDataTab() instanceof ReportTablePlugin)
                && (UIComponentHelper.shouldRefresh(forcerefreshdata, getHostInstrument(), getProcessedDataTab())))
                {
                LOGGER.debug(debug,
                             SOURCE + "--> ProcessedDataTab refreshTable()");

                ((ReportTablePlugin) getProcessedDataTab()).refreshTable();
                }
            else
                {
                LOGGER.debug(debug,
                             SOURCE + "Cannot refresh ProcessedDataTab [processeddata.instanceof="
                                 + (getProcessedDataTab() instanceof ReportTablePlugin)
                                 + "] [should.refresh=" + (UIComponentHelper.shouldRefresh(forcerefreshdata, getHostInstrument(), getProcessedDataTab())) + "]");
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + "Unable to update ProcessedData");
            }
        }


    /***********************************************************************************************
     * Remove any Data associated with this Instrument's appearance on the UI,
     * on the InstrumentPanel. For instance, remove a Chart regardless of it being visible.
     */

    public void removeInstrumentIdentity()
        {
        final String SOURCE = "InstrumentUIComponentDecorator.removeInstrumentIdentity() " ;

        LOGGER.debug((LOADER_PROPERTIES.isMasterDebug() || LOADER_PROPERTIES.isChartDebug()),
                     SOURCE + "Removing Identity all relevant items on the ControlPanel");

        for (int intIndicatorIndex = 0;
             ((getControlPanelIndicators() != null)
                && (intIndicatorIndex < getControlPanelIndicators().size()));
             intIndicatorIndex++)
            {
            final IndicatorInterface indicator;

            // Handle each Indicator in turn
            indicator = getControlPanelIndicators().get(intIndicatorIndex);

            // The Instrument isn't doing anything, so don't display anything
            indicator.setValue(QUERY);
            indicator.setUnits(EMPTY_STRING);
            indicator.setStatus(QUERY);
            indicator.setToolTip(QUERY);
            }

        LOGGER.debug((LOADER_PROPERTIES.isMasterDebug() || LOADER_PROPERTIES.isChartDebug()),
                     SOURCE + "Removing Identity all relevant items on the InstrumentPanel");

        UIComponentHelper.removeIdentityOfAllTabComponents(getTabbedPane());

        // Something has changed, we may need to update indicators etc.
        InstrumentHelper.notifyInstrumentChanged(getHostInstrument());
        }


    /***********************************************************************************************
     * Update any logs from data in the DAO Wrapper.
     *
     * @param daowrapper
     */

    public void flushLogFragments(final DAOWrapperInterface daowrapper)
        {
        if (daowrapper != null)
            {
            // Handle the EventLog for the ObservatoryLog Instrument
            if ((getHostInstrument() != null)
                && (getHostInstrument().getHostUI() != null)
                && (getHostInstrument().getHostUI().getObservatoryLog() != null)
                && (getHostInstrument().getHostUI().getObservatoryLog().getInstrumentPanel() != null))
                {
                // Always update the Log, regardless of visibility
                // Don't update the Metadata
                getHostInstrument().getHostUI().getObservatoryLog().getInstrumentPanel().setWrappedData(daowrapper,
                                                                                                        true,
                                                                                                        false);
                }

            // Handle the EventLog for this Instrument
            if ((daowrapper.getEventLogFragment() != null)
                && (!daowrapper.getEventLogFragment().isEmpty()))
                {
                getHostInstrument().addEventLogFragment(daowrapper.getEventLogFragment());

                // Refresh only if visible
                if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getEventLogTab()))
                    {
                    ((ReportTablePlugin) getEventLogTab()).refreshTable();
                    }

                // Clear the log fragment
                daowrapper.getEventLogFragment().clear();
                }

            // Handle the InstrumentLog for this Instrument
            if ((daowrapper.getInstrumentLogFragment() != null)
                && (!daowrapper.getInstrumentLogFragment().isEmpty()))
                {
                getHostInstrument().addInstrumentLogFragment(daowrapper.getInstrumentLogFragment());

                // Refresh only if visible
                if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getInstrumentLogTab()))
                    {
                    ((ReportTablePlugin) getInstrumentLogTab()).refreshTable();
                    }

                // Clear the log fragment
                daowrapper.getInstrumentLogFragment().clear();
                }

            // Something has changed, we may need to update indicators etc.
            InstrumentHelper.notifyInstrumentChanged(getHostInstrument());
            }
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.instrumentChanged() ";

        if (event != null)
            {
            //LOGGER.debugTimedEvent("InstrumentUIComponentDecorator InstrumentState changed to --> " + event.getNextState().getName());
            }
        }


    /**********************************************************************************************
     * Indicate that the Metadata has changed.
     *
     * @param event
     */

    public void metadataChanged(final MetadataChangedEvent event)
        {
        final String SOURCE = "InstrumentUIComponentDecorator.metadataChanged() ";
        final boolean boolDebug;

        boolDebug = LOADER_PROPERTIES.isMetadataDebug()
                        || LOADER_PROPERTIES.isChartDebug();

        if ((event != null)
            && (getHostInstrument() != null)
            && (getHostInstrument().getDAO() != null)
            && (getHostInstrument().getDAO().isInstrumentDataConsumer()))
            {
            final List<Metadata> listAggregateDAOMetadata;

            // Redraw the Chart
            LOGGER.debug(boolDebug,
                         SOURCE + "[key=" + event.getMetadataKey()
                                + "] [value=" + event.getMetadataValue()
                                + "] [state=" + event.getItemState().getName()
                                + "]");

            // NOTE THAT The DAO data take precedence over those in the Wrapper
            listAggregateDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(null,
                                                                                     null,
                                                                                     null,
                                                                                     getHostInstrument().getDAO(),
                                                                                     null,
                                                                                     SOURCE,
                                                                                     boolDebug);
            getHostInstrument().getDAO().setMetadataChanged(true);

            if ((getChartTab() != null)
                && (getChartTab() instanceof ChartUIComponentPlugin))
                {
                // Reasons for complete rebuild
                //
                //  DatasetTypeChanged
                //  ChannelCountChanged
                //  ChannelSelectionChanged
                //  DatasetDomainChanged
                //
                // Reasons to leave Chart alone, just update the data
                //
                //  MetadataChanged
                //  RawDataChanged
                //  ProcessedDataChanged

                // The Metadata came from the Instrument DAO, so don't re-apply
                ((ChartUIComponentPlugin)getChartTab()).setMetadata(listAggregateDAOMetadata,
                                                                    getHostInstrument().getDAO(),
                                                                    false,
                                                                    boolDebug);
                // Refresh on a separate thread!
                ((ChartUIComponentPlugin)getChartTab()).refreshChart(getHostInstrument().getDAO(),
                                                                     UIComponentHelper.shouldRefresh(true,
                                                                                                     getHostInstrument(),
                                                                                                     getChartTab()),
                                                                     SOURCE);
                }

            // Publish to the MetadataExplorer
            // This will refresh the table if it is currently visible
            if ((getMetadataExplorerTab() != null)
                && (getMetadataExplorerTab() instanceof MetadataExplorerFrameUIComponentInterface))
                {
                // This resets the selection to the root
                ((MetadataExplorerFrameUIComponentInterface)getMetadataExplorerTab()).setMetadataList(listAggregateDAOMetadata);
                }

            // Force an immediate update using the new data
            // Refresh only if visible
            if ((getRawDataTab() != null)
                && (getRawDataTab() instanceof ReportTablePlugin)
                && (UIComponentHelper.shouldRefresh(true,
                                                    getHostInstrument(),
                                                    getRawDataTab())))
                {
                ((ReportTablePlugin) getRawDataTab()).refreshTable();
                }

            if ((getProcessedDataTab() != null)
                && (getProcessedDataTab() instanceof ReportTablePlugin)
                && (UIComponentHelper.shouldRefresh(true,
                                                    getHostInstrument(),
                                                    getProcessedDataTab())))
                {
                ((ReportTablePlugin) getProcessedDataTab()).refreshTable();
                }

            if ((getImageTab() != null)
                && (getImageTab() instanceof ImageUIComponentInterface)
                && (UIComponentHelper.shouldRefresh(true,
                                                    getHostInstrument(),
                                                    getImageTab())))
                {
                ((ImageUIComponentInterface) getImageTab()).refreshImage();
                }

            if ((getRegionalMapTab() != null)
                && (getRegionalMapTab() instanceof MapUIComponentPlugin)
                && (UIComponentHelper.shouldRefresh(true,
                                                    getHostInstrument(),
                                                    getRegionalMapTab())))
                {
                getRegionalMapTab().validate();
                }
            }
        }


    /**********************************************************************************************/
    /* Control Panel Indicators                                                                   */
    /***********************************************************************************************
     * Get the Control Panel Indicator 0.
     *
     * @return ToolbarIndicator
     */

    public IndicatorInterface getIndicator0()
        {
        return (this.indicator0);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 0.
     *
     * @param indicator
     */

    public void setIndicator0(final IndicatorInterface indicator)
        {
        this.indicator0 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 1.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator1()
        {
        return (this.indicator1);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 1.
     *
     * @param indicator
     */

    public void setIndicator1(final IndicatorInterface indicator)
        {
        this.indicator1 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 2.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator2()
        {
        return (this.indicator2);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 2.
     *
     * @param indicator
     */

    public void setIndicator2(final IndicatorInterface indicator)
        {
        this.indicator2 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 3.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator3()
        {
        return (this.indicator3);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 3.
     *
     * @param indicator
     */

    public void setIndicator3(final IndicatorInterface indicator)
        {
        this.indicator3 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 4.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator4()
        {
        return (this.indicator4);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 4.
     *
     * @param indicator
     */

    public void setIndicator4(final IndicatorInterface indicator)
        {
        this.indicator4 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 5.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator5()
        {
        return (this.indicator5);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 5.
     *
     * @param indicator
     */

    public void setIndicator5(final IndicatorInterface indicator)
        {
        this.indicator5 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 6.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator6()
        {
        return (this.indicator6);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 6.
     *
     * @param indicator
     */

    public void setIndicator6(final IndicatorInterface indicator)
        {
        this.indicator6 = indicator;
        }


    /***********************************************************************************************
     * Get the Control Panel Indicator 7.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getIndicator7()
        {
        return (this.indicator7);
        }


    /***********************************************************************************************
     * Set the Control Panel Indicator 7.
     *
     * @param indicator
     */

    public void setIndicator7(final IndicatorInterface indicator)
        {
        this.indicator7 = indicator;
        }


    /***********************************************************************************************
     * Get the ControlPanel Indicators.
     *
     * @return List<IndicatorInterface>
     */

    public List<IndicatorInterface> getControlPanelIndicators()
        {
        return (this.listIndicators);
        }


    /***********************************************************************************************
     * Get the number of display Indicators.
     *
     * @return int
     */

    public int getIndicatorCount()
        {
        return (this.intIndicatorCount);
        }


    /***********************************************************************************************
     * Reset the ControlPanel Indicators.
     */

    public void resetControlPanelIndicators()
        {
        // Control Panel Indicators
        setIndicator0(null);
        setIndicator1(null);
        setIndicator2(null);
        setIndicator3(null);
        setIndicator4(null);
        setIndicator5(null);
        setIndicator6(null);
        setIndicator7(null);

        getControlPanelIndicators().clear();
        getControlPanelIndicatorDefaultValueKeys().clear();
        getControlPanelIndicatorDefaultUnits().clear();
        getControlPanelIndicatorDefaultTooltipKeys().clear();
        }


    /***********************************************************************************************
     * Get the ControlPanel Indicator Default ValueKeys.
     *
     * @return List<String>
     */

    public List<String> getControlPanelIndicatorDefaultValueKeys()
        {
        return (this.listDefaultValueKeys);
        }


    /***********************************************************************************************
     * Get the ControlPanel Indicator Default Units.
     *
     * @return List<SchemaUnits.Enum>
     */

    public List<SchemaUnits.Enum> getControlPanelIndicatorDefaultUnits()
        {
        return (this.listDefaultUnits);
        }


    /***********************************************************************************************
     * Get the ControlPanel Indicator Default Tooltip Keys.
     *
     * @return List<String>
     */

    public List<String> getControlPanelIndicatorDefaultTooltipKeys()
        {
        return (this.listDefaultTooltipKeys);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    public synchronized ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }


    /**********************************************************************************************/
    /* Instrument Panel Tabs                                                                      */
    /***********************************************************************************************
     * Get the Commands.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getCommandsTab()
        {
        return (this.tabCommands);
        }


    /***********************************************************************************************
     * Set the Commands Tab.
     *
     * @param commands
     */

    public void setCommandsTab(final UIComponentPlugin commands)
        {
        this.tabCommands = commands;
        }


    /***********************************************************************************************
     * Get the Chart.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getChartTab()
        {
        return (this.tabChart);
        }


    /***********************************************************************************************
     * Set the Chart Tab.
     *
     * @param chart
     */

    public void setChartTab(final UIComponentPlugin chart)
        {
        this.tabChart = chart;
        }


    /***********************************************************************************************
     * Get the DigitalPanelMeters.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getMetersTab()
        {
        return (this.tabMeters);
        }


    /***********************************************************************************************
     * Set the Meters Tab.
     *
     * @param meters
     */

    public void setMetersTab(final UIComponentPlugin meters)
        {
        this.tabMeters = meters;
        }


    /***********************************************************************************************
     * Get the Clocks.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getClocksTab()
        {
        return (this.tabClocks);
        }


    /***********************************************************************************************
     * Set the Clocks Tab.
     *
     * @param clocks
     */

    public void setClocksTab(final UIComponentPlugin clocks)
        {
        this.tabClocks = clocks;
        }


    /***********************************************************************************************
     * Get the SerialConfiguration Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getSerialConfigurationTab()
        {
        return (this.tabSerialConfiguration);
        }


    /***********************************************************************************************
     * Set the SerialConfiguration Tab.
     *
     * @param config
     */

    public void setSerialConfigurationTab(final UIComponentPlugin config)
        {
        this.tabSerialConfiguration = config;
        }


    /***********************************************************************************************
     * Get the JavaConsole Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getJavaConsoleTab()
        {
        return (this.tabJavaConsole);
        }


    /***********************************************************************************************
     * Set the JavaConsole Tab.
     *
     * @param console
     */

    public void setJavaConsoleTab(final UIComponentPlugin console)
        {
        this.tabJavaConsole = console;
        }


    /***********************************************************************************************
     * Get the JythonConsole Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getJythonConsoleTab()
        {
        return (this.tabJythonConsole);
        }


    /***********************************************************************************************
     * Set the JythonConsole Tab.
     *
     * @param console
     */

    public void setJythonConsoleTab(final UIComponentPlugin console)
        {
        this.tabJythonConsole = console;
        }


    /***********************************************************************************************
     * Get the JythonEditor Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getJythonEditorTab()
        {
        return (this.tabJythonEditor);
        }


    /***********************************************************************************************
     * Set the JythonEditor Tab.
     *
     * @param editor
     */

    public void setJythonEditorTab(final UIComponentPlugin editor)
        {
        this.tabJythonEditor = editor;
        }


    /***********************************************************************************************
     * Get the HexEditor Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getHexEditorTab()
        {
        return (this.tabHexEditor);
        }


    /***********************************************************************************************
     * Set the HexEditor Tab.
     *
     * @param editor
     */

    public void setHexEditorTab(final UIComponentPlugin editor)
        {
        this.tabHexEditor = editor;
        }


    /***********************************************************************************************
     * Get the AudioExplorer Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getAudioExplorerTab()
        {
        return (this.tabAudioExplorer);
        }


    /***********************************************************************************************
     * Set the AudioExplorer Tab.
     *
     * @param explorer
     */

    public void setAudioExplorerTab(final UIComponentPlugin explorer)
        {
        this.tabAudioExplorer = explorer;
        }


    /***********************************************************************************************
     * Get the Image.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getImageTab()
        {
        return (this.tabImage);
        }


    /***********************************************************************************************
     * Set the Image Tab.
     *
     * @param image
     */

    public void setImageTab(final UIComponentPlugin image)
        {
        this.tabImage = image;
        }


    /***********************************************************************************************
     * Get the SuperposedDataAnalyser Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getSuperposedDataAnalyserTab()
        {
        return (this.tabSuperposedDataAnalyser);
        }


    /***********************************************************************************************
     * Set the SuperposedDataAnalyser Tab.
     *
     * @param sda
     */

    public void setSuperposedDataAnalyserTab(final UIComponentPlugin sda)
        {
        this.tabSuperposedDataAnalyser = sda;
        }


    /***********************************************************************************************
     * Get the ProcessedData.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getProcessedDataTab()
        {
        return (this.tabProcessedData);
        }


    /***********************************************************************************************
     * Set the ProcessedData Tab.
     *
     * @param processeddata
     */

    public void setProcessedDataTab(final UIComponentPlugin processeddata)
        {
        this.tabProcessedData = processeddata;
        }


    /***********************************************************************************************
     * Get the RawData.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getRawDataTab()
        {
        return (this.tabRawData);
        }


    /***********************************************************************************************
     * Set the RawData Tab.
     *
     * @param rawdata
     */

    public void setRawDataTab(final UIComponentPlugin rawdata)
        {
        this.tabRawData = rawdata;
        }


    /***********************************************************************************************
     * Get the Regional Map.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getRegionalMapTab()
        {
        return (this.tabRegionalMap);
        }


    /***********************************************************************************************
     * Set the RegionalMap Tab.
     *
     * @param regionalmap
     */

    public void setRegionalMapTab(final UIComponentPlugin regionalmap)
        {
        this.tabRegionalMap = regionalmap;
        }


    /***********************************************************************************************
     * Get the TimeZone Map.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getTimeZoneTab()
        {
        return (this.tabTimeZones);
        }


    /***********************************************************************************************
     * Set the TimeZone Tab.
     *
     * @param timezones
     */

    public void setTimeZoneTab(final UIComponentPlugin timezones)
        {
        this.tabTimeZones = timezones;
        }


    /***********************************************************************************************
     * Get the Star Map.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getStarMapTab()
        {
        return (this.tabStarMap);
        }


    /***********************************************************************************************
     * Set the Star Map Tab.
     *
     * @param starmap
     */

    public void setStarMapTab(final UIComponentPlugin starmap)
        {
        this.tabStarMap = starmap;
        }


    /***********************************************************************************************
     * Get the Ephemerides.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getEphemeridesTab()
        {
        return (this.tabEphemerides);
        }


    /***********************************************************************************************
     * Set the Ephemerides Tab.
     *
     * @param ephemerides
     */

    public void setEphemeridesTab(final UIComponentPlugin ephemerides)
        {
        this.tabEphemerides = ephemerides;
        }


    /***********************************************************************************************
     * Get the NetworkScanner Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getNetworkScannerTab()
        {
        return (this.tabNetworkScanner);
        }


    /***********************************************************************************************
     * Set the NetworkScanner Tab.
     *
     * @param scanner
     */

    public void setNetworkScannerTab(final UIComponentPlugin scanner)
        {
        this.tabNetworkScanner = scanner;
        }


    /***********************************************************************************************
     * Get the Instrument Log.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getInstrumentLogTab()
        {
        return (this.tabInstrumentLog);
        }


    /***********************************************************************************************
     * Set the InstrumentLog Tab.
     *
     * @param log
     */

    public void setInstrumentLogTab(final UIComponentPlugin log)
        {
        this.tabInstrumentLog = log;
        }


    /***********************************************************************************************
     * Get the Event Log.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getEventLogTab()
        {
        return (this.tabEventLog);
        }


    /***********************************************************************************************
     * Set the EventLog Tab.
     *
     * @param log
     */

    public void setEventLogTab(final UIComponentPlugin log)
        {
        this.tabEventLog = log;
        }


   /***********************************************************************************************
     * Get the MetadataExplorer.
     *
     * @return UIComponentPlugin
     */

   public UIComponentPlugin getMetadataExplorerTab()
       {
       return (this.tabMetadataExplorer);
       }


   /***********************************************************************************************
    * Set the MetadataExplorer Tab.
    *
    * @param explorer
    */

   public void setMetadataExplorerTab(final UIComponentPlugin explorer)
       {
       this.tabMetadataExplorer = explorer;
       }


   /***********************************************************************************************
    * Get the Configuration.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getConfigurationTab()
       {
       return (this.tabConfiguration);
       }


   /***********************************************************************************************
    * Set the Configuration Tab.
    *
    * @param configuration
    */

   public void setConfigurationTab(final UIComponentPlugin configuration)
       {
       this.tabConfiguration = configuration;
       }


   /***********************************************************************************************
    * Get the Mantis Tab.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getMantisTab()
       {
       return (this.tabMantis);
       }


   /***********************************************************************************************
    * Set the Mantis Tab.
    *
    * @param mantis
    */

   public void setMantisTab(final UIComponentPlugin mantis)
       {
       this.tabMantis = mantis;
       }


   /***********************************************************************************************
    * Get the Subversion Tab.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getSubversionTab()
       {
       return (this.tabSubversion);
       }


   /***********************************************************************************************
    * Set the Subversion Tab.
    *
    * @param subversion
    */

   public void setSubversionTab(final UIComponentPlugin subversion)
       {
       this.tabSubversion = subversion;
       }


   /***********************************************************************************************
    * Get the Jenkins Tab.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getJenkinsTab()
       {
       return (this.tabJenkins);
       }


   /***********************************************************************************************
    * Set the Jenkins Tab.
    *
    * @param jenkins
    */

   public void setJenkinsTab(final UIComponentPlugin jenkins)
       {
       this.tabJenkins = jenkins;
       }


   /***********************************************************************************************
    * Get the RSS Newsreader Tab.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getNewsreaderTab()
       {
       return (this.tabNewsreader);
       }


   /***********************************************************************************************
    * Set the RSS Newsreader Tab.
    *
    * @param newsreader
    */

   public void setNewsreaderTab(final UIComponentPlugin newsreader)
       {
       this.tabNewsreader = newsreader;
       }


   /***********************************************************************************************
    * Get the Command Lexicon.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getCommandLexiconTab()
       {
       return (this.tabCommandLexicon);
       }


   /***********************************************************************************************
    * Set the CommandLexicon Tab.
    *
    * @param lexicon
    */

   public void setCommandLexiconTab(final UIComponentPlugin lexicon)
       {
       this.tabCommandLexicon = lexicon;
       }


   /***********************************************************************************************
    * Get the XML.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getXMLTab()
       {
       return (this.tabXML);
       }


   /***********************************************************************************************
    * Set the XML Tab.
    *
    * @param xml
    */

   public void setXMLTab(final UIComponentPlugin xml)
       {
       this.tabXML = xml;
       }


   /***********************************************************************************************
    * Get the Help.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getHelpTab()
       {
       return (this.tabHelp);
       }


   /***********************************************************************************************
    * Set the Help Tab.
    *
    * @param help
    */

   public void setHelpTab(final UIComponentPlugin help)
       {
       this.tabHelp = help;
       }


    /***********************************************************************************************
     * Get the Manual Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getManualTab()
        {
        return (this.tabManual);
        }


    /***********************************************************************************************
     * Set the Manual Tab.
     *
     * @param manual
     */

    public void setManualTab(final UIComponentPlugin manual)
        {
        this.tabManual = manual;
        }


    /***********************************************************************************************
    * Get the Publisher Tab.
    *
    * @return UIComponentPlugin
    */

   public UIComponentPlugin getPublisherTab()
       {
       return (this.tabPublisher);
       }


   /***********************************************************************************************
    * Set the Publisher Tab.
    *
    * @param help
    */

   public void setPublisherTab(final UIComponentPlugin help)
       {
       this.tabPublisher = help;
       }


    /***********************************************************************************************
     * Debug the state of a Tab.
     */

    private void debugState()
        {
        LOGGER.log("ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()) = " + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()));
        LOGGER.log("(getMetadataExplorerTab() != null) = " + (getMetadataExplorerTab() != null));
        LOGGER.log("(getMetadataExplorerTab() instanceof MetadataExplorerFrameUIComponentInterface) = " + (getMetadataExplorerTab() instanceof MetadataExplorerFrameUIComponentInterface));
        LOGGER.log("(UIComponent.isUIComponentShowing(getMetadataExplorerTab())) = " + (UIComponentHelper.isUIComponentShowing(getMetadataExplorerTab())));
        }
    }
