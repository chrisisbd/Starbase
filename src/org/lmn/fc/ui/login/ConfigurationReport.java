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

package org.lmn.fc.ui.login;

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.database.DatabaseOptions;
import org.lmn.fc.database.DatabaseType;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.Vector;


/***************************************************************************************************
 * The Login ConfigurationReport.
 */

final class ConfigurationReport extends ReportTable
                                implements ReportTablePlugin
    {
    // String Resources
    public static final String REPORT_NAME          = "Configuration";
    private static final String MSG_REPORT_CREATED  = "Configuration Report created at";

    private static final String TITLE_ICON          = SPACE;
    private static final String TITLE_PROPERTY      = "Property";
    private static final String TITLE_VALUE         = "Value";


    /***********************************************************************************************
     * Construct an ConfigurationReport JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * The Report contains the following columns:
     * <code>
     * <li>Icon
     * <li>Property
     * <li>Value
     * </code>
     *
     * @param task
     * @param reportname
     * @param resourcekey
     *
     * @throws ReportException
     */

    ConfigurationReport(final RootPlugin task,
                        final String reportname,
                        final String resourcekey) throws ReportException
        {
        super(task,
              reportname,
              resourcekey,
              NON_PRINTABLE,
              NON_EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
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

        vecHeader.add(getReportUniqueName() + SPACE + MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

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

        vecColumns = new Vector<ReportColumnMetadata>(2);

        //vecColumns.add(new ReportColumnData(TITLE_ICON, SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_PROPERTY,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_VALUE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));

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
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     * This <b>must</b> be overridden by the subclasses.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final DatabaseOptions options;
        final Vector<Vector> vecReport;
        Vector<Object> vecRow;

        options = LOADER_PROPERTIES.getDatabaseOptions();
        vecReport = new Vector<Vector>(100);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG);
        vecRow.add(LOADER_PROPERTIES.isMasterDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_STARIBUS);
        vecRow.add(LOADER_PROPERTIES.isStaribusDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_STARINET);
        vecRow.add(LOADER_PROPERTIES.isStarinetDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_TIMING);
        vecRow.add(LOADER_PROPERTIES.isTimingDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_STATE);
        vecRow.add(LOADER_PROPERTIES.isStateDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_METADATA);
        vecRow.add(LOADER_PROPERTIES.isMetadataDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_CHART);
        vecRow.add(LOADER_PROPERTIES.isChartDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_DEBUG_THREADS);
        vecRow.add(LOADER_PROPERTIES.isThreadsDebug());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_JMX_USERNAME);
        vecRow.add(LOADER_PROPERTIES.getJmxUsername());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_JMX_PASSWORD);
        vecRow.add(LOADER_PROPERTIES.getJmxPassword());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_JMX_PORT);
        vecRow.add(LOADER_PROPERTIES.getJmxPort());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DATABASE_STORE);
        vecRow.add(options.getDataStore().getName());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DATABASE_TYPE);
        vecRow.add(options.getDatabaseType().getType());
        vecReport.add(vecRow);

        // Local Properties
        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_DRIVER);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).getDriver());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_DATA_SOURCE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).getDataSource());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_PORT);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).getPort());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_DATABASE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).getDatabase());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_INLINE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).isCredentialsInline());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_USER_NAME);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).getUsername());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_LOCAL_PASSWORD);
        vecRow.add(options.getDatabaseProperties(DatabaseType.LOCAL).getPassword());
        vecReport.add(vecRow);

        // Remote Properties
        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_DRIVER);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).getDriver());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_DATA_SOURCE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).getDataSource());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_PORT);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).getPort());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_DATABASE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).getDatabase());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_INLINE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).isCredentialsInline());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_USER_NAME);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).getUsername());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_REMOTE_PASSWORD);
        vecRow.add(options.getDatabaseProperties(DatabaseType.REMOTE).getPassword());
        vecReport.add(vecRow);

        // Embedded Properties
        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_DRIVER);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).getDriver());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_DATA_SOURCE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).getDataSource());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_PORT);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).getPort());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_DATABASE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).getDatabase());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_INLINE);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).isCredentialsInline());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_USER_NAME);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).getUsername());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_EMBEDDED_PASSWORD);
        vecRow.add(options.getDatabaseProperties(DatabaseType.EMBEDDED).getPassword());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_SQL_TRACE);
        vecRow.add(LOADER_PROPERTIES.isSqlTrace());
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_ENABLE_SQL_TIMING);
        vecRow.add(LOADER_PROPERTIES.isSqlTiming());
        vecReport.add(vecRow);

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data.
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
