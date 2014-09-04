// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft;

import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * DisplayMode.
 */

public enum FFTDisplayMode
    {
    SINGLE_SPECTRUM     (0, "Spectrum",             true),
    STAGGERED_SPECTRA   (1, "Staggered Spectra",    false),
    WATERFALL           (2, "Waterfall",            false);


    public static final String TOOLTIP = "Display Mode";

    private final int intIndex;
    private final String strName;
    private final boolean boolEnabled;


    /***********************************************************************************************
     * Get the DisplayMode enum corresponding to the specified DisplayMode name.
     * Return NULL if the DisplayMode name is not found.
     *
     * @param name
     *
     * @return DisplayMode
     */

    public static FFTDisplayMode getDisplayModeForName(final String name)
        {
        FFTDisplayMode displayMode;

        displayMode = null;

        if ((name != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(name)))
            {
            final FFTDisplayMode[] modes;
            boolean boolFoundIt;

            modes = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < modes.length);
                 i++)
                {
                final FFTDisplayMode length;

                length = modes[i];

                if (name.equals(length.getName()))
                    {
                    displayMode = length;
                    boolFoundIt = true;
                    }
                }
            }

        return (displayMode);
        }


    /***********************************************************************************************
     * Construct a DisplayMode.
     *
     * @param index
     * @param name
     * @param enabled
     */

    private FFTDisplayMode(final int index,
                           final String name,
                           final boolean enabled)
        {
        this.intIndex = index;
        this.strName = name;
        this.boolEnabled = enabled;
        }


    /***********************************************************************************************
     * Get the DisplayMode index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the DisplayMode name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Indicate if this DisplayMode is enabled.
     *
     * @return boolean
     */

    public boolean isEnabled()
        {
        return (this.boolEnabled);
        }


    /***********************************************************************************************
     * Get the DisplayMode as a String.
     *
     * @return String
     */

    public String toString()
        {
        if (isEnabled())
            {
            return (this.strName);
            }
        else
            {
            return (FrameworkStrings.HTML_PREFIX_ITALIC
                        + this.strName
                        + FrameworkStrings.HTML_SUFFIX_ITALIC);
            }
        }
    }