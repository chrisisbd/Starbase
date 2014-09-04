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
//  27-04-00    LMN updated layout of file
//  02-05-00    LMN made 'final' tidy-up?!
//  18-01-02    LMN started again...
//  21-01-02    LMN added event logging etc.
//  05-02-02    LMN incorporated the column names from each object table
//  26-04-02    LMN added logCoreEvent() and logObjectEvent()
//  15-04-03    LMN changed for Icon columns
//  30-04-03    LMN changed Applications table
//  02-05-03    LMN changed Properties table
//  03-05-03    LMN changed Methods table
//  10-05-03    LMN changed table names for new RegistryModel
//  26-05-03    LMN removed all Locked fields from the database
//  05-06-03    LMN removed logCoreEvent()
//  23-06-03    LMN removed ApplicationID
//  13-07-03    LMN added PropertyClassName
//  28-07-03    LMN changed ClassNames to IDs in PropertyClassNames table
//  19-10-04    LMN changed for DateCreated, TimeCreated, DateModified, TimeModified columns
//  09-11-04    LMN removed LoginPanel to simplify FrameworkLoader
//  22-03-05    LMN tidying up...
//  29-11-05    LMN changing for DatabaseProperties etc.
//  01-03-06    LMN added DataStore to DatabaseOptions
//  03-03-06    LMN extended AbstractDatabase
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.database.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import com.mysql.jdbc.Driver;
import com.mysql.management.MysqldResource;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.database.AbstractDatabase;
import org.lmn.fc.database.DatabaseProperties;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelPlugin;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/***************************************************************************************************
 *
 */

