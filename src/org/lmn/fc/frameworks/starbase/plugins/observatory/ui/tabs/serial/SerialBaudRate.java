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
 * SerialBaudRate.
 */

public enum SerialBaudRate
    {
    RATE_1200   (1200,   "1200 baud"),
    RATE_2400   (2400,   "2400 baud"),
    RATE_4800   (4800,   "4800 baud"),
    RATE_9600   (9600,   "9600 baud"),
    RATE_19200  (19200,  "19200 baud"),
    RATE_38400  (38400,  "38400 baud"),
    RATE_57600  (57600,  "57600 baud"),
    RATE_115200 (115200, "115200 baud");

    private final int intBaudRate;
    private final String strName;


    /***********************************************************************************************
     * Get the SerialBaudRate enum corresponding to the specified SerialBaudRate name.
     * Return NULL if the SerialBaudRate name is not found.
     *
     * @param name
     *
     * @return SerialBaudRate
     */

    public static SerialBaudRate getSerialBaudRateForName(final String name)
        {
        SerialBaudRate baudRate;

        baudRate = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final SerialBaudRate[] arrayBaudRates;
            boolean boolFoundIt;

            arrayBaudRates = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < arrayBaudRates.length);
                 i++)
                {
                final SerialBaudRate baudrate;

                baudrate = arrayBaudRates[i];

                if (name.equals(baudrate.getName()))
                    {
                    baudRate = baudrate;
                    boolFoundIt = true;
                    }
                }
            }

        return (baudRate);
        }


    /***********************************************************************************************
     * Get the SerialBaudRate enum corresponding to the specified SerialBaudRate value.
     * Return RATE_1200 if the SerialBaudRate value is invalid.
     *
     * @param baudrate
     *
     * @return SerialBaudRate
     */

    public static SerialBaudRate getSerialBaudRateForValue(final int baudrate)
        {
        final SerialBaudRate[] arrayBaudRates;
        boolean boolFoundIt;
        SerialBaudRate baudRate;

        arrayBaudRates = values();
        boolFoundIt = false;
        baudRate = RATE_1200;

        for (int i = 0;
             (!boolFoundIt) && (i < arrayBaudRates.length);
             i++)
            {
            final SerialBaudRate rate;

            rate = arrayBaudRates[i];

            if (rate.getBaudRate() == baudrate)
                {
                baudRate = rate;
                boolFoundIt = true;
                }
            }

        return (baudRate);
        }


    /***********************************************************************************************
     * Construct a SerialBaudRate.
     *
     * @param baudrate
     * @param displayname
     */

    private SerialBaudRate(final int baudrate,
                           final String displayname)
        {
        this.intBaudRate = baudrate;
        this.strName = displayname;
        }


    /***********************************************************************************************
     * Get the BaudRate.
     *
     * @return int
     */

    public int getBaudRate()
        {
        return (this.intBaudRate);
        }


    /***********************************************************************************************
     * Get the BaudRate Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the BaudRate Name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
