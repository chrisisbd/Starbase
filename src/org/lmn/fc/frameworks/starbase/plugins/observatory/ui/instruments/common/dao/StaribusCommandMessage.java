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
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractCommandMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.instruments.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//**************************************************************************************************
//    Request with no parameters, length 15 before EOT
//
//    Header          STX
//    Address         char char  in Hex
//    CommandCode     char char char char
//    CommandVariant  char char char char
//    CrcChecksum     char char char char  in Hex
//    Terminator      EOT CR LF
//
//**************************************************************************************************
//    Request with Parameters
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
 * StaribusCommandMessage.
 */

public final class StaribusCommandMessage extends AbstractCommandMessage
    {
    /***********************************************************************************************
     * Build the message into an array of Bytes.
     * All parameters have been validated.
     *
     * @param instrumentxml
     * @param module
     * @param command
     * @param debugmode
     *
     * @return List<Byte>
     */

    private synchronized static List<Byte> buildStaribusByteArray(final Instrument instrumentxml,
                                                                  final XmlObject module,
                                                                  final CommandType command,
                                                                  final boolean debugmode)
        {
        final String SOURCE = "StaribusCommandMessage.buildStaribusByteArray() ";
        final List<Byte> message;
        final byte byteAddress;
        int intChecksum;
        final String strCommandCodeBase;
        final String strCommandCode;
        final String strCommandVariant;
        final StringBuffer buffer;

        message = Collections.synchronizedList(new ArrayList<Byte>(100));
        intChecksum = 0;
        buffer = new StringBuffer();

        // Start a new Message - this is not included in the checksum
        message.add(STARIBUS_START);

        // The RS485 bus address of the Controller
        // The address '000' is reserved for use by Virtual Controllers implemented in Java on the host
        // and so should not be used here
        // We know that it must be a valid Real Controller to arrive here...
        byteAddress = (byte)ObservatoryInstrumentHelper.getStaribusAddressAsInteger(instrumentxml);

        // Add the Controller Address to which the message should be sent, as HEX
        // Return the new checksum each time
        intChecksum = Utilities.addStringToMessage(message,
                                                   Utilities.byteToTwoHexString(byteAddress),
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
            strCommandCode = command.getCommandCode();
            strCommandVariant = command.getCommandVariant();
            }
        else if (module instanceof PluginType)
            {
            strCommandCodeBase = ((PluginType)module).getCommandCodeBase();
            strCommandCode = command.getCommandCode();
            strCommandVariant = command.getCommandVariant();
            }
        else
            {
            // This should never occur!
            // Just replace with ping()
            strCommandCodeBase = "00";
            strCommandCode = "00";
            strCommandVariant = "0000";
            }

        // There should never be any LegacyCode with Staribus
        if (command.getLegacyCode() != null)
            {
            LOGGER.error(SOURCE + "Command unexpectedly contained LegacyCode (ignored)");
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

        // Process all Command Parameters, if any
        if ((command.getParameterList() != null)
            && (!command.getParameterList().isEmpty()))
            {
            final List<ParameterType> listParameters;
            final Iterator<ParameterType> iterParameters;

            // Parameters are always preceded by a delimiter
            intChecksum = Utilities.addDelimiterToMessage(message, intChecksum);

            listParameters = command.getParameterList();
            iterParameters = listParameters.iterator();

            while (iterParameters.hasNext())
                {
                final ParameterType parameter;

                parameter = iterParameters.next();

                // Add the value of each Parameter as a series of ASCII characters
                if ((parameter.getValue() != null)
                    && (parameter.getTrafficDataType() != null)
                    && (parameter.getTrafficDataType().getDataTypeName() != null))
                    {
                    final DataTypeDictionary type;
                    final String strParameter;

                    // Get the Traffic DataType to use for transmission
                    type = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

                    // Remember that parameterValueToMessageString() maps some DataTypes, e.g. Boolean --> 'Y' or 'N'
                    strParameter = parameterValueToMessageString(parameter);

                    if (strParameter != null)
                        {
                        LOGGER.debugStaribusEvent(debugmode,
                                                  SOURCE + "[parameter="
                                                    + strParameter
                                                    + "] [datatype="
                                                    + type.getName()
                                                    + "]");

                        // Return the new checksum each time
                        intChecksum = Utilities.addStringToMessage(message,
                                                                   strParameter,
                                                                   intChecksum);

                        // Each Parameter is terminated with a UnitSeparator (US)
                        // even if the Parameter was empty by design or by error
                        intChecksum = Utilities.addDelimiterToMessage(message, intChecksum);
                        }
                    else
                        {
                        // Something is wrong with the mapping of the ParameterValue
                        LOGGER.error(SOURCE + "Invalid ParameterValue");
                        }
                    }
                else
                    {
                    // Something is wrong with the data we have been given
                    LOGGER.error(SOURCE + "Invalid Parameter");
                    }
                }
            }

        // Calculate the CRC checksum and add it as four uppercase ASCII characters
        Utilities.addCrcToMessage(message, intChecksum);

        // Finally, terminate the message - this is not included in the checksum
        message.add(STARIBUS_TERMINATOR_0);
        message.add(STARIBUS_TERMINATOR_1);
        message.add(STARIBUS_TERMINATOR_2);

        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + "HEX   [" + Utilities.byteArrayToSpacedHex(Utilities.byteListToArray(message)) + "]");
        LOGGER.debugStaribusEvent(debugmode,
                                  SOURCE + "ASCII [" + Utilities.byteArrayToExpandedAscii(Utilities.byteListToArray(message)) + "]");

        return (message);
        }


    /***********************************************************************************************
     * Convert the specified ParameterValue to a String representation for the CommandMessage.
     * The parameter is assumed to be valid.
     *
     * @param parameter
     *
     * @return String
     */

    public synchronized static String parameterValueToMessageString(final ParameterType parameter)
        {
        final String SOURCE = "StaribusCommandMessage.parameterValueToMessageString() ";
        final DataTypeDictionary inputDataType;
        final DataTypeDictionary trafficDataType;
        final int inputRadix;
        final int trafficRadix;
        final int trafficFieldCount;
        final DataTypeDictionary dataType;
        final String strStarScriptValue;
        final String strMessageParameter;

        // This takes the String value currently in ParameterValue, which has already been parsed
        // as a valid parameter, and constructs a suitable String for adding into the CommandMessage
        // Most parameters do not require any modification

        // Find the XmlBeans DataType specific to this Parameter's InputDataType
        inputDataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString());

        // Find the Radix specific to the InputDataType (defaults to 10)
        inputRadix = inputDataType.getRadix();

        // Find the XmlBeans DataType specific to this Parameter's TrafficDataType
        trafficDataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

        // Find the Radix specific to the TrafficDataType (defaults to 10)
        trafficRadix = trafficDataType.getRadix();

        // Find the FieldCount specific to this TrafficDataType, for transmission
        trafficFieldCount = (int)parameter.getTrafficDataType().getFieldCount();

        // Find the compile-time DataTypeDictionary which corresponds to the run-time TrafficDataType
        // This is required for switch()
        dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

        // Get the (validated) ParameterValue to encode
        strStarScriptValue = parameter.getValue();

        // Remember to keep these in step if the XSD changes, and keep in step with DataTypeDictionary!
        // This is switching on the compiler version of the TrafficDataType specified in the Parameter
        switch (dataType)
            {
            // Staribus Decimal bytes are always three digits 000...255
            case UNSIGNED_DECIMAL_BYTE:
                {
                if (strStarScriptValue.length() == 0)
                    {
                    // Something went very wrong...
                    strMessageParameter = "000";
                    }
                else if (strStarScriptValue.length() == 1)
                    {
                    strMessageParameter = "00" + strStarScriptValue;
                    }
                else if (strStarScriptValue.length() == 2)
                    {
                    strMessageParameter = "0" + strStarScriptValue;
                    }
                else
                    {
                    strMessageParameter = strStarScriptValue.substring(0, 3);
                    }

                break;
                }

            // Staribus Decimal integers are always four digits 0000...9999
            case DECIMAL_INTEGER:
                {
                if (strStarScriptValue.length() == 0)
                    {
                    // Something went very wrong...
                    strMessageParameter = "0000";
                    }
                else if (strStarScriptValue.length() == 1)
                    {
                    strMessageParameter = "000" + strStarScriptValue;
                    }
                else if (strStarScriptValue.length() == 2)
                    {
                    strMessageParameter = "00" + strStarScriptValue;
                    }
                else if (strStarScriptValue.length() == 3)
                    {
                    strMessageParameter = "0" + strStarScriptValue;
                    }
                else
                    {
                    strMessageParameter = strStarScriptValue.substring(0, 4);
                    }

                break;
                }

            case BOOLEAN:
                {
                // Convert the stored Boolean to a shorter form for transmission
                if (Boolean.TRUE.toString().equals(strStarScriptValue))
                    {
                    strMessageParameter = "Y";
                    }
                else
                    {
                    strMessageParameter = "N";
                    }
                break;
                }

            // Used for parameterised reset(flag)
            case NUMERIC_INDEXED_LIST:
                {
                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          SOURCE + "Process NUMERIC_INDEXED_LIST");

                // Assign an index {0...n} to an item from a list of Choices.
                // Return the index String padded to fieldcount with leading zeroes, or null if an error occurs.
                strMessageParameter = Utilities.assignIndexToChoiceItem(parameter,
                                                                        trafficRadix,
                                                                        trafficFieldCount);

                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          SOURCE + "MessageParameter=" + strMessageParameter);
                break;
                }

            default:
                {
                strMessageParameter = strStarScriptValue;
                }
            }

        return (strMessageParameter);
        }


    /***********************************************************************************************
     * Construct a StaribusCommandMessage.
     * Save the original StarScript representation of this Command,
     * and generate the appropriate bytes to transmit on the bus.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     */

    public StaribusCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                  final Instrument instrumentxml,
                                  final XmlObject module,
                                  final CommandType command,
                                  final String starscript)
        {
        final String SOURCE = "StaribusCommandMessage ";

        if ((dao != null)
            && (instrumentxml != null)
            && (module != null)
            && (command != null)
            && (starscript != null)
            && (!EMPTY_STRING.equals(starscript.trim()))
            && (ObservatoryInstrumentHelper.isStaribusController(instrumentxml))
            && (instrumentxml.getControllable()))
            {
            this.daoSource = dao;
            this.selectedInstrument = instrumentxml;
            this.selectedModule = module;
            this.selectedCommand = command;
            this.streamTarget = StreamType.STARIBUS;

            // Build the byte array corresponding to this context
            if (command.getSendToPort())
                {
                this.listBytes = buildStaribusByteArray(selectedInstrument,
                                                        selectedModule,
                                                        selectedCommand,
                                                        LOADER_PROPERTIES.isStaribusDebug());
                }
            else
                {
                this.listBytes = new ArrayList<Byte>(1);
                }

            this.strStarScript = starscript;

            // The calendar is set when the Message is passed to the queue (not part of the message)
            this.calTx = null;
            }
        else
            {
            throw new FrameworkException(SOURCE + EXCEPTION_PARAMETER_INVALID);
            }
        }
    }