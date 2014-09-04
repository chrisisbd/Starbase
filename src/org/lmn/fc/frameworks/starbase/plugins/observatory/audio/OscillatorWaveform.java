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

package org.lmn.fc.frameworks.starbase.plugins.observatory.audio;


import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * OscillatorWaveform.
 */

public enum OscillatorWaveform
    {
    SINE            (0, "Sine"),
    SQUARE          (1, "Square"),
    TRIANGLE        (2, "Triangle"),
    SAWTOOTH        (3, "Sawtooth"),
    RANDOM_NOISE    (4, "RandomNoise"),
    GAUSSIAN_NOISE  (5, "GaussianNoise"),
    HARMONIC_SERIES (6, "HarmonicSeries");


    private final int intIndex;
    private final String strType;


    /***********************************************************************************************
     * Get the OscillatorWaveform enum corresponding to the specified OscillatorWaveform name.
     * Return NULL if not found.
     *
     * @param type
     *
     * @return OscillatorWaveform
     */

    public static OscillatorWaveform getOscillatorWaveformForName(final String type)
        {
        OscillatorWaveform oscillatorWaveform;

        oscillatorWaveform = null;

        if ((type != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(type)))
            {
            final OscillatorWaveform[] oscillatorWaveforms;
            boolean boolFoundIt;

            oscillatorWaveforms = OscillatorWaveform.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < oscillatorWaveforms.length);
                 i++)
                {
                final OscillatorWaveform waveform;

                waveform = oscillatorWaveforms[i];

                if (type.equals(waveform.getWaveformType()))
                    {
                    oscillatorWaveform = waveform;
                    boolFoundIt = true;
                    }
                }
            }

        if (oscillatorWaveform == null)
            {
            throw new IllegalArgumentException("OscillatorWaveform.getOscillatorWaveformForName() "
                                                    + FrameworkStrings.EXCEPTION_RESOURCE_NOTFOUND
                                                    + FrameworkMetadata.METADATA_NAME
                                                    + type
                                                    + FrameworkMetadata.TERMINATOR);
            }

        return (oscillatorWaveform);
        }


    /***********************************************************************************************
     * OscillatorWaveform.
     *
     * @param index
     * @param type
     */

    private OscillatorWaveform(final int index,
                               final String type)
        {
        intIndex = index;
        strType = type;
        }


    /***********************************************************************************************
     * Get the WaveformType Index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the WaveformType.
     *
     * @return String
     */

    public String getWaveformType()
        {
        return (this.strType);
        }


    /***********************************************************************************************
     * Get the WaveformType name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strType);
        }
    }
