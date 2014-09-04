package org.lmn.fc.model.logging.impl;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.dao.DAOUtilities;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.EventLogDAOInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.resources.QueryPlugin;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;


/***************************************************************************************************
 * The MySQL LoggingDAO.
 */

public final class EventLogMySqlDAO implements EventLogDAOInterface,
                                               FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ResourceKeys
    {
    private static final DataStore DATA_STORE = DataStore.MYSQL;
    private static final int FIELDSIZE_LOG_ENTRY = 255;

    // Column names in the EventLog table
    private static final String COLUMN_FRAMEWORKID       = "FrameworkID";
    private static final String COLUMN_APPLICATIONID     = "ApplicationID";
    private static final String COLUMN_COMPONENTID       = "ComponentID";
    private static final String COLUMN_TASKID            = "TaskID";
    private static final String COLUMN_ORIGIN_CLASS_NAME = "OriginClassName";
    private static final String COLUMN_EVENT_DATE        = "EventDate";
    private static final String COLUMN_EVENT_TIME        = "EventTime";
    private static final String COLUMN_EVENT_MESSAGE     = "EventMessage";
    private static final String COLUMN_EVENT_STATUS      = "EventStatus";


    /***********************************************************************************************
     * Construct the EventLogMySqlDAO.
     */

    public EventLogMySqlDAO()
        {
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

    public final void logEvent(final long frameworkid,
                               final long atomid,
                               final long taskid,
                               final String classname,
                               final String event,
                               final EventStatus status) throws FrameworkException
        {
        try
            {
            final QueryPlugin queryPlugin;
            final PreparedStatement psLog;
            int intParameter;
            String strClassName;
            String strEvent;

            strClassName = classname;
            strEvent = event;

            if ((classname != null)
                && (classname.length() > FIELDSIZE_LOG_ENTRY))
                {
                strClassName = classname.substring(0, FIELDSIZE_LOG_ENTRY - 1);
                }

            if ((event != null)
                && (event.length() > FIELDSIZE_LOG_ENTRY))
                {
                strEvent = event.substring(0, FIELDSIZE_LOG_ENTRY - 1);
                }
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + INSERT_EVENTLOG);
            psLog = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

            intParameter = 1;
            // Set the context
            psLog.setLong(intParameter++, frameworkid);
            psLog.setLong(intParameter++, atomid);
            psLog.setLong(intParameter++, taskid);
            psLog.setString(intParameter++, strClassName);

            // Set the Event
            psLog.setDate(intParameter++, Chronos.getSystemDateNow());
            psLog.setTime(intParameter++, Chronos.getSystemTimeNow());
            psLog.setString(intParameter++, strEvent);
            psLog.setInt(intParameter, status.getStatusID());

            // TODo exec full u/d method with trace
            psLog.executeUpdate();
            psLog.close();
            }

        catch (SQLException exception)
            {
            final String [] strMessage =
                {
                DATABASE_LOGMESSAGE,
                classname,
                event,
                exception.getMessage()
                };

            JOptionPane.showMessageDialog(null,
                                          strMessage,
                                          DIALOG_TITLE,
                                          JOptionPane.ERROR_MESSAGE);
            }
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log.
     */

    public void deleteAllEvents() throws FrameworkException
        {
        final QueryPlugin queryPlugin;
        final PreparedStatement psDelete;

        try
            {
            System.out.println("in deleteAllEvents !!!!!!!!!!!!!");
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + DELETE_EVENTLOG_ALL);

            if (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE))
                {
                psDelete = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);
                System.out.println("exec delete all");
                queryPlugin.executeUpdate(this,
                                        psDelete,
                                        RegistryModel.getInstance().getSqlTrace(),
                                        RegistryModel.getInstance().getSqlTiming());
                psDelete.close();
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_DELETE_DATA, exception);
            }
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log related to the specified Atom.
     *
     * @param atom
     */

    public void deleteAtomEvents(final AtomPlugin atom) throws FrameworkException
        {
        final AtomPlugin pluginAtom;
        final QueryPlugin queryPlugin;
        final PreparedStatement psDelete;

        if ((atom == null)
            || (!(atom instanceof AtomPlugin)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            pluginAtom = (AtomPlugin)atom;
            }

        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + DELETE_EVENTLOG_ATOM);
            psDelete = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

            // There is an Atom, but is it the Framework?
            if (REGISTRY.getFramework().getLevel().equals(pluginAtom.getLevel()))
                {
                psDelete.setLong(1, pluginAtom.getID());
                psDelete.setLong(2, pluginAtom.getID());
                psDelete.setLong(3, REGISTRY.getLevelID(pluginAtom));
                }
            else
                {
                // It is a child Atom
                psDelete.setLong(1, REGISTRY.getFramework().getID());
                psDelete.setLong(2, pluginAtom.getID());
                psDelete.setLong(3, REGISTRY.getLevelID(pluginAtom));
                }

            if (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE))
                {
                System.out.println("exec atom delete");
                queryPlugin.executeUpdate(this,
                                          psDelete,
                                          RegistryModel.getInstance().getSqlTrace(),
                                          RegistryModel.getInstance().getSqlTiming());
                psDelete.close();
                }
            else
                {
                throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
                }
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(EXCEPTION_DELETE_DATA, exception);
            }
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
        Vector<Object> vecRow;
        final PreparedStatement psEvents;
        final ResultSet rsEvents;
        final QueryPlugin queryPlugin;

        vecEvents = new Vector<Vector>(10);

        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + SELECT_EVENTLOG_ALL);
            psEvents = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

            if (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE))
                {
                rsEvents = queryPlugin.executeQuery(this,
                                                  psEvents,
                                                  REGISTRY_MODEL.getSqlTrace(),
                                                  REGISTRY_MODEL.getSqlTiming());

                // Copy the database records into a Vector
                while(rsEvents.next())
                    {
                    final int intStatus;
                    final EventStatus statusEvent;
                    final ImageIcon imageIcon;
                    vecRow = new Vector<Object>(6);

                    intStatus = rsEvents.getInt(COLUMN_EVENT_STATUS);
                    statusEvent = EventStatus.getEventStatusForID(intStatus);
                    imageIcon = RegistryModelUtilities.getCommonIcon(statusEvent.getIconFilename());

                    vecRow.add(imageIcon);
                    vecRow.add(rsEvents.getString(COLUMN_EVENT_DATE));
                    vecRow.add(rsEvents.getString(COLUMN_EVENT_TIME));
                    vecRow.add(rsEvents.getString(COLUMN_EVENT_MESSAGE));
                    vecRow.add(rsEvents.getString(COLUMN_ORIGIN_CLASS_NAME));
                    vecRow.add(statusEvent.getTooltip());

                    vecEvents.add(vecRow);
                    }

                rsEvents.close();
                psEvents.close();
                }
            }

        catch (ClassCastException exception)
            {
            throw new FrameworkException(getClass().getName() + ".getAllEventsReport() ", exception);
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(getClass().getName() + ".getAllEventsReport() ", exception);
            }

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
        final AtomPlugin pluginAtom;
        final Vector<Vector> vecEvents;
        Vector<Object> vecRow;
        final PreparedStatement psEvents;
        final ResultSet rsEvents;
        final QueryPlugin queryPlugin;

        vecEvents = new Vector<Vector>(10);

        if ((atom == null)
            || (!(atom instanceof AtomPlugin)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            pluginAtom = (AtomPlugin)atom;
            }

        try
            {
            queryPlugin = REGISTRY.getQueryData(REGISTRY.getFramework().getResourceKey() + SELECT_EVENTLOG_ATOM);
            psEvents = queryPlugin.getPreparedStatement(DATABASE, DATA_STORE);

            // There is an Atom, but is it the Framework?
            if (REGISTRY.getFramework().getLevel().equals(pluginAtom.getLevel()))
                {
                psEvents.setLong(1, pluginAtom.getID());
                psEvents.setLong(2, pluginAtom.getID());
                psEvents.setLong(3, REGISTRY.getLevelID(pluginAtom));
                }
            else
                {
                // It is a child Atom
                psEvents.setLong(1, REGISTRY.getFramework().getID());
                psEvents.setLong(2, pluginAtom.getID());
                psEvents.setLong(3, REGISTRY.getLevelID(pluginAtom));
                }

            if (DAOUtilities.isValidQuery(queryPlugin, DATA_STORE))
                {
                rsEvents = queryPlugin.executeQuery(this,
                                                    psEvents,
                                                    REGISTRY_MODEL.getSqlTrace(),
                                                    REGISTRY_MODEL.getSqlTiming());

                // Copy the database records into a Vector
                while(rsEvents.next())
                    {
                    final int intStatus;
                    final EventStatus statusEvent;
                    final ImageIcon imageIcon;
                    vecRow = new Vector<Object>(6);

                    intStatus = rsEvents.getInt(COLUMN_EVENT_STATUS);
                    statusEvent = EventStatus.getEventStatusForID(intStatus);
                    imageIcon = RegistryModelUtilities.getCommonIcon(statusEvent.getIconFilename());

                    vecRow.add(imageIcon);
                    vecRow.add(rsEvents.getString(COLUMN_EVENT_DATE));
                    vecRow.add(rsEvents.getString(COLUMN_EVENT_TIME));
                    vecRow.add(rsEvents.getString(COLUMN_EVENT_MESSAGE));
                    vecRow.add(rsEvents.getString(COLUMN_ORIGIN_CLASS_NAME));
                    vecRow.add(statusEvent.getTooltip());

                    vecEvents.add(vecRow);
                    }

                rsEvents.close();
                psEvents.close();
                }
            }

        catch (ClassCastException exception)
            {
            throw new FrameworkException(getClass().getName() + ".getAtomEventsReport() ", exception);
            }

        catch (SQLException exception)
            {
            throw new FrameworkException(getClass().getName() + ".getAtomEventsReport() ", exception);
            }

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
