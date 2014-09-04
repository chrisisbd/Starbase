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
//  13-01-05    LMN created file
//  02-02-05    LMN made it work!
//  03-02-05    LMN tidying up
//  09-08-06    LMN making major changes for the new structure
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.ui.emulator;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.terminal.SerialPortData;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
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
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;


/***************************************************************************************************
 * A general purpose TerminalConsoleUIComponent.
 */

public final class TerminalEmulatorConsole extends ReportTable
                                           implements ReportTablePlugin,
                                                      TerminalEmulatorInterface
    {
    // String Resources
    private static final String REPORT_NAME                 = "TerminalConsoleUIComponent";
    private static final String VERSION                     = "v2.0";

    private static final String TITLE_LINE                  = "Line";
    private static final String TITLE_DATE                  = "Date";
    private static final String TITLE_TIME                  = "Time";
    private static final String TITLE_STREAM_OUTPUT         = "Terminal Session";
    private static final String TITLE_STREAM                = "Port";

    private static final String MSG_NO_CONNECTION           = "No connection";
    private static final String MSG_OPENED                  = "opened successfully";
    private static final String TOOLTIP_LOST_FOCUS          = "Click here to start typing";

    private static final String FLOWCONTROL_NONE            = "None";
    private static final String FLOWCONTROL_XON_XOFF        = "XON/XOFF";
    private static final String FLOWCONTROL_RTS_CTS         = "RTS/CTS";

    private static final String EXCEPTION_PORT_NOTFOUND     = "Unable find serial port";
    private static final String EXCEPTION_INVALID_PORT      = "The TerminalEmulator must use a serial port";
    private static final String EXCEPTION_PORT_WONTOPEN     = "Unable to open the requested port";
    private static final String EXCEPTION_PORT_INUSE        = "The TerminalEmulator port is already in use";
    private static final String EXCEPTION_COMMSMODE         = "Cannot support requested Comms mode";
    private static final String EXCEPTION_LIBRARY           = "Unable to load the communications library";

    private static final String ACTION_CLEAR                = "Clear the Terminal Emulator";

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
    private static final int RECEIVE_BUFFER_SIZE = 4096;
    private static final int RECEIVE_THRESHOLD = 1;
    private static final int OPEN_TIMEOUT = 2000;

    private static final char CHAR_CR = '\r';
    private static final char CHAR_CONTROL = '^';
    private static final char CHAR_TILDE = '~';
    private static final char CHAR_QUERY = '?';
    private static final int ASCII_BASE = 64;

    private static final Color COLOR_LOST_FOCUS = Color.GRAY;

    private final TerminalEmulatorInterface terminalEmulator;
    private SwingWorker workerRx;
    private TransmitThread threadTx;

    private Vector<Vector> vecConsole;
    private ArrayBlockingQueue<Byte> outputQueue;
    private SerialPort serialPort;
    private long longLineCounter;
    private boolean boolUserWantsFocus;

    // Properties
    private FontInterface pluginFont;
    private ColourInterface colText;
    private ColourInterface colTable;
    private ColourInterface colCanvas;
    private SerialPortData serialportData;
    private boolean boolLocalEcho;

    private boolean boolSignOn;
    private boolean boolReceiving;


    /***********************************************************************************************
     * Construct a TerminalConsoleUIComponent JTable for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param font
     * @param text
     * @param table
     * @param canvas
     * @param portdata
     * @param resourcekey
     */

    public TerminalEmulatorConsole(final TaskPlugin task,
                                   final FontInterface font,
                                   final ColourInterface text,
                                   final ColourInterface table,
                                   final ColourInterface canvas,
                                   final SerialPortData portdata,
                                   final String resourcekey)
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

        if ((task == null)
            || (!task.validatePlugin())
            || (font == null)
            || (text == null)
            || (table == null)
            || (canvas == null)
            || (portdata == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        pluginFont = font;
        colText = text;
        colTable = table;
        colCanvas = canvas;
        serialportData = portdata;
        serialPort = null;

        // Save a reference to this for use in inner classes
        terminalEmulator = this;

        workerRx = null;
        threadTx = null;

        vecConsole = new Vector<Vector>(serialportData.getBufferSize());
        outputQueue = new ArrayBlockingQueue<Byte>(CAPACITY_OUTPUT_QUEUE, true);
        longLineCounter = 0;
        boolUserWantsFocus = false;
        boolLocalEcho = false;
        boolSignOn = false;
        boolReceiving = false;
        }


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Initialise the UI of this Report.
     */

    public void initialiseUI()
        {
        super.initialiseUI();

        // Override the ReportTable defaults with the TerminalEmulator specifics...
        setShowGrid(false);
        setTextColour(colText);
        setTableColour(colTable);
        setCanvasColour(colCanvas);
        setReportFont(pluginFont);

        // Initialise the Console Listeners
        initialiseListeners(getOutputQueue());
        }


    /***********************************************************************************************
     * Run the UI of this Report.
     */

    public void runUI()
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
                    runUI();
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

        if (!boolSignOn)
            {
            // Start with a clear screen!
            clearScreen();

            // Show a welcome message and the port configuration the first time through
            logOn(getConsoleDisplay(), getSerialPortData());

            // Initialise the serial port connection and Listeners etc.
            // This is done last of all so that initialisation messages may appear on the console
            setSerialPort(connectToSerialPort(getSerialPortData()));

            boolSignOn = true;
            }

//        showConsoleHasFocus(getReportTable(),
//                            getScrollPane(),
//                            false);
        }


    /***********************************************************************************************
     * Stop the UI of this ReportTable.
     */

    public final void stopUI()
        {
        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public final void disposeUI()
        {
        // Close the serial port if it is still open
        if (getSerialPort() != null)
            {
            try
                {
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
                LOGGER.debug("focus gained component="
                                + event.getComponent().getClass().getName()
                                + " lost component="
                                + event.getOppositeComponent().getClass().getName());

                showConsoleHasFocus(getReportTable(),
                                    getScrollPane(),
                                    event.getComponent().equals(terminalEmulator));
                }

            public void focusLost(final FocusEvent event)
                {
                LOGGER.debug("focus lost component="
                                + event.getComponent().getClass().getName()
                                + " gained component="
                                + event.getOppositeComponent().getClass().getName());
                showConsoleHasFocus(getReportTable(),
                                    getScrollPane(),
                                    false);
                }
            };

        //------------------------------------------------------------------------------------------
        // Used on the ReportTable and ScrollPane

        listenerMouse = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent event)
                {
                // The user must click in the console window in order to proceed
                //System.out.println("click in window component="
                    //+ event.getComponent().getName());
                setUserWantsFocus(requestFocusInWindow());
                showConsoleHasFocus(getReportTable(),
                                    getScrollPane(),
                                    getUserWantsFocus());
                }
            };

        //------------------------------------------------------------------------------------------
        // Used only on the TerminalConsoleUIComponent

        listenerKey = new KeyAdapter()
            {
            public synchronized void keyTyped(final KeyEvent event)
                {
                //System.out.println("key typed in window component="
                    //+ event.getComponent().getName());
                if (getUserWantsFocus())
                    {
                    final char charKey;

                    // This seems to be incapable of generating a CR! (mapped to LF)
                    charKey = event.getKeyChar();

                    if (charKey != KeyEvent.CHAR_UNDEFINED)
                        {
                        try
                            {
                            // Update the console display
                            // Display updates occur on the Event Dispatching Thread
                            // Only LF and ACSII 20-7E are sent to the console
                            if (getLocalEcho())
                                {
                                processChar(getConsoleDisplay(),
                                            getSerialPortData(),
                                            charKey);
                                getReportTableModel().fireTableDataChanged();
                                }

                            // Copy the character to the queue,
                            // to be transferred to the serial port output stream on another Thread
                            if (queue != null)
                                {
                                queue.put((byte) charKey);
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


    /**********************************************************************************************/
    /* Threads                                                                                    */
    /***********************************************************************************************
     * Start the TerminalEmulator session.
     */

    public void startSession()
        {
        // Start the Receive and Transmit Threads.
        // TODo thread log
        LOGGER.debugNavigation("Start the Receive and Transmit Threads");

        // Stop any existing Receiver SwingWorker
        SwingWorker.disposeWorker(workerRx, true, SWING_WORKER_STOP_DELAY);
        workerRx = null;

        // Stop any existing Transmitter Thread
        if (threadTx != null)
            {
            threadTx.setRunning(false);
            }

        // Prepare SwingWorker and Thread to receive and transmit
        prepareNewThreads(getConsoleDisplay(), getSerialPortData());

        // Start the Threads
        if (workerRx != null)
            {
            boolReceiving = true;
            workerRx.start();
            }

        if (threadTx != null)
            {
            threadTx.setRunning(true);
            threadTx.start();
            }
        }


    /***********************************************************************************************
     * Stop the TerminalEmulator session.
     */

    public void stopSession()
        {
        // Stop the Receive and Transmit Threads.
        // TODo thread log
        LOGGER.debugNavigation("Stop the Receive and Transmit Threads");

        boolReceiving = false;

        // Stop any existing Receiver SwingWorker
        SwingWorker.disposeWorker(workerRx, true, SWING_WORKER_STOP_DELAY);
        workerRx = null;

        // Stop any existing Transmitter Thread
        if (threadTx != null)
            {
            threadTx.setRunning(false);
            }
        }


    /***********************************************************************************************
     * Prepare Receive and Transmit Threads.
     *
     * @param console
     * @param portdata
     */

    private void prepareNewThreads(final Vector<Vector> console,
                                   final SerialPortData portdata)
        {
        // Prepare a thread to do the Receiver buffering
        workerRx = new SwingWorker(REGISTRY.getThreadGroup(),
                                   "SwingWorker TerminalConsoleUIComponent")
            {
            final byte[] buffer = new byte[CAPACITY_INPUT_STREAM];

            public Object construct()
                {
                //LOGGER.debugTimerTick("TerminalConsoleUIComponent: SwingWorker");

                try
                    {
                    boolean boolBytesOk;

                    boolBytesOk = true;

                    // This doesn't seem to work properly if we use only interrupt()
                    while ((boolReceiving)
                        && (boolBytesOk)
                        && (!isStopping()))
                        {
                        final int intBytesRead;

                        // There is an attempt to read at least one byte.
                        // If no byte is available because the stream is at end of file,
                        // the value -1 is returned;
                        // otherwise, at least one byte is read and stored into the buffer
                        if ((getSerialPort() != null)
                            && (getSerialPort().getInputStream() != null)
                            && (buffer != null))
                            {
                            // This will block until at least one character is available
                            intBytesRead = getSerialPort().getInputStream().read(buffer);

                            if (intBytesRead == -1)
                                {
                                boolBytesOk = false;
                                }
                            else
                                {
                                // ToDo Review use of Utilities.safeSleep() instead
                                sleep(10);

                                // Update the console output...
                                for (int i = 0; i < intBytesRead; i++)
                                    {
                                    processChar(console, portdata, (char)buffer[i]);
                                    }

                                // ... and the display
                                getReportTableModel().fireTableDataChanged();

                                // Incoming characters will grab the focus,
                                // which will change the text colour if we don't already have the focus
                                // so re-establish the UI setup
                                showConsoleHasFocus(getReportTable(),
                                                    getScrollPane(),
                                                    getUserWantsFocus());
                                }
                            }
                        }
                    }

                catch (IOException exception)
                    {
                    getTask().handleException(exception,
                                              "prepareNewThreads()",
                                              EventStatus.WARNING);
                    }

                // There is no result to pass to the Event Dispatching Thread
                return (null);
                }

            // Display updates occur on the Event Dispatching Thread
            public void finished()
                {
                // There is nothing to do on the Event Dispatching Thread
                LOGGER.debugTimerEvent("RX SWINGWORKER FINISHED");
                }
            };

        // Prepare another thread to do the Transmit buffering
        try
            {
            if ((getOutputQueue() != null)
                && (getSerialPort() != null)
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
     *
     * @param portdata
     *
     * @return SerialPort
     */

    private SerialPort connectToSerialPort(final SerialPortData portdata)
        {
        SerialPort port;
        final Enumeration portList;
        CommPortIdentifier portRequested;
        final CommPortIdentifier portID;
        boolean boolFoundPort;

        if (portdata == null)
            {
            getTask().handleException(new FrameworkException(EXCEPTION_PARAMETER_NULL),
                                      "connectToSerialPort()",
                                      EventStatus.WARNING);

            }

        port = null;

        try
            {
            // Stop any existing SwingWorkers
            SwingWorker.disposeWorker(workerRx, true, SWING_WORKER_STOP_DELAY);
            workerRx = null;

            ClassPathLoader.showClassLoaderSearchPaths(isDebug());
            portList = CommPortIdentifier.getPortIdentifiers();
            portRequested = null;
            portID = null;
            boolFoundPort = false;

            // Scan all ports found for the one with the required name
            LOGGER.debug("Ports available");

            while ((portList != null)
                && (portList.hasMoreElements())
                && (!boolFoundPort))
                {
                final CommPortIdentifier portId;

                portId = (CommPortIdentifier) portList.nextElement();

                LOGGER.debug(INDENT + "port {" + portId.getName() + "}");

                if (portId.getName().equals(portdata.getPortName()))
                    {
                    portRequested = portId;
                    boolFoundPort = true;
                    LOGGER.debug("Port found {" + portId.getName() + "}");
                    }
                }

            // Did we find a port?
            if (portRequested == null)
                {
                // We did not find the required port
                addLine(getConsoleDisplay(),
                        getSerialPortData(),
                        EXCEPTION_PORT_NOTFOUND
                            + SPACE
                            + METADATA_PORTNAME
                            + portdata.getPortName()
                            + TERMINATOR);
                }
            else
                {
                // Check that the port of the required name is actually a serial port!
                if (portRequested.getPortType() == CommPortIdentifier.PORT_SERIAL)
                    {
                    // Is the port already in use?
                    if (portRequested.isCurrentlyOwned())
                        {
                        addLine(getConsoleDisplay(),
                                getSerialPortData(),
                                EXCEPTION_PORT_INUSE
                                    + SPACE
                                    + METADATA_PORTOWNER
                                    + portRequested.getCurrentOwner()
                                    + TERMINATOR);
                        }
                    else
                        {
                        // Try to open the port and input stream
                        port = openSerialPort(portRequested, portdata);

                        if (port != null)
                            {
                            // If we get here, then it all worked Ok!
                            addLine(getConsoleDisplay(),
                                    getSerialPortData(),
                                    portdata.getPortName()
                                        + SPACE
                                        + MSG_OPENED);
                            }
                        else
                            {
                            addLine(getConsoleDisplay(),
                                    getSerialPortData(),
                                    EXCEPTION_PORT_WONTOPEN
                                        + SPACE
                                        + METADATA_PORTNAME
                                        + portdata.getPortName()
                                        + TERMINATOR);
                            }
                        }
                    }
                else
                    {
                    // We don't know how to use any other kind of port!
                    addLine(getConsoleDisplay(),
                            getSerialPortData(),
                            EXCEPTION_INVALID_PORT
                                + SPACE
                                + METADATA_PORTTYPE
                                + portID.getPortType()
                                + TERMINATOR);
                    }
                }

            getReportTableModel().fireTableDataChanged();
            }

        catch (UnsatisfiedLinkError error)
            {
            addLine(getConsoleDisplay(),
                    getSerialPortData(),
                    EXCEPTION_LIBRARY);
            ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
            }

        return (port);
        }


    /***********************************************************************************************
     * Try to open the specified requested port using the SerialPortData supplied.
     *
     * @param requestedport
     * @param portdata
     *
     * @return SerialPort
     */

    private SerialPort openSerialPort(final CommPortIdentifier requestedport,
                                      final SerialPortData portdata)
        {
        String strSerialPortName;
        SerialPort port;

        if ((requestedport == null)
            || (portdata == null))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }

        strSerialPortName = EMPTY_STRING;
        port = null;

        try
            {
            port = (SerialPort) requestedport.open(portdata.getPortOwner(), OPEN_TIMEOUT);
            port.enableReceiveThreshold(RECEIVE_THRESHOLD);
            port.setInputBufferSize(RECEIVE_BUFFER_SIZE);
            strSerialPortName = port.getName();

            port.setSerialPortParams(portdata.getBaudrate(),
                                     portdata.getDatabits(),
                                     portdata.getStopbits(),
                                     portdata.getParity());

            // Set no flow control if no others found
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            if (portdata.getFlowControl().equals(FLOWCONTROL_NONE))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                }

            if (portdata.getFlowControl().equals(FLOWCONTROL_XON_XOFF))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN
                                                  & SerialPort.FLOWCONTROL_XONXOFF_OUT);
                }

            if (portdata.getFlowControl().equals(FLOWCONTROL_RTS_CTS))
                {
                port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
                                                  & SerialPort.FLOWCONTROL_RTSCTS_OUT);
                }
             }

        catch (PortInUseException exception)
            {
            if (port != null)
                {
                addLine(getConsoleDisplay(),
                        getSerialPortData(),
                        EXCEPTION_PORT_INUSE
                            + SPACE
                            + METADATA_PORTNAME
                            + port.getName()
                            + TERMINATOR);
                port = null;
                }
            }

        catch (UnsupportedCommOperationException exception)
            {
            addLine(getConsoleDisplay(),
                    getSerialPortData(),
                    EXCEPTION_COMMSMODE);
            port.close();
            port = null;
            }

        getReportTableModel().fireTableDataChanged();

        // If we get this far, the port was configured and opened correctly
        LOGGER.debug(strSerialPortName + " Port configured and opened correctly");

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
        vecColumns.add(new ReportColumnMetadata( TITLE_LINE,
                                             SchemaDataType.STRING,
                                             SchemaUnits.DIMENSIONLESS,
                                             EMPTY_STRING,
                                             SwingConstants.LEFT ));
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
            "9999",
            "2004-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
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

    private void logOn(final Vector<Vector> console,
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
                    KEY_BUFFER_SIZE + EQUALS + portdata.getBufferSize());

            // Move the cursor to a new blank line ready for user input
            addLine(console, portdata, CURSOR);
            getReportTableModel().fireTableDataChanged();
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
     *
     * @param console
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
            vecRow.add(ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
            vecRow.add(ChronosHelper.toTimeString(Chronos.getCalendarTimeNow()));
            vecRow.add(new StringBuffer(line));

            if (portdata != null)
                {
                vecRow.add(portdata.getPortName());
                console.add(vecRow);
                scrollConsole(console, portdata);
                }
            else
                {
                vecRow.add(MSG_NO_CONNECTION);
                console.add(vecRow);
                }
            }
        }


    /***********************************************************************************************
     * Put a single character at the end of the last row of the console.
     * Interpret control characters appropriately.
     * Update the Date and Time to show the time of the change.
     *
     * @param console
     * @param portdata
     * @param character
     */

    private void processChar(final Vector<Vector> console,
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
                    putCharAtCursor(console, portdata, CHAR_CONTROL);
                    putCharAtCursor(console, portdata, (char)(character + (char)ASCII_BASE));
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
                putCharAtCursor(console, portdata, character);
                }
            else
                {
                // Just show that something odd happened
                putCharAtCursor(console, portdata, CHAR_QUERY);
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

    private void putCharAtCursor(final Vector<Vector> console,
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
                        // Add the character to the end of the buffer (the 'cursor position')
                        bufferSession.setCharAt(bufferSession.length()-1, character);
                        bufferSession.append(CURSOR);

                        // Update the Date and Time to show the time of the change
                        vecCursorRow.set(INDEX_DATE, ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
                        vecCursorRow.set(INDEX_TIME, ChronosHelper.toTimeString(Chronos.getCalendarTimeNow()));
                        }
                    }
                }
            else
                {
                // This is the first character to be displayed,
                // so we must make a new line
                addLine(console, portdata, character + CURSOR);
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
            vecRow.add(ChronosHelper.toDateString(Chronos.getCalendarDateNow()));
            vecRow.add(ChronosHelper.toTimeString(Chronos.getCalendarTimeNow()));
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


    /***********************************************************************************************
     * Move the cursor back one position, deleting the previous character.
     *
     * @param console
     */

    private void backSpace(final Vector<Vector> console)
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
     * Scroll the console by one line.
     *
     * @param console
     * @param portdata
     */

    private void scrollConsole(final Vector<Vector> console,
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

            getReportTableModel().fireTableDataChanged();
            scrollRowToVisible(console.size()-1);
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

    private SerialPortData getSerialPortData()
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
     *
     * @param table
     * @param scrollpane
     * @param focus
     */

    private synchronized void showConsoleHasFocus(final JTable table,
                                                  final JScrollPane scrollpane,
                                                  final boolean focus)
        {
        setUserWantsFocus(focus);

        if ((table != null)
            && (scrollpane != null))
            {
            if (getUserWantsFocus())
                {
                //System.out.println("set green");
                table.setForeground(getTextColour().getColor());
                table.setToolTipText(EMPTY_STRING);
                scrollpane.setToolTipText(EMPTY_STRING);
                }
            else
                {
                //System.out.println("set grey");
                table.setForeground(COLOR_LOST_FOCUS);
                table.setToolTipText(TOOLTIP_LOST_FOCUS);
                scrollpane.setToolTipText(TOOLTIP_LOST_FOCUS);
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
     * Get the Local Echo mode.
     *
     * @return boolean
     */

    private synchronized boolean getLocalEcho()
        {
        return (this.boolLocalEcho);
        }


    /***********************************************************************************************q
     * Set the Local Echo mode.
     *
     * @param echo
     */

    private synchronized void setLocalEcho(final boolean echo)
        {
        this.boolLocalEcho = echo;
        }


    /***********************************************************************************************
     * Read all the Resources required by the TerminalConsoleUIComponent.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public final void readResources()
        {
        super.readResources();

        getSerialPortData().setBufferSize(BUFFER_SIZE_MAX);
        setLocalEcho(false);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
