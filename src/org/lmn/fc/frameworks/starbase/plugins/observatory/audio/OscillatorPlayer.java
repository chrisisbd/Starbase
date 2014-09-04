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
 *	OscillatorPlayer.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 -2001 by Matthias Pfisterer
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

import javax.sound.sampled.*;

public class OscillatorPlayer
    {
	private static final int BUFFER_SIZE = 128000;



	public static void play()
        {
		final byte[] abData;
		final AudioFormat audioFormat;
		final OscillatorWaveform waveformType;
		final float fSampleRate;
		final float fSignalFrequency;
		final float fAmplitude;
        final AudioInputStream oscillator;
        final DataLine.Info	info;
        final SourceDataLine line;

        waveformType = OscillatorWaveform.SINE;
        fSampleRate = 44100.0f;
        fSignalFrequency = 1000.0f;
        fAmplitude = 0.7f;

		audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					                  fSampleRate,
                                      16,
                                      2,
                                      4,
                                      fSampleRate,
                                      false);

        oscillator = new Oscillator(audioFormat,
                                    waveformType,
                                    fSignalFrequency,
                                    fAmplitude,
                                    AudioSystem.NOT_SPECIFIED);

        info = new DataLine.Info(SourceDataLine.class,
                                 audioFormat);
        try
		    {
            long longCount;

            longCount = 100000000;
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);

            line.start();

            abData = new byte[BUFFER_SIZE];

            while (longCount > 0)
                {
                final int nRead;
                final int nWritten;

                //if (DEBUG) { out("OscillatorPlayer.main(): trying to read (bytes): " + abData.length); }
                nRead = oscillator.read(abData);
                //if (DEBUG) { out("OscillatorPlayer.main(): in loop, read (bytes): " + nRead); }
                nWritten = line.write(abData, 0, nRead);
                //if (DEBUG) { out("OscillatorPlayer.main(): written: " + nWritten); }
                longCount--;
                }

            line.drain();
            line.stop();
            line.close();
		    }

		catch (LineUnavailableException e)
		    {
			e.printStackTrace();
		    }

		catch (Exception e)
		    {
			e.printStackTrace();
		    }
	    }
    }
