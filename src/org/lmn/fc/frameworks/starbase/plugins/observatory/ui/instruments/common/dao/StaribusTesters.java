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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * StaribusTesters.
 *
 * TODO Update for CommandVariant
 */

public final class StaribusTesters
    {
    private static final LoaderProperties LOADER_PROPERTIES = LoaderProperties.getInstance();


    /***********************************************************************************************
     * PING For testing!
     *
     * @param args
     */

    public static void main2(final String[] args)
        {
        final List<Byte> message;
        int checksum;

        message = new ArrayList<Byte>();
        checksum = 0;

        // Header, which is not included in the checksum
        message.add(ObservatoryConstants.STARIBUS_START);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Address LMN test controller address is 2B, i.e. 43 decimal
        checksum = Utilities.addStringToMessage(message, Utilities.byteToTwoHexString((byte)43), checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // CommandCodeBase,CommandCode 00 00 is PING
        checksum = Utilities.addStringToMessage(message, "0000", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Calculate the CRC checksum and add it as four uppercase ASCII characters
        Utilities.addCrcToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message));

        // Terminators
        Utilities.addByteToMessage(message,
                                   ObservatoryConstants.STARIBUS_TERMINATOR_0, checksum);
        Utilities.addByteToMessage(message,
                                   ObservatoryConstants.STARIBUS_TERMINATOR_1, checksum);
        Utilities.addByteToMessage(message,
                                   ObservatoryConstants.STARIBUS_TERMINATOR_2, checksum);

        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "PING message=" + Utilities.byteListToString(message));

        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "<cr>TEST STRING<lf> byteArrayToExpandedAscii " + Utilities.byteArrayToExpandedAscii("\rTEST STRING\n".getBytes()));

        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "<cr>TEST STRING<lf> byteArrayToSpacedHex " + Utilities.byteArrayToSpacedHex("\rTEST STRING\n".getBytes()));
        }


    /***********************************************************************************************
     * For testing!
     *
     * @param args
     */

    public static void main(final String[] args)
        {
        final List<Byte> message;
        int checksum;

        message = new ArrayList<Byte>();
        checksum = 0;

        // Header, which is not included in the checksum
        message.add(ObservatoryConstants.STARIBUS_START);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Address
        checksum = Utilities.addStringToMessage(message, Utilities.byteToTwoHexString((byte)42), checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // CommandCode
        checksum = Utilities.addStringToMessage(message, "0F2A", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Parameter lead-in
        checksum = Utilities.addDelimiterToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Parameter 0
        checksum = Utilities.addStringToMessage(message, "12.9", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);
        checksum = Utilities.addDelimiterToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Parameter 1
        checksum = Utilities.addStringToMessage(message, "10110010", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);
        checksum = Utilities.addDelimiterToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Parameter 1
        checksum = Utilities.addStringToMessage(message, "20070611", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);
        checksum = Utilities.addDelimiterToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Parameter 2
        checksum = Utilities.addStringToMessage(message, "231245", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);
        checksum = Utilities.addDelimiterToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);

        // Parameter 3
        checksum = Utilities.addStringToMessage(message, "Y", checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message) + " checksum=" + checksum);
        checksum = Utilities.addDelimiterToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message)
            + " final checksum =" + Integer.toHexString(checksum) + " hex "
            + checksum + " dec");

        // Calculate the CRC checksum and add it as four uppercase ASCII characters
        Utilities.addCrcToMessage(message, checksum);
        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   Utilities.byteListToString(message));

        // Terminators
        Utilities.addByteToMessage(message,
                                   ObservatoryConstants.STARIBUS_TERMINATOR_0, checksum);
        Utilities.addByteToMessage(message,
                                   ObservatoryConstants.STARIBUS_TERMINATOR_1, checksum);
        Utilities.addByteToMessage(message,
                                   ObservatoryConstants.STARIBUS_TERMINATOR_2, checksum);

        FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "message=" + Utilities.byteListToString(message));
        }
    }
