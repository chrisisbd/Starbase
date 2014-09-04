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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.CRC16;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOWrapper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractTxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * StubStaribusTxStream.
 */

public final class StubStaribusTxStream extends AbstractTxStream
                                        implements PortTxStreamInterface
    {
    // String Resources
    private static final String CONTROLLER_FILE = "workspace/staribus-stub/controller.xml";
    private static final String PLUGIN_FILE = "workspace/staribus-stub/plugin";
    private static final String EOF_MARKER = "\u0003";

    // Core <SendToPort> Commands requiring a response for the Stub
    private static final String RESET = "reset";
    private static final String PING = "ping";
    private static final String GET_CONFIGURATION_BLOCK_COUNT = "getConfigurationBlockCount";
    private static final String GET_CONFIGURATION_BLOCK = "getConfigurationBlock";

    private static final int MODULE_COUNT = 8;
    private static final int MAX_BLOCK_ID = 0xFFFF;
    private static final int LENGTH_CRC = 4;
    private static final int LENGTH_TAIL = LENGTH_CRC + 3;       // CRCChecksum + EOT + CR + LF


    /***********************************************************************************************
     * Validate the CRC checksum of the Tx Message.
     *
     * @param commandmessage
     */

    private static void validateChecksum(final CommandMessageInterface commandmessage)
        {
        final byte[] bytesInMessage;
        final byte[] bytesInCRCSegment;
        final byte[] bytesToCrc;
        final short shortCalculatedCRC;
        int intIndex;
        short shortMessageCRC;

        //------------------------------------------------------------------------------------------
        // Check the validity of the CRC checksum
        //    CrcChecksum     char char char char  in Hex
        //    Terminator      EOT CR LF

        bytesInMessage = commandmessage.getByteArray();
        bytesInCRCSegment = new byte[LENGTH_CRC];

        // Get hold of only the CRC bytes
        // <stx>2B0001 060A <eot><cr><lf>
        System.arraycopy(bytesInMessage, (bytesInMessage.length - LENGTH_TAIL),
                         bytesInCRCSegment, 0,
                         LENGTH_CRC);

        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.write() CRC segment=[" + Utilities.byteArrayToExpandedAscii(bytesInCRCSegment) + "]");

        // We expect four characters of CRC checksum
        // The result must be in the range 0000-FFFF
        intIndex = 0;
        shortMessageCRC = (short)(Character.getNumericValue(bytesInCRCSegment[intIndex++]) << 4);
        shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytesInCRCSegment[intIndex++]));
        shortMessageCRC = (short)(shortMessageCRC << 4);
        shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytesInCRCSegment[intIndex++]));
        shortMessageCRC = (short)(shortMessageCRC << 4);
        shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytesInCRCSegment[intIndex]));

        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.write() TxMessage contains CRC Checksum=" + Utilities.intToFourHexString(shortMessageCRC));

        // Calculate the CRC Checksum of the message,
        // excluding the STX, and up to the character before the checksum field
        // <stx> 2B0001 060A<eot><cr><lf>
        bytesToCrc = new byte[bytesInMessage.length - LENGTH_TAIL - 1];

        // Exclude the STX from the checksum, so start at index=1
        System.arraycopy(bytesInMessage, 1,
                         bytesToCrc, 0,
                         bytesInMessage.length - LENGTH_TAIL - 1);

        // Note the assumption that Input and Output bits are reflected
        //LOGGER.debugStaribusEvent("StubStaribusTxStream.write() Calculating Tx CRC using [" + Utilities.byteArrayToSpacedHex(bytesToCrc) + "]");

        shortCalculatedCRC = CRC16.crc16(bytesToCrc, true, true);
        //LOGGER.debugStaribusEvent("StubStaribusTxStream.write() Calculated Tx CRC Checksum=" + Utilities.intToFourHexString(shortCalculatedCRC));

        if (shortMessageCRC != shortCalculatedCRC)
            {
            LOGGER.error("StubStaribusTxStream.write() Tx CRC Checksum mismatch! [message=" + Integer.toHexString(shortMessageCRC) + "] [calculated=" + Integer.toHexString(shortCalculatedCRC) + "]");
            }
        else
            {
            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      "StubStaribusTxStream.write() Tx CRC Checksum correct!");
            }
        }


    /***********************************************************************************************
     * Construct a StubStaribusTxStream.
     *
     * @param resourcekey
     */

    public StubStaribusTxStream(final String resourcekey)
        {
        super(resourcekey);
        }


    /***********************************************************************************************
     * Get the Tx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.STARIBUS);
        }


    /***********************************************************************************************
     * Initialise the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean initialise() throws IOException
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.initialise()");

        // Mark this Stream as Closed
        this.boolStreamOpen = false;

        return (true);
        }


    /***********************************************************************************************
     * Open the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean open() throws IOException
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.open()");

        readResources();

        // Mark this Stream as Open, if successful
        this.boolStreamOpen = true;

        return (this.boolStreamOpen);
        }


    /***********************************************************************************************
     * Closes this stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    public void close() throws IOException
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.close()");

        // Mark this Stream as Closed
        this.boolStreamOpen = false;
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.reset()");
        }


    /***********************************************************************************************
     * Flushes this stream by writing any buffered output to the underlying stream.
     *
     * @throws IOException If an I/O error occurs
     */

    public void flush() throws IOException
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.flush()");
        }


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     * This stub must also provide a dummy Response to the loopback RxStream.
     *
     * @param instrumentsdoc
     * @param commandmessage
     *
     * @throws IOException
     */

    public final void write(final InstrumentsDocument instrumentsdoc,
                            final CommandMessageInterface commandmessage) throws IOException
        {
        final ResponseMessageInterface responseConstructed;
        final ResponseMessageInterface responseParsed;
        final List<String> listFileName;
        StringBuffer bufferFile;

//        LOGGER.debugStaribusEvent("StubStaribusTxStream.write() sending bytes=[" + Utilities.byteArrayToExpandedAscii(commandmessage.getByteArray()) +"]",
//                                  LOADER_PROPERTIES.isStaribusDebug());
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.write() sending String=[" + new String(commandmessage.getByteArray()) +"]");

        bufferFile = new StringBuffer();

        //------------------------------------------------------------------------------------------
        // Validate the CRC checksum of the Tx Message

        validateChecksum(commandmessage);

        //------------------------------------------------------------------------------------------
        // Prepare the stub files for the possibility of a getConfiguration()

        listFileName = new ArrayList<String>(MODULE_COUNT);

        // Prepare all stub filenames
        listFileName.add(CONTROLLER_FILE);

        for (int i = 1; i < MODULE_COUNT; i++)
            {
            listFileName.add(PLUGIN_FILE + i + DOT + FileUtilities.xml);
            }

        //--------------------------------------------------------------------------====------------
        // Method 1: Process the CommandMessage

        // This stub must provide a dummy Response to the RxStream,
        // but only if a Response is required
        // Was there a Response or Ack required?

        if ((commandmessage.getCommandType().getResponse() != null)
            || (commandmessage.getCommandType().getAck() != null))
            {
            final String strResponseValue;

            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      "StubStaribusTxStream.write() Creating dummy StaribusResponseMessage");

            if ((RESET.equals(commandmessage.getCommandType().getIdentifier()))
                && (commandmessage.getCommandType().getAck() != null))
                {
                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          "StubStaribusTxStream.write() Execute " + RESET);
                }
            else if ((PING.equals(commandmessage.getCommandType().getIdentifier()))
                && (commandmessage.getCommandType().getAck() != null))
                {
                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          "StubStaribusTxStream.write() Execute " + PING);
                }
            else if ((GET_CONFIGURATION_BLOCK_COUNT.equals(commandmessage.getCommandType().getIdentifier()))
                && (commandmessage.getCommandType().getResponse() != null)
                && (commandmessage.getCommandType().getParameterList() != null)
                && (commandmessage.getCommandType().getParameterList().size() == 1)
                && (PARAMETER_CONFIGURATION_MODULEID.equals(commandmessage.getCommandType().getParameterList().get(0).getName())))
                {
                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          "StubStaribusTxStream.write() Execute " + GET_CONFIGURATION_BLOCK_COUNT);

                // This command gets the block count from the specified (module_id) if possible
                try
                    {
                    final ParameterType parameter;
                    final int intModuleID;

                    // We expect this to be overwritten
                    commandmessage.getCommandType().getResponse().setValue(Utilities.intToFourHexString(0));

                    // Retrieve the requested ModuleID (encoded as Decimal)
                    parameter = commandmessage.getCommandType().getParameterList().get(0);
                    intModuleID = Integer.parseInt(parameter.getValue(), RADIX_DECIMAL);

                    LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "StubStaribusTxStream.write() Stubbing " + GET_CONFIGURATION_BLOCK_COUNT + " [moduleid=" + intModuleID + "]");

                    // This should have been checked by Regex anyway
                    if ((intModuleID >= 0)
                        && (intModuleID < MODULE_COUNT))
                        {
                        final int intIndexToEndOfFile;

                        // Read the requested stub file
                        bufferFile = FileUtilities.readFileAsString(listFileName.get(intModuleID));

                        // Is it a correctly formatted file?
                        intIndexToEndOfFile = bufferFile.indexOf(EOF_MARKER);

                        if (intIndexToEndOfFile >= 0)
                            {
                            int intBlockCount;

                            // Calculate the BlockCount every time
                            intBlockCount = intIndexToEndOfFile / DATATYPE_XML_BLOCK_SIZE;

                            if (Math.IEEEremainder(intIndexToEndOfFile,
                                                   DATATYPE_XML_BLOCK_SIZE) != 0.0)
                                {
                                // Include the last partial block
                                intBlockCount++;
                                }

                            commandmessage.getCommandType().getResponse().setValue(Utilities.intToFourHexString(intBlockCount));

                            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                      "StubStaribusTxStream.write() [blockcount=" + intBlockCount + "] [blocksize=" + DATATYPE_XML_BLOCK_SIZE + "] [filelength=" + intIndexToEndOfFile + "]");

                            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                      "StubStaribusTxStream.write() Stubbing "
                                                            + GET_CONFIGURATION_BLOCK_COUNT
                                                            + " [ResponseValue="
                                                            + commandmessage.getCommandType().getResponse().getValue() + "]");
                            }
                        else
                            {
                            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                      "StubStaribusTxStream.write() No "
                                                        + EOF_MARKER
                                                        + " marker in the stub file for PluginID "
                                                        + intModuleID);
                            }
                        }
                    else
                        {
                        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                  "StubStaribusTxStream.write() ModuleID out of range");
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "StubStaribusTxStream.write() Unable to parse Parameter ModuleID");
                    }
                }
            else if ((GET_CONFIGURATION_BLOCK.equals(commandmessage.getCommandType().getIdentifier()))
                && (commandmessage.getCommandType().getResponse() != null)
                && (commandmessage.getCommandType().getParameterList() != null)
                && (commandmessage.getCommandType().getParameterList().size() == 2)
                && (PARAMETER_CONFIGURATION_MODULEID.equals(commandmessage.getCommandType().getParameterList().get(0).getName()))
                && (PARAMETER_CONFIGURATION_BLOCKID.equals(commandmessage.getCommandType().getParameterList().get(1).getName())))
                {
                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          "StubStaribusTxStream.write() Execute " + GET_CONFIGURATION_BLOCK);

                // This command gets the block from the specified (module_id, block_id) if possible
                try
                    {
                    ParameterType parameter;
                    final int intModuleID;
                    final int intBlockID;

                    // We expect this to be overwritten
                    commandmessage.getCommandType().getResponse().setValue("FAILED");

                    // Retrieve the requested ModuleID (encoded as Decimal)
                    parameter = commandmessage.getCommandType().getParameterList().get(0);
                    intModuleID = Integer.parseInt(parameter.getValue(), RADIX_DECIMAL);

                    // Retrieve the requested BlockID (encoded as Hex)
                    parameter = commandmessage.getCommandType().getParameterList().get(1);
                    intBlockID = Integer.parseInt(parameter.getValue(), RADIX_HEX);

                    LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "StubStaribusTxStream.write() Stubbing "
                                                    + GET_CONFIGURATION_BLOCK
                                                    + " [moduleid=" + intModuleID + "] [blockid=" + intBlockID + "]");

                    // These should have been checked by Regex anyway
                    if ((intModuleID >= 0)
                        && (intModuleID < MODULE_COUNT)
                        && (intBlockID >= 0)
                        && (intBlockID <= MAX_BLOCK_ID))
                        {
                        final int intIndexToEndOfFile;
                        final int intIndexStart;

                        // Read the requested stub file
                        bufferFile = FileUtilities.readFileAsString(listFileName.get(intModuleID));

                        // Is it a correctly formatted file?
                        intIndexToEndOfFile = bufferFile.indexOf(EOF_MARKER);

                        if (intIndexToEndOfFile >= 0)
                            {
                            // Index in to the start of the data
                            intIndexStart = DATATYPE_XML_BLOCK_SIZE * intBlockID;

                            if (intIndexStart < intIndexToEndOfFile)
                                {
                                final StringBuffer buffer;

                                buffer = new StringBuffer();

                                // Can we read to the end of the requested data?
                                if ((intIndexStart + DATATYPE_XML_BLOCK_SIZE) <= intIndexToEndOfFile)
                                    {
                                    final int intEnd;

                                    intEnd = intIndexStart + DATATYPE_XML_BLOCK_SIZE;
                                    buffer.append(bufferFile.substring(intIndexStart, intEnd));
                                    }
                                else
                                    {
                                    final int intTruncatedEnd;

                                    // We need to pad the last block with spaces
                                    intTruncatedEnd = intIndexToEndOfFile;
                                    buffer.append(bufferFile.substring(intIndexStart, intTruncatedEnd));

                                    // Don't use buffer.length() as counter!
                                    for (int i = 0;
                                         i < (DATATYPE_XML_BLOCK_SIZE - (intTruncatedEnd - intIndexStart));
                                         i++)
                                        {
                                        buffer.append(SPACE);
                                        }
                                    }

                                // Save the final ResponseValue
                                commandmessage.getCommandType().getResponse().setValue(buffer.toString());

                                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                          "StubStaribusTxStream.write() Stubbing " + GET_CONFIGURATION_BLOCK + " [ResponseValue="
                                                                + commandmessage.getCommandType().getResponse().getValue() + "] [blocksize=" + buffer.length() + "]");
                                }
                            else
                                {
                                // Attempting to read past the EndOfFile marker
                                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                          "StubStaribusTxStream.write() BlockID would read beyond EOF [blockid=" + intBlockID + "]");
                                }
                            }
                        else
                            {
                            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                      "StubStaribusTxStream.write() No "
                                                        + EOF_MARKER
                                                        + " marker in the stub file for PluginID "
                                                        + intModuleID);
                            }
                        }
                    else
                        {
                        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                                  "StubStaribusTxStream.write() ModuleID or BlockID out of range");
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "StubStaribusTxStream.write() Unable to parse Parameter ModuleID or BlockID");
                    }
                }
            else if (commandmessage.getCommandType().getResponse() != null)
                {
                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          "StubStaribusTxStream.write() Execute "
                                                + commandmessage.getCommandType().getIdentifier()
                                                + " Inserting dummy ResponseValue");
                DAOHelper.insertDummyResponseValue((commandmessage.getCommandType().getResponse()));
                }

            //--------------------------------------------------------------------------------------
            // Sort out a ResponseValue

            if (commandmessage.getCommandType().getResponse() != null)
                {
                strResponseValue = commandmessage.getCommandType().getResponse().getValue();
                }
            else
                {
                strResponseValue = EMPTY_STRING;
                }

            // This Response must tie up with the Command which was just sent
            // Construct a simulated Response, complete with byte array
            // Generate a dummy ResponseMessageStatus with up to four error bits, or success
            responseConstructed = new StaribusResponseMessage(ResponseMessageHelper.getPortName(commandmessage.getDAO()),
                                                              commandmessage.getInstrument(),
                                                              commandmessage.getModule(),
                                                              commandmessage.getCommandType(),  // Contains new Value (or not)
                                                              StaribusResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                               commandmessage.getModule(),
                                                                                                               commandmessage.getCommandType()),
                                                              0); // Status bits (0 = success)

            // We could simulate errors here...
            // DAOHelper.createDummyResponseMessageStatusBitMask()); // Status bits (0 = success)

