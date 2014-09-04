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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl;

import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.DrawMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapViewportInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;


/***************************************************************************************************
 * StarMapObject.
 */

public abstract class StarMapObject implements StarMapPlugin
    {
    private final ObservatoryUIInterface observatoryUI;
    private final StarMapUIComponentPlugin pluginHost;             // The StarMap host for the Plugin
    private String strPluginName;           // The public name of this Plugin
    private String strObjectName;           // The Type of StarMapPoint to display
    private boolean boolClickable;          // Indicates if Object can be clicked
    private boolean boolLabelled;          // Indicates if the Object can be labelled
    private Color colourObject;             // The display colour of the Object
    private DrawMode drawMode;                // Point, Line, Polygon, Filled Polygon
    private AbstractButton buttonToolbar;

    private boolean boolActive;             // The Active state of the Plugin
    private Vector<Vector<StarMapPointInterface>> vecPoints;               // The Vector containing StarMapPoints


    /***********************************************************************************************
     * StarMapObject.
     *
     * @param observatoryui
     * @param pluginhost
     * @param pluginname
     * @param objectname
     * @param colour
     * @param labelled
     * @param clickable
     * @param drawmode
     */

    public StarMapObject(final ObservatoryUIInterface observatoryui,
                         final StarMapUIComponentPlugin pluginhost,
                         final String pluginname,
                         final String objectname,
                         final Color colour,
                         final boolean labelled,
                         final boolean clickable,
                         final DrawMode drawmode)
        {
        this.observatoryUI = observatoryui;
        this.pluginHost = pluginhost;
        this.strPluginName = pluginname;
        this.strObjectName = objectname;
        this.boolClickable = clickable;
        this.boolLabelled = labelled;
        this.colourObject = colour;
        this.drawMode = drawmode;
        this.boolActive = false;
        this.vecPoints = new Vector<Vector<StarMapPointInterface>>(100);

        // Add the basic Toolbar button
        buttonToolbar = new JButton();
        buttonToolbar.setBorder(BORDER_BUTTON);
        buttonToolbar.setText(EMPTY_STRING);

        // These will get overridden by the subclasses...
        buttonToolbar.setAction(null);
        buttonToolbar.setToolTipText(EMPTY_STRING);

        // Ensure that no text appears next to the Icon...
        buttonToolbar.setHideActionText(true);
        }



    /***********************************************************************************************
     * Get the host ObservatoryUI.
     *
     * @return ObservatoryUIInterface
     */

    public ObservatoryUIInterface getObservatoryUI()
        {
        return (this.observatoryUI);
        }


    public final StarMapUIComponentPlugin getHostStarMap()
        {
        return(this.pluginHost);
        }

    public final String getPluginName()
        {
        return(this.strPluginName);
        }

    public final void setPluginName(final String pluginName)
        {
        this.strPluginName = pluginName;
        }

    public final String getObjectName()
        {
        return(this.strObjectName);
        }

    public final void setObjectName(final String objectname)
        {
        this.strObjectName = objectname;
        }

    public final boolean isActive()
        {
        return(this.boolActive);
        }

    public final void setActive(final boolean active)
        {
        this.boolActive = active;
        }

    public final boolean isClickable()
        {
        return(this.boolClickable);
        }

    public final void setClickable(final boolean clickable)
        {
        this.boolClickable = clickable;
        }


    /***********************************************************************************************
     * Get the collection of StarMapPoints associated with this StarMapPlugin.
     *
     * @return Vector<Vector<StarMapPointInterface>>
     */

    public Vector<Vector<StarMapPointInterface>> getStarMapPoints()
        {
        return(this.vecPoints);
        }


    /***********************************************************************************************
     * Set the collection of StarMapPoints associated with this StarMapPlugin.
     *
     * @param points
     */

    public final void setStarMapPoints(final Vector<Vector<StarMapPointInterface>> points)
        {
        this.vecPoints = points;
        }


    /***********************************************************************************************
     * Add a set of StarMapPoints to the collection.
     *
     * @param points
     */

    public final void addPoints(final Vector<StarMapPointInterface> points)
        {
        if ((getStarMapPoints() != null)
            && (points != null))
            {
            getStarMapPoints().add(points);
            }
        }


    /***********************************************************************************************
     * Get the AbstractButton used to control the Plugin from the Toolbar.
     *
     * @return AbstractButton
     */

    public AbstractButton getButton()
        {
        return (this.buttonToolbar);
        }


    /***********************************************************************************************
     * Set the AbstractButton used to control the Plugin from the Toolbar.
     *
     * @param button
     */

    public void setButton(final AbstractButton button)
        {
        this.buttonToolbar = button;
        }


    public final DrawMode getDrawMode()
        {
        return(this.drawMode);
        }

    public void setDrawMode(final DrawMode drawmode)
        {
        this.drawMode = drawmode;
        }

    public abstract void drawObject(final Graphics graphics,
                                    final StarMapViewportInterface viewport,
                                    final Insets insets,
                                    final StarMapPointInterface point);

    public final boolean isLabelled()
        {
        return(this.boolLabelled);
        }

    public final void setLabelled(final boolean labelmode)
        {
        this.boolLabelled = labelmode;
        }

    public abstract void labelObject(final Graphics graphics,
                                     final StarMapViewportInterface viewportInterface,
                                     final Insets insets,
                                     final StarMapPointInterface pointInterface);


    public final Color getColour()
        {
        return(this.colourObject);
        }

    public final void setColour(final Color colour)
        {
        this.colourObject = colour;

        StarMapHelper.setPluginColour(this, colour);
        }

    public abstract void refreshCoordinates(final AstronomicalCalendarInterface calendar);

    // Transform the (Ra, Dec) or (Az, El) to (X, Y) for display at a specified time & place
    // The Latitude and LAST are ignored for (Az, El) transformations
    // The caller must take care of repaint()
    public abstract void transformToViewportXY(final StarMapViewportInterface viewport,
                                               final AstronomicalCalendarInterface calendar,
                                               final double latitude,
                                               final boolean originisnorth,
                                               final StarMapPointInterface[][] clickcoordinates);
    }
