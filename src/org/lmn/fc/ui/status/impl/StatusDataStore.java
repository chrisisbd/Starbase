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

package org.lmn.fc.ui.status.impl;

import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * StatusIndicator for the DataStore.
 */

public final class StatusDataStore extends StatusIndicator
    {
    public static final int DATASTORE_WIDTH = 50;


    /***********************************************************************************************
     * Construct a StatusDataStore.
     *
     * @param colour
     * @param font
     * @param text
     * @param tooltip
     */

    public StatusDataStore(final ColourInterface colour,
                           final FontInterface font,
                           final String text,
                           final String tooltip)
        {
        super(colour, font, text, tooltip);

        setMinimumSize(new Dimension(DATASTORE_WIDTH, STATUS_HEIGHT));
        setPreferredSize(new Dimension(DATASTORE_WIDTH, STATUS_HEIGHT));
        setMaximumSize(new Dimension(DATASTORE_WIDTH << 1, STATUS_HEIGHT));
        setHorizontalAlignment(JLabel.CENTER);

        if (DATABASE.getDatabaseOptions().getDataStore() != null)
            {
            // Initialise the DataStore display
            setText(DATABASE.getDatabaseOptions().getDataStore().getName());
            }
        else
            {
            setText(EMPTY_STRING);
            }
        }
    }
