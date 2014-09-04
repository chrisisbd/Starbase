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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.starinetpythonlogger;

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
import org.lmn.fc.ui.widgets.IndicatorInterface;
import org.lmn.fc.ui.widgets.impl.ToolbarIndicator;

import java.awt.*;


/***************************************************************************************************
 * The StarinetLoggerControlPanel.
 */

public final class StarinetPythonLoggerControlPanel extends InstrumentUIComponentDecorator
                                              implements ControlPanelInterface
    {
    // String Resources
    private static final String ICON_CONTROL = "starinet-logger-control.png";
    private static final String DEFAULT_FORMAT = "0000 mV";


    /***********************************************************************************************
     * Construct the StarinetLoggerControlPanel.
     *
     * @param instrument
     * @param instrumentxml
     * @param obsui
     * @param task
     * @param font
     * @param colour
     * @param resourcekey
     * @param indicatorcount
     */

    public StarinetPythonLoggerControlPanel(final ObservatoryInstrumentInterface instrument,
                                      final Instrument instrumentxml,
                                      final ObservatoryUIInterface obsui,
                                      final TaskPlugin task,
                                      final FontInterface font,
                                      final ColourInterface colour,
                                      final String resourcekey,
                                      final int indicatorcount)
        {
        super(instrument,
              instrumentxml,
              obsui,
              task,
              font,
              colour,
              resourcekey,
              indicatorcount);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        // The original arrangement with one indicator showing the IP address
//        resetControlPanelIndicators();
//
//        // IP Address
//        setIndicator0(new ToolbarIndicator(DIM_CONTROL_PANEL_INDICATOR_SINGLE,
//                                           UNKNOWN_IP_ADDRESS,
//                                           EMPTY_STRING));
//        getIndicator0().setAlignmentY(Component.CENTER_ALIGNMENT);
//        getIndicator0().setValueFormat(DEFAULT_IP_ADDRESS_FORMAT);
//
//        getControlPanelIndicators().add(getIndicator0());
//
//        // Default Keys if IndicatorMetadataKeys are not specified in the Instrument
//        getControlPanelIndicatorDefaultValueKeys().add(MetadataDictionary.KEY_INSTRUMENT_RESERVED_ADDRESS.getKey());
//
//        getControlPanelIndicatorDefaultUnits().add(SchemaUnits.DIMENSIONLESS);
//
//        getControlPanelIndicatorDefaultTooltipKeys().add(MetadataDictionary.KEY_INSTRUMENT_RESERVED_ADDRESS.getKey());
//
//        InstrumentUIHelper.assembleControlPanel(this, this, ICON_CONTROL);

        resetControlPanelIndicators();

        for (int intIndicatorIndex = 0;
             ((getIndicatorCount() <= MAX_CONTROL_PANEL_INDICATORS)
              && (intIndicatorIndex < getIndicatorCount()));
             intIndicatorIndex++)
            {
            final IndicatorInterface indicator;

            indicator = new ToolbarIndicator(DIM_CONTROL_PANEL_INDICATOR_DOUBLE,
                                             QUERY,
                                             EMPTY_STRING);
            indicator.setAlignmentY(Component.CENTER_ALIGNMENT);
            indicator.setValueFormat(DEFAULT_FORMAT);
            getControlPanelIndicators().add(indicator);
            InstrumentUIHelper.setIndicatorAtIndex(this, indicator, intIndicatorIndex);

            // Default Keys if IndicatorMetadataKeys are not specified in the Instrument
            getControlPanelIndicatorDefaultValueKeys().add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_VALUE.getKey() + intIndicatorIndex);
            getControlPanelIndicatorDefaultUnits().add(SchemaUnits.M_V);
            getControlPanelIndicatorDefaultTooltipKeys().add(MetadataDictionary.KEY_OBSERVATION_CHANNEL_DESCRIPTION.getKey() + intIndicatorIndex);
            }

        InstrumentUIHelper.assembleControlPanel(this, this, ICON_CONTROL);
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        LOGGER.debugIndicators("StarinetPythonLoggerControlPanel.instrumentChanged() --> update indicators");

        super.instrumentChanged(event);

        // Update the Indicators using the real Metadata if possible, defaults if not
        InstrumentUIHelper.updateControlPanelIndicators(event, this);
        }
    }
