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

/*
 *	Oscillator.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.utilities.maths.AstroMath;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/***************************************************************************************************
 * Oscillator.
 *
 * See: http://dickbaldwin.com/tocadv.htm
 * See: http://www.developer.com/java/other/article.php/1579071
 */

public class Oscillator extends AudioInputStream
                        implements FrameworkSingletons
    {
    private byte[] arrayWavetableData;
    private int intWavetableIndex;
    private long longRemainingFrames;


    /***********************************************************************************************
     * Oscillator.
     *
     * @param audioformat
     * @param waveformtype
     * @param frequency
     * @param amplitude
     * @param framecount
     */

    public Oscillator(final AudioFormat audioformat,
                      final OscillatorWaveform waveformtype,
                      final float frequency,
                      final float amplitude,
                      final long framecount)
        {
        super(new ByteArrayInputStream(new byte[0]),
              audioformat,
              framecount);

        this.arrayWavetableData = null;
        this.intWavetableIndex = 0;
        this.longRemainingFrames = AudioSystem.NOT_SPECIFIED;

        buildWavetable(waveformtype, frequency, amplitude, framecount);
        }


    /***********************************************************************************************
     * Build the wavetable containing this Oscillator's data.
     *
     * @param waveformtype
     * @param frequency
     * @param amplitude
     * @param framecount
     */

    private void buildWavetable(final OscillatorWaveform waveformtype,
                                final float frequency,
                                final float amplitude,
                                final long framecount)
        {
        final float floatAmplitude;
        final int intPeriodLengthInFrames;
        final int intBufferLengthInBytes;
        final double dblSampleWordSize;

        dblSampleWordSize = Math.pow(2, getFormat().getSampleSizeInBits() - 1);

        // Oscillator Amplitude is supplied as a fraction {0...1}
        // e.g. for 16 bit samples, map this to 0...2^15
        floatAmplitude = (float) (amplitude * dblSampleWordSize);

        // Warning! framecount *may* be AudioSystem.NOT_SPECIFIED
        // This causes read() to run forever...
        // which is what we need for a free-running oscillator
        longRemainingFrames = framecount;

        // Initialise the pointer into the wavetable
        intWavetableIndex = 0;

        // Length of one period in frames
        intPeriodLengthInFrames = Math.round(getFormat().getFrameRate() / frequency);

        // Length of the wavetable buffer in bytes. WARNING, frame size may be NOT_SPECIFIED
        // if this AudioFormat does not define a frame size.
        intBufferLengthInBytes = intPeriodLengthInFrames * getFormat().getFrameSize();
        // TODO error trap NOT_SPECIFIED
        arrayWavetableData = new byte[intBufferLengthInBytes];

        LOGGER.logTimedEvent("Oscillator: Waveform=" + waveformtype.getWaveformType());
        LOGGER.logTimedEvent("Oscillator: Frequency=" + frequency);
        LOGGER.logTimedEvent("Oscillator: Scaled Amplitude=" + floatAmplitude);
        LOGGER.logTimedEvent("Oscillator: FrameCount=" + framecount);
        LOGGER.logTimedEvent("Oscillator: Frame Rate=" + getFormat().getFrameRate());
        LOGGER.logTimedEvent("Oscillator: Frame Rate/Frequency=" + (getFormat().getFrameRate() / frequency));
        LOGGER.logTimedEvent("Oscillator: Length of one period in frames=" + intPeriodLengthInFrames);
        LOGGER.logTimedEvent("Oscillator: Remaining frames=" + longRemainingFrames);

        // Fill the wavetable with the waveform, for each frame required
        for (int intFrameIndex = 0;
             intFrameIndex < intPeriodLengthInFrames;
             intFrameIndex++)
            {
            final float floatPeriodOffset;
            final float floatValue;
            int intValue;
            final int intFrameBaseAddrInBytes;

            // The relative position inside the period of the waveform. 0.0 = beginning, 1.0 = end
            floatPeriodOffset = (float) intFrameIndex / (float) intPeriodLengthInFrames;

            // Have we hit Nyquist yet?
            // If not, the OscillatorWaveform still matters...
            if (intPeriodLengthInFrames > 3)
                {
                switch (waveformtype)
                    {
                    case SINE:
                        {
                        floatValue = (float) Math.sin(floatPeriodOffset * 2.0 * Math.PI);
                        break;
                        }

                    case SQUARE:
                        {
                        if (floatPeriodOffset < 0.5f)
                            {
                            floatValue = 1.0f;
                            }
                        else
                            {
                            floatValue = -1.0f;
                            }
                        break;
                        }

                    case TRIANGLE:
                        {
                        if (floatPeriodOffset < 0.25f)
                            {
                            floatValue = 4.0f * floatPeriodOffset;
                            }
                        else if (floatPeriodOffset < 0.75f)
                            {
                            floatValue = -4.0f * (floatPeriodOffset - 0.5f);
                            }
                        else
                            {
                            floatValue = 4.0f * (floatPeriodOffset - 1.0f);
                            }
                        break;
                        }

                    case SAWTOOTH:
                        {
                        if (floatPeriodOffset < 0.5f)
                            {
                            floatValue = 2.0f * floatPeriodOffset;
                            }
                        else
                            {
                            floatValue = 2.0f * (floatPeriodOffset - 1.0f);
                            }
                        break;
                        }

                    default:
                        {
                        floatValue = 0;
                        }
                    }
                }
            else
                {
                // With only two or three samples per period, there's not much we can do!
                if (floatPeriodOffset < 0.5f)
                    {
                    floatValue = 1.0f;
                    }
                else
                    {
                    floatValue = -1.0f;
                    }
                }

            // Transform the sample value to bytes to write to the buffer
            intValue = (int) AstroMath.truncate(floatValue * floatAmplitude);
            if (intValue > dblSampleWordSize)
                {
                LOGGER.error("Oscillator ERROR! Value out of range!");
                intValue = (int)dblSampleWordSize;
                }

            intFrameBaseAddrInBytes = intFrameIndex * getFormat().getFrameSize();

            // ToDo Lots of missing combinations for full support!
            switch (getFormat().getChannels())
                {
                // Normal Stereo
                case 2:
                    {
                    if (getFormat().isBigEndian())
                        {
                        switch (getFormat().getSampleSizeInBits())
                            {
                            case 16:
                                {
                                arrayWavetableData[intFrameBaseAddrInBytes + 0] = (byte) ((intValue >>> 8) & 0xff);
                                arrayWavetableData[intFrameBaseAddrInBytes + 1] = (byte) (intValue & 0xff);

                                arrayWavetableData[intFrameBaseAddrInBytes + 2] = (byte) ((intValue >>> 8) & 0xff);
                                arrayWavetableData[intFrameBaseAddrInBytes + 3] = (byte) (intValue & 0xff);
                                break;
                                }

                            // Only DEFAULT_BITS_PER_SAMPLE supported in this version
                            default:
                                {
                                LOGGER.error("Oscillator ERROR! Big Endian Sample Size in Bits not supported!");
                                }
                            }
                        }
                    else
                        {
                        // Little Endian

                        switch (getFormat().getSampleSizeInBits())
                            {
                            case 16:
                                {
                                // This is for 16 bit stereo, little endian
                                // >>> unsigned right-shift left bits are zero-filled
                                // Channel 0
                                arrayWavetableData[intFrameBaseAddrInBytes + 0] = (byte) (intValue & 0xff);
                                arrayWavetableData[intFrameBaseAddrInBytes + 1] = (byte) ((intValue >>> 8) & 0xff);

                                // Channel 1
                                arrayWavetableData[intFrameBaseAddrInBytes + 2] = (byte) (intValue & 0xff);
                                arrayWavetableData[intFrameBaseAddrInBytes + 3] = (byte) ((intValue >>> 8) & 0xff);
                                break;
                                }

                            // Only DEFAULT_BITS_PER_SAMPLE supported in this version
                            default:
                                {
                                LOGGER.error("Oscillator ERROR! Little Endian Sample Size in Bits not supported!");
                                }
                            }
                        }
                    break;
                    }

                // Only stereo supported in this version
                default:
                    {
                    LOGGER.error("Oscillator ERROR! Channel Count not supported!");
                    }
                }
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
        if (longRemainingFrames == AudioSystem.NOT_SPECIFIED)
            {
            intBytesRemaining = Integer.MAX_VALUE;
            }
        else
            {
            final long longBytesRemaining;

            longBytesRemaining = longRemainingFrames * getFormat().getFrameSize();
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
     * @throws IOException
     */

    public int read(final byte[] arraybytes,
                    final int offset,
                    final int length) throws IOException
        {
        final int intBytesToBeDelivered;
        int intLengthLeftToRead;
        final int intFramesRead;
        int intBytesRead;
        int intDestinationOffset;

        intDestinationOffset = offset;

        if (length % getFormat().getFrameSize() != 0)
            {
            throw new IOException("Oscillator.read() Length must be an integer multiple of frame size");
            }

        intBytesToBeDelivered = Math.min(available(), length);

        // Initialise the read...
        intLengthLeftToRead = intBytesToBeDelivered;

        while (intLengthLeftToRead > 0)
            {
            int intNumBytesToCopy;

            intNumBytesToCopy = arrayWavetableData.length - intWavetableIndex;
            intNumBytesToCopy = Math.min(intNumBytesToCopy, intLengthLeftToRead);

            System.arraycopy(arrayWavetableData, intWavetableIndex,
                             arraybytes, intDestinationOffset,
                             intNumBytesToCopy);

            intLengthLeftToRead -= intNumBytesToCopy;
            intDestinationOffset += intNumBytesToCopy;
            intWavetableIndex = (intWavetableIndex + intNumBytesToCopy) % arrayWavetableData.length;
            }

        intFramesRead = intBytesToBeDelivered / getFormat().getFrameSize();

        // Warning! RemainingFrames *may* be AudioSystem.NOT_SPECIFIED
        // This causes read() to run forever...
        // which is what we need for a free-running oscillator
        // Otherwise limit the stream to a finite duration
        if (longRemainingFrames != AudioSystem.NOT_SPECIFIED)
            {
            longRemainingFrames -= intFramesRead;
            }

        // Return the total number of bytes read into the buffer, or -1 at end of stream
        intBytesRead = intBytesToBeDelivered;

        if (longRemainingFrames == 0)
            {
            intBytesRead = -1;
            }

        return (intBytesRead);
        }


    /***********************************************************************************************
       this method should throw an IOException if the frame size is not 1.
       Since we currently always use 16 bit samples, the frame size is
       always greater than 1. So we always throw an exception.
     */

    public int read() throws IOException
        {
        throw new IOException("Oscillator.read() Method not implemented");
        }
    }
