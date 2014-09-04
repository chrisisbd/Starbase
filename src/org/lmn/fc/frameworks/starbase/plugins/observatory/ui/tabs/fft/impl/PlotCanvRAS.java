// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.impl;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.MathsHelper;

import javax.swing.*;
import java.awt.*;


public class PlotCanvRAS extends JComponent
    {
    private static final String I_OR_Q = "I";
    private static final String FT_OR_CF = "FT";
    private static final String DB_OR_V = "V";
    private static final String DEFAULT_FILENAME = "xsig220.bin";


    private final int intSampleRate;
    private int intFFTLengthIndex;
    private float xmax;
    private float xmin;
    private float floatAmplitudeCorrection;
    private float floatPhaseCorrection;
    private float floatModeCentreAdjust;

    private int fflg;
    private float sr2;
    //private float azS;


    // Only seems to use [0] and [1]
    private final int[] mxdat = new int[10];

    private final int[] arrayData = new int[70000];

    private final double[] fdatr = new double[60000];
    private final double[] fdati = new double[60000];
    private final double[] fda3r = new double[60000];
    private final double[] fda3i = new double[60000];

    private final double[] arrayMagnitude = new double[80000];
    private final double[] arrayMagnitudeReal = new double[80000];
    private final double[] arrayMagnitudeImaginary = new double[80000];

    // Diagnostics only?
    private final String[] arrayConsoleOut = new String[70000];


    public PlotCanvRAS()
        {
        super();

        xmin = -100;
        xmax = 100;

        intSampleRate = 2048;
        }




    public void paint(final Graphics graphics)
        {
        final long BUFFER_SIZE = 2048 * 16;
        float xcur, ycur, ycur2;
        final PlotCoordRAS plotCoords;
        final float ymin;
        final Dimension d;
        int stepSize;
        final float ymax = 14;
        final int inset = 20;
        int xcoord, ycoord;
        final float xaxisStart = xmax;
        final float xaxisEnd = xmin;
        final float xaxisPosy;
        final float yaxisStart;
        final float yaxisEnd;
        final float yaxisPosx;
        final Font f = new Font("Dialog", Font.PLAIN, 10);
        final float floatFFTLength;
        final float offst;
        final int mult;
        int i20 = 0;
        int xcoordPrev = 0;
        int ycoordPrev = 0;
        int cte = 1;
        float xprev = 0;
        float yprev = 0;
        float max = 0;
        float min = 0;
        float mx = 0;
        float mnr = 0;
        float mni = 0;
        int ycoordy;
        int xcoordx;
        float ycuri;
        long longBlockCount = 10;


        System.out.println("PAINT PLOT CANVAS +++++++++++++++++++++++++++++++++++++++++++++++");

        floatFFTLength = (float) Math.pow(2, intFFTLengthIndex + 5);

        mult = 2048 * 16 / 2 / (int) floatFFTLength;

        offst = (float) -((int) (1 * (floatModeCentreAdjust - (float) 0.5) * (float) 16 * floatFFTLength / (float) 2));


        //=========================================================================================
        d = size();
        ymin = -ymax;
        yaxisStart = ymax;
        yaxisEnd = ymin;

        graphics.setColor(Color.black);

        plotCoords = new PlotCoordRAS(xmax, ymax, xmin, ymin, d.width, d.height, inset);

        if (xmin < 0 && xmax > 0)
            {
            yaxisPosx = 0;
            }
        else
            {
            yaxisPosx = xmin;
            }

        if ((ymin < 0) && (ymax > 0))
            {
            xaxisPosy = ymin / (float) 1.5;
            }
        else
            {
            xaxisPosy = ymin / (float) 1.5;
            }

        graphics.setFont(f);

        /* Draw x axis. */
//        g.drawLine(plotCoords.xcoord(xaxisStart), plotCoords.ycoord(xaxisPosy),
//                   plotCoords.xcoord(xaxisEnd), plotCoords.ycoord(xaxisPosy));
//
//        if ("FT".equals(FT_OR_CF))
//            {
//            g.drawLine(plotCoords.xcoord(xaxisStart), plotCoords.ycoord(-ymax / 4),
//                       plotCoords.xcoord(xaxisEnd), plotCoords.ycoord(-ymax / 4));
//            }
//
//        if ("CF".equals(FT_OR_CF))
//            {
//            g.drawLine(plotCoords.xcoord(xaxisStart), plotCoords.ycoord(+ymax / 4),
//                       plotCoords.xcoord(xaxisEnd), plotCoords.ycoord(+ymax / 4));
//            g.setColor(Color.black);
//            }
//        /* Draw y axis. */
//
//        g.drawLine(plotCoords.xcoord(yaxisPosx), plotCoords.ycoord(xaxisPosy / 2),
//                   plotCoords.xcoord(yaxisPosx), plotCoords.ycoord(yaxisEnd));
//
//        if ("FT".equals(FT_OR_CF))
//            {
//            g.drawLine(plotCoords.xcoord(yaxisPosx), plotCoords.ycoord(3 * ymax / 4),
//                       plotCoords.xcoord(yaxisPosx), plotCoords.ycoord(-ymax / 4));
//            }
//
//        {
//        /* This draws one tick mark on y */
//        ycoord = plotCoords.ycoord(xaxisPosy);
//        xcoord = plotCoords.xcoord(yaxisPosx);
//
//        g.drawString("" + MathsHelper.Ro(ymin + (ymax - ymin) / 2, 2), xcoord + 5, ycoord + 10);
//        g.drawLine(xcoord + 3, ycoord, xcoord - 3, ycoord);
//        g.drawString("" + MathsHelper.Ro(ymin + (ymax - ymin) / 2, 2), xcoord + 5, ycoord + 10);
//
//        if ("FT".equals(FT_OR_CF))
//            {
//            g.setColor(Color.red);
//            //g.drawString("Hanning Weighting", xcoord + 70, ycoord - 90);
//            }
//
//        if ("Q".equals(I_OR_Q))
//            {
//            g.setColor(Color.magenta);
//            g.drawString("Quadrature Signal", xcoord + 300, ycoord + 52);
//            }
//
//        if ("I".equals(I_OR_Q))
//            {
//            g.setColor(Color.blue);
//            g.drawString("In Phase Signal", xcoord + 300, ycoord + 52);
//            }
//
//        if ("CF".equals(FT_OR_CF))
//            {
//            g.setColor(Color.magenta);
//            g.drawString("Data Plot (2)", xcoord + 330, ycoord + 64);
//
//            g.setColor(Color.blue);
//            g.drawString("Data Plot (1)", xcoord + 330, ycoord + 52);
//            }
//
//        /* This draws one tick mark on x */
//        g.setColor(Color.black);
//        xcoord = plotCoords.xcoord(xmin + (xmax - xmin) / 2 + 1 / 360 / sr2);
//        ycoord = plotCoords.ycoord(xaxisPosy);
//        g.drawString("Data Points", xcoord + 75, ycoord + 25);
//
//        g.setColor(Color.red);
//        g.drawLine(xcoord, ycoord + 50, xcoord, ycoord - 50);
//        g.drawLine(xcoord + 50, ycoord, xcoord - 50, ycoord);
//        //g.drawString( new Integer(Math.round(xmin + (xmax*(float)ftlen*16/(float)360 - xmin)/2/16)).toString(), xcoord -10, ycoord + 15);
//        g.setColor(Color.black);
//        ycoord = plotCoords.ycoord(-ymax / 4);
//
//        if ("FT".equals(FT_OR_CF))
//            {
//            g.drawLine(xcoord, ycoord + 3, xcoord, ycoord - 3);
//
//            g.drawString("" + ((Math.round(xmin + (xmax * floatFFTLength * 16 / (float) 360 / (float) 1 - xmin) / 4 / 8) - floatFFTLength / 2) - (int) (offst + (float) 0.0)), xcoord - 5, ycoord + 15);
//            g.drawString("Frequency Spectrum", xcoord + 65, ycoord + 15);
//            }
//
//
//        /* This draws one tick mark on x */
//        xcoord = plotCoords.xcoord(xmax);
//        ycoord = plotCoords.ycoord(xaxisPosy);
//
//        g.drawLine(xcoord, ycoord + 3, xcoord, ycoord - 3);
//        // g.drawString(new Integer((int)(xmax*(float)ftlen/(float)360/1+(float)0.5)).toString(),  xcoord - 10, ycoord + 15);
//        ycoord = plotCoords.ycoord(-ymax / 4);
//
//        if ("FT".equals(FT_OR_CF))
//            {
//            g.drawLine(xcoord, ycoord + 3, xcoord, ycoord - 3);
//            g.drawString("" + (int) ((Math.round(xmin + (xmax * floatFFTLength * 2 / (float) 360 - xmin) / 4) - floatFFTLength / 2) - (int) (offst + (float) 0.0) + floatFFTLength * 16 / 4 / 8 / sr2), xcoord - 10,
//                         ycoord + 15);
//
//            xcoord = plotCoords.xcoord(xmin);
//            g.drawLine(xcoord, ycoord + 3, xcoord, ycoord - 3);
//            g.drawString("" + (int) ((Math.round(xmin + (xmax * floatFFTLength * 2 / (float) 360 - xmin) / 4) - floatFFTLength / 2) + (int) (-offst + (float) 0.0) - floatFFTLength * 16 / 4 / 8 / sr2), xcoord - 10,
//                         ycoord + 15);
//            }
//        }
//
//        ycoord = plotCoords.ycoord(xaxisPosy);
//        xcoord = plotCoords.xcoord(yaxisPosx);
//
//        g.drawString("No: FFTs:- " + (mult * longBlockCount), xcoord + 70, ycoord - 110);
//        g.drawString("Temp. Factor:- " + (int) Math.sqrt(mult * longBlockCount + 0.5) + " = " + (MathsHelper.Ro(MathsHelper.dB(mult * longBlockCount) / 2, 1)) + "dB", xcoord + 270, ycoord - 100);
//        g.drawString("Max File Blocks:- " + (mxdat[0]), xcoord + 270, ycoord - 90);
//        g.drawString("Sample Rate:- " + (intSampleRate) + "kHz", xcoord + 270, ycoord - 110);
//        g.drawString("Integr. Time:- " + MathsHelper.Ro(((float) 2048 * 16 * longBlockCount / intSampleRate / 1000), 2) + "s", xcoord + 70, ycoord - 90);

        //System.out.println(floatAmplitudeCorrection+"     "+floatPhaseCorrection);


        //=========================================================================================
        /*plot ip function*/

        stepSize = 1;

        if ("I".equals(I_OR_Q) || "Q".equals(I_OR_Q))
            {
            System.out.println("ACCESS FILE FIRST TIME ...... blockindex=" + 0);

//            AveragingFFTDAO.readBlockFromFile(xzczxczxc,
//                                              0,
//                                              arrayData,
//                                              (long) floatFFTLength * 32,
//                                              2048,
//                                              16
//            );

            if (longBlockCount > mxdat[0])
                {
                longBlockCount = mxdat[0];
                }

            for (int xx = (int) (floatFFTLength);
                 xx < (int) (16 * floatFFTLength);
                 xx += stepSize)
                {
                arrayMagnitudeReal[xx] = (float) ((arrayData[2 * xx])) / (float) 128.0;
                arrayMagnitudeImaginary[xx] = (float) (arrayData[2 * xx + 1]) * floatAmplitudeCorrection / (float) 128.0 + floatPhaseCorrection * arrayMagnitudeReal[xx];
                mx = mx + ((float) (arrayMagnitudeReal[xx] * arrayMagnitudeReal[xx]) - mx) / cte; //+arrayMagnitudeImaginary[xx]*arrayMagnitudeImaginary[xx]
                mnr = mnr + ((float) (arrayMagnitudeReal[xx]) - mnr) / cte;
                mni = mni + ((float) (arrayMagnitudeImaginary[xx]) - mni) / cte;
                //idc=idc+(float)(arrayMagnitudeReal[xx])-mnr)/cte;
                //qdc=qdc+(float)(arrayMagnitudeImaginary[xx])-mni)/cte;

                if (arrayMagnitudeReal[xx] > max)
                    {
                    max = (float) arrayMagnitudeReal[xx];
                    }
                if (-arrayMagnitudeReal[xx] > min)
                    {
                    min = (float) -arrayMagnitudeReal[xx];
                    }
                cte += 1;

                }//System.out.println(mnr+"     "+mni+"    "+mx+"    "+cte+"    "+maxblks+"    "+DEFAULT_FILENAME);

//            xcoord = plotCoords.xcoord(0);
//            ycoord = plotCoords.ycoord(ymin);
//            g.drawLine(xcoord + 3, ycoord + 0, xcoord - 3, ycoord - 0);
//            g.drawString("" + -MathsHelper.Ro(min, 2), xcoord + 5, ycoord + 5);


            /* Draw tick marks at the max's. */
            /* This draws one tick mark on y */
//            ycoord = plotCoords.ycoord(xaxisPosy + ymax / 3);
//            xcoord = plotCoords.xcoord(yaxisPosx);
//            g.drawLine(xcoord + 3, ycoord, xcoord - 3, ycoord);
//            g.drawString("" + MathsHelper.Ro(max, 2), xcoord + 5, ycoord + 10);


            xcoordx = plotCoords.xcoord(xmin + (xmax - xmin) / 2);
            ycoordy = plotCoords.ycoord(xaxisPosy);


            if ("Q".equals(I_OR_Q))
                {
                graphics.setColor(Color.magenta);
                }

            for (xcur = 0;
                 xcur < 2 * floatFFTLength;
                 xcur += stepSize)
                {
                arrayMagnitude[(int) xcur] = 0;
                arrayMagnitude[(int) xcur + (int) floatFFTLength / 2] = 0;

                if ("I".equals(I_OR_Q))
                    {
                    ycur = (float) (arrayMagnitudeReal[(int) xcur] - mnr);//*(float)Hanwt(xcur,(int)ftlen));
                    ycuri = (float) (arrayMagnitudeImaginary[(int) xcur] - mni);
                    }
                else
                    {
                    ycur = (float) arrayMagnitudeImaginary[(int) xcur] - mni;//*(float)Hanwt(xcur,(int)ftlen));
                    ycuri = (float) arrayMagnitudeReal[(int) xcur] - mnr;
                    }
                xcoord = xcoordx + (int) (20 * ycur / (float) Math.sqrt(mx));//pc.xcoord((float)(180.0+(float)26.5*((ycuri)/(float)Math.sqrt(mx)/15.0)));
                ycoord = ycoordy + (int) (20 * ycuri / (float) Math.sqrt(mx));//pc.ycoord((float)(2.0*((ycur)/(float)Math.sqrt(mx)/5.0+7.0)/3.0)-ymax/(float)1.0);
                //xcoord = pc.xcoord(180+20*(ycur/(float)Math.sqrt(mx)/5)/3);
                //ycoord = pc.ycoord(2*(ycur/(float)Math.sqrt(mx)/5+7)/3-ymax/(float)1.0);
                if (xcur != 0)
                    {
                    graphics.fillRect(xcoord - 1, ycoord - 1, 3, 3);
                    //g.drawLine(xcoordPrev, ycoordPrev,xcoord, ycoord);
                    }
                xcoordPrev = xcoord;
                ycoordPrev = ycoord;
                xprev = xcur;
                yprev = ycur;
                }
            }

        //'''''''''''''''''''''''''''''''''''''''''''''''


        graphics.setColor(Color.red);
        xcoordx = plotCoords.xcoord(xmin + (xmax - xmin) / 2);
        ycoordy = plotCoords.ycoord(xaxisPosy);

        for (xcur = 0;
             xcur < 101;
             xcur += 1)
            {
            ycur = (float) (50.0 * Math.sin(xcur * 2 * Math.PI / (float) 100.0));
            final float ycurc = (float) (50.0 * Math.cos(xcur * 2 * Math.PI / (float) 100.0));

            xcoord = xcoordx + (int) ycurc;//pc.xcoord((float)180.0+ycur*(float)360.0/(float)400.0);

            ycoord = ycoordy + (int) ycur;//pc.ycoord(ymin/(float)1.5+ycurc/(float)15.0);


            if (xcur > 0)
                {
                //g.fillRect(xcoord, ycoord,1,1);
                graphics.drawLine(xcoordPrev, ycoordPrev, xcoord, ycoord);
                }

            xcoordPrev = xcoord;
            ycoordPrev = ycoord;
            xprev = xcur;
            yprev = ycur;
            }//System.out.println(dBv(max)+"   "+arrayMagnitude[(int)(ftlen/4)]+"      "+Math.sqrt(mx)+"    "+dBv(mx));



        // Analyse error - fourier      if(mxdat[1]==0)
        System.out.println(floatAmplitudeCorrection + "     " + floatPhaseCorrection);


        //-----------------------------------------------------------------------------------------
        // Get 32768 sample blocks and average FFT's

        for (long longBlockIndex = 0;
             longBlockIndex < longBlockCount;
             longBlockIndex++)
            {
            System.out.println("ACCESS FILE SECOND TIME ...... [longBlockIndex=" + longBlockIndex + "] [longBlockCount=" + longBlockCount + "]");

//            AveragingFFTDAO.readBlockFromFile(
//                    xzczxczxc, longBlockIndex,
//                                              arrayData,
//                                              BUFFER_SIZE,
//                                              2048,
//                                              16
//            );

            for (int i = 0;
                 i < BUFFER_SIZE >> 1;
                 i++)
                {
                if (("I".equals(I_OR_Q) && mxdat[1] == 0)
                    || ("Q".equals(I_OR_Q) && mxdat[1] == 1))
                    {
                    fdatr[i] = ((float) ((arrayData[2 * i]) + 0.0) / 128.0) - (1 * mnr);
                    fdati[i] = ((((float) ((arrayData[2 * i + 1] + 0.0)) * floatAmplitudeCorrection) / 128.0) + (floatPhaseCorrection * fdatr[i])) - (1 * mni);
                    }
                else
                    {
                    fdati[i] = ((float) ((arrayData[2 * i]) + 0.0) / 128.0) - mni;
                    fdatr[i] = ((((float) ((arrayData[2 * i + 1]) + 0.0) * floatAmplitudeCorrection) / 128.0) + (floatPhaseCorrection * fdati[i])) - (1 * mnr);
                    }
                }


            if ("FT".equals(FT_OR_CF))
                {
                System.out.println("FT MODE");
                if (longBlockIndex == 0)
                    {
                    i20 = 0;
                    }

                {
                for (int i2 = i20;
                     i2 < mult;
                     i2 += 1)
                    {
                    for (int i1 = 0;
                         i1 < floatFFTLength;
                         i1 += 1)
                        {
                        fda3r[i1] = (fdatr[i1 + i2 * (int) floatFFTLength]);//*(float)Hanwt(i1,(int)ftlen);
                        fda3i[i1] = (fdati[i1 + i2 * (int) floatFFTLength]);//*(float)Hanwt(i1,(int)ftlen);

                        }//System.out.println(i2+"     "+fda3r[12]+"     "+fda3i[12]);

                    MathsHelper.fft(fda3r, fda3i, (int) floatFFTLength, -1);
                    //Fouris(fda3r,fda3i,(int)ftlen,-1);

                    for (int i1 = 0;
                         i1 < floatFFTLength;
                         i1 += 1)
                        {

                        arrayMagnitude[i1] = arrayMagnitude[i1] + (float) ((fda3r[i1] * fda3r[i1]) + (fda3i[i1] * fda3i[i1]));

                        }//System.out.println(i2+"     "+fda3r[12]+"     "+arrayMagnitude[12]);
                    }

                }//System.out.println(mxdat[0]+"   "+mxdat[1]+"      "+arrayMagnitude[1]);


                //System.out.println(mxdat[0]+"   "+mxdat[1]+"      "+arrayMagnitude[1]);

                graphics.setColor(Color.red);

                if ((longBlockCount == 1) || ((longBlockCount > 1) && (longBlockIndex == (longBlockCount - 1))))
                    {
                    ycur = 0;
                    final float ycurm;
                    ycurm = ((float) arrayMagnitude[(int) floatFFTLength / 4] * floatFFTLength * floatFFTLength / 2048 / 2048);

                    for (xcur = floatFFTLength / 2;
                         xcur < floatFFTLength;
                         xcur += 1)
                        {

                        if ("V".equals(DB_OR_V))
                            {
                            ycur = (float) arrayMagnitude[(int) xcur];
                            }
                        else
                            {
                            ycur = MathsHelper.dB((float) arrayMagnitude[(int) xcur] * floatFFTLength * floatFFTLength / 2048 / 2048);
                            }

                        xcoord = plotCoords.xcoord((((xcur - floatFFTLength / 2) + offst - floatFFTLength / 2) * (sr2) + floatFFTLength / 2) * 360 / floatFFTLength);

                        if ("V".equals(DB_OR_V))
                            {
                            ycoord = plotCoords.ycoord(4 * ymax * ycur * floatFFTLength * floatFFTLength / 2048 / 2048 / ycurm / 10);
                            }
                        else
                            {
                            ycoord = plotCoords.ycoord(ymax / 2 + (ycur - MathsHelper.dB(ycurm)) / 1);
                            }

                        if (xcur >= floatFFTLength / 2)
                            {
                            graphics.fillRect(xcoord, ycoord, (int) (1 + (784 / floatFFTLength) * (int) sr2 / 2), 1);
                            //g.drawLine(xcoordPrev, ycoordPrev, xcoord, ycoord);
                            }
                        xcoordPrev = xcoord;
                        ycoordPrev = ycoord;
                        xprev = xcur;
                        yprev = ycur;
                        }


                    for (xcur = 0;
                         xcur < floatFFTLength / 2;
                         xcur += 1)
                        {

                        if ("V".equals(DB_OR_V))
                            {
                            ycur = (float) arrayMagnitude[(int) xcur];
                            }
                        else
                            {
                            ycur = MathsHelper.dB((float) arrayMagnitude[(int) xcur] * floatFFTLength * floatFFTLength / 2048 / 2048);
                            }

                        xcoord = plotCoords.xcoord((((xcur + floatFFTLength / 2) + 1 * offst - 1 * floatFFTLength / 2) * (sr2) + floatFFTLength / 2) * 360 / floatFFTLength);

                        if ("V".equals(DB_OR_V))
                            {
                            ycoord = plotCoords.ycoord(4 * ymax * ycur * floatFFTLength * floatFFTLength / 2048 / 2048 / ycurm / 10);
                            }
                        else
                            {
                            ycoord = plotCoords.ycoord(ymax / 2 + (ycur - MathsHelper.dB(ycurm)) / 1);
                            }

                        if ((xcur >= 0)
                            && (longBlockIndex == (longBlockCount - 1)))
                            {
                            graphics.fillRect(xcoord, ycoord, (int) (1 + (784 / floatFFTLength) * (int) sr2 / 2), 1);
                            //g.drawLine(xcoordPrev, ycoordPrev, xcoord, ycoord);
                            }
                        xcoordPrev = xcoord;
                        ycoordPrev = ycoord;
                        xprev = xcur;
                        yprev = ycur;
                        }//System.out.println(dBv(max)+"   "+arrayMagnitude[(int)(ftlen/4)]+"      "+Math.sqrt(mx)+"    "+dBv(mx));

                    }
                }
            }
            //System.out.println(mxdat[0]+"   "+mxdat[1]+"      "+(arrayMagnitude[1]/longBlockCount));
        }

    // End of FFT processing
    //---------------------------------------------------------------------------------------------






/*Print data to Console*/

//        int cnt = 1;
//        final int cnt2 = 1;
//        int cntmx = 0;
//        final String ss = " ";
//        ycur2 = 0;
//        {
//        for (xcur = 0;
//             xcur < floatFFTLength;
//             xcur += stepSize)
//            {
//
//            if (xcur < floatFFTLength / 2)
//                {
//
//                if ("V".equals(DB_OR_V))
//                    {
//                    ycur2 = MathsHelper.Ro((float) (2000 * arrayMagnitude[(int) (floatFFTLength / 2 + xcur)] * floatFFTLength * floatFFTLength / lym / 2048 / 2048), 4);
//                    }
//                else
//                    {
//                    ycur2 = MathsHelper.Ro(MathsHelper.dB((float) (arrayMagnitude[(int) (floatFFTLength / 2 + xcur)] * floatFFTLength * floatFFTLength / lym / 2048 / 2048)), 2);
//                    }
//
//                }
//            else
//                {
//
//                if ("V".equals(DB_OR_V))
//                    {
//                    ycur2 = MathsHelper.Ro((float) (2000 * arrayMagnitude[(int) (xcur - floatFFTLength / 2)] * floatFFTLength * floatFFTLength / lym / 2048 / 2048), 4);
//                    }
//                else
//                    {
//                    ycur2 = MathsHelper.Ro(MathsHelper.dB((float) (arrayMagnitude[(int) (xcur - floatFFTLength / 2)] * floatFFTLength * floatFFTLength / lym / 2048 / 2048)), 2);
//                    }
//                }
//
//            if ("FT".equals(FT_OR_CF))
//                {
//
//                arrayConsoleOut[cnt + 0] = (xcur - floatFFTLength / 2) + "	" + ycur2;
//
//                }
//            cnt += 1;
//            if (cnt > cntmx)
//                {
//                cntmx = cnt;
//                }
//            }
//
//
//        }
//
//
//        arrayConsoleOut[0] = "" + cntmx;
//
//        if (fflg == 1)
//            {
//            {
//            for (int i = 1;
//                 i < cntmx;
//                 i++)
//                {
//                System.out.println(arrayConsoleOut[i]);
//                }
//            }
//            fflg = 0;
//            }



    //---------------------------------------------------------------------------------------------

    public void setFFTLengthIndex(final int index)
        {
        intFFTLengthIndex = index;
        }


    public void setXmin(final float min)
        {
        this.xmin = min;
        }


    public void setXmax(final float max)
        {
        xmax = max;
        }


//    public float setazStep(final float azStep)
//        {
//        azS = azStep;
//
//        return (azS);
//        }


    public void setSCR2(final float scr2)
        {
        sr2 = scr2;
        }


    public void setModeCentreAdjust(final float scr3)
        {
        floatModeCentreAdjust = scr3;
        }


    public void setAmplitudeCorrection(final float correction)
        {
        floatAmplitudeCorrection = correction;
        }


    public void setPhaseCorrection(final float correction)
        {
        floatPhaseCorrection = correction;
        }

    }
