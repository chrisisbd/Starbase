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
//  14-04-03    LMN created file
//  07-08-06    LMN added findPort() & shutdown()
//
//------------------------------------------------------------------------------

package org.lmn.fc.common.support.gps;

//------------------------------------------------------------------------------
// Imports

import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;

import java.util.Enumeration;


/***************************************************************************************************
 * The GpsReceiverInterface.
 */

public interface GpsReceiverInterface
    {
    // This method is required by the Runnable interface
    void run();

    // This stops the Receiver thread
    void shutdown();

    // Locate a port to use for the Receiver
    boolean findPort(String portname);

    // Decode all received NMEA sentences
    boolean decodeNMEA(int capturetime);

    // Get the name of the RS232GpsReceiver Receiver
    String getReceiverName();

    // Get the list of supported NMEA sentences
    Enumeration getNMEASentences();

    // Get the DateOfLastUpdate
    String getDateOfLastUpdate();

    // GPRMC: Get the Date of the Fix
    YearMonthDayInterface getDateOfFix();

    // GPRMC: Get the Time of the Fix
    HourMinSecInterface getTimeOfFix();

    // GPRMC: Get the Latitude of the place of observation
    DegMinSecInterface getLatitude();

    // GPRMC: Get the Longitude of the place of observation
    DegMinSecInterface getLongitude();

    // GPRMC: Get the Speed in Knots
    double getSpeedKnots();

    // GPRMC: Get the Course in Degrees
    double getCourse();

    // GPRMC: Get the MagneticVariation
    double getMagneticVariation();

    // GPGGA: Get the Data Quality
    int getDataQuality();

    // GPGGA: Get the Altitude above sea level
    double getAltitudeASL();

    // GPGGA: Get the Altitude above the reference Ellipsoid
    double getGeoidAltitude();

    // GPGSA: Get the Fix Mode
    String getFixMode();

    // GPGSA: Get the Fix Type
    int getFixType();

    // GPGSA: Get the PDOP
    double getPDOP();

    // GPGSA: Get the HDOP - Horizontal Dilution of Precision
    double getHDOP();

    // GPGSA: Get the VDOP- Vertical Dilution of Precision
    double getVDOP();

    // GPGGA: Get the number of satellites in Use
    int getSatellitesInUse();

    // GPGSA: Get the IDs of the satellites in Use
    Enumeration getSatellitesInUseData();

    // GPGSV: Get the number of satellites in View
    int getSatellitesInView();

    // GPGSV: Get the IDs etc. of the satellites in View
    Enumeration getSatellitesInViewData();
    }


//------------------------------------------------------------------------------
// End of File

