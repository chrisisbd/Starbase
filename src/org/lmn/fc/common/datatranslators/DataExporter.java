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

package org.lmn.fc.common.datatranslators;

import org.apache.xmlbeans.XmlOptions;
import org.jfree.chart.ChartUtilities;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ExportableComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * DataExporter.
 * A collection of exporters which do not use a DataTranslator.
 */

public final class DataExporter implements FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkSingletons,
                                           ObservatoryConstants
    {
    // String Resources
    private static final String ERROR_XML = "There is no XML document available";
    private static final String ERROR_CHART = "There is no Chart available";
    private static final String ERROR_MAP = "There is no Map available";
    private static final String ERROR_COMPONENT = "There is no Visual Component available";
    private static final String ERROR_IMAGE = "There is no Image available";

    private static final String ERROR_FILE_SAVE = "Unable to save data file";
    private static final String ERROR_FILE_NOT_FOUND = "Data file not found";
    private static final String ERROR_ACCESS_DENIED = "Access denied to data file";
    private static final String ERROR_INVALID_FORMAT = "Invalid DataFormat";

    private static final String CR_LF = "\r\n";
    private static final int XML_INDENT = 4;


    /***********************************************************************************************
     * exportChart().
     *
     * @param dao
     * @param chartui
     * @param metadatalist
     * @param filename
     * @param timestamp
     * @param type
     * @param width
     * @param height
     * @param log
     * @param clock
     * @param verbose
     *
     * @return boolean
     */

    public static boolean exportChart(final ObservatoryInstrumentDAOInterface dao,
                                      final ChartUIComponentPlugin chartui,
                                      final List<Metadata> metadatalist,
                                      final String filename,
                                      final boolean timestamp,
                                      final String type,
                                      final int width,
                                      final int height,
                                      final Vector<Vector> log,
                                      final ObservatoryClockInterface clock,
                                      final boolean verbose)
        {
        final String SOURCE = "DataExporter.exportChart() ";

        return (exportChartUsingFilename(dao,
                                         chartui,
                                         metadatalist,
                                         FileUtilities.buildFullFilename(filename, timestamp, type),
                                         type,
                                         width,
                                         height,
                                         log,
                                         clock,
                                         verbose));
        }


    /***********************************************************************************************
     * exportChart().
     * This variant uses a fully specified filename.
     *
     * @param dao
     * @param chartui
     * @param metadatalist
     * @param fullfilename
     * @param type
     * @param width
     * @param height
     * @param log
     * @param clock
     * @param verbose
     *
     * @return boolean
     */

    public static boolean exportChartUsingFilename(final ObservatoryInstrumentDAOInterface dao,
                                                   final ChartUIComponentPlugin chartui,
                                                   final List<Metadata> metadatalist,
                                                   final String fullfilename,
                                                   final String type,
                                                   final int width,
                                                   final int height,
                                                   final Vector<Vector> log,
                                                   final ObservatoryClockInterface clock,
                                                   final boolean verbose)
        {
        final String SOURCE = "DataExporter.exportChartUsingFilename() ";
        final boolean boolDebug;
        boolean boolSuccess;

        boolDebug = (LOADER_PROPERTIES.isChartDebug()
                     || LOADER_PROPERTIES.isMetadataDebug()
                     || LOADER_PROPERTIES.isStaribusDebug()
                     || LOADER_PROPERTIES.isStarinetDebug());

        boolSuccess = false;

        // Let the User know the Chart must have data!
        if ((fullfilename != null)
            && (!EMPTY_STRING.equals(fullfilename))
            && (type != null)
            && (!EMPTY_STRING.equals(type))
            && (chartui != null)
            && (log != null)
            && (clock != null))
            {
            try
                {
                final File file;

                file = new File(fullfilename);
                FileUtilities.overwriteFile(file);

                // Force the Chart to be generated, even if not visible
                // This calls ChartHelper.associateChartUIWithDAO()
                chartui.refreshChart(dao, true, SOURCE);

                if ((chartui.getChartPanel() != null)
                    && (chartui.getChartPanel().getChart() != null))
                    {
                    if (FileUtilities.jpg.equalsIgnoreCase(type))
                        {
                        LOGGER.debug(boolDebug,
                                     SOURCE + "writing [file "
                                     + file.getAbsolutePath()
                                     + "] [width="
                                     + width
                                     + "] [height"
                                     + height
                                     + "]");
                        ChartUtilities.saveChartAsJPEG(file, chartui.getChartPanel().getChart(), width, height);
                        boolSuccess = true;

                        if (verbose)
                            {
                            SimpleEventLogUIComponent.logEvent(log,
                                                               EventStatus.INFO,
                                                               METADATA_TARGET_CHART
                                                                   + METADATA_ACTION_EXPORT
                                                                   + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            }
                        }
                    else if (FileUtilities.png.equalsIgnoreCase(type))
                        {
                        LOGGER.debug(boolDebug,
                                     SOURCE + "writing [file "
                                     + file.getAbsolutePath()
                                     + "] [width="
                                     + width
                                     + "] [height"
                                     + height
                                     + "]");
                        ChartUtilities.saveChartAsPNG(file, chartui.getChartPanel().getChart(), width, height);
                        boolSuccess = true;

                        if (verbose)
                            {
                            SimpleEventLogUIComponent.logEvent(log,
                                                               EventStatus.INFO,
                                                               METADATA_TARGET_CHART
                                                                   + METADATA_ACTION_EXPORT
                                                                   + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR,
                                                               SOURCE,
                                                               clock);
                            }
                        }
                    else
                        {
                        SimpleEventLogUIComponent.logEvent(log,
                                                           EventStatus.FATAL,
                                                           METADATA_TARGET_CHART
                                                               + METADATA_ACTION_EXPORT
                                                               + METADATA_RESULT + ERROR_INVALID_FORMAT + TERMINATOR,
                                                           SOURCE,
                                                           clock);
                        }
                    }
                }

            catch (SecurityException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (FileNotFoundException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_CHART
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
//            System.out.println("FILENAME=" + ((fullfilename != null) && (!EMPTY_STRING.equals(fullfilename))));
//            System.out.println("TYPE=" + ((type != null) && (!EMPTY_STRING.equals(type))));
//            System.out.println("CHART=" + (chart != null));
//            System.out.println("LOG=" + (log != null));
//            System.out.println("CLOCK=" + (clock != null));
            SimpleEventLogUIComponent.logEvent(log,
                                               EventStatus.FATAL,
                                               METADATA_TARGET_CHART
                                                   + METADATA_ACTION_EXPORT
                                                   + METADATA_RESULT + ERROR_CHART + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportComponent().
     *
     * @param exportable
     * @param filename
     * @param timestamp
     * @param type
     * @param width
     * @param height
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public static boolean exportComponent(final ExportableComponentInterface exportable,
                                          final String filename,
                                          final boolean timestamp,
                                          final String type,
                                          final int width,
                                          final int height,
                                          final Vector<Vector> log,
                                          final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataExporter.exportComponent()";
        boolean boolSuccess;

//        String[] arrayNames = ImageIO.getWriterFormatNames();
//
//        for (int i = 0;
//             i < arrayNames.length;
//             i++)
//            {
//            String arrayName = arrayNames[i];
//            System.out.println("DataExporter.exportComponent() FORMAT NAME=" + arrayName);
//            }
//
        boolSuccess = false;

        if ((exportable != null)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename))
            && (type != null)
            && (!EMPTY_STRING.equals(type))
            && (log != null)
            && (clock != null)
            && (Arrays.asList(ImageIO.getWriterFormatNames()).contains(type)))
            {
            try
                {
                final File file;
                final int intRealWidth;
                final int intRealHeight;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, type));
                FileUtilities.overwriteFile(file);

                intRealWidth = width;
                intRealHeight = height;

                // Support all current formats
                if ((FileUtilities.png.equalsIgnoreCase(type))
                    || (FileUtilities.jpg.equalsIgnoreCase(type))
                    || (FileUtilities.gif.equalsIgnoreCase(type)))
                    {
                    BufferedImage buffer;
                    final Graphics2D graphics2D;

                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "DataExporter.exportComponent() writing [file "
                                            + file.getAbsolutePath()
                                            + "] [width="
                                            + intRealWidth
                                            + "] [height="
                                            + intRealHeight
                                            + "]");

                    buffer = new BufferedImage(intRealWidth,
                                               intRealHeight,
                                               BufferedImage.TYPE_INT_RGB);

                    // Create a graphics context on the buffered image
                    graphics2D = buffer.createGraphics();

                    // Draw on the image
                    graphics2D.clearRect(0, 0, intRealWidth, intRealHeight);
                    exportable.paintForExport(graphics2D, intRealWidth, intRealHeight);

                    // Export the image
                    ImageIO.write(buffer, type, file);
                    graphics2D.dispose();
                    boolSuccess = true;

                    SimpleEventLogUIComponent.logEvent(log,
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_COMPONENT
                                                           + METADATA_ACTION_EXPORT
                                                           + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    // Help the GC?
                    buffer = null;

                    ObservatoryInstrumentHelper.runGarbageCollector();
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(log,
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET_COMPONENT
                                                           + METADATA_ACTION_EXPORT
                                                           + METADATA_RESULT + ERROR_INVALID_FORMAT + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    }
                }

            catch (IllegalArgumentException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_COMPONENT
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + EXCEPTION_PARAMETER_INVALID + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (SecurityException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_COMPONENT
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (FileNotFoundException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_COMPONENT
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_COMPONENT
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(log,
                                               EventStatus.FATAL,
                                               METADATA_TARGET_COMPONENT
                                                   + METADATA_ACTION_EXPORT
                                                   + METADATA_RESULT + ERROR_COMPONENT + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportInstrumentXML().
     * Saves the current Instrument XML at the specified location.
     *
     * @param filename
     * @param timestamp
     * @param instrument
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public static boolean exportInstrumentXML(final String filename,
                                              final boolean timestamp,
                                              final Instrument instrument,
                                              final Vector<Vector> log,
                                              final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataExporter.exportInstrumentXML()";
        boolean boolSuccess;

        boolSuccess = false;

        if ((filename != null)
            && (!EMPTY_STRING.equals(filename))
            && (instrument != null)
            && (log != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, DataFormat.XML));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                instrument.save(outputStream, getXmlOptions());

                boolSuccess = true;

                // Tidy up
                outputStream.flush();
                outputStream.close();

                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.INFO,
                                                   METADATA_TARGET_XML
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (SecurityException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_XML
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (FileNotFoundException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_XML
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_XML
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(log,
                                               EventStatus.FATAL,
                                               METADATA_TARGET_XML
                                                   + METADATA_ACTION_EXPORT
                                                   + METADATA_RESULT + ERROR_XML + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportStringBuffer().
     * Saves the StringBuffer at the specified location.
     *
     * @param filename
     * @param timestamp
     * @param format
     * @param buffer
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public static boolean exportStringBuffer(final String filename,
                                             final boolean timestamp,
                                             final DataFormat format,
                                             final StringBuffer buffer,
                                             final Vector<Vector> log,
                                             final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataExporter.exportStringBuffer()";
        boolean boolSuccess;

        boolSuccess = false;

        if ((filename != null)
            && (!EMPTY_STRING.equals(filename))
            && (format != null)
            && (buffer != null)
            && (buffer.length() > 0)
            && (log != null)
            && (clock != null))
            {
            try
                {
                final File file;
                final OutputStream outputStream;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, format));
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                outputStream.write(buffer.toString().getBytes());

                boolSuccess = true;

                // Tidy up
                outputStream.flush();
                outputStream.close();

                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.INFO,
                                                   METADATA_TARGET + format.getFileExtension() + TERMINATOR
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (SecurityException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET + format.getFileExtension() + TERMINATOR
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (FileNotFoundException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET + format.getFileExtension() + TERMINATOR
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET + format.getFileExtension() + TERMINATOR
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(log,
                                               EventStatus.FATAL,
                                               METADATA_TARGET + format.getFileExtension() + TERMINATOR
                                                   + METADATA_ACTION_EXPORT
                                                   + METADATA_RESULT + ERROR_XML + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportImage().
     *
     * @param image
     * @param filename
     * @param timestamp
     * @param type
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public static boolean exportImage(final Image image,
                                      final String filename,
                                      final boolean timestamp,
                                      final String type,
                                      final Vector<Vector> log,
                                      final ObservatoryClockInterface clock)
        {
        final String SOURCE = "DataExporter.exportImage()";
        boolean boolSuccess;

        boolSuccess = false;

        if ((image != null)
            && (image instanceof RenderedImage)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename))
            && (type != null)
            && (!EMPTY_STRING.equals(type))
            && (log != null)
            && (clock != null)
            && (Arrays.asList(ImageIO.getWriterFormatNames()).contains(type)))
            {
            try
                {
                final File file;

                file = new File(FileUtilities.buildFullFilename(filename, timestamp, type));
                FileUtilities.overwriteFile(file);

                // Support all current formats
                if ((FileUtilities.png.equalsIgnoreCase(type))
                    || (FileUtilities.jpg.equalsIgnoreCase(type))
                    || (FileUtilities.gif.equalsIgnoreCase(type)))
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "DataExporter.exportImage() writing [file "
                                            + file.getAbsolutePath() + "]");
                    // Export the image
                    ImageIO.write((RenderedImage)image, type, file);
                    boolSuccess = true;

                    SimpleEventLogUIComponent.logEvent(log,
                                                       EventStatus.INFO,
                                                       METADATA_TARGET_IMAGE
                                                           + METADATA_ACTION_EXPORT
                                                           + METADATA_FILENAME + file.getAbsolutePath() + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    }
                else
                    {
                    SimpleEventLogUIComponent.logEvent(log,
                                                       EventStatus.FATAL,
                                                       METADATA_TARGET_IMAGE
                                                           + METADATA_ACTION_EXPORT
                                                           + METADATA_RESULT + ERROR_INVALID_FORMAT + TERMINATOR,
                                                       SOURCE,
                                                       clock);
                    }
                }

            catch (SecurityException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_IMAGE
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_ACCESS_DENIED + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (FileNotFoundException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_IMAGE
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_NOT_FOUND + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }

            catch (IOException exception)
                {
                SimpleEventLogUIComponent.logEvent(log,
                                                   EventStatus.FATAL,
                                                   METADATA_TARGET_IMAGE
                                                       + METADATA_ACTION_EXPORT
                                                       + METADATA_RESULT + ERROR_FILE_SAVE + TERMINATOR + SPACE
                                                       + METADATA_EXCEPTION + exception.getMessage() + TERMINATOR,
                                                   SOURCE,
                                                   clock);
                }
            }
        else
            {
            SimpleEventLogUIComponent.logEvent(log,
                                               EventStatus.FATAL,
                                               METADATA_TARGET_IMAGE
                                                   + METADATA_ACTION_EXPORT
                                                   + METADATA_RESULT + ERROR_IMAGE + TERMINATOR,
                                               SOURCE,
                                               clock);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Get the XmlOptions to use for the export.
     *
     * @return XmlOptions
     */

    private static XmlOptions getXmlOptions()
        {
        final XmlOptions xmlOptions;

        xmlOptions = new XmlOptions();
        xmlOptions.setUseDefaultNamespace();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSavePrettyPrint();
        xmlOptions.setSavePrettyPrintIndent(XML_INDENT);
        xmlOptions.setSaveOuter();

        return (xmlOptions);
        }
    }
