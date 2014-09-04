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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.RemoteDataConnectionInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.dataconnections.RemoteDataConnectionFTP;
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
 * ImportRawDataRemoteServer.
 */

public final class ImportRawDataRemoteServer implements FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata,
                                                        FrameworkSingletons,
                                                        FrameworkRegex,
                                                        ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportRawDataRemoteServer().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportRawDataRemoteServer(final ObservatoryInstrumentDAOInterface dao,
                                                                       final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportRawDataRemoteServer.doImportRawDataRemoteServer()";
        final int PARAMETER_COUNT_MIN = 8;
        final int INDEX_SERVER_HOSTNAME = 0;
        final int INDEX_SERVER_USERNAME = 1;
        final int INDEX_SERVER_PASSWORD = 2;
        final int INDEX_SERVER_REMOTEDIRECTORY = 3;
        final int INDEX_SERVER_REMOTEFILENAME = 4;
        final int INDEX_IMPORT_LOCALDIRECTORY = 5;
        final int INDEX_IMPORT_FORMAT = 6;
        final int INDEX_IMPORT_FILTER = 7;
        final CommandType cmdImportRemoteData;
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
        cmdImportRemoteData = (CommandType)commandmessage.getCommandType().copy();

        listExecutionParameters = commandmessage.getExecutionParameters();

        // Check the Command parameters before continuing to retrieve the data file
        // Note that a FileChooser is not provided for remote file retrieval!
        if ((listExecutionParameters != null)
            && (listExecutionParameters.size() >= PARAMETER_COUNT_MIN)
            && (listExecutionParameters.get(INDEX_SERVER_HOSTNAME) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_SERVER_HOSTNAME).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_SERVER_USERNAME) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_SERVER_USERNAME).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_SERVER_PASSWORD) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_SERVER_PASSWORD).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_SERVER_REMOTEDIRECTORY) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_SERVER_REMOTEDIRECTORY).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_SERVER_REMOTEFILENAME) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_SERVER_REMOTEFILENAME).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_IMPORT_LOCALDIRECTORY) != null)
            && (SchemaDataType.PATH_NAME.equals(listExecutionParameters.get(INDEX_IMPORT_LOCALDIRECTORY).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_IMPORT_FORMAT) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_IMPORT_FORMAT).getInputDataType().getDataTypeName()))
            && (listExecutionParameters.get(INDEX_IMPORT_FILTER) != null)
            && (SchemaDataType.STRING.equals(listExecutionParameters.get(INDEX_IMPORT_FILTER).getInputDataType().getDataTypeName())))
            {
            RemoteDataConnectionInterface dataConnectionSaved;

            dataConnectionSaved = null;

            try
                {
                final String strServerHostname;
                final String strServerUsername;
                final String strServerPassword;
                final String strServerRemoteDirectory;
                final String strServerRemoteFilename;
                final RemoteDataConnectionInterface dataConnection;
                final String strImportLocalDirectory;
                final String strImportFormat;
                final DataFormat dataFormat;
                final DataTranslatorInterface translator;
                final String strImportFilter;
                final DataFilterType dataFilterType;
                final boolean boolInitialised;

                //---------------------------------------------------------------------------------
                // Obtain all Parameters

                // Server
                strServerHostname = listExecutionParameters.get(INDEX_SERVER_HOSTNAME).getValue();
                strServerUsername = listExecutionParameters.get(INDEX_SERVER_USERNAME).getValue();
                strServerPassword = listExecutionParameters.get(INDEX_SERVER_PASSWORD).getValue();
                strServerRemoteDirectory = listExecutionParameters.get(INDEX_SERVER_REMOTEDIRECTORY).getValue();
                strServerRemoteFilename = listExecutionParameters.get(INDEX_SERVER_REMOTEFILENAME).getValue();

                // Import
                strImportLocalDirectory = listExecutionParameters.get(INDEX_IMPORT_LOCALDIRECTORY).getValue();
                strImportFormat = listExecutionParameters.get(INDEX_IMPORT_FORMAT).getValue();
                strImportFilter = listExecutionParameters.get(INDEX_IMPORT_FILTER).getValue();

                //---------------------------------------------------------------------------------
                // Map the format entry to a DataFormat
                // (may throw IllegalArgumentException if XML is incorrectly configured)

                dataFormat = DataFormat.getDataFormatForName(strImportFormat);

                // Instantiate the translator required by the DataFormat
                if (dataFormat != null)
                    {
                    translator = DataTranslatorHelper.instantiateTranslator(dataFormat.getTranslatorClassname());

                    if (translator != null)
                        {
                        // Set the translator for this DAO (until changed by another command)
                        dao.setTranslator(translator);
                        dao.getTranslator().initialiseTranslator();
                        }
                    else
                        {
                        dao.setTranslator(null);
                        throw new  IllegalArgumentException("Data Translator not found");
                        }
                    }
                else
                    {
                    dao.setTranslator(null);
                    throw new  IllegalArgumentException("Unable to instantiate the Data Translator");
                    }

                //---------------------------------------------------------------------------------
                // Map the filter entry to a FilterType

                dataFilterType = DataFilterType.getDataFilterTypeForName(strImportFilter);

                if (dataFilterType != null)
                    {
                    final DataFilterInterface filter;

                    // Instantiate the filter required by the DataFilterType
                    filter = DataFilterHelper.instantiateFilter(dataFilterType.getFilterClassname());

                    if (filter != null)
                        {
                        // All subsequent access to the Filter must be via the DAO
                        dao.setFilter(filter);
                        dao.getFilter().initialiseFilter();
                        DataFilterHelper.applyFilterParameters(filter,
                                                               listExecutionParameters,
                                                               INDEX_IMPORT_FILTER);

                        }
                    else
                        {
                        dao.setFilter(null);
                        throw new  IllegalArgumentException("Data Filter not found");
                        }
                    }
                else
                    {
                    dao.setFilter(null);
                    throw new  IllegalArgumentException("Invalid Data Filter");
                    }

                // Save the original DataConnection, as used by the Importer etc.
                dataConnectionSaved = dao.getRemoteDataConnection();

                dataConnection = new RemoteDataConnectionFTP(strServerHostname,
                                                             strServerUsername,
                                                             strServerPassword,
                                                             RemoteDataConnectionInterface.TRANSFER_MODE_BINARY,
                                                             RemoteDataConnectionInterface.CONNECTION_MODE_PASSIVE,
                                                             strImportLocalDirectory,
                                                             strServerRemoteFilename,
                                                             strServerRemoteDirectory,
                                                             strServerRemoteFilename,
                                                             dao.getEventLogFragment(),
                                                             dao.getObservatoryClock(),
                                                             boolDebug);
                dao.setRemoteDataConnection(dataConnection);

                // We should now have all validated Parameters
                if (boolDebug)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_IMPORT

                                                           + METADATA_HOSTNAME + dataConnection.getHostname() + TERMINATOR_SPACE
                                                           + METADATA_USERNAME + dataConnection.getUsername() + TERMINATOR_SPACE
                                                           + METADATA_PASSWORD + dataConnection.getPassword() + TERMINATOR_SPACE
                                                           + METADATA_REMOTEDIR + dataConnection.getRemoteDirectory() + TERMINATOR_SPACE
                                                           + METADATA_REMOTEFILE + dataConnection.getRemoteFilename() + TERMINATOR_SPACE

                                                           + METADATA_LOCALDIR + dataConnection.getLocalDirectory() + TERMINATOR_SPACE
                                                           + METADATA_LOCALFILE + dataConnection.getLocalFilename() + TERMINATOR_SPACE

                                                           + METADATA_FORMAT + dataFormat.getName() + TERMINATOR_SPACE
                                                           + METADATA_FILTERNAME + dao.getFilter().getFilterType().getName() + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());
                    }

                // This should always set a default timeout at least
                boolInitialised = dao.getRemoteDataConnection().initialise(dao.getTimeoutMillis(commandmessage.getModule(), cmdImportRemoteData), false);

                if (boolInitialised)
                    {
                    final boolean boolLoggedIn;

                    boolLoggedIn = dao.getRemoteDataConnection().login();

                    if (boolLoggedIn)
                        {
                        final boolean boolReceived;

                        // Transfer user-input RemoteFilename from RemoteDirectory to LocalDirectory/LocalFilename
                        // Use the same filename for both
                        dao.getRemoteDataConnection().setRemoteFilename(strServerRemoteFilename);
                        dao.getRemoteDataConnection().setLocalFilename(strServerRemoteFilename);

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
                                                                   + METADATA_LOCALPATH + strLocalPath + TERMINATOR_SPACE
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
                                                                 dao.getFilter(),
                                                                 null,
                                                                 true,
                                                                 boolDebug);

                                // Add the most recent sample Values to the ObservationMetadata, for all channels
                                // What kind of data file is it?
                                if (DataAnalyser.isCalendarisedRawData(dao.getRawData()))
                                    {
                                    MetadataHelper.addLastTimestampedValuesToAllChannels(dao);
                                    }
                                else if (DataAnalyser.isColumnarRawData(dao.getRawData()))
                                    {
                                    MetadataHelper.addLastColumnarValuesToAllChannels(dao);
                                    }

                                // Were we successful in getting some data?
                                if ((timeSeriesCollection != null)
                                    && (timeSeriesCollection.getSeriesCount() > 0))
                                    {
                                    // Tell the DAO about the TimeSeries
                                    dao.setXYDataset(timeSeriesCollection);

                                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                    }
                                else if ((dao.getXYDataset() != null)
                                         && (dao.getXYDataset().getSeriesCount() > 0))
                                    {
                                    // The XYDataset is already there, provided by the columnar data Filter

                                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                                    }
                                else
                                    {
                                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                                       ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
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
                                                                   ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
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
                                                               ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
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
                                                           ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
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
                                                       ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
                                                       METADATA_TARGET_RAWDATA
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_RESULT + "Unable to connect to the server" + TERMINATOR,
                                                       SOURCE,
                                                       dao.getObservatoryClock());

                    strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   ResponseMessageStatus.INVALID_PARAMETER.getEventStatus(),
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
                                                   ResponseMessageStatus.PREMATURE_TERMINATION.getEventStatus(),
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

                // Restore the original connection, as used by the Importer etc.
                dao.setRemoteDataConnection(dataConnectionSaved);
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
                                                                      cmdImportRemoteData,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
