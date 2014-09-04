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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import javax.swing.*;


/***************************************************************************************************
 * AwaitingDevelopment.
 */

public final class AwaitingDevelopment implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ObservatoryConstants
    {
    /***********************************************************************************************
     * Show an Awaiting Development message dialog.
     *
     * @param message
     */

    public static void showAwaitingDevelopment(final String message)
        {
        JOptionPane.showMessageDialog(null,
                                      message,
                                      AWAITING_DEVELOPMENT,
                                      JOptionPane.WARNING_MESSAGE);

        }


    /***********************************************************************************************
     * Inform the User that something is Awaiting Development in the DataTranslator.
     *
     * @param translator
     * @param action
     * @param name
     * @param context
     */

    public static void informTranslatorUser(final DataTranslatorInterface translator,
                                            final String action,
                                            final String name,
                                            final String context)
        {
        if (translator != null)
            {
            translator.addMessage(METADATA_TARGET_TRANSLATOR
                                       + action + SPACE
                                       + METADATA_NAME + name + TERMINATOR_SPACE
                                       + METADATA_CONTEXT + context + TERMINATOR_SPACE
                                       + METADATA_STATUS + AWAITING_DEVELOPMENT + TERMINATOR);
            }

        java.awt.Toolkit.getDefaultToolkit().beep();
        }


    /***********************************************************************************************
     * doAwaitingDevelopment().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doAwaitingDevelopment(final ObservatoryInstrumentDAOInterface dao,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "AwaitingDevelopment.doAwaitingDevelopment() ";
        final CommandType commandType;
        final ResponseMessageInterface responseMessage;
        final String [] strMessage =
            {
            MSG_AWAITING_DEVELOPMENT
            };


        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        //toFile(outputfile, toText(separator));

        // Let the user know where it went
        JOptionPane.showMessageDialog(null,
                                      strMessage,
                                      AWAITING_DEVELOPMENT,
                                      JOptionPane.WARNING_MESSAGE);

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        if (dao != null)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_COMMAND
                                                   + METADATA_ACTION_EXECUTE
                                                   + METADATA_STATUS
                                                       + AWAITING_DEVELOPMENT
                                                       + TERMINATOR_SPACE
                                                   + METADATA_STARSCRIPT
                                                       + commandmessage.getStarScript()
                                                       + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            }

        // Create the ResponseMessage
        // The doAwaitingDevelopment() operation normally just requires an Ack, i.e. no Response
//        dao.setRawDataChannelCount(0);
//        dao.setTemperatureChannel(false);
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }
    }
