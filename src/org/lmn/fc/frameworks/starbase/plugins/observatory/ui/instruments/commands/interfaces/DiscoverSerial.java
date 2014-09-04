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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.interfaces;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * DiscoverSerial.
 */

public final class DiscoverSerial implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             ObservatoryConstants
    {
    /***********************************************************************************************
     * doDiscoverSerial().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doDiscoverSerial(final ObservatoryInstrumentDAOInterface dao,
                                                            final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "DiscoverSerial.doDiscoverSerial() ";
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Create the ResponseMessage
//        dao.setRawDataChannelCount(0);
//        dao.setTemperatureChannel(false);

        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }
    }
