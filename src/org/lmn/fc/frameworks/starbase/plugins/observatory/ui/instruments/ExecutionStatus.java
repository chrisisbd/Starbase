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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

/***************************************************************************************************
 * ExecutionStatus.
 */

public enum ExecutionStatus
    {
    INITIALISED (0, "Initialised",  "Initialised"),
    WAITING     (1, "Waiting",      "Waiting"),
    FINISHED    (2, "Finished",     "Finished");


    private final int intValue;
    private final String strName;
    private final String strStatus;


    /***********************************************************************************************
     * ExecutionStatus.
     *
     * @param value
     * @param name
     * @param status
     */

    private ExecutionStatus(final int value,
                            final String name,
                            final String status)
        {
        intValue = value;
        strName = name;
        strStatus = status;
        }


    /***********************************************************************************************
     * Get the TypeID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intValue);
        }


    /***********************************************************************************************
     * Get the state name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the status text.
     *
     * @return String
     */

    public String getStatus()
        {
        return (this.strStatus);
        }


    /***********************************************************************************************
     * Get the state name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
