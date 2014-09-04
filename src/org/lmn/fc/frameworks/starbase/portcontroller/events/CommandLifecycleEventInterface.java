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

package org.lmn.fc.frameworks.starbase.portcontroller.events;

import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;

import java.io.Serializable;


public interface CommandLifecycleEventInterface extends Serializable
    {
    /***********************************************************************************************
     * The object on which the event occurred.
     *
     * @return Object
     */

    Object getSource();


    /***********************************************************************************************
     * Get the CommandMessage.
     *
     * @return CommandMessageInterface
     */

    CommandMessageInterface getCommandMessage();


    /***********************************************************************************************
     * Set the CommandMessage.
     *
     * @param message
     */

    void setCommandMessage(CommandMessageInterface message);


    /***********************************************************************************************
     * Get the ResponseMessage.
     *
     * @return ResponseMessageInterface
     */

    ResponseMessageInterface getResponseMessage();


    /***********************************************************************************************
     * Set the ResponseMessage.
     *
     * @param message
     */

    void setResponseMessage(ResponseMessageInterface message);
    }
