package org.lmn.fc.model.logging.impl;

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.EventLogDAOInterface;
import org.lmn.fc.model.logging.EventLogInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.xmlbeans.events.EventsDocument;

import java.io.File;
import java.io.IOException;
import java.util.Vector;


/***************************************************************************************************
 * The EventLogXmlDAO.
 */

public final class EventLogXmlDAO implements EventLogDAOInterface
    {
    private static final DataStore DATA_STORE = DataStore.XML;

    private static final EventLogInterface EVENT_LOG = EventLog.getInstance();

    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct the EventLogXmlDAO.
     *
     * @param debug
     */

    public EventLogXmlDAO(final boolean debug)
        {
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Load the previous Event Log from the DataStore.
     */

    public void loadEventLog()
        {
        final File fileEventLog;
        final EventsDocument docEvents;

        try
            {
            fileEventLog = new File(DATA_STORE.getLoadFolder() + EVENTLOG_XML);

            // Read the existing file or create a new EventLog document
            if (fileEventLog.exists())
                {
                docEvents = EventsDocument.Factory.parse(fileEventLog);
                }
            else
                {
                docEvents = EventsDocument.Factory.newInstance();
                docEvents.addNewEvents();
                }
            }

        catch (IOException exception)
            {
            throw new FrameworkException(exception.getMessage(), exception);
            }

        catch (XmlException exception)
            {
            throw new FrameworkException(exception.getMessage(), exception);
            }

        EVENT_LOG.setEventLog(docEvents);
        }


    /***********************************************************************************************
     * Save the entire current EventLog to the DataStore.
     */

    public void saveEventLog()
        {
        final File fileEventLog;

        try
            {
            fileEventLog = new File(DATA_STORE.getSaveFolder() + EVENTLOG_XML);

            // Overwrite existing output file or create a new file
            if (fileEventLog.exists())
                {
                fileEventLog.delete();
                fileEventLog.createNewFile();
                }
            else
                {
                fileEventLog.createNewFile();
                }

            EVENT_LOG.getEventLog().save(fileEventLog);
            EVENT_LOG.setUpdated(false);
            }

        catch (IOException exception)
            {
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


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

    public void logEvent(final long frameworkid,
                         final long atomid,
                         final long taskid,
                         final String classname,
                         final String event,
                         final EventStatus status)
        {
        EVENT_LOG.logEvent(frameworkid, atomid, taskid, classname, event, status);
        saveEventLog();
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log.
     */

    public void deleteAllEvents()
        {
        EVENT_LOG.deleteAllEvents();
        saveEventLog();
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log related to the specified Atom.
     *
     * @param atom
     */

    public void deleteAtomEvents(final AtomPlugin atom)
        {
        EVENT_LOG.deleteAtomEvents(atom);
        saveEventLog();
        }


    /***********************************************************************************************
     * Truncate the EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param length
     */

    public void truncateEventLog(final int length)
        {
        EVENT_LOG.truncateEventLog(length);
        saveEventLog();
        }


    /***********************************************************************************************
     * Truncate the EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param atom
     * @param length
     */

    public void truncateAtomEventLog(final AtomPlugin atom, final int length)
        {
        EVENT_LOG.truncateAtomEventLog(atom, length);
        saveEventLog();
        }


    /***********************************************************************************************
     * Select all events in the Event Log.
     * Return a Vector suitable for an EventReport.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAllEventsReport()
        {
        return (EVENT_LOG.getAllEventsReport());
        }


    /***********************************************************************************************
     * Select all events relevant to the specified Atom.
     * Return a Vector suitable for a Report.
     *
     * @param atom
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAtomEventsReport(final AtomPlugin atom)
        {
        return (EVENT_LOG.getAtomEventsReport(atom));
        }


    /***********************************************************************************************
     * Get the Debug Mode.
     *
     * @return boolean
     */

    private boolean getDebugMode()
        {
        return (this.boolDebugMode);
        }
    }
