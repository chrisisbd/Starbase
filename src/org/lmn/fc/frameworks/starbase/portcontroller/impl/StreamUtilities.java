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

package org.lmn.fc.frameworks.starbase.portcontroller.impl;

import gnu.io.*;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.misc.CRC16;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortRxStreamInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.PortTxStreamInterface;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;


/***************************************************************************************************
 * StreamUtilities.
 */

public final class StreamUtilities implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ResourceKeys
    {
    public static final String FLOWCONTROL_NONE = "None";
    public static final String FLOWCONTROL_XON_XOFF = "XonXoff";
    public static final String FLOWCONTROL_RTS_CTS = "RtsCts";

    public static final int SLEEP_WAIT_MILLIS = 1000;
    public static final int PORT_OPEN_TIMEOUT_MILLIS = 2000;
    public static final int PORT_OPEN_DELAY_MILLIS = 2000;
    public static final int RECEIVE_TIMEOUT_MILLIS = 1000;
    public static final int RECEIVE_THRESHOLD = 100;
    public static final int DEFAULT_BAUDRATE = 9600;
    public static final int DEFAULT_DATABITS = 8;
    public static final int DEFAULT_STOPBITS = 1;
    public static final int DEFAULT_PARITY = 0;
    public static final int BUFFER_SIZE = 8000;

    public static final int LENGTH_CRC = 4;
    public static final int LENGTH_TAIL = LENGTH_CRC + 3;       // CRCChecksum + EOT + CR + LF


    /***********************************************************************************************
     * Instantiate the specified PortTxStreamInterface, initialising from the ResourceKey if necessary.
     *
     * @param classname
     * @param resourcekey
     *
     * @return PortTxStreamInterface
     */

    public static PortTxStreamInterface instantiateTxStream(final String classname,
                                                            final String resourcekey)
        {
        return ((PortTxStreamInterface)instantiateStream(classname,
                                                         resourcekey,
                                                         PortTxStreamInterface.class));
        }


    /***********************************************************************************************
     * Instantiate the specified InputStream, initialising from the ResourceKey if necessary.
     *
     * @param classname
     * @param resourcekey
     * @return PortRxStreamInterface
     */

    public static PortRxStreamInterface instantiateRxStream(final String classname,
                                                            final String resourcekey)
        {
        return ((PortRxStreamInterface)instantiateStream(classname,
                                                         resourcekey,
                                                         PortRxStreamInterface.class));
        }


    /***********************************************************************************************
     * Instantiate the specified Stream, initialising from the ResourceKey if necessary.
     * Uses the Discovery Pattern.
     *
     * @param classname
     * @param resourcekey
     * @param streaminterface
     * @return Object
     */

    public static Object instantiateStream(final String classname,
                                            final String resourcekey,
                                            final Class streaminterface)
        {
        final String SOURCE = "DaoPort.instantiateStream() ";
        Object objReturn;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[classname=" + classname + "]");

        // Some basic error traps... (allow an empty ResourceKey)
        if ((classname == null)
            || (EMPTY_STRING.equals(classname))
            || (resourcekey == null)
            || (streaminterface == null))
            {
            return (null);
            }

        objReturn = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            boolean boolInstantiated;

            boolInstantiated = false;

            // Does the target implement the correct interface?
            classObject = Class.forName(classname);
            interfaces = classObject.getInterfaces();

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0;
                         ((i < interfaces.length) && (!boolInstantiated));
                         i++)
                        {
                        if (streaminterface.getName().equals(interfaces[i].getName()))
                            {
                            // We have found the interface
                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   SOURCE + "[" + classname + " implements " + streaminterface.getName() + "]");

                            final Constructor[] constructors;

                            // Get hold of the Constructors for the requested class name
                            constructors = classObject.getDeclaredConstructors();

                            if ((constructors != null)
                                && (constructors.length >= 1))
                                {
                                // Step through all constructors (there should be only one),
                                // and find one which takes one String
                                for (int j = 0;
                                     ((!boolInstantiated) && (j < constructors.length));
                                     j++)
                                    {
                                    final Class classParameters[];

                                    classParameters = constructors[j].getParameterTypes();

                                    // Only check the single-parameter constructors, class in [j]
                                    if (classParameters.length == 1)
                                        {
                                        // Check that the parameter type is String
                                        if (classParameters[0].getName().equals(String.class.getName()))
                                            {
                                            // It is safe to try to instantiate
                                            // Constructor must take only one String
                                            final Object[] objArguments;

                                            objArguments = new Object[1];
                                            objArguments[0] = resourcekey;

                                            // If we get this far, we have a valid OutputStream
                                            objReturn = constructors[j].newInstance(objArguments);
                                            boolInstantiated = true;
                                            }
                                        else
                                            {
                                            // Constructor has incorrect parameter type
                                            LOGGER.error(SOURCE + "Incorrect parameter type [parameter=" + classParameters[0].getName() + "]");
                                            }
                                        }
                                    else
                                        {
                                        // Constructor has incorrect number of parameters
                                        LOGGER.error(SOURCE + "Constructor has an incorrect number of parameters");
                                        }
                                    }
                                }
                            else
                                {
                                // Constructor is null or empty
                                LOGGER.error(SOURCE + "Constructor is null or empty");
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "No interfaces found");
                }
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(SOURCE + "InstantiationException [classname=" + classname + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(SOURCE + "IllegalAccessException [classname=" + classname + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + "IllegalArgumentException [classname=" + classname + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(SOURCE + "InvocationTargetException [classname=" + classname + "]");
            }

        catch (ClassNotFoundException exception)
            {
            LOGGER.error(SOURCE + "ClassNotFoundException [classname=" + classname + "]");
            }

        catch (NullPointerException exception)
            {
            LOGGER.error(SOURCE + "NullPointerException [classname=" + classname + "]");
            }

        return (objReturn);
        }


    /***********************************************************************************************
     * Try to find, open and close the specified port, to check that it works ok.
     * Return the identifier of the requested CommPort.
     *
     * @param owner
     * @param name
     * @param baudrate
     * @param databits
     * @param stopbits
     * @param parity
     * @param flowcontrol
     *
     * @return CommPortIdentifier
     */

//    public static CommPortIdentifier findSerialPort(final String owner,
//                                                    final String name,
//                                                    final int baudrate,
//                                                    final int databits,
//                                                    final int stopbits,
//                                                    final int parity,
//                                                    final String flowcontrol)
//        {
//        CommPortIdentifier commPortRequested;
//
//        commPortRequested = null;
//
//        if ((name == null)
//            || (EMPTY_STRING.equals(name.trim())))
//            {
//            LOGGER.error("StreamUtilities.findSerialPort() " + EXCEPTION_PARAMETER_INVALID);
//
//            return (commPortRequested);
//            }
//
//        try
//            {
//            final Enumeration portList;
//            boolean boolFoundPort;
//
//            portList = CommPortIdentifier.getPortIdentifiers();
//            boolFoundPort = false;
//
//            // Scan all ports found for the one with the required name
//            LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() Scanning ports for [port=" + name + "]");
//
//            while ((portList != null)
//                && (portList.hasMoreElements()))
//                {
//                final CommPortIdentifier commPortId;
//
//                commPortId = (CommPortIdentifier) portList.nextElement();
//
//                if (commPortId.isCurrentlyOwned())
//                    {
//                    LOGGER.debugTimedEvent(INDENT + "[port=" + commPortId.getName()
//                                    + "] [owner=" + commPortId.getCurrentOwner() + "]");
//                    }
//                else
//                    {
//                    LOGGER.debugTimedEvent(INDENT + "[port=" + commPortId.getName()
//                                    + "] [not owned]");
//                    }
//
//                // Record the Port we want, but keep going so we can see them all...
//                if ((!boolFoundPort)
//                    && (name.equals(commPortId.getName()))
//                    && (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL))
//                    {
//                    commPortRequested = commPortId;
//                    boolFoundPort = true;
//                    }
//                }
//
//            // Did we find a Port?
//            if (boolFoundPort)
//                {
//                LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() found [port=" + commPortRequested.getName() + "]");
//
//                // We found the Port, but is it already in use?
//                if (commPortRequested.isCurrentlyOwned())
//                    {
//                    LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() "
//                                    + " Port already in use [portowner="
//                                    + commPortRequested.getCurrentOwner()
//                                    + "]");
//                    commPortRequested = null;
//                    }
//                else
//                    {
//                    final SerialPort serialPort;
//
//                    // Try to open the SerialPort
//                    LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() Open port");
//                    serialPort = openSerialPort(commPortRequested,
//                                                owner,
//                                                name,
//                                                baudrate,
//                                                databits,
//                                                stopbits,
//                                                parity,
//                                                flowcontrol);
//
//                    if (serialPort != null)
//                        {
//                        // If we get here, then it all worked Ok, and we can close the SerialPort and stream
//                        LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() Close port");
//                        serialPort.close();
//                        }
//                    else
//                        {
//                        LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() "
//                                        + " Unable to open [port=" + name + "]");
//                        commPortRequested = null;
//                        }
//                    }
//                }
//            else
//                {
//                // We did not find the required serialPort
//                LOGGER.debugTimedEvent("StreamUtilities.findSerialPort() "
//                                + " Failed to find [port=" + name + "]");
//                commPortRequested = null;
//                }
//            }
//
//        catch (UnsatisfiedLinkError exception)
//            {
//            exception.printStackTrace();
//            LOGGER.error("StreamUtilities.findSerialPort() "
//                            + METADATA_EXCEPTION
//                            + exception.getMessage()
//                            + TERMINATOR);
//            commPortRequested = null;
//            }
//
//        catch (IOException exception)
//            {
//            exception.printStackTrace();
//            LOGGER.error("StreamUtilities.findSerialPort() "
//                            + METADATA_EXCEPTION
//                            + exception.getMessage()
//                            + TERMINATOR);
//            commPortRequested = null;
//            }
//
//        // If the SerialPort functioned correctly, then save the id for later use
//        return (commPortRequested);
//        }


    /***********************************************************************************************
     * Try to find and open the specified port.
     *
     * @param owner
     * @param name
     * @param baudrate
     * @param databits
     * @param stopbits
     * @param parity
     * @param flowcontrol
     *
     * @return SerialPort
     */

    public static SerialPort findAndOpenSerialPort(final String owner,
                                                   final String name,
                                                   final int baudrate,
                                                   final int databits,
                                                   final int stopbits,
                                                   final int parity,
                                                   final String flowcontrol)
        {
        final String SOURCE = "StreamUtilities.findAndOpenSerialPort() ";
        CommPortIdentifier commPortRequested;
        SerialPort serialPortRequested;

        commPortRequested = null;
        serialPortRequested = null;

        if ((name == null)
            || (EMPTY_STRING.equals(name.trim())))
            {
            LOGGER.error(SOURCE + EXCEPTION_PARAMETER_INVALID);

            return (serialPortRequested);
            }

        try
            {
            final Enumeration enumCommPortIDs;
            boolean boolFoundPort;

            enumCommPortIDs = CommPortIdentifier.getPortIdentifiers();
            boolFoundPort = false;

            // Scan all ports found for the one with the required name
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Scanning ports for [port=" + name + "]");

            while ((enumCommPortIDs != null)
                && (enumCommPortIDs.hasMoreElements()))
                {
                final CommPortIdentifier commPortId;

                commPortId = (CommPortIdentifier) enumCommPortIDs.nextElement();

                if (commPortId.isCurrentlyOwned())
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           INDENT + "[port=" + commPortId.getName()
                                                + "] [owner=" + commPortId.getCurrentOwner() + "]");
                    }
                else
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           INDENT + "[port=" + commPortId.getName()
                                                + "] [not owned]");
                    }

                // Record the Port we want, but keep going so we can see them all...
                if ((!boolFoundPort)
                    && (name.equals(commPortId.getName()))
                    && (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL))
                    {
                    commPortRequested = commPortId;
                    boolFoundPort = true;
                    }
                }

            // Did we find a Port?
            if (boolFoundPort)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "found [port=" + commPortRequested.getName() + "]");

                // Supposedly getting a new ID helps...
                // See http://mailman.qbang.org/pipermail/rxtx/2007-June/2362845.html
                commPortRequested = CommPortIdentifier.getPortIdentifier(name);

                // We found the Port, but is it already in use?
                if (commPortRequested.isCurrentlyOwned())
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           SOURCE
                                            + " Port already in use [portowner="
                                            + commPortRequested.getCurrentOwner()
                                            + "]");
                    serialPortRequested = null;
                    }
                else
                    {
                    final SerialPort serialPort;

                    // ...as does waiting a while before opening the Port
//                    Thread.sleep(PORT_OPEN_DELAY_MILLIS);
                    Utilities.safeSleep(PORT_OPEN_DELAY_MILLIS);

                    // Try to open the SerialPort
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           SOURCE + "Open port");
                    serialPort = openSerialPort(commPortRequested,
                                                owner,
                                                name,
                                                baudrate,
                                                databits,
                                                stopbits,
                                                parity,
                                                flowcontrol);
                    if (serialPort != null)
                        {
                        serialPortRequested = serialPort;
                        }
                    else
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               SOURCE + " Unable to open [port=" + name + "]");
                        serialPortRequested = null;
                        }
                    }
                }
            else
                {
                // We did not find the required serialPort
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + " Failed to find [port=" + name + "]");
                serialPortRequested = null;
                }
            }

        catch (UnsatisfiedLinkError exception)
            {
            exception.printStackTrace();
            LOGGER.error(SOURCE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR);
            serialPortRequested = null;
            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
            }

        catch (IOException exception)
            {
            exception.printStackTrace();
            LOGGER.error(SOURCE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR);
            serialPortRequested = null;
            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
            }

        catch (NoSuchPortException exception)
            {
            exception.printStackTrace();
            LOGGER.error(SOURCE
                            + METADATA_EXCEPTION
                            + exception.getMessage()
                            + TERMINATOR);
            serialPortRequested = null;
            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
            }

        return (serialPortRequested);
        }


    /***********************************************************************************************
     * Open the specified serial CommPort if possible.
     *
     * @param requestedport
     * @param owner
     * @param name
     * @param baudrate
     * @param databits
     * @param stopbits
     * @param parity
     * @param flowcontrol
     *
     * @return SerialPort
     */

    private static SerialPort openSerialPort(final CommPortIdentifier requestedport,
                                             final String owner,
                                             final String name,
                                             final int baudrate,
                                             final int databits,
                                             final int stopbits,
                                             final int parity,
                                             final String flowcontrol) throws IOException
        {
        final String SOURCE = "StreamUtilities.openSerialPort() ";
        SerialPort port;

        port = null;

        if ((requestedport == null)
            || (requestedport.getPortType() != CommPortIdentifier.PORT_SERIAL))
            {
            LOGGER.error(SOURCE + EXCEPTION_PARAMETER_INVALID);

            return (port);
            }

        try
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "[requested.owner=" + owner + "]");

            if (requestedport.isCurrentlyOwned())
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "ERROR! Port is owned by [" + requestedport.getCurrentOwner() + "]");
                }
            else
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       SOURCE + "Port NOT owned, so going to try to open it for [" + owner + "]");
                }

            // open() may throw PortInUseException
            port = (SerialPort) requestedport.open(owner,
                                                   PORT_OPEN_TIMEOUT_MILLIS);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Opened port for owner, now setting parameters.....");

            // All error exits after here must close the serial port!
            // These may throw UnsupportedCommOperationException

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "[rate=" + baudrate + "] [data=" + databits + "] [stop=" + stopbits + "] [parity=" + parity + "]");
            port.setSerialPortParams(baudrate,
                                     databits,
                                     stopbits,
                                     parity);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "[rxtimeout=" + RECEIVE_TIMEOUT_MILLIS + "]");
            port.enableReceiveTimeout(RECEIVE_TIMEOUT_MILLIS);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "[rxthreshold=" + RECEIVE_THRESHOLD + "]");
            port.enableReceiveThreshold(RECEIVE_THRESHOLD);

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "[inputbuffer=" + BUFFER_SIZE + "]");
            port.setInputBufferSize(BUFFER_SIZE);

