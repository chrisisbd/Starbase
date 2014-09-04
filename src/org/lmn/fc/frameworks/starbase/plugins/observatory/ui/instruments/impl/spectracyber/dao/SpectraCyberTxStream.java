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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.spectracyber.dao;

import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.AbstractTxStream;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.streams.StreamType;
import org.lmn.fc.model.resources.PropertyPlugin;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;

import java.io.IOException;
import java.util.TooManyListenersException;


/***************************************************************************************************
 * SpectraCyberTxStream.
 */

public final class SpectraCyberTxStream extends AbstractTxStream
                                        implements PortTxStreamInterface
    {
    // The SpectraCyber RS232 port
    private SerialPort serialPort;

    // SpectraCyberTxStream Stream Resources
    private String strPortOwner;
    private String strPortName;
    private int intBaudrate;
    private int intDatabits;
    private int intStopbits;
    private int intParity;
    private String strFlowControl;


    /***********************************************************************************************
     * Construct a SpectraCyberTxStream.
     *
     * @param resourcekey
     */

    public SpectraCyberTxStream(final String resourcekey)
        {
        super(resourcekey);

        this.serialPort = null;

        strPortOwner = EMPTY_STRING;
        strPortName = PORT_COM1;
        intBaudrate = StreamUtilities.DEFAULT_BAUDRATE;
        intDatabits = StreamUtilities.DEFAULT_DATABITS;
        intStopbits = StreamUtilities.DEFAULT_STOPBITS;
        intParity = StreamUtilities.DEFAULT_PARITY;
        strFlowControl = StreamUtilities.FLOWCONTROL_NONE;
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
                               "SpectraCyberTxStream.open()");

        // Ensure that we get the port configuration...
        readResources();

        try
            {
            // First find and open the serial port to be used for the TxStream (and RxStream)
            this.serialPort = StreamUtilities.findAndOpenSerialPort(strPortOwner,
                                                                    strPortName,
                                                                    intBaudrate,
                                                                    intDatabits,
                                                                    intStopbits,
                                                                    intParity,
                                                                    strFlowControl);
            // Set up to receive events, errors etc.
            if (getSerialPort() != null)
                {
                // Everything is working, so reveal the underlying Streams
                // Do them both here, to avoid having to pass a Stream to the RxStream
                setUnderlyingTxStream(getSerialPort().getOutputStream());
                getLoopbackRxStream().setUnderlyingRxStream(getSerialPort().getInputStream());

                // Activate the RxListener!
                // Note that we can only have *one* Listener...
                if (getLoopbackRxStream() instanceof SerialPortEventListener)
                    {
                    getSerialPort().addEventListener((SerialPortEventListener) getLoopbackRxStream());

                    // This will fire serialEvent() when data are waiting
                    getSerialPort().notifyOnDataAvailable(true);
                    }
                else
                    {
                    LOGGER.error("SpectraCyberTxStream.open() LoopbackStream is not a SerialPortEventListener");
                    }
                }
            }

        catch (TooManyListenersException exception)
            {
            throw new IOException("SpectraCyberTxStream.open()", exception);
            }

        // Prepare the RxStream for the first write()
        if ((getHostPort() != null)
            && (getHostPort().getRxStream() != null))
            {
            getHostPort().getRxStream().setCommandContext(null);
            }

        // Mark this Stream as Open, if successful
        this.boolStreamOpen = (getSerialPort() != null);

        return (this.boolStreamOpen);
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
                               "SpectraCyberTxStream.close()");

        // Do not close the serial port or streams if attached to the StaribusPort!
        if ((getHostPort() != null)
            && (!getHostPort().isStaribusPort()))
            {
            if (getUnderlyingTxStream() != null)
                {
                getUnderlyingTxStream().flush();
                getUnderlyingTxStream().close();
                }

            if (getSerialPort() != null)
                {
                // This will automatically remove the SerialPortEventListener
                getSerialPort().close();
                }

            // Tidy up the RxStream
            if ((getHostPort() != null)
                && (getHostPort().getRxStream() != null))
                {
                getHostPort().getRxStream().setCommandContext(null);
                }

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            }
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "SpectraCyberTxStream.reset()");
        }


    /***********************************************************************************************
     * Flushes this stream by writing any buffered output to the underlying stream.
     *
     * @throws IOException If an I/O error occurs
     */

    public void flush() throws IOException
        {
        // TODO write(dfgdfg, command);
        }


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     *
     * @param instrumentsdoc
     * @param commandmessage
     *
     * @throws IOException
     */

    public final void write(final InstrumentsDocument instrumentsdoc,
                            final CommandMessageInterface commandmessage) throws IOException
        {
        // Send the message to the TxStream
        if ((getSerialPort() != null)
            && (getSerialPort().getOutputStream() != null)
            && (isStreamOpen()))
            {
            //LOGGER.debugProtocolEvent("SpectraCyberTxStream.write() sending bytes=[" + Utilities.byteArrayToExpandedAscii(commandmessage.getByteArray()) +"]");
            //LOGGER.debugProtocolEvent("SpectraCyberTxStream.write() sending String=[" + new String(commandmessage.getByteArray()) +"]");

            // Prepare the RxStream for the write()
            if ((getHostPort() != null)
                && (getHostPort().getRxStream() != null))
                {
                getHostPort().getRxStream().setCommandContext(null);
                }

            getSerialPort().getOutputStream().write(commandmessage.getByteArray());

            // Let the Receive Stream know the Instrument, Module and Command context,
            // since this is not available in the received message itself
            // Don't do this if write() failed because of an IOException
            if ((getHostPort() != null)
                && (getHostPort().getRxStream() != null))
                {
                getHostPort().getRxStream().setCommandContext(commandmessage);
                }
            }
        else
            {
            // This should never happen!
            LOGGER.debugCommandEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      "SpectraCyberTxStream.write() ERROR Unable to transmit");
            }
        }


    /***********************************************************************************************
     * Get the SerialPort underlying this TxStream.
     *
     * @return SerialPort
     */

    private SerialPort getSerialPort()
        {
        return (this.serialPort);
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     *
     * KEY_PORT_NAME
     * KEY_PORT_OWNER
     * KEY_PORT_BAUDRATE
     * KEY_PORT_DATA_BITS
     * KEY_PORT_STOP_BITS
     * KEY_PORT_PARITY
     * KEY_PORT_FLOW_CONTROL
     */

    public final void readResources()
        {
        //LOGGER.debugProtocolEvent("SpectraCyberTxStream.readResources() [ResourceKey=" + getResourceKey() + "]");

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
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_NAME,
                                                       strPortName);
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_OWNER,
                                                       strPortOwner);
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_BAUDRATE,
                                                       Integer.toString(intBaudrate));
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_DATA_BITS,
                                                       Integer.toString(intDatabits));
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_STOP_BITS,
                                                       Integer.toString(intStopbits));
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_PARITY,
                                                       Integer.toString(intParity));
            ConfigurationHelper.addItemToConfiguration(getStreamConfiguration(),
                                                       PropertyPlugin.PROPERTY_ICON,
                                                       getResourceKey() + KEY_PORT_FLOW_CONTROL,
                                                       strFlowControl);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }
    }
