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

package org.lmn.fc.model.dao.mysql;

import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.ui.components.EditorUIComponent;


public class QueriesMySqlDAO
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    private static boolean boolBootstrapMode;
    // Column names
    public static final String APPLICATION_ID           = "ApplicationID";
    public static final String COMPONENT_ID             = "ComponentID";
    public static final String FRAMEWORK_QUERY_ID       = "FrameworkQueryID";
    public static final String APPLICATION_QUERY_ID     = "ApplicationQueryID";
    public static final String COMPONENT_QUERY_ID       = "ComponentQueryID";
    public static final String RESOURCE_KEY0            = "ResourceKey0";
    public static final String RESOURCE_KEY1            = "ResourceKey1";
    public static final String RESOURCE_KEY2            = "ResourceKey2";
    public static final String RESOURCE_KEY3            = "ResourceKey3";
    public static final String INSTALLED                = "Installed";
    public static final String EDITOR_ID                = "EditorID";
    public static final String QUERY0                   = "Query0";
    public static final String QUERY1                   = "Query1";
    public static final String QUERY2                   = "Query2";
    public static final String QUERY3                   = "Query3";
    public static final String QUERY_DESCRIPTION        = "QueryDescription";
    public static final String EXECUTION_COUNT          = "ExecutionCount";
    public static final String EXECUTION_TIME           = "ExecutionTime";
    public static final String DATE_CREATED             = "DateCreated";
    public static final String TIME_CREATED             = "TimeCreated";
    public static final String DATE_MODIFIED            = "DateModified";
    public static final String TIME_MODIFIED            = "TimeModified";
    // Column name in the Editors table, joined on EditorID
    public static final String EDITOR_CLASSNAME        = EditorUIComponent.EDITOR_CLASSNAME;
    public static final String SELECT_FRAMEWORK_QUERY_LIST     = "Select.Framework.Query.List";
    public static final String SELECT_FRAMEWORK_QUERY_BYID     = "Select.Framework.Query.ByID";
    public static final String SELECT_FRAMEWORK_QUERY_BYNAME   = "Select.Framework.Query.ByName";
    public static final String UPDATE_FRAMEWORK_QUERY          = "Update.Framework.Query";
    public static final String SELECT_APPLICATION_QUERY_LIST   = "Select.Application.Query.List";
    public static final String SELECT_APPLICATION_QUERY_BYID   = "Select.Application.Query.ByID";
    public static final String SELECT_APPLICATION_QUERY_BYNAME = "Select.Application.Query.ByName";
    public static final String UPDATE_APPLICATION_QUERY        = "Update.Application.Query";
    public static final String SELECT_COMPONENT_QUERY_LIST     = "Select.Component.Query.List";
    public static final String SELECT_COMPONENT_QUERY_BYID     = "Select.Component.Query.ByID";
    public static final String SELECT_COMPONENT_QUERY_BYNAME   = "Select.Component.Query.ByName";
    public static final String UPDATE_COMPONENT_QUERY          = "Update.Component.Query";
    public static final String DEFAULT_LOAD_QUERY_LIST_KEY      = SELECT_FRAMEWORK_QUERY_LIST;
    public static final String DEFAULT_LOAD_QUERY_LIST          = "SELECT * FROM FrameworkQueries WHERE (Installed = True) ORDER BY ResourceKey0, ResourceKey1, ResourceKey2, ResourceKey3";
    public static final String DEFAULT_LOAD_QUERY_BYNAME_KEY    = SELECT_FRAMEWORK_QUERY_BYNAME;
    public static final String DEFAULT_LOAD_QUERY_BYNAME        = "SELECT * FROM FrameworkQueries WHERE (Installed = True) AND (ResourceKey0=?) AND (ResourceKey1=?) AND (ResourceKey2=?) AND (ResourceKey3=?)";

    /***********************************************************************************************
     * Load the bootstrap Queries into the hashtable, before using loadFrameworkQueries().
     *
     * Queries must be provided for:
     * <code>
     * <li>Select.Framework.LoadAtStart
     * <li>Select.Framework.Query.List
     * <li>Select.Framework.Query.ByName
     * </code>
     *
     * @param database
     * @param selectframework
     * @param selectlist
     * @param selectbyname
     * @param debugmode
     */

    public static void setBootstrapQueries(final FrameworkDatabase database,
                                           final String selectframework,
                                           final String selectlist,
                                           final String selectbyname,
                                           final boolean debugmode)
        {
//        QueryData queryData;
//        PreparedStatement statementTemp;
//
//        try
//            {
//            // Tell the loader to use our PreparedStatements
//            boolBootstrapMode = true;
//
//            // Prepare a NON-EDITABLE QueryData for Select.Framework.LoadAtStart
//            queryData = new QueryData(database,
//                                      "Select",
//                                      "Framework",
//                                      "LoadAtStart",
//                                      RegistryModel.TERMINATOR_RESOURCE,
//                                      LanguageData.NO_LANGUAGE,
//                                      null);
//
//            // We must do all initialisation normally done by readResource()
//            queryData.setDebugMode(debugmode);
//            queryData.setID(-1);
//            queryData.setDescription(FrameworkMySqlDAO.SELECT_FRAMEWORK_LOADATSTART);
//            queryData.setCreatedDate(Chronos.getCalendarTimeNow());
//            queryData.setCreatedTime(Chronos.getCalendarTimeNow());
//            queryData.setModifiedDate(Chronos.getCalendarTimeNow());
//            queryData.setModifiedTime(Chronos.getCalendarTimeNow());
//            queryData.setIconFilename(QueryData.QUERY_ICON);
//            queryData.setISOLanguageCode(LanguageData.NO_LANGUAGE);
//            queryData.setDataType(QueryData.QUERY_CLASSNAME);
//            queryData.setEditable(false);
//            queryData.setResource(selectframework.trim());
//            queryData.setExecutionCount(0);
//            queryData.setExecutionTime(0);
//
//            // Set the location in the hierarchy
//            //queryData.setRootPathname("");
//
//            statementTemp = database.getConnection().prepareStatement(selectframework.trim());
//            queryData.setPreparedStatement(statementTemp);
//            REGISTRY.getQueries().put(FrameworkMySqlDAO.SELECT_FRAMEWORK_LOADATSTART, queryData);
//
//            // Prepare a NON-EDITABLE QueryData for Select.Framework.Query.List
//            queryData = new QueryData(database,
//                                      "Select",
//                                      "Framework",
//                                      "Query",
//                                      "List",
//                                      LanguageData.NO_LANGUAGE,
//                                      null);
//
//            // We must do all initialisation normally done by readResource()
//            queryData.setDebugMode(debugmode);
//            queryData.setID(-1);
//            queryData.setDescription(DEFAULT_LOAD_QUERY_LIST_KEY);
//            queryData.setCreatedDate(Chronos.getCalendarTimeNow());
//            queryData.setCreatedTime(Chronos.getCalendarTimeNow());
//            queryData.setModifiedDate(Chronos.getCalendarTimeNow());
//            queryData.setModifiedTime(Chronos.getCalendarTimeNow());
//            queryData.setIconFilename(QueryData.QUERY_ICON);
//            queryData.setISOLanguageCode(LanguageData.NO_LANGUAGE);
//            queryData.setDataType(QueryData.QUERY_CLASSNAME);
//            queryData.setEditable(false);
//            queryData.setResource(selectlist.trim());
//            queryData.setExecutionCount(0);
//            queryData.setExecutionTime(0);
//
//            // Set the location in the hierarchy
//            //queryData.setRootPathname("");
//
//            statementTemp = database.getConnection().prepareStatement(selectlist.trim());
//            queryData.setPreparedStatement(statementTemp);
//            REGISTRY.getQueries().put(DEFAULT_LOAD_QUERY_LIST_KEY, queryData);
//
//            // Prepare a NON-EDITABLE QueryData for Select.Framework.Query.ByName
//            queryData = new QueryData(database,
//                                      "Select",
//                                      "Framework",
//                                      "Query",
//                                      "ByName",
//                                      LanguageData.NO_LANGUAGE,
//                                      null);
//
//            queryData.setDebugMode(debugmode);
//            queryData.setID(-2);
//            queryData.setDescription(DEFAULT_LOAD_QUERY_BYNAME_KEY);
//            queryData.setCreatedDate(Chronos.getCalendarTimeNow());
//            queryData.setCreatedTime(Chronos.getCalendarTimeNow());
//            queryData.setModifiedDate(Chronos.getCalendarTimeNow());
//            queryData.setModifiedTime(Chronos.getCalendarTimeNow());
//            queryData.setIconFilename(QueryData.QUERY_ICON);
//            queryData.setISOLanguageCode(LanguageData.NO_LANGUAGE);
//            queryData.setDataType(QueryData.QUERY_CLASSNAME);
//            queryData.setEditable(false);
//            queryData.setResource(selectbyname.trim());
//            queryData.setExecutionCount(0);
//            queryData.setExecutionTime(0);
//
//            // Set the location in the hierarchy
//            //queryData.setRootPathname("");
//
//
//            statementTemp = database.getConnection().prepareStatement(selectbyname.trim());
//            queryData.setPreparedStatement(statementTemp);
//            REGISTRY.getQueries().put(DEFAULT_LOAD_QUERY_BYNAME_KEY, queryData);
//
//            REGISTRY.showQueries(debugmode);
//            }
//
//        catch(SQLException exception)
//            {
//            // We may not have Queries to drive the database, and we certainly don't have
//            // the dynamic Exception meesages, so fail inelegantly...
//            System.out.println(ExceptionLibrary.LOAD_BOOTSTRAP_QUERIES);
//            }
        }


    /***********************************************************************************************
     * Load all FrameworkQueries from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the FrameworkQueries hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @return DefaultMutableTreeNode
     */

