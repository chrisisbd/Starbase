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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationNodeType;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;

import java.io.Serializable;


/***************************************************************************************************
 * SerialConfigurationCellDataInterface.
 */

public interface SerialConfigurationCellDataInterface extends Serializable,
                                                              FrameworkConstants,
                                                              FrameworkStrings,
                                                              FrameworkMetadata,
                                                              FrameworkSingletons,
                                                              FrameworkRegex,
                                                              ResourceKeys
    {
    /***********************************************************************************************
     * Get the Cell label.
     *
     * @return String
     */

    String getLabel();


    /***********************************************************************************************
     * Set the Cell label.
     *
     * @param text
     */

    void setLabel(String text);


    /***********************************************************************************************
     * Get the NodeType.
     *
     * @return SerialConfigurationNodeType
     */

    SerialConfigurationNodeType getNodeType();


    /***********************************************************************************************
     * Set the NodeType.
     *
     * @param type
     */

    void setNodeType(SerialConfigurationNodeType type);


    /***********************************************************************************************
     * Get the StreamType.
     *
     * @return StreamType
     */

    StreamType getStreamType();


    /***********************************************************************************************
     * Set the StreamType.
     *
     * @param type
     */

    void setStreamType(StreamType type);


    /***********************************************************************************************
     * Get the DAO Port currently associated with this SerialConfiguration Cell.
     *
     * @return DaoPortInterface
     */

    DaoPortInterface getDaoPort();


    /***********************************************************************************************
     * Set the DAO Port currently associated with this SerialConfiguration Cell.
     *
     * @param daoport
     */

    void setDaoPort(DaoPortInterface daoport);


    /***********************************************************************************************
     * Indicate if the associated Port is open.
     *
     * @return boolean
     */

    boolean isOpen();


    /***********************************************************************************************
     * Indicate if the associated Port is open.
     *
     * @param open
     */

    void setOpen(boolean open);


    /***********************************************************************************************
     * Get the ResourceKey for the SerialConfiguration Properties.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Set the ResourceKey for the SerialConfiguration Properties.
     *
     * @param resourcekey
     */

    void setResourceKey(String resourcekey);


    /***********************************************************************************************
     * Indicate if any of these data have been changed.
     *
     * @return boolean
     */

    boolean isChanged();


    /***********************************************************************************************
     * Indicate if any of these data have been changed.
     *
     * @param changed
     */

    void setChanged(boolean changed);


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    boolean isDebug();


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @param debug
     */

    void setDebug(boolean debug);


    /***********************************************************************************************
     * Get the Cell label.
     *
     * @return String
     */

    String toString();
    }
