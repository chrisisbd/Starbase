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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.staribusmagnetometer;

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
 * The StaribusMagnetometerControlPanel.
 */

public final class StaribusMagnetometerControlPanel extends InstrumentUIComponentDecorator
                                                    implements ControlPanelInterface
    {
    // String Resources
    private static final String ICON_CONTROL = "staribus-magnetometer-control.png";
    private static final String DEFAULT_DATA_FORMAT_0 = "+0000";
    private static final String DEFAULT_DATA_FORMAT_1 = "+0000";


    /***********************************************************************************************
     * Construct the StaribusMagnetometerControlPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param hostui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     */

    public StaribusMagnetometerControlPanel(final ObservatoryInstrumentInterface instrument,
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
              resourcekey,
              INDICATOR_COUNT_2);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        resetControlPanelIndicators();

        setIndicator0(new ToolbarIndicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                           QUERY,
                                           EMPTY_STRING));
        getIndicator0().setAlignmentY(Component.CENTER_ALIGNMENT);
        getIndicator0().setValueFormat(DEFAULT_DATA_FORMAT_0);

        setIndicator1(new ToolbarIndicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                           QUERY,
                                           EMPTY_STRING));
        getIndicator1().setAlignmentY(Component.CENTER_ALIGNMENT);
        getIndicator1().setValueFormat(DEFAULT_DATA_FORMAT_1);

        getControlPanelIndicators().add(getIndicator0());
        getControlPanelIndicators().add(getIndicator1());

        // Default Keys if IndicatorMetadataKeys are not specified in the Instrument
        getControlPanelIndicatorDefaultValueKeys().add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO);
        getControlPanelIndicatorDefaultValueKeys().add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        // These won't be displayed (Ideally nT if calibrated)
        getControlPanelIndicatorDefaultUnits().add(SchemaUnits.DIMENSIONLESS);
        getControlPanelIndicatorDefaultUnits().add(SchemaUnits.DIMENSIONLESS);

        getControlPanelIndicatorDefaultTooltipKeys().add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO);
        getControlPanelIndicatorDefaultTooltipKeys().add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE);

        InstrumentUIHelper.assembleControlPanel(this, this, ICON_CONTROL);
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        LOGGER.debugIndicators("StaribusMagnetometerControlPanel.instrumentChanged() --> update indicators");

        super.instrumentChanged(event);

        // Uodate the Indicators using the real Metadata if possible, defaults if not
        InstrumentUIHelper.updateControlPanelIndicators(event, this);
        }
    }
