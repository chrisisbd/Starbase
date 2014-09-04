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

package org.lmn.fc.ui.reports.impl;

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.util.Vector;


/***************************************************************************************************
 * Generate a NodeReport to show all loaded Nodes,
 * specifically for use on a JTabbedPane.
 */

public class TabbedNodeReportDecorator extends NodeReport
                                       implements ReportTablePlugin
    {
    private Vector<Vector> vecNodeReport;


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
     * @param report
     *
     * @throws ReportException
     */

    public TabbedNodeReportDecorator(final TaskPlugin task,
                                     final String reportname,
                                     final String resourcekey,
                                     final Vector<Vector> report) throws ReportException
        {
        super(task,
              reportname,
              resourcekey);

        if (report == null)
            {
            throw new ReportException(EXCEPTION_PARAMETER_NULL);
            }

        this.vecNodeReport = report;
        }


    /**********************************************************************************************
     * Set up the table given the header and column information.
     * Remove the ContextActions added by super.initialiseReport().
     */

    public JTable initialiseReport() throws ReportException
        {
        final JTable report;

        report = super.initialiseReport();

        // Remove the ContextActions added by initialiseReport();
        //super.clearContextActionGroups();

        return (report);
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        return (new Vector<String>(1));
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        return (this.vecNodeReport);
        }
    }