//            > *timeout threshold       Behavior
//            > *------------------------------------------------------------------------
//            > *0       0       blocks until 1 byte is available timeout > 0,
//            > *                threshold = 0, blocks until timeout occurs, returns -1
//            > *                on timeout
//            > *>0      >0      blocks until timeout, returns - 1 on timeout, magnitude
//            > *                of threshold doesn't play a role.
//            > *0       >0      Blocks until 1 byte, magnitude of  threshold doesn't
//            > *                play a role
//            >

            // Now set the *Receive* flow control
            // ToDo Review *Transmit* Flow Control
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "[flowcontrol=" + flowcontrol + "]");
            if (FrameworkConstants.FLOWCONTROL_NONE.equals(flowcontrol))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                }
            else if (FrameworkConstants.FLOWCONTROL_XON_XOFF.equals(flowcontrol))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
                }
            else if (FrameworkConstants.FLOWCONTROL_RTS_CTS.equals(flowcontrol))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                }
            else
                {
                // Set no flow control if no others found
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                }
            }

        catch (PortInUseException exception)
            {
            // WARNING!! I don't think this does anything at all!
            if (port != null)
                {
                port.close();
                port = null;
                }

            throw new IOException(SOURCE + "[PortInUseException="
                                    + exception.getMessage()
                                    + "]",
                                  exception);
            }

        catch (UnsupportedCommOperationException exception)
            {
            if (port != null)
                {
                port.close();
                port = null;
                }

            exception.printStackTrace();
            throw new IOException(SOURCE + "[UnsupportedCommOperationException="
                                    + exception.getMessage()
                                    + "]",
                                  exception);
            }

        if (port != null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "Port configured and opened correctly [portname="
                                    + port.getName() + "]");
            }

        return (port);
        }


    /***********************************************************************************************
     * Check to see if the Tx checksum is correct.
     *
     * @param commandmessage
     *
     * @return boolean
     */

    public static boolean isStaribusTxChecksumValid(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StreamUtilities.isStaribusTxChecksumValid() ";
        final byte[] bytesInMessage;
        final byte[] bytesInCRCSegment;
        final byte[] bytesToCrc;
        final short shortCalculatedCRC;
        short shortMessageCRC;
        int intIndex;
        final boolean boolValid;

        // Check the validity of the CRC checksum
        //    CrcChecksum     char char char char  in Hex
        //    Terminator      EOT CR LF

        bytesInMessage = commandmessage.getByteArray();
        bytesInCRCSegment = new byte[LENGTH_CRC];

        // Get hold of only the CRC bytes
        // <stx>2B0001 060A <eot><cr><lf>
        System.arraycopy(bytesInMessage, (bytesInMessage.length - LENGTH_TAIL),
                         bytesInCRCSegment, 0,
                         LENGTH_CRC);

        //LOGGER.debugTimedEvent("StreamUtilities.isStaribusTxChecksumValid() CRC segment=[" + Utilities.byteArrayToExpandedAscii(bytesInCRCSegment) +"]");

        // We expect four characters of CRC checksum
        // The result must be in the range 0000-FFFF
        intIndex = 0;
        shortMessageCRC = (short)(Character.getNumericValue(bytesInCRCSegment[intIndex++]) << 4);
        shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytesInCRCSegment[intIndex++]));
        shortMessageCRC = (short)(shortMessageCRC << 4);
        shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytesInCRCSegment[intIndex++]));
        shortMessageCRC = (short)(shortMessageCRC << 4);
        shortMessageCRC = (short)(shortMessageCRC + Character.getNumericValue(bytesInCRCSegment[intIndex]));

        //LOGGER.debugTimedEvent("StreamUtilities.isStaribusTxChecksumValid() TxMessage contains CRC Checksum=" + Utilities.intToFourHexString(shortMessageCRC));

        // Calculate the CRC Checksum of the message,
        // excluding the STX, and up to the character before the checksum field
        // <stx> 2B0001 060A<eot><cr><lf>
        bytesToCrc = new byte[bytesInMessage.length - LENGTH_TAIL - 1];

        // Exclude the STX from the checksum, so start at index=1
        System.arraycopy(bytesInMessage, 1,
                         bytesToCrc, 0,
                         bytesInMessage.length - LENGTH_TAIL - 1);

        // Note the assumption that Input and Output bits are reflected
        //LOGGER.debugProtocolEvent("StreamUtilities.isStaribusTxChecksumValid() Calculating Tx CRC using [" + Utilities.byteArrayToSpacedHex(bytesToCrc) + "]");

        shortCalculatedCRC = CRC16.crc16(bytesToCrc, true, true);
        //LOGGER.debugProtocolEvent("StreamUtilities.isStaribusTxChecksumValid() Calculated Tx CRC Checksum=" + Utilities.intToFourHexString(shortCalculatedCRC));

        boolValid = (shortMessageCRC == shortCalculatedCRC);

        if (boolValid)
            {
            //LOGGER.debugTimedEvent("StreamUtilities.isStaribusTxChecksumValid() Tx CRC Checksum correct!");
            }
        else
            {
            LOGGER.error(SOURCE + "Tx CRC Checksum mismatch! [message=" + Integer.toHexString(shortMessageCRC) + "] [calculated=" + Integer.toHexString(shortCalculatedCRC) + "]");
            }

        return (boolValid);
        }
    }
