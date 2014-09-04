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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.exceptions.HourMinSecException;
import org.lmn.fc.common.exceptions.YearMonthDayException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsInstrumentReceiverInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.DegMinSecFormat;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;
import org.lmn.fc.model.logging.EventStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//--------------------------------------------------------------------------------------------------
// NMEA is a standard protocol, used by GpsReceiver receivers to transmit data.
// NMEA output is EIA-422A but for most purposes you can consider it RS-232 compatible.
// Use 4800 bps, 8 data bits, no parity and one stop bit (8N1).
// NMEA 0183 sentences are all ASCII. Each sentence begins with a dollarsign ($)
// and ends with a carriage return linefeed (<CR><LF>).
// Data is comma delimited. All commas must be included as they act as markers.
// Some Garmin receivers do not send some of the fields.
// A checksum is optionally added (in a few cases it is mandatory).
// Following the $ is the address field aaccc.
// aa is the device id. Transmission of the device ID is usually optional.
// GP is used to identify GpsReceiver data.
// ccc is the sentence formatter, otherwise known as the sentence name.
//
// GPRMC: Get the Date of the Fix
// GPRMC: Get the Time of the Fix
// GPRMC: Get the Latitude of the place of observation
// GPRMC: Get the Longitude of the place of observation
// GPRMC: Get the Speed in Knots
// GPRMC: Get the Course in Degrees
// GPRMC: Get the MagneticVariation
// GPGGA: Get the Data Quality
// GPGGA: Get the Altitude above sea level
// GPGGA: Get the Altitude above the reference Ellipsoid
// GPGSA: Get the Fix Mode
// GPGSA: Get the Fix Type
// GPGSA: Get the PDOP
// GPGSA: Get the HDOP - Horizontal Dilution of Precision
// GPGSA: Get the VDOP- Vertical Dilution of Precision
// GPGGA: Get the number of satellites in Use
// GPGSA: Get the IDs of the satellites in Use
// GPGSV: Get the number of satellites in View
// GPGSV: Get the List of the satellites in View
//
/***************************************************************************************************
 * An AbstractNMEAGpsReceiver.
 */

