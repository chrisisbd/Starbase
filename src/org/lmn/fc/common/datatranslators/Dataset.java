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

package org.lmn.fc.common.datatranslators;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * Dataset.
 * An enumeration of possible datasets.
 * Currently only RawData and ProcessedData.
 */

public enum Dataset
    {
    RAW         (0, "RawData"),
    PROCESSED   (1, "ProcessedData");


    private final int intIndex;
    private final String strName;


    /***********************************************************************************************
     * Get the Dataset enum corresponding to the specified Dataset name.
     *
     * @param name
     *
     * @return Dataset
     */

    public static Dataset getDatasetForName(final String name)
        {
        Dataset dataset;

        dataset = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final Dataset[] datasets;
            boolean boolFoundIt;

            datasets = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < datasets.length);
                 i++)
                {
                final Dataset dataSet;

                dataSet = datasets[i];

                if (name.equals(dataSet.getDatasetName()))
                    {
                    dataset = dataSet;
                    boolFoundIt = true;
                    }
                }
            }

        return (dataset);
        }


    /***********************************************************************************************
     * Dataset.
     *
     * @param index
     * @param name
     */

    private Dataset(final int index,
                    final String name)
        {
        intIndex = index;
        strName = name;
        }


    /***********************************************************************************************
     * Get the Index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the Dataset name.
     *
     * @return String
     */

    public String getDatasetName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Dataset name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
