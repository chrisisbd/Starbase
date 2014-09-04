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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.io.File;


/***************************************************************************************************
 * AwaitingDevelopment.
 */

public final class GetDataBlockCount implements FrameworkConstants,
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
     * doGetDataBlockCount().
     * File contains records of length: StaribusCoreHostMemoryInterface.RECORD_SIZE_BYTES_CAPTURED.
     * Simulated storage contains blocks of length: StaribusParsers.LENGTH_STARIBUS_BLOCK.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetDataBlockCount(final StaribusCoreHostMemoryInterface dao,
                                                               final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetDataBlockCount.doGetDataBlockCount() ";
        final CommandType cmdGetBlockCount;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;
        File fileHostMemory;

        // Don't affect the CommandType of the incoming Command
        cmdGetBlockCount = (CommandType)commandmessage.getCommandType().copy();

        // The HostMemory File contains records of length: StaribusCoreHostMemoryInterface.RECORD_SIZE_BYTES_CAPTURED,
        // where each record contains dao.getRawDataChannelCount() channels (including Temperature)
        // However... we can't check for that, because to do so would mean this command has to
        // establish an identity for Capture, which it can't do, since the capture is elsewhere
        // We don't mind about CAPTURE_ACTIVE, but there must be a file
        // Find out the name of the HostMemory file for this Instrument
        fileHostMemory = new File(Capture.buildHostMemoryFilename(dao.getHostInstrument(),
                                                                  StaribusCoreHostMemoryInterface.TIMESTAMPED_HOSTMEMORYFILE));
        // See if we have a current HostMemory file
        if (fileHostMemory.exists())
            {
            final long longCompleteRecords;
            final int intCompleteStaribusBlocks;

            // Find the number of *complete* records in the file
            longCompleteRecords = fileHostMemory.length() / StaribusCoreHostMemoryInterface.RECORD_SIZE_BYTES_CAPTURED;

            // Find the number of *complete* StaribusBlocks in the file
            if (longCompleteRecords == 0)
                {
                // An empty file (or length less than one record) has zero blocks
                intCompleteStaribusBlocks = 0;
                }
            else
                {
                // StaribusBlockCount must also be 0 for FileRecordCount {1...FILE_RECORDS_PER_STARIBUS_BLOCK-1}
                // since it is an incomplete StaribusBlock
                intCompleteStaribusBlocks = (int)(longCompleteRecords / StaribusCoreHostMemoryInterface.FILE_RECORDS_PER_STARIBUS_BLOCK);
                }

            // Double check the block count we've calculated (last BlockID = BlockCount-1)
            if (Capture.isAccessibleBlockID(fileHostMemory, (intCompleteStaribusBlocks - 1)))
                {
                // Strictly speaking the BLockCount could be 10000, but allow only to FFFF
                strResponseValue = Utilities.intToFourHexString(intCompleteStaribusBlocks);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

                if (dao.isCaptureMode())
                    {
                    // We don't mind about CAPTURE_ACTIVE, but remind the User anyway
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.CAPTURE_ACTIVE);
                    }
                }
            else
                {
                // This should not be possible....
                strResponseValue = StaribusCoreHostMemoryInterface.MSG_BLOCK_COUNT_OUT_OF_RANGE;
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // If there is no current file, then the block count must be zero
            // We know this is four digits
            strResponseValue = "0000";
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }

        // Dispose the File
        fileHostMemory = null;

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdGetBlockCount,
                                                                      null,
                                                                      null,
                                                                      strResponseValue
        );
        return (responseMessage);
        }
    }
