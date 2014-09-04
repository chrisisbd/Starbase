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
import org.lmn.fc.model.dao.LookAndFeelsDAOInterface;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeelsDocument;

import javax.swing.*;
import java.io.File;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The LookAndFeelsXmlDAO.
 */

public final class LookAndFeelsXmlDAO implements LookAndFeelsDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the LookAndFeelsXmlDAO.
     *
     * @param folder
     * @param debug
     */

    public LookAndFeelsXmlDAO(final String folder,
                              final boolean debug)
        {
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the LookAndFeels.
     *
     * @throws FrameworkException
     */

    public final void importLookAndFeels() throws FrameworkException
        {
        final File xmlFile;

        if ((getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing LookAndFeels from [" + getFolder() + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                               + getFolder()
                               + LOOKANDFEELS_XML);
        try
            {
            final LookAndFeelsDocument docLookAndFeels;
            final LookAndFeelsDocument.LookAndFeels looks;
            final List<org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeel> list;
            final Iterator<org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeel> iterList;

            docLookAndFeels = LookAndFeelsDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docLookAndFeels))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            looks = docLookAndFeels.getLookAndFeels();
            list = looks.getLookAndFeelList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeel looksXml;
                final LookAndFeelPlugin plugin;
                final UIManager.LookAndFeelInfo[] lookAndFeelInfo;
                boolean boolFound;

                looksXml = iterList.next();

                // Check that we know enough to install this LookAndFeel
                if ((looksXml != null)
                    && (looksXml.getName() != null)
                    && (!EMPTY_STRING.equals(looksXml.getName()))
                    && (looksXml.getClassName() != null)
                    && (!EMPTY_STRING.equals(looksXml.getClassName()))
                    && (looksXml.getInstalled()))
                    {
                    // Initialise the LookAndFeelPlugin from the XML LookAndFeels configuration
                    plugin = BEAN_FACTORY_XML.createLookAndFeel(looksXml);

                    if ((plugin != null)
                        && (plugin.getClassName() != null)
                        && (plugin.getResourceKey() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null))
                        {
                        LOGGER.login("Registering LookAndFeel " + plugin.getResourceKey());

                        // Add this LookAndFeelPlugin to the Registry
                        REGISTRY.addLookAndFeel(plugin.getResourceKey(), plugin);

                        // Look to see if this LookAndFeel is already installed
                        // This is tedious, but there is no contains() method!
                        lookAndFeelInfo = UIManager.getInstalledLookAndFeels();
                        boolFound = false;

                        for (int i = 0;
                             ((!boolFound) && (i < lookAndFeelInfo.length));
                             i++)
                            {
                            if (lookAndFeelInfo[i].getClassName().equals(plugin.getClassName()))
                                {
                                boolFound = true;
                                }
                            }

                        // Install this LookAndFeel if possible
                        if (!boolFound)
                            {
                            UIManager.installLookAndFeel(plugin.getName(),
                                                         plugin.getClassName());
                            }

                        // Do some debugging as the installation proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new FrameworkException(EXCEPTION_CREATE_LOOKANDFEEL + SPACE + looksXml.getName());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the installation proceeds
            REGISTRY.showLookAndFeels(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importLookAndFeels() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************L************************************
     * Export the LookAndFeels.
     *
     * @param debug
     *
     * @throws FrameworkException
     */

    public final void exportLookAndFeels(final boolean debug) throws FrameworkException
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
