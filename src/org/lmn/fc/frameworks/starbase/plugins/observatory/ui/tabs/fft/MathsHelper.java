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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft;

/***************************************************************************************************
 * MathsHelper.
 */

public final class MathsHelper
    {
    /* Fast Fourier real + imaginary */
    public static void fft(final double[] fdr,
                           final double[] fdi,
                           final int nnn,
                           final int isign)
        {

        double temp;
        final double[] fata = new double[200000];
        final int n;
        int mmax;
        int m;
        int j;
        int istep;
        int i;
        int a;
        double wtemp = 0, wwr = 0, wpr, wpi, wsi = 0, theta;
        double tempr = 0, tempi = 0;

        for (int r = 0;
             r < nnn;
             r++)
            {
            fata[2 * r + 1] = fdr[r];
            fata[2 * r + 2] = fdi[r];
            }

        n = nnn << 1;
        j = 1;
        for (i = 1;
             i < n;
             i += 2)
            {
            if (j > i)
                {
                temp = fata[j];
                fata[j] = fata[i];
                fata[i] = temp;
                temp = fata[j + 1];
                fata[j + 1] = fata[i + 1];
                fata[i + 1] = temp;
                }
            m = n >> 1;
            while (m >= 2 && j > m)
                {
                j -= m;
                m >>= 1;
                }
            j += m;
            }
        mmax = 2;
        while (n > mmax)
            {
            istep = 2 * mmax;
            theta = 6.28318530717959 / (double) (isign * mmax);
            wtemp = Math.sin(0.5 * theta);
            wpr = -2.0 * wtemp * wtemp;
            wpi = Math.sin(theta);
            wwr = 1.0;
            wsi = 0.0;
            for (m = 1;
                 m < mmax;
                 m += 2)
                {
                for (i = m;
                     i <= n;
                     i += istep)
                    {
                    j = i + mmax;
                    tempr = wwr * fata[j] - wsi * fata[j + 1];
                    tempi = wwr * fata[j + 1] + wsi * fata[j];
                    fata[j] = fata[i] - tempr;
                    fata[j + 1] = fata[i + 1] - tempi;
                    fata[i] += tempr;
                    fata[i + 1] += tempi;
                    }
                wwr = (wtemp = wwr) * wpr - wsi * wpi + wwr;
                wsi = wsi * wpr + wtemp * wpi + wsi;
                }
            mmax = istep;
            }
        if (isign == -1)
            {
            for (a = 1;
                 a < nnn + 1;
                 a++)
                {
                fdr[a - 1] = fata[2 * a - 1] / (double) nnn;
                fdi[a - 1] = fata[2 * a] / (double) nnn;
                }
            }


        }


    /* modulus Function */
    public static int mod(final int n,
                          final int N)
        {
        final double co;
        co = (n - N * (n / N));
        return ((int) co);
        }


    /* Proper Random function */
    public static float Rand()
        {
        final float ra;
        ra = (float) (Math.random() - 0.5);
        return (ra);
        }


    /* Prime Factors function */
    public static float PF2(final int x)
        {
        float par = 0, ra;
        int fl = 0;
        par = (float) x / 2;
        while (par - (int) par == 0)
            {
            par = par / 2;
            fl = fl + 1;
            }
        return ((float) Math.pow(2, fl));
        }


    /* Value of string */
    public static float Val(final String s)
        {
        final float sr;
        sr = Float.valueOf(s);
        return (sr);
        }


    /* Hanning Weight Function */
    public static float Hanwt(final float x,
                              final int nn)
        {
        final float bl;
        final float a0;
        final float a1;
        final float a2;
        final float pi;
        pi = (float) Math.PI;
        a0 = (float) 0.5;
        a1 = (float) 0.5;
        a2 = (float) 0.0;
        bl = a0 + a1 * (float) Math.cos(2 * pi * (x - nn / 2) / nn) + a2 * (float) Math.cos(2 * 2 * pi * (x - nn / 2) / nn);
        return (bl);
        }


    /* Riesz Weight Function  */
    public static float Riwt(final float x,
                             final int nn)
        {
        final float bl;
        final float a0;
        a0 = 2 * (x - nn / 2) / (nn + 1);
        bl = 1 - a0 * a0;
        return (bl);
        }


    /* Quantising function */
    public static float Quant(final float x,
                              final float mv,
                              final int bits)
        {
        final float ret;
        float delt = 0;
        delt = mv / (float) Math.pow(2, (double) bits);
        if (x < mv)
            {
            ret = delt * Math.round(x / delt);
            }
        else
            {
            ret = mv - delt;
            }
        return (ret);
        }


    /* Rounding function */
    public static float Ro(final float x,
                           final int N)
        {
        final float ret;
        final float ex;
        ex = (float) Math.pow(10, N);
        ret = (float) (Math.round(ex * x)) / ex;
        return (ret);
        }


    /* dB conversion*/
    public static float dB(final float x)
        {
        final float db;
        db = (float) (10 * Math.log((double) x) / Math.log(10));
        return (db);
        }


    /* Antilog conversion*/
    public static float aLg(final float x)
        {
        final float alg;
        alg = (float) (Math.pow(10, (double) (x / 10)));
        return (alg);
        }


    /* Antilog volts conversion*/
    public static float algv(final float x)
        {
        final float alg;
        alg = (float) (Math.pow(10, (double) (x / 20)));
        return (alg);
        }


    /* dBv conversion*/
    public static float dBv(final float x)
        {
        final float dbv;
        dbv = (float) (20 * Math.log((double) x) / Math.log(10));
        return (dbv);
        }


    /* Fast Fourier single file */
    public static void Fouris(final double[] fda3rx,
                       final double[] fda3ix,
                       final double[] fata,
                       final int nnn,
                       final int isign)
        {

        double temp;
        //double[] fata = new double[200000];
        final int n;
        int mmax;
        int m;
        int j;
        int istep;
        int i;
        int a;
        double wtemp = 0, wwr = 0, wpr, wpi, wsi = 0, theta;
        double tempr = 0, tempi = 0;

        ///for(int r=0;r<nnn;r++){
        //fata[2*r+1]=fdr[r];
        //fata[2*r+2]=fdi[r];
        //}

        n = nnn << 1;
        j = 1;
        for (i = 1;
             i < n;
             i += 2)
            {
            if (j > i)
                {
                temp = fata[j];
                fata[j] = fata[i];
                fata[i] = temp;
                temp = fata[j + 1];
                fata[j + 1] = fata[i + 1];
                fata[i + 1] = temp;
                }
            m = n >> 1;
            while (m >= 2 && j > m)
                {
                j -= m;
                m >>= 1;
                }
            j += m;
            }
        mmax = 2;
        while (n > mmax)
            {
            istep = 2 * mmax;
            theta = 6.28318530717959 / (double) (isign * mmax);
            wtemp = Math.sin(0.5 * theta);
            wpr = -2.0 * wtemp * wtemp;
            wpi = Math.sin(theta);
            wwr = 1.0;
            wsi = 0.0;
            for (m = 1;
                 m < mmax;
                 m += 2)
                {
                for (i = m;
                     i <= n;
                     i += istep)
                    {
                    j = i + mmax;
                    tempr = wwr * fata[j] - wsi * fata[j + 1];
                    tempi = wwr * fata[j + 1] + wsi * fata[j];
                    fata[j] = fata[i] - tempr;
                    fata[j + 1] = fata[i + 1] - tempi;
                    fata[i] += tempr;
                    fata[i + 1] += tempi;
                    }
                wwr = (wtemp = wwr) * wpr - wsi * wpi + wwr;
                wsi = wsi * wpr + wtemp * wpi + wsi;
                }
            mmax = istep;
            }
        if (isign == -1)
            {
            for (a = 1;
                 a < nnn + 1;
                 a++)
                {
                fda3rx[a - 1] = fata[2 * a - 1] / (double) nnn;
                fda3ix[a - 1] = fata[2 * a] / (double) nnn;
                }
            }


        }
    }
