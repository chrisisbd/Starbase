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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;
import org.lmn.fc.frameworks.starbase.ui.userinterface.HeaderUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.widgets.IndicatorInterface;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * The InstrumentUIComponentDecoratorInterface.
 */

public interface InstrumentUIComponentDecoratorInterface extends UIComponentPlugin,
                                                                 InstrumentStateChangedListener,
                                                                 MetadataChangedListener
    {
    // String Resources
    String INVALID_RESOURCE_KEY = "InvalidResourceKey";
    String MSG_NO_DATA_AVAILABLE = "<html><i>No data available</i></html>";

    String TAB_ACTIONS= "Actions";
    String TAB_ASCII= "ASCII Codes";
    String TAB_CHART = "Chart";
    String TAB_COMMAND_LEXICON = "Command Lexicon";
    String TAB_COMMAND_MONITOR = "Command Monitor";
    String TAB_COMMANDS = "Commands";
    String TAB_CONFIGURATION = "Configuration";
    String TAB_DILBERT = "Dilbert";
    String TAB_EPHEMERIDES = "Ephemerides";
    String TAB_EVENT_LOG = "Event Log";
    String TAB_HELP = "Help";
    String TAB_HEX_EDITOR = "Hex Editor";
    String TAB_IMAGE = "Image";
    String TAB_INSTRUMENT_LOG = "Instrument Log";
    String TAB_JAVA_CONSOLE = "Java Console";
    String TAB_JENKINS = "Jenkins Builds";
    String TAB_JYTHON_CONSOLE = "Jython Console";
    String TAB_JYTHON_EDITOR = "Jython Editor";
    String TAB_MANTIS = "Mantis";
    String TAB_MANUAL = "Manual";
    String TAB_MEMORY_MONITOR = "Memory Monitor";
    String TAB_META_DATA_EXPLORER = "Meta Data";
    String TAB_METERS = "Meters";
    String TAB_NEWS = "News";
    String TAB_NEWSFEEDS = "Newsfeeds";
    String TAB_OBSERVATORY_LOG = "Observatory Log";
    String TAB_PORT_MONITOR = "Port Monitor";
    String TAB_PUBLISHER = "Publisher";
    String TAB_PRESENTER = "Presenter";
    String TAB_PROCESSED_DATA = "Processed Data";
    String TAB_RAW_DATA = "Raw Data";
    String TAB_REGIONAL_MAP = " RegionalMap";
    String TAB_SCANNER = "Starinet Scanner";
    String TAB_SERIAL_CONFIGURATION = "Serial Configuration";
    String TAB_AUDIO_EXPLORER = "Audio Explorer";
    String TAB_STAR_MAP = "Star Map";
    String TAB_SUBVERSION = "Subversion Log";
    String TAB_SUPERPOSED_DATA_ANALYSER = "Superposed Data Analyser";
    String TAB_TIME_ZONES = "Time Zones";
    String TAB_XML = "Instrument XML";

    String TOOLTIP_PRESERVED = "All data are preserved; you may run out of memory!";
    String TOOLTIP_AUTO_TRUNCATES = "Auto-truncates to ";
    String TOOLTIP_AUTO_DECIMATES = "Auto-decimates to ";
    String TOOLTIP_ENTRIES = " entries";

    Dimension DIMENSION_METER = new Dimension(140, 80);


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getHostInstrument();


    /***********************************************************************************************
     * Get the Instrument as an XMLBean.
     *
     * @return Instrument
     */

    Instrument getInstrument();


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    ObservatoryUIInterface getObservatoryUI();


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    TaskPlugin getHostTask();


    /***********************************************************************************************
     * Initialise the ObservatoryInstrumentUIComponent.
     */

    void initialise();


    /***********************************************************************************************
     * Start the ObservatoryInstrumentUIComponent.
     *
     * @return boolean
     */

    boolean start();


    /***********************************************************************************************
     * Indicate if the Instrument is in the Started state.
     *
     * @return boolean
     */

    boolean isStarted();


    /***********************************************************************************************
     * Stop the ObservatoryInstrumentUIComponent.
     *
     * @return boolean
     */

    boolean stop();


    /***********************************************************************************************
     * Shutdown the ObservatoryInstrumentUIComponent after use.
     */

    void dispose();


    /***********************************************************************************************
     * Reset the ObservatoryInstrumentUIComponent.
     *
     * @param resetmode
     */

    void reset(final ResetMode resetmode);


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    FontInterface getFontData();


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    ColourInterface getColourData();


    /**********************************************************************************************
     * Get the ColourDataType.
     * ToDo Refactor to remove getColourData().
     *
     * @return ColourPlugin
     */

    ColourInterface getForegroundColour();


    /**********************************************************************************************
     * Get the Background Colour.
     * ToDo Refactor to implement a variable.
     *
     * @return ColourInterface
     */

    ColourInterface getBackgroundColour();


    /***********************************************************************************************
     * Get the Header UIComponent.
     *
     * @return HeaderUIComponent
     */

    HeaderUIComponent getHeaderUIComponent();


    /***********************************************************************************************
     * Get the JTabbedPane underlying this UI, if used (null if not).
     *
     * @return JTabbedPane
     */

    JTabbedPane getTabbedPane();


    /***********************************************************************************************
     * Set the JTabbedPane.
     *
     * @param tabbedpane
     */

    void setTabbedPane(final JTabbedPane tabbedpane);


    /***********************************************************************************************
     * Configure the TabbedPane with default colours and font.
     */

    void configureTabbedPane();


    /***********************************************************************************************
     * Get the List of ExportableComponents which may be exported.
     *
     * @return List<ExportableComponentInterface>
     */

    List<ExportableComponentInterface> getExportableComponents();


    /***********************************************************************************************
     * Get the specified ExportableComponent which may be exported.
     *
     * @param index
     *
     * @return ExportableComponentInterface
     */

    ExportableComponentInterface getExportableComponent(int index);


    /***********************************************************************************************
     * Set an ExportableComponent which may be exported, at the specified Index.
     *
     * @param index
     * @param component
     */

    void setExportableComponent(int index,
                                ExportableComponentInterface component);


    /***********************************************************************************************
     * Get the List of ExportableTabs.
     *
     * @return List<String>
     */

    List<String> getExportableTabs();


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    String getResourceKey();


    /**********************************************************************************************/
    /* DAO Helper methods                                                                         */
    /***********************************************************************************************
     * Get the data produced by the DAO.
     * Return null if not applicable or not available.
     *
     * @return DAOWrapper
     */

    DAOWrapperInterface getWrappedData();


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     * This is used only on ControlPanels, InstrumentPanels and their UIComponents.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    void setWrappedData(DAOWrapperInterface daowrapper,
                        boolean updatedata,
                        boolean updatemetadata);


    /***********************************************************************************************
     * Remove any Data associated with this Instrument's appearance on the UI,
     * on the InstrumentPanel. For instance, remove a Chart regardless of it being visible.
     */

    void removeInstrumentIdentity();


    /***********************************************************************************************
     * Update any logs from data in the DAO Wrapper.
     *
     * @param daowrapper
     */

    void flushLogFragments(DAOWrapperInterface daowrapper);


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    ObservatoryClockInterface getObservatoryClock();


    /**********************************************************************************************/
    /* Control Panel Indicators                                                                   */
    /***********************************************************************************************
     * Get the Control Panel Indicator 0.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator0();


    /***********************************************************************************************
     * Set the Control Panel Indicator 0.
     *
     * @param indicator
     */

    void setIndicator0(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 1.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator1();


    /***********************************************************************************************
     * Set the Control Panel Indicator 1.
     *
     * @param indicator
     */

    void setIndicator1(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 2.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator2();


    /***********************************************************************************************
     * Set the Control Panel Indicator 2.
     *
     * @param indicator
     */

    void setIndicator2(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 3.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator3();


    /***********************************************************************************************
     * Set the Control Panel Indicator 3.
     *
     * @param indicator
     */

    void setIndicator3(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 4.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator4();


    /***********************************************************************************************
     * Set the Control Panel Indicator 4.
     *
     * @param indicator
     */

    void setIndicator4(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 5.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator5();


    /***********************************************************************************************
     * Set the Control Panel Indicator 5.
     *
     * @param indicator
     */

    void setIndicator5(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 6.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator6();


    /***********************************************************************************************
     * Set the Control Panel Indicator 6.
     *
     * @param indicator
     */

    void setIndicator6(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the Control Panel Indicator 7.
     *
     * @return IndicatorInterface
     */

    IndicatorInterface getIndicator7();


    /***********************************************************************************************
     * Set the Control Panel Indicator 7.
     *
     * @param indicator
     */

    void setIndicator7(IndicatorInterface indicator);


    /***********************************************************************************************
     * Get the ControlPanel Indicators.
     *
     * @return List<IndicatorInterface>
     */

    List<IndicatorInterface> getControlPanelIndicators();


    /***********************************************************************************************
     * Get the number of display Indicators.
     *
     * @return int
     */

    int getIndicatorCount();


    /***********************************************************************************************
     * Reset the ControlPanel Indicators.
     */

    void resetControlPanelIndicators();


    /***********************************************************************************************
     * Get the ControlPanel Indicator Default ValueKeys.
     *
     * @return List<String>
     */

    List<String> getControlPanelIndicatorDefaultValueKeys();


    /***********************************************************************************************
     * Get the ControlPanel Indicator Default Units.
     *
     * @return List<SchemaUnits.Enum>
     */

    List<SchemaUnits.Enum> getControlPanelIndicatorDefaultUnits();


    /***********************************************************************************************
     * Get the ControlPanel Indicator Default Tooltip Keys.
     *
     * @return List<String>
     */

    List<String> getControlPanelIndicatorDefaultTooltipKeys();


    /**********************************************************************************************/
    /* Instrument Panel Tabs                                                                      */
    /***********************************************************************************************
     * Get the Commands.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getCommandsTab();


    /***********************************************************************************************
     * Set the Commands Tab.
     *
     * @param commands
     */

    void setCommandsTab(UIComponentPlugin commands);


    /***********************************************************************************************
     * Get the Chart.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getChartTab();


    /***********************************************************************************************
     * Set the Chart Tab.
     *
     * @param chart
     */

    void setChartTab(UIComponentPlugin chart);


    /***********************************************************************************************
     * Get the Meters Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getMetersTab();


    /***********************************************************************************************
     * Set the Meters Tab.
     *
     * @param meters
     */

    void setMetersTab(UIComponentPlugin meters);


    /***********************************************************************************************
     * Get the Clocks.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getClocksTab();


    /***********************************************************************************************
     * Set the Clocks Tab.
     *
     * @param commands
     */

    void setClocksTab(UIComponentPlugin commands);


    /***********************************************************************************************
     * Get the SerialConfiguration Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getSerialConfigurationTab();


    /***********************************************************************************************
     * Set the SerialConfiguration Tab.
     *
     * @param config
     */

    void setSerialConfigurationTab(UIComponentPlugin config);


    /***********************************************************************************************
     * Get the JavaConsole Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getJavaConsoleTab();


    /***********************************************************************************************
     * Set the JavaConsole Tab.
     *
     * @param console
     */

    void setJavaConsoleTab(UIComponentPlugin console);


    /***********************************************************************************************
     * Get the JythonConsole Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getJythonConsoleTab();


    /***********************************************************************************************
     * Set the JythonConsole Tab.
     *
     * @param console
     */

    void setJythonConsoleTab(UIComponentPlugin console);


    /***********************************************************************************************
     * Get the JythonEditor Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getJythonEditorTab();


    /***********************************************************************************************
     * Set the JythonEditor Tab.
     *
     * @param editor
     */

    void setJythonEditorTab(UIComponentPlugin editor);


    /***********************************************************************************************
     * Get the HexEditor Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getHexEditorTab();


    /***********************************************************************************************
     * Set the HexEditor Tab.
     *
     * @param editor
     */

    void setHexEditorTab(UIComponentPlugin editor);


    /***********************************************************************************************
     * Get the AudioExplorer Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getAudioExplorerTab();


    /***********************************************************************************************
     * Set the AudioExplorer Tab.
     *
     * @param explorer
     */

    void setAudioExplorerTab(UIComponentPlugin explorer);


    /***********************************************************************************************
     * Get the Image.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getImageTab();


    /***********************************************************************************************
     * Set the Image Tab.
     *
     * @param image
     */

    void setImageTab(UIComponentPlugin image);


    /***********************************************************************************************
     * Get the SuperposedDataAnalyser Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getSuperposedDataAnalyserTab();


    /***********************************************************************************************
     * Set the SuperposedDataAnalyser Tab.
     *
     * @param sda
     */

    void setSuperposedDataAnalyserTab(UIComponentPlugin sda);


    /***********************************************************************************************
     * Get the ProcessedData.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getProcessedDataTab();


    /***********************************************************************************************
     * Set the ProcessedData Tab.
     *
     * @param processeddata
     */

    void setProcessedDataTab(UIComponentPlugin processeddata);


    /***********************************************************************************************
     * Get the RawData.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getRawDataTab();


    /***********************************************************************************************
     * Set the RawData Tab.
     *
     * @param rawdata
     */

    void setRawDataTab(UIComponentPlugin rawdata);


    /***********************************************************************************************
     * Get the Regional Map.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getRegionalMapTab();


    /***********************************************************************************************
     * Set the RegionalMap Tab.
     *
     * @param regionalmap
     */

    void setRegionalMapTab(UIComponentPlugin regionalmap);


    /***********************************************************************************************
     * Get the TimeZone Map.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getTimeZoneTab();


    /***********************************************************************************************
     * Set the TimeZone Tab.
     *
     * @param timezones
     */

    void setTimeZoneTab(UIComponentPlugin timezones);


    /***********************************************************************************************
     * Get the Star Map.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getStarMapTab();


    /***********************************************************************************************
     * Set the Star Map Tab.
     *
     * @param newsreader
     */

    void setStarMapTab(UIComponentPlugin newsreader);


    /***********************************************************************************************
     * Get the Ephemerides.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getEphemeridesTab();


    /***********************************************************************************************
     * Set the Ephemerides Tab.
     *
     * @param ephemerides
     */

    void setEphemeridesTab(UIComponentPlugin ephemerides);


    /***********************************************************************************************
     * Get the NetworkScanner Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getNetworkScannerTab();


    /***********************************************************************************************
     * Set the NetworkScanner Tab.
     *
     * @param xml
     */

    void setNetworkScannerTab(UIComponentPlugin xml);


    /***********************************************************************************************
     * Get the Instrument Log.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getInstrumentLogTab();


    /***********************************************************************************************
     * Set the InstrumentLog Tab.
     *
     * @param log
     */

    void setInstrumentLogTab(UIComponentPlugin log);


    /***********************************************************************************************
     * Get the Event Log.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getEventLogTab();


    /***********************************************************************************************
     * Set the EventLog Tab.
     *
     * @param log
     */

    void setEventLogTab(UIComponentPlugin log);


    /***********************************************************************************************
     * Get the MetadataExplorer.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getMetadataExplorerTab();


    /***********************************************************************************************
     * Set the MetadataExplorer Tab.
     *
     * @param explorer
     */

    void setMetadataExplorerTab(UIComponentPlugin explorer);


    /***********************************************************************************************
     * Get the Configuration.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getConfigurationTab();


    /***********************************************************************************************
     * Set the Configuration Tab.
     *
     * @param configuration
     */

    void setConfigurationTab(UIComponentPlugin configuration);


    /***********************************************************************************************
     * Get the Mantis Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getMantisTab();


    /***********************************************************************************************
     * Set the Mantis Tab.
     *
     * @param mantis
     */

    void setMantisTab(final UIComponentPlugin mantis);


    /***********************************************************************************************
     * Get the Subversion Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getSubversionTab();


    /***********************************************************************************************
     * Set the Subversion Tab.
     *
     * @param subversion
     */

    void setSubversionTab(UIComponentPlugin subversion);


    /***********************************************************************************************
     * Get the Jenkins Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getJenkinsTab();


    /***********************************************************************************************
     * Set the Jenkins Tab.
     *
     * @param jenkins
     */

    void setJenkinsTab(UIComponentPlugin jenkins);


    /***********************************************************************************************
     * Get the RSS Newsreader Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getNewsreaderTab();


    /***********************************************************************************************
     * Set the RSS Newsreader Tab.
     *
     * @param newsreader
     */

    void setNewsreaderTab(UIComponentPlugin newsreader);


    /***********************************************************************************************
     * Get the Command Lexicon.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getCommandLexiconTab();


    /***********************************************************************************************
     * Set the CommandLexicon Tab.
     *
     * @param lexicon
     */

    void setCommandLexiconTab(UIComponentPlugin lexicon);


    /***********************************************************************************************
     * Get the XML.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getXMLTab();


    /***********************************************************************************************
     * Set the XML Tab.
     *
     * @param xml
     */

    void setXMLTab(UIComponentPlugin xml);


    /***********************************************************************************************
     * Get the Help.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getHelpTab();


    /***********************************************************************************************
     * Set the Help Tab.
     *
     * @param help
     */

    void setHelpTab(UIComponentPlugin help);


    /***********************************************************************************************
     * Get the Manual Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getManualTab();


    /***********************************************************************************************
     * Set the Manual Tab.
     *
     * @param manual
     */

    void setManualTab(UIComponentPlugin manual);


    /***********************************************************************************************
     * Get the Publisher Tab.
     *
     * @return UIComponentPlugin
     */

    UIComponentPlugin getPublisherTab();


    /***********************************************************************************************
     * Set the Publisher Tab.
     *
     * @param help
     */

    void setPublisherTab(UIComponentPlugin help);
    }
