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
//  21-06-03    LMN created file
//  07-10-03    LMN moved into the FrameworkTasks!
//  13-10-03    LMN made it work! (again)
//  18-10-04    LMN rewrote for new Task structure
//  04-04-05    LMN tidying logging
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.reports.impl.RegistryReport;
import org.lmn.fc.ui.widgets.MonitoredItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/***************************************************************************************************
 * The DataDaemon FrameworkTask.
 * This Task writes all changed Framework data to the database, on a configurable Timer.
 */

public final class DataDaemon extends TaskData
                              implements MonitoredItem
    {
    private static final long VERSION_ID = 1132988854505047172L;

    // String Resources
    private static final String STATUS_NODES = "The list shows all nodes for";

    private static final int DEFAULT_BACKUP_PERIOD = 10000;

    private Timer timerBackup;
    private SwingWorker workerBackup;

    // DataDaemon Properties
    private int intBackupPeriod;
    private boolean boolEnableBackup;


    /***********************************************************************************************
     * Construct a DataDaemon FrameworkTask.
     */

    private DataDaemon()
        {
        super(VERSION_ID, REGISTRY.getFramework());

        timerBackup = null;
        workerBackup = null;

        intBackupPeriod = DEFAULT_BACKUP_PERIOD;
        boolEnableBackup = true;
        }


    /***********************************************************************************************
     * Initialise the DataDaemon.
     * Task state changes are handled by setInstrumentState() of the parent Framework.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources
        readResources();

        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerBackup, true, SWING_WORKER_STOP_DELAY);
        workerBackup = null;

        // Set up a Timer to do the Backup
        timerBackup = new Timer(intBackupPeriod, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                LOGGER.debugTimerTick("DataDaemon");

                // Stop any existing SwingWorker
                SwingWorker.disposeWorker(workerBackup, true, SWING_WORKER_STOP_DELAY);
                workerBackup = null;

                // Prepare another thread to do the Node backup
                workerBackup = new SwingWorker(REGISTRY.getThreadGroup(),
                                               "SwingWorker DataDaemon")
                    {
                    public Object construct()
                        {
                        LOGGER.debugSwingWorker("DataDaemon: Construct SwingWorker");
                        // Get the latest Resources
                        readResources();

                        // Check to see if updates are allowed
                        if ((boolEnableBackup)
                            && (!isStopping()))
                            {
                            // Update the Registry with any changes
                            REGISTRY_MANAGER.updateRegistry();
                            }

                        // Update the Timer delay in case the RegistryModel has changed...
                        if (timerBackup != null)
                            {
                            timerBackup.setDelay(intBackupPeriod);
                            }

                        // There is no result to pass to the Event Dispatching Thread
                        return (null);
                        }

                    // Display updates occur on the Event Dispatching Thread
                    public void finished()
                        {
                        // There is nothing to do on the Event Dispatching Thread
                        }
                    };

                // When the Timer goes off, start the Thread we have prepared...
                workerBackup.start();
                }
            });

        return (true);
        }


    /***********************************************************************************************
     * Run the DataDaemon.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        if (timerBackup != null)
            {
            // Set up the UI of a RegistryReport
            try
                {
                // Remove any previous RegistryReport
                if (getUIComponent() != null)
                    {
                    getUIComponent().disposeUI();
                    setUIComponent(null);
                    }

                // Create a Node list for the Framework
                // The ResourceKey is always that of the host Framework,
                // since this is a general utility
                setUIComponent(new RegistryReport(this, REGISTRY.getFramework().getResourceKey()));
                getUIComponent().initialiseUI();

                // There is no Editor, and we are always in Browse mode
                setEditorComponent(null);
                setBrowseMode(true);
                }

            catch (ReportException exception)
                {
                handleException(exception,
                                "runTask()",
                                EventStatus.WARNING);
                }

            timerBackup.setCoalesce(false);
            timerBackup.restart();

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Park the DataDaemon in Idle.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the RegistryReport data, and stop the refresh Timer
        stopUI();

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        if (timerBackup != null)
            {
            // Make sure that the Timer has stopped running,
            // but leave the reference to it, so that runTask() can use it
            timerBackup.stop();
            }

        SwingWorker.disposeWorker(workerBackup, true, SWING_WORKER_STOP_DELAY);
        workerBackup = null;

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the DataDaemon after use.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Remove the RegistryReport
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Stop the Timer
        if (timerBackup != null)
            {
            // Make sure that the Timer has stopped running
            timerBackup.stop();
            timerBackup = null;
            }

        SwingWorker.disposeWorker(workerBackup, true, SWING_WORKER_STOP_DELAY);
        workerBackup = null;

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_NODES + SPACE + REGISTRY.getFramework().getPathname());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            // Reduce resources as far as possible
            getUIComponent().stopUI();
            }
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DataDaemon.
     *
     * <code>
     * <li>DataDaemon.Backup.Period
     * <li>DataDaemon.Enable.Backup
     * <li>DataDaemon.Enable.Debug
     * </code>
     */

    public final void readResources()
        {
        // Use the parent Framework's debug mode
        setDebugMode(REGISTRY.getFramework().getDebugMode());

        // getResourceKey() returns '<Framework>.DataDaemon.'
        boolEnableBackup = REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_BACKUP);
        intBackupPeriod = 1000 * REGISTRY.getIntegerProperty(getResourceKey() + KEY_BACKUP_PERIOD);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
