package org.lmn.fc.model.logging;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.impl.EventLogHsqldbDAO;
import org.lmn.fc.model.logging.impl.EventLogMySqlDAO;
import org.lmn.fc.model.logging.impl.EventLogXmlDAO;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelControllerInterface;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryModelController;
import org.lmn.fc.model.root.RootPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * The Framework Logger.
 */

public final class Logger implements FrameworkConstants,
                                     FrameworkMetadata,
                                     FrameworkStrings,
                                     ResourceKeys
    {
    // String Resources
    public static final String CONSOLE_SEPARATOR_MAJOR = "====================================================================================================";
    public static final String CONSOLE_SEPARATOR_MINOR = "....................................................................................................";
    public static final String CONSOLE_SEPARATOR       = "\n----------------------------------------------------------------------------------------------------";

    private static final FrameworkDatabase DATABASE = FrameworkDatabase.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    private static final RegistryModelControllerInterface MODEL_CONTROLLER = RegistryModelController.getInstance();
    private static final LoaderProperties LOADER_PROPERTIES = LoaderProperties.getInstance();

    private static final boolean DEBUG = false;

    private static final String LOGIN_PREFIX = "Login-->";
    private static final int LOGIN_LOG_SIZE = 50;

    // The Logger is a Singleton!
    private volatile static Logger LOGGER_INSTANCE;

    // Buffer log messages during login
    private final Vector<String> vecLog;


    /***********************************************************************************************
     * The Logger is a Singleton!
     *
     * @return Logger
     */

    public static Logger getInstance()
        {
        if (LOGGER_INSTANCE == null)
            {
            synchronized (Logger.class)
                {
                if (LOGGER_INSTANCE == null)
                    {
                    LOGGER_INSTANCE = new Logger();
                    }
                }
            }

        return (LOGGER_INSTANCE);
        }


    /***********************************************************************************************
     * Do a debug message.
     *
     * @param debugmode
     * @param message
     */

    private static void doDebug(final boolean debugmode,
                                final String message)
        {
        if (debugmode)
            {
            final String strNow;

            strNow = Chronos.timeNow();

            // Timestamp the log
            System.out.println(strNow
                                + SPACE
                                + message);
            }
        }


    /***********************************************************************************************
     * Privately construct the Logger.
     */

    private Logger()
        {
        // As soon as we have a Logger, we can buffer LoginPanel messages
        vecLog = new Vector<String>(LOGIN_LOG_SIZE);
        }


    /**********************************************************************************************/
    /* Simple Message Logging                                                                     */
    /***********************************************************************************************
     * Show a message on the console.
     * This is just a timestamped version of System.out.
     *
     * @param message
     */

    public void console(final String message)
        {
        // Timestamp the log
        final String strNow = Chronos.timeNow();

        // This must always use System.out
        System.out.println(strNow
                            + SPACE
                            + message);
        }


    /***********************************************************************************************
     * Log a message.
     *
     * @param message
     */

    public void log(final String message)
        {
        // ToDo decide where the messages go...


        if (LOADER_PROPERTIES.isSqlTiming())
            {
            System.out.println("! " + message);
            }
        else
            {
            System.out.println(message);
            }
        }


    /***********************************************************************************************
     * Log a message with timestamp.
     *
     * @param message
     */

    public void logTimedEvent(final String message)
        {
        // ToDo decide where the messages go...
        // Timestamp the log
        final String strNow = Chronos.timeNow();

        System.out.println(strNow
                            + SPACE
                            + message);
        }


    /***********************************************************************************************
     * Log warning messages.
     *
     * @param message
     */

    public void warn(final String message)
        {
        logTimedEvent("Warning: " + message);
        }


    /***********************************************************************************************
     * Log warning messages conditionally.
     *
     * @param enable
     * @param message
     */

    public void warn(final boolean enable,
                     final String message)
        {
        if (enable)
            {
            logTimedEvent("Warning: " + message);
            }
        }


    /***********************************************************************************************
     * Log error messages.
     *
     * @param message
     */

    public void error(final String message)
        {
        Toolkit.getDefaultToolkit().beep();
        logTimedEvent("Error: " + message);
        }


    /***********************************************************************************************
     * Log a List<String> of Error messages.
     *
     * @param source
     * @param errors
     */

    public void errors(final String source,
                       final List<String> errors)
        {
        if ((errors != null)
            && (!errors.isEmpty()))
            {
            final Iterator<String> iterErrors;

            Toolkit.getDefaultToolkit().beep();
            iterErrors = errors.iterator();

            while (iterErrors.hasNext())
                {
                logTimedEvent(source + iterErrors.next());
                }
            }
        }


    /***********************************************************************************************
     * Log a List<String> of Error messages as Warnings, i.e. silently.
     *
     * @param source
     * @param errors
     */

    public void warnings(final String source,
                         final List<String> errors)
        {
        if ((errors != null)
            && (!errors.isEmpty()))
            {
            final Iterator<String> iterErrors;

            iterErrors = errors.iterator();

            while (iterErrors.hasNext())
                {
                logTimedEvent(source + iterErrors.next());
                }
            }
        }


    /***********************************************************************************************
     * Log a Vector of messages.
     *
     * @param messages
     */

    public void logMessages(final Vector<String> messages)
        {
        if ((messages != null)
            && (!messages.isEmpty()))
            {
            final Iterator<String> iterMessages;

            iterMessages = messages.iterator();

            while (iterMessages.hasNext())
                {
                log(iterMessages.next());
                }
            }
        }


    /**********************************************************************************************/
    /* Login logging                                                                              */
    /***********************************************************************************************
     * Show messages captured during login, or buffer them until the LoginDialog is visible.
     *
     * @param message
     */

    public void login(final String message)
        {
        if ((MODEL_CONTROLLER.getLoginDialog() != null)
            && ((MODEL_CONTROLLER.getLoginDialog().isVisible())))
            {
            // Not disposed, so we still have a LoginDialog
            // Flush the LoginDialog log buffer as soon as we can
            flushLoginLog();

            // Now show the message we were originally given
            MODEL_CONTROLLER.getLoginDialog().setNarrative(message);
            }
        else
            {
            // Buffer all LoginPanel messages until we have a LoginPanel again
            vecLog.add(message);
            console(LOGIN_PREFIX + message);
            }
        }


    /***********************************************************************************************
     * Flush the LoginPanel log buffer.
     */

    public void flushLoginLog()
        {
        if ((!vecLog.isEmpty())
            && (MODEL_CONTROLLER.getLoginDialog() != null))
            {
            final Iterator<String> iterLog;

            iterLog = vecLog.iterator();

            while (iterLog.hasNext())
                {
                final String msg = iterLog.next();
                MODEL_CONTROLLER.getLoginDialog().setNarrative(msg);
                }

            vecLog.clear();
            }
        }


    /**********************************************************************************************/
    /* Atom Event Logging                                                                              */
    /***********************************************************************************************
     * Log an Event for an Atom, with an optional Task.
     * This is the preferred method to call for Event logging, since it gives full context.
     *
     * @param atom
     * @param task
     * @param classname
     * @param event
     * @param status
     */

    public void logAtomEvent(final AtomPlugin atom,
                             final RootPlugin task,
                             final String classname,
                             final String event,
                             final EventStatus status)
        {
        final long longFrameworkID;
        final long longAtomID;
        final long longTaskID;

        // Discover what kind of Atom we are dealing with
        if (atom != null)
            {
            // There is an Atom, but is it the Framework?
            if (REGISTRY.getFramework().getLevel().equals(atom.getLevel()))
                {
                longFrameworkID = atom.getID();
                longAtomID = atom.getID();
                }
            else
                {
                // It is a child Atom
                longFrameworkID = REGISTRY.getFramework().getID();
                longAtomID = atom.getID();
                }
            }
        else
            {
            // No Atom was specified...
            // Record this event as coming from the Framework
            longFrameworkID = REGISTRY.getFramework().getID();
            longAtomID = longFrameworkID;
            }

        // See if we have a usable TaskID
        if (task != null)
            {
            longTaskID = task.getID();
            }
        else
            {
            // Use the Atom's RootTask if there is no Task supplied
            if ((atom != null)
               && (atom.getRootTask() != null))
                {
                longTaskID = atom.getRootTask().getID();
                }
            else
                {
                // We tried, but gave up...
                longTaskID = REGISTRY.getFramework().getRootTask().getID();
                }
            }

        // Log the event as best we can
        logEvent(longFrameworkID,
                 longAtomID,
                 longTaskID,
                 classname,
                 event,
                 status);
        }


    /**********************************************************************************************/
    /* Exception Handling                                                                         */
    /***********************************************************************************************
     * Handle an Exception within an Atom, with an optional Task.
     *
     * @param atom
     * @param task
     * @param classname
     * @param exception
     * @param identifier
     * @param status
     */

    public void handleAtomException(final AtomPlugin atom,
                                    final RootPlugin task,
                                    final String classname,
                                    final Exception exception,
                                    final String identifier,
                                    final EventStatus status)
        {
        final long longFrameworkID;
        final long longAtomID;
        final long longTaskID;

        // Discover what kind of Atom we are dealing with
        if (atom != null)
            {
            // There is an Atom, but is it the Framework?
            if (REGISTRY.getFramework().getLevel().equals(atom.getLevel()))
                {
                longFrameworkID = atom.getID();
                longAtomID = atom.getID();
                }
            else
                {
                // It is a child Atom
                longFrameworkID = REGISTRY.getFramework().getID();
                longAtomID = atom.getID();
                }
            }
        else
            {
            // No Atom was specified...
            // Record this event as coming from the Framework
            longFrameworkID = REGISTRY.getFramework().getID();
            longAtomID = longFrameworkID;
            }

        // See if we have a usable TaskID
        if (task != null)
            {
            longTaskID = task.getID();
            }
        else
            {
            // Use the Atom's RootTask if there is no Task supplied
            if ((atom != null)
               && (atom.getRootTask() != null))
                {
                longTaskID = atom.getRootTask().getID();
                }
            else
                {
                // We tried, but gave up...
                longTaskID = REGISTRY.getFramework().getRootTask().getID();
                }
            }

        // Log the exception as best we can
        logException(longFrameworkID,
                     longAtomID,
                     longTaskID,
                     classname,
                     exception,
                     identifier,
                     status);
        }


    /***********************************************************************************************
     * Log the Exception, given the context IDs.
     *
     * @param frameworkid
     * @param atomid
     * @param taskid
     * @param classname
     * @param exception
     * @param identifier
     * @param status
     */

    private void logException(final long frameworkid,
                              final long atomid,
                              final long taskid,
                              final String classname,
                              final Exception exception,
                              final String identifier,
                              final EventStatus status)
        {
        final String strExceptionMsgBox;

        if (exception != null)
            {
            // Format the exception for a message box
            strExceptionMsgBox = "<html>"
                                + exception.getMessage()
                                + "<br>" + identifier
                                + "</html>";

            // First log the Exception just as it comes
            if (!EventStatus.FATAL.equals(status))
                {
                logEvent(frameworkid,
                         atomid,
                         taskid,
                         classname,
                         METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR
                            + SPACE
                            + METADATA_IDENTIFIER
                            + identifier
                            + TERMINATOR,
                         status);
                }

            // Check to see the status of the message
            // to check for Fatal Exceptions, which must shutdown the Framework
            // ToDo rewrite as switch()
            if (EventStatus.FATAL.equals(status))
                {
                // Tell the user what happened
                JOptionPane.showMessageDialog(null,
                                              strExceptionMsgBox,
                                              classname,
                                              JOptionPane.ERROR_MESSAGE);

                // Now log the fatal error exit separately
                logEvent(frameworkid,
                         atomid,
                         taskid,
                         classname,
                         METADATA_PLUGIN_SHUTDOWN
                             + SPACE
                             + METADATA_EXCEPTION
                             + identifier
                             + TERMINATOR,
                         EventStatus.FATAL);

                // It is important that we don't fail in closing the database,
                // otherwise we generate another Exception, and hence loop round

                if (DATABASE.getActive())
                    {
                    DATABASE.closeConnection();
                    }

                // Todo do we need to shutdown Tasks here??

                System.exit(0);
                }
            else if (EventStatus.INFO.equals(status))
                {
                // Just pop up a message box for informative messages
                JOptionPane.showMessageDialog(null,
                                              strExceptionMsgBox,
                                              classname,
                                              status.getStatusID());
                }
            else if (EventStatus.WARNING.equals(status))
                {
                JOptionPane.showMessageDialog(null,
                                              strExceptionMsgBox,
                                              classname,
                                              status.getStatusID());
                }
            else if (EventStatus.QUESTION.equals(status))
                {
                JOptionPane.showMessageDialog(null,
                                              strExceptionMsgBox,
                                              classname,
                                              status.getStatusID());
                }
            else if (EventStatus.PLAIN.equals(status))
                {
                JOptionPane.showMessageDialog(null,
                                              strExceptionMsgBox,
                                              classname,
                                              status.getStatusID());
                }
            else if (EventStatus.SILENT.equals(status))
                {
                // Do nothing at all...
                }
            else
                {
                JOptionPane.showMessageDialog(null,
                                              strExceptionMsgBox,
                                              classname,
                                              status.getStatusID());
                }
            }
        }


    /***********************************************************************************************
     * Log a message in the EventLog in the current DataStore,
     * using the specified IDs for the Framework, Atom and Task.
     * Unknown IDs are <code>-1</code>.
     * This *assumes* that the database connection is open.
     *
     * @param frameworkid
     * @param atomid
     * @param taskid
     * @param classname
     * @param event
     * @param status
     */

    private void logEvent(final long frameworkid,
                          final long atomid,
                          final long taskid,
                          final String classname,
                          final String event,
                          final EventStatus status)
        {
        final EventLogDAOInterface dao;

        // Echo the database entry on the console
        console(event);

        // Don't trap an inactive database, just see the Console
        if ((DATABASE != null)
            && (DATABASE.getActive())
            && (DATABASE.getDatabaseOptions() != null))
            {
            // ToDo rewrite as switch()
            if (DataStore.XML.equals(DATABASE.getDatabaseOptions().getDataStore()))
                {
                //Todo review debug flag
                dao = new EventLogXmlDAO(false);
                dao.logEvent(frameworkid,
                             atomid,
                             taskid,
                             classname,
                             event,
                             status);
                }
            else if (DataStore.MYSQL.equals(DATABASE.getDatabaseOptions().getDataStore()))
                {
                dao = new EventLogMySqlDAO();
                dao.logEvent(frameworkid,
                             atomid,
                             taskid,
                             classname,
                             event,
                             status);
                }
            else if (DataStore.HSQLDB.equals(DATABASE.getDatabaseOptions().getDataStore()))
                {
                dao = new EventLogHsqldbDAO();
                dao.logEvent(frameworkid,
                             atomid,
                             taskid,
                             classname,
                             event,
                             status);
                }
            else
                {
                error(EXCEPTION_DATASTORE_INVALID);
                }
            }
        else
            {
            console("The database is not active, so no events may be logged");
            }
        }


    /**********************************************************************************************/
    /* Debug controlled by LOADER_PROPERTIES                                                             */
    /***********************************************************************************************
     * Log a Staribus debug message with timestamp.
     *
     * @param debugmode
     * @param message
     */

    public void debugStaribusEvent(final boolean debugmode,
                                   final String message)
        {
        doDebug(debugmode, message);
        }


    /***********************************************************************************************
     * Log a Starinet debug message with timestamp.
     *
     * @param debugmode
     * @param message
     */

    public void debugStarinetEvent(final boolean debugmode,
                                   final String message)
        {
        doDebug(debugmode, message);
        }


    /***********************************************************************************************
     * Log a Command debug message with timestamp.
     *
     * @param debugmode
     * @param message
     */

    public void debugCommandEvent(final boolean debugmode,
                                  final String message)
        {
        doDebug(debugmode, message);
        }


    /***********************************************************************************************
     * Log a Timed debug message with timestamp.
     *
     * @param debugmode
     * @param message
     */

    public void debugTimedEvent(final boolean debugmode,
                                final String message)
        {
        doDebug(debugmode, message);
        }


    /***********************************************************************************************
     * Log a State debug message with timestamp.
     *
     * @param debugmode
     * @param message
     */

    public void debugStateEvent(final boolean debugmode,
                                final String message)
        {
        doDebug(debugmode, message);
        }


    /***********************************************************************************************
     * Log a GPS debug message with timestamp.
     *
     * @param debugmode
     * @param message
     */

    public void debugGpsEvent(final boolean debugmode,
                              final String message)
        {
        doDebug(debugmode, "GPS: " + message);
        }


    /**********************************************************************************************/
    /* Debugging logs                                                                             */
    /* Turn these off by commenting out!                                                          */
    /***********************************************************************************************
     * A general debug message.
     *
     * @param message
     */

    public void debug(final String message)
        {
        if (DEBUG)
//        if (LOADER_PROPERTIES.isMasterDebug())
            {
            log(message);
            }
        }


    /***********************************************************************************************
     * Debug control panels etc.
     *
     * @param message
     */

    public void debugIndicators(final String message)
        {
        //System.out.println(message);
        }


    /***********************************************************************************************
     * A general debug message, controlled by a debug mode flag.
     *
     * @param debug
     * @param message
     */

    public void debug(final boolean debug, final String message)
        {
        if (debug)
            {
            final String strNow;

            strNow = Chronos.timeNow();

            // Timestamp the log
            System.out.println(strNow
                               + SPACE
                               + message);
            }
        }


    /***********************************************************************************************
     * Log a debug Exception with timestamp.
     *
     * @param throwable
     */

    public void debugException(final Throwable throwable)
        {
        System.out.println(Chronos.timeNow() + " Exception");
        throwable.printStackTrace();
        }


    /***********************************************************************************************
     * Show a debug message from within a Runnable, if debug is enabled.
     *
     * @param message
     */

    public void debugRunnable(final String message)
        {
        if (DEBUG)
//        if (LOADER_PROPERTIES.isMasterDebug())
            {
            //log("Runnable: " + message);
            }
        }


    /***********************************************************************************************
     * Show a debug message from within a Timer action, if debug is enabled.
     *
     * @param message
     */

    public void debugTimerTick(final String message)
        {
        if (DEBUG)
//        if (LOADER_PROPERTIES.isMasterDebug())
            {
            //log("Timer: TICK! " + message);
            }
        }


    /***********************************************************************************************
     * Show a debug message from within a SwingWorker Thread, if debug is enabled.
     *
     * @param message
     */

    public void debugSwingWorker(final String message)
        {
        if (DEBUG)
//        if (LOADER_PROPERTIES.isMasterDebug())
            {
            //log("SwingWorker: " + message);
            }
        }


    /***********************************************************************************************
     * Log messages which debug the Timer events (such as start and stop).
     *
     * @param message
     */

    public void debugTimerEvent(final String message)
        {
        if (DEBUG)
//        if (LOADER_PROPERTIES.isMasterDebug())
            {
            //log("Timer: " + message);
            }
        }


    /***********************************************************************************************
     * Log messages which debug the navigation events (such as toolbar and menu rebuilds).
     *
     * @param message
     */

    public void debugNavigation(final String message)
        {
        if (DEBUG)
//        if (LOADER_PROPERTIES.isMasterDebug())
            {
            //log("Navigation:" + message);
            }
        }
    }
