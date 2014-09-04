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

package org.lmn.fc.model.emails;


/***************************************************************************************************
 * Enumeration of ActionStatus.
 */

public final class EmailStatus
    {
    public static final EmailStatus WAITING = new EmailStatus(0,
                                                              "Waiting",
                                                              "/toolbarButtonGraphics/general/SendMail16.gif",
                                                              "The Email is waiting to be sent");
    public static final EmailStatus SENT = new EmailStatus(1,
                                                           "Sent",
                                                           "/toolbarButtonGraphics/general/SendMail16.gif",
                                                           "The Email has been sent successfully");
    private final int intID;
    private final String strStatus;
    private final String strIconFilename;
    private final String strTooltip;


    /***********************************************************************************************
     * Privately construct an ActionStatus.
     *
     * @param id
     * @param status
     * @param icon
     * @param tooltip
     */

    private EmailStatus(final int id,
                        final String status,
                        final String icon,
                        final String tooltip)
        {
        intID = id;
        strStatus = status;
        strIconFilename = icon;
        strTooltip = tooltip;
        }


    /***********************************************************************************************
     *
     * @return int
     */

    public int getStatusID()
        {
        return (this.intID);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getStatus()
        {
        return(this.strStatus);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getIconFilename()
        {
        return (this.strIconFilename);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getTooltip()
        {
        return (this.strTooltip);
        }


    /***********************************************************************************************
      *
      * @return String
      */

     public String toString()
        {
        return (this.strStatus);
        }
    }
