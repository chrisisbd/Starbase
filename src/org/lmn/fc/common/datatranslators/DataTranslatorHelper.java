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
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.csv.CsvHelper;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DataTranslatorHelper.
 */

public final class DataTranslatorHelper implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons
    {
    // String Resources
    private static final String TRANSLATOR_NOT_INSTANTIATED = "Unable to instantiate the DataTranslator ";

    // Note similar things exist in CsvHelper
    public static final String CR_LF = "\r\n";
    public static final int TAB = 0x09;
    public static final int COMMA = 0x2C;

    private static final int MASK_FF = 0xff;


    /***********************************************************************************************
     * Instantiate a DataTranslator.
     *
     * @param classname
     *
     * @return DataTranslator
     */

    public static DataTranslatorInterface instantiateTranslator(final String classname)
        {
        DataTranslatorInterface translatorInterface;

        translatorInterface = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            final String strInterface;
            boolean boolLoaded;

            classObject = Class.forName(classname);

            // Does the target implement the DataTranslatorInterface?
            interfaces = classObject.getInterfaces();
            strInterface = DataTranslatorInterface.class.getName();
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
                            // We have found the correct interface
                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "DataTranslator: [" + classname + " implements " + strInterface + "]");

                            // Prove that the real Instrument is a subclass of DataTranslator
                            final Class superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (DataTranslator.class.getName().equals(superClass.getName()))
                                    {
                                    final Constructor constructor;

                                    // Now get hold of the Constructor
                                    constructor = classObject.getDeclaredConstructor();

                                    if (constructor != null)
                                        {
                                        translatorInterface = (DataTranslatorInterface)constructor.newInstance();
                                        boolLoaded = true;
                                        }
                                    else
                                        {
                                        LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "Constructor not found");
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "Class is not a subclass of " + DataTranslator.class.getName());
                                    }
                                }
                            else
                                {
                                LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "Class has no superclass");
                                }
                            }
                        else
                            {
                            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "No interfaces found");
                }
            }

        catch(NoSuchMethodException exception)
            {
            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "NoSuchMethodException [classname=" + classname + "]");
            }

        catch(SecurityException exception)
            {
            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "SecurityException [classname=" + classname + "]");
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "InstantiationException [classname=" + classname + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "IllegalAccessException [classname=" + classname + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "IllegalArgumentException [classname=" + classname + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "InvocationTargetException [classname=" + classname + "]");
            }

        catch (ClassNotFoundException exception)
            {
            // Suppress empty classnames, because these are probably intentional
            if ((classname != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(classname.trim())))
                {
                LOGGER.error(TRANSLATOR_NOT_INSTANTIATED + "ClassNotFoundException [classname=" + classname + "]");
                }
            }

        return (translatorInterface);
        }


    /***********************************************************************************************
     * Copy all Metadata produced by the DataTranslator into the appropriate containers in the
     * Framework and DAO.
     *
     * @param translator
     * @param framework
     * @param dao
     * @param traceon
     */

    public static void copyMetadataFromTranslator(final DataTranslatorInterface translator,
                                                  final FrameworkPlugin framework,
                                                  final ObservatoryInstrumentDAOInterface dao,
                                                  final boolean traceon)
        {
        final String SOURCE = "DataTranslatorHelper.copyMetadataFromTranslator() ";

        if (translator != null)
            {
            // Framework Metadata
            if (framework != null)
                {
                MetadataHelper.addOrUpdateMetadataListTraced(framework.getFrameworkMetadata(),
                                                             translator.getFrameworkMetadata(),
                                                             SOURCE + "Framework Metadata",
                                                             traceon);
                }

            if (dao != null)
                {
                // Metadata Metadata
                dao.addAllMetadataToContainersTraced(translator.getMetadataMetadata(),
                                                     SOURCE + "Metadata Metadata",
                                                     traceon);
                // Observatory Metadata
                dao.addAllMetadataToContainersTraced(translator.getObservatoryMetadata(),
                                                     SOURCE + "Observatory Metadata",
                                                     traceon);
                // Observer Metadata
                dao.addAllMetadataToContainersTraced(translator.getObserverMetadata(),
                                                     SOURCE + "Observer Metadata",
                                                     traceon);
                // Observation Metadata
                dao.addAllMetadataToContainersTraced(translator.getObservationMetadata(),
                                                     SOURCE + "Observation Metadata",
                                                     traceon);
                // Instrument Metadata
                dao.addAllMetadataToContainersTraced(translator.getInstrumentMetadata(),
                                                     SOURCE + "Instrument Metadata",
                                                     traceon);
                // Controller Metadata
                dao.addAllMetadataToContainersTraced(translator.getControllerMetadata(),
                                                     SOURCE + "Controller Metadata",
                                                     traceon);
                // Plugin Metadata
                dao.addAllMetadataToContainersTraced(translator.getPluginMetadata(),
                                                     SOURCE + "Plugin Metadata",
                                                     traceon);
                // RawData Metadata
                dao.addAllMetadataToContainersTraced(translator.getRawDataMetadata(),
                                                     SOURCE + "RawData Metadata",
                                                     traceon);
                // XYDataset Metadata
                dao.addAllMetadataToContainersTraced(translator.getXYDatasetMetadata(),
                                                     SOURCE + "XYDataset Metadata",
                                                     traceon);
                }
            }
        }


    /***********************************************************************************************
     * Find the most appropriate DataTranslator container for Metadata with the specified Key.
     * Return NULL if no suitable container is found.
     *
     * @param translator
     * @param metadatakey
     *
     * @return List<Metadata>
     */

    public static List<Metadata> findMetadataContainerByKey(final DataTranslator translator,
                                                            final String metadatakey)
        {
        final String SOURCE = "DataTranslatorHelper.findMetadataContainerByKey() ";
        final List<Metadata> listMetadata;

        if (translator != null)
            {
            if ((metadatakey != null)
                && (!EMPTY_STRING.equals(metadatakey))
                && (MetadataDictionary.isValidMetadataDictionaryKey(metadatakey)))
                {
                // Work down the Metadata object model
                if (metadatakey.startsWith(MetadataDictionary.KEY_FRAMEWORK_ROOT.getKey()))
                    {
                    listMetadata = translator.getFrameworkMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVATORY_ROOT.getKey()))
                    {
                    listMetadata = translator.getObservatoryMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVER_ROOT.getKey()))
                    {
                    listMetadata = translator.getObserverMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVATION_CHANNEL_ROOT.getKey()))
                    {
                    // ChannelID syntax was checked by the MetadataDictionary lookup
                    listMetadata = translator.getObservationMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_OBSERVATION_ROOT.getKey()))
                    {
                    listMetadata = translator.getObservationMetadata();
                    }
                else if (metadatakey.contains(MetadataDictionary.KEY_CONTROLLER_ROOT.getKey()))
                    {
                    listMetadata = translator.getControllerMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey()))
                    {
                    listMetadata = translator.getInstrumentMetadata();
                    }
                else if (metadatakey.startsWith(MetadataDictionary.KEY_RSP_ROOT.getKey()))
                    {
                    // All RSP data are Observation attributes
                    listMetadata = translator.getObservationMetadata();
                    }
                else
                    {
                    // Everything else ends up in Observation Metadata
                    // TODO Review Plugin Metadata and keys starting 'Metadata.'
                    listMetadata = translator.getObservationMetadata();
                    }
                }
            else
                {
                listMetadata = null;
                LOGGER.error(SOURCE +  "Metadata Key is NULL or is not known to the MetadataDictionary [key=" + metadatakey + "]");
                }
            }
        else
            {
            listMetadata = null;
            LOGGER.error(SOURCE +  "DataTranslator is NULL");
            }

        return (listMetadata);
        }


    /***********************************************************************************************
     * Add any INFO messages from the Translator to the EventLog.
     *
     * @param translator
     * @param eventlog
     * @param clock
     * @param source
     */

    public static void addTranslatorMessages(final DataTranslatorInterface translator,
                                             final Vector<Vector> eventlog,
                                             final ObservatoryClockInterface clock,
                                             final String source)
        {
        if ((translator != null)
            && (translator.getMessages() != null)
            && (!translator.getMessages().isEmpty())
            && (eventlog != null))
            {
            final Iterator<String> iterMessages;

            iterMessages = translator.getMessages().iterator();

            // Copy all Translator messages to the EventLog fragment
            while (iterMessages.hasNext())
                {
                SimpleEventLogUIComponent.logEvent(eventlog,
                                                   EventStatus.INFO,
                                                   iterMessages.next(),
                                                   source,
                                                   clock);
                }
            }
        }


    /***********************************************************************************************
     * Add a message to the list of messages.
     *
     * @param messages
     * @param message
     */

    public static void addMessage(final List<String> messages,
                                  final String message)
        {
        if ((messages != null)
            && (message != null))
            {
            messages.add(message);
            }
        }


    /***********************************************************************************************
     * Show the specified number of samples.
     * If maxcount <= 0, show all samples.
     *
     * @param translator
     * @param maxcount
     *
     * @return StringBuffer
     */

    public static StringBuffer showTranslatedData(final DataTranslatorInterface translator,
                                                  final long maxcount)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((translator != null)
            && (translator.getRawData() != null))
            {
            final Iterator iterSamples;
            final long longLimit;
            long longCount;

            buffer.append("Translated Data for " ) ;
            buffer.append(translator.getRawData().size()) ;
            buffer.append(" points");

            iterSamples = translator.getRawData().iterator();
            longCount = 0;

            if (maxcount <= 0)
                {
                longLimit = Long.MAX_VALUE;
                }
            else
                {
                longLimit = maxcount;
                }

            while ((iterSamples.hasNext())
                && (longCount < longLimit))
                {
                final Vector vecSample = (Vector) iterSamples.next();

                // There must be at least one Calendar and one data sample
                if ((vecSample != null)
                    && (vecSample.size() > 1))
                    {
                    Object objItem;

                    objItem = vecSample.get(DataTranslatorInterface.INDEX_TIMESTAMPED_CALENDAR);

                    if ((objItem != null)
                        && (objItem instanceof Calendar))
                        {
                        buffer.append("\n");
                        buffer.append(INDENT);
                        buffer.append(longCount);
                        buffer.append(SPACE);
                        buffer.append(ChronosHelper.toCalendarString((Calendar)objItem));
                        buffer.append(SPACE);

                        for (int i = 1; i < vecSample.size(); i++)
                            {
                            objItem = vecSample.get(i);

                            if (objItem != null)
                                {
                                buffer.append(objItem);
                                }

                            buffer.append(SPACE);
                            }
                        }
                    }

                longCount++;
                }
            }

        return (buffer);
        }


    /***********************************************************************************************
     * Show the specified number of samples from a TimeSeriesCollection.
     * If maxcount <= 0, show all samples.
     *
     * @param dataset
     * @param maxcount
     *
     * @return StringBuffer
     */

    public static StringBuffer showTimeSeriesCollection(final XYDataset dataset,
                                                        final long maxcount)
        {
        final StringBuffer buffer;

        buffer = new StringBuffer();

        if ((dataset != null)
            && (dataset.getSeriesCount() > 0)
            && (dataset instanceof TimeSeriesCollection))
            {
            final TimeSeriesCollection collection;
            final long longLimit;
            long longCount;

            collection = (TimeSeriesCollection)dataset;

            buffer.append("TimeSeriesCollection for " ) ;
            buffer.append(collection.getSeries(0).getItemCount()) ;
            buffer.append(" points");

            longCount = 0;

            if (maxcount <= 0)
                {
                longLimit = Long.MAX_VALUE;
                }
            else
                {
                longLimit = maxcount;
                }

            // Scan each row of data
            for (int i = 0;
                 ((i < collection.getSeries(0).getItemCount()) && (longCount < longLimit));
                 i++)
                {
                buffer.append("\n");
                buffer.append(INDENT);
                buffer.append(longCount);
                buffer.append(SPACE);

                // Get the RegularTimePeriod from the first Series only
                buffer.append(collection.getSeries(0).getTimePeriod(i));
                buffer.append(SPACE);

                // Step over all TimeSeries in the Collection to get the data values
                for (int j = 0;
                    j < collection.getSeriesCount();
                    j++)
                    {
                    buffer.append(collection.getSeries(j).getValue(i));
                    buffer.append(SPACE);
                    }

                longCount++;
                }
            }

        return (buffer);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Convert the contents of the specified array to a <code><b>String</b></code>,
     * starting at an offset of <code>start</code>, for <code>length</code>.
     *
     * @param array
     * @param start
     * @param length
     *
     * @return String
     */

    public static String arrayToString(final byte[] array,
                                       final int start,
                                       final int length)
        {
        int i;
        int count;
        final byte[] temp;

        count = 0;
        temp = new byte[length];

        for (i = start; i < (start + length); i++)
            {
            if ((array[i] >= 0)
                && (array[i] < 32))
                {
                // Control characters are replaced by spaces
                temp[count] = 32;
                }
            else
                {
                temp[count] = array[i];
                }

            count++;
            }

        return (new String(temp));
        }


    /***********************************************************************************************
     * Convert the contents of the specified array to a <code><b>double</b></code>,
     * starting at an offset of <code>start</code>.
     *
     * @param array
     * @param start
     *
     * @return double
     */

    public static double arrayToDouble(final byte[] array,
                                       final int start)
        {
        int i;
        int count;
        long accum;
        final int length;
        final byte[] temp;

        count = 0;
        length = 8;
        temp = new byte[length];

        for (i = start; i < (start + length); i++)
            {
            temp[count] = array[i];
            count++;
            }

        accum = 0;
        i = 0;

        for (int shiftBy = 0;
             shiftBy < Double.SIZE;
             shiftBy += 8)
            {
            accum |= ((long) (temp[i] & MASK_FF)) << shiftBy;
            i++;
            }

        return (Double.longBitsToDouble(accum));
        }


    /***********************************************************************************************
     * Convert the contents of the specified array to a <code><b>long</b></code>,
     * starting at an offset of <code>start</code>.
     *
     * @param array
     * @param start
     *
     * @return long
     */

    public static long arrayToLong(final byte[] array,
                                   final int start)
        {
        int i;
        int count;
        long accum;
        final int length;
        final byte[] temp;

        count = 0;
        length = 4;
        temp = new byte[length];

        for (i = start; i < (start + length); i++)
            {
            temp[count] = array[i];
            count++;
            }

        accum = 0;
        i = 0;

        for (int shiftBy = 0;
             shiftBy < 32;
             shiftBy += 8)
            {
            accum |= ((long) (temp[i] & MASK_FF)) << shiftBy;
            i++;
            }

        return (accum);
        }


    /***********************************************************************************************
     * Convert the contents of the specified array to an <code><b>int</b></code>,
     * starting at an offset of <code>start</code>.
     *
     * @param array
     * @param start
     *
     * @return int
     */

    public static int arrayToInt(final byte[] array,
                                 final int start)
        {
        final int lowbits;
        final int highbits;

        lowbits = array[start] & MASK_FF;
        highbits = array[start + 1] & MASK_FF;

        return ((highbits << 8 | lowbits));
        }


    /***********************************************************************************************
     * Indicate if the supplied String represents a Date.
     *
     * @param token
     *
     * @return boolean
     */

    public static boolean isValidDate(final String token)
        {
        boolean boolIsValid;

        boolIsValid = true;

        try
            {
            java.sql.Date.valueOf(token);
            }

        catch (IllegalArgumentException exception)
            {
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * Indicate if the supplied String represents a Number.
     *
     * @param token
     *
     * @return boolean
     */

    public static boolean isValidNumber(final String token)
        {
        boolean boolIsValid;

        boolIsValid = true;

        try
            {
            Double.parseDouble(token);
            }

        catch (NumberFormatException exception)
            {
            boolIsValid = false;
            }

        return (boolIsValid);
        }


    /***********************************************************************************************
     * Normalise the token array, i.e. trim leading and trailing spaces, replace null with SPACE.
     *
     * @param array
     */

    public static void normaliseTokens(final String[] array)
        {
        for (int i = 0;
             i < array.length;
             i++)
            {
            if (array[i] == null)
                {
                array[i] = SPACE;
                }
            else
                {
                array[i] = array[i].trim();
                }
            }
        }


    /***********************************************************************************************
     * Write Metadata to the Stream.
     *
     * @param stream
     * @param metadata
     * @param keys
     * @param separator
     *
     * @throws IOException
     */

    public static void writeMetadata(final OutputStream stream,
                                     final List<Metadata> metadata,
                                     final List<String> keys,
                                     final int separator) throws IOException
        {
        final String SOURCE = "DataTranslatorHelper.writeMetadata() ";

        // Process the List of Keys in alphabetical order
        for (int i = 0;
             i < keys.size();
             i++)
            {
            final String strKey;

            strKey = keys.get(i);

            processOneKey(stream, metadata, strKey, separator);
            }
        }


    /***********************************************************************************************
     * Traverse the structure implied by one Key and write the Metadata information to the stream.
     *
     * @param stream
     * @param metadata
     * @param key
     * @param separator
     *
     * @throws IOException
     */

    private static void processOneKey(final OutputStream stream,
                                      final List<Metadata> metadata,
                                      final String key,
                                      final int separator) throws IOException
        {
        final String SOURCE = "DataTranslatorHelper.processOneKey() ";
        final Metadata metadataItem;

        metadataItem = MetadataHelper.getMetadataByKey(metadata, key);

        if (metadataItem != null)
            {
            if (metadataItem.getKey() != null)
                {
                stream.write(metadataItem.getKey().getBytes());
                }
            else
                {
                stream.write("InvalidKey".getBytes());
                }
            stream.write(separator);

            if (metadataItem.getValue() != null)
                {
                if (separator == CsvHelper.CSV_SEPARATOR)
                    {
                    stream.write(CsvHelper.buildCsvToken(metadataItem.getValue()).getBytes());
                    }
                else
                    {
                    stream.write(metadataItem.getValue().getBytes());
                    }
                }
            else
                {
                stream.write("InvalidValue".getBytes());
                }
            stream.write(separator);

            if (metadataItem.getDataTypeName() != null)
                {
                stream.write(metadataItem.getDataTypeName().toString().getBytes());
                }
            else
                {
                stream.write("InvalidDataType".getBytes());
                }
            stream.write(separator);

            if (metadataItem.getUnits() != null)
                {
                stream.write(metadataItem.getUnits().toString().getBytes());
                }
            else
                {
                stream.write("InvalidUnits".getBytes());
                }
            stream.write(separator);

            if (metadataItem.getDescription() != null)
                {
                if (separator == CsvHelper.CSV_SEPARATOR)
                    {
                    stream.write(CsvHelper.buildCsvToken(metadataItem.getDescription()).getBytes());
                    }
                else
                    {
                    stream.write(metadataItem.getDescription().getBytes());
                    }
                }
            else
                {
                // An empty Description is acceptable
                stream.write(SPACE.getBytes());
                }

            stream.write(CR_LF.getBytes());
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Metadata Key [key=" + key + "]");
            }
        }


    /***********************************************************************************************
     * Write all supported types of data to the specified Stream.
     *
     * @param stream
     * @param dataitem
     *
     * @throws IOException
     */

    public static void writeAnyDataItem(final OutputStream stream,
                                        final Object dataitem) throws IOException
        {
        if ((dataitem == null)
            || (EMPTY_STRING.equals(dataitem)))
            {
            stream.write(SPACE.getBytes());
            }
        else if (dataitem instanceof String)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Integer)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Float)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Double)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof BigDecimal)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Icon)
            {
            stream.write(DataTranslatorInterface.DATATYPE_ICON.getBytes());
            }
        else
            {
            // The data format is not recognised
            stream.write(DataTranslatorInterface.DATATYPE_UNKNOWN.getBytes());
            }
        }


    /***********************************************************************************************
     * Write all supported types of CSV data to the specified Stream.
     *
     * @param stream
     * @param dataitem
     *
     * @throws IOException
     */

    public static void writeCsvDataItem(final OutputStream stream,
                                        final Object dataitem) throws IOException
        {
        if ((dataitem == null)
            || (EMPTY_STRING.equals(dataitem)))
            {
            stream.write(SPACE.getBytes());
            }
        else if (dataitem instanceof String)
            {
            stream.write(CsvHelper.buildCsvToken(dataitem.toString()).getBytes());
            }
        else if (dataitem instanceof Integer)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Float)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Double)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof BigDecimal)
            {
            stream.write(dataitem.toString().getBytes());
            }
        else if (dataitem instanceof Icon)
            {
            stream.write(DataTranslatorInterface.DATATYPE_ICON.getBytes());
            }
        else
            {
            // The data format is not recognised
            stream.write(DataTranslatorInterface.DATATYPE_UNKNOWN.getBytes());
            }
        }


    /***********************************************************************************************
     * Debug one line of the token String[].
     *
     * @param tokens
     * @param intLineNumber
     * @param debug
     */

    public static void debugTokens(final String[] tokens,
                                   final int intLineNumber,
                                   final boolean debug)
        {
        if (debug)
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();
            buffer.append("LINE ");
            buffer.append(intLineNumber);
            buffer.append(" tokens: ");

            for (int i = 0;
                 i < tokens.length;
                 i++)
                {
                final String token;

                token = tokens[i];

                buffer.append(token);
                buffer.append(" !! ");
                }

            System.out.println(buffer);
            }
        }


    /***********************************************************************************************
     * Debug the token array.
     *
     * @param array
     */

    public static void debugTokens(final String[] array)
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "");

        for (int i = 0;
             i < array.length;
             i++)
            {
            final String token = array[i];

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   i + " [token=" + token + "]");
            }
        }
    }
