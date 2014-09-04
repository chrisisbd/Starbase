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
import org.lmn.fc.model.dao.RolesDAOInterface;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.xmlbeans.roles.Role;
import org.lmn.fc.model.xmlbeans.roles.RolesDocument;

import java.io.File;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The RolesXmlDAO.
 */

public final class RolesXmlDAO implements RolesDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the RolesXmlDAO.
     *
     * @param folder
     * @param debug
     */

    public RolesXmlDAO(final String folder,
                       final boolean debug)
        {
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Roles from the XML Document.
     *
     * @throws FrameworkException
     */

    public void importRoles() throws FrameworkException
        {
        final File xmlFile;

        if ((getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing Roles from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + ROLES_XML);
        try
            {
            final RolesDocument docRoles;
            final RolesDocument.Roles roles;
            final List<Role> list;
            final Iterator<Role> iterList;

            docRoles = RolesDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docRoles))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            roles = docRoles.getRoles();
            list = roles.getRoleList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final Role roleXml;
                final RolePlugin plugin;

                roleXml = iterList.next();

                // Check that we know enough to install this Role
                if ((roleXml != null)
                    && (roleXml.getRoleName() != null))
                    {
                    // Initialise the RolePlugin from the XML Document
                    plugin = BEAN_FACTORY_XML.createRole(roleXml);

                    if ((plugin != null)
                        && (plugin.getResourceKey() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null))
                        {
                        LOGGER.login("Registering Role " + plugin.getResourceKey());

                        // Add this RolePlugin to the Registry
                        REGISTRY.addRole(plugin.getResourceKey(), plugin);

                        // Do some debugging as the installation proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new FrameworkException(EXCEPTION_CREATE_ROLE + SPACE + roleXml.getRoleName());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the installation proceeds
            REGISTRY.showRoles(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importRoles() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Export the Roles.
     *
     * @throws FrameworkException
     */

    public void exportRoles() throws FrameworkException
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
