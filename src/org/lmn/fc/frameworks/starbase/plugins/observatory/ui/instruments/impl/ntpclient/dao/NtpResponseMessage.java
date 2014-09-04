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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.ntpclient.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.util.ArrayList;
import java.util.List;


/***********************************************************************************************
 * NtpResponseMessage.
 */

public final class NtpResponseMessage extends AbstractResponseMessage
    {
    /***********************************************************************************************
     * Construct a NtpResponseMessage.
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

    public NtpResponseMessage(final String portname,
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
     * Parse a byte array, attempting to find a NtpResponseMessage
     * which can be interpreted as coming from one of the Instruments.
     *
     * @param instrumentsdoc
     * @param dao
     * @param bytes
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface parseBytes(final InstrumentsDocument instrumentsdoc,
                                               final ObservatoryInstrumentDAOInterface dao,
                                               final byte[] bytes)
        {
        return null;
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


    /***********************************************************************************************
     * Build the byte array which represents the Response of the supplied context.
     * All parameters are assumed to have been validated.
     *
     * @param instrumentxml
     * @param module
     * @param commandtype
     * @param status
     *
     * @return List<Byte>
     */

    public List<Byte> buildByteArray(final Instrument instrumentxml,
                                     final XmlObject module,
                                     final CommandType commandtype,
                                     final int status)
        {
        return null;
        }
    }
