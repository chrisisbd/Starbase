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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.CustomMetadataDocument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.List;


/***************************************************************************************************
 * ImportMetadata
 */

public final class ImportMetadataLocal implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportMetadataLocal().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportMetadataLocal(final ObservatoryInstrumentDAOInterface dao,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportMetadataLocal.doImportMetadataLocal() ";
        final int PARAMETER_COUNT = 1;
        final int INDEX_FILENAME = 0;
        final CommandType cmdImportMetadataLocal;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debug(dao.isDebugMode(), SOURCE);

        // Don't affect the CommandType of the incoming Command
        cmdImportMetadataLocal = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, the Filename
        listParameters = cmdImportMetadataLocal.getParameterList();

        // Check the Command parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (dao.getObservatoryClock() != null)
            && (dao.getObservatoryClock().getCalendarDateNow() != null)
            && (dao.getEventLogFragment() != null))
            {
            try
                {
                final String strFilename;

                // Get the latest Resources
                dao.readResources();

                strFilename = listParameters.get(INDEX_FILENAME).getValue();

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_METADATA
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_FILENAME + strFilename + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                if ((strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename)))
                    {
                    final File fileMetadata;
                    final CustomMetadataDocument docCustomMetadata;

                    fileMetadata = new File(strFilename);
                    docCustomMetadata = CustomMetadataDocument.Factory.parse(fileMetadata);

                    if (XmlBeansUtilities.isValidXml(docCustomMetadata))
                        {
                        final CustomMetadataDocument.CustomMetadata customMetadata;

                        customMetadata = docCustomMetadata.getCustomMetadata();

                        if ((customMetadata != null)
                            && (!customMetadata.getMetadataList().isEmpty()))
                            {
                            int intImportedCount;

                            intImportedCount = 0;

                            for (int intMetadataIndex = 0;
                                 intMetadataIndex < customMetadata.getMetadataList().size();
                                 intMetadataIndex++)
                                {
                                final Metadata metadataItem;
                                final List<Metadata> listMetadataTarget;

                                metadataItem = customMetadata.getMetadataList().get(intMetadataIndex);

                                // Return NULL if no suitable container is found
                                listMetadataTarget = MetadataHelper.findMetadataContainerByKeyTraced(dao,
                                                                                                     metadataItem.getKey(),
                                                                                                     SOURCE,
                                                                                                     dao.isDebugMode());
                                if (listMetadataTarget != null)
                                    {
                                    listMetadataTarget.add(metadataItem);
                                    intImportedCount++;
                                    }
                                else
                                    {
                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_METADATA
                                                                           + METADATA_ACTION_IMPORT
                                                                           + METADATA_KEY + metadataItem.getKey() + TERMINATOR_SPACE
                                                                           + METADATA_RESULT_NOT_IN_MDD,
                                                                       SOURCE,
                                                                       dao.getObservatoryClock());
                                    }
                                }

                            // If everything worked, log it...
                            if (intImportedCount > 0)
                                {
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.INFO,
                                                                   METADATA_TARGET_METADATA
                                                                       + METADATA_ACTION_IMPORT
                                                                       + METADATA_RESULT_SUCCESS + SPACE
                                                                       + METADATA_COUNT + intImportedCount + TERMINATOR,
                                                                   SOURCE,
                                                                   dao.getObservatoryClock());

                                // At least one item counts as a success
                                strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                }
                            else
                                {
                                // Failed perhaps because the Keys were not recognised
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_METADATA
                                                                       + METADATA_ACTION_IMPORT
                                                                       + METADATA_RESULT_NO_DATA,
                                                                   SOURCE,
                                                                   dao.getObservatoryClock());

                                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_METADATA
                                                                   + METADATA_ACTION_IMPORT
                                                                   + METADATA_RESULT_NO_DATA,
                                                               SOURCE,
                                                               dao.getObservatoryClock());

                            strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                            }
                        }
                    else
                        {
                        strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                        dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                              SOURCE,
                                                                                              METADATA_TARGET_METADATA,
                                                                                              METADATA_ACTION_IMPORT));
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_METADATA
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_RESULT_INVALID_FILENAME,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }

            catch (XmlException exception)
                {
                strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                      SOURCE,
                                                                                      METADATA_TARGET_METADATA,
                                                                                      METADATA_ACTION_IMPORT));
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_METADATA
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_RESULT_FILE_NOT_FOUND,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }
            }
        else
            {
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                  SOURCE,
                                                                                  METADATA_TARGET_METADATA,
                                                                                  METADATA_ACTION_IMPORT));
            }

        // Create the ResponseMessage
        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdImportMetadataLocal,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }