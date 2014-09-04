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

// http://www.jupiterspacestation.org/software/Vlsr.html
// http://fuse.pha.jhu.edu/support/tools/vlsr.html

// http://www.haystack.mit.edu/edu/undergrad/srt/SRT%20Software/SRTfiles.html
// http://www.atnf.csiro.au/people/Tobias.Westmeier/tools_hihelpers.php#restframes
// http://star-www.rl.ac.uk/star/docs/sun67.htx/node230.html
// http://code.google.com/p/distance-omnibus/source/browse/trunk/utils/vphys2vlsr.pro?spec=svn119&r=119
// http://www.gb.nrao.edu/~rmaddale/140ft/h316.html


package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.metadata;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.common.exceptions.HourMinSecException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.ReferenceFrame;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.VLSRMetadataFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.datatypes.types.DeclinationDataType;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;

// B1950.0 = JDE 2433282.4235 = 1950 January 0.9235 TT

// The currently-used standard epoch "J2000", defined by international agreement,
// is that of 2000 January 1.5 (or January 1 at 12h on a defined time scale usually TT)
// and is precisely defined to be
//
//    The Julian date 2451545.0 TT (Terrestrial Time), or January 1, 2000, noon TT.
//    This is equivalent to January 1, 2000, 11:59:27.816 TAI (International Atomic Time) or
//    January 1, 2000, 11:58:55.816 UTC (Coordinated Universal Time).


/***************************************************************************************************
 * AddVLSRMetadata.
 *
 * See:
 *  http://www.jupiterspacestation.org/software/Vlsr.html
 *  http://www.astro.virginia.edu/~emm8x/
 *  http://www.astro.virginia.edu/~emm8x/utils/vlsr.html
 */

