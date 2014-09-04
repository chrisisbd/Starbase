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
 * StaribusReceiveEventState.
 */

public enum StaribusReceiveEventState
    {
    IDLE            (0,  "IDLE",            "Idle",             " [state=idle]"),
    DATA_AVAILABLE  (1,  "DATA_AVAILABLE",  "Data Available",   " [state=data_available]"),
    DRAIN_UNWANTED  (2,  "DRAIN_UNWANTED",  "Drain Unwanted",   " [state=drain_unwanted]"),
    FIND_STX        (3,  "FIND_STX",        "Find STX",         " [state=find_stx]"),
    ACCUMULATE      (4,  "ACCUMULATE",      "Accumulate Data",  " [state=accumulate]"),
    WAIT_EOT        (5,  "WAIT_EOT",        "Wait for EOT",     " [state=wait_for_eot]"),
    DRAIN_TRAILING  (6,  "DRAIN_TRAILING",  "Drain Trailing",   " [state=drain_trailing]"),
    ERROR           (7,  "COMMS_ERROR",     "Comms Error",      " [state=comms_error]");

    private final int intIndex;
    private final String strName;
    private final String strStatus;
    private final String strLogEntry;


    /***********************************************************************************************
     * StaribusReceiveEventState.
     *
     * @param index
     * @param name
     * @param status
     * @param logentry
     */

    private StaribusReceiveEventState(final int index,
                                      final String name,
                                      final String status,
                                      final String logentry)
        {
        intIndex = index;
        strName = name;
        strStatus = status;
        strLogEntry = logentry;
        }


    /***********************************************************************************************
     * Get the Index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
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
     * Get the log entry.
     *
     * @return String
     */

    public String getLogEntry()
        {
        return (this.strLogEntry);
        }


    /***********************************************************************************************
     * Get the state name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
