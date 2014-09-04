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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.DiscoveryUtilities;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***************************************************************************************************
 * GetConfiguration.
 */

public final class GetConfiguration implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               FrameworkXpath,
                                               ObservatoryConstants
    {
    /***********************************************************************************************
     * getConfiguration().
     *
     * getConfiguration() reads the memories of all connected Modules,
     * and forms the XML configuration of the composite Instrument.
     * This Command is executed entirely on the host.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetConfiguration(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetConfiguration.dogetConfiguration() ";
        final int CHANNEL_COUNT = 0;
        final CommandType cmdGetConfig;
        final Instrument xmlInstrument;
        final XmlObject xmlController;
        final ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        String strResponseValue;

        LOGGER.debugTimedEvent(boolDebug,
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command in the CommandMessage
        cmdGetConfig = (CommandType)commandmessage.getCommandType().copy();

        // We haven't found anything yet...
        strResponseValue = UNEXPECTED;

        // Which Instrument is running this Command?
        // For a ready installed virtual Instrument, or one which has already been discovered,
        // then the incoming CommandMessage holds the real Instrument, with an address.
        // During the Discovery process, let's assume that the CommandMessage holds the
        // DiscoveryController, which is assumed to have a valid set of Core Commands.

        // The Instrument being used either to Discover, or has already been discovered :-)
        xmlInstrument = commandmessage.getInstrument();

        // We know we are only dealing with the Controller's Core module (as opposed to a Plugin)
        xmlController = xmlInstrument.getController();

        //------------------------------------------------------------------------------------------
        // Do the getConfiguration() operation, which expects a Response!

        // Is this a Virtual Instrument, i.e. the XML configuration is already on the host?
        if (DiscoveryUtilities.isGetConfigurationVirtualInstrument(cmdGetConfig))
            {
            // We must return the XML we already have on the host
            strResponseValue = xmlInstrument.xmlText();
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
            }
        else
            {
            // We need to get the configuration from the remote device, one block at a time
            // We expect a SteppedDataCommand, with one Command entry, but with no Parameters...
            if (((cmdGetConfig.getResponse() != null)
                && (RESPONSE_CONFIGURATION_XML.equals(cmdGetConfig.getResponse().getName())))
                && (cmdGetConfig.getSteppedDataCommandList() != null)
                && (cmdGetConfig.getSteppedDataCommandList().size() == 1))
                {
                // Always include the Controller
                strResponseValue = DiscoveryUtilities.assembleXmlFragments(dao,
                                                                           cmdGetConfig,
                                                                           xmlInstrument,
                                                                           xmlController,
                                                                           true,
                                                                           boolDebug);
                }
            else
                {
                // Something is wrong with the XML definition of getConfiguration()
                LOGGER.error(SOURCE + "Incorrectly set up in XML");
                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                      SOURCE,
                                                                                      METADATA_TARGET_CONFIGURATION,
                                                                                      METADATA_ACTION_ASSEMBLE));
                }
            }

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // Don't keep the data on ABORT...

        if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            {
            // Explicitly set the ResponseValue as the XML plain text
            cmdGetConfig.getResponse().setValue(strResponseValue);

            // These parameters are irrelevant here
//            dao.setRawDataChannelCount(CHANNEL_COUNT);
//            dao.setTemperatureChannel(false);

            // Create the ResponseMessage - this creates a DAOWrapper containing the XML
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      xmlInstrument,
                                                                      xmlController,
                                                                      cmdGetConfig,
                                                                      AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdGetConfig));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           xmlInstrument,
                                                                           xmlController,
                                                                           cmdGetConfig,
                                                                           AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdGetConfig));
            }

        return (responseMessage);
        }
    }
