// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012
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

import org.lmn.fc.frameworks.starbase.plugins.observatory.events.CommitChangeListener;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.event.ChangeListener;
import java.awt.*;


/***************************************************************************************************
 * ControlKnobInterface.
 */

public interface ControlKnobInterface extends UIComponentPlugin
    {
    /**********************************************************************************************/
    /* UI State                                                                                   */
    /***********************************************************************************************
     * Paint the Control.
     *
     * @param graphics
     */

    void paint(Graphics graphics);


    /***********************************************************************************************
     * Control the enable state of the Control.
     *
     * @param enabled
     */

    void setEnabled(boolean enabled);


    /**********************************************************************************************/
    /* Knob State                                                                                 */
    /***********************************************************************************************
     * Get the current value of the Control.
     *
     * @return double
     */

    double getValue();


    /***********************************************************************************************
     * Set the Value on the Control, forcing the value to be {ScaleMin...ScaleMax}.
     *
     * @param value
     */

    void setValue(double value);


    /***********************************************************************************************
     * Set the Control to indicate centre, wherever that may be.
     */

    void setCentre();


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Add a ChangeListener.
     *
     * @param listener
     */

    void addChangeListener(ChangeListener listener);


    /***********************************************************************************************
     * Remove a ChangeListener.
     *
     * @param listener
     */

    void removeChangeListener(ChangeListener listener);


    /***********************************************************************************************
     * Add a CommitChangeListener.
     *
     * @param listener
     */

    void addCommitChangeListener(CommitChangeListener listener);


    /***********************************************************************************************
     * Remove a CommitChangeListener.
     *
     * @param listener
     */

    void removeCommitChangeListener(CommitChangeListener listener);


    /***********************************************************************************************
     * Fire a ChangeEvent to show that the Control has changed.
     */

    void fireChangeEvent();


    /***********************************************************************************************
     * Fire a CommitChangeEvent to show that the Control change is complete.
     */

    void fireCommitChangeEvent();
    }
