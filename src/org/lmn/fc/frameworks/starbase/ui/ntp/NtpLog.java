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
//  14-10-04    LMN created file from RideManagerNodeReport
//  15-10-04    LMN added more columns, String Resources, sorted in reverse time order
//  08-08-06    LMN changed for new structure
//  23-01-07    LMN changed run/stop sequence to fix menu/toolbar state bug
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.ui.ntp;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
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
import java.util.Collections;
import java.util.Vector;


/***************************************************************************************************
 * An NtpLog.
 */

public final class NtpLog extends ReportTable
                          implements ReportTablePlugin
    {
    public static final int EVENT_LOG_WIDTH = 15;

    // String Resources
    private static final String REPORT_NAME = "NtpLog";

    private static final String HEADER_TITLE = "Network Time Protocol";
    private static final String MSG_REPORT_CREATED = "NTP Report created at ";

    private static final String TITLE_ICON              = SPACE;
    private static final String TITLE_SET               = "Set";
    private static final String TITLE_DATE              = "Date";
    private static final String TITLE_TIME              = "Time";
    private static final String TITLE_TIME_SERVER       = "Time Server";
    private static final String TITLE_SERVER_ADDRESS    = "Server Address";
    private static final String TITLE_VERSION           = "Version";
    private static final String TITLE_OFFSET            = "Offset msec";
    private static final String TITLE_DELAY             = "Delay msec";
    private static final String TITLE_STRATUM           = "Stratum";
    private static final String TITLE_PRECISION         = "Precision msec";
    private static final String TITLE_STATUS            = "Status";
    private static final String TITLE_TIMESTAMP_DATE    = "DateStamp";
    private static final String TITLE_TIMESTAMP_TIME    = "Time";
    private static final String TITLE_TIMESTAMP_FRACTION = "Seconds";

    private static final String MENU_TRUNCATE = "Truncate NTP Log";

    private final Vector<Vector> vecEventLog;
    private int intLogIndex;


    /***********************************************************************************************
     * Construct an NtpLog for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param resourcekey
     */

    public NtpLog(final TaskPlugin task,
                  final String resourcekey)
        {
        super(task,
              REPORT_NAME,
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

        vecEventLog = new Vector<Vector>(LOGSIZE_MAX);
        intLogIndex = 0;
        }


    /***********************************************************************************************
     * Run the UI of this Report.
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

            imageURL = getClass().getResource(ACTION_ICON_TRUNCATE);

            if (imageURL != null)
                {
                final ContextAction truncateContextAction;

                truncateContextAction = new ContextAction(MENU_TRUNCATE,
                                                          new ImageIcon(imageURL),
                                                          getReportUniqueName() + COLON + SPACE + MENU_TRUNCATE,
                                                          KeyEvent.VK_T,
                                                          true,
                                                          true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        readResources();
                        truncateReport();
                        runUI();
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
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(2);

        vecHeader.add(HEADER_TITLE);
        vecHeader.add(MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata( TITLE_ICON,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata( TITLE_SET,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.CENTER ));
        vecColumns.add(new ReportColumnMetadata( TITLE_DATE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_TIME,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_TIME_SERVER,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_SERVER_ADDRESS,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_VERSION,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_OFFSET,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_DELAY,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_STRATUM,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_PRECISION,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_STATUS,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_TIMESTAMP_DATE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_TIMESTAMP_TIME,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_TIMESTAMP_FRACTION,
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
            Boolean.TRUE,
            "2000-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMM",
            "MMMMMMMMMMM",
            "0",
            "000000",
            "000000",
            "0",
            "00000000000000E00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "2000-00-00",
            "00:00:00",
            "99999999"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the NtpLog data table directly from the NtpDaemon or NtpDAO log.
     * The report is sorted in reverse order so that the most recent entry is at the top.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        // Make a copy of the Log for sorting...
        vecReport = (Vector<Vector>)getLog().clone();

        // Sort the copy of the Log by the 'hidden' last Integer column
        Collections.sort(vecReport,
                         new ReportRowsByColumn(defineColumnWidths().length));

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the NtpLog data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Add the specified entry to the Log.
     * ToDo remove ... used only in NtpDaemon now...
     *
     * @param logentry
     */

    public synchronized void logger(final Vector<Object> logentry)
        {
        if ((getLog() != null)
            && (logentry != null)
            && (logentry.size() == defineColumnWidths().length))
            {
            // Add the LogIndex at the end (used for sorting, not display)
            logentry.add(intLogIndex);

            // Count a new entry to the Log, so they are shown in descending order
            intLogIndex--;

            getLog().add(logentry);

            // Limit the Log size!
            if (getLog().size() >= LOGSIZE_MAX)
                {
                // Just remove the oldest entry in the Log
                getLog().removeElementAt(0);
                }

            // Force an immediate update
            refreshTable();
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Truncate the NtpLog.
     */

    public synchronized final void truncateReport()
        {
        final int intChoice;
        final String [] strMessage =
            {
            "Are you sure that you wish to truncate the NTP Log",
            "to leave" + SPACE + LOGSIZE_TRUNCATE + SPACE + "items?"
            };

        if ((getLog() != null)
            && (getLog().size() > LOGSIZE_TRUNCATE))
            {
            intChoice = JOptionPane.showOptionDialog(null,
                                                     strMessage,
                                                     getReportUniqueName(),
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     null,
                                                     null);

            while ((intChoice == JOptionPane.YES_OPTION)
                && (getLog() != null)
                && (getLog().size() > LOGSIZE_TRUNCATE))
                {
                getLog().remove(0);
                }
            }
        }


    /***********************************************************************************************
     * Get the NtpLog.
     *
     * @return Vector<Vector>
     */

    private synchronized Vector<Vector> getLog()
        {
        return (this.vecEventLog);
        }


    /***********************************************************************************************
     * Read all the Resources required by the NtpLog.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public final void readResources()
        {
        super.readResources();
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
