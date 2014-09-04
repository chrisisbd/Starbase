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

package org.lmn.fc.common.datatranslators.stardata;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;

import java.util.Vector;


/***************************************************************************************************
 * StardataTranslatorReaders.
 */

public final class StardataTranslatorReaders implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        FrameworkRegex,
                                                        ResourceKeys
    {
    /***********************************************************************************************
     * Parse a tStardata file into Timestamped or XY data in RawData.
     * Extract any Metadata header into RawDataMetadata.
     * The DataTranslator is assumed to be initialised.
     * The parsing fails if RawData remains empty.
     *
     * @param translator
     * @param filename
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public static boolean parseStardataToRawData(final DataTranslatorInterface translator,
                                                 final String filename,
                                                 final Vector<Vector> log,
                                                 final ObservatoryClockInterface clock)
        {
        return false;
        }
    }
