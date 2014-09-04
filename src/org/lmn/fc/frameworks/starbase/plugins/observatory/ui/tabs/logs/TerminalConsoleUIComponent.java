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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.terminal.SerialPortData;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.terminal.dao.TransmitThread;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.TerminalConsoleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;


/***************************************************************************************************
 * A general purpose TerminalConsoleUIComponent.
 */

public final class TerminalConsoleUIComponent extends ReportTable
                                              implements ReportTablePlugin,
                                                         TerminalConsoleUIComponentInterface,
                                                         SerialPortEventListener
    {
    // String Resources
    private static final String REPORT_NAME                 = "Terminal";
    private static final String VERSION                     = "v1.0";

    private static final String TITLE_LINE                  = "Line";
    private static final String TITLE_DATE                  = "Date";
    private static final String TITLE_TIME                  = "Time";
    private static final String TITLE_STREAM_OUTPUT         = "Terminal Session";
    private static final String TITLE_STREAM                = "Port";

    private static final String MSG_NO_CONNECTION           = "No connection";
    private static final String MSG_OPENED                  = "opened successfully";
    private static final String TOOLTIP_LOST_FOCUS          = "Click here to start typing";
    private static final String TOOLTIP_STOPPED             = "Start the Instrument before typing";

    private static final String ACTION_CLEAR                = "Clear the Terminal Emulator";

    private static final Color COLOR_LOST_FOCUS = Color.GRAY;

    private static final int INDEX_LINE = 0;
    private static final int INDEX_DATE = 1;
    private static final int INDEX_TIME = 2;
    private static final int INDEX_SESSION = 3;
    private static final int INDEX_PORT = 4;

    private static final int REPORT_COLUMNS = 5;
    private static final int BUFFER_SIZE_MAX = 50000;
    private static final int BUFFER_SIZE_MIN = 10;
    private static final int CAPACITY_INPUT_STREAM = 10000;
    private static final int CAPACITY_OUTPUT_QUEUE = 100;

    private static final char CHAR_CR = '\r';
    private static final char CHAR_CONTROL = '^';
    private static final char CHAR_TILDE = '~';
    private static final char CHAR_QUERY = '?';
    private static final int ASCII_BASE = 64;
    private static final int DEFAULT_LINE_WRAP_COUNT = 60;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final String strPropertyResourceKey;

    private final TerminalConsoleUIComponentInterface terminalEmulator;
    private TransmitThread threadTx;
    private Vector<Vector> vecConsole;
    private ArrayBlockingQueue<Byte> outputQueue;
    private SerialPort serialPort;
    private long longLineCounter;
    private boolean boolUserWantsFocus;
    private boolean boolStarted;

    // Properties
    private FontInterface pluginFont;
    private ColourInterface colText;
    private ColourInterface colTable;
    private ColourInterface colCanvas;
    private SerialPortData serialportData;
    private int intLineWrapCount;


    /***********************************************************************************************
     * A debug utility to get the (safe) classname of a Component.
     *
     * @param component
     *
     * @return String
     */

    private static String getComponentClassName(final Component component)
        {
        if (component != null)
            {
            return (component.getClass().getName());
            }
        else
            {
            return ("NULL");
            }
        }


    /***********************************************************************************************
     * Move the cursor back one position, deleting the previous character.
     *
     * @param console
     */

    private static void backSpace(final Vector<Vector> console)
        {
        final Vector vecCursorRow;
        final StringBuffer bufferSession;

        if ((console != null)
            && (console.size() > 0))
            {
            // Retrieve the last row (i.e. the 'cursor row')
            vecCursorRow = console.get(console.size()-1);

            if (vecCursorRow != null)
                {
                // Retrieve the Session element of the cursor row
                bufferSession = (StringBuffer)vecCursorRow.get(INDEX_SESSION);

                // Do nothing if the last row contains only a cursor (i.e. length=1)
                if ((bufferSession != null)
                    && (bufferSession.length() > 1))
                    {
                    // Remove the cursor from the last row
                    bufferSession.deleteCharAt(bufferSession.length()-1);

                    // ...and the previous character, if any
                    if (bufferSession.length() > 0)
                        {
                        bufferSession.deleteCharAt(bufferSession.length()-1);
                        }

                    // Replace the cursor
                    bufferSession.append(CURSOR);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Trim the console by one line if it has exceeded the buffer size.
     * This must be followed by a call to refresh.
     *
     * @param console
     * @param portdata
     */

    private static void trimConsole(final Vector<Vector> console,
                                    final SerialPortData portdata)
        {
        if ((console != null)
            && (portdata != null))
            {
            // Prune the output buffer
            while ((console.size() > (portdata.getBufferSize()-1))
                && (!console.isEmpty()))
                {
                // Remove the very first line...
                console.removeElementAt(0);
                }
            }
        }


    /***********************************************************************************************
     * Construct a TerminalConsoleUIComponent JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param reportresourcekey
     * @param propertyresourcekey
     */

    public TerminalConsoleUIComponent(final TaskPlugin task,
                                      final ObservatoryInstrumentInterface hostinstrument,
                                      final String reportresourcekey,
                                      final String propertyresourcekey)
        {
        super(task,
              REPORT_NAME,
              reportresourcekey,
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

        if ((task == null)
            || (!task.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        this.hostInstrument = hostinstrument;
        this.strPropertyResourceKey = propertyresourcekey;

        // Save a reference to this for use in inner classes
        terminalEmulator = this;
        threadTx = null;

        vecConsole = new Vector<Vector>(1000);
        outputQueue = new ArrayBlockingQueue<Byte>(CAPACITY_OUTPUT_QUEUE, true);
        longLineCounter = 0;
        boolUserWantsFocus = false;
        boolStarted = false;

        // Properties
        this.serialportData = new SerialPortData();
        this. intLineWrapCount = DEFAULT_LINE_WRAP_COUNT;
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Initialise the UI of this UIComponent.
     */

    public synchronized void initialiseUI()
        {
        // This will read the Resources
        super.initialiseUI();

        // Start with a clear screen!
        clearScreen();

        // Override the ReportTable defaults with the TerminalEmulator specifics...
        refreshStyle();

        // Initialise the Console Listeners
        initialiseListeners(getOutputQueue());

        // Tell the User which port is in use
        if ((getHostInstrument().getDAO() != null)
            && (getHostInstrument().getDAO().getInstrumentMetadata() != null))
            {
            MetadataHelper.addNewMetadata(getHostInstrument().getDAO().getInstrumentMetadata(),
                                          MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                          getSerialPortData().getPortName(),
                                          EMPTY_STRING,
                                          DataTypeDictionary.STRING,
                                          SchemaUnits.DIMENSIONLESS,
                                          "The Port used by the Terminal Emulator");
            }
        }


    /***********************************************************************************************
     * Run the UI of this Report.
     */

    public synchronized void runUI()
        {
        final URL imageURL;

        imageURL = getClass().getResource(ACTION_ICON_CLEAR_SCREEN);

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
                    readResources();
                    clearScreen();
                    refreshStyle();
                    refreshTable();
                    }
                };

            // This Log is used in a Runnable Task,
            // so start a new ContextActionGroup for this UIComponent,
            // and add the new Action
            final ContextActionGroup group;

            group = new ContextActionGroup(getReportUniqueName(), true, true);
            group.addContextAction(clearContextAction);

            // Add the new Group to the list of Groups for this UIComponent
            clearUIComponentContextActionGroups();
            addUIComponentContextActionGroup(group);
            }

        // This resets the colours...
        super.runUI();

        // Override the ReportTable defaults with the TerminalEmulator specifics...
        refreshStyle();
        refreshTable();

        showConsoleHasFocus(getReportTable(),
                            getScrollPane(),
                            false,
                            isStarted());
        }


    /***********************************************************************************************
     * Stop the UI of this ReportTable.
     */

    public synchronized final void stopUI()
        {
        // Stay connected
        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized final void disposeUI()
        {
        // Close the serial port if it is still open
        if (getSerialPort() != null)
            {
            try
                {
                getSerialPort().removeEventListener();
                getSerialPort().getInputStream().close();
                getSerialPort().getOutputStream().close();
                }

            catch (IOException exception)
                {
                // There isn't much we can do...
                }

            finally
                {
                getSerialPort().close();
                }
            }

        // Dispose of the Report
        super.disposeUI();
        }


    /***********************************************************************************************
     * Initialise the Console Focus Listeners.
     * The user must click in the console window in order to proceed.
     * Write characters typed by the User to the specified ArrayBlockingQueue.
     *
     * @param queue
     */

    private void initialiseListeners(final ArrayBlockingQueue<Byte> queue)
        {
        final FocusListener listenerFocus;
        final MouseListener listenerMouse;
        final KeyListener listenerKey;

        //------------------------------------------------------------------------------------------
        // Used on the ReportTable, ScrollPane and TerminalConsoleUIComponent

        listenerFocus = new FocusAdapter()
            {
            public void focusGained(final FocusEvent event)
                {
                // We can capture keyboard input only if the TerminalEmulator has the focus
                // The JTable may initially get the focus,
                // but it will be transferred to the Report
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "TerminalConsoleUIComponent focus gained component="
                                        + getComponentClassName(event.getComponent())
                                        + " lost component="
                                        + getComponentClassName(event.getOppositeComponent()));
                showConsoleHasFocus(getReportTable(),
                                    getScrollPane(),
                                    terminalEmulator.equals(event.getComponent()),
                                    isStarted());
                }

            public void focusLost(final FocusEvent event)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "TerminalConsoleUIComponent focus lost component="
                                        + getComponentClassName(event.getComponent())
                                        + " gained component="
                                        + getComponentClassName(event.getOppositeComponent()));
                showConsoleHasFocus(getReportTable(),
                                    getScrollPane(),
                                    false,
                                    isStarted());
                }
            };

        //------------------------------------------------------------------------------------------
        // Used on the ReportTable and ScrollPane

        listenerMouse = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent event)
                {
                // The user must click in the console window in order to proceed
                setUserWantsFocus(requestFocusInWindow());
                showConsoleHasFocus(getReportTable(),
                                    getScrollPane(),
                                    getUserWantsFocus(),
                                    isStarted());
                }
            };

        //------------------------------------------------------------------------------------------
        // Used only on the TerminalConsoleUIComponent

        listenerKey = new KeyAdapter()
            {
            // Key events are dispatched to the focus owner by default
            // The getKeyChar method always returns a valid Unicode character or CHAR_UNDEFINED.
            // Character input is reported by KEY_TYPED events:
            // KEY_PRESSED and KEY_RELEASED events are not necessarily associated with character input.
            // Therefore, the result of the getKeyChar method is guaranteed to be meaningful
            // only for KEY_TYPED events.

            public synchronized void keyTyped(final KeyEvent event)
                {
                if (getUserWantsFocus())
                    {
                    final char charKey;

                    // This seems to be incapable of generating a CR!
                    // CR (^M) is mapped to LF (^J) internally
                    charKey = event.getKeyChar();

                    if (charKey != KeyEvent.CHAR_UNDEFINED)
                        {
                        try
                            {
                            // Update the console display
                            // Only ACSII 20-7E are sent to the console display directly
                            // Control characters are shown as such
                            // LF, BS are interpreted
                            if (getSerialPortData().isLocalEcho())
                                {
                                processTxChar(getConsoleDisplay(),
                                              getSerialPortData(),
                                              charKey);
                                refreshStyle();
                                refreshTable();
                                }

                            // Copy the character to the queue,
                            // to be transferred to the serial port output stream on another Thread
                            if (queue != null)
                                {
                                // This is a real bodge!
                                // Intercept LF and generate a CF/LF pair
                                // since CR can never be generated by Java (weird)
                                if (charKey == (char)KeyEvent.VK_ENTER)
                                    {
                                    queue.put((byte) 0x0d);
                                    queue.put((byte) KeyEvent.VK_ENTER);
                                    }
                                else
                                    {
                                    queue.put((byte) charKey);
                                    }
                                }
                            }

                        catch (InterruptedException exception)
                            {
                            Thread.currentThread().interrupt();
                            getTask().handleException(exception,
                                                      "initialiseListeners()",
                                                      EventStatus.WARNING);
                            }
                        }
                    }
                }
            };

        // We must wait for the ReportTable to get focus when the user clicks
        // on the console window, so add a MouseListener to the Report *and* the ScrollPane...
        if (getReportTable() != null)
            {
            getReportTable().setFocusable(true);
            getReportTable().addFocusListener(listenerFocus);
            getReportTable().addMouseListener(listenerMouse);

            getScrollPane().setFocusable(true);
            getScrollPane().addFocusListener(listenerFocus);
            getScrollPane().addMouseListener(listenerMouse);
            }

        // ... focus will then be transferred to this TerminalConsoleUIComponent,
        // which must capture the user's typed characters, so add a KeyListener
        setFocusable(true);
        addFocusListener(listenerFocus);
        addKeyListener(listenerKey);
        }


    /***********************************************************************************************
     * Start the TerminalEmulator session.
     */

    public boolean startSession()
        {
        this.boolStarted = false;

        // Show a welcome message and the port configuration the first time through
        logOn(getConsoleDisplay(), getSerialPortData());

        // Initialise the serial port connection and Listeners etc.
        // This is done last of all so that initialisation messages may appear on the console
        setSerialPort(connectToSerialPort(getSerialPortData()));

        // Set up to receive events, errors etc.
        if (getSerialPort() != null)
            {
            try
                {
                // Activate the SerialPortEventListener!
                // Note that we can only have *one* Listener...
                getSerialPort().addEventListener(this);

                // Start the Transmit Thread
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "TerminalConsoleUIComponent Start the Transmit Thread");

                // Stop any existing Transmitter Thread
                if (threadTx != null)
                    {
                    threadTx.setRunning(false);
                    }

                // Prepare Thread to transmit
                prepareNewThreads(getConsoleDisplay(), getSerialPortData());

                if (threadTx != null)
                    {
                    threadTx.setRunning(true);
                    threadTx.start();
                    }

                // This will fire serialEvent() when data are waiting
                getSerialPort().notifyOnDataAvailable(true);

                this.boolStarted = true;
                }

            catch (TooManyListenersException exception)
                {
                LOGGER.error("TerminalConsoleUIComponent.startSession() TooManyListenersException");
                }
            }
        else
            {
            // Tell the User that the serial port could not be opened
            addLine(getConsoleDisplay(),
                    getSerialPortData(),
                    "It was not possible to open the Serial Port " + getSerialPortData().getPortName());
            refreshStyle();
            refreshTable();
            }

        return (isStarted());
        }


    /***********************************************************************************************
     * Stop the TerminalEmulator session.
     */

    public boolean stopSession()
        {
        // Stop the Transmit Thread
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "TerminalConsoleUIComponent Stop the Transmit Thread");

        // Stop any existing Transmitter Thread
        if (threadTx != null)
            {
            threadTx.setRunning(false);
            }

        // Close the serial port if it is still open
        if (getSerialPort() != null)
            {
            try
                {
                getSerialPort().removeEventListener();
                getSerialPort().getInputStream().close();
                getSerialPort().getOutputStream().close();
                }

            catch (IOException exception)
                {
                // There isn't much we can do...
                }

            finally
                {
                getSerialPort().close();
                }
            }

        this.boolStarted = false;

        return (!isStarted());
        }


    /***********************************************************************************************
     * Prepare Transmit Thread.
     *
     * @param console
     * @param portdata
     */

    private void prepareNewThreads(final Vector<Vector> console,
                                   final SerialPortData portdata)
        {
        // Prepare another thread to do the Transmit buffering
        try
            {
            if ((getOutputQueue() != null)
                && (portdata != null)
                && (getSerialPort().getOutputStream() != null))
                {
                threadTx = new TransmitThread(getTask(),
                                              getOutputQueue(),
                                              getSerialPort().getOutputStream());
                }
            }

        catch (IOException exception)
            {
            getTask().handleException(exception,
                                      "prepareNewThreads()",
                                      EventStatus.WARNING);
            }
        }


    /**********************************************************************************************/
    /* Serial Port                                                                                */
    /***********************************************************************************************
     * Connect to the Serial Port using the specified SerialPortData.
     * Return null if no connection is possible.
     *
     * @param portdata
     *
     * @return SerialPort
     */

    private SerialPort connectToSerialPort(final SerialPortData portdata)
        {
        final SerialPort port;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "TerminalConsoleUIComponent.connectToSerialPort()");

        // Ensure that we get the port configuration...
        readResources();

        // First find and open the serial port
        port = StreamUtilities.findAndOpenSerialPort(portdata.getPortOwner(),
                                                     portdata.getPortName(),
                                                     portdata.getBaudrate(),
                                                     portdata.getDatabits(),
                                                     portdata.getStopbits(),
                                                     portdata.getParity(),
                                                     portdata.getFlowControl());
        return (port);
        }


    /**********************************************************************************************/
    /* ReportTable                                                                                */
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
        vecColumns.add(new ReportColumnMetadata(TITLE_LINE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DATE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STREAM_OUTPUT,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STREAM,
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
            "9999",
            "2000-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMM"
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
        return (getConsoleDisplay());
        }


    /***********************************************************************************************
     * Refresh the TerminalConsoleUIComponent table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************/
    /* Terminal Emulation Functions.                                                               */
    /***********************************************************************************************
     * Issue a Log On message.
     *
     * @param console
     * @param portdata
     */

    private synchronized void logOn(final Vector<Vector> console,
                                    final SerialPortData portdata)
        {
        if ((console != null)
            && (portdata != null))
            {
            addLine(console,
                    portdata,
                    "<html><i><font color=red>" +
                    MSG_WELCOME
                        + SPACE
                        + REGISTRY.getFramework().getName()
                        + SPACE
                        + REPORT_NAME
                            + SPACE
                        + VERSION
                            + "</font></i></html>");
            addLine(console,
                    portdata,
                    KEY_PORT_NAME + EQUALS + portdata.getPortName());
            addLine(console,
                    portdata,
                    KEY_PORT_OWNER + EQUALS + portdata.getPortOwner());
            addLine(console,
                    portdata,
                    KEY_PORT_BAUDRATE + EQUALS + portdata.getBaudrate());
            addLine(console,
                    portdata,
                    KEY_PORT_DATA_BITS + EQUALS + portdata.getDatabits());
            addLine(console,
                    portdata,
                    KEY_PORT_STOP_BITS + EQUALS + portdata.getStopbits());
            addLine(console,
                    portdata,
                    KEY_PORT_PARITY + EQUALS + portdata.getParity());
            addLine(console,
                    portdata,
                    KEY_PORT_FLOW_CONTROL + EQUALS + portdata.getFlowControl());
            addLine(console,
                    portdata,
                    KEY_ENABLE_LOCAL_ECHO + EQUALS + portdata.isLocalEcho());
            addLine(console,
                    portdata,
                    KEY_LINE_WRAP_COUNT + EQUALS + intLineWrapCount);
            addLine(console,
                    portdata,
                    KEY_BUFFER_SIZE + EQUALS + portdata.getBufferSize());

            // Move the cursor to a new blank line ready for user input
            addLine(console, portdata, CURSOR);
            refreshStyle();
            refreshTable();
            }
        }


    /***********************************************************************************************
     * Clear the screen.
     */

    public final void clearScreen()
        {
        // Empty the Queues
        if (getOutputQueue() != null)
            {
            getOutputQueue().clear();
            }
        else
            {
            setOutputQueue(new ArrayBlockingQueue<Byte>(CAPACITY_OUTPUT_QUEUE));
            }

        if (getConsoleDisplay() != null)
            {
            getConsoleDisplay().clear();
            }
        else
            {
            setConsoleDisplay(new Vector<Vector>(CAPACITY_INPUT_STREAM));
            }

        longLineCounter = 0;
        }


    /***********************************************************************************************
     * Add one line to the console output.
     * All addLine() must be followed by a call to refresh.
     *
     * @param console
     * @param portdata
     * @param line
     */

    private void addLine(final Vector<Vector> console,
                         final SerialPortData portdata,
                         final String line)
        {
        final Vector<Object> vecRow;

        if (console != null)
            {
            vecRow = new Vector<Object>(REPORT_COLUMNS);
            vecRow.add(Long.toString(longLineCounter++));
            vecRow.add(ChronosHelper.toDateString(getHostInstrument().getObservatoryClock().getCalendarDateNow()));
            vecRow.add(ChronosHelper.toTimeString(getHostInstrument().getObservatoryClock().getCalendarTimeNow()));
            vecRow.add(new StringBuffer(line));

            if (portdata != null)
                {
                vecRow.add(portdata.getPortName());
                console.add(vecRow);
                }
            else
                {
                vecRow.add(MSG_NO_CONNECTION);
                console.add(vecRow);
                }

            trimConsole(console, portdata);
            }
        }


    /***********************************************************************************************
     * Put a single Transmitted character at the end of the last row of the console.
     * Interpret control characters appropriately.
     * Update the Date and Time to show the time of the change.
     * This must be followed by a call to refresh.
     *
     * @param console
     * @param portdata
     * @param character
     */

    private void processTxChar(final Vector<Vector> console,
                               final SerialPortData portdata,
                               final char character)
        {
        // Trap control characters
        if (character < KeyEvent.VK_SPACE)
            {
            switch (character)
                {
                case KeyEvent.VK_UNDEFINED:
                    {
                    // Do nothing for NULL
                    break;
                    }

                case KeyEvent.VK_BACK_SPACE:
                    {
                    backSpace(console);
                    break;
                    }

                case CHAR_CR:
                    {
                    // Do nothing for CR, since we know it is always followed by a LF on Tx
                    break;
                    }

                case KeyEvent.VK_ENTER:
                    {
                    // LF will scroll the console
                    newLine(console, portdata);
                    break;
                    }

                default:
                    {
                    // Just show that it was a control character
                    putCharAtCursor(console,
                                    portdata,
                                    CHAR_CONTROL);
                    putCharAtCursor(console,
                                    portdata,
                                    (char)(character + (char) ASCII_BASE));
                    }
                }
            }
        else
            {
            // Trap printable characters
            if ((character >= KeyEvent.VK_SPACE)
                && (character <= CHAR_TILDE))
                {
                // Show the input character on the console
                putCharAtCursor(console,
                                portdata,
                                character);
                }
            else
                {
                // Just show that something odd happened
                putCharAtCursor(console,
                                portdata,
                                CHAR_QUERY);
                }
            }
        }


    /***********************************************************************************************
     * Put a single Received character at the end of the last row of the console.
     * Interpret control characters appropriately.
     * Update the Date and Time to show the time of the change.
     * This must be followed by a call to refresh.
     *
     * @param console
     * @param portdata
     * @param character
     */

    private void processRxChar(final Vector<Vector> console,
                               final SerialPortData portdata,
                               final char character)
        {
        // Trap control characters
        if (character < KeyEvent.VK_SPACE)
            {
            switch (character)
                {
                case KeyEvent.VK_UNDEFINED:
                    {
                    // Do nothing for NULL
                    break;
                    }

                case KeyEvent.VK_BACK_SPACE:
                    {
                    backSpace(console);
                    break;
                    }

                case CHAR_CR:
                    {
                    // Do nothing for CR
                    // ToDo maybe work out if it is a CR/LF pair, and scroll etc.?
                    break;
                    }

                case KeyEvent.VK_ENTER:
                    {
                    // LF will scroll the console
                    newLine(console, portdata);
                    break;
                    }

                default:
                    {
                    // Just show that it was a control character
                    putCharAtCursor(console,
                                    portdata,
                                    CHAR_CONTROL);
                    putCharAtCursor(console,
                                    portdata,
                                    (char)(character + (char) ASCII_BASE));
                    }
                }
            }
        else
            {
            // Trap printable characters
            if ((character >= KeyEvent.VK_SPACE)
                && (character <= CHAR_TILDE))
                {
                // Show the input character on the console
                putCharAtCursor(console,
                                portdata,
                                character);
                }
            else
                {
                // Just show that something odd happened
                putCharAtCursor(console,
                                portdata,
                                CHAR_QUERY);
                }
            }
        }


    /***********************************************************************************************
     * Put a single character at the end of the last row of the console.
     * Update the Date and Time to show the time of the change.
     *
     * @param console
     * @param portdata
     * @param character
     */

    private synchronized void putCharAtCursor(final Vector<Vector> console,
                                              final SerialPortData portdata,
                                              final char character)
        {
        final Vector vecCursorRow;
        final StringBuffer bufferSession;

        if (console != null)
            {
            if (console.size() > 0)
                {
                // Retrieve the last row (i.e. the 'cursor row')
                vecCursorRow = console.get(console.size()-1);

                if (vecCursorRow != null)
                    {
                    // Retrieve the Session element of the cursor row
                    bufferSession = (StringBuffer)vecCursorRow.get(INDEX_SESSION);

                    if ((bufferSession != null)
                        && (bufferSession.length() > 0))
                        {
                        // Check that we can add two more characters to the buffer without wrapping
                        // If not, finish the current line and add a new line
                        if (bufferSession.length() >= (intLineWrapCount - 2))
                            {
                            addLine(console,
                                    portdata,
                                    character + CURSOR);
                            }
                        else
                            {
                            // Add the character to the end of the buffer (the 'cursor position')
                            bufferSession.setCharAt(bufferSession.length()-1, character);
                            bufferSession.append(CURSOR);

                            // Update the Date and Time to show the time of the change
                            vecCursorRow.set(INDEX_DATE, ChronosHelper.toDateString(getHostInstrument().getObservatoryClock().getCalendarDateNow()));
                            vecCursorRow.set(INDEX_TIME, ChronosHelper.toTimeString(getHostInstrument().getObservatoryClock().getCalendarTimeNow()));
                            }
                        }
                    }
                }
            else
                {
                // This is the first character to be displayed,
                // so we must make a new line
                addLine(console,
                        portdata,
                        character + CURSOR);
                }
            }
        }


    /***********************************************************************************************
     * Add a new line to the console output.
     *
     * @param console
     * @param portdata
     */

    private void newLine(final Vector<Vector> console,
                         final SerialPortData portdata)
        {
        final Vector<Object> vecRow;
        final Vector vecCursorRow;
        final StringBuffer bufferSession;

        if (console != null)
            {
            if (console.size() > 0)
                {
                // Retrieve the last row (i.e. the 'cursor row')
                vecCursorRow = console.get(console.size()-1);

                if (vecCursorRow != null)
                    {
                    // Retrieve the Session element of the cursor row
                    bufferSession = (StringBuffer)vecCursorRow.get(INDEX_SESSION);

                    if ((bufferSession != null)
                        && (bufferSession.length() > 0))
                        {
                        // Remove the cursor from the last row
                        bufferSession.deleteCharAt(bufferSession.length()-1);
                        }
                    }
                }

            // Add a new blank line containing only a cursor
            vecRow = new Vector<Object>(REPORT_COLUMNS);
            vecRow.add(Long.toString(longLineCounter++));

            vecRow.add(ChronosHelper.toDateString(getHostInstrument().getObservatoryClock().getCalendarDateNow()));
            vecRow.add(ChronosHelper.toTimeString(getHostInstrument().getObservatoryClock().getCalendarTimeNow()));
            vecRow.add(new StringBuffer(CURSOR));

            if (portdata != null)
                {
                vecRow.add(portdata.getPortName());
                }
            else
                {
                vecRow.add(MSG_NO_CONNECTION);
                }

            console.add(vecRow);
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the SerialPort.
     *
     * @return SerialPort
     */

    private SerialPort getSerialPort()
        {
        return (this.serialPort);
        }


    /***********************************************************************************************
     * Set the SerialPort.
     *
     * @param serialport
     */

    private void setSerialPort(final SerialPort serialport)
        {
        this.serialPort = serialport;
        }


    /***********************************************************************************************
     * Get the SerialPortData.
     *
     * @return SerialPortData
     */

    private synchronized SerialPortData getSerialPortData()
        {
        return (this.serialportData);
        }


    /***********************************************************************************************
     * Get the Vector which holds the console display columns.
     *
     * @return Vector
     */

    private Vector<Vector> getConsoleDisplay()
        {
        return (this.vecConsole);
        }


    /***********************************************************************************************
     * Set the Vector which holds the console display columns.
     *
     * @param display
     */

    private void setConsoleDisplay(final Vector<Vector> display)
        {
        this.vecConsole = display;
        }


    /***********************************************************************************************
     * Get the ConsoleHasFocus flag.
     *
     * @return boolean
     */

    private synchronized boolean getUserWantsFocus()
        {
        return (this.boolUserWantsFocus);
        }


    /***********************************************************************************************
     * Set the ConsoleHasFocus flag.
     *
     * @param focus
     */

    private synchronized void setUserWantsFocus(final boolean focus)
        {
        this.boolUserWantsFocus = focus;
        }


    /***********************************************************************************************
     * Indicate that the TerminalEmulator has gained or lost the focus.
     * The User can't type until the session has started.
     *
     * @param table
     * @param scrollpane
     * @param focus
     * @param started
     */

    private synchronized void showConsoleHasFocus(final JTable table,
                                                  final JScrollPane scrollpane,
                                                  final boolean focus,
                                                  final boolean started)
        {
        setUserWantsFocus(focus);
        refreshStyle();

        if ((table != null)
            && (scrollpane != null))
            {
            if (started)
                {
                if (getUserWantsFocus())
                    {
                    table.setForeground(getTextColour().getColor());
                    table.setToolTipText(EMPTY_STRING);
                    scrollpane.setToolTipText(EMPTY_STRING);
                    }
                else
                    {
                    table.setForeground(COLOR_LOST_FOCUS);
                    table.setToolTipText(TOOLTIP_LOST_FOCUS);
                    scrollpane.setToolTipText(TOOLTIP_LOST_FOCUS);
                    }
                }
            else
                {
                // The User can't type until the session has started
                table.setForeground(COLOR_LOST_FOCUS);
                table.setToolTipText(TOOLTIP_STOPPED);
                scrollpane.setToolTipText(TOOLTIP_STOPPED);
                }
            }
        }


    /***********************************************************************************************
     * Set the TerminalEmulator buffer size.
     *
     * @param size
     */

    private void setBufferSize(final int size)
        {
        if ((size >= BUFFER_SIZE_MIN)
            && (size <= BUFFER_SIZE_MAX))
            {
            getSerialPortData().setBufferSize(size);
            }
        else
            {
            if (size < BUFFER_SIZE_MIN)
                {
                getSerialPortData().setBufferSize(BUFFER_SIZE_MIN);
                }
            else
                {
                getSerialPortData().setBufferSize(BUFFER_SIZE_MAX);
                }
            }
        }


    /***********************************************************************************************
     * Get the OutputQueue containing characters typed by the User.
     *
     * @return ArrayBlockingQueue<Byte>
     */

    private ArrayBlockingQueue<Byte> getOutputQueue()
        {
        return (this.outputQueue);
        }


    /***********************************************************************************************
     * Set the OutputQueue.
     *
     * @param queue
     */

    private void setOutputQueue(final ArrayBlockingQueue<Byte> queue)
        {
        this.outputQueue = queue;
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private synchronized ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Indicate if the Terminal is in the Started state.
     *
     * @return boolean
     */

    private boolean isStarted()
        {
        return (this.boolStarted);
        }


    /***********************************************************************************************
     * Reset the colours and fonts.
     */

    private synchronized void refreshStyle()
        {
        setShowGrid(false);
        setTextColour(colText);
        setTableColour(colTable);
        setCanvasColour(colCanvas);
        setReportFont(pluginFont);

        getReportTable().setFont(getReportFont().getFont());
        getReportTable().setForeground(getTextColour().getColor());
        getReportTable().setBackground(getTableColour().getColor());
        getTablePanel().setBackground(getCanvasColour().getColor());
        }


    /***********************************************************************************************
     * Get the Property ResourceKey for the Properties to be shown on this Report.
     *
     * @return String
     */

    private String getPropertyResourceKey()
        {
        return (this.strPropertyResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the TerminalConsoleUIComponent.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public synchronized final void readResources()
        {
        // This reads a different set of colours and fonts...
        super.readResources();

        setDebug(REGISTRY.getBooleanProperty(getPropertyResourceKey() + KEY_ENABLE_DEBUG));

        colTable = (ColourInterface) REGISTRY.getProperty(getPropertyResourceKey() + KEY_COLOUR_TABLE);
        colCanvas = (ColourInterface)REGISTRY.getProperty(getPropertyResourceKey() + KEY_COLOUR_CANVAS);
        colText = (ColourInterface)REGISTRY.getProperty(getPropertyResourceKey() + KEY_COLOUR_TEXT);
        pluginFont = (FontInterface)REGISTRY.getProperty(getPropertyResourceKey() + KEY_FONT);
        intLineWrapCount = REGISTRY.getIntegerProperty(getPropertyResourceKey() + KEY_LINE_WRAP_COUNT);

        if (serialportData != null)
            {
            serialportData.setPortOwner(REGISTRY.getStringProperty(getPropertyResourceKey() + KEY_PORT_OWNER));
            serialportData.setPortName(REGISTRY.getStringProperty(getPropertyResourceKey() + KEY_PORT_NAME));
            serialportData.setBaudrate(REGISTRY.getIntegerProperty(getPropertyResourceKey() + KEY_PORT_BAUDRATE));
            serialportData.setDatabits(REGISTRY.getIntegerProperty(getPropertyResourceKey() + KEY_PORT_DATA_BITS));
            serialportData.setStopbits(REGISTRY.getIntegerProperty(getPropertyResourceKey() + KEY_PORT_STOP_BITS));
            serialportData.setParity(REGISTRY.getIntegerProperty(getPropertyResourceKey() + KEY_PORT_PARITY));
            serialportData.setFlowControl(REGISTRY.getStringProperty(getPropertyResourceKey() + KEY_PORT_FLOW_CONTROL));

            // ToDo remove these from serialportData
            serialportData.setBufferSize(REGISTRY.getIntegerProperty(getPropertyResourceKey() + KEY_BUFFER_SIZE));
            serialportData.setLocalEcho(REGISTRY.getBooleanProperty(getPropertyResourceKey() + KEY_ENABLE_LOCAL_ECHO));
            }
        }


    /***********************************************************************************************
     * This is the event handler for SerialPortEventListener.
     * The RS232GpsReceiver only receives data.
     *
     * @param event
     */

    public final void serialEvent(final SerialPortEvent event)
        {
        switch (event.getEventType())
            {
            case SerialPortEvent.DATA_AVAILABLE:
                {
                final StringBuffer buffer = new StringBuffer();

                // Data are available, so read from the underlying InputStream and place in a buffer
                try
                    {
                    // There is an attempt to read at least one byte.
                    // If no byte is available because the stream is at end of file,
                    // the value -1 is returned;
                    // otherwise, at least one byte is read and stored into the buffer
                    while ((getSerialPort() != null)
                        && (getSerialPort().getInputStream() != null)
                        && (getSerialPort().getInputStream().available() > 0))
                        {
                        final int intCharacter;

                        // There are some bytes available, so accumulate them
                        // Reads the next byte of data from the input stream.
                        // The value byte is returned as an int in the range 0 to 255.
                        // If no byte is available because the end of the stream has been reached,
                        // the value -1 is returned.
                        // This method blocks until input data is available,
                        // the end of the stream is detected, or an exception is thrown.

                        intCharacter = getSerialPort().getInputStream().read();

                        if (intCharacter >= 0)
                            {
                            buffer.append((char)intCharacter);
                            }

                        Thread.yield();
                        }

                    // Did we get any characters?
                    if (buffer.length() > 0)
                        {
                        // Update the console output...
                        for (int i = 0; i < buffer.length(); i++)
                            {
                            processRxChar(getConsoleDisplay(),
                                          getSerialPortData(),
                                          buffer.charAt(i));

                            Thread.yield();
                            }

                        // ... and the display
                        // Display updates occur on the Event Dispatching Thread
                        refreshStyle();
                        refreshTable();

                        // Incoming characters will grab the focus,
                        // which will change the text colour if we don't already have the focus
                        // so re-establish the UI setup
                        showConsoleHasFocus(getReportTable(),
                                            getScrollPane(),
                                            getUserWantsFocus(),
                                            isStarted());
                        }
                    }

                catch (IOException exception)
                    {
                    // Ignore errors normally, just keep trying
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "TerminalConsoleUIComponent.serialEvent() IOException reading data from input stream [exception=" + exception.getMessage() + "]");
                    }

                break;
                }

            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "TerminalConsoleUIComponent.serialEvent() Serial Event type [eventtype=" + event.getEventType() + "]");
                break;
                }

            default:
                {
                // Ignore errors normally, just keep trying
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "TerminalConsoleUIComponent.serialEvent() Unknown Serial Event type [eventtype=" + event.getEventType() + "]");
                break;
                }
            }
        }
    }
