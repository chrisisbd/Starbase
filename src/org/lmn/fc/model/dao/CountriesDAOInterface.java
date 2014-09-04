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

package org.lmn.fc.model.dao;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;


/***************************************************************************************************
 */

public interface CountriesDAOInterface extends FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
    ResourceKeys
    {
    String COUNTRIES_XML = "/countries.xml";

    Logger LOGGER = Logger.getInstance();
    RegistryPlugin REGISTRY = Registry.getInstance();

    String EXCEPTION_ISO2_LENGTH       = "ISOCountryCode2 must be two characters in length";
    String EXCEPTION_ISO3_LENGTH       = "ISOCountryCode3 must be three characters in length";
    String EXCEPTION_ISO_COUNTRY_NAME  = "ISOCountryName is invalid";

    // Countries Table Columns
    String ISO_COUNTRY_CODE_2           = "ISO2";
    String ISO_COUNTRY_CODE_3           = "ISO3";
    String IOC_COUNTRY_CODE_3           = "IOC3";
    String COUNTRY_INSTALLED            = "CountryInstalled";
    String INTERNET_DOMAIN              = "InternetDomain";
    String ISO_NUMERIC                  = "ISONumeric";
    String ITU_DIALLING_CODE            = "ITUDiallingCode";
    String UN_VEHICLE_CODE              = "UNVehicleCode";
    String ISO_COUNTRY_NAME             = "ISOCountryName";
    String ISO_COUNTRY_NAME_LOWER       = "ISOCountryNameLower";
    String IOC_COUNTRY_NAME             = "IOCCountryName";
    String IOC_COUNTRY_NAME_LOWER       = "IOCCountryNameLower";

    int FIELDSIZE_ISO_COUNTRY_NAME = 100;
    int FIELDSIZE_ISO_CODE_2 = 2;
    int FIELDSIZE_ISO_CODE_3 = 3;
    int FIELDSIZE_IOC_CODE_3 = 3;

    // Queries
    String SELECT_COUNTRY_LIST_ALL      = "Select.Country.List.All";

    void importCountries() throws FrameworkException;

    void exportCountries() throws FrameworkException;
    }
