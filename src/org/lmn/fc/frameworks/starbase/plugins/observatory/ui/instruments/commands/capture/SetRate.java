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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * SetRate.
 */

public final class SetRate implements FrameworkConstants,
                                      FrameworkStrings,
                                      FrameworkMetadata,
                                      FrameworkSingletons,
                                      ObservatoryConstants
    {
    /***********************************************************************************************
     * doSetRate().
     * This does not change the Rate if in CAPTURE_ACTIVE state.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetRate(final StaribusCoreHostMemoryInterface dao,
                                                     final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetRate.doSetRate() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_RATE = 0;
        final CommandType cmdSetRate;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdSetRate = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, the sample rate
        listParameters = cmdSetRate.getParameterList();

        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_RATE) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_RATE).getInputDataType().getDataTypeName()))
            && (dao.getHostInstrument() != null))
            {
            // We can only set the Rate if not capturing
            if (!dao.isCaptureMode())
                {
                try
                    {
                    final String strSampleRate;
                    final int intSampleRate;

                    strSampleRate = listParameters.get(INDEX_RATE).getValue();
                    intSampleRate = Integer.parseInt(strSampleRate);

                    // Set the SampleRate back on the DAO
                    dao.setSampleRate(intSampleRate);

                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                            + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                            + METADATA_ACTION_SET
                                                            + METADATA_ITEM + listParameters.get(INDEX_RATE).getName() + TERMINATOR_SPACE
                                                            + METADATA_VALUE + Integer.toString(intSampleRate) + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    LogHelper.updateEventLogFragment(dao);

                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }

                catch (NumberFormatException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                       METADATA_EXCEPTION
                                                           + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                           + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }
            else
                {
                // We are in Capture mode, so do nothing
                // We don't mind about CAPTURE_ACTIVE, but remind the User anyway
                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.CAPTURE_ACTIVE);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
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
                                                                      cmdSetRate,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
