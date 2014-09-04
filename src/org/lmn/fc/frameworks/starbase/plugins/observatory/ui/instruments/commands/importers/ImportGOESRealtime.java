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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.importers;


import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataExporter;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.CaptureCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatusList;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * ImportGOESRealtime.
 */

public final class ImportGOESRealtime implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 FrameworkXpath,
                                                 ObservatoryConstants
    {
    /***********************************************************************************************
     * doImportGOESRealtime().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doImportGOESRealtime(final ObservatoryInstrumentDAOInterface dao,
                                                                final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ImportGOESRealtime.doImportGOESRealtime()";
        final int PARAMETER_COUNT = 5;
        final int INDEX_URL = 0;
        final int INDEX_PATHNAME = 1;
        final int INDEX_INTERVAL = 2;
        final int INDEX_PERIOD = 3;
        final int INDEX_UPDATE = 4;
        final CommandType cmdImport;
        final List<ParameterType> listParameters;
        String strResponseValue;
        final ResponseMessageInterface responseMessage;
        final List<String> errors;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Initialise
        errors = new ArrayList<String>(10);
        dao.clearEventLogFragment();

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdImport = (CommandType)commandmessage.getCommandType().copy();

        // We expect five parameters: the url, the pathname, the capture interval, the capture period and the update flag
        listParameters = cmdImport.getParameterList();

        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getController() != null)
            && (dao.getHostInstrument().getInstrument().getController().getVirtualAddress() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_URL) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_URL).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_PATHNAME) != null)
            && (SchemaDataType.PATH_NAME.equals(listParameters.get(INDEX_PATHNAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_INTERVAL) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_INTERVAL).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_PERIOD) != null)
            && (SchemaDataType.DECIMAL_INTEGER.equals(listParameters.get(INDEX_PERIOD).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_UPDATE) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_UPDATE).getInputDataType().getDataTypeName())))
            {
            try
                {
                final String strURL;
                final String strPathname;
                final String strCaptureInterval;
                final String strCapturePeriod;
                final boolean boolRealtimeUpdate;
                final int intCaptureIntervalSec;
                int intCapturePeriodSec;
                final HttpClient client;
                final HttpMethod method;

                strURL = listParameters.get(INDEX_URL).getValue();
                strPathname = listParameters.get(INDEX_PATHNAME).getValue();
                strCaptureInterval = listParameters.get(INDEX_INTERVAL).getValue();
                strCapturePeriod = listParameters.get(INDEX_PERIOD).getValue();
                boolRealtimeUpdate = Boolean.parseBoolean(listParameters.get(INDEX_UPDATE).getValue());

                intCaptureIntervalSec = Integer.parseInt(strCaptureInterval);
                intCapturePeriodSec = Integer.parseInt(strCapturePeriod);

                // Check for silly parameter settings
                // CapturePeriod = 0 means run continuously
                if ((strURL != null)
                    && (!EMPTY_STRING.equals(strURL.trim()))
                    && (intCaptureIntervalSec > 0)
                    && (intCapturePeriodSec >= 0))
                    {
                    final String strPeriod;
                    final int intCaptureCountMax;
                    final ResponseMessageStatusList listStatusInCaptureLoop;

                    boolean boolSuccess;

                    if (intCapturePeriodSec == 0)
                        {
                        strPeriod = CaptureCommandHelper.MSG_PERIOD_CONTINUOUS;
                        }
                    else
                        {
                        strPeriod = Integer.toString(intCapturePeriodSec);
                        }

                    // We have all the Parameters, now prepare the HTTP client
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

                    // Log everything we have so far
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_IMAGE
                                                           + METADATA_ACTION_IMPORT
                                                           + METADATA_URL_REQUEST + strURL + TERMINATOR_SPACE
                                                           + METADATA_PATHNAME + strPathname + TERMINATOR_SPACE
                                                           + METADATA_INTERVAL + intCaptureIntervalSec + TERMINATOR_SPACE
                                                           + METADATA_PERIOD + strPeriod + TERMINATOR,
                                                       dao.getLocalHostname(),
                                                       dao.getObservatoryClock());

                    // Correct the CapturePeriod for continuous operation
                    if (intCapturePeriodSec == 0)
                        {
                        // Allow about 10 days of operation! (Could be Integer.MAX_VALUE?)
                        intCapturePeriodSec = 1000000;
                        }

                    intCaptureCountMax = intCapturePeriodSec / intCaptureIntervalSec;
                    boolSuccess = true;

                    // Adjust the Timeout for the number of commands expected
                    // TODO REVIEW TimeoutHelper.restartDAOTimeoutTimer(dao, intCounter, 0, 0);

                    listStatusInCaptureLoop = ResponseMessageStatus.createResponseMessageStatusList();

                    for (int intCaptureIndex = 0;
                         ((intCaptureIndex < intCaptureCountMax)
                              && (boolSuccess)
                              && Utilities.executeWorkerCanProceed(dao));
                         intCaptureIndex++)
                        {
                        final long longTimeStart;
                        final long longTimeFinish;
                        final long longTimeToWait;

                        longTimeStart = dao.getObservatoryClock().getSystemTimeMillis();
                        listStatusInCaptureLoop.clear();

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
                                final BufferedImage image;

                                image = ImageIO.read(inputStream);

                                // If we get here, it must have succeeded... (otherwise IOException, ImageFormatException)
                                // Save the image as a timestamped PNG file every time
                                boolSuccess = DataExporter.exportImage(image,
                                                                       strPathname + "/GOES",
                                                                       true,
                                                                       FileUtilities.png,
                                                                       dao.getEventLogFragment(),
                                                                       dao.getObservatoryClock());

                                // Pass the Image to the DAO, and then to the Instrument and InstrumentPanel
                                if (boolRealtimeUpdate)
                                    {
                                    dao.setImageData(image);
                                    dao.setUnsavedData(true);

                                    REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                                    InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
                                    }

                                inputStream.close();
                                listStatusInCaptureLoop.add(ResponseMessageStatus.SUCCESS);
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

                        // Take account of the time elapsed so far...
                        longTimeFinish = dao.getObservatoryClock().getSystemTimeMillis();
                        longTimeToWait = (intCaptureIntervalSec * ChronosHelper.SECOND_MILLISECONDS)
                                         - (longTimeFinish - longTimeStart);

                        if (longTimeToWait > 0)
                            {
                            // Wait for the required time, checking to see if we have been stopped by the User
                            Utilities.safeSleepPollExecuteWorker(longTimeToWait, dao);
                            }
                        else
                            {
                            LOGGER.error(SOURCE + " Invalid capture interval [time_to_wait=" + longTimeToWait + " msec]");
                            }
                        }

                    // Always release the HttpClient connection
                    method.releaseConnection();

                    //-----------------------------------------------------------------------------
                    // Capture the final Status and ResponseValue (these may show a failure)

                    dao.getResponseMessageStatusList().addAll(listStatusInCaptureLoop);

                    if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
                        {
                        strResponseValue = ResponseMessageStatus.SUCCESS.getResponseValue();
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logErrors(dao.getEventLogFragment(),
                                                            EventStatus.FATAL,
                                                            errors,
                                                            SOURCE,
                                                            dao.getObservatoryClock());
                        strResponseValue = ResponseMessageStatus.PREMATURE_TERMINATION.getResponseValue();
                        }
                    }
                else
                    {
                    throw new IllegalArgumentException(EXCEPTION_PARAMETER_INVALID);
                    }
                }

            catch (NumberFormatException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.FATAL,
                                                   METADATA_EXCEPTION
                                                       + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + exception.getMessage()
                                                       + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_PARAMETER.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            catch (Exception exception)
                {
                exception.printStackTrace();
                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.WARNING,
                                                   METADATA_EXCEPTION
                                                   + dao.getInstrumentName() + " Generic Exception [exception=" + exception.getMessage() + "]"
                                                   + TERMINATOR,
                                                   dao.getLocalHostname(),
                                                   dao.getObservatoryClock());
                strResponseValue = ResponseMessageStatus.INVALID_MESSAGE.getResponseValue();
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_MESSAGE);
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

        ObservatoryInstrumentHelper.runGarbageCollector();
        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdImport,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
