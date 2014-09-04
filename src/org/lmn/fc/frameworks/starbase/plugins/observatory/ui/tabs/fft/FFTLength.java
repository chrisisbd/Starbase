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


/***************************************************************************************************
 * FFTLength.
 */

public enum FFTLength
    {
    FFT_32      (0,  5,   32,   "32"),
    FFT_64      (1,  6,   64,   "64"),
    FFT_128     (2,  7,  128,  "128"),
    FFT_256     (3,  8,  256,  "256"),
    FFT_512     (4,  9,  512,  "512"),
    FFT_1024    (5, 10, 1024, "1024"),
    FFT_2048    (6, 11, 2048, "2048");


    public static final String TOOLTIP = "FFT Length";

    private final int intIndex;
    private final int intBitCount;
    private final int intLength;
    private final String strName;


    /***********************************************************************************************
     * Get the FFTLength enum corresponding to the specified FFTLength name.
     * Return NULL if the FFTLength name is not found.
     *
     * @param name
     *
     * @return FFTLength
     */

    public static FFTLength getFFTLengthForName(final String name)
        {
        FFTLength fftLength;

        fftLength = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final FFTLength[] lengths;
            boolean boolFoundIt;

            lengths = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < lengths.length);
                 i++)
                {
                final FFTLength length;

                length = lengths[i];

                if (name.equals(length.getName()))
                    {
                    fftLength = length;
                    boolFoundIt = true;
                    }
                }
            }

        return (fftLength);
        }


    /***********************************************************************************************
     * Construct a FFTLength.
     *
     * @param index
     * @param bits
     * @param length
     * @param name
     */

    private FFTLength(final int index,
                      final int bits,
                      final int length,
                      final String name)
        {
        intIndex = index;
        intBitCount = bits;
        intLength = length;
        strName = name;
        }


    /***********************************************************************************************
     * Get the FFTLength index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the FFTLength number of Bits.
     *
     * @return int
     */

    public int getBitCount()
        {
        return (this.intBitCount);
        }


    /***********************************************************************************************
     * Get the FFTLength Length.
     *
     * @return int
     */

    public int getLength()
        {
        return (this.intLength);
        }


    /***********************************************************************************************
     * Get the FFTLength name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the FFTLength as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }