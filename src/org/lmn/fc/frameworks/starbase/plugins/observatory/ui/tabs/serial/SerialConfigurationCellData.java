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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial;


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;


/***************************************************************************************************
 * SerialConfigurationCellData.
 */

public final class SerialConfigurationCellData implements SerialConfigurationCellDataInterface
    {
    // ToDo correct this entry
    private static final long serialVersionUID = -708317745824467773L;

    // Cell Data
    private String label;
    private SerialConfigurationNodeType nodeType;
    private StreamType streamType;
    private DaoPortInterface daoPort;
    private boolean boolOpen;
    private String strResourceKey;
    private boolean boolChanged;
    private boolean boolDebug;


    /***********************************************************************************************
     * SerialConfigurationCellData.
     */

    public SerialConfigurationCellData()
        {
        this.label = "Uninitialised label";
        this.nodeType = SerialConfigurationNodeType.UNKNOWN_PORT;
        this.streamType = StreamType.VIRTUAL;
        this.daoPort= null;
        this.boolOpen = false;
        this.strResourceKey = EMPTY_STRING;
        this.boolChanged = false;
        this.boolDebug = false;
        }


    /***********************************************************************************************
     * Get the Cell label.
     *
     * @return String
     */

    public String getLabel()
        {
        return (this.label);
        }


    /***********************************************************************************************
     * Set the Cell label.
     *
     * @param text
     */

    public void setLabel(final String text)
        {
        this.label = text;
        }


    /***********************************************************************************************
     * Get the NodeType.
     *
     * @return SerialConfigurationNodeType
     */

    public SerialConfigurationNodeType getNodeType()
        {
        return (this.nodeType);
        }


    /***********************************************************************************************
     * Set the NodeType.
     *
     * @param type
     */

    public void setNodeType(final SerialConfigurationNodeType type)
        {
        this.nodeType = type;
        }


    /***********************************************************************************************
     * Get the StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (this.streamType);
        }


    /***********************************************************************************************
     * Set the StreamType.
     *
     * @param type
     */

    public void setStreamType(final StreamType type)
        {
        this.streamType = type;
        }


    /***********************************************************************************************
     * Get the DAO Port currently associated with this SerialConfiguration Cell.
     *
     * @return DaoPortInterface
     */

    public DaoPortInterface getDaoPort()
        {
        return (this.daoPort);
        }


    /***********************************************************************************************
     * Set the DAO Port currently associated with this SerialConfiguration Cell.
     *
     * @param daoport
     */

    public void setDaoPort(final DaoPortInterface daoport)
        {
        this.daoPort = daoport;
        }


    /***********************************************************************************************
     * Indicate if the associated Port is open.
     *
     * @return boolean
     */

    public boolean isOpen()
        {
        return (this.boolOpen);
        }


    /***********************************************************************************************
     * Indicate if the associated Port is open.
     *
     * @param open
     */

    public void setOpen(final boolean open)
        {
        final String SOURCE = "SerialConfigurationCellData.setOpen() ";

        this.boolOpen = open;

        LOGGER.debug(isDebug(), SOURCE + "[label=" + getLabel() + "] [isopen=" + isOpen() + "]");
        }


    /***********************************************************************************************
     * Get the ResourceKey for the SerialConfiguration Properties.
     *
     * @return String
     */

    public String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Set the ResourceKey for the SerialConfiguration Properties.
     *
     * @param resourcekey
     */

    public void setResourceKey(final String resourcekey)
        {
        this.strResourceKey = resourcekey;
        }


    /***********************************************************************************************
     * Indicate if any of these data have been changed.
     *
     * @return boolean
     */

    public boolean isChanged()
        {
        return (this.boolChanged);
        }


    /***********************************************************************************************
     * Indicate if any of these data have been changed.
     *
     * @param changed
     */

    public void setChanged(final boolean changed)
        {
        final String SOURCE = "SerialConfigurationCellData.setChanged() ";

        this.boolChanged = changed;

        LOGGER.debug(isDebug(), SOURCE + "[label=" + getLabel() + "] [changed=" + isChanged() + "]");
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    public boolean isDebug()
        {
        return (this.boolDebug);
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @param debug
     */

    public void setDebug(final boolean debug)
        {
        this.boolDebug = debug;
        }


    /***********************************************************************************************
     * Get the Cell label.
     *
     * @return String
     */

    public String toString()
        {
        return (this.label);
        }
    }
