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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.awt.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DataTranslatorInterface.
 */

public interface DataTranslatorInterface extends FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 FrameworkRegex,
                                                 ResourceKeys,
                                                 AstronomyConstants
    {
    // String Resources
    String MSG_BYTES_EXPORTED = " bytes exported successfully";
    String MSG_SAMPLES_TRANSLATED = " samples translated successfully";
    String MSG_PARSE_DATE_OR_TIME = "Failed to parse Date or Time";
    String MSG_PARSE_INTEGER = "Failed to parse Integer";

    String DEFAULT_TIME_ZONE = "GMT+00:00";

    String PREFIX_COMMENT = "#";

    String DATE_PARSE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    String ERROR_INVALID_DATASET = "The XYDataset is invalid";
    String ERROR_INVALID_DATASET_TYPE = "The Dataset Type is invalid";
    String ERROR_SERIES_COUNT = "The Dataset does not have the correct number of Series";
    String ERROR_XYDATASET = "There is no XYDataset available";
    String ERROR_RAW_DATA = "There are no Raw Data available";
    String ERROR_EVENT_LOG = "There is no EventLog available";
    String ERROR_REPORT = "There is no Report available";
    String ERROR_METADATA = "There are no Metadata available";
    String ERROR_CONFIG = "There is no Configuration available";
    String ERROR_NO_DATA = "No data to export";
    String DATATYPE_ICON = "Icon";
    String DATATYPE_UNKNOWN = "Unknown";

    String ERROR_FILE_IMPORT = "Unable to import data file";
    String ERROR_FILE_EXPORT = "Unable to export data file";
    String ERROR_FILE_NAME = "Filename is invalid";
    String ERROR_FILE_SAVE = "Unable to save data file";
    String ERROR_FILE_NOT_FOUND = "Data file not found";
    String ERROR_ACCESS_DENIED = "Access denied to data file";
    String ERROR_INVALID_FORMAT = "Invalid DataFormat";
    String ERROR_INVALID_TOKEN = "Invalid Token found in data";
    String ERROR_INVALID_DATA_TYPE = "Invalid Data Type";

    String REGEX_SEPARATOR_TAB = "\\t";

    int INITIAL_CAPACITY = 50;

    // Index into Timestamped data format Vectors
    int MIN_TIMESTAMPED_RAW_DATA_COLUMNS = 3;
    int INDEX_RAWDATA_DATE = 0;
    int INDEX_RAWDATA_TIME = 1;

    int INDEX_TIMESTAMPED_CALENDAR = 0;
    int INDEX_TIMESTAMPED_DATA = 1;

    // Index into Indexed data format Vectors
    int MIN_INDEXED_RAW_DATA_COLUMNS = 2;
    int INDEX_RAWDATA_INDEX = 0;

    int INDEX_INDEXED_X_VALUE = 0;
    int INDEX_INDEXED_DATA = 1;

    // Common to both formats
    int INDEX_DATA_SAMPLE = 1;

    // Index into Metadata
    int METADATA_COLUMNS = 5;
    int INDEX_METADATA_KEY = 0;
    int INDEX_METADATA_VALUE = 1;
    int INDEX_METADATA_DATATYPE = 2;
    int INDEX_METADATA_UNITS = 3;
    int INDEX_METADATA_DESCRIPTION = 4;


    /***********************************************************************************************
     * Initialise the Translator.
     */

    void initialiseTranslator();


    /**********************************************************************************************/
    /* Importers                                                                                  */
    /***********************************************************************************************
     * Import and translate RawData.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    boolean importRawData(String filename,
                          Vector<Vector> eventlog,
                          ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Get the number of items imported.
     *
     * @return long
     */

    long getImportedCount();


    /***********************************************************************************************
     * Set the number of bytes imported by the last RawData import operation.
     *
     * @param count
     */

    void setImportedCount(long count);


    /***********************************************************************************************
     * Import an Image.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    boolean importImage(String filename,
                        Vector<Vector> eventlog,
                        ObservatoryClockInterface clock);


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

    boolean exportXYDataset(DAOWrapperInterface wrapper,
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

    boolean exportRawData(DAOWrapperInterface wrapper,
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
     *
*      @return boolean
     */

    boolean exportMetadata(List<Metadata> metadatametadata, List<Metadata> metadata,
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

    boolean exportLog(List<Metadata> logmetadata,
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

    boolean exportConfiguration(List<Metadata> configmetadata,
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
     * Export the the data from a general ReportTable.
     * The DatasetType is used to refine the Metadata describing the columns.
     * Optional extra metadata may be added to the export.
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

    boolean exportReportTable(List<Metadata> metadatametadata,
                              List<Metadata> metadata,
                              boolean infercolumnmetadata,
                              DatasetType datasettype,
                              ReportTablePlugin report,
                              String filename,
                              boolean timestamp,
                              Vector<Vector> eventlog,
                              ObservatoryClockInterface clock);


    /***********************************************************************************************
     * Get the number of items exported.
     *
     * @return long
     */

    long getExportedCount();


    /***********************************************************************************************
     * Set the number of bytes exported by the last export operation.
     *
     * @param count
     */

    void setExportedCount(long count);


    /**********************************************************************************************/
    /* MetadataMetadata                                                                           */
    /***********************************************************************************************
     * Get the Metadata Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getMetadataMetadata();


    /***********************************************************************************************
     * Add all MetadataMetadata items to the DataTranslator, if possible.
     *
     * @param metadatalist
     */

    void addAllMetadataMetadata(List<Metadata> metadatalist);


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

    Metadata addMetadataMetadata(String key,
                                 String value,
                                 String regex,
                                 DataTypeDictionary datatype,
                                 SchemaUnits.Enum units,
                                 String description);


    /***********************************************************************************************
     * Clear the List of MetadataMetadata.
     */

    void clearMetadataMetadata();


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the List of Metadata for the Framework.
     *
     * @return List<Metadata>
     */

    List<Metadata> getFrameworkMetadata();


    /***********************************************************************************************
     * Get the ObservatoryMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObservatoryMetadata();


    /***********************************************************************************************
     * Get the ObserverMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObserverMetadata();


    /***********************************************************************************************
     * Get the ObservationMetadata.
     *
     * @return List<Metadata>
     */

    List<Metadata> getObservationMetadata();


    /***********************************************************************************************
     * Get the InstrumentMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getInstrumentMetadata();


    /***********************************************************************************************
     * Get the ControllerMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getControllerMetadata();


    /***********************************************************************************************
     * Get the PluginMetadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getPluginMetadata();


    /***********************************************************************************************
     * Get the RawData Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getRawDataMetadata();


    /***********************************************************************************************
     * Get the XYDataset Metadata List.
     *
     * @return List<Metadata>
     */

    List<Metadata> getXYDatasetMetadata();


    /***********************************************************************************************
     * Add all Metadata items in a List to the most appropriate Metadata container
     * in the DataTranslator, if possible.
     *
     * @param metadatalist
     */

    void addOrUpdateAllMetadataToContainers(List<Metadata> metadatalist);


    /***********************************************************************************************
     * Add Metadata to the most appropriate Metadata container in the DataTranslator.
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

    void addMetadataToContainer(String key,
                                String value,
                                String regex,
                                DataTypeDictionary datatype,
                                SchemaUnits.Enum units,
                                String description);


    /***********************************************************************************************
     * Clear all Metadata containers.
     */

    void clearMetadata();


    /**********************************************************************************************/
    /* RawData                                                                                    */
    /***********************************************************************************************
     * Get the RawData translated by this DataTranslator.
     *
     * @return Vector<Object>
     */

    Vector<Object> getRawData();


    /***********************************************************************************************
     * Get the specified translated data sample.
     *
     * @param index
     *
     * @return Vector<Object>
     */

    Vector<Object> getRawDataSample(int index);


    /***********************************************************************************************
     * Add a sample to the translated data.
     *
     * @param sample
     */

    void addRawDataSample(Vector<Object> sample);


    /***********************************************************************************************
     * Clear the RawData.
     */

    void clearRawData();


    /***********************************************************************************************
     * Get the RawDataChannelCount for this DataTranslator.
     *
     * @return int
     */

    int getRawDataChannelCount();


    /***********************************************************************************************
     * Set the ChannelCount for this DataTranslator.
     * The ChannelCount does not include the Calendar in the case of timestamped data.
     *
     * @param count
     */

    void setRawDataChannelCount(int count);


    /***********************************************************************************************
     * Get the TemperatureChannel indicator for this DataTranslator.
     *
     * @return boolean
     */

    boolean hasTemperatureChannel();


    /***********************************************************************************************
     * Set the TemperatureChannel indicator for this DataTranslator.
     *
     * @param flag
     */

    void setTemperatureChannel(boolean flag);


    /***********************************************************************************************
     * Check that the number of data channels has not changed since it was established.
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

    boolean checkRawDataChannelCount(String[] tokens,
                                     int linenumber,
                                     String linecontext,
                                     boolean inferredcount,
                                     boolean timestamped,
                                     Vector<Object> row);


    /**********************************************************************************************/
    /* Images                                                                                     */
    /***********************************************************************************************
     * Get the Image from the Translator.
     *
     * @return Image
     */

    Image getImage();


    /***********************************************************************************************
     * Set the Image.
     *
     * @param img
     */

    void setImage(Image img);


    /***********************************************************************************************
     * Clear the Image.
     */

    void clearImage();


   /**********************************************************************************************/
    /* Messages                                                                                   */
    /***********************************************************************************************
     * Get the messages prepared by the DataTranslator.
     *
     * @return Vector<String>
     */

    List<String> getMessages();


    /***********************************************************************************************
     * Add a message to the list of messages.
     *
     * @param message
     */

    void addMessage(String message);


    /***********************************************************************************************
     * Clear the List of Messages.
     */

    void clearMessages();
    }
