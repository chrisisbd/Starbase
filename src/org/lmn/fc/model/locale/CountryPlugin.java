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

package org.lmn.fc.model.locale;

import org.lmn.fc.model.root.RootPlugin;


/***************************************************************************************************
 * The CountryPlugin.
 */

public interface CountryPlugin extends RootPlugin
    {
    // String Resources
    String COUNTRIES_ICON               = "countries.jpg";
    String COUNTRIES_EXPANDER_NAME      = "Countries";
    String COUNTRY_REPORT               = COUNTRIES_EXPANDER_NAME;
    String DEFAULT_COUNTRY              = "GB";
    String FOLDER_FLAGS_COUNTRIES      = "flags/countries";


    String getISOCode2();

    void setISOCode2(String iso2);

    String getISOCode3();

    void setISOCode3(String iso3);

    String getIOCCode3();

    void setIOCCode3(String ioc3);

    boolean isInstalled();

    void setInstalled(boolean flag);


    /***********************************************************************************************
     * Get the classname of the code to handle the NationalGrid for this Country.
     *
     * @return String
     */

    String getNationalGridClassname();

    
    String getInternetDomain();

    void setInternetDomain(String domain);

    String getISONumeric();

    void setISONumeric(String numeric);

    String getITUDiallingCode();

    void setITUDiallingCode(String code);

    String getUNVehicleCode();

    void setUNVehicleCode(String code);

    String getISOCountryName();

    void setISOCountryName(String name);

    String getISOCountryNameLower();

    void setISOCountryNameLower(String name);

    String getIOCCountryName();

    void setIOCCountryName(String name);

    String getIOCCountryNameLower();

    void setIOCCountryNameLower(String name);
    }
