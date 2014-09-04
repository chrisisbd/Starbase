// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * SerialStopBits.
 */

public enum SerialStopBits
    {
    STOPBITS_1   (1, "1 bit"),
    STOPBITS_2   (2, "2 bits"),
    STOPBITS_1_5 (3, "1.5 bits");

    private final int intStopBits;
    private final String strName;


    /***********************************************************************************************
     * Get the SerialStopBits enum corresponding to the specified SerialStopBits name.
     * Return NULL if the SerialStopBits name is not found.
     *
     * @param name
     *
     * @return SerialStopBits
     */

    public static SerialStopBits getSerialStopBitsForName(final String name)
        {
        SerialStopBits stopBits;

        stopBits = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SerialStopBits[] arrayStopBits;
            boolean boolFoundIt;

            arrayStopBits = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayStopBits.length);
                 i++)
                {
                final SerialStopBits stopbits;

                stopbits = arrayStopBits[i];

                if (name.equals(stopbits.getName()))
                    {
                    stopBits = stopbits;
                    boolFoundIt = true;
                    }
                }
            }

        return (stopBits);
        }


    /***********************************************************************************************
     * Get the SerialStopBits enum corresponding to the specified SerialStopBits value.
     * Return STOPBITS_1 if the SerialStopBits value is invalid.
     *
     * @param stopbits
     *
     * @return SerialStopBits
     */

    public static SerialStopBits getSerialStopBitsForValue(final int stopbits)
        {
        final SerialStopBits[] arrayStopBits;
        boolean boolFoundIt;
        SerialStopBits stopBits;

        arrayStopBits = values();
        boolFoundIt = false;
        stopBits = STOPBITS_1;

        for (int i = 0;
             (!boolFoundIt) && (i < arrayStopBits.length);
             i++)
            {
            final SerialStopBits bits;

            bits = arrayStopBits[i];

            if (bits.getStopBits() == stopbits)
                {
                stopBits = bits;
                boolFoundIt = true;
                }
            }

        return (stopBits);
        }


    /***********************************************************************************************
     * Construct a SerialStopBits.
     *
     * @param stopbits
     * @param displayname
     */

    private SerialStopBits(final int stopbits,
                           final String displayname)
        {
        this.intStopBits = stopbits;
        this.strName = displayname;
        }


    /***********************************************************************************************
     * Get the StopBits.
     *
     * @return int
     */

    public int getStopBits()
        {
        return (this.intStopBits);
        }


    /***********************************************************************************************
     * Get the StopBits Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the StopBits Name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
