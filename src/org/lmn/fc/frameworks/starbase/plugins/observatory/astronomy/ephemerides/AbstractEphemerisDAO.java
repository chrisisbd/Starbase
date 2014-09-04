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


import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryLogInterface;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * AbstractEphemerisDAO.
 */

public class AbstractEphemerisDAO
    {
    private final Ephemeris ephemeris;
    private CoordinateType coordinateType;
    private Point2D.Double pointCoords;
    private List<Metadata> listEphemerisMetadata;

    // DAO Logging
    private final Vector<Vector> vecEventLogFragment;
    private List<Metadata> listEventLogMetadata;
    private ObservatoryLogInterface logObservatory;


    /***********************************************************************************************
     * AbstractEphemerisDAO.
     *
     * @param ephem
     */

    protected AbstractEphemerisDAO(final Ephemeris ephem)
        {
        // Injections
        this.ephemeris = ephem;

        // todo inject
        this.logObservatory = null;

        this.coordinateType = CoordinateType.UNASSIGNED;
        this.pointCoords = new Point2D.Double(0.0, 0.0);
        this.listEphemerisMetadata = new ArrayList<Metadata>(10);

        // Logging
        this.vecEventLogFragment = new Vector<Vector>(100);
        this.listEventLogMetadata = new ArrayList<Metadata>(10);
        }


    /***********************************************************************************************
     * Get the Ephemeris associated with this DAO.
     *
     * @return Ephemeris
     */

    public Ephemeris getEphemeris()
        {
        return (this.ephemeris);
        }


    /***********************************************************************************************
     * Get the CoordinateType of the Ephemeris Coordinates.
     *
     * @return CoordinateType
     */

    public final CoordinateType getCoordinateType()
        {
        return(this.coordinateType);
        }


    /***********************************************************************************************
     * Set the CoordinateType of the Ephemeris Coordinates.
     *
     * @param type
     */

    public final void setCoordinateType(final CoordinateType type)
        {
        this.coordinateType = type;
        }


    /***********************************************************************************************
     * Get the Coordinates for the current Julian Date.
     * RightAscension is in HOURS, Declination is in DEGREES.
     * Azimuth and Elevation are in DEGREES.
     * Galactic Coordinates are in DEGREES.
     *
     * @return Point2D.Double
     */

    public Point2D.Double getCoordinates()
        {
        return (this.pointCoords);
        }


    /***********************************************************************************************
     * Set the Coordinates for the current Julian Date.
     * RightAscension is in HOURS, Declination is in DEGREES.
     * Azimuth and Elevation are in DEGREES.
     * Galactic Coordinates are in DEGREES.
     *
     * @param coords
     */

    protected void setCoordinates(final Point2D.Double coords)
        {
        this.pointCoords = coords;
        }


    /***********************************************************************************************
     * Get the EphemerisMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getEphemerisMetadata()
        {
        return (this.listEphemerisMetadata);
        }


    /***********************************************************************************************
     * Set the EphemerisMetadata List.
     *
     * @param metadata
     */

    public void setEphemerisMetadata(final List<Metadata> metadata)
        {
        this.listEphemerisMetadata = metadata;
        }


    /**********************************************************************************************/
    /* Logging                                                                                    */
    /***********************************************************************************************
     * Get the DAO EventLog Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getEventLogMetadata()
        {
        return (this.listEventLogMetadata);
        }


    /***********************************************************************************************
     * Set the DAO EventLog Metadata List.
     *
     * @param metadata
     */

    public void setEventLogMetadata(final List<Metadata> metadata)
        {
        this.listEventLogMetadata = metadata;
        }


    /***********************************************************************************************
     * Get the DAO EventLogFragment.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getEventLogFragment()
        {
        return (this.vecEventLogFragment);
        }


    /***********************************************************************************************
     * Re-initialise the EventLogFragment and its Metadata.
     */

    public synchronized void clearEventLogFragment()
        {
        if (getEventLogFragment() != null)
            {
            getEventLogFragment().clear();
            }

        if (getEventLogMetadata() != null)
            {
            getEventLogMetadata().clear();
            }
        else
            {
            setEventLogMetadata(new ArrayList<Metadata>(10));
            }
        }


    /***********************************************************************************************
     * Get the ObservatoryLog.
     *
     * @return ObservatoryLogInterface
     */

    public ObservatoryLogInterface getObservatoryLog()
        {
        return (this.logObservatory);
        }


    /***********************************************************************************************
     * Set the ObservatoryLog.
     *
     * @param log
     */

    public void setObservatoryLog(final ObservatoryLogInterface log)
        {
        this.logObservatory = log;
        }
    }
