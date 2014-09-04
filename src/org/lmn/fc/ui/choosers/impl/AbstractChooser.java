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

import java.awt.*;


/***************************************************************************************************
 * AbstractChooser.
 */

public class AbstractChooser implements ChooserInterface
    {
    // Injections
    private final ObservatoryInstrumentInterface observatoryInstrument;
    private final FontInterface pluginFont;
    private final ColourInterface pluginColour;
    private final String strValueDefault;

    private String strValueSelected;


    /***********************************************************************************************
     * AbstractChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public AbstractChooser(final ObservatoryInstrumentInterface obsinstrument,
                           final FontInterface font,
                           final ColourInterface colourforeground,
                           final String defaultvalue)
        {
        this.observatoryInstrument = obsinstrument;
        this.pluginFont = font;
        this.pluginColour = colourforeground;
        this.strValueDefault = defaultvalue;
        this.strValueSelected = defaultvalue;
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    protected ObservatoryInstrumentInterface getObservatoryInstrument()
        {
        return (this.observatoryInstrument);
        }


    /***********************************************************************************************
     * Get the FontDataType.
     *
     * @return FontPlugin
     */

    protected FontInterface getFontData()
        {
        return (this.pluginFont);
        }


    /***********************************************************************************************
     * Get the ColourDataType.
     *
     * @return ColourPlugin
     */

    protected ColourInterface getColourData()
        {
        return (this.pluginColour);
        }


    /***********************************************************************************************
     * Get the default Value to be returned by the Chooser.
     * Never change this value.
     *
     * @return String
     */

    protected String getDefaultValue()
        {
        return (this.strValueDefault);
        }


    /***********************************************************************************************
     * Get the Value returned by the Chooser, or a default if no choice is made.
     *
     * @return String
     */

    public String getValue()
        {
        return (this.strValueSelected);
        }


    /***********************************************************************************************
     * Set the Value to be returned by the Chooser.
     * Never return NULL, only EMPTY_STRING.
     *
     * @param value
     */

    public void setValue(final String value)
        {
        if (value != null)
            {
            this.strValueSelected = value.trim();
            }
        else
            {
            this.strValueSelected = EMPTY_STRING;
            }
        }


    /***********************************************************************************************
     * Show the Chooser, centred on the specified Component.
     *
     * @param component
     */

    public void showChooser(final Component component)
        {
        LOGGER.error("AbstractChooser.showChooser() No Chooser instance found");
        }
    }
