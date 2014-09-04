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
//  24-05-03    LMN created file
//  26-05-04    LMN added Installed flag
//  26-05-04    LMN added aboutCountries()
//  06-06-04    LMN added current Country
//  16-07-04    LMN added sorted iteration
//  18-09-04    LMN changed for full ISO/IOC data
//  04-10-04    LMN removed aboutCountries(), added getCountryReport()
//  29-03-06    LMN finally split data from DAO!
//  28-04-06    LMN implementing...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.locale.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.root.impl.RootData;
import org.lmn.fc.model.xmlbeans.countries.Country;

import java.awt.*;


/***************************************************************************************************
 * Encapsulate all Country information.
 * This class is not final because we might need to override toString()
 * for different CountryName displays in e.g. combo boxes.
 *
 * ToDo Handle National Grids...
 */

public class CountryData extends RootData
                         implements CountryPlugin
    {
    /***********************************************************************************************
     * Get a Country ResourceKey given an ISO2 code.
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

        return (PREFIX_COUNTRY + KEY_DELIMITER + isocode2.toUpperCase());
        }


    /***********************************************************************************************
     * Creates a new CountryData object.
     *
     * @param country
     */

    public CountryData(final Country country)
        {
        super(-6896977087636711111L);

        if ((country == null)
            || (!XmlBeansUtilities.isValidXml(country)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        setXml(country);

        // Make the Countries accessible on the navigation tree
        getHostTreeNode().setUserObject(this);
        }


    /**********************************************************************************************/
    /* CountryPlugin implementations                                                              */
    /***********************************************************************************************
     * Get the ISO2 Country Code.
     *
     * @return String The two-letter ISO-3166 CountryData code
     */

     public final String getISOCode2()
         {
         return (getXml().getISO2());
         }


    /***********************************************************************************************
     * Set the ISO2 Country Code.
     *
     * @param iso2
     */

    public final void setISOCode2(final String iso2)
        {
        getXml().setISO2(iso2.toUpperCase());
        updateRoot();
        }


    /***********************************************************************************************
     * Get the ISO3 Country Code.
     *
     * @return String
     */

    public final String getISOCode3()
        {
        return (getXml().getISO3());
        }


    /***********************************************************************************************
     * Set the ISO3 Country Code.
     *
     * @param iso3
     */

    public final void setISOCode3(final String iso3)
        {
        getXml().setISO3(iso3.toUpperCase());
        updateRoot();
        }


    /***********************************************************************************************
     * Get the IOC3 Country Code.
     *
     * @return String
     */

    public final String getIOCCode3()
        {
        return (getXml().getIOC3());
        }


    /***********************************************************************************************
     * Set the IOC3 Country Code.
     *
     * @param ioc3
     */

    public final void setIOCCode3(final String ioc3)
        {
        getXml().setIOC3(ioc3.toUpperCase());
        updateRoot();
        }


    /***********************************************************************************************
     * Return a flag indicating if the Country is installed.
     *
     * @return boolean
     */

    public final boolean isInstalled()
        {
        return (getXml().getInstalled());
        }


    /***********************************************************************************************
     * Set a flag indicating if the Country is installed.
     *
     * @param flag
     */

    public final void setInstalled(final boolean flag)
        {
        getXml().setInstalled(flag);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the classname of the code to handle the NationalGrid for this Country.
     *
     * @return String
     */

    public final String getNationalGridClassname()
        {
        return (getXml().getNationalGridClassname());
        }


    /***********************************************************************************************
     * Get the Internet Domain.
     *
     * @return String
     */

    public final String getInternetDomain()
        {
        return (getXml().getInternetDomain());
        }


    /***********************************************************************************************
     * Set the Internet Domain.
     *
     * @param domain
     */

    public final void setInternetDomain(final String domain)
        {
        getXml().setInternetDomain(domain);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the ISO Numeric Code.
     *
     * @return String
     */

    public final String getISONumeric()
        {
        return (getXml().getISONumeric());
        }


    /***********************************************************************************************
     * Set the ISO Numeric Code.
     *
     * @param numeric
     */

    public final void setISONumeric(final String numeric)
        {
        getXml().setISONumeric(numeric);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the ITU DiallingCode.
     *
     * @return String
     */

    public final String getITUDiallingCode()
        {
        return (getXml().getITUDiallingCode());
        }


    /***********************************************************************************************
     * Set the ITU DiallingCode.
     *
     * @param code
     */

    public final void setITUDiallingCode(final String code)
        {
        getXml().setITUDiallingCode(code);
        updateRoot();
        }


   /***********************************************************************************************
    * Get the UN VehicleCode.
    *
    * @return String
    */

    public final String getUNVehicleCode()
        {
        return (getXml().getUNVehicleCode());
        }


    /***********************************************************************************************
     * Set the UN VehicleCode.
     *
     * @param code
     */

    public final void setUNVehicleCode(final String code)
        {
        getXml().setUNVehicleCode(code);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the ISO Country Name.
     *
     * @return String The Country Name
     */

    public final String getISOCountryName()
        {
        return (getXml().getISOCountryName());
        }


    /***********************************************************************************************
     * Set the ISO Country Name.
     *
     * @param name
     */

    public final void setISOCountryName(final String name)
        {
        getXml().setISOCountryName(name);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the lower case form of the ISO Country Name.
     *
     * @return String
     */

    public final String getISOCountryNameLower()
        {
        return (getXml().getISOCountryNameLower());
        }


    /***********************************************************************************************
     * Set the lower case form of the ISO Country Name.
     *
     * @param name
     */

    public final void setISOCountryNameLower(final String name)
        {
        getXml().setISOCountryNameLower(name);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the IOC Country Name.
     *
     * @return String The Country Name
     */

    public final String getIOCCountryName()
        {
        return (getXml().getIOCCountryName());
        }


    /***********************************************************************************************
     * Set the IOC Country Name.
     *
     * @param name
     */

    public final void setIOCCountryName(final String name)
        {
        getXml().setIOCCountryName(name);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the lower case form of the IOC Country Name.
     *
     * @return String
     */

    public final String getIOCCountryNameLower()
        {
        return (getXml().getIOCCountryNameLower());
        }


    /***********************************************************************************************
     * Set the lower case form of the IOC Country Name.
     *
     * @param name
     */

    public final void setIOCCountryNameLower(final String name)
        {
        getXml().setIOCCountryNameLower(name);
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
        return (getXml().getISOCountryName());
        }


    /***********************************************************************************************
     * Set the Name.
     *
     * @param name
     */

    public final void setName(final String name)
        {
        getXml().setIOCCountryName(name);
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
     * Show the Country debug data.
     */

    public final void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.debug("Country");
            LOGGER.debug(INDENT + "[id=" + getID() + "]");
            LOGGER.debug(INDENT + "[resourcekey=" + getResourceKey() + "]");

            LOGGER.debug(INDENT + "[isocode2=" + getISOCode2() + "]");
            LOGGER.debug(INDENT + "[isocode3=" + getISOCode3() + "]");
            LOGGER.debug(INDENT + "[ioccode3=" + getIOCCode3() + "]");
            LOGGER.debug(INDENT + "[installed=" + isInstalled() + "]");
            LOGGER.debug(INDENT + "[internetdomain=" + getInternetDomain() + "]");
            LOGGER.debug(INDENT + "[isonumeric=" + getISONumeric() + "]");
            LOGGER.debug(INDENT + "[itudiallingcode=" + getITUDiallingCode() + "]");
            LOGGER.debug(INDENT + "[unvehiclecode=" + getUNVehicleCode() + "]");
            LOGGER.debug(INDENT + "[isocountryname=" + getISOCountryName() + "]");
            LOGGER.debug(INDENT + "[isocountrynamelower=" + getISOCountryNameLower() + "]");
            LOGGER.debug(INDENT + "[ioccountryname=" + getIOCCountryName() + "]");
            LOGGER.debug(INDENT + "[ioccountrynamelower=" + getIOCCountryNameLower() + "]");
            }
        }


    /***********************************************************************************************
     * Get the XML part of the Country.
     *
     * @return XmlObject
     */

     public final Country getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((Country)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
