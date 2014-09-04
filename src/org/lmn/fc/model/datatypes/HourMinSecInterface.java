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

import java.math.BigDecimal;


/***************************************************************************************************
 * HourMinSecInterface.
 */

public interface HourMinSecInterface extends RootDataTypeInterface,
                                             FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkSingletons,
                                             FrameworkMetadata,
                                             ResourceKeys
    {
    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Construct the HMS from Hours (which could be negative), to a precision of 1msec.
     * Use BigDecimal to guarantee no rounding errors!
     *
     * @param hours
     */

    void setFromBigHours(BigDecimal hours);


    /***********************************************************************************************
     * Get the state of the Sign.
     *
     * @return boolean
     */

    boolean isPositive();


    /***********************************************************************************************
     * Set the state of the Sign.
     *
     * @param positive
     */

    void setPositive(boolean positive);


    /***********************************************************************************************
     * Get the Hours value.
     *
     * @return int
     */

    int getHours();


    /***********************************************************************************************
     * Set the Hours value.
     * The sign must be supplied also, to avoid problems if Hour=0.
     *
     * @param hours
     * @param positive
     */

    void setHours(int hours, boolean positive);


    /***********************************************************************************************
     * Get the Minutes value.
     *
     * @return
     */

    int getMinutes();


    /***********************************************************************************************
     * Set the Minutes value.
     *
     * @param minutes
     */

    void setMinutes(int minutes);


    /***********************************************************************************************
     * Get the Seconds value.
     *
     * @return double
     */

    double getSeconds();


    /***********************************************************************************************
     * Set the Seconds value.
     *
     * @param seconds
     */

    void setSeconds(double seconds);


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Indicate if this HMS DataType has the sign preceding the value.
     *
     * @return boolean
     */

    boolean hasFormatSign();


    /***********************************************************************************************
     * Control the use of a sign (+/-) on the formatted output.
     *
     * @param sign
     */

    void enableFormatSign(boolean sign);


    /***********************************************************************************************
     * Return the HourMinSecDataType as a signed double value.
     *
     * @return double
     */

    double toDouble();


    /***********************************************************************************************
     * Return the HourMinSecDataType as a formatted, delimited String.
     * Note that seconds are rounded to the nearest integer.
     * Primarily intended for reports and display.
     *
     * @return String
     */

    String toString_HH_MM_SS();


    /***********************************************************************************************
     * Return the HourMinSecDataType as a formatted, delimited String.
     * Note that seconds may be formatted with a fractional part.
     * WARNING! You may find that e.g. 00:01:59.9995 is produced where 00:02:00 was intended.
     * Read about rounding problems, BigDecimal, BigInteger and so on.
     *
     * @return String
     */

    String toString();


    /***********************************************************************************************
     * Indicate if this HMS instance is after the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    boolean after(HourMinSecInterface hmswhen);


    /***********************************************************************************************
     * Indicate if this HMS instance is equal to or after the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    boolean equalOrAfter(HourMinSecInterface hmswhen);


    /***********************************************************************************************
     * Indicate if this HMS instance is before the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    boolean before(HourMinSecInterface hmswhen);


    /***********************************************************************************************
     * Indicate if this HMS instance is before or equal to the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    boolean beforeOrEqual(HourMinSecInterface hmswhen);
    }
