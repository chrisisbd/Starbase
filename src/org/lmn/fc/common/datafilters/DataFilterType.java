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

package org.lmn.fc.common.datafilters;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * DataFilterType.
 */

public enum DataFilterType
    {
    // ToDo Load filters at run time, do not assume which ones will be present
    PASS_THROUGH      ("PassThrough",
                       "org.lmn.fc.common.datafilters.impl.passthroughfilter.PassThroughFilter"),
    SIMPLE_INTEGRATOR ("SimpleIntegrator",
                       "org.lmn.fc.common.datafilters.impl.simpleintegratorfilter.SimpleIntegratorFilter"),
    LINEAR_TRANSFORM  ("LinearTransform",
                       "org.lmn.fc.common.datafilters.impl.lineartransformfilter.LinearTransformFilter"),
    GOESXRAY_LINEAR   ("GOESXrayLinear",
                       "org.lmn.fc.common.datafilters.impl.goesxraylinearfilter.GOESXrayLinearFilter"),
    CUSTOM            ("Custom",
                       "org.lmn.fc.common.datafilters.impl.customfilter.CustomFilter"),
    TEST              ("Test",
                       "org.lmn.fc.common.datafilters.impl.testfilter.TestFilter");

    private final String strName;
    private final String strFilterClassname;


    /***********************************************************************************************
     * Get the DataFilterType enum corresponding to the specified DataFilter name.
     *
     * @param name
     *
     * @return DataFormat
     */

    public static DataFilterType getDataFilterTypeForName(final String name)
        {
        DataFilterType dataFilterType;

        dataFilterType = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final DataFilterType[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final DataFilterType filterType;

                filterType = types[i];

                if (name.equals(filterType.getName()))
                    {
                    dataFilterType = filterType;
                    boolFoundIt = true;
                    }
                }
            }

        return (dataFilterType);
        }


    /***********************************************************************************************
     * DataFilterType.
     *
     * @param name
     * @param filterclass
     */

    private DataFilterType(final String name,
                           final String filterclass)
        {
        this.strName = name;
        this.strFilterClassname = filterclass;
        }


    /***********************************************************************************************
     * Get the name of the DataFilterType.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the FilterClassname of the DataFilterType.
     *
     * @return String
     */

    public String getFilterClassname()
        {
        return (this.strFilterClassname);
        }
    }
