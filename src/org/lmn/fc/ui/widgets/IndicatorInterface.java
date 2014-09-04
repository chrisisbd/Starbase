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

package org.lmn.fc.ui.widgets;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.IndicatorException;

import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.io.Serializable;



public interface IndicatorInterface extends ImageObserver,
                                            MenuContainer,
                                            Serializable,
                                            Accessible,
                                            FrameworkConstants,
                                            FrameworkSingletons,
                                            FrameworkStrings
    {
    Color DEFAULT_VALUE_COLOR = Color.green;


    /***********************************************************************************************
     * Initialise the Indicator.
     */

    void initialiseUI();


    void setValueFont(String strFontName) throws IndicatorException;


    /***********************************************************************************************
     * Get the Indicator Value Format.
     *
     * @return String
     */

    String getValueFormat();


    void setValueFormat(String strFormat) throws IndicatorException;


    String getValueFont();


    void setStatusFont(Font font) throws IndicatorException;


    void setStatusFont(String strFontName) throws IndicatorException;


    void setStatusColour(Color color);


    /***********************************************************************************************
     * Set the Color of the Status bvackground.
     *
     * @param color
     */

    void setStatusBackground(Color color);


    void setStatusFormat(String strFormat) throws IndicatorException;


    String getStatusFont();


    void setValue(String strValue) throws IndicatorException;


    void setUnits(String strUnits);


    /***********************************************************************************************
     * Indicate if the Units should be displayed.
     *
     * @return boolean
     */

    boolean areUnitsVisible();


    /***********************************************************************************************
     * Indicate if the Units should be displayed.
     *
     * @param visible
     */

    void setUnitsVisible(boolean visible);


    void setStatus(String strStatus) throws IndicatorException;


    void setValueForeground(Color foreground);


    void setValueBackground(Color background);

    void setForeground(Color foreground);


    /***********************************************************************************************
     *
     * @return String
     */

    String getTooltip();


    void setToolTip(String strToolTip);


    void setAlignment(int alignment) throws IllegalArgumentException;

    void setAlignmentY(float alignmenty);

    void addMouseListenerToValue(MouseListener listener);
    }
