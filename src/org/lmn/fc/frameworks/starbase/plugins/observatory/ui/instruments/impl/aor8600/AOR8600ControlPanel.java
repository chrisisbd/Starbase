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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.aor8600;

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


/***************************************************************************************************
 * The AOR8600ControlPanel.
 */

public final class AOR8600ControlPanel extends InstrumentUIComponentDecorator
                                       implements ControlPanelInterface
    {
    // String Resources
    private static final String ICON_CONTROL = "aor8600-control.png";


    /***********************************************************************************************
     * Construct the AOR8600ControlPanel.
     *
     * @param instrument
     * @param oui
     * @param instrumentxml
     * @param colour
     * @param font
     * @param resourcekey
     * @param task
     */

    public AOR8600ControlPanel(final ObservatoryInstrumentInterface instrument,
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
              INDICATOR_COUNT_0);
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        resetControlPanelIndicators();

        InstrumentUIHelper.assembleControlPanel(this, this, ICON_CONTROL);
        }


    /***********************************************************************************************
     * Indicate that the state of the Instrument has changed.
     *
     * @param event
     */

    public void instrumentChanged(final InstrumentStateChangedEvent event)
        {
        LOGGER.debugIndicators("AOR8600ControlPanel.instrumentChanged() --> update indicators");

        super.instrumentChanged(event);

        // Update the Indicators using the real Metadata if possible, defaults if not
        InstrumentUIHelper.updateControlPanelIndicators(event, this);
        }
    }
