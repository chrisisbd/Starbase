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

import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.FontInterface;

import java.awt.*;
import java.util.Hashtable;


/***************************************************************************************************
 * FontDataType.
 */

public final class FontDataType extends RootDataType
                                implements FontInterface
    {
    private static final Hashtable<String, Integer> hashtableStyles;

    static
        {
        hashtableStyles = new Hashtable<String, Integer>(4);
        hashtableStyles.put("plain", Font.PLAIN);
        hashtableStyles.put("bold", Font.BOLD);
        hashtableStyles.put("italic", Font.ITALIC);
        hashtableStyles.put("bolditalic", Font.BOLD + Font.ITALIC);
        }

    private String strFontName;
    private String strStyle;
    private int intStyle;
    private int intSize;
    private final Font fontData;


    /***********************************************************************************************
     * Construct a FontDataType with default values.
     * This cannot fail...
     */

    public FontDataType()
        {
        super(DataTypeDictionary.FONT_DATA);

        // Initialise the Font to default values
        strFontName = "monospaced";
        strStyle = "plain";
        intStyle = Font.PLAIN;
        intSize = 10;

        // Now make a Font object with the defaults
        fontData = new Font(strFontName,
                            intStyle,
                            intSize);
        }


    /***********************************************************************************************
     * Construct a FontDataType from a font specification string.
     *
     * Syntax: "<code>font=XXXX style=YYYY size=ZZZZ</code>".
     * Style may be one of {Plain, Bold, Italic, BoldItalic}.
     * Size is in the range {1...99}.
     *
     * @param fontspec
     *
     * @throws IllegalArgumentException
     */

    public FontDataType(final String fontspec) throws IllegalArgumentException
        {
        super(DataTypeDictionary.FONT_DATA);

        final String SOURCE = "FontDataType ";

        String strWorkspace;
        final String[] arrayTokens;

        if ((fontspec == null)
            || (EMPTY_STRING.equals(fontspec)))
            {
            throw new IllegalArgumentException(SOURCE + "Null parameters");
            }

        // Try to parse the Font specification from the string
        // e.g. [   font = Font Name    style=  Bold   size = 12  ]
        // Remove leading & trailing spaces, but make sure the first token will be found correctly
        // e.g. [ font = Font Name    style=  Bold   size = 12]
        strWorkspace = SPACE + fontspec.trim();

        // Make it easier to parse by marking the keys differently
        // These could throw PatternSyntaxException
        strWorkspace = strWorkspace.replaceFirst("\\s+font\\s*=\\s*", ":font:");
        strWorkspace = strWorkspace.replaceFirst("\\s+style\\s*=\\s*", ":style:");
        strWorkspace = strWorkspace.replaceFirst("\\s+size\\s*=\\s*", ":size:");
        strWorkspace = strWorkspace.replaceFirst(":", "");

        // Probably unnecessary...
        strWorkspace = strWorkspace.trim();

        // We should end up with [font:Font Name   :style:Bold   :size:12]
        // but the order of tokens is unimportant

        // Delimit the input on ":"
        // This could throw PatternSyntaxException
        arrayTokens = strWorkspace.split(":");

        if ((arrayTokens != null)
            && (arrayTokens.length == 6))
            {
            try
                {
                for (int i = 0;
                     i < arrayTokens.length;
                     i = i + 2)
                    {
                    final String strName;
                    final String strValue;

                    strName = arrayTokens[i];
                    strValue = arrayTokens[i+1];

//                    System.out.println("name token=[" + strName + "]");
//                    System.out.println("value token=[" + strValue + "]");

                    if (strName.equalsIgnoreCase("font"))
                        {
                        // Take the Font Family Name from the next token
                        strFontName = strValue;
                        }
                    else if (strName.equalsIgnoreCase("style"))
                        {
                        final Integer integerTmp;

                        // Take the Font style from the next token
                        integerTmp = hashtableStyles.get(strValue);

                        if (integerTmp != null)
                            {
                            intStyle = integerTmp;
                            strStyle = strValue;
                            }
                        else
                            {
                            intStyle = Font.PLAIN;
                            strStyle = "plain";
                            }
                        }
                    else if (strName.equalsIgnoreCase("size"))
                        {
                        // Take the Font size from the next token
                        intSize = Integer.parseInt(strValue);
                        }
                    else
                        {
                        LOGGER.error("FontDataType cannot parse Font specification [fontspec=" + fontspec + "]");
                        throw new IllegalArgumentException(SOURCE + "Could not parse Font specification [fontspec=" + fontspec + "]");
                        }
                    }

                // Now try to make a Font object with the results of the successful parsing
                fontData = new Font(strFontName,
                                    intStyle,
                                    intSize);
                }

            catch (NullPointerException exception)
                {
                LOGGER.error("FontDataType [exception=" + exception.getMessage() + "]");
                throw new IllegalArgumentException(SOURCE + "Construction error [fontspec=" + fontspec + "]", exception);
                }

            catch (NumberFormatException exception)
                {
                LOGGER.error("FontDataType cannot parse Font size [exception=" + exception.getMessage() + "]");
                throw new IllegalArgumentException(SOURCE + "Could not parse Font size [fontspec=" + fontspec + "]", exception);
                }
            }
        else
            {
            LOGGER.error("FontDataType cannot parse Font specification [fontspec=" + fontspec + "]");
            throw new IllegalArgumentException(SOURCE + "Could not parse Font specification [fontspec=" + fontspec + "]");
            }
        }


    /***********************************************************************************************
     * Get the Font associated with this FontDataType.
     *
     * @return Font
     */

    public final Font getFont()
        {
        return (this.fontData);
        }


    /***********************************************************************************************
     * Get the Name of the Font.
     * The name may contain spaces.
     *
     * @return String
     */

    public final String getFontName()
        {
        return (this.strFontName);
        }


    /***********************************************************************************************
     * Get the Style of the Font.
     * Style may be one of {Plain, Bold, Italic, BoldItalic}.
     *
     * @return String
     */

    public final String getFontStyle()
        {
        return (this.strStyle.toLowerCase());
        }


    /***********************************************************************************************
     * Get the Size of the Font.
     * The Size is in the range {1...n}.
     *
     * @return int
     */

    public final int getFontSize()
        {
        return (this.intSize);
        }


    /***********************************************************************************************
     * Derive a larger Font.
     *
     * @param increase
     *
     * @return Font
     */

    public Font deriveLargerFont(final int increase)
        {
        return (getFont().deriveFont((float)increase));
        }


    /***********************************************************************************************
     * Convert the Font specification back into a string.
     * <p>
     * Syntax: "<code>font=XXXX style=YYYY size=ZZZZ</code>".
     * <p>
     * The name may contain spaces.
     * Style may be one of {Plain, Bold, Italic, BoldItalic}.
     * The Size is in the range {1...n}.
     *
     * @return String
     */

    public final String toString()
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append("font=");
        buffer.append(this.strFontName);
        buffer.append(" style=");
        buffer.append(this.strStyle);
        buffer.append(" size=");
        buffer.append(this.intSize);

        return (buffer.toString());
        }
    }
