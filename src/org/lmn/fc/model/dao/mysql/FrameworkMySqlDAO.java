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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.dao.DAOUtilities;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public final class FrameworkMySqlDAO implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata
    {
    private static final Logger LOGGER = Logger.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    private static final FrameworkDatabase DATABASE = FrameworkDatabase.getInstance();

    // String Resources
    public static final String SYSTEM_REPORT                = "SystemProperties";
    private static final String EXCEPTION_NO_NAME           = "selectData() no Framework name";
    private static final String EXCEPTION_SQL_READ          = "selectData() SQL";
    private static final String EXCEPTION_DEGMINSEC         = "selectData() DegMinSec";
    private static final String EXCEPTION_SQL_WRITE         = "updateData()";

    // Column names in Frameworks table
    private static final String FRAMEWORKS_TABLE             = "Frameworks";
    private static final String FRAMEWORK_ID                 = "FrameworkID";
    private static final String USER_ROLE_MASK               = "UserRoleMask";
    private static final String FRAMEWORK_NAME               = "FrameworkName";
    private static final String FRAMEWORK_EDITABLE           = "FrameworkEditable";
    private static final String FRAMEWORK_ACTIVE             = "FrameworkActive";
    private static final String FRAMEWORK_LOADATSTART        = "FrameworkLoadAtStart";
    private static final String EDITOR_ID                    = "EditorID";
    private static final String FRAMEWORK_DESCRIPTION        = "FrameworkDescription";
    private static final String FRAMEWORK_LANGUAGE           = "FrameworkLanguage";
    private static final String FRAMEWORK_COUNTRY            = "FrameworkCountry";
    private static final String FRAMEWORK_TIMEZONE           = "FrameworkTimezone";
    private static final String FRAMEWORK_LONGITUDE          = "FrameworkLongitude";
    private static final String FRAMEWORK_LATITUDE           = "FrameworkLatitude";
    private static final String SELECT_QUERY_LIST            = "SelectFrameworkQueryList";
    private static final String SELECT_QUERY_BYNAME          = "SelectFrameworkQueryByName";
    private static final String EXPORTS_FOLDER               = "FrameworkExportsFolder";
    private static final String SPLASHSCREEN_FILENAME        = "FrameworkSplashScreenFilename";
    private static final String ICON_FILENAME                = "FrameworkIconFilename";
    private static final String HELP_FILENAME                = "FrameworkHelpFilename";
    private static final String ABOUT_FILENAME               = "FrameworkAboutFilename";
    private static final String MAP_FILENAME                 = "FrameworkMapFilename";
    private static final String MAP_TOPLEFT_LONGITUDE        = "FrameworkMapTopLeftLongitude";
    private static final String MAP_TOPLEFT_LATITUDE         = "FrameworkMapTopLeftLatitude";
    private static final String MAP_BOTTOMRIGHT_LONGITUDE    = "FrameworkMapBottomRightLongitude";
    private static final String MAP_BOTTOMRIGHT_LATITUDE     = "FrameworkMapBottomRightLatitude";
    private static final String DATE_CREATED                 = "DateCreated";
    private static final String TIME_CREATED                 = "TimeCreated";
    private static final String DATE_MODIFIED                = "DateModified";
    private static final String TIME_MODIFIED                = "TimeModified";

    // The FrameworkMySqlDAO is a singleton!
    private static final FrameworkMySqlDAO DAO_FRAMEWORK_SQL = new FrameworkMySqlDAO();

    // Column name in the Editors table, joined on EditorID
    private static final String EDITOR_CLASSNAME             = EditorUIComponent.EDITOR_CLASSNAME;

    // Queries
    public static final String SELECT_FRAMEWORK_LOADATSTART = "Select.Framework.LoadAtStart";
    private static final String SELECT_FRAMEWORK_BYID        = "Select.Framework.Data.ByID";
    private static final String UPDATE_FRAMEWORK             = "Update.Framework.Data";

    public static final int DESCRIPTION_LENGTH = 255;
    public static final int QUERY_LENGTH = 255;

    private final FrameworkPlugin pluginFramework;


    /***********************************************************************************************
     * Get an instance of the FrameworkMySqlDAO.
     *
     * @return FrameworkMySqlDAO
     */

    public static FrameworkMySqlDAO getInstance()
        {
        return (DAO_FRAMEWORK_SQL);
        }


    /***********************************************************************************************
     * Initialise the XML Bean data for the Framework from the XML Object.
     *
     *
     * @throws DegMinSecException
     */

//    private static void initialiseFrameworkFromXML(final FrameworkData framework,
//                                                   final FrameworksDocument.Frameworks.Framework xmlobject) throws DegMinSecException
//        {
//        framework.setName(xmlobject.getName());
//        framework.setUserRoleMask(xmlobject.getUserRoleMask());
//        framework.setEditable(xmlobject.isEditable());
//        framework.setActive(xmlobject.isActive());
//        framework.setLoadAtStart(xmlobject.isLoadAtStart());
//        framework.setEditorClassname(xmlobject.getEditorClassname());
//        framework.setDescription(xmlobject.getDescription());
//        framework.setLanguageISOCode(xmlobject.getLanguage());
//        framework.setCountryISOCode(xmlobject.getCountry());
//        framework.setTimeZoneCode(xmlobject.getTimezone());
//        framework.setLongitude(DegMinSec.parseDMS(xmlobject.getLongitude(), DELIMITER_LONGITUDE));
//        framework.setLatitude(DegMinSec.parseDMS(xmlobject.getLatitude(), DELIMITER_LATITUDE));
//        framework.setSelectQueryList(xmlobject.getSelectQueryList());
//        framework.setSelectQueryByName(xmlobject.getSelectQueryByName());
//        framework.setExportsFolder(xmlobject.getExportsFolder());
//        framework.setSplashScreenFilename(xmlobject.getSplashScreenFilename());
//        framework.setIconFilename(xmlobject.getIconFilename());
//        framework.setHelpFilename(xmlobject.getHelpFilename());
//        framework.setAboutFilename(xmlobject.getAboutFilename());
//        framework.setMapFilename(xmlobject.getMapFilename());
//        framework.setMapTopLeftLongitude(DegMinSec.parseDMS(xmlobject.getMapTopLeftLongitude(), DELIMITER_LONGITUDE));
//        framework.setMapTopLeftLatitude(DegMinSec.parseDMS(xmlobject.getMapTopLeftLatitude(), DELIMITER_LATITUDE));
//        framework.setMapBottomRightLongitude(DegMinSec.parseDMS(xmlobject.getMapBottomRightLongitude(), DELIMITER_LONGITUDE));
//        framework.setMapBottomRightLatitude(DegMinSec.parseDMS(xmlobject.getMapBottomRightLatitude(), DELIMITER_LATITUDE));
//        }


//    /***********************************************************************************************
//     * Find the Framework marked <code>LoadAtStart</code> and return its FrameworkID.
//     * Return <code><b>null</b></code> if no Framework is found.
//     * This should be used only by the FrameworkLoader.
//     *
//     * @param frameworkmodel
//     * @param bootstrapquery
//     *
//     * @return int
//     *
//     * @throws DegMinSecException
//     * @throws SQLException
//     */

//    public static FrameworkPlugin getInitialFramework(final String bootstrapquery) throws SQLException,
//                                                                                              DegMinSecException
//        {
//        final PreparedStatement psFramework;
//        final ResultSet rsFramework;
//        FrameworkPlugin pluginFramework;
//        final DefaultMutableTreeNode nodeFramework;
//
//        // Assume that we won't find a Framework...
//        pluginFramework = null;
//
//        // Read the Frameworks table to find the LoadAtStart FrameworkData
//        // using the supplied bootstrap query string (not a QueryData)
//        // We can't use selectData() because the QueryData don't exist yet...
//        if ((RegistryModel.getInstance() != null)
//            && (FrameworkDatabase.getInstance() != null)
//            && (FrameworkDatabase.getInstance().getConnection() != null)
//            && (!FrameworkDatabase.getInstance().getConnection().isClosed())
//            && (bootstrapquery != null)
//            && (!bootstrapquery.trim().equals("")))
//            {
//            psFramework = FrameworkDatabase.getInstance().getConnection().prepareStatement(bootstrapquery);
//            rsFramework = psFramework.executeQuery();
//
//            while(rsFramework.next())
//                {
//                // There must be only one Framework Root node!
//                nodeFramework = new DefaultMutableTreeNode();
//                nodeFramework.setAllowsChildren(true);
//
//                // Create an unitialised FrameworkData
////                pluginFramework = new FrameworkData(frameworkmodel,
////                                                  rsFramework.getInt(FRAMEWORK_ID),
////                                                  nodeFramework);
//
//                pluginFramework = REGISTRY.getFramework();
//
//                // Prepare to populate the FrameworkData
//                pluginFramework.suspendUpdates();
//
//                // We must have a FrameworkName...
//                pluginFramework.setName(rsFramework.getString(FRAMEWORK_NAME));
//
//                if ((pluginFramework.getName() == null)
//                    || (pluginFramework.getName().equals("")))
//                    {
//                    throw new SQLException(REGISTRY.getException(EXCEPTION_NO_NAME));
//                    }
//
//                //Todo pluginFramework.setUserRoles(rsFramework.getLong(USER_ROLE_MASK));
//                pluginFramework.setEditable(rsFramework.getBoolean(FRAMEWORK_EDITABLE));
//                pluginFramework.setActive(rsFramework.getBoolean(FRAMEWORK_ACTIVE));
//                pluginFramework.setLoadAtStart(rsFramework.getBoolean(FRAMEWORK_LOADATSTART));
//                pluginFramework.setEditorClassname(rsFramework.getString(EDITOR_CLASSNAME));
//                pluginFramework.setDescription(rsFramework.getString(FRAMEWORK_DESCRIPTION));
//                pluginFramework.setLanguageISOCode(rsFramework.getString(FRAMEWORK_LANGUAGE));
//                pluginFramework.setCountryISOCode(rsFramework.getString(FRAMEWORK_COUNTRY));
//                pluginFramework.setTimeZoneCode(rsFramework.getString(FRAMEWORK_TIMEZONE));
//                pluginFramework.setLongitude(
//                        DegMinSecRootParser.parseDMS(rsFramework.getString(FRAMEWORK_LONGITUDE), FrameworkConstants.DELIMITER_LONGITUDE));
//                pluginFramework.setLatitude(
//                        DegMinSecRootParser.parseDMS(rsFramework.getString(FRAMEWORK_LATITUDE), FrameworkConstants.DELIMITER_LATITUDE));
////                pluginFramework.setSelectQueryList(rsFramework.getString(SELECT_QUERY_LIST));
////                pluginFramework.setSelectQueryByName(rsFramework.getString(SELECT_QUERY_BYNAME));
////                pluginFramework.setExportsFolder(rsFramework.getString(EXPORTS_FOLDER));
//                pluginFramework.setSplashScreenFilename(rsFramework.getString(SPLASHSCREEN_FILENAME));
//                pluginFramework.setIconFilename(rsFramework.getString(ICON_FILENAME));
//                pluginFramework.setHelpFilename(rsFramework.getString(HELP_FILENAME));
//                pluginFramework.setAboutFilename(rsFramework.getString(ABOUT_FILENAME));
//                pluginFramework.setMapFilename(rsFramework.getString(MAP_FILENAME));
//                pluginFramework.setMapTopLeftLongitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_TOPLEFT_LONGITUDE), FrameworkConstants.DELIMITER_LONGITUDE));
//                pluginFramework.setMapTopLeftLatitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_TOPLEFT_LATITUDE), FrameworkConstants.DELIMITER_LATITUDE));
//                pluginFramework.setMapBottomRightLongitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_BOTTOMRIGHT_LONGITUDE), FrameworkConstants.DELIMITER_LONGITUDE));
//                pluginFramework.setMapBottomRightLatitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_BOTTOMRIGHT_LATITUDE), FrameworkConstants.DELIMITER_LATITUDE));
//
//                GregorianCalendar calDateCreated = new GregorianCalendar();
//                calDateCreated.setTimeInMillis(rsFramework.getDate(DATE_CREATED).getTime());
//                pluginFramework.setCreatedDate(calDateCreated);
//
//                GregorianCalendar calTimeCreated = new GregorianCalendar();
//                calTimeCreated.setTimeInMillis(rsFramework.getTime(TIME_CREATED).getTime());
//                pluginFramework.setCreatedTime(calTimeCreated);
//
//                GregorianCalendar calDateModified = new GregorianCalendar();
//                calDateModified.setTimeInMillis(rsFramework.getDate(DATE_MODIFIED).getTime());
//                pluginFramework.setModifiedDate(calDateModified);
//
//                GregorianCalendar calTimeModified = new GregorianCalendar();
//                calTimeModified.setTimeInMillis(rsFramework.getTime(TIME_MODIFIED).getTime());
//                pluginFramework.setModifiedTime(calTimeModified);
//
//                // Record the fact that we have read all of the data
//                pluginFramework.setUpdated(false);
//
//                // Create QueryData for the Bootstrap Queries
//                // so that the FrameworkLoader and ResourceLoader will work
////                QueriesMySqlDAO.setBootstrapQueries(FrameworkDatabase.getInstance(),
////                                              bootstrapquery,
////                                              pluginFramework.getSelectQueryList(),
////                                              pluginFramework.getSelectQueryByName(),
////                                              pluginFramework.getDebugMode());
//
//                pluginFramework.resumeUpdates();
//                pluginFramework.showDebugData();
//
//                // Grab the first Framework marked 'LoadAtStart' and leave
//                break;
//                }
//
//            // Do not use pluginFramework here, because it may be null
//            rsFramework.close();
//            }
//
//        return (pluginFramework);
//        }


    /***********************************************************************************************
     *
     */

    private FrameworkMySqlDAO()
        {
        pluginFramework = REGISTRY.getFramework();
        }


    /***********************************************************************************************
     *
     * @return FrameworkPlugin
     */

    private FrameworkPlugin getFramework()
        {
        return(this.pluginFramework);
        }


    /***********************************************************************************************
     * Load the Framework object with data from the database.
     * The FrameworkID must be already set; all other data are reloaded.
     */

//    public final void selectData()
//        {
//        final PreparedStatement psFramework;
//        final ResultSet rsFramework;
//        final ResultSetMetaData mdApplication;
//        final int intColumnCount;
//        final QueryPlugin queryPlugin;
//
//        // Read the FrameworkData
//        // There must be only one Framework Root node!
//        try
//            {
//            queryPlugin = REGISTRY.getQueryData(SELECT_FRAMEWORK_BYID);
//            psFramework = queryPlugin.getPreparedStatement(DATABASE, DataStore.MYSQL);
//            psFramework.setLong(1, REGISTRY.getFramework().getID());
//
//            rsFramework = queryPlugin.executeQuery(this,
//                                                 psFramework,
//                                                 RegistryModel.getInstance().getSqlTrace(),
//                                                 RegistryModel.getInstance().getSqlTiming());
//
//            mdApplication = psFramework.getMetaData();
//            intColumnCount = mdApplication.getColumnCount();
//
//            LOGGER.log( ".selectData [intColumnCount=" + intColumnCount + "]");
//            getFramework().suspendUpdates();
//
//            while(rsFramework.next())
//                {
//                // Prepare the User Object for the TreeNode
//                // Change here if the table design changes!
//
//                // We must have a FrameworkName...
//                getFramework().setName(rsFramework.getString(FRAMEWORK_NAME));
//
//                if ((getFramework().getName() == null)
//                    || (getFramework().getName().equals("")))
//                    {
//                    throw new SQLException(REGISTRY.getException(EXCEPTION_NO_NAME));
//                    }
//
//                //todo getFramework().setUserRoles(rsFramework.getLong(USER_ROLE_MASK));
//                getFramework().setEditable(rsFramework.getBoolean(FRAMEWORK_EDITABLE));
//                getFramework().setActive(rsFramework.getBoolean(FRAMEWORK_ACTIVE));
//                getFramework().setLoadAtStart(rsFramework.getBoolean(FRAMEWORK_LOADATSTART));
//                getFramework().setEditorClassname(rsFramework.getString(EDITOR_CLASSNAME));
//                getFramework().setDescription(rsFramework.getString(FRAMEWORK_DESCRIPTION));
//                getFramework().setLanguageISOCode(rsFramework.getString(FRAMEWORK_LANGUAGE));
//                getFramework().setCountryISOCode(rsFramework.getString(FRAMEWORK_COUNTRY));
//                getFramework().setTimeZoneCode(rsFramework.getString(FRAMEWORK_TIMEZONE));
//                getFramework().setLongitude(DegMinSecRootParser.parseDMS(rsFramework.getString(FRAMEWORK_LONGITUDE), DELIMITER_LONGITUDE));
//                getFramework().setLatitude(DegMinSecRootParser.parseDMS(rsFramework.getString(FRAMEWORK_LATITUDE), DELIMITER_LATITUDE));
////                getFramework().setSelectQueryList(rsFramework.getString(SELECT_QUERY_LIST));
////                getFramework().setSelectQueryByName(rsFramework.getString(SELECT_QUERY_BYNAME));
////                getFramework().setExportsFolder(rsFramework.getString(EXPORTS_FOLDER));
//                getFramework().setSplashScreenFilename(rsFramework.getString(SPLASHSCREEN_FILENAME));
//                getFramework().setIconFilename(rsFramework.getString(ICON_FILENAME));
//                getFramework().setHelpFilename(rsFramework.getString(HELP_FILENAME));
//                getFramework().setAboutFilename(rsFramework.getString(ABOUT_FILENAME));
//                getFramework().setMapFilename(rsFramework.getString(MAP_FILENAME));
//                getFramework().setMapTopLeftLongitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_TOPLEFT_LONGITUDE), DELIMITER_LONGITUDE));
//                getFramework().setMapTopLeftLatitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_TOPLEFT_LATITUDE), DELIMITER_LATITUDE));
//                getFramework().setMapBottomRightLongitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_BOTTOMRIGHT_LONGITUDE), DELIMITER_LONGITUDE));
//                getFramework().setMapBottomRightLatitude(DegMinSecRootParser.parseDMS(rsFramework.getString(MAP_BOTTOMRIGHT_LATITUDE), DELIMITER_LATITUDE));
//
//                GregorianCalendar calDateCreated = new GregorianCalendar();
//                calDateCreated.setTimeInMillis(rsFramework.getDate(DATE_CREATED).getTime());
//                getFramework().setCreatedDate(calDateCreated);
//
//                GregorianCalendar calTimeCreated = new GregorianCalendar();
//                calTimeCreated.setTimeInMillis(rsFramework.getTime(TIME_CREATED).getTime());
//                getFramework().setCreatedTime(calTimeCreated);
//
//                GregorianCalendar calDateModified = new GregorianCalendar();
//                calDateModified.setTimeInMillis(rsFramework.getDate(DATE_MODIFIED).getTime());
//                getFramework().setModifiedDate(calDateModified);
//
//                GregorianCalendar calTimeModified = new GregorianCalendar();
//                calTimeModified.setTimeInMillis(rsFramework.getTime(TIME_MODIFIED).getTime());
//                getFramework().setModifiedTime(calTimeModified);
//
//                // ToDo update the two bootstrap QueryData which should already exist
//                // to correspond with the new loaded query SQL Strings
//
//
//                // Record the fact that we have re-read all of the data
//                getFramework().setUpdated(false);
//                }
//
//            rsFramework.close();
//            getFramework().resumeUpdates();
//            }
//
//        catch(SQLException exception)
//            {
//            getFramework().handleException(exception,
//                                           REGISTRY.getException(EXCEPTION_SQL_READ),
//                                           EventStatus.FATAL);
//            }
//
//        catch(DegMinSecException exception)
//            {
//            getFramework().handleException(exception,
//                                           REGISTRY.getException(EXCEPTION_DEGMINSEC),
//                                           EventStatus.FATAL);
//            }
//        }
//

    /***********************************************************************************************
     * Write the FrameworkData object back to the database.
     */

    public final void updateData()
        {
        final PreparedStatement psFramework;
        int intParameter;
        final QueryPlugin queryPlugin;

        if (getFramework().isUpdateAllowed())
            {
            try
                {
                queryPlugin = REGISTRY.getQueryData(UPDATE_FRAMEWORK);
                psFramework = queryPlugin.getPreparedStatement(DATABASE, DataStore.MYSQL);

                intParameter = 1;
                //todo psFramework.setLong(intParameter++, getFramework().getUserRoles());
                psFramework.setString(intParameter++, getFramework().getName());
                psFramework.setBoolean(intParameter++, getFramework().isEditable());
                psFramework.setBoolean(intParameter++, getFramework().isActive());
                psFramework.setBoolean(intParameter++, getFramework().isLoadAtStart());
//                psFramework.setString(intParameter++, Integer.toString(EditorUtilities.getResourceEditorID(getFramework().getEditorClassname())));
                psFramework.setString(intParameter++, getFramework().getDescription());
                psFramework.setString(intParameter++, getFramework().getLanguageISOCode());
                psFramework.setString(intParameter++, getFramework().getCountryISOCode());
                psFramework.setString(intParameter++, getFramework().getTimeZoneCode());
                psFramework.setString(intParameter++, getFramework().getLongitude().toString());
                psFramework.setString(intParameter++, getFramework().getLatitude().toString());
//                psFramework.setString(intParameter++, getFramework().getSelectQueryList());
//                psFramework.setString(intParameter++, getFramework().getSelectQueryByName());
//                psFramework.setString(intParameter++, getFramework().getExportsFolder());
                psFramework.setString(intParameter++, getFramework().getSplashScreenFilename());
                psFramework.setString(intParameter++, getFramework().getIconFilename());
                psFramework.setString(intParameter++, getFramework().getHelpFilename());
                psFramework.setString(intParameter++, getFramework().getAboutFilename());
                psFramework.setString(intParameter++, getFramework().getMapFilename());
                psFramework.setString(intParameter++, getFramework().getMapTopLeftLongitude().toString());
                psFramework.setString(intParameter++, getFramework().getMapTopLeftLatitude().toString());
                psFramework.setString(intParameter++, getFramework().getMapBottomRightLongitude().toString());
                psFramework.setString(intParameter++, getFramework().getMapBottomRightLatitude().toString());

                // TODO don't forget this!!
//                psFramework.setDate(intParameter++, getFramework().getCreatedDate());
//                psFramework.setTime(intParameter++, getFramework().getCreatedTime());
//                psFramework.setDate(intParameter++, getFramework().getModifiedDate());
//                psFramework.setTime(intParameter++, getFramework().getModifiedTime());

                // WHERE
                psFramework.setLong(intParameter, REGISTRY.getFramework().getID());

                queryPlugin.executeUpdate(this,
                                        psFramework,
                                        RegistryModel.getInstance().getSqlTrace(),
                                        RegistryModel.getInstance().getSqlTiming());

                // Record the fact that we have written all of the data
                getFramework().setUpdated(false);
                }

            catch(SQLException exception)
                {
                getFramework().handleException(exception,
                                               REGISTRY.getException(EXCEPTION_SQL_WRITE),
                                               EventStatus.FATAL);
                }
            }
        }


    /***********************************************************************************************
     * Create the SQL script to delete this FrameworkData.
     *
     * @return StringBuffer
     */

    public final StringBuffer createSQLDeleteScript()
        {
        final StringBuffer bufferScript;

        bufferScript = new StringBuffer();
        bufferScript.append(SQL_DELETE);
        bufferScript.append(FRAMEWORKS_TABLE);
        bufferScript.append(SQL_WHERE);
        bufferScript.append(FRAMEWORK_ID);
        bufferScript.append(SQL_EQUALS);
        bufferScript.append(REGISTRY.getFramework().getID());

        return (bufferScript);
        }


    /***********************************************************************************************
     * Create the SQL script to install this FrameworkData.
     *
     * @return StringBuffer
     */

    public final StringBuffer createSQLInsertScript()
        {
        final StringBuffer bufferScript;
        bufferScript = new StringBuffer();
        bufferScript.append(SQL_INSERT);
        bufferScript.append(FRAMEWORKS_TABLE);
        bufferScript.append(SQL_LEFT_PAREN);
        bufferScript.append(FRAMEWORK_ID);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(USER_ROLE_MASK);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_NAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_EDITABLE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_ACTIVE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_LOADATSTART);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(EDITOR_ID);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_DESCRIPTION);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_LANGUAGE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_COUNTRY);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_TIMEZONE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_LONGITUDE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(FRAMEWORK_LATITUDE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(SELECT_QUERY_LIST);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(SELECT_QUERY_BYNAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(EXPORTS_FOLDER);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(SPLASHSCREEN_FILENAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(ICON_FILENAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(HELP_FILENAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(ABOUT_FILENAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(MAP_FILENAME);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(MAP_TOPLEFT_LONGITUDE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(MAP_TOPLEFT_LATITUDE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(MAP_BOTTOMRIGHT_LONGITUDE);
        bufferScript.append(SQL_COMMA);
        bufferScript.append(MAP_BOTTOMRIGHT_LATITUDE);

        bufferScript.append(SQL_VALUES);
        bufferScript.append(REGISTRY.getFramework().getID());
        bufferScript.append(SQL_COMMA);
// todo       bufferScript.append(DAOUtilities.toSQL(getFramework().getUserRoles()));
//        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getName()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().isEditable()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().isActive()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().isLoadAtStart()));
        bufferScript.append(SQL_COMMA);
//        bufferScript.append(EditorUtilities.getResourceEditorID(getFramework().getEditorClassname()));
//        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getDescription()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getLanguageISOCode()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getCountryISOCode()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getTimeZoneCode()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getLongitude().toString()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getLatitude().toString()));
        bufferScript.append(SQL_COMMA);
//        bufferScript.append(DAOUtilities.toSQL(getFramework().getSelectQueryList()));
//        bufferScript.append(SQL_COMMA);
//        bufferScript.append(DAOUtilities.toSQL(getFramework().getSelectQueryByName()));
//        bufferScript.append(SQL_COMMA);
//        bufferScript.append(DAOUtilities.toSQL(getFramework().getExportsFolder()));
//        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getSplashScreenFilename()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getIconFilename()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getHelpFilename()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getAboutFilename()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getMapFilename()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getMapTopLeftLongitude().toString()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getMapTopLeftLatitude().toString()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getMapBottomRightLongitude().toString()));
        bufferScript.append(SQL_COMMA);
        bufferScript.append(DAOUtilities.toSQL(getFramework().getMapBottomRightLatitude().toString()));
        bufferScript.append(SQL_RIGHT_PAREN);

        return (bufferScript);
        }
    }
