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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * GroupRearrangerMenu.
 */

public final class GroupRearrangerMenu extends JPopupMenu
    {
    private ObservatoryUIInterface hostUI;


    /***********************************************************************************************
     * Displays the popup menu at the position x,y in the coordinate
     * space of the component invoker.
     *
     * @param hostui
     * @param invoker the component in whose space the popup menu is to appear
     * @param x the x coordinate in invoker's coordinate space at which
     *        the popup menu is to be displayed
     * @param y the y coordinate in invoker's coordinate space at which
     */

    public void show(final ObservatoryUIInterface hostui,
                     final Component invoker,
                     final int x,
                     final int y)
        {
        setHostUI(hostui);

        super.show(invoker, x, y);
        }


    /***********************************************************************************************
     * Get the ObservatoryUI that is associated with this Menu.
     *
     * @return ObservatoryUIInterface
     */

    private ObservatoryUIInterface getHostUI()
        {
        return (this.hostUI);
        }


    /***********************************************************************************************
     * Set the ObservatoryUI that is associated with this Menu.
     *
     * @param hostui
     */

    private void setHostUI(final ObservatoryUIInterface hostui)
        {
        this.hostUI = hostui;
        }
    }
