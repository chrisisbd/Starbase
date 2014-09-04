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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.sandbox;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.impl.AveragingFFTFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.DigitalPanelMeterUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The SandboxInstrumentPanel.
 */

public final class SandboxInstrumentPanel extends InstrumentUIComponentDecorator
    {
    private static final long serialVersionUID = 9204989944841558803L;

    // String Resources
    private static final String ICON_HEADER = "sandbox-header.png";
    private static final String INSTRUMENT_HELP = "SandboxHelp.html";
    private static final String TAB_ALTERNATIVE_CHART = "Alternative Chart";

    private UIComponentPlugin tabAlternativeChart;
    private UIComponentPlugin tabDSP;


    /***********************************************************************************************
     * Construct a SandboxInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param indicatorcount
     */

    public SandboxInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                  final Instrument instrumentxml,
                                  final ObservatoryUIInterface hostui,
                                  final TaskPlugin task,
                                  final FontInterface font,
                                  final ColourInterface colour,
                                  final String resourcekey,
                                  final int indicatorcount)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              indicatorcount);
        }


    /***********************************************************************************************
     * Initialise the SandboxInstrumentPanel.
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

        // Create the SandboxInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     * Notify interested tabs of the change.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        final String SOURCE = "SandboxInstrumentPanel.instrumentChanged() ";

        super.instrumentChanged(event);

        if ((getMetersTab() != null)
            && (getMetersTab() instanceof DigitalPanelMeterUIComponent))
            {
            InstrumentUIHelper.updateDigitalPanelMeters(getHostInstrument(),
                                                        getHostInstrument().getControlPanel(),
                                                        (DigitalPanelMeterUIComponent)getMetersTab());
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the SandboxInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addLogLinChartTab(this,
                                                    TAB_CHART,
                                                    null,
                                                    DataUpdateType.DECIMATE,
                                                    -1000.0,
                                                    1000.0,
                                                    -1000.0,
                                                    1000.0,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetersTab(this,
                                               TAB_METERS,
                                               REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addProcessedDataTab(this,
                                                      TAB_PROCESSED_DATA,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addRawDataTab(this,
                                                TAB_RAW_DATA,
                                                REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addSuperposedDataAnalyserTab(this,
                                                               TAB_SUPERPOSED_DATA_ANALYSER,
                                                               REGISTRY_MODEL.getLoggedInUser());

        tabDSP = new AveragingFFTFrameUIComponent(getHostInstrument(),
                                                  getInstrument(),
                                                  getObservatoryUI(),
                                                  getHostTask(),
                                                  getFontData(),
                                                  getColourData(),
                                                  REGISTRY.getFrameworkResourceKey());
        getTabbedPane().addTab("DSP Experiments",
                               (Component) getDSPExperimentsTab());

        InstrumentPanelTabFactory.addImageTab(this,
                                              TAB_IMAGE,
                                              null,
                                              REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addStarMapTab(this,
                                                TAB_STAR_MAP,
                                                REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEphemeridesTab(this,
                                                    TAB_EPHEMERIDES,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addRegionalMapTab(this,
                                                    TAB_REGIONAL_MAP,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_EVENT_LOG,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addConfigurationTab(this,
                                                      TAB_CONFIGURATION,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addInstrumentNewsreaderTab(this,
                                                             TAB_NEWS,
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

        // Allow export of the StarMap
        // The StarMap is not created until the StarMapUIComponent is initialised,
        // which happens in initialiseAllTabComponents()
        // Normally the ExportableComponent would be one of the tabs,
        // but in this case it is held within a tab component, so must be retrieved differently
        setExportableComponent(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_STAR_MAP,
                               ((StarMapUIComponentPlugin) getStarMapTab()).getExportableComponent());

        // Similarly add the RegionalMap
        setExportableComponent(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_REGIONAL_MAP,
                               ((MapUIComponentPlugin) getRegionalMapTab()).getExportableComponent());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the DSP Experiments Tab.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getDSPExperimentsTab()
        {
        return (this.tabDSP);
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method, or from any Command doing a realtime update.
     *
     * @param daowrapper
     * @param updatemetadata
     */

//    public void setWrappedData(final DAOWrapperInterface daowrapper,
//                               final boolean updatemetadata)
//        {
//        final String SOURCE = "SandboxInstrumentPanel.setWrappedData() ";
//
//        super.setWrappedData(daowrapper, updatemetadata);
//
//        // Set the AlternativeChart Metadata to be the same as Chart Metadata
//        // The Chart Metadata were set by setWrappedData()
//        if ((updatemetadata)
//            && (getChartTab() != null)
//            && (getChartTab() instanceof ChartUIComponent)
//            && (getAlternativeChartTab() != null)
//            && (getAlternativeChartTab() instanceof ChartUIComponent))
//            {
//            ((ChartUIComponent) getAlternativeChartTab()).setMetadata(((ChartUIComponent) getChartTab()).getMetadata());
//            }
//        else
//            {
//            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
//                                      SOURCE + "AlternativeChart Metadata will not be updated");
//            }
//
//        // This will refresh the Alternative Chart if it is of the appropriate DataType
//        // and it is on the AlternativeChartTab
//        setAlternativeXyDataset(getWrappedData().getXYDataset(),
//                                getWrappedData().getRawDataChannelCount(),
//                                getWrappedData().hasTemperatureChannel());
//        }


    /***********************************************************************************************
     * Set and refresh the XYDataset to be displayed on the Alternative Chart tab.
     *
     * @param dataset
     * @param channelcount
     * @param temperaturechannel
     */

//    private void setAlternativeXyDataset(final XYDataset dataset,
//                                         final int channelcount,
//                                         final boolean temperaturechannel)
//        {
//        // Allow null datasets following a reset()
//        if (getAlternativeChartTab() != null)
//            {
//            if (getAlternativeChartTab() instanceof LogLinChartUIComponent)
//                {
//                // Set the ChannelCount and Temperature flag first, because setPrimaryXYDataset() uses them
//                ((LogLinChartUIComponent) getAlternativeChartTab()).setChannelCount(channelcount);
//                ((LogLinChartUIComponent) getAlternativeChartTab()).setTemperatureChannel(temperaturechannel);
//                ((LogLinChartUIComponent) getAlternativeChartTab()).setPrimaryXYDataset(dataset);
//                }
//
//            // Force an immediate update of the Chart only if visible
//            if ((getAlternativeChartTab() instanceof ChartUIComponentPlugin)
//                && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), getAlternativeChartTab())))
//                {
//                ((ChartUIComponentPlugin) getAlternativeChartTab()).refreshChart(zxczxczxc, UIComponentHelper.shouldRefresh(false,
//                                                                                                                 getHostInstrument(),
//                                                                                                                 getAlternativeChartTab()));
//                }
//            }
//        }


    /***********************************************************************************************
     * Get the Alternative Chart.
     *
     * @return UIComponentPlugin
     */

//    public UIComponentPlugin getAlternativeChartTab()
//        {
//        return (this.tabAlternativeChart);
//        }
    }
