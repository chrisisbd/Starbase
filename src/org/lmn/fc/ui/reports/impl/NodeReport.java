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
//  22-06-04    LMN created file
//  04-10-04    LMN updated String Resources
//  28-10-04    LMN rewrote to include Framework and custom Application nodes
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.Vector;


/***************************************************************************************************
 * Generate a NodeReport to show all loaded Nodes (Framework and Application).
 */

public class NodeReport extends ReportTable
                        implements ReportTablePlugin
    {
    // String Resources
    private static final String MSG_REPORT_CREATED  = "Node Report created at";

    private static final int REPORT_COLUMN_COUNT = 10;

    private static final String TITLE_ICON          = SPACE;
    private static final String TITLE_ACTIVE        = "Active";
    private static final String TITLE_UPDATED       = "Updated";
    private static final String TITLE_KEY           = "Key";
    private static final String TITLE_VALUE         = "Value";
    private static final String TITLE_DESCRIPTION   = "Description";
    private static final String TITLE_PARENT        = "Parent";
    private static final String TITLE_MODIFIED_DATE = "Modified";
    private static final String TITLE_MODIFIED_TIME = "Time";
    private static final String TITLE_CLASSNAME     = "Classname";


    /***********************************************************************************************
     * Construct an NodeReport JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * The Report contains the following columns:
     * <code>
     * <li>Icon
     * <li>Active
     * <li>Updated
     * <li>Key
     * <li>Value
     * <li>Description
     * <li>Parent
     * <li>Modified Date
     * <li>Modified Time
     * <li>Classname
     * </code>
     *
     * @param task
     * @param reportname
     * @param resourcekey
     *
     * @throws ReportException
     */

    public NodeReport(final TaskPlugin task,
                      final String reportname,
                      final String resourcekey) throws ReportException
        {
        super(task,
              reportname,
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
        }


    /***********************************************************************************************
     * Generate the report header.
     * This <b>may</b> be overridden by the subclasses.
     *
     * @return Vector
     *
     * @throws ReportException
     */

    public Vector<String> generateHeader() throws ReportException
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(getReportUniqueName() + SPACE + MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     * This <b>may</b> be overridden by the subclasses.
     *
     * @return Vector
     */

    public Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(REPORT_COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_ACTIVE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_UPDATED,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_KEY,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_VALUE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_DESCRIPTION,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_PARENT,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_MODIFIED_DATE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_MODIFIED_TIME,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_CLASSNAME,
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
            ReportIcon.getIcon(ICON_DUMMY),
            Boolean.TRUE,
            Boolean.TRUE,
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMM",
            "2000-01-01",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     * This <b>must</b> be overridden by the subclasses.
     *
     * @return Vector of report rows
     *
     * @throws ReportException
     */

    public Vector<Vector> generateReport() throws ReportException
        {
        return (new Vector<Vector>());
        }


    /***********************************************************************************************
     * Refresh the Report data.
     * This <b>may</b> be overridden by the subclasses.
     *
     * @return Vector
     *
     * @throws ReportException
     */

    public Vector<Vector> refreshReport() throws ReportException
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Read all the Resources required by the NodeReport.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public final void readResources()
        {
        super.readResources();
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
