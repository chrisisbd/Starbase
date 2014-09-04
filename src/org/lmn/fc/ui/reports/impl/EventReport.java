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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  04-05-04    LMN created file
//  06-05-04    LMN getting Resources to work
//  16-03-06    LMN converted to use a DAO!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.logging.EventLogDAOInterface;
import org.lmn.fc.model.logging.impl.EventLogHsqldbDAO;
import org.lmn.fc.model.logging.impl.EventLogMySqlDAO;
import org.lmn.fc.model.logging.impl.EventLogXmlDAO;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose EventReport for a Task.
 */

public class EventReport extends ReportTable
                         implements ReportTablePlugin
    {
    private static final String MSG_REPORT_CREATED = "Event Log created at";

    private static final int REPORT_COLUMN_COUNT = 6;
    private static final int MIN_ENTRIES = 5;
    private static final int MAX_ENTRIES = 500;

    // The number of entries displayed in the log
    private int intLogEntries;

    // The text for the Truncate Action
    private String strMenuTruncate;

    // The DataStore containing the EventLog
    private final DataStore dataStore;


    /***********************************************************************************************
     * Construct an EventReport for the specified Task, obtaining data from the specified DataStore.
     *
     * @param task
     * @param store
     * @param resourcekey
     *
     * @throws ReportException
     */

    public EventReport(final RootPlugin task,
                       final DataStore store,
                       final String resourcekey) throws ReportException
        {
        super(task,
              task.getName(),
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              REFRESHABLE,
              REFRESH_CLICK,
              NON_REORDERABLE,
              TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        this.dataStore = store;
        }


    /***********************************************************************************************
     * Run the UI of this ReportTable.
     */

    public void runUI()
        {
        final ContextActionGroup group;

        // This Log is used in a Runnable Task,
        // so start a new ContextActionGroup for this UIComponent,
        group = new ContextActionGroup(getReportUniqueName(), true, true);

        // Add the new Group to the list of Groups for this UIComponent
        clearUIComponentContextActionGroups();
        addUIComponentContextActionGroup(group);

        if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
            {
            final URL imageURL;

            LOGGER.debugNavigation("EventReport.runUI()");

            imageURL = getClass().getResource(ACTION_ICON_TRUNCATE);

            if (imageURL != null)
                {
                final ContextAction truncateContextAction;

                truncateContextAction = new ContextAction(strMenuTruncate,
                                                          new ImageIcon(imageURL),
                                                          strMenuTruncate,
                                                          KeyEvent.VK_T,
                                                          true,
                                                          true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        try
                            {
                            readResources();
                            truncateReport();
                            runUI();
                            }

                        catch (ReportException exception)
                            {
                            LOGGER.error(EXCEPTION_PARAMETER_INVALID);
                            }
                        }
                    };

                // Add the new ContextAction to the UIComponent ContextActionGroup
                group.addContextAction(truncateContextAction);
                }
            }

        // Now run the Report, and add any further ContextActions (e.g. Export & Print)
        super.runUI();
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector<String>
     */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector<ReportColumnData>
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(REPORT_COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata( SPACE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata( "Date",
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( "Time",
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( "Event",
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( "Source",
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( "Status",
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));

        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final Object [] columnWidths =
            {
            // Use an icon which we know must exist
            RegistryModelUtilities.getCommonIcon(ICON_DUMMY),
            "2004-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     * @throws ReportException
     */

    public final Vector<Vector> generateReport() throws ReportException
        {
        final EventLogDAOInterface dao;
        final Vector<Vector> vecReport;

        if ((DataStore.XML.equals(getDataStore()))
            && (DataStore.XML.isAvailable()))
            {
            dao = new EventLogXmlDAO(isDebug());
            }
        else if ((DataStore.MYSQL.equals(getDataStore()))
            && (DataStore.MYSQL.isAvailable()))
            {
            dao = new EventLogMySqlDAO();
            }
        else if ((DataStore.HSQLDB.equals(getDataStore()))
            && (DataStore.HSQLDB.isAvailable()))
            {
            dao = new EventLogHsqldbDAO();
            }
        else
            {
            throw new ReportException(EXCEPTION_DATASTORE_INVALID);
            }

        try
            {
            vecReport = dao.getAtomEventsReport(getTask().getParentAtom());
            }

        catch (FrameworkException exception)
            {
            throw new ReportException(exception.getMessage(), exception);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     *
     * @throws ReportException
     */

    public final Vector<Vector> refreshReport() throws ReportException
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Get the number of displayed log entries.
     *
     * @return int the number of Log entries
     */

    public final int getLogEntries()
        {
        return (this.intLogEntries);
        }


    /***********************************************************************************************
     * Set the number of displayed log entries.
     *
     * @param logentries
     */

    public final void setLogEntries(final int logentries)
        {
        intLogEntries = logentries;

        if (logentries < MIN_ENTRIES) { intLogEntries = MIN_ENTRIES; }
        if (logentries > MAX_ENTRIES) { intLogEntries = MAX_ENTRIES; }
        }


    /***********************************************************************************************
     * Truncate the EventReport table to the value defined in Report.LogEntries.
     */

    public final void truncateReport() throws ReportException
        {
        final EventLogDAOInterface dao;

        if ((DataStore.XML.equals(getDataStore()))
            && (DataStore.XML.isAvailable()))
            {
            dao = new EventLogXmlDAO(isDebug());
            }
        else if ((DataStore.MYSQL.equals(getDataStore()))
            && (DataStore.MYSQL.isAvailable()))
            {
            dao = new EventLogMySqlDAO();
            }
        else if ((DataStore.HSQLDB.equals(getDataStore()))
            && (DataStore.HSQLDB.isAvailable()))
            {
            dao = new EventLogHsqldbDAO();
            }
        else
            {
            throw new ReportException(EXCEPTION_DATASTORE_INVALID);
            }

        // Truncate the EventLog!
        if ((REGISTRY.getFramework() != null)
            && (getTask() != null)
            && (REGISTRY.getFramework().equals(getTask().getParentAtom())))
            {
            // The top-level Framework EventLog shows *all* Events
            dao.truncateEventLog(intLogEntries);
            }
        else
            {
            // Plugin EventLogs are truncated by removing Events from the Plugin *only*
            dao.truncateAtomEventLog(getTask().getParentAtom(), intLogEntries);
            }
        }


    /***********************************************************************************************
     * Get the DataStore currently used for this Framework.
     *
     * @return DataStore
     */

    private DataStore getDataStore()
        {
        return (this.dataStore);
        }


    /***********************************************************************************************
     * Read all the Resources required by the EventReport.
     */

    public final void readResources()
        {
        super.readResources();

        intLogEntries = REGISTRY.getIntegerProperty(getResourceKey() + KEY_REPORT_LOG_ROWS);
        strMenuTruncate = "ToDo Truncate";
        //strMenuTruncate = REGISTRY.getString(getResourceKey() + KEY_REPORT_ACTION_TRUNCATE);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
