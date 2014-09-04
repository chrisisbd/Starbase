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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.gpsrx;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.StarMapPointInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.plugins.impl.GpsSatellites;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.starmap.ui.StarMapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MapUIComponentPlugin;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * GpsReceiverInstrumentPanel.
 */

public final class GpsReceiverInstrumentPanel extends InstrumentUIComponentDecorator
    {
    // String Resources
    private static final String ICON_HEADER_GPS = "gps-header.png";
    private static final String TAB_SATELLITE_LOCATIONS = "Satellite Locations";
    private static final String TAB_SCATTER_PLOT = "Scatter Plot";
    private static final String TAB_GPS_FIXES = "GPS Fixes";
    private static final String INSTRUMENT_HELP = "GpsHelp.html";

    private static final Color COLOR_SATELLITE = new Color(7, 230, 23);

    private StarMapPlugin pluginGpsSatellites;


    /***********************************************************************************************
     * Construct a GpsReceiverInstrumentPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public GpsReceiverInstrumentPanel(final ObservatoryInstrumentInterface instrument,
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

        this.pluginGpsSatellites = null;
        }


    /***********************************************************************************************
     * Initialise the GpsReceiverInstrumentPanel.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        InstrumentUIHelper.configureInstrumentPanelHeader(getHeaderUIComponent(),
                                                          getObservatoryUI(),
                                                          this,
                                                          getHostTask().getParentAtom(),
                                                          getInstrument(),
                                                          ICON_HEADER_GPS,
                                                          getFontData(),
                                                          getColourData());

        // Create the GpsReceiverInstrumentPanel and add it to the host UIComponent
        createAndInitialiseTabs();
        }


    /***********************************************************************************************
     * Dispose of the GpsUI.
     */

    public final void disposeUI()
        {
        stopUI();

        if (getTabbedPane() != null)
            {
            this.pluginGpsSatellites = null;

            // Reduce resources as far as possible
            UIComponentHelper.disposeAllTabComponents(getTabbedPane());
            getTabbedPane().removeAll();
            setTabbedPane(null);
            }

        super.disposeUI();
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
        final String SOURCE = "GpsReceiverInstrumentPanel.setWrappedData() ";

        super.setWrappedData(daowrapper,
                             updatedata,
                             updatemetadata);

        //------------------------------------------------------------------------------------------
        // Update the GPS Satellite tracks on the StarMap
        // This is the only use of the RawData

        if (getGpsSatellitePlugin() != null)
            {
            if ((getWrappedData() != null)
                && (getWrappedData().getRawData() != null))
                {
                final Vector<Vector<StarMapPointInterface>> vecCollection;

                // The data arrive as Vector<Vector<SatelliteData>> in getWrappedData().getRawData()
                // and must be presented to the StarMap as Vector<Vector<StarMapPointInterface>>
                // using getGpsSatellitePlugin().setStarMapPoints()
                vecCollection = GpsReceiverHelper.convertSatelliteDataToStarMap(getWrappedData().getRawData(),
                                                                                getGpsSatellitePlugin());
                if (vecCollection != null)
                    {
                    getGpsSatellitePlugin().setStarMapPoints(vecCollection);
                    }
                else
                    {
                    getGpsSatellitePlugin().setStarMapPoints(new Vector<Vector<StarMapPointInterface>>(1));
                    }
                }
            else
                {
                // Clear the Map if there's no data - this may be a reset()
                getGpsSatellitePlugin().setStarMapPoints(new Vector<Vector<StarMapPointInterface>>(1));
                }
            }

        //------------------------------------------------------------------------------------------
        // Update the StarMap tab
        // Allow null datasets following a reset()

        if (getStarMapTab() != null)
            {
            // Force an immediate update
            if ((getStarMapTab() instanceof StarMapUIComponentPlugin)
                && (UIComponentHelper.shouldRefresh(updatedata, getHostInstrument(), getStarMapTab())))
                {
                // Force all StarMapPlugin points to be drawn
                ((StarMapUIComponentPlugin) getStarMapTab()).refreshStarMap();
                }
            }

        //------------------------------------------------------------------------------------------
        // Update the Framework PointOfInterest
        // {Longitude, Latitude, HASL} are passed via Observation.Channel.Value, since they are
        // 'observations' made by this DAO.

        if ((daowrapper != null)
            && (daowrapper.getObservationMetadata() != null)
            && (!daowrapper.getObservationMetadata().isEmpty())
            && (getRegionalMapTab() != null))
            {
            final List<PointOfInterest> listPOIs;
            String strLongitude;
            String strLatitude;
            String strHASL;
            double dblHASL;
            final List<String> errors;

            errors = new ArrayList<String>(10);

            strLongitude = MetadataHelper.getMetadataValueByKey(daowrapper.getObservationMetadata(),
                                                                MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO);
            strLatitude = MetadataHelper.getMetadataValueByKey(daowrapper.getObservationMetadata(),
                                                               MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE);
            strHASL = MetadataHelper.getMetadataValueByKey(daowrapper.getObservationMetadata(),
                                                           MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO);

            if ((NO_DATA.equals(strLongitude))
                || (NO_DATA.equals(strLatitude))
                || (NO_DATA.equals(strHASL)))
                {
                LOGGER.error(SOURCE + "One or more Metadata Keys not found [longitude="
                                     + MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO
                                     + "] [latitude="
                                     + MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE
                                     + "] [hasl="
                                     + MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_TWO + "]");

                MetadataHelper.showMetadataList(daowrapper.getObservationMetadata(),
                                                SOURCE + "GPS Receiver Metadata",
                                                true);
                }

            //-------------------------------------------------------------------------------------
            // Observation.Channel.Value data are not in the correct format, so some parsing is needed

            // Prune the 'E/W' off Longitude, the 'N/S' off Latitude, and the 'm' off HASL
            // Restore the sign in order to parse back correctly for the PointOfInterest XSD Regex!
            if (strLongitude.endsWith("E"))
                {
                // Longitude is NEGATIVE to the EAST
                strLongitude = MINUS + strLongitude.substring(0, strLongitude.length()-1);
                }
            else if (strLongitude.endsWith("W"))
                {
                // Longitude is POSITIVE to the WEST
                strLongitude = PLUS + strLongitude.substring(0, strLongitude.length()-1);
                }

            if (strLatitude.endsWith("N"))
                {
                // Latitude is POSITIVE to the NORTH
                strLatitude = PLUS + strLatitude.substring(0, strLatitude.length()-1);
                }
            else if (strLatitude.endsWith("S"))
                {
                // Latitude is NEGATIVE to the SOUTH
                strLatitude = MINUS + strLatitude.substring(0, strLatitude.length()-1);
                }

            if ((strHASL.endsWith("m"))
                || (strHASL.endsWith("M")))
                {
                // I suppose this could be negative?!
                strHASL = strHASL.substring(0, strHASL.length()-1);
                }

            try
                {
                dblHASL = Double.parseDouble(strHASL);
                }

            catch (NumberFormatException exception)
                {
                dblHASL = 0.0;
                }

            //-------------------------------------------------------------------------------------
            // TODO REVIEW USE OF MAP POIs
            // Search all of the POIs currently on the Map
            //listPOIs = ((MapUIComponentPlugin) getRegionalMapTab()).getPointOfInterestList();

            // Search the Framework POIs
            listPOIs = REGISTRY.getFramework().getPointOfInterestList();

            if ((listPOIs != null)
                && (!listPOIs.isEmpty()))
                {
                final List<Metadata> listMetadata;
                final Iterator<PointOfInterest> iterPOIs;
                boolean boolFoundIt;

                listMetadata = MetadataHelper.collectAggregateMetadataTraced(REGISTRY.getFramework(),
                                                                             getHostInstrument().getContext().getObservatory(),
                                                                             getHostInstrument(),
                                                                             getHostInstrument().getDAO(), daowrapper,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());
                iterPOIs = listPOIs.iterator();
                boolFoundIt = false;

                // Just look at all POIs, there won't be very many...
                while ((!boolFoundIt)
                    && (iterPOIs.hasNext()))
                    {
                    final PointOfInterest poi;

                    poi = iterPOIs.next();

                    // This is the only place where the Framework POI is updated
                    // We ASSUME that the Framework POI is named the same as the Framework itself
                    // Note that if the Framework POI is metadata-driven, then it has already been updated
                    // by the code in GpsReceiverHelper.getFixAndUpdate() etc.
                    // The helper returns EMPTY_STRING if the Name cannot be found

                    if ((poi != null)
                        && (REGISTRY.getFramework().getName().equals(PointOfInterestHelper.getPOIName(poi, listMetadata, SOURCE, errors))))
                        {
                        boolean boolSuccess;
                        boolean boolCompleteSuccess;

                        // Expect to succeed
                        boolCompleteSuccess = true;

                        boolSuccess = PointOfInterestHelper.setPOILongitude(poi, listMetadata, strLongitude, SOURCE, errors);

                        if (!boolSuccess)
                            {
                            SimpleEventLogUIComponent.logEvent(daowrapper.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_FRAMEWORK_UPDATE
                                                                    + METADATA_RESULT + METADATA_FAIL + SPACE
                                                                    + METADATA_REASON + "Unable to update Longitude" + TERMINATOR_SPACE
                                                                    + METADATA_LONGITUDE + strLongitude + TERMINATOR,
                                                               SOURCE,
                                                               getObservatoryClock());
                            boolCompleteSuccess = false;
                            }

                        boolSuccess = PointOfInterestHelper.setPOILatitude(poi, listMetadata, strLatitude, SOURCE, errors);

                        if (!boolSuccess)
                            {
                            SimpleEventLogUIComponent.logEvent(daowrapper.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                               METADATA_FRAMEWORK_UPDATE
                                                                   + METADATA_RESULT + METADATA_FAIL + SPACE
                                                                   + METADATA_REASON + "Unable to update Latitude" + TERMINATOR_SPACE
                                                                   + METADATA_LATITUDE + strLatitude + TERMINATOR,
                                                               SOURCE,
                                                               getObservatoryClock());
                            boolCompleteSuccess = false;
                            }

                        boolSuccess = PointOfInterestHelper.setPOIHASL(poi, listMetadata, dblHASL, SOURCE, errors);

                        if (!boolSuccess)
                            {
                            SimpleEventLogUIComponent.logEvent(daowrapper.getEventLogFragment(),
                                                               EventStatus.WARNING,
                                                                   METADATA_FRAMEWORK_UPDATE
                                                                   + METADATA_RESULT + METADATA_FAIL + SPACE
                                                                   + METADATA_REASON + "Unable to update HASL" + TERMINATOR_SPACE
                                                                   + METADATA_HASL + strHASL + TERMINATOR,
                                                               SOURCE,
                                                               getObservatoryClock());
                            boolCompleteSuccess = false;
                            }

                        if (boolCompleteSuccess)
                            {
                            SimpleEventLogUIComponent.logEvent(daowrapper.getEventLogFragment(),
                                                               EventStatus.INFO,
                                                               METADATA_FRAMEWORK_UPDATE
                                                                   + METADATA_LONGITUDE
                                                                   + strLongitude
                                                                   + TERMINATOR_SPACE
                                                                   + METADATA_LATITUDE
                                                                   + strLatitude
                                                                   + TERMINATOR_SPACE
                                                                   + METADATA_HASL
                                                                   + dblHASL
                                                                   + TERMINATOR,
                                                               SOURCE,
                                                               getObservatoryClock());
                            }

                        boolFoundIt = true;
                        }
                    }
                }

            if (LOADER_PROPERTIES.isMetadataDebug())
                {
                LOGGER.errors(SOURCE, errors);
                }
            }

        //------------------------------------------------------------------------------------------
        // Now update the Map display with the updated POI

        // Force an immediate update
        if (UIComponentHelper.shouldRefresh(updatedata, getHostInstrument(), getRegionalMapTab()))
            {
            getRegionalMapTab().runUI();
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Create and initialise the JTabbedPane for the GpsReceiverInstrumentPanel.
     */

    private void createAndInitialiseTabs()
        {
        setTabbedPane(new JTabbedPane(JTabbedPane.BOTTOM));
        configureTabbedPane();

        InstrumentPanelTabFactory.addCommandsTab(this,
                                                 TAB_COMMANDS,
                                                 REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addStarMapTab(this,
                                                TAB_STAR_MAP,
                                                REGISTRY_MODEL.getLoggedInUser());

        // Customise the StarMap: Add the GPS Satellites
        // Allow the user to click on the last point of each track, i.e. the satellite
        pluginGpsSatellites = new GpsSatellites(getObservatoryUI(),
                                                (StarMapUIComponentPlugin)getStarMapTab(),
                                                "GPS Tracker",
                                                "GPS Satellite",
                                                StarMapPointInterface.CLICKABLE,
                                                COLOR_SATELLITE);
        getGpsSatellitePlugin().setActive(true);
        ((StarMapUIComponentPlugin)getStarMapTab()).addPlugin(getGpsSatellitePlugin());

        getTabbedPane().addTab(TAB_SATELLITE_LOCATIONS,
                               (Component) getStarMapTab());

        InstrumentPanelTabFactory.addGPSScatterChartTab(this,
                                                        TAB_SCATTER_PLOT,
                                                        null,
                                                        REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addProcessedDataTab(this,
                                                      TAB_GPS_FIXES,
                                                      REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addMetadataExplorerTab(this,
                                                         TAB_META_DATA_EXPLORER,
                                                         REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addEphemeridesTab(this,
                                                    TAB_EPHEMERIDES,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addRegionalMapTab(this,
                                                    TAB_REGIONAL_MAP,
                                                    REGISTRY_MODEL.getLoggedInUser());

        InstrumentPanelTabFactory.addGPSInstrumentLogTab(this,
                                                         TAB_INSTRUMENT_LOG,
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

        // Allow export of the StarMap
        // The StarMap is not created until the StarMapUIComponent is initialised,
        // which happens in initialiseAllTabComponents()
        // Normally the ExportableComponent would be one of the tabs,
        // but in this case it is held within a tab component, so must be retrieved differently
        setExportableComponent(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_STAR_MAP,
                               ((StarMapUIComponentPlugin) getStarMapTab()).getExportableComponent());

        // Similarly add the RegionalMap
        setExportableComponent(ObservatoryInstrumentDAOInterface.EXPORTABLE_INDEX_REGIONAL_MAP,
                               ((MapUIComponentPlugin) getRegionalMapTab()).getExportableComponent());

        // Listen for clicks on the JTabbedPane
        // On click, this calls stopUI() for all tabs, then runUI() for the selected tab
        UIComponentHelper.addTabListener(getHostTask(), this, getTabbedPane());

        // Add the tabs to the host UIComponent
        this.add(getTabbedPane());
        }


    /***********************************************************************************************
     * Get the StarMapPlugin used to display the GPS Satellite tracks on the StarMap.
     *
     * @return StarMapPlugin
     */

    private StarMapPlugin getGpsSatellitePlugin()
        {
        return (this.pluginGpsSatellites);
        }
    }
