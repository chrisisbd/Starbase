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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.spectracyberclient.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractCommandMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/***************************************************************************************************
 * SpectraCyberClientCommandMessage.
 */

public final class SpectraCyberClientCommandMessage extends AbstractCommandMessage
    {
    /***********************************************************************************************
     * Build the message into an array of Bytes.
     * All parameters have been validated.
     *
     * @param instrumentxml
     * @param module
     * @param command
     *
     * @return List<Byte>
     */

    private synchronized static List<Byte> buildByteArray(final Instrument instrumentxml,
                                                          final XmlObject module,
                                                          final CommandType command)
        {
        final List<Byte> message;
        int intChecksum;

        //LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.buildByteArray()");

        message = Collections.synchronizedList(new ArrayList<Byte>(MAX_MESSAGE_SIZE));

        // The checksum is just so we can use the Utilities methods,
        // since the SpectraCyber protocol doesn't use checksums...
        intChecksum = 0;

        // First check to see if a Parameter is required (only one allowed)
        if ((command.getParameterList() != null)
            && (!command.getParameterList().isEmpty()))
            {
            // Add the LegacyCode as the command prefix
            if (command.getLegacyCode() != null)
                {
                intChecksum = Utilities.addStringToMessage(message,
                                                           command.getLegacyCode(),
                                                           intChecksum);

                //LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.buildByteArray() [legacy_message=" + Utilities.byteListToString(message) + "]");

                // All SpectraCyber bus Commands are monadic
                // There should be only one Parameter supplied
                if (command.getParameterList().size() == 1)
                    {
                    final ParameterType parameter;

                    // The Parameter must be at index zero
                    parameter = command.getParameterList().get(0);

                    // Check that we have information to transport this Parameter
                    if ((parameter.getValue() != null)
                        && (parameter.getTrafficDataType() != null)
                        && (parameter.getTrafficDataType().getDataTypeName() != null))
                        {
                        final String strParameter;

                        // Map the Parameter to a String appropriate to a SpectraCyberClientCommandMessage
                        // Applies to:
                        //      setNoiseSource()
                        //      setSpectrometerFrequency()
                        //      setRadiometerGain()
                        //      setRadiometerTimeConstant()
                        //      setSpectrometerGain()
                        //      setSpectrometerTimeConstant()
                        //      set70MHzGain()
                        //      setBandwidth()
                        //      setRadiometerOffset()
                        //      setSpectrometerOffset()

                        strParameter = parameterValueToMessageString(parameter);

                        if (strParameter != null)
                            {
                            intChecksum = Utilities.addStringToMessage(message,
                                                                       strParameter,
                                                                       intChecksum);

//                            LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.buildByteArray() [parameter="
//                                                        + strParameter
//                                                        + "] [datatype="
//                                                        + parameter.getTrafficDataType().getDataTypeName().toString()
//                                                        + "]");
                            }
                        else
                            {
                            // Something is wrong with the mapping of the ParameterValue
                            LOGGER.error("SpectraCyberClientCommandMessage.buildByteArray() Invalid ParameterValue");
                            }
                        }
                    else
                        {
                        // Something is wrong with the data we have been given
                        LOGGER.error("SpectraCyberClientCommandMessage.buildByteArray() Invalid Parameter");
                        }
                    }
                else
                    {
                    LOGGER.error("SpectraCyberClientCommandMessage.buildByteArray() Command has an incorrect number of Parameters");
                    }
                }
            else
                {
                LOGGER.error("SpectraCyberClientCommandMessage.buildByteArray() Command does not have a LegacyCode");
                }
            }
        else
            {
            // No Parameter required, use the entire LegacyCode directly as the Command string
            // This applies to:
            //      reset()
            //      getRadiometerOutput()
            //      getSpectrometerOutput()

            if (command.getLegacyCode() != null)
                {
                intChecksum = Utilities.addStringToMessage(message,
                                                           command.getLegacyCode(),
                                                           intChecksum);
                //LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.buildByteArray() [legacy_message=" + Utilities.byteListToString(message) + "]");
                }
            else
                {
                LOGGER.error("SpectraCyberClientCommandMessage.buildByteArray() Command does not have a LegacyCode");
                }
            }

        //LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.buildByteArray() [message=" + Utilities.byteListToString(message) + "]");

        return (message);
        }


    /***********************************************************************************************
     * Convert the specified ParameterValue to a String representation for the CommandMessage.
     * The parameter is assumed to be valid.
     * ToDO add Error list?
     *
     * @param parameter
     *
     * @return String
     */

    private synchronized static String parameterValueToMessageString(final ParameterType parameter)
        {
        final DataTypeDictionary inputDataType;
        final DataTypeDictionary trafficDataType;
        final int inputRadix;
        final int trafficRadix;
        final int trafficFieldCount;
        final DataTypeDictionary datatypeTraffic;
        final String strStarScriptValue;
        String strMessageString;

        // This takes the String value currently in ParameterValue, which has already been parsed
        // as a valid parameter, and constructs a suitable String for adding into the CommandMessage
        // Most parameters do not require any modification

        //------------------------------------------------------------------------------------------
        // InputDataType

        // Find the XmlBeans DataType specific to this Parameter's InputDataType
        inputDataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getInputDataType().getDataTypeName().toString());

        // Find the Radix specific to the InputDataType (defaults to 10)
        inputRadix = inputDataType.getRadix();

        //------------------------------------------------------------------------------------------
        // TrafficDataType

        // Find the XmlBeans DataType specific to this Parameter's TrafficDataType
        trafficDataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

        // Find the Radix specific to the TrafficDataType (defaults to 10)
        trafficRadix = trafficDataType.getRadix();

        // Find the FieldCount specific to this TrafficDataType, for transmission
        trafficFieldCount = (int)parameter.getTrafficDataType().getFieldCount();


        //------------------------------------------------------------------------------------------
        // Find the compile-time DataTypeDictionary which corresponds to the run-time TrafficDataType
        // This is required for switch()

        datatypeTraffic = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

        // Get the (validated) ParameterValue to encode
        strStarScriptValue = parameter.getValue();
        //LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.parameterValueToMessageString() [starscript=" + strStarScriptValue + "]");

        // Remember to keep these in step if the XSD changes, and keep in step with DataTypeDictionary!
        switch (datatypeTraffic)
            {
            //--------------------------------------------------------------------------------------
            // Used only for setNoiseSource()

            case BOOLEAN:
                {
                try
                    {
                    final boolean boolValue;
                    final int intValue;

                    // The boolean returned represents the value 'true' if the string argument is not null
                    // and is equal, ignoring case, to the string "true"
                    boolValue = Boolean.parseBoolean(parameter.getValue());

                    // Spell it out!
                    if (boolValue)
                        {
                        intValue = 1;
                        }
                    else
                        {
                        intValue = 0;
                        }

                    // Find out how many characters we need to send, and assume a radix of 2!
                    strMessageString = Utilities.intToString(intValue,
                                                             2,
                                                             trafficFieldCount);
                    }

                catch (NullPointerException exception)
                    {
                    // This should never happen, because the ParameterValue has already been parsed
                    strMessageString = null;
                    }

                break;
                }

            //--------------------------------------------------------------------------------------
            // Used only for setSpectrometerFrequency()
            // The SpectraCyber requires frequency -2000 is sent as 078 hex, +2000 is sent as 398 hex
            // so the values must be scaled and offset (yuk)

            case SCALED_HEX_INTEGER:
                {
                try
                    {
                    final int intInput;
                    final double dblInputMin;
                    final double dblInputMax;
                    final double dblOutputMin;
                    final double dblOutputMax;
                    double dblOutput;

                    // Parse the User's input using the INPUT DataType radix!
                    intInput = Integer.parseInt(strStarScriptValue, inputRadix);

                    // Parse the mapping ranges using the TRAFFIC DataType radix!
                    // ToDo review double?
                    dblInputMin = Integer.parseInt(parameter.getTrafficDataType().getInputMin(), trafficRadix);
                    dblInputMax = Integer.parseInt(parameter.getTrafficDataType().getInputMax(), trafficRadix);
                    dblOutputMin = Integer.parseInt(parameter.getTrafficDataType().getOutputMin(), trafficRadix);
                    dblOutputMax = Integer.parseInt(parameter.getTrafficDataType().getOutputMax(), trafficRadix);

//                    System.out.println("intInput=" + intInput);
//                    System.out.println("dblInputMin=" + dblInputMin);
//                    System.out.println("dblInputMax=" + dblInputMax);
//                    System.out.println("dblOutputMin=" + dblOutputMin);
//                    System.out.println("dblOutputMax=" + dblOutputMax);

                    if ((dblOutputMax > dblOutputMin)
                        && (dblInputMax > dblInputMin))
                        {
                        // Map the Input to the Output
                        dblOutput =  (dblOutputMax - dblOutputMin) / (dblInputMax - dblInputMin);
                        dblOutput = dblOutput * ((double)intInput - dblInputMin);
                        dblOutput = dblOutput + dblOutputMin;

//                        System.out.println("dblOutput=" + dblOutput);

                        // Find out how many characters we need to send, and in what radix
                        strMessageString = Utilities.intToString((int)dblOutput,
                                                                 trafficRadix,
                                                                 trafficFieldCount);
                        }
                    else
                        {
                        // The ScaledInteger was not configured correctly
                        strMessageString = null;
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    // This should never happen, because the ParameterValue has already been parsed,
                    // but the Input and Output ranges may be incorrect
                    strMessageString = null;
                    }

                break;
                }

            //--------------------------------------------------------------------------------------
            // Used for:
            //      setRadiometerGain()
            //      setRadiometerTimeConstant()
            //      setSpectrometerGain()
            //      setSpectrometerTimeConstant()
            //      set70MHzGain()
            //      setBandwidth()

            case NUMERIC_INDEXED_LIST:
                {
                // Assign an index {0...n} to an item from a list of Choices.
                // Return the index String padded to fieldcount with leading zeroes, or null if an error occurs.
                strMessageString = Utilities.assignIndexToChoiceItem(parameter,
                                                                     trafficRadix,
                                                                     trafficFieldCount);
                break;
                }

            //--------------------------------------------------------------------------------------
            // Used for:
            //      setRadiometerOffset()
            //      setSpectrometerOffset()

            case HEX_INTEGER:
                {
                try
                    {
                    final int intValue;

                    // Parse the integer using the INPUT DataType radix!
                    intValue = Integer.parseInt(strStarScriptValue, inputRadix);

                    // Find out how many characters we need to send, and in what radix
                    strMessageString = Utilities.intToString(intValue,
                                                             trafficRadix,
                                                             trafficFieldCount);
                    }

                catch (NumberFormatException exception)
                    {
                    // This should never happen, because the ParameterValue has already been parsed
                    strMessageString = null;
                    }

                break;
                }

            //--------------------------------------------------------------------------------------
            // This should not occur!

            default:
                {
                strMessageString = EMPTY_STRING;
                LOGGER.error("SpectraCyberClientCommandMessage.parameterValueToMessageString() Invalid TrafficDataType");
                }
            }

        //LOGGER.debugProtocolEvent("SpectraCyberClientCommandMessage.parameterValueToMessageString() [message_string=" + strMessageString + "]");

        // May be null to indicate failure
        return (strMessageString);
        }


    /***********************************************************************************************
     * Construct a SpectraCyberClientCommandMessage.
     * Save the original StarScript representation of this Command,
     * and generate the appropriate bytes to transmit on the bus.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     */

    public SpectraCyberClientCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                            final Instrument instrumentxml,
                                            final XmlObject module,
                                            final CommandType command,
                                            final String starscript)
        {
        // Validate as much as we can...
        if ((dao != null)
            && (instrumentxml != null)
            && (module != null)
            && (command != null)
            && (starscript != null)
            && (!EMPTY_STRING.equals(starscript.trim()))
            && (ObservatoryInstrumentHelper.isEthernetController(instrumentxml))
            && (instrumentxml.getControllable()))
            {
            this.daoSource = dao;
            this.selectedInstrument = instrumentxml;
            this.selectedModule = module;
            this.selectedCommand = command;
            this.streamTarget = StreamType.SERIAL;

            // Build the byte array corresponding to this context
            if (command.getSendToPort())
                {
                this.listBytes = buildByteArray(selectedInstrument,
                                                selectedModule,
                                                selectedCommand);
                }
            else
                {
                this.listBytes = new ArrayList<Byte>(1);
                }

            this.strStarScript = starscript;

            // The calendar is set when the Message is executed locally,
            // or passed to the queue (not part of the message)
            this.calTx = null;
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }
    }
