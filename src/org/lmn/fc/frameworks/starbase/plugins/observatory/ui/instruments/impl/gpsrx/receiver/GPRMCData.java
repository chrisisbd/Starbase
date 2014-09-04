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
import org.lmn.fc.model.datatypes.YearMonthDayInterface;
import org.lmn.fc.model.datatypes.types.HourMinSecDataType;
import org.lmn.fc.model.datatypes.types.YearMonthDayDataType;


/***************************************************************************************************
 * GPRMCData.
 */

public final class GPRMCData implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkSingletons
    {
    // GPRMC internal data
    private YearMonthDayInterface ymdGPRMC_DateStamp;
    private HourMinSecInterface hmsGPRMC_TimeStamp;
    private double dblGPRMC_Latitude;
    private double dblGPRMC_Longitude;
    private double dblGPRMC_Speed;
    private double dblGPRMC_Course;
    private double dblGPRMC_Variation;
    private boolean boolIsUpdated;


    /***********************************************************************************************
     *
     */
    public GPRMCData()
        {
        ymdGPRMC_DateStamp = new YearMonthDayDataType();
        hmsGPRMC_TimeStamp = new HourMinSecDataType();
        dblGPRMC_Latitude = 0.0;
        dblGPRMC_Longitude = 0.0;
        dblGPRMC_Speed = 0.0;
        dblGPRMC_Course = 0.0;
        dblGPRMC_Variation = 0.0;
        boolIsUpdated = false;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public YearMonthDayInterface getDateStamp()
        {
        return ymdGPRMC_DateStamp;
        }


    /***********************************************************************************************
     *
     * @param datestamp
     */
    public void setDateStamp(final YearMonthDayInterface datestamp)
        {
        this.ymdGPRMC_DateStamp = datestamp;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public HourMinSecInterface getTimeStamp()
        {
        return hmsGPRMC_TimeStamp;
        }


    /***********************************************************************************************
     *
     * @param timestamp
     */
    public void setTimeStamp(final HourMinSecInterface timestamp)
        {
        this.hmsGPRMC_TimeStamp = timestamp;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getLatitude()
        {
        return dblGPRMC_Latitude;
        }


    /***********************************************************************************************
     *
     * @param latitude
     */
    public void setLatitude(final double latitude)
        {
        this.dblGPRMC_Latitude = latitude;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getLongitude()
        {
        return dblGPRMC_Longitude;
        }


    /***********************************************************************************************
     *
     * @param longitude
     */
    public void setLongitude(final double longitude)
        {
        this.dblGPRMC_Longitude = longitude;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getSpeed()
        {
        return dblGPRMC_Speed;
        }


    /***********************************************************************************************
     *
     * @param speed
     */
    public void setSpeed(final double speed)
        {
        this.dblGPRMC_Speed = speed;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getCourse()
        {
        return dblGPRMC_Course;
        }


    /***********************************************************************************************
     *
     * @param course
     */
    public void setCourse(final double course)
        {
        this.dblGPRMC_Course = course;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getVariation()
        {
        return dblGPRMC_Variation;
        }


    /***********************************************************************************************
     *
     * @param variation
     */
    public void setVariation(final double variation)
        {
        this.dblGPRMC_Variation = variation;
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
