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
//  22-10-04    LMN created file from OldNodeReport
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.registry.RegistryManagerPlugin;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose SqlTraceReport.
 */

public final class SqlTraceReport extends ReportTable
                                  implements ReportTablePlugin
    {
    // String Resources
    private static final String REPORT_NAME = "SqlTrace";
    private static final String MSG_REPORT_CREATED = "Framework Report created at";

    private static final int REPORT_COLUMN_COUNT = 7;

    private static final String TITLE_DATE = "Date";
    private static final String TITLE_TIME = "Time";
    private static final String TITLE_KEY = "Query Name";
    private static final String TITLE_CLASSNAME = "Caller Classname";
    private static final String TITLE_EXECUTION_COUNT = "Count";
    private static final String TITLE_EXECUTION_TIME = "Elapsed";
    private static final String TITLE_SQL_STATEMENT = "SQL Statement";

    private static final String ITEM_TIMING_DISABLED = "disabled";


    /***********************************************************************************************
     * Construct a SqlTraceReport JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param resourcekey
     *
     * @throws ReportException
     */

    public SqlTraceReport(final TaskPlugin task,
                          final String resourcekey) throws ReportException
        {
        super(task,
              REPORT_NAME,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              REFRESHABLE,
              REFRESH_CLICK,
              REORDERABLE,
              TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(REPORT_NAME + SPACE + MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

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

        vecColumns = new Vector<ReportColumnMetadata>(REPORT_COLUMN_COUNT);

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
        vecColumns.add(new ReportColumnMetadata( TITLE_KEY,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_CLASSNAME,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_EXECUTION_COUNT,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_EXECUTION_TIME,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_SQL_STATEMENT,
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
            "2004-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMM",
            "0000",
            "0000",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;
        Vector<Object> vecRow;
        final Iterator iterSqlTrace;
        QueryPlugin queryPlugin;

        vecReport = new Vector<Vector>(100);

        // Is the SQL Trace mode enabled?
        if ((getTask() != null)
            && (REGISTRY_MODEL.getSqlTrace()))
            {
            // Create the Report by unpacking the SqlTrace
            iterSqlTrace = REGISTRY_MANAGER.getQueryTrace().iterator();

            while ((iterSqlTrace != null)
                && (iterSqlTrace.hasNext()))
                {
                final Vector vecTraceItem = (Vector) iterSqlTrace.next();

                if ((vecTraceItem != null)
                    && (vecTraceItem.size() == RegistryManagerPlugin.TRACE_ITEMS)
                    && (vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_QUERY_DATA) != null)
                    && (vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_QUERY_DATA) instanceof QueryPlugin))
                    {
                    queryPlugin = (QueryPlugin)vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_QUERY_DATA);

                    if (queryPlugin != null)
                        {
                        vecRow = new Vector<Object>(REPORT_COLUMN_COUNT);
                        vecRow.add(vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_DATE).toString());
                        vecRow.add(vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_TIME).toString());
                        vecRow.add(queryPlugin.getPathname());
                        vecRow.add(vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_HOST_CLASS).getClass().getName());

                        // Is the SQL Timing mode enabled?
                        if (REGISTRY_MODEL.getSqlTiming())
                            {
                            vecRow.add(vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_EXEC_COUNT));
                            vecRow.add(vecTraceItem.get(RegistryManagerPlugin.INDEX_TRACE_EXEC_TIME));
                            }
                        else
                            {
                            vecRow.add(ReportTableHelper.greyCell(ITEM_TIMING_DISABLED, true));
                            vecRow.add(ReportTableHelper.greyCell(ITEM_TIMING_DISABLED, true));
                            }

                        // Show the SQL Statement
                        vecRow.add(queryPlugin.getResource());

                        vecReport.add(vecRow);
                        }
                    }
                }
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the SqlTraceReport data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Read all the Resources required by the SqlTraceReport.
     */

    public final void readResources()
        {
        super.readResources();
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
