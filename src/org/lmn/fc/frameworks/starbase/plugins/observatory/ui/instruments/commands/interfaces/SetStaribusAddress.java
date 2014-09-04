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
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * SetStaribusAddress.
 */

public final class SetStaribusAddress implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 ObservatoryConstants
    {
    /***********************************************************************************************
     * doSetStaribusAddress().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetStaribusAddress(final ObservatoryInstrumentDAOInterface dao,
                                                                final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetStaribusAddress.doSetStaribusAddress() ";
        final int PARAMETER_COUNT = 1;
        final CommandType cmdSetAddress;
        final List<ParameterType> listParameters;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdSetAddress = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdSetAddress.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        // Expect one Parameter, the StaribusAddress

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getController() != null)
            && (dao.getHostInstrument().getInstrument().getController().getStaribusAddress() != null)
            && (!EMPTY_STRING.equals(dao.getHostInstrument().getInstrument().getController().getStaribusAddress()))
            && (dao.getHostInstrument().getInstrument().getControllable())
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(0).getInputDataType().getDataTypeName())))
            {
            final String strIdentifier;

            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();

            try
                {
                String strStaribusAddress;
                final int intStaribusAddress;

                // Reformat the address with the correct number of leading zeroes
                strStaribusAddress = listParameters.get(0).getValue();
                intStaribusAddress = Integer.parseInt(strStaribusAddress);
                strStaribusAddress = Utilities.intToString(intStaribusAddress, RADIX_DECIMAL, 3);
                listParameters.get(0).setValue(strStaribusAddress);

                // TODO Really set the address....


                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET
                                                        + strIdentifier + TERMINATOR
                                                    + METADATA_ACTION_SET_ADDRESS
                                                    + METADATA_ADDRESS
                                                        + strStaribusAddress
                                                        + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET
                                                        + strIdentifier + TERMINATOR
                                                    + METADATA_ACTION_SET_ADDRESS
                                                    + METADATA_RESULT
                                                           + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // Incorrectly configured XML
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                    SOURCE,
                                                                                    METADATA_TARGET_UNKNOWN,
                                                                                    METADATA_ACTION_SET_ADDRESS));
            }

        // Create the ResponseMessage
        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
            cmdSetAddress.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdSetAddress,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdSetAddress));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdSetAddress,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdSetAddress));
             }

        return (responseMessage);
        }
    }
