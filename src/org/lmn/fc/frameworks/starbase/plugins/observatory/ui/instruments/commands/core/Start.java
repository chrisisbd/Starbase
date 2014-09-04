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

import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * Start.
 */

public final class Start
    {
    /***********************************************************************************************
     * start().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doStart(final ObservatoryInstrumentDAOInterface dao,
                                                   final CommandMessageInterface commandmessage)
        {
        final CommandType cmdStart;
        final ObservatoryInstrumentInterface instrumentStart;
        final ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        FrameworkSingletons.LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                                   "AbstractObservatoryInstrumentDAO.start() LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdStart = (CommandType)commandmessage.getCommandType().copy();

        instrumentStart = dao.getHostInstrument();
        boolSuccess = false;

        //------------------------------------------------------------------------------------------
        // Just in case there's a UI, set up the ControlPanel buttons correctly
        // Otherwise don't change any Instrument selection etc.
        // See the code in ObservatoryInstrumentHelper.createButtonPanel()

        if (instrumentStart.getOnButton() != null)
            {
            instrumentStart.getOnButton().setBackground(instrumentStart.getOnButton().getBackground().brighter());
            instrumentStart.getOnButton().setEnabled(false);
            instrumentStart.getOnButton().setToolTipText(FrameworkStrings.EMPTY_STRING);
            }

        if (instrumentStart.getOffButton() != null)
            {
            instrumentStart.getOffButton().setBackground(instrumentStart.getOffButton().getBackground().darker());
            instrumentStart.getOffButton().setEnabled(true);
            instrumentStart.getOffButton().setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_STOP);
            }

        //------------------------------------------------------------------------------------------
        // Start the Instrument

        if ((InstrumentState.INITIALISED.equals(instrumentStart.getInstrumentState()))
            || (InstrumentState.STOPPED.equals(instrumentStart.getInstrumentState())))
            {
            boolSuccess = ObservatoryInstrumentHelper.startInstrument(instrumentStart);
            }

        //------------------------------------------------------------------------------------------
        // Create the ResponseMessage

        if (boolSuccess)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               FrameworkMetadata.METADATA_TARGET
                                                       + instrumentStart.getInstrument().getName()
                                                           + FrameworkMetadata.TERMINATOR
                                                       + FrameworkMetadata.METADATA_ACTION_START
                                                       + FrameworkMetadata.METADATA_ORIGIN_MACRO,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());

            instrumentStart.notifyInstrumentStateChangedEvent(instrumentStart,
                                                              instrumentStart,
                                                              instrumentStart.getInstrumentState(),
                                                              InstrumentState.READY,
                                                              0,
                                                              FrameworkStrings.UNEXPECTED);
            // Set the ResponseValue if required
            if (cmdStart.getResponse() != null)
                {
                cmdStart.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                }

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                cmdStart);
            }
        else
            {
            // We are unable to start the Instrument
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_COMMAND);
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdStart,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdStart));
            }

        return (responseMessage);
        }
    }
