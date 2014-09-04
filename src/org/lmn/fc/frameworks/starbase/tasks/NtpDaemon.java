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
//  12-12-03    LMN created file from FtpTester
//  14-10-04    LMN adding NTP functionality!
//  04-04-05    LMN tidying logging
//  08-08-06    LMN changed for new structure
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.net.ntp.NtpConnection;
import org.lmn.fc.common.net.ntp.NtpData;
import org.lmn.fc.common.net.ntp.TimeManager;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.ui.ntp.NtpUI;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.widgets.MonitoredItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * The Framework NtpDaemon.
 */

public final class NtpDaemon extends TaskData
                             implements MonitoredItem
    {
    // String Resources
    private static final String LOG_TIME_SET_OK                 = "Time set successfully";
    private static final String LOG_TIME_SET_LINK_ERROR         = "Time not set because of UnsatisfiedLinkError";
    private static final String LOG_TIME_SET_TIMEMANAGER_ERROR  = "Time not set because of TimeManagerException";
    private static final String LOG_TIME_SET_DISABLED           = "Time not set at request of User";
    private static final String LOG_TIME_NO_DATA                = "Time not set because server did not provide data";
    private static final String LOG_TIME_SET_SOCKET_ERROR       = "Time not set because of SocketException";
    private static final String LOG_TIME_SET_UNKNOWN_HOST       = "Time not set because of UnknownHostException";
    private static final String LOG_TIME_SET_IO_ERROR           = "Time not set because of IOException";
    private static final String LOG_TIME_SET_NULL               = "Time not set because of Null";

    private static final String STATUS_NTP_LOG                  = "Showing the NTP log for";

    private static final String EXCEPTION_UNKNOWN_SERVER        = "Unknown Time Server";
    private static final String EXCEPTION_SOCKET_ERROR          = "TCP Socket Error for Time Server";
    private static final String EXCEPTION_IO                    = "IOException for Time Server";
    private static final String EXCEPTION_NULL                  = "NullPointerException for Time Server";

    private Timer timerNTP;
    private SwingWorker workerNTP;

    // NtpDaemon Properties
    private String urlServerDefault;
    private String urlServer1;
    private String urlServer2;
    private String urlServer3;
    private int intUpdatePeriod;
    private boolean boolEnableNTP;
    private boolean boolEnableSetTime;
    private boolean boolEnableTrace;
    private FontInterface pluginFont;
    private ColourInterface pluginColour;


    /***********************************************************************************************
     * Add a null LogEntry, but without the sorting index column.
     *
     * @param logentry
     * @param server
     * @param status
     */

    private static void setNullLogEntry(final Vector<Object> logentry,
                                        final String server,
                                        final String status)
        {
        logentry.add(null);
        logentry.add(false);
        logentry.add(null);
        logentry.add(null);
        logentry.add(server);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        logentry.add(status);
        logentry.add(null);
        logentry.add(null);
        logentry.add(null);
        }


    /***********************************************************************************************
     * Construct an NtpDaemon.
     */

    private NtpDaemon()
        {
        super(-3114337503350625201L, REGISTRY.getFramework());

        timerNTP = null;
        workerNTP = null;

        // Initialise the Resources
        urlServerDefault = NtpConnection.DEFAULT_NTP_SERVER;
        urlServer1 = NtpConnection.DEFAULT_NTP_SERVER;
        urlServer2 = NtpConnection.DEFAULT_NTP_SERVER;
        urlServer3 = NtpConnection.DEFAULT_NTP_SERVER;

        boolEnableNTP = true;
        boolEnableSetTime = false;
        boolEnableTrace = false;
        intUpdatePeriod = 10;
        setDebugMode(false);
        }


    /***********************************************************************************************
     * Initialise the NtpDaemon.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources
        readResources();

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        // Create an NtpUI and initialise it
        setUIComponent(new NtpUI(this, pluginFont, pluginColour));
        getUIComponent().setDebug(getDebugMode());
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerNTP, true, SWING_WORKER_STOP_DELAY);
        workerNTP = null;

        // Set up a Timer to do the NTP
        timerNTP = new Timer(intUpdatePeriod * (int) ChronosHelper.SECOND_MILLISECONDS,
                             new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                LOGGER.debugTimerTick(getName());
                doNtp();
                }
            });

        return (true);
        }


    /***********************************************************************************************
     * Run the NtpDaemon.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        if (timerNTP != null)
            {
            // Start the NTP Timer
            timerNTP.setCoalesce(false);
            timerNTP.restart();

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Park the NtpDaemon in Idle.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean idleTask()
        {
        stopUI();

        if (timerNTP != null)
            {
            // Make sure that the Timer has stopped running,
            // but leave the reference to it, so that runTask() can use it
            timerNTP.stop();
            }

        SwingWorker.disposeWorker(workerNTP, true, SWING_WORKER_STOP_DELAY);
        workerNTP = null;

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the NtpDaemon after use.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Remove the NtpLog
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        // Stop the Timer
        if (timerNTP != null)
            {
            // Make sure that the Timer has stopped running
            timerNTP.stop();
            timerNTP = null;
            }

        SwingWorker.disposeWorker(workerNTP, true, SWING_WORKER_STOP_DELAY);
        workerNTP = null;

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        // The UIComponent is NtpUI, which holds a JTabbedPane
        // The first Tab should be the NTP Log, which calls its runUI()
        // when created. Other Tabs call runUI() via the Tab Listener.
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_NTP_LOG + SPACE + REGISTRY.getFramework().getPathname());
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
     * Do the Ntp operations.
     */

    private void doNtp()
        {
        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerNTP, true, SWING_WORKER_STOP_DELAY);
        workerNTP = null;

        // Prepare another thread to do the NTP
        workerNTP = new SwingWorker(REGISTRY.getThreadGroup(),
                                    "SwingWorker NtpDaemon")
            {
            public Object construct()
                {
                NtpConnection ntpConnection;
                NtpData ntpData;
                final Vector<String> vecServers;
                final Iterator iterServers;
                Vector<Object> vecLogEntry;
                GregorianCalendar calNTP;
                Calendar calTimeStamp;
                boolean boolTimeValid;
                ImageIcon imageIcon;

                LOGGER.debugSwingWorker(getName());

                // Get the latest Resources
                readResources();

                // Check to see if NTP is allowed
                if ((boolEnableNTP)
                    && (!isStopping()))
                    {
                    // Organise all available Time servers
                    vecServers = new Vector<String>(4);
                    vecServers.add(urlServerDefault);
                    vecServers.add(urlServer1);
                    vecServers.add(urlServer2);
                    vecServers.add(urlServer3);

                    // No Time yet!
                    boolTimeValid = false;

                    // Cycle through the available servers if we get a timeout etc.
                    iterServers = vecServers.iterator();

                    while ((iterServers.hasNext())
                        && (!boolTimeValid)
                        && (!isStopping()))
                        {
                        final String strServerName = (String) iterServers.next();

                        if ((strServerName != null)
                            && (!EMPTY_STRING.equals(strServerName.trim())))
                            {
                            // Prepare a Log entry for each server attempt
                            vecLogEntry = new Vector<Object>(20);
                            ntpConnection = null;
                            ntpData = null;
                            calNTP = Chronos.getCalendarDateNow();

                            try
                                {
                                LOGGER.debug("Trying to connect to NTP server " + strServerName);

                                // Attempt to get an NtpConnection and read the NtpData
                                ntpConnection = new NtpConnection(InetAddress.getByName(strServerName));
                                ntpData = ntpConnection.getNtpData();

                                if ((ntpData != null)
                                    && (ntpData.getServerAddress() != null)
                                    && (!isStopping()))
                                    {
                                    // Set the Local Time if required
                                    if (boolEnableSetTime)
                                        {
                                        LOGGER.debug("NTP attempting to set the time...");

                                        final TimeManager timeManager;

                                        // Call the DLL via the TimeManager
                                        // Ideally check for the host Operating System...
                                        // Save the Time wrapped in a Date
                                        calNTP = new GregorianCalendar();
                                        calNTP.setTimeInMillis(Chronos.getSystemTime() + ntpData.getOffset());

                                        // Getting an instance of the LocalTimeManager tries to load the settime DLL
                                        timeManager = TimeManager.getInstance();

//                                        if (timeManager.isLoaded())
//                                            {
//                                            timeManager.setTime(calNTP);
//                                            }

                                        // Record the valid Time details in the Log
                                        imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.INFO.getIconFilename());
                                        vecLogEntry.add(imageIcon);
                                        vecLogEntry.add(true);
                                        vecLogEntry.add(ChronosHelper.toDateString(calNTP));
                                        vecLogEntry.add(ChronosHelper.toTimeString(calNTP));
                                        vecLogEntry.add(strServerName);
                                        vecLogEntry.add(ntpData.getServerAddress().toString());
                                        vecLogEntry.add(Integer.toString(ntpData.getVersionNumber()));
                                        vecLogEntry.add(Long.toString(ntpData.getOffset()));
                                        vecLogEntry.add(Long.toString(ntpData.getRoundTripDelay()));
                                        vecLogEntry.add(Integer.toString(ntpData.getStratum()));
                                        vecLogEntry.add(Double.toString(ntpData.getPrecision()));
                                        vecLogEntry.add(LOG_TIME_SET_OK);

                                        calTimeStamp = ntpData.getReferenceTimeStamp().toCalendar();
                                        vecLogEntry.add(ChronosHelper.toDateString(calTimeStamp));
                                        vecLogEntry.add(ChronosHelper.toTimeString(calTimeStamp));
                                        vecLogEntry.add(Double.toString(ntpData.getReferenceTimeStamp().getFractionalPart()));
                                        }
                                    else
                                        {
                                        LOGGER.debug("NTP logging the time...");

                                        // Save the Time wrapped in a Date
                                        calNTP = new GregorianCalendar();
                                        calNTP.setTimeInMillis(Chronos.getSystemTime() + ntpData.getOffset());

                                        // Record the valid Time details in the Log
                                        imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());
                                        vecLogEntry.add(imageIcon);
                                        vecLogEntry.add(false);
                                        vecLogEntry.add(ChronosHelper.toDateString(calNTP));
                                        vecLogEntry.add(ChronosHelper.toTimeString(calNTP));
                                        vecLogEntry.add(strServerName);
                                        vecLogEntry.add(ntpData.getServerAddress().toString());
                                        vecLogEntry.add(Integer.toString(ntpData.getVersionNumber()));
                                        vecLogEntry.add(Long.toString(ntpData.getOffset()));
                                        vecLogEntry.add(Long.toString(ntpData.getRoundTripDelay()));
                                        vecLogEntry.add(Integer.toString(ntpData.getStratum()));
                                        vecLogEntry.add(Double.toString(ntpData.getPrecision()));
                                        vecLogEntry.add(LOG_TIME_SET_DISABLED);

                                        calTimeStamp = ntpData.getReferenceTimeStamp().toCalendar();
                                        vecLogEntry.add(ChronosHelper.toDateString(calTimeStamp));
                                        vecLogEntry.add(ChronosHelper.toTimeString(calTimeStamp));
                                        vecLogEntry.add(Double.toString(ntpData.getReferenceTimeStamp().getFractionalPart()));
                                        }

                                    // Time to leave!
                                    boolTimeValid = true;
                                    }
                                else
                                    {
                                    // We cannot read a valid NtpData for this server
                                    setNullLogEntry(vecLogEntry,
                                                    strServerName,
                                                    LOG_TIME_NO_DATA);
                                    }

                                // Add this entry to the Log
                                getNtpUI().logger(vecLogEntry);
                                }

                            catch (UnsatisfiedLinkError exception)
                                {
                                ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());

                                // We can't find the native-language definition of a method declared native
                                // The Time was valid, so record it in the Log
                                imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.WARNING.getIconFilename());
                                vecLogEntry.add(imageIcon);
                                vecLogEntry.add(false);
                                vecLogEntry.add(ChronosHelper.toDateString(calNTP));
                                vecLogEntry.add(ChronosHelper.toTimeString(calNTP));
                                vecLogEntry.add(strServerName);
                                vecLogEntry.add(ntpData.getServerAddress().toString());
                                vecLogEntry.add(Integer.toString(ntpData.getVersionNumber()));
                                vecLogEntry.add(Long.toString(ntpData.getOffset()));
                                vecLogEntry.add(Long.toString(ntpData.getRoundTripDelay()));
                                vecLogEntry.add(Integer.toString(ntpData.getStratum()));
                                vecLogEntry.add(Double.toString(ntpData.getPrecision()));
                                vecLogEntry.add(LOG_TIME_SET_LINK_ERROR);

                                calTimeStamp = ntpData.getReferenceTimeStamp().toCalendar();
                                vecLogEntry.add(ChronosHelper.toDateString(calTimeStamp));
                                vecLogEntry.add(ChronosHelper.toTimeString(calTimeStamp));
                                vecLogEntry.add(Double.toString(ntpData.getReferenceTimeStamp().getFractionalPart()));

                                getNtpUI().logger(vecLogEntry);
                                }

                            catch (ClassNotFoundException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_TIMEMANAGER_ERROR);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                LOG_TIME_SET_TIMEMANAGER_ERROR + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (InstantiationException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_TIMEMANAGER_ERROR);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                LOG_TIME_SET_TIMEMANAGER_ERROR + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (IllegalAccessException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_TIMEMANAGER_ERROR);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                LOG_TIME_SET_TIMEMANAGER_ERROR + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (SocketTimeoutException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_SOCKET_ERROR);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                EXCEPTION_SOCKET_ERROR + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (SocketException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_SOCKET_ERROR);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                EXCEPTION_SOCKET_ERROR + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (UnknownHostException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_UNKNOWN_HOST);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                EXCEPTION_UNKNOWN_SERVER + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (IOException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_IO_ERROR);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                EXCEPTION_IO + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            catch (NullPointerException exception)
                                {
                                setNullLogEntry(vecLogEntry,
                                                strServerName,
                                                LOG_TIME_SET_NULL);
                                getNtpUI().logger(vecLogEntry);

                                handleException(exception,
                                                EXCEPTION_NULL + SPACE + strServerName,
                                                EventStatus.SILENT);
                                }

                            finally
                                {
                                // Always close the connection for this server
                                if (ntpConnection != null)
                                    {
                                    ntpConnection.close();
                                    }
                                }
                            }
                        }

                    // Update the Timer delay in case the RegistryModel has changed...
                    timerNTP.setDelay(intUpdatePeriod * (int) ChronosHelper.SECOND_MILLISECONDS);
                    }

                // There is no result to pass to the Event Dispatching Thread
                return (null);
                }

            // Display updates occur on the Event Dispatching Thread
            public void finished()
                {
                // There is nothing to do on the Event Dispatching Thread
                // ToDo One day this will update the system clock using JNI...
                }
            };

        // When the Timer goes off, start the Thread we have prepared...
        workerNTP.start();
        }


    /***********************************************************************************************
     * A convenience method to get the NTP UI.
     *
     * @return NtpUI
     */

    private NtpUI getNtpUI()
        {
        return ((NtpUI)getUIComponent());
        }


    /***********************************************************************************************
     * Read all the Resources required by the NtpDaemon.
     */

    public final void readResources()
        {
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));

        intUpdatePeriod = REGISTRY.getIntegerProperty(getResourceKey() + KEY_NTP_PERIOD_UPDATE);

        // getResourceKey() returns '<Framework>.NtpDaemon.'
        urlServerDefault = REGISTRY.getStringProperty(getResourceKey() + KEY_NTP_SERVER_DEFAULT);
        urlServer1 = REGISTRY.getStringProperty(getResourceKey() + KEY_NTP_SERVER_1);
        urlServer2 = REGISTRY.getStringProperty(getResourceKey() + KEY_NTP_SERVER_2);
        urlServer3 = REGISTRY.getStringProperty(getResourceKey() + KEY_NTP_SERVER_3);

        boolEnableNTP = REGISTRY.getBooleanProperty(getResourceKey() + KEY_NTP_ENABLE_NTP);
        boolEnableSetTime = REGISTRY.getBooleanProperty(getResourceKey() + KEY_NTP_ENABLE_SET_TIME);
        boolEnableTrace = REGISTRY.getBooleanProperty(getResourceKey() + KEY_NTP_ENABLE_TRACE);

        // Use the parent Framework's Colour and Font
        pluginFont = (FontInterface)REGISTRY.getProperty(REGISTRY.getFrameworkResourceKey() + KEY_FONT_LABEL);
        pluginColour = (ColourInterface)REGISTRY.getProperty(REGISTRY.getFrameworkResourceKey() + KEY_COLOUR_TEXT);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
