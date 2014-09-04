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

package org.lmn.fc.model.datatypes;


import java.text.DecimalFormat;


/***************************************************************************************************
 * DecimalFormatPattern using ThreadLocal DecimalFormat.
 */

public enum DecimalFormatPattern
    {
    JD                          ("0000000.00000",   ""),

    // Multi-radix YMD
    YEAR                        ("0000",            "-"),
    MONTH                       ("00",              "-"),
    DAY                         ("00",              ""),

    // Multi-radix Angles first part
    DEGREES_360                 ("000",             ":"),
    DEGREES_90                  ("00",              ":"),
    HOURS                       ("00",              ":"),

    // Multi-radix Angles second part
    MINUTES                     ("00",              ":"),

    // Multi-radix Angles third part
    SECONDS_S                   ("00",              ""),
    SECONDS_MS                  ("00.000",          ""),
    SECONDS_LONGITUDE           ("00.000",          ""),
    SECONDS_LATITUDE            ("00.0",            ""),
    SECONDS_DECLINATION         ("00.00",           ""),

    // (Azimuth, Elevation)
    AZIMUTH                     ("000.0",           ","),
    AZIMUTH_SCALE               ("000",             ","),

    ELEVATION                   ("00.0",            ""),
    ELEVATION_SCALE             ("00",              ""),

    // (Right Ascension, Declination)
    DECLINATION                 ("00.0",            ""),

    // (Longitude, Latitude)
    LONGITUDE_GALACTIC          ("000.0",           ","),
    LATITUDE_GALACTIC           ("00.0",            ""),

    // Miscellaneous
    DECIMAL_INTEGER             ("0000",            ""),
    SIGNED_DECIMAL_INTEGER      ("+0000;-0000",     ""),
    SIGNED_PERCENTAGE           ("+00.0;-00.0",     ""),
    DECIMAL_DOUBLE              ("+#0.00;-#0.00",   ""),
    PERCENTAGE                  ("000.###",         ""),
    PHASE                       ("+0.00;-0.00",     ""),
    GPS_PRN                     ("00",              ""),
    VLSR                        ("00.00",           ""),
    PEARSON                     ("0.000",           "");


    private final ThreadLocal<DecimalFormat> formatThreadLocal;
    private final String strPattern;
    private final String strDelimiter;


    /***********************************************************************************************
     * Creates a new DecimalFormatPattern object.
     *
     * @param pattern
     * @param delimiter
     */

    private DecimalFormatPattern(final String pattern,
                                 final String delimiter)
        {
        this.formatThreadLocal = new ThreadLocal< DecimalFormat >()
                                    {
                                    @Override
                                    protected DecimalFormat initialValue()
                                        {
                                        return new DecimalFormat(pattern);
                                        }
                                    };

        this.strPattern = pattern;
        this.strDelimiter = delimiter;
        }


    /***********************************************************************************************
     * Get the Formatter enum.
     *
     * @return DecimalFormat
     */

    public DecimalFormat getFormatter()
        {
        return (this.formatThreadLocal.get());
        }


    /***********************************************************************************************
     * Format the specified number using the Formatter enum.
     *
     * @param number
     *
     * @return String
     */

    public String format(final double number)
        {
        return (this.formatThreadLocal.get().format(number));
        }


    /***********************************************************************************************
     * Format the specified number using the Formatter enum.
     *
     * @param number
     *
     * @return String
     */

    public String format(final long number)
        {
        return (this.formatThreadLocal.get().format(number));
        }


    /***********************************************************************************************
     * Get the Pattern character(s) associated with this DecimalFormatPattern enum.
     *
     * @return String
     */

    public String getPattern()
        {
        return (this.strPattern);
        }


    /***********************************************************************************************
     * Get the Delimiter character(s) associated with this DecimalFormatPattern enum.
     *
     * @return String
     */

    public String getDelimiter()
        {
        return (this.strDelimiter);
        }
    }
