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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;


import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.ui.AlignedListIconCellRenderer;
import org.lmn.fc.common.utilities.ui.ListCellDatasetState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.widgets.impl.CoarseFineKnobs;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * SuperposedDataAnalyserHelper.
 */

public final class SuperposedDataAnalyserHelper implements FrameworkConstants,
                                                           FrameworkStrings,
                                                           FrameworkMetadata,
                                                           FrameworkSingletons
    {
    // String Resources
    public static final String MSG_WAITING_FOR_IMPORT = "Waiting for Import";
    private static final String TITLE_SELECT_DATASET   = "Select a Secondary Dataset";
    private static final String MSG_SWITCHED_ON        = "The Instrument must be switched on in order to select a Secondary Dataset.";
    private static final String MSG_CLICK_GREEN        = "Please click the small green button on the control panel and try again.";
    private static final String TOOLTIP_SHOW_DATASET   = "Show the selected Secondary Dataset";

    private static final String ICON_ATTACH = "toolbar-attach-dataset.png";
    private static final String ICON_DETACH = "toolbar-detach-dataset.png";
    private static final String ICON_REMOVE = "toolbar-remove-dataset.png";
    private static final String ICON_DATASET_IMPORTER = "toolbar-dataset-importer.png";
    private static final String ICON_COMPOSITE_VIEWER = "toolbar-dataset-viewer.png";
    private static final String ICON_METADATA = "toolbar-dataset-metadata.png";
    private static final String ICON_DETACH_ALL = "toolbar-detachall-dataset.png";
    private static final String ICON_COMPOSITE_EXPORTER = "toolbar-composite-exporter.png";

    public static final DecimalFormatPattern PATTERN_OFFSET = DecimalFormatPattern.SIGNED_PERCENTAGE;
    public static final boolean ALLOW_EVENTS = true;
    public static final boolean BLOCK_EVENTS = false;

    private static final int WIDTH_DATASETSELECTOR_DROPDOWN = 260;


    /***********************************************************************************************
     * Create the Composite Viewer Toolbar Buttons, for injection into the Chart.
     *
     * @param obsinstrument
     * @param sdaui
     * @param viewerui
     * @param fontdata
     * @param colourdata
     *
     * @return List<Component>
     */

    public static List<Component> createCompositeToolbarComponents(final ObservatoryInstrumentInterface obsinstrument,
                                                                   final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                                   final CompositeViewerUIComponentInterface viewerui,
                                                                   final FontInterface fontdata,
                                                                   final ColourInterface colourdata)
        {
        final List<Component> listComponents;
        final JLabel labelName;
        final ContextAction actionDetachAll;
        final ContextAction actionMetadata;
        final ContextAction actionExport;
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;

        listComponents = new ArrayList<Component>(10);

        //-------------------------------------------------------------------------------------
        // Initialise the Labels

        labelName = new JLabel(CompositeViewerUIComponentInterface.TITLE_COMPOSITE,
                               RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                  ObservatoryInterface.FILENAME_ICON_SDA),
                               SwingConstants.LEFT)
            {
            private static final long serialVersionUID = 4819361856726303204L;


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
        labelName.setForeground(colourdata.getColor());
        labelName.setIconTextGap(UIComponentPlugin.TOOLBAR_ICON_TEXT_GAP);

        viewerui.setChannelsLabel(new JLabel());
        viewerui.getChannelsLabel().setFont(fontdata.getFont());
        viewerui.getChannelsLabel().setForeground(colourdata.getColor());
        viewerui.getChannelsLabel().setText("0 Channels");
        viewerui.getChannelsLabel().setToolTipText(CompositeViewerUIComponentInterface.TOOLTIP_CHANNELS);
        viewerui.getChannelsLabel().setEnabled(false);
        // Sets the alignment of the label's contents along the X axis
        viewerui.getChannelsLabel().setHorizontalTextPosition(SwingConstants.RIGHT);

        //-------------------------------------------------------------------------------------
        // Initialise the Buttons

        viewerui.setDetachAllButton(new JButton());
        viewerui.getDetachAllButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        viewerui.getDetachAllButton().setBorderPainted(false);

        // Ensure that no text appears next to the Icon...
        viewerui.getDetachAllButton().setHideActionText(true);

        viewerui.setMetadataButton(new JButton());
        viewerui.getMetadataButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        viewerui.getMetadataButton().setBorderPainted(false);

        // Ensure that no text appears next to the Icon...
        viewerui.getMetadataButton().setHideActionText(true);

        viewerui.setExportButton(new JButton());
        viewerui.getExportButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        viewerui.getExportButton().setBorderPainted(false);

        // Ensure that no text appears next to the Icon...
        viewerui.getExportButton().setHideActionText(true);

        viewerui.setPageSetupButton(new JButton());
        viewerui.getPageSetupButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        viewerui.getPageSetupButton().setBorderPainted(false);

        // Ensure that no text appears next to the Icon...
        viewerui.getPageSetupButton().setHideActionText(true);

        viewerui.setPrintButton(new JButton());
        viewerui.getPrintButton().setBorder(UIComponentPlugin.BORDER_BUTTON);
        viewerui.getPrintButton().setBorderPainted(false);

        // Ensure that no text appears next to the Icon...
        viewerui.getPrintButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // Detach All

        actionDetachAll = new ContextAction("Detach All",
                                            RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                               ICON_DETACH_ALL),
                                            CompositeViewerUIComponentInterface.TOOLTIP_DETACH_ALL,
                                            KeyEvent.VK_X,
                                            false,
                                            true)
            {
            static final String SOURCE = "ContextAction:DetachAll ";
            static final long serialVersionUID = 3486059811646162340L;


            public void actionPerformed(final ActionEvent event)
                {
                final boolean boolSuccess;

                boolSuccess = DetachDataset.doDetachAllDatasets(sdaui,
                                                                sdaui.getDatasetViewer(),
                                                                this);
                if (boolSuccess)
                    {
                    // Redraw the Composite completely, because everything changed
                    DatasetManagerHelper.rebuildComposite(sdaui.getDatasetManager(),
                                                          sdaui,
                                                          true, true, true, true, true, true, true,
                                                          event,
                                                          LOADER_PROPERTIES.isChartDebug());
                    }
                }
            };

        viewerui.getDetachAllButton().setAction(actionDetachAll);
        viewerui.getDetachAllButton().setToolTipText((String) actionDetachAll.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getDetachAllButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Metadata

        actionMetadata = new ContextAction("Metadata",
                                           RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                              ICON_METADATA),
                                           CompositeViewerUIComponentInterface.TOOLTIP_METADATA,
                                           KeyEvent.VK_M,
                                           false,
                                           true)
            {
            static final long serialVersionUID = -307436773020050527L;
            static final String SOURCE = "ContextAction:Metadata ";


            public void actionPerformed(final ActionEvent event)
                {
                switch (sdaui.getCompositeViewerMode())
                    {
                    case COMPOSITE_VIEWER_CHART:
                    {
                    // Clicking this button must switch the displayed panel to the CompositeMetadata
                    switchCompositeViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_METADATA);

                    break;
                    }

                    case COMPOSITE_VIEWER_METADATA:
                    {
                    // This calls refreshChart()
                    switchCompositeViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART);
                    break;
                    }

                    default:
                    {
                    // It should not be possible to click this button in CompositeExporter mode
                    LOGGER.error(SOURCE + "Invalid Display Mode [mode=" + sdaui.getCompositeViewerMode().getName() + "]");
                    }
                    }
                }
            };

        viewerui.getMetadataButton().setAction(actionMetadata);
        viewerui.getMetadataButton().setToolTipText((String) actionMetadata.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getMetadataButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Export

        actionExport = new ContextAction("Export",
                                         RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                            ICON_COMPOSITE_EXPORTER),
                                         CompositeViewerUIComponentInterface.TOOLTIP_COMPOSITE_EXPORTER,
                                         KeyEvent.VK_E,
                                         false,
                                         true)
            {
            static final String SOURCE = "ContextAction:Export ";
            static final long serialVersionUID = -4660069305829133388L;


            public void actionPerformed(final ActionEvent event)
                {
                // Clicking this button must switch the displayed panel to the CompositeExporter,
                // which we assumed is in the STOPPED state
                switchCompositeViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.COMPOSITE_EXPORTER);
                }
            };

        viewerui.getExportButton().setAction(actionExport);
        viewerui.getExportButton().setToolTipText((String) actionExport.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getExportButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Page Setup

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + CompositeViewerUIComponentInterface.MSG_PRINT_COMPOSITE,
                                            RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + CompositeViewerUIComponentInterface.MSG_PRINT_COMPOSITE,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            static final long serialVersionUID = 6802400471966299436L;
            static final String SOURCE = "ContextAction:PageSetup ";

            public void actionPerformed(final ActionEvent event)
                {
                if (viewerui.getChartViewer().getChartPanel() != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(viewerui.getChartViewer().getPageFormat());

                    if (pageFormat != null)
                        {
                        viewerui.getChartViewer().setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Chart Panel unexpectedly NULL");
                    }
                }
            };

        viewerui.getPageSetupButton().setAction(actionPageSetup);
        viewerui.getPageSetupButton().setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getPageSetupButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Print

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + CompositeViewerUIComponentInterface.MSG_PRINT_COMPOSITE,
                                        RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + CompositeViewerUIComponentInterface.MSG_PRINT_COMPOSITE,
                                        KeyEvent.VK_P,
                                        false,
                                        false)
            {
            static final long serialVersionUID = 8699648522703111782L;
            static final String SOURCE = "ContextAction:Print ";


            public void actionPerformed(final ActionEvent event)
                {
                // Check to see that we actually have a printer...
                if (PrinterJob.lookupPrintServices().length == 0)
                    {
                    JOptionPane.showMessageDialog(null,
                                                  ReportTablePlugin.MSG_NO_PRINTER,
                                                  ReportTablePlugin.PREFIX_PRINT + CompositeViewerUIComponentInterface.MSG_PRINT_COMPOSITE,
                                                  JOptionPane.WARNING_MESSAGE);
                    return;
                    }

                switch (sdaui.getCompositeViewerMode())
                    {
                    case COMPOSITE_EXPORTER:
                    {
                    LOGGER.error(SOURCE + "Invalid CompositeViewerMode [mode=" + sdaui.getCompositeViewerMode().getName() + "]");

                    break;
                    }

                    case COMPOSITE_VIEWER_CHART:
                    {
                    if ((viewerui.getChartViewer() != null)
                        && (viewerui.getChartViewer().getChartPanel() != null))
                        {
                        // Print the Chart
                        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        // ToDo WARNING! This does its own PageSetup dialog before the Print
                        viewerui.getChartViewer().getChartPanel().createChartPrintJob();
                        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }

                    break;
                    }

                    case COMPOSITE_VIEWER_METADATA:
                    {
                    // Obtain the MetadataList by a very convoluted path!
                    if ((viewerui.getMetadataViewer() != null)
                        && (viewerui.getMetadataViewer() instanceof MetadataExplorerUIComponentInterface)
                        && (((MetadataExplorerUIComponentInterface)viewerui.getMetadataViewer()).getTheExpanderUI() != null))
                        {
                        // Print the Report
                        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        ((MetadataExplorerUIComponentInterface)viewerui.getMetadataViewer()).getTheExpanderUI().printReport();
                        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }

                    break;
                    }

                    default:
                    {
                    LOGGER.error(SOURCE + "Invalid CompositeViewerMode [mode=" + sdaui.getCompositeViewerMode().getName() + "]");
                    }
                    }
                }
            };

        viewerui.getPrintButton().setAction(actionPrint);
        viewerui.getPrintButton().setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getPrintButton().setEnabled(false);


        //-------------------------------------------------------------------------------------
        // Put it all together

        listComponents.clear();

        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON));
        listComponents.add(labelName);
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getChannelsLabel());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(Box.createHorizontalGlue());

        listComponents.add(viewerui.getDetachAllButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getMetadataButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getPageSetupButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getPrintButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getExportButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        return (listComponents);
        }


    /***********************************************************************************************
     * Create the Dataset Viewer Toolbar Buttons, for injection into the Viewer.
     *
     * @param obsinstrument
     * @param sdaui
     * @param viewerui
     * @param fontdata
     * @param colourdata
     *
     * @return List<Component>
     */

    public static List<Component> createDatasetToolbarComponents(final ObservatoryInstrumentInterface obsinstrument,
                                                                 final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                                 final DatasetViewerUIComponentInterface viewerui,
                                                                 final FontInterface fontdata,
                                                                 final ColourInterface colourdata)
        {
        final List<Component> listComponents;
        final ContextAction actionAttach;
        final ContextAction actionDetach;
        final ContextAction actionMetadata;
        final ContextAction actionRemove;
        final ContextAction actionImport;

        final JLabel labelDataset;
        final JLabel labelOffset;
        final JLabel labelUnits;

        listComponents = new ArrayList<Component>(25);

        labelDataset = new JLabel("Dataset");
        labelDataset.setFont(fontdata.getFont());
        labelDataset.setForeground(colourdata.getColor());

        viewerui.setDatasetSelector(createComboSelectDataset(obsinstrument,
                                                             sdaui,
                                                             viewerui,
                                                             fontdata,
                                                             colourdata,
                                                             UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND));
        viewerui.getDatasetSelector().addItem(SuperposedDataAnalyserUIComponentInterface.NO_SELECTION_MSG);
        viewerui.getDatasetSelector().setSelectedIndex(DatasetManagerInterface.NO_SELECTION_INDEX);
        viewerui.getDatasetSelector().setEnabled(false);

        labelOffset = new JLabel("Offset %");
        labelOffset.setFont(fontdata.getFont());
        labelOffset.setForeground(colourdata.getColor());

        viewerui.setOffsetIndicator(new ToolbarIndicator(DatasetViewerUIComponentInterface.DIM_OFFSET_INDICATOR,
                                                         EMPTY_STRING,
                                                         DatasetViewerUIComponentInterface.TOOLTIP_OFFSET));
        viewerui.getOffsetIndicator().setValueFormat("00000");
        viewerui.getOffsetIndicator().setValueBackground(Color.BLACK);
        viewerui.getOffsetIndicator().setValue(PATTERN_OFFSET.format(0.0));

        labelUnits = new JLabel();
        labelUnits.setFont(fontdata.getFont());
        labelUnits.setForeground(colourdata.getColor());
        labelUnits.setText(SchemaUnits.SECONDS.toString());
        labelUnits.setToolTipText(DatasetViewerUIComponentInterface.TOOLTIP_UNITS);

        viewerui.setOffsetControl(new CoarseFineKnobs(DatasetViewerUIComponentInterface.DIM_OFFSET_CONTROL_KNOB,
                                                      true,
                                                      DatasetViewerUIComponentInterface.OFFSET_CONTROL_TICKLENGTH,
                                                      -100,
                                                      100,
                                                      -10,
                                                      10,
                                                      DatasetViewerUIComponentInterface.TOOLTIP_OFFSET_CONTROL_COARSE,
                                                      DatasetViewerUIComponentInterface.TOOLTIP_OFFSET_CONTROL_FINE,
                                                      UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND));
        viewerui.getOffsetControl().initialiseUI();
        viewerui.getOffsetControl().setEnabled(false);

        // Both the Offset Indicator and the Chart need to know about changes in the Knob
        // Wait until later to add the Chart in initialiseUI()
        viewerui.getOffsetControl().addChangeListener(new ChangeListener()
            {
            public void stateChanged(final ChangeEvent event)
                {
                if ((sdaui != null)
                    && (sdaui.getDatasetManager().hasSelection())
                    && (viewerui.getOffsetControl() != null)
                    && (viewerui.getOffsetIndicator() != null))
                    {
                    final double dblOffset;

                    dblOffset = viewerui.getOffsetControl().getValue();
                    sdaui.getDatasetManager().setSelectedOffset(dblOffset);
                    viewerui.getOffsetIndicator().setValue(PATTERN_OFFSET.format(dblOffset));

                    // Keep the UI in step with the new States
                    updateDatasetSelectorStates(sdaui, viewerui);
                    }
                }
            });

        // We also need to know about changes in Offset
        viewerui.getOffsetControl().addCommitChangeListener(viewerui);

        //-------------------------------------------------------------------------------------
        // Initialise the Buttons

        viewerui.setAttachButton(new JButton());
        viewerui.getAttachButton().setBorder(UIComponentPlugin.BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        viewerui.getAttachButton().setHideActionText(true);

        viewerui.setDetachButton(new JButton());
        viewerui.getDetachButton().setBorder(UIComponentPlugin.BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        viewerui.getDetachButton().setHideActionText(true);

        viewerui.setMetadataButton(new JButton());
        viewerui.getMetadataButton().setBorder(UIComponentPlugin.BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        viewerui.getMetadataButton().setHideActionText(true);

        viewerui.setRemoveButton(new JButton());
        viewerui.getRemoveButton().setBorder(UIComponentPlugin.BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        viewerui.getRemoveButton().setHideActionText(true);

        viewerui.setImportButton(new JButton());
        viewerui.getImportButton().setBorder(UIComponentPlugin.BORDER_BUTTON);

        // Ensure that no text appears next to the Icon...
        viewerui.getImportButton().setHideActionText(true);

        //-------------------------------------------------------------------------------------
        // Attach

        actionAttach = new ContextAction("Attach",
                                         RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                            ICON_ATTACH),
                                         DatasetViewerUIComponentInterface.TOOLTIP_ATTACH,
                                         KeyEvent.VK_A,
                                         false,
                                         true)
            {
            private static final long serialVersionUID = 8481630118218373501L;


            public void actionPerformed(final ActionEvent event)
                {
                final boolean boolSuccess;

                boolSuccess = AttachDataset.doAttachDataset(sdaui,
                                                            viewerui,
                                                            this);
                if (boolSuccess)
                    {
                    // Redraw the Composite completely, because something changed
                    DatasetManagerHelper.rebuildComposite(sdaui.getDatasetManager(),
                                                          sdaui,
                                                          true, true, true, true, true, true, true,
                                                          event,
                                                          LOADER_PROPERTIES.isChartDebug());
                    }
                }
            };

        viewerui.getAttachButton().setAction(actionAttach);
        viewerui.getAttachButton().setToolTipText((String) actionAttach.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getAttachButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Detach

        actionDetach = new ContextAction("Detach",
                                         RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                            ICON_DETACH),
                                         DatasetViewerUIComponentInterface.TOOLTIP_DETACH,
                                         KeyEvent.VK_D,
                                         false,
                                         true)
            {
            private static final long serialVersionUID = 4286774496711142895L;


            public void actionPerformed(final ActionEvent event)
                {
                final boolean boolSuccess;

                boolSuccess = DetachDataset.doDetachDataset(sdaui,
                                                            viewerui,
                                                            this);
                if (boolSuccess)
                    {
                    // Redraw the Composite completely, because something changed
                    DatasetManagerHelper.rebuildComposite(sdaui.getDatasetManager(),
                                                          sdaui,
                                                          false, true, true, true, true, true, true,
                                                          event,
                                                          LOADER_PROPERTIES.isChartDebug());
                    }
                }
            };

        viewerui.getDetachButton().setAction(actionDetach);
        viewerui.getDetachButton().setToolTipText((String) actionDetach.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getDetachButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Metadata

        actionMetadata = new ContextAction("Metadata",
                                           RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                              ICON_METADATA),
                                           DatasetViewerUIComponentInterface.TOOLTIP_METADATA,
                                           KeyEvent.VK_M,
                                           false,
                                           true)
            {
            final static String SOURCE = "ContextAction:Metadata ";
            private static final long serialVersionUID = -470016606468774520L;


            public void actionPerformed(final ActionEvent event)
                {
                switch (sdaui.getDatasetViewerMode())
                    {
                    case DATASET_VIEWER_CHART:
                    {
                    // Clicking this button must switch the displayed panel to the DatasetMetadata
                    switchDatasetViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_METADATA);

                    break;
                    }

                    case DATASET_VIEWER_METADATA:
                    {
                    // This calls refreshChart()
                    switchDatasetViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);
                    break;
                    }

                    default:
                    {
                    // It should not be possible to click this button in DatasetImporter mode
                    LOGGER.error(SOURCE + "Invalid Display Mode [mode=" + sdaui.getDatasetViewerMode().getName() + "]");
                    }
                    }
                }
            };

        viewerui.getMetadataButton().setAction(actionMetadata);
        viewerui.getMetadataButton().setToolTipText((String) actionMetadata.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getMetadataButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // Remove

        actionRemove = new ContextAction("Remove",
                                         RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                            ICON_REMOVE),
                                         DatasetViewerUIComponentInterface.TOOLTIP_REMOVE,
                                         KeyEvent.VK_R,
                                         false,
                                         true)
            {
            private static final long serialVersionUID = 2949455705724101912L;


            public void actionPerformed(final ActionEvent event)
                {
                final boolean boolSuccess;

                boolSuccess = RemoveDataset.doRemoveDataset(sdaui,
                                                            viewerui,
                                                            this);
                if (boolSuccess)
                    {
                    // Redraw the Composite completely, because something changed
                    DatasetManagerHelper.rebuildComposite(sdaui.getDatasetManager(),
                                                          sdaui,
                                                          false, true, true, true, true, true, true,
                                                          event,
                                                          LOADER_PROPERTIES.isChartDebug());
                    }
                }
            };

        viewerui.getRemoveButton().setAction(actionRemove);
        viewerui.getRemoveButton().setToolTipText((String) actionRemove.getValue(Action.SHORT_DESCRIPTION));
        viewerui.getRemoveButton().setEnabled(false);

        //-------------------------------------------------------------------------------------
        // DatasetType

        viewerui.setDatasetTypeLabel(new JLabel());
        viewerui.getDatasetTypeLabel().setFont(fontdata.getFont());
        viewerui.getDatasetTypeLabel().setForeground(colourdata.getColor());
        viewerui.getDatasetTypeLabel().setText(MSG_WAITING_FOR_IMPORT);
        viewerui.getDatasetTypeLabel().setToolTipText(DatasetViewerUIComponentInterface.TOOLTIP_DATASET_TYPE);
        viewerui.getDatasetTypeLabel().setEnabled(false);
        // Sets the alignment of the label's contents along the X axis
        viewerui.getDatasetTypeLabel().setHorizontalTextPosition(SwingConstants.RIGHT);

        //-------------------------------------------------------------------------------------
        // Import

        actionImport = new ContextAction("Import",
                                         RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                            ICON_DATASET_IMPORTER),
                                         DatasetViewerUIComponentInterface.TOOLTIP_DATASET_IMPORTER,
                                         KeyEvent.VK_I,
                                         false,
                                         true)
            {
            private static final long serialVersionUID = -8879684257949156344L;


            public void actionPerformed(final ActionEvent event)
                {
                // Clicking this button must switch the displayed panel to the DatasetImporter,
                // which we assumed is in the STOPPED state
                switchDatasetViewerDisplayMode(sdaui, SuperposedDataAnalyserDisplayMode.DATASET_IMPORTER);
                }
            };

        viewerui.getImportButton().setAction(actionImport);
        viewerui.getImportButton().setToolTipText((String) actionImport.getValue(Action.SHORT_DESCRIPTION));
        // Import is always enabled
        viewerui.getImportButton().setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Put it all together

        listComponents.clear();

        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON));

        listComponents.add(labelDataset);
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_LABEL_SEPARATOR));
        listComponents.add(Box.createHorizontalGlue());

        listComponents.add(viewerui.getDatasetSelector());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getAttachButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getDetachButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(labelOffset);
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_LABEL_SEPARATOR));

        listComponents.add((Component) viewerui.getOffsetIndicator());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_LABEL_SEPARATOR));

        listComponents.add((Component) viewerui.getOffsetControl());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getMetadataButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getRemoveButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getDatasetTypeLabel());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        listComponents.add(viewerui.getImportButton());
        listComponents.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

        return (listComponents);
        }


    /***********************************************************************************************
     * Switch the SDA DatasetViewer Display Mode to that specified.
     *
     * @param sdaui
     * @param mode
     */

    public static void switchDatasetViewerDisplayMode(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                      final SuperposedDataAnalyserDisplayMode mode)
        {
        final String SOURCE = "SuperposedDataAnalyserHelper.switchDatasetViewerDisplayMode() ";

        switch (mode)
            {
            case DATASET_IMPORTER:
                {
                final ExecuteCommandUIComponentInterface uiImporter;

                if (sdaui.getBottomUIComponent() != null)
                    {
                    // Stop the displayed UI
                    sdaui.getBottomUIComponent().stopUI();
                    }

                sdaui.setDatasetViewerMode(mode);
                sdaui.getBottomPanel().removeAll();

                // Reload the Importer (which has been initialised, run and stopped)
                uiImporter = sdaui.getDatasetImporter();
                sdaui.setBottomUIComponent(uiImporter);
                sdaui.getBottomPanel().add((Component)sdaui.getBottomUIComponent(),
                                           BorderLayout.CENTER);
                sdaui.getBottomPanel().revalidate();
                sdaui.getBottomPanel().repaint();

                // The host panel is already running, so run this one too
                sdaui.getBottomUIComponent().runUI();

                break;
                }

            case DATASET_VIEWER_CHART:
                {
                final ChartUIComponentPlugin uiChart;

                // Stop the displayed UI
                if (sdaui.getBottomUIComponent() != null)
                    {
                    sdaui.getBottomUIComponent().stopUI();
                    }

                sdaui.setDatasetViewerMode(mode);
                sdaui.getBottomPanel().removeAll();

                // Reload the ViewerChart into the DatasetViewer (which has been initialised, run and stopped)
                sdaui.getDatasetViewer().getViewerContainer().removeAll();
                uiChart = sdaui.getDatasetViewer().getChartViewer();
                sdaui.getDatasetViewer().getViewerContainer().add((Component) uiChart,
                                                                  BorderLayout.CENTER);

                // Reload the Viewer (which has been initialised, run and stopped)
                sdaui.setBottomUIComponent(sdaui.getDatasetViewer());
                sdaui.getBottomPanel().add((Component)sdaui.getBottomUIComponent(),
                                           BorderLayout.CENTER);
                sdaui.getBottomPanel().revalidate();
                sdaui.getBottomPanel().repaint();

                // The host panel is already running, so run this one too
                if ((sdaui.getDatasetManager() != null)
                    && (sdaui.getDatasetManager().getSelectedDAO() != null))
                    {
                    // Reasons to leave Chart alone, just update the data
                    sdaui.getDatasetManager().getSelectedDAO().setChannelCountChanged(true);
                    sdaui.getDatasetManager().getSelectedDAO().setMetadataChanged(true);

                    uiChart.refreshChart(sdaui.getDatasetManager().getSelectedDAO(),
                                         true,
                                         SOURCE);
                    }

                sdaui.getBottomUIComponent().runUI();

                // Let the User know how to return to the Metadata
                if ((sdaui.getDatasetViewer() != null)
                    && (sdaui.getDatasetViewer().getMetadataButton() != null)
                    && (sdaui.getDatasetViewer().getMetadataButton().getAction() != null))
                    {
                    sdaui.getDatasetViewer().getMetadataButton().getAction().putValue(Action.SHORT_DESCRIPTION,
                                                                                      DatasetViewerUIComponentInterface.TOOLTIP_METADATA);
                    sdaui.getDatasetViewer().getMetadataButton().getAction().putValue(Action.SMALL_ICON,
                                                                                      RegistryModelUtilities.getAtomIcon(sdaui.getHostInstrument().getHostAtom(),
                                                                                                                         ICON_METADATA));
                    sdaui.getDatasetViewer().getMetadataButton().repaint();
                    }

                // Check that we haven't hit the Import limit
                // If so, don't allow any more Imports
                // After returning to the Viewer, we can't come back here for more Imports
                sdaui.getDatasetViewer().getImportButton().setEnabled(!sdaui.getDatasetManager().isFull());

                break;
                }

            case DATASET_VIEWER_METADATA:
                {
                final UIComponentPlugin uiMetadata;

                // Stop the displayed UI
                if (sdaui.getBottomUIComponent() != null)
                    {
                    sdaui.getBottomUIComponent().stopUI();
                    }

                sdaui.setDatasetViewerMode(mode);
                sdaui.getBottomPanel().removeAll();

                // Reload the ViewerMetadata into the DatasetViewer (which has been initialised, run and stopped)
                sdaui.getDatasetViewer().getViewerContainer().removeAll();
                uiMetadata = sdaui.getDatasetViewer().getMetadataViewer();
                sdaui.getDatasetViewer().getViewerContainer().add((Component) uiMetadata,
                                                                  BorderLayout.CENTER);

                sdaui.setBottomUIComponent(sdaui.getDatasetViewer());
                sdaui.getBottomPanel().add((Component)sdaui.getBottomUIComponent(),
                                           BorderLayout.CENTER);
                sdaui.getBottomPanel().revalidate();
                sdaui.getBottomPanel().repaint();

                // The host panel is already running, so run this one too
                uiMetadata.runUI();
                sdaui.getBottomUIComponent().runUI();

                // Let the User know how to return to the Chart
                if ((sdaui.getDatasetViewer() != null)
                    && (sdaui.getDatasetViewer().getMetadataButton() != null)
                    && (sdaui.getDatasetViewer().getMetadataButton().getAction() != null))
                    {
                    sdaui.getDatasetViewer().getMetadataButton().getAction().putValue(Action.SHORT_DESCRIPTION,
                                                                                      DatasetViewerUIComponentInterface.TOOLTIP_CHART);
                    sdaui.getDatasetViewer().getMetadataButton().getAction().putValue(Action.SMALL_ICON,
                                                                                      RegistryModelUtilities.getAtomIcon(sdaui.getHostInstrument().getHostAtom(),
                                                                                                                         DatasetImporterUIComponent.ICON_DATASET_VIEWER));
                    sdaui.getDatasetViewer().getMetadataButton().repaint();
                    }

                // Check that we haven't hit the Import limit
                // If so, don't allow any more Imports
                // After returning to the Viewer, we can't come back here for more Imports
                if ((sdaui.getDatasetViewer() != null)
                    && (sdaui.getDatasetViewer().getImportButton() != null))
                    {
                    sdaui.getDatasetViewer().getImportButton().setEnabled(!sdaui.getDatasetManager().isFull());
                    }

                break;
                }

            default:
                {
                sdaui.setDatasetViewerMode(SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);
                sdaui.getBottomPanel().removeAll();
                sdaui.getBottomPanel().add(new BlankUIComponent("Invalid Display Mode",
                                                                UIComponentPlugin.DEFAULT_COLOUR_CANVAS,
                                                                UIComponentPlugin.COLOUR_WARN_TEXT),
                                           BorderLayout.CENTER);
                sdaui.getBottomPanel().revalidate();
                sdaui.getBottomPanel().repaint();

                // The host panel is already running, so run this one too
                sdaui.getBottomUIComponent().runUI();

                if (sdaui.getDatasetViewer().getMetadataButton() != null)
                    {
                    sdaui.getDatasetViewer().getMetadataButton().setEnabled(false);
                    }

                if (sdaui.getDatasetViewer().getImportButton() != null)
                    {
                    sdaui.getDatasetViewer().getImportButton().setEnabled(false);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Switch the SDA CompositeViewer Display Mode to that specified.
     *
     * @param sdaui
     * @param mode
     */

    public static void switchCompositeViewerDisplayMode(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                        final SuperposedDataAnalyserDisplayMode mode)
        {
        switch (mode)
            {
            case COMPOSITE_EXPORTER:
                {
                if (sdaui.getTopUIComponent() != null)
                    {
                    // Stop the displayed UI
                    sdaui.getTopUIComponent().stopUI();
                    }

                sdaui.setCompositeViewerMode(mode);
                sdaui.getTopPanel().removeAll();

                // Reload the Exporter (which has been initialised, run and stopped)
                sdaui.setTopUIComponent(sdaui.getCompositeExporter());
                sdaui.getTopPanel().add((Component)sdaui.getTopUIComponent(),
                                        BorderLayout.CENTER);
                sdaui.getTopPanel().revalidate();
                sdaui.getTopPanel().repaint();

                // The host panel is already running, so run this one too
                sdaui.getTopUIComponent().runUI();

                break;
                }

            case COMPOSITE_VIEWER_CHART:
                {
                if (sdaui.getTopUIComponent() != null)
                    {
                    // Stop the displayed UI
                    sdaui.getTopUIComponent().stopUI();
                    }

                sdaui.setCompositeViewerMode(mode);
                sdaui.getTopPanel().removeAll();

                // Reload the ViewerChart into the CompositeViewer (which has been initialised, run and stopped)
                sdaui.getCompositeViewer().getViewerContainer().removeAll();
                sdaui.getCompositeViewer().getViewerContainer().add((Component) sdaui.getCompositeViewer().getChartViewer(),
                                                                    BorderLayout.CENTER);

                // Reload the Viewer (which has been initialised, run and stopped
                sdaui.setTopUIComponent(sdaui.getCompositeViewer());
                sdaui.getTopPanel().add((Component)sdaui.getTopUIComponent(),
                                        BorderLayout.CENTER);

                sdaui.getTopPanel().revalidate();
                sdaui.getTopPanel().repaint();

                // The host panel is already running, so run this one too
                sdaui.getTopUIComponent().runUI();

                // Let the User know how to return to the Metadata
                if ((sdaui.getCompositeViewer().getMetadataButton() != null)
                    && (sdaui.getCompositeViewer().getMetadataButton().getAction() != null))
                    {
                    sdaui.getCompositeViewer().getMetadataButton().getAction().putValue(Action.SHORT_DESCRIPTION,
                                                                                        CompositeViewerUIComponentInterface.TOOLTIP_METADATA);
                    sdaui.getCompositeViewer().getMetadataButton().getAction().putValue(Action.SMALL_ICON,
                                                                                        RegistryModelUtilities.getAtomIcon(sdaui.getHostInstrument().getHostAtom(),
                                                                                                                           ICON_METADATA));
                    sdaui.getCompositeViewer().getMetadataButton().repaint();
                    }

                if (sdaui.getCompositeViewer().getDetachAllButton() != null)
                    {
                    sdaui.getCompositeViewer().getDetachAllButton().setEnabled(sdaui.getDatasetManager().canDetachAll());
                    }

                if (sdaui.getCompositeViewer().getExportButton() != null)
                    {
                    // If there's anything to detach, then it can also be exported
                    sdaui.getCompositeViewer().getExportButton().setEnabled(sdaui.getDatasetManager().canDetachAll());
                    }

                break;
                }

            case COMPOSITE_VIEWER_METADATA:
                {
                if (sdaui.getTopUIComponent() != null)
                    {
                    // Stop the displayed UI
                    sdaui.getTopUIComponent().stopUI();
                    }

                sdaui.setCompositeViewerMode(mode);
                sdaui.getTopPanel().removeAll();

                // Reload the ViewerMetadata into the CompositeViewer (which has been initialised, run and stopped)
                sdaui.getCompositeViewer().getViewerContainer().removeAll();
                sdaui.getCompositeViewer().getViewerContainer().add((Component) sdaui.getCompositeViewer().getMetadataViewer(),
                                                                    BorderLayout.CENTER);

                sdaui.setTopUIComponent(sdaui.getCompositeViewer());
                sdaui.getTopPanel().add((Component)sdaui.getTopUIComponent(),
                                        BorderLayout.CENTER);
                sdaui.getTopPanel().revalidate();
                sdaui.getTopPanel().repaint();

                // The host panel is already running, so run this one too
                sdaui.getTopUIComponent().runUI();

                // Let the User know how to return to the Composite Chart
                if ((sdaui.getCompositeViewer().getMetadataButton() != null)
                    && (sdaui.getCompositeViewer().getMetadataButton().getAction() != null))
                    {
                    sdaui.getCompositeViewer().getMetadataButton().getAction().putValue(Action.SHORT_DESCRIPTION,
                                                                                        CompositeViewerUIComponentInterface.TOOLTIP_CHART);
                    sdaui.getCompositeViewer().getMetadataButton().getAction().putValue(Action.SMALL_ICON,
                                                                                        RegistryModelUtilities.getAtomIcon(sdaui.getHostInstrument().getHostAtom(),
                                                                                                                           ICON_COMPOSITE_VIEWER));
                    sdaui.getCompositeViewer().getMetadataButton().repaint();
                    }

                if (sdaui.getCompositeViewer().getDetachAllButton() != null)
                    {
                    // Don't allow DetachAll while viewing Metadata, it could be confusing!
                    sdaui.getCompositeViewer().getDetachAllButton().setEnabled(false);
                    }

                if (sdaui.getCompositeViewer().getExportButton() != null)
                    {
                    // Don't allow Chart export while viewing Metadata, it could be confusing!
                    sdaui.getCompositeViewer().getExportButton().setEnabled(false);
                    }

                break;
                }

            default:
                {
                sdaui.setCompositeViewerMode(SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART);
                sdaui.getTopPanel().removeAll();
                sdaui.getTopPanel().add(new BlankUIComponent("Invalid Display Mode",
                                                             UIComponentPlugin.DEFAULT_COLOUR_CANVAS,
                                                             UIComponentPlugin.COLOUR_WARN_TEXT),
                                        BorderLayout.CENTER);
                sdaui.getTopPanel().revalidate();
                sdaui.getTopPanel().repaint();

                // The host panel is already running, so run this one too
                sdaui.getTopUIComponent().runUI();

                if (sdaui.getCompositeViewer().getMetadataButton() != null)
                    {
                    sdaui.getCompositeViewer().getMetadataButton().setEnabled(false);
                    }

                if (sdaui.getCompositeViewer().getExportButton() != null)
                    {
                    sdaui.getCompositeViewer().getExportButton().setEnabled(false);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Show the DAO XYDataset Chart and Metadata on the DatasetViewer UIComponentPlugin.
     * This optionally calls refreshChart().
     *
     * @param datasetmanager
     * @param datasetviewerui
     * @param refreshdatasetchart
     * @param debug
     */

    public static void showSelectedXYDatasetOnDatasetViewer(final DatasetManagerInterface datasetmanager,
                                                            final DatasetViewerUIComponentInterface datasetviewerui,
                                                            final boolean refreshdatasetchart,
                                                            final boolean debug)
        {
        final String SOURCE = "SuperposedDataAnalyserHelper.showSelectedXYDatasetOnDatasetViewer() ";
        final ObservatoryInstrumentDAOInterface daoSelected;

        // We only ever display the selected DAO
        daoSelected = datasetmanager.getSelectedDAO();

        // Show the XYDataset on the Viewer
        if ((daoSelected != null)
            && (datasetviewerui != null)
            && (datasetviewerui.isValidViewerUI())
            && (datasetviewerui.getChartViewer() != null))
            {
            final List<Metadata> listAggregateSelectedDAOMetadata;

            // There's only one Chart, but multiple DAOs
            // De-associate the current DAO from the DatasetViewer Chart
            // De-associate the Chart from the current DAO
            // These steps are probably not necessary!
            datasetviewerui.getChartViewer().setDAO(null);
            daoSelected.setChartUI(null);

            datasetviewerui.getChartViewer().setChannelSelector(false);
            datasetviewerui.getChartViewer().setDatasetDomainControl(false);
            datasetviewerui.getChartViewer().setCanAutorange(true);
            datasetviewerui.getChartViewer().setChartName(datasetmanager.getSelectedName());
            datasetviewerui.getChartViewer().setChannelCount(daoSelected.getRawDataChannelCount());
            datasetviewerui.getChartViewer().setTemperatureChannel(daoSelected.hasTemperatureChannel());

            // This calls setDatasetType() and setDatasetDomainChanged()
            // and will reset the Range and Domain crosshairs
            datasetviewerui.getChartViewer().setPrimaryXYDataset(daoSelected,
                                                                 daoSelected.getXYDataset());

            // We only need the Metadata that arrived in the DAO, nothing else
            // NOTE THAT The DAO data take precedence over those in the Wrapper
            listAggregateSelectedDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(null,
                                                                                             null,
                                                                                             null,
                                                                                             daoSelected,
                                                                                             null,
                                                                                             SOURCE,
                                                                                             debug);
            // There's only one Chart, but multiple DAOs
            // Associate the selected DAO with the DatasetViewer Chart
            // Associate the Chart with the selected DAO
            // The Metadata came from the SelectedDAO, so don't re-apply
            datasetviewerui.getChartViewer().setMetadata(listAggregateSelectedDAOMetadata,
                                                         daoSelected,
                                                         false,
                                                         debug);
            // Reasons for complete rebuild
            daoSelected.setDatasetTypeChanged(true);
            daoSelected.setChannelCountChanged(true);
            datasetviewerui.getChartViewer().setChannelSelectionChanged(true);
            datasetviewerui.getChartViewer().setDatasetDomainChanged(true);

            // Reasons to leave Chart alone, just update the data
            daoSelected.setMetadataChanged(true);
            daoSelected.setRawDataChanged(true);
            daoSelected.setProcessedDataChanged(true);

            if (refreshdatasetchart)
                {
                // Now refresh *on a separate Thread* which will not return here....
                // Apply the single instance of the DatasetViewer Chart to this new DAO,
                // so that all dataset DAOs refer to the same Chart
                datasetviewerui.getChartViewer().refreshChart(daoSelected,
                                                              true,
                                                              SOURCE);
                }

            // Show the Metadata on the tab
            // See similar code in DatasetManager.rebuildComposite()
            if ((datasetviewerui.getMetadataViewer() != null)
                && (datasetviewerui.getMetadataViewer() instanceof MetadataExplorerUIComponentInterface))
                {
                final MetadataExplorerUIComponentInterface uiDatasetMetadataExplorer;

                uiDatasetMetadataExplorer = (MetadataExplorerUIComponentInterface)datasetviewerui.getMetadataViewer();

                // This resets the selection to the root
                uiDatasetMetadataExplorer.setMetadataList(listAggregateSelectedDAOMetadata);
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to show the Chart on the DatasetViewer");
            }
        }


    /***********************************************************************************************
     * Create the ComboBox to select Secondary Datasets to display.
     *
     * @param obsinstrument
     * @param sdaui
     * @param datasetviewerui
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     *
     * @return JComboBox
     */

    private static JComboBox createComboSelectDataset(final ObservatoryInstrumentInterface obsinstrument,
                                                      final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                      final DatasetViewerUIComponentInterface datasetviewerui,
                                                      final FontInterface fontdata,
                                                      final ColourInterface colourforeground,
                                                      final ColourInterface colourbackground)
        {
        final String SOURCE = "SuperposedDataAnalyserHelper.createComboSelectDataset() ";
        final JComboBox comboSelectDataset;
        final ActionListener choiceListener;

        comboSelectDataset = new JComboBox();

        comboSelectDataset.setFont(fontdata.getFont());
        comboSelectDataset.setForeground(colourforeground.getColor());
        comboSelectDataset.setRenderer(new AlignedListIconCellRenderer(obsinstrument.getHostAtom(),
                                                                       SwingConstants.LEFT, // Sets the alignment of the label's contents along the X axis
                                                                       SwingConstants.LEFT, // Sets the horizontal position of the label's text, relative to its image
                                                                       fontdata,
                                                                       colourforeground,
                                                                       colourbackground));

        // Do NOT allow the combo box to take up all the remaining space!
        comboSelectDataset.setPreferredSize(new Dimension(WIDTH_DATASETSELECTOR_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        comboSelectDataset.setMaximumSize(new Dimension(WIDTH_DATASETSELECTOR_DROPDOWN, UIComponentPlugin.HEIGHT_TOOLBAR_ICON - 4));
        comboSelectDataset.setAlignmentX(0);
        comboSelectDataset.setToolTipText(TOOLTIP_SHOW_DATASET);
        comboSelectDataset.setEnabled(false);
        comboSelectDataset.setEditable(false);

        ToolTipManager.sharedInstance().registerComponent(comboSelectDataset);

        choiceListener = new ActionListener()
            {
            // Select a new Secondary Dataset
            public void actionPerformed(final ActionEvent event)
                {
                // This is the only place where the state of the Semaphore is tested
                if ((datasetviewerui != null)
                    && (datasetviewerui.getDatasetSemaphore() != null)
                    && (datasetviewerui.getDatasetSemaphore().getState() == BLOCK_EVENTS))
                    {
                    return;
                    }

                // This method is only ever called from the UI, so it is ok to show a MessageDialog
                if ((InstrumentState.isOff(obsinstrument))
                    && (ObservatoryUIHelper.isSelectedInstrument(obsinstrument))
                    && (UIComponentHelper.isUIComponentShowing(datasetviewerui)))
                    {
                    final String [] message =
                            {
                                    MSG_SWITCHED_ON,
                                    MSG_CLICK_GREEN
                            };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  TITLE_SELECT_DATASET,
                                                  JOptionPane.WARNING_MESSAGE);
                    // This is the easiest way!
                    return;
                    }

                // We can't select a DAO if there are no DAOs...
                // ...or if something has been corrupted
                if ((sdaui != null)
                    && (sdaui.getDatasetManager().isValidManager())
                    && (datasetviewerui != null)
                    && (datasetviewerui.isValidViewerUI()))
                    {
                    try
                        {
                        final int intComboSelectedIndex;

                        // Returns -1 if no item is selected
                        intComboSelectedIndex = comboSelectDataset.getSelectedIndex();

                        // Check the integrity of the dataset references
                        if (intComboSelectedIndex > DatasetManagerInterface.NO_SELECTION_INDEX)
                            {
                            // There's only one Chart, but multiple DAOs
                            // De-associate the current DAO from the DatasetViewer Chart
                            // De-associate the Chart from the current DAO
                            datasetviewerui.getChartViewer().setDAO(null);

                            if (sdaui.getDatasetManager().getSelectedDAO() != null)
                                {
                                sdaui.getDatasetManager().getSelectedDAO().setChartUI(null);
                                }

                            // Update the selection index to this new selection
                            sdaui.getDatasetManager().setSelectedIndex(intComboSelectedIndex);

                            // Update the Toolbar buttons to show the new state
                            datasetviewerui.getOffsetIndicator().setValue(PATTERN_OFFSET.format(sdaui.getDatasetManager().getSelectedOffset()));

                            // Offset may now be adjusted again, but not for the Primary
                            datasetviewerui.getOffsetControl().setEnabled(sdaui.getDatasetManager().canAdjustOffset());

                            // BEWARE setValue() will fire a ChangeEvent,
                            // which will use the *current* SecondaryDatasetIndex
                            datasetviewerui.getOffsetControl().setValue(sdaui.getDatasetManager().getSelectedOffset());

                            // Already attached, so can't attach again
                            datasetviewerui.getAttachButton().setEnabled(sdaui.getDatasetManager().canAttach());

                            // The User will be asked if they really want to Detach the Primary,
                            // which will clear all current attachments
                            datasetviewerui.getDetachButton().setEnabled(sdaui.getDatasetManager().canDetach());

                            // We can always view the Metadata of a selection
                            datasetviewerui.getMetadataButton().setEnabled(true);

                            // A Primary Dataset cannot be removed directly, it must be Detached first
                            datasetviewerui.getRemoveButton().setEnabled(sdaui.getDatasetManager().canRemove());

                            // Show the selected Chart and Metadata using the new DAO
                            // This calls refreshChart()
                            showSelectedXYDatasetOnDatasetViewer(sdaui.getDatasetManager(),
                                                                 datasetviewerui,
                                                                 true,
                                                                 LOADER_PROPERTIES.isChartDebug());
                            }
                        else
                            {
                            LOGGER.error("No current selection, or the DatasetManager is corrupted");

                            isNull(datasetviewerui.getOffsetIndicator(), "0");
                            isNull(datasetviewerui.getOffsetControl(), "1");
                            isNull(datasetviewerui.getAttachButton(), "2");
                            isNull(datasetviewerui.getDetachButton(), "3");
                            isNull(datasetviewerui.getMetadataButton(), "4");
                            isNull(datasetviewerui.getRemoveButton(), "5");
                            }
                        }

                    catch (final IndexOutOfBoundsException exception)
                        {
                        LOGGER.error("Invalid Secondary Data because Secondary DAOs not in sync");
                        }
                    }
                else
                    {
                    // The drop-down should not be enabled unless there are data to select
                    LOGGER.error("No Secondary DAOs currently loaded or invalid UI Component");
                    }
                }
            };

        comboSelectDataset.addActionListener(choiceListener);

        return (comboSelectDataset);
        }


    /***********************************************************************************************
     * Build the name of the imported dataset, preferably using the imported Metadata,
     * but only if it contains key Observation.Title, otherwise be creative.
     *
     * @param obsinstrument
     * @param dao
     *
     * @return String
     */

    public static String buildDatasetName(final ObservatoryInstrumentInterface obsinstrument,
                                          final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DatasetViewerUIComponent.buildDatasetName() ";
        final StringBuffer buffer;
        final List<Metadata> listAggregateDAOMetadata;
        final String strObservationTitle;

        buffer = new StringBuffer();

        // We only need the Metadata that arrived in the DAO, nothing else
        // NOTE THAT The DAO data take precedence over those in the Wrapper
        listAggregateDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(null,
                                                                                 null,
                                                                                 null,
                                                                                 dao,
                                                                                 null,
                                                                                 SOURCE,
                                                                                 false);

        strObservationTitle = MetadataHelper.getMetadataValueByKey(listAggregateDAOMetadata,
                                                                   MetadataDictionary.KEY_OBSERVATION_TITLE.getKey());

        if (!NO_DATA.equals(strObservationTitle))
            {
            buffer.append(strObservationTitle);
            }
        else if (dao.getXYDataset() != null)
            {
            buffer.append("[ch=");
            buffer.append(dao.getRawDataChannelCount());
            buffer.append("] [raw=");
            buffer.append(dao.getRawData().size());
            buffer.append("] [proc=");
            buffer.append(dao.getXYDataset().getItemCount(0));
            buffer.append("]");
            }
        else
            {
            buffer.append("Import ");
            buffer.append(obsinstrument.getObservatoryClock().getDateTimeNowAsString());
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Check the states of the data structures for the secondary datasets and their selection.
     *
     * @param sdaui
     * @param datasetselector
     * @param selectedindex
     *
     * @return boolean
     */

    public static boolean isValidSelectionState(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                final JComboBox datasetselector,
                                                final int selectedindex)
        {
        // We must have at least one DAO to remove...

        return ((sdaui != null)
                && (sdaui.getDatasetManager().isValidManager())
                && (selectedindex > DatasetManagerInterface.NO_SELECTION_INDEX)
                && (selectedindex < sdaui.getDatasetManager().size())
                && (datasetselector != null)
                && (datasetselector.getItemCount() > 0)
                && (datasetselector.getItemCount() == sdaui.getDatasetManager().size()));
        }


    /***********************************************************************************************
     * A simple debug utility to indicate NULL.
     *
     * @param object
     * @param id
     */

    private static void isNull(final Object object,
                               final String id)
        {
        if (object == null)
            {
            LOGGER.error("Object [id=" + id + "] is NULL");
            }
        }


    /***********************************************************************************************
     * Update the DatasetStates shown on the DatasetSelector JComboBox with those in the DatasetManager.
     *
     * @param sdaui
     * @param viewerui
     */

    public static void updateDatasetSelectorStates(final SuperposedDataAnalyserUIComponentInterface sdaui,
                                                   final DatasetViewerUIComponentInterface viewerui)
        {
        final String SOURCE = "SuperposedDataAnalyserHelper.updateDatasetSelectorStates() ";

        if ((sdaui != null)
            && (viewerui != null)
            && (viewerui.getDatasetSelector() != null)
            && (viewerui.getDatasetSelector().getItemCount() > 0))
            {
            // Re-render each item, to show the current states
            for (int intItemIndex = 0;
                 intItemIndex < viewerui.getDatasetSelector().getItemCount();
                 intItemIndex++)
                {
                if (viewerui.getDatasetSelector().getItemAt(intItemIndex) instanceof ListCellDatasetState)
                    {
                    final ListCellDatasetState listcellDatasetState;
                    final ObservatoryInstrumentDAOInterface dao;
                    int intAttachmentIndex;

                    // DAOs
                    // Names
                    // States
                    // Offsets
                    // AttachedDAOs

                    listcellDatasetState = (ListCellDatasetState)viewerui.getDatasetSelector().getItemAt(intItemIndex);
                    listcellDatasetState.setDatasetState(sdaui.getDatasetManager().getSecondaryStates().get(intItemIndex));

                    dao = sdaui.getDatasetManager().getSecondaryDAOs().get(intItemIndex);
                    intAttachmentIndex = DatasetManagerInterface.NO_SELECTION_INDEX;

                    if ((dao != null)
                        && (sdaui.getDatasetManager().getAttachedDAOs().contains(dao)))
                        {
                        intAttachmentIndex = sdaui.getDatasetManager().getAttachedDAOs().indexOf(dao);
                        listcellDatasetState.setIndex(intAttachmentIndex);
                        }
                    else
                        {
                        listcellDatasetState.setIndex(intAttachmentIndex);
                        }

                    switch (listcellDatasetState.getDatasetState())
                        {
                        case ATTACHED:
                            {
                            listcellDatasetState.setTooltipText("Attached Secondary Dataset ["
                                                                + intAttachmentIndex
                                                                + "] (offset "
                                                                + PATTERN_OFFSET.format(sdaui.getDatasetManager().getSecondaryOffsets().get(intItemIndex))
                                                                + ")");
                            break;
                            }

                        case DETACHED:
                            {
                            listcellDatasetState.setTooltipText(EMPTY_STRING);
                            break;
                            }

                        case LOCKED:
                            {
                            listcellDatasetState.setTooltipText("Primary Dataset [0]");
                            break;
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "The DatasetSelector has an invalid component");
                    }
                }

            viewerui.getDatasetSelector().repaint();
            }
        }
    }
