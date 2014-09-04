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
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import java.util.Calendar;
import java.util.List;


public interface ResponseMessageInterface extends FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  FrameworkXpath,
                                                  ObservatoryConstants
    {
    /***********************************************************************************************
     * Get the source, i.e. the DAO Port Name.
     *
     * @return String
     */

    String getPortName();


    /***********************************************************************************************
     * Set the source, i.e. the DAO Port Name.
     *
     * @param portname
     */

    void setPortName(String portname);


    /***********************************************************************************************
     * Get the DaoData, appropriately wrapped.
     * This is used only to transfer the data from the DAO thread to the EventDispatchThread.
     * It must be set to null after use.
     *
     * @return DAOWrapperInterface
     */

    DAOWrapperInterface getWrappedData();


    /***********************************************************************************************
     * Set the DaoData, appropriately wrapped.
     * This is used only to transfer the data from the DAO thread to the EventDispatchThread.
     * It must be set to null after use, to avoid hanging on to the message data.
     *
     * @param daodata
     *
     * @return DAOWrapperInterface
     */

    void setWrappedData(DAOWrapperInterface daodata);


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
     * Get the StarScript representation of this Command.
     *
     * @return String
     */

    String getStarScript();


    /***********************************************************************************************
     * Get the status of the Response as an integer bitfield.
     *
     * @return int
     */

    int getStatusBits();


    /***********************************************************************************************
     * Get the status of the Response as a List of ResponseMessageStatus Enum.
     *
     * @return ResponseMessageStatusList
     */

    ResponseMessageStatusList getResponseMessageStatusList();


    /***********************************************************************************************
     * Get the Calendar indicating the time the message was received.
     *
     * @return Calendar
     */

    Calendar getRxCalendar();


    /***********************************************************************************************
     * Set the Calendar indicating the time the message was received.
     *
     * @param calendar
     */

    void setRxCalendar(Calendar calendar);


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
