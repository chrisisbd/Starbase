package org.lmn.fc.model.logging;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.plugins.AtomPlugin;

import java.util.Vector;


/***************************************************************************************************
 * The EventLogDAOInterface.
 */

public interface EventLogDAOInterface extends FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              ResourceKeys
    {
    String DIALOG_TITLE = "Framework Logger";

    // Query Keys
    String INSERT_EVENTLOG                  = "Insert.EventLog";

    String DELETE_EVENTLOG_ALL              = "Delete.EventLog.All";
    String DELETE_EVENTLOG_ATOM             = "Delete.EventLog.Atom";

    String SELECT_EVENTLOG_ALL              = "Select.EventLog.All";
    String SELECT_EVENTLOG_ATOM             = "Select.EventLog.Atom";

    String EVENTLOG_XML = "/eventlog.xml";


    /***********************************************************************************************
     * Load the previous EventLog from the DataStore.
     */

    void loadEventLog();


    /***********************************************************************************************
     * Save the current EventLog to the DataStore.
     */

    void saveEventLog();


    /***********************************************************************************************
     * Log the Event originating from the specified context.
     *
     * @param frameworkid
     * @param atomid
     * @param taskid
     * @param classname
     * @param event
     * @param status
     */

    void logEvent(long frameworkid,
                  long atomid,
                  long taskid,
                  String classname,
                  String event,
                  EventStatus status) throws FrameworkException;


    /***********************************************************************************************
     * Delete all Events in the Event Log.
     */

    void deleteAllEvents() throws FrameworkException;


    /***********************************************************************************************
     * Delete all Events in the Event Log related to the specified Atom.
     *
     * @param atom
     */

    void deleteAtomEvents(AtomPlugin atom) throws FrameworkException;


    /***********************************************************************************************
     * Truncate the EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param length
     */

    void truncateEventLog(int length);


    /***********************************************************************************************
     * Truncate the Atom EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param length
     */

    void truncateAtomEventLog(AtomPlugin atom, int length);


    /***********************************************************************************************
     * Select all events in the Event Log.
     * Return a Vector suitable for a Report.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getAllEventsReport() throws FrameworkException;


    /***********************************************************************************************
     * Select all events originating from the specified Atom.
     * Return a Vector suitable for a Report.
     *
     * @param atom
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getAtomEventsReport(AtomPlugin atom) throws FrameworkException;
    }
