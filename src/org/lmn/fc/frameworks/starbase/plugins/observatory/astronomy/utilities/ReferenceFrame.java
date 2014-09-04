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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * ReferenceFrame.
 */

public enum ReferenceFrame
    {
    GEOCENTRIC  (0, "Geocentric"),
    TOPCENTRIC  (1, "Topocentric");


    private final int intTypeID;
    private final String strName;


    /***********************************************************************************************
     * Get the ReferenceFrame enum corresponding to the specified ReferenceFrame name.
     *
     * @param name
     *
     * @return ReferenceFrame
     */

    public static ReferenceFrame getReferenceFrameForName(final String name)
        {
        ReferenceFrame referenceFrame;

        //LOGGER.debug("ReferenceFrame.getReferenceFrameForName() [name=" + name + "]");

        referenceFrame = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final ReferenceFrame[] referenceFrames;
            boolean boolFoundIt;

            referenceFrames = ReferenceFrame.values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < referenceFrames.length);
                 i++)
                {
                final ReferenceFrame referenceFrameTest;

                referenceFrameTest = referenceFrames[i];

                if (name.equals(referenceFrameTest.getName()))
                    {
                    referenceFrame = referenceFrameTest;
                    boolFoundIt = true;
                    }
                }
            }

        return (referenceFrame);
        }


    /***********************************************************************************************
     * ReferenceFrame.
     *
     * @param typedid
     * @param name
     */

    private ReferenceFrame(final int typedid,
                           final String name)
        {
        intTypeID = typedid;
        strName = name;
        }


    /***********************************************************************************************
     * Get the TypeID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intTypeID);
        }


    /***********************************************************************************************
     * Get the ReferenceFrame name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the ReferenceFrame name.
     *
     * @return
     */

    public String toString()
        {
        return (this.strName);
        }
    }
