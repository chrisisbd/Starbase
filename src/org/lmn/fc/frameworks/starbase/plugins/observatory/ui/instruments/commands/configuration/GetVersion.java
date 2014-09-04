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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

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
 * GetVersion.
 */

public final class GetVersion implements FrameworkConstants,
                                         FrameworkStrings,
                                         FrameworkMetadata,
                                         FrameworkSingletons,
                                         ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetVersion().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetVersion(final ObservatoryInstrumentDAOInterface dao,
                                                        final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "AbstractObservatoryInstrumentDAO.getVersion() ";
        final CommandType cmdVersion;
        final ResponseMessageInterface responseMessage;
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Don't affect the CommandType of the incoming Command
        cmdVersion = (CommandType)commandmessage.getCommandType().copy();

        buffer.append(VERSION_RESPONSE);
        buffer.append(REGISTRY.getFramework().getVersionNumber());
        buffer.append(TERMINATOR_SPACE);
        buffer.append(BUILD_RESPONSE);
        buffer.append(REGISTRY.getFramework().getBuildNumber());
        buffer.append(TERMINATOR_SPACE);
        buffer.append(STATUS_RESPONSE);
        buffer.append(REGISTRY.getFramework().getBuildStatus());
        buffer.append(TERMINATOR);

        LOGGER.logTimedEvent(buffer.toString());

        // Put the version in the Response
        cmdVersion.getResponse().setValue(buffer.toString());

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            cmdVersion);
        return (responseMessage);
        }
    }
