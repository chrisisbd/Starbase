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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.dao;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.ObservatoryMonitor;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.ObservatoryMonitorHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.ObservatoryMonitorInstrumentPanelInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.util.List;


/***************************************************************************************************
 * MonitorDAO.
 */

public final class MonitorDAO extends AbstractObservatoryInstrumentDAO
                              implements ObservatoryInstrumentDAOInterface
    {
    private static final int DAO_CHANNEL_COUNT = 1;

    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("monitorCommands");
        pool.add("monitorPorts");
        pool.add("monitorMemory");
        pool.add("enableJavaConsole");
        pool.add("exportCommandMonitor");
        pool.add("exportPortMonitor");
        pool.add("exportMemoryMonitor");
        pool.add("exportActions");
        pool.add("exportJavaConsole");
        pool.add("exportObservatoryLog");
        }


    /***********************************************************************************************
     * Construct a MonitorDAO.
     *
     * @param hostinstrument
     */

    public MonitorDAO(final ObservatoryInstrumentInterface hostinstrument)
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
                               "MonitorDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

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
                               "MonitorDAO.disposeDAO()");

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
        return (new MonitorCommandMessage(dao,
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
        return (new MonitorResponseMessage(portname,
                                           instrumentxml,
                                           module,
                                           command,
                                           starscript.trim(),
                                           responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * Monitor the Commands.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface monitorCommands(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.monitorCommands() ";
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

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
            && (getHostInstrument() instanceof ObservatoryMonitor))
            {
            final boolean boolRun;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRun = Boolean.parseBoolean(listParameters.get(0).getValue());

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + boolRun);

            ((ObservatoryMonitor)getHostInstrument()).setCommandMonitorRunning(boolRun);

            // Something has changed, we may need to refresh a browser etc.
            InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

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
     * Monitor the Ports.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface monitorPorts(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.monitorPorts() ";
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

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
            && (getHostInstrument() instanceof ObservatoryMonitor))
            {
            final boolean boolRun;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRun = Boolean.parseBoolean(listParameters.get(0).getValue());

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + boolRun);

            ((ObservatoryMonitor)getHostInstrument()).setPortMonitorRunning(boolRun);

            // Something has changed, we may need to refresh a browser etc.
            InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

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
     * Monitor the MemoryUsage.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface monitorMemory(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.monitorMemory() ";
        final int INDEX_RUN = 0;
        final CommandType cmdMonitor;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        cmdMonitor = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, a control boolean
        listParameters = cmdMonitor.getParameterList();
        responseMessage = null;

        if ((listParameters != null)
            && (listParameters.size() == 1)
            && (listParameters.get(INDEX_RUN) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_RUN).getInputDataType().getDataTypeName()))
            && (getHostInstrument() != null)
            && (getHostInstrument() instanceof ObservatoryMonitor))
            {
            final boolean boolRun;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRun = Boolean.parseBoolean(listParameters.get(INDEX_RUN).getValue());

            if (boolRun)
                {
                // Establish the identity of this Instrument using Metadata
                // from the Framework, Observatory and Observer
                establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdMonitor),
                                               DAO_CHANNEL_COUNT,
                                               false,
                                               ObservatoryMonitorHelper.createMemoryMonitorChannelMetadata(),
                                               null);
                }
            else
                {
                // This takes account of isInstrumentDataConsumer()
                clearData();
                }

            ((ObservatoryMonitor)getHostInstrument()).setMemoryMonitorRunning(boolRun);

            // Something has changed, we may need to refresh a browser etc.
            InstrumentHelper.notifyInstrumentChanged(getHostInstrument());

            // Create the ResponseMessage
            cmdMonitor.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                commandmessage,
                                                                                cmdMonitor);
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
                                                                           cmdMonitor,
                                                                           AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                                            commandmessage.getModule(),
                                                                                                                            cmdMonitor));
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * Monitor control the JavaConsole.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface enableJavaConsole(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.enableJavaConsole() ";
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
            && (getHostInstrument() instanceof ObservatoryMonitor))
            {
            final boolean boolRun;

            // This should never throw NumberFormatException, because it has already been parsed
            boolRun = Boolean.parseBoolean(listParameters.get(0).getValue());

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + boolRun);

            if ((getHostInstrument().getInstrumentPanel() != null)
                && (getHostInstrument().getInstrumentPanel() instanceof ObservatoryMonitorInstrumentPanelInterface)
                && (((ObservatoryMonitor)getHostInstrument()).isJavaConsoleInstalled() != boolRun))
                {
                ((ObservatoryMonitorInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getJavaConsole().enableJavaConsole(boolRun);
                ((ObservatoryMonitor)getHostInstrument()).setJavaConsoleInstalled(boolRun);

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
     * exportCommandMonitor().
     * Saves the Command Monitor at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportCommandMonitor(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.exportCommandMonitor()";

        if ((getHostInstrument().getInstrumentPanel() != null)
            && (getHostInstrument().getInstrumentPanel() instanceof ObservatoryMonitorInstrumentPanelInterface))
            {
            return (exportReportTable(SOURCE,
                                      commandmessage,
                                      null,
                                      null,
                                      true,
                                      DatasetType.TABULAR, // ToDo Could be TIMESTAMPED?
                                      ((ObservatoryMonitorInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getCommandMonitor()));
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
     * exportPortMonitor().
     * Saves the Port Monitor at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportPortMonitor(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.exportPortMonitor()";

        if ((getHostInstrument().getInstrumentPanel() != null)
            && (getHostInstrument().getInstrumentPanel() instanceof ObservatoryMonitorInstrumentPanelInterface))
            {
            return (exportReportTable(SOURCE,
                                      commandmessage,
                                      null,
                                      null,
                                      true,
                                      DatasetType.TABULAR, // ToDo Could be TIMESTAMPED?
                                      ((ObservatoryMonitorInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getPortMonitor()));
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
     * exportMemoryMonitor().
     * Saves the current MemoryMonitor chart as an image at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportMemoryMonitor(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.exportMemoryMonitor()";

        // Simply change the name of the Command!
        // The Chart is already attached to the DAOWrapper
        return (exportChart(commandmessage));
        }


    /***********************************************************************************************
     * exportActions().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportActions(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.exportActions()";

        if ((getHostInstrument().getInstrumentPanel() != null)
            && (getHostInstrument().getInstrumentPanel() instanceof ObservatoryMonitorInstrumentPanelInterface))
            {
            return (exportReportTable(SOURCE,
                                      commandmessage,
                                      null,
                                      null,
                                      true,
                                      DatasetType.TABULAR,
                                      ((ObservatoryMonitorInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getActions()));
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
     * exportJavaConsole().
     * Saves the Monitor JavaConsole at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportJavaConsole(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.exportJavaConsole()";

        if ((getHostInstrument().getInstrumentPanel() != null)
            && (getHostInstrument().getInstrumentPanel() instanceof ObservatoryMonitorInstrumentPanelInterface))
            {
            return (exportReportTable(SOURCE,
                                      commandmessage,
                                      null,
                                      null,
                                      true,
                                      DatasetType.TIMESTAMPED,
                                      ((ObservatoryMonitorInstrumentPanelInterface)getHostInstrument().getInstrumentPanel()).getJavaConsole()));
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
     * exportObservatoryLog().
     * Saves the composite Observatory Log at the specified location.
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportObservatoryLog(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "MonitorDAO.exportObservatoryLog()";

        if ((getHostInstrument().getInstrumentPanel() != null)
            && (getHostInstrument().getInstrumentPanel().getEventLogTab() != null)
            && (getHostInstrument().getInstrumentPanel().getEventLogTab() instanceof ReportTablePlugin))
            {
            return (exportReportTable(SOURCE,
                                      commandmessage,
                                      null,
                                      null,
                                      true,
                                      DatasetType.TIMESTAMPED,
                                      (ReportTablePlugin)getHostInstrument().getInstrumentPanel().getEventLogTab()));
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
     * Read all the Resources required by the MonitorDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "MonitorDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
