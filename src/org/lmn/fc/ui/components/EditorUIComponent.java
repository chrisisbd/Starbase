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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  23-10-03    LMN created file
//  20-10-04    LMN tidied up...
//  09-11-11    LMN split into Utilities and UIComponent
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.components;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;


/***************************************************************************************************
 * An EditorUIComponent, to ensure that all Editors look the same...
 */

public class EditorUIComponent extends UIComponent
    {
    // Column names for Editors table (until we get an Editor bean)
    public static final String EDITOR_CLASSNAME  = "EditorClassName";

    public static final int HEIGHT_DESCRIPTION = 100;
    public static final int HEIGHT_DESCRIPTION_SHORT = 50;

    public static final Dimension DIM_HORIZ_SPACER_30 = new Dimension(30, 1);
    public static final Dimension DIM_HORIZ_SPACER_10 = new Dimension(10, 1);
    public static final Dimension DIM_ROW_SPACER = new Dimension(1, 3);
    public static final Dimension DIM_DESCRIPTION_SPACER = new Dimension(1, HEIGHT_DESCRIPTION- EditorUtilities.HEIGHT_ROW + (int)DIM_ROW_SPACER.getHeight());
    public static final Dimension DIM_TEXTAREA_SPACER = new Dimension(1, EditorUtilities.HEIGHT_TEXTAREA- EditorUtilities.HEIGHT_ROW + (int)DIM_ROW_SPACER.getHeight());
    public static final Dimension DIM_ROW_SPACER_END = new Dimension(1, 3);

    private final String strResourceKey;
    private ColourInterface colourCanvas;
    private ColourInterface colourText;
    private FontInterface fontLabel;
    private FontInterface fontInterface;


    /***********************************************************************************************
     * Create the EditorUIComponent and the underlying JPanel.
     *
     * @param resourcekey
     */

    public EditorUIComponent(final String resourcekey)
        {
        // Create the JPanel, with a BorderLayout
        super();

        // Save the ResourceKey
        strResourceKey = resourcekey;
        }


    /***********************************************************************************************
     *
     * @param colour
     * @param editor
     * @param label
     * @param data
     * @param buttons
     */

    public final void installPanels(final ColourInterface colour,
                                    final JPanel editor,
                                    final JPanel label,
                                    final JPanel data,
                                    final JPanel buttons)
        {
        setMaximumSize(new Dimension(MAX_UNIVERSE, MAX_UNIVERSE));
        setMinimumSize(new Dimension(EditorUtilities.DIM_MAGIC, EditorUtilities.DIM_MAGIC));
        setBorder(BorderFactory.createEmptyBorder(EditorUtilities.WIDTH_EMPTY_BORDER,
                                                  EditorUtilities.WIDTH_EMPTY_BORDER,
                                                  EditorUtilities.WIDTH_EMPTY_BORDER,
                                                  EditorUtilities.WIDTH_EMPTY_BORDER));
        setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.Y_AXIS));
        setBackground(colour.getColor());

        editor.add(label, null);
        editor.add(data, null);

        editor.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createRigidArea(new Dimension(10, 20)));
        add(editor);
        add(Box.createRigidArea(new Dimension(10,20)));
        add(buttons);
        }


    /***********************************************************************************************
     * Create a JLabel.
     *
     * @param colour
     * @param font
     * @param labelkey
     *
     * @return JLabel
     */

    public final JLabel createLabel(final ColourInterface colour,
                                    final FontInterface font,
                                    final String labelkey)
        {
        final JLabel label;

        label = new JLabel();
        label.setPreferredSize(new Dimension(EditorUtilities.DIM_MAGIC, EditorUtilities.HEIGHT_ROW));
        label.setMaximumSize(new Dimension(MAX_UNIVERSE, EditorUtilities.HEIGHT_ROW));
        label.setMinimumSize(new Dimension(0, EditorUtilities.HEIGHT_ROW));
        label.setText(REGISTRY.getString(getResourceKey() + labelkey));
        label.setForeground(colour.getColor());
        label.setFont(font.getFont());

        return (label);
        }


    /***********************************************************************************************
     * Create a JTextField.
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltipkey
     * @param enabled
     *
     * @return JTextField
     */

    public final JTextField createTextField(final ColourInterface colour,
                                            final FontInterface font,
                                            final String text,
                                            final String tooltipkey,
                                            final boolean enabled)
        {
        final JTextField textField;

        textField = new JTextField();
        textField.setPreferredSize(new Dimension(EditorUtilities.DIM_MAGIC, EditorUtilities.HEIGHT_ROW));
        textField.setMaximumSize(new Dimension(MAX_UNIVERSE, EditorUtilities.HEIGHT_ROW));
        textField.setMinimumSize(new Dimension(0, EditorUtilities.HEIGHT_ROW));
        textField.setAlignmentX(0);
        textField.setMargin(new Insets(0, 7, 0, 7));
        textField.setText(text);
        textField.setToolTipText(REGISTRY.getString(getResourceKey() + tooltipkey));
        textField.setEnabled(enabled);
        textField.setForeground(colour.getColor());
        textField.setFont(font.getFont());

        return (textField);
        }


    /***********************************************************************************************
     * Create a JTextArea.
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltipkey
     * @param height
     * @param enabled
     *
     * @return JTextArea
     */

    public final JTextArea createTextArea(final ColourInterface colour,
                                          final FontInterface font,
                                          final String text,
                                          final String tooltipkey,
                                          final int height,
                                          final boolean enabled)
        {
        final JTextArea textArea;

        textArea = new JTextArea();
        textArea.setPreferredSize(new Dimension(EditorUtilities.DIM_MAGIC, height));
        textArea.setMaximumSize(new Dimension(MAX_UNIVERSE, height));
        textArea.setMinimumSize(new Dimension(0, height));
        textArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
                                                              BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        textArea.setAlignmentX(0);
        textArea.setLineWrap(true);

        // This margin is ignored because we set a border... sigh
        textArea.setMargin(new Insets(5, 5, 5, 5));

        if ((tooltipkey != null)
            && (!EMPTY_STRING.equals(tooltipkey)))
            {
            textArea.setToolTipText(REGISTRY.getString(getResourceKey() + tooltipkey));
            }

        textArea.setText(text);
        textArea.setEnabled(enabled);
        textArea.setForeground(colour.getColor());
        textArea.setFont(font.getFont());

        return (textArea);
        }


    /***********************************************************************************************
     * Create a JButton.
     *
     * @param colour
     * @param font
     * @param labelkey
     * @param tooltipkey
     * @param command
     * @param enabled
     *
     * @return JButton
     */

    public final JButton createButton(final ColourInterface colour,
                                      final FontInterface font,
                                      final String labelkey,
                                      final String tooltipkey,
                                      final String command,
                                      final boolean enabled)
        {
        final JButton button;

        button = new JButton();
        button.setPreferredSize(EditorUtilities.DIM_BUTTON);
        button.setMaximumSize(EditorUtilities.DIM_BUTTON);
        button.setMinimumSize(EditorUtilities.DIM_BUTTON);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setText(REGISTRY.getString(getResourceKey() + labelkey));
        button.setToolTipText(REGISTRY.getString(getResourceKey() + tooltipkey));
        button.setActionCommand(command);
        button.setForeground(colour.getColor());
        button.setEnabled(enabled);
        button.setFont(font.getFont());

        return (button);
        }


    /***********************************************************************************************
     * Create a browser JButton.
     *
     * @param colour
     * @param font
     * @param labelkey
     * @param tooltipkey
     * @param command
     * @param enabled
     *
     * @return JButton
     */

    public final JButton createBrowserButton(final ColourInterface colour,
                                             final FontInterface font,
                                             final String labelkey,
                                             final String tooltipkey,
                                             final String command,
                                             final boolean enabled)
        {
        final JButton button;

        button = new JButton();
//        JButton button = new JButton(new ImageIcon(RegistryModel.getImagesRoot()
//                                     + "framework/ellipsis.jpg"));
        button.setPreferredSize(EditorUtilities.DIM_BUTTON_BROWSE);
        button.setMaximumSize(EditorUtilities.DIM_BUTTON_BROWSE);
        button.setMinimumSize(EditorUtilities.DIM_BUTTON_BROWSE);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
//        button.setIcon(new ImageIcon(RegistryModel.getImagesRoot()
//                                     + "ellipsis.jpg"));
//        button.setText(StringData.getString(getResourceKey() + labelkey));
        button.setFont(new Font("Monospaced", Font.BOLD, 40));
        button.setText("...");
        button.setToolTipText(REGISTRY.getString(getResourceKey() + tooltipkey));
        button.setActionCommand(command);
        button.setForeground(colour.getColor());
        button.setEnabled(enabled);
        button.setFont(font.getFont());

        return (button);
        }


    /***********************************************************************************************
     * Create a JCheckBox.
     *
     * @param colour
     * @param tooltipkey
     * @param enabled
     * @param selected
     *
     * @return JCheckBox
     */

    public final JCheckBox createCheckBox(final ColourInterface colour,
                                          final String tooltipkey,
                                          final boolean enabled,
                                          final boolean selected)
        {
        final JCheckBox checkBox;

        checkBox = new JCheckBox();
        checkBox.setAlignmentX(0);
        checkBox.setToolTipText(REGISTRY.getString(getResourceKey() + tooltipkey));
        checkBox.setEnabled(enabled);
        checkBox.setSelected(selected);
        checkBox.setBackground(colour.getColor());

        return (checkBox);
        }


    /***********************************************************************************************
     * Create a JComboBox.
     *
     * @param colourforeground
     * @param font
     * @param tooltipkey
     * @param enabled
     * @param items
     * @param selecteditem
     *
     * @return JComboBox
     */

    public final JComboBox createComboBox(final ColourInterface colourforeground,
                                          final FontInterface font,
                                          final String tooltipkey,
                                          final boolean enabled,
                                          final Iterator items,
                                          final String selecteditem)
        {
        final JComboBox comboBox;

        comboBox = new JComboBox();
        comboBox.setPreferredSize(new Dimension(EditorUtilities.DIM_MAGIC, EditorUtilities.HEIGHT_ROW));
        comboBox.setMaximumSize(new Dimension(MAX_UNIVERSE, EditorUtilities.HEIGHT_ROW));
        comboBox.setMinimumSize(new Dimension(0, EditorUtilities.HEIGHT_ROW));
        comboBox.setAlignmentX(0);
        comboBox.setToolTipText(REGISTRY.getString(getResourceKey() + tooltipkey));
        comboBox.setEnabled(enabled);
        comboBox.setForeground(colourforeground.getColor());
        comboBox.setBackground(new Color(255, 255, 255));
        comboBox.setFont(font.getFont());

        if ((items != null)
            && (selecteditem != null))
            {
            while (items.hasNext())
                {
                comboBox.addItem(items.next());
                }

            comboBox.setSelectedItem(selecteditem);
            }

        return (comboBox);
        }


    /***********************************************************************************************
     * Get the colour of the Editor canvas.
     *
     * @return ColourPlugin
     */

    public final ColourInterface getCanvasColour()
        {
        return (UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND);

        //return (colourCanvas);
        }


    /***********************************************************************************************
     * Get the colour of the Editor text.
     *
     * @return ColourPlugin
     */

    public final ColourInterface getTextColour()
        {
        return (colourText);
        }


    /***********************************************************************************************
     * Get the Font for the Labels.
     *
     * @return FontPlugin
     */

    public final FontInterface getLabelFont()
        {
        return (fontLabel);
        }


    /***********************************************************************************************
     * Get the Font for data entry.
     *
     * @return FontPlugin
     */

    public final FontInterface getDataFont()
        {
        return (fontInterface);
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Editor.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (strResourceKey);
        }


    /***********************************************************************************************
     * Read all Resources required by the Editor.
     */

    public final void readResources()
        {
        final String key;

        // These Resources are common to all Editors, and so use a combination
        // of the Registry ResourceKey and 'Editor.' instead of the key provided by the Editor
        key = REGISTRY.getFrameworkResourceKey();

        setDebug(REGISTRY.getBooleanProperty(key + KEY_EDITOR_ENABLE_DEBUG));

        colourCanvas = (ColourInterface)REGISTRY.getProperty(key + KEY_EDITOR_COLOUR_CANVAS);
        colourText = (ColourInterface)REGISTRY.getProperty(key + KEY_EDITOR_COLOUR_TEXT);
        fontLabel = (FontInterface)REGISTRY.getProperty(key + KEY_EDITOR_FONT_LABEL);
        fontInterface = (FontInterface) REGISTRY.getProperty(key + KEY_EDITOR_FONT_DATA);
        }
    }
