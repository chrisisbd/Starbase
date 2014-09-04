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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.receiver.SatelliteData;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;

import java.util.List;


/***************************************************************************************************
 * The GpsReceiverInterface.
 */

public interface GpsInstrumentReceiverInterface
    {
    // String Resources
    String SENTENCE_GPRMC = "$GPRMC";
    String SENTENCE_GPGGA = "$GPGGA";
    String SENTENCE_GPGSA = "$GPGSA";
    String SENTENCE_GPGSV = "$GPGSV";

    // Supported Receiver Types
    String NMEA0183_GPS_RX = "NMEA-0183";
    String NMEA0183_GPS_STATIC = "NMEA-0183-STATIC";


    String EXCEPTION_INVALID_GPSRX= "RS232GpsReceiver receiver type not supported";
    String MSG_TOO_MANY_ERRORS = "Too many decoding errors";
    String LINE = "------------------------------------------";
    String VALIDITY_A = "A";
    String HEMISPHERE_NORTH = "N";
    String HEMISPHERE_SOUTH = "S";
    String HEMISPHERE_EAST = "E";
    String HEMISPHERE_WEST = "W";
    String NMEA_DELIMITER = ",";
    String CHECKSUM_DELIMITER = "*";
    String REGEX_CHECKSUM_DELIMITER = "\\*";
    String UNITS_METRES = "M";
    // Mode indicator, (A=Autonomous, D=Differential, E=Estimated, N=Data not valid)
    String MODE_AUTONOMOUS = "A";
    String MODE_DIFFERENTIAL = "D";
    String MODE_ESTIMATED = "E";
    String MODE_NOTVALID = "N";

    // As stated by http://en.wikipedia.org/wiki/Global_Positioning_System  2008 December
    // with a couple of extra ones for the high resolution new satellites?!
    int MAX_SATELLITES = 35;
    int MAX_SATELLITES_IN_VIEW = 12;

    int MAX_MESSAGES = 50;
    int YEAR_OFFSET = 2000;
    int RADIX_16 = 16;
    double MINUTES_PER_DEGREE = 60.0;


    /***********************************************************************************************
     * Get the host ObservatoryInstrumentDAO for this Receiver.
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    ObservatoryInstrumentDAOInterface getHostDAO();


    /***********************************************************************************************
     * Get the name of the ReceiverType.
     *
     * @return String
     */

    String getReceiverType();


    /***********************************************************************************************
     * Initialise the GPS Receiver.
     *
     * @return boolean
     */

    boolean initialise();


    /***********************************************************************************************
     * Start the Gps Receiver.
     *
     * @return boolean
     */

    boolean start();


    /***********************************************************************************************
     * Stop the Gps Receiver.
     * Do not clear the read buffer! It is needed for parsing
     *
     * @return boolean
     */

    boolean stop();


    /***********************************************************************************************
     * Dispose the GPS Receiver.
     */

    void dispose();


    /***********************************************************************************************
     * Indicate if this Receiver has been started.
     *
     * @return boolean
     */

    boolean isStarted();


    /***********************************************************************************************
     * Control the state of the Receiver.
     *
     * @param state
     */

    void setStarted(boolean state);


    /***********************************************************************************************
     * Get the list of supported NMEA sentences.
     *
     * @return List<String>
     */

    List<String> getNMEASentences();


    /***********************************************************************************************
     * Decode all NMEA sentences received in the specified time.
     *
     * @param capturetime
     * @param clock
     *
     * @return boolean
     */

    boolean decodeNMEA(int capturetime, ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Get the DateOfLastUpdate.
     *
     * @return String
     */

    String getDateOfLastUpdate();


    /***********************************************************************************************
     * GPRMC: Get the Date of the Fix.
     *
     * @return YearMonthDayInterface
     */

    YearMonthDayInterface getDateOfFix();


    /***********************************************************************************************
     * GPRMC: Get the Time of the Fix.
     *
     * @return HourMinSecInterface
     */

    HourMinSecInterface getTimeOfFix();


    /***********************************************************************************************
     * GPRMC: Get the Latitude of the place of observation.
     *
     * @return DegMinSec
     */

    DegMinSecInterface getLatitude();


    /***********************************************************************************************
     * GPRMC: Get the Longitude of the place of observation.
     *
     * @return DegMinSec
     */

    DegMinSecInterface getLongitude();


    /***********************************************************************************************
     * GPRMC: Get the Speed in Knots.
     *
     * @return double
     */

    double getSpeedKnots();


    /***********************************************************************************************
     * GPRMC: Get the Course in Degrees.
     *
     * @return double
     */

    double getCourse();


    /***********************************************************************************************
     * GPRMC: Get the MagneticVariation.
     *
     * @return double
     */

    double getMagneticVariation();


    /***********************************************************************************************
     * GPGGA: Get the Data Quality.
     *
     * @return int
     */

    int getDataQuality();


    /***********************************************************************************************
     * GPGGA: Get the Altitude above sea level.
     *
     * @return double
     */

    double getAltitudeASL();


    /***********************************************************************************************
     * GPGGA: Get the Altitude above the reference Ellipsoid.
     *
     * @return double
     */

    double getGeoidAltitude();


    /***********************************************************************************************
     * GPGSA: Get the Fix Mode.
     *
     * @return String
     */

    String getFixMode();


    /***********************************************************************************************
     * GPGSA: Get the Fix Type.
     *
     * @return int
     */

    int getFixType();


    /***********************************************************************************************
     * GPGSA: Get the PDOP.
     *
     * @return double
     */

    double getPDOP();

    //
    /***********************************************************************************************
     * GPGSA: Get the HDOP - Horizontal Dilution of Precision.
     *
     * @return double
     */

    double getHDOP();


    /***********************************************************************************************
     * GPGSA: Get the VDOP- Vertical Dilution of Precision.
     *
     * @return double
     */

    double getVDOP();


    /***********************************************************************************************
     * GPGGA: Get the number of satellites in Use.
     *
     * @return int
     */

    int getSatellitesInUseCount();


    /***********************************************************************************************
     * GPGSA: Get the IDs of the satellites in Use.
     *
     * @return List<String>
     */

    List<String> getSatellitesInUseIDs();


    /***********************************************************************************************
     * GPGSV: Get the number of satellites in View.
     *
     * @return int
     */

    int getSatellitesInViewCount();


    /***********************************************************************************************
     * GPGSV: Get the List of the satellites in View.
     *
     * @return List<SatelliteData>
     */

    List<SatelliteData> getSatellitesInView();


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    String getResourceKey();


    /***********************************************************************************************
     * Read all the Resources required by the GpsReceiver.
     */

    void readResources();
    }
