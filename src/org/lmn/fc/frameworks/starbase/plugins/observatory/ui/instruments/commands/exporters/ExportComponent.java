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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataExporter;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * ExportComponent.
 */

public final class ExportComponent implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportComponent().
     * Saves a visual component at componentindex as an image at the specified location.
     * Optionally use the size entered by the user.
     *
     * @param dao
     * @param commandmessage
     * @param componentindex
     * @param usesize
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportComponent(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage,
                                                             final int componentindex,
                                                             final boolean usesize)
        {
        final String SOURCE = "ExportComponent.exportComponent()";
        final int PARAMETER_COUNT;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;
        final boolean boolSizeValid;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect five (or three) parameters, the filename, timestamp flag, type, [width and height]
        listParameters = commandType.getParameterList();
        responseMessage = null;

        // See if we are expecting any size parameters
        if (usesize)
            {
            PARAMETER_COUNT = 5;

            boolSizeValid = ((listParameters != null)
                                && (listParameters.size() == PARAMETER_COUNT)
                                && (listParameters.get(3) != null)
                                && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(3).getInputDataType().getDataTypeName()))
                                && (listParameters.get(4) != null)
                                && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(4).getInputDataType().getDataTypeName())));
            }
        else
            {
            PARAMETER_COUNT = 3;
            boolSizeValid = true;
            }

        // Do not change any DAO data containers!
        dao.clearEventLogFragment();

        // Check the parameters before continuing
        if ((listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (listParameters.get(1) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(1).getInputDataType().getDataTypeName()))
            && (listParameters.get(2) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(2).getInputDataType().getDataTypeName()))
            && (boolSizeValid)
            && (dao.getHostInstrument().getInstrumentPanel() != null));
            {
            try
                {
                final String strFilename;
                final boolean boolTimestamp;
                final String strType;
                final int intWidth;
                final int intHeight;
                final boolean boolSuccess;


                strFilename = listParameters.get(0).getValue();
                boolTimestamp = Boolean.parseBoolean(listParameters.get(1).getValue());
                strType = listParameters.get(2).getValue();

                // Read the size parameters if required
                if (usesize)
                    {
                    // Values supplied by the User. Width and Height are DECIMAL
                    intWidth = Integer.parseInt(listParameters.get(3).getValue());
                    intHeight = Integer.parseInt(listParameters.get(4).getValue());
                    }
                else
                    {
                    if (dao.getHostInstrument().getInstrumentPanel().getExportableComponent(componentindex) != null)
                        {
                        // These values will be used by DataExporter to set the size of the BufferedImage
                        intWidth = dao.getHostInstrument().getInstrumentPanel().getExportableComponent(componentindex).getWidth();
                        intHeight = dao.getHostInstrument().getInstrumentPanel().getExportableComponent(componentindex).getHeight();

                        if ((intWidth <= 0)
                            || (intHeight <= 0))
                            {
                            LOGGER.error(SOURCE + "ExportableComponent[" + componentindex + "] has an invalid size [width=" + intWidth + "] [height=" + intHeight + "]");
                            }
                        }
                    else
                        {
                        // This should never happen...
                        LOGGER.error(SOURCE + "ExportableComponent[" + componentindex + "] is NULL");
                        intWidth = 0;
                        intHeight = 0;
                        }
                    }

                // We know we have an Instrument and an InstrumentPanel!
                // The ExportableComponent could still be null
                boolSuccess = DataExporter.exportComponent(dao.getHostInstrument().getInstrumentPanel().getExportableComponent(componentindex),
                                                           strFilename,
                                                           boolTimestamp,
                                                           strType,
                                                           intWidth,
                                                           intHeight,
                                                           dao.getEventLogFragment(),
                                                           dao.getObservatoryClock());
                if (boolSuccess)
                    {
                    // Create the ResponseMessage
                    // Just feed the existing DaoData back round again
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

//                    dao.setRawDataChannelCount(dao.getWrappedData().getRawDataChannelCount());
//                    dao.setTemperatureChannel(dao.getWrappedData().hasTemperatureChannel());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                }

//            finally
//                {
//                // Regardless of the outcome, we can now remove the reference to the specified Component
//                dao.getHostInstrument().getInstrumentPanel().setExportableComponent(componentindex,
//                                                                                    new BlankExportableComponent());
//                }
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
