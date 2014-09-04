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
//  10-12-04    LMN created file
//  22-12-04    LMN implemented Action completion
//  02-01-05    LMN added comment entry for completed Actions
//  16-11-06    LMN changed for Starbase...
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.actions.ActionDataInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;


/**************************************************************************************************
 * The list of Actions for the User.
 */

public final class ActionListReport extends ReportTable
                                    implements ReportTablePlugin,
                                               FrameworkConstants,
                                               FrameworkStrings
    {
    // String Resources
    private static final String REPORT_NAME                 = "ActionList";

    private static final String HEADER_REPORT_CREATED       = "Report created at";

    private static final String TITLE_TYPE                  = SPACE;
    private static final String TITLE_COMPLETED             = "Completed";
    private static final String TITLE_ID                    = "ID";
    private static final String TITLE_DATE_RAISED           = "Raised";
    private static final String TITLE_TIME_RAISED           = SPACE;
    private static final String TITLE_DATE_COMPLETED        = "Completed";
    private static final String TITLE_TIME_COMPLETED        = SPACE;
    private static final String TITLE_ACTION                = "Action";
    private static final String TITLE_COMMENTS              = "Comments";
    private static final String TITLE_USER                  = "User";
    private static final String TITLE_ROLE                  = "Role";

    private static final String MSG_WAITING                 = "WAITING";
    private static final String MSG_CONFIRM_COMPLETION      = "Are you sure that you wish to mark this Action as Completed?";
    private static final String MSG_ADD_COMMENT             = "You may add a comment below";
    private static final String TOOLTIP_COMPLETED           = "Click on an Action in order to mark it as Completed";

    private static final int SORT_COLUMN = 2;


    /***********************************************************************************************
     * Create one row of the Report, indicating with a highlight if the Action is still pending.
     *
     * @param actiondata
     * @param row
     * @param rowid
     */

    private static void createReportRow(final ActionDataInterface actiondata,
                                        final Vector<Object> row,
                                        final int rowid)
        {
        final ReportIcon reportIcon;

        reportIcon = ReportIcon.getIcon(actiondata.getStatus().getIconFilename());
        row.add(reportIcon);
        row.add(actiondata.isCompleted());
        row.add(Integer.toString(rowid));

        if (actiondata.getDateRaised() != null)
            {
            row.add(ChronosHelper.toDateString(actiondata.getDateRaised()));
            }
        else
            {
            row.add(SPACE);
            }

        if (actiondata.getTimeRaised() != null)
            {
            row.add(ChronosHelper.toTimeString(actiondata.getTimeRaised()));
            }
        else
            {
            row.add(SPACE);
            }

        if (actiondata.getDateCompleted() != null)
            {
            row.add(ChronosHelper.toDateString(actiondata.getDateCompleted()));
            }
        else
            {
            row.add(ReportTableHelper.highlightCell(MSG_WAITING, true));
            }

        if (actiondata.getTimeCompleted() != null)
            {
            row.add(ChronosHelper.toTimeString(actiondata.getTimeCompleted()));
            }
        else
            {
            row.add(SPACE);
            }

        if (actiondata.getAction() != null)
            {
            row.add(ReportTableHelper.highlightCell(actiondata.getAction(),
                                                    !actiondata.isCompleted()));
            }
        else
            {
            row.add(SPACE);
            }

        if (actiondata.getComments() != null)
            {
            row.add(ReportTableHelper.highlightCell(actiondata.getComments(),
                                                    !actiondata.isCompleted()));
            }
        else
            {
            row.add(SPACE);
            }

        if (actiondata.getUserData() != null)
            {
            row.add(ReportTableHelper.highlightCell(actiondata.getUserData().getName(),
                                                    !actiondata.isCompleted()));
            }
        else
            {
            row.add(SPACE);
            }

        if (actiondata.getUserData().getRole() != null)
            {
            row.add(ReportTableHelper.highlightCell(actiondata.getUserData().getRole().getName(),
                                                    !actiondata.isCompleted()));
            }
        else
            {
            row.add(SPACE);
            }

        // Add the ActionData itself for later retrieval
        row.add(actiondata);
        }


    /**********************************************************************************************
     * Construct the ActionListReport.
     *
     * @param taskplugin
     * @param resourcekey
     */

    public ActionListReport(final TaskPlugin taskplugin,
                            final String resourcekey)
        {
        super(taskplugin,
              REPORT_NAME,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);
        }


    /***********************************************************************************************
     * Override the ReportTable.initialiseReport().
     *
     * @return JTable
     *
     * @throws ReportException
     */

    public synchronized JTable initialiseReport() throws ReportException
        {
        final JTable tableReport = super.initialiseReport();

        if (tableReport != null)
            {
            tableReport.setToolTipText(TOOLTIP_COMPLETED);

            // Listen for clicks on each Action
            tableReport.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                public void valueChanged(final ListSelectionEvent event)
                    {
                    final Object objComment;

                    // Ignore extra messages
                    if (event.getValueIsAdjusting())
                        {
                        return;
                        }

                    final ListSelectionModel modelSelection = (ListSelectionModel)event.getSource();

                    if (!modelSelection.isSelectionEmpty())
                        {
                        final int intRowSelected;

                        intRowSelected = modelSelection.getMinSelectionIndex();

                        // Retrieve the ActionData from this row
                        if ((getReportTableModel() != null)
                            && (getReportTableModel().getRowCount() > 0))
                            {
                            final Vector vecSelectedRow;

                            vecSelectedRow = getReportTableModel().getRowAt(intRowSelected);

                            if (vecSelectedRow != null)
                                {
                                final ActionDataInterface actionDataSelected;

                                // Get the contents of the row at one past the last displayed item
                                actionDataSelected = (ActionDataInterface)vecSelectedRow.elementAt(defineColumnWidths().length);

                                if ((actionDataSelected != null)
                                    && (!actionDataSelected.isCompleted()))
                                    {
                                    final String [] strMessage =
                                        {
                                        MSG_CONFIRM_COMPLETION,
                                        MSG_ADD_COMMENT
                                        };

                                    objComment = JOptionPane.showInputDialog(null,
                                                                             strMessage,
                                                                             REPORT_NAME,
                                                                             JOptionPane.QUESTION_MESSAGE,
                                                                             null,
                                                                             null,
                                                                             null);

                                    if ((objComment != null)
                                        && (objComment instanceof String))
                                        {
                                        actionDataSelected.setComments(actionDataSelected.getComments()
                                                                       + SPACE + objComment);
                                        actionDataSelected.setDateCompleted(Chronos.getCalendarDateNow());
                                        actionDataSelected.setTimeCompleted(Chronos.getCalendarTimeNow());
                                        actionDataSelected.setCompleted(true);
                                        refreshTable();

                                        // Archive the current Actions in the Exports folder
                                        archiveActions();
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

            tableReport.setRowSelectionAllowed(true);
            }

        return (tableReport);
        }


    /***********************************************************************************************
     * Archive the current Actions in the Exports folder, if possible.
     */

    public void archiveActions()
        {
        try
            {
            // Produce a tab-separated Report, but overwrite the same one each time
            if ((getTask() != null)
                && (REGISTRY_MODEL != null)
                && (REGISTRY_MODEL.getUserInterface() != null)
                && (REGISTRY_MODEL.getUserInterface().getUI() != null)
                && (REGISTRY_MODEL.getFramework() != null))
                {
                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                LOGGER.error("Need a way of archiving the Action List!");

                // Archive silently...
//                toFile(REGISTRY_MODEL.getFramework().getExportsFolder()
//                           + System.getProperty("file.separator")
//                           + getReportUniqueName()
//                           + FileUtilities.timestampFileName()
//                           + DOT
//                           + FileUtilities.tsv,
//                       toText(ReportTable.TAB_SEPARATOR));

                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

        catch (ReportException exception)
            {
            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            getTask().handleException(exception,
                                      "archiveActions()",
                                      EventStatus.WARNING);
            }
        }


    /**********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     *
     * @throws ReportException
     */

    public final Vector<String> generateHeader() throws ReportException
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(REPORT_NAME + SPACE + HEADER_REPORT_CREATED + SPACE + Chronos.timeNow());

        return (vecHeader);
        }


    /**********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);
        vecColumns.add(new ReportColumnMetadata(TITLE_TYPE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_COMPLETED,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_ID,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_DATE_RAISED,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME_RAISED,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DATE_COMPLETED,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME_COMPLETED,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_ACTION,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_COMMENTS,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_USER,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_ROLE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));

        return (vecColumns);
        }


    /**********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final Object [] columnWidths =
            {
            // Use an icon which we know must exist
            ReportIcon.getIcon(ICON_DUMMY),
            Boolean.TRUE,
            "999",
            "2000-00-00",
            "00:00:00",
            "2000-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMM",
            "MMMMMMMMM",
            "MMMMMMMMM",
            };

        return (columnWidths);
        }


    /**********************************************************************************************
     * Generate the ActionList report data table.
     *
     * @return Vector
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;
        Vector<Object> vecRow;
        final Iterator iterActions;
        int intID;

        vecReport = new Vector<Vector>(10);

        // Read the ActionList from the Registry
        if (REGISTRY.getActionList() != null)
            {
            iterActions = REGISTRY.getActionList().iterator();
            intID = 0;

            while ((iterActions != null)
                && (iterActions.hasNext()))
                {
                vecRow = new Vector<Object>(defineColumnWidths().length + 1);

                final ActionDataInterface actionData = (ActionDataInterface) iterActions.next();

                if (actionData != null)
                    {
                    createReportRow(actionData, vecRow, intID);
                    vecReport.add(vecRow);
                    intID++;
                    }
                }

            // Now sort the Report by the Action IDs (which are assigned sequentially)
            // and appear in SORT_COLUMN
            final ArrayList<Integer> listColumns;
            listColumns = new ArrayList<Integer>(10);
            listColumns.add(SORT_COLUMN);

            // Sort the Report rows by the list of specified column indexes
            Collections.sort(vecReport, new ReportRowsByColumn(listColumns));
            }

        return (vecReport);
        }


    /**********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
