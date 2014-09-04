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

package org.lmn.fc.common.support.gps.impl;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.support.gps.GpsReceiverInterface;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.YearMonthDayInterface;

import java.util.Enumeration;



public class StubGarminGpsReceiver implements GpsReceiverInterface,
                                              FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkSingletons
    {
    // This method is required by the Runnable interface
    public void run()
        {
        }// This stops the Receiver thread

    public void shutdown()
        {
        }// Locate a port to use for the Receiver

    public boolean findPort(String portname)
        {
        return false;
        }// Decode all received NMEA sentences

    public boolean decodeNMEA(int capturetime)
        {
        return false;
        }// Get the name of the RS232GpsReceiver Receiver

    public String getReceiverName()
        {
        return null;
        }// Get the list of supported NMEA sentences

    public Enumeration getNMEASentences()
        {
        return null;
        }// Get the DateOfLastUpdate

    public String getDateOfLastUpdate()
        {
        return null;
        }// GPRMC: Get the Date of the Fix

    public YearMonthDayInterface getDateOfFix()
        {
        return null;
        }// GPRMC: Get the Time of the Fix

    public HourMinSecInterface getTimeOfFix()
        {
        return null;
        }// GPRMC: Get the Latitude of the place of observation

    public DegMinSecInterface getLatitude()
        {
        return null;
        }// GPRMC: Get the Longitude of the place of observation

    public DegMinSecInterface getLongitude()
        {
        return null;
        }// GPRMC: Get the Speed in Knots

    public double getSpeedKnots()
        {
        return 0;
        }// GPRMC: Get the Course in Degrees

    public double getCourse()
        {
        return 0;
        }// GPRMC: Get the MagneticVariation

    public double getMagneticVariation()
        {
        return 0;
        }// GPGGA: Get the Data Quality

    public int getDataQuality()
        {
        return 0;
        }// GPGGA: Get the Altitude above sea level

    public double getAltitudeASL()
        {
        return 0;
        }// GPGGA: Get the Altitude above the reference Ellipsoid

    public double getGeoidAltitude()
        {
        return 0;
        }// GPGSA: Get the Fix Mode

    public String getFixMode()
        {
        return null;
        }// GPGSA: Get the Fix Type

    public int getFixType()
        {
        return 0;
        }// GPGSA: Get the PDOP

    public double getPDOP()
        {
        return 0;
        }// GPGSA: Get the HDOP - Horizontal Dilution of Precision

    public double getHDOP()
        {
        return 0;
        }// GPGSA: Get the VDOP- Vertical Dilution of Precision

    public double getVDOP()
        {
        return 0;
        }// GPGGA: Get the number of satellites in Use

    public int getSatellitesInUse()
        {
        return 0;
        }// GPGSA: Get the IDs of the satellites in Use

    public Enumeration getSatellitesInUseData()
        {
        return null;
        }// GPGSV: Get the number of satellites in View

    public int getSatellitesInView()
        {
        return 0;
        }// GPGSV: Get the IDs etc. of the satellites in View

    public Enumeration getSatellitesInViewData()
        {
        return null;
        }
    }
