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

package org.lmn.fc.model.actions.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.model.actions.ActionDataInterface;
import org.lmn.fc.model.actions.ActionStatus;
import org.lmn.fc.model.users.UserPlugin;

import java.util.GregorianCalendar;


/***************************************************************************************************
 * An Action to be performed by the TestPlugin user.
 */

public final class ActionData implements ActionDataInterface,
                                         FrameworkStrings
    {
    private final UserPlugin userData;
    private final String strAction;
    private final ActionStatus actionStatus;
    private String strComments;
    private boolean boolCompleted;
    private final GregorianCalendar dateRaised;
    private final GregorianCalendar timeRaised;
    private GregorianCalendar dateCompleted;
    private GregorianCalendar timeCompleted;


    /***********************************************************************************************
     * Construct an ActionData.
     *
     * @param userdata
     * @param dateraised
     * @param timeraised
     * @param action
     * @param status
     */

    public ActionData(final UserPlugin userdata,
                      final GregorianCalendar dateraised,
                      final GregorianCalendar timeraised,
                      final String action,
                      final ActionStatus status)
        {
        userData = userdata;
        dateRaised = dateraised;
        timeRaised = timeraised;
        dateCompleted = null;
        timeCompleted = null;
        strAction = action;
        actionStatus = status;
        strComments = EMPTY_STRING;
        boolCompleted = false;
        }


    /***********************************************************************************************
     *
     * @return UserData
     */

    public final UserPlugin getUserData()
        {
        return (this.userData);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public final String getAction()
        {
        return (this.strAction);
        }


    /***********************************************************************************************
     *
     * @return int
     */

    public final ActionStatus getStatus()
        {
        return (this.actionStatus);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public final String getComments()
        {
        return (this.strComments);
        }


    /***********************************************************************************************
     *
     * @param comments
     */

    public final void setComments(final String comments)
        {
        this.strComments = comments;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isCompleted()
        {
        return (this.boolCompleted);
        }


    /***********************************************************************************************
     *
     * @param completed
     */

    public final void setCompleted(final boolean completed)
        {
        this.boolCompleted = completed;
        }


    /***********************************************************************************************
     *
     * @return Date
     */

    public final GregorianCalendar getDateRaised()
        {
        return (this.dateRaised);
        }


    /***********************************************************************************************
     *
     * @return Time
     */

    public final GregorianCalendar getTimeRaised()
        {
        return (this.timeRaised);
        }


    /***********************************************************************************************
     *
     * @return Date
     */

    public final GregorianCalendar getDateCompleted()
        {
        return (this.dateCompleted);
        }


    /***********************************************************************************************
     *
     * @param completed
     */

    public final void setDateCompleted(final GregorianCalendar completed)
        {
        this.dateCompleted = completed;
        }


    /***********************************************************************************************
     *
     * @return Time
     */

    public final GregorianCalendar getTimeCompleted()
        {
        return (this.timeCompleted);
        }


    /***********************************************************************************************
     *
     * @param completed
     */

    public final void setTimeCompleted(final GregorianCalendar completed)
        {
        this.timeCompleted = completed;
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
