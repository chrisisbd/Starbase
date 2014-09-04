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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

import org.apache.commons.lang3.ArrayUtils;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.hex.HexFileHelper;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.UDPClient;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 *  UploadControllerOS.
 */

public final class UploadControllerOS implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 ObservatoryConstants
    {
    // String Resources
    private static final String BROADCAST_IP_ADDRESS = "255.255.255.255";

    private static final int INDEX_LOADER_VERSION = 0;
    private static final int INDEX_CODESIZE = 1;


    /***********************************************************************************************
     * Enumerate the supported target Networks.
     */

    private enum TargetNetwork
        {
        STARINET    ("Starinet"),
        STARIBUS    ("Staribus");


        private final String strName;


        /***********************************************************************************************
         * Get the TargetNetwork enum corresponding to the specified TargetNetwork name.
         * Return NULL if the TargetNetwork name is not found.
         *
         * @param name
         *
         * @return TargetNetwork
         */

        public static TargetNetwork getTargetForName(final String name)
            {
            TargetNetwork network;

            network = null;

            if ((name != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(name)))
                {
                final TargetNetwork[] targets;
                boolean boolFoundIt;

                targets = values();
                boolFoundIt = false;

                for (int i = 0;
                     (!boolFoundIt) && (i < targets.length);
                     i++)
                    {
                    final TargetNetwork targetNetwork;

                    targetNetwork = targets[i];

                    if (name.equals(targetNetwork.getName()))
                        {
                        network = targetNetwork;
                        boolFoundIt = true;
                        }
                    }
                }

            return (network);
            }


        /*******************************************************************************************
         * Construct a TargetNetwork.
         *
         * @param name
         */

        private TargetNetwork(final String name)
            {
            this.strName = name;
            }


        /***********************************************************************************************
         * Get the Target name.
         *
         * @return String
         */

        public String getName()
            {
            return (this.strName);
            }
        }


    /***********************************************************************************************
     * Enumerate the supported target CPUs.
     * ID = 32 for mega328 (so a mega 168 would be 0x10 = 16).
     */

    private enum TargetCPU
        {
        ATMEGA328    ("ATmega328", 32001, 1200, 10000, 0x7000, (byte) 0x20, 128, (byte) 0xff);


        private final String strName;
        private final int intLocalPort;
        private final int intRemotePort;
        private final int intTimeoutMillis;
        private final int intCodesize;
        private final byte byteID;
        private final int intPagesize;
        private final byte byteFill;


        /***********************************************************************************************
         * Get the TargetCPU enum corresponding to the specified TargetCPU name.
         * Return NULL if the TargetCPU name is not found.
         *
         * @param name
         *
         * @return TargetCPU
         */

        public static TargetCPU getTargetForName(final String name)
            {
            TargetCPU source;

            source = null;

            if ((name != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(name)))
                {
                final TargetCPU[] targets;
                boolean boolFoundIt;

                targets = values();
                boolFoundIt = false;

                for (int i = 0;
                     (!boolFoundIt) && (i < targets.length);
                     i++)
                    {
                    final TargetCPU updateSource;

                    updateSource = targets[i];

                    if (name.equals(updateSource.getName()))
                        {
                        source = updateSource;
                        boolFoundIt = true;
                        }
                    }
                }

            return (source);
            }


        /*******************************************************************************************
         * Construct a TargetCPU.
         *
         * @param name
         * @param localport
         * @param remotelport
         * @param timeout
         * @param codesize
         * @param id
         * @param pagesize
         * @param fill
         */

        private TargetCPU(final String name,
                          final int localport,
                          final int remotelport,
                          final int timeout,
                          final int codesize,
                          final byte id,
                          final int pagesize,
                          final byte fill)
            {
            this.strName = name;
            this.intLocalPort = localport;
            this.intRemotePort = remotelport;
            this.intTimeoutMillis = timeout;
            this.intCodesize = codesize;
            this.byteID = id;
            this.intPagesize = pagesize;
            this.byteFill = fill;
            }


        /***********************************************************************************************
         * Get the Target name.
         *
         * @return String
         */

        public String getName()
            {
            return (this.strName);
            }


        /***********************************************************************************************
         * Get the Local Port.
         *
         * @return int
         */

        public int getLocalPort()
            {
            return (this.intLocalPort);
            }


        /***********************************************************************************************
         * Get the Remote Port.
         *
         * @return int
         */

        public int getRemotePort()
            {
            return (this.intRemotePort);
            }


        /***********************************************************************************************
         * Get the Timeout in milliseconds.
         *
         * @return int
         */

        public int getTimeoutMillis()
            {
            return (this.intTimeoutMillis);
            }


        /***********************************************************************************************
         * Get the size of the Code memory area.
         *
         * @return int
         */

        public int getCodesize()
            {
            return (this.intCodesize);
            }


        /***********************************************************************************************
         * Get the byte to use to fil the unused area of the Code Memory.
         *
         * @return byte
         */

        public byte getID()
            {
            return (this.byteID);
            }


        /***********************************************************************************************
         * Get the Pagesize for this CPU.
         *
         * @return int
         */

        public int getPagesize()
            {
            return (this.intPagesize);
            }


        /***********************************************************************************************
         * Get the byte to use to fill the unused area of the Code Memory.
         *
         * @return byte
         */

        public byte getFill()
            {
            return (this.byteFill);
            }
        }


    /***********************************************************************************************
     * doUploadControllerOS().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doUploadControllerOS(final ObservatoryInstrumentDAOInterface dao,
                                                                final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "UploadControllerOS.doUploadControllerOS()";
        final int PARAMETER_COUNT = 4;
        final int INDEX_NETWORK = 0;
        final int INDEX_TARGET = 1;
        final int INDEX_FILENAME = 2;
        final int INDEX_FILLUNUSED = 3;
        final CommandType cmdUploadOS;
        final List<ParameterType> listParameters;
        final String strResponseValue;
        final ResponseMessageInterface responseMessage;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isMasterDebug(),
                               SOURCE);

        // Get the latest Resources
        dao.readResources();

        // Don't affect the CommandType of the incoming Command
        cmdUploadOS = (CommandType)commandmessage.getCommandType().copy();

        // We expect four parameters, the network, target CPU, the filename, and fillunused flag
        listParameters = cmdUploadOS.getParameterList();

        // Do not change any DAO data containers!

        // Check the parameters before continuing
        if ((dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (listParameters != null)
            && (listParameters.size() == PARAMETER_COUNT)
            && (listParameters.get(INDEX_NETWORK) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_NETWORK).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_TARGET) != null)
            && (SchemaDataType.STRING.equals(listParameters.get(INDEX_TARGET).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_FILENAME) != null)
            && (SchemaDataType.FILE_NAME.equals(listParameters.get(INDEX_FILENAME).getInputDataType().getDataTypeName()))
            && (listParameters.get(INDEX_FILLUNUSED) != null)
            && (SchemaDataType.BOOLEAN.equals(listParameters.get(INDEX_FILLUNUSED).getInputDataType().getDataTypeName())))
            {
            final String strTargetNetwork;
            final TargetNetwork targetNetwork;
            final String strTargetCPU;
            final TargetCPU targetCPU;
            final String strFilename;
            final boolean boolFillUnused;
            final List<String> listLogEntries;
            final List<String> errors;

            strTargetNetwork = listParameters.get(INDEX_NETWORK).getValue();
            targetNetwork = TargetNetwork.getTargetForName(strTargetNetwork);

            strTargetCPU = listParameters.get(INDEX_TARGET).getValue();
            targetCPU = TargetCPU.getTargetForName(strTargetCPU);

            strFilename = listParameters.get(INDEX_FILENAME).getValue();

            // This should never throw NumberFormatException, because it has already been parsed
            boolFillUnused = Boolean.parseBoolean(listParameters.get(INDEX_FILLUNUSED).getValue());

            listLogEntries = new ArrayList<String>(10);

            errors = new ArrayList<String>(10);

            if (TargetNetwork.STARINET.equals(targetNetwork))
                {
                // Only Starinet is supported in this version
                switch (targetCPU)
                    {
                    case ATMEGA328:
                        {
                        // SUCCESS
                        // PREMATURE_TERMINATION file is not a valid HEX file
                        //                       file exceeds the permitted length

                        dao.getResponseMessageStatusList().add(uploadATmega328(targetCPU,
                                                                                 strFilename,
                                                                                 boolFillUnused,
                                                                                 listLogEntries,
                                                                                 errors));
                        break;
                        }

                    default:
                        {
                        errors.add("Invalid Target CPU selection " + METADATA_TARGETCPU + strTargetCPU + TERMINATOR);
                        dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                        }
                    }
                }
            else if (TargetNetwork.STARIBUS.equals(targetNetwork))
                {
                // ToDo Add Staribus support
                errors.add("Invalid Target Network selection " + METADATA_TARGETNETWORK  + strTargetNetwork + TERMINATOR);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }
            else
                {
                errors.add("Invalid Target Network selection " + METADATA_TARGETNETWORK  + strTargetNetwork + TERMINATOR);
                dao.getResponseMessageStatusList().add(ResponseMessageStatus.INVALID_PARAMETER);
                }

            // Let's see how we got on...
            if (dao.getResponseMessageStatusList().contains(ResponseMessageStatus.SUCCESS))
                {
                // Safety measure
                if (listLogEntries.size() < 2)
                    {
                    listLogEntries.clear();
                    listLogEntries.add("00"); // Loader Version
                    listLogEntries.add("0");  // Codesize
                    }

                SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                   EventStatus.INFO,
                                                   METADATA_TARGET
                                                       + dao.getHostInstrument().getInstrument().getIdentifier() + TERMINATOR
                                                       + METADATA_ACTION_UPLOAD
                                                       + METADATA_TARGETNETWORK + targetNetwork.getName() + TERMINATOR_SPACE
                                                       + METADATA_TARGETCPU + targetCPU.getName() + TERMINATOR_SPACE
                                                       + METADATA_CODESIZE + listLogEntries.get(INDEX_CODESIZE) + TERMINATOR_SPACE
                                                       + METADATA_FILLUNUSED + boolFillUnused + TERMINATOR_SPACE
                                                       + METADATA_LOADER_VERSION + listLogEntries.get(INDEX_LOADER_VERSION) + TERMINATOR_SPACE
                                                       + METADATA_FILENAME + strFilename + TERMINATOR,
                                                   SOURCE,
                                                   dao.getObservatoryClock());
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
            // The XML configuration was inappropriate
            strResponseValue = ResponseMessageStatus.INVALID_XML.getResponseValue();
            dao.getResponseMessageStatusList().add(DAOCommandHelper.logInvalidXML(dao,
                                                                                    SOURCE,
                                                                                    METADATA_TARGET
                                                                                    + SOURCE.trim()
                                                                                    + TERMINATOR,
                                                                                    METADATA_ACTION_COMMAND));
            }

        responseMessage = ResponseMessageHelper.createResponseMessage(dao,
                                                                      commandmessage,
                                                                      cmdUploadOS,
                                                                      null,
                                                                      null,
                                                                      strResponseValue);
        return (responseMessage);
        }


    /**********************************************************************************************/
    /* Target Utilities                                                                           */
    /***********************************************************************************************
     * Upload the OS for the ATmega328, from the specified HEX file.
     * Fill unused code memory if requested.
     * Capture the version number of the bootloader.
     * Return SUCCESS, or PREMATURE_TERMINATION if the file is not a valid HEX file,
     * or it exceeds the permitted length.
     *
     * @param target
     * @param filename
     * @param fillunused
     * @param logentries
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus uploadATmega328(final TargetCPU target,
                                                         final String filename,
                                                         final boolean fillunused,
                                                         final List<String> logentries,
                                                         final List<String> errors)
        {
        final String SOURCE = "UploadControllerOS.uploadATmega328() ";
        final ResponseMessageStatus responseMessageStatus;

        if ((filename != null)
            && (!EMPTY_STRING.equals(filename.trim())))
            {
            final File fileHex;
            final List<Byte> listData;

            // This can never throw NullPointerException
            fileHex = new File(filename);

            if (fileHex.exists())
                {
                listData = HexFileHelper.parseHexFileToList(fileHex,
                                                            target.getCodesize(),
                                                            errors,
                                                            LOADER_PROPERTIES.isStarinetDebug());
                if ((listData != null)
                    && (!listData.isEmpty()))
                    {
                    // All ok, so send the data to the Controller's AVR Bootloader
                    responseMessageStatus = uploadToAVRBootloader(target,
                                                                  BROADCAST_IP_ADDRESS,
                                                                  listData,
                                                                  fillunused,
                                                                  logentries,
                                                                  errors);
                    }
                else
                    {
                    // Something was wrong with the syntax or length of the Hex file,
                    // details in errors, provided by parseHexFileToList()
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    }
                }
            else
                {
                // The file does not exist, details in errors
                errors.add(SOURCE + "File not found [filename=" + filename + "]");
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                }
            }
        else
            {
            // This should never happen, since it was validated in the Parameter XML
            errors.add(SOURCE + "Invalid filename - check Parameter Regex in Instrument XML");
            responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;
            }

        return (responseMessageStatus);
        }


    //-------------------------------------------------------------------------------------------------
    // Starinet Uploader Protocol Description
    //
    // All transactions are UDP broadcasts to port 1200
    //
    // The first character of the packet is the command, followed by 6 characters of the destination MAC address
    // (for outbound packets, source MAC for inbound)
    //
    // The host program transmits an 'identify'  packet to kick off communication with any devices in bootloader mode.
    // The MAC address in this packet is all zeros meaning any bootloader should respond.
    //
    // I <6 bytes all 0x00>
    //
    // The host then waits for about 5-10 seconds for any/all devices in bootloader mode to respond.
    //
    // The devices respond with an 'acknowledge' packet and its MAC address.
    // There may be additional data giving more details about the target processor but these are not currently used.
    //
    // A <6 bytes Target MAC> < Extra bytes ignored>
    //
    // Ideally the host should capture all responses and allow the user to select which device to program,
    // at the moment my host simply uses the first (and normally only) response.
    //
    // The host then sends as many 'program' packets as necessary waiting for an 'acknowledge' packet from the device between each.
    //
    // The program packet is made up as follows:
    //
    // P
    // <6 bytes Dest MAC>
    // <PacketNumber>
    // <TargetAddressMSB>
    // <TargetAddressLSB>
    // <DataBytesLengthMSB>
    // <DataBytesLengthLSB>
    // <DataBytes>
    //
    // Code words in DataBytes are sent little-endian. The ack will be
    //
    // A
    // <6 bytes Source MAC>
    // <PacketNumber>
    //
    // PacketNumber is incremented for each packet sent.
    // If programming fails the first char will be N (Nack), which causes a retry of that packet.
    //
    // The binary data must be split up into 'pagesize' chunks (see atmega328 data sheet) of 128 bytes(64 words) each.
    // Partial pages are not allowed so the last packet might need to be padded with 0xff up to page size.
    // The address range should be limited to the application area only otherwise you risk overwriting the bootloader,
    // see device datasheet for details of memory layout.

    /***********************************************************************************************
     * Upload the specified data to the AVR Controller's Bootloader.
     * Optionally fill the unused code memory with 0xff.
     * Capture the version number of the bootloader.
     *
     * @param target
     * @param broadcastaddress
     * @param data
     * @param fillunused
     * @param logentries
     * @param errors
     *
     * @return ResponseMessageStatus
     */

    private static ResponseMessageStatus uploadToAVRBootloader(final TargetCPU target,
                                                               final String broadcastaddress,
                                                               final List<Byte> data,
                                                               final boolean fillunused,
                                                               final List<String> logentries,
                                                               final List<String> errors)
        {
        final String SOURCE = "UploadControllerOS.uploadToAVRBootloader() ";
        final int RETRY_COUNT = 5;
        final byte COMMAND_IDENTIFY = 0x49; // ASCII 'I'
        final byte COMMAND_PROGRAM = 0x50;  // ASCII 'P'
        final byte COMMAND_ACK = 0x41;      // ASCII 'A'
        final byte COMMAND_NACK = 0x4e;     // ASCII 'N'
        UDPClient clientUDP;
        ResponseMessageStatus responseMessageStatus;

        clientUDP = null;
        responseMessageStatus = ResponseMessageStatus.SUCCESS;

        try
            {
            final String strHostname;
            final InetAddress inetAddress;
            List<Byte> listPayload;
            byte[] arrayResponse;

            // Set up the UDPClient
            // Note that UDP requests are often restricted to the current subnet
            strHostname = broadcastaddress;
            inetAddress = InetAddress.getByName(strHostname);

            // SocketException
            clientUDP = new UDPClient(inetAddress,
                                      target.getLocalPort(),
                                      target.getRemotePort(),
                                      target.getTimeoutMillis());
            clientUDP.getSocket().setBroadcast(true);

            // The host program transmits an 'identify' packet to kick off communication with any devices in bootloader mode
            // The MAC address in this packet is all zeroes meaning any bootloader should respond
            // I <0x00> <0x00> <0x00> <0x00> <0x00> <0x00>
            listPayload = new ArrayList<Byte>(7);
            listPayload.add(COMMAND_IDENTIFY);
            listPayload.add((byte) 0x00);
            listPayload.add((byte) 0x00);
            listPayload.add((byte) 0x00);
            listPayload.add((byte) 0x00);
            listPayload.add((byte) 0x00);
            listPayload.add((byte) 0x00);

            // Prepare to get the Bootloader Version
            logentries.clear();

            // Send the packet, do NOT call UDPClient.connect() !
            // If the payload is longer than the maximum reliable length of a UDP Datagram
            // (8192) bytes then an IOException is thrown
            clientUDP.send(ArrayUtils.toPrimitive(listPayload.toArray(new Byte[listPayload.size()])));

            // The host then waits for timeout for any/all devices in bootloader mode to respond
            // This method blocks until a UDP Datagram is received
            // This Command is being executed on its own SwingWorker, so this doesn't matter...
            // IOException
            arrayResponse = clientUDP.receive();

            // The devices respond with an 'acknowledge' packet and its MAC address
            // There are additional data giving more details about the target processor
            // A <6 bytes Target MAC> <ChipFlashPageSize> <ProcessorID> <VersionNumber>
            // Ideally the host should capture all responses and allow the user to select which device to program,
            // at the moment this host simply uses the first (and normally only) response.

            if ((arrayResponse != null)
                && (arrayResponse.length > 0))
                {
                final int INDEX_ACK = 0;
                final int INDEX_MAC_ADDRESS = 1;
                final int INDEX_PAGE_SIZE = 7;
                final int INDEX_PACKET_NUMBER = 7;
                final int INDEX_ID = 8;
                final int INDEX_VERSION = 9;
                final byte[] arrayMAC;
                final List<Byte> listMAC;
                final int intChipFlashPageSize;
                final byte byteProcessorID;
                final byte byteVersion;

                LOGGER.debug(LOADER_PROPERTIES.isStarinetDebug(),
                             "[identify_response=" + Utilities.byteArrayToSpacedHex(arrayResponse) + "]");

                // Read the MAC address of the responder, and convert to a List
                arrayMAC = ArrayUtils.subarray(arrayResponse, INDEX_MAC_ADDRESS, INDEX_MAC_ADDRESS+6);
                listMAC = new ArrayList<Byte>(6);

                for (int intMACIndex = 0;
                    intMACIndex < 6;
                    intMACIndex++)
                    {
                    listMAC.add(arrayMAC[intMACIndex]);
                    }

                // ChipFlashPageSize
                intChipFlashPageSize = arrayResponse[INDEX_PAGE_SIZE] << 4;

                // ProcessorID
                byteProcessorID = arrayResponse[INDEX_ID];

                // Version Number (did not exist prior to v1.1)
                if (arrayResponse.length > INDEX_VERSION)
                    {
                    byteVersion = arrayResponse[INDEX_VERSION];
                    }
                else
                    {
                    byteVersion = 0x00;
                    }
                logentries.add(Utilities.byteToTwoHexString(byteVersion));

                // Check that we have exactly the right target
                if ((byteProcessorID == target.getID())
                    && (intChipFlashPageSize == target.getPagesize()))
                    {
                    // Now program the data in PageSize packets
                    if ((data != null)
                        && (!data.isEmpty()))
                        {
                        final int intSizeOfLastPacket;
                        final int intPacketCount;
                        boolean boolSuccessfulPacket;

                        // Record the Codesize (before padding) as the second entry
                        logentries.add(Integer.toString(data.size()));

                        // Fill the unused bytes in the code memory if requested
                        if (fillunused)
                            {
                            while (data.size() < target.getCodesize())
                                {
                                data.add(target.getFill());
                                }
                            }

                        // Only now do we know the page size required for this processor
                        // Pad the data List to be multiple of PageSize bytes
                        intSizeOfLastPacket = data.size() % intChipFlashPageSize;

                        if ((intSizeOfLastPacket != intChipFlashPageSize)
                            && (data.size() < target.getCodesize()))
                            {
                            for (int intPadding = 0;
                                 ((intPadding < (intChipFlashPageSize - intSizeOfLastPacket))
                                  && (data.size() < target.getCodesize()));
                                 intPadding++)
                                {
                                data.add(target.getFill());
                                }
                            }

                        // Calculate the number of packets to send
                        intPacketCount = data.size() / intChipFlashPageSize;
                        boolSuccessfulPacket = true;

                        for (int intPacketIndex = 0;
                            ((intPacketIndex < intPacketCount)
                                && (boolSuccessfulPacket));
                            intPacketIndex++)
                            {
                            final int intPacketAddress;
                            boolean boolKeepRetrying;

                            intPacketAddress = intPacketIndex * intChipFlashPageSize;

                            // The program packet is made up as follows:
                            // P
                            // <6 bytes Dest MAC>
                            // <PacketNumber>
                            // <TargetAddressMSB>
                            // <TargetAddressLSB>
                            // <DataBytesLengthMSB>
                            // <DataBytesLengthLSB>
                            // <DataBytes>
                            // Code words in DataBytes are sent little-endian, i.e. LSB first
                            // but that order will have been set during compilation to create the HEX file

                            listPayload = new ArrayList<Byte>(12 + intChipFlashPageSize);

                            // Program Command 'P'
                            listPayload.add(COMMAND_PROGRAM);

                            // MAC Address
                            listPayload.addAll(listMAC);

                            // PacketNumber
                            listPayload.add((byte) intPacketIndex);

                            // Target Address MSB, LSB
                            listPayload.add((byte) ((intPacketAddress & 0xff00) >> 8));
                            listPayload.add((byte) ((intPacketAddress & 0x00ff)));

                            // DataBytesLength MSB, LSB
                            listPayload.add((byte) ((intChipFlashPageSize & 0xff00) >> 8));
                            listPayload.add((byte) ((intChipFlashPageSize & 0x00ff)));

                            // DataBytes
                            listPayload.addAll(data.subList(intPacketAddress,
                                                            intPacketAddress + intChipFlashPageSize));

                            LOGGER.debug(LOADER_PROPERTIES.isStarinetDebug(),
                                         "[send] [packet_index=" + intPacketIndex
                                             + "] [packet_address=" + intPacketAddress
                                             + "] [pagesize=" + intChipFlashPageSize + "]");
                            LOGGER.debug(LOADER_PROPERTIES.isStarinetDebug(),
                                         HexFileHelper.dumpHex(listPayload,
                                                               intPacketAddress,
                                                               HexFileHelper.DUMP_BYTES_PER_LINE));
                            boolKeepRetrying = true;

                            // Start the retry loop
                            for (int intRetryIndex = 0;
                                ((intRetryIndex < RETRY_COUNT)
                                    && (boolKeepRetrying));
                                intRetryIndex++)
                                {
                                // Send the packet to the remote Port
                                clientUDP.send(ArrayUtils.toPrimitive(listPayload.toArray(new Byte[listPayload.size()])));

                                // Wait here for Ack or Nack
                                arrayResponse = clientUDP.receive();

                                if ((arrayResponse != null)
                                    && (arrayResponse.length > 0))
                                    {
                                    // Was it an Ack or Nack from the correct MAC address?
                                    // A
                                    // <6 bytes Source MAC>
                                    // <PacketNumber>

                                    LOGGER.debug(LOADER_PROPERTIES.isStarinetDebug(),
                                                 "[program_response=" + Utilities.byteArrayToSpacedHex(arrayResponse) + "]");

                                    // See if it was an Ack
                                    if ((arrayResponse.length >= 8)
                                        && (COMMAND_ACK == arrayResponse[INDEX_ACK]))
                                        {
                                        final byte[] arrayAckMAC;
                                        final List<Byte> listAckMAC;

                                        // Read the MAC address in the Ack
                                        arrayAckMAC = ArrayUtils.subarray(arrayResponse, INDEX_MAC_ADDRESS, INDEX_MAC_ADDRESS+6);
                                        listAckMAC = new ArrayList<Byte>(6);

                                        for (int intMACIndex = 0;
                                             intMACIndex < 6;
                                             intMACIndex++)
                                            {
                                            listAckMAC.add(arrayAckMAC[intMACIndex]);
                                            }

                                        // Check the the MAC address is correct
                                        if (listMAC.equals(listAckMAC))
                                            {
                                            // Check the packet number
                                            if (arrayResponse[INDEX_PACKET_NUMBER] == (byte)intPacketIndex)
                                                {
                                                // Success, so stop the retry loop and carry on
                                                boolKeepRetrying = false;
                                                boolSuccessfulPacket = true;
                                                }
                                            else
                                                {
                                                // Faulty packet index, so retry?
                                                boolKeepRetrying = true;
                                                boolSuccessfulPacket = false;
                                                errors.add(METADATA_TARGET_STARINET
                                                               + METADATA_ACTION_REQUEST_UDP
                                                               + METADATA_RESULT + "Faulty Packet Index, Retry [index=" + arrayResponse[7] + TERMINATOR);
                                                }
                                            }
                                        else
                                            {
                                            // Faulty MAC address, so retry
                                            boolKeepRetrying = true;
                                            boolSuccessfulPacket = false;
                                            errors.add(METADATA_TARGET_STARINET
                                                           + METADATA_ACTION_REQUEST_UDP
                                                           + METADATA_RESULT + "Faulty MAC Address, Retry [index=" + intRetryIndex + TERMINATOR);
                                            }
                                        }
                                    else if (COMMAND_NACK == arrayResponse[INDEX_ACK])
                                        {
                                        // A Nack must always cause a retry
                                        boolKeepRetrying = true;
                                        boolSuccessfulPacket = false;
                                        errors.add(METADATA_TARGET_STARINET
                                                       + METADATA_ACTION_REQUEST_UDP
                                                       + METADATA_RESULT + "NACK Packet, Retry [index=" + intRetryIndex + TERMINATOR);
                                        }
                                    else
                                        {
                                        // An unknown packet type, so give up
                                        boolKeepRetrying = false;
                                        boolSuccessfulPacket = false;
                                        responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                                        errors.add(METADATA_TARGET_STARINET
                                                       + METADATA_ACTION_REQUEST_UDP
                                                       + METADATA_RESULT + "Unknown packet type (" + arrayResponse[INDEX_ACK] + ")" + TERMINATOR);
                                        }
                                    }
                                else
                                    {
                                    // A fatal error, since no Ack or Nack packet, so no sense in retrying again
                                    boolKeepRetrying = false;
                                    boolSuccessfulPacket = false;
                                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                                    errors.add(METADATA_TARGET_STARINET
                                                   + METADATA_ACTION_REQUEST_UDP
                                                   + METADATA_RESULT + "Program Acknowledge was empty or NULL" + TERMINATOR);
                                    }
                                }
                            }
                        }
                    else
                        {
                        // Record the Codesize (before padding) as the second entry
                        logentries.add("0");

                        responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                        errors.add(METADATA_TARGET_STARINET
                                       + METADATA_ACTION_REQUEST_UDP
                                       + METADATA_RESULT + "No data to transmit" + TERMINATOR);
                        }
                    }
                else
                    {
                    responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                    errors.add(METADATA_TARGET_STARINET
                                   + METADATA_ACTION_REQUEST_UDP
                                   + METADATA_RESULT + "Invalid AVR ProcessorID or PageSize [id="
                                           + byteProcessorID + "] [pagesize="
                                           + intChipFlashPageSize + TERMINATOR);
                    }
                }
            else
                {
                responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                errors.add(METADATA_TARGET_STARINET
                               + METADATA_ACTION_REQUEST_UDP
                               + METADATA_RESULT + "Identify Acknowledge was empty or NULL" + TERMINATOR);
                }
            }

        catch (IndexOutOfBoundsException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                               + METADATA_ACTION_REQUEST_UDP
                               + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ARRAY_INDEX + TERMINATOR + SPACE
                               + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (PortUnreachableException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_PORT + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (SocketException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
                errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_SOCKET + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (IllegalArgumentException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_PARSE_INPUT + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (SecurityException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_SECURITY + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (UnknownHostException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_UNKNOWN_HOST + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (SocketTimeoutException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_TIMEOUT + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (IllegalBlockingModeException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_ILLEGAL_MODE + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        catch (IOException exception)
            {
            responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;
            errors.add(METADATA_TARGET_STARINET
                           + METADATA_ACTION_REQUEST_UDP
                           + METADATA_EXCEPTION + ObservatoryInstrumentDAOInterface.ERROR_IO + TERMINATOR + SPACE
                           + METADATA_MESSAGE + exception.getMessage() + TERMINATOR);
            }

        finally
            {
            // Make sure that the Socket is released
            if (clientUDP != null)
                {
                clientUDP.close();
                }
            }

        return (responseMessageStatus);
        }
    }
