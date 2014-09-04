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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;

import com.sixlegs.png.PngImage;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// This looks to be the best?
// http://code.google.com/p/javapng/

// PNG Tools
// http://www.libpng.org/pub/png/apps/pngcheck.html
// http://www.schaik.com/pngsuite/
// http://code.google.com/p/javapng/wiki/BrokenSuite

// Storing Images in XML!
// http://www.feike.biz/base64_java_xml_en.html

// This doesn't seem very good (all the same)
// http://www.java2s.com/Code/Java/2D-Graphics-GUI/PNGfileformatdecoder.htm
// http://www.java-forums.org/java-awt/9285-png-file-format-decoder-java.html
// http://www.java-tips.org/java-se-tips/java.awt.image/png-file-format-decoder-in-java.html


/***************************************************************************************************
 * ImportImageRemote.
 */

public final class ImportImageRemote implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons,
                                                ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportImageRemote().
     * http://hc.apache.org/httpclient-3.x/tutorial.html
     * http://www.eboga.org/java/open-source/httpclient-demo.html
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportImageRemote(final ObservatoryInstrumentDAOInterface dao,
                                                               final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportImageRemote.doImportImageRemote()";
        final int PARAMETER_COUNT = 1;
        final int INDEX_URL = 0;
        final CommandType commandType;
        final List<ParameterType> listParameters;
        ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        commandType = (CommandType)commandmessage.getCommandType().copy();

        // We expect one parameter, the filename
        listParameters = commandType.getParameterList();
        responseMessage = null;

        // TODO REVIEW Initialise all DAO data containers if possible
        // dao.clearData();

        // Check the Command parameters before continuing to retrieve the data file
        // Note that a FileChooser is not provided for remote file retrieval!
        if ((commandmessage.getInstrument() != null)
            && (commandmessage.getInstrument().getController() != null)
            && (commandmessage.getInstrument().getController().getVirtualAddress() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_URL) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_URL).getInputDataType().getDataTypeName())))
            {
            final String strURL;

            strURL = listParameters.get(INDEX_URL).getValue();

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "ImportImageRemote.importImageRemote() [url=" + strURL + "]");

            if ((strURL != null)
                && (!EMPTY_STRING.equals(strURL.trim())))
                {
                final HttpClient client;
                final HttpMethod method;

                // Set up the HttpClient
                client = new HttpClient();
                client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                                new DefaultHttpMethodRetryHandler(1, false));

                method = new GetMethod(strURL);

                // See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
                // The User-Agent request-header field contains information about the user agent originating the request
                method.setRequestHeader("User-Agent",
                                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
                // The Referer[sic] request-header field allows the client to specify, for the server's benefit,
                // the address (URI) of the resource from which the Request-URI was obtained
                // (the "referrer", although the header field is misspelled.)
                method.setRequestHeader("Referer",
                                        strURL + "/Jview.htm");
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
                                                           ObservatoryInstrumentDAOInterface.ERROR_HTTP + SPACE + method.getStatusLine(),
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        }

                    // It is vital that the response body is always read,
                    // regardless of the status returned by the server.
                    inputStream = method.getResponseBodyAsStream();

                    if (inputStream != null)
                        {
                        BufferedImage image;

                        image = null;

                        if ((strURL.endsWith(FileUtilities.jpg))
                            || (strURL.endsWith(FileUtilities.jpeg)))
                            {
                            image = ImageIO.read(inputStream);
                            }
                        else if (strURL.endsWith(FileUtilities.png))
                            {
                            final PngImage pngImage;

                            // http://code.google.com/p/javapng/
                            pngImage = new PngImage();
                            image = pngImage.read(inputStream, true);
                            }

                        //--------------------------------------------------------------------------
                        // If we get here, it must have succeeded...

                        if (image != null)
                            {
                            // Pass the Image to the DAO, and then to the Instrument and InstrumentPanel
                            dao.setImageData(image);

                            // Don't force the user to export imported data, but don't change the state of SavedData
                            // Do not affect RawData, because Images have different containers
                            // So don't change the ChannelCount or Temperature flag

                            // If we get here, we have the image...
                            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                               EventStatus.INFO,
                                                               METADATA_TARGET_IMAGE
                                                                   + METADATA_ACTION_IMPORT
                                                                   + METADATA_URL_REQUEST + strURL + TERMINATOR,
                                                               dao.getLocalHostname(),
                                                               dao.getObservatoryClock());

                            REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                            InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

                            // Create the ResponseMessage
                            // The captureImage() operation normally just requires an Ack, i.e. no ResponseValue
                            commandType.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());

                            responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                                                commandmessage,
                                                                                                commandType);
                            }
                        }
                    else
                        {
                        // No data are available
                        SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                           EventStatus.WARNING,
                                                           ObservatoryInstrumentDAOInterface.ERROR_PARSE_DATA,
                                                           SOURCE,
                                                           dao.getObservatoryClock());
                        }
                    }

                catch (NumberFormatException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }

                catch (IllegalArgumentException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.FATAL,
                                                       ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage(),
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }

                catch (HttpException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       ObservatoryInstrumentDAOInterface.ERROR_HTTP + exception.getMessage(),
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }

                catch (IOException exception)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.WARNING,
                                                       ObservatoryInstrumentDAOInterface.ERROR_IO + exception.getMessage(),
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }

                finally
                    {
                    method.releaseConnection();
                    }
                }
            else
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_TARGET_IMAGE
                                                        + METADATA_ACTION_IMPORT
                                                        + METADATA_RESULT + "Image URL is not valid" + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                }
            }

        REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
        InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());

        // If the Command failed, do not change any DAO data containers!
        // Our valuable data must remain available for export later...
        // Construct INVALID_PARAMETER
        responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                              commandmessage,
                                                                              commandType,
                                                                              responseMessage);
        return (responseMessage);
        }
    }
