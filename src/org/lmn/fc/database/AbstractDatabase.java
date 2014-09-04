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

package org.lmn.fc.database;

import com.mysql.management.MysqldResource;
import org.lmn.fc.model.dao.DataStore;

import java.sql.Connection;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;


/***************************************************************************************************
 * An Abstract Database.
 */

public abstract class AbstractDatabase implements DatabasePlugin
    {
    private DatabaseOptions databaseOptions;
    private boolean boolDatabaseActive;
    private boolean boolDebugMode;
    private Connection connDatabase;

    // Database-specific fields
    private MysqldResource mySQL;


    /***********************************************************************************************
     * Configure the specified DatabaseOptions using properties read from the PropertyResourceBundle.
     * Use defaults for missing properties.
     *
     * @param options
     * @param bundle
     *
     * @throws MissingResourceException
     */

    public static void configureDatabaseOptions(final DatabaseOptions options,
                                                final PropertyResourceBundle bundle) throws MissingResourceException
        {
        try
            {
            final String strDataStore;
            final String strType;
            final DatabaseProperties propertiesLocal;
            final DatabaseProperties propertiesRemote;
            final DatabaseProperties propertiesEmbedded;

            // Set the DataStore
            strDataStore = bundle.getString(KEY_DATABASE_STORE);

            // Note that IMPORTS and EXPORTS are not DataStores!
            if ((DataStore.XML.getName().equalsIgnoreCase(strDataStore))
                && (DataStore.XML.isAvailable()))
                {
                options.setDataStore(DataStore.XML);
                }
            else if ((DataStore.MYSQL.getName().equalsIgnoreCase(strDataStore))
                && (DataStore.MYSQL.isAvailable()))
                {
                options.setDataStore(DataStore.MYSQL);
                }
            else if ((DataStore.HSQLDB.getName().equalsIgnoreCase(strDataStore))
                && (DataStore.HSQLDB.isAvailable()))
                {
                options.setDataStore(DataStore.HSQLDB);
                }
            else
                {
                options.setDataStore(DataStore.XML);
                }

            // Set the DatabaseType
            strType = bundle.getString(KEY_DATABASE_TYPE);

            if (DatabaseType.LOCAL.getType().equalsIgnoreCase(strType))
                {
                options.setDatabaseType(DatabaseType.LOCAL);
                }
            else if (DatabaseType.REMOTE.getType().equalsIgnoreCase(strType))
                {
                options.setDatabaseType(DatabaseType.REMOTE);
                }
            else if (DatabaseType.EMBEDDED.getType().equalsIgnoreCase(strType))
                {
                options.setDatabaseType(DatabaseType.EMBEDDED);
                }
            else
                {
                // Use the default if the property is not found
                options.setDatabaseType(DatabaseOptions.DEFAULT_TYPE);
                }

            // Now read each Database Property
            propertiesLocal = new DatabaseProperties();
            propertiesLocal.setDriver(bundle.getString(KEY_LOCAL_DRIVER));
            propertiesLocal.setDataSource(bundle.getString(KEY_LOCAL_DATA_SOURCE));
            propertiesLocal.setPort(bundle.getString(KEY_LOCAL_PORT));
            propertiesLocal.setDatabase(bundle.getString(KEY_LOCAL_DATABASE));
            propertiesLocal.setCredentialsInline(Boolean.valueOf(bundle.getString(KEY_LOCAL_INLINE)));
            propertiesLocal.setUsername(bundle.getString(KEY_LOCAL_USER_NAME));
            propertiesLocal.setPassword(bundle.getString(KEY_LOCAL_PASSWORD));
            options.setDatabaseProperties(DatabaseType.LOCAL, propertiesLocal);

            propertiesRemote = new DatabaseProperties();
            propertiesRemote.setDriver(bundle.getString(KEY_REMOTE_DRIVER));
            propertiesRemote.setDataSource(bundle.getString(KEY_REMOTE_DATA_SOURCE));
            propertiesRemote.setPort(bundle.getString(KEY_REMOTE_PORT));
            propertiesRemote.setDatabase(bundle.getString(KEY_REMOTE_DATABASE));
            propertiesRemote.setCredentialsInline(Boolean.valueOf(bundle.getString(KEY_REMOTE_INLINE)));
            propertiesRemote.setUsername(bundle.getString(KEY_REMOTE_USER_NAME));
            propertiesRemote.setPassword(bundle.getString(KEY_REMOTE_PASSWORD));
            options.setDatabaseProperties(DatabaseType.REMOTE, propertiesRemote);

            propertiesEmbedded = new DatabaseProperties();
            propertiesEmbedded.setDriver(bundle.getString(KEY_EMBEDDED_DRIVER));
            propertiesEmbedded.setDataSource(bundle.getString(KEY_EMBEDDED_DATA_SOURCE));
            propertiesEmbedded.setPort(bundle.getString(KEY_EMBEDDED_PORT));
            propertiesEmbedded.setDatabase(bundle.getString(KEY_EMBEDDED_DATABASE));
            propertiesEmbedded.setCredentialsInline(Boolean.valueOf(bundle.getString(KEY_EMBEDDED_INLINE)));
            propertiesEmbedded.setUsername(bundle.getString(KEY_EMBEDDED_USER_NAME));
            propertiesEmbedded.setPassword(bundle.getString(KEY_EMBEDDED_PASSWORD));
            options.setDatabaseProperties(DatabaseType.EMBEDDED, propertiesEmbedded);

            LOGGER.login(KEY_DATABASE_STORE + EQUALS + options.getDataStore().getName());
            LOGGER.login(KEY_DATABASE_TYPE + EQUALS + options.getDatabaseType().getType());
            }

        catch (NullPointerException exception)
            {
            LOGGER.error("configureDatabaseOptions() exception " + exception);
            }

        catch (ClassCastException exception)
            {
            LOGGER.error("configureDatabaseOptions() exception " + exception);
            }
        }


    /***********************************************************************************************
     * Build a connection URL string for the specified DatabaseProperties.
     *
     * @param database
     *
     * @return StringBuffer
     */

    public static StringBuffer buildURL(final DatabaseProperties database)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append(database.getDataSource());
        buffer.append(COLON);
        buffer.append(database.getPort());
        buffer.append(RIGHT_SLASH);
        buffer.append(database.getDatabase());

        if (database.isCredentialsInline())
            {
            appendCredentials(buffer, database);
            }

        return (buffer);
        }


    /***********************************************************************************************
     * Append the USername and Password credentials to the connection string if required.
     *
     * @param buffer
     * @param database
     */

    private static void appendCredentials(final StringBuffer buffer,
                                          final DatabaseProperties database)
        {
        buffer.append(QUERY);
        buffer.append(USER);
        buffer.append(database.getUsername());
        buffer.append(AMPERSAND);
        buffer.append(PASSWD);
        buffer.append(database.getPassword());
        }


    /***********************************************************************************************
     * Construct the AbstractDatabase.
     */

    public AbstractDatabase()
        {
        this.databaseOptions = null;
        this.boolDatabaseActive = false;
        this.boolDebugMode = false;
        this.connDatabase = null;
        this.mySQL = null;
        }


    /***********************************************************************************************
     * Get the database connection.
     *
     * @return Connection
     */

    public final synchronized Connection getConnection()
        {
        return (this.connDatabase);
        }


    /***********************************************************************************************
     * Set the database connection.
     *
     * @param connection
     */

    public final synchronized void setConnection(final Connection connection)
        {
        this.connDatabase = connection;
        }


    /***********************************************************************************************
     * Get the Database Active flag.
     *
     * @return boolean
     */

    public final synchronized boolean getActive()
        {
        return (this.boolDatabaseActive);
        }


    /***********************************************************************************************
     * Set the Database Active flag.
     *
     * @param active
     */

    public final synchronized void setActive(final boolean active)
        {
        boolDatabaseActive = active;
        }


    /***********************************************************************************************
     * Get the DatabaseOptions.
     *
     * @return DatabaseOptions
     */

    public final synchronized DatabaseOptions getDatabaseOptions()
        {
        return (this.databaseOptions);
        }


    /***********************************************************************************************
     * Set the DatabaseOptions.
     *
     * @param options
     */

    public final synchronized void setDatabaseOptions(final DatabaseOptions options)
        {
        this.databaseOptions = options;
        }


    /***********************************************************************************************
     * Get the MysqldResource.
     *
     * @return MysqldResource
     */

    protected final synchronized MysqldResource getMySQL()
        {
        return (this.mySQL);
        }


    /***********************************************************************************************
     * Set the MysqldResource.
     *
     * @param sql
     */

    protected final synchronized void setMySQL(final MysqldResource sql)
        {
        this.mySQL = sql;
        }


    /***********************************************************************************************
     * Get the Debug Mode flag.
     *
     * @return boolean
     */

    public final synchronized boolean getDebugMode()
        {
        return (this.boolDebugMode);
        }


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    public final synchronized void setDebugMode(final boolean flag)
        {
        this.boolDebugMode = flag;
        }
    }
