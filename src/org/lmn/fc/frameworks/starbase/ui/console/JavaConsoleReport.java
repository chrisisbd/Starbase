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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  26-10-04    LMN created file from SqlTraceReport
//  27-10-04    LMN made JavaConsoleReport into a StreamObserver
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.ui.console;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.streams.ObservableOutputStream;
import org.lmn.fc.common.utilities.streams.StreamObserver;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose JavaConsoleReport.
 *
 * ToDo make a Vector of observed streams, add remove etc
 */

public class JavaConsoleReport extends ReportTable
                               implements JavaConsoleReportInterface,
                                          StreamObserver
    {
    // String Resources
    private static final String REPORT_NAME             = "JavaConsole";

    private static final String TITLE_DATE              = "Date";
    private static final String TITLE_TIME              = "Time";
    private static final String TITLE_STREAM_OUTPUT     = "Stream Output";
    private static final String TITLE_STREAM            = "Stream";

    private String ACTION_CLEAR;                // The text for the ClearConsole Action

    private static final int REPORT_COLUMNS = 4;

    private Vector<Vector> vecConsoleOutput;
    private int intBufferSize;

    private ObservableOutputStream errorDevice;
    private ObservableOutputStream outputDevice;

    private final ByteArrayOutputStream arrayErrorDevice;
    private final ByteArrayOutputStream arrayOutputDevice;

    private PrintStream streamConsoleError;
    private PrintStream streamConsoleOutput;

    private PrintStream streamDefaultError;
    private PrintStream streamDefaultOutput;

    private boolean boolOutputInstalled;
    private boolean boolErrorInstalled;


    /***********************************************************************************************
     * Construct a JavaConsoleReport JTable for the specified Task.
     *
     * @param task
     * @param resourcekey
     *
     * @throws ReportException
     */

    public JavaConsoleReport(final TaskPlugin task,
                             final String resourcekey) throws ReportException
        {
        super(task,
              REPORT_NAME,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        // Set a default for the text buffer size
        intBufferSize = BUFFER_DEFAULT;

        vecConsoleOutput = new Vector<Vector>(intBufferSize);

        // Save the default streams
        streamDefaultOutput = System.out;
        streamDefaultError = System.err;

        // Create new output devices
        arrayOutputDevice = new ByteArrayOutputStream();
        arrayErrorDevice = new ByteArrayOutputStream();

        boolOutputInstalled = false;
        boolErrorInstalled = false;
        }


    /***********************************************************************************************
     * Run the UI of this Report.
     */

    public void runUI()
        {
        final URL imageURL;

        imageURL = getClass().getResource(ACTION_ICON_CONSOLE_CLEAR);

        if (imageURL != null)
            {
            final ContextAction clearContextAction;

            clearContextAction = new ContextAction(ACTION_CLEAR,
                                                   new ImageIcon(imageURL),
                                                   ACTION_CLEAR,
                                                   KeyEvent.VK_C,
                                                   true,
                                                   true)
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    LOGGER.debugNavigation("ContextAction actionPerformed() in JavaConsole");
                    readResources();
                    clearConsole();
                    refreshTable();
                    }
                };

            // This Log is used in a Runnable Task,
            // so start a new ContextActionGroup for this UIComponent,
            // and add the new Action
            final ContextActionGroup group = new ContextActionGroup(getReportUniqueName(), true, true);
            group.addContextAction(clearContextAction);

            // Add the new Group to the list of Groups for this UIComponent
            clearUIComponentContextActionGroups();
            addUIComponentContextActionGroup(group);
            }

        super.runUI();
        }


    /***********************************************************************************************
     * Generate a dummy header.
     *
     * @return Vector<String>
     */

    public final Vector<String> generateHeader()
        {
        return (new Vector<String>(1));
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(REPORT_COLUMNS);
        vecColumns.add(new ReportColumnMetadata( TITLE_DATE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_TIME,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_STREAM_OUTPUT,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata( TITLE_STREAM,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));

        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final Object [] columnWidths =
            {
            "2004-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        return (getConsoleOutput());
        }


    /***********************************************************************************************
     * Refresh the JavaConsoleReport data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * This StreamObserver is notified whenever the ObservableOutputStream is written to.
     * Blank lines are removed to save on buffer space...
     * WARNING! Do not call any debug messages in this method! --> stack overflow...
     */

    public final void streamChanged()
        {
        Vector<Object> vecRow;

        if (getConsoleOutput() != null)
            {
            // Handle each stream in turn...
            if (outputDevice != null)
                {
                if ((outputDevice.toString() != null)
                    && (!EMPTY_STRING.equals(outputDevice.toString().trim())))
                    {
                    vecRow = new Vector<Object>(REPORT_COLUMNS);
                    vecRow.add(ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
                    vecRow.add(ChronosHelper.toTimeString(Chronos.getCalendarTimeNow()));
                    vecRow.add(new String(outputDevice.toString()));
                    vecRow.add(outputDevice.getStreamName());
                    getConsoleOutput().add(vecRow);

                    // Update the display
                    refreshTable();

                    // Scroll the table so that the last output is visible
                    //scrollRowToVisible(getConsoleOutput().size()-1);
                    }

                try
                    {
                    // Copy the data from the ObservableOutputStream to the OutputDevice
                    outputDevice.writeTo(arrayOutputDevice);
                    }

                catch (IOException exception)
                    {
                    getTask().handleException(exception,
                                              EXCEPTION_WRITE_STREAM,
                                              EventStatus.WARNING);
                    }

                // All currently accumulated output in the Output ObservableOutputStream is discarded
                outputDevice.reset();
                }

            if (errorDevice != null)
                {
                // Flush the Error stream and check its error state
                if ((!streamConsoleError.checkError())
                    && (errorDevice.toString() != null)
                    && (!errorDevice.toString().trim().equals("")))
                    {
                    vecRow = new Vector<Object>(REPORT_COLUMNS);
                    vecRow.add(ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
                    vecRow.add(ChronosHelper.toTimeString(Chronos.getCalendarTimeNow()));
                    vecRow.add(new String(errorDevice.toString()));
                    vecRow.add(errorDevice.getStreamName());
                    getConsoleOutput().add(vecRow);

                    // Update the display
                    refreshTable();

                    // Scroll the table so that the last output is visible
                    // scrollRowToVisible(getConsoleOutput().size()-1);
                    }

                try
                    {
                    // Copy the data from the ObservableOutputStream to the ErrorDevice
                    errorDevice.writeTo(arrayErrorDevice);
                    }

                catch (IOException exception)
                    {
                    getTask().handleException(exception,
                                              EXCEPTION_WRITE_STREAM,
                                              EventStatus.WARNING);
                    }

                // All currently accumulated output in the Error ObservableOutputStream is discarded
                errorDevice.reset();
                }

            // Prune the output buffer
            while ((getConsoleOutput().size() > getBufferSize())
                && (!getConsoleOutput().isEmpty()))
                {
                getConsoleOutput().removeElementAt(0);
                }
            }
        }


    /***********************************************************************************************
     * Save the System.out and System.err PrintStreams for restoration when JavaConsole is shut down.
     *
     * @param out
     * @param err
     */

    public final void saveStreams(final PrintStream out,
                                  final PrintStream err)
        {
        this.streamDefaultOutput = out;
        this.streamDefaultError = err;
        }


    /***********************************************************************************************
     * Clears the Console.
     */

    public final void clearConsole()
        {
        LOGGER.debugNavigation("JavaConsoleReport.clearConsole()");

        if ((boolOutputInstalled)
            && (outputDevice != null))
            {
            try
                {
                // Copy the data from the ObservableOutputStream to the OutputDevice
                outputDevice.writeTo(arrayOutputDevice);
                }

            catch (IOException exception)
                {
                getTask().handleException(exception,
                                          EXCEPTION_WRITE_STREAM,
                                          EventStatus.WARNING);
                }

            // All currently accumulated output in the Output ObservableOutputStream is discarded
            outputDevice.reset();
            }

        if ((boolErrorInstalled)
            && (errorDevice != null))
            {
            try
                {
                // Copy the data from the ObservableOutputStream to the OutputDevice
                errorDevice.writeTo(arrayErrorDevice);
                }

            catch (IOException exception)
                {
                getTask().handleException(exception,
                                          EXCEPTION_WRITE_STREAM,
                                          EventStatus.WARNING);
                }

            // All currently accumulated output in the Error ObservableOutputStream is discarded
            errorDevice.reset();
            }

        if (getConsoleOutput() != null)
            {
            getConsoleOutput().clear();
            setConsoleOutput(new Vector<Vector>(intBufferSize));
            }
        }


    /***********************************************************************************************
     * Sets the output device to the Console if not set already.
     */

    public final void setOutputDevice()
        {
        LOGGER.debugNavigation("JavaConsoleReport.setOutputDevice()");

        // Create a new Output ObservableOutputStream
        outputDevice = new ObservableOutputStream(ObservableOutputStream.STREAM_SYSTEM_OUT);
        outputDevice.addStreamObserver(this);

        // The output stream to which values and objects will be printed
        // Set true to flush on writes
        streamConsoleOutput = new PrintStream(outputDevice, true);

        // Make System.out use this stream
        System.setOut(streamConsoleOutput);
        boolOutputInstalled = true;
        }


    /***********************************************************************************************
     * Resets the output device to the default.
     * JavaConsole will no longer receive data directed to the output stream.
     */

    public final void resetOutputDevice()
        {
        System.setOut(streamDefaultOutput);

        if (streamConsoleOutput != null)
            {
            streamConsoleOutput.close();
            }

        if (outputDevice != null)
            {
            outputDevice.removeStreamObserver(this);
            }

        boolOutputInstalled = false;
        }


    /***********************************************************************************************
     * Sets the error device to the JavaConsole if not set already.
     */

    public final void setErrorDevice()
        {
        LOGGER.debugNavigation("JavaConsoleReport.setErrorDevice()");

        // Create a new Error ObservableOutputStream
        errorDevice = new ObservableOutputStream(ObservableOutputStream.STREAM_SYSTEM_ERR);
        errorDevice.addStreamObserver(this);

        // The output stream to which values and objects will be printed
        // Set true to flush on writes
        streamConsoleError = new PrintStream(errorDevice, true);

        // Make System.err use this stream
        System.setErr(streamConsoleError);
        boolErrorInstalled = true;
        }


    /***********************************************************************************************
     * Resets the error device to the default.
     * JavaConsole will no longer receive data directed to the error stream.
     */

    public final void resetErrorDevice()
        {
        System.setErr(streamDefaultError);

        if (streamConsoleError != null)
            {
            streamConsoleError.close();
            }

        if (errorDevice != null)
            {
            errorDevice.removeStreamObserver(this);
            }

        boolErrorInstalled = false;
        }


    /***********************************************************************************************
     * Get the size of the JavaConsole buffer.
     *
     * @return int
     */

    public final int getBufferSize()
        {
        return (this.intBufferSize);
        }


    /***********************************************************************************************
     * Set the size of the JavaConsole buffer.
     *
     * @param size
     */

    public final void setBufferSize(final int size)
        {
        if ((size > BUFFER_MIN)
            && (size < BUFFER_MAX))
            {
            intBufferSize = size;
            }
        else
            {
            intBufferSize = BUFFER_DEFAULT;
            }
        }


    /***********************************************************************************************
     *
     * @return Vector
     */

    public final Vector<Vector> getConsoleOutput()
        {
        return (this.vecConsoleOutput);
        }


    /***********************************************************************************************
     * Set the JavaConsole output vector.
     *
     * @param output
     */

    public final void setConsoleOutput(final Vector<Vector> output)
        {
        this.vecConsoleOutput = output;
        }


    /***********************************************************************************************
     * Read all the Resources required by the JavaConsoleReport.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public void readResources()
        {
        super.readResources();

        // getResourceKey() returns <Framework>.JavaConsole.
        LOGGER.debugNavigation("JavaConsoleReport.readResources() [ResourceKey=" + getResourceKey() + "]");
        LOGGER.debugNavigation("JavaConsoleReport host Task ResourceKey=" + getTask().getResourceKey());
        ACTION_CLEAR = REGISTRY.getString(getTask().getResourceKey() + KEY_ACTION_CLEAR_CONSOLE);

        LOGGER.debugNavigation("JavaConsoleReport.readResources() END");
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
