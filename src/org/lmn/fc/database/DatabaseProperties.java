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


/***************************************************************************************************
 * Encapsulate all properties required to access a database.
 */

public final class DatabaseProperties implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 ResourceKeys
    {
    // String Resources
    private static final String DEFAULT_DRIVER               = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static final String DEFAULT_DATASOURCE           = "jdbc:odbc:JavaDatabase";
    private static final String DEFAULT_LOCAL_PORT           = "3306";
    public static final String DEFAULT_REMOTE_PORT          = "3306";
    public static final String DEFAULT_EMBEDDED_PORT        = "3336";
    private static final String DEFAULT_DATABASE             = "framework";
    private static final boolean DEFAULT_CREDENTIALS_INLINE  = true;
    private static final String DEFAULT_USERNAME             = "";
    private static final String DEFAULT_PASSWORD             = "";

    private String strDriver;
    private String strDataSource;
    private String strPort;
    private String strDatabase;
    private boolean boolCredentialsInline;
    private String strUsername;
    private String strPassword;


    /***********************************************************************************************
     *
     */

    public DatabaseProperties()
        {
        // Set up default Properties
        strDriver = DEFAULT_DRIVER;
        strDataSource = DEFAULT_DATASOURCE;
        strPort = DEFAULT_LOCAL_PORT;
        strDatabase = DEFAULT_DATABASE;
        boolCredentialsInline = DEFAULT_CREDENTIALS_INLINE;
        strUsername = DEFAULT_USERNAME;
        strPassword = DEFAULT_PASSWORD;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getDriver()
        {
        return strDriver;
        }


    /***********************************************************************************************
     *
     * @param driver
     */

    public void setDriver(final String driver)
        {
        this.strDriver = driver;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getDataSource()
        {
        return strDataSource;
        }


    /***********************************************************************************************
     *
     * @param source
     */

    public void setDataSource(final String source)
        {
        this.strDataSource = source;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getPort()
        {
        return strPort;
        }


    /***********************************************************************************************
     *
     * @param port
     */

    public void setPort(final String port)
        {
        this.strPort = port;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getDatabase()
        {
        return strDatabase;
        }


    /***********************************************************************************************
     *
     * @param database
     */

    public void setDatabase(final String database)
        {
        this.strDatabase = database;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isCredentialsInline()
        {
        return boolCredentialsInline;
        }


    /***********************************************************************************************
     *
     * @param inline
     */

    public void setCredentialsInline(final boolean inline)
        {
        this.boolCredentialsInline = inline;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getUsername()
        {
        return strUsername;
        }


    /***********************************************************************************************
     *
     * @param username
     */

    public void setUsername(final String username)
        {
        this.strUsername = username;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getPassword()
        {
        return strPassword;
        }


    /***********************************************************************************************
     *
     * @param password
     */

    public void setPassword(final String password)
        {
        this.strPassword = password;
        }
    }
