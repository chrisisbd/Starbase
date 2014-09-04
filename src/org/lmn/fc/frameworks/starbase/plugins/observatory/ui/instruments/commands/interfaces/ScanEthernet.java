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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.interfaces;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.UDPClient;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner.NetworkScannerHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.io.IOException;
import java.net.*;


/***************************************************************************************************
 * ScanEthernet.
 */

public final class ScanEthernet implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkSingletons,
                                           ObservatoryConstants
    {
    /***********************************************************************************************
     * doScanEthernet().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doScanEthernet(final ObservatoryInstrumentDAOInterface dao,
                                                          final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ScanEthernet.doScanEthernet() ";
        final CommandType cmdNetworkInterfaces;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        long longStartIP;
        UDPClient clientUDP;

        try
            {
            longStartIP = NetworkScannerHelper.ipV4ToLong("192.168.1.60");
            }

        catch (UnknownHostException e)
            {
            longStartIP = 0L;
            }

        // Check that we are still running,
        // to save time if the User has stopped the Instrument

        for (int intIPIndex = 0;
             ((intIPIndex <= 10)
              && (InstrumentState.isDoingSomething(dao.getHostInstrument())));
             intIPIndex++)
            {
            clientUDP = null;

            // ToDo Remember to skip our own address

            try
                {
                final long longIP;
                final String strIPAddress;
                final InetAddress inetAddress;
                final byte[] arrayResponse;

                longIP = longStartIP + intIPIndex;
                strIPAddress = NetworkScannerHelper.longToIPv4(longIP);
                inetAddress = InetAddress.getByName(strIPAddress);
                clientUDP = new UDPClient(inetAddress, 32001, DEFAULT_STARINET_UDP_PORT, 2000);

                LOGGER.logTimedEvent("Trying    " + strIPAddress + ":" + DEFAULT_STARINET_UDP_PORT);
                clientUDP.connect();
                clientUDP.send("?".getBytes());

                // This method blocks until a UDP Datagram is received
                // This Command is being executed on its own SwingWorker, so this doesn't matter...
                arrayResponse = clientUDP.receive();

                LOGGER.logTimedEvent("Host Name " + inetAddress.getHostName());

                if ((arrayResponse != null)
                    && (arrayResponse.length > 0))
                    {
                    LOGGER.logTimedEvent("Response  " + Utilities.byteArrayToExpandedAscii(arrayResponse));
                    }

                LOGGER.logTimedEvent("\n");

                clientUDP.close();
                }

            catch (SocketTimeoutException e)
                {
                // Ignore and move on
                }

            catch (PortUnreachableException e)
                {
                // Ignore and move on
                }

            catch (IllegalArgumentException e)
                {
                e.printStackTrace();
                }

            catch (UnknownHostException e)
                {
                e.printStackTrace();
                }

            catch (SecurityException e)
                {
                e.printStackTrace();
                }

            catch (SocketException e)
                {
                e.printStackTrace();
                }

            catch (IOException e)
                {
                e.printStackTrace();
                }

            finally
                {
                if (clientUDP != null)
                    {
                    clientUDP.close();
                    }
                }
            }

        // Don't affect the CommandType of the incoming Command
        cmdNetworkInterfaces = (CommandType)commandmessage.getCommandType().copy();

        strResponseValue = RESPONSE_ACK;

        dao.getResponseMessageStatusList().add(ResponseMessageStatus.SUCCESS);

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdNetworkInterfaces,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }
    }
