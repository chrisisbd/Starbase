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

import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.ObservatoryMonitor;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.CommandLifecycleUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.events.CommandLifecycleEventInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.DiscoveryUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.*;


/***************************************************************************************************
 * A general purpose CommandLifecycleUIComponent.
 */

public final class CommandLifecycleUIComponent extends ReportTable
                                               implements CommandLifecycleUIComponentInterface
    {
    // String Resources
    private static final String REPORT_NAME = "Command Log";
    private static final String REPORT_HEADER = "Command Report created at";
    private static final String NO_RESPONSE_EXPECTED = "No response expected";
    private static final String STARSCRIPT_NOT_FOUND = STARSCRIPT + " not found";

    private static final String TITLE_RESPONSE_STATUS = SPACE;  // Status Icon - Do not use EMPTY_STRING!
    private static final String TITLE_COMMAND_SCRIPT = STARSCRIPT;
    private static final String TITLE_RESPONSE_STATUS_CODE = "Status";
    private static final String TITLE_RESPONSE_VALUE = "Response Value";
    private static final String TITLE_RESPONSE_UNITS = "Units";
    private static final String TITLE_RESPONSE_DATATYPE = "DataType";
    private static final String TITLE_RESPONSE_NAME = "Response Name";
    private static final String TITLE_DATE_TX = "Date Tx";
    private static final String TITLE_TIME_TX = "Time Tx";
    private static final String TITLE_DATE_RX = "Date Rx";
    private static final String TITLE_TIME_RX = "Time Rx";

    private static final int COLUMN_COUNT = 11;
    private static final int DEFAULT_LOG_SIZE = 1000;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final ObservatoryInstrumentInterface monitoredInstrument;

    // The Log
    private final List<CommandLifecycleEventInterface> listLifecycleEntries;


    /***********************************************************************************************
     * Add a CommandLifecycleEvent to the specified Report.
     *
     * @param report
     * @param event
     */

    private static void addEvent(final Vector<Vector> report,
                                 final CommandLifecycleEventInterface event)
        {
        final CommandMessageInterface command;
        final ResponseMessageInterface response;
        final Vector<Object> vecRow;

        // This gets call a LOT, and so minimise the number of constructors etc.
        // Remember that all data entries in vecRow must be Strings (for export etc.)

        // Convenience calls
        command = event.getCommandMessage();
        response = event.getResponseMessage();
        vecRow = new Vector<Object>(COLUMN_COUNT);

        //------------------------------------------------------------------------------
        // An Icon to indicate the outcome of the Command, if the ResponseMessage has arrived

        if (response != null)
            {
            final int intStatus;
            final ImageIcon icon;

            // It is easier to look at the bitfield rather than the List of errors
            // when trying to get one icon?
            intStatus = response.getStatusBits();

            // If the Status is zero, all is well, otherwise it went wrong...
            // This is equivalent to the List having one entry of SUCCESS
            if (intStatus == 0)
                {
                icon = RegistryModelUtilities.getCommonIcon(ResponseMessageStatus.SUCCESS.getEventStatus().getIconFilename());

                vecRow.add(icon);
                }
            else
                {
                final Iterator<ResponseMessageStatus> iterRMS;
                ResponseMessageStatus rmsHighestPriority;

                iterRMS = response.getResponseMessageStatusList().iterator();

                // It is possible that the List contains SUCCESS and something else, so start with the lowest priority possible
                rmsHighestPriority = ResponseMessageStatus.SUCCESS;

                while (iterRMS.hasNext())
                    {
                    final ResponseMessageStatus status;

                    status = iterRMS.next();

                    // Find the highest priority status and use its icon
                    if (status.getEventStatusPriority() > rmsHighestPriority.getEventStatusPriority())
                        {
                        rmsHighestPriority = status;
                        }
                    }

                icon = RegistryModelUtilities.getCommonIcon(rmsHighestPriority.getEventStatus().getIconFilename());

                vecRow.add(icon);
                }
            }
        else
            {
            // No status is available until the ResponseMessage arrives
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // Add the StarScript representation of the Command

        if ((command != null)
            && (command.getStarScript() != null))
            {
            vecRow.add(command.getStarScript());
            }
        else
            {
            vecRow.add(STARSCRIPT_NOT_FOUND);
            }

        //------------------------------------------------------------------------------
        // The expanded Status Codes

        if ((response != null)
            && (response.getResponseMessageStatusList() != null))
            {
            vecRow.add(ResponseMessageStatus.expandResponseStatusCodes(response.getResponseMessageStatusList()));
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // Add the ResponseValue (if any)

        if ((response != null)
            && (response.getCommandType() != null)
            && (response.getCommandType().getResponse() != null)
            && (response.getCommandType().getResponse().getValue() != null))
            {
            vecRow.add(response.getCommandType().getResponse().getValue());
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // Add the Response Units

        if ((response != null)
            && (response.getCommandType() != null)
            && (response.getCommandType().getResponse() != null)
            && (response.getCommandType().getResponse().getUnits() != null))
            {
            vecRow.add(response.getCommandType().getResponse().getUnits().toString());
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // Add the Response DataType

        if ((response != null)
            && (response.getCommandType() != null)
            && (response.getCommandType().getResponse() != null)
            && (response.getCommandType().getResponse().getDataTypeName() != null))
            {
            vecRow.add(response.getCommandType().getResponse().getDataTypeName().toString());
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // The ResponseValue name, in its full StarScript form

        if (response != null)
            {
            vecRow.add(response.getStarScript());
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // Dates & Times of the CommandLifecycleEvent

        if (command != null)
            {
            final Calendar calendarTx;

            calendarTx = command.getTxCalendar();
            vecRow.add(ChronosHelper.toDateString(calendarTx));
            vecRow.add(ChronosHelper.toTimeString(calendarTx));
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            }

        if (response != null)
            {
            final Calendar calendarRx;

            calendarRx = response.getRxCalendar();
            vecRow.add(ChronosHelper.toDateString(calendarRx));
            vecRow.add(ChronosHelper.toTimeString(calendarRx));
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            vecRow.add(EMPTY_STRING);
            }

        report.add(vecRow);
        }


    /***********************************************************************************************
     * Construct a CommandLifecycleUIComponent.
     * If the monitored Instrument is null, then log events from *all* Instruments.
     * The ResourceKey is always that of the host Framework.
     *
     * @param task
     * @param hostinstrument
     * @param monitoredinstrument
     * @param hostresourcekey
     * @param toolbarstate
     */

    public CommandLifecycleUIComponent(final TaskPlugin task,
                                       final ObservatoryInstrumentInterface hostinstrument,
                                       final ObservatoryInstrumentInterface monitoredinstrument,
                                       final String hostresourcekey,
                                       final ReportTableToolbar toolbarstate)
        {
        super(task,
              REPORT_NAME,
              hostresourcekey,
              PRINTABLE,
              EXPORTABLE,
              NON_REFRESHABLE,
              REFRESH_NONE,
              NON_REORDERABLE,
              TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              toolbarstate, null);

        this.hostInstrument = hostinstrument;
        this.monitoredInstrument = monitoredinstrument;
        this.listLifecycleEntries = new ArrayList<CommandLifecycleEventInterface>(DEFAULT_LOG_SIZE);

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public synchronized final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(ReportTablePlugin.HEADER_ROWS_PER_COLUMN);

        vecHeader.add(getReportUniqueName());
        vecHeader.add("This report shows all commands executed by the Instrument");

        ReportTableHelper.addDefaultFooter(getHostInstrument(), vecHeader, vecHeader.size());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public synchronized final Vector<ReportColumnMetadata> defineColumns()
        {
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_STATUS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The status of the Command execution",
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_COMMAND_SCRIPT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Starscript which has been executed",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_STATUS_CODE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Status code returned by the Instrument",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_VALUE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Response returned by the Instrument",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_UNITS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Units of the Response Value",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_DATATYPE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The DataType of the Response Value",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_NAME,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Name of the Response Value",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DATE_TX,
                                                SchemaDataType.DATE,
                                                SchemaUnits.YEAR_MONTH_DAY,
                                                "The Date when the Command was executed",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME_TX,
                                                SchemaDataType.TIME,
                                                SchemaUnits.HOUR_MIN_SEC,
                                                "The Time when the Command was executed",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DATE_RX,
                                                SchemaDataType.DATE,
                                                SchemaUnits.YEAR_MONTH_DAY,
                                                "The Date when the Response was received",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME_RX,
                                                SchemaDataType.TIME,
                                                SchemaUnits.HOUR_MIN_SEC,
                                                "The Time when the Response was received",
                                                SwingConstants.LEFT ));
        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public synchronized final Object [] defineColumnWidths()
        {
        final Object [] columnWidths;

        columnWidths = new Object[]
            {
            IMAGE_ICON_PLAIN,
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMM",
            "MMMMMMM",
            "MMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMM",
            "2000-00-00",
            "00:00:00",
            "2000-00-00",
            "00:00:00"
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

        LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                               "CommandLifecycleUIComponent.generateReport() [isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()) + "]");

        // Only generate a Report if this UIComponent is showing
        // Trap the case of disposeUI(), when there won't be a ReportTable
        if ((ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()))
            && (getReportTable() != null)
            && (UIComponentHelper.isComponentShowing(getReportTable().getTableHeader())))
            {
            int intLogSizeTruncate;

            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   "CommandLifecycleUIComponent.generateReport() SHOWING");

            intLogSizeTruncate = REGISTRY.getIntegerProperty(getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX);

            if (intLogSizeTruncate <= 0)
                {
                intLogSizeTruncate = DEFAULT_DATA_VIEW_LIMIT;
                }

            vecReport = new Vector<Vector>(DEFAULT_LOG_SIZE);

            // Generate the report from the data in the CommandLifecycleEvent List
            if ((getCommandLifecycleEntries() != null)
                && (!getCommandLifecycleEntries().isEmpty()))
                {
                final Iterator<CommandLifecycleEventInterface> iterEntries;
                final int intLogSize;
                int intCounter;

                iterEntries = getCommandLifecycleEntries().iterator();
                intLogSize = getCommandLifecycleEntries().size();
                intCounter = 0;

                while ((iterEntries != null)
                    && (iterEntries.hasNext()))
                    {
                    final CommandLifecycleEventInterface event;

                    event = iterEntries.next();
                    intCounter++;

                    if (event != null)
                        {
                        // Only add a maximum of the most recent intLogSizeTruncate entries to the Log
                        if ((intLogSize < intLogSizeTruncate)
                            || (intCounter > (intLogSize - intLogSizeTruncate)))
                            {
                            addEvent(vecReport, event);
                            }
                        }
                    }
                }
            }
        else
            {
            LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                                   "CommandLifecycleUIComponent.generateReport() NOT SHOWING");

            vecReport = new Vector<Vector>(1);
            }

        return (vecReport);
        }


    /***********************************************************************************************
     * Generate the raw Report, i.e not truncated,
     * and regardless of whether the component is visible. This is used for e.g. exports.
     *
     * @return Vector<Vector>
     *
     * @throws ReportException
     */

    public Vector<Vector> generateRawReport() throws ReportException
        {
        final Vector<Vector> vecReport;

        LOGGER.debugTimedEvent(FrameworkSingletons.LOADER_PROPERTIES.isTimingDebug(),
                               "CommandLifecycleUIComponent.generateRawReport()");

        vecReport = new Vector<Vector>(DEFAULT_LOG_SIZE);

        // Generate the report from the data in the CommandLifecycleEntries List
        if ((getCommandLifecycleEntries() != null)
            && (!getCommandLifecycleEntries().isEmpty()))
            {
            final Iterator<CommandLifecycleEventInterface> iterEntries;

            iterEntries = getCommandLifecycleEntries().iterator();

            while ((iterEntries != null)
                && (iterEntries.hasNext()))
                {
                final CommandLifecycleEventInterface event;

                event = iterEntries.next();

                if (event != null)
                    {
                    // Add all available Events
                    addEvent(vecReport, event);
                    }
                }
            }

        return (vecReport);
        }



    /***********************************************************************************************
     * Refresh the Report data table.
     *
     * @return Vector
     */

    public synchronized final Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Dispose of the Report.
     */

    public void disposeReport()
        {
        super.disposeReport();

        if ((getCommandLifecycleEntries() != null)
            && (!getCommandLifecycleEntries().isEmpty()))
            {
            getCommandLifecycleEntries().clear();
            }
        }


    /***********************************************************************************************
     * Run the UI of this ReportTable.
     */

    public synchronized void runUI()
        {
        // ToDo Review this?!
        super.runUI();
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        super.disposeUI();

        if (getCommandLifecycleEntries() != null)
            {
            getCommandLifecycleEntries().clear();
            }
        }


    /***********************************************************************************************
     * Truncate the underlying List of CommandLifecycleEntries.
     */

    public synchronized final void truncateReport()
        {
        final int intChoice;
        final String [] strMessage =
            {
            "Are you sure that you wish to truncate the Command Log",
            "to leave" + SPACE + LOGSIZE_TRUNCATE + SPACE + "items?"
            };

        if ((getCommandLifecycleEntries() != null)
            && (getCommandLifecycleEntries().size() > LOGSIZE_TRUNCATE))
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
                   && (getCommandLifecycleEntries() != null)
                   && (getCommandLifecycleEntries().size() > LOGSIZE_TRUNCATE))
                {
                getCommandLifecycleEntries().remove(0);
                }
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the CommandLifecycleUIComponent.
     */

    public final void readResources()
        {
        super.readResources();
        }


    /***********************************************************************************************
     * Indicate that there has been a CommandLifecycleEvent.
     * If the MonitoredInstrument is null, then log events from *all* Instruments,
     * otherwise just for the host.
     *
     * @param event
     */

    public synchronized final void commandChanged(final CommandLifecycleEventInterface event)
        {
        // If the event is invalid, do nothing. If the context is non-null,
        // select only those messages coming from the same DAO as the host Instrument,
        // otherwise log everything in the Observatory

        // Only monitor events if the host Instrument is running
        // Have we seen this CommandLifecycleEvent before?
        if ((InstrumentState.isDoingSomething(getHostInstrument()))
            && (isValidEvent(event)))
            {
            if (!getCommandLifecycleEntries().contains(event))
                {
                // Record the event and an empty new row for the log
                getCommandLifecycleEntries().add(event);
                }

            // Force an immediate update using the new data, if any
            // We need to refresh whenever an event is supplied,
            // regardless of having it already,
            // in case it is the response phase of an existing command
            // Trap the case of disposeUI(), when there won't be a ReportTable
            if ((ObservatoryUIHelper.isSelectedInstrument(getHostInstrument()))
                && (getReportTable() != null)
                && (UIComponentHelper.isComponentShowing(getReportTable().getTableHeader())))
                {
                refreshTable();
                }
            }
        }


    /***********************************************************************************************
     * Check to see if the CommandLifecycleEvent is valid for this Log.
     *
     * @param event
     *
     * @return boolean
     */

    private boolean isValidEvent(final CommandLifecycleEventInterface event)
        {
        boolean boolValid;

        // Check we have a valid event and message(s)
        boolValid = ((getCommandLifecycleEntries() != null)
                        && (event != null)
                        && (event.getSource() != null)
                        && (((event.getCommandMessage() != null)
                            && (event.getCommandMessage().getInstrument() != null))
                        || ((event.getResponseMessage() != null)
                            && (event.getResponseMessage().getInstrument() != null))));

        // Only allow Log updates if there is no Instrument at all (i.e. log everything),
        // or if a valid host Instrument is READY
        boolValid = boolValid
                    && (
                            ((getMonitoredInstrument() == null)
                                && (getHostInstrument() instanceof ObservatoryMonitor)
                                && (((ObservatoryMonitor)getHostInstrument()).isCommandMonitorRunning()))
                        ||
                            ((getMonitoredInstrument() != null)
                                && (InstrumentState.isDoingSomething(getMonitoredInstrument())))
                       );

        // If there is a READY Instrument,
        // select only those messages coming from the same Instrument as the host Instrument
        if ((boolValid)
            && (getMonitoredInstrument() != null)
            && (InstrumentState.isDoingSomething(getMonitoredInstrument())))
            {
            // The Instrument Identifier in the CommandMessage must come from the MonitoredInstrument
            if (event.getCommandMessage() != null)
                {
                boolValid = (event.getCommandMessage().getInstrument().getIdentifier().equals(getMonitoredInstrument().getInstrument().getIdentifier()));
                }

            // If the message is a result of activity by the DiscoveryController,
            // then the ResponseMessage Instrument Identifier may not be as expected
            if (event.getResponseMessage() != null)
                {
                final boolean boolInstrumentController;
                final boolean boolDiscoveryController;

                // Only one of these can be valid:
                boolInstrumentController = event.getResponseMessage().getInstrument().getIdentifier().equals(getMonitoredInstrument().getInstrument().getIdentifier());
                boolDiscoveryController = event.getResponseMessage().getInstrument().getIdentifier().equals(DiscoveryUtilities.DISCOVERY_CONTROLLER_IDENTIFIER);
                boolValid = boolValid && (boolInstrumentController || boolDiscoveryController);
                }
            }

        return (boolValid);
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
     * Get the ObservatoryInstrument which this UIComponent is monitoring (may be null).
     *
     * @return ObservatoryInstrumentInterface
     */

    private synchronized ObservatoryInstrumentInterface getMonitoredInstrument()
        {
        return (this.monitoredInstrument);
        }


    /***********************************************************************************************
     * Get the List of CommandLifecycleEvents to be shown on the CommandLifecycleUIComponent.
     *
     * @return List<CommandLifecycleEventInterface>
     */

    public synchronized List<CommandLifecycleEventInterface> getCommandLifecycleEntries()
        {
        return (this.listLifecycleEntries);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
