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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  29-11-05    LMN created file
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.database;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.Logger;

import java.util.HashMap;


/***************************************************************************************************
 * Encapsulate all database options in a HashMap.
 */

public final class DatabaseOptions implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              ResourceKeys
    {
    private static final Logger LOGGER = Logger.getInstance();

    public static final DatabaseType DEFAULT_TYPE = DatabaseType.LOCAL;

    private DataStore dataStore;
    private DatabaseType databaseType;
    private final HashMap<DatabaseType, DatabaseProperties> mapDatabaseProperties;


    /***********************************************************************************************
     * Construct the DatabaseOptions.
     */

    public DatabaseOptions()
        {
        // Set up a default Store
        dataStore = DataStore.XML;

        // Set up a default Type
        databaseType = DEFAULT_TYPE;

        // We expect sets of options for at least Local, Remote & Embedded databases
        mapDatabaseProperties = new HashMap<DatabaseType, DatabaseProperties>(3);
        }


    /***********************************************************************************************
     *
     * @return DataStore
     */

    public DataStore getDataStore()
        {
        return dataStore;
        }


    /***********************************************************************************************
     *
     * @param store
     */

    public void setDataStore(final DataStore store)
        {
        this.dataStore = store;
        }


    /***********************************************************************************************
     *
     * @return DatabaseType
     */

    public DatabaseType getDatabaseType()
        {
        return databaseType;
        }


    /***********************************************************************************************
     *
     * @param type
     */

    public void setDatabaseType(final DatabaseType type)
        {
        this.databaseType = type;
        }


    /***********************************************************************************************
     *
     * @param type
     *
     * @return DatabaseProperties
     */

    public DatabaseProperties getDatabaseProperties(final DatabaseType type)
        {
        DatabaseProperties properties;

        // Set some default properties
        properties = new DatabaseProperties();

        if ((getPropertiesMap() != null)
            && (getPropertiesMap().containsKey(type)))
            {
            properties = getPropertiesMap().get(type);
            }

        return (properties);
        }


    /***********************************************************************************************
     *
     * @param type
     * @param properties
     */

    public void setDatabaseProperties(final DatabaseType type,
                                      final DatabaseProperties properties)
        {
        if (getPropertiesMap() != null)
            {
            getPropertiesMap().put(type, properties);
            }
        }


    /***********************************************************************************************
     *
     * @return DatabaseProperties
     */

    public DatabaseProperties getCurrentDatabaseProperties()
        {
        return (getDatabaseProperties(getDatabaseType()));
        }


    /***********************************************************************************************
     *
     * @return HashMap<DatabaseType, DatabaseProperties>
     */

    private HashMap<DatabaseType, DatabaseProperties> getPropertiesMap()
        {
        return (this.mapDatabaseProperties);
        }


    /***********************************************************************************************
     * A utility method to indicate if the database is embedded.
     *
     * @return boolean
     */

    public boolean isEmbedded()
        {
        return (DatabaseType.EMBEDDED.equals(getDatabaseType()));
        }


    /***********************************************************************************************
     *
     * @param debug
     */

    public void debugOptions(final boolean debug)
        {
        DatabaseProperties database;

        LOGGER.debug(debug, INDENT + PREFIX + KEY_DATABASE_STORE + EQUALS + getDataStore() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_DATABASE_TYPE + EQUALS + getDatabaseType() + TERMINATOR);

        database = getDatabaseProperties(DatabaseType.LOCAL);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_DRIVER + EQUALS + database.getDriver() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_DATA_SOURCE + EQUALS + database.getDataSource() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_PORT + EQUALS + database.getPort() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_DATABASE + EQUALS + database.getDatabase() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_INLINE + EQUALS + database.isCredentialsInline() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_USER_NAME + EQUALS + database.getUsername() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_LOCAL_PASSWORD + EQUALS + database.getPassword() + TERMINATOR);

        database = getDatabaseProperties(DatabaseType.REMOTE);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_DRIVER + EQUALS + database.getDriver() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_DATA_SOURCE + EQUALS + database.getDataSource() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_PORT + EQUALS + database.getPort() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_DATABASE + EQUALS + database.getDatabase() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_INLINE + EQUALS + database.isCredentialsInline() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_USER_NAME + EQUALS + database.getUsername() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_REMOTE_PASSWORD + EQUALS + database.getPassword() + TERMINATOR);

        database = getDatabaseProperties(DatabaseType.EMBEDDED);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_DRIVER + EQUALS + database.getDriver() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_DATA_SOURCE + EQUALS + database.getDataSource() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_PORT + EQUALS + database.getPort() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_DATABASE + EQUALS + database.getDatabase() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_INLINE + EQUALS + database.isCredentialsInline() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_USER_NAME + EQUALS + database.getUsername() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_EMBEDDED_PASSWORD + EQUALS + database.getPassword() + TERMINATOR);
        }
    }
