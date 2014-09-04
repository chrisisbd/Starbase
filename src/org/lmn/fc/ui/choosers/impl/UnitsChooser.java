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
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.choosers.utilities.JListDialog;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * UnitsChooser.
 */

public class UnitsChooser extends AbstractChooser
                           implements ChooserInterface
    {
    // String Resources
    private static final String DIALOG_TITLE = "Select a Unit";
    private static final String UNITS_DICTIONARY = "Units Dictionary";


    /***********************************************************************************************
     * UnitsChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public UnitsChooser(final ObservatoryInstrumentInterface obsinstrument,
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
        final String SOURCE = "UnitsChooser.showChooser() ";

        try
            {
            final List<String> listUnits;
            final String[] arrayUnitNames;
            final String strResult;
            String strLongestValue;

            listUnits = new ArrayList<String>(SchemaUnits.Enum.table.lastInt());
            arrayUnitNames = new String[SchemaUnits.Enum.table.lastInt()];
            strLongestValue = EMPTY_STRING;

            // Don't sort the Units, in order to maintain their natural grouping
            for (int intUnitsIndex = 1;
                 intUnitsIndex < (SchemaUnits.Enum.table.lastInt() + 1);
                 intUnitsIndex++)
                {
                final SchemaUnits.Enum unit;

                // All Units are applicable to Parameters and Metadata
                unit = SchemaUnits.Enum.forInt(intUnitsIndex);

                if (unit != null)
                    {
                    listUnits.add(unit.toString());

                    // Search for the longest name
                    if (unit.toString().length() > strLongestValue.length())
                        {
                        strLongestValue = unit.toString();
                        }
                    }
                }

            strResult = JListDialog.showDialog(component,
                                               null,
                                               UNITS_DICTIONARY,
                                               DIALOG_TITLE,
                                               listUnits.toArray(arrayUnitNames),
                                               getFontData(),
                                               getColourData(),
                                               getDefaultValue(),
                                               strLongestValue + "    ");
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

        catch (HeadlessException exception)
            {
            LOGGER.error(SOURCE + "HeadlessException [exception=" + exception.getMessage() + "]");
            setValue(getDefaultValue());
            }
        }
    }
