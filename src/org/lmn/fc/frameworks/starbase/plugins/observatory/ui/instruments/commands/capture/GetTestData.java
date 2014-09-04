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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture;


import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreHostMemoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusParsers;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * GetTestData.
 */

public final class GetTestData implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ObservatoryConstants
    {
    /***********************************************************************************************
     * getTestData().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface getTestData(final StaribusCoreHostMemoryInterface dao,
                                                       final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetTestData.getTestData()";
        final int PARAMETER_COUNT_MIN = 2;
        final int INDEX_FORMAT = 0;
        final int INDEX_FILTER = 1;
        Instrument xmlInstrument;
        XmlObject xmlDataCaptureModule;
        final CommandType cmdGetData;
        final List<ParameterType> listExecutionParameters;
        final ResponseMessageInterface responseMessage;
        int intCaptureChannelCount;
        final StringBuffer bufferExpression;
        StringBuffer bufferResult;
        final XmlObject[] selection;
        final List<DataTypeDictionary> listChannelDataTypes;
        final List<String> listTemperatureFlag;
        final List<String> errors;

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdGetData = (CommandType)commandmessage.getCommandType().copy();

        // We expect two parameters, the data format (only Staribus), and the data filter
        //listExecutionParameters = cmdGetData.getParameterList();
        listExecutionParameters = commandmessage.getExecutionParameters();

        // We haven't found anything yet...
        xmlInstrument = null;
        xmlDataCaptureModule = null;
        bufferExpression = new StringBuffer();
        bufferResult = new StringBuffer();
        listChannelDataTypes = new ArrayList<DataTypeDictionary>(10);
        listTemperatureFlag = new ArrayList<String>(1);
        errors = new ArrayList<String>(10);

        // The number of channels will be determined from the DataCapture Module Metadata
        intCaptureChannelCount = 0;

        // Make sure this Instrument has a DataCapture Module
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getController() != null))
            {
            xmlInstrument = dao.getHostInstrument().getInstrument();

            bufferExpression.setLength(0);
            bufferExpression.append(FrameworkXpath.XPATH_INSTRUMENTS_NAMESPACE);
            bufferExpression.append(FrameworkXpath.XPATH_PLUGIN_DATA_CAPTURE);

            // Query from the root of the Controller
            selection = xmlInstrument.getController().selectPath(bufferExpression.toString());

            if ((selection != null)
                && (selection instanceof PluginType[])
                && (selection.length == 1)
                && (selection[0] != null)
                && (selection[0] instanceof PluginType))
                {
                //LOGGER.debugTimedEvent("Data PluginType=" + selection[0].xmlText());
                xmlDataCaptureModule = selection[0];

                // Validate the Metadata from the DataCapture Module
                // and find the number of channels to process, with their DataTypes
                // The ConfigurationList is not modified, so no need to copy()
                intCaptureChannelCount = DataAnalyser.getCaptureChannelCount(((PluginType) xmlDataCaptureModule).getPluginMetadataList(),
                                                                             EMPTY_STRING,
                                                                             listChannelDataTypes,
                                                                             listTemperatureFlag);
                }
            }

        //         LOGGER.debugTimedEvent("Instrument =" + (xmlInstrument != null));
        //         LOGGER.debugTimedEvent("DataCapture Module =" + (xmlDataCaptureModule != null));
        //         LOGGER.debugTimedEvent("DECLARED CHANNEL COUNT=" + intChannelCount);
        //         LOGGER.debugTimedEvent("TEMPFLAG size= " + listTemperatureFlag.size());
        //         LOGGER.debugTimedEvent("Param 0= " + listParameters.get(0).getInputDataType().getDataTypeName());
        //         LOGGER.debugTimedEvent("Param 1= " + listParameters.get(1).getInputDataType().getDataTypeName());

        //------------------------------------------------------------------------------------------
        // Do the getStubData() operation, which expects a Response!
        // Create the stub data rather than repeatedly getting data blocks from the bus
        // Only proceed if there are correctly defined channels to capture...

        if ((listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
            && (listExecutionParameters.get(INDEX_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName()))
            && (intCaptureChannelCount > 0)
            && (listChannelDataTypes != null)
            && (intCaptureChannelCount == listChannelDataTypes.size())
            && (xmlInstrument != null)
            && (xmlDataCaptureModule != null))
            {
            try
                {
                final String strFormat;
                final String strFilter;
                final DataFormat dataFormat;
                final DataFilterType dataFilterType;

                strFormat = listExecutionParameters.get(INDEX_FORMAT).getValue();
                strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                dataFormat = DataFormat.getDataFormatForName(strFormat);

                // Map the filter entry to a FilterType (which must return not NULL)
                dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                if (dataFilterType != null)
                    {
                    final DataFilterInterface filter;

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
                    }

                // Make up some data...
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_CAPTURE
                                                   + METADATA_FORMAT + dataFormat.getName() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                bufferResult = StaribusParsers.createStubStaribusBlockData(listChannelDataTypes, intCaptureChannelCount);

                // We can only deal with Staribus in this DAO!
                if ((bufferResult.length() > 0)
                    && (DataFormat.STARIBUS.getName().equals(dataFormat.getName())))
                    {
                    // Parse the concatenated data Blocks into timestamped Vectors, setting RawData
                    // Return NULL if the parsing failed
                    dao.setRawData(StaribusParsers.parseStaribusBlocksIntoVector(bufferResult,
                                                                                 listChannelDataTypes,
                                                                                 intCaptureChannelCount,
                                                                                 listTemperatureFlag,
                                                                                 errors));
                    // TODO get errors and do something!

                    if (dao.getRawData() != null)
                        {
                        final boolean boolTemperature;

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FORMAT + dataFormat.getName() + TERMINATOR + SPACE
                                                           + METADATA_FILTERNAME + dataFilterType.getName() + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        // There are no MetadataMetadata
                        // These Metadata are valid for use as a complete Channel specification
                        // Add Metadata provided by the DataCapture Module
                        // Preserve the original ConfigurationList, because Metadata will be cleared later


                        // ALARM!!!!!
                        System.out.println("STARIBUS CORE DAO START SUSPECT");
                        dao.addAllMetadataToContainersTraced(((PluginType) xmlDataCaptureModule.copy()).getPluginMetadataList(),
                                                             SOURCE,
                                                             true);
                        //LOADER_PROPERTIES.isMetadataDebug());

                        // Did we have a Temperature channel?
                        boolTemperature = ((listTemperatureFlag != null)
                                           && (listTemperatureFlag.contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey())));

                        // Filter the RawData to produce the XYDataset
                        dao.setRawDataChannelCount(intCaptureChannelCount);
                        dao.setTemperatureChannel(boolTemperature);
                        dao.setUnsavedData(true);
                        System.out.println("STARIBUS CORE DAO END SUSPECT");
                        // ALARM!!!!!



                        DataFilterHelper.filterCalendarisedRawDataAndTemperature(dao,
                                                                                 dao.getFilter(),
                                                                                 listChannelDataTypes,
                                                                                 dao.getLocalHostname());
                        // Help the GC?
                        if (dao.getFilter() != null)
                            {
                            dao.getFilter().disposeFilter();
                            }

                        dao.setFilter(null);
                        dao.setTranslator(null);

                        ObservatoryInstrumentHelper.runGarbageCollector();

                        // Say we succeeded, even if the Filter failed,
                        // so the data are always visible in RawData
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                        }
                    else
                        {
                        // This must fail, because we can't interpret the data blocks
                        //                         getMetadataMetadata().clear();
                        //                         getObservationMetadata().clear();
                        dao.clearRawData();

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           ObservatoryInstrumentDAOInterface.ERROR_PARSE_DATA,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                        }
                    }
                else
                    {
                    // This must fail, because we can't interpret the data blocks
                    //                     getMetadataMetadata().clear();
                    //                     getObservationMetadata().clear();
                    // ToDo: REVIEW This must fail, because we can't interpret the data
                    //dao.clearData();

                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       ObservatoryInstrumentDAOInterface.ERROR_DATA_FORMAT,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                    }
                }

            catch (IllegalArgumentException exception)
                {
                // We can't parse the input parameters
                //                 getMetadataMetadata().clear();
                //                 getObservationMetadata().clear();
                dao.clearRawData();

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                // A generic Exception - this should never happen...
                //                 getMetadataMetadata().clear();
                //                 getObservationMetadata().clear();
                dao.clearRawData();

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]",
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                }
            }
        else
            {
            // Something is wrong with the XML definition of getStubData()
            //             getMetadataMetadata().clear();
            //             getObservationMetadata().clear();
            dao.clearRawData();

            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                    SOURCE,
                                                                                    METADATA_TARGET_RAWDATA,
                                                                                    METADATA_ACTION_CAPTURE));
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        //------------------------------------------------------------------------------------------
        // Finally construct the appropriate ResponseMessage which contains the DAOWrapper

        if (ResponseMessageStatus.wasResponseListSuccessful(dao.getResponseMessageStatusList()))
            {
            // Explicitly set the ResponseValue as the first timestamp only,
            // to save space in the CommandLog
            cmdGetData.getResponse().setValue(bufferResult.toString().substring(0, StaribusParsers.LENGTH_STARIBUS_TIMESTAMP));

            //LOGGER.debugTimedEvent("ResponseValue={" + cmdGetData.getResponse().getValue() + "}");

            dao.setRawDataChannelCount(intCaptureChannelCount);
            // DAO Temperature Channel has been set

            // Create the ResponseMessage - this creates a DAOWrapper containing the parsed data
            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      xmlInstrument,
                                                                      xmlDataCaptureModule,
                                                                      cmdGetData,
                                                                      AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdGetData));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           xmlInstrument,
                                                                           xmlDataCaptureModule,
                                                                           cmdGetData,
                                                                           AbstractResponseMessage.buildResponseResourceKey(xmlInstrument,
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdGetData));
            }

        return (responseMessage);
        }
    }
