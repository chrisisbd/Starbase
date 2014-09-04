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

package org.lmn.fc.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * A splash screen that produces the illusion of transparency for a top level Container.
 *
 * @author F. Fleischer
 */
public class TestWindowTransparency extends JWindow
                                    implements Runnable
    {
    /**
     * File path of the image to display, preferrably .gif or .png. Best choice
     * would be the png-24 format, since it supports 256 transparency levels.
     */
    private String imageFile;

    /**
     * A writable off screen image.
     */
    private BufferedImage bufImage;

    /**
     * The rectangle to be captured.
     */
    private Rectangle rect;

    /**
     * True if initialization thread is running.
     */
    private boolean isAlive;

    public static void main(final String[] args)
        {
//        System.out.println("user.home=" + System.getProperty("user.home") );
//        System.out.println("user.dir=" + System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR) );
        TestWindowTransparency test = new TestWindowTransparency();
        }


    /**
     * Constructor for the SplashScreen object. Starts initialization and showing
     * of the splash screen immediately.
     */
    public TestWindowTransparency()
        {
        this.imageFile = "baarag.png";
        run();
        }

    /**
     * Starts the initialization thread of the SplashScreen.
     */
    public final void run()
        {
        isAlive = true;
        // use ImageIcon, so we don't need to use MediaTracker
        final Image image = new ImageIcon(imageFile).getImage();
        final int imageWidth = image.getWidth(this);
        final int imageHeight = image.getHeight(this);
        if (imageWidth > 0 && imageHeight > 0)
            {
            final int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            final int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().width;
            // a Rectangle centered on screen
            rect = new Rectangle((screenWidth - imageWidth) / 2, (screenHeight - imageHeight) / 2,
                                 imageWidth, imageHeight);
            // the critical lines, create a screen shot
            try
                {
                bufImage = new Robot().createScreenCapture(rect);
                }
            catch (AWTException e)
                {
                e.printStackTrace();
                }
            // obtain the graphics context from the BufferedImage
            final Graphics2D g2D = bufImage.createGraphics();
            // Draw the image over the screen shot
            g2D.drawImage(image, 0, 0, this);
            // draw the modified BufferedImage back into the same space
            setBounds(rect);
            // present our work :)
            show();
            }
        else
            {
            System.err.println("File " + imageFile + " was not found or is not an image file.");
            }
        isAlive = false;
        }

    /**
     * Disposes of the SplashScreen. To be called shortly before the main application
     * is ready to be displayed.
     *
     * @throws IllegalStateException Is thrown if the initialization thread
     *                               has not yet reached it's end.
     */
    public void close() throws IllegalStateException
        {
        if (!isAlive)
            {
            dispose();
            }
        else
            {
            // better not dispose a SplashScreen that has not been painted on screen yet.
            throw new IllegalStateException("SplashScreen not yet fully initialized.");
            }
        }

    /**
     * Overrides the paint() method of JWindow.
     *
     * @param g The graphics context
     */
    public void paint(final Graphics g)
        {
        final Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(bufImage, 0, 0, this);
        }
    }

