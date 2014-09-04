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

package org.lmn.fc.common.datatranslators;

import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DataAnalyser.
 */

public final class DataAnalyser implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkSingletons
    {
    /***********************************************************************************************
     * Validate the specified Metadata, for use as a complete Channel specification.
     * Specify the prefix for all Metadata keys.
     * The prefix must end with the path separator.
     * Populate the List of DataTypes with the types to be parsed for each item.
     * Return the number of channels for which valid Metadata were found, or 0 on error.
     * If a valid Temperature channel has been found,
     * add an entry KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE in the temperatureflag List.
     * The channel count returned INCLUDES the Temperature channel.
     * Return empty Lists if it is not possible to find a Channel.Count.
     *
     * @param metadatalist
     * @param keyprefix
     * @param listdatatypes
     * @param temperatureflag
     *
     * @return int
     */

    public static int getCaptureChannelCount(final List<Metadata> metadatalist,
                                             final String keyprefix,
                                             final List<DataTypeDictionary> listdatatypes,
                                             final List<String> temperatureflag)
        {
        final String SOURCE = "DataAnalyser.getCaptureChannelCount() ";
        int intDiscoveredChannelCount;

        intDiscoveredChannelCount = 0;

        // We must check for the presence of:
        // Observation.Channel.Count
        // where the count includes the slow speed channel, if present
        //
        // The slow speed Temperature channel, which is optional in the Metadata
        // Observation.Channel.Name.Temperature
        // Observation.Channel.DataType.Temperature
        // Observation.Channel.Units.Temperature
        // Observation.Channel.Description.Temperature
        //
        // The data channels  {0...maxchannel}
        // Observation.Channel.Name.n
        // Observation.Channel.DataType.n
        // Observation.Channel.Units.n
        // Observation.Channel.Description.n
        //
        // where n exists for all channels

        //MetadataHelper.showMetadataList(metadatalist, SOURCE);

        if ((metadatalist != null)
            && (!metadatalist.isEmpty())
            && (listdatatypes != null)
            && (temperatureflag != null))
            {
            Metadata metadata;

            // Make sure we only return valid DataTypes
            listdatatypes.clear();
            temperatureflag.clear();

            metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                       keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey());

            // Did we find the Observation.Channel.Count?
            if (metadata != null)
                {
                final int intDeclaredChannelCount;

                intDeclaredChannelCount = getDeclaredChannelCount(metadata);

                // Now look for data for each declared channel
                if (intDeclaredChannelCount > 0)
                    {
                    int intTemperatureChannelCount;
                    final int intDataChannelCount;

                    intTemperatureChannelCount = 0;

                    // Do we need to look for the slow-speed Temperature channel?
                    metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                               keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());
                    if (metadata != null)
                        {
                        // We found the Name, so the other items might be there
                        // Add the DataType if so
                        intTemperatureChannelCount = getTemperatureChannelCount(metadatalist,
                                                                                keyprefix,
                                                                                listdatatypes);

                        // Let the caller know that there's a Temperature channel to parse
                        if (intTemperatureChannelCount == 1)
                            {
                            temperatureflag.add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());
                            }
                        }

                    // If we discovered Temperature,
                    // then remove it from the declared count to search for data
                    intDataChannelCount = discoverChannelDataTypesAndCount(metadatalist,
                                                                           keyprefix,
                                                                           listdatatypes,
                                                                           intDeclaredChannelCount - intTemperatureChannelCount);

                    // Now check that the total number of channels found agrees with Channel.Count
                    if (intDeclaredChannelCount == (intTemperatureChannelCount + intDataChannelCount))
                        {
                        intDiscoveredChannelCount = intTemperatureChannelCount + intDataChannelCount;
                        }
                    else
                        {
                        // If not, we cannot continue...
                        LOGGER.error(SOURCE + "Incorrect amount of metadata for declared channels"
                                         + " [discovered=" + intDiscoveredChannelCount
                                         + "] [declared=" + intDeclaredChannelCount
                                         + "] [temperature=" + intTemperatureChannelCount
                                         + "] [data=" + intDataChannelCount + "]");

                        intDiscoveredChannelCount = 0;
                        listdatatypes.clear();
                        temperatureflag.clear();
                        }
                    }
                else
                    {
                    intDiscoveredChannelCount = 0;
                    listdatatypes.clear();
                    temperatureflag.clear();
                    LOGGER.error(SOURCE + "Unable to parse " + MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey());
                    }
                }
            else
                {
                intDiscoveredChannelCount = 0;
                listdatatypes.clear();
                temperatureflag.clear();
                LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey());
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to determine Raw Data Channel Count");
            }

        // A final check that it all worked
        if ((listdatatypes != null)
            && (intDiscoveredChannelCount != listdatatypes.size()))
            {
            intDiscoveredChannelCount = 0;
            listdatatypes.clear();
            LOGGER.error(SOURCE + "Mismatch between channels found and allocation of DataTypes");
            }

        return (intDiscoveredChannelCount);
        }


    /***********************************************************************************************
     * Get the DeclaredChannelCount from Metadata KEY_OBSERVATION_CHANNEL_COUNT.
     *
     *
     * @param metadata
     *
     * @return int
     */

    private static int getDeclaredChannelCount(final Metadata metadata)
        {
        final String SOURCE = "DataAnalyser.getDeclaredChannelCount() ";
        int intDeclaredChannelCount;

        try
            {
            final String strChannelCount;

            // Find out how many channels were declared
            // The Observation.Channel.Count DataType is always DecimalInteger
            strChannelCount = metadata.getValue();
            intDeclaredChannelCount = Integer.parseInt(strChannelCount);
            }

        catch (NumberFormatException exception)
            {
            intDeclaredChannelCount = 0;
            LOGGER.error(SOURCE + "Unable to parse value of Key=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey());
            }

        return (intDeclaredChannelCount);
        }


    /***********************************************************************************************
     * Get the number of valid Data Channels found in the Metadata.
     * Add DataTypes for the valid Channels.
     * Return the Discovered Channel Count.
     *
     * @param metadatalist
     * @param keyprefix
     * @param listdatatypes
     * @param declareddatachannelcount
     *
     * @return int
     */

    private static int discoverChannelDataTypesAndCount(final List<Metadata> metadatalist,
                                                        final String keyprefix,
                                                        final List<DataTypeDictionary> listdatatypes,
                                                        final int declareddatachannelcount)
        {
        final String SOURCE = "DataAnalyser.discoverChannelDataTypesAndCount() ";
        Metadata metadata;
        int intDiscoveredDataChannelCount;
        boolean boolContinueSearching;

        intDiscoveredDataChannelCount = 0;
        boolContinueSearching = true;

        // Limit the search to the declared number of Data channels...
        for (int intChannelIndex = 0;
            ((boolContinueSearching) && (intChannelIndex < declareddatachannelcount));
            intChannelIndex++)
            {
            metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                       keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + intChannelIndex);

            // Did we find the Channel.Name?
            if (metadata != null)
                {
                metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                           keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + intChannelIndex);

                // Did we find the Channel.DataType?
                if (metadata != null)
                    {
                    final DataTypeDictionary dataType;

                    // Look up the DataType with the name given in the metadata
                    //System.out.println("LOOK FOR DATATYPE " + PREFIX_DATATYPE + KEY_DELIMITER + metadata.getValue());

                    dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(metadata.getValue());

                    if (dataType != null)
                        {
                        // Now look for channel Units
                        metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                                   keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + intChannelIndex);

                        // Did we find the Channel.Units?
                        if (metadata != null)
                            {
                            metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                                       keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + intChannelIndex);

                            // Did we find the Channel.Description?
                            if (metadata != null)
                                {
                                // Add the DataType at the next position
                                listdatatypes.add(dataType);

                                // We found one complete channel, look for another?
                                intDiscoveredDataChannelCount++;
                                }
                            else
                                {
                                // Not enough data for one channel, so abort
                                boolContinueSearching = false;
                                intDiscoveredDataChannelCount = 0;
                                LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + intChannelIndex);
                                }
                            }
                        else
                            {
                            // Not enough data for one channel, so abort
                            boolContinueSearching = false;
                            intDiscoveredDataChannelCount = 0;
                            LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS.getKey() + intChannelIndex);
                            }
                        }
                    else
                        {
                        boolContinueSearching = false;
                        intDiscoveredDataChannelCount = 0;
                        LOGGER.error(SOURCE + "DataType not found=" + metadata.getValue());
                        }
                    }
                else
                    {
                    // Not enough data for one channel, so abort
                    boolContinueSearching = false;
                    intDiscoveredDataChannelCount = 0;
                    LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE.getKey() + intChannelIndex);
                    }
                }
            else
                {
                // Not enough data for one channel, so abort
                boolContinueSearching = false;
                intDiscoveredDataChannelCount = 0;
                LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + intChannelIndex);
                }
            }

        return (intDiscoveredDataChannelCount);
        }


    /***********************************************************************************************
     * Determine if the specified Metadata contains a reference to a Temperature Channel.
     * This will be specified with a key Observation.Channel.Name.Temperature,
     * and so the specified List of Metadata should be of ObservationMetadata.
     *
     * @param metadatalist
     *
     * @return boolean
     */

    public static boolean hasTemperatureChannel(final List<Metadata> metadatalist)
        {
        final String SOURCE = "DataAnalyser.hasTemperatureChannel() ";
        boolean boolHasTemperatureChannel;

        boolHasTemperatureChannel = false;

        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            final Metadata metadata;

            // Do we need to look for the slow-speed Temperature channel?
            metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                       MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());
            if (metadata != null)
                {
                final int intTemperatureChannelCount;

                // We found the Name, so the other items might be there
                // Add the DataType, but ignore it
                intTemperatureChannelCount = getTemperatureChannelCount(metadatalist,
                                                                        EMPTY_STRING,
                                                                        new ArrayList<DataTypeDictionary>(1));

                // Let the caller know that there's a Temperature channel
                if (intTemperatureChannelCount == 1)
                    {
                    boolHasTemperatureChannel = true;
                    }
                }
            }

        return (boolHasTemperatureChannel);
        }


    /***********************************************************************************************
     * Indicate if there is a Temperature channel present.
     * Look for the Metadata Key Observation.Channel.Name.Temperature.
     *
     * @param temperaturelist
     *
     * @return boolean
     */

    public static boolean hasTemperatureChannelInList(final List<String> temperaturelist)
        {
        return ((temperaturelist != null)
                 && (!temperaturelist.isEmpty())
                 && (temperaturelist.contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey())));
        }


    /***********************************************************************************************
     * Validate the metadata for the slow-speed Temperature channel.
     * We should find only one Temperature channel.
     * Update the List of DataTypes with the Temperature Type.
     * Return 0 if any item was missing or invalid.
     *
     * @param metadatalist
     * @param keyprefix
     * @param datatypeslist
     *
     * @return int
     */

    private static int getTemperatureChannelCount(final List<Metadata> metadatalist,
                                                  final String keyprefix,
                                                  final List<DataTypeDictionary> datatypeslist)
        {
        final String SOURCE = "DataAnalyser.getTemperatureChannelCount() ";
        Metadata metadata;
        int intTemperatureChannelCount;

        intTemperatureChannelCount = 0;

        metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                   keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());

        // Did we find the Channel.Name?
        if (metadata != null)
            {
            metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                       keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE_TEMPERATURE.getKey());

            // Did we find the Channel.DataType?
            if (metadata != null)
                {
                final DataTypeDictionary dataType;

                // Look up the DataType with the name given in the metadata
                dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(metadata.getValue());

                if (dataType != null)
                    {
                     // Now look for channel Units
                    metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                               keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS_TEMPERATURE.getKey());

                    // Did we find the Channel.Units?
                    if (metadata != null)
                        {
                        metadata = MetadataHelper.getMetadataByKey(metadatalist,
                                                                   keyprefix + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION_TEMPERATURE.getKey());

                        // Did we find the Channel.Description?
                        if (metadata != null)
                            {
                            // Add the DataType at the next position
                            datatypeslist.add(dataType);

                            // We found a valid Temperature channel
                            intTemperatureChannelCount++;
                            }
                        else
                            {
                            // Not enough data for a valid Temperature channel, so abort
                            intTemperatureChannelCount = 0;
                            LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION_TEMPERATURE.getKey());
                            }
                        }
                    else
                        {
                        // Not enough data for a valid Temperature channel, so abort
                        intTemperatureChannelCount = 0;
                        LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_UNITS_TEMPERATURE.getKey());
                        }
                    }
                else
                    {
                    intTemperatureChannelCount = 0;
                    LOGGER.error(SOURCE + "DataType not found=" + metadata.getValue());
                    }
                }
            else
                {
                // Not enough data for a valid Temperature channel, so abort
                intTemperatureChannelCount = 0;
                LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_DATA_TYPE_TEMPERATURE.getKey());
                }
            }
        else
            {
            // Not enough data for a valid Temperature channel, so abort
            intTemperatureChannelCount = 0;
            LOGGER.error(SOURCE + "Key not found=" + MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME_TEMPERATURE.getKey());
            }

        return (intTemperatureChannelCount);
        }


    /***********************************************************************************************
     * Indicate if the supplied RawData represents Calendarised columns.
     *
     * @param data
     *
     * @return boolean
     */

    public static boolean isCalendarisedRawData(final Vector<Object> data)
        {
        return ((data != null)
                && (data.size() > 0)
                && (data.get(0) != null)
                && (data.get(0) instanceof Vector)
                && (((Vector)data.get(0)).size() > 0)
                && (((Vector)data.get(0)).get(0) instanceof Calendar));
        }


    /***********************************************************************************************
     * Indicate if the supplied RawData represents plain columnar data.
     *
     * @param data
     *
     * @return boolean
     */

    public static boolean isColumnarRawData(final Vector<Object> data)
        {
        return ((data != null)
                && (data.size() > 0)
                && (data.get(0) != null)
                && (data.get(0) instanceof Vector)
                && (((Vector)data.get(0)).size() > 0)
                && (!(((Vector)data.get(0)).get(0) instanceof Calendar)));
        }


    /***********************************************************************************************
     * Indicate if the supplied ProcessedData represents a TimeSeriesCollection
     * with at least one TimeSeries.
     *
     * @param data
     *
     * @return boolean
     */

    public static boolean isTimeSeriesProcessedData(final XYDataset data)
        {
        return ((data != null)
                && (data.getSeriesCount() > 0)
                && (data instanceof TimeSeriesCollection)
                && (((TimeSeriesCollection)data).getSeries() != null)
                && (((TimeSeriesCollection)data).getSeries().get(0) != null));
        }



    /***********************************************************************************************
     * Indicate if the supplied ProcessedData represents an XYSeries.
     *
     * @param data
     *
     * @return boolean
     */

    public static boolean isXYSeriesProcessedData(final XYDataset data)
        {
        return ((data != null)
                && (data.getSeriesCount() > 0)
                && (data instanceof XYSeriesCollection));
        }
    }
