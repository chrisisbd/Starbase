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

package org.lmn.fc.ui.choosers.impl;


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.choosers.utilities.MetadataKeyDialog;

import java.awt.*;


/***************************************************************************************************
 * MetadataKeyChooser.
 */

public class MetadataKeyChooser extends AbstractChooser
                                implements ChooserInterface
    {
    // String Resources
    private static final String DIALOG_TITLE = "Select a Metadata Key";
    private static final String DIALOG_LABEL = "Current Metadata Keys";


    /***********************************************************************************************
     * MetadataKeyChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public MetadataKeyChooser(final ObservatoryInstrumentInterface obsinstrument,
                              final FontInterface font,
                              final ColourInterface colourforeground,
                              final String defaultvalue)
        {
        super(obsinstrument, font, colourforeground, defaultvalue);
        }


    /***********************************************************************************************
     * Show the Chooser, centred on the specified Component.
     *
     * @param component
     */

    public void showChooser(final Component component)
        {
        final String SOURCE = "MetadataKeyChooser.showChooser() ";

        final String strResult;

        strResult = MetadataKeyDialog.showDialog(component,
                                                 null,
                                                 DIALOG_LABEL,
                                                 DIALOG_TITLE,
                                                 getObservatoryInstrument(),
                                                 getFontData(),
                                                 getColourData(),
                                                 getDefaultValue());
        if (strResult != null)
            {
            setValue(strResult);
            }
        else
            {
            // User did not make a selection
            setValue(getDefaultValue());
            }
        }
    }
