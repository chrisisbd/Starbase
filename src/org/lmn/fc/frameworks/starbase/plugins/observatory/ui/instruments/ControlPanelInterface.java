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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;


import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;

import java.awt.*;


/***************************************************************************************************
 * ControlPanelInterface.
 */

public interface ControlPanelInterface
    {
    // String Resources
    String DEFAULT_ADDRESS = FrameworkStrings.QUERY;
    String DEFAULT_ADDRESS_FORMAT = "999";

    String UNKNOWN_IP_ADDRESS = FrameworkStrings.QUERY;
    String DEFAULT_IP_ADDRESS_FORMAT = "00.000.00.000";

    String UNKNOWN_PORT = FrameworkStrings.QUERY;
    String DEFAULT_PORT_FORMAT = "COM00";

    String PREFIX_HTTP = "http://";
    String PREFIX_UDP = "udp://";

    Dimension DIM_CONTROL_PANEL_INDICATOR_SINGLE = new Dimension(110, 25);
    Dimension DIM_CONTROL_PANEL_INDICATOR_DOUBLE = new Dimension(80, 25);

    int MAX_CONTROL_PANEL_INDICATORS = 8;

    int INDICATOR_COUNT_0 = 0;
    int INDICATOR_COUNT_1 = 1;
    int INDICATOR_COUNT_2 = 2;
    int INDICATOR_COUNT_3 = 3;
    int INDICATOR_COUNT_4 = 4;
    int INDICATOR_COUNT_5 = 5;
    int INDICATOR_COUNT_6 = 6;
    int INDICATOR_COUNT_7 = 7;
    int INDICATOR_COUNT_8 = 8;

    int WIDTH_INCREASE_SINGLE_INDICATOR = 30;


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    void instrumentChanged(InstrumentStateChangedEvent event);
    }
