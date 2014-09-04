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

import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;


/***************************************************************************************************
 * A SimpleInstrumentLogUIComponent.
 */

public final class SimpleInstrumentLogUIComponent extends ReportTable
                                                  implements ReportTablePlugin
    {
    // String Resources
    private static final String HEADER_TITLE = "Instrument Log";
    private static final String MSG_REPORT_CREATED = "Instrument Log created at ";

    private static final String TITLE_ICON      = SPACE;
    private static final String TITLE_DATE      = "Date";
    private static final String TITLE_TIME      = "Time";
    private static final String TITLE_EVENT     = "Event";
    private static final String TITLE_SOURCE    = "Source";

    private static final String MENU_TRUNCATE = "Truncate Log";

    private final ObservatoryInstrumentInterface hostInstrument;


    /***********************************************************************************************
     * Log an Event from the Instrument on a SimpleInstrumentLogUIComponent.
     *
     * @param log
     * @param status
     * @param event
     * @param source
     * @param clock
     */

    public static void logEvent(final Vector<Vector> log,
                                final EventStatus status,
                                final String event,
                                final String source,
                                final ObservatoryClockInterface clock)
        {
        final Vector<Object> vecLogFragment;

        vecLogFragment = createInstrumentLogFragment(status, event, source, clock);

        if ((log != null)
            && (vecLogFragment != null)
            && (!vecLogFragment.isEmpty()))
            {
            log.add(vecLogFragment);
            }
        }


    /***********************************************************************************************
     * Create an InstrumentLogFragment from the specified values.
     * This is just one line of a log!
     *
     * @param status
     * @param event
     * @param source
     * @param clock
     *
     * @return Vector<Object>
     */

    private static Vector<Object> createInstrumentLogFragment(final EventStatus status,
                                                              final String event,
                                                              final String source,
                                                              final ObservatoryClockInterface clock)
        {
        final Vector<Object> vecLogFragment;

        vecLogFragment = new Vector<Object>(LogHelper.DEFAULT_LOG_COLUMN_COUNT);

        if ((status != null)
            && (event != null)
            && (!EMPTY_STRING.equals(event))
            & (source != null)
            && (!EMPTY_STRING.equals(source))
            && (clock != null))
            {
            final ImageIcon imageIcon;

            //LOGGER.debugProtocolEvent("SimpleInstrumentLogUIComponent.createInstrumentLogFragment() [event=" + event + "]");

            imageIcon = RegistryModelUtilities.getCommonIcon(status.getIconFilename());

            vecLogFragment.add(imageIcon);
            vecLogFragment.add(ChronosHelper.toDateString(clock.getCalendarDateNow()));
            vecLogFragment.add(ChronosHelper.toTimeString(clock.getCalendarTimeNow()));
            vecLogFragment.add(event);
            vecLogFragment.add(source);
            }

        return (vecLogFragment);
        }


    /***********************************************************************************************
     * Construct an SimpleInstrumentLogUIComponent for the specified Task.
     * The injected ObservatoryInstrument provides the InstrumentLog data.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     * @param name
     */

    public SimpleInstrumentLogUIComponent(final TaskPlugin task,
                                          final ObservatoryInstrumentInterface hostinstrument,
                                          final String resourcekey,
                                          final String name)
        {
        super(task,
              name,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              REFRESHABLE,
              REFRESH_CLICK,
              NON_REORDERABLE,
              TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              ReportTableToolbar.HORIZ_NORTH_RNG_PRT_RF_RV_TV_DA,
              RegistryModelUtilities.getCommonIcon(INSTRUMENT_LOG_ICON_FILENAME));

        this.hostInstrument = hostinstrument;

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }
        }


    /***********************************************************************************************
     * Initialise the UI of this Report.
     */

    public synchronized void initialiseUI()
        {
        final ContextActionGroup group;

        // This Log is used in a Runnable Task,
        // so start a new ContextActionGroup for this UIComponent,
        group = new ContextActionGroup(getReportUniqueName(), true, true);

        // Add the new Group to the list of Groups for this UIComponent
        clearUIComponentContextActionGroups();
        addUIComponentContextActionGroup(group);

        if (REGISTRY_MODEL.getLoggedInUser().getRole().isFrameworkViewer())
            {
            // Task-specific Context Actions
            final URL imageURL;

            imageURL = getClass().getResource(ACTION_ICON_TRUNCATE);

            if (imageURL != null)
                {
                final ContextAction truncateContextAction;

                truncateContextAction = new ContextAction(MENU_TRUNCATE,
                                                          new ImageIcon(imageURL),
                                                          MENU_TRUNCATE,
                                                          KeyEvent.VK_T,
                                                          true,
                                                          true)
                    {
                    public void actionPerformed(final ActionEvent event)
                        {
                        readResources();
                        truncateReport();
                        runUI();
                        }
                    };

                // Add the new ContextAction to the UIComponent ContextActionGroup
                group.addContextAction(truncateContextAction);
                }
            }

        // Now initialise the Report, and add any further ContextActions (e.g. Export & Print)
        super.initialiseUI();
        }


    /***********************************************************************************************
     * Run the UI of this ReportTable.
     */

    public synchronized void runUI()
        {
        super.runUI();

        // Clear up any left-over clones etc. every time the user selects this report
        ObservatoryInstrumentHelper.runGarbageCollector();
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(2);

        vecHeader.add(HEADER_TITLE);
        vecHeader.add("This report shows all Events related to this Instrument");

        ReportTableHelper.addDefaultFooter(getHostInstrument(), vecHeader, vecHeader.size());

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

        vecColumns = new Vector<ReportColumnMetadata>(LogHelper.DEFAULT_LOG_COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                                 SchemaDataType.STRING,
                                                 SchemaUnits.DIMENSIONLESS,
                                                 EMPTY_STRING,
                                                 SwingConstants.CENTER));
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
        vecColumns.add(new ReportColumnMetadata(TITLE_EVENT,
                                                 SchemaDataType.STRING,
                                                 SchemaUnits.DIMENSIONLESS,
                                                 EMPTY_STRING,
                                                 SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_SOURCE,
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
            // Use an icon which we know must exist
            RegistryModelUtilities.getCommonIcon(ICON_DUMMY),
            "2000-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the SimpleInstrumentLogUIComponent table.
     * The report is sorted in reverse order so that the most recent entry is at the top.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "SimpleInstrumentLogUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[isselectedinstrument="
                                    + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                    + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        // Only generate a Report if this UIComponent is visible
        // There is no need to auto-truncate
        if ((UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            && (getDataViewMode() != null)
            && (getDataViewLimit() > 0))
            {
            // Make a copy of the whole Log for truncation and sorting...
            vecReport = (Vector<Vector>) getInstrumentLog().clone();

            // All EventLogs are always sorted in descending order
            // Sort the copy of the Log by the 'hidden' last Integer column
            // See AbstractObservatoryInstrument.addInstrumentLogFragment()

            switch (getDataViewMode())
                {
                case SHOW_ALL:
                    {
                    Collections.sort(vecReport,
                                     new ReportRowsByColumn(LogHelper.DEFAULT_LOG_COLUMN_COUNT));
                    break;
                    }

                case SHOW_FIRST:
                    {
                    if (vecReport.size() > getDataViewLimit())
                        {
                        vecReport.setSize(getDataViewLimit());
                        }

                    Collections.sort(vecReport,
                                     new ReportRowsByColumn(LogHelper.DEFAULT_LOG_COLUMN_COUNT));
                    break;
                    }

                case SHOW_LAST:
                    {
                    if (vecReport.size() > getDataViewLimit())
                        {
                        // We have to do this because removeRange() is protected...
                        while (vecReport.size() > getDataViewLimit())
                            {
                            // Remove the *oldest* element
                            vecReport.removeElementAt(0);
                            }
                        }

                    Collections.sort(vecReport,
                                     new ReportRowsByColumn(LogHelper.DEFAULT_LOG_COLUMN_COUNT));
                    break;
                    }

                default:
                    {
                    LOGGER.error(SOURCE + "Unsupported DataViewMode");
                    }
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + "NOT VISIBLE");

            vecReport = new Vector<Vector>(1);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Generate the raw Report, i.e not truncated,
     * and regardless of whether the component is visible.
     * This is used for e.g. exports.
     *
     * @return Vector<Vector>
     *
     * @throws ReportException
     */

    public Vector<Vector> generateRawReport() throws ReportException
        {
        final Vector<Vector> vecReport;

        // Make a copy of the Log for sorting...
        vecReport = (Vector<Vector>) getInstrumentLog().clone();

        // Sort the copy of the Log by the 'hidden' last Integer column
        // All InstrumentLogs are sorted in descending order
        Collections.sort(vecReport, new ReportRowsByColumn(LogHelper.DEFAULT_LOG_COLUMN_COUNT));

        return (vecReport);
        }


    /***********************************************************************************************
     * Refresh the SimpleInstrumentLogUIComponent data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Truncate the InstrumentLog.
     */

    public synchronized final void truncateReport()
        {
        final int intChoice;
        final String [] strMessage =
            {
            "Are you sure that you wish to truncate the Instrument Log",
            "to leave" + SPACE + LOGSIZE_TRUNCATE + SPACE + "items?"
            };

        if ((getInstrumentLog() != null)
            && (getInstrumentLog().size() > LOGSIZE_TRUNCATE))
            {
            intChoice = JOptionPane.showOptionDialog(null,
                                                     strMessage,
                                                     getReportUniqueName(),
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     null,
                                                     null);

            while ((intChoice == JOptionPane.YES_OPTION)
                && (getInstrumentLog() != null)
                && (getInstrumentLog().size() > LOGSIZE_TRUNCATE))
                {
                getInstrumentLog().remove(0);
                }
            }
        }


    /***********************************************************************************************
     * Dispose of the Report.
     */

    public void disposeReport()
        {
        super.disposeReport();

        if ((getInstrumentLog() != null)
            && (!getInstrumentLog().isEmpty()))
            {
            getInstrumentLog().clear();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the SimpleInstrumentLogUIComponent.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
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
     * Get the InstrumentLog from the Instrument.
     *
     * @return Vector<Vector>
     */

    private synchronized Vector<Vector> getInstrumentLog()
        {
        return (getHostInstrument().getInstrumentLog());
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
