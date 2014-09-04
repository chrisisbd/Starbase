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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.receiver;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.exceptions.GpsException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsInstrumentReceiverInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsReceiverHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.model.resources.PropertyPlugin;

import java.io.IOException;
import java.util.TooManyListenersException;

//--------------------------------------------------------------------------------------------------
// The following NMEA sentences are required to produce the full data displayed
//
//  $GPGGA  Global Positioning System Fix Data
//  $GPGSA  Gps DOP and Active Satellites
//  $GPRMC  Recommended minimum specific Gps/Transit data
//  $GPGSV  Gps Satellites in view
//
//  See:    http://www.nmea.org
//          http://home.mira.net/~gnb/gps/nmea.html
//          http://www.garmin.com/products/gps35/spec.html
//
/***************************************************************************************************
 * Read a RS232 GpsReceiver device to determine a location.
 */

public final class RS232GpsReceiver extends AbstractNMEAGpsReceiver
                                    implements GpsInstrumentReceiverInterface,
                                               SerialPortEventListener,
                                               FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkSingletons,
                                               ResourceKeys
    {
    private static final int SLEEP_WAIT_MILLIS = 1000;
    private static final int BUFFER_SIZE = 4000;

    private StringBuffer readBuffer;

    // The GPS RS232 port
    private SerialPort serialPort;

    // RS232GpsReceiver Resources
    private String strPortOwner;
    private String strPortName;
    private int intBaudrate;
    private int intDatabits;
    private int intStopbits;
    private int intParity;
    private String strFlowControl;


    /***********************************************************************************************
     * Construct a RS232GpsReceiver.
     *
     * @param dao
     * @param rxtype
     * @param resourcekey
     *
     * @throws GpsException
     */

    public RS232GpsReceiver(final ObservatoryInstrumentDAOInterface dao,
                            final String rxtype,
                            final String resourcekey) throws GpsException
        {
        super(dao, rxtype, resourcekey);

        this.readBuffer = new StringBuffer();
        this.serialPort = null;

        this.strPortOwner = EMPTY_STRING;
        this.strPortName = PORT_COM1;
        this.intBaudrate = StreamUtilities.DEFAULT_BAUDRATE;
        this.intDatabits = StreamUtilities.DEFAULT_DATABITS;
        this.intStopbits = StreamUtilities.DEFAULT_STOPBITS;
        this.intParity = StreamUtilities.DEFAULT_PARITY;
        this.strFlowControl = StreamUtilities.FLOWCONTROL_NONE;
        }


    /***********************************************************************************************
     * Initialise the GPS Receiver.
     *
     * @return boolean
     */

    public boolean initialise()
        {
        final boolean boolSuccess;

        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "RS232GpsReceiver.initialise()");

        initialiseNMEAData();

        // Clear the input buffer used by the serial port every time
        this.readBuffer = new StringBuffer();

        // Ensure that we get the port configuration...
        readResources();

        try
            {
            // First find and open the serial port
            this.serialPort = StreamUtilities.findAndOpenSerialPort(strPortOwner,
                                                                    strPortName,
                                                                    intBaudrate,
                                                                    intDatabits,
                                                                    intStopbits,
                                                                    intParity,
                                                                    strFlowControl);
            // Set up to receive events, errors etc.
            if (getSerialPort() != null)
                {
                // Everything is working, so reveal the underlying Streams
                if ((getHostDAO() != null)
                    && (getHostDAO().getPort() != null))
                    {
                    if (getHostDAO().getPort().getRxStream() != null)
                        {
                        getHostDAO().getPort().getRxStream().setUnderlyingRxStream(getSerialPort().getInputStream());
                        }

                    if (getHostDAO().getPort().getTxStream() != null)
                        {
                        getHostDAO().getPort().getTxStream().setUnderlyingTxStream(getSerialPort().getOutputStream());
                        }
                    }

                // Activate the SerialPortEventListener!
                // Note that we can only have *one* Listener...
                getSerialPort().addEventListener(this);
                }
            }

        catch (TooManyListenersException exception)
            {
            throw new GpsException("RS232GpsReceiver.initialise()", exception);
            }

        catch (IOException exception)
            {
            throw new GpsException("RS232GpsReceiver.initialise()", exception);
            }

        // Mark this Receiver as initialised, if successful
        boolSuccess = (getSerialPort() != null);

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Start the Gps Receiver, by allowing data into the RxStream.
     *
     * @return boolean
     */

    public boolean start()
        {
        boolean boolSuccess;

        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "RS232GpsReceiver.start()");

        boolSuccess = true;

        // Clear the input buffer used by the serial port every time
        // The SerialEvent handler cannot write while the Rx is stopped
        this.readBuffer = new StringBuffer("START");

        // Allow things to happen...
        setStarted(boolSuccess);

        // This will fire serialEvent() when data are waiting
        if (getSerialPort() != null)
            {
            getSerialPort().notifyOnDataAvailable(true);
            }
        else
            {
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Stop the Gps Receiver.
     * Do not clear the read buffer! It is needed for parsing.
     */

    public boolean stop()
        {
        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "RS232GpsReceiver.stop()");

        if (getSerialPort() != null)
            {
            // Probably unnecessary...
            getSerialPort().notifyOnDataAvailable(false);
            }

        setStarted(false);

        // Do not clear the read buffer! It is needed for parsing.

        return (true);
        }


    /***********************************************************************************************
     * Dispose the GPS Receiver.
     */

    public void dispose()
        {
        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "RS232GpsReceiver.dispose()");

        stop();

        if (getSerialPort() != null)
            {
            // This will automatically remove the SerialPortEventListener
            getSerialPort().close();

            this.serialPort = null;

            this.readBuffer = new StringBuffer();
            }
        }


    /***********************************************************************************************
     * Decode all received NMEA sentences, capturing data for the specified number of seconds.
     * Return <code>true</code> if a valid fix is obtained.
     *
     * @param capturetimesec
     * @param clock
     *
     * @return boolean
     */

    public final boolean decodeNMEA(final int capturetimesec,
                                    final ObservatoryClockInterface clock)
        {
        boolean boolNMEAValid;

        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "RS232GpsReceiver.decodeNMEA() [capturetime=" + capturetimesec + "sec]");

        // Assume that we will fail...
        boolNMEAValid = false;

        if ((isStarted())
            && (capturetimesec > 0))
            {
            try
                {
                int intElapsedTimeSec;

                // Capture enough GpsReceiver data to get a complete set of NMEA sentences
                intElapsedTimeSec = 0;

                while ((isStarted())
                        && (intElapsedTimeSec < capturetimesec)
                        && (Utilities.executeWorkerCanProceed(getHostDAO())))
                    {
                    // Allow other things to happen every second
                    Utilities.safeSleepPollExecuteWorker(SLEEP_WAIT_MILLIS,
                                                         getHostDAO());

                    intElapsedTimeSec++;

                    LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                         "RS232GpsReceiver.decodeNMEA() Data capture [elapsedticks=" + intElapsedTimeSec + "]");
                    }

                // Timed out, so stop the receiver immediately
                stop();

                // We eventually leave when intElapsedTimeSec == capturetimesec

                // There should be something in the input buffer?
                LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                     "RS232GpsReceiver.decodeNMEA() [length=" + readBuffer.length() + "] [buffer=" + readBuffer.toString() + "]");

                // Parse the NMEA data into Receiver instance variables
                boolNMEAValid = parseNMEA(readBuffer.toString(), clock);

                // Now it is safe to discard all previous data
                this.readBuffer = new StringBuffer();
                }

            finally
                {
                // Stop the receiver regardless
                stop();
                }
            }

        GpsReceiverHelper.showFixDebug(this, boolNMEAValid);

        return (boolNMEAValid);
        }


    /***********************************************************************************************
     * Get the SerialPort underlying this TxStream.
     *
     * @return SerialPort
     */

    private SerialPort getSerialPort()
        {
        return (this.serialPort);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the RS232GpsReceiver.
     */

    public void readResources()
        {
        final String strResourceKey;

        // The ResourceKey passed to the Receiver is the Instrument ResourceKey,
        // but we need the Port's key
        if ((getHostDAO() != null)
            && (getHostDAO().getPort() != null))
            {
            strResourceKey = getHostDAO().getPort().getResourceKey();
            }
        else
            {
            strResourceKey = getResourceKey();
            }

        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "RS232GpsReceiver.readResources() [ResourceKey=" + strResourceKey + "]");

        // Serial Port
        strPortName = REGISTRY.getStringProperty(strResourceKey + KEY_PORT_NAME);
        strPortOwner = REGISTRY.getStringProperty(strResourceKey + KEY_PORT_OWNER);
        intBaudrate = REGISTRY.getIntegerProperty(strResourceKey + KEY_PORT_BAUDRATE);
        intDatabits = REGISTRY.getIntegerProperty(strResourceKey + KEY_PORT_DATA_BITS);
        intStopbits = REGISTRY.getIntegerProperty(strResourceKey + KEY_PORT_STOP_BITS);
        intParity = REGISTRY.getIntegerProperty(strResourceKey + KEY_PORT_PARITY);
        strFlowControl = REGISTRY.getStringProperty(strResourceKey + KEY_PORT_FLOW_CONTROL);

        // Reload the Stream Configuration every time the Resources are read
        if ((getHostDAO() != null)
            && (getHostDAO().getPort() != null)
            && (getHostDAO().getPort().getRxStream() != null)
            && (getHostDAO().getPort().getRxStream().getStreamConfiguration() != null))
            {
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_NAME,
                                                       strPortName);
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_OWNER,
                                                       strPortOwner);
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_BAUDRATE,
                                                       Integer.toString(intBaudrate));
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_DATA_BITS,
                                                       Integer.toString(intDatabits));
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_STOP_BITS,
                                                       Integer.toString(intStopbits));
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_PARITY,
                                                       Integer.toString(intParity));
            ConfigurationHelper.addItemToConfiguration(getHostDAO().getPort().getRxStream().getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       strResourceKey + KEY_PORT_FLOW_CONTROL,
                                                       strFlowControl);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * This is the event handler for SerialPortEventListener.
     * The RS232GpsReceiver only receives data.
     *
     * @param event
     */

    public final void serialEvent(final SerialPortEvent event)
        {
        switch(event.getEventType())
            {
            case SerialPortEvent.DATA_AVAILABLE:
                {
                // Data are available, so read from the underlying InputStream and place in a buffer
                try
                    {
                    // ToDo review length constraint
                    while ((getSerialPort() != null)
                        && (getSerialPort().getInputStream() != null)
                        && (getSerialPort().getInputStream().available() > 0)
                        && (readBuffer != null)
                        && (readBuffer.length() < BUFFER_SIZE))
                        {
                        if (isStarted())
                            {
                            // There are some bytes available, so accumulate them
                            readBuffer.append((char)getSerialPort().getInputStream().read());
                            }
                        else
                            {
                            // Otherwise just discard them
                            getSerialPort().getInputStream().read();
                            }
                        }
                    }

                catch(IOException exception)
                    {
                    // Ignore errors normally, just keep trying
                    LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                         "RS232GpsReceiver.serialEvent() IOException reading data from input stream [exception=" + exception.getMessage() + "]");
                    }

                break;
                }

            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                {
                LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                     "RS232GpsReceiver.serialEvent() Serial Event type [eventtype=" + event.getEventType() + "]");
                break;
                }

            default:
                {
                // Ignore errors normally, just keep trying
                LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                     "RS232GpsReceiver.serialEvent() Unknown Serial Event type [eventtype=" + event.getEventType() + "]");
                break;
                }
            }
        }
    }
