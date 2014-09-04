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

package org.lmn.fc.common.loaders;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.database.DatabaseOptions;


/***************************************************************************************************
 * Encapsulate all LoaderProperties read from the file loader.properties.
 */

public final class LoaderProperties implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               ResourceKeys
    {
    private static final String DEFAULT_LOAD_FRAMEWORK      = "SELECT * FROM Editors INNER JOIN Frameworks ON Editors.EditorID = Frameworks.EditorID WHERE (FrameworkActive=1) AND (FrameworkLoadAtStart=1) ORDER BY FrameworkID";

    // The LoaderProperties is a Singleton!
    private volatile static LoaderProperties PROPERTIES_INSTANCE;

    private String strJmxUsername;
    private String strJmxPassword;
    private String strJmxPort;
    private DatabaseOptions databaseOptions;

    private boolean boolMasterDebug;
    private boolean boolDebugStaribus;
    private boolean boolDebugStarinet;
    private boolean boolDebugTiming;
    private boolean boolDebugState;
    private boolean boolDebugMetadata;
    private boolean boolDebugChart;
    private boolean boolDebugThreads;
    private boolean boolToolbar;
    private boolean boolCommandMacros;
    private boolean boolValidationXML;
    private boolean boolCommandVariant;

    private boolean boolSqlTrace;
    private boolean boolSqlTiming;
    private String strQueryLoadFramework;


    /***********************************************************************************************
     * The LoaderProperties is a Singleton!
     *
     * @return LoaderProperties
     */

    public static LoaderProperties getInstance()
        {
        if (PROPERTIES_INSTANCE == null)
            {
            synchronized (LoaderProperties.class)
                {
                if (PROPERTIES_INSTANCE == null)
                    {
                    PROPERTIES_INSTANCE = new LoaderProperties();
                    }
                }
            }

        return (PROPERTIES_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the LoaderProperties.
     */

    private LoaderProperties()
        {
        strJmxUsername = EMPTY_STRING;
        strJmxPassword = EMPTY_STRING;
        strJmxPort = EMPTY_STRING;
        databaseOptions = new DatabaseOptions();
        strQueryLoadFramework = DEFAULT_LOAD_FRAMEWORK;
        boolMasterDebug = false;
        boolDebugStaribus = false;
        boolDebugStarinet = false;
        boolDebugTiming = false;
        boolDebugState = false;
        boolDebugMetadata = false;
        boolDebugChart = false;
        boolDebugThreads = false;
        boolToolbar = false;
        boolCommandMacros = false;
        boolValidationXML = false;
        boolCommandVariant = false;
        boolSqlTrace = false;
        boolSqlTiming = false;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getJmxUsername()
        {
        return strJmxUsername;
        }


    /***********************************************************************************************
     *
     * @param username
     */

    public void setJmxUsername(final String username)
        {
        this.strJmxUsername = username;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getJmxPassword()
        {
        return strJmxPassword;
        }


    /***********************************************************************************************
     *
     * @param password
     */

    public void setJmxPassword(final String password)
        {
        this.strJmxPassword = password;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getJmxPort()
        {
        return strJmxPort;
        }


    /***********************************************************************************************
     *
     * @param port
     */

    public void setJmxPort(final String port)
        {
        this.strJmxPort = port;
        }


    /***********************************************************************************************
     * Get the DatabaseOptions.
     *
     * @return DatabaseOptions
     */

    public DatabaseOptions getDatabaseOptions()
        {
        return databaseOptions;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isMasterDebug()
        {
        return boolMasterDebug;
        }


    /***********************************************************************************************
     *
     * @param debug
     */

    public void setMasterDebug(final boolean debug)
        {
        this.boolMasterDebug = debug;
        }


    /***********************************************************************************************
     * Get the Staribus (RS485) Debug flag.
     *
     * @return boolean
     */

    public boolean isStaribusDebug()
        {
        return boolDebugStaribus;
        }


    /***********************************************************************************************
     * Indicate that we are in Staribus (RS485) Debug mode.
     *
     * @param debug
     */

    public void setStaribusDebug(final boolean debug)
        {
        this.boolDebugStaribus = debug;
        }


    /***********************************************************************************************
     * Get the Starinet (Ethernet) Debug flag.
     *
     * @return boolean
     */

    public boolean isStarinetDebug()
        {
        return (this.boolDebugStarinet);
        }


    /***********************************************************************************************
     * Indicate that we are in Starinet (Ethernet) Debug mode.
     *
     * @param debug
     */

    public void setStarinetDebug(final boolean debug)
        {
        this.boolDebugStarinet = debug;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isTimingDebug()
        {
        return boolDebugTiming;
        }


    /***********************************************************************************************
     *
     * @param timing
     */

    public void setTimingDebug(final boolean timing)
        {
        this.boolDebugTiming = timing;
        }


    /***********************************************************************************************
     * Indicate if we are in State Debug mode.
     *
     * @return boolean
     */

    public boolean isStateDebug()
        {
        return boolDebugState;
        }


    /***********************************************************************************************
     * Indicate if we are in State Debug mode.
     *
     * @param state
     */

    public void setStateDebug(final boolean state)
        {
        this.boolDebugState = state;
        }


    /***********************************************************************************************
     * Indicate if we are in Metadata Debug mode.
     *
     * @return boolean
     */

    public boolean isMetadataDebug()
        {
        return boolDebugMetadata;
        }


    /***********************************************************************************************
     * Indicate if we are in Metadata Debug mode.
     *
     * @param metadatadebug
     */

    public void setMetadataDebug(final boolean metadatadebug)
        {
        this.boolDebugMetadata = metadatadebug;
        }


    /***********************************************************************************************
     * Indicate if we are in Chart Debug mode.
     *
     * @return boolean
     */

    public boolean isChartDebug()
        {
        return boolDebugChart;
        }


    /***********************************************************************************************
     * Indicate if we are in Chart Debug mode.
     *
     * @param chartdebug
     */

    public void setChartDebug(final boolean chartdebug)
        {
        this.boolDebugChart = chartdebug;
        }


    /***********************************************************************************************
     * Indicate if we are in Threads Debug mode.
     *
     * @return boolean
     */

    public boolean isThreadsDebug()
        {
        return boolDebugThreads;
        }


    /***********************************************************************************************
     * Indicate if we are in Threads Debug mode.
     *
     * @param threadsdebug
     */

    public void setThreadsDebug(final boolean threadsdebug)
        {
        this.boolDebugThreads = threadsdebug;
        }


    /***********************************************************************************************
     * Indicate if Command Macros are available.
     *
     * @return boolean
     */

    public boolean isCommandMacros()
        {
        return boolCommandMacros;
        }


    /***********************************************************************************************
     * Indicate if Command Macros are available.
     *
     * @param enable
     */

    public void setCommandMacros(final boolean enable)
        {
        this.boolCommandMacros = enable;
        }


    /***********************************************************************************************
     * Indicate if the Framework Toolbar is displayed.
     *
     * @return boolean
     */

    public boolean isToolbarDisplayed()
        {
        return (this.boolToolbar);
        }


    /***********************************************************************************************
     * Indicate if the Framework Toolbar is displayed.
     *
     * @param enable
     */

    public void setToolbarDisplayed(final boolean enable)
        {
        this.boolToolbar = enable;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isValidationXML()
        {
        return boolValidationXML;
        }


    /***********************************************************************************************
     *
     * @param enable
     */

    public void setValidationXML(final boolean enable)
        {
        this.boolValidationXML = enable;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isCommandVariant()
        {
        return boolCommandVariant;
        }


    /***********************************************************************************************
     *
     * @param enable
     */

    public void setCommandVariant(final boolean enable)
        {
        this.boolCommandVariant = enable;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isSqlTrace()
        {
        return boolSqlTrace;
        }


    /***********************************************************************************************
     *
     * @param sql
     */

    public void setSqlTrace(final boolean sql)
        {
        this.boolSqlTrace = sql;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public boolean isSqlTiming()
        {
        return boolSqlTiming;
        }


    /***********************************************************************************************
     *
     * @param timing
     */

    public void setSqlTiming(final boolean timing)
        {
        this.boolSqlTiming = timing;
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getLoadFrameworkQuery()
        {
        return strQueryLoadFramework;
        }


    /***********************************************************************************************
     *
     * @param query
     */

    public void setLoadFrameworkQuery(final String query)
        {
        this.strQueryLoadFramework = query;
        }
    }
