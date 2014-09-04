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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.smithchart;

import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;


public final class SmithChartPanel extends UIComponent
                                   implements UIComponentPlugin
    {
    private static final Color color0 = Color.black;
    private static int mouseX;
    private static int mouseY;

    private int intWidth;
    private int intHeight;
    private static int Xs;
    private static int Ys;
    private static int Xb;
    private static int Yb;
    private static int Xc;
    private static int Yc;
    private static int Ptx;
    private static int Pty;
    private double rad;
    private double rad1;
    private double rad2;
    private double rad3;
    private double rad4;
    private double rad5;
    private double rad6;
    private double rad7;
    private double radb;
    private double radc;
    private double XR;
    private double alpha;
    // --Commented out by Inspection (13/05/05 13:56): public double phi;
    private double theta;
    private double beta;
    private double Xt;
    private double Yt;
    private double beta1;
    private double theta1;
    private double comp;
    private Polygon polygon;
    private static final double[] ba = new double[120];
    private static Point pts;
    private static final double[] dog = new double[2];

    private final Font f1 = new Font("TimesRoman", Font.BOLD, 24);
    private final Font f2 = new Font("TimesRoman", Font.PLAIN, 12);
    private final Font f3 = new Font("TimesRoman", Font.PLAIN, 10);

    private static boolean State1 = false;
    private static boolean State2 = false;
    private static boolean State3 = false;


    private static double VSWR(final double rr, final double ri)
        {
        final double VR;

        VR = (1 + Math.sqrt(rr * rr + ri * ri)) / (1 - Math.sqrt(rr * rr + ri * ri));

        return VR;
        }

    private static double Resistance(final double rr, final double ri)
        {
        final double R;

        R = (1 - rr * rr - ri * ri) / ((1 - rr) * (1 - rr) + ri * ri);

        return R;
        }

    private static double Reactance(final double rr, final double ri)
        {
        final double Rx;

        Rx = (2 * ri) / ((1 - rr) * (1 - rr) + ri * ri);

        return Rx;
        }

    private static Point Cord(final double R1, final double X1)
        {
        final double a;
        final double b;
        final double A;
        final double B;
        final double R;
        final double X;

        R = R1;
        X = X1;
        a = 165 * (X * X + R * R - 1) / ((R + 1) * (R + 1) + X * X);
        b = 165 * 2 * X / ((R + 1) * (R + 1) + X * X);
        A = a + 185;
        B = 245 - b;
        final Point pts = new Point((int) A, (int) B);//equivalent to Xs,Ys in double form

        return pts;
        }


    private static void drawCoaxialCable(final Graphics graphics, final Font font)
        {
        final int T;
        final int W;
        final int V;

        W = 120;
        T = 500;
        V = 400;
        graphics.setColor(Color.white);
        //drawing of load and connecting leads
        graphics.drawLine(T + W, V - 50, T + W, V - 60);//vertical lead from coax RHS (South)
        graphics.drawLine(T + W, V - 50, T + W + 35, V - 50);//horizontal lead from coax bottom (RHS)
        //Drawing of ramp to be inserted into source circle (keeping Coax general)
        graphics.drawLine(T - 38, V - 62, T - 38, V - 70);//vertical line for trianglular pulse inside source
        graphics.drawLine(T - 45, V - 62, T - 42, V - 62);//left horizontal line trianglular pulse inside source
        graphics.drawLine(T - 38, V - 70, T - 42, V - 62);//slanting line for trianglular pulse inside source
        graphics.drawLine(T - 38, V - 62, T - 35, V - 62);//right horizontal line trianglular pulse inside source

        graphics.setFont(font);//sets the font for Load and Source
        graphics.drawString("Load", T + W + 15, V - 90);
        graphics.drawString("Source", T - 47, V - 90);

        //drawing of source and connecting leads - some leads below under colour black
        graphics.drawOval(T - 50, V - 75, 20, 20);//source circle
        graphics.drawLine(T - 40, V - 75, T - 40, V - 85);//vertical lead from source (North)
        graphics.drawLine(T - 40, V - 85, T, V - 85);//horizontal lead from source (North)
        graphics.drawLine(T - 40, V - 55, T - 40, V - 50);//vertical lead from source (South)
        graphics.drawLine(T - 40, V - 50, T - 2, V - 50);//horizontal lead from source (South)
        graphics.drawLine(T, V - 50, T, V - 60);//vertical lead from coax LHS (South)

        //drawing of coax
        graphics.drawOval(T - 4, V - 93, 6, 15);//centre LHS oval
        graphics.drawOval(T + W - 4, V - 93, 6, 15);//centre RHS oval
        graphics.drawLine(T, V - 110, W + T, V - 110);//top line of coax
        graphics.drawLine(T, V - 60, W + T, V - 60);//bottom line of coax
        graphics.setColor(Color.lightGray);
        graphics.fillOval(W + T - 10, V - 110, 20, 50);//centre RHS oval
        graphics.fillOval(T - 10, V - 110, 20, 50);//LHS oval(so that it goes into the background)
        graphics.setColor(Color.white);
        graphics.drawOval(W + T - 10, V - 110, 20, 50);//RHS oval(so that yellow covers it)
        graphics.setColor(color0);
        graphics.fillOval(T - 4, V - 93, 6, 15);//centre LHS oval
        graphics.fillOval(T + W - 4, V - 93, 6, 15);//centre RHS oval
        graphics.drawRect(T, V - 93, W, 15);//centre core coloured yellow
        graphics.fillRect(T, V - 93, W, 15);//colouring of yellow
        graphics.setColor(Color.lightGray);
        graphics.drawOval(T + W - 4, V - 93, 6, 15);//centre RHS oval
        graphics.setColor(Color.white);
        graphics.drawLine(T - 2, V - 93, W + T - 4, V - 93);//inner core top line
        graphics.drawLine(T - 2, V - 78, W + T - 4, V - 78);//inner core bottom line
        graphics.drawOval(T - 10, V - 110, 20, 50);//LHS oval
        graphics.drawOval(T - 4, V - 93, 6, 15);//centre LHS oval
        graphics.drawLine(T - 40, V - 85, T - 2, V - 85);//horizontal lead from source (South)
        graphics.drawLine(W + T + 35, V - 85, W + T, V - 85);//centreline of coax RHS to load
        }


    public SmithChartPanel()
        {
        super();

        dog[0] = 1.0;
        dog[1] = 1.0;

        addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(final MouseEvent e)
                {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
                }
            });

        addMouseMotionListener(new MouseMotionListener()
            {
            public void mouseDragged(final MouseEvent e)
                {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
                }

            public void mouseMoved(final MouseEvent event)
                {
                mouseX = event.getX();
                mouseY = event.getY();
                repaint();
                }
            });
        }

    public void initialise()
        {
        intWidth = getWidth();
        intHeight = getHeight();

        }

    public void paint(final Graphics graphics)
        {
        super.paint(graphics);

        intWidth = getWidth();
        intHeight = getHeight();

        graphics.setClip(0, 0, intWidth, intHeight);
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, intWidth, intHeight);

        Xs = (mouseX - 185);//centre of SmithChart chart is at (185, 245)
        Ys = (245 - mouseY);
        rad = Math.sqrt((Xs / 165.0) * (Xs / 165.0) + (Ys / 165.0) * (Ys / 165.0));
        rad1 = Math.sqrt((Xs * Xs) + (Ys * Ys));
        rad4 = Resistance(Xs / 165.0, Ys / 165.0);
        rad2 = 165 * (rad4 / (1 + rad4));//centre of  R chart
        rad3 = 165 * (1 / (1 + rad4));//radius of R chart
        rad5 = Reactance(Xs / 165.0, Ys / 165.0);
        rad6 = 165 * (1 / rad5);//centre of  X chart and Radius of X chart
        rad7 = 165 * Math.abs(1 / rad5);//centre of  X chart and Radius of X chart

        graphics.fillRect(0, 0, 850, 750);
        graphics.setColor(Color.red);
        graphics.drawOval(20, 80, 330, 330);//outside boundary of SmithChart Chart
        graphics.drawOval(19, 79, 332, 332);//outside boundary of SmithChart Chart
        graphics.drawOval(18, 78, 334, 334);//outside boundary of SmithChart Chart
        graphics.setColor(color0);
        graphics.drawLine(20, 245, 350, 245);//horizontal line (diameter)
        graphics.drawString("\u221E", 355, 248);//infinity symbol
        graphics.drawString("0", 10, 248);//zero on LHS of chart
        theta = Math.atan2(Ys, Xs);
        theta1 = Math.acos(Xs / 165);
        graphics.setColor(Color.blue);//X circles & Textfield cross-hair
        ///////////////////////////////////////////Textfield values mapped onto blue cross-hair
        pts = Cord(dog[0], dog[1]);
        Ptx = (pts.x);
        Pty = (pts.y);
        graphics.setFont(f3);//decreases font size
        graphics.drawLine(Ptx - 5, Pty, Ptx + 5, Pty);//horizontal crossline for text input
        graphics.drawLine(Ptx, Pty - 5, Ptx, Pty + 5);//vertical crossline for text input
        graphics.drawString("Z(Y)", Ptx + 8, Pty);
        graphics.setFont(f2);//resets font
