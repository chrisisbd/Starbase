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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.audio;


import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.FixedOscillator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.io.IOException;


/***************************************************************************************************
 * AudioHelper.
 */

public final class AudioHelper
    {
    /***********************************************************************************************
     * Find a MixerInfo, given the Mixer's name.
     * Return NULL if the Mixer was not found.
     *
     * @param mixername
     *
     * @return Mixer.Info
     */

    public static Mixer.Info findMixerInfoFromName(final String mixername)
        {
        final Mixer.Info[] arrayMixerInfo;
        Mixer.Info info;
        boolean boolFoundIt;

        arrayMixerInfo = AudioSystem.getMixerInfo();
        info = null;
        boolFoundIt = false;

        for (int intInfoIndex = 0;
             ((intInfoIndex < arrayMixerInfo.length)
                && (!boolFoundIt));
             intInfoIndex++)
            {
            info = arrayMixerInfo[intInfoIndex];

            if ((info != null)
                && (mixername != null)
                && (mixername.equals(info.getName())))
                {
                boolFoundIt = true;
                }
            }

        return (info);
        }


    /***********************************************************************************************
     * Write an audio file to debug the Oscillator.
     *
     * @param oscillator
     *
     * @throws IOException
     */

    public static void debugAudioOscillatorToFile(final OscillatorInterface oscillator) throws IOException
        {
        if ((oscillator != null)
            && (oscillator.getAudioFormat() != null)
            && (FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug()))
            {
            final AudioInputStream oscillator5sec;
            final long longFrameCount5sec;

            // Calculate the FrameCount to make the Oscillator stop after 5sec
            // Use this Oscillator to write a test file
            // We know the frame rate is the same as the sample rate, not NOT_SPECIFIED
            longFrameCount5sec = (long) (oscillator.getAudioFormat().getFrameRate() * 5);

            oscillator5sec = new FixedOscillator(oscillator.getAudioFormat(),
                                                 oscillator.getWaveformType(),
                                                 oscillator.getSignalFrequencyStart(),
                                                 oscillator.getSignalAmplitude(),
                                                 longFrameCount5sec);

            // Write a test file each time we run in debug
            AudioSystem.write(oscillator5sec,
                              AudioFileFormat.Type.WAVE,
                              new File("exports/Oscillator("
                                           + oscillator.getWaveformType().getWaveformType()
                                           + ")_"
                                           + (int) oscillator.getAudioFormat().getSampleRate()
                                           + "samples-sec_"
                                           + oscillator.getSignalFrequencyStart() + "Hz.wav"));

//            oscillator5sec = new HarmonicOscillator(oscillator.getAudioFormat(),
//                                                    oscillator.getWaveformType(),
//                                                    oscillator.getSignalFrequencyStart(),
//                                                    oscillator.getSignalAmplitude(),
//                                                    longFrameCount5sec);
//
//            // Write a test file each time we run in debug
//            AudioSystem.write(oscillator5sec,
//                              AudioFileFormat.Type.WAVE,
//                              new File("exports/HarmonicOscillator_"
//                                           + oscillator.getWaveformType().getWaveformType()
//                                           + "_"
//                                           + (int) oscillator.getAudioFormat().getSampleRate()
//                                           + "samples-sec_"
//                                           + oscillator.getSignalFrequencyStart() + "Hz.wav"));
            }
        }
    }
