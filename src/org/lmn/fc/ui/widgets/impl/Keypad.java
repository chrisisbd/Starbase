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

//------------------------------------------------------------------------------
// A 10-digit numeric keypad, with configurable font
//------------------------------------------------------------------------------
// Revision History
//
//  07-03-00    LMN created file
//
//------------------------------------------------------------------------------
// Widgets package

package org.lmn.fc.ui.widgets.impl;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.ui.widgets.KeypadListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

//------------------------------------------------------------------------------

public class Keypad extends JPanel
    {
    private Vector listeners = new Vector();    // KeypadEvent listeners

    private Dimension dimKeypadSize;
    private String strFontName;
    private int intFontSize;
    private Font fontKeypad;
    private String strDelimiter;        // Decimal point, colon etc.

    private GridLayout gridLayout1 = new GridLayout();
    private JButton jButton0 = new JButton();
    private JButton jButton1 = new JButton();
    private JButton jButton2 = new JButton();
    private JButton jButton3 = new JButton();
    private JButton jButton4 = new JButton();
    private JButton jButton5 = new JButton();
    private JButton jButton6 = new JButton();
    private JButton jButton7 = new JButton();
    private JButton jButton8 = new JButton();
    private JButton jButton9 = new JButton();
    private JButton jButtonDelimiter = new JButton();
    private JButton jButtonSign = new JButton();

    private boolean boolDebugMode;      // Controls debug messages

    //--------------------------------------------------------------------------
    // Class Methods
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public Keypad(Dimension dimensionKeypad,
                  String fontName,
                  int fontSize)
        {
        // Make the JPanel
        super();

        this.dimKeypadSize = dimensionKeypad;
        this.strFontName = fontName;
        this.intFontSize = fontSize;
        this.fontKeypad = new Font(strFontName, Font.PLAIN, intFontSize);
        this.strDelimiter = ".";

        try
            {
            initialiseKeypad();
            }

        catch(Exception exception)
            {
//            ExceptionLibrary.handleCoreException(exception,
//                                             this.getClass().getName(),
//                                             ExceptionLibrary.EXCEPTION_FAILTOCREATEKEYPAD,
//                                             ExceptionLibrary.STATUS_FATAL);
            }
        }

    //--------------------------------------------------------------------------
    // Instance Methods
    //--------------------------------------------------------------------------

    private void initialiseKeypad() throws Exception
        {
        gridLayout1.setColumns(3);
        gridLayout1.setRows(4);
        gridLayout1.setHgap(2);
        gridLayout1.setVgap(2);

        this.setBackground(new Color(199, 199, 199));

        jButton0.setFont(fontKeypad);
        jButton1.setFont(fontKeypad);
        jButton2.setFont(fontKeypad);
        jButton3.setFont(fontKeypad);
        jButton4.setFont(fontKeypad);
        jButton5.setFont(fontKeypad);
        jButton6.setFont(fontKeypad);
        jButton7.setFont(fontKeypad);
        jButton8.setFont(fontKeypad);
        jButton9.setFont(fontKeypad);
        jButtonDelimiter.setFont(fontKeypad);
        jButtonSign.setFont(fontKeypad);

        jButton0.setText("0");
        jButton1.setText("1");
        jButton2.setText("2");
        jButton3.setText("3");
        jButton4.setText("4");
        jButton5.setText("5");
        jButton6.setText("6");
        jButton7.setText("7");
        jButton8.setText("8");
        jButton9.setText("9");
        jButtonDelimiter.setText(strDelimiter);
        jButtonSign.setText("-");

        this.setLayout(gridLayout1);
        this.add(jButton7, null);
        this.add(jButton8, null);
        this.add(jButton9, null);
        this.add(jButton4, null);
        this.add(jButton5, null);
        this.add(jButton6, null);
        this.add(jButton1, null);
        this.add(jButton2, null);
        this.add(jButton3, null);
        this.add(jButtonSign, null);
        this.add(jButton0, null);
        this.add(jButtonDelimiter, null);

        this.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                          BorderFactory.createLoweredBevelBorder()));

        this.setPreferredSize(dimKeypadSize);
        this.setMaximumSize(dimKeypadSize);
        this.setMinimumSize(dimKeypadSize);

        //----------------------------------------------------------------------
        // Add the Action Listeners

        jButton0.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "0",
                                  boolDebugMode);
                }
            });

        jButton1.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "1",
                                  boolDebugMode);
                }
            });

        jButton2.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "2",
                                  boolDebugMode);
                }
            });

        jButton3.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "3",
                                  boolDebugMode);
                }
            });

        jButton4.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "4",
                                  boolDebugMode);
                }
            });

        jButton5.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "5",
                                  boolDebugMode);
                }
            });

        jButton6.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "6",
                                  boolDebugMode);
                }
            });

        jButton7.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "7",
                                  boolDebugMode);
                }
            });

        jButton8.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "8",
                                  boolDebugMode);
                }
            });

        jButton9.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "9",
                                  boolDebugMode);
                }
            });

        jButtonDelimiter.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  strDelimiter,
                                  boolDebugMode);
                }
            });

        jButtonSign.addActionListener(new java.awt.event.ActionListener()
            {
            public void actionPerformed(ActionEvent event)
                {
                notifyKeypadEvent(this,
                                  this.getClass().getName(),
                                  "-",
                                  boolDebugMode);
                }
            });
        }

    //--------------------------------------------------------------------------
    // Set the font used on the buttons

    public void setFont(String strFontName, int intFontSize)
        {
        fontKeypad = new Font(strFontName, Font.PLAIN, intFontSize);

        jButton0.setFont(fontKeypad);
        jButton1.setFont(fontKeypad);
        jButton2.setFont(fontKeypad);
        jButton3.setFont(fontKeypad);
        jButton4.setFont(fontKeypad);
        jButton5.setFont(fontKeypad);
        jButton6.setFont(fontKeypad);
        jButton7.setFont(fontKeypad);
        jButton8.setFont(fontKeypad);
        jButton9.setFont(fontKeypad);
        jButtonDelimiter.setFont(fontKeypad);
        jButtonSign.setFont(fontKeypad);
        this.repaint();
        }


    //--------------------------------------------------------------------------
    // Get the font used on the buttons

    public Font getFont()
        {
        return(fontKeypad);
        }


    //--------------------------------------------------------------------------
    // Control the keypad buttons

    public void setEnabled(boolean enable)
        {
        jButton0.setEnabled(enable);
        jButton1.setEnabled(enable);
        jButton2.setEnabled(enable);
        jButton3.setEnabled(enable);
        jButton4.setEnabled(enable);
        jButton5.setEnabled(enable);
        jButton6.setEnabled(enable);
        jButton7.setEnabled(enable);
        jButton8.setEnabled(enable);
        jButton9.setEnabled(enable);
        jButtonDelimiter.setEnabled(enable);
        jButtonSign.setEnabled(enable);
        this.repaint();
        }


    //--------------------------------------------------------------------------
    // Set the delimiter string

    public void setDelimiter(String delimiter)
        {
        strDelimiter = delimiter;
        jButtonDelimiter.setText(strDelimiter);
        this.repaint();
        }


    //--------------------------------------------------------------------------
    // Get the delimiter string

    public String getDelimiter()
        {
        return(strDelimiter);
        }


    //--------------------------------------------------------------------------
    // Get the Debug Mode flag

    public boolean getDebugMode()
        {
        return(boolDebugMode);
        }


    //--------------------------------------------------------------------------
    // Set the Debug Mode flag

    public void setDebugMode(boolean flag)
        {
        boolDebugMode = flag;
        }


    //--------------------------------------------------------------------------
    // Show a debug message

    private void showDebugMessage(String message)
        {
        String strSeparator;

        if (boolDebugMode)
            {
            if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = " ";
                }

            System.out.println(Chronos.timeNow() + " "
                               + this.getClass().getName()
                               + strSeparator
                               + message);
            }
        }


    //--------------------------------------------------------------------------
    // Events
    //--------------------------------------------------------------------------
    // Notify all listeners of KeypadEvents

    public void notifyKeypadEvent(Object objectsource,
                                  String sourcename,
                                  String eventdata,
                                  boolean debugmode)
        {
        final List<KeypadListener> keypadListeners;
        final KeypadEvent keypadEvent;

        // Create a Thread-safe List of Listeners
        keypadListeners = new CopyOnWriteArrayList<KeypadListener>(this.listeners);

        // Create a KeypadEvent
        keypadEvent = new KeypadEvent(objectsource,
                                      sourcename,
                                      eventdata,
                                      debugmode);
        // Fire the event to every listener
        synchronized(keypadListeners)
            {
            for (int i = 0; i < keypadListeners.size(); i++)
                {
                final KeypadListener keypadListener;

                keypadListener = keypadListeners.get(i);
                keypadListener.keypadEvent(keypadEvent);
                }
            }
        }


    //--------------------------------------------------------------------------
    // Add a listener for this event

    public void addKeypadListener(KeypadListener listener)
        {
        listeners.addElement(listener);
        }


    //--------------------------------------------------------------------------
    // Remove a listener for this event

    public void removeKeypadListener(KeypadListener listener)
        {
        listeners.removeElement(listener);
        }

    }


//------------------------------------------------------------------------------
// End of File

