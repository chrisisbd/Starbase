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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.maps;

import javax.swing.*;


/***************************************************************************************************
 * A utility class to encapsulate a PointOfInterest to be drawn on the Map
 * at the specified (x,y) location, using the specified ImageIcon.
 */

public final class MappedPointOfInterest
    {
    final int intX;
    final int intY;
    final ImageIcon iconPOI;


    /***********************************************************************************************
     * MappedPointOfInterest.
     *
     * @param X
     * @param Y
     * @param icon
     */

    MappedPointOfInterest(final int X,
                          final int Y,
                          final ImageIcon icon)
        {
        intX = X;
        intY = Y;
        iconPOI = icon;
        }


    /***********************************************************************************************
     * Get the MappedPointOfInterest X pixel coordinate.
     *
     * @return int
     */

    public final int getX()
        {
        return (this.intX);
        }


    /***********************************************************************************************
     * Get the MappedPointOfInterest Y pixel coordinate.
     *
     * @return int
     */

    public final int getY()
        {
        return (this.intY);
        }


    /***********************************************************************************************
     * Get the PointOfInterest ImageIcon.
     *
     * @return ImageIcon
     */

    public final ImageIcon getPOIIcon()
        {
        return (this.iconPOI);
        }
    }
