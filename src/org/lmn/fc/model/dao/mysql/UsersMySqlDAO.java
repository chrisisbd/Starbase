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

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.dao.DAOUtilities;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.dao.UsersDAOInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/***************************************************************************************************
 * The UsersMySqlDAO.
 */

public final class UsersMySqlDAO implements UsersDAOInterface
    {
    private static final DataStore DATA_STORE = DataStore.MYSQL;
    private static final FrameworkDatabase DATABASE = FrameworkDatabase.getInstance();


    /***********************************************************************************************
     * Construct the UsersMySqlDAO.
     */

    public UsersMySqlDAO()
        {
        }


    public void importUsers() throws FrameworkException
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    public void exportUsers() throws FrameworkException
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    /***********************************************************************************************
     * Delete.User.All.
     *
     * @throws FrameworkException
     */

    public final void deleteAllUsers() throws FrameworkException
        {
        final QueryPlugin queryPlugin;
        final PreparedStatement psDelete;

        System.out.println("in deleteAllUsers key=" + REGISTRY.getFramework().getResourceKey() + QUERY_DELETE_USER_ALL);
        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + QUERY_DELETE_USER_ALL);

            if (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE))
                {
                System.out.println(" getting prep stat");
                psDelete = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);
                System.out.println("exec update");
                queryPlugin.executeUpdate(this,
                                          psDelete,
                                          RegistryModel.getInstance().getSqlTrace(),
                                          RegistryModel.getInstance().getSqlTiming());
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_DELETE_DATA, exception);
            }
        }


    /***********************************************************************************************
     * Delete.User.ByID.
     *
     * @param user
     *
     * @throws FrameworkException
     */

    public final void deleteUser(final UserPlugin user) throws FrameworkException
        {
        final QueryPlugin queryPlugin;
        final PreparedStatement psDelete;

        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + QUERY_DELETE_USER_BYID);

            if ((RegistryModelUtilities.isValidUser(user))
                && (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE)))
                {
                psDelete = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

                psDelete.setLong(1, user.getID());
                queryPlugin.executeUpdate(this,
                                        psDelete,
                                        RegistryModel.getInstance().getSqlTrace(),
                                        RegistryModel.getInstance().getSqlTiming());
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_DELETE_DATA, exception);
            }
        }


    /***********************************************************************************************
     * Insert.User.Data.
     *
     * @param username
     * @param password
     * @param active
     * @param rolePlugin
     *
     * @return UserData
     *
     * @throws FrameworkException
     */

    public final UserPlugin insertUser(final String username,
                                       final String password,
                                       final boolean active,
                                       final RolePlugin rolePlugin) throws FrameworkException
        {
        UserPlugin userPlugin;
        final QueryPlugin queryPlugin;
        final PreparedStatement psInsert;

        userPlugin = null;

        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + QUERY_INSERT_USER_DATA);

            if ((username != null)
                && (!EMPTY_STRING.equals(username.trim()))
                && (password != null)
                && (!EMPTY_STRING.equals(password.trim()))
                && (rolePlugin != null)
                && (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE)))
                {
                psInsert = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

                psInsert.setString(1, username.trim().toLowerCase());
                psInsert.setString(2, password.trim());
                psInsert.setBoolean(3, active);
                psInsert.setLong(4, rolePlugin.getID());
                queryPlugin.executeUpdate(this,
                                          psInsert,
                                          RegistryModel.getInstance().getSqlTrace(),
                                          RegistryModel.getInstance().getSqlTiming());

                // Read back the new user into a UserData
                userPlugin = selectUser(username, password);
                }
            }

        catch(SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_INSERT_DATA, exception);
            }

        // Double check that the User is valid now...
        if (!RegistryModelUtilities.isValidUser(userPlugin))
            {
            userPlugin = null;
            }

        return (userPlugin);
        }


    /***********************************************************************************************
     * Select.User.Data.
     *
     * @param username
     * @param password
     *
     * @return UserData
     *
     * @throws FrameworkException
     */

    public final UserPlugin selectUser(final String username,
                                       final String password) throws FrameworkException
        {
        UserPlugin userPlugin;
        final QueryPlugin queryPlugin;
        final PreparedStatement psSelect;
        final ResultSet rsSelect;

        userPlugin = null;

        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + QUERY_SELECT_USER_DATA);

            if ((username != null)
                && (!EMPTY_STRING.equals(username.trim()))
                && (password != null)
                && (!EMPTY_STRING.equals(password.trim()))
                && (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE)))
                {
                psSelect = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

                psSelect.setString(1, username.trim().toLowerCase());
                psSelect.setString(2, password.trim());
                rsSelect = queryPlugin.executeQuery(this,
                                                  psSelect,
                                                  RegistryModel.getInstance().getSqlTrace(),
                                                  RegistryModel.getInstance().getSqlTiming());
                // Did we find the User?
                if (rsSelect.next())
                    {
                    // The username is case insensitive, but the password must match exactly!
                    if ((username.trim().equalsIgnoreCase(rsSelect.getString(USERNAME).trim()))
                        && (password.trim().equals(rsSelect.getString(PASSWORD).trim())))
                        {
                        System.out.println("found the user!! make UserData and carry on...");
//                        final RoleData roleData = new RoleData(rsSelect.getInt(RoleData.ROLE_ID),
//                                                               rsSelect.getString(RoleData.ROLE_NAME),
//                                                               rsSelect.getBoolean(RoleData.READ_PERMISSION),
//                                                               rsSelect.getBoolean(RoleData.MODIFY_PERMISSION),
//                                                               rsSelect.getBoolean(RoleData.DELETE_PERMISSION),
//                                                               rsSelect.getString(RoleData.DESCRIPTION));
                        // ToDo change role
//                        final RolePlugin rolePlugin = REGISTRY.getRole(RoleName.ADMINISTRATOR.toString());
//                        userPlugin = new UserData(rsSelect.getInt(USER_ID),
//                                                  username.trim().toLowerCase(),
//                                                  password.trim(),
//                                                  rsSelect.getBoolean(USER_ACTIVE),
//                                                  rsSelect.getDate(LASTLOGIN_DATE),
//                                                  rsSelect.getTime(LASTLOGIN_TIME),
//                                                  rolePlugin,
//                                                  // ToDo FIX THIS!!
//                                                  REGISTRY.getCountry("GB"),
//                                                  REGISTRY.getLanguage("en"));
                        }
                    }

                rsSelect.close();
                }
            }

        catch(SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_SELECT_DATA, exception);
            }

        // Double check that the User is valid now...
        if (!RegistryModelUtilities.isValidUser(userPlugin))
            {
            userPlugin = null;
            }

        return (userPlugin);
        }


    /***********************************************************************************************
     * Update.User.Data.
     *
     * @param user
     *
     * @throws FrameworkException
     */

    public final void updateUser(final UserPlugin user) throws FrameworkException
        {
        final QueryPlugin queryPlugin;
        final PreparedStatement psUpdate;

        try
            {
            // Update all UserData
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + QUERY_UPDATE_USER_DATA);

            if ((RegistryModelUtilities.isValidUser(user))
                && (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE)))
                {
                psUpdate = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

                psUpdate.setString(1, user.getName().trim().toLowerCase());
                psUpdate.setString(2, user.getPassword().trim());
                psUpdate.setBoolean(3, user.isActive());
                psUpdate.setString(4, user.getRoleName());
                psUpdate.setDate(5, user.getDateLastLogin());
                psUpdate.setTime(6, user.getTimeLastLogin());

                // WHERE
                psUpdate.setLong(7, user.getID());

                queryPlugin.executeUpdate(this,
                                        psUpdate,
                                        RegistryModel.getInstance().getSqlTrace(),
                                        RegistryModel.getInstance().getSqlTiming());
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_UPDATE_DATA, exception);
            }
        }


    /***********************************************************************************************
     * Update only the LastLogin Date and Time for the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     */

    public final void updateLastLogin(final UserPlugin user) throws FrameworkException
        {
        final QueryPlugin queryPlugin;
        final PreparedStatement psUpdate;

        try
            {
            // Write back the login Date and Time
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + QUERY_UPDATE_USER_LOGIN);

            if ((RegistryModelUtilities.isValidUser(user))
                && (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE)))
                {
                psUpdate = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

                psUpdate.setDate(1, user.getDateLastLogin());
                psUpdate.setTime(2, user.getTimeLastLogin());

                // WHERE
                psUpdate.setLong(3, user.getID());

                queryPlugin.executeUpdate(this,
                                        psUpdate,
                                        RegistryModel.getInstance().getSqlTrace(),
                                        RegistryModel.getInstance().getSqlTiming());
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_UPDATE_DATA, exception);
            }
        }
    }
