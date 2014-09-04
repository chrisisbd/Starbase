package org.lmn.fc.model.logging.impl;

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.EventLogDAOInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;

import java.util.Vector;

/***************************************************************************************************
 * The EventLogHsqldbDAO.
 */

public final class EventLogHsqldbDAO implements EventLogDAOInterface,
                                                FrameworkSingletons
    {
    private static final DataStore DATA_STORE = DataStore.HSQLDB;


    /***********************************************************************************************
     * Construct the EventLogHsqldbDAO.
     */

    public EventLogHsqldbDAO()
        {
        }


    /***********************************************************************************************
     * Log the Event originating from the specified context.
     *
     * @param frameworkid
     * @param applicationid
     * @param taskid
     * @param classname
     * @param event
     * @param status
     */

    public void logEvent(final long frameworkid,
                         final long applicationid,
                         final long taskid,
                         final String classname,
                         final String event,
                         final EventStatus status) throws FrameworkException
        {
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log.
     */

    public void deleteAllEvents() throws FrameworkException
        {
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log related to the specified Atom.
     */

    public void deleteAtomEvents(final AtomPlugin atom) throws FrameworkException
        {
        }


    /***********************************************************************************************
     * Select all events in the Event Log.
     * Return a Vector suitable for a Report.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAllEventsReport() throws FrameworkException
        {
        final Vector<Vector> vecEvents;
        final Vector<Object> vecRow;

        vecEvents = new Vector<Vector>(1);
        vecRow = new Vector<Object>(6);
        vecRow.add(RegistryModelUtilities.getCommonIcon(ICON_DUMMY));
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecEvents.add(vecRow);

        return (vecEvents);
        }


    /***********************************************************************************************
     * Select all events relevant to the specified Atom.
     * Return a Vector suitable for a Report.
     *
     * @param atom
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAtomEventsReport(final AtomPlugin atom) throws FrameworkException
        {
        final Vector<Vector> vecEvents;
        final Vector<Object> vecRow;

        vecEvents = new Vector<Vector>(1);
        vecRow = new Vector<Object>(6);
        vecRow.add(RegistryModelUtilities.getCommonIcon(ICON_DUMMY));
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecRow.add(EMPTY_STRING);
        vecEvents.add(vecRow);

        return (vecEvents);
        }


    /***********************************************************************************************
     * Truncate the EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param length
     */

    public void truncateEventLog(int length)
        {
        //todo truncate
        }


    /***********************************************************************************************
     * Truncate the EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param atom
     * @param length
     */

    public void truncateAtomEventLog(final AtomPlugin atom, final int length)
        {
        //todo truncate
        }


    /**
     * ********************************************************************************************
     * Load the previous EventLog from the DataStore.
     */

    public void loadEventLog()
        {
        //To change body of implemented methods use File | Settings | File Templates.
        }

    /***********************************************************************************************
     * Write the current EventLog to the DataStore.
     */

    public void saveEventLog()
        {
        System.out.println("saveEventLog to store!");
        }
    }
