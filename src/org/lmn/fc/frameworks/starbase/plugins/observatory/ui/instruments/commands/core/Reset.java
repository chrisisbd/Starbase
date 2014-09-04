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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * Reset.
 */

public final class Reset implements FrameworkConstants,
                                    FrameworkStrings,
                                    FrameworkMetadata,
                                    FrameworkSingletons,
                                    ObservatoryConstants
    {
    /***********************************************************************************************
     * reset() resets the whole Instrument.
     * This local Command may be overridden by a Command which sends a reset() to the DAO Port.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doReset(final ObservatoryInstrumentDAOInterface dao,
                                                   final CommandMessageInterface commandmessage
    )
        {
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractObservatoryInstrumentDAO.reset() LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Do the reset(mode) operation for the Instrument, its DAO and anything else...
        if (dao.getHostInstrument() != null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "DAO reset, go to reset instrument");
            dao.getHostInstrument().reset(DAOHelper.extractResetMode(commandType));
            }

        // Create the ResponseMessage
        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
        responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                       commandmessage,
                                                                       commandmessage.getInstrument(),
                                                                       commandmessage.getModule(),
                                                                       commandType,
                                                                       AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                        commandmessage.getModule(),
                                                                                                                        commandType));
        return (responseMessage);
        }
    }
