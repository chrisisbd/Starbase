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

package org.lmn.fc.ui;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * UIComponentState.
 */

public enum UIComponentState
    {
    CREATED         (0, "Created"),
    INITIALISED     (1, "Initialised"),
    RUNNING         (2, "Running"),
    STOPPED         (3, "Stopped"),
    DISPOSED        (4, "Disposed");


    private final int intIndex;
    private final String strName;


    /***********************************************************************************************
     * Get the UIComponentState enum corresponding to the specified UIComponentState name.
     * Return NULL if the UIComponentState name is not found.
     *
     * @param name
     *
     * @return UIComponentState
     */

    public static UIComponentState getUIComponentStateForName(final String name)
        {
        UIComponentState uiState;

        uiState = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final UIComponentState[] states;
            boolean boolFoundIt;

            states = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < states.length);
                 i++)
                {
                final UIComponentState state;

                state = states[i];

                if (name.equals(state.getName()))
                    {
                    uiState = state;
                    boolFoundIt = true;
                    }
                }
            }

        return (uiState);
        }


    /***********************************************************************************************
     * Construct a UIComponentState.
     *
     * @param index
     * @param name
     */

    private UIComponentState(final int index,
                             final String name)
        {
        intIndex = index;
        strName = name;
        }


    /***********************************************************************************************
     * Get the UIComponentState index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the UIComponentState name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the UIComponentState as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }