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

package org.lmn.fc.ui.components;


import org.lmn.fc.common.utilities.files.IconFilter;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Time;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * EditorUtilities.
 */

public final class EditorUtilities
    {
    public static final int HEIGHT_ROW = 27; // TODO WAS 21
    public static final int HEIGHT_TEXTAREA = 150;
    public static final int DIM_MAGIC = 100;
    public static final int WIDTH_EMPTY_BORDER = 10;
    public static final Dimension DIM_BUTTON = new Dimension(140, 27);
    public static final Dimension DIM_BUTTON_BROWSE = new Dimension(30, HEIGHT_ROW);

    private static final int WIDTH_LABEL = 130;
    private static final int WIDTH_NARROW_FIELD = 100;
    private static final Dimension DIM_BUTTON_SPACER = new Dimension(10, 30);
    private static final Dimension DIM_BUTTON_HEIGHT = new Dimension(1, 30);


    /***********************************************************************************************
     * Create an ActionListener for the Browser chooser.
     *
     * @param pathname
     * @param title
     * @param component
     *
     * @return ActionListener
     */

    public static ActionListener createBrowserListener(final String pathname,
                                                       final String title,
                                                       final JTextComponent component)
        {
        final ActionListener listener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final JFileChooser fileChooser = new JFileChooser(pathname);
                fileChooser.setDialogTitle(title);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileFilter(new IconFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);
                final int intChoice = fileChooser.showOpenDialog(null);

                if ((intChoice == JFileChooser.APPROVE_OPTION)
                    && (component != null))
                    {
                    component.setText(fileChooser.getSelectedFile().getName());
                    }
                }
            };

        return (listener);
        }


    /***********************************************************************************************
     * Create an ActionListener for the Image Browser chooser.
     *
     * @param imagesroot
     * @param pathname
     * @param title
     * @param component
     *
     * @return ActionListener
     */

    public static ActionListener createImageBrowserListener(final String imagesroot,
                                                            final String pathname,
                                                            final String title,
                                                            final JTextComponent component)
        {
        final ActionListener listener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final JFileChooser fileChooser = new JFileChooser(imagesroot + pathname);
                fileChooser.setDialogTitle(title);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileFilter(new IconFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);
                final int intChoice = fileChooser.showOpenDialog(null);

                if ((intChoice == JFileChooser.APPROVE_OPTION)
                    && (component != null))
                    {
                    component.setText(pathname + fileChooser.getSelectedFile().getName());
                    }
                }
            };

        return (listener);
        }


    /***********************************************************************************************
     * Adjust the sizes of the specified component to be a narrower field.
     *
     * @param component
     */

    public static void adjustNarrowField(final JComponent component)
        {
        adjustNarrowField(component, WIDTH_NARROW_FIELD);
        }


    /***********************************************************************************************
     * Adjust the sizes of the specified component to be a narrower field.
     *
     * @param component
     * @param width
     */

    public static void adjustNarrowField(final JComponent component,
                                         final int width)
        {
        component.setMaximumSize(new Dimension(width, HEIGHT_ROW));
        component.setPreferredSize(new Dimension(width, HEIGHT_ROW));
        }


    /***********************************************************************************************
     * Adjust the width of the specified component.
     *
     * @param component
     * @param width
     */

    public static void setComponentWidth(final JComponent component,
                                         final int width)
        {
        if ((component != null)
            && (width > 0))
            {
            component.setMaximumSize(new Dimension(width, HEIGHT_ROW));
            component.setPreferredSize(new Dimension(width, HEIGHT_ROW));
            }
        }


    /***********************************************************************************************
     * Replace null Strings with empty Strings.
     *
     * @param string
     *
     * @return String
     */

    public static String replaceNull(final String string)
        {
        if (string == null)
            {
            return ("");
            }
        else
            {
            return (string);
            }
        }


    /***********************************************************************************************
     * Replace null Calendars with <code>Now</code>.
     *
     * @param calendar
     *
     * @return Calendar
     */

    public static GregorianCalendar replaceNull(final GregorianCalendar calendar)
        {
        if (calendar == null)
            {
            return (new GregorianCalendar());
            }
        else
            {
            return (calendar);
            }
        }


    /***********************************************************************************************
     * Replace null Dates with <code>Now</code>.
     *
     * @param date
     *
     * @return Date
     */

    public static Date replaceNull(final Date date)
        {
        if (date == null)
            {
            return (Chronos.getSystemDateNow());
            }
        else
            {
            return (date);
            }
        }


    /***********************************************************************************************
     * Replace null Times with <code>Now</code>.
     *
     * @param time
     *
     * @return Time
     */

    public static Time replaceNull(final Time time)
        {
        if (time == null)
            {
            return (Chronos.getSystemTimeNow());
            }
        else
            {
            return (time);
            }
        }


