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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.jupiter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.jupiter.Maths.*;


public final class Moons
        extends JComponent
        implements Runnable,
                   ComponentListener
    {
    /**
     * 1970.0 UT was JD 2440587.5
     */
    private static final double JD1970_DAYS = 2440587.5;
    private static final double MS_PER_DAY = 24.0 * 60.0 * 60.0 * 1000.0;
    private static final double MS_INCREMENT = 60.0 * 60.0 * 1000.0; // 1 hour

    private static final int PX_PER_JER = 13; // Pixels per Jupiter equatorial radius
    private static final int MOON_RADIUS_PX = 2;
    private static final int MOON_DIAMETER_PX = MOON_RADIUS_PX << 1;
    private static final Color BACKGROUND = Color.BLACK;
    private static final Color JUPITER_COLOUR = Color.LIGHT_GRAY;

    private static final Color[] COLOURS = new Color[]
            {
                    Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
            };


    private static final Font FONT = new Font(Font.SANS_SERIF,
                                                          Font.BOLD,
                                                          12);

    private static final int JUPITER_WIDTH_PX = 32; //26;
    private static final int JUPITER_HEIGHT_PX = 32; //22;
//    private static final int IMWIDTH = 750;
//    private static final int IMHEIGHT = 100;
//    private static final int X0 = IMWIDTH / 2;
//    private static final int Y0 = IMHEIGHT / 2;
//    private static final int JUPITER_X0 = X0 - JUPITER_WIDTH_PX / 2;
//    private static final int JUPITER_Y0 = Y0 - JUPITER_HEIGHT_PX / 2;

    // Injections
    private final int intWidth;
    private final int intHeight;

    private final int intX0;
    private final int intY0;
    private final int intJupiterX0;
    private final int intJupiterY0;

    private BufferedImage bim;

    private double msSince1970;
    private boolean paused;
    private boolean forward;
    private boolean firstPlot;

    private final int[] xPxPrev;
    private final int[] yPxPrev;


    /***********************************************************************************************
     * Moons.
     *
     * @param width
     * @param height
     */

    public Moons(final int width,
                 final int height)
        {
        intWidth = width;
        intHeight = height;
        intX0 = width >> 1;
        intY0 = height >> 1;
        intJupiterX0 = intX0 - (JUPITER_WIDTH_PX >> 1);
        intJupiterY0 = intY0 - (JUPITER_HEIGHT_PX >> 1);

        msSince1970 = (double) new Date().getTime();
        paused = false;
        forward = true;
        firstPlot = true;
        xPxPrev = new int[4];
        yPxPrev = new int[4];


        addComponentListener(this);

        }


    public void reset()
        {
        msSince1970 = (double) new Date().getTime();
        bim = new BufferedImage(getPanelWidth(),
                                               getPanelHeight(),
                                               BufferedImage.TYPE_INT_RGB);
        firstPlot = true;
        plot();
        } // reset


    public boolean isPaused()
        {
        return paused;
        }


    public void setPaused(final boolean b)
        {
        paused = b;
        }


    public boolean isForward()
        {
        return forward;
        }


    public void setForward(final boolean b)
        {
        forward = b;
        }


    /**
     * Implementing Runnable
     */
    //@Override
    public void run()
        {
        setSize(getPanelWidth(),
                getPanelHeight());
        setPreferredSize(new Dimension(getPanelWidth(),
                                                getPanelHeight()));
        bim = new BufferedImage(getPanelWidth(),
                                               getPanelHeight(),
                                               BufferedImage.TYPE_INT_RGB);
        boolean interrupted = false;

        while (!interrupted)
            {
            if (!paused)
                {
                plot();
                }

            try
                {
                Thread.sleep(500);
                }
            catch (InterruptedException ex)
                {
                interrupted = true;
                }
            }
        } // run


    /**
     * Calculate once (current step) and display.
     */
    private void plot()
        {
        final double vJDE = JD1970_DAYS + msSince1970 / MS_PER_DAY;
        final double vd = vJDE - 2451545.0; // Since 2000 Jan 1.5
        final double vVdegs = in360(172.74 + 0.00111588 * vd);
        final double v329sinV = 0.329 * sin(vVdegs);
        final double vMdegs = in360(357.529 + 0.9856003 * vd);
        final double vNdegs = in360(20.020 + 0.0830853 * vd + v329sinV);
        final double vJdegs = in360(66.115 + 0.9025179 * vd - v329sinV);
        final double vAdegs = 1.915 * sin(vMdegs) + 0.020 * sin(2.0 * vMdegs);
        final double vBdegs = 5.555 * sin(vNdegs) + 0.168 * sin(2.0 * vNdegs);
        final double vKdegs = vJdegs + vAdegs - vBdegs;
        final double vRau = 1.00014 - 0.01671 * cos(vMdegs) - 0.00014 * cos(2.0 * vMdegs);
        final double vrau = 5.20872 - 0.25208 * cos(vNdegs) - 0.00611 * cos(2.0 * vNdegs);
        final double vDeltaau = Math.sqrt(vrau * vrau + vRau * vRau - 2.0 * vrau * vRau * cos(vKdegs));
        final double psiDegs = asin(vRau * sin(vKdegs) / vDeltaau); // Range -12 .. +12
        final double v173 = vd - vDeltaau / 173.0;
        final double psiBdegs = psiDegs - vBdegs;
        final double lambdaDegs = 34.35 + 0.083091 * vd + v329sinV + vBdegs;
        final double vDSdegs = 3.12 * sin(lambdaDegs + 42.8);
        final double vDEdegs = vDSdegs - 2.22 * sin(psiDegs) * cos(lambdaDegs + 22.0) -
                1.3 * (vrau - vDeltaau) / vDeltaau * sin(lambdaDegs - 100.5);
        double vu1degs = in360(163.8069 + 203.4058646 * v173 + psiBdegs);
        double vu2degs = in360(358.4140 + 101.2916335 * v173 + psiBdegs);
        double vu3degs = in360(5.7176 + 50.2345180 * v173 + psiBdegs);
        double vu4degs = in360(224.8092 + 21.4879800 * v173 + psiBdegs);
        final double vGdegs = 331.18 + 50.310482 * v173;
        final double vHdegs = 87.45 + 21.569231 * v173;
        // In the following, units jer = Jupiter equatorial radius
        final double vr1jer = 5.9057 - 0.0244 * cos(2.0 * (vu1degs - vu2degs));
        final double vr2jer = 9.3966 - 0.0882 * cos(2.0 * (vu2degs - vu3degs));
        final double vr3jer = 14.9883 - 0.0216 * cos(vGdegs);
        final double vr4jer = 26.3627 - 0.1939 * cos(vHdegs);
        vu1degs += 0.473 * sin(2.0 * (vu1degs - vu2degs));
        vu2degs += 1.065 * sin(2.0 * (vu2degs - vu3degs));
        vu3degs += 0.165 * sin(vGdegs);
        vu4degs += 0.843 * sin(vHdegs);
        final double sinDE = sin(vDEdegs);

        final double[] vXjer = new double[4];
        final double[] vYjer = new double[4];
        vXjer[0] = vr1jer * sin(vu1degs);
        vYjer[0] = -vr1jer * cos(vu1degs) * sinDE;
        vXjer[1] = vr2jer * sin(vu2degs);
        vYjer[1] = -vr2jer * cos(vu2degs) * sinDE;
        vXjer[2] = vr3jer * sin(vu3degs);
        vYjer[2] = -vr3jer * cos(vu3degs) * sinDE;
        vXjer[3] = vr4jer * sin(vu4degs);
        vYjer[3] = -vr4jer * cos(vu4degs) * sinDE;

        draw(msSince1970,
             vXjer,
             vYjer);

        if (forward)
            {
            msSince1970 += MS_INCREMENT;
            }
        else
            {
            msSince1970 -= MS_INCREMENT;
            }
        } // plot


    private void draw(final double since1970,
                      final double[] vXjer,
                      final double[] vYjer)
        {
        final Graphics2D g2 = (Graphics2D) bim.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2.setFont(FONT);
        g2.setPaint(BACKGROUND);
        final int nameY = 12;
        final int dateY = getPanelHeight() - 8;
        g2.fillRect(0,
                    dateY - 12,
                    getPanelWidth(),
                    20); // Clear date string
        g2.fillRect(0,
                    nameY - 12,
                    getPanelWidth(),
                    20); // Clear names
        g2.setPaint(COLOURS[0]);
        g2.drawString("Io",
                      160,
                      nameY);
        g2.setPaint(COLOURS[1]);
        g2.drawString("Europa",
                      260,
                      nameY);
        g2.setPaint(COLOURS[2]);
        g2.drawString("Ganymede",
                      410,
                      nameY);
        g2.setPaint(COLOURS[3]);
        g2.drawString("Callisto",
                      560,
                      nameY);

        if (firstPlot)
            {
            firstPlot = false;
            }
        else
            {
            for (int i = 0;
                 i < vXjer.length;
                 i++)
                {
                eraseMoon(g2,
                          xPxPrev[i],
                          yPxPrev[i],
                          COLOURS[i]);
                }
            }

        for (int i = 0;
             i < vXjer.length;
             i++)
            {
            final int xPx = (int) Math.round(getX0() + vXjer[i] * PX_PER_JER);
            final int yPx = (int) Math.round(getY0() - vYjer[i] * PX_PER_JER);
            drawMoon(g2,
                     xPx,
                     yPx,
                     COLOURS[i]);
//System.out.println (i + " (" + xPx + ", " + yPx + ")");
            xPxPrev[i] = xPx;
            yPxPrev[i] = yPx;
            }

        g2.setPaint(JUPITER_COLOUR);
        g2.fillOval(getJupiterX0(),
                    getJupiterY0(),
                    JUPITER_WIDTH_PX,
                    JUPITER_HEIGHT_PX);

        final Date date = new Date((long) since1970);
        g2.setPaint(Color.WHITE);
        g2.drawString(date.toString(),
                      290,
                      dateY);
        repaint();
        } // draw


    private static void drawMoon(final Graphics2D g2,
                          final int xPx,
                          final int yPx,
                          final Color colour)
        {
        g2.setPaint(colour);
        g2.fillOval(xPx - MOON_RADIUS_PX,
                    yPx - MOON_RADIUS_PX,
                    MOON_DIAMETER_PX,
                    MOON_DIAMETER_PX);
        } // drawMoon


    private static void eraseMoon(final Graphics2D g2,
                           final int xPx,
                           final int yPx,
                           final Color colour)
        {
        g2.setPaint(BACKGROUND);
        g2.fillOval(xPx - MOON_RADIUS_PX,
                    yPx - MOON_RADIUS_PX,
                    MOON_DIAMETER_PX,
                    MOON_DIAMETER_PX);
        g2.setPaint(colour);
        g2.fillOval(xPx,
                    yPx,
                    1,
                    1);
        } // eraseMoon


    @Override
    public void paintComponent(final Graphics g)
        {
        final Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(bim,
                     0,
                     0,
                     this);
        } // paint


    public static GregorianCalendar toGregorianCalendar(final double jd)
        {
        final double jd1970 = jd - JD1970_DAYS;
        final long javaTime = (long) (jd1970 * MS_PER_DAY); // ms, UT

        final GregorianCalendar calendar = new GregorianCalendar();
        // Uses default locale - no time zone conversion needed

        calendar.setTimeInMillis(javaTime);
        return calendar;
        } // toGregorianCalendar


    private int getPanelWidth()
        {
        return intWidth;
        }


    private int getPanelHeight()
        {
        return intHeight;
        }


    private int getX0()
        {
        return intX0;
        }


    private int getY0()
        {
        return intY0;
        }


    private int getJupiterX0()
        {
        return intJupiterX0;
        }


    private int getJupiterY0()
        {
        return intJupiterY0;
        }
    /**
     * Invoked when the component's size changes.
     */
    public void componentResized(ComponentEvent e)
        {
        //System.out.println("MOONS RESIZED");
        }


    /**
     * Invoked when the component's position changes.
     */
    public void componentMoved(ComponentEvent e)
        {
        //System.out.println("MOONS MOVED");
        }


    /**
     * Invoked when the component has been made visible.
     */
    public void componentShown(ComponentEvent e)
        {
        //System.out.println("MOONS SHOWN");
        }


    /**
     * Invoked when the component has been made invisible.
     */
    public void componentHidden(ComponentEvent e)
        {
        //System.out.println("MOONS HIDDEN");
        }
    } // Moons
