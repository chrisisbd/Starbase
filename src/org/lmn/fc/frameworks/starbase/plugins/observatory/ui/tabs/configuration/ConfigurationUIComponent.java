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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.configuration;

import org.lmn.fc.common.comparators.ReportRowsByColumn;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.components.UIComponentHelper;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.*;


/***************************************************************************************************
 * The ConfigurationUIComponent.
 */

public class ConfigurationUIComponent extends ReportTable
                                      implements ReportTablePlugin
    {
    // String Resources
    private static final String MSG_REPORT_CREATED  = "Configuration Report created at";

    private static final String TITLE_ICON          = SPACE;
    public static final String TITLE_UPDATED        = "Updated";
    public static final String TITLE_PROPERTY       = "Property";
    public static final String TITLE_VALUE          = "Value";

    private static final int COLUMN_INDEX_ICON      = 0;
    private static final int COLUMN_INDEX_UPDATED   = 1;
    private static final int COLUMN_INDEX_KEY       = 2;
    private static final int COLUMN_INDEX_VALUE     = 3;

    private final ObservatoryInstrumentInterface hostInstrument;
    private final String strPropertyResourceKey;


    /***********************************************************************************************
     * Construct a ConfigurationUIComponent for Properties in the Registry with the
     * specified PropertyResourceKey, and any Instrument Configuration.
     * The Report ResourceKey is always that of the host Framework, since this is a general utility.
     *
     * @param task
     * @param hostinstrument
     * @param reportname
     * @param reportresourcekey
     * @param propertyresourcekey
     */

    public ConfigurationUIComponent(final RootPlugin task,
                                    final ObservatoryInstrumentInterface hostinstrument,
                                    final String reportname,
                                    final String reportresourcekey,
                                    final String propertyresourcekey)
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
              ReportTableToolbar.HORIZ_NORTH_PRT,
              RegistryModelUtilities.getAtomIcon(hostinstrument.getHostAtom(),
                                                 ObservatoryInterface.FILENAME_ICON_CONFIGURATION));

        this.hostInstrument = hostinstrument;
        this.strPropertyResourceKey = propertyresourcekey;
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

        vecHeader.add(MSG_REPORT_CREATED + SPACE + getObservatoryClock().getDateTimeNowAsString());

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

        vecColumns.add(new ReportColumnMetadata(TITLE_ICON,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_UPDATED,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.CENTER));
        vecColumns.add(new ReportColumnMetadata(TITLE_PROPERTY,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT));
        vecColumns.add(new ReportColumnMetadata(TITLE_VALUE,
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
            ReportIcon.getIcon(ICON_DUMMY),
            Boolean.TRUE,
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
            "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
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

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "ConfigurationUIComponent.generateReport() [isselectedinstrument="
                                + ObservatoryUIHelper.isSelectedInstrument(getHostInstrument())
                                + "] [visible=" + UIComponentHelper.isUIComponentShowing(this) + "]");

        vecReport = new Vector<Vector>(50);

        // List all of the Properties in the Registry with this ResourceKey
        // Only generate a Report if this UIComponent is visible
        // There is no need to auto-truncate
        if (UIComponentHelper.shouldRefresh(false, getHostInstrument(), this))
            {
            final List<String> listKeys;

            // Record all Keys
            listKeys = new ArrayList<String>(50);

            if (getPropertyResourceKey() != null)
                {
                final Hashtable<String, RootPlugin> properties;
                final Enumeration<String> enumKeys;

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "ConfigurationUIComponent.generateReport() VISIBLE");

                // First all all those Properties in the Registry with the specified Key
                properties = REGISTRY.getProperties();
                enumKeys = properties.keys();

                while (enumKeys.hasMoreElements())
                    {
                    final String key;

                    key = enumKeys.nextElement();

                    if (key.startsWith(getPropertyResourceKey()))
                        {
                        final RootPlugin resource;

                        resource = properties.get(key);

                        if (!listKeys.contains(key))
                            {
                            final Vector<Object> vecRow;

                            listKeys.add(key);
                            vecRow = new Vector<Object>(defineColumnWidths().length);

                            // Remember that all data entries must be Strings
                            vecRow.add(ReportIcon.getIcon(resource.getIconFilename()));
                            vecRow.add(resource.isUpdated());
                            vecRow.add(key);
                            vecRow.add(((ResourcePlugin)resource).getResource().toString());

                            vecReport.add(vecRow);
                            }
                        }
                    }
                }

            // Append any extra data, which may have nothing to do with the Registry (e.g. the Port)
            // The format is Icon:Updated:Key:Value
            getHostInstrument().setInstrumentConfiguration(
                    ConfigurationHelper.assembleInstrumentConfiguration(getHostInstrument()));

            if ((getHostInstrument().getInstrumentConfiguration() != null)
                && (!getHostInstrument().getInstrumentConfiguration().isEmpty()))
                {
                final Iterator<Vector> iterConfiguration;

                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "ConfigurationUIComponent.generateReport() InstrumentConfiguration size=" + getHostInstrument().getInstrumentConfiguration().size());

                iterConfiguration = getHostInstrument().getInstrumentConfiguration().iterator();

                while (iterConfiguration.hasNext())
                    {
                    final Vector vecRowData;

                    vecRowData = iterConfiguration.next();

                    // Don't add anything we've had before
                    if ((vecRowData != null)
                        && (vecRowData.get(COLUMN_INDEX_KEY) != null)
                        && (vecRowData.get(COLUMN_INDEX_KEY) instanceof String)
                        && (!listKeys.contains((String)vecRowData.get(COLUMN_INDEX_KEY))))
                        {
                        final Vector<Object> vecRow;

                        listKeys.add((String)vecRowData.get(COLUMN_INDEX_KEY));
                        vecRow = new Vector<Object>(defineColumnWidths().length);

                        // The format is Icon:Updated:Key:Value
                        vecRow.add(vecRowData.get(COLUMN_INDEX_ICON));
                        vecRow.add(vecRowData.get(COLUMN_INDEX_UPDATED));
                        vecRow.add(vecRowData.get(COLUMN_INDEX_KEY));
                        vecRow.add(vecRowData.get(COLUMN_INDEX_VALUE));

                        vecReport.add(vecRow);
                        }
                    }
                }

            // Sort the Properties by their keys (column 2)
            Collections.sort(vecReport, new ReportRowsByColumn(COLUMN_INDEX_KEY));
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   "ConfigurationUIComponent.generateReport() NOT VISIBLE");
            }

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
     * Get the Property ResourceKey for the Properties to be shown on this Report.
     *
     * @return String
     */

    private String getPropertyResourceKey()
        {
        return (this.strPropertyResourceKey);
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
