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

package org.lmn.fc.model.datatypes.types;


import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.HourMinSecInterface;

import java.util.Calendar;


/***************************************************************************************************
 * RightAscensionDataType.
 */

public final class RightAscensionDataType extends HourMinSecDataType
                                          implements HourMinSecInterface
    {
    /***********************************************************************************************
     * Don't supply anything, and get 00:00:00.
     */

    public RightAscensionDataType()
        {
        super(DataTypeDictionary.RIGHT_ASCENSION);
        enableFormatSign(false);
        }


    /***********************************************************************************************
     * Get the Time from the specified Calendar.
     *
     * @param calendar
     */

    public RightAscensionDataType(final Calendar calendar)
        {
        super(DataTypeDictionary.RIGHT_ASCENSION, calendar);
        enableFormatSign(false);
        }


    /***********************************************************************************************
     * Supply Sign, Hours, Minutes, Seconds separately, and get +hh:mm:ss.
     *
     * @param positive
     * @param hours
     * @param minutes
     * @param seconds
     */

    public RightAscensionDataType(final boolean positive,
                                  final int hours,
                                  final int minutes,
                                  final double seconds)
        {
        super(DataTypeDictionary.RIGHT_ASCENSION, positive, hours, minutes, seconds);
        enableFormatSign(false);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +hh:mm:ss.
     *
     * @param hours
     */

    public RightAscensionDataType(final double hours)
        {
        super(DataTypeDictionary.RIGHT_ASCENSION, hours);
        enableFormatSign(false);
        }
    }
