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
 * GPGSVData.
 */

public final class GPGSVData implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkSingletons
    {
    private List<SatelliteData> listGPGSV_SatellitesInView;
    private boolean boolIsUpdated;


    /***********************************************************************************************
     * Construct a GPGSVData.
     */

    public GPGSVData()
        {
        this.listGPGSV_SatellitesInView = new ArrayList<SatelliteData>(GpsInstrumentReceiverInterface.MAX_SATELLITES);
        boolIsUpdated = false;
        }


    /***********************************************************************************************
     * Get the List of SatellitesInView.
     *
     * @return List<SatelliteData>
     */

    public List<SatelliteData> getSatellitesInView()
        {
        return (this.listGPGSV_SatellitesInView);
        }


    /***********************************************************************************************#
     * Set the List of SatellitesInView.
     *
     * @param inview
     */

    public void setSatellitesInView(final List<SatelliteData> inview)
        {
        this.listGPGSV_SatellitesInView = inview;
        boolIsUpdated = true;
        }


    /***********************************************************************************************
     * Indicate if the data have been updated.
     *
     * @return boolean
     */

    public boolean isUpdated()
        {
        return (this.boolIsUpdated);
        }
    }
