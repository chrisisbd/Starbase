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

import javax.swing.tree.DefaultMutableTreeNode;

public class PropertiesSqlDAO
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    // Column names in Properties tables
    public static final String FRAMEWORK_PROPERTIES_TABLE   = "LoaderProperties";
    public static final String APPLICATION_PROPERTIES_TABLE = "ApplicationProperties";
    public static final String COMPONENT_PROPERTIES_TABLE   = "ComponentProperties";

    public static final String APPLICATION_ID           = "ApplicationID";
    public static final String COMPONENT_ID             = "ComponentID";
    public static final String FRAMEWORK_PROPERTYID     = "FrameworkPropertyID";
    public static final String APPLICATION_PROPERTYID   = "ApplicationPropertyID";
    public static final String COMPONENT_PROPERTYID     = "ComponentPropertyID";
    public static final String RESOURCE_KEY0            = "ResourceKey0";
    public static final String RESOURCE_KEY1            = "ResourceKey1";
    public static final String RESOURCE_KEY2            = "ResourceKey2";
    public static final String RESOURCE_KEY3            = "ResourceKey3";
    public static final String EDITABLE                 = "PropertyEditable";
    public static final String DATATYPE_ID              = "DataTypeID";
    public static final String EDITOR_ID                = "EditorID";
    public static final String PROPERTY_VALUE           = "PropertyValue";
    public static final String PROPERTY_DESCRIPTION     = "PropertyDescription";
    public static final String DATE_CREATED             = "DateCreated";
    public static final String TIME_CREATED             = "TimeCreated";
    public static final String DATE_MODIFIED            = "DateModified";
    public static final String TIME_MODIFIED            = "TimeModified";

    // Column name in the DataTypes and Editors tables, required for JOINs
