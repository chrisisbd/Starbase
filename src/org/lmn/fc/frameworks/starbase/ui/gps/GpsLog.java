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
//  28-10-04    LMN created file
//  07-08-06    LMN tidying up!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.ui.gps;

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
 * A GpsLog.
 */

public final class GpsLog extends ReportTable
                          implements ReportTablePlugin
    {
    public static final int INSTRUMENT_LOG_WIDTH = 13;

    // String Resources
    private static final String REPORT_NAME             = "GpsLog";

    private static final String HEADER_TITLE            = "GpsReceiver Location Log";
    private static final String MSG_REPORT_CREATED      = "GpsReceiver Report created at";

    private static final String TITLE_ICON              = SPACE;
    private static final String TITLE_DATE              = "Date";
    private static final String TITLE_TIME              = "Time";
    private static final String TITLE_LONGITUDE         = "Longitude";
    private static final String TITLE_LATITUDE          = "Latitude";
    private static final String TITLE_HASL              = "HeightASL";
    private static final String TITLE_GEOID_ALT         = "GeoidAltitude";
    private static final String TITLE_MAG_VARIATION     = "Variation";
    private static final String TITLE_HDOP              = "HDOP";
    private static final String TITLE_VDOP              = "VDOP";
    private static final String TITLE_FIX_TYPE          = "FixType";
    private static final String TITLE_FIX_SATELLITES    = "Satellites";
    private static final String TITLE_STATUS            = "Status";

    private static final String MENU_TRUNCATE           = "Truncate GPS Log";

    private final Vector<Vector> vecGpsLog;
    private int intLogIndex;


    /***********************************************************************************************
     * Construct an GpsLog for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param resourcekey
     */

    public GpsLog(final TaskPlugin task,
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

        vecGpsLog = new Vector<Vector>(LOGSIZE_MAX);
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
            // Task-specific Context Actions
            final URL imageURL;

            imageURL = getClass().getResource(ACTION_ICON_TRUNCATE);

            if (imageURL != null)
                {
                final ContextAction truncateContextAction;

                truncateContextAction = new ContextAction(MENU_TRUNCATE,
                                                          new ImageIcon(imageURL),
                                                          MENU_TRUNCATE,
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
        vecColumns.add(new ReportColumnMetadata( TITLE_LONGITUDE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_LATITUDE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_HASL,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_GEOID_ALT,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_MAG_VARIATION,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_HDOP,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_VDOP,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_FIX_TYPE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_FIX_SATELLITES,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_STATUS,
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
            "2000-00-00",
            "00:00:00",
            "+000:00:00E",
            "+00:00:00N",
            "000000",
            "000000",
            "0000",
            "000000",
            "000000",
            "00",
            "00",
            "MMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the GpsLog table.
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
     * Refresh the GpsLog data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Add the specified entry to the Log.
     * ToDo remove ... Used only in GpsUI now...
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
     * Truncate the GpsLog.
     */

    public synchronized final void truncateReport()
        {
        final int intChoice;
        final String [] strMessage =
            {
            "Are you sure that you wish to truncate the GPS Log",
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
     * Get the GpsLog.
     *
     * @return Vector<Vector>
     */

    private synchronized Vector<Vector> getLog()
        {
        return (this.vecGpsLog);
        }


    /***********************************************************************************************
     * Read all the Resources required by the GpsLog.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public final void readResources()
        {
        super.readResources();
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
