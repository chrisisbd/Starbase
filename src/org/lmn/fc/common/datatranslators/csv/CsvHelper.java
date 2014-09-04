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

package org.lmn.fc.common.datatranslators.csv;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * CsvHelper.
 * http://opencsv.sourceforge.net/
 * See: http://en.wikipedia.org/wiki/Comma-separated_values
 */

public final class CsvHelper implements FrameworkConstants,
                                        FrameworkStrings
    {
    // The default separator to use
    public static final char CSV_SEPARATOR = ',';

    // The default quote character to use
    public static final char QUOTE_CHARACTER = '"';

    // The character used for escaping quotes
    public static final char ESCAPE_CHARACTER = '"';
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';

    // The default strict quote behavior to use if none is supplied to the constructor
    public static final boolean STRICT_QUOTES = false;

    // Default line terminator uses platform encoding
    public static final String LINE_END = "\r\n";

    public static final int INITIAL_STRING_SIZE = 128;

    public static final int INITIAL_READ_SIZE = 128;

    // The default line to start reading
    public static final int DEFAULT_SKIP_LINES = 0;


    /***********************************************************************************************
     * Makes one line of CSV values.
     *
     * @param line a string array with each element as a separate entry
     *
     * @return StringBuilder
     */

    public static StringBuilder makeCsvLine(final String[] line)
        {
        final StringBuilder builder;

        builder = new StringBuilder(INITIAL_STRING_SIZE);

        if (line != null)
            {
            for (int i = 0;
                 i < line.length;
                 i++)
                {
                final String strToken;

                if (i != 0)
                    {
                    builder.append(CSV_SEPARATOR);
                    }

                strToken = line[i];

                if (strToken != null)
                    {
                    makeCsvToken(strToken, builder);
                    }
                }

            builder.append(LINE_END);
            }

        return (builder);
        }


    /***********************************************************************************************
     * Make one CSV token from the specified String.
     *
     * @param token
     * @param builder
     *
     * @return StringBuilder
     */

    private static StringBuilder makeCsvToken(final String token,
                                              final StringBuilder builder)
        {
        if (isTokenQuotable(token))
            {
            builder.append(QUOTE_CHARACTER);
            }

        if (tokenContainsSpecialCharacters(token))
            {
            builder.append(processToken(token));
            }
        else
            {
            builder.append(token);
            }

        if (isTokenQuotable(token))
            {
            builder.append(QUOTE_CHARACTER);
            }

        return (builder);
        }


    /***********************************************************************************************
     * Build one CSV token from the specified String.
     *
     * @param token
     *
     * @return StringBuilder
     */

    public static String buildCsvToken(final String token)
        {
        final StringBuilder builder;

        builder = new StringBuilder(INITIAL_STRING_SIZE);

        if (isTokenQuotable(token))
            {
            builder.append(QUOTE_CHARACTER);
            }

        if (tokenContainsSpecialCharacters(token))
            {
            builder.append(processToken(token));
            }
        else
            {
            builder.append(token);
            }

        if (isTokenQuotable(token))
            {
            builder.append(QUOTE_CHARACTER);
            }

        return (builder.toString());
        }


    /***********************************************************************************************
     * Check to see if there is any reason to enclose the entire token in Quotes.
     * Fields may always be enclosed within double-quote characters, whether necessary or not.
     * Fields with embedded commas must be enclosed within double-quote characters.
     * Fields with leading or trailing spaces must be enclosed within double-quote characters.
     * Fields with embedded line breaks must be enclosed within double-quote characters.
     * A line break within an element must be preserved.
     * Fields with embedded double-quote characters must be enclosed within double-quote characters.
     *
     * @param token
     * @return boolean
     */

    private static boolean isTokenQuotable(final String token)
        {
        return ((token.indexOf(CSV_SEPARATOR) != -1)
                    || (token.indexOf("\r") != -1)
                    || (token.indexOf("\n") != -1)
                    || (token.indexOf(QUOTE_CHARACTER) != -1)
                    || (token.startsWith(SPACE))
                    || (token.endsWith(SPACE)));
        }


    /***********************************************************************************************
     * See if one CSV token contains the Escape character or Quotes.
     *
     * Fields with embedded double-quote characters must be enclosed within double-quote characters
     * and each of the embedded double-quote characters must be represented by a pair of double-quote characters.
     *
     * @param token
     *
     * @return boolean
     */

    private static boolean tokenContainsSpecialCharacters(final String token)
        {
        boolean boolSpecial;

        // Fields with embedded double-quote characters must be enclosed within double-quote characters
        // and each of the embedded double-quote characters must be represented by a pair of double-quote characters
        boolSpecial = (token.indexOf(QUOTE_CHARACTER) != -1);

        // In this implementation, the QUOTE character is also the ESCAPE character
        boolSpecial = boolSpecial && (token.indexOf(ESCAPE_CHARACTER) != -1);

        return (boolSpecial);
        }


    /***********************************************************************************************
     * Process one CSV token, character by character.
     *
     * @param token
     *
     * @return StringBuilder
     */

    private static StringBuilder processToken(final String token)
        {
        final StringBuilder builder;

        builder = new StringBuilder(INITIAL_STRING_SIZE);

        for (int j = 0;
             j < token.length();
             j++)
            {
            final char nextChar;

            nextChar = token.charAt(j);

            // Fields with embedded double-quote characters must be enclosed within double-quote characters
            // and each of the embedded double-quote characters must be represented by a pair of double-quote characters
            // In this implementation, the QUOTE character is also the ESCAPE character

            if (nextChar == QUOTE_CHARACTER)
                {
                // An escaped quote is two consecutive quote characters
                builder.append(ESCAPE_CHARACTER);
                builder.append(nextChar);
                }
            else if (nextChar == ESCAPE_CHARACTER)
                {
                // An escaped escape is two consecutive escape characters
                builder.append(ESCAPE_CHARACTER);
                builder.append(nextChar);
                }
            else
                {
                // Just pass anything else straight through
                builder.append(nextChar);
                }
            }

        return (builder);
        }
    }
