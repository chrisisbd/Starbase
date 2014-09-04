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

import javax.sound.sampled.Mixer;
import java.util.List;


/***************************************************************************************************
 * ConfigureOscillator.
 */

public final class ConfigureOscillator implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ObservatoryConstants
    {
    /***********************************************************************************************
     * doConfigureOscillator().
     *
     * @param dao
     * @param oscillator
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doConfigureOscillator(final ObservatoryInstrumentDAOInterface dao,
                                                                 final OscillatorInterface oscillator,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ConfigureOscillator.doConfigureOscillator() ";
        final String ACTION = " [action=configure] ";
        final int PARAMETER_COUNT = 8;
        final int INDEX_MIXER = 0;
        final int INDEX_WAVEFORM_TYPE = 1;
        final int INDEX_SAMPLE_RATE = 2;
        final int INDEX_ENCODING = 3;
        final int INDEX_BITSPERSAMPLE = 4;
        final int INDEX_CHANNELS = 5;
        final int INDEX_FREQUENCY_START = 6;
        //final int INDEX_FREQUENCY_END = 7;
        final int INDEX_AMPLITUDE = 7;
        //final int INDEX_STEP_SIZE = 9;
        //final int INDEX_DWELL_TIME = 10;
        final List<ParameterType> listParameters;
        final CommandType cmdConfigure;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdConfigure = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdConfigure.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        if ((dao.getHostInstrument() != null)
            && (oscillator != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            // Output.Mixer
            && (listParameters.get(INDEX_MIXER) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_MIXER).getInputDataType().getDataTypeName()))
            // Waveform.Type
            && (listParameters.get(INDEX_WAVEFORM_TYPE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_WAVEFORM_TYPE).getInputDataType().getDataTypeName()))
            // Waveform.SampleRate
            && (listParameters.get(INDEX_SAMPLE_RATE) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_SAMPLE_RATE).getInputDataType().getDataTypeName()))
            // Waveform.Encoding
            && (listParameters.get(INDEX_ENCODING) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_ENCODING).getInputDataType().getDataTypeName()))
            // Waveform.BitsPerSample
            && (listParameters.get(INDEX_BITSPERSAMPLE) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_BITSPERSAMPLE).getInputDataType().getDataTypeName()))
            // Waveform.Channels
            && (listParameters.get(INDEX_CHANNELS) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_CHANNELS).getInputDataType().getDataTypeName()))
            // Oscillator.Frequency.Start
            && (listParameters.get(INDEX_FREQUENCY_START) != null)
            && (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(INDEX_FREQUENCY_START).getInputDataType().getDataTypeName()))
            // Oscillator.Frequency.End
            //&& (listParameters.get(INDEX_FREQUENCY_END) != null)
            //&& (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(INDEX_FREQUENCY_END).getInputDataType().getDataTypeName()))
            // Oscillator.Amplitude
            && (listParameters.get(INDEX_AMPLITUDE) != null)
            && (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(INDEX_AMPLITUDE).getInputDataType().getDataTypeName())))
            // Sweep.StepSize
            //&& (listParameters.get(INDEX_STEP_SIZE) != null)
            //&& (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_STEP_SIZE).getInputDataType().getDataTypeName()))
            // Sweep.DwellTime
            //&& (listParameters.get(INDEX_DWELL_TIME) != null)
            //&& (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_DWELL_TIME).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strOutputMixer;
                final Mixer.Info info;
                final String strOscillatorWaveform;
                final OscillatorWaveform oscillatorWaveform;
                final String strSampleRate;
                final int intSampleRate;

                final String strFrequencyStart;
                final double dblFrequencyStart;
                final String strFrequencyEnd;
                final double dblFrequencyEnd;
                final String strAmplitudePercent;
                final double dblParamAmplitudePercent;
                final String strSweepStepSize;
                final int intStepSize;
                final String strSweepDwellTime;
                final int intDwellTime;

                // OutputMixer
                strOutputMixer = listParameters.get(INDEX_MIXER).getValue();
                info = AudioHelper.findMixerInfoFromName(strOutputMixer);

                if (info != null)
                    {
                    oscillator.setMixerInfo(info);
                    }
                else
                    {
                    throw new IllegalArgumentException(SOURCE + "Unable to find the Mixer Line which was selected by the User [output.mixer="
                                                        + strOutputMixer + "]");
                    }

                // Waveform
                strOscillatorWaveform = listParameters.get(INDEX_WAVEFORM_TYPE).getValue();

                // Map the entries to Enums
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                oscillatorWaveform = OscillatorWaveform.getOscillatorWaveformForName(strOscillatorWaveform);

                strSampleRate = listParameters.get(INDEX_SAMPLE_RATE).getValue();
                intSampleRate = Integer.parseInt(strSampleRate);

                // ToDo add other parameters when they become configurable!
                // INDEX_ENCODING          LinearPCM
                // INDEX_BITSPERSAMPLE     16
                // INDEX_CHANNELS          2

                strFrequencyStart = listParameters.get(INDEX_FREQUENCY_START).getValue();
                dblFrequencyStart = Double.parseDouble(strFrequencyStart);

                //strFrequencyEnd = listParameters.get(INDEX_FREQUENCY_END).getValue();
                //dblFrequencyEnd = Double.parseDouble(strFrequencyEnd);
                dblFrequencyEnd = dblFrequencyStart;

                strAmplitudePercent = listParameters.get(INDEX_AMPLITUDE).getValue();
                dblParamAmplitudePercent = Double.parseDouble(strAmplitudePercent);

                //strSweepStepSize = listParameters.get(INDEX_STEP_SIZE).getValue();
                //intStepSize = Integer.parseInt(strSweepStepSize);
                intStepSize = 1;

                //strSweepDwellTime = listParameters.get(INDEX_DWELL_TIME).getValue();
                //intDwellTime = Integer.parseInt(strSweepDwellTime);
                intDwellTime = 1;

                // Oscillator Waveform
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
                        break;
                        }

                    default:
                        {
                        // This should never happen!
                        oscillator.setWaveformType(OscillatorWaveform.SINE);
                        }
                    }

                // Sample Rate
                oscillator.setSignalSampleRate(intSampleRate);

                // Oscillator Start Frequency
                oscillator.setSignalFrequencyStart(dblFrequencyStart);

                // Oscillator End Frequency
                oscillator.setSignalFrequencyEnd(dblFrequencyEnd);

                // Oscillator Amplitude as a fraction {0...1}
                oscillator.setSignalAmplitude(dblParamAmplitudePercent / 100.0);

                // Step Size
                oscillator.setSweepStepSize(intStepSize);

                // Dwell Time
                oscillator.setSweepDwellTime(intDwellTime);

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.SUCCESS.getEventStatus(),
                                                   METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                       + ACTION
                                                       + "[output.mixer=" + oscillator.getMixerInfo().getName() + "] "
                                                       + "[waveform.type=" + oscillator.getWaveformType() + "] "
                                                       + "[waveform.samplerate=" + oscillator.getSignalSampleRate() + "] "
                                                       + "[oscillator.frequency=" + oscillator.getSignalFrequencyStart() + "] "
                                                       //+ "[frequency_end=" + oscillator.getSignalFrequencyEnd() + "] "
                                                       + "[oscillator.amplitude=" + oscillator.getSignalAmplitude() + "]",
                                                       //+ "[step_size=" + oscillator.getSweepStepSize() + "] "
                                                       //+ "[dwell_time=" + oscillator.getSweepDwellTime() + "]",
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                // If we get this far, it worked...
                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                }

            // This should have been trapped by Regex
            catch (final NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                       + ACTION
                                                       + METADATA_RESULT
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                       + exception.getMessage()
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (final IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   METADATA_TARGET + OscillatorInterface.TARGET_OSCILLATOR + TERMINATOR
                                                       + ACTION
                                                       + METADATA_RESULT
                                                       + MSG_UNSUPPORTED_OSCILLATOR_CONFIG
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
                                                                      cmdConfigure,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
