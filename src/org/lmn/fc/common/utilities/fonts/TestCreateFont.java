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

package org.lmn.fc.common.utilities.fonts;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * How to include a custom font with your application.<br> Demonstrate use of
 * java.awt.Font.createFont<br> Accessing an uninstalled Font bundled in a jar
 * or on the classpath.<br> Note this technique is illegal if you don't have
 * licensing permission from the font designer. This only works in Swing!
 * <p/>
 * http://mindprod.com/jgloss/font.html
 *
 * @author Roedy Green
 */

public class TestCreateFont
    {
    /**
     * Debugging harness for a Frame
     *
     * @param args command line arguments are ignored.
     */
    public static void main(String args[])
        {
        SwingUtilities.invokeLater(new Runnable()
            {
            public void run()
                {
                final JFrame frame = new JFrame();

                // Uninstalled font is a resource on classpath or in jar
                try
                    {
                    InputStream fontStream = TestCreateFont.class.getResourceAsStream("Anniebtn.ttf");

                    if (fontStream == null)
                        {
                        System.out.println("NULL stream");
                        }

                    Font onePoint = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    fontStream.close();
                    Font snowFont = onePoint.deriveFont(Font.PLAIN, 18);
                    System.out.println(snowFont);

                    JLabel label = new JLabel("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                    label.setFont(snowFont);

                    frame.getContentPane().add(label);
                    frame.setSize(300, 100);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.validate();
                    frame.setVisible(true);
                    }
                catch (FontFormatException e)
                    {
                    System.err
                        .println("FontFormaException: " + e.getMessage());
                    System.exit(1);
                    }
                catch (IOException e)
                    {
                    System.err.println("IOException: " + e.getMessage());
                    System.exit(1);
                    }
                }
            });
        }
    }
