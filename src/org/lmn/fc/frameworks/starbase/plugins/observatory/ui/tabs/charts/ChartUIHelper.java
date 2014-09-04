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

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportTableHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface.INDEX_LEFT;
import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.DatasetDomainUIComponentInterface.INDEX_RIGHT;


/***************************************************************************************************
 * ChartUIHelper.
 */

public final class ChartUIHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons
    {
    // Colours formatted so that the values can be pasted into Metadata in sample data files
    // ToDo Replace this with an Observatory-wide list loaded from XML files
    private static final Color[] arrayColours =
            {
                    // Red Green Blue
                    new Color(  0,   0, 255),
                    new Color(255,   0,   0),
                    new Color(7, 206, 19),
                    new Color(229, 227, 50),
                    new Color(255, 135, 147),
                    new Color(4, 195, 199),
                    new Color(221, 39, 201),
                    new Color(  0,   0, 128),
                    new Color(153, 204, 204),
                    new Color(255, 153, 204),
                    new Color(153,  51,   0),
                    new Color( 51,  51, 153),
                    new Color(255, 204, 153),
                    new Color(136,  43, 214),
                    new Color(255, 112,  51),
                    new Color(255, 204,  51),
                    new Color(  0,   0, 255),
                    new Color(255,   0,   0),
                    new Color(  0, 255,   0),
                    new Color(255, 255,   0),
                    new Color(255,   0, 255),
                    new Color(  0, 255, 255),
                    new Color(255, 128, 128),
                    new Color(  0,   0, 128),
                    new Color(153, 204, 204),
                    new Color(255, 153, 204),
                    new Color(153,  51,   0),
                    new Color( 51,  51, 153),
                    new Color(255, 204, 153),
                    new Color(136,  43, 214),
                    new Color(255, 112,  51),
                    new Color(255, 204,  51)
            };


    /***********************************************************************************************
     * Create the default Toolbar for a Chart.
     *
     * @param obsinstrument
     * @param chart
     * @param title
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     *
     * @return JToolBar
     */

    public static JToolBar createDefaultToolbar(final ObservatoryInstrumentInterface obsinstrument,
                                                final ChartUIComponentPlugin chart,
                                                final String title,
                                                final FontInterface fontdata,
                                                final ColourInterface colourforeground,
                                                final ColourInterface colourbackground,
                                                final boolean debug)
        {
        final JToolBar toolbar;

        if ((obsinstrument != null)
            && (chart != null))
            {
            final List<Component> listComponentsToAdd;
            final JLabel labelName;

            listComponentsToAdd = new ArrayList<Component>(10);

            //-------------------------------------------------------------------------------------
            // Initialise the Label

            labelName = new JLabel(title,
                                   RegistryModelUtilities.getAtomIcon(obsinstrument.getHostAtom(),
                                                                      ObservatoryInterface.FILENAME_ICON_CHART_VIEWER),
                                   SwingConstants.LEFT)
                {
                static final long serialVersionUID = 7580736117336162922L;

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
            labelName.setIconTextGap(UIComponentPlugin.TOOLBAR_ICON_TEXT_GAP);

            listComponentsToAdd.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON));
            listComponentsToAdd.add(labelName);
            listComponentsToAdd.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));

            listComponentsToAdd.add(Box.createHorizontalGlue());

            UIComponentHelper.addToolbarPrintButtons(listComponentsToAdd,
                                                     chart,
                                                     chart.getChartPanel(),
                                                     title,
                                                     fontdata,
                                                     colourforeground,
                                                     colourbackground,
                                                     debug);

            // Build the Toolbar using the Components, if any
            toolbar = UIComponentHelper.buildToolbar(listComponentsToAdd);
            }
        else
            {
            toolbar = new JToolBar();
            }

        toolbar.setFloatable(false);
        toolbar.setMinimumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        toolbar.setPreferredSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        toolbar.setMaximumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        toolbar.setBackground(colourbackground.getColor());
        NavigationUtilities.updateComponentTreeUI(toolbar);

        return (toolbar);
        }


    /***********************************************************************************************
     * A convenience method to use the same colours as the Charts.
     *
     * @param index
     *
     * @return ColourPlugin
     */

    public static ColourInterface getStandardColour(final int index)
        {
        if ((index >= 0)
            && (index < ObservatoryInterface.MAX_CHANNELS))
            {
            return (new ColourDataType(arrayColours[index & 0x0f]));
            }
        else
            {
            return (new ColourDataType(0, 0, 0));
            }
        }


    /***********************************************************************************************
     * Customise the Plot data renderer to improve legend visibility.
     *
     * @param plot
     * @param rendererindex
     */

    public static void customisePlotRenderer(final XYPlot plot,
                                             final int rendererindex)
        {
        if (plot != null)
            {
            final XYItemRenderer renderer;

            renderer = plot.getRenderer(rendererindex);

            if ((renderer != null)
                && (renderer instanceof XYLineAndShapeRenderer))
                {
                final XYLineAndShapeRenderer xyItemRenderer;

                xyItemRenderer = (XYLineAndShapeRenderer) renderer;

                xyItemRenderer.setBaseLinesVisible(true);
                xyItemRenderer.setBaseShapesVisible(false);
                xyItemRenderer.setBaseShapesFilled(true);
                xyItemRenderer.setItemLabelsVisible(true);

                // Set the shape for the Chart legend items
                setLegendShape(xyItemRenderer);
                }
            }
        }


    /***********************************************************************************************
     * Set the shape for the Chart legend items.
     *
     * @param renderer
     */

    static void setLegendShape(final XYLineAndShapeRenderer renderer)
        {
        final GeneralPath pathBlock;

        pathBlock = new GeneralPath();

        pathBlock.moveTo(-7.0f, -3.0f);
        pathBlock.lineTo(7.0f, -3.0f);

        pathBlock.moveTo(-7.0f, -2.0f);
        pathBlock.lineTo(7.0f, -2.0f);

        pathBlock.moveTo(-7.0f, -1.0f);
        pathBlock.lineTo(7.0f, -1.0f);

        pathBlock.moveTo(-7.0f, 0.0f);
        pathBlock.lineTo(7.0f, 0.0f);

        pathBlock.moveTo(-7.0f, 1.0f);
        pathBlock.lineTo(7.0f, 1.0f);

        pathBlock.moveTo(-7.0f, 2.0f);
        pathBlock.lineTo(7.0f, 2.0f);

        pathBlock.moveTo(-7.0f, 3.0f);
        pathBlock.lineTo(7.0f, 3.0f);

        renderer.setLegendLine(pathBlock);
        }


    /***********************************************************************************************
     * Update the Domain Slider Crosshair.
     *
     * @param chartui
     * @param movingindex
     * @param domainstart
     * @param domainend
     * @param movingslidervalue
     * @param sliderminimum
     * @param slidermaximum     *
     * @param debug
     *
     * @return double
     */

    public static double updateDomainCrosshairForDomainSlider(final ChartUIComponentPlugin chartui,
                                                              final int movingindex,
                                                              final int domainstart,
                                                              final int domainend,
                                                              final int movingslidervalue,
                                                              final int sliderminimum,
                                                              final int slidermaximum,
                                                              final boolean debug)
        {
        final String SOURCE = "ChartHelper.updateDomainCrosshairForDomainSlider() ";
        final double dblDomainCrosshairXYPlot;

        if ((chartui.getChartPanel() != null)
            && (chartui.getChartPanel().getChart() != null)
            && (chartui.getChartPanel().getChart().getPlot() != null))
            {
            final double dblDomainCrosshair;
            final double dblPositionFraction;
            final XYPlot plot;
            final ValueAxis domainAxis;
            final Range range;

            plot = (XYPlot) chartui.getChartPanel().getChart().getPlot();
            domainAxis = plot.getDomainAxis();
            range = domainAxis.getRange();

            if (movingindex == INDEX_LEFT)
                {
                // Trying to Move Left, but are we already at the Left Extent?
                if (movingslidervalue <= sliderminimum)
                    {
                    dblDomainCrosshair = sliderminimum;

                    dblDomainCrosshairXYPlot = domainAxis.getLowerBound();

                    FrameworkSingletons.LOGGER.debug(debug,
                                                     SOURCE + "At Left Extent, cannot move Left Thumb to the Left"
                                                     + "  [crosshair.domain=" + dblDomainCrosshair
                                                     + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                     + "]");
                    }
                // It is Ok to move further Left than the current setting of domainstart,
                // but we musn't draw a crosshair
                else if (movingslidervalue < domainstart)
                    {
                    // Do not draw a crosshair, set to lower bound?
                    dblDomainCrosshair = sliderminimum;

                    dblDomainCrosshairXYPlot = domainAxis.getLowerBound();

                    FrameworkSingletons.LOGGER.debug(debug,
                                                     SOURCE + "Moving Left Thumb to the Left beyond current DomainStart"
                                                     + "  [crosshair.domain=" + dblDomainCrosshair
                                                     + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                     + "]");
                    }
                else
                    {
                    // Try to Move Right, the slider won't be able to move past the Right Thumb
                    dblDomainCrosshair = movingslidervalue;

                    dblPositionFraction =  (double)(movingslidervalue - domainstart) / (double)(domainend - domainstart);
                    dblDomainCrosshairXYPlot = domainAxis.getLowerBound() + (dblPositionFraction * range.getLength());

                    FrameworkSingletons.LOGGER.debug(debug,
                                                     SOURCE + "Moving Left Thumb to the Right"
                                                     + "  [crosshair.domain=" + dblDomainCrosshair
                                                     + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                     + "]");
                    }
                }
            else if (movingindex == INDEX_RIGHT)
                {
                //  Trying to Move Right, but are we already at the Right Extent?
                if (movingslidervalue >= slidermaximum)
                    {
                    dblDomainCrosshair = slidermaximum;

                    dblDomainCrosshairXYPlot = domainAxis.getLowerBound() + range.getLength();

                    FrameworkSingletons.LOGGER.debug(debug,
                                                     SOURCE + "At Right Extent, cannot move Right Thumb to the Right"
                                                     + "  [crosshair.domain=" + dblDomainCrosshair
                                                     + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                     + "]");
                    }
                // It is Ok to move further Right than the current setting of domainend
                // but we musn't draw a crosshair
                else if (movingslidervalue > domainend)
                    {
                    // Do not draw a crosshair, set to upper bound?
                    dblDomainCrosshair = slidermaximum;

                    dblDomainCrosshairXYPlot = domainAxis.getLowerBound() + range.getLength();

                    FrameworkSingletons.LOGGER.debug(debug,
                                                     SOURCE + "Moving Right Thumb to the Right beyond current DomainEnd"
                                                     + "  [crosshair.domain=" + dblDomainCrosshair
                                                     + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                     + "]");
                    }
                else
                    {
                    // Try to Move Left, the slider won't be able to move past the Left Thumb
                    dblDomainCrosshair = movingslidervalue;

                    dblPositionFraction = (double)(movingslidervalue - domainstart) / (double)(domainend - domainstart);
                    dblDomainCrosshairXYPlot = domainAxis.getLowerBound() + (dblPositionFraction * range.getLength());

                    FrameworkSingletons.LOGGER.debug(debug,
                                                     SOURCE + "Moving Right Thumb to the Left"
                                                     + "  [crosshair.domain=" + dblDomainCrosshair
                                                     + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                     + "]");
                    }
                }
            else
                {
                // Do nothing, an Error
                dblDomainCrosshair = sliderminimum;
                dblDomainCrosshairXYPlot = domainAxis.getLowerBound();

                FrameworkSingletons.LOGGER.debug(debug,
                                                 SOURCE + "Invalid Thumb index"
                                                 + "  [crosshair.domain=" + dblDomainCrosshair
                                                 + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                                 + "]");
                }

            chartui.setDomainCrosshair(dblDomainCrosshair);
            plot.setDomainCrosshairValue(dblDomainCrosshairXYPlot);
            }
        else
            {
            chartui.setDomainCrosshair(sliderminimum);
            dblDomainCrosshairXYPlot = Double.MIN_VALUE;

            FrameworkSingletons.LOGGER.debug(debug,
                                             SOURCE + "There is no Chart, so cannot update Domain Slider Crosshair"
                                             + "  [crosshair.domain=" + sliderminimum
                                             + "] [crosshair.xyplot" + dblDomainCrosshairXYPlot
                                             + "]");
            }

        return (dblDomainCrosshairXYPlot);
        }


    /***********************************************************************************************
     * Calculate and update the Domain Offset Crosshair and draw the crosshair on the XYPlot.
     * Return the value set on the XYPlot, or Double.MIN_VALUE on failure.
     *
     * @param chartui
     * @param valueminimum
     * @param valuemaximum
     * @param offset
     * @param debug
     *
     * @return double
     */

    public static double updateDomainCrosshairForOffsetControl(final ChartUIComponentPlugin chartui,
                                                               final int valueminimum,
                                                               final int valuemaximum,
                                                               final double offset,
                                                               final boolean debug)
        {
        final String SOURCE = "ChartHelper.updateDomainCrosshairForOffsetControl() ";
        double dblDomainCrosshairXYPlot;

        dblDomainCrosshairXYPlot = Double.MIN_VALUE;

        if (chartui != null)
            {
            if ((chartui.getChartPanel() != null)
                && (chartui.getChartPanel().getChart() != null)
                && (chartui.getChartPanel().getChart().getPlot() != null))
                {
                final XYPlot plot;
                final ValueAxis domainAxis;
                final Range range;
                final double dblPositionFraction;

                //  Save the supplied new value
                chartui.setDomainCrosshair(offset);

                plot = (XYPlot) chartui.getChartPanel().getChart().getPlot();
                domainAxis = plot.getDomainAxis();
                range = domainAxis.getRange();

                if (offset >= 0)
                    {
                    //dblPositionFraction = (offset - (double)valueminimum) / (double)(valuemaximum - valueminimum);
                    dblPositionFraction = (offset * 2.0) / (double)(valuemaximum - valueminimum);
                    }
                else
                    {
                    dblPositionFraction = 0.0;
                    }

                dblDomainCrosshairXYPlot = domainAxis.getLowerBound() + (dblPositionFraction * range.getLength());

                // Update the XYPlot
                plot.setDomainCrosshairValue(dblDomainCrosshairXYPlot);

                FrameworkSingletons.LOGGER.debug(debug,
                                                 SOURCE + "Domain Crosshair updated [value.knob=" + offset
                                                 + "] [value.xyplot=" + dblDomainCrosshairXYPlot
                                                 + "] [domain.lowerbound=" + domainAxis.getLowerBound()
                                                 + "] [domain.upperbound=" + domainAxis.getUpperBound()
                                                 + "] [value.fraction=" + dblPositionFraction
                                                 + "] [value.minimum=" + valueminimum
                                                 + "] [value.maximum=" + valuemaximum
                                                 + "]");
                }
            else
                {
                FrameworkSingletons.LOGGER.debug(debug,
                                                 SOURCE + "There is no Chart, so cannot update Domain Crosshair on XYPlot");

                // Save a default value
                chartui.setDomainCrosshair(valueminimum);

                dblDomainCrosshairXYPlot = Double.MIN_VALUE;
                }
            }

        return (dblDomainCrosshairXYPlot);
        }


    /***********************************************************************************************
     * Draw the Chart Crosshair values on the XYPlot if possible.
     * Used by refreshChart().
     * This should be followed by fireChartChanged() when all updates are complete.
     *
     * @param chartui
     * @param debug
     */

    public static void drawChartCrosshairsOnXYPlot(final ChartUIComponentPlugin chartui,
                                                   final boolean debug)
        {
        final String SOURCE = "ChartHelper.drawChartCrosshairsOnXYPlot() ";

        // RangeCrosshair
        if ((chartui != null)
            && (chartui.getRangeCrosshair() > ChartUIComponentPlugin.RANGE_MIN)
            && (chartui.getChartPanel() != null)
            && (chartui.getChartPanel().getChart() != null)
            && (chartui.getChartPanel().getChart().getXYPlot() != null))
            {
            final XYPlot plot;
            final ValueAxis rangeAxis;
            final Range range;
            final double dblPositionFraction;
            final double dblRangeCrosshairXYPlot;

            plot = (XYPlot) chartui.getChartPanel().getChart().getPlot();
            rangeAxis = plot.getRangeAxis();
            range = rangeAxis.getRange();

            dblPositionFraction = (chartui.getRangeCrosshair() - ChartUIComponentPlugin.RANGE_MIN) / (double)(ChartUIComponentPlugin.RANGE_MAX - ChartUIComponentPlugin.RANGE_MIN);
            dblRangeCrosshairXYPlot = rangeAxis.getLowerBound() + (dblPositionFraction * range.getLength());

            FrameworkSingletons.LOGGER.debug(debug,
                                             SOURCE + "Draw Range Crosshair on XYPlot [crosshair.range=" + chartui.getRangeCrosshair()
                                             + "] [crosshair.xyplot=" + dblRangeCrosshairXYPlot
                                             + "] [range.min=" + ChartUIComponentPlugin.RANGE_MIN
                                             + "] [range.max=" + ChartUIComponentPlugin.RANGE_MAX
                                             + "]");

            plot.setRangeCrosshairValue(dblRangeCrosshairXYPlot);
            }

        // DomainCrosshair
        if ((chartui != null)
            && (chartui.getDomainCrosshair() > ChartUIComponentPlugin.DOMAIN_MIN)
            && (chartui.getChartPanel() != null)
            && (chartui.getChartPanel().getChart() != null)
            && (chartui.getChartPanel().getChart().getXYPlot() != null))
            {
            final XYPlot plot;
            final ValueAxis domainAxis;
            final Range range;
            final double dblPositionFraction;
            final double dblDomainCrosshairXYPlot;

            plot = (XYPlot) chartui.getChartPanel().getChart().getPlot();
            domainAxis = plot.getDomainAxis();
            range = domainAxis.getRange();

            // ToDo WARNING! Needs to be the same as for the slider??
            dblPositionFraction = (chartui.getDomainCrosshair() - ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MINIMUM) / (double)(ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MAXIMUM - ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MINIMUM);
            //            dblPositionFraction = (chartui.getDomainCrosshair() - ChartUIComponentPlugin.DOMAIN_MIN) / (double)(ChartUIComponentPlugin.DOMAIN_MAX - ChartUIComponentPlugin.DOMAIN_MIN);
            dblDomainCrosshairXYPlot = domainAxis.getLowerBound() + (dblPositionFraction * range.getLength());

            FrameworkSingletons.LOGGER.debug(debug,
                                             SOURCE + "Draw Domain Crosshair on XYPlot [crosshair.domain=" + chartui.getDomainCrosshair()
                                             + "] [crosshair.xyplot=" + dblDomainCrosshairXYPlot
                                             + "] [domain.min=" + ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MINIMUM
                                             + "] [domain.max=" + ChartUIComponentPlugin.OFFSET_CONTROL_COARSE_MAXIMUM
                                             + "]");

            //            LOGGER.debug(debug,
            //                         SOURCE + "Draw Domain Crosshair on XYPlot [crosshair.domain=" + chartui.getDomainCrosshair()
            //                                + "] [crosshair.xyplot=" + dblDomainCrosshairXYPlot
            //                                + "] [domain.min=" + ChartUIComponentPlugin.DOMAIN_MIN
            //                                + "] [domain.max=" + ChartUIComponentPlugin.DOMAIN_MAX
            //                                + "]");
            //
            plot.setDomainCrosshairValue(dblDomainCrosshairXYPlot);
            }
        }
    }
