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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.net.NetworkInterfaceHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.net.SocketException;


/***************************************************************************************************
 * GetNetworkInterfaces.
 */

public final class GetNetworkInterfaces implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetNetworkInterfaces().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetNetworkInterfaces(final ObservatoryInstrumentDAOInterface dao,
                                                                  final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetNetworkInterfaces.doGetNetworkInterfaces() ";
        final CommandType cmdNetworkInterfaces;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdNetworkInterfaces = (CommandType)commandmessage.getCommandType().copy();

        try
            {
            final StringBuffer buffer;

            buffer = NetworkInterfaceHelper.getInterfaceDetails();

            // Put the NetworkInterfaces in the Response
            strResponseValue = buffer.toString();

            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }

        catch (SocketException exception)
            {
            strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
            }

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdNetworkInterfaces,
                                                                      null,
                                                                      null,
                                                                      strResponseValue
        );
        return (responseMessage);
        }
    }
