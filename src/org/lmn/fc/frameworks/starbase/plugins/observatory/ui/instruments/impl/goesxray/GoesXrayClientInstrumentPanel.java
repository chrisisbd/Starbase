// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.goesxray;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentPanelTabFactory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ChartUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.charts.GOESChartUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * The GoesXrayClientInstrumentPanel.
 */

public final class GoesXrayClientInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER_GOES = "goes-header.png";

    private static final String INSTRUMENT_HELP = "GoesXrayHelp.html";


    /***********************************************************************************************
     * Construct a GoesXrayClientInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public GoesXrayClientInstrumentPanel(final ObservatoryInstrumentInterface instrument,
                                         final Instrument instrumentxml,
                                         final ObservatoryUIInterface hostui,
                                         final TaskPlugin task,
                                         final FontInterface font,
                                         final ColourInterface colour,
                                         final String resourcekey)
        {
        super(instrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);
        }


    /***********************************************************************************************
     * Initialise the GoesXrayClientInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER_GOES,
                                                          getFontData(),
                                                          getColourData());

        // Create the GoesXrayClientInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /**********************************************************************************************/
    /* DAO                                                                                        */
    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean updatedata,
                               final boolean updatemetadata)
        {
        super.setWrappedData(daowrapper,
                             updatedata,
                             updatemetadata);

        // GOES Xray Chart
        if (getWrappedData() != null)
            {
            setChartDataset(getWrappedData().getWrappedDAO(),
                            getWrappedData().getXYDataset(),
                            updatedata);
            }
        }


    /***********************************************************************************************
     * Set the XYDataset to be displayed on the Chart tab, a GOESChartUIComponent.
     * The logarithmic chart must not contain zero or negative values, so remove these.
     *
     * @param dataset
     * @param refreshdata
     */

    private void setChartDataset(final ObservatoryInstrumentDAOInterface dao,
                                 final XYDataset dataset,
                                 final boolean refreshdata)
        {
        final String SOURCE = "GoesXrayClientInstrumentPanel.setChartDataset() ";

        if ((dao != null)
            && (dataset != null)
            && (dataset.getSeriesCount() > 0)
            && (dataset instanceof TimeSeriesCollection))
            {
            final TimeSeriesCollection collection;
            final List<TimeSeries> listSeries;
            final Vector<Boolean> vecDeletion;
            Iterator<TimeSeries> iterTimeSeries;

            // Process the dataset to remove any negative values from any channel
            // These values are used by GOES to indicate missing data,
            // and cannot be plotted directly on the logarithmic chart
            // If the dataset is not a TimeSeriesCollection, we don't know how to delete
            collection = (TimeSeriesCollection)dataset;

            // Assume that all channels are of the same length, so use channel 0
            vecDeletion = new Vector<Boolean>(collection.getSeries(0).getItemCount());

            // Initialise the deletion vector
            for (int i = 0;
                 i < collection.getSeries(0).getItemCount();
                 i++)
                {
                vecDeletion.add(false);
                }

            listSeries = collection.getSeries();
            iterTimeSeries = listSeries.iterator();

            // Scan each channel of data for negative values
            while (iterTimeSeries.hasNext())
                {
                final TimeSeries timeSeries;

                timeSeries = iterTimeSeries.next();

                for (int j = 0; j < timeSeries.getItemCount(); j++)
                    {
                    if ((timeSeries.getValue(j) instanceof Double)
                        && (timeSeries.getValue(j).doubleValue() <= 0))
                        {
                        // Mark this entry for deletion later...
                        //System.out.println("marking for deletion [item=" + j + "] [value=" + timeSeries.getValue(j) + "]");
                        vecDeletion.set(j, true);
                        }
                    }
                }

            // Save some time if there are no deletions at all
            if (vecDeletion.contains(true))
                {
                int intDeletionCount;

                intDeletionCount = 0;

                // Now do any deletions
                // Any index marked for deletion must be deleted from ALL series
                for (int i = 0;
                     i < vecDeletion.size();
                     i++)
                    {
                    if (vecDeletion.get(i))
                        {
                        // Delete this index from ALL TimeSeries (channels)
                        iterTimeSeries = listSeries.iterator();

                        // Scan each channel of data
                        while (iterTimeSeries.hasNext())
                            {
                            final TimeSeries timeSeries;

                            timeSeries = iterTimeSeries.next();
                            //System.out.println("do deletion for " + (i-intDeletionCount));
                            timeSeries.delete(i-intDeletionCount, i-intDeletionCount);
                            }

                        // Keep track of the deletions, so we can adjust the later deletion indexes
                        intDeletionCount++;
                        }
                    }
                }

            //LOGGER.debugTimedEvent(DataTranslator.showTimeSeriesCollection(dataset, -1).toString());
            }

        // Allow null datasets following a reset()
        if (getChartTab() != null)
            {
            if (getChartTab() instanceof GOESChartUIComponent)
                {
                ((GOESChartUIComponent) getChartTab()).setDatasetsAndMetadata(dataset);
                }

            // Force an immediate update
            if ((getChartTab() instanceof ChartUIComponentPlugin)
                && (UIComponentHelper.shouldRefresh(refreshdata, getHostInstrument(), getChartTab())))
                {
                ((ChartUIComponentPlugin) getChartTab()).refreshChart(dao,
                                                                      UIComponentHelper.shouldRefresh(refreshdata, getHostInstrument(), getChartTab()),
                                                                      SOURCE);
                }
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the GoesXrayClientInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addGOESChartTab(this,
                                                  TAB_CHART,
                                                  null,
                                                  DataUpdateType.PRESERVE,
                                                  REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addImageTab(this,
                                              TAB_IMAGE,
                                              null,
                                              REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addRawDataTab(this,
                                                TAB_RAW_DATA,
                                                REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEventLogTab(this,
                                                 TAB_EVENT_LOG,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addConfigurationTab(this,
                                                      TAB_CONFIGURATION,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addInstrumentNewsreaderTab(this,
                                                             TAB_NEWS,
                                                             REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addCommandLexiconTab(this,
                                                       TAB_COMMAND_LEXICON,
                                                       REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addXmlTab(this,
                                            TAB_XML,
                                            REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addHelpTab(this,
                                             TAB_HELP,
                                             FileUtilities.html,
                                             INSTRUMENT_HELP,
                                             REGISTRY_MODEL.getLoggedInUser());

        // Initialise all UIComponentPlugins on the tabs of the JTabbedPane
        // This will apply ContextActions for each UIComponentPlugin
        UIComponentHelper.initialiseAllTabComponents(getTabbedPane());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the UIComponent
        this.add(getTabbedPane());
        }
    }
