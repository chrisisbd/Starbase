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

//------------------------------------------------------------------------------
// Azimuth-Elevation StarMap
//------------------------------------------------------------------------------
// Revision History
//
//  05-07-02    LMN created file
//  10-07-02    LMN had GPS tracking working, and horizon profile from database
//  16-07-02    LMN corrected coordinate system and drew scales correctly
//  18-07-02    LMN had map object display working
//  22-07-02    LMN added the Cursor track and the configurable sky colour
//  30-07-02    LMN added AntennaTrackingListener and multiple Cursors
//  31-07-02    LMN added label mode
//  02-08-02    LMN made Sun position work!
//  03-08-02    LMN rewrote paintComponent() to use paintPointXY()
//  06-08-02    LMN rewrote Horizon Profiles to use HorizonProfile & Identifier
//  07-08-02    LMN made Horizon Profile work as a Vector(Az, El) & Polygon(x, y)
//  11-08-02    LMN finished RaDec and AzEl Projection Grids
//  15-08-02    LMN rewrote to simplify use of fundamental StarMapPoint structure
//  19-08-02    LMN finished rewrite of Cursors
//  27-08-02    LMN got clickable array working, with search pattern around click point
//  29-08-02    LMN wrote skeleton code for Zoom
//  22-09-02    LMN getting drawElevationScale() working with zoom
//  28-09-02    LMN finally got scales to work with zoom!
//  29-10-02    LMN pruned down to minimum, having removed all Plugin code
//  01-11-02    LMN finished Plugin drawing methods...
//  02-11-02    LMN added getAzimuthOrigin()
//  11-09-06    LMN converting for Starbase!
//
//------------------------------------------------------------------------------
// To Do
//
//      finish setAzimuthOrigin
//      tidy exception handling
//
//------------------------------------------------------------------------------
// Astronomy package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.impl;

import info.clearthought.layout.TableLayout;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.printing.PrintUtilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.trackables.AxisData;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.trackables.Trackable;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryClockChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.EpochConsumerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.TrueApparentConsumerInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.maps.ExportableStarMap;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.Indicator;
import org.lmn.fc.ui.widgets.impl.SidebarIndicator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;


/***********************************************************************************************
 * StarMapUIComponent.
 */

