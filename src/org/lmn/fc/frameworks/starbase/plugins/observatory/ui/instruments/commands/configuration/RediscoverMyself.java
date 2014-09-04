// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010,
//           2011, 2012, 2013, 2014
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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.XpathHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.DiscoveryUtilities;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/***************************************************************************************************
 * RediscoverMyself.
 */

public final class RediscoverMyself implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               FrameworkXpath,
                                               ObservatoryConstants
    {
    /**********************************************************************************************
     * Rediscovers and validates the plugins for this Instrument.
     * If all XML from Controller and all Plugins is valid,
     * the current instrument is stopped, and replaced with the new assembled XML.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doRediscoverMyself(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "RediscoverMyself.doRediscoverMyself() ";
        final String COMMAND_ID = COMMAND_CORE_GET_CONFIGURATION;
        final int PARAMETER_COUNT = 1;
        final int INDEX_DISCOVER_CONTROLLER = 0;
        final CommandType cmdRediscover;
        final Instrument xmlInstrument;
        final XmlObject xmlController;
        final ResponseMessageInterface responseMessage;
        String strResponseValue;
        final boolean boolDebug;

        boolDebug = true;  //TODO dao.isDebugMode();

        LOGGER.debug(boolDebug,
                     SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command in the CommandMessage
        cmdRediscover = (CommandType)commandmessage.getCommandType().copy();

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

        if ((xmlInstrument != null)
            && (xmlInstrument.getControllable())
            && (xmlController != null))
            {
            final int intChoice;
            final String[] strMessage =
                {
                "Are you sure that you wish to rediscover this Instrument?",
                "This action will stop all current activity and lose all data.",
                "Any open data connections will be closed."
                };
            final Object[] options = {"Discover",
                                      "Cancel"};

            intChoice = JOptionPane.showOptionDialog(null,
                                                     strMessage,
                                                     "Instrument Discovery",
                                                     JOptionPane.OK_CANCEL_OPTION,
                                                     JOptionPane.WARNING_MESSAGE,
                                                     null,
                                                     options,
                                                     options[1]);
            if (intChoice == JOptionPane.YES_OPTION)
                {
                final List<ParameterType> listParameters;

                // We expect one parameter, Discover.Controller
                listParameters = cmdRediscover.getParameterList();

                if ((listParameters != null)
                    && (listParameters.size() == PARAMETER_COUNT)
                    && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_DISCOVER_CONTROLLER).getInputDataType().getDataTypeName()))
                    && (dao.getHostInstrument() != null))
                    {
                    final boolean boolDiscoverController;

                    // This should never throw NumberFormatException, because it has already been parsed
                    boolDiscoverController = Boolean.parseBoolean(listParameters.get(INDEX_DISCOVER_CONTROLLER).getValue());

                    // Is this a Virtual Instrument, i.e. the XML configuration is already on the host?
                    if ((xmlInstrument.getController().getVirtualAddress() != null)
                        && (xmlInstrument.getController().getVirtualAddress().length() == 3)
                        && (ObservatoryInstrumentHelper.isVirtualController(xmlInstrument)))
                        {
                        // We must return the XML we already have on the host
                        strResponseValue = xmlInstrument.xmlText();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

                        // ToDo Review if stop/start is needed for consistency?
                        // ToDo I suppose the XML could have been altered programmatically?
                        //System.out.println("Not sure if a full stop/start is really needed here?");
                        }
                    else
                        {
                        final CommandType cmdGetConfig;

                        // We need to find the Core.getConfiguration() command *for this Instrument*,
                        // then use it to gather all XML again from all attached plugins, since they may have changed
                        cmdGetConfig = XpathHelper.findCommandFromIdentifier(xmlInstrument,
                                                                             xmlController,
                                                                             COMMAND_ID,
                                                                             boolDebug);
                        if (cmdGetConfig != null)
                            {
                            // We need to get the configuration from the remote devices, one block at a time
                            // We expect a SteppedDataCommand, with one Command entry, but with no Parameters...
                            if (((cmdGetConfig.getResponse() != null)
                                 && (RESPONSE_CONFIGURATION_XML.equals(cmdGetConfig.getResponse().getName())))
                                && (cmdGetConfig.getSteppedDataCommandList() != null)
                                && (cmdGetConfig.getSteppedDataCommandList().size() == 1))
                                {
                                strResponseValue = DiscoveryUtilities.assembleXmlFragments(dao,
                                                                                           cmdGetConfig,
                                                                                           xmlInstrument,
                                                                                           xmlController,
                                                                                           boolDiscoverController,
                                                                                           boolDebug);
                                // A fairly fatal error
                                if (ResponseMessageStatus.RESPONSE_NODATA.equals(strResponseValue))
                                    {
                                    // Something is wrong with one or more Plugin XML fragments
                                    LOGGER.error(SOURCE + "One or more Plugin XML fragments are invalid, see the EventLog");
                                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                          SOURCE,
                                                                                                          METADATA_TARGET_CONFIGURATION,
                                                                                                          METADATA_ACTION_ASSEMBLE));
                                    }
                                else
                                    {
                                    // Now check the DAO ResponseMessageStatus
                                    if (ResponseMessageStatus.isResponseStatusOk(dao.getResponseMessageStatusList()))
                                        {
                                        // It all seemed to work?

//                                        System.out.println("ResponseStatusOk --> All Controller and Plugin XML assembled ok");
//                                        System.out.println("STITCHED XML IS:========================================================================================");
//                                        System.out.println(strResponseValue);
//                                        System.out.println("========================================================================================================");
//                                        System.out.println("REBUILD INSTRUMENT!!!!!");

                                        try
                                            {
                                            final InstrumentsDocument docInstruments;
                                            final InstrumentsDocument.Instruments instruments;

                                            // The XML document in ResponseValue should now contain a single valid Instrument, so try to parse it
                                            docInstruments = InstrumentsDocument.Factory.parse(strResponseValue);
                                            instruments = docInstruments.getInstruments();

                                            //System.out.println("Passed parsing");

                                            if ((instruments != null)
                                                && (instruments.getInstrumentList() != null)
                                                && (instruments.getInstrumentList().size() == 1))
                                                {
                                                final Instrument instrument;

                                                instrument = instruments.getInstrumentList().get(0);

                                                if ((instrument != null)
                                                    && (dao.getHostInstrument() != null))
                                                    {
                                                    final ObservatoryInstrumentInterface observatoryInstrument;

//                                                    System.out.println("Try to instantiate a new ObservatoryInstrument from the classname in the assembled Xml");
//                                                    System.out.println("UI ResourceKey=" + dao.getHostInstrument().getHostUI().getResourceKey());
                                                    observatoryInstrument = ObservatoryInstrumentHelper.instantiateInstrument(instrument,
                                                                                                                              dao.getHostInstrument().getHostAtom(),
                                                                                                                              dao.getHostInstrument().getHostUI(),
                                                                                                                              dao.getHostInstrument().getHostUI().getResourceKey());
                                                    if (observatoryInstrument != null)
                                                        {
//                                                        System.out.println("****************** INSTANTIATED! *****************");
//                                                        System.out.println("Discovered Description=" + observatoryInstrument.getInstrument().getDescription());

                                                        // Remove the *existing* Instrument from the ObservatoryUI,
                                                        // from all Groups
                                                        ObservatoryUIHelper.removeInstrumentFromObservatory(dao.getHostInstrument().getHostUI(),
                                                                                                            dao.getHostInstrument());
//                                                        System.out.println("BEFORE STOP key.instr=" + dao.getHostInstrument().getResourceKey());
//                                                        System.out.println("BEFORE STOP key.ui=" + dao.getHostInstrument().getHostUI().getResourceKey());
//                                                        System.out.println("BEFORE STOP key.dao=" + dao.getResourceKey());
//                                                        System.out.println("Stop status =" + dao.getHostInstrument().stop());

                                                        // Now install the *new* Instrument into the existing ObservatoryUI
                                                        // for the currently selected InstrumentSelector
                                                        // Leave it to the User to start it up
                                                        ObservatoryUIHelper.addInstrumentToObservatory(dao.getHostInstrument().getHostUI(),
                                                                                                       dao.getHostInstrument().getHostUI().getCurrentGroupInstrumentSelector(),
                                                                                                       observatoryInstrument);
//                                                        System.out.println("AFTER ADD ******************");

                                                        // This DAO should evaporate after use!
                                                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                                        }
                                                    else
                                                        {
                                                        LOGGER.error("It was not possible to instantiate an Instrument from the assembled XML");
                                                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                                        }
                                                    }
                                                else
                                                    {
                                                    LOGGER.error(SOURCE + "Unable to retrieve Instrument (NULL)");
                                                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                                          SOURCE,
                                                                                                                          METADATA_TARGET_COMMAND,
                                                                                                                          METADATA_ACTION_VALIDATE + METADATA_TEXT + COMMAND_ID + TERMINATOR));
                                                    }
                                                }
                                            else
                                                {
                                                LOGGER.error(SOURCE + "Instruments XML is NULL or invalid, or there is more than one Instrument defined");
                                                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                                      SOURCE,
                                                                                                                      METADATA_TARGET_COMMAND,
                                                                                                                      METADATA_ACTION_VALIDATE + METADATA_TEXT + COMMAND_ID + TERMINATOR));
                                                }
                                            }

                                        catch (final XmlException exception)
                                            {
                                            LOGGER.error(SOURCE + "Rediscovered instrument is incorrectly set up in the Instrument XML");
                                            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                                  SOURCE,
                                                                                                                  METADATA_TARGET_COMMAND,
                                                                                                                  METADATA_ACTION_VALIDATE + METADATA_TEXT + COMMAND_ID + TERMINATOR));
                                            }
                                        }
                                    else
                                        {
                                        // Leave the DAO ResponseMessageStatusList to tell its story later
                                        LOGGER.error(SOURCE + "ResponseMessageStatus contains an error after XML assembly");
                                        LOGGER.error(SOURCE + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList()));
                                        }
                                    }
                                }
                            else
                                {
                                // Something is wrong with the XML definition of getConfiguration()
                                LOGGER.error(SOURCE + "Core.getConfiguration() is incorrectly set up in the Instrument XML");
                                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                      SOURCE,
                                                                                                      METADATA_TARGET_COMMAND,
                                                                                                      METADATA_ACTION_VALIDATE + METADATA_TEXT + COMMAND_ID + TERMINATOR));
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Unable to locate Command [command.id=" + COMMAND_ID + "]");
                            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                  SOURCE,
                                                                                                  METADATA_TARGET_COMMAND,
                                                                                                  METADATA_ACTION_FIND + METADATA_TEXT + COMMAND_ID + TERMINATOR));
                            }
                        }
                    }
                else
                    {
                    // The XML configuration was inappropriate
                    strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                          SOURCE,
                                                                                          METADATA_TARGET
                                                                                              + SOURCE.trim()
                                                                                              + TERMINATOR,
                                                                                          METADATA_ACTION_COMMAND));
                    }
                }
            else
                {
                // The User decided not to do it!
                Toolkit.getDefaultToolkit().beep();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }
            }
        else
            {
            // It is not possible to discover an Instrument with no Controller!
            LOGGER.error(SOURCE + "It is not possible to discover an Instrument with no Controller!");
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                  SOURCE,
                                                                                  METADATA_TARGET_INSTRUMENT,
                                                                                  METADATA_ACTION_DISCOVER + METADATA_REASON + "No Controller found" + TERMINATOR));
            }

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage
        // Don't keep the data on ABORT...

        if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            {
            // Explicitly set the ResponseValue as the XML plain text
            cmdRediscover.getResponse().setValue(strResponseValue);

            // These parameters are irrelevant here
            //            dao.setRawDataChannelCount(CHANNEL_COUNT);
            //            dao.setTemperatureChannel(false);

            // Create the ResponseMessage - this creates a DAOWrapper containing the XML
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      xmlInstrument,
                                                                      xmlController,
                                                                      cmdRediscover,
                                                                      AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdRediscover));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           xmlInstrument,
                                                                           xmlController,
                                                                           cmdRediscover,
                                                                           AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdRediscover));
            }

        return (responseMessage);
        }
    }
