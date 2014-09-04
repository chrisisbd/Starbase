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

package org.lmn.fc.common.utilities.streams;

import java.io.ByteArrayOutputStream;
import java.util.Vector;


public final class ObservableOutputStream extends ByteArrayOutputStream
    {
    public static final String STREAM_SYSTEM_OUT   = "System.out";
    public static final String STREAM_SYSTEM_ERR   = "System.err";

    private final Vector streamObservers = new Vector();
    private final String strStreamName;


    public ObservableOutputStream(final String name)
        {
        super();

        this.strStreamName = name;
        }

    public void addStreamObserver(final StreamObserver o)
        {
        streamObservers.addElement(o);
        }

    public void removeStreamObserver(final StreamObserver o)
        {
        streamObservers.removeElement(o);
        }

    public void write(final byte[] b, final int off, final int len)
        {
        super.write(b, off, len);
        notifyObservers();
        }

    private void notifyObservers()
        {
        for (int i = 0; i < streamObservers.size(); i++)
            {
            ((StreamObserver) streamObservers.elementAt(i)).streamChanged();
            }
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getStreamName()
        {
        return (this.strStreamName);
        }
    }
