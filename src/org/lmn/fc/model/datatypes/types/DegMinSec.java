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
//  02-04-02    LMN changed formatting options
//  17-04-02    LMN added sign enable, tidied up...
//  05-10-04    LMN extended DataType
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.datatypes.types;


import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;


/***************************************************************************************************
 * DegMinSec.
 */

public class DegMinSec extends RootDataType
                       implements DegMinSecInterface
    {
    // Internal State
    private boolean boolPositive;           // Sign of the DMS - problems with -00:nn:nn
    private int intDeg;                     // The Degrees part of the DMS (positive)
    private int intMin;                     // The Minutes part of the DMS
    private double dblSec;                  // The Seconds part of the DMS

    // Output Formatting
    private DegMinSecFormat dmsFormat;      // Enables use of '+/-' or "E/W, N/S" on the output

    private DecimalFormatPattern patternDegrees;
    private DecimalFormatPattern patternMinutes;
    private DecimalFormatPattern patternSeconds;


    /***********************************************************************************************
     * Indicate if the two specified DegMinSec appear to be at or near the same angle or location.
     * Compare {Deg Min Sec} but not the decimal fractions of Seconds.
     *
     * @param dms0
     * @param dms1
     *
     * @return boolean
     */

    public static boolean dmsAreVeryClose(final DegMinSecInterface dms0,
                                          final DegMinSecInterface dms1)
        {
        final boolean boolAreVeryClose;

        boolAreVeryClose = ((dms0 != null)
                            && (dms1 != null)
                            && (dms0.isPositive() == dms1.isPositive())
                            && (dms0.getDegrees() == dms1.getDegrees())
                            && (dms0.getMinutes() == dms1.getMinutes())
                            && (Math.abs(dms0.getSeconds() - dms1.getSeconds()) <= 1.0));

        return (boolAreVeryClose);
        }


    /***********************************************************************************************
     * Add a leading sign or trailing E/W, N/S as required.
     *
     * @param dms
     * @param buffer
     */

    protected static void addSign(final DegMinSecInterface dms,
                                  final StringBuffer buffer)
        {
        if ((dms != null)
            && (dms.getDisplayFormat() != null)
            && (buffer != null))
            {
            switch (dms.getDisplayFormat())
                {
                case SIGN:
                    {
                    // Beware parsing the output with parseDate()!
                    // Signs come at the start
                    if (dms.isPositive())
                        {
                        buffer.insert(0, "+");
                        }
                    else
                        {
                        buffer.insert(0, "-");
                        }
                    break;
                    }

                case EW:
                    {
                    // Longitude is POSITIVE to the WEST
                    // EW comes at the end
                    if (dms.isPositive())
                        {
                        buffer.append("W");
                        }
                    else
                        {
                        buffer.append("E");
                        }
                    break;
                    }

                case NS:
                    {
                    // Latitude is POSITIVE to the NORTH
                    // NS comes at the end
                    if (dms.isPositive())
                        {
                        buffer.append("N");
                        }
                    else
                        {
                        buffer.append("S");
                        }
                    break;
                    }

                default:
                    {
                    // Beware the loss of the sign!
                    }
                }
            }
        }


    /***********************************************************************************************
     * Don't supply anything, and get +00:00:00.
     *
     * @param type
     *
     * @throws DegMinSecException
     */

    public DegMinSec(final DataTypeDictionary type) throws DegMinSecException
        {
        super(type);

        // This can never be negative
        setDegrees(0, true);
        setMinutes(0);
        setSeconds(0.0);

        // Set default format for output
        setDisplayFormat(DegMinSecFormat.NONE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Supply Sign, Degrees, Minutes, Seconds separately, and get +dd:mm:ss.
     *
     * @param type
     * @param positive
     * @param degrees
     * @param minutes
     * @param seconds
     *
     * @throws DegMinSecException
     */

    public DegMinSec(final DataTypeDictionary type,
                     final boolean positive,
                     final int degrees,
                     final int minutes,
                     final double seconds) throws DegMinSecException
        {
        super(type);

        // degrees must never be negative
        setDegrees(Math.abs(degrees), positive);
        setMinutes(minutes);
        setSeconds(seconds);

        // Set default format for output
        setDisplayFormat(DegMinSecFormat.NONE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +dd:mm:ss.
     *
     * @param type
     * @param degrees
     */

    public DegMinSec(final DataTypeDictionary type,
                     final BigDecimal degrees) throws DegMinSecException
        {
        super(type);

        setFromBigDegrees(degrees);

        // Set default format for output
        setDisplayFormat(DegMinSecFormat.NONE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /***********************************************************************************************
     * Supply a signed angle, and get +dd:mm:ss.
     * This should be avoided - use the constructor with BigDecimal instead.
     *
     * @param type
     * @param degrees
     *
     * @throws DegMinSecException
     */

    public DegMinSec(final DataTypeDictionary type,
                     final double degrees) throws DegMinSecException
        {
        super(type);

        setFromBigDegrees(BigDecimal.valueOf(degrees));

        // Set default format for output
        setDisplayFormat(DegMinSecFormat.NONE);
        apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        }


    /**********************************************************************************************/
    /* Internal State                                                                             */
    /***********************************************************************************************
     * Set the Value of the DegMinSec from the specified Degrees as a BigDecimal (which could be negative).
     * Used when the internal configuration should be maintained, e.g. formats.
     * The specified number of hours must be in the range {000:00:00 to xxx:59:59.999xxx}.
     * Do not change the DisplayFormat (sign, E/W, N/S).
     * See similar code in HourMinMinSecDataType.
     *
     * @param degrees
     */

    public void setFromBigDegrees(final BigDecimal degrees)
        {
        BigDecimal bdDegreesTotal;
        BigDecimal bdArcMinutesTotal;
        BigDecimal bdArcSecondsTotal;

        BigInteger biDegreesValue;
        BigInteger biArcMinutesValue;
        BigDecimal bdArcSecondsValue;

        BigDecimal bdDegreesRemainder;
        BigDecimal bdArcMinutesRemainder;

        final MathContext mathContextArcDegrees;
        final MathContext mathContextArcMinutes;
        final MathContext mathContextArcSeconds;

        // The total number of digits to return is specified by the MathContext's precision setting
        // The digit count starts from the leftmost nonzero digit of the exact result
        // The rounding mode determines how any discarded trailing digits affect the returned result
        // If zero or positive, the scale is the number of digits to the right of the decimal point
        // Assume that a precision of 1 milliarcsec is more than enough!

        // 1 milliarcsec in 1 degree is about 9 decimal places (1/3600 = 0.000002777 hour), with up to 359 degrees, i.e. 12 digits
        mathContextArcDegrees = new MathContext(12, RoundingMode.HALF_UP);

        // 1 milliarcsec in 1 arcminute is about 7 decimal places (1/60000 = 0.0000167 min), with up to 59 minutes, i.e. 9 digits
        mathContextArcMinutes = new MathContext(9, RoundingMode.HALF_UP);

        // 1 milliarcsec in 1 arcsecond is 3 decimal places (1/1000 = 0.001 sec), with up to 59 seconds, i.e. 5 digits
        mathContextArcSeconds = new MathContext(5, RoundingMode.HALF_UP);

        // To allow debugging
        bdDegreesTotal = BigDecimal.ZERO;
        biDegreesValue = BigInteger.ZERO;
        bdDegreesRemainder = BigDecimal.ZERO;

        bdArcMinutesTotal = BigDecimal.ZERO;
        biArcMinutesValue = BigInteger.ZERO;
        bdArcMinutesRemainder = BigDecimal.ZERO;

        bdArcSecondsTotal = BigDecimal.ZERO;
        bdArcSecondsValue = BigDecimal.ZERO;

        try
            {
            // See if we need to round to get milliarcsec precision
            bdArcSecondsTotal = degrees.multiply(BigDecimal.valueOf(3600.0), mathContextArcDegrees).setScale(4, BigDecimal.ROUND_HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);

            // 1 milliarcsec in 1 degree is about 9 decimal places (1/3600 = 0.000002777 hour)
            bdDegreesTotal = bdArcSecondsTotal.divide(BigDecimal.valueOf(3600.0), mathContextArcDegrees).setScale(10, BigDecimal.ROUND_HALF_UP).setScale(9, BigDecimal.ROUND_HALF_UP);

            // Let's assume that the above is accurate...
            biDegreesValue = bdDegreesTotal.toBigInteger();

            // ... and find the remainder degrees to the nearest milliarcsecond (0.0000002777 hour)
            bdDegreesRemainder = bdDegreesTotal.subtract(new BigDecimal(biDegreesValue), mathContextArcDegrees).setScale(10, BigDecimal.ROUND_HALF_UP).setScale(9, BigDecimal.ROUND_HALF_UP);

            // Leave with bdDegreesRemainder as a positive number
            bdDegreesRemainder = bdDegreesRemainder.abs();

            // If the remainder is 1.0000000 then we must undo the rounding of the DegreesValue,
            // any less and we can use it directly
            if (bdDegreesRemainder.compareTo(BigDecimal.ONE) == 0)
                {
                biDegreesValue = biDegreesValue.add(BigInteger.ONE);
                bdDegreesRemainder = BigDecimal.ZERO;
                }

            // Now check that we didn't rollover from 359:59:59.999xxx to 360:00:00
            if (biDegreesValue.compareTo(new BigInteger("360")) == 0)
                {
                biDegreesValue = BigInteger.ZERO;
                bdDegreesRemainder = BigDecimal.ZERO;
                }

            // Use positive numbers from here on, setting the specified sign for later use
            // Note that longitudes on the Greenwich meridian are regarded as NEGATIVE (i.e. EAST)
            // and the Equator is regarded as SOUTH
            // Don't compare using the Value because that may be zero, and hence never negative
            setDegrees(biDegreesValue.abs().intValue(),
                       (bdDegreesTotal.signum() >= 0));

            // Calculate minutes to the nearest millisecond (0.0000167 min)
            // If zero or positive, the scale is the number of digits to the right of the decimal point
            bdArcMinutesTotal = bdDegreesRemainder.multiply(BigDecimal.valueOf(60.0), mathContextArcMinutes).setScale(8, BigDecimal.ROUND_HALF_UP).setScale(7, BigDecimal.ROUND_HALF_UP);

            // Let's assume that the above is accurate...
            biArcMinutesValue = bdArcMinutesTotal.toBigInteger();

            // ... and find the remainder arcminutes to the nearest milliarcsecond (0.0000167 min)
            bdArcMinutesRemainder = bdArcMinutesTotal.subtract(new BigDecimal(biArcMinutesValue), mathContextArcMinutes).setScale(8, BigDecimal.ROUND_HALF_UP).setScale(7, BigDecimal.ROUND_HALF_UP);

            // If the remainder is 1.0000000 then we must undo the rounding of the MinutesValue,
            // any less and we can use it directly
            if (bdArcMinutesRemainder.compareTo(BigDecimal.ONE) == 0)
                {
                biArcMinutesValue = biArcMinutesValue.add(BigInteger.ONE);
                bdArcSecondsValue = BigDecimal.ZERO;
                }
            else
                {
                // The original biMinutesValue value was correct
                // Calculate arcseconds to the nearest milliarcsecond (0.001 sec)
                bdArcSecondsValue = bdArcMinutesRemainder.multiply(BigDecimal.valueOf(60.0), mathContextArcSeconds).setScale(4, BigDecimal.ROUND_HALF_UP).setScale(3, BigDecimal.ROUND_HALF_UP);
                }

            setMinutes(biArcMinutesValue.intValue());

            setSeconds(bdArcSecondsValue.doubleValue());
            }

        catch (ArithmeticException exception)
            {
            // BEWARE! Using debugs which rely on formatters (e.g. toString()) may cause problems if the type
            // hasn't completed initialisation in the contructor
            LOGGER.error("DegMinSec.setFromBigDegrees() FAILED");
            LOGGER.error("HMS ArithmeticException " + exception.getMessage());
            LOGGER.error("DMS isPositive()            =" + isPositive());
            LOGGER.error("DMS                         =" + getDegrees() + ":" + getMinutes() + ":" + getSeconds());

            LOGGER.error("DMS bdDegreesTotal          =" + bdDegreesTotal.toPlainString());
            LOGGER.error("HMS bdSecondsTotal          =" + bdArcSecondsTotal.toPlainString());

            LOGGER.error("DMS biDegreesValue          =" + biDegreesValue.toString());
            LOGGER.error("DMS bdDegreesRemainder      =" + bdDegreesRemainder.toPlainString());
            LOGGER.error("DMS bdMinutesTotal          =" + bdArcMinutesTotal.toPlainString());
            LOGGER.error("DMS biMinutesValue          =" + biArcMinutesValue.toString());
            LOGGER.error("DMS bdMinutesRemainder      =" + bdArcMinutesRemainder.toPlainString());
            LOGGER.error("DMS bdSecondsValue          =" + bdArcSecondsValue.toString());
            }

        catch (DegMinSecException exception)
            {
            LOGGER.error("DegMinSec.setFromBigDegrees() FAILED");
            LOGGER.error("HMS DegMinSecException " + exception.getMessage());
            LOGGER.error("DMS isPositive()            =" + isPositive());
            LOGGER.error("DMS                         =" + getDegrees() + ":" + getMinutes() + ":" + getSeconds());

            LOGGER.error("DMS bdDegreesTotal          =" + bdDegreesTotal.toPlainString());
            LOGGER.error("HMS bdSecondsTotal          =" + bdArcSecondsTotal.toPlainString());

            LOGGER.error("DMS biDegreesValue          =" + biDegreesValue.toString());
            LOGGER.error("DMS bdDegreesRemainder      =" + bdDegreesRemainder.toPlainString());
            LOGGER.error("DMS bdMinutesTotal          =" + bdArcMinutesTotal.toPlainString());
            LOGGER.error("DMS biMinutesValue          =" + biArcMinutesValue.toString());
            LOGGER.error("DMS bdMinutesRemainder      =" + bdArcMinutesRemainder.toPlainString());
            LOGGER.error("DMS bdSecondsValue          =" + bdArcSecondsValue.toString());
            }
        }


    /***********************************************************************************************
     * Get the Sign.
     *
     * @return boolean
     */

    public boolean isPositive()
        {
        // We could return +1 or -1, but a boolean seems more logical...
        return (this.boolPositive);
        }


    /***********************************************************************************************
     * Set the Sign.
     *
     * @param positive
     */

    public void setPositive(final boolean positive)
        {
        this.boolPositive = positive;
        }


    /***********************************************************************************************
     * Get the Degrees value.
     * Note that this still fails if Degrees=0 and the sign isn't read at the same time.
     *
     * @return
     */

    public int getDegrees()
        {
        return (this.intDeg);
        }


    /***********************************************************************************************
     * Set the Degrees value.
     * The sign must be supplied also, to avoid problems if Degrees=0.
     *
     * @param degrees
     * @param positive
     */

    public void setDegrees(final int degrees,
                           final boolean positive)
        {
        // degrees must never be negative
        this.intDeg = Math.abs(degrees);
        setPositive(positive);
        }


    /***********************************************************************************************
     * Get the Minutes value.
     *
     * @return int
     */

    public int getMinutes()
        {
        return (this.intMin);
        }


    /***********************************************************************************************
     * Set the Minutes value {0...59}.
     *
     * @param minutes
     *
     * @throws DegMinSecException
     */

    public void setMinutes(final int minutes) throws DegMinSecException
        {
        if ((minutes < 0) || (minutes >= 60))
            {
            throw new DegMinSecException(EXCEPTION_OUTOFRANGE + " [minutes=" + minutes + "]");
            }

        // minutes must never be negative
        this.intMin = Math.abs(minutes);
        }


    /***********************************************************************************************
     * Get the Seconds value.
     *
     * @return double
     */

    public double getSeconds()
        {
        return (this.dblSec);
        }


    /***********************************************************************************************
     * Set the Seconds value {0.0...59.999}.
     *
     * @param seconds
     *
     * @throws DegMinSecException
     */

    public void setSeconds(final double seconds) throws DegMinSecException
        {
        if ((seconds < 0.0) || (seconds >= 60.0))
            {
            throw new DegMinSecException(EXCEPTION_OUTOFRANGE + " [seconds=" + seconds + "]");
            }

        // seconds must never be negative
        this.dblSec = Math.abs(seconds);
        }


    /**********************************************************************************************/
    /* Output Formatting                                                                          */
    /***********************************************************************************************
     * Get the formatting Unit, i.e. signed or compass points.
     *
     * @return DegMinSecFormat
     */

    public final DegMinSecFormat getDisplayFormat()
        {
        return (this.dmsFormat);
        }


    /***********************************************************************************************
     * Control the use of a sign (+/-), (E/W), (N/S) on the formatted output.
     *
     * @param dmsformat
     */

    public void setDisplayFormat(final DegMinSecFormat dmsformat)
        {
        this.dmsFormat = dmsformat;
        }


    /***********************************************************************************************
     * Apply the default pattern for 360 degree range.
     * The sign or E/W/N/S is handled with setDisplayFormat().
     *
     * @param secondspattern
     */

    public void apply360DegreeSecondsPattern(final DecimalFormatPattern secondspattern)
        {
        this.patternDegrees = DecimalFormatPattern.DEGREES_360;
        this.patternMinutes = DecimalFormatPattern.MINUTES;
        this.patternSeconds = secondspattern;
        }


    /***********************************************************************************************
     * Apply the default pattern for +/-90 degree range.
     * The sign or E/W/N/S is handled with setDisplayFormat().
     *
     * @param secondspattern
     */

    public void apply90DegreeSecondsPattern(final DecimalFormatPattern secondspattern)
        {
        this.patternDegrees = DecimalFormatPattern.DEGREES_90;
        this.patternMinutes = DecimalFormatPattern.MINUTES;
        this.patternSeconds = secondspattern;
        }


    /***********************************************************************************************
     * Return the DegMinSec as a signed double value.
     *
     * @return double
     */

    public double toDouble()
        {
        final double dblDMS;

        dblDMS = (double) getDegrees() + ((double) getMinutes() / 60.0) + (getSeconds() / 3600.0);

        if (isPositive())
            {
            return (dblDMS);
            }
        else
            {
            return (-dblDMS);
            }
        }


    /***********************************************************************************************
     * Return the DegMinSecDataType as a formatted, delimited String, assuming 360 degrees range.
     * Note that seconds are rounded to the nearest integer,
     * so internally degrees may be 012:34:56.995, but it will display as 012:34:57.
     * Primarily intended for reports and display.
     *
     * @return String
     */

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

        // Create a formatted DDD:MM:SS string, with integer Degrees, Minutes and Seconds
        buffer.append(DecimalFormatPattern.DEGREES_360.format(biDegreesValue.intValue()));
        buffer.append(DecimalFormatPattern.DEGREES_360.getDelimiter());
        buffer.append(DecimalFormatPattern.MINUTES.format(biMinutesValue.intValue()));
        buffer.append(DecimalFormatPattern.MINUTES.getDelimiter());
        buffer.append(DecimalFormatPattern.SECONDS_S.format(biSecondsValue.intValue()));
        buffer.append(DecimalFormatPattern.SECONDS_S.getDelimiter());

        // Do we need a sign character?
        addSign(this, buffer);

//        System.out.println("DMS ============================================");
//        System.out.println("DMS DDD:MM:SS.sss               =" + toString());
//        System.out.println("DMS DDD:MM:SS                   =" + buffer.toString());
//
//        System.out.println("DMS dblAccumulatedSeconds       =" + dblAccumulatedSeconds);
//        System.out.println("DMS bdAccumulatedSeconds        =" + bdAccumulatedSeconds.toPlainString());
//        System.out.println("DMS bdRoundedSeconds            =" + bdRoundedSeconds.toPlainString());
//
//        System.out.println("DMS isPositive                  =" + isPositive());
//
//        System.out.println("DMS intDegrees                  =" + getDegrees());
//        System.out.println("DMS biDegreesValue              =" + biDegreesValue.toString());
//        System.out.println("DMS bdDegreesRemainderAsSeconds =" + bdDegreesRemainderAsSeconds.toPlainString());
//
//        System.out.println("DMS intMinutes                  =" + getMinutes());
//        System.out.println("DMS biMinutesValue              =" + biMinutesValue.toString());
//        System.out.println("DMS bdMinutesRemainderAsSeconds =" + bdMinutesRemainderAsSeconds.toPlainString());
//
//        System.out.println("DMS dblSeconds                  =" + getSeconds());
//        System.out.println("DMS biSecondsValue              =" + biSecondsValue.toString());

        return  (buffer.toString());
        }


    /***********************************************************************************************
     * Return the DegMinSec as a formatted, delimited String.
     * Note that seconds may be formatted with a fractional part.
     * Add a sign indicator '+/-', 'E/W', 'N/S' if required.
     * WARNING! You may find that e.g. 00:01:59.9995 is produced where 00:02:00 was intended.
     * Read about rounding problems, BigDecimal, BigInteger and so on.
     *
     * @return String
     */

    public String toString()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Create a formatted DD:MM:SS.sss string
        // using the current formatters
        buffer.append(this.patternDegrees.format(getDegrees()));
        buffer.append(this.patternDegrees.getDelimiter());
        buffer.append(this.patternMinutes.format(getMinutes()));
        buffer.append(this.patternMinutes.getDelimiter());
        buffer.append(this.patternSeconds.format(getSeconds()));
        buffer.append(this.patternSeconds.getDelimiter());

        addSign(this, buffer);

        return  (buffer.toString());
        }
    }
