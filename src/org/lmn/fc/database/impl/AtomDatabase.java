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
//  19-02-04    LMN created file
//  29-11-05    LMN converting for DatabaseProperties
//  03-03-06    LMN changed to AtomDatabase
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.database.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.database.AbstractDatabase;
import org.lmn.fc.database.DatabaseOptions;
import org.lmn.fc.database.DatabaseProperties;
import org.lmn.fc.database.DatabaseType;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;


/***************************************************************************************************
 *
 */

public final class AtomDatabase extends AbstractDatabase
    {
    // The AtomDatabase is a Singleton!
    private volatile static AtomDatabase DATABASE_INSTANCE;

    private AtomPlugin atomPlugin;

    private String strDataSource;
    private String strUserName;
    private String strPassword;


    /***********************************************************************************************
     * Configure the specified DatabaseOptions using properties read from the ResourceKey.
     *
     * @param options
     * @param resourcekey
     * @param isframework
     *
     * @throws MissingResourceException
     */

    public static void configureDatabaseOptions(final DatabaseOptions options,
                                                final String resourcekey,
                                                final boolean isframework) throws MissingResourceException
        {
        try
            {
            final DatabaseProperties propertiesLocal;
            final DatabaseProperties propertiesRemote;
            final DatabaseProperties propertiesEmbedded;
            final String strType;

            System.out.println("look for " + resourcekey + KEY_DATABASE_TYPE);
            // Set the DatabaseType
            strType = REGISTRY.getStringProperty(resourcekey + KEY_DATABASE_TYPE);

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
                // ToDo string
                throw new MissingResourceException("Invalid parameter",
                                                   String.class.getName(),
                                                   resourcekey + KEY_DATABASE_TYPE);
                }

            // Now read each Property
            propertiesLocal = new DatabaseProperties();
            propertiesLocal.setDriver(REGISTRY.getStringProperty(resourcekey + KEY_LOCAL_DRIVER));
            propertiesLocal.setDataSource(REGISTRY.getStringProperty(resourcekey + KEY_LOCAL_DATA_SOURCE));
            propertiesLocal.setPort(REGISTRY.getStringProperty(resourcekey + KEY_LOCAL_PORT));
            propertiesLocal.setDatabase(REGISTRY.getStringProperty(resourcekey + KEY_LOCAL_DATABASE));
            propertiesLocal.setCredentialsInline(REGISTRY.getBooleanProperty(resourcekey + KEY_LOCAL_INLINE));
            propertiesLocal.setUsername(REGISTRY.getStringProperty(resourcekey + KEY_LOCAL_USER_NAME));
            propertiesLocal.setPassword(REGISTRY.getStringProperty(resourcekey + KEY_LOCAL_PASSWORD));
            options.setDatabaseProperties(DatabaseType.LOCAL, propertiesLocal);

            propertiesRemote = new DatabaseProperties();
            propertiesRemote.setDriver(REGISTRY.getStringProperty(resourcekey + KEY_REMOTE_DRIVER));
            propertiesRemote.setDataSource(REGISTRY.getStringProperty(resourcekey + KEY_REMOTE_DATA_SOURCE));
            propertiesRemote.setPort(REGISTRY.getStringProperty(resourcekey + KEY_REMOTE_PORT));
            propertiesRemote.setDatabase(REGISTRY.getStringProperty(resourcekey + KEY_REMOTE_DATABASE));
            propertiesRemote.setCredentialsInline(REGISTRY.getBooleanProperty(resourcekey + KEY_REMOTE_INLINE));
            propertiesRemote.setUsername(REGISTRY.getStringProperty(resourcekey + KEY_REMOTE_USER_NAME));
            propertiesRemote.setPassword(REGISTRY.getStringProperty(resourcekey + KEY_REMOTE_PASSWORD));
            options.setDatabaseProperties(DatabaseType.REMOTE, propertiesRemote);

            propertiesEmbedded = new DatabaseProperties();

            // ToDo have a better check...
            if (isframework)
                {
                propertiesEmbedded.setDriver(REGISTRY.getStringProperty(resourcekey + KEY_EMBEDDED_DRIVER));
                propertiesEmbedded.setDataSource(REGISTRY.getStringProperty(resourcekey + KEY_EMBEDDED_DATA_SOURCE));
                propertiesEmbedded.setPort(REGISTRY.getStringProperty(resourcekey + KEY_EMBEDDED_PORT));
                }
            else
                {
                // Embedded AtomDatabases must use the Framework Driver & DataSource & Port
                propertiesEmbedded.setDriver(LoaderProperties.getInstance().getDatabaseOptions().getCurrentDatabaseProperties().getDriver());
                propertiesEmbedded.setDataSource(LoaderProperties.getInstance().getDatabaseOptions().getCurrentDatabaseProperties().getDataSource());
                propertiesEmbedded.setPort(LoaderProperties.getInstance().getDatabaseOptions().getCurrentDatabaseProperties().getPort());
                }

            // But they will probably use a different properties
            propertiesEmbedded.setDatabase(REGISTRY.getStringProperty(resourcekey + KEY_EMBEDDED_DATABASE));
            propertiesEmbedded.setCredentialsInline(REGISTRY.getBooleanProperty(resourcekey + KEY_EMBEDDED_INLINE));
            propertiesEmbedded.setUsername(REGISTRY.getStringProperty(resourcekey + KEY_EMBEDDED_USER_NAME));
            propertiesEmbedded.setPassword(REGISTRY.getStringProperty(resourcekey + KEY_EMBEDDED_PASSWORD));
            options.setDatabaseProperties(DatabaseType.EMBEDDED, propertiesEmbedded);
            }

        // ToDo do something here?!

        catch (NullPointerException exception)     { }
        catch (ClassCastException exception)       { }
        }


    /***********************************************************************************************
     * The AtomDatabase is a Singleton!
     *
     * @return AtomDatabase
     */

    public static AtomDatabase getInstance()
        {
        if (DATABASE_INSTANCE == null)
            {
            synchronized (AtomDatabase.class)
                {
                if (DATABASE_INSTANCE == null)
                    {
                    DATABASE_INSTANCE = new AtomDatabase();
                    }
                }
            }

        return (DATABASE_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the AtomDatabase.
     */

    private AtomDatabase()
        {
        super();
        }


    /***********************************************************************************************
     * Get the AtomPlugin host for this AtomDatabase.
     *
     * @return AtomPlugin
     */

    public AtomPlugin getHostAtom()
        {
        return (this.atomPlugin);
        }


    /***********************************************************************************************
     * Set the AtomPlugin host for this AtomDatabase.
     *
     * @param plugin
     */

    public void setHostAtom(final AtomPlugin plugin)
        {
        this.atomPlugin = plugin;
        }


    /***********************************************************************************************
     * Open the AtomDatabase connection.
     */

    public synchronized void openConnection()
        {
        final DatabaseProperties database;

        database = getDatabaseOptions().getCurrentDatabaseProperties();

        if ((!getActive())
            && (database != null))
            {
            try
                {
                if (!getDatabaseOptions().isEmbedded())
                    {
                    // Embedded databases must use the Framework
                    // i.e. we do not need to redeploy the database...
                    // Load the specified JDBC driver
                    Class.forName(database.getDriver());
                    }

                // Form the URL with the credentials inline if required
                strDataSource = FrameworkDatabase.buildURL(database).toString();

                if (database.isCredentialsInline())
                    {
                    // The username and password were in the URL...
                    strUserName = EMPTY_STRING;
                    strPassword = EMPTY_STRING;
                    }
                else
                    {
                    // Specify the username and password separately...
                    strUserName = database.getUsername();
                    strPassword = database.getPassword();
                    }
                }

            catch(ClassNotFoundException exception)
                {
                LOGGER.handleAtomException(REGISTRY.getFramework(),
                                           REGISTRY.getFramework().getRootTask(),
                                           this.getClass().getName(),
                                           exception,
                                           DATABASE_FIND_JDBC,
                                           EventStatus.FATAL);
                }

            //--------------------------------------------------------------------------------------
            // Try to connect to the database using the information gathered above

            try
                {
                setConnection(DriverManager.getConnection(strDataSource,
                                                          strUserName,
                                                          strPassword));

                // Assuming that we get here, then set the database as Active...
                setActive(true);

//                LOGGER.logApplicationEvent(REGISTRY.getFramework().getID(),
//                                                                    getHostAtom().getApplicationID(),
//                                                                    getHostAtom().getRootTask().getID(),
//                                                                    getClass().getName(),
//                                                                    METADATA_DATABASE_OPEN
//                                                                        + SPACE
//                                                                        + METADATA_SOURCE
//                                                                        + strDataSource
//                                                                        + TERMINATOR
//                                                                        + SPACE
//                                                                        + METADATA_NAME
//                                                                        + strUserName
//                                                                        + TERMINATOR,
//                                                                    EventStatus.INFO);
                }

            catch (SQLException exception)
                {
                LOGGER.handleAtomException(REGISTRY.getFramework(),
                                           REGISTRY.getFramework().getRootTask(),
                                           this.getClass().getName(),
                                           exception,
                                           DATABASE_OPEN,
                                           EventStatus.FATAL);
                }
            }
        else
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new SQLException(DATABASE_ALREADYACTIVE),
                                       DATABASE_ALREADYACTIVE,
                                       EventStatus.FATAL);
            }
        }


    /***********************************************************************************************
     * Close the AtomDatabase connection, if open.
     */

    public synchronized void closeConnection()
        {
        try
            {
            // Do not attempt to undeploy an embedded database!
            if ((getActive())
                && (getConnection() != null)
                && (!getConnection().isClosed())
                && (getHostAtom() != null))
                {
                final long longTaskID;

                // Close the Application Database connection
                getConnection().close();

                // Do we have a Framework loaded?
                // ToDo Framework context helper method?
                if ((REGISTRY.getFramework() != null)
                    && (REGISTRY.getFramework().getRootTask() != null))
                    {
                    longTaskID = REGISTRY.getFramework().getRootTask().getID();
                    }
                else
                    {
                    // This dummy value will not appear in the EventLog Reports,
                    // but will get written to the database
                    longTaskID = REGISTRY.getFramework().getRootTask().getID();
                    }

                // Use the Framework to log the event
//                LOGGER.logApplicationEvent(REGISTRY.getFramework().getID(),
//                                                                    getHostAtom().getApplicationID(),
//                                                                    longTaskID,
//                                                                    getClass().getName(),
//                                                                    METADATA_DATABASE_CLOSE
//                                                                        + SPACE
//                                                                        + METADATA_SOURCE
//                                                                        + strDataSource
//                                                                        + TERMINATOR
//                                                                        + SPACE
//                                                                        + METADATA_NAME
//                                                                        + strUserName
//                                                                        + TERMINATOR,
//                                                                    EventStatus.INFO);
                setActive(false);
                }
            }

        catch (SQLException exception)
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       exception,
                                       DATABASE_CLOSE,
                                       EventStatus.FATAL);
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
