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

/***************************************************************************************************
 * StringData to associate with RegistryModel TreeNode
 * This may be used to store Framework, Application and Component Strings.
 *
 * <br>Uses Queries:
 * <code>
 * <li>Select.Framework.String.List</li>
 * <li>Select.Framework.String.ByID</li>
 * <li>Select.Framework.String.ByName</li>
 * <li>Update.Framework.String</li>
 * <li>Select.Application.String.List</li>
 * <li>Select.Application.String.ByID</li>
 * <li>Select.Application.String.ByName</li>
 * <li>Update.Application.String</li>
 * <li>Select.Component.String.List</li>
 * <li>Select.Component.String.ByID</li>
 * <li>Select.Component.String.ByName</li>
 * <li>Update.Component.String</li>
 * </code>
 *
 * Todo Complete all Queries
 * ToDO Exception metadata
 */
public class StringsSqlDAO
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    // Column names
    public static final String APPLICATION_ID           = "ApplicationID";
    public static final String COMPONENT_ID             = "ComponentID";
    public static final String FRAMEWORK_STRING_ID      = "FrameworkStringID";
    public static final String APPLICATION_STRING_ID    = "ApplicationStringID";
    public static final String COMPONENT_STRING_ID      = "ComponentStringID";
    public static final String RESOURCE_KEY0            = "ResourceKey0";
    public static final String RESOURCE_KEY1            = "ResourceKey1";
    public static final String RESOURCE_KEY2            = "ResourceKey2";
    public static final String RESOURCE_KEY3            = "ResourceKey3";
    public static final String LANGUAGE_ISOCODE         = "LanguageCode";
    public static final String INSTALLED                = "Installed";
    public static final String EDITOR_ID                = "EditorID";
    public static final String FRAMEWORK_STRING         = "FrameworkString";
    public static final String APPLICATION_STRING       = "ApplicationString";
    public static final String COMPONENT_STRING         = "ComponentString";
    public static final String DESCRIPTION              = "StringDescription";
    public static final String DATE_CREATED             = "DateCreated";
    public static final String TIME_CREATED             = "TimeCreated";
    public static final String DATE_MODIFIED            = "DateModified";
    public static final String TIME_MODIFIED            = "TimeModified";
    // Column name in the Editors table, joined on EditorID
    public static final String EDITOR_CLASSNAME        = EditorUIComponent.EDITOR_CLASSNAME;
    // Queries
    public static final String SELECT_FRAMEWORK_STRING_LIST        = "Select.Framework.String.List";
    public static final String SELECT_FRAMEWORK_STRING_BYID        = "Select.Framework.String.ByID";
    public static final String SELECT_FRAMEWORK_STRING_BYNAME      = "Select.Framework.String.ByName";
    public static final String UPDATE_FRAMEWORK_STRING             = "Update.Framework.String";
    public static final String SELECT_APPLICATION_STRING_LIST      = "Select.Application.String.List";
    public static final String SELECT_APPLICATION_STRING_BYID      = "Select.Application.String.ByID";
    public static final String SELECT_APPLICATION_STRING_BYNAME    = "Select.Application.String.ByName";
    public static final String UPDATE_APPLICATION_STRING           = "Update.Application.String";
    public static final String SELECT_COMPONENT_STRING_LIST        = "Select.Component.String.List";
    public static final String SELECT_COMPONENT_STRING_BYID        = "Select.Component.String.ByID";
    public static final String SELECT_COMPONENT_STRING_BYNAME      = "Select.Component.String.ByName";
    public static final String UPDATE_COMPONENT_STRING             = "Update.Component.String";

    /***********************************************************************************************
     * Load all FrameworkStrings from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the FrameworkStrings hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param languagecode
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadFrameworkStrings(final FrameworkDatabase database,
                                                              final String languagecode,
                                                              final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeStrings;

        // Create the expander node for the FrameworkStrings
        nodeStrings = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     null,
//                                     null,
//                                     ResourceLoader.RESOURCE_FRAMEWORK,
//                                     StringData.class,
//                                     database,
//                                     SELECT_FRAMEWORK_STRING_LIST,
//                                     languagecode,
//                                     nodeStrings,
//                                     StringData.STRINGS_EXPANDER_NAME,
//                                     StringData.STRINGS_ICON,
//                                     StringData.STRING_ICON,
//                                     StringData.hashtableStrings,
//                                     debugmode);

        REGISTRY.showStrings(debugmode);

        return (nodeStrings);
        }

    /***********************************************************************************************
     * Load all ApplicationStrings from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the ApplicationStrings hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param application
     * @param languagecode
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadApplicationStrings(final FrameworkDatabase database,
                                                                final AtomPlugin application,
                                                                final String languagecode,
                                                                final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeStrings;

        // Create the expander node for the ApplicationStrings
        nodeStrings = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     application,
//                                     null,
//                                     ResourceLoader.RESOURCE_APPLICATION,
//                                     StringData.class,
//                                     database,
//                                     SELECT_APPLICATION_STRING_LIST,
//                                     languagecode,
//                                     nodeStrings,
//                                     StringData.STRINGS_EXPANDER_NAME,
//                                     StringData.STRINGS_ICON,
//                                     StringData.STRING_ICON,
//                                     StringData.hashtableStrings,
//                                     debugmode);

        REGISTRY.showStrings(debugmode);

        return (nodeStrings);
        }

    /***********************************************************************************************
     * Load all ComponentStrings from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the ComponentStrings hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param component
     * @param languagecode
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadComponentStrings(final FrameworkDatabase database,
                                                              final AtomPlugin component,
                                                              final String languagecode,
                                                              final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeStrings;

        // Create the expander node for the ComponentStrings
        nodeStrings = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(REGISTRY.getFramework(),
//                                     component.getParentApplication(),
//                                     component,
//                                     ResourceLoader.RESOURCE_COMPONENT,
//                                     StringData.class,
//                                     database,
//                                     SELECT_COMPONENT_STRING_LIST,
//                                     languagecode,
//                                     nodeStrings,
//                                     StringData.STRINGS_EXPANDER_NAME,
//                                     StringData.STRINGS_ICON,
//                                     StringData.STRING_ICON,
//                                     StringData.hashtableStrings,
//                                     debugmode);

        REGISTRY.showStrings(debugmode);

        return (nodeStrings);
        }
    /***********************************************************************************************
     * Load the StringData object with data from the database
     * <br>Resources are retrieved for the language specified in the constructor
     */

    public final void readResource()
        {
//        final PreparedStatement psString;
//        final ResultSet rsResource;
//        int intParameter;
//        QueryData queryData;
//
//        try
//            {
//            queryData = null;
//
//            // Read the full details of the specified String
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
//                    queryData = REGISTRY.getQueryData(StringsSqlDAO.SELECT_FRAMEWORK_STRING_BYID);
//                    psString = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psString.setInt(intParameter++, this.getID());
//                    psString.setString(intParameter, this.strLanguage);
//                    }
//                else
//                    {
//                    // No StringID, so we must use the Level names
//                    queryData = REGISTRY.getQueryData(StringsSqlDAO.SELECT_FRAMEWORK_STRING_BYNAME);
//                    psString = queryData.getPreparedStatement();
//
//                    intParameter = 1;
//                    psString.setString(intParameter++, this.getResourceKey0());
//                    psString.setString(intParameter++, this.getResourceKey1());
//                    psString.setString(intParameter++, this.getResourceKey2());
//                    psString.setString(intParameter++, this.getResourceKey3());
//                    psString.setString(intParameter, this.strLanguage);
//                    }
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.APPLICATION_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryData = REGISTRY.getQueryData(StringsSqlDAO.SELECT_APPLICATION_STRING_BYID);
//                    psString = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psString.setInt(intParameter++, this.getApplicationID());
////                        psString.setInt(intParameter++, this.getID());
//                    psString.setString(intParameter, this.strLanguage);
//                    }
//                else
//                    {
//                    // No StringID, so we must use the Level names
//                    queryData = REGISTRY.getQueryData(StringsSqlDAO.SELECT_APPLICATION_STRING_BYNAME);
//                    psString = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psString.setInt(intParameter++, this.getApplicationID());
//                    psString.setString(intParameter++, this.getResourceKey0());
//                    psString.setString(intParameter++, this.getResourceKey1());
//                    psString.setString(intParameter++, this.getResourceKey2());
//                    psString.setString(intParameter++, this.getResourceKey3());
//                    psString.setString(intParameter, this.strLanguage);
//                   }
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                showDebugMessage(".readResource [ResourceData.COMPONENT_RESOURCE]");
//
////                    if (this.getCreatedByID())
//                if (false)
//                    {
//                    queryData = REGISTRY.getQueryData(StringsSqlDAO.SELECT_COMPONENT_STRING_BYID);
//                    psString = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psString.setInt(intParameter++, this.getApplicationID());
////                        psString.setInt(intParameter++, this.getPointID());
////                        psString.setInt(intParameter++, this.getID());
//                    psString.setString(intParameter, this.strLanguage);
//                    }
//                else
//                    {
//                    // No StringID, so we must use the Level names
//                    queryData = REGISTRY.getQueryData(StringsSqlDAO.SELECT_COMPONENT_STRING_BYNAME);
//                    psString = queryData.getPreparedStatement();
//
//                    intParameter = 1;
////                        psString.setInt(intParameter++, this.getApplicationID());
////                        psString.setInt(intParameter++, this.getPointID());
//                    psString.setString(intParameter++, this.getResourceKey0());
//                    psString.setString(intParameter++, this.getResourceKey1());
//                    psString.setString(intParameter++, this.getResourceKey2());
//                    psString.setString(intParameter++, this.getResourceKey3());
//                    psString.setString(intParameter, this.strLanguage);
//                    }
//                }
//            else
//                {
//                psString = null;
//                ExceptionLibrary.handleAtomException(new FrameworkException("[stringtype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_STRING_TYPE,
//                                                          EventStatus.FATAL);
//                }
//
//            if (queryData != null)
//                {
//                rsResource = queryData.executeQuery(this,
//                                                    psString,
//                                                    RegistryModel.getInstance().getSqlTrace(),
//                                                    RegistryModel.getInstance().getSqlTiming());
//
//                // Check here for no String data at all!
//                rsResource.next();
//
//                // Prepare the User Object for the TreeNode
//                // Change here if the String table designs change!
//                suspendUpdates();
//
//                // Errors in RootType will have already been trapped...
//                if (RootLevel.FRAMEWORK.equals(type))
//                    {
//                    setID(rsResource.getInt(StringsSqlDAO.FRAMEWORK_STRING_ID));
//                    setResource(rsResource.getString(StringsSqlDAO.FRAMEWORK_STRING));
//                    }
//                else if (RootLevel.APPLICATION.equals(type))
//                    {
////                        setApplicationID(rsResource.getInt(APPLICATION_ID));
//                    setID(rsResource.getInt(StringsSqlDAO.APPLICATION_STRING_ID));
//                    setResource(rsResource.getString(StringsSqlDAO.APPLICATION_STRING));
//                    }
//                else if (RootLevel.COMPONENT.equals(type))
//                    {
////                        setApplicationID(rsResource.getInt(APPLICATION_ID));
////                        setComponentID(rsResource.getInt(COMPONENT_ID));
//                    setID(rsResource.getInt(StringsSqlDAO.COMPONENT_STRING_ID));
//                    setResource(rsResource.getString(StringsSqlDAO.COMPONENT_STRING));
//                    }
//                else
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException("[stringtype=" + getLevel() + "]"),
//                                                              0, REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionLibrary.INVALID_STRING_TYPE,
//                                                              EventStatus.FATAL);
//                    }
//
//                setResourceKey0(rsResource.getString(StringsSqlDAO.RESOURCE_KEY0));
//                setResourceKey1(rsResource.getString(StringsSqlDAO.RESOURCE_KEY1));
//                setResourceKey2(rsResource.getString(StringsSqlDAO.RESOURCE_KEY2));
//                setResourceKey3(rsResource.getString(StringsSqlDAO.RESOURCE_KEY3));
//                setISOLanguageCode(rsResource.getString(StringsSqlDAO.LANGUAGE_ISOCODE));
//                setInstalled(rsResource.getBoolean(StringsSqlDAO.INSTALLED));
//                setEditorClassname(rsResource.getString(StringsSqlDAO.EDITOR_CLASSNAME));
//                setDescription(rsResource.getString(StringsSqlDAO.DESCRIPTION));
//                setCreatedDate(rsResource.getDate(StringsSqlDAO.DATE_CREATED));
//                setCreatedTime(rsResource.getTime(StringsSqlDAO.TIME_CREATED));
//                setModifiedDate(rsResource.getDate(StringsSqlDAO.DATE_MODIFIED));
//                setModifiedTime(rsResource.getTime(StringsSqlDAO.TIME_MODIFIED));
//
//                // Initialise the extra fields which are not stored in the FrameworkDatabase
//                setEditable(true);
//                setDataType(STRING_CLASSNAME);
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
//                                                          ExceptionLibrary.LOAD_FRAMEWORK_STRINGS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1 + "]"
//                                                                                 + " [resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.LOAD_APPLICATION_STRINGS,
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
//                                                          ExceptionLibrary.LOAD_COMPONENT_STRINGS,
//                                                          EventStatus.FATAL);
//                }
//            else
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [stringtype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_STRING_TYPE,
//                                                          EventStatus.FATAL);
//                }
//            }
        }


    /***********************************************************************************************
     * Write the StringData object back to the database
     */

    public final void writeResource()
        {
//        final PreparedStatement psString;
//        int intParameter;
//        QueryData queryData;
//
//        // Check that it is possible to store the String successfully...
//        if (((String)getResource()).length() > STRING_LENGTH)
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
//            queryData = null;
//
//            // Read the full details of the specified String
//            final RootLevel type;
//
//            type = getLevel();
//
//            if (RootLevel.FRAMEWORK.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.FRAMEWORK_RESOURCE]");
//
//                queryData = REGISTRY.getQueryData(StringsSqlDAO.UPDATE_FRAMEWORK_STRING);
//                psString = queryData.getPreparedStatement();
//
//                intParameter = 1;
//                psString.setString(intParameter++, this.getResourceKey0());
//                psString.setString(intParameter++, this.getResourceKey1());
//                psString.setString(intParameter++, this.getResourceKey2());
//                psString.setString(intParameter++, this.getResourceKey3());
//                psString.setString(intParameter++, this.getISOLanguageCode());
//                psString.setBoolean(intParameter++, this.isInstalled());
//                psString.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psString.setString(intParameter++, (String)this.getResource());
//                psString.setString(intParameter++, getDescription());
//                psString.setDate(intParameter++, getCreatedDate());
//                psString.setTime(intParameter++, getCreatedTime());
//                psString.setDate(intParameter++, getModifiedDate());
//                psString.setTime(intParameter++, getModifiedTime());
//
//                // WHERE
////                    psString.setInt(intParameter, this.getID());
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.APPLICATION_RESOURCE]");
//
//                queryData = REGISTRY.getQueryData(StringsSqlDAO.UPDATE_APPLICATION_STRING);
//                psString = queryData.getPreparedStatement();
//
//                intParameter = 1;
//                psString.setString(intParameter++, this.getResourceKey0());
//                psString.setString(intParameter++, this.getResourceKey1());
//                psString.setString(intParameter++, this.getResourceKey2());
//                psString.setString(intParameter++, this.getResourceKey3());
//                psString.setString(intParameter++, this.getISOLanguageCode());
//                psString.setBoolean(intParameter++, this.isInstalled());
//                psString.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psString.setString(intParameter++, (String)this.getResource());
//                psString.setString(intParameter++, getDescription());
//                psString.setDate(intParameter++, getCreatedDate());
//                psString.setTime(intParameter++, getCreatedTime());
//                psString.setDate(intParameter++, getModifiedDate());
//                psString.setTime(intParameter++, getModifiedTime());
//
//                // WHERE
////                    psString.setInt(intParameter++, this.getApplicationID());
////                    psString.setInt(intParameter, this.getID());
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                showDebugMessage(".writeResource [ResourceData.COMPONENT_RESOURCE]");
//
//                queryData = REGISTRY.getQueryData(StringsSqlDAO.UPDATE_COMPONENT_STRING);
//                psString = queryData.getPreparedStatement();
//
//                intParameter = 1;
//                psString.setString(intParameter++, this.getResourceKey0());
//                psString.setString(intParameter++, this.getResourceKey1());
//                psString.setString(intParameter++, this.getResourceKey2());
//                psString.setString(intParameter++, this.getResourceKey3());
//                psString.setString(intParameter++, this.getISOLanguageCode());
//                psString.setBoolean(intParameter++, this.isInstalled());
//                psString.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                psString.setString(intParameter++, (String)this.getResource());
//                psString.setString(intParameter++, getDescription());
//                psString.setDate(intParameter++, getCreatedDate());
//                psString.setTime(intParameter++, getCreatedTime());
//                psString.setDate(intParameter++, getModifiedDate());
//                psString.setTime(intParameter++, getModifiedTime());
//
//                // WHERE
////                    psString.setInt(intParameter++, this.getApplicationID());
////                    psString.setInt(intParameter++, this.getPointID());
////                    psString.setInt(intParameter, this.getID());
//                }
//            else
//                {
//                psString = null;
//                ExceptionLibrary.handleAtomException(new FrameworkException("[stringtype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_STRING_TYPE,
//                                                          EventStatus.FATAL);
//                }
//
//            if (queryData != null)
//                {
//                queryData.executeUpdate(this,
//                                        psString,
//                                        RegistryModel.getInstance().getSqlTrace(),
//                                        RegistryModel.getInstance().getSqlTiming());
//                }
//
//            // Record the fact that we have written all of the data for this String
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
//                                                          ExceptionLibrary.SAVE_FRAMEWORK_STRINGS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.APPLICATION.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1 + "]"
//                                                                                 + "[resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.SAVE_APPLICATION_STRINGS,
//                                                          EventStatus.FATAL);
//                }
//            else if (RootLevel.COMPONENT.equals(type))
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [applicationid=" + 1 + "]"
//                                                                                 + " [componentid=" + 1 + "]"
//                                                                                 + "[resourceid=" +  getID() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.SAVE_COMPONENT_STRINGS,
//                                                          EventStatus.FATAL);
//                }
//            else
//                {
//                ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                 + " [stringtype=" + getLevel() + "]"),
//                                                          0, REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionLibrary.INVALID_STRING_TYPE,
//                                                          EventStatus.FATAL);
//                }
//            }
        }


    /***********************************************************************************************
     * Create the SQL script to delete this StringData.
     *
     * @return StringBuffer
     */

    public final StringBuffer createSQLDeleteScript()
        {
        final StringBuffer bufferScript;

        bufferScript = new StringBuffer();

        bufferScript.append("ToDo !!!!!!!!!!!!!!!!!!!!!");

        return (bufferScript);
        }


    /***********************************************************************************************
     * Create the SQL script to insert this StringData.
     *
     * @return StringBuffer
     */

    public final StringBuffer createSQLInsertScript()
        {
        final StringBuffer bufferScript;

        bufferScript = new StringBuffer();

        bufferScript.append("ToDo !!!!!!!!!!!!!!!!!!!!!");

        return(bufferScript);
        }
    }
