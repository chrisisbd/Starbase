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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * UpdateTarget.
 */

public enum UpdateTarget
    {
    FRAMEWORK       (0, "Framework",    "Framework"),
    OBSERVATORY     (1, "Observatory",  "Observatory"),
    UPDATE_ALL      (1, "UpdateAll",    "Update All"),
    NO_UPDATE       (1, "NoUpdate",     "No Update");


    private final int intValue;
    private final String strName;
    private final String strTarget;


    /***********************************************************************************************
     * Get the UpdateTarget enum corresponding to the specified UpdateTarget name.
     * Return NULL if the UpdateTarget name is not found.
     *
     * @param name
     *
     * @return UpdateTarget
     */

    public static UpdateTarget getUpdateTargetForName(final String name)
        {
        UpdateTarget target;

        target = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final UpdateTarget[] targets;
            boolean boolFoundIt;

            targets = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < targets.length);
                 i++)
                {
                final UpdateTarget updateTarget;

                updateTarget = targets[i];

                if (name.equals(updateTarget.getName()))
                    {
                    target = updateTarget;
                    boolFoundIt = true;
                    }
                }
            }

        return (target);
        }


    /***********************************************************************************************
     * UpdateTarget.
     *
     * @param value
     * @param name
     * @param target
     */

    private UpdateTarget(final int value,
                         final String name,
                         final String target)
        {
        intValue = value;
        strName = name;
        strTarget = target;
        }


    /***********************************************************************************************
     * Get the TypeID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intValue);
        }


    /***********************************************************************************************
     * Get the Target name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Target text.
     *
     * @return String
     */

    public String getTarget()
        {
        return (this.strTarget);
        }


    /***********************************************************************************************
     * Get the Target name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
