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
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusParsers;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * GetDataBlock.
 */

public final class GetDataBlock implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkSingletons,
                                           ObservatoryConstants
    {
    // The simulated HostMemory File contains a CSV record
    // 2012-08-20,20:58:02,+037,0870,1023,1023,1023

    // Simulated HostMemory storage uses Staribus Format
    // The Staribus format has multiple blocks of 512 bytes,
    // where each block has a timestamped header of the form:
    // 2008-08-23 12:34:56 followed by Temperature +999 and SampleRate 9999,
    // in a total of 32 characters, currently leaving three spare.
    // Then follow (512-32) characters assigned to fixed width fields
    // for data values for each channel in sequence.
    // Data space is therefore 512 - 32 = 480 bytes
    // DataType is assumed to be DecimalInteger, i.e. four characters per number
    // There are five data channels in the Nanode (four data and Temperature)
    // but note that the Temperature is a 'slow' channel,
    // i.e. occurs only once per StaribusBlock, in the header
    // So at four bytes per sample, each subsequent data record therefore takes 4 * 4 = 16 bytes
    // The Block has capacity for 480 / 16 = 30 records exactly.

    //  "2008-08-23 12:00:00 +023 0001   "  ; 32 bytes

    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //  "0000000100020003"      ; 16 bytes
    //
    //                      Total 512 bytes


    /***********************************************************************************************
     * doGetDataBlock().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetDataBlock(final StaribusCoreHostMemoryInterface dao,
                                                          final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetDataBlock.doGetDataBlock() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_BLOCKID = 0;
        final CommandType cmdGetDataBlock;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdGetDataBlock = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, the Block ID
        listParameters = cmdGetDataBlock.getParameterList();

        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_BLOCKID) != null)
            && (SchemaDataType.HEX_INTEGER.equals(listParameters.get(INDEX_BLOCKID).getInputDataType().getDataTypeName()))

            // This is a command specific to the Staribus comms protocol
            && (cmdGetDataBlock.getResponse() != null)
            && (SchemaDataType.STARIBUS_BLOCK.toString().equals(cmdGetDataBlock.getResponse().getDataTypeName().toString()))

            && (dao != null)
            && (dao.getHostInstrument() != null))
            {
            try
                {
                final String strBlockID;
                final int intBlockID;

                strBlockID = listParameters.get(INDEX_BLOCKID).getValue();
                intBlockID = Integer.parseInt(strBlockID, RADIX_HEX);

                if ((intBlockID >= 0)
                    && (intBlockID < StaribusCoreHostMemoryInterface.MAX_STARIBUS_BLOCK_COUNT))
                    {
                    File fileHostMemory;

                    // Find out the name of the HostMemory file for this Instrument
                    fileHostMemory = new File(Capture.buildHostMemoryFilename(dao.getHostInstrument(),
                                                                              StaribusCoreHostMemoryInterface.TIMESTAMPED_HOSTMEMORYFILE));
                    // See if we have a current HostMemory file
                    if (fileHostMemory.exists())
                        {
                        // Check that the requested Block ID exists in the HostMemory file
                        if (Capture.isAccessibleBlockID(fileHostMemory, intBlockID))
                            {
                            RandomAccessFile fileRandomAccess;
                            final int intRecordStartIndex;
                            final long longByteIndex;
                            final StringBuffer bufferResponse;
                            boolean boolSuccess;

                            // Open for read-only RandomAccess
                            fileRandomAccess = new RandomAccessFile(fileHostMemory, "r");

                            // Point to the requested record
                            intRecordStartIndex = intBlockID
                                                  * StaribusCoreHostMemoryInterface.FILE_RECORDS_PER_STARIBUS_BLOCK;

                            // Point to the start of the first block
                            longByteIndex = intRecordStartIndex
                                            * StaribusCoreHostMemoryInterface.RECORD_SIZE_BYTES_CAPTURED;

                            bufferResponse = new StringBuffer();

                            // Seek the file to the start of the data representing the Block
                            // This should not fail, because of the check in isAccessibleBlockID()
                            fileRandomAccess.seek(longByteIndex);

                            // So far so good...
                            boolSuccess = true;

                            // Read the all of the records for the Block
                            for (int intDataRecordIndex = 0;
                                 ((boolSuccess)
                                    && (intDataRecordIndex < StaribusCoreHostMemoryInterface.FILE_RECORDS_PER_STARIBUS_BLOCK));
                                 intDataRecordIndex++)
                                {
                                // The first record contains information for the Header
                                boolSuccess = translateRecordToBlock(dao,
                                                                     fileRandomAccess,
                                                                     (intDataRecordIndex == 0),
                                                                     bufferResponse);
                                }

                            // Check that we have potentially got a buffer of the correct length
                            // even if we translated all Records correctly
                            boolSuccess = boolSuccess && (bufferResponse.length() <= StaribusParsers.LENGTH_STARIBUS_BLOCK);

                            if (boolSuccess)
                                {
                                // Pad the StaribusBlock to the correct length, if required
                                while (bufferResponse.length() < StaribusParsers.LENGTH_STARIBUS_BLOCK)
                                    {
                                    bufferResponse.append(SPACE);
                                    }

                                strResponseValue = bufferResponse.toString();
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

                                if (dao.isCaptureMode())
                                    {
                                    // We don't mind about CAPTURE_ACTIVE, but remind the User anyway
                                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.CAPTURE_ACTIVE);
                                    }
                                }
                            else
                                {
                                // The HostMemory Record could not be translated to a Staribus Block
                                strResponseValue = StaribusCoreHostMemoryInterface.MSG_BLOCK_FORMAT_INVALID;
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }

                            // Close and dispose the RandomAccessFile
                            fileRandomAccess.close();
                            fileRandomAccess = null;
                            }
                        else
                            {
                            // This may happen if the User enters an ID which is too large
                            // or the file hasn't been created yet
                            strResponseValue = StaribusCoreHostMemoryInterface.MSG_BLOCK_ID_OUT_OF_RANGE;
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                            }
                        }
                    else
                        {
                        strResponseValue = StaribusCoreHostMemoryInterface.MSG_HOST_MEMORY_FILE_NOT_FOUND;
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }

                    // Dispose the File
                    fileHostMemory = null;
                    }
                else
                    {
                    // This should not be possible since BlockID is constrained by Regex....
                    strResponseValue = StaribusCoreHostMemoryInterface.MSG_BLOCK_ID_OUT_OF_RANGE;
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }

            catch (NumberFormatException exception)
                {
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IOException exception)
                {
                strResponseValue = StaribusCoreHostMemoryInterface.MSG_HOST_MEMORY_FILE_ERROR;
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            catch (PatternSyntaxException exception)
                {
                strResponseValue = ResponseMessageStatus.INVALID_COMMAND.getResponseValue();
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
                                                                      cmdGetDataBlock,
                                                                      null,
                                                                      null,
                                                                      strResponseValue
        );
        return (responseMessage);
        }


    /***********************************************************************************************
     * Translate the HostMemory Record in the File to a Staribus Block in the Buffer.
     * All parameters are assumed to be not-NULL.
     * Return a flag to indicate if translation succeeded.
     *
     * @param dao
     * @param file
     * @param isheader
     * @param buffer
     *
     * @return boolean
     *
     * @throws IOException
     */

    private static boolean translateRecordToBlock(final StaribusCoreHostMemoryInterface dao,
                                                  final RandomAccessFile file,
                                                  final boolean isheader,
                                                  final StringBuffer buffer) throws IOException
        {
        final String strRecord;
        final String[] arrayTokens;
        boolean boolSuccess;

        strRecord = file.readLine();
        boolSuccess = true;

        if (strRecord != null)
            {
            // The simulated HostMemory File contains a CSV record
            arrayTokens = strRecord.split(COMMA);

            if ((arrayTokens != null)
                && (arrayTokens.length == StaribusCoreHostMemoryInterface.RECORD_TOKEN_COUNT))
                {
                // Process all tokens in this record
                if (isheader)
                    {
                    // The first record contains information for the Header
                    // Assembled Format: "2008-08-23 12:00:00 +023 0001   "

                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_DATE]);
                    buffer.append(SPACE);
                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_TIME]);
                    buffer.append(SPACE);
                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_TEMP]);
                    buffer.append(SPACE);
                    buffer.append(Utilities.intPositiveToFourDecimalString(dao.getSampleRate()));
                    buffer.append(SPACE);
                    buffer.append(SPACE);
                    buffer.append(SPACE);

                    boolSuccess = (buffer.length() == StaribusParsers.LENGTH_STARIBUS_HEADER_FIELD);
                    }

                if (boolSuccess)
                    {
                    // Now process the data channels, but NOT Date, Time or Temperature
                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_CH0]);
                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_CH1]);
                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_CH2]);
                    buffer.append(arrayTokens[StaribusCoreHostMemoryInterface.INDEX_CH3]);
                    }
                }
            else
                {
                // Invalid token count
                boolSuccess = false;
                }
            }
        else
            {
            // Found EOF
            boolSuccess = false;
            }

        return (boolSuccess);
        }
    }
