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

//--------------------------------------------------------------------------------------------------
// ActiveChangeEvent object, to notify of changes in Active states of RegistryModel items
//--------------------------------------------------------------------------------------------------
// Revision History
//
//  21-02-00    LMN created file
//
//--------------------------------------------------------------------------------------------------
// RegistryModel package

package org.lmn.fc.common.events;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.time.Chronos;

import java.util.EventObject;

//--------------------------------------------------------------------------------------------------

public final class ActiveChangeEvent extends EventObject
                                     implements FrameworkConstants,
                                                FrameworkStrings
    {
    //----------------------------------------------------------------------------------------------
    // Class FrameworkConstants & Variables
    //----------------------------------------------------------------------------------------------
    // private = 'visible only in this class, visible in all instances'
    // static = 'class variable'
    // final = 'can't be changed' (i.e. makes it into a constant)

    //----------------------------------------------------------------------------------------------
    // Instance FrameworkConstants & Variables
    //----------------------------------------------------------------------------------------------
    // private = 'visible only in this class, visible in all instances'
    // public = 'publically accessible fields'

    private String strSourceName;
    private boolean boolOldValue;
    private boolean boolNewValue;
    private boolean boolDebugMode;      // Controls debug messages

    //----------------------------------------------------------------------------------------------
    // Class Methods
    //----------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------
    // Constructor
    //----------------------------------------------------------------------------------------------

    public ActiveChangeEvent(Object objectsource,
                             String sourcename,
                             boolean oldvalue,
                             boolean newvalue,
                             boolean debugmode)
        {
        super(objectsource);
        this.strSourceName = sourcename;    // The name which appears in the hash table
        this.boolOldValue = oldvalue;
        this.boolNewValue = newvalue;
        this.boolDebugMode = debugmode;

        showDebugMessage("ActiveChangeEvent created for " + strSourceName);
        }


    //----------------------------------------------------------------------------------------------
    // Instance Methods
    //----------------------------------------------------------------------------------------------
    // Read the RegistryModel pathname of the object causing the event

    public String getSourceName()
        {
        return(this.strSourceName);
        }


    //----------------------------------------------------------------------------------------------
    // Get the previous value of the Active flag

    public boolean getOldValue()
        {
        return(this.boolOldValue);
        }


    //----------------------------------------------------------------------------------------------
    // Get the new value of the Active flag

    public boolean getNewValue()
        {
        return(this.boolNewValue);
        }


    //----------------------------------------------------------------------------------------------
    // Get the Debug Mode flag

    public boolean getDebugMode()
        {
        return(this.boolDebugMode);
        }


    //----------------------------------------------------------------------------------------------
    // Set the Debug Mode flag

    public void setDebugMode(boolean flag)
        {
        this.boolDebugMode = flag;
        }


    //----------------------------------------------------------------------------------------------
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
                strSeparator = SPACE;
                }

            System.out.println(Chronos.timeNow()
                               + SPACE
                               + this.getClass().getName()
                               + strSeparator
                               + message);
          }
        }

    //----------------------------------------------------------------------------------------------
    // Events
    //----------------------------------------------------------------------------------------------

  }

//--------------------------------------------------------------------------------------------------
// End of File
