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

//------------------------------------------------------------------------------
// Revision History
//
//  13-06-02    LMN created file from original in Phoenix
//  19-06-02    LMN made it work!
//  28-06-02    LMN finishing $GPGGA and $GPRMC
//  01-07-02    LMN added receiver device type
//  02-07-02    LMN tidying up...
//  03-07-02    LMN added serial port parameters to constructor
//  14-04-03    LMN added GpsReceiverInterface
//  28-10-04    LMN copied into the new Framework!
//  17-11-04    LMN started splitting into GpsReceiver and RS232GpsReceiver
//
//------------------------------------------------------------------------------
// To Do
//
//      Verify that all Comm exceptions are dealt with
//
//------------------------------------------------------------------------------
// NMEA is a standard protocol, used by RS232GpsReceiver receivers to transmit data.
// NMEA output is EIA-422A but for most purposes you can consider it RS-232 compatible.
// Use 4800 bps, 8 data bits, no parity and one stop bit (8N1).
// NMEA 0183 sentences are all ASCII. Each sentence begins with a dollarsign ($)
// and ends with a carriage return linefeed (<CR><LF>).
// Data is comma delimited. All commas must be included as they act as markers.
// Some RS232GpsReceiver receivers do not send some of the fields.
// A checksum is optionally added (in a few cases it is mandatory).
// Following the $ is the address field aaccc.
// aa is the device id. Transmission of the device ID is usually optional.
// GP is used to identify RS232GpsReceiver data.
// ccc is the sentence formatter, otherwise known as the sentence name.
//------------------------------------------------------------------------------
// The Garmin GPS35-PC unit produces the following NMEA sentences
//
//  $GPGGA  Global Positioning System Fix Data
//  $GPGSA  RS232GpsReceiver DOP and Active Satellites
//  $GPRMC  Recommended minimum specific RS232GpsReceiver/Transit data
//  $GPGSV  RS232GpsReceiver Satellites in view
//
//  See:    http://www.nmea.org
//          http://home.mira.net/~gnb/gps/nmea.html
//          http://www.garmin.com/products/gps35/spec.html
//
//------------------------------------------------------------------------------

package org.lmn.fc.common.support.gps.impl;

//------------------------------------------------------------------------------
// Imports

import gnu.io.*;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.GpsException;
import org.lmn.fc.common.support.gps.GpsReceiverInterface;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;
import java.util.Vector;


/***************************************************************************************************
 * Read a GARMIN-GPS35PC RS232GpsReceiver device to determine a location.
 */

