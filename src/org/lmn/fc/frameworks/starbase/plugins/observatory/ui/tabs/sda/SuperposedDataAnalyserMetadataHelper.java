// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.sda;


import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.ChartUIHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.widgets.ControlKnobInterface;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


/***************************************************************************************************
 * SuperposedDataAnalyserMetadataHelper.
 */

public final class SuperposedDataAnalyserMetadataHelper implements FrameworkStrings,
                                                                   FrameworkMetadata,
                                                                   FrameworkRegex,
                                                                   FrameworkSingletons
    {
    // String Resources
    private static final String DATASET_CHANNEL_PREFIX = "[";
    private static final String DATASET_CHANNEL_SEPARATOR = COMMA;
    private static final String DATASET_CHANNEL_SUFFIX = "]";
    private static final char CHAR_DELTA = '\u0394';


    /***********************************************************************************************
     * Create the Metadata to describe the Composite Chart.
     *
     * Observation.Title
     * Observation.Axis.Label.X
     * Observation.Axis.Label.Y.n
     * Observation.Channel.Count
     *
     * @param title
     * @param datasettype
     * @param channelcount
     * @param debug
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createCompositeChartMetadata(final String title,
                                                              final DatasetType datasettype,
                                                              final int channelcount,
                                                              final boolean debug)
        {
        final String SOURCE = "SuperposedDataAnalyserMetadataHelper.createCompositeChartMetadata() ";
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(9);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      title,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Title of the composite chart");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      "Signal Level",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The label of the Y axis of the composite chart");

        switch (datasettype)
            {
            case TABULAR:
                {
                MetadataHelper.addNewMetadata(listMetadata,
                                              MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                              "X Value",
                                              REGEX_STRING,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "The label of the X axis of the composite chart");

                break;
                }

            case TIMESTAMPED:
                {
                MetadataHelper.addNewMetadata(listMetadata,
                                              MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                              "Time",
                                              REGEX_STRING,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "The label of the X (time) axis of the composite chart");

                break;
                }

            case XY:
                {
                MetadataHelper.addNewMetadata(listMetadata,
                                              MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                              "Index",
                                              REGEX_STRING,
                                              DataTypeDictionary.STRING,
                                              SchemaUnits.DIMENSIONLESS,
                                              "The label of the X (index) axis of the composite chart");

                break;
                }
            }

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
                                      Integer.toString(channelcount),
                                      REGEX_SIGNED_DECIMAL_INTEGER,
                                      DataTypeDictionary.DECIMAL_INTEGER,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The number of Channels on the composite chart");

        MetadataHelper.showMetadataList(listMetadata,
                                        "Superposed Data Analyser Composite Chart Metadata",
                                        debug);

        return (listMetadata);
        }


    /***********************************************************************************************
     * Add any missing Metadata to describe the Composite Chart.
     * If the Metadata item is already present in the specified list,
     * update the ChannelCount to the specified new value.
     *
     * Observation.Title
     * Observation.Axis.Label.X
     * Observation.Axis.Label.Y.n
     * Observation.Channel.Count
     *
     * @param metadatalist
     * @param datasettype
     * @param datasettypename
     * @param newchannelcount
     * @param debug
     */

    public static void addCompositeChartMissingMetadata(final List<Metadata> metadatalist,
                                                        final DatasetType datasettype,
                                                        final String datasettypename,
                                                        final int newchannelcount,
                                                        final boolean debug)
        {
        final String SOURCE = "SuperposedDataAnalyserMetadataHelper.addCompositeChartMissingMetadata() ";
        Metadata metadataItem;

        metadataItem = MetadataHelper.getMetadataByKey(metadatalist,
                                                       MetadataDictionary.KEY_OBSERVATION_TITLE.getKey());
        if (metadataItem == null)
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            switch (datasettype)
                {
                case TABULAR:
                    {
                    buffer.append("Superposed Data Analysis ");
                    buffer.append(datasettypename);
                    break;
                    }

                case TIMESTAMPED:
                    {
                    buffer.append("Superposed Epoch Analysis ");
                    buffer.append(datasettypename);
                    break;
                    }

                case XY:
                    {
                    buffer.append("Superposed Data Analysis ");
                    buffer.append(datasettypename);
                    break;
                    }
                }

            // New Metadata with the Title
            MetadataHelper.addNewMetadata(metadatalist,
                                          MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                          buffer.toString(),
                                          REGEX_STRING,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The Title of the composite chart");
            }
        else
            {
            // Leave the existing Title alone
            }

        metadataItem = MetadataHelper.getMetadataByKey(metadatalist,
                                                       (MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO));
        if (metadataItem == null)
            {
            MetadataHelper.addNewMetadata(metadatalist,
                                          MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                          "Signal Level",
                                          REGEX_STRING,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The label of the Y axis of the composite chart");
            }
        else
            {
            // Leave the Axis Label alone
            }

        metadataItem = MetadataHelper.getMetadataByKey(metadatalist,
                                                       MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey());
        if (metadataItem == null)
            {
            switch (datasettype)
                {
                case TABULAR:
                    {
                    MetadataHelper.addNewMetadata(metadatalist,
                                                  MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                                  "X Value",
                                                  REGEX_STRING,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  "The label of the X axis of the composite chart");

                    break;
                    }

                case TIMESTAMPED:
                    {
                    MetadataHelper.addNewMetadata(metadatalist,
                                                  MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                                  "Time",
                                                  REGEX_STRING,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  "The label of the X (time) axis of the composite chart");

                    break;
                    }

                case XY:
                    {
                    MetadataHelper.addNewMetadata(metadatalist,
                                                  MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                                  "Index",
                                                  REGEX_STRING,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  "The label of the X (index) axis of the composite chart");

                    break;
                    }
                }
            }
        else
            {
            // Leave the Axis Label alone
            }

        metadataItem = MetadataHelper.getMetadataByKey(metadatalist,
                                                       MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey());
        if (metadataItem == null)
            {
            MetadataHelper.addNewMetadata(metadatalist,
                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
                                          Integer.toString(newchannelcount),
                                          REGEX_SIGNED_DECIMAL_INTEGER,
                                          DataTypeDictionary.DECIMAL_INTEGER,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The number of Channels on the composite chart");
            }
        else
            {
            // Update the existing Metadata Value for the ChannelCount
            metadataItem.setValue(Integer.toString(newchannelcount));
            }

        MetadataHelper.showMetadataList(metadatalist,
                                        "Superposed Data Analyser Chart Metadata",
                                        debug);
        }


    /***********************************************************************************************
     * Add all of the specified DAO's Metadata appropriate for a Chart to the Composite collection,
     * renaming any Observation.Channel Keys starting at the current CompositeChannelCount,
     * to ensure all Channels of the Composite are unique.
     *
     * Uses:
     *      ObservationMetadata
     *      InstrumentMetadata
     *      ControllerMetadata
     *      PluginMetadata
     *      RawDataMetadata
     *      XYDatasetMetadata
     *
     * @param dao
     * @param daoindex
     * @param offset
     * @param compositecount
     * @param debug
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createRenamedMetadataForChartFromDAO(final ObservatoryInstrumentDAOInterface dao,
                                                                      final int daoindex,
                                                                      final double offset,
                                                                      final int compositecount,
                                                                      final boolean debug)
        {
        final String SOURCE = "SuperposedDataAnalyserMetadataHelper.collectAndRenameChannelMetadataFromAttachedDAO() ";
        final List<Metadata> listOriginalMetadata;
        final List<Metadata> listRenamedMetadata;

        listOriginalMetadata = MetadataHelper.collectMetadataForChartFromDAO(dao);
        listRenamedMetadata = new ArrayList<Metadata>(listOriginalMetadata.size());

        for (int intMetadataIndex = 0;
             intMetadataIndex < listOriginalMetadata.size();
             intMetadataIndex++)
            {
            final Metadata mdOriginal;

            mdOriginal = listOriginalMetadata.get(intMetadataIndex);

            // Rename only the Observation.Channel Metadata
            // Make sure we ignore Observation.Channel.Count
            // Discard everything not related to a Channel?
            if ((mdOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_ROOT.getKey()))
                && (MetadataDictionary.isLastKeyTokenChannelId(mdOriginal.getKey())))
                {
                try
                    {
                    final StringBuffer bufferNewKey;
                    final Metadata mdRenamed;
                    final int intOriginalChannelIndex;
                    final int intRenamedChannelIndex;

                    bufferNewKey = new StringBuffer();
                    bufferNewKey.append(mdOriginal.getKey().substring(0, mdOriginal.getKey().lastIndexOf(DOT)));
                    bufferNewKey.append(DOT);

                    // Find the original Channel ID
                    // We know the Key must end with a dot and a Channel ID, and nothing else
                    intOriginalChannelIndex = Integer.parseInt(mdOriginal.getKey().substring(mdOriginal.getKey().lastIndexOf(DOT) + 1));
                    intRenamedChannelIndex = intOriginalChannelIndex + compositecount;

                    bufferNewKey.append(intRenamedChannelIndex);

                    // Be sure we have a new instance of Metadata, not a reference to the original
                    mdRenamed = MetadataHelper.createMetadata(bufferNewKey.toString(),
                                                              mdOriginal.getValue(),
                                                              mdOriginal.getRegex(),
                                                              DataTypeDictionary.getDataTypeDictionaryEntryForName(mdOriginal.getDataTypeName().toString()),
                                                              mdOriginal.getUnits(),
                                                              mdOriginal.getDescription());

                    if (mdOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey()))
                        {
                        // [dao,channel] name (offset +1234)
                        mdRenamed.setValue(buildChannelName(daoindex,
                                                            intOriginalChannelIndex,
                                                            mdOriginal.getValue(),
                                                            offset));
                        }
                    else if (mdOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey()))
                        {
                        // Ensure all Channels have unique colours
                        mdRenamed.setValue(ChartUIHelper.getStandardColour(intRenamedChannelIndex).toString());
                        }

                    listRenamedMetadata.add(mdRenamed);

                    MetadataHelper.showMetadata(mdRenamed,
                                                "Renamed Channel [key=" + bufferNewKey.toString() + "]",
                                                debug);
                    }

                catch (final NumberFormatException exception)
                    {
                    LOGGER.error(SOURCE + "Unable to identify Channel number [channelid=" + mdOriginal.getKey() + "]");
                    }
                }
            }

        return (listRenamedMetadata);
        }


    /***********************************************************************************************
     * Add any Channel Metadata from the specified DAO appropriate for a Chart
     * which is *missing* from the specified Composite collection,
     * renaming any Observation.Channel Keys starting at the current CompositeChannelCount,
     * to ensure all Channels of the Composite have unique ChannelIDs.
     * Do nothing if the Metadata Key is already present in the Composite collection,
     * since we wish to preserve any edits by the User.
     *
     * Collect:
     *      Channel.Name
     *      Channel.Colour
     *      Channel.Description
     *
     * Uses:
     *      ObservationMetadata
     *      InstrumentMetadata
     *      ControllerMetadata
     *      PluginMetadata
     *      RawDataMetadata
     *      XYDatasetMetadata
     *
     * @param metadatalist
     * @param attacheddao
     * @param daoindex
     * @param channelidoffset
     * @param datasetoffset
     * @param event
     * @param debug
     */

    public static void collectAndRenameChannelMetadataFromAttachedDAO(final List<Metadata> metadatalist,
                                                                      final ObservatoryInstrumentDAOInterface attacheddao,
                                                                      final int daoindex,
                                                                      final int channelidoffset,
                                                                      final double datasetoffset,
                                                                      final EventObject event,
                                                                      final boolean debug)
        {
        final String SOURCE = "SuperposedDataAnalyserMetadataHelper.collectAndRenameChannelMetadataFromAttachedDAO() ";
        final List<Metadata> listAttachedDAOMetadata;

        // Collect only from ObservationMetadata downwards
        listAttachedDAOMetadata = MetadataHelper.collectMetadataForChartFromDAO(attacheddao);

        // Traverse all Metadata in the AttachedDAO
        for (int intMetadataIndex = 0;
             intMetadataIndex < listAttachedDAOMetadata.size();
             intMetadataIndex++)
            {
            final Metadata mdAttachedOriginal;

            mdAttachedOriginal = listAttachedDAOMetadata.get(intMetadataIndex);

            // Discard everything not related to a Channel
            // Rename only the Observation.Channel Metadata
            // Make sure we ignore Observation.Channel.Count
            if ((mdAttachedOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_ROOT.getKey()))
                && (MetadataDictionary.isLastKeyTokenChannelId(mdAttachedOriginal.getKey())))
                {
                try
                    {
                    final StringBuffer bufferRenamedKey;
                    final int intOriginalChannelIndex;
                    final int intRenamedChannelIndex;
                    Metadata mdRenamed;

                    // Form the renamed Key to be used in the Composite set of Metadata
                    bufferRenamedKey = new StringBuffer();
                    bufferRenamedKey.append(mdAttachedOriginal.getKey().substring(0, mdAttachedOriginal.getKey().lastIndexOf(DOT)));
                    bufferRenamedKey.append(DOT);

                    // Find the original Channel ID
                    // We know the Key must end with a dot and a Channel ID, and nothing else
                    intOriginalChannelIndex = Integer.parseInt(mdAttachedOriginal.getKey().substring(mdAttachedOriginal.getKey().lastIndexOf(DOT) + 1));

                    // Offset the ChannelID from the specified starting value
                    intRenamedChannelIndex = intOriginalChannelIndex + channelidoffset;

                    bufferRenamedKey.append(intRenamedChannelIndex);

                    // See if a Metadata Item with that Key already exists in the Composite set
                    mdRenamed = MetadataHelper.getMetadataByKey(metadatalist,
                                                                bufferRenamedKey.toString());
                    if (mdRenamed == null)
                        {
                        // The Key does not exist in the Composite collection, so create the item
                        // Be sure we have a new instance of Metadata, not a reference to the original in the Attached DAO
                        mdRenamed = MetadataHelper.createMetadata(bufferRenamedKey.toString(),
                                                                  mdAttachedOriginal.getValue(),
                                                                  mdAttachedOriginal.getRegex(),
                                                                  DataTypeDictionary.getDataTypeDictionaryEntryForName(mdAttachedOriginal.getDataTypeName().toString()),
                                                                  mdAttachedOriginal.getUnits(),
                                                                  mdAttachedOriginal.getDescription());

                        // Adjust the Values of specific items from those given by the AttachedDAO
                        if (mdAttachedOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey()))
                            {
                            // [dao,channel] name (offset +1234)
                            mdRenamed.setValue(buildChannelName(daoindex,
                                                                intOriginalChannelIndex,
                                                                mdAttachedOriginal.getValue(),
                                                                datasetoffset));
                            }
                        else if (mdAttachedOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_COLOUR.getKey()))
                            {
                            // Ensure all Channels have unique colours (to begin with)
                            mdRenamed.setValue(ChartUIHelper.getStandardColour(intRenamedChannelIndex).toString());
                            }

                        metadatalist.add(mdRenamed);
                        }
                    else
                        {
                        // The Metadata Key already exists, so do nothing to preserve edits,
                        // unless the Event source is a ControlKnob, in which case rebuild the ChannelNames to show the offsets
                        if ((mdAttachedOriginal.getKey().contains(MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey()))
                            && (event != null)
                            && (event.getSource() instanceof ControlKnobInterface))
                            {
                            // [dao,channel] name (offset +1234)
                            mdRenamed.setValue(buildChannelName(daoindex,
                                                                intOriginalChannelIndex,
                                                                mdAttachedOriginal.getValue(),
                                                                datasetoffset));

                            MetadataHelper.showMetadata(mdRenamed,
                                                        "The Metadata Key already exists, but Event source is a ControlKnob [offset=" + datasetoffset
                                                            + "] [key=" + mdRenamed.getKey()
                                                            + "] [value.new=" + mdRenamed.getValue() + "]",
                                                        debug);

                            }
                        else
                            {
                            MetadataHelper.showMetadata(mdRenamed,
                                                        "The Metadata Key already exists, do nothing so as to preserve any edits"
                                                            + " [key=" + mdRenamed.getKey()
                                                            + "] [value=" + mdRenamed.getValue() + "]",
                                                        debug);
                            }
                        }

                    MetadataHelper.showMetadata(mdRenamed,
                                                "Renamed Channel [metadata.index=" + intMetadataIndex
                                                    + "] [key=" + bufferRenamedKey.toString() + "]",
                                                debug);
                    }

                catch (final NumberFormatException exception)
                    {
                    LOGGER.error(SOURCE + "Unable to identify Channel number [key=" + mdAttachedOriginal.getKey() + "]");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Build the revised Channel name, given a DAO and Channel.
     *
     * [dao,channel] name (offset +1234)
     *
     * @param daoindex
     * @param channelindex
     * @param name
     * @param offset
     *
     * @return String
     */

    public static String buildChannelName(final int daoindex,
                                          final int channelindex,
                                          final Comparable<String> name,
                                          final double offset)
        {
        return (DATASET_CHANNEL_PREFIX
                    + daoindex
                    + DATASET_CHANNEL_SEPARATOR
                    + channelindex
                    + DATASET_CHANNEL_SUFFIX + SPACE
                    + name
                    + " (offset "
//                    + " ("
//                    + CHAR_DELTA    This messes up the Metadata Regex! :-(
//                    + " "
                    + SuperposedDataAnalyserHelper.PATTERN_OFFSET.format(offset)
                    + "%)");
        }
    }
