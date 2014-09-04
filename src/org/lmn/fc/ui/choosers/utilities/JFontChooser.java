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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/***************************************************************************************************
 * A simple FontChooser dialog that implements similar functionality to
 * the JFileChooser, JOptionPane and JColorChooser components provided with Swing.<p>
 * Upon initialization, the JFontChooser polls the system for all available
 * fonts, initializes the various JList components to the values of the
 * default font and provides a preview of the font. As options are changed/selected
 * the preview is updated to display the current font.<p>
 * JFileChooser can either be created and added to a GUI as a typical
 * JComponent or it can display a JDialog using the {@link #showDialog(Component, String) showDialog}
 * method (just like the <b>JFileChooser</b> component). Objects
 * that extend JFontChooser should use the {@link #acceptSelection() acceptSelection} and
 * {@link #cancelSelection() cancelSelection} methods to determine either
 * an accept or cancel selection.<p>
 */

public class JFontChooser extends JComponent implements ActionListener,
                                                        ListSelectionListener
    {
    private static final int ERROR_OPTION  = 0;
    public static final int ACCEPT_OPTION = 2;
    private static final int CANCEL_OPTION = 4;

    // Injections
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;
    private Font fontSelected;

    private JList fontNames;
    private JList fontSizes;
    private JList fontStyles;
    private JTextField currentSize;
    private JButton buttonSelect;
    private JButton buttonCancel;
    private Font[] availableFonts;
    private JFontPreviewPanel preview;
    private JDialog dialog;
    private int returnValue;


    /***********************************************************************************************
     * Constructs a new JFontChooser component initialized to the supplied font object.
     *
     * @param fonttext
     * @param colourforeground
     * @param newfont
     */

    public JFontChooser(final FontInterface fonttext,
                        final ColourInterface colourforeground,
                        final Font newfont)
        {
        super();

        this.pluginFont = fonttext;
        this.pluginColour = colourforeground;
        this.fontSelected = newfont;

        setupChooser();
        }


    /***********************************************************************************************
     * Set up the Font Chooser.
     */

    private void setupChooser()
        {
        final Font[] fontList;
        final Object[] styles;
        final Vector<String> vecNames;
        final Vector<Font> vecFonts;
        final String[] arrayFontSizes;
        final JScrollPane fontNamesScroll;
        final JScrollPane fontStylesScroll;
        final JScrollPane fontSizesScroll;
        final GridBagLayout layoutTop;
        final GridBagLayout layoutSizes;
        final GridBagConstraints constraintsTop;
        final GridBagConstraints constraintsSizes;
        final JPanel panelTop;
        final JPanel panelSizes;
        final JPanel panelButtons;

        removeAll();
        setLayout(new BorderLayout());

        fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        vecNames = new Vector<String>(1, 1);
        vecFonts = new Vector<Font>(1, 1);

        for (int intIndexFonts = 0;
             intIndexFonts < fontList.length;
             intIndexFonts++)
            {
            final String fontName;

            fontName = fontList[intIndexFonts].getFamily();

            if (!vecNames.contains(fontName))
                {
                vecNames.addElement(fontName);
                vecFonts.addElement(fontList[intIndexFonts]);
                }
            }

        availableFonts = new Font[vecFonts.size()];

        for (int intIndexFonts = 0;
             intIndexFonts < vecFonts.size();
             intIndexFonts++)
            {
            availableFonts[intIndexFonts] = vecFonts.elementAt(intIndexFonts);
            }

        fontNames = new JList(vecNames);
        fontNames.setForeground(getColourData().getColor());
        fontNames.setFont(getFontData().getFont());

        fontNamesScroll = new JScrollPane(fontNames);
        fontNames.addListSelectionListener(this);

        styles = new Object[]{"Plain",
                              "Bold",
                              "Italic",
                              "BoldItalic"};

        fontStyles = new JList(styles);
        fontStylesScroll = new JScrollPane(fontStyles);
        fontStyles.setSelectedIndex(0);
        fontStyles.addListSelectionListener(this);
        fontStyles.setForeground(getColourData().getColor());
        fontStyles.setFont(getFontData().getFont());

        arrayFontSizes = new String[69];

        for (int intFontSize = 3;
             intFontSize < 72;
             intFontSize++)
            {
            arrayFontSizes[intFontSize - 3] = (new Integer(intFontSize + 1)).toString();
            }

        fontSizes = new JList(arrayFontSizes);
        fontSizes.setForeground(getColourData().getColor());
        fontSizes.setFont(getFontData().getFont());

        fontSizesScroll = new JScrollPane(fontSizes);
        fontSizes.addListSelectionListener(this);

        currentSize = new JTextField(5);
        currentSize.setForeground(getColourData().getColor());
        currentSize.setFont(getFontData().getFont());
        currentSize.setText((new Integer(fontSelected.getSize())).toString());
        currentSize.addActionListener(this);

        layoutSizes = new GridBagLayout();
        constraintsSizes = new GridBagConstraints();
        panelSizes = new JPanel(layoutSizes);

        constraintsSizes.gridx = 0;
        constraintsSizes.gridy = 0;
        constraintsSizes.insets = new Insets(2, 5, 2, 5);
        constraintsSizes.anchor = GridBagConstraints.WEST;
        panelSizes.add(currentSize);
        layoutSizes.setConstraints(currentSize, constraintsSizes);

        panelSizes.add(fontSizesScroll);
        constraintsSizes.gridy++;
        constraintsSizes.fill = GridBagConstraints.HORIZONTAL;
        layoutSizes.setConstraints(fontSizesScroll, constraintsSizes);

        preview = new JFontPreviewPanel(this.fontSelected);

        buttonSelect = new JButton(ChooserInterface.BUTTON_SELECT);
        buttonSelect.addActionListener(this);
        buttonSelect.setForeground(getColourData().getColor());
        buttonSelect.setFont(getFontData().getFont());

        buttonCancel = new JButton(ChooserInterface.BUTTON_CANCEL);
        buttonCancel.addActionListener(this);
        buttonCancel.setForeground(getColourData().getColor());
        buttonCancel.setFont(getFontData().getFont());

        layoutTop = new GridBagLayout();
        constraintsTop = new GridBagConstraints();
        panelTop = new JPanel(layoutTop);

        constraintsTop.anchor = GridBagConstraints.WEST;
        constraintsTop.fill = GridBagConstraints.VERTICAL;
        constraintsTop.insets = new Insets(2, 5, 8, 5);
        constraintsTop.gridx = 0;
        constraintsTop.gridy = 0;
        panelTop.add(fontNamesScroll);
        layoutTop.setConstraints(fontNamesScroll, constraintsTop);
        constraintsTop.gridx++;
        panelTop.add(fontStylesScroll);
        layoutTop.setConstraints(fontStylesScroll, constraintsTop);
        constraintsTop.gridx++;
        panelTop.add(panelSizes);
        layoutTop.setConstraints(panelSizes, constraintsTop);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(BorderLayout.NORTH, panelTop);
        add(BorderLayout.CENTER, preview);

        panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        panelButtons.add(buttonSelect);
        panelButtons.add(buttonCancel);

        add(BorderLayout.SOUTH, panelButtons);

        fontSizes.setSelectedValue((new Integer(fontSelected.getSize())).toString(), true);
        fontNames.setSelectedValue(fontSelected.getFamily(), true);

        if (fontSelected.getStyle() == Font.PLAIN)
            {
            fontStyles.setSelectedValue("Plain", false);
            }
        else if (fontSelected.getStyle() == Font.ITALIC)
            {
            fontStyles.setSelectedValue("Italic", false);
            }
        else if (fontSelected.getStyle() == Font.BOLD)
            {
            fontStyles.setSelectedValue("Bold", false);
            }
        else if (fontSelected.getStyle() == (Font.BOLD | Font.ITALIC))
            {
            fontStyles.setSelectedValue("BoldItalic", false);
            }
        }


    /***********************************************************************************************
     * Update the selected Font.
     *
     * @param newfont
     */

    private void updateFont(final Font newfont)
        {
        this.fontSelected = newfont;
        preview.setFont(this.fontSelected);
        }


    /***********************************************************************************************
     * Update the selected Font size.
     *
     * @param size
     */

    private void updateFontSize(final int size)
        {
        updateFont(fontSelected.deriveFont((new Integer(size)).floatValue()));
        }


    /***********************************************************************************************
     * Update the selected Font style.
     *
     * @param style
     */

    private void updateFontStyle(final int style)
        {
        updateFont(fontSelected.deriveFont(style));
        }


    /***********************************************************************************************
     * Returns the currently selected font. Typically called after receipt
     * of an ACCEPT_OPTION (using the {@link #showDialog(Component, String) showDialog} option)
     * or from within the approveSelection method (using the component option).
     *
     * @return java.awt.Font A font class that represents the currently selected font.
     */

    public Font getSelectedFont()
        {
        return (this.fontSelected);
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



    /***********************************************************************************************
     * Processes action events from the Select and Cancel buttons
     * as well as the current size TextField.
     *
     * @param event
     */

    public void actionPerformed(final ActionEvent event)
        {
        if (event.getSource().equals(buttonSelect))
            {
            returnValue = ACCEPT_OPTION;
            if (dialog != null)
                {
                dialog.setVisible(false);
                }
            acceptSelection();
            }

        if (event.getSource().equals(buttonCancel))
            {
            returnValue = CANCEL_OPTION;
            if (dialog != null)
                {
                dialog.setVisible(false);
                }
            cancelSelection();
            }

        if (event.getSource().equals(currentSize))
            {
            fontSizes.setSelectedValue(currentSize.getText(), true);
            }
        }


    /***********************************************************************************************
     * Processes events received from the various JList objects.
     *
     * @param event
     */

    public void valueChanged(final ListSelectionEvent event)
        {
        if (event.getSource().equals(fontNames))
            {
            Font fontNew;

            fontNew = availableFonts[fontNames.getSelectedIndex()];
            fontNew = new Font(fontNew.getFontName(), fontSelected.getStyle(), fontSelected.getSize());
            updateFont(fontNew);
            }

        if (event.getSource().equals(fontSizes))
            {
            currentSize.setText((String) fontSizes.getSelectedValue());
            updateFontSize(new Integer(currentSize.getText()));
            }

        if (event.getSource().equals(fontStyles))
            {
            int intStyle;
            final String strSelectedStyle;

            intStyle = Font.PLAIN;
            strSelectedStyle = (String) fontStyles.getSelectedValue();

            if ("Plain".equals(strSelectedStyle))
                {
                intStyle = Font.PLAIN;
                }
            if ("Bold".equals(strSelectedStyle))
                {
                intStyle = Font.BOLD;
                }
            if ("Italic".equals(strSelectedStyle))
                {
                intStyle = Font.ITALIC;
                }
            if ("BoldItalic".equals(strSelectedStyle))
                {
                intStyle = (Font.BOLD | Font.ITALIC);
                }

            updateFontStyle(intStyle);
            }
        }


    /***********************************************************************************************
     * Pops up a Font chooser dialog with the supplied <i>title</i>,
     * centered about the component <i>parent</i>.
     *
     * @param parent
     * @param title
     *
     * @return int
     */

    public int showDialog(final Component parent,
                          final String title)
        {
        final Frame frame;

        returnValue = ERROR_OPTION;

        if (parent instanceof Frame)
            {
            frame = (Frame) parent;
            }
        else
            {
            frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
            }

        dialog = new JDialog(frame, title, true);
        dialog.getContentPane().add("Center", this);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return returnValue;
        }


    /***********************************************************************************************
     * This method is called when the user presses the Select button, selecting
     * the currently displayed font.
     * Children of JFontChooser should override this method to process this event.
     */

    public void acceptSelection()
        {
        }


    /***********************************************************************************************
     * This method is called when the user presses the Cancel button.
     * Children of JFontChooser should override this method to process this event.
     */

    public void cancelSelection()
        {
        }
    }