public final class GarminGpsReceiver implements GpsReceiverInterface,
                                                FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkSingletons,
                                                Runnable,
                                                SerialPortEventListener
    {
    // Implemented device
    private static final String GARMINGPS35PC = "GARMIN-GPS35PC";

    // String Resources
    private static final String EXCEPTION_PORT_NOTFOUND = "Unable to construct the RS232GpsReceiver using serial port ";
    private static final String EXCEPTION_INVALID_GPSRX= "RS232GpsReceiver receiver type not supported";
    private static final String EXCEPTION_INVALID_GPSPORT = "The RS232GpsReceiver must use a serial port";
    public static final String EXCEPTION_OWNED_GPSPORT = "The RS232GpsReceiver port is already owned by another process";
    private static final String EXCEPTION_INUSE_GPSPORT = "The RS232GpsReceiver port is already in use";
    private static final String EXCEPTION_NOSTREAM_GPS = "Unable to get the RS232GpsReceiver input stream";
    public static final String EXCEPTION_LISTENERS_GPS = "Too many serial port listeners";
    private static final String EXCEPTION_COMMSMODE_GPS = "Cannot support requested Comms mode";
    private static final String EXCEPTION_INVALID_BUFFERSIZE = "Invalid Serial Port Size";

    private static final String NMEA_DELIMITER = ",";
    private static final String CHECKSUM_DELIMITER = "*";

    private static final int GPRMC_TOKENS = 12;
    private static final int GPGGA_TOKENS = 14;

    private static final int SLEEP_WAIT_MILLIS = 1000;
    private static final int YEAR_OFFSET = 2000;
    private static final int RADIX_16 = 16;
    private static final int OPEN_TIMEOUT = 2000;
    private static final int BUFFER_SIZE = 4096;
    private static final int RECEIVE_THRESHOLD = 200;
    private static final int SATELLITES_MAX = 12;
    private static final double MINUTES_PER_DEGREE = 60.0;

    // Configurable parameters
    private final String strReceiver;                 // The type of receiver being constructed
    private final String strPortName;                 // The requested port name, COM1 etc.
    private final String strPortOwner;
    private final int intBaudrate;
    private final int intDatabits;
    private final int intStopbits;
    private final int intParity;
    private final String strFlowControl;

    private CommPortIdentifier portID;
    private SerialPort serialPort;
    private InputStream inputStream;
    private StringBuffer readBuffer;            // The serial input buffer

    private Thread gpsReceiverThread;           // The thread to run the receiver
    private boolean boolRunThread;              // Controls the receiver thread

    //--------------------------------------------------------------------------
    // External data, combined from all NMEA sentences

    private String strDateOfLastUpdate;         // The date on which the observation was made
    private YearMonthDayInterface ymdDateOfFix;          // YYYY:MM:DD of the fix
    private HourMinSecInterface hmsTimeOfFix;            // HH:MM:SS of the fix

    private DegMinSecInterface dmsLatitude;              // Latitude of this place
    private DegMinSecInterface dmsLongitude;             // Longitude of this place
    private double dblSpeedKnots;               // The speed of the receiver, in knots
    private double dblCourseDegrees;            // The course of the receiver, in degrees from North
    private double dblMagneticVariation;        // The Magnetic Variation, in degrees West
    private int intDataQuality;                 // The fix quality
    private double dblAltitudeASL;              // Altitude Above Sea Level
    private double dblGeoidAltitude;            // Height of geoid above WGS84 ellipsoid
    private String strFixMode;                  // M=manual, A=automatic (2D/3D)
    private int intFixType;                     // 1=nofix, 2=2D, 3=3D
    private double dblPDOP;                     //
    private double dblHDOP;                     // Horizontal Dilution of Precision
    private double dblVDOP;                     // Vertical Dilution of Precision

    private int intSatellitesInUse;             // Number of satellites used for the fix
    private Vector vecSatellitesInUse;          // IDs of satellites in use

    private int intSatellitesInView;            // Number of satellites in view
    private Vector vecSatellitesInView;         // IDs etc. of satellites in view

    //--------------------------------------------------------------------------
    // GPRMC internal data

    private YearMonthDayInterface ymdGPRMC_DateStamp;
    private HourMinSecInterface hmsGPRMC_TimeStamp;
    private double dblGPRMC_Latitude;
    private double dblGPRMC_Longitude;
    private double dblGPRMC_Speed;
    private double dblGPRMC_Course;
    private double dblGPRMC_Variation;

    //--------------------------------------------------------------------------
    // GPGGA internal data

    private HourMinSecInterface hmsGPGGA_TimeStamp;      // GPRMC takes precedence
    private int intGPGGA_FixQuality;
    private int intGPGGA_SatellitesInUse;
    private double dblGPGGA_HDOP;               // GPGSA takes precedence
    private double dblGPGGA_Altitude;
    private double dblGPGGA_Geoid;
    private double dblGPGGA_Latitude;           // GPRMC takes precedence
    private double dblGPGGA_Longitude;          // GPRMC takes precedence

    //--------------------------------------------------------------------------
    // GPGSA internal data

    private String strGPGSA_FixMode;
    private int intGPGSA_FixType;
    private double dblGPGSA_PDOP;
    private double dblGPGSA_HDOP;
    private double dblGPGSA_VDOP;
    private Vector<String> vecGPGSA_SatellitesInUse;

    //--------------------------------------------------------------------------
    // GPGSV internal data

    private int intGPGSV_SatellitesInView;      // The number of satellites in view
    private Vector<SatelliteData> vecGPGSV_SatellitesInView;   // One GPGSVdata for each satellite
    private static final int RECEIVER_SLEEP_MILLIS = 5000;


    /***********************************************************************************************
     * The constructor checks that the required serial port can be opened,
     * but leaves it closed for later use by the update method.
     *
     * @param receiver
     * @param owner
     * @param portname
     * @param baudrate
     * @param databits
     * @param stopbits
     * @param parity
     * @param flowcontrol
     * @param debug
     *
     * @throws GpsException
     */

    public GarminGpsReceiver(final String receiver,
                             final String owner,
                             final String portname,
                             final int baudrate,
                             final int databits,
                             final int stopbits,
                             final int parity,
                             final String flowcontrol,
                             final boolean debug) throws GpsException
        {
        // Save the data from the parameters
        this.strPortOwner = owner;
        this.strReceiver = receiver;
        this.strPortName = portname;
        this.intBaudrate = baudrate;
        this.intDatabits = databits;
        this.intStopbits = stopbits;
        this.intParity = parity;
        this.strFlowControl = flowcontrol;

        // Todo input parameter validation

        // Check Receiver against the supported type...
        if (!strReceiver.equals(GARMINGPS35PC))
            {
            // Device type not recognised
            throw new GpsException(EXCEPTION_INVALID_GPSRX + " [receiver=" + strReceiver + "]");
            }

        // Initialise all RS232GpsReceiver data
        this.strDateOfLastUpdate = Chronos.timeNow();
        this.ymdDateOfFix = new YearMonthDayDataType();
        this.hmsTimeOfFix = new HourMinSecDataType();

        this.dmsLatitude = new LatitudeDataType();
        this.dmsLongitude = new LongitudeDataType();
        this.dblSpeedKnots = 0.0;
        this.dblCourseDegrees = 0.0;
        this.dblMagneticVariation = 0.0;
        this.intDataQuality = 0;
        this.dblAltitudeASL = 0.0;
        this.dblGeoidAltitude = 0.0;
        this.strFixMode = "A";
        this.intFixType = 0;
        this.dblPDOP = 0.0;
        this.dblHDOP = 0.0;
        this.dblVDOP = 0.0;

        this.intSatellitesInUse = 0;
        this.intSatellitesInView = 0;
        this.vecSatellitesInUse = new Vector<String>(10);
        this.vecSatellitesInView = new Vector<String>(10);
        }


    //----------------------------------------------------------------------------------------------
    // This method is required by the Runnable interface
    // The Runnable interface should be implemented by any class whose instances
    // are intended to be executed by a thread.
    // In most cases, the Runnable interface should be used if you are only
    // planning to override the run() method and no other Thread methods.
    /***********************************************************************************************
     * Run the Gps Receiver.
     */

    public final void run()
        {
        // The Thread runs only until this flag is set false...
        while (boolRunThread)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "GPS Receiver Thread run() sleep for 5 seconds");

            Utilities.safeSleep(RECEIVER_SLEEP_MILLIS);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "GPS Receiver Thread resuming");
            }

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "GPS Receiver Thread leaving run() loop");
        }


    /***********************************************************************************************
     * Shut down the Gps Receiver.
     */

    public void shutdown()
        {
        LOGGER.debugRunnable(Chronos.getSystemTimeNow() + " Garmin GpsReceiver shutdown");
        boolRunThread = false;
        }


    /***********************************************************************************************
     * Try to open and close the specified serial port.
     *
     * @param portname
     *
     * @return boolean
     */

    public boolean findPort(final String portname)
        {
        final Enumeration portList;
        CommPortIdentifier portRequested;
        SerialPort port;
        boolean boolFoundPort;
        boolean boolPortOk;

        if ((portname == null)
            || (EMPTY_STRING.equals(portname)))
            {
            throw new GpsException(EXCEPTION_PARAMETER_INVALID);
            }

        LOGGER.debug("RS232GpsReceiver locating port=" + portname);
        port = null;
        boolPortOk = false;
        this.portID = null;

        try
            {
            portList = CommPortIdentifier.getPortIdentifiers();
            portRequested = null;
            boolFoundPort = false;

            // Scan all ports found for the one with the required name
            LOGGER.debug("Scanning ports for " + portname);

            while ((portList != null)
                && (portList.hasMoreElements())
                && (!boolFoundPort))
                {
                final CommPortIdentifier portId;

                portId = (CommPortIdentifier) portList.nextElement();

                if (portId.isCurrentlyOwned())
                    {
                    LOGGER.debug(INDENT + "[port=" + portId.getName()
                                    + "] [owner=" + portId.getCurrentOwner() + "]");
                    }
                else
                    {
                    LOGGER.debug(INDENT + "[port=" + portId.getName()
                                    + "] [not owned]");
                    }

                if ((portname.equals(portId.getName()))
                    && (portId.getPortType() == CommPortIdentifier.PORT_SERIAL))
                    {
                    portRequested = portId;
                    boolFoundPort = true;
                    LOGGER.debug("Serial Port found [port=" + portId.getName() + "]");
                    }
                }

            // Did we find a port?
            if (boolFoundPort)
                {
                // Is the port already in use?
                if (portRequested.isCurrentlyOwned())
                    {
                    throw new GpsException(EXCEPTION_INUSE_GPSPORT
                                                + " [findPort] [portowner="
                                                + portRequested.getCurrentOwner()
                                                + "]");
                    }
                else
                    {
                    final InputStream stream;

                    // Try to open the port and input stream (these throw exceptions)
                    LOGGER.debug("findPort() Open port");
                    port = openSerialPort(portRequested);
                    stream = getInputStream(port);

                    // If we get here, then it all worked Ok, and we can close the port
                    LOGGER.debug("findPort() Close port");
                    port.close();
                    stream.close();
                    boolPortOk = true;
                    }
                }
            else
                {
                // Cannot create a RS232GpsReceiver because we did not find the required port
                throw new GpsException(EXCEPTION_PORT_NOTFOUND
                                        + " [findPort] [portname="
                                        + portname
                                        + "]");
                }
            }

        catch (UnsatisfiedLinkError exception)
            {
            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
            throw new GpsException(exception.getMessage(),
                                   exception);
            }

        catch (IOException exception)
            {
            throw new GpsException(exception.getMessage(),
                                   exception);
            }

        catch (GpsException exception)
            {
            throw new GpsException(exception.getMessage(),
                                   exception);
            }

        finally
            {
            if (port != null)
                {
                port.close();
                }
            }

        // If the port functioned correctly, then set the port for later use
        // This is done here so that the GpsReceiverInterface does not know about
        // the actual implementation of the port
        if (boolPortOk)
            {
            this.portID = portRequested;
            }

        return (boolPortOk);
        }


    /***********************************************************************************************
     * Decode all received NMEA sentences, capturing data for the specified number of seconds.
     *
     * @param capturetime
     *
     * @return boolean
     */

    public final boolean decodeNMEA(final int capturetime)
        {
        boolean boolNMEAValid;

        // Assume that we will fail...
        boolNMEAValid = false;

        if ((this.portID != null)
            && (capturetime > 0))
            {
            try
                {
                int intElapsedTime;

                // Clear the input buffer used by the serial port
                this.readBuffer = new StringBuffer();

                // Capture enough RS232GpsReceiver data to get a complete set of NMEA sentences
                LOGGER.debug("RS232GpsReceiver.decodeNMEA() Starting RS232GpsReceiver");
                LOGGER.debug("RS232GpsReceiver.decodeNMEA() Open port");
                serialPort = openSerialPort(portID);

                if (serialPort != null)
                    {
                    inputStream = getInputStream(serialPort);

                    // Remember that 'this' implements SerialPortEventListener
                    // The event handler is serialEvent()
                    serialPort.addEventListener(this);

                    // This will fire serialEvent() when data is waiting
                    serialPort.notifyOnDataAvailable(true);

                    if ((gpsReceiverThread != null)
                        && (gpsReceiverThread.isAlive()))
                        {
                        gpsReceiverThread.interrupt();
                        }

                    // Start the receiver reading thread
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "Garmin GpsReceiver boolRunThread = true");
                    boolRunThread = true;

                    // ToDo Consider SwingWorker
                    gpsReceiverThread = new Thread(REGISTRY.getThreadGroup(),
                                                   this,
                                                   "Thread RS232GpsReceiver");
                    gpsReceiverThread.start();

                    // Wait for it to capture enough data to decode
                    intElapsedTime = 0;
                    while ((boolRunThread)
                        && (intElapsedTime < capturetime))
                        {
                        // Allow other things to happen every second
                        // Causes the currently executing thread to sleep
                        // (temporarily cease execution) for the specified number of milliseconds.
                        //Thread.sleep(SLEEP_WAIT_MILLIS);
                        Utilities.safeSleep(SLEEP_WAIT_MILLIS);

                        intElapsedTime++;
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               "Data capture [elapsedticks=" + intElapsedTime + "]");
                        }

                    // Timed out, so stop the receiver
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "Stopping RS232GpsReceiver receiver");
                    shutdown();
                    serialPort.notifyOnDataAvailable(false);
                    serialPort.removeEventListener();

                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "Close port");
                    serialPort.close();
                    inputStream.close();

                    // There should be something in the input buffer?
                    LOGGER.debug("[buffer=" + readBuffer.toString() + "]");

                    LOGGER.debug("Parsing NMEA");
                    boolNMEAValid = parseNMEA(readBuffer.toString());
                    }
                else
                    {
                    boolNMEAValid = false;
                    throw new GpsException("decodeNMEA Unable to open serial port");
                    }
                }

            catch (IOException exception)
                {
                throw new GpsException("decodeNMEA",
                                       exception);
                }

