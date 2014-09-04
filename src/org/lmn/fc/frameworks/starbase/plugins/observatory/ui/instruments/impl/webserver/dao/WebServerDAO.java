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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.webserver.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.webserver.WebServer;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.mortbay.jetty.Server;

import java.util.List;


/***************************************************************************************************
 * WebServerDAO.
 */

public final class WebServerDAO extends AbstractObservatoryInstrumentDAO
                                implements ObservatoryInstrumentDAOInterface
    {
    private String strConfigurationFile;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("getServerConfiguration");
        pool.add("runServer");
        }


    /***********************************************************************************************
     * Construct a WebServerDAO.
     *
     * @param hostinstrument
     */

    public WebServerDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.strConfigurationFile = WebServer.DEFAULT_CONFIGURATION_FILE;

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServerDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServerDAO.disposeDAO()");

        super.disposeDAO();
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return (new WebServerCommandMessage(dao,
                                            instrumentxml,
                                            module,
                                            command,
                                            starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new WebServerResponseMessage(portname,
                                             instrumentxml,
                                             module,
                                             command,
                                             starscript.trim(),
                                             responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * getServerConfiguration().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getServerConfiguration(final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final byte[] bytesXML;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServerDAO.getServerConfiguration()");

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // Do the getServerConfiguration() operation
        // Read the latest from the configuration file
        bytesXML = FileUtilities.readFileAsByteArray(InstallationFolder.getTerminatedUserDir()
                                                        + strConfigurationFile);
        if (bytesXML != null)
            {
            commandType.getResponse().setValue(new String(bytesXML));
            }
        else
            {
            commandType.getResponse().setValue(MSG_UNABLE_TO_READ + SPACE + strConfigurationFile);
            }

        // Create the ResponseMessage
//        setRawDataChannelCount(0);
//        setTemperatureChannel(false);

        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                            commandmessage,
                                                                            commandType);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Run the Web Server.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface runServer(final CommandMessageInterface commandmessage)
        {
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServerDAO.runServer()");

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, a control boolean
        listParameters = commandType.getParameterList();
        responseMessage = null;

        // Control the server
        if ((listParameters != null)
            && (listParameters.size() == 1)
            && (listParameters.get(0) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(0).getInputDataType().getDataTypeName())))
            {
            final boolean boolRun;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRun = Boolean.parseBoolean(listParameters.get(0).getValue());

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "WebServerDAO.runServer() run=" + boolRun);

            try
                {
                if ((getHostInstrument() != null)
                    && (getHostInstrument() instanceof WebServer))
                    {
                    final Server server;

                    server = ((WebServer)getHostInstrument()).getJettyServer();

                    if (server != null)
                        {
                        if ((boolRun)
                            && (server.isStopped()))
                            {
                            server.start();
                            }
                        else
                            {
                            server.stop();

                            // Wait for the server to shutdown?
                            server.join();
                            }
                        }

                    // Something has changed, we may need to refresh a browser etc.
                    InstrumentHelper.notifyInstrumentChanged(getHostInstrument());
                    }
                }

            catch (Exception exception)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "WebServerDAO.runServer() catching a plain Exception!");
                exception.printStackTrace();
                }

            // Create the ResponseMessage
            commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                commandmessage,
                                                                                commandType);
            }

        if (responseMessage == null)
            {
            // Invalid Parameters
            // Create the ResponseMessage
            getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
            responseMessage = ResponseMessageHelper.constructEmptyResponse(this,
                                                                           commandmessage,
                                                                           commandmessage.getInstrument(),
                                                                           commandmessage.getModule(),
                                                                           commandType,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            commandType));
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "WebServerDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        strConfigurationFile = REGISTRY.getStringProperty(getResourceKey() + KEY_CONFIGURATION_FILE);
        }
    }
