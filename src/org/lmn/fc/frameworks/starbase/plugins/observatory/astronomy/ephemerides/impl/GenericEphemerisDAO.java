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

package org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.impl;


import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.common.exceptions.HourMinSecException;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.AbstractEphemerisDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.xmlbeans.ephemerides.Ephemeris;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * GenericEphemerisDAO.
 */

public class GenericEphemerisDAO extends AbstractEphemerisDAO
                                 implements EphemerisDAOInterface
    {
    /***********************************************************************************************
     * GenericEphemerisDAO.
     *
     * @param ephemeris
     */

    public GenericEphemerisDAO(final Ephemeris ephemeris)
        {
        super(ephemeris);
        }


    /***********************************************************************************************
     * Recalculate the coordinates for the specified JD.
     *
     * @param juliandate
     * @param last
     * @param latitude
     *
     *
     */

    public CoordinateType recalculateForJulianDate(final double juliandate,
                                                   final double last,
                                                   final double latitude)
        {
        final String SOURCE = "GenericEphemerisDAO.recalculateForJulianDate() ";
        CoordinateType coordinateType;

        coordinateType = CoordinateType.UNASSIGNED;

        getEventLogFragment().clear();

        if (getEphemeris() != null)
            {
            final List<String> errors;
            Point2D.Double pointCoords;

            errors = new ArrayList<String>(10);

            // Prepare defaults
            pointCoords = new Point2D.Double(0.0, 0.0);

            try
                {
                // See which coordinates we have been given
                if (getEphemeris().getAzEl() != null)
                    {
                    final double dblAz;
                    final double dblEl;

                    // Az 000.0 to 359.9 dd.d Clockwise from North
                    // El 00.0 to 89.9  dd.d
                    dblAz = Double.parseDouble(getEphemeris().getAzEl().getAz());
                    dblEl = Double.parseDouble(getEphemeris().getAzEl().getEl());

                    coordinateType = CoordinateType.AZEL;
                    pointCoords = new Point2D.Double(dblAz, dblEl);
                    }
                else if (getEphemeris().getRaDec() != null)
                    {
                    final HourMinSecInterface hmsRA;
                    final DegMinSecInterface dmsDec;

                    // RA   00:00:00 to 23:59:59  hh:mm:ss
                    // Dec -89:59:59 to +00:00:00 to +89:59:59  deg:mm:ss
                    hmsRA = (HourMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(getEphemeris().getRaDec().getRA(),
                                                                                            DataTypeDictionary.RIGHT_ASCENSION,
                                                                                            EMPTY_STRING,
                                                                                            EMPTY_STRING,
                                                                                            errors);
                    dmsDec = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(getEphemeris().getRaDec().getDec(),
                                                                                            DataTypeDictionary.DECLINATION,
                                                                                            EMPTY_STRING,
                                                                                            EMPTY_STRING,
                                                                                            errors);
                    // We don't need to set the display formats because we only need the double values

                    if ((hmsRA != null)
                        && (dmsDec != null))
                        {
                        coordinateType = CoordinateType.RADEC;
                        pointCoords = new Point2D.Double(hmsRA.toDouble(),
                                                         dmsDec.toDouble());
                        }
                    else
                        {
                        LOGGER.errors(SOURCE, errors);
                        }
                    }
                else if (getEphemeris().getGalactic() != null)
                    {
                    final DegMinSecInterface dmsGalacticLongitude;
                    final DegMinSecInterface dmsGalacticLatitude;

                    // Longitude 000:00:00 to 359:59:59  deg:mm:ss
                    // Latitude  -89:59:59 to +00:00:00 to +89:59:59  deg:mm:ss
                    dmsGalacticLongitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(getEphemeris().getGalactic().getL(),
                                                                                                          DataTypeDictionary.GALACTIC_LONGITUDE,
                                                                                                          EMPTY_STRING,
                                                                                                          EMPTY_STRING,
                                                                                                          errors);
                    dmsGalacticLatitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(getEphemeris().getGalactic().getB(),
                                                                                                         DataTypeDictionary.GALACTIC_LATITUDE,
                                                                                                         EMPTY_STRING,
                                                                                                         EMPTY_STRING,
                                                                                                         errors);
                    if ((dmsGalacticLongitude != null)
                        && (dmsGalacticLatitude != null))
                        {
                        coordinateType = CoordinateType.GALACTIC;
                        pointCoords = new Point2D.Double(dmsGalacticLongitude.toDouble(),
                                                         dmsGalacticLatitude.toDouble());
                        }
                    else
                        {
                        LOGGER.errors(SOURCE, errors);
                        }
                    }
                else if (getEphemeris().getMetadata() != null)
                    {
                    final String strKeyX;
                    final String strValueX;
                    final String strKeyY;
                    final String strValueY;
                    final String strKeyType;
                    final String strValueType;
                    final CoordinateType coordinateTypeFromMetadata;

                    // The Ephemeris contains an object whose coordinates must be found via Metadata,
                    // soo see if we have the three required fields (x, y, type)
                    strKeyX = getEphemeris().getMetadata().getKeyX();
                    strKeyY = getEphemeris().getMetadata().getKeyY();
                    strKeyType = getEphemeris().getMetadata().getKeyType();

                    // Try to find the Metadata containing the CoordinateType
                    // Return NO_DATA if the key cannot be found in the List
                    strValueType = MetadataHelper.getMetadataValueByKey(getEphemerisMetadata(), strKeyType);

                    // Return NULL if the name is not found
                    coordinateTypeFromMetadata = CoordinateType.getCoordinateTypeForName(strValueType);

                    // Try to find the Metadata containing the X value
                    // Return NO_DATA if the key cannot be found in the List
                    strValueX = MetadataHelper.getMetadataValueByKey(getEphemerisMetadata(), strKeyX);

                    // Try to find the Metadata containing the Y value
                    // Return NO_DATA if the key cannot be found in the List
                    strValueY = MetadataHelper.getMetadataValueByKey(getEphemerisMetadata(), strKeyY);

                    // D0 we have all three required fields?
                    if ((coordinateTypeFromMetadata != null)
                        && (strValueX != null)
                        && (!NO_DATA.equals(strValueX))
                        && (strValueY != null)
                        && (!NO_DATA.equals(strValueY)))
                        {
                        switch (coordinateTypeFromMetadata)
                            {
                            case UNASSIGNED:
                                {
                                pointCoords = new Point2D.Double(0.0, 0.0);
                                LOGGER.warn(SOURCE + "CoordinateType UNASSIGNED in use");
                                break;
                                }

                            case AZEL:
                                {
                                final double dblAz;
                                final double dblEl;

                                // Az 000.0 to 359.9 dd.d Clockwise from North
                                // Alt 00.0 to 89.9  dd.d
                                dblAz = Double.parseDouble(strValueX);
                                dblEl = Double.parseDouble(strValueY);

                                coordinateType = coordinateTypeFromMetadata;
                                pointCoords = new Point2D.Double(dblAz, dblEl);
                                break;
                                }

                            case RADEC:
                                {
                                final HourMinSecInterface hmsRA;
                                final DegMinSecInterface dmsDec;

                                // RA   00:00:00 to 23:59:59  hh:mm:ss
                                // Dec -89:59:59 to +00:00:00 to +89:59:59  deg:mm:ss
                                hmsRA = (HourMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(strValueX,
                                                                                                        DataTypeDictionary.RIGHT_ASCENSION,
                                                                                                        EMPTY_STRING,
                                                                                                        EMPTY_STRING,
                                                                                                        errors);
                                dmsDec = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(strValueY,
                                                                                                        DataTypeDictionary.DECLINATION,
                                                                                                        EMPTY_STRING,
                                                                                                        EMPTY_STRING,
                                                                                                        errors);
                                // We don't need to set the display formats because we only need the double values

                                if ((hmsRA != null)
                                    && (dmsDec != null))
                                    {
                                    coordinateType = coordinateTypeFromMetadata;
                                    pointCoords = new Point2D.Double(hmsRA.toDouble(),
                                                                     dmsDec.toDouble());
                                    }
                                else
                                    {
                                    LOGGER.errors(SOURCE, errors);
                                    }
                                break;
                                }

                            case GALACTIC:
                                {
                                final DegMinSecInterface dmsGalacticLongitude;
                                final DegMinSecInterface dmsGalacticLatitude;

                                // Longitude 000:00:00 to 359:59:59  deg:mm:ss
                                // Latitude  -89:59:59 to +00:00:00 to +89:59:59  deg:mm:ss
                                dmsGalacticLongitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(strValueX,
                                                                                                                      DataTypeDictionary.GALACTIC_LONGITUDE,
                                                                                                                      EMPTY_STRING,
                                                                                                                      EMPTY_STRING,
                                                                                                                      errors);
                                dmsGalacticLatitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(strValueY,
                                                                                                                     DataTypeDictionary.GALACTIC_LATITUDE,
                                                                                                                     EMPTY_STRING,
                                                                                                                     EMPTY_STRING,
                                                                                                                     errors);
                                if ((dmsGalacticLongitude != null)
                                    && (dmsGalacticLatitude != null))
                                    {
                                    coordinateType = coordinateTypeFromMetadata;
                                    pointCoords = new Point2D.Double(dmsGalacticLongitude.toDouble(),
                                                                     dmsGalacticLatitude.toDouble());
                                    }
                                else
                                    {
                                    LOGGER.errors(SOURCE, errors);
                                    }
                                break;
                                }

                            default:
                                {
                                pointCoords = new Point2D.Double(0.0, 0.0);
                                LOGGER.error(SOURCE + "Invalid Ephemerides XML configuration, CoordinateType not recognised [type=" + coordinateTypeFromMetadata.getName() + "]");
                                }
                            }
                        }
                    else
                        {
                        if ((getObservatoryLog() != null)
                            && (getObservatoryLog().getInstrumentPanel() != null)
                            && (getObservatoryLog().getInstrumentPanel().getEventLogTab() != null))
                            {
                            // Unable to use one or more of the supplied parameters
                            LOGGER.warn(SOURCE + "Coordinate Metadata not found. Are the Instrument Metadata loaded?");

                            // ToDo Use the EventLog instead
                            //                        SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                            //                                                           EventStatus.WARNING,
                            //                                                           METADATA_TARGET_EPHEMERIS
                            //                                                               + METADATA_ACTION_RECALCULATE
                            //                                                               + METADATA_RESULT
                            //                                                               + "Coordinate Metadata Keys not found"
                            //                                                               + TERMINATOR,
                            //                                                           SOURCE,
                            //                                                           getObservatoryClock());

                            }
                        }
                    }
                else
                    {
                    // Do the best we can if no coordinates are supplied...
                    LOGGER.error(SOURCE + "No coordinates supplied in XML, so using default of (0, 0)");
                    }
                }

            catch (NumberFormatException exception)
                {
                LOGGER.error(SOURCE + "NumberFormatException [exception=" + exception.getMessage() + "]");
                }

            catch (HourMinSecException exception)
                {
                LOGGER.error(SOURCE + "HourMinSecException [exception=" + exception.getMessage() + "]");
                }

            catch (DegMinSecException exception)
                {
                LOGGER.error(SOURCE + "DegMinSecException [exception=" + exception.getMessage() + "]");
                }

            setCoordinates(pointCoords);
            }

        return (coordinateType);
        }
    }
