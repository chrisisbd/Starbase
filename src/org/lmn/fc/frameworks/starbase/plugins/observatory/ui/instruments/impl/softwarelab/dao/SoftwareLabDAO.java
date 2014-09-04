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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.softwarelab.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.softwarelab.SoftwareLab;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.softwarelab.SoftwareLabInstrumentPanelInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.util.List;


/***************************************************************************************************
 * SoftwareLabDAO.
 */

public final class SoftwareLabDAO extends AbstractObservatoryInstrumentDAO
                                  implements ObservatoryInstrumentDAOInterface
    {
    public static final int DAO_CHANNEL_COUNT = 1;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("enableJavaConsole");
        pool.add("exportJavaConsole");
        }


    /***********************************************************************************************
     * Construct a SoftwareLabDAO.
     *
     * @param hostinstrument
     */

    public SoftwareLabDAO(final ObservatoryInstrumentInterface hostinstrument)
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
                               "SoftwareLabDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

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
                               "SoftwareLabDAO.disposeDAO()");

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
        return (new SoftwareLabCommandMessage(dao,
                                              instrumentxml,
                                              module,
                                              command,
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
        return (new SoftwareLabResponseMessage(portname,
                                               instrumentxml,
                                               module,
                                               command,
                                               starscript.trim(),
                                               responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * SoftwareLab control the JavaConsole.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface enableJavaConsole(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SoftwareLabDAO.enableJavaConsole() ";
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(), SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, a control boolean
        listParameters = commandType.getParameterList();
        responseMessage = null;

        if ((listParameters != null)
            && (listParameters.size() == 1)
            && (listParameters.get(0) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
            && (getHostInstrument() != null)
            && (getHostInstrument() instanceof SoftwareLab))
            {
            final boolean boolRun;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRun = Boolean.parseBoolean(listParameters.get(0).getValue());

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + boolRun);

            if ((getHostInstrument().getInstrumentPanel() != null)
                && (getHostInstrument().getInstrumentPanel() instanceof SoftwareLabInstrumentPanelInterface)
                && (((SoftwareLab)getHostInstrument()).isJavaConsoleInstalled() != boolRun))
                {
                ((SoftwareLabInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getJavaConsole().enableJavaConsole(boolRun);
                ((SoftwareLab)getHostInstrument()).setJavaConsoleInstalled(boolRun);

                // Something has changed, we may need to refresh a browser etc.
                InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

                // Create the ResponseMessage
                commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                    commandmessage,
                                                                                    commandType);
                }
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


    /**********************************************************************************************/
    /* Exporters - all as Local Commands                                                          */
    /***********************************************************************************************
     * exportJavaConsole().
     * Saves the SoftwareLab JavaConsole at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportJavaConsole(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "SoftwareLabDAO.exportJavaConsole()";

        if ((getHostInstrument().getInstrumentPanel() != null)
            && (getHostInstrument().getInstrumentPanel() instanceof SoftwareLabInstrumentPanelInterface))
            {
            return (exportReportTable(SOURCE,
                                      commandmessage,
                                      null,
                                      null,
                                      true,
                                      DatasetType.TIMESTAMPED,
                                      ((SoftwareLabInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getJavaConsole()));
            }
        else
            {
            final ResponseMessageInterface responseMessage;

            // If the Command failed, do not change any DAO data containers!
            // Our valuable data must remain available for export later...
            // Don't affect the CommandType of the incoming Command
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(this,
                                                                                  commandmessage,
                                                                                  (CommandType) commandmessage.getCommandType().copy(),
                                                                                  null);
            return (responseMessage);
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the SoftwareLabDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SoftwareLabDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
