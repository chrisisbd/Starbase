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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.metadata;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.WMMMetadataFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.Calendar;
import java.util.List;


/***************************************************************************************************
 * AddWMMMetadata
 */

public final class AddWMMMetadata implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             ObservatoryConstants
    {
    /***********************************************************************************************
     * doAddToposWMMMetadata().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doAddToposWMMMetadata(final ObservatoryInstrumentDAOInterface dao,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "AddWMMMetadata.doAddToposWMMMetadata() ";
        final CommandType commandType;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();
        responseMessage = null;

        if ((REGISTRY.getFramework() != null)
            && (REGISTRY.getFramework().getLatitude() != null)
            && (REGISTRY.getFramework().getLongitude() != null)
            && (dao.getObservatoryClock() != null)
            && (dao.getObservatoryClock().getCalendarDateNow() != null)
            && (dao.getEventLogFragment() != null))
            {
            final double dblLatitude;
            final double dblLongitude;
            final double dblHASL_metres;
            final double dblYear;
            final List<Metadata> listMetadata;

            // Calculate the WMM values
            dblLatitude = REGISTRY.getFramework().getLatitude().toDouble();
            dblLongitude = REGISTRY.getFramework().getLongitude().toDouble();
            dblHASL_metres = REGISTRY.getFramework().getHASL();
            dblYear = dao.getObservatoryClock().getCalendarDateNow().get(Calendar.YEAR);

            listMetadata = WMMMetadataFactory.createWMMObservationMetadata(dblLatitude,
                                                                           dblLongitude,
                                                                           dblYear,
                                                                           dblHASL_metres);

            // All of the Metadata are for the Observation, so end up in the DAO ObservationMetadata
            dao.addAllMetadataToContainersTraced(listMetadata,
                                                 SOURCE,
                                                 LOADER_PROPERTIES.isMetadataDebug());

            REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
            InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                    + "observation" + TERMINATOR
                                                    + METADATA_ACTION_ADD_METADATA,
                                               SOURCE,
                                               dao.getObservatoryClock());

            // Create the ResponseMessage
            commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                commandType);
            }
        else
            {
            commandType.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                  commandmessage,
                                                                                  commandType,
                                                                                  responseMessage);
            }

        return (responseMessage);
        }
    }
