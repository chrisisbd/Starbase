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

//  27-04-00    LMN created file
//  18-01-02    LMN started again...
//  26-04-02    LMN added handleComponentException()
//  27-04-02    LMN changed Exception message format
//  05-05-03    LMN removed ApplicationID storage
//  20-06-03    LMN rationalised exception handlers
//  16-07-03    LMN tidied up Exception names at last!
//  04-09-03    LMN implementing database lookup of strings
//  05-04-05    LMN tidying logging, changed signatures to include FrameworkID
//  03-03-06    LMN removed Exception handlers to Logger
//  14-03-06    LMN converted to an interface...

package org.lmn.fc.common.constants;


public interface FrameworkStrings
    {
    String VERSION_LOGIN    = "Version ";
    String VERSION_MENU     = "Version ";
    String VERSION_BRANDING = "Starbase v";
    String VERSION_RESPONSE = "[version=";
    String BUILD_RESPONSE   = "[build=";
    String STATUS_RESPONSE  = "[status=";

    String DEFAULT_USER     = "user";
    String DEFAULT_PASSWORD = "starbase";

    String PATH_PLUGINS_OBSERVATORY = "plugins/observatory/";

    String AWAITING_DEVELOPMENT     = "Awaiting development!";
    String MSG_AWAITING_DEVELOPMENT = "This function is awaiting development!";

    String UNEXPECTED = "Unexpected Error if this appears!";

    String STARSCRIPT = "Starscript";

    String UKRAA_LOGO_FILENAME           = "ukraa-logo.png";
    String UKRAA_LOGO_SEPARATOR_FILENAME = "ukraa-logo-separator.png";

    boolean EXPANDER_NODE = true;
    boolean LEAF_NODE     = false;

    String INVALID_RESOURCE_KEY = "InvalidResourceKey";

    //----------------------------------------------------------------------------------------------
    // Characters

    char NULL = '\0';
    String EMPTY_STRING = "";
    String NO_DATA = "NO_DATA";
    String SPACE = " ";
    String MINUS = "-";
    String HYPHEN = "-";
    String UNDERSCORE = "_";
    String PLUS = "+";
    String ZERO = "0";
    String PERCENT = "%";
    String HTML_NBSPACE = "&nbsp;";
    String DOT = ".";
    String COMMA = ",";
    String COLON = ":";
    String AMPERSAND = "&";
    String EQUALS = "=";
    String EQUALS_SPACED = " = ";
    String ELLIPSIS = "...";
    String ACTION_COLON = ":" + SPACE;
    String QUERY = "?";
    String CURSOR = "_";
    String LEFT_PARENTHESIS = "(";
    String RIGHT_PARENTHESIS = ")";
    String LEFT_BRACE = "{";
    String RIGHT_BRACE = "}";
    String RIGHT_SLASH = "/";
    String ROLE = "Role";

    //----------------------------------------------------------------------------------------------
    // ContextActions and their Tooltips

    String ACTION_LOGOUT = "Logout";
    String ACTION_EXIT = "Exit";
    String ACTION_SAVE = "Save Changes";
    String ACTION_GC = "Garbage Collector";
    String ACTION_FULL_SCREEN = "Toggle Full Screen";
    String ACTION_TOGGLE_ICONS = "Toggle Display Icons";
    String ACTION_SHOW_ACTIONS = "Show the Action List";

    String TOOLTIP_ACTION_LOGOUT = "Log out the current User";
    String TOOLTIP_ACTION_EXIT = "Leave the Framework";
    String TOOLTIP_ACTION_SAVE = "Save all changes to the Data Store";
    String TOOLTIP_ACTION_GC = "Run the Garbage Collector";
    String TOOLTIP_ACTION_FULL_SCREEN = "Toggle full screen mode";
    String TOOLTIP_ACTION_TOGGLE_ICONS = "Toggle display icons";
    String TOOLTIP_ACTION_SHOW_ACTIONS = "Show the Action List";

    String TOOLTIP_CATEGORY_BUILDER_MODULE = "Builder Module: ";

    String TOOLTIP_CATEGORY_BUILDER_COMMAND = "Builder Command: ";
    String TOOLTIP_COMMAND_NOT_IN_POOL = "Command not in pool: ";
    String TOOLTIP_CATEGORY_CAPTURE_COMMAND = "Data Capture Command: ";
    String TOOLTIP_CATEGORY_IMPORT_COMMAND = "Data Import Command: ";

    //----------------------------------------------------------------------------------------------
    // Status Messages

    String STATUS_EVENTS = "The log shows all events for";
    String STATUS_HELP = "Showing Help for";
    String STATUS_JMX = "Showing the JMX Management Panel for ";
    String STATUS_TASKS = "Showing Tasks for";
    String STATUS_JAVADOC = "Showing Javadoc for";
    String STATUS_ACTIONS = "The log shows all Actions for";
    String MSG_WELCOME = "Welcome to";
    String MSG_CHARACTERS = "characters";
    String HEADER_REPORT_CREATED = "Report created at";
    String MSG_BEANS = "Beans";

    //----------------------------------------------------------------------------------------------

    String USER = "user=";
    String PASSWD = "password=";
    String MSG_NULL = "NULL";
    String INDENT = "    ";
    String ZERO_TIME = "00:00:00";
    String MILLI_SEC = "msec";
    String SUFFIX_MBEAN = "MBean";
    String HTML_DEVELOPER_PREFIX = "<html><i><font color=red>";
    String HTML_DEVELOPER_SUFFIX = "</font></i></html>";
    String HTML_ADMINISTRATOR_PREFIX = "<html><i><font color=red>";
    String HTML_ADMINISTRATOR_SUFFIX = "</font></i></html>";

    String EXCEPTION_CREATE_FRAMEWORK = "Unable to create a Framework";
    String EXCEPTION_CREATE_PLUGIN = "Unable to create a Plugin";
    String EXCEPTION_CREATE_TASK = "Unable to create a Task";
    String EXCEPTION_CREATE_ROLE = "Unable to create a Role";
    String EXCEPTION_CREATE_USER = "Unable to create a User";
    String EXCEPTION_CREATE_LANGUAGE = "Unable to create a Language";
    String EXCEPTION_CREATE_COUNTRY = "Unable to create a Country";
    String EXCEPTION_CREATE_LOOKANDFEEL = "Unable to create a LookAndFeel";
    String EXCEPTION_CREATE_DATATYPE = "Unable to create a DataType";
    String EXCEPTION_CREATE_PROPERTY = "Unable to create a Property";
    String EXCEPTION_CREATE_STRING = "Unable to create a String";
    String EXCEPTION_CREATE_EXCEPTION = "Unable to create an Exception";
    String EXCEPTION_CREATE_QUERY = "Unable to create a Query";

    String EXCEPTION_START_FRAMEWORK = "Unable to start the Framework";

    String EXCEPTION_HOST_TASK = "Invalid Task!";
    String EXCEPTION_UI = "Invalid UI!";
    String EXCEPTION_SELECT_DATA = ".selectData()";
    String EXCEPTION_INSERT_DATA = ".insertData()";
    String EXCEPTION_UPDATE_DATA = ".updateData()";
    String EXCEPTION_DELETE_DATA = ".deleteData()";
    String EXCEPTION_TASK_INITIALISE = "Unable to initialise Task";
    String EXCEPTION_RUN_TASK = ".runTask()";
    String EXCEPTION_SHUTDOWN_TASK = ".shutdownTask()";
    String EXCEPTION_UPDATE_NODES = ".updateNodes()";
    String EXCEPTION_ACTION_PERFORMED = ".actionPerformed()";
    String EXCEPTION_PARAMETER_NULL = "One or more parameters are NULL";
    String EXCEPTION_PARAMETER_TYPE = "One or more parameters has an incorrect type";
    String EXCEPTION_PARAMETER_RANGE = "One or more parameters are out of range";
    String EXCEPTION_PARAMETER_INVALID = "One or more parameters are invalid";
    String EXCEPTION_OBJECT_NAME = "The MBean server ObjectName is malformed";
    String EXCEPTION_XML_VALIDATION = "The XML does not agree with the schema";
    String EXCEPTION_FAULTY_HEADER = "Faulty Report header";
    String EXCEPTION_GENERATE_REPORT = ".generateReport()";
    String EXCEPTION_REPORT_INITIALISATION = "Unable to initialise a Report";
    String EXCEPTION_DATABASE_CLOSED = "Database is closed";
    String EXCEPTION_DATABASE_UNAVAILABLE = "The Database is not available";
    String EXCEPTION_TAB_LISTENER = "addTabListener()";
    String EXCEPTION_DATASTORE_INVALID = "Invalid DataStore";
    String EXCEPTION_NO_IPADDRESS = "Instrument Controller does not have an IPAddress";
    String EXCEPTION_UNRECOGNISED_POITYPE = "Unrecognised PointOfInterestType";
    String EXCEPTION_UNRECOGNISED_METADATATYPE = "Unrecognised MetadataType";

    String MSG_ERROR = "Error!";
    String MSG_DOES_NOT_EXIST = "does not exist";
    String MSG_INCORRECT_FORMAT = "is not in the correct format";
    String MSG_UNEXPECTED_ERROR = "An unexpected error has occurred!";
    String TITLE_ACTION_LIST = "ActionList";
    String MSG_ACTION_LIST = "The following Action requires your attention!";
    String EMBEDDED_DATABASE_THREAD = "Framework MySQL";

    String MSG_TASK_START_ALL = "Starting Tasks";
    String MSG_TASK_INIT = "Initialised Task";
    //String MSG_TASK_START = "Starting Task";

    // Database
    String DATABASE_NULL                = "No database found";
    String DATABASE_FIND_JDBC           = "Unable to find the JDBC class";
    String DATABASE_OPEN                = "Unable to open the database";
    String DATABASE_CLOSE               = "Unable to close the database";
    String DATABASE_NOTACTIVE           = "There is no connection to the database";
    String DATABASE_ALREADYACTIVE       = "The connection to the database is already open";
    String DATABASE_LOGMESSAGE          = "Unable to log the message";
    String INVALID_RESOURCE_TYPE        = "The RootType is invalid";
    String RESOURCE_CONFIGURATION       = "The resource is incorrectly configured";
    String RESOURCE_UNAMED              = "The resource has no name!";
    String RESOURCE_LENGTH              = "The resource string is too long";

    // Languages
    String LOAD_LANGUAGES               = "Unable to load the Languages";
    String FIND_LANGUAGES               = "No Languages found";
    String SET_LANGUAGE                 = "Unable to set the requested Language";
    String INVALID_LANGUAGE             = "The requested Language ISO code is invalid";
    String UNAVAILABLE_LANGUAGE         = "The requested Language is not installed";

    // Countries
    String LOAD_COUNTRIES               = "Unable to load the Countries";
    String FIND_COUNTRIES               = "No Countries found";
    String SET_COUNTRY                  = "Unable to set the requested Country";
    String INVALID_COUNTRY              = "The Country is invalid";
    String UNAVAILABLE_COUNTRY          = "The requested Country is not installed";

    // Queries
    String INVALID_QUERY_TYPE           = "The Query type is invalid";
    String LOAD_BOOTSTRAP_QUERIES       = "Unable to load the SQL Bootstrap Queries";
    String LOAD_QUERIES                 = "Unable to load the SQL Queries";
    String GET_QUERY                    = "Could not find the requested SQL Query";
    String LOAD_FRAMEWORK_QUERIES       = "Unable to load the Framework Queries";
    String SAVE_FRAMEWORK_QUERIES       = "Unable to save the Framework Queries";
    String LOAD_APPLICATION_QUERIES     = "Unable to load the Application Queries";
    String SAVE_APPLICATION_QUERIES     = "Unable to save the Application Queries";
    String LOAD_COMPONENT_QUERIES       = "Unable to load the Component Queries";
    String SAVE_COMPONENT_QUERIES       = "Unable to save the Component Queries";

    // Exceptions
    String ATOM_STARTUP                 = "Failed to start Atom";
    String LOAD_RESOURCE                = "Unable to load Resource";
    String INVALID_EXCEPTION_TYPE       = "The Exception type is invalid";
    String INVALID_EXCEPTION_KEY        = "The Exception ResourceKey is invalid";
    String LOAD_FRAMEWORK_EXCEPTIONS    = "Unable to load the Framework Exceptions";
    String SAVE_FRAMEWORK_EXCEPTIONS    = "Unable to save the Framework Exceptions";
    String FIND_FRAMEWORK_EXCEPTIONS    = "No Framework Exceptions were found";
    String KEY_FRAMEWORK_EXCEPTION      = "Framework Exception ResourceKey not valid";
    String LOAD_APPLICATION_EXCEPTIONS  = "Unable to load the Application Exceptions";
    String SAVE_APPLICATION_EXCEPTIONS  = "Unable to save the Application Exceptions";
    String KEY_APPLICATION_EXCEPTION    = "Application Exception ResourceKey not valid";
    String LOAD_COMPONENT_EXCEPTIONS    = "Unable to load the Component Exceptions";
    String SAVE_COMPONENT_EXCEPTIONS    = "Unable to save the Component Exceptions";
    String KEY_COMPONENT_EXCEPTION      = "Component Exception ResourceKey not valid";

    // Strings
    String INVALID_STRING_TYPE          = "The String type is not valid";
    String INVALID_STRING_KEY           = "The String key is not valid";
    String LOAD_FRAMEWORK_STRINGS       = "Unable to load the Framework Strings";
    String SAVE_FRAMEWORK_STRINGS       = "Unable to save the Framework Exceptions";
    String KEY_FRAMEWORK_STRING         = "Framework String key not valid";
    String LOAD_APPLICATION_STRINGS     = "Unable to load the Application Strings";
    String SAVE_APPLICATION_STRINGS     = "Unable to save the Application Strings";
    String KEY_APPLICATION_STRING       = "Application Strings ResourceKey not valid";
    String LOAD_COMPONENT_STRINGS       = "Unable to load the Component Strings";
    String SAVE_COMPONENT_STRINGS       = "Unable to save the Component Strings";
    String KEY_COMPONENT_STRING         = "Component Strings ResourceKey not valid";

    // Properties
    String INVALID_PROPERTY_KEY         = "The Property key is not valid";
    String EXCEPTION_INVALID_FORMAT     = "Invalid format pattern for output";
    String EXCEPTION_PARAMETERJDNEG     = "Julian Days cannot be negative";
    String EXCEPTION_INVALIDTIMESYSTEM  = "Time system identifier not found";
    String EXCEPTION_STATUSOUTOFRANGE   = "Invalid number of StatusLights requested by constructor";
    String EXCEPTION_STATUSLIGHTOUTOFRANGE = "Light index number of StatusLight is out of range";
    String EXCEPTION_STATUSLABELTOOLONG = "Requested label for StatusLight is too long";
    String EXCEPTION_FILESYSTEM = "An error occurred in the filesystem";
    String EXCEPTION_OUTOFRANGE = "The parameter value is out of range";
    String EXCEPTION_NO_FRAMEWORK = "The Framework cannot be found in the Registry";
    String EXCEPTION_ROOT_TASK_NULL = "Interface Task is NULL";
    String EXCEPTION_INVALID_ROOT_LEVEL = "The Root Level is invalid";
    String EXCEPTION_INVALID_USERDATA  = "Invalid UserData";
    String EXCEPTION_INVALID_FONTDATA  = "Invalid Font";
    String EXCEPTION_INVALID_REGISTRY_MODEL  = "The RegistryModel is invalid";
    String EXCEPTION_RESOURCE_NOTFOUND  = "The Resource could not be found";
    String SPLASHSCREEN_START_FRAMEWORK         = "Starting Framework";
    String LOG_MBEAN_UNABLE_TO_UNREGISTER = "MBean server Unable to unregister plugin";
    String MSG_FRAMEWORK_ERROR = "Unable to load the Framework";
    String MSG_SHUTDOWN = "Please wait for the Framework, Plugins and Tasks to be shut down";
    String DIALOG_SHUTDOWN = "Shutdown";

    String SYSOUT_RUN_GC = "Running the Garbage Collector";
    String SYSOUT_GC_MEMORY_TOTAL = "Total Memory";
    String SYSOUT_GC_MEMORY_FREE = "Free Memory";
    String MSG_CANNOT_COMPLETE = "The action cannot be completed because";
    String MSG_NO_RENDERER = "no renderer is available";

    String HTML_PREFIX = "<html>";
    String HTML_SUFFIX = "</html>";

    String HTML_PREFIX_FONT_BLUE = "<html><font color=blue>";
    String HTML_SUFFIX_FONT = "</font></html>";

    String HTML_BREAK = "<br>";
    String HTML_NBSP = "&nbsp;";

    String PREFIX_BOLD = "<b>";
    String SUFFIX_BOLD = "</b>";

    String PREFIX_ITALIC = "<i>";
    String SUFFIX_ITALIC = "</i>";

    String HTML_PREFIX_BOLD = "<html><b>";
    String HTML_SUFFIX_BOLD = "</b></html>";

    String HTML_PREFIX_ITALIC = "<html><i>";
    String HTML_SUFFIX_ITALIC = "</i></html>";

    String HTML_PREFIX_FONT_COLOR = "<html><i><font color=";
    String HTML_SUFFIX_FONT_COLOR = "</font></i></html>";

    String HTML_PREFIX_MACRO_IDENTIFIER = "<html><font color=blue>";
    String HTML_SUFFIX_MACRO_IDENTIFIER = "</font></html>";

    String HTML_PREFIX_MACRO_LABEL = "<html><font color=blue>";
    String HTML_SUFFIX_MACRO_LABEL = "</font></html>";

    String HTML_PREFIX_MACRO_COMMENT = "<html><i><font color=gray>";
    String HTML_SUFFIX_MACRO_COMMENT = "</font></i></html>";

    String HTML_PREFIX_MACRO_DESCRIPTION = "<html><font color=black>";
    String HTML_SUFFIX_MACRO_DESCRIPTION = "</font></html>";

    // This does not need the HTML tags because it is inside the Starscript block
    String HTML_PREFIX_MACRO_PARAMETER = "<font color=green>";
    String HTML_SUFFIX_MACRO_PARAMETER = "</font>";

    String STREAM_TX_REMOTE = "TxRemote";
    String STREAM_TX_LOCAL = "TxLocal";
    String STREAM_RX_REMOTE = "RxRemote";
    String STREAM_RX_LOCAL = "RxLocal";

    String MSG_NO_STARSCRIPT = STARSCRIPT + " not found";
    String MSG_NO_BYTES = "";
    String MSG_UNABLE_TO_READ = "Unable to read";

    String MSG_NO_DATA = "No Data records were present";
    String MSG_UNSUPPORTED_DATA_FORMAT = "Unsupported Data Format";
    String MSG_UNSUPPORTED_FORMAT = "Unsupported Data Format or Filter type";
    String MSG_UNSUPPORTED_MODE = "Unsupported mode";
    String MSG_UNSUPPORTED_OSCILLATOR_CONFIG = "Unsupported mixer, audio format, or waveform type";
    String MSG_CHANNEL_RANGE = "Channel ID out of range";
    String MSG_INVALID_DATASET_TYPE = "The Dataset Type is invalid";
    String MSG_INVALID_TIMESTAMPED_RAWDATA = "Timestamped RawData is invalid for this operation";
    String MSG_INVALID_INDEXED_RAWDATA = "Indexed RawData is invalid for this operation";
    String MSG_INVALID_TIME_SERIES = "TimeSeriesCollection is invalid for this operation";
    String MSG_INVALID_XY_SERIES = "XYSeriesCollection is invalid for this operation";

    String NO_CLOCK = "No Clock";
    }