public abstract class AbstractNMEAGpsReceiver implements GpsInstrumentReceiverInterface,
                                                         FrameworkConstants,
                                                         FrameworkStrings,
                                                         FrameworkMetadata,
                                                         FrameworkSingletons
    {
    private static final String EVENT_SOURCE = "NMEAGpsReceiver";

    // External data, combined from all NMEA sentences
    protected String strDateOfLastUpdate;         // The date on which the observation was made
    protected YearMonthDayInterface ymdDateOfFix; // YYYY:MM:DD of the fix
    protected HourMinSecInterface hmsTimeOfFix;   // HH:MM:SS of the fix
    protected DegMinSecInterface dmsLatitude;     // Latitude of this place
    protected DegMinSecInterface dmsLongitude;    // Longitude of this place
    protected double dblSpeedKnots;               // The speed of the receiver, in knots
    protected double dblCourseDegrees;            // The course of the receiver, in degrees from North
    protected double dblMagneticVariation;        // The Magnetic Variation, in degrees West
    protected int intDataQuality;                 // The fix quality
    protected double dblAltitudeASL;              // Altitude Above Sea Level
    protected double dblGeoidAltitude;            // Height of geoid above WGS84 ellipsoid
    protected String strFixMode;                  // M=manual, A=automatic (2D/3D)
    protected int intFixType;                     // 1=nofix, 2=2D, 3=3D
    protected double dblPDOP;                     //
    protected double dblHDOP;                     // Horizontal Dilution of Precision
    protected double dblVDOP;                     // Vertical Dilution of Precision

    protected int intSatellitesInUseCount;                  // Number of satellites used for the fix
    protected List<String> listSatellitesInUse;             // IDs of satellites in use

    protected int intSatellitesInViewCount;                 // Number of satellites in view
    protected List<SatelliteData> listSatellitesInView;     // IDs etc. of satellites in view

    // Injections
    private final ObservatoryInstrumentDAOInterface hostDAO;
    private final String strReceiverType;
    private final String strResourceKey;

    private boolean boolStarted;
    private boolean boolMessageNotShown;


    /***********************************************************************************************
     * Construct a AbstractNMEAGpsReceiver.
     *
     * @param dao
     * @param rxtype
     * @param resourcekey
     */

    public AbstractNMEAGpsReceiver(final ObservatoryInstrumentDAOInterface dao,
                                   final String rxtype,
                                   final String resourcekey)
        {
        if ((rxtype == null)
            || (EMPTY_STRING.equals(rxtype))
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.hostDAO = dao;
        this.strReceiverType = rxtype;
        this.strResourceKey = resourcekey;

        this.boolStarted = false;
        this.boolMessageNotShown = true;

        initialiseNMEAData();
        }


    /***********************************************************************************************
     * Initialise all GpsReceiver NMEA data.
     */

    protected void initialiseNMEAData()
        {
        this.strDateOfLastUpdate = EMPTY_STRING;

        this.ymdDateOfFix = new YearMonthDayDataType();
        this.hmsTimeOfFix = new HourMinSecDataType();

        this.dmsLatitude = new LatitudeDataType();
        this.dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

        this.dmsLongitude = new LongitudeDataType();
        this.dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);

        this.dblSpeedKnots = 0.0;
        this.dblCourseDegrees = 0.0;
        this.dblMagneticVariation = 0.0;
        this.intDataQuality = 0;
        this.dblAltitudeASL = 0.0;
        this.dblGeoidAltitude = 0.0;
        this.strFixMode = VALIDITY_A;
        this.intFixType = 0;
        this.dblPDOP = 0.0;
        this.dblHDOP = 0.0;
        this.dblVDOP = 0.0;

        this.intSatellitesInUseCount = 0;
        this.intSatellitesInViewCount = 0;

        this.listSatellitesInUse = new ArrayList<String>(MAX_SATELLITES);
        this.listSatellitesInView = new ArrayList<SatelliteData>(MAX_SATELLITES);
        }


    /***********************************************************************************************
     * Get the host ObservatoryInstrumentDAO for this Receiver.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public final ObservatoryInstrumentDAOInterface getHostDAO()
        {
        return (this.hostDAO);
        }


    /***********************************************************************************************
     * Get the type of the GpsReceiver.
     *
     * @return String
     */

    public final String getReceiverType()
        {
        return(this.strReceiverType);
        }


    /***********************************************************************************************
     * Initialise the GPS Receiver.
     *
     * @return boolean
     */

    public abstract boolean initialise();


    /***********************************************************************************************
     * Start the Gps Receiver.
     *
     * @return boolean
     */

    public abstract boolean start();


    /***********************************************************************************************
     * Stop the Gps Receiver.
     * Do not clear the read buffer! It is needed for parsing
     *
     * @return boolean
     */

    public abstract boolean stop();


    /***********************************************************************************************
     * Dispose the GPS Receiver.
     */

    public abstract void dispose();


    /***********************************************************************************************
     * Indicate if this Receiver has been started.
     *
     * @return boolean
     */

    public boolean isStarted()
        {
        return (this.boolStarted);
        }


    /***********************************************************************************************
     * Control the state of the Receiver.
     *
     * @param state
     */

    public void setStarted(final boolean state)
        {
        this.boolStarted = state;
        }


    /***********************************************************************************************
     * Get the list of NMEA sentences supported by this GpsReceiver.
     *
     * @return List<String>
     */

    public final List<String> getNMEASentences()
        {
        final List<String> listNMEASupport;

        listNMEASupport = new ArrayList<String>(10);

        if ((NMEA0183_GPS_RX.equals(getReceiverType()))
            || (NMEA0183_GPS_STATIC.equals(getReceiverType())))
            {
            listNMEASupport.addAll(GpsReceiverNMEAParsers.getNMEASentences());
            }

        return (listNMEASupport);
        }


     /***********************************************************************************************
      * Parse the NMEA sentences.
      * This may be overridden if necessary.
      * Parse out each NMEA sentence in order $GPRMC->$GPGGA->$GPGSA->$GPGSV.
      * ToDo Needs some serious work to improve reliability.
      *
      * @param nmea
      * @param clock
      *
      * @return boolean
      */

     protected boolean parseNMEA(final String nmea,
                                 final ObservatoryClockInterface clock)
         {
         boolean boolNMEAValid;

         // Prepare to fail...
         boolNMEAValid = false;

         try
             {
             final GPGSVData dataGPGSV;
             final GPRMCData dataGPRMC;
             final GPGGAData dataGPGGA;
             final GPGSAData dataGPGSA;
             int intChecksumPointer;
             int intNMEAPointer;
             boolean bool$GPRMCValid;
             String str$GPRMC;
             boolean bool$GPGGAValid;
             String str$GPGGA;
             boolean bool$GPGSAValid;
             String str$GPGSA;
             final Vector<String> vec$GPGSV;
             int intGPGSVCounter;
             boolean bool$GPGSVValid;
             int intGPGSVIndex;
             final int intGPGSVSetCount;

             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "AbstractNMEAGpsReceiver.parseNMEA()");

             // Start with initialised data every time
             initialiseNMEAData();
             intChecksumPointer = 0;

             //-----------------------------------------------------------------------------------------
             // Search for the $GPRMC sentence

             bool$GPRMCValid = false;
             str$GPRMC = EMPTY_STRING;
             intNMEAPointer = nmea.indexOf(SENTENCE_GPRMC);

             if (intNMEAPointer != -1)
                 {
                 intChecksumPointer = nmea.indexOf(CHECKSUM_DELIMITER, intNMEAPointer);

                 if (intChecksumPointer != -1)
                     {
                     str$GPRMC = nmea.substring(intNMEAPointer, intChecksumPointer + 3);
                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          "[$GPRMCsentence=" + str$GPRMC + "]");
                     bool$GPRMCValid = true;
                     }
                 }

             //-----------------------------------------------------------------------------------------
             // Search for the $GPGGA sentence

             bool$GPGGAValid = false;
             str$GPGGA = EMPTY_STRING;
             intNMEAPointer = nmea.indexOf(SENTENCE_GPGGA);

             if (intNMEAPointer != -1)
                 {
                 intChecksumPointer = nmea.indexOf(CHECKSUM_DELIMITER, intNMEAPointer);

                 if (intChecksumPointer != -1)
                     {
                     str$GPGGA = nmea.substring(intNMEAPointer, intChecksumPointer + 3);
                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          "[$GPGGAsentence=" + str$GPGGA + "]");
                     bool$GPGGAValid = true;
                     }
                 }

             //-----------------------------------------------------------------------------------------
             // Search for the $GPGSA sentence

             bool$GPGSAValid = false;
             str$GPGSA = EMPTY_STRING;
             intNMEAPointer = nmea.indexOf(SENTENCE_GPGSA);

             if (intNMEAPointer != -1)
                 {
                 intChecksumPointer = nmea.indexOf(CHECKSUM_DELIMITER, intNMEAPointer);

                 if (intChecksumPointer != -1)
                     {
                     str$GPGSA = nmea.substring(intNMEAPointer, intChecksumPointer + 3);
                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          "[$GPGSAsentence=" + str$GPGSA + "]");
                     bool$GPGSAValid = true;
                     }
                 }

             //-----------------------------------------------------------------------------------------
             // Search for the $GPGSV sentences
             // There may be several in the same set {1...n} but the order doesn't matter

             vec$GPGSV = new Vector<String>(10);
             intNMEAPointer = nmea.indexOf(SENTENCE_GPGSV);
             intGPGSVCounter = 0;
             bool$GPGSVValid = false;

             if (intNMEAPointer != -1)
                 {
                 intChecksumPointer = nmea.indexOf(CHECKSUM_DELIMITER, intNMEAPointer);

                 if (intChecksumPointer != -1)
                     {
                     String strTest;

                     // We have found one sentence (including checksum),
                     // so add it to the vector regardless...
                     intGPGSVCounter = 1;
                     vec$GPGSV.add(nmea.substring(intNMEAPointer, intChecksumPointer + 3));

                     // Read how many messages of this type should occur in this cycle
                     // This is in character 7 (assuming there are never more than 9 in the set)
                     strTest = vec$GPGSV.get(0);
                     intGPGSVSetCount = Integer.parseInt(strTest.substring(7, 8));
                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          "[$GPGSVset=" + intGPGSVSetCount + "]");

                     // Are there any more to collect?
                     if (intGPGSVSetCount > 1)
                         {
                         // There's more to read
                         // Any errors here will reset the valid flag

                         for (int i = 0;
                              i < intGPGSVSetCount - 1;
                              i++)
                             {
                             // Reset pointer to the beginning of the next sentence
                             intNMEAPointer = intChecksumPointer+3;
                             intNMEAPointer = nmea.indexOf(SENTENCE_GPGSV, intNMEAPointer);

                             // Did we find another one?
                             if (intNMEAPointer != -1)
    //                             && (intNMEAPointer <= intChecksumPointer + 10))
                                 {
                                 // Make sure that the sentence has a checksum
                                 intChecksumPointer = nmea.indexOf(CHECKSUM_DELIMITER, intNMEAPointer);

                                 if (intChecksumPointer != -1)
                                     {
                                     // Check that this sentence is the next in the series
                                     // The index is in character 9 (assuming there are never more than 9 in the set)
                                     strTest = nmea.substring(intNMEAPointer, intChecksumPointer + 3);
    //                                 intGPGSVIndex = Integer.parseInt(strTest.substring(9, 10));
    //
    //                                 if (intGPGSVIndex == (i + 2))
    //                                     {
                                         // We seem to have found a valid sentence, so add it to the vector
                                         intGPGSVCounter++;
                                         vec$GPGSV.add(strTest);
                                         bool$GPGSVValid = true;
    //                                     }
    //                                 else
    //                                     {
    //                                     // The sentence found doesn't fit the series expected
    //                                     bool$GPGSVValid = false;
    //                                     }
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

                     // Show all sentences found
                     for (int i = 0; i < intGPGSVCounter; i++)
                         {
                         LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                              "[$GPGSVsentence(" + (i+1) + ")=" + vec$GPGSV.get(i) + "]");
                         }
                     }
                 }

             //-----------------------------------------------------------------------------------------
             // Show these messages once only. to avoid cluttering the Log

             if (boolMessageNotShown)
                 {
                 if (!bool$GPRMCValid)
                     {
                     SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                        EventStatus.WARNING,
                                                        METADATA_TARGET + SENTENCE_GPRMC + TERMINATOR
                                                            + METADATA_ACTION_PARSE
                                                            + METADATA_RESULT_NOT_FOUND,
                                                        EVENT_SOURCE,
                                                        getHostDAO().getObservatoryClock());
                     }

                 if (!bool$GPGGAValid)
                     {
                     SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                        EventStatus.WARNING,
                                                        METADATA_TARGET + SENTENCE_GPGGA + TERMINATOR
                                                            + METADATA_ACTION_PARSE
                                                            + METADATA_RESULT_NOT_FOUND,
                                                        EVENT_SOURCE,
                                                        getHostDAO().getObservatoryClock());
                     }

                 if (!bool$GPGSAValid)
                     {
                     SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                        EventStatus.WARNING,
                                                        METADATA_TARGET + SENTENCE_GPGSA + TERMINATOR
                                                            + METADATA_ACTION_PARSE
                                                            + METADATA_RESULT_NOT_FOUND,
                                                        EVENT_SOURCE,
                                                        getHostDAO().getObservatoryClock());
                     }

                 if (!bool$GPGSVValid)
                     {
                     SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                        EventStatus.WARNING,
                                                        METADATA_TARGET + SENTENCE_GPGSV + TERMINATOR
                                                            + METADATA_ACTION_PARSE
                                                            + METADATA_RESULT_NOT_FOUND,
                                                        EVENT_SOURCE,
                                                        getHostDAO().getObservatoryClock());
                     }

                 boolMessageNotShown = false;
                 }

             //-----------------------------------------------------------------------------------------
             // If **at least one** sentence was parsed correctly,
             // try to decode each sentence to get at the data

             if (bool$GPGGAValid
                 || bool$GPGSAValid
                 || bool$GPRMCValid
                 || bool$GPGSVValid)
                 {
                 LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                      "");
                 LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                      "Decoding NMEA");

                 // Just return the default NMEA data if the sentences were not found
                 dataGPRMC = GpsReceiverNMEAParsers.parseGPRMC(str$GPRMC, bool$GPRMCValid,
                                                               getHostDAO().getHostInstrument().isDebugMode());
                 dataGPGGA = GpsReceiverNMEAParsers.parseGPGGA(str$GPGGA, bool$GPGGAValid,
                                                               getHostDAO().getHostInstrument().isDebugMode());
                 dataGPGSA = GpsReceiverNMEAParsers.parseGPGSA(str$GPGSA, bool$GPGSAValid,
                                                               getHostDAO().getHostInstrument().isDebugMode());
                 dataGPGSV = GpsReceiverNMEAParsers.parseGPGSV(vec$GPGSV, intGPGSVCounter, bool$GPGSVValid,
                                                               getHostDAO().getHostInstrument().isDebugMode());

                 // See if anything was updated
                 if (!dataGPRMC.isUpdated()
                     && !dataGPGGA.isUpdated()
                     && !dataGPGSA.isUpdated()
                     && !dataGPGSV.isUpdated())
                     {
                     // All decodes failed, so not worth using any data
                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          "Decoding errors encountered");

                     SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                        EventStatus.WARNING,
                                                        METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                            + METADATA_ACTION_PARSE
                                                            + METADATA_RESULT + MSG_TOO_MANY_ERRORS + TERMINATOR,
                                                        EVENT_SOURCE,
                                                        getHostDAO().getObservatoryClock());
                     }
                 else
                     {
                     // If we had at least one valid set, then use the data

                     // GPGGA_TimeStamp; GPRMC takes precedence
                     // There is no GPGGA DateStamp, so we must use GPRMC for Date and Time
                     this.ymdDateOfFix = dataGPRMC.getDateStamp();
                     ymdDateOfFix.enableFormatSign(false);

                     this.hmsTimeOfFix = dataGPRMC.getTimeStamp();
                     hmsTimeOfFix.enableFormatSign(false);

                     // GPGGA_Latitude; GPRMC takes precedence
                     // GPGGA_Longitude; GPRMC takes precedence
                     // Latitude and Longitude formats default to SIGN
                     if (dataGPRMC.isUpdated())
                         {
                         this.dmsLatitude = new LatitudeDataType(dataGPRMC.getLatitude());
                         this.dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

                         this.dmsLongitude = new LongitudeDataType(dataGPRMC.getLongitude());
                         this.dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
                         }
                     else if (dataGPGGA.isUpdated())
                         {
                         this.dmsLatitude = new LatitudeDataType(dataGPGGA.getLatitude());
                         this.dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

                         this.dmsLongitude = new LongitudeDataType(dataGPGGA.getLongitude());
                         this.dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
                         }
                     else
                         {
                         // Just in case!
                         this.dmsLatitude = new LatitudeDataType(0.0);
                         this.dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

                         this.dmsLongitude = new LongitudeDataType(0.0);
                         this.dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);
                         }

                     this.dblSpeedKnots = dataGPRMC.getSpeed();
                     this.dblCourseDegrees = dataGPRMC.getCourse();
                     this.dblMagneticVariation = dataGPRMC.getVariation();

                     // WARNING! Some Rx do not provide $GPGGA sentences
                     if (dataGPGGA.isUpdated())
                         {
                         this.intDataQuality = dataGPGGA.getFixQuality();
                         }
                     else
                         {
                         // Simulate a good fix
                         this.intDataQuality  = 1;
                         }

                     this.strFixMode = dataGPGSA.getFixMode();
                     this.intFixType = dataGPGSA.getFixType();

                     this.dblPDOP = dataGPGSA.getPDOP();
                     this.dblVDOP = dataGPGSA.getVDOP();

                     // GPGGA_HDOP; GPGSA takes precedence
                     if (dataGPGSA.isUpdated())
                         {
                         this.dblHDOP = dataGPGSA.getHDOP();
                         }
                     else if (dataGPGGA.isUpdated())
                         {
                         this.dblHDOP = dataGPGGA.getHDOP();
                         }
                     else
                         {
                         this.dblHDOP = 0.0;
                         }

                     this.listSatellitesInUse = dataGPGSA.getSatellitesInUse();

                     // There might not be a GPGGA sentence
                     if (dataGPGGA.isUpdated())
                         {
                         this.intSatellitesInUseCount = dataGPGGA.getSatellitesInUseCount();
                         }
                     else if (dataGPGSA.isUpdated())
                         {
                         this.intSatellitesInUseCount = dataGPGSA.getSatellitesInUse().size();
                         }
                     else
                         {
                         this.intSatellitesInUseCount = 0;
                         }

                     // GPGSV
                     this.intSatellitesInViewCount = dataGPGSV.getSatellitesInView().size();
                     this.listSatellitesInView = dataGPGSV.getSatellitesInView();

                     // Only update altitude if we have four or more satellites
                     if ((intSatellitesInUseCount > 3)
                         && (intSatellitesInViewCount > 3))
                         {
                         this.dblAltitudeASL = dataGPGGA.getAltitude();
                         this.dblGeoidAltitude = dataGPGGA.getGeoid();
                         }

                     setDateOfLastUpdate(clock.getDateTimeNowAsString());
                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          GpsInstrumentReceiverInterface.LINE);

                     // This is the only valid exit!
                     boolNMEAValid = ((getFixType() > 1)
                                      && (getDataQuality() > 0));

                     LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                          "NMEA Decoding completed successfully at " + getDateOfLastUpdate()
                                          + " [validfix=" + boolNMEAValid + "]");
                     }
                 }
             else
                 {
                 LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                      "Parsing errors encountered, nothing to decode");
                 SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                    EventStatus.WARNING,
                                                    METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                        + METADATA_ACTION_PARSE
                                                        + METADATA_RESULT + MSG_TOO_MANY_ERRORS + TERMINATOR,
                                                    EVENT_SOURCE,
                                                    getHostDAO().getObservatoryClock());
                 }
             }

         //----------------------------------------------------------------------------------------
         // These are all fatal errors which must stop the parsing

         catch (NullPointerException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "NullPointerException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (NumberFormatException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "NumberFormatException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (DegMinSecException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "DegMinSecException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (YearMonthDayException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "YearMonthDayException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (HourMinSecException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "HourMinSecException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (StringIndexOutOfBoundsException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "StringIndexOutOfBoundsException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (ArrayIndexOutOfBoundsException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "ArrayIndexOutOfBoundsException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         catch (IndexOutOfBoundsException exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "IndexOutOfBoundsException, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         // The ultimate catch-all. We don't want to pass exceptions, only a flag.
         catch (Exception exception)
             {
             LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                                  "Generic Exception, nothing to decode");
             SimpleEventLogUIComponent.logEvent(getHostDAO().getEventLogFragment(),
                                                EventStatus.FATAL,
                                                METADATA_TARGET + NMEA0183_GPS_RX + TERMINATOR
                                                    + METADATA_ACTION_PARSE
                                                    + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                EVENT_SOURCE,
                                                getHostDAO().getObservatoryClock());
             }

         return (boolNMEAValid);
         }


    /**********************************************************************************************/
    /* Data returned by the GpsReceiver                                                           */
    /***********************************************************************************************
     * Get the DateOfLastUpdate.
     *
     * @return String
     */

    public final String getDateOfLastUpdate()
        {
        return(this.strDateOfLastUpdate);
        }


    /***********************************************************************************************
     * Set the DateOfLastUpdate.
     * Private use only, so don't parse the Date to check it.
     *
     * @param dateofupdate
     */

     private void setDateOfLastUpdate(final String dateofupdate)
         {
         this.strDateOfLastUpdate = dateofupdate;
         }


    /***********************************************************************************************
     * GPRMC: Get the Date of the Fix.
     *
     * @return YearMonthDayInterface
     */

    public final YearMonthDayInterface getDateOfFix()
        {
        ymdDateOfFix.enableFormatSign(false);

        return(this.ymdDateOfFix);
        }


    /***********************************************************************************************
     * GPRMC: Get the Time of the Fix.
     *
     * @return HourMinSecInterface
     */

    public final HourMinSecInterface getTimeOfFix()
        {
        hmsTimeOfFix.enableFormatSign(false);

        return(this.hmsTimeOfFix);
        }


    /***********************************************************************************************
     * GPRMC: Get the Latitude of the place of observation.
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getLatitude()
        {
        return(this.dmsLatitude);
        }


    /***********************************************************************************************
     * GPRMC: Get the Longitude of the place of observation.
     *
     * @return DegMinSec
     */

    public final DegMinSecInterface getLongitude()
        {
        return(this.dmsLongitude);
        }


    /***********************************************************************************************
     * GPRMC: Get the Speed in Knots.
     *
     * @return double
     */

    public final double getSpeedKnots()
        {
        return(this.dblSpeedKnots);
        }


    /***********************************************************************************************
     * GPRMC: Get the Course in Degrees.
     *
     * @return double
     */

    public final double getCourse()
        {
        return(this.dblCourseDegrees);
        }


    /***********************************************************************************************
     * GPRMC: Get the MagneticVariation.
     *
     * @return double
     */

    public final double getMagneticVariation()
        {
        return(this.dblMagneticVariation);
        }


    /***********************************************************************************************
     * GPGGA: Get the Data Quality.
     *
     * @return int
     */

    public final int getDataQuality()
        {
        return(this.intDataQuality);
        }


    /***********************************************************************************************
     * GPGGA: Get the Altitude above sea level.
     *
     * @return double
     */

    public final double getAltitudeASL()
        {
        return(this.dblAltitudeASL);
        }


    /***********************************************************************************************
     * GPGGA: Get the Altitude above the reference Ellipsoid.
     *
     * @return double
     */

    public final double getGeoidAltitude()
        {
        return(this.dblGeoidAltitude);
        }


    /***********************************************************************************************
     * GPGSA: Get the Fix Mode.
     *
     * @return String
     */

    public final String getFixMode()
        {
        return(this.strFixMode);
        }


    /***********************************************************************************************
     * GPGSA: Get the Fix Type.
     *
     * @return int
     */

    public final int getFixType()
        {
        return(this.intFixType);
        }


    /***********************************************************************************************
     * GPGSA: Get the PDOP.
     *
     * @return double
     */

    public final double getPDOP()
        {
        return(this.dblPDOP);
        }


    /***********************************************************************************************
     * GPGSA: Get the HDOP.
     *
     * @return double
     */

    public final double getHDOP()
        {
        return(this.dblHDOP);
        }


    /***********************************************************************************************
     * GPGSA: Get the VDOP.
     *
     * @return double
     */

    public final double getVDOP()
        {
        return(this.dblVDOP);
        }


    /***********************************************************************************************
     * GPGGA: Get the number of satellites in Use.
     *
     * @return int
     */

    public final int getSatellitesInUseCount()
        {
        return(this.intSatellitesInUseCount);
        }


    /***********************************************************************************************
     * GPGSA: Get the IDs of the satellites in Use.
     *
     * @return Enumeration
     */

    public final List<String> getSatellitesInUseIDs()
        {
        return(this.listSatellitesInUse);
        }


    /***********************************************************************************************
     * GPGSV: Get the number of satellites in View.
     *
     * @return int
     */

    public final int getSatellitesInViewCount()
        {
        return(this.intSatellitesInViewCount);
        }


    /***********************************************************************************************
     * GPGSV: Get the List of the satellites in View.
     *
     * @return Enumeration
     */

    public final List<SatelliteData> getSatellitesInView()
        {
        return(this.listSatellitesInView);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the GpsReceiver.
     */

    public abstract void readResources();
    }
