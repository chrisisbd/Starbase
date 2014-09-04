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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.actions.ActionStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.instruments.CommandCategory;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.io.*;
import java.util.Calendar;
import java.util.List;


/***************************************************************************************************
 * DAOCommandHelper.
 */

public final class DAOCommandHelper implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               FrameworkXpath
    {
    // String Resources
    private static final String ERROR_XML_CONFIGURATION = "The XML configuration is incorrect for this Command";

    private static final String MSG_ALL = "all";


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the Applicable ChannelID as a String.
     * A ChannelID of -1 indicates 'all'.
     *
     * @param channelid
     *
     * @return String
     */

    public static String getApplicableChannelID(final int channelid)
        {
        final String strApplicableChannel;

        if (channelid == -1)
            {
            strApplicableChannel = MSG_ALL;
            }
        else
            {
            strApplicableChannel = Integer.toString(channelid);
            }

        return (strApplicableChannel);
        }


    /***********************************************************************************************
     * Log a failed Block ResponseMessage.
     * All parameters are assumed to be not-NULL.
     *
     * @param dao
     * @param command
     * @param response
     * @param blockid
     * @param retryid
     */

    public static void logResponseBlock(final ObservatoryInstrumentDAOInterface dao,
                                        final CommandMessageInterface command,
                                        final ResponseMessageInterface response,
                                        final int blockid,
                                        final int retryid)
        {
        // Don't log failures caused by ABORT
        if (!dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
            {
            try
                {
                final StringBuffer bufferFilename;
                final StringBuffer bufferLogData;
                final File file;
                final OutputStream outputStream;

                // Write the log to dist/logs
                // InstrumentID_Block_n_20110101_123456.txt
                bufferFilename = new StringBuffer();
                bufferFilename.append(InstallationFolder.LOGS.getName());
                bufferFilename.append(System.getProperty("file.separator"));
                bufferFilename.append(dao.getHostInstrument().getInstrument().getIdentifier());
                bufferFilename.append("_Block_");
                bufferFilename.append(blockid);

                file = new File(FileUtilities.buildFullFilename(bufferFilename.toString(),
                                                                true,
                                                                DataFormat.TXT));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                bufferLogData = new StringBuffer();

                // Starscript
                if ((command != null)
                    && (command.getStarScript() != null))
                    {
                    bufferLogData.append("\nStarscript: ");
                    bufferLogData.append(command.getStarScript());
                    }

                bufferLogData.append("\nBlock: ");
                bufferLogData.append(blockid);
                bufferLogData.append("\nRetry: ");
                bufferLogData.append(retryid);

                // The original ResponseMessage
                if (response != null)
                    {
                    bufferLogData.append("\n\nResponse Message - Hex  \n");
                    bufferLogData.append(Utilities.byteArrayToSpacedHex(response.getByteArray()));
                    bufferLogData.append("\nResponse Message - ASCII\n");
                    bufferLogData.append(Utilities.byteArrayToExpandedAscii(response.getByteArray()));
                    }

                // Add the ResponseValue (if any)
                if ((response != null)
                    && (response.getCommandType() != null)
                    && (response.getCommandType().getResponse() != null)
                    && (response.getCommandType().getResponse().getValue() != null))
                    {
                    bufferLogData.append("\n\nResponse Value - Raw    \n");
                    bufferLogData.append(response.getCommandType().getResponse().getValue());
                    bufferLogData.append("\nResponse Value - Hex    \n");
                    bufferLogData.append(Utilities.byteArrayToSpacedHex(response.getCommandType().getResponse().getValue().getBytes()));
                    bufferLogData.append("\nResponse Value - ASCII  \n");
                    bufferLogData.append(Utilities.byteArrayToExpandedAscii(response.getCommandType().getResponse().getValue().getBytes()));
                    }

                // The ResponseValue name, in its full StarScript form
                if (response != null)
                    {
                    bufferLogData.append("\nResponse Name:          ");
                    bufferLogData.append(response.getStarScript());
                    }

                // Add the Response Units
                if ((response != null)
                    && (response.getCommandType() != null)
                    && (response.getCommandType().getResponse() != null)
                    && (response.getCommandType().getResponse().getUnits() != null))
                    {
                    bufferLogData.append("\nResponse Units:         ");
                    bufferLogData.append(response.getCommandType().getResponse().getUnits().toString());
                    }

                // Add the Response DataType
                if ((response != null)
                    && (response.getCommandType() != null)
                    && (response.getCommandType().getResponse() != null)
                    && (response.getCommandType().getResponse().getDataTypeName() != null))
                    {
                    bufferLogData.append("\nResponse DataType:      ");
                    bufferLogData.append(response.getCommandType().getResponse().getDataTypeName().toString());
                    }

                if (response != null)
                    {
                    final int intStatus;

                    intStatus = response.getStatusBits();
                    bufferLogData.append("\nStatus Word:            ");
                    bufferLogData.append(Utilities.intToBitString(intStatus));
                    bufferLogData.append("\nStatus Codes:           ");
                    bufferLogData.append(ResponseMessageStatus.expandResponseStatusCodes(response.getResponseMessageStatusList()));
                    }

                // Dates & Times of the CommandLifecycleEvent
                if (command != null)
                    {
                    final Calendar calendarTx;

                    calendarTx = command.getTxCalendar();
                    bufferLogData.append("\nTransmitted:            ");
                    bufferLogData.append(ChronosHelper.toDateString(calendarTx));
                    bufferLogData.append(SPACE);
                    bufferLogData.append(ChronosHelper.toTimeString(calendarTx));
                    }

                if (response != null)
                    {
                    final Calendar calendarRx;

                    calendarRx = response.getRxCalendar();
                    bufferLogData.append("\nReceived:               ");
                    bufferLogData.append(ChronosHelper.toDateString(calendarRx));
                    bufferLogData.append(SPACE);
                    bufferLogData.append(ChronosHelper.toTimeString(calendarRx));
                    }

                bufferLogData.append("\n\nWritten by Starbase ");
                bufferLogData.append(dao.getObservatoryClock().getDateTimeNowAsString());

                // Write the Log in the specified DataFormat
                outputStream.write(bufferLogData.toString().getBytes());

                // Tidy up
                outputStream.flush();
                outputStream.close();

                LOGGER.logTimedEvent("Failed data block logged [filename=" + file.getAbsolutePath()
                                     + "] [blockid=" + blockid + "] [retryid=" + retryid + "]");
                }

            catch (SecurityException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_LOG
                                                       + METADATA_ACTION_LOGGING
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_LOG_ACCESS_DENIED + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            catch (FileNotFoundException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_LOG
                                                       + METADATA_ACTION_LOGGING
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_LOG_NOT_FOUND + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_LOG
                                                       + METADATA_ACTION_LOGGING
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_LOG_SAVE + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }
            }
        }


    /***********************************************************************************************
     * Log Response Bytes.
     * All parameters are assumed to be not-NULL.
     *
     * @param daos
     * @param bytes
     * @param status
     * @param message
     */


    public static void logResponseBytes(final List<ObservatoryInstrumentDAOInterface> daos,
                                        final byte[] bytes,
                                        final int status,
                                        final String message)
        {
        final StringBuffer bufferStatus;
        final ResponseMessageStatusList listStatus;

        listStatus = AbstractResponseMessage.mapResponseStatusBits(status);

        bufferStatus = new StringBuffer();
        bufferStatus.append("\n\nResponse Word      ");
        bufferStatus.append(Utilities.intToBitString(status));
        bufferStatus.append("\nResponse Status    ");
        bufferStatus.append(ResponseMessageStatus.expandResponseStatusCodes(listStatus));

        logResponseMessageBytes(daos, bytes, bufferStatus, message);
        }


    /***********************************************************************************************
     * Log Response Message Bytes.
     * All parameters are assumed to be not-NULL.
     *
     * @param daos
     * @param bytes
     * @param message
     */


    public static void logResponseMessageBytes(final List<ObservatoryInstrumentDAOInterface> daos,
                                               final byte[] bytes,
                                               final String message)
        {
        final StringBuffer bufferStatus;

        bufferStatus = new StringBuffer();
        bufferStatus.append("\n\nResponse Status not available to analyse");

        logResponseMessageBytes(daos, bytes, bufferStatus, message);
        }


    /***********************************************************************************************
     * Log Response Message Bytes.
     * statusbuffer may be NULL.
     *
     * @param daos
     * @param bytes
     * @param statusbuffer
     * @param message
     */

    private static void logResponseMessageBytes(final List<ObservatoryInstrumentDAOInterface> daos,
                                                final byte[] bytes,
                                                final StringBuffer statusbuffer,
                                                final String message)
        {
        try
            {
            final StringBuffer bufferFilename;
            final StringBuffer bufferLogData;
            final File file;
            final OutputStream outputStream;

            // Write the log to dist/logs
            // InstrumentID_Bytes_20110101_123456.txt
            bufferFilename = new StringBuffer();
            bufferFilename.append(InstallationFolder.LOGS.getName());
            bufferFilename.append(System.getProperty("file.separator"));
            bufferFilename.append(daos.get(0).getHostInstrument().getInstrument().getIdentifier());
            bufferFilename.append("_Response");

            file = new File(FileUtilities.buildFullFilename(bufferFilename.toString(),
                                                            true,
                                                            DataFormat.TXT));
            FileUtilities.overwriteFile(file);
            outputStream = new FileOutputStream(file);

            bufferLogData = new StringBuffer();

            if (message != null)
                {
                bufferLogData.append(message);
                }

            if ((statusbuffer != null)
                && (statusbuffer.length() > 0))
                {
                bufferLogData.append(statusbuffer);
                }

            if (bytes != null)
                {
                bufferLogData.append("\n\nResponse Bytes - Hex  \n");
                bufferLogData.append(Utilities.byteArrayToSpacedHex(bytes));
                bufferLogData.append("\nResponse Bytes - ASCII\n");
                bufferLogData.append(Utilities.byteArrayToExpandedAscii(bytes));
                }

            bufferLogData.append("\n\nWritten by Starbase ");

            if ((daos != null)
                && (daos.get(0) != null))
                {
                bufferLogData.append(daos.get(0).getObservatoryClock().getDateTimeNowAsString());
                }

            // Write the Log in the specified DataFormat
            outputStream.write(bufferLogData.toString().getBytes());

            // Tidy up
            outputStream.flush();
            outputStream.close();
            }

        catch (SecurityException exception)
            {
            SimpleEventLogUIComponent.logEvent(daos.get(0).getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_LOG
                                                   + METADATA_ACTION_LOGGING
                                                   + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_LOG_ACCESS_DENIED + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               daos.get(0).getLocalHostname(),
                                               daos.get(0).getObservatoryClock());
            }

        catch (FileNotFoundException exception)
            {
            SimpleEventLogUIComponent.logEvent(daos.get(0).getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_LOG
                                                   + METADATA_ACTION_LOGGING
                                                   + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_LOG_NOT_FOUND + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               daos.get(0).getLocalHostname(),
                                               daos.get(0).getObservatoryClock());
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(daos.get(0).getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_LOG
                                                   + METADATA_ACTION_LOGGING
                                                   + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_LOG_SAVE + TERMINATOR + SPACE
                                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                               daos.get(0).getLocalHostname(),
                                               daos.get(0).getObservatoryClock());
            }
        }


    /***********************************************************************************************
     * Log Invalid XML.
     * The ActionList is updated to show a pending Action to correct the XML.
     *
     * @param dao
     * @param SOURCE
     * @param target
     * @param action
     *
     * @return ResponseMessageStatus
     */

    public static ResponseMessageStatus logInvalidXML(final ObservatoryInstrumentDAOInterface dao,
                                                      final String SOURCE,
                                                      final String target,
                                                      final String action)
        {
        final ResponseMessageStatus responseMessageStatus;

        responseMessageStatus = ResponseMessageStatus.INVALID_XML;

        // Incorrectly configured XML
        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                           responseMessageStatus.getEventStatus(),
                                           target + action
                                               + METADATA_EXCEPTION
                                               + ERROR_XML_CONFIGURATION
                                               + TERMINATOR,
                                           dao.getLocalHostname(),
                                           dao.getObservatoryClock());

        REGISTRY.addActionToList(REGISTRY_MODEL.getLoggedInUser(),
                                 Chronos.getSystemDateNow(),
                                 Chronos.getSystemTimeNow(),
                                 SOURCE + ERROR_XML_CONFIGURATION,
                                 ActionStatus.FATAL);

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Get the CommandCategory, or return CommandCategory.UNDEFINED if missing from the XML.
     *
     * @param command
     *
     * @return CommandCategory.Enum
     */

    public static CommandCategory.Enum getCommandCategory(final CommandType command)
        {
        final CommandCategory.Enum category;

        if ((command != null)
            && (command.getCategory() != null))
            {
            category = command.getCategory();
            }
        else
            {
            category = CommandCategory.UNDEFINED;
            }

        return (category);
        }


    /***********************************************************************************************
     * Construct a detailed ResponseValue string, given a ResponseMessageStatus code.
     * The format is:
     *  [result=xxxx] [detail=yyyy]
     *
     * @param status
     * @param detail
     *
     * @return String
     */

    public static String constructResponseValue(final ResponseMessageStatus status,
                                                final String detail)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        buffer.append(METADATA_RESULT);
        buffer.append(status.getResponseValue());
        buffer.append(TERMINATOR_SPACE);
        buffer.append(METADATA_DETAIL);
        buffer.append(detail);
        buffer.append(TERMINATOR);

        return (buffer.toString());
        }
    }
