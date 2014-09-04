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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentStateTransition;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandProcessorUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;


/***************************************************************************************************
 * ActivityIndicatorUIComponentInterface.
 */

public interface ActivityIndicatorUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String FILENAME_ICON_BUSY = "activity-busy";
    String ICON_ACTIVITY_UNSAVED = "activity-unsaved.png";
    String ICON_ACTIVITY_IDLE = "activity-idle.png";
    String TOOLTIP_ACTIVITY_UNSAVED = "You have unsaved data - please export (click to clear message)";
    String TOOLTIP_ACTIVITY_IDLE = "Indicates Instrument activity";
    String TOOLTIP_ACTIVITY_ACTIVE = "The Instrument is ";
    String MSG_UNSAVED_DATA = " - Unsaved data!";

    int ANIMATION_COUNT = 12;
    int ANIMATION_CYCLE_MILLIS = 3000;
    long ANIMATOR_STOP_DELAY = 20;
    long STATUS_STOP_DELAY = 20;
    int WIDTH_STRUT = 9;

    Dimension DIM_ACTIVITY = new Dimension(CommandProcessorUtilities.HEIGHT_BUTTON,
                                           CommandProcessorUtilities.HEIGHT_BUTTON);
    Dimension DIM_ACTIVITY_TOOLBAR = new Dimension(CommandProcessorUtilities.HEIGHT_BUTTON-3,
                                                   CommandProcessorUtilities.HEIGHT_BUTTON-3);
    Dimension DIM_STATUS = new Dimension(CommandProcessorUtilities.WIDTH_BUTTON,
                                         CommandProcessorUtilities.HEIGHT_BUTTON);
    Dimension DIM_STATUS_TOOLBAR = new Dimension(CommandProcessorUtilities.WIDTH_BUTTON,
                                                 CommandProcessorUtilities.HEIGHT_BUTTON-3);
    Dimension DIM_INDICATOR = new Dimension(CommandProcessorUtilities.HEIGHT_BUTTON + CommandProcessorUtilities.WIDTH_BUTTON + WIDTH_STRUT,
                                            CommandProcessorUtilities.HEIGHT_BUTTON);

    Color COLOR_ACTIVITY_BG_IDLE = UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor();
    Color COLOR_ACTIVITY_BG_ACTIVE = UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor();
    Color COLOR_ACTIVITY_BG_UNSAVED = UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor();

    Color COLOR_STATUS_BG_NORMAL = Color.black;
    Color COLOR_STATUS_FG_NORMAL = Color.green;
    Color COLOR_STATUS_FG_ALERT = Color.red.brighter();
    Color COLOR_STATUS_BG_ALERT = new Color(255, 255, 153);


    /***********************************************************************************************
     * Set the InstrumentStateTransition to be displayed, using the CommandProcessorContext.
     * This is set from InstrumentStateMachine.
     *
     * @param context
     * @param transition
     */

    void addStateTransition(CommandProcessorContextInterface context,
                            InstrumentStateTransition transition);
    }
