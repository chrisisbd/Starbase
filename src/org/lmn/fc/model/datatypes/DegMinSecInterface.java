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

package org.lmn.fc.model.datatypes;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.DegMinSecException;

import java.math.BigDecimal;


/***************************************************************************************************
 * DegMinSecInterface.
 */

public interface DegMinSecInterface extends RootDataTypeInterface,
                                            FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkSingletons,
                                            FrameworkMetadata,
                                            ResourceKeys
    {
    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Set the Value of the DegMinSec from the specified Degrees as a BigDecimal.
     * Used when the internal configuration should be maintained, e.g. formats.
     *
     * @param degrees
     */

    void setFromBigDegrees(BigDecimal degrees);


    /***********************************************************************************************
     * Get the Sign.
     *
     * @return boolean
     */

    boolean isPositive();


    /***********************************************************************************************
     * Set the Sign.
     *
     * @param positive
     */

    void setPositive(boolean positive);


    /***********************************************************************************************
     * Get the Degrees value.
     * Note that this still fails if Degrees=0 and the sign isn't read at the same time.
     *
     * @return
     */

    int getDegrees();


    /***********************************************************************************************
     * Set the Degrees value.
     * The sign must be supplied also, to avoid problems if Degrees=0.
     *
     * @param degrees
     * @param positive
     */

    void setDegrees(int degrees,
                    boolean positive);


    /***********************************************************************************************
     * Get the Minutes value.
     *
     * @return int
     */

    int getMinutes();


    /***********************************************************************************************
     * Set the Minutes value {0...59}.
     *
     * @param minutes
     *
     * @throws DegMinSecException
     */

    void setMinutes(int minutes) throws DegMinSecException;


    /***********************************************************************************************
     * Get the Seconds value.
     *
     * @return double
     */

    double getSeconds();


    /***********************************************************************************************
     * Set the Seconds value {0.0...59.999}.
     *
     * @param seconds
     *
     * @throws DegMinSecException
     */

    void setSeconds(double seconds) throws DegMinSecException;


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Get the display format, i.e. signed or compass points.
     *
     * @return DegMinSecUnit
     */

    DegMinSecFormat getDisplayFormat();


    /***********************************************************************************************
     * Control the use of a sign (+/-), (E/W), (N/S) on the formatted output.
     *
     * @param dmsformat
     */

    void setDisplayFormat(DegMinSecFormat dmsformat);


    /***********************************************************************************************
     * Apply the default pattern for 360 degree range.
     * The sign or E/W/N/S is handled with setDisplayFormat().
     *
     * @param secondspattern
     */

    void apply360DegreeSecondsPattern(DecimalFormatPattern secondspattern);


    /***********************************************************************************************
     * Apply the default pattern for +/-90 degree range.
     * The sign or E/W/N/S is handled with setDisplayFormat().
     *
     * @param secondspattern
     */

    void apply90DegreeSecondsPattern(DecimalFormatPattern secondspattern);


    /***********************************************************************************************
     * Return the DegMinSec as a signed double value.
     *
     * @return double
     */

    double toDouble();


    /***********************************************************************************************
     * Return the DegMinSecDataType as a formatted, delimited String.
     * Note that seconds are rounded to the nearest integer,
     * so internally degrees may be 012:34:56.995, but it will display as 012:34:57.
     * Primarily intended for reports and display.
     *
     * @return String
     */

    String toString_DDD_MM_SS();


    /***********************************************************************************************
     * Return the DegMinSec as a formatted, delimited String.
     * Note that seconds may be formatted with a fractional part.
     * Add a sign indicator '+/-', 'E/W', 'N/S' if required.
     * WARNING! You may find that e.g. 00:01:59.9995 is produced where 00:02:00 was intended.
     * Read about rounding problems, BigDecimal, BigInteger and so on.
     *
     * @return String
     */

    String toString();
    }
