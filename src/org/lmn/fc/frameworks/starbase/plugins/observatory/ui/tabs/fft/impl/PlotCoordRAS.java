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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.impl;


public class PlotCoordRAS
    {
    private float xstretch = 1;
    private float ystretch = 1;
    private final float xshift;
    private final float yshift;
    private final float ywholeScreen;
    private final int inset;             /* How far from the edge the display is */


    public PlotCoordRAS(final float xmax,
                        final float ymax,
                        final float xmin,
                        final float ymin,
                        final int width,
                        final int height,
                        final int insetin)
        {

        inset = insetin;

        xstretch = (width - (2 * inset)) / (xmax - xmin);
        ystretch = (height - (2 * inset)) / (ymax - ymin);

        xshift = -xmin;
        yshift = -ymin;

        ywholeScreen = ymax - ymin;
        }


    /* Return the X and Y coordinates on the canvas. */
    public int xcoord(final float x)
        {
        final float xcoordFloat;

        xcoordFloat = (x + xshift) * xstretch + inset;
        return (Math.round(xcoordFloat));
        }


    public int ycoord(final float y)
        {
        final float ycoordFloat;

        ycoordFloat = (ywholeScreen - (y + yshift)) * ystretch + inset;
        return (Math.round(ycoordFloat));
        }
    }