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

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * ApplyFilter.
 */

public final class ApplyFilter implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ObservatoryConstants
    {
    // String Resources
    private static final String MSG_NO_DATA = "No data returned by the Filter";


    /***********************************************************************************************
     * doApplyFilter().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doApplyFilter(final ObservatoryInstrumentDAOInterface dao,
                                                         final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ApplyFilter.doApplyFilter() ";
        final int PARAMETER_COUNT_MIN = 3;
        final int INDEX_FILTER = 0;
        final List<ParameterType> listExecutionParameters;
        final CommandType cmdApplyFilter;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(dao.isDebugMode(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdApplyFilter = (CommandType)commandmessage.getCommandType().copy();

        // Parameters entered on the UI, or via a Macro
        listExecutionParameters = commandmessage.getExecutionParameters();

        // Do not affect any data containers, channel count, or temperature indicator
        // Expect three Parameters:
        // Filter.Name          String
        // ApplyTo.ChannelID    DecimalInteger  0...n or -1
        // ApplyTo.Dataset      String RawData,ProcessedData

        // We can only check as far as the Filter, because there may be a variable number of Parameters

        if ((listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
            && (listExecutionParameters.get(INDEX_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName())))
            {
            // Check that we have some data to Filter....
            if ((dao.getHostInstrument() != null)
                && (dao.getRawData() != null)
                && (!dao.getRawData().isEmpty())
                && (dao.getRawDataChannelCount() > 0))
                {
                try
                    {
                    final String strFilter;
                    final DataFilterType dataFilterType;

                    int intApplyToChannelID;
                    String strApplyToDataset;

                    // Some safe defaults, but again, this should never happen!
                    intApplyToChannelID = -1;
                    strApplyToDataset = DATA_PROCESSED_DATA;

                    strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                    // Map the filter entry to a FilterType
                    dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                    if (dataFilterType != null)
                        {
                        final DataFilterInterface filter;

                        // Instantiate the filter required by the DataFilterType (which must return not NULL)
                        filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                        if (filter != null)
                            {
                            filter.initialiseFilter();
                            DataFilterHelper.applyFilterParameters(filter,
                                                                   commandmessage.getExecutionParameters(),
                                                                   INDEX_FILTER);

                            // All subsequent access to the Filter must be via the DAO
                            dao.setFilter(filter);

                            // Parse the remaining Parameters now we know the number of Filter Parameters
                            if (listExecutionParameters.size() == (PARAMETER_COUNT_MIN + filter.getParameterCount()))
                                {
                                final int intNextParameterIndex;

                                intNextParameterIndex = INDEX_FILTER + filter.getParameterCount() + 1;
                                intApplyToChannelID = Integer.parseInt(listExecutionParameters.get(intNextParameterIndex).getValue());
                                strApplyToDataset = listExecutionParameters.get(intNextParameterIndex + 1).getValue();
                                }

                            // Check that the ChannelID is in range
                            // The ChannelCount includes the Temperature Channel, if present
                            if ((intApplyToChannelID < -1)
                                || (intApplyToChannelID >= dao.getWrappedData().getRawDataChannelCount()))
                                {
                                throw new IndexOutOfBoundsException(MSG_CHANNEL_RANGE);
                                }
                            }
                        else
                            {
                            // With no Filter, there's nothing to do
                            dao.setFilter(null);

                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.FATAL,
                                                               METADATA_ACTION_FILTERING
                                                                   + METADATA_RESULT
                                                                   + "Unable to instantiate the DataFilter [name=" + dataFilterType.getName() + "]"
                                                                   + TERMINATOR,
                                                               SOURCE,
                                                               dao.getObservatoryClock());
                            }
                        }
                    else
                        {
                        // This should never happen!
                        dao.setFilter(null);

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.FATAL,
                                                           METADATA_ACTION_FILTERING
                                                               + METADATA_RESULT
                                                               + "Invalid DataFilterType"
                                                               + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        }

                    //---------------------------------------------------------------------------------
                    // Proceed to do the Filter operation if we are able

                    if (dao.getFilter() != null)
                        {
                        final List<Metadata> listMetadata;

                        // Gather the Metadata that define what we are filtering
                        listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                     (ObservatoryInterface) dao.getHostInstrument().getHostAtom(),
                                                                                     dao.getHostInstrument(),
                                                                                     dao, null,
                                                                                     SOURCE,
                                                                                     dao.isDebugMode());
                        if (DATA_RAW_DATA.equals(strApplyToDataset))
                            {
                            dao.getResponseMessageStatusList().add(filterRawData(dao,
                                                                                 listMetadata,
                                                                                 SOURCE,
                                                                                 intApplyToChannelID,
                                                                                 dao.getEventLogFragment(),
                                                                                 dao.getObservatoryClock()));
                            // Force the Chart to be generated, even if not visible
                            refreshChart(dao);

                            // Create the SUCCESS ResponseMessage
                            strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }
                        else if (DATA_PROCESSED_DATA.equals(strApplyToDataset))
                            {
                            dao.getResponseMessageStatusList().add(filterProcessedData(dao,
                                                                                       listMetadata,
                                                                                       SOURCE,
                                                                                       intApplyToChannelID,
                                                                                       dao.getEventLogFragment(),
                                                                                       dao.getObservatoryClock()));
                            // Force the Chart to be generated, even if not visible
                            refreshChart(dao);

                            // Create the SUCCESS ResponseMessage
                            strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }
                        else
                            {
                            // No other Dataset is acceptable
                            // The XML configuration was inappropriate
                            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
                            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                  SOURCE,
                                                                                                  METADATA_TARGET_DATASET,
                                                                                                  METADATA_ACTION_FILTERING));
                            }
                        }
                    else
                        {
                        // The reason for failure was logged above
                        strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                        }
                    }

                // This should have been trapped by Regex
                catch (NumberFormatException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_ACTION_FILTERING
                                                        + METADATA_RESULT
                                                           + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }

                catch (IndexOutOfBoundsException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       METADATA_ACTION_FILTERING
                                                        + METADATA_RESULT
                                                           + MSG_CHANNEL_RANGE
                                                           + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_ACTION_FILTERING
                                                    + METADATA_RESULT
                                                       + "No Data"
                                                       + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                  SOURCE,
                                                                                  METADATA_TARGET_DATASET,
                                                                                  METADATA_ACTION_FILTERING));
            }

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdApplyFilter,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Filter Raw Data.
     * This assumes a valid initialised Filter instance is held by the DAO.
     *
     * @param dao
     * @param metadatalist
     * @param SOURCE
     * @param channelid
     * @param log
     * @param clock
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus filterRawData(final ObservatoryInstrumentDAOInterface dao,
                                                       final List<Metadata> metadatalist,
                                                       final String SOURCE,
                                                       final int channelid,
                                                       final Vector<Vector> log,
                                                       final ObservatoryClockInterface clock)
        {
        final ResponseMessageStatus responseMessageStatus;
        final String strApplicableChannel;

        strApplicableChannel = DAOCommandHelper.getApplicableChannelID(channelid);

        // Are the RawData Indexed or Calendarised?
        // There must be one Calendar and ChannelCount samples in the Vector...
        // OR <x-axis> <Channel0> <Channel1> <Channel2> ...
        // Also check that this Filter may be used with this DatasetType

        if ((DataAnalyser.isCalendarisedRawData(dao.getRawData()))
            &&(dao.getFilter().getDatasetTypes().contains(DatasetType.TIMESTAMPED)))
            {
            // If ChannelID is -1, apply the Filter to all channels
            if (channelid == -1)
                {
                final TimeSeriesCollection timeSeriesCollection;

                // Somewhere to put the filtered data
                timeSeriesCollection = new TimeSeriesCollection();

                for (int intChannelIndex = 0;
                    ((dao.getRawDataChannelCount() > 0)
                     && (intChannelIndex < dao.getRawDataChannelCount()));
                    intChannelIndex++)
                    {
                    final TimeSeries timeSeries;

                    // The output TimeSeries may be of length different from the input, depending on the operation performed
                    timeSeries = dao.getFilter().filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                                   intChannelIndex,
                                                                                   MetadataHelper.getChannelName(metadatalist,
                                                                                                                 intChannelIndex,
                                                                                                                 dao.hasTemperatureChannel()),
                                                                                   log,
                                                                                   clock);
                    if ((timeSeries != null)
                        && (timeSeries.getItemCount() > 0))
                        {
                        timeSeriesCollection.addSeries(timeSeries);
                        dao.setProcessedDataChanged(true);
                        }
                    }

                // ToDo Did we get the correct number of Series?
                if (timeSeriesCollection.getSeriesCount() > 0)
                    {
                    // Tell the DAO about the new TimeSeriesCollection
                    dao.setXYDataset(timeSeriesCollection);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    // Filtering failed for some reason
                    dao.setXYDataset(null);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            else
                {
                final TimeSeries timeSeries;

                // Apply the Filter to a single channel
                // The output TimeSeries may be of length different from the input, depending on the operation performed
                timeSeries = dao.getFilter().filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                               channelid,
                                                                               MetadataHelper.getChannelName(metadatalist,
                                                                                                             channelid,
                                                                                                             dao.hasTemperatureChannel()),
                                                                               log,
                                                                               clock);

                // Replace the existing TimeSeries with the new, filtered version
                // On failure, leave the existing series alone
                if ((timeSeries != null)
                    && (timeSeries.getItemCount() > 0)
                    && (dao.getXYDataset() instanceof TimeSeriesCollection)
                    && (dao.getXYDataset().getSeriesCount() == dao.getRawDataChannelCount()))
                    {
                    final List<TimeSeries> listSeries;

                    listSeries = ((TimeSeriesCollection)dao.getXYDataset()).getSeries();

                    if ((listSeries != null)
                        && (listSeries.size() == dao.getRawDataChannelCount()))
                        {
                        final TimeSeries timeSeriesOriginal;
                        final List<TimeSeriesDataItem> listFilteredItems;

                        // The JFreeChart List is an unmodifiable view, so we can't just replace the Series...
                        timeSeriesOriginal = listSeries.get(channelid);

                        // ...so clear the original, and add the new items to it
                        timeSeriesOriginal.clear();

                        // Add each item one at a time
                        listFilteredItems = timeSeries.getItems();

                        for (int intItemIndex = 0;
                             intItemIndex < listFilteredItems.size();
                             intItemIndex++)
                            {
                            final TimeSeriesDataItem item;

                            item = listFilteredItems.get(intItemIndex);

                            timeSeriesOriginal.add(item);
                            }
                        }

                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            }

        else if ((DataAnalyser.isColumnarRawData(dao.getWrappedData().getRawData()))
             &&(dao.getFilter().getDatasetTypes().contains(DatasetType.XY)))
            {
            // If ChannelID is -1, apply the Filter to all channels
            if (channelid == -1)
                {
                final XYSeriesCollection xySeriesCollection;

                // Somewhere to put the filtered data
                xySeriesCollection = new XYSeriesCollection();

                for (int intChannelIndex = 0;
                     ((dao.getRawDataChannelCount() > 0)
                      && (intChannelIndex < dao.getRawDataChannelCount()));
                     intChannelIndex++)
                    {
                    final XYSeries xySeries;

                    // The output XYSeries may be of length different from the input, depending on the operation performed
                    xySeries = dao.getFilter().filterIndexedListToXYSeries(dao.getRawData(),
                                                                           intChannelIndex,
                                                                           MetadataHelper.getChannelName(metadatalist,
                                                                                                         intChannelIndex,
                                                                                                         dao.hasTemperatureChannel()),
                                                                           log,
                                                                           clock);
                    if ((xySeries != null)
                        && (xySeries.getItemCount() > 0))
                        {
                        xySeriesCollection.addSeries(xySeries);
                        dao.setProcessedDataChanged(true);
                        }
                    }

                // ToDo Did we get the correct number of Series?
                if (xySeriesCollection.getSeriesCount() > 0)
                    {
                    // Tell the DAO about the new XYSeriesCollection
                    dao.setXYDataset(xySeriesCollection);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    // Filtering failed for some reason
                    dao.setXYDataset(null);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            else
                {
                final XYSeries xySeries;

                // Apply the Filter to a single channel
                // The output XYSeries may be of length different from the input, depending on the operation performed
                xySeries = dao.getFilter().filterIndexedListToXYSeries(dao.getRawData(),
                                                                       channelid,
                                                                       MetadataHelper.getChannelName(metadatalist,
                                                                                                     channelid,
                                                                                                     dao.hasTemperatureChannel()),
                                                                       log,
                                                                       clock);

                // Replace the existing XYSeries with the new, filtered version
                // On failure, leave the existing series alone
                if ((xySeries != null)
                    && (xySeries.getItemCount() > 0)
                    && (dao.getXYDataset() instanceof XYSeriesCollection)
                    && (dao.getXYDataset().getSeriesCount() == dao.getRawDataChannelCount()))
                    {
                    final List<XYSeries> listXYSeries;

                    listXYSeries = ((XYSeriesCollection)dao.getXYDataset()).getSeries();

                    if ((listXYSeries != null)
                        && (listXYSeries.size() == dao.getRawDataChannelCount()))
                        {
                        final XYSeries xySeriesOriginal;
                        final List<XYDataItem> listFilteredItems;

                        // The JFreeChart List is an unmodifiable view, so we can't just replace the Series...
                        xySeriesOriginal = listXYSeries.get(channelid);

                        // ...so clear the original, and add the new items to it
                        xySeriesOriginal.clear();

                        // Add each item one at a time
                        listFilteredItems = xySeries.getItems();

                        for (int intItemIndex = 0;
                             intItemIndex < listFilteredItems.size();
                             intItemIndex++)
                            {
                            final XYDataItem item;

                            item = listFilteredItems.get(intItemIndex);

                            xySeriesOriginal.add(item);
                            }
                        }

                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            }
        else
            {
            // We don't understand the data format or type of Data Filter
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET + DATA_RAW_DATA + TERMINATOR
                                                + METADATA_ACTION_FILTERING
                                                + METADATA_RESULT + MSG_UNSUPPORTED_FORMAT + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Filter Processed Data.
     * This assumes a valid Filter instance is held by the DAO.
     *
     * @param dao
     * @param metadatalist
     * @param SOURCE
     * @param channelid
     * @param log
     * @param clock
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus filterProcessedData(final ObservatoryInstrumentDAOInterface dao,
                                                             final List<Metadata> metadatalist,
                                                             final String SOURCE,
                                                             final int channelid,
                                                             final Vector<Vector> log,
                                                             final ObservatoryClockInterface clock)
        {
        final ResponseMessageStatus responseMessageStatus;
        final String strApplicableChannel;

        strApplicableChannel = DAOCommandHelper.getApplicableChannelID(channelid);

        // Is the XYDataset a TimeSeriesCollection or an XYSeriesCollection?
        // Also check that this Filter may be used with this DatasetType

        if ((DataAnalyser.isTimeSeriesProcessedData(dao.getXYDataset()))
            &&(dao.getFilter().getDatasetTypes().contains(DatasetType.TIMESTAMPED)))
            {
            // If ChannelID is -1, apply the Filter to all channels
            if (channelid == -1)
                {
                final TimeSeriesCollection timeSeriesCollection;

                // Somewhere to put the filtered data
                timeSeriesCollection = new TimeSeriesCollection();

                for (int intChannelIndex = 0;
                     ((dao.getRawDataChannelCount() > 0)
                      && (intChannelIndex < dao.getRawDataChannelCount()));
                     intChannelIndex++)
                    {
                    final TimeSeries timeSeries;

                    // The output TimeSeries may be of length different from the input, depending on the operation performed
                    timeSeries = dao.getFilter().filterTimeSeries((TimeSeriesCollection) dao.getXYDataset(),
                                                                  intChannelIndex,
                                                                  MetadataHelper.getChannelName(metadatalist,
                                                                                                intChannelIndex,
                                                                                                dao.hasTemperatureChannel()),
                                                                  log,
                                                                  clock);
                    if ((timeSeries != null)
                        && (timeSeries.getItemCount() > 0))
                        {
                        timeSeriesCollection.addSeries(timeSeries);
                        dao.setProcessedDataChanged(true);
                        }
                    }

                // ToDo Did we get the correct number of Series?
                if (timeSeriesCollection.getSeriesCount() > 0)
                    {
                    // Tell the DAO about the new TimeSeriesCollection
                    dao.setXYDataset(timeSeriesCollection);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    // Filtering failed for some reason
                    dao.setXYDataset(null);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            else
                {
                final TimeSeries timeSeries;

                // Apply the Filter to a single channel
                // The output TimeSeries may be of length different from the input, depending on the operation performed
                timeSeries = dao.getFilter().filterTimeSeries((TimeSeriesCollection) dao.getXYDataset(),
                                                              channelid,
                                                              MetadataHelper.getChannelName(metadatalist,
                                                                                            channelid,
                                                                                            dao.hasTemperatureChannel()),
                                                              log,
                                                              clock);

                // Replace the existing TimeSeries with the new, filtered version
                // On failure, leave the existing series alone
                if ((timeSeries != null)
                    && (timeSeries.getItemCount() > 0)
                    && (dao.getXYDataset() instanceof TimeSeriesCollection)
                    && (dao.getXYDataset().getSeriesCount() == dao.getRawDataChannelCount()))
                    {
                    final List<TimeSeries> listSeries;

                    listSeries = ((TimeSeriesCollection)dao.getXYDataset()).getSeries();

                    if ((listSeries != null)
                        && (listSeries.size() == dao.getRawDataChannelCount()))
                        {
                        final TimeSeries timeSeriesOriginal;
                        final List<TimeSeriesDataItem> listFilteredItems;

                        // The JFreeChart List is an unmodifiable view, so we can't just replace the Series...
                        timeSeriesOriginal = listSeries.get(channelid);

                        // ...so clear the original, and add the new items to it
                        timeSeriesOriginal.clear();

                        // Add each item one at a time
                        listFilteredItems = timeSeries.getItems();

                        for (int intItemIndex = 0;
                             intItemIndex < listFilteredItems.size();
                             intItemIndex++)
                            {
                            final TimeSeriesDataItem item;

                            item = listFilteredItems.get(intItemIndex);

                            timeSeriesOriginal.add(item);
                            }
                        }

                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            }

        else if ((DataAnalyser.isXYSeriesProcessedData(dao.getXYDataset()))
                 &&(dao.getFilter().getDatasetTypes().contains(DatasetType.XY)))
            {
            // If ChannelID is -1, apply the Filter to all channels
            if (channelid == -1)
                {
                final XYSeriesCollection xySeriesCollection;

                // Somewhere to put the filtered data
                xySeriesCollection = new XYSeriesCollection();

                for (int intChannelIndex = 0;
                     ((dao.getRawDataChannelCount() > 0)
                      && (intChannelIndex < dao.getRawDataChannelCount()));
                     intChannelIndex++)
                    {
                    final XYSeries xySeries;

                    // The output XYSeries may be of length different from the input, depending on the operation performed
                    xySeries = dao.getFilter().filterXYSeries((XYSeriesCollection) dao.getXYDataset(),
                                                              intChannelIndex,
                                                              MetadataHelper.getChannelName(metadatalist,
                                                                                            intChannelIndex,
                                                                                            dao.hasTemperatureChannel()),
                                                              log,
                                                              clock);
                    if ((xySeries != null)
                        && (xySeries.getItemCount() > 0))
                        {
                        xySeriesCollection.addSeries(xySeries);
                        dao.setProcessedDataChanged(true);
                        }
                    }

                // ToDo Did we get the correct number of Series?
                if (xySeriesCollection.getSeriesCount() > 0)
                    {
                    // Tell the DAO about the new XYSeriesCollection
                    dao.setXYDataset(xySeriesCollection);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    // Filtering failed for some reason
                    dao.setXYDataset(null);
                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            else
                {
                final XYSeries xySeries;

                // Apply the Filter to a single channel
                // The output XYSeries may be of length different from the input, depending on the operation performed
                xySeries = dao.getFilter().filterXYSeries((XYSeriesCollection) dao.getXYDataset(),
                                                          channelid,
                                                          MetadataHelper.getChannelName(metadatalist,
                                                                                        channelid,
                                                                                        dao.hasTemperatureChannel()),
                                                          log,
                                                          clock);

                // Replace the existing XYSeries with the new, filtered version
                // On failure, leave the existing series alone
                if ((xySeries != null)
                    && (xySeries.getItemCount() > 0)
                    && (dao.getXYDataset() instanceof XYSeriesCollection)
                    && (dao.getXYDataset().getSeriesCount() == dao.getRawDataChannelCount()))
                    {
                    final List<XYSeries> listXYSeries;

                    listXYSeries = ((XYSeriesCollection)dao.getXYDataset()).getSeries();

                    if ((listXYSeries != null)
                        && (listXYSeries.size() == dao.getRawDataChannelCount()))
                        {
                        final XYSeries xySeriesOriginal;
                        final List<XYDataItem> listFilteredItems;

                        // The JFreeChart List is an unmodifiable view, so we can't just replace the Series...
                        xySeriesOriginal = listXYSeries.get(channelid);

                        // ...so clear the original, and add the new items to it
                        xySeriesOriginal.clear();

                        // Add each item one at a time
                        listFilteredItems = xySeries.getItems();

                        for (int intItemIndex = 0;
                             intItemIndex < listFilteredItems.size();
                             intItemIndex++)
                            {
                            final XYDataItem item;

                            item = listFilteredItems.get(intItemIndex);

                            xySeriesOriginal.add(item);
                            }
                        }

                    dao.setProcessedDataChanged(true);
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.SUCCESS;
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR_SPACE
                                                           + METADATA_CHANNEL + strApplicableChannel + TERMINATOR_SPACE
                                                           + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            }
        else
            {
            // We don't understand the data format or type of Data Filter
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                   + METADATA_ACTION_FILTERING
                                                   + METADATA_RESULT + MSG_UNSUPPORTED_FORMAT + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Refresh the ProcessedData Chart.
     *
     * @param dao
     */

    private static void refreshChart(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "ApplyFilter.refreshChart() ";

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrumentPanel() != null)
            && (dao.getHostInstrument().getInstrumentPanel().getChartTab() != null)
            && (dao.getHostInstrument().getInstrumentPanel().getChartTab() instanceof ChartUIComponentPlugin))
            {
            // Force the Chart to be generated, even if not visible
            ((ChartUIComponentPlugin) dao.getHostInstrument().getInstrumentPanel().getChartTab()).refreshChart(dao, true, SOURCE);
            }
        }
    }
