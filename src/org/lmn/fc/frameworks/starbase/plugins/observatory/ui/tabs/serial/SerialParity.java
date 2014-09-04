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
 * SerialParity.
 */

public enum SerialParity
    {
    PARITY_NONE     (0, "None"),
    PARITY_ODD      (1, "Odd"),
    PARITY_EVEN     (2, "Even"),
    PARITY_MARK     (3, "Mark"),
    PARITY_SPACE    (4, "Space");

    private final int intParity;
    private final String strName;


    /***********************************************************************************************
     * Get the SerialParity enum corresponding to the specified SerialParity name.
     * Return NULL if the SerialParity name is not found.
     *
     * @param name
     *
     * @return SerialParity
     */

    public static SerialParity getSerialParityForName(final String name)
        {
        SerialParity serialParity;

        serialParity = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SerialParity[] arraySerialParity;
            boolean boolFoundIt;

            arraySerialParity = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arraySerialParity.length);
                 i++)
                {
                final SerialParity serialparity;

                serialparity = arraySerialParity[i];

                if (name.equals(serialparity.getName()))
                    {
                    serialParity = serialparity;
                    boolFoundIt = true;
                    }
                }
            }

        return (serialParity);
        }


    /***********************************************************************************************
     * Get the SerialParity enum corresponding to the specified SerialParity value.
     * Return PARITY_NONE if the SerialParity value is invalid.
     *
     * @param parity
     *
     * @return SerialParity
     */

    public static SerialParity getSerialParityForValue(final int parity)
        {
        final SerialParity[] arraySerialParity;
        boolean boolFoundIt;
        SerialParity serialParity;

        arraySerialParity = values();
        boolFoundIt = false;
        serialParity = PARITY_NONE;

        for (int i = 0;
             (!boolFoundIt) && (i < arraySerialParity.length);
             i++)
            {
            final SerialParity serialparity;

            serialparity = arraySerialParity[i];

            if (serialparity.getParity() == parity)
                {
                serialParity = serialparity;
                boolFoundIt = true;
                }
            }

        return (serialParity);
        }


    /***********************************************************************************************
     * Construct a SerialParity.
     *
     * @param parity
     * @param displayname
     */

    private SerialParity(final int parity,
                         final String displayname)
        {
        this.intParity = parity;
        this.strName = displayname;
        }


    /***********************************************************************************************
     * Get the Parity.
     *
     * @return int
     */

    public int getParity()
        {
        return (this.intParity);
        }


    /***********************************************************************************************
     * Get the Parity Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Parity Name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
