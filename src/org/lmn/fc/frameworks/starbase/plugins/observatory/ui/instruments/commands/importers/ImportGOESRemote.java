// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;

import org.jfree.data.time.TimeSeriesCollection;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datafilters.DataFilterInterface;
import org.lmn.fc.common.datafilters.DataFilterType;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.datatranslators.DataTranslatorHelper;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * ImportGOESRemote.
 */

public final class ImportGOESRemote implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               FrameworkRegex,
                                               ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportGOESRemote().
     * doImportRawDataRemote() is not used directly because we need to modify the filename to use the date.
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportGOESRemote(final ObservatoryInstrumentDAOInterface dao,
                                                              final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportGOESRemote.doImportGOESRemote() ";
        final int PARAMETER_COUNT_MIN = 3;
        final int INDEX_FILENAME = 0;
        final int INDEX_FORMAT = 1;
        final int INDEX_FILTER = 2;
        final CommandType cmdImportRemote;
        final List<ParameterType> listExecutionParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debug(boolDebug, SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdImportRemote = (CommandType)commandmessage.getCommandType().copy();

        // We expect three parameters, the filename to download, format and the filter
        listExecutionParameters = commandmessage.getExecutionParameters();

        // Check the Command parameters before continuing to retrieve the data file
        // Note that a FileChooser is not provided for remote file retrieval!
        if ((listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
            && (listExecutionParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FORMAT).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_FILTER).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strRemoteFilename;
                final String strFormat;
                final String strFilter;
                final DataFormat dataFormat;
                final DataFilterType dataFilterType;

                strRemoteFilename = listExecutionParameters.get(INDEX_FILENAME).getValue();
                strFormat = listExecutionParameters.get(INDEX_FORMAT).getValue();
                strFilter = listExecutionParameters.get(INDEX_FILTER).getValue();

                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)
                dataFormat = DataFormat.getDataFormatForName(strFormat);

                // Map the filter entry to a FilterType
                dataFilterType = DataFilterType.getDataFilterTypeForName(strFilter);

                LOGGER.debug(boolDebug,
                             SOURCE + "[remotefilename=" + strRemoteFilename + "] [format=" + dataFormat.getName() + "]");

                if ((strRemoteFilename != null)
                    && (!EMPTY_STRING.equals(strRemoteFilename))
                    && (dataFilterType != null))
                    {
                    final DataTranslatorInterface translator;
                    final DataFilterInterface filter;

                    // Instantiate up the translator required by the DataFormat
                    translator = DataTranslatorHelper.instantiateTranslator(dataFormat.getTranslatorClassname());

                    // Instantiate the filter required by the DataFilterType
                    filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                    if (filter != null)
                        {
                        filter.initialiseFilter();
                        DataFilterHelper.applyFilterParameters(filter,
                                                               commandmessage.getExecutionParameters(),
                                                               INDEX_FILTER);

                        // All subsequent access to the Filter must be via the DAO
                        dao.setFilter(filter);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Unable to instantiate the DataFilter [name=" + dataFilterType.getName() + "]");
                        dao.setFilter(null);
                        }

                    // The DAO using this command must have set up the RemoteDataConnection in readResources()
                    if ((translator != null)
                        && (dao.getRemoteDataConnection() != null))
                        {
                        final boolean boolInitialised;

                        // Set the translator for this DAO (until changed by another command)
                        dao.setTranslator(translator);
                        dao.getTranslator().initialiseTranslator();

                        // Is the User asking for the **same** file as downloaded incrementally?
                        // If so, start again...
                        if (strRemoteFilename.equals(dao.getRemoteDataConnection().getIncrementalRemoteFilename()))
                            {
                            LOGGER.debug(boolDebug,
                                         SOURCE + "resetting incremental download pointers");
                            dao.getRemoteDataConnection().setIncrementalIndex(0L);
                            dao.getRemoteDataConnection().setIncrementalRemoteFilename(EMPTY_STRING);
                            }

                        // This should always set a default timeout at least
                        boolInitialised = dao.getRemoteDataConnection().initialise(dao.getTimeoutMillis(commandmessage.getModule(), cmdImportRemote), false);

                        if (boolInitialised)
                            {
                            final boolean boolLoggedIn;

                            boolLoggedIn = dao.getRemoteDataConnection().login();

                            if (boolLoggedIn)
                                {
                                final String strConvertedRemoteFilename;
                                final boolean boolReceived;

                                // Check the format of the user-entered filename to see if it is to be used literally,
                                // or if today's date should be prepended
                                strConvertedRemoteFilename = ImportHelper.interpretGOESFilename(strRemoteFilename,
                                                                                                dao.getObservatoryClock());

                                // Transfer (possibly) converted RemoteFilename from RemoteDirectory to LocalDirectory/LocalFilename
                                // Use the same filename for both
                                dao.getRemoteDataConnection().setRemoteFilename(strConvertedRemoteFilename);
                                dao.getRemoteDataConnection().setLocalFilename(strConvertedRemoteFilename);

                                boolReceived = dao.getRemoteDataConnection().receive();

                                // Check to see if we have some valid data to process
                                if (boolReceived)
                                    {
                                    final String strLocalPath;

                                    strLocalPath = dao.getRemoteDataConnection().getLocalDirectory()
                                                        + System.getProperty("file.separator")
                                                        + dao.getRemoteDataConnection().getLocalFilename();

                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.INFO,
                                                                       METADATA_TARGET_RAWDATA
                                                                           + METADATA_ACTION_IMPORT
                                                                           + METADATA_LOCALDIR + strLocalPath + TERMINATOR_SPACE
                                                                           + METADATA_FORMAT + dataFormat.getName() + TERMINATOR,
                                                                       SOURCE,
                                                                       dao.getObservatoryClock());

                                    if (dao.getTranslator().importRawData(strLocalPath,
                                                                          dao.getEventLogFragment(),
                                                                          dao.getObservatoryClock()))
                                        {
                                        final TimeSeriesCollection timeSeriesCollection;

                                        timeSeriesCollection = new TimeSeriesCollection();

                                        // ToDo Supply list of DataTypes
                                        ImportHelper.processImportedData(dao,
                                                                         timeSeriesCollection,
                                                                         filter,
                                                                         null,
                                                                         true,
                                                                         boolDebug);

                                        // Add the most recent sample Values to the ObservationMetadata, for all channels
                                        // What kind of data file is it?
                                        if (DataAnalyser.isCalendarisedRawData(dao.getRawData()))
                                            {
                                            MetadataHelper.addLastTimestampedValuesToAllChannels(dao);
                                            }

                                        // Were we successful in getting some data from the Filter?
                                        if ((timeSeriesCollection != null)
                                            && (timeSeriesCollection.getSeriesCount() > 0))
                                            {
                                            // Tell the DAO about the TimeSeries
                                            dao.setXYDataset(timeSeriesCollection);

                                            strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                            }
                                        else
                                            {
                                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                               EventStatus.WARNING,
                                                                               METADATA_TARGET_RAWDATA
                                                                                   + METADATA_ACTION_IMPORT
                                                                                   + METADATA_RESULT + "DatasetType not recognised" + TERMINATOR,
                                                                               SOURCE,
                                                                               dao.getObservatoryClock());

                                            strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                            }
                                        }
                                    else
                                        {
                                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                           EventStatus.WARNING,
                                                                           METADATA_TARGET_RAWDATA
                                                                               + METADATA_ACTION_IMPORT
                                                                               + METADATA_RESULT + "DataTranslator did not produce any data" + TERMINATOR,
                                                                           SOURCE,
                                                                           dao.getObservatoryClock());

                                        strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                        }
                                    }
                                else
                                    {
                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       EventStatus.WARNING,
                                                                       METADATA_TARGET_RAWDATA
                                                                           + METADATA_ACTION_IMPORT
                                                                           + METADATA_RESULT + "Unable to retrieve the data file" + TERMINATOR,
                                                                       SOURCE,
                                                                       dao.getObservatoryClock());

                                    strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                    }
                                }
                            else
                                {
                                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                   EventStatus.WARNING,
                                                                   METADATA_TARGET_RAWDATA
                                                                       + METADATA_ACTION_IMPORT
                                                                       + METADATA_RESULT + "Unable to log in to the server" + TERMINATOR,
                                                                   SOURCE,
                                                                   dao.getObservatoryClock());

                                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                                }
                            }
                        else
                            {
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_TARGET_RAWDATA
                                                                   + METADATA_ACTION_IMPORT
                                                                   + METADATA_RESULT + "Unable to connect to the server" + TERMINATOR,
                                                               SOURCE,
                                                               dao.getObservatoryClock());

                            strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                            dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           METADATA_TARGET_RAWDATA
                                                               + METADATA_ACTION_IMPORT
                                                               + METADATA_RESULT + "Unable to instantiate the DataTranslator or RemoteDataConnection" + TERMINATOR,
                                                           SOURCE,
                                                           dao.getObservatoryClock());

                        strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                        }
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_RESULT + "Import filename is not valid" + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                    }
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_RESULT + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR_SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_RAWDATA
                                                       + METADATA_ACTION_IMPORT
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());

                strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                }

            finally
                {
                // Make sure the FTP has stopped
                if (dao.getRemoteDataConnection() != null)
                    {
                    dao.getRemoteDataConnection().dispose();
                    }

                // Help the GC?
                if (dao.getFilter() != null)
                    {
                    dao.getFilter().disposeFilter();
                    }

                dao.setFilter(null);
                dao.setTranslator(null);
                }
            }
        else
            {
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_RAWDATA,
                                                                                     METADATA_ACTION_IMPORT));
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
        ObservatoryInstrumentHelper.runGarbageCollector();

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdImportRemote,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
