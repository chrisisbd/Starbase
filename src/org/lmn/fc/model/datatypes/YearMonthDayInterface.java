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
import org.lmn.fc.common.exceptions.YearMonthDayException;


/***************************************************************************************************
 * YearMonthDayInterface.
 */

public interface YearMonthDayInterface extends RootDataTypeInterface,
                                               FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkSingletons,
                                               FrameworkMetadata,
                                               ResourceKeys
    {
    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Set the internal state of this YMD from the specified YMD.
     *
     * @param ymd
     */

    void copy(YearMonthDayInterface ymd);


    /***********************************************************************************************
     * Indicate if the YMD is positive.
     *
     * @param positive
     */

    void setPositive(boolean positive);


    /***********************************************************************************************
     * Indicate if the YMD is positive.
     *
     * @return boolean
     */

    boolean isPositive();


    /***********************************************************************************************
     * Get the Year field.
     *
     * @return int
     */

    int getYear();


    /**********************************************************************************************
     * Set the Year field.
     *
     * @param year
     * @param positive
     *
     * @throws YearMonthDayException
     */

    void setYear(int year, boolean positive) throws YearMonthDayException;


    /***********************************************************************************************
     * Get the Month field.
     * NOTE: Calendar months {0...11} but the instance variable holds {1...12}.
     *
     * @return int
     */

    int getMonth();


    /***********************************************************************************************
     * Set the Month field.
     *
     * @param month
     *
     * @throws YearMonthDayException
     */

    void setMonth(int month) throws YearMonthDayException;


    /***********************************************************************************************
     * Get the Day field.
     *
     * @return int
     */

    int getDay();


    /***********************************************************************************************
     * Set the Day field.
     *
     * @param day
     *
     * @throws YearMonthDayException
     */

    void setDay(int day) throws YearMonthDayException;


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Indicate if this YMD DataType has the sign preceding the value.
     *
     * @return boolean
     */

    boolean hasFormatSign();


    /***********************************************************************************************
     * Enable the use of a sign (+/-) in the display format.
     *
     * @param sign
     */

    void enableFormatSign(boolean sign);


    /***********************************************************************************************
     * Return the YearMonthDayDataType as a signed double value.
     *
     * @return double
     */

    double toDouble();


    /***********************************************************************************************
     * Return the YearMonthDayDataType as a formatted, delimited String.
     *
     * @return String
     */

    String toString();


    /***********************************************************************************************
     * Indicate if this YMD instance is after the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    boolean after(YearMonthDayInterface ymdwhen);


    /***********************************************************************************************
     * Indicate if this YMD instance is equal to or after the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    boolean equalOrAfter(YearMonthDayInterface ymdwhen);


    /***********************************************************************************************
     * Indicate if this YMD instance is before the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    boolean before(YearMonthDayInterface ymdwhen);


    /***********************************************************************************************
     * Indicate if this YMD instance is before or equal to the specified YMD value.
     * Note that the YMD values may be negative!
     *
     * @param ymdwhen
     *
     * @return boolean
     */

    boolean beforeOrEqual(YearMonthDayInterface ymdwhen);
    }
