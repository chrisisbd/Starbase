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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.starcam;

import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ControlPanelInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import java.awt.*;


/***************************************************************************************************
 * The StarcamControlPanel.
 */

public final class StarcamControlPanel extends InstrumentUIComponentDecorator
                                       implements ControlPanelInterface
    {
    // String Resources
    private static final String ICON_CONTROL = "starcam-control.png";

    // Configurable Resources
    private String strImageURL;


    /***********************************************************************************************
     * Construct the StarcamControlPanel.
     *
     * @param instrument
     * @param oui
     * @param instrumentxml
     * @param colour
     * @param font
     * @param resourcekey
     * @param task
     */

    public StarcamControlPanel(final ObservatoryInstrumentInterface instrument,
                               final Instrument instrumentxml,
                               final ObservatoryUIInterface oui,
                               final TaskPlugin task,
                               final FontInterface font,
                               final ColourInterface colour,
                               final String resourcekey)
        {
        super(instrument,
              instrumentxml,
              oui,
              task,
              font,
              colour,
              resourcekey,
              INDICATOR_COUNT_1);

        this.strImageURL = EMPTY_STRING;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        resetControlPanelIndicators();

        // IP Address
        setIndicator0(new ToolbarIndicator(DIM_CONTROL_PANEL_INDICATOR_SINGLE,
                                           UNKNOWN_IP_ADDRESS,
                                           EMPTY_STRING));
        getIndicator0().setAlignmentY(Component.CENTER_ALIGNMENT);
        getIndicator0().setValueFormat(DEFAULT_IP_ADDRESS_FORMAT);

        getControlPanelIndicators().add(getIndicator0());

        // Default Keys if IndicatorMetadataKeys are not specified in the Instrument
        getControlPanelIndicatorDefaultValueKeys().add(MetadataDictionary.KEY_INSTRUMENT_RESERVED_ADDRESS.getKey());

        getControlPanelIndicatorDefaultUnits().add(SchemaUnits.DIMENSIONLESS);

        getControlPanelIndicatorDefaultTooltipKeys().add(MetadataDictionary.KEY_INSTRUMENT_RESERVED_ADDRESS.getKey());

        InstrumentUIHelper.assembleControlPanel(this, this, ICON_CONTROL);
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        LOGGER.debugIndicators("StarcamControlPanel.instrumentChanged() --> update indicators");

        super.instrumentChanged(event);

        // Update the Indicators using the real Metadata if possible, defaults if not
        InstrumentUIHelper.updateControlPanelIndicators(event, this);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     *  Read all the Resources required by the StarcamControlPanel.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StarcamControlPanel [ResourceKey=" + getResourceKey() + "]");

        strImageURL = REGISTRY.getStringProperty(getResourceKey() + KEY_DAO_IMAGE_FILENAME);
        }
    }
