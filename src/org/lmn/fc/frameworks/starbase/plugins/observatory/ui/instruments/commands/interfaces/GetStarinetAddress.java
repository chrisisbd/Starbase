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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.interfaces;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * GetStarinetAddress.
 */

public final class GetStarinetAddress implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetStarinetAddress().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetStarinetAddress(final ObservatoryInstrumentDAOInterface dao,
                                                                final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetStarinetAddress.doGetStarinetAddress() ";
        final int PARAMETER_COUNT = 0;
        final CommandType cmdGetAddress;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdGetAddress = (CommandType)commandmessage.getCommandType().copy();

        // Do not affect any data containers, channel count, or temperature indicator

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getController() != null)
            && (dao.getHostInstrument().getInstrument().getController().getIPAddress() != null)
            && (!EMPTY_STRING.equals(IPVersion.stripTrailingPaddingFromIPAddressAndPort(dao.getHostInstrument().getInstrument().getController().getIPAddress())))
            && (dao.getHostInstrument().getInstrument().getControllable()))
            {
            final String strIdentifier;

            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();
            strResponseValue = IPVersion.stripTrailingPaddingFromIPAddressAndPort(dao.getHostInstrument().getInstrument().getController().getIPAddress());

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                    + strIdentifier + TERMINATOR
                                                    + METADATA_ACTION_GET_ADDRESS
                                                    + METADATA_ADDRESS
                                                        + strResponseValue
                                                        + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());

            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }
        else
            {
            // The XML configuration was inappropriate
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                    SOURCE,
                                                                                    METADATA_TARGET
                                                                                    + SOURCE.trim()
                                                                                    + TERMINATOR,
                                                                                    METADATA_ACTION_COMMAND));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdGetAddress,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
