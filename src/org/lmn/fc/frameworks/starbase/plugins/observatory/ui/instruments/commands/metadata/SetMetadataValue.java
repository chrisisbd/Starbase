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
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.List;


/***************************************************************************************************
 * SetMetadataValue.
 */

public final class SetMetadataValue implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ObservatoryConstants
    {
    /***********************************************************************************************
     * setMetadataValue().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSetMetadataValue(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SetMetadataValue.doSetMetadataValue() ";
        final int PARAMETER_COUNT = 2;
        final int INDEX_KEY = 0;
        final int INDEX_VALUE = 1;
        final CommandType cmdSetMetadata;
        final List<ParameterType> listParameters;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdSetMetadata = (CommandType)commandmessage.getCommandType().copy();

        // Parameters - there's no need to use the ExecutionParameters
        listParameters = cmdSetMetadata.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        // Expect two Parameters:
        //
        //  Metadata.Key
        //  Metadata.Value

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (dao.getWrappedData() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_KEY) != null)
            && (SchemaDataType.METADATA_KEY.equals(listParameters.get(INDEX_KEY).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_VALUE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_VALUE).getInputDataType().getDataTypeName())))
            {
            final String strIdentifier;
            final String strKey;
            final String strValue;
            final List<Metadata> listMetadata;
            final Metadata metadataOriginal;

            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();

            strKey = listParameters.get(INDEX_KEY).getValue();
            strValue = listParameters.get(INDEX_VALUE).getValue();

            // Do we know about this Metadata Key in the Framework, Observatory, Instrument or the DAO??
            listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                         dao.getHostInstrument().getContext().getObservatory(),
                                                                         dao.getHostInstrument(),
                                                                         dao, dao.getWrappedData(),
                                                                         SOURCE,
                                                                         LOADER_PROPERTIES.isMetadataDebug());
            metadataOriginal = MetadataHelper.getMetadataByKey(listMetadata, strKey);

            if (metadataOriginal != null)
                {
                final Metadata metadataTest;

                // Don't affect the Original just yet
                metadataTest = (Metadata)metadataOriginal.copy();
                metadataTest.setValue(strValue);

                // Check that the supplied Value results in valid Metadata
                if (MetadataHelper.isValidMetadataItem(metadataTest))
                    {
                    // It did, so update the **Original** Value
                    metadataOriginal.setValue(strValue);

                    if ((metadataOriginal.getKey().startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey()))
                        && (dao.getHostInstrument().getContext().getObservatory() != null))
                        {
                        dao.getHostInstrument().getContext().getObservatory().notifyObservatoryMetadataChangedEvent(dao,
                                                                                                                    metadataOriginal.getKey(),
                                                                                                                    MetadataItemState.EDIT);
                        }
                    else if ((metadataOriginal.getKey().startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey()))
                        && (dao.getHostInstrument().getContext().getObservatory() != null))
                        {
                        dao.getHostInstrument().getContext().getObservatory().notifyObserverMetadataChangedEvent(dao,
                                                                                                                 metadataOriginal.getKey(),
                                                                                                                 MetadataItemState.EDIT);
                        }

                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                            + strIdentifier + TERMINATOR
                                                            + METADATA_ACTION_SET_METADATA
                                                            + METADATA_KEY + strKey + TERMINATOR_SPACE
                                                            + METADATA_VALUE + strValue + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else
                    {
                    // The Command must fail since the supplied Value is invalid
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET
                                                            + strIdentifier + TERMINATOR
                                                            + METADATA_ACTION_SET_METADATA
                                                            + METADATA_RESULT + METADATA_FAIL + SPACE
                                                            + METADATA_REASON + "invalid value" + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }
            else
                {
                // The Key was not found
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET
                                                            + strIdentifier + TERMINATOR
                                                            + METADATA_ACTION_SET_METADATA
                                                        + METADATA_RESULT + METADATA_FAIL + SPACE
                                                        + METADATA_REASON + "invalid key" + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // Incorrectly configured XML
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                    SOURCE,
                                                                                    METADATA_TARGET_UNKNOWN,
                                                                                    METADATA_ACTION_SET_METADATA));
            }

        // Create the ResponseMessage
        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
            InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

            // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
            cmdSetMetadata.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdSetMetadata,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdSetMetadata));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdSetMetadata,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdSetMetadata));
             }

        return (responseMessage);
        }
    }
