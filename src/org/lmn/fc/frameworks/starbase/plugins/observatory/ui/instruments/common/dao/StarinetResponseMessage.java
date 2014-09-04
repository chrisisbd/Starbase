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

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.CRC16;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.xmlbeans.instruments.*;

import java.util.*;

//**************************************************************************************************
//    Response Only, or ErrorResponse for a Command which would expect a ResponseValue
//
//    Header          STX
//    Address         char char
//    CommandCode     char char char char
//    CommandVariant  char char char char
//    StatusCode      char char char char
//    CrcChecksum     char char char char  in Hex
//    Terminator      EOT CR LF
//
//    h n n n n n n n n n n n n n n n n n n e c l
//    i.e. must get at least 19 before looking for the terminator
//
//**************************************************************************************************
//    Response with Value
//
//    Header          STX
//    Address         char char
//    CommandCode     char char char char
//    CommandVariant  char char char char
//    StatusCode      char char char char
//    Separator       US
//    Value           char {char ..} US
//    CrcChecksum     char char char char  in Hex
//    Terminator      EOT CR LF
//
/***************************************************************************************************
 * StarinetResponseMessage.
 */

public final class StarinetResponseMessage extends AbstractResponseMessage
    {
    private static final int LENGTH_RESPONSE = 22;              // Minimum whole message with no Value payload
    private static final int LENGTH_COMMAND_VARIANT = 4;
    private static final int LENGTH_CRC = 4;
    private static final int LENGTH_TAIL = LENGTH_CRC + 3;      // CRCChecksum + EOT + CR + LF

    private static final int MASK_WORD = 0xffff;
    private static final int MASK_BYTE = 0xff;


    /***********************************************************************************************
      * Parse a byte array, attempting to find a StarinetResponseMessage
      * which can be interpreted as coming from one of the Instruments.
      * A null Response means that there's nothing going to appear in the RxStream.
      *
      * @param commandmessage
      * @param daos
      * @param bytes
      * @param debugmode
     *
      * @return ResponseMessageInterface
      */

     public synchronized static ResponseMessageInterface parseStarinetBytes(final CommandMessageInterface commandmessage,
                                                                            final List<ObservatoryInstrumentDAOInterface> daos,
                                                                            final byte[] bytes,
                                                                            final boolean debugmode)
         {
         final String SOURCE = "StarinetResponseMessage.parseStarinetBytes() ";
         int intAddress;
         int intCommandCodeBase;
         int intCommandCode;
         int intCommandVariant;
         int intStatusWord;
         short shortMessageCRC;
         byte[] byteResponseValue;
         final int intLength;
         ResponseMessageInterface responseMessage;
         int intIndex;
         final int intIndexOfStart;

         byteResponseValue = null;
         responseMessage = null;
         intIndex = 0;

         LOGGER.debugStarinetEvent(debugmode,
                                   SOURCE + "Parsing [" + Utilities.byteArrayToSpacedHex(bytes) + "]");
         LOGGER.debugStarinetEvent(debugmode,
                                   SOURCE + "Parsing [" + Utilities.byteArrayToExpandedAscii(bytes) + "]" );

         if (LOADER_PROPERTIES.isCommandVariant())
             {
             intLength = LENGTH_RESPONSE;
             }
         else
             {
             intLength = LENGTH_RESPONSE - LENGTH_COMMAND_VARIANT;
             }

         // Any shorter than intLength, and it is not worth parsing
         if ((commandmessage != null)
             && (commandmessage.getCommandType() != null)
             && (daos != null)
             && (daos.size() > 0)
             && (daos.get(0) != null)
             && (daos.get(0).getPort() != null)
             && (daos.get(0).getPort().getTxStream() != null)
             && (daos.get(0).getPort().getRxStream() != null)
             && (bytes != null)
             && (bytes.length >= intLength))
             {
             try
                 {
                 // There may be some lead-in characters (very unlikely!)
                 // Search for the start character just in case
                 while ((intIndex < bytes.length)
                     && (bytes[intIndex] != STARIBUS_START))
                     {
                     intIndex++;
                     }

                 // Record the actual start index, after removal of lead-in
                 intIndexOfStart = intIndex;

                 // Check that we are synchronised, and still have enough bytes left
                 if ((bytes[intIndex] == STARIBUS_START)
                     && ((bytes.length - intIndex) >= intLength))
                     {
                     final boolean boolParsingSucceeded;

                     intIndex++;

                     // The next two characters are the device address,
                     // to be converted from the HEX representation of two nibbles
                     // to an unsigned Integer as a String!
                     // The result must be in the range 00-FF
                     // For Starinet, this should be 00, but continue with any value
                     intAddress = Character.getNumericValue(bytes[intIndex++]) << 4;
                     intAddress = intAddress + Character.getNumericValue(bytes[intIndex++]);

                     if ((intAddress >= 0)
                         && (intAddress <= MASK_BYTE))
                         {
                         LOGGER.debugStarinetEvent(debugmode,
                                                   SOURCE + "Address=" + Utilities.intToTwoHexString(intAddress));

                         // The next two characters are the CommandCodeBase
                         // The result must be in the range 00-FF
                         intCommandCodeBase = Character.getNumericValue(bytes[intIndex++]) << 4;
                         intCommandCodeBase = intCommandCodeBase + Character.getNumericValue(bytes[intIndex++]);

                         if ((intCommandCodeBase >= 0)
                             && (intCommandCodeBase <= MASK_BYTE))
                             {
                             LOGGER.debugStarinetEvent(debugmode,
                                                       SOURCE + "CommandCodeBase=" + Utilities.intToTwoHexString(intCommandCodeBase));

                             // The next two characters are the CommandCode
                             // The result must be in the range 00-FF
                             intCommandCode = Character.getNumericValue(bytes[intIndex++]) << 4;
                             intCommandCode = intCommandCode + Character.getNumericValue(bytes[intIndex++]);

                             if ((intCommandCode >= 0)
                                 && (intCommandCode <= MASK_BYTE))
                                 {
                                 LOGGER.debugStarinetEvent(debugmode,
                                                           SOURCE + "CommandCode=" + Utilities.intToTwoHexString(intCommandCode));

                                 // See which protocol we are using...
                                 if (LOADER_PROPERTIES.isCommandVariant())
                                     {
                                     // The next four characters are the CommandVariant
                                     // The result must be in the range 0000-FFFF
                                     intCommandVariant = Character.getNumericValue(bytes[intIndex++]) << 4;
                                     intCommandVariant = intCommandVariant + Character.getNumericValue(bytes[intIndex++]);
                                     intCommandVariant = intCommandVariant << 4;
                                     intCommandVariant = intCommandVariant + Character.getNumericValue(bytes[intIndex++]);
                                     intCommandVariant = intCommandVariant << 4;
                                     intCommandVariant = intCommandVariant + Character.getNumericValue(bytes[intIndex++]);
                                     }
                                 else
                                     {
                                     intCommandVariant = 0;
                                     }

                                 if ((intCommandVariant >= 0)
                                     && (intCommandVariant <= MASK_WORD))
                                     {
                                     LOGGER.debugStarinetEvent(debugmode,
                                                               SOURCE + "CommandVariant=" + Utilities.intToFourHexString(intCommandVariant));

                                     // The next four characters are the Status
                                     // The result must be in the range 0000-FFFF
                                     intStatusWord = Character.getNumericValue(bytes[intIndex++]) << 4;
                                     intStatusWord = intStatusWord + Character.getNumericValue(bytes[intIndex++]);
                                     intStatusWord = intStatusWord << 4;
                                     intStatusWord = intStatusWord + Character.getNumericValue(bytes[intIndex++]);
                                     intStatusWord = intStatusWord << 4;
                                     intStatusWord = intStatusWord + Character.getNumericValue(bytes[intIndex++]);

                                     if ((intStatusWord >= 0)
                                         && (intStatusWord <= MASK_WORD))
                                         {
                                         // The status map is in ResponseMessageStatus
                                         LOGGER.debugStarinetEvent(debugmode,
                                                                   SOURCE + "Status=" + Utilities.intToFourHexString(intStatusWord));

                                         // Do we have a US as the next byte?
                                         // If so, we *expect* a Response value (otherwise, it is an Ack)
                                         if (bytes[intIndex] == STARIBUS_DELIMITER)
                                             {
                                             final int intStart;
                                             final int intEnd;

                                             LOGGER.debugStarinetEvent(debugmode,
                                                                       SOURCE + "US separator found; parsing ResponseValue");

                                             // Move to the first ResponseValue byte
                                             intIndex++;
                                             intStart = intIndex;

                                             // We now need to keep checking that there are enough characters left to parse
                                             while ((intIndex < bytes.length)
                                                 && (bytes[intIndex] != STARIBUS_DELIMITER))
                                                 {
                                                 intIndex++;
                                                 }

                                             // Check that we did in fact find the ResponseValue terminator
                                             // All bytes must be copied to the value, except for the closing delimiter
                                             if (bytes[intIndex] == STARIBUS_DELIMITER)
                                                 {
                                                 intEnd = intIndex;

                                                 // Response values could be over 512 characters?
                                                 byteResponseValue = new byte[intEnd-intStart];

                                                 // Capture the ResponseValue bytes verbatim
                                                 System.arraycopy(bytes,
                                                                  intStart,
                                                                  byteResponseValue,
                                                                  0,
                                                                  (intEnd-intStart));

                                                 LOGGER.debugStarinetEvent(debugmode,
                                                                           SOURCE + "Response=[" + Utilities.byteArrayToSpacedHex(byteResponseValue).trim() + "]");

                                                 // Move to the first character of the checksum
                                                 intIndex++;
                                                 boolParsingSucceeded = true;
                                                 }
                                             else
                                                 {
                                                 LOGGER.error(SOURCE + "ResponseValue terminator not found");
                                                 boolParsingSucceeded = false;
                                                 }
                                             }
                                         else
                                             {
                                             // It is acceptable to have a message defined with no ResponseValue,
                                             // but an error to have a message which expects a ResponseValue which is missing
                                             // We can't yet tell the difference, because we don't know which Command is involved,
                                             // so we must let it pass to the next stage of parsing,
                                             // where byteResponseValue will be empty...
                                             // The Response may also be a valid ErrorResponse, with no payload, but valid Status
                                             LOGGER.debugStarinetEvent(debugmode,
                                                                       SOURCE + "ResponseValue not expected or present");
                                             boolParsingSucceeded = true;
                                             }

                                         // intIndex should now be pointing at the first character of the checksum
                                         // There must now be at least LENGTH_TAIL characters left to complete the message
                                         // i.e. CRCChecksum + EOT + CR + LF
                                         // However, don't bother to complete the job if something failed beforehand...
                                         if ((boolParsingSucceeded)
                                             && (bytes.length - intIndex) >= LENGTH_TAIL)
                                             {
                                             final int intMessageLengthWithoutCrcChecksum;

                                             intMessageLengthWithoutCrcChecksum = intIndex - intIndexOfStart;

                                             // We expect four characters of CRC checksum
                                             // The result must be in the range 0000-FFFF
                                             shortMessageCRC = (short)(Character.getNumericValue(bytes[intIndex++]) << 4);
                                             shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytes[intIndex++]));
                                             shortMessageCRC = (short)(shortMessageCRC << 4);
                                             shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytes[intIndex++]));
                                             shortMessageCRC = (short)(shortMessageCRC << 4);
                                             shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytes[intIndex++]));

                                             // How could this not be true?!
                                             if ((shortMessageCRC >= 0)
                                                 && (shortMessageCRC <= MASK_WORD))
                                                 {
                                                 final byte[] bytesToCrc;
                                                 final short shortCalculatedCRC;

                                                 LOGGER.debugStarinetEvent(debugmode,
                                                                           SOURCE + "RxMessage contains CRC Checksum=" + Utilities.intToFourHexString(shortMessageCRC));

                                                 // Calculate the CRC Checksum of the message,
                                                 // up to the character before the checksum field
                                                 bytesToCrc = new byte[intMessageLengthWithoutCrcChecksum-1];

                                                 // Exclude the STX from the checksum, so start at index=1
                                                 System.arraycopy(bytes, intIndexOfStart + 1,
                                                                  bytesToCrc, 0,
                                                                  intMessageLengthWithoutCrcChecksum-1);

                                                 // Note the assumption that Input and Output bits are reflected
                                                 LOGGER.debugStarinetEvent(debugmode,
                                                                           SOURCE + "Calculating Rx CRC using [" + Utilities.byteArrayToSpacedHex(bytesToCrc) + "]");

                                                 shortCalculatedCRC = CRC16.crc16(bytesToCrc, true, true);
                                                 LOGGER.debugStarinetEvent(debugmode,
                                                                           SOURCE + "Calculated Rx CRC Checksum=" + Utilities.intToFourHexString(shortCalculatedCRC));

                                                 if (shortMessageCRC != shortCalculatedCRC)
                                                     {
                                                     LOGGER.error(SOURCE + "Rx CRC Checksum mismatch! [message=" + Utilities.intToFourHexString(shortMessageCRC) + "] [calculated=" + Utilities.intToFourHexString(shortCalculatedCRC) + "]");

                                                     intStatusWord |= ResponseMessageStatus.CRC_ERROR.getBitMask();
                                                     }
                                                 else
                                                     {
                                                     LOGGER.debugStarinetEvent(debugmode,
                                                                               SOURCE + "Rx CRC Checksum correct!");
                                                     }

                                                 // Finally, three terminators to complete the ResponseMessage
                                                 if ((bytes[intIndex++] == STARIBUS_TERMINATOR_0)
                                                     && (bytes[intIndex++] == STARIBUS_TERMINATOR_1)
                                                     && (bytes[intIndex] == STARIBUS_TERMINATOR_2))
                                                     {
                                                     // Try to find the Response in the Instruments XML
                                                     // This may come back NULL on failure
                                                     // Also, handle empty ResponseValue for malformed ResponseMessages
                                                     // or a valid ErrorResponse with no payload
                                                     // Allow CRC errors to go to locateResponseContext()
                                                     // since the addressing part of the message may be valid
                                                     responseMessage = locateResponseContext(commandmessage,
                                                                                      daos,
                                                                                      bytes,
                                                                                      intAddress,
                                                                                      intCommandCodeBase,
                                                                                      intCommandCode,
                                                                                      intCommandVariant,
                                                                                      intStatusWord,
                                                                                      byteResponseValue,
                                                                                      shortMessageCRC,
                                                                                      debugmode);
                                                     LOGGER.debugStarinetEvent(debugmode,
                                                                               SOURCE + "returned from locateResponseContext()");

                                                     // Don't allow anything other than SUCCESS or BUSY or CAPTURE_ACTIVE
                                                     if (!ResponseMessageStatus.wasResponseStatusSuccessful(intStatusWord))
                                                         {
                                                         LOGGER.error(SOURCE + "Response may be valid, but Status was not SUCCESS or BUSY or CAPTURE_ACTIVE");
                                                         DAOCommandHelper.logResponseBytes(daos,
                                                                                           bytes,
                                                                                           intStatusWord,
                                                                                           SOURCE + "Response may be valid, but Status was not SUCCESS or BUSY or CAPTURE_ACTIVE");
                                                         }

                                                     // Response may be NULL only if locateResponseContext() failed to find a Context,
                                                     // otherwise return a valid ResponseMessage or an ErrorResponse
                                                     }
                                                 else
                                                     {
                                                     LOGGER.error(SOURCE + "Final terminators EOT CR LF not found");
                                                     DAOCommandHelper.logResponseMessageBytes(daos,
                                                                                              bytes,
                                                                                              SOURCE + "Final terminators EOT CR LF not found");
                                                     // We don't have any Context, so can't return an ErrorResponse
                                                     responseMessage = null;
                                                     }
                                                 }
                                             else
                                                 {
                                                 LOGGER.error(SOURCE + "Checksum out of range " + shortMessageCRC);
                                                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                                                          bytes,
                                                                                          SOURCE + "Checksum out of range " + shortMessageCRC);
                                                 // We don't have any Context, so can't return an ErrorResponse
                                                 responseMessage = null;
                                                 }
                                             }
                                         else
                                             {
                                             LOGGER.error(SOURCE + "Parsing failed, or insufficient characters remain [count=" + (bytes.length - intIndex) + "]");
                                             DAOCommandHelper.logResponseMessageBytes(daos,
                                                                                      bytes,
                                                                                      SOURCE + "Parsing failed, or insufficient characters remain [count=" + (bytes.length - intIndex) + "]");
                                             // We don't have any Context, so can't return an ErrorResponse
                                             responseMessage = null;
                                             }
                                         }
                                     else
                                         {
                                         LOGGER.error(SOURCE + "Status field out of range " + intStatusWord);
                                         DAOCommandHelper.logResponseMessageBytes(daos,
                                                                                  bytes,
                                                                                  SOURCE + "Status field out of range " + intStatusWord);
                                         // We don't have any Context, so can't return an ErrorResponse
                                         responseMessage = null;
                                         }
                                     }
                                 else
                                     {
                                     LOGGER.error(SOURCE + "CommandVariant field out of range " + intCommandVariant);
                                     DAOCommandHelper.logResponseMessageBytes(daos,
                                                                              bytes,
                                                                              SOURCE + "CommandVariant field out of range " + intCommandVariant);
                                     // We don't have any Context, so can't return an ErrorResponse
                                     responseMessage = null;
                                     }
                                 }
                             else
                                 {
                                 LOGGER.error(SOURCE + "CommandCode field out of range " + intCommandCode);
                                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                                          bytes,
                                                                          SOURCE + "CommandCode field out of range " + intCommandCode);
                                 // We don't have any Context, so can't return an ErrorResponse
                                 responseMessage = null;
                                 }
                             }
                         else
                             {
                             LOGGER.error(SOURCE + "CommandCodeBase field out of range " + intCommandCodeBase);
                             DAOCommandHelper.logResponseMessageBytes(daos,
                                                                      bytes,
                                                                      SOURCE + "CommandCodeBase field out of range " + intCommandCodeBase);
                             // We don't have any Context, so can't return an ErrorResponse
                             responseMessage = null;
                             }
                         }
                     else
                         {
                         LOGGER.error(SOURCE + "Address field out of range " + intAddress);
                         DAOCommandHelper.logResponseMessageBytes(daos,
                                                                  bytes,
                                                                  SOURCE + "Address field out of range " + intAddress);
                         // We don't have any Context, so can't return an ErrorResponse
                         responseMessage = null;
                         }
                     }
                 else
                     {
                     LOGGER.error(SOURCE + "Failed to synchronise on STX");
                     DAOCommandHelper.logResponseMessageBytes(daos,
                                                              bytes,
                                                              SOURCE + "Failed to synchronise on STX");
                     // We don't have any Context, so can't return an ErrorResponse
                     responseMessage = null;
                     }
                 }

             catch (ArrayIndexOutOfBoundsException exception)
                 {
                 LOGGER.error(SOURCE + "ArrayIndexOutOfBoundsException [exception=" + exception.getMessage() + "]");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          bytes,
                                                          SOURCE + "ArrayIndexOutOfBoundsException - Response probably badly corrupted");
                 exception.printStackTrace();
                 // We don't have any Context, so can't return an ErrorResponse
                 responseMessage = null;
                 }

             catch (IndexOutOfBoundsException exception)
                 {
                 LOGGER.error(SOURCE + "IndexOutOfBoundsException [exception=" + exception.getMessage() + "]");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          bytes,
                                                          SOURCE + "IndexOutOfBoundsException - Response probably badly corrupted");
                 exception.printStackTrace();
                 // We don't have any Context, so can't return an ErrorResponse
                 responseMessage = null;
                 }

             catch (ArrayStoreException exception)
                 {
                 LOGGER.error(SOURCE + "ArrayStoreException [exception=" + exception.getMessage() + "]");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          bytes,
                                                          SOURCE + "ArrayStoreException");
                 exception.printStackTrace();
                 // We don't have any Context, so can't return an ErrorResponse
                 responseMessage = null;
                 }

             catch (NullPointerException exception)
                 {
                 LOGGER.error(SOURCE + "NullPointerException [exception=" + exception.getMessage() + "]");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          bytes,
                                                          SOURCE + "NullPointerException");
                 exception.printStackTrace();
                 // We don't have any Context, so can't return an ErrorResponse
                 responseMessage = null;
                 }

             catch (FrameworkException exception)
                 {
                 LOGGER.error(SOURCE + "FrameworkException [exception=" + exception.getMessage() + "]");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          bytes,
                                                          SOURCE + "FrameworkException");
                 exception.printStackTrace();
                 // We don't have any Context, so can't return an ErrorResponse
                 responseMessage = null;
                 }

             catch (Exception exception)
                 {
                 LOGGER.error(SOURCE + "Generic Exception [exception=" + exception.getMessage() + "]");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          bytes,
                                                          SOURCE + "Unexpected Generic Exception");
                 exception.printStackTrace();
                 // We don't have any Context, so can't return an ErrorResponse
                 responseMessage = null;
                 }
             }
         else
             {
             LOGGER.error(SOURCE + "Response is too short to parse");
             DAOCommandHelper.logResponseMessageBytes(daos,
                                                      bytes,
                                                      SOURCE + "Response is too short to parse into a valid message");
             // We don't have any Context, so can't return an ErrorResponse
             responseMessage = null;
             }

         return (responseMessage);
         }


     /***********************************************************************************************
      * Discover the context of the Response, and create a ResponseMessage.
      * All parameters have been validated.
      * Return NULL if we can't locate the context.
      * ResponseValue may be empty for malformed ResponseMessages.
      *
      * @param commandmessage
      * @param daos
      * @param messagebytes
      * @param sourceaddress
      * @param commandcodebase
      * @param commandcode
      * @param commandvariant
      * @param status
      * @param responsevaluebytes
      * @param checksum
      * @param debugmode
      *
      * @return ResponseMessageInterface
      */

     private synchronized static ResponseMessageInterface locateResponseContext(final CommandMessageInterface commandmessage,
                                                                                final List<ObservatoryInstrumentDAOInterface> daos,
                                                                                final byte[] messagebytes,
                                                                                final int sourceaddress,
                                                                                final int commandcodebase,
                                                                                final int commandcode,
                                                                                final int commandvariant,
                                                                                final int status,
                                                                                final byte[] responsevaluebytes,
                                                                                final int checksum,
                                                                                final boolean debugmode)
         {
         final String SOURCE = "StarinetResponseMessage.locateResponseContext() ";
         final CommandType commandType;
         ResponseMessageInterface responseMessage;
         final int intStatus;

         // This is similar to StaribusResponseMessage.locateResponseContext()

         responseMessage = null;

         // Preserve the incoming status, and add other bits as required
         intStatus = status;

         if ((commandmessage != null)
             && (commandmessage.getInstrument() != null)
             && (ObservatoryInstrumentHelper.isEthernetController(commandmessage.getInstrument()))
             && (commandmessage.getInstrument().getControllable())
             && (commandmessage.getCommandType() != null))
             {
             commandType = commandmessage.getCommandType();
             }
         else
             {
             commandType = null;
             }

         // REMOVE this old check!
//         if (sourceaddress != 0)
//             {
//             LOGGER.error(SOURCE + "Controller StarinetAddress is not zero in message");
//             }

         //-----------------------------------------------------------------------------------------
         // We now know how to parse the Response byte array into a valid ResponseValue (String),
         // since we know the Instrument, Module and CommandType, which defines the Response
         // The following block either returns a valid ResponseMessage, an ErrorResponse, or NULL
         //--------------------------------------------------------------------------------------

         if (commandType != null)
             {
             final int intParsingFailures;

             // See if a Response is expected for this Command (as opposed to an Ack)
             // If not, we can respond immediately
             if (commandType.getResponse() != null)
                 {
                 // We expect a ResponseValue, but does ResponseValue contain any bytes to parse?
                 // It might not, if the ResponseMessage was malformed
                 // or if it is an ErrorResponse, where we can only use the Status
                 if ((responsevaluebytes != null)
                     && (responsevaluebytes.length > 0))
                     {
                     final DataTypeDictionary dataType;
                     final List<String> errors;

                     // Get the Response DataType to use for parsing
                     dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(commandType.getResponse().getDataTypeName().toString());
                     errors = new ArrayList<String>(10);

                     // Prepare to tell the user that something went wrong (details in the Status field)
                     // This will be overwritten if parsing succeeds
                     commandType.getResponse().setValue(ResponseMessageStatus.RESPONSE_NODATA);

                     intParsingFailures = DataTypeHelper.parseResponseValueFromByteArray(responsevaluebytes,
                                                                                         dataType,
                                                                                         commandType.getResponse(),
                                                                                         errors);
                     LOGGER.debugStarinetEvent(debugmode,
                                               SOURCE + "Parsed ResponseValue [ResponseValue="
                                                   + commandType.getResponse().getValue() + "] [status="
                                                   + Utilities.intToBitString(intStatus)
                                                   + "] [errors=" + intParsingFailures + "]");
                     LOGGER.errors(SOURCE, errors);
                     }
                 else
                     {
                     // A Response is expected for this Command,
                     // but the responsevaluebytes is null or zero length
                     // therefore it is an ErrorResponse (or something very corrupted)

                     // Tell the user that something went wrong (details in the unmodified Status field)
                     // We can be sure that commandType.getResponse() is not NULL
                     commandType.getResponse().setValue(ResponseMessageStatus.RESPONSE_NODATA);

                     // Indicate that this message was ok, so we can use the Status field
                     intParsingFailures = 0;

                     LOGGER.error(SOURCE + "A ResponseValue was expected, but was missing, so assume an ErrorResponse [status="
                                     + Utilities.intToBitString(intStatus) + "]");
                     }
                 }
             else
                 {
                 // No parsing errors are possible, since there's nothing to parse
                 // This is a valid Command, so allow a valid Response
                 intParsingFailures = 0;

                 LOGGER.debugStarinetEvent(debugmode,
                                           SOURCE + "No ResponseValue expected for this Command");
                 }

             //--------------------------------------------------------------------------------------
             // Double check that the ResponseValue (if any) parsed correctly
             // The Status may show an error, but the parsing must be correct
             // or at least simulated correct, in order to access the Status.

             if (intParsingFailures == 0)
                 {
                 final TimeZone timeZone;
                 final Locale locale;
                 final ObservatoryInstrumentDAOInterface dao;

                 // Find the DAO responsible for this Command and Response
                 dao = DAOHelper.findOriginatingDAO(daos, commandType);

                 LOGGER.debugStarinetEvent(debugmode,
                                           SOURCE + "Parsed correctly [status=" + Utilities.intToBitString(intStatus) + "]");

                 // Build messages only from the sourceaddress
                 responseMessage = new StarinetResponseMessage(dao.getPort().getName(),
                                                               commandmessage.getInstrument(),
                                                               commandmessage.getModule(),
                                                               commandType,  // Contains new Value (or not)
                                                               sourceaddress,
                                                               buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                        commandmessage.getModule(),
                                                                                        commandType),
                                                               intStatus);

                 // Tag the message with the time at which it was received
                 timeZone = REGISTRY.getFrameworkTimeZone();
                 locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                     REGISTRY.getFramework().getCountryISOCode());
                 responseMessage.setRxCalendar(dao.getObservatoryClock().getSystemCalendar(timeZone, locale));

                 LOGGER.debugStarinetEvent(debugmode,
                                           SOURCE + "ResponseMessage context successfully located");
                 }
             else
                 {
                 // We couldn't parse anything, but we do have a valid Context,
                 // therefore we can return an ErrorResponse
                 LOGGER.error(SOURCE + "Failed to parse Response, returning ErrorResponse");
                 DAOCommandHelper.logResponseMessageBytes(daos,
                                                          messagebytes,
                                                          SOURCE + "Failed to parse Response, returning ErrorResponse");

                 responseMessage = ResponseMessageHelper.constructErrorResponse(DAOHelper.findOriginatingDAO(daos, commandType),
                                                                                commandmessage.getInstrument(),
                                                                                commandmessage.getModule(),
                                                                                commandType,
                                                                                intStatus);
                 }
             }
         else
             {
             LOGGER.error(SOURCE + "Discovery of Instrument:Module:Command failed, TIMEOUT is inevitable");
             DAOCommandHelper.logResponseMessageBytes(daos,
                                                      messagebytes,
                                                      SOURCE + "Discovery of Instrument:Module:Command failed, TIMEOUT is inevitable");

             // If there is no Context, then we can't return an ErrorResponse, so NULL is the *only* option
             responseMessage = null;
             }

         return (responseMessage);
         }


    /***********************************************************************************************
     * Build the byte array which represents the Response of the supplied context as Starinet.
     * All parameters are assumed to have been validated.
     *
     * Returns a synchronized (thread-safe) list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  List list = Collections.synchronizedList(new ArrayList());
     *      ...
     *  synchronized(list) {
     *      Iterator i = list.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * @param instrumentxml
     * @param module
     * @param commandtype
     * @param sourceaddress
     * @param status
     * @param debugmode
     *
     * @return List<Byte>
     */

    private synchronized static List<Byte> buildStarinetByteArray(final Instrument instrumentxml,
                                                                  final XmlObject module,
                                                                  final CommandType commandtype,
                                                                  final int sourceaddress,
                                                                  final int status,
                                                                  final boolean debugmode)
        {
        final String SOURCE = "StarinetResponseMessage.buildStarinetByteArray() ";
        final List<Byte> message;
        final String strCommandCodeBase;
        final String strCommandCode;
        final String strCommandVariant;
        int intChecksum;
        final ResponseType responseType;
        final StringBuffer buffer;

        message = Collections.synchronizedList(new ArrayList<Byte>(100));
        intChecksum = 0;
        buffer = new StringBuffer();

        // Start a new Message - this is not included in the checksum
        message.add(STARIBUS_START);

        // The address '000' is reserved for use by Virtual Controllers implemented in Java on the host
        // So we use it here instead of the IPAddress
        // We know that it must be a valid Real Controller to arrive here...

        // Add the Controller Address to which the message should be sent, as HEX
        // Return the new checksum each time
        intChecksum = Utilities.addStringToMessage(message,
                                                   Utilities.intToTwoHexString(sourceaddress),
                                                   intChecksum);
        // Add the CommandCode
        // This is made up from the CommandCodeBase of the Controller or Plugin,
        // and the CommandCode and CommandVariant of the CommandType

        // Are we dealing with a Controller or a Module?
        // Note that the CommandCodeBase and CommandCode are constrained to be two characters
        // but CommandVariant is four characters
        if (module instanceof Controller)
            {
            strCommandCodeBase = ((Controller)module).getCommandCodeBase();
            strCommandCode = commandtype.getCommandCode();
            strCommandVariant = commandtype.getCommandVariant();
            }
        else if (module instanceof PluginType)
            {
            strCommandCodeBase = ((PluginType)module).getCommandCodeBase();
            strCommandCode = commandtype.getCommandCode();
            strCommandVariant = commandtype.getCommandVariant();
            }
        else
            {
            // This should never occur!
            // Just replace with ping()
            strCommandCodeBase = "00";
            strCommandCode = "00";
            strCommandVariant = "0000";
            }

        // See which protocol we are using...
        if (LOADER_PROPERTIES.isCommandVariant())
            {
            buffer.append(strCommandCodeBase);
            buffer.append(strCommandCode);
            buffer.append(strCommandVariant);
            }
        else
            {
            buffer.append(strCommandCodeBase);
            buffer.append(strCommandCode);
            }

        // Return the new checksum each time
        intChecksum = Utilities.addStringToMessage(message,
                                                   buffer.toString(),
                                                   intChecksum);
        // Now add the status code
        intChecksum = Utilities.addStringToMessage(message,
                                                   Utilities.intToFourHexString(status),
                                                   intChecksum);

        // See which kind of Response is required for the supplied Command
        responseType = commandtype.getResponse();

        // Was there a Response required?
        // If it was just an Ack, no action is to be taken, since there is no data payload
        if (responseType != null)
            {
            // The ResponseValue US separator
            intChecksum = Utilities.addDelimiterToMessage(message, intChecksum);

            // Now the ResponseValue
            // The ResponseValue is just a String, to be added character by character
            intChecksum = Utilities.addStringToMessage(message,
                                                       responseType.getValue(),
                                                       intChecksum);
            // And the ResponseValue US terminator
            intChecksum = Utilities.addDelimiterToMessage(message, intChecksum);
            }

        // Calculate the CRC checksum and add it as four uppercase ASCII characters,
        // remembering not to use the STX at the start of the message!
        Utilities.addCrcToMessage(message, intChecksum);

        // Finally, terminate the message - this is not included in the checksum
        message.add(STARIBUS_TERMINATOR_0);
        message.add(STARIBUS_TERMINATOR_1);
        message.add(STARIBUS_TERMINATOR_2);

        LOGGER.debugStarinetEvent(debugmode,
                                  SOURCE + "HEX   [" + Utilities.byteArrayToSpacedHex(Utilities.byteListToArray(message)) + "]");
        LOGGER.debugStarinetEvent(debugmode,
                                  SOURCE + "ASCII [" + Utilities.byteArrayToExpandedAscii(Utilities.byteListToArray(message)) + "]");

        return (message);
        }


    /***********************************************************************************************
     * Construct a StarinetResponseMessage.
     * Save the original StarScript representation of this Command,
     * and generate the appropriate bytes (which should be) received on the bus.
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param sourceaddress
     * @param starscript
     * @param status
     */

    public StarinetResponseMessage(final String portname,
                                   final Instrument instrumentxml,
                                   final XmlObject module,
                                   final CommandType command,
                                   final int sourceaddress,
                                   final String starscript,
                                   final int status)
        {
        final String SOURCE = "StarinetResponseMessage ";

        if ((instrumentxml != null)
            && (module != null)
            && (command != null)
            && (starscript != null)
            && (!EMPTY_STRING.equals(starscript.trim()))
            && (ObservatoryInstrumentHelper.isEthernetController(instrumentxml))
            && (instrumentxml.getControllable()))
            {
            this.strSource = portname;
            this.daoWrapper = null;
            this.selectedInstrument = instrumentxml;
            this.selectedModule = module;

            // Don't affect the CommandType of the incoming Command,
            // because this is where the ResponseValue will go...
            this.selectedCommand = (CommandType)command.copy();
            this.intStatus = status;

            // Map the status bits into a List of Enums for easier use
            this.listResponseMessageStatus = mapResponseStatusBits(intStatus);

            // Build the byte array corresponding to this context
            if (command.getSendToPort())
                {
                // Look for messages only from the sourceaddress
                this.listBytes = buildStarinetByteArray(selectedInstrument,
                                                        selectedModule,
                                                        selectedCommand,
                                                        sourceaddress,
                                                        intStatus,
                                                        LOADER_PROPERTIES.isStarinetDebug());
                }
            else
                {
                this.listBytes = new ArrayList<Byte>(1);
                }

            this.strStarScript = starscript;

            // The calendar is set when the Message is passed to the queue (not part of the message)
            this.calRx = null;
            }
        else
            {
            throw new FrameworkException(SOURCE + EXCEPTION_PARAMETER_INVALID);
            }
        }
    }
