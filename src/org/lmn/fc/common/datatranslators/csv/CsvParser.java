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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A very simple CSV parser released under a commercial-friendly license.
 * This just implements splitting a single line into fields.
 * http://opencsv.sourceforge.net/
 *
 * @author Glen Smith
 * @author Rainer Pruy
 */

public final class CsvParser
    {
    private final char separator;

    private final char quotechar;

    private final char escape;

    private final boolean strictQuotes;

    private String pending;


    /**
     * Constructs CsvParser using a comma for the separator.
     */
    public CsvParser()
        {
        this(CsvHelper.CSV_SEPARATOR,
             CsvHelper.QUOTE_CHARACTER,
             CsvHelper.ESCAPE_CHARACTER);
        }


    /**
     * Constructs CsvParser with supplied separator.
     *
     * @param separator the delimiter to use for separating entries.
     */
    public CsvParser(final char separator)
        {
        this(separator,
             CsvHelper.QUOTE_CHARACTER,
             CsvHelper.ESCAPE_CHARACTER);
        }


    /**
     * Constructs CsvParser with supplied separator and quote char.
     *
     * @param separator the delimiter to use for separating entries
     * @param quotechar the character to use for quoted elements
     */
    public CsvParser(final char separator,
                     final char quotechar)
        {
        this(separator,
             quotechar,
             CsvHelper.ESCAPE_CHARACTER);
        }


    /**
     * Constructs CsvParser with supplied separator and quote char.
     *
     * @param separator the delimiter to use for separating entries
     * @param quotechar the character to use for quoted elements
     * @param escape    the character to use for escaping a separator or quote
     */
    public CsvParser(final char separator,
                     final char quotechar,
                     final char escape)
        {
        this(separator,
             quotechar,
             escape,
             CsvHelper.STRICT_QUOTES);
        }


    /**
     * Constructs CsvParser with supplied separator and quote char.
     * Allows setting the "strict quotes" flag
     *
     * @param separator    the delimiter to use for separating entries
     * @param quotechar    the character to use for quoted elements
     * @param escape       the character to use for escaping a separator or quote
     * @param strictQuotes if true, characters outside the quotes are ignored
     */
    public CsvParser(final char separator,
                     final char quotechar,
                     final char escape,
                     final boolean strictQuotes)
        {
        this.separator = separator;
        this.quotechar = quotechar;
        this.escape = escape;
        this.strictQuotes = strictQuotes;
        }


    /**
     * @return true if something was left over from last call(s)
     */
    public boolean isPending()
        {
        return pending != null;
        }


    public String[] parseLineMulti(final String nextLine) throws
                                                    IOException
        {
        return parseLine(nextLine,
                         true);
        }


    public String[] parseLine(final String nextLine) throws
                                               IOException
        {
        return parseLine(nextLine,
                         false);
        }


    /**
     * Parses an incoming String and returns an array of elements.
     *
     * @param nextLine the string to parse
     * @param multi
     *
     * @return the comma-tokenized list of elements, or null if nextLine is null
     *
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(final String nextLine,
                               final boolean multi) throws
                                              IOException
        {

        if (!multi && pending != null)
            {
            pending = null;
            }

        if (nextLine == null)
            {
            if (pending != null)
                {
                final String s = pending;
                pending = null;
                return new String[]{s};
                }
            else
                {
                return null;
                }
            }

        final List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuilder sb = new StringBuilder(CsvHelper.INITIAL_READ_SIZE);
        boolean inQuotes = false;
        if (pending != null)
            {
            sb.append(pending);
            pending = null;
            inQuotes = true;
            }
        for (int i = 0;
             i < nextLine.length();
             i++)
            {

            final char c = nextLine.charAt(i);
            if (c == this.escape)
                {
                if (isNextCharacterEscapable(nextLine,
                                             inQuotes,
                                             i))
                    {
                    sb.append(nextLine.charAt(i + 1));
                    i++;
                    }
                }
            else if (c == quotechar)
                {
                if (isNextCharacterEscapedQuote(nextLine,
                                                inQuotes,
                                                i))
                    {
                    sb.append(nextLine.charAt(i + 1));
                    i++;
                    }
                else
                    {
                    inQuotes = !inQuotes;
                    // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                    if (!strictQuotes)
                        {
                        if (i > 2 //not on the beginning of the line
                                && nextLine.charAt(i - 1) != this.separator //not at the beginning of an escape sequence
                                && nextLine.length() > (i + 1) &&
                                nextLine.charAt(i + 1) != this.separator //not at the	end of an escape sequence
                                )
                            {
                            sb.append(c);
                            }
                        }
                    }
                }
            else if (c == separator && !inQuotes)
                {
                tokensOnThisLine.add(sb.toString());
                sb = new StringBuilder(CsvHelper.INITIAL_READ_SIZE); // start work on next token
                }
            else
                {
                if (!strictQuotes || inQuotes)
                    {
                    sb.append(c);
                    }
                }
            }
        // line is done - check status
        if (inQuotes)
            {
            if (multi)
                {
                // continuing a quoted section, re-append newline
                sb.append("\n");
                pending = sb.toString();
                sb = null; // this partial content is not to be added to field list yet
                }
            else
                {
                throw new IOException("Un-terminated quoted field at end of CSV line");
                }
            }
        if (sb != null)
            {
            tokensOnThisLine.add(sb.toString());
            }
        return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);

        }


    /**
     * precondition: the current character is a quote or an escape
     *
     * @param nextLine the current line
     * @param inQuotes true if the current context is quoted
     * @param i        current index in line
     *
     * @return true if the following character is a quote
     */
    private boolean isNextCharacterEscapedQuote(final String nextLine,
                                                final boolean inQuotes,
                                                final int i)
        {
        return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
                && nextLine.length() > (i + 1)  // there is indeed another character to check.
                && nextLine.charAt(i + 1) == quotechar;
        }


    /**
     * precondition: the current character is an escape
     *
     * @param nextLine the current line
     * @param inQuotes true if the current context is quoted
     * @param i        current index in line
     *
     * @return true if the following character is a quote
     */
    private boolean isNextCharacterEscapable(final String nextLine,
                                             final boolean inQuotes,
                                             final int i)
        {
        return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
                && nextLine.length() > (i + 1)  // there is indeed another character to check.
                && (nextLine.charAt(i + 1) == quotechar || nextLine.charAt(i+1) == this.escape);
	}
}
