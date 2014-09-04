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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.*;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * SuperposedDataAnalyserUIComponent.
 */

public final class SuperposedDataAnalyserUIComponent extends InstrumentUIComponentDecorator
                                                     implements SuperposedDataAnalyserUIComponentInterface
    {
    private DatasetManagerInterface datasetManager;
    private final ObservatoryInstrumentDAOInterface daoComposite;

    // Top panel - may contain the Composite Viewer or Exporter
    private final JPanel panelTop;
    private UIComponentPlugin uiComponentTop;
    private CompositeViewerUIComponentInterface uiCompositeViewer;
    private ExecuteCommandUIComponentInterface uiCompositeExporter;
    private SuperposedDataAnalyserDisplayMode sdaCompositeViewerMode;

    // Bottom Panel: may contain Imported Dataset or Import Command
    private final JPanel panelBottom;
    private UIComponentPlugin uiComponentBottom;
    private DatasetViewerUIComponentInterface uiDatasetViewer;
    private ExecuteCommandUIComponentInterface uiDatasetImporter;
    private SuperposedDataAnalyserDisplayMode sdaDatasetViewerMode;


    /***********************************************************************************************
     * Construct a SuperposedDataAnalyserUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public SuperposedDataAnalyserUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                             final Instrument instrumentxml,
                                             final ObservatoryUIInterface hostui,
                                             final TaskPlugin task,
                                             final FontInterface font,
                                             final ColourInterface colour,
                                             final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey,
              0);

        this.datasetManager = null;

        // Instantiate a new DAO (just once) to hold the data for the composite Chart
        this.daoComposite = DAOHelper.instantiateDAO(getHostInstrument(),
                                                     SuperposedDataAnalyserUIComponentInterface.CLASSNAME_DAO_COMPOSITE);
        this.panelTop = new JPanel();
        this.uiComponentTop = null;
        this.uiCompositeViewer = null;
        this.uiCompositeExporter = null;
        this.sdaCompositeViewerMode = SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART;

        this.panelBottom = new JPanel();
        this.uiComponentBottom = null;
        this.uiDatasetViewer = null;
        this.uiDatasetImporter = null;
        this.sdaDatasetViewerMode = SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART;
        }


    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final String SOURCE = "SuperposedDataAnalyserUIComponent.initialiseUI() ";
        final JSplitPane splitPane;

        setDatasetManager(new DatasetManager());
        getDatasetManager().initialise();

        if (getCompositeDAO() != null)
            {
            final boolean boolSuccess;

            boolSuccess = getCompositeDAO().initialiseDAO(getHostInstrument().getResourceKey());

            if (!boolSuccess)
                {
                LOGGER.error(SOURCE + "Unable to initialise the Composite DAO");
                }
            }

        // DO NOT USE super.initialiseUI()
        removeAll();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(false);
        splitPane.setDividerSize(DIVIDER_SIZE);

        // Setting the location explicitly seems to be the best I can do to reveal the panel at startup...
        splitPane.setDividerLocation(DIVIDER_LOCATION);
        splitPane.setResizeWeight(RESIZE_WEIGHT);

        // The host UIComponent uses BorderLayout
        add(splitPane, BorderLayout.CENTER);

        //-----------------------------------------------------------------------------------------
        // Top Panel

        getTopPanel().setLayout(new BorderLayout());
        getTopPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        splitPane.setTopComponent(getTopPanel());

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "Creating a new Composite Viewer and initialising");

        setCompositeViewerMode(SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART);

        // Create the two Composite UIs only once
        setCompositeViewer(new CompositeViewerUIComponent(getHostInstrument(),
                                                          this,
                                                          getFontData(),
                                                          getColourData()));
        getCompositeViewer().initialiseUI();

        setCompositeExporter(new CompositeExporterUIComponent(getHostInstrument(),
                                                              this,
                                                              getFontData(),
                                                              getColourData()));
        getCompositeExporter().initialiseUI();

        // Initialise using the SuperposedDataAnalyserDisplayMode
        SuperposedDataAnalyserHelper.switchCompositeViewerDisplayMode(this, getCompositeViewerMode());

        //-----------------------------------------------------------------------------------------
        // Bottom Panel

        getBottomPanel().setMinimumSize(new Dimension(Integer.MAX_VALUE, BOTTOM_MINIMUM_HEIGHT));
        getBottomPanel().setLayout(new BorderLayout());
        getBottomPanel().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        splitPane.setBottomComponent(getBottomPanel());

        setDatasetViewerMode(SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);

        // Create the two Dataset UIs only once
        setDatasetViewer(new DatasetViewerUIComponent(getHostInstrument(),
                                                      this,
                                                      getFontData(),
                                                      getColourData()));
        getDatasetViewer().initialiseUI();

        setDatasetImporter(new DatasetImporterUIComponent(getHostInstrument(),
                                                          this,
                                                          getFontData(),
                                                          getColourData()));
        getDatasetImporter().initialiseUI();

        // Initialise using the SuperposedDataAnalyserDisplayMode
        SuperposedDataAnalyserHelper.switchDatasetViewerDisplayMode(this, getDatasetViewerMode());
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        if (getTopUIComponent() != null)
            {
            getTopUIComponent().runUI();
            }

        if (getBottomUIComponent() != null)
            {
            getBottomUIComponent().runUI();
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        if (getTopUIComponent() != null)
            {
            getTopUIComponent().stopUI();
            }

        if (getBottomUIComponent() != null)
            {
            getBottomUIComponent().stopUI();
            }

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of this UIComponent.
     */

    public void disposeUI()
        {
        getDatasetManager().dispose();

        setCompositeViewerMode(SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART);
        setDatasetViewerMode(SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);

        if (getTopUIComponent() != null)
            {
            getTopUIComponent().disposeUI();
            }

        if (getBottomUIComponent() != null)
            {
            getBottomUIComponent().disposeUI();
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Reset the ObservatoryInstrumentUIComponent.
     *
     * @param resetmode
     */

    public void reset(final ResetMode resetmode)
        {
        super.reset(resetmode);

        stopUI();

        getDatasetManager().dispose();
        getDatasetManager().initialise();

        setCompositeViewerMode(SuperposedDataAnalyserDisplayMode.COMPOSITE_VIEWER_CHART);
        // This calls refreshChart()
        SuperposedDataAnalyserHelper.switchCompositeViewerDisplayMode(this, getCompositeViewerMode());

        setDatasetViewerMode(SuperposedDataAnalyserDisplayMode.DATASET_VIEWER_CHART);
        // This calls refreshChart()
        SuperposedDataAnalyserHelper.switchDatasetViewerDisplayMode(this, getDatasetViewerMode());
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatedata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "SuperposedDataAnalyserUIComponent.setWrappedData() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug());

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        LOGGER.debug(boolDebug,
                     SOURCE + "Set Wrapped Data on SuperposedDataAnalyserUIComponent (not used locally)");

        if (getDatasetViewer() != null)
            {
            LOGGER.debug(boolDebug,
                         SOURCE + "Pass data to SuperposedDataAnalyserUIComponent:DatasetViewer");

            getDatasetViewer().setWrappedData(daowrapper,
                                              updatedata,
                                              updatemetadata);
            }
        }


    /***********************************************************************************************
     * Get the DatasetManager, which handles the state of all imported Datasets.
     *
     * @return DatasetManagerInterface
     */

    public DatasetManagerInterface getDatasetManager()
        {
        return (this.datasetManager);
        }


    /***********************************************************************************************
     * Set the DatasetManager, which handles the state of all imported Datasets.
     *
     * @param manager
     */

    private void setDatasetManager(final DatasetManagerInterface manager)
        {
        this.datasetManager = manager;
        }


    /***********************************************************************************************
     * Get the Composite DAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public ObservatoryInstrumentDAOInterface getCompositeDAO()
        {
        return (this.daoComposite);
        }


    /**********************************************************************************************/
    /* Top Panel                                                                                  */
    /***********************************************************************************************
     * Get the Top Panel.
     *
     * @return JPanel
     */

    public JPanel getTopPanel()
        {
        return (this.panelTop);
        }


    /***********************************************************************************************
     * Get the UIComponent, which appears at the top of the UI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getTopUIComponent()
        {
        return (this.uiComponentTop);
        }


    /***********************************************************************************************
     * Set the UIComponent, which appears at the top of the UI.
     *
     * @param uicomponent
     */

    public void setTopUIComponent(final UIComponentPlugin uicomponent)
        {
        this.uiComponentTop = uicomponent;
        }


    /***********************************************************************************************
     * Get the Composite Viewer, which appears at the top of the UI.
     *
     * @return CompositeViewerUIComponentInterface
     */

    public CompositeViewerUIComponentInterface getCompositeViewer()
        {
        return (this.uiCompositeViewer);
        }


    /***********************************************************************************************
     * Set the Composite Viewer, which appears at the top of the UI.
     *
     * @param viewer
     */

    private void setCompositeViewer(final CompositeViewerUIComponentInterface viewer)
        {
        this.uiCompositeViewer = viewer;
        }


    /***********************************************************************************************
     * Get the CompositeExporter, which appears at the top of the UI.
     *
     * @return ExecuteCommandUIComponentInterface
     */

    public ExecuteCommandUIComponentInterface getCompositeExporter()
        {
        return (this.uiCompositeExporter);
        }


    /***********************************************************************************************
     * Set the CompositeExporter, which appears at the top of the UI.
     *
     * @param uicomponent
     */

    private void setCompositeExporter(final ExecuteCommandUIComponentInterface uicomponent)
        {
        this.uiCompositeExporter = uicomponent;
        }


    /***********************************************************************************************
     * Get the SDA CompositeViewer Display Mode.
     *
     * @return SuperposedDataAnalyserDisplayMode
     */

    public SuperposedDataAnalyserDisplayMode getCompositeViewerMode()
        {
        return (this.sdaCompositeViewerMode);
        }


    /***********************************************************************************************
     * Set the SDA CompositeViewer Display Mode.
     *
     * @param mode
     */

    public void setCompositeViewerMode(final SuperposedDataAnalyserDisplayMode mode)
        {
        this.sdaCompositeViewerMode = mode;
        }


    /**********************************************************************************************/
    /* Bottom Panel                                                                               */
    /***********************************************************************************************
     * Get the Bottom Panel.
     *
     * @return JPanel
     */

    public JPanel getBottomPanel()
        {
        return (this.panelBottom);
        }


    /***********************************************************************************************
     * Get the UIComponent, which appears at the bottom of the UI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getBottomUIComponent()
        {
        return (this.uiComponentBottom);
        }


    /***********************************************************************************************
     * Set the UIComponent, which appears at the bottom of the UI.
     *
     * @param uicomponent
     */

    public void setBottomUIComponent(final UIComponentPlugin uicomponent)
        {
        this.uiComponentBottom = uicomponent;
        }


    /***********************************************************************************************
     * Get the DatasetViewer, which appears at the bottom of the UI.
     *
     * @return DatasetViewerUIComponentInterface
     */

    public DatasetViewerUIComponentInterface getDatasetViewer()
        {
        return (this.uiDatasetViewer);
        }


    /***********************************************************************************************
     * Set the DatasetViewer, which appears at the bottom of the UI.
     *
     * @param uicomponent
     */

    private void setDatasetViewer(final DatasetViewerUIComponentInterface uicomponent)
        {
        this.uiDatasetViewer = uicomponent;
        }


    /***********************************************************************************************
     * Get the DatasetImporter, which appears at the bottom of the UI.
     *
     * @return ExecuteCommandUIComponentInterface
     */

    public ExecuteCommandUIComponentInterface getDatasetImporter()
        {
        return (this.uiDatasetImporter);
        }


    /***********************************************************************************************
     * Set the DatasetImporter, which appears at the bottom of the UI.
     *
     * @param uicomponent
     */

    private void setDatasetImporter(final ExecuteCommandUIComponentInterface uicomponent)
        {
        this.uiDatasetImporter = uicomponent;
        }


    /***********************************************************************************************
     * Get the SDA DatasetViewer Display Mode.
     *
     * @return SuperposedDataAnalyserDisplayMode
     */

    public SuperposedDataAnalyserDisplayMode getDatasetViewerMode()
        {
        return (this.sdaDatasetViewerMode);
        }


    /***********************************************************************************************
     * Set the SDA DatasetViewer Display Mode.
     *
     * @param mode
     */

    public void setDatasetViewerMode(final SuperposedDataAnalyserDisplayMode mode)
        {
        this.sdaDatasetViewerMode = mode;
        }
    }
