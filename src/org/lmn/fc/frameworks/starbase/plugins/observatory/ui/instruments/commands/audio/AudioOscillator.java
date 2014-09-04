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


import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.FixedOscillator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.OscillatorWaveform;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;

import javax.sound.sampled.*;
import java.io.IOException;


/***************************************************************************************************
 * AudioOscillator.
 */

public final class AudioOscillator implements OscillatorInterface
    {
    private static final int STARTUP_DELAY_MILLIS = 500;

    // Oscillator Parameters
    private Mixer.Info         mixerInfo;
    private OscillatorWaveform waveformType;
    private float              floatSampleRate;
    private double             dblSignalFrequencyStart;
    private double             dblSignalFrequencyEnd;
    private double             dblAmplitude;
    private int                intSweepStepSize;
    private int                intSweepDwellTime;

    // Oscillator Thread
    private SwingWorker workerOscillator;
    private boolean     boolOscillatorRunning;
    private AudioFormat audioFormat;


    /***********************************************************************************************
     * AudioOscillator.
     */

    public AudioOscillator()
        {
        final Mixer.Info[] arrayMixerInfo;

        arrayMixerInfo = AudioSystem.getMixerInfo();

        if ((arrayMixerInfo != null)
            && (arrayMixerInfo.length > 0))
            {
            // A reasonable default?
            this.mixerInfo = arrayMixerInfo[0];
            }
        else
            {
            // If there are no Mixers, we are not going to get very far :-)
            this.mixerInfo = null;
            }

        this.waveformType = DEFAULT_WAVEFORM;
        this.floatSampleRate = DEFAULT_SAMPLE_RATE;
        this.dblSignalFrequencyStart = DEFAULT_FREQUENCY_START;
        this.dblSignalFrequencyEnd = DEFAULT_FREQUENCY_END;
        this.dblAmplitude = DEFAULT_AMPLITUDE;
        this.intSweepStepSize = DEFAULT_SWEEP_STEP_SIZE;
        this.intSweepDwellTime = DEFAULT_SWEEP_DWELL_TIME;

        this.workerOscillator = null;
        this.boolOscillatorRunning = false;
        this.audioFormat = null;
        }


    /***********************************************************************************************
     * Initialise the Oscillator, to provide data for the specified DAO.
     *
     * @param dao
     *
     * @return boolean
     */

    public boolean initialise(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "AudioOscillator.initialise() ";
        final OscillatorInterface thisOscillator;

        thisOscillator = this;

        // Constructs an AudioFormat with a linear PCM encoding and the given parameters.
        // The frame size is set to the number of bytes required to contain
        // one sample from each channel, and the frame rate is set to the sample rate.
        // Always make two channel (stereo) 16 bit samples for now
        setAudioFormat(new AudioFormat(getSignalSampleRate(),    // Sample Rate
                                       DEFAULT_BITS_PER_SAMPLE,  // Bits
                                       CHANNELS,                 // Channels
                                       true,                     // Signed
                                       true));                   // Big Endian);

        setOscillatorWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
            {
            /*******************************************************************************
             * Run the Oscillator
             *
             * @return Object
             */

            public Object construct()
                {
                final FixedOscillator oscillatorFreeRun;

                // Update all Resources
                dao.readResources();

                // An application writes audio bytes to a source data line
                // A Line.Info object contains information about a line.
                // The only information provided by Line.Info itself is the Java class of the line.
                // A subclass of Line.Info adds other kinds of information about the line.
                // This additional information depends on which Line subinterface is
                // implemented by the kind of line that the Line.Info subclass describes.
                // Besides the class information inherited from its superclass,
                // DataLine.Info provides additional information specific to data lines.
                // This information includes:
                // the audio formats supported by the data line and the minimum and maximum sizes of its internal buffer
                // Because a Line.Info knows the class of the line it describes,
                // a DataLine.Info object can describe DataLine subinterfaces
                // such as SourceDataLine, TargetDataLine, and Clip.
                // You can query a mixer for lines of any of these types,
                // passing an appropriate instance of DataLine.Info
                // as the argument to a method such as Mixer.getLine(Line.Info).

                // Generate the waveform data using the current properties
                // Warning! framecount *may* be AudioSystem.NOT_SPECIFIED
                // This causes read() to run forever...
                // which is what we need for a free-running oscillator
                oscillatorFreeRun = new FixedOscillator(getAudioFormat(),
                                                        getWaveformType(),
                                                        getSignalFrequencyStart(),
                                                        getSignalAmplitude(),
                                                        AudioSystem.NOT_SPECIFIED);
                try
                    {
                    final SourceDataLine sourceDataLine;
                    final byte[] arrayData;

                    // Prepare to run the Oscillator,
                    if ((getMixerInfo() == null)
                        || (getAudioFormat() == null))
                        {
                        throw new LineUnavailableException("The Oscillator has not been configured correctly");
                        }

                    AudioHelper.debugAudioOscillatorToFile(thisOscillator);

                    // if the format requested is supported by the current hardware
                    // Throw a LineUnavailableException if not....
                    // From the viewpoint of the application (as opposed to the viewpoint of the mixer),
                    // a SourceDataLine is a target for audio data (such as a speaker)
                    sourceDataLine = AudioSystem.getSourceDataLine(getAudioFormat(),
                                                                   getMixerInfo());
                    sourceDataLine.open(getAudioFormat());
                    sourceDataLine.start();

                    // If we get here without Exception, we should be running...
                    setRunning(true);

                    arrayData = new byte[BUFFER_SIZE];

                    while ((isRunning())
                           && (Utilities.workerCanProceed(dao, getOscillatorWorker())))
                        {
                        final int intBytesRead;
                        final int intBytesWritten;

                        // We musn't timeout, since this might run forever...
                        TimeoutHelper.restartDAOTimeoutTimerInfinite(dao);

                        if (!oscillatorFreeRun.getFormat().equals(getAudioFormat()))
                            {
                            AudioHelper.debugAudioOscillatorToFile(thisOscillator);
                            }

                        // Check to see if there have been any (sensible) changes to the configuration
                        // Ignore any changes to the Mixer!
                        oscillatorFreeRun.setAudioFormat(getAudioFormat());
                        oscillatorFreeRun.setWaveformType(getWaveformType());
                        oscillatorFreeRun.setFrequency(getSignalFrequencyStart());
                        oscillatorFreeRun.setAmplitude(getSignalAmplitude());

                        // Repeatedly read the Oscillator and write to the target output
                        intBytesRead = oscillatorFreeRun.read(arrayData);
                        intBytesWritten = sourceDataLine.write(arrayData, 0, intBytesRead);
                        }

                    // Shut down neatly
                    sourceDataLine.drain();
                    sourceDataLine.stop();
                    sourceDataLine.close();
                    }

                catch (final IllegalArgumentException exception)
                    {
                    setRunning(false);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET + TARGET_OSCILLATOR + TERMINATOR
                                                           + METADATA_ACTION_INITIALISE
                                                           + METADATA_EXCEPTION
                                                           + "IllegalArgumentException "
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                catch (final LineUnavailableException exception)
                    {
                    setRunning(false);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET + TARGET_OSCILLATOR + TERMINATOR_SPACE
                                                           + METADATA_ACTION_INITIALISE
                                                           + METADATA_EXCEPTION
                                                           + "LineUnavailableException "
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                catch (final IOException exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET + TARGET_OSCILLATOR + TERMINATOR_SPACE
                                                           + METADATA_ACTION_INITIALISE
                                                           + METADATA_EXCEPTION
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                catch (final Exception exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET + TARGET_OSCILLATOR + TERMINATOR_SPACE
                                                           + METADATA_ACTION_INITIALISE
                                                           + METADATA_EXCEPTION
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                return (null);
                }


            /*******************************************************************************
             * When the Thread stops.
             */

            public void finished()
                {
                setRunning(false);

                // Put the Timeout back to what it should be for a single default command
                TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault(dao);
                }
            });

        // Start the Thread we have prepared...
        getOscillatorWorker().start();

        // Wait until the Oscillator has a chance to start up (or not)
        Utilities.safeSleep(STARTUP_DELAY_MILLIS);

        return (isRunning());
        }


    /***********************************************************************************************
     * Stop the Oscillator and dispose of all Resources.
     */

    public void dispose()
        {
        final String SOURCE = "AudioOscillator.dispose() ";

        //LOGGER.logTimedEvent(SOURCE);

        setRunning(false);

        SwingWorker.disposeWorker(getOscillatorWorker(), true, SWING_WORKER_STOP_DELAY);
        setOscillatorWorker(null);
        }


    /***********************************************************************************************
     * Get the MixerInfo.
     *
     * @return Mixer.Info
     */

    public Mixer.Info getMixerInfo()
        {
        return (this.mixerInfo);
        }


    /***********************************************************************************************
     * Set the MixerInfo.
     *
     * @param info
     */

    public void setMixerInfo(final Mixer.Info info)
        {
        this.mixerInfo = info;
        }


    /***********************************************************************************************
     * Get the AudioFormat.
     *
     * @return AudioFormat
     */

    public AudioFormat getAudioFormat()
        {
        return (this.audioFormat);
        }


    /***********************************************************************************************
     * Set the AudioFormat.
     *
     * @param format
     */

    public void setAudioFormat(final AudioFormat format)
        {
        this.audioFormat = format;
        }


    /***********************************************************************************************
     * Get the Waveform Type.
     *
     * @return OscillatorWaveform
     */

    public OscillatorWaveform getWaveformType()
        {
        return (this.waveformType);
        }


    /***********************************************************************************************
     * Set the Waveform Type.
     *
     * @param type
     */

    public void setWaveformType(final OscillatorWaveform type)
        {
        this.waveformType = type;
        }


    /***********************************************************************************************
     * Get the Signal Sample Rate.
     *
     * @return float
     */

    public float getSignalSampleRate()
        {
        return (this.floatSampleRate);
        }


    /***********************************************************************************************
     * Set the Signal Sample Rate.
     *
     * @param samplerate
     */

    public void setSignalSampleRate(final float samplerate)
        {
        this.floatSampleRate = samplerate;

        // There is no way to change the SampleRate without making a new AudioFormat
        setAudioFormat(new AudioFormat(getSignalSampleRate(),    // Sample Rate
                                       DEFAULT_BITS_PER_SAMPLE,  // Bits
                                       CHANNELS,                 // Channels
                                       true,                     // Signed
                                       true));                   // Big Endian);
        }


    /***********************************************************************************************
     * Get the Signal Frequency Start.
     *
     * @return double
     */

    public double getSignalFrequencyStart()
        {
        return (this.dblSignalFrequencyStart);
        }


    /***********************************************************************************************
     * Set the Signal Frequency Start.
     *
     * @param frequencystart
     */

    public void setSignalFrequencyStart(final double frequencystart)
        {
        this.dblSignalFrequencyStart = frequencystart;
        }


    /***********************************************************************************************
     * Get the Signal Frequency End.
     *
     * @return double
     */

    public double getSignalFrequencyEnd()
        {
        return (this.dblSignalFrequencyEnd);
        }


    /***********************************************************************************************
     * Set the Signal Frequency End.
     *
     * @param frequencyend
     */

    public void setSignalFrequencyEnd(final double frequencyend)
        {
        this.dblSignalFrequencyEnd = frequencyend;
        }


    /***********************************************************************************************
     * Get the Signal Amplitude as a fraction {0...1}.
     *
     * @return double
     */

    public double getSignalAmplitude()
        {
        return (this.dblAmplitude);
        }


    /***********************************************************************************************
     * Set the Oscillator Amplitude as a fraction {0...1}.
     *
     * @param amplitude
     */

    public void setSignalAmplitude(final double amplitude)
        {
        this.dblAmplitude = amplitude;
        }


    /***********************************************************************************************
     * Get the Sweep Step Size.
     *
     * @return int
     */

    public int getSweepStepSize()
        {
        return (this.intSweepStepSize);
        }


    /***********************************************************************************************
     * Set the Sweep Step Size.
     *
     * @param sweepstepsize
     */

    public void setSweepStepSize(final int sweepstepsize)
        {
        this.intSweepStepSize = sweepstepsize;
        }


    /***********************************************************************************************
     * Get the Sweep Dwell Time.
     *
     * @return int
     */

    public int getSweepDwellTime()
        {
        return (this.intSweepDwellTime);
        }


    /***********************************************************************************************
     * Set the Sweep Dwell Time.
     *
     * @param dwelltime
     */

    public void setSweepDwellTime(final int dwelltime)
        {
        this.intSweepDwellTime = dwelltime;
        }


    /***********************************************************************************************
     * Get the SwingWorker which handles the Oscillator.
     *
     * @return SwingWorker
     */

    public SwingWorker getOscillatorWorker()
        {
        return (this.workerOscillator);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the Oscillator.
     *
     * @param worker
     */

    public void setOscillatorWorker(final SwingWorker worker)
        {
        this.workerOscillator = worker;
        }


    /***********************************************************************************************
     * Indicate if the Oscillator is running.
     *
     * @return boolean
     */

    public boolean isRunning()
        {
        return (this.boolOscillatorRunning);
        }


    /***********************************************************************************************
     * Control the Oscillator state.
     *
     * @param running
     */

    public void setRunning(final boolean running)
        {
        this.boolOscillatorRunning = running;
        }
    }
