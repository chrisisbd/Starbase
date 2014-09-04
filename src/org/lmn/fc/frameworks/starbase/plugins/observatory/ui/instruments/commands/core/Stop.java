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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.core;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.io.IOException;


/***************************************************************************************************
 * Stop.
 */

public final class Stop implements FrameworkConstants,
                                   FrameworkStrings,
                                   FrameworkMetadata,
                                   FrameworkSingletons,
                                   ObservatoryConstants
    {
    /***********************************************************************************************
     * stop().
     * See the code in ObservatoryInstrumentHelper.createButtonPanel().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doStop(final ObservatoryInstrumentDAOInterface dao,
                                                  final CommandMessageInterface commandmessage)
        {
        final CommandType cmdStop;
        final ObservatoryInstrumentInterface instrumentStop;
        final DaoPortInterface portStop;
        final ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AbstractObservatoryInstrumentDAO.stop() LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdStop = (CommandType)commandmessage.getCommandType().copy();

        instrumentStop = dao.getHostInstrument();
        portStop = dao.getPort();
        boolSuccess = false;

        //------------------------------------------------------------------------------------------
        // Just in case there's a UI, set up the ControlPanel buttons correctly
        // Otherwise don't change any Instrument selection etc.
        // See the code in ObservatoryInstrumentHelper.createButtonPanel()

        if (instrumentStop.getOnButton() != null)
            {
            instrumentStop.getOnButton().setBackground(instrumentStop.getOnButton().getBackground().darker());
            instrumentStop.getOnButton().setEnabled(true);
            instrumentStop.getOnButton().setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_START);
            }

        if (instrumentStop.getOffButton() != null)
            {
            instrumentStop.getOffButton().setBackground(instrumentStop.getOffButton().getBackground().brighter());
            instrumentStop.getOffButton().setEnabled(false);
            instrumentStop.getOffButton().setToolTipText(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------------------
        // Stop the Instrument

        if (InstrumentState.isDoingSomething(instrumentStop))
            {
            try
                {
                // Close the Port and Streams if we can
                boolSuccess = ObservatoryInstrumentHelper.stopInstrument(instrumentStop, dao, portStop);

                // Don't dispose of the DAO, since this is a programmatic stop()
                // It will also keep the EventLog etc.
                }

            catch (IOException exception)
                {
                LOGGER.error("AbstractObservatoryInstrument.stop() [exception=" + exception.getMessage() + "]");
                }
            }

        //------------------------------------------------------------------------------------------
        // Create the ResponseMessage

        if (boolSuccess)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                       + instrumentStop.getInstrument().getName()
                                                       + TERMINATOR
                                               + METADATA_ACTION_STOP
                                               + METADATA_ORIGIN_MACRO,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());

            instrumentStop.notifyInstrumentStateChangedEvent(dao,
                                                             instrumentStop,
                                                             instrumentStop.getInstrumentState(),
                                                             InstrumentState.STOPPED,
                                                             0,
                                                             UNEXPECTED);
            // Set the ResponseValue if required
            if (cmdStop.getResponse() != null)
                {
                cmdStop.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                }

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                cmdStop);
            }
        else
            {
            // We are unable to stop the Instrument
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdStop,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdStop));
            }

        return (responseMessage);
        }
    }
