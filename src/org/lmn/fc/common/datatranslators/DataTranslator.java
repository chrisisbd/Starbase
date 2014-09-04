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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * The DataTranslator base class for all DataTranslators.
 */

public abstract class DataTranslator implements DataTranslatorInterface,
                                                FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons
    {
    //---------------------------------------------------------------------------------------------
    // Metadata, RawData and Image, and their corresponding Metadata,
    // which are the only things the DataTranslator can produce

    // Metadata containers
    private final List<Metadata> listMetadataMetadata;
    private final List<Metadata> listFrameworkMetadata;
    private final List<Metadata> listObservatoryMetadata;
    private final List<Metadata> listObserverMetadata;
    private final List<Metadata> listObservationMetadata;
    private final List<Metadata> listInstrumentMetadata;
    private final List<Metadata> listControllerMetadata;
    private final List<Metadata> listPluginMetadata;
    private final List<Metadata> listRawDataMetadata;
    private final List<Metadata> listXYDatasetMetadata;

    // RawData
    private final Vector<Object> vecRawData;

    // Image
    private Image image;

    //---------------------------------------------------------------------------------------------

    // Information derived by analysis of the translated data
    private int intChannelCount;
    private boolean boolHasTemperatureChannel;

    // Miscellaneous
    private final List<String> listMessages;
    private long longImportCount;
    private long longExportCount;


    /************************************************************************************************
    * Construct a DataTranslator.
    */

    public DataTranslator()
        {
        // Initialise all MetaData containers
        this.listFrameworkMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listObservatoryMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listObserverMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listMetadataMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listObservationMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listInstrumentMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listControllerMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listPluginMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listRawDataMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);
        this.listXYDatasetMetadata = new ArrayList<Metadata>(INITIAL_CAPACITY);

        this.vecRawData = new Vector<Object>(10000);
        this.image = null;

        this.intChannelCount = 0;
        this.boolHasTemperatureChannel = false;

        this.listMessages = new ArrayList<String>(INITIAL_CAPACITY);
        this.longImportCount = 0L;
        this.longExportCount = 0L;
        }


    /***********************************************************************************************
     * Initialise the Translator.
     */

    public void initialiseTranslator()
        {
        clearMetadataMetadata();
        clearMetadata();

        clearRawData();
        clearImage();

        setRawDataChannelCount(0);
        setTemperatureChannel(false);

        clearMessages();
        setImportedCount(0);
        setExportedCount(0);
        }


    /**********************************************************************************************/
    /* Importers                                                                                  */
    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public abstract boolean importRawData(String filename,
                                          Vector<Vector> eventlog,
                                          ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Get the number of bytes imported by the last RawData import operation.
     *
     * @return
     */

    public long getImportedCount()
        {
        return (this.longImportCount);
        }


    /***********************************************************************************************
     * Set the number of bytes imported by the last RawData import operation.
     *
     * @param count
     */

    public void setImportedCount(final long count)
        {
        this.longImportCount = count;
        }


    /***********************************************************************************************
     * Import an Image.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean importImage(final String filename,
                               final Vector<Vector> eventlog,
                               final ObservatoryClockInterface clock)
        {
        // Override where needed
        return (false);
        }


    /**********************************************************************************************/
    /* Exporters                                                                                  */
    /***********************************************************************************************
     * exportXYDataset().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public abstract boolean exportXYDataset(DAOWrapperInterface wrapper,
                                            String filename,
                                            boolean timestamp,
                                            Vector<Vector> eventlog,
                                            ObservatoryClockInterface clock);


    /***********************************************************************************************
     * exportRawData().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public abstract boolean exportRawData(DAOWrapperInterface wrapper,
                                          String filename,
                                          boolean timestamp,
                                          Vector<Vector> eventlog,
                                          ObservatoryClockInterface clock);


    /***********************************************************************************************
     * exportMetadata().
     *
     * @param metadatametadata
     * @param metadata
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     * @return boolean
     */

    public abstract boolean exportMetadata(List<Metadata> metadatametadata,
                                           List<Metadata> metadata,
                                           String filename,
                                           boolean timestamp,
                                           Vector<Vector> eventlog,
                                           ObservatoryClockInterface clock);


    /***********************************************************************************************
     * exportLog().
     *
     * @param logmetadata
     * @param logdata
     * @param logwidth
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public abstract boolean exportLog(List<Metadata> logmetadata,
                                      Vector<Vector> logdata,
                                      int logwidth,
                                      String filename,
                                      boolean timestamp,
                                      Vector<Vector> log,
                                      ObservatoryClockInterface clock);


    /***********************************************************************************************
     * exportConfiguration().
     *
     * @param configmetadata
     * @param instrument
     * @param extraconfig
     * @param parentresourcekey
     * @param resourcekey
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public abstract boolean exportConfiguration(List<Metadata> configmetadata,
                                                Instrument instrument,
                                                Vector<Vector> extraconfig,
                                                String parentresourcekey,
                                                String resourcekey,
                                                String filename,
                                                boolean timestamp,
                                                Vector<Vector> eventlog,
                                                ObservatoryClockInterface clock);


    /***********************************************************************************************
     * exportReportTable().
     *
     * @param metadatametadata
     * @param metadata
     * @param infercolumnmetadata
     * @param datasettype
     * @param report
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public abstract boolean exportReportTable(List<Metadata> metadatametadata,
                                              List<Metadata> metadata,
                                              boolean infercolumnmetadata,
                                              DatasetType datasettype,
                                              ReportTablePlugin report,
                                              String filename,
                                              boolean timestamp,
                                              Vector<Vector> eventlog,
                                              ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Get the number of bytes exported by the last export operation.
     *
     * @return
     */

    public long getExportedCount()
        {
        return (this.longExportCount);
        }


    /***********************************************************************************************
     * Set the number of bytes exported by the last export operation.
     *
     * @param count
     */

    public void setExportedCount(final long count)
        {
        this.longExportCount = count;
        }


    /**********************************************************************************************/
    /* MetadataMetadata                                                                           */
    /***********************************************************************************************
     * Get the Metadata Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getMetadataMetadata()
        {
        return (this.listMetadataMetadata);
        }


    /***********************************************************************************************
     * Add all MetadataMetadata items to the DataTranslator, if possible.
     *
     * @param metadatalist
     */

    public void addAllMetadataMetadata(final List<Metadata> metadatalist)
        {
        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            final Iterator<Metadata> iterMetadata;

            iterMetadata = metadatalist.iterator();

            while (iterMetadata.hasNext())
                {
                final Metadata metadata;

                metadata = iterMetadata.next();

                MetadataHelper.addOrUpdateMetadataItemTraced(getMetadataMetadata(),
                                                             metadata,
                                                             "DataTranslator.addAllMetadataMetadata()",
                                                             LOADER_PROPERTIES.isMetadataDebug());
                }
            }
        }


    /***********************************************************************************************
     * Add MetadataMetadata to the DataTranslator.
     *
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     *
     * @return Metadata
     */

    public final Metadata addMetadataMetadata(final String key,
                                              final String value,
                                              final String regex,
                                              final DataTypeDictionary datatype,
                                              final SchemaUnits.Enum units,
                                              final String description)
        {
        final Metadata metaData;

        // There is only one container where this can go...
        metaData = MetadataHelper.addNewMetadata(getMetadataMetadata(),
                                                 key,
                                                 value,
                                                 regex,
                                                 datatype,
                                                 units,
                                                 description);
        return (metaData);
        }


    /***********************************************************************************************
     * Clear the List of MetadataMetadata.
     */

    public final void clearMetadataMetadata()
        {
        getMetadataMetadata().clear();
        }


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the List of Metadata for the Framework.
     *
     * @return List<Metadata>
     */

    public final List<Metadata> getFrameworkMetadata()
        {
        return (this.listFrameworkMetadata);
        }


    /***********************************************************************************************
     * Get the ObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObservatoryMetadata()
        {
        return (this.listObservatoryMetadata);
        }


    /***********************************************************************************************
     * Get the ObserverMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObserverMetadata()
        {
        return (this.listObserverMetadata);
        }


    /***********************************************************************************************
     * Get the ObservationMetadata.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getObservationMetadata()
        {
        return (this.listObservationMetadata);
        }


    /***********************************************************************************************
     * Get the InstrumentMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentMetadata()
        {
        return (this.listInstrumentMetadata);
        }


    /***********************************************************************************************
     * Get the ControllerMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getControllerMetadata()
        {
        return (this.listControllerMetadata);
        }


    /***********************************************************************************************
     * Get the PluginMetadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getPluginMetadata()
        {
        return (this.listPluginMetadata);
        }


    /***********************************************************************************************
     * Get the RawData Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getRawDataMetadata()
        {
        return (this.listRawDataMetadata);
        }


    /***********************************************************************************************
     * Get the XYDataset Metadata List.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getXYDatasetMetadata()
        {
        return (this.listXYDatasetMetadata);
        }


    /***********************************************************************************************
     * Add all Metadata items in a List to the most appropriate Metadata container
     * in the DataTranslator, if possible.
     *
     * @param metadatalist
     */

    public void addOrUpdateAllMetadataToContainers(final List<Metadata> metadatalist)
        {
        final String SOURCE = "DataTranslator.addOrUpdateAllMetadataToContainers() ";

        if ((metadatalist != null)
            && (!metadatalist.isEmpty()))
            {
            final Iterator<Metadata> iterMetadata;

            iterMetadata = metadatalist.iterator();

            while (iterMetadata.hasNext())
                {
                final Metadata metadata;
                final List<Metadata> listMetadataContainer;

                metadata = iterMetadata.next();

                listMetadataContainer = DataTranslatorHelper.findMetadataContainerByKey(this, metadata.getKey());

                if (listMetadataContainer != null)
                    {
                    MetadataHelper.addOrUpdateMetadataItemTraced(listMetadataContainer,
                                                                 metadata,
                                                                 SOURCE,
                                                                 LOADER_PROPERTIES.isMetadataDebug());
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add Metadata to the most appropriate Metadata container in the DataTranslator.
     * Do nothing if no suitable container is found.
     *
     * @param key
     * @param value
     * @param regex
     * @param datatype
     * @param units
     * @param description
     *
     * @return Metadata
     */

    public final void addMetadataToContainer(final String key,
                                             final String value,
                                             final String regex,
                                             final DataTypeDictionary datatype,
                                             final SchemaUnits.Enum units,
                                             final String description)
        {
        final List<Metadata> listMetadata;

        listMetadata = DataTranslatorHelper.findMetadataContainerByKey(this, key);

        if (listMetadata != null)
            {
            MetadataHelper.addNewMetadata(listMetadata,
                                          key,
                                          value,
                                          regex,
                                          datatype,
                                          units,
                                          description);
            }
        }


    /***********************************************************************************************
     * Clear all Metadata containers (but not MetadataMetadata).
     */

    public void clearMetadata()
        {
        getFrameworkMetadata().clear();
        getObservatoryMetadata().clear();
        getObserverMetadata().clear();
        getObservationMetadata().clear();
        getInstrumentMetadata().clear();
        getControllerMetadata().clear();
        getPluginMetadata().clear();
        getRawDataMetadata().clear();
        getXYDatasetMetadata().clear();
        }


    /**********************************************************************************************/
    /* RawData                                                                                    */
    /***********************************************************************************************
     * Get the RawData translated by this DataTranslator.
     *
     * @return Vector
     */

    public final Vector<Object> getRawData()
        {
        return (this.vecRawData);
        }


    /***********************************************************************************************
     * Get a data sample from the RawData.
     *
     * @param index
     *
     * @return Vector
     */

    public final Vector<Object> getRawDataSample(final int index)
        {
        if ((index >= 0)
            && (getRawData() != null)
            && (getRawData().get(index) != null)
            && (getRawData().get(index) instanceof Vector))
            {
            return ((Vector<Object>) getRawData().get(index));
            }
        else
            {
            return (null);
            }
        }


    /***********************************************************************************************
     * Add a data sample to the RawData.
     *
     * @param sample
     */

    public final void addRawDataSample(final Vector<Object> sample)
        {
        if ((getRawData() != null)
            && (sample != null))
            {
            getRawData().add(sample);
            }
        }


    /***********************************************************************************************
     * Clear the RawData.
     */

    public final void clearRawData()
        {
        getRawData().clear();
        }


    /***********************************************************************************************
     * Get the RawData ChannelCount for this DataTranslator.
     *
     * @return int
     */

    public int getRawDataChannelCount()
        {
        return (this.intChannelCount);
        }


    /***********************************************************************************************
     * Set the RawData ChannelCount for this DataTranslator.
     * The ChannelCount does not include the Calendar in the case of timestamped data,
     * or the index (X) column for XY data.
     *
     * @param count
     */

    public void setRawDataChannelCount(final int count)
        {
        this.intChannelCount = count;
        }


    /***********************************************************************************************
     * Get the TemperatureChannel indicator for this DataTranslator.
     *
     * @return boolean
     */

    public boolean hasTemperatureChannel()
        {
        return (this.boolHasTemperatureChannel);
        }


    /***********************************************************************************************
     * Set the TemperatureChannel indicator for this DataTranslator.
     *
     * @param flag
     */

    public void setTemperatureChannel(final boolean flag)
        {
        this.boolHasTemperatureChannel = flag;
        }


    /***********************************************************************************************
     * Check that the number of RawData channels has not changed since it was established.
     *
     * @param tokens
     * @param linenumber
     * @param linecontext
     * @param inferredcount
     * @param timestamped
     * @param row
     *
     * @return boolean
     */

    public boolean checkRawDataChannelCount(final String[] tokens,
                                            final int linenumber,
                                            final String linecontext,
                                            final boolean inferredcount,
                                            final boolean timestamped,
                                            final Vector<Object> row)
        {
        boolean boolInferred;

        boolInferred = inferredcount;

        // Now establish the channel count for the duration of the import
        // Do this only once
        if (!boolInferred)
            {
            if (timestamped)
                {
                setRawDataChannelCount(tokens.length - 2);
                }
            else
                {
                setRawDataChannelCount(tokens.length - 1);
                }

            // Don't come back here...
            boolInferred = true;
            }

        // Double check that the channel count hasn't changed this time round
        if (((timestamped) && (getRawDataChannelCount() == (tokens.length - 2)))
            || ((!timestamped) && (getRawDataChannelCount() == (tokens.length - 1))))
            {
            // This sample is added only if we don't get any ParseExceptions
            addRawDataSample(row);
            }
        else
            {
            addMessage(METADATA_TARGET_RAWDATA
                           + METADATA_ACTION_IMPORT
                           + METADATA_RESULT + "Incorrect number of data columns - expected " + getRawDataChannelCount() + TERMINATOR + SPACE
                           + METADATA_LINE + linenumber+ TERMINATOR + SPACE
                           + METADATA_CONTEXT + linecontext + TERMINATOR);
            }

        return (boolInferred);
        }


    /**********************************************************************************************/
    /* Images                                                                                     */
    /***********************************************************************************************
     * Get the Image from the Translator.
     *
     * @return Image
     */

    public Image getImage()
        {
        return (this.image);
        }


    /***********************************************************************************************
     * Set the Image.
     *
     * @param img
     */

    public void setImage(final Image img)
        {
        this.image = img;
        }


    /***********************************************************************************************
     * Clear the Image.
     */

    public void clearImage()
        {
        this.image = null;
        }


    /**********************************************************************************************/
    /* Messages                                                                                   */
    /***********************************************************************************************
     * Get the messages prepared by the DataTranslator.
     *
     * @return Vector<String>
     */

    public final List<String> getMessages()
        {
        return (this.listMessages);
        }


    /***********************************************************************************************
     * Add a message to the list of messages.
     *
     * @param message
     */

    public void addMessage(final String message)
        {
        getMessages().add(message);
        }


    /***********************************************************************************************
     * Clear the List of Messages.
     */

    public final void clearMessages()
        {
        getMessages().clear();
        }
    }
