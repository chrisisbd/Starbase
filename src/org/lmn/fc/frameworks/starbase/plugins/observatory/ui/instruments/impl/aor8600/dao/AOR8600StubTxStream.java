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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.aor8600.dao;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractTxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/***************************************************************************************************
 * AOR8600StubTxStream.
 */

public final class AOR8600StubTxStream extends AbstractTxStream
                                       implements PortTxStreamInterface
    {
    private final BlockingQueue<CommandMessageInterface> queueCommands;

    // Resources
    private String strPortOwner;
    private String strPortName;
    private int intBaudrate;
    private int intDatabits;
    private int intStopbits;
    private int intParity;
    private String strFlowControl;


    /***********************************************************************************************
     * Construct a AOR8600StubTxStream.
     *
     * @param resourcekey
     */

    public AOR8600StubTxStream(final String resourcekey)
        {
        super(resourcekey);
        this.queueCommands = new ArrayBlockingQueue<CommandMessageInterface>(1000);

        strPortOwner = EMPTY_STRING;
        strPortName = PORT_COM1;
        intBaudrate = DEFAULT_BAUDRATE;
        intDatabits = DEFAULT_DATABITS;
        intStopbits = DEFAULT_STOPBITS;
        intParity = DEFAULT_PARITY;
        strFlowControl = FLOWCONTROL_NONE;
        }


    /***********************************************************************************************
     * Get the Tx StreamType.
     *
     * @return StreamType
     */

    public StreamType getStreamType()
        {
        return (StreamType.SERIAL);
        }


    /***********************************************************************************************
     * Initialise the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean initialise() throws IOException
        {
        return (true);
        }


    /***********************************************************************************************
     * Open the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean open() throws IOException
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubTxStream.open()");

        // Mark this Stream as Open, if successful
        this.boolStreamOpen = true;

        return (true);
        }


    /***********************************************************************************************
     * Closes this stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    public void close() throws IOException
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubTxStream.close()");

        // Mark this Stream as Closed
        this.boolStreamOpen = false;
        getCommands().clear();
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubTxStream.reset()");

        getCommands().clear();
        }


    /***********************************************************************************************
     * Flushes this stream by writing any buffered output to the underlying stream.
     *
     * @throws IOException If an I/O error occurs
     */

    public void flush() throws IOException
        {
        final Iterator<CommandMessageInterface> iterCommands;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubTxStream.flush()");

        iterCommands = getCommands().iterator();

        while (iterCommands.hasNext())
            {
            final CommandMessageInterface command;

            command = iterCommands.next();
            // ToDo write(dfgdfg, command);
            }
        }


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     * This stub must also provide a dummy Response to the loopback RxStream.
     *
     * @param instrumentsdoc
     * @param command
     *
     * @throws IOException
     */

    public final void write(final InstrumentsDocument instrumentsdoc,
                            final CommandMessageInterface command) throws IOException
        {
        final ResponseMessageInterface responseConstructed;
        final ResponseMessageInterface responseParsed;
        final ResponseType responseType;
        final TimeZone timeZone;
        final Locale locale;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubTxStream sending bytes=[" + Utilities.byteArrayToExpandedAscii(command.getByteArray()) +"]");

        // This stub must provide a dummy Response to the RxStream

        // Method 1: Process the CommandMessageInterface
        // See which kind of Response is required for the supplied Command
        responseType = command.getCommandType().getResponse();

        // Was there a Response required?
        if (responseType != null)
            {
            DAOHelper.insertDummyResponseValue(responseType);
            }

        // This Response must tie up with the Command which was just sent
        // Construct a simulated Response, complete with byte array
        // Generate a dummy ResponseMessageStatus of a single bit error, or success
//        responseConstructed = new StaribusResponseMessage(command.getInstrument(),
//                                                          command.getModule(),
//                                                          command.getCommandType(),  // Contains new Value (or not)
//                                                          StaribusResponseMessage.buildResponseResourceKey(command.getInstrument(),
//                                                                                                           command.getModule(),
//                                                                                                           command.getCommandType()),
//                                                          command.getDAO(),
//                                                          DAOHelper.createDummyResponseMessageStatusBitMask()); // Status bits (0 = success)
//        LOGGER.debugTimedEvent("Constructed StaribusResponseMessage bytes=[" + Utilities.byteArrayToSpacedHex(responseConstructed.getByteArray()) + "]");
//
//        // Method 2: Re-parse the byte array of the simulated Response stream
//        // This is now mimicing the behaviour of the real RxStream
//        responseParsed = responseConstructed.parseStaribusBytes(instrumentsdoc,
//                                                        command.getDAO(),
//                                                        responseConstructed.getByteArray());
//        // Put the Response into the loopback...
//        // If the response is null (i.e. parsing failed for this chunk),
//        // then no action is taken by the PortController
//        ((StubStaribusRxStream)getLoopbackRxStream()).putResponseToStream(responseParsed);
        }


    /***********************************************************************************************
     * Get the queue of Commands in the stream.
     *
     * @return BlockingQueue<CommandMessageInterface>
     */

    private BlockingQueue<CommandMessageInterface> getCommands()
        {
        return (this.queueCommands);
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public final void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "AOR8600StubTxStream.readResources() [ResourceKey=" + getResourceKey() + "]");

        // Serial Port
        strPortName = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_NAME);
        strPortOwner = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_OWNER);
        intBaudrate = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_BAUDRATE);
        intDatabits = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_DATA_BITS);
        intStopbits = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_STOP_BITS);
        intParity = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_PARITY);
        strFlowControl = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_FLOW_CONTROL);

        // Reload the Stream Configuration every time the Resources are read
        if (getStreamConfiguration() != null)
            {
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_NAME,
//                                                                      strPortName);
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_OWNER,
//                                                                      strPortOwner);
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_BAUDRATE,
//                                                                      Integer.toString(intBaudrate));
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_DATA_BITS,
//                                                                      Integer.toString(intDatabits));
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_STOP_BITS,
//                                                                      Integer.toString(intStopbits));
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_PARITY,
//                                                                      Integer.toString(intParity));
//            InstrumentUIComponentDecorator.addItemToConfiguration(getConfiguration(),
//                                                                      PropertyPlugin.PROPERTY_ICON,
//                                                                      getResourceKey() + KEY_PORT_FLOW_CONTROL,
//                                                                      strFlowControl);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }
    }
