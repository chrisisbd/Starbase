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
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;


/***************************************************************************************************
 * GPGGAData.
 */

public final class GPGGAData implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkSingletons
    {
    private HourMinSecInterface hmsGPGGA_TimeStamp;
    private double dblGPGGA_HDOP;
    private double dblGPGGA_Latitude;
    private double dblGPGGA_Longitude;

    private int intGPGGA_FixQuality;
    private int intGPGGA_SatellitesInUse;
    private double dblGPGGA_Altitude;
    private double dblGPGGA_Geoid;
    private boolean boolIsUpdated;


    /***********************************************************************************************
     *
     */
    public GPGGAData()
        {
        hmsGPGGA_TimeStamp = new HourMinSecDataType();
        dblGPGGA_Latitude = 0.0;
        dblGPGGA_Longitude = 0.0;
        intGPGGA_FixQuality = 0;
        intGPGGA_SatellitesInUse = 0;
        dblGPGGA_HDOP = 0.0;
        dblGPGGA_Altitude = 0.0;
        dblGPGGA_Geoid = 0.0;
        boolIsUpdated = false;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public HourMinSecInterface getTimeStamp()
        {
        return hmsGPGGA_TimeStamp;
        }


    /***********************************************************************************************
     *
     * @param timestamp
     */
    public void setTimeStamp(final HourMinSecInterface timestamp)
        {
        this.hmsGPGGA_TimeStamp = timestamp;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getHDOP()
        {
        return dblGPGGA_HDOP;
        }


    /***********************************************************************************************
     *
     * @param hdop
     */
    public void setHDOP(final double hdop)
        {
        this.dblGPGGA_HDOP = hdop;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getLatitude()
        {
        return dblGPGGA_Latitude;
        }


    /***********************************************************************************************
     *
     * @param latitude
     */
    public void setLatitude(final double latitude)
        {
        this.dblGPGGA_Latitude = latitude;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getLongitude()
        {
        return dblGPGGA_Longitude;
        }


    /***********************************************************************************************
     *
     * @param longitude
     */
    public void setLongitude(final double longitude)
        {
        this.dblGPGGA_Longitude = longitude;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public int getFixQuality()
        {
        return intGPGGA_FixQuality;
        }


    /***********************************************************************************************
     *
     * @param fixquality
     */
    public void setFixQuality(final int fixquality)
        {
        this.intGPGGA_FixQuality = fixquality;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public int getSatellitesInUseCount()
        {
        return intGPGGA_SatellitesInUse;
        }


    /***********************************************************************************************
     *
     * @param inusecount
     */
    public void setSatellitesInUseCount(final int inusecount)
        {
        this.intGPGGA_SatellitesInUse = inusecount;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getAltitude()
        {
        return dblGPGGA_Altitude;
        }


    /***********************************************************************************************
     *
     * @param altitude
     */
    public void setAltitude(final double altitude)
        {
        this.dblGPGGA_Altitude = altitude;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getGeoid()
        {
        return dblGPGGA_Geoid;
        }


    /***********************************************************************************************
     *
     * @param geoid
     */
    public void setGeoid(final double geoid)
        {
        this.dblGPGGA_Geoid = geoid;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public boolean isUpdated()
        {
        return (this.boolIsUpdated);
        }
    }
