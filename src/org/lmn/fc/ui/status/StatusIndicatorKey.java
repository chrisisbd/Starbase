// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

package org.lmn.fc.ui.status;

public enum StatusIndicatorKey
    {
    STATUS (0),
    LONGITUDE (1),
    LATITUDE (2),
    HASL (3),
    DATE (4),
    TIME (5),
    TIMEZONE (6),
    USER (7),
    LOCALE (8),
    REGISTRY (9),
    DATASTORE (10),
    MEMORY (11),
    GARBAGE (12);


    private final int intID;


    private StatusIndicatorKey(final int id)
        {
        intID = id;
        }

    public int getID()
        {
        return (this.intID);
        }

    }
