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

//------------------------------------------------------------------------------
// KeypadMachine Event
//------------------------------------------------------------------------------
// Revision History
//
//  21-03-00    LMN created file
//
//------------------------------------------------------------------------------
// Widgets package

package org.lmn.fc.ui.widgets.impl;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.utilities.time.Chronos;

import java.util.EventObject;

//------------------------------------------------------------------------------

public class KeypadEvent extends EventObject
    {
    private String strSourceName;
    private String strEventData;
    private boolean boolDebugMode;      // Controls debug messages

    public KeypadEvent(Object objectsource,
                       String sourcename,
                       String eventdata,
                       boolean debugmode)
        {
        super(objectsource);

        this.strSourceName = sourcename;
        this.strEventData = eventdata;
        this.boolDebugMode = debugmode;
        }


    //--------------------------------------------------------------------------
    // Instance Methods
    //--------------------------------------------------------------------------
    // Read the ObjectModel pathname of the object causing the event

    public String getSourceName()
        {
        return(this.strSourceName);
        }


    //--------------------------------------------------------------------------
    // Read the EventData which caused this Event

    public String getEventData()
        {
        return(this.strEventData);
        }


    //--------------------------------------------------------------------------
    // Get the Debug Mode flag

    public boolean getDebugMode()
        {
        return(this.boolDebugMode);
        }


    //--------------------------------------------------------------------------
    // Set the Debug Mode flag

    public void setDebugMode(boolean flag)
        {
        this.boolDebugMode = flag;
        }


    //--------------------------------------------------------------------------
    // Show a debug message

    private void showDebugMessage(String message)
        {
        String strSeparator;

        if (boolDebugMode)
            {
              if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = " ";
                }

            System.out.println(Chronos.timeNow() + " "
                               + this.getClass().getName()
                               + strSeparator + message);
          }
        }
    }

//------------------------------------------------------------------------------
// End of File
