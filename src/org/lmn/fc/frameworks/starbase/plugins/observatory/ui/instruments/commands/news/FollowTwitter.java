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
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.subscriptions.SubscriptionsDocument;
import org.lmn.fc.model.xmlbeans.subscriptions.TwitterSubscription;

import java.util.List;


/***************************************************************************************************
 * FollowTwitter.
 */

public final class FollowTwitter implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ObservatoryConstants
    {
    // String Resources
    private static final String NEWSFEED_TYPE = "twitter";


    /***********************************************************************************************
     * doFollowTwitter().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doFollowTwitter(final CommunicatorDAOInterface dao,
                                                           final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "FollowTwitter.doFollowTwitter() ";
        final int PARAMETER_COUNT = 1;
        final CommandType cmdFollow;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdFollow = (CommandType)commandmessage.getCommandType().copy();

        // Prepare for the worst
        cmdFollow.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
        responseMessage = null;
        boolSuccess = false;

        // We expect one parameter, the username
        listParameters = cmdFollow.getParameterList();

        // Do not change any DAO data containers!
        dao.clearEventLogFragment();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (dao.getSubscriptionsDoc() != null)
            && (dao.getSubscriptionsDoc().getSubscriptions() != null)
            && (dao.getObservatoryClock() != null)
            && (dao.getEventLogFragment() != null))
            {
            final String strUsername;
            final SubscriptionsDocument docSubscriptions;
            final List<TwitterSubscription> listTwitterSubscription;
            boolean boolInUse;

            strUsername = listParameters.get(0).getValue();

            docSubscriptions = dao.getSubscriptionsDoc();

            // Firstly check to see if this Username already exists
            listTwitterSubscription = docSubscriptions.getSubscriptions().getTwitterList();
            boolInUse = false;

            if ((strUsername != null)
                && (listTwitterSubscription != null))
                {
                for (int i = 0;
                     (!boolInUse) && (i < listTwitterSubscription.size());
                     i++)
                    {
                    final TwitterSubscription subscription;

                    subscription = listTwitterSubscription.get(i);

                    boolInUse = ((subscription != null)
                                 && (strUsername.equalsIgnoreCase(subscription.getUsername())));
                    }
                }

            if (!boolInUse)
                {
                final TwitterSubscription twitterSubscription;

                // Add a Twitter Subscription with the specified parameter
                twitterSubscription =  docSubscriptions.getSubscriptions().addNewTwitter();

                twitterSubscription.setUsername(strUsername);

                if (XmlBeansUtilities.isValidXml(twitterSubscription))
                    {
                    // Automatically save the new Subscriptions
                    boolSuccess = dao.saveSubscriptions();

                    if (boolSuccess)
                        {
                        // If everything worked, log it...
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET
                                                                + NEWSFEED_TYPE + TERMINATOR
                                                                + METADATA_ACTION_ADD_SUBSCRIPTION
                                                                + METADATA_NAME + strUsername + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());

                        cmdFollow.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.FATAL,
                                                           METADATA_TARGET
                                                                + NEWSFEED_TYPE + TERMINATOR
                                                                + METADATA_ACTION_ADD_SUBSCRIPTION
                                                                + METADATA_RESULT
                                                                   + "Unable to save subscriptions"
                                                                   + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());

                        cmdFollow.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET
                                                            + NEWSFEED_TYPE + TERMINATOR
                                                            + METADATA_ACTION_ADD_SUBSCRIPTION
                                                            + METADATA_RESULT
                                                               + ResponseMessageStatus.INVALID_XML.getResponseValue()
                                                               + TERMINATOR ,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    cmdFollow.getResponse().setValue(ResponseMessageStatus.INVALID_XML.getResponseValue());
                    boolSuccess = false;
                    }
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET
                                                        + NEWSFEED_TYPE + TERMINATOR
                                                        + METADATA_ACTION_ADD_SUBSCRIPTION
                                                        + METADATA_RESULT
                                                           + "Username already subscribed"
                                                           + TERMINATOR ,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                cmdFollow.getResponse().setValue(ResponseMessageStatus.INVALID_PARAMETER.getResponseValue());
                boolSuccess = false;
                }
            }

        // Create the ResponseMessage
        if (boolSuccess)
            {
            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                cmdFollow);
            }
        else
            {
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                  commandmessage,
                                                                                  cmdFollow,
                                                                                  responseMessage);
            }

        return (responseMessage);
        }
    }