//                LOGGER.debugStaribusEvent("StubStaribusTxStream.write() Constructed StaribusResponseMessage bytes=[" + Utilities.byteArrayToSpacedHex(responseConstructed.getByteArray()) + "]",
//                                          LOADER_PROPERTIES.isStaribusDebug());
            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      "StubStaribusTxStream.write() Constructed StaribusResponseMessage String=[" + new String(responseConstructed.getByteArray()) + "]");

            //--------------------------------------------------------------------------------------
            // Method 2: Re-parse the byte array of the simulated Response stream
            // This is now mimicing the behaviour of the real RxStream
            // This may return NULL, e.g. on TIMEOUT

            final List<ObservatoryInstrumentDAOInterface> listDAOs;

            listDAOs = new ArrayList<ObservatoryInstrumentDAOInterface>(10);
            listDAOs.add(commandmessage.getDAO());
            responseParsed = StaribusResponseMessage.parseStaribusBytes(instrumentsdoc,
                                                                        listDAOs,
                                                                        responseConstructed.getByteArray(),
                                                                        LOADER_PROPERTIES.isStaribusDebug());
            if (responseParsed != null)
                {
                final ObservatoryInstrumentDAOInterface dao;

                dao = commandmessage.getDAO();

                // Add any data or metadata produced by this command
                dao.setRawData(null);
                dao.setXYDataset(null);
                dao.setImageData(null);

                dao.setRawDataChannelCount(0);
                dao.setTemperatureChannel(false);
                dao.setUnsavedData(false);

                // The RawData etc. cannot be parsed from the message, so must be set to NULL
                responseParsed.setWrappedData(new DAOWrapper(commandmessage,
                                                             responseParsed,
                                                             strResponseValue,
                                                             dao));
                }

            // Put the Response into the loopback...
            // If the response is null (i.e. parsing failed for this chunk),
            // then no action is taken by the PortController
            ((StubStaribusRxStream) getLoopbackRxStream()).putResponseToStream(responseParsed);
            }
        else
            {
            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      "StubStaribusTxStream.write() Command does not require a Response");
            }

        // Tidy up...
        bufferFile.setLength(0);
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public final void readResources()
        {
        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "StubStaribusTxStream.readResources() [ResourceKey=" + getResourceKey() + "]");
        }
    }
