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

import gnu.io.SerialPortEventListener;
import org.lmn.fc.common.constants.ControlCharacters;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//**************************************************************************************************
//    CommandMessage with no parameters, i.e. minimum length 15 before EOT
//
//    Header          STX
//    Address         char char  in Hex
//    CommandCode     char char char char
//    CommandVariant  char char char char
//    CrcChecksum     char char char char  in Hex
//    Terminator      EOT CR LF
//
//**************************************************************************************************
//    CommandMessage with Parameters
//
//    Header          STX
//    Address         char char  in Hex
//    CommandCode     char char char char
//    CommandVariant  char char char char
//    Separator       US
//    Parameters      char char {char char ..} US          parameter 0
//                    char char {char char ..} US          parameter 1
//    CrcChecksum     char char char char  in Hex
//    Terminator      EOT CR LF
//
/***************************************************************************************************
 * LoopbackStaribusRxStream.
 *
 * Default settings: 57600 Baud, 7 Data Bits, Even Parity, 1 Stop Bit, No flow control
 */

public final class LoopbackStaribusRxStream extends StaribusRxStream
                                            implements PortRxStreamInterface,
                                                       SerialPortEventListener
    {
    private static final int LENGTH_BEFORE_TERMINATOR = 15;
    private static final int RESPONSE_DEDBUG_COLUMNS = 16;
    private static final int MAX_MESSAGE_LENGTH = 2000;


    /***********************************************************************************************
     * Construct a LoopbackStaribusRxStream.
     *
     * @param resourcekey
     */

    public LoopbackStaribusRxStream(final String resourcekey)
        {
        super(resourcekey);

        // Allow the RxStream to return earlier, on receiving a CommandMessage,
        // which is shorter than a ResponseMessage
        setLengthBeforeTerminator(LENGTH_BEFORE_TERMINATOR);
        }


    /***********************************************************************************************
     * Get the Rx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.STARIBUS);
        }


    /***********************************************************************************************
     * Read a ResponseMessage from the stream, if possible.
     * The PortController will take the message and place it in the RxQueue for the host DaoPort.
     * Intentionally ignores the superclass because this completely replaces the message parser.
     *
     * @param instrumentsdoc
     *
     * @return ResponseMessageInterface
     *
     * @throws IOException
     */

    public ResponseMessageInterface read(final InstrumentsDocument instrumentsdoc) throws IOException
        {
        ResponseMessageInterface response;

        // NULL indicates that there's nothing to read, so PortController timeout is inevitable
        // We don't yet know the instrument context, so we can't simulate a response with status
        response = null;

        // There may be something in the input buffer?
        if ((getReadBuffer() != null)
            && (getReadBuffer().length() > 0)
            && (isStreamOpen())
            && (isAssembledMessageWaitingToBeReadByPortController()))
            {
            LOGGER.debugStaribusEvent(isDebugMode(),
                                      "LoopbackStaribusRxStream.read() Response buffer prepared for testing");

            if ((getLoopbackTxStream() != null)
                && (getLoopbackTxStream() instanceof LoopbackStaribusTxStream))
                {
                final CommandMessageInterface commandMessage;

                // Stop all incoming data until the next write()
                getLoopbackTxStream().reset();

                commandMessage = ((LoopbackStaribusTxStream) getLoopbackTxStream()).getCommandMessage();

                if (commandMessage != null)
                    {
                    final ResponseMessageStatus status;
                    final String strResponseValue;
                    final byte[] arrayReceived;
                    int intByteIndex;
                    boolean boolMatched;
                    byte byteSent;
                    int intIndexReceivedSTX;
                    boolean boolFoundIt;

                    arrayReceived = getReadBuffer().toString().getBytes();

                    // Check the returned bytes against those that were sent by LoopbackStaribusTxStream
                    // Remember the simulated SYN do not appear in the proper CommandMessage,
                    // but are added in the LoopbackTxStream
                    // Should be STX ... EOT CR LF
                    LOGGER.debugStaribusEvent(isDebugMode(),
                                              "LoopbackStaribusRxStream.read() SENT     [bytes=" + Utilities.byteArrayToExpandedAscii(commandMessage.getByteArray()) + "]");
                    // Should be {preamble_count SYN} STX ... EOT
                    LOGGER.debugStaribusEvent(isDebugMode(),
                                              "LoopbackStaribusRxStream.read() RECEIVED [bytes=" + Utilities.byteArrayToExpandedAscii(arrayReceived) + "]");

                    // Compare the arrays
                    boolMatched = true;
                    byteSent = ControlCharacters.NUL.getByteCode();
                    intIndexReceivedSTX = 0;
                    boolFoundIt = false;

                    for (intByteIndex = 0;
                         ((intByteIndex < arrayReceived.length)
                           && (!boolFoundIt));
                         intByteIndex++)
                        {
                        if (ControlCharacters.STX.getByteCode() == arrayReceived[intByteIndex])
                            {
                            intIndexReceivedSTX = intByteIndex;
                            boolFoundIt = true;
                            }
                        }

                    // If we didn't find STX it will fail anyway
                    // Stop checking at EOT or when we run out of data
                    for (intByteIndex = 0;
                         ((intByteIndex < commandMessage.getByteArray().length)
                            && ((intByteIndex + intIndexReceivedSTX) < arrayReceived.length)
                            && (ControlCharacters.EOT.getByteCode() != byteSent));
                         intByteIndex++)
                        {
                        byteSent = commandMessage.getByteArray()[intByteIndex];

                        if (arrayReceived[intByteIndex + intIndexReceivedSTX] != byteSent)
                            {
                            // Fail on the first error
                            boolMatched = false;
                            }
                        }

                    // Check that we matched each character with the ResponseMessage,
                    // as far as the EOT in the CommandMessage
                    if ((boolMatched)
                        && (ControlCharacters.EOT.getByteCode() == byteSent))
                        {
                        strResponseValue = "The Transmitted and Received messages are identical, up to EOT";
                        status = ResponseMessageStatus.SUCCESS;
                        }
                    else
                        {
                        final List<Byte> listResponse;

                        listResponse = new ArrayList<Byte>(MAX_MESSAGE_LENGTH);

                        for (intByteIndex = 0;
                             ((intByteIndex < commandMessage.getByteArray().length)
                                && (intByteIndex < arrayReceived.length));
                             intByteIndex++)
                            {
                            // One row from CommandMessage, one from ResponseMessage
                            for (int intColumnIndex = 0;
                                ((intColumnIndex < RESPONSE_DEDBUG_COLUMNS)
                                    && ((intByteIndex + intColumnIndex) < commandMessage.getByteArray().length));
                                intColumnIndex++)
                                {
                                listResponse.add(commandMessage.getByteArray()[intByteIndex + intColumnIndex]);
                                }

                            for (int intColumnIndex = 0;
                                ((intColumnIndex < RESPONSE_DEDBUG_COLUMNS)
                                    && ((intByteIndex + intColumnIndex) < arrayReceived.length));
                                intColumnIndex++)
                                {
                                listResponse.add(arrayReceived[intByteIndex + intColumnIndex]);
                                }

                            intByteIndex += RESPONSE_DEDBUG_COLUMNS;
                            }

                        // The Message was invalid in some way, so let's see why...
                        strResponseValue = Utilities.byteListToString(listResponse);
                        status = ResponseMessageStatus.INVALID_MESSAGE;
                        }

                    // Create a dummy ResponseMessage, assuming a DAO with a Port
                    if (commandMessage.getCommandType() != null)
                        {
                        if (commandMessage.getCommandType().getResponse() != null)
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      "LoopbackStaribusRxStream.read() RESPONSE Non-NULL [ResponseValue=" + strResponseValue + "]");
                            commandMessage.getCommandType().getResponse().setValue(strResponseValue);
                            }
                        else if (commandMessage.getCommandType().getAck() != null)
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      "LoopbackStaribusRxStream.read() RESPONSE Non-NULL [Ack] --> Debug Response only [ResponseValue=" + strResponseValue + "]");
                            }
                        else
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      "LoopbackStaribusRxStream.read() This Command does not produce a Response or Ack --> Debug Response only [ResponseValue=" + strResponseValue + "]");
                            }

                        response = ResponseMessageHelper.constructFailedResponseIfNullWithStatus(commandMessage.getDAO(),
                                                                                                 commandMessage,
                                                                                                 commandMessage.getCommandType(),
                                                                                                 null,
                                                                                                 status);

                        LOGGER.debugStaribusEvent(isDebugMode(),
                                                  "LoopbackStaribusRxStream.read() Returning simulated ResponseMessage to Port Controller");
                        }
                    else
                        {
                        LOGGER.debugStaribusEvent(isDebugMode(),
                                                  "LoopbackStaribusRxStream.read() Invalid CommandType, PortController timeout is inevitable");
                        }
                    }
                else
                    {
                    LOGGER.debugStaribusEvent(isDebugMode(),
                                              "LoopbackStaribusRxStream.read() LoopbackStaribusTxStream did not appear to send a CommandMessage, PortController timeout is inevitable");
                    }
                }
            else
                {
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "LoopbackStaribusRxStream.read() Invalid LoopbackStaribusTxStream, timeout is PortController inevitable");
                }

            // Clear the receive data buffer, regardless of the outcome of parsing the Response
            getReadBuffer().setLength(0);

            // Allow SerialEvents to gather data again
            setAssembledMessageWaitingToBeReadByPortController(false);
            }

        return (response);
        }
    }
