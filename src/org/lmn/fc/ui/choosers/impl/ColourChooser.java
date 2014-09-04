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


import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.ui.choosers.ChooserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.NoSuchElementException;


/***************************************************************************************************
 * ColourChooser.
 */

public class ColourChooser extends AbstractChooser
                           implements ChooserInterface
    {
    // String Resources
    private static final String DIALOG_TITLE = "Select a colour";


    /***********************************************************************************************
     * ColourChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public ColourChooser(final ObservatoryInstrumentInterface obsinstrument,
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
        final String SOURCE = "ColourChooser.showChooser() ";

        try
            {
            final JColorChooser colorChooser;
            ColourInterface colour;
            final Color color;
            final UIDefaults defaults;
            final ColorTracker listenerOK;
            final JDialog dialog;

            defaults = UIManager.getDefaults();

            defaults.put("ColorChooser.font", getFontData().getFont());
            defaults.put("ColorChooser.foreground", getColourData().getColor());
            defaults.put("ColorChooser.okText", ChooserInterface.BUTTON_SELECT);
            defaults.put("ColorChooser.cancelText", ChooserInterface.BUTTON_CANCEL);
            defaults.put("ColorChooser.resetText", ChooserInterface.BUTTON_RESET);

            // Check we have a valid default value
            colour = new ColourDataType(getDefaultValue());

            colorChooser = new JColorChooser(colour.getColor());

            Utilities.setContainerHierarchyFonts(colorChooser.getComponents(),
                                                 getFontData());

            Utilities.setContainerHierarchyColours(colorChooser.getComponents(),
                                                   getColourData(),
                                                   null);

            listenerOK = new ColorTracker(colorChooser);
            dialog = JColorChooser.createDialog(component,
                                                DIALOG_TITLE,
                                                true,
                                                colorChooser,
                                                listenerOK,
                                                null);
            Utilities.setContainerHierarchyFonts(dialog.getComponents(),
                                                 getFontData());

            Utilities.setContainerHierarchyColours(dialog.getComponents(),
                                                   getColourData(),
                                                   null);
            dialog.setVisible(true);
            color = listenerOK.getColor();

            if (color != null)
                {
                colour = new ColourDataType(color);
                setValue(colour.toString());
                }
            else
                {
                // User did not make a selection
                setValue(getDefaultValue());
                }
            }

        catch (NoSuchElementException exception)
            {
            LOGGER.error(SOURCE + "NoSuchElementException [exception=" + exception.getMessage() + "]");
            setValue(getDefaultValue());
            }

        catch (NumberFormatException exception)
            {
            LOGGER.error(SOURCE + "NumberFormatException [exception=" + exception.getMessage() + "]");
            setValue(getDefaultValue());
            }

        catch (HeadlessException exception)
            {
            LOGGER.error(SOURCE + "HeadlessException [exception=" + exception.getMessage() + "]");
            setValue(getDefaultValue());
            }
        }


    /***********************************************************************************************
     * ColorTracker.
     */

    class ColorTracker implements ActionListener,
                                  Serializable
        {
        private final JColorChooser colorChooser;
        private Color color;


        /*******************************************************************************************
         * ColorTracker.
         *
         * @param chooser
         */

        ColorTracker(final JColorChooser chooser)
            {
            this.colorChooser = chooser;
            }


        /*******************************************************************************************
         * Handle the OK button event.
         *
         * @param event
         */

        public void actionPerformed(final ActionEvent event)
            {
            this.color = colorChooser.getColor();
            }


        /*******************************************************************************************
         * Get the Color chosen by the User.
         *
         * @return Color
         */

        public Color getColor()
            {
            return (this.color);
            }
        }
    }
