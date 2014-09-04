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
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.LogHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.List;


/***************************************************************************************************
 * PlayAudioFile.
 *
 * See: http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 */

public final class PlayAudioFile implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            FrameworkXpath,
                                            ObservatoryConstants
    {
    /***********************************************************************************************
     * doPlayAudioFile().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doPlayAudioFile(final ObservatoryInstrumentDAOInterface dao,
                                                           final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "PlayAudioFile.doPlayAudioFile() ";
        final int INDEX_OUTPUT_DEVICE = 0;
        final int INDEX_FILENAME = 1;
        final int PARAMETER_COUNT = 2;
        final List<ParameterType> listParameters;
        final CommandType cmdPlayAudio;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdPlayAudio = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdPlayAudio.getParameterList();

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_OUTPUT_DEVICE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_OUTPUT_DEVICE).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strOutputDevice;
                final String strFilename;

                strOutputDevice = listParameters.get(INDEX_OUTPUT_DEVICE).getValue();
                strFilename = listParameters.get(INDEX_FILENAME).getValue();

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "[output=" + strOutputDevice + "] [filename=" + strFilename + "]");

                if ((strOutputDevice != null)
                    && (!EMPTY_STRING.equals(strOutputDevice))
                    && (strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename)))
                    {
                    final File fileAudio;

                    fileAudio = new File(strFilename);

                    // Play the Audio File if possible
                    if (fileAudio.exists())
                        {
                        final int EXTERNAL_BUFFER_SIZE = 524288;
                        SourceDataLine sourceDataLine;
                        byte[] arrayData;

                        sourceDataLine = null;

                        try
                            {
                            final AudioInputStream audioInputStream;
                            final AudioFormat audioFormat;
                            final DataLine.Info infoDataLine;
                            int intBytesRead;

                            audioInputStream = AudioSystem.getAudioInputStream(fileAudio);
                            audioFormat = audioInputStream.getFormat();
                            infoDataLine = new DataLine.Info(SourceDataLine.class, audioFormat);

                            sourceDataLine = (SourceDataLine) AudioSystem.getLine(infoDataLine);
                            sourceDataLine.open(audioFormat);
                            sourceDataLine.start();

                            intBytesRead = 0;
                            arrayData = new byte[EXTERNAL_BUFFER_SIZE];

                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.SUCCESS.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_PLAY
                                                                   + METADATA_OUTPUT + strOutputDevice + TERMINATOR_SPACE
                                                                   + METADATA_FILENAME + strFilename + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            // Play the file!
                            while ((intBytesRead != -1)
                                   && (Utilities.executeWorkerCanProceed(dao)))
                                {
                                intBytesRead = audioInputStream.read(arrayData, 0, arrayData.length);

                                if (intBytesRead >= 0)
                                    {
                                    sourceDataLine.write(arrayData, 0, intBytesRead);
                                    }
                                }

                            strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }

                        catch (UnsupportedAudioFileException exception)
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_PLAY
                                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                            }

                        catch (LineUnavailableException exception)
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_PLAY
                                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                            }

                        catch (IOException exception)
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_PLAY
                                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                            }

                        catch (Exception exception)
                            {
                            exception.printStackTrace();
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.INVALID_MESSAGE.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_PLAY
                                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                            }

                        finally
                            {
                            if (sourceDataLine != null)
                                {
                                if (Utilities.executeWorkerCanProceed(dao))
                                    {
                                    sourceDataLine.drain();
                                    }
                                else
                                    {
                                    sourceDataLine.stop();
                                    sourceDataLine.flush();
                                    }

                                sourceDataLine.close();
                                }

                            // Help the gc?
                            arrayData = null;
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                           METADATA_TARGET_AUDIO_FILE
                                                               + METADATA_ACTION_PLAY
                                                               + METADATA_RESULT + "Audio filename does not exist" + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        LogHelper.updateEventLogFragment(dao);

                        strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                       METADATA_TARGET_AUDIO_FILE
                                                           + METADATA_ACTION_PLAY
                                                           + METADATA_RESULT + "Audio filename is not valid" + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    LogHelper.updateEventLogFragment(dao);

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_MESSAGE.getEventStatus(),
                                                   dao.getInstrumentName() + " [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                LogHelper.updateEventLogFragment(dao);

                strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_AUDIO_FILE,
                                                                                     METADATA_ACTION_PLAY));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdPlayAudio,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
