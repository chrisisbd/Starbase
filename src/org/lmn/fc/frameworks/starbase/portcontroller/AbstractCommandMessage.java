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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.Calendar;
import java.util.List;


/***********************************************************************************************
 * AbstractCommandMessage.
 */

public abstract class AbstractCommandMessage implements CommandMessageInterface
    {
    protected static final int MAX_MESSAGE_SIZE = 1000;

    protected ObservatoryInstrumentDAOInterface daoSource;
    protected Instrument selectedInstrument;
    protected XmlObject selectedModule;
    protected CommandType selectedCommand;
    protected List<ParameterType> listExecutionParameters;
    protected StreamType streamTarget;
    protected String strStarScript;
    protected List<Byte> listBytes;
    protected Calendar calTx;


    /***********************************************************************************************
     * Get the source, i.e. the DAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public ObservatoryInstrumentDAOInterface getDAO()
        {
        return (this.daoSource);
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
     * Get the Execution Parameters.
     * This carries the complete list of Parameters entered on the UI to the Command execution.
     *
     * @return List<ParameterType>
     */

    public List<ParameterType> getExecutionParameters()
        {
        return (this.listExecutionParameters);
        }


    /***********************************************************************************************
     * Set the Execution Parameters.
     * This carries the complete list of Parameters entered on the UI to the Command execution.
     *
     * @param parameters
     */

    public void setExecutionParameters(final List<ParameterType> parameters)
        {
        this.listExecutionParameters = parameters;
        }


    /***********************************************************************************************
     * Get the StreamType of the stream which receives this CommandMessage.
     *
     * @return StreamType
     */

    public StreamType getTargetStreamType()
        {
        return (this.streamTarget);
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
     * Get the Calendar indicating the time the message was transmitted.
     *
     * @return Calendar
     */

    public Calendar getTxCalendar()
        {
        return (this.calTx);
        }


    /***********************************************************************************************
     * Set the Calendar indicating the time the message was transmitted.
     *
     * @param calendar
     */

    public void setTxCalendar(final Calendar calendar)
        {
        this.calTx = calendar;
        }


    /***********************************************************************************************
     * Get the list of bytes received on the bus.
     * Remember this comes from a Collections.synchronizedList()
     * so any iteration must be inside a synchronized wrapper.
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
