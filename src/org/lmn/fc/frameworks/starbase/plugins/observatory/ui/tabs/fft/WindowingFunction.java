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

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.maths.BesselFunction;


/***************************************************************************************************
 * WindowingFunction.
 * See: http://www.y1pwe.co.uk/AppletCD/WindFunctions.htm
 * See: http://en.wikipedia.org/wiki/Window_function
 * See: http://www.dspguide.com
 */

public enum WindowingFunction
    {
    WINDOW_NONE                 (0,  new Identity(),            "None",                 ""),
    WINDOW_BLACKMAN             (1,  new Blackman(),            "Blackman",             ""),
    WINDOW_BLACKMAN_HARRIS_3    (2,  new Blackman_Harris_3(),   "Blackman_Harris_3",    ""),
    WINDOW_BLACKMAN_HARRIS_4    (3,  new Blackman_Harris_4(),   "Blackman_Harris_4",    ""),
    WINDOW_CHEBYSHEV            (4,  new Chebyshev(),           "Chebyshev",            ""),
    WINDOW_FLAT_TOP             (5,  new FlatTop(),             "FlatTop",              ""),
    WINDOW_GAUSSIAN             (6,  new Gaussian(),            "Gaussian",             ""),
    WINDOW_HAMMING              (7,  new Hamming(),             "Hamming",              ""),
    WINDOW_HANNING              (8,  new Hanning(),             "Hanning",              "von Hann, raised cosine window"),
    WINDOW_KAISER               (9,  new Kaiser(),              "Kaiser",               "Kaiser-Bessel"),
    WINDOW_LANCZOS              (10, new Lanczos(),             "Lanczos",              ""),
    WINDOW_RECTANGULAR          (11, new Rectangular(),         "Rectangular",          "Rectangular Window Function (Boxcar or Dirichlet)"),
    WINDOW_RIESZ                (12, new Riesz(),               "Riesz",                ""),
    WINDOW_TRIANGULAR           (13, new Triangular(),          "Triangular",           "");


    public static final String TOOLTIP = "Windowing Function";

    private final int intIndex;
    private final WindowingFunctionInterface windowingFunctionInterface;
    private final String strName;
    private final String strDescription;


    /**********************************************************************************************
     * Get the WindowingFunction enum corresponding to the specified WindowingFunction name.
     * Return NULL if the WindowingFunction name is not found.
     *
     * @param name
     *
     * @return WindowingFunction
     */

    public static WindowingFunction getWindowingFunctionForName(final String name)
        {
        WindowingFunction windowingFunction;

        windowingFunction = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final WindowingFunction[] functions;
            boolean boolFoundIt;

            functions = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < functions.length);
                 i++)
                {
                final WindowingFunction function;

                function = functions[i];

                if (name.equals(function.getName()))
                    {
                    windowingFunction = function;
                    boolFoundIt = true;
                    }
                }
            }

        return (windowingFunction);
        }


    /**********************************************************************************************
     * Construct a WindowingFunction.
     *
     * @param index
     * @param function
     * @param name
     * @param description
     */

    private WindowingFunction(final int index,
                              final WindowingFunctionInterface function,
                              final String name,
                              final String description)
        {
        this.intIndex = index;
        this.windowingFunctionInterface = function;
        this.strName = name;
        this.strDescription = description;
        }


    /**********************************************************************************************
     * Get the WindowingFunction index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /**********************************************************************************************
     * Get the WindowingFunction.
     *
     * @return WindowingFunctionInterface
     */

    public WindowingFunctionInterface getWindowingFunction()
        {
        return (this.windowingFunctionInterface);
        }


    /**********************************************************************************************
     * Get the WindowingFunction name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /**********************************************************************************************
     * Get the WindowingFunction description.
     *
     * @return String
     */

    public String getDescription()
        {
        return (this.strDescription);
        }


    /**********************************************************************************************
     * Get the WindowingFunction as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }


    /**********************************************************************************************
     * Do the windowing function to find w(n) for a window of N samples.
     *
     * @param d0
     * @param length
     *
     * @return double
     */

    public double window(final double d0,
                         final int length)
        {
        return (getWindowingFunction().window(d0, length));
        }


    /**************************************************************************************************
     * Windowing Functions which are enumerated.
     **************************************************************************************************/

    private static final class Identity implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            // An alias for Rectangular
            return (1.0);
            }
        }


    private static final class Rectangular implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            return (1.0);
            }
        }


    private static final class Riesz implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double w;

            a0 = ((n - (N / 2)) * 2) / (N + 1);
            w = 1 - (a0 * a0);

            return (w);
            }
        }


    private static final class Hamming implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double a1;
            final double a2;
            final double w;

            a0 = 0.54;
            a1 = 0.46;
            a2 = 0.0;

            w = a0
                + a1 * Math.cos((2 * Math.PI * (n - (N / 2))) / N)
                + a2 * Math.cos((2 * 2 * Math.PI * (n - (N / 2))) / N);

            return (w);
            }
        }


    private static final class Hanning implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double a1;
            final double a2;
            final double w;

            a0 = 0.5;
            a1 = 0.5;
            a2 = 0.0;

            // ToDo REVIEW - This seems to be the same as Hamming?
            w = a0
                + a1 * Math.cos((2 * Math.PI * (n - (N / 2))) / N)
                + a2 * Math.cos((2 * 2 * Math.PI * (n - (N / 2))) / N);

            return (w);
            }
        }


    private static final class FlatTop implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double a1;
            final double a2;
            final double w;

            a0 = 0.2810639;
            a1 = 0.5208972;
            a2 = 0.1980399;

            // ToDo REVIEW - This does not agree with Wikipedia
            w = a0
                + a1 * Math.cos(2 * Math.PI * (n - N / 2) / N)
                + a2 * Math.cos(2 * 2 * Math.PI * (n - N / 2) / N);

            return (w);
            }
        }


    private static final class Lanczos implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            double w;

            // ToDo ?? (2n / (N - 1)) - 1

            a0 = (2 * (n - (N / 2)) * Math.PI) / (N + 1);

            if (a0 == 0.0)
                {
                w = 1;
                }
            else
                {
                // This is sinc(), i.e. sin(x) sinc(x) = x
                w = Math.sin(a0) / a0;
                }

            if (w < 0.0)
                {
                w = 0.0;
                }

            return (w);
            }
        }


    private static final class Triangular implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double w;

            if (n < (N * 2))
                {
                w = (n * 2) / N;
                }
            else
                {
                w = ((N - n) * 2) / N;
                }

            return (w);
            }
        }


    private static final class Chebyshev implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Arc Hyperbolic Cosine.
         *
         * @param x
         *
         * @return double
         */

        private static double acosh(final double x)
            {
            return (Math.log(x + Math.sqrt(x * x - 1.0)));
            }


        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double w;

            // ToDo REVIEW - Reference?
            if (Math.abs(n) > 1.0)
                {
                w = Math.cosh(N * acosh(n));
                }
            else
                {
                w = Math.cos(N * Math.acos(n));
                }

            return (w);
            }
        }


    private static final class Kaiser implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double w;

            // http://mathworld.wolfram.com/BesselFunctionoftheFirstKind.html

            // Variable parameter ? determines the tradeoff between main lobe width
            // and side lobe levels of the spectral leakage pattern.
            // A typical value of ? is 3

            a0 = Math.PI * 3.0;

            if (n < (N + 1))
                {
                final double dblVar;

                dblVar = ((2 * n) / N) - 1;

                // ToDo REVIEW - Is this what was intended?  BesselFunction is from http://xaldev.sourceforge.net
                w = BesselFunction.Jn(0, a0 * Math.sqrt(1 - (dblVar * dblVar)))
                    / BesselFunction.Jn(0, a0);
                }
            else
                {
                w = 0.0;
                }

            return (w);
            }
        }


    private static final class Gaussian implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double w;
            final double dblVar;

            // ToDo REVIEW
            a0 = 0.5;

            dblVar = (2 * a0 * (n - (N / 2))) / N;

            // The Fourier transform of a Gaussian is also a Gaussian
            // ToDo REVIEW http://en.wikipedia.org/wiki/Window_function- How does this relate to Wikipedia?
            w = Math.exp(-0.5 * dblVar * dblVar);

            return (w);
            }
        }


    private static final class Blackman implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double a1;
            final double a2;
            final double w;

            a0 = 0.42659;
            a1 = -0.49656;
            a2 = 0.076849;

            // ToDo REVIEW - Wikipedia says n / (N - 1)
            w = a0
                + a1 * Math.cos(2 * Math.PI * n / N)
                + a2 * Math.cos(2 * 2 * Math.PI * n / N);

            return (w);
            }
        }


    private static final class Blackman_Harris_3 implements WindowingFunctionInterface
        {
        // A generalization of the Hamming family,
        // produced by adding more shifted sinc functions,
        // meant to minimize side-lobe levels


        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double a1;
            final double a2;
            final double w;

            a0 = 0.44951;
            a1 = -0.49364;
            a2 = 0.05677;

            // ToDo REVIEW - How does this relate to Wikipedia?
            w = a0
                + a1 * Math.cos(2 * Math.PI * n / N)
                + a2 * Math.cos(2 * 2 * Math.PI * n / N);

            return (w);
            }
        }


    private static final class Blackman_Harris_4 implements WindowingFunctionInterface
        {
        /**********************************************************************************************
         * Do the windowing function to find w(n) for a window of N samples.
         *
         * @param n
         * @param N
         *
         * @return double
         */

        public double window(final double n,
                             final double N)
            {
            final double a0;
            final double a1;
            final double a2;
            final double a3;
            final double w;

            a0 = 0.40217;
            a1 = -0.49703;
            a2 = 0.09892;
            a3 = -0.001188;

            // ToDo REVIEW - How does this relate to Wikipedia?
            w = a0
                + a1 * Math.cos(2 * Math.PI * n / N)
                + a2 * Math.cos(2 * 2 * Math.PI * n / N)
                + a3 * Math.cos(2 * 3 * Math.PI * n / N);

            return (w);
            }
        }
    }

