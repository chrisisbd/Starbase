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
import org.lmn.fc.model.dao.FrameworkDAOInterface;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.plugins.impl.AtomData;
import org.lmn.fc.model.plugins.impl.FrameworkData;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.xmlbeans.frameworks.Framework;
import org.lmn.fc.model.xmlbeans.frameworks.FrameworksDocument;

import java.io.File;


/***************************************************************************************************
 * The FrameworkXmlDAO.
 */

public final class FrameworkXmlDAO implements FrameworkDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the FrameworkXmlDAO.
     *
     * @param folder
     * @param debug
     */

    public FrameworkXmlDAO(final String folder,
                           final boolean debug)
        {
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Framework from the file <code>frameworks.xml</code> in the specified folder.
     *
     * @throws FrameworkException
     */

    public void importFramework() throws FrameworkException
        {
        final File xmlFile;
        final FrameworkPlugin plugin;

        if ((getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing Framework from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + FRAMEWORKS_XML);
        try
            {
            final FrameworksDocument docFrameworks;
            final FrameworksDocument.Frameworks frameworks;
            final Framework frameworkXml;

            docFrameworks = FrameworksDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docFrameworks))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            frameworks = docFrameworks.getFrameworks();
            frameworkXml = frameworks.getFramework();

            // Check that we know enough to import this Framework
            if ((frameworkXml != null)
                && (frameworkXml.getName() != null)
                && (!EMPTY_STRING.equals(frameworkXml.getName())))
                {
                // Now construct the user's Framework which was requested in the XML file
                plugin = BEAN_FACTORY_XML.createFramework(FrameworkData.class,
                                                          frameworkXml);
                if ((plugin != null)
                    && (plugin.getName() != null)
                    && (plugin.getHostTreeNode() != null)
                    && (plugin.getResourceKey() != null))
                    {
                    // Assign the UserRoles now that the Framework is completely specified
                    AtomData.assignRoles(plugin);

                    // Add this FrameworkPlugin to the Registry
                    LOGGER.login("Registering Framework " + plugin.getResourceKey());
                    REGISTRY.addAtom(plugin.getResourceKey(), plugin);

                    if ((REGISTRY.getVersionNumbers().containsKey(plugin.getName()))
                        && (REGISTRY.getBuildNumbers().containsKey(plugin.getName()))
                        && (REGISTRY.getBuildStatuses().containsKey(plugin.getName())))
                        {
                        plugin.setVersionNumber(REGISTRY.getVersionNumbers().get(plugin.getName()));
                        plugin.setBuildNumber(REGISTRY.getBuildNumbers().get(plugin.getName()));
                        plugin.setBuildStatus(REGISTRY.getBuildStatuses().get(plugin.getName()));

                        LOGGER.login("[plugin=" + plugin.getName()
                                     + "] [version.number=" + plugin.getVersionNumber()
                                     + "] [build.number=" + plugin.getBuildNumber()
                                     + "] [build.status=" + plugin.getBuildStatus() + "]");
                        }

                    // Special case for the Framework: Indicate that it is the Framework...
                    REGISTRY.setFrameworkResourceKey(plugin.getResourceKey());

                    // Start up the Framework MBean server
                    plugin.initialiseMBeanServer();

                    // Register the Framework with the MBean server, if possible
                    // We know that the plugin implements the <plugin>MBean interface
                    plugin.registerAtom(plugin);

                    // Do some debugging as the installation proceeds
                    plugin.setDebugMode(getDebugMode());
                    plugin.showDebugData();
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_CREATE_FRAMEWORK + SPACE + frameworkXml.getDescription());
                    }
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                }

            // Do some more debugging as the installation proceeds
            REGISTRY.showAtoms(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importFramework() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Export this Framework and its associated Tasks, Properties, Queries, Exceptions and Strings
     * to XML in the <code>exports</code> folder.
     *
     * @throws FrameworkException
     */

    public void exportFramework() throws FrameworkException
        {
        LOGGER.log("Framework export");

//        exportAtom(this, folder);
//        TaskData.exportTasks(this, folder);
        //PropertyData.exportProperties(this, folder);
//        QueryData.exportProperties(this, folder);
//        ExceptionData.exportExceptions(this, folder);
//        StringData.exportStrings(this, folder);
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
