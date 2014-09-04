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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.generic.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.dataconnections.RemoteDataConnectionFTP;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***************************************************************************************************
 * GenericInstrumentDAO.
 * Delivers data from a local file or from a file on a remote server, retrieved by FTP.
 */

public final class GenericInstrumentDAO extends AbstractObservatoryInstrumentDAO
                                        implements ObservatoryInstrumentDAOInterface
    {
    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        }


    /***********************************************************************************************
     * Construct a GenericInstrumentDAO.
     *
     * @param hostinstrument
     */

    public GenericInstrumentDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * KEY_DAO_CONNECTION_HOSTNAME
     * KEY_DAO_CONNECTION_USERNAME
     * KEY_DAO_CONNECTION_PASSWORD
     * KEY_DAO_CONNECTION_TRANSFER_MODE
     * KEY_DAO_CONNECTION_CONNECTION_MODE
     * KEY_DAO_TRANSFER_LOCAL_DIRECTORY
     * KEY_DAO_TRANSFER_REMOTE_DIRECTORY
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "GenericInstrumentDAO.initialiseDAO() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // A RemoteDataConnection is required by importRawDataRemote() and importRawDataRemoteIncrement()
        // This *must* occur *before* initialiseDAO() because the connection is initialised in that call
        setRemoteDataConnection(new RemoteDataConnectionFTP(REGISTRY.getStringProperty(resourcekey + KEY_DAO_CONNECTION_HOSTNAME),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_CONNECTION_USERNAME),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_CONNECTION_PASSWORD),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_CONNECTION_TRANSFER_MODE),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_CONNECTION_CONNECTION_MODE),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_TRANSFER_LOCAL_DIRECTORY),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_TRANSFER_LOCAL_FILENAME),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_TRANSFER_REMOTE_DIRECTORY),
                                                            REGISTRY.getStringProperty(resourcekey + KEY_DAO_TRANSFER_REMOTE_FILENAME),
                                                            getEventLogFragment(),
                                                            getObservatoryClock(),
                                                            boolDebug));
        super.initialiseDAO(resourcekey);

        LOGGER.debug(boolDebug,
                     SOURCE + "[ResourceKey=" + getResourceKey() + "]");

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "GenericInstrumentDAO.disposeDAO() ");

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
        return (new GenericInstrumentCommandMessage(dao,
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
        return (new GenericInstrumentResponseMessage(portname,
                                             instrumentxml,
                                             module,
                                             command,
                                             starscript.trim(),
                                             responsestatusbits));
        }


    /***********************************************************************************************
     * Read all the Resources required by the GenericInstrumentDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     */

    public void readResources()
        {
        final String SOURCE = "GenericInstrumentDAO.readResources() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        LOGGER.debug(boolDebug,
                     SOURCE + "[ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
