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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.lego.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.RediscoverMyself;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration.WriteCommandMap;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.interfaces.GetStarinetAddress;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.CaptureCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.StaribusHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StarinetCommandMessage;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StarinetResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***************************************************************************************************
 * LegoDemonstratorDAO.
 */

public final class LegoDemonstratorDAO extends StaribusCoreDAO
                                       implements ObservatoryInstrumentDAOInterface
    {
    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("captureRawDataRealtime");
        pool.add("rediscoverMyself");
        pool.add("getStarinetAddress");

        // ToDo Review putting this in the StaribusCoreDAO
        pool.add("writeCommandMap");
        }


    /***********************************************************************************************
     * Construct a LegoDemonstratorDAO.
     *
     * @param hostinstrument
     */

    public LegoDemonstratorDAO(final ObservatoryInstrumentInterface hostinstrument)
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
        LOGGER.debug(isDebugMode(),
                     "LegoDemonstratorDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debug(isDebugMode(),
                     "LegoDemonstratorDAO.disposeDAO()");

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
        // Use a dummy StaribusAddress field of 00
        return (new StarinetCommandMessage(dao,
                                           instrumentxml,
                                           module,
                                           command,
                                           StreamType.STARINET,
                                           0,
                                           starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
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
        // Use a dummy StaribusAddress field of 00
        return (new StarinetResponseMessage(portname,
                                            instrumentxml,
                                            module,
                                            command,
                                            0,
                                            starscript.trim(),
                                            responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * captureRawDataRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureRawDataRealtime(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "LegoDemonstratorDAO.captureRawDataRealtime()";

        // Only generate a ResponseMessage when completed
        return (CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand(this,
                                                                                      commandmessage,
                                                                                      StaribusHelper.createMultichannelChartLegendMetadata("Lego Demonstrator",
                                                                                                                                           "Time (UT)",
                                                                                                                                           "Colorimeter Output"),
                                                                                      SOURCE,
                                                                                      false));
        }


    /***********************************************************************************************
     * rediscoverMyself().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface rediscoverMyself(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "LegoDemonstratorDAO.rediscoverMyself() ";

        return (RediscoverMyself.doRediscoverMyself(this, commandmessage));
        }


    /***********************************************************************************************
     * getStarinetAddress().
     * A local implementation, reading directly from the loaded XML, not the Controller memory.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getStarinetAddress(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "LegoDemonstratorDAO.getStarinetAddress() ";

        return (GetStarinetAddress.doGetStarinetAddress(this, commandmessage));
        }


    /***********************************************************************************************
     * writeCommandMap().
     * ToDo Review putting this in the StaribusCoreDAO.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface writeCommandMap(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "LegoDemonstratorDAO.writeCommandMap() ";

        return (WriteCommandMap.doWriteCommandMap(this, commandmessage));
        }


    /***********************************************************************************************
     * Read all the Resources required by the LegoDemonstratorDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     */

    public void readResources()
        {
        LOGGER.debug(isDebugMode(),
                     "LegoDemonstratorDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
