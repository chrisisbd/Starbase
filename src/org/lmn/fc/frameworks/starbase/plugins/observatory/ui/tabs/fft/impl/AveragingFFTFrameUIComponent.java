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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.impl;


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AveragingFFTUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata.MetadataExplorerUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * AveragingFFTFrameUIComponent.
 */

public class AveragingFFTFrameUIComponent extends InstrumentUIComponentDecorator
                                          implements AveragingFFTFrameUIComponentInterface
    {
    private static final long serialVersionUID = -5351217777261070511L;

    // Data
    private final AveragingFFTDAOInterface daoFFT;

    // User Interface
    private AveragingFFTToolbarInterface fftToolBar;
    private final UIComponentPlugin uiViewer;
    private AveragingFFTUIComponentInterface viewerFFT;
    private UIComponentPlugin viewerMetadata;
    private UIComponentPlugin viewerHelp;
    private AveragingFFTDisplayMode viewerMode;

    // Data
    private List<Metadata> listMetadata;


    /**********************************************************************************************
     * Construct an AveragingFFTFrameUIComponent.
     *
     * @param hostinstrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colourforeground
     * @param resourcekey
     */

    public AveragingFFTFrameUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                        final Instrument instrumentxml,
                                        final ObservatoryUIInterface hostui,
                                        final TaskPlugin task,
                                        final FontInterface font,
                                        final ColourInterface colourforeground,
                                        final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colourforeground,
              resourcekey,
              ControlPanelInterface.INDICATOR_COUNT_0);

        // Instantiate a new DAO (just once) to hold the data for the FFT
        // The host Instrument does NOT use the data produced by this DAO
        // i.e. isInstrumentDataConsumer() == false
        this.daoFFT = (AveragingFFTDAOInterface)DAOHelper.instantiateDAO(hostinstrument, CLASSNAME_DAO_FFT);

        this.fftToolBar = null;

        // Make the UI container panel only once
        this.uiViewer = new UIComponent();
        this.viewerFFT = null;
        this.viewerMetadata = null;
        this.viewerHelp = null;
        this.viewerMode = AveragingFFTDisplayMode.FFT_VIEWER_CHART;

        this.listMetadata = null;

        // ToDo REMOVE
        setDebug(true);
        }


    /***********************************************************************************************
     /* UI State                                                                                  */
    /**********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "AveragingFFTFrameUIComponent.initialiseUI() ";
        final MetadataExplorerUIComponentInterface uiMetadata;

        LOGGER.debug(isDebug(), SOURCE);

        // DO NOT super.initialiseUI();
        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        // Create the default set of Metadata to be used by this UI
        setMetadataList(AveragingFFTUIHelper.createAveragerMetadata(isDebug()));

        // Create the Toolbar
        setToolbar(new AveragingFFTToolbar(getObservatoryUI(),
                                           getHostInstrument(),
                                           this,
                                           getFontData(),
                                           getForegroundColour(),
                                           getBackgroundColour(),
                                           getResourceKey(),
                                           isDebug()));
        getToolbar().initialise();

        // The ViewerContainer contains either the Chart Viewer,
        // the Metadata Viewer or the Help Viewer
        // Start with the Chart Viewer visible
        setViewerMode(AveragingFFTDisplayMode.FFT_VIEWER_CHART);

        setFFTViewer(new AveragingFFTUIComponent(getObservatoryUI(),
                                                 getHostInstrument(),
                                                 this,
                                                 getFontData(),
                                                 getForegroundColour(),
                                                 getBackgroundColour(),
                                                 getResourceKey(),
                                                 isDebug()));
        getFFTViewer().initialiseUI();

        // Prepare and initialise a Metadata viewer, with no Toolbar
        uiMetadata = new MetadataExplorerUIComponent(getHostTask(),
                                                     getHostInstrument(),
                                                     getMetadataList(),
                                                     REGISTRY.getFrameworkResourceKey(),
                                                     isDebug());
        setMetadataViewer(uiMetadata);
        getMetadataViewer().initialiseUI();

        // Make sure we are notified if the Metadata changes in any way
        if (uiMetadata.getTheLeafUI() != null)
            {
            uiMetadata.getTheLeafUI().addMetadataChangedListener(this);
            }

        // ToDo Help Viewer





        // Associate the DAO with its Chart (on the FFT Viewer Canvas)
        if (getDAO() != null)
            {
            final boolean boolSuccess;

            boolSuccess = getDAO().initialiseDAO(getHostInstrument().getResourceKey());

            if ((boolSuccess)
                && (getFFTViewer().getCanvas() != null))
                {
                getFFTViewer().getCanvas().getChartViewer().setDAO(getDAO());
                }
            else
                {
                LOGGER.error(SOURCE + "Unable to initialise the DAO");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "DAO is NULL");
            }

        getViewerContainer().setBackground(getBackgroundColour().getColor());
        getViewerContainer().initialiseUI();
        getViewerContainer().add((Component)getFFTViewer(), BorderLayout.CENTER);

        // Assemble the whole Viewer
        removeAll();
        add((Component)getToolbar(), BorderLayout.NORTH);
        add((Component)getViewerContainer(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "AveragingFFTFrameUIComponent.runUI() ";

        super.runUI();

        switch (getViewerMode())
            {
            case FFT_VIEWER_CHART:
                {
                if (getFFTViewer() != null)
                    {
                    getFFTViewer().runUI();
                    }

                break;
                }

            case FFT_VIEWER_METADATA:
                {
                if (getMetadataViewer() != null)
                    {
                    getMetadataViewer().runUI();
                    }

                break;
                }

            case FFT_VIEWER_HELP:
                {
                if (getHelpViewer() != null)
                    {
                    getHelpViewer().runUI();
                    }

                break;
                }
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        // Stop everything regardless of Mode

        if (getFFTViewer() != null)
            {
            getFFTViewer().stopUI();
            }

        if (getMetadataViewer() != null)
            {
            getMetadataViewer().stopUI();
            }

        if (getHelpViewer() != null)
            {
            getHelpViewer().stopUI();
            }

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        // Dispose everything regardless of Mode

        if (getFFTViewer() != null)
            {
            getFFTViewer().disposeUI();
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

        if (getHelpViewer() != null)
            {
            getHelpViewer().disposeUI();
            }

        super.disposeUI();
        }


    /**********************************************************************************************/
    /* DAO                                                                                        */
    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of Data or Metadata.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatedata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "AveragingFFTFrameUIComponent.setWrappedData() ";

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        LOGGER.debug(isDebug(),
                     SOURCE + "Set Wrapped Data on AveragingFFTFrameUIComponent (not used locally)");

        // Send the data to the AveragingFFTUIComponent
        if (getFFTViewer() != null)
            {
            // Pass the DAO's data to the FFT Viewer
            getFFTViewer().setWrappedData(daowrapper,
                                          updatedata,
                                          updatemetadata);
            }
        }


    /***********************************************************************************************
     * Get the DAO.
     *
     * @return AveragingFFTDAOInterface
     */

    public AveragingFFTDAOInterface getDAO()
        {
        return (this.daoFFT);
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the ToolBar.
     *
     * @return AveragingFFTToolbarInterface
     */

    public AveragingFFTToolbarInterface getToolbar()
        {
        return (this.fftToolBar);
        }


    /***********************************************************************************************
     * Set the ToolBar.
     *
     * @param toolbar
     */

    private void setToolbar(final AveragingFFTToolbarInterface toolbar)
        {
        this.fftToolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the Viewer UI container.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getViewerContainer()
        {
        return (this.uiViewer);
        }


    /***********************************************************************************************
     * Get the FFT Viewer UI.
     *
     * @return AveragingFFTUIComponentInterface
     */

    public AveragingFFTUIComponentInterface getFFTViewer()
        {
        return (this.viewerFFT);
        }


    /***********************************************************************************************
     * Set the FFT Viewer UI.
     *
     * @param fftviewer
     */

    private void setFFTViewer(final AveragingFFTUIComponentInterface fftviewer)
        {
        this.viewerFFT = fftviewer;
        }


    /***********************************************************************************************
     * Get the Metadata Viewer.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getMetadataViewer()
        {
        return (this.viewerMetadata);
        }


    /***********************************************************************************************
     * Set the Metadata Viewer.
     *
     * @param mdviewer
     */

    private void setMetadataViewer(final UIComponentPlugin mdviewer)
        {
        this.viewerMetadata = mdviewer;
        }


    /***********************************************************************************************
     * Get the Help Viewer.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getHelpViewer()
        {
        return (this.viewerHelp);
        }


    /***********************************************************************************************
     * Set the Help Viewer.
     *
     * @param mdviewer
     */

    private void setHelpViewer(final UIComponentPlugin mdviewer)
        {
        this.viewerHelp = mdviewer;
        }


    /***********************************************************************************************
     * Get the FFT Viewer Display Mode.
     *
     * @return AveragingFFTDisplayMode
     */

    public AveragingFFTDisplayMode getViewerMode()
        {
        return (this.viewerMode);
        }


    /***********************************************************************************************
     * Set the FFT Viewer Display Mode.
     *
     * @param mode
     */

    public void setViewerMode(final AveragingFFTDisplayMode mode)
        {
        this.viewerMode = mode;
        }


    /***********************************************************************************************
     * Get the Metadata associated with this UIComponent.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadataList()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the Metadata associated with this UIComponent.
     *
     * @param metadatalist
     */

    private void setMetadataList(final List<Metadata> metadatalist)
        {
        this.listMetadata = metadatalist;
        }
    }
