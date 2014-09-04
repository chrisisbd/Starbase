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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.sandbox;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***************************************************************************************************
 * The Sandbox.
 */

public final class Sandbox extends AbstractObservatoryInstrument
                           implements ObservatoryInstrumentInterface
    {
    /***********************************************************************************************
     * Construct a Sandbox.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public Sandbox(final Instrument instrument,
                   final AtomPlugin plugin,
                   final ObservatoryUIInterface hostui,
                   final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);
        }


    /***********************************************************************************************
     * Initialise the Sandbox.
     */

    public final void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;

        // Read the Resources for the Sandbox
        readResources();

        // Set up the Instrument
        super.initialise();

        // Create and initialise the Sandbox ControlPanel
        controlPanel = new SandboxControlPanel(this,
                                               getInstrument(),
                                               getHostUI(),
                                               (TaskPlugin)getHostAtom().getRootTask(),
                                               getFontData(),
                                               getColourData(),
                                               getResourceKey(),
                                               ControlPanelInterface.INDICATOR_COUNT_4);
        setControlPanel(controlPanel,
                        getInstrument().getName());
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        // Create an Sandbox InstrumentPanel and initialise it
        instrumentPanel = new SandboxInstrumentPanel(this,
                                                     getInstrument(),
                                                     getHostUI(),
                                                     (TaskPlugin)getHostAtom().getRootTask(),
                                                     getFontData(),
                                                     getColourData(),
                                                     getResourceKey(),
                                                     ControlPanelInterface.INDICATOR_COUNT_4);
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Read all the Resources required by the Sandbox.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "Sandbox [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
