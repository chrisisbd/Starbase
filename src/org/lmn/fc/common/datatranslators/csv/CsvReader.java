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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * A very simple CSV reader released under a commercial-friendly license.
 * http://opencsv.sourceforge.net/
 *
 * @author Glen Smith
 */

public final class CsvReader implements Closeable
    {
    private final BufferedReader bufferedReader;

    private boolean hasNext = true;

    private final CsvParser parser;

    private final int skipLines;

    private boolean linesSkipped;


    /***********************************************************************************************
     * Constructs CsvReader using a comma for the separator.
     *
     * @param reader the reader to an underlying CSV source.
     */

    public CsvReader(final Reader reader)
        {
        this(reader,
             CsvHelper.CSV_SEPARATOR,
             CsvHelper.QUOTE_CHARACTER,
             CsvHelper.ESCAPE_CHARACTER);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator.
     *
     * @param reader    the reader to an underlying CSV source.
     * @param separator the delimiter to use for separating entries.
     */

    public CsvReader(final Reader reader,
                     final char separator)
        {
        this(reader,
             separator,
             CsvHelper.QUOTE_CHARACTER,
             CsvHelper.ESCAPE_CHARACTER);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator and quote char.
     *
     * @param reader    the reader to an underlying CSV source.
     * @param separator the delimiter to use for separating entries
     * @param quotechar the character to use for quoted elements
     */

    public CsvReader(final Reader reader,
                     final char separator,
                     final char quotechar)
        {
        this(reader,
             separator,
             quotechar,
             CsvHelper.ESCAPE_CHARACTER,
             CsvHelper.DEFAULT_SKIP_LINES,
             CsvHelper.STRICT_QUOTES);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator, quote char and quote handling
     * behavior.
     *
     * @param reader       the reader to an underlying CSV source.
     * @param separator    the delimiter to use for separating entries
     * @param quotechar    the character to use for quoted elements
     * @param strictQuotes sets if characters outside the quotes are ignored
     */

    public CsvReader(final Reader reader,
                     final char separator,
                     final char quotechar,
                     final boolean strictQuotes)
        {
        this(reader,
             separator,
             quotechar,
             CsvHelper.ESCAPE_CHARACTER,
             CsvHelper.DEFAULT_SKIP_LINES,
             strictQuotes);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator and quote char.
     *
     * @param reader    the reader to an underlying CSV source.
     * @param separator the delimiter to use for separating entries
     * @param quotechar the character to use for quoted elements
     * @param escape    the character to use for escaping a separator or quote
     */

    public CsvReader(final Reader reader,
                     final char separator,
                     final char quotechar,
                     final char escape)
        {
        this(reader,
             separator,
             quotechar,
             escape,
             CsvHelper.DEFAULT_SKIP_LINES,
             CsvHelper.STRICT_QUOTES);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator and quote char.
     *
     * @param reader    the reader to an underlying CSV source.
     * @param separator the delimiter to use for separating entries
     * @param quotechar the character to use for quoted elements
     * @param line      the line number to skip for start reading
     */

    public CsvReader(final Reader reader,
                     final char separator,
                     final char quotechar,
                     final int line)
        {
        this(reader,
             separator,
             quotechar,
             CsvHelper.ESCAPE_CHARACTER,
             line,
             CsvHelper.STRICT_QUOTES);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator and quote char.
     *
     * @param reader    the reader to an underlying CSV source.
     * @param separator the delimiter to use for separating entries
     * @param quotechar the character to use for quoted elements
     * @param escape    the character to use for escaping a separator or quote
     * @param line      the line number to skip for start reading
     */

    public CsvReader(final Reader reader,
                     final char separator,
                     final char quotechar,
                     final char escape,
                     final int line)
        {
        this(reader,
             separator,
             quotechar,
             escape,
             line,
             CsvHelper.STRICT_QUOTES);
        }


    /***********************************************************************************************
     * Constructs CsvReader with supplied separator and quote char.
     *
     * @param reader       the reader to an underlying CSV source.
     * @param separator    the delimiter to use for separating entries
     * @param quotechar    the character to use for quoted elements
     * @param escape       the character to use for escaping a separator or quote
     * @param line         the line number to skip for start reading
     * @param strictQuotes sets if characters outside the quotes are ignored
     */

    public CsvReader(final Reader reader,
                     final char separator,
                     final char quotechar,
                     final char escape,
                     final int line,
                     final boolean strictQuotes)
        {
        this.bufferedReader = new BufferedReader(reader);
        this.parser = new CsvParser(separator,
                                    quotechar,
                                    escape,
                                    strictQuotes);
        this.skipLines = line;
        }


    /***********************************************************************************************
     * Reads the entire file into a List with each element being a String[] of tokens.
     *
     * @return a List of String[], with each String[] representing a line of the file.
     *
     * @throws IOException if bad things happen during the read
     */

    public List<String[]> readAll() throws IOException
        {
        final List<String[]> allElements;

        allElements = new ArrayList<String[]>(10000);

        while (hasNext)
            {
            final String[] nextLineAsTokens;

            nextLineAsTokens = readNext();

            if (nextLineAsTokens != null)
                {
                allElements.add(nextLineAsTokens);
                }
            }

        return (allElements);
        }


    /***********************************************************************************************
     * Reads the next line from the buffer and converts to a string array.
     *
     * @return a string array with each comma-separated element as a separate entry.
     *
     * @throws IOException if bad things happen during the read
     */

    public String[] readNext() throws IOException
        {
        String[] strResult;

        strResult = null;

        do
            {
            final String nextLine;
            final String[] r;

            nextLine = getNextLine();

            if (!hasNext)
                {
                // TODO should throw if still pending?
                return (strResult);
                }

            r = parser.parseLineMulti(nextLine);

            if (r.length > 0)
                {
                if (strResult == null)
                    {
                    strResult = r;
                    }
                else
                    {
                    final String[] t;

                    t = new String[strResult.length + r.length];

                    System.arraycopy(strResult,
                                     0,
                                     t,
                                     0,
                                     strResult.length);
                    System.arraycopy(r,
                                     0,
                                     t,
                                     strResult.length,
                                     r.length);
                    strResult = t;
                    }
                }
            }
        while (parser.isPending());

        return (strResult);
        }


    /***********************************************************************************************
     * Reads the next line from the file.
     *
     * @return the next line from the file without trailing newline
     *
     * @throws IOException if bad things happen during the read
     */

    private String getNextLine() throws IOException
        {
        final String nextLine;

        if (!this.linesSkipped)
            {
            for (int i = 0;
                 i < skipLines;
                 i++)
                {
                bufferedReader.readLine();
                }

            this.linesSkipped = true;
            }

        nextLine = bufferedReader.readLine();

        if (nextLine == null)
            {
            hasNext = false;
            }

        return (hasNext ? nextLine : null);
        }


    /***********************************************************************************************
     * Closes the underlying reader.
     *
     * @throws IOException if the close fails
     */

    public void close() throws IOException
        {
        bufferedReader.close();
        }
    }
