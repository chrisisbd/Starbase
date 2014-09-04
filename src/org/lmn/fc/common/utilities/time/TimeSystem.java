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

package org.lmn.fc.common.utilities.time;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * TimeSystem.
 */

public enum TimeSystem
    {
    LMT     (0, "LMT",      "00:00:00",        "Local Mean Time"),
    UT      (1, "UT",       "00:00:00",        "Universal Time"),
    JD0     (2, "JD0",      "0000000.00000",   "Julian Day at 0h UT"),
    JD      (3, "JD",       "0000000.00000",   "Julian Day"),
    GMST0   (4, "GMST0",    "00:00:00",        "Greenwich Mean ST 0h UT"),
    GAST0   (5, "GAST0",    "00:00:00",        "Greenwich Apparent ST 0h UT"),
    GMST    (6, "GMST",     "00:00:00",        "Greenwich Mean ST"),
    GAST    (7, "GAST",     "00:00:00",        "Greenwich Apparent ST"),
    LMST    (8, "LMST",     "00:00:00",        "Local Mean ST"),
    LAST    (9, "LAST",     "00:00:00",        "Local Apparent ST");


    private final int intIndex;
    private final String strMnemonic;
    private final String strFormat;
    private final String strName;


    /***********************************************************************************************
     * Get the TimeSystem enum corresponding to the specified TimeSystem mnemonic.
     * Return NULL if the TimeSystem mnemonic is not found.
     *
     * @param mnemonic
     *
     * @return TimeSystem
     */

    public static TimeSystem getTimeSystemForMnemonic(final String mnemonic)
        {
        TimeSystem timeSystem;

        timeSystem = null;

        if ((mnemonic != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(mnemonic)))
            {
            final TimeSystem[] systems;
            boolean boolFoundIt;

            systems = TimeSystem.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < systems.length);
                 i++)
                {
                final TimeSystem system;

                system = systems[i];

                if (mnemonic.equals(system.getMnemonic()))
                    {
                    timeSystem = system;
                    boolFoundIt = true;
                    }
                }
            }

        return (timeSystem);
        }


    /***********************************************************************************************
     * Privately construct a TimeSystem.
     *
     * @param index
     * @param mnemonic
     * @param format
     * @param name
     */

    private TimeSystem(final int index,
                       final String mnemonic,
                       final String format,
                       final String name)
        {
        this.intIndex = index;
        this.strMnemonic = mnemonic;
        this.strFormat = format;
        this.strName = name;
        }


    /***********************************************************************************************
     * Get the TimeSystem index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the TimeSystem Mnemonic.
     *
     * @return String
     */

    public String getMnemonic()
        {
        return (this.strMnemonic);
        }


    /***********************************************************************************************
     * Get the Format string.
     *
     * @return String
     */

    public String getFormat()
        {
        return (this.strFormat);
        }


    /***********************************************************************************************
     * Get the TimeSystem Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }
    }