public final class AddVLSRMetadata implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * Calculate the Vlsr.
     *
     * @param clock
     * @param rightascension
     * @param declination
     * @param epoch
     * @param frame
     * @param latitude
     * @param longitude
     * @param hasl
     *
     * @return double
     */

    private static double calculateVlsr(final ObservatoryClockInterface clock,
                                        final HourMinSecInterface rightascension,
                                        final DegMinSecInterface declination,
                                        final Epoch epoch,
                                        final ReferenceFrame frame,
                                        final DegMinSecInterface latitude,
                                        final DegMinSecInterface longitude,
                                        final double hasl)
        {
        final double dblVlsr;

        System.out.println("AddVLSRMetadata.calculateVlsr() ");
        //dblVlsr = Vlsr.dop_ed();
        dblVlsr = 0.0;

        return (dblVlsr);
        }


    /***********************************************************************************************
     * Calculate the Velocity of the Local Standard of Rest.
     *
     * @param dao
     * @param commandmessage
     * @param source
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doAddVlsrMetadata(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage,
                                                             final String source)
        {
        final String SOURCE = "AddVLSRMetadata.doAddVlsrMetadata() ";
        final int PARAMETER_COUNT = 4;
        final int INDEX_RA = 0;
        final int INDEX_DEC = 1;
        final int INDEX_EPOCH = 2;
        final int INDEX_FRAME = 3;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;
        final List<String> errors;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               source + "LOCAL COMMAND");

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType) commandmessage.getCommandType().copy();

        // Expect FOUR parameters, RA, Dec, Epoch and ReferenceFrame
        listParameters = commandType.getParameterList();

        // Prepare for the worst
        commandType.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
        responseMessage = null;
        errors = new ArrayList<String>(10);

        // Do not change any DAO data containers!

        // Check the Parameters and Response before continuing
        // The Name of the Response doesn't matter, but it must expect a DecimalDouble of km s-1
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getObservatoryClock() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_RA) != null)
            && (SchemaDataType.RIGHT_ASCENSION.equals(listParameters.get(INDEX_RA).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_DEC) != null)
            && (SchemaDataType.DECLINATION.equals(listParameters.get(INDEX_DEC).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_EPOCH) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_EPOCH).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_FRAME) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_FRAME).getInputDataType().getDataTypeName()))
            && (SchemaDataType.DECIMAL_DOUBLE.equals(commandType.getResponse().getDataTypeName()))
            && (SchemaUnits.KM_S_1.equals(commandType.getResponse().getUnits())))
            {
            try
                {
                final String strRA;
                final String strDec;
                final String strEpoch;
                final String strReferenceFrame;

                final HourMinSecInterface hmsRA;
                final DegMinSecInterface dmsDec;
                final Epoch epoch;
                final ReferenceFrame frame;

                final List<Metadata> listMetadata;
                final Metadata metadataLongitude;
                final Metadata metadataLatitude;
                final Metadata metadataHASL;

                List<Metadata> listVlsrMetadata;
                double dblVlsr;

                // Expect to fail
                listVlsrMetadata = null;
                dblVlsr = 0.0;

                // Read the User-supplied parameter values
                strRA = listParameters.get(INDEX_RA).getValue();
                strDec = listParameters.get(INDEX_DEC).getValue();
                strEpoch = listParameters.get(INDEX_EPOCH).getValue();
                strReferenceFrame = listParameters.get(INDEX_FRAME).getValue();

                hmsRA = (HourMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strRA,
                                                                                         DataTypeDictionary.RIGHT_ASCENSION,
                                                                                         EMPTY_STRING,
                                                                                         EMPTY_STRING,
                                                                                         errors);
                hmsRA.enableFormatSign(false);

                dmsDec = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strDec,
                                                                                         DataTypeDictionary.DECLINATION,
                                                                                         EMPTY_STRING,
                                                                                         EMPTY_STRING,
                                                                                         errors);
                dmsDec.setDisplayFormat(DeclinationDataType.DEFAULT_UNIT_DECLINATION);
                dmsDec.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_DECLINATION);

                epoch = Epoch.getEpochForName(strEpoch);

                frame = ReferenceFrame.getReferenceFrameForName(strReferenceFrame);

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

               //---------------------------------------------------------------------------------
                // Calculate the VLSR only if all User-entered parameters are correct
                // Only proceed if we have a complete valid Observatory location to use for the calculation

                if ((hmsRA != null)
                    && (dmsDec != null)
                    && (epoch != null)
                    && (frame != null)
                    && (metadataLongitude != null)
                    && (metadataLatitude != null)
                    && (metadataHASL != null)
                    && (errors.size() == 0))
                    {
                    final DegMinSecInterface dmsLongitudeObservatory;
                    final DegMinSecInterface dmsLatitudeObservatory;
                    final double dblHASLObservatory;

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
                    // The very unlikely event of a NumberFormatException is trapped below
                    dblHASLObservatory = Double.parseDouble(metadataHASL.getValue());

                    if ((dmsLongitudeObservatory != null)
                        && (dmsLatitudeObservatory != null)
                        && (errors.size() == 0))
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET
                                                               + "vlsr"
                                                               + TERMINATOR
                                                               + METADATA_ACTION_ADD_METADATA
                                                               + "[clock=" + dao.getHostInstrument().getObservatoryClock().getDateTimeNowAsString()
                                                               + "] [ra=" + strRA
                                                               + "] [dec=" + strDec
                                                               + "] [epoch=" + strEpoch
                                                               + "] [frame=" + strReferenceFrame
                                                               + "] [latitude=" + dmsLatitudeObservatory.toString()
                                                               + "] [longitude=" + dmsLongitudeObservatory.toString()
                                                               + "] [hasl=" + dblHASLObservatory + "]",
                                                           source,
                                                           dao.getObservatoryClock());
                        // Calculate the Vlsr
                        dblVlsr = calculateVlsr(dao.getHostInstrument().getObservatoryClock(),
                                                hmsRA,
                                                dmsDec,
                                                epoch,
                                                frame,
                                                dmsLatitudeObservatory,
                                                dmsLongitudeObservatory,
                                                dblHASLObservatory);

                        listVlsrMetadata = VLSRMetadataFactory.createVLSRObservationMetadata(hmsRA,
                                                                                             dmsDec,
                                                                                             epoch,
                                                                                             frame,
                                                                                             dblVlsr);

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

                // Create the ResponseMessage if all went well
                if (boolSuccess)
                    {
                    // All of the Metadata are for the Observation, so end up in the DAO ObservationMetadata
                    dao.addAllMetadataToContainersTraced(listVlsrMetadata,
                                                         SOURCE,
                                                         LOADER_PROPERTIES.isMetadataDebug());

                    REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                    InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                    commandType.getResponse().setValue(DecimalFormatPattern.VLSR.format(dblVlsr));
                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                }

            // These Exceptions should never occur, because of the Regex constraints in the Instrument XML
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

            catch (IllegalArgumentException exception)
                {
                commandType.getResponse().setValue(ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT);
                errors.add(METADATA_TARGET_COMMAND
                               + METADATA_ACTION_EXECUTE
                               + METADATA_RESULT
                               + commandType.getResponse().getValue()
                               + TERMINATOR);
                boolSuccess = false;
                }

            catch (HourMinSecException exception)
                {
                commandType.getResponse().setValue(ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT);
                errors.add(METADATA_TARGET_COMMAND
                               + METADATA_ACTION_EXECUTE
                               + METADATA_RESULT
                               + commandType.getResponse().getValue()
                               + TERMINATOR);
                boolSuccess = false;
                }

            catch (DegMinSecException exception)
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
