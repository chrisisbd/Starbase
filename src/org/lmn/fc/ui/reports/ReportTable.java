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

package org.lmn.fc.ui.reports;

//--------------------------------------------------------------------------------------------------
// Imports

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.FontDataType;
import org.lmn.fc.model.emails.EmailMessageInterface;
import org.lmn.fc.model.emails.impl.EmailMessageData;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * The base class for a Reports table shown on a JPanel, which may be printed.
 *
 * Todo Initialise all properties
 * ToDo Show wait cursor on lengthy operations?
 * ToDo set horiz alignment on columns/headers
 * ToDo FrameworkStrings
 */

public abstract class ReportTable extends UIComponent
                                  implements ReportTablePlugin
    {
    // Injections
    private final RootPlugin pluginTask;
    private String strReportTabName;
    private String strReportUniqueName;
    private final String strResourceKey;
    private boolean boolPrintable;
    private boolean boolExportable;
    private boolean boolRefreshable;
    private boolean boolClickRefresh;
    private boolean boolReorderable;
    private boolean boolTruncateable;
    private final boolean boolLockTopRow;
    private final boolean boolLockLeftColumns;
    private final int intColumnsToLock;
    private final ReportTableToolbar toolbarState;
    private Icon iconToolBar;

    // Refresh Timer
    private Timer timerRefresh;
    private int intRefreshPeriod;
    private SwingWorker workerRefresh;

    // Report data
    private ReportTableModel modelReport;
    private Vector<String> vecHeader;
    private Vector<PrintableIconInterface> vecIcons;
    private Vector<ReportColumnMetadata> vecColumns;
    private Object [] columnWidths;
    private int intPreferredWidth;

    // User Interface
    private JToolBar toolBar;
    private JScrollPane scrollPane;
    private JPanel panelTable;
    private JTableHeader tableHeader;
    private JTable tableReport;
    private int intScrollToRow;
    private DataViewMode dataViewMode;
    private int intDataViewLimit;
    private JTextField textFieldDataViewLimit;

    // Printing Properties & Resources
    private int intMaxPages;
    private int intOrientation;
    private int intRowsPerFirstPage;
    private FontInterface fontHeader;
    private FontInterface fontReport;
    private ColourInterface colourCanvas;
    private ColourInterface colourTable;
    private ColourInterface colourGrid;
    private ColourInterface pluginColour;
    private boolean boolPageDefault;
    private double dblPageWidth;
    private double dblPageHeight;
    private double dblMarginTop;
    private double dblMarginBottom;
    private double dblMarginLeft;
    private double dblMarginRight;
    private boolean boolEnableBorder;

    private String MENU_PRINT;       // The text for the Print Action
    private String MENU_TEXT;        // The text for the Text Export Action
    private String MENU_EXCEL;       // The text for the Excel Export Action
    private String MENU_XML;         // The text for the XML Export Action
    private String MENU_EMAIL;       // The text for the Email Export Action


    /**********************************************************************************************
     * Construct a report named as specified, run under the specified TaskData.
     *
     * @param task
     * @param name
     * @param resourcekey
     * @param printable
     * @param exportable
     * @param refreshable
     * @param clickrefresh
     * @param reorderable
     * @param truncateable
     * @param locktoprow
     * @param lockleftcolumns
     * @param columnstolock
     * @param toolbarstate
     * @param toolbaricon
     */

    public ReportTable(final RootPlugin task,
                       final String name,
                       final String resourcekey,
                       final boolean printable,
                       final boolean exportable,
                       final boolean refreshable,
                       final boolean clickrefresh,
                       final boolean reorderable,
                       final boolean truncateable,
                       final boolean locktoprow,
                       final boolean lockleftcolumns,
                       final int columnstolock,
                       final ReportTableToolbar toolbarstate,
                       final Icon toolbaricon)
        {
        // Create the Report JPanel
        super(new BorderLayout());

        // Injections
        this.pluginTask = task;
        this.strReportTabName = name;
        this.strReportUniqueName = name;
        this.strResourceKey = resourcekey;
        this.boolPrintable = printable;
        this.boolExportable = exportable;
        this.boolRefreshable = refreshable;
        this.boolClickRefresh = clickrefresh;
        this.boolReorderable = reorderable;
        this.boolTruncateable = truncateable;
        this.boolLockTopRow = locktoprow;
        this.boolLockLeftColumns = lockleftcolumns;
        this.intColumnsToLock = columnstolock;
        this.toolbarState = toolbarstate;
        this.iconToolBar = toolbaricon;

        this.toolBar = null;
        this.modelReport = null;
        this.scrollPane = null;
        this.intScrollToRow = 0;

        // Set up defaults, just in case
        this.boolPageDefault = true;
        this.intOrientation = PageFormat.PORTRAIT;
        this.fontHeader = new FontDataType("font=SansSerif style=plain size=14");
        this.fontReport = new FontDataType("font=SansSerif style=plain size=12");
        this.colourGrid = null;
        this.dataViewMode = DataViewMode.SHOW_LAST;
        this.intDataViewLimit = -1;
        this.textFieldDataViewLimit = null;

        this.timerRefresh = null;
        this.intRefreshPeriod = 5;
        }


    /***********************************************************************************************
     * initialiseUI().
     */

    public synchronized void initialiseUI()
        {
        super.initialiseUI();

        LOGGER.debugNavigation("ReportTable.initialiseUI() report=" + getReportUniqueName());

        try
            {
            removeAll();
            initialiseReport();
            }

        catch (final ReportException exception)
            {
            LOGGER.error("Unable to initialise the ReportTable");
            exception.printStackTrace();
            }
        }


    /***********************************************************************************************
     * Run the UI of this ReportTable.
     * Refresh the data and start the Timer, if any.
     * Usually called in RootData.runUI().
     */

    public synchronized void runUI()
        {
        LOGGER.debugNavigation("ReportTable.runUI() [Report=" + getReportUniqueName() + "]");

        // Update the properties in case subclasses change them...

        try
            {
            tableReport.setFont(getReportFont().getFont());
            tableReport.setForeground(getTextColour().getColor());
            tableReport.setBackground(getTableColour().getColor());
            panelTable.setBackground(getCanvasColour().getColor());
            }
        catch (final Exception e)
            {
            System.out.println("ReportTable.runUI()");
            e.printStackTrace();
            }

        // Leave the default L&F grid colour alone unless it has been overwritten
        if (getGridColour() != null)
            {
            tableReport.setGridColor(getGridColour().getColor());
            }

        // Ensure that there is something to see...
        refreshTable();

        // Start the Report timer, if any
        setRefreshTimer(REFRESH_START);

        // Check to see if we need to provide Print and Export buttons on the toolbar
        // and add them to any existing Actions
        assembleContextActionGroups();

        super.runUI();
        }


    /***********************************************************************************************
     * Stop the UI of this ReportTable.
     * Clear the Table and stop the Timer to reduce resources.
     * Usually called in RootData.stopUI().
     */

    public synchronized void stopUI()
        {
//        LOGGER.debug("ReportTable stopUI() " + getReportUniqueName());
        // Stop the ReportTable timer
        setRefreshTimer(REFRESH_STOP);

        if (modelReport != null)
            {
            // Remove the Table data
            modelReport.setRows(new Vector<Vector>(10));
            modelReport.fireTableDataChanged();
            }

        clearUIComponentContextActionGroups();
        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        scrollPane = null;
        tableHeader = null;
        tableReport = null;

        if (timerRefresh != null)
            {
            // Make sure that the Timer has stopped running
            if (timerRefresh.isRunning())
                {
                timerRefresh.stop();
                }

            timerRefresh = null;
            }

        SwingWorker.disposeWorker(workerRefresh, true, SWING_WORKER_REPORT_TABLE_REFRESH_DELAY);
        workerRefresh = null;

        clearUIComponentContextActionGroups();
        super.disposeUI();
        }


    /**********************************************************************************************
     * Set up the table given the header and column information,
     * using the interface methods.
     */

    public synchronized JTable initialiseReport() throws ReportException
        {
        LOGGER.debugNavigation("ReportTable.initialiseReport() for " + getReportUniqueName());

        // Create the new data
        vecHeader = generateHeader();
        vecIcons = generateIcons();
        vecColumns = defineColumns();
        columnWidths = defineColumnWidths();

        // Read the Fonts etc.
        readResources();

        // Create the ReportTableModel from the supplied information
        modelReport = new ReportTableModel(vecHeader,
                                           vecIcons,
                                           vecColumns,
                                           columnWidths,
                                           generateReport());

        // Create and configure the JTable
        tableReport = new JTable(modelReport);

        // The default is for no selection allowed!
        tableReport.setRowSelectionAllowed(false);
        tableReport.setColumnSelectionAllowed(false);
        tableReport.setCellSelectionEnabled(false);

        tableReport.setBackground(getTableColour().getColor());
        tableReport.setForeground(getTextColour().getColor());
        tableReport.setFont(getReportFont().getFont());

        // Leave the default L&F grid colour alone unless it has been overwritten
        if (getGridColour() != null)
            {
            tableReport.setGridColor(getGridColour().getColor());
            }

        // Check to see if we need to listen for mouse clicks to refresh the data
        if (isRefreshable())
            {
            // See which refresh mode we must use
            if (isClickRefresh())
                {
                tableReport.setToolTipText(TOOLTIP_CLICK_HERE);

                tableReport.addMouseListener(new MouseAdapter()
                    {
                    public void mousePressed(final MouseEvent event)
                        {
                        //LOGGER.debug("REFRESH!!");
                        refreshTable();
                        }
                    });

                tableHeader = tableReport.getTableHeader();
                tableHeader.setToolTipText(tableReport.getToolTipText());

                tableHeader.addMouseListener(new MouseAdapter()
                    {
                    public void mousePressed(final MouseEvent event)
                        {
                        refreshTable();
                        }
                    });
                }
            else
                {
                // It must be a Timer refresh
                tableReport.setToolTipText(TOOLTIP_TIMER_REFRESH + SPACE + (getRefreshPeriod()/1000) + "sec");
                tableHeader = tableReport.getTableHeader();
                tableHeader.setToolTipText(tableReport.getToolTipText());

                // Assume Timer refresh...
                timerRefresh = new Timer(getRefreshPeriod(),
                                         new ActionListener()
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        LOGGER.debugTimerTick("ReportTable(Refresh): " + getReportUniqueName());
                        refreshTable();
                        }
                    });
                }
            }
        else
            {
            // The Report may not be refreshed
            tableReport.setToolTipText(EMPTY_STRING);
            }

        // Configure the table header
        tableHeader = tableReport.getTableHeader();
        tableHeader.setReorderingAllowed(isReorderable());
        tableHeader.setForeground(getTextColour().getColor());
        tableHeader.setFont(getHeaderFont().getFont());

        initColumnRenderers(tableReport, modelReport);

        // Put it all together on a JPanel
        panelTable = new JPanel();
        panelTable.setLayout(new BoxLayoutFixed(panelTable, BoxLayoutFixed.Y_AXIS));
        panelTable.setBackground(getCanvasColour().getColor());

        // ...and then on to a JScrollPane
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(panelTable);
        scrollPane.getViewport().setBackground(tableReport.getBackground());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // See if we need to lock the top row (containing the table header)
        if (isTopRowLocked())
            {
            scrollPane.setColumnHeaderView(tableReport.getTableHeader());
            panelTable.add(tableReport);
            }
        else
            {
            panelTable.add(tableReport.getTableHeader());
            panelTable.add(tableReport);
            }

        //-----------------------------------------------------------------------------------------
        // See if a Toolbar is required

        switch (getToolbarState())
            {
            case NONE:
                {
                add(getScrollPane(), BorderLayout.CENTER);
                break;
                }

            case HORIZ_NORTH_PRT:
            case HORIZ_NORTH_RNG_PRT_RF_RV_TV_DA:
            case HORIZ_SOUTH_RNG_PRT_RF_RV_TV_DA:
                {
                // Create the JToolBar and initialise it
                setToolBar(new JToolBar(JToolBar.HORIZONTAL));
                getToolBar().setFloatable(false);
                getToolBar().setMinimumSize(getToolbarState().getDimension());
                getToolBar().setPreferredSize(getToolbarState().getDimension());
                getToolBar().setMaximumSize(getToolbarState().getDimension());
                getToolBar().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

                this.textFieldDataViewLimit = ReportTableHelper.initialiseHorizontalToolbar(this,
                                                                                            getToolBar(),
                                                                                            getToolbarState(),
                                                                                            getToolBarIcon());

                add(getToolBar(), getToolbarState().getOrientation());
                add(getScrollPane(), BorderLayout.CENTER);
                break;
                }

            case VERT_EAST_PRT_TV_DA:
            case VERT_WEST_PRT_TV_DA:
                {
                final Dimension dimVertical;

                // Create the JToolBar and initialise it
                setToolBar(new JToolBar(JToolBar.VERTICAL));
                getToolBar().setFloatable(false);
                getToolBar().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

                // Constrain the height to be the same as the Report
                getToolBar().setMinimumSize(new Dimension((int) getToolbarState().getDimension().getWidth(),
                                                          (int) getMinimumSize().getHeight()));
                getToolBar().setPreferredSize(new Dimension((int) getToolbarState().getDimension().getWidth(),
                                                            (int) getPreferredSize().getHeight()));
                getToolBar().setMaximumSize(new Dimension((int) getToolbarState().getDimension().getWidth(),
                                                          (int) getMaximumSize().getHeight()));

                // There is no label or icon
                this.textFieldDataViewLimit = ReportTableHelper.initialiseVerticalToolbar(this,
                                                                                          getToolBar(),
                                                                                          getToolbarState());

                add(getToolBar(), getToolbarState().getOrientation());
                add(getScrollPane(), BorderLayout.CENTER);
                break;
                }
            }

        return (tableReport);
        }


    /***********************************************************************************************
     * Control the Refresh Timer with <code>REFRESH_START</code> or <code>REFRESH_STOP</code>.
     * This has no effect if the current refresh mode is Clickable.
     *
     * @param state
     */

    private synchronized void setRefreshTimer(final boolean state)
        {
        // Control the Timer
        if (timerRefresh != null)
            {
            if (state == REFRESH_START)
                {
                timerRefresh.setCoalesce(false);
                timerRefresh.restart();
                LOGGER.debugTimerEvent("ReportTable START [name=" + getReportUniqueName() + "]");
                }
            else
                {
                if (timerRefresh.isRunning())
                    {
                    timerRefresh.stop();
                    LOGGER.debugTimerEvent("ReportTable STOP [name=" + getReportUniqueName() + "]");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Refresh the table data on a separate thread.
     * This is used by all setWrappedData().
     */

    public synchronized final void refreshTable()
        {
//        System.out.println("ReportTable.refreshTable() [name=" + getReportUniqueName() + "]");

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               getReportUniqueName() + " ReportTable.refreshTable() [name=" + getReportUniqueName()
                           + "] [task=" + getTask().getName()
                           + "] [tick " + getRefreshPeriod() + " sec]");
        // Update all Resources whenever the Table is refreshed
        //LOGGER.debug("refreshTable");
//        readResources();

        // Stop any existing SwingWorker
        if (workerRefresh != null)
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   getReportUniqueName() + " ReportTable.refreshTable() interrupt existing SwingWorker BEFORE controlledStop() report="
                                    + getReportUniqueName()
                                    + " thread=" + workerRefresh.getName());

            // We don't need to wait for long before trying again just for a table refresh
            SwingWorker.disposeWorker(workerRefresh, true, SWING_WORKER_REPORT_TABLE_REFRESH_DELAY);
            workerRefresh = null;

//            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
//                                   getReportUniqueName() + " ReportTable.refreshTable() AFTER controlledStop() report="
//                                    + getReportUniqueName()
//                                    + " thread=" + workerRefresh.getName());
            }

        // Fire off another thread to prepare the report
        workerRefresh = new SwingWorker(REGISTRY.getThreadGroup(),
                                        getReportUniqueName() + " SwingWorker ReportTable "
                                            + "[plugin=" + getTask().getParentAtom().getName() + "]"
                                            + "[task=" + getTask().getName() + "]"
                                            + "[report=" + getReportUniqueName() + "]")
            {
            public Object construct()
                {
                //LOGGER.debugTimerTick("ReportTable: SwingWorker " + getReportUniqueName());
                try
                    {
                    // Are we running under a Timer?
//                    if (timerRefresh != null)
//                        {
//                        // Update the Timer delay in case the RegistryModel has changed...
//                        timerRefresh.setDelay(getRefreshPeriod());
//                        }

                    // The result of this Thread is a Vector of new data
                    // which may have been produced in a long database process...
//                    LOGGER.debug("ReportTable refreshReport()");
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           getReportUniqueName() + " ReportTable SwingWorker.construct() refreshTable() BEFORE refreshReport() [stopping=" + isStopping() + "]");

                    if (!isStopping())
                        {
                        final Vector<Vector> vecRefreshedReport;

                        vecRefreshedReport = refreshReport();

                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               getReportUniqueName() + " ReportTable SwingWorker.construct() AFTER refreshReport()");


                        return (vecRefreshedReport);
                        }
                    else
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               getReportUniqueName() + " ReportTable SwingWorker.construct() ASKED TO STOP");

                        return (null);
                        }
                    }

                catch (final NullPointerException exception)
                    {
                    exception.printStackTrace();
                    }

                catch (final ConcurrentModificationException exception)
                    {
                    LOGGER.error("ConcurrentModificationException in " + getReportUniqueName());
                    exception.printStackTrace();
                    }

                catch (final ReportException exception)
                    {
                    getTask().handleException(exception,
                                              "refreshTable()",
                                              EventStatus.WARNING);
                    }

                catch (final Exception exception)
                    {
                    exception.printStackTrace();
                    }

                // This exit point should only occur on an error
                return (null);
                }

            // Display updates occur on the Event Dispatching Thread
            public void finished()
                {
                // Update the table on the Event Dispatching Thread
//                LOGGER.debug("ReportTable SwingWorker finished");

                if ((get() != null)
                    && (get() instanceof Vector)
                    && (modelReport != null)
                    && (SwingUtilities.isEventDispatchThread()))
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           getReportUniqueName() + " ReportTable SwingWorker.finished()");

                    if (!isStopping())
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               getReportUniqueName() + " ReportTable SwingWorker.finished() change data in fireTableDataChanged [thread=" + this.getName() + "]");

                        modelReport.setRows((Vector<Vector>)get());
                        modelReport.fireTableDataChanged();

                        // Scroll the table so that the last output is visible
                        // Don't do this is there is nothing to scroll!
                        if (modelReport.getRowCount() > 0)
                            {
                            // Negative row means scroll to the end
                            if (getScrollToRow() < 0)
                                {
                                // Pass the row index (0...n)
                                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                       "ReportTable.refreshTable() scrollRowToVisible() [row=" + (modelReport.getRowCount()-1) + "]");
                                scrollRowToVisible(modelReport.getRowCount()-1);
                                }
                            else
                                {
                                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                       "ReportTable.refreshTable() scrollRowToVisible() [row=" + getScrollToRow() + "]");
                                scrollRowToVisible(getScrollToRow());
                                }
                            }
                        }
                    }
                else
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           getReportUniqueName() + " ReportTable SwingWorker.finished() NO DATA RETURNED");
                    }
                }
            };

        // Start the Thread we have prepared...
        workerRefresh.start();
        }


    /***********************************************************************************************
     * Get the Thread used to refresh the Report.
     *
     * @return SwingWorker
     */

    public synchronized SwingWorker getRefreshThread()
        {
        return (this.workerRefresh);
        }


    /***********************************************************************************************
     * Generate the raw Report, i.e not truncated,
     * and regardless of whether the component is visible. This is used for e.g. exports.
     * This should be overridden by those Reports which are inactive unless visible.
     *
     * @return Vector<Vector>
     *
     * @throws ReportException
     */

    public Vector<Vector> generateRawReport() throws ReportException
        {
        return (generateReport());
        }



    /***********************************************************************************************
     * Assemble the Report ContextActions for use by rebuildNavigation().
     * For TaskUIs, add the Report ContextActions to the Task's PrimaryContextActionGroup.
     * For TabbedUIs, use the first ContextActionGroup created by the UIComponent subclass,
     * if one exists; if not, make a new group named from the ReportName.
     * The appropriate set will be displayed at runtime.
     *
     * @throws ReportException
     */

    private synchronized void assembleContextActionGroups()
        {
        final ContextActionGroup actionGroup;
        ContextAction actionContext;
        URL imageURL;

        LOGGER.debugNavigation("ReportTable.assembleContextActionGroups");

        //------------------------------------------------------------------------------------------
        // Is there an appropriately-named existing UIComponent ContextActionGroup
        // which was created by the subclass?

        if ((getUIComponentContextActionGroups() != null)
            && (!getUIComponentContextActionGroups().isEmpty())
            && (getUIComponentContextActionGroups().get(0) != null)
            && (getReportUniqueName() != null)
            && (getReportUniqueName().equals(getUIComponentContextActionGroups().get(0).getName())))
            {
            LOGGER.debugNavigation("ReportTable.assembleContextActionGroups found existing group=" + getReportUniqueName());
            actionGroup = getUIComponentContextActionGroups().get(0);

            // Now add a separator for the Menu only...
            if (!actionGroup.isEmpty())
                {
                LOGGER.debugNavigation("group was not empty");
                actionGroup.addContextAction(ContextAction.getSeparator(true, false));
                }
            }
        else
            {
            // There is no existing group
            // Make a new group named from the ReportName, and add it to the UIComponent
            LOGGER.debugNavigation("ReportTable.assembleContextActionGroups creating group=" + getReportUniqueName());
            actionGroup = new ContextActionGroup(getReportUniqueName(), true, true);
            addUIComponentContextActionGroup(actionGroup);
            }

        LOGGER.debugNavigation("CAG BEFORE adding Print etc in ReportTable " + getReportUniqueName());
        //ContextActionGroup.showContextActions(actionGroup);

        //------------------------------------------------------------------------------------------
        // Set up the Report ContextActions, for menu and toolbar

        if (isPrintable())
            {
            imageURL = getClass().getResource(ACTION_ICON_PRINT);

            if (imageURL != null)
                {
                actionContext = new ContextAction(getReportUniqueName() + ": " + MENU_PRINT,
                                                  new ImageIcon(imageURL),
                                                  getReportUniqueName() + ": " + MENU_PRINT,
                                                  KeyEvent.VK_P,
                                                  true,
                                                  true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        // Check to see that we actually have a printer...
                        if (PrinterJob.lookupPrintServices().length == 0)
                            {
                            JOptionPane.showMessageDialog(null,
                                                          MSG_NOPRINTER,
                                                          DIALOG_PRINT + SPACE + getReportUniqueName(),
                                                          JOptionPane.WARNING_MESSAGE);
                            return;
                            }

                        // Print the Report
                        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        printReport();
                        REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    };

                actionGroup.addContextAction(actionContext);
                }
            }

        if (false)
//        TODO if (isExportable())
            {
            imageURL = getClass().getResource(ACTION_ICON_EXPORT);

            if (imageURL != null)
                {
                actionContext = new ContextAction(getReportUniqueName() + ": " + MENU_TEXT,
                                                  new ImageIcon(imageURL),
                                                  getReportUniqueName() + ": " + MENU_TEXT,
                                                  KeyEvent.VK_T,
                                                  true,
                                                  true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        try
                            {
                            // Produce a tab-separated Report
                            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//                            exportText(REGISTRY.getFramework().getExportsFolder()
//                                           + System.getProperty("file.separator")
//                                           + getReportUniqueName()
//                                           + FileUtilities.timestampFileName()
//                                           + DOT
//                                           + FileUtilities.tsv,
//                                       ReportTable.TAB_SEPARATOR);
                            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            }

                        catch (final ReportException exception)
                            {
                            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            getTask().handleException(exception,
                                                      "exportText() Text",
                                                      EventStatus.WARNING);
                            }
                        }
                    };

                actionGroup.addContextAction(actionContext);
                }

            //--------------------------------------------------------------------------------------
            // Excel

            imageURL = getClass().getResource(ACTION_ICON_EXCEL);

            if (imageURL != null)
                {
                actionContext = new ContextAction(getReportUniqueName() + ": " + MENU_EXCEL,
                                                  new ImageIcon(imageURL),
                                                  getReportUniqueName() + ": " + MENU_EXCEL,
                                                  KeyEvent.VK_E,
                                                  true,
                                                  true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        try
                            {
                            // Produce an Excel file
                            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//                            exportExcel(REGISTRY.getFramework().getExportsFolder()
//                                            + System.getProperty("file.separator")
//                                            + getReportUniqueName()
//                                            + FileUtilities.timestampFileName()
//                                            + DOT
//                                            + FileUtilities.xls);
                            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            }

                        catch (final ReportException exception)
                            {
                            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            getTask().handleException(exception,
                                                      "exportText() Excel",
                                                      EventStatus.WARNING);
                            }
                        }
                    };

                actionGroup.addContextAction(actionContext);
                }

            if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
                {
                //----------------------------------------------------------------------------------
                // XML

                imageURL = getClass().getResource(ACTION_ICON_XML);

                if (imageURL != null)
                    {
                    actionContext = new ContextAction(getReportUniqueName() + ": " + MENU_XML,
                                                      new ImageIcon(imageURL),
                                                      getReportUniqueName() + ": " + MENU_XML,
                                                      KeyEvent.VK_X,
                                                      true,
                                                      true)
                        {
                        public void actionPerformed(final ActionEvent event)
                            {
                            try
                                {
                                // Produce an XML file
                                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//                                exportXML(REGISTRY.getFramework().getExportsFolder()
//                                                + System.getProperty("file.separator")
//                                                + getReportUniqueName()
//                                                + FileUtilities.timestampFileName()
//                                                + DOT
//                                                + FileUtilities.xml);
                                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                }

                            catch (final ReportException exception)
                                {
                                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                getTask().handleException(exception,
                                                          "exportText() XML",
                                                          EventStatus.WARNING);
                                }
                            }
                        };

                    actionGroup.addContextAction(actionContext);
                    }

                //----------------------------------------------------------------------------------
                // Email

                imageURL = getClass().getResource(ACTION_ICON_EMAIL);

                if (imageURL != null)
                    {
                    actionContext = new ContextAction(getReportUniqueName() + ": " + MENU_EMAIL,
                                                      new ImageIcon(imageURL),
                                                      getReportUniqueName() + ": " + MENU_EMAIL,
                                                      KeyEvent.VK_M,
                                                      true,
                                                      true)
                        {
                        public void actionPerformed(final ActionEvent event)
                            {
                            try
                                {
                                // Produce an Email
                                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                exportEmail();
                                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                }

                            catch (final ReportException exception)
                                {
                                REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                getTask().handleException(exception,
                                                          "exportText() Email",
                                                          EventStatus.WARNING);
                                }
                            }
                        };

                    actionGroup.addContextAction(actionContext);
                    }
                }
            }

        LOGGER.debugNavigation("CAG AFTER adding Print etc in ReportTable " + getReportUniqueName());
        //ContextActionGroup.showContextActions(actionGroup);
        }


    /**********************************************************************************************
     * Set the renderers for each table column.
     *
     * @param table
     * @param model
     */

    private synchronized void initColumnRenderers(final JTable table,
                                                  final ReportTableModel model)
        {
        final TableCellRenderer headerRenderer;
        TableColumn tableColumn;
        Component cellComponent;
        final Object[] columnOccupants;
        int headerWidth;
        int cellPreferredWidth;

        intPreferredWidth = 0;
        columnOccupants = model.getColumnWidths();
        headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < model.getColumnCount(); i++)
            {
            // Find the required width of the Header
            tableColumn = table.getColumnModel().getColumn(i);
            cellComponent = headerRenderer.getTableCellRendererComponent(null,
                                                                         tableColumn.getHeaderValue(),
                                                                         false,
                                                                         false,
                                                                         0,
                                                                         0);
            headerWidth = (int)cellComponent.getPreferredSize().getWidth();

            // Find the width of the column to accommodate the maximum size object
            final TableCellRenderer cellRenderer = table.getDefaultRenderer(model.getColumnClass(i));

            // Set alignment for this column, if applicable
//            if (cellRenderer instanceof DefaultTableCellRenderer)
//                {
//                ((DefaultTableCellRenderer)cellRenderer).setHorizontalAlignment(model.getColumnMetadata(i).getColumnAlignment());
//                }

            cellComponent = cellRenderer.getTableCellRendererComponent(table,
                                                                       columnOccupants[i],
                                                                       false,
                                                                       false,
                                                                       0,
                                                                       i);
            cellPreferredWidth = (int)cellComponent.getPreferredSize().getWidth();

            // Choose the largest width of the header or the cell's maximum object...
            tableColumn.setPreferredWidth(Math.max(headerWidth, cellPreferredWidth));

            // Try to work out how wide the overall table needs to be
            intPreferredWidth += tableColumn.getPreferredWidth();

//            LOGGER.log("==========================================================================");
//            LOGGER.log("HEADER WIDTH=" + headerWidth);
//            LOGGER.log("COLUMN=" + i);
//            LOGGER.log("CELL PREFERRED WIDTH=" + cellPreferredWidth);
//            LOGGER.log("PREFERRED COLUMN WIDTH=" + Math.max(headerWidth, cellPreferredWidth));
//            LOGGER.log("TOTAL PREFERRED WIDTH=" + intPreferredWidth);

//            DefaultTableCellRenderer cellRendererTest = new DefaultTableCellRenderer();
//            cellRendererTest.setHorizontalAlignment(model.getColumnMetadata(i).getColumnAlignment());
//            tableColumn.setCellRenderer(cellRendererTest);
            }
        }


    /**********************************************************************************************
     * Find out how wide the table needs to be.
     *
     * @return int
     */

    public synchronized final int getPreferredWidth()
        {
        return (this.intPreferredWidth);
        }


    /*********************************************************************************************/
    /* Printing                                                                                  */
    /**********************************************************************************************
     * Print the Report on a separate SwingWorker Thread,
     * which allows us to see when it is finished.
     * ToDo what happens when this Thread is finished??
     */

    public synchronized final void printReport()
        {
        final SwingWorker workerPrinter;

        workerPrinter = new SwingWorker(REGISTRY.getThreadGroup(),
                                        "SwingWorker Printer "
                                            + "[plugin=" + getTask().getParentAtom().getName() + "]"
                                            + "[task=" + getTask().getName() + "]"
                                            + "[report=" + getReportUniqueName() + "]")
            {
            public Object construct()
                {
                LOGGER.debugTimerTick("ReportTable(Print): SwingWorker");
                // Let the user know what happened
                return (printDialog());
                }

            // Display updates occur on the Event Dispatching Thread
            public void finished()
                {
                final String [] strSuccess =
                    {
                    MSG_REPORT_PRINTED,
                    MSG_PRINT_CANCELLED
                    };

                if ((get() != null)
                    && (get() instanceof Boolean)
                    && ((Boolean) get())
                    && (getTask() != null)
                    && (FrameworkDatabase.getInstance() != null)
                    && (!isStopping()))
                    {
                    // Log the reports operation
                    LOGGER.logAtomEvent(getTask().getParentAtom(),
                                        getTask(),
                                        ReportTable.class.getName(),
                                        METADATA_PRINT_REPORT
                                            + SPACE
                                            + METADATA_NAME
                                            + getReportUniqueName()
                                            + TERMINATOR,
                                        EventStatus.INFO);
                    JOptionPane.showMessageDialog(null,
                                                  strSuccess[0],
                                                  DIALOG_PRINT + SPACE + getReportUniqueName(),
                                                  JOptionPane.INFORMATION_MESSAGE);
                    }
                else
                    {
                    JOptionPane.showMessageDialog(null,
                                                  strSuccess[1],
                                                  DIALOG_PRINT + SPACE + getReportUniqueName(),
                                                  JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            };

        // Start the reports Thread
        workerPrinter.start();
        }


    /**********************************************************************************************
     * Print this report!
     *
     * @return boolean
     */

    private synchronized boolean printDialog()
        {
        try
            {
            final PrinterJob jobPrinter;
            final PageFormat pageFormat;
            final PageFormat pageFormatNew;
//            final Paper paperPrint;

//            LOGGER.debug("\nprintDialog()");

            // Start a PrinterJob on the selected Printer
            jobPrinter = PrinterJob.getPrinterJob();

            // Adjust the Orientation as required
//            pageFormat = getPageFormat();
//            pageFormat.setOrientation(getOrientation());
//
//            pageFormatNew = jobPrinter.pageDialog(pageFormat);
//            setPageFormat(pageFormatNew);

            jobPrinter.setPrintable(this, getPageFormat());

            if (!jobPrinter.printDialog())
                {
                // Leave if the User cancels the dialog
                return (false);
                }

            // There is only one page until we calculate otherwise
            intMaxPages = 1;
            intRowsPerFirstPage = 0;

            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            jobPrinter.print();
            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

        catch (final PrinterException exception)
            {
            REGISTRY_MODEL.getUserInterface().getUI().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            getTask().handleException(exception,
                                      "printDialog()",
                                      EventStatus.WARNING);
            }

        return (true);
        }

    //            // Work out the ImageableArea
    //            paperPrint = pageFormat.getPaper();
    //
    //            // Override the printer default page size if required
    //            if (!getDefaultPage())
    //                {
    //                paperPrint.setSize(Utilities.to_72nd_inch(getPageWidth()), Utilities.to_72nd_inch(getPageHeight()));
    //                }
    //
    //            paperPrint.setImageableArea(Utilities.to_72nd_inch(getLeftMargin()),
    //                                        Utilities.to_72nd_inch(getTopMargin()),
    //                                        paperPrint.getWidth() - Utilities.to_72nd_inch(getLeftMargin()) - Utilities.to_72nd_inch(getRightMargin()),
    //                                        paperPrint.getHeight() - Utilities.to_72nd_inch(getTopMargin()) - Utilities.to_72nd_inch(getBottomMargin()));
    //            pageFormat.setPaper(paperPrint);



    /*********************************************************************************************/
    /* The Printable Interface                                                                   */
    /**********************************************************************************************
     * Render the Report pages on the printer.
     *
     * @param graphics
     * @param pageformat
     * @param pageIndex
     *
     * @return int
     */

    public synchronized final int print(final Graphics graphics,
                                        final PageFormat pageformat,
                                        final int pageIndex)
        {
        final TableColumnModel modelColumn;
        TableColumn columnTable;
        FontMetrics fontMetrics;
        final Font fontPrintHeader;
        final int intPageWidth;
        final int intPageHeight;
        int cursorY;
        int intAscent;
        int intColumnCount;
        int intRow;
        int intCol;
        int intWidth;
        final int[] arrayColumnX ;
        final int intRowHeight;
        final int intRowsPerPage;
        final int intRowStart;
        final int intRowEndCount;
        String strColumnTitle;
        int intTotalColumnWidth;

//        LOGGER.debug("Page index=" + pageIndex);

        if (pageIndex >= intMaxPages)
            {
//            LOGGER.debug("Printable.NO_SUCH_PAGE");
            return (Printable.NO_SUCH_PAGE);
            }

        // Set the reports orientation
       // pageFormat.setOrientation(getOrientation());

        // Page Format
        LOGGER.debug("Sizes derived from Paper");
        LOGGER.debug("Paper width=" + Utilities.to_mm(pageformat.getPaper().getWidth()));
        LOGGER.debug("Paper height=" + Utilities.to_mm(pageformat.getPaper().getHeight()));
        LOGGER.debug("Paper imageable width=" + Utilities.to_mm(pageformat.getPaper().getImageableWidth()));
        LOGGER.debug("Paper imageable height=" + Utilities.to_mm(pageformat.getPaper().getImageableHeight()));
        LOGGER.debug("Paper imageable X=" + Utilities.to_mm(pageformat.getPaper().getImageableX()));
        LOGGER.debug("Paper imageable Y=" + Utilities.to_mm(pageformat.getPaper().getImageableY()));

        LOGGER.debug("Sizes derived from Page Format");
        LOGGER.debug("Page Format width=" + Utilities.to_mm(pageformat.getWidth()));
        LOGGER.debug("Page Format height=" + Utilities.to_mm(pageformat.getHeight()));
        LOGGER.debug("Page Format imageable width=" + Utilities.to_mm(pageformat.getImageableWidth()));
        LOGGER.debug("Page Format imageable height=" + Utilities.to_mm(pageformat.getImageableHeight()));
        LOGGER.debug("Page Format imageable X=" + Utilities.to_mm(pageformat.getImageableX()));
        LOGGER.debug("Page Format imageable Y=" + Utilities.to_mm(pageformat.getImageableY()));

        // Map the graphics origin on to the Printer
        graphics.translate((int) pageformat.getImageableX(),
                           (int) pageformat.getImageableY());

        intPageWidth = (int) pageformat.getImageableWidth();
        intPageHeight = (int) pageformat.getImageableHeight();

        graphics.setClip(0, 0, intPageWidth, intPageHeight);

        LOGGER.debug("After translation");
        LOGGER.debug("Page Format imageable width=" + Utilities.to_mm(pageformat.getImageableWidth()));
        LOGGER.debug("Page width=" + Utilities.to_mm((double)intPageWidth) + "mm");
        LOGGER.debug("Page Format imageable height=" + Utilities.to_mm(pageformat.getImageableHeight()));
        LOGGER.debug("Page height=" + Utilities.to_mm((double)intPageHeight) + "mm");

        // All reports are black for now!
        graphics.setColor(Color.blue);

        // Draw a border if required
        if (getEnableBorder())
            {
            graphics.drawLine(0, 0, 0, intPageHeight);
            graphics.drawLine(0, 0, intPageWidth, 0);
            graphics.drawLine(intPageWidth, 0, intPageWidth, intPageHeight);
            graphics.drawLine(0, intPageHeight, intPageWidth, intPageHeight);
            }

        // Set the Font for the header lines
//        graphics.setFont(getHeaderFont().getFont().deriveFont((float)(getHeaderFont().getFont().getSize() - 2)));
        graphics.setFont(getHeaderFont().getFont());
        fontMetrics = graphics.getFontMetrics();
        intAscent = fontMetrics.getAscent();

        // Start off the Y cursor
        // Start at ascent of header font because of baseline positioning
        cursorY = intAscent;
//        LOGGER.debug("header intAscent=" + intAscent);

        // Generate the very latest Header
        // This was missing!
        modelReport.setHeader(generateHeader());

        // Render the full header for the first page only
        if (pageIndex == 0)
            {
            // The returned cursor is the position for the *next* output
            cursorY = printFullHeader(tableReport,
                                      modelReport,
                                      graphics,
                                      cursorY,
                                      intPageWidth);
            }
        else
            {
            // Subsequent pages show the first line of the Header and a Page Number
            cursorY = printSimpleHeader(tableReport,
                                        modelReport,
                                        graphics,
                                        cursorY,
                                        intPageWidth,
                                        pageIndex);
            }

        // Change Font for the JTable column titles
//        fontHeader = getReportFont().getFont().deriveFont(Font.BOLD);
        fontPrintHeader = getReportFont().getFont().deriveFont((float)(getReportFont().getFont().getSize() - 3));
        fontPrintHeader.deriveFont(Font.BOLD);
        graphics.setFont(fontPrintHeader);
        fontMetrics = graphics.getFontMetrics();
        intAscent = fontMetrics.getAscent();
//        LOGGER.debug("column header intAscent=" + intAscent);

        modelColumn = tableReport.getColumnModel();
        intColumnCount = modelColumn.getColumnCount();
        //LOGGER.debug("column count=" + intColumnCount);

        intTotalColumnWidth = modelColumn.getTotalColumnWidth();
        LOGGER.debug("total column width=" + intTotalColumnWidth);
        LOGGER.debug("total column width mm=" + Utilities.to_mm(intTotalColumnWidth));

        // Work out the X-coord of each column's upper-left corner
        arrayColumnX = new int[intColumnCount];

        // Column zero is left-aligned and indented
        arrayColumnX[0] = TABLE_INDENT;

        // Adjust the total width to include the Indents
        intTotalColumnWidth += TABLE_INDENT << 1;

        //------------------------------------------------------------------------------------------
        // Render all the column headers that will fit into the page width

        // Add ascent of header font because of baseline positioning
        cursorY += intAscent;

        for (intCol = 0; intCol < intColumnCount; intCol++)
            {
            // Get the width of the current column in page units
            // and scale for the page size
            columnTable = modelColumn.getColumn(intCol);
            intWidth = ReportTableHelper.scale(intPageWidth,
                                               intTotalColumnWidth,
                                               columnTable.getWidth());

//            LOGGER.debug("column=" + intCol);
//            LOGGER.debug("page width=" + intPageWidth);
//            LOGGER.debug("mm page width=" + to_mm(intPageWidth));
//
//            LOGGER.debug("pg column width=" + intWidth);
//            LOGGER.debug("mm column width=" + to_mm(intWidth));
//
//            LOGGER.debug("pg accum width=" + (arrayColumnX[intCol] + intWidth));
//            LOGGER.debug("mm accum width=" + to_mm(arrayColumnX[intCol] + intWidth));

            // Can we render this column or have we exceeded the page width?
            if (arrayColumnX[intCol] + intWidth > intPageWidth)
                {
                // This should never happen with scaled rendering!
                LOGGER.debug("page width exceeded, limiting column count");
                LOGGER.debug("columns=" + (arrayColumnX[intCol] + intWidth));
                LOGGER.debug("intPageWidth=" + intPageWidth);
                // If so redefine the max column count
                intColumnCount = intCol;
                break;
                }

            if (intCol + 1 < intColumnCount)
                {
//                LOGGER.debug("add next column");
                // Record the location of the next column
                // So that we know where to draw the cell contents
                arrayColumnX[intCol + 1] = arrayColumnX[intCol] + intWidth;
                }

            // Render the table column header for the current column
            strColumnTitle = (String)columnTable.getIdentifier();

            // Prune the length of the String until it fits!
            ReportTableHelper.drawScaledText(graphics,
                                             new StringBuffer(Utilities.stripHTML(strColumnTitle)),
                                             intWidth,
                                             arrayColumnX[intCol],
                                             cursorY);
            }

        // Change Font for the JTable data
        // Beware the cast to float to get the new size!
        graphics.setFont(getReportFont().getFont().deriveFont((float)(getReportFont().getFont().getSize() - 4)));
        fontMetrics = graphics.getFontMetrics();
        intAscent = fontMetrics.getHeight();

        // Calculate the area left for display of rows
        // The minimum row height must allow for icons
        intRowHeight = Math.max(intAscent, MIN_ROW_HEIGHT);
//        intRowHeight = (int)(intAscent * 1.5);
//        intRowHeight = intAscent;
//        LOGGER.debug("page height=" + intPageHeight);
//        LOGGER.debug("row height=" + intRowHeight);

        // Work out how many rows per first page
        if (pageIndex == 0)
            {
            intRowsPerFirstPage = (intPageHeight - cursorY) / intRowHeight;
            LOGGER.debug("rows per first page=" + intRowsPerFirstPage);
            }

        // Work out how many rows per subsequent pages
        // Allow for the column headers
        intRowsPerPage = (intPageHeight - intRowHeight) / intRowHeight;
        LOGGER.debug("rows per subsequent page=" + intRowsPerPage);

        // Work out how many pages (there must be at least one)
        // RPFP + (n-1) RPP = Total
        // n = ((Total - RPFP) / RPP) + 1
        intMaxPages = Math.max((int) Math.ceil(((tableReport.getRowCount() - intRowsPerFirstPage) /
                                               (double) intRowsPerPage) + 1), 1);
//        LOGGER.debug("number of pages=" + intMaxPages);

        // Index into the TabelModel rows for this specific page
        if (pageIndex == 0)
            {
//            LOGGER.debug("first page");
            intRowStart = 0;
//            LOGGER.debug("row start=" + intRowStart);

            // Never move past the end of the model!
            intRowEndCount = Math.min(tableReport.getRowCount(),
                                      intRowStart + intRowsPerFirstPage);
//            LOGGER.debug("row end count=" + intRowEndCount);
            }
        else
            {
//            LOGGER.debug("subsequent page");
            intRowStart = intRowsPerFirstPage + ((pageIndex-1) * intRowsPerPage);
//            LOGGER.debug("row start=" + intRowStart);

            // Never move past the end of the model!
            intRowEndCount = Math.min(tableReport.getRowCount(),
                                      intRowStart + intRowsPerPage);
//            LOGGER.debug("row end count=" + intRowEndCount);
            }

        //------------------------------------------------------------------------------------------
        // Render the rows on this page

        for (intRow = intRowStart;
             intRow < intRowEndCount;
             intRow++)
            {
            cursorY += intRowHeight;

            // Render each column in turn
            for (intCol = 0;
                 intCol < intColumnCount;
                 intCol++)
                {
                final Object objCell;
                final int intScaledColumnWidth;

                objCell = modelReport.getValueAt(intRow,
                                                 tableReport.getColumnModel().getColumn(intCol).getModelIndex());

                intScaledColumnWidth = ReportTableHelper.scale(intPageWidth,
                                                               intTotalColumnWidth,
                                                               modelColumn.getColumn(
                                                                       intCol).getWidth());

                if ((objCell instanceof String)
                    || (objCell instanceof StringBuffer))
                    {
                    // Prune the length of the String until it fits!
                    ReportTableHelper.drawScaledText(graphics,
                                                     new StringBuffer(Utilities.stripHTML(
                                                             objCell.toString()).trim()),
                                                     intScaledColumnWidth,
                                                     arrayColumnX[intCol],
                                                     cursorY);
                    }
                else if (objCell instanceof ImageIcon)
                    {
                    final ImageIcon imageIcon;
                    final double dblAspectRatio;

                    imageIcon = (ImageIcon)objCell;
                    dblAspectRatio = (double)imageIcon.getIconWidth() / (double)imageIcon.getIconHeight();

                    // (x, y) is the top-left corner of the image reports position
                    // So align the baseline of the icon with the text baseline

//                    graphics.drawLine(arrayColumnX[intCol],
//                                      cursorY - ((int)((intScaledColumnWidth*0.85) / dblAspectRatio)),
//                                      arrayColumnX[intCol] + (int)(intScaledColumnWidth*0.85),
//                                      cursorY);
//                    graphics.drawLine(arrayColumnX[intCol],
//                                      cursorY - intRowHeight,
//                                      arrayColumnX[intCol] + (int)(intScaledColumnWidth*0.9),
//                                      cursorY);

                    graphics.drawImage(imageIcon.getImage(),
                                       arrayColumnX[intCol],
                                       cursorY - ((int)((intScaledColumnWidth*0.9) / dblAspectRatio)),
                                       (int)(intScaledColumnWidth*0.9),
                                       (int)((intScaledColumnWidth*0.9) / dblAspectRatio),
                                       tableReport);
                    }
                else if (objCell instanceof Boolean)
                    {
                    if ((Boolean) objCell)
                        {
                        graphics.drawString("Y",
                                            arrayColumnX[intCol],
                                            cursorY);
                        }
                    else
                        {
                        graphics.drawString("N",
                                            arrayColumnX[intCol],
                                            cursorY);
                        }
                    }
                else if (objCell == null)
                    {
                    graphics.drawString(SPACE,
                                        arrayColumnX[intCol],
                                        cursorY);
                    }
                else
                    {
                     graphics.drawString(QUERY,
                                         arrayColumnX[intCol],
                                         cursorY);
                    }
                }
            }

        return (Printable.PAGE_EXISTS);
        }


    /***********************************************************************************************
     * Print the Table Header on the specified graphics context,
     * starting at the specified cursor Y position.
     * Print any associated PrintableIcons.
     * Return the cursor Y position for the next printed output.
     *
     * @param table
     * @param model
     * @param graphics
     * @param cursorY
     * @param pagewidth
     *
     * @return int
     */

    private synchronized int printFullHeader(final JTable table,
                                             final ReportTableModel model,
                                             final Graphics graphics,
                                             final int cursorY,
                                             final int pagewidth)
        {
        final ImageIcon iconLogo;
        final Iterator<String> iteratorHeader;
        Iterator<PrintableIconInterface> iteratorIcons;
        final int intOriginalCursorY;
        final int intLogoCursorY;
        int intTextCursorY;
        int intIconCursorY;
        int intLastCursorY;
        FontMetrics fontMetrics;
        Rectangle2D rectText;
        int intTextMaxWidth;
        final int intAscent;

        if ((table != null)
            && (model != null)
            && (graphics != null)
            && (cursorY >= 0)
            && (pagewidth > 0))
            {
            fontMetrics = getFontMetrics(graphics.getFont());
            intAscent = fontMetrics.getAscent();

            intOriginalCursorY = cursorY;
            intTextCursorY = cursorY + HEADER_INDENT;
            intIconCursorY = cursorY + HEADER_INDENT;
            intTextMaxWidth = HEADER_INDENT;

            //--------------------------------------------------------------------------------------
            // Application Logo

            iconLogo = RegistryModelUtilities.getCommonIcon(HEADER_ICON_FILENAME);

            if (iconLogo != null)
                {
                graphics.drawImage(iconLogo.getImage(),
                                   HEADER_INDENT,
                                   HEADER_INDENT,
                                   iconLogo.getIconWidth(),
                                   iconLogo.getIconHeight(),
                                   table);
                intLogoCursorY = iconLogo.getIconHeight() + HEADER_INDENT;
                }
            else
                {
                intLogoCursorY = 0;
                }

            //--------------------------------------------------------------------------------------
            // Print the Header text, if any
            // Allow HEADER_ROWS_PER_COLUMN lines per column, offsetting columns by HEADER_COLUMN_WIDTH

            if ((model.getHeader() != null)
                && (!model.getHeader().isEmpty()))
                {
                String strHeaderText;
                Font fontInUse;
                int intLineIndex;

                iteratorHeader = model.getHeader().iterator();
                intLineIndex = 0;

                while ((iteratorHeader != null)
                    && (iteratorHeader.hasNext()))
                    {
                    strHeaderText = iteratorHeader.next();

                    // Use a bold font for the first line only
                    if (intLineIndex == 0)
                        {
                        fontInUse = graphics.getFont();
                        graphics.setFont(fontInUse.deriveFont(Font.BOLD));

                        graphics.drawString(strHeaderText,
                                            HEADER_OFFSET,
                                            intTextCursorY);

                        // Find the length of the longest line...
                        rectText = fontMetrics.getStringBounds(strHeaderText, graphics);
                        intTextMaxWidth = Math.max(intTextMaxWidth, (int)rectText.getWidth());

                        graphics.setFont(fontInUse);
                        }
                    else if (intLineIndex < HEADER_ROWS_PER_COLUMN)
                        {
                        // Which column should we write into? (Assume that there are only two)
                        graphics.drawString(strHeaderText,
                                            HEADER_OFFSET,
                                            intTextCursorY);

                        // Find the length of the longest line...
                        rectText = fontMetrics.getStringBounds(strHeaderText, graphics);
                        intTextMaxWidth = Math.max(intTextMaxWidth, (int)rectText.getWidth());
                        }
                    else if (intLineIndex == HEADER_ROWS_PER_COLUMN)
                        {
                        // Which column should we write into? (Assume that there are only two)
                        intTextCursorY = intOriginalCursorY + HEADER_INDENT;

                        graphics.drawString(strHeaderText,
                                            HEADER_OFFSET + HEADER_COLUMN_WIDTH,
                                            intTextCursorY);

                        // Find the length of the longest line...
                        rectText = fontMetrics.getStringBounds(strHeaderText, graphics);
                        intTextMaxWidth = Math.max(intTextMaxWidth, (int)rectText.getWidth());
                        }
                    else
                        {
                        graphics.drawString(strHeaderText,
                                            HEADER_OFFSET + HEADER_COLUMN_WIDTH,
                                            intTextCursorY);

                        // Find the length of the longest line...
                        rectText = fontMetrics.getStringBounds(strHeaderText, graphics);
                        intTextMaxWidth = Math.max(intTextMaxWidth, (int)rectText.getWidth());
                        }

                    intTextCursorY += intAscent;
                    intLineIndex++;
                    }

                // The last line of text defines the lower edge of the Text area, if any
                }

            //--------------------------------------------------------------------------------------
            // Now print the Icons, if any

            if ((model.getIcons() != null)
                && (!model.getIcons().isEmpty()))
                {
                int intCursorX;
                int intTotalIconWidth;
                double dblScaleFactor;

                // Firstly scan the Icons to get their total width
                // so we can scale them to fit in the available page width
                iteratorIcons = model.getIcons().iterator();

                // Avoid division by zero!
                intTotalIconWidth = 1;

                while ((iteratorIcons != null)
                    && (iteratorIcons.hasNext()))
                    {
                    final PrintableIconInterface iconInterface;
                    final ImageIcon imageIcon;

                    iconInterface = iteratorIcons.next();

                    if (iconInterface != null)
                        {
                        imageIcon = RegistryModelUtilities.getCommonIcon(iconInterface.getLogoFilename());

                        // Accumulate the Icon widths, taking into account the layout spacers
                        if ((imageIcon != null)
                            && (imageIcon.getImage() != null))
                            {
                            intTotalIconWidth += imageIcon.getIconWidth() + HEADER_ICON_GAP;
                            }
                        }
                    }

                // Calculate the Icon scale factor to make the Icons fit on the page
                // A bit of a bodge to make room for the bold first line?
                intCursorX = intTextMaxWidth + (HEADER_INDENT << 2);
                dblScaleFactor = (double)(pagewidth - intCursorX) / (double)intTotalIconWidth;

                // Don't make the logos any larger than they really are!
                dblScaleFactor = Math.min(dblScaleFactor, 1.0);

                // Scan the Icons again, printing each one
                iteratorIcons = model.getIcons().iterator();

                while ((iteratorIcons != null)
                    && (iteratorIcons.hasNext()))
                    {
                    final PrintableIconInterface iconInterface;
                    final ImageIcon imageIcon;
                    final String strWebsite;

                    iconInterface = iteratorIcons.next();

                    if (iconInterface != null)
                        {
                        imageIcon = RegistryModelUtilities.getCommonIcon(iconInterface.getLogoFilename());
                        strWebsite = iconInterface.getWebsite();

                        if ((imageIcon != null)
                            && (imageIcon.getImage() != null))
                            {
                            final int intIconHeight;
                            final int intIconWidth;

                            intIconHeight = (int)(imageIcon.getIconHeight() * dblScaleFactor);
                            intIconWidth = (int)(imageIcon.getIconWidth() * dblScaleFactor);

                            graphics.drawImage(imageIcon.getImage(),
                                               intCursorX,
                                               intOriginalCursorY,
                                               intIconWidth,
                                               intIconHeight,
                                               table);

                            // Print the Website URL if possible
                            if ((strWebsite != null)
                                && (!strWebsite.trim().equals(EMPTY_STRING)))
                                {
                                final Font fontInUse;
                                Font fontURL;

                                // Save a reference to the existing Font
                                fontInUse = graphics.getFont();
                                fontURL = fontInUse;

                                // See if the URL can fit below the Icon!
                                do
                                    {
                                    // Remember to cast to a float in order to indicate that it is a size!
                                    fontURL = fontURL.deriveFont((float)(fontURL.getSize() - 1));
                                    fontMetrics = getFontMetrics(fontURL);
                                    rectText = fontMetrics.getStringBounds(strWebsite.trim(), graphics);
                                    }
                                while((rectText.getWidth() > intIconWidth)
                                    && (fontURL.getSize() > 4));

                                graphics.setFont(fontURL);
                                graphics.drawString(strWebsite.trim(),
                                                    intCursorX,
                                                    intIconHeight + intAscent + HEADER_VERTICAL_GAP);
                                graphics.setFont(fontInUse);
                                }

                            // Move to the position for the next Icon in the list
                            intCursorX += intIconWidth + (HEADER_ICON_GAP * dblScaleFactor);

                            // Record the lowest part of each Icon
                            intIconCursorY = Math.max(intIconCursorY,
                                                      intIconHeight + intAscent);

                            // Do the same as for Text rendering
                            intIconCursorY += intAscent;
                            }
                        }
                    }
                }

            intLastCursorY = Math.max(intTextCursorY, intIconCursorY);
            intLastCursorY = Math.max(intLastCursorY, intLogoCursorY);
            intLastCursorY = intLastCursorY + HEADER_INDENT;

            // Line and space between title header and table headers
            // at the lowest point reached during rendering
            graphics.drawLine(0,
                              intLastCursorY,
                              pagewidth,
                              intLastCursorY);

            return (intLastCursorY + HEADER_VERTICAL_GAP);
            }
        else
            {
            // Nothing to do, so just return the original cursorY
            return (cursorY);
            }
        }


    /***********************************************************************************************
     * Print the first line of the Table Header and a Page Number
     * on the specified graphics context, starting at the specified cursor Y position.
     * Return the cursor Y position for the next printed output.
     *
     * @param table
     * @param model
     * @param graphics
     * @param cursorY
     * @param pagewidth
     * @param pagenumber
     *
     * @return int
     */

    private synchronized int printSimpleHeader(final JTable table,
                                               final ReportTableModel model,
                                               final Graphics graphics,
                                               final int cursorY,
                                               final int pagewidth,
                                               final int pagenumber)
        {
        int intTextCursorY;
        final int intAscent;
        final String strPageNumber;
        final FontMetrics fontMetrics;
        final Rectangle2D rectText;

        if ((table != null)
            && (model != null)
            && (graphics != null)
            && (cursorY >= 0)
            && (pagewidth > 0)
            && (pagenumber > 0))
            {
            fontMetrics = getFontMetrics(graphics.getFont());
            intAscent = fontMetrics.getAscent();
            intTextCursorY = cursorY + HEADER_INDENT;

            // Print the first line of the Header text, if any
            if ((model.getHeader() != null)
                && (!model.getHeader().isEmpty()))
                {
                final Font fontInUse;

                // Save a reference to the existing Font
                fontInUse = graphics.getFont();

                // We only need the first line - there must be at least one entry
                graphics.setFont(fontInUse.deriveFont(Font.BOLD));
                graphics.drawString(model.getHeader().get(0),
                                    HEADER_INDENT,
                                    intTextCursorY);
                graphics.setFont(fontInUse);
                }

            // Now draw the Page Number
            // Remember that PageIndex starts at 0
            strPageNumber = MSG_PAGE_NUMBER + SPACE + Integer.toString(pagenumber + 1);

            // Find the length of the page number text
            rectText = fontMetrics.getStringBounds(strPageNumber, graphics);

            // Print the page Number set back from the right hand side
            graphics.drawString(strPageNumber,
                                pagewidth - HEADER_INDENT - (int)rectText.getWidth(),
                                intTextCursorY);

            // Line and space between title header and table headers
            // at the lowest point reached during rendering
            intTextCursorY += intAscent;
            graphics.drawLine(0,
                              intTextCursorY,
                              pagewidth,
                              intTextCursorY);

            return (intTextCursorY + HEADER_VERTICAL_GAP);
            }
        else
            {
            // Nothing to do, so just return the original cursorY
            return (cursorY);
            }
        }


    /**********************************************************************************************
     * Generate the PrintableIcons for the Header.
     *
     * @return Vector
     *
     * @throws ReportException
     */

    public synchronized final Vector<PrintableIconInterface> generateIcons() throws ReportException
        {
        return (new Vector<PrintableIconInterface>(1));
        }


    /***********************************************************************************************
     * Get the JTable on which the Report is based.
     *
     * @return JTable
     */

    public synchronized final JTable getReportTable()
        {
        return (this.tableReport);
        }


    /***********************************************************************************************
     *
     * @return JScrollPane
     */

    public synchronized final JScrollPane getScrollPane()
        {
        return scrollPane;
        }


    /***********************************************************************************************
     *
     * @param pane
     */

    public synchronized final void setScrollPane(final JScrollPane pane)
        {
        this.scrollPane = pane;
        }


    /***********************************************************************************************
     *
     * @return JPanel
     */

    public synchronized final JPanel getTablePanel()
        {
        return panelTable;
        }


    /***********************************************************************************************
     *
     * @param panel
     */

    public synchronized final void setTablePanel(final JPanel panel)
        {
        this.panelTable = panel;
        }


    /***********************************************************************************************
     * Render this Table as HTML.
     *
     * @param border
     * @param cssHeader
     * @param cssTitles
     * @param cssCells
     * @param imageroot
     *
     * @return String
     */

    public synchronized final String toHTML(final boolean border,
                                            final String cssHeader,
                                            final String cssTitles,
                                            final String cssCells,
                                            final String imageroot)
        {
        final String strBorder;
        final String strCssHeader;
        final String strCssTitles;
        final String strCssCells;
        String strTemp;
        final StringBuffer bufferHTML;
        final TableColumnModel modelColumn;
        TableColumn columnTable;
        final int intColumnCount;
        int intRow;
        int intCol;

        // Control the borders (mainly for testing)
        if (border)
            {
            strBorder = "border=1";
            }
        else
            {
            strBorder = "";
            }

        // Set the Header style
        if ((cssHeader == null) || (cssHeader.equals("")))
            {
            strCssHeader = "";
            }
        else
            {
            strCssHeader = " class=" + cssHeader + SPACE;
            }

        // Set the Titles style
        if ((cssTitles == null) || (cssTitles.equals("")))
            {
            strCssTitles = "";
            }
        else
            {
            strCssTitles = " class=" + cssTitles + SPACE;
            }

        // Set the Cell style
        if ((cssCells == null) || (cssCells.equals("")))
            {
            strCssCells = "";
            }
        else
            {
            strCssCells = " class=" + cssCells + SPACE;
            }

        // Count the columns we have to render...
        modelColumn = tableReport.getColumnModel();
        intColumnCount = modelColumn.getColumnCount();

        // Start an HTML table
        bufferHTML = new StringBuffer();
        bufferHTML.append("<table cellspacing=0 cellpadding=0>" + ReportTableHelper.CR_LF);
        bufferHTML.append("<tr><td>&nbsp;</td></tr>" + ReportTableHelper.CR_LF);

        // Render the header lines (if any)
        // These are always left-aligned
        final Iterator iteratorHeader = modelReport.getHeader().iterator();

        while (iteratorHeader.hasNext())
            {
            strTemp = Utilities.stripHTML(((String)iteratorHeader.next()).trim());

            // Protect against empty table cells...
            if ((strTemp == null)
                || (strTemp.equals("")))
                {
                strTemp = "&nbsp;";
                }

            bufferHTML.append("<tr>" + ReportTableHelper.CR_LF + "<td" + strCssHeader + " colspan=" + intColumnCount + ">" + ReportTableHelper.CR_LF);
            bufferHTML.append(strTemp);
            bufferHTML.append(
                    ReportTableHelper.CR_LF + "</td>" + ReportTableHelper.CR_LF + "</tr>" + ReportTableHelper.CR_LF);
            }

        bufferHTML.append("<tr><td>&nbsp;</td></tr>" + ReportTableHelper.CR_LF);
        bufferHTML.append("</table>" + ReportTableHelper.CR_LF);

        // Render all the column headers
        // These are always left-aligned
        bufferHTML.append("<table cellspacing=2 cellpadding=0 " + strBorder + ">" + ReportTableHelper.CR_LF);
        bufferHTML.append("<tr>" + ReportTableHelper.CR_LF);

        for (intCol = 0; intCol < intColumnCount; intCol++)
            {
            columnTable = modelColumn.getColumn(intCol);
            strTemp = Utilities.stripHTML(((String)columnTable.getIdentifier()).trim());

            // Protect against empty table cells...
            if ((strTemp == null)
                || (strTemp.equals("")))
                {
                strTemp = "&nbsp;";
                }

            ReportTableHelper.renderHTMLCell(bufferHTML, strTemp, strCssTitles, SwingConstants.LEFT);
            }
        bufferHTML.append("</tr>" + ReportTableHelper.CR_LF);

        // Render the column contents
        for (intRow = 0; intRow < tableReport.getRowCount(); intRow++)
            {
            bufferHTML.append("<tr>" + ReportTableHelper.CR_LF);
            for (intCol = 0; intCol < intColumnCount; intCol++)
                {
                final Object objCell;

                objCell = modelReport.getValueAt(intRow,
                                                 tableReport.getColumnModel().getColumn(intCol).getModelIndex());

                if ((objCell instanceof String)
                    || (objCell instanceof StringBuffer))
                    {
                    final int intAlignment;

                    strTemp = (Utilities.stripHTML(objCell.toString()).trim());

                    // Protect against empty table cells...
                    if ((strTemp == null)
                        || (strTemp.equals("")))
                        {
                        strTemp = "&nbsp;";
                        }

                    // Set the column alignment
                    intAlignment = ((ReportTableModel)tableReport.getModel()).getColumnMetadata(intCol).getColumnAlignment();

                    ReportTableHelper.renderHTMLCell(bufferHTML, strTemp, strCssCells, intAlignment);
                    }
                // HTML can't cope with showing an Icon in this way...
//                else if (objCell instanceof ImageIcon)
//                    {
//                    //((ImageIcon)objCell).getImageData().
//                    renderHTMLCell(bufferHTML, "<center><img src=" + imageroot + "></center>" + CR_LF, strCssCells);
//                    }
                else if (objCell instanceof FlagIcon)
                    {
                    ReportTableHelper.renderHTMLCell(bufferHTML,
                                                     "<center><img src=" + imageroot + ((FlagIcon) objCell).getFilename() + "></center>" + ReportTableHelper.CR_LF,
                                                     strCssCells, SwingConstants.CENTER);
                    }
                else if (objCell instanceof Boolean)
                    {
                    // Checkboxes can only be off or on
                    // These are always centered
                    if ((Boolean) objCell)
                        {
                        ReportTableHelper.renderHTMLCell(bufferHTML,
                                                         "<img src=" + imageroot + "on.gif>" + ReportTableHelper.CR_LF,
                                                         strCssCells, SwingConstants.CENTER);
                        }
                    else
                        {
                        ReportTableHelper.renderHTMLCell(bufferHTML,
                                                         "<img src=" + imageroot + "off.gif>" + ReportTableHelper.CR_LF,
                                                         strCssCells, SwingConstants.CENTER);
                        }
                    }
                else if (objCell == null)
                    {
                    ReportTableHelper.renderHTMLCell(bufferHTML, SPACE, strCssCells,
                                                     SwingConstants.CENTER);
                    }
                else
                    {
                    // We don't know what we are trying to render
                    // ReportIcons come here too...
                    ReportTableHelper.renderHTMLCell(bufferHTML, "??", strCssCells,
                                                     SwingConstants.CENTER);
                    }
                }
            bufferHTML.append("</tr>" + ReportTableHelper.CR_LF);
            }
        bufferHTML.append("</table>" + ReportTableHelper.CR_LF);

        return (bufferHTML.toString());
        }


    /***********************************************************************************************
     * Show the column alignments, for debugging.
     *
     * @return String
     */

    private String showColumnAlignments()
        {
        final int intColumnCount;
        int intCol;
        int intAlignment;
        String strAlignments;

        intColumnCount = tableReport.getColumnModel().getColumnCount();
        strAlignments= "Column Alignments\n ";

        for (intCol = 0; intCol < intColumnCount; intCol++)
            {
            intAlignment = ((ReportTableModel)tableReport.getModel()).getColumnMetadata(intCol).getColumnAlignment();
            strAlignments += intAlignment + ", ";
            }

        return (strAlignments);
        }


    /***********************************************************************************************
     * Render this Table as plain text, usually for Export to a file.
     *
     * @param separator Usually Tab or Comma
     *
     * @return String
     */

    public synchronized final String toText(final char separator)
        {
        final StringBuffer bufferText;
        final TableColumnModel modelColumn;
        TableColumn columnTable;
        final int intColumnCount;
        int intRow;
        int intCol;

        // Count the columns we have to render...
        modelColumn = tableReport.getColumnModel();
        intColumnCount = modelColumn.getColumnCount();

        bufferText = new StringBuffer();

        // Render the header lines (if any)
        final Iterator iteratorHeader = modelReport.getHeader().iterator();

        while (iteratorHeader.hasNext())
            {
            bufferText.append(Utilities.stripHTML((String)iteratorHeader.next()));
            bufferText.append(ReportTableHelper.CR_LF);
            }

        // Render all the column headers
        for (intCol = 0; intCol < intColumnCount; intCol++)
            {
            columnTable = modelColumn.getColumn(intCol);
            bufferText.append(Utilities.stripHTML((String)columnTable.getIdentifier()));
            bufferText.append(separator);
            }
        bufferText.append(ReportTableHelper.CR_LF);

        // Render the column contents
        for (intRow = 0; intRow < tableReport.getRowCount(); intRow++)
            {
            for (intCol = 0; intCol < intColumnCount; intCol++)
                {
                final Object objCell;

                objCell = modelReport.getValueAt(intRow,
                                                 tableReport.getColumnModel().getColumn(intCol).getModelIndex());

                if ((objCell instanceof String)
                    || (objCell instanceof StringBuffer))
                    {
                    bufferText.append(Utilities.stripHTML(objCell.toString()));
                    }
                else if (objCell instanceof FlagIcon)
                    {
                    bufferText.append(((FlagIcon)objCell).getCountryCode());
                    }
                else if (objCell instanceof Boolean)
                    {
                    // Checkboxes can only be off or on
                    if ((Boolean) objCell)
                        {
                        bufferText.append("Y");
                        }
                    else
                        {
                        bufferText.append("N");
                        }
                    }
                else if (objCell == null)
                    {
                    bufferText.append(SPACE);
                    }
                else
                    {
                    // We don't know what we are trying to render
                    // ReportIcons come here too...
                    bufferText.append("??");
                    }
                bufferText.append(separator);
                }
            bufferText.append(ReportTableHelper.CR_LF);
            }

        return (bufferText.toString());
        }


    /***********************************************************************************************
     * Export the Header and Table to a file as Plain Text.
     *
     * @param outputfile
     * @param separator
     *
     * @throws ReportException
     */

    public final void exportText(final String outputfile,
                                 final char separator) throws ReportException
        {
        final String [] strTruncationWarning =
            {
            "The output will contain only those samples currently visible!"
            };

        final String [] strExportMessage =
            {
            MSG_EXPORTED + SPACE + outputfile
            };

        // Warn the User of truncation
        JOptionPane.showMessageDialog(null,
                                      strTruncationWarning,
                                      DIALOG_EXPORT,
                                      JOptionPane.WARNING_MESSAGE);

        ReportTableHelper.toFile(outputfile, toText(separator));

        // Let the user know where it went
        JOptionPane.showMessageDialog(null,
                                      strExportMessage,
                                      DIALOG_EXPORT,
                                      JOptionPane.INFORMATION_MESSAGE);
        }


    /***********************************************************************************************
     * Export the Header and Table to a file as formatted HTML.
     * If required, the HTML preamble and postamble may be set.
     * Don't tell the user that this has happened,
     * because it may be an automatic export via the Publisher.
     *
     * @param outputfile
     * @param border
     * @param cssHeader
     * @param cssTitles
     * @param cssCells
     * @param imageroot
     * @throws ReportException
     */

    public synchronized final void exportHTML(final String outputfile,
                                              final boolean border,
                                              final String cssHeader,
                                              final String cssTitles,
                                              final String cssCells,
                                              final String imageroot) throws ReportException
        {
        final String strTableData;

        strTableData = getHTMLPreamble()
                       + toHTML(border, cssHeader, cssTitles, cssCells, imageroot)
                       + getHTMLPostamble();

        ReportTableHelper.toFile(outputfile, strTableData);
        }


    /***********************************************************************************************
     * Get the HTML preamble with which to surround the exported table.
     * Overridden by subclasses as necessary.
     *
     * @return String
     */

    public final String getHTMLPreamble()
        {
        return ("");
        }


    /***********************************************************************************************
     * Get the HTML postamble with which to surround the exported table.
     * Overridden by subclasses as necessary.
     *
     * @return String
     */

    public final String getHTMLPostamble()
        {
        return ("");
        }


    /***********************************************************************************************
     * Export the Header and Table to an Excel spreadsheet!
     *
     * @param outputfile
     *
     * @throws ReportException
     */

    public synchronized final void exportExcel(final String outputfile) throws ReportException
        {
        final HSSFWorkbook workbook;
        final HSSFSheet worksheet;
        HSSFRow row;
        final FileOutputStream fileOut;
        final TableColumnModel modelColumn;
        TableColumn columnTable;
        final int intColumnCount;
        int intRow;
        int intCol;
        int intSheetRow;

        final String [] strTruncationWarning =
            {
            "The output will contain only those samples currently visible!"
            };

        final String [] strExportMessage =
            {
            MSG_EXPORTED + SPACE + outputfile
            };

        // Warn the User of truncation
        JOptionPane.showMessageDialog(null,
                                      strTruncationWarning,
                                      DIALOG_EXPORT,
                                      JOptionPane.WARNING_MESSAGE);

        try
            {
            workbook = new HSSFWorkbook();
            worksheet = workbook.createSheet(getReportUniqueName());
            fileOut = new FileOutputStream(outputfile);

            // Count the columns we have to render...
            modelColumn = tableReport.getColumnModel();
            intColumnCount = modelColumn.getColumnCount();

            // Render the header lines (if any)
            final Iterator iteratorHeader = modelReport.getHeader().iterator();
            intSheetRow = 0;

            while (iteratorHeader.hasNext())
                {
                row = worksheet.createRow(intSheetRow++);
                row.createCell(0).setCellValue(Utilities.stripHTML((String)iteratorHeader.next()));
                }

            // Render all the column headers
            row = worksheet.createRow(intSheetRow++);

            for (intCol = 0; intCol < intColumnCount; intCol++)
                {
                columnTable = modelColumn.getColumn(intCol);
                row.createCell(intCol).setCellValue(Utilities.stripHTML((String)columnTable.getIdentifier()));
                }

            // Render the column contents
            for (intRow = 0; intRow < tableReport.getRowCount(); intRow++)
                {
                row = worksheet.createRow(intSheetRow++);

                for (intCol = 0; intCol < intColumnCount; intCol++)
                    {
                    final Object objCell;

                    objCell = modelReport.getValueAt(intRow,
                                                     tableReport.getColumnModel().getColumn(intCol).getModelIndex());

                    if ((objCell instanceof String)
                        || (objCell instanceof StringBuffer))
                        {
                        row.createCell(intCol).setCellValue(Utilities.stripHTML(objCell.toString()));
                        }
                    else if (objCell instanceof FlagIcon)
                        {
                        row.createCell(intCol).setCellValue(((FlagIcon)objCell).getCountryCode());
                        }
                    else if (objCell instanceof Boolean)
                        {
                        // Checkboxes can only be off or on
                        if ((Boolean) objCell)
                            {
                            row.createCell(intCol).setCellValue("Yes");
                            }
                        else
                            {
                            row.createCell(intCol).setCellValue("No");
                            }
                        }
                    else if (objCell == null)
                        {
                        row.createCell(intCol).setCellValue(SPACE);
                        }
                    else
                        {
                        // We don't know what we are trying to render
                        // ReportIcons come here too...
                        row.createCell(intCol).setCellValue("??");
                        }
                    }
                }

            // Save the Excel workbook!
            workbook.write(fileOut);
            fileOut.close();

            // Let the user know where it went
            JOptionPane.showMessageDialog(null,
                                          strExportMessage,
                                          DIALOG_EXPORT,
                                          JOptionPane.INFORMATION_MESSAGE);
            }

        catch (final IOException exception)
            {
            throw new ReportException(exception.getMessage(), exception);
            }
        }


    /***********************************************************************************************
     * Export the Report as an XML file.
     *
     * @param outputfile
     *
     * @throws ReportException
     */

    public synchronized final void exportXML(final String outputfile) throws ReportException
        {
        final String [] strMessage =
            {
            //MSG_EXPORTED + SPACE + outputfile
            AWAITING_DEVELOPMENT
            };

        //toFile(outputfile, toText(separator));

        // Let the user know where it went
        JOptionPane.showMessageDialog(null,
                                      strMessage,
                                      DIALOG_EXPORT,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Send an Email containing the Report.
     *
     * @throws ReportException
     */

    public synchronized final void exportEmail() throws ReportException
        {
        EmailMessageInterface email;
        final String [] strMessage =
            {
            AWAITING_DEVELOPMENT
            };


        REGISTRY.addEmailToOutbox(new EmailMessageData());

        // Let the user know where it went
        JOptionPane.showMessageDialog(null,
                                      strMessage,
                                      DIALOG_EXPORT,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /*********************************************************************************************/
    /* Utilities                                                                                 */
    /**********************************************************************************************
     * Get the Report Unique name.
     * This is useful for Report subclasses.
     *
     * @return String
     */

    public synchronized final String getReportUniqueName()
        {
        return (this.strReportUniqueName);
        }


    /***********************************************************************************************
     * Set the Report Unique name.
     * This is useful for Report subclasses.
     *
     * @param name
     */

    public synchronized final void setReportUniqueName(final String name)
        {
        strReportUniqueName = name;
        }


    /**********************************************************************************************
     * Get the Report Tab name, i.e. just its purpose, not the host Instrument prefix.
     *
     * @return String
     */

    public synchronized final String getReportTabName()
        {
        return (this.strReportTabName);
        }


    /***********************************************************************************************
     * Set the Report Tab name, i.e. just its purpose, not the host Instrument prefix.
     *
     * @param name
     */

    public synchronized final void setReportTabName(final String name)
        {
        this.strReportTabName = name;
        }


    /**********************************************************************************************
     *
     * @return int
     */

    public synchronized final int getOrientation()
        {
        return (this.intOrientation);
        }


    /**********************************************************************************************
     *
     * @param orientation
     */

    public synchronized final void setOrientation(final int orientation)
        {
        this.intOrientation = orientation;
        }


    /**********************************************************************************************
     *
     * @return FontPlugin
     */

    public synchronized final FontInterface getHeaderFont()
        {
        return (this.fontHeader);
        }


    /**********************************************************************************************
     *
     * @param font
     */

    public synchronized final void setHeaderFont(final FontInterface font)
        {
        this.fontHeader = font;
        }


    /**********************************************************************************************
     * Get the Font used on the Report.
     *
     * @return FontPlugin
     */

    public synchronized final FontInterface getReportFont()
        {
        return (this.fontReport);
        }


    /**********************************************************************************************
     *
     * @param font
     */

    public synchronized final void setReportFont(final FontInterface font)
        {
        if (font != null)
            {
            this.fontReport = font;
            }
        }


    /***********************************************************************************************
     *
     * @return ColourPlugin
     */

    public synchronized final ColourInterface getCanvasColour()
        {
        return (this.colourCanvas);
        }


    /***********************************************************************************************
     *
     * @param colour
     */

    public synchronized final void setCanvasColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            this.colourCanvas = colour;
            }
        }


    /***********************************************************************************************
     *
     * @return ColourPlugin
     */

    public synchronized final ColourInterface getTableColour()
        {
        return (this.colourTable);
        }


    /***********************************************************************************************
     * Set the table foreground Colour.
     *
     * @param colour
     */

    public synchronized final void setTableColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            this.colourTable = colour;
            }
        }


    /***********************************************************************************************
     *
     * @return ColourPlugin
     */

    public synchronized final ColourInterface getGridColour()
        {
        return (this.colourGrid);
        }


    /***********************************************************************************************
     *
     * @param colour
     */

    public synchronized final void setGridColour(final ColourInterface colour)
        {
        this.colourGrid = colour;
        }


    /***********************************************************************************************
     * Indicate if the table grid should be shown.
     *
     * @param grid
     */

    public synchronized final void setShowGrid(final boolean grid)
        {
        if (tableReport != null)
            {
            tableReport.setShowGrid(grid);
            }
        else
            {
            LOGGER.error("Unable to set the grid");
            }
        }


    /***********************************************************************************************
     * Get the colour used for the text of the Report.
     *
     * @return ColourPlugin
     */

    public synchronized final ColourInterface getTextColour()
        {
        return (this.pluginColour);
        }


    /***********************************************************************************************
     * Set the colour used for the text of the Report.
     *
     * @param colour
     */

    public synchronized final void setTextColour(final ColourInterface colour)
        {
        if (colour != null)
            {
            this.pluginColour = colour;
            }
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean getDefaultPage()
        {
        return (this.boolPageDefault);
        }


    /***********************************************************************************************
     *
     * @param defaultpage
     */

    public synchronized final void setDefaultPage(final boolean defaultpage)
        {
        this.boolPageDefault = defaultpage;
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public synchronized final double getPageWidth()
        {
        return (this.dblPageWidth);
        }


    /***********************************************************************************************
     *
     * @param width
     */

    public synchronized final void setPageWidth(final double width)
        {
        this.dblPageWidth = width;
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public synchronized final double getPageHeight()
        {
        return (this.dblPageHeight);
        }


    /***********************************************************************************************
     *
     * @param height
     */

    public synchronized final void setPageHeight(final double height)
        {
        this.dblPageHeight = height;
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public synchronized final double getTopMargin()
        {
        return (this.dblMarginTop);
        }


    /***********************************************************************************************
     *
     * @param margin
     */

    public synchronized final void setTopMargin(final double margin)
        {
        if (margin < 0)
            {
            this.dblMarginTop = 0;
            }
        else
            {
            this.dblMarginTop = margin;
            }
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public synchronized final double getBottomMargin()
        {
        return (this.dblMarginBottom);
        }


    /***********************************************************************************************
     *
     * @param margin
     */

    public synchronized final void setBottomMargin(final double margin)
        {
        if (margin < 0)
            {
            this.dblMarginBottom = 0;
            }
        else
            {
            this.dblMarginBottom = margin;
            }
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public synchronized final double getLeftMargin()
        {
        return (this.dblMarginLeft);
        }


    /***********************************************************************************************
     *
     * @param margin
     */

    public synchronized final void setLeftMargin(final double margin)
        {
        if (margin < 0)
            {
            this.dblMarginLeft = 0;
            }
        else
            {
            this.dblMarginLeft = margin;
            }
        }


    /***********************************************************************************************
     *
     * @return double
     */

    public synchronized final double getRightMargin()
        {
        return (this.dblMarginRight);
        }


    /***********************************************************************************************
     *
     * @param margin
     */

    public synchronized final void setRightMargin(final double margin)
        {
        if (margin < 0)
            {
            this.dblMarginRight = 0;
            }
        else
            {
            this.dblMarginRight = margin;
            }
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean getEnableBorder()
        {
        return (this.boolEnableBorder);
        }


    /***********************************************************************************************
     *
     * @param enable
     */

    public synchronized final void setEnableBorder(final boolean enable)
        {
        this.boolEnableBorder = enable;
        }


    /***********************************************************************************************
     *
     * @return int
     */

    public synchronized final int getRefreshPeriod()
        {
        return (this.intRefreshPeriod);
        }


    /***********************************************************************************************
     *
     * @param period
     */

    public synchronized final void setRefreshPeriod(final int period)
        {
        this.intRefreshPeriod = period;
        }


    /***********************************************************************************************
     *
     * @param tooltip
     */

    public synchronized final void setToolTipText(final String tooltip)
        {
        tableReport.setToolTipText(tooltip);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean isPrintable()
        {
        return (this.boolPrintable);
        }


    /***********************************************************************************************
     *
     * @param printable
     */

    public synchronized final void setPrintable(final boolean printable)
        {
        this.boolPrintable = printable;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean isExportable()
        {
        return (this.boolExportable);
        }


    /***********************************************************************************************
     *
     * @param exportable
     */

    public synchronized final void setExportable(final boolean exportable)
        {
        this.boolExportable = exportable;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean isRefreshable()
        {
        return (this.boolRefreshable);
        }


    /***********************************************************************************************
     * Get the ReportTable container.
     *
     * @return Container
     */

    public Container getContainer()
        {
        return (this);
        }


    /***********************************************************************************************
     * Get the JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }


    /***********************************************************************************************
     * Get the JToolBar Icon.
     *
     * @return Icon
     */

    public Icon getToolBarIcon()
        {
        return (this.iconToolBar);
        }


    /***********************************************************************************************
     * Set the JToolBar Icon.
     *
     * @param icon
     */

    public void setToolBarIcon(final Icon icon)
        {
        this.iconToolBar = icon;
        }


    /***********************************************************************************************
     * Indicate if the Report is Refreshable.
     *
     * @param refreshable
     */

    public synchronized final void setRefreshable(final boolean refreshable)
        {
        this.boolRefreshable = refreshable;
        }


    /***********************************************************************************************
     * Indicate if the Report is Refreshable with a mouse click.
     *
     * @return boolean
     */

    public synchronized final boolean isClickRefresh()
        {
        return (this.boolClickRefresh);
        }


    /***********************************************************************************************
     *
     * @param click
     */

    public synchronized final void setClickRefresh(final boolean click)
        {
        this.boolClickRefresh = click;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean isTopRowLocked()
        {
        return (this.boolLockTopRow);
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public synchronized final boolean isLeftColumnsLocked()
        {
        return (this.boolLockLeftColumns);
        }


    /***********************************************************************************************
     *
     * @return int
     */

    public synchronized final int getColumnsToLock()
        {
        return (this.intColumnsToLock);
        }


    /***********************************************************************************************
     * Get the ReportTableToolbar state..
     *
     * @return ReportTableToolbar
     */

    private synchronized ReportTableToolbar getToolbarState()
        {
        return (this.toolbarState);
        }


    /***********************************************************************************************
     * Get the DataViewMode mode.
     *
     * @return DataViews
     */

    public DataViewMode getDataViewMode()
        {
        return (this.dataViewMode);
        }


    /***********************************************************************************************
     * Set the DataViewMode mode.
     *
     * @param dataview
     */

    public void setDataViewMode(final DataViewMode dataview)
        {
        this.dataViewMode = dataview;
        }


    /***********************************************************************************************
     * Get the DataView limit.
     *
     * @return int
     */

    public int getDataViewLimit()
        {
        return (this.intDataViewLimit);
        }


    /***********************************************************************************************
     * Set the DataView limit.
     *
     * @param limit
     */

    public void setDataViewLimit(final int limit)
        {
        this.intDataViewLimit = limit;
        }


    /***********************************************************************************************
     * Indicate if this Report may be reordered.
     *
     * @return boolean
     */

    public synchronized final boolean isReorderable()
        {
        return (this.boolReorderable);
        }


    /***********************************************************************************************
     * Indicate if this Report may be reordered.
     *
     * @param reorderable
     */

    public synchronized final void setReorderable(final boolean reorderable)
        {
        this.boolReorderable = reorderable;
        }


    /***********************************************************************************************
     * Indicate if this Report may be truncated.
     *
     * @return boolean
     */

    public synchronized final boolean isTruncateable()
        {
        return (this.boolTruncateable);
        }


    /***********************************************************************************************
     * Indicate if this Report may be truncated.
     *
     * @param truncateable
     */

    public synchronized final void setTruncateable(final boolean truncateable)
        {
        this.boolTruncateable = truncateable;
        }


    /***********************************************************************************************
     * Truncate the Report to the number of entries specified in the Resources.
     *
     * @throws ReportException
     */

    public synchronized void truncateReport() throws ReportException
        {
        // We expect to override this, so this message should never appear...
        JOptionPane.showMessageDialog(this,
                                      "Truncation is not implemented for this Report",
                                      "Truncation Warning",
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Dispose of the Report.
     * Override this if the Report data are held somewhere else.
     */

    public void disposeReport()
        {
        if (getReportTableModel() != null)
            {
            getReportTableModel().clearRows();
            }
        }


    /**********************************************************************************************
     * Get the Task on which this Report is based.
     *
     * @return TaskData
     */

    public synchronized final TaskPlugin getTask()
        {
        return ((TaskPlugin)this.pluginTask);
        }


    /***********************************************************************************************
     * Get the Row to scroll to after refresh.
     *
     * @return int
     */

    public int getScrollToRow()
        {
        return (this.intScrollToRow);
        }


    /***********************************************************************************************
     * Set the Row to scroll to after refresh.
     *
     * @param row
     */

    public void setScrollToRow(final int row)
        {
        this.intScrollToRow = row;
        }


    /***********************************************************************************************
     * Scroll the Report so that the specified Row is visible.
     * Used mainly in JavaConsole, so that the last output is always visible.
     *
     * @param row
     */

    public synchronized final void scrollRowToVisible(final int row)
        {
        if ((tableReport != null)
            && (scrollPane != null)
            && (scrollPane.getViewport() != null))
            {
            final Rectangle rect;

            rect = tableReport.getCellRect(row, 0, true);

//            SwingUtilities.invokeLater(new Runnable()
//                {
//                public void run()
//                    {
                    // This line is essential! Just scrolling the Viewport doesn't work for row zero
                    tableReport.scrollRectToVisible(rect);
                    scrollPane.getViewport().scrollRectToVisible(rect);
//                    }
//                });

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "ReportTable.scrollRowToVisible() Scrolling to rectangle: " +
                                        "[x=" + rect.getX()
                                        + "] [y=" + rect.getY()
                                        + "] [width=" + rect.getWidth()
                                        + "] [height=" + rect.getHeight()
                                        + "] [row=" + row + "]");
            }
        }


    /***********************************************************************************************
     * Get the ReportTableModel.
     *
     * @return ReportTableModel
     */

    public synchronized ReportTableModel getReportTableModel()
        {
        return (this.modelReport);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the Reports.
     *
     * Framework Properties used:
     * <code>
     * <li>Report.Page.Orientation
     * <li>Report.Page.Default
     * <li>Report.Page.Width
     * <li>Report.Page.Height
     * <li>Report.Page.Margin.Top
     * <li>Report.Page.Margin.Bottom
     * <li>Report.Page.Margin.Left
     * <li>Report.Page.Margin.Right
     * <li>Report.Font.Header
     * <li>Report.Font.Table
     * <li>Report.Colour.Canvas
     * <li>Report.Colour.Table
     * <li>Report.Colour.Text
     * <li>Report.Period.Refresh
     * <li>Report.Enable.Border
     * <li>Report.Enable.Debug
     * </code>
     */

    public void readResources()
        {
        final String strOrientation;

        LOGGER.debugNavigation("ReportTable.readResources() [ResourceKey=" + getResourceKey() + "]");

        setDebug(REGISTRY.getBooleanProperty(getResourceKey() + KEY_REPORT_ENABLE_DEBUG));

        strOrientation = REGISTRY.getStringProperty(getResourceKey() + KEY_REPORT_PAGE_ORIENTATION);
        setOrientation(PageFormat.PORTRAIT);

        if (strOrientation.toLowerCase().contains("reverselandscape"))
            {
            setOrientation(PageFormat.REVERSE_LANDSCAPE);
            }
        else
            {
            if (strOrientation.toLowerCase().contains("landscape"))
                {
                setOrientation(PageFormat.LANDSCAPE);
                }
            }

        setDefaultPage(REGISTRY.getBooleanProperty(getResourceKey() + KEY_REPORT_PAGE_DEFAULT));
        setPageWidth(REGISTRY.getDoubleProperty(getResourceKey() + KEY_REPORT_PAGE_WIDTH));
        setPageHeight(REGISTRY.getDoubleProperty(getResourceKey() + KEY_REPORT_PAGE_HEIGHT));
        setTopMargin(REGISTRY.getDoubleProperty(getResourceKey() + KEY_REPORT_PAGE_MARGIN_TOP));
        setBottomMargin(REGISTRY.getDoubleProperty(getResourceKey() + KEY_REPORT_PAGE_MARGIN_BOTTOM));
        setLeftMargin(REGISTRY.getDoubleProperty(getResourceKey() + KEY_REPORT_PAGE_MARGIN_LEFT));
        setRightMargin(REGISTRY.getDoubleProperty(getResourceKey() + KEY_REPORT_PAGE_MARGIN_RIGHT));
        setEnableBorder(REGISTRY.getBooleanProperty(getResourceKey() + KEY_REPORT_PAGE_BORDER));

        setHeaderFont((FontInterface)REGISTRY.getProperty(getResourceKey() + KEY_REPORT_FONT_HEADER));
        setReportFont((FontInterface)REGISTRY.getProperty(getResourceKey() + KEY_REPORT_FONT_TABLE));
        setCanvasColour((ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_REPORT_COLOUR_CANVAS));
        setTableColour((ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_REPORT_COLOUR_TABLE));
        setTextColour((ColourInterface)REGISTRY.getProperty(getResourceKey() + KEY_REPORT_COLOUR_TEXT));
        setRefreshPeriod(1000 * REGISTRY.getIntegerProperty(getResourceKey() + KEY_REPORT_PERIOD_REFRESH));

        // StringData.getString(getResourceKey() + "Report.ContextMenu.Print");
        // StringData.getString(getResourceKey() + "Report.ContextMenu.Export.Text");
        // StringData.getString(getResourceKey() + "Report.ContextMenu.Export.Excel");

        MENU_PRINT = "Print the Report";
        MENU_TEXT = "Export Report as Text";
        MENU_EXCEL = "Export Report as Excel";
        MENU_XML = "Export Report as XML";
        MENU_EMAIL = "Send Report as Email";

        LOGGER.debugNavigation("ReportTable.readResources() END");
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Report.
     *
     * @return String
     */

    public synchronized final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Show the table configuration and data.
     */

    public synchronized final void showTableData()
        {
        if ((getReportTable() != null)
            && (getReportTable().getModel() != null))
            {
            LOGGER.debug("Table data: ");

            for (int i = 0; i < getReportTable().getRowCount(); i++)
                {
                LOGGER.debug(INDENT + "row " + i + ":");

                for (int j = 0; j < getReportTable().getColumnCount(); j++)
                    {
                    LOGGER.debug(INDENT + getReportTable().getModel().getValueAt(i, j));
                    }

                LOGGER.debug("");
                }
            }
        }
    }
