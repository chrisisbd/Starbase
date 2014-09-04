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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  20-03-02    LMN created file from Java Applications book
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.common.constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


public interface FrameworkConstants
    {
    String UKRAA_PASSWORD = "twittwot0";
    String UKRAA_USERNAME = "ukraastarbase";

    int RADIX_DECIMAL = 10;
    int RADIX_HEX = 16;

    long SWING_WORKER_STOP_DELAY = 500;
    long SWING_WORKER_REPORT_TABLE_REFRESH_DELAY = 10;
    int SHOW_DEBUG_LINECOUNT = 10;

    // Geometry
    double TwoPi = Math.PI * 2.0;
    double RADperDEG = Math.PI / 180.0;
    double DEGperRAD = 1.0 / RADperDEG;
    double RADIAN = 0.0174532925200;

    // Epochs
    double JULIAN_YEAR = 365.25;
    double JULIAN_CENTURY = 100.0 * JULIAN_YEAR;

    // Miscellaneous
    double SIDEREAL_RATIO = 1.00273790935; // Java book ends with 2558 instead of 35
    double EARTH_RAD = 6378140.0;
    double EARTH_RAD_P = 6356755.0;
    double GEOCENTRIC_K = (EARTH_RAD_P * EARTH_RAD_P) / (EARTH_RAD * EARTH_RAD);

    String ICON_JAVADOC = "javadoc.png";
    String ICON_ACKNOWLEDGEMENTS = "acknowledgements.png";
    String ICON_LICENCE = "licence.png";

    String DEFAULT_TIME_ZONE_ID = "GMT+00:00";

    // Scraps of SQL
    String SQL_DELETE = "DELETE FROM ";
    String SQL_INSERT = "INSERT INTO ";
    String SQL_WHERE = " WHERE ";
    String SQL_EQUALS = "=";
    String SQL_LEFT_PAREN = " (";
    String SQL_COMMA = ", ";
    String SQL_QUOTE = "'";
    String SQL_RIGHT_PAREN = " )";
    String SQL_AND = " AND ";
    String SQL_VALUES = SQL_RIGHT_PAREN + " VALUES(";

    // Action Icons
    String ICON_DUMMY = "dummy.jpg";
    String ICON_DUMMY_DISTRIBUTION = "distribution.png";
    String ICON_NOT_IN_POOL = "command-notinpool.png";
    String ICON_CATEGORY_CAPTURE = "command-capture.png";
    String ICON_CATEGORY_IMPORT = "command-import.png";
    String ICON_CATEGORY_BUILDER = "command-builder.png";

    // These icons are in a jar file
    String ACTION_ICON_FULLSCREEN = "/toolbarButtonGraphics/general/Zoom16.gif";
    String ACTION_ICON_DELETE = "/toolbarButtonGraphics/general/Delete16.gif";
    String ACTION_ICON_SAVE = "/toolbarButtonGraphics/general/SaveAll16.gif";
    String ACTION_ICON_LOGOUT = "/toolbarButtonGraphics/general/Stop16.gif";
    String ACTION_ICON_EXIT = "/toolbarButtonGraphics/general/Stop16.gif";
    String ACTION_ICON_ABOUT = "/toolbarButtonGraphics/general/About16.gif";
    String ACTION_ICON_COPY = "/toolbarButtonGraphics/general/Copy16.gif";

    String ACTION_ICON_TEST0 = "/toolbarButtonGraphics/media/Play16.gif";
    String ACTION_ICON_TEST1 = "/toolbarButtonGraphics/media/Stop16.gif";
    String ACTION_ICON_TEST2 = "action.png";
    String ACTION_ICON_SUBMENU = "/toolbarButtonGraphics/media/Play16.gif";

    // JavaConsole
    String ACTION_ICON_CONSOLE_RUN = "/toolbarButtonGraphics/development/Host16.gif";
    String ACTION_ICON_CONSOLE_CLEAR = "/toolbarButtonGraphics/general/New16.gif";

    // ActionList
    String ACTION_ICON_ACTIONS = "/toolbarButtonGraphics/general/Properties16.gif";

    // TerminalEmulator
    String ACTION_ICON_CLEAR_SCREEN = "/toolbarButtonGraphics/general/New16.gif";

    // ReportTable
    String ACTION_ICON_PRINT = "/toolbarButtonGraphics/general/Print16.gif";
    String ACTION_ICON_EXPORT = "/toolbarButtonGraphics/general/Export16.gif";
    String ACTION_ICON_EXCEL = "/toolbarButtonGraphics/general/Export16.gif";
    String ACTION_ICON_XML = "/toolbarButtonGraphics/general/AlignCenter16.gif";
    String ACTION_ICON_EMAIL = "/toolbarButtonGraphics/general/SendMail16.gif";
    String ACTION_ICON_RELOAD = "/toolbarButtonGraphics/general/Redo16.gif";
    String ACTION_ICON_TRUNCATE = "/toolbarButtonGraphics/table/RowDelete16.gif";
    String ACTION_ICON_ICONS = "/toolbarButtonGraphics/general/Information16.gif";

    // MapUIComponent
    String ACTION_ICON_TOGGLE_POI = "/toolbarButtonGraphics/general/Zoom16.gif";

    String PROPERTY_JAVA_CLASS_PATH = "java.class.path";
    String PROPERTY_JAVA_LIBRARY_PATH = "java.library.path";

    Border INDICATOR_BORDER = BorderFactory.createLineBorder(new Color(100, 100, 100), 1);

    int BYTE_MASK = 0xff;
    int SIZE_SPLIT_PANE_DIVIDER = 10;

    String DATA_TIME_SERIES = "TimeSeries";
    String DATA_RAW_DATA = "RawData";
    String DATA_PROCESSED_DATA = "ProcessedData";

    //---------------------------------------------------------------------------------------------
    // Staribus

    String PORT_COM1 = "COM1";
    String PORT_COM2 = "COM2";
    String PORT_COM3 = "COM3";
    String PORT_COM4 = "COM4";
    String PORT_COM5 = "COM5";
    String PORT_COM6 = "COM6";
    String PORT_COM7 = "COM7";
    String PORT_COM8 = "COM8";
    String PORT_COM9 = "COM9";
    String PORT_COM10 = "COM10";

    int DEFAULT_BAUDRATE = 4800;
    int DEFAULT_DATABITS = 8;
    int DEFAULT_STOPBITS = 1;
    int DEFAULT_PARITY = 0;

    String FLOWCONTROL_NONE = "None";
    String FLOWCONTROL_XON_XOFF = "XON/XOFF";
    String FLOWCONTROL_RTS_CTS = "RTS/CTS";

    //---------------------------------------------------------------------------------------------
    // Starinet

    int DEFAULT_STARINET_UDP_PORT = 1205;
    }
