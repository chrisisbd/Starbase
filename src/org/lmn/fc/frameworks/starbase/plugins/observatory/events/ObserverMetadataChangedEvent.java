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

import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;

import java.util.EventObject;


/***************************************************************************************************
 * ObserverMetadataChangedEvent.
 */

public final class ObserverMetadataChangedEvent extends EventObject
    {
    private String strKey;
    private MetadataItemState itemState;


    /***********************************************************************************************
     * Encapsulate a ObserverMetadata change.
     * The Metadata Key may be NULL or EMPTY_STRING if more than one item is involved.
     * Indicate the state of the new Metadata.
     *
     * @param eventsource
     * @param metadatakey
     * @param state
     */

    public ObserverMetadataChangedEvent(final Object eventsource,
                                        final String metadatakey,
                                        final MetadataItemState state)
        {
        super(eventsource);

        this.strKey = metadatakey;
        this.itemState = state;
        }


    /***********************************************************************************************
     * Get the Key of the Metadata Item that has been changed.
     * This may be NULL or EMPTY_STRING if more than one item is involved.
     *
     * @return String
     */

    public String getMetadataKey()
        {
        return (this.strKey);
        }


    /***********************************************************************************************
     * Get the ItemState of the ObserverMetadata.
     *
     * @return MetadataItemState
     */

    public MetadataItemState getItemState()
        {
        return (this.itemState);
        }
    }
