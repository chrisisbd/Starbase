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
import org.lmn.fc.model.dao.LanguagesDAOInterface;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.xmlbeans.languages.Language;
import org.lmn.fc.model.xmlbeans.languages.LanguagesDocument;

import java.io.File;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The LanguagesXmlDAO.
 */

public final class LanguagesXmlDAO implements LanguagesDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct a LanguagesXmlDAO.
     *
     * @param folder
     * @param debug
     */

    public LanguagesXmlDAO(final String folder,
                           final boolean debug)
        {
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Languages from the XML document.
     *
     * @throws FrameworkException
     */

    public final void importLanguages() throws FrameworkException
        {
        final File xmlFile;

        if ((getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing Languages from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + LANGUAGES_XML);
        try
            {
            final LanguagesDocument docLanguages;
            final LanguagesDocument.Languages languages;
            final List<Language> list;
            final Iterator<Language> iterList;

            docLanguages = LanguagesDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docLanguages))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            languages = docLanguages.getLanguages();
            list = languages.getLanguageList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final Language languageXml;
                final LanguagePlugin plugin;

                languageXml = iterList.next();

                // Check that we know enough to install this Language
                if ((languageXml != null)
                    && (languageXml.getName() != null)
                    && (!EMPTY_STRING.equals(languageXml.getName())))
                    {
                    // Initialise the LanguagePlugin from the XML Language configuration
                    plugin = BEAN_FACTORY_XML.createLanguage(languageXml);

                    if ((plugin != null)
                        && (plugin.getName() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null))
                        {
                        //LOGGER.login("Registering Language " + plugin.getResourceKey());

                        // Add this LanguagePlugin to the Registry
                        REGISTRY.addLanguage(plugin.getResourceKey(), plugin);

                        // Do some debugging as the installation proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new FrameworkException(EXCEPTION_CREATE_LANGUAGE + SPACE + languageXml.getName());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the installation proceeds
            REGISTRY.showLanguages(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importLanguages() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     *
     * @throws FrameworkException
     */

    public final void exportLanguages() throws FrameworkException
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
