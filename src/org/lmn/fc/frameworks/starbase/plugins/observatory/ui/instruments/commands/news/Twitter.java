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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.news;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import winterwell.jtwitter.TwitterException;

import java.util.List;


/***************************************************************************************************
 * Twitter.
 *
 * For Twitter see
 * http://www.winterwell.com/software/jtwitter.php
 * http://www.winterwell.com/software/jtwitter/javadoc/
 */

public final class Twitter implements FrameworkConstants,
                                      FrameworkStrings,
                                      FrameworkMetadata,
                                      FrameworkSingletons,
                                      ObservatoryConstants
    {
    private static final String RESPONSE_TWITTER_STATUS = "Twitter.Status";


    /***********************************************************************************************
     * doGetTwitterStatus().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetTwitterStatus(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "Twitter.doGetTwitterStatus()";
        final int PARAMETER_COUNT = 1;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "Twitter.doGetTwitterStatus() LOCAL COMMAND");

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect one Parameter, the Twitter username
        listParameters = commandType.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (commandType.getResponse() != null)
            && (RESPONSE_TWITTER_STATUS.equals(commandType.getResponse().getName())))
            {
            try
                {
                final winterwell.jtwitter.Twitter twitter;
                final String strUsername;
                String strStatus;

                strUsername = listParameters.get(0).getValue();
                strStatus = EMPTY_STRING;

                if ((strUsername != null)
                    && (!EMPTY_STRING.equals(strUsername)))
                    {
                    // Log into Twitter as ukraastarbase
                    twitter = new winterwell.jtwitter.Twitter(UKRAA_USERNAME, UKRAA_PASSWORD);

                    strStatus = twitter.getStatus(strUsername).toString();
                    commandType.getResponse().setValue("[username=" + strUsername + "] [status=" + strStatus + "]");
                    }
                else
                    {
                    commandType.getResponse().setValue("[error] [username=" + strUsername + "] [status=" + strStatus + "]");
                    }
                }

            catch (TwitterException exception)
                {
                // TwitterException can probably be ignored except for invalid accounts
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   "[target=twitter] [action=getstatus] [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                commandType.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
                }
            }

//        dao.setRawDataChannelCount(0);
//        dao.setTemperatureChannel(false);

        // Help the GC?
        dao.setTranslator(null);
        dao.setFilter(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }


    /***********************************************************************************************
     * doSetTwitterStatus().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetTwitterStatus(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "Twitter.doSetTwitterStatus()";
        final int PARAMETER_COUNT = 3;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "Twitter.doSetTwitterStatus() LOCAL COMMAND");

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect three Parameters, the Twitter username, password and Status
        listParameters = commandType.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (listParameters.get(1) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(1).getInputDataType().getDataTypeName()))
            && (listParameters.get(2) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(2).getInputDataType().getDataTypeName())))
            {
            try
                {
                final winterwell.jtwitter.Twitter twitter;
                final String strUsername;
                final String strPassword;
                final String strStatus;

                strUsername = listParameters.get(0).getValue();
                strPassword = listParameters.get(1).getValue();
                strStatus = listParameters.get(2).getValue();

                if ((strUsername != null)
                    && (!EMPTY_STRING.equals(strUsername))
                    && (strPassword != null)
                    && (!EMPTY_STRING.equals(strPassword))
                    && (strStatus != null)
                    && (!EMPTY_STRING.equals(strStatus)))
                    {
                    // Log into Twitter using the credentials provided
                    twitter = new winterwell.jtwitter.Twitter(strUsername, strPassword);

                    twitter.setStatus(strStatus);

                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       "[target=twitter] [action=setstatus] [username="
                                                            + strUsername + "] [status="
                                                            + strStatus + "]",
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    // The doSetTwitterStatus() operation requires 'Ok'
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       "[target=twitter] [action=setstatus] [result=not set]",
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    commandType.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
                    }
                }

            catch (TwitterException exception)
                {
                // TwitterException can probably be ignored except for invalid accounts
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   "[target=twitter] [action=setstatus] [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                commandType.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
                }
            }

        // Help the GC?
        dao.setTranslator(null);
        dao.setFilter(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }
    }
