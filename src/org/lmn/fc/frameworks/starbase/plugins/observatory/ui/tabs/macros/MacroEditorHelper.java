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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MacroUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandProcessorUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***************************************************************************************************
 * MacroEditorHelper.
 */

public final class MacroEditorHelper
    {
    // String Resources
    private static final String MSG_MACRO_INVALID = "Current Command selection is not a Macro, or unable to edit";

    private static final String BUTTON_ADD_SELECTION = "Add Selection to Macro";
    private static final String BUTTON_MOVE_UP = "Move Up";
    private static final String BUTTON_MOVE_DOWN = "Move Down";
    private static final String BUTTON_DELETE_LINE = "Delete Line";
    private static final String BUTTON_EDIT_COMMENT = "Edit Comment";
    private static final String BUTTON_EDIT_LABEL = "Edit Label";
    private static final String BUTTON_VALIDATE = "Validate Macro";

    private static final String TOOLTIP_ADD_SELECTION = "Add the current selection below the current step";
    private static final String TOOLTIP_MOVE_UP = "Move this line Up by one";
    private static final String TOOLTIP_MOVE_DOWN = "Move this line Down by one";
    private static final String TOOLTIP_DELETE_LINE = "Delete the current line";
    private static final String TOOLTIP_EDIT_COMMENT = "Add or Edit a Comment";
    private static final String TOOLTIP_EDIT_LABEL = "Add or Edit a Label";
    private static final String TOOLTIP_VALIDATE = "Check that this Macro can be run in this Observatory";

    private static final String TOOLTIP_TEXT_FIELD = "Enter a Comment or Label here";

    public static final Dimension DIM_BUTTON_EDIT = new Dimension(CommandProcessorUtilities.WIDTH_BUTTON,
                                                                  CommandProcessorUtilities.HEIGHT_BUTTON);


    /***********************************************************************************************
     * Create the AddSelection button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createAddSelectionButton(final CommandProcessorContextInterface context,
                                                   final MacroUIComponentInterface ui)
        {
        final JButton buttonAddSelection;

        // The EditMacro button
        buttonAddSelection = new JButton(BUTTON_ADD_SELECTION);

        buttonAddSelection.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonAddSelection.setMinimumSize(DIM_BUTTON_EDIT);
        buttonAddSelection.setMaximumSize(DIM_BUTTON_EDIT);
        buttonAddSelection.setPreferredSize(DIM_BUTTON_EDIT);
        buttonAddSelection.setFont(context.getFontData().getFont());
        buttonAddSelection.setForeground(context.getColourData().getColor());

        buttonAddSelection.setEnabled(true);
        buttonAddSelection.setToolTipText(TOOLTIP_ADD_SELECTION);

        buttonAddSelection.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("add selection!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setAddSelectionButton(buttonAddSelection);

        return (buttonAddSelection);
        }


    /***********************************************************************************************
     * Create the MoveUp button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createMoveUpButton(final CommandProcessorContextInterface context,
                                             final MacroUIComponentInterface ui)
        {
        final JButton buttonMoveUp;

        // The MoveUp button
        buttonMoveUp = new JButton(BUTTON_MOVE_UP);

        buttonMoveUp.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonMoveUp.setMinimumSize(DIM_BUTTON_EDIT);
        buttonMoveUp.setMaximumSize(DIM_BUTTON_EDIT);
        buttonMoveUp.setPreferredSize(DIM_BUTTON_EDIT);
        buttonMoveUp.setFont(context.getFontData().getFont());
        buttonMoveUp.setForeground(context.getColourData().getColor());

        buttonMoveUp.setEnabled(true);
        buttonMoveUp.setToolTipText(TOOLTIP_MOVE_UP);

        buttonMoveUp.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("move up!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setMoveUpButton(buttonMoveUp);

        return (buttonMoveUp);
        }


    /***********************************************************************************************
     * Create the MoveDown button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createMoveDownButton(final CommandProcessorContextInterface context,
                                               final MacroUIComponentInterface ui)
        {
        final JButton buttonMoveDown;

        // The MoveDown button
        buttonMoveDown = new JButton(BUTTON_MOVE_DOWN);

        buttonMoveDown.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonMoveDown.setMinimumSize(DIM_BUTTON_EDIT);
        buttonMoveDown.setMaximumSize(DIM_BUTTON_EDIT);
        buttonMoveDown.setPreferredSize(DIM_BUTTON_EDIT);
        buttonMoveDown.setFont(context.getFontData().getFont());
        buttonMoveDown.setForeground(context.getColourData().getColor());

        buttonMoveDown.setEnabled(true);
        buttonMoveDown.setToolTipText(TOOLTIP_MOVE_DOWN);

        buttonMoveDown.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("move down!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setMoveDownButton(buttonMoveDown);

        return (buttonMoveDown);
        }


    /***********************************************************************************************
     * Create the DeleteLine button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createDeleteLineButton(final CommandProcessorContextInterface context,
                                                 final MacroUIComponentInterface ui)
        {
        final JButton buttonDeleteLine;

        // The DeleteLine button
        buttonDeleteLine = new JButton(BUTTON_DELETE_LINE);

        buttonDeleteLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonDeleteLine.setMinimumSize(DIM_BUTTON_EDIT);
        buttonDeleteLine.setMaximumSize(DIM_BUTTON_EDIT);
        buttonDeleteLine.setPreferredSize(DIM_BUTTON_EDIT);
        buttonDeleteLine.setFont(context.getFontData().getFont());
        buttonDeleteLine.setForeground(context.getColourData().getColor());

        buttonDeleteLine.setEnabled(true);
        buttonDeleteLine.setToolTipText(TOOLTIP_DELETE_LINE);

        buttonDeleteLine.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("delete line!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setDeleteLineButton(buttonDeleteLine);

        return (buttonDeleteLine);
        }


    /***********************************************************************************************
     * Create the EditComment button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createEditCommentButton(final CommandProcessorContextInterface context,
                                                 final MacroUIComponentInterface ui)
        {
        final JButton buttonEditComment;

        // The EditMacro button
        buttonEditComment = new JButton(BUTTON_EDIT_COMMENT);

        buttonEditComment.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonEditComment.setMinimumSize(DIM_BUTTON_EDIT);
        buttonEditComment.setMaximumSize(DIM_BUTTON_EDIT);
        buttonEditComment.setPreferredSize(DIM_BUTTON_EDIT);
        buttonEditComment.setFont(context.getFontData().getFont());
        buttonEditComment.setForeground(context.getColourData().getColor());

        buttonEditComment.setEnabled(true);
        buttonEditComment.setToolTipText(TOOLTIP_EDIT_COMMENT);

        buttonEditComment.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("edit comment!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setEditCommentButton(buttonEditComment);

        return (buttonEditComment);
        }


    /***********************************************************************************************
     * Create the EditLabel button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createEditLabelButton(final CommandProcessorContextInterface context,
                                                final MacroUIComponentInterface ui)
        {
        final JButton buttonEditLabel;

        // The EditMacro button
        buttonEditLabel = new JButton(BUTTON_EDIT_LABEL);

        buttonEditLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonEditLabel.setMinimumSize(DIM_BUTTON_EDIT);
        buttonEditLabel.setMaximumSize(DIM_BUTTON_EDIT);
        buttonEditLabel.setPreferredSize(DIM_BUTTON_EDIT);
        buttonEditLabel.setFont(context.getFontData().getFont());
        buttonEditLabel.setForeground(context.getColourData().getColor());

        buttonEditLabel.setEnabled(true);
        buttonEditLabel.setToolTipText(TOOLTIP_EDIT_LABEL);

        buttonEditLabel.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("edit label!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setEditLabelButton(buttonEditLabel);

        return (buttonEditLabel);
        }


    /***********************************************************************************************
     * Create the Validate button.
     *
     * @param context
     * @param ui
     *
     * @return JButton
     */

    public static JButton createValidateButton(final CommandProcessorContextInterface context,
                                               final MacroUIComponentInterface ui)
        {
        final JButton buttonValidate;

        // The EditMacro button
        buttonValidate = new JButton(BUTTON_VALIDATE);

        buttonValidate.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonValidate.setMinimumSize(DIM_BUTTON_EDIT);
        buttonValidate.setMaximumSize(DIM_BUTTON_EDIT);
        buttonValidate.setPreferredSize(DIM_BUTTON_EDIT);
        buttonValidate.setFont(context.getFontData().getFont());
        buttonValidate.setForeground(context.getColourData().getColor());

        buttonValidate.setEnabled(true);
        buttonValidate.setToolTipText(TOOLTIP_VALIDATE);

        buttonValidate.addActionListener(new ActionListener()
            {
            public synchronized void actionPerformed(final ActionEvent event)
                {
                if ((context != null)
                    && (context.getSelectedMacro() != null)
                    && (context.getSelectedMacro().size() == 1))
                    {
                    System.out.println("validate!");
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(MSG_MACRO_INVALID);
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });

        context.setValidateButton(buttonValidate);

        return (buttonValidate);
        }


    /***********************************************************************************************
     * Create the TextEntry panel, for entering Comments and Labels etc.
     *
     * @param context
     * @param ui
     *
     * @return JPanel
     */

    public static JPanel createTextEntryPanel(final CommandProcessorContextInterface context,
                                              final MacroUIComponentInterface ui)
        {
        final JPanel textPanel;
        final JTextField textField;
        final DocumentListener listener;

        textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));

        // Allow free-text entry
        textField = new JTextField();
        ui.setTextEntryField(textField);

        textField.setFont(context.getFontData().getFont());
        textField.setForeground(context.getColourData().getColor().darker());
        textField.setToolTipText(TOOLTIP_TEXT_FIELD);
        textField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        // Text Field Listener
        listener = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                System.out.println("insertUpdate");
                }

            public void removeUpdate(final DocumentEvent event)
                {
                System.out.println("removeUpdate");
                }

            public void changedUpdate(final DocumentEvent event)
                {
                System.out.println("changedUpdate");
                }
            };

        textField.getDocument().addDocumentListener(listener);

        textPanel.add(textField, BorderLayout.CENTER);

        return (textPanel);
        }
    }
