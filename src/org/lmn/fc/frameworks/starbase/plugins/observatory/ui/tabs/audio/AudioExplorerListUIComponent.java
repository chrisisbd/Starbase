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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.audio;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AudioExplorerListUIComponentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * AudioExplorerListUIComponent.
 */

public final class AudioExplorerListUIComponent extends ReportTable
                                                implements AudioExplorerListUIComponentInterface
    {
    private static final long serialVersionUID = -8219670276473410826L;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private       Object[][]                     arrayData;
    private       String[]                       arrayColumnNames;


    /***********************************************************************************************
     * Construct an AudioExplorerListUIComponent.
     * The metadata is contained in the List of Metadata (name, value, units, type)
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param arraydata
     * @param arraynames
     * @param font
     * @param colour
     * @param hostresourcekey
     * @param debug
     */

    public AudioExplorerListUIComponent(final TaskPlugin task,
                                        final ObservatoryInstrumentInterface hostinstrument,
                                        final Object[][] arraydata,
                                        final String[] arraynames,
                                        final FontInterface font,
                                        final ColourInterface colour,
                                        final String hostresourcekey,
                                        final boolean debug)
        {
        super(task,
              REPORT_NAME,
              hostresourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.NONE,
              null);

        // Injections
        this.hostInstrument = hostinstrument;
        this.arrayData = arraydata;
        this.arrayColumnNames = arraynames;

        setReportFont(font);
        setTextColour(colour);

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName() + SPACE + getReportTabName());
            }
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public synchronized void initialiseUI()
        {
        super.initialiseUI();

//        System.out.println("ListUI: [state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public synchronized void runUI()
        {
        super.runUI();

//        System.out.println("ListUI  UI: [state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public synchronized void stopUI()
        {
        super.stopUI();

//        System.out.println("ListUI  UI: [state=" + getUIState().getName() + "]");
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public synchronized void disposeUI()
        {
        super.disposeUI();

//        System.out.println("ListUI  UI: [state=" + getUIState().getName() + "]");
        }


   /***********************************************************************************************
    * Generate the report header.
    *
    * @return Vector
        */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(1);

        vecHeader.add(REPORT_HEADER + SPACE + getObservatoryClock().getDateTimeNowAsString());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final String SOURCE = "AudioExplorerListUIComponent.defineColumns() ";
        final Vector<ReportColumnMetadata> vecColumns;

        if (getColumnNames() != null)
            {
            vecColumns = new Vector<ReportColumnMetadata>(getColumnNames().length);

            for (int intColumnIndex = 0;
                 intColumnIndex < getColumnNames().length;
                 intColumnIndex++)
                {
                final String strName;

                strName = getColumnNames()[intColumnIndex];

                vecColumns.add(new ReportColumnMetadata(strName,
                                                        SchemaDataType.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        EMPTY_STRING,
                                                        SwingConstants.LEFT ));
                }
            }
        else
            {
            vecColumns = new Vector<ReportColumnMetadata>(1);

            vecColumns.add(new ReportColumnMetadata(TITLE_COLUMN,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    EMPTY_STRING,
                                                    SwingConstants.LEFT ));
            }

        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final String SOURCE = "AudioExplorerListUIComponent.defineColumnWidths() ";
        final List<Object> listColumnWidths;

        listColumnWidths = new ArrayList<Object>(16);

        if (getColumnNames() != null)
            {
            for (int intColumnIndex = 0;
                 intColumnIndex < getColumnNames().length;
                 intColumnIndex++)
                {
                listColumnWidths.add("MMMMMMMMMMMMMMMMMMMMMM");
                }
            }
        else
            {
            listColumnWidths.add("MMMMMMMMMMMMMMMMMMMMMM");
            }

        return (listColumnWidths.toArray());
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "AudioExplorerListUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(25);

        // Only generate a Report if this UIComponent is visible
        // There is no need to auto-truncate
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            if (getData() != null)
                {
                final Object[][] arrayReportData;

                arrayReportData = getData();

                for (int intDataIndex = 0;
                     intDataIndex < arrayReportData.length;
                     intDataIndex++)
                    {
                    final Object[] arrayDataItems;

                    arrayDataItems = arrayReportData[intDataIndex];

                    if (arrayDataItems != null)
                        {
                        final Vector<Object> vecRow;

                        vecRow = new Vector<Object>(arrayDataItems.length);

                        for (int intItemIndex = 0;
                             intItemIndex < arrayDataItems.length;
                             intItemIndex++)
                            {
                            final Object arrayDataItem;

                            arrayDataItem = arrayDataItems[intItemIndex];

                            // Remember that all data entries in a Report must be Strings
                            if (arrayDataItem != null)
                                {
                                vecRow.add(arrayDataItem.toString());
                                }
                            }

                        vecReport.add(vecRow);
                        }
                    }
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "NOT VISIBLE");
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Get the Array of Data for this UIComponent.
     *
     * @return Object[][]
     */

    private Object[][] getData()
        {
        return (this.arrayData);
        }


    /***********************************************************************************************
     * Set the Array of Data for this UIComponent.
     *
     * @param arraydata
     */

    public void setData(final Object[][] arraydata)
        {
        this.arrayData = arraydata;
        }


    /***********************************************************************************************
     * Get the Array of Column Names for this UIComponent.
     *
     * @return Object[][]
     */

    private String[] getColumnNames()
        {
        return (this.arrayColumnNames);
        }


    /***********************************************************************************************
     * Set the Array of Column Names for this UIComponent.
     *
     * @param arraynames
     */

    public void setColumnNames(final String[] arraynames)
        {
        this.arrayColumnNames = arraynames;
        }


    /***********************************************************************************************
     * Read all the Resources required by the AudioExplorerListUIComponent.
     */

    public final void readResources()
        {
        super.readResources();
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
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private synchronized ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
