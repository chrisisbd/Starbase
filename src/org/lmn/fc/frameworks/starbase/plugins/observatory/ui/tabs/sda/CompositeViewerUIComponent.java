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

import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.CompositeViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SuperposedDataAnalyserUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.LogLinChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataExplorerUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * CompositeViewerUIComponent.
 */

public final class CompositeViewerUIComponent extends UIComponent
                                              implements CompositeViewerUIComponentInterface
    {
    private static final long serialVersionUID = 2662073525959421709L;

    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final SuperposedDataAnalyserUIComponentInterface uiSDA;
    private final FontInterface fontData;
    private final ColourInterface colourData;

    // UI Components for the Composite Viewer
    private JLabel labelChannels;
    private JButton buttonDetachAll;
    private JButton buttonMetadata;
    private JButton buttonExport;
    private JButton buttonPageSetup;
    private JButton buttonPrint;

    private final UIComponentPlugin uiViewer;
    private ChartUIComponentPlugin chartViewer;
    private UIComponentPlugin metadataViewer;


    /***********************************************************************************************
     * Construct a CompositeViewerUIComponent.
     *
     * @param obsinstrument
     * @param sdaui
     * @param fontdata
     * @param colourdata
     */

    public CompositeViewerUIComponent(final ObservatoryInstrumentInterface obsinstrument,
                                      final SuperposedDataAnalyserUIComponentInterface sdaui,
                                      final FontInterface fontdata,
                                      final ColourInterface colourdata)
        {
        super();

        // Injections
        this.observatoryInstrument = obsinstrument;
        this.uiSDA = sdaui;
        this.fontData = fontdata;
        this.colourData = colourdata;

        // UI
        this.labelChannels = null;
        this.buttonDetachAll = null;
        this.buttonMetadata = null;
        this.buttonExport = null;
        this.buttonPageSetup = null;
        this.buttonPrint = null;

        this.uiViewer = new UIComponent();
        this.chartViewer = null;
        this.metadataViewer = null;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final List<Component> listComponents;
        final JToolBar toolbarViewer;
        final MetadataExplorerUIComponentInterface uiMetadata;

        super.initialiseUI();

        // Create the Viewer ToolBar Components
        listComponents = SuperposedDataAnalyserHelper.createCompositeToolbarComponents(getObservatoryInstrument(),
                                                                                       getSdaUI(),
                                                                                       this,
                                                                                       getFontData(),
                                                                                       getColourData());
        toolbarViewer = UIComponentHelper.buildToolbar(listComponents);
        toolbarViewer.setFloatable(false);
        toolbarViewer.setMinimumSize(DIM_TOOLBAR_SIZE);
        toolbarViewer.setPreferredSize(DIM_TOOLBAR_SIZE);
        toolbarViewer.setMaximumSize(DIM_TOOLBAR_SIZE);
        toolbarViewer.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        // There is only ever one Chart for the CompositeViewer
        // A special version for use in e.g. the Superposed Data Analyser, with no Toolbar
        setChartViewer(new LogLinChartUIComponent(getSdaUI().getHostTask(),
                                                  getObservatoryInstrument(),
                                                  SuperposedDataAnalyserUIComponentInterface.TITLE_COMPOSITE,
                                                  null,
                                                  REGISTRY.getFrameworkResourceKey(),
                                                  DataUpdateType.DECIMATE,
                                                  REGISTRY.getIntegerProperty(getObservatoryInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX),
                                                  -SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE,
                                                  SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE,
                                                  -SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE,
                                                  SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE)
            {
            static final long serialVersionUID = 8318030305607681611L;


            /**********************************************************************************
             * Initialise the Chart.
             */

            public synchronized void initialiseUI()
                {
                final String SOURCE = "ChartUIComponent.initialiseUI() ";

                setChannelSelector(true);
                setDatasetDomainControl(true);

                super.initialiseUI();

                // Indicate if the Chart can Autorange, and is currently Autoranging
                setCanAutorange(true);
                setLinearMode(true);

                // Configure the Chart for this specific use
                if (getChannelSelectorOccupant() != null)
                    {
                    getChannelSelectorOccupant().setAutoranging(true);
                    getChannelSelectorOccupant().setLinearMode(true);
                    getChannelSelectorOccupant().setDecimating(true);
                    getChannelSelectorOccupant().setLegend(false);
                    getChannelSelectorOccupant().setShowChannels(true);
                    getChannelSelectorOccupant().debugSelector(LOADER_PROPERTIES.isChartDebug(), SOURCE);
                    }
                }
            });

        getChartViewer().initialiseUI();

        // Prepare and initialise a Metadata viewer, with no Toolbar
        uiMetadata = new MetadataExplorerUIComponent(getSdaUI().getHostTask(),
                                                     getObservatoryInstrument(),
                                                     null,
                                                     REGISTRY.getFrameworkResourceKey(),
                                                     false);
        setMetadataViewer(uiMetadata);
        getMetadataViewer().initialiseUI();

        // Make sure we are notified if the Composite Metadata changes in any way
        if (uiMetadata.getTheLeafUI() != null)
            {
            uiMetadata.getTheLeafUI().addMetadataChangedListener(this);
            }

        // The ViewerContainer contains either the Chart viewer or the Metadata viewer
        // Start with the Composite Chart visible
        getViewerContainer().removeAll();
        getViewerContainer().add((Component) getChartViewer(), BorderLayout.CENTER);

        // Assemble the whole Viewer
        removeAll();
        add(toolbarViewer, BorderLayout.NORTH);
        add((Component) getViewerContainer(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "CompositeViewerUIComponent.runUI() ";

        super.runUI();

        switch (getSdaUI().getCompositeViewerMode())
            {
            case COMPOSITE_EXPORTER:
                {
                LOGGER.error(SOURCE + "Invalid CompositeViewerMode [mode=" + getSdaUI().getCompositeViewerMode().getName() + "]");

                break;
                }

            case COMPOSITE_VIEWER_CHART:
                {
                if (getChartViewer() != null)
                    {
                    getChartViewer().runUI();
                    }

                break;
                }

            case COMPOSITE_VIEWER_METADATA:
                {
                if (getMetadataViewer() != null)
                    {
                    getMetadataViewer().runUI();
                    }

                break;
                }

            default:
                {
                LOGGER.error(SOURCE + "Invalid Composite Viewer Display Mode");
                }
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        if (getChartViewer() != null)
            {
            getChartViewer().stopUI();
            }

        if (getMetadataViewer() != null)
            {
            getMetadataViewer().stopUI();
            }

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        if (getChartViewer() != null)
            {
            getChartViewer().disposeUI();
            }

        if (getMetadataViewer() != null)
            {
            if ((getMetadataViewer() instanceof MetadataExplorerUIComponentInterface)
                && (((MetadataExplorerUIComponentInterface) getMetadataViewer()).getTheLeafUI() != null))
                {
                ((MetadataExplorerUIComponentInterface) getMetadataViewer()).getTheLeafUI().removeMetadataChangedListener(this);
                }

            getMetadataViewer().disposeUI();
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the JLabel which indicates the number of Channels.
     *
     * @return JLabel
     */

    public JLabel getChannelsLabel()
        {
        return (this.labelChannels);
        }


    /***********************************************************************************************
     * Set the JLabel which indicates the number of Channels.
     *
     * @param label
     */

    public void setChannelsLabel(final JLabel label)
        {
        this.labelChannels = label;
        }


    /***********************************************************************************************
     * Get the Detach All button.
     *
     * @return JButton
     */

    public JButton getDetachAllButton()
        {
        return (this.buttonDetachAll);
        }


    /***********************************************************************************************
     * Set the Detach All button.
     *
     * @param button
     */

    public void setDetachAllButton(final JButton button)
        {
        this.buttonDetachAll = button;
        }


    /***********************************************************************************************
     * Get the Metadata button.
     *
     * @return JButton
     */

    public JButton getMetadataButton()
        {
        return (this.buttonMetadata);
        }


    /***********************************************************************************************
     * Set the Metadata button.
     *
     * @param button
     */

    public void setMetadataButton(final JButton button)
        {
        this.buttonMetadata = button;
        }


    /***********************************************************************************************
     * Get the Export button.
     *
     * @return JButton
     */

    public JButton getExportButton()
        {
        return (this.buttonExport);
        }


    /***********************************************************************************************
     * Set the Export button.
     *
     * @param button
     */

    public void setExportButton(final JButton button)
        {
        this.buttonExport = button;
        }


    /***********************************************************************************************
     * Get the PageSetup button.
     *
     * @return JButton
     */

    public JButton getPageSetupButton()
        {
        return (this.buttonPageSetup);
        }


    /***********************************************************************************************
     * Set the PageSetup button.
     *
     * @param button
     */

    public void setPageSetupButton(final JButton button)
        {
        this.buttonPageSetup = button;
        }


    /***********************************************************************************************
     * Get the Print button.
     *
     * @return JButton
     */

    public JButton getPrintButton()
        {
        return (this.buttonPrint);
        }


    /***********************************************************************************************
     * Set the Print button.
     *
     * @param button
     */

    public void setPrintButton(final JButton button)
        {
        this.buttonPrint = button;
        }


    /**********************************************************************************************/
    /* Viewers                                                                                    */
    /***********************************************************************************************
     * Get the Viewer UI container, which appears at the top of the host UI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getViewerContainer()
        {
        return (this.uiViewer);
        }


    /***********************************************************************************************
     * Get the Chart Viewer.
     *
     * @return ChartUIComponentPlugin
     */

    public ChartUIComponentPlugin getChartViewer()
        {
        return (this.chartViewer);
        }


    /***********************************************************************************************
     * Set the Chart Viewer.
     *
     * @param chart
     */

    private void setChartViewer(final ChartUIComponentPlugin chart)
        {
        this.chartViewer = chart;
        }


    /***********************************************************************************************
     * Get the Metadata Viewer, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getMetadataViewer()
        {
        return (this.metadataViewer);
        }


    /***********************************************************************************************
     * Set the Metadata Viewer, which appears at the bottom of the UI.
     *
     * @param mdviewer
     */

    private void setMetadataViewer(final UIComponentPlugin mdviewer)
        {
        this.metadataViewer = mdviewer;
        }


    /***********************************************************************************************
     * Indicate if all of the Viewer UI Components are not NULL.
     *
     * @return boolean
     */

    public boolean isValidViewerUI()
        {
        return ((getDetachAllButton() != null)
                && (getMetadataButton() != null)
                && (getExportButton() != null)
                && (getPageSetupButton() != null)
                && (getPrintButton() != null));
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /**********************************************************************************************
     * Indicate that the Metadata has changed.
     *
     * @param event
     */

    public void metadataChanged(final MetadataChangedEvent event)
        {
        final String SOURCE = "CompositeViewerUIComponent.metadataChanged() ";
        final boolean boolDebug;

        boolDebug = LOADER_PROPERTIES.isMetadataDebug()
                    || LOADER_PROPERTIES.isChartDebug();

        if (event != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "[key=" + event.getMetadataKey()
                         + "] [value=" + event.getMetadataValue()
                         + "] [state=" + event.getItemState().getName()
                         + "]");
            }

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

        if ((getSdaUI() != null)
            && (getSdaUI().getCompositeDAO() != null))
            {
            final List<Metadata> listAggregateCompositeDAOMetadata;

            // NOTE THAT The DAO data take precedence over those in the Wrapper
            listAggregateCompositeDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(null,
                                                                                              null,
                                                                                              null,
                                                                                              getSdaUI().getCompositeDAO(),
                                                                                              null,
                                                                                              SOURCE,
                                                                                              boolDebug);
            getSdaUI().getCompositeDAO().setMetadataChanged(true);

            // Refresh the Composite Chart if possible
            if (getChartViewer() != null)
                {
                // The Metadata came from the CompositeDAO, so don't re-apply
                getChartViewer().setMetadata(listAggregateCompositeDAOMetadata,
                                             getSdaUI().getCompositeDAO(),
                                             false,
                                             boolDebug);

                // Refresh on a separate thread!
                getChartViewer().refreshChart(getSdaUI().getCompositeDAO(),
                                              true,
                                              SOURCE);
                }

            // Update the Composite MetadataExplorer
            if ((getMetadataViewer() != null)
                && (getMetadataViewer() instanceof MetadataExplorerUIComponentInterface))
                {
                // This resets the selection to the root
                ((MetadataExplorerUIComponentInterface) getMetadataViewer()).setMetadataList(listAggregateCompositeDAOMetadata);
                }
            }
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getObservatoryInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Get the host SuperposedDataAnalyser UIComponent.
     *
     * @return SuperposedDataAnalyserUIComponentInterface
     */

    public SuperposedDataAnalyserUIComponentInterface getSdaUI()
        {
        return (this.uiSDA);
        }


    /***********************************************************************************************
     * Get the FontData.
     *
     * @return FontPlugin
     */

    public FontInterface getFontData()
        {
        return (this.fontData);
        }


    /***********************************************************************************************
     * Get the ColourData.
     *
     * @return ColourPlugin
     */

    public ColourInterface getColourData()
        {
        return (this.colourData);
        }
    }
