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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * SignalGeneratorMode.
 */

public enum SignalGeneratorMode
    {
    FREE_RUN            (0, "FreeRun"),
    CONTINUOUS_SWEEP    (1, "ContinuousSweep"),
    SINGLE_SWEEP        (2, "SingleSweep"),
    STOP                (3, "Stop");


    private final int intMode;
    private final String strModeName;


    /***********************************************************************************************
     * Get the SignalGeneratorMode enum corresponding to the specified SignalGeneratorMode name.
     * Return NULL if not found.
     *
     * @param modename
     *
     * @return SignalGeneratorMode
     */

    public static SignalGeneratorMode getSignalGeneratorModeForName(final String modename)
        {
        SignalGeneratorMode signalGeneratorMode;

        signalGeneratorMode = null;

        if ((modename != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(modename)))
            {
            final SignalGeneratorMode[] signalGeneratorModes;
            boolean boolFoundIt;

            signalGeneratorModes = SignalGeneratorMode.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < signalGeneratorModes.length);
                 i++)
                {
                final SignalGeneratorMode generatorMode;

                generatorMode = signalGeneratorModes[i];

                if (modename.equals(generatorMode.getName()))
                    {
                    signalGeneratorMode = generatorMode;
                    boolFoundIt = true;
                    }
                }
            }

        if (signalGeneratorMode == null)
            {
            throw new IllegalArgumentException("SignalGeneratorMode.getSignalGeneratorModeForName() "
                                                    + FrameworkStrings.EXCEPTION_RESOURCE_NOTFOUND
                                                    + FrameworkMetadata.METADATA_NAME
                                                    + modename
                                                    + FrameworkMetadata.TERMINATOR);
            }

        return (signalGeneratorMode);
        }


    /***********************************************************************************************
     * SignalGeneratorMode.
     *
     * @param mode
     * @param modename
     */

    private SignalGeneratorMode(final int mode,
                                final String modename)
        {
        intMode = mode;
        strModeName = modename;
        }


    /***********************************************************************************************
     * Get the SignalGeneratorMode.
     *
     * @return int
     */

    public int getSignalGeneratorMode()
        {
        return (this.intMode);
        }


    /***********************************************************************************************
     * Get the SignalGeneratorMode name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strModeName);
        }


    /***********************************************************************************************
     * Get the SignalGeneratorMode name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strModeName);
        }
    }
