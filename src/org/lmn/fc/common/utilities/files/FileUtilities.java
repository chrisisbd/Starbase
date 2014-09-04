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

//--------------------------------------------------------------------------------------------------
// Utilities package

package org.lmn.fc.common.utilities.files;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.datatranslators.DataFormat;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;

import javax.swing.*;
import java.io.*;
import java.net.URL;


public final class FileUtilities implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ResourceKeys
    {
    // ToDo Convert to enum!
    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    public final static String jar = "jar";
    public final static String dll = "dll";
    public final static String so = "so";
    public final static String jnilib = "jnilib";
    public final static String java = "java";
    public final static String classfile = "class"; // Beware name different!
    public final static String tsv = "tsv";
    public final static String csv = "csv";
    public final static String xls = "xls";
    public final static String xml = "xml";
    public final static String pdf = "pdf";
    public final static String txt = "txt";
    public final static String html = "html";
    public final static String odt = "odt";
    public final static String ods = "ods";
    public final static String odp = "odp";
    public final static String properties = "properties";
    public final static String fits = "fits";
    public final static String h = "h";
    public final static String c = "c";
    public final static String data = "data";

    public static final String SUFFIX_PLUGIN_JAR = "-plugin.jar";
    public static final String SUFFIX_JAVADOC_JAR = "-javadoc.jar";


    /***********************************************************************************************
     * Read a StringBuffer of bytes from the specified filename.
     *
     * @param inputfile
     *
     * @return StringBuffer
     *
     * @throws IOException
     */

    public static StringBuffer readFileAsString(final String inputfile) throws IOException
        {
        final File fileImport;

        fileImport = new File(inputfile);

        return (readFileAsString(fileImport));
        }


    /***********************************************************************************************
     * Read a StringBuffer of bytes from the specified File.
     *
     * @param file
     *
     * @return StringBuffer
     *
     * @throws IOException
     */

    public static StringBuffer readFileAsString(final File file) throws IOException
        {
        final BufferedInputStream streamBuffer;
        final StringBuffer buffer;
        int intByte;

        buffer = new StringBuffer();

        if (file.exists())
            {
            streamBuffer = new BufferedInputStream(new FileInputStream(file));

            while ((intByte = streamBuffer.read()) > -1)
                {
                buffer.append((char) intByte);
                }

            streamBuffer.close();
            }

        return (buffer);
        }


    /***********************************************************************************************
     * Read a StringBuffer of bytes from the specified filename, with a maximum allowed length.
     * If the file is too long, then return null.
     *
     * @param inputfile
     * @param length
     *
     * @return StringBuffer
     *
     * @throws IOException
     */

    public static StringBuffer readFileAsString(final String inputfile,
                                                final long length) throws IOException
        {
        final File fileImport;
        StringBuffer buffer;

        fileImport = new File(inputfile);
        buffer = null;

        if ((fileImport.exists())
            && (inputfile != null))
            {
            // Is the file of a valid length?
            if (fileImport.length() <= length)
                {
                final BufferedInputStream streamBuffer;
                int intByte;

                buffer = new StringBuffer();
                streamBuffer = new BufferedInputStream(new FileInputStream(fileImport));

                while ((intByte = streamBuffer.read()) > -1)
                    {
                    buffer.append((char) intByte);
                    }

                streamBuffer.close();
                }
            }

        return (buffer);
        }


    /***********************************************************************************************
     * Read a file and return an array of bytes.
     *
     * @param filename
     *
     * @return byte[]
     */

    public static byte[] readFileAsByteArray(final String filename)
        {
        byte[] bytes;

        bytes = new byte[] {};

        try
            {
            final File file;
            final InputStream inputStream;
            final long longFileLength;

            file = new File(filename);
            inputStream = new FileInputStream(file);
            longFileLength = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (longFileLength > Integer.MAX_VALUE)
                {
                FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "The file is too long ("
                                                             + longFileLength
                                                             + " > "
                                                             + Integer.MAX_VALUE
                                                             + ")");
                }
            else
                {
                int offset;
                int intBytesRead;

                offset = 0;
                intBytesRead = 0;
                bytes = new byte[(int) longFileLength];

                while (offset < bytes.length
                    && (intBytesRead >= 0))
                    {
                    intBytesRead = inputStream.read(bytes, offset, (bytes.length - offset));
                    offset += intBytesRead;
                    }

                if (offset < bytes.length)
                    {
                    FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                               "Could not read all data from file " + file.getName());
                    }

                // We should now have the entire file in the byte array,
                // so we don't need the file again
                inputStream.close();
                }
            }

        catch (IOException exception)
            {
            FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                       "File operation failed [exception=" + exception.getMessage() + "]");
            }

        return (bytes);
        }


    /***********************************************************************************************
     * Returns the contents of the file in a byte array.
     *
     * @param file File to convert
     *
     * @return Array of bytes
     *
     * @throws IOException
     */

    public static byte[] getBytesFromFile(final File file) throws IOException
        {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();
//        log.debugTimedEvent("length of file is " + length + " bytes");

        byte[] bytes = null;

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE)
            {
            // File is too large
//            log.warn("File " + file.getName() + " is too big to process");
            }
        else
            {
            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;

            while ((offset < bytes.length) &&
                ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0))
                {
                offset += numRead;
                }

            // Ensure all the bytes have been read in
            if (offset < bytes.length)
                {
                throw new IOException("Could not completely read file " + file.getName());
                }

            // Close the input stream and return bytes
            is.close();
            }

        return bytes;
        }


    /***********************************************************************************************
     * Write an array of bytes out to a file
     *
     * @param directory Directory in which the file should sit
     * @param filename  Name of the file within the directory
     * @param bytes     Contents of file as an array of bytes
     *
     * @throws IOException
     */

    public static void writeBytesToFile(final String directory,
                                        final String filename,
                                        final byte[] bytes) throws IOException
        {
        final File dir;
        final File fullPath;

        dir = new File(directory);

        if (directory != null)
            {
            if (!dir.exists())
                {
                dir.mkdirs();
                }

            fullPath = new File(dir.getAbsolutePath() + System.getProperty("file.separator") + filename);
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "Saving file as " + fullPath.getAbsolutePath());

            if (bytes.length <= 0)
                {
                throw new IOException("Zero length file!");
                }

            if (!fullPath.exists())
                {
                final OutputStream outputStream;

                outputStream = new FileOutputStream(fullPath);
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
                }
            else
                {
                throw new IOException("File " + fullPath.getAbsolutePath()
                                        + " already exists and will not be overwritten");
                }
            }
        else
            {
            throw new IOException("Directory path is null!");
            }
        }


    /***********************************************************************************************
     * Delete a file
     *
     * @param path
     *
     * @throws IOException
     */

    public static void deleteFile(final String path) throws IOException
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "FileUtiltities.deleteFile() Deleting " + path);

        File file = new File(path);

        // Recursively delete children if this is a directory
        if (file.isDirectory())
            {
//            log.debugTimedEvent(path + " is a directory.  Deleting all child nodes");

            File[] children = file.listFiles();

            for (int i = 0; i < children.length; i++)
                {
                deleteFile(children[i].getAbsolutePath());
                }
            }

        if (!file.delete())
            {
            throw new IOException("Could not delete " + path);
            }
        }


    /***********************************************************************************************
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path
     *
     * @return ImageIcon
     */

    public static ImageIcon createImageIcon(String path)
        {
        final URL imgURL = FileUtilities.class.getResource(path);

        if (imgURL != null)
            {
            return new ImageIcon(imgURL);
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "Couldn't find file: " + path);
            return null;
            }
        }


    /***********************************************************************************************
     * Get the extension of a file.
     *
     * @return String
     */

    public static String getExtension(File f)
        {
        String ext;
        String s;
        int i;

        ext = null;
        s = f.getName();
        i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
            {
            ext = s.substring(i + 1).toLowerCase();
            }

        return ext;
        }


    /***********************************************************************************************
     * Produce a timestamp for appending to a filename.
     * The format is '_yyyymmdd_hhmmss'.
     *
     * @return String
     */

    public static final String timestampFileName()
        {
        String strDate;
        String strTime;
        StringBuffer strBuffer;

        strBuffer = new StringBuffer("_");
        strDate = ChronosHelper.toDateString(Chronos.getCalendarDateNow());
        strTime = ChronosHelper.toTimeString(Chronos.getCalendarTimeNow());

        // Remove slashes from Date
        for (int i = 0; i < strDate.length(); i++)
            {
            if (strDate.charAt(i) != '-')
                {
                strBuffer.append(strDate.charAt(i));
                }
            }

        strBuffer.append('_');

        // Remove colons from Time
        for (int i = 0; i < strTime.length(); i++)
            {
            if (strTime.charAt(i) != ':')
                {
                strBuffer.append(strTime.charAt(i));
                }
            }

        return (strBuffer.toString());
        }


    /***********************************************************************************************
     * Map the specified file format to a suitable file extension.
     * Just return the supplied format if no mapping is found.
     * ToDO Format could be an enum?
     *
     * @param format
     *
     * @return String
     */

    public static String mapFormatToExtension(final String format)
        {
        String strExtension;

        strExtension = format;

        if (format != null)
            {
            if ("TabSeparated".equals(format))
                {
                strExtension = tsv;
                }
            else if ("Excel".equals(format))
                {
                strExtension = xls;
                }
            else if ("HTML".equals(format))
                {
                strExtension = html;
                }
            else if ("XML".equals(format))
                {
                strExtension = xml;
                }
            else if ("FITS".equals(format))
                {
                strExtension = fits;
                }
            }

        return (strExtension);
        }


    /***********************************************************************************************
     * Map all invalid filename characters to underscores.
     *
     * @param string
     *
     * @return New string with all invalid filename characters replaced with <code>_</code>
     */

    public static String mapFilenameChars(final String string)
        {
        String retVal;

        retVal = string.replaceAll(" ", "_");
        retVal =
            retVal.replaceAll("[#,|,=,\\^,\\(,\\),;,:,&,<,>,\\*,\\?,\\[,\\],{,},\\$,',`,\\/,\\\\,\\~,@,\\\"]",
                              "_");

        return retVal;
        }


    /***********************************************************************************************
     * Build a full filename from the filename prefix, an optional timestamp and a format.
     * Map all invalid filename characters to underscores.
     *
     * @param filename
     * @param timestamp
     * @param format
     *
     * @return String
     */

    public static String buildFullFilename(final String filename,
                                           final boolean timestamp,
                                           final String format)
        {
        final StringBuffer buffer;
        final String strFilename;

        buffer = new StringBuffer();

        // Form the full filename, timestamping if requested
        buffer.append(filename);

        if (timestamp)
            {
            buffer.append(timestampFileName());
            }

        buffer.append(DOT);
        buffer.append(mapFormatToExtension(format));
        strFilename = buffer.toString();

//        return (mapFilenameChars(strFilename));
        return (strFilename);
        }


    /***********************************************************************************************
     * Build a full filename from the filename prefix, an optional timestamp and a format.
     * Map all invalid filename characters to underscores.
     *
     * @param filename
     * @param timestamp
     * @param format
     *
     * @return String
     */

    public static String buildFullFilename(final String filename,
                                           final boolean timestamp,
                                           final DataFormat format)
        {
        final StringBuffer buffer;
        final String strFilename;

        buffer = new StringBuffer();

        // Form the full filename, timestamping if requested
        buffer.append(filename);

        if (timestamp)
            {
            buffer.append(timestampFileName());
            }

        buffer.append(format.getFileExtension());
        strFilename = buffer.toString();

//        return (mapFilenameChars(strFilename));
        return (strFilename);
        }


    /***********************************************************************************************
     * Overwrite existing output file or create a new file.
     *
     * @param file
     *
     * @throws IOException
     */

    public static void overwriteFile(final File file) throws IOException
        {
        final String SOURCE = "FileUtilities.overwriteFile() ";
        final boolean boolDeleted;
        final boolean boolCreated;
        final boolean boolDebug;

        boolDebug = (LOADER_PROPERTIES.isTimingDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        // Overwrite existing output file or create a new file
        if (file.exists())
            {
            boolDeleted = file.delete();
            boolCreated = file.createNewFile();
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Delete and Create [deleted=" + boolDeleted + "] [created=" + boolCreated + "]");
            }
        else
            {
            boolCreated = file.createNewFile();
            LOGGER.debugTimedEvent(boolDebug,
                                   SOURCE + "Create [created=" + boolCreated + "]");
            }
        }
    }
