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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.audio.OscillatorWaveform;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import javax.sound.sampled.Mixer;
import java.util.List;


/***************************************************************************************************
 * MeasureFrequencyResponse.
 *
 * Q factor: http://en.wikipedia.org/wiki/Q_factor
 */

public final class MeasureFrequencyResponse implements FrameworkConstants,
                                                       FrameworkStrings,
                                                       FrameworkMetadata,
                                                       FrameworkRegex,
                                                       FrameworkSingletons,
                                                       ObservatoryConstants
    {
    /***********************************************************************************************
     * doMeasureFrequencyResponse().
     *
     * There are eight Command parameters: the mixer, the starting value, the end value, the step size,
     * the wait time, the analogue input channel, realtime updates and verbose logging control.
     * The starting value is used to drive the first command in the SteppedCommand list.
     * The wait time is sent to the next command in the list which takes a Parameter, and so on.
     * There are therefore THREE Parameters which must be sent to the SteppedCommands:
     * the starting value, the wait time, the analogue input channel.
     *
     * @param dao
     * @param commandmessage
     * @param oscillator
     * @param metadatalist
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doMeasureFrequencyResponse(final ObservatoryInstrumentDAOInterface dao,
                                                                      final CommandMessageInterface commandmessage,
                                                                      final OscillatorInterface oscillator,
                                                                      final List<Metadata> metadatalist)
        {
        final String SOURCE = "MeasureFrequencyResponse.doMeasureFrequencyResponse() ";
        final int PARAMETER_COUNT_MIN = 8;
        final int INDEX_MIXER = 0;
        final List<ParameterType> listParameters;
        final CommandType cmdMeasure;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Don't affect the CommandType of the incoming Command
        cmdMeasure = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdMeasure.getParameterList();

        // We need a working Oscillator to measure the frequency response
        // We need a Metadata List to carry the Chart and ControlPanel data
        if ((dao.getHostInstrument() != null)
            && (oscillator != null)
            && (metadatalist != null)
            && (listParameters != null)
            && (listParameters.size() >= PARAMETER_COUNT_MIN)
            // Output.Mixer
            && (listParameters.get(INDEX_MIXER) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_MIXER).getInputDataType().getDataTypeName())))
            {
            final List<String> listKeys;
            final Metadata metadataX;
            final Metadata metadataY;
            final String strOutputMixer;
            final Mixer.Info info;

            // See if we have two ControlPanel indicators, one for the X (Test Frequency), one for the Y (Receiver Output)
            // We can safely assume we have an Instrument!
            listKeys = commandmessage.getDAO().getHostInstrument().getInstrument().getIndicatorMetadataKeyList();

            if ((listKeys != null)
                && (listKeys.size() >= 2))
                {
                final int INDEX_INDICATOR_X = 0;
                final int INDEX_INDICATOR_Y = 1;
                final List<Metadata> metadataComposite;
                final Metadata metadata0;
                final Metadata metadata1;

                // Gather together the full set of Metadata associated with this Observatory,
                // this Instrument and its DAO and translator to search for required items
                metadataComposite = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                  dao.getHostInstrument().getContext().getObservatory(),
                                                                                  dao.getHostInstrument(),
                                                                                  dao,
                                                                                  dao.getWrappedData(),
                                                                                  SOURCE,
                                                                                  boolDebug);

                metadata0 = MetadataHelper.getMetadataByKey(metadataComposite, listKeys.get(INDEX_INDICATOR_X));

                if (metadata0 != null)
                    {
                    // The Metadata item is already in the composite collection,
                    // so we don't need to add anything
                    metadataX = metadata0;
                    }
                else
                    {
                    // The Metadata item did not exist, so create it
                    metadataX = MetadataHelper.createMetadata(listKeys.get(INDEX_INDICATOR_X),
                                                              "0",
                                                              REGEX_NONE,
                                                              DataTypeDictionary.DECIMAL_INTEGER,
                                                              SchemaUnits.HZ,
                                                              "The Response Analyser test Frequency");
                    }

                metadata1 = MetadataHelper.getMetadataByKey(metadataComposite, listKeys.get(INDEX_INDICATOR_Y));

                if (metadata1 != null)
                    {
                    // The Metadata item is already in the composite collection,
                    // so we don't need to add anything
                    metadataY = metadata1;
                    }
                else
                    {
                    // The Metadata item did not exist, so create it
                    metadataY = MetadataHelper.createMetadata(listKeys.get(INDEX_INDICATOR_Y),
                                                              "0",
                                                              REGEX_NONE,
                                                              DataTypeDictionary.DECIMAL_INTEGER,
                                                              SchemaUnits.M_V,
                                                              "The output from the VLF Receiver under test");
                    }

                // Place the Metadata items in the List of sundries,
                // which will all be added to the Composite collection by establishDAOIdentityForCapture()
                metadatalist.add(metadataX);
                metadatalist.add(metadataY);

                MetadataHelper.showMetadata(metadataX,
                                            SOURCE + "Test Frequency Indicator",
                                            boolDebug);
                MetadataHelper.showMetadata(metadataY,
                                            SOURCE + "Receiver Output Indicator",
                                            boolDebug);
                }
            else
                {
                // Don't affect the sundry Metadata
                metadataX = null;
                metadataY = null;
                }

            MetadataHelper.showMetadataList(metadatalist,
                                            SOURCE + "--> Sundry Metadata",
                                            boolDebug);
            // OutputMixer
            strOutputMixer = listParameters.get(INDEX_MIXER).getValue();
            info = AudioHelper.findMixerInfoFromName(strOutputMixer);

            if (info != null)
                {
                // The Command Parameters will override the default Oscillator settings
                //
                //  Output.Mixer
                //  Frequency.Start     -->  setOscillatorFrequency()  **  Indicator 0
                //  Frequency.End
                //  Sweep.StepSize
                //  Sweep.DwellTime     -->  wait()                    **
                //  A2D.Channel         -->  getA2D()                  **  Indicator 1
                //  RealtimeUpdate
                //  VerboseLogging
                //
                // ** Parameters which must be sent to the SteppedCommands
                // Only generate a ResponseMessage when completed

                // Configure and start the Oscillator
                oscillator.setMixerInfo(info);
                oscillator.setWaveformType(OscillatorWaveform.SINE);

                if (oscillator.initialise(dao))
                    {
                    responseMessage = CaptureCommandHelper.doSteppedDataCaptureCommand(dao,
                                                                                       commandmessage,
                                                                                       metadatalist,
                                                                                       metadataX,
                                                                                       metadataY,
                                                                                       1,     // Ignore the Output.Mixer parameter
                                                                                       7,     // Parameters for the macro steps
                                                                                       3,     // Parameters which must be sent to the SteppedCommands
                                                                                       SOURCE,
                                                                                       false);
                    // If we get this far, it worked...
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

                    REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                    InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                    // Try to tidy up, if we are allowed to run the gc
                    ObservatoryInstrumentHelper.runGarbageCollector();
                    }
                else
                    {
                    strResponseValue = DAOCommandHelper.constructResponseValue(ResponseMessageStatus.PREMATURE_TERMINATION,
                                                                               OscillatorInterface.MSG_FAILED_TO_START );
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                                  commandmessage,
                                                                                  cmdMeasure,
                                                                                  null,
                                                                                  null,
                                                                                  strResponseValue);
                    }

                oscillator.setRunning(false);
                oscillator.dispose();
                }
            else
                {
                strResponseValue = DAOCommandHelper.constructResponseValue(ResponseMessageStatus.INVALID_PARAMETER,
                                                                           "Unknown Mixer " + strOutputMixer);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                              commandmessage,
                                                                              cmdMeasure,
                                                                              null,
                                                                              null,
                                                                              strResponseValue);
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
            responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                          commandmessage,
                                                                          cmdMeasure,
                                                                          null,
                                                                          null,
                                                                          strResponseValue);
            }

        return (responseMessage);
        }
    }
