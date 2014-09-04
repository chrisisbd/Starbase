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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObservatoryMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.ObserverMetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentState;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.List;


/***************************************************************************************************
 * MetadataExplorerUIComponent.
 */

public final class MetadataExplorerFrameUIComponent extends InstrumentUIComponentDecorator
                                                    implements MetadataExplorerFrameUIComponentInterface
    {
    private static final long serialVersionUID = 7811007468443481752L;

    // Injections
    private List<Metadata> listMetadata;
    private final boolean boolShowImporters;

    // UI
    private JToolBar toolBar;
    private MetadataExplorerUIComponentInterface uiExplorer;


    /***********************************************************************************************
     * Initialise the Toolbar.
     *
     * @param explorerframe
     * @param toolbar
     * @param obsinstrument
     * @param showimporters
     * @param font
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    private static void initialiseToolbar(final MetadataExplorerFrameUIComponentInterface explorerframe,
                                          final JToolBar toolbar,
                                          final ObservatoryInstrumentInterface obsinstrument,
                                          final boolean showimporters,
                                          final FontInterface font,
                                          final ColourInterface colourforeground,
                                          final ColourInterface colourbackground,
                                          final boolean debug)
        {
        final String SOURCE = "MetadataExplorerFrameUIComponent.initialiseToolbar() ";

        if ((explorerframe != null)
            && (toolbar != null))
            {
            final JLabel labelName;

            toolbar.removeAll();
            toolbar.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

            //-------------------------------------------------------------------------------------
            // Initialise the Label

            labelName = new JLabel(METADATA_EDITOR_NAME,
                                   RegistryModelUtilities.getCommonIcon(FILENAME_ICON_METADATA_EDITOR),
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

            labelName.setFont(font.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
            labelName.setForeground(colourforeground.getColor());
            labelName.setIconTextGap(TOOLBAR_ICON_TEXT_GAP);

            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR_START);
            toolbar.add(labelName);
            toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

            toolbar.add(Box.createHorizontalGlue());

            //-------------------------------------------------------------------------------------
            // See if the Importer drop-downs are required

            if (showimporters)
                {
                final JLabel labelImportMetadata;
                final JLabel labelRemoveMetadata;

                // Add the Toolbar combo box to Import Metadata
                labelImportMetadata = new JLabel(TOOLTIP_METADATA_IMPORT);
                labelImportMetadata.setFont(font.getFont());
                labelImportMetadata.setForeground(colourforeground.getColor());
                toolbar.add(labelImportMetadata);
                toolbar.addSeparator(DIM_LABEL_SEPARATOR);
                toolbar.add(MetadataUIHelper.createComboImportMetadata(obsinstrument,
                                                                       font,
                                                                       colourforeground,
                                                                       colourbackground));
                toolbar.addSeparator(DIM_TOOLBAR_SEPARATOR);

                // Add the Toolbar combo box to Remove Metadata
                labelRemoveMetadata = new JLabel(TOOLTIP_METADATA_REMOVE);
                labelRemoveMetadata.setFont(font.getFont());
                labelRemoveMetadata.setForeground(colourforeground.getColor());
                toolbar.add(labelRemoveMetadata);
                toolbar.addSeparator(DIM_LABEL_SEPARATOR);
                toolbar.add(MetadataUIHelper.createComboRemoveMetadata(obsinstrument,
                                                                       font,
                                                                       colourforeground,
                                                                       colourbackground));
                }

            //-------------------------------------------------------------------------------------
            // Printing
            //-------------------------------------------------------------------------------------
            // Add buttons to page setup and print the MetadataExpander ReportTable

            if ((explorerframe.getMetadataExplorerUI() != null)
                && (explorerframe.getMetadataExplorerUI().getSelectedExpanderUI() != null)
                && (explorerframe.getMetadataExplorerUI().getSelectedExpanderUI().isPrintable()))
                {
                final String strTitle;
                final ContextAction actionPageSetup;
                final ContextAction actionPrint;
                final JButton buttonPageSetup;
                final JButton buttonPrint;

                strTitle = explorerframe.getMetadataExplorerUI().getSelectedExpanderUI().getReportUniqueName();

                actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + strTitle,
                                                    RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PAGE_SETUP),
                                                    ReportTablePlugin.PREFIX_PAGE_SETUP + strTitle,
                                                    KeyEvent.VK_S,
                                                    false,
                                                    true)
                    {
                    final static String SOURCE = "ContextAction:PageSetup ";
                    private static final long serialVersionUID = 6802400471966299436L;


                    public void actionPerformed(final ActionEvent event)
                        {
                        if (explorerframe.getMetadataExplorerUI() != null)
                            {
                            final PrinterJob printerJob;
                            final PageFormat pageFormat;

                            printerJob = PrinterJob.getPrinterJob();
                            pageFormat = printerJob.pageDialog(explorerframe.getMetadataExplorerUI().getPageFormat());

                            if (pageFormat != null)
                                {
                                explorerframe.getMetadataExplorerUI().setPageFormat(pageFormat);
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "MetadataEditor unexpectedly NULL");
                            }
                        }
                    };

                buttonPageSetup = new JButton();
                buttonPageSetup.setBorder(BORDER_BUTTON);
                buttonPageSetup.setAction(actionPageSetup);
                buttonPageSetup.setHideActionText(true);
                buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
                buttonPageSetup.setEnabled(true);

                actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + strTitle,
                                                RegistryModelUtilities.getCommonIcon(FILENAME_ICON_PRINT),
                                                ReportTablePlugin.PREFIX_PRINT + strTitle,
                                                KeyEvent.VK_P,
                                                false,
                                                false)
                    {
                    private static final long serialVersionUID = 597658600242486985L;


                    public void actionPerformed(final ActionEvent event)
                        {
                        // Check to see that we actually have a printer...
                        if (PrinterJob.lookupPrintServices().length == 0)
                            {
                            JOptionPane.showMessageDialog(null,
                                                          ReportTablePlugin.MSG_NO_PRINTER,
                                                          ReportTablePlugin.PREFIX_PRINT + strTitle,
                                                          JOptionPane.WARNING_MESSAGE);
                            return;
                            }

                        // Print the Report
                        explorerframe.getMetadataExplorerUI().getSelectedExpanderUI().printReport();
                        }
                    };

                buttonPrint = new JButton();
                buttonPrint.setBorder(BORDER_BUTTON);
                buttonPrint.setAction(actionPrint);
                buttonPrint.setHideActionText(true);
                buttonPrint.setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
                buttonPrint.setEnabled(true);

                toolbar.addSeparator(DIM_LABEL_SEPARATOR);
                toolbar.addSeparator(DIM_LABEL_SEPARATOR);
                toolbar.add(buttonPageSetup);
                toolbar.addSeparator(DIM_LABEL_SEPARATOR);
                toolbar.add(buttonPrint);
                }

            NavigationUtilities.updateComponentTreeUI(toolbar);
            }
        }


    /***********************************************************************************************
     * Construct an MetadataExplorerUIComponent.
     * The metadata is contained in the List of Metadata (name, value, units, type)
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param metadatalist
     * @param showimporters
     * @param hostresourcekey
     */

    public MetadataExplorerFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                            final Instrument instrumentxml,
                                            final ObservatoryUIInterface hostui,
                                            final TaskPlugin task,
                                            final List<Metadata> metadatalist,
                                            final boolean showimporters,
                                            final FontInterface font,
                                            final ColourInterface colour,
                                            final String hostresourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              hostresourcekey);

        // Injections
        this.listMetadata = metadatalist;
        this.boolShowImporters = showimporters;

        // UI
        // See if we need to inform listeners of MetadataChanged events
        // Listen to the Observatory and Observer Metadata
        if ((hostinstrument != null)
            && (hostinstrument.getContext() != null)
            && (hostinstrument.getContext().getObservatory() != null))
            {
            hostinstrument.getContext().getObservatory().addObservatoryMetadataChangedListener(this);
            hostinstrument.getContext().getObservatory().addObserverMetadataChangedListener(this);
            }

        setDebug(false);
        }


    /**********************************************************************************************/
    /* UIComponent State                                                                          */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "MetadataExplorerFrameUIComponent.initialiseUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        // Colours
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        // Create the MetadataExplorer and initialise it
        // Do this first to get the colours and fonts
        // The Toolbar is provided by this frame, not the Explorer
        setMetadataExplorerUI(new MetadataExplorerUIComponent(getHostTask(),
                                                              getHostInstrument(),
                                                              getMetadataList(),
                                                              getResourceKey(),
                                                              isDebug()));
        getMetadataExplorerUI().initialiseUI();

        // Put the components together
        removeAll();

        // Create the Metadata JToolBar and initialise it
        setToolBar(new JToolBar());
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(DIM_TOOLBAR_SIZE);

        initialiseToolbar(this,
                          getToolBar(),
                          getHostInstrument(),
                          isShowingImporters(),
                          getFontData(),
                          getColourData(),
                          DEFAULT_COLOUR_TAB_BACKGROUND,
                          isDebug());

        add(getToolBar(), BorderLayout.NORTH);

        add((Component) getMetadataExplorerUI(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        // TODO MOVE ?
        UIComponentHelper.runComponentAndTransferActions((Component) getMetadataExplorerUI(), this);
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        super.stopUI();

        if (getMetadataExplorerUI() != null)
            {
            getMetadataExplorerUI().stopUI();
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        // Stop listening to MetadataChanged events
        if ((getHostInstrument() != null)
            && (getHostInstrument().getContext() != null)
            && (getHostInstrument().getContext().getObservatory() != null))
            {
            getHostInstrument().getContext().getObservatory().removeObservatoryMetadataChangedListener(this);
            getHostInstrument().getContext().getObservatory().removeObserverMetadataChangedListener(this);
            }

        if (getToolBar() != null)
            {
            getToolBar().removeAll();
            setToolBar(null);
            }

        if (getMetadataExplorerUI() != null)
            {
            getMetadataExplorerUI().disposeUI();
            setMetadataExplorerUI(null);
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the MetadataExplorerUI JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the MetadataExplorerUI JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the MetadataExplorerUI.
     *
     * @return MetadataExplorerUIComponentInterface
     */

    public MetadataExplorerUIComponentInterface getMetadataExplorerUI()
        {
        return (this.uiExplorer);
        }


    /***********************************************************************************************
     * Set the MetadataExplorerUI.
     *
     * @param explorerui
     */

    private void setMetadataExplorerUI(final MetadataExplorerUIComponentInterface explorerui)
        {
        this.uiExplorer = explorerui;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the List of Metadata upon which the UIComponent is based.
     *
     * @return List<Metadata>
     */

    private List<Metadata> getMetadataList()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the complete List of Metadata for this UIComponent.
     * See similar code in treeSelection().
     *
     * @param metadatalist
     */

    public void setMetadataList(final List<Metadata> metadatalist)
        {
        final String SOURCE = "MetadataExplorerUIComponent.setMetadataList() ";

        this.listMetadata = metadatalist;

        MetadataHelper.showMetadataList(metadatalist,
                                        SOURCE + "Metadata available to Editor",
                                        isDebug());

        // Rebuild the existing Tree with the new Metadata
        if ((getMetadataList() != null)
            && (getMetadataExplorerUI() != null))
            {
            getMetadataExplorerUI().setMetadataList(metadatalist);
            }
        else
            {
            LOGGER.warn(SOURCE + "Metadata or MetadataExplorerUI are NULL, unable to set Metadata on MetadataExplorerUI");
            }
        }


    /***********************************************************************************************
     * Indicate if the MetadataExplorer should show Importer drop downs.
     *
     * @return boolean
     */

    private boolean isShowingImporters()
        {
        return (this.boolShowImporters);
        }


    /******************************************************************************************/
    /* Events                                                                                 */
    /*******************************************************************************************
     * Indicate that the ObservatoryMetadata has changed.
     *
     * @param event
     */

    public void observatoryChanged(final ObservatoryMetadataChangedEvent event)
        {
        final String SOURCE = "MetadataExplorerUIComponent.observatoryChanged() ";

        if ((event != null)
            && (!event.getSource().equals(this))
            && (UIComponentState.RUNNING.equals(getUIState()))
            && (event.getMetadataKey() != null)
            && (event.getMetadataKey().startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey())))
            {
            LOGGER.debug(isDebug(),
                         SOURCE + "[state=" + event.getItemState().getName()
                             + "] [key=" + event.getMetadataKey()
                             + "] [instrument=" + getHostInstrument().getInstrument().getIdentifier()
                             + "] [source=" + event.getSource().getClass().getName()
                             + "] [this=" + this.getClass().getName() + "]");

            // TODO
            }
        }


    /*******************************************************************************************
     * Indicate that the ObserverMetadata has changed.
     *
     * @param event
     */

    public void observerChanged(final ObserverMetadataChangedEvent event)
        {
        final String SOURCE = "MetadataExplorerUIComponent.observerChanged() ";

        if ((event != null)
            && (!event.getSource().equals(this))
            && (UIComponentState.RUNNING.equals(getUIState()))
            && (event.getMetadataKey() != null)
            && (event.getMetadataKey().startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey())))
            {
            LOGGER.debug(isDebug(),
                         SOURCE + "[state=" + event.getItemState().getName()
                             + "] [key=" + event.getMetadataKey()
                             + "] [instrument=" + getHostInstrument().getInstrument().getIdentifier()
                             + "] [source=" + event.getSource().getClass().getName()
                             + "] [this=" + this.getClass().getName() + "]");

            // TODO
            }
        }
    }
