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

import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;

import java.math.BigDecimal;


/***************************************************************************************************
 * LongitudeDataType.
 */

public final class LongitudeDataType extends DegMinSec
                                     implements DegMinSecInterface
    {
    private static final DegMinSecFormat DEFAULT_UNIT_LONGITUDE = DegMinSecFormat.SIGN;


    /***********************************************************************************************
     * LongitudeDataType.
     */

    public LongitudeDataType()
        {
        super(DataTypeDictionary.SIGNED_LONGITUDE);

        setDisplayFormat(DEFAULT_UNIT_LONGITUDE);
        apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        }


    /***********************************************************************************************
     * Supply Sign, Degrees, Minutes, Seconds separately, and get +ddd:mm:ss.sss.
     *
     * @param positive
     * @param degrees
     * @param minutes
     * @param seconds
     *
     * @throws DegMinSecException
     */

    public LongitudeDataType(final boolean positive,
                             final int degrees,
                             final int minutes,
                             final double seconds) throws DegMinSecException
        {
        super(DataTypeDictionary.SIGNED_LONGITUDE,
              positive,
              degrees,
              minutes,
              seconds);

        setDisplayFormat(DEFAULT_UNIT_LONGITUDE);
        apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +ddd:mm:ss.sss.
     *
     * @param degrees
     *
     * @throws DegMinSecException
     */

    public LongitudeDataType(final BigDecimal degrees) throws DegMinSecException
        {
        super(DataTypeDictionary.SIGNED_LONGITUDE, degrees);

        setDisplayFormat(DEFAULT_UNIT_LONGITUDE);
        apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +ddd:mm:ss.sss.
     * This should be avoided - use the constructor with BigDecimal instead.
     *
     * @param degrees
     *
     * @throws DegMinSecException
     */

    public LongitudeDataType(final double degrees) throws DegMinSecException
        {
        super(DataTypeDictionary.SIGNED_LONGITUDE, degrees);

        setDisplayFormat(DEFAULT_UNIT_LONGITUDE);
        apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        }
    }