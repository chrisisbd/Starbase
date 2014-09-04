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
//  18-10-04    LMN created file from RideManagerNodeReport
//  20-10-04    LMN extended OldNodeReport
//  28-10-04    LMN changed to extend a new NodeReport
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.ui.mail;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.ReportIcon;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.impl.NodeReport;

import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose RegistryReport.
 */

public final class OutboxReport extends NodeReport
                                implements ReportTablePlugin
    {
    private static final RegistryPlugin REGISTRY = Registry.getInstance();

    // String Resources
    private static final String REPORT_NAME = "RegistryReport";
    private static final String MSG_REPORT_CREATED = "Framework Node Report created at";


    /***********************************************************************************************
     * Construct an RegistryReport JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param resourcekey
     *
     * @throws ReportException
     */

    public OutboxReport(final TaskPlugin task,
                        final String resourcekey) throws ReportException
        {
        super(task,
              REPORT_NAME,
              resourcekey);
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector generateHeader()
        {
        final Vector vecHeader;

        vecHeader = new Vector();

        vecHeader.add(MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Generate the Node Report data table
     * from the list of Nodes maintained by the Organisation.
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
     * @return Vector of report rows
     */

    public final Vector generateReport()
        {
        FrameworkPlugin pluginFramework;
        RootPlugin frameworkTask;
        AtomPlugin applicationData;
        TaskPlugin applicationTask;
        AtomPlugin componentData;
        TaskPlugin componentTask;
        final Iterator<RootPlugin> iterFrameworkTasks;
        final Iterator<RootPlugin> iterApplications;
        Iterator iterApplicationTasks;
        Iterator<RootPlugin> iterComponents;
        Iterator iterComponentTasks;
        final Vector vecReport;
        Vector vecRow;

        vecReport = new Vector();
        pluginFramework = REGISTRY.getFramework();

        if (pluginFramework != null)
            {
            // Report on the Framework
            vecRow = new Vector();

            vecRow.add(ReportIcon.getIcon(pluginFramework.getIconFilename()));
            vecRow.add(new Boolean(pluginFramework.isActive()));
            vecRow.add(new Boolean(pluginFramework.isUpdated()));
            vecRow.add(pluginFramework.getPathname());
            vecRow.add("");
            vecRow.add(pluginFramework.getDescription());
            vecRow.add("????");
            vecRow.add(ChronosHelper.toDateString(pluginFramework.getModifiedDate()));
            vecRow.add(ChronosHelper.toTimeString(pluginFramework.getModifiedTime()));
            vecRow.add(REGISTRY.getFramework().getClass().getName());

            vecReport.add(vecRow);

            // Report on the FrameworkTasks
            iterFrameworkTasks = pluginFramework.getTasks().iterator();

            while ((iterFrameworkTasks != null)
                && (iterFrameworkTasks.hasNext()))
                {
                frameworkTask = iterFrameworkTasks.next();

                if (frameworkTask != null)
                    {
                    vecRow = new Vector();

                    vecRow.add(ReportIcon.getIcon(frameworkTask.getIconFilename()));
                    vecRow.add(new Boolean(frameworkTask.isActive()));
                    vecRow.add(new Boolean(frameworkTask.isUpdated()));
                    vecRow.add(frameworkTask.getPathname());
                    vecRow.add("");
                    vecRow.add(frameworkTask.getDescription());
                    vecRow.add(REGISTRY.getFramework().getPathname());
                    vecRow.add(ChronosHelper.toDateString(((UserObjectPlugin)frameworkTask).getModifiedDate()));
                    vecRow.add(ChronosHelper.toTimeString(((UserObjectPlugin)frameworkTask).getModifiedTime()));
                    vecRow.add(TaskData.class.getName());

                    vecReport.add(vecRow);
                    }
                }

            // Report on all Applications and ApplicationTasks
            if (pluginFramework.getAtoms() != null)
                {
                iterApplications = pluginFramework.getAtoms().iterator();

                while ((iterApplications != null)
                    && (iterApplications.hasNext()))
                    {
                    applicationData = (AtomPlugin)iterApplications.next();

                    if (applicationData != null)
                        {
                        vecRow = new Vector();

                        vecRow.add(ReportIcon.getIcon(applicationData.getIconFilename()));
                        vecRow.add(new Boolean(applicationData.isActive()));
                        vecRow.add(new Boolean(applicationData.isUpdated()));
                        vecRow.add(applicationData.getPathname());
                        vecRow.add("");
                        vecRow.add(applicationData.getDescription());
                        vecRow.add(REGISTRY.getFramework().getPathname());
                        vecRow.add(ChronosHelper.toDateString(applicationData.getModifiedDate()));
                        vecRow.add(ChronosHelper.toTimeString(applicationData.getModifiedTime()));
                        vecRow.add(AtomPlugin.class.getName());

                        vecReport.add(vecRow);

                        // Report on the ApplicationTasks for each Application
                        iterApplicationTasks = applicationData.getTasks().iterator();

                        while ((iterApplicationTasks != null)
                            && (iterApplicationTasks.hasNext()))
                            {
                            applicationTask = (TaskPlugin) iterApplicationTasks.next();

                            if (applicationTask != null)
                                {
                                vecRow = new Vector();

                                vecRow.add(ReportIcon.getIcon(applicationTask.getIconFilename()));
                                vecRow.add(new Boolean(applicationTask.isActive()));
                                vecRow.add(new Boolean(applicationTask.isUpdated()));
                                vecRow.add(applicationTask.getPathname());
                                vecRow.add("");
                                vecRow.add(applicationTask.getDescription());
                                vecRow.add(applicationTask.getParentAtom().getPathname());
                                vecRow.add(ChronosHelper.toDateString(applicationTask.getModifiedDate()));
                                vecRow.add(ChronosHelper.toTimeString(applicationTask.getModifiedTime()));
                                vecRow.add(TaskPlugin.class.getName());

                                vecReport.add(vecRow);
                                }
                            }

                        // Report on all Components and ComponentTasks
                        if (applicationData.getAtoms() != null)
                            {
                            iterComponents = applicationData.getAtoms().iterator();

                            while ((iterComponents != null)
                                && (iterComponents.hasNext()))
                                {
                                componentData = (AtomPlugin)iterComponents.next();

                                if (componentData != null)
                                    {
                                    vecRow = new Vector();

                                    vecRow.add(ReportIcon.getIcon(componentData.getIconFilename()));
                                    vecRow.add(new Boolean(componentData.isActive()));
                                    vecRow.add(new Boolean(componentData.isUpdated()));
                                    vecRow.add(componentData.getPathname());
                                    vecRow.add("");
                                    vecRow.add(componentData.getDescription());
                                    vecRow.add(componentData.getParentAtom().getPathname());
                                    vecRow.add(ChronosHelper.toDateString(componentData.getModifiedDate()));
                                    vecRow.add(ChronosHelper.toTimeString(componentData.getModifiedTime()));
                                    vecRow.add(AtomPlugin.class.getName());

                                    vecReport.add(vecRow);

                                    // Report on the ComponentTasks for each Component
                                    iterComponentTasks = componentData.getTasks().iterator();

                                    while ((iterComponentTasks != null)
                                        && (iterComponentTasks.hasNext()))
                                        {
                                        componentTask = (TaskPlugin) iterComponentTasks.next();

                                        if (componentTask != null)
                                            {
                                            vecRow = new Vector();

                                            vecRow.add(ReportIcon.getIcon(componentTask.getIconFilename()));
                                            vecRow.add(new Boolean(componentTask.isActive()));
                                            vecRow.add(new Boolean(componentTask.isUpdated()));
                                            vecRow.add(componentTask.getPathname());
                                            vecRow.add("");
                                            vecRow.add(componentTask.getDescription());
                                            vecRow.add(componentTask.getParentAtom().getPathname());
                                            vecRow.add(ChronosHelper.toDateString(componentTask.getModifiedDate()));
                                            vecRow.add(ChronosHelper.toTimeString(componentTask.getModifiedTime()));
                                            vecRow.add(TaskPlugin.class.getName());

                                            vecReport.add(vecRow);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // Now add all Resources to the Report
        vecReport.addAll(REGISTRY.getQueryReport());
        vecReport.addAll(REGISTRY.getExceptionReport());
        vecReport.addAll(REGISTRY.getStringReport());
        vecReport.addAll(REGISTRY.getPropertyReport());
        vecReport.addAll(REGISTRY.getCountriesReport());
        vecReport.addAll(REGISTRY.getLanguagesReport());
//        vecReport.addAll(REGISTRY.getDataTypesReport());

        return (vecReport);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
