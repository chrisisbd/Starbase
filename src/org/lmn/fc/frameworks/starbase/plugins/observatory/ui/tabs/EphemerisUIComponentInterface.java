// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.ui.reports.ReportTablePlugin;


/***************************************************************************************************
 * EphemerisUIComponentInterface.
 */

public interface EphemerisUIComponentInterface extends ReportTablePlugin
    {
    // String Resources
    String HEADER_TITLE                = "Ephemeris";
    String MSG_EPHEMERIS_CREATED       = "Ephemeris created at";
    String TITLE_EPHEMERIS_DATE        = "Date";
    String TITLE_UT                    = "UT";
    String TITLE_JD                    = "Julian Date";
    String TITLE_LAST                  = "LAST";
    String TITLE_AZIMUTH               = "Azimuth";
    String TITLE_ELEVATION_TRUE        = "True Elev";
    String TITLE_ELEVATION_APPARENT    = "Apparent Elev";
    String TITLE_RA                    = "Right Ascension";
    String TITLE_DEC                   = "Declination";
    String TITLE_GALACTIC_LONGITUDE    = "Galactic l";
    String TITLE_GALACTIC_LATITUDE     = "Galactic b";

    int COLUMN_COUNT = 11;


    /***********************************************************************************************
     * Set the Ephemeris on which this report is based, given its Name.
     *
     * @param ephemerisname
     */

    void setEphemerisFromName(String ephemerisname);


    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * The Ephemeris data appear in the UserObject.
     *
     * @param daowrapper
     * @param updatemetadata
     */

    void setWrappedData(DAOWrapperInterface daowrapper,
                        boolean updatemetadata);
    }
