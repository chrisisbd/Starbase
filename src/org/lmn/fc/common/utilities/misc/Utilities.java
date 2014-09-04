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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatusList;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterDataType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * Utilities.
 */

public final class Utilities implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkMetadata,
                                        FrameworkSingletons,
                                        FrameworkRegex
    {
    public static final String REGEX_REPLACE_COMMENT = " -->";
    public static final String REGEX_REPLACE_COMMENT_DOTS = "<!-- ";
    public static final String REGEX_REPLACE_WHITESPACE = "><";
    public static final String REGEX_REPLACE_MARKER_START = ">[";
    public static final String REGEX_REPLACE_MARKER_END = "]<";


    public static void main(final String[] args)
        {
        final String strRegex0 = "^(1|5|10|20|50|60)$";
        final String strRegex = "^(- 0.3 |  1 | + 10)$";

        final String[] arrayItems;

        // Match all punctuation except '.' '-' and '+'
        arrayItems = strRegex.split(FrameworkRegex.REGEX_INDEXED_NUMERIC_LIST);

        if (arrayItems.length > 0)
            {
            final List<String> list;

            list = new ArrayList<String>();

            for (int i = 0;
                 i < arrayItems.length;
                 i++)
                {
                final String item = arrayItems[i];

                if ((item != null)
                    && (!EMPTY_STRING.equals(item.trim())))
                    {
                    list.add(item);
                    }

                System.out.println("[array i=" + i + "]  [item=" + item + "]");
                }

            for (int i = 0;
                 i < list.size();
                 i++)
                {
                final String s = list.get(i);
                System.out.println("[list i=" + i + "]  [item=" + list.get(i) + "]");

                }
            }

        }


    /***********************************************************************************************
     * For testing!
     *
     * e.g. FieldWidth = 4
     *       XXXXX   -->   EMPTY_STRING
     *        XXXX   -->   XXXX
     *         XXX   -->   0XXX
     *          XX   -->   00XX
     *           X   -->   000X
     *           0   -->   0000
     *          -X   -->   -00X
     *         -XX   -->   -0XX
     *        -XXX   -->   -XXX
     *       -XXXX   -->   EMPTY_STRING
     *      -XXXXX   -->   EMPTY_STRING
     *
     * @param args
     */

    public static void main2(final String[] args)
        {
        System.out.println("DECIMAL 4");
        System.out.println("[test=XXXXX    -->   EMPTY_STRING] [result=" + intToString(12345, 10, 4) + "]");
        System.out.println("[test=XXXX     -->   XXXX]         [result=" + intToString(1234, 10, 4) + "]");
        System.out.println("[test=XXX      -->   0XXX]         [result=" + intToString(123, 10, 4) + "]");
        System.out.println("[test=XX       -->   00XX]         [result=" + intToString(12, 10, 4) + "]");
        System.out.println("[test=X        -->   000X]         [result=" + intToString(1, 10, 4) + "]");
        System.out.println("[test=0        -->   0000]         [result=" + intToString(0, 10, 4) + "]");
        System.out.println("[test=-X       -->   -00X]         [result=" + intToString(-1, 10, 4) + "]");
        System.out.println("[test=-XX      -->   -0XX]         [result=" + intToString(-12, 10, 4) + "]");
        System.out.println("[test=-XXX     -->   -XXX]         [result=" + intToString(-123, 10, 4) + "]");
        System.out.println("[test=-XXXX    -->   EMPTY_STRING] [result=" + intToString(-1234, 10, 4) + "]");
        System.out.println("[test=-XXXXX   -->   EMPTY_STRING] [result=" + intToString(-12345, 10, 4) + "]");
        System.out.println("DECIMAL 2");
        System.out.println("[test=XXX      -->   EMPTY_STRING] [result=" + intToString(123, 10, 2) + "]");
        System.out.println("[test=XX       -->   XX]           [result=" + intToString(12, 10, 2) + "]");
        System.out.println("[test=X        -->   0X]           [result=" + intToString(1, 10, 2) + "]");
        System.out.println("[test=0        -->   00]           [result=" + intToString(0, 10, 2) + "]");
        System.out.println("[test=-X       -->   -X]           [result=" + intToString(-1, 10, 2) + "]");
        System.out.println("[test=-XX      -->   EMPTY_STRING] [result=" + intToString(-12, 10, 2) + "]");
        System.out.println("HEX 4");
        System.out.println("[test=XXXXX    -->   EMPTY_STRING] [result=" + intToString(0x12345, 16, 4) + "]");
        System.out.println("[test=XXXX     -->   XXXX]         [result=" + intToString(0x1234, 16, 4) + "]");
        System.out.println("[test=XXX      -->   0XXX]         [result=" + intToString(0x123, 16, 4) + "]");
        System.out.println("[test=XX       -->   00XX]         [result=" + intToString(0x12, 16, 4) + "]");
        System.out.println("[test=X        -->   000X]         [result=" + intToString(0x1, 16, 4) + "]");
        System.out.println("[test=0        -->   0000]         [result=" + intToString(0x0, 16, 4) + "]");
        System.out.println("[test=-X       -->   -00X]         [result=" + intToString(-0x1, 16, 4) + "]");
        System.out.println("[test=-XX      -->   -0XX]         [result=" + intToString(-0x12, 16, 4) + "]");
        System.out.println("[test=-XXX     -->   -XXX]         [result=" + intToString(-0x123, 16, 4) + "]");
        System.out.println("[test=-XXXX    -->   EMPTY_STRING] [result=" + intToString(-0x1234, 16, 4) + "]");
        System.out.println("[test=-XXXXX   -->   EMPTY_STRING] [result=" + intToString(-0x12345, 16, 4) + "]");
        System.out.println("HEX 2");
        System.out.println("[test=XXX      -->   EMPTY_STRING] [result=" + intToString(0x123, 16, 2) + "]");
        System.out.println("[test=XX       -->   XX]           [result=" + intToString(0x12, 16, 2) + "]");
        System.out.println("[test=X        -->   0X]           [result=" + intToString(0x1, 16, 2) + "]");
        System.out.println("[test=0        -->   00]           [result=" + intToString(0x0, 16, 2) + "]");
        System.out.println("[test=-X       -->   -X]           [result=" + intToString(-0x1, 16, 2) + "]");
        System.out.println("[test=-XX      -->   EMPTY_STRING] [result=" + intToString(-0x12, 16, 2) + "]");

        System.out.println("INDEXED LIST");
        final ParameterType parameter;
        parameter = ParameterType.Factory.newInstance();

        parameter.setName("Test");
        parameter.setRegex("^(123|456|789|987|654|321)$");
        parameter.setUnits(SchemaUnits.DIMENSIONLESS);
        parameter.setTooltip("Test");

        final ParameterDataType inputType;
        inputType = ParameterDataType.Factory.newInstance();

        inputType.setDataTypeName(SchemaDataType.NUMERIC_INDEXED_LIST);

        parameter.setInputDataType(inputType);

        final ParameterDataType trafficType;
        trafficType = ParameterDataType.Factory.newInstance();

        trafficType.setDataTypeName(SchemaDataType.HEX_INTEGER);
        trafficType.setFieldCount(3L);

        parameter.setTrafficDataType(trafficType);

        parameter.setValue("456");

        if (XmlBeansUtilities.isValidXml(parameter))
            {
            final DataTypeDictionary trafficDataType;
            final String strResult;

            // Find the XmlBeans DataType specific to this Parameter's TrafficDataType
            trafficDataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(parameter.getTrafficDataType().getDataTypeName().toString());

            strResult = assignIndexToNumericListItem(parameter,
                                                     trafficDataType.getRadix(),
                                                     (int)parameter.getTrafficDataType().getFieldCount());

            System.out.println("Result=[" + strResult + "]");
            }
        else
            {
            System.out.println("Failed!");
            }
        }


    /***********************************************************************************************
     * Set the Font of a hierarchy of Containers.
     * http://www.coderanch.com/t/342116/GUI/java/set-font-JFileChooser
     *
     * @param components
     * @param font
     */

    public static void setContainerHierarchyFonts(final Component[] components,
                                                  final FontInterface font)
        {
        for (int intIndexComponent = 0;
             intIndexComponent < components.length;
             intIndexComponent++)
            {
            if(components[intIndexComponent] instanceof Container)
                {
                setContainerHierarchyFonts(((Container) components[intIndexComponent]).getComponents(),
                                           font);
                }

            try
                {
                components[intIndexComponent].setFont(font.getFont());
                }

            catch (Exception exception)
                {
                // Do nothing on any error
                }
            }
        }


    /***********************************************************************************************
     * Set the Foreground and Background colours of a hierarchy of Containers.
     *
     * @param components
     * @param foreground
     * @param background
     */

    public static void setContainerHierarchyColours(final Component[] components,
                                                    final ColourInterface foreground,
                                                    final ColourInterface background)
        {
        for (int intIndexComponent = 0;
             intIndexComponent < components.length;
             intIndexComponent++)
            {
            if(components[intIndexComponent] instanceof Container)
                {
                setContainerHierarchyColours(((Container) components[intIndexComponent]).getComponents(),
                                             foreground,
                                             background);
                }

            try
                {
                if (foreground != null)
                    {
                    components[intIndexComponent].setForeground(foreground.getColor());
                    }

                if (background != null)
                    {
                    components[intIndexComponent].setBackground(background.getColor());
                    }
                }

            catch (Exception exception)
                {
                // Do nothing on any error
                }
            }
        }


    /***********************************************************************************************
     * Convert a byte to a String of two Hex characters, with leading zero. 00...FF
     * Always return upper case.
     *
     * @param data
     *
     * @return String
     */

    public static String byteToTwoHexString(final byte data)
        {
        final StringBuffer buffer;
        final String strHex;

        buffer = new StringBuffer();

        // Returns a string representation of the *integer* argument as an unsigned integer in base 16
        strHex = Integer.toHexString(data & 0xFF);

        // Replace leading zeroes
        if (strHex.length() == 0)
            {
            // Not needed?
            buffer.append("00");
            }
        else if (strHex.length() == 1)
            {
            buffer.append("0");
            buffer.append(strHex);
            }
        else
            {
            // We must remove any leading 'FF's caused by sign extension...
            buffer.append(strHex.substring(0,2));
            }

        return (buffer.toString().toUpperCase());
        }


    /***********************************************************************************************
     * Convert a byte to a three character Decimal string, with leading zeroes. 000...255
     *
     * @param value
     *
     * @return String
     */

    public static String byteToThreeDecimalString(final byte value)
        {
        final StringBuffer buffer;
        final String strDecimal;

        buffer = new StringBuffer();
        // ToDo FIX THIS! Signed decimal representation, The radix is assumed to be 10
        strDecimal = Byte.toString(value);

        if (strDecimal.length() == 0)
            {
            // Not needed?
            buffer.append("000");
            }
        else if (strDecimal.length() == 1)
            {
            buffer.append("00");
            buffer.append(strDecimal);
            }
        else if (strDecimal.length() == 2)
            {
            buffer.append("0");
            buffer.append(strDecimal);
            }
        else
            {
            buffer.append(strDecimal);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert an integer to a two character Hex string, with leading zero.
     * Always return upper case.
     *
     * @param value
     *
     * @return String
     */

    public static String intToTwoHexString(final int value)
        {
        final StringBuffer buffer;
        final String strHex;

        buffer = new StringBuffer();

        // Returns a string representation of the *integer* argument as an unsigned integer in base 16
        strHex = Integer.toHexString(value & 0xFF);

        if (strHex.length() == 0)
            {
            // Not needed?
            buffer.append("00");
            }
        else if (strHex.length() == 1)
            {
            buffer.append("0");
            buffer.append(strHex);
            }
        else
            {
            // We must remove any leading 'FF's caused by sign extension...
            buffer.append(strHex.substring(0,2));
            }

        return (buffer.toString().toUpperCase());
        }


    /***********************************************************************************************
     * Convert an integer to a four character Hex string, with leading zeroes.
     * Always return upper case.
     *
     * @param value
     *
     * @return String
     */

    public static String intToFourHexString(final int value)
        {
        final StringBuffer buffer;
        final String strHex;

        buffer = new StringBuffer();
        // Returns a string representation of the integer argument as an unsigned integer in base 16
        strHex = Integer.toHexString(value & 0xFFFF);

        // There must be a better way?!
        if (strHex.length() == 0)
            {
            // Not needed?
            buffer.append("0000");
            }
        else if (strHex.length() == 1)
            {
            buffer.append("000");
            buffer.append(strHex);
            }
        else if (strHex.length() == 2)
            {
            buffer.append("00");
            buffer.append(strHex);
            }
        else if (strHex.length() == 3)
            {
            buffer.append("0");
            buffer.append(strHex);
            }
        else
            {
            // We must remove any leading 'FF's caused by sign extension...
            buffer.append(strHex.substring(0,4));
            }

        return (buffer.toString().toUpperCase());
        }


    /***********************************************************************************************
     * Convert an integer to a eight character Hex string, with leading zeroes.
     * Always return upper case.
     *
     * @param value
     *
     * @return String
     */

    public static String intToEightHexString(final int value)
        {
        final StringBuffer buffer;
        final String strHex;

        buffer = new StringBuffer();
        // Returns a string representation of the integer argument as an unsigned integer in base 16
        strHex = Integer.toHexString(value & 0xFFFFFFFF);

        // There must be a better way?!
        if (strHex.length() == 0)
            {
            // Not needed?
            buffer.append("00000000");
            }
        else if (strHex.length() == 1)
            {
            buffer.append("0000000");
            buffer.append(strHex);
            }
        else if (strHex.length() == 2)
            {
            buffer.append("000000");
            buffer.append(strHex);
            }
        else if (strHex.length() == 3)
            {
            buffer.append("00000");
            buffer.append(strHex);
            }
        else if (strHex.length() == 4)
            {
            buffer.append("0000");
            buffer.append(strHex);
            }
        else if (strHex.length() == 5)
            {
            buffer.append("000");
            buffer.append(strHex);
            }
        else if (strHex.length() == 6)
            {
            buffer.append("00");
            buffer.append(strHex);
            }
        else if (strHex.length() == 7)
            {
            buffer.append("0");
            buffer.append(strHex);
            }
        else
            {
            // We must remove any leading 'FF's caused by sign extension...
            buffer.append(strHex.substring(0, 8));
            }

        return (buffer.toString().toUpperCase());
        }


    /***********************************************************************************************
     * Convert an integer to a String, in the specified radix,
     * with an appropriate number of leading zeroes, and possibly a minus sign.
     * Always return upper case, unless out of range, in which case return EMPTY_STRING.
     *
     * e.g. FieldWidth = 4
     *       XXXXX   -->   EMPTY_STRING
     *        XXXX   -->   XXXX
     *         XXX   -->   0XXX
     *          XX   -->   00XX
     *           X   -->   000X
     *           0   -->   0000
     *          -X   -->   -00X
     *         -XX   -->   -0XX
     *        -XXX   -->   -XXX
     *       -XXXX   -->   EMPTY_STRING
     *      -XXXXX   -->   EMPTY_STRING
     *
     * i.e. be careful to ensure that the field width is correct if the number can be negative!
     *
     * @param value
     * @param radix
     * @param fieldwidth
     *
     * @return String
     */

    public static String intToString(final int value,
                                     final int radix,
                                     final long fieldwidth)
        {
        final StringBuffer buffer;
        final String strInt;

        buffer = new StringBuffer();

        // Returns a string representation of the first argument in the radix specified by the second argument
        // If the first argument is negative, the first element of the result is the ASCII minus character '-' ('\u002D').
        // If the first argument is not negative, no sign character appears in the result.
        strInt = Integer.toString(value, radix).toUpperCase();

        if (value > 0)
            {
            // Prepend with enough zeroes to make up to 'fieldwidth'
            for (int i = 0;
                i < (fieldwidth-strInt.length());
                i++)
                {
                buffer.append("0");
                }

            buffer.append(strInt);
            }
        else if (value == 0)
            {
            // The conversion is trivial, we just need 'fieldwidth' zeroes
            for (int i = 0;
                i < fieldwidth;
                i++)
                {
                buffer.append("0");
                }
            }
        else
            {
            // The number is negative, so we have one less leading zero to prepend
            // but we must start with the minus sign
            // e.g. -X   -->  -00X

            buffer.append(MINUS);

            for (int i = 0;
                i < (fieldwidth-strInt.length());
                i++)
                {
                buffer.append("0");
                }

            // Append the value, but ignore the leading minus sign
            buffer.append(strInt.substring(1));
            }

        // Finally check the overall length, and indicate an error if appropriate
        if (buffer.length() > fieldwidth)
            {
            buffer.setLength(0);
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert a positive integer to a four character Decimal string, with leading zeroes.
     *
     * @param value
     *
     * @return String
     */

    public static String intPositiveToFourDecimalString(final int value)
        {
        final StringBuffer buffer;
        final String strDecimal;

        buffer = new StringBuffer();

        if ((value >= 0) && (value <= 9999))
            {
            // Signed decimal representation
            strDecimal = Integer.toString(value, 10);

            // There must be a better way?!
            if (strDecimal.length() == 0)
                {
                // Not needed?
                buffer.append("0000");
                }
            else if (strDecimal.length() == 1)
                {
                buffer.append("000");
                buffer.append(strDecimal);
                }
            else if (strDecimal.length() == 2)
                {
                buffer.append("00");
                buffer.append(strDecimal);
                }
            else if (strDecimal.length() == 3)
                {
                buffer.append("0");
                buffer.append(strDecimal);
                }
            else
                {
                buffer.append(strDecimal);
                }
            }
        else
            {
            // The supplied int is Out of Range
            // For simplicity, just return 0
            buffer.append("0000");
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert a positive integer to a three character Decimal string, with leading zeroes.
     *
     * @param value
     *
     * @return String
     */

    public static String intPositiveToThreeDecimalString(final int value)
        {
        final StringBuffer buffer;
        final String strDecimal;

        buffer = new StringBuffer();

        if ((value >= 0) && (value <= 999))
            {
            // Signed decimal representation
            strDecimal = Integer.toString(value, 10);

            // There must be a better way?!
            if (strDecimal.length() == 0)
                {
                // Not needed?
                buffer.append("000");
                }
            else if (strDecimal.length() == 1)
                {
                buffer.append("00");
                buffer.append(strDecimal);
                }
            else if (strDecimal.length() == 2)
                {
                buffer.append("0");
                buffer.append(strDecimal);
                }
            else
                {
                buffer.append(strDecimal);
                }
            }
        else
            {
            // The supplied int is Out of Range
            // For simplicity, just return 0
            buffer.append("000");
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Format the int as a bit String, one nybble at a time.
     * Output is 1111 0000 1111 0000
     *
     * @param value
     *
     * @return String
     */

    public static String intToBitString(final int value)
        {
        final StringBuffer buffer;
        int intBitMask;
        final int intValue;

        buffer = new StringBuffer();
        intBitMask = 0x8000;
        intValue = value & 0xffff;

        for (int i = 0;
             i < 16;
             i++)
            {
            if ((intValue & intBitMask) != 0)
                {
                buffer.append("1");
                }
            else
                {
                buffer.append("0");
                }

            // Insert a formatting space every nybble
            if (((i+1) % 4) == 0)
                {
                buffer.append(SPACE);
                }

            intBitMask = intBitMask >> 1;
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert a List of Bytes to a human-readable string with expanded control characters.
     *
     * @param bytes
     *
     * @return String
     */

    public static String byteListToString(final List<Byte> bytes)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((bytes != null)
            && (!bytes.isEmpty()))
            {
            // Must be in synchronized block because incoming List may be from Collections.synchronizedList
            synchronized (bytes)
                {
                final Iterator<Byte> iterBytes;

                iterBytes = bytes.iterator();

                while (iterBytes.hasNext())
                    {
                    expandControlChars(buffer, iterBytes.next());
                    }
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert a List of Bytes to a byte array.
     *
     * @param bytes
     *
     * @return byte[]
     */

    public static byte[] byteListToArray(final List<Byte> bytes)
        {
        byte[] arrayBytes;

        arrayBytes = null;

        if ((bytes != null)
            && (!bytes.isEmpty()))
            {
            arrayBytes = new byte[bytes.size()];

            // Must be in synchronized block because incoming List may be from Collections.synchronizedList
            synchronized (bytes)
                {
                final Iterator<Byte> iterBytes;
                int intIndex;

                iterBytes = bytes.iterator();
                intIndex = 0;

                while (iterBytes.hasNext())
                    {
                    arrayBytes[intIndex++] = iterBytes.next();
                    }
                }

            // There must be an easier way?!
//            for (int i = 0; i < bytes.size(); i++)
//                {
//                arrayBytes[i] = bytes.get(i);
//                }
            }

        return (arrayBytes);
        }


    /***********************************************************************************************
     * Convert a byte array to a List of Bytes.
     *
     * @param bytes
     *
     * @return List<Byte>
     */

    public static List<Byte> byteArrayToList(final byte[] bytes)
        {
        final List<Byte> listBytes;

        listBytes = new ArrayList<Byte>();

        if ((bytes != null)
            && (bytes.length > 0))
            {
            for (int i = 0;
                 i < bytes.length;
                 i++)
                {
                listBytes.add(bytes[i]);
                }
            }

        return (listBytes);
        }


    /***********************************************************************************************
     * Convert a byte array to a human-readable string with expanded control characters.
     *
     * @param bytes
     *
     * @return String
     */

    public static String byteArrayToExpandedAscii(final byte[] bytes)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((bytes != null)
            && (bytes.length > 0))
            {
            for (int i = 0;
                 i < bytes.length;
                 i++)
                {
                expandControlChars(buffer, bytes[i]);
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert a byte array to a string of HEX characters, spaced by one.
     *
     * @param bytes
     *
     * @return String
     */

    public static String byteArrayToSpacedHex(final byte[] bytes)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((bytes != null)
            && (bytes.length > 0))
            {
            for (int i = 0;
                 i < bytes.length;
                 i++)
                {
                buffer.append(byteToTwoHexString(bytes[i]));
                buffer.append(FrameworkStrings.SPACE);
                }
            }

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Add the byte to the buffer, expanding control characters to human-readable form.
     *
     * @param buffer
     * @param bytemsg
     */

    public static void expandControlChars(final StringBuffer buffer,
                                          final byte bytemsg)
        {
        // Is it a control character?
        if ((bytemsg >= 0)
            && (bytemsg <= ControlCharacters.US.getByteCode()))
            {
            final ControlCharacters[] ctrl;
            boolean boolFoundIt;

            ctrl = ControlCharacters.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < ctrl.length);
                 i++)
                {
                if (ctrl[i].getByteCode() == bytemsg)
                    {
                    buffer.append(ctrl[i].getSymbol());
                    boolFoundIt = true;
                    }
                }
            }
        else
            {
            buffer.append(new char[] { (char)bytemsg });
            }
        }


    /***********************************************************************************************
     * Add a String to the message, character by character.
     * Return the updated checksum.
     *
     * @param message
     * @param text
     * @param checksum
     *
     * @return int
     */

    public static int addStringToMessage(final List<Byte> message,
                                         final String text,
                                         final int checksum)
        {
        int intChecksum;

        intChecksum = checksum;

        if ((text != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(text)))
            {
            final byte[] arrayBytes;

            arrayBytes = text.getBytes();

            for (int i = 0;
                 i < arrayBytes.length;
                 i++)
                {
                message.add(arrayBytes[i]);
                intChecksum += arrayBytes[i];
                }
            }

        return (intChecksum);
        }


    /***********************************************************************************************
     * Add the US delimiter to the message, updating the checksum.
     *
     * @param message
     * @param checksum
     *
     * @return int
     */

    public static int addDelimiterToMessage(final List<Byte> message,
                                            final int checksum)
        {
        return (addByteToMessage(message,
                                 ObservatoryConstants.STARIBUS_DELIMITER,
                                 checksum));
        }


    /***********************************************************************************************
     * Add a byte to the message, updating the checksum.
     *
     * @param message
     * @param bytevalue
     * @param checksum
     *
     * @return int
     */

    public static int addByteToMessage(final List<Byte> message,
                                       final byte bytevalue,
                                       final int checksum)
        {
        int intChecksum;

        intChecksum = checksum;

        message.add(bytevalue);
        intChecksum += bytevalue;

        return (intChecksum);
        }


    /***********************************************************************************************
     * Add the checksum to the message, as two Hex characters.
     *
     * @param message
     * @param checksum
     */

//    public static void addChecksumToMessage(final List<Byte> message,
//                                            final int checksum)
//        {
//        // Use uppercase for the checksum
////        System.out.println("Checksum = " + checksum);
////        System.out.println("Checksum 2's complement dec " + (~(checksum) + 1));
////        System.out.println("Checksum 2's complement hex " + intToTwoHexString(~(checksum) + 1));
////        System.out.println("Checksum 2's complement hex masked " + intToTwoHexString((byte)(~(checksum) + 1) & 0xff));
//        addStringToMessage(message,
//                           intToTwoHexString((byte)(~(checksum) + 1) & 0xff),
//                           checksum);
//        }


    /***********************************************************************************************
     * Add the CRC checksum to the message, as four Hex characters.
     * Exclude the first Byte in the message, which was the STX.
     *
     * @param message
     * @param checksum
     */

    public static void addCrcToMessage(final List<Byte> message,
                                       final int checksum)
        {
        if ((message != null)
            && (!message.isEmpty()))
            {
            final byte[] bytesInMessage;
            final byte[] bytesToCrc;
            final short shortCalculatedCRC;

            bytesInMessage = new byte[message.size()];
            bytesToCrc = new byte[message.size() - 1];

            // Do not pass message via a String! (encoding and number base issues)
            // Must be in synchronized block because incoming List may be from Collections.synchronizedList
            synchronized (message)
                {
                final Iterator<Byte> iterBytes;
                int intIndex;

                iterBytes = message.iterator();
                intIndex = 0;

                while (iterBytes.hasNext())
                    {
                    bytesInMessage[intIndex++] = iterBytes.next();
                    }
                }

            //System.out.println("Utilities.addCrcToMessage() complete byte array=" + byteArrayToSpacedHex(bytesInMessage));

            System.arraycopy(bytesInMessage, 1,
                             bytesToCrc, 0,
                             message.size() - 1);

            //System.out.println("Utilities.addCrcToMessage() using byte array=" + byteArrayToSpacedHex(bytesToCrc));

            shortCalculatedCRC = CRC16.crc16(bytesToCrc, true, true);

            // Use uppercase for the checksum
            // This does not iterate over the List
            addStringToMessage(message,
                               intToFourHexString(shortCalculatedCRC),
                               checksum);

            // System.out.println("ADDED CRC TO MESSAGE = [" + intToFourHexString(shortCalculatedCRC) + "]");
            //System.out.println("MESSAGE AFTER CRC = [" + Utilities.byteListToString(message) + "]");
            }
        else
            {
            LOGGER.error("Utilities.addCrcToMessage() tried to use CRC invalid message list");
            }
        }


    /***********************************************************************************************
     * Assign an index {0...n} to an item from a list of ParameterValues constrained by Regex.
     * Return the index String padded to fieldcount with leading zeroes, or null if an error occurs.
     *
     * @param parameter
     * @param radix
     * @param fieldcount
     *
     * @return String
     */

    public static String assignIndexToNumericListItem(final ParameterType parameter,
                                                      final int radix,
                                                      final int fieldcount)
        {
        String strResult;

        strResult = null;

        // This type constrains a list by Regex OR choices,
        // and assigns an index {0...n} for items from the Regex list

        if ((parameter != null)
            && (XmlBeansUtilities.isValidXml(parameter))
            && (fieldcount > 0))
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            if ((parameter.getRegex() == null)
                || (EMPTY_STRING.equals(parameter.getRegex().trim())))
                {
                // If there is no Regex present, then there can only be one item, of index 0
                // padded to a width of fieldcount
                fillField(buffer, fieldcount, ZERO);
                strResult = buffer.toString();
                }
            else
                {
                // We have to deal with the Regex expression, and make the assumption that it
                // is a list of OR choices, using '|' to separate items, with possible ^(...)$

                // First re-check that the ParameterValue actually appears in the Regex output
                if (Pattern.matches(parameter.getRegex(), parameter.getValue()))
                    {
                    final String[] arrayItems;

                    // So... the value *must* match one of the items in the Regex list
                    // Produce an array of the items, matching all Regex punctuation except '.' '-' and '+'
                    arrayItems = parameter.getRegex().split(FrameworkRegex.REGEX_INDEXED_NUMERIC_LIST);

                    if (arrayItems.length > 0)
                        {
                        final List<String> list;
                        boolean boolFoundIt;

                        list = new ArrayList<String>();

                        // Get only those non-empty items in the split,
                        // i.e. everything between the punctuation
                        for (int i = 0;
                             i < arrayItems.length;
                             i++)
                            {
                            final String item;

                            item = arrayItems[i];

                            if ((item != null)
                                && (!EMPTY_STRING.equals(item.trim())))
                                {
                                list.add(item.trim());
                                }
                            }

                        // Which item in the List matches the ParameterValue now?
                        boolFoundIt = false;

                        for (int i = 0;
                             (!boolFoundIt) && (i < list.size());
                             i++)
                            {
                            final String item;

                            item = list.get(i);

//                            LOGGER.debug("Regex [i=" + i + "] [item=" + item + "]");

                            if (parameter.getValue().equals(item))
                                {
                                // The index is the loop index padded to fieldcount
                                buffer.append(intToString(i, radix, fieldcount));

                                strResult = buffer.toString();

                                // Time to leave the loop
                                boolFoundIt = true;
                                }
                            }

                        // If we didn't find it, something went wrong again...
                        if (!boolFoundIt)
                            {
                            strResult = null;
                            }
                        }
                    else
                        {
                        // There were no characters found by the Regex, so we don't know how to deal with this
                        strResult = null;
                        }
                    }
                else
                    {
                    // We have been misled by the caller - something went wrong...
                    strResult = null;
                    }
                }
            }
        else
            {
            // Something went wrong...
            strResult = null;
            }

        return (strResult);
        }


    /***********************************************************************************************
     * Assign an index {0...n} to an item from a list of Choices.
     * Return the index String padded to fieldcount with leading zeroes, or null if an error occurs.
     *
     * @param parameter
     * @param radix
     * @param fieldcount
     *
     * @return String
     */

    public static String assignIndexToChoiceItem(final ParameterType parameter,
                                                 final int radix,
                                                 final int fieldcount)
        {
        String strResult;

        strResult = null;

        // This assigns an index {0...n} for items from the Choices list

        if ((parameter != null)
            && (XmlBeansUtilities.isValidXml(parameter))
            && (radix > 0)
            && (fieldcount > 0))
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            if ((parameter.getChoices() == null)
                || (EMPTY_STRING.equals(parameter.getChoices().trim())))
                {
                // If there are no Choices present, then there can only be one item, of index 0
                // padded to a width of fieldcount
                fillField(buffer, fieldcount, ZERO);
                strResult = buffer.toString();

                LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                          "Utilities.assignIndexToChoiceItem() No Choices present [value_assigned=" + strResult + "]"
                );
                }
            else
                {
                // We have to deal with the Choices, and make the assumption that it
                // is a list of Strings, using ',' to separate items

                // First re-check that the ParameterValue actually appears in the Choices
                if (parameter.getChoices().contains(parameter.getValue()))
                    {
                    final String[] arrayItems;

                    LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "Utilities.assignIndexToChoiceItem() Choice appears to be valid"
                    );

                    // So... the value *must* match one of the items in the Choices list
                    // Produce an array of the Choice items
                    arrayItems = parameter.getChoices().split(COMMA);

                    if (arrayItems.length > 0)
                        {
                        final List<String> list;
                        boolean boolFoundIt;

                        list = new ArrayList<String>();

                        // Get only those non-empty items in the split,
                        // i.e. everything between the punctuation
                        for (int i = 0;
                             i < arrayItems.length;
                             i++)
                            {
                            final String item;

                            item = arrayItems[i];

                            if ((item != null)
                                && (!EMPTY_STRING.equals(item.trim())))
                                {
                                list.add(item.trim());
                                }
                            }

                        // Which item in the List matches the ParameterValue now?
                        boolFoundIt = false;

                        for (int i = 0;
                             (!boolFoundIt) && (i < list.size());
                             i++)
                            {
                            final String item;

                            item = list.get(i);

                            if (parameter.getValue().equals(item))
                                {
                                // The index is the loop index padded to fieldcount
                                buffer.append(intToString(i, radix, fieldcount));

                                strResult = buffer.toString();

                                // Time to leave the loop
                                boolFoundIt = true;
                                }
                            }

                        // If we didn't find it, something went wrong again...
                        if (!boolFoundIt)
                            {
                            LOGGER.error("Utilities.assignIndexToChoiceItem() Unable to find Choice item");

                            strResult = null;
                            }
                        }
                    else
                        {
                        // There were no characters found in the Choices, so we don't know how to deal with this
                        LOGGER.error("Utilities.assignIndexToChoiceItem() Choice List is empty");

                        strResult = null;
                        }
                    }
                else
                    {
                    // We have been misled by the caller - something went wrong...
                    LOGGER.error("Utilities.assignIndexToChoiceItem() Choice List does not contain Parameter Value");

                    strResult = null;
                    }
                }
            }
        else
            {
            // Something went wrong...
            LOGGER.error("Utilities.assignIndexToChoiceItem() Called with invalid parameters");

            strResult = null;
            }

        LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                  "Utilities.assignIndexToChoiceItem() [value_assigned=" + strResult + "]"
        );

        return (strResult);
        }


    /***********************************************************************************************
     * Fill the StringBuffer with an appropriate number of characters to fill the field.
     *
     * @param buffer
     * @param fieldcount
     * @param symbol
     */

    private static void fillField(final StringBuffer buffer,
                                  final int fieldcount,
                                  final String symbol)
        {
        for (int i = 0;
             i < fieldcount;
             i++)
            {
            buffer.append(symbol);
            }
        }


    /***********************************************************************************************
     * Concatentate two byte arrays.
     *
     * @param array0
     * @param array1
     *
     * @return byte[]
     */

    public static byte[] concatenateByteArrays(final byte[] array0,
                                               final byte[] array1)
        {
        final byte[] bytesTotal;

        bytesTotal = new byte[(array0.length + array1.length)];

        System.arraycopy(array0, 0,
                         bytesTotal, 0,
                         array0.length);
        System.arraycopy(array1, 0,
                         bytesTotal, array0.length,
                         array1.length);

        return (bytesTotal);
        }


    /***********************************************************************************************
     * Get the Port Configuration as (COM1 9600, 8, 1, NP).
     *
     * @return String
     */

//    private static String getPortConfiguration()
//        {
//        final String strParity;
//        final String strPortConfiguration;
//
//        if (serialportData != null)
//            {
//            if (serialportData.getParity() == 0)
//                {
//                strParity = NO_PARITY;
//                }
//            else
//                {
//                strParity = Integer.toString(serialportData.getParity());
//                }
//
//            strPortConfiguration = LEFT_PARENTHESIS
//                                    + serialportData.getPortName()
//                                    + SPACE
//                                    + serialportData.getBaudrate()
//                                    + COMMA + SPACE
//                                    + serialportData.getDatabits()
//                                    + COMMA + SPACE
//                                    + serialportData.getStopbits()
//                                    + COMMA + SPACE
//                                    + strParity
//                                    + RIGHT_PARENTHESIS;
//            }
//        else
//            {
//            strPortConfiguration = EMPTY_STRING;
//            }
//
//        return (strPortConfiguration);
//        }


    /***********************************************************************************************
     * Convert 72nds of an inch into millimetres.
     *
     * @param size
     *
     * @return double
     */

    public synchronized static double to_mm(final double size)
        {
        return ((size / 72) * 25.4);
        }


    /***********************************************************************************************
     * Convert millimetres to 72nds of an incn (yuk).
     *
     * @param size
     *
     * @return double
     */

    public synchronized static double to_72nd_inch(final double size)
        {
        return ((size / 25.4) * 72);
        }


    /***********************************************************************************************
     * Strip any HTML beginning and end tags from the specified String.
     *
     * @param text
     *
     * @return String
     */

    public synchronized static String stripHTML(final String text)
        {
        final StringBuffer bufferInput;
        final StringBuffer bufferOutput;
        int i;

        if ((text != null)
            && (!text.trim().equals(""))
            && (text.trim().toLowerCase().startsWith("<html>"))
            && (text.trim().toLowerCase().endsWith("</html>")))
            {
            bufferInput = new StringBuffer();
            bufferOutput = new StringBuffer();

            bufferInput.append(text.trim());

            // Scan the string...
            i = 0;

            while (i < bufferInput.length())
                {
                while ((bufferInput.charAt(i) != '<')
                     && (i < bufferInput.length()))
                    {
                    bufferOutput.append(bufferInput.charAt(i));
                    i++;
                    }

                // We must be at the end of the string, or pointing to a '<'
                // Skip all characters until the closing '>' or the end of the string
                while ((bufferInput.charAt(i) != '>')
                        && (i < bufferInput.length()))
                    {
                    i++;
                    }

                // Did we reach the end of the tag?
                if ((bufferInput.charAt(i) == '>')
                    && (i < bufferInput.length()))
                    {
                    // If so, point to the next character to parse
                    i++;
                    }
                }

            return (bufferOutput.toString());
            }
        else
            {
            // Leave the text alone if there is no HTML tag pair
            return (text);
            }
        }


    /***********************************************************************************************
     * Wraps the call to Thread.sleep() in a catch block.
     * Stops waiting when told to do so, with a possible 5msec delay to respond.
     *
     * @param periodmillis period to sleep for in milliseconds
     * @param dao
     */

    public static void safeSleepPollExecuteWorker(final long periodmillis,
                                                  final ObservatoryInstrumentDAOInterface dao)
        {
        if (periodmillis > 0)
            {
            final long longStart;

            longStart = System.currentTimeMillis();

            // Check the DAO every 5msec to make sure it has not been interrupted
            while (((System.currentTimeMillis() - longStart) < periodmillis)
                && (executeWorkerCanProceed(dao)))
                {
                try
                    {
                    Thread.sleep(5);
                    }

                catch (InterruptedException exception)
                    {
                    // Ignored, since we are going to continue to wait
                    }
                }
            }
        }


    /***********************************************************************************************
     * Wraps the call to Thread.sleep() in a catch block.
     * Stops waiting when told to do so, with a possible 5msec delay to respond.
     *
     * @param periodmillis period to sleep for in milliseconds
     * @param dao
     * @param worker
     */

    public static void safeSleepPollWorker(final long periodmillis,
                                           final ObservatoryInstrumentDAOInterface dao,
                                           final SwingWorker worker)
        {
        if (periodmillis > 0)
            {
            final long longStart;

            longStart = System.currentTimeMillis();

            // Check the DAO every 5msec to make sure it has not been interrupted
            while (((System.currentTimeMillis() - longStart) < periodmillis)
                && (workerCanProceed(dao, worker)))
                {
                try
                    {
                    Thread.sleep(5);
                    }

                catch (InterruptedException exception)
                    {
                    // Ignored, since we are going to continue to wait
                    }
                }
            }
        }


    /***********************************************************************************************
     * Wraps the call to Thread.sleep() in a catch block.
     *
     * @param periodmillis period to sleep for in milliseconds
     */

    public static void safeSleep(final long periodmillis)
        {
        if (periodmillis > 0)
            {
            final long longStart;

            longStart = System.currentTimeMillis();

            while ((System.currentTimeMillis() - longStart) < periodmillis)
                {
                try
                    {
                    Thread.sleep(5);
                    }

                catch (InterruptedException exception)
                    {
                    // Ignored, since we are going to continue to wait
                    }
                }
            }
        }


    /***********************************************************************************************
     * Indicate if the operation can proceed, using the specified DAO and the DAO's ExecuteWorker.
     * Return false if interrupted by the User or other activity.
     *
     * @param dao
     *
     * @return boolean
     */

    public static boolean executeWorkerCanProceed(final ObservatoryInstrumentDAOInterface dao)
        {
        return ((dao != null)
                && (InstrumentState.isDoingSomething(dao.getHostInstrument()))
                && (ResponseMessageStatus.isResponseStatusOk(dao.getResponseMessageStatusList()))
                && (dao.getExecuteWorker() != null)
                && (!dao.getExecuteWorker().isStopping()));
        }


    /***********************************************************************************************
     * Indicate if the operation can proceed, using the DAO and the specified SwingWorker.
     * Return false if interrupted by the User or other activity.
     *
     * @param dao
     * @param worker
     *
     * @return boolean
     */

    public static boolean workerCanProceed(final ObservatoryInstrumentDAOInterface dao,
                                           final SwingWorker worker)
        {
        return ((dao != null)
                && (InstrumentState.isDoingSomething(dao.getHostInstrument()))
                && (ResponseMessageStatus.isResponseStatusOk(dao.getResponseMessageStatusList()))
                && (worker != null)
                && (!worker.isStopping()));
        }


    /***********************************************************************************************
     * Indicate if the retry operation can proceed, using the DAO and the specified SwingWorker.
     * Allow selection of the ResponseMessageStatusList,
     * since this could be in the DAO, a ResponseMessage, or explicitly created.
     * Retries are required on any ResponseMessageStatus Error, but not SUCCESS or ABORT.
     * Return false if interrupted by the User or other activity.
     *
     * @param dao
     * @param statuslist
     * @param worker
     *
     * @return boolean
     */

    public static boolean retryCanProceed(final ObservatoryInstrumentDAOInterface dao,
                                          final ResponseMessageStatusList statuslist,
                                          final SwingWorker worker)
        {
        return ((dao != null)
                && (InstrumentState.isDoingSomething(dao.getHostInstrument()))
                && (statuslist != null)
                && (!statuslist.contains(ResponseMessageStatus.SUCCESS))
                && (!statuslist.contains(ResponseMessageStatus.ABORT))
                && (worker != null)
                && (!worker.isStopping()));
        }


    /***********************************************************************************************
     * Compress XML in the specified buffer, by removing unnecessary whitespace etc.
     * See: http://www.regular-expressions.info/java.html
     *
     * @param buffer
     *
     * @return StringBuffer
     */

    public static StringBuffer compressXML(final StringBuffer buffer)
        {
        final String SOURCE = "Utilities.compressXML() ";
        StringBuffer newBuffer;

        newBuffer = buffer;

        try
            {
            StringBuffer compressedBuffer;
            Pattern pattern;
            Matcher matcher;

            //----------------------------------------------------------------------
            // Remove any comment fields - do this FIRST!

            compressedBuffer = new StringBuffer();
            pattern = Pattern.compile(REGEX_XML_COMMENT);
            matcher = pattern.matcher(newBuffer);

            while (matcher.find())
                {
                matcher.appendReplacement(compressedBuffer, SPACE);
                }

            // Tidy up
            matcher.appendTail(compressedBuffer);
            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Remove all whitespace between XML tags

            compressedBuffer = new StringBuffer();
            pattern = Pattern.compile(REGEX_COMPRESS_XML);
            matcher = pattern.matcher(newBuffer);

            while (matcher.find())
                {
                // Keep one space in case any Parameters etc. were found
                matcher.appendReplacement(compressedBuffer,
                                          REGEX_REPLACE_WHITESPACE);
                }

            // Tidy up
            matcher.appendTail(compressedBuffer);

            // Replace the original buffer with the new compressed String
            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Remove all blank lines

            compressedBuffer = new StringBuffer();
            pattern = Pattern.compile(REGEX_NEWLINE);
            matcher = pattern.matcher(newBuffer);

            while (matcher.find())
                {
                matcher.appendReplacement(compressedBuffer, EMPTY_STRING);
                }

            // Tidy up
            matcher.appendTail(compressedBuffer);
            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Remove anything between > and [

            compressedBuffer = new StringBuffer();
            pattern = Pattern.compile(REGEX_MARKER_START);
            matcher = pattern.matcher(newBuffer);

            while (matcher.find())
                {
                matcher.appendReplacement(compressedBuffer, REGEX_REPLACE_MARKER_START);
                }

            // Tidy up
            matcher.appendTail(compressedBuffer);
            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Remove anything between ] and <

            compressedBuffer = new StringBuffer();
            pattern = Pattern.compile(REGEX_MARKER_END);
            matcher = pattern.matcher(newBuffer);

            while (matcher.find())
                {
                matcher.appendReplacement(compressedBuffer, REGEX_REPLACE_MARKER_END);
                }

            // Tidy up
            matcher.appendTail(compressedBuffer);
            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Replace all CR LF whitespace, LF CR whitespace with a single space

//            compressedBuffer = new StringBuffer();
//            pattern = Pattern.compile(REGEX_LF_CR_WHITESPACE);
//            matcher = pattern.matcher(newBuffer);
//
//            while (matcher.find())
//                {
//                matcher.appendReplacement(compressedBuffer, SPACE);
//                }
//
//            // Tidy up
//            matcher.appendTail(compressedBuffer);
//            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Tidy up any trailing spaces in XML comments

//            compressedBuffer = new StringBuffer();
//            pattern = Pattern.compile(REGEX_XML_COMMENT_END);
//            matcher = pattern.matcher(newBuffer);
//
//            while (matcher.find())
//                {
//                matcher.appendReplacement(compressedBuffer, REGEX_REPLACE_COMMENT);
//                }
//
//            // Tidy up
//            matcher.appendTail(compressedBuffer);
//            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Tidy up any XML comments with repeated dots....

//            compressedBuffer = new StringBuffer();
//            pattern = Pattern.compile(REGEX_XML_COMMENT_DOTS);
//            matcher = pattern.matcher(newBuffer);
//
//            while (matcher.find())
//                {
//                matcher.appendReplacement(compressedBuffer, REGEX_REPLACE_COMMENT_DOTS);
//                }
//
//            // Tidy up
//            matcher.appendTail(compressedBuffer);
//            newBuffer = compressedBuffer;

            //----------------------------------------------------------------------
            // Remove any empty comment fields

//            compressedBuffer = new StringBuffer();
//            pattern = Pattern.compile(REGEX_XML_COMMENT_EMPTY);
//            matcher = pattern.matcher(newBuffer);
//
//            while (matcher.find())
//                {
//                matcher.appendReplacement(compressedBuffer, SPACE);
//                }
//
//            // Tidy up
//            matcher.appendTail(compressedBuffer);
//            newBuffer = compressedBuffer;

            }

        catch (PatternSyntaxException exception)
            {
            // Just stop now without compressing
            LOGGER.error(SOURCE + "Compression error "
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR);
            }

        catch (IllegalStateException exception)
            {
            // Just stop now without compressing
            LOGGER.error(SOURCE + "Compression error "
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR);
            }

        catch (IndexOutOfBoundsException exception)
            {
            // Just stop now without compressing
            LOGGER.error(SOURCE + "Compression error "
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR);
            }

        return (newBuffer);
        }
    }
