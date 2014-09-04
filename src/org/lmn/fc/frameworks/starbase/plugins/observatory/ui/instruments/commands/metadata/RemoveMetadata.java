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

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.List;


/***************************************************************************************************
 * RemoveMetadata.
 */

public final class RemoveMetadata implements FrameworkConstants,
                                             FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkSingletons,
                                             ObservatoryConstants
    {
    /***********************************************************************************************
     * doRemoveMetadata().
     * This has the same logic as class MetadataUIHelper.createComboRemoveMetadata().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doRemoveMetadata(final ObservatoryInstrumentDAOInterface dao,
                                                            final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "RemoveMetadata.doRemoveMetadata() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_METADATATYPE = 0;
        final CommandType cmdRemoveMetadata;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        boolean boolSuccess;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdRemoveMetadata = (CommandType)commandmessage.getCommandType().copy();

        // Prepare for the worst
        responseMessage = null;
        boolSuccess = false;

        // We expect one parameter, the MetadataType
        listParameters = cmdRemoveMetadata.getParameterList();

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
                        case FRAMEWORK:
                            {
                            final FrameworkPlugin framework;

                            framework = REGISTRY.getFramework();

                            // The Framework holds the Framework Metadata
                            framework.getFrameworkMetadata().clear();

                            // ... but we can't go on like this, so force creation of the basics
                            framework.getLongitude();
                            framework.getLatitude();
                            framework.getHASL();
                            framework.getTimeZoneCode();
                            boolSuccess = true;

                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.INFO,
                                                               METADATA_FRAMEWORK_RESET
                                                                   + METADATA_LONGITUDE + framework.getLongitude().toString() + TERMINATOR_SPACE
                                                                   + METADATA_LATITUDE + framework.getLatitude().toString() + TERMINATOR_SPACE
                                                                   + METADATA_HASL + Double.toString(framework.getHASL()) + TERMINATOR_SPACE
                                                                   + METADATA_TIMEZONE + framework.getTimeZoneCode() + TERMINATOR,
                                                               SOURCE,
                                                               dao.getObservatoryClock());

                            DAOHelper.setResponseValue(cmdRemoveMetadata, boolSuccess);
                            break;
                            }

                        case OBSERVATORY:
                            {
                            if ((dao.getHostInstrument().getContext().getObservatory() != null)
                                && (dao.getHostInstrument().getContext().getObservatory().getObservatoryMetadata() != null))
                                {
                                // Remove the DAO references
                                if (dao.getCurrentObservatoryMetadata() != null)
                                    {
                                    dao.getCurrentObservatoryMetadata().clear();
                                    }

                                // Remove the Wrapper references
                                if ((dao.getWrappedData() != null)
                                    && (dao.getWrappedData().getCurrentObservatoryMetadata() != null))
                                    {
                                    dao.getWrappedData().getCurrentObservatoryMetadata().clear();
                                    }

                                // If there are no ObservatoryMetadata listeners, remove the underlying master data also
                                // This is very unlikely to occur!
                                if (dao.getHostInstrument().getContext().getObservatory().getObservatoryMetadataChangedListeners().isEmpty())
                                    {
                                    // The Observatory holds the master Observatory Metadata, so clear it
                                    dao.getHostInstrument().getContext().getObservatory().getObservatoryMetadata().clear();
                                    dao.getHostInstrument().getContext().getObservatory().setObservatoryMetadataLoaded(false);

                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.INFO,
                                                                       METADATA_TARGET_METADATA
                                                                           + METADATA_ACTION + "Metadata unloaded" + TERMINATOR_SPACE
                                                                           + METADATA_CATEGORY + metadataType.getName() + TERMINATOR,
                                                                       SOURCE,
                                                                       dao.getObservatoryClock());
                                    }

                                // Tell the Observatory listeners
                                dao.getHostInstrument().getContext().getObservatory().notifyObservatoryMetadataChangedEvent(dao,
                                                                                                                            EMPTY_STRING,
                                                                                                                            MetadataItemState.UNLOAD);
                                boolSuccess = true;
                                }

                            DAOHelper.setResponseValue(cmdRemoveMetadata, boolSuccess);
                            break;
                            }

                        case OBSERVER:
                            {
                            if ((dao.getHostInstrument().getContext().getObservatory() != null)
                                && (dao.getHostInstrument().getContext().getObservatory().getObserverMetadata() != null))
                                {
                                // Remove the DAO references
                                if (dao.getCurrentObserverMetadata() != null)
                                    {
                                    dao.getCurrentObserverMetadata().clear();
                                    }

                                // Remove the Wrapper references
                                if ((dao.getWrappedData() != null)
                                    && (dao.getWrappedData().getCurrentObserverMetadata() != null))
                                    {
                                    dao.getWrappedData().getCurrentObserverMetadata().clear();
                                    }

                                // If there are no ObserverMetadata listeners, remove the underlying master data also
                                // This is very unlikely to occur!
                                if (dao.getHostInstrument().getContext().getObservatory().getObserverMetadataChangedListeners().isEmpty())
                                    {
                                    // The Observatory holds the Observer Metadata, so clear it
                                    dao.getHostInstrument().getContext().getObservatory().getObserverMetadata().clear();
                                    dao.getHostInstrument().getContext().getObservatory().setObserverMetadataLoaded(false);

                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.INFO,
                                                                       METADATA_TARGET_METADATA
                                                                           + METADATA_ACTION + "Metadata unloaded" + TERMINATOR_SPACE
                                                                           + METADATA_CATEGORY + metadataType.getName() + TERMINATOR,
                                                                       SOURCE,
                                                                       dao.getObservatoryClock());
                                    }

                                // Tell the Observatory listeners
                                dao.getHostInstrument().getContext().getObservatory().notifyObserverMetadataChangedEvent(dao,
                                                                                                                         EMPTY_STRING,
                                                                                                                         MetadataItemState.UNLOAD);
                                boolSuccess = true;
                                }

                            DAOHelper.setResponseValue(cmdRemoveMetadata, boolSuccess);
                            break;
                            }

                        case OBSERVATION:
                            {
                            // The DAO holds the Observation Metadata
                            if (dao.getObservationMetadata() != null)
                                {
                                dao.getObservationMetadata().clear();
                                boolSuccess = true;
                                }

                            // Clear Metadata in the DAO Wrapper,
                            // whose references point back to the DAO so they should have been cleared above
                            if ((dao.getWrappedData() != null)
                                && (dao.getWrappedData().getObservationMetadata() != null))
                                {
                                dao.getWrappedData().getObservationMetadata().clear();
                                boolSuccess = true;
                                }

                            DAOHelper.setResponseValue(cmdRemoveMetadata, boolSuccess);
                            break;
                            }

                        case INSTRUMENT:
                            {
                            // The DAO holds the Instrument, Controller, Plugin Metadata
                            // Clear all for Instrument, Controller, Plugin for simplicity
                            if (dao.getInstrumentMetadata() != null)
                                {
                                dao.getInstrumentMetadata().clear();
                                boolSuccess = true;
                                }

                            if (dao.getControllerMetadata() != null)
                                {
                                dao.getControllerMetadata().clear();
                                boolSuccess = true;
                                }

                            if (dao.getPluginMetadata() != null)
                                {
                                dao.getPluginMetadata().clear();
                                boolSuccess = true;
                                }

                            // Clear Metadata in the DAO Wrapper,
                            // whose references point back to the DAO so they should have been cleared above
                            if (dao.getWrappedData() != null)
                                {
                                if (dao.getWrappedData().getInstrumentMetadata() != null)
                                    {
                                    dao.getWrappedData().getInstrumentMetadata().clear();
                                    boolSuccess = true;
                                    }

                                if (dao.getWrappedData().getControllerMetadata() != null)
                                    {
                                    dao.getWrappedData().getControllerMetadata().clear();
                                    boolSuccess = true;
                                    }

                                if (dao.getWrappedData().getPluginMetadata() != null)
                                    {
                                    dao.getWrappedData().getPluginMetadata().clear();
                                    boolSuccess = true;
                                    }
                                }

                            DAOHelper.setResponseValue(cmdRemoveMetadata, boolSuccess);
                            break;
                            }

                        case METADATA:
                            {
                            if (dao.getMetadataMetadata() != null)
                                {
                                dao.getMetadataMetadata().clear();
                                boolSuccess = true;
                                }

                            // Clear MetadataMetadata in the DAO Wrapper,
                            // whose references point back to the DAO so they should have been cleared above
                            if ((dao.getWrappedData() != null)
                                && (dao.getWrappedData().getMetadataMetadata() != null))
                                {
                                dao.getWrappedData().getMetadataMetadata().clear();
                                boolSuccess = true;
                                }
                            break;
                            }

                        default:
                            {
                            // An unrecognised MetadataType
                            throw new XmlException(SOURCE + EXCEPTION_UNRECOGNISED_METADATATYPE);
                            }
                        }

                    if (boolSuccess)
                        {
                        // If everything worked, log it...
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET
                                                               + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                               + METADATA_ACTION_REMOVE_METADATA
                                                               + METADATA_CATEGORY + metadataType.getName() + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        }
                    }
                }

            catch (XmlException exception)
                {
                LOGGER.error(SOURCE + "XmlException = " + exception.getMessage());
                cmdRemoveMetadata.getResponse().setValue(ResponseMessageStatus.INVALID_XML.getResponseValue());
                boolSuccess = false;
                }
            }

        // Recollect the Instrument's Aggregate Metadata just to be sure
        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getContext() != null))
            {
            final List<Metadata> listMetadata;

            listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                         dao.getHostInstrument().getContext().getObservatory(),
                                                                         dao.getHostInstrument(),
                                                                         dao,
                                                                         dao.getWrappedData(),
                                                                         SOURCE,
                                                                         LOADER_PROPERTIES.isMetadataDebug());
            // Set the Aggregate Metadata on the host Instrument
            // All e.g. Control panel data are taken from here
            // NOTE THAT The DAO data take precedence over those in the Wrapper
            dao.getHostInstrument().setAggregateMetadata(listMetadata);
            }

        // Tidy up, just in case!
        ObservatoryInstrumentHelper.runGarbageCollector();

        // Create the ResponseMessage
        if (boolSuccess)
            {
            // Something has changed, we may need to update indicators etc.
            REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
            InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                commandmessage,
                                                                                cmdRemoveMetadata);
            }
        else
            {
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                  commandmessage,
                                                                                  cmdRemoveMetadata,
                                                                                  responseMessage);
            }

        return (responseMessage);
        }
    }
