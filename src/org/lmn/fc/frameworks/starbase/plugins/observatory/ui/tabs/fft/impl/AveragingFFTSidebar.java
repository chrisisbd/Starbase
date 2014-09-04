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

import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedListener;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AveragingFFTUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTSidebarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTUIHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * AveragingFFTSidebar.
 */

public class AveragingFFTSidebar extends UIComponent
                                 implements AveragingFFTSidebarInterface

    {
    private static final long serialVersionUID = 4934064289412503090L;

    // Injections
    private final ObservatoryUIInterface observatoryUI;
    private final ObservatoryInstrumentInterface hostInstrument;
    private final AveragingFFTFrameUIComponentInterface hostFrameUI;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColourForeground;
    private final ColourInterface pluginColourBackground;
    private final String strResourceKey;

    // User Interface
    private JComponent componentIQ;

    // Events
    private final Vector<MetadataChangedListener> vecMetadataChangedListeners;


    /***********************************************************************************************
     * AveragingFFTSidebar.
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

    public AveragingFFTSidebar(final ObservatoryUIInterface hostui,
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

        // User Interface
        this.componentIQ = null;

        // Events
        this.vecMetadataChangedListeners = new Vector<MetadataChangedListener>(10);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        final JPanel panelFFTLength;
        final JPanel panelWindowingFunction;
        final JPanel panelDisplayMode;
        final JPanel panelLogLin;
        final JPanel panelAdjustIQ;
        final JPanel panelSidebar;

        super.initialiseUI();
        removeAll();

        // All initial default values are taken from the Metadata keys
        panelFFTLength = AveragingFFTUIHelper.createFFTLengthSelector(this,
                                                                      AveragingFFTUIComponentInterface.KEY_FFT_LENGTH,
                                                                      getFontData(),
                                                                      getForegroundColour(),
                                                                      getBackgroundColour());

        panelWindowingFunction = AveragingFFTUIHelper.createWindowingFunctionSelector(this,
                                                                                      AveragingFFTUIComponentInterface.KEY_WINDOW,
                                                                                      getFontData(),
                                                                                      getForegroundColour(),
                                                                                      getBackgroundColour());

        panelDisplayMode = AveragingFFTUIHelper.createDisplayModeSelector(this,
                                                                          AveragingFFTUIComponentInterface.KEY_MODE_DISPLAY,
                                                                          getFontData(),
                                                                          getForegroundColour(),
                                                                          getBackgroundColour());

        panelLogLin = AveragingFFTUIHelper.createLinLogSelector(this,
                                                                AveragingFFTUIComponentInterface.KEY_MODE_PLOT,
                                                                getFontData(),
                                                                getForegroundColour(),
                                                                getBackgroundColour());

        panelAdjustIQ = AveragingFFTUIHelper.createIQAdjuster(this,
                                                              AveragingFFTUIComponentInterface.KEY_OFFSET_AMPLITUDE,
                                                              AveragingFFTUIComponentInterface.KEY_OFFSET_PHASE,
                                                              AveragingFFTUIComponentInterface.KEY_MODE_IQ,
                                                              getFontData(),
                                                              getForegroundColour(),
                                                              getBackgroundColour());

        //-------------------------------------------------------------------------------------
        // Put it all together
        // In general, all the components controlled by a top-to-bottom BoxLayout object should have the same X alignment.
        // Similarly, all the components controlled by a left-to-right Boxlayout should generally have the same Y alignment.

        panelSidebar = new JPanel();
        panelSidebar.setLayout(new BoxLayoutFixed(panelSidebar, BoxLayoutFixed.Y_AXIS));
        panelSidebar.setBorder(UIComponentPlugin.BORDER_SIDEBAR);
        panelSidebar.setBackground(getBackgroundColour().getColor());

        panelSidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_MARGIN));
        panelSidebar.add(panelFFTLength);
        panelSidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

        panelSidebar.add(panelWindowingFunction);
        panelSidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

        panelSidebar.add(panelDisplayMode);
        panelSidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

        panelSidebar.add(panelLogLin);
        panelSidebar.add(Box.createVerticalStrut(SIDEBAR_HEIGHT_SEPARATOR));

        panelSidebar.add(panelAdjustIQ);
        panelSidebar.add(Box.createVerticalGlue());

        // Consume all area on the underlying UIComponent
        // The host UIComponent uses BorderLayout
        add(panelSidebar, BorderLayout.CENTER);

        // Set up to inform the DAO if the Sidebar changes any parameters
        addMetadataChangedListener(getHostFrameUI().getDAO());
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        super.disposeUI();

        removeMetadataChangedListener(getHostFrameUI().getDAO());
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the IQPlot component.
     *
     * @return JComponent
     */

    public JComponent getIQPlot()
        {
        return (this.componentIQ);
        }


    /***********************************************************************************************
     * Set the IQPlot component.
     *
     * @param plot
     */

    public void setIQPlot(final JComponent plot)
        {
        this.componentIQ = plot;
        }


    /***********************************************************************************************
     * Get the Metadata associated with this UIComponent.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadataList()
        {
        return (getHostFrameUI().getMetadataList());
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /**********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
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
     * @return FontInterface
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


    /******************************************************************************************/
    /* Events                                                                                 */
    /***********************************************************************************************
     * Notify all listeners of MetadataChangedEvents.
     *
     * @param eventsource
     * @param metadatakey
     * @param metadatavalue
     * @param state
     */

    public final void notifyMetadataChangedEvent(final Object eventsource,
                                                 final String metadatakey,
                                                 final String metadatavalue,
                                                 final MetadataItemState state)
        {
        final String SOURCE = "AveragingFFTSidebar.notifyMetadataChangedEvent() ";
        List<MetadataChangedListener> listeners;
        final MetadataChangedEvent changeEvent;

        // Create a Thread-safe List of Listeners
        listeners = new CopyOnWriteArrayList<MetadataChangedListener>(getMetadataChangedListeners());

        // Create an MetadataChangedEvent
        changeEvent = new MetadataChangedEvent(eventsource, metadatakey, metadatavalue, state);

        // Fire the event to every listener
        synchronized(listeners)
            {
            for (int i = 0; i < listeners.size(); i++)
                {
                final MetadataChangedListener changeListener;

                changeListener = listeners.get(i);
                changeListener.metadataChanged(changeEvent);
                }
            }

        // Help the GC?
        listeners = null;
        }


    /***********************************************************************************************
     * Get the MetadataChanged Listeners (mostly for testing).
     *
     * @return Vector<MetadataChangedListener>
     */

    public final Vector<MetadataChangedListener> getMetadataChangedListeners()
        {
        return (this.vecMetadataChangedListeners);
        }


    /***********************************************************************************************
     * Add a listener for this event, uniquely.
     *
     * @param listener
     */

    public final void addMetadataChangedListener(final MetadataChangedListener listener)
        {
        final String SOURCE = "AveragingFFTSidebar.addMetadataChangedListener() ";

        if ((listener != null)
            && (getMetadataChangedListeners() != null)
            && (!getMetadataChangedListeners().contains(listener)))
            {
            getMetadataChangedListeners().addElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[count=" + getMetadataChangedListeners().size()
                         + "] [class=" + listener.getClass().getName() + "]");
            }
        }


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    public final void removeMetadataChangedListener(final MetadataChangedListener listener)
        {
        final String SOURCE = "AveragingFFTSidebar.removeMetadataChangedListener() ";

        if ((listener != null)
            && (getMetadataChangedListeners() != null))
            {
            getMetadataChangedListeners().removeElement(listener);
            LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                         SOURCE + "[count=" + getMetadataChangedListeners().size()
                         + "] [class=" + listener.getClass().getName() + "]");
            }
        }
    }
