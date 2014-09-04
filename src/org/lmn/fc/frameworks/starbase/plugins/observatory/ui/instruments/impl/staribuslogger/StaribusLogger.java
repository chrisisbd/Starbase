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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.staribuslogger;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrument;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;


/***********************************************************************************************
 * A generic StaribusLogger.
 */

public final class StaribusLogger extends AbstractObservatoryInstrument
                                  implements ObservatoryInstrumentInterface
    {
    private static final int INDICATOR_COUNT = 1;


    /***********************************************************************************************
     * Construct a StaribusLogger.
     *
     * @param instrument
     * @param plugin
     * @param hostui
     * @param resourcekey
     */

    public StaribusLogger(final Instrument instrument,
                          final AtomPlugin plugin,
                          final ObservatoryUIInterface hostui,
                          final String resourcekey)
        {
        super(instrument, plugin, hostui, resourcekey);
        }


    /***********************************************************************************************
     * Initialise the StaribusLogger.
     */

    public void initialise()
        {
        final InstrumentUIComponentDecoratorInterface controlPanel;
        final InstrumentUIComponentDecoratorInterface instrumentPanel;

        // Read the Resources for the StaribusLogger
        readResources();

        super.initialise();

        // Create and initialise the ControlPanel
        controlPanel = new StaribusLoggerControlPanel(this,
                                                      getInstrument(),
                                                      getHostUI(),
                                                      (TaskPlugin)getHostAtom().getRootTask(),
                                                      getFontData(),
                                                      getColourData(),
                                                      getResourceKey(),
                                                      INDICATOR_COUNT);
        setControlPanel(controlPanel,
                        getInstrument().getName());
        addInstrumentStateChangedListener(controlPanel);
        getControlPanel().initialiseUI();

        // Create a StaribusLoggerInstrumentPanel and initialise it
        instrumentPanel = new StaribusLoggerInstrumentPanel(this,
                                                            getInstrument(),
                                                            getHostUI(),
                                                            (TaskPlugin)getHostAtom().getRootTask(),
                                                            getFontData(),
                                                            getColourData(),
                                                            getResourceKey(),
                                                            INDICATOR_COUNT);
        setInstrumentPanel(instrumentPanel);
        addInstrumentStateChangedListener(instrumentPanel);
        getInstrumentPanel().initialiseUI();
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     *  Read all the Resources required by the StaribusLogger.
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "StaribusLogger [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
