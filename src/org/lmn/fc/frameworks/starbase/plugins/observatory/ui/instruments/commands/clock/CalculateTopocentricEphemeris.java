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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.clock;


import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.maths.AstroMath;
import org.lmn.fc.common.utilities.time.AstronomicalCalendar;
import org.lmn.fc.common.utilities.time.AstronomicalCalendarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.ephemerides.EphemerisDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.EphemeridesHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;


/***************************************************************************************************
 * CalculateTopocentricEphemeris.
 */

public final class CalculateTopocentricEphemeris implements FrameworkConstants,
                                                            FrameworkStrings,
                                                            FrameworkMetadata,
                                                            FrameworkSingletons,
                                                            ObservatoryConstants
    {
    private static final int MAX_INTERVALS = 20000;


    /***********************************************************************************************
     * Check to see that we are not going to exceed a sensible maximum number
     * of iteration intervals for the Ephemeris.
     *
     * @param jdstart
     * @param jdend
     * @param interval
     *
     * @return boolean
     */

    private static boolean isValidIntervalCount(final double jdstart,
                                                final double jdend,
                                                final int interval)
        {
        final boolean boolValid;

        boolValid = ((jdend - jdstart) * AstronomyConstants.SECONDS_PER_DAY / interval) < MAX_INTERVALS;

        return (boolValid);
        }


    /***********************************************************************************************
     * doCalculateTopocentricEphemeris().
     * Calculates a topocentric Ephemeris.
     * Return the data via the DAO's UserObject.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doCalculateTopocentricEphemeris(final ObservatoryInstrumentDAOInterface dao,
                                                                           final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "CalculateTopocentricEphemeris.doCalculateTopocentricEphemeris() ";
        final int PARAMETER_COUNT = 7;
        final int INDEX_TARGET = 0;
        final int INDEX_DATE_START = 1;
        final int INDEX_TIME_START = 2;
        final int INDEX_DATE_END = 3;
        final int INDEX_TIME_END = 4;
        final int INDEX_INTERVAL = 5;
        final int INDEX_EPOCH = 6;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;
        final List<String> errors;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect seven parameters
        listParameters = commandType.getParameterList();

        // Prepare to fail
        responseMessage = null;
        errors = new ArrayList<String>(10);

        // Do not change any other DAO data containers!
        dao.setUserObject(null);
        dao.clearEventLogFragment();

        // Check the parameters before continuing
        // Ephemeris.Target
        // Ephemeris.Date.Start
        // Ephemeris.Time.Start
        // Ephemeris.Date.End
        // Ephemeris.Time.End
        // Ephemeris.Time.Interval
        // Ephemeris.Epoch

        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_TARGET) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_TARGET).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_DATE_START) != null)
            && (SchemaDataType.DATE.equals(listParameters.get(INDEX_DATE_START).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TIME_START) != null)
            && (SchemaDataType.TIME.equals(listParameters.get(INDEX_TIME_START).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_DATE_END) != null)
            && (SchemaDataType.DATE.equals(listParameters.get(INDEX_DATE_END).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TIME_END) != null)
            && (SchemaDataType.TIME.equals(listParameters.get(INDEX_TIME_END).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_INTERVAL) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_INTERVAL).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_EPOCH) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_EPOCH).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strTarget;
                final String strDateStart;
                final String strTimeStart;
                final String strDateEnd;
                final String strTimeEnd;
                final String strIntervalSeconds;
                final String strEpoch;

                final EphemerisDAOInterface daoEphemeris;
                final YearMonthDayInterface ymdStart;
                final HourMinSecInterface hmsStart;
                final YearMonthDayInterface ymdEnd;
                final HourMinSecInterface hmsEnd;
                final int intIntervalSeconds;
                final Epoch epoch;

                final List<Metadata> listMetadata;
                final Metadata metadataLongitude;
                final Metadata metadataLatitude;
                final Metadata metadataHASL;
                final Metadata metadataTimeZone;

                double dblJDStart;
                double dblJDEnd;
                double dblLongitudeObservatory;
                double dblLatitudeObservatory;
                double dblHASLObservatory;
                TimeZone timeZoneObservatory;
                AstronomicalCalendarInterface calendarWorkspace;
                Vector<Object> vecEphemeris;

                // Some exit defaults in case of invalid parameters
                dblJDStart = -1.0;
                dblJDEnd = -1.0;
                dblLongitudeObservatory = Double.MAX_VALUE;
                dblLatitudeObservatory = Double.MAX_VALUE;
                dblHASLObservatory = Double.MAX_VALUE;
                timeZoneObservatory = null;
                vecEphemeris = null;

                // Read the User-supplied parameter values
                strTarget = listParameters.get(INDEX_TARGET).getValue();
                strDateStart = listParameters.get(INDEX_DATE_START).getValue();
                strTimeStart = listParameters.get(INDEX_TIME_START).getValue();
                strDateEnd = listParameters.get(INDEX_DATE_END).getValue();
                strTimeEnd = listParameters.get(INDEX_TIME_END).getValue();
                strIntervalSeconds = listParameters.get(INDEX_INTERVAL).getValue();
                strEpoch = listParameters.get(INDEX_EPOCH).getValue();

                // Find the Ephemeris target if possible
                if ((dao.getHostInstrument() != null)
                    && (dao.getHostInstrument().getHostAtom() != null)
                    && (dao.getHostInstrument().getHostAtom() instanceof ObservatoryInterface))
                    {
                    daoEphemeris = EphemeridesHelper.getEphemerisDAOforName(dao.getHostInstrument(), strTarget);
                    }
                else
                    {
                    daoEphemeris = null;
                    }

                // Dates and Times
                ymdStart = (YearMonthDayInterface) DataTypeHelper.parseDataTypeFromValueField(strDateStart,
                                                                                              DataTypeDictionary.DATE_YYYY_MM_DD,
                                                                                              EMPTY_STRING,
                                                                                              EMPTY_STRING,
                                                                                              errors);
                hmsStart = (HourMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strTimeStart,
                                                                                            DataTypeDictionary.TIME_HH_MM_SS,
                                                                                            EMPTY_STRING,
                                                                                            EMPTY_STRING,
                                                                                            errors);
                ymdEnd = (YearMonthDayInterface) DataTypeHelper.parseDataTypeFromValueField(strDateEnd,
                                                                                            DataTypeDictionary.DATE_YYYY_MM_DD,
                                                                                            EMPTY_STRING,
                                                                                            EMPTY_STRING,
                                                                                            errors);
                hmsEnd = (HourMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strTimeEnd,
                                                                                          DataTypeDictionary.TIME_HH_MM_SS,
                                                                                          EMPTY_STRING,
                                                                                          EMPTY_STRING,
                                                                                          errors);
                // The number of seconds between each line of the Ephemeris
                // The very unlikely event of a NumberFormatException is trapped below
                intIntervalSeconds = Integer.parseInt(strIntervalSeconds);

                // Returns NULL if the Epoch is not found in the enum
                epoch = Epoch.getEpochForName(strEpoch);

                // Now try to find the Observatory topocentric information
                listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                             (ObservatoryInterface) dao.getHostInstrument().getHostAtom(),
                                                                             dao.getHostInstrument(),
                                                                             dao, null,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());

                metadataLongitude = MetadataHelper.getMetadataByKey(listMetadata,
                                                                    MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

                metadataLatitude = MetadataHelper.getMetadataByKey(listMetadata,
                                                                   MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());

                metadataHASL = MetadataHelper.getMetadataByKey(listMetadata,
                                                               MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());

                metadataTimeZone = MetadataHelper.getMetadataByKey(listMetadata,
                                                                   MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

                //-----------------------------------------------------------------------------
                // Only proceed if we have a complete valid Observatory location to use for the calculation

                if ((daoEphemeris != null)
                    && (ymdStart != null)
                    && (hmsStart != null)
                    && (ymdEnd != null)
                    && (hmsEnd != null)
                    && (epoch != null)
                    && (metadataLongitude != null)
                    && (metadataLatitude != null)
                    && (metadataHASL != null)
                    && (metadataTimeZone != null)
                    && (errors.size() == 0))
                    {
                    final DegMinSecInterface dmsLongitudeObservatory;
                    final DegMinSecInterface dmsLatitudeObservatory;

                    // Longitude -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
                    dmsLongitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                             DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                             EMPTY_STRING,
                                                                                                             EMPTY_STRING,
                                                                                                             errors);
                    // Latitude  -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999  deg:mm:ss
                    dmsLatitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                                            DataTypeDictionary.LATITUDE,
                                                                                                            EMPTY_STRING,
                                                                                                            EMPTY_STRING,
                                                                                                            errors);
                    if ((dmsLongitudeObservatory != null)
                        && (dmsLatitudeObservatory != null)
                        && (errors.size() == 0))
                        {
                        // These must be correct to have passed the Metadata parsing above
                        dblLongitudeObservatory = dmsLongitudeObservatory.toDouble();
                        dblLatitudeObservatory = dmsLatitudeObservatory.toDouble();

                        // The very unlikely event of a NumberFormatException is trapped below
                        dblHASLObservatory = Double.parseDouble(metadataHASL.getValue());

                        // This returns the GMT zone if the given ID cannot be understood
                        timeZoneObservatory = TimeZone.getTimeZone(metadataTimeZone.getValue());

                        // The Julian Date of the start point
                        if ((ymdStart != null)
                            && (hmsStart != null))
                            {
                            calendarWorkspace = new AstronomicalCalendar(ymdStart, hmsStart, timeZoneObservatory, dblLongitudeObservatory);
                            dblJDStart = calendarWorkspace.getJD();
                            }

                        // The Julian Date of the end point
                        if ((ymdEnd != null)
                            && (hmsEnd != null))
                            {
                            calendarWorkspace = new AstronomicalCalendar(ymdEnd, hmsEnd, timeZoneObservatory, dblLongitudeObservatory);
                            dblJDEnd = calendarWorkspace.getJD();
                            }

                        if (LOADER_PROPERTIES.isMetadataDebug())
                            {
                            LOGGER.logTimedEvent("--------------------------------------------------------------");
                            LOGGER.logTimedEvent(SOURCE + "Gathered Parameters");
                            LOGGER.logTimedEvent("Target=" + daoEphemeris.getEphemeris().getName());
                            LOGGER.logTimedEvent("Start Date=" + ymdStart.toString());
                            LOGGER.logTimedEvent("Start Time=" + hmsStart.toString_HH_MM_SS());
                            LOGGER.logTimedEvent("End Date=" + ymdEnd.toString());
                            LOGGER.logTimedEvent("End Time=" + hmsEnd.toString_HH_MM_SS());
                            LOGGER.logTimedEvent("JD Start=" + dblJDStart);
                            LOGGER.logTimedEvent("JD End=" + dblJDEnd);
                            LOGGER.logTimedEvent("Interval=" + intIntervalSeconds);
                            LOGGER.logTimedEvent("Epoch=" + epoch.getName());
                            LOGGER.logTimedEvent("Observatory Longitude=" + metadataLongitude.getValue() + " dms=" + dmsLongitudeObservatory.toString() + " dbl=" + dblLongitudeObservatory);
                            LOGGER.logTimedEvent("Observatory Latitude=" + metadataLatitude.getValue() + " dms=" + dmsLatitudeObservatory.toString()  + " dbl=" + dblLatitudeObservatory);
                            LOGGER.logTimedEvent("Observatory HASL=" + metadataHASL.getValue() + " dbl=" + dblHASLObservatory);
                            LOGGER.logTimedEvent("Observatory TimeZone=" + metadataTimeZone.getValue() + " tz=" + timeZoneObservatory.getDisplayName());
                            }

                        boolSuccess = true;
                        }
                    else
                        {
                        commandType.getResponse().setValue("Invalid Observatory Location in metadata");
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + commandType.getResponse().getValue()
                                       + TERMINATOR);
                        boolSuccess = false;
                        }
                    }
                else
                    {
                    // ToDo Explain detailed reason for failure
                    commandType.getResponse().setValue("Invalid Observatory Location in metadata");
                    errors.add(METADATA_TARGET_COMMAND
                                   + METADATA_ACTION_EXECUTE
                                   + METADATA_RESULT
                                   + commandType.getResponse().getValue()
                                   + TERMINATOR);
                    boolSuccess = false;
                    }

                //---------------------------------------------------------------------------------
                // Generate the Ephemeris only if all User-entered parameters are correct

                if ((boolSuccess)
                    && (daoEphemeris != null)
                    && (dblJDStart > 0.0)
                    && (dblJDEnd > 0.0)
                    && (intIntervalSeconds > 0)
                    && (dblJDEnd >= (dblJDStart + intIntervalSeconds/AstronomyConstants.SECONDS_PER_DAY))
                    && (isValidIntervalCount(dblJDStart, dblJDEnd, intIntervalSeconds))
                    && (dblLongitudeObservatory != Double.MAX_VALUE)
                    && (dblLatitudeObservatory != Double.MAX_VALUE)
                    && (dblHASLObservatory != Double.MAX_VALUE)
                    && (timeZoneObservatory != null)
                    && (errors.size() == 0))
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isMetadataDebug(),
                                           SOURCE + "--> EphemeridesHelper.generateEphemerisData()");

                    // If we get here, we have all we need...
                    // MetadataHelper.addParameterValuesToMetadata()
                    // will have added the latest Parameter values to the InstrumentMetadata in the DAO Wrapper
                    vecEphemeris = EphemeridesHelper.generateEphemerisData(daoEphemeris,
                                                                           dblJDStart,
                                                                           dblJDEnd,
                                                                           intIntervalSeconds,
                                                                           epoch,
                                                                           dblLongitudeObservatory,
                                                                           dblLatitudeObservatory,
                                                                           timeZoneObservatory);
                    // If we get here without Exception, the job is done!
                    boolSuccess = ((vecEphemeris != null) && (vecEphemeris.size() > 0));
                    }
                else
                    {
                    // Explain detailed reason for failure
                    if (daoEphemeris == null)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Unable to locate Ephemeris DAO"
                                       + TERMINATOR);
                        }

                    if (dblJDStart <= 0.0)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Start Julian Date is invalid"
                                       + TERMINATOR);
                        }

                    if (dblJDEnd <= 0.0)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "End Julian Date is invalid"
                                       + TERMINATOR);
                        }

                    if ((dblJDStart > 0.0)
                        && (dblJDEnd > 0.0)
                        && (dblJDEnd < (dblJDStart + intIntervalSeconds/AstronomyConstants.SECONDS_PER_DAY)))
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Insufficient date range for at least one iteration"
                                       + TERMINATOR);
                        }

                    if (dblLongitudeObservatory == Double.MAX_VALUE)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Unable to find the Observatory Longitude"
                                       + TERMINATOR);
                        }

                    if (dblLatitudeObservatory == Double.MAX_VALUE)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Unable to find the Observatory Latitude"
                                       + TERMINATOR);
                        }

                    if (dblHASLObservatory == Double.MAX_VALUE)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Unable to find the Observatory HASL"
                                       + TERMINATOR);
                        }

                    if (timeZoneObservatory == null)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Unable to find the Observatory TimeZone"
                                       + TERMINATOR);
                        }

                    if (intIntervalSeconds <= 0)
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "IntervalSeconds is invalid"
                                       + TERMINATOR);
                        }

                    if (!isValidIntervalCount(dblJDStart, dblJDEnd, intIntervalSeconds))
                        {
                        errors.add(METADATA_TARGET_COMMAND
                                       + METADATA_ACTION_EXECUTE
                                       + METADATA_RESULT
                                       + "Parameters would require too many iterations "
                                       + "] [requested=" + AstroMath.truncate((dblJDEnd - dblJDStart) * AstronomyConstants.SECONDS_PER_DAY / intIntervalSeconds)
                                       + "] [limit=" + MAX_INTERVALS
                                       + TERMINATOR);
                        }

                    commandType.getResponse().setValue(ResponseMessageStatus.INVALID_PARAMETER.getResponseValue());
                    boolSuccess = false;
                    }

                // Create the OK ResponseMessage if all went well
                if (boolSuccess)
                    {
                    // Pass the Ephemeris back to the DAO
                    // This will then be passed to the InstrumentPanel via the AbstractObservatoryInstrument.setWrappedData()
                    dao.setUserObject(vecEphemeris);

                    // The Command has produced the following Metadata:
                    //      Instrument.Ephemerides.Ephemeris.Target
                    //      Instrument.Ephemerides.Ephemeris.Date.Start
                    //      Instrument.Ephemerides.Ephemeris.Time.Start
                    //      Instrument.Ephemerides.Ephemeris.Date.End
                    //      Instrument.Ephemerides.Ephemeris.Time.End
                    //      Instrument.Ephemerides.Ephemeris.Time.Interval
                    //      Instrument.Ephemerides.Ephemeris.Epoch
                    // and has used:
                    //      Observatory.Longitude
                    //      Observatory.Latitude
                    //      Observatory.HASL
                    //      Observatory.TimeZone
                    // all of which should be present in the Metadata in the Wrapper

                    MetadataHelper.showMetadataList(dao.getInstrumentMetadata(),
                                                    SOURCE + "DAO InstrumentMetadata after execution of Command, before setWrappedData()",
                                                    LOADER_PROPERTIES.isMetadataDebug());

                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                }

            catch (NumberFormatException exception)
                {
                commandType.getResponse().setValue(ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT);
                errors.add(METADATA_TARGET_COMMAND
                               + METADATA_ACTION_EXECUTE
                               + METADATA_RESULT
                               + commandType.getResponse().getValue()
                               + TERMINATOR);
                boolSuccess = false;
                }
            }
        else
            {
            commandType.getResponse().setValue(ResponseMessageStatus.INVALID_XML.getResponseValue());
            errors.add(METADATA_TARGET_COMMAND
                           + METADATA_ACTION_EXECUTE
                           + METADATA_RESULT
                           + commandType.getResponse().getValue()
                           + TERMINATOR);
            boolSuccess = false;
            }

        if (!boolSuccess)
            {
            // This takes account of isInstrumentDataConsumer()
            dao.clearData();

            SimpleEventLogUIComponent.logErrors(dao.getEventLogFragment(),
                                                EventStatus.FATAL,
                                                errors,
                                                SOURCE,
                                                dao.getObservatoryClock());
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        // The CommandType ResponseValue carries some status information
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
