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


import org.lmn.fc.common.constants.*;

import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.Serializable;


/***************************************************************************************************
 * SerialConfigurationIndicatorInterface.
 */

public interface SerialConfigurationIndicatorInterface extends SwingConstants,
                                                               Accessible,
                                                               ImageObserver,
                                                               MenuContainer,
                                                               Serializable,
                                                               FrameworkConstants,
                                                               FrameworkStrings,
                                                               FrameworkMetadata,
                                                               FrameworkSingletons,
                                                               FrameworkRegex,
                                                               ResourceKeys
    {
    // String Resources
    String LABEL_RATE = "Rate";
    String LABEL_DATA = "Data";
    String LABEL_STOP = "Stop";
    String LABEL_PARITY = "Parity";
    String LABEL_FLOW = "Flow";
    String TOOLTIP_PORTNAME = "Currently selected Port.Name";
    String TOOLTIP_RATE = "Select the Baud Rate for the Port";
    String TOOLTIP_DATABITS = "Select the Data Bits for the Port";
    String TOOLTIP_STOPBITS = "Select the Stop Bits for the Port";
    String TOOLTIP_PARITY = "Select the Parity for the Port";
    String TOOLTIP_FLOWCONTROL = "Select the Flow Control (Handshaking) for the Port";

    int WIDTH_PORT_RATE_DROPDOWN = 105;
    int WIDTH_PORT_CONFIG_DROPDOWN = 75;


    /***********************************************************************************************
     * Set the CellData containing links to the Properties of the new selection.
     * Setting NULL results in no selection.
     *
     * @param celldata
     * @param debug
     */

    void setSelectedCellData(SerialConfigurationCellDataInterface celldata,
                             boolean debug);
    }
