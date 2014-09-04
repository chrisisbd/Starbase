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
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.choosers.utilities.JListDialog;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/***************************************************************************************************
 * DataTypeChooser.
 */

public class DataTypeChooser extends AbstractChooser
                             implements ChooserInterface
    {
    // String Resources
    private static final String DIALOG_TITLE = "Select a DataType";
    private static final String DATATYPE_DICTIONARY = "DataType Dictionary";


    /***********************************************************************************************
     * DataTypeChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public DataTypeChooser(final ObservatoryInstrumentInterface obsinstrument,
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
        final String SOURCE = "DataTypeChooser.showChooser() ";

        try
            {
            final DataTypeDictionary[] arrayDataTypes;
            final List<String> listDataTypeNames;
            final String[] arrayDataTypeNames;
            final String strResult;
            String strLongestValue;

            arrayDataTypes = DataTypeDictionary.values();
            listDataTypeNames = new ArrayList<String>(arrayDataTypes.length);
            arrayDataTypeNames = new String[arrayDataTypes.length];
            strLongestValue = EMPTY_STRING;

            // Only list the DataTypes which may be used in Metadata
            for (int intDataTypeIndex = 0;
                 intDataTypeIndex < arrayDataTypes.length;
                 intDataTypeIndex++)
                {
                if (arrayDataTypes[intDataTypeIndex].isMetadataType())
                    {
                    listDataTypeNames.add(arrayDataTypes[intDataTypeIndex].getName());

                    // Search for the longest name
                    if (arrayDataTypes[intDataTypeIndex].getName().length() > strLongestValue.length())
                        {
                        strLongestValue = arrayDataTypes[intDataTypeIndex].getName();
                        }
                    }
                }

            // Sort the DataType names
            Collections.sort(listDataTypeNames);

            strResult = JListDialog.showDialog(component,
                                               null,
                                               DATATYPE_DICTIONARY,
                                               DIALOG_TITLE,
                                               listDataTypeNames.toArray(arrayDataTypeNames),
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
