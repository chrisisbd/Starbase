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

package org.lmn.fc.model.actions;

import org.lmn.fc.common.constants.FrameworkSingletons;

import java.util.HashMap;


/***************************************************************************************************
 * Enumeration of ActionStatus.
 */

public final class ActionStatus implements FrameworkSingletons
    {
    public static final ActionStatus FATAL = new ActionStatus(0,
                                                              "Fatal",
                                                              "messages/MessageFatal.gif",
                                                              "An Error has occurred");
    public static final ActionStatus INFO = new ActionStatus(1,
                                                             "Info",
                                                             "messages/MessageInfo.gif",
                                                             "Information only");
    public static final ActionStatus WARNING = new ActionStatus(2,
                                                                "Warning",
                                                                "messages/MessageWarning.gif",
                                                                "Warning!");
    public static final ActionStatus QUESTION = new ActionStatus(3,
                                                                 "Question",
                                                                 "messages/MessageQuestion.gif",
                                                                 "Question");
    public static final ActionStatus PLAIN = new ActionStatus(4,
                                                              "Plain",
                                                              "messages/MessagePlain.gif",
                                                              "Message only");
    public static final ActionStatus SILENT = new ActionStatus(5,
                                                               "Silent",
                                                               "messages/MessageWarning.gif",
                                                               "Warning!");

    private static HashMap<Integer, ActionStatus> mapStatus;

    private final int intID;
    private final String strStatus;
    private final String strIconFilename;
    private final String strTooltip;


    /***********************************************************************************************
     * Get the ActionStatus corresponding to the specified ID.
     *
     * @param id
     *
     * @return ActionStatus
     */

    public static ActionStatus getEventStatus(final int id)
        {
        final ActionStatus status;

        if ((mapStatus != null)
            && (mapStatus.containsKey(id)))
            {
            status = mapStatus.get(id);
            }
        else
            {
            LOGGER.error("ERROR IN Action STATUS MAP!");
            status = FATAL;
            }

        return (status);
        }


    /***********************************************************************************************
     * Privately construct an ActionStatus.
     *
     * @param id
     * @param status
     * @param icon
     * @param tooltip
     */

    private ActionStatus(final int id,
                         final String status,
                         final String icon,
                         final String tooltip)
        {
        intID = id;
        strStatus = status;
        strIconFilename = icon;
        strTooltip = tooltip;

        if (mapStatus == null)
            {
            mapStatus = new HashMap<Integer, ActionStatus>(6);
            }

        // Keep a map of all ActionStatus to allow easy retrieval from the database
        mapStatus.put(intID, this);
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
