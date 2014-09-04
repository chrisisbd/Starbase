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
//  28-10-04    LMN created file
//  04-04-05    LMN tidying logging
//  07-08-06    LMN converting for RXTX etc.
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.GpsException;
import org.lmn.fc.common.support.gps.GpsReceiverInterface;
import org.lmn.fc.common.support.gps.impl.GarminGpsReceiver;
import org.lmn.fc.common.support.gps.impl.SatelliteData;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.TimeoutHelper;
import org.lmn.fc.frameworks.starbase.ui.gps.GpsUI;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.panels.MapUIComponent;
import org.lmn.fc.ui.widgets.MonitoredItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;


/***************************************************************************************************
 * The Framework GpsDaemon.
 */

public final class GpsDaemon extends TaskData
                             implements MonitoredItem,
                                        FrameworkStrings
    {
    // String Resources
    private static final String STATUS_GPS_LOG              = "Showing the RS232GpsReceiver log for";
    private static final String EXCEPTION_GPS_RECEIVER      = "RS232GpsReceiver failure";
    private static final String EXCEPTION_SERIAL_COMMS_DLL  = "Unable to load the Serial Communications support";
    private static final String HTML_NO_DATA                = "<html><font color=red>No Data</font></html>";

    private static final int GPS_RETRIES = 3;

    private GpsReceiverInterface gpsReceiver;
    private Timer timerGPS;
    private SwingWorker workerGPS;

    // GpsDaemon Properties
    private String strReceiverClassName;
    private String strReceiverType;
    private String strPortOwner;
    private String strPortName;
    private int intBaudrate;
    private int intDatabits;
    private int intStopbits;
    private int intParity;
    private String strFlowControl;
    private int intUpdatePeriodMillisec;
    private int intCapturePeriod;
    private boolean boolEnableGPS;
    private FontInterface pluginFont;
    private ColourInterface pluginColour;


    /***********************************************************************************************
     * Debug the RS232GpsReceiver fix information.
     *
     * @param receiver
     */

    private static void showFix(final GpsReceiverInterface receiver)
        {
        LOGGER.debug("[ReceiverName=" + receiver.getReceiverName() + "]");
        LOGGER.debug("[DateOfLastUpdate=" + receiver.getDateOfLastUpdate() + "]");
        LOGGER.debug("[DateOfFix=" + receiver.getDateOfFix().toString() + "]");
        LOGGER.debug("[TimeOfFix=" + receiver.getTimeOfFix().toString() + "]");
        LOGGER.debug("[Latitude=" + receiver.getLatitude().toString() + "]");
        LOGGER.debug("[Longitude=" + receiver.getLongitude().toString() + "]");
        LOGGER.debug("[SpeedKnots=" + receiver.getSpeedKnots() + "]");
        LOGGER.debug("[Course=" + receiver.getCourse() + "]");
        LOGGER.debug("[MagneticVariation=" + receiver.getMagneticVariation() + "]");
        LOGGER.debug("[DataQuality=" + receiver.getDataQuality() + "]");
        LOGGER.debug("[AltitudeASL=" + receiver.getAltitudeASL() + "]");
        LOGGER.debug("[GeoidAltitude=" + receiver.getGeoidAltitude() + "]");
        LOGGER.debug("[FixMode=" + receiver.getFixMode() + "]");
        LOGGER.debug("[FixType=" + receiver.getFixType() + "]");
        LOGGER.debug("[PDOP=" + receiver.getPDOP() + "]");
        LOGGER.debug("[HDOP=" + receiver.getHDOP() + "]");
        LOGGER.debug("[VDOP=" + receiver.getVDOP() + "]");

        LOGGER.debug("[SatellitesInUse=" + receiver.getSatellitesInUse() + "]");
        final Enumeration enumInUse = receiver.getSatellitesInUseData();

        while (enumInUse.hasMoreElements())
            {
            LOGGER.debug("[satellite=" + enumInUse.nextElement() + "]");
            }

        if (receiver.getSatellitesInView() > 0)
            {
            LOGGER.debug("[SatellitesInView=" + receiver.getSatellitesInView() + "]");
            final Enumeration enumGPGSV = receiver.getSatellitesInViewData();
            SatelliteData GPGSVsatellite;

            while (enumGPGSV.hasMoreElements())
                {
                GPGSVsatellite = (SatelliteData)enumGPGSV.nextElement();

                LOGGER.debug("[satelliteinview="
                     + GPGSVsatellite.getSatellitePRN() + ", "
                     + GPGSVsatellite.getElevation() + ", "
                     + GPGSVsatellite.getAzimuth() + ", "
                     + GPGSVsatellite.getSNR() + "] ");
                }
            }

        final Enumeration enumSupport = receiver.getNMEASentences();

        while (enumSupport.hasMoreElements())
            {
            LOGGER.debug("[NMEAsentence=" + enumSupport.nextElement() + "]");
            }
        }


    /***********************************************************************************************
     * Construct a GpsDaemon Task.
     */

    private GpsDaemon()
        {
        super(-4633059546763466847L);

        gpsReceiver = null;
        timerGPS = null;
        workerGPS = null;

        // Initialise the Resources
        strReceiverClassName = EMPTY_STRING;
        strReceiverType = EMPTY_STRING;
        strPortOwner = EMPTY_STRING;
        strPortName = PORT_COM1;
        intBaudrate = DEFAULT_BAUDRATE;
        intDatabits = DEFAULT_DATABITS;
        intStopbits = DEFAULT_STOPBITS;
        intParity = DEFAULT_PARITY;
        strFlowControl = FLOWCONTROL_NONE;
        intUpdatePeriodMillisec = (int) ChronosHelper.SECOND_MILLISECONDS;
        intCapturePeriod = 10;
        boolEnableGPS = true;
        setDebugMode(false);
        }


    /***********************************************************************************************
     * Initialise the GpsDaemon.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        // Get the latest Resources
        readResources();

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        // Create a GpsUI and initialise it
        setUIComponent(new GpsUI(this, pluginFont, pluginColour));
        getUIComponent().setDebug(getDebugMode());
        getUIComponent().initialiseUI();

        // There is no Editor, and we are always in Browse mode
        setEditorComponent(null);
        setBrowseMode(true);

        // Stop any existing Receiver
        if (gpsReceiver != null)
            {
            gpsReceiver.shutdown();
            }

        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerGPS, true, SWING_WORKER_STOP_DELAY);
        workerGPS = null;

        // Set up a Timer to read the GpsReceiver
        timerGPS = new Timer(intUpdatePeriodMillisec,
                             new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                LOGGER.debugTimerTick(getName());
                doGPS();
                }
            });

        return (true);
        }


    /***********************************************************************************************
     * Run the GpsDaemon.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        if (timerGPS != null)
            {
            // Start the RS232GpsReceiver Timer
            timerGPS.setCoalesce(false);
            timerGPS.restart();

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Park the GpsDaemon in Idle.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean idleTask()
        {
        LOGGER.debugNavigation("GpsDaemon.idleTask() START");

        stopUI();

        // Stop the Receiver
        if (gpsReceiver != null)
            {
            gpsReceiver.shutdown();
            }

        if (timerGPS != null)
            {
            // Make sure that the Timer has stopped running,
            // but leave the reference to it, so that runTask() can use it
            timerGPS.stop();
            }

        SwingWorker.disposeWorker(workerGPS, true, SWING_WORKER_STOP_DELAY);
        workerGPS = null;

        LOGGER.debugNavigation("GpsDaemon.idleTask() END");

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the GpsDaemon after use.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        LOGGER.debugNavigation("GpsDaemon.shutdownTask() START");

        stopUI();

        // Remove the GpsUI
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        // Stop the Receiver
        if (gpsReceiver != null)
            {
            gpsReceiver.shutdown();
            }

        // Stop the Timer
        if (timerGPS != null)
            {
            // Make sure that the Timer has stopped running
            timerGPS.stop();
            timerGPS = null;
            }

        SwingWorker.disposeWorker(workerGPS, true, SWING_WORKER_STOP_DELAY);
        workerGPS = null;

        LOGGER.debugNavigation("GpsDaemon.shutdownTask() END");

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        // The first Tab should be the GPS Log, which calls its runUI()
        // when created. Other Tabs call runUI() via the Tab Listener.
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_GPS_LOG + SPACE + REGISTRY.getFramework().getPathname());
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
     * Do the GPS operations.
     */

    private void doGPS()
        {
        // Stop any existing Receiver
        if (gpsReceiver != null)
            {
            gpsReceiver.shutdown();
            }

        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerGPS, true, SWING_WORKER_STOP_DELAY);
        workerGPS = null;

        // Prepare another thread to do the RS232GpsReceiver
        workerGPS = new SwingWorker(REGISTRY.getThreadGroup(),
                                    "SwingWorker GpsDaemon")
            {
            public Object construct()
                {
                final Vector<Object> vecLogEntry;
                boolean boolGPSValid;   // True if a fix was received Ok
                int intRetryCounter;    // Count attempts to get a fix

                LOGGER.debugSwingWorker(getName());

                // Get the latest Resources
                readResources();
                boolGPSValid = false;

                // Check to see if RS232GpsReceiver is allowed
                if ((boolEnableGPS)
                    && (!isStopping()))
                    {
                    try
                        {
                        // Stop any existing Receiver
                        if (gpsReceiver != null)
                            {
                            gpsReceiver.shutdown();
                            }

                        // Construct an instance of a GpsReceiver
                        // Make a new RS232GpsReceiver receiver every time
                        gpsReceiver = new GarminGpsReceiver(strReceiverType,
                                                            strPortOwner,
                                                            strPortName,
                                                            intBaudrate,
                                                            intDatabits,
                                                            intStopbits,
                                                            intParity,
                                                            strFlowControl,
                                                            getDebugMode());

                        if ((gpsReceiver != null)
                            && (gpsReceiver.findPort(strPortName))
                            && (!isStopping()))
                            {
                            final ImageIcon imageIcon;

                            // Try up to GPS_RETRIES times to get a valid RS232GpsReceiver fix
                            intRetryCounter = 0;
                            boolGPSValid = false;
                            vecLogEntry = new Vector<Object>(20);

                            while((!boolGPSValid)
                                && (intRetryCounter < GPS_RETRIES)
                                && (!isStopping()))
                                {
                                LOGGER.debug("GpsDaemon.decodeNMEA attempt " + intRetryCounter);
                                boolGPSValid = gpsReceiver.decodeNMEA(intCapturePeriod);
                                intRetryCounter++;
                                }

                            // Get the RS232GpsReceiver data and update our location if all is well
                            if ((boolGPSValid)
                                && (gpsReceiver != null)
                                && (!isStopping()))
                                {
                                showFix(gpsReceiver);

                                // Prepare a Log entry for each valid RS232GpsReceiver fix
                                imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());
                                vecLogEntry.add(imageIcon);
                                vecLogEntry.add(gpsReceiver.getDateOfFix().toString());
                                vecLogEntry.add(ChronosHelper.parseTime(gpsReceiver.getTimeOfFix().toString(),
                                                                        Chronos.getSystemDateNow()));
                                vecLogEntry.add(gpsReceiver.getLongitude().toString());
                                vecLogEntry.add(gpsReceiver.getLatitude().toString());
                                vecLogEntry.add(Double.toString(gpsReceiver.getAltitudeASL()));
                                vecLogEntry.add(Double.toString(gpsReceiver.getGeoidAltitude()));
                                vecLogEntry.add(Double.toString(gpsReceiver.getMagneticVariation()));
                                vecLogEntry.add(Double.toString(gpsReceiver.getHDOP()));
                                vecLogEntry.add(Double.toString(gpsReceiver.getVDOP()));
                                vecLogEntry.add(Integer.toString(gpsReceiver.getFixType()));
                                vecLogEntry.add(Integer.toString(gpsReceiver.getSatellitesInUse()));
                                vecLogEntry.add(EMPTY_STRING);
                                }
                            else
                                {
                                // Log any errors
                                imageIcon = RegistryModelUtilities.getCommonIcon(EventStatus.WARNING.getIconFilename());
                                vecLogEntry.add(imageIcon);
                                vecLogEntry.add(ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
                                vecLogEntry.add(ChronosHelper.toTimeString(Chronos.getCalendarTimeNow()));
                                vecLogEntry.add(HTML_NO_DATA);
                                vecLogEntry.add(HTML_NO_DATA);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                vecLogEntry.add(EMPTY_STRING);
                                }

                            // Add this entry to the Log
                            getGpsUI().logger(vecLogEntry);
                            }
                        else
                            {
                            LOGGER.log("The GPS Receiver Port is not working");
                            }
                        }

                    catch (GpsException exception)
                        {
                        handleException(exception,
                                        EXCEPTION_GPS_RECEIVER,
                                        EventStatus.SILENT);
                        }

                    catch (UnsatisfiedLinkError error)
                        {
                        ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
                        handleException(new GpsException(error.getMessage()),
                                        EXCEPTION_SERIAL_COMMS_DLL,
                                        EventStatus.SILENT);
                        }

                    finally
                        {
                        // Make sure that the Receiver thread has stopped!
                        if (gpsReceiver != null)
                            {
                            gpsReceiver.shutdown();
                            }
                        }

                    // Update the Timer delay in case the RegistryModel has changed...
                    timerGPS.setDelay(intUpdatePeriodMillisec);
                    }

                // Is the GPS fix valid?
                return (boolGPSValid);
                }

            // Display updates occur on the Event Dispatching Thread
            public void finished()
                {
                if ((SwingUtilities.isEventDispatchThread())
                    && (!isStopping()))
                    {
                    // Finally, update the Framework!
                    if (gpsReceiver != null)
                        {
                        // Make really sure that the Receiver thread has stopped!
                        gpsReceiver.shutdown();

                        // Check that we are allowed to update the Framework directly
                        // and that it was a valid GPS fix
                        if ((REGISTRY.getFramework().isAutoUpdate())
                            &&((Boolean) get()))
                            {
                            // Record the change in the Framework location...
                            REGISTRY.getFramework().setLongitude(gpsReceiver.getLongitude());
                            REGISTRY.getFramework().setLatitude(gpsReceiver.getLatitude());
                            REGISTRY.getFramework().setHASL(gpsReceiver.getAltitudeASL());

                            // ...and update the PointOfInterest on the GPS map
                            ((MapUIComponent)getGpsUI().getGpsMap()).repaint();
                            }
                        }
                    }
                }
            };

        // When the Timer goes off, start the Thread we have prepared...
        workerGPS.start();
        }


    /***********************************************************************************************
     * A convenience method to get the GPS UI.
     *
     * @return GpsUI
     */

    private GpsUI getGpsUI()
        {
        return ((GpsUI)getUIComponent());
        }


    /***********************************************************************************************
     * Read all the Resources required by the GpsDaemon.
     * Note that the Update Period <b>must</b> be significantly longer than the Capture Period!
     */

    public final void readResources()
        {
        // getResourceKey() returns 'GpsDaemon.'
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));

        intUpdatePeriodMillisec = REGISTRY.getIntegerProperty(getResourceKey() + KEY_GPS_PERIOD_UPDATE)
                                                        * (int) ChronosHelper.SECOND_MILLISECONDS;
        // Trap the careless use of the Update Period
        if ((intUpdatePeriodMillisec < ChronosHelper.SECOND_MILLISECONDS)
            || (intUpdatePeriodMillisec > TimeoutHelper.TIMEOUT_MAX_MILLISECONDS))
            {
            intUpdatePeriodMillisec = (int) ChronosHelper.SECOND_MILLISECONDS;
            LOGGER.error("GpsDaemon: Update.Period is set incorrectly. Using a value of 1sec.");
            }

        // GPS Receiver
        strReceiverClassName = REGISTRY.getStringProperty(getResourceKey() + KEY_GPS_RECEIVER_CLASS_NAME);
        strReceiverType = REGISTRY.getStringProperty(getResourceKey() + KEY_GPS_RECEIVER_TYPE);
        boolEnableGPS = REGISTRY.getBooleanProperty(getResourceKey() + KEY_GPS_ENABLE_GPS_RECEIVER);
        intCapturePeriod = REGISTRY.getIntegerProperty(getResourceKey() + KEY_GPS_PERIOD_CAPTURE);

        // Serial Port
        strPortOwner = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_OWNER);
        strPortName = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_NAME);
        intBaudrate = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_BAUDRATE);
        intDatabits = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_DATA_BITS);
        intStopbits = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_STOP_BITS);
        intParity = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_PARITY);
        strFlowControl = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_FLOW_CONTROL);

        // ToDo validate Update >> Capture

        // Use the parent Framework's Colour and Font
        pluginFont = (FontInterface)REGISTRY.getProperty(REGISTRY.getFrameworkResourceKey() + KEY_FONT_LABEL);
        pluginColour = (ColourInterface)REGISTRY.getProperty(REGISTRY.getFrameworkResourceKey() + KEY_COLOUR_TEXT);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