//    public static DefaultMutableTreeNode loadFrameworkQueries(final FrameworkDatabase database,
//                                                              final boolean boolDebugMode)
//        {
//        final DefaultMutableTreeNode nodeQueries;
//
//        // We can only load the FrameworkQueries if the Hashtable is initialised,
//        // and the bootstrap queries have been set
//        if (!boolBootstrapMode)
//            {
//            // We may not have Queries to drive the database
//            // or the dynamic Exception meesages, so fail inelegantly...
//            System.out.println(FrameworkStrings.LOAD_QUERIES);
//            }
//
//        // Create the expander node for the FrameworkQueries
//        nodeQueries = new DefaultMutableTreeNode();
//
//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     null,
//                                     null,
//                                     ResourceLoader.RESOURCE_FRAMEWORK,
//                                     QueryData.class,
//                                     database,
//                                     DEFAULT_LOAD_QUERY_LIST_KEY,
//                                     LanguagePlugin.NO_LANGUAGE,
//                                     nodeQueries,
//                                     QueryData.QUERIES_EXPANDER_NAME,
//                                     QueryData.QUERIES_ICON,
//                                     QueryData.QUERY_ICON,
//                                     REGISTRY.getQueries(),
//                                     boolDebugMode);
//
//        // If we are in bootstrap mode, then link the bootstrap Queries into the node tree
//        if (boolBootstrapMode)
//            {
//            // Todo link nodes - don't know how to do this?? Do we want to anyway??
//            }
//
//        REGISTRY.showQueries(boolDebugMode);
//
//        // We must not return here without resetting the bootstrap queries...
//        boolBootstrapMode = false;
//
//        return (nodeQueries);
//        }
//
//
//    /***********************************************************************************************
//     * Load all ApplicationQueries from the database
//     * <br>Return a DefaultMutableTreeNode which is the root of the ApplicationQueries hierarchy
//     * to be displayed by the FrameworkManager
//     *
//     * @param database
//     * @param application
//     * @return DefaultMutableTreeNode
//     */
//
//    public static DefaultMutableTreeNode loadApplicationQueries(final FrameworkDatabase database,
//                                                                final AtomPlugin application,
//                                                                final boolean boolDebugMode)
//        {
//        final DefaultMutableTreeNode nodeQueries;
//
//        // Create the expander node for the ApplicationQueries
//        nodeQueries = new DefaultMutableTreeNode();
//
//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     application,
//                                     null,
//                                     ResourceLoader.RESOURCE_APPLICATION,
//                                     QueryData.class,
//                                     database,
//                                     SELECT_APPLICATION_QUERY_LIST,
//                                     LanguagePlugin.NO_LANGUAGE,
//                                     nodeQueries,
//                                     QueryData.QUERIES_EXPANDER_NAME,
//                                     QueryData.QUERIES_ICON,
//                                     QueryData.QUERY_ICON,
//                                     REGISTRY.getQueries(),
//                                     boolDebugMode);
//
//        REGISTRY.showQueries(boolDebugMode);
//
//        return (nodeQueries);
//        }
//
//
//    /***********************************************************************************************
//     * Load all ComponentQueries from the database
//     * <br>Return a DefaultMutableTreeNode which is the root of the ComponentQueries hierarchy
//     * to be displayed by the FrameworkManager
//     *
//     * @param database
//     * @param component
//     *
//     * @return DefaultMutableTreeNode
//     */
//
//    public static DefaultMutableTreeNode loadComponentQueries(final FrameworkDatabase database,
//                                                              final AtomPlugin component,
//                                                              final boolean boolDebugMode)
//        {
//        final DefaultMutableTreeNode nodeQueries;
//
//        // Create the expander node for the ComponentQueries
//        nodeQueries = new DefaultMutableTreeNode();
//
//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     (AtomPlugin)component.getParentAtom(),
//                                     component,
//                                     ResourceLoader.RESOURCE_COMPONENT,
//                                     QueryData.class,
//                                     database,
//                                     SELECT_COMPONENT_QUERY_LIST,
//                                     LanguagePlugin.NO_LANGUAGE,
//                                     nodeQueries,
//                                     QueryData.QUERIES_EXPANDER_NAME,
//                                     QueryData.QUERIES_ICON,
//                                     QueryData.QUERY_ICON,
//                                     REGISTRY.getQueries(),
//                                     boolDebugMode);
//
//        REGISTRY.showQueries(boolDebugMode);
//
//        return (nodeQueries);
//        }
//    /***********************************************************************************************
//     * Load the QueryData object with data from the database
//     * ToDO move to DAO etc.....
//     */
//
//    public final void readResource()
//        {
//        final PreparedStatement psQuery;
//        final ResultSet rsResource;
//        QueryPlugin queryPlugin;
//        String strQuery0;
//        String strQuery1;
//        String strQuery2;
//        String strQuery3;
//        String strQuery;
//
//        strQuery0 = "";
//        strQuery1 = "";
//        strQuery2 = "";
//        strQuery3 = "";
//
//        try
//            {
//            queryPlugin = null;
//
//            // Read the full details of the specified Query
//            final RootLevel type;
//
//            type = getLevel();
//
//            if (RootLevel.FRAMEWORK.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.FRAMEWORK_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.SELECT_FRAMEWORK_QUERY_BYID);
//                    psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                        psQuery.setInt(1, this.getID());
//                    }
//                else
//                    {
//                    // No QueryID, so we must use the Level names
//                    queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.SELECT_FRAMEWORK_QUERY_BYNAME);
//                    psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
//                    psQuery.setString(1, this.getResourceKey0());
//                    psQuery.setString(2, this.getResourceKey1());
//                    psQuery.setString(3, this.getResourceKey2());
//                    psQuery.setString(4, this.getResourceKey3());
//                    }
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.APPLICATION_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.SELECT_APPLICATION_QUERY_BYID);
//                    psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                        psQuery.setInt(1, this.getApplicationID());
////                        psQuery.setInt(2, this.getID());
//                    }
//                else
//                    {
//                    // No QueryID, so we must use the Level names
//                    queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.SELECT_APPLICATION_QUERY_BYNAME);
//                    psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                        psQuery.setInt(1, this.getApplicationID());
//                    psQuery.setString(2, this.getResourceKey0());
//                    psQuery.setString(3, this.getResourceKey1());
//                    psQuery.setString(4, this.getResourceKey2());
//                    psQuery.setString(5, this.getResourceKey3());
//                   }
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.COMPONENT_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.SELECT_COMPONENT_QUERY_BYID);
//                    psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                        psQuery.setInt(1, this.getApplicationID());
////                        psQuery.setInt(2, this.getPointID());
////                        psQuery.setInt(3, this.getID());
//                    }
//                else
//                    {
//                    // No QueryID, so we must use the Level names
//                    queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.SELECT_COMPONENT_QUERY_BYNAME);
//                    psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                        psQuery.setInt(1, this.getApplicationID());
////                        psQuery.setInt(2, this.getPointID());
//                    psQuery.setString(3, this.getResourceKey0());
//                    psQuery.setString(4, this.getResourceKey1());
//                    psQuery.setString(5, this.getResourceKey2());
//                    psQuery.setString(6, this.getResourceKey3());
//                   }
//                }
//            else
//                {
//                psQuery = null;
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException("[querytype=" + getLevel() + "]"),
//                                           ExceptionLibrary.INVALID_QUERY_TYPE,
//                                           EventStatus.FATAL);
//                }
//
//            if (queryPlugin != null)
//                {
//                rsResource = queryPlugin.executeQuery(this,
//                                                      psQuery,
//                                                      RegistryModel.getInstance().getSqlTrace(),
//                                                      RegistryModel.getInstance().getSqlTiming());
//
//                // Check here for no Query data at all!
//                rsResource.next();
//
//                // Prepare the User Object for the TreeNode
//                // Change here if the Query table designs change!
//                suspendUpdates();
//
//                // Errors in RootType will have already been trapped...
//                if (RootLevel.FRAMEWORK.equals(type))
//                    {
//                    setID(rsResource.getInt(QueriesMySqlDAO.FRAMEWORK_QUERY_ID));
//                    strQuery0 = rsResource.getString(QueriesMySqlDAO.QUERY0);
//                    strQuery1 = rsResource.getString(QueriesMySqlDAO.QUERY1);
//                    strQuery2 = rsResource.getString(QueriesMySqlDAO.QUERY2);
//                    strQuery3 = rsResource.getString(QueriesMySqlDAO.QUERY3);
//                    }
//                else if (RootLevel.APPLICATION.equals(type))
//                    {
////                        setApplicationID(rsResource.getInt(APPLICATION_ID));
//                    setID(rsResource.getInt(QueriesMySqlDAO.APPLICATION_QUERY_ID));
//                    strQuery0 = rsResource.getString(QueriesMySqlDAO.QUERY0);
//                    strQuery1 = rsResource.getString(QueriesMySqlDAO.QUERY1);
//                    strQuery2 = rsResource.getString(QueriesMySqlDAO.QUERY2);
//                    strQuery3 = rsResource.getString(QueriesMySqlDAO.QUERY3);
//                    }
//                else if (RootLevel.COMPONENT.equals(type))
//                    {
////                        setApplicationID(rsResource.getInt(APPLICATION_ID));
////                        setComponentID(rsResource.getInt(COMPONENT_ID));
//                    setID(rsResource.getInt(QueriesMySqlDAO.COMPONENT_QUERY_ID));
//                    strQuery0 = rsResource.getString(QueriesMySqlDAO.QUERY0);
//                    strQuery1 = rsResource.getString(QueriesMySqlDAO.QUERY1);
//                    strQuery2 = rsResource.getString(QueriesMySqlDAO.QUERY2);
//                    strQuery3 = rsResource.getString(QueriesMySqlDAO.QUERY3);
//                    }
//                else
//                    {
//                    LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                               REGISTRY.getFramework().getRootTask(),
//                                               this.getClass().getName(),
//                                               new FrameworkException("[querytype=" + getLevel() + "]"),
//                                               ExceptionLibrary.INVALID_QUERY_TYPE,
//                                               EventStatus.FATAL);
//                    }
//
//                // Build up the full Query string
//                strQuery = "";
//                if (strQuery0 != null)
//                    {
//                    strQuery += strQuery0;
//                    }
//
//                if (strQuery1 != null)
//                    {
//                    strQuery += strQuery1;
//                    }
//
//                if (strQuery2 != null)
//                    {
//                    strQuery += strQuery2;
//                    }
//
//                if (strQuery3 != null)
//                    {
//                    strQuery += strQuery3;
//                    }
//
//                // Set the ResourceData from the database
//                setResourceKey0(rsResource.getString(QueriesMySqlDAO.RESOURCE_KEY0));
//                setResourceKey1(rsResource.getString(QueriesMySqlDAO.RESOURCE_KEY1));
//                setResourceKey2(rsResource.getString(QueriesMySqlDAO.RESOURCE_KEY2));
//                setResourceKey3(rsResource.getString(QueriesMySqlDAO.RESOURCE_KEY3));
//                setInstalled(rsResource.getBoolean(QueriesMySqlDAO.INSTALLED));
//                setEditorClassname(rsResource.getString(QueriesMySqlDAO.EDITOR_CLASSNAME));
//                setResource(strQuery);
//                setDescription(rsResource.getString(QueriesMySqlDAO.QUERY_DESCRIPTION));
//                setExecutionCount(rsResource.getLong(QueriesMySqlDAO.EXECUTION_COUNT));
//                setExecutionTime(rsResource.getLong(QueriesMySqlDAO.EXECUTION_TIME));
//                setCreatedDate(rsResource.getDate(QueriesMySqlDAO.DATE_CREATED));
//                setCreatedTime(rsResource.getTime(QueriesMySqlDAO.TIME_CREATED));
//                setModifiedDate(rsResource.getDate(QueriesMySqlDAO.DATE_MODIFIED));
//                setModifiedTime(rsResource.getTime(QueriesMySqlDAO.TIME_MODIFIED));
//
//                // Initialise the extra fields which are not stored in the FrameworkDatabase
//                setEditable(true);
//                setDataType(QUERY_CLASSNAME);
//                setISOLanguageCode(LanguagePlugin.NO_LANGUAGE);
//
//                rsResource.close();
//                }
//
//            resumeUpdates();
//            setUpdated(false);
//            }
//
//        catch (SQLException exception)
//            {
//            // Errors in RootType will have already been trapped...
//            final RootLevel type;
//
//            type = getLevel();
//
//            if (RootLevel.FRAMEWORK.equals(type))
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString() + " [resourceid=" +  getID() + "]"),
//                                           ExceptionLibrary.LOAD_FRAMEWORK_QUERIES,
//                                           EventStatus.FATAL);
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString()
//                                                                     + " [applicationid=" + 1 + "]"
//                                                                     + " [resourceid=" +  getID() + "]"),
//                                           ExceptionLibrary.LOAD_APPLICATION_QUERIES,
//                                           EventStatus.FATAL);
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString()
//                                                                     + " [applicationid=" + 1 + "]"
//                                                                     + " [componentid=" + 2 + "]"
//                                                                     + " [resourceid=" +  getID() + "]"),
//                                           ExceptionLibrary.LOAD_COMPONENT_QUERIES,
//                                           EventStatus.FATAL);
//                }
//            else
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString() + " [querytype=" + getLevel() + "]"),
//                                           ExceptionLibrary.INVALID_QUERY_TYPE,
//                                           EventStatus.FATAL);
//                }
//            }
//        }
//
//
//    /***********************************************************************************************
//     * Write the QueryData object back to the database.
//     * Split the Query into four Strings of length QUERY_ATOM_LENGTH.
//     */
//
//    public final void writeResource()
//        {
//        final PreparedStatement psQuery;
//        final String strQuery;
//        String strQuery0;
//        String strQuery1;
//        String strQuery2;
//        String strQuery3;
//        int intParameter;
//        final int intParameterOffset;
//        QueryPlugin queryPlugin;
//
//        // Read the Resource, which may be of any length at this stage
//        strQuery = (String)getResource();
//
//        // Check that it is possible to store the Query successfully...
//        // Normally this would be trapped by a QueryEditor before getting this far
//        if (strQuery.length() > (QUERY_LENGTH))
//            {
//            LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                       REGISTRY.getFramework().getRootTask(),
//                                       this.getClass().getName(),
//                                       new FrameworkException("[key=" + getPathname() + "]"
//                                                                 + " [resource=" + getResource() + "] "
//                                                                 + " [length=" + ((String)getResource()).length() + "]" ),
//                                       ExceptionLibrary.RESOURCE_LENGTH,
//                                       EventStatus.FATAL);
//            }
//
//        try
//            {
//            queryPlugin = null;
//
//            // WARNING!! The parameters are added out of natural order for convenience
//            // BUT this offset MUST be kept in step with changes to the fields...
//            intParameterOffset = PARAMETER_OFFSET;
//
//            // Write the full details of the specified Query
//            final RootLevel type;
//
//            type = getLevel();
//
//            if (RootLevel.FRAMEWORK.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.FRAMEWORK_RESOURCE]");
//
//                queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.UPDATE_FRAMEWORK_QUERY);
//                psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                    psQuery.setInt(intParameterOffset, this.getID());
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.APPLICATION_RESOURCE]");
//
//                queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.UPDATE_APPLICATION_QUERY);
//                psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                    psQuery.setInt(intParameterOffset++, this.getApplicationID());
////                    psQuery.setInt(intParameterOffset, this.getID());
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.COMPONENT_RESOURCE]");
//
//                queryPlugin = REGISTRY.getQueryData(QueriesMySqlDAO.UPDATE_COMPONENT_QUERY);
//                psQuery = queryPlugin.getPreparedStatement(DataStore.MYSQL);
////                    psQuery.setInt(intParameterOffset++, this.getApplicationID());
////                    psQuery.setInt(intParameterOffset++, this.getPointID());
////                    psQuery.setInt(intParameterOffset, this.getID());
//                }
//            else
//                {
//                psQuery = null;
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException("[querytype=" + getLevel() + "]"),
//                                           ExceptionLibrary.INVALID_QUERY_TYPE,
//                                           EventStatus.FATAL);
//                }
//
//            // Now split up the Query to fit into the FrameworkDatabase (sigh...)
//            strQuery0 = strQuery;
//            strQuery1 = SPACE;
//            strQuery2 = SPACE;
//            strQuery3 = SPACE;
//
//            if (strQuery0.length() > QUERY_ATOM_LENGTH)
//                {
//                strQuery1 = strQuery0.substring(QUERY_ATOM_LENGTH);
//                strQuery0 = strQuery0.substring(0, QUERY_ATOM_LENGTH);
//
//                if (strQuery1.length() > QUERY_ATOM_LENGTH)
//                    {
//                    strQuery2 = strQuery1.substring(QUERY_ATOM_LENGTH);
//                    strQuery1 = strQuery1.substring(0, QUERY_ATOM_LENGTH);
//
//                    if (strQuery2.length() > QUERY_ATOM_LENGTH)
//                        {
//                        strQuery3 = strQuery2.substring(QUERY_ATOM_LENGTH);
//                        strQuery2 = strQuery2.substring(0, QUERY_ATOM_LENGTH);
//                        }
//                    }
//                }
//
//            showDebugMessage(".writeResource Query0 [" + strQuery0 + "]");
//            showDebugMessage(".writeResource Query1 [" + strQuery1 + "]");
//            showDebugMessage(".writeResource Query2 [" + strQuery2 + "]");
//            showDebugMessage(".writeResource Query3 [" + strQuery3 + "]");
//
//            intParameter = 1;
//
//            if (psQuery != null)
//                {
//                psQuery.setString(intParameter++, getResourceKey0());
//                psQuery.setString(intParameter++, getResourceKey1());
//                psQuery.setString(intParameter++, getResourceKey2());
//                psQuery.setString(intParameter++, getResourceKey3());
//                psQuery.setBoolean(intParameter++, isInstalled());
//                psQuery.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psQuery.setString(intParameter++, strQuery0);
//                psQuery.setString(intParameter++, strQuery1);
//                psQuery.setString(intParameter++, strQuery2);
//                psQuery.setString(intParameter++, strQuery3);
//                psQuery.setString(intParameter++, getDescription());
//                psQuery.setLong(intParameter++, getExecutionCount());
//                psQuery.setLong(intParameter++, getExecutionTime());
//                psQuery.setDate(intParameter++, getCreatedDate());
//                psQuery.setTime(intParameter++, getCreatedTime());
//                psQuery.setDate(intParameter++, getModifiedDate());
//                psQuery.setTime(intParameter++, getModifiedTime());
//
//                // Double check the odd parameter order...
//                if (intParameter != PARAMETER_OFFSET)
//                    {
//                    throw new SQLException("Parameter offset is incorrect!");
//                    }
//
//                if (queryPlugin != null)
//                    {
//                    queryPlugin.executeUpdate(this,
//                                              psQuery,
//                                              RegistryModel.getInstance().getSqlTrace(),
//                                              RegistryModel.getInstance().getSqlTiming());
//                    }
//
//                // Record the fact that we have written all of the data for this Query
//                setUpdated(false);
//                }
//            }
//
//        catch (SQLException exception)
//            {
//            // Errors in RootType will have already been trapped...
//            final RootLevel type;
//
//            type = getLevel();
//
//            if (RootLevel.FRAMEWORK.equals(type))
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString() + " [resourceid=" +  getID() + "]"),
//                                           ExceptionLibrary.SAVE_FRAMEWORK_QUERIES,
//                                           EventStatus.FATAL);
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString()
//                                                                     + " [applicationid=" + 1 + "]"
//                                                                     + " [resourceid=" +  getID() + "]"),
//                                           ExceptionLibrary.SAVE_APPLICATION_QUERIES,
//                                           EventStatus.FATAL);
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString()
//                                                                     + " [applicationid=" + 1 + "]"
//                                                                     + " [componentid=" + 2 + "]"
//                                                                     + " [resourceid=" +  getID() + "]"),
//                                           ExceptionLibrary.SAVE_COMPONENT_QUERIES,
//                                           EventStatus.FATAL);
//                }
//            else
//                {
//                LOGGER.handleAtomException(REGISTRY.getFramework(),
//                                           REGISTRY.getFramework().getRootTask(),
//                                           this.getClass().getName(),
//                                           new FrameworkException(exception.toString() + " [querytype=" + getLevel() + "]"),
//                                           ExceptionLibrary.INVALID_QUERY_TYPE,
//                                           EventStatus.FATAL);
//                }
//            }
//        }
//
//
//    /***********************************************************************************************
//     * Create the SQL script to delete this QueryData.
//     *
//     * @return StringBuffer
//     */
//
//    public final StringBuffer createSQLDeleteScript()
//        {
//        final StringBuffer bufferScript;
//
//        bufferScript = new StringBuffer();
//
//        bufferScript.append("ToDo !!!!!!!!!!!!!!!!!!!!!");
//
//        return (bufferScript);
//        }
//
//
//    /***********************************************************************************************
//     * Create the SQL script to insert this QueryData.
//     *
//     * @return StringBuffer
//     */
//
//    public final StringBuffer createSQLInsertScript()
//        {
//        final StringBuffer bufferScript;
//
//        bufferScript = new StringBuffer();
//
//        bufferScript.append("ToDo !!!!!!!!!!!!!!!!!!!!!");
//
//        return(bufferScript);
//        }
//

    }
