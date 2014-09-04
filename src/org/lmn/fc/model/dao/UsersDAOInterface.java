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

package org.lmn.fc.model.dao;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;


/***************************************************************************************************
 * The UserDAO Interface.
 */

public interface UsersDAOInterface extends FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
    ResourceKeys
    {
    String USERS_XML = "/users.xml";

    Logger LOGGER = Logger.getInstance();
    RegistryPlugin REGISTRY = Registry.getInstance();

    // Query Keys
    String QUERY_DELETE_USER_ALL    = "Delete.User.All";
    String QUERY_DELETE_USER_BYID   = "Delete.User.ByID";
    String QUERY_INSERT_USER_DATA   = "Insert.User.Data";
    String QUERY_SELECT_USER_DATA   = "Select.User.Data";
    String QUERY_UPDATE_USER_DATA   = "Update.User.Data";
    String QUERY_UPDATE_USER_LOGIN  = "Update.User.Login";
    String USER_ID          = "UserID";
    String USERNAME         = "Username";
    String PASSWORD         = "Password";
    String USER_ACTIVE      = "UserActive";
    String ROLE_ID          = "RoleID";
    String LASTLOGIN_DATE   = "LastLoginDate";
    String LASTLOGIN_TIME   = "LastLoginTime";


    void importUsers() throws FrameworkException;

    void exportUsers() throws FrameworkException;

    /***********************************************************************************************
     * Delete all Users.
     *
     * @throws FrameworkException
     */

    void deleteAllUsers() throws FrameworkException;


    /***********************************************************************************************
     * Delete the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     */

    void deleteUser(UserPlugin user) throws FrameworkException;


    /***********************************************************************************************
     * Insert the specified User.
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

    UserPlugin insertUser(String username,
                          String password,
                          boolean active,
                          RolePlugin rolePlugin) throws FrameworkException;


    /***********************************************************************************************
     * Select the specified User.
     *
     * @param username
     * @param password
     *
     * @return UserData
     *
     * @throws FrameworkException
     */

    UserPlugin selectUser(String username,
                          String password) throws FrameworkException;


    /***********************************************************************************************
     * Update the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     */

    void updateUser(UserPlugin user) throws FrameworkException;


    /***********************************************************************************************
     * Update the LastLogin Date and Time for the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     */

    void updateLastLogin(UserPlugin user) throws FrameworkException;
    }
