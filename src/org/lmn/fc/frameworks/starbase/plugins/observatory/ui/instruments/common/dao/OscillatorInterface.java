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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.OscillatorWaveform;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;


/***************************************************************************************************
 * OscillatorInterface.
 */

public interface OscillatorInterface extends FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             ObservatoryConstants
    {
    // String Resources
    String TARGET_OSCILLATOR   = "oscillator";
    String MSG_ALREADY_STOPPED = "Oscillator is already stopped";
    String MSG_ALREADY_RUNNING = "Oscillator is already running";
    String MSG_FAILED_TO_START = "Oscillator failed to start";

    int BUFFER_SIZE = 128000;

    OscillatorWaveform DEFAULT_WAVEFORM         = OscillatorWaveform.SINE;
    float              DEFAULT_SAMPLE_RATE      = 44100.0f;
    int                DEFAULT_FREQUENCY_START  = 1000;
    int                DEFAULT_FREQUENCY_END    = 10000;
    double             DEFAULT_AMPLITUDE        = 0.9999;
    int                DEFAULT_SWEEP_STEP_SIZE  = 100;
    int                DEFAULT_SWEEP_DWELL_TIME = 1;
    int                DEFAULT_BITS_PER_SAMPLE  = 16;
    int                CHANNELS                 = 2;


    /***********************************************************************************************
     * Initialise the Oscillator, to provide data for the specified DAO.
     *
     * @param dao
     *
     * @return boolean
     */

    boolean initialise(ObservatoryInstrumentDAOInterface dao);


    /***********************************************************************************************
     * Stop the Oscillator and dispose of all Resources.
     */

    void dispose();


    /***********************************************************************************************
     * Get the MixerInfo.
     *
     * @return Mixer.Info
     */

    Mixer.Info getMixerInfo();


    /***********************************************************************************************
     * Set the MixerInfo.
     *
     * @param info
     */

    void setMixerInfo(Mixer.Info info);


    /***********************************************************************************************
     * Get the AudioFormat.
     *
     * @return AudioFormat
     */

    AudioFormat getAudioFormat();


    /***********************************************************************************************
     * Set the AudioFormat.
     *
     * @param format
     */

    void setAudioFormat(final AudioFormat format);


    /***********************************************************************************************
     * Get the Waveform Type.
     *
     * @return OscillatorWaveform
     */

    OscillatorWaveform getWaveformType();


    /***********************************************************************************************
     * Set the Waveform Type.
     *
     * @param type
     */

    void setWaveformType(OscillatorWaveform type);


    /***********************************************************************************************
     * Get the Signal Sample Rate.
     *
     * @return float
     */

    float getSignalSampleRate();


    /***********************************************************************************************
     * Set the Signal Sample Rate.
     *
     * @param samplerate
     */

    void setSignalSampleRate(float samplerate);


    /***********************************************************************************************
     * Get the Signal Frequency Start.
     *
     * @return double
     */

    double getSignalFrequencyStart();


    /***********************************************************************************************
     * Set the Signal Frequency Start.
     *
     * @param frequencystart
     */

    void setSignalFrequencyStart(double frequencystart);


    /***********************************************************************************************
     * Get the Signal Frequency End.
     *
     * @return double
     */

    double getSignalFrequencyEnd();


    /***********************************************************************************************
     * Set the Signal Frequency End.
     *
     * @param frequencyend
     */

    void setSignalFrequencyEnd(double frequencyend);


    /***********************************************************************************************
     * Get the Signal Amplitude as a fraction {0...1}.
     *
     * @return double
     */

    double getSignalAmplitude();


    /***********************************************************************************************
     * Set the Oscillator Amplitude as a fraction {0...1}.
     *
     * @param amplitude
     */

    void setSignalAmplitude(double amplitude);


    /***********************************************************************************************
     * Get the Sweep Step Size.
     *
     * @return int
     */

    int getSweepStepSize();


    /***********************************************************************************************
     * Set the Sweep Step Size.
     *
     * @param sweepstepsize
     */

    void setSweepStepSize(int sweepstepsize);


    /***********************************************************************************************
     * Get the Sweep Dwell Time.
     *
     * @return int
     */

    int getSweepDwellTime();


    /***********************************************************************************************
     * Set the Sweep Dwell Time.
     *
     * @param dwelltime
     */

    void setSweepDwellTime(int dwelltime);


    /***********************************************************************************************
     * Get the SwingWorker which handles the Oscillator.
     *
     * @return SwingWorker
     */

    SwingWorker getOscillatorWorker();


    /***********************************************************************************************
     * Set the SwingWorker which handles the Oscillator.
     *
     * @param worker
     */

    void setOscillatorWorker(SwingWorker worker);


    /***********************************************************************************************
     * Indicate if the Oscillator is running.
     *
     * @return boolean
     */

    boolean isRunning();


    /***********************************************************************************************
     * Control the Oscillator state.
     *
     * @param running
     */

    void setRunning(boolean running);
    }
