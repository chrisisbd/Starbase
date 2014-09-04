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
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.ui.components.EditorUIComponent;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExceptionsSqlDAO
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    // Column names in Exceptions Tables
    public static final String FRAMEWORK_EXCEPTIONS_TABLE       = "FrameworkExceptions";
    public static final String APPLICATION_EXCEPTIONS_TABLE     = "ApplicationExceptions";
    public static final String COMPONENT_EXCEPTIONS_TABLE       = "ComponentExceptions";
    public static final String APPLICATION_ID           = "ApplicationID";
    public static final String COMPONENT_ID             = "ComponentID";
    public static final String FRAMEWORK_EXCEPTION_ID   = "FrameworkExceptionID";
    public static final String APPLICATION_EXCEPTION_ID = "ApplicationExceptionID";
    public static final String COMPONENT_EXCEPTION_ID   = "ComponentExceptionID";
    public static final String RESOURCE_KEY0            = "ResourceKey0";
    public static final String RESOURCE_KEY1            = "ResourceKey1";
    public static final String RESOURCE_KEY2            = "ResourceKey2";
    public static final String RESOURCE_KEY3            = "ResourceKey3";
    public static final String LANGUAGE_ISOCODE         = "LanguageCode";
    public static final String INSTALLED                = "Installed";
    public static final String EDITOR_ID                = "EditorID";
    public static final String FRAMEWORK_EXCEPTION      = "FrameworkException";
    public static final String APPLICATION_EXCEPTION    = "AtomException";
    public static final String COMPONENT_EXCEPTION      = "ComponentException";
    public static final String DESCRIPTION              = "ExceptionDescription";
    public static final String DATE_CREATED             = "DateCreated";
    public static final String TIME_CREATED             = "TimeCreated";
    public static final String DATE_MODIFIED            = "DateModified";
    public static final String TIME_MODIFIED            = "TimeModified";
    // Column name in the Editors table, joined on EditorID
    public static final String EDITOR_CLASSNAME        = EditorUIComponent.EDITOR_CLASSNAME;
    // Queries
    public static final String SELECT_FRAMEWORK_EXCEPTION_LIST         = "Select.Framework.Exception.List";
    public static final String SELECT_FRAMEWORK_EXCEPTION_BYID         = "Select.Framework.Exception.ByID";
    public static final String SELECT_FRAMEWORK_EXCEPTION_BYNAME       = "Select.Framework.Exception.ByName";
    public static final String UPDATE_FRAMEWORK_EXCEPTION              = "Update.Framework.Exception";
    public static final String SELECT_APPLICATION_EXCEPTION_LIST       = "Select.Application.Exception.List";
    public static final String SELECT_APPLICATION_EXCEPTION_BYID       = "Select.Application.Exception.ByID";
    public static final String SELECT_APPLICATION_EXCEPTION_BYNAME     = "Select.Application.Exception.ByName";
    public static final String UPDATE_APPLICATION_EXCEPTION            = "Update.Application.Exception";
    public static final String SELECT_COMPONENT_EXCEPTION_LIST         = "Select.Component.Exception.List";
    public static final String SELECT_COMPONENT_EXCEPTION_BYID         = "Select.Component.Exception.ByID";
    public static final String SELECT_COMPONENT_EXCEPTION_BYNAME       = "Select.Component.Exception.ByName";
    public static final String UPDATE_COMPONENT_EXCEPTION              = "Update.Component.Exception";

    /***********************************************************************************************
     * Load all FrameworkExceptions from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the FrameworkException hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param languagecode
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadFrameworkExceptions(final FrameworkDatabase database,
                                                                 final String languagecode,
                                                                 final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeExceptions;

        // Create the expander node for the FrameworkExceptions
        nodeExceptions = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     null,
//                                     null,
//                                     ResourceLoader.RESOURCE_FRAMEWORK,
//                                     ExceptionData.class,
//                                     database,
//                                     SELECT_FRAMEWORK_EXCEPTION_LIST,
//                                     languagecode,
//                                     nodeExceptions, ExceptionData.EXCEPTIONS_EXPANDER_NAME,
//                                     ExceptionData.EXCEPTIONS_ICON,
//                                     ExceptionData.EXCEPTION_ICON,
//                                     ExceptionData.hashtableExceptions,
//                                     debugmode);

        REGISTRY.showExceptions(debugmode);

        return (nodeExceptions);
        }

    /***********************************************************************************************
     * Load the ExceptionData object with data from the database
     * <br>Resources are retrieved for the language specified in the constructor
     */

    public final void readResource()
        {
//        final PreparedStatement psException;
//        final ResultSet rsResource;
//        int intParameter;
//        QueryData queryData;
//
//        try
//            {
//            queryData = null;
//
//            // Read the full details of the specified Exception
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
//                    queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.SELECT_FRAMEWORK_EXCEPTION_BYID);
//                    psException = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psException.setInt(intParameter++, this.getID());
//                    psException.setString(intParameter, this.strLanguage);
//                    }
//                else
//                    {
//                    // No ExceptionID, so we must use the Level names
//                    queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.SELECT_FRAMEWORK_EXCEPTION_BYNAME);
//                    psException = queryData.getPreparedStatement();
//
//                    intParameter = 1;
//                    psException.setString(intParameter++, this.getResourceKey0());
//                    psException.setString(intParameter++, this.getResourceKey1());
//                    psException.setString(intParameter++, this.getResourceKey2());
//                    psException.setString(intParameter++, this.getResourceKey3());
//                    psException.setString(intParameter, this.strLanguage);
//                    }
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.APPLICATION_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.SELECT_APPLICATION_EXCEPTION_BYID);
//                    psException = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psException.setInt(intParameter++, this.getApplicationID());
////                        psException.setInt(intParameter++, this.getID());
//                    psException.setString(intParameter, this.strLanguage);
//                    }
//                else
//                    {
//                    // No ExceptionID, so we must use the Level names
//                    queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.SELECT_APPLICATION_EXCEPTION_BYNAME);
//                    psException = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psException.setInt(intParameter++, this.getApplicationID());
//                    psException.setString(intParameter++, this.getResourceKey0());
//                    psException.setString(intParameter++, this.getResourceKey1());
//                    psException.setString(intParameter++, this.getResourceKey2());
//                    psException.setString(intParameter++, this.getResourceKey3());
//                    psException.setString(intParameter, this.strLanguage);
//                   }
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.COMPONENT_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.SELECT_COMPONENT_EXCEPTION_BYID);
//                    psException = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psException.setInt(intParameter++, this.getApplicationID());
////                        psException.setInt(intParameter++, this.getPointID());
////                        psException.setInt(intParameter++, this.getID());
//                    psException.setString(intParameter, this.strLanguage);
//                    }
//                else
//                    {
//                    // No ExceptionID, so we must use the Level names
//                    queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.SELECT_COMPONENT_EXCEPTION_BYNAME);
//                    psException = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psException.setInt(intParameter++, this.getApplicationID());
////                        psException.setInt(intParameter++, this.getPointID());
//                    psException.setString(intParameter++, this.getResourceKey0());
//                    psException.setString(intParameter++, this.getResourceKey1());
//                    psException.setString(intParameter++, this.getResourceKey2());
//                    psException.setString(intParameter++, this.getResourceKey3());
//                    psException.setString(intParameter, this.strLanguage);
//                   }
//                }
//            else
//                {
//                psException = null;
//                ExceptionLibrary.handleAtomException(new FrameworkException("[exceptiontype=" + getLevel() + "]"),
//                                                          0,
//                                                          REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                          EventStatus.FATAL);
//                }
//
//            if (queryData != null)
//                {
//                rsResource = queryData.executeQuery(this,
//                                                    psException,
//                                                    RegistryModel.getInstance().getSqlTrace(),
//                                                    RegistryModel.getInstance().getSqlTiming());
//
//                // Check here for no Exception data at all!
//                rsResource.next();
//
//                // Prepare the User Object for the TreeNode
//                // Change here if the Exception table designs change!
//                suspendUpdates();
//
//                // Errors in RootType will have already been trapped...
//                if (RootLevel.FRAMEWORK.equals(type))
//                    {
//                    setID(rsResource.getInt(ExceptionsSqlDAO.FRAMEWORK_EXCEPTION_ID));
//                    setResource(rsResource.getString(ExceptionsSqlDAO.FRAMEWORK_EXCEPTION));
//                    }
//                else if (RootLevel.APPLICATION.equals(type))
//                    {
////                        setApplicationID(rsResource.getInt(APPLICATION_ID));
//                    setID(rsResource.getInt(ExceptionsSqlDAO.APPLICATION_EXCEPTION_ID));
//                    setResource(rsResource.getString(ExceptionsSqlDAO.APPLICATION_EXCEPTION));
//                    }
//                else if (RootLevel.COMPONENT.equals(type))
//                    {
////                        setApplicationID(rsResource.getInt(APPLICATION_ID));
////                        setComponentID(rsResource.getInt(COMPONENT_ID));
//                    setID(rsResource.getInt(ExceptionsSqlDAO.COMPONENT_EXCEPTION_ID));
//                    setResource(rsResource.getString(ExceptionsSqlDAO.COMPONENT_EXCEPTION));
//                    }
//                else
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException("[exceptiontype=" + getLevel() + "]"),
//                                                              0, REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                              EventStatus.FATAL);
//                    }
//
//                setResourceKey0(rsResource.getString(ExceptionsSqlDAO.RESOURCE_KEY0));
//                setResourceKey1(rsResource.getString(ExceptionsSqlDAO.RESOURCE_KEY1));
//                setResourceKey2(rsResource.getString(ExceptionsSqlDAO.RESOURCE_KEY2));
//                setResourceKey3(rsResource.getString(ExceptionsSqlDAO.RESOURCE_KEY3));
//                setISOLanguageCode(rsResource.getString(ExceptionsSqlDAO.LANGUAGE_ISOCODE));
//                setInstalled(rsResource.getBoolean(ExceptionsSqlDAO.INSTALLED));
//                setEditorClassname(rsResource.getString(ExceptionsSqlDAO.EDITOR_CLASSNAME));
//                setDescription(rsResource.getString(ExceptionsSqlDAO.DESCRIPTION));
//                setCreatedDate(rsResource.getDate(ExceptionsSqlDAO.DATE_CREATED));
//                setCreatedTime(rsResource.getTime(ExceptionsSqlDAO.TIME_CREATED));
//                setModifiedDate(rsResource.getDate(ExceptionsSqlDAO.DATE_MODIFIED));
//                setModifiedTime(rsResource.getTime(ExceptionsSqlDAO.TIME_MODIFIED));
//
//                // Initialise the extra fields which are not stored in the FrameworkDatabase
//                setEditable(true);
//                setDataType(EXCEPTION_CLASSNAME);
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
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.LOAD_FRAMEWORK_EXCEPTIONS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1+ "]"
//                                                                                 + " [resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.LOAD_APPLICATION_EXCEPTIONS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1 + "]"
//                                                                                 + " [componentid=" + 2 + "]"
//                                                                                 + " [resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.LOAD_COMPONENT_EXCEPTIONS,
//                                                          EventStatus.FATAL);
//                }
//            else
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [exceptiontype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                          EventStatus.FATAL);
//                }
//            }
        }


    /***********************************************************************************************
     * Write the ExceptionData object back to the database
     */

    public final void writeResource()
        {
//        final PreparedStatement psException;
//        int intParameter;
//        QueryData queryData;
//
//        // Check that it is possible to store the Exception string successfully...
//        if (((String)getResource()).length() > EXCEPTION_LENGTH)
//            {
//            ExceptionLibrary.handleAtomException(new FrameworkException("[resource=" + getResource() + "] "
//                                                                             + "[length=" + ((String)getResource()).length() + "]" ),
//                                                      0, REGISTRY.getFramework().getRootTask().getID(),
//                                                      this.getClass().getName(),
//                                                      ExceptionLibrary.RESOURCE_LENGTH,
//                                                      EventStatus.FATAL);
//            }
//
//        try
//            {
//            queryData= null;
//
//            // Read the full details of the specified Exception
//            final RootLevel type;
//
//            type = getLevel();
//
//            if (RootLevel.FRAMEWORK.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.FRAMEWORK_RESOURCE]");
//
//                queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.UPDATE_FRAMEWORK_EXCEPTION);
//                psException = queryData.getPreparedStatement();
//
//                intParameter = 1;
//                psException.setString(intParameter++, this.getResourceKey0());
//                psException.setString(intParameter++, this.getResourceKey1());
//                psException.setString(intParameter++, this.getResourceKey2());
//                psException.setString(intParameter++, this.getResourceKey3());
//                psException.setString(intParameter++, this.getISOLanguageCode());
//                psException.setBoolean(intParameter++, this.isInstalled());
//                psException.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psException.setString(intParameter++, (String)this.getResource());
//                psException.setString(intParameter++, getDescription());
//                psException.setDate(intParameter++, getCreatedDate());
//                psException.setTime(intParameter++, getCreatedTime());
//                psException.setDate(intParameter++, getModifiedDate());
//                psException.setTime(intParameter++, getModifiedTime());
//
//                // WHERE
////                    psException.setInt(intParameter, this.getID());
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.APPLICATION_RESOURCE]");
//
//                queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.UPDATE_APPLICATION_EXCEPTION);
//                psException = queryData.getPreparedStatement();
//
//                intParameter = 1;
//                psException.setString(intParameter++, this.getResourceKey0());
//                psException.setString(intParameter++, this.getResourceKey1());
//                psException.setString(intParameter++, this.getResourceKey2());
//                psException.setString(intParameter++, this.getResourceKey3());
//                psException.setString(intParameter++, this.getISOLanguageCode());
//                psException.setBoolean(intParameter++, this.isInstalled());
//                psException.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psException.setString(intParameter++, (String)this.getResource());
//                psException.setString(intParameter++, getDescription());
//                psException.setDate(intParameter++, getCreatedDate());
//                psException.setTime(intParameter++, getCreatedTime());
//                psException.setDate(intParameter++, getModifiedDate());
//                psException.setTime(intParameter++, getModifiedTime());
//
//                // WHERE
////                    psException.setInt(intParameter++, this.getApplicationID());
////                    psException.setInt(intParameter, this.getID());
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.COMPONENT_RESOURCE]");
//
//                queryData = REGISTRY.getQueryData(ExceptionsSqlDAO.UPDATE_COMPONENT_EXCEPTION);
//                psException = queryData.getPreparedStatement();
//
//                intParameter = 1;
//                psException.setString(intParameter++, this.getResourceKey0());
//                psException.setString(intParameter++, this.getResourceKey1());
//                psException.setString(intParameter++, this.getResourceKey2());
//                psException.setString(intParameter++, this.getResourceKey3());
//                psException.setString(intParameter++, this.getISOLanguageCode());
//                psException.setBoolean(intParameter++, this.isInstalled());
//                psException.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psException.setString(intParameter++, (String)this.getResource());
//                psException.setString(intParameter++, getDescription());
//                psException.setDate(intParameter++, getCreatedDate());
//                psException.setTime(intParameter++, getCreatedTime());
//                psException.setDate(intParameter++, getModifiedDate());
//                psException.setTime(intParameter++, getModifiedTime());
//
//                // WHERE
////                    psException.setInt(intParameter++, this.getApplicationID());
////                    psException.setInt(intParameter++, this.getPointID());
////                    psException.setInt(intParameter, this.getID());
//                }
//            else
//                {
//                psException = null;
//                ExceptionLibrary.handleAtomException(new FrameworkException("[exceptiontype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                          EventStatus.FATAL);
//                }
//
//            if (queryData != null)
//                {
//                queryData.executeUpdate(this,
//                                        psException,
//                                        RegistryModel.getInstance().getSqlTrace(),
//                                        RegistryModel.getInstance().getSqlTiming());
//                }
//
//            // Record the fact that we have written all of the data for this Exception
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
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.SAVE_FRAMEWORK_EXCEPTIONS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1 + "]"
//                                                                                 + "[resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.SAVE_APPLICATION_EXCEPTIONS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1 + "]"
//                                                                                 + " [componentid=" + 2 + "]"
//                                                                                 + "[resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.SAVE_COMPONENT_EXCEPTIONS,
//                                                          EventStatus.FATAL);
//                }
//            else
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [exceptiontype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                          EventStatus.FATAL);
//                }
//            }
        }


    /***********************************************************************************************
     * Create the SQL script to delete this ExceptionData.
     *
     * @return StringBuffer
     */

    public final StringBuffer createSQLDeleteScript()
        {
        final StringBuffer bufferScript;

        bufferScript = new StringBuffer();
//
//        final RootLevel type;
//
//        type = getLevel();
//
//        if (RootLevel.FRAMEWORK.equals(type))
//            {
//            bufferScript.append(SQL_DELETE);
//            bufferScript.append(ExceptionsSqlDAO.FRAMEWORK_EXCEPTIONS_TABLE);
//            bufferScript.append(SQL_WHERE);
//            bufferScript.append(ExceptionsSqlDAO.FRAMEWORK_EXCEPTION_ID);
//            bufferScript.append(SQL_EQUALS);
//            bufferScript.append(getID());
//            }
//        else if (RootLevel.APPLICATION.equals(type))
//            {
//            bufferScript.append(SQL_DELETE);
//            bufferScript.append(ExceptionsSqlDAO.APPLICATION_EXCEPTIONS_TABLE);
//            bufferScript.append(SQL_WHERE);
//            bufferScript.append(ExceptionsSqlDAO.APPLICATION_EXCEPTION_ID);
//            bufferScript.append(SQL_EQUALS);
//            bufferScript.append(getID());
//            }
//        else if (RootLevel.COMPONENT.equals(type))
//            {
//            bufferScript.append(SQL_DELETE);
//            bufferScript.append(ExceptionsSqlDAO.COMPONENT_EXCEPTIONS_TABLE);
//            bufferScript.append(SQL_WHERE);
//            bufferScript.append(ExceptionsSqlDAO.COMPONENT_EXCEPTION_ID);
//            bufferScript.append(SQL_EQUALS);
//            bufferScript.append(getID());
//            }
//        else
//            {
//            ExceptionLibrary.handleAtomException(new FrameworkException("[exceptiontype=" + getLevel() + "]"),
//                                                      0,
//                                                      REGISTRY.getFramework().getRootTask().getID(),
//                                                      this.getClass().getName(),
//                                                      ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                      EventStatus.FATAL);
//            }

        return (bufferScript);
        }


    /***********************************************************************************************
     * Create the SQL script to insert this ExceptionData.
     *
     * @return StringBuffer
     */

    public final StringBuffer createSQLInsertScript()
        {
        final StringBuffer bufferScript;

        bufferScript = new StringBuffer();

        // ToDo !!!!!!!!!!!!!!!!!!!!!

        return(bufferScript);
        }
    /***********************************************************************************************
     * Load all ApplicationExceptions from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the AtomException hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param application
     * @param languagecode
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadApplicationExceptions(final FrameworkDatabase database,
                                                                   final AtomPlugin application,
                                                                   final String languagecode,
                                                                   final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeExceptions;

        // Create the expander node for the ApplicationExceptions
        nodeExceptions = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     application,
//                                     null,
//                                     ResourceLoader.RESOURCE_APPLICATION,
//                                     ExceptionData.class,
//                                     database,
//                                     SELECT_APPLICATION_EXCEPTION_LIST,
//                                     languagecode,
//                                     nodeExceptions,
//                                     ExceptionData.EXCEPTIONS_EXPANDER_NAME,
//                                     ExceptionData.EXCEPTIONS_ICON,
//                                     ExceptionData.EXCEPTION_ICON,
//                                     ExceptionData.hashtableExceptions,
//                                     debugmode);

        REGISTRY.showExceptions(debugmode);

        return (nodeExceptions);
        }

    /***********************************************************************************************
     * Load all ComponentExceptions from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the ComponentException hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param component
     * @param languagecode
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadComponentExceptions(final FrameworkDatabase database,
                                                                 final AtomPlugin component,
                                                                 final String languagecode,
                                                                 final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeExceptions;

        // Create the expander node for the ComponentExceptions
        nodeExceptions = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     component.getParentApplication(),
//                                     component,
//                                     ResourceLoader.RESOURCE_COMPONENT,
//                                     ExceptionData.class,
//                                     database,
//                                     SELECT_COMPONENT_EXCEPTION_LIST,
//                                     languagecode,
//                                     nodeExceptions,
//                                     ExceptionData.EXCEPTIONS_EXPANDER_NAME,
//                                     ExceptionData.EXCEPTIONS_ICON,
//                                     ExceptionData.EXCEPTION_ICON,
//                                     ExceptionData.hashtableExceptions,
//                                     debugmode);

        REGISTRY.showExceptions(debugmode);

        return (nodeExceptions);
        }
    }
