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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.ui;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.Vector;


/***********************************************************************************************
 * A UIComponent to show the News Headlines.
 */

public final class RssNewsHeadlinesUIComponent extends ReportTable
                                               implements ReportTablePlugin
    {
    // String Resources
    private static final String MSG_REPORT_CREATED  = "News Headlines Report created at";

    public static final String TITLE_DATE       = "Date";
    public static final String TITLE_TIME       = "Time";
    public static final String TITLE_HEADLINES  = "Headlines";

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final String strResourceKey;


    /***********************************************************************************************
     * Construct an XmlUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param reportname
     * @param reportresourcekey
     */

    public RssNewsHeadlinesUIComponent(final RootPlugin task,
                                       final ObservatoryInstrumentInterface hostinstrument,
                                       final String reportname,
                                       final String reportresourcekey)
        {
        super(task,
              reportname,
              reportresourcekey,
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

        if ((hostinstrument != null)
            && (reportresourcekey != null)
            && (!EMPTY_STRING.equals(reportresourcekey)))
            {
            this.strResourceKey = reportresourcekey;
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.hostInstrument = hostinstrument;
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

        vecHeader.add(getReportUniqueName() + SPACE + MSG_REPORT_CREATED + SPACE + getObservatoryClock().getDateTimeNowAsString());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_DATE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_HEADLINES,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
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
            "2000-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
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
        final Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(50);






        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the Report data.
     *
     * @return Vector
     */

    public Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
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
