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

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTToolbarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTUIHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * AveragingFFTToolbar.
 */

public class AveragingFFTToolbar extends JToolBar
                                 implements AveragingFFTToolbarInterface
    {
    private static final long serialVersionUID = -6296357045363331368L;

    // Injections
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final AveragingFFTFrameUIComponentInterface hostFrameUI;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColourForeground;
    private final ColourInterface pluginColourBackground;
    private final String strResourceKey;
    private final boolean boolDebugMode;

    // Toolbar
    private final JButton buttonOpenFile;
    private final JButton buttonPlayPause;
    private final JButton buttonStop;
    private JProgressBar progressBar;

    private final JButton buttonMetadata;
    private final JButton buttonExport;
    private final JButton buttonPageSetup;
    private final JButton buttonPrint;
    private final JButton buttonHelp;


    /***********************************************************************************************
     * AveragingFFTToolbar.
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

    public AveragingFFTToolbar(final ObservatoryUIInterface hostui,
                               final ObservatoryInstrumentInterface hostinstrument,
                               final AveragingFFTFrameUIComponentInterface hostframe,
                               final FontInterface font,
                               final ColourInterface colourforeground,
                               final ColourInterface colourbackground,
                               final String resourcekey,
                               final boolean debug)
        {
        // Injections
        this.observatoryUI = hostui;
        this.hostInstrument = hostinstrument;
        this.hostFrameUI = hostframe;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;
        this.strResourceKey = resourcekey;
        this.boolDebugMode = debug;

        // Toolbar
        this.buttonOpenFile = new JButton();
        this.buttonPlayPause = new JButton();
        this.buttonStop = new JButton();
        this.progressBar = null;

        this.buttonMetadata = new JButton();
        this.buttonExport = new JButton();
        this.buttonPageSetup = new JButton();
        this.buttonPrint = new JButton();
        this.buttonHelp = new JButton();
        }


    /***********************************************************************************************
     * initialise.
     */

    public void initialise()
        {
        final List<Component> listComponents;

        listComponents = AveragingFFTUIHelper.createToolbarComponents(getObservatoryUI(),
                                                                      getHostInstrument(),
                                                                      getHostFrameUI(),
                                                                      this,
                                                                      getFontData(),
                                                                      getForegroundColour(),
                                                                      getBackgroundColour(),
                                                                      isDebug());
        UIComponentHelper.buildToolbar(this, listComponents);

        setFloatable(false);
        setMinimumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setPreferredSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setMaximumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        setBackground(getBackgroundColour().getColor());
        }


    /***********************************************************************************************
     * Removes all the components from this container.
     * This method also notifies the layout manager to remove the
     * components from this container's layout via the
     * <code>removeLayoutComponent</code> method.
     */

    public void removeAll()
        {
        super.removeAll();
        }


    /***********************************************************************************************
     * Get the Title to be displayed on the Toolbar.
     *
     * @return String
     */

    public String getTitle()
        {
        return (TOOLBAR_TITLE);
        }


    /**********************************************************************************************/
    /* Toolbar Buttons                                                                            */
    /***********************************************************************************************
     * Get the OpenFile button.
     *
     * @return JButton
     */

    public JButton getOpenFileButton()
        {
        return (this.buttonOpenFile);
        }


    /***********************************************************************************************
     * Get the PlayPause button.
     *
     * @return JButton
     */

    public JButton getPlayPauseButton()
        {
        return (this.buttonPlayPause);
        }


    /***********************************************************************************************
     * Get the Stop button.
     *
     * @return JButton
     */

    public JButton getStopButton()
        {
        return (this.buttonStop);
        }


    /***********************************************************************************************
     * Get the ProgressBar component.
     *
     * @return JProgressBar
     */

    public JProgressBar getProgressBar()
        {
        return (this.progressBar);
        }


    /***********************************************************************************************
     * Set the ProgressBar component.
     *
     * @param bar
     */

    public void setProgressBar(final JProgressBar bar)
        {
        this.progressBar = bar;
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
     * Get the Export button.
     *
     * @return JButton
     */

    public JButton getExportButton()
        {
        return (this.buttonExport);
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
     * Get the Print button.
     *
     * @return JButton
     */

    public JButton getPrintButton()
        {
        return (this.buttonPrint);
        }


    /***********************************************************************************************
     * Get the Help button.
     *
     * @return JButton
     */

    public JButton getHelpButton()
        {
        return (this.buttonHelp);
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


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    private boolean isDebug()
        {
        return (this.boolDebugMode);
        }
    }
