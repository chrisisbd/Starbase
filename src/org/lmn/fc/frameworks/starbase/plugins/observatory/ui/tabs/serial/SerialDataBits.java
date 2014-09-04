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
 * SerialDataBits.
 */

public enum SerialDataBits
    {
    DATABITS_5   (5, "5 bits"),
    DATABITS_6   (6, "6 bits"),
    DATABITS_7   (7, "7 bits"),
    DATABITS_8   (8, "8 bits");

    private final int intDataBits;
    private final String strName;


    /***********************************************************************************************
     * Get the SerialDataBits enum corresponding to the specified SerialDataBits name.
     * Return NULL if the SerialDataBits name is not found.
     *
     * @param name
     *
     * @return SerialDataBits
     */

    public static SerialDataBits getSerialDataBitsForName(final String name)
        {
        SerialDataBits dataBits;

        dataBits = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SerialDataBits[] arrayDataBits;
            boolean boolFoundIt;

            arrayDataBits = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayDataBits.length);
                 i++)
                {
                final SerialDataBits databits;

                databits = arrayDataBits[i];

                if (name.equals(databits.getName()))
                    {
                    dataBits = databits;
                    boolFoundIt = true;
                    }
                }
            }

        return (dataBits);
        }


    /***********************************************************************************************
     * Get the SerialDataBits enum corresponding to the specified SerialDataBits value.
     * Return DATABITS_8 if the SerialDataBits value is invalid.
     *
     * @param databits
     *
     * @return SerialDataBits
     */

    public static SerialDataBits getSerialDataBitsForValue(final int databits)
        {
        final SerialDataBits[] arrayDataBits;
        boolean boolFoundIt;
        SerialDataBits dataBits;

        arrayDataBits = values();
        boolFoundIt = false;
        dataBits = DATABITS_8;

        for (int i = 0;
             (!boolFoundIt) && (i < arrayDataBits.length);
             i++)
            {
            final SerialDataBits bits;

            bits = arrayDataBits[i];

            if (bits.getDataBits() == databits)
                {
                dataBits = bits;
                boolFoundIt = true;
                }
            }

        return (dataBits);
        }


    /***********************************************************************************************
     * Construct a SerialDataBits.
     *
     * @param databits
     * @param displayname
     */

    private SerialDataBits(final int databits,
                           final String displayname)
        {
        this.intDataBits = databits;
        this.strName = displayname;
        }


    /***********************************************************************************************
     * Get the DataBits.
     *
     * @return int
     */

    public int getDataBits()
        {
        return (this.intDataBits);
        }


    /***********************************************************************************************
     * Get the DataBits Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the DataBits Name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }