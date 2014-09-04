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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;


/***************************************************************************************************
 * StaribusCoreHostMemoryInterface.
 */

public interface StaribusCoreHostMemoryInterface extends ObservatoryInstrumentDAOInterface
    {
    // String Resources
    String MSG_HOST_MEMORY_FILE_NOT_FOUND = "HostMemory File not found";
    String MSG_HOST_MEMORY_FILE_ERROR = "HostMemory File error";
    String MSG_BLOCK_COUNT_OUT_OF_RANGE = "Block Count out of Range";
    String MSG_BLOCK_ID_OUT_OF_RANGE = "Block ID out of Range";
    String MSG_BLOCK_FORMAT_INVALID = "Unable to retrieve data block";


    // BlockID 0000 to FFFF for compatibility with the Futurlec, i.e. 65536 blocks
    int MAX_STARIBUS_BLOCK_COUNT = 0x10000;

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

    // This assumes a slow Temperature channel, and four data channels
    int FILE_RECORDS_PER_STARIBUS_BLOCK = 30;

    // This would give many days at 1 sample per second
    long MAX_FILE_RECORD_COUNT = MAX_STARIBUS_BLOCK_COUNT * FILE_RECORDS_PER_STARIBUS_BLOCK;

    // Logger getRealtimeData() format: +0321023102310231023
    int REALTIMEDATA_SIZE = "+032,1023,1023,1023,1023".length();

    // Saved in file as: 2000-00-00,00:00:00,+032,1023,1023,1023,1023
    int RECORD_SIZE_BYTES_CAPTURED = "2000-00-00,".length()
                                       + "00:00:00,".length()
                                       + REALTIMEDATA_SIZE
                                       + 1;  // CR

    // Maximum allowed file size in bytes
    long MAX_FILE_SIZE_BYTES = MAX_FILE_RECORD_COUNT * RECORD_SIZE_BYTES_CAPTURED;

    // Assume Date, Time, Temperature, Data * 4 i.e. 7 tokens
    int RECORD_TOKEN_COUNT = 7;

    // Indexes into the file record
    int INDEX_DATE = 0;
    int INDEX_TIME = 1;
    int INDEX_TEMP = 2;
    int INDEX_CH0 = 3;
    int INDEX_CH1 = 4;
    int INDEX_CH2 = 5;
    int INDEX_CH3 = 6;

    boolean TIMESTAMPED_HOSTMEMORYFILE = false;


    /***********************************************************************************************
     * getDataBlockCount().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getDataBlockCount(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * getDataBlock().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getDataBlock(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * getRate().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getRate(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * setRate().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface setRate(CommandMessageInterface commandmessage);


    /***********************************************************************************************
     * capture().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface capture(CommandMessageInterface commandmessage);


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the File used to simulate HostMemory.
     *
     * @return File
     */

    File getHostMemoryFile();


    /***********************************************************************************************
     * Set the File used to simulate HostMemory.
     *
     * @param file
     */

    void setHostMemoryFile(File file);


    /***********************************************************************************************
     * Get the RandomAccessFile used to simulate HostMemory.
     *
     * @return RandomAccessFile
     */

    RandomAccessFile getRandomAccessFile();


    /***********************************************************************************************
     * Set the RandomAccessFile used to simulate HostMemory.
     *
     * @param stream
     */

    void setRandomAccessFile(RandomAccessFile stream);


    /***********************************************************************************************
     * Get the Sample Rate.
     *
     * @return int
     */

    int getSampleRate();


    /***********************************************************************************************
     * Set the Sample Rate.
     *
     * @param rate
     */

    void setSampleRate(int rate);


    /***********************************************************************************************
     * Indicate if we are in capture mode.
     *
     * @return boolean
     */

    boolean isCaptureMode();


    /***********************************************************************************************
     * Indicate if we are in capture mode.
     *
     * @param capture
     */

    void setCaptureMode(boolean capture);


    /***********************************************************************************************
     * Get the Capture Timer.
     *
     * @return Timer
     */

    Timer getCaptureTimer();


    /***********************************************************************************************
     * Set the Capture Timer.
     *
     * @param timer
     */

    void setCaptureTimer(Timer timer);
    }
