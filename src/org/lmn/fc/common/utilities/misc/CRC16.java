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

package org.lmn.fc.common.utilities.misc;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * Calculating 16-bit CRC
 *
 * http://en.wikipedia.org/wiki/Cyclic_redundancy_check
 *
 * http://zorc.breitbandkatze.de/crc.html
 */

public final class CRC16 implements FrameworkConstants,
                                    FrameworkStrings,
                                    FrameworkMetadata,
                                    FrameworkSingletons
    {
    private static final int POLYNOMIAL_1021 = 0x1021;
    private static final int POLYNOMIAL_8408 = 0x8408; // 0x8408 used in European X.25
    private static final int POLYNOMIAL_ORDER = 16;
    private static final int INITIAL_VALUE_DIRECT = 0xffff;
    private static final int FINAL_XOR_VALUE = 0;
    private static final int CRC_MASK = 0xffff;
    private static final int CRC_HIGH_BIT = 0x8000;

    // Scrambler lookup table for fast computation
    private static int[] crcTable8408 = new int[256];


    /***********************************************************************************************
     * x16 + x12 + x5 + 1 generator polynomial
     *
     * x16 = 1
     *
     * 1111 1100 0000 0000
     * 5432 1098 7654 3211
     *
     * 1111 1111 1111 1111
     *
     * 1021...............
     *
     * 0001
     *      0000
     *           0010
     *                0001
     *
     * 8408...............
     *
     * 1000
     *      0100
     *           0000
     *                1000
     */

    // ~a       Inverts the bits

    // a & b    AND     1 if both bits are 1

    // a | b    OR      1 if either bit is 1

    // a ^ b    XOR     1 if both bits are different

    // n << p   Shifts the bits of n left p positions.
    //          Zero bits are shifted into the low-order positions

    // n >> p   Shifts the bits of n right p positions. If n is a 2's complement signed number,
    //          the sign bit is shifted into the high-order positions.

    // n >>> p  Shifts the bits of n right p positions.
    //          Zeros are shifted into the high-order positions.


    /***********************************************************************************************
     * Initialise the scrambler table.
     * This *must* be called before the first use of crc16().
     *
     * @param reflectinput
     */

    public static void generateCrcTable(final boolean reflectinput)
        {
        // Create the X25 table
        for (int i = 0;
             i < 256;
             i++)
            {
            int intCrc;
            int intBit;

            intCrc = i;

            if (reflectinput)
                {
                intCrc = reflect(intCrc, 8);
                }

            intCrc <<= POLYNOMIAL_ORDER - 8;

            for (int k = 0;
                 k < 8;
                 k++)
                {
                intBit = intCrc & CRC_HIGH_BIT;

                intCrc <<= 1;

                if (intBit != 0)
                    {
                    intCrc ^= POLYNOMIAL_8408;
                    }
                }

            if (reflectinput)
                {
                intCrc = reflect(intCrc, POLYNOMIAL_ORDER);
                }

            intCrc &= CRC_MASK;

            crcTable8408[i] = intCrc;
            }

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "CRC16 finished generating CRC table");
        }


    /**
     * Calc CRC-16 with unofficial Sun method. You will get a warning message of the form
     * [warning: sun.misc.CRC16 is Sun proprietary API and may be removed in a future release]
     *
     * @param ba byte array to compute CRC on
     * @return 16-bit CRC, signed
     */
