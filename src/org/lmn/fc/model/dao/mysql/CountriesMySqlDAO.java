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

package org.lmn.fc.model.dao.mysql;

import javax.swing.tree.DefaultMutableTreeNode;


/***************************************************************************************************
 */

public class CountriesMySqlDAO
    {
    /***********************************************************************************************
     * Load all installed Countries from the specified FrameworkDatabase.
     * The Country code keys are the upper-case, two-letter codes as defined by ISO-3166.
     *
     * @param database
     * @param code for the default Country selection
     * @param debugmode
     *
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadCountries(org.lmn.fc.database.impl.FrameworkDatabase database,
                                                       String code,
                                                       boolean debugmode)
        {
//        DefaultMutableTreeNode nodeCountries;
//        AtomPlugin expanderCountries;
//        PreparedStatement psCountries;
//        ResultSet rsCountries;
//        CountryData countryData;
//        String strISOCode2;
//        boolean boolInstalled;
//        QueryPlugin queryPlugin;
//
//        CountryData.hashtableCountries = new Hashtable<String, CountryData>();
//
//        nodeCountries = new DefaultMutableTreeNode();
//        expanderCountries = new NullData("path",
//                                                                      CountryPlugin.COUNTRIES_EXPANDER_NAME,
//                                                                      CountryPlugin.COUNTRIES_ICON,
//                                                                      nodeCountries);
//        nodeCountries.setUserObject(expanderCountries);
//
//        try
//            {
//            if ((database != null)
//                && (database.getConnection() != null)
//                && (!database.getConnection().isClosed()))
//                {
//                // Read the Countries from the FrameworkDatabase
//                queryPlugin = FrameworkSingletons.REGISTRY.getQueryData(CountriesDAOInterface.SELECT_COUNTRY_LIST_ALL);
//                psCountries = queryPlugin.getPreparedStatement(DataStore.MYSQL);
//
//                rsCountries = queryPlugin.executeQuery(CountryData.class,
//                                                       psCountries,
//                                                       RegistryModel.getInstance().getSqlTrace(),
//                                                       RegistryModel.getInstance().getSqlTiming());
//
//                // Now loop through all Countries and check for installation
//                // Only load those Countries marked as installed
//
//                while (rsCountries.next())
//                    {
//                    strISOCode2 = rsCountries.getString(CountriesDAOInterface.ISO_COUNTRY_CODE_2).toUpperCase();
//                    boolInstalled = rsCountries.getBoolean(CountriesDAOInterface.COUNTRY_INSTALLED);
//
//                    if (boolInstalled)
//                        {
////                        countryData = new CountryData(strISOCode2,
////                                                      rsCountries.getString(ISO_COUNTRY_CODE_3),
////                                                      rsCountries.getString(IOC_COUNTRY_CODE_3),
////                                                      boolInstalled,
////                                                      rsCountries.getString(INTERNET_DOMAIN),
////                                                      rsCountries.getString(ISO_NUMERIC),
////                                                      rsCountries.getString(ITU_DIALLING_CODE),
////                                                      rsCountries.getString(UN_VEHICLE_CODE),
////                                                      rsCountries.getString(ISO_COUNTRY_NAME),
////                                                      rsCountries.getString(ISO_COUNTRY_NAME_LOWER),
////                                                      rsCountries.getString(IOC_COUNTRY_NAME),
////                                                      rsCountries.getString(IOC_COUNTRY_NAME_LOWER));
//                        // ToDo change the database contents!
//                        countryData = new CountryData(strISOCode2,
//                                                      rsCountries.getString(CountriesDAOInterface.ISO_COUNTRY_CODE_3),
//                                                      rsCountries.getString(CountriesDAOInterface.IOC_COUNTRY_CODE_3),
//                                                      boolInstalled,
//                                                      rsCountries.getString(CountriesDAOInterface.INTERNET_DOMAIN),
//                                                      rsCountries.getString(CountriesDAOInterface.ISO_NUMERIC),
//                                                      rsCountries.getString(CountriesDAOInterface.ITU_DIALLING_CODE),
//                                                      rsCountries.getString(CountriesDAOInterface.UN_VEHICLE_CODE),
//                                                      rsCountries.getString(CountriesDAOInterface.ISO_COUNTRY_NAME),
//                                                      rsCountries.getString(CountriesDAOInterface.ISO_COUNTRY_NAME),
//                                                      rsCountries.getString(CountriesDAOInterface.ISO_COUNTRY_NAME),
//                                                      rsCountries.getString(CountriesDAOInterface.ISO_COUNTRY_NAME));
//
//                        if (debugmode)
//                            {
//                            System.out.println("Adding Country "
//                                               + " [code=" + countryData.getISOCode2() + "]"
//                                               + " [name=" + countryData.getISOCountryName() + "]");
//                            }
//
//                        // Put the CountryData in the Hashtable, keyed by the ISO2 code
//                        CountryData.getCountries().put(strISOCode2, countryData);
//                        }
//                    }
//
//                // We must have at least one Country!
//                if (CountryData.getCountries().isEmpty())
//                    {
//                    throw new FrameworkException(ExceptionLibrary.FIND_COUNTRIES);
//                    }
//
//                // Check that the requested default Country is actually installed
//                if (CountryData.getCountries().containsKey(code.toUpperCase()))
//                    {
//                    // Set the user's chosen Country
//                    RegistryModelUtilities.setCurrentCountryCode(code.toUpperCase());
//                    }
//                else
//                    {
//                    throw new FrameworkException(ExceptionLibrary.UNAVAILABLE_COUNTRY + "[code=" + code + "]");
//                    }
//                }
//            else
//                {
//                throw new SQLException(ExceptionLibrary.DATABASE_NOTACTIVE);
//                }
//            }
//
//        catch (SQLException exception)
//            {
//            FrameworkSingletons.LOGGER.handleAtomException(FrameworkSingletons.REGISTRY.getFramework(),
//                                       FrameworkSingletons.REGISTRY.getFramework().getRootTask(),
//                                       CountryData.class.getName(),
//                                       exception,
//                                       ExceptionLibrary.LOAD_COUNTRIES,
//                                       EventStatus.WARNING);
//            }
//
//        catch (FrameworkException exception)
//            {
//            FrameworkSingletons.LOGGER.handleAtomException(FrameworkSingletons.REGISTRY.getFramework(),
//                                       FrameworkSingletons.REGISTRY.getFramework().getRootTask(),
//                                       CountryData.class.getName(),
//                                       exception,
//                                       ExceptionLibrary.INVALID_COUNTRY,
//                                       EventStatus.WARNING);
//            }

        return (null);
        }
    }
