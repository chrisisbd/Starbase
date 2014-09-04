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

package org.lmn.fc.ui.choosers.utilities;

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/***************************************************************************************************
 * Use this modal dialog to let the user choose one string from a long
 * list.
 * The basics:
 * <pre>
 * String[] choices = {"A", "long", "array", "of", "strings"};
 * String selectedName = JListDialog.showDialog(
 * componentInControllingFrame,
 * locatorComponent,
 * "A description of the list:",
 * "Dialog Title",
 * choices,
 * choices[0]);
 * </pre>
 */

public class JListDialog extends JDialog
                         implements ActionListener
    {
    private static final Dimension DIM_PREFERRED_SIZE = new Dimension(550, 180);
    private static JListDialog dialog;
    private static String strValue = "";

    // JListDialog Injections
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;

    private final JList list;


    /***********************************************************************************************
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     *
     * @param framecomponent
     * @param locationcomponent
     * @param labeltext
     * @param title
     * @param possiblevalues
     * @param font
     * @param colourforeground
     * @param initialvalue
     * @param longestvalue
     *
     * @return String
     */

    public static String showDialog(final Component framecomponent,
                                    final Component locationcomponent,
                                    final String labeltext,
                                    final String title,
                                    final String[] possiblevalues,
                                    final FontInterface font,
                                    final ColourInterface colourforeground,
                                    final String initialvalue,
                                    final String longestvalue)
        {
        final Frame frame;

        frame = JOptionPane.getFrameForComponent(framecomponent);

        dialog = new JListDialog(frame,
                                 locationcomponent,
                                 labeltext,
                                 title,
                                 possiblevalues,
                                 font,
                                 colourforeground,
                                 initialvalue,
                                 longestvalue);
        dialog.setVisible(true);

        return (strValue);
        }


    /***********************************************************************************************
     * JListDialog.
     *
     * @param frame
     * @param locationComp
     * @param labelText
     * @param title
     * @param data
     * @param font
     * @param colourforeground
     * @param initialValue
     * @param longestvalue
     */

    private JListDialog(final Frame frame,
                        final Component locationComp,
                        final String labelText,
                        final String title,
                        final Object[] data,
                        final FontInterface font,
                        final ColourInterface colourforeground,
                        final String initialValue,
                        final String longestvalue)
        {
        super(frame, title, true);

        // Injections
        this.pluginFont = font;
        this.pluginColour = colourforeground;

        final JScrollPane scrollList;
        final JPanel panelList;
        final JLabel label;
        final JPanel panelButtons;
        final JButton buttonSelect;
        final JButton buttonCancel;
        final Container contentPane;

        // Create and initialize the buttons
        buttonSelect = new JButton(ChooserInterface.BUTTON_SELECT);
        buttonSelect.setFont(getFontData().getFont());
        buttonSelect.setForeground(getColourData().getColor());
        buttonSelect.setActionCommand(ChooserInterface.BUTTON_SELECT);
        buttonSelect.addActionListener(this);
        getRootPane().setDefaultButton(buttonSelect);

        buttonCancel = new JButton(ChooserInterface.BUTTON_CANCEL);
        buttonCancel.setFont(getFontData().getFont());
        buttonCancel.setForeground(getColourData().getColor());
        buttonCancel.addActionListener(this);

        list = new JList(data)
            {
            // Subclass JList to workaround bug 4832765, which can cause the
            // scroll pane to not let the user easily scroll up to the beginning
            // of the list.  An alternative would be to set the unitIncrement
            // of the JScrollBar to a fixed value. You wouldn't get the nice
            // aligned scrolling, but it should work.

            public int getScrollableUnitIncrement(final Rectangle visibleRect,
                                                  final int orientation,
                                                  final int direction)
                {
                // If nothing is visible or the list is empty, -1 is returned
                if ((orientation == SwingConstants.VERTICAL)
                    && (direction < 0)
                    && (getFirstVisibleIndex() != -1))
                    {
                    final int intRow;
                    final Rectangle rectangle;

                    intRow = getFirstVisibleIndex();
                    rectangle = getCellBounds(intRow, intRow);

                    if ((rectangle.y == visibleRect.y)
                        && (intRow != 0))
                        {
                        final Point point;
                        final int intIndexPrevious;
                        final Rectangle rectPrevious;

                        point = rectangle.getLocation();
                        point.y--;

                        intIndexPrevious = locationToIndex(point);
                        rectPrevious = getCellBounds(intIndexPrevious, intIndexPrevious);

                        if ((rectPrevious == null)
                            || (rectPrevious.y >= rectangle.y))
                            {
                            return (0);
                            }

                        return (rectPrevious.height);
                        }
                    }

                return (super.getScrollableUnitIncrement(visibleRect, orientation, direction));
                }
            };

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(getFontData().getFont());
        list.setForeground(getColourData().getColor());

        if (longestvalue != null)
            {
            list.setPrototypeCellValue(longestvalue);
            }

        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);

        list.addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent event)
                {
                if (event.getClickCount() == 2)
                    {
                    buttonSelect.doClick(); // Emulate button click
                    }
                }
            });

        scrollList = new JScrollPane(list);
        scrollList.setPreferredSize(DIM_PREFERRED_SIZE);
        scrollList.setAlignmentX(LEFT_ALIGNMENT);

        // Create a container so that we can add a title around
        // the scroll pane.  Can't add a title directly to the
        // scroll pane because its background would be white.
        // Lay out the label and scroll pane from top to bottom.

        panelList = new JPanel();
        panelList.setLayout(new BoxLayoutFixed(panelList, BoxLayout.PAGE_AXIS));
        label = new JLabel(labelText);
        label.setFont(getFontData().getFont());
        label.setForeground(getColourData().getColor());
        label.setLabelFor(list);

        panelList.add(label);
        panelList.add(Box.createRigidArea(new Dimension(0, 5)));
        panelList.add(scrollList);
        panelList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Lay out the buttons from left to right
        panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayout.LINE_AXIS));
        panelButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelButtons.add(Box.createHorizontalGlue());
        panelButtons.add(buttonSelect);
        panelButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        panelButtons.add(buttonCancel);
        panelButtons.add(Box.createHorizontalGlue());

        // Put everything together, using the content pane's BorderLayout
        contentPane = getContentPane();
        contentPane.add(panelList, BorderLayout.CENTER);
        contentPane.add(panelButtons, BorderLayout.PAGE_END);

        // Initialize values
        setValue(initialValue);
        pack();
        setLocationRelativeTo(locationComp);
        }


    /***********************************************************************************************
     * Handle clicks on the Select and Cancel buttons.
     *
     * @param event
     */

    public void actionPerformed(final ActionEvent event)
        {
        if (ChooserInterface.BUTTON_SELECT.equals(event.getActionCommand()))
            {
            strValue = (String) (list.getSelectedValue());
            }

        JListDialog.dialog.setVisible(false);
        }


    /***********************************************************************************************
     * Set the Value on the JList.
     *
     * @param value
     */

    private void setValue(final String value)
        {
        strValue = value;
        list.setSelectedValue(strValue, true);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    private FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    private ColourInterface getColourData()
        {
        return (this.pluginColour);
        }
    }