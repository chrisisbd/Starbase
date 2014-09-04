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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.MetadataFactory;
import org.lmn.fc.common.utilities.astronomy.CoordinateType;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.StarMapPoint;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.receiver.AbstractNMEAGpsReceiver;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx.receiver.SatelliteData;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.datatypes.types.LatitudeDataType;
import org.lmn.fc.model.datatypes.types.LongitudeDataType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/***************************************************************************************************
 * GpsReceiverHelper.
 */

public final class GpsReceiverHelper implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkSingletons,
                                                FrameworkMetadata,
                                                FrameworkRegex
    {
    // String Resources
    private static final String KEY_SATELLITE_ID = "Satellite.ID";
    private static final String KEY_SATELLITE_SNR = "Satellite.SNR";
    private static final String MSG_INSUFFICIENT_SATELLITES = "Insufficient satellites in use to calculate altitude (more than three required)";

    public static final int INSTRUMENT_LOG_COLUMN_COUNT = 13;


    /***********************************************************************************************
     * Instantiate the specified GPS Receiver.
     *
     * @param classname
     * @param dao
     * @param rxtype
     * @param resourcekey
     *
     * @return GpsReceiverInterface
     */

    public static GpsInstrumentReceiverInterface instantiateReceiver(final String classname,
                                                                     final ObservatoryInstrumentDAOInterface dao,
                                                                     final String rxtype,
                                                                     final String resourcekey)
        {
        final String SOURCE = "GpsReceiverHelper.instantiateReceiver() ";
        Object objReturn;

        LOGGER.debugGpsEvent(dao.getHostInstrument().isDebugMode(),
                             SOURCE + "[classname=" + classname + "]");

        // Some basic error traps...
        // A null DAO is permissible
        if ((classname == null)
            || (EMPTY_STRING.equals(classname))
            || (rxtype == null)
            || (EMPTY_STRING.equals(rxtype))
            || (resourcekey == null))
            {
            return (null);
            }

        objReturn = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            boolean boolInstantiated;

            boolInstantiated = false;

            // Does the target implement the GpsInstrumentReceiverInterface interface?
            classObject = Class.forName(classname);
            interfaces = classObject.getInterfaces();

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0;
                         ((i < interfaces.length) && (!boolInstantiated));
                         i++)
                        {
                        if (GpsInstrumentReceiverInterface.class.getName().equals(interfaces[i].getName()))
                            {
                            final Class superClass;

                            // We have found the GpsInstrumentReceiverInterface interface
                            LOGGER.debugGpsEvent(dao.getHostInstrument().isDebugMode(),
                                                 SOURCE + "[" + classname + " implements " + GpsInstrumentReceiverInterface.class.getName() + "]");

                            // Prove that the real Receiver is a subclass of AbstractNMEAGpsReceiver
                            superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (AbstractNMEAGpsReceiver.class.getName().equals(superClass.getName()))
                                    {
                                    final Constructor[] constructors;

                                    // We are dealing with the right kind of object...
                                    LOGGER.debugGpsEvent(dao.getHostInstrument().isDebugMode(),
                                                         SOURCE + "[" + classname + " is a subclass of " + AbstractNMEAGpsReceiver.class.getName() + "]");

                                    // Get hold of the Constructors for the requested class name
                                    constructors = classObject.getDeclaredConstructors();

                                    if ((constructors != null)
                                        && (constructors.length >= 1))
                                        {
                                        // Step through all constructors (there should be only one),
                                        // and find one which takes a DAO and two Strings
                                        for (int j = 0;
                                             ((!boolInstantiated) && (j < constructors.length));
                                             j++)
                                            {
                                            final Class classParameters[];

                                            classParameters = constructors[j].getParameterTypes();

                                            // Only check the three-parameter constructors, class in [j]
                                            if (classParameters.length == 3)
                                                {
                                                // Check the parameter types
                                                if ((classParameters[0].getName().equals(ObservatoryInstrumentDAOInterface.class.getName()))
                                                    && (classParameters[1].getName().equals(String.class.getName()))
                                                    && (classParameters[2].getName().equals(String.class.getName())))
                                                    {
                                                    // It is safe to try to instantiate
                                                    // Constructor must take one DAO and two Strings
                                                    final Object[] objArguments;

                                                    objArguments = new Object[3];
                                                    objArguments[0] = dao;
                                                    objArguments[1] = rxtype;
                                                    objArguments[2] = resourcekey;

                                                    // If we get this far, we have a valid GpsReceiver
                                                    objReturn = constructors[j].newInstance(objArguments);
                                                    boolInstantiated = true;
                                                    }
                                                else
                                                    {
                                                    // Constructor has incorrect parameter type
                                                    //LOGGER.log(SOURCE + "Incorrect parameter type [parameter=" + classParameters[0].getName() + "]");
                                                    }
                                                }
                                            else
                                                {
                                                // Constructor has incorrect number of parameters
                                                //LOGGER.log(SOURCE + "Constructor has an incorrect number of parameters");
                                                }
                                            }
                                        }
                                    else
                                        {
                                        // Constructor is null or empty
                                        LOGGER.error(SOURCE + "Constructor is null or empty");
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Class is not a subclass of " + AbstractNMEAGpsReceiver.class.getName());
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Class has no superclass");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "No interfaces found");
                }
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(SOURCE + "InstantiationException [classname=" + classname + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(SOURCE + "IllegalAccessException [classname=" + classname + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + "IllegalArgumentException [classname=" + classname + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(SOURCE + "InvocationTargetException [classname=" + classname + "]");
            }

        catch (ClassNotFoundException exception)
            {
            LOGGER.error(SOURCE + "ClassNotFoundException [classname=" + classname + "]");
            }

        catch (NullPointerException exception)
            {
            LOGGER.error(SOURCE + "NullPointerException [classname=" + classname + "]");
            }

        return ((GpsInstrumentReceiverInterface)objReturn);
        }


    /***********************************************************************************************
     * Add Location Metadata Values (Longitude, Latitude, HASL) from the GpsReceiver.
     * These are currently added to the DAO ObservationMetadata.
     *
     * @param metadatalist
     * @param receiver
     * @param debug
     */

    private static void addGpsReceiverLocationMetadataValues(final List<Metadata> metadatalist,
                                                             final GpsInstrumentReceiverInterface receiver,
                                                             final boolean debug)
        {
        final String SOURCE = "GpsReceiverHelper.addGpsReceiverLocationMetadataValues() ";

        if ((metadatalist != null)
            && (receiver != null)
            && (receiver.getSatellitesInUseCount() > 0)
            && (receiver.getSatellitesInViewCount() > 0)
            && (receiver.getDataQuality() > 0)
            && (receiver.getFixType() > 1))
            {
            LOGGER.debugGpsEvent(debug,
                                 SOURCE
                                    + "[longitude=" + receiver.getLongitude().toString()
                                    + "] [latitude=" + receiver.getLatitude().toString()
                                    + "] [hasl=" + receiver.getAltitudeASL() + "]");

            // Tell the GpsReceiver Instrument where we are...
            // Note that the GPS Rx returns location in E/W, N/S format, to agree with the Rx device

            // Longitude
            MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                            MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                                            receiver.getLongitude().toString(),
                                                            REGEX_LONGITUDE_DMS_EW,
                                                            DataTypeDictionary.SIGNED_LONGITUDE,
                                                            SchemaUnits.DEG_MIN_SEC,
                                                            "The current Longitude fix as ddd:mm:ss.sss (E/W)",
                                                            "Longitude fix",
                                                            debug);
            // Latitude
            MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                            MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                                            receiver.getLatitude().toString(),
                                                            REGEX_LATITUDE_DMS_NS,
                                                            DataTypeDictionary.LATITUDE,
                                                            SchemaUnits.DEG_MIN_SEC,
                                                            "The current Latitude fix as dd:mm:ss.sss (N/S)",
                                                            "Latitude fix",
                                                            debug);
            // HASL
            MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                            MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                                            Double.toString(receiver.getAltitudeASL()),
                                                            REGEX_HASL,
                                                            DataTypeDictionary.DECIMAL_FLOAT,
                                                            SchemaUnits.M,
                                                            "The current Height Above Sea Level in metres",
                                                            "Height Above Sea Level",
                                                            debug);
            MetadataHelper.showMetadataList(metadatalist,
                                            "Location Metadata Values",
                                            debug);
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to add GpsReceiver location Metadata values");
            }
        }


    /***********************************************************************************************
     * Add the UpdateSource and UpdateTarget used to obtain the Fix.
     * These are currently added to the DAO ObservationMetadata.
     *
     * @param metadatalist
     * @param receiver
     * @param sourcekey
     * @param source
     * @param targetkey
     * @param target
     * @param debug
     */

    private static void addGpsReceiverUpdateConfiguration(final List<Metadata> metadatalist,
                                                          final GpsInstrumentReceiverInterface receiver,
                                                          final String sourcekey,
                                                          final UpdateSource source,
                                                          final String targetkey,
                                                          final UpdateTarget target,
                                                          final boolean debug)
        {
        final String SOURCE = "GpsReceiverHelper.addGpsReceiverUpdateConfiguration() ";

        if ((metadatalist != null)
            && (receiver != null)
            && (source != null)
            && (sourcekey != null)
            && (!EMPTY_STRING.equals(sourcekey))
            && (target != null)
            && (targetkey != null)
            && (!EMPTY_STRING.equals(targetkey)))
            {
            LOGGER.debugGpsEvent(debug,
                                 SOURCE + "["
                                     + sourcekey
                                     + "=" + source.getName()
                                     + "] ["
                                     + targetkey
                                     + "=" + target.getName() + "]");
            // Source
            MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                            sourcekey,
                                                            source.getName(),
                                                            REGEX_STRING,
                                                            DataTypeDictionary.STRING,
                                                            SchemaUnits.DIMENSIONLESS,
                                                            "The UpdateSource used for obtaining the Fix",
                                                            "UpdateSource",
                                                            debug);
            // Target
            MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                            targetkey,
                                                            target.getName(),
                                                            REGEX_STRING,
                                                            DataTypeDictionary.STRING,
                                                            SchemaUnits.DIMENSIONLESS,
                                                            "The UpdateTarget to which the Fix is applied",
                                                            "UpdateTarget",
                                                            debug);
            MetadataHelper.showMetadataList(metadatalist,
                                            "Location Metadata Values",
                                            debug);
            }
        }


    /***********************************************************************************************
     * Add the CentroidOfFixes Metadata
     * These are currently added to the DAO ObservationMetadata.
     *
     * @param metadatalist
     * @param fixlist
     * @param debug
     */

    public static void addGpsReceiverCentroidOfFixesMetadata(final List<Metadata> metadatalist,
                                                             final List<math.geom2d.Point2D> fixlist,
                                                             final boolean debug)
        {
        final String SOURCE = "GpsReceiverHelper.addGpsReceiverCentroidOfFixesMetadata() ";

        if ((metadatalist != null)
            && (!metadatalist.isEmpty())
            && (fixlist != null)
            && (!fixlist.isEmpty()))
            {
            final math.geom2d.Point2D pointCentroidOfFixes;
            final Point2D.Double pointCentroidAsDoubles;

            LOGGER.debug(debug,
                         SOURCE + "Calculate Centroid [fix.count=" + fixlist.size() + "]");

            pointCentroidOfFixes = math.geom2d.Point2D.centroid(fixlist);
            pointCentroidAsDoubles = pointCentroidOfFixes.getAsDouble();

            // Make very sure we haven't been given a duff value
            // X is Longitude
            // Y is Latitude
            if ((pointCentroidAsDoubles.getX() >= -180.0)
                && (pointCentroidAsDoubles.getX() <= 180.0)
                && (pointCentroidAsDoubles.getY() >= -90.0)
                && (pointCentroidAsDoubles.getY() <= 90.0))
                {
                final DegMinSecInterface dmsLongitude;
                final DegMinSecInterface dmsLatitude;

                dmsLongitude = new LongitudeDataType(pointCentroidAsDoubles.getX());
                dmsLatitude = new LatitudeDataType(pointCentroidAsDoubles.getY());

                // Longitude
                MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                                MetadataDictionary.KEY_OBSERVATION_CENTROID_LONGITUDE.getKey(),
                                                                dmsLongitude.toString(),
                                                                REGEX_LONGITUDE_DMS_SIGNED,
                                                                DataTypeDictionary.SIGNED_LONGITUDE,
                                                                SchemaUnits.DEG_MIN_SEC,
                                                                "The centroid of Longitude as ddd:mm:ss.sss  East is positive",
                                                                "Centroid of Longitude",
                                                                debug);
                // Latitude
                MetadataHelper.createOrUpdateMetadataItemTraced(metadatalist,
                                                                MetadataDictionary.KEY_OBSERVATION_CENTROID_LATITUDE.getKey(),
                                                                dmsLatitude.toString(),
                                                                REGEX_LATITUDE_DMS_SIGNED,
                                                                DataTypeDictionary.LATITUDE,
                                                                SchemaUnits.DEG_MIN_SEC,
                                                                "The centroid of Latitude as dd:mm:ss.sss",
                                                                "Centroid of Latitude",
                                                                debug);
                MetadataHelper.showMetadataList(metadatalist,
                                                "Centroid Metadata",
                                                debug);
                }
            else
                {
                LOGGER.error(SOURCE + "Centroid out of range [longitude=" + pointCentroidAsDoubles.getX()
                                + "] [latitude=" + pointCentroidAsDoubles.getY() + "]");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Parameters");
            }
        }


    /***********************************************************************************************
     * Obtain the specified GPS Fix location and update the target.
     * If necessary, prune the 'E/W' off Longitude, the 'N/S' off Latitude, and the 'm' off HASL.
     *
     * @param obsinstrument
     * @param metadatalist
     * @param target
     * @param longitudekey
     * @param latitudekey
     * @param haslkey
     * @param errors
     * @param debug
     *
     * @throws NumberFormatException
     */

    public static void getFixAndUpdate(final ObservatoryInstrumentInterface obsinstrument,
                                       final List<Metadata> metadatalist,
                                       final UpdateTarget target,
                                       final String longitudekey,
                                       final String latitudekey,
                                       final String haslkey,
                                       final List<String> errors,
                                       final boolean debug) throws NumberFormatException
        {
        final String SOURCE = "GpsReceiverHelper.getFixAndUpdate() ";
        String strLongitude;
        String strLatitude;
        String strHASL;
        final DegMinSecInterface dmsLongitude;
        final DegMinSecInterface dmsLatitude;
        final double dblHASL;

        // Return NO_DATA if the key cannot be found in the List
        strLongitude = MetadataHelper.getMetadataValueByKey(metadatalist, longitudekey);
        strLatitude = MetadataHelper.getMetadataValueByKey(metadatalist, latitudekey);
        strHASL = MetadataHelper.getMetadataValueByKey(metadatalist, haslkey);

        if ((NO_DATA.equals(strLongitude))
            || (NO_DATA.equals(strLatitude))
            || (NO_DATA.equals(strHASL)))
            {
            LOGGER.error(SOURCE + "One or more Metadata Keys not found "
                             + "[longitude.key=" + longitudekey + "] [longitude.value=" + strLongitude + "] "
                             + "[latitude.key=" + latitudekey + "] [latitude.value=" + strLatitude + "] "
                             + "[hasl.key=" + haslkey + "] [hasl.value=" + strHASL + "]");

            MetadataHelper.showMetadataList(metadatalist,
                                            SOURCE + "GPS Receiver Metadata",
                                            true);
            }

        // Prune the 'E/W' off Longitude, the 'N/S' off Latitude, and the 'm' off HASL
        // Restore the sign in order to parse back correctly!
        if (strLongitude.endsWith("E"))
            {
            strLongitude = MINUS + strLongitude.substring(0, strLongitude.length()-1);
            }

        // Longitude is POSITIVE to the WEST
        if (strLongitude.endsWith("W"))
            {
            strLongitude = strLongitude.substring(0, strLongitude.length()-1);
            }

        // Latitude is POSITIVE to the NORTH
        if (strLatitude.endsWith("N"))
            {
            strLatitude = strLatitude.substring(0, strLatitude.length()-1);
            }

        if (strLatitude.endsWith("S"))
            {
            strLatitude = MINUS + strLatitude.substring(0, strLatitude.length()-1);
            }

        if ((strHASL.endsWith("m"))
            || (strHASL.endsWith("M")))
            {
            strHASL = strHASL.substring(0, strHASL.length()-1);
            }

        // Return NULL if parsing failed
        dmsLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strLongitude,
                                                                                       DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                       EMPTY_STRING,
                                                                                       EMPTY_STRING,
                                                                                       errors);
        dmsLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strLatitude,
                                                                                      DataTypeDictionary.LATITUDE,
                                                                                      EMPTY_STRING,
                                                                                      EMPTY_STRING,
                                                                                      errors);
        dblHASL = Double.parseDouble(strHASL);

        // Update is possible only if all data were valid
        doFixUpdate(obsinstrument, target, dmsLongitude, dmsLatitude, dblHASL, errors, debug);
        }


    /************************************************************************************************
     * Update the specified UpdateTarget with the location information.
     *
     * @param obsinstrument
     * @param target
     * @param longitude
     * @param latitude
     * @param dblHASL
     * @param errors
     * @param debug
     */

    private static void doFixUpdate(final ObservatoryInstrumentInterface obsinstrument,
                                    final UpdateTarget target,
                                    final DegMinSecInterface longitude,
                                    final DegMinSecInterface latitude,
                                    final double dblHASL,
                                    final List<String> errors,
                                    final boolean debug)
        {
        if ((target != null)
            && (longitude != null)
            && (latitude != null))
            {
            switch (target)
                {
                case UPDATE_ALL:
                    {
                    applyFixToFrameworkMetadata(longitude, latitude, dblHASL, debug);

                    // Do we have an Observatory?
                    if ((obsinstrument != null)
                        && (obsinstrument.getContext() != null)
                        && (obsinstrument.getContext().getObservatory() != null))
                        {
                        // Update the master Metadata directly, all DAOs should follow suit
                        applyFixToObservatoryMetadata(obsinstrument.getContext().getObservatory().getObservatoryMetadata(),
                                                      longitude,
                                                      latitude,
                                                      dblHASL,
                                                      debug);

                        // Tell anyone else that might be interested
                        if (obsinstrument.getDAO() != null)
                            {
                            obsinstrument.getContext().getObservatory().notifyObservatoryMetadataChangedEvent(obsinstrument.getDAO(),
                                                                                                              EMPTY_STRING,
                                                                                                              MetadataItemState.EDIT);
                            }
                        }
                    break;
                    }

                case FRAMEWORK:
                    {
                    applyFixToFrameworkMetadata(longitude, latitude, dblHASL, debug);
                    break;
                    }

                case OBSERVATORY:
                    {
                    if ((obsinstrument != null)
                        && (obsinstrument.getContext() != null)
                        && (obsinstrument.getContext().getObservatory() != null))
                        {
                        // Update the master Metadata directly, all DAOs should follow suit
                        applyFixToObservatoryMetadata(obsinstrument.getContext().getObservatory().getObservatoryMetadata(),
                                                      longitude,
                                                      latitude,
                                                      dblHASL,
                                                      debug);

                        // Tell anyone else that might be interested
                        if (obsinstrument.getDAO() != null)
                            {
                            obsinstrument.getContext().getObservatory().notifyObservatoryMetadataChangedEvent(obsinstrument.getDAO(),
                                                                                                          EMPTY_STRING,
                                                                                                          MetadataItemState.EDIT);
                            }
                        }
                    break;
                    }

                case NO_UPDATE:
                    {
                    break;
                    }

                default:
                    {
                    // Unrecognised UpdateTarget
                    errors.add("Unrecognised UpdateTarget [target=" + target.getName() + "]");
                    }
                }
            }
        else
            {
            // Unable to retrieve Longitude, Latitude, HASL
            errors.add("Unable to retrieve one or more of Longitude, Latitude, HASL");
            }
        }


    /***********************************************************************************************
     * Update the Framework location from the specified fix, if possible.
     *
     * Updates Metadata:
     *      Framework.Longitude
     *      Framework.Latitude
     *      Framework.HASL
     *
     * @param dmsLongitude
     * @param dmsLatitude
     * @param dblHASL
     * @param debugmode
     */

    private static void applyFixToFrameworkMetadata(final DegMinSecInterface dmsLongitude,
                                                    final DegMinSecInterface dmsLatitude,
                                                    final double dblHASL,
                                                    final boolean debugmode)
        {
        // These convert the DisplayFormat to DegMinSecFormat.SIGN regardless of the incoming format
        // and also set the appropriate number of decimal places
        // These write to the {Longitude, Latitude, HASL} in the Framework Metadata
        REGISTRY.getFramework().setLongitude(dmsLongitude);
        REGISTRY.getFramework().setLatitude(dmsLatitude);
        REGISTRY.getFramework().setHASL(dblHASL);

        LOGGER.debugGpsEvent(debugmode,
                             METADATA_FRAMEWORK_UPDATE
                                 + METADATA_LONGITUDE
                                 + dmsLongitude.toString()
                                 + TERMINATOR_SPACE
                                 + METADATA_LATITUDE
                                 + dmsLatitude.toString()
                                 + TERMINATOR_SPACE
                                 + METADATA_HASL
                                 + Double.toString(dblHASL)
                                 + TERMINATOR);
        }


    /***********************************************************************************************
     * Update the Observatory location from the specified fix, if possible.
     *
     * Updates Metadata:
     *      Observatory.Longitude
     *      Observatory.Latitude
     *      Observatory.HASL
     *
     * @param metadatalist
     * @param longitude
     * @param latitude
     * @param dblHASL
     * @param debugmode
     */

    private static void applyFixToObservatoryMetadata(final List<Metadata> metadatalist,
                                                      final DegMinSecInterface longitude,
                                                      final DegMinSecInterface latitude,
                                                      final double dblHASL,
                                                      final boolean debugmode)
        {
        final String SOURCE = "GpsReceiverHelper.applyFixToObservatoryMetadata() ";

        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            final Metadata metaDataLongitude;
            final Metadata metaDataLatitude;
            final Metadata metaDataHASL;
            boolean boolSuccess;

            // The incoming Longitude and Latitude should be configured as SIGNED, with correct field width
            // toString() will then give the correct format for the MetadataValue
            longitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
            longitude.setDisplayFormat(DegMinSecFormat.SIGN);

            latitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
            latitude.setDisplayFormat(DegMinSecFormat.SIGN);

            // Do we already have this Metadata item in the Observatory Metadata?
            metaDataLongitude = MetadataHelper.getMetadataByKey(metadatalist,
                                                                MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());
            // Found the Key, so just update the Value
            boolSuccess = MetadataHelper.setValueOnlyIfValid(metaDataLongitude, longitude.toString());

            if (!boolSuccess)
                {
                LOGGER.error(SOURCE + "Unable to update Observatory.Longitude, now using original value");
                }

            metaDataLatitude = MetadataHelper.getMetadataByKey(metadatalist,
                                                               MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());
            boolSuccess = MetadataHelper.setValueOnlyIfValid(metaDataLatitude, latitude.toString());

            if (!boolSuccess)
                {
                LOGGER.error(SOURCE + "Unable to update Observatory.Latitude, now using original value");
                }

            metaDataHASL = MetadataHelper.getMetadataByKey(metadatalist,
                                                           MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());
            boolSuccess = MetadataHelper.setValueOnlyIfValid(metaDataHASL, Double.toString(dblHASL));

            if (!boolSuccess)
                {
                LOGGER.error(SOURCE + "Unable to update Observatory.HASL, now using original value");
                }

            LOGGER.debugGpsEvent(debugmode,
                                 METADATA_OBSERVATORY_UPDATE
                                     + METADATA_LONGITUDE
                                     + longitude.toString()
                                     + TERMINATOR_SPACE
                                     + METADATA_LATITUDE
                                     + latitude.toString()
                                     + TERMINATOR_SPACE
                                     + METADATA_HASL
                                     + Double.toString(dblHASL)
                                     + TERMINATOR);
            }
        }


    /***********************************************************************************************
     * Log the outcome of an individual GPS Fix in the InstrumentLog.
     * Return the ResponseValue.
     *
     * @param logfragment
     * @param gpsrx
     * @param metadatalist
     * @param sourcekey
     * @param source
     * @param targetkey
     * @param target
     * @param validfix
     * @param clock
     * @param debug
     *
     * @return String
     */

    public static String logFixOutcome(final Vector<Vector> logfragment,
                                       final GpsInstrumentReceiverInterface gpsrx,
                                       final List<Metadata> metadatalist,
                                       final String sourcekey,
                                       final UpdateSource source,
                                       final String targetkey,
                                       final UpdateTarget target,
                                       final boolean validfix,
                                       final ObservatoryClockInterface clock,
                                       final boolean debug)
        {
        final String SOURCE = "GpsReceiverHelper.logFixOutcome() ";
        String strResponseValue;

        strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;

        if ((logfragment != null)
            && (gpsrx != null)
            && (metadatalist != null
            && (clock != null)))
            {
            final Vector<Object> vecInstrumentLogEntry;

            vecInstrumentLogEntry = new Vector<Object>(INSTRUMENT_LOG_COLUMN_COUNT);

            if (validfix)
                {
                // Prepare a Log entry for each valid GpsReceiver fix
                vecInstrumentLogEntry.add(ObservatoryInstrumentDAOInterface.IMAGE_ICON_PLAIN);
                vecInstrumentLogEntry.add(gpsrx.getDateOfFix().toString());
                vecInstrumentLogEntry.add(gpsrx.getTimeOfFix().toString());
                vecInstrumentLogEntry.add(gpsrx.getLongitude().toString());
                vecInstrumentLogEntry.add(gpsrx.getLatitude().toString());
                vecInstrumentLogEntry.add(Double.toString(gpsrx.getAltitudeASL()));
                vecInstrumentLogEntry.add(Double.toString(gpsrx.getGeoidAltitude()));
                vecInstrumentLogEntry.add(Double.toString(gpsrx.getMagneticVariation()));
                vecInstrumentLogEntry.add(Double.toString(gpsrx.getHDOP()));
                vecInstrumentLogEntry.add(Double.toString(gpsrx.getVDOP()));
                vecInstrumentLogEntry.add(Integer.toString(gpsrx.getFixType()));
                vecInstrumentLogEntry.add(Integer.toString(gpsrx.getSatellitesInUseCount()));

                // Only update altitude if we have four or more satellites
                if ((gpsrx.getSatellitesInUseCount() > 3)
                    && (gpsrx.getSatellitesInViewCount() > 3))
                    {
                    vecInstrumentLogEntry.add(EMPTY_STRING);
                    }
                else
                    {
                    vecInstrumentLogEntry.add(MSG_INSUFFICIENT_SATELLITES);
                    }

                // Tell the GpsReceiver Instrument where we are...
                // The actual fix is only held in the DAO Observation MetaData
                addGpsReceiverLocationMetadataValues(metadatalist,
                                                     gpsrx,
                                                     debug);

                // Add the Update configuration, so we know how to use the Fix
                addGpsReceiverUpdateConfiguration(metadatalist,
                                                  gpsrx,
                                                  sourcekey,
                                                  source,
                                                  targetkey,
                                                  target,
                                                  debug);
                // The ResponseValue
                strResponseValue = "[latitude="
                                   + gpsrx.getLatitude().toString()
                                   + "] [longitude="
                                   + gpsrx.getLongitude().toString()
                                   + "] [hasl="
                                   + gpsrx.getAltitudeASL()
                                   + "] [inuse="
                                   + Integer.toString(gpsrx.getSatellitesInUseCount())
                                   + "] [inview="
                                   + Integer.toString(gpsrx.getSatellitesInViewCount())
                                   + "]";
                }
            else
                {
                // Log any errors
                vecInstrumentLogEntry.add(ObservatoryInstrumentDAOInterface.IMAGE_ICON_WARNING);
                vecInstrumentLogEntry.add(ChronosHelper.toDateString(clock.getCalendarDateNow()));
                vecInstrumentLogEntry.add(ChronosHelper.toTimeString(clock.getCalendarTimeNow()));
                vecInstrumentLogEntry.add(ResponseMessageStatus.RESPONSE_NO_DATA_HTML);
                vecInstrumentLogEntry.add(ResponseMessageStatus.RESPONSE_NO_DATA_HTML);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(EMPTY_STRING);
                vecInstrumentLogEntry.add(ResponseMessageStatus.RESPONSE_NO_DATA_HTML);

                // The ResponseValue
                strResponseValue = ResponseMessageStatus.RESPONSE_NODATA;
                }

            logfragment.add(vecInstrumentLogEntry);
            }

        return (strResponseValue);
        }


    /**********************************************************************************************/
    /* Metadata Utilities                                                                         */
    /***********************************************************************************************
     * Create Metadata for the GpsReceiver data Channels.
     * Three Channels of data are collected, Longitude, Latitude and HASL.
     * Each Channel requires {Name, Value, DataType, Units, Description, Colour}.
     * The Value (last sample) is added by the DAO during data capture.
     * Include also the Scatter Plot Chart Title and Axis Labels.
     * These are currently added to the DAO ObservationMetadata.
     *
     * @param debug
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createGpsReceiverChannelMetadata(final boolean debug)
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(10);

        // Each Channel requires {Name, Value, DataType, Units, Description, Colour}
        // The Value (last sample) is added by the DAO during data capture

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // Channel 0 - Longitude

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      "Longitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_NAME + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      DataTypeDictionary.SIGNED_LONGITUDE.getName(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      SchemaUnits.DEG_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      "The Longitude fix produced by the GPS Receiver",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      "r=000 g=255 b=000",
                                      REGEX_COLOUR,
                                      DataTypeDictionary.COLOUR_DATA,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ZERO);

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // Channel 1 - Latitude

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                      "Latitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_NAME + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                      DataTypeDictionary.LATITUDE.getName(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                      SchemaUnits.DEG_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                      "The Latitude fix produced by the GPS Receiver",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                      "r=000 g=255 b=000",
                                      REGEX_COLOUR,
                                      DataTypeDictionary.COLOUR_DATA,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // Channel 2 - HASL

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                      "Height Above Sea Level",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_NAME + MetadataDictionary.SUFFIX_CHANNEL_TWO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                      DataTypeDictionary.DECIMAL_FLOAT.getName(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DATATYPE + MetadataDictionary.SUFFIX_CHANNEL_TWO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                      SchemaUnits.M.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_UNITS + MetadataDictionary.SUFFIX_CHANNEL_TWO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                      "The Height Above Sea Level produced by the GPS Receiver",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_DESCRIPTION + MetadataDictionary.SUFFIX_CHANNEL_TWO);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO,
                                      "r=000 g=000 b=255",
                                      REGEX_COLOUR,
                                      DataTypeDictionary.COLOUR_DATA,
                                      SchemaUnits.DIMENSIONLESS,
                                      MetadataFactory.DESCRIPTION_CHANNEL_COLOUR + MetadataDictionary.SUFFIX_CHANNEL_TWO);

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // Chart

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      "GPS Fix Scatter Plot",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      FrameworkStrings.EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      "Longitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      FrameworkStrings.EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      "Latitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      FrameworkStrings.EMPTY_STRING);

        MetadataHelper.showMetadataList(listMetadata,
                                        "GPS Receiver Channel Metadata",
                                        debug);

        return (listMetadata);
        }


    /***********************************************************************************************
     * Get the Gps Receiver Instrument Log Metadata, i.e describing the columns of the Log.
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createGpsReceiverInstrumentLogMetadata()
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(INSTRUMENT_LOG_COLUMN_COUNT << 2);

        //------------------------------------------------------------------------------------------
        // Names

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      "Icon",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      "Date",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      "Time",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      "Longitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Longitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      "Latitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Latitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      "HeightASL",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the HeightASL column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      "GeoidAltitude",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the GeoidAltitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      "Variation",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Variation column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      "HDOP",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the HDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      "VDOP",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the VDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      "FixType",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the FixType column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      "Satellites",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Satellites column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_NAME.getKey() + MetadataDictionary.SUFFIX_COLUMN_12,
                                      "Status",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The name of the Status column");

        //------------------------------------------------------------------------------------------
        // DataTypes

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      DataTypeDictionary.IMAGE_DATA.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      DataTypeDictionary.DATE_YYYY_MM_DD.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      DataTypeDictionary.TIME_HH_MM_SS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      DataTypeDictionary.SIGNED_LONGITUDE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Longitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      DataTypeDictionary.LATITUDE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Latitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the HeightASL column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the GeoidAltitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Variation column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the HDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      DataTypeDictionary.DECIMAL_DOUBLE.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the VDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      DataTypeDictionary.DECIMAL_INTEGER.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the FixType column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      DataTypeDictionary.DECIMAL_INTEGER.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Satellites column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_METADATA_DATATYPE.getKey() + MetadataDictionary.SUFFIX_COLUMN_12,
                                      DataTypeDictionary.STRING.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.DATA_TYPE,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The DataType of the Status column");

        //------------------------------------------------------------------------------------------
        // Units

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      SchemaUnits.YEAR_MONTH_DAY.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      SchemaUnits.HOUR_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      SchemaUnits.DEG_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Longitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      SchemaUnits.DEG_MIN_SEC.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Latitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      SchemaUnits.M.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the HeightASL column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      SchemaUnits.M.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the GeoidAltitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      SchemaUnits.DEGREES.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Variation column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the HDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the VDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the FixType column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Satellites column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_UNITS.getKey() + MetadataDictionary.SUFFIX_COLUMN_12,
                                      SchemaUnits.DIMENSIONLESS.toString(),
                                      REGEX_NONE,
                                      DataTypeDictionary.UNITS,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Units of the Status column");

        //------------------------------------------------------------------------------------------
        // Descriptions

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_0,
                                      "An icon showing the event status",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Icon column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_1,
                                      "The Date of the log entry",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Date column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_2,
                                      "The Time of the log entry",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Time column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_3,
                                      "The Longitude of the GPS fix",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Longitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_4,
                                      "The Latitude of the GPS fix",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Latitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_5,
                                      "The Height Above Sea Level of the GPS fix",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the HeightASL column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_6,
                                      "The height above the reference geoid",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the GeoidAltitude column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_7,
                                      "The Variation of the GPS fix (Easterley subtracts from the course)",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Variation column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_8,
                                      "Relative value of Horizontal Dilution of Precision",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the HDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_9,
                                      "Relative value of Vertical Dilution of Precision",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the VDOP column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_10,
                                      "The type of the GPS Fix 0=None, 1=Non-differential, 2=Differential, 6=Estimated",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the FixType column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_11,
                                      "The number of Satellites used for the fix",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Satellites column");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_COLUMN_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_COLUMN_12,
                                      "The status of the GPS Fix",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Description of the Status column");

        return (listMetadata);
        }


    /**********************************************************************************************/
    /* Satellite Mapping Utilities                                                                */
    /***********************************************************************************************
     * Convert SatelliteData to StarMapPoint.
     *
     * The data arrive as Vector{Vector{SatelliteData}} in getWrappedData().getRawData()
     * and must be presented to the StarMap as Vector{Vector{StarMapPointInterface}}
     * using getGpsSatellitePlugin().setStarMapPoints().
     *
     * @param rawdata
     * @param plugin
     *
     * @return Vector{Vector{StarMapPointInterface}}
     */

    public static Vector<Vector<StarMapPointInterface>> convertSatelliteDataToStarMap(final Vector<Object> rawdata,
                                                                                      final StarMapPlugin plugin)
        {
        final Vector<Vector<StarMapPointInterface>> vecCollection;

        // There can only be MAX_SATELLITES in the collection
        vecCollection = new Vector<Vector<StarMapPointInterface>>(GpsInstrumentReceiverInterface.MAX_SATELLITES);

        if (rawdata != null)
            {
            final Iterator<Object> iterRawData;

            iterRawData = rawdata.iterator();

            while (iterRawData.hasNext())
                {
                final Object objVector;
                int intID;
                int intSNRdb;

                objVector = iterRawData.next();
                intSNRdb = -1;

                if ((objVector != null)
                    && (objVector instanceof Vector))
                    {
                    final Vector vecSingleSatellite;
                    final Iterator iterSingleSatellite;
                    final Vector<StarMapPointInterface> vecSingleSatelliteConvertedPoints;

                    vecSingleSatellite = (Vector)objVector;
                    iterSingleSatellite = vecSingleSatellite.iterator();
                    vecSingleSatelliteConvertedPoints = new Vector<StarMapPointInterface>(500);

                    while (iterSingleSatellite.hasNext())
                        {
                        final Object objSatelliteData;

                        objSatelliteData = iterSingleSatellite.next();

                        if ((objSatelliteData != null)
                            && (objSatelliteData instanceof SatelliteData))
                            {
                            final SatelliteData satelliteData;
                            final StarMapPointInterface point;

                            satelliteData = (SatelliteData)objSatelliteData;
                            intID = satelliteData.getSatellitePRN();

                            // SAve the last SNR for use later
                            intSNRdb = satelliteData.getSNRdB();

                            // All points on each track are not clickable, except the last
                            // No Metadata yet...
                            point = new StarMapPoint(plugin,
                                                     intID,
                                                     Integer.toString(intID),
                                                     CoordinateType.AZEL,
                                                     new Point2D.Double(satelliteData.getAzimuth(), satelliteData.getElevation()),
                                                     plugin.getColour(),
                                                     StarMapPointInterface.NOTCLICKABLE,
                                                     null);
                            vecSingleSatelliteConvertedPoints.add(point);
                            }
                        }

                    // Make the last point inherit the plugin's clickable state
                    if (!vecSingleSatelliteConvertedPoints.isEmpty())
                        {
                        final StarMapPointInterface pointEnd;

                        // Inherit the clickability of the plugin
                        pointEnd = vecSingleSatelliteConvertedPoints.lastElement();
                        pointEnd.setClickable(plugin.isClickable());

                        // Only add Metadata to the last Point if it is clickable
                        if (plugin.isClickable())
                            {
                            final Vector<Metadata> metaData;

                            metaData = new Vector<Metadata>(2);

                            MetadataHelper.addNewMetadata(metaData,
                                                          KEY_SATELLITE_ID,
                                                          Integer.toString(pointEnd.getPointID()),
                                                          REGEX_STRING,
                                                          DataTypeDictionary.DECIMAL_INTEGER,
                                                          SchemaUnits.DIMENSIONLESS,
                                                          "The Satellite ID");

                            MetadataHelper.addNewMetadata(metaData,
                                                          KEY_SATELLITE_SNR,
                                                          Integer.toString(intSNRdb),
                                                          REGEX_STRING,
                                                          DataTypeDictionary.DECIMAL_INTEGER,
                                                          SchemaUnits.D_B,
                                                          "The Satellite Signal to Noise Ratio");

                            pointEnd.setMetadata(metaData);
                            }

                        //System.out.println("convert SATELLITE ID=" + intID + "  points=" + vecSingleSatelliteConvertedPoints.size());
                        vecCollection.add(vecSingleSatelliteConvertedPoints);
                        }
                    }
                }
            }

        return (vecCollection);
        }


    /***********************************************************************************************
     * Accumulate the satellites in view from the current fix into the overall historical positions.
     *
     * The returned Vector contains sub-Vectors of history for each satellite,
     * suitable for use as RawData by the DAO:
     *
     * vector  satellite_id0  -->  location0  location1  location2 location3 ...
     * vector  satellite_id1  -->  locationa  locationb  ...
     * vector  satellite_id2  -->  locationx  locationy  locationz ...
     *
     * @param history
     * @param inview
     *
     * @return Vector<Object>
     */

    public static Vector<Object> accumulateSatellitesInView(final Map<Integer, Vector<SatelliteData>> history,
                                                            final List<SatelliteData> inview)
        {
        final String SOURCE = "GpsReceiverHelper.accumulateSatellitesInView() ";
        final Vector<Object> vecRawData;

        vecRawData = new Vector<Object>(GpsInstrumentReceiverInterface.MAX_SATELLITES_IN_VIEW);

        // Are there any satellites to accumulate?
        if ((history != null)
            && (inview != null)
            && (!inview.isEmpty()))
            {
            List<SatelliteData> listSatellitesInView;

            // Protect against changes in the incoming List of satellites in view
            listSatellitesInView = new CopyOnWriteArrayList<SatelliteData>(inview);

            synchronized(listSatellitesInView)
                {
                // Examine each satellite in view,
                // and put it under the appropriate key in the overall Map of satellite history
                for (int i = 0; i < listSatellitesInView.size(); i++)
                    {
                    final SatelliteData satelliteInView;
                    Vector<SatelliteData> vecSingleSatelliteHistory;

                    // Get the next satellite in view to accumulate
                    satelliteInView = listSatellitesInView.get(i);

                    // See if the history Map already contains an entry for this satellite ID
                    vecSingleSatelliteHistory = history.get(satelliteInView.getSatellitePRN());

                    if (vecSingleSatelliteHistory != null)
                        {
                        // Entry already exists, so just add the latest position to the satellite's Vector
                        vecSingleSatelliteHistory.add(satelliteInView);
                        }
                    else
                        {
                        // We need to create a new entry for this satellite
                        vecSingleSatelliteHistory = new Vector<SatelliteData>(10);
                        vecSingleSatelliteHistory.add(satelliteInView);

                        history.put(satelliteInView.getSatellitePRN(),
                                    vecSingleSatelliteHistory);
                        }
                    }

                // Try to help the GC?
                listSatellitesInView = null;
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to accumulate satellites in view");
            }

        // Now convert the whole historical collection to the DAO internal format for return
        // The historical Map cannot change during this iteration,
        // because no-one else is writing to it
        if ((history != null)
            && (!history.isEmpty()))
            {
            final Set<Integer> setKeys;
            final Iterator<Integer> iterKeys;

            setKeys = history.keySet();

            if ((setKeys != null)
                && (!setKeys.isEmpty()))
                {
                iterKeys = setKeys.iterator();

                while (iterKeys.hasNext())
                    {
                    final Integer key;
                    final Vector<SatelliteData> vecSatelliteHistory;

                    key = iterKeys.next();

                    // Get the Map entry corresponding to this key...
                    vecSatelliteHistory = history.get(key);

                    // ,,,and add it to the returned Vector
                    vecRawData.add(vecSatelliteHistory);
                    }
                }
            }

        return (vecRawData);
        }


    /**********************************************************************************************/
    /* Debugging Utilities                                                                        */
    /***********************************************************************************************
     * Debug the GpsReceiver fix information.
     *
     * @param receiver
     * @param validfix
     */

    public static void showFixDebug(final GpsInstrumentReceiverInterface receiver,
                                    final boolean validfix)
        {
        if (receiver != null)
            {
            final boolean boolDebug;
            final Iterator<String> iterSupport;

            boolDebug = receiver.getHostDAO().getHostInstrument().isDebugMode();

            LOGGER.debugGpsEvent(boolDebug,
                                 GpsInstrumentReceiverInterface.LINE);
            LOGGER.debugGpsEvent(boolDebug,
                                 "GPS Receiver Fix");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[ReceiverName=" + receiver.getReceiverType() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[DateOfLastUpdate=" + receiver.getDateOfLastUpdate() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[ValidFix=" + validfix + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[DateOfFix=" + receiver.getDateOfFix().toString() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[TimeOfFix=" + receiver.getTimeOfFix().toString() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[Latitude=" + receiver.getLatitude().toString() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[Longitude=" + receiver.getLongitude().toString() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[SpeedKnots=" + receiver.getSpeedKnots() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[Course=" + receiver.getCourse() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[MagneticVariation=" + receiver.getMagneticVariation() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[DataQuality=" + receiver.getDataQuality() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[AltitudeASL=" + receiver.getAltitudeASL() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[GeoidAltitude=" + receiver.getGeoidAltitude() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[FixMode=" + receiver.getFixMode() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[FixType=" + receiver.getFixType() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[PDOP=" + receiver.getPDOP() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[HDOP=" + receiver.getHDOP() + "]");
            LOGGER.debugGpsEvent(boolDebug,
                                 INDENT + "[VDOP=" + receiver.getVDOP() + "]");

            //-------------------------------------------------------------------------------------
            // GPGSA: Get the IDs of the satellites in Use

            LOGGER.debugGpsEvent(boolDebug,
                                 "[SatellitesInUse=" + receiver.getSatellitesInUseCount() + "]");

            if (receiver.getSatellitesInUseCount() > 0)
                {
                final Iterator<String> iterInUse;

                iterInUse = receiver.getSatellitesInUseIDs().iterator();

                while (iterInUse.hasNext())
                    {
                    LOGGER.debugGpsEvent(boolDebug,
                                         INDENT + "[satellite=" + iterInUse.next() + "]");
                    }
                }

            //-------------------------------------------------------------------------------------
            // GPGSV: Get the List of the satellites in View

            LOGGER.debugGpsEvent(boolDebug,
                                 "[SatellitesInView=" + receiver.getSatellitesInViewCount() + "]" );

            if (receiver.getSatellitesInViewCount() > 0)
                {
                final Iterator<SatelliteData> iterInView;

                iterInView = receiver.getSatellitesInView().iterator();

                while (iterInView.hasNext())
                    {
                    final SatelliteData satelliteData;

                    satelliteData = iterInView.next();

                    LOGGER.debugGpsEvent(boolDebug,
                                         INDENT + "[satelliteinview="
                                         + satelliteData.getSatellitePRN() + ", "
                                         + satelliteData.getElevation() + ", "
                                         + satelliteData.getAzimuth() + ", "
                                         + satelliteData.getSNRdB() + "] ");
                    }
                }

            LOGGER.debugGpsEvent(boolDebug,
                                 GpsInstrumentReceiverInterface.LINE);

            LOGGER.debugGpsEvent(boolDebug,
                                 "NMEA Sentences Supported" );

            // Get the list of supported NMEA sentences
            iterSupport = receiver.getNMEASentences().iterator();

            while (iterSupport.hasNext())
                {
                LOGGER.debugGpsEvent(boolDebug,
                                     INDENT + "[NMEAsentence=" + iterSupport.next() + "]");
                }

            LOGGER.debugGpsEvent(boolDebug,
                                 GpsInstrumentReceiverInterface.LINE);
            }
        }


    /***********************************************************************************************
     * Debug the contents of RawData.
     *
     * The returned Vector contains sub-Vectors of history for each satellite,
     * suitable for use as RawData by the DAO:
     *
     * vector  satellite_id0  -->  location0  location1  location2 location3 ...
     * vector  satellite_id1  -->  locationa  locationb  ...
     * vector  satellite_id2  -->  locationx  locationy  locationz ...
     *
     * @param data
     */

    public static void debugRawDataAsSatelliteData(final Vector<Object> data)
        {
        if (data != null)
            {
            final Iterator<Object> iterData;
            StringBuffer buffer;

            iterData = data.iterator();

            while (iterData.hasNext())
                {
                final Object objVector;
                int intID;

                objVector = iterData.next();
                buffer = new StringBuffer();
                intID = -1;

                if ((objVector != null)
                    && (objVector instanceof Vector))
                    {
                    final Vector vecHistory;
                    final Iterator iterHistory;

                    vecHistory = (Vector)objVector;
                    iterHistory = vecHistory.iterator();

                    while (iterHistory.hasNext())
                        {
                        final Object objSatelliteData;

                        objSatelliteData = iterHistory.next();

                        if ((objSatelliteData != null)
                            && (objSatelliteData instanceof SatelliteData))
                            {
                            final SatelliteData satelliteData;

                            satelliteData = (SatelliteData)objSatelliteData;
                            intID = satelliteData.getSatellitePRN();

                            buffer.append("(");
                            buffer.append(satelliteData.getAzimuth());
                            buffer.append(", ");
                            buffer.append(satelliteData.getElevation());
                            buffer.append(") ");
                            }
                        }
                    }

                LOGGER.debugGpsEvent(true, Integer.toString(intID) + " " + buffer);
                }
            }
        }
    }
