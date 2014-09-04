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

import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * A GpsReceiver.
 */

public final class GpsReceiver extends AbstractObservatoryInstrument
                               implements ObservatoryInstrumentInterface
    {
    // String Resources
    private static final String PARAM_NAME_SOURCE = "FixUpdate.Source";
    private static final String PARAM_NAME_TARGET = "FixUpdate.Target";
    private static final String WARNING_UNABLE_TO_UPDATE = "Unable to update the location ";

    private boolean boolEnableDebug;


    /***********************************************************************************************
     * Construct a GpsReceiver.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public GpsReceiver(final Instrument instrument,
                       final AtomPlugin plugin,
                       final ObservatoryUIInterface hostui,
                       final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);

        // ToDo This is an 'old code' way of enabling debug, and should be changed
        this.boolEnableDebug = false;
        }


    /***********************************************************************************************
     * Initialise the GpsReceiver.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;

        // Read the Resources for the GpsReceiver
        readResources();

        super.initialise();

        // Create and initialise the GpsReceiverControlPanel
        controlPanel = new GpsReceiverControlPanel(this,
                                                   getInstrument(),
                                                   getHostUI(),
                                                   (TaskPlugin)getHostAtom().getRootTask(),
                                                   getFontData(),
                                                   getColourData(),
                                                   getResourceKey());
        setControlPanel(controlPanel,
                        getInstrument().getName());
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        // Create a GpsReceiverInstrumentPanel and initialise it
        instrumentPanel = new GpsReceiverInstrumentPanel(this,
                                                         getInstrument(),
                                                         getHostUI(),
                                                         (TaskPlugin)getHostAtom().getRootTask(),
                                                         getFontData(),
                                                         getColourData(),
                                                         getResourceKey());
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();
        }


    /***********************************************************************************************
     * This method is called (on the Event Dispatching Thread)
     * by the Execute SwingWorker when the operation is complete and data are available.
     * The Instrument may pass data to a UIComponent, or perform further processing.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     *
     * @param daowrapper
     * @param forcerefreshdata
     * @param updatemetadata
     */

    public void setWrappedData(final DAOWrapperInterface daowrapper,
                               final boolean forcerefreshdata,
                               final boolean updatemetadata)
        {
        final String SOURCE = "GpsReceiver.setWrappedData() ";
        final List<String> errors;

        errors = new ArrayList<String>(10);

        if (daowrapper != null)
            {
            // Pass the DAO data on to the Instrument Panel
            super.setWrappedData(daowrapper, forcerefreshdata, updatemetadata);

            // Find the Update Source and Target
            if ((daowrapper.getObservationMetadata() != null)
                && (!daowrapper.getObservationMetadata().isEmpty()))
                {
                try
                    {
                    final String strSource;
                    final String strTarget;
                    final UpdateSource updateSource;
                    final UpdateTarget updateTarget;

                    strSource = MetadataHelper.getMetadataValueByKey(daowrapper.getObservationMetadata(),
                                                                     MetadataDictionary.KEY_OBSERVATION_ROOT.getKey() + PARAM_NAME_SOURCE);
                    strTarget = MetadataHelper.getMetadataValueByKey(daowrapper.getObservationMetadata(),
                                                                     MetadataDictionary.KEY_OBSERVATION_ROOT.getKey() + PARAM_NAME_TARGET);

                    updateSource = UpdateSource.getUpdateSourceForName(strSource);
                    updateTarget = UpdateTarget.getUpdateTargetForName(strTarget);

                    if ((updateSource != null)
                        && (updateTarget != null))
                        {
                        // Save time by checking explicitly for NoUpdate
                        if (!UpdateTarget.NO_UPDATE.equals(updateTarget))
                            {
                            final int INDEX_LONGITUDE = 0;
                            final int INDEX_LATITUDE = 1;
                            final int INDEX_HASL = 2;

                            LOGGER.debugGpsEvent(isDebugMode(),
                                                 SOURCE + "Attempting to apply GPS fix [source=" + updateSource.getName()
                                                    + "] [target=" + updateTarget.getName() + "]");

                            // Do we need to get the fix from the CentroidOfFixes, or the latest single fix?
                            switch (updateSource)
                                {
                                case CENTROID:
                                    {
                                    GpsReceiverHelper.getFixAndUpdate(this,
                                                                      daowrapper.getObservationMetadata(),
                                                                      updateTarget,
                                                                      MetadataDictionary.KEY_OBSERVATION_CENTROID_LONGITUDE.getKey(),
                                                                      MetadataDictionary.KEY_OBSERVATION_CENTROID_LATITUDE.getKey(),
                                                                      // ToDo Review if an averaged HASL is required, rather than the latest fix
                                                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + INDEX_HASL,
                                                                      errors,
                                                                      isDebugMode());
                                    break;
                                    }

                                case SINGLE:
                                    {
                                    GpsReceiverHelper.getFixAndUpdate(this,
                                                                      daowrapper.getObservationMetadata(),
                                                                      updateTarget,
                                                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + INDEX_LONGITUDE,
                                                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + INDEX_LATITUDE,
                                                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + INDEX_HASL,
                                                                      errors,
                                                                      isDebugMode());
                                    break;
                                    }

                                default:
                                    {
                                    errors.add("Update Source is incorrectly specified");
                                    }
                                }
                            }
                        else
                            {
                            // NoUpdate, so there's nothing to do!
                            // This is not an error
                            }
                        }
                    else
                        {
                        // Unable to proceed
                        errors.add("Update Source and/or Target are incorrectly specified");
                        }
                    }

                catch (DegMinSecException exception)
                    {
                    errors.add(WARNING_UNABLE_TO_UPDATE
                                   + SPACE
                                   + METADATA_EXCEPTION
                                   + exception.getMessage()
                                   + TERMINATOR);
                    }

                catch (NumberFormatException exception)
                    {
                    errors.add(WARNING_UNABLE_TO_UPDATE
                                   + SPACE
                                   + METADATA_EXCEPTION
                                   + exception.getMessage()
                                   + TERMINATOR);
                    }
                }
            else
                {
                // No Metadata, so we have no data to use
                errors.add("Warning: No Observation Metadata supplied, so fix update is not possible");
                }
            }

        LOGGER.errors(SOURCE, errors);
        }


    /***********************************************************************************************
     * Get the number of columns in the ObservatoryInstrument InstrumentLog.
     * Instruments must override this if their logs are different from the default.
     *
     * @return int
     */

    public int getInstrumentLogWidth()
        {
        return (GpsReceiverHelper.INSTRUMENT_LOG_COLUMN_COUNT);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument InstrumentLog Metadata,
     * i.e describing the columns of the InstrumentLog.
     * Instruments must override this if their log implementations are different from the default.
     * SimpleEventLogUIComponent has five columns: Icon, Date, Time, Event, Source.
     *
     * @return List<Metadata>
     */

    public List<Metadata> getInstrumentLogMetadata()
        {
        return (GpsReceiverHelper.createGpsReceiverInstrumentLogMetadata());
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     *  Read all the Resources required by the GpsReceiver.
     */

    public void readResources()
        {
        LOGGER.debugGpsEvent(isDebugMode(),
                             "GpsReceiver [ResourceKey=" + getResourceKey() + "]");

        super.readResources();

        boolEnableDebug = REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG);
        }


    /***********************************************************************************************
     * Indicate if the Instrument is in debug mode.
     *
     * @return boolean
     */

    public boolean isDebugMode()
        {
        return (boolEnableDebug);
        }
    }
