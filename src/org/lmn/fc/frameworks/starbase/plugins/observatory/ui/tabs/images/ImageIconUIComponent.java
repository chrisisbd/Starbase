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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ImageIconUIComponentInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * ImageIconUIComponent.
 */

public class ImageIconUIComponent extends UIComponent
                                  implements ImageIconUIComponentInterface
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private ImageIcon imageIcon;
    private List<Metadata> listMetadata;
    private final String strResourceKey;

    // The Image panel we must update
    private JComponent panelImage;


    /***********************************************************************************************
     * Create the ImagePanel given an ImageIcon.
     *
     * @param imageicon
     *
     * @return JComponent
     */

    private static JComponent createImagePanel(final ImageIcon imageicon)
        {
        final JScrollPane scrollPane;
        final JPanel panelRefreshedImage;
        final JLabel labelImage;

        labelImage = new JLabel(imageicon);

        panelRefreshedImage = new JPanel(new BorderLayout());
        panelRefreshedImage.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        panelRefreshedImage.add(labelImage, BorderLayout.CENTER);

        scrollPane = new JScrollPane(panelRefreshedImage);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(100, 100));
        scrollPane.setBackground(DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        return (scrollPane);
        }


    /***********************************************************************************************
     * Construct an ImageIconUIComponent.
     *
     * @param hostinstrument
     * @param imageicon
     * @param metadata
     * @param resourcekey
     */

    public ImageIconUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                final ImageIcon imageicon,
                                final List<Metadata> metadata,
                                final String resourcekey)
        {
        // UIComponent has a BorderLayout
        super();

        // Injections
        this.hostInstrument = hostinstrument;
        this.imageIcon = imageicon;
        this.listMetadata = metadata;
        this.strResourceKey = resourcekey;

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
        setImagePanel(createImagePanel(getImageIcon()));
        add(getImagePanel());
        revalidate();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        super.runUI();

        refreshImageIcon();
        }


    /***********************************************************************************************
     * Refresh the ImageIcon on a separate thread.
     */

    public void refreshImageIcon()
        {
        final String SOURCE = "ImageIconUIComponent.refreshImageIcon() ";

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        // Only generate an Image if this UIComponent is visible
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "VISIBLE");

            if (getImageIcon() != null)
                {
                setImagePanel(createImagePanel(getImageIcon()));
                }
            else
                {
                // There's no data, so we mustn't have an Image either
                setImagePanel(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT));
                }
            }
        else
            {
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
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    public void removeUIIdentity()
        {
        final String SOURCE = "ImageIconUIComponent.removeUIIdentity() ";

        super.removeUIIdentity();

        // This must do the same as the dynamic parts of initialiseUI()
        removeAll();
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        setImagePanel(new BlankUIComponent(MSG_WAITING_FOR_DATA, DEFAULT_COLOUR_CANVAS, COLOUR_INFO_TEXT));
        add(getImagePanel());
        revalidate();
        repaint();
        }


    /***********************************************************************************************
     * Get the ImageIcon to be shown on the ImageUIComponent.
     *
     * @return Image
     */

    public ImageIcon getImageIcon()
        {
        return (this.imageIcon);
        }


    /***********************************************************************************************
     * Set the ImageIcon to be shown on the ImageUIComponent.
     *
     * @param imageicon
     */

    public void setImageIcon(final ImageIcon imageicon)
        {
        this.imageIcon = imageicon;
        }


    /***********************************************************************************************
     * Get the List of Metadata for the ImageIcon.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadata()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata for this ImageIcon.
     *
     * @param metadata
     */

    public void setMetadata(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the Image panel.
     *
     * @return JComponent
     */

    private JComponent getImagePanel()
        {
        return (this.panelImage);
        }


    /***********************************************************************************************
     * Set the Image panel.
     *
     * @param panel
     */

    private void setImagePanel(final JComponent panel)
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

    private ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
