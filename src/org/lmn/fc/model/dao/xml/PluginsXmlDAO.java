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
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.dao.PluginsDAOInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.AtomData;
import org.lmn.fc.model.plugins.impl.PluginData;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.registry.impl.RegistryManager;
import org.lmn.fc.model.xmlbeans.plugins.Plugin;
import org.lmn.fc.model.xmlbeans.plugins.PluginsDocument;

import java.io.File;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The PluginsXmlDAO.
 */

public final class PluginsXmlDAO implements PluginsDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();
    private static final RegistryManager REGISTRY_MANAGER = RegistryManager.getInstance();

    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the PluginsXmlDAO.
     *
     * @param debug
     */

    public PluginsXmlDAO(final boolean debug)
        {
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Plugin from the file <code>plugins.xml</code> in the specified folder.
     * Recursively add all child plugins.
     *
     * @param host
     * @param folder
     * @param language
     *
     * @throws FrameworkException
     */

    public void importPlugins(final AtomPlugin host,
                              final String folder,
                              final String language) throws FrameworkException
        {
        final String SOURCE = "PluginsXmlDAO.importPlugins() ";
        final File xmlFile;

        if ((host == null)
            || (!host.validatePlugin())
            || (folder == null)
            || (EMPTY_STRING.equals(folder))
            || (language == null)
            || (language.length() != 2))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.login("Importing "
                        + host
                        + " Plugins from ["
                        + folder
                        + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + folder
                               + PLUGINS_XML);
        try
            {
            final PluginsDocument docPlugins;
            final PluginsDocument.Plugins plugins;
            final List<Plugin> listPlugins;
            final Iterator<Plugin> iterPlugins;

            docPlugins = PluginsDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docPlugins))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            plugins = docPlugins.getPlugins();
            listPlugins = plugins.getPluginList();
            iterPlugins = listPlugins.iterator();

            while (iterPlugins.hasNext())
                {
                final Plugin pluginXml;

                pluginXml = iterPlugins.next();

                if (pluginXml != null)
                    {
                    final AtomPlugin plugin;

                    // Now construct the user's Plugin which was requested in the XML file
                    // Returns NULL if the class could not be found in a Jar
                    plugin = BEAN_FACTORY_XML.createPlugin(host,
                                                           PluginData.class,
                                                           pluginXml);
                    if (plugin != null)
                        {
                        // Did we get enough to make a Plugin work?
                        if ((plugin.getName() != null)
                            && (plugin.getHostTreeNode() != null)
                            && (plugin.getResourceKey() != null)
                            && (plugin.isClassFound()))
                            {
                            // Set the parent Atom of this Plugin
                            // (this should have happened in the BeanFactory)
                            plugin.setParentAtom(host);

                            // Attach this Plugin to the host Atom
                            host.addAtom(plugin);

                            // Assign the UserRoles now that the Plugin is completely specified
                            AtomData.assignRoles(plugin);

                            // Add this AtomPlugin to the Registry
                            // NOTE! This requires that the parent has been set beforehand!
                            LOGGER.login("Registering Plugin " + plugin.getResourceKey());
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

                            // Register the plugin with the Framework MBean server, possible
                            // We know that the plugin implements the <plugin>MBean interface
                            REGISTRY.getFramework().registerAtom(plugin);

                            // Do some debugging as the installation proceeds
                            plugin.setDebugMode(getDebugMode());
                            plugin.showDebugData();

                            //--------------------------------------------------------------------------
                            // Now recursively add all child plugins if we can...
                            // We have already done Level 0 in the Framework
                            // The Level of the *last* plugin in the chain is the MaxLevel-1

                            if ((REGISTRY.getMaxRecursionDepth() > 1)
                                && (REGISTRY.getLevelID(plugin) < REGISTRY.getMaxRecursionDepth()))
                                {
                                AtomPlugin iPlugin;
                                final StringBuffer buffer;

                                // Save the current plugin
                                iPlugin = plugin;
                                buffer = new StringBuffer();

                                // Move up the chain, assembling the path as we go
                                // This will terminate just before the root - we hope!
                                while ((iPlugin != null)
                                    && (iPlugin.getParentAtom() != null)
                                    && (iPlugin.validatePlugin())
                                    && (REGISTRY.getLevelID(iPlugin) > 0)
                                    && (REGISTRY.getLevelID(iPlugin) == REGISTRY.getLevelID((AtomPlugin)iPlugin.getParentAtom()) + 1))
                                    {
                                    // Make "plugins/name/"
                                    buffer.insert(0, RegistryModelPlugin.DELIMITER_PATH);
                                    buffer.insert(0, iPlugin.getName().toLowerCase());
                                    buffer.insert(0, RegistryModelPlugin.DELIMITER_PATH);
                                    buffer.insert(0, InstallationFolder.PLUGINS.getName());

                                    // Move up the chain until we hit the root
                                    iPlugin = (AtomPlugin)iPlugin.getParentAtom();
                                    }

                                // If we added anything, we know all the previous conditions must be true
                                // so we should *try* to recurse one level further down for another plugin,
                                // but we can be sure that there *will* be tasks and resources at this level anyway
                                if (buffer.length() > 0)
                                    {
                                    // Finally add the imports folder for the next level of plugin
                                    // (which may or may not exist, we don't know yet)
                                    buffer.append(InstallationFolder.IMPORTS.getName());

                                    // Add the Tasks for the current Plugin; these must exist
                                    REGISTRY_MANAGER.importTasks(plugin,
                                                                 DataStore.XML,
                                                                 buffer.toString(),
                                                                 getDebugMode());

                                    // Add the Resources for the current Plugin; these must exist
                                    REGISTRY_MANAGER.importResources(plugin,
                                                                     DataStore.XML,
                                                                     buffer.toString(),
                                                                     language,
                                                                     getDebugMode());

                                    // Recurse using the current plugin as the new host...
                                    // ...and look in the calculated new imports folder
                                    // e.g. "/plugins/name0/plugins/name1/imports" and so on...
                                    importPlugins(plugin, buffer.toString(), language);
                                    }
                                else
                                    {
                                    LOGGER.debug(SOURCE + "Recursion stopped at " + iPlugin.getName());
                                    }
                                }
                            else
                                {
                                LOGGER.debug(SOURCE + "Recursion stopped at " + plugin.getName());
                                }
                            }
                        else
                            {
                            // The plugin was NOT NULL but it still failed, so what happened?
                            // This should never occur
                            throw new FrameworkException(EXCEPTION_CREATE_PLUGIN + SPACE + pluginXml.getDescription());
                            }
                        }
                    else
                        {
                        // The plugin was NULL, so it most definitely failed
                        // Do nothing, since the plugin may not be selected in the installer
                        LOGGER.login(SOURCE + "Skipping Plugin found in XML for which no class is loaded [name=" + pluginXml.getName() + "]");
                        //throw new FrameworkException(EXCEPTION_CREATE_PLUGIN + SPACE + pluginXml.getDescription());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }

                // Do some more debugging as the installation proceeds
                REGISTRY.showAtoms(getDebugMode());
                }
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded on the classpath!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login(SOURCE + "Generic Exception [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Export this Plugin and its associated Tasks, Properties, Queries, Exceptions and Strings
     * to XML in the <code>exports</code> folder.
     *
     * @throws FrameworkException
     */

    public void exportPlugins() throws FrameworkException
        {
        LOGGER.log("Plugin export");

        // ToDo exportPlugins()

//        exportAtom(this, folder);
//        TaskData.exportTasks(this, folder);
        //PropertyData.exportProperties(this, folder);
//        QueryData.exportProperties(this, folder);
//        ExceptionData.exportExceptions(this, folder);
//        StringData.exportStrings(this, folder);
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
