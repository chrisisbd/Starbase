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


public interface FrameworkMetadata
    {
    //----------------------------------------------------------------------------------------------
    // Miscellaneous

    String PREFIX                               = "[";
    String TERMINATOR                           = "]";
    String TERMINATOR_SPACE                     = "] ";
    String DELIMITER                            = " ";

    //----------------------------------------------------------------------------------------------
    // Targets come before the Actions

    String METADATA_TARGET                      = "[target=";
    String METADATA_TARGET_AUDIO_FILE           = "[target=audiofile]";
    String METADATA_TARGET_CHART                = "[target=chart]";
    String METADATA_TARGET_CLOCK                = "[target=clock]";
    String METADATA_TARGET_CLOCK_OFFSET         = "[target=clockoffset]";
    String METADATA_TARGET_COMMAND              = "[target=command]";
    String METADATA_TARGET_COMMUNICATOR         = "[target=communicator]";
    String METADATA_TARGET_COMPONENT            = "[target=component]";
    String METADATA_TARGET_CONFIGURATION        = "[target=configuration]";
    String METADATA_TARGET_CORRELATION          = "[target=correlation]";
    String METADATA_TARGET_CSV                  = "[target=csv]";
    String METADATA_TARGET_DATACONNECTION       = "[target=dataconnection]";
    String METADATA_TARGET_DATASET              = "[target=dataset]";
    String METADATA_TARGET_EPHEMERIS            = "[target=ephemeris]";
    String METADATA_TARGET_EVENTLOG             = "[target=eventlog]";
    String METADATA_TARGET_FILE                 = "[target=file]";
    String METADATA_TARGET_IMAGE                = "[target=image]";
    String METADATA_TARGET_INSTRUMENT           = "[target=instrument]";
    String METADATA_TARGET_INSTRUMENTLOG        = "[target=instrumentlog]";
    String METADATA_TARGET_JENKINS              = "[target=jenkins]";
    String METADATA_TARGET_LOG                  = "[target=log]";
    String METADATA_TARGET_MAP                  = "[target=map]";
    String METADATA_TARGET_MEMORY               = "[target=memory]";
    String METADATA_TARGET_METADATA             = "[target=metadata]";
    String METADATA_TARGET_POI                  = "[target=poi]";
    String METADATA_TARGET_PROPERTY             = "[target=property]";
    String METADATA_TARGET_RAWDATA              = "[target=rawdata]";
    String METADATA_TARGET_REPORT               = "[target=report]";
    String METADATA_TARGET_SANDBOX              = "[target=sandbox]";
    String METADATA_TARGET_SCATTER_PLOT         = "[target=scatterplot]";
    String METADATA_TARGET_STARINET             = "[target=starinet]";
    String METADATA_TARGET_TIME_PROXY           = "[target=time_proxy]";
    String METADATA_TARGET_TIMESERIES           = "[target=timeseries]";
    String METADATA_TARGET_TRANSLATOR           = "[target=datatranslator]";
    String METADATA_TARGET_TSV                  = "[target=tsv]";
    String METADATA_TARGET_TXT                  = "[target=txt]";
    String METADATA_TARGET_UDP_SERVER           = "[target=udp_server]";
    String METADATA_TARGET_UNKNOWN              = "[target=unknown]";
    String METADATA_TARGET_XML                  = "[target=xml]";
    String METADATA_TARGET_XYDATASET            = "[target=xydataset]";

    //----------------------------------------------------------------------------------------------
    // Actions come after the Targets, and have leading and trailing spaces

    String METADATA_ACTION                      = " [action=";
    String METADATA_ACTION_ADD_METADATA         = " [action=addmetadata] ";
    String METADATA_ACTION_ADD_SUBSCRIPTION     = " [action=addsubscription] ";
    String METADATA_ACTION_APPEND               = " [action=append] ";
    String METADATA_ACTION_ASSEMBLE             = " [action=assemble] ";
    String METADATA_ACTION_CALCULATE            = " [action=calculate] ";
    String METADATA_ACTION_CAPTURE              = " [action=capture] ";
    String METADATA_ACTION_CAPTURE_RESET        = " [action=capture.reset] ";
    String METADATA_ACTION_CHANGE               = " [action=change] ";
    String METADATA_ACTION_COMMAND              = " [action=command] ";
    String METADATA_ACTION_COLLECT_METADATA     = " [action=collectmetadata] ";
    String METADATA_ACTION_CONNECT              = " [action=connect] ";
    String METADATA_ACTION_CREATE               = " [action=create] ";
    String METADATA_ACTION_DISCONNECT           = " [action=disconnect] ";
    String METADATA_ACTION_DISCOVER             = " [action=discover] ";
    String METADATA_ACTION_EDIT_METADATA        = " [action=editmetadata] ";
    String METADATA_ACTION_EXECUTE              = " [action=execute] ";
    String METADATA_ACTION_EXPORT               = " [action=export] ";
    String METADATA_ACTION_FILTER               = " [action=filter] ";
    String METADATA_ACTION_FILTERING            = " [action=filtering] ";
    String METADATA_ACTION_FIND                 = " [action=find] ";
    String METADATA_ACTION_GET_ADDRESS          = " [action=getaddress] ";
    String METADATA_ACTION_IMPORT               = " [action=import] ";
    String METADATA_ACTION_IMPORT_METADATA      = " [action=importmetadata] ";
    String METADATA_ACTION_IMPORT_POI           = " [action=importpoi] ";
    String METADATA_ACTION_INITIALISE           = " [action=initialise] ";
    String METADATA_ACTION_INTEGRATE            = " [action=integrate] ";
    String METADATA_ACTION_INTEGRATING          = " [action=integrating] ";
    String METADATA_ACTION_LOGGING              = " [action=logging] ";
    String METADATA_ACTION_LOGIN                = " [action=login] ";
    String METADATA_ACTION_LOGOUT               = " [action=logout] ";
    String METADATA_ACTION_OPEN                 = " [action=open] ";
    String METADATA_ACTION_PARSE                = " [action=parse] ";
    String METADATA_ACTION_PLAY                 = " [action=play] ";
    String METADATA_ACTION_PUBLISH              = " [action=publish] ";
    String METADATA_ACTION_READ                 = " [action=read] ";
    String METADATA_ACTION_RECALCULATE          = " [action=recalculate] ";
    String METADATA_ACTION_RECEIVE              = " [action=receive] ";
    String METADATA_ACTION_RECEIVE_INC          = " [action=receive.inc] ";
    String METADATA_ACTION_REMOVE               = " [action=remove] ";
    String METADATA_ACTION_REMOVE_METADATA      = " [action=removemetadata] ";
    String METADATA_ACTION_REMOVE_POI           = " [action=removepoi] ";
    String METADATA_ACTION_REMOVE_SUBSCRIPTION  = " [action=removesubscription] ";
    String METADATA_ACTION_REQUEST_HTTP         = " [action=request.http] ";
    String METADATA_ACTION_REQUEST_UDP          = " [action=request.udp] ";
    String METADATA_ACTION_RESET                = " [action=reset] ";
    String METADATA_ACTION_RUN                  = " [action=run] ";
    String METADATA_ACTION_SEGMENT              = " [action=segment] ";
    String METADATA_ACTION_SET                  = " [action=set] ";
    String METADATA_ACTION_SET_ADDRESS          = " [action=setaddress] ";
    String METADATA_ACTION_SET_METADATA         = " [action=setmetadata] ";
    String METADATA_ACTION_START                = " [action=start] ";
    String METADATA_ACTION_STOP                 = " [action=stop] ";
    String METADATA_ACTION_SYNCHRONISE          = " [action=synchronise] ";
    String METADATA_ACTION_TRANSFORM            = " [action=transform] ";
    String METADATA_ACTION_TRANSLATOR           = " [action=translator] ";
    String METADATA_ACTION_TRANSLATING          = " [action=translating] ";
    String METADATA_ACTION_TRANSMIT             = " [action=transmit] ";
    String METADATA_ACTION_UPLOAD               = " [action=upload] ";
    String METADATA_ACTION_VALIDATE             = " [action=validate] ";
    String METADATA_ACTION_WAIT                 = " [action=wait] ";
    String METADATA_ACTION_WRITE                = " [action=write] ";
    String METADATA_ACTION_WRITE_MAP            = " [action=writemap] ";
    String METADATA_ACTION_DAO_INIT             = " [action=dao.initialise] ";
    String METADATA_ACTION_DAO_DISPOSE          = " [action=dao.dispose] ";

    //----------------------------------------------------------------------------------------------
    // Composite Targets and Actions

    String STATUS_FAIL                          = " [status=fail]";

    String METADATA_DATABASE_OPEN               = "[target=database] [action=open] ";
    String METADATA_DATABASE_CLOSE              = "[target=database] [action=close] ";

    String METADATA_FRAMEWORK_START             = "[target=framework] [action=start] ";
    String METADATA_FRAMEWORK_LOGIN             = "[target=framework] [action=login] ";
    String METADATA_FRAMEWORK_LOGOUT            = "[target=framework] [action=logout] ";
    String METADATA_FRAMEWORK_UPDATE            = "[target=framework] [action=update] ";
    String METADATA_FRAMEWORK_RESET             = "[target=framework] [action=reset] ";

    // ToDo Strictly this is in the wrong place
    String METADATA_OBSERVATORY_UPDATE          = "[target=observatory] [action=update] ";

    String METADATA_PLUGIN_START                = "[target=plugin] [action=start]";
    String METADATA_PLUGIN_START_FAIL           = METADATA_PLUGIN_START + STATUS_FAIL;

    String METADATA_PLUGIN_SHUTDOWN             = "[target=plugin] [action=shutdown]";
    String METADATA_PLUGIN_SHUTDOWN_FAIL        = METADATA_PLUGIN_SHUTDOWN + STATUS_FAIL;

    String METADATA_TASK_INITIALISE             = "[target=task] [action=initialise]";
    String METADATA_TASK_INITIALISE_FAIL        = METADATA_TASK_INITIALISE + STATUS_FAIL;

    String METADATA_TASK_START                  = "[target=task] [action=start]";
    String METADATA_TASK_START_FAIL             = METADATA_TASK_START + STATUS_FAIL;

    String METADATA_TASK_IDLE                   = "[target=task] [action=idle]";
    String METADATA_TASK_IDLE_FAIL              = METADATA_TASK_IDLE + STATUS_FAIL;

    String METADATA_TASK_SHUTDOWN               = "[target=task] [action=shutdown]";
    String METADATA_TASK_SHUTDOWN_FAIL          = METADATA_TASK_SHUTDOWN + STATUS_FAIL;

    String METADATA_PLUGIN_STATE_ERROR = "[target=plugin] [state=error]";
    String METADATA_TASK_STATE_ERROR = "[target=task] [state=error]";

    // Editors
    String METADATA_FRAMEWORK_EDIT = "[target=framework] [action=edit] [source=frameworkeditor]";
    String METADATA_PLUGIN_EDIT = "[target=plugin] [action=edit] [source=plugineditor]";
    String METADATA_TASK_EDIT = "[target=task] [action=edit] [source=taskeditor]";
    String METADATA_QUERY_EDIT = "[target=query] [action=edit] [source=queryeditor] ";
    String METADATA_STRING_EDIT = "[target=string] [action=edit] [source=stringeditor] ";
    String METADATA_EXCEPTION_EDIT = "[target=exception] [action=edit] [source=exceptioneditor] ";
    String METADATA_PROPERTY_EDIT = "[target=property] [action=edit] [source=propertyeditor] ";

    // Registry Model
    String METADATA_MODEL_INITIALISE = "[target=registry_model] [action=initialise] ";
    String METADATA_MODEL_ASSEMBLY = "[target=registry_model] [action=assembly] ";

    String METADATA_UI_LOOKANDFEEL = "[target=look&feel] [action=load]";
    String METADATA_PRINT_REPORT = "[target=report] [action=print]";
    String METADATA_MBEAN_SERVER_INITIALISE = "[target=mbean_server] [action=initialise]";
    String METADATA_HTTP_ADAPTOR_START= "[target=http_adaptor] [action=start]";
    String METADATA_MBEAN_REGISTER = "[target=mbean] [action=register]";
    String METADATA_MBEAN_UNREGISTER = "[target=mbean] [action=unregister]";
    String METADATA_SERVER = "[target=server] ";
    // Task Controller
    String METADATA_INACTIVEPARENT = "[status=inactiveparent] ";
    String METADATA_INACTIVE = "[status=inactive] ";
    String METADATA_LOCKED = "[status=locked]";


    // Single Items
    String METADATA_ITEM                        = "[item=";
    String METADATA_VALUE                       = "[value=";

    // Origins come after the Actions
    String METADATA_ORIGIN                      = " [origin=";
    String METADATA_ORIGIN_MACRO                = " [origin=macro]";
    String METADATA_ORIGIN_USER                 = " [origin=user]";

    String METADATA_ACTIVE = "[active=";
    String METADATA_ADDRESS = "[address=";
    String METADATA_ADDRESS_REMOTE = "[remote_address=";
    String METADATA_CAPTURE = "[capture=";
    String METADATA_CAPTURE_TRIGGER = "[capture.trigger=";
    String METADATA_CATEGORY = "[category=";
    String METADATA_CHANNEL = "[channel=";
    String METADATA_CHANNEL_COUNT = "[channels=";
    String METADATA_CODESIZE = "[codesize=";
    String METADATA_COMPRESSED = "[compressed=";
    String METADATA_CONNECTIONMODE = "[connection.mode=";
    String METADATA_CONTEXT = "[context=";
    String METADATA_COUNT = "[count=";
    String METADATA_COUNTRY = "[country=";
    String METADATA_DATAFORMAT = "[dataformat=";
    String METADATA_DATASET = "[dataset=";
    String METADATA_DATASTORE = "[datastore=";
    String METADATA_DATE = "[date=";
    String METADATA_DESCRIPTION = "[description=";
    String METADATA_DETAIL = "[detail=";
    String METADATA_DRIFT_RATE = "[driftrate=";
    String METADATA_ERROR = "[error=";
    String METADATA_EXCEPTION = "[exception=";
    String METADATA_FAIL = "fail]";
    String METADATA_FILENAME = "[filename=";
    String METADATA_FILLUNUSED = "[fillunused=";
    String METADATA_FILTERNAME = "[filter.name=";
    String METADATA_FINAL = "[final=";
    String METADATA_FORMAT = "[format=";
    String METADATA_GROUP = "[group=";
    String METADATA_HASL = "[hasl=";
    String METADATA_HEIGHT = "[height=";
    String METADATA_HOSTNAME = "[hostname=";
    String METADATA_IDENTIFIER = "[identifier=";
    String METADATA_INITIAL = "[initial=";
    String METADATA_INTERVAL = "[interval=";
    String METADATA_IP = "[ip=";
    String METADATA_KEY = "[key=";
    String METADATA_LANGUAGE = "[language=";
    String METADATA_LATITUDE = "[latitude=";
    String METADATA_LENGTH = "[length=";
    String METADATA_LINE = "[line=";
    String METADATA_LOADATSTART = "[loadatstart=";
    String METADATA_LOADER_VERSION = "[loaderversion=";
    String METADATA_LOCALDIR = "[local.dir=";
    String METADATA_LOCALFILE = "[local.file=";
    String METADATA_LOCALPATH = "[local.path=";
    String METADATA_LONGITUDE = "[longitude=";
    String METADATA_MESSAGE = "[message=";
    String METADATA_MODE = "[mode=";
    String METADATA_MODULE = "[module=";
    String METADATA_NAME = "[name=";
    String METADATA_NODE = "[node=";
    String METADATA_OFFSET = "[offset=";
    String METADATA_OUTPUT = "[output=";
    String METADATA_PASSWORD = "[password=";
    String METADATA_PATHNAME = "[pathname=";
    String METADATA_PAYLOAD = "[payload=";
    String METADATA_PERIOD = "[period=";
    String METADATA_PORT = "[port=";
    String METADATA_PWD = "[pwd=";
    String METADATA_RATIO = "[ratio=";
    String METADATA_REASON = "[reason=";
    String METADATA_REMOTEDIR = "[remote.dir=";
    String METADATA_REMOTEFILE = "[remote.file=";
    String METADATA_REMOTEFILE_INC = "[remote.file.inc=";
    String METADATA_RETRY = "[retryid=";
    String METADATA_ROLE = "[role=";
    String METADATA_SAMPLES = "[samples=";
    String METADATA_SAVE = "[save=";
    String METADATA_SCALE_FACTOR = "[scalefactor=";
    String METADATA_SERIAL_PREFIX = "[serial.";
    String METADATA_SOURCE = "[source=";
    String METADATA_STARSCRIPT = "[starscript=";
    String METADATA_STATE = "[state=";
    String METADATA_STATUS = "[status=";
    String METADATA_STEP = "[step=";
    String METADATA_SUNRISE = "[sunrise=";
    String METADATA_SUNSET = "[sunset=";
    String METADATA_SYSTEM = "[system=";
    String METADATA_TARGETCPU = "[targetcpu=";
    String METADATA_TARGETNETWORK = "[targetnetwork=";
    String METADATA_TEMPERATURE = "temperature";
    String METADATA_TEXT = "[text=";
    String METADATA_TIME = "[time=";
    String METADATA_TIMECONSTANT = "[timeconstant=";
    String METADATA_TIMESTAMP = "[timestamp=";
    String METADATA_TIMEZONE = "[timezone=";
    String METADATA_TRANSFERMODE = "[transfer.mode=";
    String METADATA_TYPE = "[type=";
    String METADATA_URL_REFERRER = "[url.referrer=";
    String METADATA_URL_REQUEST = "[url.request=";
    String METADATA_USER = "[user=";
    String METADATA_USERNAME = "[username=";
    String METADATA_VERIFY = "[verify=";
    String METADATA_VERSION = "[version=";
    String METADATA_VERSION_PREFIX = "[version.";
    String METADATA_WARNING = "[warning=";
    String METADATA_WIDTH = "[width=";

    // Serial Ports
    String METADATA_PORTNAME = "[portname=";
    String METADATA_PORTOWNER = "[portowner=";
    String METADATA_PORTTYPE = "[porttype=";


    // Command Names
    String COMMAND_CORE_RESET = "reset";
    String COMMAND_CORE_PING = "ping";
    String COMMAND_CORE_GET_CONFIGURATION = "getConfiguration";
    String COMMAND_CORE_GET_MODULE_CONFIGURATION = "getModuleConfiguration";
    String COMMAND_CORE_GET_CONFIGURATION_BLOCK_COUNT = "getConfigurationBlockCount";
    String COMMAND_CORE_GET_CONFIGURATION_BLOCK = "getConfigurationBlock";

    // Command Parameter Names
    String PARAMETER_CONFIGURATION_MODULEID = "Configuration.ModuleID";
    String PARAMETER_CONFIGURATION_BLOCKID = "Configuration.BlockID";
    String PARAMETER_CONFIGURATION_DATA = "Configuration.Data";
    //String PARAMETER_CONFIGURATION_FILENAME = "Configuration.Filename";
    String PARAMETER_DATA_BLOCKID = "Data.BlockID";

    // Response Names
    String RESPONSE_ACK = "Ack";
    String RESPONSE_CONFIGURATION_XML = "Configuration.XML";
    String RESPONSE_CONFIGURATION_MODULE = "Configuration.Module";
    String RESPONSE_CONFIGURATION_BLOCK_COUNT = "Configuration.BlockCount";
    String RESPONSE_CONFIGURATION_BLOCK = "Configuration.Block";

    // Results
    String METADATA_RESULT = "[result=";
    String METADATA_RESULT_SUCCESS = "[result=success]";
    String METADATA_RESULT_NOT_FOUND = "[result=not found, or invalid syntax]";
    String METADATA_RESULT_INVALID_FILENAME = "[result=Filename is invalid]";
    String METADATA_RESULT_FILE_NOT_FOUND = "[result=File not found]";
    String METADATA_RESULT_NO_DATA = "[result=No data]";
    String METADATA_RESULT_NOT_IN_MDD = "[result=Key not found in MetadataDictionary]";
    }