//Textfield values mapped onto blue cross-hair

        if (rad <= 1)
            {//limits of SmithChart chart condition
//////these work ok to allow x circles to go to point z, but are not used
//phi = 180-(180/Math.PI)*Math.asin((rad1*Math.sin(theta)-rad6)/rad6);
//graphics.drawArc((int)(350-rad6),(int)(245-2*rad6),(int)(2*rad6),(int)(2*rad6),(int)(phi),270-(int)(phi));//X circle +VE

//this section of code permits the reactive curve to stop at the boundary of the chart
////////top reactance circle (Positive reactance)
            Xt = 165 * (165 * 165 - rad7 * rad7) / (165 * 165 + rad7 * rad7);
            Yt = 2 * 165 * rad6 / (165 * 165 + rad6 * rad6);
            alpha = Math.acos(Xt / 165);
            beta = 180 - (180 / Math.PI) * Math.asin((165 * Math.sin(alpha) - rad7) / rad7);
            graphics.drawArc((int) (350 - rad6), (int) (245 - 2 * rad6), (int) (2 * rad6), (int) (2 * rad6), (int) (beta), 270 - (int) (beta));//X circle +VE

////////bottom reactance circle (Negative reactance)
            if (rad6 <= 0)
                {//condition for negative X circle
                beta1 = 180 + (180 / Math.PI) * Math.asin((165 * Math.sin(alpha) - rad7) / rad7);
                graphics.drawArc((int) (350 - rad7), 245, (int) (2 * rad7), (int) (2 * rad7), (int) (beta1), 90 - (int) (beta1));//X circle +VE
                }

            graphics.setColor(Color.white);//R Circle
            graphics.drawOval((int) (185 + (rad2 - rad3)), (int) (245 - rad3), (int) (2 * rad3), (int) (2 * rad3));//R circle

            graphics.setColor(color0);//VSWR circle and text
//provides the value of the distance in wavelengths on the outer boundary
            graphics.drawString("\u03BB" + " = " + NumberFormat.getNumberInstance().format(0.25 - 0.25 * Math.atan2(Ys, Xs) / Math.PI), (int) (185 + 170 * Math.cos(Math.atan2(Ys, Xs))), (int) (245 - 170 * Math.sin(Math.atan2(Ys, Xs))));//radial

            graphics.drawOval((int) (185 - rad1), (int) (245 - rad1), (int) (2 * rad1), (int) (2 * rad1));//VSWR circle


//this code locks angles and a VSWR circle and prints out the difference in angles
//for two positions and then calculates the length of a s/c stub
            if (State1 == true)
                {//start checkbox activated
                radb = rad1;
                Xb = Xs;
                Yb = Ys;
                }

            if (State2 == true)
                {
                lock1(graphics);
                }

            if (State3 == true)
                {
                lock2(graphics);
                }

            graphics.setColor(color0);//VSWR circle and text
            graphics.drawString("\u03C1", mouseX + 5, mouseY);
            graphics.drawLine(mouseX - 3, mouseY, mouseX + 3, mouseY);//horizontal crossline
            graphics.drawLine(mouseX, mouseY - 3, mouseX, mouseY + 3);//vertical crossline
//graphics.drawLine(185,245,mouseX,mouseY);//radial to VSWR circle (not required as replace by one to boundary)

//extends the radius to the SmithChart chart boundary
//graphics.drawLine(185,245,(int)(185+165*Math.cos(Math.atan2(Ys,Xs))),(int)(245-165*Math.sin(Math.atan2(Ys,Xs))));//radial
            graphics.drawLine((int) (185 - 165 * Math.cos(Math.atan2(Ys, Xs))), (int) (245 + 165 * Math.sin(Math.atan2(Ys, Xs))), (int) (185 + 165 * Math.cos(Math.atan2(Ys, Xs))), (int) (245 - 165 * Math.sin(Math.atan2(Ys, Xs))));//radial

            final Point Pt = new Point((int) Math.sqrt(Xs * Xs + Ys * Ys), (int) Math.atan2(Ys, Xs));
            if (180 * Pt.y / Math.PI <= 0)
                Pt.y = Pt.y + (int) (2 * Math.PI);//ensures angle continues positive past 180 degrees
///////rho values
            graphics.drawString("\u03C1" + " =" + NumberFormat.getNumberInstance().format(Pt.x / 165.0) + " (" + NumberFormat.getNumberInstance().format(180 * Pt.y / Math.PI) + "\u00B0" + ")", 400, 100);
            graphics.drawString("\u03C1" + "(Real) =" + NumberFormat.getNumberInstance().format(Xs / 165.0), 520, 100);
            graphics.drawString("\u03C1" + "(Imag.) =" + NumberFormat.getNumberInstance().format(Ys / 165.0), 620, 100);

            graphics.setColor(Color.white);//R circle
            graphics.drawString("R(G)" + " =" + NumberFormat.getNumberInstance().format(Resistance(Xs / 165.0, Ys / 165.0)), 400, 120);

            graphics.setColor(Color.magenta);//X circles
            graphics.drawString("X(B)" + " =" + NumberFormat.getNumberInstance().format(Reactance(Xs / 165.0, Ys / 165.0)), 520, 120);

            graphics.setColor(color0);//VSWR circle
            graphics.drawString("VSWR =" + NumberFormat.getNumberInstance().format(VSWR(Xs / 165.0, Ys / 165.0)), 620, 120);

            polygon = new Polygon();
            for (int i = 0; i < 120; i++)
                {
                ba[i] = 25 * (rad) * Math.sin((3 * i * Math.PI / 180) + theta / 2) * Math.sin((3 * i * Math.PI / 180) + theta / 2);
                polygon.addPoint(500 + i, 250 + (int) ba[i]);
                }
            graphics.drawPolygon(polygon);
            graphics.drawString("Standing wave on XLine", 500, 240);

            }//end of SmithChart Chart boundary condition
        // Warns that outside of SmithChart chart boundary
        else
            {
            graphics.setFont(f1);//VSWR circle
            graphics.setColor(color0);//VSWR circle
            graphics.drawString("Outside SmithChart Chart limits", 400, 130);
            graphics.setFont(f2);//VSWR circle
            }

        graphics.drawLine(500, 275, 617, 275);
        graphics.drawLine(500, 250, 617, 250);

        comp = (180 / Math.PI) * Math.abs(theta);

        if (comp <= 3 && comp >= 0 && rad >= 0.97)
            graphics.drawString("Load Open Circuit", 625, 263);
        else
            {
            graphics.drawLine(655, 315, 655, 318);//Resistor
            graphics.drawLine(655, 318, 660, 323);//Resistor
            graphics.drawLine(655, 328, 660, 323);//Resistor
            graphics.drawLine(655, 328, 660, 333);//Resistor
            graphics.drawLine(655, 338, 660, 333);//Resistor
            graphics.drawLine(655, 338, 660, 343);//Resistor
            graphics.drawLine(655, 348, 660, 343);//Resistor
            graphics.drawLine(655, 348, 655, 350);//Resistor
            }
        if (comp <= 180 && comp >= 177 && rad >= 0.97)
            {
            graphics.drawString("Load Short Circuit", 625, 263);
            graphics.drawLine(655, 315, 655, 350);
            }

        drawCoaxialCable(graphics, f3);
        }

    private void lock1(final Graphics graphics)
        {
        //lock1 checkbox activated
        graphics.setColor(Color.green);//locked VSWR circle and angle
        graphics.drawLine(185, 245, (int) (185 + 165 * Math.cos(Math.atan2(Yb, Xb))), (int) (245 - 165 * Math.sin(Math.atan2(Yb, Xb))));//radial
        //provides the value of the distance in wavelengths on the outer boundary
        graphics.drawString("\u03BB" + "1" + " = " + NumberFormat.getNumberInstance().format(0.25 - 0.25 * Math.atan2(Yb, Xb) / Math.PI), (int) (185 + 170 * Math.cos(Math.atan2(Yb, Xb))), (int) (245 - 170 * Math.sin(Math.atan2(Yb, Xb))));//radial
        graphics.drawOval((int) (185 - radb), (int) (245 - radb), (int) (2 * radb), (int) (2 * radb));//VSWR circle
        if (Math.atan2(Yb, Xb) - Math.atan2(Ys, Xs) >= 0)
            graphics.drawString("Difference in angles (" + "\u03BB" + " - " + "\u03BB" + "1) = " + NumberFormat.getNumberInstance().format(0.25 * (Math.atan2(Yb, Xb) - Math.atan2(Ys, Xs)) / Math.PI) + " " + "\u03BB", 480, 150);
        else
            graphics.drawString("Difference in angles (" + "\u03BB" + " - " + "\u03BB" + "1) = " + NumberFormat.getNumberInstance().format(0.50 + (0.25 * (Math.atan2(Yb, Xb) - Math.atan2(Ys, Xs)) / Math.PI)) + " " + "\u03BB", 480, 150);
        radc = rad1;
        Xc = Xs;
        Yc = Ys;
        XR = -Reactance(Xs / 165.0, Ys / 165.0);
        }


    private void lock2(final Graphics graphics)
        {
        //lock2 checkbox activated
        graphics.drawString("\u03BB" + "1" + " = " + NumberFormat.getNumberInstance().format(0.25 - 0.25 * Math.atan2(Yb, Xb) / Math.PI), (int) (185 + 170 * Math.cos(Math.atan2(Yb, Xb))), (int) (245 - 170 * Math.sin(Math.atan2(Yb, Xb))));//radial
        //graphics.drawOval((int)(185-radb),(int)(245-radb),(int)(2*radb),(int)(2*radb));//VSWR circle
        graphics.drawString("Difference in angles (" + "\u03BB" + "2" + " - " + "\u03BB" + "1) = " + NumberFormat.getNumberInstance().format(0.25 * (Math.atan2(Yb, Xb) - Math.atan2(Yc, Xc)) / Math.PI) + " " + "\u03BB", 480, 150);
        graphics.setColor(Color.green);//locked VSWR circle and angle
        graphics.drawLine(185, 245, (int) (185 + 165 * Math.cos(Math.atan2(Yc, Xc))), (int) (245 - 165 * Math.sin(Math.atan2(Yc, Xc))));//radial
        //provides the value of the distance in wavelengths on the outer boundary
        graphics.drawString("\u03BB" + "2" + " = " + NumberFormat.getNumberInstance().format(0.25 - 0.25 * Math.atan2(Yc, Xc) / Math.PI), (int) (185 + 170 * Math.cos(Math.atan2(Yc, Xc))), (int) (245 - 170 * Math.sin(Math.atan2(Yc, Xc))));//radial
        //graphics.drawOval((int)(185-radc),(int)(245-radc),(int)(2*radc),(int)(2*radc));//VSWR circle
        graphics.drawString("Want to match out a reactance of  " + NumberFormat.getNumberInstance().format(XR) + " " + "\u03A9", 480, 170);
        if (0.25 * Math.atan2(Ys, Xs) / Math.PI <= 0)
            graphics.drawString("Difference in angles (" + "\u03BB" + " - " + 0.25 + "\u03BB" + ") = " + NumberFormat.getNumberInstance().format(-0.25 * Math.atan2(Ys, Xs) / Math.PI) + " " + "\u03BB", 480, 190);
        else
            graphics.drawString("Difference in angles (" + "\u03BB" + " - " + 0.25 + "\u03BB" + ") = " + NumberFormat.getNumberInstance().format(0.50 - 0.25 * Math.atan2(Ys, Xs) / Math.PI) + " " + "\u03BB", 480, 190);
        }
    }

