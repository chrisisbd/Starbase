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

import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.misc.Utilities;
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
 * StaribusTxStream.
 */

public class StaribusTxStream extends AbstractTxStream
                              implements PortTxStreamInterface
    {
    // StaribusTxStream Stream Resources
    private String strPortOwner;
    private String strPortName;
    private int intBaudrate;
    private int intDatabits;
    private int intStopbits;
    private int intParity;
    private String strFlowControl;

    // The Staribus RS232/485 port
    private SerialPort serialPort;


    /***********************************************************************************************
     * Construct a StaribusTxStream.
     *
     * @param resourcekey
     */

    public StaribusTxStream(final String resourcekey)
        {
        super(resourcekey);

        strPortOwner = EMPTY_STRING;
        strPortName = PORT_COM1;
        intBaudrate = StreamUtilities.DEFAULT_BAUDRATE;
        intDatabits = StreamUtilities.DEFAULT_DATABITS;
        intStopbits = StreamUtilities.DEFAULT_STOPBITS;
        intParity = StreamUtilities.DEFAULT_PARITY;
        strFlowControl = StreamUtilities.FLOWCONTROL_NONE;

        this.serialPort = null;
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
     * Initialise the Stream.
     *
     * @return boolean
     *
     * @throws IOException
     */

    public boolean initialise() throws IOException
        {
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusTxStream.initialise()");

        // Close the serial port regardless of whether it is a Staribus Port
        if (getHostPort() != null)
            {
            if (getUnderlyingTxStream() != null)
                {
                getUnderlyingTxStream().flush();
                getUnderlyingTxStream().close();
                }

            if (getSerialPort() != null)
                {
                // This will automatically remove any SerialPortEventListener
                getSerialPort().close();
                }

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            }

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
        final String SOURCE = "StaribusTxStream.open() ";

        LOGGER.debugStaribusEvent(isDebugMode(), SOURCE);

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

                    if (isDebugMode())
                        {
                        try
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "low_latency=[" + getSerialPort().getLowLatency() +"]");
                            }

                        catch (UnsatisfiedLinkError exception)
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "Port does not support Low Latency config");
                            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
                            }

                        catch (UnsupportedCommOperationException exception)
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "Port does not support Low Latency config");
                            }

                        try
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "end_of_input_char=[" + getSerialPort().getEndOfInputChar() +"]");
                            }

                        catch (UnsatisfiedLinkError exception)
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "Port does not support End of Input Character config");
                            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
                            }

                        catch (UnsupportedCommOperationException exception)
                            {
                            LOGGER.debugStaribusEvent(isDebugMode(),
                                                      SOURCE + "Port does not support End of Input Character config");
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "LoopbackStream is not a SerialPortEventListener");
                    }
                }
            }

        catch (TooManyListenersException exception)
            {
            throw new IOException(SOURCE, exception);
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
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusTxStream.close()");

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

                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StaribusTxStream.close() PORT CLOSED!");
                }

            // Mark this Stream as Closed
            this.boolStreamOpen = false;
            }
        }


    /***********************************************************************************************
     * Closes this stream regardless of its type, and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */

    public void forceClose() throws IOException
        {
        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusTxStream.forceClose()");

        if (getUnderlyingTxStream() != null)
            {
            getUnderlyingTxStream().flush();
            getUnderlyingTxStream().close();
            }

        if (getSerialPort() != null)
            {
            // This will automatically remove the SerialPortEventListener
            getSerialPort().close();

            LOGGER.debugStaribusEvent(isDebugMode(),
                                      "StaribusTxStream.forceClose() PORT CLOSED!");
            }

        // Mark this Stream as Closed
        this.boolStreamOpen = false;
        }


    /***********************************************************************************************
     * Reset the Stream.
     */

    public void reset()
        {
        try
            {
            if (getUnderlyingTxStream() != null)
                {
                getUnderlyingTxStream().flush();
                }

            if (getSerialPort() != null)
                {
                LOGGER.debugStaribusEvent(isDebugMode(),
                                          "StaribusTxStream.reset() --> notifyOnDataAvailable(false)");

                // Stop all incoming data until the next write()
                getSerialPort().notifyOnDataAvailable(false);
                }
            }

        catch (IOException exception)
            {
            LOGGER.error("Error in StaribusTxStream.reset() [exception=" + exception.getMessage() + "]");
            }
        }


    /***********************************************************************************************
     * Flushes this stream by writing any buffered output to the underlying stream.
     *
     * @throws IOException If an I/O error occurs
     */

    public void flush() throws IOException
        {
        // ToDo write(dfgdfg, command);
        }


    /***********************************************************************************************
     * Write a CommandMessage to the Tx stream.
     *
     * @param instrumentsdoc
     * @param commandmessage
     *
     * @throws IOException
     */

    public void write(final InstrumentsDocument instrumentsdoc,
                      final CommandMessageInterface commandmessage) throws IOException
        {
        final String SOURCE = "StaribusTxStream.write() ";

        // Send the message to the TxStream, but only if the Tx checksum is valid
        if ((StreamUtilities.isStaribusTxChecksumValid(commandmessage))
            && (getSerialPort() != null)
            && (getSerialPort().getOutputStream() != null)
            && (isStreamOpen()))
            {
            LOGGER.debugStaribusEvent(isDebugMode(),
                                      SOURCE + "[" + Utilities.byteArrayToExpandedAscii(commandmessage.getByteArray()) +"]");

            getSerialPort().getOutputStream().write(commandmessage.getByteArray());

            LOGGER.debugStaribusEvent(isDebugMode(),
                                      SOURCE + "--> notifyOnDataAvailable(true)");
            getSerialPort().notifyOnDataAvailable(true);
            }
        else
            {
            LOGGER.error(SOURCE + "ERROR Unable to transmit");
            }
        }


    /***********************************************************************************************
     * Get the SerialPort underlying this TxStream.
     *
     * @return SerialPort
     */

    protected SerialPort getSerialPort()
        {
        return (this.serialPort);
        }


    /***********************************************************************************************
     * Read all the Resources required by the Stream.
     */

    public void readResources()
        {
//        boolEnableDebug = REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG);

        LOGGER.debugStaribusEvent(isDebugMode(),
                                  "StaribusTxStream.readResources() [ResourceKey=" + getResourceKey() + "]");

        // Serial Port
        strPortName = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_NAME);
        strPortOwner = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_OWNER);
        intBaudrate = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_BAUDRATE);
        intDatabits = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_DATA_BITS);
        intStopbits = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_STOP_BITS);
        intParity = REGISTRY.getIntegerProperty(getResourceKey() + KEY_PORT_PARITY);
        strFlowControl = REGISTRY.getStringProperty(getResourceKey() + KEY_PORT_FLOW_CONTROL);

        // Reload the Stream Configuration every time the Resources are read from the Registry
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
