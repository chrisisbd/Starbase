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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.datasets;

import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
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

import java.util.List;


/***************************************************************************************************
 * AppendDataset.
 */

public final class AppendDataset implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ObservatoryConstants
    {
    /***********************************************************************************************
     * appendDataset().
     * Append the Dataset imported from a local file to the data already held by the DAO.
     *
     * @param commandmessage
     * @param dao
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doAppendDataset(final ObservatoryInstrumentDAOInterface dao,
                                                           final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "AppendDataset.appendDataset()";
        final int PARAMETER_COUNT_MIN = 3;
        final int INDEX_FILENAME = 0;
        final int INDEX_FORMAT = 1;
        final int INDEX_FILTER = 2;
        final CommandType commandType;
        final List<ParameterType> listExecutionParameters;
        ResponseMessageInterface responseMessage;
        final TimeSeriesCollection timeSeriesCollection;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect three parameters, the filename to download, format and the filter
        //listExecutionParameters = commandType.getParameterList();
        listExecutionParameters = commandmessage.getExecutionParameters();

        responseMessage = null;

        // Keep the existing Metadata and XYDataset, but allow new RawData
        dao.clearRawData();

        // ...and somewhere to put the combined data
        timeSeriesCollection = new TimeSeriesCollection();

        // Check the Command parameters before continuing to retrieve the data file
        if ((listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
            && (listExecutionParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listExecutionParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strFilename;
                final String strFormat;
                final String strFilter;
                final DataFormat dataFormat;
                final DataFilterType dataFilterType;

                strFilename = listExecutionParameters.get(INDEX_FILENAME).getValue();
                strFormat = listExecutionParameters.get(INDEX_FORMAT).getValue();
                strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                dataFormat = DataFormat.getDataFormatForName(strFormat);

                // Map the filter entry to a FilterType
                dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "[filename=" + strFilename + "] [format=" + dataFormat.getName() + "]");

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_APPEND
                                                       + METADATA_FILENAME + strFilename + TERMINATOR + SPACE
                                                       + METADATA_FORMAT + strFormat + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                if ((strFilename != null)
                    && (!EMPTY_STRING.equals(strFilename))
                    && (dataFilterType != null))
                    {
                    final DataTranslatorInterface translator;
                    final DataFilterInterface filter;

                    // Instantiate the translator required by the DataFormat
                    translator = DataTranslatorHelper.instantiateTranslator(dataFormat.getTranslatorClassname());

                    // Instantiate the filter required by the DataFilterType
                    filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                    if (filter != null)
                        {
                        filter.initialiseFilter();
                        DataFilterHelper.applyFilterParameters(filter,
                                                               commandmessage.getExecutionParameters(),
                                                               INDEX_FILTER);

                        // All subsequent access to the Filter must be via the DAO
                        dao.setFilter(filter);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Unable to instantiate the DataFilter [name=" + dataFilterType.getName() + "]");
                        dao.setFilter(null);
                        }

                    if (translator != null)
                        {
                        // Set the translator for this DAO (until changed by another command)
                        dao.setTranslator(translator);
                        dao.getTranslator().initialiseTranslator();

                        // Do not change any existing Metadata!

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_RAWDATA
                                                               + METADATA_ACTION_TRANSLATING
                                                               + METADATA_FILENAME + strFilename + TERMINATOR + SPACE
                                                               + METADATA_FORMAT + strFormat + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());

                        // Make a dataset from the file and send to the Chart and Data Reports...
                        if (dao.getTranslator().importRawData(strFilename,
                                                              dao.getEventLogFragment(),
                                                              dao.getObservatoryClock()))
                            {
                            // We now have new:
                            // MetadataMetadata
                            // Metadata
                            // RawDataMetadata
                            // RawData
                            // RawDataChannelCount
                            // ImportedCount

//                            setMetadataMetadata(getTranslator().getMetadataMetadata());
//                            setEditedMetadata(getTranslator().getMetadata());
//
//                            setRawDataMetadata(getTranslator().getRawDataMetadata());
//                            setRawData(getTranslator().getRawData());

//                            getTranslator().getRawDataChannelCount();
//                              getTranslator().getImportedCount();

                            //System.out.println("APPEND DATASET!!!!!!!!!!!!!!!!!!!");
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_RAWDATA
                                                                    + METADATA_ACTION_APPEND
                                                                    + METADATA_RESULT + "DAO has no data" + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_RAWDATA
                                                                + METADATA_ACTION_APPEND
                                                                + METADATA_RESULT + "Unable to instantiate translator" + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_RAWDATA
                                                            + METADATA_ACTION_APPEND
                                                            + METADATA_RESULT + "Append filename is not valid" + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                // Were we successful in getting some new data?
                if ((timeSeriesCollection != null)
                    && (timeSeriesCollection.getSeriesCount() > 0))
                    {
                    // Tell the DAO about the TimeSeries
                    dao.setXYDataset(timeSeriesCollection);

                    dao.setRawDataChannelCount(dao.getTranslator().getRawDataChannelCount());
                    dao.setTemperatureChannel(dao.getTranslator().hasTemperatureChannel());

                    // Copy over all metadata from the Translator to the DAO
                    DataTranslatorHelper.copyMetadataFromTranslator(dao.getTranslator(),
                                                                    REGISTRY.getFramework(),
                                                                    dao,
                                                                    LOADER_PROPERTIES.isMetadataDebug());

                    REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                    InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                    // Create the ResponseMessage
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
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
                                                   EventStatus.WARNING,
                                                   dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }
            }

        // Help the GC?
        if (dao.getFilter() != null)
            {
            dao.getFilter().disposeFilter();
            }

        dao.setFilter(null);
        dao.setTranslator(null);

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

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
