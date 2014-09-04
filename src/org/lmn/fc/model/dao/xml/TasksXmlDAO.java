// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.model.dao.xml;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.dao.TasksDAOInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.model.xmlbeans.tasks.Task;
import org.lmn.fc.model.xmlbeans.tasks.TasksDocument;

import java.io.File;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * The TasksXmlDAO.
 */

public final class TasksXmlDAO implements TasksDAOInterface
    {
    private static final BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    private final AtomPlugin hostAtom;
    private final String strFolder;
    private final boolean boolDebugMode;


    /***********************************************************************************************
     * Construct a TasksXmlDAO.
     * The methods are never used recursively, and so the folder may be set in the constructor.
     *
     * @param host
     * @param folder
     * @param debug
     */

    public TasksXmlDAO(final AtomPlugin host,
                       final String folder,
                       final boolean debug)
        {
        this.hostAtom = host;
        this.strFolder = folder;
        this.boolDebugMode = debug;
        }


    /***********************************************************************************************
     * Import the Tasks for the specified Atom from the specified folder.
     *
     * @throws FrameworkException
     */

    public void importTasks() throws FrameworkException
        {
        final File xmlFile;

        if ((getHost() == null)
            || (getHost().getLevel() == null)
            || (getFolder() == null)
            || (EMPTY_STRING.equals(getFolder())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        LOGGER.login("Importing "
                      + getHost().getName()
                      + SPACE
                      + getHost().getLevel()
                      + " Tasks from ["
                      + getFolder()
                      + "]");

        xmlFile = new File(InstallationFolder.getTerminatedUserDir()
                           + getFolder()
                           + TASKS_XML);
        try
            {
            final TasksDocument docTasks;
            final TasksDocument.Tasks tasks;
            final List<Task> list;
            final Iterator<Task> iterList;

            docTasks = TasksDocument.Factory.parse(xmlFile);

            if (!XmlBeansUtilities.isValidXml(docTasks))
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            tasks = docTasks.getTasks();
            list = tasks.getTaskList();
            iterList = list.iterator();

            while (iterList.hasNext())
                {
                final Task taskXml;
                final TaskPlugin plugin;

                taskXml = iterList.next();

                // Check that we know enough to install this Task
                if ((taskXml != null)
                    && (taskXml.getName() != null)
                    && (!EMPTY_STRING.equals(taskXml.getName())))
                    {
                    // Initialise the TaskPlugin from the XML Task configuration
                    plugin = (TaskPlugin) BEAN_FACTORY_XML.createTask(getHost(),
                                                                      TaskData.class,
                                                                      taskXml);
                    if ((plugin != null)
                        && (plugin.getName() != null)
                        && (plugin.getHostTreeNode() != null)
                        && (plugin.getResourceKey() != null))
                        {
                        // Set the parent Atom of this TaskPlugin
                        // (this should have happened in the BeanFactory)
                        plugin.setParentAtom(getHost());

                        // Identify and link the RootTask
                        if (plugin.isRootTask())
                            {
                            getHost().setRootTask(plugin);
                            }

                        // Attach this TaskPlugin to the host Atom
                        getHost().addTask(plugin);

                        // Assign the UserRoles now that the TaskPlugin is completely specified
                        TaskData.assignRoles(plugin);

                        // Add this TaskPlugin to the Registry
                        // NOTE! This requires that the parent has been set beforehand!
                        //LOGGER.login("Registering Task " + plugin.getResourceKey());
                        REGISTRY.addTask(plugin.getResourceKey(), plugin);

                        // Do some debugging as the installation proceeds
                        plugin.setDebugMode(getDebugMode());
                        plugin.showDebugData();
                        }
                    else
                        {
                        throw new FrameworkException(EXCEPTION_CREATE_TASK + SPACE + taskXml.getDescription());
                        }
                    }
                else
                    {
                    throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            // Do some more debugging as the installation proceeds
            getHost().showAttachedTasks(getDebugMode());
            REGISTRY.showTasks(getDebugMode());
            }

        // Break the rules here, because we don't yet have XmlExceptions loaded!
        // The FrameworkLoader would try to load the class before the corresponding Jar
        // was loaded on to the classpath. Oh well...
        catch (Exception exception)
            {
            LOGGER.login("Generic Exception in importTasks() [exception=" + exception + "]");
            throw new FrameworkException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     *
     * @throws FrameworkException
     */

    public void exportTasks() throws FrameworkException
        {
        }


    /***********************************************************************************************
     * Get the host Atom.
     *
     * @return AtomPlugin
     */

    private AtomPlugin getHost()
        {
        return (this.hostAtom);
        }


    /***********************************************************************************************
     * Get the name of the folder containing the XML file.
     *
     * @return String
     */

    private String getFolder()
        {
        return (this.strFolder);
        }


    /***********************************************************************************************
     * Get the debug mode.
     *
     * @return boolean
     */

    private boolean getDebugMode()
        {
        return (this.boolDebugMode);
        }
    }
