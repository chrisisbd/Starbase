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


/***************************************************************************************************
 * Enumerate the Framework DataStores.
 */

public enum DataStore
    {
    CONFIG (0, "Config", true, "imports", "exports"),
    XML (1, "XML", true, "datastore/xml", "datastore/xml"),
    MYSQL (2, "MySQL", false, "datastore/mysql", "datastore/mysql"),
    HSQLDB (3, "hsqldb", false, "datastore/hsqldb", "datastore/hsqldb"),
    DIST (4, "Distribution", true, "datastore/distribution", "datastore/distribution");

    private final int intValue;
    private final String strName;
    private final boolean boolAvailable;
    private String strLoadFolder;
    private String strSaveFolder;


    /***********************************************************************************************
     * Privately construct a DataStore.
     *
     * @param value
     * @param name
     * @param available
     * @param loadfolder
     * @param savefolder
     */

    private DataStore(final int value,
                      final String name,
                      final boolean available,
                      final String loadfolder,
                      final String savefolder)
        {
        intValue = value;
        strName = name;
        boolAvailable = available;
        strLoadFolder = loadfolder;
        strSaveFolder = savefolder;
        }


    /***********************************************************************************************
     * Get the ID of the DataStore Type.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intValue);
        }


    /***********************************************************************************************
     * Get the name of the DataStore.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the name of the DataStore.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get a flag indicating if the DataStore is available, i.e. implemented.
     *
     * @return boolean
     */

    public boolean isAvailable()
        {
        return (this.boolAvailable);
        }


    /***********************************************************************************************
     * Get the name of the folder from which to Load the data for this DataStore.
     *
     * @return String
     */

    public String getLoadFolder()
        {
        return (this.strLoadFolder);
        }


    /***********************************************************************************************
     * Get the name of the folder to which to Save the data for this DataStore.
     *
     * @return String
     */

    public String getSaveFolder()
        {
        return (this.strSaveFolder);
        }


    /***********************************************************************************************
     * Check that the specified DataStore is valid and available for use.
     *
     * @param store
     *
     * @return boolean
     */

    public boolean isValidDataStore(final DataStore store)
        {
        final boolean boolValid;

        boolValid = (isAvailable()) && (this.equals(store));

        return (boolValid);
        }
    }
