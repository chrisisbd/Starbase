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
// StarMap Utilities
//------------------------------------------------------------------------------
// Revision History
//
//  28-09-02    LMN created file
//  08-10-02    LMN added transformRaDecToViewportXY()
//  28-10-02    LMN added transformViewportXYtoAzEl()
//  07-06-08    LMN making major changes for new multiple collections!
//
//------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * StarMapHelper.
 */

public final class StarMapHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons
    {
    /***********************************************************************************************
     * Set the colour (or brightness) of ALL of the objects in this Plugin.
     * This will override the colour used in the constructor.
     *
     * @param plugin
     * @param colour
     */

    public static void setPluginColour(final StarMapPlugin plugin,
                                       final Color colour)
        {
        // Check that we have some data loaded into the plugin!
        if ((plugin != null)
            && (colour != null))
            {
            final Vector<Vector<StarMapPointInterface>> wholeCollection;

            wholeCollection = plugin.getStarMapPoints();

            if ((wholeCollection != null)
                && (!wholeCollection.isEmpty()))
                {
                final Iterator<Vector<StarMapPointInterface>> iterWholeCollection;

                iterWholeCollection = wholeCollection.iterator();

                while ((iterWholeCollection != null)
                    && (iterWholeCollection.hasNext()))
                    {
                    final Vector<StarMapPointInterface> singleCollection;

                    singleCollection = iterWholeCollection.next();

                    // See if we can recolour one set from the collection
                    if ((singleCollection != null)
                        && (!singleCollection.isEmpty()))
                        {
                        final Iterator<StarMapPointInterface> iterSingleCollection;

                        iterSingleCollection = singleCollection.iterator();

                        while ((iterSingleCollection != null)
                            && (iterSingleCollection.hasNext()))
                            {
                            final StarMapPointInterface point;

                            point = iterSingleCollection.next();

                            if (point != null)
                                {
                                point.setColour(colour);
                                }
                            }
                        }
                    }

                // Show the colour changes
                plugin.getHostStarMap().refreshStarMap();
                }
            }
        }
    }
