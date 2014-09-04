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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.logging.EventStatus;

import java.net.InetAddress;
import java.util.Calendar;


/***************************************************************************************************
 * NetworkScannerData.
 */

public final class NetworkScannerData implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons
    {
    // Injections
    private final long longIPAddress;
    private final int intTargetPort;
    private final InetAddress inetAddress;
    private final Calendar calendarResponse;
    private final EventStatus eventStatus;
    private final String strStatus;


    /***********************************************************************************************
     * Construct a NetworkScannerData.
     *
     * @param ipaddress
     * @param targetport
     * @param inetaddress
     * @param calendar
     * @param eventstatus
     * @param statustext
     */

    public NetworkScannerData(final long ipaddress,
                              final int targetport,
                              final InetAddress inetaddress,
                              final Calendar calendar,
                              final EventStatus eventstatus,
                              final String statustext)
        {
        // Injections
        this.longIPAddress = ipaddress;
        this.intTargetPort = targetport;
        this.inetAddress = inetaddress;
        this.calendarResponse = calendar;
        this.eventStatus = eventstatus;
        this.strStatus = statustext;

//        if (datagram != null)
//            {
//            final byte[] arrayResponse;
//
//            arrayResponse = new byte[datagram.getLength()];
//            System.arraycopy(datagram.getData(),
//                             0,
//                             arrayResponse,
//                             0,
//                             datagram.getLength());
//
//            if (arrayResponse.length > 0)
//                {
//                LOGGER.logTimedEvent("NetworkScannerData Response Datagram" + Utilities.byteArrayToExpandedAscii(arrayResponse));
//                }
//            }
        }


    /***********************************************************************************************
     * Get the IP Address.
     *
     * @return long
     */

    public long getIPAddress()
        {
        return (this.longIPAddress);
        }


    /***********************************************************************************************
     * Get the Target Port.
     *
     * @return int
     */

    public int getTargetPort()
        {
        return (this.intTargetPort);
        }


    /***********************************************************************************************
     * Get the InetAddress.
     *
     * @return InetAddress
     */

    public InetAddress getInetAddress()
        {
        return (this.inetAddress);
        }


    /***********************************************************************************************
     * Get the Hostname.
     *
     * @return String
     */

    public String getHostname()
        {
        final String strHostname;

        if (getInetAddress() != null)
            {
            // NOTE: To obtain the hostname, a DNS lookup may be performed
            // This may take several seconds to evaluate
            strHostname = getInetAddress().getHostName();
            }
        else
            {
            strHostname = "unknown";
            }

        return (strHostname);
        }


    /***********************************************************************************************
     * Get the Calendar of the Response.
     *
     * @return Calendar
     */

    public Calendar getResponseCalendar()
        {
        return (this.calendarResponse);
        }


    /***********************************************************************************************
     * Get the Date of the Response, as a String.
     *
     * @return String
     */

    public String getDateAsString()
        {
        final String strDate;

        if (getResponseCalendar() != null)
            {
            strDate = ChronosHelper.toDateString(getResponseCalendar());
            }
        else
            {
            strDate = "2000-00-00";
            }

        return (strDate);
        }


    /***********************************************************************************************
     * Get the Time of the Response, as a String.
     *
     * @return String
     */

    public String getTimeAsString()
        {
        final String strTime;

        if (getResponseCalendar() != null)
            {
            strTime = ChronosHelper.toTimeString(getResponseCalendar());
            }
        else
            {
            strTime = "00:00:00";
            }

        return (strTime);
        }


    /***********************************************************************************************
     * Get the EventStatus.
     *
     * @return EventStatus
     */

    public EventStatus getEventStatus()
        {
        return (this.eventStatus);
        }


    /***********************************************************************************************
     * Get the Status text.
     *
     * @return String
     */

    public String getStatus()
        {
        return (this.strStatus);
        }
    }
