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

package org.lmn.fc.common.datatranslators.tsv;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.util.*;


/***************************************************************************************************
 * TsvTranslatorReaders.
 */

public final class TsvTranslatorReaders implements FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkMetadata,
                                                   FrameworkSingletons,
                                                   FrameworkRegex,
                                                   ResourceKeys
    {
    private static final String METADATA_FORMAT = "[format=tsv]";


    /***********************************************************************************************
     * Parse a Tab-separated file into Timestamped or XY data in RawData.
     * Extract any Metadata header into RawDataMetadata.
     * The DataTranslator is assumed to be initialised.
     * The parsing fails if RawData remains empty.
     *
     * @param translator
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public static boolean parseTabSeparatedToRawData(final DataTranslatorInterface translator,
                                                     final String filename,
                                                     final Vector<Vector> eventlog,
                                                     final ObservatoryClockInterface clock)
        {
        try
            {
            final String SOURCE = "TsvTranslatorReaders.parseTabSeparatedToRawData() ";
            FileReader fileReader;
            LineNumberReader lineNumberReader;
            String strLine;
            boolean boolParseMetadata;
            final TimeZone timeZone;
            final Locale locale;
            boolean boolChannelCountInferred;

            // A bit too verbose?
            //            translator.addMessage(METADATA_TARGET_RAWDATA
//                                   + METADATA_ACTION_IMPORT
//                                   + METADATA_FILENAME + filename + TERMINATOR);

            // Do this only once...
            // ToDo TimeZone, Language, Country should really come from the metadata if possible?
            timeZone = TimeZone.getTimeZone(DataTranslatorInterface.DEFAULT_TIME_ZONE);
            locale = new Locale(REGISTRY.getFramework().getLanguageISOCode(),
                                REGISTRY.getFramework().getCountryISOCode());

            fileReader = new FileReader(filename);
            lineNumberReader = new LineNumberReader(fileReader);
            boolChannelCountInferred = false;

            // Start by parsing the Metadata header
            boolParseMetadata = true;

            do
                {
                // We will need to rollback at the end of the Metadata
                lineNumberReader.mark(1000);

                strLine = lineNumberReader.readLine();

                try
                    {
                    // Only use non-comment lines
                    if ((strLine != null)
                        && (!strLine.trim().startsWith(DataTranslatorInterface.PREFIX_COMMENT)))
                        {
                        // Accumulate the number of characters read from the file
                        translator.setImportedCount(translator.getImportedCount() + strLine.length());

                        // Which parsing mode are we in?

                        if (boolParseMetadata) //---------------- Metadata Mode ----------------
                            {
                            final String[] arrayMetadataTokens;

                            // Fix the problem of the line ending with a null token
                            // This could be a missing Metadata Description, which is allowed
                            if (strLine.endsWith(DataTranslatorInterface.REGEX_SEPARATOR_TAB))
                                {
                                strLine += SPACE;
                                }

                            // First find and parse the Metadata at the head of the file (if any)
                            arrayMetadataTokens = strLine.split(DataTranslatorInterface.REGEX_SEPARATOR_TAB);

                            // Normalise the token array, i.e. trim leading and trailing spaces,
                            // replace null with SPACE.
                            // This is strictly against the spirit of CSV, but in most cases would help the user?
                            DataTranslatorHelper.normaliseTokens(arrayMetadataTokens);
                            //DataTranslatorHelper.debugTokens(arrayMetadataTokens);

                            // Check if the array contains enough tokens,
                            // since we don't know where the Metadata starts
                            if (arrayMetadataTokens.length >= DataTranslatorInterface.MIN_INDEXED_RAW_DATA_COLUMNS)
                                {
                                // If the array contains at least METADATA_COLUMNS,
                                // then we can probably extract Metadata
                                // Key, Value, DataType, Units, Description

                                if (arrayMetadataTokens.length >= DataTranslatorInterface.METADATA_COLUMNS)
                                    {
                                    // It is potential Metadata if the first token is NOT a Date or a Number
                                    if ((!DataTranslatorHelper.isValidDate(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY]))
                                        && (!DataTranslatorHelper.isValidNumber(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY])))
                                        {
                                        final DataTypeDictionary dataType;
                                        final SchemaUnits.Enum unitsEnum;

                                        dataType = DataTypeDictionary.getDataTypeDictionaryEntryForName(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_DATATYPE]);
                                        unitsEnum = SchemaUnits.Enum.forString(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_UNITS]);

                                        // Check that the Metadata key, DataType and Units
                                        // appear in the appropriate Dictionaries
                                        // DataType and Units will have been checked by the above lookups
                                        if ((dataType != null)
                                            && (unitsEnum != null)
                                            && (MetadataDictionary.isValidMetadataDictionaryKey(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY])))
                                            {
                                            final List<String> errors;

                                            errors = new ArrayList<String>(10);

                                            // Validate the Metadata Value using a utility in DataTypeParser
                                            if ((DataTypeHelper.validateDataTypeOfValueField(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_VALUE],
                                                                                             dataType,
                                                                                             errors) == 0)
                                                && (errors.isEmpty()))
                                                {
                                                // Factory the Metadata from the tokens supplied
                                                translator.addMetadataToContainer(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY],
                                                                                  arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_VALUE],
                                                                                  REGEX_NONE,
                                                                                  dataType,
                                                                                  unitsEnum,
                                                                                  arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_DESCRIPTION]);
                                                //System.out.println("Adding Metadata entry [line=" + lineNumberReader.getLineNumber() + "] [" + arrayMetadataTokens[0] + "]");
                                                }
                                            else
                                                {
                                                // ToDo Add parsing errors to the eventlog?
                                                translator.addMessage(METADATA_TARGET_RAWDATA
                                                                       + METADATA_ACTION_IMPORT
                                                                       + METADATA_RESULT + "The Metadata has an invalid Value, so was not used" + TERMINATOR + SPACE
                                                                       + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                                                       + METADATA_CONTEXT + strLine + TERMINATOR);
                                                LOGGER.errors(SOURCE, errors);
                                                }
                                            }
                                        else
                                            {
                                            if (dataType == null)
                                                {
                                                translator.addMessage(METADATA_TARGET_RAWDATA
                                                                      + METADATA_ACTION_IMPORT
                                                                      + METADATA_RESULT + "DataType not in the Dictionary" + TERMINATOR + SPACE
                                                                      + PREFIX + "DataType=" + arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_DATATYPE]
                                                                      + " Key=" + arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY] + TERMINATOR + SPACE
                                                                      + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                                                      + METADATA_CONTEXT + strLine + TERMINATOR);
                                                }

                                            if (unitsEnum == null)
                                                {
                                                translator.addMessage(METADATA_TARGET_RAWDATA
                                                                      + METADATA_ACTION_IMPORT
                                                                      + METADATA_RESULT + "Units not in the Dictionary" + TERMINATOR + SPACE
                                                                      + PREFIX + "Units=" + arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_UNITS]
                                                                      + " Key=" + arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY] + TERMINATOR + SPACE
                                                                      + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                                                      + METADATA_CONTEXT + strLine + TERMINATOR);
                                                }

                                            if (!MetadataDictionary.isValidMetadataDictionaryKey(arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY]))
                                                {
                                                translator.addMessage(METADATA_TARGET_RAWDATA
                                                                      + METADATA_ACTION_IMPORT
                                                                      + METADATA_RESULT + "Metadata Key not in the Dictionary" + TERMINATOR + SPACE
                                                                      + PREFIX + "Key=" + arrayMetadataTokens[DataTranslatorInterface.INDEX_METADATA_KEY] + TERMINATOR + SPACE
                                                                      + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                                                      + METADATA_CONTEXT + strLine + TERMINATOR);
                                                }
                                            }
                                        }
                                    else
                                        {
                                        // We've found the data columns, wider than the Metadata
                                        // so we assume that's the end of the Metadata header
                                        //System.out.println("Leaving Metadata mode (0) [line=" + lineNumberReader.getLineNumber() + "] [" + strLine + "]");

                                        // Permanently leave Metadata mode, and re-read this line
                                        lineNumberReader.reset();
                                        boolParseMetadata = false;
                                        }
                                    }
                                else
                                    {
                                    // We have insufficent tokens for Metadata,
                                    // so we assume that's the end of the Metadata header
                                    //System.out.println("Leaving Metadata mode (1) [line=" + lineNumberReader.getLineNumber() + "] [" + strLine + "]");

                                    // Permanently leave Metadata mode, and re-read this line
                                    lineNumberReader.reset();
                                    boolParseMetadata = false;
                                    }
                                }
                            else
                                {
                                // There's not enough tokens for anything, so just keep going
                                // We will either not find any Metadata, or RawData,
                                // and so never leave Metadata mode before the end of the stream
                                // Ignore all blank lines
                                if ((strLine != null)
                                    && (!EMPTY_STRING.equals(strLine.trim())))
                                    {
                                    translator.addMessage(METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_RESULT + "Skipping invalid Metadata entry" + TERMINATOR + SPACE
                                                           + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                                           + METADATA_CONTEXT + strLine + TERMINATOR);
                                    }
                                }
                            }
                        else // ------------------------- RawData Mode -------------------------
                            {
                            final String[] arrayRawDataTokens;

                            // Fix the problem of the line ending with a null token
                            if (strLine.endsWith(DataTranslatorInterface.REGEX_SEPARATOR_TAB))
                                {
                                strLine += SPACE;
                                }

                            // Now parse the data rows into their channels
                            arrayRawDataTokens = strLine.split(DataTranslatorInterface.REGEX_SEPARATOR_TAB);

                            // Normalise the token array, i.e. trim leading and trailing spaces,
                            // replace null with SPACE.
                            // This is strictly against the spirit of CSV, but in most cases would help the user?
                            DataTranslatorHelper.normaliseTokens(arrayRawDataTokens);
                            //DataTranslatorHelper.debugTokens(arrayRawDataTokens);

                            // It is potential RawData if the first token IS a Date or a Number

                            // Look for a Date column first, assuming Timestamped data with 3 columns
                            if ((DataTranslatorHelper.isValidDate(arrayRawDataTokens[DataTranslatorInterface.INDEX_RAWDATA_DATE]))
                                && (arrayRawDataTokens.length >= DataTranslatorInterface.MIN_TIMESTAMPED_RAW_DATA_COLUMNS))
                                {
                                final Vector<Object> vecRow;
                                final Calendar calendar;

                                vecRow = new Vector<Object>(arrayRawDataTokens.length);

                                // Add the Date and Time
                                calendar = ChronosHelper.parseCalendar(timeZone,
                                                                       locale,
                                                                       DataTranslatorInterface.DATE_PARSE_FORMAT,
                                                                       arrayRawDataTokens[DataTranslatorInterface.INDEX_RAWDATA_DATE]
                                                                           + SPACE
                                                                           + arrayRawDataTokens[DataTranslatorInterface.INDEX_RAWDATA_TIME]);

                                // This sample isn't added if we get a ParseException above
                                vecRow.add(calendar);

                                for (int j = 2;
                                     j < arrayRawDataTokens.length;
                                     j++)
                                    {
                                    vecRow.add(Double.parseDouble(arrayRawDataTokens[j]));
                                    }

                                // Check that the number of data channels has not changed since it was established
                                boolChannelCountInferred = translator.checkRawDataChannelCount(arrayRawDataTokens,
                                                                                               lineNumberReader.getLineNumber(),
                                                                                               strLine,
                                                                                               boolChannelCountInferred,
                                                                                               true,
                                                                                               vecRow);
                                }
                            else if ((DataTranslatorHelper.isValidNumber(arrayRawDataTokens[DataTranslatorInterface.INDEX_RAWDATA_INDEX]))
                                && (arrayRawDataTokens.length >= DataTranslatorInterface.MIN_INDEXED_RAW_DATA_COLUMNS))
                                {
                                final Vector<Object> vecRow;

                                // It should be indexed data
                                vecRow = new Vector<Object>(arrayRawDataTokens.length);

                                // Note that there is ONE MORE column than the ChannelCount
                                for (int j = 0;
                                     j < arrayRawDataTokens.length;
                                     j++)
                                    {
                                    vecRow.add(Double.parseDouble(arrayRawDataTokens[j]));
                                    }

                                // Check that the number of data channels has not changed since it was established
                                boolChannelCountInferred = translator.checkRawDataChannelCount(arrayRawDataTokens,
                                                                                               lineNumberReader.getLineNumber(),
                                                                                               strLine,
                                                                                               boolChannelCountInferred,
                                                                                               false,
                                                                                               vecRow);
                                }
                            else
                                {
                                // Unable to determine the data format for this line
                                // Ignore all blank lines
                                if ((strLine != null)
                                    && (!EMPTY_STRING.equals(strLine.trim())))
                                    {
                                    translator.addMessage(METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_RESULT + "Unable to determine the data format" + TERMINATOR + SPACE
                                                           + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                                           + METADATA_CONTEXT + strLine + TERMINATOR);
                                    }
                                }
                            }
                        }
                    else
                        {
                        // Skip Comments
                        //System.out.println("Comment ignored [line=" + lineNumberReader.getLineNumber() + "] [" + strLine + "]");
                        }
                    }

                catch (FrameworkException exception)
                    {
                    translator.addMessage(METADATA_TARGET_RAWDATA
                                           + METADATA_ACTION_IMPORT
                                           + METADATA_RESULT + DataTranslatorInterface.ERROR_INVALID_DATA_TYPE + TERMINATOR + SPACE
                                           + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                           + METADATA_CONTEXT + strLine + TERMINATOR + SPACE
                                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                    }

                catch (ParseException exception)
                    {
                    translator.addMessage(METADATA_TARGET_RAWDATA
                                           + METADATA_ACTION_IMPORT
                                           + METADATA_RESULT + DataTranslatorInterface.MSG_PARSE_DATE_OR_TIME + TERMINATOR + SPACE
                                           + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                           + METADATA_CONTEXT + strLine + TERMINATOR + SPACE
                                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                    }

                catch (NumberFormatException exception)
                    {
                    translator.addMessage(METADATA_TARGET_RAWDATA
                                           + METADATA_ACTION_IMPORT
                                           + METADATA_RESULT + DataTranslatorInterface.ERROR_INVALID_FORMAT + TERMINATOR + SPACE
                                           + METADATA_LINE + lineNumberReader.getLineNumber() + TERMINATOR + SPACE
                                           + METADATA_CONTEXT + strLine + TERMINATOR + SPACE
                                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
                    }
                }

            // Try again with another line if there was an Exception above
            // null if the end of the stream has been reached
            while (strLine != null);

            // The ChannelCount was set by checkChannelCount()
            // Finally see if the Translator ObservationMetadata contains a Temperature Channel
            translator.setTemperatureChannel(DataAnalyser.hasTemperatureChannel(translator.getObservationMetadata()));

            LOGGER.debugTimedEvent(true,
                                   "TsvTranslatorReaders.parseTabSeparatedToRawData() [channel_count=="
                                    + translator.getRawDataChannelCount()
                                    + "] [temperature_channel=" + translator.hasTemperatureChannel() + "]");

            // Help the GC?
            fileReader = null;
            lineNumberReader = null;

            ObservatoryInstrumentHelper.runGarbageCollector();
            }

        catch (FileNotFoundException exception)
            {
            translator.addMessage(METADATA_TARGET_RAWDATA
                                   + METADATA_ACTION_IMPORT
                                   + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        catch (IOException exception)
            {
            translator.addMessage(METADATA_TARGET_RAWDATA
                                   + METADATA_ACTION_IMPORT
                                   + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_IMPORT + TERMINATOR + SPACE
                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        catch (Exception exception)
            {
            translator.addMessage(METADATA_TARGET_RAWDATA
                                   + METADATA_ACTION_IMPORT
                                   + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_IMPORT + TERMINATOR + SPACE
                                   + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            exception.printStackTrace();
            }

        return (!translator.getRawData().isEmpty());
        }
    }
