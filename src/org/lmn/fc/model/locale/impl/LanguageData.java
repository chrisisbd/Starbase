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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  31-08-03    LMN created file
//  01-09-03    LMN changed key to ISO Code
//  02-09-03    LMN added iterateLanguageNames()
//  17-09-03    LMN integrating with RegistryModel
//  30-09-03    LMN renamed LanguageLibrary to LanguageData, tidied up..
//  24-05-04    LMN moved in getCurrentLanguageFlag()
//  26-05-04    LMN added aboutLanguages()
//  26-05-04    LMN converted to instances of LanguageData
//  16-07-04    LMN added sorted iteration
//  29-03-06    LMN finally split data from DAO!
//  29-04-06    LMN implementing...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.locale.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.root.impl.RootData;
import org.lmn.fc.model.xmlbeans.languages.Language;

import java.awt.*;


/***************************************************************************************************
 * Encapsulate all data for a Language.
 * This class is not final because we might need to override toString()
 * for different LanguageName displays in e.g. combo boxes.
 *
 * Language Codes are defined in ISO-639
 * <link>http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt</link>
 */

public class LanguageData extends RootData
                          implements LanguagePlugin
    {
    /***********************************************************************************************
     * Get a Language ResourceKey given an ISO2 code.
     *
     * @param isocode2
     *
     * @return String
     */

    public static String getResourceKeyFromCode(final String isocode2)
        {
        if ((isocode2 == null)
            || (isocode2.length() != 2))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        return (PREFIX_LANGUAGE + KEY_DELIMITER + isocode2.toLowerCase());
        }


    /***********************************************************************************************
     * Construct a LanguageData from the specified XML Object.
     *
     * @param language
     */

    public LanguageData(final Language language)
        {
        super(-8442018637307997291L);

        if ((language == null)
            || (!XmlBeansUtilities.isValidXml(language)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        setXml(language);

        // Make the Languages accessible on the navigation tree
        getHostTreeNode().setUserObject(this);
        }


    /**********************************************************************************************/
    /* LanguagePlugin implementations                                                             */
    /***********************************************************************************************
     * Get the Language ISO Code.
     *
     * @return String The two-letter ISO-639 Language code
     */

    public final String getISOCode2()
        {
        return (getXml().getISOCode());
        }


    /***********************************************************************************************
     * Set the Language ISO Code.
     *
     * @param code The two-letter ISO-639 Language code
     */

    public final void setISOCode2(final String code)
        {
        getXml().setISOCode(code.toLowerCase());
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Installed flag.
     *
     * @return boolean
     */

    public final boolean isInstalled()
        {
        return (getXml().getInstalled());
        }


    /***********************************************************************************************
     * Set the Installed flag.
     *
     * @param installed
     */

    public final void setInstalled(final boolean installed)
        {
        getXml().setInstalled(installed);
        updateRoot();
        }


    /**********************************************************************************************/
    /* RootPlugin implementations                                                                 */
    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (getResourceKeyFromCode(getISOCode2()));
        }


    /***********************************************************************************************
     * Get the full pathname for the Resource.
     *
     * @return String
     */

    public final String getPathname()
        {
        return (getResourceKey());
        }


    /***********************************************************************************************
     * Get the Name.
     *
     * @return String
     */

    public final String getName()
        {
        return (getXml().getName());
        }


    /***********************************************************************************************
     * Set the Name.
     *
     * @param name
     */

    public final void setName(final String name)
        {
        getXml().setName(name);
        updateRoot();
        }


    /***********************************************************************************************
     * Get a flag indicating if this Resource is active.
     *
     * @return boolean
     */

    public final boolean isActive()
        {
        return (true);
        }


    /***********************************************************************************************
     * Set a flag indicating if this Resource is active.
     *
     * @param flag
     */

    public final void setActive(final boolean flag)
        {
        }


    /***********************************************************************************************
     * Get the sort order for displayed lists of Resources.
     *
     * @return short
     */

    public final short getSortOrder()
        {
        return (0);
        }


    /***********************************************************************************************
     * Set the sort order for displayed lists of Resources.
     *
     * @param sortorder
     */

    public final void setSortOrder(final short sortorder)
        {
        }


    /***********************************************************************************************
     * Get a flag indicating if the Resource is Editable.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return (false);
        }


    /***********************************************************************************************
     * Set a flag indicating if the Resource is Editable.
     *
     * @param flag
     */

    public final void setEditable(final boolean flag)
        {
        }


    /***********************************************************************************************
     * Get the class name of the ResourceEditor.
     *
     * @return String
     */

    public final String getEditorClassname()
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Set the class name of the ResourceEditor.
     *
     * @param classname
     */

    public final void setEditorClassname(final String classname)
        {
        }


    /***********************************************************************************************
     * Get the Resource Description.
     *
     * @return String
     */

    public final String getDescription()
        {
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Set the Resource Description.
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        }


    /***********************************************************************************************
     * Get the IconFilename, constructed from the ISO2 code.
     *
     * @return String
     */

    public final String getIconFilename()
        {
        return (getISOCode2().toLowerCase() + "." + FileUtilities.gif);
        }


    /***********************************************************************************************
     * Set the IconFilename - this may not be changed.
     *
     * @param filename
     */

    public final void setIconFilename(final String filename)
        {
        }


    /**********************************************************************************************/
    /* User Interface and Debugging                                                               */
    /***********************************************************************************************
     * Override toString() to provide the User name which appears on the navigation tree.
     *
     * @return String
     */

    public String toString()
        {
        return(getName());
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     * setUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public final void runUI()
        {
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     * clearUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public final void stopUI()
        {
        }


    /***********************************************************************************************
     * The action to be performed when the tree node containing this Resource is selected.
     *
     * @param event
     * @param mode
     */

    public final void actionPerformed(final AWTEvent event,
                                      final boolean mode)
        {
        }


    /***********************************************************************************************
     * Show the Language debug data.
     */

    public final void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.debug("Language");
            LOGGER.debug(INDENT + "[isocode=" + getISOCode2() + "]");
            LOGGER.debug(INDENT + "[installed=" + isInstalled() + "]");
            LOGGER.debug(INDENT + "[name=" + getName() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the Language.
     *
     * @return XmlObject
     */

     public final Language getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((Language)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
