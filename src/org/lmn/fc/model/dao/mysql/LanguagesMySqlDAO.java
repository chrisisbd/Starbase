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

import org.lmn.fc.database.impl.FrameworkDatabase;

import javax.swing.tree.DefaultMutableTreeNode;


/***************************************************************************************************
 */

public class LanguagesMySqlDAO
    {
    /***********************************************************************************************
     * Load all installed Languages from the specified FrameworkDatabase
     *
     * @param database The FrameworkDatabase with an open connection
     * @param code The ISO language code for the requested default language
     * @param debugmode
     *
     * @return DefaultMutableTreeNode
     */

    public static DefaultMutableTreeNode loadLanguages(FrameworkDatabase database,
                                                       String code,
                                                       boolean debugmode)
        {
//        DefaultMutableTreeNode nodeLanguages;
//        AtomPlugin expanderLanguages;
//        PreparedStatement psLanguages;
//        ResultSet rsLanguages;
//        LanguageData languageData;
//        String strISOCode;
//        String strLanguage;
//        boolean boolInstalled;
//        QueryPlugin queryPlugin;
//
//        LanguageData.hashtableLanguages = new Hashtable<String, LanguageData>(5);
//
//        nodeLanguages = new DefaultMutableTreeNode();
//        expanderLanguages = new org.lmn.fc.model.atoms.components.NullData("path",
//                                                                           LanguagePlugin.LANGUAGES_EXPANDER_NAME,
//                                                                           LanguagePlugin.LANGUAGES_ICON,
//                                                                           nodeLanguages);
//        nodeLanguages.setUserObject(expanderLanguages);
//
//        try
//            {
//            if ((database != null)
//                && (!database.getConnection().isClosed()))
//                {
//                queryPlugin = FrameworkSingletons.REGISTRY.getQueryData(LanguagesDAOInterface.SELECT_LANGUAGE_LIST);
//                psLanguages = queryPlugin.getPreparedStatement(DataStore.MYSQL);
//
//                rsLanguages = queryPlugin.executeQuery(LanguageData.class,
//                                                       psLanguages,
//                                                       RegistryModel.getInstance().getSqlTrace(),
//                                                       RegistryModel.getInstance().getSqlTiming());
//
//                // Now loop through all Languages and put in the hashtable
//                while (rsLanguages.next())
//                    {
//                    strISOCode = rsLanguages.getString(LanguagesDAOInterface.LANGUAGE_ISOCODE).toLowerCase();
//                    if (strISOCode.length() != LanguageData.LENGTH_CODE)
//                        {
//                        throw new FrameworkException(ExceptionLibrary.INVALID_LANGUAGE
//                                                     + " [ISOLanguageCode=" + strISOCode + "]");
//                        }
//
//                    strLanguage = rsLanguages.getString(LanguagesDAOInterface.LANGUAGE_NAME);
//                    boolInstalled = rsLanguages.getBoolean(LanguagesDAOInterface.LANGUAGE_INSTALLED);
//
//                    if (boolInstalled)
//                        {
//                        languageData = new LanguageData(strISOCode,
//                                                        strLanguage,
//                                                        boolInstalled);
//
//                        if (debugmode)
//                            {
//                            System.out.println("Adding Language "
//                                               + " [code=" + languageData.getISOCode2() + "]"
//                                               + " [name=" + languageData.getName() + "]");
//                            }
//
//                        LanguageData.getLanguages().put(strISOCode, languageData);
//                        }
//                    }
//
//                // We must have at least one Language!
//                if (LanguageData.getLanguages().isEmpty())
//                    {
//                    throw new FrameworkException(ExceptionLibrary.FIND_LANGUAGES);
//                    }
//
//                // Check that the requested Language is actually installed
//                if (LanguageData.getLanguages().containsKey(code.toLowerCase()))
//                    {
//                    // Set the user's chosen Language
//                    RegistryModelUtilities.setCurrentLanguage(code.toLowerCase());
//                    }
//                else
//                    {
//                    throw new FrameworkException(ExceptionLibrary.UNAVAILABLE_LANGUAGE + "[code=" + code + "]");
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
//                                       LanguageData.class.getName(),
//                                       exception,
//                                       ExceptionLibrary.LOAD_LANGUAGES,
//                                       EventStatus.FATAL);
//            }
//
//        catch(FrameworkException exception)
//            {
//            FrameworkSingletons.LOGGER.handleAtomException(FrameworkSingletons.REGISTRY.getFramework(),
//                                       FrameworkSingletons.REGISTRY.getFramework().getRootTask(),
//                                       LanguageData.class.getName(),
//                                       exception,
//                                       ExceptionLibrary.LOAD_LANGUAGES,
//                                       EventStatus.FATAL);
//            }

        return (null);
        }
    }
