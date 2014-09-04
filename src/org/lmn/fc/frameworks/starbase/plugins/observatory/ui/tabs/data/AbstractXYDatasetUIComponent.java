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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data;

import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.reports.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * A general purpose AbstractXYDatasetUIComponent.
 */

public abstract class AbstractXYDatasetUIComponent extends ReportTable
                                                   implements ReportTablePlugin,
                                                              FrameworkConstants,
                                                              FrameworkStrings,
                                                              FrameworkMetadata
    {
    private static final String HEADER = "Data Report created at";
    private static final String MSG_DISPOSE_0 = "All data will be permanently removed!";
    private static final String MSG_DISPOSE_1 = "Do you wish to preserve the data?";
    public static final String XY_DATA_ICON_FILENAME = "toolbars/toolbar-xy-data.png";

    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;

    private XYDataset xyDataset;
    private DatasetType datasetType;
    private int intChannelCount;


    /***********************************************************************************************
     * Construct a AbstractXYDatasetUIComponent.
     *
     * @param task
     * @param hostinstrument
     * @param resourcekey
     * @param name
     * @param dataviewlimit
     * @param toolbarstate
     * @param toolbaricon
     */

    public AbstractXYDatasetUIComponent(final TaskPlugin task,
                                        final ObservatoryInstrumentInterface hostinstrument,
                                        final String resourcekey,
                                        final String name,
                                        final int dataviewlimit,
                                        final ReportTableToolbar toolbarstate,
                                        final Icon toolbaricon)
        {
        super(task,
              name,
              resourcekey,
              PRINTABLE,
              EXPORTABLE,
              REFRESHABLE,
              REFRESH_CLICK,
              NON_REORDERABLE,
              NON_TRUNCATEABLE,
              LOCK_TOP_ROW,
              SCROLL_LEFT_COLUMNS,
              0,
              toolbarstate,
              toolbaricon);

        // Uniquely identify this UIComponent
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            setReportUniqueName(hostinstrument.getInstrument().getName()+ SPACE + getReportTabName());
            }

        hostInstrument = hostinstrument;
        setDataViewLimit(dataviewlimit);

        xyDataset = null;
        datasetType = DatasetType.TIMESTAMPED;
        intChannelCount = 1;
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

        vecHeader.add(getReportUniqueName());
        vecHeader.add("This report shows the Processed Data, i.e. after any filtering performed on the Raw Data");

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

        vecColumns = new Vector<ReportColumnMetadata>(getChannelCount() + 3);

        vecColumns.add(new ReportColumnMetadata(TITLE_INDEX,
                                                SchemaDataType.STRING,
                                                SchemaUnits.DIMENSIONLESS,
                                                EMPTY_STRING,
                                                SwingConstants.LEFT ));
        switch (getDatasetType())
            {
            case XY:
                {
                vecColumns.add(new ReportColumnMetadata(TITLE_X,
                                                        SchemaDataType.STRING,
                                                        SchemaUnits.DIMENSIONLESS,
                                                        EMPTY_STRING,
                                                        SwingConstants.LEFT ));
                break;
                }

            case TIMESTAMPED:
                {
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
                break;
                }

            default:
                {
                LOGGER.error("AbstractXYDatasetUIComponent.defineColumns() Invalid DatasetType");
                }
            }

        for (int i = 0;
             ((getChannelCount() > 0) && (i < getChannelCount()));
             i++)
            {
            vecColumns.add(new ReportColumnMetadata(TITLE_CHANNEL + i,
                                                    SchemaDataType.STRING,
                                                    SchemaUnits.DIMENSIONLESS,
                                                    EMPTY_STRING,
                                                    SwingConstants.LEFT ));
            }

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

        listColumnWidths = new ArrayList<String>(getChannelCount() + 3);
        listColumnWidths.add("MMMMMMM");    // Index

        switch (getDatasetType())
            {
            case XY:
                {
                listColumnWidths.add("MMMMMMM");    // X-value
                break;
                }

            case TIMESTAMPED:
                {
                listColumnWidths.add("MMMMMMM");    // Date
                listColumnWidths.add("MMMMMMM");    // Time
                break;
                }

            default:
                {
                LOGGER.error("AbstractVectorDataUIComponent.defineColumnWidths() Invalid DatasetType");
                }
            }

        for (int i = 0;
             ((getChannelCount() > 0) && (i < getChannelCount()));
             i++)
            {
            listColumnWidths.add("MMMMMM");
            }

        return (listColumnWidths.toArray());
        }


    /***********************************************************************************************
     * Generate the report data table.
     *
     * @return Vector of report rows
     */

    public abstract Vector<Vector> generateReport();


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
     * Dispose of the Report.
     */

    public void disposeReport()
        {
        final int intChoice;
        final String [] strMessage =
            {
            MSG_DISPOSE_0,
            MSG_DISPOSE_1
            };

        intChoice = JOptionPane.showOptionDialog(null,
                                                 strMessage,
                                                 getReportUniqueName(),
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 null,
                                                 null);

        // Try to confuse the User to protect them....
        // by asking twice, and switching the response
        if (intChoice == JOptionPane.NO_OPTION)
            {
            super.disposeReport();

            if (getXyDataset() != null)
                {
                setXyDataset(null, getChannelCount());
                refreshReport();
                }
            }
        }


    /***********************************************************************************************
     * Get the XYDataset to be shown on the AbstractXYDatasetUIComponent.
     *
     * @return XYDataset
     */

    public XYDataset getXyDataset()
        {
        // Supported datasets: XYSeriesCollection and TimeSeriesCollection
        return (this.xyDataset);
        }


    /***********************************************************************************************
     * Set the XYDataset to be shown on the AbstractXYDatasetUIComponent.
     * Adjust the type of the Dataset and the number of channels if required.
     * Supported datasets: XYSeriesCollection and TimeSeriesCollection.
     *
     * @param dataset
     * @param channelcount
     */

    public void setXyDataset(final XYDataset dataset,
                             final int channelcount)
        {
        final String SOURCE = "AbstractXYDatasetUIComponent.setXyDataset()";

        if ((dataset != null)
            && (dataset.getSeriesCount() > 0)
            && (channelcount > 0))
            {
            boolean boolDatasetTypeChanged;
            boolean boolChannelsChanged;
            final DatasetType type;

            // Check to see if we need to re-initialise the Report
            boolDatasetTypeChanged = false;

            // Supported datasets: XYSeriesCollection and TimeSeriesCollection
            if (dataset instanceof XYSeriesCollection)
                {
                type = DatasetType.XY;
                }
            else if (dataset instanceof TimeSeriesCollection)
                {
                type = DatasetType.TIMESTAMPED;
                }
            else
                {
                LOGGER.error(SOURCE + " Unexpected DatasetType");
                type = DatasetType.XY;
                }

            // Has the DatasetType changed?
            if (!getDatasetType().getName().equals(type.getName()))
                {
                //LOGGER.debugProtocolEvent(SOURCE + " DatasetType changed [type=" + type.getName() + "]");
                setDatasetType(type);
                boolDatasetTypeChanged = true;
                }

            // Has the number of channels changed?
            boolChannelsChanged = (channelcount != getChannelCount());

            // Set the ChannelCount regardless...
            setChannelCount(channelcount);

            //LOGGER.debugProtocolEvent(SOURCE + " Channel count changed [count=" + getChannelCount() + "]");

            if (boolDatasetTypeChanged || boolChannelsChanged)
                {
                // Regenerate the Report with the new DatasetType and/or number of channels with defineColumns()
                initialiseUI();
                }

            // Check the dataset SeriesCount
            if (dataset.getSeriesCount() == getChannelCount())
                {
                // Save a reference to the XYDataset if all is well
                this.xyDataset = dataset;
                }
            else
                {
                LOGGER.error(SOURCE + " Supplied with invalid data (e.g. incorrect Channel or Series Count) [expected_count="
                                + getChannelCount() + "] [dataseries_count=" + dataset.getSeriesCount() + "]");
                this.xyDataset = null;
                }
            }
        else
            {
            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                   SOURCE + " Supplied with invalid channel count or no XYdataset [count=" + channelcount + "]");
            this.xyDataset = null;
            }
        }


    /***********************************************************************************************
     * Get the DatasetType.
     *
     * @return DatasetType
     */

    protected DatasetType getDatasetType()
        {
        return (this.datasetType);
        }


    /***********************************************************************************************
     * Set the DatasetType.
     *
     * @param type
     */

    private void setDatasetType(final DatasetType type)
        {
        this.datasetType = type;
        }


    /***********************************************************************************************
     * Get the Channel count.
     *
     * @return int
     */

    public int getChannelCount()
        {
        return (this.intChannelCount);
        }


    /***********************************************************************************************
     * Set the Channel count.
     *
     * @param channels
     */

    private void setChannelCount(final int channels)
        {
        this.intChannelCount = channels;
        }


    /***********************************************************************************************
     * Read all the Resources required by the AbstractXYDatasetUIComponent.
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

    protected synchronized ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    protected synchronized ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }
    }
