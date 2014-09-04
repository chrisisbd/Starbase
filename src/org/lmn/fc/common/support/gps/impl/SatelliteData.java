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
//  02-07-02    LMN created file
//
//------------------------------------------------------------------------------
// Utilities RS232GpsReceiver package

package org.lmn.fc.common.support.gps.impl;

//------------------------------------------------------------------------------
// Imports


//------------------------------------------------------------------------------

public class SatelliteData
    {
    private int intSatellitePRN;            // The satellite ID
    private int intElevation;               // Satellite elevation 00...90
    private int intAzimuth;                 // Satellite azimuth 000...359
    private int intSNR;                     // Satellite Signal to Noise ratio 00...99dB


    public SatelliteData()
        {
        intSatellitePRN = 0;
        intElevation = 0;
        intAzimuth = 0;
        intSNR = 0;
        }


    //--------------------------------------------------------------------------
    // Set the SatellitePRN

    public final void setSatellitePRN(final int prn)
        {
        this.intSatellitePRN = prn;
        }


    //--------------------------------------------------------------------------
    // Get the SatellitePRN

    public final int getSatellitePRN()
        {
        return(this.intSatellitePRN);
        }


    //--------------------------------------------------------------------------
    // Set the Elevation

    public final void setElevation(final int elevation)
        {
        this.intElevation = elevation;
        }


    //--------------------------------------------------------------------------
    // Get the Elevation

    public final int getElevation()
        {
        return(this.intElevation);
        }


    //--------------------------------------------------------------------------
    // Set the Azimuth

    public final void setAzimuth(final int azimuth)
        {
        this.intAzimuth = azimuth;
        }


    //--------------------------------------------------------------------------
    // Get the Azimuth

    public final int getAzimuth()
        {
        return(this.intAzimuth);
        }


    //--------------------------------------------------------------------------
    // Set the SNR

    public final void setSNR(final int snr)
        {
        this.intSNR = snr;
        }


    //--------------------------------------------------------------------------
    // Get the SNR

    public final int getSNR()
        {
        return(this.intSNR);
        }
    }


//------------------------------------------------------------------------------
// End of File

