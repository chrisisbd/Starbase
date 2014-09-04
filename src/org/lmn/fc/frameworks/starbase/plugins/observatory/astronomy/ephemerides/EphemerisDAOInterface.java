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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryLogInterface;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * EphemerisDAOInterface.
 */

public interface EphemerisDAOInterface extends FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ResourceKeys
    {
    boolean ORIGIN_IS_NORTH = true;


    /***********************************************************************************************
     * Get the Ephemeris associated with this DAO.
     *
     * @return Ephemeris
     */

    Ephemeris getEphemeris();


    /***********************************************************************************************
     * Get the CoordinateType of the Ephemeris Coordinates.
     *
     * @return CoordinateType
     */

     CoordinateType getCoordinateType();


    /***********************************************************************************************
     * Set the CoordinateType of the Ephemeris Coordinates.
     *
     * @param type
     */

     void setCoordinateType(CoordinateType type);


    /***********************************************************************************************
     * Get the Coordinates for the current Julian Date.
     * RightAscension is in HOURS, Declination is in DEGREES.
     * Azimuth and Elevation are in DEGREES.
     * Galactic Coordinates are in DEGREES.
     *
     * @return Point2D.Double
     */

    Point2D.Double getCoordinates();


    /***********************************************************************************************
     * Get the EphemerisMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getEphemerisMetadata();


    /***********************************************************************************************
     * Set the EphemerisMetadata List.
     *
     * @param metadata
     */

    void setEphemerisMetadata(List<Metadata> metadata);


    /***********************************************************************************************
     * Recalculate the coordinates for the specified JD.
     * Return the CoordinateType of the original coordinates.
     *
     * @param juliandate
     * @param last
     * @param latitude
     *
     * @return CoordinateType
     */

    CoordinateType recalculateForJulianDate(double juliandate,
                                            double last,
                                            double latitude);


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the EventLog Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getEventLogMetadata();


    /***********************************************************************************************
     * Set the DAO EventLog Metadata List.
     *
     * @param metadata
     */

    void setEventLogMetadata(List<Metadata> metadata);


    /***********************************************************************************************
     * Get the DAO EventLogFragment.
     *
     * @return Vector<Vector>
     */

    Vector<Vector> getEventLogFragment();


    /***********************************************************************************************
     * Re-initialise the EventLog and its Metadata.
     */

    void clearEventLogFragment();


    /***********************************************************************************************
     * Get the ObservatoryLog.
     *
     * @return ObservatoryLogInterface
     */

    ObservatoryLogInterface getObservatoryLog();


    /***********************************************************************************************
     * Set the ObservatoryLog.
     *
     * @param log
     */

    void setObservatoryLog(ObservatoryLogInterface log);
    }
