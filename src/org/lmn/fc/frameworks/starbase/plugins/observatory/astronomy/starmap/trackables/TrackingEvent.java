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

//------------------------------------------------------------------------------
// Revision History
//
//  30-07-02    LMN created file
//  12-09-06    LMN updated for Starbase!
//
//------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.trackables;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkSingletons;

import java.util.EventObject;

//------------------------------------------------------------------------------

public final class TrackingEvent extends EventObject
                                 implements FrameworkSingletons
    {
    private final Trackable trackAble;
    private boolean boolDebugMode;      // Controls debug messages



    public TrackingEvent(final Object objectsource,
                         final Trackable trackable,
                         final boolean debugmode)
        {
        super(objectsource);
        this.trackAble = trackable;
        this.boolDebugMode = debugmode;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "TrackingEvent created "
                         + "[trackable=" + this.trackAble.getName() + "] ");
        }


    //--------------------------------------------------------------------------
    // Instance Methods
    //--------------------------------------------------------------------------
    // Read the Trackable which caused this Event

    public final Trackable getTrackable()
        {
        return(this.trackAble);
        }


    //--------------------------------------------------------------------------
    // Get the Debug Mode flag

    public final boolean getDebugMode()
        {
        return(this.boolDebugMode);
        }


    //--------------------------------------------------------------------------
    // Set the Debug Mode flag

    public final void setDebugMode(final boolean flag)
        {
        this.boolDebugMode = flag;
        }
    }
