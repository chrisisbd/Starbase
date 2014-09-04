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

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.monitor.ObservatoryMonitor;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortMessageEventInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.events.PortMessageListener;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTablePlugin;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.*;


/***************************************************************************************************
 * A general purpose PortMonitorUIComponent.
 */

public final class PortMonitorUIComponent extends ReportTable
                                          implements PortMessageListener,
                                                     ReportTablePlugin,
                                                     FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata
    {
    // String Resources
    private static final String REPORT_NAME = "Port ObservatoryMonitor";
    private static final String REPORT_HEADER = "Port ObservatoryMonitor Report created at";

    private static final String TITLE_INDEX = "Index";
    private static final String TITLE_DATE = "Date";
    private static final String TITLE_TIME = "Time";
    private static final String TITLE_PORT = "Port";
    private static final String TITLE_STREAM = "Stream";
    private static final String TITLE_TRAFFIC = "Traffic";
    private static final String TITLE_HEX = "Traffic as Hex";
    private static final String TITLE_SCRIPT = STARSCRIPT;
    private static final String TITLE_RESPONSE_VALUE = "Response Value";
    private static final String TITLE_RESPONSE_UNITS = "Units";
    private static final String TITLE_RESPONSE_DATATYPE = "Response DataType";
    private static final String TITLE_RESPONSE_STATUS = "Response Status";
    private static final String TITLE_RESPONSE_STATUS_CODES = "Status Codes";

    private static final int COLUMN_COUNT = 13;
    private static final int DEFAULT_LOG_SIZE = 100;

    private final ObservatoryInstrumentInterface hostInstrument;
    private final ObservatoryInstrumentInterface monitoredInstrument;
    private final List<PortMessageEventInterface> listPortMessageEntries;


    /***********************************************************************************************
     * Add a PortMessageEvent to the Log.
     *
     * @param report
     * @param event
     * @param index
     */

    private static void addEvent(final Vector<Vector> report,
                                 final PortMessageEventInterface event,
                                 final int index)
        {
        final CommandMessageInterface command;
        final ResponseMessageInterface response;
        final Vector<Object> vecRow;

        // Remember that all data entries in vecRow must be Strings (for export etc.)

        // Convenience calls
        command = event.getCommandMessage();
        response = event.getResponseMessage();
        vecRow = new Vector<Object>(COLUMN_COUNT);

        //------------------------------------------------------------------------------
        // Add a Row Index

        vecRow.add(Integer.toString(index));

        //------------------------------------------------------------------------------
        // Date & Time of the PortMessageEvent Command or Response

        if (command != null)
            {
            final Calendar calendarTx;

            calendarTx = command.getTxCalendar();
            vecRow.add(ChronosHelper.toDateString(calendarTx));
            vecRow.add(ChronosHelper.toTimeString(calendarTx));
            }
        else if (response != null)
            {
            final Calendar calendarRx;

            calendarRx = response.getRxCalendar();
            vecRow.add(ChronosHelper.toDateString(calendarRx));
            vecRow.add(ChronosHelper.toTimeString(calendarRx));
            }
        else
            {
            vecRow.add(QUERY);
            vecRow.add(QUERY);
            }

        //------------------------------------------------------------------------------
        // Add the Port and Stream
        // (the same for Command or Response, but we only have one)

        if (command != null)
            {
            if ((command.getDAO() != null)
                && (command.getDAO().getPort() != null))
                {
                vecRow.add(command.getDAO().getPort().getName());

                // Commands are Transmitted
                if (command.getCommandType().getSendToPort())
                    {
                    vecRow.add(STREAM_TX_REMOTE);
                    }
                else
                    {
                    vecRow.add(STREAM_TX_LOCAL);
                    }
                }
            else
                {
                vecRow.add("None");
                vecRow.add(EMPTY_STRING);
                }
            }
        else if (response != null)
            {
            if ((response.getPortName() != null))
                {
                vecRow.add(response.getPortName());

                // Responses are Received
                if (response.getCommandType().getSendToPort())
                    {
                    vecRow.add(STREAM_RX_REMOTE);
                    }
                else
                    {
                    vecRow.add(STREAM_RX_LOCAL);
                    }
                }
            else
                {
                vecRow.add("None");
                vecRow.add(EMPTY_STRING);
                }
            }
        else
            {
            vecRow.add(QUERY);
            vecRow.add(QUERY);
            }

        //------------------------------------------------------------------------------
        // Now add the Traffic as ASCII and HEX

        if (command != null)
            {
            if (command.getByteArray() != null)
                {
                // ASCII: Control characters are expanded for clarity
                vecRow.add(Utilities.byteArrayToExpandedAscii(command.getByteArray()));

                // HEX: AA BB CC ...
                vecRow.add(Utilities.byteArrayToSpacedHex(command.getByteArray()));
                }
            else
                {
                vecRow.add(QUERY);
                vecRow.add(QUERY);
                }
            }
        else if (response != null)
            {
            if (response.getByteArray() != null)
                {
                // ASCII: Control characters are expanded for clarity
                vecRow.add(Utilities.byteArrayToExpandedAscii(response.getByteArray()));

                // HEX: AA BB CC ...
                vecRow.add(Utilities.byteArrayToSpacedHex(response.getByteArray()));
                }
            else
                {
                vecRow.add(QUERY);
                vecRow.add(QUERY);
                }
            }
        else
            {
            vecRow.add(QUERY);
            vecRow.add(QUERY);
            }

        //------------------------------------------------------------------------------
        // Now add the StarScript representation of the traffic

        if ((command != null)
            && (command.getStarScript() != null))
            {
            vecRow.add(command.getStarScript());
            }
        else if ((response != null)
            && (response.getStarScript() != null))
            {
            vecRow.add(response.getStarScript());
            }
        else
            {
            vecRow.add(MSG_NO_STARSCRIPT);
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
        // Add the ResponseStatus bitfield text

        if (response != null)
            {
            vecRow.add(Utilities.intToBitString(response.getStatusBits()));
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        //------------------------------------------------------------------------------
        // Finally add the expanded Status Codes

        if ((response != null)
            && (response.getResponseMessageStatusList() != null))
            {
            vecRow.add(ResponseMessageStatus.expandResponseStatusCodes(response.getResponseMessageStatusList()));
            }
        else
            {
            vecRow.add(EMPTY_STRING);
            }

        report.add(vecRow);
        }


    /***********************************************************************************************
     * Construct a PortMonitorUIComponent.
     * If the Instrument is null, then log events from *all* Instruments.
     * The ResourceKey is always that of the host Framework.
     *
     * @param task
     * @param hostinstrument
     * @param monitoredinstrument
     * @param resourcekey
     */

    public PortMonitorUIComponent(final TaskPlugin task,
                                  final ObservatoryInstrumentInterface hostinstrument,
                                  final ObservatoryInstrumentInterface monitoredinstrument,
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

        this.hostInstrument = hostinstrument;
        this.monitoredInstrument = monitoredinstrument;
        this.listPortMessageEntries = new Vector<PortMessageEventInterface>(DEFAULT_LOG_SIZE);

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }
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
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata(TITLE_INDEX,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The index number of the log entry",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DATE,
                                                SchemaDataType.DATE,
                                                SchemaUnits.YEAR_MONTH_DAY,
                                                "The Date of the log entry",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TIME,
                                                SchemaDataType.TIME,
                                                SchemaUnits.HOUR_MIN_SEC,
                                                "The Time of the log entry",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_PORT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Port on which the traffic occurred",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STREAM,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Stream on which the traffic occurred",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TRAFFIC,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The data transferred to or from the Port",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_HEX,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The data transferred to or from the Port, in HEX",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_SCRIPT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Starscript Command executed",
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
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_STATUS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The overall Status returned by the Instrument",
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_RESPONSE_STATUS_CODES,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                "The Status codes returned by the Instrument",
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
        final List<String> listColumnWidths;

        listColumnWidths = new ArrayList<String>(COLUMN_COUNT);

        listColumnWidths.add("MMMMMMM");                                // Index
        listColumnWidths.add("MMMMMMM");                                // Date
        listColumnWidths.add("MMMMMMM");                                // Time
        listColumnWidths.add("MMMMMMM");                                // Port
        listColumnWidths.add("MM");                                     // Stream (Tx or Rx)
        listColumnWidths.add("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");  // ASCII
        listColumnWidths.add("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");  // HEX
        listColumnWidths.add("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");  // Script
        listColumnWidths.add("MMMMMMMMMMMM");                           // Value
        listColumnWidths.add("MMMMMMMMMMMM");                           // Units
        listColumnWidths.add("MMMMMMMMMMMM");                           // DataType
        listColumnWidths.add("MMMMMMMMMMMM");                           // Status
        listColumnWidths.add("MMMMMMMMMMMM");                           // Status Codes

        return (listColumnWidths.toArray());
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public synchronized final Vector<Vector> generateReport()
        {
        final Vector<Vector> vecReport;

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "PortMonitorUIComponent.generateReport() [isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        // Only generate a Report if this UIComponent is visible
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            int intLogSizeTruncate;

            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "PortMonitorUIComponent.generateReport() VISIBLE");

            intLogSizeTruncate = REGISTRY.getIntegerProperty(getHostInstrument().getHostAtom().getResourceKey() + KEY_DISPLAY_DATA_MAX);

            if (intLogSizeTruncate <= 0)
                {
                intLogSizeTruncate = DEFAULT_DATA_VIEW_LIMIT;
                }

            vecReport = new Vector<Vector>(DEFAULT_LOG_SIZE);

            if ((getPortMessageEntries() != null)
                && (!getPortMessageEntries().isEmpty()))
                {
                final Iterator<PortMessageEventInterface> iterEntries;
                final int intLogSize;
                int intIndex;

                iterEntries = getPortMessageEntries().iterator();
                intLogSize = getPortMessageEntries().size();
                intIndex = -1;

                while ((iterEntries != null)
                    && (iterEntries.hasNext()))
                    {
                    final PortMessageEventInterface event;

                    event = iterEntries.next();
                    intIndex++;

                    if (event != null)
                        {
                        // Only add a maximum of the most recent DEFAULT_DATA_VIEW_LIMIT entries to the Log
                        if ((intLogSize < intLogSizeTruncate)
                            || (intIndex > (intLogSize - intLogSizeTruncate)))
                            {
                            addEvent(vecReport, event, intIndex);
                            }
                        }
                    }
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "PortMonitorUIComponent.generateReport() NOT SHOWING");

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

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "PortMonitorUIComponent.generateRawReport()");

        vecReport = new Vector<Vector>(DEFAULT_LOG_SIZE);

        // Generate the report from the data in the PortMessageEntries List
        if ((getPortMessageEntries() != null)
            && (!getPortMessageEntries().isEmpty()))
            {
            final Iterator<PortMessageEventInterface> iterEntries;
            int intIndex;

            iterEntries = getPortMessageEntries().iterator();
            intIndex = 0;

            while ((iterEntries != null)
                && (iterEntries.hasNext()))
                {
                final PortMessageEventInterface event;

                event = iterEntries.next();

                if (event != null)
                    {
                    // Add all available Events
                    addEvent(vecReport, event, intIndex);
                    intIndex++;
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

    public synchronized Vector<Vector> refreshReport()
        {
        return (generateReport());
        }


    /***********************************************************************************************
     * Dispose of all UI components and remove the Toolbar Actions.
     */

    public synchronized void disposeUI()
        {
        super.disposeUI();

        if (getPortMessageEntries() != null)
            {
            getPortMessageEntries().clear();
            }
        }


    /***********************************************************************************************
     * Read all the Resources required by the PortMonitorUIComponent.
     */

    public final void readResources()
        {
        super.readResources();
        }


    /***********************************************************************************************
     * Indicate that there has been a PortMessageEvent.
     * If the Instrument is null, then log events from *all* Instruments, otherwise just for the host.
     *
     * @param event
     */

    public synchronized final void messageChanged(final PortMessageEventInterface event)
        {
        // If the event is invalid, do nothing. If the Instrument is non-null,
        // select only those messages coming from the host Instrument, otherwise log everything

        // Only monitor events if the host Instrument is running
        // Have we seen this CommandLifecycleEvent before?
        if ((InstrumentState.isDoingSomething(getHostInstrument()))
            && (getHostInstrument() != null)
            && (getHostInstrument() instanceof ObservatoryMonitor)
            && (((ObservatoryMonitor)getHostInstrument()).isPortMonitorRunning())
            && (isValidEvent(event)))
            {
            if (!getPortMessageEntries().contains(event))
                {
                // Record the event and an empty new row for the log
                getPortMessageEntries().add(event);
                }

            // Force an immediate update using the new data, if any
            // We need to refresh whenever an event is supplied,
            // regardless of having it already,
            // in case it is the response phase of an existing command
            // Refresh only if visible
            if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
                {
                refreshTable();
                }
            }
        }


    /***********************************************************************************************
     * Check to see if the PortMessageEvent is valid for this Log.
     *
     * @param event
     *
     * @return boolean
     */

    private boolean isValidEvent(final PortMessageEventInterface event)
        {
        boolean boolValid;

        // Check we have a valid event and message(s)
        boolValid = ((getPortMessageEntries() != null)
                        && (event != null)
                        && (event.getSource() != null)
                        && (((event.getCommandMessage() != null)
                            && (event.getCommandMessage().getInstrument() != null))
                        || ((event.getResponseMessage() != null)
                            && (event.getResponseMessage().getInstrument() != null))));

        // Only allow Log updates if there is no Instrument at all (i.e. log everything),
        // or if a valid host Instrument is READY
        boolValid = boolValid
                    && ((getMonitoredInstrument() == null)
                    || ((getMonitoredInstrument() != null)
                        && (InstrumentState.isDoingSomething(getMonitoredInstrument()))));

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
     * Get the List of PortMessageEvents to be shown on the PortMonitorUIComponent.
     *
     * @return List<PortMessageEventInterface>
     */

    public synchronized List<PortMessageEventInterface> getPortMessageEntries()
        {
        return (this.listPortMessageEntries);
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
