// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * ValidateConfigurationXML.
 */

public final class ValidateConfigurationXML implements FrameworkConstants,
                                                       FrameworkStrings,
                                                       FrameworkMetadata,
                                                       FrameworkSingletons,
                                                       ObservatoryConstants
    {
    /***********************************************************************************************
     * doValidateConfigurationXML().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doValidateConfigurationXML(final ObservatoryInstrumentDAOInterface dao,
                                                                      final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ValidateConfigurationXML.doValidateConfigurationXML() ";
        final CommandType cmdValidateXML;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Don't affect the CommandType of the incoming Command
        cmdValidateXML = (CommandType)commandmessage.getCommandType().copy();

        // Basic XML validation test
        if ((dao.getHostInstrument() != null)
            && (XmlBeansUtilities.isValidXml(dao.getHostInstrument().getInstrument())))
            {
            // More than one PrimaryPlugin

            // PluginManifest consistency

            // Command with PluginProvider of Controller does not have CommandVariant of 0000

            // CommandVariant codes present vs. (todo) enum of supported CommandVariants

            // BlockedDataCommand links

            // SteppedDataCommand links

            // IteratedDataCommand links

            strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                   + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                   + METADATA_ACTION_VALIDATE
                                                   + METADATA_RESULT_SUCCESS,
                                               SOURCE,
                                               dao.getObservatoryClock());
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

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdValidateXML,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
