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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;


/***************************************************************************************************
 * ImportAudioFile.
 */

public final class ImportAudioFile implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              FrameworkXpath,
                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportAudioFile().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportAudioFile(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportAudioFile.doImportAudioFile() ";
        final int INDEX_FILENAME = 0;
        final int PARAMETER_COUNT = 1;
        final List<ParameterType> listParameters;
        final CommandType cmdImportAudio;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdImportAudio = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdImportAudio.getParameterList();

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;

                strFilename = listParameters.get(INDEX_FILENAME).getValue();

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "[filename=" + strFilename + "]");

                if ((strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename)))
                    {
                    final File fileAudio;

                    fileAudio = new File(strFilename);

                    // Read the Audio File if possible
                    if (fileAudio.exists())
                        {
                        final int BUFFER_SIZE_MAX = 524288;
                        byte[] arrayData;

                        try
                            {
                            final AudioInputStream audioInputStream;
                            final AudioFormat audioFormat;
                            int intBytesRead;
                            final StringBuffer bufferFormat;
                            final String [] strMessage =
                                {
                                MSG_AWAITING_DEVELOPMENT
                                };

                            audioInputStream = AudioSystem.getAudioInputStream(fileAudio);
                            audioFormat = audioInputStream.getFormat();
                            bufferFormat = new StringBuffer();

                            // Show the AudioFormat details
                            if (audioFormat != null)
                                {
                                final int intChannelCount;
                                final AudioFormat.Encoding encoding;
                                final float floatFrameRate;
                                final int intFrameSize;
                                final float floatSampleRate;
                                final int intSampleSizeInBits;
                                final boolean boolBigEndian;

                                intChannelCount = audioFormat.getChannels();
                                encoding = audioFormat.getEncoding();
                                floatFrameRate = audioFormat.getFrameRate();
                                intFrameSize = audioFormat.getFrameSize();
                                floatSampleRate = audioFormat.getSampleRate();
                                intSampleSizeInBits = audioFormat.getSampleSizeInBits();
                                boolBigEndian = audioFormat.isBigEndian();

                                bufferFormat.append("[channel_count=");
                                bufferFormat.append(intChannelCount);
                                bufferFormat.append("] [encoding=");
                                bufferFormat.append(encoding.toString());
                                bufferFormat.append("] [frame_rate=");
                                bufferFormat.append(floatFrameRate);
                                bufferFormat.append("] [frame_size=");
                                bufferFormat.append(intFrameSize);
                                bufferFormat.append("] [sample_rate=");
                                bufferFormat.append(floatSampleRate);
                                bufferFormat.append("] [sample_bits=");
                                bufferFormat.append(intSampleSizeInBits);
                                bufferFormat.append("] [big_endian=");
                                bufferFormat.append(boolBigEndian);
                                bufferFormat.append("]");
                                }

                            intBytesRead = 0;
                            arrayData = new byte[BUFFER_SIZE_MAX];

                            JOptionPane.showMessageDialog(null,
                                                          strMessage,
                                                          AWAITING_DEVELOPMENT,
                                                          JOptionPane.WARNING_MESSAGE);

                            while ((intBytesRead != -1)
                                   && (Utilities.executeWorkerCanProceed(dao)))
                                {
                                intBytesRead = audioInputStream.read(arrayData, 0, arrayData.length);

                                if (intBytesRead >= 0)
                                    {
                                    // Copy the data to the DAO internal form, in the correct format

                                    LOGGER.log("copy data to RawData....");

                                    }
                                }

                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.SUCCESS.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_IMPORT
                                                                   + METADATA_FILENAME + strFilename + TERMINATOR_SPACE
                                                                   + bufferFormat.toString(),
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }

                        catch (UnsupportedAudioFileException exception)
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_IMPORT
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
                                                                   + METADATA_ACTION_IMPORT
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
                                                               ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                               METADATA_TARGET_AUDIO_FILE
                                                                   + METADATA_ACTION_IMPORT
                                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            LogHelper.updateEventLogFragment(dao);

                            strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                            }

                        finally
                            {
                            // Help the gc?
                            arrayData = null;
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
                                                           METADATA_TARGET_AUDIO_FILE
                                                               + METADATA_ACTION_IMPORT
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
                                                           + METADATA_ACTION_IMPORT
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
                                                                                     METADATA_ACTION_IMPORT));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdImportAudio,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
