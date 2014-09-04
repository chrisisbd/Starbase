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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.PointOfInterestType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.PointOfInterestHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.io.IOException;
import java.util.List;


/***************************************************************************************************
 * ImportPointsOfInterest
 */

public final class ImportPointsOfInterest implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkSingletons,
                                                     ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportPointsOfInterest().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportPointsOfInterest(final ObservatoryInstrumentDAOInterface dao,
                                                                    final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportPointsOfInterest.doImportPointsOfInterest() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_POITYPE = 0;
        final CommandType cmdImportPOI;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdImportPOI = (CommandType)commandmessage.getCommandType().copy();
        responseMessage = null;
        boolSuccess = false;

        // We expect one parameter, the PointOfInterestType
        listParameters = cmdImportPOI.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_POITYPE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_POITYPE).getInputDataType().getDataTypeName()))
            && (dao.getObservatoryClock() != null)
            && (dao.getObservatoryClock().getCalendarDateNow() != null)
            && (dao.getEventLogFragment() != null))
            {
            try
                {
                final String strPOIType;
                final PointOfInterestType poiType;

                // Find out which PointOfInterestType is required
                strPOIType = listParameters.get(INDEX_POITYPE).getValue();

                // Map the parameter entry to a PointOfInterestType
                poiType = PointOfInterestType.getPointOfInterestTypeForName(strPOIType);

                if (poiType != null)
                    {
                    switch (poiType)
                        {
                        case FRAMEWORK:
                            {
                            boolSuccess = PointOfInterestHelper.importFrameworkPOIandLOI(poiType, REGISTRY.getFramework(), dao);
                            DAOHelper.setResponseValue(cmdImportPOI, boolSuccess);
                            break;
                            }

                        case OBSERVATORY:
                            {
                            boolSuccess = PointOfInterestHelper.importObservatoryPOIandLOI(poiType, REGISTRY.getFramework(), dao);
                            DAOHelper.setResponseValue(cmdImportPOI, boolSuccess);
                            break;
                            }

                        case INSTRUMENT:
                            {
                            boolSuccess = PointOfInterestHelper.importInstrumentPOIandLOI(poiType, REGISTRY.getFramework(), dao);
                            DAOHelper.setResponseValue(cmdImportPOI, boolSuccess);
                            break;
                            }

                        default:
                            {
                            // An unrecognised PointOfInterestType
                            throw new XmlException(SOURCE + EXCEPTION_UNRECOGNISED_POITYPE);
                            }
                        }
                    }
                }

            catch (XmlException exception)
                {
                LOGGER.error(SOURCE + "XmlException = " + exception.getMessage());
                cmdImportPOI.getResponse().setValue(ResponseMessageStatus.INVALID_XML.getResponseValue());
                boolSuccess = false;
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + "IOException = " + exception.getMessage());
                cmdImportPOI.getResponse().setValue(ResponseMessageStatus.RESPONSE_NODATA);
                boolSuccess = false;
                }
            }

        // Create the ResponseMessage
        if (boolSuccess)
            {
            REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
            InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                cmdImportPOI);
            }
        else
            {
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                  commandmessage,
                                                                                  cmdImportPOI,
                                                                                  responseMessage);
            }

        return (responseMessage);
        }
    }