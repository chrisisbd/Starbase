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
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import java.util.Vector;


/***************************************************************************************************
 * A general purpose CommandLexiconUIComponent.
 */

public final class CommandLexiconUIComponent extends ReportTable
                                             implements ReportTablePlugin,
                                                        FrameworkConstants,
                                                        FrameworkStrings,
                                                        FrameworkMetadata
    {
    public static final String REPORT_NAME = "Command Lexicon";
    public static final String REPORT_HEADER = "Command Lexicon Report created at";

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private Instrument instrumentXml;


    /***********************************************************************************************
     * Construct an CommandLexiconUIComponent.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param instrument
     * @param hostresourcekey
     */

    public CommandLexiconUIComponent(final TaskPlugin task,
                                     final ObservatoryInstrumentInterface hostinstrument,
                                     final Instrument instrument,
                                     final String hostresourcekey)
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
              ReportTableToolbar.HORIZ_NORTH_PRT,
              RegistryModelUtilities.getAtomIcon(hostinstrument.getHostAtom(),
                                                 ObservatoryInterface.FILENAME_ICON_LEXICON));

        if ((instrument == null)
            || (!XmlBeansUtilities.isValidXml(instrument))
            || (instrument.getResourceKey() == null)
            || (EMPTY_STRING.equals(instrument.getResourceKey().trim())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.hostInstrument = hostinstrument;
        this.instrumentXml = instrument;

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

        // Install the CommandLexiconUIComponent as the CommandLexicon for this Instrument
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
        return (CommandLexiconHelper.createColumns());
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        return (CommandLexiconHelper.createWidths());
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        vecReport = new Vector<Vector>(10);

        // Check that the Instrument has a Controller
        if ((getInstrument() != null)
            && (getInstrument().getController() != null))
            {
            // Check that the Instrument has Plugins
            CommandLexiconHelper.addPluginCommands(getInstrument().getController(),
                                                   vecReport,
                                                   defineColumnWidths().length,
                                                   0);

            // Check that the Instrument has Commands (i.e. the Core)
            CommandLexiconHelper.addControllerCommands(getInstrument().getController(),
                                                       vecReport,
                                                       defineColumnWidths().length,
                                                       0);

            // Sort the Commands by their Identifiers (column 0)
            //Collections.sort(vecReport, new ReportRowsByColumn(COLUMN_INDEX_IDENTIFIER));
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
     * Read all the Resources required by the CommandLexiconUIComponent.
     */

    public final void readResources()
        {
        super.readResources();
        }


    /***********************************************************************************************
     * Get the Instrument Xml.
     *
     * @return Instrument
     */

    private Instrument getInstrument()
        {
        return (this.instrumentXml);
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