public final class StarMapUIComponent extends UIComponent
                                      implements StarMapUIComponentPlugin,
                                                 EpochConsumerInterface,
                                                 TrueApparentConsumerInterface,
                                                 ObservatoryClockChangedListener
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;

    // User Interface
    private final JPanel panelUIContainer;
    private JToolBar toolBar;
    private JPanel panelMap;
    private JPanel panelSidebar;

    private IndicatorInterface clockJD;
    private IndicatorInterface clockUT;
    private IndicatorInterface clockLAST;
    private IndicatorInterface indicatorAzimuth;
    private IndicatorInterface indicatorElevation;
    private IndicatorInterface indicatorRa;
    private IndicatorInterface indicatorDec;
    private IndicatorInterface indicatorGalacticLongitude;
    private IndicatorInterface indicatorGalacticLatitude;
    private IndicatorInterface indicatorLongitude;
    private IndicatorInterface indicatorLatitude;
    private IndicatorInterface indicatorHASL;

    private final JComboBox comboEpoch;
    private Epoch selectedEpoch;
    private boolean booleanTrueMode;
    private int intToolbarOriginComponentCount;
    private ExportableComponentInterface exportableComponent;
    private boolean boolInViewport;
    private Point pointLastViewportXY;

    // StarMap Plugins
    private final Vector<StarMapPlugin> vecPlugins;

    // Coordinate frame of reference
    private StarMapViewportInterface screenViewport;        // Subset of maximum extent when zooming
    private StarMapViewportInterface exportViewport;        // No zooming

    // Clickable object detection
    private final StarMapPointInterface[][] clickablePoints;  // Array of clickable StarMapPoints

    // The Trackable being tracked
    private Trackable trackAble;

    private boolean boolAzimuthOriginIsNorth;   // True if the Azimuth Origin is North

    // Scales
    private boolean boolEnableScales;              // Controls display of the scales
    private final Color colourScale;
    private final Color colourScaleBackground;

    // TableLayout row and column size definitions
    private static final double[][] size =
        {
            { // Columns
              TableLayout.FILL,
              TableLayout.PREFERRED
            },
            { // Rows
              TableLayout.PREFERRED,
              TableLayout.FILL
            }
        };

    // TableLayout constraints
    // http://www.clearthought.info/sun/products/jfc/tsc/articles/tablelayout/Cells.html
    // The horizontal justification is specified before the vertical justification
    // Multiple Cells
    // A component can also be added to a rectangular set of cells.
    // This is done by specifying the upper, left and lower, right corners of that set.
    // Components that occupy more than one cell will have a size equal to the total area
    // of all cells that component occupies.
    // There is no justification attribute for multi-celled components.
    private static final String[] constraints =
        { // Column, Row, JustificationX, JustificationY
          "0, 0, 1, 0",              // Toolbar
          "0, 1, CENTER, CENTER",    // Map
          "1, 1, CENTER, TOP"        // Sidebar
        };


    /***********************************************************************************************
     * Construct a StarMapUIComponent.
     *
     * @param hostinstrument
     */

    public StarMapUIComponent(final ObservatoryInstrumentInterface hostinstrument)
        {
        super();

        this.hostInstrument = hostinstrument;

        // UIComponent initially has a BorderLayout

        // Make the UI panel only once
        this.panelUIContainer = new JPanel();
        this.toolBar = null;
        this.panelMap = null;
        this.panelSidebar = null;

        this.clockJD = null;
        this.clockUT = null;
        this.clockLAST = null;

        this.indicatorAzimuth = null;
        this.indicatorElevation = null;
        this.indicatorRa = null;
        this.indicatorDec = null;
        this.indicatorGalacticLongitude = null;
        this.indicatorGalacticLatitude = null;
        this.indicatorLongitude = null;
        this.indicatorLatitude = null;
        this.indicatorHASL = null;
        this.comboEpoch = new JComboBox();
        this.selectedEpoch = null;
        this.booleanTrueMode = true;

        this.intToolbarOriginComponentCount = 0;
        this.exportableComponent = null;
        this.boolInViewport = false;
        this.pointLastViewportXY = new Point(0, 0);

        // Initialise the Plugins
        vecPlugins = new Vector<StarMapPlugin>(10);

        this.screenViewport = null;
        this.exportViewport = null;

        // Disable tracking to start with
        trackAble = null;

        boolAzimuthOriginIsNorth = true;

        // Scales and Labels
        boolEnableScales = false;
        colourScale = COLOR_SCALE;
        colourScaleBackground = COLOR_SCALE_BACKGROUND;

        // Create the clickable points array
        // The array is always the same size, regardless of the viewport
        clickablePoints = new StarMapPointInterface[AZIMUTH_EXTENT+1] [ELEVATION_EXTENT+1];

        // Fill the array with null data
        initialiseClickables();

        // Viewports must be available immediately after construction

        // Initialise the default coordinate frame of reference
        // Don't use setExtents() until the MapPanel size is known
        setScreenViewport(new StarMapViewport(CoordinateConversions.AZI_MIN,
                                              CoordinateConversions.AZI_MAX,
                                              CoordinateConversions.ELEV_MIN,
                                              CoordinateConversions.ELEV_MAX,
                                              CoordinateConversions.AZI_MIN,
                                              CoordinateConversions.AZI_MAX,
                                              CoordinateConversions.ELEV_MIN,
                                              CoordinateConversions.ELEV_MAX));
        // Set a default zoom factor
        getScreenViewport().setZoomFactor(0.9);

        // Another Viewport for exports
        setExportViewport(new StarMapViewport(CoordinateConversions.AZI_MIN,
                                              CoordinateConversions.AZI_MAX,
                                              CoordinateConversions.ELEV_MIN,
                                              CoordinateConversions.ELEV_MAX,
                                              CoordinateConversions.AZI_MIN,
                                              CoordinateConversions.AZI_MAX,
                                              CoordinateConversions.ELEV_MIN,
                                              CoordinateConversions.ELEV_MAX));
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     * The plugins will usually have been added before this is called.
     */

    public void initialiseUI()
        {
        final Border raisedBevel;
        final Border loweredBevel;
        final Border compoundBorder;
        final JScrollPane scrollPaneSidebar;

        super.initialiseUI();

        // Remove any previous components and start with a blank panel UIComponent
        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        getUIContainer().setLayout(new TableLayout(size));
        getUIContainer().setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        //------------------------------------------------------------------------------------------
        // Toolbar

        // Create the Map JToolBar
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);

        // There may not be any plugins at this point...
        initialiseToolbar(getToolBar(),
                          getHostInstrument(),
                          DEFAULT_FONT,
                          DEFAULT_COLOUR_TEXT,
                          DEFAULT_COLOUR_TAB_BACKGROUND,
                          false);

        // Only now do we have the initial Plugins, *and* a Toolbar, so update it
        // Later Plugin additions or removals will update the Toolbar again
        updateToolbar(getToolBar(), getPlugins());

        //------------------------------------------------------------------------------------------
        // Create the StarMap JPanel

        setMapPanel(new JPanel(new BorderLayout())
            {
            /***************************************************************************************
             * Paint the Map panel.
             *
             * @param graphics
             */

            public void paintComponent(final Graphics graphics)
                {
                // If you override this in a subclass you should not make permanent changes
                // to the passed in Graphics. For example, you should not alter the clip Rectangle
                // or modify the transform. If you need to do these operations you may find it easier
                // to create a new Graphics from the passed in Graphics and manipulate it.
                // Further, if you do not invoker super's implementation you must honor the opaque property,
                // that is if this component is opaque, you must completely fill in the background
                // in a non-opaque color.
                // If you do not honor the opaque property you will likely see visual artifacts.

                // Paint the JPanel background...
                super.paintComponent(graphics);

                // ...and draw the map
                redrawMap(graphics);
                }
            });

        // This colours the lower left corner between the scales
        getMapPanel().setBackground(COLOR_BACKGROUND);
        getMapPanel().setOpaque(true);
        // Make sure that the Map uses all available space...
        getMapPanel().setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        getMapPanel().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        //getMapPanel().setCursor(StarMapUIComponentUtilities.createCustomCursor());

        // Add a Border to the Map panel
        raisedBevel = BorderFactory.createRaisedBevelBorder();
        loweredBevel = BorderFactory.createLoweredBevelBorder();
        compoundBorder = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);
        getMapPanel().setBorder(compoundBorder);
        NavigationUtilities.updateComponentTreeUI(getMapPanel());

        // Allow interaction with the StarMap
        getMapPanel().addMouseListener(createMouseListener());
        getMapPanel().addMouseMotionListener(createMouseMotionListener());

        //------------------------------------------------------------------------------------------
        // The Exportable version of the StarMap

        setExportableComponent(new ExportableStarMap()
            {
            // We do not need to set the size of the StarMap,
            // since it will be specified by the User

            /***************************************************************************************
             * Repaint the ExportableComponent ready for the export.
             *
             * @param width
             * @param height
             */

            public void paintForExport(final Graphics2D graphics,
                                       final int width,
                                       final int height)
                {
                paintMapForExport(graphics, width, height);
                }
            });

        //------------------------------------------------------------------------------------------
        // Sidebar

        // Create the Map Sidebar and Indicators
        setSidebarPanel(new JPanel());
        getSidebarPanel().setLayout(new BoxLayoutFixed(getSidebarPanel(), BoxLayoutFixed.Y_AXIS));
        getSidebarPanel().setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        getSidebarPanel().setBorder(BorderFactory.createEmptyBorder());

        initialiseSidebar(getSidebarPanel());

        scrollPaneSidebar = new JScrollPane();
        scrollPaneSidebar.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        scrollPaneSidebar.setViewportView(getSidebarPanel());
        scrollPaneSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneSidebar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneSidebar.setWheelScrollingEnabled(true);
        scrollPaneSidebar.setBorder(BorderFactory.createEmptyBorder());

        //------------------------------------------------------------------------------------------
        // Put the visible components together

        getUIContainer().add(getToolBar(), constraints[0]);
        getUIContainer().add(getMapPanel(), constraints[1]);
        getUIContainer().add(scrollPaneSidebar, constraints[2]);

        // Consume all area on the underlying UIComponent
        add(getUIContainer(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        // Listen to the ObservatoryClock for LAST updates
        if ((getHostInstrument() != null)
            && (getHostInstrument().getObservatoryClock() != null)
            && (getHostInstrument().getObservatoryClock().getClockDAO() != null))
            {
            getHostInstrument().getObservatoryClock().getClockDAO().addObservatoryClockChangedListener(this);
            }

        // Refresh the StarMap, to update coordinates etc.
        refreshStarMap();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();

        if ((getHostInstrument() != null)
            && (getHostInstrument().getObservatoryClock() != null)
            && (getHostInstrument().getObservatoryClock().getClockDAO() != null))
            {
            getHostInstrument().getObservatoryClock().getClockDAO().removeObservatoryClockChangedListener(this);
            }

        if (clockJD != null)
            {
            clockJD.setValue("0.0");
            }

        if (clockUT != null)
            {
            clockUT.setValue(DEFAULT_TIME);
            }

        if (clockLAST != null)
            {
            clockLAST.setValue(DEFAULT_TIME);
            }

        // redraw() always re-initialises the clickables, so it's Ok to dispose of them here
        initialiseClickables();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        stopUI();

        if ((getHostInstrument() != null)
            && (getHostInstrument().getObservatoryClock() != null)
            && (getHostInstrument().getObservatoryClock().getClockDAO() != null))
            {
            getHostInstrument().getObservatoryClock().getClockDAO().removeObservatoryClockChangedListener(this);
            }

        // Remove all references to the StarMapPoints
        initialiseClickables();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getMapPanel() != null)
            {
            getMapPanel().removeAll();
            setMapPanel(null);
            }

        if (getSidebarPanel() != null)
            {
            getSidebarPanel().removeAll();
            setSidebarPanel(null);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Initialise the Toolbar, with Scale button, but without Plugin control buttons.
     * These are added later, when they are 'plugged in'.
     *
     * @param toolbar
     * @param obsinstrument
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private void initialiseToolbar(final JToolBar toolbar,
                                   final ObservatoryInstrumentInterface obsinstrument,
                                   final FontInterface fontdata,
                                   final ColourInterface colourforeground,
                                   final ColourInterface colourbackground,
                                   final boolean debug)
        {
        if (toolbar != null)
            {
            final JLabel labelName;
            final ContextAction actionScales;
            final JButton buttonScales;
            int intCount;

            //-------------------------------------------------------------------------------------
            // Initialise the Label

            labelName = new JLabel("Star Map",
                                   RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                      ObservatoryInterface.FILENAME_ICON_STAR_MAP),
                                   SwingConstants.LEFT)
                {
                private static final long serialVersionUID = 7580736117336162922L;

                // Enable Antialiasing in Java 1.5
                protected void paintComponent(final Graphics graphics)
                    {
                    final Graphics2D graphics2D = (Graphics2D) graphics;

                    // For antialiasing text
                    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    super.paintComponent(graphics2D);
                    }
                };

            labelName.setFont(fontdata.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
            labelName.setForeground(colourforeground.getColor());
            labelName.setIconTextGap(TOOLBAR_ICON_TEXT_GAP);

            toolbar.removeAll();
            intCount = 0;

            toolbar.setBackground(colourbackground.getColor());

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
            intCount++;
            toolbar.add(labelName);
            intCount++;
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
            intCount++;
            toolbar.add(Box.createHorizontalGlue());
            intCount++;

            //-------------------------------------------------------------------------------------
            // Create the toolbar button to control the plugin

            actionScales = new ContextAction("Control Scales",
                                              RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                                 ICON_TOOLBAR_SCALES),
                                              TOOLTIP_CONTROL_SCALES,
                                              KeyEvent.VK_S,
                                              false,
                                              true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    enableScales(!areScalesEnabled());
                    refreshStarMap();
                    }
                };

            buttonScales = new JButton();
            buttonScales.setBorder(BORDER_BUTTON);
            buttonScales.setAction(actionScales);
            buttonScales.setToolTipText((String) actionScales.getValue(Action.SHORT_DESCRIPTION));

            // Ensure that no text appears next to the Icon...
            buttonScales.setHideActionText(true);

            toolbar.add(buttonScales);
            intCount++;
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
            intCount++;

            // Record the position of label and Scales etc.
            setToolbarOriginComponentCount(intCount);
            NavigationUtilities.updateComponentTreeUI(toolbar);
            }
        }


    /***********************************************************************************************
     * Update the Toolbar, showing Plugin control buttons, if any.
     *
     * @param toolbar
     * @param plugins
     */

    private void updateToolbar(final JToolBar toolbar,
                               final Vector<StarMapPlugin> plugins)
        {
        if (toolbar != null)
            {
            final int intCurrentComponentCount;
            final ContextAction actionPageSetup;
            final ContextAction actionPrint;
            final JButton buttonPageSetup;
            final JButton buttonPrint;

            // Get the current number of toolbar components
            intCurrentComponentCount = toolbar.getComponentCount();

            // Has anything been added since the origin?
            if (intCurrentComponentCount > getToolbarOriginComponentCount())
                {
                for (int i = 0;
                     i < (intCurrentComponentCount - getToolbarOriginComponentCount());
                     i++)
                    {
                    // Firstly, remove all previous Plugin and Print buttons,
                    // back to the Origin
                    toolbar.remove(toolbar.getComponentCount() - 1);
                    }

                toolbar.validate();
                }

            //-------------------------------------------------------------------------------------
            // Add the Plugin control buttons, if any
            // We don't need to count these additions beyond the Origin

            if ((plugins != null)
                && (!plugins.isEmpty()))
                {
                final Iterator<StarMapPlugin> iterPlugins;

                iterPlugins = plugins.iterator();

                while (iterPlugins.hasNext())
                    {
                    final StarMapPlugin plugin;

                    plugin = iterPlugins.next();

                    if (plugin.getButton() != null)
                        {
                        toolbar.add(plugin.getButton());
                        toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
                        }
                    }
                }

            //-------------------------------------------------------------------------------------
            // Now replace the Print buttons, regardless of the Plugin count
            // Page Setup

            buttonPageSetup = new JButton();
            buttonPageSetup.setBorderPainted(false);
            buttonPageSetup.setBorder(BORDER_BUTTON);
            buttonPageSetup.setHideActionText(true);

            actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_MAP_VIEWER,
                                                RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                                ReportTablePlugin.PREFIX_PAGE_SETUP + MSG_MAP_VIEWER,
                                                KeyEvent.VK_S,
                                                false,
                                                true)
                {
                final static String SOURCE = "ContextAction:PageSetup ";
                private static final long serialVersionUID = 6802400471966299436L;


                public void actionPerformed(final ActionEvent event)
                    {
                    if (getMapPanel() != null)
                        {
                        final PrinterJob printerJob;
                        final PageFormat pageFormat;

                        printerJob = PrinterJob.getPrinterJob();
                        pageFormat = printerJob.pageDialog(getPageFormat());

                        if (pageFormat != null)
                            {
                            setPageFormat(pageFormat);
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Map Viewer UI unexpectedly NULL");
                        }
                    }
                };

            buttonPageSetup.setAction(actionPageSetup);
            buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
            buttonPageSetup.setEnabled(true);
            toolbar.add(buttonPageSetup);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

            //-------------------------------------------------------------------------------------
            // Printing

            buttonPrint = new JButton();
            buttonPrint.setBorderPainted(false);
            buttonPrint.setBorder(BORDER_BUTTON);
            buttonPrint.setHideActionText(true);

            actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + MSG_MAP_VIEWER,
                                            RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                            ReportTablePlugin.PREFIX_PRINT + MSG_MAP_VIEWER,
                                            KeyEvent.VK_P,
                                            false,
                                            true)
                {
                final static String SOURCE = "ContextAction:Print ";
                private static final long serialVersionUID = 8346968631811861938L;


                public void actionPerformed(final ActionEvent event)
                    {
                    final SwingWorker workerPrinter;

                    workerPrinter = new SwingWorker(REGISTRY.getThreadGroup(),
                                                    "SwingWorker Printer")
                        {
                        public Object construct()
                            {
                            LOGGER.debug(isDebug(), SOURCE + "SwingWorker construct()");

                            // Let the user know what happened
                            return (printDialog());
                            }

                        // Display updates occur on the Event Dispatching Thread
                        public void finished()
                            {
                            final String [] strSuccess =
                                {
                                MSG_MAP_VIEWER_PRINTED,
                                MSG_PRINT_CANCELLED
                                };

                            if ((get() != null)
                                && (get() instanceof Boolean)
                                && ((Boolean) get())
                                && (!isStopping()))
                                {
                                JOptionPane.showMessageDialog(null,
                                                              strSuccess[0],
                                                              DIALOG_PRINT,
                                                              JOptionPane.INFORMATION_MESSAGE,
                                                              RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                                }
                            else
                                {
                                JOptionPane.showMessageDialog(null,
                                                              strSuccess[1],
                                                              DIALOG_PRINT,
                                                              JOptionPane.INFORMATION_MESSAGE,
                                                              RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                                }
                            }
                        };

                    // Start the Print Thread
                    workerPrinter.start();
                    }


                /**********************************************************************************
                 * Show the Print dialog.
                 *
                 * @return boolean
                 */

                private boolean printDialog()
                    {
                    final boolean boolSuccess;

                    // Check to see that we actually have a printer...
                    if (PrinterJob.lookupPrintServices().length == 0)
                        {
                        JOptionPane.showMessageDialog(null,
                                                      ReportTablePlugin.MSG_NO_PRINTER,
                                                      ReportTablePlugin.PREFIX_PRINT + MSG_MAP_VIEWER,
                                                      JOptionPane.WARNING_MESSAGE,
                                                      RegistryModelUtilities.getCommonIcon(FILENAME_ICON_DIALOG_PRINT));
                        boolSuccess = false;
                        }
                    else
                        {
                        if (getMapPanel() != null)
                            {
                            final PageFormat pageFormat;

                            pageFormat = getPageFormat();

                            if (pageFormat != null)
                                {
                                // The StarMap Viewer is Printable
                                // ToDo Header & Footer MessageFormats
                                boolSuccess = PrintUtilities.printComponent(getMapPanel(), pageFormat);
                                }
                            else
                                {
                                boolSuccess = false;
                                }
                            }
                        else
                            {
                            boolSuccess = false;
                            }
                        }

                    return (boolSuccess);
                    }
                };

            buttonPrint.setAction(actionPrint);
            buttonPrint.setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
            buttonPrint.setEnabled(true);
            toolbar.add(buttonPrint);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

            NavigationUtilities.updateComponentTreeUI(toolbar);
            }
        }


    /***********************************************************************************************
     * Initialise the Sidebar.
     *
     * @param sidebar
     */

    private void initialiseSidebar(final JPanel sidebar)
        {
        if (sidebar != null)
            {
            final JPanel panelJD;
            final JPanel panelUT;
            final JPanel panelLAST;
            final JPanel panelAzimuth;
            final JPanel panelElevation;
            final JPanel panelRa;
            final JPanel panelDec;
            final JPanel panelGalacticLongitude;
            final JPanel panelGalacticLatitude;
            final JPanel panelLongitude;
            final JPanel panelLatitude;
            final JPanel panelHASL;
            final JPanel panelEpoch;
            final JPanel panelTrueApparent;

            final JLabel labelJD;
            final JLabel labelUT;
            final JLabel labelLAST;
            final JLabel labelAzimuth;
            final JLabel labelElevation;
            final JLabel labelRa;
            final JLabel labelDec;
            final JLabel labelGalacticLongitude;
            final JLabel labelGalacticLatitude;
            final JLabel labelLongitude;
            final JLabel labelLatitude;
            final JLabel labelHASL;
            final JLabel labelEpoch;

            sidebar.removeAll();
            sidebar.setBorder(BORDER_SIDEBAR);

            //-------------------------------------------------------------------------------------
            // Julian Day

            panelJD = new JPanel();
            panelJD.setLayout(new BoxLayoutFixed(panelJD, BoxLayoutFixed.X_AXIS));
            panelJD.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelJD.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelJD = new JLabel(TimeSystem.JD.getMnemonic());
            labelJD.setBorder(LABEL_SPACER);
            labelJD.setForeground(COLOR_TOOLBAR);

            clockJD = new Indicator(SidebarIndicator.DIM_SIDEBAR_INDICATOR,
                                    DEFAULT_TIME,
                                    EMPTY_STRING,
                                    TimeSystem.JD.getName(),
                                    INDICATOR_BORDER);
            clockJD.setAlignmentY(Component.CENTER_ALIGNMENT);
            clockJD.setValueFormat(DEFAULT_TIME);

            panelJD.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelJD.add(labelJD);
            panelJD.add(Box.createHorizontalGlue());
            panelJD.add((JComponent) clockJD);
            panelJD.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelJD);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // UT

            panelUT = new JPanel();
            panelUT.setLayout(new BoxLayoutFixed(panelUT, BoxLayoutFixed.X_AXIS));
            panelUT.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelUT.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelUT = new JLabel(TimeSystem.UT.getMnemonic());
            labelUT.setForeground(COLOR_TOOLBAR);
            labelUT.setBorder(LABEL_SPACER);

            //labelUT.setFont(getLabelFont().getFont());

            clockUT = new Indicator(SidebarIndicator.DIM_SIDEBAR_INDICATOR,
                                    DEFAULT_TIME,
                                    EMPTY_STRING,
                                    TimeSystem.UT.getName(),
                                    INDICATOR_BORDER);
            clockUT.setAlignmentY(Component.CENTER_ALIGNMENT);
            clockUT.setValueFormat(DEFAULT_TIME);

            panelUT.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelUT.add(labelUT);
            panelUT.add(Box.createHorizontalGlue());
            panelUT.add((JComponent)clockUT);
            panelUT.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelUT);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // LAST

            panelLAST = new JPanel();
            panelLAST.setLayout(new BoxLayoutFixed(panelLAST, BoxLayoutFixed.X_AXIS));
            panelLAST.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelLAST.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelLAST = new JLabel(TimeSystem.LAST.getMnemonic());
            labelLAST.setBorder(LABEL_SPACER);
            labelLAST.setForeground(COLOR_TOOLBAR);
            //labelLAST.setFont(getLabelFont().getFont());

            clockLAST = new Indicator(SidebarIndicator.DIM_SIDEBAR_INDICATOR,
                                      DEFAULT_TIME,
                                      EMPTY_STRING,
                                      TimeSystem.LAST.getName(),
                                      INDICATOR_BORDER);
            clockLAST.setAlignmentY(Component.CENTER_ALIGNMENT);
            clockLAST.setValueFormat(DEFAULT_TIME);

            panelLAST.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelLAST.add(labelLAST);
            panelLAST.add(Box.createHorizontalGlue());
            panelLAST.add((JComponent)clockLAST);
            panelLAST.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelLAST);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Azimuth

            panelAzimuth = new JPanel();
            panelAzimuth.setLayout(new BoxLayoutFixed(panelAzimuth, BoxLayoutFixed.X_AXIS));
            panelAzimuth.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelAzimuth.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelAzimuth = new JLabel("Azi");
            labelAzimuth.setBorder(LABEL_SPACER);
            labelAzimuth.setForeground(COLOR_TOOLBAR);
            //labelAzimuth.setFont(getLabelFont().getFont());

            indicatorAzimuth = new SidebarIndicator(EMPTY_STRING, TOOLTIP_AZIMUTH);
            indicatorAzimuth.setValueFormat(DecimalFormatPattern.AZIMUTH.getPattern());
            indicatorAzimuth.setValueBackground(Color.BLACK);

            panelAzimuth.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelAzimuth.add(labelAzimuth);
            panelAzimuth.add(Box.createHorizontalGlue());
            panelAzimuth.add((JComponent)indicatorAzimuth);
            panelAzimuth.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelAzimuth);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Elevation

            panelElevation = new JPanel();
            panelElevation.setLayout(new BoxLayoutFixed(panelElevation, BoxLayoutFixed.X_AXIS));
            panelElevation.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelElevation.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelElevation = new JLabel("Elev");
            labelElevation.setBorder(LABEL_SPACER);
            labelElevation.setForeground(COLOR_TOOLBAR);
            //labelElevation.setFont(getLabelFont().getFont());

            indicatorElevation = new SidebarIndicator(EMPTY_STRING, TOOLTIP_ELEVATION);
            indicatorElevation.setValueFormat(DecimalFormatPattern.ELEVATION.getPattern());
            indicatorElevation.setValueBackground(Color.BLACK);

            panelElevation.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelElevation.add(labelElevation);
            panelElevation.add(Box.createHorizontalGlue());
            panelElevation.add((JComponent)indicatorElevation);
            panelElevation.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelElevation);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // RA

            panelRa = new JPanel();
            panelRa.setLayout(new BoxLayoutFixed(panelRa, BoxLayoutFixed.X_AXIS));
            panelRa.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelRa.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelRa = new JLabel("RA");
            labelRa.setBorder(LABEL_SPACER);
            labelRa.setForeground(COLOR_TOOLBAR);
            //labelRa.setFont(getLabelFont().getFont());

            indicatorRa = new SidebarIndicator(EMPTY_STRING, TOOLTIP_RA);
            indicatorRa.setValueFormat(FORMAT_RA);
            indicatorRa.setValueBackground(Color.BLACK);

            panelRa.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelRa.add(labelRa);
            panelRa.add(Box.createHorizontalGlue());
            panelRa.add((JComponent)indicatorRa);
            panelRa.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelRa);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Dec

            panelDec = new JPanel();
            panelDec.setLayout(new BoxLayoutFixed(panelDec, BoxLayoutFixed.X_AXIS));
            panelDec.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelDec.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelDec = new JLabel("Dec");
            labelDec.setBorder(LABEL_SPACER);
            labelDec.setForeground(COLOR_TOOLBAR);
            //labelDec.setFont(getLabelFont().getFont());

            indicatorDec = new SidebarIndicator(EMPTY_STRING, TOOLTIP_DEC);
            indicatorDec.setValueFormat(DecimalFormatPattern.DECLINATION.getPattern());
            indicatorDec.setValueBackground(Color.BLACK);

            panelDec.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelDec.add(labelDec);
            panelDec.add(Box.createHorizontalGlue());
            panelDec.add((JComponent)indicatorDec);
            panelDec.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelDec);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Galactic Longitude

            panelGalacticLongitude = new JPanel();
            panelGalacticLongitude.setLayout(new BoxLayoutFixed(panelGalacticLongitude, BoxLayoutFixed.X_AXIS));
            panelGalacticLongitude.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelGalacticLongitude.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelGalacticLongitude = new JLabel("Gal l");
            labelGalacticLongitude.setBorder(LABEL_SPACER);
            labelGalacticLongitude.setForeground(COLOR_TOOLBAR);

            indicatorGalacticLongitude = new SidebarIndicator(EMPTY_STRING, TOOLTIP_GALACTIC_LONG);
            indicatorGalacticLongitude.setValueFormat("000:00:00:000W");
            indicatorGalacticLongitude.setValueBackground(Color.BLACK);

            panelGalacticLongitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelGalacticLongitude.add(labelGalacticLongitude);
            panelGalacticLongitude.add(Box.createHorizontalGlue());
            panelGalacticLongitude.add((JComponent)indicatorGalacticLongitude);
            panelGalacticLongitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelGalacticLongitude);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Galactic Latitude

            panelGalacticLatitude = new JPanel();
            panelGalacticLatitude.setLayout(new BoxLayoutFixed(panelGalacticLatitude, BoxLayoutFixed.X_AXIS));
            panelGalacticLatitude.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelGalacticLatitude.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelGalacticLatitude = new JLabel("Gal b");
            labelGalacticLatitude.setBorder(LABEL_SPACER);
            labelGalacticLatitude.setForeground(COLOR_TOOLBAR);

            indicatorGalacticLatitude = new SidebarIndicator(EMPTY_STRING, TOOLTIP_GALACTIC_LAT);
            indicatorGalacticLatitude.setValueFormat("00:00:00:00.0N");
            indicatorGalacticLatitude.setValueBackground(Color.BLACK);

            panelGalacticLatitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelGalacticLatitude.add(labelGalacticLatitude);
            panelGalacticLatitude.add(Box.createHorizontalGlue());
            panelGalacticLatitude.add((JComponent)indicatorGalacticLatitude);
            panelGalacticLatitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelGalacticLatitude);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Longitude

            panelLongitude = new JPanel();
            panelLongitude.setLayout(new BoxLayoutFixed(panelLongitude, BoxLayoutFixed.X_AXIS));
            panelLongitude.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelLongitude.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelLongitude = new JLabel("Long");
            labelLongitude.setBorder(LABEL_SPACER);
            labelLongitude.setForeground(COLOR_TOOLBAR);

            indicatorLongitude = new SidebarIndicator(EMPTY_STRING, TOOLTIP_LONGITUDE);
            indicatorLongitude.setValueFormat("000:00:00:000W");
            indicatorLongitude.setValueBackground(Color.BLACK);

            panelLongitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelLongitude.add(labelLongitude);
            panelLongitude.add(Box.createHorizontalGlue());
            panelLongitude.add((JComponent)indicatorLongitude);
            panelLongitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelLongitude);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Latitude

            panelLatitude = new JPanel();
            panelLatitude.setLayout(new BoxLayoutFixed(panelLatitude, BoxLayoutFixed.X_AXIS));
            panelLatitude.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelLatitude.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelLatitude = new JLabel("Lat");
            labelLatitude.setBorder(LABEL_SPACER);
            labelLatitude.setForeground(COLOR_TOOLBAR);

            indicatorLatitude = new SidebarIndicator(EMPTY_STRING, TOOLTIP_LATITUDE);
            indicatorLatitude.setValueFormat("00:00:00:00.0N");
            indicatorLatitude.setValueBackground(Color.BLACK);

            panelLatitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelLatitude.add(labelLatitude);
            panelLatitude.add(Box.createHorizontalGlue());
            panelLatitude.add((JComponent)indicatorLatitude);
            panelLatitude.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelLatitude);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // HASL

            panelHASL = new JPanel();
            panelHASL.setLayout(new BoxLayoutFixed(panelHASL, BoxLayoutFixed.X_AXIS));
            panelHASL.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelHASL.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelHASL = new JLabel("HASL");
            labelHASL.setBorder(LABEL_SPACER);
            labelHASL.setForeground(COLOR_TOOLBAR);

            indicatorHASL = new SidebarIndicator(EMPTY_STRING, TOOLTIP_HASL);
            indicatorHASL.setValueFormat("9999.9");
            indicatorHASL.setValueBackground(Color.BLACK);

            panelHASL.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelHASL.add(labelHASL);
            panelHASL.add(Box.createHorizontalGlue());
            panelHASL.add((JComponent)indicatorHASL);
            panelHASL.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelHASL);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // Epoch

            panelEpoch = new JPanel();
            panelEpoch.setLayout(new BoxLayoutFixed(panelEpoch, BoxLayoutFixed.X_AXIS));
            panelEpoch.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
            panelEpoch.setAlignmentX(Component.LEFT_ALIGNMENT);

            labelEpoch = new JLabel("Epoch");
            labelEpoch.setBorder(LABEL_SPACER);
            labelEpoch.setForeground(COLOR_TOOLBAR);

            StarMapUIComponentUtilities.createEpochCombo(comboEpoch,
                                                         DEFAULT_FONT,
                                                         DEFAULT_COLOUR_TEXT,
                                                         DEFAULT_COLOUR_TAB_BACKGROUND,
                                                         this);

            panelEpoch.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            panelEpoch.add(labelEpoch);
            panelEpoch.add(Box.createHorizontalGlue());
            panelEpoch.add(comboEpoch);
            panelEpoch.add(Box.createHorizontalStrut(DIM_LABEL_SEPARATOR.width));
            sidebar.add(panelEpoch);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

            //-------------------------------------------------------------------------------------
            // True or Apparent Coordinates

            panelTrueApparent = StarMapUIComponentUtilities.createTrueApparentSelector(this,
                                                                                       DEFAULT_FONT,
                                                                                       DEFAULT_COLOUR_TEXT,
                                                                                       DEFAULT_COLOUR_TAB_BACKGROUND,
                                                                                       true);

            sidebar.add(panelTrueApparent);
            sidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));
            }
        }


    /***********************************************************************************************
     * Refresh the Map JPanel.
     * By default, this first paints the background if the component is opaque.
     * The map area is defined by the current viewport.
     *
     * @return JPanel
     */

    public JPanel refreshStarMap()
        {
        repaint();

        // It may be useful to have the JPanel returned?
        return (getMapPanel());
        }


    /***********************************************************************************************
     * Redraw the Map panel, update coordinates etc.
     *
     * @param graphics
     */

    private void redrawMap(final Graphics graphics)
        {
        final String SOURCE = "StarMapUIComponent.redrawMap() ";

        // Draw all of the Plugin Objects again, on to a blank canvas
        try
            {
            final AstronomicalCalendarInterface calendarUpdate;
            final double dblLongitude;
            final double dblLatitude;
            final double dblHASL;
            final DegMinSecInterface dmsLongitudeObservatory;
            final DegMinSecInterface dmsLatitudeObservatory;

            // Find out the current size of the window, for coordinate conversions
            // Take account of the space used by the scales, if enabled
            getScreenViewport().updatePixelViewportAndRemoveScales(getMapPanel());

            calendarUpdate = EphemeridesHelper.getCalendarNow(REGISTRY.getFramework(),
                                                              getHostInstrument(),
                                                              LOADER_PROPERTIES.isMetadataDebug());
            // What is the Location for this update?
            // Find the (latest) Observatory Longitude, Latitude
            dblLongitude = EphemeridesHelper.getLongitude(REGISTRY.getFramework(),
                                                          (ObservatoryInterface)getHostInstrument().getHostAtom(),
                                                          LOADER_PROPERTIES.isMetadataDebug());
            dmsLongitudeObservatory = new LongitudeDataType(new BigDecimal(dblLongitude));
            dmsLongitudeObservatory.setDisplayFormat(DegMinSecFormat.EW);
            dmsLongitudeObservatory.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
            indicatorLongitude.setValue(dmsLongitudeObservatory.toString());

            // Make sure the Calendar is in step with the latest Metadata
            calendarUpdate.setLongitude(dblLongitude);

            dblLatitude = EphemeridesHelper.getLatitude(REGISTRY.getFramework(),
                                                        (ObservatoryInterface)getHostInstrument().getHostAtom(),
                                                        LOADER_PROPERTIES.isMetadataDebug());
            dmsLatitudeObservatory = new LatitudeDataType(new BigDecimal(dblLatitude));
            dmsLatitudeObservatory.setDisplayFormat(DegMinSecFormat.NS);
            dmsLatitudeObservatory.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
            indicatorLatitude.setValue(dmsLatitudeObservatory.toString());

            dblHASL = EphemeridesHelper.getHASL(REGISTRY.getFramework(),
                                                (ObservatoryInterface)getHostInstrument().getHostAtom(),
                                                LOADER_PROPERTIES.isMetadataDebug());
            indicatorHASL.setValue(Double.toString(dblHASL));

            // Fill the clickable object array with null data
            // The Plugins will add appropriate StarMapPoints to this array
            initialiseClickables();

            // Fill in the map window in the default sky colour, before drawing anything
            StarMapUIComponentUtilities.clearSky(graphics, getScreenViewport());

            //--------------------------------------------------------------------------------------
            // Step through the Plugins, and draw their objects...

            final Iterator<StarMapPlugin> iterPlugins;

            iterPlugins = getPlugins().iterator();

            while ((iterPlugins != null)
                && (iterPlugins.hasNext()))
                {
                final StarMapPlugin plugin;

                plugin = iterPlugins.next();

                // The Active state can be controlled using the Toolbar buttons
                if ((plugin != null)
                    && (plugin.isActive()))
                    {
                    //System.out.println("StarMapUIComponent.refreshStarMap() [plugin=" + plugin.getPluginName() + "]");

                    // Make sure we have the latest set of coordinates...
                    // ...if appropriate, since most plugins don't do anything here
                    plugin.refreshCoordinates(calendarUpdate);

                    // Get the coordinates in (x, y) form for display
                    plugin.transformToViewportXY(getScreenViewport(),
                                                 calendarUpdate,
                                                 dblLatitude,
                                                 boolAzimuthOriginIsNorth,
                                                 getClickablePoints());

                    switch (plugin.getDrawMode())
                        {
                        case POINT:
                            {
                            StarMapUIComponentUtilities.paintPluginAsPointXY(graphics,
                                                                             getScreenViewport(),
                                                                             getMapPanel().getInsets(),
                                                                             plugin);
                            break;
                            }

                        case LINE:
                            {
                            StarMapUIComponentUtilities.paintPluginAsLineXY(graphics,
                                                                            getScreenViewport(),
                                                                            getMapPanel().getInsets(),
                                                                            plugin);
                            break;
                            }

                        case POLYGON_OPEN:
                            {
                            StarMapUIComponentUtilities.paintPluginAsOpenPolygonXY(graphics,
                                                                                   getScreenViewport(),
                                                                                   getMapPanel().getInsets(),
                                                                                   plugin);
                            break;
                            }

                        case POLYGON_FILLED:
                            {
                            StarMapUIComponentUtilities.paintPluginAsFilledPolygonXY(graphics,
                                                                                     getScreenViewport(),
                                                                                     getMapPanel().getInsets(),
                                                                                     plugin);
                            break;
                            }

                        default:
                            {
                            // We've been asked to do something impossible
                            throw new FrameworkException(SOURCE + "[drawmode=" + plugin.getDrawMode() + "]");
                            }
                        }
                    }
                }

            //--------------------------------------------------------------------------------------
            // Finally, draw the scales if enabled
            // These are drawn last to erase any out-of-bounds object shapes
            // which stray into the Scales area

            if (areScalesEnabled())
                {
                //LOGGER.debugTimedEvent("StarMapUIComponent.redrawMap() Drawing Scales");
                StarMapUIComponentUtilities.drawElevationScale(graphics,
                                                               getScreenViewport(),
                                                               colourScaleBackground,
                                                               colourScale);
                StarMapUIComponentUtilities.drawAzimuthScale(graphics,
                                                             getScreenViewport(),
                                                             colourScaleBackground,
                                                             colourScale);
                }
            }

        // This is here because of AstronomicalCalendar
        catch (Exception exception)
            {
            LOGGER.error(SOURCE + "[exception=" + exception.getMessage() + "]");
            exception.printStackTrace();
            }
        }


    /***********************************************************************************************
     * Redraw the Map panel, update coordinates etc., but use the unzoomed ExportViewport.
     * This is used for export only.
     *
     * @param graphics2D
     * @param width
     * @param height
     */

    private void paintMapForExport(final Graphics2D graphics2D,
                                   final int width,
                                   final int height)
        {
        try
            {
            final AstronomicalCalendarInterface calendarUpdate;
            final double dblLatitude;

            // Start with a blank canvas in the background colour used on screen
            graphics2D.setBackground(COLOR_BACKGROUND);
            graphics2D.clearRect(0, 0, width, height);

            // Find out the requested size of the window, for coordinate conversions
            // Take account of the space used by the scales, if enabled
            getExportViewport().updatePixelViewportAndRemoveScales(width,
                                                                   height,
                                                                   new Insets(0, 0, 0, 0));

            // What is the Location for this update?
            // Find the (latest) Observatory Latitude, Longitude
            calendarUpdate = EphemeridesHelper.getCalendarNow(REGISTRY.getFramework(),
                                                              getHostInstrument(),
                                                              LOADER_PROPERTIES.isMetadataDebug());
            dblLatitude = EphemeridesHelper.getLatitude(REGISTRY.getFramework(),
                                                        (ObservatoryInterface)getHostInstrument().getHostAtom(),
                                                        LOADER_PROPERTIES.isMetadataDebug());

            // Fill the clickable object array with null data
            // The Plugins will add appropriate StarMapPoints to this array
            // This won't be used during the export!
            initialiseClickables();

            // Fill in the map window in the default sky colour, before drawing anything
            StarMapUIComponentUtilities.clearSky(graphics2D, getExportViewport());

            //--------------------------------------------------------------------------------------
            // Step through the Plugins, and draw their objects...

            final Iterator<StarMapPlugin> iterPlugins;

            iterPlugins = getPlugins().iterator();

            while ((iterPlugins != null)
                && (iterPlugins.hasNext()))
                {
                final StarMapPlugin plugin;

                plugin = iterPlugins.next();

                // The Active state can be controlled using the Toolbar buttons
                if ((plugin != null)
                    && (plugin.isActive()))
                    {
                    // Make sure we have the latest set of coordinates...
                    // ...if appropriate, since most plugins don't do anything here
                    plugin.refreshCoordinates(calendarUpdate);

                    // Get the coordinates in (x, y) form for display
                    plugin.transformToViewportXY(getExportViewport(),
                                                 calendarUpdate,
                                                 dblLatitude,
                                                 boolAzimuthOriginIsNorth,
                                                 getClickablePoints());

                    switch (plugin.getDrawMode())
                        {
                        case POINT:
                            {
                            StarMapUIComponentUtilities.paintPluginAsPointXY(graphics2D,
                                                                             getExportViewport(),
                                                                             getMapPanel().getInsets(),
                                                                             plugin);
                            break;
                            }

                        case LINE:
                            {
                            StarMapUIComponentUtilities.paintPluginAsLineXY(graphics2D,
                                                                            getExportViewport(),
                                                                            getMapPanel().getInsets(),
                                                                            plugin);
                            break;
                            }

                        case POLYGON_OPEN:
                            {
                            StarMapUIComponentUtilities.paintPluginAsOpenPolygonXY(graphics2D,
                                                                                   getExportViewport(),
                                                                                   getMapPanel().getInsets(),
                                                                                   plugin);
                            break;
                            }

                        case POLYGON_FILLED:
                            {
                            StarMapUIComponentUtilities.paintPluginAsFilledPolygonXY(graphics2D,
                                                                                     getExportViewport(),
                                                                                     getMapPanel().getInsets(),
                                                                                     plugin);
                            break;
                            }

                        default:
                            {
                            // We've been asked to do something impossible
                            throw new FrameworkException("StarMapUIComponent.paintMapForExport() [drawmode=" + plugin.getDrawMode() + "]");
                            }
                        }
                    }
                }

            //--------------------------------------------------------------------------------------
            // Finally, draw the scales if enabled
            // These are drawn last to erase any out-of-bounds object shapes
            // which stray into the Scales area

            if (areScalesEnabled())
                {
                //LOGGER.debugTimedEvent("StarMapUIComponent.redrawMap() Drawing Scales");
                StarMapUIComponentUtilities.drawElevationScale(graphics2D,
                                                               getExportViewport(),
                                                               colourScaleBackground,
                                                               colourScale);
                StarMapUIComponentUtilities.drawAzimuthScale(graphics2D,
                                                             getExportViewport(),
                                                             colourScaleBackground,
                                                             colourScale);
                }
            }

        // This is here because of AstronomicalCalendar
        catch (Exception exception)
            {
            LOGGER.error("StarMapUIComponent.paintMapForExport() [exception=" + exception.getMessage() + "]");
            exception.printStackTrace();
            }
        }


    /***********************************************************************************************
     * Initialise the clickable array.
     */

    private void initialiseClickables()
        {
        for (int i = 0; i < AZIMUTH_EXTENT+1; i++)
            {
            for (int j = 0; j < ELEVATION_EXTENT+1; j++)
                {
                getClickablePoints()[i][j] = null;
                }
            }
        }


    /***********************************************************************************************
     * Create the MouseListener for interaction with the StarMap.
     *
     * @return MouseListener
     */

    private MouseListener createMouseListener()
        {
        final StarMapUIComponentPlugin thisMapUI;
        final MouseListener listener;

        thisMapUI = this;

        listener = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent event)
                {
                final int intClickPixelsX;
                final int intClickPixelsY;
                final int intModifiers;

                // The mouse Cursor gives us (x, y) pixels relative to the underlying panel,
                // which includes the border and scale areas, so remove these first
                // X is the only value affected here by cursor coords
                if (areScalesEnabled())
                    {
                    intClickPixelsX = event.getX() - getMapPanel().getInsets().left - getElevationScaleWidth();
                    intClickPixelsY = event.getY() - getMapPanel().getInsets().top;
                    }
                else
                    {
                    intClickPixelsX = event.getX() - getMapPanel().getInsets().left;
                    intClickPixelsY = event.getY() - getMapPanel().getInsets().top;
                    }

                intModifiers = event.getModifiers();

                try
                    {
                    //System.out.println("[clickat=(" + x + ", " + y + ")] [modifiers=" + intModifiers + "]");

                    if (false)
                        {
                        if (SwingUtilities.isLeftMouseButton(event))
                            {
                            // Left_click captures a point for the HorizonProfile
                            //captureHorizonProfilePoint(new Point(x, y));
                            }

                        if (SwingUtilities.isRightMouseButton(event))
                            {
                            // Terminate the capture on a right_click
                            // and try to save the HorizonProfile to the database
                            //stopHorizonProfileCapture(true);
                            }
                        }
                    else
                        {
                        if ((SwingUtilities.isLeftMouseButton(event))
                             && (getScreenViewport() != null))
                            {
                            // Find out the current size of the window, for coordinate conversions
                            // Take account of the space used by the scales, if enabled
                            getScreenViewport().updatePixelViewportAndRemoveScales(getMapPanel());

                            if ((!event.isShiftDown())
                                && (event.isControlDown())
                                && (trackAble != null)
                                && (isDebug()))
                                {
                                final Point2D.Double pointAzElClick;

                                // Control-left_click moves the Actual Cursor in Debug Mode
                                pointAzElClick = CoordinateConversions.transformViewportXYtoAzEl(getScreenViewport(), new Point(intClickPixelsX, intClickPixelsY));
                                trackAble.getHorizontalData().setTopocentricCoordinate(AxisData.COORD_ACTUAL, pointAzElClick.x);
                                trackAble.getVerticalData().setTopocentricCoordinate(AxisData.COORD_ACTUAL, pointAzElClick.y);
                                }
                            else
                                {
                                if ((event.isShiftDown())
                                    && (!event.isControlDown()))
                                    {
                                    final Toolkit toolKit;

                                    // Shift-left_click zooms to extents
                                    //zoomViewport(ZOOM_EXTENTS);
                                    toolKit = getToolkit();
                                    toolKit.beep();
                                    }
                                else
                                    {
                                    if ((!event.isShiftDown())
                                        && (!event.isControlDown()))
                                        {
                                        if ((intClickPixelsX < getScreenViewport().getTopLeft().x) && (intClickPixelsY > getScreenViewport().getBottomRight().y))
                                            {
                                            // Left_click in corner zooms to extents
                                            // Use a dummy point near the middle of the viewport...
                                            getScreenViewport().zoomViewport(new Point(getScreenViewport().getTopLeft().x + ((getScreenViewport().getBottomRight().x - getScreenViewport().getTopLeft().x) >> 1),
                                                                                (getScreenViewport().getTopLeft().y + ((getScreenViewport().getBottomRight().y - getScreenViewport().getTopLeft().y) >> 1))),
                                                                      ZOOM_EXTENTS);
                                            getMapPanel().repaint();
                                            }
                                        else
                                            {
                                            // Left_click zooms in
                                            getScreenViewport().zoomViewport(new Point(intClickPixelsX, intClickPixelsY),
                                                                             ZOOM_IN);
                                            getMapPanel().repaint();
                                            }
                                        }
                                    else
                                        {
                                        final Toolkit toolKit;

                                        // Unrecognized combination
                                        toolKit = getToolkit();
                                        toolKit.beep();
                                        }
                                    }
                                }
                            }

                        if ((SwingUtilities.isRightMouseButton(event))
                             && (getScreenViewport() != null))
                            {
                            // Find out the current size of the window, for coordinate conversions
                            // Take account of the space used by the scales, if enabled
                            getScreenViewport().updatePixelViewportAndRemoveScales(getMapPanel());

                            if ((!event.isShiftDown())
                                && (event.isControlDown()))
                                {
                                // Control-right_click looks for an object
                                StarMapUIComponentUtilities.showObjectProperties(getHostInstrument(),
                                                                                 StarMapUIComponentUtilities.getObjectAtXY(thisMapUI,
                                                                                                                           intClickPixelsX,
                                                                                                                           intClickPixelsY),
                                                                                 LOADER_PROPERTIES.isMetadataDebug());
                                }
                            else
                                {
                                if ((event.isShiftDown())
                                    && (!event.isControlDown()))
                                    {
                                    // Shift-right_click zooms to extents
                                    getScreenViewport().zoomViewport(new Point(intClickPixelsX, intClickPixelsY),
                                                                     ZOOM_EXTENTS);
                                    getMapPanel().repaint();
                                    }
                                else
                                    {
                                    if ((!event.isShiftDown())
                                        && (!event.isControlDown()))
                                        {
                                        // Right_click zooms out
                                        getScreenViewport().zoomViewport(new Point(intClickPixelsX, intClickPixelsY),
                                                                         ZOOM_OUT);
                                        getMapPanel().repaint();
                                        }
                                    else
                                        {
                                        final Toolkit toolKit;

                                        // Unrecognized combination
                                        toolKit = getToolkit();
                                        toolKit.beep();
                                        }
                                    }
                                }
                            }
                        }
                    }

                catch (Exception exception)
                    {
                    // Exceptions cannot be thrown from here...
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "StarMapUIComponent.mousePressed() [exception=" + exception.getMessage() + "]");
                    }
                }

            /***************************************************************************************
             * Handle MouseEntered events.
             *
             * @param event
             */

            public void mouseEntered(final MouseEvent event)
                {
                try
                    {
                    setInViewport(true);
                    clearIndicators(Color.BLACK);
                    }

                catch (IndicatorException exception)
                    {
                    LOGGER.error("StarMapUIComponent.mouseEntered() IndicatorException [exception=" + exception.getMessage() + "]");
                    }
                }


            /***************************************************************************************
             * Handle MouseExited events.
             *
             * @param event
             */

            public void mouseExited(final MouseEvent event)
                {
                try
                    {
                    setInViewport(false);
                    clearIndicators(Color.BLACK);
                    }

                catch (IndicatorException exception)
                    {
                    LOGGER.error("StarMapUIComponent.mouseExited() IndicatorException [exception=" + exception.getMessage() + "]");
                    }
                }
            };

        return (listener);
        }


    /***********************************************************************************************
     * Create the MouseMotionListener.
     *
     * @return MouseMotionListener
     */

    private MouseMotionListener createMouseMotionListener()
        {
        final String SOURCE = "StarMapUIComponent.createMouseMotionListener() ";
        final MouseMotionListener listener;

        listener = new MouseMotionListener()
            {
            /***************************************************************************************
             * Handle MouseDragged events.
             *
             * @param event
             */

            public void mouseDragged(final MouseEvent event)
                {
                // Dragging not supported in this version
                }


            /***************************************************************************************
             * Handle MouseMoved events.
             *
             * @param event
             */

            public void mouseMoved(final MouseEvent event)
                {
                final String SOURCE = "StarMapUIComponent.createMouseMotionListener() --> mouseMoved() ";

                try
                    {
                    if ((indicatorAzimuth != null)
                        && (indicatorElevation != null)
                        && (indicatorRa != null)
                        && (indicatorDec != null)
                        && (indicatorGalacticLongitude != null)
                        && (indicatorGalacticLatitude != null))
                        {
                        final Point pointViewportXY;
                        final Point2D.Double pointAzElDegrees;
                        final Point2D.Double pointRaDec;

                        // The Cursor gives us (x, y) pixels relative to the underlying panel,
                        // which includes the border and scale areas, so remove these first
                        // X is the only value affected by cursor coords,
                        // since Y starts at the top
                        if (areScalesEnabled())
                            {
                            pointViewportXY = new Point(event.getX() - getMapPanel().getInsets().left - getElevationScaleWidth(),
                                                        event.getY() - getMapPanel().getInsets().top);
                            }
                        else
                            {
                            pointViewportXY = new Point(event.getX() - getMapPanel().getInsets().left,
                                                        event.getY() - getMapPanel().getInsets().top);
                            }

                        // Record the most recent position for later updates of the (RA, Dec) indicators
                        // particularly if the cursor remains stationary
                        setLastViewportXY(pointViewportXY);

                        // Find out the current size of the window, for coordinate conversions
                        // Take account of the space used by the insets and by the scales, if enabled
                        getScreenViewport().updatePixelViewportAndRemoveScales(getMapPanel());

//                        System.out.println("-----------------------------------------------------------------------------------------");
//                        System.out.println(SOURCE + "event.getX() = " + event.getX());
//                        System.out.println(SOURCE + "event.getY() = " + event.getY());
//
//                        System.out.println(SOURCE + " getMapPanel().getInsets().left = " + getMapPanel().getInsets().left);
//                        System.out.println(SOURCE + " getMapPanel().getInsets().right = " + getMapPanel().getInsets().right);
//                        System.out.println(SOURCE + " getMapPanel().getInsets().top = " + getMapPanel().getInsets().top);
//                        System.out.println(SOURCE + " getMapPanel().getInsets().bottom = " + getMapPanel().getInsets().bottom);
//                        System.out.println(SOURCE + " ELEVATION_SCALE_WIDTH = " + getElevationScaleWidth());
//
//                        // PointXY is relative to the StarMap area only
//                        System.out.println(SOURCE + " pointXY.getX() = " + pointViewportXY.getX());
//                        System.out.println(SOURCE + " pointXY.getY() = " + pointViewportXY.getY());
//
//                        System.out.println(SOURCE + " getScreenViewport().getTopLeft() = " + getScreenViewport().getTopLeft());
//                        System.out.println(SOURCE + " getScreenViewport().getBottomRight() = " + getScreenViewport().getBottomRight());
//
//                        System.out.println(SOURCE + " getScreenViewport().getAziScaleTopLeft() = " + getScreenViewport().getAziScaleTopLeft());
//                        System.out.println(SOURCE + " getScreenViewport().getAziScaleBottomRight() = " + getScreenViewport().getAziScaleBottomRight());
//                        System.out.println(SOURCE + " getScreenViewport().getElevScaleTopLeft() = " + getScreenViewport().getElevScaleTopLeft());
//                        System.out.println(SOURCE + " getScreenViewport().getElevScaleBottomRight() = " + getScreenViewport().getElevScaleBottomRight());
//
//                        System.out.println(SOURCE + " getScreenViewport().getAzimuthEast() = " + getScreenViewport().getAzimuthEast());
//                        System.out.println(SOURCE + " getScreenViewport().getAzimuthWest() = " + getScreenViewport().getAzimuthWest());
//                        System.out.println(SOURCE + " getScreenViewport().getElevationNorth() = " + getScreenViewport().getElevationNorth());
//                        System.out.println(SOURCE + " getScreenViewport().getElevationSouth() = " + getScreenViewport().getElevationSouth());
//
//                        System.out.println(SOURCE + " getScreenViewport().getHorizPixelsPerDegree() = " + getScreenViewport().getHorizPixelsPerDegree());
//                        System.out.println(SOURCE + " getScreenViewport().getVertPixelsPerDegree() = " + getScreenViewport().getVertPixelsPerDegree());

                        // ZoomFactor is the fraction to change on each zoom event, not the current magnification factor
//                        System.out.println(SOURCE + " getScreenViewport().getZoomFactor() = " + getScreenViewport().getZoomFactor());

                        pointAzElDegrees = CoordinateConversions.transformViewportXYtoAzEl(getScreenViewport(), pointViewportXY);

                        // Check for any AzEl points straying out of the StarMap into e.g. the scales
                        StarMapUIComponentUtilities.constrainAzElRange(pointAzElDegrees);

                        indicatorAzimuth.setValue(DecimalFormatPattern.AZIMUTH.format(pointAzElDegrees.getX()));
                        indicatorElevation.setValue(DecimalFormatPattern.ELEVATION.format(pointAzElDegrees.getY()));

                        // We can only find the RA if we have a clock...
                        pointRaDec = StarMapUIComponentUtilities.updateRaDecIndicators(getHostInstrument(),
                                                                                       pointAzElDegrees,
                                                                                       boolAzimuthOriginIsNorth,
                                                                                       indicatorRa,
                                                                                       indicatorDec,
                                                                                       LOADER_PROPERTIES.isMetadataDebug(),
                                                                                       SOURCE);

                        StarMapUIComponentUtilities.updateGalacticIndicators(pointRaDec,
                                                                             indicatorGalacticLongitude,
                                                                             indicatorGalacticLatitude,
                                                                             LOADER_PROPERTIES.isMetadataDebug(),
                                                                             SOURCE);
                        }
                    }

                catch (IndicatorException exception)
                    {
                    LOGGER.error(SOURCE + "IndicatorException [exception=" + exception.getMessage() + "]");
                    }
                }
            };

        return (listener);
        }


    /***********************************************************************************************
     * Clear all Indicators except the clocks, and set the background colour to that specified.
     *
     * @param background
     *
     * @throws IndicatorException
     */

    private void clearIndicators(final Color background) throws IndicatorException
        {
        if ((indicatorAzimuth != null)
            && (indicatorElevation != null)
            && (indicatorRa != null)
            && (indicatorDec != null)
            && (indicatorGalacticLongitude != null)
            && (indicatorGalacticLatitude != null))
            {
            indicatorAzimuth.setValue(EMPTY_STRING);
            indicatorAzimuth.setValueBackground(background);

            indicatorElevation.setValue(EMPTY_STRING);
            indicatorElevation.setValueBackground(background);

            indicatorRa.setValue(EMPTY_STRING);
            indicatorRa.setValueBackground(background);

            indicatorDec.setValue(EMPTY_STRING);
            indicatorDec.setValueBackground(background);

            indicatorGalacticLongitude.setValue(EMPTY_STRING);
            indicatorGalacticLongitude.setValueBackground(background);

            indicatorGalacticLatitude.setValue(EMPTY_STRING);
            indicatorGalacticLatitude.setValueBackground(background);
            }
        }


    /***********************************************************************************************
     * Indicate if the Mouse is currently in the Viewport.
     *
     * @return boolean
     */

    private boolean isInViewport()
        {
        return (this.boolInViewport);
        }


    /***********************************************************************************************
     * Indicate if the Mouse is currently in the Viewport.
     *
     * @param inviewport
     */

    private void setInViewport(final boolean inviewport)
        {
        this.boolInViewport = inviewport;
        }


    /***********************************************************************************************
     * Get the last XY pixel position of the Mouse in the Viewport.
     *
     * @return
     */

    private Point getLastViewportXY()
        {
        return (this.pointLastViewportXY);
        }


    /***********************************************************************************************
     * Set the last XY pixel position of the Mouse in the Viewport.
     *
     * @param lastxy
     */

    private void setLastViewportXY(final Point lastxy)
        {
        this.pointLastViewportXY = lastxy;
        }


    /***********************************************************************************************
     * Get the StarMapViewport for the StarMap.
     *
     * @return StarMapViewportInterface
     */

    public final StarMapViewportInterface getScreenViewport()
        {
        return (this.screenViewport);
        }


    /***********************************************************************************************
     * Set the StarMapViewport for the StarMap.
     *
     * @param viewport
     */

    public void setScreenViewport(final StarMapViewportInterface viewport)
        {
        this.screenViewport = viewport;
        }


    /***********************************************************************************************
     * Get the StarMapViewport for the StarMap.
     *
     * @return StarMapViewportInterface
     */

    private StarMapViewportInterface getExportViewport()
        {
        return (this.exportViewport);
        }


    /***********************************************************************************************
     * Set the StarMapViewport for the StarMap.
     *
     * @param viewport
     */

    private void setExportViewport(final StarMapViewportInterface viewport)
        {
        this.exportViewport = viewport;
        }


    /***********************************************************************************************
     * Set the Azimuth and Elevation range of the StarMap.
     *
     * @param azimutheast
     * @param azimuthwest
     * @param elevationsouth
     * @param elevationnorth
     */

    public final void setExtents(final double azimutheast,
                                 final double azimuthwest,
                                 final double elevationsouth,
                                 final double elevationnorth)
        {
        if (getScreenViewport() != null)
            {
            // This is the only call to Viewport Extents
            // StarMapViewport does the range checking...
            getScreenViewport().setExtents(azimutheast,
                                           azimuthwest,
                                           elevationsouth,
                                           elevationnorth);
            }

        repaint();
        }


    /***********************************************************************************************
     * Control the Scales.
     *
     * @param enable
     */

    public final void enableScales(final boolean enable)
        {
        this.boolEnableScales = enable;

        // Pass on the scale state to the Viewports
        if (getScreenViewport() != null)
            {
            getScreenViewport().enableScales(enable);
            }

        if (getExportViewport() != null)
            {
            getExportViewport().enableScales(enable);
            }

        repaint();
        }


    /***********************************************************************************************
     * Indicate if the scales are enabled.
     *
     * @return boolean
     */

    public final boolean areScalesEnabled()
        {
        return (this.boolEnableScales);
        }


    /***********************************************************************************************
     * Get the StarMap Azimuth origin.
     *
     * @return int
     */

    public final int getAzimuthOrigin()
        {
        if (boolAzimuthOriginIsNorth)
            {
            return(AZIMUTH_NORTH);
            }
        else
            {
            return(AZIMUTH_SOUTH);
            }
        }


    /***********************************************************************************************
     * Set the Azimuth Origin to NORTH or SOUTH.
     *
     * @param origin
     */

    public final void setAzimuthOrigin(final int origin)
        {
        if (origin == AZIMUTH_NORTH)
            {
            boolAzimuthOriginIsNorth = true;
            }
        else
            {
            if (origin == AZIMUTH_SOUTH)
                {
                boolAzimuthOriginIsNorth = false;
                }
            else
                {
//                throw new Exception(ExceptionLibrary.EXCEPTION_INVALID_ORIGIN);
                }
            }

        // Recalculate the map coordinate system, since the Azimuth may have changed
        repaint();
        }


    /***********************************************************************************************
     * Get the selected Epoch.
     *
     * @return Epoch
     */

    public Epoch getSelectedEpoch()
        {
        final Epoch epoch;

        if (this.selectedEpoch != null)
            {
            epoch = this.selectedEpoch;
            }
        else
            {
            epoch = Epoch.J2000;
            }

        return (epoch);
        }


    /***********************************************************************************************
     * Set the selected Epoch.
     *
     * @param epoch
     */

    public void setSelectedEpoch(final Epoch epoch)
        {
        this.selectedEpoch = epoch;
        }


    /***********************************************************************************************
     * Get the selected True or Apparent mode.
     *
     * @return boolean
     */

    public boolean isTrueMode()
        {
        return (this.booleanTrueMode);
        }


    /***********************************************************************************************
     * Set the selected True or Apparent mode.
     *
     * @param truemode
     */

    public void setTrueMode(final boolean truemode)
        {
        this.booleanTrueMode = truemode;
        }


    /***********************************************************************************************
     * Get the Trackable being tracked.
     *
     * @return Trackable
     */

    public final Trackable getTrackable()
        {
        return(trackAble);
        }


    /***********************************************************************************************
     * Set the Trackable to be tracked.
     *
     * @param trackable
     */

    public final void setTrackable(final Trackable trackable)
        {
        if (trackable != null)
            {
            this.trackAble = trackable;

            // Refresh so that Cursors are correct
            repaint();
            }
        else
            {
//            throw new Exception(ExceptionLibrary.EXCEPTION_NULL_ANTENNA);
            }
        }


    /***********************************************************************************************
     * Get all Plugins attached to this StarMap, active or not.
     *
     * @return Vector<StarMapPlugin>
     */

    public final Vector<StarMapPlugin> getPlugins()
        {
        return (this.vecPlugins);
        }


    /***********************************************************************************************
     * Add a StarMapPlugin to this StarMap.
     *
     * @param plugin
     */

    public final void addPlugin(final StarMapPlugin plugin)
        {
        if (getPlugins() != null)
            {
            getPlugins().addElement(plugin);

            // initialiseAllTabComponents() --> initialiseUI() is usually called after addPlugin()
            // so there will be no Toolbar to update, and nothing will change...
            updateToolbar(getToolBar(), getPlugins());
            }
        }


    /***********************************************************************************************
     * Remove a StarMapPlugin from this StarMap.
     *
     * @param plugin
     */

    public final void removePlugin(final StarMapPlugin plugin)
        {
        if (getPlugins() != null)
            {
            getPlugins().removeElement(plugin);

            // initialiseAllTabComponents() --> initialiseUI() is usually called after addPlugin()
            // so there will be no Toolbar to update, and nothing will change...
            updateToolbar(getToolBar(), getPlugins());
            }
        }


    /***********************************************************************************************
     * Show all the Plugins attached to this StarMap, active or not.
     */

    public final void showPluginNames()
        {
        final Iterator<StarMapPlugin> iterPlugins;

        iterPlugins = getPlugins().iterator();

        while ((iterPlugins != null)
            && (iterPlugins.hasNext()))
            {
            final StarMapPlugin plugin;

            plugin = iterPlugins.next();

            if (plugin != null)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "StarMapUIComponent.showPlugins "
                                         + " [plugin=" + plugin.getPluginName() + "]"
                                         + " [object=" + plugin.getObjectName() + "]"
                                         + " [active=" + String.valueOf(plugin.isActive()) + "]");
                }
            }
        }


    /***********************************************************************************************
     * Get the width of the Elevation scale.
     *
     * @return int
     */

    public int getElevationScaleWidth()
        {
        return (ELEVATION_SCALE_WIDTH);
        }


    /***********************************************************************************************
     * Get the height of the Azimuth scale.
     *
     * @return int
     */

    public int getAzimuthScaleHeight()
        {
        return (AZIMUTH_SCALE_HEIGHT);
        }


    /***********************************************************************************************
     * Get the ExportableComponent which may be exported.
     *
     * @return JComponent
     */

    public ExportableComponentInterface getExportableComponent()
        {
        return (this.exportableComponent);
        }


    /***********************************************************************************************
     * Set the ExportableComponent which may be exported.
     *
     * @param component
     */

    private void setExportableComponent(final ExportableComponentInterface component)
        {
        this.exportableComponent = component;
        }


   /***********************************************************************************************
     * Get the array of points which may be clicked.
     *
     * @return StarMapPointInterface[][]
     */

    public StarMapPointInterface[][] getClickablePoints()
        {
        return (this.clickablePoints);
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
     * Get the Map JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the Map JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the Map JPanel.
     *
     * @return JPanel
     */

    private JPanel getMapPanel()
        {
        return (this.panelMap);
        }


    /***********************************************************************************************
     * Set the Map Panel.
     *
     * @param panel
     */

    private void setMapPanel(final JPanel panel)
        {
        this.panelMap = panel;
        }


    /***********************************************************************************************
     * Get the Sidebar JPanel.
     *
     * @return JPanel
     */

    private JPanel getSidebarPanel()
        {
        return (this.panelSidebar);
        }


    /***********************************************************************************************
     * Set the Sidebar Panel.
     *
     * @param panel
     */

    private void setSidebarPanel(final JPanel panel)
        {
        this.panelSidebar = panel;
        }


    /***********************************************************************************************
     * Get the ToolbarOriginComponentCount.
     *
     * @return int
     */

    private int getToolbarOriginComponentCount()
        {
        return intToolbarOriginComponentCount;
        }


    /***********************************************************************************************
     * Set the ToolbarOriginComponentCount.
     *
     * @param count
     */

    private void setToolbarOriginComponentCount(final int count)
        {
        this.intToolbarOriginComponentCount = count;
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /*******************************************************************************************
     * Indicate that the ObservatoryClock has changed,
     * by updating the clocks and the (RA, Dec) indicators.
     *
     * @param event
     */

    public void clockChanged(final ObservatoryClockChangedEvent event)
        {
        final String SOURCE = "StarMapUIComponent.clockChanged() ";

        // Refresh only if visible
        if ((event != null)
            && (event.hasChanged())
            && (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this)))
            {
            if ((getHostInstrument() != null)
                && (getHostInstrument().getObservatoryClock() != null)
                && (getHostInstrument().getObservatoryClock().getAstronomicalCalendar() != null))
                {
                // Update the Clocks every time we are told that the clocks have changed
                clockJD.setValue(getHostInstrument().getObservatoryClock().getAstronomicalCalendar().toString_HH_MM_SS(TimeSystem.JD));
                clockUT.setValue(getHostInstrument().getObservatoryClock().getAstronomicalCalendar().toString_HH_MM_SS(TimeSystem.UT));
                clockLAST.setValue(getHostInstrument().getObservatoryClock().getAstronomicalCalendar().toString_HH_MM_SS(TimeSystem.LAST));

                // Update the (RA, Dec) and (l, b) indicators
                if ((isInViewport())
                    && (getLastViewportXY() != null))
                    {
                    final Point2D.Double pointAzElDegrees;
                    final Point2D.Double pointRaDec;

                    // Find out the current size of the window, for coordinate conversions
                    // Take account of the space used by the insets and by the scales, if enabled
                    getScreenViewport().updatePixelViewportAndRemoveScales(getMapPanel());

                    // Transform the last Viewport (x, y) into (Az, El), but don't change the indicators
                    pointAzElDegrees = CoordinateConversions.transformViewportXYtoAzEl(getScreenViewport(), getLastViewportXY());

                    // Check for any AzEl points straying out of the StarMap into e.g. the scales
                    StarMapUIComponentUtilities.constrainAzElRange(pointAzElDegrees);

                    // We can only find the RA if we have a clock...
                    pointRaDec = StarMapUIComponentUtilities.updateRaDecIndicators(getHostInstrument(),
                                                                                   pointAzElDegrees,
                                                                                   boolAzimuthOriginIsNorth,
                                                                                   indicatorRa,
                                                                                   indicatorDec,
                                                                                   LOADER_PROPERTIES.isMetadataDebug(),
                                                                                   SOURCE);

                    StarMapUIComponentUtilities.updateGalacticIndicators(pointRaDec,
                                                                         indicatorGalacticLongitude,
                                                                         indicatorGalacticLatitude,
                                                                         LOADER_PROPERTIES.isMetadataDebug(),
                                                                         SOURCE);
                    }

                // Is it also time to refresh the StarMap?
                // Make sure we do it no faster than once per second
                if ((getHostInstrument().getObservatoryClock().getAstronomicalCalendar().get(Calendar.SECOND) % REFRESH_SECONDS) == 0)
                    {
                    refreshStarMap();
                    }
                }
            else
                {
                // Update all Clock-related indicators
                clockJD.setValue(NO_CLOCK);
                clockUT.setValue(NO_CLOCK);
                clockLAST.setValue(NO_CLOCK);

                indicatorRa.setValue(NO_CLOCK);
                indicatorDec.setValue(NO_CLOCK);
                indicatorGalacticLongitude.setValue(NO_CLOCK);
                indicatorGalacticLatitude.setValue(NO_CLOCK);
                }
            }
        }
    }
