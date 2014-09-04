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
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.Calendar;
import java.util.List;


public interface CommandMessageInterface extends FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 ObservatoryConstants
    {
    /***********************************************************************************************
     * Get the source, i.e. the DAO.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getDAO();


    /***********************************************************************************************
     * Get the Instrument.
     *
     * @return Instrument
     */

    Instrument getInstrument();


    /***********************************************************************************************
     * Get the Module.
     *
     * @return XmlObject
     */

    XmlObject getModule();


    /***********************************************************************************************
     * Get the CommandType.
     *
     * @return CommandType
     */

    CommandType getCommandType();


    /***********************************************************************************************
     * Get the Execution Parameters.
     * This carries the complete list of Parameters entered on the UI to the Command execution.
     *
     * @return List<ParameterType>
     */

    List<ParameterType> getExecutionParameters();


    /***********************************************************************************************
     * Set the Execution Parameters.
     * This carries the complete list of Parameters entered on the UI to the Command execution.
     *
     * @param parameters
     */

    void setExecutionParameters(List<ParameterType> parameters);


    /***********************************************************************************************
     * Get the StreamType of the stream which receives this CommandMessage.
     *
     * @return StreamType
     */

    StreamType getTargetStreamType();


    /***********************************************************************************************
     * Get the StarScript representation of this Command.
     *
     * @return String
     */

    String getStarScript();


    /***********************************************************************************************
     * Get the Calendar indicating the time the message was transmitted.
     *
     * @return Calendar
     */

    Calendar getTxCalendar();


    /***********************************************************************************************
     * Set the Calendar indicating the time the message was transmitted.
     *
     * @param calendar
     */

    void setTxCalendar(Calendar calendar);


    /***********************************************************************************************
     * Retrieve the message as a List of Bytes.
     *
     * @return List<Byte>
     */

    List<Byte> getByteList();


    /***********************************************************************************************
     * Retrieve the message as an array of primitive bytes.
     *
     * @return byte[]
     */

    byte[] getByteArray();


    /***********************************************************************************************
     * Show the message bytes as a String.
     *
     * @return String
     */

    String toString();
    }
