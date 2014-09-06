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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.goesxray.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportImage;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.dataconnections.RemoteDataConnectionFTP;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***************************************************************************************************
 * CombinedGoesXrayDAO.
 * Delivers data from a local file or from a file on the NOAA server, retrieved by FTP.
 */

public final class CombinedGoesXrayDAO extends AbstractObservatoryInstrumentDAO
                                       implements ObservatoryInstrumentDAOInterface
    {
    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("importGOESLocal");
        pool.add("importGOESRemote");
        pool.add("importGOESRealtime");

        pool.add("importImageLocal");
        pool.add("importImageRemote");

        pool.add("exportImage");
        }


    /***********************************************************************************************
     * Construct a CombinedGoesXrayDAO.
     *
     * @param hostinstrument
     */

    public CombinedGoesXrayDAO(final ObservatoryInstrumentInterface hostinstrument)
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
        final String SOURCE = "CombinedGoesXrayDAO.initialiseDAO() ";
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
                               "CombinedGoesXrayDAO.disposeDAO()");

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
        return (new GoesXrayCommandMessage(dao,
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
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new GoesXrayResponseMessage(portname,
                                            instrumentxml,
                                            module,
                                            command,
                                            starscript.trim(),
                                            responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * importGOESLocal().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importGOESLocal(final CommandMessageInterface commandmessage)
        {
        return (ImportRawDataLocal.doImportRawDataLocal(this, commandmessage));
        }


    /***********************************************************************************************
     * importGOESRemote().
     * This has to be overridden because we need to modify the filename to use the date.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importGOESRemote(final CommandMessageInterface commandmessage)
        {
        return (ImportGOESRemote.doImportGOESRemote(this, commandmessage));
        }


    /***********************************************************************************************
     * importGOESRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importGOESRealtime(final CommandMessageInterface commandmessage)
        {
        return (ImportGOESRealtime.doImportGOESRealtime(this, commandmessage));
        }


    /***********************************************************************************************
     * importImageLocal().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importImageLocal(final CommandMessageInterface commandmessage)
        {
        return (ImportImageLocal.doImportImageLocal(this, commandmessage));
        }


    /***********************************************************************************************
     * importImageRemote().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importImageRemote(final CommandMessageInterface commandmessage)
        {
        return (ImportImageRemote.doImportImageRemote(this, commandmessage));
        }


    /***********************************************************************************************
     * exportImage().
     * Saves the current image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportImage(final CommandMessageInterface commandmessage)
        {
        return (ExportImage.doExportImage(this, commandmessage));
        }


    /***********************************************************************************************
     * Read all the Resources required by the CombinedGoesXrayDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     */

    public void readResources()
        {
        final String SOURCE = "CombinedGoesXrayDAO.readResources() ";
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