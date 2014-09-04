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
import java.math.BigInteger;
import java.math.RoundingMode;


/***************************************************************************************************
 * LatitudeDataType.
 */

public final class LatitudeDataType extends DegMinSec
                                    implements DegMinSecInterface
    {
    private static final DegMinSecFormat DEFAULT_UNIT_LATITUDE = DegMinSecFormat.SIGN;


    /***********************************************************************************************
     * LatitudeDataType.
     */

    public LatitudeDataType()
        {
        super(DataTypeDictionary.LATITUDE);

        setDisplayFormat(DEFAULT_UNIT_LATITUDE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Supply Sign, Degrees, Minutes, Seconds separately, and get +dd:mm:ss.s.
     *
     * @param positive
     * @param degrees
     * @param minutes
     * @param seconds
     *
     * @throws DegMinSecException
     */

    public LatitudeDataType(final boolean positive,
                            final int degrees,
                            final int minutes,
                            final double seconds) throws DegMinSecException
        {
        super(DataTypeDictionary.LATITUDE,
              positive,
              degrees,
              minutes,
              seconds);

        setDisplayFormat(DEFAULT_UNIT_LATITUDE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +dd:mm:ss.s.
     *
     * @param degrees
     *
     * @throws DegMinSecException
     */

    public LatitudeDataType(final BigDecimal degrees) throws DegMinSecException
        {
        super(DataTypeDictionary.LATITUDE, degrees);

        setDisplayFormat(DEFAULT_UNIT_LATITUDE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +dd:mm:ss.s.
     * This should be avoided - use the constructor with BigDecimal instead.
     *
     * @param degrees
     *
     * @throws DegMinSecException
     */

    public LatitudeDataType(final double degrees) throws DegMinSecException
        {
        super(DataTypeDictionary.LATITUDE, degrees);

        setDisplayFormat(DEFAULT_UNIT_LATITUDE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Return the LatitudeDataType as a formatted, delimited String, assuming 90 degrees range,
     * unlike the DegMinSecDataType superclass, which assumes 360 degrees.
     * Note that seconds are rounded to the nearest integer,
     * so internally latitude may be 12:34:56.995, but it will display as 012:34:57.
     * Primarily intended for reports and display.
     *
     * @return String
     */
    @Override
    public String toString_DDD_MM_SS()
        {
        final double dblAccumulatedSeconds;
        final BigDecimal bdAccumulatedSeconds;
        final BigDecimal bdRoundedSeconds;

        final BigDecimal[] arrayDivideForDegreesAndRemainder;
        final BigDecimal bdDegreesRemainderAsSeconds;
        final BigInteger biDegreesValue;

        final BigDecimal[] arrayDivideForMinutesAndRemainder;
        final BigDecimal bdMinutesRemainderAsSeconds;
        final BigInteger biMinutesValue;

        final BigInteger biSecondsValue;
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Construct BigDecimal via toString() for reliability,
        // so form a double initially...
        dblAccumulatedSeconds = (getDegrees() * 3600.0) + (getMinutes() * 60.0) + getSeconds();

        // Re-round accumulated Degrees, Minutes and Seconds to nearest Second
        bdAccumulatedSeconds = new BigDecimal(Double.toString(dblAccumulatedSeconds));
        bdRoundedSeconds = bdAccumulatedSeconds.setScale(0, RoundingMode.HALF_UP);

        arrayDivideForDegreesAndRemainder = bdRoundedSeconds.divideAndRemainder(BigDecimal.valueOf(3600.0));
        biDegreesValue = arrayDivideForDegreesAndRemainder[0].toBigInteger();
        bdDegreesRemainderAsSeconds = arrayDivideForDegreesAndRemainder[1];

        arrayDivideForMinutesAndRemainder = bdDegreesRemainderAsSeconds.divideAndRemainder(BigDecimal.valueOf(60.0));
        biMinutesValue = arrayDivideForMinutesAndRemainder[0].toBigInteger();
        bdMinutesRemainderAsSeconds = arrayDivideForMinutesAndRemainder[1];

        // Take the rounded seconds only
        biSecondsValue = bdMinutesRemainderAsSeconds.remainder(BigDecimal.valueOf(60.0)).toBigInteger();

        // Create a formatted DD:MM:SS string, with integer Degrees, Minutes and Seconds
        buffer.append(DecimalFormatPattern.DEGREES_90.format(biDegreesValue.intValue()));
        buffer.append(DecimalFormatPattern.DEGREES_90.getDelimiter());
        buffer.append(DecimalFormatPattern.MINUTES.format(biMinutesValue.intValue()));
        buffer.append(DecimalFormatPattern.MINUTES.getDelimiter());
        buffer.append(DecimalFormatPattern.SECONDS_S.format(biSecondsValue.intValue()));
        buffer.append(DecimalFormatPattern.SECONDS_S.getDelimiter());

        // Do we need a sign character?
        addSign(this, buffer);

        return  (buffer.toString());
        }
    }
