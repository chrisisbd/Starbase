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
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.OscillatorWaveform;
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
 * SetOscillatorWaveformType.
 */

public final class SetOscillatorWaveformType implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        ObservatoryConstants
    {
    /***********************************************************************************************
     * doSetWaveformType().
     *
     * @param dao
     * @param oscillator
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetWaveformType(final ObservatoryInstrumentDAOInterface dao,
                                                             final OscillatorInterface oscillator,
                                                             final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetOscillatorWaveformType.doSetWaveformType() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_TYPE = 0;
        final List<ParameterType> listParameters;
        final CommandType cmdSetType;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdSetType = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdSetType.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        if ((dao.getHostInstrument() != null)
            && (oscillator != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_TYPE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_TYPE).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strOscillatorWaveform;
                final OscillatorWaveform oscillatorWaveform;

                strOscillatorWaveform = listParameters.get(INDEX_TYPE).getValue();

                // Map the entries to Enums
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                oscillatorWaveform = OscillatorWaveform.getOscillatorWaveformForName(strOscillatorWaveform);

                switch (oscillatorWaveform)
                    {
                    case SINE:
                    case SQUARE:
                    case TRIANGLE:
                    case SAWTOOTH:
                    case RANDOM_NOISE:
                    case GAUSSIAN_NOISE:
                    case HARMONIC_SERIES:
                        {
                        oscillator.setWaveformType(oscillatorWaveform);

                        strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                        break;
                        }

                    default:
                        {
                        // Incorrectly configured XML
                        strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                        dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                              SOURCE,
                                                                                              METADATA_TARGET
                                                                                                  + SOURCE.trim()
                                                                                                  + TERMINATOR,
                                                                                              METADATA_ACTION_COMMAND));
                        }
                    }
                }

            // This should have been trapped by Regex
            catch (final IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                       + METADATA_ACTION_SET
                                                       + METADATA_ITEM + listParameters.get(INDEX_TYPE).getName() + TERMINATOR_SPACE
                                                       + METADATA_RESULT
                                                       + MSG_UNSUPPORTED_OSCILLATOR_CONFIG
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
                                                                      cmdSetType,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
