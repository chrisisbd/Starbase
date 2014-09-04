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


import info.clearthought.layout.TableLayout;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AveragingFFTUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTCanvasInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTSidebarInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;


/**********************************************************************************************
 * AveragingFFTUIComponent.
 */

public final class AveragingFFTUIComponent extends UIComponent
                                           implements AveragingFFTUIComponentInterface
    {
    private static final long serialVersionUID = -8129858443213421033L;

    // Injections
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final AveragingFFTFrameUIComponentInterface hostFrameUI;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColourForeground;
    private final ColourInterface pluginColourBackground;
    private final String strResourceKey;

    // User Interface
    private final JPanel panelUIContainer;
    private AveragingFFTCanvasInterface fftCanvas;
    private AveragingFFTSidebarInterface fftSidebar;

    // TableLayout row and column size definitions
    private static final double[][] size =
        {
            { // Columns
              TableLayout.FILL,         // Canvas
              TableLayout.PREFERRED     // Sidebar
            },
            { // Rows
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
          "0, 0, CENTER, CENTER",    // Canvas
          "1, 0, CENTER, TOP"        // Sidebar
        };


    /**********************************************************************************************
     * Construct an AveragingFFTUIComponent.
     *
     * @param hostui
     * @param hostinstrument
     * @param hostframe
     * @param font
     * @param colourforeground
     * @param colourbackground
     * @param resourcekey
     * @param debug
     */

    public AveragingFFTUIComponent(final ObservatoryUIInterface hostui,
                                   final ObservatoryInstrumentInterface hostinstrument,
                                   final AveragingFFTFrameUIComponentInterface hostframe,
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
        this.hostFrameUI = hostframe;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;
        this.strResourceKey = resourcekey;
        setDebug(debug);

        // Make the UI container panel only once
        this.panelUIContainer = new JPanel();
        this.fftCanvas = null;
        this.fftSidebar = null;
        }


    /***********************************************************************************************
     /* UI State                                                                                  */
    /**********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final String SOURCE = "AveragingFFTUIComponent.initialiseUI() ";
        final JScrollPane scrollPaneSidebar;

        LOGGER.debug(isDebug(), SOURCE);

        super.initialiseUI();

        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

        getUIContainer().setLayout(new TableLayout(size));
        getUIContainer().setBackground(getBackgroundColour().getColor());

        // Create the AveragingFFTCanvas
        setCanvas(new AveragingFFTCanvas(getObservatoryUI(),
                                         getHostInstrument(),
                                         getHostFrameUI(),
                                         getFontData(),
                                         getForegroundColour(),
                                         getBackgroundColour(),
                                         getResourceKey(),
                                         isDebug()));
        getCanvas().initialiseUI();

        // Create the Sidebar
        setSidebar(new AveragingFFTSidebar(getObservatoryUI(),
                                           getHostInstrument(),
                                           getHostFrameUI(),
                                           getFontData(),
                                           getForegroundColour(),
                                           getBackgroundColour(),
                                           getResourceKey(),
                                           isDebug()));
        getSidebar().initialiseUI();

        scrollPaneSidebar = new JScrollPane();
        scrollPaneSidebar.setBackground(getBackgroundColour().getColor());
        scrollPaneSidebar.setViewportView((Component) getSidebar());
        scrollPaneSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneSidebar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneSidebar.setWheelScrollingEnabled(true);
        scrollPaneSidebar.setBorder(BorderFactory.createEmptyBorder());

        //------------------------------------------------------------------------------------------
        // Put the visible components together

        getUIContainer().add((Component)getCanvas(), constraints[0]);
        getUIContainer().add(scrollPaneSidebar, constraints[1]);

        // Consume all area on the underlying UIComponent
        // The host UIComponent uses BorderLayout
        add(getUIContainer(), BorderLayout.CENTER);
        }


    /**********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        final String SOURCE = "AveragingFFTUIComponent.runUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        super.runUI();

        if (getCanvas() != null)
            {
            getCanvas().runUI();
            }

        if (getSidebar() != null)
            {
            getSidebar().runUI();
            }
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        final String SOURCE = "AveragingFFTUIComponent.stopUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        if (getCanvas() != null)
            {
            getCanvas().stopUI();
            }

        if (getSidebar() != null)
            {
            getSidebar().stopUI();
            }

        super.stopUI();
        }


    /**********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        final String SOURCE = "AveragingFFTUIComponent.disposeUI() ";

        LOGGER.debug(isDebug(), SOURCE);

        if (getCanvas() != null)
            {
            getCanvas().disposeUI();
            setCanvas(null);
            }

        if (getSidebar() != null)
            {
            getSidebar().disposeUI();
            setSidebar(null);
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
        final String SOURCE = "AveragingFFTUIComponent.setWrappedData() ";

        // This is the simplest way!
        if (daowrapper == null)
            {
            return;
            }

        LOGGER.debug(isDebug(),
                     SOURCE + "Set Wrapped Data on AveragingFFTUIComponent (not used locally)");

        if (getCanvas() != null)
            {
            LOGGER.debug(isDebug(),
                         SOURCE + "Pass data to AveragingFFTUIComponent:AveragingFFTCanvas");

            getCanvas().setWrappedData(daowrapper,
                                       updatedata,
                                       updatemetadata);
            }
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the JPanel holding the complete UI of the AveragingFFT Tab.
     *
     * @return JPanel
     */

    private JPanel getUIContainer()
        {
        return (this.panelUIContainer);
        }


    /***********************************************************************************************
     * Get the Canvas.
     *
     * @return AveragingFFTCanvasInterface
     */

    public AveragingFFTCanvasInterface getCanvas()
        {
        return (this.fftCanvas);
        }


    /***********************************************************************************************
     * Set the Canvas.
     *
     * @param canvas
     */

    private void setCanvas(final AveragingFFTCanvasInterface canvas)
        {
        this.fftCanvas = canvas;
        }


    /***********************************************************************************************
     * Get the Sidebar.
     *
     * @return AveragingFFTSidebarInterface
     */

    public AveragingFFTSidebarInterface getSidebar()
        {
        return (this.fftSidebar);
        }


    /***********************************************************************************************
     * Set the Sidebar.
     *
     * @param sidebar
     */

    private void setSidebar(final AveragingFFTSidebarInterface sidebar)
        {
        this.fftSidebar = sidebar;
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
     * @return ColourPlugin
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