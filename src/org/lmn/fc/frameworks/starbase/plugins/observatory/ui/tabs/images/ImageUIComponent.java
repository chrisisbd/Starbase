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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.images;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ImageUIComponentInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * ImageUIComponent.
 */

public class ImageUIComponent extends UIComponent
                              implements ImageUIComponentInterface
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private List<Metadata> listMetadata;
    private final String strResourceKey;

    // Underlying ImageData, passed in from a DAO
    private Image imageData;

    // The Image panel we must update
    private JComponent panelImage;


    /***********************************************************************************************
     * Construct a ImageUIComponent.
     *
     * @param hostinstrument
     * @param metadata
     * @param resourcekey
     */

    public ImageUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                            final List<Metadata> metadata,
                            final String resourcekey)
        {
        // UIComponent has a BorderLayout
        super();

        this.hostInstrument = hostinstrument;
        this.listMetadata = metadata;
        this.strResourceKey = resourcekey;
        this.imageData = null;
        this.panelImage = null;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        super.initialiseUI();

        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        setImagePanel(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT));
        add(getImagePanel());
        revalidate();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        refreshImage();
        }


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        final String SOURCE = "ImageUIComponent.removeUIIdentity() ";

        super.removeUIIdentity();

        // This must do the same as the dynamic parts of initialiseUI()
        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        setImagePanel(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT));
        add(getImagePanel());
        revalidate();
        repaint();

        LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                     SOURCE + "Cleared Image [uistate=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Get the List of Metadata for the Image.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata for this Image.
     *
     * @param metadata
     */

    public void setMetadata(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the Image to be shown on the ImageUIComponent.
     *
     * @return Image
     */

    public Image getImage()
        {
        return (this.imageData);
        }


    /***********************************************************************************************
     * Set the Image to be shown on the ImageUIComponent.
     *
     * @param image
     */

    public void setImage(final Image image)
        {
        this.imageData = image;
        }


    /***********************************************************************************************
     * Refresh the Image on a separate thread.
     */

    public void refreshImage()
        {
        final String SOURCE = "ImageUIComponent.refreshImage() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        // Only generate an Image if this UIComponent is visible
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "VISIBLE");

            if (getImage() != null)
                {
                final JScrollPane scrollPane;
                final JPanel panelRefreshedImage;
                final JLabel labelImage;

                labelImage = new JLabel(new ImageIcon(getImage()));
                panelRefreshedImage = new JPanel(new BorderLayout());
                panelRefreshedImage.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
                panelRefreshedImage.add(labelImage, BorderLayout.CENTER);
                scrollPane = new JScrollPane(panelRefreshedImage);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setMinimumSize(new Dimension(100, 100));
                scrollPane.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());

                setImagePanel(scrollPane);
                }
            else
                {
                // There's no data, so we mustn't have an Image either
                setImagePanel(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT));
                }
            }
        else
            {
            // Not visible, so we may or may not have some data and a Chart
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "NOT VISIBLE");

            setImagePanel(new BlankUIComponent(MSG_PLEASE_WAIT, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT));
            }

        // UIComponent has a BorderLayout
        removeAll();
        add(getImagePanel(), BorderLayout.CENTER);
        revalidate();
        }


    /***********************************************************************************************
     * Get the Image panel.
     *
     * @return JComponent
     */

    protected JComponent getImagePanel()
        {
        return (this.panelImage);
        }


    /***********************************************************************************************
     * Set the Image panel.
     *
     * @param panel
     */

    protected void setImagePanel(final JComponent panel)
        {
        this.panelImage = panel;
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    protected ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
