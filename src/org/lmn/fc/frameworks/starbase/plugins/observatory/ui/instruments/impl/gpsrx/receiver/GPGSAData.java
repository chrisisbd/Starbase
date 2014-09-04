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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.GpsInstrumentReceiverInterface;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * GPGSAData.
 */

public final class GPGSAData implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkSingletons
    {
    private String strGPGSA_FixMode;
    private int intGPGSA_FixType;
    private double dblGPGSA_PDOP;
    private double dblGPGSA_HDOP;
    private double dblGPGSA_VDOP;
    private List<String> listGPGSA_SatellitesInUse;
    private boolean boolIsUpdated;


    /***********************************************************************************************
     *
     */
    public GPGSAData()
        {
        strGPGSA_FixMode = EMPTY_STRING;
        intGPGSA_FixType = 0;
        dblGPGSA_PDOP = 0.0;
        dblGPGSA_HDOP = 0.0;
        dblGPGSA_VDOP = 0.0;

        this.listGPGSA_SatellitesInUse = new ArrayList<String>(GpsInstrumentReceiverInterface.MAX_SATELLITES);
        boolIsUpdated = false;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public String getFixMode()
        {
        return strGPGSA_FixMode;
        }


    /***********************************************************************************************
     *
     * @param mode
     */
    public void setFixMode(final String mode)
        {
        this.strGPGSA_FixMode = mode;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public int getFixType()
        {
        return intGPGSA_FixType;
        }


    /***********************************************************************************************
     *
     * @param type
     */
    public void setFixType(final int type)
        {
        this.intGPGSA_FixType = type;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getPDOP()
        {
        return dblGPGSA_PDOP;
        }


    /***********************************************************************************************
     *
     * @param pdop
     */
    public void setPDOP(final double pdop)
        {
        this.dblGPGSA_PDOP = pdop;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public double getHDOP()
        {
        return dblGPGSA_HDOP;
        }


    /***********************************************************************************************
     *
     * @param hdop
     */
    public void setHDOP(final double hdop)
        {
        this.dblGPGSA_HDOP = hdop;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */

    public double getVDOP()
        {
        return dblGPGSA_VDOP;
        }


    /***********************************************************************************************
     *
     * @param vdop
     */
    public void setVDOP(final double vdop)
        {
        this.dblGPGSA_VDOP = vdop;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     *
     * @return
     */
    public List<String> getSatellitesInUse()
        {
        return listGPGSA_SatellitesInUse;
        }


    /***********************************************************************************************
     *
     * @param inuse
     */
    public void setSatellitesInUse(final List<String> inuse)
        {
        this.listGPGSA_SatellitesInUse = inuse;
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
