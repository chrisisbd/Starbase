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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * ApplyLinearTransform.
 */

public final class ApplyLinearTransform implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   ObservatoryConstants
    {
    /***********************************************************************************************
     * doApplyLinearTransform.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doApplyLinearTransform(final ObservatoryInstrumentDAOInterface dao,
                                                                  final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ApplyLinearTransform.doApplyLinearTransform() ";
        final int PARAMETER_COUNT = 4;
        final List<ParameterType> listParameters;
        final CommandType cmdApplyTransform;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdApplyTransform = (CommandType)commandmessage.getCommandType().copy();

        // Parameters
        listParameters = cmdApplyTransform.getParameterList();

        // Do not affect any data containers, channel count, or temperature indicator
        // Expect four Parameters:
        //
        // Linear.ScaleFactor   DecimalFloat
        // Linear.Offset        DecimalFloat
        // ApplyTo.ChannelID    DecimalInteger  0...n or -1
        // ApplyTo.Dataset      String RawData,ProcessedData

        if ((dao.getHostInstrument() != null)
            && (dao.getWrappedData() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(0) != null)
            && (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (listParameters.get(1) != null)
            && (SchemaDataType.DECIMAL_FLOAT.equals(listParameters.get(1).getInputDataType().getDataTypeName()))
            && (listParameters.get(2) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(2).getInputDataType().getDataTypeName()))
            && (listParameters.get(3) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(3).getInputDataType().getDataTypeName())))
            {
            try
                {
                final double dblLinearScaleFactor;
                final double dblLinearOffset;
                final int intApplyToChannelID;
                final String strApplyToDataset;
                final String strApplicableChannel;

                dblLinearScaleFactor = Double.parseDouble(listParameters.get(0).getValue());
                dblLinearOffset = Double.parseDouble(listParameters.get(1).getValue());
                intApplyToChannelID = Integer.parseInt(listParameters.get(2).getValue());
                strApplyToDataset = listParameters.get(3).getValue();

                if (DATA_RAW_DATA.equals(strApplyToDataset))
                    {
                    // Apply to RawData
                    if ((dao.getWrappedData() != null)
                        && (dao.getWrappedData().getRawData() != null)
                        && (!dao.getWrappedData().getRawData().isEmpty())
                        && (dao.getWrappedData().getRawDataChannelCount() > 0))
                        {
                        // The ChannelCount includes the Temperature Channel, if present
                        if ((intApplyToChannelID < -1)
                            || (intApplyToChannelID >= dao.getWrappedData().getRawDataChannelCount()))
                            {
                            throw new IndexOutOfBoundsException(MSG_CHANNEL_RANGE);
                            }

                        strApplicableChannel = DAOCommandHelper.getApplicableChannelID(intApplyToChannelID);

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_ACTION_TRANSFORM
                                                                + METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                                + METADATA_SCALE_FACTOR + dblLinearScaleFactor + TERMINATOR_SPACE
                                                                + METADATA_OFFSET + dblLinearOffset + TERMINATOR_SPACE
                                                                + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());

                        // Is RawData Indexed or Calendarised?
                        // There must be one Calendar and ChannelCount samples in the Vector...
                        // OR <x-axis> <Channel0> <Channel1> <Channel2> ...

                        if (DataAnalyser.isCalendarisedRawData(dao.getWrappedData().getRawData()))
                            {
                            final Iterator iterRows;

                            iterRows = dao.getWrappedData().getRawData().iterator();

                            while (iterRows.hasNext())
                                {
                                final Vector vecRawDataRow;

                                // The internal Data format is a Vector of Objects
                                vecRawDataRow = (Vector) iterRows.next();

                                if ((vecRawDataRow != null)
                                    && (vecRawDataRow.get(0) != null))
                                    {
                                    if (intApplyToChannelID == -1)
                                        {
                                        // Process all channels
                                        for (int intChannelIndex = 0;
                                             ((dao.getWrappedData().getRawDataChannelCount() > 0) && (intChannelIndex < dao.getWrappedData().getRawDataChannelCount()));
                                             intChannelIndex++)
                                            {
                                            final Object objRawData;

                                            objRawData = vecRawDataRow.get(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intChannelIndex);

                                            if (objRawData != null)
                                                {
                                                if (objRawData instanceof Double)
                                                    {
                                                    // Step over the Calendar
                                                    vecRawDataRow.set(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intChannelIndex,
                                                                      (((Double)objRawData) * dblLinearScaleFactor) + dblLinearOffset);
                                                    }
                                                else if (objRawData instanceof Integer)
                                                    {
                                                    // Step over the Calendar
                                                    vecRawDataRow.set(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intChannelIndex,
                                                                      (((Integer)objRawData).doubleValue() * dblLinearScaleFactor) + dblLinearOffset);
                                                    }
                                                }
                                            else
                                                {
                                                LOGGER.error(SOURCE + MSG_UNSUPPORTED_FORMAT);
                                                }
                                            }
                                        }
                                    else
                                        {
                                        final Object objRawData;

                                        // Just process a single channel
                                        objRawData = vecRawDataRow.get(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intApplyToChannelID);

                                        if (objRawData instanceof Double)
                                            {
                                            // Step over the Calendar
                                            vecRawDataRow.set(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intApplyToChannelID,
                                                              (((Double)objRawData) * dblLinearScaleFactor) + dblLinearOffset);
                                            }
                                        else if (objRawData instanceof Integer)
                                            {
                                            // Step over the Calendar
                                            vecRawDataRow.set(DataTranslatorInterface.INDEX_TIMESTAMPED_DATA + intApplyToChannelID,
                                                              (((Integer)objRawData).doubleValue() * dblLinearScaleFactor) + dblLinearOffset);
                                            }
                                        else
                                            {
                                            LOGGER.error(SOURCE + MSG_UNSUPPORTED_FORMAT);
                                            }
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + MSG_INVALID_TIMESTAMPED_RAWDATA);
                                    }
                                }

                            // Update ProcessedData and the Chart
                            DataFilterHelper.passThroughRawDataToXYDataset(dao);

                            // Now pass the DAO data to the host Instrument
                            // This is responsible for updating Instrument panels and EventLogs, for instance
                            // Only refresh the data if visible
                            dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, false);
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }
                        else if (DataAnalyser.isColumnarRawData(dao.getWrappedData().getRawData()))
                            {
                            final Iterator iterRows;

                            iterRows = dao.getWrappedData().getRawData().iterator();

                            while (iterRows.hasNext())
                                {
                                final Vector vecRawDataRow;

                                // The internal Data format is a Vector of Objects
                                vecRawDataRow = (Vector) iterRows.next();

                                if ((vecRawDataRow != null)
                                    && (vecRawDataRow.get(0) != null))
                                    {
                                    if (intApplyToChannelID == -1)
                                        {
                                        // Process all channels
                                        for (int intChannelIndex = 0;
                                             ((dao.getWrappedData().getRawDataChannelCount() > 0) && (intChannelIndex < dao.getWrappedData().getRawDataChannelCount()));
                                             intChannelIndex++)
                                            {
                                            final Object objRawData;

                                            objRawData = vecRawDataRow.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex);

                                            if (objRawData instanceof Double)
                                                {
                                                // Step over the Calendar
                                                vecRawDataRow.set(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex,
                                                                  (((Double)objRawData) * dblLinearScaleFactor) + dblLinearOffset);
                                                }
                                            else if (objRawData instanceof Integer)
                                                {
                                                // Step over the Calendar
                                                vecRawDataRow.set(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex,
                                                                  (((Integer)objRawData).doubleValue() * dblLinearScaleFactor) + dblLinearOffset);
                                                }
                                            else
                                                {
                                                LOGGER.error(SOURCE + MSG_UNSUPPORTED_FORMAT);
                                                }
                                            }
                                        }
                                    else
                                        {
                                        final Object objRawData;

                                        // Just process a single channel
                                        objRawData = vecRawDataRow.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intApplyToChannelID);

                                        if (objRawData instanceof Double)
                                            {
                                            // Step over the Calendar
                                            vecRawDataRow.set(DataTranslatorInterface.INDEX_INDEXED_DATA + intApplyToChannelID,
                                                              (((Double)objRawData) * dblLinearScaleFactor) + dblLinearOffset);
                                            }
                                        else if (objRawData instanceof Integer)
                                            {
                                            // Step over the Calendar
                                            vecRawDataRow.set(DataTranslatorInterface.INDEX_INDEXED_DATA + intApplyToChannelID,
                                                              (((Integer)objRawData).doubleValue() * dblLinearScaleFactor) + dblLinearOffset);
                                            }
                                        else
                                            {
                                            LOGGER.error(SOURCE + MSG_UNSUPPORTED_FORMAT);
                                            }
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + MSG_INVALID_INDEXED_RAWDATA);
                                    }
                                }

                            // Update ProcessedData and the Chart
                            DataFilterHelper.passThroughRawDataToXYDataset(dao);

                            // Now pass the DAO data to the host Instrument
                            // This is responsible for updating Instrument panels and EventLogs, for instance
                            // Only refresh the data if visible
                            dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, false);
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }
                        else
                            {
                            // We don't understand the data format
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_ACTION_TRANSFORM
                                                                    + METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                                    + METADATA_RESULT + MSG_UNSUPPORTED_FORMAT + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_ACTION_TRANSFORM
                                                                + METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                                + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }
                    }
                else if (DATA_PROCESSED_DATA.equals(strApplyToDataset))
                    {
                    final DatasetType datasetType;
                    final XYDataset xyDataset;

                    xyDataset = dao.getWrappedData().getXYDataset();

                    // Apply to ProcessedData i.e. XYDataset
                    if ((xyDataset != null)
                        && (xyDataset.getSeriesCount() > 0))
                        {
                        if ((intApplyToChannelID < -1)
                            || (intApplyToChannelID >= xyDataset.getSeriesCount()))
                            {
                            throw new IndexOutOfBoundsException(MSG_CHANNEL_RANGE);
                            }

                        strApplicableChannel = DAOCommandHelper.getApplicableChannelID(intApplyToChannelID);

                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_ACTION_TRANSFORM
                                                                + METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                + METADATA_SCALE_FACTOR + dblLinearScaleFactor + TERMINATOR_SPACE
                                                                + METADATA_OFFSET + dblLinearOffset + TERMINATOR_SPACE
                                                                + METADATA_CHANNEL + strApplicableChannel + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());

                        // Is ProcessedData a TimeSeries or an XYSeries?

                        if (DataAnalyser.isTimeSeriesProcessedData(dao.getXYDataset()))
                            {
                            final TimeSeriesCollection collection;

                            datasetType = DatasetType.TIMESTAMPED;

                            // There should be a collection of <channelcount> TimeSeries in the Dataset
                            collection = (TimeSeriesCollection) dao.getWrappedData().getXYDataset();

                            if ((collection != null)
                                && (collection.getSeriesCount() > 0)
                                && (collection.getSeriesCount() == dao.getWrappedData().getRawDataChannelCount())
                                && (collection.getSeries() != null)
                                && (collection.getSeries().get(0) != null))
                                {
                                final List listSeries;
                                final int intItemCount;

                                // Retrieve the TimeSeries as a List
                                listSeries = collection.getSeries();

                                // We assume that all Series contain the same number of DataItems
                                // This is the number of data rows in the Export
                                // We know that Series 0 must exist, so count that one
                                intItemCount = ((TimeSeries)listSeries.get(0)).getItemCount();

                                // Process each Row
                                for (int intRowIndex = 0;
                                     intRowIndex < intItemCount;
                                     intRowIndex++)
                                    {
                                    if (intApplyToChannelID == -1)
                                        {
                                        // Process each TimeSeries (channel)
                                        for (int intSeriesIndex = 0;
                                             intSeriesIndex < dao.getWrappedData().getRawDataChannelCount();
                                             intSeriesIndex++)
                                            {
                                            final TimeSeriesDataItem item;

                                            item = ((TimeSeries)listSeries.get(intSeriesIndex)).getDataItem(intRowIndex);

                                            // Calculate the new value and set
                                            item.setValue((((Double)item.getValue()) * dblLinearScaleFactor) + dblLinearOffset);
                                            }
                                        }
                                    else
                                        {
                                        final TimeSeriesDataItem item;

                                        // Just process a single TimeSeries (channel)
                                        item = ((TimeSeries)listSeries.get(intApplyToChannelID)).getDataItem(intRowIndex);

                                        // Calculate the new value and set
                                        item.setValue((((Double)item.getValue()) * dblLinearScaleFactor) + dblLinearOffset);
                                        }
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + MSG_INVALID_TIME_SERIES);
                                }

                            // Now pass the DAO data to the host Instrument
                            // This is responsible for updating Instrument panels and EventLogs, for instance
                            // Only refresh the data if visible
                            dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, false);
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }
                        else if (DataAnalyser.isXYSeriesProcessedData(xyDataset))
                            {
                            final XYSeriesCollection collection;

                            datasetType = DatasetType.XY;

                            // There should be a collection of <channelcount> XYSeries in the Dataset
                            collection = (XYSeriesCollection)xyDataset;

                            if ((collection != null)
                                && (collection.getSeriesCount() > 0)
                                && (collection.getSeriesCount() == dao.getWrappedData().getRawDataChannelCount())
                                && (collection.getSeries() != null))
                                {
                                final List listSeries;
                                final int intItemCount;

                                // Retrieve the XYSeries as a List
                                listSeries = collection.getSeries();

                                // We assume that all Series contain the same number of DataItems
                                // We know that Series 0 must exist, so count that one
                                intItemCount = ((XYSeries)listSeries.get(0)).getItemCount();

                                // Process each Row
                                // Index X-value Channel0  Channel1  Channel2 ...
                                for (int intRowIndex = 0;
                                     intRowIndex < intItemCount;
                                     intRowIndex++)
                                    {
                                    if (intApplyToChannelID == -1)
                                        {
                                        // Process each XYSeries (channel)
                                        for (int intSeriesIndex = 0;
                                             intSeriesIndex < dao.getWrappedData().getRawDataChannelCount();
                                             intSeriesIndex++)
                                            {
                                            final XYDataItem item;

                                            item = ((XYSeries)listSeries.get(intSeriesIndex)).getDataItem(intRowIndex);

                                            // Calculate the new value and set
                                            item.setY((((Double)item.getY()) * dblLinearScaleFactor) + dblLinearOffset);
                                            }
                                        }
                                    else
                                        {
                                        final XYDataItem item;

                                        // Just process a single XYSeries (channel)
                                        item = ((XYSeries)listSeries.get(intApplyToChannelID)).getDataItem(intRowIndex);

                                        // Calculate the new value and set
                                        item.setY((((Double)item.getY()) * dblLinearScaleFactor) + dblLinearOffset);
                                        }
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + MSG_INVALID_XY_SERIES);
                                }

                            // Now pass the DAO data to the host Instrument
                            // This is responsible for updating Instrument panels and EventLogs, for instance
                            // Only refresh the data if visible
                            dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, false);
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                            }
                        else
                            {
                            // We don't understand the data format
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_ACTION_TRANSFORM
                                                                    + METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                    + METADATA_RESULT + MSG_UNSUPPORTED_FORMAT + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.INFO,
                                                           METADATA_ACTION_TRANSFORM
                                                                + METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }
                    }
                else
                    {
                    // Incorrectly configured XML
                    dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                            SOURCE,
                                                                                            METADATA_TARGET_DATASET,
                                                                                            METADATA_ACTION_TRANSFORM));
                    }
                }

            // This should have been trapped by Regex
            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_ACTION_TRANSFORM
                                                   + METADATA_RESULT
                                                           + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IndexOutOfBoundsException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_ACTION_TRANSFORM
                                                   + METADATA_RESULT
                                                           + MSG_CHANNEL_RANGE
                                                           + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            }
        else
            {
            // Incorrectly configured XML
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                    SOURCE,
                                                                                    METADATA_TARGET_DATASET,
                                                                                    METADATA_ACTION_TRANSFORM));
            }


        // Create the ResponseMessage
        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
            cmdApplyTransform.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdApplyTransform,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdApplyTransform));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdApplyTransform,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdApplyTransform));
             }

        return (responseMessage);
        }
    }
