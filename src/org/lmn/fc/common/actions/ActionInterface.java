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

package org.lmn.fc.common.actions;

import org.lmn.fc.model.users.UserPlugin;

import java.sql.Date;
import java.sql.Time;


/**
 */
public interface ActionInterface
    {
    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    UserPlugin getUserData();


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    String getAction();


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    int getType();


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    String getComments();


    /**
     * TODO DOCUMENT ME!
     *
     * @param comments -
     */
    void setComments(String comments);


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    boolean isCompleted();


    /**
     * TODO DOCUMENT ME!
     *
     * @param completed -
     */
    void setCompleted(boolean completed);


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    Date getDateRaised();


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    Time getTimeRaised();


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    Date getDateCompleted();


    /**
     * TODO DOCUMENT ME!
     *
     * @param completed -
     */
    void setDateCompleted(Date completed);


    /**
     * TODO DOCUMENT ME!
     *
     * @return -
     */
    Time getTimeCompleted();


    /**
     * TODO DOCUMENT ME!
     *
     * @param completed -
     */
    void setTimeCompleted(Time completed);
    }
