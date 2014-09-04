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

package org.lmn.fc.frameworks.starbase.portcontroller;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Controller;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;

import java.util.Calendar;
import java.util.List;


/***************************************************************************************************
 * AbstractResponseMessage.
 */

public abstract class AbstractResponseMessage implements ResponseMessageInterface
    {
    protected String strSource;
    protected DAOWrapperInterface daoWrapper;
    protected Instrument selectedInstrument;
    protected XmlObject selectedModule;
    protected CommandType selectedCommand;
    protected String strStarScript;
    protected int intStatus;
    protected ResponseMessageStatusList listResponseMessageStatus;
    protected List<Byte> listBytes;
    protected Calendar calRx;


    /***********************************************************************************************
     * Build the Registry ResourceKey for the Instrument.Module.Response Property.
     * This is the StarScript reference to this value.
     *
     * @param instrumentxml
     * @param module
     * @param commandtype
     *
     * @return String
     */

    public synchronized static String buildResponseResourceKey(final Instrument instrumentxml,
                                                               final XmlObject module,
                                                               final CommandType commandtype)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if (instrumentxml != null)
//            && (XmlBeansUtilities.isValidXml(instrumentxml)))
            {
            buffer.append(instrumentxml.getResourceKey());

            if (module != null)
                {
                if (module instanceof Controller)
                    {
                    buffer.append(DOT);
                    buffer.append(((Controller) module).getResourceKey());
                    buffer.append(DOT);

                    // There may be no Response expected for this Command
                    if ((commandtype != null)
                        && (commandtype.getResponse() != null)
                        && (commandtype.getResponse().getName() != null))
                        {
                        buffer.append(commandtype.getResponse().getName());
                        }
                    else
                        {
                        buffer.append(RESPONSE_ACK);
                        }
                    }
                else if (module instanceof PluginType)
                    {
                    buffer.append(DOT);
                    buffer.append(((PluginType) module).getResourceKey());
                    buffer.append(DOT);

                    // There may be no Response expected for this Command
                    if ((commandtype != null)
                        && (commandtype.getResponse() != null)
                        && (commandtype.getResponse().getName() != null))
                        {
                        buffer.append(commandtype.getResponse().getName());
                        }
                    else
                        {
                        buffer.append(RESPONSE_ACK);
                        }
                    }
                else
                    {
                    // This is an error!
                    buffer.append(DOT);
                    buffer.append(INVALID_RESOURCE_KEY);
                    }
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Map the ResponseMessageStatus bits into a List of Enum for easier use.
     * If the operation was successful, i.e. no errors were found,
     * then SUCCESS appears at element zero in the List.
     *
     * @param statusbits
     *
     * @return ResponseMessageStatusList
     */

    public synchronized static ResponseMessageStatusList mapResponseStatusBits(final int statusbits)
        {
        final ResponseMessageStatusList listResponseMessageStatus;

        // There can be at most 16 status values
        listResponseMessageStatus = ResponseMessageStatus.createResponseMessageStatusList();

        if (statusbits == 0)
            {
            // There are no errors, or BUSY, or CAPTURE_ACTIVE,
            // so stop now with a single entry
            listResponseMessageStatus.add(ResponseMessageStatus.SUCCESS);
            }
        else
            {
            final ResponseMessageStatus[] arrayStatus;

            // Enumerate the set of ResponseMessageStatus to see if there is a match on any bit
            arrayStatus = ResponseMessageStatus.values();

            for (int i = 0;
                 i < arrayStatus.length;
                 i++)
                {
                final ResponseMessageStatus status;

                status = arrayStatus[i];

                if ((status.getBitMask() & statusbits) != 0)
                    {
                    // Add all non-zero, including BUSY and CAPTURE_ACTIVE
                    // SUCCESS will never be found here, since it is represented by all zeroes
                    listResponseMessageStatus.add(status);
                    }
                }

            // IMPORTANT! If we **ONLY** found BUSY and CAPTURE_ACTIVE then we need SUCCESS too
            // This MUST appear at entry index 0, since not all of this code handles
            // the case of multiple errors or status, and will rely on the value from .get(0)
            // SUCCESS usually takes precedence, but if more detail is required, then search
            // in this List, or look directly at the status bits.
            // Mask off BUSY and CAPTURE_ACTIVE, to leave genuine error bits,
            // if these are zero, then it is a real SUCCESS, so add that status.
            if ((statusbits & 0x3fff) == 0)
                {
                listResponseMessageStatus.add(0, ResponseMessageStatus.SUCCESS);
                }
            }

        return (listResponseMessageStatus);
        }


    /***********************************************************************************************
     * Debug the parameters supplied to the ResponseMessage.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param status
     */

    public static void debugParameters(final ObservatoryInstrumentDAOInterface dao,
                                       final Instrument instrumentxml,
                                       final XmlObject module,
                                       final CommandType command,
                                       final String starscript,
                                       final int status)
        {
        if (dao == null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() DAO is NULL");
            }

        if (instrumentxml == null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Instrument is NULL");
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Instrument Valid XML = " + XmlBeansUtilities.isValidXml(instrumentxml));
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Controllable = " + instrumentxml.getControllable());

            if (instrumentxml.getController() == null)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "AbstractResponseMessage.debugParameters() Controller is NULL");
                }
            else
                {
                if (instrumentxml.getController().getIPAddress() != null)
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "AbstractResponseMessage.debugParameters() Controller Address = "
                                           + IPVersion.stripTrailingPaddingFromIPAddressAndPort(instrumentxml.getController().getIPAddress()));
                    }
                else
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "AbstractResponseMessage.debugParameters() Controller Address = "
                                           + instrumentxml.getController().getStaribusAddress());
                    }
                }
            }

        if (module == null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Module is NULL");
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Module Valid XML = " + XmlBeansUtilities.isValidXml(module));
            }

        if (command == null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Command is NULL");
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() Command Valid XML = " + XmlBeansUtilities.isValidXml(command));
            }

        if (starscript == null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "AbstractResponseMessage.debugParameters() StarScript is NULL");
            }
        else
            {
            if (EMPTY_STRING.equals(starscript.trim()))
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "AbstractResponseMessage.debugParameters() StarScript is EMPTY");
                }
            else
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "AbstractResponseMessage.debugParameters() StarScript = " + starscript);
                }
            }
        }


    /***********************************************************************************************
     * Get the source, i.e. the DAO Port Name.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public String getPortName()
        {
        return (this.strSource);
        }


    /***********************************************************************************************
     * Set the source, i.e. the DAO Port Name.
     *
     * @param portname
     */

    public void setPortName(final String portname)
        {
        this.strSource = portname;
        }


    /***********************************************************************************************
     * Get the DaoData, appropriately wrapped.
     * This is used only to transfer the data from the DAO thread to the EventDispatchThread.
     * It must be set to null after use, to avoid hanging on to the message data.
     *
     * @return DAOWrapperInterface
     */

    public DAOWrapperInterface getWrappedData()
        {
        return (this.daoWrapper);
        }


    /***********************************************************************************************
     * Set the DaoData, appropriately wrapped.
     * This is used only to transfer the data from the DAO thread to the EventDispatchThread.
     * It must be set to null after use, to avoid hanging on to the message data.
     *
     * @param daodata
     *
     * @return DAOWrapperInterface
     */

    public void setWrappedData(final DAOWrapperInterface daodata)
        {
        this.daoWrapper = daodata;
        }


    /***********************************************************************************************
     * Get the Instrument.
     *
     * @return Instrument
     */

    public Instrument getInstrument()
        {
        return (this.selectedInstrument);
        }


    /***********************************************************************************************
     * Get the Module.
     *
     * @return XmlObject
     */

    public XmlObject getModule()
        {
        return (this.selectedModule);
        }


    /***********************************************************************************************
     * Get the CommandType.
     *
     * @return CommandType
     */

    public CommandType getCommandType()
        {
        return (this.selectedCommand);
        }


    /***********************************************************************************************
     * Get the StarScript representation of this Command.
     *
     * @return String
     */

    public String getStarScript()
        {
        return (this.strStarScript);
        }


    /***********************************************************************************************
     * Get the status of the Response as an integer bitfield.
     *
     * @return int
     */

    public int getStatusBits()
        {
        return (this.intStatus);
        }


    /***********************************************************************************************
     * Get the status of the Response as a List of ResponseMessageStatus Enum.
     *
     * @return ResponseMessageStatusList
     */

    public ResponseMessageStatusList getResponseMessageStatusList()
        {
        return (this.listResponseMessageStatus);
        }


    /***********************************************************************************************
     * Get the Calendar indicating the time the message was received.
     *
     * @return Calendar
     */

    public Calendar getRxCalendar()
        {
        return (this.calRx);
        }


    /***********************************************************************************************
     * Set the Calendar indicating the time the message was received.
     *
     * @param calendar
     */

    public void setRxCalendar(final Calendar calendar)
        {
        this.calRx = calendar;
        }


    /***********************************************************************************************
     * Get the list of bytes received on the bus.
     * Remember this comes from a Collections.synchronizedList()
     *
     * @return List<Byte>
     */

    public List<Byte> getByteList()
        {
        return (this.listBytes);
        }


    /***********************************************************************************************
     * Retrieve the message as an array of primitive bytes.
     *
     * @return byte[]
     */

    public byte[] getByteArray()
        {
        if ((getByteList() != null)
            && (!getByteList().isEmpty()))
            {
            // This method will handle the synchronized wrapping needed by Collections.synchronizedList()
            return (Utilities.byteListToArray(getByteList()));
            }
        else
            {
            return (null);
            }
        }


    /***********************************************************************************************
     * Show the message bytes as a String.
     *
     * @return String
     */

    public String toString()
        {
        if ((getByteList() != null)
            && (!getByteList().isEmpty()))
            {
            // This method will handle the synchronized wrapping needed by Collections.synchronizedList()
            return (Utilities.byteListToString(getByteList()));
            }
        else
            {
            return (EMPTY_STRING);
            }
        }
    }
