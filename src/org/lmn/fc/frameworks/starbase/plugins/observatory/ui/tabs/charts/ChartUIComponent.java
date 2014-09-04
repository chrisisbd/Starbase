// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts;

import info.clearthought.layout.TableLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectionMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.multipleslider.MThumbSlider;
import org.lmn.fc.ui.multipleslider.MThumbSliderAdditionalUI;
import org.lmn.fc.ui.widgets.ControlKnobInterface;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface.*;


/***************************************************************************************************
 * The base class for all Charts.
 */

public class ChartUIComponent extends UIComponent
        implements ChartUIComponentPlugin,
                   ChannelSelectionChangedListener,
                   DatasetDomainChangedListener,
                   ObservatoryConstants
    {
    private static final long serialVersionUID = 610799415134903715L;

    // TableLayout row and column size definitions
    private static final double[][] size =
            {
                    { // Columns
                      TableLayout.FILL,
                      TableLayout.PREFERRED
                    },
                    { // Rows
                      TableLayout.PREFERRED,
                      TableLayout.FILL,
                      TableLayout.PREFERRED
                    }
            };

    // TableLayout constraints
    // http://www.clearthought.info/sun/products/jfc/tsc/articles/tablelayout/Cells.html
    // Indicates TableLayoutConstraints's position and justification as a string in the form
    // "column, row, horizontal justification, vertical justification"
    // Valid values: LEFT, RIGHT, CENTER, TOP, BOTTOM
    // The horizontal justification is specified before the vertical justification
    // Multiple Cells
    // A component can also be added to a rectangular set of cells.
    // This is done by specifying the upper, left and lower, right corners of that set.
    // Components that occupy more than one cell will have a size equal to the total area
    // of all cells that component occupies.
    // There is no justification attribute for multi-celled components.

    private static final String[] constraints =
            { // Column, Row, JustificationX, JustificationY
              "0, 0, 1, 0",              // Toolbar (optional)
              "0, 1, CENTER, CENTER",    // Chart
              "1, 1, CENTER, TOP",       // Channel Selector
              "0, 2, CENTER, CENTER"     // Slider
            };

    // Injections
    private final TaskPlugin pluginTask;
    private final ObservatoryInstrumentInterface hostInstrument;
    private String strReportName;
    private final String strResourceKey;
    private DataUpdateType updateType;
    private int intDisplayLimit;
    private boolean boolRefreshable;
    private boolean boolClickRefresh;

    private int intDomainStartPoint;
    private int intDomainEndPoint;
    private double dblRangeCrosshair;
    private double dblDomainCrosshair;

    // Underlying data, passed in from a DAO
    private ObservatoryInstrumentDAOInterface daoChart;
    private List<Metadata> listMetadata;
    private XYDataset xyDatasetPrimary;
    private List<XYDataset> xyDatasetSecondaries;
    private int intChannelCount;
    private boolean boolTemperatureChannel;

    // Chart configuration
    private DatasetType datasetType;
    private boolean boolChartHasLinearMode;
    private boolean boolChartHasLogarithmicMode;
    private boolean boolChartCanAutorange;

    // User Interface
    private final JPanel panelUIContainer;
    private final JPanel panelChartContainer;

    // The dynamic components
    private ChartPanel chartPanel;
    private boolean boolHasChannelSelector;
    private ChannelSelectorUIComponentInterface channelSelectorOccupant;
    private boolean boolHasDatasetDomainControl;
    private DatasetDomainUIComponentInterface datasetDomainControlOccupant;

    // Chart Status
    private boolean boolChannelSelectionChanged;
    private boolean boolDatasetDomainChanged;

    // Refresh Worker
    private SwingWorker workerRefresh;

    // Listeners
    private final Vector<DatasetChangedListener> vecDatasetChangedListeners;


    /***********************************************************************************************
     * Construct a ChartUIComponent with no Toolbar.
     *
     * @param task
     * @param hostinstrument
     * @param name
     * @param resourcekey
     * @param updatetype
     * @param displaylimit
     * @param refreshable
     * @param clickrefresh
     */

    public ChartUIComponent(final TaskPlugin task,
                            final ObservatoryInstrumentInterface hostinstrument,
                            final String name,
                            final String resourcekey,
                            final DataUpdateType updatetype,
                            final int displaylimit,
                            final boolean refreshable,
                            final boolean clickrefresh)
        {
        // Create the ChartUIComponent
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.pluginTask = task;
        this.hostInstrument = hostinstrument;
        this.strReportName = name;
        this.strResourceKey = resourcekey;
        this.updateType = updatetype;
        this.intDisplayLimit = displaylimit;
        this.boolRefreshable = refreshable;
        this.boolClickRefresh = clickrefresh;

        this.intDomainStartPoint = 0;
        this.intDomainEndPoint = DOMAIN_SLIDER_MAXIMUM;
        this.dblRangeCrosshair = 0.0;
        this.dblDomainCrosshair = 0.0;

        this.daoChart = null;
        this.listMetadata = null;
        this.xyDatasetPrimary = null;
        this.xyDatasetSecondaries = new ArrayList<XYDataset>(10);
        this.intChannelCount = 0;
        this.boolTemperatureChannel = false;

        this.datasetType = DatasetType.TIMESTAMPED;
        this.boolChartHasLinearMode = true;
        this.boolChartHasLogarithmicMode = false;
        this.boolChartCanAutorange = false;

        // Make the UI and Chart container panels only once
        this.panelUIContainer = new JPanel();
        this.panelChartContainer = new JPanel();
        this.chartPanel = null;

        // Most Charts have a ChannelSelector
        this.boolHasChannelSelector = true;
        this.channelSelectorOccupant = null;
        this.boolChannelSelectionChanged = false;

        // Most Charts have a DatasetDomainControl
        this.boolHasDatasetDomainControl = true;
        this.datasetDomainControlOccupant = null;
        this.boolDatasetDomainChanged = false;

        this.workerRefresh = null;

        this.vecDatasetChangedListeners = new Vector<DatasetChangedListener>(10);
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise the Chart.
     */

    public synchronized void initialiseUI()
        {
        final String SOURCE = "ChartUIComponent.initialiseUI() ";

        super.initialiseUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[uistate=" + getUIState().getName() + "]");

        setRangeCrosshair(0.0);
        setDomainCrosshair(0.0);

        // Remove any previous components and start with a blank panel UIComponent
        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)) ;

        getUIContainer().setLayout(new TableLayout(size));
        getUIContainer().setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        getChartContainer().removeAll();
        getChartContainer().setLayout(new BorderLayout());
        getChartContainer().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        // Make sure that the Chart uses all available space...
        getChartContainer().setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        getChartContainer().add(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT), BorderLayout.CENTER);

        // There may not be a Toolbar
        if (getToolBar() != null)
            {
            getUIContainer().add(getToolBar(),
                                 constraints[0]);
            }

        getUIContainer().add(getChartContainer(),
                             constraints[1]);

        // Start with no Chart or container
        // The refresh Thread will create what's needed
        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "setChartPanel(NULL)");
        setChartPanel(null);

        // Set up the ChannelSelector
        // It will be empty until an XYDataset is loaded
        setChannelSelectorOccupant(new ChannelSelectorUIComponent(getHostInstrument(),
                                                                  this,
                                                                  getMetadata(),
                                                                  getUpdateType(),
                                                                  getResourceKey()));
        getChannelSelectorOccupant().initialiseUI();
        getChannelSelectorOccupant().addChannelSelectionChangedListener(this);

        if (hasChannelSelector())
            {
            getUIContainer().add((Component) getChannelSelectorOccupant(),
                                 constraints[2]);
            }

        setDatasetDomainControlOccupant(new DatasetDomainUIComponent(getHostInstrument(),
                                                                     this,
                                                                     getMetadata(),
                                                                     getUpdateType(),
                                                                     getResourceKey()));
        getDatasetDomainControlOccupant().initialiseUI();
        getDatasetDomainControlOccupant().addDatasetDomainChangedListener(this);
        setDatasetDomainStartPoint(DOMAIN_SLIDER_MINIMUM);
        setDatasetDomainEndPoint(DOMAIN_SLIDER_MAXIMUM);

        if (hasDatasetDomainControl())
            {
            getUIContainer().add((Component) getDatasetDomainControlOccupant(),
                                 constraints[3]);
            }

        // Consume all area on the underlying UIComponent
        add(getUIContainer(), BorderLayout.CENTER);

        // Set up for mouse click refresh, or none
        setRefreshType();
        }


    /***********************************************************************************************
     * Run the UI of this Chart.
     * Refresh the data and start the Timer, if any.
     * Usually called in RootData.runUI().
     */

    public synchronized void runUI()
        {
        final String SOURCE = "ChartUIComponent.runUI() ";

        super.runUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[uistate=" + getUIState().getName() + "]");

        // These don't do anything useful for now!
        if (getChannelSelectorOccupant() != null)
            {
            getChannelSelectorOccupant().runUI();
            }

        if (getDatasetDomainControlOccupant() != null)
            {
            getDatasetDomainControlOccupant().runUI();

            // However, we can make sure the slider data are up to date now we are running
            if (getDatasetDomainControlOccupant().getDatasetDomainSlider() != null)
                {
                setDatasetDomainStartPoint(getDatasetDomainControlOccupant().getDatasetDomainSlider().getValueAt(INDEX_LEFT));
                setDatasetDomainEndPoint(getDatasetDomainControlOccupant().getDatasetDomainSlider().getValueAt(INDEX_RIGHT));
                }
            }

        // Refresh the Chart using the current state of the Instrument, DAO and Chart
        refreshChart(getDAO(), true, SOURCE);
        }


    /***********************************************************************************************
     * Stop the UI of this Chart.
     * Usually called in RootData.stopUI().
     */

    public synchronized void stopUI()
        {
        final String SOURCE = "ChartUIComponent.stopUI() ";

        // These don't do anything useful for now!
        if (getChannelSelectorOccupant() != null)
            {
            getChannelSelectorOccupant().stopUI();
            }

        if (getDatasetDomainControlOccupant() != null)
            {
            getDatasetDomainControlOccupant().stopUI();
            }

        // Neither does this
        super.stopUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[uistate=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        final String SOURCE = "ChartUIComponent.disposeUI() ";

        SwingWorker.disposeWorker(workerRefresh, true, SWING_WORKER_STOP_DELAY);
        workerRefresh = null;

        clearUIComponentContextActionGroups();

        // Remove all UIComponents...
        getChartContainer().removeAll();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "setChartPanel(NULL)");
        setChartPanel(null);

        if (getChannelSelectorOccupant() != null)
            {
            getChannelSelectorOccupant().removeChannelSelectionChangedListener(this);
            getChannelSelectorOccupant().disposeUI();
            }

        if (getDatasetDomainControlOccupant() != null)
            {
            getDatasetDomainControlOccupant().removeDatasetDomainChangedListener(this);
            getDatasetDomainControlOccupant().disposeUI();
            }

        super.disposeUI();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "[uistate=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        final String SOURCE = "ChartUIComponent.removeUIIdentity() ";

        super.removeUIIdentity();

        if ((getChannelSelectorOccupant() != null)
            && (getChannelSelectorOccupant().getSelectorContainer() != null))
            {
            getChannelSelectorOccupant().getSelectorContainer().removeAll();
            getChannelSelectorOccupant().getSelectorContainer().revalidate();
            }

        if ((getDatasetDomainControlOccupant() != null)
            && (getDatasetDomainControlOccupant().getDatasetDomainContainer() != null))
            {
            getDatasetDomainControlOccupant().getDatasetDomainContainer().removeAll();
            getDatasetDomainControlOccupant().getDatasetDomainContainer().revalidate();
            }

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "setChartPanel(NULL)");
        setChartPanel(null);

        if (getChartContainer() != null)
            {
            getChartContainer().removeAll();
            getChartContainer().add(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT), BorderLayout.CENTER);
            getChartContainer().revalidate();
            }

        revalidate();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "Cleared Chart container and Chart [uistate=" + getUIState().getName() + "]");
        }


    /**********************************************************************************************/
    /* Chart Creation and Refresh                                                                 */
    /***********************************************************************************************
     * Create or refresh the visible Chart on a separate thread.
     * Only create or update if the Instrument is selected and the Chart tab is visible.
     *
     * @param message
     */

    //    public synchronized void refreshChart(final String message)
    //        {
    //        final String SOURCE = "ChartUIComponent.refreshChart(NO PARAMETERS) ";
    //
    //        if ((getHostInstrument() != null)
    //            && (getHostInstrument().getDAO() != null)
    //            && (getHostInstrument().getDAO().getXYDataset() != null))
    //            {
    //            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
    //                         Logger.CONSOLE_SEPARATOR_MAJOR);
    //            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
    //                         SOURCE + "DAO [classname=" + getHostInstrument().getDAO().getClass().getName()
    //                                + "] [name=" + getHostInstrument().getDAO().getInstrumentName()
    //                                + "] [crosshair.range=" + getRangeCrosshair()
    //                                + "] [crosshair.domain=" + getDomainCrosshair()
    //                                + "] [channel.count.raw=" + getHostInstrument().getDAO().getRawDataChannelCount()
    //                                + "] [channel.count.xy=" + getHostInstrument().getDAO().getXYDataset().getSeriesCount()
    //                                + "]");
    //            }
    //
    //        doRefreshChart(getHostInstrument().getDAO(),
    //                       (ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()))
    //                        && (UIComponentHelper.isUIComponentShowing(this)),
    //                       message + " --> " + SOURCE);
    //        }


    /***********************************************************************************************
     * Conditionally create or refresh the Chart on a separate thread.
     * Only create or update if asked to do so via the generate flag.
     * Intended for use in exporting Charts which are not currently visible.
     *
     * @param dao
     * @param generateflag
     * @param message
     */

    public synchronized void refreshChart(final ObservatoryInstrumentDAOInterface dao,
                                          final boolean generateflag,
                                          final String message)
        {
        final String SOURCE = "ChartUIComponent.refreshChart(DAO) ";

        if ((dao != null)
            && (dao.getXYDataset() != null))
            {
            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         Logger.CONSOLE_SEPARATOR_MAJOR);
            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "DAO [classname=" + dao.getClass().getName()
                         + "] [name=" + dao.getInstrumentName()
                         + "] [crosshair.range=" + getRangeCrosshair()
                         + "] [crosshair.domain=" + getDomainCrosshair()
                         + "] [channel.count.raw=" + dao.getRawDataChannelCount()
                         + "] [channel.count.xy=" + dao.getXYDataset().getSeriesCount()
                         + "] [generate.flag=" + generateflag
                         + "]");
            }

        doRefreshChart(dao,
                       generateflag,
                       message + " --> " + SOURCE);
        }


    /***********************************************************************************************
     * Create or refresh the Chart on a separate thread.
     * Only create or update if asked to do so via the generate flag.
     *
     * @param dao
     * @param generateflag
     * @param message
     */

    private synchronized void doRefreshChart(final ObservatoryInstrumentDAOInterface dao,
                                             final boolean generateflag,
                                             final String message)
        {
        //final String SOURCE = "ChartUIComponent.refreshChart() ";
        final String SOURCE = message;
        final ChartUIComponentPlugin thisUI;
        final boolean boolDebug;

        boolDebug = true;
//        boolDebug = LOADER_PROPERTIES.isMetadataDebug()
//                    || LOADER_PROPERTIES.isChartDebug();

        // For use in inner classes
        thisUI = this;

        // Stop any existing SwingWorker
        if ((getHostInstrument() != null)
            && (dao != null))
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "DAO [dao.classname=" + dao.getClass().getName()
                         + "] [instrument.name=" + dao.getInstrumentName()
                         + "] [generate.flag=" + generateflag
                         + "]");

            LOGGER.debug(boolDebug,
                         SOURCE + "SwingWorker.controlledStop()");
            }

        SwingWorker.disposeWorker(workerRefresh, true, SWING_WORKER_STOP_DELAY >> 1);

        // Fire off another thread to process the Chart
        workerRefresh = new SwingWorker(REGISTRY.getThreadGroup(),
                                        SOURCE + "SwingWorker [group=" + REGISTRY.getThreadGroup().getName()
                                        + "] [thread=" + getChartName() + "]")
        {
        public Object construct()
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "SwingWorker START -------------------------------------------------------------------------!");

            // The result of this Thread is a JFreeChart
            // which may have been produced in a long process...
            if ((!isStopping())
                && (getHostInstrument() != null)
                && (getHostInstrument().getInstrumentState().isOn()))
                {
                try
                    {
                    final JFreeChart chart;
                    final Calendar calObservatory;

                    // Either find the Current Observatory calendar, or provide a default
                    calObservatory = ObservatoryInstrumentHelper.getCurrentObservatoryCalendar(REGISTRY.getFramework(),
                                                                                               dao,
                                                                                               boolDebug);

                    //-----------------------------------------------------------------------------
                    // Debug the data we are trying to display

                    if (boolDebug)
                        {
                        // Dump the (partial) contents of each Series in the composite XYdataset
                        ChartHelper.dumpXYDataset(boolDebug,
                                                  calObservatory,
                                                  getPrimaryXYDataset(),
                                                  4,
                                                  SOURCE + "Original unmodified Primary XYDataset before channel or domain selection");

                        if ((getSecondaryXYDatasets() != null)
                            && (!getSecondaryXYDatasets().isEmpty()))
                            {
                            for (int intDatasetIndex= 0;
                                 intDatasetIndex < getSecondaryXYDatasets().size();
                                 intDatasetIndex++)
                                {
                                ChartHelper.dumpXYDataset(boolDebug,
                                                          calObservatory,
                                                          getSecondaryXYDatasets().get(intDatasetIndex),
                                                          4,
                                                          SOURCE + "Original unmodified Secondary XYDataset ["
                                                          + intDatasetIndex
                                                          + "] before channel or domain selection");
                                }
                            }
                        else
                            {
                            LOGGER.debug(boolDebug,
                                         SOURCE + "There are no SecondaryXYDatasets associated with this Chart");
                            }
                        }

                    //-----------------------------------------------------------------------------
                    // Apply a ChartUI and its Metadata to the specified DAO

                    ChartHelper.associateChartUIWithDAO(thisUI, getMetadata(), dao);

                    //-----------------------------------------------------------------------------
                    // Create a new Channel Selector showing the channels of the Primary Dataset

                    if ((getChannelSelectorOccupant() != null)
                        && (getHostInstrument().getInstrumentState().isOn())
                        && (dao != null)
                        && (generateflag))
                        {
                        LOGGER.debug(boolDebug,
                                     SOURCE + "SwingWorker --> ChannelSelector --> createOrUpdateSelectors()");

                        // The Channel Selector will be empty until an XYDataset is loaded
                        // Note that ChannelCount etc. must be set before calling createOrUpdateSelectors()
                        getChannelSelectorOccupant().setChannelCount(getChannelCount());
                        getChannelSelectorOccupant().setTemperatureChannel(hasTemperatureChannel());
                        getChannelSelectorOccupant().setMetadata(getMetadata());
                        getChannelSelectorOccupant().setUpdateType(getUpdateType());

                        // Force a rebuild of the Channel Selector only if necessary
                        getChannelSelectorOccupant().createOrUpdateSelectors(getDatasetType(),
                                                                             getPrimaryXYDataset(),
                                                                             getSecondaryXYDatasets(),
                                                                             dao.isDatasetTypeChanged(),
                                                                             dao.isChannelCountChanged(),
                                                                             dao.isMetadataChanged(),
                                                                             dao.isRawDataChanged(),
                                                                             dao.isProcessedDataChanged(),
                                                                             isRefreshable(),
                                                                             isClickRefresh(),
                                                                             boolDebug);
                        }
                    else
                        {
                        // This debug ASSUMES the Instrument is not NULL
                        LOGGER.debug(boolDebug,
                                     SOURCE + "Unable to configure the Channel Selector UIComponent"
                                     + "  [has.channelselector=" + (hasChannelSelector())
                                     + "] [channelselector.notnull=" + (getChannelSelectorOccupant() != null)
                                     + "] [isselectedinstrument=" + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                     + "] [isinstrument.on=" + getHostInstrument().getInstrumentState().isOn()
                                     + "] [dao.notnull=" + (dao != null)
                                     + "] [generateflag=" + generateflag
                                     + "]");
                        }

                    //-----------------------------------------------------------------------------
                    // Force a rebuild of the DatasetDomain Control only if necessary

                    if ((hasDatasetDomainControl())
                        && (getDatasetDomainControlOccupant() != null)
                        && (getHostInstrument().getInstrumentState().isOn())
                        && (dao != null)
                        && (generateflag))
                        {
                        LOGGER.debug(boolDebug,
                                     SOURCE + "SwingWorker --> DatasetDomainControl --> createOrUpdateDomainControl()");

                        // Note that ChannelCount etc. must be set before calling createOrUpdateDomainControl()
                        getDatasetDomainControlOccupant().setRawDataChannelCount(getChannelCount());
                        getDatasetDomainControlOccupant().setTemperatureChannel(hasTemperatureChannel());
                        getDatasetDomainControlOccupant().setMetadata(getMetadata());
                        getDatasetDomainControlOccupant().setUpdateType(getUpdateType());

                        getDatasetDomainControlOccupant().createOrUpdateDomainControl(getDatasetType(),
                                                                                      getPrimaryXYDataset(),
                                                                                      getSecondaryXYDatasets(),
                                                                                      dao.isDatasetTypeChanged(),
                                                                                      dao.isChannelCountChanged(),
                                                                                      dao.isMetadataChanged(),
                                                                                      dao.isRawDataChanged(),
                                                                                      dao.isProcessedDataChanged(),
                                                                                      isRefreshable(),
                                                                                      isClickRefresh(),
                                                                                      boolDebug);
                        }
                    else
                        {
                        // This debug ASSUMES the Instrument is not NULL
                        LOGGER.debug(boolDebug,
                                     SOURCE + "Unable to configure the DatasetDomainControl UIComponent"
                                     + "  [has.domaincontrol=" + (hasDatasetDomainControl())
                                     + "] [domaincontrol.notnull=" + (getDatasetDomainControlOccupant() != null)
                                     + "] [isselectedinstrument=" + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                     + "] [isinstrument.on=" + getHostInstrument().getInstrumentState().isOn()
                                     + "] [dao.notnull=" + (dao != null)
                                     + "] [generateflag=" + generateflag
                                     + "]");
                        }

                    // Do this anyway, because it doesn't affect the UI
                    updateSliderLocalValues();

                    //-----------------------------------------------------------------------------
                    // If the Chart does not exist, create it
                    // If the Chart structure has changed, recreate it using the new configuration
                    // If only the data have changed, update the existing JFreeChart
                    // and return NULL here, to avoid redrawing

                    LOGGER.debug(boolDebug,
                                 SOURCE + "SwingWorker --> createOrUpdateChart()");

                    // Use the Observatory ResourceKey
                    setDisplayLimit(REGISTRY.getIntegerProperty(getHostInstrument().getHostAtom().getResourceKey() + ResourceKeys.KEY_DISPLAY_DATA_MAX));

                    // Only create or update if asked to do so
                    // This is the last step, so can reset the DAO changed status flags
                    chart = ChartHelper.createOrUpdateChart(getHostInstrument(),
                                                            thisUI,
                                                            dao,
                                                            generateflag,
                                                            getDatasetType(),
                                                            getPrimaryXYDataset(),
                                                            getSecondaryXYDatasets(),
                                                            getUpdateType(),
                                                            isRefreshable(),
                                                            isClickRefresh(),
                                                            getDisplayLimit(),
                                                            getDatasetDomainStartPoint(),
                                                            getDatasetDomainEndPoint(),
                                                            getChannelSelectorOccupant(),
                                                            boolDebug);
                    return (chart);
                    }

                catch (final Exception exception)
                    {
                    LOGGER.debug(boolDebug,
                                 SOURCE + "SwingWorker Thread GENERIC EXCEPTION");
                    exception.printStackTrace();
                    return null;
                    }
                }
            else
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "SwingWorker Thread stopping, or the Instrument has been turned OFF...");
                return (null);
                }
            }

        // Return a JFreeChart or NULL, depending on the outcome of createOrUpdateChart()
        // If NULL, don't affect the ChartPanel contents
        public void finished()
            {
            MetadataHelper.showMetadataList(getMetadata(),
                                            SOURCE + "Chart Metadata on arrival at finished()",
                                            boolDebug);

            // Update the Chart on the Event Dispatching Thread
            // Check thoroughly that there is some point to this update...
            if ((workerRefresh != null)
                && (workerRefresh.get() != null)
                && (workerRefresh.get() instanceof JFreeChart)
                && (SwingUtilities.isEventDispatchThread())
                && (!isStopping())
                && (getHostInstrument() != null)
                && (getHostInstrument().getInstrumentState().isOn()))
                {
                // See if we already have a ChartPanel to hold the new JFreeChart
                if (getChartPanel() != null)
                    {
                    LOGGER.debug(boolDebug,
                                 SOURCE + "finished() Apply the JFreeChart returned by get() to exisiting ChartPanel");

                    getChartPanel().setChart((JFreeChart) workerRefresh.get());
                    }
                else
                    {
                    // There is NO ChartPanel, so start again from scratch
                    LOGGER.debug(boolDebug,
                                 SOURCE + "finished() Create a NEW ChartPanel and apply the JFreeChart returned by get()");

                    setChartPanel(ChartPanelFactory.createChartPanel((JFreeChart) workerRefresh.get()));
                    }

                // NOTE: Chart applied to the specified DAO which was passed as a parameter
                LOGGER.debug(boolDebug,
                             SOURCE + "finished() returned JFreeChart on a ChartPanel");

                getChartContainer().removeAll();
                getChartContainer().add(getChartPanel(), BorderLayout.CENTER);
                getChartPanel().getChart().fireChartChanged();
                getChartContainer().revalidate();
                }
            else
                {
                // We failed to return a JFreeChart for some reason

                if (getPrimaryXYDataset() == null)
                    {
                    LOGGER.debug(boolDebug,
                                 SOURCE + "finished() No JFreeChart returned, Dataset is NULL, show BlankUIComponent");

                    // Getting here with a NULL dataset should mean no Chart!
                    // ChartContainer is always NOT NULL
                    getChartContainer().removeAll();
                    getChartContainer().add(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT), BorderLayout.CENTER);
                    getChartContainer().revalidate();

                    // ToDo Consider setting DAO Chart and ChartPanel Chart to NULL?
                    //SOURCE + "finished() No JFreeChart returned, Dataset is NULL, set Chart to NULL if possible");

                    //                        if (getChartPanel() != null)
                    //                            {
                    //                            getChartPanel().setChart(null);
                    //                            }
                    }
                else
                    {
                    // We have some data, if we also have a Chart, just display it again
                    if ((getChartPanel() != null)
                        && (getChartPanel().getChart() != null))
                        {
                        LOGGER.debug(boolDebug,
                                     SOURCE + "finished() No JFreeChart returned, Dataset is NOT NULL, redraw data on existing ChartPanel");

                        getChartContainer().removeAll();
                        getChartContainer().add(getChartPanel(), BorderLayout.CENTER);
                        getChartPanel().getChart().fireChartChanged();
                        getChartContainer().revalidate();
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "finished() No JFreeChart returned, Dataset is NOT NULL, but no Chart for display, no action taken");
                        }
                    }
                }

            // Handle the DatasetDomainControl

            if ((getChartPanel() != null)
                && (hasDatasetDomainControl())
                && (getDatasetDomainControlOccupant() != null)
                && (getDatasetDomainControlOccupant().getDatasetDomainContainer() != null))
                {
                final ChartRenderingInfo infoChart;
                final PlotRenderingInfo infoPlot;
                final Rectangle2D rectPlot;
                final Rectangle2D rectData;

                infoChart = getChartPanel().getChartRenderingInfo();
                infoPlot = infoChart.getPlotInfo();
                rectPlot = infoPlot.getPlotArea();
                rectData = infoPlot.getDataArea();

                // Trap the cases where the Plot hasn't been rendered, or there's nothing to lay out,
                // in which cases use the default sizes
                if  ((rectPlot != null)
                     && (rectData != null)
                     && ((int)rectData.getWidth() > 0)
                     && ((int)rectData.getHeight() > 0)
                     && ((int)rectData.getX() > 0)
                     && ((int)rectData.getY() > 0))
                    {
                    int intLeft;
                    int intRight;

                    // Try to get the slider to align with the extents of the data area
                    // Ideally this should happen also after WindowResizing events
                    intLeft = (int)rectData.getX();
                    intRight = (int)(getChartPanel().getWidth() - rectData.getWidth() - rectData.getX());

                    LOGGER.debug(boolDebug,
                                 "DatasetDomainControl -- PANEL, PLOT & DATA AREAS"
                                 + "  [panel.width=" + getChartPanel().getWidth()
                                 + "] [panel.height=" + getChartPanel().getHeight()
                                 + "] [plot.width=" + rectPlot.getWidth()
                                 + "] [plot.height=" + rectPlot.getHeight()
                                 + "] [plot.x=" + rectPlot.getX()
                                 + "] [plot.y=" + rectPlot.getY()
                                 + "] [data.width=" + rectData.getWidth()
                                 + "] [data.height=" + rectData.getHeight()
                                 + "] [data.x=" + rectData.getX()
                                 + "] [data.y=" + rectData.getY()
                                 + "] [indent.left=" + intLeft
                                 + "] [indent.right=" + intRight
                                 + "]");

                    if (intLeft < 0)
                        {
                        intLeft = 0;
                        }

                    if (intRight < 0)
                        {
                        intRight = 0;
                        }

                    if ((intLeft + rectData.getWidth() + intRight) > getChartPanel().getWidth())
                        {
                        intRight = 5;
                        }

                    getDatasetDomainControlOccupant().getDatasetDomainContainer().setBorder(BorderFactory.createEmptyBorder(0,
                                                                                                                            intLeft,
                                                                                                                            5,
                                                                                                                            intRight));
                    }
                else
                    {
                    getDatasetDomainControlOccupant().getDatasetDomainContainer().setBorder(BorderFactory.createEmptyBorder(0,
                                                                                                                            INDENT_LEFT,
                                                                                                                            5,
                                                                                                                            INDENT_RIGHT));
                    }

                // Layout the Domain slider control again
                getDatasetDomainControlOccupant().getDatasetDomainContainer().revalidate();
                }

            LOGGER.debug(boolDebug,
                         SOURCE + "SwingWorker.finished() STOP ---------------------------------------------------------------!\n");
            }
        };

        // Start the Thread we have prepared...
        workerRefresh.start();
        }


    /**********************************************************************************************
     * Create a new Chart and customise the XYPlot, e.g. for fixed range axes.
     *
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param updatetype
     * @param displaylimit
     * @param channelselector
     * @param debug
     *
     * @return JFreeChart
     */

    public JFreeChart createCustomisedChart(final DatasetType datasettype,
                                            final XYDataset primarydataset,
                                            final List<XYDataset> secondarydatasets,
                                            final DataUpdateType updatetype,
                                            final int displaylimit,
                                            final ChannelSelectorUIComponentInterface channelselector,
                                            final boolean debug)
        {
        final String SOURCE = "ChartUIComponent.createCustomisedChart() ";

        LOGGER.error(SOURCE + "Chart subclass is missing implementation!");

        return (null);
        }


    /***********************************************************************************************
     * Update the existing Chart referenced by the DAO, by applying the specified datasets,
     * using all existing channel selections and range selections.
     * This implementation ASSUMES a ChannelSelector is present.
     *
     * @param dao
     * @param datasettype
     * @param primarydataset
     * @param secondarydatasets
     * @param displaylimit
     * @param domainstartpoint
     * @param domainendpoint
     * @param channelselector
     * @param debug
     */

    public void updateChartForSelection(final ObservatoryInstrumentDAOInterface dao,
                                        final DatasetType datasettype,
                                        final XYDataset primarydataset,
                                        final List<XYDataset> secondarydatasets,
                                        final int displaylimit,
                                        final int domainstartpoint,
                                        final int domainendpoint,
                                        final ChannelSelectorUIComponentInterface channelselector,
                                        final boolean debug)
        {
        final String SOURCE = "ChartUIComponent.updateChartForSelection() ";

        if ((dao != null)
            && (dao.getChartUI() != null)
            && (dao.getChartUI().getChartPanel() != null)
            && (dao.getChartUI().getChartPanel().getChart() != null)
            && (dao.getChartUI().getChartPanel().getChart().getXYPlot() != null)
            && (datasettype != null)
            && (primarydataset != null)
            && (secondarydatasets != null)
            && (channelselector != null))
            {
            final XYDataset xyNewPrimaryDataset;
            final List<XYDataset> listNewSecondaryDatasets;
            final List<XYDataset> listParentSecondaryDatasetForSeries;
            final Iterator iterOriginalSecondaryDatasets;
            final Calendar calObservatory;

            // Either find the Current Observatory calendar, or provide a default
            calObservatory = ObservatoryInstrumentHelper.getCurrentObservatoryCalendar(REGISTRY.getFramework(),
                                                                                       dao,
                                                                                       debug);

            // Create a list of NewSecondaryDatasets for display
            // Add an appropriate *empty* new dataset for every one existing in the secondary set
            // and record the parent new Dataset for each Series in each original Dataset
            // So much complexity just to handle the very few cases of secondary datasets...

            listNewSecondaryDatasets = new ArrayList<XYDataset>(10);
            listParentSecondaryDatasetForSeries = new ArrayList<XYDataset>(10);

            iterOriginalSecondaryDatasets = secondarydatasets.iterator();

            while (iterOriginalSecondaryDatasets.hasNext())
                {
                final XYDataset xyDatasetOriginal;

                xyDatasetOriginal = (XYDataset) iterOriginalSecondaryDatasets.next();

                if ((xyDatasetOriginal != null)
                    && (xyDatasetOriginal.getSeriesCount() > 0))
                    {
                    final XYDataset xyDatasetNew;

                    // Create an empty Dataset to correspond with the original
                    if (datasettype.getName().equals(DatasetType.XY.getName()))
                        {
                        xyDatasetNew = new XYSeriesCollection();
                        listNewSecondaryDatasets.add(xyDatasetNew);
                        }
                    else if (datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                        {
                        xyDatasetNew = new TimeSeriesCollection(calObservatory.getTimeZone());
                        listNewSecondaryDatasets.add(xyDatasetNew);
                        }
                    else
                        {
                        xyDatasetNew = new XYSeriesCollection();
                        listNewSecondaryDatasets.add(xyDatasetNew);
                        }

                    // Record the *same* parent Dataset for each Series in each original Secondary Dataset
                    // This creates a List in channel order, but with references to Datasets not Series
                    for (int i = 0;
                         i < xyDatasetOriginal.getSeriesCount();
                         i++)
                        {
                        listParentSecondaryDatasetForSeries.add(xyDatasetNew);
                        }
                    }
                }

            // Confirm the DatasetType
            if ((datasettype.getName().equals(DatasetType.XY.getName()))
                && (primarydataset instanceof XYSeriesCollection))
                {
                final XYSeriesCollection collectionPrimary;

                // Prepare a new XYSeriesCollection for display
                xyNewPrimaryDataset = new XYSeriesCollection();

                // There should be a collection of <channelcount> XYSeries in the Primary Dataset
                // but there may also be some in the Secondary Datasets
                collectionPrimary = (XYSeriesCollection) primarydataset;

                // ToDo Make this work for ChannelSelector NULL, get channel count from primary and secondaries? Set mode to X1?

                if ((collectionPrimary.getSeriesCount() > 0)
                    && (collectionPrimary.getSeries() != null)
                    && (channelselector.getChannelSelectionModes() != null))
                    {
                    LOGGER.debug(debug,
                                 SOURCE + "XY domainstartpoint=" + domainstartpoint + " domainendpoint=" + domainendpoint);

                    ChartHelper.dumpXYDataset(debug,
                                              calObservatory,
                                              collectionPrimary,
                                              4,
                                              SOURCE + "XYSeriesCollection --> Original Dataset");

                    // Find which channels and data range to use this time round
                    for (int intChannelIndex = 0;
                         intChannelIndex < channelselector.getChannelSelectionModes().size();
                         intChannelIndex++)
                        {
                        final ChannelSelectionMode selectionMode;

                        selectionMode = channelselector.getChannelSelectionModes().get(intChannelIndex);

                        if (!ChannelSelectionMode.OFF.equals(selectionMode))
                            {
                            final XYSeries seriesOriginalData;

                            // We needed to know about the secondary datasets in order to get the correct index
                            seriesOriginalData = (XYSeries) ChartHelper.getSeriesForIndex(datasettype,
                                                                                          primarydataset,
                                                                                          secondarydatasets,
                                                                                          intChannelIndex,
                                                                                          debug);
                            if (seriesOriginalData != null)
                                {
                                final XYSeries seriesChangedData;
                                final List listOriginalDataItems;
                                int intStartIndex;
                                int intEndIndex;

                                listOriginalDataItems = seriesOriginalData.getItems();

                                // Prepare a new Series for the changed data, with the same Key
                                seriesChangedData = new XYSeries(seriesOriginalData.getKey());

                                if (listOriginalDataItems.size() > 0)
                                    {
                                    // Map the slider values to data indexes
                                    intStartIndex = ChartHelper.transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                                        ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                                        domainstartpoint,
                                                                                                        DatasetDomainUIComponentInterface.INDEX_LEFT,
                                                                                                        collectionPrimary.getDomainLowerBound(true),
                                                                                                        collectionPrimary.getDomainUpperBound(true),
                                                                                                        DatasetType.XY,
                                                                                                        calObservatory,
                                                                                                        seriesOriginalData,
                                                                                                        debug);

                                    intEndIndex = ChartHelper.transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                                      ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                                      domainendpoint,
                                                                                                      DatasetDomainUIComponentInterface.INDEX_RIGHT,
                                                                                                      collectionPrimary.getDomainLowerBound(true),
                                                                                                      collectionPrimary.getDomainUpperBound(true),
                                                                                                      DatasetType.XY,
                                                                                                      calObservatory,
                                                                                                      seriesOriginalData,
                                                                                                      debug);
                                    if ((intStartIndex == -1)
                                        || (intEndIndex == -1))
                                        {
                                        // If either index is returned as -1, then there's nothing to do...
                                        // ...so stop the for() loop
                                        intStartIndex = 0;
                                        intEndIndex = 0;
                                        }
                                    else if (intEndIndex <= intStartIndex)
                                        {
                                        intEndIndex = intStartIndex + 1;
                                        }

                                    LOGGER.debug(debug,
                                                 SOURCE + "before copy of selected series subset [channel=" + intChannelIndex
                                                 + "] [index.start=" + intStartIndex
                                                 + "] [index.end=" + intEndIndex + "]");

                                    // Copy over only the selected range from the Slider
                                    for (int intDataIndex = intStartIndex;
                                         intDataIndex < intEndIndex;
                                         intDataIndex++)
                                        {
                                        final XYDataItem dataOriginalItem;
                                        final XYDataItem dataChangedItem;

                                        dataOriginalItem = (XYDataItem)listOriginalDataItems.get(intDataIndex);

                                        if (!ChannelSelectionMode.X1.equals(selectionMode))
                                            {
                                            // Change each value of the series according to the multiplier
                                            dataChangedItem = new XYDataItem(dataOriginalItem.getX(),
                                                                             dataOriginalItem.getY().doubleValue() *  selectionMode.getMultiplier());
                                            }
                                        else
                                            {
                                            // Just use the whole series unaltered for gain of X1
                                            dataChangedItem = new XYDataItem(dataOriginalItem.getX(),
                                                                             dataOriginalItem.getY());
                                            }

                                        seriesChangedData.add(dataChangedItem);
                                        }

                                    // Did we collect any data for this Series?
                                    // If not, place a dummy point at the origin of the *visible* chart
                                    if (seriesChangedData.getItemCount() == 0)
                                        {
                                        // TODO ChartHelper.createDummyXYSeriesDataItemAtOrigin()
                                        seriesChangedData.add(new XYDataItem(0,
                                                                             0));
                                        }

                                    // Place the changed series in the correct collection
                                    // to correspond with the original
                                    if (intChannelIndex < collectionPrimary.getSeriesCount())
                                        {
                                        // Simply add the changed Primary series to the PrimaryDataset collection
                                        ((XYSeriesCollection)xyNewPrimaryDataset).addSeries(seriesChangedData);
                                        }
                                    else
                                        {
                                        // It must be a secondary dataset
                                        // Add the changed Secondary series to the parent SecondaryDataset
                                        // given by the *secondary* channel index
                                        ChartHelper.addSecondarySeries(datasettype,
                                                                       listParentSecondaryDatasetForSeries,
                                                                       seriesChangedData,
                                                                       intChannelIndex - collectionPrimary.getSeriesCount());
                                        }
                                    }
                                else
                                    {
                                    LOGGER.warn(SOURCE + "OriginalData XYSeries unexpectedly NULL");
                                    }
                                }
                            else
                                {
                                // There are no data! Do nothing...
                                LOGGER.warn(SOURCE + "There are no data, so do nothing");
                                }
                            }
                        }

                    LOGGER.debug(debug,
                                 SOURCE + "Update the data shown on existing Chart");

                    // The outputs are xyNewPrimaryDataset and listNewSecondaryDatasets
                    // This Chart does not use secondary datasets (yet)

                    // Dump the (partial) contents of each Series in the new XYdataset
                    ChartHelper.dumpXYDataset(debug,
                                              calObservatory,
                                              xyNewPrimaryDataset,
                                              4,
                                              SOURCE + "XYSeriesCollection --> setDataset");

                    dao.getChartUI().getChartPanel().getChart().getXYPlot().setDataset(INDEX_DATA, xyNewPrimaryDataset);
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The XYSeriesCollection does not have any XYSeries");
                    }
                }
            else if ((datasettype.getName().equals(DatasetType.TIMESTAMPED.getName()))
                     && (primarydataset instanceof TimeSeriesCollection))
                {
                final TimeSeriesCollection collectionPrimary;

                LOGGER.debug(debug,
                             SOURCE + "TIMESTAMPED domainstartpoint=" + domainstartpoint + " domainendpoint=" + domainendpoint);

                // Prepare a new TimeSeriesCollection for display
                xyNewPrimaryDataset = new TimeSeriesCollection(calObservatory.getTimeZone());

                // There should be a collection of <channelcount> TimeSeries in the Primary Dataset
                // but there may also be some in the Secondary Datasets
                collectionPrimary = (TimeSeriesCollection) primarydataset;

                if ((collectionPrimary.getSeriesCount() > 0)
                    && (collectionPrimary.getSeries() != null)
                    && (channelselector.getChannelSelectionModes() != null))
                    {
                    ChartHelper.dumpXYDataset(debug,
                                              calObservatory,
                                              collectionPrimary,
                                              4,
                                              SOURCE + "TimeSeriesCollection PrimaryDataset --> Original Dataset");

                    // Find which channels and data range to use this time round
                    for (int intChannelIndex = 0;
                         intChannelIndex < channelselector.getChannelSelectionModes().size();
                         intChannelIndex++)
                        {
                        final ChannelSelectionMode selectionMode;

                        selectionMode = channelselector.getChannelSelectionModes().get(intChannelIndex);

                        if (!ChannelSelectionMode.OFF.equals(selectionMode))
                            {
                            final TimeSeries seriesOriginalData;

                            // We needed to know about the secondary datasets in order to get the correct index
                            seriesOriginalData = (TimeSeries) ChartHelper.getSeriesForIndex(datasettype,
                                                                                            primarydataset,
                                                                                            secondarydatasets,
                                                                                            intChannelIndex,
                                                                                            debug);
                            if (seriesOriginalData != null)
                                {
                                final List listOriginalDataItems;
                                final TimeSeries seriesChangedData;

                                listOriginalDataItems = seriesOriginalData.getItems();

                                // Prepare a new Series for the changed data, with the same Key
                                seriesChangedData = new TimeSeries(seriesOriginalData.getKey().toString(),
                                                                   seriesOriginalData.getTimePeriodClass());

                                if (listOriginalDataItems.size() > 0)
                                    {
                                    int intStartIndex;
                                    int intEndIndex;

                                    // Map the slider values to data indexes
                                    intStartIndex = ChartHelper.transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                                        ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                                        domainstartpoint,
                                                                                                        DatasetDomainUIComponentInterface.INDEX_LEFT,
                                                                                                        collectionPrimary.getDomainLowerBound(true),
                                                                                                        collectionPrimary.getDomainUpperBound(true),
                                                                                                        DatasetType.TIMESTAMPED,
                                                                                                        calObservatory,
                                                                                                        seriesOriginalData,
                                                                                                        debug);

                                    intEndIndex = ChartHelper.transformDomainSliderValueToSeriesIndex(ChartUIComponentPlugin.DOMAIN_SLIDER_MINIMUM,
                                                                                                      ChartUIComponentPlugin.DOMAIN_SLIDER_MAXIMUM,
                                                                                                      domainendpoint,
                                                                                                      DatasetDomainUIComponentInterface.INDEX_RIGHT,
                                                                                                      collectionPrimary.getDomainLowerBound(true),
                                                                                                      collectionPrimary.getDomainUpperBound(true),
                                                                                                      DatasetType.TIMESTAMPED,
                                                                                                      calObservatory,
                                                                                                      seriesOriginalData,
                                                                                                      debug);
                                    if ((intStartIndex == -1)
                                        || (intEndIndex == -1))
                                        {
                                        // If either index is returned as -1, then there's nothing to do...
                                        // ...so stop the for() loop
                                        LOGGER.debug(debug,
                                                     SOURCE + "Set EndIndex = StartIndex = 0");
                                        intStartIndex = 0;
                                        intEndIndex = 0;
                                        }
                                    else if (intEndIndex <= intStartIndex)
                                        {
                                        LOGGER.debug(debug,
                                                     SOURCE + "Correcting EndIndex less than StartIndex");
                                        intEndIndex = intStartIndex + 1;
                                        }

                                    LOGGER.debug(debug,
                                                 SOURCE + "before copy of selected series subset [channel=" + intChannelIndex
                                                 + "] [start_index=" + intStartIndex
                                                 + "] [end_index=" + intEndIndex + "]");

                                    // Copy over only the selected range from the Slider
                                    for (int intDataIndex = intStartIndex;
                                         intDataIndex < intEndIndex;
                                         intDataIndex++)
                                        {
                                        final TimeSeriesDataItem dataOriginalItem;
                                        final TimeSeriesDataItem dataChangedItem;

                                        dataOriginalItem = (TimeSeriesDataItem)listOriginalDataItems.get(intDataIndex);

                                        if (!ChannelSelectionMode.X1.equals(selectionMode))
                                            {
                                            // Change each value of the series according to the multiplier
                                            dataChangedItem = new TimeSeriesDataItem(dataOriginalItem.getPeriod(),
                                                                                     dataOriginalItem.getValue().doubleValue() * selectionMode.getMultiplier());
                                            }
                                        else
                                            {
                                            // Just use the whole series unaltered for gain of X1
                                            dataChangedItem = new TimeSeriesDataItem(dataOriginalItem.getPeriod(),
                                                                                     dataOriginalItem.getValue().doubleValue());
                                            }

                                        seriesChangedData.add(dataChangedItem);
                                        }

                                    // Did we collect any data for this Series?
                                    // If not, place a dummy point at the origin of the *visible* chart
                                    if (seriesChangedData.getItemCount() == 0)
                                        {
                                        seriesChangedData.add(ChartHelper.createDummyTimeSeriesDataItemAtOrigin(collectionPrimary,
                                                                                                                seriesOriginalData,
                                                                                                                domainstartpoint,
                                                                                                                debug));
                                        }

                                    // Place the changed series in the correct collection
                                    // to correspond with the original
                                    if (intChannelIndex < collectionPrimary.getSeriesCount())
                                        {
                                        // Simply add the changed Primary series to the PrimaryDataset collection
                                        ((TimeSeriesCollection)xyNewPrimaryDataset).addSeries(seriesChangedData);
                                        }
                                    else
                                        {
                                        // It must be a secondary dataset
                                        // Add the changed Secondary series to the parent SecondaryDataset
                                        // given by the *secondary* channel index
                                        ChartHelper.addSecondarySeries(datasettype,
                                                                       listParentSecondaryDatasetForSeries,
                                                                       seriesChangedData,
                                                                       intChannelIndex - collectionPrimary.getSeriesCount());
                                        }
                                    }
                                else
                                    {
                                    // There are no data! Do nothing...
                                    LOGGER.warn(SOURCE + "There are no data, so do nothing [channel.index=" + intChannelIndex + "]");
                                    }
                                }
                            else
                                {
                                LOGGER.warn(SOURCE + "OriginalData TimeSeries unexpectedly NULL [channel.index=" + intChannelIndex + "]");
                                }
                            }
                        else
                            {
                            LOGGER.debug(debug,
                                         SOURCE + "Channel is OFF [channel.index=" + intChannelIndex + "]");
                            }
                        }

                    LOGGER.debug(debug,
                                 SOURCE + "Update the data shown on existing Chart");

                    // The outputs are xyNewPrimaryDataset and listNewSecondaryDatasets
                    // This Chart superclass does not use secondary datasets (yet)

                    // Dump the (partial) contents of each Series in the new XYdataset
                    ChartHelper.dumpXYDataset(debug,
                                              calObservatory,
                                              xyNewPrimaryDataset,
                                              4,
                                              SOURCE + "TimeSeriesCollection NewPrimaryDataset --> setDataset");

                    dao.getChartUI().getChartPanel().getChart().getXYPlot().setDataset(INDEX_DATA, xyNewPrimaryDataset);
                    }
                else
                    {
                    LOGGER.error(SOURCE + " The TimeSeriesCollection does not have any TimeSeries");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + " The Dataset is of an invalid type");
                }
            }
        else
            {
            LOGGER.debug(debug,
                         SOURCE + " Unable to change the Chart - invalid parameters");
            }
        }


    /**********************************************************************************************/
    /* Data and Metadata                                                                          */
    /***********************************************************************************************
     * Get the DAO providing data for this Chart.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public ObservatoryInstrumentDAOInterface getDAO()
        {
        return (this.daoChart);
        }


    /***********************************************************************************************
     * Set the DAO providing data for this Chart.
     *
     * @param dao
     */

    public void setDAO(final ObservatoryInstrumentDAOInterface dao)
        {
        this.daoChart = dao;
        }


    /***********************************************************************************************
     * Get the List of Metadata for this Chart.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata for this Chart, and associate the Chart with the specified DAO.
     * Set updatedaometadata to TRUE if the specified Metadata should be copied into the
     * appropriate containers in the specified DAO.
     * If the Metadata was already collected from the DAO, just set updatedaometadata to FALSE.
     *
     * @param metadatalist
     * @param dao
     * @param updatedaometadata
     * @param debug
     */

    public void setMetadata(final List<Metadata> metadatalist,
                            final ObservatoryInstrumentDAOInterface dao,
                            final boolean updatedaometadata,
                            final boolean debug)
        {
        final String SOURCE = "ChartUIComponent.setMetadata() ";

        // Set the List of Metadata for this Chart
        this.listMetadata = metadatalist;

        // Associate the specified DAO (even if NULL) with the Chart
        setDAO(dao);

        // Associate the Chart with the specified DAO (if any)
        if (dao != null)
            {
            dao.setChartUI(this);

            // The specified Metadata should be copied into the appropriate containers in the DAO
            if (updatedaometadata)
                {
                dao.addAllMetadataToContainersTraced(metadatalist,
                                                     SOURCE,
                                                     debug);
                }
            }

        MetadataHelper.showMetadataList(metadatalist,
                                        SOURCE + "[chart=" + getChartName() + "]",
                                        debug);
        }


    /***********************************************************************************************
     * Get the Primary XYDataset to be shown on the ChartUIComponent.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     *
     * @return XYDataset
     */

    public XYDataset getPrimaryXYDataset()
        {
        return (this.xyDatasetPrimary);
        }


    /***********************************************************************************************
     * Set the primary XYDataset, adjust the type of the Dataset if required.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     * This relies on ChannelCount having been set by the caller.
     *
     * @param dao
     * @param dataset
     */

    public void setPrimaryXYDataset(final ObservatoryInstrumentDAOInterface dao,
                                    final XYDataset dataset)
        {
        final String SOURCE = "ChartUIComponent.setPrimaryXYDataset() ";

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(), SOURCE);

        // We can rely on ChannelCount having been set by the caller
        if ((dao != null)
            && (dataset != null)
            && (dataset.getSeriesCount() > 0))
            {
            final DatasetType typeNew;

            // Supported datasets: XYSeriesCollection and TimeSeriesCollection
            if (dataset instanceof XYSeriesCollection)
                {
                typeNew = DatasetType.XY;
                }
            else if (dataset instanceof TimeSeriesCollection)
                {
                typeNew = DatasetType.TIMESTAMPED;
                }
            else
                {
                LOGGER.error(SOURCE + " Unexpected DatasetType");
                typeNew = DatasetType.XY;
                }

            setDatasetType(typeNew);

            this.xyDatasetPrimary = dataset;
            this.xyDatasetSecondaries.clear();

            // Remove all crosshairs because this may be a new import or capture
            setRangeCrosshair(0.0);
            setDomainCrosshair(0.0);

            // Always reset to extents when new data arrive,
            // otherwise we might not see any realtime updates
            if ((hasDatasetDomainControl())
                && (getDatasetDomainControlOccupant() != null))
                {
                setDatasetDomainChanged(getDatasetDomainControlOccupant().resetToExtents());
                }

            LOGGER.debug(((LOADER_PROPERTIES.isChartDebug())
                          && (getHostInstrument() != null)
                          && (getDatasetType() != null)),
                         SOURCE + "New XYDataset configuration "
                         + "[dataset_type=" + getDatasetType().getName()  // NullPointerException will not occur
                         + "] [dataset_type.changed=" + dao.isDatasetTypeChanged()
                         + "] [channel_count.changed=" + dao.isChannelCountChanged()
                         + "] [channel_count=" + dataset.getSeriesCount()
                         + "] [dataset_domain.changed=" + isDatasetDomainChanged()
                         + "] [channel_selection.changed=" + isChannelSelectionChanged()
                         + "] [raw_data.changed=" + dao.isRawDataChanged()
                         + "] [processed_data.changed=" + dao.isProcessedDataChanged()
                         + "]");
            }
        else
            {
            // Force re-creation of the Chart to show a blank, since there is no DAO or dataset
            setDatasetType(null);
            setChannelCount(0);

            this.xyDatasetPrimary = null;
            this.xyDatasetSecondaries.clear();

            setRangeCrosshair(0.0);
            setDomainCrosshair(0.0);

            if ((hasDatasetDomainControl())
                && (getDatasetDomainControlOccupant() != null))
                {
                setDatasetDomainChanged(getDatasetDomainControlOccupant().resetToExtents());
                }

            notifyDatasetChangedEvent(this, 0, 0);

            // Don't show DAO or DatasetType, because they are NULL!
            LOGGER.debug(((LOADER_PROPERTIES.isChartDebug()) && (getHostInstrument() != null)),
                         SOURCE + "Supplied with invalid or null XYDataset "
                         + "[domain_changed=" + isDatasetDomainChanged()
                         + "] [dataset_notnull=" + (dataset != null)
                         + "]");
            }
        }


    /***********************************************************************************************
     * Get the Secondary XYDatasets to be shown on the ChartUIComponent.
     * This is rarely used: for example GOES Client uses it for the Ratio axis.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     *
     * @return List<XYDataset>
     */

    public List<XYDataset> getSecondaryXYDatasets()
        {
        return (this.xyDatasetSecondaries);
        }


    /***********************************************************************************************
     * Set the Secondary XYDatasets to be shown on the ChartUIComponent.
     * This is rarely used: for example GOES Client uses it for the Ratio axis.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     *
     * @param datasets
     */

    public void setSecondaryDatasets(final List<XYDataset> datasets)
        {
        this.xyDatasetSecondaries = datasets;
        }


    /***********************************************************************************************
     * Get the DatasetType - Tabular, XY or Timestamped.
     *
     * @return DatasetType
     */

    public DatasetType getDatasetType()
        {
        return (this.datasetType);
        }


    /***********************************************************************************************
     * Set the DatasetType - Tabular, XY or Timestamped.
     *
     * @param type
     */

    private void setDatasetType(final DatasetType type)
        {
        this.datasetType = type;
        }


    /***********************************************************************************************
     * Get the Update Type - Preserve, Truncate or Decimate.
     *
     * @return DataUpdateType
     */

    protected DataUpdateType getUpdateType()
        {
        return (this.updateType);
        }


    /***********************************************************************************************
     * Set the Update Type - Preserve, Truncate or Decimate.
     *
     * @param type
     */

    protected void setUpdateType(final DataUpdateType type)
        {
        this.updateType = type;
        }


    /***********************************************************************************************
     * Set up for mouse click refresh, or no refresh.
     */

    private synchronized void setRefreshType()
        {
        final String SOURCE = "ChartUIComponent.setRefreshType()";

        // Check to see if we need to refresh the Chart
        if (getChartContainer() != null)
            {
            if ((isRefreshable())
                && (isClickRefresh()))
                {
                // Refresh on a mouse click
                getChartContainer().setToolTipText(TOOLTIP_CLICK_HERE);

                getChartContainer().addMouseListener(new MouseAdapter()
                {
                public void mousePressed(final MouseEvent event)
                    {
                    LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                                 "CLICK! Refresh Chart " + getChartName());

                    // If there is a User click, then the Chart must be visible
                    refreshChart(getDAO(), true, SOURCE + "mousePressed() ");
                    }
                });
                }
            else
                {
                getChartContainer().setToolTipText(EMPTY_STRING);
                }
            }
        }


    /***********************************************************************************************
     * Get the Channel count.
     *
     * @return int
     */

    public int getChannelCount()
        {
        return (this.intChannelCount);
        }


    /***********************************************************************************************
     * Set the Channel count.
     *
     * @param count
     */

    public void setChannelCount(final int count)
        {
        this.intChannelCount = count;
        }


    /***********************************************************************************************
     * Indicate if the first data channel represents Temperature (Usually a Staribus dataset).
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (this.boolTemperatureChannel);
        }


    /***********************************************************************************************
     * Set a flag to indicate if the first data channel represents Temperature
     * (Usually a Staribus dataset).
     *
     * @param temperature
     */

    public void setTemperatureChannel(final boolean temperature)
        {
        this.boolTemperatureChannel = temperature;
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the JPanel holding the complete UI of the Chart Tab.
     *
     * @return JPanel
     */

    private JPanel getUIContainer()
        {
        return (this.panelUIContainer);
        }


    /***********************************************************************************************
     * Get the optional JToolBar.
     * Default is NULL, override if needed.
     *
     * @return JToolBar
     */

    public JToolBar getToolBar()
        {
        return (null);
        }


    /***********************************************************************************************
     * Get the Chart JPanel container.
     *
     * @return JPanel
     */

    public synchronized JPanel getChartContainer()
        {
        return (this.panelChartContainer);
        }


    /***********************************************************************************************
     * Get the Chart name.
     *
     * @return String
     */

    public synchronized final String getChartName()
        {
        return (this.strReportName);
        }


    /***********************************************************************************************
     * Set the Chart name.
     *
     * @param name
     */

    public synchronized final void setChartName(final String name)
        {
        strReportName = name;
        }


    /***********************************************************************************************
     * Get the ChartPanel containing the JFreeChart.
     *
     * @return ChartPanel
     */

    public final ChartPanel getChartPanel()
        {
        return (this.chartPanel);
        }


    /***********************************************************************************************
     * Set the ChartPanel containing the JFreeChart.
     *
     * @param chartpanel
     */

    public final void setChartPanel(final ChartPanel chartpanel)
        {
        this.chartPanel = chartpanel;
        }


    /***********************************************************************************************
     * Indicate if the Chart has a ChannelSelector.
     *
     * @return boolean
     */

    public boolean hasChannelSelector()
        {
        return (this.boolHasChannelSelector);
        }


    /***********************************************************************************************
     * Indicate if the Chart has a ChannelSelector.
     *
     * @param hasselector
     */

    public void setChannelSelector(final boolean hasselector)
        {
        this.boolHasChannelSelector = hasselector;
        }


    /***********************************************************************************************
     * Get the Chart Channel Selector.
     *
     * @return ChannelSelectorUIComponentInterface
     */

    public synchronized ChannelSelectorUIComponentInterface getChannelSelectorOccupant()
        {
        return (this.channelSelectorOccupant);
        }


    /***********************************************************************************************
     * Set the Chart Channel Selector.
     *
     * @param selector
     */

    public synchronized void setChannelSelectorOccupant(final ChannelSelectorUIComponentInterface selector)
        {
        this.channelSelectorOccupant = selector;
        }


    /***********************************************************************************************
     * Indicate if the Chart has a DatasetDomainControl.
     *
     * @return boolean
     */

    public boolean hasDatasetDomainControl()
        {
        return (this.boolHasDatasetDomainControl);
        }


    /***********************************************************************************************
     * Indicate if the Chart has a DatasetDomainControl.
     *
     * @param hasdomaincontrol
     */

    public void setDatasetDomainControl(final boolean hasdomaincontrol)
        {
        this.boolHasDatasetDomainControl = hasdomaincontrol;
        }


    /***********************************************************************************************
     * Get the DatasetDomain UIComponent.
     *
     * @return DatasetDomainUIComponentInterface
     */

    protected synchronized DatasetDomainUIComponentInterface getDatasetDomainControlOccupant()
        {
        return (this.datasetDomainControlOccupant);
        }


    /***********************************************************************************************
     * Set the DatasetDomain UIComponent.
     *
     * @param datasetdomain
     */

    private synchronized void setDatasetDomainControlOccupant(final DatasetDomainUIComponentInterface datasetdomain)
        {
        this.datasetDomainControlOccupant = datasetdomain;
        }


    /***********************************************************************************************
     * Indicate if the ChannelSelection of the chart data has changed.
     *
     * @return boolean
     */

    public boolean isChannelSelectionChanged()
        {
        return (this.boolChannelSelectionChanged);
        }


    /***********************************************************************************************
     * Indicate if the ChannelSelection of the chart data has changed.
     *
     * @param changed
     */

    public void setChannelSelectionChanged(final boolean changed)
        {
        this.boolChannelSelectionChanged = changed;
        }


    /***********************************************************************************************
     * Indicate if the DatasetDomain of the chart data has changed.
     *
     * @return boolean
     */

    public boolean isDatasetDomainChanged()
        {
        return (this.boolDatasetDomainChanged);
        }


    /***********************************************************************************************
     * Indicate if the DatasetDomain of the chart data has changed.
     *
     * @param changed
     */

    public void setDatasetDomainChanged(final boolean changed)
        {
        this.boolDatasetDomainChanged = changed;
        }


    /***********************************************************************************************
     * Indicate if this Chart has a Linear display mode.
     *
     * @return boolean
     */

    public boolean hasLinearMode()
        {
        return (this.boolChartHasLinearMode);
        }


    /***********************************************************************************************
     * Indicate if this Chart has a Linear display mode.
     *
     * @param linearmode
     */

    protected void setLinearMode(final boolean linearmode)
        {
        this.boolChartHasLinearMode = linearmode;
        }

    /***********************************************************************************************
     * Indicate if this Chart has a Logarithmic display mode.
     *
     * @return boolean
     */

    public boolean hasLogarithmicMode()
        {
        return (this.boolChartHasLogarithmicMode);
        }


    /***********************************************************************************************
     * Indicate if this Chart has a Logarithmic display mode.
     *
     * @param logmode
     */

    protected void setLogarithmicMode(final boolean logmode)
        {
        this.boolChartHasLogarithmicMode = logmode;
        }


    /***********************************************************************************************
     * Get the Domain Start Point.
     *
     * @return int
     */

    protected int getDatasetDomainStartPoint()
        {
        return (this.intDomainStartPoint);
        }


    /***********************************************************************************************
     * Set the Domain Start Point.
     *
     * @param startpoint
     */

    private void setDatasetDomainStartPoint(final int startpoint)
        {
        this.intDomainStartPoint = startpoint;
        }


    /***********************************************************************************************
     * Get the Domain End Point.
     *
     * @return int
     */

    protected int getDatasetDomainEndPoint()
        {
        return (this.intDomainEndPoint);
        }


    /***********************************************************************************************
     * Set the Domain End Point.
     *
     * @param endpoint                                              c
     */

    private void setDatasetDomainEndPoint(final int endpoint)
        {
        this.intDomainEndPoint = endpoint;
        }


    /***********************************************************************************************
     * Get the Range Crosshair value.
     * Returns 0 if no crosshair is in use.
     *
     * @return double
     */

    public double getRangeCrosshair()
        {
        return (this.dblRangeCrosshair);
        }


    /***********************************************************************************************
     * Set the Range Crosshair value.
     * Set 0 if no crosshair is in use.
     *
     * @param value
     */

    public void setRangeCrosshair(final double value)
        {
        final String SOURCE = "ChartUIComponent.setRangeCrosshair() ";

        this.dblRangeCrosshair = value;

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "Set Range Crosshair on ChartUI [value=" + value + "]");
        }


    /***********************************************************************************************
     * Get the Domain Crosshair value.
     * Returns DOMAIN_SLIDER_MINIMUM if no crosshair is in use.
     *
     * @return double
     */

    public double getDomainCrosshair()
        {
        return (this.dblDomainCrosshair);
        }


    /***********************************************************************************************
     * Set the Domain Crosshair value.
     * Set DOMAIN_SLIDER_MINIMUM if no crosshair is in use.
     *
     * @param value
     */

    public void setDomainCrosshair(final double value)
        {
        final String SOURCE = "ChartUIComponent.setDomainCrosshair() ";

        this.dblDomainCrosshair = value;

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "Set Domain Crosshair on ChartUI [value=" + value + "]");
        }


    /***********************************************************************************************
     * Get the Display Limit.
     *
     * @return int
     */

    public int getDisplayLimit()
        {
        return (this.intDisplayLimit);
        }


    /***********************************************************************************************
     * Set the Display Limit.
     *
     * @param limit
     */

    protected void setDisplayLimit(final int limit)
        {
        this.intDisplayLimit = limit;
        }


    /***********************************************************************************************
     * Indicate if this ChartUIComponent is refreshable.
     *
     * @return boolean
     */

    private synchronized boolean isRefreshable()
        {
        return (this.boolRefreshable);
        }


    /***********************************************************************************************
     * Indicate if this ChartUIComponent is refreshable.
     *
     * @param refreshable
     */

    public synchronized final void setRefreshable(final boolean refreshable)
        {
        this.boolRefreshable = refreshable;
        }


    /***********************************************************************************************
     * Indicate if this Chart is to be refreshed on a mouse click.
     *
     * @return boolean
     */

    private synchronized boolean isClickRefresh()
        {
        return (this.boolClickRefresh);
        }


    /***********************************************************************************************
     * Indicate if this Chart is to be refreshed on a mouse click.
     *
     * @param click
     */

    public synchronized final void setClickRefresh(final boolean click)
        {
        this.boolClickRefresh = click;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Task on which this Chart is based.
     *
     * @return TaskData
     */

    private synchronized TaskPlugin getTask()
        {
        return (this.pluginTask);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    protected ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Indicate if the Chart can Autorange, or must stay at FixedRange
     *
     * @return boolean
     */

    public boolean canAutorange()
        {
        return(this.boolChartCanAutorange);
        }


    /***********************************************************************************************
     * Indicate if the Chart can Autorange, or must stay at FixedRange
     *
     * @param autorange
     */

    public void setCanAutorange(final boolean autorange)
        {
        this.boolChartCanAutorange = autorange;
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    protected ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Chart.
     *
     * @return String
     */

    public synchronized final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Indicate that something changed on the ChannelSelection panel.
     *
     * @param event
     */

    public void channelSelectionChanged(final ChannelSelectionChangedEvent event)
        {
        final String SOURCE = "ChartUIComponent.channelSelectionChanged() ";

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "Channel Selection changed");

        if ((event != null)
            && (event.isCropped()))
            {
            // Use the datasets being displayed as the new RawData (Primary and Secondaries)
            if ((hasDatasetDomainControl())
                && (getDatasetDomainControlOccupant() != null))
                {
                setDatasetDomainChanged(getDatasetDomainControlOccupant().resetToExtents());
                }
            }

        // A change in Channel selection must force a refresh
        setDomainCrosshair(0.0);
        setChannelSelectionChanged(true);

        // If the Channel selection has changed, then the Chart must be visible
        refreshChart(getDAO(), true, SOURCE);
        }


    /*******************************************************************************************
     * Indicate that something changed on the DatasetDomain panel.
     *
     * @param event
     */

    public void datasetDomainChanged(final DatasetDomainChangedEvent event)
        {
        final String SOURCE = "ChartUIComponent.datasetDomainChanged() ";

        if ((event != null)
            && (event.getSource() instanceof MThumbSlider))
            {
            final MThumbSlider slider;

            slider = (MThumbSlider)event.getSource();

            // Only update the values from the DatasetDomainUIComponent if the slider has stopped moving,
            // and if there has been a change
            if ((slider != null)
                && (!slider.getValueIsAdjusting())
                && (slider.getValueAt(INDEX_LEFT) >= DOMAIN_SLIDER_MINIMUM)
                && (slider.getValueAt(INDEX_RIGHT) <= DOMAIN_SLIDER_MAXIMUM)
                && (slider.getValueAt(INDEX_LEFT) < slider.getValueAt(INDEX_RIGHT))
                && ((getDatasetDomainStartPoint() != slider.getValueAt(INDEX_LEFT))
                    || (getDatasetDomainEndPoint() != slider.getValueAt(INDEX_RIGHT))))
                {
                setDatasetDomainStartPoint(slider.getValueAt(INDEX_LEFT));
                setDatasetDomainEndPoint(slider.getValueAt(INDEX_RIGHT));

                LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                             SOURCE + "DatasetDomain changed [startpoint=" + getDatasetDomainStartPoint() + "] [endpoint=" + getDatasetDomainEndPoint() + "]");

                // A change in Dataset Domain must force a refresh
                setDatasetDomainChanged(true);

                // If the Dataset Domain has changed, then the Chart must be visible
                refreshChart(getDAO(), true, SOURCE);
                }
            //            else
            //                {
            //                LOGGER.error(SOURCE + "Incorrect slider value range!");
            //                }
            }
        else
            {
            LOGGER.error(SOURCE + "Incorrect event source!");
            }
        }


    /***********************************************************************************************
     * Update the DatasetDomain Slider Values which are stored in the ChartUIComponent.
     */

    private void updateSliderLocalValues()
        {
        final String SOURCE = "ChartUIComponent.updateSliderLocalValues() ";

        if ((hasDatasetDomainControl())
            && (getDatasetDomainControlOccupant() != null)
            && (getDatasetDomainControlOccupant().getDatasetDomainSlider() != null))
            {
            setDatasetDomainStartPoint(getDatasetDomainControlOccupant().getDatasetDomainSlider().getValueAt(INDEX_LEFT));
            setDatasetDomainEndPoint(getDatasetDomainControlOccupant().getDatasetDomainSlider().getValueAt(INDEX_RIGHT));

            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "[startpoint=" + getDatasetDomainStartPoint() + "] [endpoint=" + getDatasetDomainEndPoint() + "]");
            }
        else
            {
            LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                         SOURCE + "Can't update slider values, since there is currently no slider");
            }
        }


    /***********************************************************************************************
     * Respond to changes in the DatasetDomain Sliders or a ControlKnobUIComponent.
     *
     * @param event
     */

    public void stateChanged(final ChangeEvent event)
        {
        final String SOURCE = "ChartUIComponent.stateChanged() ";
        final Object source;
        final boolean boolDebug;

        boolDebug = LOADER_PROPERTIES.isChartDebug();

        LOGGER.debug(boolDebug, "Chart State Changed " + Logger.CONSOLE_SEPARATOR_MAJOR);

        source = event.getSource();

        if (source instanceof MThumbSlider)
            {
            final MThumbSlider slider;

            slider = (MThumbSlider) source;

            // Try to update the moving crosshair, but only if there is a Chart
            if ((slider.getModelAt(MThumbSliderAdditionalUI.INDEX_LEFT) != null)
                && (slider.getModelAt(MThumbSliderAdditionalUI.INDEX_RIGHT) != null))
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "[value.current.left=" + slider.getValueAt(MThumbSliderAdditionalUI.INDEX_LEFT)
                             + "] [value.current.right=" + slider.getValueAt(MThumbSliderAdditionalUI.INDEX_RIGHT)
                             + "] [value.previous.left=" + getDatasetDomainStartPoint()
                             + "] [value.previous.right=" + getDatasetDomainEndPoint()
                             + "] [slider.isadjusting=" + slider.getValueIsAdjusting()
                             + "] [model.isadjusting.left=" + slider.getModelAt(MThumbSliderAdditionalUI.INDEX_LEFT).getValueIsAdjusting()
                             + "] [model.isadjusting.right=" + slider.getModelAt(MThumbSliderAdditionalUI.INDEX_RIGHT).getValueIsAdjusting() + "]");

                if (slider.getModelAt(MThumbSliderAdditionalUI.INDEX_LEFT).getValueIsAdjusting())
                    {
                    ChartUIHelper.updateDomainCrosshairForDomainSlider(this,
                                                                       INDEX_LEFT,
                                                                       getDatasetDomainStartPoint(),
                                                                       getDatasetDomainEndPoint(),
                                                                       slider.getValueAt(INDEX_LEFT),
                                                                       DOMAIN_SLIDER_MINIMUM,
                                                                       DOMAIN_SLIDER_MAXIMUM,
                                                                       boolDebug);
                    }
                else  if (slider.getModelAt(MThumbSliderAdditionalUI.INDEX_RIGHT).getValueIsAdjusting())
                    {
                    ChartUIHelper.updateDomainCrosshairForDomainSlider(this,
                                                                       INDEX_RIGHT,
                                                                       getDatasetDomainStartPoint(),
                                                                       getDatasetDomainEndPoint(),
                                                                       slider.getValueAt(INDEX_RIGHT),
                                                                       DOMAIN_SLIDER_MINIMUM,
                                                                       DOMAIN_SLIDER_MAXIMUM,
                                                                       boolDebug);
                    }
                else
                    {
                    // This should never occur!
                    setDomainCrosshair(0.0);
                    }

                LOGGER.debug(boolDebug,
                             SOURCE + "Domain Slider Crosshair updated [crosshair.domain=" + getDomainCrosshair() + "]");
                }
            else
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "There is no Slider, so cannot update Domain Crosshair");
                }
            }
        else if (source instanceof ControlKnobInterface)
            {
            final ControlKnobInterface knob;

            knob = (ControlKnobInterface)source;

            LOGGER.debug(boolDebug,
                         SOURCE + "ControlKnobUIComponent [value.knob=" + knob.getValue() + "]");

            //            ChartHelper.updateDomainCrosshairForOffsetControl(this,
            //                                                              OFFSET_CONTROL_COARSE_MINIMUM,
            //                                                              OFFSET_CONTROL_COARSE_MAXIMUM,
            //                                                              knob.getValue(),
            //                                                              boolDebug);
            }
        else
            {
            LOGGER.error(SOURCE + "Unexpected Source of ChangeEvent");
            }
        }


    /***********************************************************************************************
     * Notify all listeners of DatasetChangedEvents.
     *
     * @param eventsource
     * @param seriescount
     * @param itemcount0
     */

    public final void notifyDatasetChangedEvent(final Object eventsource,
                                                final int seriescount,
                                                final int itemcount0)
        {
        List<DatasetChangedListener> listeners;
        final DatasetChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<DatasetChangedListener>(getDatasetChangedListeners());

        // Create an DatasetChangedEvent
        changeEvent = new DatasetChangedEvent(eventsource, seriescount, itemcount0);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final DatasetChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.datasetChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the DatasetChanged Listeners (mostly for testing).
     *
     * @return Vector<DatasetChangedListener>
     */

    public final Vector<DatasetChangedListener> getDatasetChangedListeners()
        {
        return (this.vecDatasetChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    public final void addDatasetChangedListener(final DatasetChangedListener listener)
        {
        if ((listener != null)
            && (getDatasetChangedListeners() != null)
            && (!getDatasetChangedListeners().contains(listener)))
            {
            getDatasetChangedListeners().addElement(listener);
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeDatasetChangedListener(final DatasetChangedListener listener)
        {
        if ((listener != null)
            && (getDatasetChangedListeners() != null))
            {
            getDatasetChangedListeners().removeElement(listener);
            }
        }
    }
