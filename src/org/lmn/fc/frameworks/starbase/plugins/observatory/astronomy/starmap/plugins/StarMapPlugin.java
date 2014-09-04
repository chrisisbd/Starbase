// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

//------------------------------------------------------------------------------
// StarMap Plugin
//------------------------------------------------------------------------------
// Revision History
//
//  28-09-02    LMN created file
//  03-10-02    LMN extended with ObjectType etc.
//  28-10-02    LMN added Draw Mode
//
//------------------------------------------------------------------------------
// Astronomy Starmap package

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Vector;

//------------------------------------------------------------------------------

public interface StarMapPlugin extends FrameworkConstants,
                                       FrameworkStrings,
                                       FrameworkMetadata,
                                       FrameworkSingletons,
                                       ResourceKeys
    {
    int CURSOR_HALFWIDTH = 8;
    int CURSOR_HALFHEIGHT = 8;
    Border BORDER_BUTTON = BorderFactory.createEmptyBorder(3, 3, 3, 3);


    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    ObservatoryUIInterface getObservatoryUI();

    StarMapUIComponentPlugin getHostStarMap();

    String getPluginName();

    void setPluginName(String name);

    String getObjectName();

    void setObjectName(String name);

    boolean isActive();

    void setActive(boolean active);

    boolean isClickable();

    void setClickable(boolean clickable);

    Vector<Vector<StarMapPointInterface>> getStarMapPoints();

    void setStarMapPoints(Vector<Vector<StarMapPointInterface>> collection);

    /***********************************************************************************************
     * Add a set of StarMapPoints to the collection.
     *
     * @param points
     */

    void addPoints(Vector<StarMapPointInterface> points);


    /***********************************************************************************************
     * Get the JButton used to control the Plugin from the Toolbar.
     *
     * @return JButton
     */

    AbstractButton getButton();


    /***********************************************************************************************
     * Set the JButton used to control the Plugin from the Toolbar.
     *
     * @param button
     */

    void setButton(AbstractButton button);


    DrawMode getDrawMode();

    // Draw Methods
    void setDrawMode(DrawMode drawmode);

    void drawObject(Graphics graphics,
                    StarMapViewportInterface viewport,
                    Insets insets,
                    StarMapPointInterface point);

    boolean isLabelled();

    // Label Methods
    void setLabelled(boolean labelmode);

    void labelObject(Graphics graphics,
                     StarMapViewportInterface viewport,
                     Insets insets,
                     StarMapPointInterface point);


    Color getColour();

    // Colour Methods
    void setColour(Color colour);

    // Coordinate Methods
    void refreshCoordinates(AstronomicalCalendarInterface calendar);

    void transformToViewportXY(StarMapViewportInterface viewport,
                               AstronomicalCalendarInterface calendar,
                               double latitude,
                               boolean originisnorth,
                               StarMapPointInterface[][] clickcoordinates);
    }
