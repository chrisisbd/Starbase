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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.softwarelab.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
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
 * SoftwareLabCommandMessage.
 */

public final class SoftwareLabCommandMessage extends AbstractCommandMessage
    {
    /***********************************************************************************************
     * Construct a SoftwareLabCommandMessage.
     * Save the original StarScript representation of this Command,
     * and generate the appropriate bytes to transmit on the bus.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     */

    public SoftwareLabCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                     final Instrument instrumentxml,
                                     final XmlObject module,
                                     final CommandType command,
                                     final String starscript)
        {
        // Validate as much as we can...
        if ((dao != null)
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
            this.daoSource = dao;
            this.selectedInstrument = instrumentxml;
            this.selectedModule = module;
            this.selectedCommand = command;
            this.streamTarget = StreamType.VIRTUAL;

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

    public List<Byte> buildByteArray(final Instrument instrumentxml,
                                     final XmlObject module,
                                     final CommandType command)
        {
        final List<Byte> message;

        message = Collections.synchronizedList(new ArrayList<Byte>(MAX_MESSAGE_SIZE));

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

    public String parameterValueToMessageString(final ParameterType parameter)
        {
        final DataTypeDictionary inputDataType;
        final DataTypeDictionary trafficDataType;
        final int inputRadix;
        final int trafficRadix;
        final int trafficFieldCount;
        final DataTypeDictionary datatypeTraffic;
        final String strStarScriptValue;
        String strMessageParameter;

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
        datatypeTraffic = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

        // Get the (validated) ParameterValue to encode
        strStarScriptValue = parameter.getValue();

        // Remember to keep these in step if the XSD changes, and keep in step with DataTypeDictionary!

        // May be null to indicate failure
        return (EMPTY_STRING);
        }
    }
