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
import org.lmn.fc.model.dao.CountriesDAOInterface;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.xmlbeans.countries.CountriesDocument;
import org.lmn.fc.model.xmlbeans.countries.Country;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The CountriesXmlDAO.
 */

public final class CountriesXmlDAO implements CountriesDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the CountriesXmlDAO.
     *
     * @param folder
     * @param debug
     */

    public CountriesXmlDAO( final String folder,
                            final boolean debug)
        {
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Countries from the XML Document.
     *
     * @throws FrameworkException
     */

    public void importCountries() throws FrameworkException
        {
        final File xmlFile;

        if ((getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing Countries from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + COUNTRIES_XML);

        try
            {
            final CountriesDocument docCountries;
            final CountriesDocument.Countries countries;
            final List<Country> list;
            final Iterator<Country> iterList;

            docCountries = CountriesDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docCountries))
                {
                throw new IOException(EXCEPTION_XML_VALIDATION);
                }

            countries = docCountries.getCountries();
            list = countries.getCountryList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final Country countryXml;
                final CountryPlugin plugin;

                countryXml = iterList.next();

                // Check that we know enough to install this Country
                if ((countryXml != null)
                    && (countryXml.getISOCountryName() != null)
                    && (!EMPTY_STRING.equals(countryXml.getISOCountryName())))
                    {
                    // Initialise the CountryPlugin from the XML Document
                    plugin = BEAN_FACTORY_XML.createCountry(countryXml);

                    if ((plugin != null)
                        && (plugin.getISOCode2() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null))
                        {
                        //LOGGER.login("Registering Country " + plugin.getResourceKey());

                        // Add this CountryPlugin to the Registry
                        REGISTRY.addCountry(plugin.getResourceKey(), plugin);

                        // Do some debugging as the installation proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new IOException(EXCEPTION_CREATE_COUNTRY + SPACE + countryXml.getISOCountryName());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the installation proceeds
            REGISTRY.showCountries(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importCountries() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Export the Countries to the XML Document.
     *
     * @throws FrameworkException
     */

    public void exportCountries() throws FrameworkException
        {
        // ToDo exportCountries
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
