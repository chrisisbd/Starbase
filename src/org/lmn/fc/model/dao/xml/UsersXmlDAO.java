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

package org.lmn.fc.model.dao.xml;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.dao.UsersDAOInterface;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.users.User;
import org.lmn.fc.model.xmlbeans.users.UsersDocument;

import java.io.File;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The UsersXmlDAO.
 */

public final class UsersXmlDAO implements UsersDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the UsersXmlDAO.
     *
     * @param folder
     * @param debug
     */

    public UsersXmlDAO(final String folder,
                       final boolean debug)
        {
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Users from the XML Document.
     *
     * @throws FrameworkException
     */

    public void importUsers() throws FrameworkException
        {
        final File xmlFile;

        if ((getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing Users from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + USERS_XML);
        try
            {
            final UsersDocument docUsers;
            final UsersDocument.Users users;
            final List<User> list;
            final Iterator<User> iterList;

            docUsers = UsersDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docUsers))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            users = docUsers.getUsers();
            list = users.getUserList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final User userXml;
                final UserPlugin plugin;

                userXml = iterList.next();

                // Check that we know enough to install this User
                if ((userXml != null)
                    && (userXml.getUserName() != null))
                    {
                    // Initialise the UserPlugin from the XML Document
                    plugin = BEAN_FACTORY_XML.createUser(userXml);

                    if ((plugin != null)
                        && (plugin.getResourceKey() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null))
                        {
                        LOGGER.login("Registering User " + plugin.getResourceKey());

                        // Add this UserPlugin to the Registry
                        REGISTRY.addUser(plugin.getResourceKey(), plugin);

                        // Do some debugging as the installation proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new FrameworkException(EXCEPTION_CREATE_USER + SPACE + userXml.getUserName());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the installation proceeds
            REGISTRY.showUsers(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importUsers() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Export the Users.
     *
     * @throws FrameworkException
     */

    public void exportUsers() throws FrameworkException
        {
        }


    /**********************************************************************************************
     * Delete all Users.
     *
     * @throws FrameworkException
     *
     */

    public void deleteAllUsers() throws FrameworkException
        {

        }


    /**********************************************************************************************
     * Delete the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     *
     */

    public void deleteUser(final UserPlugin user) throws FrameworkException
        {

        }


    /**********************************************************************************************
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

    public UserPlugin insertUser(final String username,
                                 final String password,
                                 final boolean active,
                                 final RolePlugin rolePlugin) throws FrameworkException
        {
        return null;
        }


    /**********************************************************************************************
     * Select the specified User.
     *
     * @param username
     * @param password
     *
     * @return UserData
     *
     * @throws FrameworkException
     *
     */

    public UserPlugin selectUser(final String username,
                                 final String password) throws FrameworkException
        {
        return null;
        }


    /**********************************************************************************************
     * Update the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     *
     */

    public void updateUser(final UserPlugin user) throws FrameworkException
        {

        }


    /**********************************************************************************************
     * Update the LastLogin Date and Time for the specified User.
     *
     * @param user
     *
     * @throws FrameworkException
     *
     */

    public void updateLastLogin(final UserPlugin user) throws FrameworkException
        {

        }


    /***********************************************************************************************
     * Get the name of the folder containing the XML file.
     *
     * @return String
     */

    private String getFolder()
        {
        return (this.strFolder);
        }


    /***********************************************************************************************
     * Get the debug mode.
     *
     * @return boolean
     */

    private boolean getDebugMode()
        {
        return (this.boolDebugMode);
        }
    }
