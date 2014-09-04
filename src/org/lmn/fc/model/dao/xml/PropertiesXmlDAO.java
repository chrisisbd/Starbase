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

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.dao.ResourcesDAOInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.xmlbeans.properties.PropertiesDocument;
import org.lmn.fc.model.xmlbeans.properties.PropertyResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The PropertiesXmlDAO.
 */

public final class PropertiesXmlDAO implements ResourcesDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();
    private static final String FILENAME_PROPERTIES_COMMON = "properties.xml";
    private static final String FILENAME_PROPERTIES_SUFFIX = "-properties.xml";

    private final AtomPlugin hostAtom;
    private final String strFolder;
    private final String strLanguage;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct a PropertiesXmlDAO.
     * The methods are never used recursively, and so the folder may be set in the constructor.
     *
     * @param host
     * @param folder
     * @param language
     * @param debug
     */

    public PropertiesXmlDAO(final AtomPlugin host,
                            final String folder,
                            final String language,
                            final boolean debug)
        {
        this.hostAtom = host;
        this.strFolder = folder;
        this.strLanguage = language;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Properties from the specified folder and attach them to the specified host Atom.
     * Properties may exist in a 'common' file properties.xml,
     * or grouped by (e.g.) ResourceKey in separate files, e.g. properties-XXX.xml.
     *
     * @throws FrameworkException
     */

    public void importResources() throws FrameworkException
        {
        // Check the parameters...
        if ((getHost() == null)
            || (getFolder() == null)
            || (FrameworkStrings.EMPTY_STRING.equals(getFolder()))
            || (getLanguage() == null)
            || (FrameworkStrings.EMPTY_STRING.equals(getLanguage()))
            || (getLanguage().length() != 2))
            {
            throw new FrameworkException(FrameworkStrings.EXCEPTION_PARAMETER_NULL);
            }

        try
            {
            final String strFolder;
            final File xmlFile;
            final PropertiesDocument docProperties;

            LOGGER.login("Importing " + getHost().getName() + " Properties from [" + getFolder() + "]");

            // First try to get the 'common' Properties
            strFolder = InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + System.getProperty("file.separator")
                               + FILENAME_PROPERTIES_COMMON;
            xmlFile = new File(strFolder);

            docProperties = PropertiesDocument.Factory.parse(xmlFile);

            // This document will still be valid if it does not contain any PropertyResources,
            // but it must contain one Properties element
            if (XmlBeansUtilities.isValidXml(docProperties))
                {
                final String strImportFolder;
                final File dir;
                final PropertiesDocument.Properties properties;
                final List<PropertyResource> listCombinedResources;
                final Iterator<PropertyResource> iterCombinedResources;

                // Now read the individual Properties files 'properties-XXX.xml'
                strImportFolder = InstallationFolder.getTerminatedUserDir()
                                        + getFolder();

                dir = new File(strImportFolder);

                if (dir != null)
                    {
                    final File [] files;

                    // If this abstract pathname does not denote a directory,
                    // then this method returns null.
                    files = dir.listFiles();

                    if (files != null)
                        {
                        for (final File file : files)
                            {
                            // Read all files with names 'properties-XXX.xml' in the imports folder
                            if ((file != null)
                                && (file.isFile())
                                && (file.getName().endsWith(FILENAME_PROPERTIES_SUFFIX)))
                                {
                                final PropertiesDocument docGroupedProperties;

                                docGroupedProperties = PropertiesDocument.Factory.parse(file);

                                // This document will still be valid if it does not contain any PropertyResources,
                                // but it must contain one Properties element
                                if (XmlBeansUtilities.isValidXml(docGroupedProperties))
                                    {
                                    final List<PropertyResource> listGroupedProperties;

                                    listGroupedProperties = docGroupedProperties.getProperties().getPropertyResourceList();

                                    if ((listGroupedProperties != null)
                                        && (listGroupedProperties.size() >= 1))
                                        {
                                        //System.out.println("ADDING " + file.getName() + "listCombinedResources size=" + listGroupedProperties.size());
                                        docProperties.getProperties().getPropertyResourceList().addAll(listGroupedProperties);
                                        }
                                    }
                                }
                            }
                        }
                    }

                // Now create all of the combined PropertyResources, if any
                properties = docProperties.getProperties();
                listCombinedResources = properties.getPropertyResourceList();
                iterCombinedResources = listCombinedResources.iterator();

                while (iterCombinedResources.hasNext())
                    {
                    final PropertyResource propertyXml;
                    final ResourcePlugin plugin;

                    propertyXml = iterCombinedResources.next();

                    // Check that we know enough to import this Property
                    if ((propertyXml != null)
                        && (XmlBeansUtilities.isValidXml(propertyXml)))
                        {
                        // Initialise the ResourcePlugin from the XML Property configuration
                        plugin = (ResourcePlugin) BEAN_FACTORY_XML.createProperty(getHost(),
                                                                                  propertyXml,
                                                                                  getLanguage());
                        if ((plugin != null)
                            && (plugin.isInstalled())
                            && (plugin.getName() != null)
                            && (plugin.getHostTreeNode() != null)
                            && (plugin.getResourceKey() != null)
                            && (getHost().getPropertyExpander() != null))
                            {
                            // Create a unique ID from the host's ID and the plugin hashcode
                            plugin.setID(getHost().getID() + plugin.hashCode());

                            // Add this Property to the host Atom
                            getHost().addProperty(plugin);

                            if (!REGISTRY.getProperties().containsKey(plugin.getResourceKey()))
                                {
                                // Add this Property to the Registry
                                LOGGER.login("Registering Property " + plugin.getName());
                                REGISTRY.addProperty(plugin.getResourceKey(), plugin);
                                }

                            // Do some debugging as the import proceeds
                            plugin.setDebugMode(getDebugMode());
                            plugin.showDebugData();
                            }
                        else
                            {
                            throw new FrameworkException(EXCEPTION_CREATE_PROPERTY + SPACE + propertyXml.getDescription());
                            }
                        }
                    else
                        {
                        throw new FrameworkException(FrameworkStrings.EXCEPTION_PARAMETER_INVALID);
                        }
                    }

                // Do some more debugging as the import proceeds
                getHost().showAttachedProperties(getDebugMode());
                REGISTRY.showProperties(getDebugMode());
                }
            else
                {
                LOGGER.error("PropertiesXmlDAO.importResources() " + EXCEPTION_XML_VALIDATION);
                }
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Xml Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("PropertiesXmlDAO.importProperties() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }

    /***********************************************************************************************
     * Export the Properties from the specified Atom, as XML into the specified folder.
     *
     * @throws FrameworkException
     */

    public void exportResources() throws FrameworkException
        {
        final File fileExport;
        final FileWriter writerOutput;

        if ((getHost() == null)
            || (getFolder() == null)
            || (FrameworkStrings.EMPTY_STRING.equals(getFolder()))
            || (REGISTRY.getProperties() == null))
            {
            throw new FrameworkException(FrameworkStrings.EXCEPTION_PARAMETER_NULL);
            }

        fileExport = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + System.getProperty("file.separator")
                               + FILENAME_PROPERTIES_COMMON);

        // Overwrite existing output file or create a new file
        try
            {
            if (fileExport.exists())
                {
                fileExport.delete();
                fileExport.createNewFile();
                }
            else
                {
                fileExport.createNewFile();
                }
            }

        catch (IOException exception)
            {
            throw new FrameworkException(EXCEPTION_FILESYSTEM, exception);
            }

        // Iterate over the Hashtable Properties,
        // and write out those fields required in the XML
        }


    /***********************************************************************************************
     * Get the host Atom.
     *
     * @return AtomPlugin
     */

    private AtomPlugin getHost()
        {
        return (this.hostAtom);
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
     * Get the Language of the Resources.
     *
     * @return String
     */

    private String getLanguage()
        {
        return (this.strLanguage);
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
