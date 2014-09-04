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

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.misc.Semaphore;
import org.lmn.fc.common.utilities.ui.ListCellDatasetState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOWrapper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.LogLinChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataExplorerUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.widgets.ControlKnobInterface;
import org.lmn.fc.ui.widgets.IndicatorInterface;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommanderToolbarHelper.TITLE_DIALOG_COMMAND_EXECUTION;


/***************************************************************************************************
 * DatasetViewerUIComponent.
 */

public final class DatasetViewerUIComponent extends UIComponent
                                            implements DatasetViewerUIComponentInterface
    {
    private static final long serialVersionUID = 2785101518792814066L;

    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final SuperposedDataAnalyserUIComponentInterface uiSDA;
    private final FontInterface fontData;
    private final ColourInterface colourData;

    // UI Components for Imported Dataset
    private JComboBox comboSelectDataset;
    private final Semaphore semaphoreDataset;
    private IndicatorInterface indicatorOffset;
    private ControlKnobInterface controlOffset;
    private JButton buttonAttach;
    private JButton buttonDetach;
    private JButton buttonMetadata;
    private JButton buttonRemove;
    private JButton buttonImport;
    private JLabel labelDatasetType;

    private final UIComponentPlugin uiViewer;
    private ChartUIComponentPlugin chartViewer;
    private UIComponentPlugin metadataViewer;


    /***********************************************************************************************
     * Construct a DatasetViewerUIComponent.
     *
     * @param obsinstrument
     * @param sdaui
     * @param fontdata
     * @param colourdata
     */

    public DatasetViewerUIComponent(final ObservatoryInstrumentInterface obsinstrument,
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
        this.comboSelectDataset = null;
        this.semaphoreDataset = new Semaphore(false);
        this.indicatorOffset = null;
        this.controlOffset = null;
        this.labelDatasetType = null;
        this.buttonAttach = null;
        this.buttonDetach = null;
        this.buttonMetadata = null;
        this.buttonRemove = null;
        this.buttonImport = null;

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

        getDatasetSemaphore().setState(SuperposedDataAnalyserHelper.BLOCK_EVENTS);

        // Create the Viewer JToolBar
        listComponents = SuperposedDataAnalyserHelper.createDatasetToolbarComponents(getObservatoryInstrument(),
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

        // There is only ever one Chart for the DatasetViewer
        // A special version for use in e.g. the Superposed Data Analyser, with no Toolbar
        setChartViewer(new LogLinChartUIComponent(getSdaUI().getHostTask(),
                                                  getObservatoryInstrument(),
                                                  SuperposedDataAnalyserUIComponentInterface.TITLE_DATASET,
                                                  null,
                                                  REGISTRY.getFrameworkResourceKey(),
                                                  DataUpdateType.DECIMATE,
                                                  REGISTRY.getIntegerProperty(getObservatoryInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX),
                                                  -SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE,
                                                  SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE,
                                                  -SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE,
                                                  SuperposedDataAnalyserUIComponentInterface.DEFAULT_COMPOSITE_RANGE)
            {
            final static long serialVersionUID = -2433194350569140263L;


            /**********************************************************************************
             * Customise the XYPlot of a new chart, e.g. for fixed range axes.
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
                final JFreeChart jFreeChart;

                jFreeChart = super.createCustomisedChart(datasettype,
                                                         primarydataset,
                                                         secondarydatasets,
                                                         updatetype,
                                                         displaylimit,
                                                         channelselector,
                                                         debug);
                // Remove all labels
                jFreeChart.setTitle(EMPTY_STRING);

                if ((jFreeChart.getXYPlot() != null)
                    && (jFreeChart.getXYPlot().getDomainAxis() != null))
                    {
                    jFreeChart.getXYPlot().getDomainAxis().setLabel(EMPTY_STRING);
                    }

                if ((jFreeChart.getXYPlot() != null)
                    && (jFreeChart.getXYPlot().getRangeAxis() != null))
                    {
                    jFreeChart.getXYPlot().getRangeAxis().setLabel(EMPTY_STRING);
                    }

                return (jFreeChart);
                }


            /**********************************************************************************
             * Initialise the Chart.
             */

            public synchronized void initialiseUI()
                {
                final String SOURCE = "ChartUIComponent.initialiseUI() ";

                setChannelSelector(false);
                setDatasetDomainControl(false);

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
                    getChannelSelectorOccupant().setShowChannels(false);
                    getChannelSelectorOccupant().debugSelector(LOADER_PROPERTIES.isChartDebug(), SOURCE);
                    }
                }
            });

        getChartViewer().initialiseUI();

        // Now add the Viewer Chart as a ChangeListener
        getOffsetControl().addChangeListener(getSdaUI().getDatasetViewer().getChartViewer());

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
        // Start with the Chart visible
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
        super.runUI();
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
        getDatasetSemaphore().setState(SuperposedDataAnalyserHelper.BLOCK_EVENTS);

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


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param forcerefreshdata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean forcerefreshdata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "DatasetViewerUIComponent.setWrappedData() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        LOGGER.debug(boolDebug,
                     SOURCE + "Set Wrapped Data on DatasetViewerUIComponent");

        // NOTE - do not accept anything from the host Instrument's DAO!
        // only from the DatasetImporter, so InstrumentDAO != DatasetImporterDAO
        // and double check by ensuring the incoming DAO is marked as NOT supplying the Instrument with data
        if ((getObservatoryInstrument().getDAO() != null)
            && (!getObservatoryInstrument().getDAO().getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
            && (daowrapper != null)
            && (daowrapper.getWrappedDAO() != null)
            && (!daowrapper.getWrappedDAO().getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
            && (!getObservatoryInstrument().getDAO().equals(daowrapper.getWrappedDAO()))
            && (!daowrapper.getWrappedDAO().isInstrumentDataConsumer())
            && isValidViewerUI())
            {
            final DatasetType datasetType;

            LOGGER.debug(boolDebug,
                         SOURCE + "[response.status="
                         + ResponseMessageStatus.expandResponseStatusCodes(getObservatoryInstrument().getDAO().getResponseMessageStatusList())
                         + "]");

            DAOWrapper.debugDAOWrapper(daowrapper,
                                       daowrapper.getWrappedDAO(),
                                       "On arrival at DatasetViewerUIComponent.setWrappedData() (vs. Wrapped DAO)",
                                       boolDebug);

            // Identify the DatasetType of the Imported data...
            if (DataAnalyser.isCalendarisedRawData(daowrapper.getWrappedDAO().getRawData()))
                {
                datasetType = DatasetType.TIMESTAMPED;
                }
            else if (DataAnalyser.isColumnarRawData(daowrapper.getWrappedDAO().getRawData()))
                {
                datasetType = DatasetType.XY;
                }
            else
                {
                datasetType = null;
                }

            // Is the format recognisable?
            if (datasetType != null)
                {
                // Update the DatasetViewer UI to show details of the import, using data from the importer DAO,
                // and if not the first, check that it is consistent with the required type in the DatasetManager
                if ((!getSdaUI().getDatasetManager().hasSelection())
                    || ((getSdaUI().getDatasetManager().hasSelection())
                        && (getSdaUI().getDatasetManager().getDatasetType().equals(datasetType))))
                    {
                    // Is this the first Import?
                    if (!getSdaUI().getDatasetManager().hasSelection())
                        {
                        // Clear the drop-down if we are the first Import
                        getDatasetSelector().removeAllItems();
                        }

                    // Import the dataset into the DatasetManager
                    // Leave with the SelectedIndex at the imported dataset
                    getSdaUI().getDatasetManager().importDataset(getObservatoryInstrument(),
                                                                 datasetType,
                                                                 daowrapper.getWrappedDAO());
                    // Stop dataset selections for a while
                    if (getDatasetSemaphore() != null)
                        {
                        getDatasetSemaphore().setState(SuperposedDataAnalyserHelper.BLOCK_EVENTS);
                        }
                    getDatasetSelector().setEnabled(false);

                    // Add the new import at the end, and update the selection
                    // Prepare the ListCellDatasetState for later changes of state
                    getDatasetSelector().addItem(new ListCellDatasetState(getSdaUI().getDatasetManager().getSelectedName(),
                                                                          getSdaUI().getDatasetManager().getSelectedState()));

                    // Simple assertion that we are still in step
                    if (getSdaUI().getDatasetManager().getSelectedIndex() != (getDatasetSelector().getItemCount() - 1))
                        {
                        LOGGER.error(SOURCE + "[datasetmanager.index=" + getSdaUI().getDatasetManager().getSelectedIndex()
                                     + "] [datasetselector.index=" + (getDatasetSelector().getItemCount() - 1) + "]");
                        }

                    // Always select the last item, i.e. the most recent import
                    getDatasetSelector().setSelectedIndex(getSdaUI().getDatasetManager().getSelectedIndex());

                    // Imports always start with an offset of zero
                    getOffsetIndicator().setValue(SuperposedDataAnalyserHelper.PATTERN_OFFSET.format(getSdaUI().getDatasetManager().getSelectedOffset()));

                    // Offset may now be adjusted again, but not for the Primary
                    getOffsetControl().setEnabled(getSdaUI().getDatasetManager().canAdjustOffset());

                    // BEWARE setValue() will fire a ChangeEvent,
                    // which will use the *current* SecondaryDatasetIndex,
                    // so do this LAST
                    getOffsetControl().setValue(getSdaUI().getDatasetManager().getSelectedOffset());

                    // A new import is not attached yet
                    getAttachButton().setEnabled(getSdaUI().getDatasetManager().canAttach());

                    // A new import is not attached, so can't detach either
                    getDetachButton().setEnabled(getSdaUI().getDatasetManager().canDetach());

                    // A new import has Metadata, which may be viewed
                    getMetadataButton().setEnabled(true);

                    // A Primary Dataset cannot be removed directly, it must be Detached first
                    getRemoveButton().setEnabled(getSdaUI().getDatasetManager().canRemove());

                    // Show the required DatasetType
                    getDatasetTypeLabel().setText(datasetType.getDescription() + datasetType.getName());

                    // Show the imported Chart and Metadata using the Import DAO
                    // This does NOT call refreshChart()
                    SuperposedDataAnalyserHelper.showSelectedXYDatasetOnDatasetViewer(getSdaUI().getDatasetManager(),
                                                                                      this,
                                                                                      false,
                                                                                      boolDebug);

                    // Switch immediately to the DatasetViewer for the Chart (or not)
                    // This calls refreshChart()
                    SuperposedDataAnalyserHelper.switchDatasetViewerDisplayMode(getSdaUI(), SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);

                    // Disable the Abort button on the Importer, since the Import is now complete
                    if ((getSdaUI() != null)
                        && (getSdaUI().getDatasetImporter() != null)
                        && (getSdaUI().getDatasetImporter().getExecutionContext() != null)
                        && (getSdaUI().getDatasetImporter().getExecutionContext().getAbortButton() != null)
                            )
                        {
                        getSdaUI().getDatasetImporter().getExecutionContext().getAbortButton().setEnabled(false);
                        }

                    // Allow selections to work again
                    if (getDatasetSemaphore() != null)
                        {
                        getDatasetSemaphore().setState(SuperposedDataAnalyserHelper.ALLOW_EVENTS);
                        }
                    getDatasetSelector().setEnabled(true);
                    }
                else
                    {
                    final String strMessage;

                    strMessage = "This import is not of the required DatasetType"
                                 + "  [type.required=" + getSdaUI().getDatasetManager().getDatasetType().getName()
                                 + "] [type.imported=" + datasetType.getName() + "]";

                    JOptionPane.showMessageDialog(null,
                                                  strMessage,
                                                  getSdaUI().getHostInstrument().getInstrument().getIdentifier()
                                                  + TITLE_DIALOG_COMMAND_EXECUTION,
                                                  JOptionPane.ERROR_MESSAGE);
                    LOGGER.error(SOURCE + strMessage);
                    }
                }
            else
                {
                // Fail silently if this is likely to be the first time through?
                if (!((daowrapper.getWrappedDAO().getRawData() == null)
                      || (daowrapper.getWrappedDAO().getRawDataChannelCount() == 0)
                      || (daowrapper.getWrappedDAO().getRawData().get(0) == null)
                      || (!(daowrapper.getWrappedDAO().getRawData().get(0) instanceof Vector))))
                    {
                    final String strMessage;

                    strMessage = "Unable to identify the format of the imported data";

                    JOptionPane.showMessageDialog(null,
                                                  strMessage,
                                                  getSdaUI().getHostInstrument().getInstrument().getIdentifier()
                                                  + TITLE_DIALOG_COMMAND_EXECUTION,
                                                  JOptionPane.ERROR_MESSAGE);
                    LOGGER.error(SOURCE + strMessage);
                    }
                }
            }
        else
            {
            if (boolDebug)
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "Unable to Set Wrapped Data on DatasetViewerUIComponent");

                if (getObservatoryInstrument().getDAO() != null)
                    {
                    LOGGER.debug(boolDebug,
                                 SOURCE + "[response.status.instrument="
                                 + ResponseMessageStatus.expandResponseStatusCodes(getObservatoryInstrument().getDAO().getResponseMessageStatusList())
                                 + "]");
                    }

                if ((daowrapper != null)
                    && (daowrapper.getWrappedDAO() != null))
                    {
                    LOGGER.debug(boolDebug,
                                 SOURCE + "[response.status.wrapper="
                                 + ResponseMessageStatus.expandResponseStatusCodes(daowrapper.getWrappedDAO().getResponseMessageStatusList())
                                 + "]");
                    }
                }
            }
        }


    /**********************************************************************************************/
    /* UI Components                                                                              */
    /***********************************************************************************************
     * Get the JComboBox used for dataset selection.
     *
     * @return JComboBox
     */

    public JComboBox getDatasetSelector()
        {
        return (this.comboSelectDataset);
        }


    /***********************************************************************************************
     * Set the JComboBox used for dataset selection.
     *
     * @param selector
     */

    public void setDatasetSelector(final JComboBox  selector)
        {
        this.comboSelectDataset = selector;
        }


    /***********************************************************************************************
     * Get the Semaphore used to control e.g. the ActionListener on the DatasetSelector.
     *
     * @return Semaphore
     */

    public Semaphore getDatasetSemaphore()
        {
        return (this.semaphoreDataset);
        }


    /***********************************************************************************************
     * Get the Offset Indicator.
     *
     * @return IndicatorInterface
     */

    public IndicatorInterface getOffsetIndicator()
        {
        return (this.indicatorOffset);
        }


    /***********************************************************************************************
     * Set the Offset Indicator.
     *
     * @param indicator
     */

    public void setOffsetIndicator(final IndicatorInterface indicator)
        {
        this.indicatorOffset = indicator;
        }


    /***********************************************************************************************
     * Get the Offset Control.
     *
     * @return ControlKnobInterface
     */

    public ControlKnobInterface getOffsetControl()
        {
        return (this.controlOffset);
        }


    /***********************************************************************************************
     * Set the Offset Control.
     *
     * @param knob
     */

    public void setOffsetControl(final ControlKnobInterface knob)
        {
        this.controlOffset = knob;
        }


    /***********************************************************************************************
     * Get the Attach button.
     *
     * @return JButton
     */

    public JButton getAttachButton()
        {
        return (this.buttonAttach);
        }


    /***********************************************************************************************
     * Set the Attach button.
     *
     * @param button
     */

    public void setAttachButton(final JButton button)
        {
        this.buttonAttach = button;
        }


    /***********************************************************************************************
     * Get the Detach button.
     *
     * @return JButton
     */

    public JButton getDetachButton()
        {
        return (this.buttonDetach);
        }


    /***********************************************************************************************
     * Set the Detach button.
     *
     * @param button
     */

    public void setDetachButton(final JButton button)
        {
        this.buttonDetach = button;
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
     * Get the Remove button.
     *
     * @return JButton
     */

    public JButton getRemoveButton()
        {
        return (this.buttonRemove);
        }


    /***********************************************************************************************
     * Set the Remove button.
     *
     * @param button
     */

    public void setRemoveButton(final JButton button)
        {
        this.buttonRemove = button;
        }


    /***********************************************************************************************
     * Get the Import button.
     *
     * @return JButton
     */

    public JButton getImportButton()
        {
        return (this.buttonImport);
        }


    /***********************************************************************************************
     * Set the Import button.
     *
     * @param button
     */

    public void setImportButton(final JButton button)
        {
        this.buttonImport = button;
        }


    /***********************************************************************************************
     * Get the JLabel which indicates the chosen DatasetType.
     *
     * @return JLabel
     */

    public JLabel getDatasetTypeLabel()
        {
        return (this.labelDatasetType);
        }


    /***********************************************************************************************
     * Set the JLabel which indicates the chosen DatasetType.
     *
     * @param label
     */

    public void setDatasetTypeLabel(final JLabel label)
        {
        this.labelDatasetType = label;
        }


    /***********************************************************************************************
     * Get the Viewer UI container, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getViewerContainer()
        {
        return (this.uiViewer);
        }


    /***********************************************************************************************
     * Get the Chart Viewer, which appears at the bottom of the UI.
     *
     * @return ChartUIComponentPlugin
     */

    public ChartUIComponentPlugin getChartViewer()
        {
        return (this.chartViewer);
        }


    /***********************************************************************************************
     * Set the Chart Viewer, which appears at the bottom of the UI.
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
        return ((getDatasetSelector() != null)
                && (getOffsetIndicator() != null)
                && (getOffsetControl() != null)
                && (getAttachButton() != null)
                && (getDetachButton() != null)
                && (getMetadataButton() != null)
                && (getRemoveButton() != null)
                && (getDatasetTypeLabel() != null)
                && (getImportButton() != null));
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /**********************************************************************************************
     * Indicate that a Change (of the ControlKnobs) should be committed.
     *
     * @param event
     */

    public void commitChange(final CommitChangeEvent event)
        {
        final String SOURCE = "DatasetViewerUIComponent.commitChange() ";
        final boolean boolDebug;

        boolDebug = LOADER_PROPERTIES.isChartDebug();

        LOGGER.debug(boolDebug,
                     SOURCE + "Start CommitChangeEvent");

        // Is there currently anything to detach?
        // If not, then there's no change to be committed
        if ((getSdaUI() != null)
            && (getSdaUI().getDatasetManager() != null)
            && (getSdaUI().getDatasetManager().canDetach()))
            {
            // Redraw the Composite, because something changed,
            // i.e. an offset knob
            // It isn't necessary to change channel selection states,
            // only the channel names on the selector
            //
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

            // Rebuild on THIS thread, refresh on a separate thread
            getSdaUI().getCompositeDAO().setMetadataChanged(true);
            DatasetManagerHelper.rebuildComposite(getSdaUI().getDatasetManager(),
                                                  getSdaUI(),
                                                  false, false, false, false, true, false, false,
                                                  event,
                                                  boolDebug);
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Nothing to commit, since no currently attached dataset");
            }
        }


    /**********************************************************************************************
     * Indicate that the Metadata has changed.
     *
     * @param event
     */

    public void metadataChanged(final MetadataChangedEvent event)
        {
        final String SOURCE = "DatasetViewerUIComponent.metadataChanged() ";
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

        if ((getChartViewer() != null)
            && (getSdaUI() != null)
            && (getSdaUI().getDatasetManager() != null))
            {
            // Redraw the current selected Chart - we are really only interested in the colours
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

            if (getSdaUI().getDatasetManager().hasSelection())
                {
                final ObservatoryInstrumentDAOInterface daoSelected;

                daoSelected = getSdaUI().getDatasetManager().getSelectedDAO();

                if (daoSelected != null)
                    {
                    final List<Metadata> listAggregateSelectedDAOMetadata;

                    // NOTE THAT The DAO data take precedence over those in the Wrapper
                    listAggregateSelectedDAOMetadata = MetadataHelper.collectAggregateMetadataTraced(null,
                                                                                                     null,
                                                                                                     null,
                                                                                                     daoSelected,
                                                                                                     null,
                                                                                                     SOURCE,
                                                                                                     boolDebug);
                    daoSelected.setMetadataChanged(true);

                    // Refresh the Dataset Chart if possible
                    if (getChartViewer() != null)
                        {
                        // The Metadata came from the SelectedDAO, so don't re-apply
                        getChartViewer().setMetadata(listAggregateSelectedDAOMetadata,
                                                     daoSelected,
                                                     false,
                                                     boolDebug);

                        // Refresh on a separate thread!
                        getChartViewer().refreshChart(daoSelected,
                                                      true,
                                                      SOURCE);
                        }

                    // Update the Dataset MetadataExplorer (not done by DatasetManager.rebuildComposite())
                    if ((getMetadataViewer() != null)
                        && (getMetadataViewer() instanceof MetadataExplorerUIComponentInterface))
                        {
                        // This resets the selection to the root
                        ((MetadataExplorerUIComponentInterface) getMetadataViewer()).setMetadataList(listAggregateSelectedDAOMetadata);
                        }

                    // Redraw the Composite also, because e.g. Channel.Name may have changed
                    // A brute force way to keep everything in step!
                    // Rebuild on THIS thread, refresh on a separate thread
                    DatasetManagerHelper.rebuildComposite(getSdaUI().getDatasetManager(),
                                                          getSdaUI(),
                                                          false, false, false, false, true, false, false,
                                                          event,
                                                          boolDebug);
                    }
                else
                    {
                    LOGGER.error(SOURCE + "DatasetManager DAO selection is NULL");
                    }
                }
            else
                {
                LOGGER.debug(boolDebug,
                             SOURCE + "No DatasetManager selection to which to apply metadata changes");
                }
            }
        else
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Unable to make metadata changes");
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
