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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.audio;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * SetOscillatorWaveformSampleRate.
 */

public final class SetOscillatorWaveformSampleRate implements FrameworkConstants,
                                                              FrameworkStrings,
                                                              FrameworkMetadata,
                                                              FrameworkSingletons,
                                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * doSetWaveformSampleRate().
     *
     * @param dao
     * @param oscillator
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetWaveformSampleRate(final ObservatoryInstrumentDAOInterface dao,
                                                                   final OscillatorInterface oscillator,
                                                                   final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetOscillatorWaveformSampleRate.doSetWaveformSampleRate() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_SAMPLE_RATE = 0;
        final List<ParameterType> listParameters;
        final CommandType cmdSetRate;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdSetRate = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdSetRate.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        if ((dao.getHostInstrument() != null)
            && (oscillator != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_SAMPLE_RATE) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_SAMPLE_RATE).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strSampleRate;
                final int intSampleRate;

                strSampleRate = listParameters.get(INDEX_SAMPLE_RATE).getValue();
                intSampleRate = Integer.parseInt(strSampleRate);

                oscillator.setSignalSampleRate(intSampleRate);

                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }

            // This should have been trapped by Regex
            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                       + METADATA_ACTION_SET
                                                       + METADATA_ITEM + listParameters.get(INDEX_SAMPLE_RATE).getName() + TERMINATOR_SPACE
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
                                                                      cmdSetRate,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
