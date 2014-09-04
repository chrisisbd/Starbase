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
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.GpsException;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsInstrumentReceiverInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsReceiverHelper;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;


/***************************************************************************************************
 * StubStaticRS232GpsReceiver provides fixed data all the time.
 */

public final class StubStaticRS232GpsReceiver extends AbstractNMEAGpsReceiver
                                              implements GpsInstrumentReceiverInterface,
                                                         FrameworkConstants,
                                                         FrameworkStrings,
                                                         FrameworkSingletons,
                                                         ResourceKeys
    {
    private int intOffset;
    private String strLatitude;
    private String strLongitude;
    private double dblASL;


    /***********************************************************************************************
     * Wrap the coordinates of a satellite off the map.
     *
     * @param data
     * @param azimuth
     * @param elevation
     */

    private static void wrapCoords(final SatelliteData data,
                                   final int azimuth,
                                   final int elevation)
        {
        if (data.getAzimuth() >= 360)
            {
            data.setAzimuth(azimuth);
            }

        if (data.getElevation() >= 90)
            {
            data.setElevation(elevation);
            }
        }


    /***********************************************************************************************
     * Add some Gaussian jitter to the Seconds part of the specified fix.
     * Since the distribution can be negative as well as positive, choose a mid-range value initially.
     * See: http://www.csgnetwork.com/degreelenllavcalc.html
     *
     * @param dmsfix
     *
     * @return DegMinSec
     */

    private static DegMinSecInterface addSecondsJitterToFix(final DegMinSecInterface dmsfix)
        {
        final Random random;
        double dblFixDegrees;

        random = new Random();
        dblFixDegrees = dmsfix.toDouble();

        // This returns next pseudorandom, Gaussian ("normally") distributed double value
        // with mean 0.0 and standard deviation 1.0 from this random number generator's sequence
        // 0.001 degree is about 100 metres on the ground in the UK
        dblFixDegrees += random.nextGaussian() / 10000.0;

        // Reset the incoming DegMinSec to the new value
        dmsfix.setFromBigDegrees(BigDecimal.valueOf(dblFixDegrees));

        return (dmsfix);
        }


    /***********************************************************************************************
     * Construct a StubStaticRS232GpsReceiver.
     *
     * @param dao
     * @param rxtype
     * @param resourcekey
     *
     * @throws GpsException
     */

    public StubStaticRS232GpsReceiver(final ObservatoryInstrumentDAOInterface dao,
                                      final String rxtype,
                                      final String resourcekey) throws GpsException
        {
        super(dao, rxtype, resourcekey);

        this.intOffset = 0;
        this.strLatitude = "+00:00:00.0000";
        this.strLongitude = "+000:00:00.0000";
        this.dblASL = 0.0;

        // Check Receiver against the supported type...
        if (!NMEA0183_GPS_STATIC.equals(getReceiverType()))
            {
            // Device type not recognised
            throw new GpsException(EXCEPTION_INVALID_GPSRX + " [receiver=" + getReceiverType() + "]");
            }
        }


    /***********************************************************************************************
     * Initialise the GPS Receiver.
     *
     * @return boolean
     */

    public boolean initialise()
        {
        initialiseNMEAData();

        return (true);
        }


    /***********************************************************************************************
     * Start the GPS Receiver.
     *
     * @return boolean
     */

    public boolean start()
        {
        readResources();

        setStarted(true);

        return (true);
        }


    /***********************************************************************************************
     * Stop the GPS Receiver.
     *
     * @return boolean
     */

    public boolean stop()
        {
        setStarted(false);

        return (true);
        }


    /***********************************************************************************************
     * Dispose the GPS Receiver.
     */

    public void dispose()
        {
        }


    /***********************************************************************************************
     * Decode all NMEA sentences received in the specified time.
     * Return <code>true</code> if a valid fix is obtained.
     * The fix has some jitter in the last three decimal places.
     *
     * @param capturetime
     * @param clock
     *
     * @return boolean
     */

    public boolean decodeNMEA(final int capturetime,
                              final ObservatoryClockInterface clock)
        {
        // This stub ignores the capture time

        // Get the DateOfLastUpdate
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

        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "StubStaticRS232GpsReceiver.decodeNMEA()");

        readResources();

        this.strDateOfLastUpdate = clock.getDateTimeNowAsString();
        this.ymdDateOfFix = new YearMonthDayDataType(clock.getCalendarDateNow());
        this.hmsTimeOfFix = new HourMinSecDataType(clock.getCalendarTimeNow());

        // Latitude is POSITIVE to the NORTH.
        // -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999

        // NSC is Latitude/Longitude: 52°39'13"N, 01°07'57"W; altitude: 185' or 56.39m

        this.dmsLatitude = addSecondsJitterToFix((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strLatitude,
                                                                                                                 DataTypeDictionary.LATITUDE,
                                                                                                                 EMPTY_STRING,
                                                                                                                 EMPTY_STRING,
                                                                                                                 new ArrayList<String>(1)));
        this.dmsLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        this.dmsLatitude.setDisplayFormat(DegMinSecFormat.NS);

        // Longitude is POSITIVE to the WEST.
        // -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999
        this.dmsLongitude = addSecondsJitterToFix((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strLongitude,
                                                                                                                  DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                                  EMPTY_STRING,
                                                                                                                  EMPTY_STRING,
                                                                                                                  new ArrayList<String>(1)));
        this.dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        this.dmsLongitude.setDisplayFormat(DegMinSecFormat.EW);

        this.dblAltitudeASL = dblASL;

        this.intDataQuality = 1;            // Data from a GPS fix
        this.intFixType = 2;                // 2D fix, Fix Type > 1 if a fix exists

        this.dblSpeedKnots = 0.0;
        this.dblCourseDegrees = 123.0;
        this.dblMagneticVariation = 8.0;
        this.dblGeoidAltitude = 19.0;
        this.strFixMode = VALIDITY_A;
        this.dblPDOP = 1.0;
        this.dblHDOP = 2.0;
        this.dblVDOP = 3.0;

        // SatellitesInUse
        this.listSatellitesInUse = new Vector<String>(MAX_SATELLITES);

        // There must be at least four satellites to get a valid altitude
        this.listSatellitesInUse.add("1");
        this.listSatellitesInUse.add("2");
        this.listSatellitesInUse.add("3");
        this.listSatellitesInUse.add("4");
        this.intSatellitesInUseCount = listSatellitesInUse.size();

        // SatellitesInView
        this.listSatellitesInView = new Vector<SatelliteData>(MAX_SATELLITES);

        // Simulate four satellite tracks
        final SatelliteData data00;

        data00 = new SatelliteData();
        data00.setAzimuth(0 + intOffset);
        data00.setElevation(10 + ((int)(AstroMath.sind((double)intOffset) * 10)));
        data00.setSatellitePRN(1);
        data00.setSNRdB(14);
//        wrapCoords(data00, 0, 0);

        this.listSatellitesInView.add(data00);

        final SatelliteData data01;

        data01 = new SatelliteData();
        data01.setAzimuth(0 + intOffset);
        data01.setElevation(30 + ((int)(AstroMath.sind((double)intOffset) * 10)));
        data01.setSatellitePRN(2);
        data01.setSNRdB(43);
//        wrapCoords(data01, 5, 5);

        this.listSatellitesInView.add(data01);

        final SatelliteData data02;

        data02 = new SatelliteData();
        data02.setAzimuth(0 + intOffset);
        data02.setElevation(50 + ((int)(AstroMath.sind((double)intOffset) * 10)));
        data02.setSatellitePRN(3);
        data02.setSNRdB(19);
//        wrapCoords(data02, 10, 10);

        this.listSatellitesInView.add(data02);

        final SatelliteData data03;

        data03 = new SatelliteData();
        data03.setAzimuth(0 + intOffset);
        data03.setElevation(70 + ((int)(AstroMath.sind((double)intOffset) * 10)));
        data03.setSatellitePRN(4);
        data03.setSNRdB(29);
//        wrapCoords(data03, 15, 15);

        this.listSatellitesInView.add(data03);

        this.intSatellitesInViewCount = listSatellitesInView.size();

        // Move the satellites!
        this.intOffset++;

        if (this.intOffset >= 360)
            {
            this.intOffset = 0;
            }

        GpsReceiverHelper.showFixDebug(this, true);

        // This fix is always valid
        return (true);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the StubStaticRS232GpsReceiver.
     */

    public void readResources()
        {
        LOGGER.debugGpsEvent(getHostDAO().getHostInstrument().isDebugMode(),
                             "StubStaticRS232GpsReceiver.readResources() [ResourceKey=" + getResourceKey() + "]");

        // ToDo Take location from the Properties file
        // NSC is Latitude/Longitude: 52°39'13"N, 01°07'57"W; altitude: 185' or 56.39m
        this.strLatitude = "+52:39:13.0000";
        this.strLongitude = "+001:07:57.0000";
        this.dblASL = 56.4;
        }
    }
