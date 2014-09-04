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
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.FontDataType;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.choosers.ChooserInterface;
import org.lmn.fc.ui.choosers.utilities.JFontChooser;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/***************************************************************************************************
 * FontChooser.
 */

public class FontChooser extends AbstractChooser
                         implements ChooserInterface
    {
    // String Resources
    private static final String DIALOG_TITLE = "Select a font";
    private static final String VALUE_FONT = "font=";
    private static final String VALUE_STYLE = " style=";
    private static final String VALUE_SIZE = " size=";

    private final JFontChooser fc;


    /***********************************************************************************************
     * FontChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public FontChooser(final ObservatoryInstrumentInterface obsinstrument,
                       final FontInterface font,
                       final ColourInterface colourforeground,
                       final String defaultvalue)
        {
        super(obsinstrument, font, colourforeground, defaultvalue);

        final String SOURCE = "FontChooser() ";
        final List<String> errors;

        errors = new ArrayList<String>(10);

        // Check that the default value is valid
        if (DataTypeHelper.validateDataTypeOfMetadataValue(getDefaultValue(),
                                                           DataTypeDictionary.FONT_DATA,
                                                           EMPTY_STRING,
                                                           errors) == 0)
            {
            fc = new JFontChooser(font,
                                  colourforeground,
                                  (new FontDataType(getDefaultValue())).getFont());
            }
        else
            {
            fc = new JFontChooser(font,
                                  colourforeground,
                                  UIComponentPlugin.DEFAULT_FONT.getFont());
            LOGGER.errors(SOURCE, errors);
            }
        }


    /***********************************************************************************************
     * Show the Chooser, centred on the specified Component.
     *
     * @param component
     */

    public void showChooser(final Component component)
        {
        final String SOURCE = "FontChooser.showChooser() ";
        final int intState;

        intState = getChooser().showDialog(component, DIALOG_TITLE);

        if (intState == JFontChooser.ACCEPT_OPTION)
            {
            final Font font;
            final String strFontName;
            final Hashtable<Integer, String> hashtableStyles;
            final int intStyle;
            final int intSize;
            final StringBuffer buffer;

            hashtableStyles = new Hashtable<Integer, String>(4);
            hashtableStyles.put(Font.PLAIN, "plain");
            hashtableStyles.put(Font.BOLD, "bold");
            hashtableStyles.put(Font.ITALIC, "italic");
            hashtableStyles.put(Font.BOLD + Font.ITALIC, "bolditalic");

            font = getChooser().getSelectedFont();
            buffer = new StringBuffer();

            // Value: font=XXXX style=YYYY size=ZZZZ

            buffer.append(VALUE_FONT);
            // Beware that the Family names may contain spaces
            strFontName = font.getFamily();
            buffer.append(strFontName);

            // Style may be {Plain, Bold, Italic, Bold+Italic}
            buffer.append(VALUE_STYLE);
            intStyle = font.getStyle();
            // Rely on Font to constrain the range of the Style
            buffer.append(hashtableStyles.get(intStyle));

            buffer.append(VALUE_SIZE);
            intSize = font.getSize();
            buffer.append(intSize);

            setValue(buffer.toString());
            }
        else
            {
            setValue(getDefaultValue());
            }
        }


    /***********************************************************************************************
     * A convenience method to get the Chooser.
     *
     * @return JFontChooser
     */

    private JFontChooser getChooser()
        {
        return (this.fc);
        }
    }