public final class FrameworkDatabase extends AbstractDatabase
    {
    // The FrameworkDatabase is a Singleton!
    private volatile static FrameworkDatabase DATABASE_INSTANCE;


    /***********************************************************************************************
     * The FrameworkDatabase is a Singleton!
     *
     * @return FrameworkDatabase
     */

    public static FrameworkDatabase getInstance()
        {
        if (DATABASE_INSTANCE == null)
            {
            synchronized (FrameworkDatabase.class)
                {
                if (DATABASE_INSTANCE == null)
                    {
                    DATABASE_INSTANCE = new FrameworkDatabase();
                    }
                }
            }

        return (DATABASE_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the FrameworkDatabase.
     */

    private FrameworkDatabase()
        {
        super();
        }


    /***********************************************************************************************
     * Open the Framework database connection using the information in DatabaseOptions.
     */

    public synchronized void openConnection()
        {
        try
            {
            if ((!getActive())
                && (getDatabaseOptions() != null))
                {
                if (DataStore.XML.equals(getDatabaseOptions().getDataStore()))
                    {
                    openXml();
                    }
                else if (DataStore.MYSQL.equals(getDatabaseOptions().getDataStore()))
                    {
                    openMySQL();
                    }
                else if (DataStore.HSQLDB.equals(getDatabaseOptions().getDataStore()))
                    {
                    // ToDo
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_DATASTORE_INVALID);
                    }
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                }
            }

        catch (ClassNotFoundException e)
            {
            // Log the exception if we can...
            // TODo exception
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new SQLException(DATABASE_ALREADYACTIVE),
                                       DATABASE_ALREADYACTIVE,
                                       EventStatus.FATAL);
            }

        catch (SQLException e)
            {
            // Log the exception if we can...
            // TODo exception
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new SQLException(DATABASE_ALREADYACTIVE),
                                       DATABASE_ALREADYACTIVE,
                                       EventStatus.FATAL);
            }

        catch (FrameworkException e)
            {
            // Log the exception if we can...
            // TODo exception
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new SQLException(DATABASE_ALREADYACTIVE),
                                       DATABASE_ALREADYACTIVE,
                                       EventStatus.FATAL);
            }
        }


    /***********************************************************************************************
     * Close the database connection.
     */

    public synchronized void closeConnection()
        {
        try
            {
            if ((getActive())
                && (getDatabaseOptions() != null))
                {
                if (DataStore.XML.equals(getDatabaseOptions().getDataStore()))
                    {
                    closeXml();
                    }
                else if (DataStore.MYSQL.equals(getDatabaseOptions().getDataStore()))
                    {
                    closeMySQL();
                    }
                else if (DataStore.HSQLDB.equals(getDatabaseOptions().getDataStore()))
                    {
                    //ToDo
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_DATASTORE_INVALID);
                    }
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                }
            }

        catch (SQLException e)
            {
            // Log the exception if we can...
            // TODo exception
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new SQLException(DATABASE_ALREADYACTIVE),
                                       DATABASE_ALREADYACTIVE,
                                       EventStatus.FATAL);
            }

        catch (FrameworkException e)
            {
            // Log the exception if we can...
            // TODo exception
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new SQLException(DATABASE_ALREADYACTIVE),
                                       DATABASE_ALREADYACTIVE,
                                       EventStatus.FATAL);
            }
        }


    /***********************************************************************************************
     * Open the XML pseudo-Connection.
     */

    private void openXml() throws FrameworkException
        {
        final DatabaseProperties properties;

        if (!getActive())
            {
           if ((getDatabaseOptions() != null)
               && ((getDatabaseOptions().getDataStore() != null))
               && (getDatabaseOptions().getCurrentDatabaseProperties() != null))
               {
               properties = getDatabaseOptions().getCurrentDatabaseProperties();

               // Assuming that we get here, then set the database as Active...
               setActive(true);

               // Log the event if we can...
               // This should only happen during login, so use login()
               LOGGER.login(METADATA_DATABASE_OPEN
                              + SPACE
                              + METADATA_DATASTORE
                              + getDatabaseOptions().getDataStore().getName()
                              + TERMINATOR
                              + SPACE
                              + METADATA_USER
                              + properties.getUsername()
                              + TERMINATOR);
               }
            else
               {
               throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
               }
            }
        else
            {
            throw new FrameworkException(DATABASE_ALREADYACTIVE);
            }
        }


    /***********************************************************************************************
     * Close the XML pseudo-Connection.
     *
     * @throws FrameworkException
     */

    private void closeXml() throws FrameworkException
        {
        if (getActive())
            {
            // Log the event if we can... before the shutdown!
            if ((getDatabaseOptions() != null)
                && ((getDatabaseOptions().getDataStore() != null))
                && (getDatabaseOptions().getCurrentDatabaseProperties() != null))
                {
                final DatabaseProperties properties;

                properties = getDatabaseOptions().getCurrentDatabaseProperties();

                LOGGER.logTimedEvent(METADATA_DATABASE_CLOSE
                                       + SPACE
                                       + METADATA_DATASTORE
                                       + getDatabaseOptions().getDataStore().getName()
                                       + TERMINATOR
                                       + SPACE
                                       + METADATA_USER
                                       + properties.getUsername()
                                       + TERMINATOR);

                // Make sure we can't shutdown again...
                setActive(false);
                }
            else
               {
               setActive(false);
               throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
               }
            }
        else
            {
            throw new FrameworkException(DATABASE_NOTACTIVE);
            }
        }


    /***********************************************************************************************
     * Open the MySQL Connection.
     */

    private void openMySQL() throws ClassNotFoundException,
                                    SQLException,
                                    FrameworkException
        {
        final DatabaseProperties properties;
        final String strDatabaseDriver;
        final String strDataSource;
        final String strUserName;
        final String strPassword;

       if ((getDatabaseOptions() != null)
           && (getDatabaseOptions().getCurrentDatabaseProperties()) != null)
           {
           properties = getDatabaseOptions().getCurrentDatabaseProperties();

           if (getDatabaseOptions().isEmbedded())
               {
               final File dirMySQL;
               final Map<String, String> mapOptions;

               // Deploy the MySQL database to a folder under the distribution root
               dirMySQL = new File(System.getProperty(KEY_SYSTEM_USER_DIR),
                                   RegistryModelPlugin.FOLDER_DATABASE_MYSQL);
               setMySQL(new MysqldResource(dirMySQL));

               // Tell the database where to find the data...
               mapOptions = new HashMap<String, String>(2);
               mapOptions.put("port", properties.getPort());
               mapOptions.put("server.datadir",
                              System.getProperty(KEY_SYSTEM_USER_DIR)
                                  + RIGHT_SLASH
                                  + RegistryModelPlugin.FOLDER_DATASTORE_MYSQL);

               getMySQL().start(EMBEDDED_DATABASE_THREAD, mapOptions);

               // This should not have been specified in loader.properties
               // ... so use the embedded MySQL JDBC driver
               strDatabaseDriver = Driver.class.getName();
               }
           else
               {
               // Make sure that we don't try to shut down the embedded database
               // ... because there isn't one
               setMySQL(null);

               // Read the specified JDBC driver when the time comes...
               strDatabaseDriver = properties.getDriver();
               }

           // Load the selected driver
           Class.forName(strDatabaseDriver);

           // Form the URL with the credentials inline if required
           strDataSource = buildURL(properties).toString();

           if (properties.isCredentialsInline())
               {
               // The username and password were in the URL...
               strUserName = EMPTY_STRING;
               strPassword = EMPTY_STRING;
               }
           else
               {
               // Specify the username and password separately...
               strUserName = properties.getUsername();
               strPassword = properties.getPassword();
               }

           // Try to connect to the database using the information gathered above
           setConnection(DriverManager.getConnection(strDataSource,
                                                     strUserName,
                                                     strPassword));

           // Assuming that we get here, then set the database as Active...
           setActive(true);

           // Log the event if we can...
           // This should only happen during login, so use login()
           LOGGER.login(METADATA_DATABASE_OPEN
                          + SPACE
                          + METADATA_SOURCE
                          + strDataSource
                          + TERMINATOR
                          + SPACE
                          + METADATA_NAME
                          + strUserName
                          + TERMINATOR);
           }
        else
           {
           throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
           }
        }


    /***********************************************************************************************
     * Close the MySQL connection.
     *
     * @throws SQLException
     * @throws FrameworkException
     */

    private void closeMySQL() throws SQLException,
                                     FrameworkException
        {
        if (getActive())
            {
            // Log the event if we can... before the shutdown!
            if ((getDatabaseOptions() != null)
                && (getDatabaseOptions().getCurrentDatabaseProperties()) != null)
                {
                final DatabaseProperties properties;

                properties = getDatabaseOptions().getCurrentDatabaseProperties();

                LOGGER.logTimedEvent(METADATA_DATABASE_CLOSE
                                       + SPACE
                                       + METADATA_SOURCE
                                       + properties.getDataSource()
                                       + TERMINATOR
                                       + SPACE
                                       + METADATA_NAME
                                       + properties.getUsername()
                                       + TERMINATOR);
                }

            // Shut down the embedded MySQL database, or close the connection
            if ((getMySQL() != null)
                && (getMySQL().isRunning())
                && (getDatabaseOptions() != null)
                && (getDatabaseOptions().isEmbedded()))
                {
                getMySQL().shutdown();
                }
            else
                {
                getConnection().close();
                }

            // Make sure we can't shutdown again...
            setActive(false);
            setMySQL(null);
            }
        else
            {
            throw new FrameworkException(DATABASE_NOTACTIVE);
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File