//    public static short sunCRC16( byte[] ba )
//        {
//        // create a new CRC-calculating object
//        final sun.misc.CRC16 crc = new sun.misc.CRC16();
//        // loop, calculating CRC for each byte of the string
//        // There is no CRC16.update(byte[]) method.
//        for ( byte b : ba )
//            {
//            crc.update( b );
//            }
//        // note use crc.value, not crc.getValue()
//        return ( short ) crc.value;
//        }


    /***********************************************************************************************
     * Some simple testing!
     *
     * @param args
     */

    public static void main(final String[] args)
        {
        final String strTest;

        // This should return 2B91 for 1021 polynomial
        strTest = "123456789";

        generateCrcTable(true);
        System.out.println("RefIn, RefOut [1DBA] CRC(" + strTest + ") = " + Integer.toHexString(crc16(strTest.getBytes(), true, true)).toUpperCase());

        generateCrcTable(true);
        System.out.println("RefIn [5DB8] CRC(" + strTest + ") = " + Integer.toHexString(crc16(strTest.getBytes(), true, false)).toUpperCase());

        generateCrcTable(false);
        System.out.println("RefOut [4A0] CRC(" + strTest + ") = " + Integer.toHexString(crc16(strTest.getBytes(), false, true)).toUpperCase());

        generateCrcTable(false);
        System.out.println("Neither [520] CRC(" + strTest + ") = " + Integer.toHexString(crc16(strTest.getBytes(), false, false)).toUpperCase());

//        testReflect(0x1f1a, 16);
//        testReflect(0x0000, 16);
//        testReflect(0x0001, 3);
//        testReflect(0x000f, 3);
//        testReflect(0x0010, 5);
//        testReflect(0x00f0, 5);
//        testReflect(0x0100, 9);
//        testReflect(0x0f00, 9);
//        testReflect(0x1000, 13);
//        testReflect(0xf000, 13);


//        System.out.println("CRC 1021");
//        for (int i = 0;
//             i < crcTable1021.length;
//             i++)
//            {
//            int integerCRC = crcTable1021[i];
//
//            System.out.println(i + "  " + Utilities.intToBitString(integerCRC));
//            }
//
//        System.out.println("CRC 8408");
//        for (int i = 0;
//             i < crcTable8408.length;
//             i++)
//            {
//            int integerCRC = crcTable8408[i];
//
//            System.out.println(i + "  " + Utilities.intToBitString(integerCRC));
//            }
        }


    /***********************************************************************************************
     * Calc CRC with CCITT method.
     * Only usable with polynomial orders of 8, 16, 24 or 32.
     * Uses direct method, i.e. no augmented zero bits.
     * Optional reflect the data bits and just before the final XOR.
     * WARNING! Java's bitwise operators operate on individual bits of integer (int and long) values.
     * If an operand is shorter than an int, it is promoted to int before doing the operations.
     * ^ is XOR.
     * The short data type is a 16-bit signed two's complement integer.
     *
     * @param bytes
     * @param reflectinput
     * @param reflectoutput
     *
     * @return short
     */

    public synchronized static short crc16(final byte[] bytes,
                                           final boolean reflectinput,
                                           final boolean reflectoutput)
        {
        final String SOURCE = "CRC16.crc16() ";
        int intCrc;

        intCrc = INITIAL_VALUE_DIRECT;

        if (reflectinput)
            {
            intCrc = reflect(intCrc, POLYNOMIAL_ORDER);
            }

        // Loop, calculating CRC for each byte of the string
        if (reflectinput)
            {
            for (int i = 0;
                 i < bytes.length;
                 i++)
                {
                try
                    {
                    // ^ is BITWISE XOR, & is BITWISE AND
                    // BEWARE! If an operand is shorter than an int, it is promoted to int before doing the operations,
                    // so we need to mask down to a byte, then cast back down to a byte for the array index {0...255}
                    // BEWARE!! the signed upcast of byte to int
                    // BEWARE!! Array index values are integers
                    // See: http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#170952
                    // The index expression undergoes unary numeric promotion; the promoted type must be int.

                    intCrc = (intCrc >> 8) ^ crcTable8408[((intCrc & 0xff) ^ (bytes[i] & 0xff)) & 0xff];
                    }

                catch (ArrayIndexOutOfBoundsException exception)
                    {
                    LOGGER.error(SOURCE
                                     + "ArrayIndexOutOfBoundsException Reflect loop [intCrc="
                                     + intCrc
                                     + "] [bytes[i]="
                                     + bytes[i]
                                     + "] [i="
                                     + i
                                     + "] [index="
                                     + (((intCrc & 0xff) ^ (bytes[i] & 0xff)) & 0xff)
                                     + "]");
                    exception.printStackTrace();
                    }
                }
            }
        else
            {
            for (int i = 0;
                 i < bytes.length;
                 i++)
                {
                try
                    {
                    // ^ is BITWISE XOR, & is BITWISE AND
                    // BEWARE! If an operand is shorter than an int, it is promoted to int before doing the operations,
                    // so we need to mask down to a byte, then cast back down to a byte for the array index {0...255}
                    // BEWARE!! the signed upcast of byte to int
                    // BEWARE!! Array index values are integers
                    // See: http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#170952
                    // The index expression undergoes unary numeric promotion; the promoted type must be int.

                    intCrc = (intCrc << 8) ^ crcTable8408[(((intCrc >> (POLYNOMIAL_ORDER-8)) & 0xff) ^ (bytes[i] & 0xff)) & 0xff];
                    }

                catch (ArrayIndexOutOfBoundsException exception)
                    {
                    LOGGER.error(SOURCE
                                     + "ArrayIndexOutOfBoundsException NO Reflect loop [intCrc="
                                     + intCrc
                                     + "] [bytes[i]="
                                     + bytes[i]
                                     + "] [i="
                                     + i
                                     + "] [index="
                                     + (((intCrc & 0xff) ^ (bytes[i] & 0xff)) & 0xff)
                                     + "]");
                    exception.printStackTrace();
                    }
                }
            }

        // Do we need to reflect the output before the XOR?
        if (reflectinput ^ reflectoutput)
            {
            intCrc = reflect(intCrc, POLYNOMIAL_ORDER);
            }

        intCrc ^= FINAL_XOR_VALUE;
        intCrc &= CRC_MASK;

        // The short data type is a 16-bit signed two's complement integer
        return ((short) intCrc);
        }


    /***********************************************************************************************
     * Test bit reflection.
     *
     * @param in
     * @param bits
     */

    private static void testReflect(final int in,
                                    final int bits)
        {
        int intTest;

        intTest = reflect(in, bits);

        System.out.println(bits + "bits  " + Utilities.intToBitString(in) + " --> " + Utilities.intToBitString(intTest));
        }


    /***********************************************************************************************
     * Reflects the lower 'bitcount' bits of 'crc'.
     */

    public static int reflect (final int crc,
                               final int bitcount)
        {
        int i;
        int j;
        int crcout;

        j = 1;
        crcout = 0;

        for (i = (int)1 << (bitcount -1);
             i != 0;
             i >>= 1)
            {
            if ((crc & i) != 0)
                {
                crcout |= j;
                }

            j<<= 1;
            }

        return (crcout);
        }
    }


//public class CRC16 {
//   35
//   36         /** value contains the currently computed CRC, set it to 0 initally */
//   37         public int value;
//   38
//   39         public CRC16() {
//   40             value = 0;
//   41         }
//   42
//   43         /** update CRC with byte b */
//   44         public void update(byte aByte) {
//   45             int a, b;
//   46
//   47             a = (int) aByte;
//   48             for (int count = 7; count >=0; count--) {
//   49                 a = a << 1;
//   50                 b = (a >>> 8) & 1;
//   51                 if ((value & 0x8000) != 0) {
//   52                     value = ((value << 1) + b) ^ 0x1021;
//   53                 } else {
//   54                     value = (value << 1) + b;
//   55                 }
//   56             }
//   57             value = value & 0xffff;
//   58             return;
//   59         }
//   60
//   61         /** reset CRC value to 0 */
//   62         public void reset() {
//   63             value = 0;
//   64         }
//   65     }

