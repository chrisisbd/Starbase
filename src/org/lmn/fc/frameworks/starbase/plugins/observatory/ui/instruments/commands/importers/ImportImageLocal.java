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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
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

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;


/***************************************************************************************************
 * ImportImageLocal.
 */

public final class ImportImageLocal implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportImageLocal().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportImageLocal(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportImageLocal.doImportImageLocal()";
        final int PARAMETER_COUNT = 1;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, the filename
        listParameters = commandType.getParameterList();
        responseMessage = null;

        // Do not clear RawData, because Images have different containers

        // Check the Command parameters before continuing to retrieve the data file
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(0).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;

                strFilename = listParameters.get(0).getValue();

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "ImportImageLocal.importImageLocal() [filename=" + strFilename + "]");

                if ((strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename.trim())))
                    {
                    final File fileImage;
                    final FileImageInputStream stream;
                    final BufferedImage image;

                    // See: http://www.exampledepot.com/egs/javax.imageio/DiscType.html?l=rel
                    fileImage = new File(strFilename);
                    stream = new FileImageInputStream(fileImage);
                    image = ImageIO.read(stream);

                    //--------------------------------------------------------------------------
                    // If we get here, it must have succeeded...

                    // Pass the Image to the DAO, and then to the Instrument and InstrumentPanel
                    dao.setImageData(image);

                    // Don't force the user to export imported data, but don't change the state of SavedData
                    // Do not affect RawData, because Images have different containers
                    // So don't change the ChannelCount or Temperature flag

                    // If we get here, we have the image...
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_IMAGE
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_FILENAME + strFilename + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());

                    REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                    InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                    // Create the ResponseMessage
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_IMAGE
                                                            + METADATA_ACTION_IMPORT
                                                            + METADATA_RESULT + "Image filename is not valid" + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_READ_IMAGE + exception.getMessage(),
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            catch (Exception exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        // Construct INVALID_PARAMETER
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
