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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * Epoch.
 * An arbitrary Julian epoch is related to the Julian date by
 * J = 2000.0 + (Julian date ? 2451545.0)/365.25
 */

public enum Epoch
    {
    J2000     (0, "J2000.0", 2000.0, 2451545.0),      // Julian date 2451545.0 TT (Terrestrial Time), or January 1, 2000, noon TT
                                                      // International Atomic Time is 11:59:27.816; Coordinated Universal Time is 11:58:55.816
    B1950     (1, "B1950.0", 1950.0, 2433282.4235),   // B1950.0 = JDE 2433282.4235 = 1950 January 0.9235 TT

    B1900     (2, "B1900.0", 1900.0, 2415020.3135);   // B1900.0 = JDE 2415020.3135 = 1900 January 0.8135 TT


    private final int intTypeID;
    private final String strName;
    private final double dblValue;
    private final double dblJD;


    /***********************************************************************************************
     * Get the Epoch enum corresponding to the specified Epoch name.
     * Return NULL if not found.
     *
     * @param name
     *
     * @return Epoch
     */

    public static Epoch getEpochForName(final String name)
        {
        Epoch epoch;

        //LOGGER.debug("Epoch.getEpochForName() [name=" + name + "]");

        epoch = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final Epoch[] epochs;
            boolean boolFoundIt;

            epochs = Epoch.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < epochs.length);
                 i++)
                {
                final Epoch epochTest;

                epochTest = epochs[i];

                // Allow the final ".0" to be missing
                if ((name.equals(epochTest.getName()))
                    || ((epochTest.getName().contains(".0"))
                        && name.equals(epochTest.getName().substring(0, epochTest.getName().indexOf(".0")))))
                    {
                    epoch = epochTest;
                    boolFoundIt = true;
                    }
                }
            }

        return (epoch);
        }


    /***********************************************************************************************
     * Epoch.
     *
     * @param typedid
     * @param name
     * @param value
     * @param juliandate
     */

    private Epoch(final int typedid,
                  final String name,
                  final double value,
                  final double juliandate)
        {
        this.intTypeID = typedid;
        this.strName = name;
        this.dblValue = value;
        this.dblJD = juliandate;
        }


    /***********************************************************************************************
     * Get the TypeID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intTypeID);
        }


    /***********************************************************************************************
     * Get the Epoch name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Epoch value.
     *
     * @return double
     */

    public double getValue()
        {
        return (this.dblValue);
        }


    /***********************************************************************************************
     * Get the Epoch Julian Date.
     *
     * @return double
     */

    public double getJD()
        {
        return (this.dblJD);
        }


    /***********************************************************************************************
     * Get the Epoch name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
