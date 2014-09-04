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

package org.lmn.fc.frameworks.starbase.ui.snmp;

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
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
import java.util.Vector;


/***************************************************************************************************
 * An SnmpObjectReport.
 */

public final class SnmpObjectReport extends ReportTable
                                    implements ReportTablePlugin
    {
    // String Resources
    private static final String REPORT_NAME = "SnmpObjectReport";

    private static final String HEADER_TITLE = "Simple Network Management Protocol";
    private static final String MSG_REPORT_CREATED = "SNMP Report created at ";
    private static final String MENU_CLEAR = "Clear SNMP Responses";

    private static final String TITLE_OID = "Object ID";
    private static final String TITLE_VALUE = "Value";
    private static final String TITLE_HEX = "Hex Value";
    private static final String TITLE_TYPE = "Type";

    private final Vector<Vector> vecSnmpLog;


    /***********************************************************************************************
     * Construct an SnmpObjectReport for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param resourcekey
     */

    public SnmpObjectReport(final TaskPlugin task,
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
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        vecSnmpLog = new Vector<Vector>(LOGSIZE_MAX);
        }


    /**********************************************************************************************
     * Run this UIComponent.
     */

    public final void runUI()
        {
        final URL imageURL;

        LOGGER.debugNavigation("SnmpObjectReport.runUI()");

        // Read the Resources for the SNMPanel
        readResources();

        imageURL = getClass().getResource(ACTION_ICON_CLEAR_SCREEN);

        if (imageURL != null)
            {
            final ContextAction clearContextAction;

            clearContextAction = new ContextAction(MENU_CLEAR,
                                                   new ImageIcon(imageURL),
                                                   getReportUniqueName() + COLON + SPACE + MENU_CLEAR,
                                                   KeyEvent.VK_T,
                                                   true,
                                                   true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    readResources();
                    clearResponses();
                    runUI();
                    }
                };

            // This Log is used in a Runnable Task,
            // so start a new ContextActionGroup for this UIComponent,
            // and add the new Action
            final ContextActionGroup group = new ContextActionGroup(getReportUniqueName(), true, true);
            group.addContextAction(clearContextAction);

            // Add the new Group to the list of Groups for this UIComponent
            clearUIComponentContextActionGroups();
            addUIComponentContextActionGroup(group);
            }

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

        vecColumns.add(new ReportColumnMetadata(TITLE_OID,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_VALUE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_HEX,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TYPE,
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
            "MMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the SnmpObjectReport data table directly from the SNMP log.
     *
     * @return Vector of report rows
     */

    public synchronized final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        // Make a copy of the Log for sorting...
        vecReport = (Vector<Vector>)getLog().clone();

        // Sort the copy of the Log by the ObjectID column
//        Collections.sort(vecReport,
//                         new ReportRowsByColumn(0));

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the SnmpObjectReport data table.
     *
     * @return Vector
     */

    public synchronized final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Add the specified entry to the Log.
     *
     * @param logentry
     */

    public synchronized void logger(final Vector<Object> logentry)
        {
        if ((getLog() != null)
            && (logentry != null)
            && (logentry.size() == defineColumnWidths().length))
            {
            getLog().add(logentry);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Clear the table of SNMP Responses.
     */

    private void clearResponses()
        {
        getTask().setStatus(EMPTY_STRING);
        getLog().clear();
        refreshTable();
        }


    /***********************************************************************************************
     * Get the SnmpObjectReport.
     *
     * @return Vector<Vector>
     */

    public synchronized Vector<Vector> getLog()
        {
        return (this.vecSnmpLog);
        }


    /***********************************************************************************************
     * Read all the Resources required by the SnmpObjectReport.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public final void readResources()
        {
        super.readResources();
        }
    }
