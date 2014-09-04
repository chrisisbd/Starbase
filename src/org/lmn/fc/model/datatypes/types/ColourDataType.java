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

//  05-10-04    LMN extended DataType

package org.lmn.fc.model.datatypes.types;

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;

import java.awt.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/***************************************************************************************************
 * ColourDataType.
 */

public final class ColourDataType extends RootDataType
                                  implements ColourInterface
    {
    private final Color colorData;
    private int intRed;
    private int intGreen;
    private int intBlue;


    /***********************************************************************************************
     * Append a Colour specification to the specified buffer.
     *
     * @param buffer
     * @param colour
     */

    private static void appendColour(final StringBuffer buffer,
                                     final int colour)
        {
        final String strColour;

        strColour = Integer.toHexString(colour & BYTE_MASK);

        if (strColour.length() == 0)
            {
            // Not needed?
            buffer.append("00");
            }
        else if (strColour.length() == 1)
            {
            buffer.append("0");
            buffer.append(strColour);
            }
        else
            {
            buffer.append(strColour);
            }
        }


    /***********************************************************************************************
     * Construct a ColourDataType with default values.
     */

    public ColourDataType()
        {
        super(DataTypeDictionary.COLOUR_DATA);

        // Initialise the colours to a default state
        this.intRed = 0;
        this.intGreen = 0;
        intBlue = 0;

        colorData = new Color(intRed, intGreen, intBlue);
        }


    /***********************************************************************************************
     * Construct a ColourDataType from a colour specification string r=nnn g=nnn b=nnn.
     *
     * @param colourvalues
     *
     * @throws NumberFormatException
     * @throws NoSuchElementException
     */

    public ColourDataType(final String colourvalues) throws NumberFormatException,
                                                            NoSuchElementException
        {
        super(DataTypeDictionary.COLOUR_DATA);

        final String strColour;
        String strName;
        String strValue;
        int intTemp;
        final StringTokenizer tokenizerColour;
        boolean boolExit;

        // Initialise the colours to a default state
        intRed = 0;
        intGreen = 0;
        intBlue = 0;

        // Try to parse the colour values from the string
        strColour = colourvalues.toLowerCase().trim();

        // Delimit the input on <space> and "="
        tokenizerColour = new StringTokenizer(strColour, " =");
        boolExit = false;

        while ((!boolExit)
            && (tokenizerColour.hasMoreTokens()))
            {
            try
                {
                // Find the first colour indicator
                strName = tokenizerColour.nextToken();

                //System.out.println("name token=[" + strName + "]");
                strValue = tokenizerColour.nextToken();

                //System.out.println("value token=[" + strValue + "]");
                intTemp = Integer.parseInt(strValue);

                if ((intTemp < 0) || (intTemp > BYTE_MASK))
                    {
                    throw new NumberFormatException();
                    }
                else
                    {
                    if (strName.equalsIgnoreCase("r"))
                        {
                        // Take the Red value
                        intRed = intTemp;
                        }
                    else if (strName.equalsIgnoreCase("g"))
                        {
                        // Take the Green value
                        intGreen = intTemp;
                        }
                    else if (strName.equalsIgnoreCase("b"))
                        {
                        // Take the Blue value
                        intBlue = intTemp;
                        }
                    else
                        {
                        throw new NumberFormatException();
                        }
                    }
                }

            catch(NoSuchElementException exception)
                {
                boolExit = true;
                }

            catch(NumberFormatException exception)
                {
                boolExit = true;
                }
            }

        // Now try to make a Color object with the results of the parsing
        // This will fail if any colours have not been parsed correctly
        colorData = new Color(intRed, intGreen, intBlue);
        }


    /***********************************************************************************************
     * Construct a ColourDataType from individual RGB colour values.
     *
     * @param red
     * @param green
     * @param blue
     */

    public ColourDataType(final int red,
                          final int green,
                          final int blue)
        {
        super(DataTypeDictionary.COLOUR_DATA);

        intRed = red & BYTE_MASK;
        intGreen = green & BYTE_MASK;
        intBlue = blue & BYTE_MASK;

        colorData = new Color(intRed, intGreen, intBlue);
        }


    /***********************************************************************************************
     * Construct a ColourDataType from a Color.
     *
     * @param color
     */

    public ColourDataType(final Color color)
        {
        super(DataTypeDictionary.COLOUR_DATA);

        intRed = color.getRed();
        intGreen = color.getGreen();
        intBlue = color.getBlue();

        colorData = color;
        }


    /***********************************************************************************************
     * Get the Color represented by this ColourDataType.
     *
     * @return Color
     */

    public Color getColor()
        {
        return (colorData);
        }


    /***********************************************************************************************
     * Format the Colour as a hex string, i.e. '#rrggbb' where the numbers are [00...ff].
     *
     * @return String
     */

    public String toHexFormat()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer("#");

        appendColour(buffer, intRed);
        appendColour(buffer, intGreen);
        appendColour(buffer, intBlue);

        return (buffer.toString());
        }


    /***********************************************************************************************
     * Convert the colour values back into a String.
     * <p>
     * Syntax: "<code>r=XX g=YY b=ZZ</code>" where the values are in Decimal
     *
     * @return String
     */

    public String toString()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        buffer.append("r=");
        buffer.append(intRed & BYTE_MASK);
        buffer.append(" g=");
        buffer.append(intGreen & BYTE_MASK);
        buffer.append(" b=");
        buffer.append(intBlue & BYTE_MASK);

        return (buffer.toString());
        }
    }
