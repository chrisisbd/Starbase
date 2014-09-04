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

package org.lmn.fc.common.utilities.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamCopier extends Thread
    {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public StreamCopier(final InputStream input,
                        final OutputStream output)
        {
        inputStream = input;
        outputStream = output;
        }

    public final void run()
        {
        try
            {
            final byte[] buffer = new byte[10000];

            while (true)
                {
                final int intBytesRead;

                // There is an attempt to read at least one byte.
                // If no byte is available because the stream is at end of file,
                // the value -1 is returned;
                // otherwise, at least one byte is read and stored into the buffer
                intBytesRead = inputStream.read(buffer);

                if (intBytesRead == -1)
                    {
                    break;
                    }

                outputStream.write(buffer, 0, intBytesRead);
                }
            }

        catch (IOException exception)
            {

            }

        }
    }
