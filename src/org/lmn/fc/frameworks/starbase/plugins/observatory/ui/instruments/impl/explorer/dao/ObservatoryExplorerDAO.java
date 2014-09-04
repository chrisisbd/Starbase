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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.explorer.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.GetNetworkInterfaces;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.UploadControllerOS;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***************************************************************************************************
 * ObservatoryExplorerDAO.
 */

public final class ObservatoryExplorerDAO extends AbstractObservatoryInstrumentDAO
                                          implements ObservatoryInstrumentDAOInterface
    {
    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("discoverUninstalled");
        pool.add("discoverSerial");
        pool.add("scanEthernet");
        pool.add("discoverEthernet");
        pool.add("saveDiscoveries");
        pool.add("uploadOS");
        pool.add("getNetworkInterfaces");
        }


    /***********************************************************************************************
     * Construct a ObservatoryExplorerDAO.
     *
     * @param hostinstrument
     */

    public ObservatoryExplorerDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

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
                               "ObservatoryExplorerDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

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
                               "ObservatoryExplorerDAO.disposeDAO()");

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
        return (new ObservatoryExplorerCommandMessage(dao,
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
        return (new ObservatoryExplorerResponseMessage(portname,
                                                       instrumentxml,
                                                       module,
                                                       command,
                                                       starscript.trim(),
                                                       responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Commands                                                                               */
    /***********************************************************************************************
     * discoverUninstalled().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface discoverUninstalled(final CommandMessageInterface commandmessage)
        {
        //return (DiscoverUninstalled.discoverUninstalled(this, commandmessage));
        return (AwaitingDevelopment.doAwaitingDevelopment(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Staribus                                                                                   */
    /***********************************************************************************************
     * discoverSerial().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface discoverSerial(final CommandMessageInterface commandmessage)
        {
        //return (DiscoverSerial.doDiscoverSerial(this, commandmessage));
        return (AwaitingDevelopment.doAwaitingDevelopment(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Starinet                                                                                   */
    /***********************************************************************************************
     * scanEthernet().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

//    public ResponseMessageInterface scanEthernet(final CommandMessageInterface commandmessage)
//        {
//        return (ScanEthernet.doScanEthernet(this, commandmessage));
//        }


    /***********************************************************************************************
     * discoverEthernet().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface discoverEthernet(final CommandMessageInterface commandmessage)
        {
        //return (DiscoverEthernet.doDiscoverEthernet(this, commandmessage));
        return (AwaitingDevelopment.doAwaitingDevelopment(this, commandmessage));
        }


    /***********************************************************************************************
     * saveDiscoveries().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface saveDiscoveries(final CommandMessageInterface commandmessage)
        {
        //return (SaveDiscoveries.doSaveDiscoveries(this, commandmessage));
        return (AwaitingDevelopment.doAwaitingDevelopment(this, commandmessage));
        }


    /***********************************************************************************************
     * uploadOS().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface uploadOS(final CommandMessageInterface commandmessage)
        {
        return (UploadControllerOS.doUploadControllerOS(this, commandmessage));
        }


    /***********************************************************************************************
     * getNetworkInterfaces().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getNetworkInterfaces(final CommandMessageInterface commandmessage)
        {
        return (GetNetworkInterfaces.doGetNetworkInterfaces(this, commandmessage));
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Read all the Resources required by the ObservatoryExplorerDAO.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "ObservatoryExplorerDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        }
    }
