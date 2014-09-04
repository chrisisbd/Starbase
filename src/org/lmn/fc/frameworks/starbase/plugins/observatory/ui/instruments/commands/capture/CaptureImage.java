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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.capture;


import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.IPVersion;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ControlPanelInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


/***************************************************************************************************
 * CaptureImage.
 */

public final class CaptureImage implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkSingletons,
                                           FrameworkXpath,
                                           ObservatoryConstants
    {
    /***********************************************************************************************
     * doCaptureImage().
     * http://hc.apache.org/httpclient-3.x/tutorial.html
     * http://www.eboga.org/java/open-source/httpclient-demo.html
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doCaptureImage(final ObservatoryInstrumentDAOInterface dao,
                                                          final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "CaptureImage.doCaptureImage()";
        final CommandType cmdCapture;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;

        // Initialise
        dao.clearEventLogFragment();

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdCapture = (CommandType)commandmessage.getCommandType().copy();

        // Ensure we are dealing with an Ethernet-based instrument
        if ((commandmessage.getInstrument() != null)
            && (commandmessage.getInstrument().getController() != null)
            && (commandmessage.getInstrument().getController().getIPAddress() != null)
            && (dao.getRemoteDataConnection() != null))
            {
            final HttpClient client;
            final HttpMethod method;
            final String strURL;

            // Set up the HttpClient
            client = new HttpClient();
            client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                            new DefaultHttpMethodRetryHandler(1, false));

            // Beware filename part (e.g. IMAGE.JPG) *must* be uppercase!
            // Assume that we are dealing with a valid Ethernet Controller
            strURL = ControlPanelInterface.PREFIX_HTTP
                     + IPVersion.stripTrailingPaddingFromIPAddressAndPort(commandmessage.getInstrument().getController().getIPAddress());

            // The DAO Hostname contains the Value of Property KEY_DAO_IMAGE_FILENAME
            method = new GetMethod( strURL + "/" + dao.getRemoteDataConnection().getHostname());

            // See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            // The User-Agent request-header field contains information about the user agent originating the request
            method.setRequestHeader("User-Agent",
                                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
            // The Referer[sic] request-header field allows the client to specify, for the server's benefit,
            // the address (URI) of the resource from which the Request-URI was obtained
            // (the "referrer", although the header field is misspelled.)
            method.setRequestHeader("Referer",
                                    strURL + "/Jview.htm");

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET_IMAGE
                                                   + METADATA_ACTION_CAPTURE
                                                   + METADATA_ADDRESS + strURL + "/" + dao.getRemoteDataConnection().getHostname() + TERMINATOR,
                                               SOURCE,
                                               dao.getObservatoryClock());

            // Establish the identity of this Instrument using Metadata
            // from the Framework, Observatory and Observer
            dao.establishDAOIdentityForCapture(DAOCommandHelper.getCommandCategory(cmdCapture),
                                               0,
                                               false,
                                               null,
                                               null);
            try
                {
                final int intStatus;
                final InputStream inputStream;

                // Now try to get the Image
                intStatus = client.executeMethod(method);

                // Check the server status
                if (intStatus != HttpStatus.SC_OK)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       ObservatoryInstrumentDAOInterface.ERROR_HTTP + method.getStatusLine(),
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }

                // It is vital that the response body is always read,
                // regardless of the status returned by the server.
                inputStream = method.getResponseBodyAsStream();

                if (inputStream != null)
                    {
                    final BufferedImage image;

                    image = ImageIO.read(inputStream);

                    // If we get here, it must have succeeded...
                    // Pass the Image to the DAO, and then to the Instrument and InstrumentPanel
                    dao.setImageData(image);
                    dao.setUnsavedData(true);
                    inputStream.close();

                    strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);
                    }
                else
                    {
                    // No data are available
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       ObservatoryInstrumentDAOInterface.ERROR_PARSE_DATA,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                    dao.getResponseMessageStatusList().add(ResponseMessageStatus.PREMATURE_TERMINATION);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (HttpException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   ObservatoryInstrumentDAOInterface.ERROR_HTTP + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   ObservatoryInstrumentDAOInterface.ERROR_IO + exception.getMessage(),
                                                   SOURCE,
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
                }

            finally
                {
                // It is important to always release the connection,
                // regardless of whether the server returned an error or not
                method.releaseConnection();
                }
            }
        else
            {
            // Incorrectly configured XML
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                     SOURCE,
                                                                                     METADATA_TARGET_IMAGE,
                                                                                     METADATA_ACTION_CAPTURE));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdCapture,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
