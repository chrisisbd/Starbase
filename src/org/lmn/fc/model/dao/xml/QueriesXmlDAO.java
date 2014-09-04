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
import org.lmn.fc.model.dao.ResourcesDAOInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.xmlbeans.queries.QueriesDocument;
import org.lmn.fc.model.xmlbeans.queries.QueryResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The QueriesXmlDAO.
 */

public final class QueriesXmlDAO implements ResourcesDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();
    private static final String QUERIES_XML = "/queries.xml";

    private final AtomPlugin hostAtom;
    private final String strFolder;
    private final String strLanguage;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct a QueriesXmlDAO.
     * The methods are never used recursively, and so the folder may be set in the constructor.
     *
     * @param host
     * @param folder
     * @param language
     * @param debug
     */

    public QueriesXmlDAO(final AtomPlugin host,
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
     * Import the Queries from the specified folder.
     *
     * @throws FrameworkException
     */

    public final void importResources() throws FrameworkException
        {
        final File xmlFile;

        // Check the parameters...
        if ((getHost() == null)
            || (getFolder() == null)
            || (EMPTY_STRING.equals(getFolder()))
            || (getLanguage() == null)
            || (getLanguage().length() != 2))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing " + getHost().getName() + " Queries from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + QUERIES_XML);
        try
            {
            final QueriesDocument queriesDoc;
            final QueriesDocument.Queries queries;
            final List<QueryResource> list;
            final Iterator<QueryResource> iterList;

            queriesDoc = QueriesDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(queriesDoc))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            queries = queriesDoc.getQueries();
            list = queries.getQueryResourceList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final QueryResource queryXml;
                final ResourcePlugin plugin;

                queryXml = iterList.next();

                // Check that we know enough to import this Query
                if ((queryXml != null)
                    && (XmlBeansUtilities.isValidXml(queryXml)))
                    {
                    // Initialise the ResourcePlugin from the XML String configuration
                    plugin = (ResourcePlugin) BEAN_FACTORY_XML.createQuery(getHost(),
                                                                           queryXml,
                                                                           getLanguage());
                    if ((plugin != null)
                        && (plugin.isInstalled())
                        && (plugin.getName() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null)
                        && (getHost().getQueryExpander() != null))
                        {
                        // Create a unique ID from the host's ID and the plugin hashcode
                        plugin.setID(getHost().getID() + plugin.hashCode());

                        // Add this Query to the host Atom
                        getHost().addQuery(plugin);

                        // Add this Query to the Registry
                        //LOGGER.login("Registering Query " + plugin.getName());
                        REGISTRY.addQuery(plugin.getResourceKey(), plugin);

                        // Do some debugging as the import proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new FrameworkException(EXCEPTION_CREATE_QUERY + SPACE + queryXml.getDescription());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the import proceeds
            getHost().showAttachedQueries(getDebugMode());
            REGISTRY.showQueries(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importQueries() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }

    /***********************************************************************************************
     * Export the Queries from the specified Atom, as XML into the specified folder.
     *
     * @throws FrameworkException
     */

    public final void exportResources() throws FrameworkException
        {
        final File fileExport;
        final FileWriter writerOutput;

        if ((getHost() == null)
            || (getFolder() == null)
            || (EMPTY_STRING.equals(getFolder()))
            || (REGISTRY.getQueries() == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        fileExport = new File(InstallationFolder.getTerminatedUserDir()
                                   + getFolder()
                                   + QUERIES_XML);

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
            throw new FrameworkException(EXCEPTION_INVALID_ROOT_LEVEL, exception);
            }

        // Iterate over the Hashtable Queries,
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
