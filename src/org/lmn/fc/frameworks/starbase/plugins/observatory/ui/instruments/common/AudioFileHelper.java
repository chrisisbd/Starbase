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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Vector;


/***************************************************************************************************
 * AudioFileHelper.
 */

public final class AudioFileHelper implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              FrameworkXpath,
                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * Enumerate sound Mixers currently available.
     *
     * @return Vector<String>
     */

    public static Vector<String> getSystemMixers()
        {
        final Vector<String> vecMixers;
        final Mixer.Info[] arrayMixerInfo;

        vecMixers = new Vector<String>(10);
        arrayMixerInfo = AudioSystem.getMixerInfo();

        LOGGER.logTimedEvent("System Mixers");

        for (int intMixerIndex = 0;
             intMixerIndex < arrayMixerInfo.length;
             intMixerIndex++)
            {
            // NB: Linux puts the Device name in description field, Windows in name field, Mac - dunno.. sheesh.
            vecMixers.add(arrayMixerInfo[intMixerIndex].getName() + ", " + arrayMixerInfo[intMixerIndex].getDescription());

            LOGGER.logTimedEvent(
                    "[name=" + arrayMixerInfo[intMixerIndex].getName() + "] [description=" + arrayMixerInfo[intMixerIndex].getDescription() + "]");
            }

        return (vecMixers);
        }
    }
