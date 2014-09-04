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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.model.xmlbeans.groups.Definition;
import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.*;
import java.util.Vector;


public interface InstrumentSelector extends UIComponentPlugin
    {
    int INDEX_INITIAL_INSTRUMENT_SELECTION = 0;

    // SelectorPanel dimensions
    int RACK_PANEL_HEIGHT_MIN = 1;
    int RACK_PANEL_HEIGHT_MAX = 10;

    int WIDTH_SCROLL_BAR = 18;

    // I don't understand why this works!
    int HEIGHT_TAB = 21;
    int MARGIN_TOP_HEIGHT = 8;

    int SELECTOR_SCROLL_FUDGE_FACTOR = 9;

    // Add offset here if making a gap on the selector for scroll bar
    Dimension DIM_SELECTOR_SIZE_MIN = new Dimension(RackPanel.PANEL_1U.getPixelWidth() + WIDTH_SCROLL_BAR + SELECTOR_SCROLL_FUDGE_FACTOR,
                                                    RackPanel.PANEL_1U.getPixelHeight() - 1);
    Dimension DIM_SELECTOR_SIZE_MAX = new Dimension(RackPanel.PANEL_1U.getPixelWidth() + WIDTH_SCROLL_BAR + SELECTOR_SCROLL_FUDGE_FACTOR,
                                                    Integer.MAX_VALUE);

    // I don't understand why the scroll bar width has to be added twice!
    Dimension DIM_GROUP_STOP_SIZE = new Dimension(DIM_SELECTOR_SIZE_MIN.width + HEIGHT_TAB + WIDTH_SCROLL_BAR,
                                                     50);
    Dimension DIM_GROUP_TAB_SIZE_MIN = new Dimension(DIM_SELECTOR_SIZE_MIN.width + HEIGHT_TAB + WIDTH_SCROLL_BAR,
                                                     DIM_SELECTOR_SIZE_MIN.height);
    Dimension DIM_GROUP_TAB_SIZE_MAX = new Dimension(DIM_SELECTOR_SIZE_MAX.width + HEIGHT_TAB + WIDTH_SCROLL_BAR,
                                                     DIM_SELECTOR_SIZE_MAX.height);

    float ALIGNMENT_XY = 0.5f;

    // SelectorPanel button dimensions
    Dimension DIM_BUTTON = new Dimension(15, 15);
    int WIDTH_BUTTON_SPACING = 4;

    Dimension DIM_BUTTON_PANEL = new Dimension((DIM_BUTTON.width << 1) + WIDTH_BUTTON_SPACING,
                                               DIM_BUTTON.height + 2);
    Dimension DIM_PANEL_ICON = new Dimension(DIM_BUTTON_PANEL.width, 32);

    Dimension DIM_DIVIDER = new Dimension(10, Integer.MAX_VALUE);


    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the Container holding the SelectorPanels.
     *
     * @return Container
     */

    Container getSelectorPanelContainer();


    /**********************************************************************************************/
    /* ObservatoryInstruments                                                                     */
    /***********************************************************************************************
     * Add an ObservatoryInstrument to the List in the InstrumentSelector.
     * This does not render the Instrument, or affect its UI state.
     *
     * @param instrument
     */

    void addInstrumentToSelector(ObservatoryInstrumentInterface instrument);


    /***********************************************************************************************
     * Remove an ObservatoryInstrument from the List in the InstrumentSelector.
     * This does not render the Instrument, or affect its UI state.
     *
     * @param instrument
     */

    void removeInstrumentFromSelector(ObservatoryInstrumentInterface instrument);


    /***********************************************************************************************
     * Get the List of ObservatoryInstruments currently in the InstrumentSelector.
     *
     * @return Vector<ObservatoryInstrumentInterface>
     */

    Vector<ObservatoryInstrumentInterface> getInstrumentsOnSelector();


    /***********************************************************************************************
     * Get the currently selected Instrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    ObservatoryInstrumentInterface getSelectedInstrument();


    /***********************************************************************************************
     * Set the selected Instrument.
     *
     * @param instrument
     */

    void setSelectedInstrument(ObservatoryInstrumentInterface instrument);
    }
