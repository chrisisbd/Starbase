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

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChannelSelectorUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.LogLinChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTCanvasInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTFrameUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * AveragingFFTCanvas.
 */

public class AveragingFFTCanvas extends UIComponent
                                implements AveragingFFTCanvasInterface
    {
    private static final long serialVersionUID = 9097650896589892518L;

    // Injections
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final AveragingFFTFrameUIComponentInterface hostFrameUI;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColourForeground;
    private final ColourInterface pluginColourBackground;
    private final String strResourceKey;

    // UI
    private ChartUIComponentPlugin chartViewer;


    /***********************************************************************************************
     * AveragingFFTCanvas.
     *
     * @param hostui
     * @param hostinstrument
     * @param hostframeui
     * @param font
     * @param colourforeground
     * @param colourbackground
     * @param resourcekey
     * @param debug
     */

    public AveragingFFTCanvas(final ObservatoryUIInterface hostui,
                              final ObservatoryInstrumentInterface hostinstrument,
                              final AveragingFFTFrameUIComponentInterface hostframeui,
                              final FontInterface font,
                              final ColourInterface colourforeground,
                              final ColourInterface colourbackground,
                              final String resourcekey,
                              final boolean debug)
        {
        super();

        // Injections
        this.observatoryUI = hostui;
        this.hostInstrument = hostinstrument;
        this.hostFrameUI = hostframeui;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;
        this.strResourceKey = resourcekey;
        setDebug(debug);

        // UI
        this.chartViewer = null;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final Border compoundBorder;

        super.initialiseUI();
        removeAll();

        setBackground(getBackgroundColour().getColor());
        setOpaque(true);

        // Make sure that the Canvas uses all available space...
        setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        // Add a Border to the Canvas panel
        compoundBorder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                                            BorderFactory.createLoweredBevelBorder());
        setBorder(compoundBorder);

        // There is only ever one Chart, a special version with no Toolbar
        setChartViewer(new LogLinChartUIComponent(getHostFrameUI().getHostTask(),
                                                  getHostInstrument(),
                                                  "FFT",
                                                  null,
                                                  REGISTRY.getFrameworkResourceKey(),
                                                  DataUpdateType.PRESERVE,
                                                  REGISTRY.getIntegerProperty(getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX),
                                                  -1000.0,
                                                  1000.0,
                                                  -1000.0,
                                                  1000.0)
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
                    getChannelSelectorOccupant().setDecimating(false);
                    getChannelSelectorOccupant().setLegend(false);
                    getChannelSelectorOccupant().setShowChannels(false);
                    getChannelSelectorOccupant().debugSelector(LOADER_PROPERTIES.isChartDebug(), SOURCE);
                    }
                }
            });

        getChartViewer().initialiseUI();

        add((Component) getChartViewer(), BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        if (getChartViewer() != null)
            {
            getChartViewer().runUI();
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

        super.disposeUI();
        }


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
        final String SOURCE = "AveragingFFTCanvas.setWrappedData() ";

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        LOGGER.debug(isDebug(),
                     SOURCE + "Pass data to Chart");

        if (getChartViewer() !=  null)
            {
            System.out.println("WRAPPED ON CHART (EXCEPT WRAPPED IS NOT USED!!");

            getChartViewer().setPrimaryXYDataset(daowrapper.getWrappedDAO(),
                                                 daowrapper.getXYDataset());
            System.out.println("AFTER SET PRIMARY ***********************************");
            getChartViewer().refreshChart(daowrapper.getWrappedDAO(),
                                          true,
                                          SOURCE);
            }
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
     * Get the Printable Component of the Canvas.
     *
     * @return Component
     */

    public Component getPrintableComponent()
        {
        // ToDo Print Chart
        return (null);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /**********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    private ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    /**********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /**********************************************************************************************
     * Get the Host Frame UI.
     *
     * @return AveragingFFTFrameUIComponentInterface
     */

    private AveragingFFTFrameUIComponentInterface getHostFrameUI()
        {
        return (this.hostFrameUI);
        }


    /**********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /**********************************************************************************************
     * Get the Foreground Colour.
     *
     * @return ColourInterface
     */

    private ColourInterface getForegroundColour()
        {
        return (this.pluginColourForeground);
        }


    /**********************************************************************************************
     * Get the Background Colour.
     *
     * @return ColourInterface
     */

    private ColourInterface getBackgroundColour()
        {
        return (this.pluginColourBackground);
        }


    /**********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }


