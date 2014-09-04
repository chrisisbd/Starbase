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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ParameterChoiceToken;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.io.IOException;
import java.util.List;


/***************************************************************************************************
 * ImportMetadata
 */

public final class ImportMetadata implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportMetadata().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportMetadata(final ObservatoryInstrumentDAOInterface dao,
                                                            final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportMetadata.doImportMetadata() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_METADATATYPE = 0;
        final CommandType cmdAddMetadata;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdAddMetadata = (CommandType)commandmessage.getCommandType().copy();

        // Prepare for the worst
        cmdAddMetadata.getResponse().setValue(ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue());
        responseMessage = null;
        boolSuccess = false;

        // We expect one parameter, the MetadataType
        listParameters = cmdAddMetadata.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_METADATATYPE) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_METADATATYPE).getInputDataType().getDataTypeName()))
            && (dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (dao.getObservatoryClock() != null)
            && (dao.getObservatoryClock().getCalendarDateNow() != null)
            && (dao.getEventLogFragment() != null))
            {
            try
                {
                final String strMetadataType;
                final MetadataType metadataType;

                // Find out which MetadataType is required
                strMetadataType = listParameters.get(INDEX_METADATATYPE).getValue();

                // Map the parameter entry to a MetadataType
                metadataType = MetadataType.getMetadataTypeForName(strMetadataType);

                if (metadataType != null)
                    {
                    switch (metadataType)
                        {
                        // Framework Metadata end up in the Framework
                        case FRAMEWORK:
                            {
                            boolSuccess = MetadataHelper.importFrameworkMetadata(metadataType,
                                                                                 REGISTRY.getFramework());
                            DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);
                            break;
                            }

                        // Observatory end up in the Observatory
                        case OBSERVATORY:
                            {
                            final boolean boolLoaded;

                            boolLoaded = MetadataHelper.reloadObservatoryDefaultMetadata(dao.getHostInstrument(),
                                                                                         LOADER_PROPERTIES.isMetadataDebug());
                            // If the Metadata were reloaded, tell the Observatory listeners
                            if (boolLoaded)
                                {
                                dao.getHostInstrument().getContext().getObservatory().notifyObservatoryMetadataChangedEvent(dao,
                                                                                                                            EMPTY_STRING,
                                                                                                                            MetadataItemState.ADD);
                                }
                            else
                                {
                                LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                                             SOURCE + "Observatory Metadata already loaded");
                                }

                            if (dao.getHostInstrument().getContext().getObservatory().getObservatoryMetadata() != null)
                                {
                                // Remove all traces of any previous Observatory Metadata from this DAO
                                dao.getCurrentObservatoryMetadata().clear();

                                // Now add references to the Master ObservatoryMetadata to the current Instrument DAO containers,
                                // as in AbstractObservatoryInstrumentDAO.establishDAOIdentityForCapture()
                                dao.addAllMetadataToContainersTraced(dao.getHostInstrument().getContext().getObservatory().getObservatoryMetadata(),
                                                                     SOURCE + "Adding references to Observatory Metadata to DAO",
                                                                     LOADER_PROPERTIES.isMetadataDebug());
                                boolSuccess = true;
                                }

                            DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);
                            break;
                            }

                        // Observer (current User?) end up in the Observatory
                        case OBSERVER:
                            {
                            final boolean boolLoaded;

                            boolLoaded = MetadataHelper.reloadObserverDefaultMetadata(dao.getHostInstrument(),
                                                                                      LOADER_PROPERTIES.isMetadataDebug());
                            // If the Metadata were reloaded, tell the Observatory listeners
                            if (boolLoaded)
                                {
                                dao.getHostInstrument().getContext().getObservatory().notifyObserverMetadataChangedEvent(dao,
                                                                                                                         EMPTY_STRING,
                                                                                                                         MetadataItemState.ADD);
                                }
                            else
                                {
                                LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                                             SOURCE + "Observer Metadata already loaded");
                                }

                            if (dao.getHostInstrument().getContext().getObservatory().getObserverMetadata() != null)
                                {
                                // Remove all traces of any previous Observer Metadata from this DAO
                                dao.getCurrentObserverMetadata().clear();

                                // Now add references to the Master ObserverMetadata to the current Instrument DAO containers,
                                // as in AbstractObservatoryInstrumentDAO.establishDAOIdentityForCapture()
                                dao.addAllMetadataToContainersTraced(dao.getHostInstrument().getContext().getObservatory().getObserverMetadata(),
                                                                     SOURCE + "Adding references to Observer Metadata to DAO",
                                                                     LOADER_PROPERTIES.isMetadataDebug());
                                boolSuccess = true;
                                }

                            DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);
                            break;
                            }

                        // All of the following have the filename prefixed by the Instrument Identifier,
                        // and so end up in the DAO Metadata

                        case OBSERVATION:
                            {
                            boolSuccess = MetadataHelper.importObservationMetadata(metadataType, dao);
                            DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);
                            break;
                            }

                        case INSTRUMENT:
                            {
                            boolSuccess = MetadataHelper.importInstrumentMetadata(metadataType, dao);
                            DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);
                            break;
                            }

                        case CONTROLLER:
                            {
                            // Modify the Keys of each item to show that they came from the Instrument
                            boolSuccess = MetadataHelper.importControllerMetadata(metadataType, dao);
                            DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);
                            break;
                            }

                        default:
                            {
                            // An unrecognised MetadataType
                            throw new XmlException(SOURCE + "Unrecognised MetadataType");
                            }
                        }

                    if (boolSuccess)
                        {
                        // If everything worked, log it...
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET
                                                               + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                               + METADATA_ACTION_IMPORT_METADATA
                                                               + METADATA_CATEGORY + metadataType.getName() + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        }
                    }
                else
                    {
                    // Assume we have been given a Plugin Identifier in strMetadataType
                    if ((strMetadataType != null)
                        && (strMetadataType.endsWith(ParameterChoiceToken.CHOICE_SUFFIX_MODULE)))
                        {
                        final String strPluginIdentifier;

                        // Remove the "_Module" from the Plugin choice
                        strPluginIdentifier = strMetadataType.substring(0, strMetadataType.length() - ParameterChoiceToken.CHOICE_SUFFIX_MODULE.length());

                        boolSuccess = MetadataHelper.importPluginMetadata(MetadataType.PLUGIN,
                                                                          strPluginIdentifier,
                                                                          dao);
                        DAOHelper.setResponseValue(cmdAddMetadata, boolSuccess);

                        if (boolSuccess)
                            {
                            // If everything worked, log it...
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.INFO,
                                                               METADATA_TARGET
                                                                   + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                                   + METADATA_ACTION_IMPORT_METADATA
                                                                   + METADATA_CATEGORY + dao.getHostInstrument().getInstrument().getIdentifier() + DOT
                                                                   + strPluginIdentifier + TERMINATOR,
                                                               SOURCE,
                                                               dao.getObservatoryClock());
                            }
                        }
                    }
                }

            catch (XmlException exception)
                {
                LOGGER.error(SOURCE + "XmlException = " + exception.getMessage());
                cmdAddMetadata.getResponse().setValue(ResponseMessageStatus.INVALID_XML.getResponseValue());
                boolSuccess = false;
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + "IOException = " + exception.getMessage());
                cmdAddMetadata.getResponse().setValue(ResponseMessageStatus.RESPONSE_NODATA);
                boolSuccess = false;
                }
            }

            // Create the ResponseMessage
            if (boolSuccess)
                {
                REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                    commandmessage,
                                                                                    cmdAddMetadata);
                }
            else
                {
                responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                      commandmessage,
                                                                                      cmdAddMetadata,
                                                                                      responseMessage);
                }

        return (responseMessage);
        }
    }