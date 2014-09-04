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
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.*;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChartHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import uk.me.jstott.coordconv.LatitudeLongitude;
import uk.me.jstott.sun.Sun;
import uk.me.jstott.sun.SunFixed;
import uk.me.jstott.sun.Time;

import java.util.*;


/***************************************************************************************************
 * SegmentTimeSeries.
 */

public final class SegmentTimeSeries implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkRegex,
                                                FrameworkSingletons,
                                                ObservatoryConstants
    {
    // String Resources
    private static final String FILETYPE_RAW_DATA = "_RawData_";
    private static final String FILETYPE_PROCESSED_DATA = "_ProcessedData_";
    private static final String FILE_NOT_CROPPED = "NotCropped";


    /***********************************************************************************************
     * doSegmentTimeSeries.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doSegmentTimeSeries(final ObservatoryInstrumentDAOInterface dao,
                                                               final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SegmentTimeSeries.doSegmentTimeSeries() ";
        final int PARAMETER_COUNT = 4;
        final int INDEX_SEGMENT_SIZE = 0;
        final int INDEX_PATHNAME = 1;
        final int INDEX_FORMAT = 2;
        final int INDEX_APPLYTO = 3;
        final List<ParameterType> listExecutionParameters;
        final CommandType cmdSegmentTimeSeries;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "LOCAL COMMAND");

        // Don't affect the CommandType of the incoming Command
        cmdSegmentTimeSeries = (CommandType)commandmessage.getCommandType().copy();

        // Parameters entered on the UI, or via a Macro
        listExecutionParameters = commandmessage.getExecutionParameters();

        // Do not affect any data containers, channel count, or temperature indicator
        // Expect four Parameters:
        //
        // Segment.Size         String          Day,Daytime,Hour,Minute
        // Pathname             String
        // Format               String          FormattedStardata,CompressedStardata,CommaSeparated,TabSeparated
        // ApplyTo.Dataset      String          RawData,ProcessedData

        if ((dao.getHostInstrument() != null)
            && (listExecutionParameters != null)
            && (listExecutionParameters.size() == PARAMETER_COUNT)
            && (listExecutionParameters.get(INDEX_SEGMENT_SIZE) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_SEGMENT_SIZE).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_PATHNAME) != null)
            && (SchemaDataType.PATH_NAME.equals(listExecutionParameters.get(INDEX_PATHNAME).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_APPLYTO) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_APPLYTO).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strSegmentSize;
                final SegmentSize segmentSize;
                final String strPathname;
                final String strFormat;
                final DataFormat dataFormat;
                final String strApplyToDataset;
                final Dataset datasetApplyTo;

                strSegmentSize = listExecutionParameters.get(INDEX_SEGMENT_SIZE).getValue();
                strPathname = listExecutionParameters.get(INDEX_PATHNAME).getValue();
                strFormat = listExecutionParameters.get(INDEX_FORMAT).getValue();
                strApplyToDataset = listExecutionParameters.get(INDEX_APPLYTO).getValue();

                // Map the entries to Enums
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                segmentSize = SegmentSize.getSegmentSizeForName(strSegmentSize);
                dataFormat = DataFormat.getDataFormatForName(strFormat);
                datasetApplyTo = Dataset.getDatasetForName(strApplyToDataset);

                switch (datasetApplyTo)
                    {
                    case RAW:
                        {
                        dao.getResponseMessageStatusList().add(applyToRawDataTimeSeries(dao,
                                                                                          segmentSize,
                                                                                          strPathname,
                                                                                          dataFormat));
                        break;
                        }

                    // Removed for simplicity for now...
//                    case PROCESSED:
//                        {
//                        responseStatus = applyToProcessedDataTimeSeries(dao,
//                                                                        segmentSize,
//                                                                        strPathname,
//                                                                        dataFormat);
//                        break;
//                        }

                    default:
                        {
                        // Incorrectly configured XML
                        dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                                SOURCE,
                                                                                                METADATA_TARGET_TIMESERIES,
                                                                                                METADATA_ACTION_SEGMENT));
                        }
                    }
                }

            // This should have been trapped by Regex
            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_SEGMENT
                                                       + METADATA_RESULT
                                                           + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT
                                                           + exception.getMessage()
                                                           + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_SEGMENT
                                                       + METADATA_RESULT
                                                           + MSG_UNSUPPORTED_FORMAT
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
                                                                                    METADATA_TARGET_TIMESERIES,
                                                                                    METADATA_ACTION_SEGMENT));
            }

        ObservatoryInstrumentHelper.runGarbageCollector();

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        // Create the ResponseMessage
        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Create the ResponseMessage - this creates a DAOWrapper containing the data and logs
            cmdSegmentTimeSeries.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

            responseMessage = ResponseMessageHelper.constructResponse(dao,
                                                                      commandmessage,
                                                                      commandmessage.getInstrument(),
                                                                      commandmessage.getModule(),
                                                                      cmdSegmentTimeSeries,
                                                                      AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                       commandmessage.getModule(),
                                                                                                                       cmdSegmentTimeSeries));
            }
        else
            {
            // Create the failed ResponseMessage, indicating the last Status received
            responseMessage = ResponseMessageHelper.constructEmptyResponse(dao,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           cmdSegmentTimeSeries,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdSegmentTimeSeries));
             }

        return (responseMessage);
        }


    /***********************************************************************************************
     * ApplyTo RawData TimeSeries.
     *
     * @param dao
     * @param segmentsize
     * @param pathname
     * @param format
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus applyToRawDataTimeSeries(final ObservatoryInstrumentDAOInterface dao,
                                                                  final SegmentSize segmentsize,
                                                                  final String pathname,
                                                                  final DataFormat format)
        {
        final String SOURCE = "SegmentTimeSeries.applyToRawDataTimeSeries() ";
        final ResponseMessageStatus responseMessageStatus;

        // Apply to RawData
        if ((dao.getRawData() != null)
            && (!dao.getRawData().isEmpty())
            && (dao.getRawDataChannelCount() > 0)
            && (segmentsize != null)
            && (pathname != null)
            && (format != null))
            {
            // Is RawData Calendarised or Columnar? (TIMESTAMPED or XY)
            // There must be one Calendar and ChannelCount samples in the Vector...
            // OR <x-axis> <Channel0> <Channel1> <Channel2> ...

            if (DataAnalyser.isCalendarisedRawData(dao.getRawData()))
                {
                responseMessageStatus = segmentRawData(dao,
                                                segmentsize,
                                                pathname,
                                                format,
                                                DatasetType.TIMESTAMPED);
                }
            else if (DataAnalyser.isColumnarRawData(dao.getRawData()))
                {
                // This isn't allowed for this Command
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                       + METADATA_ACTION_SEGMENT
                                                       + METADATA_RESULT + MSG_INVALID_DATASET_TYPE + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                }
            else
                {
                // We don't understand the data format
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                       + METADATA_ACTION_SEGMENT
                                                       + METADATA_RESULT + MSG_UNSUPPORTED_FORMAT + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                   + METADATA_ACTION_SEGMENT
                                                   + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * ApplyTo ProcessedData TimeSeries.
     *
     * @param dao
     * @param segmentsize
     * @param pathname
     * @param format
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus applyToProcessedDataTimeSeries(final ObservatoryInstrumentDAOInterface dao,
                                                                        final SegmentSize segmentsize,
                                                                        final String pathname,
                                                                        final DataFormat format)
        {
        final String SOURCE = "SegmentTimeSeries.applyToProcessedDataTimeSeries() ";
        final ResponseMessageStatus responseMessageStatus;

        // Apply to ProcessedData i.e. XYDataset
        if ((dao.getRawData() != null)
            && (dao.getRawData().size() > 0)
            && (segmentsize != null)
            && (pathname != null)
            && (format != null))
            {
            if ((dao.getXYDataset() != null)
                && (dao.getXYDataset().getSeriesCount() > 0))
                {
                // Is ProcessedData a TimeSeries or an XYSeries? (TIMESTAMPED or XY)

                if (DataAnalyser.isTimeSeriesProcessedData(dao.getXYDataset()))
                    {
                    final TimeSeriesCollection collection;

                    // There should be a collection of <channelcount> TimeSeries in the Dataset
                    collection = (TimeSeriesCollection) dao.getXYDataset();

                    if ((collection != null)
                        && (collection.getSeriesCount() > 0)
                        && (collection.getSeriesCount() == dao.getRawDataChannelCount())
                        && (collection.getSeries() != null)
                        && (collection.getSeries().get(0) != null)
                        && (((TimeSeries)collection.getSeries().get(0)).getItemCount() > 0))
                        {
                        responseMessageStatus = segmentProcessedData(dao,
                                                              segmentsize,
                                                              pathname,
                                                              format,
                                                              DatasetType.TIMESTAMPED);
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                               + METADATA_ACTION_SEGMENT
                                                               + METADATA_RESULT + MSG_INVALID_TIME_SERIES + TERMINATOR,
                                                           dao.getLocalHostname(),
                                                           dao.getObservatoryClock());
                        responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
                        }
                    }
                else if (DataAnalyser.isXYSeriesProcessedData(dao.getXYDataset()))
                    {
                    // This isn't allowed for this Command
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                            + METADATA_ACTION_SEGMENT
                                                            + METADATA_RESULT + MSG_INVALID_DATASET_TYPE + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;
                    }
                else
                    {
                    // We don't understand the data format
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                           + METADATA_ACTION_SEGMENT
                                                           + METADATA_RESULT + MSG_UNSUPPORTED_FORMAT + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                       + METADATA_ACTION_SEGMENT
                                                       + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                   + METADATA_ACTION_SEGMENT
                                                   + METADATA_RESULT + MSG_NO_DATA + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Segment the Timestamped RawData into SegmentSize separate files,
     * written to pathname, in the specified DataFormat.
     * Respond with SUCCESS, PREMATURE_TERMINATION, or INVALID_PARAMETER.
     *
     * @param dao
     * @param segmentsize
     * @param pathname
     * @param format
     * @param datasettype
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus segmentRawData(final ObservatoryInstrumentDAOInterface dao,
                                                        final SegmentSize segmentsize,
                                                        final String pathname,
                                                        final DataFormat format,
                                                        final DatasetType datasettype)
        {
        final String SOURCE = "SegmentTimeSeries.segmentRawData() ";
        ResponseMessageStatus responseMessageStatus;
        DataTranslatorInterface translator;

        // Prepare to fail
        responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;

        // Instantiate the translator required by the export DataFormat
        // Do it here to make sure it is available
        translator = DataTranslatorHelper.instantiateTranslator(format.getTranslatorClassname());

        if ((DatasetType.TIMESTAMPED.equals(datasettype))
            && (dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getRawData() != null)
            && (translator != null))
            {
            final int INDEX_DATA_START = 0;
            final int intDataItemCount;
            Vector<Object> vecSegmentedRawData;
            Vector<Object> vecSegmentRawDataCropped;
            final StringBuffer bufferSegmentFilename;
            final List<Metadata> listAggregateMetadata;
            DAOWrapperInterface wrapperExportRawData;
            Calendar calendarSegmentStart;
            Calendar calendarDataEnd;
            Metadata metadataTmp;
            final String strSavedStartDate;
            final String strSavedStartTime;
            final String strSavedFinishDate;
            final String strSavedFinishTime;
            final Metadata metadataSegmentStartDate;
            final Metadata metadataSegmentStartTime;
            final Metadata metadataSegmentFinishDate;
            final Metadata metadataSegmentFinishTime;
            boolean boolSegmentationSuccess;

            // Set the translator for this DAO (until changed by another command)
            dao.setTranslator(translator);
            dao.getTranslator().initialiseTranslator();

            // Do not change any existing Metadata!

            // Only create the workspace once
            intDataItemCount = dao.getRawData().size();
            bufferSegmentFilename = new StringBuffer();

            // Wrap the existing Metadata and a new empty RawData ready for the Response
            // We can use the DAO or Wrapper interchangeably now
            wrapperExportRawData = new DAOWrapper(null,
                                                  null,
                                                  EMPTY_STRING,
                                                  dao);
            vecSegmentedRawData = new Vector<Object>(intDataItemCount);
            wrapperExportRawData.setRawData(vecSegmentedRawData);
            dao.setUnsavedData(false);

            // Initialise the Calendar at the start of the dataset
            calendarSegmentStart = (Calendar) ((Vector)dao.getRawData().get(INDEX_DATA_START)).get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

            // Find the end of the whole dataset
            calendarDataEnd = (Calendar) ((Vector)dao.getRawData().get(dao.getRawData().size()-1)).get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

            // Obtain all of the Metadata available for this operation
            listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                  (ObservatoryInterface) dao.getHostInstrument().getHostAtom(),
                                                                                  dao.getHostInstrument(),
                                                                                  dao, dao.getWrappedData(),
                                                                                  SOURCE,
                                                                                  LOADER_PROPERTIES.isMetadataDebug());
            // because these will have e.g. the Start and End times changed
            // If the items we require do not exist, create them
            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey(),
                                                            ChronosHelper.toDateString(calendarSegmentStart),
                                                            REGEX_DATE_ISO_YYYY_MM_DD,
                                                            DataTypeDictionary.DATE_YYYY_MM_DD,
                                                            SchemaUnits.YEAR_MONTH_DAY,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_START_DATE);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedStartDate = metadataTmp.getValue();

            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey(),
                                                            ChronosHelper.toTimeString(calendarSegmentStart),
                                                            REGEX_TIME_ISO_HH_MM_SS,
                                                            DataTypeDictionary.TIME_HH_MM_SS,
                                                            SchemaUnits.HOUR_MIN_SEC,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_START_TIME);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedStartTime = metadataTmp.getValue();

            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey(),
                                                            ChronosHelper.toDateString(calendarDataEnd),
                                                            REGEX_DATE_ISO_YYYY_MM_DD,
                                                            DataTypeDictionary.DATE_YYYY_MM_DD,
                                                            SchemaUnits.YEAR_MONTH_DAY,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_DATE);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedFinishDate = metadataTmp.getValue();

            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey(),
                                                            ChronosHelper.toTimeString(calendarDataEnd),
                                                            REGEX_TIME_ISO_HH_MM_SS,
                                                            DataTypeDictionary.TIME_HH_MM_SS,
                                                            SchemaUnits.HOUR_MIN_SEC,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_TIME);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedFinishTime = metadataTmp.getValue();

            // Create the Metadata for use in the export of each segment
            // This will have e.g. the Start and End times changed
            // Initialise the timing Metadata with a default Calendar
            // ToDo Use Observation TimeZone
            metadataSegmentStartDate = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey(),
                                                                     ChronosHelper.toDateString(calendarSegmentStart),
                                                                     REGEX_DATE_ISO_YYYY_MM_DD,
                                                                     DataTypeDictionary.DATE_YYYY_MM_DD,
                                                                     SchemaUnits.YEAR_MONTH_DAY,
                                                                     MetadataFactory.DESCRIPTION_OBSERVATION_START_DATE);

            metadataSegmentStartTime = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey(),
                                                                     ChronosHelper.toTimeString(calendarSegmentStart),
                                                                     REGEX_TIME_ISO_HH_MM_SS,
                                                                     DataTypeDictionary.TIME_HH_MM_SS,
                                                                     SchemaUnits.HOUR_MIN_SEC,
                                                                     MetadataFactory.DESCRIPTION_OBSERVATION_START_TIME);

            metadataSegmentFinishDate = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey(),
                                                                      ChronosHelper.toDateString(calendarDataEnd),
                                                                      REGEX_DATE_ISO_YYYY_MM_DD,
                                                                      DataTypeDictionary.DATE_YYYY_MM_DD,
                                                                      SchemaUnits.YEAR_MONTH_DAY,
                                                                      MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_DATE);

            metadataSegmentFinishTime = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey(),
                                                                      ChronosHelper.toTimeString(calendarDataEnd),
                                                                      REGEX_TIME_ISO_HH_MM_SS,
                                                                      DataTypeDictionary.TIME_HH_MM_SS,
                                                                      SchemaUnits.HOUR_MIN_SEC,
                                                                      MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_TIME);

            // Put the new Metadata back in the Wrapper ObservationMetadata ready for exporting this segment
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentStartDate);
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentStartTime);
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentFinishDate);
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentFinishTime);

            // Some debugs
//            MetadataHelper.showMetadata(metadataSegmentStartDate, "SegmentStartDate", true);
//            MetadataHelper.showMetadata(metadataSegmentStartTime, "SegmentStartTime", true);
//            MetadataHelper.showMetadata(metadataSegmentFinishDate, "SegmentFinishDate", true);
//            MetadataHelper.showMetadata(metadataSegmentFinishTime, "SegmentFinishTime", true);

            // Start the first loop
            boolSegmentationSuccess = true;

            // Scan the entire dataset, and export in chunks of SegmentSize
            // This is a long operation, so check to see if the Instrument has been stopped during execution
            for (int intDataIndex = 0;
                 ((boolSegmentationSuccess)
                    && (intDataIndex < intDataItemCount)
                    && (InstrumentState.isOccupied(dao.getHostInstrument())));
                 intDataIndex++)
                {
                final int intSegmentStartCalendarField;
                int intNextSegmentStartCalendarField;

                // Reset all segment pointers
                vecSegmentedRawData.clear();
                boolSegmentationSuccess = false;

                // Find the first SegmentSize field in the dataset at the current index
                intSegmentStartCalendarField = calendarSegmentStart.get(segmentsize.getCalendarField());
                intNextSegmentStartCalendarField = intSegmentStartCalendarField;

                // Accumulate data items until the SegmentSize calendar field changes or there's no more data
                // Use == so we detect rollovers
                while ((intNextSegmentStartCalendarField == intSegmentStartCalendarField)
                    && (intDataIndex < intDataItemCount))
                    {
                    final Vector<Object> vecRow;

                    // Get the next row
                    vecRow = (Vector<Object>)dao.getRawData().get(intDataIndex);

                    // Get the latest Calendar and see if the SegmentSize field is the same as the first...
                    calendarSegmentStart = (Calendar) (vecRow.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR));
                    intNextSegmentStartCalendarField = calendarSegmentStart.get(segmentsize.getCalendarField());

                    // Have we finished this segment?
                    // Keep only those rows with the same SegmentSize calendar field
                    if (intNextSegmentStartCalendarField == intSegmentStartCalendarField)
                        {
                        vecSegmentedRawData.add(vecRow);
                        intDataIndex++;
                        }
                    else
                        {
                        // We moved into a new segment, so back up
                        intDataIndex--;
                        }

                    // Leave the while() with the Index pointing to the end of the *current* SegmentSize field
                    }

                // Did this Segment contain any data?
                if (!vecSegmentedRawData.isEmpty())
                    {
                    // Form the full Segment filename without extension
                    bufferSegmentFilename.setLength(0);
                    bufferSegmentFilename.append(pathname);

                    if (!pathname.endsWith(System.getProperty("file.separator")))
                        {
                        bufferSegmentFilename.append(System.getProperty("file.separator"));
                        }

                    bufferSegmentFilename.append(dao.getHostInstrument().getInstrument().getIdentifier());
                    bufferSegmentFilename.append(FILETYPE_RAW_DATA);

                    // Is there anything else to do (which might affect the calendars)?
                    // In other words, we have a Segment, but possibly need to remove some items,
                    // for instance the night-time period
                    if (segmentsize.isPostProcess())
                        {
                        switch(segmentsize)
                            {
                            case DAYTIME:
                                {
                                final Calendar calendarRelevantDay;
                                final Calendar calendarStartCroppedExport;
                                final Calendar calendarFinishCroppedExport;

                                calendarRelevantDay = (Calendar) ((Vector)vecSegmentedRawData.get(INDEX_DATA_START)).get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                                // These Calendars will be changed by Sunrise and Sunset
                                calendarStartCroppedExport = (Calendar) calendarRelevantDay.clone();
                                calendarFinishCroppedExport = (Calendar) calendarRelevantDay.clone();

                                if (findObservatorySunriseAndSunset(calendarRelevantDay,
                                                                    calendarStartCroppedExport,
                                                                    calendarFinishCroppedExport,
                                                                    listAggregateMetadata,
                                                                    wrapperExportRawData.getObservationMetadata(),
                                                                    dao.getEventLogFragment(),
                                                                    dao.getLocalHostname(),
                                                                    dao.getObservatoryClock()))
                                    {
                                    vecSegmentRawDataCropped = ChartHelper.cropCalendarisedDataToRange(vecSegmentedRawData,
                                                                                                       calendarStartCroppedExport,
                                                                                                       calendarFinishCroppedExport);
                                    if (vecSegmentRawDataCropped != null)
                                        {
                                        // Change the RawData in the Wrapper
                                        wrapperExportRawData.setRawData(vecSegmentRawDataCropped);

                                        // Update the Dates and Times in the Wrapper ObservationMetadata
                                        metadataSegmentStartDate.setValue(ChronosHelper.toDateString(calendarStartCroppedExport));
                                        metadataSegmentStartTime.setValue(ChronosHelper.toTimeString(calendarStartCroppedExport));
                                        metadataSegmentFinishDate.setValue(ChronosHelper.toDateString(calendarFinishCroppedExport));
                                        metadataSegmentFinishTime.setValue(ChronosHelper.toTimeString(calendarFinishCroppedExport));

                                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentStartDate);
                                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentStartTime);
                                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentFinishDate);
                                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentFinishTime);

                                        // For TIMESTAMPED data, get the Date and Time of the Calendar in the first item in the Vector
                                        // DataAnalyser.isCalendarisedRawData() has *proved* that element zero contains
                                        // the correct Vector format of one Calendar and ChannelCount samples
                                        appendTimestampFromCalendar(bufferSegmentFilename, calendarStartCroppedExport);
                                        }
                                    else
                                        {
                                        // The cropping failed for some reason
                                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                           EventStatus.FATAL,
                                                                           METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                                               + METADATA_ACTION_SEGMENT
                                                                               + METADATA_RESULT + "Unable to remove the hours of darkness" + TERMINATOR,
                                                                           dao.getLocalHostname(),
                                                                           dao.getObservatoryClock());
                                        bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                        }
                                    }
                                else
                                    {
                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.FATAL,
                                                                       METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                                           + METADATA_ACTION_SEGMENT
                                                                           + METADATA_RESULT + "Unable to calculate Sunrise and Sunset" + TERMINATOR,
                                                                       dao.getLocalHostname(),
                                                                       dao.getObservatoryClock());
                                    bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                    }

                                break;
                                }

                            default:
                                {
                                // We should never reach here
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.FATAL,
                                                                   METADATA_TARGET + DATA_RAW_DATA + TERMINATOR_SPACE
                                                                       + METADATA_ACTION_SEGMENT
                                                                       + METADATA_RESULT + "Invalid SegmentSize requested post processing, segmentsize=" + segmentsize.toString() + TERMINATOR,
                                                                   dao.getLocalHostname(),
                                                                   dao.getObservatoryClock());
                                bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                }
                            }

                        // The Data Index will be incremented at the end of each successful loop,
                        // ready for the start of the next segment
                        }

                    // No post-processing, so timestamp normally and leave
                    else
                        {
                        final Calendar calendarStartExport;
                        final Calendar calendarFinishExport;

                        // Get the full Calendar from the first data item in this segment (which must exist)
                        calendarStartExport = (Calendar) ((Vector)vecSegmentedRawData.get(INDEX_DATA_START)).get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                        // ... and again at the end of the export, which again must exist
                        calendarFinishExport = (Calendar) ((Vector)vecSegmentedRawData.get(vecSegmentedRawData.size()-1)).get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                        // Update the Dates and Times in the Wrapper ObservationMetadata
                        // We know these Metadata items must exist
                        metadataSegmentStartDate.setValue(ChronosHelper.toDateString(calendarStartExport));
                        metadataSegmentStartTime.setValue(ChronosHelper.toTimeString(calendarStartExport));
                        metadataSegmentFinishDate.setValue(ChronosHelper.toDateString(calendarFinishExport));
                        metadataSegmentFinishTime.setValue(ChronosHelper.toTimeString(calendarFinishExport));

                        // Update the RawData in the Wrapper for the export
                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentStartDate);
                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentStartTime);
                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentFinishDate);
                        MetadataHelper.addOrUpdateMetadataItem(wrapperExportRawData.getObservationMetadata(), metadataSegmentFinishTime);

                        // For TIMESTAMPED data, get the Date and Time of the Calendar in the first item in the Vector
                        // DataAnalyser.isCalendarisedRawData() has *proved* that element zero contains
                        // the correct Vector format of one Calendar and ChannelCount samples
                        appendTimestampFromCalendar(bufferSegmentFilename, calendarStartExport);
                        }

                    // Don't ask for an export timestamp, because we've done that already
                    boolSegmentationSuccess = dao.getTranslator().exportRawData(wrapperExportRawData,
                                                                                bufferSegmentFilename.toString(),
                                                                                false,
                                                                                dao.getEventLogFragment(),
                                                                                dao.getObservatoryClock());
                    }
                }

            // Some debugs
//            MetadataHelper.showMetadata(metadataSegmentStartDate, "At End SegmentStartDate", true);
//            MetadataHelper.showMetadata(metadataSegmentStartTime, "At End SegmentStartTime", true);
//            MetadataHelper.showMetadata(metadataSegmentFinishDate, "At End SegmentFinishDate", true);
//            MetadataHelper.showMetadata(metadataSegmentFinishTime, "At End SegmentFinishTime", true);

            // Regardless of the outcome of the segmentation, restore the Metadata that were changed during this operation
            // which relate to the entire dataset, still displayed to the user
            MetadataHelper.setValueOnlyIfValid(metadataSegmentStartDate, strSavedStartDate);
            MetadataHelper.setValueOnlyIfValid(metadataSegmentStartTime, strSavedStartTime);
            MetadataHelper.setValueOnlyIfValid(metadataSegmentFinishDate, strSavedFinishDate);
            MetadataHelper.setValueOnlyIfValid(metadataSegmentFinishTime, strSavedFinishTime);

            // See if there's anything we need to know...
            DataTranslatorHelper.addTranslatorMessages(dao.getTranslator(),
                                                       dao.getEventLogFragment(),
                                                       dao.getObservatoryClock(),
                                                       dao.getLocalHostname());
            // Explicitly discard the Wrapper etc.
            wrapperExportRawData = null;
            translator = null;
            vecSegmentedRawData = null;
            vecSegmentRawDataCropped = null;
            calendarSegmentStart = null;
            calendarDataEnd = null;

            // Help things along a bit.... (but this may be blocked by command line options)
            ObservatoryInstrumentHelper.runGarbageCollector();

            // Prepare for exit
            if (boolSegmentationSuccess)
                {
                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                }
            else
                {
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Segment the ProcessedData into SegmentSize separate files,
     * written to pathname, in the specified DataFormat.
     * Respond with SUCCESS, PREMATURE_TERMINATION, or INVALID_PARAMETER.
     * <b>WARNING!</b> This code is incomplete and contains may errors of logic!
     *
     * @param dao
     * @param segmentsize
     * @param pathname
     * @param format
     * @param datasettype
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus segmentProcessedData(final ObservatoryInstrumentDAOInterface dao,
                                                              final SegmentSize segmentsize,
                                                              final String pathname,
                                                              final DataFormat format,
                                                              final DatasetType datasettype)
        {
        final String SOURCE = "SegmentTimeSeries.segmentProcessedData() ";
        ResponseMessageStatus responseMessageStatus;
        DataTranslatorInterface translator;

        // Prepare to fail
        responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;

        // Instantiate the translator required by the export DataFormat
        // Do it here to make sure it is available
        translator = DataTranslatorHelper.instantiateTranslator(format.getTranslatorClassname());

        // Double check we are working with the correct data structure
        if ((DatasetType.TIMESTAMPED.equals(datasettype))
            && (dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getXYDataset() != null)
            && (dao.getXYDataset().getSeriesCount() == dao.getRawDataChannelCount())
            && (translator != null))
            {
            final TimeSeriesCollection xyOriginalDataset;
            final int intChannelCount;
            final int intDataItemCount;
            TimeSeriesCollection xySegmentedDataset;
            List<Metadata> listSegmentMetadata;
            final StringBuffer bufferSegmentFilename;
            final List<Metadata> listAggregateMetadata;
            DAOWrapperInterface wrapperExportProcessedData;

            Calendar calendarSegmentStart;
            Calendar calendarDataEnd;
            Metadata metadataTmp;
            final String strSavedStartDate;
            final String strSavedStartTime;
            final String strSavedFinishDate;
            final String strSavedFinishTime;
            final Metadata metadataSegmentStartDate;
            final Metadata metadataSegmentStartTime;
            final Metadata metadataSegmentFinishDate;
            final Metadata metadataSegmentFinishTime;

            boolean boolSegmentationSuccess;

            // Set the translator for this DAO (until changed by another command)
            dao.setTranslator(translator);
            dao.getTranslator().initialiseTranslator();

            // Do not change any existing Metadata!

            // Only create the workspace once
            xyOriginalDataset = (TimeSeriesCollection)dao.getXYDataset();

            // ProcessedData must have the same channel count as RawData
            intChannelCount = dao.getRawDataChannelCount();

            // todo find max count We know that TimeSeries zero must exist
            intDataItemCount = ((TimeSeries)xyOriginalDataset.getSeries().get(0)).getItemCount();
            bufferSegmentFilename = new StringBuffer();

            xySegmentedDataset = new TimeSeriesCollection();
            listSegmentMetadata = new ArrayList<Metadata>();

            // Add enough empty TimeSeries to the collection, one for each channel
            // Copy the Series Key and TimePeriodClass from the originals
            for (int intChannelIndex = 0;
                 intChannelIndex < intChannelCount;
                 intChannelIndex++)
                {
                // All Starbase TimeSeries are based on Seconds
                // ... this constructor is deprecated, but the time period inference doesn't seem to work
                xySegmentedDataset.addSeries(new TimeSeries(((TimeSeries)xyOriginalDataset.getSeries().get(intChannelIndex)).getKey().toString(),
                                                          ((TimeSeries)xyOriginalDataset.getSeries().get(intChannelIndex)).getTimePeriodClass()));
                }

            wrapperExportProcessedData = new DAOWrapper(null,
                                                        null,
                                                        EMPTY_STRING,
                                                        dao);

            // Initialise the Calendar at the start of the dataset
            // The caller has *proved* that series 0 exists, and has at least one item, at index 0
            // ToDo Use Observation TimeZone and Locale
            calendarSegmentStart = setCalendarFromDataItem(xyOriginalDataset,
                                                           new GregorianCalendar(),
                                                           0);
            calendarDataEnd = null;

            // Obtain all of the Metadata available for this operation
            listAggregateMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                                  (ObservatoryInterface) dao.getHostInstrument().getHostAtom(),
                                                                                  dao.getHostInstrument(),
                                                                                  dao, dao.getWrappedData(),
                                                                                  SOURCE,
                                                                                  LOADER_PROPERTIES.isMetadataDebug());
            // because these will have e.g. the Start and End times changed
            // If the items we require do not exist, create them
            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey(),
                                                            ChronosHelper.toDateString(calendarSegmentStart),
                                                            REGEX_DATE_ISO_YYYY_MM_DD,
                                                            DataTypeDictionary.DATE_YYYY_MM_DD,
                                                            SchemaUnits.YEAR_MONTH_DAY,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_START_DATE);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedStartDate = metadataTmp.getValue();

            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey(),
                                                            ChronosHelper.toTimeString(calendarSegmentStart),
                                                            REGEX_TIME_ISO_HH_MM_SS,
                                                            DataTypeDictionary.TIME_HH_MM_SS,
                                                            SchemaUnits.HOUR_MIN_SEC,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_START_TIME);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedStartTime = metadataTmp.getValue();

            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey(),
                                                            ChronosHelper.toDateString(calendarDataEnd),
                                                            REGEX_DATE_ISO_YYYY_MM_DD,
                                                            DataTypeDictionary.DATE_YYYY_MM_DD,
                                                            SchemaUnits.YEAR_MONTH_DAY,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_DATE);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedFinishDate = metadataTmp.getValue();

            metadataTmp = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                          MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey());
            if (metadataTmp == null)
                {
                metadataTmp = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey(),
                                                            ChronosHelper.toTimeString(calendarDataEnd),
                                                            REGEX_TIME_ISO_HH_MM_SS,
                                                            DataTypeDictionary.TIME_HH_MM_SS,
                                                            SchemaUnits.HOUR_MIN_SEC,
                                                            MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_TIME);
                }

            // Save only the Value, because we can't be sure what the reference actually points to
            strSavedFinishTime = metadataTmp.getValue();

            // Create the Metadata for use in the export of each segment
            // This will have e.g. the Start and End times changed
            // Initialise the timing Metadata with a default Calendar
            // ToDo Use Observation TimeZone
            metadataSegmentStartDate = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_DATE.getKey(),
                                                                     ChronosHelper.toDateString(calendarSegmentStart),
                                                                     REGEX_DATE_ISO_YYYY_MM_DD,
                                                                     DataTypeDictionary.DATE_YYYY_MM_DD,
                                                                     SchemaUnits.YEAR_MONTH_DAY,
                                                                     MetadataFactory.DESCRIPTION_OBSERVATION_START_DATE);

            metadataSegmentStartTime = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_START_TIME.getKey(),
                                                                     ChronosHelper.toTimeString(calendarSegmentStart),
                                                                     REGEX_TIME_ISO_HH_MM_SS,
                                                                     DataTypeDictionary.TIME_HH_MM_SS,
                                                                     SchemaUnits.HOUR_MIN_SEC,
                                                                     MetadataFactory.DESCRIPTION_OBSERVATION_START_TIME);

            metadataSegmentFinishDate = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_DATE.getKey(),
                                                                      ChronosHelper.toDateString(calendarDataEnd),
                                                                      REGEX_DATE_ISO_YYYY_MM_DD,
                                                                      DataTypeDictionary.DATE_YYYY_MM_DD,
                                                                      SchemaUnits.YEAR_MONTH_DAY,
                                                                      MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_DATE);

            metadataSegmentFinishTime = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATION_FINISH_TIME.getKey(),
                                                                      ChronosHelper.toTimeString(calendarDataEnd),
                                                                      REGEX_TIME_ISO_HH_MM_SS,
                                                                      DataTypeDictionary.TIME_HH_MM_SS,
                                                                      SchemaUnits.HOUR_MIN_SEC,
                                                                      MetadataFactory.DESCRIPTION_OBSERVATION_FINISH_TIME);

            // Put the new Metadata back in the Wrapper ObservationMetadata ready for exporting this segment
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData .getObservationMetadata(), metadataSegmentStartDate);
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentStartTime);
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentFinishDate);
            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentFinishTime);

            // Some debugs
            //            MetadataHelper.showMetadata(metadataSegmentStartDate, "SegmentStartDate", true);
            //            MetadataHelper.showMetadata(metadataSegmentStartTime, "SegmentStartTime", true);
            //            MetadataHelper.showMetadata(metadataSegmentFinishDate, "SegmentFinishDate", true);
            //            MetadataHelper.showMetadata(metadataSegmentFinishTime, "SegmentFinishTime", true);

            dao.addAllMetadataToContainersTraced(listSegmentMetadata,
                                                 SOURCE,
                                                 LOADER_PROPERTIES.isMetadataDebug());
            dao.setXYDataset(xySegmentedDataset);
            dao.setUnsavedData(false);

            // Start the first loop
            boolSegmentationSuccess = true;

            // Scan the entire dataset, and export in chunks of SegmentSize
            // This is a long operation, so check to see if the Instrument has been stopped
            for (int intIndex = 0;
                 ((boolSegmentationSuccess)
                    && (intIndex < intDataItemCount)
                    && (InstrumentState.isOccupied(dao.getHostInstrument())));
                 intIndex++)
                {
                final int intStartPoint;
                int intNextPoint;

                // Reset all segment pointers
                for (int intChannelIndex = 0;
                     intChannelIndex < intChannelCount;
                     intChannelIndex++)
                    {
                    // Removes all data items from the series and sends a SeriesChangeEvent to all registered listeners
                    xySegmentedDataset.getSeries(intChannelIndex).clear();
                    }

                boolSegmentationSuccess = false;

                // Find the first SegmentSize field in the dataset at the current index
                intStartPoint = calendarSegmentStart.get(segmentsize.getCalendarField());
                intNextPoint = intStartPoint;

                // Accumulate data items until the SegmentSize field changes or there's no more data
                // Use == so we detect rollovers
                while ((intNextPoint == intStartPoint)
                    && (intIndex < intDataItemCount))
                    {
                    // Get the latest Calendar and see if the SegmentSize field is the same as the first...
                    calendarSegmentStart = setCalendarFromDataItem(xyOriginalDataset,
                                                                   calendarSegmentStart,
                                                                   intIndex);
                    intNextPoint = calendarSegmentStart.get(segmentsize.getCalendarField());

                    // Keep only those rows with the same SegmentSize field
                    if (intNextPoint == intStartPoint)
                        {
                        // Process each TimeSeries (channel) for this point in time
                        for (int intChannelIndex = 0;
                             intChannelIndex < intChannelCount;
                             intChannelIndex++)
                            {
                            xySegmentedDataset.getSeries(intChannelIndex).add(((TimeSeriesDataItem)((TimeSeries)xyOriginalDataset.getSeries().get(intChannelIndex)).getItems().get(intIndex)));
                            }

                        // Move to the next row of Channels
                        intIndex++;
                        }

                    // Leave with the Index pointing to the *next* item in the TimeSeries
                    }

                // Did this Segment contain any data?
                if ((xySegmentedDataset.getSeriesCount() > 0)
                    && (xySegmentedDataset.getSeries(0).getItemCount() > 0))
                    {
                    // Form the full filename without extension
                    bufferSegmentFilename.setLength(0);
                    bufferSegmentFilename.append(pathname);

                    if (!pathname.endsWith(System.getProperty("file.separator")))
                        {
                        bufferSegmentFilename.append(System.getProperty("file.separator"));
                        }

                    bufferSegmentFilename.append(dao.getHostInstrument().getInstrument().getIdentifier());
                    bufferSegmentFilename.append(FILETYPE_PROCESSED_DATA);

                    // Is there anything else to do (which might affect the calendars)?
                    // In other words, we have a Segment, but possibly need to remove some items,
                    // for instance the night-time period
                    if (segmentsize.isPostProcess())
                        {
                        switch(segmentsize)
                            {
                            case DAYTIME:
                                {
                                final Metadata metadataLongitude;
                                final Metadata metadataLatitude;
                                final Metadata metadataHASL;
                                final Metadata metadataTimeZone;
                                final List<String> errors;

                                errors = new ArrayList<String>(10);

                                // Try to find a valid Observatory location to use for the Sunrise and Sunset calculation
                                metadataLongitude = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                                                    MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

                                metadataLatitude = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                                                   MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());

                                metadataHASL = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                                               MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());

                                metadataTimeZone = MetadataHelper.getMetadataByKey(listAggregateMetadata,
                                                                                   MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

                                if ((metadataLongitude != null)
                                    && (metadataLatitude != null)
                                    && (metadataHASL != null)
                                    && (metadataTimeZone != null))
                                    {
                                    final DegMinSecInterface dmsLongitudeObservatory;
                                    final DegMinSecInterface dmsLatitudeObservatory;

                                    // Longitude -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
                                    dmsLongitudeObservatory = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                                              DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                                              EMPTY_STRING,
                                                                                                                              EMPTY_STRING,
                                                                                                                              errors);
                                    // Latitude  -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999  deg:mm:ss
                                    dmsLatitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                                                            DataTypeDictionary.LATITUDE,
                                                                                                                            EMPTY_STRING,
                                                                                                                            EMPTY_STRING,
                                                                                                                            errors);
                                    if ((dmsLongitudeObservatory != null)
                                        && (dmsLatitudeObservatory != null)
                                        && (errors.size() == 0))
                                        {
                                        final double dblLongitudeObservatory;
                                        final double dblLatitudeObservatory;
                                        double dblHASLObservatory;
                                        final TimeZone timeZoneObservatory;
                                        final LatitudeLongitude latlongObservatory;
                                        final Calendar calendarRelevantDay;
                                        final Calendar calendarStartCroppedExport;
                                        final Calendar calendarFinishCroppedExport;
                                        final Time timeSunrise;
                                        final Time timeSunset;
                                        final TimeSeriesCollection xyDatasetSegmentCropped;

                                        // These must be correct to have passed the Metadata parsing above
                                        dblLongitudeObservatory = dmsLongitudeObservatory.toDouble();
                                        dblLatitudeObservatory = dmsLatitudeObservatory.toDouble();

                                        // The very unlikely event of a NumberFormatException is trapped below
                                        try
                                            {
                                            dblHASLObservatory = Double.parseDouble(metadataHASL.getValue());
                                            }

                                        catch (NumberFormatException exception)
                                            {
                                            // This is so unlikely we'll use a default rather than log an error
                                            dblHASLObservatory = 0.0;
                                            }

                                        // This returns the GMT zone if the given ID cannot be understood
                                        timeZoneObservatory = TimeZone.getTimeZone(metadataTimeZone.getValue());

                                        // Move to the form required by JSunTimes
                                        latlongObservatory = new LatitudeLongitude(dblLatitudeObservatory, dblLongitudeObservatory);

                                        // The segment is known to be of a single day only, so take the first Calendar to get the Day
                                        //calendarRelevantDay = (Calendar) ((Vector)vecSegmentedRawData.get(INDEX_DATA_START)).get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);
                                        calendarRelevantDay = null;

                                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                           EventStatus.FATAL,
                                                                           METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR
                                                                           + METADATA_ACTION_SEGMENT
                                                                           + METADATA_LATITUDE + metadataLatitude.getValue() + TERMINATOR_SPACE
                                                                           + METADATA_LONGITUDE + metadataLongitude.getValue() + TERMINATOR_SPACE
                                                                           + METADATA_TIMEZONE + timeZoneObservatory.getDisplayName() + TERMINATOR_SPACE
                                                                           + METADATA_HASL + metadataHASL.getValue() + TERMINATOR_SPACE
                                                                           + METADATA_DATE + ChronosHelper.toDateString(calendarRelevantDay) + TERMINATOR,
                                                                           dao.getLocalHostname(),
                                                                           dao.getObservatoryClock());

                                        timeSunrise = Sun.sunriseTime(calendarRelevantDay, latlongObservatory, timeZoneObservatory, false);
                                        timeSunrise.setRoundedSeconds(true);
                                        timeSunset = Sun.sunsetTime(calendarRelevantDay, latlongObservatory, timeZoneObservatory, false);
                                        timeSunset.setRoundedSeconds(true);

                                        // Log the details of what we've done (these could end up in the Observation Metadata?)
                                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                           EventStatus.FATAL,
                                                                           METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                               + METADATA_ACTION_SEGMENT
                                                                               + METADATA_LATITUDE + metadataLatitude.getValue() + TERMINATOR_SPACE
                                                                               + METADATA_LONGITUDE + metadataLongitude.getValue() + TERMINATOR_SPACE
                                                                               + METADATA_TIMEZONE + timeZoneObservatory.getDisplayName() + TERMINATOR_SPACE
                                                                               + METADATA_HASL + metadataHASL.getValue() + TERMINATOR_SPACE
                                                                               + METADATA_DATE + ChronosHelper.toDateString(calendarRelevantDay) + TERMINATOR_SPACE
                                                                               + METADATA_SUNRISE + timeSunrise.toString() + TERMINATOR_SPACE
                                                                               + METADATA_SUNSET + timeSunset.toString() + TERMINATOR,
                                                                           dao.getLocalHostname(),
                                                                           dao.getObservatoryClock());

                                        // Use the Calendar from the first data item in this segment (which must exist)
                                        calendarStartCroppedExport = (Calendar)calendarRelevantDay.clone();
                                        calendarStartCroppedExport.set(Calendar.MILLISECOND, 0);
                                        calendarStartCroppedExport.set(Calendar.SECOND, (int)timeSunrise.getSeconds());
                                        calendarStartCroppedExport.set(Calendar.MINUTE, timeSunrise.getMinutes());
                                        calendarStartCroppedExport.set(Calendar.HOUR, timeSunrise.getHours());

                                        // ... and again at the end of the export, which again must exist
                                        calendarFinishCroppedExport = (Calendar)calendarRelevantDay.clone();
                                        calendarFinishCroppedExport.set(Calendar.MILLISECOND, 0);
                                        calendarFinishCroppedExport.set(Calendar.SECOND, (int)timeSunset.getSeconds());
                                        calendarFinishCroppedExport.set(Calendar.MINUTE, timeSunset.getMinutes());
                                        calendarFinishCroppedExport.set(Calendar.HOUR, timeSunset.getHours());


                                        xyDatasetSegmentCropped = ChartHelper.cropTimeSeriesCollectionToRange(xySegmentedDataset,
                                                                                                              calendarStartCroppedExport,
                                                                                                              calendarFinishCroppedExport);
                                        if (xyDatasetSegmentCropped != null)
                                            {
                                            // Update the ProcessedData in the Wrapper for the export
                                            wrapperExportProcessedData.setXYDataset(xyDatasetSegmentCropped);

                                            // Update the Dates and Times in the Wrapper ObservationMetadata
                                            metadataSegmentStartDate.setValue(ChronosHelper.toDateString(calendarStartCroppedExport));
                                            metadataSegmentStartTime.setValue(ChronosHelper.toTimeString(calendarStartCroppedExport));
                                            metadataSegmentFinishDate.setValue(ChronosHelper.toDateString(calendarFinishCroppedExport));
                                            metadataSegmentFinishTime.setValue(ChronosHelper.toTimeString(calendarFinishCroppedExport));

                                            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentStartDate);
                                            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentStartTime);
                                            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentFinishDate);
                                            MetadataHelper.addOrUpdateMetadataItem(wrapperExportProcessedData.getObservationMetadata(), metadataSegmentFinishTime);

                                            appendTimestampFromCalendar(bufferSegmentFilename, calendarStartCroppedExport);
                                            }
                                        else
                                            {
                                            // The cropping failed for some reason
                                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                               EventStatus.FATAL,
                                                                               METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                                   + METADATA_ACTION_SEGMENT
                                                                                   + METADATA_RESULT + "Unable to remove the hours of darkness" + TERMINATOR,
                                                                               dao.getLocalHostname(),
                                                                               dao.getObservatoryClock());
                                            bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                            }
                                        }
                                    else
                                        {
                                        // An Observatory location is unlikely (so is this error message)
                                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                           EventStatus.FATAL,
                                                                           METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                               + METADATA_ACTION_SEGMENT
                                                                               + METADATA_RESULT + "Observatory Latitude or Longitude is invalid" + TERMINATOR,
                                                                           dao.getLocalHostname(),
                                                                           dao.getObservatoryClock());
                                        bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                        }
                                    }
                                else
                                    {
                                    // An Observatory location is unlikely
                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.FATAL,
                                                                       METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                       + METADATA_ACTION_SEGMENT
                                                                       + METADATA_RESULT + "Observatory Location incomplete, or not found in Metadata" + TERMINATOR,
                                                                       dao.getLocalHostname(),
                                                                       dao.getObservatoryClock());
                                    bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                    }

                                break;
                                }

                            default:
                                {
                                // We should never reach here
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.FATAL,
                                                                   METADATA_TARGET + DATA_PROCESSED_DATA + TERMINATOR_SPACE
                                                                       + METADATA_ACTION_SEGMENT
                                                                       + METADATA_RESULT + "Invalid SegmentSize requested post processing, segmentsize=" + segmentsize.toString() + TERMINATOR,
                                                                   dao.getLocalHostname(),
                                                                   dao.getObservatoryClock());
                                bufferSegmentFilename.append(FILE_NOT_CROPPED);
                                }
                            }

                        // The Data Index will be incremented at the end of each successful loop,
                        // ready for the start of the next segment
                        }

                    // No post-processing, so timestamp normally and leave
                    else
                        {
                        final Calendar calendarStartExport;
                        final Calendar calendarFinishExport;

                        // Update the ProcessedData in the Wrapper for the export
                        wrapperExportProcessedData.setXYDataset(xySegmentedDataset);

                        // For TIMESTAMPED data, get the Date and Time of the Calendar of the first and last items
                        // No post-processing, so timestamp normally and leave
                        // Get the full Calendar from the first data item in this segment (which must exist)
                        calendarStartExport = setCalendarFromDataItem(xySegmentedDataset,
                                                                      new GregorianCalendar(),
                                                                      0);

                        // ... and again at the end of the export, which again must exist
                        calendarFinishExport = setCalendarFromDataItem(xySegmentedDataset,
                                                                       new GregorianCalendar(),
                                                                       ((TimeSeries)xySegmentedDataset.getSeries().get(0)).getItemCount()-1);

                        // Update the Dates and Times in the Wrapper Metadata
                        if (!wrapperExportProcessedData.getXYDatasetMetadata().isEmpty())
                            {
                            metadataSegmentStartDate.setValue(ChronosHelper.toDateString(calendarStartExport));
                            metadataSegmentStartTime.setValue(ChronosHelper.toTimeString(calendarStartExport));
                            metadataSegmentFinishDate.setValue(ChronosHelper.toDateString(calendarFinishExport));
                            metadataSegmentFinishTime.setValue(ChronosHelper.toTimeString(calendarFinishExport));

                            MetadataHelper.addOrUpdateMetadataItem(listSegmentMetadata, metadataSegmentStartDate);
                            MetadataHelper.addOrUpdateMetadataItem(listSegmentMetadata, metadataSegmentStartTime);
                            MetadataHelper.addOrUpdateMetadataItem(listSegmentMetadata, metadataSegmentFinishDate);
                            MetadataHelper.addOrUpdateMetadataItem(listSegmentMetadata, metadataSegmentFinishTime);
                            }

                        appendTimestampFromCalendar(bufferSegmentFilename, calendarStartExport);
                        }

                    // Don't ask for an export timestamp, because we've done that already
                    boolSegmentationSuccess = dao.getTranslator().exportXYDataset(wrapperExportProcessedData,
                                                                                  bufferSegmentFilename.toString(),
                                                                                  false,
                                                                                  dao.getEventLogFragment(),
                                                                                  dao.getObservatoryClock());
                    }
                }

            // Some debugs
            //            MetadataHelper.showMetadata(metadataSegmentStartDate, "At End SegmentStartDate", true);
            //            MetadataHelper.showMetadata(metadataSegmentStartTime, "At End SegmentStartTime", true);
            //            MetadataHelper.showMetadata(metadataSegmentFinishDate, "At End SegmentFinishDate", true);
            //            MetadataHelper.showMetadata(metadataSegmentFinishTime, "At End SegmentFinishTime", true);

            // Regardless of the outcome of the segmentation, restore the Metadata that were changed during this operation
            // which relate to the entire dataset, still displayed to the user
            MetadataHelper.setValueOnlyIfValid(metadataSegmentStartDate, strSavedStartDate);
            MetadataHelper.setValueOnlyIfValid(metadataSegmentStartTime, strSavedStartTime);
            MetadataHelper.setValueOnlyIfValid(metadataSegmentFinishDate, strSavedFinishDate);
            MetadataHelper.setValueOnlyIfValid(metadataSegmentFinishTime, strSavedFinishTime);

            // See if there's anything we need to know...
            DataTranslatorHelper.addTranslatorMessages(dao.getTranslator(),
                                                       dao.getEventLogFragment(),
                                                       dao.getObservatoryClock(),
                                                       dao.getLocalHostname());
            // Explicitly discard the Wrapper etc.
            wrapperExportProcessedData = null;
            translator = null;
            xySegmentedDataset = null;
            listSegmentMetadata = null;
            calendarSegmentStart = null;
            calendarDataEnd = null;

            // Help things along a bit....
            ObservatoryInstrumentHelper.runGarbageCollector();

            // Prepare for exit
            if (boolSegmentationSuccess)
                {
                responseMessageStatus = ResponseMessageStatus.SUCCESS;
                }
            else
                {
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Find Sunrise and Sunset for the specified Calendar day,
     * changing the settings of the supplied Sunrise and Sunset Calendars.
     * Look in the specified Input Metadata for the Observatory location.
     * Place the calculated Sunrise and Sunset in the Output Metadata.
     * Return a flag to indicate if the times were found correctly.
     * See for instance: http://www.adeptscience.co.uk/products/mathsim/mathcad/add-ons/free_ebooks/astro_form_samp.htm.
     *
     * @param calendarday
     * @param calendarsunrise
     * @param calendarsunset
     * @param inputmetadata
     * @param outputmetadata
     * @param eventlogfragment
     * @param SOURCE
     * @param clock
     *
     * @return boolean
     */

    private static boolean findObservatorySunriseAndSunset(final Calendar calendarday,
                                                           final Calendar calendarsunrise,
                                                           final Calendar calendarsunset,
                                                           final List<Metadata> inputmetadata,
                                                           final List<Metadata> outputmetadata,
                                                           final Vector<Vector> eventlogfragment,
                                                           final String SOURCE,
                                                           final ObservatoryClockInterface clock)
        {
        final String TARGET = "SunriseSunset";
        final Metadata metadataLongitude;
        final Metadata metadataLatitude;
        final Metadata metadataHASL;
        final Metadata metadataTimeZone;
        final List<String> errors;
        final boolean boolSuccess;

        errors = new ArrayList<String>(10);

        // Try to find a valid Observatory location to use for the Sunrise and Sunset calculation
        metadataLongitude = MetadataHelper.getMetadataByKey(inputmetadata,
                                                            MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey());

        metadataLatitude = MetadataHelper.getMetadataByKey(inputmetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey());

        metadataHASL = MetadataHelper.getMetadataByKey(inputmetadata,
                                                       MetadataDictionary.KEY_OBSERVATORY_HASL.getKey());

        metadataTimeZone = MetadataHelper.getMetadataByKey(inputmetadata,
                                                           MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());

        if ((metadataLongitude != null)
            && (metadataLatitude != null)
            && (metadataHASL != null)
            && (metadataTimeZone != null))
            {
            final DegMinSecInterface dmsLongitudeObservatory;
            final DegMinSecInterface dmsLatitudeObservatory;

            // Longitude -179:59:59.9999 to +000:00:00.0000 to +179:59:59.9999  deg:mm:ss
            dmsLongitudeObservatory = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(metadataLongitude.getValue(),
                                                                                                      DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                      EMPTY_STRING,
                                                                                                      EMPTY_STRING,
                                                                                                      errors);
            // Latitude  -89:59:59.9999 to +00:00:00.0000 to +89:59:59.9999  deg:mm:ss
            dmsLatitudeObservatory = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(metadataLatitude.getValue(),
                                                                                                    DataTypeDictionary.LATITUDE,
                                                                                                    EMPTY_STRING,
                                                                                                    EMPTY_STRING,
                                                                                                    errors);
            if ((dmsLongitudeObservatory != null)
                && (dmsLatitudeObservatory != null)
                && (errors.size() == 0))
                {
                final double dblLongitudeObservatory;
                final double dblLatitudeObservatory;
                double dblHASLObservatory;
                final TimeZone timeZoneObservatory;
                final LatitudeLongitude latlongObservatory;
                final Time timeSunrise;
                final Time timeSunset;
                final Metadata metadataSunrise;
                final Metadata metadataSunset;

                // These must be correct to have passed the Metadata parsing above
                dblLongitudeObservatory = dmsLongitudeObservatory.toDouble();
                dblLatitudeObservatory = dmsLatitudeObservatory.toDouble();

                // HASL is not used in this version; the difference to the calculated times is minimal
                // The very unlikely event of a NumberFormatException is trapped below
                try
                    {
                    dblHASLObservatory = Double.parseDouble(metadataHASL.getValue());
                    }

                catch (NumberFormatException exception)
                    {
                    // This is so unlikely we'll use a default rather than log an error
                    dblHASLObservatory = 0.0;
                    }

                // This returns the GMT zone if the given ID cannot be understood
                timeZoneObservatory = TimeZone.getTimeZone(metadataTimeZone.getValue());

                // Move to the form required by JSunTimes
                // WARNING! For JSunTimes, WEST IS NEGATIVE, so change the sign of our Longitude
                latlongObservatory = new LatitudeLongitude(dblLatitudeObservatory, -dblLongitudeObservatory);
                timeSunrise = SunFixed.sunriseTime(calendarday, latlongObservatory, timeZoneObservatory, false);
                timeSunrise.setRoundedSeconds(true);
                timeSunset = SunFixed.sunsetTime(calendarday, latlongObservatory, timeZoneObservatory, false);
                timeSunset.setRoundedSeconds(true);

                // Put the calculated Sunrise and Sunset in the Output Metadata
                metadataSunrise = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATORY_SUNRISE.getKey(),
                                                                timeSunrise.toString(),
                                                                REGEX_TIME_ISO_HH_MM_SS,
                                                                DataTypeDictionary.TIME_HH_MM_SS,
                                                                SchemaUnits.HOUR_MIN_SEC,
                                                                "The time of Sunrise at the Observatory");
                MetadataHelper.addOrUpdateMetadataItemTraced(outputmetadata,
                                                             metadataSunrise,
                                                             SOURCE,
                                                             LOADER_PROPERTIES.isMetadataDebug());

                metadataSunset = MetadataHelper.createMetadata(MetadataDictionary.KEY_OBSERVATORY_SUNSET.getKey(),
                                                               timeSunset.toString(),
                                                               REGEX_TIME_ISO_HH_MM_SS,
                                                               DataTypeDictionary.TIME_HH_MM_SS,
                                                               SchemaUnits.HOUR_MIN_SEC,
                                                               "The time of Sunset at the Observatory");
                MetadataHelper.addOrUpdateMetadataItemTraced(outputmetadata,
                                                             metadataSunset,
                                                             SOURCE,
                                                             LOADER_PROPERTIES.isMetadataDebug());

                // Log the details of what we've done
                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                   EventStatus.INFO,
                                                   METADATA_TARGET + TARGET + TERMINATOR
                                                   + METADATA_ACTION_CALCULATE
                                                   + METADATA_LATITUDE + metadataLatitude.getValue() + TERMINATOR_SPACE
                                                   + METADATA_LONGITUDE + metadataLongitude.getValue() + TERMINATOR_SPACE
                                                   + METADATA_TIMEZONE + timeZoneObservatory.getDisplayName() + TERMINATOR_SPACE
                                                   + METADATA_HASL + metadataHASL.getValue() + TERMINATOR_SPACE
                                                   + METADATA_DATE + ChronosHelper.toDateString(calendarday) + TERMINATOR_SPACE
                                                   + METADATA_SUNRISE + timeSunrise.toString() + TERMINATOR_SPACE
                                                   + METADATA_SUNSET + timeSunset.toString() + TERMINATOR,
                                                   SOURCE,
                                                   clock);

                // Add a one hour guard band to both Sunrise and Sunset
                // Never roll back to the previous day!
                calendarsunrise.set(Calendar.MILLISECOND, 0);
                calendarsunrise.set(Calendar.SECOND, (int)timeSunrise.getSeconds());
                calendarsunrise.set(Calendar.MINUTE, timeSunrise.getMinutes());
                if (timeSunrise.getHours() > 0)
                    {
                    calendarsunrise.set(Calendar.HOUR, timeSunrise.getHours() - 1);
                    }

                // Never roll forward to the next day!
                calendarsunset.set(Calendar.MILLISECOND, 0);
                calendarsunset.set(Calendar.SECOND, (int)timeSunset.getSeconds());
                calendarsunset.set(Calendar.MINUTE, timeSunset.getMinutes());
                calendarsunset.set(Calendar.HOUR, timeSunset.getHours());
                if (timeSunset.getHours() < 23)
                    {
                    calendarsunset.set(Calendar.HOUR, timeSunset.getHours() + 1);
                    }

                boolSuccess = true;
                }
            else
                {
                // An Observatory location is unlikely (so is this error message)
                SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET + TARGET + TERMINATOR
                                                   + METADATA_ACTION_CALCULATE
                                                   + METADATA_RESULT + "Observatory Latitude or Longitude is invalid" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                boolSuccess = false;
                }
            }
        else
            {
            // An Observatory location is unlikely
            SimpleEventLogUIComponent.logEvent(eventlogfragment,
                                               EventStatus.FATAL,
                                               METADATA_TARGET + TARGET + TERMINATOR
                                               + METADATA_ACTION_CALCULATE
                                               + METADATA_RESULT + "Observatory Location incomplete, or not found in Metadata" + TERMINATOR,
                                               SOURCE,
                                               clock);
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Set the time of a Calendar from the number of milliseconds
     * in the TimeSeries DataItem at the given index.
     * TODO All data are taken from Series zero in the TimeSeriesCollection, which must exist.
     * All parameters are assumed to be non-null.
     *
     * @param collection
     * @param calendar
     * @param index
     *
     * @return Calendar
     */

    private static Calendar setCalendarFromDataItem(final TimeSeriesCollection collection,
                                                    final Calendar calendar,
                                                    final int index)
        {
        calendar.setTimeInMillis(((TimeSeries)collection.getSeries().get(0)).getDataItem(index).getPeriod().getFirstMillisecond(calendar));

        return (calendar);
        }


    /************************************************************************************************
     * Append a timestamp to a buffer, using the specified Calendar, in the format YYYMMDD_HHMMSS.
     *
     * @param buffer
     * @param calendar
     */

    private static void appendTimestampFromCalendar(final StringBuffer buffer,
                                                    final Calendar calendar)
        {
        final String strDate;
        final String strTime;

        strDate = ChronosHelper.toDateString(calendar);
        strTime = ChronosHelper.toTimeString(calendar);

        // Remove slashes from Date
        for (int i = 0; i < strDate.length(); i++)
            {
            if (strDate.charAt(i) != '-')
                {
                buffer.append(strDate.charAt(i));
                }
            }

        buffer.append('_');

        // Remove colons from Time
        for (int i = 0; i < strTime.length(); i++)
            {
            if (strTime.charAt(i) != ':')
                {
                buffer.append(strTime.charAt(i));
                }
            }
        }
    }