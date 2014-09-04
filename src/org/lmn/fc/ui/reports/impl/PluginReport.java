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
//  11-05-04    LMN created file
//  12-05-04    LMN adding all Tasks
//  21-05-04    LMN added exportText()
//  07-06-06    LMN making it recursive!
//  01-02-07    LMN adding Atoms!
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose PluginReport, to show the status of all Atoms and Tasks under a specified Atom.
 */

public final class PluginReport extends ReportTable
                                implements ReportTablePlugin
    {
    // String Resources
    private static final String TITLE_PLUGIN = "Plugin";
    private static final String TITLE_STATUS = "Status";
    private static final String TITLE_STARTED_DATE = "Started";
    private static final String TITLE_STARTED_TIME = "";
    private static final String TITLE_STOPPED_DATE = "Stopped";
    private static final String TITLE_STOPPED_TIME = "";
    private static final String TITLE_ACTIVE = "Active";
    private static final String TITLE_RUN_AT_START = "RunAtStart";
    private static final String TITLE_PUBLIC = "Public";
    private static final String TITLE_EDITABLE = "Editable";
    private static final String TITLE_LEVEL = "Level";
    private static final String MSG_REPORT_CREATED = "Plugin and Task Report created at ";

    private static final int REPORT_COLUMN_COUNT = 12;


    /***********************************************************************************************
     * Add the Atom and all child Tasks to the report, below and including the specified Atom.
     *
     * @param atom
     * @param report
     */

    private static void addChildren(final AtomPlugin atom,
                                    final Vector<Vector> report)
        {
        final Vector<RootPlugin> vecAtoms;
        final Iterator<RootPlugin> iterAtoms;

        if ((atom != null)
            && (atom.validatePlugin())
            && (atom.getTasks() != null)
            && (report != null))
            {
            addAtom(atom, report);
            addTasks(atom, report);
            addBlankLine(report);

            // Now recursively add all child Atoms and Tasks...
            vecAtoms = atom.getAtoms();
            iterAtoms = vecAtoms.iterator();

            while (iterAtoms.hasNext())
                {
                final AtomPlugin childatom;

                childatom = (AtomPlugin) iterAtoms.next();
                addChildren(childatom, report);
                }
            }
        }


    /***********************************************************************************************
     * Add the specified Atom to the PluginReport.
     *
     * @param atom
     * @param report
     */

    private static void addAtom(final AtomPlugin atom,
                                final Vector<Vector> report)
        {
        final Vector<Object> vecRow;

        vecRow = new Vector<Object>(REPORT_COLUMN_COUNT);

        if ((atom != null)
            && (atom.validatePlugin()))
            {
            final ImageIcon icon;

            icon = RegistryModelUtilities.getAtomIcon(atom, atom.getIconFilename());

            if (icon != null)
                {
                final Image image;

                image = icon.getImage();
                // ToDo Icon size
                if (image != null)
                    {
                    image.getScaledInstance(13, 13, Image.SCALE_SMOOTH);

                    vecRow.add(new ImageIcon(image));
                    }
                else
                    {
                    System.out.println("Could not scale Icon for PluginReport [atom=" + atom.getName() + "]");
                    vecRow.add(new ImageIcon());
                    }
                }
            else
                {
                System.out.println("Could not find Icon for PluginReport [atom=" + atom.getName() + "]");
                vecRow.add(new ImageIcon());
                }

            vecRow.add(atom.getPathname());
            vecRow.add(atom.getState().getStatus());

            if ((atom.getStartDate() != null)
                && (atom.getStartTime() != null))
                {
                vecRow.add(ChronosHelper.toDateString(atom.getStartDate()));
                vecRow.add(ChronosHelper.toTimeString(atom.getStartTime()));
                }
            else
                {
                vecRow.add(EMPTY_STRING);
                vecRow.add(EMPTY_STRING);
                }

            if ((atom.getStopDate() != null)
                && (atom.getStopTime() != null))
                {
                vecRow.add(ChronosHelper.toDateString(atom.getStopDate()));
                vecRow.add(ChronosHelper.toTimeString(atom.getStopTime()));
                }
            else
                {
                vecRow.add(EMPTY_STRING);
                vecRow.add(EMPTY_STRING);
                }

            vecRow.add(atom.isActive());
            vecRow.add(atom.isLoadAtStart());
            vecRow.add(Boolean.TRUE);
            vecRow.add(atom.isEditable());
            vecRow.add(atom.getLevel());

            if (vecRow.size() > 0)
                {
                report.add(vecRow);
                }
            }
        }


    /***********************************************************************************************
     * Add the iterated Tasks to the PluginReport.
     *
     * @param atom
     * @param report
     */

    private static void addTasks(final AtomPlugin atom,
                                 final Vector<Vector> report)
        {
        // Iterate over all child Tasks of this Atom
        final Iterator<RootPlugin> iterTasks;

        iterTasks = atom.getTasks().iterator();

        // Retrieve the Tasks in the order in which they were added
        while (iterTasks.hasNext())
            {
            final RootPlugin pluginTask;
            final Vector<Object> vecRow;

            pluginTask = iterTasks.next();
            vecRow = addTask((TaskPlugin)pluginTask);

            if (vecRow.size() > 0)
                {
                report.add(vecRow);
                }
            }
        }


    /***********************************************************************************************
     * Add the specified Task to the PluginReport.
     *
     * @param task
     *
     * @return Vector
     */

    private static Vector<Object> addTask(final TaskPlugin task)
        {
        final Vector<Object> vecRow;

        vecRow = new Vector<Object>(REPORT_COLUMN_COUNT);

        if ((task != null)
            && (task.validatePlugin())
            && (task.isRunnable()))
            {
            vecRow.add(RegistryModelUtilities.getCommonIcon(task.getIconFilename()));
            vecRow.add(task.getPathname());
            vecRow.add(task.getState().getStatus());

            if ((task.getStartDate() != null)
                && (task.getStartTime() != null))
                {
                vecRow.add(ChronosHelper.toDateString(task.getStartDate()));
                vecRow.add(ChronosHelper.toTimeString(task.getStartTime()));
                }
            else
                {
                vecRow.add(EMPTY_STRING);
                vecRow.add(EMPTY_STRING);
                }

            if ((task.getStopDate() != null)
                && (task.getStopTime() != null))
                {
                vecRow.add(ChronosHelper.toDateString(task.getStopDate()));
                vecRow.add(ChronosHelper.toTimeString(task.getStopTime()));
                }
            else
                {
                vecRow.add(EMPTY_STRING);
                vecRow.add(EMPTY_STRING);
                }

            vecRow.add(task.isActive());
            vecRow.add(task.isRunAtStart());
            vecRow.add(task.isPublic());
            vecRow.add(task.isEditable());
            vecRow.add(task.getLevel());
            }

        return (vecRow);
        }


    /***********************************************************************************************
     * Add a blank line to the PluginReport.
     *
     * @param report
     */

    private static void addBlankLine(final Vector<Vector> report)
        {
        final Vector<Object> vecRow;

        if (report != null)
            {
            vecRow = new Vector<Object>(REPORT_COLUMN_COUNT);

            vecRow.add(null);
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            vecRow.add(Boolean.FALSE);
            vecRow.add(Boolean.FALSE);
            vecRow.add(Boolean.FALSE);
            vecRow.add(Boolean.FALSE);
            vecRow.add(EMPTY_STRING);

            report.add(vecRow);
            }
        }


    /***********************************************************************************************
     * Construct an PluginReport JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param resourcekey
     *
     * @throws ReportException
     */

    public PluginReport(final TaskPlugin task,
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

        vecHeader.add(MSG_REPORT_CREATED + Chronos.timeNow());

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
        vecColumns.add(new ReportColumnMetadata(SPACE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_PLUGIN,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STATUS,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STARTED_DATE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STARTED_TIME,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STOPPED_DATE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STOPPED_TIME,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_ACTIVE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RUN_AT_START,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_PUBLIC,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_EDITABLE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_LEVEL,
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
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMM",
            "2004-00-00",
            "00:00:00",
            "2004-00-00",
            "00:00:00",
            Boolean.TRUE,
            Boolean.TRUE,
            Boolean.TRUE,
            Boolean.TRUE,
            "Framework"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the Task report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(20);

        // Iterate over all recursion levels below that of the current Atom
        if ((getTask() != null)
            && (getTask().getParentAtom() != null)
            && (REGISTRY.getFramework().getRecursionLevels() != null))
            {
            // Add all Tasks below and including the parent Atom
            addChildren(getTask().getParentAtom(), vecReport);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Read all the Resources required by the PluginReport.
     */

    public final void readResources()
        {
        super.readResources();
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
