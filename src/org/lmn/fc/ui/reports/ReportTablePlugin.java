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
//  26-03-04    LMN created file
//  21-03-05    LMN added generateIcons()
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.Vector;


/**************************************************************************************************
 * Any ReportTable must implement all of these methods.
 */

public interface ReportTablePlugin extends UIComponentPlugin,
                                           Printable
    {
    // String Resources
    String CONTEXT_ACTION_GROUP_REPORTS    = "Reports";
    String TOOLTIP_CLICK_HERE              = "Click here to see the latest information";
    String TOOLTIP_TIMER_REFRESH           = "This information will be refreshed every ";
    String LOG_PRINTED_REPORT              = "Printed Report for";
    String MSG_NOPRINTER                   = "There is no default printer installed";
    String DIALOG_PRINT                    = "Print Report";
    String DIALOG_EXPORT                   = "Export Report";
    String MSG_REPORT_PRINTED              = "The Report has been printed";
    String MSG_EXPORTED                    = "The Report was exported to";
    String TITLE_INDEX = "Index";
    String TITLE_X = "X-value";
    String TITLE_DATE = "Date";
    String TITLE_TIME = "Time";
    String TITLE_CHANNEL = "Channel";
    String FOOTER_WEBSITE       = "Starbase.Website http://www.ukraa.com";
    String FOOTER_EMAIL         = "Starbase.Contact starbase@ukraa.com";
    String MSG_REPORT_CREATED   = "Report created at ";
    String PREFIX_PAGE_SETUP = "Page Setup ";
    String PREFIX_PRINT = "Print ";
    String MSG_NO_PRINTER = "No printer found!";
    String MSG_PAGE_NUMBER = "Page";
    String HEADER_ICON_FILENAME = "branding/ukraa-logo-print.png";
    String EVENT_LOG_ICON_FILENAME = "toolbars/toolbar-event-log.png";
    String INSTRUMENT_LOG_ICON_FILENAME = "toolbars/toolbar-instrument-log.png";

    // ReportTable configuration via the constructor
    boolean PRINTABLE = true;
    boolean NON_PRINTABLE = false;
    boolean EXPORTABLE = true;
    boolean NON_EXPORTABLE = false;
    boolean REFRESHABLE = true;
    boolean NON_REFRESHABLE = false;
    boolean REFRESH_CLICK = true;
    boolean REFRESH_TIMER = false;
    boolean REFRESH_NONE = false;
    boolean REORDERABLE = true;
    boolean NON_REORDERABLE = false;
    boolean TRUNCATEABLE = true;
    boolean NON_TRUNCATEABLE = false;
    boolean LOCK_TOP_ROW = true;
    boolean SCROLL_TOP_ROW = false;
    boolean LOCK_LEFT_COLUMNS = true;
    boolean SCROLL_LEFT_COLUMNS = false;

    int HEADER_ROWS_PER_COLUMN = 18;

    int DEFAULT_DATA_VIEW_LIMIT = 250;
    int LOGSIZE_TRUNCATE = 25;
    int LOGSIZE_MAX = 10000;
    Color COLOR_SELECTION_BACKGROUND = new Color(255, 255, 204);
    Color COLOR_SELECTION_FOREGROUND = new Color(0, 67, 191);

    // Keep the compiler a bit quieter...
    long VERSION_ID = 0L;

    boolean REFRESH_START = true;
    boolean REFRESH_STOP = false;
    char TAB_SEPARATOR = '\u0009';
    char COMMA_SEPARATOR = ',';
    int MIN_ROW_HEIGHT = 15;
    int HEADER_INDENT = 10;
    int HEADER_OFFSET = HEADER_INDENT + 150 + 10;
    // Logo icon set at 150px
    int HEADER_VERTICAL_GAP = 6;
    int HEADER_COLUMN_WIDTH = 250;
    int TABLE_INDENT = 10;
    int HEADER_ICON_GAP = 10;


    /***********************************************************************************************
     * Get the Report Unique name.
     *
     * @return String
     */

    String getReportUniqueName();


    /***********************************************************************************************
     * Set the Report Unique name.
     * This is useful for Report subclasses.
     *
     * @param name
     */

    void setReportUniqueName(String name);


    /***********************************************************************************************
     * Get the Report Tab name, i.e. just its purpose, not the host Instrument prefix.
     *
     * @return String
     */

    String getReportTabName();


    /***********************************************************************************************
     * Set the Report Tab name, i.e. just its purpose, not the host Instrument prefix.
     *
     * @param name
     */

    void setReportTabName(String name);


    JTable initialiseReport() throws ReportException;

    Vector<String> generateHeader() throws ReportException;

    Vector<PrintableIconInterface> generateIcons() throws ReportException;


    /***********************************************************************************************
     * Get the JTable on which the Report is based.
     *
     * @return JTable
     */

    JTable getReportTable();


     Vector<ReportColumnMetadata> defineColumns();

    Object [] defineColumnWidths();

    Vector<Vector> generateReport() throws ReportException;


    Vector<Vector> generateRawReport() throws ReportException;


    /**********************************************************************************************
     * Print the Report on a separate SwingWorker Thread,
     * which allows us to see when it is finished.
     */

    void printReport();


    /***********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     */

    Vector<Vector> refreshReport() throws ReportException;


    /***********************************************************************************************
     * Refresh the table data on a separate thread.
     */

    void refreshTable();

    /***********************************************************************************************
     * Get the Thread used to refresh the Report.
     *
     * @return SwingWorker
     */

    SwingWorker getRefreshThread();


    /***********************************************************************************************
     * Indicate if this Report may be reordered.
     *
     * @return boolean
     */

    boolean isReorderable();


    /***********************************************************************************************
     * Indicate if this Report may be reordered.
     *
     * @param reorderable
     */

    void setReorderable(boolean reorderable);


    /***********************************************************************************************
     * Indicate if this Report may be truncated.
     *
     * @return boolean
     */

    boolean isTruncateable();


    /***********************************************************************************************
     * Indicate if this Report may be truncated.
     *
     * @param truncateable
     */

    void setTruncateable(boolean truncateable);


    /***********************************************************************************************
     * Truncate the Report to the number of entries specified in the Resources.
     *
     * @throws ReportException
     */

    void truncateReport() throws ReportException;


    /***********************************************************************************************
     * Dispose of the Report.
     */

    void disposeReport();


    /**********************************************************************************************
     * Get the Task on which this Report is based.
     *
     * @return TaskData
     */

    TaskPlugin getTask();


    /***********************************************************************************************
     * Get the Row to scroll to after refresh.
     *
     * @return int
     */

    int getScrollToRow();


    /***********************************************************************************************
     * Set the Row to scroll to after refresh.
     *
     * @param row
     */

    void setScrollToRow(int row);


    /***********************************************************************************************
     * Scroll the Report so that the specified Row is visible.
     * Used mainly in JavaConsole, so that the last output is always visible.
     *
     * @param row
     */

    void scrollRowToVisible(int row);


    /***********************************************************************************************
     * Get the ReportTableModel.
     *
     * @return ReportTableModel
     */

    ReportTableModel getReportTableModel();


    /***********************************************************************************************
     * Get the ReportTable container.
     *
     * @return Container
     */

    Container getContainer();


    /***********************************************************************************************
     * Get the JToolBar Icon.
     *
     * @return JToolBar
     */

    Icon getToolBarIcon();


    /***********************************************************************************************
     * Set the JToolBar Icon.
     *
     * @param icon
     */

    void setToolBarIcon(final Icon icon);


    /***********************************************************************************************
     * Get the DataViewMode mode.
     *
     * @return DataViews
     */

    DataViewMode getDataViewMode();


    /***********************************************************************************************
     * Set the DataViewMode mode.
     *
     * @param dataview
     */

    void setDataViewMode(DataViewMode dataview);


    /***********************************************************************************************
     * Get the DataView limit.
     *
     * @return int
     */

    int getDataViewLimit();


    /***********************************************************************************************
     * Set the DataView limit.
     *
     * @param limit
     */

    void setDataViewLimit(int limit);


    /***********************************************************************************************
     * Indicate if the table grid should be shown.
     *
     * @param grid
     */

    void setShowGrid(boolean grid);


    /***********************************************************************************************
     * Set the table foreground Colour.
     *
     * @param colour
     */

    void setTableColour(ColourInterface colour);


    /***********************************************************************************************
     * Get the colour used for the text of the Report.
     *
     * @return ColourPlugin
     */

    ColourInterface getTextColour();


    /***********************************************************************************************
     * Set the colour used for the text of the Report.
     *
     * @param colour
     */

    void setTextColour(ColourInterface colour);


    /***********************************************************************************************
     *
     * @return boolean
     */

    boolean isPrintable();


    /**********************************************************************************************
     * Get the Font used on the Report.
     *
     * @return FontPlugin
     */

    FontInterface getReportFont();

    void setReportFont(FontInterface font);

    void setCanvasColour(ColourInterface colour);

    }
