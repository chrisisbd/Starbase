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

package org.lmn.fc.model.dao;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.resources.QueryPlugin;

import java.sql.SQLException;

/***************************************************************************************************
 * DAOUtilities.
 */

public final class DAOUtilities
    {
    /***********************************************************************************************
     * Check that the QueryData is valid, and that there is an open database connection.
     * Also check that this DAO is being used for the correct DataStore.
     *
     * @param query
     * @param store
     *
     * @return boolean
     *
     * @throws SQLException
     */

    public static boolean isValidQuery(final QueryPlugin query,
                                       final DataStore store) throws SQLException
        {
        final boolean boolValid;

        // ToDo check query for specific store??
        boolValid = ((query != null)
                    && (query.getResource() != null)
                    && (store != null)
                    && (FrameworkDatabase.getInstance().getDatabaseOptions() != null)
                    && (FrameworkDatabase.getInstance().getDatabaseOptions().getDataStore() != null)
                    && (FrameworkDatabase.getInstance().getDatabaseOptions().getDataStore().equals(store))
                    && (FrameworkDatabase.getInstance().getConnection() != null)
                    && (!FrameworkDatabase.getInstance().getConnection().isClosed()));

        return (boolValid);
        }


    /***********************************************************************************************
     * Convert a Long value for use in an SQL statment.
     *
     * @param value
     *
     * @return String
     */

    public static String toSQL(final long value)
        {
        return (Long.toString(value));
        }


    /***********************************************************************************************
     * Convert a boolean value for use in an SQL statment.
     *
     * @param value
     *
     * @return String
     */

    public static String toSQL(final boolean value)
        {
        if (value)
            {
            return ("1");
            }
        else
            {
            return ("0");
            }
        }

    /***********************************************************************************************
     * Surround a String value with quotes for use in an SQL statment.
     *
     * @param value
     *
     * @return String
     */

    public static String toSQL(final String value)
        {
        return (FrameworkConstants.SQL_QUOTE + value + FrameworkConstants.SQL_QUOTE);
        }
    }
