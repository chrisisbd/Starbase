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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.maps;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.coastline.Coastline;
import org.lmn.fc.common.coastline.CoastlineSegmentInterface;
import org.lmn.fc.common.datatranslators.DataExporter;
import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.common.utilities.astronomy.CoordinateConversions;
import org.lmn.fc.common.utilities.coords.GridReferenceConverter;
import org.lmn.fc.common.utilities.coords.LatitudeLongitude;
import org.lmn.fc.common.utilities.coords.OSGBGridReference;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.PointOfInterestHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MapUIComponentPlugin;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * MapUIComponent.
 */

public final class MapUIComponent extends UIComponent
                                  implements MapUIComponentPlugin
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final CountryPlugin pluginCountry;
    private FontInterface pluginFont;
    private ColourInterface pluginColourForeground;
    private ColourInterface pluginColourBackground;

    // UI
    private JToolBar toolBar;
    private JScrollPane scrollPaneMap;
    private JLabel labelMap;
    private JPanel panelMap;
    private Icon iconMap;
    private IndicatorInterface indicatorLongitude;
    private IndicatorInterface indicatorLatitude;
    private IndicatorInterface indicatorGridRef;
    private ExportableComponentInterface exportableComponent;

    // Map Scaling
    private Point2D.Double pointTopLeft;
    private Point2D.Double pointBottomRight;
    private double dblScaleX;
    private double dblScaleY;
    private boolean boolScaleValid;

    // Mouse interaction
    private Point pointStartDrag;
    private boolean boolDoDrag;

    // Points of Interest
    private JButton buttonTogglePOI;

    private final List<PointOfInterest> listPOIs;
    private final List<LineOfInterest> listLOIs;
    private boolean boolShowPOI;

    // Coastline map
    private final Coastline coastline;
    private JButton buttonExportMap;


    /***********************************************************************************************
     * Construct a MapUIComponent with scale information.
     *
     * @param hostinstrument
     * @param country
     * @param font
     * @param colourforeground
     * @param colourbackground
     */

    public MapUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                          final CountryPlugin country,
                          final FontInterface font,
                          final ColourInterface colourforeground,
                          final ColourInterface colourbackground)
        {
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.hostInstrument = hostinstrument;
        this.pluginCountry = country;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;

        this.toolBar = null;
        this.scrollPaneMap = null;
        this.labelMap = null;
        this.panelMap = null;
        this.iconMap = null;
        this.indicatorLongitude = null;
        this.indicatorLatitude = null;
        this.indicatorGridRef = null;
        this.exportableComponent = null;

        this.pointTopLeft = new Point2D.Double(0.0, 0.0);
        this.pointBottomRight = new Point2D.Double(0.0, 0.0);
        this.dblScaleX = 1.0;
        this.dblScaleY = 1.0;
        this.boolScaleValid = false;

        this.pointStartDrag = null;
        this.boolDoDrag = false;

        this.buttonTogglePOI = null;

        this.listPOIs = new ArrayList<PointOfInterest>(10);
        this.listLOIs = new ArrayList<LineOfInterest>(10);
        this.boolShowPOI = true;

        this.coastline = new Coastline();
        this.buttonExportMap = null;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "MapUIComponent.initialiseUI() ";

        super.initialiseUI();

        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Create the Map JToolBar and Indicators
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        initialiseToolbar(this,
                          getToolBar(),
                          getMapLabel(),
                          getFontData(),
                          getColourForeground(),
                          getColourBackground(),
                          false);

        removeAll();
        add(createMapUI(REGISTRY.getFramework().getMapFilename()));

        // Set the coordinates and recalculate the Scale information
        // BEWARE! The Coastline treats WEST as NEGATIVE
        // Starbase treats WEST as POSITIVE - see Meeus pg. 89
        setTopLeft(new Point2D.Double(REGISTRY.getFramework().getMapTopLeftLongitude().toDouble(),
                                      REGISTRY.getFramework().getMapTopLeftLatitude().toDouble()));
        setBottomRight(new Point2D.Double(REGISTRY.getFramework().getMapBottomRightLongitude().toDouble(),
                                          REGISTRY.getFramework().getMapBottomRightLatitude().toDouble()));

//        LOGGER.logTimedEvent(SOURCE + "[top_left_long=" + getTopLeft().getX()
//                                + "] [top_left_lat=" + getTopLeft().getY()
//                                + "] [bottom_right_long=" + getBottomRight().getX()
//                                + "] [ bottom_right_lat=" + getBottomRight().getY()
//                                + "]");

        // Create the Exportable version of the RegionalMap
        setExportableMap();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        if ((getMapLabel() != null)
            && (getMapIcon() != null))
            {
            // Recalculate the Map Scale factors, now that the component size is known
            // Valid Scale --> allows indicator updates
            setScaleValid(recalculateScale(getMapIcon().getIconWidth(), getMapIcon().getIconHeight()));
            getMapLabel().repaint();

            // Ensure we always have the latest POIs and LOIs
            collectPOIandLOI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public final void disposeUI()
        {
        stopUI();

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Create the UI on which the Map and Toolbar are displayed.
     *
     * @param filename
     *
     * @return JComponent
     *
     * @throws IndicatorException
     */

    private JComponent createMapUI(final String filename) throws IndicatorException
        {
        final String SOURCE = "MapUIComponent.createMapUI() ";
        final JPanel panelContainingMap;
        final JPanel panelUI;
        final Border compoundBorder;

        // Now create the main panel, and the Map JLabel
        panelContainingMap = new JPanel();
        panelContainingMap.setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        setMapLabel(new JLabel()
            {
            /***************************************************************************************
             * Render the Map, and the list of PointsOfInterest.
             *
             * @param graphics
             */

            public void paintComponent(final Graphics graphics)
                {
                super.paintComponent(graphics);

                final List<Metadata> listMetadata;

                // Collect the Aggregate Metadata just once per redraw
                listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                             (ObservatoryInterface) getHostInstrument().getHostAtom(),
                                                                             getHostInstrument(),
                                                                             getHostInstrument().getDAO(), null,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());
                // Draw the Points of Interest and Lines of Interest
                redrawPOIandLOI(graphics,
                                getMapLabel(),
                                getPointOfInterestList(),
                                getLineOfInterestList(),
                                listMetadata);
                }
            });

        getMapLabel().setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        getMapLabel().setOpaque(false);

        //getMapLabel().setIcon(RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(), filename));
        getMapLabel().setIcon(new ImageIcon(InstallationFolder.MAPS.getName() + System.getProperty("file.separator") + filename));

        panelContainingMap.add(getMapLabel());

        setDoDrag(false);

        // Save a reference to the Map Icon
        setMapIcon(getMapLabel().getIcon());

        // Handle Mouse clicks
        getMapLabel().addMouseListener(createMouseListener(getMapLabel()));

        // Handle Mouse drags
        getMapLabel().addMouseMotionListener(createMouseMotionListener());

        // Create the JScrollPane for the Map
        scrollPaneMap = new JScrollPane();
        scrollPaneMap.setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        scrollPaneMap.setViewportView(panelContainingMap);
        scrollPaneMap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneMap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneMap.setWheelScrollingEnabled(true);

        // Add a Border to the Map
        compoundBorder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                                            BorderFactory.createLoweredBevelBorder());
        scrollPaneMap.setBorder(compoundBorder);

        // Put all the components together
        panelUI = new JPanel();
        panelUI.setLayout(new BorderLayout());
        panelUI.setBackground(DEFAULT_COLOUR_CANVAS.getColor());
        panelUI.add(getToolBar(), BorderLayout.NORTH);
        panelUI.add(scrollPaneMap, BorderLayout.CENTER);

        return (panelUI);
        }


    /***********************************************************************************************
     * Initialise the Toolbar.
     *
     * @param uicomponent
     * @param toolbar
     * @param component (to repaint)
     */

    private void initialiseToolbar(final UIComponentPlugin uicomponent,
                                   final JToolBar toolbar,
                                   final JComponent component,
                                   final FontInterface fontdata,
                                   final ColourInterface colourforeground,
                                   final ColourInterface colourbackground,
                                   final boolean debug)
        {
        final String SOURCE = "MapUIComponent.initialiseToolbar() ";

        if ((toolbar != null)
            && (getHostInstrument() != null))
            {
            final ContextAction actionTogglePOI;
            final ContextAction actionExportCoastline;
            final JLabel labelName;
            final JLabel labelLongitude;
            final JLabel labelLatitude;
            final JLabel labelGrid;
            final JLabel labelImportPOI;
            final JLabel labelRemovePOI;

            toolbar.removeAll();

            //-------------------------------------------------------------------------------------
            // Initialise the Label

            labelName = new JLabel("Regional Map",
                                   RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                      ObservatoryInterface.FILENAME_ICON_MAP_VIEWER),
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

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);
            toolbar.add(labelName);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

            toolbar.add(Box.createHorizontalGlue());

            //--------------------------------------------------------------------------------------
            // Longitude

            indicatorLongitude = new ToolbarIndicator(DIM_LONGITUDE,
                                                      EMPTY_STRING,
                                                      TOOLTIP_LONGITUDE);
            indicatorLongitude.setValueFormat(FORMAT_LONGITUDE);
            indicatorLongitude.setValueBackground(Color.BLACK);
            labelLongitude = new JLabel(TOOLTIP_LONGITUDE);
            labelLongitude.setForeground(COLOR_TOOLBAR);
            //labelLongitude.setFont(getLabelFont().getFont());
            toolbar.add(labelLongitude);
            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            toolbar.add((Component)indicatorLongitude);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

            //--------------------------------------------------------------------------------------
            // Latitude

            indicatorLatitude = new ToolbarIndicator(DIM_LATITUDE,
                                                     EMPTY_STRING,
                                                     TOOLTIP_LATITUDE);
            indicatorLatitude.setValueFormat(FORMAT_LATITUDE);
            indicatorLatitude.setValueBackground(Color.BLACK);
            labelLatitude = new JLabel(TOOLTIP_LATITUDE);
            labelLatitude.setForeground(COLOR_TOOLBAR);
            //labelLatitude.setFont(getLabelFont().getFont());
            toolbar.add(labelLatitude);
            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            toolbar.add((Component)indicatorLatitude);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

            //--------------------------------------------------------------------------------------
            // Grid Reference if possible
            // Note that this is very country-specific

            if (HAS_GRID_REFERENCE
                && (getHostCountry().getNationalGridClassname() != null)
                && (!EMPTY_STRING.equals(getHostCountry().getNationalGridClassname().trim())))
                {
                // ToDo Instantiate code to handle the grid generically
                indicatorGridRef = new ToolbarIndicator(DIM_GRID_REFERENCE,
                                                        EMPTY_STRING,
                                                        TOOLTIP_GRID);
                indicatorGridRef.setValueFormat(FORMAT_GRID);
                indicatorGridRef.setValueBackground(Color.BLACK);
                labelGrid = new JLabel(TOOLTIP_GRID);
                labelGrid.setForeground(COLOR_TOOLBAR);
                //labelGrid.setFont(getLabelFont().getFont());
                toolbar.add(labelGrid);
                toolbar.addSeparator(DIM_LABEL_SEPARATOR);
                toolbar.add((Component)indicatorGridRef);
                toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);
                }

            //--------------------------------------------------------------------------------------
            // Add the Toolbar combo box to Import POI

            labelImportPOI = new JLabel(TOOLTIP_POI_IMPORT);
            labelImportPOI.setForeground(COLOR_TOOLBAR);
            //labelImportPOI.setFont(getLabelFont().getFont());
            toolbar.add(labelImportPOI);
            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            toolbar.add(MapUIHelper.createComboImportPOI(getHostInstrument(),
                                                         this,
                                                         getHostInstrument().getFontData(),
                                                         getHostInstrument().getColourData(),
                                                         DEFAULT_COLOUR_TAB_BACKGROUND));
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

            //--------------------------------------------------------------------------------------
            // Add the Toolbar combo box to Remove POI

            labelRemovePOI = new JLabel(TOOLTIP_POI_REMOVE);
            labelRemovePOI.setForeground(COLOR_TOOLBAR);
            //labelRemovePOI.setFont(getLabelFont().getFont());
            toolbar.add(labelRemovePOI);
            toolbar.addSeparator(DIM_LABEL_SEPARATOR);
            toolbar.add(MapUIHelper.createComboRemovePOI(getHostInstrument(),
                                                         this,
                                                         getHostInstrument().getFontData(),
                                                         getHostInstrument().getColourData(),
                                                         DEFAULT_COLOUR_TAB_BACKGROUND));
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

            //--------------------------------------------------------------------------------------
            // Create the toolbar button to toggle the POI

            actionTogglePOI = new ContextAction(ACTION_TOGGLE_POI,
                                                RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                                   FILENAME_TOOLBAR_TOGGLE_POI),
                                                TOOLTIP_TOGGLE_POI,
                                                KeyEvent.VK_P,
                                                false,
                                                true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // Toggle the PointsOfInterest
                    boolShowPOI = !boolShowPOI;

                    runUI();
                    }
                };

            // Add the Toolbar button
            buttonTogglePOI = new JButton();
            buttonTogglePOI.setBorder(BORDER_BUTTON);
            buttonTogglePOI.setAction(actionTogglePOI);
            buttonTogglePOI.setText(EMPTY_STRING);
            buttonTogglePOI.setToolTipText((String) actionTogglePOI.getValue(Action.SHORT_DESCRIPTION));
            toolbar.add(buttonTogglePOI);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_BUTTON);

            //--------------------------------------------------------------------------------------
            // A button to create a new map image from a file in workspace

            actionExportCoastline = new ContextAction(ACTION_EXPORT_MAP,
                                                      RegistryModelUtilities.getAtomIcon(getHostInstrument().getHostAtom(),
                                                                                         FILENAME_TOOLBAR_EXPORT_COASTLINE),
                                                      TOOLTIP_EXPORT_MAP,
                                                      KeyEvent.VK_X,
                                                      false,
                                                      true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    final String [] strMessage =
                        {
                        MSG_CREATE_COASTLINE_MAP
                        };
                    final int intChoice;

                    // Ask the User for confirmation
                    intChoice = JOptionPane.showOptionDialog(null,
                                                             strMessage,
                                                             "Map Creation",
                                                             JOptionPane.OK_CANCEL_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             null,
                                                             null);
                    if (intChoice == JOptionPane.YES_OPTION)
                        {
                        final Vector<Vector> log;
                        final Image image;
                        final String strExistingMap;
                        final String strMapPathname;
                        final String strMapType;
                        final boolean boolSuccess;

                        log = new Vector<Vector>(10);

                        strExistingMap = REGISTRY.getFramework().getMapFilename();
                        strMapPathname = InstallationFolder.MAPS.getName()
                                            + System.getProperty("file.separator")
                                            + strExistingMap.substring(0, strExistingMap.lastIndexOf(DOT));
                        strMapType = strExistingMap.substring(strExistingMap.lastIndexOf(DOT)+1);

                        // Remove any POI
                        boolShowPOI = false;
                        getMapLabel().repaint();

                        // Save the existing (exportable) map as a timestamped (therefore unique) backup
                        // Export the map image in the existing format, timestamped
                        // ExportableComponent refers to the MapIcon
                        // This calls exportable.paintForExport()
                        boolSuccess = DataExporter.exportComponent(getExportableComponent(),
                                                                   strMapPathname,
                                                                   true,
                                                                   strMapType,
                                                                   getExportableComponent().getWidth(),
                                                                   getExportableComponent().getHeight(),
                                                                   log,
                                                                   getHostInstrument().getObservatoryClock());

                        // Read in the new Coastline data and export to check format
                        coastline.importCoastline(FILENAME_MAP_IMPORT, log);
                        coastline.exportCoastline(FILENAME_MAP_EXPORT, false, log);

                        // Create an image for display the same size as the existing Map
                        image = createMapImage(coastline,
                                               getMapIcon().getIconWidth(),
                                               getMapIcon().getIconHeight(),
                                               getMapLabel(),
                                               boolShowPOI);
                        if (image != null)
                            {
                            // Replace the current map with a new one based on the new Coastline
                            getMapLabel().setIcon(new ImageIcon(image));

                            // Save a reference to the new Map Icon
                            setMapIcon(getMapLabel().getIcon());

                            // Set the new map coordinates and recalculate the Scale information
                            // BEWARE! The Coastline treats WEST as NEGATIVE
                            // Starbase treats WEST as POSITIVE - see Meeus pg. 89
                            setTopLeft(new Point2D.Double(-coastline.getWestExtent(), coastline.getNorthExtent()));
                            setBottomRight(new Point2D.Double(-coastline.getEastExtent(), coastline.getSouthExtent()));

                            // Recalculate the Map Scale factors, now that the component size is known
                            // Valid Scale --> allows indicator updates
                            setScaleValid(recalculateScale(getMapIcon().getIconWidth(), getMapIcon().getIconHeight()));

                            // Make the new MapIcon exportable
                            setExportableMap();

                            // Export the map image in the existing format, NOT timestamped
                            // The aspect ratio is width/height
                            exportMapImage(coastline,
                                           strMapPathname,
                                           false,
                                           strMapType,
                                           (int)(HEIGHT_MAP_EXPORT * coastline.getAspectRatio()),
                                           HEIGHT_MAP_EXPORT,
                                           getMapPanel(),
                                           boolShowPOI);
                            }
                        else
                            {
                            LOGGER.error(ERROR_CREATE_MAP);
                            }
                        }
                    }
                };

            // Add the Export button
            buttonExportMap = new JButton();
            buttonExportMap.setBorder(BORDER_BUTTON);
            buttonExportMap.setAction(actionExportCoastline);
            buttonExportMap.setText(EMPTY_STRING);
            buttonExportMap.setToolTipText((String)actionExportCoastline.getValue(Action.SHORT_DESCRIPTION));
            toolbar.add(buttonExportMap);

            NavigationUtilities.updateComponentTreeUI(toolbar);
            }
        }


    /**********************************************************************************************/
    /* Map Utilities                                                                              */
    /***********************************************************************************************
     * Create a map image from Coastline data.
     *
     * @param coast
     * @param width
     * @param height
     * @param observer
     * @param showpoi
     *
     * @return Image
     */

    private Image createMapImage(final Coastline coast,
                                 final int width,
                                 final int height,
                                 final ImageObserver observer,
                                 final boolean showpoi)
        {
        final BufferedImage buffer;
        final Graphics2D graphics2D;

        buffer = new BufferedImage(width,
                                   height,
                                   BufferedImage.TYPE_INT_RGB);

        // Create a graphics context on the buffered image
        graphics2D = buffer.createGraphics();

        // Draw on the image
        graphics2D.clearRect(0, 0, width, height);
        redrawMapFromCoastline(graphics2D,
                               coast,
                               width,
                               height,
                               observer,
                               showpoi);

        graphics2D.dispose();

        return (buffer);
        }


    /***********************************************************************************************
     * Export the map image to create the file used by the MapUIComponent.
     *
     * @param coast
     * @param filename
     * @param timestamped
     * @param type
     * @param width
     * @param height
     * @param observer
     * @param showpoi
     */

    private void exportMapImage(final Coastline coast,
                                final String filename,
                                final boolean timestamped,
                                final String type,
                                final int width,
                                final int height,
                                final ImageObserver observer,
                                final boolean showpoi)
        {
        try
            {
            final File file;
            final BufferedImage buffer;
            final Graphics2D graphics2D;

            file = new File(FileUtilities.buildFullFilename(filename, timestamped, type));
            FileUtilities.overwriteFile(file);

            LOGGER.log("MapUIComponent.exportMapImage() writing [file "
                            + file.getAbsolutePath()
                            + "] [width="
                            + width
                            + "] [height="
                            + height
                            + "]");

            buffer = new BufferedImage(width,
                                       height,
                                       BufferedImage.TYPE_INT_RGB);

            // Create a graphics context on the buffered image
            graphics2D = buffer.createGraphics();

            // Draw on the image
            graphics2D.clearRect(0, 0, width, height);
            redrawMapFromCoastline(graphics2D,
                                   coast,
                                   width,
                                   height,
                                   observer,
                                   showpoi);

            // Export the image
            ImageIO.write(buffer, type, file);
            graphics2D.dispose();
            }

        catch (IOException exception)
            {
            exception.printStackTrace();
            }
        }


    /***********************************************************************************************
     * Redraw the Map from the Coastline data, update coordinates etc.
     *
     * @param graphics
     * @param coast
     * @param width
     * @param height
     * @param observer
     * @param showpoi
     */

    private void redrawMapFromCoastline(final Graphics graphics,
                                        final Coastline coast,
                                        final int width,
                                        final int height,
                                        final ImageObserver observer,
                                        final boolean showpoi)
        {
        final String SOURCE = "MapUIComponent.redrawMapFromCoastline() ";

        graphics.setColor(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        graphics.fillRect(0, 0, width, height);

        if ((coast != null)
            && (coast.getSegments() != null))
            {
            final Iterator<CoastlineSegmentInterface> iterSegments;

            recalculateScale(width, height);
            iterSegments = coast.getSegments().iterator();
            graphics.setColor(COLOR_MAP_LAND);

            while (iterSegments.hasNext())
                {
                final CoastlineSegmentInterface segment;

                segment = iterSegments.next();

                // Make sure we have a line!
                if (segment.getPoints().size() > 1)
                    {
                    final Iterator<Point2D.Double> iterPointsOnSegment;
                    Point2D.Double pointCurrent;
                    Point2D.Double pointPrevious;
                    boolean boolFirstTime;

                    pointPrevious = null;
                    boolFirstTime = true;

                    iterPointsOnSegment = segment.getPoints().iterator();

                    // Draw the line
                    while (iterPointsOnSegment.hasNext())
                        {
                        pointCurrent = iterPointsOnSegment.next();

                        if (boolFirstTime)
                            {
                            // The first time through just record the point,
                            // don't try to draw anything...
                            pointPrevious = pointCurrent;
                            boolFirstTime = false;
                            }
                        else
                            {
                            // Map the (Long, Lat) into pixels (x,y)
                            // BEWARE! The Coastline treats WEST as NEGATIVE
                            // Starbase treats WEST as POSITIVE - see Meeus pg. 89
                            // Scales are in degrees per pixel
                            graphics.drawLine((int)((getTopLeft().getX() + pointPrevious.getX()) / getScaleX()),
                                              (int)((getTopLeft().getY() - pointPrevious.getY()) / getScaleY()),
                                              (int)((getTopLeft().getX() + pointCurrent.getX()) / getScaleX()),
                                              (int)((getTopLeft().getY() - pointCurrent.getY()) / getScaleY()));
                            pointPrevious = pointCurrent;
                            }
                        }
                    }
                }
            }

        // Draw the Points of Interest and Lines of Interest
        if (showpoi)
            {
            final List<Metadata> listMetadata;

            // Collect the Aggregate Metadata just once per redraw
            listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                         (ObservatoryInterface) getHostInstrument().getHostAtom(),
                                                                         getHostInstrument(),
                                                                         getHostInstrument().getDAO(), null,
                                                                         SOURCE,
                                                                         LOADER_PROPERTIES.isMetadataDebug());
            redrawPOIandLOI(graphics,
                            observer,
                            getPointOfInterestList(),
                            getLineOfInterestList(),
                            listMetadata);
            }
        }


    /***********************************************************************************************
     * Redraw the Points of Interest and Lines of Interest on the Map.
     *
     * @param graphics
     * @param observer
     * @param pois
     * @param lois
     * @param metadatalist
     */

    private void redrawPOIandLOI(final Graphics graphics,
                                 final ImageObserver observer,
                                 final List<PointOfInterest> pois,
                                 final List<LineOfInterest> lois,
                                 final List<Metadata> metadatalist)
        {
        final String SOURCE = "MapUIComponent.redrawPOIandLOI() ";

        if ((pois != null)
            && (!pois.isEmpty()))
            {
            final List<String> errors;

            errors = new ArrayList<String>(10);

            // Enable the POI button only if there are some POI
            buttonTogglePOI.setEnabled(true);

            // Now show the POI if it is the right time
            if (boolShowPOI)
                {
                final Iterator<PointOfInterest> iterPOI;

                iterPOI = pois.iterator();

                while (iterPOI.hasNext())
                    {
                    try
                        {
                        final PointOfInterest poi;
                        final ImageIcon iconPOI;

                        poi = iterPOI.next();

                        // All POI filenames are relative to the Framework,
                        // since they will probably include the Framework POI itself
                        iconPOI = RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(),
                                                                     PointOfInterestHelper.getPOIIconFilename(poi, metadatalist, SOURCE, errors));
                        if (iconPOI != null)
                            {
                            drawPOI(graphics, poi, iconPOI, metadatalist, observer, errors);
                            }
                        }

                    catch (DegMinSecException exception)
                        {
                        // Just skip to the next POI
                        LOGGER.error(ERROR_PARSE_POI);
                        }
                    }

                // Show any LOI too, but only if we had POIs
                if ((lois != null)
                    && (!lois.isEmpty()))
                    {
                    final Iterator<LineOfInterest> iterLOI;

                    iterLOI = lois.iterator();

                    while (iterLOI.hasNext())
                        {
                        try
                            {
                            final LineOfInterest loi;

                            loi = iterLOI.next();

                            drawLOI(graphics, loi, metadatalist, observer, errors);
                            }

                        catch (DegMinSecException exception)
                            {
                            // Just skip to the next LOI
                            LOGGER.error(ERROR_PARSE_LOI);
                            }
                        }
                    }
                }

            if (LOADER_PROPERTIES.isMetadataDebug())
                {
                LOGGER.errors(SOURCE, errors);
                }
            }
        else
            {
            // Disable the POI button if there's nothing to show
            buttonTogglePOI.setEnabled(false);
            }
        }


    /***********************************************************************************************
     * Create the Exportable version of the RegionalMap.
     */

    private void setExportableMap()
        {
        final String SOURCE = "MapUIComponent.setExportableMap() ";

        setExportableComponent(new ExportableRegionalMap()
            {
            /***********************************************************************************
             * Get the ExportableComponent width.
             *
             * @return int
             */

            public int getWidth()
                {
                return (getMapIcon().getIconWidth());
                }


            /***********************************************************************************
             * Get the ExportableComponent height.
             *
             * @return int
             */

            public int getHeight()
                {
                return (getMapIcon().getIconHeight());
                }


            /***********************************************************************************
             * Repaint the ExportableComponent ready for the export.
             *
             * @param width
             * @param height
             */

            public void paintForExport(final Graphics2D graphics,
                                       final int width,
                                       final int height)
                {
                final List<Metadata> listMetadata;

                // Collect the Aggregate Metadata just once per export
                listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                             (ObservatoryInterface) getHostInstrument().getHostAtom(),
                                                                             getHostInstrument(),
                                                                             getHostInstrument().getDAO(), null,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());
               // Ignore the incoming sizes
                paintMapForExport(graphics,
                                  getMapIcon(),
                                  listMetadata);
                }
            });
        }


    /***********************************************************************************************
     * Redraw the Map panel.
     * This is used for export only.
     *
     * @param graphics2D
     * @param mapicon
     * @param metadatalist
     */

    private void paintMapForExport(final Graphics2D graphics2D,
                                   final Icon mapicon,
                                   final List<Metadata> metadatalist)
        {
        final String SOURCE = "MapUIComponent.paintMapForExport() ";
        final List<String> errors;

        errors = new ArrayList<String>(10);

        // Start with a blank canvas in the background colour used on screen
        graphics2D.setBackground(COLOR_BACKGROUND);

        if (mapicon != null)
            {
            mapicon.paintIcon(getMapLabel(), graphics2D, 0, 0);
            }

        // Render the POI
        if ((getPointOfInterestList() != null)
            && (!getPointOfInterestList().isEmpty())
            && (boolShowPOI))
            {
            final Iterator<PointOfInterest> iterPOI;

            iterPOI = getPointOfInterestList().iterator();

            while (iterPOI.hasNext())
                {
                try
                    {
                    final PointOfInterest poi;
                    final ImageIcon iconPOI;

                    poi = iterPOI.next();

                    // All POI filenames are relative to the Framework,
                    // since they will probably include the Framework POI itself
                    iconPOI = RegistryModelUtilities.getAtomIcon(REGISTRY.getFramework(),
                                                                 PointOfInterestHelper.getPOIIconFilename(poi, metadatalist, SOURCE, errors));
                    if (iconPOI != null)
                        {
                        drawPOI(graphics2D, poi, iconPOI, metadatalist, getMapLabel(), errors);
                        }
                    }

                catch (DegMinSecException exception)
                    {
                    // Just skip to the next POI
                    LOGGER.error(ERROR_PARSE_POI);
                    }
                }
            }

        // Render the LOI
        if ((getLineOfInterestList() != null)
            && (!getLineOfInterestList().isEmpty())
            && (boolShowPOI))
            {
            final Iterator<LineOfInterest> iterLOI;

            iterLOI = getLineOfInterestList().iterator();

            while (iterLOI.hasNext())
                {
                try
                    {
                    final LineOfInterest loi;

                    loi = iterLOI.next();

                    drawLOI(graphics2D, loi, metadatalist, getMapLabel(), errors);
                    }

                catch (DegMinSecException exception)
                    {
                    // Just skip to the next LOI
                    LOGGER.error(ERROR_PARSE_LOI);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Draw a single POI on the supplied graphics context.
     *
     * @param graphics
     * @param poi
     * @param iconPOI
     * @param metadatalist
     * @param observer
     * @param errors
     */

    private void drawPOI(final Graphics graphics,
                         final PointOfInterest poi,
                         final ImageIcon iconPOI,
                         final List<Metadata> metadatalist,
                         final ImageObserver observer,
                         final List<String> errors)
        {
        final String SOURCE = "MapUIComponent.drawPOI() ";
        final DegMinSecInterface dmsLongitude;
        final DegMinSecInterface dmsLatitude;
        final String strName;

        // The Icon can only be rendered if we have a valid scale factor
        // Longitude is POSITIVE to the WEST
        // Latitude is POSITIVE to the NORTH

        // Map the (Long, Lat) into pixels (x,y)

        // Do we have a real POI or one linked to a Metadata item?
        dmsLongitude = PointOfInterestHelper.getPOILongitude(poi, metadatalist, SOURCE, errors);
        dmsLatitude = PointOfInterestHelper.getPOILatitude(poi, metadatalist, SOURCE, errors);
        strName = PointOfInterestHelper.getPOIName(poi, metadatalist, SOURCE, errors);

        if ((dmsLongitude != null)
            && (dmsLatitude != null)
            && (strName != null))
            {
            int intIconX;
            int intIconY;
            final int intTextX;
            final int intTextY;

            intIconX = (int)((getTopLeft().getX() - dmsLongitude.toDouble()) / getScaleX());
            intIconY = (int)((getTopLeft().getY() - dmsLatitude.toDouble()) / getScaleY());

            // Position the text to the right
            intTextX = intIconX + (iconPOI.getIconWidth() >> 1);
            intTextY = intIconY + 4;  //(iconPOI.getIconHeight() >> 1);

            //    * = (long,lat)
            //    icon = (x,y)
            //
            //    i--------
            //    |       |
            //    |       |
            //    |   *   |
            //    |       |
            //    |       |
            //    ---------    text
            //
            // Adjust (x,y) for the size of the Icon, to point to the top-left corner
            intIconX -= (iconPOI.getIconWidth() >> 1);
            intIconY -= (iconPOI.getIconHeight() >> 1);

            // The image is drawn with its top-left corner at i = (x, y)
            graphics.drawImage(iconPOI.getImage(),
                               intIconX,
                               intIconY,
                               observer);

            // Label the POI
            graphics.setColor(Color.blue.brighter());

            // The baseline of the leftmost character is at position (x, y)
            graphics.drawString(strName,
                                intTextX,
                                intTextY);
            }
        else
            {
            if (LOADER_PROPERTIES.isMetadataDebug())
                {
                LOGGER.errors(SOURCE, errors);
                }
            }
        }


    /***********************************************************************************************
     * Draw a single LOI on the supplied graphics context.
     *
     * @param graphics
     * @param loi
     * @param metadatalist
     * @param observer
     * @param errors
     */

    private void drawLOI(final Graphics graphics,
                         final LineOfInterest loi,
                         final List<Metadata> metadatalist,
                         final ImageObserver observer,
                         final List<String> errors)
        {
        final String SOURCE = "MapUIComponent.drawLOI() ";
        final DegMinSecInterface dmsStartLongitude;
        final DegMinSecInterface dmsStartLatitude;
        final DegMinSecInterface dmsEndLongitude;
        final DegMinSecInterface dmsEndLatitude;
        final String strLabel;
        final String strColour;

        // Longitude is POSITIVE to the WEST
        // Latitude is POSITIVE to the NORTH

        // Do we have a real POI or one linked to a Metadata item?
        dmsStartLongitude = PointOfInterestHelper.getLOIStartLongitude(loi, metadatalist, SOURCE, errors);
        dmsStartLatitude = PointOfInterestHelper.getLOIStartLatitude(loi, metadatalist, SOURCE, errors);
        dmsEndLongitude = PointOfInterestHelper.getLOIEndLongitude(loi, metadatalist, SOURCE, errors);
        dmsEndLatitude = PointOfInterestHelper.getLOIEndLatitude(loi, metadatalist, SOURCE, errors);
        strLabel = PointOfInterestHelper.getLOILabel(loi, metadatalist, SOURCE, errors);
        strColour = PointOfInterestHelper.getLOIColour(loi, metadatalist, SOURCE, errors);

        // The Label isn't used in this version
        if ((dmsStartLongitude != null)
            && (dmsStartLatitude != null)
            && (dmsEndLongitude != null)
            && (dmsEndLatitude != null))
            {
            final int intStartX;
            final int intStartY;
            final int intEndX;
            final int intEndY;

            // Map the (Long, Lat) into pixels (x,y)

            intStartX = (int)((getTopLeft().getX() - dmsStartLongitude.toDouble()) / getScaleX());
            intStartY = (int)((getTopLeft().getY() - dmsStartLatitude.toDouble()) / getScaleY());
            intEndX = (int)((getTopLeft().getX() - dmsEndLongitude.toDouble()) / getScaleX());
            intEndY = (int)((getTopLeft().getY() - dmsEndLatitude.toDouble()) / getScaleY());

            // Find the Colour information, which must exist in the XML
            graphics.setColor((new ColourDataType(strColour)).getColor());

            // Label the LOI
            // strLabel

            // Draw the Line of Interest!
            graphics.drawLine(intStartX, intStartY, intEndX, intEndY);
            }
        else
            {
            if (LOADER_PROPERTIES.isMetadataDebug())
                {
                LOGGER.errors(SOURCE, errors);
                }
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create the MouseListener for interaction with the Map.
     *
     * @param component
     *
     * @return MouseListener
     */

    private MouseListener createMouseListener(final JComponent component)
        {
        final MouseListener listener;

        listener = new MouseAdapter()
            {
            /***************************************************************************************
             * Handle MouseEntered events.
             *
             * @param mouseEvent
             */

            public void mouseEntered(final MouseEvent mouseEvent)
                {
                component.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

                try
                    {
                    clearIndicators(Color.BLACK);
                    }

                catch (IndicatorException exception)
                    {
                    LOGGER.error(ERROR_INDICATOR + exception);
                    }
                }


            /***************************************************************************************
             * Handle MouseExited events.
             *
             * @param mouseEvent
             */

            public void mouseExited(final MouseEvent mouseEvent)
                {
                component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                try
                    {
                    clearIndicators(Color.BLACK);
                    }

                catch (IndicatorException exception)
                    {
                    LOGGER.error(ERROR_INDICATOR + exception);
                    }
                }


            /***************************************************************************************
             * Handle MousePressed events.
             *
             * @param mouseEvent
             */

            public void mousePressed(final MouseEvent mouseEvent)
                {
    //                LOGGER.debug("mouse pressed at x=" + mouseEvent.getX() + "  y=" + mouseEvent.getY());
    //                getMapLabel().setCursor(new Cursor(Cursor.MOVE_CURSOR));
    //                setStartPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
    //                setDoDrag(true);

                }


            /***************************************************************************************
             * Handle MouseReleased events.
             *
             * @param mouseEvent
             */

            public void mouseReleased(final MouseEvent mouseEvent)
                {
    //                LOGGER.debug("mouse released at x=" + mouseEvent.getX() + "  y=" + mouseEvent.getY());
    //                getMapLabel().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    //                setDoDrag(false);
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
        final MouseMotionListener listener;

        listener = new MouseMotionListener()
            {
            /***************************************************************************************
             * Handle MouseDragged events.
             *
             * @param mouseEvent
             */

            public void mouseDragged(final MouseEvent mouseEvent)
                {
    //                if (getDoDrag())
                if (false)
                    {
                    int intX;
                    int intY;
                    final int intDeltaX;
                    final int intDeltaY;
                    final int intViewportX;
                    final int intViewportY;
                    final int intViewportWidth;
                    final int intViewportHeight;

                    try
                        {
                        clearIndicators(Color.BLACK);
                        }

                    catch (IndicatorException exception)
                        {
                        LOGGER.error(ERROR_INDICATOR + exception);
                        }

                    intViewportX = scrollPaneMap.getViewport().getX();
                    intViewportY = scrollPaneMap.getViewport().getY();
                    intViewportWidth = scrollPaneMap.getViewport().getWidth();
                    intViewportHeight = scrollPaneMap.getViewport().getHeight();

    //                    System.out.println("viewport width=" + scrollPaneMap.getScreenViewport().getWidth());
    //                    System.out.println("viewport height=" + scrollPaneMap.getScreenViewport().getHeight());
    //                    System.out.println("mouse dragging at x=" + mouseEvent.getX() + "  y=" + mouseEvent.getY());
    //                    System.out.println("(long, lat)  ("
    //                                       + (getTopLeft().getX() + (mouseEvent.getX() * getScaleX()))
    //                                       + ", "
    //                                       + (getTopLeft().getY() - (mouseEvent.getY() * getScaleY()))
    //                                       + ")" );

                    intDeltaX = (int)(mouseEvent.getX() - getStartPoint().getX());
    //                    System.out.println("mouse delta x=" + intDeltaX);

                    intDeltaY = (int)(mouseEvent.getY() - getStartPoint().getY());
    //                    System.out.println("mouse delta y=" + intDeltaY);


                    intX = intViewportX + intDeltaX;

                    // The new rectangle cannot extend beyond the size of the underlying map
                    if (intX < intViewportX)
                        {
                        // Moving left, so see if we have hit the left edge
                        if (intX < 0)
                            {
                            intX = 0;
                            }
                        }
                    else
                        {
                        // Moving right, so check the right edge
                        if ((intX + intViewportWidth) > getMapIcon().getIconWidth())
                            {
                            intX = getMapIcon().getIconWidth() - intViewportWidth;
                            }
                        }

                    intY = intViewportY + intDeltaY;

                    if (intY < intViewportY)
                        {
                        // Moving up, so see if we have hit the top edge
                        if (intY < 0)
                            {
                            intY = 0;
                            }
                        }
                    else
                        {
                        // Moving down, so check the bottom edge
                        if ((intY + intViewportHeight) > getMapIcon().getIconHeight())
                            {
                            intY = getMapIcon().getIconHeight() - intViewportHeight;
                            }
                        }

                    //getMapLabel().scrollRectToVisible(new Rectangle(intX,
                                                             //  intY,
                                                          //     intViewportWidth,
                                                          //     intViewportHeight));

    //                    setStartPoint(getNextPoint());
    //                    setStartPoint(new Point(intViewportX + intDeltaX,
    //                                            intViewportY + intDeltaY));

                    }
                }

            /***************************************************************************************
             * Handle MouseMoved events.
             *
             * @param event
             */

            public void mouseMoved(final MouseEvent event)
                {
                try
                    {
                    if ((isScaleValid())
                        && (indicatorLongitude != null)
                        && (indicatorLatitude != null))
                        {
                        final DegMinSecInterface dmsLongitude;
                        final DegMinSecInterface dmsLatitude;
                        final OSGBGridReference gridReference;
                        final LatitudeLongitude latLong;
                        final String strGridReference;

                        // Longitude is POSITIVE to the WEST
                        dmsLongitude = new LongitudeDataType(getTopLeft().getX() - (getScaleX()*event.getX()));
                        dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);

                        // Latitude is POSITIVE to the NORTH
                        dmsLatitude = new LatitudeDataType(getTopLeft().getY() - (getScaleY()*event.getY()));
                        dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

//                        System.out.println("toString()            long=" + dmsLongitude.toString() + " lat=" + dmsLatitude.toString());
//                        System.out.println("toString_DDD_MM_SS()  long=" + dmsLongitude.toString_DDD_MM_SS() + " lat=" + dmsLatitude.toString_DDD_MM_SS());

                        indicatorLongitude.setValue(dmsLongitude.toString_DDD_MM_SS());
                        indicatorLatitude.setValue(dmsLatitude.toString_DDD_MM_SS());

                        // Update the NationalGrid Reference if possible
                        if (HAS_GRID_REFERENCE
                            && (getHostCountry().getNationalGridClassname() != null)
                            && (!EMPTY_STRING.equals(getHostCountry().getNationalGridClassname().trim()))
                            && (indicatorGridRef != null))
                            {
                            // Convert to WEST is NEGATIVE
                            latLong = new LatitudeLongitude(dmsLatitude.toDouble(), -dmsLongitude.toDouble());
                            gridReference = GridReferenceConverter.convertLatLongToGridRef(getHostCountry(), latLong);
                            strGridReference = (int)(gridReference.getEasting()) + SPACE + SPACE + (int)(gridReference.getNorthing());

                            indicatorGridRef.setValue(strGridReference);
                            }
                        }
//                    else
//                        {
//                        System.out.println("CAN'T DRAW [scale_valid=" + (isScaleValid()
//                                             + "] [long_notnull" + (indicatorLongitude != null)
//                                             + "] [lat_notnull" + (indicatorLatitude != null)));
//                        }
                    }

                catch (DegMinSecException exception)
                    {
                    LOGGER.error(ERROR_DMS + exception);
                    }

                catch (IndicatorException exception)
                    {
                    LOGGER.error(ERROR_INDICATOR + exception);
                    }
                }
            };

        return (listener);
        }


    /***********************************************************************************************
     * Recalculate the Map Scale factors.
     * Each Scale Factor is in degrees per pixel.
     * Note that this will not work for maps crossing the Date Line!
     *
     * WEST is POSITIVE.
     *
     * @param width
     * @param height
     *
     * @return boolean
     */

    private boolean recalculateScale(final int width,
                                     final int height)
        {
        final String SOURCE = "MapUIComponent.recalculateScale() ";
        final double dblLongitudeRange;
        final double dblLatitudeRange;
        final int intXRange;
        final int intYRange;
        boolean boolValid;

        // Set some defaults
        setScaleX(1.0);
        setScaleY(1.0);
        boolValid = false;

        if ((getTopLeft() != null)
            && (getBottomRight() != null))
            {
//            System.out.println("recalculateScale() top left     (long, lat) = (" + getTopLeft().getX() + "," + + getTopLeft().getY() + ")");
//            System.out.println("recalculateScale() bottom right (long, lat) = (" + getBottomRight().getX() + "," + + getBottomRight().getY() + ")");

            // Longitude is POSITIVE to the WEST
            if ((getTopLeft().getX() > getBottomRight().getX())
                && (getTopLeft().getX() >= CoordinateConversions.LONGITUDE_RANGE_MIN)
                && (getTopLeft().getX() <= CoordinateConversions.LONGITUDE_RANGE_MAX)
                && (getBottomRight().getX() >= CoordinateConversions.LONGITUDE_RANGE_MIN)
                && (getBottomRight().getX() <= CoordinateConversions.LONGITUDE_RANGE_MAX))
                {
                dblLongitudeRange = getTopLeft().getX() - getBottomRight().getX();

                //LOGGER.debug("long range = " + dblLongitudeRange);

                // Latitude is POSITIVE to the NORTH
                if ((dblLongitudeRange > 0.0)
                    && (getTopLeft().getY() > getBottomRight().getY())
                    && (getTopLeft().getY() <= CoordinateConversions.LATITUDE_RANGE_MAX)
                    && (getTopLeft().getY() >= CoordinateConversions.LATITUDE_RANGE_MIN)
                    && (getBottomRight().getY() <= CoordinateConversions.LATITUDE_RANGE_MAX)
                    && (getBottomRight().getY() >= CoordinateConversions.LATITUDE_RANGE_MIN))
                    {
                    dblLatitudeRange = getTopLeft().getY() - getBottomRight().getY();

                    //LOGGER.debug("lat range = " + dblLatitudeRange);

                    // Get the pixel ranges
                    intXRange = width;
                    intYRange = height;
                    //LOGGER.debug("x range = " + intXRange);
                    //LOGGER.debug("y range = " + intYRange);

                    if ((dblLatitudeRange > 0.0)
                        && (intXRange > 0)
                        && (intYRange > 0))
                        {
                        // Set the new scale factors
                        setScaleX(dblLongitudeRange/intXRange);
                        setScaleY(dblLatitudeRange/intYRange);

                        //LOGGER.debug("x scale = " + getScaleX());
                        //LOGGER.debug("y scale = " + getScaleY());

                        boolValid = true;
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "One or both of (X, Y) pixels is out of range");
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Latitude extents out of range");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "Longitude extents out of range");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "One or both of TopLeft and BottomRight is NULL");
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Redisplay the Map centred on the specified (Long, Lat).
     *
     * @param longitude
     * @param latitude
     */

    public final void centreLongLat(final DegMinSecInterface longitude,
                                    final DegMinSecInterface latitude)
        {
        final int intTargetX;
        final int intTargetY;
        final int intWidth;
        final int intHeight;
        int intTopLeftX;
        int intTopLeftY;

        if ((longitude != null)
            && (latitude != null))
            {
            // Map the (Long, Lat) into pixels (x,y)
            intTargetX = (int)((longitude.toDouble() - getTopLeft().getX()) / getScaleX());
            intTargetY = (int)((getTopLeft().getY() - latitude.toDouble()) / getScaleY());

            // Find the current viewport size
            intWidth = scrollPaneMap.getViewport().getWidth();
            intHeight = scrollPaneMap.getViewport().getHeight();

            // The TopLeftX cannot be less than 0
            intTopLeftX = Math.max((intTargetX - (intWidth >> 1)), 0);

            // The TopLeftX cannot be greater than the Map IconWidth less the width of the viewport
            intTopLeftX = Math.min((getMapIcon().getIconWidth() - intWidth), intTopLeftX);

            // The TopLeftY cannot be less than 0
            intTopLeftY = Math.max((intTargetY - (intHeight >> 1)), 0);

            // The TopLeftY cannot be greater than the Map IconHeight less the height of the viewport
            intTopLeftY = Math.min((getMapIcon().getIconHeight() - intHeight), intTopLeftY);

    //        System.out.println("centre at " + longitude.toString() + ", " + latitude.toString());
    //        System.out.println("intTargetX=" + intTargetX);
    //        System.out.println("intTargetY=" + intTargetY);
    //        System.out.println("intWidth=" + intWidth);
    //        System.out.println("intHeight=" + intHeight);
    //        System.out.println("intTopLeftX=" + intTopLeftX);
    //        System.out.println("intTopLeftY=" + intTopLeftY);

//            getMapLabel().scrollRectToVisible(new Rectangle(intTopLeftX,
//                                                       intTopLeftY,
//                                                       intWidth,
//                                                       intHeight));
            repaint();
            }
        }


    /***********************************************************************************************
     * Redisplay the Map centred on the specified Pixel (x, y).
     *
     * @param x
     * @param y
     */

    public final void centrePixel(final int x,
                                  final int y)
        {
        final int intWidth;
        final int intHeight;
        int intTopLeftX;
        int intTopLeftY;

        // Find the current viewport size
        intWidth = scrollPaneMap.getViewport().getWidth();
        intHeight = scrollPaneMap.getViewport().getHeight();

        // The TopLeftX cannot be less than 0
        intTopLeftX = Math.max((x - (intWidth >> 1)), 0);

        // The TopLeftX cannot be greater than the Map IconWidth less the width of the viewport
        intTopLeftX = Math.min((getMapIcon().getIconWidth() - intWidth), intTopLeftX);

        // The TopLeftY cannot be less than 0
        intTopLeftY = Math.max((y - (intHeight >> 1)), 0);

        // The TopLeftY cannot be greater than the Map IconHeight less the height of the viewport
        intTopLeftY = Math.min((getMapIcon().getIconHeight() - intHeight), intTopLeftY);

//        getMapLabel().scrollRectToVisible(new Rectangle(intTopLeftX,
//                                                   intTopLeftY,
//                                                   intWidth,
//                                                   intHeight));
        }


    /***********************************************************************************************
     * Clear all Indicators, and set the background colour to that specified.
     *
     * @param background
     *
     * @throws IndicatorException
     */

    private void clearIndicators(final Color background) throws IndicatorException
        {
        if (indicatorLongitude != null)
            {
            indicatorLongitude.setValue(EMPTY_STRING);
            indicatorLongitude.setValueBackground(background);
            }

        if (indicatorLatitude != null)
            {
            indicatorLatitude.setValue(EMPTY_STRING);
            indicatorLatitude.setValueBackground(background);
            }

        if (indicatorGridRef != null)
            {
            indicatorGridRef.setValue(EMPTY_STRING);
            indicatorGridRef.setValueBackground(background);
            }
        }


    /***********************************************************************************************
     * Add a PointOfInterest to the Map (such as the Framework location).
     *
     * @param poi
     */

    public final void addPointOfInterest(final PointOfInterest poi)
        {
        if ((poi != null)
            && (getPointOfInterestList() != null))
            {
            // Add the PointOfInterest to the list to be rendered
            getPointOfInterestList().add(poi);
            }
        }


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Map.
     */

    public void clearPointsOfInterest()
        {
        if (getPointOfInterestList() != null)
            {
            getPointOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of PointsOfInterest.
     *
     * @return List<PointOfInterest>
     */

    public List<PointOfInterest> getPointOfInterestList()
        {
        return (this.listPOIs);
        }


    /***********************************************************************************************
     * Add a LineOfInterest to the Map.
     *
     * @param loi
     */

    public final void addLineOfInterest(final LineOfInterest loi)
        {
        if ((loi != null)
            && (getLineOfInterestList() != null))
            {
            // Add the LineOfInterest to the list to be rendered
            getLineOfInterestList().add(loi);
            }
        }


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Map.
     */

    public void clearLinesOfInterest()
        {
        if (getLineOfInterestList() != null)
            {
            getLineOfInterestList().clear();
            }
        }


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    public List<LineOfInterest> getLineOfInterestList()
        {
        return (this.listLOIs);
        }


    /***********************************************************************************************
     * Collect all PointsOfInterest and all LinesOfInterest.
     */

    public void collectPOIandLOI()
        {
        if ((getHostInstrument() != null)
            && (getHostInstrument().getContext() != null))
            {
            getPointOfInterestList().clear();
            getLineOfInterestList().clear();

            PointOfInterestHelper.collectPointsOfInterest(getPointOfInterestList(),
                                                          REGISTRY.getFramework(),
                                                          getHostInstrument().getContext().getObservatory(),
                                                          getHostInstrument(),
                                                          null,
                                                          getHostInstrument().getDAO());

            PointOfInterestHelper.collectLinesOfInterest(getLineOfInterestList(),
                                                         REGISTRY.getFramework(),
                                                         getHostInstrument().getContext().getObservatory(),
                                                         getHostInstrument(),
                                                         null,
                                                         getHostInstrument().getDAO());

            getHostInstrument().setCompositePointOfInterestList(getPointOfInterestList());
            getHostInstrument().setCompositeLineOfInterestList(getLineOfInterestList());
            }
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
     * Get the Map JLabel.
     *
     * @return JLabel
     */

    private JLabel getMapLabel()
        {
        return (this.labelMap);
        }


    /***********************************************************************************************
     * Set the Map JLabel.
     *
     * @param label
     */

    private void setMapLabel(final JLabel label)
        {
        this.labelMap = label;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the Map Icon.
     *
     * @return Icon
     */

    private Icon getMapIcon()
        {
        return (this.iconMap);
        }


    /***********************************************************************************************
     * Set the Map Icon.
     *
     * @param map
     */

    private void setMapIcon(final Icon map)
        {
        this.iconMap = map;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the Map TopLeft coordinates.
     *
     * @return Point2D.Double
     */

    private Point2D.Double getTopLeft()
        {
        return (this.pointTopLeft);
        }


    /***********************************************************************************************
     * Set the Map TopLeft coordinates.
     *
     * @param point
     */

    private void setTopLeft(final Point2D.Double point)
        {
        this.pointTopLeft = point;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the Map BottomRight coordinates.
     *
     * @return Point2D.Double
     */

    private Point2D.Double getBottomRight()
        {
        return (this.pointBottomRight);
        }


    /***********************************************************************************************
     * Set the Map BottomRight coordinates.
     *
     * @param point
     */

    private void setBottomRight(final Point2D.Double point)
        {
        this.pointBottomRight = point;
        setScaleValid(false);
        }


    /***********************************************************************************************
     * Get the X-axis Scale Factor in degrees per pixel.
     *
     * @return double
     */

    private double getScaleX()
        {
        return (this.dblScaleX);
        }


    /***********************************************************************************************
     * Set the X-axis Scale Factor in degrees per pixel.
     *
     * @param scale
     */

    private void setScaleX(final double scale)
        {
        this.dblScaleX = scale;
        }


    /***********************************************************************************************
     * Get the Y-axis Scale Factor in degrees per pixel.
     *
     * @return double
     */

    private double getScaleY()
        {
        return (this.dblScaleY);
        }


    /***********************************************************************************************
     * Set the Y-axis Scale Factor in degrees per pixel.
     *
     * @param scale
     */

    private void setScaleY(final double scale)
        {
        this.dblScaleY = scale;
        }


    /***********************************************************************************************
     * Get a flag indicating if the Scale Factors are valid.
     *
     * @return boolean
     */

    private boolean isScaleValid()
        {
        return (this.boolScaleValid);
        }


    /***********************************************************************************************
     * Set a flag indicating if the Scale Factors are valid.
     *
     * @param valid
     */

    private void setScaleValid(final boolean valid)
        {
        this.boolScaleValid = valid;
        }


    /***********************************************************************************************
     * Get a flag indicating if the mouse is being dragged.
     *
     * @return boolean
     */

    private boolean getDoDrag()
        {
        return (this.boolDoDrag);
        }


    /***********************************************************************************************
     * Set a flag indicating if the mouse is being dragged.
     *
     * @param drag
     */

    private void setDoDrag(final boolean drag)
        {
        this.boolDoDrag = drag;
        }


    /***********************************************************************************************
     * Get the mouse drag start Point.
     *
     * @return Point
     */

    private Point getStartPoint()
        {
        return pointStartDrag;
        }


    /***********************************************************************************************
     * Get the mouse drag start Point.
     *
     * @param point
     */

    private void setStartPoint(final Point point)
        {
        this.pointStartDrag = point;
        }


    /***********************************************************************************************
     * Get the host Country.
     * This is used to select the correct National Grid.
     *
     * @return CountryData
     */

    private CountryPlugin getHostCountry()
        {
        return (this.pluginCountry);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the Foreground ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourForeground()
        {
        return (this.pluginColourForeground);
        }


    /***********************************************************************************************
     * Get the Background ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourBackground()
        {
        return (this.pluginColourBackground);
        }
    }
