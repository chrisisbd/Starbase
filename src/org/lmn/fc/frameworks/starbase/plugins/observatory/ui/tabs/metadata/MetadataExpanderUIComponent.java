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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.metadata;


import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExpanderUIComponentInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * MetadataExpanderUIComponent.
 */

public final class MetadataExpanderUIComponent extends ReportTable
                                               implements MetadataExpanderUIComponentInterface
    {
    private static final long serialVersionUID = -7801206994762298731L;

    private static final int COLUMN_INDEX_KEY = 0;

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private List<Metadata> listMetadata;


    /***********************************************************************************************
     * Construct an MetadataExpanderUIComponent.
     * The metadata is contained in the List of Metadata (name, value, units, type)
     * The ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param metadata
     * @param hostresourcekey
     */

    public MetadataExpanderUIComponent(final TaskPlugin task,
                                       final ObservatoryInstrumentInterface hostinstrument,
                                       final List<Metadata> metadata,
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
              ReportTableToolbar.NONE,
              null);

        // Injections
        this.hostInstrument = hostinstrument;
        this.listMetadata = metadata;

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

    public final Vector<String> generateHeader()
        {
        final Vector<String> vecHeader;

        vecHeader = new Vector<String>(ReportTablePlugin.HEADER_ROWS_PER_COLUMN);

        vecHeader.add(getReportUniqueName());
        vecHeader.add("This report shows all Metadata currently loaded for the Framework and the Observatory");

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

        vecColumns = new Vector<ReportColumnMetadata>(defineColumnWidths().length);

        vecColumns.add(new ReportColumnMetadata(TITLE_NAME,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_VALUE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_UNITS,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_TYPE,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        vecColumns.add(new ReportColumnMetadata(TITLE_DESCRIPTION,
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
            "MMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMM",
            "MMMMMMMMM",
            "MMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
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
        final String SOURCE = "MetadataExpanderUIComponent.generateReport() ";
        final Vector<Vector> vecReport;

        //        LOGGER.log("MetadataExpanderUIComponent.generateReport() [isselectedinstrument="
        //                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
        //                                + "] [showing=" + UIComponent.isUIComponentShowing(this) + "]");

        vecReport = new Vector<Vector>(10);

        // Only generate a Report if this UIComponent is visible
        // There is no need to auto-truncate
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            if (getMetadataList() != null)
                {
                final Iterator<Metadata> iterMetadata;

                LOGGER.debug(LOADER_PROPERTIES.isTimingDebug(),
                             SOURCE + "IS VISIBLE [instrument=" + getHostInstrument().getInstrument().getIdentifier() + "]");

                iterMetadata = getMetadataList().iterator();

                while (iterMetadata.hasNext())
                    {
                    final Metadata metaData;

                    metaData = iterMetadata.next();

                    if (metaData != null)
                        {
                        final Vector<Object> vecRow;

                        vecRow = new Vector<Object>(defineColumnWidths().length);

                        // Remember that all data entries must be Strings
                        vecRow.add(metaData.getKey());
                        vecRow.add(metaData.getValue());
                        vecRow.add(metaData.getUnits().toString());
                        vecRow.add(metaData.getDataTypeName().toString());
                        vecRow.add(metaData.getDescription());

                        vecReport.add(vecRow);
                        }
                    }

                // Sort the Metadata by their keys (column 0)
                Collections.sort(vecReport, new ReportRowsByColumn(COLUMN_INDEX_KEY));
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
     * Get the List of Metadata upon which the Report is based.
     *
     * @return List<Metadata>
     */

    private List<Metadata> getMetadataList()
        {
        return (this.listMetadata);
        }


    /***********************************************************************************************
     * Set the List of Metadata for this Report.
     *
     * @param metadata
     */

    public void setMetadataList(final List<Metadata> metadata)
        {
        this.listMetadata = metadata;
        }


    /***********************************************************************************************
     * Read all the Resources required by the MetadataExpanderUIComponent.
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
