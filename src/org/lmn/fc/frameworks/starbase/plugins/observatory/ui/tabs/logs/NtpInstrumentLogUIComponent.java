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
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
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
 * An NtpInstrumentLogUIComponent.
 */

public final class NtpInstrumentLogUIComponent extends ReportTable
                                               implements ReportTablePlugin
    {
    // String Resources
    private static final String HEADER_TITLE = "Network Time Protocol";
    private static final String MSG_REPORT_CREATED = "NTP Report created at ";

    private static final String TITLE_ICON              = SPACE;
    private static final String TITLE_SET               = "Set";
    private static final String TITLE_DATE              = "Date";
    private static final String TITLE_TIME              = "Time";
    private static final String TITLE_TIME_SERVER       = "Time Server";
    private static final String TITLE_SERVER_ADDRESS    = "Server Address";
    private static final String TITLE_VERSION           = "Version";
    private static final String TITLE_OFFSET            = "Offset msec";
    private static final String TITLE_DELAY             = "Delay msec";
    private static final String TITLE_STRATUM           = "Stratum";
    private static final String TITLE_PRECISION         = "Precision msec";
    private static final String TITLE_STATUS            = "Status";
    private static final String TITLE_TIMESTAMP_DATE    = "DateStamp";
    private static final String TITLE_TIMESTAMP_TIME    = "Time";
    private static final String TITLE_TIMESTAMP_FRACTION = "Seconds";

    private static final String MENU_TRUNCATE = "Truncate NTP Log";

    public static final int LOG_COLUMN_COUNT = 15;

    private final ObservatoryInstrumentInterface hostInstrument;


    /***********************************************************************************************
     * Construct an NtpInstrumentLogUIComponent for the specified Task.
     * The injected ObservatoryInstrument provides the InstrumentLog data.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     * @param name
     */

    public NtpInstrumentLogUIComponent(final TaskPlugin task,
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
     * Run the UI of this Report.
     */

    public synchronized void runUI()
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
            final URL imageURL;

            imageURL = getClass().getResource(ACTION_ICON_TRUNCATE);

            if (imageURL != null)
                {
                final ContextAction truncateContextAction;

                truncateContextAction = new ContextAction(MENU_TRUNCATE,
                                                          new ImageIcon(imageURL),
                                                          getReportUniqueName() + COLON + SPACE + MENU_TRUNCATE,
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

        // Now run the Report, and add any further ContextActions (e.g. Export & Print)
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

        vecColumns = new Vector<ReportColumnMetadata>(LOG_COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_SET,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.CENTER ));
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
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME_SERVER,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_SERVER_ADDRESS,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_VERSION,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_OFFSET,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DELAY,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STRATUM,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_PRECISION,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STATUS,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIMESTAMP_DATE,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIMESTAMP_TIME,
                                            SchemaDataType.STRING,
                                            SchemaUnits.DIMENSIONLESS,
                                            EMPTY_STRING,
                                            SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIMESTAMP_FRACTION,
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
            Boolean.TRUE,
            "2000-00-00",
            "00:00:00",
            "MMMMMMMMMMMMMMM",
            "MMMMMMMMMMM",
            "0",
            "000000",
            "000000",
            "0",
            "00000000000000E00",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "2000-00-00",
            "00:00:00",
            "99999999"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Generate the NtpLog data table directly from the InstrumentLog.
     * The report is sorted in reverse order so that the most recent entry is at the top.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "NtpInstrumentLogUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               SOURCE + "[isselectedinstrument="
                                    + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                    + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        // Only generate a Report if this UIComponent is visible
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
                                     new ReportRowsByColumn(LOG_COLUMN_COUNT));
                    break;
                    }

                case SHOW_FIRST:
                    {
                    if (vecReport.size() > getDataViewLimit())
                        {
                        vecReport.setSize(getDataViewLimit());
                        }

                    Collections.sort(vecReport,
                                     new ReportRowsByColumn(LOG_COLUMN_COUNT));
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
                                     new ReportRowsByColumn(LOG_COLUMN_COUNT));
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
     * Refresh the NtpLog data table.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Truncate the NtpLog.
     */

    public synchronized final void truncateReport()
        {
        final int intChoice;
        final String [] strMessage =
            {
            "Are you sure that you wish to truncate the NTP Log",
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
     * Read all the Resources required by the NtpInstrumentLogUIComponent.
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
