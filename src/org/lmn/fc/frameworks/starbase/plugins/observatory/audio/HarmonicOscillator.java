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

package org.lmn.fc.frameworks.starbase.plugins.observatory.audio;


import org.lmn.fc.common.constants.AstronomyConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.utilities.maths.AstroMath;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;


/***************************************************************************************************
 * HarmonicOscillator.
 *
 * See: http://en.wikipedia.org/wiki/Java_Media_Framework
 * See: http://www.developer.com/java/other/article.php/2226701/Java-Sound-Creating-Playing-and-Saving-Synthetic-Sounds.htm
 * See: http://dickbaldwin.com/tocadv.htm
 * See: http://www.developer.com/java/other/article.php/1579071
 * See: http://www.docjar.com/html/api/jm/audio/synth/Oscillator.java.html
 * See: http://en.wikipedia.org/wiki/Aliasing
 */

public class HarmonicOscillator extends AudioInputStream
                                implements FrameworkSingletons,
                                           AstronomyConstants
    {
    private static final int BITS_PER_SAMPLE = 16;
    private static final int STEREO = 2;
    private static final int BYTE_MASK = 0xff;

    // Injections
    private OscillatorWaveform waveformType;      // May be changed when running

    private final double dblSampleWordSize;
    private double dblAmplitude;                  // May be changed when running
    private double dblScaledAmplitude;            // May be changed when running
    private double dblPhaseRadians;
    private double dblFrequency;                  // May be changed when running
    private double dblSamplingIncrementRadians;   // May be changed when running
    private long longRemainingFrames;
    private final Random randomGenerator;


    /***********************************************************************************************
     * HarmonicOscillator.
     * Warning! framecount *may* be AudioSystem.NOT_SPECIFIED.
     * This causes read() to run forever.
     *
     * @param audioformat
     * @param waveformtype
     * @param frequency
     * @param amplitude
     * @param framecount
     */

    public HarmonicOscillator(final AudioFormat audioformat,
                              final OscillatorWaveform waveformtype,
                              final double frequency,
                              final double amplitude,
                              final long framecount)
        {
        super(new ByteArrayInputStream(new byte[0]),
              audioformat,
              framecount);

        this.waveformType = waveformtype;

        this.dblSampleWordSize = Math.pow(2, audioformat.getSampleSizeInBits() - 1);

        // Oscillator Amplitude is supplied as a fraction {0...1}
        // e.g. for 16 bit samples, map this to 0...2^15
        this.dblAmplitude = amplitude;
        this.dblScaledAmplitude = (dblAmplitude * dblSampleWordSize);

        // The incremental phase of the generated waveform, in radians
        this.dblPhaseRadians = 0;

        this.dblFrequency = frequency;

        // The sampling increment in radians for a waveform of the specified frequency
        this.dblSamplingIncrementRadians = TWO_PI / (audioformat.getSampleRate() / dblFrequency);

        // Warning! framecount *may* be AudioSystem.NOT_SPECIFIED
        // This causes read() to run forever...
        // which is what we need for a free-running oscillator
        this.longRemainingFrames = framecount;

        // Injections
//        LOGGER.logTimedEvent("HarmonicOscillator: Waveform=" + waveformType.getWaveformType());
//        LOGGER.logTimedEvent("HarmonicOscillator: Frequency=" + frequency);
//        LOGGER.logTimedEvent("HarmonicOscillator: Fractional Amplitude=" + amplitude);
//        LOGGER.logTimedEvent("HarmonicOscillator: Scaled Amplitude=" + dblScaledAmplitude);
//        LOGGER.logTimedEvent("HarmonicOscillator: Sampling Increment=" + dblSamplingIncrementRadians);
//        LOGGER.logTimedEvent("HarmonicOscillator: Frames per second=" + getFormat().getFrameRate());
//        LOGGER.logTimedEvent("HarmonicOscillator: Frame Rate/Frequency=" + (getFormat().getFrameRate() / dblFrequency));
//        LOGGER.logTimedEvent("HarmonicOscillator: Remaining Frames=" + longRemainingFrames);
//        LOGGER.logTimedEvent("HarmonicOscillator: Sample Word Size=" + dblSampleWordSize);

        this.randomGenerator = new Random(System.currentTimeMillis());
        }


    /***********************************************************************************************
     * Set the AudioFormat.
     *
     * @param audioformat
     */

    public void setAudioFormat(final AudioFormat audioformat)
        {
        if (!getFormat().equals(audioformat))
            {
            this.format = audioformat;

            // Changing AudioFormat changes the sampling increment
            // The sampling increment in radians for a waveform of the specified frequency
            this.dblSamplingIncrementRadians = TWO_PI / (getFormat().getSampleRate() / getFrequency());

            this.dblPhaseRadians = 0.0;

            //this.longRemainingFrames = 0;
            }
        }


    /***********************************************************************************************
     * Get the Oscillator Waveform Type.
     *
     * @return OscillatorWaveform
     */

    public OscillatorWaveform getWaveformType()
        {
        return (this.waveformType);
        }


    /***********************************************************************************************
     * Set the Oscillator Waveform Type.
     *
     * @param waveformtype
     */

    public void setWaveformType(final OscillatorWaveform waveformtype)
        {
        if (!getWaveformType().equals(waveformtype))
            {
            this.waveformType = waveformtype;

            this.dblPhaseRadians = 0.0;

            //this.longRemainingFrames = 0;
            }
        }


    /***********************************************************************************************
     * Get the Signal Amplitude as a fraction {0...1}.
     *
     * @return double
     */

    public double getAmplitude()
        {
        return (this.dblAmplitude);
        }


    /***********************************************************************************************
     * Set the Oscillator Amplitude as a fraction {0...1}.
     *
     * @param amplitude
     */

    public void setAmplitude(final double amplitude)
        {
        if (getAmplitude() != amplitude)
            {
            this.dblAmplitude = amplitude;
            this.dblScaledAmplitude = (getAmplitude() * dblSampleWordSize);

            this.dblPhaseRadians = 0.0;

            //this.longRemainingFrames = 0;
            }
        }


    /***********************************************************************************************
     * Get the Signal Frequency.
     *
     * @return double
     */

    public double getFrequency()
        {
        return (this.dblFrequency);
        }


    /***********************************************************************************************
     * Set the Signal Frequency.
     *
     * @param frequency
     */

    public void setFrequency(final double frequency)
        {
        if (getFrequency() != frequency)
            {
            this.dblFrequency = frequency;

            // Changing Frequency changes the sampling increment
            // The sampling increment in radians for a waveform of the specified frequency
            this.dblSamplingIncrementRadians = TWO_PI / (getFormat().getSampleRate() / getFrequency());

            this.dblPhaseRadians = 0.0;

            //this.longRemainingFrames = 0;
            }
        }


    /***********************************************************************************************
     * Returns the number of bytes that can be read without blocking.
     * Since there is no blocking possible here, we simply try to
     * return the number of bytes available at all. In case the
     * length of the stream is indefinite, we return the highest
     * number that can be represented in an integer. If the length
     * if finite, this length is returned, clipped by the maximum
     * that can be represented.
     *
     * @return int
     */

    public int available()
        {
        final int intBytesRemaining;

        // Warning! framecount *may* be AudioSystem.NOT_SPECIFIED
        // This causes read() to run forever...
        // which is what we need for a free-running oscillator
        if (this.longRemainingFrames == AudioSystem.NOT_SPECIFIED)
            {
            intBytesRemaining = Integer.MAX_VALUE;
            }
        else
            {
            final long longBytesRemaining;

            longBytesRemaining = this.longRemainingFrames * getFormat().getFrameSize();
            intBytesRemaining = (int) Math.min(longBytesRemaining, (long) Integer.MAX_VALUE);
            }

        return (intBytesRemaining);
        }


    /***********************************************************************************************
     * Reads up to a specified maximum number of bytes of data from the audio
     * stream, putting them into the given byte array.
     * <p>This method will always read an integral number of frames.
     * If <code>len</code> does not specify an integral number
     * of frames, a maximum of <code>len - (len % frameSize)
     * </code> bytes will be read.
     * Return the total number of bytes read into the buffer, or -1 at end of stream.
     *
     * @param arraybytes
     * @param offset
     * @param length
     *
     * @return int
     *
     * @throws java.io.IOException
     */

    public int read(final byte[] arraybytes,
                    final int offset,
                    final int length) throws IOException
        {
        final double SCALING = 0.48;
        final int intDestinationOffset;
        final int intFramesToBeDelivered;
        int intValueForFrame;
        final int intBytesRead;

        intDestinationOffset = offset;

        // How many Frames can we deliver less than or equal to the requested length?
        intFramesToBeDelivered = length / getFormat().getFrameSize();

//        LOGGER.logTimedEvent("HarmonicOscillator.read(): Requested Length=" + length);
//        LOGGER.logTimedEvent("HarmonicOscillator.read(): Destination Offset=" + intDestinationOffset);

//        LOGGER.logTimedEvent("HarmonicOscillator.read(): Frames To Deliver=" + intFramesToBeDelivered);
//        LOGGER.logTimedEvent("HarmonicOscillator.read(): Frames Remaining=" + longRemainingFrames);
//        LOGGER.logTimedEvent("HarmonicOscillator.read(): Phase Radians=" + dblPhaseRadians);

        for (int intFrameIndex = 0;
            intFrameIndex < intFramesToBeDelivered;
            intFrameIndex++)
            {
            final double dblValueForFrame;
            final int intFrameBaseAddrInBytes;

            // Range check
            if (dblPhaseRadians < 0.0)
                {
                dblPhaseRadians += TWO_PI;
                }

            switch (getWaveformType())
                {
                case SINE:
                    {
                    dblValueForFrame = (Math.sin(dblPhaseRadians)
                                        + Math.sin(dblPhaseRadians * 2.0) / 2.0
                                        + Math.sin(dblPhaseRadians * 3.0) / 3.0
                                        + Math.sin(dblPhaseRadians * 4.0) / 4.0) * SCALING;
                    break;
                    }

//                case SQUARE:
//                    {
//                    if (dblPhaseRadians < Math.PI)
//                        {
//                         dblValueForFrame = 1.0;
//                        }
//                    else
//                        {
//                         dblValueForFrame = -1.0;
//                        }
//                    break;
//                    }
//
//                case TRIANGLE:
//                    {
//                    if (dblPhaseRadians < PI_BY_TWO)
//                        {
//                         dblValueForFrame = dblPhaseRadians / PI_BY_TWO;
//                        }
//                    else if (dblPhaseRadians < THREE_PI_BY_TWO)
//                        {
//                         dblValueForFrame = (Math.PI - dblPhaseRadians) / PI_BY_TWO;
//                        }
//                    else
//                        {
//                         dblValueForFrame = (dblPhaseRadians - TWO_PI) / PI_BY_TWO;
//                        }
//                    break;
//                    }
//
//                case SAWTOOTH:
//                    {
//                    if (dblPhaseRadians < Math.PI)
//                        {
//                         dblValueForFrame = dblPhaseRadians / Math.PI;
//                        }
//                    else
//                        {
//                         dblValueForFrame = (dblPhaseRadians - TWO_PI) / Math.PI;
//                        }
//                    break;
//                    }
//
//                case RANDOM_NOISE:
//                    {
//                    // Ignore the phase!
//                    if (randomGenerator.nextBoolean())
//                        {
//                        // {0.0 ... 1.0}
//                        dblValueForFrame = randomGenerator.nextDouble();
//                        }
//                    else
//                        {
//                        // {-1.0 ... 0.0}
//                        dblValueForFrame = -randomGenerator.nextDouble();
//                        }
//                    break;
//                    }
//
//                case GAUSSIAN_NOISE:
//                    {
//                    // Ignore the phase!
//                    // WARNING! The values generated will be out of range... sometimes...
//                    dblValueForFrame = randomGenerator.nextGaussian();
//                    break;
//                    }

                default:
                    {
                    dblValueForFrame = 0.0;
                    }
                }

            // Prepare for the next sample
            dblPhaseRadians += dblSamplingIncrementRadians;

            // Range check
            if (dblPhaseRadians >= TWO_PI)
                {
                dblPhaseRadians -= TWO_PI;
                }

            // Transform the sample value {-1...0...1} to scaled bytes to write to the buffer
            intValueForFrame = (int) AstroMath.truncate(dblValueForFrame * dblScaledAmplitude);

            if (intValueForFrame > dblSampleWordSize)
                {
                // This should only occur if the algorithm is incorrect, or Gaussian distribution
                //LOGGER.error("HarmonicOscillator.read() ERROR! Value out of range!");
                intValueForFrame = (int)dblSampleWordSize;
                }

            // Point to the start of the Frame in the buffer
            intFrameBaseAddrInBytes = intDestinationOffset + (intFrameIndex * getFormat().getFrameSize());

            // Write the samples to the buffer, for each channel
            // ToDo Lots of missing combinations for full support!
            switch (getFormat().getChannels())
                {
                // Normal Stereo
                case STEREO:
                    {
                    if (getFormat().isBigEndian())
                        {
                        switch (getFormat().getSampleSizeInBits())
                            {
                            case BITS_PER_SAMPLE:
                                {
                                arraybytes[intFrameBaseAddrInBytes    ] = (byte) ((intValueForFrame >>> 8) & BYTE_MASK);
                                arraybytes[intFrameBaseAddrInBytes + 1] = (byte) (intValueForFrame & BYTE_MASK);

                                arraybytes[intFrameBaseAddrInBytes + 2] = (byte) ((intValueForFrame >>> 8) & BYTE_MASK);
                                arraybytes[intFrameBaseAddrInBytes + 3] = (byte) (intValueForFrame & BYTE_MASK);
                                break;
                                }

                            // Only DEFAULT_BITS_PER_SAMPLE supported in this version
                            default:
                                {
                                LOGGER.error("HarmonicOscillator.read() ERROR! Big Endian Sample Size in Bits not supported!");
                                }
                            }
                        }
                    else
                        {
                        // Little Endian

                        switch (getFormat().getSampleSizeInBits())
                            {
                            case BITS_PER_SAMPLE:
                                {
                                // This is for 16 bit stereo, little endian
                                // >>> unsigned right-shift left bits are zero-filled
                                // Channel 0
                                arraybytes[intFrameBaseAddrInBytes    ] = (byte) (intValueForFrame & BYTE_MASK);
                                arraybytes[intFrameBaseAddrInBytes + 1] = (byte) ((intValueForFrame >>> 8) & BYTE_MASK);

                                // Channel 1
                                arraybytes[intFrameBaseAddrInBytes + 2] = (byte) (intValueForFrame & BYTE_MASK);
                                arraybytes[intFrameBaseAddrInBytes + 3] = (byte) ((intValueForFrame >>> 8) & BYTE_MASK);
                                break;
                                }

                            // Only DEFAULT_BITS_PER_SAMPLE supported in this version
                            default:
                                {
                                LOGGER.error("HarmonicOscillator.read() ERROR! Little Endian Sample Size in Bits not supported!");
                                }
                            }
                        }
                    break;
                    }

                // Only stereo supported in this version
                default:
                    {
                    LOGGER.error("HarmonicOscillator.read() ERROR! Channel Count not supported!");
                    }
                }
            }

        // Warning! RemainingFrames *may* be AudioSystem.NOT_SPECIFIED
        // This causes read() to run forever...
        // which is what we need for a free-running oscillator
        // Otherwise limit the stream to a finite duration
        if (longRemainingFrames != AudioSystem.NOT_SPECIFIED)
            {
            longRemainingFrames -= intFramesToBeDelivered;
            }

        // Return the total number of bytes read into the buffer, or -1 if at end of stream
        if (longRemainingFrames == 0)
            {
            // We have reached the end of the stream
            intBytesRead = -1;
            }
        else
            {
            intBytesRead = intFramesToBeDelivered * getFormat().getFrameSize();
            }

        return (intBytesRead);
        }


    /***********************************************************************************************
     * This method should throw an IOException if the frame size is not 1.
     * Since we currently always use 16 bit samples, the frame size is
     * always greater than 1, so we always throw an exception.
     */

    public int read() throws IOException
        {
        throw new IOException("HarmonicOscillator.read() Method not implemented");
        }
    }
