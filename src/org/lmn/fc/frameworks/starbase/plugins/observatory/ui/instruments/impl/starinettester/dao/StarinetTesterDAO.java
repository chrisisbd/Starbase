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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.starinettester.dao;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.UDPClient;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.io.IOException;
import java.net.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * StarinetTesterDAO.
 */

public final class StarinetTesterDAO extends AbstractObservatoryInstrumentDAO
                                     implements ObservatoryInstrumentDAOInterface
    {
    private static final String DEFAULT_URL = "http://localhost:8080/index.html";

    private String strReferrerURL;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("sendHttpRequest");
        pool.add("sendUdpDatagram");
        }


    /***********************************************************************************************
     * Construct a StarinetTesterDAO.
     *
     * @param hostinstrument
     */

    public StarinetTesterDAO(final ObservatoryInstrumentInterface hostinstrument)
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
                               "StarinetTesterDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Clear all DAO data containers.
     * Be careful to take account of the flag which indicates if the host Instrument consumes
     * the data from this DAO. If not, e.g. leave the **host's** data containers alone!
     * Re-initialise the EventLog and Metadata.
     * Make sure that there's somewhere to accumulate each Response.
     */

    public void clearData()
        {
        // This takes account of isInstrumentDataConsumer()
        super.clearData();

        // Make sure that there's somewhere to accumulate each Response
        setRawData(new Vector<Object>(100));
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarinetTesterDAO.disposeDAO()");

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
        return (new StarinetTesterCommandMessage(dao,
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
        return (new StarinetTesterResponseMessage(portname,
                                                  instrumentxml,
                                                  module,
                                                  command,
                                                  starscript.trim(),
                                                  responsestatusbits));
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * sendHttpRequest().
     * http://hc.apache.org/httpclient-3.x/tutorial.html
     * http://www.eboga.org/java/open-source/httpclient-demo.html
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface sendHttpRequest(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StarinetTesterDAO.sendHttpRequest()";
        final int PARAMETER_COUNT = 1;
        final int CHANNEL_COUNT = 1;
        final CommandType commandType;
        ResponseMessageInterface responseMessage;
        final HttpClient client;
        HttpMethod method;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();
        responseMessage = null;

        // Do not change any DAO data containers!
        clearEventLogFragment();

        // Set up the HttpClient
        client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                        new DefaultHttpMethodRetryHandler(1, false));

        // Initialise the Method with a default URL
        method = new GetMethod(DEFAULT_URL);

        try
            {
            final List<ParameterType> listParameters;

            // We expect one Parameter, the URL
            listParameters = commandType.getParameterList();

            if ((listParameters != null)
                && (listParameters.size() == PARAMETER_COUNT)
                && (SchemaDataType.STRING.equals(listParameters.get(0).getInputDataType().getDataTypeName())))
                {
                final String strURLInput;
                final int intStatus;
                final String strResponseBody;

                strURLInput = listParameters.get(0).getValue();

                // Use the default URL if the Parameter was blank
                if ((strURLInput != null)
                    && (!EMPTY_STRING.equals(strURLInput.trim())))
                    {
                    method = new GetMethod(strURLInput);
                    }

                // See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
                // The User-Agent request-header field contains information about the user agent originating the request
                method.setRequestHeader("User-Agent",
                                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
                // The Referer[sic] request-header field allows the client to specify, for the server's benefit,
                // the address (URI) of the resource from which the Request-URI was obtained
                // (the "referrer", although the header field is misspelled.)
                method.setRequestHeader("Referer",
                                        strReferrerURL);

                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_STARINET
                                                       + METADATA_ACTION_REQUEST_HTTP
                                                       + METADATA_URL_REQUEST + strURLInput + TERMINATOR + SPACE
                                                       + METADATA_URL_REFERRER + strReferrerURL + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());
                // Now try to send the Request
                intStatus = client.executeMethod(method);

                // Check the server status
                if (intStatus != HttpStatus.SC_OK)
                    {
                    SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_STARINET
                                                            + METADATA_ACTION_REQUEST_HTTP
                                                            + METADATA_MESSAGE + ERROR_HTTP + method.getStatusLine() + TERMINATOR,
                                                       SOURCE,
                                                       getObservatoryClock());
                    }

                // It is vital that the response body is *always* read,
                // regardless of the status returned by the server.
                strResponseBody = method.getResponseBodyAsString();

                if ((strResponseBody != null)
                    && (getRawData() != null))
                    {
                    final Vector vecResponseBody;

                    // If we get here, it must have succeeded...
                    // Add one channel of data, the Response
                    // There must be one Calendar and ChannelCount samples in the Vector...
                    vecResponseBody = new Vector(2);
                    vecResponseBody.add(getObservatoryClock().getCalendarDateNow());
                    vecResponseBody.add(strResponseBody);

                    getRawData().add(vecResponseBody);
                    setRawDataChannelCount(CHANNEL_COUNT);
                    setTemperatureChannel(false);

                    // Create the ResponseMessage
                    // The ResponseValue is just 'Ok'
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                else
                    {
                    // No data are available
                    SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       METADATA_TARGET_STARINET
                                                            + METADATA_ACTION_REQUEST_HTTP
                                                            + METADATA_MESSAGE + ERROR_PARSE_DATA + TERMINATOR,
                                                       SOURCE,
                                                       getObservatoryClock());
                    }
                }
            else
                {
                throw new NumberFormatException(ResponseMessageStatus.INVALID_PARAMETER.getName());
                }
            }

        catch (NumberFormatException exception)
            {
            // Invalid Parameters
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.FATAL,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_HTTP
                                                    + METADATA_EXCEPTION + ERROR_PARSE_INPUT + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (IllegalArgumentException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.FATAL,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_HTTP
                                                    + METADATA_EXCEPTION + ERROR_PARSE_INPUT + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (HttpException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_HTTP
                                                    + METADATA_EXCEPTION + ERROR_HTTP + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_HTTP
                                                    + METADATA_EXCEPTION + ERROR_IO + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        finally
            {
            method.releaseConnection();
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(this,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }


    /***********************************************************************************************
     * sendUdpDatagram().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface sendUdpDatagram(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StarinetTesterDAO.sendUdpDatagram()";
        final int PARAMETER_COUNT = 3;
        final int CHANNEL_COUNT = 1;
        final int TIMEOUT_MSEC = 10000;
        final CommandType commandType;
        UDPClient clientUDP;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();
        clientUDP = null;
        responseMessage = null;

        // Do not change any DAO data containers!
        clearEventLogFragment();

        try
            {
            final List<ParameterType> listParameters;

            // We expect three Parameters, the Hostname, Port and Payload
            listParameters = commandType.getParameterList();

            if ((listParameters != null)
                && (listParameters.size() == PARAMETER_COUNT)
                && (SchemaDataType.STRING.equals(listParameters.get(0).getInputDataType().getDataTypeName()))
                && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(1).getInputDataType().getDataTypeName()))
                && (SchemaDataType.STRING.equals(listParameters.get(2).getInputDataType().getDataTypeName())))
                {
                final String strHostname;
                final String strPortInput;
                final int intPort;
                final String strPayload;
                final InetAddress inetAddress;
                final byte[] arrayResponse;

                strHostname = listParameters.get(0).getValue();
                strPortInput = listParameters.get(1).getValue();
                strPayload = listParameters.get(2).getValue();

                intPort = Integer.parseInt(strPortInput);

                SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_STARINET
                                                       + METADATA_ACTION_REQUEST_UDP
                                                       + METADATA_HOSTNAME + strHostname + TERMINATOR + SPACE
                                                       + METADATA_PORT + intPort + TERMINATOR + SPACE
                                                       + METADATA_PAYLOAD + Utilities.byteArrayToSpacedHex(strPayload.getBytes()) + TERMINATOR,
                                                   SOURCE,
                                                   getObservatoryClock());

                inetAddress = InetAddress.getByName(strHostname);
                clientUDP = new UDPClient(inetAddress, intPort, intPort, TIMEOUT_MSEC);

                // If the payload is longer than the maximum reliable length of a UDP Datagram
                // (8192) bytes then an IOException is thrown
                clientUDP.connect();
                clientUDP.send(strPayload.getBytes());

                // This method blocks until a UDP Datagram is received
                // This Command is being executed on its own SwingWorker, so this doesn't matter...
                arrayResponse = clientUDP.receive();

                if ((arrayResponse != null)
                    && (arrayResponse.length > 0)
                    && (getRawData() != null))
                    {
                    final Vector vecResponse;

                    // If we get here, it must have succeeded...
                    // Add one channel of data, the Response
                    // There must be one Calendar and ChannelCount samples in the Vector...
                    vecResponse = new Vector(2);
                    vecResponse.add(getObservatoryClock().getCalendarDateNow());
                    vecResponse.add(Utilities.byteArrayToSpacedHex(arrayResponse));

                    getRawData().add(vecResponse);
                    setRawDataChannelCount(CHANNEL_COUNT);
                    setTemperatureChannel(false);

                    // Create the ResponseMessage
                    // The ResponseValue is just 'Ok'
                    commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                    responseMessage = ResponseMessageHelper.constructSuccessfulResponse(this,
                                                                                        commandmessage,
                                                                                        commandType);
                    }
                else
                    {
                    LOGGER.error(SOURCE + " Can't make response");
                    }
                }
            else
                {
                throw new NumberFormatException(ResponseMessageStatus.INVALID_PARAMETER.getName());
                }
            }

        catch (NumberFormatException exception)
            {
            // Invalid Parameters
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.FATAL,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_PARSE_INPUT + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (IllegalArgumentException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.FATAL,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_PARSE_INPUT + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (UnknownHostException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_UNKNOWN_HOST + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (PortUnreachableException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_PORT + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (SecurityException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_SECURITY + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (SocketException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_SOCKET + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (SocketTimeoutException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_TIMEOUT + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (IllegalBlockingModeException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_ILLEGAL_MODE + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        catch (IOException exception)
            {
            SimpleEventLogUIComponent.logEvent(getEventLogFragment(),
                                               EventStatus.WARNING,
                                               METADATA_TARGET_STARINET
                                                    + METADATA_ACTION_REQUEST_UDP
                                                    + METADATA_EXCEPTION + ERROR_IO + TERMINATOR + SPACE
                                                    + METADATA_MESSAGE + exception.getMessage() + TERMINATOR,
                                               SOURCE,
                                               getObservatoryClock());
            }

        finally
            {
            // Make sure that the Socket is released
            if (clientUDP != null)
                {
                clientUDP.close();
                }
            }

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(this,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }


    /***********************************************************************************************
     * Read all the Resources required by the StarinetTesterDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     * KEY_DAO_UPDATE_PERIOD
     *
     * KEY_DAO_URL_DEFAULT
     * KEY_DAO_URL_REFERRER
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarinetTesterDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        strReferrerURL = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_URL_REFERRER);
        }
    }
