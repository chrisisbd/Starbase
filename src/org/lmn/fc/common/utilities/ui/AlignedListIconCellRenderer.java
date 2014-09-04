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

package org.lmn.fc.common.utilities.ui;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda.DatasetState;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.AtomPlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * AlignedListCellRenderer.
 * ListCellRenderer with alignment functionality.
 * Found at: http://forum.java.sun.com/thread.jspa?threadID=616601&messageID=3435178.
 */

public final class AlignedListIconCellRenderer extends DefaultListCellRenderer
    {
    private final AtomPlugin hostAtom;
    private final int             intHorizontalAlignment;
    private final int             intHorizontalTextPosition;
    private final FontInterface   pluginFont;
    private final ColourInterface pluginColourForeground;
    private final ColourInterface pluginColourBackground;


    /***********************************************************************************************
     * AlignedListIconCellRenderer.
     *
     *         enum: LEFT     SwingConstants.LEFT
     *               CENTER   SwingConstants.CENTER
     *               RIGHT    SwingConstants.RIGHT
     *               LEADING  SwingConstants.LEADING
     *               TRAILING SwingConstants.TRAILING
     *
     * @param horizalign
     * @param horizposition
     */

    public AlignedListIconCellRenderer(final int horizalign,
                                       final int horizposition)
        {
        this.hostAtom = null;
        this.intHorizontalAlignment = horizalign;
        this.intHorizontalTextPosition = horizposition;
        this.pluginFont = null;
        this.pluginColourForeground = null;
        this.pluginColourBackground = null;
        }


    /***********************************************************************************************
     * AlignedListIconCellRenderer.
     *
     *         enum: LEFT     SwingConstants.LEFT
     *               CENTER   SwingConstants.CENTER
     *               RIGHT    SwingConstants.RIGHT
     *               LEADING  SwingConstants.LEADING
     *               TRAILING SwingConstants.TRAILING
     *
     * @param atom
     * @param horizalign
     * @param horizposition
     * @param font
     * @param colourforeground
     * @param colourbackground
     */

    public AlignedListIconCellRenderer(final AtomPlugin atom,
                                       final int horizalign,
                                       final int horizposition,
                                       final FontInterface font,
                                       final ColourInterface colourforeground,
                                       final ColourInterface colourbackground)
        {
        this.hostAtom = atom;
        this.intHorizontalAlignment = horizalign;
        this.intHorizontalTextPosition = horizposition;
        this.pluginFont = font;
        this.pluginColourForeground = colourforeground;
        this.pluginColourBackground = colourbackground;
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

                // Sets the alignment of the label's contents along the X axis
                label.setHorizontalAlignment(intHorizontalAlignment);

                // Sets the horizontal position of the label's text, relative to its image
                label.setHorizontalTextPosition(intHorizontalTextPosition);

                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

                // See if we have a standard JLabel, or something more complex
                // in which case overwrite whatever the Renderer set already
                if (value instanceof ListCellBooleanData)
                    {
                    final ListCellBooleanData booleanData;

                    booleanData = (ListCellBooleanData)value;

                    label.setText(booleanData.getText());

                    if (((ListCellBooleanData) value).getState())
                        {
                        label.setIcon(booleanData.getTrueIcon());
                        }
                    else
                        {
                        label.setIcon(booleanData.getFalseIcon());
                        }
                    }
                else if (value instanceof ListCellDatasetState)
                    {
                    final ListCellDatasetState datasetState;

                    datasetState = (ListCellDatasetState)value;

                    // Indicate the Dataset Index in the current set of attachments
                    if (datasetState.getIndex() > -1)
                        {
                        label.setText(datasetState.getText() + " [" + datasetState.getIndex() + "]");
                        }
                    else
                        {
                        label.setText(datasetState.getText());
                        }

                    switch (datasetState.getDatasetState())
                        {
                        case ATTACHED:
                            {
                            label.setIcon(DatasetState.ATTACHED.getIcon(hostAtom));
                            break;
                            }

                        case DETACHED:
                            {
                            label.setIcon(DatasetState.DETACHED.getIcon(hostAtom));
                            break;
                            }

                        case LOCKED:
                            {
                            label.setIcon(DatasetState.LOCKED.getIcon(hostAtom));
                            break;
                            }
                        }

                    if (isSelected)
                        {
                        list.setToolTipText(datasetState.getTooltipText());
                        }
                    }

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
                    }
                }
            else
                {
                label = new JLabel("Error:ListCellRenderer");
                }
            }

        catch (Exception exception)
            {
            label = new JLabel("Error:ListCellRenderer");
            }

        return (label);
        }
    }
