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
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * ExportInstrumentXML.
 */

public final class ExportInstrumentXML implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  ObservatoryConstants
    {
    /***********************************************************************************************
     * doExportInstrumentXML().
     * Saves the current Instrument XML at the specified location.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportInstrumentXML(final ObservatoryInstrumentDAOInterface dao,
                                                                 final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ExportInstrumentXML.exportInstrumentXML()";
        final int PARAMETER_COUNT = 2;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect two parameters, the filename and timestamp
        listParameters = commandType.getParameterList();
        responseMessage = null;

        // Do not change any DAO data containers!

        // Check the parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (listParameters.get(1) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(1).getInputDataType().getDataTypeName())))
            {
            final String strFilename;
            final boolean boolTimestamp;
            final boolean boolSuccess;

            strFilename = listParameters.get(0).getValue();
            boolTimestamp = Boolean.parseBoolean(listParameters.get(1).getValue());

            boolSuccess = DataExporter.exportInstrumentXML(strFilename,
                                                           boolTimestamp,
                                                           dao.getHostInstrument().getInstrument(),
                                                           dao.getEventLogFragment(),
                                                           dao.getObservatoryClock());
            if (boolSuccess)
                {
                // Create the ResponseMessage
                // Just feed the existing DaoData back round again
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                dao.setRawDataChannelCount(dao.getWrappedData().getRawDataChannelCount());
                dao.setTemperatureChannel(dao.getWrappedData().hasTemperatureChannel());

                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                    commandmessage,
                                                                                    commandType);
                }
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