//            catch (InterruptedException exception)
//                {
//                Thread.currentThread().interrupt();
//                throw new GpsException("decodeNMEA",
//                                       exception);
//                }
//
            catch(TooManyListenersException exception)
                {
                throw new GpsException("[TooManyListenersException=" + exception.getMessage() + "]",
                                       exception);
                }

            finally
                {
                if (serialPort != null)
                    {
                    serialPort.close();
                    }
                }
            }
        else
            {
            boolNMEAValid = false;
            }

        return (boolNMEAValid);
        }


    /***********************************************************************************************
     * Parse the NMEA sentences.
     *
     * @param nmea
     *
     * @return boolean
     */

    private boolean parseNMEA(final String nmea)
        {
        int intChecksumPointer;
        boolean bool$GPRMCValid;
        String str$GPRMC;
        int intNMEAPointer;
        boolean bool$GPGGAValid;
        String str$GPGGA;
        boolean bool$GPGSAValid;
        String str$GPGSA;
        final Vector<String> vec$GPGSV;
        int intGPGSVFound;
        boolean bool$GPGSVValid;
        int intGPGSVIndex;
        final int intGPGSVSet;
        final boolean boolNMEAValid;
        intChecksumPointer = 0;

        // Parse out each NMEA sentence in order $GPRMC->$GPGGA->$GPGSA->$GPGSV

        // Search for the $GPRMC sentence

        bool$GPRMCValid = false;
        str$GPRMC = "";
        intNMEAPointer = nmea.indexOf("$GPRMC");

        if (intNMEAPointer != -1)
            {
            intChecksumPointer = nmea.indexOf("*", intNMEAPointer);
            if (intChecksumPointer != -1)
                {
                str$GPRMC = nmea.substring(intNMEAPointer, intChecksumPointer+3);
                LOGGER.debug("[$GPRMCsentence=" + str$GPRMC + "]");
                bool$GPRMCValid = true;
                }
            }

        //----------------------------------------------------------------------
        // Search for the $GPGGA sentence

        bool$GPGGAValid = false;
        str$GPGGA = "";
        intNMEAPointer = nmea.indexOf("$GPGGA", intChecksumPointer);

        if (intNMEAPointer != -1)
            {
            intChecksumPointer = nmea.indexOf("*", intNMEAPointer);
            if (intChecksumPointer != -1)
                {
                str$GPGGA = nmea.substring(intNMEAPointer, intChecksumPointer+3);
                LOGGER.debug("[$GPGGAsentence=" + str$GPGGA + "]");
                bool$GPGGAValid = true;
                }
            }

        //----------------------------------------------------------------------
        // Search for the $GPGSA sentence

        bool$GPGSAValid = false;
        str$GPGSA = "";
        intNMEAPointer = nmea.indexOf("$GPGSA", intChecksumPointer);

        if (intNMEAPointer != -1)
            {
            intChecksumPointer = nmea.indexOf("*", intNMEAPointer);
            if (intChecksumPointer != -1)
                {
                str$GPGSA = nmea.substring(intNMEAPointer, intChecksumPointer+3);
                LOGGER.debug("[$GPGSAsentence=" + str$GPGSA + "]");
                bool$GPGSAValid = true;
                }
            }

        //----------------------------------------------------------------------
        // Search for the first $GPGSV sentence
        // There may be several in the same set {1...n}

        vec$GPGSV = new Vector<String>(10);
        intNMEAPointer = nmea.indexOf("$GPGSV", intChecksumPointer);
        intGPGSVFound = 0;
        bool$GPGSVValid = false;

        if (intNMEAPointer != -1)
            {
            intChecksumPointer = nmea.indexOf("*", intNMEAPointer);
            if (intChecksumPointer != -1)
                {
                // We have found one sentence, so add it to the vector
                intGPGSVFound = 1;
                vec$GPGSV.add(nmea.substring(intNMEAPointer, intChecksumPointer+3));
                //LOGGER.debug("[$GPGSVsentence=" + (String)vec$GPGSV.get(0) + "]");

                // Check that the sentence already read is index 1 of a series
                String strTest = vec$GPGSV.get(0);
                intGPGSVIndex = Integer.parseInt(strTest.substring(9, 10));

                if (intGPGSVIndex == 1)
                    {
                    // Read how many messages of this type should occur in this cycle
                    // This is in character 7 (assuming there are never more than 9 in the set)
                    intGPGSVSet = Integer.parseInt(strTest.substring(7, 8));
                    LOGGER.debug("[$GPGSVset=" + intGPGSVSet + "]");

                    if (intGPGSVSet > 1)
                        {
                        // There's more to read
                        // Any errors here will reset the valid flag

                        for (int i = 0; i < intGPGSVSet-1; i++)
                            {
                            // Reset pointer to the beginning of the next sentence
                            intNMEAPointer = intChecksumPointer+3;
                            intNMEAPointer = nmea.indexOf("$GPGSV", intNMEAPointer);

                            // Did we find another one immediately?
                            if ((intNMEAPointer != -1) && (intNMEAPointer <= intChecksumPointer+10))
                                {
                                intChecksumPointer = nmea.indexOf("*", intNMEAPointer);
                                if (intChecksumPointer != -1)
                                    {
                                    // Check that this sentence is the ith in the series
                                    // The index is in character 9 (assuming there are never more than 9 in the set)
                                    strTest = nmea.substring(intNMEAPointer, intChecksumPointer+3);
                                    intGPGSVIndex = Integer.parseInt(strTest.substring(9, 10));
                                    if (intGPGSVIndex == i+2)
                                        {
                                        // We seem to have found a valid sentence, so add it to the vector
                                        intGPGSVFound++;
                                        vec$GPGSV.add(strTest);
                                        //LOGGER.debug("[$GPGSVsentence=" + (String)vec$GPGSV.get(i+1) + "]");
                                        bool$GPGSVValid = true;
                                        }
                                    else
                                        {
                                        // The sentence found doesn't fit the series expected
                                        bool$GPGSVValid = false;
                                        }
                                    }
                                else
                                    {
                                    // We didn't find the checksum field...
                                    bool$GPGSVValid = false;
                                    }
                                }
                            else
                                {
                                // We didn't find another sentence, but we should have done...
                                bool$GPGSVValid = false;
                                }
                            }
                        }
                    else
                        {
                        // There's no more to read, so all seems Ok?
                        bool$GPGSVValid = true;
                        }
                    }
                else
                    {
                    // The index was not the first in the series
                    bool$GPGSVValid = false;
                    }

                // Show all sentences found
                for (int i = 0; i < intGPGSVFound; i++)
                    {
                    LOGGER.debug("[$GPGSVsentence(" + (i+1) + ")=" + vec$GPGSV.get(i) + "]");
                    }
                }
            }

        //----------------------------------------------------------------------
        // If all sentences were parsed correctly,
        // decode each sentence to get at the data
        // Save all the decoded data, but only if *all* decoded correctly

        if (bool$GPGGAValid
            && bool$GPGSAValid
            && bool$GPRMCValid
            && bool$GPGSVValid)
            {
            LOGGER.debug("Decoding NMEA");

            bool$GPRMCValid = decode$GPRMC(str$GPRMC);
            bool$GPGGAValid = decode$GPGGA(str$GPGGA);
            bool$GPGSAValid = decode$GPGSA(str$GPGSA);
            bool$GPGSVValid = decode$GPGSV(vec$GPGSV, intGPGSVFound);

            if (bool$GPGGAValid
                && bool$GPGSAValid
                && bool$GPRMCValid
                && bool$GPGSVValid)
                {
                this.ymdDateOfFix = ymdGPRMC_DateStamp;
                ymdDateOfFix.enableFormatSign(false);
                this.hmsTimeOfFix = hmsGPRMC_TimeStamp;
                hmsTimeOfFix.enableFormatSign(false);

                this.dmsLatitude = new LatitudeDataType(dblGPRMC_Latitude);
                this.dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);
                this.dmsLongitude = new LongitudeDataType(dblGPRMC_Longitude);
                dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);

                this.dblSpeedKnots = dblGPRMC_Speed;
                this.dblCourseDegrees = dblGPRMC_Course;
                this.dblMagneticVariation = dblGPRMC_Variation;
                this.intDataQuality = intGPGGA_FixQuality;

                this.strFixMode = strGPGSA_FixMode;
                this.intFixType = intGPGSA_FixType;
                this.dblPDOP = dblGPGSA_PDOP;
                this.dblHDOP = dblGPGSA_HDOP;
                this.dblVDOP = dblGPGSA_VDOP;

                this.intSatellitesInUse = intGPGGA_SatellitesInUse;
                this.vecSatellitesInUse = vecGPGSA_SatellitesInUse;

                this.intSatellitesInView = intGPGSV_SatellitesInView;
                this.vecSatellitesInView = vecGPGSV_SatellitesInView;

                // Only update altitude if we have four or more satellites
                if (intSatellitesInUse > 3)
                    {
                    this.dblAltitudeASL = dblGPGGA_Altitude;
                    this.dblGeoidAltitude = dblGPGGA_Geoid;
                    }

                setDateOfLastUpdate(Chronos.timeNow());
                LOGGER.debug("NMEA Decoding completed successfully at " + getDateOfLastUpdate());
                boolNMEAValid = true;
                }
            else
                {
                // One or more decodes failed
                LOGGER.debug("Decoding errors encountered");
                boolNMEAValid = false;
                }
            }
        else
            {
            LOGGER.debug("Parsing errors encountered, nothing to decode");

            boolNMEAValid = false;
            }

        return (boolNMEAValid);
        }


    //--------------------------------------------------------------------------
    // Decoding utilities
    // Keep decoded data in local variables until *all* have been decoded correctly
    //--------------------------------------------------------------------------
    // Decode the $GPRMC sentence

    private boolean decode$GPRMC(final String sentence)
        {
        final StringTokenizer stringTokenizer;
        final int intCountTokens;
        final String strTimeStamp;
        final String strValidity;
        final String strLatitude;
        final String strLatHemisphere;
        final String strLongitude;
        final String strLongHemisphere;
        final String strSpeed;
        final String strCourse;
        final String strDateStamp;
        final String strVariation;
        final String strVariationHemisphere;
        final String strChecksum;
        final int intChecksum;
        final String strTemp;

        stringTokenizer = new StringTokenizer(sentence, NMEA_DELIMITER);
        intCountTokens = stringTokenizer.countTokens();

        // There must be exactly 12 tokens
        if (intCountTokens == GPRMC_TOKENS)
            {
            LOGGER.debug("Decoding $GPRMC [tokens=" + intCountTokens + "]");

            // Discard the sentence identifier
            stringTokenizer.nextToken();

            // Read the TimeStamp
            strTimeStamp = stringTokenizer.nextToken();
            hmsGPRMC_TimeStamp = new HourMinSecDataType(true,
                                                        Integer.parseInt(strTimeStamp.substring(0, 2)),
                                                        Integer.parseInt(strTimeStamp.substring(2, 4)),
                                                        Integer.parseInt(strTimeStamp.substring(4)));
            LOGGER.debug("[timestamp=" + hmsGPRMC_TimeStamp.toString_HH_MM_SS() + "]");

            // Read the validity indicator
            strValidity = stringTokenizer.nextToken();
            LOGGER.debug("[validity=" + strValidity + "]");

            if (strValidity.equals("A"))
                {
                // Read the latitude and decode the degrees and minutes
                strLatitude = stringTokenizer.nextToken();
                // Convert the minutes to degrees
                dblGPRMC_Latitude = Double.parseDouble(strLatitude.substring(2)) / MINUTES_PER_DEGREE;
                // Add in the degrees (two digits)
                dblGPRMC_Latitude = dblGPRMC_Latitude + Double.parseDouble(strLatitude.substring(0, 2));

                // and the latitude hemisphere
                strLatHemisphere = stringTokenizer.nextToken();

                if ((strLatHemisphere.equals("S")) || (strLatHemisphere.equals("N")))
                    {
                    if (strLatHemisphere.equals("S"))
                        {
                        dblGPRMC_Latitude = -dblGPRMC_Latitude;
                        }

                    LOGGER.debug("[latitude=" + dblGPRMC_Latitude + "]");

                    // Read the longitude and decode the degrees and minutes
                    strLongitude = stringTokenizer.nextToken();
                    // Convert the minutes to degrees
                    dblGPRMC_Longitude = Double.parseDouble(strLongitude.substring(3)) / MINUTES_PER_DEGREE;
                    // Add in the degrees (three digits)
                    dblGPRMC_Longitude = dblGPRMC_Longitude + Double.parseDouble(strLongitude.substring(0, 3));

                    // and the longitude hemisphere
                    strLongHemisphere = stringTokenizer.nextToken();

                    if ((strLongHemisphere.equals("E")) || (strLongHemisphere.equals("W")))
                        {
                        // Astronomical convention is that Longitudes are POSITIVE in the WEST
                        // but the rest of the world does it the other way round....
                        if (strLongHemisphere.equals("W"))
                            {
                            dblGPRMC_Longitude = -dblGPRMC_Longitude;
                            }

                        LOGGER.debug("[longitude=" + dblGPRMC_Longitude + "]");

                        // Read the Speed
                        strSpeed = stringTokenizer.nextToken();
                        dblGPRMC_Speed = Double.parseDouble(strSpeed);
                        LOGGER.debug("[speed=" + dblGPRMC_Speed + "]");

                        // Read the course
                        strCourse = stringTokenizer.nextToken();
                        dblGPRMC_Course = Double.parseDouble(strCourse);
                        LOGGER.debug("[course=" + dblGPRMC_Course + "]");

                        // Read the DateStamp (note the adjustment for the year 2000)
                        strDateStamp = stringTokenizer.nextToken();
                        ymdGPRMC_DateStamp = new YearMonthDayDataType(true,
                                                              YEAR_OFFSET + Integer.parseInt(strDateStamp.substring(4)),
                                                              Integer.parseInt(strDateStamp.substring(2, 4)),
                                                              Integer.parseInt(strDateStamp.substring(0, 2)));
                        LOGGER.debug("[datestamp=" + ymdGPRMC_DateStamp.toString() + "]");

                        // Read the magnetic variation
                        strVariation = stringTokenizer.nextToken();
                        dblGPRMC_Variation = Double.parseDouble(strVariation);
                        LOGGER.debug("[unsignedvariation=" + dblGPRMC_Variation + "]");

                        // The last token contains the Variation Hemisphere AND the checksum (duh!)
                        // This seems to be anomalous NMEA syntax. Oh well...
                        strTemp = stringTokenizer.nextToken();
                        strVariationHemisphere = strTemp.substring(0, 1);
                        strChecksum = strTemp.substring(1);

                        // Check the variation hemisphere
                        if ((strVariationHemisphere.equals("E"))
                            || (strVariationHemisphere.equals("W")))
                            {
                            if (strVariationHemisphere.equals("E"))
                                {
                                dblGPRMC_Variation = -dblGPRMC_Variation;
                                }

                            LOGGER.debug("[variation=" + dblGPRMC_Variation + "]");

                            // Read the HEX checksum
                            intChecksum = Integer.parseInt(strChecksum.substring(1), RADIX_16);
                            LOGGER.debug("[checksum=" + Integer.toHexString(intChecksum) + "]");

                            // Check the checksum...
                            // This could be expanded
                            if (strChecksum.substring(0, 1).equals("*"))
                                {
                                // Everything worked Ok!
                                LOGGER.debug("$GPRMC decoded successfully");
                                return(true);
                                }
                            else
                                {
                                // Checksum delimiter invalid (very unlikely)
                                LOGGER.debug("Invalid checksum");
                                return(false);
                                }
                            }
                        else
                            {
                            // Variation hemisphere was expected to be "E" or "W"
                            LOGGER.debug("Invalid Variation hemisphere");
                            return(false);
                            }
                        }
                    else
                        {
                        // Longitude hemisphere was expected to be "E" or "W"
                        LOGGER.debug("Invalid Longitude hemisphere");
                        return(false);
                        }
                     }
                else
                    {
                    // Latitude hemisphere was expected to be "S" or "N"
                    LOGGER.debug("Invalid Latitude hemisphere");
                    return(false);
                    }
                }
            else
                {
                // The data is not valid (receiver warning)
                LOGGER.debug("Invalid data (receiver warning)");
                return(false);
                }
            }
        else
            {
            // Not enough tokens were found to be decoded correctly
            LOGGER.debug("Not enough tokens in $GPRMC ([found=" + intCountTokens + "]");
            return(false);
            }
        }


    //--------------------------------------------------------------------------
    // Decode the $GPGGA sentence

    private boolean decode$GPGGA(final String sentence)
        {
        final StringTokenizer stringTokenizer;
        int intCountTokens;
        final String strChecksum;
        final int intChecksum;
        final String strTimeStamp;
        String strUnits;
        final String strLatitude;
        final String strLongitude;
        final String strLatHemisphere;
        final String strLongHemisphere;

        stringTokenizer = new StringTokenizer(sentence, NMEA_DELIMITER);
        intCountTokens = stringTokenizer.countTokens();

        // There must be enough tokens to get as far as the Fix Quality
        if (intCountTokens >= GPGGA_TOKENS-7)
            {
            LOGGER.debug("Decoding $GPGGA [tokens=" + intCountTokens + "]");

            // Discard the sentence identifier
            stringTokenizer.nextToken();

            // Read the TimeStamp
            strTimeStamp = stringTokenizer.nextToken();
            hmsGPGGA_TimeStamp = new HourMinSecDataType(true,
                                                        Integer.parseInt(strTimeStamp.substring(0, 2)),
                                                        Integer.parseInt(strTimeStamp.substring(2, 4)),
                                                        Integer.parseInt(strTimeStamp.substring(4)));
            LOGGER.debug("[timestamp=" + hmsGPGGA_TimeStamp.toString_HH_MM_SS() + "]");

            // Read the latitude and decode the degrees and minutes
            strLatitude = stringTokenizer.nextToken();
            // Convert the minutes to degrees
            dblGPGGA_Latitude = Double.parseDouble(strLatitude.substring(2)) / MINUTES_PER_DEGREE;
            // Add in the degrees (two digits)
            dblGPGGA_Latitude = dblGPGGA_Latitude + Double.parseDouble(strLatitude.substring(0, 2));

            // and the latitude hemisphere
            strLatHemisphere = stringTokenizer.nextToken();

            if ((strLatHemisphere.equals("S")) || (strLatHemisphere.equals("N")))
                {
                if (strLatHemisphere.equals("S"))
                    {
                    dblGPGGA_Latitude = -dblGPGGA_Latitude;
                    }

                LOGGER.debug("[latitude=" + dblGPGGA_Latitude + "]");

                // Read the longitude and decode the degrees and minutes
                strLongitude = stringTokenizer.nextToken();
                // Convert the minutes to degrees
                dblGPGGA_Longitude = Double.parseDouble(strLongitude.substring(3)) / MINUTES_PER_DEGREE;
                // Add in the degrees (three digits)
                dblGPGGA_Longitude = dblGPGGA_Longitude + Double.parseDouble(strLongitude.substring(0, 3));

                // and the longitude hemisphere
                strLongHemisphere = stringTokenizer.nextToken();

                if ((strLongHemisphere.equals("E")) || (strLongHemisphere.equals("W")))
                    {
                    // Astronomical convention is that Longitudes are POSITIVE in the WEST
                    if (strLongHemisphere.equals("E"))
                        {
                        dblGPGGA_Longitude = -dblGPGGA_Longitude;
                        }

                    LOGGER.debug("[longitude=" + dblGPGGA_Longitude + "]");

                    // Read the Fix Quality, and subsequent tokens if Ok
                    intGPGGA_FixQuality = Integer.parseInt(stringTokenizer.nextToken());
                    LOGGER.debug("[fixquality=" + intGPGGA_FixQuality + "]");

                    if (intGPGGA_FixQuality > 0)
                        {
                        // Now check that we have enough tokens left
                        intCountTokens = stringTokenizer.countTokens();
                        LOGGER.debug("[tokensleft=" + intCountTokens + "]");

                        // Read the number of satellites in view
                        intGPGGA_SatellitesInUse = Integer.parseInt(stringTokenizer.nextToken());
                        LOGGER.debug("[satellites=" + intGPGGA_SatellitesInUse + "]");

                        // Read HDOP
                        dblGPGGA_HDOP = Double.parseDouble(stringTokenizer.nextToken());
                        LOGGER.debug("[HDOP=" + dblGPGGA_HDOP + "]");

                        // Read Altitude
                        dblGPGGA_Altitude = Double.parseDouble(stringTokenizer.nextToken());
                        LOGGER.debug("[altitude=" + dblGPGGA_Altitude + "]");

                        // Read altitude units
                        strUnits = stringTokenizer.nextToken();
                        LOGGER.debug("[units=" + strUnits + "]");

                        if (strUnits.equals("M"))
                            {
                            // Read height of geoid above WGS84 ellipsoid
                            dblGPGGA_Geoid = Double.parseDouble(stringTokenizer.nextToken());
                            LOGGER.debug("[geoid=" + dblGPGGA_Geoid + "]");

                            // Read geoid units
                            strUnits = stringTokenizer.nextToken();
                            LOGGER.debug("[units=" + strUnits + "]");

                            if (strUnits.equals("M"))
                                {
                                // Read the HEX checksum
                                strChecksum = stringTokenizer.nextToken();
                                intChecksum = Integer.parseInt(strChecksum.substring(1), RADIX_16);
                                LOGGER.debug("[checksum=" + Integer.toHexString(intChecksum) + "]");

                                // Check the checksum...
                                // This could be expanded
                                if (strChecksum.substring(0, 1).equals("*"))
                                    {
                                    // Everything worked Ok!
                                    LOGGER.debug("$GPGGA decoded successfully");
                                    return(true);
                                    }
                                else
                                    {
                                    // Checksum delimiter invalid (very unlikely)
                                    LOGGER.debug("Invalid checksum");
                                    return(false);
                                    }
                                }
                            else
                                {
                                // Geoid Units were expected to be Metres
                                LOGGER.debug("Invalid geoid units");
                                return(false);
                                }
                            }
                        else
                            {
                            // Altitude Units were expected to be Metres
                            LOGGER.debug("Invalid altitude units");
                            return(false);
                            }
                        }
                    else
                        {
                        // Unable to get a valid fix
                        LOGGER.debug("Invalid fix");
                        return(false);
                        }
                    }
                else
                    {
                    // Longitude hemisphere was expected to be "E" or "W"
                    LOGGER.debug("Invalid Longitude hemisphere");
                    return(false);
                    }
                 }
            else
                {
                // Latitude hemisphere was expected to be "S" or "N"
                LOGGER.debug("Invalid Latitude hemisphere");
                return(false);
                }
            }
        else
            {
            // Not enough tokens were found to be decoded correctly
            LOGGER.debug("Not enough tokens in $GPGGA ([found=" + intCountTokens + "]");
            return(false);
            }
        }


    //--------------------------------------------------------------------------
    // Decode the $GPGSA sentence

    private boolean decode$GPGSA(final String sentence)
        {
        StringTokenizer stringTokenizer;
        final int intCountTokens;
        final String strChecksum;
        final int intChecksum;
        final String strFixType;
        final String strPDOP;
        final String strHDOP;
        final String strVDOP;
        String strTemp;

        vecGPGSA_SatellitesInUse = new Vector<String>(10);

        // Return the delimiters this time, because of empty fields...
        stringTokenizer = new StringTokenizer(sentence, NMEA_DELIMITER, true);
        intCountTokens = stringTokenizer.countTokens();
        LOGGER.debug("Decoding $GPGSA [tokens=" + intCountTokens + "]");

        // Discard the sentence identifier
        stringTokenizer.nextToken();
        // Discard the trailing delimiter
        stringTokenizer.nextToken();

        // Read the Mode
        strGPGSA_FixMode = stringTokenizer.nextToken();
        LOGGER.debug("[fixmode=" + strGPGSA_FixMode + "]");
        // Discard the trailing delimiter
        stringTokenizer.nextToken();

        // Read the Fix Type
        strFixType = stringTokenizer.nextToken();
        intGPGSA_FixType = Integer.parseInt(strFixType);
        LOGGER.debug("[fixtype=" + intGPGSA_FixType + "]");
        // Discard the trailing delimiter
        stringTokenizer.nextToken();

        if (intGPGSA_FixType > 1)
            {
            // Read the satellites in view used for the fix
            // A maximum of 12 entries
            for (int i = 0; i < SATELLITES_MAX; i++)
                {
                // Read a token, and see if the field is occupied
                strTemp = stringTokenizer.nextToken();

                if (!strTemp.equals(NMEA_DELIMITER))
                    {
                    // Record the satellite ID in the vector
                    vecGPGSA_SatellitesInUse.add(strTemp);
                    LOGGER.debug("[add satelliteid=" + strTemp + "]");
                    // Discard the trailing delimiter
                    stringTokenizer.nextToken();
                    }
                else
                    {
                    LOGGER.debug("[skip null satelliteid=" +  strTemp + "]");
                    }
                }

            // Read the PDOP
            strPDOP = stringTokenizer.nextToken();
            dblGPGSA_PDOP = Double.parseDouble(strPDOP);
            LOGGER.debug("[PDOP=" + dblGPGSA_PDOP + "]");
            // Discard the trailing delimiter
            stringTokenizer.nextToken();

            // Read the HDOP
            strHDOP = stringTokenizer.nextToken();
            dblGPGSA_HDOP = Double.parseDouble(strHDOP);
            LOGGER.debug("[HDOP=" + dblGPGSA_HDOP + "]");
            // Discard the trailing delimiter
            stringTokenizer.nextToken();

            // The last token contains the VDOP and the checksum (duh!)
            strTemp = stringTokenizer.nextToken();

            // The VDOP may be missing?!
            if (strTemp.substring(0, 1).equals(CHECKSUM_DELIMITER))
                {
                // Set a neutral VDOP
                strVDOP = "1.0";

                // There is no VDOP information, so just read the checksum
                strChecksum = strTemp.substring(1);
                }
            else
                {
                // We have a VDOP and checksum, so retokenize using the checksum delimiter
                stringTokenizer = new StringTokenizer(strTemp, CHECKSUM_DELIMITER);

                // Read the VDOP
                strVDOP = stringTokenizer.nextToken();

                // Read the checksum
                strChecksum = stringTokenizer.nextToken();
                LOGGER.debug("[strchecksum=" + strChecksum + "]");
                }

            dblGPGSA_VDOP = Double.parseDouble(strVDOP);
            LOGGER.debug("[VDOP=" + dblGPGSA_VDOP + "]");

            intChecksum = Integer.parseInt(strChecksum, RADIX_16);
            LOGGER.debug("[checksum=" + Integer.toHexString(intChecksum) + "]");

            // ToDo Check the checksum...
            // This could be expanded
            if (true)
                {
                // Everything worked Ok!
                LOGGER.debug("$GPGSA decoded successfully");
                return(true);
                }
            else
                {
                // Checksum delimiter invalid (very unlikely)
                LOGGER.debug("Invalid checksum");
                return(false);
                }
            }
        else
            {
            // No fix available
            LOGGER.debug("No fix available");
            return(false);
            }
        }


    //--------------------------------------------------------------------------
    // Decode the $GPGSV sentence(s)

    private boolean decode$GPGSV(final Vector sentence, final int count)
        {
        StringTokenizer stringTokenizer;
        int intCountTokens;
        String strChecksum;
        int intChecksum;
        String strSentence;
        int intMessageCount;
        int intMessageNumber;
        String strTemp;
        SatelliteData GPGSVsatellite;
        final Enumeration enumGPGSV;

        if ((count < 1) || (count > 50))
            {
            // Input parameter invalid
            LOGGER.debug("Invalid satellite count [count=" + count + "]");
            return(false);
            }

        vecGPGSV_SatellitesInView = new Vector<SatelliteData>(10);

        for (int i = 0; i < count; i++)
            {
            // Retrieve the ith parsed $GPGSV sentence
            strSentence = (String)sentence.get(i);

            // Return the delimiters this time, because of possible empty fields...
            stringTokenizer = new StringTokenizer(strSentence, NMEA_DELIMITER, true);
            intCountTokens = stringTokenizer.countTokens();
//            LOGGER.debug(StringLibrary.MARKER);
            LOGGER.debug("Decoding $GPGSV(" + (i+1) + ") [tokens=" + intCountTokens + "]");
            LOGGER.debug("[sentence=" + strSentence + "]");

            // Discard the sentence identifier
            stringTokenizer.nextToken();
            // Discard the trailing delimiter
            stringTokenizer.nextToken();

            // Read the total number of messages in this cycle
            intMessageCount = Integer.parseInt(stringTokenizer.nextToken());
            // Discard the trailing delimiter
            stringTokenizer.nextToken();

            // Double check for correct parsing earlier
            if (intMessageCount == count)
                {
                LOGGER.debug("[messagecount=" + intMessageCount + "]");

                // Read the MessageNumber
                intMessageNumber = Integer.parseInt(stringTokenizer.nextToken());
                // Discard the trailing delimiter
                stringTokenizer.nextToken();

                // Check that the sentences have remained in sequence
                if (intMessageNumber == (i+1))
                    {
                    LOGGER.debug("[messagenumber=" + intMessageNumber + "]");

                    // Read the SatellitesInView count
                    this.intGPGSV_SatellitesInView = Integer.parseInt(stringTokenizer.nextToken());
                    LOGGER.debug("[satellitecount=" + intGPGSV_SatellitesInView + "]");
                    // Discard the trailing delimiter
                    stringTokenizer.nextToken();

                    // There's now a maxium of four sets of satellite data
                    // The checksum field also contains the last SNR (duh!)
                    // Empty fields contain one delimiter token

                    for (int j = 0; j < 4; j++)
                        {
//                        LOGGER.debug(StringLibrary.MARKER);
                        LOGGER.debug("[j=" + j + "]");

                        // Prepare to store the data for this satellite
                        GPGSVsatellite = new SatelliteData();

                        // Read the Satellite PRN number, or NMEA_DELIMITER if none present
                        strTemp = stringTokenizer.nextToken();

                        if (!strTemp.equals(NMEA_DELIMITER))
                            {
                            // Discard the trailing delimiter
                            stringTokenizer.nextToken();

                            // Save the PRN in this satellite's data structure
                            GPGSVsatellite.setSatellitePRN(Integer.parseInt(strTemp));
                            LOGGER.debug("[satellitePRN=" + strTemp + "]");

                            // Now read the Elevation
                            strTemp = stringTokenizer.nextToken();

                            if (!strTemp.equals(NMEA_DELIMITER))
                                {
                                // Discard the trailing delimiter
                                stringTokenizer.nextToken();

                                GPGSVsatellite.setElevation(Integer.parseInt(strTemp));
                                LOGGER.debug("[elevation=" + strTemp + "]");

                                // Now read the Azimuth
                                strTemp = stringTokenizer.nextToken();

                                if (!strTemp.equals(NMEA_DELIMITER))
                                    {
                                    // Discard the trailing delimiter
                                    stringTokenizer.nextToken();

                                    GPGSVsatellite.setAzimuth(Integer.parseInt(strTemp));
                                    LOGGER.debug("[azimuth=" + strTemp + "]");

                                    // Now read the SNR, checking for the last satellite field
                                    // which has a different syntax...
                                    if (j < 3)
                                        {
                                        strTemp = stringTokenizer.nextToken();

                                        if (!strTemp.equals(NMEA_DELIMITER))
                                            {
                                            // Discard the trailing delimiter
                                            stringTokenizer.nextToken();

                                            GPGSVsatellite.setSNR(Integer.parseInt(strTemp));
                                            LOGGER.debug("[snr=" + strTemp + "]");
                                            }
                                        else
                                            {
                                            // SNR is null, so not tracking this satellite
                                            // There is no trailing delimiter in this case
                                            // Set SNR = -1?

                                            GPGSVsatellite.setSNR(-1);
                                            LOGGER.debug("[snr=null]");
                                            }

                                        // Commit the data received
                                        LOGGER.debug("Commit data to vector");
                                        vecGPGSV_SatellitesInView.add(GPGSVsatellite);
                                        }
                                    else
                                        {
                                        // It is the last satellite in the group,
                                        // so we need to chop up the SNR and checksum
                                        strTemp = stringTokenizer.nextToken();

                                        // The SNR may be missing?!
                                        if (strTemp.substring(0, 1).equals(CHECKSUM_DELIMITER))
                                            {
                                            GPGSVsatellite.setSNR(-1);
                                            LOGGER.debug("[snr=null]");

                                            // There is no SNR information, so just read the checksum
                                            // from CHECKSUM_DELIMITER onwards
                                            strChecksum = strTemp.substring(1);
                                            }
                                        else
                                            {
                                            // We have an SNR and checksum, so retokenize using the checksum delimiter
                                            // <SNR><CHECKSUM_DELIMITER><CHECKSUM>  (do not return the delimiter)
                                            stringTokenizer = new StringTokenizer(strTemp, CHECKSUM_DELIMITER);

                                            // Read the SNR
                                            strTemp = stringTokenizer.nextToken();
                                            GPGSVsatellite.setSNR(Integer.parseInt(strTemp));
                                            LOGGER.debug("[snr=" + strTemp + "]");

                                            // Read the checksum
                                            strChecksum = stringTokenizer.nextToken();
                                            }

                                        LOGGER.debug("[strchecksum=" + strChecksum + "]");
                                        intChecksum = Integer.parseInt(strChecksum, RADIX_16);
                                        LOGGER.debug("[checksum=" + Integer.toHexString(intChecksum) + "]");

                                        // ToDo Check the checksum...
                                        if (true)
                                            {
                                            // Commit the data received
                                            LOGGER.debug("Commit data to vector");
                                            vecGPGSV_SatellitesInView.add(GPGSVsatellite);
                                            }
                                        else
                                            {
                                            // Checksum delimiter invalid (very unlikely)
                                            LOGGER.debug("Invalid checksum");
                                            return(false);
                                            }
                                        }
                                    }
                                else
                                    {
                                    // Something's wrong - delimiter where Azimuth should be!
                                    // Leave the loop early...
                                    LOGGER.debug("[azimutherror=" + strTemp + "]");
                                    return(false);
                                    }
                                }
                            else
                                {
                                // Something's wrong - delimiter where Elevation should be!
                                // Leave the loop early...
                                LOGGER.debug("[elevationerror=" + strTemp + "]");
                                return(false);
                                }
                            }
                        else
                            {
                            // The PRN field was null, so skip all fields for this satellite
                            // Only ONE NMEA_DELIMITER expected per empty field
                            LOGGER.debug("Skipping null PRN");

                            // Elevation
                            stringTokenizer.nextToken();
                            LOGGER.debug("Skipping null Elevation");

                            // Azimuth
                            stringTokenizer.nextToken();
                            LOGGER.debug("Skipping null Azimuth");

                            // SNR, checking for the last satellite (includes checksum)
                            strTemp = stringTokenizer.nextToken();

                            if (j < 3)
                                {
                                // Assume the SNR is null
                                LOGGER.debug("Skipping null SNR");
                                }
                            else
                                {
                                // The SNR may not be missing?!
                                if (strTemp.substring(0, 1).equals(CHECKSUM_DELIMITER))
                                    {
                                    LOGGER.debug("[snr=null]");

                                    // There is no SNR information, so just read the checksum
                                    strChecksum = strTemp.substring(1);
                                    }
                                else
                                    {
                                    // We have an SNR and checksum, so retokenize using the checksum delimiter
                                    stringTokenizer = new StringTokenizer(strTemp, CHECKSUM_DELIMITER);

                                    // Read the SNR, but discard because it is not valid data?
                                    strTemp = stringTokenizer.nextToken();
                                    LOGGER.debug("[snr=" + strTemp + "]");

                                    // Read the checksum
                                    strChecksum = stringTokenizer.nextToken();
                                    }

                                LOGGER.debug("[strchecksum=" + strChecksum + "]");
                                intChecksum = Integer.parseInt(strChecksum, RADIX_16);
                                LOGGER.debug("[checksum=" + Integer.toHexString(intChecksum) + "]");

                                // ToDO Check the checksum...
                                if (false)
                                    {
                                    // Checksum delimiter invalid (very unlikely)
                                    LOGGER.debug("Invalid checksum");
                                    return(false);
                                    }

                                // Otherwise just leave the loop, there is no data to saveEventLog
                                }
                            }
                        }
                    }
                else
                    {
                    // Leave the loop early...
                    LOGGER.debug("[messagenumber=" + intMessageNumber + "] [expected=" + (i+1) + "]");
                    return(false);
                    }
                }
            else
                {
                // Leave the loop early...
                LOGGER.debug("[messagecount=" + intMessageCount + "] [expected=" + count + "]");
                return(false);
                }
            }

        // Everything worked Ok!
//        LOGGER.debug(StringLibrary.MARKER);
        LOGGER.debug("$GPGSV decoded successfully");

        enumGPGSV = vecGPGSV_SatellitesInView.elements();

        while (enumGPGSV.hasMoreElements())
            {
            GPGSVsatellite = (SatelliteData)enumGPGSV.nextElement();
            LOGGER.debug("[satellitedata="
                             + GPGSVsatellite.getSatellitePRN() + ", "
                             + GPGSVsatellite.getElevation() + ", "
                             + GPGSVsatellite.getAzimuth() + ", "
                             + GPGSVsatellite.getSNR() + "] ");
            }

        return(true);
        }


    //--------------------------------------------------------------------------
    // RS232GpsReceiver Utilities
    //--------------------------------------------------------------------------
    // Get the name of the RS232GpsReceiver Receiver

    public final String getReceiverName()
        {
        return(this.strReceiver);
        }


    /***********************************************************************************************
     * Get the PortOwner.
     *
     * @return String
     */

    private String getPortOwner()
        {
        return (this.strPortOwner);
        }


    /***********************************************************************************************
     * Open the specified serial port if possible.
     *
     * @param requestedport
     *
     * @return SerialPort
     */

    private SerialPort openSerialPort(final CommPortIdentifier requestedport)
        {
        String strSerialPortName;
        SerialPort port;

        if (requestedport == null)
            {
            throw new GpsException(EXCEPTION_PARAMETER_NULL);
            }

        strSerialPortName = "uninitialised";
        port = null;

        try
            {
            LOGGER.debug("openSerialPort() Saved owner " + getPortOwner());

            if (requestedport.isCurrentlyOwned())
                {
                LOGGER.debug("openSerialPort() ERROR!");
                LOGGER.debug("openSerialPort() Port is owned by " + requestedport.getCurrentOwner());
                }
            else
                {
                LOGGER.debug("openSerialPort() Port NOT owned, so going to try to open it for " + getPortOwner());
                LOGGER.debug(requestedport.toString());
                }

            port = (SerialPort) requestedport.open(getPortOwner(), OPEN_TIMEOUT);

            System.out.println("Opened port for owner, got here.....");
            port.enableReceiveThreshold(RECEIVE_THRESHOLD);
            port.setInputBufferSize(BUFFER_SIZE);
            strSerialPortName = port.getName();

            // Note that the Operating System might not accurately return the buffer size

//            if (port.getInputBufferSize() != BUFFERSIZE)
//                {
//                LOGGER.debug("Buffer size error 1 [size=" + port.getInputBufferSize() + "]");
//
//                // Have another go before giving up
//                port.setInputBufferSize(BUFFERSIZE);
//
//                if (port.getInputBufferSize() != BUFFERSIZE)
//                    {
//                    LOGGER.debug("Buffer size error 2 [size=" + port.getInputBufferSize() + "]");
//                    port.close();
//                    throw new GpsException(EXCEPTION_INVALID_BUFFERSIZE + " [size=" + port.getInputBufferSize() + "]");
//                    }
//                }

//            LOGGER.debug("Port opened with input buffer size of " + port.getInputBufferSize());

            //----------------------------------------------------------------------
            // All error exits after here must close the serial port!
            // Try to set the Locator serial port configuration

            // Save the original port configuration

//            LOGGER.debug("Requested BaudRate=" + intBaudrate);
//            LOGGER.debug("Requested Databits=" + intDatabits);
//            LOGGER.debug("Requested Stopbits=" + intStopbits);
//            LOGGER.debug("Requested Parity=" + intParity);

            port.setSerialPortParams(intBaudrate,
                                     intDatabits,
                                     intStopbits,
                                     intParity);

//            LOGGER.debug("Set BaudRate=" + port.getBaudRate());
//            LOGGER.debug("Set Databits=" + port.getDataBits());
//            LOGGER.debug("Set Stopbits=" + port.getStopBits());
//            LOGGER.debug("Set Parity=" + port.getParity());

            // Now set the flow control
//            LOGGER.debug("FlowControl=" + strFlowControl);

            // Set no flow control if no others found
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            if (strFlowControl.equals("None"))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                }

            if (strFlowControl.equals("XonXoff"))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
                }

            if (strFlowControl.equals("RtsCts"))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                }
            }

        catch(PortInUseException exception)
            {
            exception.printStackTrace();
            if (port != null)
                {
                port.close();
                }

            throw new GpsException(EXCEPTION_INUSE_GPSPORT
                                        + " [openSerialPort] [PortInUseException="
                                        + exception.getMessage()
                                        + "]",
                                   exception);
            }

        catch(UnsupportedCommOperationException exception)
            {
            if (port != null)
                {
                port.close();
                }

            throw new GpsException(EXCEPTION_COMMSMODE_GPS
                                        + " [openSerialPort] [UnsupportedCommOperationException="
                                        + exception.getMessage()
                                        + "]",
                                   exception);
            }

        // If we get this far, the port was configured and opened correctly
        LOGGER.debug(strSerialPortName + "openSerialPort() Port configured and opened correctly");
        return(port);
        }


    /***********************************************************************************************
     * Try to get the input stream the serial port uses for input.
     *
     * @param port
     *
     * @return InputStream
     */

    private InputStream getInputStream(final SerialPort port)
        {
        final InputStream stream;

        if (port == null)
            {
            throw new GpsException(EXCEPTION_PARAMETER_NULL);
            }

        try
            {
            stream = port.getInputStream();
            }

        catch(IOException exception)
            {
            port.close();
            throw new GpsException(EXCEPTION_NOSTREAM_GPS
                                        + " [IOException="
                                        + exception.getMessage()
                                        + "]",
                                   exception);
            }

        return(stream);
        }


    //--------------------------------------------------------------------------
    // Get the list of supported NMEA sentences

    public final Enumeration getNMEASentences()
        {
        final Vector<String> vecNMEASupport;

        if (GARMINGPS35PC.equals(this.strReceiver))
            {
            vecNMEASupport = new Vector<String>(4);
            vecNMEASupport.add("$GPRMC");
            vecNMEASupport.add("$GPGGA");
            vecNMEASupport.add("$GPGSA");
            vecNMEASupport.add("$GPGSV");

            return(vecNMEASupport.elements());
            }
        else
            {
            return(null);
            }
        }


    //--------------------------------------------------------------------------
    // Set the DateOfLastUpdate
    // Private use only, so don't parse the Date to check it

    private void setDateOfLastUpdate(final String dateofupdate)
        {
        this.strDateOfLastUpdate = dateofupdate;
        }


    //--------------------------------------------------------------------------
    // Get the DateOfLastUpdate

    public final String getDateOfLastUpdate()
        {
        return(this.strDateOfLastUpdate);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the Date of the Fix

    public final YearMonthDayInterface getDateOfFix()
        {
        ymdDateOfFix.enableFormatSign(false);
        return(this.ymdDateOfFix);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the Time of the Fix

    public final HourMinSecInterface getTimeOfFix()
        {
        hmsTimeOfFix.enableFormatSign(false);
        return(this.hmsTimeOfFix);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the Latitude of the place of observation

    public final DegMinSecInterface getLatitude()
        {
        return(this.dmsLatitude);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the Longitude of the place of observation

    public final DegMinSecInterface getLongitude()
        {
        return(this.dmsLongitude);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the Speed in Knots

    public final double getSpeedKnots()
        {
        return(this.dblSpeedKnots);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the Course in Degrees

    public final double getCourse()
        {
        return(this.dblCourseDegrees);
        }


    //--------------------------------------------------------------------------
    // GPRMC: Get the MagneticVariation

    public final double getMagneticVariation()
        {
        return(this.dblMagneticVariation);
        }


    //--------------------------------------------------------------------------
    // GPGGA: Get the Data Quality

    public final int getDataQuality()
        {
        return(this.intDataQuality);
        }


    //--------------------------------------------------------------------------
    // GPGGA: Get the Altitude above sea level

    public final double getAltitudeASL()
        {
        return(this.dblAltitudeASL);
        }


    //--------------------------------------------------------------------------
    // GPGGA: Get the Altitude above the reference Ellipsoid

    public final double getGeoidAltitude()
        {
        return(this.dblGeoidAltitude);
        }


    //--------------------------------------------------------------------------
    // GPGSA: Get the Fix Mode

    public final String getFixMode()
        {
        return(this.strFixMode);
        }


    //--------------------------------------------------------------------------
    // GPGSA: Get the Fix Type

    public final int getFixType()
        {
        return(this.intFixType);
        }


    //--------------------------------------------------------------------------
    // GPGSA: Get the PDOP

    public final double getPDOP()
        {
        return(this.dblPDOP);
        }


    //--------------------------------------------------------------------------
    // GPGSA: Get the HDOP

    public final double getHDOP()
        {
        return(this.dblHDOP);
        }


    //--------------------------------------------------------------------------
    // GPGSA: Get the VDOP

    public final double getVDOP()
        {
        return(this.dblVDOP);
        }


    //--------------------------------------------------------------------------
    // GPGGA: Get the number of satellites in Use

    public final int getSatellitesInUse()
        {
        return(this.intSatellitesInUse);
        }


    //--------------------------------------------------------------------------
    // GPGSA: Get the IDs of the satellites in Use

    public final Enumeration getSatellitesInUseData()
        {
        return(this.vecSatellitesInUse.elements());
        }


    //--------------------------------------------------------------------------
    // GPGSV: Get the number of satellites in View

    public final int getSatellitesInView()
        {
        return(this.intSatellitesInView);
        }


    //--------------------------------------------------------------------------
    // GPGSV: Get the IDs etc. of the satellites in View

    public final Enumeration getSatellitesInViewData()
        {
        final Enumeration enumSatellitesInViewData;

        if (getSatellitesInView() > 0)
            {
            enumSatellitesInViewData = this.vecSatellitesInView.elements();

            return(enumSatellitesInViewData);
            }
        else
            {
            return(null);
            }
        }


    /***********************************************************************************************
     * This is the event handler for SerialPortEventListener.
     *
     * @param event
     */

    public final void serialEvent(final SerialPortEvent event)
        {
        // Ignore everything except DATA_AVAILABLE (for now)

        switch(event.getEventType())
            {
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
                // Ignore errors normally, just keep trying
                LOGGER.debug("Error reading data [eventtype=" + event.getEventType() + "]");
                break;
                }

            case SerialPortEvent.DATA_AVAILABLE:
                {
                try
                    {
                    while ((inputStream != null)
                        && (inputStream.available() > 0)
                        && (readBuffer != null))
                        {
                        // There are some bytes available, so accumulate them
                        readBuffer.append((char)inputStream.read());
                        }
                    }

                catch(IOException exception)
                    {
                    // Ignore errors normally, just keep trying
                    LOGGER.debug("IOException reading data from input stream");
                    }

                break;
                }

            default:
                {
                // Ignore errors normally, just keep trying
                LOGGER.debug("Unknown event type [eventtype=" + event.getEventType() + "]");
                break;
                }
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File

