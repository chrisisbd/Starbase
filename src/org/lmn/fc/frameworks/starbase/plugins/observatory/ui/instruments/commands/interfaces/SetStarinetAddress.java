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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.net.UnknownHostException;
import java.util.List;


/***************************************************************************************************
 * SetStarinetAddress.
 */

public final class SetStarinetAddress implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 ObservatoryConstants
    {
    /***********************************************************************************************
     * doSetStarinetAddress().
     * ToDo Extend to support IPv6Address.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetStarinetAddress(final ObservatoryInstrumentDAOInterface dao,
                                                                final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetStarinetAddress.doSetStarinetAddress() ";
        final int PARAMETER_COUNT = 4;
        final int INDEX_ADDRESS = 0;
        final int INDEX_VERSION = 1;
        final int INDEX_PORT = 2;
        final int INDEX_SAVE = 3;
        final CommandType cmdSetAddress;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdSetAddress = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdSetAddress.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getController() != null)
            && (dao.getHostInstrument().getInstrument().getController().getIPAddress() != null)
            && (!EMPTY_STRING.equals(IPVersion.stripTrailingPaddingFromIPAddressAndPort(dao.getHostInstrument().getInstrument().getController().getIPAddress())))
            && (dao.getHostInstrument().getInstrument().getControllable())
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_ADDRESS) != null)
            && (SchemaDataType.I_PV_4_ADDRESS.equals(listParameters.get(INDEX_ADDRESS).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_VERSION) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_VERSION).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_PORT) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_PORT).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_SAVE) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_SAVE).getInputDataType().getDataTypeName())))
            {
            final String strIdentifier;

            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();

            try
                {
                final String strStarinetAddress;
                final long longStarinetAddress;
                final String strVersion;
                final IPVersion ipVersion;
                final String strUDPPort;
                final int intUDPPort;
                final String strSaveFlag;
                final boolean boolSaveFlag;

                strStarinetAddress = listParameters.get(INDEX_ADDRESS).getValue();
                longStarinetAddress = NetworkScannerHelper.ipV4ToLong(strStarinetAddress);

                strVersion = listParameters.get(INDEX_VERSION).getValue();
                ipVersion = IPVersion.getIPVersionForName(strVersion);

                strUDPPort = listParameters.get(INDEX_PORT).getValue();
                intUDPPort = Integer.parseInt(strUDPPort);

                // This should never throw NumberFormatException, because it has already been parsed
                strSaveFlag = listParameters.get(INDEX_SAVE).getValue();
                boolSaveFlag = Boolean.parseBoolean(strSaveFlag);

                // TODO Really set the address....
                // We must have a Controller...
                dao.getHostInstrument().getInstrument().getController().setIPAddress(IPVersion.addTrailingPaddingToIPAddressAndPort(strStarinetAddress,
                                                                                                                                    strUDPPort));


                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET
                                                        + strIdentifier + TERMINATOR
                                                    + METADATA_ACTION_SET_ADDRESS
                                                    + METADATA_ADDRESS
                                                        + NetworkScannerHelper.longToIPv4(longStarinetAddress)
                                                        + TERMINATOR_SPACE
                                                    + METADATA_VERSION
                                                        + ipVersion.getName()
                                                        + TERMINATOR_SPACE
                                                    + METADATA_PORT
                                                        + Integer.toString(intUDPPort)
                                                        + TERMINATOR_SPACE
                                                    + METADATA_SAVE
                                                        + boolSaveFlag
                                                        + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }

            catch (UnknownHostException exception)
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
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (SecurityException exception)
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
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
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
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
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
                                                                      cmdSetAddress,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
