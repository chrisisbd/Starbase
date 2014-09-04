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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration;

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
 * The Ftp ConfigurationReport.
 */

public final class FtpConfigurationUIComponent extends ReportTable
                                               implements ReportTablePlugin
    {
    // String Resources
    private static final String MSG_REPORT_CREATED  = "Configuration Report created at";

    private static final String TITLE_ICON          = SPACE;
    private static final String TITLE_PROPERTY      = "Property";
    private static final String TITLE_VALUE         = "Value";


    /***********************************************************************************************
     * Construct an FtpConfigurationUIComponent JTable for the specified Task.
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
     */

    public FtpConfigurationUIComponent(final RootPlugin task,
                                       final String reportname,
                                       final String resourcekey)
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

        //vecHeader.add(getReportUniqueName() + SPACE + MSG_EPHEMERIS_CREATED + SPACE + Chron os.timeNow());

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
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String key;
        final Vector<Vector> vecReport;
        Vector<Object> vecRow;

        key = getTask().getResourceKey();

        vecReport = new Vector<Vector>(10);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_CONNECTION_HOSTNAME);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_CONNECTION_HOSTNAME));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_CONNECTION_USERNAME);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_CONNECTION_USERNAME));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_CONNECTION_PASSWORD);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_CONNECTION_PASSWORD));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_CONNECTION_CONNECTION_MODE);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_CONNECTION_CONNECTION_MODE));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_CONNECTION_TRANSFER_MODE);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_CONNECTION_TRANSFER_MODE));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_TRANSFER_LOCAL_DIRECTORY);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_TRANSFER_LOCAL_DIRECTORY));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_TRANSFER_LOCAL_FILENAME);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_TRANSFER_LOCAL_FILENAME));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_TRANSFER_REMOTE_DIRECTORY);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_TRANSFER_REMOTE_DIRECTORY));
        vecReport.add(vecRow);

        vecRow = new Vector<Object>(2);
        vecRow.add(KEY_DAO_TRANSFER_REMOTE_FILENAME);
        vecRow.add(REGISTRY.getStringProperty(key + KEY_DAO_TRANSFER_REMOTE_FILENAME));
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
