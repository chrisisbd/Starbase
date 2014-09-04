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

package org.lmn.fc.frameworks.starbase.plugins.observatory.events;

import java.util.EventObject;


/***************************************************************************************************
 * DatasetChangedEvent.
 */

public final class DatasetChangedEvent extends EventObject
    {
    private final int intSeriesCount;
    private final int intItemCountSeries0;


    /***********************************************************************************************
     * Encapsulate a Dataset change.
     *
     * @param eventsource
     * @param seriescount
     * @param itemcount0
     */

    public DatasetChangedEvent(final Object eventsource,
                               final int seriescount,
                               final int itemcount0)
        {
        super(eventsource);

        this.intSeriesCount = seriescount;
        this.intItemCountSeries0 = itemcount0;
        }


    /***********************************************************************************************
     * Get the new Series Count for the Dataset that changed.
     *
     * @return int
     */

    public int getSeriesCount()
        {
        return (this.intSeriesCount);
        }


    /***********************************************************************************************
     * Get the new Item Count of Series 0 for the Dataset that changed.
     *
     * @return int
     */

    public int getItemCountSeries0()
        {
        return (this.intItemCountSeries0);
        }
    }
