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

package org.lmn.fc.common.datatranslators.hex;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * HexFileHelper.
 */

public final class HexFileHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ObservatoryConstants
    {
    // String Resources
    private static final String EOF_RECORD = ":00000001FF";

    public static final int DUMP_BYTES_PER_LINE = 32;


    /***********************************************************************************************
     * Parse the specified HEX file into a Byte List.
     * The maxlength parameter should indicate the highest address + 1 to be found in the records.
     * Returns a List of exactly the same length as the number of data bytes in the file.
     * Return NULL on any failure, details in errors.
     *
     * @param file
     * @param maxlength
     * @param errors
     * @param debug
     *
     * @return List<Byte>
     */

    public static List<Byte> parseHexFileToList(final File file,
                                                final int maxlength,
                                                final List<String> errors,
                                                final boolean debug)
        {
        final String SOURCE = "HexFileHelper.parseHexFileToList() ";
        List<Byte> listData;

        // Create the maximum possible List, and initialise
        listData = new ArrayList<Byte>(maxlength);

        for (int i = 0;
             i < maxlength;
             i++)
            {
            listData.add((byte) 0xff);
            }

        try
            {
            final LineNumberReader reader;
            final List<String> listRecords;
            boolean boolReading;
            int intHighestAddress;

            reader = new LineNumberReader(new FileReader(file));
            // Let's take a guess at the number of records
            listRecords = new ArrayList<String>(((int)file.length() >> 5));
            boolReading = true;

            // Reads the entire file into a List with each Record element being a String
            // The line numbers are now the List index
            while (boolReading)
                {
                final String strRecord;

                strRecord = reader.readLine();

                if (strRecord != null)
                    {
                    // Remove any leading or trailing spaces, there shouldn't be any...
                    listRecords.add(strRecord.trim());
                    }
                else
                    {
                    boolReading = false;
                    }
                }

            // Start at the lowest possible address
            intHighestAddress = 0;

            // Read each record and validate its syntax
            // If valid, write the data to the correct address in the data List
            for (int intLineNumber = 0;
                 ((intHighestAddress >= 0)
                 && (intLineNumber < listRecords.size()));
                 intLineNumber++)
                {
                final String strRecord;

                strRecord = listRecords.get(intLineNumber);
                intHighestAddress = writeRecordToList(listData,
                                                      strRecord,
                                                      intHighestAddress,
                                                      maxlength,
                                                      errors);
                }

            if (intHighestAddress >= 0)
                {
                // Truncate the supplied List to the space actually used
                // We have to do it this way because removeRange() is protected...
                while (listData.size() > (intHighestAddress + 1))
                    {
                    // Remove the last element
                    listData.remove(listData.size() - 1);
                    }

                // Show the resultant data if possible
                LOGGER.debug(((debug) && (listData != null)),
                             SOURCE);
                LOGGER.debug(((debug) && (listData != null)),
                             dumpHex(listData, DUMP_BYTES_PER_LINE));
                }
            else
                {
                // Ensure that we don't return invalid data
                listData = null;
                }
            }

        catch (FileNotFoundException exception)
            {
            listData = null;
            errors.add(METADATA_TARGET_FILE
                           + METADATA_ACTION_READ
                           + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        catch (IOException exception)
            {
            listData = null;
            errors.add(METADATA_TARGET_FILE
                           + METADATA_ACTION_READ
                           + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_IMPORT + TERMINATOR + SPACE
                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            }

        catch (Exception exception)
            {
            listData = null;
            errors.add(METADATA_TARGET_FILE
                           + METADATA_ACTION_READ
                           + METADATA_RESULT + DataTranslatorInterface.ERROR_FILE_IMPORT + TERMINATOR + SPACE
                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR);
            exception.printStackTrace();
            }

        return (listData);
        }


    /***********************************************************************************************
     * Read the specified data record, and write to the data List if the syntax is valid.
     * The List is assumed to be initialised to be of at least maxlength in size.
     * Return the highest address written to, or -1 on failure.
     *
     * @param datalist
     * @param record
     * @param highestaddress
     * @param maxlength
     * @param errors
     *
     * @return int
     */

    private static int writeRecordToList(final List<Byte> datalist,
                                         final String record,
                                         final int highestaddress,
                                         final int maxlength,
                                         final List<String> errors)
        {
        final String SOURCE = "HexFileHelper.writeRecordToList() ";
        final int RADIX = 16;
        int intHighestAddress;

        // Use the previous highest address to start with
        intHighestAddress = highestaddress;

        try
            {
            // 0 Record Marker:
            // The first character of the line is always a colon (ASCII 0x3A)
            // to identify the line as an Intel HEX file

            if (record.startsWith(COLON))
                {
                final int intRecordLength;
                final int intRecordStartAddress;
                final int intRecordType;
                int intCalculatedChecksum;

                intCalculatedChecksum = 0;

                // 1-2 Record Length:
                // This field contains the number of data bytes in the record
                // represented as a 2-digit hexadecimal number. This is the
                // total number of *data* bytes, not including the checksum byte
                // nor the first 9 characters of the line.
                intRecordLength = Integer.parseInt(record.substring(1, 3), RADIX);
                intCalculatedChecksum += intRecordLength;

                // 3-6 Address:
                // This field contains the address where the data should be
                // loaded into the chip. This is a value from 0 to 65,535
                // represented as a 4-digit hexadecimal value.
                intRecordStartAddress = Integer.parseInt(record.substring(3, 7), RADIX);

                // Read again as bytes for the checksum
                intCalculatedChecksum += intRecordStartAddress & 0x00ff;
                intCalculatedChecksum += (intRecordStartAddress & 0xff00) >> 8;

                // The highest address in the record must lie within the range allowed by the target device
                if ((intRecordStartAddress >= 0)
                    && ((intRecordStartAddress + intRecordLength - 1) < maxlength))
                    {
                    // 7-8 Record Type:
                    // This field indicates the type of record for this line.
                    //
                    // 00 = Record contains normal data.
                    // 01 = End of File.
                    // 02 = Extended address.
                    intRecordType = Integer.parseInt(record.substring(7, 9), RADIX);
                    intCalculatedChecksum += intRecordType;

                    if ((intRecordType >= 0)
                       && (intRecordType <= 2))
                        {
                        // Ordinary Data Record
                        if (intRecordType == 0)
                            {
                            int intDataAddress;
                            boolean boolValidAddress;

                            intDataAddress = 0;
                            boolValidAddress = true;

                            // 9-? Data Bytes:
                            // The data are represented as 2-digit hexadecimal values

                            // Make sure we never index past the end of the Record
                            // Step along two characters at a time
                            for (int intCharacterIndex = 0;
                                (boolValidAddress
                                    && ((intCharacterIndex + 11) < record.length())
                                    && (intCharacterIndex < (intRecordLength << 1)));
                                intCharacterIndex = intCharacterIndex + 2)
                                {
                                final int intDataByte;

                                // Remember the index is characters, not assembled data bytes
                                intDataAddress = intRecordStartAddress + (intCharacterIndex >> 1);
                                intDataByte = Integer.parseInt(record.substring(intCharacterIndex + 9, (intCharacterIndex + 11)), RADIX);

                                // Remember to check again for address violations on every write
                                // since the record may generate an address which is beyond the limit
                                if ((intDataAddress < maxlength)
                                    && (intDataAddress < datalist.size()))
                                    {
                                    // Record the highest address written to
                                    intHighestAddress = Math.max(intHighestAddress, intDataAddress);

                                    // Write the byte into the result List
                                    datalist.set(intDataAddress, (byte)intDataByte);

                                    // Update the checksum
                                    intCalculatedChecksum = intCalculatedChecksum + intDataByte;
                                    }
                                else
                                    {
                                    // The address of the current byte is beyond the limit, so stop now
                                    boolValidAddress = false;
                                    }
                                }

                            if (boolValidAddress)
                                {
                                final int intRecordChecksum;

                                // Last 2 Checksum:
                                // The last two characters of the line are a checksum for the
                                // line. The checksum value is calculated by taking the two's
                                // complement of the sum of all the preceding data bytes,
                                // excluding the checksum byte itself and the colon at the
                                // beginning of the line.
                                intRecordChecksum = Integer.parseInt(record.substring(record.length()-2, record.length()), RADIX);

//                                LOGGER.log("RECORD [length=" + intRecordLength
//                                               + "] [record_address=" + Utilities.intToFourHexString(intRecordStartAddress)
//                                               + "] [highest_address=" + Utilities.intToFourHexString(intHighestAddress)
//                                               + "] [recordtype=" + Utilities.intToTwoHexString(intRecordType)
//                                               + "] [checksum-calc=" + Utilities.intToTwoHexString(intCalculatedChecksum)
//                                               + "] [checksum-calc-comp=" + Utilities.intToTwoHexString(-intCalculatedChecksum)
//                                               + "] [checksum-rec=" + Utilities.intToTwoHexString(intRecordChecksum)
//                                               + "] [" + record + "]");

                                // Remember to take the two's complement of the calculated checksum
                                // http://en.wikipedia.org/wiki/Intel_HEX
                                intCalculatedChecksum = ((0x0100 - (intCalculatedChecksum & 0x00ff)) & 0x00ff);

                                if (intRecordChecksum != intCalculatedChecksum)
                                    {
                                    intHighestAddress = -1;
                                    errors.add(METADATA_TARGET_FILE
                                                   + METADATA_ACTION_READ
                                                   + METADATA_RESULT + "Invalid checksum" + TERMINATOR + SPACE
                                                   + "[record_checksum=" + Utilities.intToTwoHexString(intRecordChecksum) + TERMINATOR + SPACE
                                                   + "[calculated_checksum=" + Utilities.intToTwoHexString(intCalculatedChecksum) + TERMINATOR + SPACE
                                                   + METADATA_DETAIL + record + TERMINATOR);
                                    }
                                }
                            else
                                {
                                intHighestAddress = -1;
                                errors.add(METADATA_TARGET_FILE
                                               + METADATA_ACTION_READ
                                               + METADATA_RESULT + "Address out of range" + TERMINATOR + SPACE
                                               + "[dataaddress=" + Utilities.intToFourHexString(intDataAddress) + TERMINATOR + SPACE
                                               + METADATA_DETAIL + record + TERMINATOR);
                                }
                            }

                        // Is it EOF?
                        else if (intRecordType == 1)
                            {
                            // The Record should look like ":00000001FF" every time
                            // Return with the highest address unaffected
                            if (!EOF_RECORD.equals(record))
                                {
                                errors.add(METADATA_TARGET_FILE
                                               + METADATA_ACTION_READ
                                               + METADATA_RESULT + "Invalid EOF Record" + TERMINATOR + SPACE
                                               + METADATA_DETAIL + record + TERMINATOR);
                                }
                            }

                        // It must be Extended Addressing
                        else
                            {
                            // Not sure what to do with this?
                            intHighestAddress = -1;
                            errors.add(METADATA_TARGET_FILE
                                           + METADATA_ACTION_READ
                                           + METADATA_RESULT + "RecordType indicates Extended Addressing" + TERMINATOR);
                            }
                        }
                    else
                        {
                        intHighestAddress = -1;
                        errors.add(METADATA_TARGET_FILE
                                       + METADATA_ACTION_READ
                                       + METADATA_RESULT + "RecordType out of range" + TERMINATOR + SPACE
                                       + "[recordtype=" + Utilities.intToTwoHexString(intRecordType) + TERMINATOR + SPACE
                                       + METADATA_DETAIL + record + TERMINATOR);
                        }
                    }
                else
                    {
                    intHighestAddress = -1;
                    errors.add(METADATA_TARGET_FILE
                                   + METADATA_ACTION_READ
                                   + METADATA_RESULT + "Address out of range" + TERMINATOR + SPACE
                                   + "[address=" + Utilities.intToFourHexString(intRecordStartAddress) + TERMINATOR + SPACE
                                   + METADATA_DETAIL + record + TERMINATOR);
                    }
                }
            else
                {
                // Leave immediately if any record is invalid
                intHighestAddress = -1;
                errors.add(METADATA_TARGET_FILE
                               + METADATA_ACTION_READ
                               + METADATA_RESULT + "Record does not start with ':'" + TERMINATOR + SPACE
                               + METADATA_DETAIL + record + TERMINATOR);
                }
            }

        catch (NumberFormatException exception)
            {
            // Leave immediately if any record is invalid
            intHighestAddress = -1;
            errors.add(METADATA_TARGET_FILE
                           + METADATA_ACTION_READ
                           + METADATA_RESULT + "Unable to parse Record" + TERMINATOR + SPACE
                           + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR + SPACE
                           + METADATA_DETAIL + record + TERMINATOR);
            }

        return (intHighestAddress);
        }


    /***********************************************************************************************
     * Dumps a Byte List as Hex, breaking the result into lines.
     *
     * @param bytes        The byte List to dump
     * @param bytesperline The maximum number of bytes per line.
     *                     The next byte will be written to a new line
     *
     * @return String
     */

    public static String dumpHex(final List<Byte> bytes,
                                 final int bytesperline)
        {
        return (dumpHex(bytes, 0, bytesperline));
        }


    /***********************************************************************************************
     * Dumps a Byte List as Hex, breaking the result into lines.
     * Optionally start the address at a specified offset.
     *
     * @param bytes        The byte List to dump
     * @param addressoffset
     * @param bytesperline The maximum number of bytes per line.
     *                     The next byte will be written to a new line
     *
     * @return String
     */

    public static String dumpHex(final List<Byte> bytes,
                                 final int addressoffset,
                                 final int bytesperline)
        {
        final StringBuffer bufferDump;

        bufferDump = new StringBuffer();

        if ((bytes != null)
            && (!bytes.isEmpty()))
            {
            int intColumnCounter;

            bufferDump.append("\n");
            bufferDump.append(Utilities.intToFourHexString(addressoffset));
            bufferDump.append(": ");

            intColumnCounter = -1;

            for (int intIndex = 0;
                 intIndex < bytes.size();
                 intIndex++)
                {
                if (++intColumnCounter == bytesperline)
                    {
                    bufferDump.append("\n");
                    bufferDump.append(Utilities.intToFourHexString(intIndex + addressoffset));
                    bufferDump.append(": ");
                    intColumnCounter = 0;
                    }

                bufferDump.append(Utilities.byteToTwoHexString(bytes.get(intIndex)));
                bufferDump.append(" ");
                }
            }

        return (bufferDump.toString());
        }


    /***********************************************************************************************
     * Dumps a byte array as Hex, breaking the result into lines.
     *
     * @param bytes        The byte array to dump
     * @param bytesperline The maximum number of bytes per line.
     *                     The next byte will be written to a new line
     *
     * @return String
     */

    public static String dumpHex(final byte[] bytes,
                                 final int bytesperline)
        {
        final StringBuffer bufferDump;

        bufferDump = new StringBuffer();

        if (bytes != null)
            {
            int intColumnCounter;

            bufferDump.append("\n0000: ");

            intColumnCounter = -1;

            for (int intIndex = 0;
                 intIndex < bytes.length;
                 intIndex++)
                {
                if (++intColumnCounter == bytesperline)
                    {
                    bufferDump.append('\n');
                    bufferDump.append(Utilities.intToFourHexString(intIndex));
                    bufferDump.append(": ");
                    intColumnCounter = 0;
                    }

                bufferDump.append(Utilities.byteToTwoHexString(bytes[intIndex]));
                bufferDump.append(" ");
                }
            }

        return (bufferDump.toString());
        }


    /***********************************************************************************************
     * Dumps an integer array as Hex, breaking the result into lines.
     *
     * @param integers        The integer array to dump
     * @param integersperline The maximum number of integers per line.
     *                        The next integer will be written to a new line
     * @return String
     */

    public static String dumpHex(final int[] integers,
                                 final int integersperline)
        {
        final StringBuffer bufferDump;

        bufferDump = new StringBuffer();

        if (integers != null)
            {
            int intColumnCounter;

            bufferDump.append("\n00000000: ");

            intColumnCounter = -1;

            for (int intIndex = 0;
                 intIndex < integers.length;
                 intIndex++)
                {
                if (++intColumnCounter == integersperline)
                    {
                    bufferDump.append('\n');
                    bufferDump.append(Utilities.intToEightHexString(intIndex));
                    bufferDump.append(": ");
                    intColumnCounter = 0;
                    }

                bufferDump.append(Utilities.intToFourHexString(integers[intIndex]));
                bufferDump.append(" ");
                }
            }

        return (bufferDump.toString());
        }


    /***********************************************************************************************
     * Dumps a double array using the specified Pattern, breaking the result into lines.
     *
     * @param doubles        The double array to dump
     * @param pattern        The format pattern
     * @param doublesperline The maximum number of doubles per line.
     *                       The next double will be written to a new line
     * @return String
     */

    public static String dumpDoubles(final double[] doubles,
                                     final DecimalFormatPattern pattern,
                                     final int doublesperline)
        {
        final StringBuffer bufferDump;

        bufferDump = new StringBuffer();

        if ((doubles != null)
            && (pattern != null))
            {
            int intColumnCounter;

            bufferDump.append("\n00000000: ");

            intColumnCounter = -1;

            for (int intIndex = 0;
                 intIndex < doubles.length;
                 intIndex++)
                {
                if (++intColumnCounter == doublesperline)
                    {
                    bufferDump.append('\n');
                    bufferDump.append(Utilities.intToEightHexString(intIndex));
                    bufferDump.append(": ");
                    intColumnCounter = 0;
                    }

                bufferDump.append(pattern.format(doubles[intIndex]));
                bufferDump.append(" ");
                }
            }

        return (bufferDump.toString());
        }
    }
