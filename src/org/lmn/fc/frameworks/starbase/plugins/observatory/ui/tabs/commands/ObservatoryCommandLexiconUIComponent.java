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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.comparators.ObservatoryInstrumentsByIdentifier;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.*;


/***************************************************************************************************
 * ObservatoryCommandLexiconUIComponent.
 * This gives a list of all of the Commands of all of the Instruments currently in the Observatory.
 */

public final class ObservatoryCommandLexiconUIComponent extends ReportTable
                                                        implements ReportTablePlugin,
                                                                   FrameworkConstants,
                                                                   FrameworkStrings,
                                                                   FrameworkMetadata
    {
    private static final String TITLE_INSTRUMENT = "Instrument";
    private static final String MSG_NO_COMMANDS = "<html><i>No Commands</i></html>";

    private final ObservatoryInstrumentInterface hostInstrument;
    private final ObservatoryUIInterface hostUI;


    /***********************************************************************************************
     * Add an Instrument to the Report.
     *
     * @param instrument
     * @param report
     * @param columncount
     */

    private static void addInstrument(final ObservatoryInstrumentInterface instrument,
                                      final Vector<Vector> report,
                                      final int columncount)
        {
        if ((instrument != null)
            && (instrument.getInstrument() != null))
            {
            CommandLexiconHelper.addLabelLine(report,
                                              columncount,
                                              0,
                                              instrument.getInstrument().getIdentifier());

            // Check that the Instrument has a Controller
            if (instrument.getInstrument().getController() != null)
                {
                // Check that the Instrument has Plugin Commands
                CommandLexiconHelper.addPluginCommands(instrument.getInstrument().getController(),
                                                       report,
                                                       columncount,
                                                       1);

                // Check that the Instrument has Controller Commands
                CommandLexiconHelper.addControllerCommands(instrument.getInstrument().getController(),
                                                           report,
                                                           columncount,
                                                           1);
                }
            else
                {
                // Show that the Instrument has no Commands
                CommandLexiconHelper.addLabelLine(report,
                                                  columncount,
                                                  1,
                                                  MSG_NO_COMMANDS);
                }
            }
        }


    /***********************************************************************************************
     * Construct an ObservatoryCommandLexiconUIComponent.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param hostui
     * @param resourcekey
     */

    public ObservatoryCommandLexiconUIComponent(final TaskPlugin task,
                                                final ObservatoryInstrumentInterface hostinstrument,
                                                final ObservatoryUIInterface hostui,
                                                final String resourcekey)
        {
        super(task,
              CommandLexiconUIComponent.REPORT_NAME,
              resourcekey,
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

        this.hostInstrument = hostinstrument;
        this.hostUI = hostui;

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public synchronized final void initialiseUI()
        {
        super.initialiseUI();

        // Install the ObservatoryCommandLexiconUIComponent as the CommandLexicon for this Instrument
        if (getHostInstrument().getContext() != null)
            {
            getHostInstrument().getContext().setCommandLexicon(this);
            }
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

        vecHeader.add(CommandLexiconUIComponent.REPORT_HEADER
                        + SPACE
                        + getObservatoryClock().getDateTimeNowAsString());

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

        vecColumns = new Vector<ReportColumnMetadata>(CommandLexiconHelper.createWidths().length + 1);

        // Add the Instrument column
        vecColumns.add(new ReportColumnMetadata(TITLE_INSTRUMENT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Name of the Instrument",
                                                SwingConstants.LEFT));

        // Use the columns from CommandLexiconUIComponent
        vecColumns.addAll(CommandLexiconHelper.createColumns());

        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final Object [] columnWidthsModule;
        final Object [] columnWidths;

        columnWidthsModule = CommandLexiconHelper.createWidths();
        columnWidths = new Object[columnWidthsModule.length + 1];

        // Instrument Name
        columnWidths[0] = "MMMMMMMMMMMMMMMMMMMMMM";

        for (int i = 0;
             i < columnWidthsModule.length;
             i++)
            {
            // Use the columns from CommandLexiconUIComponent
            columnWidths[i+1] = columnWidthsModule[i];
            }

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

        vecReport = new Vector<Vector>(100);

        if ((getHostUI() != null)
            && (getHostUI().getObservatoryInstruments() != null)
            && (!getHostUI().getObservatoryInstruments().isEmpty()))
            {
            final List<ObservatoryInstrumentInterface> listInstruments;
            final Iterator<ObservatoryInstrumentInterface> iterInstruments;

            listInstruments = new ArrayList<ObservatoryInstrumentInterface>(getHostUI().getObservatoryInstruments().size());
            listInstruments.addAll(getHostUI().getObservatoryInstruments());

            Collections.sort(listInstruments, new ObservatoryInstrumentsByIdentifier());
            iterInstruments = listInstruments.iterator();

            while (iterInstruments.hasNext())
                {
                final ObservatoryInstrumentInterface instrument;

                instrument = iterInstruments.next();

                // Add the Instrument to the Report
                addInstrument(instrument,
                              vecReport,
                              defineColumnWidths().length);
                }
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
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private synchronized ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ObservatoryUI to which this UIComponent is attached.
     *
     * @return ObservatoryUIInterface
     */

    private synchronized ObservatoryUIInterface getHostUI()
        {
        return (this.hostUI);
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
