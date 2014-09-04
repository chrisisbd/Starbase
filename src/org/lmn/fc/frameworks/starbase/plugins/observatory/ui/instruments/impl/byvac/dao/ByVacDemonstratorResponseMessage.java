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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.byvac.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.*;


/***************************************************************************************************
 * ByVacDemonstratorResponseMessage.
 */

public final class ByVacDemonstratorResponseMessage extends AbstractResponseMessage
    {
    public static final int LENGTH_VALID_RESPONSE = 5;


    /***********************************************************************************************
     * Parse a byte array, attempting to find a ByVacDemonstratorResponseMessage
     * which can be interpreted as coming from a ByVacDemonstrator Instrument.
     *      reset()
     *      getRadiometerOutput()
     *      getSpectrometerOutput()
     *
     * @param commandmessage
     * @param daos
     * @param bytes
     *
     * @return ResponseMessageInterface
     */

    public synchronized static ResponseMessageInterface parseBytes(final CommandMessageInterface commandmessage,
                                                                   final List<ObservatoryInstrumentDAOInterface> daos,
                                                                   final byte[] bytes)
        {
        final String SOURCE = "ByVacDemonstratorResponseMessage.parseBytes() ";
        ResponseMessageInterface responseMessage;

//        LOGGER.debugProtocolEvent("ByVacDemonstratorResponseMessage.parseStaribusBytes() [" + Utilities.byteArrayToSpacedHex(bytes) + "]");
        responseMessage = null;

        // We should know how to parse the Response byte array into a valid ResponseValue (String),
        // since we know the CommandType, which defines the Response DataType
        // Any shorter than LENGTH_RESPONSE, and it is not worth parsing
        // since all ByVacDemonstrator responses are four characters plus <cr>
        // If no CommandMessage was provided, it is because the TxStream did not inject it during write()
        // so for some reason we can't return a valid ResponseMessage

        // This is similar to StaribusResponseMessage.locateResponseContext()

        if ((commandmessage != null)
            && (commandmessage.getCommandType() != null)
            && (daos != null)
            && (daos.size() > 0)
            && (daos.get(0) != null)
            && (daos.get(0).getPort() != null))
            {
            final CommandType commandType;
            final int intParsingFailures;
            int intStatus;

            // Use the CommandType which was supplied by the TxStream
            // Don't affect the CommandType of the incoming Command
            commandType = (CommandType)commandmessage.getCommandType().copy();

            intStatus = ResponseMessageStatus.SUCCESS.getBitMask();

            // See if a Response is expected for this Command (as opposed to an Ack)
            // If not, we can respond immediately
            if ((commandType != null)
                && (commandType.getResponse() != null))
                {
                // We expect a ResponseValue plus <cr>, but does are there any bytes to parse?
                // It might not, if the ResponseMessage was malformed
                if ((bytes != null)
                    && (bytes.length >= LENGTH_VALID_RESPONSE))
                    {
                    final DataTypeDictionary dataType;
                    final List<String> errors;
                    final byte[] bytesToParse;

                    // Ignore the final <cr> during parsing
                    bytesToParse = new byte[LENGTH_VALID_RESPONSE-1];

                    // Get the Response DataType to use for parsing
                    dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(commandType.getResponse().getDataTypeName().toString());
                    errors = new ArrayList<String>(10);

                    // Here is where we cheat with prior knowledge of the ByVacDemonstrator data format...
                    // The Radiometer or Spectrometer Response is D followed by three Hex digits representing 0...10V
                    // So we can cheat by calling it a HexInteger and replacing the leading 'D' with '0'
                    // since HexIntegers are four digits!
                    if (SchemaDataType.HEX_INTEGER.equals(commandType.getResponse().getDataTypeName()))
                        {
//                        LOGGER.debugProtocolEvent("ByVacDemonstratorResponseMessage.parseStaribusBytes() Received HexInteger [bytes="
//                                                    + Utilities.byteArrayToSpacedHex(bytes) + "]");

                        // Replace the leading 'D' with a zero! (If it wasn't a 'D', it will fail anyway)
                        bytesToParse[0] = 0x30;

                        // Don't copy the initial 'D' or the <cr>
                        System.arraycopy(bytes,
                                         1,
                                         bytesToParse,
                                         1,
                                         LENGTH_VALID_RESPONSE-2);

//                        LOGGER.debugProtocolEvent("ByVacDemonstratorResponseMessage.parseStaribusBytes() Revised bytes to parse [bytes="
//                                                    + Utilities.byteArrayToSpacedHex(bytesToParse) + "]");

                        intParsingFailures = DataTypeHelper.parseResponseValueFromByteArray(bytesToParse,
                                                                                            dataType,
                                                                                            commandType.getResponse(),
                                                                                            errors);
                        }
                    else
                        {
                        // All other DataTypes remain unchanged
                        // Ignore the <cr> during parsing
                        System.arraycopy(bytes,
                                         0,
                                         bytesToParse,
                                         0,
                                         LENGTH_VALID_RESPONSE-1);
                        intParsingFailures = DataTypeHelper.parseResponseValueFromByteArray(bytesToParse,
                                                                                            dataType,
                                                                                            commandType.getResponse(),
                                                                                            errors);
                        }

//                    LOGGER.debugProtocolEvent("ByVacDemonstratorResponseMessage.parseStaribusBytes() [ResponseValue="
//                                                + commandType.getResponse().getValue() + "] [errors=" + intParsingFailures + "]");
                    LOGGER.errors(SOURCE, errors);
                    }
                else
                    {
                    // Modify the Status word to show INVALID_MESSAGE (as well as whatever else it had)
                    intStatus |= ResponseMessageStatus.INVALID_MESSAGE.getBitMask();

                    // Indicate that we can't parse this message
                    intParsingFailures = 1;

                    LOGGER.error("ByVacDemonstratorResponseMessage.parseStaribusBytes() ResponseMessage appears malformed - a ResponseValue was expected, but was missing [status="
                                    + Utilities.intToBitString(intStatus) + "]");
                    }
                }
            else
                {
                // No parsing errors are possible, since there's nothing to parse
                intParsingFailures = 0;

//                LOGGER.debugProtocolEvent("ByVacDemonstratorResponseMessage.parseStaribusBytes() No ResponseValue expected for this Command");
                }

            //--------------------------------------------------------------------------------------
            // Double check that the ResponseValue (if any) parsed correctly
            // The Status may show an error, but the parsing must be correct

            if (intParsingFailures == 0)
                {
                final TimeZone timeZone;
                final Locale locale;
                final ObservatoryInstrumentDAOInterface dao;

                // TODO We must find the DAO responsible for this Command and Response
                //System.out.println("DAO LIST [size=" + daos.size() + "]");
                dao = daos.get(0);

                responseMessage = new ByVacDemonstratorResponseMessage(dao.getPort().getName(),
                                                                       commandmessage.getInstrument(),
                                                                       commandmessage.getModule(),
                                                                       commandType,  // Contains new Value (or not)
                                                                       buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                commandmessage.getModule(),
                                                                                                commandType),
                                                                       intStatus);

                // Tag the message with the time at which it was received
                timeZone = REGISTRY.getFrameworkTimeZone();
                locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                    REGISTRY.getFramework().getCountryISOCode());
                responseMessage.setRxCalendar(dao.getObservatoryClock().getSystemCalendar(timeZone, locale));

//                LOGGER.debugProtocolEvent("ByVacDemonstratorResponseMessage.parseStaribusBytes() ResponseMessage successfully created");
                }
            else
                {
                // We couldn't parse anything, so can't provide a message to carry any Status
                LOGGER.error("ByVacDemonstratorResponseMessage.parseStaribusBytes() Failed to parse Response");
                }
            }
        else
            {
            // We couldn't parse anything, so can't provide a message to carry any Status
            LOGGER.error("ByVacDemonstratorResponseMessage.parseStaribusBytes() Invalid DAO or context");
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * Build the byte array which represents the Response of the supplied context as ByVacDemonstrator.
     * All parameters are assumed to have been validated.
     *
     * @param instrumentxml
     * @param module
     * @param commandtype
     * @param status
     *
     * @return List<Byte>
     */

    private synchronized static List<Byte> buildByteArray(final Instrument instrumentxml,
                                                          final XmlObject module,
                                                          final CommandType commandtype,
                                                          final int status)
        {
        final List<Byte> message;
        int intChecksum;
        final ResponseType responseType;

        message = Collections.synchronizedList(new ArrayList<Byte>(100));
        intChecksum = 0;

        // See which kind of Response is required for the supplied Command
        responseType = commandtype.getResponse();

        // Was there a Response required?
        // If it was just an Ack, no action is to be taken, since there is no data payload
        if (responseType != null)
            {
            // The Response List of bytes does not contain any context Instrument. Module,Command
            // A ByVacDemonstrator ResponseValue is just a String, to be added character by character
            intChecksum = Utilities.addStringToMessage(message,
                                                       responseType.getValue(),
                                                       intChecksum);
            }

        return (message);
        }


    /***********************************************************************************************
     * Construct a ByVacDemonstratorResponseMessage.
     * Save the original StarScript representation of this Command,
     * and generate the appropriate bytes (which should be) received on the bus.
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param status
     */

    public ByVacDemonstratorResponseMessage(final String portname,
                                            final Instrument instrumentxml,
                                            final XmlObject module,
                                            final CommandType command,
                                            final String starscript,
                                            final int status)
        {
        if ((portname != null)
            && (instrumentxml != null)
//            && (XmlBeansUtilities.isValidXml(instrumentxml))
            && (module != null)
//            && (XmlBeansUtilities.isValidXml(module))
            && (command != null)
//            && (XmlBeansUtilities.isValidXml(command))
            && (starscript != null)
            && (!EMPTY_STRING.equals(starscript.trim()))
            && (ObservatoryInstrumentHelper.isVirtualController(instrumentxml))
            && (instrumentxml.getControllable()))
            {
            this.strSource = portname;
            this.daoWrapper = null;
            this.selectedInstrument = instrumentxml;
            this.selectedModule = module;

            // Don't affect the CommandType of the incoming Command,
            // because this is where the ResponseValue will go...
            // This is the only way to hold unique ResponseValues for execution of the same Command
            this.selectedCommand = (CommandType)command.copy();
            this.intStatus = status;

            // Map the status bits into a List of Enums for easier use
            this.listResponseMessageStatus = mapResponseStatusBits(intStatus);

            // Build the byte array corresponding to this context
            if (command.getSendToPort())
                {
                this.listBytes = buildByteArray(selectedInstrument,
                                                selectedModule,
                                                selectedCommand,
                                                intStatus);
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
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Discover the context of the Response, and create a ResponseMessage.
     * All parameters have been validated.
     *
     * @param instrumentsdoc
     * @param dao
     * @param intAddress
     * @param intCommandCodeBase
     * @param intCommandCode
     * @param status
     * @param byteResponseValue
     * @param intChecksum
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface locateResponseContext(final InstrumentsDocument instrumentsdoc,
                                                          final ObservatoryInstrumentDAOInterface dao,
                                                          final int intAddress,
                                                          final int intCommandCodeBase,
                                                          final int intCommandCode,
                                                          final int status,
                                                          final byte[] byteResponseValue,
                                                          final int intChecksum)
        {
        return null;
        }
    }