//    public static final String DATATYPE_CLASSNAME       = DataTypeParser.DATATYPE_CLASSNAME;
//    public static final String EDITOR_CLASSNAME         = EditorUIComponent.EDITOR_CLASSNAME;
//    // Queries
//    private static final String SELECT_FRAMEWORK_PROPERTY_LIST      = "Select.Framework.Property.List";
//    private static final String SELECT_FRAMEWORK_PROPERTY_BYID      = "Select.Framework.Property.ByID";
//    private static final String SELECT_FRAMEWORK_PROPERTY_BYNAME    = "Select.Framework.Property.ByName";
//    private static final String UPDATE_FRAMEWORK_PROPERTY           = "Update.Framework.Property";
//
//    private static final String SELECT_APPLICATION_PROPERTY_LIST    = "Select.Application.Property.List";
//    private static final String SELECT_APPLICATION_PROPERTY_BYID    = "Select.Application.Property.ByID";
//    private static final String SELECT_APPLICATION_PROPERTY_BYNAME  = "Select.Application.Property.ByName";
//    private static final String UPDATE_APPLICATION_PROPERTY         = "Update.Application.Property";
//
//    private static final String SELECT_COMPONENT_PROPERTY_LIST      = "Select.Component.Property.List";
//    private static final String SELECT_COMPONENT_PROPERTY_BYID      = "Select.Component.Property.ByID";
//    private static final String SELECT_COMPONENT_PROPERTY_BYNAME    = "Select.Component.Property.ByName";
//    private static final String UPDATE_COMPONENT_PROPERTY           = "Update.Component.Property";
//
//
    /***********************************************************************************************
     * Load all LoaderProperties from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the FrameworkProperty hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadFrameworkProperties(final FrameworkDatabase database,
                                                                 final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeProperties;
        nodeProperties = new DefaultMutableTreeNode();

//        hashtableProperties = new Hashtable();
//
//        // Create the expander node for the LoaderProperties
//        nodeProperties = new DefaultMutableTreeNode();
//
//        ResourceLoader.loadResources(database.RegistryModel.getInstance().getFramework(),
//                                     null,
//                                     null,
//                                     ResourceLoader.RESOURCE_FRAMEWORK,
//                                     class,
//                                     database,
//                                     SELECT_FRAMEWORK_PROPERTY_LIST,
//                                     LanguageData.NO_LANGUAGE,
//                                     nodeProperties,
//                                     PropertyData.PROPERTIES_EXPANDER_NAME,
//                                     PropertyData.PROPERTIES_ICON,
//                                     PropertyData.PROPERTY_ICON,
//                                     hashtableProperties,
//                                     debugmode);

        return (nodeProperties);
        }

    /***********************************************************************************************
     * Load all ApplicationProperties from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the ApplicationProperty hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param application ApplicationData
     * @param debugmode
     * @return DefaultMutableTreeNode
     */
    public static DefaultMutableTreeNode loadApplicationProperties(final FrameworkDatabase database,
                                                                   final AtomPlugin application,
                                                                   final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeProperties;

        // Create the expander node for the ApplicationProperties
        nodeProperties = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(application.REGISTRY.getFramework(),
//                                     application,
//                                     null,
//                                     ResourceLoader.RESOURCE_APPLICATION,
//                                     class,
//                                     database,
//                                     SELECT_APPLICATION_PROPERTY_LIST,
//                                     LanguageData.NO_LANGUAGE,
//                                     nodeProperties,
//                                     PropertyData.PROPERTIES_EXPANDER_NAME,
//                                     PropertyData.PROPERTIES_ICON,
//                                     PropertyData.PROPERTY_ICON,
//                                     hashtableProperties,
//                                     debugmode);

        return (nodeProperties);
        }

    /***********************************************************************************************
     * Load all ComponentProperties from the database
     * <br>Return a DefaultMutableTreeNode which is the root of the ComponentProperty hierarchy
     * to be displayed by the FrameworkManager
     *
     * @param database
     * @param component
     * @param debugmode
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadComponentProperties(final FrameworkDatabase database,
                                                                 final AtomPlugin component,
                                                                 final boolean debugmode)
        {
        final DefaultMutableTreeNode nodeProperties;

        // Create the expander node for the ComponentProperties
        nodeProperties = new DefaultMutableTreeNode();

//        ResourceLoader.loadResources(component.getParentApplication().REGISTRY.getFramework(),
//                                     component.getParentApplication(),
//                                     component,
//                                     ResourceLoader.RESOURCE_COMPONENT,
//                                     class,
//                                     database,
//                                     SELECT_COMPONENT_PROPERTY_LIST,
//                                     LanguageData.NO_LANGUAGE,
//                                     nodeProperties,
//                                     PropertyData.PROPERTIES_EXPANDER_NAME,
//                                     PropertyData.PROPERTIES_ICON,
//                                     PropertyData.PROPERTY_ICON,
//                                     hashtableProperties,
//                                     debugmode);

        return (nodeProperties);
        }
//    /***********************************************************************************************
//     * Construct a FrameworkProperty when the PropertyID is known
//     * <br>Attach the PropertyData to the specified DefaultMutableTreeNode
//     *
//     * @param database
//     * @param intPropertyID
//     * @param language
//     * @param treeNode
//     */
//
//    public PropertyData(final FrameworkDatabase database,
//                        final int intPropertyID,
//                        final String language,
//                        final DefaultMutableTreeNode treeNode)
//        {
//        super(database,
//              intPropertyID,
//              treeNode);
//
//        this.strLanguage = language;
//        }
//
//
//    /***********************************************************************************************
//     * Construct a FrameworkProperty
//     * <br>Attach the PropertyData to the specified DefaultMutableTreeNode
//     * <br>Alternative Constructor for when we've only got the names (e.g in loader)
//     *
//     * @param database
//     * @param strResourceKey0
//     * @param strResourceKey1
//     * @param strResourceKey2
//     * @param strResourceKey3
//     * @param treeNode
//     */
//
//    public PropertyData(final FrameworkDatabase database,
//                        final String strResourceKey0,
//                        final String strResourceKey1,
//                        final String strResourceKey2,
//                        final String strResourceKey3,
//                        final String language,
//                        final DefaultMutableTreeNode treeNode)
//        {
//        super(database,
//              strResourceKey0,
//              strResourceKey1,
//              strResourceKey2,
//              strResourceKey3,
//              treeNode);
//
//        this.strLanguage = language;
//        }
//
//
//    /***********************************************************************************************
//     * Construct an ApplicationProperty
//     *
//     * @param database
//     * @param intApplicationID
//     * @param applicationname
//     * @param intPropertyID
//     * @param language
//     * @param treeNode
//     */
//    public PropertyData(final FrameworkDatabase database,
//                        final int intApplicationID,
//                        final String applicationname,
//                        final int intPropertyID,
//                        final String language,
//                        final DefaultMutableTreeNode treeNode)
//        {
//        super(database,
//              intApplicationID,
//              applicationname,
//              intPropertyID,
//              treeNode);
//
//        this.strLanguage = language;
//        }
//
//
//    /***********************************************************************************************
//     * Alternative Constructor for when we've only got the names (e.g in loader)
//     *
//     * @param database
//     * @param intApplicationID
//     * @param applicationname
//     * @param strResourceKey0
//     * @param strResourceKey1
//     * @param strResourceKey2
//     * @param strResourceKey3
//     * @param language
//     * @param treeNode
//     */
//
//    public PropertyData(final FrameworkDatabase database,
//                        final int intApplicationID,
//                        final String applicationname,
//                        final String strResourceKey0,
//                        final String strResourceKey1,
//                        final String strResourceKey2,
//                        final String strResourceKey3,
//                        final String language,
//                        final DefaultMutableTreeNode treeNode)
//        {
//        super(database,
//              intApplicationID,
//              applicationname,
//              strResourceKey0,
//              strResourceKey1,
//              strResourceKey2,
//              strResourceKey3,
//              treeNode);
//
//        this.strLanguage = language;
//        }
//
//
//    /***********************************************************************************************
//     * Construct a ComponentProperty
//     *
//     * @param database
//     * @param intApplicationID
//     * @param applicationname
//     * @param intComponentID
//     * @param componentname
//     * @param intPropertyID
//     * @param language
//     * @param treeNode
//     */
//
//    public PropertyData(final FrameworkDatabase database,
//                        final int intApplicationID,
//                        final String applicationname,
//                        final int intComponentID,
//                        final String componentname,
//                        final int intPropertyID,
//                        final String language,
//                        final DefaultMutableTreeNode treeNode)
//        {
//        super(database,
//              intApplicationID,
//              applicationname,
//              intComponentID,
//              componentname,
//              intPropertyID,
//              treeNode);
//
//        this.strLanguage = language;
//        }
//
//
//    /***********************************************************************************************
//     * Alternative Constructor for when we've only got the names (e.g in loader)
//     *
//     * @param database
//     * @param intApplicationID
//     * @param applicationname
//     * @param intComponentID
//     * @param componentname
//     * @param strResourceKey0
//     * @param strResourceKey1
//     * @param strResourceKey2
//     * @param strResourceKey3
//     * @param language
//     * @param treeNode
//     */
//
//    public PropertyData(final FrameworkDatabase database,
//                        final int intApplicationID,
//                        final String applicationname,
//                        final int intComponentID,
//                        final String componentname,
//                        final String strResourceKey0,
//                        final String strResourceKey1,
//                        final String strResourceKey2,
//                        final String strResourceKey3,
//                        final String language,
//                        final DefaultMutableTreeNode treeNode)
//        {
//        super(database,
//              intApplicationID,
//              applicationname,
//              intComponentID,
//              componentname,
//              strResourceKey0,
//              strResourceKey1,
//              strResourceKey2,
//              strResourceKey3,
//              treeNode);
//
//        this.strLanguage = language;
//        }


//    /***********************************************************************************************
//     * Load the PropertyData object with data from the database
//     */
//
//    public final void readResource()
//        {
//        final PreparedStatement psProperty;
//        final ResultSet rsResource;
//        final String strClassName;
//        QueryData queryData;
//
//        strClassName = "";
//        queryData = null;
//
//        try
//            {
//            // Read the full details of the specified Property
//            switch (this.getResourceType())
//                {
//                case ResourceData.FRAMEWORK_RESOURCE:
//                    {
//                    showDebugMessage(".readResource [ResourceData.FRAMEWORK_RESOURCE]");
//
//                    if (this.getCreatedByID())
//                        {
//                        queryData = QueryData.getQueryData(SELECT_FRAMEWORK_PROPERTY_BYID);
//                        psProperty = queryData.getPreparedStatement();
//                        psProperty.setInt(1, this.getResourceID());
//                        }
//                    else
//                        {
//                        // No PropertyID, so we must use the Level names
////                        psProperty = QueryData.getPreparedStatement(PropertyData.SELECT_FRAMEWORK_PROPERTY_BYNAME);
//                        queryData = QueryData.getQueryData(SELECT_FRAMEWORK_PROPERTY_BYNAME);
//                        psProperty = queryData.getPreparedStatement();
//                        psProperty.setString(1, this.getResourceKey0());
//                        psProperty.setString(2, this.getResourceKey1());
//                        psProperty.setString(3, this.getResourceKey2());
//                        psProperty.setString(4, this.getResourceKey3());
//                        }
//                    break;
//                    }
//
//                case ResourceData.APPLICATION_RESOURCE:
//                    {
//                    showDebugMessage(".readResource [ResourceData.APPLICATION_RESOURCE]");
//
//                    if (this.getCreatedByID())
//                        {
////                        psProperty = QueryData.getPreparedStatement(PropertyData.SELECT_APPLICATION_PROPERTY_BYID);
//                        queryData = QueryData.getQueryData(SELECT_APPLICATION_PROPERTY_BYID);
//                        psProperty = queryData.getPreparedStatement();
//                        psProperty.setInt(1, this.getApplicationID());
//                        psProperty.setInt(2, this.getResourceID());
//                        }
//                    else
//                        {
//                        // No ResourceID, so we must use the Level names
////                        psProperty = QueryData.getPreparedStatement(PropertyData.SELECT_APPLICATION_PROPERTY_BYNAME);
//                        queryData = QueryData.getQueryData(SELECT_APPLICATION_PROPERTY_BYNAME);
//                        psProperty = queryData.getPreparedStatement();
//                        psProperty.setInt(1, this.getApplicationID());
//                        psProperty.setString(2, this.getResourceKey0());
//                        psProperty.setString(3, this.getResourceKey1());
//                        psProperty.setString(4, this.getResourceKey2());
//                        psProperty.setString(5, this.getResourceKey3());
//                       }
//                    break;
//                    }
//
//                case ResourceData.COMPONENT_RESOURCE:
//                    {
//                    showDebugMessage(".readResource [ResourceData.COMPONENT_RESOURCE]");
//
//                    if (this.getCreatedByID())
//                        {
////                        psProperty = QueryData.getPreparedStatement(PropertyData.SELECT_COMPONENT_PROPERTY_BYID);
//                        queryData = QueryData.getQueryData(SELECT_COMPONENT_PROPERTY_BYID);
//                        psProperty = queryData.getPreparedStatement();
//                        psProperty.setInt(1, this.getApplicationID());
//                        psProperty.setInt(2, this.getPointID());
//                        psProperty.setInt(3, this.getResourceID());
//                        }
//                    else
//                        {
//                        // No ResourceID, so we must use the Level names
////                        psProperty = QueryData.getPreparedStatement(PropertyData.SELECT_COMPONENT_PROPERTY_BYNAME);
//                        queryData = QueryData.getQueryData(SELECT_COMPONENT_PROPERTY_BYNAME);
//                        psProperty = queryData.getPreparedStatement();
//                        psProperty.setInt(1, this.getApplicationID());
//                        psProperty.setInt(2, this.getPointID());
//                        psProperty.setString(3, this.getResourceKey0());
//                        psProperty.setString(4, this.getResourceKey1());
//                        psProperty.setString(5, this.getResourceKey2());
//                        psProperty.setString(6, this.getResourceKey3());
//                       }
//                    break;
//                    }
//
//                default:
//                    {
//                    psProperty = null;
//                    ExceptionLibrary.handleAtomException(new Exception("[resourcetype=" + getResourceType() + "]"),
//                                                              0, REGISTRY.getFramework().getRootTask().getID(),
//                                                              PropertyData.class.getName(),
//                                                              ExceptionData.getException("INVALID_RESOURCE_TYPE"),
//                                                              EventStatus.FATAL);
//                    }
//                }
//
//            if (queryData != null)
//                {
//                rsResource = queryData.executeQuery(this,
//                                                    psProperty,
//                                                    RegistryModel.getInstance().getSqlTrace(),
//                                                    RegistryModel.getInstance().getSqlTiming());
//
//                // Check here for no Property data at all!
//                rsResource.next();
//
//                // Prepare the User Object for the TreeNode
//                // Change here if the Property table designs change!
//                suspendUpdates();
//
//                // Errors in RootType will have already been trapped...
//                switch (getResourceType())
//                    {
//                    case ResourceData.FRAMEWORK_RESOURCE:
//                        {
//                        setResourceID(rsResource.getInt(FRAMEWORK_PROPERTYID));
//                        break;
//                        }
//
//                    case ResourceData.APPLICATION_RESOURCE:
//                        {
//                        setApplicationID(rsResource.getInt(APPLICATION_ID));
//                        setResourceID(rsResource.getInt(APPLICATION_PROPERTYID));
//                        break;
//                        }
//
//                    case ResourceData.COMPONENT_RESOURCE:
//                        {
//                        setApplicationID(rsResource.getInt(APPLICATION_ID));
//                        setComponentID(rsResource.getInt(COMPONENT_ID));
//                        setResourceID(rsResource.getInt(COMPONENT_PROPERTYID));
//                        break;
//                        }
//
//                    default:
//                        {
//                        ExceptionLibrary.handleAtomException(new Exception("[resourcetype=" + getResourceType() + "]"),
//                                                                  0,
//                                                                  REGISTRY.getFramework().getRootTask().getID(),
//                                                                  this.getClass().getName(),
//                                                                  ExceptionData.getException("INVALID_RESOURCE_TYPE"),
//                                                                  EventStatus.FATAL);
//                        }
//                    }
//
//                setResourceKey0(rsResource.getString(RESOURCE_KEY0));
//                setResourceKey1(rsResource.getString(RESOURCE_KEY1));
//                setResourceKey2(rsResource.getString(RESOURCE_KEY2));
//                setResourceKey3(rsResource.getString(RESOURCE_KEY3));
//
//                setEditable(rsResource.getBoolean(EDITABLE));
//                setDataType(rsResource.getString(DATATYPE_CLASSNAME));
//                setEditorClassname(rsResource.getString(EDITOR_CLASSNAME));
//
//                setDescription(rsResource.getString(PROPERTY_DESCRIPTION));
//                setCreatedDate(rsResource.getDate(DATE_CREATED));
//                setCreatedTime(rsResource.getTime(TIME_CREATED));
//                setModifiedDate(rsResource.getDate(DATE_MODIFIED));
//                setModifiedTime(rsResource.getTime(TIME_MODIFIED));
//
//                // Properties don't have a language
//                setISOLanguageCode(this.strLanguage);
//                // Properties are always installed
//                setInstalled(true);
//
//                // Check that the Property value can be instantiated as the correct datatype
//                // in order to set the Value (as an Object)
//                setResource(null);
//                boolInstantiated = false;
//                final Object objProperty = instantiateDataType(getChannelDataType(),
//                                                               rsResource.getString(PROPERTY_VALUE));
//
//                if (objProperty != null)
//                    {
//                    setResource(objProperty);
//                    setInstantiated(true);
//                    }
//
//                rsResource.close();
//                resumeUpdates();
//                setUpdated(false);
//                }
//            }
//
//        // These must all be FrameworkExceptions in case we didn't get an ApplicationID
//        catch (SQLException exception)
//            {
//            // Errors in RootType will have already been trapped...
//            switch (getResourceType())
//                {
//                case ResourceData.FRAMEWORK_RESOURCE:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [resourceid=" +  getResourceID() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("LOAD_FRAMEWORK_PROPERTIES"),
//                                                              EventStatus.FATAL);
//                    break;
//                    }
//
//                case ResourceData.APPLICATION_RESOURCE:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [applicationid=" + getApplicationID() + "]"
//                                                                                     + " [resourceid=" +  getResourceID() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("LOAD_APPLICATION_PROPERTIES"),
//                                                              EventStatus.FATAL);
//                    break;
//                    }
//
//                case ResourceData.COMPONENT_RESOURCE:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [applicationid=" + getApplicationID() + "]"
//                                                                                     + " [componentid=" + getPointID() + "]"
//                                                                                     + " [resourceid=" +  getResourceID() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("LOAD_COMPONENT_PROPERTIES"),
//                                                              EventStatus.FATAL);
//                    break;
//                    }
//
//                default:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [exceptiontype=" + getResourceType() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                              EventStatus.FATAL);
//                    }
//                }
//            }
//
//        catch (IllegalArgumentException exception)
//            {
//            showDebugMessage(".readResource IllegalArgumentException [classname=" + strClassName + "]");
//            }
//        }
//
//
//    /***********************************************************************************************
//     * Write the Property object back to the database
//     * All Properties are written as strings for simplicity
//     */
//
//    public final void writeResource()
//        {
//        final PreparedStatement psProperty;
//        final String strPropertyValue;
//        int intParameter;
//        QueryData queryData;
//
//        // Obtain the string version of the Property value for storage in the database
//        strPropertyValue = getResource().toString();
//
//        // Check that it is possible to store it successfully...
//        if (strPropertyValue.length() > VALUE_LENGTH)
//            {
//            ExceptionLibrary.handleAtomException(new FrameworkException("[property=" + getPathname() + "] "
//                                                                             + " [value=" + strPropertyValue + "] "
//                                                                             + " [length=" + strPropertyValue.length() + "]"),
//                                                      0,
//                                                      REGISTRY.getFramework().getRootTask().getID(),
//                                                      this.getClass().getName(),
//                                                      ExceptionData.getException("WRITE_PROPERTY"),
//                                                      EventStatus.FATAL);
//            }
//
//        // Remember that Properties do not have Language or Installed fields!
//
//        try
//            {
//            queryData = null;
//
//            // Read the full details of the specified Property
//            switch (getResourceType())
//                {
//                case ResourceData.FRAMEWORK_RESOURCE:
//                    {
//                    showDebugMessage(".writeResource [ResourceData.FRAMEWORK_RESOURCE]");
//
//                    queryData = QueryData.getQueryData(UPDATE_FRAMEWORK_PROPERTY);
//                    psProperty = queryData.getPreparedStatement();
//
//                    intParameter = 1;
//                    psProperty.setString(intParameter++, this.getResourceKey0());
//                    psProperty.setString(intParameter++, this.getResourceKey1());
//                    psProperty.setString(intParameter++, this.getResourceKey2());
//                    psProperty.setString(intParameter++, this.getResourceKey3());
//                    psProperty.setBoolean(intParameter++, this.isEditable());
//                    psProperty.setString(intParameter++, Integer.toString(DataType.getDataTypeID(getChannelDataType())));
//                    psProperty.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                    psProperty.setString(intParameter++, this.getResource().toString());
//                    psProperty.setString(intParameter++, getDescription());
//                    psProperty.setDate(intParameter++, getCreatedDate());
//                    psProperty.setTime(intParameter++, getCreatedTime());
//                    psProperty.setDate(intParameter++, getModifiedDate());
//                    psProperty.setTime(intParameter++, getModifiedTime());
//
//                    // WHERE
//                    psProperty.setInt(intParameter, this.getResourceID());
//                    break;
//
//                    }
//
//                case ResourceData.APPLICATION_RESOURCE:
//                    {
//                    showDebugMessage(".writeResource [ResourceData.APPLICATION_RESOURCE]");
//
//                    queryData = QueryData.getQueryData(UPDATE_APPLICATION_PROPERTY);
//                    psProperty = queryData.getPreparedStatement();
//
//                    intParameter = 1;
//                    psProperty.setString(intParameter++, this.getResourceKey0());
//                    psProperty.setString(intParameter++, this.getResourceKey1());
//                    psProperty.setString(intParameter++, this.getResourceKey2());
//                    psProperty.setString(intParameter++, this.getResourceKey3());
//                    psProperty.setBoolean(intParameter++, this.isEditable());
//                    psProperty.setString(intParameter++, Integer.toString(DataType.getDataTypeID(getChannelDataType())));
//                    psProperty.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                    psProperty.setString(intParameter++, this.getResource().toString());
//                    psProperty.setString(intParameter++, getDescription());
//                    psProperty.setDate(intParameter++, getCreatedDate());
//                    psProperty.setTime(intParameter++, getCreatedTime());
//                    psProperty.setDate(intParameter++, getModifiedDate());
//                    psProperty.setTime(intParameter++, getModifiedTime());
//
//                    // WHERE
//                    psProperty.setInt(intParameter++, this.getApplicationID());
//                    psProperty.setInt(intParameter, this.getResourceID());
//                    break;
//                    }
//
//                case ResourceData.COMPONENT_RESOURCE:
//                    {
//                    showDebugMessage(".writeResource [ResourceData.COMPONENT_RESOURCE]");
//
//                    queryData = QueryData.getQueryData(UPDATE_COMPONENT_PROPERTY);
//                    psProperty = queryData.getPreparedStatement();
//
//                    intParameter = 1;
//                    psProperty.setString(intParameter++, this.getResourceKey0());
//                    psProperty.setString(intParameter++, this.getResourceKey1());
//                    psProperty.setString(intParameter++, this.getResourceKey2());
//                    psProperty.setString(intParameter++, this.getResourceKey3());
//                    psProperty.setBoolean(intParameter++, this.isEditable());
//                    psProperty.setString(intParameter++, Integer.toString(DataType.getDataTypeID(getChannelDataType())));
//                    psProperty.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getEditorClassname())));
//                    psProperty.setString(intParameter++, this.getResource().toString());
//                    psProperty.setString(intParameter++, getDescription());
//                    psProperty.setDate(intParameter++, getCreatedDate());
//                    psProperty.setTime(intParameter++, getCreatedTime());
//                    psProperty.setDate(intParameter++, getModifiedDate());
//                    psProperty.setTime(intParameter++, getModifiedTime());
//
//                    // WHERE
//                    psProperty.setInt(intParameter++, this.getApplicationID());
//                    psProperty.setInt(intParameter++, this.getPointID());
//                    psProperty.setInt(intParameter, this.getResourceID());
//                    break;
//                    }
//
//                default:
//                    {
//                    psProperty = null;
//                    ExceptionLibrary.handleAtomException(new FrameworkException("[resourcetype=" + getResourceType() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("TODO INVALID_RESOURCE_TYPE"),
//                                                              EventStatus.FATAL);
//                    }
//                }
//
//            // Now try to write the Property data back to the database...
//            if (queryData != null)
//                {
//                queryData.executeUpdate(this,
//                                        psProperty,
//                                        RegistryModel.getInstance().getSqlTrace(),
//                                        RegistryModel.getInstance().getSqlTiming());
//                }
//
//            // Record the fact that we have written all of the data for this Property
//            setUpdated(false);
//            }
//
//        catch(ClassCastException exception)
//            {
//            ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                             + " [resourceid=" +  getResourceID() + "]"),
//                                                      0,
//                                                      REGISTRY.getFramework().getRootTask().getID(),
//                                                      this.getClass().getName(),
//                                                      ExceptionData.getException("TODO Cannot cast Resource"),
//                                                      EventStatus.FATAL);
//            }
//
//        catch(NullPointerException exception)
//            {
//            ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                             + " [resourceid=" +  getResourceID() + "]"),
//                                                      0,
//                                                      REGISTRY.getFramework().getRootTask().getID(),
//                                                      this.getClass().getName(),
//                                                      ExceptionData.getException("TODO Cannot update"),
//                                                      EventStatus.FATAL);
//            }
//
//        catch(SQLException exception)
//            {
//            // Errors in RootType will have already been trapped...
//            switch (this.getResourceType())
//                {
//                case ResourceData.FRAMEWORK_RESOURCE:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [resourceid=" +  getResourceID() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("SAVE_FRAMEWORK_PROPERTIES"),
//                                                              EventStatus.FATAL);
//                    break;
//                    }
//
//                case ResourceData.APPLICATION_RESOURCE:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [resourceid=" +  getResourceID() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("SAVE_FRAMEWORK_PROPERTIES"),
//                                                              EventStatus.FATAL);
//                    break;
//                    }
//
//                case ResourceData.COMPONENT_RESOURCE:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [resourceid=" +  getResourceID() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionData.getException("SAVE_FRAMEWORK_PROPERTIES"),
//                                                              EventStatus.FATAL);
//                    break;
//                    }
//
//                default:
//                    {
//                    ExceptionLibrary.handleAtomException(new FrameworkException(exception.toString()
//                                                                                     + " [exceptiontype=" + getResourceType() + "]"),
//                                                              0,
//                                                              REGISTRY.getFramework().getRootTask().getID(),
//                                                              this.getClass().getName(),
//                                                              ExceptionLibrary.INVALID_EXCEPTION_TYPE,
//                                                              EventStatus.FATAL);
//                    }
//                }
//            }
//        }
//
//    /***********************************************************************************************
//     * Create the SQL script to delete this PropertyData.
//     * Remember to remove the PropertyData from the hashtables!
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
//        switch (getResourceType())
//            {
//            case ResourceData.FRAMEWORK_RESOURCE:
//                {
//                bufferScript.append(SQL_DELETE);
//                bufferScript.append(FRAMEWORK_PROPERTIES_TABLE);
//                bufferScript.append(SQL_WHERE);
//                bufferScript.append(FRAMEWORK_PROPERTYID);
//                bufferScript.append(SQL_EQUALS);
//                bufferScript.append(RootData.toSQL(getResourceID()));
//
//                break;
//                }
//
//            case ResourceData.APPLICATION_RESOURCE:
//                {
//                bufferScript.append(SQL_DELETE);
//                bufferScript.append(FRAMEWORK_PROPERTIES_TABLE);
//                bufferScript.append(SQL_WHERE);
//                bufferScript.append(APPLICATION_ID);
//                bufferScript.append(SQL_EQUALS);
//                bufferScript.append(RootData.toSQL(getApplicationID()));
//                bufferScript.append(SQL_AND);
//                bufferScript.append(APPLICATION_PROPERTYID);
//                bufferScript.append(SQL_EQUALS);
//                bufferScript.append(RootData.toSQL(getResourceID()));
//
//                break;
//                }
//
//            case ResourceData.COMPONENT_RESOURCE:
//                {
//                bufferScript.append(SQL_DELETE);
//                bufferScript.append(FRAMEWORK_PROPERTIES_TABLE);
//                bufferScript.append(SQL_WHERE);
//                bufferScript.append(APPLICATION_ID);
//                bufferScript.append(SQL_EQUALS);
//                bufferScript.append(RootData.toSQL(getApplicationID()));
//                bufferScript.append(SQL_AND);
//                bufferScript.append(COMPONENT_ID);
//                bufferScript.append(SQL_EQUALS);
//                bufferScript.append(RootData.toSQL(getPointID()));
//                bufferScript.append(SQL_AND);
//                bufferScript.append(COMPONENT_PROPERTYID);
//                bufferScript.append(SQL_EQUALS);
//                bufferScript.append(RootData.toSQL(getResourceID()));
//
//                break;
//                }
//
//            default:
//                {
//                // ToDo FrameworkID & exception code
//                ExceptionLibrary.handleAtomException(new Exception("[resourcetype=" + getResourceType() + "]"),
//                                                          0,
//                                                          REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionData.getException("INVALID_RESOURCE_TYPE"),
//                                                          EventStatus.FATAL);
//                }
//            }
//
//        return (bufferScript);
//        }
//
//
//    /***********************************************************************************************
//     * Create the SQL script to insert this PropertyData.
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
//        switch (getResourceType())
//            {
//            case ResourceData.FRAMEWORK_RESOURCE:
//                {
//                bufferScript.append(SQL_INSERT);
//                bufferScript.append(FRAMEWORK_PROPERTIES_TABLE);
//                bufferScript.append(SQL_LEFT_PAREN);
//                bufferScript.append(FRAMEWORK_PROPERTYID);
//                bufferScript.append(SQL_COMMA);
//                createInsertValueNames(bufferScript);
//
//                bufferScript.append(SQL_VALUES);
//                createInsertValues(bufferScript);
//
//                break;
//                }
//
//            case ResourceData.APPLICATION_RESOURCE:
//                {
//                bufferScript.append(SQL_INSERT);
//                bufferScript.append(APPLICATION_PROPERTIES_TABLE);
//                bufferScript.append(SQL_LEFT_PAREN);
//                bufferScript.append(APPLICATION_ID);
//                bufferScript.append(SQL_COMMA);
//                bufferScript.append(APPLICATION_PROPERTYID);
//                bufferScript.append(SQL_COMMA);
//                createInsertValueNames(bufferScript);
//
//                bufferScript.append(SQL_VALUES);
//                bufferScript.append(RootData.toSQL(getApplicationID()));
//                bufferScript.append(SQL_COMMA);
//                createInsertValues(bufferScript);
//
//                break;
//                }
//
//            case ResourceData.COMPONENT_RESOURCE:
//                {
//                bufferScript.append(SQL_INSERT);
//                bufferScript.append(COMPONENT_PROPERTIES_TABLE);
//                bufferScript.append(SQL_LEFT_PAREN);
//                bufferScript.append(APPLICATION_ID);
//                bufferScript.append(SQL_COMMA);
//                bufferScript.append(COMPONENT_ID);
//                bufferScript.append(SQL_COMMA);
//                bufferScript.append(COMPONENT_PROPERTYID);
//                bufferScript.append(SQL_COMMA);
//                createInsertValueNames(bufferScript);
//
//                bufferScript.append(SQL_VALUES);
//                bufferScript.append(RootData.toSQL(getApplicationID()));
//                bufferScript.append(SQL_COMMA);
//                bufferScript.append(RootData.toSQL(getPointID()));
//                bufferScript.append(SQL_COMMA);
//                createInsertValues(bufferScript);
//
//                break;
//                }
//
//            default:
//                {
//                // ToDo FrameworkID & exception code
//                ExceptionLibrary.handleAtomException(new Exception("[resourcetype=" + getResourceType() + "]"),
//                                                          0,
//                                                          REGISTRY.getFramework().getRootTask().getID(),
//                                                          this.getClass().getName(),
//                                                          ExceptionData.getException("INVALID_RESOURCE_TYPE"),
//                                                          EventStatus.FATAL);
//                }
//            }
//
//        return(bufferScript);
//        }
//
//
//    /***********************************************************************************************
//     *
//     * @param buffer
//     */
//
//    private void createInsertValues(final StringBuffer buffer)
//        {
//        buffer.append(RootData.toSQL(getResourceID()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(getResourceKey0()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(getResourceKey1()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(getResourceKey2()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(getResourceKey3()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(isEditable()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(DataType.getDataTypeID(getChannelDataType())));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(EditorUtilities.getResourceEditorID(getEditorClassname())));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(getResource().toString()));
//        buffer.append(SQL_COMMA);
//        buffer.append(RootData.toSQL(getDescription()));
//        buffer.append(SQL_RIGHT_PAREN);
//        }
    /***********************************************************************************************
     *
     * @param buffer
     */

//    private static void createInsertValueNames(final StringBuffer buffer)
//        {
//        buffer.append(RESOURCE_KEY0);
//        buffer.append(SQL_COMMA);
//        buffer.append(RESOURCE_KEY1);
//        buffer.append(SQL_COMMA);
//        buffer.append(RESOURCE_KEY2);
//        buffer.append(SQL_COMMA);
//        buffer.append(RESOURCE_KEY3);
//        buffer.append(SQL_COMMA);
//        buffer.append(EDITABLE);
//        buffer.append(SQL_COMMA);
//        buffer.append(DATATYPE_ID);
//        buffer.append(SQL_COMMA);
//        buffer.append(EDITOR_ID);
//        buffer.append(SQL_COMMA);
//        buffer.append(PROPERTY_VALUE);
//        buffer.append(SQL_COMMA);
//        buffer.append(PROPERTY_DESCRIPTION);
//        }



    }
