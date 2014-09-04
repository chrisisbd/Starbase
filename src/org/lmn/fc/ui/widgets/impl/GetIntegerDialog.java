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
//  15-03-05    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.widgets.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;


/***************************************************************************************************
 * Get an integer from the User.
 */

public final class GetIntegerDialog extends JDialog
                                    implements ActionListener,
                                               PropertyChangeListener,
                                               FrameworkStrings
    {
    // String Resources
    private static final String BUTTON_ENTER        = "Enter";
    private static final String BUTTON_CANCEL       = "Cancel";
    private static final String MSG_ENTRY_INVALID   = "Please enter an integer between";

    private Integer integerInput;
    private final JTextField textInput;
    private final int intMinimum;
    private final int intMaximum;

    private final JOptionPane optionPane;


    /***********************************************************************************************
     *
     * @param frame
     * @param title
     * @param messages
     * @param minimum
     * @param maximum
     */

    public GetIntegerDialog(final Frame frame,
                            final String title,
                            final String[] messages,
                            final int minimum,
                            final int maximum)
        {
        super(frame, true);

        intMinimum = minimum;
        intMaximum = maximum;
        setTitle(title);
        textInput = new JTextField(15);

        // Create an array of the text and components to be displayed
        final ArrayList listComponents;

        listComponents = new ArrayList();

        if (messages != null)
            {
            for (int i = 0; i < messages.length; i++)
                {
                listComponents.add(messages[i]);
                }
            }

        listComponents.add(textInput);

        // Create an array specifying the number of dialog buttons and their text
        final Object[] options = {BUTTON_ENTER, BUTTON_CANCEL};

        // Create the JOptionPane
        optionPane = new JOptionPane(listComponents.toArray(),
                                     JOptionPane.QUESTION_MESSAGE,
                                     JOptionPane.YES_NO_OPTION,
                                     null,
                                     options,
                                     options[0]);

        // Make this dialog display it
        setContentPane(optionPane);

        // Handle window closing correctly
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter()
            {
            public void windowClosing(final WindowEvent event)
                {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
                }
            });

        // Ensure that the text field always gets the first focus
        addComponentListener(new ComponentAdapter()
            {
            public void componentShown(final ComponentEvent event)
                {
                textInput.requestFocusInWindow();
                }
            });

        // Register an event handler that puts the text into the option pane
        textInput.addActionListener(this);

        //  Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);

        // Causes this Window to be sized to fit the preferred size and layouts of its subcomponents.
        // If the window and/or its owner are not yet displayable,
        // both are made displayable before calculating the preferred size.
        // The Window will be validated after the preferredSize is calculated.
        pack();
        setResizable(false);

        // Now that we have the preferred sizes, we can work out where to put the JDialog
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension windowSize = getPreferredSize();
        setLocation((screenSize.width /2 ) - (windowSize.width / 2),
                    (screenSize.height / 2) - (windowSize.height / 2));
        }


    /***********************************************************************************************
     * Get the Integer value as entered by the User,
     * or <code>null</code> if the entry was invalid.
     *
     * @return Integer
     */

    public final Integer getValidatedInteger()
        {
        return (this.integerInput);
        }


    /***********************************************************************************************
     * This method handles events for the text field.
     *
     * @param event
     */

    public void actionPerformed(final ActionEvent event)
        {
        optionPane.setValue(BUTTON_ENTER);
        }

    /**
     * This method reacts to state changes in the option pane.
     */
    public void propertyChange(final PropertyChangeEvent event)
        {
        final String strPropertyName = event.getPropertyName();

        if (isVisible()
            && (event.getSource() == optionPane)
            && (JOptionPane.VALUE_PROPERTY.equals(strPropertyName)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(strPropertyName)))
            {
            final Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE)
                {
                // Ignore reset
                return;
                }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (BUTTON_ENTER.equals(value))
                {
                try
                    {
                    final int intInput;

                    intInput = Integer.parseInt(textInput.getText());

                    if ((intInput < intMinimum)
                        || (intInput > intMaximum))
                        {
                        throw new NumberFormatException(EXCEPTION_PARAMETER_RANGE);
                        }

                    integerInput = new Integer(intInput);
                    clearAndHide();
                    }

                catch (NumberFormatException exception)
                    {
                    // The input text was invalid
                    textInput.selectAll();
                    JOptionPane.showMessageDialog(GetIntegerDialog.this,
                                                  MSG_ENTRY_INVALID
                                                      + SPACE
                                                      + intMinimum
                                                      + COMMA
                                                      + SPACE
                                                      + intMaximum,
                                                  getTitle(),
                                                  JOptionPane.ERROR_MESSAGE);
                    integerInput = null;
                    textInput.requestFocusInWindow();
                    }
                }
            else
                {
                // The user closed the dialog or clicked cancel
                integerInput = null;
                clearAndHide();
                }
            }
        }


    /***********************************************************************************************
     * Clear the User's input and hide the JDialog.
     */

    private void clearAndHide()
        {
        textInput.setText(null);
        setVisible(false);
        }
    }


//-------------------------------------------------------------------------------------------------
// End of file
