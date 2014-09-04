// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.common.utilities.ui;

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * AlignedListCellRenderer.
 * ListCellRenderer with alignment functionality.
 * Found at: http://forum.java.sun.com/thread.jspa?threadID=616601&messageID=3435178.
 */

public final class AlignedListCellRenderer extends DefaultListCellRenderer
    {
    // Injections
    private final int intAlignment;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColourForeground;
    private final ColourInterface pluginColourBackground;
    private final String strTooltip;


    /***********************************************************************************************
     * AlignedListCellRenderer.
     *
     * @param align
     */

    public AlignedListCellRenderer(final int align)
        {
        this.intAlignment = align;
        this.pluginFont = null;
        this.pluginColourForeground = null;
        this.pluginColourBackground = null;
        this.strTooltip = "";
        }


    /***********************************************************************************************
     * AlignedListCellRenderer.
     *
     * @param align
     * @param font
     * @param colourforeground
     * @param colourbackground
     */

    public AlignedListCellRenderer(final int align,
                                   final FontInterface font,
                                   final ColourInterface colourforeground,
                                   final ColourInterface colourbackground)
        {
        this.intAlignment = align;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;
        this.strTooltip = "";
        }


    /***********************************************************************************************
     * AlignedListCellRenderer with Tooltip.
     *
     * @param align
     * @param font
     * @param colourforeground
     * @param colourbackground
     * @param tooltip
     */

    public AlignedListCellRenderer(final int align,
                                   final FontInterface font,
                                   final ColourInterface colourforeground,
                                   final ColourInterface colourbackground,
                                   final String tooltip)
        {
        this.intAlignment = align;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;
        this.strTooltip = tooltip;
        }


    /***********************************************************************************************
     * Get the List Cell Renderer Component.
     *
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     *
     * @return Component
     */

    public Component getListCellRendererComponent(final JList list,
                                                  final Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
        {
        JLabel label;

        try
            {
            final Component component;

            component = super.getListCellRendererComponent(list,
                                                           value,
                                                           index,
                                                           isSelected,
                                                           cellHasFocus);

            // DefaultListCellRenderer uses a JLabel as the rendering component:
            if ((component != null)
                && (component instanceof JLabel))
                {
                // The renderer is a JLabel
                label = (JLabel) component;
                label.setHorizontalAlignment(intAlignment);
                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

                if (pluginFont != null)
                    {
                    label.setFont(pluginFont.getFont());
                    }

                if (isSelected)
                    {
                    if (pluginColourBackground != null)
                        {
                        label.setForeground(pluginColourBackground.getColor());
                        }
                    else
                        {
                        label.setForeground(list.getSelectionForeground());
                        }

                    if (pluginColourForeground != null)
                        {
                        label.setBackground(pluginColourForeground.getColor());
                        }
                    else
                        {
                        label.setBackground(list.getSelectionBackground());
                        }

                    if (strTooltip != null)
                        {
                        label.setToolTipText(strTooltip);
                        }
                    else
                        {
                        label.setToolTipText("");
                        }
                    }
                else
                    {
                    if (pluginColourForeground != null)
                        {
                        label.setForeground(pluginColourForeground.getColor());
                        }
                    else
                        {
                        label.setForeground(list.getForeground());
                        }

                    if (pluginColourBackground != null)
                        {
                        label.setBackground(pluginColourBackground.getColor());
                        }
                    else
                        {
                        label.setBackground(list.getBackground());
                        }

                    // No Tooltip if not selected
                    label.setToolTipText("");
                    }
                }
            else
                {
                label = new JLabel("Error:ListCellRenderer");
                }
            }

        catch (final Exception exception)
            {
            label = new JLabel("Error:ListCellRenderer");
            }

        return (label);
        }
    }
