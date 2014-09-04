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

package org.lmn.fc.common.datafilters;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


/***************************************************************************************************
 * DataFilterHelper.
 */

public final class DataFilterHelper implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               FrameworkRegex,
                                               ResourceKeys
    {
    // String Resources
    private static final String FILTER_NOT_INSTANTIATED = "Unable to instantiate the DataFilter";


    /**********************************************************************************************/
    /* Filter Management                                                                          */
    /***********************************************************************************************
     * Instantiate a DataFilter.
     *
     * @param classname
     *
     * @return DataFilterInterface
     */

    public static DataFilterInterface instantiateFilter(final String classname)
        {
        final String SOURCE = "DataFilterHelper.instantiateFilter() ";
        DataFilterInterface filterInterface;

        filterInterface = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            final String strInterface;
            boolean boolLoaded;

            classObject = Class.forName(classname);

            // Does the target implement the DataFilterInterface?
            interfaces = classObject.getInterfaces();
            strInterface = DataFilterInterface.class.getName();
            boolLoaded = false;

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0;
                         ((i < interfaces.length) && (!boolLoaded));
                         i++)
                        {
                        if (strInterface.equals(interfaces[i].getName()))
                            {
                            final Class superClass;

                            // We have found the correct interface
                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   SOURCE + "DataFilter: [" + classname + " implements " + strInterface + "]");

                            // Prove that the real Instrument is a subclass of DataFilter
                            superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (DataFilter.class.getName().equals(superClass.getName()))
                                    {
                                    final Constructor constructor;

                                    // Now get hold of the Constructor
                                    constructor = classObject.getDeclaredConstructor();

                                    if (constructor != null)
                                        {
                                        filterInterface = (DataFilterInterface)constructor.newInstance();
                                        boolLoaded = true;
                                        }
                                    else
                                        {
                                        LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "Constructor not found");
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "Class is not a subclass of " + DataFilter.class.getName());
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "Class has no superclass");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "No interfaces found");
                }
            }

        catch(NoSuchMethodException exception)
            {
            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "NoSuchMethodException [classname=" + classname + "]");
            }

        catch(SecurityException exception)
            {
            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "SecurityException [classname=" + classname + "]");
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "InstantiationException [classname=" + classname + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "IllegalAccessException [classname=" + classname + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "IllegalArgumentException [classname=" + classname + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "InvocationTargetException [classname=" + classname + "]");
            }

        catch (ClassNotFoundException exception)
            {
            // Suppress empty classnames, because these are probably intentional
            if ((classname != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(classname.trim())))
                {
                LOGGER.error(SOURCE + FILTER_NOT_INSTANTIATED + "ClassNotFoundException [classname=" + classname + "]");
                }
            }

        return (filterInterface);
        }


    /***********************************************************************************************
     * Apply the set of Parameters to the specified Filter, starting at the specified index.
     * Do nothing if the specified parameters are incorrect.
     *
     * @param filter
     * @param executionparameters
     * @param parameterindex
     */

    public static void applyFilterParameters(final DataFilterInterface filter,
                                             final List<ParameterType> executionparameters,
                                             final int parameterindex)
        {
        if (filter != null)
            {
            final List<ParameterType> listFilterParameters;

            listFilterParameters = new ArrayList<ParameterType>(filter.getParameterCount());

            if ((executionparameters != null)
                && (!executionparameters.isEmpty())
                && (parameterindex < executionparameters.size()))
                {
                for (int intFilterSubParameterIndex = 0;
                     (intFilterSubParameterIndex < filter.getParameterCount());
                     intFilterSubParameterIndex++)
                    {
                    final int intExecutionParameterIndex;

                    intExecutionParameterIndex = parameterindex + 1 + intFilterSubParameterIndex;

                    if (intExecutionParameterIndex < executionparameters.size())
                        {
                        listFilterParameters.add(executionparameters.get(intExecutionParameterIndex));
                        }
                    }
                }

            if (listFilterParameters.size() == filter.getParameterCount())
                {
                filter.setParameters(listFilterParameters);
                }
            }
        }


    /**********************************************************************************************/
    /* Filter to TimeSeries                                                                       */
    /***********************************************************************************************
     * Perform the TimeSeries Filter operation on the Calendarised RawData.
     *
     * @param dao
     * @param filter
     * @param filtertype
     * @param datatypes
     * @param metadatalist
     * @param collection
     */

    public static void filterCalendarisedRawDataToTimeSeries(final ObservatoryInstrumentDAOInterface dao,
                                                             final DataFilterInterface filter,
                                                             final DataFilterType filtertype,
                                                             final List<DataTypeDictionary> datatypes,
                                                             final List<Metadata> metadatalist,
                                                             final TimeSeriesCollection collection)
        {
        final String SOURCE = "DataFilterHelper.filterCalendarisedRawDataToTimeSeries() ";

        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                           EventStatus.INFO,
                                           METADATA_TARGET_TIMESERIES
                                               + METADATA_ACTION_FILTERING
                                               + METADATA_FILTERNAME + filtertype.getName() + TERMINATOR,
                                           dao.getLocalHostname(),
                                           dao.getObservatoryClock());

        // TODO supply data types
        // The data format is: <Calendar> <Channel0> <Channel1> <Channel2>
        for (int intChannelIndex = 0;
             ((intChannelIndex < dao.getRawDataChannelCount())
              && (dao.getRawData() != null)
              && (dao.getEventLogFragment() != null)
              && (dao.getObservatoryClock() != null));
             intChannelIndex++)
            {
            final String strChannelName;
            final TimeSeries timeSeries;

            // Get the channel names from the Metadata
            strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                           intChannelIndex,
                                                           dao.hasTemperatureChannel());

            timeSeries = filter.filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                  intChannelIndex,
                                                                  strChannelName,
                                                                  dao.getEventLogFragment(),
                                                                  dao.getObservatoryClock());
            if (timeSeries != null)
                {
                collection.addSeries(timeSeries);
                dao.setProcessedDataChanged(true);
                }
            }
        }


    /***********************************************************************************************
     * Filter the DAO RawData, to produce the TimeSeries XYDataset,
     * and apply to the host Instrument for passing onwards to InstrumentPanels etc.
     * This only applies to an IteratedDataCommand.
     * Set the RawData Metadata on the DAO.
     * Set the XYDataset on the DAO.
     * All parameters are assumed to be valid and not null.
     * TODO All channels?
     *
     * @param dao
     * @param listdatatypes
     * @param metadatalist
     * @param realtimeupdate
     * @param verboselogging
     * @param source
     */

    public static void filterCapturedCalendarisedRawDataToTimeSeries(final ObservatoryInstrumentDAOInterface dao,
                                                                     final List<DataTypeDictionary> listdatatypes,
                                                                     final List<Metadata> metadatalist,
                                                                     final boolean realtimeupdate,
                                                                     final boolean verboselogging,
                                                                     final String source)
        {
        final String SOURCE = "DataFilterHelper.filterCapturedCalendarisedRawDataToTimeSeries() ";
        final TimeSeriesCollection timeSeriesCollection;
        final boolean boolDoLogging;

        // Don't log during RealtimeUpdate unless we must do VerboseLogging
        //
        // RealtimeUpdate   VerboseLogging   DoLogging
        //       0                0              0
        //       0                1              1
        //       1                0              0
        //       1                1              1

        boolDoLogging = verboselogging;

        // This will be empty if the filter fails
        timeSeriesCollection = new TimeSeriesCollection();

        // Make sure there's something to do...
        // This version filters only one channel
        if ((dao.getFilter() != null)
            && (listdatatypes != null)
            && (listdatatypes.size() >= 1))
            {
            final DataTypeDictionary channelDataType;
            final int intChannelID;
            final String strChannelName;
            TimeSeries timeSeries;

            if (boolDoLogging)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            // The Metadata and RawData are already set on the DAO, ready for the DAOWrapper

            // Add the Metadata provided by the Iterator Module
            dao.addAllMetadataToContainersTraced(metadatalist,
                                                 SOURCE,
                                                 LOADER_PROPERTIES.isMetadataDebug());

            // This version filters only one channel, the first
            intChannelID = 0;

            // The data format is: <Calendar> <Channel0> {<Channel1>}
            // Get the channel names from the Metadata
            strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                           intChannelID,
                                                           dao.hasTemperatureChannel());
            if (boolDoLogging)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_CREATE
                                                           + METADATA_CHANNEL + intChannelID + TERMINATOR + SPACE
                                                           + METADATA_NAME + strChannelName + TERMINATOR + SPACE
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }

            channelDataType = listdatatypes.get(intChannelID);
            timeSeries = null;

            // Filter the Data and create a TimeSeries
            if ((channelDataType != null)
                && (dao.getFilter() != null)
                && (dao.getRawData() != null))
                {
                // Do the TimeSeriesFilter for a single channel
                timeSeries = dao.getFilter().filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                               intChannelID,
                                                                               strChannelName,
                                                                               dao.getEventLogFragment(),
                                                                               dao.getObservatoryClock());
                }

            if (timeSeries != null)
                {
                // Add the series to a Collection for transfer to the DAO XYDataset
                timeSeriesCollection.addSeries(timeSeries);
                dao.setProcessedDataChanged(true);
                }
            }
        else
            {
            // No suitable filter is available, so do nothing and return an empty TimeSeriesCollection
            if (boolDoLogging)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_FILTERING
                                                           + METADATA_RESULT + "Unable to instantiate filter" + TERMINATOR,
                                                   source,
                                                   dao.getObservatoryClock());
                }
            }

        // Were we successful in getting some data from the Filter?
        if (timeSeriesCollection.getSeriesCount() > 0)
            {
            // Tell the DAO about the TimeSeries Collection
            dao.setXYDataset(timeSeriesCollection);
            //LOGGER.debugProtocolEvent(SOURCE + " Filter created valid TimeSeriesCollection");
            }
        }


    /************************************************************************************************
    * Filter the DAO Multichannel RawData, to produce the TimeSeries XYDataset,
    * and apply to the host Instrument for passing onwards to InstrumentPanels etc.
    * This only applies to an IteratedDataCommand, e.g. captureRawDataRealtime().
    * Set the RawData Metadata on the DAO.
    * Set the XYDataset on the DAO.
    * All parameters are assumed to be valid and not null.
    *
    * @param dao
    * @param listdatatypes
    * @param metadatalist
    * @param seriescollection
    * @param realtimeupdate
    * @param verbose
    * @param source
    */

    public static void filterCapturedCalendarisedMultichannelRawDataToTimeSeries(final ObservatoryInstrumentDAOInterface dao,
                                                                                 final List<DataTypeDictionary> listdatatypes,
                                                                                 final List<Metadata> metadatalist,
                                                                                 final TimeSeriesCollection seriescollection,
                                                                                 final boolean realtimeupdate,
                                                                                 final boolean verbose,
                                                                                 final String source)
       {
       final String SOURCE = "DataFilterHelper.filterCapturedCalendarisedMultichannelRawDataToTimeSeries() ";

       // Don't log during RealtimeUpdate unless we must do VerboseLogging
       //
       // RealtimeUpdate   VerboseLogging   DoLogging
       //       0                0              0
       //       0                1              1
       //       1                0              0
       //       1                1              1

       // The TimeSeriesCollection will be empty if the filter fails
       seriescollection.removeAllSeries();

       // Make sure there's something to do...
       if ((dao.getFilter() != null)
           && (listdatatypes != null)
           && (listdatatypes.size() >= 1))
           {
           if (verbose)
               {
               SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                  EventStatus.INFO,
                                                  METADATA_TARGET_RAWDATA
                                                          + METADATA_ACTION_FILTERING
                                                          + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR,
                                                  dao.getLocalHostname(),
                                                  dao.getObservatoryClock());
               }

           // The Metadata and RawData are already set on the DAO, ready for the DAOWrapper

           // Filter all channels, including Temperature
           for (int channel = 0;
                ((channel < listdatatypes.size())
                   && (dao.getFilter() != null)
                   && (dao.getRawData() != null)
                   && (dao.getEventLogFragment() != null)
                   && (dao.getObservatoryClock() != null));
                channel++)
               {
               final String strChannelName;
               final TimeSeries timeSeries;

               // The data format is: <Calendar> <Channel0> {<Channel1>}

               // Note that the IteratorModule Metadata is now in the RawDataMetadata
               // Index:           0  1  2  3  4  5  6  7  8  ...
               // Label: Temperature  0  1  2  3  4  5  6  7  ...
               // Label:           0  1  2  3  4  5  6  7  8  ...

               strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                              channel,
                                                              dao.hasTemperatureChannel());
               if (verbose)
                   {
                   SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                      EventStatus.INFO,
                                                      METADATA_TARGET_TIMESERIES
                                                              + METADATA_ACTION_CREATE
                                                              + METADATA_CHANNEL + channel + TERMINATOR + SPACE
                                                              + METADATA_NAME + strChannelName + TERMINATOR + SPACE
                                                              + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR,
                                                      dao.getLocalHostname(),
                                                      dao.getObservatoryClock());
                   }

               // Filter the Data and create a TimeSeries
               // Do the TimeSeriesFilter for a single channel
               timeSeries = dao.getFilter().filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                              channel,
                                                                              strChannelName,
                                                                              dao.getEventLogFragment(),
                                                                              dao.getObservatoryClock());
               if (timeSeries != null)
                   {
                   // Add the series to a Collection for transfer to the DAO XYDataset
                   // Adds a series to the collection and sends a DatasetChangeEvent to all registered listeners
                   seriescollection.addSeries(timeSeries);
                   dao.setProcessedDataChanged(true);
                   }
               }
           }
       else
           {
           // No suitable filter is available, so do nothing and return an empty TimeSeriesCollection
           if (verbose)
               {
               SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                  EventStatus.WARNING,
                                                  METADATA_TARGET_RAWDATA
                                                          + METADATA_ACTION_FILTERING
                                                          + METADATA_RESULT + "Unable to instantiate filter" + TERMINATOR,
                                                  source,
                                                  dao.getObservatoryClock());
               }
           }

       // Were we successful in getting some data from the Filter?
       if (seriescollection.getSeriesCount() > 0)
           {
           // Tell the DAO about the TimeSeries Collection
           dao.setXYDataset(seriescollection);

           LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                        SOURCE + " Filter created valid TimeSeriesCollection [series_count=" + seriescollection.getSeriesCount() + "]");
           }
       else
           {
           // Help the GC?
           dao.setXYDataset(null);

           LOGGER.debug(LOADER_PROPERTIES.isChartDebug(),
                        SOURCE + " Filter could not create TimeSeriesCollection");
           ObservatoryInstrumentHelper.runGarbageCollector();
           }
       }


    /**********************************************************************************************/
    /* Filter to XYDataset                                                                        */
    /***********************************************************************************************
     * Perform the XYDataset Filter operation on the Calendarised RawData.
     * This could be used in Capture or Import modes.
     *
     * @param dao
     * @param filter
     * @param filtertype
     * @param listdatatypes
     * @param metadatalist
     */

    private static void filterCalendarisedRawDataToXYDataset(final ObservatoryInstrumentDAOInterface dao,
                                                             final DataFilterInterface filter,
                                                             final DataFilterType filtertype,
                                                             final List<DataTypeDictionary> listdatatypes,
                                                             final List<Metadata> metadatalist)
        {
        final String SOURCE = "DataFilterHelper.filterCalendarisedRawDataToXYDataset() ";
        final boolean boolDoLogging;
        final TimeSeriesCollection timeSeriesCollection;

        // Don't log during RealtimeUpdate unless we must do VerboseLogging
        //
        // RealtimeUpdate   VerboseLogging   DoLogging
        //       0                0              0
        //       0                1              1
        //       1                0              0
        //       1                1              1

        boolDoLogging = true;

        if (boolDoLogging)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_FILTERING
                                                   + METADATA_FILTERNAME + filter.getFilterType().getName() + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        // The Metadata and RawData are already set on the DAO, ready for the DAOWrapper
        dao.addAllMetadataToContainersTraced(metadatalist,
                                             SOURCE,
                                             LOADER_PROPERTIES.isMetadataDebug());

        timeSeriesCollection = new TimeSeriesCollection();

        if ((dao.getFilter() != null)
            && (dao.getRawData() != null)
            && (!dao.getRawData().isEmpty())
            && (dao.getRawDataChannelCount() > 0))
            {
            // Process each channel in turn
            for (int intChannelIndex = 0;
                 ((intChannelIndex < dao.getRawDataChannelCount())
                  && (dao.getRawData() != null));
                 intChannelIndex++)
                {
                final String strChannelName;
                final TimeSeries timeSeries;

                // Note that the supplied Metadata is now in the RawDataMetadata
                // Get the channel names from the Metadata
                strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                               intChannelIndex,
                                                               dao.hasTemperatureChannel());

                // Do the TimeSeriesFilter for a single channel
                timeSeries = dao.getFilter().filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                               intChannelIndex,
                                                                               strChannelName,
                                                                               dao.getEventLogFragment(),
                                                                               dao.getObservatoryClock());

                // Build up the TimeSeriesCollection, to eventually contain ChannelCount series
                if (timeSeries != null)
                    {
                    // Add the series to a Collection for transfer to the DAO XYDataset
                    timeSeriesCollection.addSeries(timeSeries);
                    dao.setProcessedDataChanged(true);
                    }
                }
            }

        // Tell the DAO about the TimeSeriesCollection, and hence the ProcessedData and Charts
        dao.setXYDataset(timeSeriesCollection);
        }


    /***********************************************************************************************
     * Perform the XYDataset Filter operation on the Columnar RawData.
     *
     * @param dao
     * @param filter
     * @param filtertype
     * @param listdatatypes
     * @param metadatalist
     */

    public static void filterColumnarRawDataToXYDataset(final ObservatoryInstrumentDAOInterface dao,
                                                        final DataFilterInterface filter,
                                                        final DataFilterType filtertype,
                                                        final List<DataTypeDictionary> listdatatypes,
                                                        final List<Metadata> metadatalist)
        {
        final String SOURCE = "DataFilterHelper.filterColumnarRawDataToXYDataset() ";
        final boolean boolDoLogging;
        final XYSeriesCollection xyDataset;

        // Don't log during RealtimeUpdate unless we must do VerboseLogging
        //
        // RealtimeUpdate   VerboseLogging   DoLogging
        //       0                0              0
        //       0                1              1
        //       1                0              0
        //       1                1              1

        boolDoLogging = true;

        if (boolDoLogging)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_FILTERING
                                                   + METADATA_FILTERNAME + filter.getFilterType().getName() + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        // The Metadata and RawData are already set on the DAO, ready for the DAOWrapper
        dao.addAllMetadataToContainersTraced(metadatalist,
                                             SOURCE,
                                             LOADER_PROPERTIES.isMetadataDebug());

        xyDataset = new XYSeriesCollection();

        if ((dao.getFilter() != null)
            && (dao.getRawData() != null)
            && (!dao.getRawData().isEmpty())
            && (dao.getRawDataChannelCount() > 0))
            {
            // Process each channel in turn
            for (int intChannelIndex = 0;
                 ((intChannelIndex < dao.getRawDataChannelCount())
                  && (dao.getRawData() != null));
                 intChannelIndex++)
                {
                final String strChannelName;
                final XYSeries xySeriesChannel;

                // Note that the supplied Metadata is now in the RawDataMetadata
                // Get the channel names from the Metadata
                strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                               intChannelIndex,
                                                               dao.hasTemperatureChannel());

                xySeriesChannel = dao.getFilter().filterIndexedListToXYSeries(dao.getRawData(),
                                                                              intChannelIndex,
                                                                              strChannelName,
                                                                              dao.getEventLogFragment(),
                                                                              dao.getObservatoryClock());

                // Build up the XYSeriesCollection, to eventually contain ChannelCount series
                if (xySeriesChannel != null)
                    {
                    xyDataset.addSeries(xySeriesChannel);
                    dao.setProcessedDataChanged(true);
                    }
                }
            }

        // Tell the DAO about the XYSeriesCollection, and hence the ProcessedData and Charts
        dao.setXYDataset(xyDataset);
        }


    /***********************************************************************************************
     * Filter the RawData (including optional Temperature channel) to produce the XYDataset,
     * using the specified DataFilterType.
     *
     * @param dao
     * @param filter
     * @param datatypes
     * @param eventsource
     */

    public static void filterCalendarisedRawDataAndTemperature(final ObservatoryInstrumentDAOInterface dao,
                                                               final DataFilterInterface filter,
                                                               final List<DataTypeDictionary> datatypes,
                                                               final String eventsource)
        {
        final String SOURCE = "DataFilterHelper.filterCalendarisedRawDataAndTemperature() ";

        // Create a filtered TimeSeries for each channel of data, including Temperature
        if ((dao != null)
            && (filter != null)
            && (datatypes != null)
            && (datatypes.size() == dao.getRawDataChannelCount()))
            {
            final TimeSeriesCollection timeSeriesCollection;
//            final int intDataChannels;

            // Somewhere to put the data
            timeSeriesCollection = new TimeSeriesCollection();

            // How many channels of real data?
//            if (dao.hasTemperatureChannel())
//                {
//                intDataChannels = dao.getRawDataChannelCount() - 1;
//                }
//            else
//                {
//                intDataChannels = dao.getRawDataChannelCount();
//                }

            //--------------------------------------------------------------------------------------
            // The data format is: <Calendar> [<Temperature>] <Channel0> <Channel1> <Channel2>
            // Add the Temperature channel if present

//            if (dao.hasTemperatureChannel())
//                {
//                final String strChannelName;
//                TimeSeries timeSeries;
//                final int intTemperatureChannelIndex;
//                final DataTypePlugin channelDataType;
//
//                // The channel specifications are in the RawDataMetadata, not in the Metadata
//                strChannelName = ObservatoryInstrumentHelper.getMetadataValueByKey(dao.getRawDataMetadata(),
//                                                                                   MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());
//                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
//                                                   EventStatus.INFO,
//                                                   METADATA_TARGET_TIMESERIES
//                                                           + METADATA_ACTION_CREATE
//                                                           + METADATA_CHANNEL + METADATA_TEMPERATURE + TERMINATOR + SPACE
//                                                           + METADATA_FILTERNAME + filtertype.getName() + TERMINATOR,
//                                                   eventsource,
//                                                   dao.getObservatoryClock());
//
//                // Temperature is always the *first* channel in the data
//                intTemperatureChannelIndex = 0;
//
//                // Temperature is the *first* channel in the List of DataTypes
//                channelDataType = datatypes.get(intTemperatureChannelIndex);
//                timeSeries = null;
//
//                // Filter the Temperature and create a TimeSeries
//                if (channelDataType != null)
//                    {
//                    timeSeries = filter.filterTimestampedListToTimeSeries(dao.getRawData(),
//                                                           intTemperatureChannelIndex,
//                                                           strChannelName,
//                                                           dao.getEventLogFragment(),
//                                                           dao.getObservatoryClock());
//                    }
//
//                if (timeSeries != null)
//                    {
//                    timeSeriesCollection.addSeries(timeSeries);
//
//                    // Add the most recent samples to the Metadata
//                    MetadataHelper.addLastTimestampedValueToMetadata(dao.getObservationMetadata(),
//                                                                         dao.getRawData(),
//                                                                         intTemperatureChannelIndex,
//                                                                         strChannelName);
//                    }
//                }

            //--------------------------------------------------------------------------------------

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_FILTER
                                                   + METADATA_FILTERNAME + filter.getFilterType().getName() + TERMINATOR,
                                               eventsource,
                                               dao.getObservatoryClock());

            // The data format is: <Calendar> [<Temperature>] <Channel0> <Channel1> <Channel2>
            for (int intChannelIndex = 0;
                 ((intChannelIndex < dao.getRawDataChannelCount())
                   && (dao.getFilter() != null)
                   && (dao.getRawData() != null)
                   && (dao.getEventLogFragment() != null)
                   && (dao.getObservatoryClock() != null));
                 intChannelIndex++ )
                {
                final String strChannelName;
                TimeSeries timeSeries;
                final DataTypeDictionary channelDataType;

                // The channel specifications are in the RawDataMetadata, not in the Metadata
                // The supplied Metadata MUST contain the Observation.Channel.Name
               strChannelName = MetadataHelper.getChannelName(dao.getObservationMetadata(),
                                                              intChannelIndex,
                                                              dao.hasTemperatureChannel());

//                // Temperature is the *first* channel in the List of DataTypes,
//                // so skip over it if present
//                if (dao.hasTemperatureChannel())
//                    {
                    channelDataType = datatypes.get(intChannelIndex);
//                    }
//                else
//                    {
//                    channelDataType = datatypes.get(channel);
//                    }

                timeSeries = null;

                // Filter the Channel and create a TimeSeries
                if (channelDataType != null)
                    {
                    // Filter all Channels
                    timeSeries = filter.filterTimestampedListToTimeSeries(dao.getRawData(),
                                                                          intChannelIndex,
                                                                          strChannelName,
                                                                          dao.getEventLogFragment(),
                                                                          dao.getObservatoryClock());
                    }

                if (timeSeries != null)
                    {
                    timeSeriesCollection.addSeries(timeSeries);
                    dao.setProcessedDataChanged(true);

                    // Add the most recent samples to the DAO Metadata, for all channels
//                    MetadataHelper.addLastTimestampedValueToMetadata(dao.getObservationMetadata(),
//                                                                     dao.getRawData(),
//                                                                     intChannelIndex,
//                                                                     dao.hasTemperatureChannel(),
//                                                                     MetadataHelper.getChannelDataType(dao.getRawDataMetadata(),
//                                                                                                       intChannelIndex,
//                                                                                                       dao.hasTemperatureChannel()),
//                                                                     MetadataHelper.getChannelUnits(dao.getRawDataMetadata(),
//                                                                                                    intChannelIndex,
//                                                                                                    dao.hasTemperatureChannel()),
//                                                                     strChannelName);
                    }
                }

            //--------------------------------------------------------------------------------------
            // Were we successful in getting some data?

            if (timeSeriesCollection.getSeriesCount() > 0)
                {
                // Tell the DAO about the TimeSeries
                dao.setXYDataset(timeSeriesCollection);
                }
            }
        else
            {
            // No suitable filter is available
            if (dao != null)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_RAWDATA
                                                   + METADATA_ACTION_FILTERING
                                                   + METADATA_RESULT + "Unable to instantiate filter" + TERMINATOR,
                                                   eventsource,
                                                   dao.getObservatoryClock());
                }
            }
        }


    /***********************************************************************************************
     * Use the PassThrough Filter to update the ProcessedData XYDataset.
     * This could be used in Capture or Import modes.
     *
     * @param dao
     */

    public static void passThroughRawDataToXYDataset(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DataFilterHelper.passThroughRawDataToXYDataset() ";

        final DataFilterInterface filter;

        // Do the job of the PassThroughFilter to update ProcessedData and the Chart
        // Instantiate the filter required by the DataFilterType (which must return not NULL)
        filter = instantiateFilter(DataFilterType.PASS_THROUGH.getFilterClassname());

        if (filter != null)
            {
            filter.initialiseFilter();

            // ToDo REVIEW - this logic seems a bit odd?

            if (DataAnalyser.isCalendarisedRawData(dao.getWrappedData().getRawData()))
                {
                // The supplied Metadata MUST contain the Observation.Channel.Name, so look in ObservationMetadata
                // This could be used in Capture or Import modes
                filterCalendarisedRawDataToXYDataset(dao,
                                                     filter,
                                                     DataFilterType.PASS_THROUGH,
                                                     null,
                                                     dao.getWrappedData().getObservationMetadata());
                }
            else if (DataAnalyser.isColumnarRawData(dao.getWrappedData().getRawData()))
                {
                // The supplied Metadata MUST contain the Observation.Channel.Name, so look in ObservationMetadata
                filterColumnarRawDataToXYDataset(dao,
                                                 filter,
                                                 DataFilterType.PASS_THROUGH,
                                                 null,
                                                 dao.getWrappedData().getObservationMetadata());
                }
             else
                {
                LOGGER.error(SOURCE + MSG_UNSUPPORTED_DATA_FORMAT);
                }

            // Dispose of the Filter
            filter.disposeFilter();
            }
        }


    /***********************************************************************************************
     * Produce the TimeSeries XYDataset with no filtering,
     * and apply to the host Instrument for passing onwards to InstrumentPanels etc.
     * Set the RawData Metadata on the DAO.
     * Set the XYDataset on the DAO.
     * All parameters are assumed to be valid and not null.
     *
     * @param dao
     * @param listdatatypes
     * @param metadatalist
     * @param realtimeupdate
     * @param verboselogging
     * @param source
     */

    private static void copyCalendarisedRawDataToXYDataset(final ObservatoryInstrumentDAOInterface dao,
                                                           final List<DataTypeDictionary> listdatatypes,
                                                           final List<Metadata> metadatalist,
                                                           final boolean realtimeupdate,
                                                           final boolean verboselogging,
                                                           final String source)
        {
        final String SOURCE = "DataFilterHelper.copyCalendarisedRawDataToXYDataset() ";
        final boolean boolDoLogging;
        final TimeSeriesCollection timeSeriesCollection;

        // Don't log during RealtimeUpdate unless we must do VerboseLogging
        //
        // RealtimeUpdate   VerboseLogging   DoLogging
        //       0                0              0
        //       0                1              1
        //       1                0              0
        //       1                1              1

        boolDoLogging = verboselogging;

        if (boolDoLogging)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_FILTERING
                                                       + METADATA_FILTERNAME + DataFilterType.PASS_THROUGH.getName() + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        dao.addAllMetadataToContainersTraced(metadatalist,
                                             SOURCE,
                                             LOADER_PROPERTIES.isMetadataDebug());

        timeSeriesCollection = new TimeSeriesCollection();

        if ((dao.getRawData() != null)
            && (!dao.getRawData().isEmpty())
            && (dao.getRawDataChannelCount() > 0))
            {
            // Process each channel in turn
            for (int intChannelIndex = 0;
                 intChannelIndex < dao.getRawDataChannelCount();
                 intChannelIndex++)
                {
                try
                    {
                    final String strChannelName;
                    final TimeSeries timeSeries;
                    final Vector<Object> vecRawData;
                    final Iterator iterRawData;
                    TimeZone timeZone;

                    // Note that the supplied Metadata is now in the RawDataMetadata
                    // Get the channel names from the Metadata
                    strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                                   intChannelIndex,
                                                                   dao.hasTemperatureChannel());
                    // TimeSeries must have a name!
                    // Make a TimeSeries based on Seconds...
                    timeSeries = new TimeSeries(strChannelName, Second.class);

                    vecRawData = dao.getRawData();
                    timeZone = null;
                    iterRawData = vecRawData.iterator();

                    // The data format is: <Calendar> <Channel0> <Channel1> <Channel2>
                    while ((iterRawData != null)
                        && (iterRawData.hasNext()))
                        {
                        final Object objRow;

                        objRow = iterRawData.next();

                        // There must be at least one Calendar and one data sample
                        if ((objRow instanceof Vector)
                            && (((Vector)objRow).size() == (dao.getRawDataChannelCount()+1)))
                            {
                            final Vector vecSample;
                            Object objItem;

                            vecSample = (Vector)objRow;
                            objItem = vecSample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                            if ((objItem != null)
                                && (objItem instanceof Calendar)
                                && (intChannelIndex >= 0)
                                && (intChannelIndex < (vecSample.size() - 1)))
                                {
                                final Calendar calSample;

                                calSample = (Calendar)objItem;

                                if (timeZone == null)
                                    {
                                    // Record the TimeZone once and never reset it...
                                    timeZone = calSample.getTimeZone();
                                    }

                                // Retrieve the data for the selected Channel
                                objItem = vecSample.get(intChannelIndex + DataTranslatorInterface.INDEX_TIMESTAMPED_DATA);

                                if (objItem != null)
                                    {
                                    // ToDo use reflection to construct the data type to avoid assumptions!
                                    if (objItem instanceof Number)
                                        {
                                        timeSeries.addOrUpdate(new Second(calSample.getTime(),
                                                                          calSample.getTimeZone()),
                                                                          (Number)objItem);
                                        }
                                   else
                                        {
                                        LOGGER.error(SOURCE + MSG_UNSUPPORTED_DATA_FORMAT);
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "The channel RawData item is null");
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "The RawData sample has no Calendar, or the channel is incorrect");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + " RawData has incorrect number of channels");
                            }
                        }

                    // Build up the TimeSeriesCollection, to eventually contain ChannelCount series
                    timeSeriesCollection.addSeries(timeSeries);
                    }

                catch (NumberFormatException exception)
                    {
                    LOGGER.error(SOURCE + " RawData contains data which cannot be parsed into a Double");
                    }

                catch (ClassCastException exception)
                    {
                    LOGGER.error(SOURCE + " RawData contains data of an incorrect type which cannot be added to the XYDataset");
                    }
                }
            }

        // Tell the DAO about the TimeSeriesCollection, and hence the ProcessedData and Charts
        dao.setXYDataset(timeSeriesCollection);
        }


    /***********************************************************************************************
     * Produce the XYSeries XYDataset with no filtering,
     * and apply to the host Instrument for passing onwards to InstrumentPanels etc.
     * Set the RawData Metadata on the DAO.
     * Set the XYDataset on the DAO.
     * All parameters are assumed to be valid and not null.
     *
     * @param dao
     * @param listdatatypes
     * @param metadatalist
     * @param verboselogging
     * @param source
     */

    public static void copyColumnarRawDataToXYDataset(final ObservatoryInstrumentDAOInterface dao,
                                                      final List<DataTypeDictionary> listdatatypes,
                                                      final List<Metadata> metadatalist,
                                                      final boolean verboselogging,
                                                      final String source)
        {
        final String SOURCE = "DataFilterHelper.copyColumnarRawDataToXYDataset() ";
        final boolean boolDoLogging;
        final XYSeriesCollection xyDataset;

        // Don't log during RealtimeUpdate unless we must do VerboseLogging
        //
        // RealtimeUpdate   VerboseLogging   DoLogging
        //       0                0              0
        //       0                1              1
        //       1                0              0
        //       1                1              1

        boolDoLogging = verboselogging;

        if (boolDoLogging)
            {
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_FILTERING
                                                       + METADATA_FILTERNAME + DataFilterType.PASS_THROUGH.getName() + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());
            }

        // The Metadata and RawData are already set on the DAO, ready for the DAOWrapper
        dao.addAllMetadataToContainersTraced(metadatalist,
                                             SOURCE,
                                             LOADER_PROPERTIES.isMetadataDebug());

        xyDataset = new XYSeriesCollection();

        if ((dao.getRawData() != null)
            && (!dao.getRawData().isEmpty())
            && (dao.getRawDataChannelCount() > 0))
            {
            // Process each channel in turn
            for (int channel = 0;
                 ((channel < dao.getRawDataChannelCount())
                     && (dao.getRawData() != null));
                 channel++)
                {
                final String strChannelName;
                final XYSeries xySeriesChannel;

                // Note that the supplied Metadata is now in the RawDataMetadata
                // Get the channel names from the Metadata
                strChannelName = MetadataHelper.getChannelName(metadatalist,
                                                               channel,
                                                               dao.hasTemperatureChannel());
                xySeriesChannel = new XYSeries(strChannelName);

                // Copy the RawData (index, channel_i) to the XYSeries (index, channel_i)
                try
                    {
                    final Vector<Object> vecRawData;
                    final Iterator iterRawData;

                    vecRawData = dao.getRawData();
                    iterRawData = vecRawData.iterator();

                    // The data format is: <X-axis> <Channel0> <Channel1> <Channel2>
                    while (iterRawData.hasNext())
                        {
                        final Object objRow;

                        objRow = iterRawData.next();

                        if ((objRow instanceof Vector)
                            && (((Vector)objRow).size() == (dao.getRawDataChannelCount()+1)))
                            {
                            final Vector vecRow;

                            vecRow = (Vector)objRow;

                            // TODO handle datatypes supplied in listdatatypes

                            // The first item is always the index
                            xySeriesChannel.add(Double.parseDouble(vecRow.get(0).toString()),
                                                Double.parseDouble(vecRow.get(channel+1).toString()));
                            }
                        else
                            {
                            LOGGER.error(SOURCE + " RawData has incorrect number of channels");
                            }
                        }

                    // Build up the XYSeriesCollection, to eventually contain ChannelCount series
                    xyDataset.addSeries(xySeriesChannel);
                    }

                catch (NumberFormatException exception)
                    {
                    LOGGER.error(SOURCE + " RawData contains data which cannot be parsed into a Double");
                    }

                catch (ClassCastException exception)
                    {
                    LOGGER.error(SOURCE + " RawData contains data of an incorrect type which cannot be added to the XYDataset");
                    }
                }
            }

        // Tell the DAO about the XYSeriesCollection, and hence the ProcessedData and Charts
        dao.setXYDataset(xyDataset);

        //LOGGER.debugProtocolEvent(SOURCE + " created valid XYSeriesCollection");
        }


    /**********************************************************************************************/
    /* Unfiltered Datasets                                                                        */
    /***********************************************************************************************
     * Create a TimeSeries from Timestamped RawData, for the specified channelindex.
     *
     * @param timestampeddata
     * @param channelindex
     * @param channelname
     * @param eventlog
     * @param clock
     *
     * @return TimeSeries
     */

    public static TimeSeries createUnfilteredTimeSeriesFromList(final Vector<Object> timestampeddata,
                                                                final int channelindex,
                                                                final String channelname,
                                                                final Vector<Vector> eventlog,
                                                                final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataFilterHelper.createUnfilteredTimeSeriesFromList() ";
        TimeSeries timeSeries;
        boolean boolLogged;

        timeSeries = null;
        boolLogged = false;

        if ((timestampeddata != null)
            && (timestampeddata.size() > 0))
            {
            final int intChannelIndex;
            final String strChannelName;
            final Iterator iterRawData;
            TimeZone timeZone;

            // Validate the remaining parameters
            if (channelindex < 0)
                {
                intChannelIndex = 0;
                }
            else
                {
                intChannelIndex = channelindex;
                }

            // TimeSeries must have a name!
            if ((channelname ==  null)
                || (FrameworkStrings.EMPTY_STRING.equals(channelname.trim())))
                {
                strChannelName = DataFilterInterface.DEFAULT_CHANNEL_NAME;
                }
            else
                {
                strChannelName = channelname;
                }

            // Make a TimeSeries based on Seconds...
            timeSeries = new TimeSeries(strChannelName, Second.class);
            timeZone = null;
            iterRawData = timestampeddata.iterator();

            while ((iterRawData != null)
                   && (iterRawData.hasNext()))
                {
                final Vector vecSample;

                vecSample = (Vector) iterRawData.next();

                // There must be at least one Calendar and one data sample
                if ((vecSample != null)
                    && (vecSample.size() > 1))
                    {
                    Object objItem;

                    objItem = vecSample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                    if ((objItem != null)
                        && (objItem instanceof Calendar)
                        && (intChannelIndex >= 0)
                        && (intChannelIndex < (vecSample.size() - 1)))
                        {
                        final Calendar calSample;

                        calSample = (Calendar)objItem;

                        // ToDo Improve debug flag origin
                        ObservatoryInstrumentHelper.debugCalendar(LOADER_PROPERTIES.isChartDebug(),
                                                                  calSample,
                                                                  SOURCE);
                        if (timeZone == null)
                            {
                            // Record the TimeZone once and never reset it...
                            timeZone = calSample.getTimeZone();
                            }

                        // Retrieve the data for the selected Channel
                        objItem = vecSample.get(intChannelIndex + DataTranslatorInterface.INDEX_TIMESTAMPED_DATA);

                        if (objItem != null)
                            {
                            // ToDo use reflection to construct the data type to avoid assumptions!
                            if (objItem instanceof Number)
                                {
                                timeSeries.addOrUpdate(new Second(calSample.getTime(),
                                                                  calSample.getTimeZone()),
                                                       (Number) objItem);
                                }
                            else
                                {
                                if (!boolLogged)
                                    {
                                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_TIMESERIES
                                                                       + METADATA_ACTION_CREATE
                                                                       + METADATA_RESULT + "Unsupported DataType" + TERMINATOR,
                                                                       SOURCE,
                                                                       clock);
                                    boolLogged = true;
                                    }
                                }
                            }
                        else
                            {
                            if (!boolLogged)
                                {
                                SimpleEventLogUIComponent.logEvent(eventlog,
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_TIMESERIES
                                                                   + METADATA_ACTION_CREATE
                                                                   + METADATA_RESULT + "The channel data item is null" + TERMINATOR,
                                                                   SOURCE,
                                                                   clock);
                                boolLogged = true;
                                }
                            }
                        }
                    else
                        {
                        if (!boolLogged)
                            {
                            SimpleEventLogUIComponent.logEvent(eventlog,
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_TIMESERIES
                                                               + METADATA_ACTION_CREATE
                                                               + METADATA_RESULT + "The data sample has no Calendar, or the channel is incorrect" + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            boolLogged = true;
                            }
                        }
                    }
                else
                    {
                    if (!boolLogged)
                        {
                        SimpleEventLogUIComponent.logEvent(eventlog,
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_CREATE
                                                           + METADATA_RESULT + "The RawData are not in the correct format" + TERMINATOR,
                                                           SOURCE,
                                                           clock);
                        boolLogged = true;
                        }
                    }
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlog,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                               + METADATA_ACTION_CREATE
                                               + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (timeSeries);
        }


    /***********************************************************************************************
     * Create a TimeSeries from Timestamped ProcessedData, for the specified channelindex.
     * This simply creates a clone of the specified Channel Series, and returns that.
     *
     * @param xydataset
     * @param channelindex
     * @param channelname
     * @param eventlog
     * @param clock
     *
     * @return TimeSeries
     */

    public static TimeSeries createUnfilteredTimeSeriesFromTimeSeries(final TimeSeriesCollection xydataset,
                                                                      final int channelindex,
                                                                      final String channelname,
                                                                      final Vector<Vector> eventlog,
                                                                      final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataFilterHelper.createUnfilteredTimeSeriesFromTimeSeries() ";
        TimeSeries timeSeries;

        timeSeries = null;

        if ((xydataset != null)
            && (xydataset.getSeriesCount() > 0)
            && (channelindex >= 0)
            && (channelindex < xydataset.getSeriesCount())
            && (xydataset.getSeries(channelindex) != null))
            {
            try
                {
                final Object objTimeSeries;

                objTimeSeries = xydataset.getSeries(channelindex).clone();

                if ((objTimeSeries != null)
                    && (objTimeSeries instanceof TimeSeries))
                    {
                    timeSeries = (TimeSeries)objTimeSeries;
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_CREATE
                                                           + METADATA_RESULT + "The original TimeSeries is not of the correct type" + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    }
                }

            catch (CloneNotSupportedException exception)
                {
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_CREATE
                                                       + METADATA_RESULT + "Unable to clone the original TimeSeries" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlog,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_CREATE
                                                   + METADATA_RESULT + "No data available to create a TimeSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (timeSeries);
        }


    /***********************************************************************************************
     * Create an XYSeries from Indexed RawData, for the specified channelindex.
     *
     * @param indexeddata
     * @param channelindex
     * @param channelname
     * @param eventlog
     * @param clock
     *
     * @return XYSeries
     */

    public static XYSeries createUnfilteredXYSeriesFromList(final Vector<Object> indexeddata,
                                                            final int channelindex,
                                                            final String channelname,
                                                            final Vector<Vector> eventlog,
                                                            final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataFilterHelper.createUnfilteredXYSeriesFromList() ";
        XYSeries xySeries;
        boolean boolLogged;

        xySeries = null;
        boolLogged = false;

        if ((indexeddata != null)
            && (indexeddata.size() > 0))
            {
            try
                {
                final int intChannelIndex;
                final String strChannelName;
                final Iterator iterRawData;

                // Validate the remaining parameters
                if (channelindex < 0)
                    {
                    intChannelIndex = 0;
                    }
                else
                    {
                    intChannelIndex = channelindex;
                    }

                // XYSeries must have a name!
                if ((channelname ==  null)
                    || (FrameworkStrings.EMPTY_STRING.equals(channelname.trim())))
                    {
                    strChannelName = DataFilterInterface.DEFAULT_CHANNEL_NAME;
                    }
                else
                    {
                    strChannelName = channelname;
                    }

                // Make an XYSeries
                // By default, items added to the series will be sorted into ascending order by x-value, and duplicate x-values will be allowed
                xySeries = new XYSeries(strChannelName);
                iterRawData = indexeddata.iterator();

                // Copy the RawData (index, channel_i) to the XYSeries (index, channel_i)
                // The data format is: <X-axis> <Channel0> <Channel1> <Channel2>
                while ((iterRawData != null)
                    && (iterRawData.hasNext()))
                    {
                    final Object objRow;

                    objRow = iterRawData.next();

                    if ((objRow != null)
                        && (objRow instanceof Vector))
                        {
                        final Vector vecRow;

                        vecRow = (Vector)objRow;

                        if ((intChannelIndex >= 0)
                            && (intChannelIndex < (vecRow.size() - 1)))
                            {
                            // The first item is always the index
                            // ToDo handle datatypes supplied in listdatatypes
                            xySeries.addOrUpdate(Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_X_VALUE).toString()),
                                                 Double.parseDouble(vecRow.get(DataTranslatorInterface.INDEX_INDEXED_DATA + intChannelIndex).toString()));
                            }
                        else
                            {
                            // Only log once in the loop
                            if (!boolLogged)
                                {
                                SimpleEventLogUIComponent.logEvent(eventlog,
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_XYDATASET
                                                                       + METADATA_ACTION_CREATE
                                                                       + METADATA_RESULT + "The RawData have an incorrect number of channels" + TERMINATOR,
                                                                   SOURCE,
                                                                   clock);
                                boolLogged = true;
                                }
                            }
                        }
                    else
                        {
                        // Only log once in the loop
                        if (!boolLogged)
                            {
                            SimpleEventLogUIComponent.logEvent(eventlog,
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_XYDATASET
                                                                   + METADATA_ACTION_CREATE
                                                                   + METADATA_RESULT + "The RawData are not in the correct format" + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            boolLogged = true;
                            }
                        }
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_XYDATASET
                                                       + METADATA_ACTION_CREATE
                                                       + METADATA_RESULT + "The RawData contain data which cannot be parsed into a Double" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (ClassCastException exception)
                {
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_XYDATASET
                                                       + METADATA_ACTION_CREATE
                                                       + METADATA_RESULT + "Unsupported DataType" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlog,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_XYDATASET
                                                   + METADATA_ACTION_CREATE
                                                   + METADATA_RESULT + "No data available to create an XYSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (xySeries);
        }


    /***********************************************************************************************
     * Create a XYSeries from Indexed ProcessedData, for the specified channelindex.
     *
     * @param xydataset
     * @param channelindex
     * @param channelname
     * @param eventlog
     * @param clock
     *
     * @return XYSeries
     */

    public static XYSeries createUnfilteredXYSeriesFromXYSeries(final XYSeriesCollection xydataset,
                                                                final int channelindex,
                                                                final String channelname,
                                                                final Vector<Vector> eventlog,
                                                                final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataFilterHelper.createUnfilteredXYSeriesFromXYSeries() ";
        XYSeries xySeries;

        xySeries = null;

        if ((xydataset != null)
            && (xydataset.getSeriesCount() > 0)
            && (channelindex >= 0)
            && (channelindex < xydataset.getSeriesCount())
            && (xydataset.getSeries(channelindex) != null))
            {
            try
                {
                final Object objXYSeries;

                objXYSeries = xydataset.getSeries(channelindex).clone();

                if ((objXYSeries != null)
                    && (objXYSeries instanceof XYSeries))
                    {
                    xySeries = (XYSeries)objXYSeries;
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(eventlog,
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_TIMESERIES
                                                           + METADATA_ACTION_CREATE
                                                           + METADATA_RESULT + "The original XYSeries is not of the correct type" + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    }
                }

            catch (CloneNotSupportedException exception)
                {
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_TIMESERIES
                                                       + METADATA_ACTION_CREATE
                                                       + METADATA_RESULT + "Unable to clone the original XYSeries" + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(eventlog,
                                               EventStatus.WARNING,
                                               METADATA_TARGET_TIMESERIES
                                                   + METADATA_ACTION_CREATE
                                                   + METADATA_RESULT + "No data available to create a XYSeries" + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (xySeries);
        }


    /**********************************************************************************************/
    /* Channel Values                                                                             */
    /***********************************************************************************************
     * Find the minimum value of the specified Channel of the RawData.
     *
     * @param timestampedvector
     * @param channelindex
     *
     * @return double
     */

    public static double findRawDataChannelMinimum(final Vector<Object> timestampedvector,
                                                   final int channelindex)
        {
        final Iterator iterVectorData;
        double dblMinimum;

        iterVectorData = timestampedvector.iterator();
        dblMinimum = Double.MAX_VALUE;

        while (iterVectorData.hasNext())
            {
            final Vector vecData;

            vecData = (Vector) iterVectorData.next();

            // There must be at least one Calendar and one data sample
            if ((vecData != null)
                && (vecData.size() > 1))
                {
                final Object objItem;

                // Retrieve the data for the selected Channel
                objItem = vecData.get(channelindex + DataTranslatorInterface.INDEX_TIMESTAMPED_DATA);

                if ((objItem != null)
                    && (objItem instanceof Number))
                    {
                    dblMinimum = Math.min(dblMinimum, ((Number) objItem).doubleValue());
                    }
                }
            }

        return (dblMinimum);
        }


    /***********************************************************************************************
     * Find the minimum value of the ProcessedData TimeSeriesDataItem List.
     *
     * @param timeseriesitemlist
     *
     * @return double
     */

    public static double findProcessedDataChannelMinimum(final List<TimeSeriesDataItem> timeseriesitemlist)
        {
        double dblMinimum;

        dblMinimum = Double.MAX_VALUE;

        for (int intInputIndex = 0;
             intInputIndex < timeseriesitemlist.size();
             intInputIndex++)
            {
            final TimeSeriesDataItem dataItem;

            dataItem = timeseriesitemlist.get(intInputIndex);

            // Retrieve the data for the selected Channel
            if ((dataItem != null)
                && (dataItem.getValue() != null))
                {
                dblMinimum = Math.min(dblMinimum, dataItem.getValue().doubleValue());
                }
            }

        return (dblMinimum);
        }
    }
