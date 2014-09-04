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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  20-03-02    LMN created file from Java Applications book
//  01-04-02    LMN changed formatting options
//  17-04-02    LMN added sign enable, tidied up...
//  05-10-04    LMN extended DataType
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.datatypes.types;

import org.lmn.fc.common.exceptions.HourMinSecException;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.HourMinSecInterface;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;


/***************************************************************************************************
 * HourMinSecDataType.
 */

public class HourMinSecDataType extends RootDataType
                                implements HourMinSecInterface
    {
    // Internal State
    private int intHours;                   // The Hours part of the HMS (positive)
    private int intMinutes;                 // The Minutes part of the HMS
    private double dblSeconds;              // The Seconds part of the HMS, to 1msec precision
    private boolean boolPositive;           // Sign of the HMS - problems with -00:nn:nn

    // Output Formatting
    private boolean boolEnableFormatSign;   // Enables use of '+/-' on the output


    /***********************************************************************************************
     * Add a leading sign, but only if required.
     * Assume the buffer is empty on entry.
     *
     * @param hms
     * @param buffer
     */

    private static void addLeadingSign(final HourMinSecInterface hms,
                                       final StringBuffer buffer)
        {
        if ((hms != null)
            && (buffer != null))
            {
            // Are we meant to have a sign?
            if (hms.hasFormatSign())
                {
                // Beware parsing the output with parseDate()!
                if (hms.isPositive())
                    {
                    buffer.append("+");
                    }
                else
                    {
                    buffer.append("-");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Don't supply anything, and get 00:00:00.
     */

    public HourMinSecDataType()
        {
        super(DataTypeDictionary.TIME_HH_MM_SS);

        // This can never be negative
        setHours(0, true);
        setMinutes(0);
        setSeconds(0.0);

        enableFormatSign(false);
        }


    /***********************************************************************************************
     * Don't supply anything, and get 00:00:00.
     *
     * @param type
     */

    public HourMinSecDataType(final DataTypeDictionary type)
        {
        super(type);

        // This can never be negative
        setHours(0, true);
        setMinutes(0);
        setSeconds(0.0);

        enableFormatSign(false);
        }


    /***********************************************************************************************
     * Get the Time from the specified Calendar.
     *
     * @param calendar
     */

    public HourMinSecDataType(final Calendar calendar)
        {
        super(DataTypeDictionary.TIME_HH_MM_SS);

        // This can never be negative
        setHours(calendar.get(Calendar.HOUR), true);
        setMinutes(calendar.get(Calendar.MINUTE));
        setSeconds(calendar.get(Calendar.SECOND));
        addMilliseconds(calendar.get(Calendar.MILLISECOND));

        enableFormatSign(false);
        }


    /***********************************************************************************************
     * Get the Time from the specified Calendar.
     *
     * @param type
     * @param calendar
     */

    public HourMinSecDataType(final DataTypeDictionary type,
                              final Calendar calendar)
        {
        super(type);

        // This can never be negative
        setHours(calendar.get(Calendar.HOUR), true);
        setMinutes(calendar.get(Calendar.MINUTE));
        setSeconds(calendar.get(Calendar.SECOND));
        addMilliseconds(calendar.get(Calendar.MILLISECOND));

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

    public HourMinSecDataType(final boolean positive,
                              final int hours,
                              final int minutes,
                              final double seconds)
        {
        super(DataTypeDictionary.TIME_HH_MM_SS);

        // hours must never be negative
        setHours(Math.abs(hours), positive);
        setMinutes(minutes);
        setSeconds(seconds);

        enableFormatSign(true);
        }


    /***********************************************************************************************
     * Supply Sign, Hours, Minutes, Seconds separately, and get +hh:mm:ss.
     *
     * @param type
     * @param positive
     * @param hours
     * @param minutes
     * @param seconds
     */

    public HourMinSecDataType(final DataTypeDictionary type,
                              final boolean positive,
                              final int hours,
                              final int minutes,
                              final double seconds)
        {
        super(type);

        // hours must never be negative
        setHours(Math.abs(hours), positive);
        setMinutes(minutes);
        setSeconds(seconds);

        enableFormatSign(true);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +hh:mm:ss.
     *
     * @param hours
     */

    public HourMinSecDataType(final BigDecimal hours)
        {
        super(DataTypeDictionary.TIME_HH_MM_SS);

        setFromBigHours(hours);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +hh:mm:ss.
     *
     * @param type
     * @param hours
     */

    public HourMinSecDataType(final DataTypeDictionary type,
                              final BigDecimal hours)
        {
        super(type);

        setFromBigHours(hours);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +hh:mm:ss.
     *
     * @param hours
     */

    public HourMinSecDataType(final double hours)
        {
        super(DataTypeDictionary.TIME_HH_MM_SS);

        setFromBigHours(BigDecimal.valueOf(hours));
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +hh:mm:ss.
     *
     * @param type
     * @param hours
     */

    public HourMinSecDataType(final DataTypeDictionary type,
                              final double hours)
        {
        super(type);

        setFromBigHours(BigDecimal.valueOf(hours));
        }


    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Construct the HMS from Hours (which could be negative), to a precision of 1msec.
     * The specified number of hours must be in the range {00:00:00 to 23:59:59.999xxx}.
     * Use BigDecimal to guarantee no rounding errors!
     * See similar code in DegMinSec.
     *
     * @param hours
     */

    public void setFromBigHours(final BigDecimal hours)
        {
        BigDecimal bdHoursTotal;
        BigDecimal bdMinutesTotal;
        BigDecimal bdSecondsTotal;

        BigInteger biHoursValue;
        BigInteger biMinutesValue;
        BigDecimal bdSecondsValue;

        BigDecimal bdHoursRemainder;
        BigDecimal bdMinutesRemainder;

        final MathContext mathContextHours;
        final MathContext mathContextMinutes;
        final MathContext mathContextSeconds;

        // The total number of digits to return is specified by the MathContext's precision setting
        // The digit count starts from the leftmost nonzero digit of the exact result
        // The rounding mode determines how any discarded trailing digits affect the returned result
        // If zero or positive, the scale is the number of digits to the right of the decimal point
        // Assume that a precision of 1 milliarcsec is more than enough!

        // 1msec in 1 hour is about 9 decimal places (1/3600 = 0.0000002777 hour), with up to 24 hours, i.e. 11 digits
        mathContextHours = new MathContext(11, RoundingMode.HALF_UP);

        // 1msec in 1 minute is about 7 decimal places (1/60000 = 0.0000167 min), with up to 59 minutes, i.e. 9 digits
        mathContextMinutes = new MathContext(9, RoundingMode.HALF_UP);

        // 1msec in 1 second is 3 decimal places (1/1000 = 0.001 sec), with up to 59 seconds, i.e. 5 digits
        mathContextSeconds = new MathContext(5, RoundingMode.HALF_UP);

        // To allow debugging
        bdHoursTotal = BigDecimal.ZERO;
        biHoursValue = BigInteger.ZERO;
        bdHoursRemainder = BigDecimal.ZERO;

        bdMinutesTotal = BigDecimal.ZERO;
        biMinutesValue = BigInteger.ZERO;
        bdMinutesRemainder = BigDecimal.ZERO;

        bdSecondsTotal = BigDecimal.ZERO;
        bdSecondsValue = BigDecimal.ZERO;

        try
            {
            // See if we need to round to get 1msec precision
            bdSecondsTotal = hours.multiply(BigDecimal.valueOf(3600.0), mathContextHours).setScale(4, BigDecimal.ROUND_HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);

            // 1msec in 1 hour is about 9 decimal places (0.000000277 hour)
            bdHoursTotal = bdSecondsTotal.divide(BigDecimal.valueOf(3600.0), mathContextHours).setScale(10, BigDecimal.ROUND_HALF_UP).setScale(9, BigDecimal.ROUND_HALF_UP);

            // Let's assume that the above is accurate...
            biHoursValue = bdHoursTotal.toBigInteger();

            // ... and find the remainder hours to the nearest millisecond (0.0000002777 hour)
            bdHoursRemainder = bdHoursTotal.subtract(new BigDecimal(biHoursValue), mathContextHours).setScale(10, BigDecimal.ROUND_HALF_UP).setScale(9, BigDecimal.ROUND_HALF_UP);

            // Leave with bdHoursRemainder as a positive number
            bdHoursRemainder = bdHoursRemainder.abs();

            // If the remainder is 1.0000000 then we must undo the rounding of the HoursValue,
            // any less and we can use it directly
            if (bdHoursRemainder.compareTo(BigDecimal.ONE) == 0)
                {
                biHoursValue = biHoursValue.add(BigInteger.ONE);
                bdHoursRemainder = BigDecimal.ZERO;
                }

            // Now check that we didn't rollover from 23:59:59.999xxx to 24:00:00
            if (biHoursValue.compareTo(new BigInteger("24")) == 0)
                {
                biHoursValue = BigInteger.ZERO;
                bdHoursRemainder = BigDecimal.ZERO;
                }

            // Use positive numbers from here on, setting the specified sign for later use
            // Don't compare using the Value because that may be zero, and hence never negative
            setHours(biHoursValue.abs().intValue(),
                     (bdHoursTotal.signum() >= 0));

            // Calculate minutes to the nearest millisecond (0.0000167 min)
            // If zero or positive, the scale is the number of digits to the right of the decimal point
            bdMinutesTotal = bdHoursRemainder.multiply(BigDecimal.valueOf(60.0), mathContextMinutes).setScale(8, BigDecimal.ROUND_HALF_UP).setScale(7, BigDecimal.ROUND_HALF_UP);

            // Let's assume that the above is accurate...
            biMinutesValue = bdMinutesTotal.toBigInteger();

            // ... and find the remainder minutes to the nearest millisecond (0.0000167 min)
            bdMinutesRemainder = bdMinutesTotal.subtract(new BigDecimal(biMinutesValue), mathContextMinutes).setScale(8, BigDecimal.ROUND_HALF_UP).setScale(7, BigDecimal.ROUND_HALF_UP);

            // If the remainder is 1.0000000 then we must undo the rounding of the MinutesValue,
            // any less and we can use it directly
            if (bdMinutesRemainder.compareTo(BigDecimal.ONE) == 0)
                {
                biMinutesValue = biMinutesValue.add(BigInteger.ONE);
                bdSecondsValue = BigDecimal.ZERO;
                }
            else
                {
                // The original biMinutesValue value was correct
                // Calculate seconds to the nearest millisecond (0.001 sec)
                bdSecondsValue = bdMinutesRemainder.multiply(BigDecimal.valueOf(60.0), mathContextSeconds).setScale(4, BigDecimal.ROUND_HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
                }

            setMinutes(biMinutesValue.intValue());

            setSeconds(bdSecondsValue.doubleValue());
            }

        catch (ArithmeticException exception)
            {
            LOGGER.error("HourMinSecDataType.setFromBigHours() FAILED");
            LOGGER.error("HMS ArithmeticException " + exception.getMessage());
            LOGGER.error("HMS isPositive()      =" + isPositive());
            LOGGER.error("HMS                   =" + getHours() + ":" + getMinutes() + ":" + getSeconds());

            LOGGER.error("HMS bdHoursTotal      =" + bdHoursTotal.toPlainString());
            LOGGER.error("HMS bdSecondsTotal    =" + bdSecondsTotal.toPlainString());

            LOGGER.error("HMS biHoursValue      =" + biHoursValue.toString());
            LOGGER.error("HMS bdHoursRemainder  =" + bdHoursRemainder.toPlainString());
            LOGGER.error("HMS bdMinutesTotal    =" + bdMinutesTotal.toPlainString());
            LOGGER.error("HMS biMinutesValue    =" + biMinutesValue.toString());
            LOGGER.error("HMS bdMinutesRemainder=" + bdMinutesRemainder.toPlainString());
            LOGGER.error("HMS bdSecondsValue    =" + bdSecondsValue.toString());
            }

        catch (HourMinSecException exception)
            {
            LOGGER.error("HourMinSecDataType.setFromBigHours() FAILED");
            LOGGER.error("HMS HourMinSecException " + exception.getMessage());
            LOGGER.error("HMS isPositive()      =" + isPositive());
            LOGGER.error("HMS                   =" + getHours() + ":" + getMinutes() + ":" + getSeconds());

            LOGGER.error("HMS bdHoursTotal      =" + bdHoursTotal.toPlainString());
            LOGGER.error("HMS bdSecondsTotal    =" + bdSecondsTotal.toPlainString());
            LOGGER.error("HMS biHoursValue      =" + biHoursValue.toString());
            LOGGER.error("HMS bdHoursRemainder  =" + bdHoursRemainder.toPlainString());
            LOGGER.error("HMS bdMinutesTotal    =" + bdMinutesTotal.toPlainString());
            LOGGER.error("HMS biMinutesValue    =" + biMinutesValue.toString());
            LOGGER.error("HMS bdMinutesRemainder=" + bdMinutesRemainder.toPlainString());
            LOGGER.error("HMS bdSecondsValue    =" + bdSecondsValue.toString());
            }

        enableFormatSign(true);
        }


    /***********************************************************************************************
     * Set the state of the Sign.
     *
     * @param positive
     */

    public void setPositive(final boolean positive)
        {
        this.boolPositive = positive;
        }


    /***********************************************************************************************
     * Get the state of the Sign.
     *
     * @return boolean
     */

    public boolean isPositive()
        {
        // We could return +1 or -1, but a boolean seems more logical...
        return (this.boolPositive);
        }


    /***********************************************************************************************
     * Set the Hours value.
     * The sign must be supplied also, to avoid problems if Hour=0.
     *
     * @param hours
     * @param positive
     */

    public void setHours(final int hours,
                         final boolean positive)
        {
        // hours must never be negative
        this.intHours = Math.abs(hours);
        setPositive(positive);
        }


    /***********************************************************************************************
     * Get the Hours value.
     * This still fails if intHours=0 and the sign isn't read at the same time.
     *
     * @return int
     */

    public int getHours()
        {
        return (this.intHours);
        }


    /***********************************************************************************************
     * Set the Minutes value.
     *
     * @param minutes
     *
     * @throws HourMinSecException
     */

    public void setMinutes(final int minutes) throws HourMinSecException
        {
        if ((minutes < 0) || (minutes >= 60))
            {
            throw new HourMinSecException(EXCEPTION_OUTOFRANGE + " [minutes=" + minutes + "]");
            }

        // minutes must never be negative
        this.intMinutes = Math.abs(minutes);
        }


    /***********************************************************************************************
     * Get the Minutes value.
     *
     * @return
     */

    public int getMinutes()
        {
        return (this.intMinutes);
        }


    /***********************************************************************************************
     * Set the Seconds value.
     *
     * @param seconds
     *
     * @throws HourMinSecException
     */

    public void setSeconds(final double seconds) throws HourMinSecException
        {
        if ((seconds < 0.0) || (seconds >= 60.0))
            {
            throw new HourMinSecException(EXCEPTION_OUTOFRANGE + " [seconds=" + seconds + "]");
            }

        // seconds must never be negative
        this.dblSeconds = Math.abs(seconds);
        }


    /***********************************************************************************************
     * Get the Seconds value.
     *
     * @return double
     */

    public double getSeconds()
        {
        return (this.dblSeconds);
        }


    /***********************************************************************************************
     * A convenience method to simplify the constructors which use a Calendar.
     *
     * @param millis
     *
     * @throws HourMinSecException
     */

    private void addMilliseconds(final int millis) throws HourMinSecException
        {
        if ((millis < -999) || (millis > 999))
            {
            throw new HourMinSecException(EXCEPTION_OUTOFRANGE + " [millis=" + millis + "]");
            }

        setSeconds(getSeconds() + ((double)millis / 1000.0));
        }


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Indicate if this HMS DataType has the sign preceding the value.
     *
     * @return boolean
     */

    public boolean hasFormatSign()
        {
        return (this.boolEnableFormatSign);
        }


    /***********************************************************************************************
     * Control the use of a sign (+/-) on the formatted output.
     *
     * @param sign
     */

    public void enableFormatSign(final boolean sign)
        {
        this.boolEnableFormatSign = sign;
        }


    /***********************************************************************************************
     * Return the HourMinSecDataType as a signed double value.
     *
     * @return double
     */

    public double toDouble()
        {
        final double dblHMS;

        dblHMS = (double) getHours() + ((double) getMinutes() / 60.0) + (getSeconds() / 3600.0);

        if (isPositive())
            {
            return (dblHMS);
            }
        else
            {
            return (-dblHMS);
            }
        }


    /***********************************************************************************************
     * Return the HourMinSecDataType as a formatted, delimited String.
     * Note that seconds are rounded to the nearest integer,
     * so internally a time may be 12:34:56.995, but it will display as 12:34:57.
     * Primarily intended for reports and display.
     *
     * @return String
     */

    public String toString_HH_MM_SS()
        {
        final double dblAccumulatedSeconds;
        final BigDecimal bdAccumulatedSeconds;
        final BigDecimal bdRoundedSeconds;

        final BigDecimal[] arrayDivideForHoursAndRemainder;
        final BigDecimal bdHoursRemainderAsSeconds;
        final BigInteger biHoursValue;

        final BigDecimal[] arrayDivideForMinutesAndRemainder;
        final BigDecimal bdMinutesRemainderAsSeconds;
        final BigInteger biMinutesValue;

        final BigInteger biSecondsValue;
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Construct BigDecimal via toString() for reliability,
        // so form a (positive) double initially...
        dblAccumulatedSeconds = (getHours() * 3600.0) + (getMinutes() * 60.0) + getSeconds();

        // Re-round accumulated Hours, Minutes and Seconds to nearest Second
        bdAccumulatedSeconds = new BigDecimal(Double.toString(dblAccumulatedSeconds));
        bdRoundedSeconds = bdAccumulatedSeconds.setScale(0, RoundingMode.HALF_UP);

        arrayDivideForHoursAndRemainder = bdRoundedSeconds.divideAndRemainder(BigDecimal.valueOf(3600.0));
        biHoursValue = arrayDivideForHoursAndRemainder[0].toBigInteger();
        bdHoursRemainderAsSeconds = arrayDivideForHoursAndRemainder[1];

        arrayDivideForMinutesAndRemainder = bdHoursRemainderAsSeconds.divideAndRemainder(BigDecimal.valueOf(60.0));
        biMinutesValue = arrayDivideForMinutesAndRemainder[0].toBigInteger();
        bdMinutesRemainderAsSeconds = arrayDivideForMinutesAndRemainder[1];

        // Take the rounded seconds only
        biSecondsValue = bdMinutesRemainderAsSeconds.remainder(BigDecimal.valueOf(60.0)).toBigInteger();

        // Do we need a leading sign character?
        addLeadingSign(this, buffer);

        // Create a formatted HH:MM:SS string, with integer Hours, Minutes and Seconds
        buffer.append(DecimalFormatPattern.HOURS.format(biHoursValue.intValue()));
        buffer.append(DecimalFormatPattern.HOURS.getDelimiter());
        buffer.append(DecimalFormatPattern.MINUTES.format(biMinutesValue.intValue()));
        buffer.append(DecimalFormatPattern.MINUTES.getDelimiter());
        buffer.append(DecimalFormatPattern.SECONDS_S.format(biSecondsValue.intValue()));
        buffer.append(DecimalFormatPattern.SECONDS_S.getDelimiter());

//        LOGGER.error("HMS2 ============================================");
//        System.out.println("HMS2 HH:MM:SS.sss               =" + toString());
//        System.out.println("HMS2 HH:MM:SS                   =" + buffer.toString());
//        System.out.println("HMS2 isPositive()               =" + isPositive());
//        System.out.println("HMS2 hasFormatSign()            =" + hasFormatSign());

//        System.out.println("HMS2 dblAccumulatedSeconds      =" + dblAccumulatedSeconds);
//        System.out.println("HMS2 bdAccumulatedSeconds       =" + bdAccumulatedSeconds.toPlainString());
//        System.out.println("HMS2 bdRoundedSeconds           =" + bdRoundedSeconds.toPlainString());
//
//        System.out.println("HMS2 intHours                   =" + getHours());
//        System.out.println("HMS2 biHoursValue               =" + biHoursValue.toString());
//        System.out.println("HMS2 bdHoursRemainderAsSeconds  =" + bdHoursRemainderAsSeconds.toPlainString());
//
//        System.out.println("HMS2 intMinutes                 =" + getMinutes());
//        System.out.println("HMS2 biMinutesValue             =" + biMinutesValue.toString());
//        System.out.println("HMS2 bdMinutesRemainderAsSeconds=" + bdMinutesRemainderAsSeconds.toPlainString());
//
//        System.out.println("HMS2 dblSeconds                 =" + getSeconds());
//        System.out.println("HMS2 biSecondsValue             =" + biSecondsValue.toString());

        return  (buffer.toString());
        }


    /***********************************************************************************************
     * Return the HourMinSecDataType as a formatted, delimited String.
     * Note that seconds may be formatted with a fractional part.
     * WARNING! You may find that e.g. 00:01:59.9995 is produced where 00:02:00 was intended.
     * Read about rounding problems, BigDecimal, BigInteger and so on.
     *
     * @return String
     */

    public String toString()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Do we need a leading sign character?
        addLeadingSign(this, buffer);

        // Create a formatted HH:MM:SS.sss string
        buffer.append(DecimalFormatPattern.HOURS.format(getHours()));
        buffer.append(DecimalFormatPattern.HOURS.getDelimiter());
        buffer.append(DecimalFormatPattern.MINUTES.format(getMinutes()));
        buffer.append(DecimalFormatPattern.MINUTES.getDelimiter());
        buffer.append(DecimalFormatPattern.SECONDS_MS.format(getSeconds()));
        buffer.append(DecimalFormatPattern.SECONDS_MS.getDelimiter());

        return  (buffer.toString());
        }


    /***********************************************************************************************
     * Indicate if this HMS instance is after the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    public boolean after(final HourMinSecInterface hmswhen)
        {
        final boolean boolAfter;

        boolAfter = (toDouble() > hmswhen.toDouble());

        return (boolAfter);
        }


    /***********************************************************************************************
     * Indicate if this HMS instance is equal to or after the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    public boolean equalOrAfter(final HourMinSecInterface hmswhen)
        {
        final boolean boolEqualOrAfter;

        boolEqualOrAfter = (toDouble() >= hmswhen.toDouble());

        return (boolEqualOrAfter);
        }


    /***********************************************************************************************
     * Indicate if this HMS instance is before the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    public boolean before(final HourMinSecInterface hmswhen)
        {
        final boolean boolBefore;

        boolBefore = (toDouble() < hmswhen.toDouble());

        return (boolBefore);
        }


    /***********************************************************************************************
     * Indicate if this HMS instance is before or equal to the specified HMS value.
     * Note that the HMS values may be negative!
     *
     * @param hmswhen
     *
     * @return boolean
     */

    public boolean beforeOrEqual(final HourMinSecInterface hmswhen)
        {
        final boolean boolBeforeOrEqual;

        boolBeforeOrEqual = (toDouble() <= hmswhen.toDouble());

        return (boolBeforeOrEqual);
        }
    }
