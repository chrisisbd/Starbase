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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;


/***************************************************************************************************
 * ChannelSelectionMode.
 */

public enum ChannelSelectionMode
    {
    OFF             (0, "Off", 0.0),

    X0_001          (1, "x 0.001", 0.001),
    X0_002          (2, "x 0.002", 0.002),
    X0_005          (3, "x 0.005", 0.005),
    X0_01           (4, "x 0.01", 0.01),
    X0_02           (5, "x 0.02", 0.02),
    X0_05           (6, "x 0.05", 0.05),
    X0_10           (7, "x 0.1", 0.1),
    X0_20           (8, "x 0.2", 0.2),
    X0_50           (9, "x 0.5", 0.5),

    X1              (10, "x 1", 1.0),

    X2              (11, "x 2", 2.0),
    X5              (12, "x 5", 5.0),
    X10             (13, "x 10", 10.0),
    X20             (14, "x 20", 20.0),
    X50             (15, "x 50", 50.0),
    X100            (16, "x 100", 100.0),
    X200            (17, "x 200", 200.0),
    X500            (18, "x 500", 500.0),
    X1000           (19, "x 1000", 1000.0);


    private final int intIndex;
    private final String strMode;
    private final double dblMultiplier;


    /***********************************************************************************************
     * Construct a ChannelSelectionMode.
     *
     * @param index
     * @param mode
     * @param multiplier
     */

    private ChannelSelectionMode(final int index,
                                 final String mode,
                                 final double multiplier)
        {
        this.intIndex = index;
        this.strMode = mode;
        this.dblMultiplier = multiplier;
        }


    /***********************************************************************************************
     * Get the index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the ChannelSelection Mode.
     *
     * @return String
     */

    public String getChannelSelectionMode()
        {
        return(this.strMode);
        }


    /***********************************************************************************************
     * Get the Channel Multiplier.
     *
     * @return double
     */

    public double getMultiplier()
        {
        return(this.dblMultiplier);
        }


    /***********************************************************************************************
     * Get the ChannelSelectionMode as a String.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strMode);
        }
    }