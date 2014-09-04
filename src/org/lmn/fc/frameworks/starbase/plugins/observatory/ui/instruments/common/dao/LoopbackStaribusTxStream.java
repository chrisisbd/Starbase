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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * LoopbackStaribusTxStream.
 */

public final class LoopbackStaribusTxStream extends StaribusTxStream
                                            implements PortTxStreamInterface
    {
    private CommandMessageInterface commandMessage;
    private int intPreambleSynCount;


    /***********************************************************************************************
     * Construct a LoopbackStaribusTxStream.
     *
     * @param resourcekey
     */

    public LoopbackStaribusTxStream(final String resourcekey)
        {
        super(resourcekey);

        this.commandMessage = null;
        this.intPreambleSynCount = 3;
        }


    /***********************************************************************************************
     * Get the Tx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.STARIBUS);
        }


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     * Intentionally ignores superclass to avoid checksum test of message without SYN preamble.
     *
     * @param instrumentsdoc
     * @param commandmessage
     *
     * @throws IOException
     */

    public final void write(final InstrumentsDocument instrumentsdoc,
                            final CommandMessageInterface commandmessage) throws IOException
        {
        final String SOURCE = "LoopbackStaribusTxStream.write() ";

        // Update all resources, in case the SYN count has been changed
        readResources();

        // Record the last Command sent
        this.commandMessage = commandmessage;

        // Send the message to the TxStream, but only if the Tx checksum is valid
        if ((getCommandMessage() != null)
            && (getCommandMessage().getByteList() != null)
            && (StreamUtilities.isStaribusTxChecksumValid(getCommandMessage()))
            && (getSerialPort() != null)
            && (getSerialPort().getOutputStream() != null)
            && (isStreamOpen()))
            {
            final List<Byte> listBytes;

            listBytes = new ArrayList<Byte>(getCommandMessage().getByteList().size() + intPreambleSynCount);

            LOGGER.debugStaribusEvent(isDebugMode(),
                                      SOURCE + "--> notifyOnDataAvailable(false)");
            getSerialPort().notifyOnDataAvailable(false);

            // Add some synchronisation characters to simulate the way the Responder works
            for (int intSynCount = 0;
                intSynCount < intPreambleSynCount;
                intSynCount++)
                {
                listBytes.add(STARIBUS_SYNCHRONISE);
                }

            // Now add the CommandMessage, which starts at STX
            listBytes.addAll(commandmessage.getByteList());

            if (!listBytes.isEmpty())
                {
                final byte[] arrayBytes;

                // This method will handle the synchronized wrapping needed by Collections.synchronizedList()
                arrayBytes = Utilities.byteListToArray(listBytes);

                LOGGER.debugStaribusEvent(isDebugMode(),
                                          SOURCE + "[" + Utilities.byteArrayToExpandedAscii(arrayBytes) +"]");
                // Write the modified message in a single operation
                getSerialPort().getOutputStream().write(arrayBytes);
                }

            LOGGER.debugStaribusEvent(isDebugMode(),
                                      SOURCE + "--> notifyOnDataAvailable(true)");
            getSerialPort().notifyOnDataAvailable(true);
            }
        else
            {
            LOGGER.error("LoopbackStaribusTxStream.write() ERROR Unable to transmit");
            }
        }


    /***********************************************************************************************
     * Get the last CommandMessage sent by write().
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface getCommandMessage()
        {
        return (this.commandMessage);
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public final void readResources()
        {
        super.readResources();

        this.intPreambleSynCount = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_LOOPBACK_PREAMBLE_SYN_COUNT);

        // Reload the Stream Configuration every time the Resources are read from the Registry
        if (getStreamConfiguration() != null)
            {
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_LOOPBACK_PREAMBLE_SYN_COUNT,
                                                       Integer.toString(intPreambleSynCount));
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }
    }