//    public static DegMinSecInterface replaceNull(final DegMinSecInterface dms)
//        {
//        if (dms == null)
//            {
//            final DegMinSecInterface dmsNew;
//
//            try
//                {
//                dmsNew = new DegMinSec(dms.get);
//                return (dmsNew);
//                }
//
//            catch (DegMinSecException exception)
//                {
//                return (null);
//                }
//            }
//        else
//            {
//            return (dms);
//            }
//        }


    /***********************************************************************************************
      *
      * @param host
      * @param colour
      * @param editor
      * @param label
      * @param data
      * @param buttons
      */

     public static void installPanels(final JComponent host,
                                      final ColourInterface colour,
                                      final JPanel editor,
                                      final JPanel label,
                                      final JPanel data,
                                      final JPanel buttons)
         {
         host.setMaximumSize(new Dimension(
                 UIComponentPlugin.MAX_UNIVERSE, UIComponentPlugin.MAX_UNIVERSE));
         host.setMinimumSize(new Dimension(DIM_MAGIC, DIM_MAGIC));
         host.setBorder(BorderFactory.createEmptyBorder(WIDTH_EMPTY_BORDER,WIDTH_EMPTY_BORDER,WIDTH_EMPTY_BORDER,WIDTH_EMPTY_BORDER));
         host.setLayout(new BoxLayoutFixed(host, BoxLayoutFixed.Y_AXIS));
         host.setBackground(colour.getColor());

         editor.add(label, null);
         editor.add(data, null);
         host.add(editor);
         host.add(Box.createRigidArea(new Dimension(10,15)));
         host.add(buttons);
         host.add(Box.createVerticalGlue());
         }


    /***********************************************************************************************
     *
     * @param colour
     * @param font
     * @param labeltext
     *
     * @return JLabel
     */

    public static JLabel createLabelDirect(final ColourInterface colour,
                                           final FontInterface font,
                                           final String labeltext)
        {
        final JLabel label;

        label = new JLabel();
        label.setPreferredSize(new Dimension(DIM_MAGIC, HEIGHT_ROW));
        label.setMaximumSize(new Dimension(UIComponentPlugin.MAX_UNIVERSE, HEIGHT_ROW));
        label.setMinimumSize(new Dimension(0, HEIGHT_ROW));
        label.setText(labeltext);
        label.setForeground(colour.getColor());
        label.setFont(font.getFont());

        return (label);
        }


    /***********************************************************************************************
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     * @param enabled
     *
     * @return JTextField
     */

    public static JTextField createTextFieldDirect(final ColourInterface colour,
                                                   final FontInterface font,
                                                   final String text,
                                                   final String tooltip,
                                                   final boolean enabled)
        {
        final JTextField textField;

        textField = new JTextField();
        textField.setPreferredSize(new Dimension(DIM_MAGIC, HEIGHT_ROW));
        textField.setMaximumSize(new Dimension(UIComponentPlugin.MAX_UNIVERSE, HEIGHT_ROW));
        textField.setMinimumSize(new Dimension(0, HEIGHT_ROW));
        textField.setAlignmentX(0);
        textField.setMargin(new Insets(0, 5, 0, 5));
        textField.setText(text);
        textField.setToolTipText(tooltip);
        textField.setEnabled(enabled);
        textField.setForeground(colour.getColor());
        textField.setFont(font.getFont());

        return (textField);
        }


    /***********************************************************************************************
     *
     * @param colour
     * @param font
     * @param tooltip
     * @param enabled
     *
     * @return JTextField
     */

    public static JPasswordField createPasswordFieldDirect(final ColourInterface colour,
                                                           final FontInterface font,
                                                           final String tooltip,
                                                           final boolean enabled)
        {
        final JPasswordField passwordField;

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(DIM_MAGIC, HEIGHT_ROW));
        passwordField.setMaximumSize(new Dimension(UIComponentPlugin.MAX_UNIVERSE, HEIGHT_ROW));
        passwordField.setMinimumSize(new Dimension(0, HEIGHT_ROW));
        passwordField.setAlignmentX(0);
        passwordField.setMargin(new Insets(0, 5, 0, 5));
        passwordField.setText("");
        passwordField.setToolTipText(tooltip);
        passwordField.setEnabled(enabled);
        passwordField.setForeground(colour.getColor());
        passwordField.setFont(font.getFont());

        return (passwordField);
        }


    /***********************************************************************************************
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     * @param height
     * @param enabled
     *
     * @return JTextArea
     */

    public static JTextArea createTextAreaDirect(final ColourInterface colour,
                                                 final FontInterface font,
                                                 final String text,
                                                 final String tooltip,
                                                 final int height,
                                                 final boolean enabled)
        {
        final JTextArea textArea;

        textArea = new JTextArea();
        textArea.setPreferredSize(new Dimension(HEIGHT_TEXTAREA, height));
        textArea.setMaximumSize(new Dimension(UIComponentPlugin.MAX_UNIVERSE, height));
        textArea.setMinimumSize(new Dimension(HEIGHT_TEXTAREA, height));
        textArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
                                                              BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        textArea.setAlignmentX(0);
        textArea.setLineWrap(true);

        // This margin is ignored because we set a border... sigh
        textArea.setMargin(new Insets(5, 5, 5, 5));

        textArea.setToolTipText(tooltip);
        textArea.setText(text);
        textArea.setEnabled(enabled);
        textArea.setForeground(colour.getColor());
        textArea.setFont(font.getFont());

        return (textArea);
        }


    /***********************************************************************************************
     * Create a JButton with plain text specified.
     *
     * @param colour
     * @param font
     * @param label
     * @param tooltip
     * @param command
     * @param enabled
     *
     * @return JButton
     */

    public static JButton createButtonDirect(final ColourInterface colour,
                                             final FontInterface font,
                                             final String label,
                                             final String tooltip,
                                             final String command,
                                             final boolean enabled)
        {
        final JButton button;

        button = new JButton();
        button.setPreferredSize(DIM_BUTTON);
        button.setMaximumSize(DIM_BUTTON);
        button.setMinimumSize(new Dimension(0, 0));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setText(label);
        button.setToolTipText(tooltip);
        button.setActionCommand(command);
        button.setForeground(colour.getColor());
        button.setEnabled(enabled);
        button.setFont(font.getFont());

        return (button);
        }


    /***********************************************************************************************
     * Create a browser JButton with plain text specified.
     *
     * @param colour
     * @param font
     * @param label
     * @param tooltip
     * @param command
     * @param enabled
     *
     * @return JButton
     */

    public static JButton createBrowserButtonDirect(final ColourInterface colour,
                                                    final FontInterface font,
                                                    final String label,
                                                    final String tooltip,
                                                    final String command,
                                                    final boolean enabled)
        {
        final JButton button;

        button = new JButton();
//        JButton button = new JButton(new ImageIcon(RegistryModel.getImagesRoot()
//                                     + "framework/ellipsis.jpg"));
        button.setPreferredSize(DIM_BUTTON_BROWSE);
        button.setMaximumSize(DIM_BUTTON_BROWSE);
        button.setMinimumSize(new Dimension(0, 0));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
//        button.setIcon(new ImageIcon(RegistryModel.getImagesRoot()
//                                     + "ellipsis.jpg"));
//        button.setText(StringData.getString(getResourceKey() + labelkey));
        button.setFont(new Font("Monospaced", Font.BOLD, 40));
        button.setText("...");
        button.setToolTipText(tooltip);
        button.setActionCommand(command);
        button.setForeground(colour.getColor());
        button.setEnabled(enabled);
        button.setFont(font.getFont());

        return (button);
        }


    /***********************************************************************************************
     * Create a JCheckBox with plain text supplied.
     *
     * @param colour
     * @param tooltip
     * @param enabled
     * @param selected
     *
     * @return JCheckBox
     */

    public static JCheckBox createCheckBoxDirect(final ColourInterface colour,
                                                 final String tooltip,
                                                 final boolean enabled,
                                                 final boolean selected)
        {
        final JCheckBox checkBox;

        checkBox = new JCheckBox();
        checkBox.setAlignmentX(0);
        checkBox.setToolTipText(tooltip);
        checkBox.setEnabled(enabled);
        checkBox.setSelected(selected);
        checkBox.setBackground(colour.getColor());

        return (checkBox);
        }


    /***********************************************************************************************
     * Create a JComboBox.
     *
     * @param colour
     * @param font
     * @param tooltip
     * @param enabled
     * @param items
     * @param selecteditem
     *
     * @return JComboBox
     */

    public static JComboBox createComboBoxDirect(final ColourInterface colour,
                                                 final FontInterface font,
                                                 final String tooltip,
                                                 final boolean enabled,
                                                 final Iterator items,
                                                 final String selecteditem)
        {
        final JComboBox comboBox;

        comboBox = new JComboBox();
        comboBox.setPreferredSize(new Dimension(DIM_MAGIC, HEIGHT_ROW));
        comboBox.setMaximumSize(new Dimension(UIComponentPlugin.MAX_UNIVERSE, HEIGHT_ROW));
        comboBox.setMinimumSize(new Dimension(0, HEIGHT_ROW));
        comboBox.setAlignmentX(0);
        comboBox.setToolTipText(tooltip);
        comboBox.setEnabled(enabled);
        comboBox.setForeground(colour.getColor());
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
     * Create the Editor JPanel.
     *
     * @param colour
     *
     * @return JPanel
     */

    public static JPanel createEditorPanel(final ColourInterface colour)
        {
        final JPanel panelEditor;

        panelEditor = new JPanel();
        panelEditor.setLayout(new BoxLayoutFixed(panelEditor, BoxLayoutFixed.X_AXIS));
        panelEditor.setBackground(colour.getColor());

        return (panelEditor);
        }


    /***********************************************************************************************
     *
     * @param colour
     * @param height
     *
     * @return JPanel
     */

    public static JPanel createLabelPanel(final ColourInterface colour,
                                          final int height)
        {
        final JPanel panelLabel;

        panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayoutFixed(panelLabel, BoxLayoutFixed.Y_AXIS));
        panelLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        panelLabel.setMinimumSize(new Dimension(WIDTH_LABEL, height));
        panelLabel.setPreferredSize(new Dimension(WIDTH_LABEL, height));
        panelLabel.setMaximumSize(new Dimension(WIDTH_LABEL, height));
        panelLabel.setBackground(colour.getColor());

        return (panelLabel);
        }


    /***********************************************************************************************
     *
     * @param colour
     *
     * @return JPanel
     */

    public static JPanel createDataPanel(final ColourInterface colour)
        {
        final JPanel panelData;

        panelData = new JPanel();
        panelData.setLayout(new BoxLayoutFixed(panelData, BoxLayoutFixed.Y_AXIS));
        panelData.setAlignmentY(Component.TOP_ALIGNMENT);
        panelData.setMinimumSize(new Dimension(0, 0));
        panelData.setBackground(colour.getColor());

        return (panelData);
        }


    /***********************************************************************************************
     * Create a JPanel for a chooser.
     *
     * @param colour
     * @param textfield
     * @param button
     *
     * @return JPanel
     */

    public static JPanel createBrowserPanel(final ColourInterface colour,
                                            final JTextField textfield,
                                            final JButton button)
        {
        final JPanel panelBrowser;

        panelBrowser = new JPanel();
        panelBrowser.setLayout(new BoxLayoutFixed(panelBrowser, BoxLayoutFixed.X_AXIS));
        panelBrowser.setBackground(colour.getColor());
        panelBrowser.setAlignmentX(0);
        panelBrowser.add(textfield, null);
        panelBrowser.add(Box.createRigidArea(new Dimension(4, 1)));
        panelBrowser.add(button, null);

        return (panelBrowser);
        }


    /***********************************************************************************************
      * Create a JPanel for two buttons.
      *
      * @param colour
      * @param left
      * @param right
      *
      * @return JPanel
      */

    public static JPanel createButtonPanel(final ColourInterface colour,
                                           final JButton left,
                                           final JButton right)
        {
        final JPanel panelButtons;

        panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayoutFixed.X_AXIS));

        if (left != null)
            {
            panelButtons.add(left);
            panelButtons.add(Box.createRigidArea(DIM_BUTTON_SPACER));
            }

        if (right != null)
            {
            panelButtons.add(right);
            }

        // Ensure that the buttons remain the correct height if there's only one
        if ((left == null) || (right == null))
            {
            panelButtons.add(Box.createRigidArea(DIM_BUTTON_HEIGHT));
            }

        panelButtons.setBackground(colour.getColor());

        return (panelButtons);
        }


    /***********************************************************************************************
     * Create a horizontal JPanel for a Vector of buttons.
     *
     * @param colour
     * @param buttons
     *
     * @return JPanel
     */

   public static JPanel createButtonPanel(final ColourInterface colour,
                                          final Vector buttons)
       {
       final JPanel panelButtons;
       final Iterator iterButtons;
       int i;

       panelButtons = new JPanel();
       panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayoutFixed.X_AXIS));

       if ((buttons != null)
           && (!buttons.isEmpty()))
           {
           iterButtons = buttons.iterator();
           i = 0;

           while (iterButtons.hasNext())
               {
               final JButton button = (JButton) iterButtons.next();

               if (button != null)
                   {
                   panelButtons.add(button);

                   if (i != buttons.size()-1)
                       {
                       panelButtons.add(Box.createRigidArea(DIM_BUTTON_SPACER));
                       }
                   }

               i++;
               }

           // Ensure that the buttons remain the correct height if there's only one
           if (i == 1)
               {
               panelButtons.add(Box.createRigidArea(DIM_BUTTON_HEIGHT));
               }
           }

       panelButtons.setBackground(colour.getColor());

       return (panelButtons);
       }


    /***********************************************************************************************
     *
     * @param width
     * @return Dimension
     */

    public static Dimension HORIZ_SPACER(final int width)
        {
        return (new Dimension(width, 1));
        }


    /***********************************************************************************************
     *
     * @param height
     * @return Dimension
     */

    public static Dimension VERT_SPACER(final int height)
        {
        return (new Dimension(1, height));
        }
    }
