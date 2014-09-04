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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.scanner;

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.NetworkScannerUIComponentInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.ReportColumnMetadata;
import org.lmn.fc.ui.reports.ReportTable;
import org.lmn.fc.ui.reports.ReportTableToolbar;

import javax.swing.*;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * A NetworkScannerUIComponent.
 */

public final class NetworkScannerUIComponent extends ReportTable
                                             implements NetworkScannerUIComponentInterface
    {
    // String Resources
    private static final String HEADER_TITLE            = "Starinet Network Scanner";
    private static final String MSG_REPORT_CREATED      = "Network Scanner Report created at ";

    private static final String TITLE_ICON              = SPACE;
    private static final String TITLE_ADDRESS_IP        = "IP Address";
    private static final String TITLE_PORT              = "Port";
    private static final String TITLE_STATUS            = "Status";
    private static final String TITLE_HOSTNAME          = "Hostname";
    private static final String TITLE_ADDRESS_MAC       = "MAC Address";
    private static final String TITLE_VERSION           = "Version";
    private static final String TITLE_TIMESTAMP_DATE    = "Date";
    private static final String TITLE_TIMESTAMP_TIME    = "Time";

    private static final int COLUMN_COUNT = 9;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;

    // The Network Scanner Data
    private List<NetworkScannerData> listScanData;


    /***********************************************************************************************
     * Construct an NetworkScannerUIComponent for the specified Task.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     */

    public NetworkScannerUIComponent(final TaskPlugin task,
                                     final ObservatoryInstrumentInterface hostinstrument,
                                     final String resourcekey)
        {
        super(task,
              HEADER_TITLE,
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

        // The Network Scanner Data
        this.listScanData = null;

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName() + SPACE + getReportTabName());
            }
        }


    /***********************************************************************************************
     * Generate the report header.
     *
     * @return Vector
     */

    public final Vector<String> generateHeader()
        {
        final String SOURCE = "NetworkScannerUIComponent.generateHeader() ";

        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(2);

        vecHeader.add(HEADER_TITLE);
        vecHeader.add(MSG_REPORT_CREATED + SPACE + Chronos.timeNow());

        return (vecHeader);
        }


    /***********************************************************************************************
     * Define the report columns.
     *
     * @return Vector
     */

    public final Vector<ReportColumnMetadata> defineColumns()
        {
        final String SOURCE = "NetworkScannerUIComponent.defineColumns() ";
        final Vector<ReportColumnMetadata> vecColumns;

        vecColumns = new Vector<ReportColumnMetadata>(COLUMN_COUNT);

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_ADDRESS_IP,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_PORT,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_STATUS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_HOSTNAME,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_ADDRESS_MAC,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_VERSION,
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
        return (vecColumns);
        }


    /***********************************************************************************************
     * Define the widths of each column in terms of the objects which they will contain.
     *
     * @return Object []
     */

    public final Object [] defineColumnWidths()
        {
        final String SOURCE = "NetworkScannerUIComponent.defineColumnWidths() ";
        final Object [] columnWidths =
            {
            // Use an icon which we know must exist
            RegistryModelUtilities.getCommonIcon(ICON_DUMMY),
            "MMM.MMM.MMM.MMM",
            "MMMMM",
            "Unreachable Port",
            "StarinetControllerMMMM.home",
            "MM:MM:MM:MM:MM:MM:MM:MM",
            "MM.MM",
            "2000-00-00",
            "00:00:00"
            };

        return (columnWidths);
        }


    /***********************************************************************************************
     * Refresh the Report.
     *
     * @return Vector
     */

    public final Vector<Vector> refreshReport()
        {
        final String SOURCE = "NetworkScannerUIComponent.refreshReport() ";

        return (generateReport());
        }


    /***********************************************************************************************
     * Generate the NetworkScanner log.
     *
     * @return Vector of report rows
     */

    public final Vector<Vector> generateReport()
        {
        final String SOURCE = "NetworkScannerUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        // Only generate a Report if this UIComponent is visible
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            vecReport = generateRawReport();
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
        final String SOURCE = "NetworkScannerUIComponent.generateRawReport() ";

        return (NetworkScannerHelper.convertScanDataToReport(getScanDataList(),
                                                             COLUMN_COUNT));
        }


    /***********************************************************************************************
     * Dispose of the Report.
     */

    public void disposeReport()
        {
        final String SOURCE = "NetworkScannerUIComponent.disposeReport() ";
        super.disposeReport();

        if ((getScanDataList() != null)
            && (!getScanDataList().isEmpty()))
            {
            getScanDataList().clear();
            }
        }


    /***********************************************************************************************
     * Set the data from the DAO finished() method, or from any Command doing a realtime update.
     * The NetworkScanner data appear in the UserObject.
     *
     * @param daowrapper
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatemetadata)
        {
        final String SOURCE = "NetworkScannerUIComponent.setWrappedData() ";

        if ((daowrapper != null)
            && (daowrapper.getUserObject() instanceof List))
            {
            setScanDataList((List<NetworkScannerData>) daowrapper.getUserObject());
            refreshTable();
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid DAO UserObject, so unable to refresh scan table");

            setScanDataList(null);
            refreshTable();
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the List of Network Scan data.
     *
     * @return List<NetworkScannerData>
     */

    private List<NetworkScannerData> getScanDataList()
        {
        return (this.listScanData);
        }


    /***********************************************************************************************
     * Set the List of Network Scan data.
     *
     * @param data
     */

    private void setScanDataList(final List<NetworkScannerData> data)
        {
        this.listScanData = data;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Read all the Resources required by the NetworkScannerUIComponent.
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     */

    public final void readResources()
        {
        final String SOURCE = "NetworkScannerUIComponent.readResources() ";

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
