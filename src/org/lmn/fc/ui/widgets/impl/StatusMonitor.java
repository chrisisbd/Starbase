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
//  14-03-05    LMN created file
//  30-03-05    LMN converting for Generics, tidying up
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.widgets.impl;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.IndicatorException;
import org.lmn.fc.model.plugins.impl.AtomData;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.widgets.MonitoredItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * StatusMonitor shows a panel of lights indicating e.g. Task running.
 * The Tasks may be started and stopped by clicking on the lights.
 */

public final class StatusMonitor extends StatusLights
                                 implements FrameworkConstants,
                                            FrameworkSingletons,
                                            FrameworkStrings,
                                            FrameworkMetadata
    {
    // String Resources
    private static final String MSG_TASK_STOP_QUESTION   = "Are you sure that you want to stop";
    private static final String MSG_TASK_START_QUESTION  = "Are you sure that you want to start";
    private static final String STATUS_RUNNING  = "running";
    private static final String STATUS_STOPPED  = "stopped";

    private final Vector<MonitoredItem> vecMonitor;


    /***********************************************************************************************
     * Add a single MonitoredItem to the StatusMonitor list.
     *
     * @param monitor
     * @param item
     *
     * @return Vector
     */

    public static Vector addMonitoredItem(final Vector<MonitoredItem> monitor,
                                          final MonitoredItem item)
        {
        if ((monitor != null)
            && (item != null))
            {
            monitor.add(item);
            }

        return (monitor);
        }


    /***********************************************************************************************
     * Add the Runnable Tasks attached to the specified Atom,
     * but not including the RootTask.
     * This assumes that such Tasks are instances of MonitoredItem.
     *
     * @param monitor
     * @param atom
     *
     * @return Vector
     */

    public static Vector addAtomMonitoredTasks(final Vector<MonitoredItem> monitor,
                                               final AtomData atom)
        {
        final Iterator<RootPlugin> iterTasks = atom.getTasks().iterator();

        while ((iterTasks != null)
            && (iterTasks.hasNext()))
            {
            final TaskPlugin pluginTask = (TaskPlugin)iterTasks.next();

            if ((monitor != null)
                && (pluginTask != null)
                && (pluginTask.isActive())
                && (pluginTask.isRunnable())
                && (pluginTask.isPublic())
                && (!(pluginTask.equals(atom.getRootTask())))
                && (pluginTask instanceof MonitoredItem))
                {
                monitor.add((MonitoredItem)pluginTask);
                }
            }

        return (monitor);
        }


    /***********************************************************************************************
     * Get the optimum format String for the Labels, given the list of MonitoredItems.
     *
     * @param monitor
     *
     * @return String
     */

    public static String getLabelFormat(final Vector<MonitoredItem> monitor)
        {
        final Iterator<MonitoredItem> iterItems;
        String strLongestName;

        strLongestName = "";

        if (monitor != null)
            {
            iterItems = monitor.iterator();

            while ((iterItems != null)
                && (iterItems.hasNext()))
                {
                final MonitoredItem monitoredItem;

                monitoredItem = iterItems.next();

                if ((monitoredItem != null)
                    && (monitoredItem.getName() != null)
                    && (monitoredItem.getName().length() > strLongestName.length()))
                    {
                    strLongestName = monitoredItem.getName();
                    }
                }
            }

        return (strLongestName);
        }


    /***********************************************************************************************
     * Construct a StatusMonitor.
     *
     * @param monitor
     * @param count
     * @param dimension
     * @param font
     * @param color
     * @param format
     *
     * @throws IndicatorException
     */

    public StatusMonitor(final Vector<MonitoredItem> monitor,
                         final int count,
                         final Dimension dimension,
                         final Font font,
                         final Color color,
                         final String format) throws IndicatorException
        {
        // Make the JPanel of lights
        super(dimension, count, font, color, format);

        if (monitor != null)
            {
            vecMonitor = monitor;
            }
        else
            {
            throw new IndicatorException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Create the labels for the lights.
     * Set up Start and Stop MouseListeners for any monitored items which are Tasks.
     *
     * @param title
     *
     * @throws IndicatorException
     */

    public void initialiseItems(final String title) throws IndicatorException
        {
        int i;

        if ((getMonitoredItems() != null)
            && (getMonitoredItems().size() > 0))
            {
            for (i = 0; i < getMonitoredItems().size(); i++)
                {
                final MonitoredItem monitoredItem;

                monitoredItem = getMonitoredItem(i);

                if (monitoredItem != null)
                    {
                    if (monitoredItem instanceof TaskData)
                        {
                        final TaskData taskData;
                        final MouseListener listenerMouse;

                        taskData = (TaskData)monitoredItem;

                        setState(i, false);
                        setLabel(i, taskData.getName());
                        setToolTip(i, true, taskData.getName() + SPACE + STATUS_RUNNING);
                        setToolTip(i, false, taskData.getName() + SPACE + STATUS_STOPPED);

                        // Provide a MouseAdapter for Task control
                        listenerMouse = new MouseAdapter()
                            {
                            public void mousePressed(final MouseEvent event)
                                {
                                if (SwingUtilities.isLeftMouseButton(event)
                                        && (!event.isShiftDown())
                                        && (!event.isControlDown())
                                        && (!event.isAltDown())
                                        && (event.getClickCount() == 1))
                                    {
                                    if ((taskData != null)
                                        && (taskData.isActive())
                                        && (taskData.isPublic())
                                        && (taskData.isRunnable())
                                        && (taskData.getParentAtom() != null))
                                        {
                                        // The Task may be started only if currently in Idle or Initialised
                                        if ((taskData.getState() == TaskState.INITIALISED)
                                            || (taskData.getState() == TaskState.IDLE))
                                            {
                                            final int intChoice;
                                            final String [] strMessages =
                                                {
                                                MSG_TASK_START_QUESTION + SPACE + taskData.getName() + QUERY
                                                };

                                            intChoice = JOptionPane.showOptionDialog(null,
                                                                                     strMessages,
                                                                                     title,
                                                                                     JOptionPane.YES_NO_OPTION,
                                                                                     JOptionPane.QUESTION_MESSAGE,
                                                                                     null,
                                                                                     null,
                                                                                     null);

                                            if (intChoice == JOptionPane.YES_OPTION)
                                                {
                                                // We don't want to change the UI occupant,
                                                // so don't use actionPerformed()
                                                MODEL_CONTROLLER.setTaskState(taskData.getParentAtom(),
                                                                              taskData,
                                                                              TaskState.RUNNING);
                                                }
                                            }
                                        }
                                    }

                                if (SwingUtilities.isLeftMouseButton(event)
                                        && (!event.isShiftDown())
                                        && (event.isControlDown())
                                        && (!event.isAltDown())
                                        && (event.getClickCount() == 1))
                                    {
                                    if ((taskData != null)
                                        && (taskData.isActive())
                                        && (taskData.isPublic())
                                        && (taskData.isRunnable())
                                        && (taskData.getParentAtom() != null))
                                        {
                                        // The Task may be stopped only if currently in Running
                                        if (taskData.getState() == TaskState.RUNNING)
                                            {
                                            final int intChoice;
                                            final String [] strMessages =
                                                {
                                                MSG_TASK_STOP_QUESTION + SPACE + taskData.getName() + QUERY
                                                };

                                            intChoice = JOptionPane.showOptionDialog(null,
                                                                                     strMessages,
                                                                                     title,
                                                                                     JOptionPane.YES_NO_OPTION,
                                                                                     JOptionPane.QUESTION_MESSAGE,
                                                                                     null,
                                                                                     null,
                                                                                     null);

                                            if (intChoice == JOptionPane.YES_OPTION)
                                                {
                                                // We don't want to change the UI occupant
                                                MODEL_CONTROLLER.setTaskState(taskData.getParentAtom(),
                                                                              taskData,
                                                                              TaskState.IDLE);
                                                }
                                            }
                                        }
                                    }
                                }
                            };

                        getLight(i).addMouseListener(listenerMouse);
                        }
                    else
                        {
                        // Non-interactive monitored Items
                        setState(i, false);
                        setLabel(i, monitoredItem.getName());
                        setToolTip(i, true, monitoredItem.getName() + SPACE + STATUS_RUNNING);
                        setToolTip(i, false, monitoredItem.getName() + SPACE + STATUS_STOPPED);
                        }
                    }
                }
            }
        else
            {
            throw new IndicatorException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Get the list of Items attached to the StatusLights.
     *
     * @return Vector
     */

    public final Vector<MonitoredItem> getMonitoredItems()
        {
        return (this.vecMonitor);
        }


    /***********************************************************************************************
     * Get the MonitoredItem attached to the specified Light.
     *
     * @param light
     *
     * @return MonitoredItem
     *
     * @throws IndicatorException
     */

    public MonitoredItem getMonitoredItem(final int light) throws IndicatorException
        {
        if ((getMonitoredItems() != null)
            && (light >= 0)
            && (light < getMonitoredItems().size()))
            {
            return (getMonitoredItems().get(light));
            }
        else
            {
            throw new IndicatorException(EXCEPTION_PARAMETER_RANGE);
            }
        }
    }


//-------------------------------------------------------------------------------------------------
// End of file
