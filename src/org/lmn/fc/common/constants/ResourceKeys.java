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

package org.lmn.fc.common.constants;

/**
 * TODO DOCUMENT ME!
 *
 * @version $Id$
 */
public interface ResourceKeys
    {
    String KEY_DELIMITER = ".";

    // Resource Key prefixes for those items which are assumed to belong to the Framework
    String PREFIX_ROLE = "Role";
    String PREFIX_USER = "User";
    String PREFIX_COUNTRY = "Country";
    String PREFIX_LANGUAGE = "Language";
    String PREFIX_DATATYPE = "DataType";
    String PREFIX_LOOKANDFEEL = "LookAndFeel";

    String KEY_RESOURCE_FRAMEWORK = "";
    String KEY_SYSTEM_USER_DIR = "user.dir";
    String KEY_SYSTEM_USER_HOME = "user.home";

    // Loader Properties
    String KEY_ENABLE_DEBUG = "Enable.Debug";
    String KEY_ENABLE_DEBUG_STARIBUS = "Enable.Debug.Staribus";
    String KEY_ENABLE_DEBUG_STARINET = "Enable.Debug.Starinet";
    String KEY_ENABLE_DEBUG_TIMING = "Enable.Debug.Timing";
    String KEY_ENABLE_DEBUG_STATE = "Enable.Debug.State";
    String KEY_ENABLE_DEBUG_METADATA = "Enable.Debug.Metadata";
    String KEY_ENABLE_DEBUG_CHART = "Enable.Debug.Chart";
    String KEY_ENABLE_DEBUG_THREADS = "Enable.Debug.Threads";
    String KEY_ENABLE_COMMAND_MACROS = "Enable.CommandMacros";
    String KEY_ENABLE_TOOLBAR = "Enable.Toolbar";
    String KEY_ENABLE_VALIDATION_XML = "Enable.Validation.XML";
    String KEY_ENABLE_COMMAND_VARIANT = "Enable.CommandVariant";

    //String KEY_ENABLE_REMOTE = "Enable.Remote";
    String KEY_JMX_USERNAME = "Jmx.Http.Username";
    String KEY_JMX_PASSWORD = "Jmx.Http.Password";
    String KEY_JMX_PORT = "Jmx.Http.Port";
    String KEY_ENABLE_SQL_TRACE = "Enable.Sql.Trace";
    String KEY_ENABLE_SQL_TIMING = "Enable.Sql.Timing";
    String KEY_DATABASE_STORE = "Database.DataStore";
    String KEY_DATABASE_TYPE = "Database.Type";
    String KEY_LOCAL_DRIVER = "Database.Local.Driver";
    String KEY_LOCAL_DATA_SOURCE = "Database.Local.DataSource";
    String KEY_LOCAL_PORT = "Database.Local.Port";
    String KEY_LOCAL_DATABASE = "Database.Local.DatabaseName";
    String KEY_LOCAL_INLINE = "Database.Local.CredentialsInline";
    String KEY_LOCAL_USER_NAME = "Database.Local.UserName";
    String KEY_LOCAL_PASSWORD = "Database.Local.Password";
    String KEY_REMOTE_DRIVER = "Database.Remote.Driver";
    String KEY_REMOTE_DATA_SOURCE = "Database.Remote.DataSource";
    String KEY_REMOTE_PORT = "Database.Remote.Port";
    String KEY_REMOTE_DATABASE = "Database.Remote.DatabaseName";
    String KEY_REMOTE_INLINE = "Database.Remote.CredentialsInline";
    String KEY_REMOTE_USER_NAME = "Database.Remote.UserName";
    String KEY_REMOTE_PASSWORD = "Database.Remote.Password";
    String KEY_EMBEDDED_DRIVER = "Database.Embedded.Driver";
    String KEY_EMBEDDED_DATA_SOURCE = "Database.Embedded.DataSource";
    String KEY_EMBEDDED_PORT = "Database.Embedded.Port";
    String KEY_EMBEDDED_DATABASE = "Database.Embedded.DatabaseName";
    String KEY_EMBEDDED_INLINE = "Database.Embedded.CredentialsInline";
    String KEY_EMBEDDED_USER_NAME = "Database.Embedded.UserName";
    String KEY_EMBEDDED_PASSWORD = "Database.Embedded.Password";
    String KEY_RESOURCE_LOGIN = "Login.";

    // Generic Keys
    String KEY_NAME = "Name";
    String KEY_DESCRIPTION = "Description";
    String KEY_INSTRUMENT_UPDATE_ENABLE = "Update.Enable";
    String KEY_INSTRUMENT_UPDATE_PERIOD = "Update.Period";
    String KEY_INSTRUMENT_DATA_MODE = "Data.Mode";


    //----------------------------------------------------------------------------------------------
    // DAOs

    // PortController
    String KEY_PORT_CONTROLLER = "PortController.";
    String KEY_TX_CLASSNAME = "Tx.Classname";
    String KEY_RX_CLASSNAME = "Rx.Classname";

    // Abstract DAO
    String KEY_DAO_TIMEOUT_DEFAULT = "Dao.Timeout.Default";

    //String KEY_DAO_UPDATE_PERIOD = "Dao.Update.Period";
    //String KEY_DAO_UPDATE_ENABLE = "Dao.Update.Enable";

    // Generic
    String KEY_DAO_ONERROR_CONTINUE = "Dao.OnError.Continue";

    // Starinet
    String KEY_DAO_HOSTMEMORY_FILENAME = "Dao.HostMemory.Filename";

    // RemoteDataConnections
    String KEY_DAO_CONNECTION_HOSTNAME = "Dao.Connection.Hostname";
    String KEY_DAO_CONNECTION_USERNAME = "Dao.Connection.Username";
    String KEY_DAO_CONNECTION_PASSWORD = "Dao.Connection.Password";
    String KEY_DAO_CONNECTION_TRANSFER_MODE = "Dao.Connection.TransferMode";
    String KEY_DAO_CONNECTION_CONNECTION_MODE = "Dao.Connection.ConnectionMode";

    String KEY_DAO_TRANSFER_LOCAL_DIRECTORY = "Dao.Transfer.Local.Directory";
    String KEY_DAO_TRANSFER_LOCAL_FILENAME = "Dao.Transfer.Local.Filename";
    String KEY_DAO_TRANSFER_REMOTE_DIRECTORY = "Dao.Transfer.Remote.Directory";
    String KEY_DAO_TRANSFER_REMOTE_FILENAME = "Dao.Transfer.Remote.Filename";

    // Starcam DAO
    String KEY_DAO_IMAGE_FILENAME = "Dao.Image.Filename";

    // Sandbox DAO
    String KEY_DAO_URL_DEFAULT = "Dao.URL.Default";
    String KEY_DAO_URL_REFERRER = "Dao.URL.Referrer";

    // NTP DAO
    String KEY_DAO_NTP_SERVER_DEFAULT = "Dao.Server.Default";
    String KEY_DAO_NTP_SERVER_1 = "Dao.Server.1";
    String KEY_DAO_NTP_SERVER_2 = "Dao.Server.2";
    String KEY_DAO_NTP_SERVER_3 = "Dao.Server.3";

    // GPS DAO
    String KEY_DAO_GPS_RECEIVER_CLASS_NAME = "Dao.Receiver.ClassName";
    String KEY_DAO_GPS_RECEIVER_TYPE = "Dao.Receiver.Type";
    String KEY_DAO_GPS_ENABLE_RECEIVER = "Dao.Enable.Receiver";
    String KEY_DAO_GPS_PERIOD_CAPTURE = "Dao.Period.Capture";

    // Serial Ports
    String KEY_PORT_OWNER = "Port.Owner";
    String KEY_PORT_NAME = "Port.Name";
    String KEY_PORT_BAUDRATE = "Port.Baudrate";
    String KEY_PORT_DATA_BITS = "Port.DataBits";
    String KEY_PORT_STOP_BITS = "Port.StopBits";
    String KEY_PORT_PARITY = "Port.Parity";
    String KEY_PORT_FLOW_CONTROL = "Port.FlowControl";
    String KEY_PORT_SERIALEVENT_DELAY = "Port.SerialEventDelay";

    // Staribus Loopback Ports
    String KEY_PORT_LOOPBACK_PREAMBLE_SYN_COUNT = "Port.Loopback.PreambleSynCount";


    // Ethernet Ports
    String KEY_PORT_HOSTNAME = "Port.Hostname";
    String KEY_PORT_PORT_ID = "Port.PortID";
    String KEY_PORT_TIMEOUT = "Port.TimeoutPeriod";

    String KEY_SERVER_PORT_ID = "Server.PortID";
    String KEY_SERVER_TIMEOUT = "Server.TimeoutPeriod";

    // NTP Daemon
    String KEY_NTP_SERVER_DEFAULT = "Server.Default";
    String KEY_NTP_SERVER_1 = "Server.1";
    String KEY_NTP_SERVER_2 = "Server.2";
    String KEY_NTP_SERVER_3 = "Server.3";
    String KEY_NTP_ENABLE_NTP = "Enable.NTP";
    String KEY_NTP_ENABLE_SET_TIME = "Enable.SetTime";
    String KEY_NTP_ENABLE_TRACE = "Enable.Trace";
    String KEY_NTP_PERIOD_UPDATE = "Period.Update";

    // GPS Daemon
    String KEY_GPS_RECEIVER_CLASS_NAME = "Receiver.ClassName";
    String KEY_GPS_RECEIVER_TYPE = "Receiver.Type";
    String KEY_GPS_PERIOD_UPDATE = "Period.Update";
    String KEY_GPS_PERIOD_CAPTURE = "Period.Capture";
    String KEY_GPS_ENABLE_GPS_RECEIVER = "Enable.GpsReceiver";

    // Reports
    String KEY_REPORT_ENABLE_DEBUG = "Report.Enable.Debug";
    String KEY_REPORT_PAGE_ORIENTATION = "Report.Page.Orientation";
    String KEY_REPORT_PAGE_DEFAULT = "Report.Page.Default";
    String KEY_REPORT_PAGE_WIDTH = "Report.Page.Width";
    String KEY_REPORT_PAGE_HEIGHT = "Report.Page.Height";
    String KEY_REPORT_PAGE_MARGIN_TOP = "Report.Page.Margin.Top";
    String KEY_REPORT_PAGE_MARGIN_BOTTOM = "Report.Page.Margin.Bottom";
    String KEY_REPORT_PAGE_MARGIN_LEFT = "Report.Page.Margin.Left";
    String KEY_REPORT_PAGE_MARGIN_RIGHT = "Report.Page.Margin.Right";
    String KEY_REPORT_PAGE_BORDER = "Report.Page.Border";
    String KEY_REPORT_FONT_HEADER = "Report.Font.Header";
    String KEY_REPORT_FONT_TABLE = "Report.Font.Table";
    String KEY_REPORT_COLOUR_CANVAS = "Report.Colour.Canvas";
    String KEY_REPORT_COLOUR_TABLE = "Report.Colour.Table";
    String KEY_REPORT_COLOUR_TEXT = "Report.Colour.Text";
    String KEY_REPORT_PERIOD_REFRESH = "Report.Period.Refresh";
    String KEY_REPORT_LOG_ROWS = "Report.Log.Rows";
    String KEY_REPORT_ACTION_TRUNCATE = "Report.Action.Truncate";

    String KEY_FONT = "Font";
    String KEY_FONT_LABEL = "Font.Label";

    String KEY_LINE_WRAP_COUNT = "LineWrapCount";

    String KEY_COLOUR_TEXT = "Colour.Text";
    String KEY_COLOUR_TABLE = "Colour.Table";
    String KEY_COLOUR_CANVAS = "Colour.Canvas";

    String KEY_BUFFER_SIZE = "BufferSize";
    String KEY_ENABLE_LOCAL_ECHO = "Enable.LocalEcho";
    String KEY_ACTION_RUN = "Action.Run";
    String KEY_ACTION_CLEAR_CONSOLE = "Action.ClearConsole";
    String KEY_LOOK_AND_FEEL = "LookAndFeel";

    String KEY_DIMENSION_X = "Dimension.X";
    String KEY_DIMENSION_Y = "Dimension.Y";
    String KEY_DIMENSION_WIDTH = "Dimension.Width";
    String KEY_DIMENSION_HEIGHT = "Dimension.Height";
    String KEY_ENABLE_BACKUP = "Enable.Backup";
    String KEY_BACKUP_PERIOD = "Backup.Period";
    String KEY_ENABLE_CUSTOM_ICONS = "Enable.CustomIcons";
    String KEY_DIMENSION_DIVIDER_LOCATION = "Dimension.DividerLocation";
    String KEY_STATUS_BAR_TEXT_COLOUR = "StatusBar.Text.Colour";
    String KEY_STATUS_BAR_TEXT_FONT = "StatusBar.Text.Font";

    // Editors
    String KEY_EDITOR_ENABLE_DEBUG = "Editor.Enable.Debug";
    String KEY_EDITOR_COLOUR_CANVAS = "Editor.Colour.Canvas";
    String KEY_EDITOR_COLOUR_TEXT = "Editor.Colour.Text";
    String KEY_EDITOR_FONT_LABEL = "Editor.Font.Label";
    String KEY_EDITOR_FONT_DATA = "Editor.Font.Data";

    // Telnet
    String KEY_TELNET_ENABLE_DEBUG = "Enable.Debug";
    String KEY_TELNET_ENABLE_SSH = "Enable.SSH";
    String KEY_TELNET_CONNECTION_SOCKET_HOST = "Connection.Socket.Host";
    String KEY_TELNET_CONNECTION_SOCKET_PORT = "Connection.Socket.Port";
    String KEY_TELNET_CONNECTION_TIMEOUT_SECONDS = "Connection.Timeout.Seconds";
    String KEY_TELNET_CONNECTION_TIMEOUT_COMMAND = "Connection.Timeout.Command";
    String KEY_TELNET_FONT_DISPLAY = "Font.Display";
    String KEY_TELNET_COLOUR_DISPLAY_FOREGROUND = "Colour.Display.Foreground";
    String KEY_TELNET_COLOUR_DISPLAY_BACKGROUND = "Colour.Display.Background";
    String KEY_TELNET_COLOUR_CURSOR_FOREGROUND = "Colour.Cursor.Foreground";
    String KEY_TELNET_COLOUR_CURSOR_BACKGROUND = "Colour.Cursor.Background";
    String KEY_TELNET_TERMINAL_ID = "Terminal.ID";
    String KEY_TELNET_TERMINAL_BUFFER_SIZE = "Terminal.BufferSize";

    // MailDaemon
    String KEY_MAIL_ENABLE_SEND_MAIL = "Enable.SendMail";
    String KEY_MAIL_ENABLE_SSL = "Enable.SSL";
    String KEY_MAIL_ENABLE_AUTHENTICATION = "Enable.Authentication";
    String KEY_MAIL_HOST = "Host";
    String KEY_MAILUSERNAME = "Username";
    String KEY_MAIL_PASSWORD = "Password";
    String KEY_MAIL_TO_ADDRESS = "ToAddress";
    String KEY_MAIL_FROM_ADDRESS = "FromAddress";
    String KEY_MAIL_PERIOD = "Period";

    // WebServer
    String KEY_PROTOCOL = "Protocol";
    String KEY_HOST_NAME = "HostName";
    String KEY_PORT = "Port";
    String KEY_INDEX_FILE = "IndexFile";
    String KEY_CONFIGURATION_FILE = "ConfigurationFile";
    String KEY_RESOURCE_BASE = "ResourceBase";

    String KEY_DAO_IMAGE_URL = "Dao.Image.URL";

    // Always accessed as Observatory Properties via the Registry
    String KEY_DISPLAY_DATA_MAX = "DisplayData.Max";
    String KEY_PORTCONTROLLER_LATENCY = "PortController.Latency";
    }
