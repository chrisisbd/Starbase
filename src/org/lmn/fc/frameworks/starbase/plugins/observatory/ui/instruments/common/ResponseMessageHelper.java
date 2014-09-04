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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import org.apache.xmlbeans.XmlObject;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.AbstractResponseMessage;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;

import java.util.Vector;


/***************************************************************************************************
 * ResponseMessageHelper.
 */

public final class ResponseMessageHelper implements FrameworkConstants,
                                                    FrameworkStrings,
                                                    FrameworkMetadata,
                                                    FrameworkSingletons
    {
    /***********************************************************************************************
     * Create a ResponseMessage, given a List of ResponseMessageStatus and a ResponseValue,
     * Use this one in preference - trying to remove all of the others!
     *
     * @param dao
     * @param commandmessage
     * @param command
     * @param rawdata
     * @param xydataset
     * @param responsevalue
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface createResponseMessage(final ObservatoryInstrumentDAOInterface dao,
                                                                 final CommandMessageInterface commandmessage,
                                                                 final CommandType command,
                                                                 final Vector<Object> rawdata,
                                                                 final XYDataset xydataset,
                                                                 final String responsevalue)
        {
        final ResponseMessageInterface responseMessage;

        // Create the ResponseMessage, if possible for this command
        if ((command != null)
            && (command.getResponse() != null))
            {
            command.getResponse().setValue(responsevalue);
            }

        if ((dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
            || (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT)))
            {
            // Update the XYDataset and Logs
            updateWrappedData(dao,
                              rawdata,
                              xydataset,
                              true);

            // This will do a final setWrappedData() and clear the Logs
            // Return the full details in the ResponseMessageStatus List (e.g. may also contain CAPTURE_ACTIVE)
            responseMessage = constructSuccessfulResponseWithStatus(dao,
                                                                    commandmessage,
                                                                    command);
            }
        else
            {
            // Create the failed ResponseMessage
            responseMessage = constructEmptyResponse(dao,
                                                     commandmessage,
                                                     commandmessage.getInstrument(),
                                                     commandmessage.getModule(),
                                                     command,
                                                     AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                      commandmessage.getModule(),
                                                                                                      command));
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * A convenience method to construct a successful ResponseMessage using the DAO data.
     * The ResponseMessageStatus is only SUCCESS.
     * The ResponseValue is taken from the CommandType ResponseValue, if possible.
     * The DAO must not be null.
     *
     * @param dao
     * @param commandmessage
     * @param commandtype
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructSuccessfulResponse(final ObservatoryInstrumentDAOInterface dao,
                                                                       final CommandMessageInterface commandmessage,
                                                                       final CommandType commandtype)
        {
        final String SOURCE = "ResponseMessageHelper.constructSuccessfulResponse() ";
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isThreadsDebug()
                     || LOADER_PROPERTIES.isStateDebug());

        LOGGER.debug(boolDebug,
                     SOURCE + "ON ENTRY [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList()) + "]");

        // Preserve any ABORT
        if (!dao.getResponseMessageStatusList().contains(ResponseMessageStatus.ABORT))
            {
            dao.getResponseMessageStatusList().clear();
            }

        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

        LOGGER.debug(boolDebug,
                     SOURCE + "ON EXIT [response.status=" + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList()) + "]");

        return (constructSuccessfulResponseWithStatus(dao, commandmessage, commandtype));
        }


    /***********************************************************************************************
     * A convenience method to construct a successful ResponseMessage using the DAO data.
     * The ResponseValue is taken from the CommandType ResponseValue, if possible.
     * Return the full details in the ResponseMessageStatus List.
     * The DAO must not be null.
     *
     * @param dao
     * @param commandmessage
     * @param commandtype
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructSuccessfulResponseWithStatus(final ObservatoryInstrumentDAOInterface dao,
                                                                                 final CommandMessageInterface commandmessage,
                                                                                 final CommandType commandtype)
        {
        final String SOURCE = "ResponseMessageHelper.constructSuccessfulResponseWithStatus() ";
        final ResponseMessageInterface responseMessage;
        final String strResponseValue;

        responseMessage = dao.constructResponseMessage(getPortName(dao),
                                                       commandmessage.getInstrument(),
                                                       commandmessage.getModule(),
                                                       commandtype,
                                                       AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                        commandmessage.getModule(),
                                                                                                        commandtype),
                                                       ResponseMessageStatus.convertResponseStatusCodesToBits(dao.getResponseMessageStatusList()));

        // Set the ResponseValue if required
        if ((responseMessage.getCommandType() != null)
            && (responseMessage.getCommandType().getResponse() != null))
            {
            strResponseValue = responseMessage.getCommandType().getResponse().getValue();
            }
        else
            {
            // The Command just expects an Ack
            strResponseValue = EMPTY_STRING;
            }

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[ResponseValue=" + strResponseValue
                                + "] [logsize=" + dao.getEventLogFragment().size() + "]");

        responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                      responseMessage,
                                                      strResponseValue,
                                                      dao));
        return (responseMessage);
        }


    /***********************************************************************************************
     * A convenience method to construct an empty ResponseMessage,
     * with a specified status and no data.
     * The DAO must not be null.
     *
     * @param dao
     * @param commandmessage
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructEmptyResponse(final ObservatoryInstrumentDAOInterface dao,
                                                                  final CommandMessageInterface commandmessage,
                                                                  final Instrument instrumentxml,
                                                                  final XmlObject module,
                                                                  final CommandType command,
                                                                  final String starscript)
        {
        ResponseMessageInterface responseMessage;
        final String strResponseValue;

        responseMessage = dao.constructResponseMessage(getPortName(dao),
                                                       instrumentxml,
                                                       module,
                                                       command,
                                                       starscript.trim(),
                                                       ResponseMessageStatus.convertResponseStatusCodesToBits(dao.getResponseMessageStatusList()));

        // Does this Command expect a Response?
        if ((responseMessage.getCommandType() != null)
            && (responseMessage.getCommandType().getResponse() != null))
            {
            // The incoming CommandType may have a ResponseValue to pass along
            if ((command.getResponse() != null)
                && (command.getResponse().getValue() != null))
                {
                responseMessage.getCommandType().getResponse().setValue(command.getResponse().getValue());
                }
            else
                {
                responseMessage.getCommandType().getResponse().setValue(EMPTY_STRING);
                }

            // Record the value actually used
            strResponseValue = responseMessage.getCommandType().getResponse().getValue();
            }
        else
            {
            // If a Response was not expected, ensure that the ResponseValue is empty
            strResponseValue = EMPTY_STRING;
            }

        // Wrap it all up into the ResponseMessage
        responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                      responseMessage,
                                                      strResponseValue,
                                                      dao));
        // Timestamp the empty Response
        responseMessage = ObservatoryInstrumentHelper.timestampResponseMessage(responseMessage,
                                                                               dao.getObservatoryClock());

        return (responseMessage);
        }


    /***********************************************************************************************
     * A convenience method to construct a ResponseMessage using the DAO data.
     * The DAO must not be null.
     * The CommandType ResponseValue may carry some status information.
     *
     * @param dao
     * @param commandmessage
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructResponse(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript)
        {
        final String SOURCE = "DAOHelper.constructResponse() ";
        final ResponseMessageInterface responseMessage;
        final String strResponseValue;

        responseMessage = dao.constructResponseMessage(getPortName(dao),
                                                       instrumentxml,
                                                       module,
                                                       command,
                                                       starscript.trim(),
                                                       ResponseMessageStatus.convertResponseStatusCodesToBits(dao.getResponseMessageStatusList()));

        // Set the ResponseValue if required
        // The CommandType ResponseValue may carry some status information,
        // so use it if we can
        if ((command != null)
            && (command.getResponse() != null)
            && (command.getResponse().getValue() != null))
            {
            strResponseValue = command.getResponse().getValue();
            }
        else if ((responseMessage.getCommandType() != null)
            && (responseMessage.getCommandType().getResponse() != null)
            && (responseMessage.getCommandType().getResponse().getValue() != null))
            {
            strResponseValue = responseMessage.getCommandType().getResponse().getValue();
            }
        else
            {
            // No idea what to say!
            strResponseValue = EMPTY_STRING;
            }

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[ResponseValue=" + strResponseValue
                                      + "] [logsize=" + dao.getEventLogFragment().size() + "]");

        responseMessage.setWrappedData(new DAOWrapper(commandmessage,
                                                      responseMessage,
                                                      strResponseValue,
                                                      dao));
        return (responseMessage);
        }


    /***********************************************************************************************
     * Create an ErrorResponse message, mainly used to carry Status back to the UI.
     * If the Command should return a ResponseValue, send NODATA.
     *
     * @param dao
     * @param instrument
     * @param module
     * @param commandtype
     * @param responsestatusbits
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructErrorResponse(final ObservatoryInstrumentDAOInterface dao,
                                                                  final Instrument instrument,
                                                                  final XmlObject module,
                                                                  final CommandType commandtype,
                                                                  final int responsestatusbits)
        {
        final ResponseMessageInterface responseMessage;
        final CommandType commandType;

        commandType = (CommandType)commandtype.copy();

        if (commandType.getResponse() != null)
            {
            commandType.getResponse().setValue(ResponseMessageStatus.RESPONSE_NODATA);
            }

        responseMessage = dao.constructResponseMessage(getPortName(dao),
                                                       instrument,
                                                       module,
                                                       commandType,
                                                       AbstractResponseMessage.buildResponseResourceKey(instrument,
                                                                                                        module,
                                                                                                        commandtype),
                                                       responsestatusbits);
        return (responseMessage);
        }


    /***********************************************************************************************
     * A convenience method to construct a failed ResponseMessage with a status of INVALID_PARAMETER.
     * The DAO must not be null.
     * The CommandType ResponseValue may carry some status information.
     *
     * @param dao
     * @param commandmessage
     * @param commandtype
     * @param responsemessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructFailedResponseIfNull(final ObservatoryInstrumentDAOInterface dao,
                                                                         final CommandMessageInterface commandmessage,
                                                                         final CommandType commandtype,
                                                                         final ResponseMessageInterface responsemessage)
        {
        ResponseMessageInterface responseMessage;

        responseMessage = responsemessage;

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        if (responseMessage == null)
            {
            dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);

            // Invalid Parameters
            // Create the failed ResponseMessage
            // Just feed the existing DaoData back round again
            // The CommandType ResponseValue may carry some status information
            responseMessage = constructResponse(dao,
                                                commandmessage,
                                                commandmessage.getInstrument(),
                                                commandmessage.getModule(),
                                                commandtype,
                                                AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                 commandmessage.getModule(),
                                                                                                 commandtype));
             }

        return (responseMessage);
        }


    /***********************************************************************************************
     * A convenience method to construct a failed ResponseMessage with a specified status.
     * The DAO must not be null.
     *
     * @param dao
     * @param commandmessage
     * @param commandtype
     * @param responsemessage
     * @param status
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface constructFailedResponseIfNullWithStatus(final ObservatoryInstrumentDAOInterface dao,
                                                                                   final CommandMessageInterface commandmessage,
                                                                                   final CommandType commandtype,
                                                                                   final ResponseMessageInterface responsemessage,
                                                                                   final ResponseMessageStatus status)
        {
        ResponseMessageInterface responseMessage;

        responseMessage = responsemessage;

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        if (responseMessage == null)
            {
            dao.getResponseMessageStatusList().add(status);

            // Invalid Parameters
            // Create the failed ResponseMessage
            // Just feed the existing DaoData back round again
            responseMessage = constructResponse(dao,
                                                commandmessage,
                                                commandmessage.getInstrument(),
                                                commandmessage.getModule(),
                                                commandtype,
                                                AbstractResponseMessage.buildResponseResourceKey(commandmessage.getInstrument(),
                                                                                                 commandmessage.getModule(),
                                                                                                 commandtype));
            }
        else
            {
            responseMessage.getResponseMessageStatusList().add(status);
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * Update the RawData, XYDataset and Logs, then clear the LogFragments.
     *
     * @param dao
     * @param rawdata
     * @param xydataset
     * @param updatemetadata
     */

    public static void updateWrappedData(final ObservatoryInstrumentDAOInterface dao,
                                         final Vector<Object> rawdata,
                                         final XYDataset xydataset,
                                         final boolean updatemetadata)
        {
        if ((dao != null)
            && (dao.getHostInstrument() != null))
            {
            // These flags will eventually be cleared by the call to refreshChart()

            if (rawdata != null)
                {
                dao.setRawData(rawdata);
                dao.setRawDataChanged(true);
                dao.setUnsavedData(true);
                }

            if (xydataset != null)
                {
                dao.setXYDataset(xydataset);
                dao.setProcessedDataChanged(true);
                dao.setUnsavedData(true);
                }

            // Update everything on the Instrument
            if (dao.getWrappedData() != null)
                {
                dao.getWrappedData().harmoniseWrapperWithDAO(dao);

                // Only refresh the data if visible
                dao.getHostInstrument().setWrappedData(dao.getWrappedData(), false, updatemetadata);
                }

            if (dao.getEventLogFragment() != null)
                {
                dao.getEventLogFragment().clear();
                }

            if (dao.getInstrumentLogFragment() != null)
                {
                dao.getInstrumentLogFragment().clear();
                }

            // Something has changed, we may need to refresh a ControlPanel etc.
            InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
            }
        }


    /***********************************************************************************************
     * Get the name of the DAO Port, or EMPTY_STRING if there is no Port.
     *
     * @param dao
     *
     * @return String
     */

    public static String getPortName(final ObservatoryInstrumentDAOInterface dao)
        {
        if ((dao != null)
            && (dao.getPort() != null))
            {
            return (dao.getPort().getName());
            }
        else
            {
            return (EMPTY_STRING);
            }
        }
    }
